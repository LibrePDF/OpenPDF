package org.openpdf.simple.extend;

import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.openpdf.util.InputSources;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

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

    private static class Html {
        private final Document document;

        private Html(String html) throws ParserConfigurationException, IOException, SAXException {
            document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(InputSources.fromString(html));
        }
        @NonNull
        private Node find(String tagName) {
            return find(tagName, 0);
        }
        @NonNull
        private Node find(String tagName, int index) {
            return Objects.requireNonNull(document.getElementsByTagName(tagName).item(index));
        }
    }
}