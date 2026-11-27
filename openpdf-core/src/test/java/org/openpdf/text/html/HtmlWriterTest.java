package org.openpdf.text.html;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.openpdf.text.Anchor;
import org.openpdf.text.Document;
import org.openpdf.text.Header;
import org.openpdf.text.Paragraph;

class HtmlWriterTest {

    private static String write(Anchor anchor, Header header) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document();
        HtmlWriter.getInstance(document, out);
        if (header != null) {
            document.add(header);
        }
        document.open();
        Paragraph paragraph = new Paragraph("text ");
        paragraph.add(anchor);
        document.add(paragraph);
        document.close();
        return out.toString(StandardCharsets.ISO_8859_1);
    }

    @Test
    void plainReferenceIsWrittenAsItIs() {
        Anchor anchor = new Anchor("link");
        anchor.setReference("http://example.com/index.html?a=b#top");

        assertThat(write(anchor, null)).contains(" href=\"http://example.com/index.html?a=b#top\"");
    }

    @Test
    void referenceCanNotEndTheAttribute() {
        Anchor anchor = new Anchor("link");
        anchor.setReference("http://example.com/\"><script>alert(1)</script>");

        String html = write(anchor, null);

        assertThat(html).doesNotContain("<script>alert(1)</script>");
        assertThat(html).contains(
                " href=\"http://example.com/&quot;&gt;&lt;script&gt;alert(1)&lt;/script&gt;\"");
    }

    @Test
    void ampersandAndCharactersOutsideAsciiAreEscaped() {
        Anchor anchor = new Anchor("link");
        anchor.setReference("page.html?a=1&b=é");
        anchor.setName("it's");

        String html = write(anchor, null);

        assertThat(html).contains(" href=\"page.html?a=1&amp;b=&#233;\"");
        assertThat(html).contains(" name=\"it&apos;s\"");
    }

    @Test
    void stylesheetReferenceCanNotEndTheAttribute() {
        Anchor anchor = new Anchor("link");

        String html = write(anchor, new Header(HtmlTags.STYLESHEET, "style.css\"><script>alert(1)</script>"));

        assertThat(html).doesNotContain("<script>alert(1)</script>");
        assertThat(html).contains(" href=\"style.css&quot;&gt;&lt;script&gt;alert(1)&lt;/script&gt;\"");
    }
}
