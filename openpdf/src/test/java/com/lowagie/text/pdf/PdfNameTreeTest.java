package com.lowagie.text.pdf;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.HashMap;
import org.junit.jupiter.api.Test;

class PdfNameTreeTest {

  @Test
  void shouldReadTree() {

    PdfDictionary pdfDictionary = new PdfDictionary(PdfName.PDF);
    final PdfBoolean pdfBoolean = new PdfBoolean(true);
    final String keyName = "test";
    pdfDictionary.put(PdfName.NAMES, new PdfArray(Arrays.asList(new PdfString(keyName), pdfBoolean)));

    final HashMap<String, PdfObject> tree = PdfNameTree.readTree(pdfDictionary);
    assertEquals(1, tree.size());
    assertEquals(pdfBoolean, tree.get(keyName));

  }
}
