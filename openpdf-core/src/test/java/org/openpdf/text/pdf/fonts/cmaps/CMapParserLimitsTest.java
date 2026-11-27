package org.openpdf.text.pdf.fonts.cmaps;

import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

/**
 * A damaged CMap must be reported as an error instead of keeping the parser busy without end.
 */
@Timeout(value = 30, unit = TimeUnit.SECONDS)
class CMapParserLimitsTest {

    @Test
    void validCMapIsParsed() throws Exception {
        CMap cmap = parse("/CIDInit /ProcSet findresource begin 12 dict begin begincmap\n"
                + "/CIDSystemInfo << /Registry (Adobe) /Ordering (UCS) /Supplement 0 >> def\n"
                + "1 begincodespacerange <00> <FF> endcodespacerange\n"
                + "1 beginbfchar <41> <0042> endbfchar\n"
                + "2 beginbfrange <50> <52> <0061> <60> <61> [<0078> <0079>] endbfrange\n"
                + "endcmap end end\n");

        assertThat(cmap.getCodeSpaceRanges()).hasSize(1);
        assertThat(lookup(cmap, 0x41)).isEqualTo("B");
        assertThat(lookup(cmap, 0x50)).isEqualTo("a");
        assertThat(lookup(cmap, 0x52)).isEqualTo("c");
        assertThat(lookup(cmap, 0x60)).isEqualTo("x");
        assertThat(lookup(cmap, 0x61)).isEqualTo("y");
    }

    @Test
    void unterminatedArrayIsRejected() {
        assertThatThrownBy(() -> parse("[")).isInstanceOf(IOException.class);
        assertThatThrownBy(() -> parse("1 beginbfrange <50> <52> [<0061>")).isInstanceOf(IOException.class);
    }

    @Test
    void codespaceRangeCountBeyondEndOfInputIsRejected() {
        assertThatThrownBy(() -> parse("2147483647 begincodespacerange")).isInstanceOf(IOException.class);
        assertThatThrownBy(() -> parse("2147483647 begincodespacerange <00>")).isInstanceOf(IOException.class);
    }

    @Test
    void baseFontCharCountBeyondEndOfInputIsRejected() {
        assertThatThrownBy(() -> parse("2147483647 beginbfchar")).isInstanceOf(IOException.class);
        assertThatThrownBy(() -> parse("2147483647 beginbfchar <41>")).isInstanceOf(IOException.class);
    }

    @Test
    void baseFontRangeCountBeyondEndOfInputIsRejected() {
        assertThatThrownBy(() -> parse("2147483647 beginbfrange")).isInstanceOf(IOException.class);
        assertThatThrownBy(() -> parse("2147483647 beginbfrange <50> <52>")).isInstanceOf(IOException.class);
    }

    @Test
    void longHexadecimalStringIsRead() throws Exception {
        String value = "0041".repeat(600);

        CMap cmap = parse("1 beginbfchar <41> <" + value + "> endbfchar");

        assertThat(lookup(cmap, 0x41)).isEqualTo("A".repeat(600));
    }

    @Test
    void deeplyNestedArraysAreRejected() {
        assertThatThrownBy(() -> parse("[".repeat(100_000)))
                .isInstanceOf(IOException.class)
                .hasMessage("Arrays and dictionaries in the CMap are nested too deeply.");
    }

    @Test
    void deeplyNestedDictionariesAreRejected() {
        assertThatThrownBy(() -> parse("<< /A ".repeat(100_000)))
                .isInstanceOf(IOException.class)
                .hasMessage("Arrays and dictionaries in the CMap are nested too deeply.");
    }

    @Test
    void parserCanBeUsedAgainAfterAnError() throws Exception {
        CMapParser parser = new CMapParser();
        assertThatThrownBy(() -> parser.parse(stream("[".repeat(100_000)))).isInstanceOf(IOException.class);

        CMap cmap = parser.parse(stream("[[[<41>]]] 1 beginbfchar <41> <0042> endbfchar"));

        assertThat(lookup(cmap, 0x41)).isEqualTo("B");
    }

    private static CMap parse(String content) throws IOException {
        return new CMapParser().parse(stream(content));
    }

    private static ByteArrayInputStream stream(String content) {
        return new ByteArrayInputStream(content.getBytes(ISO_8859_1));
    }

    private static String lookup(CMap cmap, int code) {
        return cmap.lookup(new byte[]{(byte) code}, 0, 1);
    }
}
