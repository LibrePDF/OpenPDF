package com.lowagie.text.html;

import com.lowagie.text.Document;
import com.lowagie.text.html.simpleparser.HTMLWorker;
import org.junit.jupiter.api.Test;
import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

public class HTMLTableTest {
    /**
     * Bug fix scenario: a table with rowspan doesn't work
     */
    @Test
    void testRolspan() {
        String code = "<td rowspan=\"4\">line 1</td>";
        String html = String.format(code);
        testParse(html);
    }

    /**
     * Bug fix scenario: a table with colspan doesn't work
     */
    @Test
    void testColspan() {
        String code = "<td colspan=\"2\">line 1</td>";
        String html = String.format(code);
        testParse(html);
    }

    /**
     * parse an html string to convert to pdf
     *
     * @param html input html string for conversion
     */
    void testParse(String html) {
        try {
            Document doc = new Document();
            doc.open();
            HTMLWorker worker = new HTMLWorker(doc);
            worker.parse(new StringReader(html));
            assertNotNull(doc, () -> html + " was not parsed successfully");
        } catch (Exception e) {
            e.printStackTrace();
            fail(() -> html + " resulted in " + e);
        }
    }
}