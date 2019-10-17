/*
 * $Id: Register.java 3373 2008-05-12 16:21:24Z xlv $
 *
 * This code is part of the 'OpenPDF Tutorial'.
 * You can find the complete tutorial at the following address:
 * https://github.com/LibrePDF/OpenPDF/wiki/Tutorial
 *
 * This code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 *
 */
package com.lowagie.examples.general.copystamp;

import com.lowagie.examples.AbstractSample;
import com.lowagie.text.pdf.AcroFields;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfStamper;

import java.io.FileOutputStream;

/**
 * Fill in a simple registration form.
 */
public class Register extends AbstractSample {

    @Override
    public String getFileName() {
        return "/registered";
    }

    public static void main(String[] args) {
        Register templates = new Register();
        templates.run(args);
    }

    /**
     * @param path
     */
    public void render(String path) {
        System.out.println("General :: CopyStamp :: Filling in a form");
        try {
            // we create a reader for a certain document
            PdfReader reader = new PdfReader(path + "/../../forms/simple_registration_form.pdf");
            // filling in the form
            PdfStamper stamp1 = new PdfStamper(reader, new FileOutputStream(path + getFileName() + ".pdf"));
            AcroFields form1 = stamp1.getAcroFields();
            form1.setField("name", "Bruno Lowagie");
            form1.setField("address", "Baeyensstraat 121, Sint-Amandsberg");
            form1.setField("postal_code", "BE-9040");
            form1.setField("email", "bruno@lowagie.com");
            stamp1.close();
        } catch (Exception de) {
            de.printStackTrace();
        }
    }
}
