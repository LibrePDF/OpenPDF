/*
 * $Id: PdfPTableEvent.java 3373 2008-05-12 16:21:24Z xlv $
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

/** An interface that can be used to retrieve the position of cells in <CODE>PdfPTable</CODE>.
 *
 * @author Paulo Soares (psoares@consiste.pt)
 */
public interface PdfPTableEvent {
    
    /** This method is called at the end of the table rendering. The text or graphics are added to
     * one of the 4 <CODE>PdfContentByte</CODE> contained in
     * <CODE>canvases</CODE>.<br>
     * The indexes to <CODE>canvases</CODE> are:<p>
     * <ul>
     * <li><CODE>PdfPTable.BASECANVAS</CODE> - the original <CODE>PdfContentByte</CODE>. Anything placed here
     * will be under the table.
     * <li><CODE>PdfPTable.BACKGROUNDCANVAS</CODE> - the layer where the background goes to.
     * <li><CODE>PdfPTable.LINECANVAS</CODE> - the layer where the lines go to.
     * <li><CODE>PdfPTable.TEXTCANVAS</CODE> - the layer where the text go to. Anything placed here
     * will be over the table.
     * </ul>
     * The layers are placed in sequence on top of each other.
     * <p>
     * The <CODE>widths</CODE> and <CODE>heights</CODE> have the coordinates of the cells.<br>
     * The size of the <CODE>widths</CODE> array is the number of rows.
     * Each sub-array in <CODE>widths</CODE> corresponds to the x column border positions where
     * the first element is the x coordinate of the left table border and the last
     * element is the x coordinate of the right table border. 
     * If colspan is not used all the sub-arrays in <CODE>widths</CODE>
     * are the same.<br>
     * For the <CODE>heights</CODE> the first element is the y coordinate of the top table border and the last
     * element is the y coordinate of the bottom table border.
     * @param table the <CODE>PdfPTable</CODE> in use
     * @param widths an array of arrays with the cells' x positions. It has the length of the number
     * of rows
     * @param heights an array with the cells' y positions. It has a length of the number
     * of rows + 1
     * @param headerRows the number of rows defined for the header.
     * @param rowStart the first row number after the header
     * @param canvases an array of <CODE>PdfContentByte</CODE>
     */    
    public void tableLayout(PdfPTable table, float widths[][], float heights[], int headerRows, int rowStart, PdfContentByte[] canvases);

}

