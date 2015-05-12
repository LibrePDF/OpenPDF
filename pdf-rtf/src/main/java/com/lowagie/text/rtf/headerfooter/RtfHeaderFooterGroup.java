/*
 * $Id: RtfHeaderFooterGroup.java 3373 2008-05-12 16:21:24Z xlv $
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

package com.lowagie.text.rtf.headerfooter;

import java.io.IOException;
import java.io.OutputStream;

import com.lowagie.text.HeaderFooter;
import com.lowagie.text.Phrase;
import com.lowagie.text.rtf.RtfBasicElement;
import com.lowagie.text.rtf.document.RtfDocument;


/**
 * The RtfHeaderFooterGroup holds 0 - 3 RtfHeaderFooters that create a group
 * of headers or footers.
 * 
 * @version $Id: RtfHeaderFooterGroup.java 3373 2008-05-12 16:21:24Z xlv $
 * @author Mark Hall (Mark.Hall@mail.room3b.eu)
 * @author Thomas Bickel (tmb99@inode.at)
 */
public class RtfHeaderFooterGroup extends HeaderFooter implements RtfBasicElement {
    
    /**
     * This RtfHeaderFooterGroup contains no RtfHeaderFooter objects
     */
    private static final int MODE_NONE = 0;
    /**
     * This RtfHeaderFooterGroup contains one RtfHeaderFooter object
     */
    private static final int MODE_SINGLE = 1;
    /**
     * This RtfHeaderFooterGroup contains two or three RtfHeaderFooter objects
     */
    private static final int MODE_MULTIPLE = 2;
    
    /**
     * The current mode of this RtfHeaderFooterGroup. Defaults to MODE_NONE
     */
    private int mode = MODE_NONE;
    /**
     * The current type of this RtfHeaderFooterGroup. Defaults to RtfHeaderFooter.TYPE_HEADER
     */
    private int type = RtfHeaderFooter.TYPE_HEADER;
    
    /**
     * The RtfHeaderFooter for all pages
     */
    private RtfHeaderFooter headerAll = null;
    /**
     * The RtfHeaderFooter for the first page
     */
    private RtfHeaderFooter headerFirst = null;
    /**
     * The RtfHeaderFooter for the left hand pages
     */
    private RtfHeaderFooter headerLeft = null;
    /**
     * The RtfHeaderFooter for the right hand pages
     */
    private RtfHeaderFooter headerRight = null;
    /**
     * The RtfDocument this RtfHeaderFooterGroup belongs to
     */
    private RtfDocument document = null;

    /**
     * Constructs a RtfHeaderGroup to which you add headers/footers using 
     * via the setHeaderFooter method.
     *
     */
    public RtfHeaderFooterGroup() {
        super(new Phrase(""), false);
        this.mode = MODE_NONE;
    }
    
    /**
     * Constructs a certain type of RtfHeaderFooterGroup. RtfHeaderFooter.TYPE_HEADER
     * and RtfHeaderFooter.TYPE_FOOTER are valid values for type.
     * 
     * @param doc The RtfDocument this RtfHeaderFooter belongs to
     * @param type The type of RtfHeaderFooterGroup to create
     */
    public RtfHeaderFooterGroup(RtfDocument doc, int type) {
        super(new Phrase(""), false);
        this.document = doc;
        this.type = type;
    }
    
    /**
     * Constructs a RtfHeaderFooterGroup by copying the content of the original
     * RtfHeaderFooterGroup
     * 
     * @param doc The RtfDocument this RtfHeaderFooter belongs to
     * @param headerFooter The RtfHeaderFooterGroup to copy
     * @param type The type of RtfHeaderFooterGroup to create
     */
    public RtfHeaderFooterGroup(RtfDocument doc, RtfHeaderFooterGroup headerFooter, int type) {
        super(new Phrase(""), false);
        this.document = doc;
        this.mode = headerFooter.getMode();
        this.type = type;
        if(headerFooter.getHeaderAll() != null) {
            this.headerAll = new RtfHeaderFooter(this.document, headerFooter.getHeaderAll(), RtfHeaderFooter.DISPLAY_ALL_PAGES);
        }
        if(headerFooter.getHeaderFirst() != null) {
            this.headerFirst = new RtfHeaderFooter(this.document, headerFooter.getHeaderFirst(), RtfHeaderFooter.DISPLAY_FIRST_PAGE);
        }
        if(headerFooter.getHeaderLeft() != null) {
            this.headerLeft = new RtfHeaderFooter(this.document, headerFooter.getHeaderLeft(), RtfHeaderFooter.DISPLAY_LEFT_PAGES);
        }
        if(headerFooter.getHeaderRight() != null) {
            this.headerRight = new RtfHeaderFooter(this.document, headerFooter.getHeaderRight(), RtfHeaderFooter.DISPLAY_RIGHT_PAGES);
        }
        setType(this.type);
    }
    
    /**
     * Constructs a RtfHeaderFooterGroup for a certain RtfHeaderFooter.
     * 
     * @param doc The RtfDocument this RtfHeaderFooter belongs to
     * @param headerFooter The RtfHeaderFooter to display
     * @param type The type of RtfHeaderFooterGroup to create
     */
    public RtfHeaderFooterGroup(RtfDocument doc, RtfHeaderFooter headerFooter, int type) {
        super(new Phrase(""), false);
        this.document = doc;
        this.type = type;
        this.mode = MODE_SINGLE;
        headerAll = new RtfHeaderFooter(doc, headerFooter, RtfHeaderFooter.DISPLAY_ALL_PAGES);
        headerAll.setType(this.type);
    }
    
    /**
     * Constructs a RtfHeaderGroup for a certain HeaderFooter
     * 
     * @param doc The RtfDocument this RtfHeaderFooter belongs to
     * @param headerFooter The HeaderFooter to display
     * @param type The type of RtfHeaderFooterGroup to create
     */
    public RtfHeaderFooterGroup(RtfDocument doc, HeaderFooter headerFooter, int type) {
        super(new Phrase(""), false);
        this.document = doc;
        this.type = type;
        this.mode = MODE_SINGLE;
        headerAll = new RtfHeaderFooter(doc, headerFooter, type, RtfHeaderFooter.DISPLAY_ALL_PAGES);
        headerAll.setType(this.type);
    }
    
    /**
     * Sets the RtfDocument this RtfElement belongs to
     * 
     * @param doc The RtfDocument to use
     */
    public void setRtfDocument(RtfDocument doc) {
        this.document = doc;
        if(headerAll != null) {
            headerAll.setRtfDocument(this.document);
        }
        if(headerFirst != null) {
            headerFirst.setRtfDocument(this.document);
        }
        if(headerLeft != null) {
            headerLeft.setRtfDocument(this.document);
        }
        if(headerRight != null) {
            headerRight.setRtfDocument(this.document);
        }
    }
    
    /**
     * Write the content of this RtfHeaderFooterGroup.
     */    
    public void writeContent(final OutputStream result) throws IOException
    {
        if(this.mode == MODE_SINGLE) {
        	headerAll.writeContent(result);
        } else if(this.mode == MODE_MULTIPLE) {
            if(headerFirst != null) {
            	headerFirst.writeContent(result);
            }
            if(headerLeft != null) {
                headerLeft.writeContent(result);
            }
            if(headerRight != null) {
                headerRight.writeContent(result);
            }
            if(headerAll != null) {
                headerAll.writeContent(result);
            }
        }
    }        
    
    /**
     * Set a RtfHeaderFooter to be displayed at a certain position
     * 
     * @param headerFooter The RtfHeaderFooter to display
     * @param displayAt The display location to use
     */
    public void setHeaderFooter(RtfHeaderFooter headerFooter, int displayAt) {
        this.mode = MODE_MULTIPLE;
        headerFooter.setRtfDocument(this.document);
        headerFooter.setType(this.type);
        headerFooter.setDisplayAt(displayAt);
        switch(displayAt) {
            case RtfHeaderFooter.DISPLAY_ALL_PAGES:
                headerAll = headerFooter;
            	break;
            case RtfHeaderFooter.DISPLAY_FIRST_PAGE:
                headerFirst = headerFooter;
                break;
            case RtfHeaderFooter.DISPLAY_LEFT_PAGES:
                headerLeft = headerFooter;
                break;
            case RtfHeaderFooter.DISPLAY_RIGHT_PAGES:
                headerRight = headerFooter;
                break;
        }
    }
    
    /**
     * Set a HeaderFooter to be displayed at a certain position
     * 
     * @param headerFooter The HeaderFooter to set
     * @param displayAt The display location to use
     */
    public void setHeaderFooter(HeaderFooter headerFooter, int displayAt) {
        this.mode = MODE_MULTIPLE;
        switch(displayAt) {
            case RtfHeaderFooter.DISPLAY_ALL_PAGES:
                headerAll = new RtfHeaderFooter(this.document, headerFooter, this.type, displayAt);
            	break;
            case RtfHeaderFooter.DISPLAY_FIRST_PAGE:
                headerFirst = new RtfHeaderFooter(this.document, headerFooter, this.type, displayAt);
                break;
            case RtfHeaderFooter.DISPLAY_LEFT_PAGES:
                headerLeft = new RtfHeaderFooter(this.document, headerFooter, this.type, displayAt);
                break;
            case RtfHeaderFooter.DISPLAY_RIGHT_PAGES:
                headerRight = new RtfHeaderFooter(this.document, headerFooter, this.type, displayAt);
                break;
        }
    }
    
    /**
     * Set that this RtfHeaderFooterGroup should have a title page. If only
     * a header / footer for all pages exists, then it will be copied to the
     * first page as well.
     */
    public void setHasTitlePage() {
        if(this.mode == MODE_SINGLE) {
            this.mode = MODE_MULTIPLE;
            headerFirst = new RtfHeaderFooter(this.document, headerAll, RtfHeaderFooter.DISPLAY_FIRST_PAGE);
            headerFirst.setType(this.type);
        }
    }
    
    /**
     * Set that this RtfHeaderFooterGroup should have facing pages. If only
     * a header / footer for all pages exists, then it will be copied to the left
     * and right pages as well.
     */
    public void setHasFacingPages() {
        if(this.mode == MODE_SINGLE) {
            this.mode = MODE_MULTIPLE;
            this.headerLeft = new RtfHeaderFooter(this.document, this.headerAll, RtfHeaderFooter.DISPLAY_LEFT_PAGES);
            this.headerLeft.setType(this.type);
            this.headerRight = new RtfHeaderFooter(this.document, this.headerAll, RtfHeaderFooter.DISPLAY_RIGHT_PAGES);
            this.headerRight.setType(this.type);
            this.headerAll = null;
        } else if(this.mode == MODE_MULTIPLE) {
            if(this.headerLeft == null && this.headerAll != null) {
                this.headerLeft = new RtfHeaderFooter(this.document, this.headerAll, RtfHeaderFooter.DISPLAY_LEFT_PAGES);
                this.headerLeft.setType(this.type);
            }
            if(this.headerRight == null && this.headerAll != null) {
                this.headerRight = new RtfHeaderFooter(this.document, this.headerAll, RtfHeaderFooter.DISPLAY_RIGHT_PAGES);
                this.headerRight.setType(this.type);
            }
            this.headerAll = null;
        }
    }
    
    /**
     * Get whether this RtfHeaderFooterGroup has a titlepage
     * 
     * @return Whether this RtfHeaderFooterGroup has a titlepage
     */
    public boolean hasTitlePage() {
        return (headerFirst != null);
    }
    
    /**
     * Get whether this RtfHeaderFooterGroup has facing pages
     * 
     * @return Whether this RtfHeaderFooterGroup has facing pages
     */
    public boolean hasFacingPages() {
        return (headerLeft != null || headerRight != null);
    }

    /**
     * Unused
     * @param inTable
     */
    public void setInTable(boolean inTable) {
    }
    
    /**
     * Unused
     * @param inHeader
     */
    public void setInHeader(boolean inHeader) {
    }
    
    /**
     * Set the type of this RtfHeaderFooterGroup. RtfHeaderFooter.TYPE_HEADER
     * or RtfHeaderFooter.TYPE_FOOTER. Also sets the type for all RtfHeaderFooters
     * of this RtfHeaderFooterGroup.
     * 
     * @param type The type to use
     */
    public void setType(int type) {
        this.type = type;
        if(headerAll != null) {
            headerAll.setType(this.type);
        }
        if(headerFirst != null) {
            headerFirst.setType(this.type);
        }
        if(headerLeft != null) {
            headerLeft.setType(this.type);
        }
        if(headerRight != null) {
            headerRight.setType(this.type);
        }
    }
    
    /**
     * Gets the mode of this RtfHeaderFooterGroup
     * 
     * @return The mode of this RtfHeaderFooterGroup
     */
    protected int getMode() {
        return this.mode;
    }
    
    /**
     * Gets the RtfHeaderFooter for all pages
     * 
     * @return The RtfHeaderFooter for all pages 
     */
    protected RtfHeaderFooter getHeaderAll() {
        return headerAll;
    }

    /**
     * Gets the RtfHeaderFooter for the title page
     * 
     * @return The RtfHeaderFooter for the title page 
     */
    protected RtfHeaderFooter getHeaderFirst() {
        return headerFirst;
    }

    /**
     * Gets the RtfHeaderFooter for all left hand pages
     * 
     * @return The RtfHeaderFooter for all left hand pages 
     */
    protected RtfHeaderFooter getHeaderLeft() {
        return headerLeft;
    }

    /**
     * Gets the RtfHeaderFooter for all right hand pages
     * 
     * @return The RtfHeaderFooter for all right hand pages 
     */
    protected RtfHeaderFooter getHeaderRight() {
        return headerRight;
    }
}
