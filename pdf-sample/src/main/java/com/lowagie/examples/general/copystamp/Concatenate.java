/*
 * $Id: Concatenate.java 3373 2008-05-12 16:21:24Z xlv $
 *
 * This code is free software. It may only be copied or modified
 * if you include the following copyright notice:
 *
 * This class by Mark Thompson. Copyright (c) 2002 Mark Thompson.
 *
 * This code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

/**
 * This class demonstrates copying a PDF file using iText.
 *
 * @author Mark Thompson
 */
package com.lowagie.examples.general.copystamp;

import com.lowagie.examples.AbstractSample;
import com.lowagie.text.Document;
import com.lowagie.text.pdf.PRAcroForm;
import com.lowagie.text.pdf.PdfCopy;
import com.lowagie.text.pdf.PdfImportedPage;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.SimpleBookmark;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Tool that can be used to concatenate existing PDF files.
 */
public class Concatenate extends AbstractSample {

    @Override
    public String getFileName() {
        return "/concatenated";
    }

    public static void main(String[] args) {
        Concatenate templates = new Concatenate();
        templates.run(args);
    }

    /**
     * @param path
     */
    public void render(String path) {
        System.out.println("General :: CopyStamp :: PdfCopy example");
        try {
            int pageOffset = 0;
            List<Map<String, Object>> master = new ArrayList<>();
            int f = 0;
            String outFile = path + getFileName() + ".pdf";
            Document document = null;
            PdfCopy writer = null;
            String[] files = new String[2];
            files[0] = path + "/../../directcontent/coordinates/affine_transformation.pdf";
            files[1] = path + "/../faq/measurements.pdf";
            while (f < files.length) {
                // we create a reader for a certain document
                PdfReader reader = new PdfReader(files[f]);
                reader.consolidateNamedDestinations();
                // we retrieve the total number of pages
                int n = reader.getNumberOfPages();
                List<Map<String, Object>> bookmarks = SimpleBookmark.getBookmarkList(reader);
                if (bookmarks != null) {
                    if (pageOffset != 0)
                        SimpleBookmark.shiftPageNumbersInRange(bookmarks, pageOffset, null);
                    master.addAll(bookmarks);
                }
                pageOffset += n;

                if (f == 0) {
                    // step 1: creation of a document-object
                    document = new Document(reader.getPageSizeWithRotation(1));
                    // step 2: we create a writer that listens to the document
                    writer = new PdfCopy(document, new FileOutputStream(outFile));
                    // step 3: we open the document
                    document.open();
                }
                // step 4: we add content
                PdfImportedPage page;
                for (int i = 0; i < n; ) {
                    ++i;
                    page = writer.getImportedPage(reader, i);
                    writer.addPage(page);
                }
                PRAcroForm form = reader.getAcroForm();
                if (form != null)
                    writer.copyAcroForm(reader);
                f++;
            }
            if (!master.isEmpty())
                writer.setOutlines(master);
            // step 5: we close the document
            document.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
