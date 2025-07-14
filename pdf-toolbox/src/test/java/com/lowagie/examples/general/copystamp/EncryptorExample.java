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

import com.lowagie.text.pdf.PdfEncryptor;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfWriter;
import java.io.FileOutputStream;

/**
 * Encrypts an existing PDF file.
 */
public class EncryptorExample {

    /**
     * Reads and encrypts an existing PDF file.
     *
     * @param args no arguments needed
     */
    public static void main(String[] args) {
        System.out.println("Encryptor example");
        try {
            PdfReader reader = new PdfReader("ChapterSection.pdf");
            PdfEncryptor.encrypt(reader,
                    new FileOutputStream("encrypted.pdf"),
                    "Hello".getBytes(),
                    "World".getBytes(),
                    PdfWriter.ALLOW_PRINTING | PdfWriter.ALLOW_COPY,
                    false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
