package org.openpdf.text.pdf;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.openpdf.text.Document;
import org.openpdf.text.DocumentException;
import org.openpdf.text.PageSize;
import org.openpdf.text.Phrase;
import org.openpdf.text.pdf.internal.PdfXConformanceImp;
import org.openpdf.text.xml.xmp.PdfA1Schema;
import org.openpdf.text.xml.xmp.XmpWriter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Tests for PDF/A-2 and PDF/A-3 conformance levels.
 */
class PdfAConformanceTest {

    @Test
    void testPdfA1AConformance() throws DocumentException, IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);
        PdfWriter writer = PdfWriter.getInstance(document, baos);
        writer.setPDFXConformance(PdfWriter.PDFA1A);
        
        assertThat(writer.isPdfA1()).isTrue();
        assertThat(writer.isPdfA2()).isFalse();
        assertThat(writer.isPdfA3()).isFalse();
        assertThat(writer.isPdfA()).isTrue();
        assertThat(writer.getPDFXConformance()).isEqualTo(PdfWriter.PDFA1A);
    }

    @Test
    void testPdfA1BConformance() throws DocumentException, IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);
        PdfWriter writer = PdfWriter.getInstance(document, baos);
        writer.setPDFXConformance(PdfWriter.PDFA1B);
        
        assertThat(writer.isPdfA1()).isTrue();
        assertThat(writer.isPdfA2()).isFalse();
        assertThat(writer.isPdfA3()).isFalse();
        assertThat(writer.isPdfA()).isTrue();
        assertThat(writer.getPDFXConformance()).isEqualTo(PdfWriter.PDFA1B);
    }

    @Test
    void testPdfA2AConformance() throws DocumentException, IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);
        PdfWriter writer = PdfWriter.getInstance(document, baos);
        writer.setPDFXConformance(PdfWriter.PDFA2A);
        
        assertThat(writer.isPdfA1()).isFalse();
        assertThat(writer.isPdfA2()).isTrue();
        assertThat(writer.isPdfA3()).isFalse();
        assertThat(writer.isPdfA()).isTrue();
        assertThat(writer.getPDFXConformance()).isEqualTo(PdfWriter.PDFA2A);
    }

    @Test
    void testPdfA2BConformance() throws DocumentException, IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);
        PdfWriter writer = PdfWriter.getInstance(document, baos);
        writer.setPDFXConformance(PdfWriter.PDFA2B);
        
        assertThat(writer.isPdfA1()).isFalse();
        assertThat(writer.isPdfA2()).isTrue();
        assertThat(writer.isPdfA3()).isFalse();
        assertThat(writer.isPdfA()).isTrue();
        assertThat(writer.getPDFXConformance()).isEqualTo(PdfWriter.PDFA2B);
    }

    @Test
    void testPdfA2UConformance() throws DocumentException, IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);
        PdfWriter writer = PdfWriter.getInstance(document, baos);
        writer.setPDFXConformance(PdfWriter.PDFA2U);
        
        assertThat(writer.isPdfA1()).isFalse();
        assertThat(writer.isPdfA2()).isTrue();
        assertThat(writer.isPdfA3()).isFalse();
        assertThat(writer.isPdfA()).isTrue();
        assertThat(writer.getPDFXConformance()).isEqualTo(PdfWriter.PDFA2U);
    }

    @Test
    void testPdfA3AConformance() throws DocumentException, IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);
        PdfWriter writer = PdfWriter.getInstance(document, baos);
        writer.setPDFXConformance(PdfWriter.PDFA3A);
        
        assertThat(writer.isPdfA1()).isFalse();
        assertThat(writer.isPdfA2()).isFalse();
        assertThat(writer.isPdfA3()).isTrue();
        assertThat(writer.isPdfA()).isTrue();
        assertThat(writer.getPDFXConformance()).isEqualTo(PdfWriter.PDFA3A);
    }

    @Test
    void testPdfA3BConformance() throws DocumentException, IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);
        PdfWriter writer = PdfWriter.getInstance(document, baos);
        writer.setPDFXConformance(PdfWriter.PDFA3B);
        
        assertThat(writer.isPdfA1()).isFalse();
        assertThat(writer.isPdfA2()).isFalse();
        assertThat(writer.isPdfA3()).isTrue();
        assertThat(writer.isPdfA()).isTrue();
        assertThat(writer.getPDFXConformance()).isEqualTo(PdfWriter.PDFA3B);
    }

    @Test
    void testPdfA3UConformance() throws DocumentException, IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);
        PdfWriter writer = PdfWriter.getInstance(document, baos);
        writer.setPDFXConformance(PdfWriter.PDFA3U);
        
        assertThat(writer.isPdfA1()).isFalse();
        assertThat(writer.isPdfA2()).isFalse();
        assertThat(writer.isPdfA3()).isTrue();
        assertThat(writer.isPdfA()).isTrue();
        assertThat(writer.getPDFXConformance()).isEqualTo(PdfWriter.PDFA3U);
    }

    @Test
    void testPdfXNoneConformance() throws DocumentException, IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);
        PdfWriter writer = PdfWriter.getInstance(document, baos);
        // Default should be PDFXNONE
        
        assertThat(writer.isPdfA1()).isFalse();
        assertThat(writer.isPdfA2()).isFalse();
        assertThat(writer.isPdfA3()).isFalse();
        assertThat(writer.isPdfA()).isFalse();
        assertThat(writer.isPdfX()).isFalse();
        assertThat(writer.getPDFXConformance()).isEqualTo(PdfWriter.PDFXNONE);
    }

    @Test
    void testPdfXConformanceImpPdfA2() {
        PdfXConformanceImp imp = new PdfXConformanceImp();
        
        imp.setPDFXConformance(PdfWriter.PDFA2A);
        assertThat(imp.isPdfA2()).isTrue();
        assertThat(imp.isPdfA1()).isFalse();
        assertThat(imp.isPdfA3()).isFalse();
        assertThat(imp.isPdfA()).isTrue();
        
        imp.setPDFXConformance(PdfWriter.PDFA2B);
        assertThat(imp.isPdfA2()).isTrue();
        assertThat(imp.isPdfA()).isTrue();
        
        imp.setPDFXConformance(PdfWriter.PDFA2U);
        assertThat(imp.isPdfA2()).isTrue();
        assertThat(imp.isPdfA()).isTrue();
    }

    @Test
    void testPdfXConformanceImpPdfA3() {
        PdfXConformanceImp imp = new PdfXConformanceImp();
        
        imp.setPDFXConformance(PdfWriter.PDFA3A);
        assertThat(imp.isPdfA3()).isTrue();
        assertThat(imp.isPdfA1()).isFalse();
        assertThat(imp.isPdfA2()).isFalse();
        assertThat(imp.isPdfA()).isTrue();
        
        imp.setPDFXConformance(PdfWriter.PDFA3B);
        assertThat(imp.isPdfA3()).isTrue();
        assertThat(imp.isPdfA()).isTrue();
        
        imp.setPDFXConformance(PdfWriter.PDFA3U);
        assertThat(imp.isPdfA3()).isTrue();
        assertThat(imp.isPdfA()).isTrue();
    }

    @Test
    void testXmpWriterPdfA2A() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfDictionary info = new PdfDictionary();
        info.put(PdfName.TITLE, new PdfString("Test Document"));
        try (XmpWriter xmpWriter = new XmpWriter(baos, info, PdfWriter.PDFA2A)) {
            // XmpWriter should complete successfully for PDF/A-2A
        }
        String xmpContent = baos.toString("UTF-8");
        assertThat(xmpContent).contains("pdfaid:part");
        assertThat(xmpContent).contains(">2<");  // Part should be 2
        assertThat(xmpContent).contains(">A<");  // Conformance should be A
    }

    @Test
    void testXmpWriterPdfA2B() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfDictionary info = new PdfDictionary();
        info.put(PdfName.TITLE, new PdfString("Test Document"));
        try (XmpWriter xmpWriter = new XmpWriter(baos, info, PdfWriter.PDFA2B)) {
            // XmpWriter should complete successfully for PDF/A-2B
        }
        String xmpContent = baos.toString("UTF-8");
        assertThat(xmpContent).contains("pdfaid:part");
        assertThat(xmpContent).contains(">2<");  // Part should be 2
        assertThat(xmpContent).contains(">B<");  // Conformance should be B
    }

    @Test
    void testXmpWriterPdfA2U() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfDictionary info = new PdfDictionary();
        info.put(PdfName.TITLE, new PdfString("Test Document"));
        try (XmpWriter xmpWriter = new XmpWriter(baos, info, PdfWriter.PDFA2U)) {
            // XmpWriter should complete successfully for PDF/A-2U
        }
        String xmpContent = baos.toString("UTF-8");
        assertThat(xmpContent).contains("pdfaid:part");
        assertThat(xmpContent).contains(">2<");  // Part should be 2
        assertThat(xmpContent).contains(">U<");  // Conformance should be U
    }

    @Test
    void testXmpWriterPdfA3A() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfDictionary info = new PdfDictionary();
        info.put(PdfName.TITLE, new PdfString("Test Document"));
        try (XmpWriter xmpWriter = new XmpWriter(baos, info, PdfWriter.PDFA3A)) {
            // XmpWriter should complete successfully for PDF/A-3A
        }
        String xmpContent = baos.toString("UTF-8");
        assertThat(xmpContent).contains("pdfaid:part");
        assertThat(xmpContent).contains(">3<");  // Part should be 3
        assertThat(xmpContent).contains(">A<");  // Conformance should be A
    }

    @Test
    void testXmpWriterPdfA3B() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfDictionary info = new PdfDictionary();
        info.put(PdfName.TITLE, new PdfString("Test Document"));
        try (XmpWriter xmpWriter = new XmpWriter(baos, info, PdfWriter.PDFA3B)) {
            // XmpWriter should complete successfully for PDF/A-3B
        }
        String xmpContent = baos.toString("UTF-8");
        assertThat(xmpContent).contains("pdfaid:part");
        assertThat(xmpContent).contains(">3<");  // Part should be 3
        assertThat(xmpContent).contains(">B<");  // Conformance should be B
    }

    @Test
    void testXmpWriterPdfA3U() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfDictionary info = new PdfDictionary();
        info.put(PdfName.TITLE, new PdfString("Test Document"));
        try (XmpWriter xmpWriter = new XmpWriter(baos, info, PdfWriter.PDFA3U)) {
            // XmpWriter should complete successfully for PDF/A-3U
        }
        String xmpContent = baos.toString("UTF-8");
        assertThat(xmpContent).contains("pdfaid:part");
        assertThat(xmpContent).contains(">3<");  // Part should be 3
        assertThat(xmpContent).contains(">U<");  // Conformance should be U
    }

    @Test
    void testPdfA1SchemaCanSetPart2() {
        PdfA1Schema schema = new PdfA1Schema();
        schema.addPart("2");
        schema.addConformance("B");
        String content = schema.toString();
        assertThat(content).contains("pdfaid:part");
        assertThat(content).contains("pdfaid:conformance");
    }

    @Test
    void testPdfA1SchemaCanSetPart3() {
        PdfA1Schema schema = new PdfA1Schema();
        schema.addPart("3");
        schema.addConformance("U");
        String content = schema.toString();
        assertThat(content).contains("pdfaid:part");
        assertThat(content).contains("pdfaid:conformance");
    }

    @Test
    void testPdfAConformanceCannotBeSetAfterDocumentOpen() throws DocumentException, IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);
        PdfWriter writer = PdfWriter.getInstance(document, baos);
        document.open();
        // Add a simple rectangle so the document has content without using fonts
        PdfContentByte cb = writer.getDirectContent();
        cb.rectangle(100, 100, 200, 200);
        cb.stroke();
        
        assertThatThrownBy(() -> writer.setPDFXConformance(PdfWriter.PDFA2A))
                .isInstanceOf(PdfXConformanceException.class);
        
        document.close();
    }

    @Test
    void testConstantValues() {
        // Verify the constant values are as expected
        assertThat(PdfWriter.PDFXNONE).isEqualTo(0);
        assertThat(PdfWriter.PDFX1A2001).isEqualTo(1);
        assertThat(PdfWriter.PDFX32002).isEqualTo(2);
        assertThat(PdfWriter.PDFA1A).isEqualTo(3);
        assertThat(PdfWriter.PDFA1B).isEqualTo(4);
        assertThat(PdfWriter.PDFA2A).isEqualTo(5);
        assertThat(PdfWriter.PDFA2B).isEqualTo(6);
        assertThat(PdfWriter.PDFA2U).isEqualTo(7);
        assertThat(PdfWriter.PDFA3A).isEqualTo(8);
        assertThat(PdfWriter.PDFA3B).isEqualTo(9);
        assertThat(PdfWriter.PDFA3U).isEqualTo(10);
    }
}
