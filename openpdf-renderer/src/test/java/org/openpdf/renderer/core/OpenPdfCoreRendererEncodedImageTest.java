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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;

/**
 * JPEG images, whose size is in the header of the image and not only in the PDF file.
 */
class OpenPdfCoreRendererEncodedImageTest {

    @Test
    void decodesJpegImage() throws Exception {
        BufferedImage image = OpenPdfCorePageRenderer.readEncodedImage(jpeg(8, 8));

        assertThat(image.getWidth()).isEqualTo(8);
        assertThat(image.getHeight()).isEqualTo(8);
    }

    @Test
    void refusesJpegImageThatAnnouncesTooManyPixels() throws Exception {
        byte[] jpeg = withSize(jpeg(8, 8), 25_000, 25_000);

        assertThatThrownBy(() -> OpenPdfCorePageRenderer.readEncodedImage(jpeg))
                .isInstanceOf(IOException.class)
                .hasMessageContaining("625000000 pixels");
    }

    @Test
    void doesNotDecodeOtherFormatsAsJpeg() throws Exception {
        ByteArrayOutputStream png = new ByteArrayOutputStream();
        ImageIO.write(new BufferedImage(8, 8, BufferedImage.TYPE_INT_RGB), "png", png);

        assertThat(OpenPdfCorePageRenderer.readEncodedImage(png.toByteArray())).isNull();
        assertThat(OpenPdfCorePageRenderer.readEncodedImage(new byte[]{1, 2, 3})).isNull();
    }

    @Test
    void rendersPageWithJpegImage() throws Exception {
        BufferedImage page = render(jpeg(8, 8), 8);

        assertThat(isRed(page.getRGB(50, 150))).isTrue();
    }

    @Test
    void rendersPageWithoutJpegImageThatAnnouncesTooManyPixels() throws Exception {
        BufferedImage page = render(withSize(jpeg(8, 8), 25_000, 25_000), 8);

        assertThat(isRed(page.getRGB(50, 150))).isFalse();
    }

    /**
     * A red JPEG image.
     */
    private static byte[] jpeg(int width, int height) throws IOException {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setColor(Color.RED);
        graphics.fillRect(0, 0, width, height);
        graphics.dispose();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(image, "jpeg", out);
        return out.toByteArray();
    }

    /**
     * Changes the size in the frame header of a JPEG image and leaves the image data as it is.
     */
    private static byte[] withSize(byte[] jpeg, int width, int height) {
        byte[] changed = jpeg.clone();
        for (int k = 0; k < changed.length - 8; k++) {
            // SOF0, the header of a baseline frame: marker, length, precision, height, width
            if ((changed[k] & 0xFF) == 0xFF && (changed[k + 1] & 0xFF) == 0xC0) {
                changed[k + 5] = (byte) (height >> 8);
                changed[k + 6] = (byte) height;
                changed[k + 7] = (byte) (width >> 8);
                changed[k + 8] = (byte) width;
                return changed;
            }
        }
        throw new IllegalArgumentException("No frame header found");
    }

    /**
     * Renders a page of 200 x 200 points that draws the JPEG image in a square of 100 points in the lower left corner.
     */
    private static BufferedImage render(byte[] jpeg, int declaredSize) throws Exception {
        String data = new String(jpeg, StandardCharsets.ISO_8859_1);
        String content = "q 100 0 0 100 0 0 cm /Im0 Do Q";
        List<String> objects = new ArrayList<>();
        objects.add("<< /Type /Catalog /Pages 2 0 R >>");
        objects.add("<< /Type /Pages /Kids [3 0 R] /Count 1 >>");
        objects.add("<< /Type /Page /Parent 2 0 R /MediaBox [0 0 200 200] /Resources << /XObject << /Im0 4 0 R >> >>"
                + " /Contents 5 0 R >>");
        objects.add("<< /Type /XObject /Subtype /Image /Width " + declaredSize + " /Height " + declaredSize
                + " /BitsPerComponent 8 /ColorSpace /DeviceRGB /Filter /DCTDecode /Length " + data.length()
                + " >>\nstream\n" + data + "\nendstream");
        objects.add("<< /Length " + content.length() + " >>\nstream\n" + content + "\nendstream");
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

    private static boolean isRed(int argb) {
        return ((argb >> 16) & 0xFF) > 200 && ((argb >> 8) & 0xFF) < 60 && (argb & 0xFF) < 60;
    }
}
