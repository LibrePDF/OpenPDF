package com.lowagie.text.pdf;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


public class PdfFormFlatteningTest {


    /**
     * Flattens a problematic document. Issue described here: https://stackoverflow.com/questions/47797647
     *
     * @throws IOException
     */
    @Test
    void testFlattenSignatureDocument() throws IOException {
        try (InputStream resource = getClass().getResourceAsStream(
                "/flattening/20231027-DistortedFlatteningInternetExample.pdf")) {

            Assertions.assertNotNull(resource, "File could not be found!");

            FileOutputStream fos = new FileOutputStream(
                    new File("target/20231027-DistortedFlatteningInternetExample-flattened.pdf"));

            PdfReader pdfReader = new PdfReader(resource);
            PdfStamper stamper = new PdfStamper(pdfReader, fos);

            stamper.setFormFlattening(true);

            pdfReader.close();
            stamper.close();
        }
        //Verify no form fields left (the correct shape is difficult to verify...)
        try (InputStream resource = new FileInputStream(
                new File("target/20231027-DistortedFlatteningInternetExample-flattened.pdf"))) {

            Assertions.assertNotNull(resource, "File could not be found!");
            PdfReader pdfReader = new PdfReader(resource);

            PdfDictionary acroForm = (PdfDictionary) PdfReader.getPdfObjectRelease(
                    pdfReader.getCatalog().get(PdfName.ACROFORM));
            Assertions.assertTrue(
                    acroForm == null || acroForm.getAsArray(PdfName.FIELDS) == null || acroForm.getAsArray(
                            PdfName.FIELDS).isEmpty());

            pdfReader.close();
        }
    }

    /**
     * Flattens a problematic document. Issue described here: https://stackoverflow.com/questions/47755629
     *
     * @throws IOException
     */
    @Test
    void testFlattenCheckboxDocument() throws IOException {
        try (InputStream resource = getClass().getResourceAsStream("/flattening/20180301-CheckboxFlatteningBug.pdf")) {

            Assertions.assertNotNull(resource, "File could not be found!");
            FileOutputStream fos = new FileOutputStream(
                    new File("target/20180301-CheckboxFlatteningBug-flattened.pdf"));

            PdfReader pdfReader = new PdfReader(resource);
            PdfStamper stamper = new PdfStamper(pdfReader, fos);

            stamper.setFormFlattening(true);

            pdfReader.close();
            stamper.close();
        }
        //Verify no form fields left (the correct shape is difficult to verify...)
        try (InputStream resource = new FileInputStream(
                new File("target/20180301-CheckboxFlatteningBug-flattened.pdf"))) {

            Assertions.assertNotNull(resource, "File could not be found!");
            PdfReader pdfReader = new PdfReader(resource);

            PdfDictionary acroForm = (PdfDictionary) PdfReader.getPdfObjectRelease(
                    pdfReader.getCatalog().get(PdfName.ACROFORM));
            Assertions.assertTrue(
                    acroForm == null || acroForm.getAsArray(PdfName.FIELDS) == null || acroForm.getAsArray(
                            PdfName.FIELDS).isEmpty());

            pdfReader.close();
        }
    }

    /**
     * Flattens a problematic document. Issue described here: https://stackoverflow.com/questions/47755629
     *
     * @throws IOException
     */
    @Test
    void testFlattenTextfieldsWithRotationAndMatrix() throws IOException {
        try (InputStream resource = getClass().getResourceAsStream(
                "/flattening/20231027-DistortedFlatteningSmall.pdf")) {
            Assertions.assertNotNull(resource, "File could not be found!");
            FileOutputStream fos = new FileOutputStream(
                    new File("target/20231027-DistortedFlatteningSmall-flattened.pdf"));

            PdfReader pdfReader = new PdfReader(resource);
            PdfStamper stamper = new PdfStamper(pdfReader, fos);

            stamper.setFormFlattening(true);

            pdfReader.close();
            stamper.close();
        }
        //Verify no form fields left (the correct shape is difficult to verify...)
        try (InputStream resource = new FileInputStream(
                new File("target/20231027-DistortedFlatteningSmall-flattened.pdf"))) {

            Assertions.assertNotNull(resource, "File could not be found!");
            PdfReader pdfReader = new PdfReader(resource);

            PdfDictionary acroForm = (PdfDictionary) PdfReader.getPdfObjectRelease(
                    pdfReader.getCatalog().get(PdfName.ACROFORM));
            Assertions.assertTrue(
                    acroForm == null || acroForm.getAsArray(PdfName.FIELDS) == null || acroForm.getAsArray(
                            PdfName.FIELDS).isEmpty());

            pdfReader.close();
        }
    }

    @Test
    void testFlattenFieldsWithPdfIndirectObjectInRect() throws IOException {
        final Path targetFilePath = Paths.get("target/indirect_object_in_rectangle-flattened.pdf");
        try (InputStream resource = getClass().getResourceAsStream(
                "/flattening/indirect_object_in_rectangle.pdf")) {
            Assertions.assertNotNull(resource, "File could not be found!");
            OutputStream fos = Files.newOutputStream(targetFilePath);

            PdfReader pdfReader = new PdfReader(resource);
            PdfStamper stamper = new PdfStamper(pdfReader, fos);

            stamper.getAcroFields().setGenerateAppearances(true);
            stamper.setFormFlattening(true);

            pdfReader.close();
            stamper.close();
        }
        //Verify no form fields left (the correct shape is difficult to verify...)
        try (InputStream resource = Files.newInputStream(targetFilePath)) {

            Assertions.assertNotNull(resource, "File could not be found!");
            PdfReader pdfReader = new PdfReader(resource);

            PdfDictionary acroForm = (PdfDictionary) PdfReader.getPdfObjectRelease(
                    pdfReader.getCatalog().get(PdfName.ACROFORM));
            Assertions.assertTrue(acroForm == null
                    || acroForm.getAsArray(PdfName.FIELDS) == null
                    || acroForm.getAsArray(PdfName.FIELDS).isEmpty());

            pdfReader.close();
        }
    }

}
