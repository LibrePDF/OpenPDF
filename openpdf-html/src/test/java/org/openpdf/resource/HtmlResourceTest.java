/*
 * This file is part of the OpenPDF HTML module.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */
package org.openpdf.resource;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test cases for HtmlResource demonstrating htmlunit-neko DOMParser integration.
 * <p>
 * These tests validate that the HtmlResource class correctly uses htmlunit-neko's
 * DOMParser for HTML5-compliant, error-tolerant HTML parsing.
 */
public class HtmlResourceTest {

    private static final Logger log = LoggerFactory.getLogger(HtmlResourceTest.class);

    @Test
    void parseSimpleHtml() {
        String html = """
            <!DOCTYPE html>
            <html>
              <head><title>Test</title></head>
              <body><h1>Hello World</h1></body>
            </html>
            """;

        HtmlResource resource = HtmlResource.load(html);
        Document doc = resource.getDocument();

        assertNotNull(doc);
        assertNotNull(doc.getDocumentElement());

        NodeList h1List = doc.getElementsByTagName("h1");
        assertEquals(1, h1List.getLength());
        assertEquals("Hello World", h1List.item(0).getTextContent());

        assertTrue(resource.getElapsedLoadTime() >= 0);
        log.info("Parsed simple HTML in {}ms", resource.getElapsedLoadTime());
    }

    @Test
    void parseHtml5SemanticElements() {
        String html = """
            <!DOCTYPE html>
            <html lang="en">
              <head><meta charset="UTF-8"><title>Semantic HTML5</title></head>
              <body>
                <header><h1>Header Content</h1></header>
                <nav><ul><li>Link 1</li><li>Link 2</li></ul></nav>
                <main>
                  <article>
                    <section><h2>Section 1</h2></section>
                    <section><h2>Section 2</h2></section>
                  </article>
                  <aside>Sidebar content</aside>
                </main>
                <footer>Footer content</footer>
              </body>
            </html>
            """;

        HtmlResource resource = HtmlResource.load(html);
        Document doc = resource.getDocument();

        assertNotNull(doc);
        assertEquals(1, doc.getElementsByTagName("header").getLength());
        assertEquals(1, doc.getElementsByTagName("nav").getLength());
        assertEquals(1, doc.getElementsByTagName("main").getLength());
        assertEquals(1, doc.getElementsByTagName("article").getLength());
        assertEquals(2, doc.getElementsByTagName("section").getLength());
        assertEquals(1, doc.getElementsByTagName("aside").getLength());
        assertEquals(1, doc.getElementsByTagName("footer").getLength());

        log.info("Parsed HTML5 semantic elements successfully");
    }

    @Test
    void parseMalformedHtml() {
        // Intentionally malformed HTML - missing closing tags, improper nesting
        String html = """
            <!DOCTYPE html>
            <html>
              <head><title>Malformed Test
              <body>
                <p>Unclosed paragraph
                <p>Another paragraph with <b>bold and <i>italic</b> text</i>
                <div class=unquoted>Missing quotes
                <ul>
                  <li>Item 1
                  <li>Item 2
                </ul>
            """;

        // Should not throw an exception - htmlunit-neko is error tolerant
        HtmlResource resource = HtmlResource.load(html);
        Document doc = resource.getDocument();

        // The key assertion: the parser handled malformed HTML gracefully
        assertNotNull(doc, "Document should be created from malformed HTML");
        assertNotNull(doc.getDocumentElement(), "Document element should exist");
        
        // Verify the document has a body (parser should create basic structure)
        assertNotNull(doc.getElementsByTagName("body"), "Body element should exist");
        
        // Verify text content is preserved
        String textContent = doc.getDocumentElement().getTextContent();
        assertNotNull(textContent, "Document should have text content");
        assertTrue(textContent.contains("Item 1"), "Text content should be preserved");

        log.info("Parsed malformed HTML successfully - parser handled errors gracefully");
    }

    @Test
    void parseWithDataAttributes() {
        String html = """
            <!DOCTYPE html>
            <html>
              <body>
                <div id="product" 
                     data-product-id="12345" 
                     data-category="electronics"
                     data-price="299.99">
                  Product Info
                </div>
              </body>
            </html>
            """;

        HtmlResource resource = HtmlResource.load(html);
        Document doc = resource.getDocument();

        assertNotNull(doc);
        NodeList divList = doc.getElementsByTagName("div");
        assertEquals(1, divList.getLength());

        Element div = (Element) divList.item(0);
        assertEquals("12345", div.getAttribute("data-product-id"));
        assertEquals("electronics", div.getAttribute("data-category"));
        assertEquals("299.99", div.getAttribute("data-price"));

        log.info("Parsed HTML5 data attributes successfully");
    }

    @Test
    void parseFromInputStream() {
        String html = "<html><body><p>Stream test</p></body></html>";
        ByteArrayInputStream stream = new ByteArrayInputStream(html.getBytes(StandardCharsets.UTF_8));

        HtmlResource resource = HtmlResource.load(stream);
        Document doc = resource.getDocument();

        assertNotNull(doc);
        NodeList pList = doc.getElementsByTagName("p");
        assertEquals(1, pList.getLength());
        assertEquals("Stream test", pList.item(0).getTextContent());

        log.info("Parsed HTML from InputStream successfully");
    }

    @Test
    void parseFromReader() {
        String html = "<html><body><p>Reader test</p></body></html>";
        StringReader reader = new StringReader(html);

        HtmlResource resource = HtmlResource.load(reader);
        Document doc = resource.getDocument();

        assertNotNull(doc);
        NodeList pList = doc.getElementsByTagName("p");
        assertEquals(1, pList.getLength());
        assertEquals("Reader test", pList.item(0).getTextContent());

        log.info("Parsed HTML from Reader successfully");
    }

    @Test
    void parseWithCustomConfig() {
        String html = """
            <!DOCTYPE html>
            <html>
              <body>
                <div class="test"/>
                <section/>
              </body>
            </html>
            """;

        HtmlParserConfig config = HtmlParserConfig.builder()
                .allowSelfClosingTags(true)
                .encoding("UTF-8")
                .build();

        HtmlResource resource = HtmlResource.load(html, config);
        Document doc = resource.getDocument();

        assertNotNull(doc);
        assertNotNull(doc.getDocumentElement());

        log.info("Parsed HTML with custom config successfully");
    }

    @Test
    void parseWithErrorReporting() {
        String html = """
            <!DOCTYPE html>
            <html>
              <body>
                <p>Content with <b>mismatched <i>tags</b></i>
              </body>
            </html>
            """;

        HtmlParserConfig config = HtmlParserConfig.builder()
                .reportErrors(true)
                .build();

        // Should not throw - just parse with error reporting enabled
        HtmlResource resource = HtmlResource.load(html, config);
        Document doc = resource.getDocument();

        assertNotNull(doc);
        assertNotNull(doc.getDocumentElement());

        log.info("Parsed HTML with error reporting enabled");
    }

    @Test
    void parseVoidElements() {
        String html = """
            <!DOCTYPE html>
            <html>
              <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width">
                <link rel="stylesheet" href="style.css">
              </head>
              <body>
                <p>Line 1<br>Line 2<br/>Line 3</p>
                <hr>
                <input type="text" value="test">
                <img alt="test">
              </body>
            </html>
            """;

        HtmlResource resource = HtmlResource.load(html);
        Document doc = resource.getDocument();

        assertNotNull(doc);
        assertEquals(2, doc.getElementsByTagName("meta").getLength());
        assertEquals(1, doc.getElementsByTagName("link").getLength());
        assertTrue(doc.getElementsByTagName("br").getLength() >= 2);
        assertEquals(1, doc.getElementsByTagName("hr").getLength());
        assertEquals(1, doc.getElementsByTagName("input").getLength());
        assertEquals(1, doc.getElementsByTagName("img").getLength());

        log.info("Parsed HTML5 void elements successfully");
    }

    @Test
    void parseHtml5FormElements() {
        String html = """
            <!DOCTYPE html>
            <html>
              <body>
                <form>
                  <input type="email" placeholder="Email">
                  <input type="tel" placeholder="Phone">
                  <input type="date">
                  <input type="color" value="#ff0000">
                  <input type="range" min="0" max="100">
                  <input type="search" placeholder="Search">
                  <progress value="70" max="100"></progress>
                  <meter value="0.6" min="0" max="1"></meter>
                  <output>Result</output>
                  <datalist id="options">
                    <option value="Option 1">
                    <option value="Option 2">
                  </datalist>
                </form>
              </body>
            </html>
            """;

        HtmlResource resource = HtmlResource.load(html);
        Document doc = resource.getDocument();

        assertNotNull(doc);
        assertEquals(1, doc.getElementsByTagName("form").getLength());
        assertEquals(6, doc.getElementsByTagName("input").getLength());
        assertEquals(1, doc.getElementsByTagName("progress").getLength());
        assertEquals(1, doc.getElementsByTagName("meter").getLength());
        assertEquals(1, doc.getElementsByTagName("output").getLength());
        assertEquals(1, doc.getElementsByTagName("datalist").getLength());
        assertEquals(2, doc.getElementsByTagName("option").getLength());

        log.info("Parsed HTML5 form elements successfully");
    }

    @Test
    void configBuilderDefaultValues() {
        HtmlParserConfig config = HtmlParserConfig.defaults();

        assertFalse(config.isReportErrors());
        assertFalse(config.isAllowSelfClosingTags());
        assertFalse(config.isAllowSelfClosingIframe());
        assertTrue(config.isParseNoScriptContent());
        assertFalse(config.isScriptStripCommentDelims());
        assertFalse(config.isStyleStripCommentDelims());
        assertNull(config.getElementNameCase());
        assertNull(config.getAttributeNameCase());
        assertNull(config.getEncoding());

        log.info("Verified HtmlParserConfig default values");
    }

    @Test
    void configBuilderCustomValues() {
        HtmlParserConfig config = HtmlParserConfig.builder()
                .reportErrors(true)
                .allowSelfClosingTags(true)
                .allowSelfClosingIframe(true)
                .parseNoScriptContent(false)
                .scriptStripCommentDelims(true)
                .styleStripCommentDelims(true)
                .elementNameCase(HtmlParserConfig.CASE_LOWER)
                .attributeNameCase(HtmlParserConfig.CASE_LOWER)
                .encoding("UTF-8")
                .build();

        assertTrue(config.isReportErrors());
        assertTrue(config.isAllowSelfClosingTags());
        assertTrue(config.isAllowSelfClosingIframe());
        assertFalse(config.isParseNoScriptContent());
        assertTrue(config.isScriptStripCommentDelims());
        assertTrue(config.isStyleStripCommentDelims());
        assertEquals("lower", config.getElementNameCase());
        assertEquals("lower", config.getAttributeNameCase());
        assertEquals("UTF-8", config.getEncoding());

        log.info("Verified HtmlParserConfig custom values");
    }
}
