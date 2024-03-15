package com.lowagie.text.html;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import com.lowagie.text.Document;
import com.lowagie.text.TextElementArray;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EmptyStackException;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * This test class contains a series of smoke tests. The goal of these tests is not validate the generated document, but
 * to ensure no exception is thrown.
 */
class HtmlParserTest {


    /**
     * This method generates a lot of common und less common combination of html tags. Each string is guaranteed to have
     * exactly one placeholder {@code %s}, so that test worthy inner tags can be inserted using
     * {@link String#format(String, Object...) String.format}.<br/> One of the generates values will be {@code %s}, so
     * every test using this generator will test the markup fragment in question without any surrounding tags as well
     *
     * @return A stream of of html tags, each containing one {@code %s}.
     */
    private static Stream<Arguments> surroundingTags() {

        String span = "<span>%s</span>";
        String ol = "<ol><li>%s</li></ol>";
        String ul = "<ul><li>%s</li></ul>";
        String div = "<div>%s</div>";
        String em = "<em>%s</em>";
        String strong = "<strong>%s</strong>";
        String table = "<table><tr><td>%s</td></tr></table>";
        String brAfter = "%s<br/>";
        String brBefore = "<br/>%s";
        String empty = "%s";

        String[] outerElements = new String[]{span, ol, ul, div, em, strong, table};
        //brAfter and brBefore must not be at top level, because it would lead to a SAXParseException (root element
        // must have an opening and a closing tag)
        String[] innerElements = new String[]{span, ol, ul, div, em, strong, table, brAfter, brBefore, empty};

        Collection<Arguments> combinations = new ArrayList<>();

        //The "naked value" should be tested first, so we begin with a simple '%s'
        combinations.add(Arguments.of(empty));

        //Combine every outer with every inner element
        //Although not every combination is common, it cannot be excluded that they may occur. In any case, no
        //exception should be thrown when parsing shitty html (for example putting a table inside a span)
        for (String outer : outerElements) {
            for (String inner : innerElements) {
                final String htmlToTest = String.format(outer, inner);
                combinations.add(Arguments.of(htmlToTest));
            }
        }

        return combinations.stream();
    }

    /**
     * Bug fix scenario: a table with a space throws a {@link TextElementArray} class cast Exception. A table without
     * spaces is parsed correctly.
     */
    @ParameterizedTest(name = "table with no space within {arguments}")
    @MethodSource("surroundingTags")
    void testParse_tableWithNoSpaces(String surroundingTags) {
        final String tableWithoutSpace = "<table><tr><td>test</td></tr></table>";

        String html = String.format(surroundingTags, tableWithoutSpace);

        testParse(html);
    }

    /**
     * Bug fix scenario: a table with a space throws a {@link TextElementArray} class cast Exception.
     */
    @ParameterizedTest(name = "table with space within {arguments}")
    @MethodSource("surroundingTags")
    void testParse_tableWithSpaces(String surroundingTags) {
        final String tableWithSpace = "<table> <tr><td>test</td></tr></table>";

        String html = String.format(surroundingTags, tableWithSpace);

        testParse(html);
    }

    /**
     * Bug fix scenario: a img within two spans throws a {@link EmptyStackException}
     */
    @ParameterizedTest(name = "img within {arguments}")
    @MethodSource("surroundingTags")
    void testParse_img(String surroundingTags) {
        String image = "<img src=\"" + ClassLoader.getSystemResource("H.gif").toExternalForm() + "\"/>";

        String html = String.format(surroundingTags, image);

        testParse(html);
    }

    @ParameterizedTest(name = "div with text within {arguments}")
    @MethodSource("surroundingTags")
    void testParse_divWithText(String surroundingTags) {
        String divWithText = "<div>text</div>";

        String html = String.format(surroundingTags, divWithText);

        testParse(html);
    }

    @ParameterizedTest(name = "empty text within {arguments}")
    @MethodSource("surroundingTags")
    void testParse_divWithoutText(String surroundingTags) {
        String divWithText = "<div></div>";

        String html = String.format(surroundingTags, divWithText);

        testParse(html);
    }

    @ParameterizedTest(name = "anchor tag within {arguments}")
    @MethodSource("surroundingTags")
    void testParse_anchor(String surroundingTags) {
        String anchor = "<a href=\"https://www.github.com\">github</a>";

        String html = String.format(surroundingTags, anchor);

        testParse(html);
    }

    @ParameterizedTest(name = "code tag within {arguments}")
    @MethodSource("surroundingTags")
    void testParse_code(String surroundingTags) {
        String code = "<code>System.out.println(\"Hello, world!\");</code>";

        String html = String.format(surroundingTags, code);

        testParse(html);
    }

    void testParse(String html) {
        try {
            Document doc1 = new Document();
            doc1.open();
            HtmlParser.parse(doc1, new StringReader(html));
            assertNotNull(doc1, () -> html + " was not parsed successfully");
        } catch (Exception e) {
            e.printStackTrace();
            fail(() -> html + " resulted in " + e);
        }
    }

}
