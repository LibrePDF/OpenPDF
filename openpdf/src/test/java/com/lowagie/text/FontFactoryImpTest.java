package com.lowagie.text;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class FontFactoryImpTest {

    /**
     * Registering the same family/name combination several times must not add duplicate
     * entries to the font family list, otherwise the list grows unbounded and slows down
     * the lookup loop in {@link FontFactoryImp#getFont}.
     */
    @Test
    void registerFamilyShouldNotStoreDuplicateFullNames() {
        FontFactoryImp fontFactory = new FontFactoryImp();

        fontFactory.registerFamily("my-family", "My-Font-Regular", null);
        fontFactory.registerFamily("my-family", "My-Font-Regular", null);
        fontFactory.registerFamily("my-family", "My-Font-Regular", null);

        // A distinct member of the same family must still be added.
        fontFactory.registerFamily("my-family", "My-Font-Bold", null);

        assertThat(fontFactory.getRegisteredFamilies()).contains("my-family");
        assertThat(fontFactory.getRegisteredFamily("my-family"))
                .containsExactlyInAnyOrder("My-Font-Regular", "My-Font-Bold");
    }
}
