package org.openpdf.pdf;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ITextFontResolverTest {
    private final ITextFontResolver resolver = new ITextFontResolver();

    @Test
    void normalizeFontFamily() {
        assertThat(resolver.normalizeFontFamily("ArialUnicodeMS")).isEqualTo("ArialUnicodeMS");
        assertThat(resolver.normalizeFontFamily("\"ArialUnicodeMS")).isEqualTo("ArialUnicodeMS");
        assertThat(resolver.normalizeFontFamily("ArialUnicodeMS\"")).isEqualTo("ArialUnicodeMS");
        assertThat(resolver.normalizeFontFamily("\"ArialUnicodeMS\"")).isEqualTo("ArialUnicodeMS");
    }

    @Test
    void normalizeFontFamily_serif() {
        assertThat(resolver.normalizeFontFamily("serif")).isEqualTo("Serif");
        assertThat(resolver.normalizeFontFamily("SERIF")).isEqualTo("Serif");
        assertThat(resolver.normalizeFontFamily("sErIf")).isEqualTo("Serif");
    }
    
    @Test
    void normalizeFontFamily_sans_serif() {
        assertThat(resolver.normalizeFontFamily("sans-serif")).isEqualTo("SansSerif");
        assertThat(resolver.normalizeFontFamily("SANS-serif")).isEqualTo("SansSerif");
        assertThat(resolver.normalizeFontFamily("sans-SERIF")).isEqualTo("SansSerif");
        assertThat(resolver.normalizeFontFamily("\"sans-serif")).isEqualTo("SansSerif");
        assertThat(resolver.normalizeFontFamily("sans-serif\"")).isEqualTo("SansSerif");
        assertThat(resolver.normalizeFontFamily("\"sans-serif\"")).isEqualTo("SansSerif");
    }

    @Test
    void normalizeFontFamily_monospace() {
        assertThat(resolver.normalizeFontFamily("monospace")).isEqualTo("Monospaced");
        assertThat(resolver.normalizeFontFamily("MONOSPACE")).isEqualTo("Monospaced");
        assertThat(resolver.normalizeFontFamily("\"monospace\"")).isEqualTo("Monospaced");
    }
}