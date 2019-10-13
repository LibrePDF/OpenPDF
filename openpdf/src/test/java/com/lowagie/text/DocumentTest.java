package com.lowagie.text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class DocumentTest {

    private static final String ERROR_MESSAGE_TEMPLATE = "Version number in code (%s) is not the same as pom (%s).";

    // TODO:2019-10-13:asturio:Reactivate or delete this test after Version-number refactoring
    // it is probably also maybe a integration test, and should run with failsafe.
    @Disabled("Test is not working, and will be changed after refactoring of version number")
    @Test
    void testThatVersionIsCorrect() {
        // Given
        String versionFromPom = getProjectVersion();
        String versionInCode = Document.getVersion();
        // maven-bundle-plugin transforms 1.0.5-SNAPSHOT to 1.0.5.SNAPSHOT
        String versionFromPomNormalized = versionFromPom.replace('-', '.');
        // Then
        Assertions.assertTrue(
                versionInCode.endsWith(versionFromPomNormalized),
                String.format(ERROR_MESSAGE_TEMPLATE, versionInCode, versionFromPom));
    }

    private String getProjectVersion() {
        InputStream is = this.getClass().getClassLoader().getResourceAsStream("version.txt");
        String version = null;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            version = reader.readLine();
        } catch (IOException ignored) {
        }
        return version;
    }

}
