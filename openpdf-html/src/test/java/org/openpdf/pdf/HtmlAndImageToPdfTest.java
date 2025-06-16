package org.openpdf.pdf;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.*;

public class HtmlAndImageToPdfTest {

    @Test
    void generatePdfWithImage() throws Exception {
        // Load image from resources
        URL imageUrl = getClass().getClassLoader().getResource("norway.png");
        assertNotNull(imageUrl, "Image resource 'norway.png' not found");
        String imageUri = imageUrl.toExternalForm();

        // HTML content
        String html = """
            <html>
              <head>
                <style>
                  @page {
                    size: A4;
                    margin: 2cm;
                  }
                  body {
                    font-family: Arial, sans-serif;
                    font-size: 12pt;
                    color: #333;
                  }
                  h1 {
                    color: navy;
                  }
                  img.flag {
                    margin-top: 20px;
                    width: 80px;
                    height: auto;
                    border: 1px solid #ccc;
                  }
                </style>
              </head>
              <body>
                <h1>Hello, World!</h1>
                <p>This PDF includes the Norwegian flag:</p>
                <img class="flag" src="%s" alt="Norwegian Flag"/>
              </body>
            </html>
            """.formatted(imageUri);

        // Save PDF to target/test-output/
        File outputDir = new File("target/test-output");
        outputDir.mkdirs();
        File pdfFile = new File(outputDir, "norway-flag.pdf");

        try (OutputStream outputStream = new FileOutputStream(pdfFile)) {
            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocumentFromString(html);
            renderer.layout();
            renderer.createPDF(outputStream);
        }

        System.out.println("PDF created at: " + pdfFile.getAbsolutePath());

        assertTrue(pdfFile.exists(), "PDF file should exist");
        assertTrue(pdfFile.length() > 1000, "PDF file should not be empty");
    }
}
