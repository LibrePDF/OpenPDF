/*
 * $Id: HelloWorld.java 3373 2008-05-12 16:21:24Z xlv $
 *
 * This code is free software. It may only be copied or modified
 * if you include the following copyright notice:
 *
 * --> Copyright 2006 by Mark Hall <--
 *
 * This code is part of the 'iText Tutorial'.
 * You can find the complete tutorial at the following address:
 * http://itextdocs.lowagie.com/tutorial/
 *
 * This code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * itext-questions@lists.sourceforge.net
 */
package com.lowagie.examples.rtf;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Paragraph;
import com.lowagie.text.rtf.RtfWriter2;

/**
 * The HelloWorld class demonstrates the basic steps of creating an
 * RTF document with iText.
 * 
 * @version $Revision: 3373 $
 * @author Mark Hall (Mark.Hall@mail.room3b.eu)
 */
public class HelloWorld {
    /**
     * Hello World! example
     * 
     * @param args Unused
     */
    public static void main(String[] args) {
        System.out.println("Hello World! example for the RTF format");
        try {
            // Step 1: Create a new Document
            Document document = new Document();
            
            // Step 2: Create a new instance of the RtfWriter2 with the document
            //         and target output stream.
            RtfWriter2.getInstance(document, new FileOutputStream("HelloWorld.rtf"));
            
            // Step 3: Open the document.
            document.open();
            
            // Step 4: Add content to the document.
            document.add(new Paragraph("Hello World!"));
            
            // Step 5: Close the document. It will be written to the target output stream.
            document.close();
        } catch (FileNotFoundException fnfe) {
            // It might not be possible to create the target file.
            fnfe.printStackTrace();
        } catch (DocumentException de) {
            // DocumentExceptions arise if you add content to the document before opening or
            // after closing the document.
            de.printStackTrace();
        }
    }
}
