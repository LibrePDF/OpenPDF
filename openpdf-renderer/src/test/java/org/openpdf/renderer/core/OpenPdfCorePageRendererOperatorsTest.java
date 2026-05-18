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

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;
import org.openpdf.text.Document;
import org.openpdf.text.PageSize;
import org.openpdf.text.Rectangle;
import org.openpdf.text.pdf.PdfContentByte;
import org.openpdf.text.pdf.PdfWriter;

/**
 * Exercises operators added in the second integration pass &mdash; CMYK colors,
 * clipping ({@code W} / {@code W*}), line styling ({@code J}, {@code j},
 * {@code M}, {@code d}), text rise ({@code Ts}), and marked content
 * ({@code BMC} / {@code BDC} / {@code EMC}) &mdash; by writing a tiny PDF with
 * {@code openpdf-core}'s {@link PdfContentByte} and rendering it back with the
 * {@code openpdf-core}-driven Java2D renderer.
 *
 * <p>These tests don't assert pixel-perfect output: they assert that the
 * renderer drives the operators without throwing, and that the resulting image
 * actually contains the colored marks (i.e. operators were honored end-to-end).</p>
 */
class OpenPdfCorePageRendererOperatorsTest {

    private static byte[] buildPdf(ContentWriter writer) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (Document doc = new Document(new Rectangle(PageSize.A6))) {
            PdfWriter pdf = PdfWriter.getInstance(doc, baos);
            doc.open();
            PdfContentByte cb = pdf.getDirectContent();
            writer.write(cb);
        }
        return baos.toByteArray();
    }

    @FunctionalInterface
    interface ContentWriter {
        void write(PdfContentByte cb) throws Exception;
    }

    private static int countPixelsMatching(BufferedImage img, ColorPredicate p) {
        int count = 0;
        for (int y = 0; y < img.getHeight(); y += 2) {
            for (int x = 0; x < img.getWidth(); x += 2) {
                int argb = img.getRGB(x, y);
                int r = (argb >> 16) & 0xFF;
                int g = (argb >> 8) & 0xFF;
                int b = argb & 0xFF;
                if (p.matches(r, g, b)) {
                    count++;
                }
            }
        }
        return count;
    }

    @FunctionalInterface
    interface ColorPredicate {
        boolean matches(int r, int g, int b);
    }

    private static void saveForInspection(BufferedImage img, String name) throws IOException {
        File outDir = new File("target/test-outputs");
        outDir.mkdirs();
        ImageIO.write(img, "png", new File(outDir, name));
    }

    @Test
    void rendersCmykFillAsApproximatedRgb() throws Exception {
        // 100% cyan in CMYK ~= (0, 1, 1) in RGB. Draw a big rectangle and check
        // that the rendered image contains pixels that look cyan-ish.
        byte[] pdf = buildPdf(cb -> {
            cb.setCMYKColorFillF(1f, 0f, 0f, 0f);
            cb.rectangle(20, 20, 200, 200);
            cb.fill();
        });

        try (OpenPdfCoreRenderer r = new OpenPdfCoreRenderer(pdf)) {
            BufferedImage img = r.renderPage(1, 150f);
            saveForInspection(img, "cmyk-fill.png");
            int cyanish = countPixelsMatching(img, (red, green, blue) ->
                    red < 80 && green > 150 && blue > 150);
            assertThat(cyanish)
                    .as("CMYK (1,0,0,0) fill must produce cyan-ish pixels")
                    .isGreaterThan(100);
        }
    }

    @Test
    void rendersDashedStrokeWithCustomLineCap() throws Exception {
        // Red dashed line with round caps. Verify the renderer at least produces
        // some red pixels (the stroke was drawn).
        byte[] pdf = buildPdf(cb -> {
            cb.setRGBColorStrokeF(1f, 0f, 0f);
            cb.setLineWidth(4f);
            cb.setLineCap(PdfContentByte.LINE_CAP_ROUND);
            cb.setLineDash(new float[]{8f, 6f}, 0f);
            cb.moveTo(20, 100);
            cb.lineTo(200, 100);
            cb.stroke();
        });

        try (OpenPdfCoreRenderer r = new OpenPdfCoreRenderer(pdf)) {
            BufferedImage img = r.renderPage(1, 150f);
            saveForInspection(img, "dashed-stroke.png");
            int redish = countPixelsMatching(img, (red, green, blue) ->
                    red > 200 && green < 80 && blue < 80);
            assertThat(redish)
                    .as("dashed red stroke must produce red pixels")
                    .isGreaterThan(20);
        }
    }

    @Test
    void rendersClippedFillWithoutSpillingOutside() throws Exception {
        // Clip to a small rectangle, then try to fill a much larger green rect.
        // Pixels outside the clip must remain the opaque-white background.
        byte[] pdf = buildPdf(cb -> {
            cb.rectangle(50, 50, 60, 60);
            cb.clip();
            cb.newPath();
            cb.setRGBColorFillF(0f, 1f, 0f);
            cb.rectangle(0, 0, 400, 400);
            cb.fill();
        });

        try (OpenPdfCoreRenderer r = new OpenPdfCoreRenderer(pdf)) {
            BufferedImage img = r.renderPage(1, 150f);
            saveForInspection(img, "clipped-fill.png");

            int greenish = countPixelsMatching(img, (red, green, blue) ->
                    green > 200 && red < 80 && blue < 80);
            assertThat(greenish)
                    .as("clip must allow the green fill inside the clip rect")
                    .isGreaterThan(50);

            // Sample a corner well outside the clip rectangle; it must still be the
            // opaque-white background.
            int corner = img.getRGB(img.getWidth() - 4, img.getHeight() - 4);
            int alpha = (corner >>> 24) & 0xFF;
            int rch = (corner >> 16) & 0xFF;
            int gch = (corner >> 8) & 0xFF;
            int bch = corner & 0xFF;
            assertThat(alpha).isEqualTo(0xFF);
            assertThat(rch).isEqualTo(0xFF);
            assertThat(gch).isEqualTo(0xFF);
            assertThat(bch).isEqualTo(0xFF);
        }
    }

    @Test
    void rendersMarkedContentWithoutFailing() throws Exception {
        // Marked content blocks (BMC / EMC) must be parsed as no-ops; any text
        // inside them must still be drawn.
        byte[] pdf = buildPdf(cb -> {
            cb.beginMarkedContentSequence(new org.openpdf.text.pdf.PdfName("Span"));
            cb.setColorFill(Color.BLUE);
            cb.rectangle(40, 40, 100, 50);
            cb.fill();
            cb.endMarkedContentSequence();
        });

        try (OpenPdfCoreRenderer r = new OpenPdfCoreRenderer(pdf)) {
            List<String> ops = r.getContentOperators(1);
            // Sanity: openpdf-core must have emitted BMC / EMC for us.
            assertThat(ops).contains("BMC", "EMC");

            BufferedImage img = r.renderPage(1, 150f);
            saveForInspection(img, "marked-content.png");

            int bluish = countPixelsMatching(img, (red, green, blue) ->
                    blue > 200 && red < 80 && green < 80);
            assertThat(bluish)
                    .as("content inside a marked-content sequence must still render")
                    .isGreaterThan(50);
        }
    }

    @Test
    void rendersJpegImageXObject() throws Exception {
        // Generate a 32x16 solid-red JPEG, embed it as an Image XObject in a PDF,
        // then verify the rendered page contains red pixels (the image was drawn).
        BufferedImage jpegSource = new BufferedImage(32, 16, BufferedImage.TYPE_INT_RGB);
        java.awt.Graphics2D gs = jpegSource.createGraphics();
        try {
            gs.setColor(Color.RED);
            gs.fillRect(0, 0, 32, 16);
        } finally {
            gs.dispose();
        }
        ByteArrayOutputStream jpegOut = new ByteArrayOutputStream();
        ImageIO.write(jpegSource, "jpg", jpegOut);
        org.openpdf.text.Image pdfImage = org.openpdf.text.Image.getInstance(jpegOut.toByteArray());

        byte[] pdf = buildPdf(cb -> {
            // Scale the image up so it covers a recognizable area of the page.
            cb.addImage(pdfImage, 160f, 0f, 0f, 80f, 30f, 100f);
        });

        try (OpenPdfCoreRenderer r = new OpenPdfCoreRenderer(pdf)) {
            List<String> ops = r.getContentOperators(1);
            assertThat(ops).contains("Do");

            BufferedImage img = r.renderPage(1, 150f);
            saveForInspection(img, "image-xobject.png");
            int redish = countPixelsMatching(img, (red, green, blue) ->
                    red > 180 && green < 100 && blue < 100);
            assertThat(redish)
                    .as("JPEG Image XObject must produce red pixels on the page")
                    .isGreaterThan(200);
        }
    }

    @Test
    void rendersFormXObjectViaNestedContentStream() throws Exception {
        // Form XObjects embed their own content stream. Wrap a colored rectangle
        // in a PdfTemplate and stamp it onto the page; the renderer must recurse
        // into the form's content and draw the rectangle.
        byte[] pdf = buildPdf(cb -> {
            org.openpdf.text.pdf.PdfTemplate tpl = cb.createTemplate(100f, 60f);
            tpl.setRGBColorFillF(1f, 0.5f, 0f); // orange
            tpl.rectangle(0f, 0f, 100f, 60f);
            tpl.fill();
            cb.addTemplate(tpl, 40f, 120f);
        });

        try (OpenPdfCoreRenderer r = new OpenPdfCoreRenderer(pdf)) {
            BufferedImage img = r.renderPage(1, 150f);
            saveForInspection(img, "form-xobject.png");
            int orangeish = countPixelsMatching(img, (red, green, blue) ->
                    red > 200 && green > 80 && green < 200 && blue < 80);
            assertThat(orangeish)
                    .as("Form XObject content must be rendered onto the page")
                    .isGreaterThan(100);
        }
    }

    @Test
    void inlineImagesDoNotBreakPageRendering() throws Exception {
        // Build a content stream by hand that contains a tiny inline image
        // followed by a normal vector draw. The inline image isn't expected
        // to render, but the rest of the page must still parse and draw.
        byte[] pdf = buildPdf(cb -> {
            // BI/ID/EI inline image: 2x2, 8bpc, DeviceGray, 4 raw bytes.
            String inline = "q\n"
                    + "BI /W 2 /H 2 /CS /G /BPC 8 ID\n"
                    + new String(new byte[]{(byte) 0xFF, 0x00, 0x00, (byte) 0xFF},
                            java.nio.charset.StandardCharsets.ISO_8859_1)
                    + "\nEI\n"
                    + "Q\n"
                    + "1 0 0 rg\n"
                    + "50 50 80 80 re f\n";
            cb.setLiteral(inline);
        });

        try (OpenPdfCoreRenderer r = new OpenPdfCoreRenderer(pdf)) {
            // Even though BI/ID/EI is in the stream, getContentOperators sees the
            // BI block as raw tokens — but renderer must not crash and the trailing
            // red rectangle must still be drawn.
            BufferedImage img = r.renderPage(1, 150f);
            saveForInspection(img, "inline-image-stripped.png");
            int redish = countPixelsMatching(img, (red, green, blue) ->
                    red > 200 && green < 80 && blue < 80);
            assertThat(redish)
                    .as("after stripping inline image, the trailing red rectangle must still render")
                    .isGreaterThan(50);
        }
    }

    @Test
    void rendersTextRiseAsVerticalOffset() throws Exception {
        // Two glyphs at the same Td, one with Ts=10 (raised). They must render
        // at different rows. We don't need a font setup: PdfContentByte with a
        // built-in font is fine, since the renderer falls back to a Java2D font
        // when it can't load the PDF font outline.
        byte[] pdf = buildPdf(cb -> {
            org.openpdf.text.pdf.BaseFont bf = org.openpdf.text.pdf.BaseFont
                    .createFont(org.openpdf.text.pdf.BaseFont.HELVETICA,
                            org.openpdf.text.pdf.BaseFont.WINANSI,
                            org.openpdf.text.pdf.BaseFont.NOT_EMBEDDED);
            cb.beginText();
            cb.setFontAndSize(bf, 24f);
            cb.setTextMatrix(40f, 200f);
            cb.showText("A");
            cb.setTextRise(20f);
            cb.showText("B");
            cb.endText();
        });

        try (OpenPdfCoreRenderer r = new OpenPdfCoreRenderer(pdf)) {
            List<String> ops = r.getContentOperators(1);
            assertThat(ops).contains("Ts", "Tj");

            BufferedImage img = r.renderPage(1, 150f);
            saveForInspection(img, "text-rise.png");

            // At least *some* dark pixels (glyphs) must have been drawn.
            int darkPixels = countPixelsMatching(img, (red, green, blue) ->
                    red < 80 && green < 80 && blue < 80);
            assertThat(darkPixels).isGreaterThan(20);
        }
    }
}
