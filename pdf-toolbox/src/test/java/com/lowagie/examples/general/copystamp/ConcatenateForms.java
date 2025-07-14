/*
 * $Id: ConcatenateForms.java 3373 2008-05-12 16:21:24Z xlv $
 *
 * This code is free software. It may only be copied or modified
 * if you include the following copyright notice:
 *
 * This class by Mark Thompson. Copyright (c) 2002 Mark Thompson.
 *
 * This code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
package com.lowagie.examples.general.copystamp;

import com.lowagie.text.pdf.PdfCopyFields;
import com.lowagie.text.pdf.PdfReader;
import java.io.FileOutputStream;

/**
 * Concatenates 2 PDF files with forms. The resulting PDF has 1 merged AcroForm.
 */
public class ConcatenateForms {

    /**
     * Concatenates 2 PDF files with forms. The resulting PDF has 1 merged AcroForm.
     *
     * @param args no arguments needed
     */
    public static void main(String[] args) {
        try {
            PdfReader reader1 = new PdfReader("SimpleRegistrationForm.pdf");
            PdfReader reader2 = new PdfReader("TextFields.pdf");
            PdfCopyFields copy = new PdfCopyFields(new FileOutputStream("concatenatedforms.pdf"));
            copy.addDocument(reader1);
            copy.addDocument(reader2);
            copy.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
