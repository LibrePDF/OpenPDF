/*
 * $Id: XfdfExample.java 3373 2008-05-12 16:21:24Z xlv $
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
import com.lowagie.text.pdf.XfdfReader;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;

/**
 * How to merge an XFDF file with a PDF form.
 */
public class XfdfExample   extends AbstractSample {

    @Override
    public String getFileName() {
        return "/registered_xfdf";
    }

    public static void main(String[] args) {
        XfdfExample templates = new XfdfExample();
        templates.run(args);
    }

    /**
     * @param path
     */
    public void render(String path) {
        System.out.println("Forms :: Fill :: XFDF Example");
        try {
            // merging the FDF file
            PdfReader pdfreader = new PdfReader(path + "/../simple_registration_form.pdf");
            PdfStamper stamp = new PdfStamper(pdfreader, new FileOutputStream(path + getFileName() + ".pdf"));
            InputStream inputStream = XfdfExample.class.getClassLoader().getResource("register.xfdf").openStream();
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();

            int nRead;
            byte[] data = new byte[inputStream.available()];

            while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }

            XfdfReader fdfreader = new XfdfReader(buffer.toByteArray());
            AcroFields form = stamp.getAcroFields();
            form.setFields(fdfreader);
            stamp.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
