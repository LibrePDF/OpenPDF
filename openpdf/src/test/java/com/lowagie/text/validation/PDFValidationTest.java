package com.lowagie.text.validation;

import com.lowagie.text.Annotation;
import com.lowagie.text.Document;
import com.lowagie.text.PageSize;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfWriter;
import org.junit.jupiter.api.Test;
import org.verapdf.core.ModelParsingException;
import org.verapdf.gf.model.GFModelParser;
import org.verapdf.pdfa.PDFAValidator;
import org.verapdf.pdfa.flavours.PDFAFlavour;
import org.verapdf.pdfa.results.TestAssertion;
import org.verapdf.pdfa.results.ValidationResult;
import org.verapdf.pdfa.validation.validators.ValidatorFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

/**
 * Validate PDF files created by OpenPDF using Vera.
 */
public class PDFValidationTest {

    @Test
    void validatePDFWithVera() throws Exception {
        Document document = new Document(PageSize.A4);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, byteArrayOutputStream);

        try {
            document.open();
            document.newPage();
            Annotation ann = new Annotation("Title", "Text");
            Rectangle rect = new Rectangle(100, 100);
            document.add(ann);
            document.add(rect);
            document.close();

            // Create a veraPDF validator
            PDFAFlavour flavour = PDFAFlavour.PDFA_1_A;
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
            }
        } catch (ModelParsingException e) {
            e.printStackTrace();
        }
    }

}
