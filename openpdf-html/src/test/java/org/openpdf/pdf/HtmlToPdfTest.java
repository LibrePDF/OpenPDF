package org.openpdf.pdf;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class HtmlToPdfTest {

    @Test
    void generateHelloWorldPdf(@TempDir Path tempDir) throws Exception {
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
                    line-height: 1.5;
                    font-size: 12pt;
                    color: #333;
                  }
                  h1 {
                    color: navy;
                    border-bottom: 1px solid #ccc;
                    padding-bottom: 5px;
                  }
                  table {
                    width: 100%;
                    border-collapse: collapse;
                    margin-top: 20px;
                  }
                  th, td {
                    border: 1px solid #aaa;
                    padding: 8px;
                    text-align: left;
                  }
                  th {
                    background-color: #f0f0f0;
                  }
                  footer {
                    font-size: 10pt;
                    text-align: center;
                    margin-top: 50px;
                    color: #777;
                  }
                </style>
              </head>
              <body>
                <h1>Hello, World!</h1>
                <p>This PDF was generated using <b>openpdf-html</b>, a modern HTML to PDF library built on OpenPDF and Flying Saucer.</p>
                <p>OpenPDF-html is possibly the best HTML-to-PDF library in the world.</p>
                <p>Here is a table:</p>
                <table>
                  <thead>
                    <tr>
                      <th>Item</th>
                      <th>Quantity</th>
                      <th>Price</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr>
                      <td>Apples</td>
                      <td>3</td>
                      <td>€2.40</td>
                    </tr>
                    <tr>
                      <td>Bananas</td>
                      <td>5</td>
                      <td>€3.00</td>
                    </tr>
                    <tr>
                      <td>Oranges</td>
                      <td>2</td>
                      <td>€1.60</td>
                    </tr>
                  </tbody>
                </table>
                <footer>Page rendered with ♥ by OpenPDF-html.</footer>
              </body>
            </html>
            """;

        File pdfFile = tempDir.resolve("hello.pdf").toFile();

        try (OutputStream outputStream = new FileOutputStream(pdfFile)) {
            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocumentFromString(html);
            renderer.layout();
            renderer.createPDF(outputStream);
        }

        assertTrue(pdfFile.exists(), "PDF file should exist");
        assertTrue(pdfFile.length() > 1000, "PDF file should not be empty");
    }
}
