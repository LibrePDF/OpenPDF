package com.lowagie.text.pdf;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class SimpleBookmarkTest {

    @Test
    void testGetBookmarkWithNoTitle() throws IOException {
        InputStream is = getClass().getResourceAsStream("/OutlineUriActionWithNoTitle.pdf");
        PdfReader reader = new PdfReader(is);
        List<?> list = SimpleBookmark.getBookmarkList(reader);
        assertNotNull(list);
        assertEquals(3, list.size());
    }

    @Test
    void testGetBookmarkListWithNoTitle() throws IOException {
        InputStream is = getClass().getResourceAsStream("/OutlineUriActionWithNoTitle.pdf");
        PdfReader reader = new PdfReader(is);
        List<Map<String, Object>> list = SimpleBookmark.getBookmarkList(reader);
        assertNotNull(list);
        assertEquals(3, list.size());
        assertEquals("ABC", list.get(0).get("Title"));
        assertEquals("", list.get(1).get("Title"));
        assertEquals("", list.get(2).get("Title"));
    }

}
