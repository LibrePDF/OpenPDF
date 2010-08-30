/*
 * $Id: RtfBorderGroup.java 3427 2008-05-24 18:32:31Z xlv $
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

import java.awt.Color;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import com.lowagie.text.Rectangle;
import com.lowagie.text.rtf.RtfElement;
import com.lowagie.text.rtf.document.RtfDocument;


/**
 * The RtfBorderGroup represents a collection of RtfBorders to use in a RtfCell
 * or RtfTable.
 * 
 * @version $Id: RtfBorderGroup.java 3427 2008-05-24 18:32:31Z xlv $
 * @author Mark Hall (Mark.Hall@mail.room3b.eu)
 * @author Thomas Bickel (tmb99@inode.at)
 */
public class RtfBorderGroup extends RtfElement {
    /**
     * The type of borders this RtfBorderGroup contains.
     * RtfBorder.ROW_BORDER or RtfBorder.CELL_BORDER
     */
    private int borderType = RtfBorder.ROW_BORDER;
    /**
     * The borders in this RtfBorderGroup
     */
    private Hashtable borders = null;

    /**
     * Constructs an empty RtfBorderGroup.
     */
    public RtfBorderGroup() {
        super(null);
        this.borders = new Hashtable();
    }
    
    /**
     * Constructs a RtfBorderGroup with on border style for multiple borders.
     * 
     * @param bordersToAdd The borders to add (Rectangle.LEFT, Rectangle.RIGHT, Rectangle.TOP, Rectangle.BOTTOM, Rectangle.BOX)
     * @param borderStyle The style of border to add (from RtfBorder)
     * @param borderWidth The border width to use
     * @param borderColor The border color to use
     */
    public RtfBorderGroup(int bordersToAdd, int borderStyle, float borderWidth, Color borderColor) {
        super(null);
        this.borders = new Hashtable();
        addBorder(bordersToAdd, borderStyle, borderWidth, borderColor);
    }
    
    /**
     * Constructs a RtfBorderGroup based on another RtfBorderGroup.
     * 
     * @param doc The RtfDocument this RtfBorderGroup belongs to
     * @param borderType The type of borders this RtfBorderGroup contains
     * @param borderGroup The RtfBorderGroup to use as a base
     */
    protected RtfBorderGroup(RtfDocument doc, int borderType, RtfBorderGroup borderGroup) {
        super(doc);
        this.borders = new Hashtable();
        this.borderType = borderType;
        if(borderGroup != null) {
            Iterator it = borderGroup.getBorders().entrySet().iterator();
            while(it.hasNext()) {
                Map.Entry entry = (Map.Entry) it.next();
                this.borders.put(entry.getKey(), new RtfBorder(this.document, this.borderType, (RtfBorder) entry.getValue()));
            }
        }
    }
    
    /**
     * Constructs a RtfBorderGroup with certain borders
     * 
     * @param doc The RtfDocument this RtfBorderGroup belongs to
     * @param borderType The type of borders this RtfBorderGroup contains
     * @param bordersToUse The borders to add (Rectangle.LEFT, Rectangle.RIGHT, Rectangle.TOP, Rectangle.BOTTOM, Rectangle.BOX)
     * @param borderWidth The border width to use
     * @param borderColor The border color to use
     */
    protected RtfBorderGroup(RtfDocument doc, int borderType, int bordersToUse, float borderWidth, Color borderColor) {
        super(doc);
        this.borderType = borderType;
        this.borders = new Hashtable();
        addBorder(bordersToUse, RtfBorder.BORDER_SINGLE, borderWidth, borderColor);
    }
    
    /**
     * Sets a border in the Hashtable of borders
     * 
     * @param borderPosition The position of this RtfBorder
     * @param borderStyle The type of borders this RtfBorderGroup contains
     * @param borderWidth The border width to use
     * @param borderColor The border color to use
     */
    private void setBorder(int borderPosition, int borderStyle, float borderWidth, Color borderColor) {
        RtfBorder border = new RtfBorder(this.document, this.borderType, borderPosition, borderStyle, borderWidth, borderColor);
        this.borders.put(new Integer(borderPosition), border);
    }
    
    /**
     * Adds borders to the RtfBorderGroup
     * 
     * @param bordersToAdd The borders to add (Rectangle.LEFT, Rectangle.RIGHT, Rectangle.TOP, Rectangle.BOTTOM, Rectangle.BOX)
     * @param borderStyle The style of border to add (from RtfBorder)
     * @param borderWidth The border width to use
     * @param borderColor The border color to use
     */
    public void addBorder(int bordersToAdd, int borderStyle, float borderWidth, Color borderColor) {
        if((bordersToAdd & Rectangle.LEFT) == Rectangle.LEFT) {
            setBorder(RtfBorder.LEFT_BORDER, borderStyle, borderWidth, borderColor);
        }
        if((bordersToAdd & Rectangle.TOP) == Rectangle.TOP) {
            setBorder(RtfBorder.TOP_BORDER, borderStyle, borderWidth, borderColor);
        }
        if((bordersToAdd & Rectangle.RIGHT) == Rectangle.RIGHT) {
            setBorder(RtfBorder.RIGHT_BORDER, borderStyle, borderWidth, borderColor);
        }
        if((bordersToAdd & Rectangle.BOTTOM) == Rectangle.BOTTOM) {
            setBorder(RtfBorder.BOTTOM_BORDER, borderStyle, borderWidth, borderColor);
        }
        if((bordersToAdd & Rectangle.BOX) == Rectangle.BOX && this.borderType == RtfBorder.ROW_BORDER) {
            setBorder(RtfBorder.VERTICAL_BORDER, borderStyle, borderWidth, borderColor);
            setBorder(RtfBorder.HORIZONTAL_BORDER, borderStyle, borderWidth, borderColor);
        }
    }
    
    /**
     * Removes borders from the list of borders
     * 
     * @param bordersToRemove The borders to remove (from Rectangle)
     */
    public void removeBorder(int bordersToRemove) {
        if((bordersToRemove & Rectangle.LEFT) == Rectangle.LEFT) {
            this.borders.remove(new Integer(RtfBorder.LEFT_BORDER));
        }
        if((bordersToRemove & Rectangle.TOP) == Rectangle.TOP) {
            this.borders.remove(new Integer(RtfBorder.TOP_BORDER));
        }
        if((bordersToRemove & Rectangle.RIGHT) == Rectangle.RIGHT) {
            this.borders.remove(new Integer(RtfBorder.RIGHT_BORDER));
        }
        if((bordersToRemove & Rectangle.BOTTOM) == Rectangle.BOTTOM) {
            this.borders.remove(new Integer(RtfBorder.BOTTOM_BORDER));
        }
        if((bordersToRemove & Rectangle.BOX) == Rectangle.BOX && this.borderType == RtfBorder.ROW_BORDER) {
            this.borders.remove(new Integer(RtfBorder.VERTICAL_BORDER));
            this.borders.remove(new Integer(RtfBorder.HORIZONTAL_BORDER));
        }
    }
    
    /**
     * Writes the borders of this RtfBorderGroup
     */    
    public void writeContent(final OutputStream result) throws IOException
    {
        Iterator it = this.borders.values().iterator();
        while(it.hasNext()) {
            ((RtfBorder) it.next()).writeContent(result);
        }
    }        
    
    /**
     * Gets the RtfBorders of this RtfBorderGroup
     * 
     * @return The RtfBorders of this RtfBorderGroup
     */
    protected Hashtable getBorders() {
        return this.borders;
    }
}
