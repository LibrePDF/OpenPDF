package org.openpdf.text.pdf;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class PageResourcesTest {

    @Test
    void hasResourcesReturnsFalseWhenAllResourceDictionariesAreEmpty() {
        PageResources resources = new PageResources();

        assertThat(resources.hasResources()).isFalse();
    }

    @Test
    void hasResourcesDetectsGraphicColorPatternAndStateResources() {
        PdfIndirectReference dummyRef = new PdfIndirectReference(0, 1);

        PageResources fontResources = new PageResources();
        fontResources.addFont(new PdfName("F1"), dummyRef);
        assertThat(fontResources.hasResources()).isTrue();

        PageResources xObjectResources = new PageResources();
        xObjectResources.addXObject(new PdfName("Im1"), dummyRef);
        assertThat(xObjectResources.hasResources()).isTrue();

        PageResources colorResources = new PageResources();
        colorResources.addColor(new PdfName("CS1"), dummyRef);
        assertThat(colorResources.hasResources()).isTrue();

        PageResources patternResources = new PageResources();
        patternResources.addPattern(new PdfName("P1"), dummyRef);
        assertThat(patternResources.hasResources()).isTrue();

        PageResources shadingResources = new PageResources();
        shadingResources.addShading(new PdfName("Sh1"), dummyRef);
        assertThat(shadingResources.hasResources()).isTrue();

        PageResources extGStateResources = new PageResources();
        extGStateResources.addExtGState(new PdfName("GS1"), dummyRef);
        assertThat(extGStateResources.hasResources()).isTrue();

        PageResources propertyResources = new PageResources();
        propertyResources.addProperty(new PdfName("Pr1"), dummyRef);
        assertThat(propertyResources.hasResources()).isTrue();
    }
}
