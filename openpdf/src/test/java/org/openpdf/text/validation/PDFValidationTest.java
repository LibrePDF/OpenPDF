package org.openpdf.text.validation;

import org.openpdf.text.Annotation;
import org.openpdf.text.Document;
import org.openpdf.text.PageSize;
import org.openpdf.text.Rectangle;
import org.openpdf.text.pdf.PdfDictionary;
import org.openpdf.text.pdf.PdfName;
import org.openpdf.text.pdf.PdfString;
import org.openpdf.text.pdf.PdfWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.verapdf.core.ModelParsingException;
import org.verapdf.gf.model.GFModelParser;
import org.verapdf.pdfa.PDFAValidator;
import org.verapdf.pdfa.flavours.PDFAFlavour;
import org.verapdf.pdfa.results.TestAssertion;
import org.verapdf.pdfa.results.ValidationResult;
import org.verapdf.pdfa.validation.validators.ValidatorFactory;

/**
 * Validate PDF files created by OpenPDF using Vera.
 */
public class PDFValidationTest {

    @Test
    public void testValidateDcTitleWithVera() throws Exception {
        PdfDictionary info = new PdfDictionary(PdfName.METADATA);
        info.put(PdfName.TITLE, new PdfString("Test pdf"));
        Assertions.assertTrue(testValidatePDFWithVera(info));
    }

    @Test
    public void testValidateDcSubjectWithVera() throws Exception {
        PdfDictionary info = new PdfDictionary(PdfName.METADATA);
        info.put(PdfName.SUBJECT, new PdfString("Test subject"));
        Assertions.assertTrue(testValidatePDFWithVera(info));
    }

    @Test
    public void testValidatePdfKeywordsWithVera() throws Exception {
        PdfDictionary info = new PdfDictionary(PdfName.METADATA);
        info.put(PdfName.KEYWORDS, new PdfString("k1, k2"));
        Assertions.assertTrue(testValidatePDFWithVera(info));
    }

    private boolean testValidatePDFWithVera(PdfDictionary info) throws Exception {
        Document document = new Document(PageSize.A4);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        PdfWriter pdfWriter = PdfWriter.getInstance(document, byteArrayOutputStream);
        pdfWriter.setPDFXConformance(PdfWriter.PDFA1B);
        pdfWriter.getInfo().putAll(info);
        pdfWriter.createXmpMetadata();

        try {
            document.open();
            document.newPage();
            Annotation ann = new Annotation("Title", "Text");
            Rectangle rect = new Rectangle(100, 100);
            document.add(ann);
            document.add(rect);
            document.close();

            // Create a veraPDF validator
            PDFAFlavour flavour = PDFAFlavour.PDFA_1_B;
            try (InputStream inputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
                    GFModelParser parser = GFModelParser.createModelWithFlavour(inputStream, flavour)) {
                PDFAValidator validator = ValidatorFactory.createValidator(flavour, false, 10);
                ValidationResult result = validator.validate(parser);

                // Check the validation result
                if (result.isCompliant()) {
                    System.out.println("The PDF is compliant with the selected PDF/A standard.");
                } else {
                    System.out.println("The PDF is not compliant with the selected PDF/A standard.");
                    System.out.println("Validation errors: " + result.getTestAssertions().size());
                    for (TestAssertion assertion : result.getTestAssertions()) {
                        System.out.println(assertion);
                    }

                }
                return result.isCompliant();
            }
        } catch (ModelParsingException e) {
            e.printStackTrace();
            return false;
        }
    }

}
