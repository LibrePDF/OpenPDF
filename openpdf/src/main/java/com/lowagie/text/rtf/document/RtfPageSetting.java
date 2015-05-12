/*
 * $Id:RtfPageSetting.java 3126 2008-02-07 20:30:46Z hallm $
 *
 * Copyright 2003, 2004 by Mark Hall
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

package com.lowagie.text.rtf.document;

import java.io.IOException;
import java.io.OutputStream;

import com.lowagie.text.DocWriter;
import com.lowagie.text.PageSize;
import com.lowagie.text.Rectangle;
import com.lowagie.text.rtf.RtfElement;
import com.lowagie.text.rtf.RtfExtendedElement;


/**
 * The RtfPageSetting stores the page size / page margins for a RtfDocument.
 * INTERNAL CLASS - NOT TO BE USED DIRECTLY
 *  
 * @version $Id:RtfPageSetting.java 3126 2008-02-07 20:30:46Z hallm $
 * @author Mark Hall (Mark.Hall@mail.room3b.eu)
 * @author Thomas Bickel (tmb99@inode.at)
 */
public class RtfPageSetting extends RtfElement implements RtfExtendedElement {

    /**
     * Constant for the page height
     */
    private static final byte[] PAGE_WIDTH = DocWriter.getISOBytes("\\paperw");
    /**
     * Constant for the page width
     */
    private static final byte[] PAGE_HEIGHT = DocWriter.getISOBytes("\\paperh");
    /**
     * Constant for the left margin
     */
    private static final byte[] MARGIN_LEFT = DocWriter.getISOBytes("\\margl");
    /**
     * Constant for the right margin
     */
    private static final byte[] MARGIN_RIGHT = DocWriter.getISOBytes("\\margr");
    /**
     * Constant for the top margin
     */
    private static final byte[] MARGIN_TOP = DocWriter.getISOBytes("\\margt");
    /**
     * Constant for the bottom margin
     */
    private static final byte[] MARGIN_BOTTOM = DocWriter.getISOBytes("\\margb");
    /**
     * Constant for landscape
     */
    private static final byte[] LANDSCAPE = DocWriter.getISOBytes("\\lndscpsxn");
    /**
     * Constant for the section page width
     */
    private static final byte[] SECTION_PAGE_WIDTH = DocWriter.getISOBytes("\\pgwsxn");
    /**
     * Constant for the section page height
     */
    private static final byte[] SECTION_PAGE_HEIGHT = DocWriter.getISOBytes("\\pghsxn");
    /**
     * Constant for the section left margin
     */
    private static final byte[] SECTION_MARGIN_LEFT = DocWriter.getISOBytes("\\marglsxn");
    /**
     * Constant for the section right margin
     */
    private static final byte[] SECTION_MARGIN_RIGHT = DocWriter.getISOBytes("\\margrsxn");
    /**
     * Constant for the section top margin
     */
    private static final byte[] SECTION_MARGIN_TOP = DocWriter.getISOBytes("\\margtsxn");
    /**
     * Constant for the section bottom margin
     */
    private static final byte[] SECTION_MARGIN_BOTTOM = DocWriter.getISOBytes("\\margbsxn");
    
    /**
     * The page width to use
     */
    private int pageWidth = 11906;
    /**
     * The page height to use
     */
    private int pageHeight = 16840;
    /**
     * The left margin to use
     */
    private int marginLeft = 1800;
    /**
     * The right margin to use
     */
    private int marginRight = 1800;
    /**
     * The top margin to use
     */
    private int marginTop = 1440;
    /**
     * The bottom margin to use
     */
    private int marginBottom = 1440;
    /**
     * Whether the page is portrait or landscape
     */
    private boolean landscape = false;

    /**
     * Constructs a new RtfPageSetting object belonging to a RtfDocument.
     * 
     * @param doc The RtfDocument this RtfPageSetting belongs to 
     */
    public RtfPageSetting(RtfDocument doc) {
        super(doc);
    }
    
    /**
     * unused
     */
    public void writeContent(final OutputStream out) throws IOException
    {    	
    }
    
    /**
     * Writes the page size / page margin definition
     */
    public void writeDefinition(final OutputStream result) throws IOException
    {
        result.write(PAGE_WIDTH);
        result.write(intToByteArray(pageWidth));
        result.write(PAGE_HEIGHT);
        result.write(intToByteArray(pageHeight));
        result.write(MARGIN_LEFT);
        result.write(intToByteArray(marginLeft));
        result.write(MARGIN_RIGHT);
        result.write(intToByteArray(marginRight));
        result.write(MARGIN_TOP);
        result.write(intToByteArray(marginTop));
        result.write(MARGIN_BOTTOM);
        result.write(intToByteArray(marginBottom));
        this.document.outputDebugLinebreak(result);    	
    }
    
    /**
     * Writes the definition part for a new section
     */
    public void writeSectionDefinition(final OutputStream result) throws IOException
    {
        if(landscape) {
            result.write(LANDSCAPE);
            result.write(SECTION_PAGE_WIDTH);
            result.write(intToByteArray(pageWidth));
            result.write(SECTION_PAGE_HEIGHT);
            result.write(intToByteArray(pageHeight));
            this.document.outputDebugLinebreak(result);
        } else {
            result.write(SECTION_PAGE_WIDTH);
            result.write(intToByteArray(pageWidth));
            result.write(SECTION_PAGE_HEIGHT);
            result.write(intToByteArray(pageHeight));
            this.document.outputDebugLinebreak(result);
        }
        result.write(SECTION_MARGIN_LEFT);
        result.write(intToByteArray(marginLeft));
        result.write(SECTION_MARGIN_RIGHT);
        result.write(intToByteArray(marginRight));
        result.write(SECTION_MARGIN_TOP);
        result.write(intToByteArray(marginTop));
        result.write(SECTION_MARGIN_BOTTOM);
        result.write(intToByteArray(marginBottom));    	
    }

    /**
     * Gets the bottom margin
     *  
     * @return Returns the bottom margin
     */
    public int getMarginBottom() {
        return marginBottom;
    }
    
    /**
     * Sets the bottom margin
     * 
     * @param marginBottom The bottom margin to use
     */
    public void setMarginBottom(int marginBottom) {
        this.marginBottom = marginBottom;
    }
    
    /**
     * Gets the left margin
     * 
     * @return Returns the left margin
     */
    public int getMarginLeft() {
        return marginLeft;
    }
    
    /**
     * Sets the left margin to use
     * 
     * @param marginLeft The left margin to use
     */
    public void setMarginLeft(int marginLeft) {
        this.marginLeft = marginLeft;
    }
    
    /**
     * Gets the right margin
     * 
     * @return Returns the right margin
     */
    public int getMarginRight() {
        return marginRight;
    }
    
    /**
     * Sets the right margin to use
     * 
     * @param marginRight The right margin to use
     */
    public void setMarginRight(int marginRight) {
        this.marginRight = marginRight;
    }
    
    /**
     * Gets the top margin
     * 
     * @return Returns the top margin
     */
    public int getMarginTop() {
        return marginTop;
    }
    
    /**
     * Sets the top margin to use
     * 
     * @param marginTop The top margin to use
     */
    public void setMarginTop(int marginTop) {
        this.marginTop = marginTop;
    }
    
    /**
     * Gets the page height
     * 
     * @return Returns the page height
     */
    public int getPageHeight() {
        return pageHeight;
    }
    
    /**
     * Sets the page height to use
     * 
     * @param pageHeight The page height to use
     */
    public void setPageHeight(int pageHeight) {
        this.pageHeight = pageHeight;
    }
    
    /**
     * Gets the page width
     * 
     * @return Returns the page width
     */
    public int getPageWidth() {
        return pageWidth;
    }
    
    /**
     * Sets the page width to use
     * 
     * @param pageWidth The page width to use
     */
    public void setPageWidth(int pageWidth) {
        this.pageWidth = pageWidth;
    }
    
    /**
     * Set the page size to use. This method will use guessFormat to try to guess the correct
     * page format. If no format could be guessed, the sizes from the pageSize are used and
     * the landscape setting is determined by comparing width and height;
     * 
     * @param pageSize The pageSize to use
     */
    public void setPageSize(Rectangle pageSize) {
        if(!guessFormat(pageSize, false)) {
            this.pageWidth = (int) (pageSize.getWidth() * RtfElement.TWIPS_FACTOR);
            this.pageHeight = (int) (pageSize.getHeight() * RtfElement.TWIPS_FACTOR);
            this.landscape = pageWidth > pageHeight;
        }
    }
    
    /**
     * This method tries to fit the <code>Rectangle pageSize</code> to one of the predefined PageSize rectangles.
     * If a match is found the pageWidth and pageHeight will be set according to values determined from files
     * generated by MS Word2000 and OpenOffice 641. If no match is found the method will try to match the rotated
     * Rectangle by calling itself with the parameter rotate set to true.
     * 
     * @param pageSize the page size for which to guess the correct format
     * @param rotate Whether we should try to rotate the size befor guessing the format
     * @return <code>True</code> if the format was guessed, <code>false/<code> otherwise
     */
    private boolean guessFormat(Rectangle pageSize, boolean rotate) {
        if (rotate) {
            pageSize = pageSize.rotate();
        }
        if (rectEquals(pageSize, PageSize.A3)) {
            pageWidth = 16837;
            pageHeight = 23811;
            landscape = rotate;
            return true;
        }
        if (rectEquals(pageSize, PageSize.A4)) {
            pageWidth = 11907;
            pageHeight = 16840;
            landscape = rotate;
            return true;
        }
        if (rectEquals(pageSize, PageSize.A5)) {
            pageWidth = 8391;
            pageHeight = 11907;
            landscape = rotate;
            return true;
        }
        if (rectEquals(pageSize, PageSize.A6)) {
            pageWidth = 5959;
            pageHeight = 8420;
            landscape = rotate;
            return true;
        }
        if (rectEquals(pageSize, PageSize.B4)) {
            pageWidth = 14570;
            pageHeight = 20636;
            landscape = rotate;
            return true;
        }
        if (rectEquals(pageSize, PageSize.B5)) {
            pageWidth = 10319;
            pageHeight = 14572;
            landscape = rotate;
            return true;
        }
        if (rectEquals(pageSize, PageSize.HALFLETTER)) {
            pageWidth = 7927;
            pageHeight = 12247;
            landscape = rotate;
            return true;
        }
        if (rectEquals(pageSize, PageSize.LETTER)) {
            pageWidth = 12242;
            pageHeight = 15842;
            landscape = rotate;
            return true;
        }
        if (rectEquals(pageSize, PageSize.LEGAL)) {
            pageWidth = 12252;
            pageHeight = 20163;
            landscape = rotate;
            return true;
        }
        if (!rotate && guessFormat(pageSize, true)) {
            int x = pageWidth;
            pageWidth = pageHeight;
            pageHeight = x;
            return true;
        }
        return false;
    }

    /**
     * This method compares to Rectangles. They are considered equal if width and height are the same
     * 
     * @param rect1 The first Rectangle to compare
     * @param rect2 The second Rectangle to compare
     * @return <code>True</code> if the Rectangles equal, <code>false</code> otherwise
     */
    private boolean rectEquals(Rectangle rect1, Rectangle rect2) {
        return (rect1.getWidth() == rect2.getWidth()) && (rect1.getHeight() == rect2.getHeight());
    }
}
