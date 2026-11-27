package org.openpdf.text.pdf.crypto;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class IVGeneratorTest {

    @Test
    void getIVReturnsSixteenBytesByDefault() {
        assertThat(IVGenerator.getIV()).hasSize(16);
    }

    @Test
    void getIVReturnsRequestedLength() {
        assertThat(IVGenerator.getIV(0)).isEmpty();
        assertThat(IVGenerator.getIV(4)).hasSize(4);
        assertThat(IVGenerator.getIV(32)).hasSize(32);
    }

    @Test
    void getIVDoesNotRepeatValues() {
        Set<String> keys = new HashSet<>();
        for (int i = 0; i < 1000; i++) {
            keys.add(new String(IVGenerator.getIV(32), StandardCharsets.ISO_8859_1));
        }
        assertThat(keys).hasSize(1000);
    }
}
