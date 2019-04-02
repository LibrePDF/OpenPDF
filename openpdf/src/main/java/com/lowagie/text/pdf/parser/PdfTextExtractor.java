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
 * http://www.lowagie.com/iText/
 */
package com.lowagie.text.pdf.parser;


import com.lowagie.text.ExceptionConverter;
import com.lowagie.text.pdf.PRIndirectReference;
import com.lowagie.text.pdf.PRStream;
import com.lowagie.text.pdf.PRTokeniser;
import com.lowagie.text.pdf.PdfArray;
import com.lowagie.text.pdf.PdfContentParser;
import com.lowagie.text.pdf.PdfDictionary;
import com.lowagie.text.pdf.PdfLiteral;
import com.lowagie.text.pdf.PdfName;
import com.lowagie.text.pdf.PdfObject;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.RandomAccessFileOrArray;

import javax.annotation.Nonnull;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

/**
 * Extracts text from a PDF file.
 *
 * @since 2.1.4
 */
@SuppressWarnings("WeakerAccess")
public class PdfTextExtractor {

    /**
     * The PdfReader that holds the PDF file.
     */
    private final PdfReader reader;

    /**
     * The {@link TextAssembler} that will receive render notifications and
     * provide resultant text
     */
    private final TextAssembler renderListener;

    /**
     * Creates a new Text Extractor object, using a {@link TextAssembler} as the
     * render listener
     *
     * @param reader the reader with the PDF
     */
    public PdfTextExtractor(PdfReader reader) {
        this(reader, new MarkedUpTextAssembler(reader));
    }

    /**
     * Creates a new Text Extractor object, using a {@link TextAssembler} as the
     * render listener
     *
     * @param reader               the reader with the PDF
     * @param usePdfMarkupElements should we use higher level tags for PDF markup entities?
     */
    public PdfTextExtractor(PdfReader reader, boolean usePdfMarkupElements) {
        this(reader, new MarkedUpTextAssembler(reader, usePdfMarkupElements));
    }

    /**
     * Creates a new Text Extractor object.
     *
     * @param reader         the reader with the PDF
     * @param renderListener the render listener that will be used to analyze renderText
     *                       operations and provide resultant text
     */
    public PdfTextExtractor(PdfReader reader, TextAssembler renderListener) {
        this.reader = reader;
        this.renderListener = renderListener;
    }

    /**
     * Gets the content bytes of a page.
     *
     * @param pageNum the 1-based page number of page you want get the content
     *                stream from
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
     * Gets the content bytes from a content object, which may be a reference a
     * stream or an array.
     *
     * @param contentObject the object to read bytes from
     * @return the content bytes
     * @throws IOException
     */
    private byte[] getContentBytesFromContentObject(PdfObject contentObject) throws IOException {
        final byte[] result;
        switch (contentObject.type()) {
            case PdfObject.INDIRECT:
                PRIndirectReference ref = (PRIndirectReference) contentObject;
                PdfObject directObject = PdfReader.getPdfObject(ref);
                result = getContentBytesFromContentObject(directObject);
                break;
            case PdfObject.STREAM:
                PRStream stream = (PRStream) PdfReader.getPdfObject(contentObject);
                result = PdfReader.getStreamBytes(stream);
                break;
            case PdfObject.ARRAY:
                // Stitch together all content before calling processContent(),
                // because
                // processContent() resets state.
                ByteArrayOutputStream allBytes = new ByteArrayOutputStream();
                PdfArray contentArray = (PdfArray) contentObject;
                ListIterator<PdfObject> iter = contentArray.listIterator();
                while (iter.hasNext()) {
                    PdfObject element = iter.next();
                    allBytes.write(getContentBytesFromContentObject(element));
                }
                result = allBytes.toByteArray();
                break;
            default:
                throw new IllegalStateException("Unable to handle Content of type " + contentObject.getClass());
        }
        return result;
    }

    /**
     * Gets the text from a page.
     *
     * @param page the 1-based page number of page
     * @return a String with the content as plain text (without PDF syntax)
     * @throws IOException on error
     */
    @Nonnull
    public String getTextFromPage(int page) throws IOException {
        return getTextFromPage(page, false);
    }

    /**
     * get the text from the page
     *
     * @param page               page number we are interested in
     * @param useContainerMarkup should we put tags in for PDf markup container elements (not
     *                           really HTML at the moment).
     * @return result of extracting the text, with tags as requested.
     * @throws IOException on error
     */
    @Nonnull
    public String getTextFromPage(int page, boolean useContainerMarkup) throws IOException {
        PdfDictionary pageDict = reader.getPageN(page);
        if (pageDict == null) {
            return "";
        }
        PdfDictionary resources = pageDict.getAsDict(PdfName.RESOURCES);

        renderListener.reset();
        renderListener.setPage(page);
        PdfContentStreamHandler handler = new PdfContentStreamHandler(renderListener);
        processContent(getContentBytesForPage(page), resources, handler);
        return handler.getResultantText();
    }

    /**
     * Processes PDF syntax
     *
     * @param contentBytes the bytes of a content stream
     * @param resources    the resources that come with the content stream
     * @param handler      interprets events caused by recognition of operations in a
     *                     content stream.
     */
    public void processContent(byte[] contentBytes, PdfDictionary resources,
                               PdfContentStreamHandler handler) {
        handler.pushContext("div class='t-extracted-page'");
        try {
            PdfContentParser ps = new PdfContentParser(new PRTokeniser(contentBytes));
            List<PdfObject> operands = new ArrayList<>();
            while (ps.parse(operands).size() > 0) {
                PdfLiteral operator = (PdfLiteral) operands.get(operands.size() - 1);
                handler.invokeOperator(operator, operands, resources);
            }
        } catch (Exception e) {
            throw new ExceptionConverter(e);
        }
        handler.popContext();
    }
}
