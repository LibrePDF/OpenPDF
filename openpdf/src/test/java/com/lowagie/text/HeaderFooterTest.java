package com.lowagie.text;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class HeaderFooterTest {

    public static Stream<Arguments> createTestDataBeforeAndAfter() {
        return Stream.of(
                Arguments.of(new Paragraph("before"), new Paragraph("after"), "Paragraph / Paragraph"),
                Arguments.of(new Paragraph("before"), new Phrase("after"), "Paragraph / Phrase"),
                Arguments.of(new Phrase("before"), new Paragraph("after"), "Phrase / Paragraph"),
                Arguments.of(new Phrase("before"), new Phrase("after"), "Phrase / Phrase"),
                Arguments.of(new Footnote("before"), new Phrase("after"), "Footnote / Phrase"),
                Arguments.of(new Footnote("before"), new Footnote("after"), "Footnote / Footnote"),
                Arguments.of(new Anchor("before"), new Paragraph("after"), "Anchor / Paragraph"),
                Arguments.of(new Paragraph("before"), new Anchor("after"), "Paragraph / Anchor"));
    }

    public static Stream<Arguments> createTestDataOnlyBefore() {
        return Stream.of(
                Arguments.of(new Paragraph("before"), "Paragraph"),
                Arguments.of(new Phrase("before"), "Phrase"),
                Arguments.of(new Footnote("before"), "Footnote"),
                Arguments.of(new Anchor("before"), "Anchor"));
    }

    @DisplayName("Before and After with Pagenumber")
    @ParameterizedTest(name = "{2}")
    @MethodSource(value = "createTestDataBeforeAndAfter")
    void testParagraphMethod(Phrase before, Phrase after, String test) {
        HeaderFooter cut = new HeaderFooter(before, after);

        Paragraph actualHeaderFooter = cut.paragraph();

        assertEquals(3, actualHeaderFooter.getChunks().size());
        assertEquals("before", actualHeaderFooter.getChunks().get(0).toString());
        assertEquals("0", actualHeaderFooter.getChunks().get(1).toString());
        assertEquals("after", actualHeaderFooter.getChunks().get(2).toString());
    }

    @DisplayName("Before with Pagenumber")
    @ParameterizedTest(name = "{1}")
    @MethodSource(value = "createTestDataOnlyBefore")
    void testParagraphMethodNoAfter(Phrase before, String test) {
        HeaderFooter cut = new HeaderFooter(before, true);

        Paragraph actualHeaderFooter = cut.paragraph();

        assertEquals(2, actualHeaderFooter.getChunks().size());
        assertEquals("before", actualHeaderFooter.getChunks().get(0).toString());
        assertEquals("0", actualHeaderFooter.getChunks().get(1).toString());
    }

    @DisplayName("Before")
    @ParameterizedTest(name = "{1}")
    @MethodSource(value = "createTestDataOnlyBefore")
    void testParagraphMethodNoAfterNoPagenumber(Phrase before, String test) {
        HeaderFooter cut = new HeaderFooter(before, false);

        Paragraph actualHeaderFooter = cut.paragraph();

        assertEquals(1, actualHeaderFooter.getChunks().size());
        assertEquals("before", actualHeaderFooter.getChunks().get(0).toString());
    }

    @Test
    void testParagraphMethodNestedElements_always_doNotEndWithNewline() {
        Paragraph paragraph = new Paragraph("1 ");
        paragraph.add(new Phrase("1.1 "));
        paragraph.add(new Paragraph("1.2 "));
        Paragraph nested = new Paragraph("1.3 ");
        nested.add(new Phrase("1.3.1 "));
        nested.add(new Paragraph("1.3.2 "));
        paragraph.add(nested);
        paragraph.add(new Phrase("1.4 "));

        HeaderFooter cut = new HeaderFooter(paragraph, false);

        Paragraph actualHeaderFooter = cut.paragraph();

        assertEquals(6, actualHeaderFooter.getChunks().size());
        assertNotEquals("\n", actualHeaderFooter.getChunks().get(5).toString());
        assertEquals("1 1.1 1.2 ", actualHeaderFooter.getChunks().get(0).toString());
        assertEquals("1.4 ", actualHeaderFooter.getChunks().get(5).toString());
    }
}