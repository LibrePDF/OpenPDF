/*
 * $Id: ConcatPdf.java 3373 2008-05-12 16:21:24Z xlv $
 *
 * Copyright 2002 by Mark Thompson
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
import com.lowagie.text.pdf.PdfCopy;
import com.lowagie.text.pdf.PdfImportedPage;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfStream;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.SimpleBookmark;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Tool that can be used to concatenate existing PDF files.
 *
 * @since 2.1.1 (renamed to follow Java naming conventions)
 */
public class ConcatPdf {

    /**
     * This class can be used to concatenate existing PDF files. (This was an example known as PdfCopy.java)
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("arguments: file1 [file2 ...] destfile");
        } else {
            try {
                File outFile = new File(args[args.length - 1]);
                List<File> sources = new ArrayList<>();
                for (int i = 0; i < args.length - 1; i++) {
                    sources.add(new File(args[i]));
                }
                concat(sources, outFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void concat(List<File> sources, File target) throws IOException {

        for (File source : sources) {
            if (!source.isFile() || !source.canRead()) {
                throw new IOException("cannot read:" + source.getAbsolutePath());
            }
        }

        int pageOffset = 0;
        List<Map<String, Object>> master = new ArrayList<>();

        Document document = new Document();
        PdfCopy writer = new PdfCopy(document, new BufferedOutputStream(Files.newOutputStream(target.toPath())));
        writer.setPdfVersion(PdfWriter.VERSION_1_7);
        writer.setFullCompression();
        writer.setCompressionLevel(PdfStream.BEST_COMPRESSION);
        document.open();
        for (File source : sources) {
            // we create a reader for a certain document
            PdfReader reader = new PdfReader(new BufferedInputStream(Files.newInputStream(source.toPath())));
            reader.consolidateNamedDestinations();
            // we retrieve the total number of pages
            int numberOfPages = reader.getNumberOfPages();
            List<Map<String, Object>> bookmarks = SimpleBookmark.getBookmarkList(reader);
            if (bookmarks != null) {
                if (pageOffset != 0) {
                    SimpleBookmark.shiftPageNumbersInRange(bookmarks, pageOffset, null);
                }
                master.addAll(bookmarks);
            }
            pageOffset += numberOfPages;

            // we add content
            PdfImportedPage page;
            for (int i = 1; i <= numberOfPages; i++) {
                page = writer.getImportedPage(reader, i);
                writer.addPage(page);
            }
            writer.freeReader(reader);
        }
        if (!master.isEmpty()) {
            writer.setOutlines(master);
        }
        // we close the document
        document.close();
    }
}
