/*
 * $Id: SplitPdf.java 4065 2009-09-16 23:09:11Z psoares33 $
 * $Name$
 *
 * Copyright 2002 by Bruno Lowagie
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the License.
 *
 * The Original Code is 'iText, a free JAVA-PDF library'.
 *
 * The Initial Developer of the Original Code is Bruno Lowagie. Portions created by
 * the Initial Developer are Copyright (C) 1999-2006 by Bruno Lowagie.
 * All Rights Reserved.
 * Co-Developer of the code is Paulo Soares. Portions created by the Co-Developer
 * are Copyright (C) 2000-2006 by Paulo Soares. All Rights Reserved.
 *
 * Contributor(s): all the names of the contributors are added in the source code
 * where applicable.
 *
 * Alternatively, the contents of this file may be used under the terms of the
 * LGPL license (the "GNU LIBRARY GENERAL PUBLIC LICENSE"), in which case the
 * provisions of LGPL are applicable instead of those above.  If you wish to
 * allow use of your version of this file only under the terms of the LGPL
 * License and not to allow others to use your version of this file under
 * the MPL, indicate your decision by deleting the provisions above and
 * replace them with the notice and other provisions required by the LGPL.
 * If you do not delete the provisions above, a recipient may use your version
 * of this file under either the MPL or the GNU LIBRARY GENERAL PUBLIC LICENSE.
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the MPL as stated above or under the terms of the GNU
 * Library General Public License as published by the Free Software Foundation;
 * either version 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Library general Public License for more
 * details.
 *
 * If you didn't download this code from the following link, you should check if
 * you aren't using an obsolete version:
 * https://github.com/LibrePDF/OpenPDF
 */

package com.lowagie.tools;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.error_messages.MessageLocalization;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfImportedPage;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfWriter;
import java.io.FileOutputStream;

/**
 * This class demonstrates how to split a PDF file using iText.
 *
 * @author Bruno Lowagie
 * @since 2.1.1 (renamed to follow Java naming conventions)
 */
public class SplitPdf extends java.lang.Object {

    /**
     * This class can be used to split an existing PDF file.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        if (args.length != 4) {
            System.err.println("arguments: srcfile destfile1 destfile2 pagenumber");
        } else {
            try {
                int pagenumber = Integer.parseInt(args[3]);

                // we create a reader for a certain document
                PdfReader reader = new PdfReader(args[0]);
                // we retrieve the total number of pages
                int n = reader.getNumberOfPages();
                System.out.println("There are " + n + " pages in the original file.");

                if (pagenumber < 2 || pagenumber > n) {
                    throw new DocumentException(MessageLocalization.getComposedMessage(
                            "you.can.t.split.this.document.at.page.1.there.is.no.such.page", pagenumber));
                }

                // step 1: creation of a document-object
                Document document1 = new Document(reader.getPageSizeWithRotation(1));
                Document document2 = new Document(reader.getPageSizeWithRotation(pagenumber));
                // step 2: we create a writer that listens to the document
                PdfWriter writer1 = PdfWriter.getInstance(document1, new FileOutputStream(args[1]));
                PdfWriter writer2 = PdfWriter.getInstance(document2, new FileOutputStream(args[2]));
                // step 3: we open the document
                document1.open();
                PdfContentByte cb1 = writer1.getDirectContent();
                document2.open();
                PdfContentByte cb2 = writer2.getDirectContent();
                PdfImportedPage page;
                int rotation;
                int i = 0;
                // step 4: we add content
                while (i < pagenumber - 1) {
                    i++;
                    document1.setPageSize(reader.getPageSizeWithRotation(i));
                    document1.newPage();
                    page = writer1.getImportedPage(reader, i);
                    rotation = reader.getPageRotation(i);
                    if (rotation == 90 || rotation == 270) {
                        cb1.addTemplate(page, 0, -1f, 1f, 0, 0, reader.getPageSizeWithRotation(i).getHeight());
                    } else {
                        cb1.addTemplate(page, 1f, 0, 0, 1f, 0, 0);
                    }
                }
                while (i < n) {
                    i++;
                    document2.setPageSize(reader.getPageSizeWithRotation(i));
                    document2.newPage();
                    page = writer2.getImportedPage(reader, i);
                    rotation = reader.getPageRotation(i);
                    if (rotation == 90 || rotation == 270) {
                        cb2.addTemplate(page, 0, -1f, 1f, 0, 0, reader.getPageSizeWithRotation(i).getHeight());
                    } else {
                        cb2.addTemplate(page, 1f, 0, 0, 1f, 0, 0);
                    }
                    System.out.println("Processed page " + i);
                }
                // step 5: we close the document
                document1.close();
                document2.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

