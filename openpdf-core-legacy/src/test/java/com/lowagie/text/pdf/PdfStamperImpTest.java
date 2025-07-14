package com.lowagie.text.pdf;

import java.io.IOException;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class PdfStamperImpTest {

    @SuppressWarnings("unchecked")
    @Test
    void getPdfLayers_isBackwardsCompatible() throws IOException {
        // given
        PdfReader reader = new PdfReader(DocumentProducerHelper.createHelloWorldDocumentBytes());
        PdfStamperImp testMe = new PdfStamperImp(reader, null, '\0', false);
        // when
        @SuppressWarnings("rawtypes")
        Map layers = testMe.getPdfLayers();
        // then
        Assertions.assertThat(layers).isEmpty();
    }

    @Test
    void getPdfLayersWithGenerics_isBackwardsCompatible() throws IOException {
        // given
        PdfReader reader = new PdfReader(DocumentProducerHelper.createHelloWorldDocumentBytes());
        PdfStamperImp testMe = new PdfStamperImp(reader, null, '\0', false);
        // when
        Map<String, PdfLayer> layers = testMe.getPdfLayers();
        // then
        Assertions.assertThat(layers).isEmpty();
    }

}
