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
    void rendersIndexedColorImageXObject() throws Exception {
        // Build an IndexColorModel-backed BufferedImage and embed it via
        // Image.getInstance(BufferedImage). openpdf-core preserves the palette and
        // writes the image as `[/Indexed /DeviceRGB hival <palette>]`, exercising
        // the Indexed-color-space decode path in OpenPdfCorePageRenderer.
        java.awt.image.IndexColorModel icm = new java.awt.image.IndexColorModel(
                8, 2,
                new byte[]{(byte) 0xFF, 0x00},          // R
                new byte[]{0x00, (byte) 0xFF},          // G
                new byte[]{(byte) 0xFF, (byte) 0xFF}); // B  -> palette: index 0 = magenta, 1 = cyan
        BufferedImage indexed = new BufferedImage(32, 32, BufferedImage.TYPE_BYTE_INDEXED, icm);
        java.awt.image.WritableRaster raster = indexed.getRaster();
        for (int y = 0; y < 32; y++) {
            for (int x = 0; x < 32; x++) {
                // Top half = magenta (index 0), bottom half = cyan (index 1).
                raster.setSample(x, y, 0, y < 16 ? 0 : 1);
            }
        }
        org.openpdf.text.Image pdfImage = org.openpdf.text.Image.getInstance(indexed, null);

        byte[] pdf = buildPdf(cb -> {
            cb.addImage(pdfImage, 160f, 0f, 0f, 160f, 30f, 80f);
        });

        try (OpenPdfCoreRenderer r = new OpenPdfCoreRenderer(pdf)) {
            BufferedImage img = r.renderPage(1, 150f);
            saveForInspection(img, "indexed-image.png");

            // Top half of the image region should contain magenta-ish pixels;
            // bottom half cyan-ish. We just need at least some of each to prove
            // the palette was decoded and the indices were looked up correctly.
            int magenta = countPixelsMatching(img, (red, green, blue) ->
                    red > 200 && green < 80 && blue > 200);
            int cyan = countPixelsMatching(img, (red, green, blue) ->
                    red < 80 && green > 200 && blue > 200);
            assertThat(magenta)
                    .as("indexed image must produce magenta pixels for palette index 0")
                    .isGreaterThan(100);
            assertThat(cyan)
                    .as("indexed image must produce cyan pixels for palette index 1")
                    .isGreaterThan(100);
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
    void inlineImageRendersAtCtmLocation() throws Exception {
        // Build a content stream by hand: a 2x2 DeviceGray inline image whose pixels are
        // [black, white; white, black], scaled and positioned by a cm before BI so that
        // it covers a recognizable area of the page. The renderer must promote the inline
        // image to a synthetic XObject and actually draw it under the CTM.
        byte[] pdf = buildPdf(cb -> {
            String inline = "q\n"
                    + "120 0 0 120 60 60 cm\n"
                    + "BI /W 2 /H 2 /CS /G /BPC 8 ID\n"
                    + new String(new byte[]{0x00, (byte) 0xFF, (byte) 0xFF, 0x00},
                            java.nio.charset.StandardCharsets.ISO_8859_1)
                    + "\nEI\n"
                    + "Q\n"
                    // Trailing red rect proves the rest of the page also still renders.
                    + "1 0 0 rg\n"
                    + "200 200 30 30 re f\n";
            cb.setLiteral(inline);
        });

        try (OpenPdfCoreRenderer r = new OpenPdfCoreRenderer(pdf)) {
            BufferedImage img = r.renderPage(1, 150f);
            saveForInspection(img, "inline-image.png");

            int dark = countPixelsMatching(img, (red, green, blue) ->
                    red < 60 && green < 60 && blue < 60);
            assertThat(dark)
                    .as("inline image must paint its black checker squares onto the page")
                    .isGreaterThan(100);

            int redish = countPixelsMatching(img, (red, green, blue) ->
                    red > 200 && green < 80 && blue < 80);
            assertThat(redish)
                    .as("content after the inline image must still render")
                    .isGreaterThan(10);
        }
    }

    @Test
    void jpegInlineImageDecodes() throws Exception {
        // Same as the previous test but the inline image is a JPEG written via the
        // PdfContentByte.addImage(image, true) helper, which produces a properly
        // framed BI/ID/EI block (binary-safe length tracking, abbreviated filter name).
        BufferedImage src = new BufferedImage(16, 16, BufferedImage.TYPE_INT_RGB);
        java.awt.Graphics2D gs = src.createGraphics();
        try {
            gs.setColor(new Color(0, 200, 0));
            gs.fillRect(0, 0, 16, 16);
        } finally {
            gs.dispose();
        }
        ByteArrayOutputStream jpegOut = new ByteArrayOutputStream();
        ImageIO.write(src, "jpg", jpegOut);
        org.openpdf.text.Image pdfImage = org.openpdf.text.Image.getInstance(jpegOut.toByteArray());

        byte[] pdf = buildPdf(cb -> {
            cb.addImage(pdfImage, 120f, 0f, 0f, 120f, 60f, 60f, /* inline */ true);
        });

        try (OpenPdfCoreRenderer r = new OpenPdfCoreRenderer(pdf)) {
            BufferedImage img = r.renderPage(1, 150f);
            saveForInspection(img, "inline-image-jpeg.png");

            int greenish = countPixelsMatching(img, (red, green, blue) ->
                    green > 150 && red < 100 && blue < 100);
            assertThat(greenish)
                    .as("JPEG inline image must decode and paint its green region")
                    .isGreaterThan(100);
        }
    }

    @Test
    void rendersTextUsingEmbeddedTrueTypeFont() throws Exception {
        // Build a PDF that embeds a real TrueType font and writes a glyph that's
        // not the .notdef of any built-in AWT fallback. The renderer must extract
        // the FontFile2 byte stream and use Font.createFont under the hood; we can
        // detect this by checking that the rendered text region contains dark pixels
        // (i.e. the glyph was drawn) while the renderer was forced to load an
        // embedded program (no name-based AWT family would match this font).
        java.net.URL ttf = OpenPdfCorePageRendererOperatorsTest.class.getClassLoader()
                .getResource("font-fallback/LiberationSans-Regular.ttf");
        assertThat(ttf).as("LiberationSans-Regular.ttf must be on the classpath via openpdf-core").isNotNull();

        byte[] pdf = buildPdf(cb -> {
            org.openpdf.text.pdf.BaseFont bf = org.openpdf.text.pdf.BaseFont
                    .createFont(ttf.toString(), org.openpdf.text.pdf.BaseFont.WINANSI,
                            org.openpdf.text.pdf.BaseFont.EMBEDDED);
            cb.beginText();
            cb.setFontAndSize(bf, 32f);
            cb.setTextMatrix(40f, 200f);
            cb.showText("Hello PDF");
            cb.endText();
        });

        try (OpenPdfCoreRenderer r = new OpenPdfCoreRenderer(pdf)) {
            BufferedImage img = r.renderPage(1, 150f);
            saveForInspection(img, "embedded-truetype.png");

            int darkPixels = countPixelsMatching(img, (red, green, blue) ->
                    red < 80 && green < 80 && blue < 80);
            assertThat(darkPixels)
                    .as("text drawn with an embedded TrueType font must produce glyph pixels")
                    .isGreaterThan(200);
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

    /**
     * Renders a full {@code PdfPTable} (background fills, colored borders, header row,
     * cell text) and checks that the renderer produces all three of: cell-background
     * fills, border strokes in their declared color, and cell text. This is a
     * regression guard for table rendering as a whole — many small operators
     * (re/f/S/m/l/Tj plus q/Q/cm/w nesting) have to cooperate to get a usable table.
     */
    @Test
    void rendersPdfPTableWithBordersFillsAndText() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (Document doc = new Document(new Rectangle(PageSize.A6))) {
            PdfWriter.getInstance(doc, baos);
            doc.open();
            org.openpdf.text.pdf.PdfPTable table = new org.openpdf.text.pdf.PdfPTable(3);
            table.setTotalWidth(240f);
            table.setLockedWidth(true);

            // Header row: blue background, white text, thicker red border.
            for (String header : new String[]{"Col A", "Col B", "Col C"}) {
                org.openpdf.text.pdf.PdfPCell hc = new org.openpdf.text.pdf.PdfPCell(
                        new org.openpdf.text.Phrase(header,
                                new org.openpdf.text.Font(org.openpdf.text.Font.HELVETICA, 10f,
                                        org.openpdf.text.Font.BOLD, Color.WHITE)));
                hc.setBackgroundColor(new Color(0, 0, 200));
                hc.setBorderColor(Color.RED);
                hc.setBorderWidth(2f);
                hc.setPadding(4f);
                table.addCell(hc);
            }
            // Body rows: default thin black borders, body text.
            for (int row = 0; row < 2; row++) {
                for (int col = 0; col < 3; col++) {
                    org.openpdf.text.pdf.PdfPCell bc = new org.openpdf.text.pdf.PdfPCell(
                            new org.openpdf.text.Phrase("r" + row + "c" + col));
                    bc.setPadding(4f);
                    table.addCell(bc);
                }
            }
            doc.add(table);
        }

        try (OpenPdfCoreRenderer r = new OpenPdfCoreRenderer(baos.toByteArray())) {
            BufferedImage img = r.renderPage(1, 200f);
            saveForInspection(img, "pdfptable.png");

            int blueHeaderFill = countPixelsMatching(img, (red, green, blue) ->
                    blue > 150 && red < 80 && green < 80);
            assertThat(blueHeaderFill)
                    .as("header-row blue background fill must be visible")
                    .isGreaterThan(200);

            int redBorder = countPixelsMatching(img, (red, green, blue) ->
                    red > 180 && green < 80 && blue < 80);
            assertThat(redBorder)
                    .as("header-row red 2pt border strokes must be visible")
                    .isGreaterThan(20);

            // Body cells sit just below the header. Look for dark glyph pixels in the
            // band of the image that contains the two body rows (header row pixels
            // are white-on-blue, so the dark pixels there are body text + borders).
            // We deliberately skip the header band to verify body-row text rendered.
            int bandTop = (int) (img.getHeight() * 0.10);
            int bandBottom = (int) (img.getHeight() * 0.30);
            int bodyDark = 0;
            for (int y = bandTop; y < bandBottom; y += 2) {
                for (int x = 0; x < img.getWidth(); x += 2) {
                    int argb = img.getRGB(x, y);
                    int rch = (argb >> 16) & 0xFF;
                    int gch = (argb >> 8) & 0xFF;
                    int bch = argb & 0xFF;
                    if (rch < 80 && gch < 80 && bch < 80) {
                        bodyDark++;
                    }
                }
            }
            assertThat(bodyDark)
                    .as("body-row text must produce dark glyph pixels in the body band")
                    .isGreaterThan(20);
        }
    }

    /**
     * PDF §8.4.3.2: a stroke width of 0 means "the thinnest line the device can
     * render", i.e. one device pixel. Naively passing the user-space width to
     * {@link java.awt.BasicStroke} would collapse to nothing once the page CTM
     * scales it. This test draws a {@code 0 w} line and verifies the stroke is
     * actually visible — which is the common case for PDFs that draw table grids
     * with hairlines.
     */
    @Test
    void rendersZeroWidthStrokeAsDevicePixelHairline() throws Exception {
        byte[] pdf = buildPdf(cb -> {
            cb.setRGBColorStrokeF(0f, 0f, 0f);
            cb.setLineWidth(0f); // hairline
            for (int i = 0; i < 5; i++) {
                cb.moveTo(20f, 40f + 15f * i);
                cb.lineTo(220f, 40f + 15f * i);
            }
            cb.stroke();
        });

        try (OpenPdfCoreRenderer r = new OpenPdfCoreRenderer(pdf)) {
            BufferedImage img = r.renderPage(1, 200f);
            saveForInspection(img, "hairline-stroke.png");
            int dark = countPixelsMatching(img, (red, green, blue) ->
                    red < 100 && green < 100 && blue < 100);
            assertThat(dark)
                    .as("PDF zero-width strokes must render as visible hairlines, not vanish")
                    .isGreaterThan(20);
        }
    }

    /**
     * Exercises the harder slice of "text inside a table cell": multi-line wrapped
     * text in a wide cell, a {@code Phrase} composed of multiple {@link
     * org.openpdf.text.Chunk}s with different fonts and colors, varied horizontal
     * alignments (left / center / right), and a larger header font. The renderer
     * has to handle the resulting {@code Tj} stream &mdash; one short showText per
     * line, with {@code Td}/{@code Tm} moves between lines &mdash; while sitting
     * under the {@code q ... re W n} clipping that {@code PdfPTable} wraps every
     * cell in.
     */
    @Test
    void rendersTableWithRichCellText() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (Document doc = new Document(new Rectangle(PageSize.A5))) {
            PdfWriter.getInstance(doc, baos);
            doc.open();

            org.openpdf.text.pdf.PdfPTable table = new org.openpdf.text.pdf.PdfPTable(3);
            table.setTotalWidth(360f);
            table.setLockedWidth(true);
            table.setWidths(new float[]{1f, 2f, 1f});

            // Larger, bold header row.
            org.openpdf.text.Font headerFont = new org.openpdf.text.Font(
                    org.openpdf.text.Font.HELVETICA, 14f, org.openpdf.text.Font.BOLD, Color.WHITE);
            for (String header : new String[]{"#", "Description", "Qty"}) {
                org.openpdf.text.pdf.PdfPCell hc = new org.openpdf.text.pdf.PdfPCell(
                        new org.openpdf.text.Phrase(header, headerFont));
                hc.setBackgroundColor(new Color(40, 40, 120));
                hc.setHorizontalAlignment(org.openpdf.text.Element.ALIGN_CENTER);
                hc.setPadding(6f);
                table.addCell(hc);
            }

            // Row 1: left-aligned number, multi-line wrapped description, right-aligned qty.
            table.addCell(numberCell("1", org.openpdf.text.Element.ALIGN_LEFT));
            table.addCell(new org.openpdf.text.pdf.PdfPCell(new org.openpdf.text.Phrase(
                    "First line of a long description that should wrap onto a second "
                            + "and probably a third line inside its table cell.",
                    new org.openpdf.text.Font(org.openpdf.text.Font.HELVETICA, 10f))));
            table.addCell(numberCell("12", org.openpdf.text.Element.ALIGN_RIGHT));

            // Row 2: a cell with mixed Chunks (regular, bold, italic, colored).
            table.addCell(numberCell("2", org.openpdf.text.Element.ALIGN_LEFT));
            org.openpdf.text.Phrase mixed = new org.openpdf.text.Phrase();
            mixed.add(new org.openpdf.text.Chunk("Regular ",
                    new org.openpdf.text.Font(org.openpdf.text.Font.HELVETICA, 11f)));
            mixed.add(new org.openpdf.text.Chunk("bold ",
                    new org.openpdf.text.Font(org.openpdf.text.Font.HELVETICA, 11f,
                            org.openpdf.text.Font.BOLD)));
            mixed.add(new org.openpdf.text.Chunk("italic ",
                    new org.openpdf.text.Font(org.openpdf.text.Font.HELVETICA, 11f,
                            org.openpdf.text.Font.ITALIC)));
            mixed.add(new org.openpdf.text.Chunk("RED",
                    new org.openpdf.text.Font(org.openpdf.text.Font.HELVETICA, 11f,
                            org.openpdf.text.Font.BOLD, Color.RED)));
            table.addCell(new org.openpdf.text.pdf.PdfPCell(mixed));
            table.addCell(numberCell("345", org.openpdf.text.Element.ALIGN_RIGHT));

            // Row 3: a centered, vertically centered cell spanning two columns.
            org.openpdf.text.pdf.PdfPCell spanCell = new org.openpdf.text.pdf.PdfPCell(
                    new org.openpdf.text.Phrase("Centered span",
                            new org.openpdf.text.Font(org.openpdf.text.Font.HELVETICA, 12f,
                                    org.openpdf.text.Font.NORMAL, Color.BLUE)));
            spanCell.setColspan(2);
            spanCell.setHorizontalAlignment(org.openpdf.text.Element.ALIGN_CENTER);
            spanCell.setVerticalAlignment(org.openpdf.text.Element.ALIGN_MIDDLE);
            spanCell.setFixedHeight(40f);
            table.addCell(spanCell);
            table.addCell(numberCell("6789", org.openpdf.text.Element.ALIGN_RIGHT));

            doc.add(table);
        }

        try (OpenPdfCoreRenderer r = new OpenPdfCoreRenderer(baos.toByteArray())) {
            BufferedImage img = r.renderPage(1, 200f);
            saveForInspection(img, "pdfptable-rich-text.png");

            // (1) White header glyphs land on the dark-blue header background.
            int whiteOnBlueHeader = countWhiteOnBlueHeaderPixels(img);
            assertThat(whiteOnBlueHeader)
                    .as("header row must show white glyphs on a dark-blue background")
                    .isGreaterThan(20);

            // (2) Red glyph chunk renders inside its row.
            int red = countPixelsMatching(img, (rch, gch, bch) ->
                    rch > 180 && gch < 80 && bch < 80);
            assertThat(red)
                    .as("Chunk with explicit red font color must produce red text pixels")
                    .isGreaterThan(20);

            // (3) Blue span-cell text renders.
            int blue = countPixelsMatching(img, (rch, gch, bch) ->
                    bch > 180 && rch < 80 && gch < 80);
            assertThat(blue)
                    .as("colspan cell with blue Phrase font must produce blue text pixels")
                    .isGreaterThan(20);

            // (4) The long wrapped description produces several distinct lines of glyphs.
            int textRowsWithGlyphs = countDarkGlyphTextRows(img);
            assertThat(textRowsWithGlyphs)
                    .as("wrapped multi-line cell text must produce multiple rows of glyphs")
                    .isGreaterThanOrEqualTo(3);
        }
    }

    private static org.openpdf.text.pdf.PdfPCell numberCell(String text, int alignment) {
        org.openpdf.text.pdf.PdfPCell c = new org.openpdf.text.pdf.PdfPCell(
                new org.openpdf.text.Phrase(text,
                        new org.openpdf.text.Font(org.openpdf.text.Font.HELVETICA, 11f)));
        c.setHorizontalAlignment(alignment);
        c.setPadding(6f);
        return c;
    }

    private static int countWhiteOnBlueHeaderPixels(BufferedImage img) {
        int matches = 0;
        for (int y = 0; y < img.getHeight() / 4; y++) {
            for (int x = 0; x < img.getWidth(); x++) {
                int argb = img.getRGB(x, y);
                int r = (argb >> 16) & 0xFF;
                int g = (argb >> 8) & 0xFF;
                int b = argb & 0xFF;
                if (r > 220 && g > 220 && b > 220) {
                    int neighborX = Math.min(img.getWidth() - 1, x + 4);
                    int neighbor = img.getRGB(neighborX, y);
                    int nr = (neighbor >> 16) & 0xFF;
                    int ng = (neighbor >> 8) & 0xFF;
                    int nb = neighbor & 0xFF;
                    if (nb > 60 && nr < 100 && ng < 100) {
                        matches++;
                    }
                }
            }
        }
        return matches;
    }

    /**
     * Counts how many horizontal scanlines contain a non-trivial number of dark
     * glyph-like pixels. A cell with three lines of text should produce ~three
     * such bands; a cell with one line should produce ~one.
     */
    private static int countDarkGlyphTextRows(BufferedImage img) {
        int rows = 0;
        boolean inRow = false;
        for (int y = 0; y < img.getHeight(); y++) {
            int dark = 0;
            for (int x = 0; x < img.getWidth(); x++) {
                int argb = img.getRGB(x, y);
                int r = (argb >> 16) & 0xFF;
                int g = (argb >> 8) & 0xFF;
                int b = argb & 0xFF;
                if (r < 80 && g < 80 && b < 80) {
                    dark++;
                }
            }
            // 6 = a few glyph strokes; below that is just border-line bleed.
            boolean isGlyphRow = dark > 6 && dark < img.getWidth() / 2;
            if (isGlyphRow && !inRow) {
                rows++;
                inRow = true;
            } else if (!isGlyphRow) {
                inRow = false;
            }
        }
        return rows;
    }
}
