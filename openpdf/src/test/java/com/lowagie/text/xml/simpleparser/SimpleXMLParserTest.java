package com.lowagie.text.xml.simpleparser;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SimpleXMLParserTest {

    static final String bom = "\uFEFF";
    static final String euro = "\u20AC";
    static final String xmlRaw = "<?xml version='1.0'?><a>" + euro + "</a>";
    static final String xmlBOM = bom + xmlRaw;
    static final String xmlI15 = "<?xml version='1.0' encoding='ISO-8859-15'?><a>" + euro + "</a>";

    static void testCharset(String xml, Charset charset) throws IOException {
        try (
                TestHandler h = new TestHandler(charset);
                ByteArrayInputStream is = new ByteArrayInputStream(xml.getBytes(charset))
        ) {
            SimpleXMLParser.parse(h, is);
        }
    }

    @Test
    void testDetectUnicode() throws IOException {
        testCharset(xmlRaw, StandardCharsets.UTF_8);
        testCharset(xmlBOM, StandardCharsets.UTF_8);
        testCharset(xmlBOM, StandardCharsets.UTF_16BE);
        testCharset(xmlBOM, StandardCharsets.UTF_16LE);
        testCharset(xmlBOM, Charset.forName("UTF-32BE"));
        testCharset(xmlBOM, Charset.forName("UTF-32LE"));
        testCharset(xmlI15, Charset.forName("ISO-8859-15"));
    }

    @Test
    void testDetectedOverDeclared() throws IOException {
        String xml = bom + xmlI15;
        testCharset(xml, StandardCharsets.UTF_8);
        testCharset(xml, StandardCharsets.UTF_16BE);
        testCharset(xml, StandardCharsets.UTF_16LE);
        testCharset(xml, Charset.forName("UTF-32BE"));
        testCharset(xml, Charset.forName("UTF-32LE"));
        testCharset(xml, Charset.forName("ISO-8859-15"));
    }

    static class TestHandler implements SimpleXMLDocHandler, AutoCloseable {

        final String charset;
        volatile boolean called = false;

        TestHandler(Charset charset) {
            this.charset = charset.displayName();
        }

        @Override
        public void startElement(String tag, Map<String, String> h) {
        }

        @Override
        public void endElement(String tag) {
        }

        @Override
        public void startDocument() {
        }

        @Override
        public void endDocument() {
        }

        @Override
        public void text(String str) {
            Assertions.assertEquals(euro, str, "text content in " + charset);
            called = true;
        }

        @Override
        public void close() {
            Assertions.assertTrue(called, "was not called");
        }
    }

}
