package org.openpdf.text.pdf;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.openpdf.text.Document;
import org.openpdf.text.DocumentException;
import org.openpdf.text.pdf.parser.PdfTextExtractor;

/**
 * Regression tests for supplementary Unicode lost when FOP glyph substitution is enabled.
 * The old processor looked up each UTF-16 surrogate half instead of the complete code point.
 */
class FopSupplementaryUnicodeTest {

    private static final String LATIN_FONT = "fonts/liberation/LiberationSerif-Regular.ttf";
    private static final String COMBINING_FONT = "fonts/Viaoda_Libre/ViaodaLibre-Regular.ttf";
    private static final String SUPPLEMENTARY_FONT = "fonts/ivs/Hei_MSCS.ttf";

    // Check both the FOP glyph mapping and the emitted PDF for characters in an existing test font.
    @ParameterizedTest
    @ValueSource(ints = {0x20546, 0x2070E})
    void supplementaryGlyphIsExtractableAndEmittedWithFop(int codePoint)
            throws DocumentException, IOException, URISyntaxException {
        assertThat(FopGlyphProcessor.isFopSupported()).isTrue();
        BaseFont specialFont = font(SUPPLEMENTARY_FONT);
        String character = new String(Character.toChars(codePoint));
        // Call the processor directly so this test cannot pass through the non-FOP fallback.
        Map<Integer, int[]> longTag = new HashMap<>();
        byte[] glyphs = FopGlyphProcessor.convertToBytesWithGlyphs(specialFont, character,
                fontPath(SUPPLEMENTARY_FONT).toString(), longTag, "dflt");
        assertThat(glyphs).isNotEmpty();
        assertThat(longTag.values()).anySatisfy(tag -> assertThat(tag[2]).isEqualTo(codePoint));

        byte[] pdf = writePdf(character, specialFont, font(LATIN_FONT));
        try (PdfReader reader = new PdfReader(pdf)) {
            // OpenPDF's extractor inserts spaces when the font changes between A, the glyph, and B.
            assertThat(new PdfTextExtractor(reader).getTextFromPage(1).strip().replace(" ", ""))
                    .isEqualTo("A" + character + "B");
        }

        // The original bug emitted an empty Tj; the glyph-only page must carry encoded text.
        assertThat(hasNonEmptyTextOperator(writePdf(character, specialFont))).isTrue();
    }

    // Smoke-check BMP glyph production and exact extraction of accented and combining text.
    @Test
    void bmpLatinCombiningAndThaiStillUseFop() throws DocumentException, IOException, URISyntaxException {
        assertFopProducesGlyphs("office fi ffi", LATIN_FONT);
        assertFopProducesGlyphs("A\u0301B", COMBINING_FONT);
        assertFopProducesGlyphs("คำทดสอบ", "fonts/Sarabun/Sarabun-Regular.ttf");
        assertExtractable("AéB", LATIN_FONT);
        assertExtractable("A\u0301B", COMBINING_FONT);
    }

    // Read back a single-font PDF with OpenPDF's own extractor.
    private static void assertExtractable(String text, String fontResource)
            throws DocumentException, IOException, URISyntaxException {
        try (PdfReader reader = new PdfReader(writePdf(text, font(fontResource)))) {
            assertThat(new PdfTextExtractor(reader).getTextFromPage(1).strip()).isEqualTo(text);
        }
    }

    // Find a Tj operator carrying glyph bytes in the decoded page content.
    private static boolean hasNonEmptyTextOperator(byte[] pdf) throws IOException {
        try (PdfReader reader = new PdfReader(pdf)) {
            PdfContentParser parser = new PdfContentParser(new PRTokeniser(reader.getPageContent(1)));
            List<PdfObject> operands;
            while (!(operands = parser.parse(null)).isEmpty()) {
                if ("Tj".equals(operands.getLast().toString())
                        && operands.getFirst() instanceof PdfString text
                        && text.getBytes().length > 0) {
                    return true;
                }
            }
            return false;
        }
    }

    // Check that the direct FOP path produces some mapped glyphs; this does not verify shaping.
    private static void assertFopProducesGlyphs(String text, String fontResource)
            throws DocumentException, IOException, URISyntaxException {
        Map<Integer, int[]> longTag = new HashMap<>();
        byte[] glyphs = FopGlyphProcessor.convertToBytesWithGlyphs(font(fontResource), text,
                fontPath(fontResource).toString(), longTag, "dflt");
        assertThat(glyphs).isNotEmpty();
        assertThat(longTag).isNotEmpty();
    }

    // Write a single-font page so its text operator contains only the selected run.
    private static byte[] writePdf(String text, BaseFont selectedFont) throws DocumentException {
        return writePdf(text, selectedFont, null);
    }

    // Optionally surround the selected run with A and B to check extraction across font changes.
    private static byte[] writePdf(String character, BaseFont specialFont, BaseFont latinFont)
            throws DocumentException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (Document document = new Document()) {
            PdfWriter writer = PdfWriter.getInstance(document, output);
            // Writer creation resets the option, so enable it after getInstance.
            document.setGlyphSubstitutionEnabled(true);
            assertThat(writer.pdf.isGlyphSubstitutionEnabled()).isTrue();
            document.open();
            PdfContentByte content = writer.getDirectContent();
            content.beginText();
            content.setTextMatrix(36, 760);
            if (latinFont != null) {
                content.setFontAndSize(latinFont, 16);
                content.showTextBasic("A");
            }
            content.setFontAndSize(specialFont, 16);
            content.showTextBasic(character);
            if (latinFont != null) {
                content.setFontAndSize(latinFont, 16);
                content.showTextBasic("B");
            }
            content.endText();
        }
        return output.toByteArray();
    }

    // Embed the font so the PDF test does not depend on fonts installed on the machine.
    private static BaseFont font(String resource) throws DocumentException, IOException, URISyntaxException {
        return BaseFont.createFont(fontPath(resource).toString(), BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
    }

    // FOP's font cache needs the resource's file path for the TrueType lookup.
    private static Path fontPath(String resource) throws URISyntaxException {
        URL fontUrl = Objects.requireNonNull(FopSupplementaryUnicodeTest.class.getClassLoader().getResource(resource),
                "Missing test font: " + resource);
        return Path.of(fontUrl.toURI());
    }
}
