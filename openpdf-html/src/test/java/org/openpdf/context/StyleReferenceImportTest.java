package org.openpdf.context;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.openpdf.css.sheet.StylesheetInfo;
import org.openpdf.pdf.ITextRenderer;
import org.openpdf.pdf.ITextUserAgent;
import org.openpdf.resource.CSSResource;
import org.openpdf.resource.XMLResource;
import org.openpdf.simple.extend.XhtmlNamespaceHandler;
import org.w3c.dom.Element;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

@Timeout(60)
class StyleReferenceImportTest {
    private static final String DEFAULT_STYLESHEET = "/css/XhtmlNamespaceHandler.css";
    private static final String RED = "#ff0000";
    private static final String BLUE = "#0000ff";
    private static final String LINKED = "<html><head><link rel=\"stylesheet\" href=\"%s\"/></head>"
            + "<body><p>text</p></body></html>";

    private final Map<String, String> stylesheets = new HashMap<>();
    private final List<String> requested = new ArrayList<>();

    @Test
    void stylesheetThatImportsItselfIsLoadedOnce() {
        stylesheets.put("a.css", "@import \"a.css\"; p { color: red; }");

        ITextRenderer renderer = load(LINKED.formatted("a.css"));

        assertThat(requested).containsExactly("a.css");
        assertThat(colorOfParagraph(renderer)).isEqualTo(RED);
    }

    @Test
    void stylesheetsThatImportEachOtherAreLoadedOnce() {
        stylesheets.put("a.css", "@import \"b.css\"; p { color: red; }");
        stylesheets.put("b.css", "@import \"a.css\"; p { color: blue; }");

        ITextRenderer renderer = load(LINKED.formatted("a.css"));

        assertThat(requested).containsExactly("a.css", "b.css");
        assertThat(colorOfParagraph(renderer)).isEqualTo(RED);
    }

    @Test
    void importsAreFollowedToALimitedDepth() {
        for (int i = 0; i < 40; i++) {
            stylesheets.put("level" + i + ".css", "@import \"level" + (i + 1) + ".css\";");
        }

        load(LINKED.formatted("level0.css"));

        assertThat(requested).hasSize(17).startsWith("level0.css").endsWith("level16.css");
    }

    @Test
    void stylesheetImportedTwiceIsStillAppliedTwice() {
        stylesheets.put("a.css", "@import \"red.css\"; @import \"blue.css\"; @import \"red.css\";");
        stylesheets.put("red.css", "p { color: red; }");
        stylesheets.put("blue.css", "p { color: blue; }");

        ITextRenderer renderer = load(LINKED.formatted("a.css"));

        assertThat(colorOfParagraph(renderer)).isEqualTo(RED);
    }

    @Test
    void styleElementCannotBeImportedByItsCacheKey() {
        String html = "<html><head><style>p { color: red; }</style><style>p { color: blue; }</style>"
                + "<link rel=\"stylesheet\" href=\"a.css\"/></head><body><p>text</p></body></html>";
        stylesheets.put("a.css", "@import \"inline:" + (lastInlineStyleNumber() + 1) + "\";");

        ITextRenderer renderer = load(html);

        assertThat(requested).containsExactly("a.css");
        assertThat(colorOfParagraph(renderer)).isEqualTo(BLUE);
    }

    private ITextRenderer load(String html) {
        ITextRenderer renderer = new ITextRenderer();
        int dotsPerPixel = Math.round(renderer.getOutputDevice().getDotsPerPoint());
        renderer.getSharedContext().setUserAgentCallback(new ITextUserAgent(renderer.getOutputDevice(), dotsPerPixel) {
            @Override
            public CSSResource getCSSResource(String uri) {
                if (uri.endsWith(DEFAULT_STYLESHEET)) {
                    return super.getCSSResource(uri);
                }
                requested.add(uri);
                String css = stylesheets.get(uri);
                return new CSSResource(css == null ? null : new ByteArrayInputStream(css.getBytes(UTF_8)));
            }
        });
        renderer.setDocumentFromString(html);
        return renderer;
    }

    private static String colorOfParagraph(ITextRenderer renderer) {
        Element paragraph = (Element) renderer.getDocument().getElementsByTagName("p").item(0);
        return renderer.getSharedContext().getCss().getCascadedPropertiesMap(paragraph).get("color").getCssText();
    }

    /**
     * Style elements are cached under a key that ends with a running number. Reads one to learn the number
     * that was given out last.
     */
    private static long lastInlineStyleNumber() {
        XMLResource document = XMLResource.load("<html><head><style>p { }</style></head><body></body></html>");
        List<StylesheetInfo> infos = new XhtmlNamespaceHandler().getStylesheets(document.getDocument());
        return Long.parseLong(infos.get(0).getUri().substring("inline:".length()));
    }
}
