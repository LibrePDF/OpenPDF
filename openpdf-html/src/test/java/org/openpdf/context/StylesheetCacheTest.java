package org.openpdf.context;

import org.junit.jupiter.api.Test;
import org.openpdf.css.sheet.Stylesheet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.openpdf.css.sheet.StylesheetInfo.Origin.AUTHOR;

class StylesheetCacheTest {
    private final StylesheetCache cache = new StylesheetCache();

    @Test
    void holdsNoMoreThan16Entries() {
        for (int i = 0; i < 17; i++) {
            cache.put("key#" + i, new Stylesheet("https://" + i, AUTHOR));
        }
        assertThat(cache.size()).isEqualTo(16);
        assertThat(cache).doesNotContainKey("key#0");
        assertThat(cache).containsKey("key#1");
        assertThat(cache).containsKey("key#16");
    }
}