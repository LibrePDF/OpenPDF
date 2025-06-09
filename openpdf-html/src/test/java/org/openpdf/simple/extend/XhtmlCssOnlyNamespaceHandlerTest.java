package org.openpdf.simple.extend;

import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.openpdf.css.sheet.StylesheetInfo;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.openpdf.css.sheet.StylesheetInfo.Origin.AUTHOR;
import static org.openpdf.css.sheet.StylesheetInfo.Origin.USER_AGENT;

class XhtmlCssOnlyNamespaceHandlerTest {
    private final XhtmlCssOnlyNamespaceHandler handler = new XhtmlCssOnlyNamespaceHandler();

    @Test
    void readsAllCssStyles() throws IOException, ParserConfigurationException, SAXException {
        List<StylesheetInfo> stylesheets = handler.getStylesheets(read("/hello.css.html"));
        assertThat(stylesheets).hasSize(2);
        assertThat(stylesheets.get(0).getUri()).matches("inline:\\d+");
        assertThat(stylesheets.get(0).getMedia()).containsExactly("all");
        assertThat(stylesheets.get(0).getOrigin()).isEqualTo(AUTHOR);
        assertThat(stylesheets.get(0).getContent()).contains("body {color: black;}");
        assertThat(stylesheets.get(1).getContent()).contains("h1 {color: red;}");
    }

    @Test
    void fileWithoutCssStyles() throws IOException, ParserConfigurationException, SAXException {
        List<StylesheetInfo> stylesheets = handler.getStylesheets(read("/hello.html"));
        assertThat(stylesheets).isEmpty();
    }

    @Test
    void readsDefaultCssStylesheetFromFile() {
        StylesheetInfo css = handler.getDefaultStylesheet().get();
        assertThat(css.getUri()).endsWith("/css/XhtmlNamespaceHandler.css");
        assertThat(css.getOrigin()).isEqualTo(USER_AGENT);
        assertThat(css.getMedia()).containsExactly("all");
    }

    @Test
    void isInteger() {
        assertThat(handler.isInteger("")).isFalse();
        assertThat(handler.isInteger("_")).isFalse();
        assertThat(handler.isInteger("0")).isTrue();
        assertThat(handler.isInteger("01234")).isTrue();
        assertThat(handler.isInteger("123a")).isFalse();
        assertThat(handler.isInteger("a234b")).isFalse();
    }

    private Document read(String name) throws IOException, SAXException, ParserConfigurationException {
        URL url = requireNonNull(getClass().getResource(name), () -> "test resource not found: " + name);
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        return builder.parse(url.toString());
    }
}