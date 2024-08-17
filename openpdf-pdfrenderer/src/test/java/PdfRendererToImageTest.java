/*
 * Copyright 2024 Andreas Røsdal.
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the License.
 *
 * The Original Code is 'OpenPDF'.
 *
 * The Initial Developer of the Original Code is Bruno Lowagie. Portions created by
 * the Initial Developer are Copyright (C) 1999-2008 by Bruno Lowagie.
 * All Rights Reserved.
 * Co-Developer of the code is Paulo Soares. Portions created by the Co-Developer
 * are Copyright (C) 2000-2008 by Paulo Soares. All Rights Reserved.
 *
 * Contributor(s): all the names of the contributors are added in the source code
 * where applicable.
 *
 * Alternatively, the contents of this file may be used under the terms of the
 * LGPL license (the "GNU LIBRARY GENERAL PUBLIC LICENSE"), in which case the
 * provisions of LGPL are applicable instead of those above.  If you wish to
 * allow use of your version of this file only under the terms of the LGPL
 * License and not to allow others to use your version of this file under
 * the MPL, indicate your decision by deleting the provisions above and
 * replace them with the notice and other provisions required by the LGPL.
 * If you do not delete the provisions above, a recipient may use your version
 * of this file under either the MPL or the GNU LIBRARY GENERAL PUBLIC LICENSE.
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the MPL as stated above or under the terms of the GNU
 * Library General Public License as published by the Free Software Foundation;
 * either version 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Library general Public License for more
 * details.
 *
 * If you didn't download this code from the following link, you should check if
 * you aren't using an obsolete version:
 * https://github.com/LibrePDF/OpenPDF
 */

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import org.junit.jupiter.api.Test;
import com.github.librepdf.pdfrenderer.PDFFile;
import com.github.librepdf.pdfrenderer.PDFPage;
import com.github.librepdf.pdfrenderer.PDFRenderer;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class PdfRendererToImageTest {

    @Test
    public void testPDFRenderToImage() throws DocumentException, IOException {
        // Step 1: Create a sample PDF using OpenPDF
        byte[] pdfBytes = createSamplePdf();

        // Step 2: Load the PDF using PDFRenderer
        PDFFile pdfFile = new PDFFile(ByteBuffer.wrap(pdfBytes));

        // Ensure there is at least one page in the PDF
        int numPages = pdfFile.getNumPages();
        assert(numPages > 0);

        // Use the correct page index (PDFRenderer typically uses 1-based indexing)
        PDFPage page = pdfFile.getPage(1, true);  // Fetch the first page

        // Step 3: Setup the dimensions for the output image
        int width = (int) page.getBBox().getWidth();
        int height = (int) page.getBBox().getHeight();
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        // Step 4: Render the page to the image
        Rectangle rect = new Rectangle(0, 0, width, height);
        PDFRenderer renderer = new PDFRenderer(page, g2d, rect, null, Color.WHITE);
        renderer.run();

        // Step 5: Save the image to a file (for verification)
        File outputfile = new File("pdf-to-image-test-output.png");
        ImageIO.write(image, "png", outputfile);

        // Step 6: Assert that the image is not null
        assertNotNull(image, "Image should not be null");

        // Cleanup graphics object
        g2d.dispose();
    }

    private byte[] createSamplePdf() throws DocumentException, IOException {
        // Use OpenPDF to create a simple PDF in memory
        Document document = new Document();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, baos);

        document.open();
        document.add(new Paragraph("Hello, World! This is a test PDF."));
        document.close();

        return baos.toByteArray();
    }
}
