/*
 * $Id: PdfImportedPage.java 4065 2009-09-16 23:09:11Z psoares33 $
 *
 * Copyright 2001, 2002 Paulo Soares
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

package com.lowagie.text.pdf;
import java.io.IOException;
import com.lowagie.text.error_messages.MessageLocalization;

import com.lowagie.text.DocumentException;
import com.lowagie.text.Image;

/** Represents an imported page.
 *
 * @author Paulo Soares (psoares@consiste.pt)
 */
public class PdfImportedPage extends com.lowagie.text.pdf.PdfTemplate {

    PdfReaderInstance readerInstance;
    int pageNumber;
    
    PdfImportedPage(PdfReaderInstance readerInstance, PdfWriter writer, int pageNumber) {
        this.readerInstance = readerInstance;
        this.pageNumber = pageNumber;
        this.writer = writer;
        bBox = readerInstance.getReader().getPageSize(pageNumber);
        setMatrix(1, 0, 0, 1, -bBox.getLeft(), -bBox.getBottom());
        type = TYPE_IMPORTED;
    }

    /** Reads the content from this <CODE>PdfImportedPage</CODE>-object from a reader.
     *
     * @return self
     *
     */
    public PdfImportedPage getFromReader() {
      return this;
    }

    public int getPageNumber() {
        return pageNumber;
    }


    /** Always throws an error. This operation is not allowed.
     * @param image dummy
     * @param a dummy
     * @param b dummy
     * @param c dummy
     * @param d dummy
     * @param e dummy
     * @param f dummy
     * @throws DocumentException  dummy */    
    public void addImage(Image image, float a, float b, float c, float d, float e, float f) throws DocumentException {
        throwError();
    }
    
    /** Always throws an error. This operation is not allowed.
     * @param template dummy
     * @param a dummy
     * @param b dummy
     * @param c dummy
     * @param d dummy
     * @param e dummy
     * @param f  dummy */    
    public void addTemplate(PdfTemplate template, float a, float b, float c, float d, float e, float f) {
        throwError();
    }
    
    /** Always throws an error. This operation is not allowed.
     * @return  dummy */    
    public PdfContentByte getDuplicate() {
        throwError();
        return null;
    }

    /**
     * Gets the stream representing this page.
     *
     * @param	compressionLevel	the compressionLevel
     * @return the stream representing this page
     * @since	2.1.3	(replacing the method without param compressionLevel)
     */
    PdfStream getFormXObject(int compressionLevel) throws IOException {
         return readerInstance.getFormXObject(pageNumber, compressionLevel);
    }
    
    public void setColorFill(PdfSpotColor sp, float tint) {
        throwError();
    }
    
    public void setColorStroke(PdfSpotColor sp, float tint) {
        throwError();
    }
    
    PdfObject getResources() {
        return readerInstance.getResources(pageNumber);
    }
    
    /** Always throws an error. This operation is not allowed.
     * @param bf dummy
     * @param size dummy */    
    public void setFontAndSize(BaseFont bf, float size) {
        throwError();
    }
    
    /**
     * Always throws an error. This operation is not allowed.
     * @param group New value of property group.
     * @since	2.1.6
     */ 
    public void setGroup(PdfTransparencyGroup group) {
        throwError();
	}

	void throwError() {
        throw new RuntimeException(MessageLocalization.getComposedMessage("content.can.not.be.added.to.a.pdfimportedpage"));
    }
    
    PdfReaderInstance getPdfReaderInstance() {
        return readerInstance;
    }
}
