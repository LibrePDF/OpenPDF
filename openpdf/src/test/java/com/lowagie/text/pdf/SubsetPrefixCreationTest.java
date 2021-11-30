package com.lowagie.text.pdf;

import org.apache.commons.io.IOUtils;
import org.bouncycastle.crypto.prng.FixedSecureRandom;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * See : https://github.com/LibrePDF/OpenPDF/issues/623
 */
public class SubsetPrefixCreationTest {

    @Test
    public void createSubsetPrefixTest() throws Exception {
        BaseFont font = BaseFont.createFont("LiberationSerif-Regular.ttf", BaseFont.IDENTITY_H,
                BaseFont.EMBEDDED,true, getFontByte("LiberationSerif-Regular.ttf"), null);
        assertNotEquals(font.createSubsetPrefix(), font.createSubsetPrefix());

        byte[] baseSeed = new SecureRandom().generateSeed(512);
        // init deterministic SecureRandom with a custom base seed
        SecureRandom secureRandom = new FixedSecureRandom(baseSeed);
        font.setSecureRandom(secureRandom);
        assertNotEquals(font.createSubsetPrefix(), font.createSubsetPrefix()); // still different, as FixedSecureRandom generates a new random on each step

        SecureRandom secureRandomOne = new FixedSecureRandom(baseSeed);
        font.setSecureRandom(secureRandomOne);

        String subsetPrefixOne = font.createSubsetPrefix();

        // re-init FixedSecureRandom for deterministic generation
        SecureRandom secureRandomTwo = new FixedSecureRandom(baseSeed);
        font.setSecureRandom(secureRandomTwo);

        String subsetPrefixTwo = font.createSubsetPrefix();
        assertEquals(subsetPrefixOne, subsetPrefixTwo); // the desired deterministic behavior
    }

    private byte[] getFontByte(String fileName) throws IOException {
        try (InputStream stream = BaseFont.getResourceStream(fileName, null)) {
            return IOUtils.toByteArray(stream);
        }
    }

}
