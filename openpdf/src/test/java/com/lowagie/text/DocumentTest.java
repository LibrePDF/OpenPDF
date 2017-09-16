package com.lowagie.text;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class DocumentTest {

    @Test
    public void testThatVersionIsCorrect() throws IOException {
        // Compare Version with
        String versionString = getVersionFromPom();

        final String version = Document.getVersion();
        Assert.assertTrue("Version number in code is not the same as pom.", version.endsWith(versionString));
    }

    private String getVersionFromPom() throws IOException {
        String versionString = "1.0.0-SNAPSHOT";
        final List<String> strings = Files.readAllLines(Paths.get("pom.xml"), StandardCharsets.UTF_8);
        for (String string : strings) {
            if (string != null && string.contains("<version>")) {
                versionString = string.replaceAll("^.*<version>(.*)</version>.*", "$1");
                break;
            }
        }
        return versionString;
    }

}