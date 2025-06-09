package org.openpdf.css.parser;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.openpdf.css.constants.CSSName;
import org.openpdf.css.sheet.PropertyDeclaration;
import org.openpdf.css.sheet.Ruleset;
import org.openpdf.css.sheet.Stylesheet;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.openpdf.css.constants.CSSName.BACKGROUND_COLOR;
import static org.openpdf.css.constants.CSSName.BORDER_TOP_COLOR;
import static org.openpdf.css.constants.CSSName.COLOR;
import static org.openpdf.css.sheet.StylesheetInfo.Origin.AUTHOR;

class CSSParserTest {
    private final List<String> errors = new ArrayList<>();
    private final CSSParser parser = new CSSParser((uri, message) -> errors.add(message));

    @AfterEach
    void tearDown() {
        assertThat(errors).isEmpty();
    }

    @Test
    void rgba() throws IOException {
        Stylesheet stylesheet = parser.parseStylesheet(null, AUTHOR, new StringReader("""
            p {
                color: rgb(255, 165, 11);
                background-color: rgba(233, 99, 71, 0.5);
                border-top-color: rgba(-20, 255, 300, 1);
            }
            """));
        assertThat(stylesheet.getContents()).hasSize(1);
        Ruleset ruleset = (Ruleset) stylesheet.getContents().get(0);

        assertThat(ruleset.getPropertyDeclarations()).usingRecursiveComparison().isEqualTo(List.of(
            css(COLOR, new FSRGBColor(255, 165, 11)),
            css(BACKGROUND_COLOR, new FSRGBColor(233, 99, 71, 0.5f)),
            css(BORDER_TOP_COLOR, new FSRGBColor(0, 255, 255, 1f))
        ));
    }

    private static PropertyDeclaration css(CSSName property, FSRGBColor color) {
        return new PropertyDeclaration(property, new PropertyValue(color), false, AUTHOR);
    }
}