package com.lowagie.text;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import org.junit.jupiter.api.Test;

class PhraseTest {

    @Test
    void setFontAfterContentAdded() {
        // given
        Font times = StandardFonts.TIMES.create();
        Font helvetica = new Font(Font.HELVETICA);
        Phrase phrase = new Phrase("Hello ", times);
        // when
        phrase.setFont(helvetica);
        phrase.add("World");
        // then
        ArrayList<Element> chunks = phrase.getChunks();
        assertThat(chunks).hasSize(2);
        assertThat(chunks.get(0))
                .isInstanceOf(Chunk.class)
                .extracting("font").isEqualTo(times);
        assertThat(chunks.get(1))
                .isInstanceOf(Chunk.class)
                .extracting("font").isEqualTo(helvetica);
    }

}
