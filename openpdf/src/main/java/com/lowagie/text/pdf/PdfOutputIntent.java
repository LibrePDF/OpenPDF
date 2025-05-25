package com.lowagie.text.pdf;

import java.awt.color.ICC_Profile;
import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 *  OutputIntent implementation for PDF/X compliance.
 */
public class PdfOutputIntent extends PdfDictionary {

    public PdfOutputIntent(PdfWriter writer,
                           String outputConditionIdentifier,
                           String info,
                           String registryName,
                           String outputCondition,
                           ICC_Profile icc) throws IOException {
        super(PdfName.OUTPUTINTENT);

        put(PdfName.OUTPUTCONDITIONIDENTIFIER, new PdfString(outputConditionIdentifier));
        put(PdfName.INFO, new PdfString(info));
        put(PdfName.REGISTRYNAME, new PdfString(registryName));
        put(PdfName.OUTPUTCONDITION, new PdfString(outputCondition));
        put(PdfName.S, new PdfName("GTS_PDFX"));

        PdfICCBased iccBased = new PdfICCBased(icc);
        PdfIndirectObject iccRef = writer.addToBody(iccBased);
        put(PdfName.DESTOUTPUTPROFILE, iccRef.getIndirectReference());
    }

    public void setOutputIntentSubtype(PdfName subtype) {
        put(PdfName.S, subtype);
    }
}
