package org.openpdf.renderer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

/**
 * Tests for the way {@link PDFFile} tells numbers and references apart, and for nested arrays and dictionaries.
 */
@Timeout(value = 20, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class PDFFileObjectParsingTest {

    @Test
    void numbersAreRead() throws IOException {
        PDFObject root = open("/Int 5 /Neg -3 /Pos +8 /Real 2.5 /Dot .5 /NegReal -1.25 /Last 7");

        assertThat(root.getDictRef("Int").getType()).isEqualTo(PDFObject.NUMBER);
        assertThat(root.getDictRef("Int").getIntValue()).isEqualTo(5);
        assertThat(root.getDictRef("Neg").getIntValue()).isEqualTo(-3);
        assertThat(root.getDictRef("Pos").getIntValue()).isEqualTo(8);
        assertThat(root.getDictRef("Real").getDoubleValue()).isEqualTo(2.5);
        assertThat(root.getDictRef("Dot").getDoubleValue()).isEqualTo(0.5);
        assertThat(root.getDictRef("NegReal").getDoubleValue()).isEqualTo(-1.25);
        assertThat(root.getDictRef("Last").getIntValue()).isEqualTo(7);
    }

    @Test
    void arrayOfNumbersIsRead() throws IOException {
        PDFObject[] array = open("/A [1 2 3 -4 .5 6]").getDictRef("A").getArray();

        assertThat(array).hasSize(6);
        assertThat(array).allSatisfy(item -> assertThat(item.isIndirect()).isFalse());
        assertThat(array[0].getIntValue()).isEqualTo(1);
        assertThat(array[1].getIntValue()).isEqualTo(2);
        assertThat(array[2].getIntValue()).isEqualTo(3);
        assertThat(array[3].getIntValue()).isEqualTo(-4);
        assertThat(array[4].getDoubleValue()).isEqualTo(0.5);
        assertThat(array[5].getIntValue()).isEqualTo(6);
    }

    @Test
    void referencesAreRead() throws IOException {
        PDFObject root = open("/Ref 4 0 R /Refs [4 0 R 5 0 R] /AfterNumbers [1 2 4 0 R] /Comment [5 % comment\n 0 R]");

        assertThat(root.getDictionary().get("Ref").isIndirect()).isTrue();
        assertThat(root.getDictRef("Ref").getStringValue()).isEqualTo("four");

        PDFObject[] refs = root.getDictRef("Refs").getArray();
        assertThat(refs).hasSize(2);
        assertThat(refs[0].isIndirect()).isTrue();
        assertThat(refs[0].getStringValue()).isEqualTo("four");
        assertThat(refs[1].isIndirect()).isTrue();
        assertThat(refs[1].getIntValue()).isEqualTo(42);

        PDFObject[] afterNumbers = root.getDictRef("AfterNumbers").getArray();
        assertThat(afterNumbers).hasSize(3);
        assertThat(afterNumbers[0].isIndirect()).isFalse();
        assertThat(afterNumbers[0].getIntValue()).isEqualTo(1);
        assertThat(afterNumbers[1].isIndirect()).isFalse();
        assertThat(afterNumbers[1].getIntValue()).isEqualTo(2);
        assertThat(afterNumbers[2].isIndirect()).isTrue();
        assertThat(afterNumbers[2].getStringValue()).isEqualTo("four");

        PDFObject[] comment = root.getDictRef("Comment").getArray();
        assertThat(comment).hasSize(1);
        assertThat(comment[0].isIndirect()).isTrue();
        assertThat(comment[0].getIntValue()).isEqualTo(42);
    }

    @Test
    void numbersFollowedByOtherObjectsAreRead() throws IOException {
        PDFObject[] array = open("/A [1 /Name 2 (text) 3 [4] 5 <</K 6>> 7 0 [8] 9 0 /Name 10 1 true 11 <6162>]")
                .getDictRef("A").getArray();

        int[] types = new int[array.length];
        for (int i = 0; i < array.length; i++) {
            assertThat(array[i].isIndirect()).isFalse();
            types[i] = array[i].getType();
        }
        assertThat(types).containsExactly(
                PDFObject.NUMBER, PDFObject.NAME, PDFObject.NUMBER, PDFObject.STRING,
                PDFObject.NUMBER, PDFObject.ARRAY, PDFObject.NUMBER, PDFObject.DICTIONARY,
                PDFObject.NUMBER, PDFObject.NUMBER, PDFObject.ARRAY,
                PDFObject.NUMBER, PDFObject.NUMBER, PDFObject.NAME,
                PDFObject.NUMBER, PDFObject.NUMBER, PDFObject.BOOLEAN,
                PDFObject.NUMBER, PDFObject.STRING);
        assertThat(array[1].getStringValue()).isEqualTo("Name");
        assertThat(array[3].getStringValue()).isEqualTo("text");
        assertThat(array[5].getAt(0).getIntValue()).isEqualTo(4);
        assertThat(array[7].getDictRef("K").getIntValue()).isEqualTo(6);
        assertThat(array[8].getIntValue()).isEqualTo(7);
        assertThat(array[9].getIntValue()).isZero();
        assertThat(array[10].getAt(0).getIntValue()).isEqualTo(8);
        assertThat(array[17].getIntValue()).isEqualTo(11);
        assertThat(array[18].getStringValue()).isEqualTo("ab");
    }

    @Test
    void deeplyNestedArraysAfterNumbersOpenQuickly() throws IOException {
        PDFObject item = open("/A " + nested("[1 ", "]", 60)).getDictRef("A");

        int depth = 0;
        while (item.getArray().length == 2) {
            assertThat(item.getAt(0).getIntValue()).isEqualTo(1);
            item = item.getAt(1);
            depth++;
        }
        assertThat(depth).isEqualTo(59);
        assertThat(item.getArray()).hasSize(1);
    }

    @Test
    void deeplyNestedArraysAfterTwoNumbersOpenQuickly() throws IOException {
        PDFObject item = open("/A " + nested("[1 0 ", "]", 60)).getDictRef("A");

        int depth = 0;
        while (item.getArray().length == 3) {
            item = item.getAt(2);
            depth++;
        }
        assertThat(depth).isEqualTo(59);
    }

    @Test
    void deeplyNestedDictionariesOpenQuickly() throws IOException {
        PDFObject item = open("/A " + nested("<</N 1 /D ", ">>", 60).replace("/D >>", ">>")).getDictRef("A");

        int depth = 1;
        while (item.getDictRef("D") != null) {
            item = item.getDictRef("D");
            depth++;
        }
        assertThat(depth).isEqualTo(60);
    }

    @Test
    void nestingUpToTheLimitIsRead() throws IOException {
        assertThat(open("/A " + nested("[", "]", 200)).getDictRef("A").getType()).isEqualTo(PDFObject.ARRAY);
    }

    @Test
    void nestingBeyondTheLimitIsRejected() {
        assertThatThrownBy(() -> open("/A " + nested("[", "]", 5000))).isInstanceOf(PDFParseException.class);
        assertThatThrownBy(() -> open("/A " + nested("<</D ", ">>", 5000))).isInstanceOf(PDFParseException.class);
    }

    private static String nested(String open, String close, int depth) {
        return open.repeat(depth) + close.repeat(depth);
    }

    /**
     * Opens a file whose catalog has the given additional entries, with the string "four" as object 4 and the number
     * 42 as object 5.
     *
     * @return the catalog
     */
    private static PDFObject open(String catalogEntries) throws IOException {
        String[] objects = {
                "<< /Type /Catalog /Pages 2 0 R " + catalogEntries + " >>",
                "<< /Type /Pages /Kids [3 0 R] /Count 1 >>",
                "<< /Type /Page /Parent 2 0 R /MediaBox [0 0 100 100] >>",
                "(four)",
                "42"
        };
        StringBuilder content = new StringBuilder("%PDF-1.4\n");
        StringBuilder xref = new StringBuilder("xref\n0 " + (objects.length + 1) + "\n0000000000 65535 f \n");
        for (int i = 0; i < objects.length; i++) {
            xref.append(String.format("%010d 00000 n \n", content.length()));
            content.append(i + 1).append(" 0 obj\n").append(objects[i]).append("\nendobj\n");
        }
        int startxref = content.length();
        content.append(xref).append("trailer\n<< /Size ").append(objects.length + 1).append(" /Root 1 0 R >>\n")
                .append("startxref\n").append(startxref).append("\n%%EOF\n");
        PDFFile file = new PDFFile(ByteBuffer.wrap(content.toString().getBytes(StandardCharsets.ISO_8859_1)));
        return file.getRoot();
    }
}
