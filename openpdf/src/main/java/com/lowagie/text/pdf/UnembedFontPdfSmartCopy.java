package com.lowagie.text.pdf;

import java.io.IOException;
import java.io.OutputStream;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;

/**
 * Makes a copy of a PDF, unembedding all embedded fonts. All font headers and descriptors are preserved and only the font file is removed.
 */
public class UnembedFontPdfSmartCopy extends PdfSmartCopy {
    
    public UnembedFontPdfSmartCopy(Document document, OutputStream os)
            throws DocumentException {
        super(document, os);
    }

    protected PdfDictionary copyDictionary(PdfDictionary in)
        throws IOException, BadPdfFormatException {

        PdfDictionary out = new PdfDictionary();
        PdfObject type = PdfReader.getPdfObjectRelease(in.get(PdfName.TYPE));

        for (PdfName key : in.getKeys()) {
            PdfObject value = in.get(key);

            if ((PdfName.FONTFILE.equals(key)
                    || PdfName.FONTFILE2.equals(key)
                    || PdfName.FONTFILE3.equals(key))
                    && !PdfReader.isFontSubset(PdfReader.getFontNameFromDescriptor(in))) {
                continue;
            } else {
                out.put(key, copyObject(value));
            }
        }

        return out;
    }
}
