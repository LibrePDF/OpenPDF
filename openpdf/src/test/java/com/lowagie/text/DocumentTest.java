package com.lowagie.text;

import static org.assertj.core.api.Assertions.assertThat;

import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.junit.jupiter.api.Test;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

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
