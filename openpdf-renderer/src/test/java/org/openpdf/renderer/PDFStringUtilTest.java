package org.openpdf.renderer;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for PDFStringUtil class.
 */
class PDFStringUtilTest {

    @Test
    void testAsTextString_WithNullInput_ReturnsNull() {
        // when
        String result = PDFStringUtil.asTextString(null);
        
        // then
        assertThat(result).isNull();
    }

    @Test
    void testAsTextString_WithEmptyString_ReturnsEmpty() {
        // when
        String result = PDFStringUtil.asTextString("");
        
        // then
        assertThat(result).isEmpty();
    }

    @Test
    void testAsTextString_WithSimpleAscii_ReturnsSameString() {
        // given
        String input = "Hello World";
        
        // when
        String result = PDFStringUtil.asTextString(input);
        
        // then
        assertThat(result).isEqualTo("Hello World");
    }

    @Test
    void testAsTextString_WithUTF16BEBOM_DecodesAsUTF16BE() {
        // given - UTF-16BE BOM (0xFE 0xFF) followed by "Hi" in UTF-16BE
        // BOM = 0xFE 0xFF, H = 0x00 0x48, i = 0x00 0x69
        // As a basic string: char(0xFE), char(0xFF), char(0x00), char(0x48), char(0x00), char(0x69)
        String input = "\u00FE\u00FF\u0000\u0048\u0000\u0069";
        
        // when
        String result = PDFStringUtil.asTextString(input);
        
        // then
        assertThat(result).isEqualTo("Hi");
    }

    @Test
    void testAsTextString_WithoutBOM_DecodesAsPDFDocEncoded() {
        // given - simple ASCII string
        String input = "Test";
        
        // when
        String result = PDFStringUtil.asTextString(input);
        
        // then
        assertThat(result).isEqualTo("Test");
    }

    @Test
    void testAsPDFDocEncoded_WithSimpleAscii() {
        // given
        String input = "Hello";
        
        // when
        String result = PDFStringUtil.asPDFDocEncoded(input);
        
        // then
        assertThat(result).isEqualTo("Hello");
    }

    @Test
    void testAsPDFDocEncoded_WithExtendedCharacters() {
        // given - using a character from the extended PDFDocEncoding range (0x80-0xFF)
        // Character 0x80 maps to bullet (U+2022)
        String input = "\u0080";
        
        // when
        String result = PDFStringUtil.asPDFDocEncoded(input);
        
        // then
        assertThat(result).isEqualTo("\u2022"); // bullet character
    }

    @Test
    void testAsBytes_WithSimpleString() {
        // given
        String input = "ABC";
        
        // when
        byte[] result = PDFStringUtil.asBytes(input);
        
        // then
        assertThat(result).containsExactly(65, 66, 67); // ASCII codes for A, B, C
    }

    @Test
    void testAsBytes_WithEmptyString() {
        // given
        String input = "";
        
        // when
        byte[] result = PDFStringUtil.asBytes(input);
        
        // then
        assertThat(result).isEmpty();
    }

    @Test
    void testAsBasicString_FromBytes() {
        // given
        byte[] bytes = {65, 66, 67}; // A, B, C
        
        // when
        String result = PDFStringUtil.asBasicString(bytes);
        
        // then
        assertThat(result).isEqualTo("ABC");
    }

    @Test
    void testAsBasicString_FromBytesWithOffsetAndLength() {
        // given
        byte[] bytes = {65, 66, 67, 68, 69}; // A, B, C, D, E
        
        // when
        String result = PDFStringUtil.asBasicString(bytes, 1, 3); // B, C, D
        
        // then
        assertThat(result).isEqualTo("BCD");
    }

    @Test
    void testAsBasicString_FromEmptyBytes() {
        // given
        byte[] bytes = {};
        
        // when
        String result = PDFStringUtil.asBasicString(bytes);
        
        // then
        assertThat(result).isEmpty();
    }

    @Test
    void testAsBytesAndAsBasicString_RoundTrip() {
        // given
        String original = "Test String 123";
        
        // when
        byte[] bytes = PDFStringUtil.asBytes(original);
        String result = PDFStringUtil.asBasicString(bytes);
        
        // then
        assertThat(result).isEqualTo(original);
    }

    @Test
    void testAsUTF16BEEncoded_WithBOM() {
        // given - UTF-16BE BOM followed by "Hi" encoded in UTF-16BE
        // BOM = 0xFE 0xFF, H = 0x00 0x48, i = 0x00 0x69
        // As a basic string: char(0xFE), char(0xFF), char(0x00), char(0x48), char(0x00), char(0x69)
        String input = "\u00FE\u00FF\u0000\u0048\u0000\u0069";
        
        // when
        String result = PDFStringUtil.asUTF16BEEncoded(input);
        
        // then
        assertThat(result).isEqualTo("Hi");
    }

    @Test
    void testPDFDocEncodingMap_FirstByte() {
        // Test that first byte (0x00) maps correctly
        String input = "\u0000";
        String result = PDFStringUtil.asPDFDocEncoded(input);
        assertThat(result).isEqualTo("\u0000");
    }

    @Test
    void testPDFDocEncodingMap_LastByte() {
        // Test that last byte (0xFF) maps correctly
        String input = "\u00FF";
        String result = PDFStringUtil.asPDFDocEncoded(input);
        assertThat(result).isEqualTo("\u00FF");
    }
}
