/*
 * $Id: RtfHeaderFooter.java 3580 2008-08-06 15:52:00Z howard_s $
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

import com.lowagie.text.DocWriter;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.HeaderFooter;
import com.lowagie.text.Image;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Table;
import com.lowagie.text.rtf.RtfBasicElement;
import com.lowagie.text.rtf.document.RtfDocument;
import com.lowagie.text.rtf.field.RtfPageNumber;


/**
 * The RtfHeaderFooter represents one header or footer. This class can be used
 * directly.
 * 
 * @version $Id: RtfHeaderFooter.java 3580 2008-08-06 15:52:00Z howard_s $
 * @author Mark Hall (Mark.Hall@mail.room3b.eu)
 * @author Thomas Bickel (tmb99@inode.at)
 */
public class RtfHeaderFooter extends HeaderFooter implements RtfBasicElement {

    /**
     * Constant for the header type
     */
    public static final int TYPE_HEADER = 1;
    /**
     * Constant for the footer type
     */
    public static final int TYPE_FOOTER = 2;
    /**
     * Constant for displaying the header/footer on the first page
     */
    public static final int DISPLAY_FIRST_PAGE = 0;
    /**
     * Constant for displaying the header/footer on all pages
     */
    public static final int DISPLAY_ALL_PAGES = 1;
    /**
     * Constant for displaying the header/footer on all left hand pages
     */
    public static final int DISPLAY_LEFT_PAGES = 2;
    /**
     * Constant for displaying the header/footer on all right hand pages
     */
    public static final int DISPLAY_RIGHT_PAGES = 4;

    /**
     * Constant for a header on all pages
     */
    private static final byte[] HEADER_ALL = DocWriter.getISOBytes("\\header");
    /**
     * Constant for a header on the first page
     */
    private static final byte[] HEADER_FIRST = DocWriter.getISOBytes("\\headerf");
    /**
     * Constant for a header on all left hand pages
     */
    private static final byte[] HEADER_LEFT = DocWriter.getISOBytes("\\headerl");
    /**
     * Constant for a header on all right hand pages
     */
    private static final byte[] HEADER_RIGHT = DocWriter.getISOBytes("\\headerr");
    /**
     * Constant for a footer on all pages
     */
    private static final byte[] FOOTER_ALL = DocWriter.getISOBytes("\\footer");
    /**
     * Constant for a footer on the first page
     */
    private static final byte[] FOOTER_FIRST = DocWriter.getISOBytes("\\footerf");
    /**
     * Constant for a footer on the left hand pages
     */
    private static final byte[] FOOTER_LEFT = DocWriter.getISOBytes("\\footerl");
    /**
     * Constant for a footer on the right hand pages
     */
    private static final byte[] FOOTER_RIGHT = DocWriter.getISOBytes("\\footerr");
    
    /**
     * The RtfDocument this RtfHeaderFooter belongs to
     */
    private RtfDocument document = null;
    /**
     * The content of this RtfHeaderFooter
     */
    private Object[] content = null;
    /**
     * The display type of this RtfHeaderFooter. TYPE_HEADER or TYPE_FOOTER
     */
    private int type = TYPE_HEADER;
    /**
     * The display location of this RtfHeaderFooter. DISPLAY_FIRST_PAGE,
     * DISPLAY_LEFT_PAGES, DISPLAY_RIGHT_PAGES or DISPLAY_ALL_PAGES
     */
    private int displayAt = DISPLAY_ALL_PAGES;
   
    /**
     * Constructs a RtfHeaderFooter based on a HeaderFooter with a certain type and displayAt
     * location. For internal use only.
     * 
     * @param doc The RtfDocument this RtfHeaderFooter belongs to
     * @param headerFooter The HeaderFooter to base this RtfHeaderFooter on
     * @param type The type of RtfHeaderFooter
     * @param displayAt The display location of this RtfHeaderFooter
     */
    protected RtfHeaderFooter(RtfDocument doc, HeaderFooter headerFooter, int type, int displayAt) {
        super(new Phrase(""), false);
        this.document = doc;
        this.type = type;
        this.displayAt = displayAt;
        Paragraph par = new Paragraph();
        par.setAlignment(headerFooter.alignment());
        if (headerFooter.getBefore() != null) {
            par.add(headerFooter.getBefore());
        }
        if (headerFooter.isNumbered()) {
            par.add(new RtfPageNumber(this.document));
        }
        if (headerFooter.getAfter() != null) {
            par.add(headerFooter.getAfter());
        }
        try {
            this.content = new Object[1];
            if(this.document != null) {
                this.content[0] = this.document.getMapper().mapElement(par)[0];
                ((RtfBasicElement) this.content[0]).setInHeader(true);
            } else {
                this.content[0] = par;
            }
        } catch(DocumentException de) {
            de.printStackTrace();
        }
    }
    
    /**
     * Constructs a RtfHeaderFooter as a copy of an existing RtfHeaderFooter.
     * For internal use only.
     * 
     * @param doc The RtfDocument this RtfHeaderFooter belongs to
     * @param headerFooter The RtfHeaderFooter to copy
     * @param displayAt The display location of this RtfHeaderFooter
     */
    protected RtfHeaderFooter(RtfDocument doc, RtfHeaderFooter headerFooter, int displayAt) {
        super(new Phrase(""), false);
        this.document = doc;
        this.content = headerFooter.getContent();
        this.displayAt = displayAt;
        for(int i = 0; i < this.content.length; i++) {
            if(this.content[i] instanceof Element) {
                try {
                    this.content[i] = this.document.getMapper().mapElement((Element) this.content[i])[0];
                } catch(DocumentException de) {
                    de.printStackTrace();
                }
            }
            if(this.content[i] instanceof RtfBasicElement) {
                ((RtfBasicElement) this.content[i]).setInHeader(true);
            }
        }
    }
    
    /**
     * Constructs a RtfHeaderFooter for a HeaderFooter.
     *  
     * @param doc The RtfDocument this RtfHeaderFooter belongs to
     * @param headerFooter The HeaderFooter to base this RtfHeaderFooter on
     */
    protected RtfHeaderFooter(RtfDocument doc, HeaderFooter headerFooter) {
        super(new Phrase(""), false);
        this.document = doc;
        Paragraph par = new Paragraph();
        par.setAlignment(headerFooter.alignment());
        if (headerFooter.getBefore() != null) {
            par.add(headerFooter.getBefore());
        }
        if (headerFooter.isNumbered()) {
            par.add(new RtfPageNumber(this.document));
        }
        if (headerFooter.getAfter() != null) {
            par.add(headerFooter.getAfter());
        }
        try {
            this.content = new Object[1];
            this.content[0] = doc.getMapper().mapElement(par)[0];
            ((RtfBasicElement) this.content[0]).setInHeader(true);
        } catch(DocumentException de) {
            de.printStackTrace();
        }
    }
    
    /**
     * Constructs a RtfHeaderFooter for any Element.
     *
     * @param element The Element to display as content of this RtfHeaderFooter
     */
    public RtfHeaderFooter(Element element) {
        this(new Element[]{element});
    }

    /**
     * Constructs a RtfHeaderFooter for an array of Elements.
     * 
     * @param elements The Elements to display as the content of this RtfHeaderFooter.
     */
    public RtfHeaderFooter(Element[] elements) {
        super(new Phrase(""), false);
        this.content = new Object[elements.length];
        for(int i = 0; i < elements.length; i++) {
            this.content[i] = elements[i];
        }
    }
    
    /**
     * Sets the RtfDocument this RtfElement belongs to
     * 
     * @param doc The RtfDocument to use
     */
    public void setRtfDocument(RtfDocument doc) {
        this.document = doc;
        if(this.document != null) {
            for(int i = 0; i < this.content.length; i++) {
                try {
                    if(this.content[i] instanceof Element) {
                        this.content[i] = this.document.getMapper().mapElement((Element) this.content[i])[0];
                        ((RtfBasicElement) this.content[i]).setInHeader(true);
                    } else if(this.content[i] instanceof RtfBasicElement){
                        ((RtfBasicElement) this.content[i]).setRtfDocument(this.document);
                        ((RtfBasicElement) this.content[i]).setInHeader(true);
                    }
                } catch(DocumentException de) {
                    de.printStackTrace();
                }
            }
        }
    }
    
    /**
     * Writes the content of this RtfHeaderFooter
     */    
    public void writeContent(final OutputStream result) throws IOException
    {
        result.write(OPEN_GROUP);
        if(this.type == TYPE_HEADER) {
            if(this.displayAt == DISPLAY_ALL_PAGES) {
                result.write(HEADER_ALL);
            } else if(this.displayAt == DISPLAY_FIRST_PAGE) {
                result.write(HEADER_FIRST);
            } else if(this.displayAt == DISPLAY_LEFT_PAGES) {
                result.write(HEADER_LEFT);
            } else if(this.displayAt == DISPLAY_RIGHT_PAGES) {
                result.write(HEADER_RIGHT);
            }
        } else {
            if(this.displayAt == DISPLAY_ALL_PAGES) {
                result.write(FOOTER_ALL);
            } else if(this.displayAt == DISPLAY_FIRST_PAGE) {
                result.write(FOOTER_FIRST);
            } else if(this.displayAt == DISPLAY_LEFT_PAGES) {
                result.write(FOOTER_LEFT);
            } else if(this.displayAt == DISPLAY_RIGHT_PAGES) {
                result.write(FOOTER_RIGHT);
            }
        }
        result.write(DELIMITER);
        for(int i = 0; i < this.content.length; i++) {
            if(this.content[i] instanceof RtfBasicElement) {
            	RtfBasicElement rbe = (RtfBasicElement)this.content[i];
            	rbe.writeContent(result);
            }
        }
        result.write(CLOSE_GROUP);
    }        
    
    
    /**
     * Sets the display location of this RtfHeaderFooter
     * 
     * @param displayAt The display location to use.
     */
    public void setDisplayAt(int displayAt) {
        this.displayAt = displayAt;
    }
    
    /**
     * Sets the type of this RtfHeaderFooter
     * 
     * @param type The type to use.
     */
    public void setType(int type) {
        this.type = type;
    }
    
    /**
     * Gets the content of this RtfHeaderFooter
     * 
     * @return The content of this RtfHeaderFooter
     */
    private Object[] getContent() {
        return this.content;
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
     * Set the alignment of this RtfHeaderFooter. Passes the setting
     * on to the contained element.
     */
    public void setAlignment(int alignment) {
        super.setAlignment(alignment);
        for(int i = 0; i < this.content.length; i++) {
            if(this.content[i] instanceof Paragraph) {
                ((Paragraph) this.content[i]).setAlignment(alignment);
            } else if(this.content[i] instanceof Table) {
                ((Table) this.content[i]).setAlignment(alignment);
            } else if(this.content[i] instanceof Image) {
                ((Image) this.content[i]).setAlignment(alignment);
            }     
        }
    }
}
