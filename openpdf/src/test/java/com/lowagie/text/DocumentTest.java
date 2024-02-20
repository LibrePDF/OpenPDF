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

    @Test
    void whenSetFootsToValueShouldSetObject() {
        // Given
        try (Document document = new Document()) {
            HeaderFooter footer = new HeaderFooter(new Phrase("Footer"), false);

            // When
            document.setFooter(footer);

            // Then
            assertThat(document.footer).isEqualTo(footer);
        }
    }

    @Test
    void whenResetFooterShouldSetNull() {
        // Given
        try (Document document = new Document()) {
            HeaderFooter footer = new HeaderFooter(new Phrase("Footer"), false);
            document.setFooter(footer);

            // When
            document.resetFooter();

            // Then
            assertThat(document.footer).isNull();
        }
    }

}
