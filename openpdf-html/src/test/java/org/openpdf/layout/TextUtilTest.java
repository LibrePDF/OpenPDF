package org.openpdf.layout;

import org.junit.jupiter.api.Test;
import org.openpdf.util.Util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.openpdf.css.constants.IdentValue.CAPITALIZE;
import static org.openpdf.css.constants.IdentValue.LOWERCASE;
import static org.openpdf.css.constants.IdentValue.SHOW;
import static org.openpdf.css.constants.IdentValue.SMALL_CAPS;
import static org.openpdf.css.constants.IdentValue.UPPERCASE;
import static org.openpdf.layout.TextUtil.transformText;

class TextUtilTest {
    @Test
    void capitalizesWords() {
        assertThat(transformText("hellO worlD!", CAPITALIZE, SHOW)).isEqualTo("HellO WorlD!");
    }
    @Test
    void toLowerCase() {
        assertThat(transformText("hellO worlD!", LOWERCASE, SHOW)).isEqualTo("hello world!");
    }
    @Test
    void toUpperCase() {
        assertThat(transformText("hellO worlD!", UPPERCASE, SHOW)).isEqualTo("HELLO WORLD!");
    }
    
    @Test
    void smallCaps() {
        assertThat(transformText("hellO worlD!", SHOW, SMALL_CAPS)).isEqualTo("HELLO WORLD!");
    }

    @Test
    void replace() {
        assertThat(Util.replace("", "a", "b")).isEqualTo("");
        assertThat(Util.replace("Hello, World", "Hello", "Goodbye")).isEqualTo("Goodbye, World");
        assertThat(Util.replace("abababab", "ab", "c")).isEqualTo("cccc");
        assertThat(Util.replace("Foo Zoo", "o", "oo")).isEqualTo("Foooo Zoooo");
    }
}