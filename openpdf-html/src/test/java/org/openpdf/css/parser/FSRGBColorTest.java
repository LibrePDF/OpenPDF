package org.openpdf.css.parser;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FSRGBColorTest {
    @Test
    void toString_rgb() {
        assertThat(new FSRGBColor(255, 250, 180)).hasToString("#fffab4");
        assertThat(new FSRGBColor(1, 2, 3)).hasToString("#010203");
    }

    @Test
    void toString_rgba() {
        assertThat(new FSRGBColor(255, 250, 180, 0.42f)).hasToString("rgba(255,250,180,0.42)");
        assertThat(new FSRGBColor(1, 2, 3, 0.01f)).hasToString("rgba(1,2,3,0.01)");
    }

    @Test
    void toHSB() {
        assertThat(new FSRGBColor(255, 200, 100).toHSB()).isEqualTo(new HSBColor(0.107526876f, 0.60784316f, 1.0f));
        assertThat(new FSRGBColor(1, 2, 3).toHSB()).isEqualTo(new HSBColor(0.5833333f, 0.6666667f, 0.011764706f));
        assertThat(new FSRGBColor(1, 2, 3, 0.1f).toHSB()).isEqualTo(new HSBColor(0.5833333f, 0.6666667f, 0.011764706f));
    }
}