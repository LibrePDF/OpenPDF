package com.lowagie.text.xml.simpleparser;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SimpleXMLParserTest {

    static final String bom = "\uFEFF";
    static final String content = "\u79C1";
    static final String xmlRaw = "<?xml version='1.0'?><a>" + content + "</a>";
    static final String xmlBOM = bom + xmlRaw;

    @Test
    void testParse() throws IOException {
        testCharset(xmlRaw, StandardCharsets.UTF_8);
        testCharset(xmlBOM, StandardCharsets.UTF_8);
        testCharset(xmlBOM, StandardCharsets.UTF_16BE);
        testCharset(xmlBOM, StandardCharsets.UTF_16LE);
        testCharset(xmlBOM, Charset.forName("UTF-32BE"));
        testCharset(xmlBOM, Charset.forName("UTF-32LE"));
    }

    static void testCharset(String xml, Charset charset) throws IOException {
        try (
                TestHandler h = new TestHandler(charset);
                ByteArrayInputStream is = new ByteArrayInputStream(xml.getBytes(charset))
        ) {
            SimpleXMLParser.parse(h, is);
        }
    }

    static class TestHandler implements SimpleXMLDocHandler, AutoCloseable {

        volatile boolean called = false;
        final String charset;

        TestHandler(Charset charset) {
            this.charset = charset.displayName();
        }

        @Override
        public void startElement(String tag, HashMap h) {}

        @Override
        public void startElement(String tag, Map<String, String> h) {}

        @Override
        public void endElement(String tag) {}

        @Override
        public void startDocument() {}

        @Override
        public void endDocument() {}

        @Override
        public void text(String str) {
            Assertions.assertEquals(content, str, "text content in " + charset);
            called = true;
        }

        @Override
        public void close() {
            Assertions.assertTrue(called, "was not called");
        }
    }

}
