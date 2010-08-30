/*
 * $Id: RtfRow.java 3735 2009-02-26 01:44:03Z xlv $
 *
 * Copyright 2001, 2002, 2003, 2004, 2005 by Mark Hall
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

import com.lowagie.text.Cell;
import com.lowagie.text.DocWriter;
import com.lowagie.text.Element;
import com.lowagie.text.Row;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPRow;
import com.lowagie.text.rtf.RtfElement;
import com.lowagie.text.rtf.document.RtfDocument;


/**
 * The RtfRow wraps one Row for a RtfTable.
 * INTERNAL USE ONLY
 * 
 * @version $Id: RtfRow.java 3735 2009-02-26 01:44:03Z xlv $
 * @author Mark Hall (Mark.Hall@mail.room3b.eu)
 * @author Steffen Stundzig
 * @author Lorenz Maierhofer
 * @author Thomas Bickel (tmb99@inode.at)
 */
public class RtfRow extends RtfElement {

    /**
     * Constant for the RtfRow beginning
     */
    private static final byte[] ROW_BEGIN = DocWriter.getISOBytes("\\trowd");
    /**
     * Constant for the RtfRow width style
     */
    private static final byte[] ROW_WIDTH_STYLE = DocWriter.getISOBytes("\\trftsWidth3");
    /**
     * Constant for the RtfRow width
     */
    private static final byte[] ROW_WIDTH = DocWriter.getISOBytes("\\trwWidth");
    /**
     * Constant to specify that this RtfRow are not to be broken across pages
     */
    private static final byte[] ROW_KEEP_TOGETHER = DocWriter.getISOBytes("\\trkeep");
    /**
     * Constant to specify that this is a header RtfRow
     */
    private static final byte[] ROW_HEADER_ROW = DocWriter.getISOBytes("\\trhdr");
    /**
     * Constant for left alignment of this RtfRow
     */
    private static final byte[] ROW_ALIGN_LEFT = DocWriter.getISOBytes("\\trql");
    /**
     * Constant for right alignment of this RtfRow
     */
    private static final byte[] ROW_ALIGN_RIGHT = DocWriter.getISOBytes("\\trqr");
    /**
     * Constant for center alignment of this RtfRow
     */
    private static final byte[] ROW_ALIGN_CENTER = DocWriter.getISOBytes("\\trqc");
    /**
     * Constant for justified alignment of this RtfRow
     */
    private static final byte[] ROW_ALIGN_JUSTIFIED = DocWriter.getISOBytes("\\trqj");
    /**
     * Constant for the graph style of this RtfRow
     */
    private static final byte[] ROW_GRAPH = DocWriter.getISOBytes("\\trgaph10");
    /**
     * Constant for the cell left spacing
     */
    private static final byte[] ROW_CELL_SPACING_LEFT = DocWriter.getISOBytes("\\trspdl");
    /**
     * Constant for the cell top spacing
     */
    private static final byte[] ROW_CELL_SPACING_TOP = DocWriter.getISOBytes("\\trspdt");
    /**
     * Constant for the cell right spacing
     */
    private static final byte[] ROW_CELL_SPACING_RIGHT = DocWriter.getISOBytes("\\trspdr");
    /**
     * Constant for the cell bottom spacing
     */
    private static final byte[] ROW_CELL_SPACING_BOTTOM = DocWriter.getISOBytes("\\trspdb");
    /**
     * Constant for the cell left spacing style
     */
    private static final byte[] ROW_CELL_SPACING_LEFT_STYLE = DocWriter.getISOBytes("\\trspdfl3");
    /**
     * Constant for the cell top spacing style
     */
    private static final byte[] ROW_CELL_SPACING_TOP_STYLE = DocWriter.getISOBytes("\\trspdft3");
    /**
     * Constant for the cell right spacing style
     */
    private static final byte[] ROW_CELL_SPACING_RIGHT_STYLE = DocWriter.getISOBytes("\\trspdfr3");
    /**
     * Constant for the cell bottom spacing style
     */
    private static final byte[] ROW_CELL_SPACING_BOTTOM_STYLE = DocWriter.getISOBytes("\\trspdfb3");
    /**
     * Constant for the cell left padding
     */
    private static final byte[] ROW_CELL_PADDING_LEFT = DocWriter.getISOBytes("\\trpaddl");
    /**
     * Constant for the cell right padding
     */
    private static final byte[] ROW_CELL_PADDING_RIGHT = DocWriter.getISOBytes("\\trpaddr");
    /**
     * Constant for the cell left padding style
     */
    private static final byte[] ROW_CELL_PADDING_LEFT_STYLE = DocWriter.getISOBytes("\\trpaddfl3");
    /**
     * Constant for the cell right padding style
     */
    private static final byte[] ROW_CELL_PADDING_RIGHT_STYLE = DocWriter.getISOBytes("\\trpaddfr3");
    /**
     * Constant for the end of a row
     */
    private static final byte[] ROW_END = DocWriter.getISOBytes("\\row");

    /**
     * The RtfTable this RtfRow belongs to
     */
    private RtfTable parentTable = null;
    /**
     * The cells of this RtfRow
     */
    private ArrayList cells = null;
    /**
     * The width of this row
     */
    private int width = 0;
    /**
     * The row number
     */
    private int rowNumber = 0;
    
    /**
     * Constructs a RtfRow for a Row.
     * 
     * @param doc The RtfDocument this RtfRow belongs to
     * @param rtfTable The RtfTable this RtfRow belongs to
     * @param row The Row this RtfRow is based on
     * @param rowNumber The number of this row
     */
    protected RtfRow(RtfDocument doc, RtfTable rtfTable, Row row, int rowNumber) {
        super(doc);
        this.parentTable = rtfTable;
        this.rowNumber = rowNumber;
        importRow(row);
    }

    /**
     * Constructs a RtfRow for a Row.
     * 
     * @param doc The RtfDocument this RtfRow belongs to
     * @param rtfTable The RtfTable this RtfRow belongs to
     * @param row The Row this RtfRow is based on
     * @param rowNumber The number of this row
     * @since 2.1.3
     */
    protected RtfRow(RtfDocument doc, RtfTable rtfTable, PdfPRow row, int rowNumber) {
        super(doc);
        this.parentTable = rtfTable;
        this.rowNumber = rowNumber;
        importRow(row);
    }
    

    /**
     * Imports a Row and copies all settings
     * 
     * @param row The Row to import
     */
    private void importRow(Row row) {
        this.cells = new ArrayList();
        this.width = this.document.getDocumentHeader().getPageSetting().getPageWidth() - this.document.getDocumentHeader().getPageSetting().getMarginLeft() - this.document.getDocumentHeader().getPageSetting().getMarginRight();
        this.width = (int) (this.width * this.parentTable.getTableWidthPercent() / 100);
        
        int cellRight = 0;
        int cellWidth = 0;
        for(int i = 0; i < row.getColumns(); i++) {
            cellWidth = (int) (this.width * this.parentTable.getProportionalWidths()[i] / 100);
            cellRight = cellRight + cellWidth;
            
            Cell cell = (Cell) row.getCell(i);
            RtfCell rtfCell = new RtfCell(this.document, this, cell);
            rtfCell.setCellRight(cellRight);
            rtfCell.setCellWidth(cellWidth);
            this.cells.add(rtfCell);
        }
    }
    /**
     * Imports a PdfPRow and copies all settings
     * 
     * @param row The PdfPRow to import
     * @since 2.1.3
     */
    private void importRow(PdfPRow row) {
        this.cells = new ArrayList();
        this.width = this.document.getDocumentHeader().getPageSetting().getPageWidth() - this.document.getDocumentHeader().getPageSetting().getMarginLeft() - this.document.getDocumentHeader().getPageSetting().getMarginRight();
        this.width = (int) (this.width * this.parentTable.getTableWidthPercent() / 100);
        
        int cellRight = 0;
        int cellWidth = 0;
        PdfPCell[] cells = row.getCells();
        for(int i = 0; i < cells.length; i++) {
            cellWidth = (int) (this.width * this.parentTable.getProportionalWidths()[i] / 100);
            cellRight = cellRight + cellWidth;
            
            PdfPCell cell = cells[i];
            RtfCell rtfCell = new RtfCell(this.document, this, cell);
            rtfCell.setCellRight(cellRight);
            rtfCell.setCellWidth(cellWidth);
            this.cells.add(rtfCell);
        }
    }
    /**
     * Performs a second pass over all cells to handle cell row/column spanning.
     */
    protected void handleCellSpanning() {
        RtfCell deletedCell = new RtfCell(true);
        for(int i = 0; i < this.cells.size(); i++) {
            RtfCell rtfCell = (RtfCell) this.cells.get(i);
            if(rtfCell.getColspan() > 1) {
                int cSpan = rtfCell.getColspan();
                for(int j = i + 1; j < i + cSpan; j++) {
                    if(j < this.cells.size()) {
                        RtfCell rtfCellMerge = (RtfCell) this.cells.get(j);
                        rtfCell.setCellRight(rtfCell.getCellRight() + rtfCellMerge.getCellWidth());
                        rtfCell.setCellWidth(rtfCell.getCellWidth() + rtfCellMerge.getCellWidth());
                        this.cells.set(j, deletedCell);
                    }
                }
            }
            if(rtfCell.getRowspan() > 1) {
                ArrayList rows = this.parentTable.getRows();
                for(int j = 1; j < rtfCell.getRowspan(); j++) {
                    RtfRow mergeRow = (RtfRow) rows.get(this.rowNumber + j);
                    if(this.rowNumber + j < rows.size()) {
                        RtfCell rtfCellMerge = (RtfCell) mergeRow.getCells().get(i);
                        rtfCellMerge.setCellMergeChild(rtfCell);
                    }
                    if(rtfCell.getColspan() > 1) {
                        int cSpan = rtfCell.getColspan();
                        for(int k = i + 1; k < i + cSpan; k++) {
                            if(k < mergeRow.getCells().size()) {
                                mergeRow.getCells().set(k, deletedCell);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Cleans the deleted RtfCells from the total RtfCells.
     */
    protected void cleanRow() {
        int i = 0;
        while(i < this.cells.size()) {
            if(((RtfCell) this.cells.get(i)).isDeleted()) {
                this.cells.remove(i);
            } else {
                i++;
            }
        }
    }
    
    /**
     * Writes the row definition/settings.
     *
     * @param result The <code>OutputStream</code> to write the definitions to.
     */
    private void writeRowDefinition(final OutputStream result) throws IOException {
        result.write(ROW_BEGIN);
        this.document.outputDebugLinebreak(result);
        result.write(ROW_WIDTH_STYLE);
        result.write(ROW_WIDTH);
        result.write(intToByteArray(this.width));
        if(this.parentTable.getCellsFitToPage()) {
            result.write(ROW_KEEP_TOGETHER);
        }
        if(this.rowNumber <= this.parentTable.getHeaderRows()) {
            result.write(ROW_HEADER_ROW);
        }
        switch (this.parentTable.getAlignment()) {
            case Element.ALIGN_LEFT:
            	result.write(ROW_ALIGN_LEFT);
                break;
            case Element.ALIGN_RIGHT:
                result.write(ROW_ALIGN_RIGHT);
                break;
            case Element.ALIGN_CENTER:
                result.write(ROW_ALIGN_CENTER);
                break;
            case Element.ALIGN_JUSTIFIED:
            case Element.ALIGN_JUSTIFIED_ALL:
                result.write(ROW_ALIGN_JUSTIFIED);
                break;
        }
        result.write(ROW_GRAPH);
        RtfBorderGroup borders =this.parentTable.getBorders();
        if(borders != null) {
        	borders.writeContent(result);
        }
        
        if(this.parentTable.getCellSpacing() > 0) {
            result.write(ROW_CELL_SPACING_LEFT);
            result.write(intToByteArray((int) (this.parentTable.getCellSpacing() / 2)));
            result.write(ROW_CELL_SPACING_LEFT_STYLE);
            result.write(ROW_CELL_SPACING_TOP);
            result.write(intToByteArray((int) (this.parentTable.getCellSpacing() / 2)));
            result.write(ROW_CELL_SPACING_TOP_STYLE);
            result.write(ROW_CELL_SPACING_RIGHT);
            result.write(intToByteArray((int) (this.parentTable.getCellSpacing() / 2)));
            result.write(ROW_CELL_SPACING_RIGHT_STYLE);
            result.write(ROW_CELL_SPACING_BOTTOM);
            result.write(intToByteArray((int) (this.parentTable.getCellSpacing() / 2)));
            result.write(ROW_CELL_SPACING_BOTTOM_STYLE);
        }
        
        result.write(ROW_CELL_PADDING_LEFT);
        result.write(intToByteArray((int) (this.parentTable.getCellPadding() / 2)));
        result.write(ROW_CELL_PADDING_RIGHT);
        result.write(intToByteArray((int) (this.parentTable.getCellPadding() / 2)));
        result.write(ROW_CELL_PADDING_LEFT_STYLE);
        result.write(ROW_CELL_PADDING_RIGHT_STYLE);
        
        this.document.outputDebugLinebreak(result);
        
        for(int i = 0; i < this.cells.size(); i++) {
            RtfCell rtfCell = (RtfCell) this.cells.get(i);
            rtfCell.writeDefinition(result);
        }    	
    }
    
    /**
     * Writes the content of this RtfRow
     */    
    public void writeContent(final OutputStream result) throws IOException
    {
    	writeRowDefinition(result);
        
        for(int i = 0; i < this.cells.size(); i++) {
            RtfCell rtfCell = (RtfCell) this.cells.get(i);
            rtfCell.writeContent(result);
        }

        result.write(DELIMITER);

        if(this.document.getDocumentSettings().isOutputTableRowDefinitionAfter()) {
        	writeRowDefinition(result);
        }

        result.write(ROW_END);
        this.document.outputDebugLinebreak(result);
    }        
    
    /**
     * Gets the parent RtfTable of this RtfRow
     * 
     * @return The parent RtfTable of this RtfRow
     */
    protected RtfTable getParentTable() {
        return this.parentTable;
    }
    
    /**
     * Gets the cells of this RtfRow
     * 
     * @return The cells of this RtfRow
     */
    protected ArrayList getCells() {
        return this.cells;
    }
}
