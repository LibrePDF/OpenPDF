package com.lowagie.text.pdf.codec;

import com.lowagie.text.pdf.RandomAccessFileOrArray;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Tiff2PdfTest
 *
 * @author tellef
 * @date 02.10.2017
 */
public class TiffReadingTest {

    @Test
    public void transparentTiffTest() throws IOException {
        InputStream inputStream = TiffReadingTest.class.getClassLoader().getResourceAsStream("gradient.tiff");
        byte[] data;

        try(ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream()) {
            int bytesRead;
            byte[] buffer = new byte[8192];

            while ((bytesRead = inputStream.read(buffer, 0, 8192)) != -1) {
                byteOutputStream.write(buffer, 0, bytesRead);
            }

            data = byteOutputStream.toByteArray();
        }

        RandomAccessFileOrArray ra = new RandomAccessFileOrArray(data);
        int pages = TiffImage.getNumberOfPages(ra);

        for (int i = 1; i <= pages; i++) {
            Assert.assertNotNull(TiffImage.getTiffImage(ra, i));
        }
    }
}
