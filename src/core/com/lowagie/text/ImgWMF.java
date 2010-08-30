/*
 * $Id: ImgWMF.java 4065 2009-09-16 23:09:11Z psoares33 $
 * $Name$
 *
 * Copyright 1999, 2000, 2001, 2002 by Paulo Soares.
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
 * the Initial Developer are Copyright (C) 1999, 2000, 2001, 2002 by Bruno Lowagie.
 * All Rights Reserved.
 * Co-Developer of the code is Paulo Soares. Portions created by the Co-Developer
 * are Copyright (C) 2000, 2001, 2002 by Paulo Soares. All Rights Reserved.
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

package com.lowagie.text;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import com.lowagie.text.error_messages.MessageLocalization;

import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.codec.wmf.InputMeta;
import com.lowagie.text.pdf.codec.wmf.MetaDo;

/**
 * An <CODE>ImgWMF</CODE> is the representation of a windows metafile
 * that has to be inserted into the document
 *
 * @see		Element
 * @see		Image
 */

public class ImgWMF extends Image {
    
    // Constructors
    
    ImgWMF(Image image) {
        super(image);
    }
    
    /**
     * Constructs an <CODE>ImgWMF</CODE>-object, using an <VAR>url</VAR>.
     *
     * @param url the <CODE>URL</CODE> where the image can be found
     * @throws BadElementException on error
     * @throws IOException on error
     */
    
    public ImgWMF(URL url) throws BadElementException, IOException {
        super(url);
        processParameters();
    }
    
    /**
     * Constructs an <CODE>ImgWMF</CODE>-object, using a <VAR>filename</VAR>.
     *
     * @param filename a <CODE>String</CODE>-representation of the file that contains the image.
     * @throws BadElementException on error
     * @throws MalformedURLException on error
     * @throws IOException on error
     */
    
    public ImgWMF(String filename) throws BadElementException, MalformedURLException, IOException {
        this(Utilities.toURL(filename));
    }
    
    /**
     * Constructs an <CODE>ImgWMF</CODE>-object from memory.
     *
     * @param img the memory image
     * @throws BadElementException on error
     * @throws IOException on error
     */
    
    public ImgWMF(byte[] img) throws BadElementException, IOException {
        super((URL)null);
        rawData = img;
        originalData = img;
        processParameters();
    }
    
/**
 * This method checks if the image is a valid WMF and processes some parameters.
 * @throws BadElementException
 * @throws IOException
 */
    
    private void processParameters() throws BadElementException, IOException {
        type = IMGTEMPLATE;
        originalType = ORIGINAL_WMF;
        InputStream is = null;
        try {
            String errorID;
            if (rawData == null){
                is = url.openStream();
                errorID = url.toString();
            }
            else{
                is = new java.io.ByteArrayInputStream(rawData);
                errorID = "Byte array";
            }
            InputMeta in = new InputMeta(is);
            if (in.readInt() != 0x9AC6CDD7)	{
                throw new BadElementException(MessageLocalization.getComposedMessage("1.is.not.a.valid.placeable.windows.metafile", errorID));
            }
            in.readWord();
            int left = in.readShort();
            int top = in.readShort();
            int right = in.readShort();
            int bottom = in.readShort();
            int inch = in.readWord();
            dpiX = 72;
            dpiY = 72;
            scaledHeight = (float)(bottom - top) / inch * 72f;
            setTop(scaledHeight);
            scaledWidth = (float)(right - left) / inch * 72f;
            setRight(scaledWidth);
        }
        finally {
            if (is != null) {
                is.close();
            }
            plainWidth = getWidth();
            plainHeight = getHeight();
        }
    }
    
    /** Reads the WMF into a template.
     * @param template the template to read to
     * @throws IOException on error
     * @throws DocumentException on error
     */    
    public void readWMF(PdfTemplate template) throws IOException, DocumentException {
        setTemplateData(template);
        template.setWidth(getWidth());
        template.setHeight(getHeight());
        InputStream is = null;
        try {
            if (rawData == null){
                is = url.openStream();
            }
            else{
                is = new java.io.ByteArrayInputStream(rawData);
            }
            MetaDo meta = new MetaDo(is, template);
            meta.readAll();
        }
        finally {
            if (is != null) {
                is.close();
            }
        }
    }
}
