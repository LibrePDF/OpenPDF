/*
 * $Id: RtfTable.java 3533 2008-07-07 21:27:13Z Howard_s $
 *
 * Copyright 2001, 2002, 2003, 2004 by Mark Hall
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
 * LGPL license (the ?GNU LIBRARY GENERAL PUBLIC LICENSE?), in which case the
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

package com.lowagie.text.rtf.table;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;

import com.lowagie.text.Element;
import com.lowagie.text.Row;
import com.lowagie.text.Table;
import com.lowagie.text.pdf.PdfPRow;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.rtf.RtfElement;
import com.lowagie.text.rtf.document.RtfDocument;
import com.lowagie.text.rtf.style.RtfFont;
import com.lowagie.text.rtf.text.RtfParagraph;


/**
 * The RtfTable wraps a Table.
 * INTERNAL USE ONLY
 * 
 * @version $Id: RtfTable.java 3533 2008-07-07 21:27:13Z Howard_s $
 * @author Mark Hall (Mark.Hall@mail.room3b.eu)
 * @author Steffen Stundzig
 * @author Benoit Wiart
 * @author Thomas Bickel (tmb99@inode.at)
 */
public class RtfTable extends RtfElement {

    /**
     * The rows of this RtfTable
     */
    private ArrayList rows = null;
    /**
     * The percentage of the page width that this RtfTable covers
     */
    private float tableWidthPercent = 80;
    /**
     * An array with the proportional widths of the cells in each row
     */
    private float[] proportionalWidths = null;
    /**
     * The cell padding
     */
    private float cellPadding = 0;
    /**
     * The cell spacing
     */
    private float cellSpacing = 0;
    
    /**
     * The border style of this RtfTable 
     */
    private RtfBorderGroup borders = null;
    /**
     * The alignment of this RtfTable
     */
    private int alignment = Element.ALIGN_CENTER;
    /**
     * Whether the cells in this RtfTable must fit in a page
     */
    private boolean cellsFitToPage = false;
    /**
     * Whether the whole RtfTable must fit in a page
     */
    private boolean tableFitToPage = false;
    /**
     * The number of header rows in this RtfTable
     */
    private int headerRows = 0;
    /**
     * The offset from the previous text
     */
    private int offset = -1;
    
    /**
     * Constructs a RtfTable based on a Table for a RtfDocument.
     * 
     * @param doc The RtfDocument this RtfTable belongs to
     * @param table The Table that this RtfTable wraps
     */
    public RtfTable(RtfDocument doc, Table table) {
        super(doc);
        table.complete();
        importTable(table);
    }

    /**
     * Constructs a RtfTable based on a PdfTable for a RtfDocument.
     * 
     * @param doc The RtfDocument this RtfTable belongs to
     * @param table The PdfPTable that this RtfTable wraps
     * @since 2.1.3
     */
    public RtfTable(RtfDocument doc, PdfPTable table) {
        super(doc);
        importTable(table);
    }
    /**
     * Imports the rows and settings from the Table into this
     * RtfTable.
     * 
     * @param table The source Table
     */
    private void importTable(Table table) {
        this.rows = new ArrayList();
        this.tableWidthPercent = table.getWidth();
        this.proportionalWidths = table.getProportionalWidths();
        this.cellPadding = (float) (table.getPadding() * TWIPS_FACTOR);
        this.cellSpacing = (float) (table.getSpacing() * TWIPS_FACTOR);
        this.borders = new RtfBorderGroup(this.document, RtfBorder.ROW_BORDER, table.getBorder(), table.getBorderWidth(), table.getBorderColor());
        this.alignment = table.getAlignment();
        
        int i = 0;
        Iterator rowIterator = table.iterator();
        while(rowIterator.hasNext()) {
            this.rows.add(new RtfRow(this.document, this, (Row) rowIterator.next(), i));
            i++;
        }
        for(i = 0; i < this.rows.size(); i++) {
            ((RtfRow) this.rows.get(i)).handleCellSpanning();
            ((RtfRow) this.rows.get(i)).cleanRow();
        }
        this.headerRows = table.getLastHeaderRow();
        this.cellsFitToPage = table.isCellsFitPage();
        this.tableFitToPage = table.isTableFitsPage();
        if(!Float.isNaN(table.getOffset())) {
            this.offset = (int) (table.getOffset() * 2);
        }
    }

    /**
     * Imports the rows and settings from the Table into this
     * RtfTable.
     * 
     * @param table The source PdfPTable
     * @since 2.1.3
     */
    private void importTable(PdfPTable table) {
        this.rows = new ArrayList();
        this.tableWidthPercent = table.getWidthPercentage();
//        this.tableWidthPercent = table.getWidth();
        this.proportionalWidths = table.getAbsoluteWidths();
//        this.proportionalWidths = table.getProportionalWidths();
        this.cellPadding = (float) (table.spacingAfter() * TWIPS_FACTOR);
//        this.cellPadding = (float) (table.getPadding() * TWIPS_FACTOR);
        this.cellSpacing = (float) (table.spacingAfter() * TWIPS_FACTOR);
//        this.cellSpacing = (float) (table.getSpacing() * TWIPS_FACTOR);
//        this.borders = new RtfBorderGroup(this.document, RtfBorder.ROW_BORDER, table.getBorder(), table.getBorderWidth(), table.getBorderColor());
//        this.borders = new RtfBorderGroup(this.document, RtfBorder.ROW_BORDER, table.getBorder(), table.getBorderWidth(), table.getBorderColor());
        this.alignment = table.getHorizontalAlignment();
//        this.alignment = table.getAlignment();
        
        int i = 0;
        Iterator rowIterator = table.getRows().iterator();
//        Iterator rowIterator = table.iterator();
        while(rowIterator.hasNext()) {
            this.rows.add(new RtfRow(this.document, this, (PdfPRow) rowIterator.next(), i));
            i++;
        }
        for(i = 0; i < this.rows.size(); i++) {
            ((RtfRow) this.rows.get(i)).handleCellSpanning();
            ((RtfRow) this.rows.get(i)).cleanRow();
        }
        
        this.headerRows = table.getHeaderRows();
//        this.headerRows = table.getLastHeaderRow();
        this.cellsFitToPage = table.getKeepTogether();
//        this.cellsFitToPage = table.isCellsFitPage();
        this.tableFitToPage = table.getKeepTogether();
//        this.tableFitToPage = table.isTableFitsPage();
//        if(!Float.isNaN(table.getOffset())) {
//            this.offset = (int) (table.getOffset() * 2);
//        }
//        if(!Float.isNaN(table.getOffset())) {
//            this.offset = (int) (table.getOffset() * 2);
//        }
    }
    
    /**
     * Writes the content of this RtfTable
     */    
    public void writeContent(final OutputStream result) throws IOException
    {
        if(!inHeader) {
            if(this.offset != -1) {
                result.write(RtfFont.FONT_SIZE);
                result.write(intToByteArray(this.offset));
            }
            result.write(RtfParagraph.PARAGRAPH);
        }
        
        for(int i = 0; i < this.rows.size(); i++) {
        	RtfElement re = (RtfElement)this.rows.get(i);
            //.result.write(re.write());
        	re.writeContent(result);
        }
        
        result.write(RtfParagraph.PARAGRAPH_DEFAULTS);
    }        
    
    /**
     * Gets the alignment of this RtfTable
     * 
     * @return The alignment of this RtfTable.
     */
    protected int getAlignment() {
        return alignment;
    }
    
    /**
     * Gets the borders of this RtfTable
     * 
     * @return The borders of this RtfTable.
     */
    protected RtfBorderGroup getBorders() {
        return this.borders;
    }
    
    /**
     * Gets the cell padding of this RtfTable
     * 
     * @return The cell padding of this RtfTable.
     */
    protected float getCellPadding() {
        return cellPadding;
    }
    
    /**
     * Gets the cell spacing of this RtfTable
     * 
     * @return The cell spacing of this RtfTable.
     */
    protected float getCellSpacing() {
        return cellSpacing;
    }
    
    /**
     * Gets the proportional cell widths of this RtfTable
     * 
     * @return The proportional widths of this RtfTable.
     */
    protected float[] getProportionalWidths() {
        return (float[]) proportionalWidths.clone();
    }
    
    /**
     * Gets the percentage of the page width this RtfTable covers 
     * 
     * @return The percentage of the page width.
     */
    protected float getTableWidthPercent() {
        return tableWidthPercent;
    }
    
    /**
     * Gets the rows of this RtfTable
     * 
     * @return The rows of this RtfTable
     */
    protected ArrayList getRows() {
        return this.rows;
    }
    
    /**
     * Gets the cellsFitToPage setting of this RtfTable.
     * 
     * @return The cellsFitToPage setting of this RtfTable.
     */
    protected boolean getCellsFitToPage() {
        return this.cellsFitToPage;
    }
    
    /**
     * Gets the tableFitToPage setting of this RtfTable.
     * 
     * @return The tableFitToPage setting of this RtfTable.
     */
    protected boolean getTableFitToPage() {
        return this.tableFitToPage;
    }
    
    /**
     * Gets the number of header rows of this RtfTable
     * 
     * @return The number of header rows
     */
    protected int getHeaderRows() {
        return this.headerRows;
    }
}
