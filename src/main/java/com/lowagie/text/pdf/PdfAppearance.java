/*
 * Copyright 2002 Paulo Soares
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
import java.util.HashMap;

import com.lowagie.text.Rectangle;

/**
 * Implements the appearance stream to be used with form fields..
 */

public class PdfAppearance extends PdfTemplate {
    
    public static final HashMap stdFieldFontNames = new HashMap();
    static {
        stdFieldFontNames.put("Courier-BoldOblique", new PdfName("CoBO"));
        stdFieldFontNames.put("Courier-Bold", new PdfName("CoBo"));
        stdFieldFontNames.put("Courier-Oblique", new PdfName("CoOb"));
        stdFieldFontNames.put("Courier", new PdfName("Cour"));
        stdFieldFontNames.put("Helvetica-BoldOblique", new PdfName("HeBO"));
        stdFieldFontNames.put("Helvetica-Bold", new PdfName("HeBo"));
        stdFieldFontNames.put("Helvetica-Oblique", new PdfName("HeOb"));
        stdFieldFontNames.put("Helvetica", PdfName.HELV);
        stdFieldFontNames.put("Symbol", new PdfName("Symb"));
        stdFieldFontNames.put("Times-BoldItalic", new PdfName("TiBI"));
        stdFieldFontNames.put("Times-Bold", new PdfName("TiBo"));
        stdFieldFontNames.put("Times-Italic", new PdfName("TiIt"));
        stdFieldFontNames.put("Times-Roman", new PdfName("TiRo"));
        stdFieldFontNames.put("ZapfDingbats", PdfName.ZADB);
        stdFieldFontNames.put("HYSMyeongJo-Medium", new PdfName("HySm"));
        stdFieldFontNames.put("HYGoThic-Medium", new PdfName("HyGo"));
        stdFieldFontNames.put("HeiseiKakuGo-W5", new PdfName("KaGo"));
        stdFieldFontNames.put("HeiseiMin-W3", new PdfName("KaMi"));
        stdFieldFontNames.put("MHei-Medium", new PdfName("MHei"));
        stdFieldFontNames.put("MSung-Light", new PdfName("MSun"));
        stdFieldFontNames.put("STSong-Light", new PdfName("STSo"));
        stdFieldFontNames.put("MSungStd-Light", new PdfName("MSun"));
        stdFieldFontNames.put("STSongStd-Light", new PdfName("STSo"));
        stdFieldFontNames.put("HYSMyeongJoStd-Medium", new PdfName("HySm"));
        stdFieldFontNames.put("KozMinPro-Regular", new PdfName("KaMi"));
    }
    
    /**
     *Creates a <CODE>PdfAppearance</CODE>.
     */
    
    PdfAppearance() {
        super();
        separator = ' ';
    }
    
    PdfAppearance(PdfIndirectReference iref) {
        thisReference = iref;
    }
    
    /**
     * Creates new PdfTemplate
     *
     * @param wr the <CODE>PdfWriter</CODE>
     */
    
    PdfAppearance(PdfWriter wr) {
        super(wr);
        separator = ' ';
    }
    
    /**
     * Creates a new appearance to be used with form fields.
     *
     * @param writer the PdfWriter to use
     * @param width the bounding box width
     * @param height the bounding box height
     * @return the appearance created
     */
    public static PdfAppearance createAppearance(PdfWriter writer, float width, float height) {
        return createAppearance(writer, width, height, null);
    }
    
    static PdfAppearance createAppearance(PdfWriter writer, float width, float height, PdfName forcedName) {
        PdfAppearance template = new PdfAppearance(writer);
        template.setWidth(width);
        template.setHeight(height);
        writer.addDirectTemplateSimple(template, forcedName);
        return template;
    }

    /**
     * Set the font and the size for the subsequent text writing.
     *
     * @param bf the font
     * @param size the font size in points
     */
    public void setFontAndSize(BaseFont bf, float size) {
        checkWriter();
        state.size = size;
        if (bf.getFontType() == BaseFont.FONT_TYPE_DOCUMENT) {
            state.fontDetails = new FontDetails(null, ((DocumentFont)bf).getIndirectReference(), bf);
        }
        else
            state.fontDetails = writer.addSimple(bf);
        PdfName psn = (PdfName)stdFieldFontNames.get(bf.getPostscriptFontName());
        if (psn == null) {
            if (bf.isSubset() && bf.getFontType() == BaseFont.FONT_TYPE_TTUNI)
                psn = state.fontDetails.getFontName();
            else {
                psn = new PdfName(bf.getPostscriptFontName());
                state.fontDetails.setSubset(false);
            }
        }
        PageResources prs = getPageResources();
//        PdfName name = state.fontDetails.getFontName();
        prs.addFont(psn, state.fontDetails.getIndirectReference());
        content.append(psn.getBytes()).append(' ').append(size).append(" Tf").append_i(separator);
    }

    public PdfContentByte getDuplicate() {
        PdfAppearance tpl = new PdfAppearance();
        tpl.writer = writer;
        tpl.pdf = pdf;
        tpl.thisReference = thisReference;
        tpl.pageResources = pageResources;
        tpl.bBox = new Rectangle(bBox);
        tpl.group = group;
        tpl.layer = layer;
        if (matrix != null) {
            tpl.matrix = new PdfArray(matrix);
        }
        tpl.separator = separator;
        return tpl;
    }
}