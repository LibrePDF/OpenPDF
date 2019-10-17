/*
 * $Id: ReadEncrypted.java 3373 2008-05-12 16:21:24Z xlv $
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
package com.lowagie.examples.general.read;

import com.lowagie.examples.AbstractSample;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfWriter;

import java.io.BufferedWriter;
import java.io.FileWriter;

/**
 * Reading an encrypted PDF file (you need the owner password to do this).
 */
public class ReadEncrypted extends AbstractSample {

    @Override
    public boolean isPdfProducer() {
        return false;
    }

    @Override
    public String getFileName() {
        return "/info_encrypted";
    }

    public static void main(String[] args) {
        ReadEncrypted templates = new ReadEncrypted();
        templates.run(args);
    }

    /**
     * @param path
     */
    public void render(String path) {
        System.out.println("General :: Read :: Read Encrypted PDF");
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(path + getFileName() + ".txt"));
            PdfReader r = new PdfReader(path + "/../HelloEncrypted.pdf", "Hello".getBytes());
            out.write(r.getInfo().toString());
            out.write("\r\n");
            int permissions = r.getPermissions();
            out.write("Printing allowed: " + ((PdfWriter.ALLOW_PRINTING & permissions) > 0));
            out.write("\r\n");
            out.write("Modifying contents allowed: " + ((PdfWriter.ALLOW_MODIFY_CONTENTS & permissions) > 0));
            out.write("\r\n");
            out.write("Copying allowed: " + ((PdfWriter.ALLOW_COPY & permissions) > 0));
            out.write("\r\n");
            out.write("Modifying annotations allowed: " + ((PdfWriter.ALLOW_MODIFY_ANNOTATIONS & permissions) > 0));
            out.write("\r\n");
            out.write("Fill in allowed: " + ((PdfWriter.ALLOW_FILL_IN & permissions) > 0));
            out.write("\r\n");
            out.write("Screen Readers allowed: " + ((PdfWriter.ALLOW_SCREENREADERS & permissions) > 0));
            out.write("\r\n");
            out.write("Assembly allowed: " + ((PdfWriter.ALLOW_ASSEMBLY & permissions) > 0));
            out.write("\r\n");
            out.write("Degraded printing allowed: " + ((PdfWriter.ALLOW_DEGRADED_PRINTING & permissions) > 0));
            out.write("\r\n");
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}