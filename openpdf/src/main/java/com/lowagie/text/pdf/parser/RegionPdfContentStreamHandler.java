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

import com.lowagie.text.pdf.PdfArray;
import com.lowagie.text.pdf.PdfDictionary;
import com.lowagie.text.pdf.PdfLiteral;
import com.lowagie.text.pdf.PdfNumber;
import com.lowagie.text.pdf.PdfObject;
import com.lowagie.text.pdf.PdfString;

import java.awt.Rectangle;
import java.util.List;

/**
 * Stream handler for reading a specific part of a PDF document.
 *
 * @since 2.1.4
 */
public class RegionPdfContentStreamHandler extends PdfContentStreamHandler {
    private Rectangle boundingBox;

    public RegionPdfContentStreamHandler(TextAssembler renderListener) {
        super(renderListener);
    }

    /**
     * Sets the bounding box to filter text extraction. If boundingBox is null,
     * no filtering will be applied.
     * 0,0 is in the bottom left part of the page.
     */
    public void setBoundingBox(float x, float y, float width, float height) {
        this.boundingBox = new Rectangle((int) x, (int) y, (int) width, (int) height);
    }

    /**
     * Overrides the invokeOperator method to include bounding box checking.
     */
    @Override
    public void invokeOperator(PdfLiteral operator, List<PdfObject> operands, PdfDictionary resources) {
        String op = operator.toString();

        if ("Tj".equals(op)) {
            // Single text string to show
            PdfString text = (PdfString) operands.get(0);
            float[] textPosition = getCurrentTextPosition();
            if (boundingBox == null || isWithinBoundingBox(textPosition[0], textPosition[1])) {
                displayPdfString(text);
            }
        } else if ("TJ".equals(op)) {
            // Array of text strings and positioning adjustments
            PdfArray array = (PdfArray) operands.get(0);
            float[] textPosition = getCurrentTextPosition();
            if (boundingBox == null || isWithinBoundingBox(textPosition[0], textPosition[1])) {
                for (PdfObject obj : array.getElements()) {
                    if (obj instanceof PdfString) {
                        displayPdfString((PdfString) obj);
                    } else if (obj instanceof PdfNumber) {
                        applyTextAdjust(((PdfNumber) obj).floatValue());
                    }
                }
            }
        } else {
            // Handle other operators as before
            super.invokeOperator(operator, operands, resources);
        }
    }

    /**
     * Checks if a text element is within the bounding box.
     */
    private boolean isWithinBoundingBox(float textX, float textY) {
        return boundingBox != null && boundingBox.contains(textX, textY);
    }

    /**
     * Method to get the current text position based on the text matrix.
     */
    private float[] getCurrentTextPosition() {
        Matrix textMatrix = getCurrentTextMatrix();
        return new float[]{textMatrix.get(Matrix.I31), textMatrix.get(Matrix.I32)};
    }
}