package com.lowagie.text;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.InputStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DocumentTest {

    private static final String ERROR_MESSAGE_TEMPLATE = "Version number in code (%s) is not the same as pom (%s).";

    @Test
    void testThatVersionIsCorrect() {
        // Given
        String versionFromPom = getProjectVersion();
        String versionInCode = Document.getVersion();

        // Then
        Assertions.assertTrue(
            versionInCode.endsWith(versionFromPom),
            String.format(ERROR_MESSAGE_TEMPLATE, versionInCode, versionFromPom));
    }

    private String getProjectVersion() {
        InputStream is = this.getClass().getClassLoader().getResourceAsStream("com/lowagie/text/version.properties");
        String version = null;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            version = reader.readLine();
            int eqPosition = version.indexOf('=');
            if (eqPosition >= 0){
                version = version.substring(eqPosition + 1);
            }
        } catch (IOException ignored) {
        }
        return version;
    }

}
