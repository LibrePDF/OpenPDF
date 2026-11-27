package org.openpdf.simple.extend;

import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.openpdf.css.parser.CSSParser;
import org.openpdf.css.sheet.PropertyDeclaration;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.openpdf.util.InputSources;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.openpdf.css.sheet.StylesheetInfo.Origin.AUTHOR;

class XhtmlNamespaceHandlerTest {
    private final XhtmlNamespaceHandler handler = new XhtmlNamespaceHandler();

    @Test
    void looksLikeAMangledColor_otherThan6Chars() {
        assertThat(handler.looksLikeAMangledColor("")).isFalse();
        assertThat(handler.looksLikeAMangledColor("12345")).isFalse();
        assertThat(handler.looksLikeAMangledColor("1234567")).isFalse();
    }

    @ParameterizedTest
    @CsvSource({
        "123456, true, 6 digits",
        "012345, true, 6 digits",
        "987654, true, 6 digits",
        "abcdef, true, letters a..f",
        "abcdeg, false, letter g",
        "1bdd3f, true, letters a..f and digits",
        "_12345, false, character _",
    })
    void looksLikeAMangledColor(String input, boolean expectedResult, String explanation) {
        assertThat(handler.looksLikeAMangledColor(input))
            .as(() -> "looksLikeAMangledColor('%s') should be %s: %s".formatted(input, expectedResult, explanation))
            .isEqualTo(expectedResult);
    }

    @Test
    void findsTable() throws ParserConfigurationException, IOException, SAXException {
        Html html = new Html("""
            <html>
                <head>
                    <title>Hello</title>
                </head>
                <body>
                <table id="table1">
                    <tr>
                        <td id="td1">111</td>
                        <td id="td2">222</td>
                    </tr>
                    <tbody>
                        <tr>
                            <td id="td3">333</td>
                            <td id="td4">444</td>
                        </tr>
                    </tbody>
                </table>
                </body>
            </html>
            """);
        var table = html.find("table");
        assertThat(handler.findTable(html.find("td", 0))).isEqualTo(table);
        assertThat(handler.findTable(html.find("td", 1))).isEqualTo(table);
        assertThat(handler.findTable(html.find("td", 2))).isEqualTo(table);
        assertThat(handler.findTable(html.find("td", 3))).isEqualTo(table);
        assertThat(handler.ancestor(html.find("title"), "head", 1000)).isEqualTo(html.find("head"));
        assertThat(handler.ancestor(html.find("td", 0), "tbody", 1000)).isNull();
        assertThat(handler.ancestor(html.find("td", 3), "tbody", 1000)).isEqualTo(html.find("tbody"));
    }

    @Test
    void presentationalAttributesBecomeCss() throws ParserConfigurationException, IOException, SAXException {
        Html html = new Html("""
            <table width="100%" border="1" cellspacing="2" cellpadding="3" bgcolor="FFEE00" background="bg.png">
                <tr align="center" valign="middle">
                    <td width="50" height="2.5em" align="RIGHT" valign="top" bgcolor="red" colspan="2">1</td>
                    <td width="auto" bgcolor="#abc" background="images/cell.png">2</td>
                </tr>
            </table>
            """);

        assertThat(handler.getNonCssStyling(html.element("table"))).isEqualTo("width: 100%;border: 1px inset black;"
                + "border-collapse: separate; border-spacing: 2px;background-color: #ffee00;"
                + "background-image: url(\"bg.png\");");
        assertThat(handler.getNonCssStyling(html.element("tr")))
                .isEqualTo("text-align: center;vertical-align: middle;");
        assertThat(handler.getNonCssStyling(html.element("td", 0))).isEqualTo("padding: 3px;border: 1px outset black;"
                + "width: 50px;height: 2.5em;text-align: right;vertical-align: top;background-color: red;");
        assertThat(handler.getNonCssStyling(html.element("td", 1))).isEqualTo("padding: 3px;border: 1px outset black;"
                + "width: auto;background-color: #abc;background-image: url(\"images/cell.png\");");
        assertThat(handler.getElementStyling(html.element("td", 0)))
                .isEqualTo("-fs-table-cell-colspan: 2;");
    }

    @ParameterizedTest
    @ValueSource(strings = {"width", "height", "bgcolor", "align", "valign", "colspan", "rowspan"})
    void cellAttributeCannotAddDeclarations(String attribute) throws Exception {
        Html html = new Html("<table><tr><td %s=\"1px;background-image:url(x)\">1</td></tr></table>"
                .formatted(attribute));

        assertThat(handler.getNonCssStyling(html.element("td"))).isEmpty();
        assertThat(handler.getElementStyling(html.element("td"))).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"width", "border", "cellspacing", "cellpadding", "bgcolor"})
    void tableAttributeCannotAddDeclarations(String attribute) throws Exception {
        Html html = new Html("<table %s=\"1px;background-image:url(x)\"><tr><td>1</td></tr></table>"
                .formatted(attribute));

        assertThat(handler.getNonCssStyling(html.element("table"))).doesNotContain("background-image");
        assertThat(handler.getNonCssStyling(html.element("td"))).doesNotContain("background-image");
    }

    @Test
    void columnAndImageAttributesCannotAddDeclarations() throws Exception {
        Html html = new Html("""
            <table><col span="2;color:red" width="1px;color:red"/><col span="3" width="20"/>
            <tr><td><img width="1px;color:red" height="1px;color:red"/><img width="10" height="20%"/></td></tr>
            </table>
            """);

        assertThat(handler.getElementStyling(html.element("col", 0))).isEmpty();
        assertThat(handler.getElementStyling(html.element("col", 1)))
                .isEqualTo("-fs-table-cell-colspan: 3;width: 20px;");
        assertThat(handler.getElementStyling(html.element("img", 0))).isEmpty();
        assertThat(handler.getElementStyling(html.element("img", 1))).isEqualTo("width: 10px;height: 20%;");
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "bg.png",
        "http://example.com/a b.png?x=1&y=(2)",
        "x.png\");color:red;background-image:url(\"y.png",
        "x.png);color:red;background-image:url(y.png",
        "a\b'c.png",
    })
    void backgroundAttributeIsOneUrl(String url) throws Exception {
        Html html = new Html("<table><tr><td>1</td></tr></table>");
        Element cell = html.element("td");
        cell.setAttribute("background", url);

        List<PropertyDeclaration> declarations = new CSSParser((uri, message) -> { })
                .parseDeclaration(AUTHOR, handler.getNonCssStyling(cell)).getPropertyDeclarations();

        assertThat(declarations).hasSize(1);
        assertThat(declarations.get(0).getPropertyName()).isEqualTo("background-image");
        assertThat(declarations.get(0).getValue().getStringValue()).isEqualTo(url);
    }

    private static class Html {
        private final Document document;

        private Html(String html) throws ParserConfigurationException, IOException, SAXException {
            document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(InputSources.fromString(html));
        }

        @NonNull
        private Node find(String tagName) {
            return find(tagName, 0);
        }

        private Element element(String tagName) {
            return element(tagName, 0);
        }

        private Element element(String tagName, int index) {
            return (Element) find(tagName, index);
        }

        @NonNull
        private Node find(String tagName, int index) {
            return Objects.requireNonNull(document.getElementsByTagName(tagName).item(index));
        }
    }
}