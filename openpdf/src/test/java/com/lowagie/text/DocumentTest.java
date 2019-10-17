package com.lowagie.text;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class DocumentTest {

    @Test
    void testThatVersionIsCorrect() {
        // Given
        String versionInCode = Document.getVersion();

        // Then
        assertThat(versionInCode)
                .as("Version number in code %s is not correct.", versionInCode)
                .matches("^OpenPDF \\d+\\.\\d+\\.\\d+(-SNAPSHOT)?$");
    }

}
