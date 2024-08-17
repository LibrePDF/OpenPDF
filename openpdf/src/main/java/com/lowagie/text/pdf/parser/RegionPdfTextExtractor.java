/*
 * Copyright 2024 OpenPDF
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the License.
 *
 * The Original Code is 'OpenPDF'.
 *
 * The Initial Developer of the Original Code is Bruno Lowagie. Portions created by
 * the Initial Developer are Copyright (C) 1999-2008 by Bruno Lowagie.
 * All Rights Reserved.
 * Co-Developer of the code is Paulo Soares. Portions created by the Co-Developer
 * are Copyright (C) 2000-2008 by Paulo Soares. All Rights Reserved.
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

package com.lowagie.text.pdf.parser;

import com.lowagie.text.pdf.*;

import java.io.IOException;

/**
 * Extracts text from a specific region of a PDF file.
 *
 * @since 2.0.4
 */
public class RegionPdfTextExtractor extends PdfTextExtractor {

    public RegionPdfTextExtractor(PdfReader reader) {
        super(reader);
    }

    public RegionPdfTextExtractor(PdfReader reader, TextAssembler renderListener) {
        super(reader, renderListener);
    }

    /**
     * Extracts text from a specific area of a page of a PDF document.
     *
     * 0,0 is the bottom left part of the page.
     */
    public String getTextFromPageArea(int page, float x, float y, float width, float height) throws IOException {
        PdfDictionary pageDict = reader.getPageN(page);
        if (pageDict == null) {
            return "";
        }
        PdfDictionary resources = pageDict.getAsDict(PdfName.RESOURCES);

        renderListener.reset();
        renderListener.setPage(page);
        RegionPdfContentStreamHandler handler = new RegionPdfContentStreamHandler(renderListener);
        handler.setBoundingBox(x, y, width, height);
        processContent(getContentBytesForPage(page), resources, handler);
        return handler.getResultantText();
    }

    /**
     * Gets the height of the specified page.
     */
    public float getPageHeight(int page) throws IOException {
        PdfDictionary pageDict = reader.getPageN(page);
        if (pageDict == null) {
            throw new IOException("Page dictionary not found for page: " + page);
        }
        PdfArray mediaBox = pageDict.getAsArray(PdfName.MEDIABOX);
        if (mediaBox == null || mediaBox.size() < 4) {
            throw new IOException("MediaBox not found or invalid for page: " + page);
        }
        PdfNumber lowerLeftY = mediaBox.getAsNumber(1);
        PdfNumber upperRightY = mediaBox.getAsNumber(3);
        return upperRightY.floatValue() - lowerLeftY.floatValue();
    }

    /**
     * Gets the width of the specified page.
     */
    public float getPageWidth(int page) throws IOException {
        PdfDictionary pageDict = reader.getPageN(page);
        if (pageDict == null) {
            throw new IOException("Page dictionary not found for page: " + page);
        }
        PdfArray mediaBox = pageDict.getAsArray(PdfName.MEDIABOX);
        if (mediaBox == null || mediaBox.size() < 4) {
            throw new IOException("MediaBox not found or invalid for page: " + page);
        }
        PdfNumber lowerLeftX = mediaBox.getAsNumber(0);
        PdfNumber upperRightX = mediaBox.getAsNumber(2);
        return upperRightX.floatValue() - lowerLeftX.floatValue();
    }

    public static String extractDateFromText(String text, String keyword) {
        int keywordIndex = text.indexOf(keyword);
        if (keywordIndex != -1) {
            // Extract the date assuming it's after the keyword
            String afterKeyword = text.substring(keywordIndex + keyword.length()).trim();
            String[] parts = afterKeyword.split("\\s+");
            if (parts.length > 0) {
                // If the first part is not a valid date, continue searching
                for (String part : parts) {
                    if (part.matches("\\d{4}-\\d{2}-\\d{2}")) {
                        return part; // Return the date in the format YYYY-MM-DD
                    }
                }
            }
        }
        return null;
    }


    public static String cleanExtractedText(String text) {
        return text.replaceAll("\\s+", " ").trim();
    }
}
