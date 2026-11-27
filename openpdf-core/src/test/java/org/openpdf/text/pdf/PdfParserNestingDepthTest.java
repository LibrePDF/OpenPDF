package org.openpdf.text.pdf;

import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.openpdf.text.exceptions.InvalidPdfException;

/**
 * Arrays and dictionaries nested deeper than any real document needs must be reported as an error instead of
 * exhausting the stack.
 */
@Timeout(value = 30, unit = TimeUnit.SECONDS)
class PdfParserNestingDepthTest {

    private static final String TOO_DEEP = "Arrays and dictionaries are nested too deeply.";

    @Test
    void readerRejectsDeeplyNestedArrays() {
        PdfReader reader = readerOf("[".repeat(100_000));

        assertThatThrownBy(reader::readPRObject)
                .isInstanceOf(InvalidPdfException.class)
                .hasMessage(TOO_DEEP);
    }

    @Test
    void readerRejectsDeeplyNestedDictionaries() {
        PdfReader reader = readerOf("<</A".repeat(100_000));

        assertThatThrownBy(reader::readPRObject)
                .isInstanceOf(InvalidPdfException.class)
                .hasMessage(TOO_DEEP);
    }

    @Test
    void readerAcceptsNestingUpToTheLimitAfterAnError() throws Exception {
        PdfReader reader = readerOf("[".repeat(PdfReader.MAX_NESTING_DEPTH + 1));
        assertThatThrownBy(reader::readPRObject).isInstanceOf(InvalidPdfException.class);

        // the failed attempt must not count towards the depth of the next object
        int depth = PdfReader.MAX_NESTING_DEPTH;
        reader.tokens = new PRTokeniser(("[".repeat(depth) + "]".repeat(depth)).getBytes(ISO_8859_1));
        PdfObject object = reader.readPRObject();

        assertThat(depthOf(object)).isEqualTo(depth);
    }

    @Test
    void documentWithDeeplyNestedObjectIsReadWithoutIt() throws Exception {
        StringBuilder pdf = new StringBuilder("%PDF-1.4\n");
        int[] offsets = new int[4];
        offsets[0] = pdf.length();
        pdf.append("1 0 obj\n<< /Type /Catalog /Pages 2 0 R /Extra 4 0 R >>\nendobj\n");
        offsets[1] = pdf.length();
        pdf.append("2 0 obj\n<< /Type /Pages /Kids [3 0 R] /Count 1 >>\nendobj\n");
        offsets[2] = pdf.length();
        pdf.append("3 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 10 10] >>\nendobj\n");
        offsets[3] = pdf.length();
        pdf.append("4 0 obj\n").append("[".repeat(100_000)).append("\nendobj\n");
        int xref = pdf.length();
        pdf.append("xref\n0 5\n0000000000 65535 f \n");
        for (int offset : offsets) {
            pdf.append(String.format("%010d 00000 n \n", offset));
        }
        pdf.append("trailer\n<< /Size 5 /Root 1 0 R >>\nstartxref\n").append(xref).append("\n%%EOF\n");

        try (PdfReader reader = new PdfReader(pdf.toString().getBytes(ISO_8859_1))) {
            assertThat(reader.getNumberOfPages()).isEqualTo(1);
            assertThat(reader.getPdfObject(4)).isNull();
        }
    }

    @Test
    void contentParserRejectsDeeplyNestedArrays() {
        PdfContentParser parser = contentParserOf("[".repeat(100_000));

        assertThatThrownBy(() -> parser.parse(null))
                .isInstanceOf(IOException.class)
                .hasMessage(TOO_DEEP);
    }

    @Test
    void contentParserRejectsDeeplyNestedDictionaries() {
        PdfContentParser parser = contentParserOf("<</A".repeat(100_000));

        assertThatThrownBy(() -> parser.parse(null))
                .isInstanceOf(IOException.class)
                .hasMessage(TOO_DEEP);
    }

    @Test
    void contentParserAcceptsNestingUpToTheLimitAfterAnError() throws Exception {
        PdfContentParser parser = contentParserOf("[".repeat(PdfReader.MAX_NESTING_DEPTH + 1));
        assertThatThrownBy(() -> parser.parse(null)).isInstanceOf(IOException.class);

        int depth = PdfReader.MAX_NESTING_DEPTH;
        parser.setTokeniser(new PRTokeniser(("[".repeat(depth) + "]".repeat(depth) + " TJ").getBytes(ISO_8859_1)));
        List<PdfObject> operands = parser.parse(null);

        assertThat(operands).hasSize(2);
        assertThat(depthOf(operands.get(0))).isEqualTo(depth);
    }

    private static int depthOf(PdfObject object) {
        int depth = 0;
        PdfObject current = object;
        while (current instanceof PdfArray array) {
            ++depth;
            current = array.isEmpty() ? null : array.getPdfObject(0);
        }
        return depth;
    }

    private static PdfReader readerOf(String content) {
        PdfReader reader = new PdfReader();
        reader.tokens = new PRTokeniser(content.getBytes(ISO_8859_1));
        return reader;
    }

    private static PdfContentParser contentParserOf(String content) {
        return new PdfContentParser(new PRTokeniser(content.getBytes(ISO_8859_1)));
    }
}
