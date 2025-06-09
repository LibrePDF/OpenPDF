package org.openpdf.pdf;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.openpdf.pdf.HTMLOutline.getOutlineLevelFromTagName;

class HTMLOutlineTest {
    @Test
    void getsOutlineLevelFromTagName_header() {
        assertThat(getOutlineLevelFromTagName("h1")).isEqualTo("1");
        assertThat(getOutlineLevelFromTagName("H2")).isEqualTo("2");
        assertThat(getOutlineLevelFromTagName("h3")).isEqualTo("3");
        assertThat(getOutlineLevelFromTagName("H6")).isEqualTo("6");
        assertThat(getOutlineLevelFromTagName("h7")).isEqualTo("7");
        assertThat(getOutlineLevelFromTagName("h10")).isEqualTo("10");
        assertThat(getOutlineLevelFromTagName("h16")).isEqualTo("16");
        assertThat(getOutlineLevelFromTagName("h99")).isEqualTo("99");
    }

    @Test
    void getsOutlineLevelFromTagName_exclude() {
        assertThat(getOutlineLevelFromTagName("blockquote")).isEqualTo("exclude");
        assertThat(getOutlineLevelFromTagName("BLOCKQUOTE")).isEqualTo("exclude");
        assertThat(getOutlineLevelFromTagName("details")).isEqualTo("exclude");
        assertThat(getOutlineLevelFromTagName("fieldset")).isEqualTo("exclude");
        assertThat(getOutlineLevelFromTagName("figure")).isEqualTo("exclude");
        assertThat(getOutlineLevelFromTagName("td")).isEqualTo("exclude");
        assertThat(getOutlineLevelFromTagName("TD")).isEqualTo("exclude");
    }

    @Test
    void getsOutlineLevelFromTagName_none() {
        assertThat(getOutlineLevelFromTagName("div")).isEqualTo("none");
        assertThat(getOutlineLevelFromTagName("table")).isEqualTo("none");
        assertThat(getOutlineLevelFromTagName("span")).isEqualTo("none");
        assertThat(getOutlineLevelFromTagName("SPAN")).isEqualTo("none");
    }
}
