/*
 * $Id: DocumentSettings.java 3373 2008-05-12 16:21:24Z xlv $
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
package com.lowagie.examples.rtf.documentsettings;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Paragraph;
import com.lowagie.text.rtf.RtfWriter2;
import com.lowagie.text.rtf.document.output.RtfDataCache;

/**
 * The DocumentSettings class demonstrates accessing and changing document
 * generation settings. Specifically the dataCacheStyle and alwaysGenerateSoftLinebreaks
 * properties are changed.
 * 
 * @version $Revision: 3373 $
 * @author Mark Hall (Mark.Hall@mail.room3b.eu)
 */
public class DocumentSettings {
    /**
     * Document settings example.
     * 
     * @param args Unused
     */
    public static void main(String[] args) {
        System.out.println("Demonstrates setting document settings");
        try {
            Document document = new Document();
            
            // Keep a reference to the RtfWriter2 instance.
            RtfWriter2 writer2 = RtfWriter2.getInstance(document, new FileOutputStream("DocumentSettings.rtf"));
            
            // Specify that the document caching is to be done on disk.
            writer2.getDocumentSettings().setDataCacheStyle(RtfDataCache.CACHE_DISK);
            
            // Specify that all \n are translated into soft linebreaks.
            writer2.getDocumentSettings().setAlwaysGenerateSoftLinebreaks(true);
            
            document.open();
            document.add(new Paragraph("This example has been cached on disk\nand all " +
                    "\\n have been translated into soft linebreaks."));
            document.close();
        } catch (FileNotFoundException fnfe) {
            fnfe.printStackTrace();
        } catch (DocumentException de) {
            de.printStackTrace();
        }
    }
}
