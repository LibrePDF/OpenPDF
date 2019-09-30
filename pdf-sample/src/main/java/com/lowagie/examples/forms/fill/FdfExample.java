/*
 * $Id: FdfExample.java 3373 2008-05-12 16:21:24Z xlv $
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

import com.lowagie.text.pdf.AcroFields;
import com.lowagie.text.pdf.FdfReader;
import com.lowagie.text.pdf.FdfWriter;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfStamper;

import java.io.FileOutputStream;

/**
 * How to create an FDF file.
 * How to merge an FDF file with a PDF form.
 */
public class FdfExample {
    /**
     * Writes an FDF file and merges it with a PDF form.
     *
     * @param args no arguments needed
     */
    public static void main(String[] args) {
        System.out.println("Forms :: Fill :: FDF Example");
        try {
            // writing the FDF file
            FdfWriter fdf = new FdfWriter();
            fdf.setFieldAsString("name", "Bruno Lowagie");
            fdf.setFieldAsString("address", "Baeyensstraat 121, Sint-Amandsberg");
            fdf.setFieldAsString("postal_code", "BE-9040");
            fdf.setFieldAsString("email", "bruno@lowagie.com");
            fdf.setFile(args[0] + "/SimpleRegistrationForm.pdf");
            fdf.writeTo(new FileOutputStream(args[0] + "/../SimpleRegistrationForm.fdf"));

            // merging the FDF file
            PdfReader pdfreader = new PdfReader(args[0] + "/../SimpleRegistrationForm.pdf");
            PdfStamper stamp = new PdfStamper(pdfreader, new FileOutputStream(args[0] + "/registered_fdf.pdf"));
            FdfReader fdfreader = new FdfReader(args[0] + "/../SimpleRegistrationForm.fdf");
            AcroFields form = stamp.getAcroFields();
            form.setFields(fdfreader);
            stamp.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
