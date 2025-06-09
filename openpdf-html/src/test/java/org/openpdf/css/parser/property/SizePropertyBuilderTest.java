package org.openpdf.css.parser.property;

import org.junit.jupiter.api.Test;
import org.openpdf.css.constants.CSSName;
import org.openpdf.css.parser.CSSParseException;
import org.openpdf.css.parser.PropertyValue;
import org.openpdf.css.sheet.PropertyDeclaration;
import org.openpdf.css.sheet.StylesheetInfo.Origin;

import java.util.List;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.w3c.dom.css.CSSPrimitiveValue.CSS_IDENT;
import static org.w3c.dom.css.CSSPrimitiveValue.CSS_MM;
import static org.openpdf.css.constants.CSSName.getByPropertyName;
import static org.openpdf.css.constants.IdentValue.AUTO;

class SizePropertyBuilderTest {
    private final SizePropertyBuilder builder = new SizePropertyBuilder();
    private final PropertyValue width = new PropertyValue(CSS_MM, 0, "43mm");
    private final PropertyValue height = new PropertyValue(CSS_MM, 0, "25mm");
    private final PropertyValue landscape = new PropertyValue(CSS_IDENT, 0, "landscape");
    private final CSSName cssName = getByPropertyName("size");

    @Test
    void buildDeclarationsFromOneValue() {
        List<PropertyDeclaration> result = builder.buildDeclarations(cssName, List.of(width), Origin.USER, false);

        assertThat(result).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new PropertyDeclaration(getByPropertyName("-fs-page-orientation"), new PropertyValue(AUTO), false, Origin.USER),
                new PropertyDeclaration(getByPropertyName("-fs-page-width"), width, false, Origin.USER),
                new PropertyDeclaration(getByPropertyName("-fs-page-height"), width, false, Origin.USER)
        );
    }

    @Test
    void buildDeclarationsFromTwoValues() {
        List<PropertyDeclaration> result = builder.buildDeclarations(cssName, List.of(width, height), Origin.USER_AGENT, false);

        assertThat(result).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new PropertyDeclaration(getByPropertyName("-fs-page-orientation"), new PropertyValue(AUTO), false, Origin.USER_AGENT),
                new PropertyDeclaration(getByPropertyName("-fs-page-width"), width, false, Origin.USER_AGENT),
                new PropertyDeclaration(getByPropertyName("-fs-page-height"), height, false, Origin.USER_AGENT)
        );
    }

    @Test
    void buildDeclarationsFromThreeValues() {
        List<PropertyDeclaration> result = builder.buildDeclarations(cssName, List.of(width, height, landscape), Origin.USER_AGENT, false);

        assertThat(result).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new PropertyDeclaration(getByPropertyName("-fs-page-width"), width, false, Origin.USER_AGENT),
                new PropertyDeclaration(getByPropertyName("-fs-page-height"), height, false, Origin.USER_AGENT),
                new PropertyDeclaration(getByPropertyName("-fs-page-orientation"), landscape, false, Origin.USER_AGENT)
        );
    }

    @Test
    void declarationMustHaveAtLeastOneValue() {
        assertThatThrownBy(() -> builder.buildDeclarations(cssName, emptyList(), Origin.AUTHOR, false))
                .isInstanceOf(CSSParseException.class)
                .hasMessageStartingWith("Found 0 values for size");
    }

    @Test
    void declarationMustHaveAtMostThreeValue() {
        assertThatThrownBy(() -> builder.buildDeclarations(cssName, List.of(width, height, landscape, landscape), Origin.AUTHOR, false))
                .isInstanceOf(CSSParseException.class)
                .hasMessageStartingWith("Found 4 values for size");
    }
}