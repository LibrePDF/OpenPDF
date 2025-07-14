package com.lowagie.text;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ParagraphTest {

    @Test
    void testParagraphInParagraphAlign() {
        // given
        Paragraph outer = new Paragraph("Outer\n");
        outer.setAlignment(Element.ALIGN_LEFT);
        Paragraph inner = new Paragraph("Inner");
        inner.setAlignment(Element.ALIGN_RIGHT);
        // when
        outer.add(inner);
        // then
        assertThat(outer.alignment).isEqualTo(Element.ALIGN_LEFT);
    }

}
