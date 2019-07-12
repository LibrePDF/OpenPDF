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
 * http://www.lowagie.com/iText/
 */

package com.lowagie.text.html.simpleparser;

import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author  psoares
 */
public class IncTable {
    private Map<String, String> props = new HashMap<>();
    private List<List<PdfPCell>> rows = new ArrayList<>();
    private List<PdfPCell> cols;
    /** Creates a new instance of IncTable */

    @Deprecated
    @SuppressWarnings("unchecked")
    public IncTable(HashMap props) {
        this.props.putAll(props);
    }

    public IncTable(Map<String, String> props) {
        this.props.putAll(props);
    }
    
    public void addCol(PdfPCell cell) {
        if (cols == null)
            cols = new ArrayList<>();
        cols.add(cell);
    }

    @Deprecated
    @SuppressWarnings("unchecked")
    public void addCols(ArrayList ncols) {
        if (cols == null)
            cols = (List<PdfPCell>)ncols;
        else
            cols.addAll(ncols);
    }
    
    public void addCols(List<PdfPCell> ncols) {
        if (cols == null)
            cols = new ArrayList<>(ncols);
        else
            cols.addAll(ncols);
    }
    
    public void endRow() {
        if (cols != null) {
            Collections.reverse(cols);
            rows.add(cols);
            cols = null;
        }
    }

    /**
     * @deprecated use {@link #getTableRows()}
     */
    @Deprecated
    public ArrayList getRows() {
        return (ArrayList) rows;
    }
    
    public List<List<PdfPCell>> getTableRows() {
        return rows;
    }
    
    public PdfPTable buildTable() {
        if (rows.isEmpty())
            return new PdfPTable(1);
        int ncol = 0;
        for (PdfPCell pCell : rows.get(0)) {
            ncol += pCell.getColspan();
        }
        PdfPTable table = new PdfPTable(ncol);
        String width = props.get("width");
        if (width == null)
            table.setWidthPercentage(100);
        else {
            if (width.endsWith("%"))
                table.setWidthPercentage(Float.parseFloat(width.substring(0, width.length() - 1)));
            else {
                table.setTotalWidth(Float.parseFloat(width));
                table.setLockedWidth(true);
            }
        }
        for (List<PdfPCell> col : rows) {
            for (PdfPCell pdfPCell : col) {
                table.addCell(pdfPCell);
            }
        }
        return table;
    }
}
