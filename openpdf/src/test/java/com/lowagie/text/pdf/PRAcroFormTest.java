package com.lowagie.text.pdf;

import static java.time.Duration.ofMillis;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

import java.io.InputStream;
import org.junit.jupiter.api.Test;

public class PRAcroFormTest {

    @Test
    public void infiniteLoopTest() throws Exception {
        try (InputStream is = PRAcroFormTest.class.getResourceAsStream("/pades_opposite_infinite_loop.pdf");
                PdfReader reader = new PdfReader(is)) {
            assertTimeoutPreemptively(ofMillis(500), () -> {
                reader.getAcroForm();
            });
        }

    }

}
