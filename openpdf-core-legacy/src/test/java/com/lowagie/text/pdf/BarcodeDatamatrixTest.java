package com.lowagie.text.pdf;

import static org.assertj.core.api.Assertions.assertThat;

import com.lowagie.text.Document;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;
import org.junit.jupiter.api.Test;

class BarcodeDatamatrixTest {

    private static final String HELLO_WORLD = "Hello World";

    @Test
    void exampleBarcodeDatamatrix() throws UnsupportedEncodingException {
        byte[] expectedBytes = {-53, -94, 58, 35, -94, 58, 35, -94, 58, 35, -94, 58, -10, -67, -80, -73, -40, -19, 36,
                17, 124, 16, 90, 17, -125, -39, -124, -118, -73, -96, -112, -126, 43, -81, 35, -92, -127, 5, -124, -126,
                8, 69, -113, 89, 78, -120, -8, 32, -79, 73, 32, 74, 56, 65, 48, -82, 8, 46, -8, 34, -121, -114, 54,
                -110, 14, 8, 44, 52, -120, -13, 88, -120, -72, -32, 2, 0, 32};

        BarcodeDatamatrix datamatrix = new BarcodeDatamatrix();
        datamatrix.setHeight(16);
        datamatrix.setWidth(16);
        datamatrix.setBorder(2);
        datamatrix.setOptions(BarcodeDatamatrix.DM_AUTO);
        datamatrix.generate(HELLO_WORLD);
        Image image = datamatrix.createImage();
        assertThat(image).isNotNull();
        assertThat(image.getWidth()).isEqualTo(20f);
        assertThat(image.getHeight()).isEqualTo(20f);
        assertThat(image.getPlainWidth()).isEqualTo(20f);
        assertThat(image.getPlainHeight()).isEqualTo(20f);
        assertThat(image.getScaledWidth()).isEqualTo(20f);
        assertThat(image.getScaledHeight()).isEqualTo(20f);
        assertThat(image.getAlignment()).isEqualTo(Image.DEFAULT);
        assertThat(image.getRawData()).isEqualTo(expectedBytes);
        assertThat(image.type()).isEqualTo(Image.IMGRAW);

        boolean createPdf = false;
        if (createPdf) {
            try (Document document = new Document(PageSize.A4)) {
                PdfWriter.getInstance(document, new FileOutputStream("target/datamatrix.pdf"));
                document.open();
                document.add(new Paragraph(HELLO_WORLD));
                document.add(image);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }

}