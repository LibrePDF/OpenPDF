package com.lowagie.text;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class DocumentTest {

    @Test
    public void testThatVersionIsCorrect() throws IOException {
        // Given
        String versionFromPom = getProjectVersion();
        String versionInCode = Document.getVersion();
        // Then
        Assert.assertTrue("Version number in code (" + versionInCode + ") is not the same as pom (" + versionFromPom + ").", versionInCode.endsWith(versionFromPom));
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
