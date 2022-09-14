/*
 * Copyright 2004 Paulo Soares
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
 * https://github.com/LibrePDF/OpenPDF
 */

package com.lowagie.text.html.simpleparser;

import com.lowagie.text.Element;
import com.lowagie.text.ElementListener;
import com.lowagie.text.Phrase;
import com.lowagie.text.TextElementArray;
import com.lowagie.text.html.Markup;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.utils.NumberUtilities;
import java.util.ArrayList;
import java.util.List;


/**
 * @author psoares
 */
public class IncCell implements TextElementArray {

    private final PdfPCell cell;
    private final List<Element> chunks = new ArrayList<>();

    /**
     * Creates a new instance of IncCell
     * @param tag the tag
     * @param props the ChainedProperties
     */
    public IncCell(String tag, ChainedProperties props) {
        cell = new PdfPCell((Phrase) null);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);

        props.findProperty("colspan")
                .flatMap(NumberUtilities::parseInt)
                .ifPresent(cell::setColspan);

        props.findProperty("rowspan")
                .flatMap(NumberUtilities::parseInt)
                .ifPresent(cell::setRowspan);

        if (tag.equals("th")) {
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        }

        props.findProperty("align")
                .ifPresent(align -> {
                    if ("center".equalsIgnoreCase(align)) {
                        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    } else if ("right".equalsIgnoreCase(align)) {
                        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    } else if ("left".equalsIgnoreCase(align))
                        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                    else if ("justify".equalsIgnoreCase(align))
                        cell.setHorizontalAlignment(Element.ALIGN_JUSTIFIED);
                });

        props.findProperty("valign")
                .ifPresent(valign -> {
                    if ("top".equalsIgnoreCase(valign)) {
                        cell.setVerticalAlignment(Element.ALIGN_TOP);
                    } else if ("bottom".equalsIgnoreCase(valign)) {
                        cell.setVerticalAlignment(Element.ALIGN_BOTTOM);
                    }
                });

        float border = props.findProperty("border")
                .flatMap(NumberUtilities::parseFloat)
                .orElse(0f);
        cell.setBorderWidth(border);

        props.findProperty("cellpadding")
                .flatMap(NumberUtilities::parseFloat)
                .ifPresent(cell::setPadding);

        cell.setUseDescender(true);
        cell.setBackgroundColor(Markup.decodeColor(props.getProperty("bgcolor")));
    }

    @Override
    public boolean add(Element o) {
        cell.addElement(o);
        return true;
    }

    @Override
    public ArrayList<Element> getChunks() {
        return (ArrayList<Element>) chunks;
    }

    @Override
    public boolean process(ElementListener listener) {
        return true;
    }

    @Override
    public int type() {
        return Element.RECTANGLE;
    }

    public PdfPCell getCell() {
        return cell;
    }

    /**
     * @see com.lowagie.text.Element#isContent()
     * @since iText 2.0.8
     */
    @Override
    public boolean isContent() {
        return true;
    }

    /**
     * @see com.lowagie.text.Element#isNestable()
     * @since iText 2.0.8
     */
    @Override
    public boolean isNestable() {
        return true;
    }
}
