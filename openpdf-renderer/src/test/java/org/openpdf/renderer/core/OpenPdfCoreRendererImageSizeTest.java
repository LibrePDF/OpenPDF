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

import java.awt.image.BufferedImage;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Images that declare a size that their data does not hold.
 */
class OpenPdfCoreRendererImageSizeTest {

    /**
     * 25,000 x 25,000 pixels with four components: the number of bytes does not fit in an {@code int}.
     */
    private static final int HUGE = 25_000;

    @Test
    void comparesTheSizeOfARasterWithoutOverflow() {
        byte[] data = new byte[12];

        assertThat(OpenPdfCorePageRenderer.holdsRaster(data, 2, 2, 3)).isTrue();
        assertThat(OpenPdfCorePageRenderer.holdsRaster(data, 2, 2, 4)).isFalse();
        // 25,000 * 25,000 * 4 is 2,500,000,000, which is -1,794,967,296 as an int
        assertThat(OpenPdfCorePageRenderer.holdsRaster(data, HUGE, HUGE, 4)).isFalse();
        // 65,536 * 65,536 is 0 as an int
        assertThat(OpenPdfCorePageRenderer.holdsRaster(data, 65_536, 65_536, 1)).isFalse();
        assertThat(OpenPdfCorePageRenderer.holdsRaster(data, Integer.MAX_VALUE, Integer.MAX_VALUE, 4)).isFalse();
        assertThat(OpenPdfCorePageRenderer.holdsRaster(data, 0, 2, 3)).isFalse();
        assertThat(OpenPdfCorePageRenderer.holdsRaster(data, 2, -2, 3)).isFalse();
        assertThat(OpenPdfCorePageRenderer.holdsRaster(data, 2, 2, 0)).isFalse();
    }

    @Test
    void skipsCmykImageWithoutTheDataForItsSize() throws Exception {
        assertThat(isBlank(render(image("/ColorSpace /DeviceCMYK", HUGE, "x")))).isTrue();
    }

    @Test
    void skipsRgbImageWithoutTheDataForItsSize() throws Exception {
        assertThat(isBlank(render(image("/ColorSpace /DeviceRGB", 30_000, "x")))).isTrue();
    }

    @Test
    void skipsIndexedImageWithoutTheDataForItsSize() throws Exception {
        assertThat(isBlank(render(image("/ColorSpace [/Indexed /DeviceRGB 0 <FF0000>]", 65_536, "x")))).isTrue();
    }

    @Test
    void skipsInlineImageWithoutTheDataForItsSize() throws Exception {
        String content = "q 100 0 0 100 0 0 cm BI /W " + HUGE + " /H " + HUGE + " /CS /CMYK /BPC 8 ID x EI Q";

        assertThat(isBlank(render(page(content, new ArrayList<>())))).isTrue();
    }

    @Test
    void drawsImageWithASoftMaskThatDeclaresMorePixelsThanItHas() throws Exception {
        // a red image of 2 x 2 pixels; its soft mask says 65,536 x 65,537 pixels, which is 65,536 as an int
        List<String> objects = new ArrayList<>();
        objects.add(stream("/Type /XObject /Subtype /Image /Width 2 /Height 2 /BitsPerComponent 8 "
                + "/ColorSpace /DeviceRGB /SMask 5 0 R", "ÿ\u0000\u0000".repeat(4)));
        objects.add(stream("/Type /XObject /Subtype /Image /Width 65536 /Height 65537 /BitsPerComponent 8 "
                + "/ColorSpace /DeviceGray", "ÿ".repeat(65_536)));

        BufferedImage image = render(page("q 100 0 0 100 0 0 cm /Im0 Do Q", objects));

        assertThat(isRed(image.getRGB(50, 150))).isTrue();
    }

    @Test
    void drawsImageWithTheDataForItsSize() throws Exception {
        BufferedImage image = render(image("/ColorSpace /DeviceRGB", 2, "ÿ\u0000\u0000".repeat(4)));

        assertThat(isRed(image.getRGB(50, 150))).isTrue();
    }

    /**
     * A page that draws a square image of the given size in the lower left corner.
     */
    private static List<String> image(String colorSpace, int size, String data) {
        List<String> objects = new ArrayList<>();
        objects.add(stream("/Type /XObject /Subtype /Image /Width " + size + " /Height " + size
                + " /BitsPerComponent 8 " + colorSpace, data));
        return page("q 100 0 0 100 0 0 cm /Im0 Do Q", objects);
    }

    /**
     * A document with one page of 200 x 200 points. The objects that are passed in get the numbers from 4, and
     * the first one is the XObject {@code /Im0} of the page.
     */
    private static List<String> page(String content, List<String> xobjects) {
        List<String> objects = new ArrayList<>();
        objects.add("<< /Type /Catalog /Pages 2 0 R >>");
        objects.add("<< /Type /Pages /Kids [3 0 R] /Count 1 >>");
        objects.add("<< /Type /Page /Parent 2 0 R /MediaBox [0 0 200 200] /Resources << /XObject << /Im0 4 0 R >> >>"
                + " /Contents " + (4 + Math.max(xobjects.size(), 1)) + " 0 R >>");
        objects.addAll(xobjects.isEmpty() ? List.of("<< >>") : xobjects);
        objects.add(stream("", content));
        return objects;
    }

    private static String stream(String entries, String content) {
        return "<< " + entries + " /Length " + content.length() + " >>\nstream\n" + content + "\nendstream";
    }

    private static BufferedImage render(List<String> objects) throws Exception {
        try (OpenPdfCoreRenderer renderer = new OpenPdfCoreRenderer(pdf(objects))) {
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
            pdf.append(String.format("%010d 00000 n \n", offset));
        }
        pdf.append("trailer\n<< /Size ").append(objects.size() + 1).append(" /Root 1 0 R >>\nstartxref\n")
                .append(xref).append("\n%%EOF\n");
        return pdf.toString().getBytes(StandardCharsets.ISO_8859_1);
    }

    private static boolean isBlank(BufferedImage image) {
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                if ((image.getRGB(x, y) & 0xFFFFFF) != 0xFFFFFF) {
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean isRed(int argb) {
        return ((argb >> 16) & 0xFF) > 200 && ((argb >> 8) & 0xFF) < 60 && (argb & 0xFF) < 60;
    }
}
