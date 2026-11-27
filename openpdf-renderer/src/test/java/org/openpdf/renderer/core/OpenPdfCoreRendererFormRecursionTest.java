/*
 * Copyright 2026 the OpenPDF contributors.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 */
package org.openpdf.renderer.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

import java.awt.image.BufferedImage;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Form XObjects that draw themselves, directly or through other forms, and forms that are nested very deeply.
 */
class OpenPdfCoreRendererFormRecursionTest {

    private static final String RED_SQUARE = "1 0 0 rg 0 0 50 50 re f ";

    @Test
    void drawsFormThatDrawsItselfOnce() throws Exception {
        List<String> objects = page("/F0 Do", "/F0 4 0 R");
        objects.add(form(RED_SQUARE + "/F0 Do", "/F0 4 0 R"));

        assertThat(redPixels(render(objects))).isGreaterThan(100);
    }

    @Test
    void drawsFormsThatDrawEachOtherOnce() throws Exception {
        List<String> objects = page("/F0 Do", "/F0 4 0 R");
        objects.add(form(RED_SQUARE + "/F1 Do", "/F1 5 0 R"));
        objects.add(form("/F0 Do", "/F0 4 0 R"));

        assertThat(redPixels(render(objects))).isGreaterThan(100);
    }

    @Test
    void drawsFormsThatAreNestedAFewLevelsDeep() throws Exception {
        assertThat(redPixels(render(chainOfForms(10)))).isGreaterThan(100);
    }

    @Test
    void stopsAtFormsThatAreNestedTooDeeply() throws Exception {
        // the last form of the chain draws the square, and it is far below the limit
        assertThat(redPixels(render(chainOfForms(20_000)))).isZero();
    }

    @Test
    void drawsTheSameFormSeveralTimesOnOnePage() throws Exception {
        List<String> objects = page("/F0 Do 1 0 0 1 100 0 cm /F0 Do", "/F0 4 0 R");
        objects.add(form(RED_SQUARE, ""));

        BufferedImage image = render(objects);

        assertThat(isRed(image.getRGB(25, 175))).isTrue();
        assertThat(isRed(image.getRGB(125, 175))).isTrue();
    }

    @Test
    void stopsAtFormsThatDrawEachOtherTooOften() {
        // twelve forms that each draw the next one ten times: the last one would be drawn 10^11 times
        int levels = 12;
        List<String> objects = page("/F0 Do", "/F0 4 0 R");
        for (int k = 0; k < levels; k++) {
            boolean last = k == levels - 1;
            objects.add(last ? form(RED_SQUARE, "") : form("/F0 Do ".repeat(10), "/F0 " + (5 + k) + " 0 R"));
        }

        BufferedImage image = assertTimeoutPreemptively(Duration.ofSeconds(60), () -> render(objects));

        assertThat(redPixels(image)).isGreaterThan(100);
    }

    @Test
    void drawsFormThatIsUsedManyTimes() throws Exception {
        // 20,000 uses of one form, the last one in a different place
        String content = "/F0 Do ".repeat(20_000) + "1 0 0 1 100 0 cm /F0 Do";
        List<String> objects = page(content, "/F0 4 0 R");
        objects.add(form(RED_SQUARE, ""));

        BufferedImage image = render(objects);

        assertThat(isRed(image.getRGB(125, 175))).isTrue();
    }

    /**
     * A page that draws form 4, which draws form 5 and so on. The last form draws a red square.
     */
    private static List<String> chainOfForms(int length) {
        List<String> objects = page("/F0 Do", "/F0 4 0 R");
        for (int k = 0; k < length; k++) {
            boolean last = k == length - 1;
            objects.add(last ? form(RED_SQUARE, "") : form("/F0 Do", "/F0 " + (5 + k) + " 0 R"));
        }
        return objects;
    }

    /**
     * The first three objects of a document with one page of 200 x 200 points. The next object has number 4.
     */
    private static List<String> page(String content, String xobjects) {
        List<String> objects = new ArrayList<>();
        objects.add("<< /Type /Catalog /Pages 2 0 R >>");
        objects.add("<< /Type /Pages /Kids [3 0 R] /Count 1 >>");
        objects.add("<< /Type /Page /Parent 2 0 R /MediaBox [0 0 200 200] /Resources << /XObject << " + xobjects
                + " >> >> /Contents " + stream("", content) + " >>");
        return objects;
    }

    private static String form(String content, String xobjects) {
        return stream("/Type /XObject /Subtype /Form /BBox [0 0 200 200] /Resources << /XObject << " + xobjects
                + " >> >>", content);
    }

    private static String stream(String entries, String content) {
        return "<< " + entries + " /Length " + content.length() + " >>\nstream\n" + content + "\nendstream";
    }

    private static BufferedImage render(List<String> objects) throws Exception {
        // the page content cannot be a direct stream, so it becomes the last object
        List<String> all = new ArrayList<>(objects);
        String page = all.get(2);
        int contents = page.indexOf("/Contents ");
        all.add(page.substring(contents + "/Contents ".length(), page.length() - " >>".length()));
        all.set(2, page.substring(0, contents) + "/Contents " + all.size() + " 0 R >>");
        try (OpenPdfCoreRenderer renderer = new OpenPdfCoreRenderer(pdf(all))) {
            return renderer.renderPage(1, 72f);
        }
    }

    /**
     * Builds a PDF file from the bodies of its objects, which are numbered from 1.
     */
    private static byte[] pdf(List<String> objects) {
        StringBuilder pdf = new StringBuilder("%PDF-1.4\n");
        List<Integer> offsets = new ArrayList<>();
        for (int k = 0; k < objects.size(); k++) {
            offsets.add(pdf.length());
            pdf.append(k + 1).append(" 0 obj\n").append(objects.get(k)).append("\nendobj\n");
        }
        int xref = pdf.length();
        pdf.append("xref\n0 ").append(objects.size() + 1).append("\n0000000000 65535 f \n");
        for (int offset : offsets) {
            pdf.append(String.format("%010d 00000 n ", offset)).append('\n');
        }
        pdf.append("trailer\n<< /Size ").append(objects.size() + 1).append(" /Root 1 0 R >>\nstartxref\n")
                .append(xref).append("\n%%EOF\n");
        return pdf.toString().getBytes(StandardCharsets.ISO_8859_1);
    }

    private static int redPixels(BufferedImage image) {
        int count = 0;
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                if (isRed(image.getRGB(x, y))) {
                    count++;
                }
            }
        }
        return count;
    }

    private static boolean isRed(int argb) {
        return ((argb >> 16) & 0xFF) > 200 && ((argb >> 8) & 0xFF) < 60 && (argb & 0xFF) < 60;
    }
}
