package org.openpdf.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.w3c.dom.Document;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

class XMLUtilTest {
    private static final String MARKER = "content-of-a-local-file";
    private static final String XHTML_DOCTYPE = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\""
            + " \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">";

    @TempDir
    Path tempDir;

    @Test
    void externalGeneralEntityIsNotRead() throws Exception {
        String xml = "<!DOCTYPE html [<!ENTITY x SYSTEM \"" + localFile("entity.txt") + "\">]>"
                + "<html><body>before &x; after</body></html>";

        assertThat(textOf(XMLUtil.documentFromString(xml))).doesNotContain(MARKER).contains("before", "after");
        assertThat(textOf(XMLUtil.documentFromStream(stream(xml)))).doesNotContain(MARKER);
    }

    @Test
    void externalParameterEntityIsNotRead() throws Exception {
        String declaration = "<!ATTLIST body title CDATA \"" + MARKER + "\">";
        String xml = "<!DOCTYPE html [<!ENTITY % p SYSTEM \"" + localFile("entities.ent", declaration) + "\"> %p;]>"
                + "<html><body>text</body></html>";

        Document document = XMLUtil.documentFromString(xml);

        assertThat(document.getElementsByTagName("body").item(0).getAttributes().getLength()).isZero();
        assertThat(textOf(document)).isEqualTo("text");
    }

    @Test
    void externalDtdIsNotRead() throws Exception {
        String declaration = "<!ENTITY x \"" + MARKER + "\">";
        String xml = "<!DOCTYPE html SYSTEM \"" + localFile("document.dtd", declaration) + "\">"
                + "<html><body>before &x; after</body></html>";

        assertThat(textOf(XMLUtil.documentFromString(xml))).doesNotContain(MARKER);
    }

    @Test
    void xhtmlDocumentWithDoctypeIsParsedWithItsEntities() throws Exception {
        String xml = XHTML_DOCTYPE + "<html xmlns=\"http://www.w3.org/1999/xhtml\">"
                + "<head><title>t</title></head><body><p>a&nbsp;b&eacute;</p></body></html>";

        Document document = XMLUtil.documentFromStream(stream(xml));

        assertThat(document.getDocumentElement().getTagName()).isEqualTo("html");
        assertThat(document.getElementsByTagName("p").item(0).getTextContent()).isEqualTo("a bé");
    }

    @Test
    void fileIsParsed() throws Exception {
        Path file = tempDir.resolve("document.xhtml");
        Files.writeString(file, XHTML_DOCTYPE + "<html><body><p>hello</p></body></html>");

        Document document = XMLUtil.documentFromFile(file.toString());

        assertThat(textOf(document)).isEqualTo("hello");
    }

    private String localFile(String name) throws IOException {
        return localFile(name, MARKER);
    }

    private String localFile(String name, String content) throws IOException {
        Path file = tempDir.resolve(name);
        Files.writeString(file, content);
        return file.toUri().toString();
    }

    private static ByteArrayInputStream stream(String xml) {
        return new ByteArrayInputStream(xml.getBytes(UTF_8));
    }

    private static String textOf(Document document) {
        return document.getDocumentElement().getTextContent();
    }
}
