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

import com.lowagie.examples.AbstractSample;
import com.lowagie.text.pdf.PdfCopyFields;
import com.lowagie.text.pdf.PdfReader;

import java.io.FileOutputStream;

/**
 * Concatenates 2 PDF files with forms. The resulting PDF has 1 merged AcroForm.
 */
public class ConcatenateForms extends AbstractSample {

    @Override
    public int getExpectedPageCount() {
        return 2;
    }

    @Override
    public String getFileName() {
        return "/concatenated_forms";
    }

    public static void main(String[] args) {
        ConcatenateForms templates = new ConcatenateForms();
        templates.run(args);
    }

    /**
     * @param path
     */
    public void render(String path) {
        System.out.println("General :: CopyStamp ::  Concatenate Forms");
        try {
            PdfReader reader1 = new PdfReader(path + "/../../forms/simple_registration_form.pdf");
            PdfReader reader2 = new PdfReader(path + "/../../forms/text_fields.pdf");
            PdfCopyFields copy = new PdfCopyFields(new FileOutputStream(path + getFileName() + ".pdf"));
            copy.addDocument(reader1);
            copy.addDocument(reader2);
            copy.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
