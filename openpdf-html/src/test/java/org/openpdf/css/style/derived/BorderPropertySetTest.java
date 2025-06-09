package org.openpdf.css.style.derived;

import org.junit.jupiter.api.Test;
import org.openpdf.newtable.CollapsedBorderValue;

import static org.assertj.core.api.Assertions.assertThat;
import static org.openpdf.css.parser.FSRGBColor.BLUE;

class BorderPropertySetTest {
    @Test
    void constructor() {
        BorderPropertySet borders = new BorderPropertySet(
                new CollapsedBorderValue(null, 1, BLUE, 0),
                new CollapsedBorderValue(null, 2, BLUE, 0),
                new CollapsedBorderValue(null, 3, BLUE, 0),
                new CollapsedBorderValue(null, 4, BLUE, 0)
        );

        assertThat(borders.top()).isEqualTo(1.0f);
        assertThat(borders.right()).isEqualTo(2.0f);
        assertThat(borders.bottom()).isEqualTo(3.0f);
        assertThat(borders.left()).isEqualTo(4.0f);
    }

    @Test
    void resetNegativeValues() {
        BorderPropertySet negativeBorders = new BorderPropertySet(
                new CollapsedBorderValue(null, -1, BLUE, 0),
                new CollapsedBorderValue(null, -2, BLUE, 0),
                new CollapsedBorderValue(null, 3, BLUE, 0),
                new CollapsedBorderValue(null, 4, BLUE, 0)
        );

        BorderPropertySet borders = negativeBorders.resetNegativeValues();

        assertThat(borders.top()).isEqualTo(0.0f);
        assertThat(borders.right()).isEqualTo(0.0f);
        assertThat(borders.bottom()).isEqualTo(3.0f);
        assertThat(borders.left()).isEqualTo(4.0f);
    }
}