/*
 * Copyright 2008 by Kevin Day.
 *
 * Contributions copyright 2014 Tizra Inc.
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
package org.openpdf.text.pdf.parser;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.openpdf.text.ExceptionConverter;
import org.openpdf.text.pdf.PRIndirectReference;
import org.openpdf.text.pdf.PRStream;
import org.openpdf.text.pdf.PRTokeniser;
import org.openpdf.text.pdf.PdfArray;
import org.openpdf.text.pdf.PdfContentParser;
import org.openpdf.text.pdf.PdfDictionary;
import org.openpdf.text.pdf.PdfLiteral;
import org.openpdf.text.pdf.PdfName;
import org.openpdf.text.pdf.PdfObject;
import org.openpdf.text.pdf.PdfReader;
import org.openpdf.text.pdf.RandomAccessFileOrArray;

/**
 * Locates text pattern coordinates inside a PDF file.
 *
 * @since 2.1.4
 */
@SuppressWarnings("WeakerAccess")
public class PdfTextLocator {

    /**
     * The PdfReader that holds the PDF file.
     */
    private final PdfReader reader;

    /**
     * The {@link TextAssembler} that will receive render notifications and provide resultant text
     */
    private final TextAssembler renderListener;

    /**
     * Creates a new Text Locator object, using a {@link TextAssembler} as the render listener
     *
     * @param reader the reader with the PDF
     */
    public PdfTextLocator(PdfReader reader) {
        this(reader, new MarkedUpTextAssembler(reader));
    }

    /**
     * Creates a new Text Extractor object, using a {@link TextAssembler} as the render listener
     *
     * @param reader               the reader with the PDF
     * @param usePdfMarkupElements should we use higher level tags for PDF markup entities?
     */
    public PdfTextLocator(PdfReader reader, boolean usePdfMarkupElements) {
        this(reader, new MarkedUpTextAssembler(reader, usePdfMarkupElements));
    }

    /**
     * Creates a new Text Locator object.
     *
     * @param reader         the reader with the PDF
     * @param renderListener the render listener that will be used to analyze renderText operations and provide
     *                       resultant text
     */
    public PdfTextLocator(PdfReader reader, TextAssembler renderListener) {
        this.reader = reader;
        this.renderListener = renderListener;
    }

    /**
     * Gets the content bytes of a page.
     *
     * @param pageNum the 1-based page number of page you want get the content stream from
     * @return a byte array with the effective content stream of a page
     * @throws IOException
     */
    private byte[] getContentBytesForPage(int pageNum) throws IOException {
        try (RandomAccessFileOrArray ignored = reader.getSafeFile()) {
            PdfDictionary pageDictionary = reader.getPageN(pageNum);
            PdfObject contentObject = pageDictionary.get(PdfName.CONTENTS);
            return getContentBytesFromContentObject(contentObject);
        }
    }

    /**
     * Gets the content bytes from a content object, which may be a reference a stream or an array.
     *
     * @param contentObject the object to read bytes from
     * @return the content bytes
     * @throws IOException
     */
    private byte[] getContentBytesFromContentObject(PdfObject contentObject) throws IOException {
        return PdfContentStreamHandler.getContentBytesFromPdfObjectStatic(contentObject);
    }

    /**
     * Locates text pattern inside a page
     *
     * @param page    page number we are interested in
     * @param pattern text to match
     * @return <CODE>ArrayList<MatchedPattern></CODE> List of matched text patterns with coordinates.
     * @throws IOException on error
     */
    public ArrayList<MatchedPattern> searchPage(int page, String pattern) throws IOException {
        PdfDictionary pageDict = reader.getPageN(page);
        if (pageDict == null) {
            return new ArrayList<>();
        }
        PdfDictionary resources = pageDict.getAsDict(PdfName.RESOURCES);
        renderListener.reset();
        renderListener.setPage(page);
        PdfContentTextLocator handler = new PdfContentTextLocator(renderListener, pattern, page);
        processContent(getContentBytesForPage(page), resources, handler);
        return handler.getMatchedPatterns();
    }

    /**
     * Locates text pattern inside a PDF
     *
     * @param pattern text to match
     * @return <CODE>ArrayList<MatchedPattern></CODE> List of matched text patterns with coordinates.
     * @throws IOException on error
     */
    public ArrayList<MatchedPattern> searchFile(String pattern) throws IOException {
        ArrayList<MatchedPattern> res = new ArrayList<>();
        for (int page = 1; page <= reader.getNumberOfPages(); page++) {
            res.addAll(searchPage(page, pattern));
        }
        return res;
    }

    /**
     * Processes PDF syntax
     *
     * @param contentBytes the bytes of a content stream
     * @param resources    the resources that come with the content stream
     * @param handler      interprets events caused by recognition of operations in a content stream.
     */
    public void processContent(byte[] contentBytes, PdfDictionary resources,
            PdfContentTextLocator handler) {
        handler.pushContext("div class='t-extracted-page'");
        try {
            PdfContentParser ps = new PdfContentParser(new PRTokeniser(contentBytes));
            List<PdfObject> operands = new ArrayList<>();
            while (!ps.parse(operands).isEmpty()) {
                PdfLiteral operator = (PdfLiteral) operands.getLast();
                handler.invokeOperator(operator, operands, resources);
            }
        } catch (Exception e) {
            throw new ExceptionConverter(e);
        }
        handler.popContext();
    }
}
