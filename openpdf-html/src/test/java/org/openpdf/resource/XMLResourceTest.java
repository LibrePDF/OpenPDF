package org.openpdf.resource;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.xml.sax.InputSource;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class XMLResourceTest {
    private static final String MARKER = "content-of-a-local-file";

    @TempDir
    Path tempDir;

    @Test
    void defaultReaderDoesNotReadExternalGeneralEntity() throws Exception {
        Path file = tempDir.resolve("entity.txt");
        Files.writeString(file, MARKER);
        String xml = "<!DOCTYPE html [<!ENTITY x SYSTEM \"" + file.toUri() + "\">]>"
                + "<html><body>before &x; after</body></html>";

        assertThatThrownBy(() -> parseWithDefaultReader(xml)).isInstanceOf(SAXParseException.class);
    }

    @Test
    void defaultReaderDoesNotReadExternalDtd() throws Exception {
        Path file = tempDir.resolve("document.dtd");
        Files.writeString(file, "<!ENTITY x \"" + MARKER + "\">");
        String xml = "<!DOCTYPE html SYSTEM \"" + file.toUri() + "\">"
                + "<html><body>before &x; after</body></html>";

        assertThatThrownBy(() -> parseWithDefaultReader(xml)).isInstanceOf(SAXParseException.class);
    }

    @Test
    void defaultReaderRefusesADocumentTypeDeclaration() {
        String xml = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\""
                + " \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">"
                + "<html><body>text</body></html>";

        assertThatThrownBy(() -> parseWithDefaultReader(xml)).isInstanceOf(SAXParseException.class);
    }

    @Test
    void defaultReaderParsesADocumentWithoutADocumentTypeDeclaration() throws Exception {
        assertThat(parseWithDefaultReader("<html><body>a &amp; b</body></html>")).isEqualTo("a & b");
    }

    private static String parseWithDefaultReader(String xml) throws Exception {
        StringBuilder text = new StringBuilder();
        XMLReader reader = XMLResource.newDefaultXMLReader();
        reader.setEntityResolver(FSEntityResolver.instance());
        reader.setContentHandler(new DefaultHandler() {
            @Override
            public void characters(char[] ch, int start, int length) {
                text.append(ch, start, length);
            }
        });
        reader.parse(new InputSource(new StringReader(xml)));
        return text.toString();
    }
}
