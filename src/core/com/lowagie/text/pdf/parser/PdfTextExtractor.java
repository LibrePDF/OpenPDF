/*
 * Copyright 2008 by Kevin Day.
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ListIterator;

import com.lowagie.text.pdf.PRIndirectReference;
import com.lowagie.text.pdf.PRStream;
import com.lowagie.text.pdf.PdfArray;
import com.lowagie.text.pdf.PdfDictionary;
import com.lowagie.text.pdf.PdfName;
import com.lowagie.text.pdf.PdfObject;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.RandomAccessFileOrArray;

/**
 * Extracts text from a PDF file.
 * @since	2.1.4
 */
public class PdfTextExtractor {

	/** The PdfReader that holds the PDF file. */
    private final PdfReader reader;
    
    /** The {@link TextProvidingRenderListener} that will receive render notifications and provide resultant text */
    private final TextProvidingRenderListener renderListener;
    
    /**
     * Creates a new Text Extractor object, using a {@link SimpleTextExtractingPdfContentRenderListener} as the render listener
     * @param reader	the reader with the PDF
     */
    public PdfTextExtractor(PdfReader reader) {
        this(reader, new SimpleTextExtractingPdfContentRenderListener());
    }

    /**
     * Creates a new Text Extractor object.
     * @param reader    the reader with the PDF
     * @param renderListener the render listener that will be used to analyze renderText operations and provide resultant text
     */
    public PdfTextExtractor(PdfReader reader, TextProvidingRenderListener renderListener) {
        this.reader = reader;
        this.renderListener = renderListener;
    }
    
    /**
     * Gets the content bytes of a page.
     * @param pageNum	the page number of page you want get the content stream from
     * @return	a byte array with the effective content stream of a page
     * @throws IOException
     */
    private byte[] getContentBytesForPage(int pageNum) throws IOException {
        RandomAccessFileOrArray f = reader.getSafeFile();
        try{
            final PdfDictionary pageDictionary = reader.getPageN(pageNum);
            final PdfObject contentObject = pageDictionary.get(PdfName.CONTENTS);
            final byte[] contentBytes = getContentBytesFromContentObject(contentObject);
            return contentBytes;
        } finally {    
            f.close();
        }
    }
    
    /**
     * Gets the content bytes from a content object, which may be a reference
     * a stream or an array.
     * @param contentObject the object to read bytes from
     * @return the content bytes
     * @throws IOException
     */
    private byte[] getContentBytesFromContentObject(final PdfObject contentObject) throws IOException {
          final byte[] result;
          switch (contentObject.type())
          {
            case PdfObject.INDIRECT:
              final PRIndirectReference ref = (PRIndirectReference) contentObject;
              final PdfObject directObject = PdfReader.getPdfObject(ref);
              result = getContentBytesFromContentObject(directObject);
              break;
            case PdfObject.STREAM:
              final PRStream stream = (PRStream) PdfReader.getPdfObject(contentObject);
              result = PdfReader.getStreamBytes(stream);
              break;
            case PdfObject.ARRAY:
              // Stitch together all content before calling processContent(), because
              // processContent() resets state.
              final ByteArrayOutputStream allBytes = new ByteArrayOutputStream();
              final PdfArray contentArray = (PdfArray) contentObject;
              final ListIterator iter = contentArray.listIterator();
              while (iter.hasNext())
              {
                final PdfObject element = (PdfObject) iter.next();
                allBytes.write(getContentBytesFromContentObject(element));
              }
              result = allBytes.toByteArray();
              break;
            default:
              final String msg = "Unable to handle Content of type " + contentObject.getClass();
              throw new IllegalStateException(msg);
          }
          return result;
        }    
    
    /**
     * Gets the text from a page.
     * @param page	the page number of the page
     * @return	a String with the content as plain text (without PDF syntax)
     * @throws IOException
     */
    public String getTextFromPage(int page) throws IOException {
        PdfDictionary pageDic = reader.getPageN(page);
        PdfDictionary resourcesDic = pageDic.getAsDict(PdfName.RESOURCES);
        
        renderListener.reset();
        PdfContentStreamProcessor processor = new PdfContentStreamProcessor(renderListener);
        processor.processContent(getContentBytesForPage(page), resourcesDic);        
        return renderListener.getResultantText();
    }
}
