/*
 * $Id: EncryptorExample.java 3373 2008-05-12 16:21:24Z xlv $
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
import com.lowagie.text.pdf.PdfEncryptor;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfWriter;

import java.io.FileOutputStream;

/**
 * Encrypts an existing PDF file.
 */
public class EncryptorExample extends AbstractSample {

    @Override
    public String getFileName() {
        return "/encrypted";
    }

    public static void main(String[] args) {
        EncryptorExample templates = new EncryptorExample();
        templates.run(args);
    }

    /**
     * @param path
     */
    public void render(String path) {
        System.out.println("General :: CopyStamp :: Encryptor example");
        try {
            PdfReader reader = new PdfReader(path + "/../../objects/bookmarks/chapter_section.pdf");
            PdfEncryptor.encrypt(reader,
                    new FileOutputStream(path + getFileName() + ".pdf"),
                    "Hello".getBytes(),
                    "World".getBytes(),
                    PdfWriter.ALLOW_PRINTING | PdfWriter.ALLOW_COPY,
                    false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
