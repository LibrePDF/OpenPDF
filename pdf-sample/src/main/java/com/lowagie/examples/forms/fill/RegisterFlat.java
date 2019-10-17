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
package com.lowagie.examples.forms.fill;

import com.lowagie.examples.AbstractSample;
import com.lowagie.text.pdf.AcroFields;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfStamper;

import java.io.FileOutputStream;

/**
 * Fill in a simple registration form.
 */
public class RegisterFlat extends AbstractSample {

    @Override
    public String getFileName() {
        return "/registered_flat";
    }

    public static void main(String[] args) {
        RegisterFlat templates = new RegisterFlat();
        templates.run(args);
    }

    /**
     * @param path
     */
    public void render(String path) {
        System.out.println("Forms :: Fill :: Filling in a form");
        try {
            // we create a reader for a certain document
            PdfReader reader = new PdfReader(path + "/../simple_registration_form.pdf");
            // filling in the form and flatten
            PdfStamper stamp2 = new PdfStamper(reader, new FileOutputStream(path + getFileName() + ".pdf"));
            AcroFields form2 = stamp2.getAcroFields();
            form2.setField("name", "Bruno Lowagie");
            form2.setField("address", "Baeyensstraat 121, Sint-Amandsberg");
            form2.setField("postal_code", "BE-9040");
            form2.setField("email", "bruno@lowagie.com");
            stamp2.setFormFlattening(true);
            stamp2.close();
        } catch (Exception de) {
            de.printStackTrace();
        }
    }
}
