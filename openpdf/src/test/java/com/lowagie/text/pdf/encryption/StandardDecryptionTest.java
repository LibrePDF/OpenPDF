package com.lowagie.text.pdf.encryption;

import com.lowagie.text.pdf.PdfEncryption;
import com.lowagie.text.pdf.PdfWriter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class StandardDecryptionTest {

    @Test
    public void testAESDecryptionOnEmptyArray() {
        // setup AES 128 encryption
        PdfEncryption decrypt = new PdfEncryption();
        decrypt.setCryptoMode(PdfWriter.ENCRYPTION_AES_128, 0);
        byte[] key = new byte[16]; // create fake key, it does not matter for this test
        decrypt.setupByEncryptionKey(key, 128);
        decrypt.setHashKey(0, 0);

        // try to decrypt an empty array - expected no NPE
        byte[] result = decrypt.decryptByteArray(new byte[]{});

        // verify empty array returned
        Assertions.assertNotNull(result);
        Assertions.assertEquals(result.length, 0);
    }

}
