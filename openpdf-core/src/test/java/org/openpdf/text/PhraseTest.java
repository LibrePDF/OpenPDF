package org.openpdf.text;

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

    @Test
    void getInstanceSplitsSpecialSymbolsIntoSymbolFontChunks() {
        Phrase phrase = Phrase.getInstance(16, "Alpha \u0391 Beta \u03B2", new Font(Font.HELVETICA, 12));

        assertThat(phrase.getContent()).isEqualTo("Alpha A Beta b");
        ArrayList<Element> chunks = phrase.getChunks();
        assertThat(chunks).hasSize(4);
        assertThat(((Chunk) chunks.get(0)).getFont().getFamily()).isEqualTo(Font.HELVETICA);
        assertThat(((Chunk) chunks.get(1)).getFont().getFamily()).isEqualTo(Font.SYMBOL);
        assertThat(((Chunk) chunks.get(2)).getFont().getFamily()).isEqualTo(Font.HELVETICA);
        assertThat(((Chunk) chunks.get(3)).getFont().getFamily()).isEqualTo(Font.SYMBOL);
    }

    @Test
    void addChunkMergesConsecutiveCompatibleChunks() {
        Phrase phrase = new Phrase();
        phrase.add(new Chunk("First "));
        phrase.add(new Chunk("Second"));

        assertThat(phrase).hasSize(1);
        assertThat(phrase.getContent()).isEqualTo("First Second");
    }

}
