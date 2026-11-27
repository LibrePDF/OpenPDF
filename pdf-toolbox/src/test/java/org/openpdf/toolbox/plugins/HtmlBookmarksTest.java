package org.openpdf.toolbox.plugins;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.openpdf.text.Document;
import org.openpdf.text.html.HtmlWriter;

class HtmlBookmarksTest {

    private static Map<String, Object> bookmark(String action, String key, String value) {
        Map<String, Object> bookmark = new HashMap<>();
        bookmark.put("Title", "Title");
        bookmark.put("Action", action);
        bookmark.put(key, value);
        return bookmark;
    }

    private static String html(Map<String, Object> bookmark) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document();
        HtmlWriter.getInstance(document, out);
        document.open();
        document.add(HtmlBookmarks.createBookmark("file.pdf", null, bookmark));
        document.close();
        return out.toString(StandardCharsets.ISO_8859_1);
    }

    @Test
    void linksWithoutSchemeAndWebLinksAreKept() {
        assertThat(HtmlBookmarks.safeReference("http://example.com/a:b")).isEqualTo("http://example.com/a:b");
        assertThat(HtmlBookmarks.safeReference("HTTPS://example.com/")).isEqualTo("HTTPS://example.com/");
        assertThat(HtmlBookmarks.safeReference("file.pdf#page=3")).isEqualTo("file.pdf#page=3");
        assertThat(HtmlBookmarks.safeReference("../docs/other.pdf#nameddest=a:b")).isEqualTo(
                "../docs/other.pdf#nameddest=a:b");
        assertThat(HtmlBookmarks.safeReference("/docs/a:b.pdf")).isEqualTo("/docs/a:b.pdf");
        assertThat(HtmlBookmarks.safeReference("other.pdf?version=1:2")).isEqualTo("other.pdf?version=1:2");
    }

    @Test
    void linksWithAnotherSchemeAreDropped() {
        assertThat(HtmlBookmarks.safeReference(null)).isNull();
        assertThat(HtmlBookmarks.safeReference("javascript:alert(1)")).isNull();
        assertThat(HtmlBookmarks.safeReference(" JavaScript:alert(1)")).isNull();
        assertThat(HtmlBookmarks.safeReference("java\tscript:alert(1)")).isNull();
        assertThat(HtmlBookmarks.safeReference("\u0001javascript:alert(1)")).isNull();
        assertThat(HtmlBookmarks.safeReference("data:text/html,<script>alert(1)</script>")).isNull();
        assertThat(HtmlBookmarks.safeReference("file:///etc/passwd")).isNull();
        assertThat(HtmlBookmarks.safeReference("vbscript:msgbox(1)#page=1")).isNull();
    }

    @Test
    void uriCanNotEndTheAttribute() {
        String html = html(bookmark("URI", "URI", "http://example.com/\"><script>alert(1)</script>"));

        assertThat(html).doesNotContain("<script>alert(1)</script>");
        assertThat(html).contains(" href=\"http://example.com/&quot;&gt;&lt;script&gt;alert(1)&lt;/script&gt;\"");
    }

    @Test
    void uriWithoutSchemeCanNotEndTheAttribute() {
        String html = html(bookmark("URI", "URI", "\"><script>alert(1)</script>"));

        assertThat(html).doesNotContain("<script>alert(1)</script>");
        assertThat(html).contains(" href=\"&quot;&gt;&lt;script&gt;alert(1)&lt;/script&gt;\"");
    }

    @Test
    void scriptUriIsNotUsedAsLink() {
        String html = html(bookmark("URI", "URI", "javascript:alert(1)"));

        assertThat(html).doesNotContain("javascript:").doesNotContain("href=");
        assertThat(html).contains("Goto URL");
    }

    @Test
    void remoteFileCanNotEndTheAttribute() {
        Map<String, Object> bookmark = bookmark("GoToR", "File", "other.pdf\"><script>alert(1)</script>");
        bookmark.put("Named", "dest\"><script>alert(2)</script>");

        String html = html(bookmark);

        assertThat(html).doesNotContain("\"><script>");
        assertThat(html).contains(" href=\"other.pdf&quot;&gt;&lt;script&gt;alert(1)&lt;/script&gt;#nameddest=");
    }

    @Test
    void pageOfTheSameFileIsLinked() {
        String html = html(bookmark("GoTo", "Page", "3 Fit"));

        assertThat(html).contains(" href=\"file.pdf#page=3\"");
    }
}
