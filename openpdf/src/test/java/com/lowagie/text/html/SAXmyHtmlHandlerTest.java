package com.lowagie.text.html;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import com.lowagie.text.Document;
import java.io.InputStream;
import org.junit.jupiter.api.Test;

/**
 * This test class contains a series of smoke tests. The goal of these tests is not validate the generated document, but
 * to ensure no exception is thrown.
 */
public class SAXmyHtmlHandlerTest {

    /**
     * Test scenario: a html file with a title will not generate.
     */
    @Test
    void testTitle_generate() {
        InputStream is = SAXmyHtmlHandlerTest.class.getClassLoader().getResourceAsStream("parseTitle.html");
        parseHtml(is);
    }


    /**
     * Test scenario: a html file with a table will generate like html webpage.
     */
    @Test
    void testTable_generate() {
        InputStream is = SAXmyHtmlHandlerTest.class.getClassLoader().getResourceAsStream("parseTable.html");
        parseHtml(is);
    }

    /**
     * Parse the input HTML file to PDF file.
     *
     * @param is The input stream of the HTML file.
     */
    void parseHtml(InputStream is) {
        try {
            Document doc1 = new Document();
            doc1.open();
            HtmlParser.parse(doc1, is);
            assertNotNull(doc1, () -> is + " was not parsed successfully");
        } catch (Exception e) {
            fail(() -> is + " resulted in " + e);
        }
    }
}
