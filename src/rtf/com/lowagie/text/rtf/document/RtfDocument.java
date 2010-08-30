/*
 * $Id: RtfDocument.java 4065 2009-09-16 23:09:11Z psoares33 $
 *
 * Copyright 2003, 2004, 2005 by Mark Hall
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
import java.util.ArrayList;
import com.lowagie.text.error_messages.MessageLocalization;

import com.lowagie.text.DocWriter;
import com.lowagie.text.rtf.RtfBasicElement;
import com.lowagie.text.rtf.RtfElement;
import com.lowagie.text.rtf.RtfMapper;
import com.lowagie.text.rtf.document.output.RtfDataCache;
import com.lowagie.text.rtf.document.output.RtfDiskCache;
import com.lowagie.text.rtf.document.output.RtfEfficientMemoryCache;
import com.lowagie.text.rtf.document.output.RtfMemoryCache;
import com.lowagie.text.rtf.graphic.RtfImage;

/**
 * The RtfDocument stores all document related data and also the main data stream.
 * INTERNAL CLASS - NOT TO BE USED DIRECTLY
 *
 * @version $Id: RtfDocument.java 4065 2009-09-16 23:09:11Z psoares33 $
 * @author Mark Hall (Mark.Hall@mail.room3b.eu)
 * @author Todd Bush [Tab support]
 * @author Thomas Bickel (tmb99@inode.at)
 * @since 1.x
 */
public class RtfDocument extends RtfElement {
    /**
     * Stores the actual document data
     */
    private RtfDataCache data = null;
    /**
     * The RtfMapper to use in this RtfDocument
     */
    private RtfMapper mapper = null;
    /**
     * The RtfDocumentHeader that handles all document header methods
     */
    private RtfDocumentHeader documentHeader = null;
    /**
     * Stores integers that have been generated as unique random numbers
     */
    private ArrayList previousRandomInts = null;
    /**
     * Whether to automatically generate TOC entries for Chapters and Sections. Defaults to false
     */
    private boolean autogenerateTOCEntries = false;
    /**
     * The RtfDocumentSettings for this RtfDocument.
     */
    private RtfDocumentSettings documentSettings = null;
    /**
     * The last RtfBasicElement that was added directly to the RtfDocument.
     */
    private RtfBasicElement lastElementWritten = null;
    
    /**
     * Constant for the Rtf document start
     */
    private static final byte[] RTF_DOCUMENT = DocWriter.getISOBytes("\\rtf1");

    private final static byte[] FSC_LINE = DocWriter.getISOBytes("\\line ");
    private final static byte[] FSC_PAR = DocWriter.getISOBytes("\\par ");
    private final static byte[] FSC_TAB = DocWriter.getISOBytes("\\tab ");
    private final static byte[] FSC_PAGE_PAR = DocWriter.getISOBytes("\\page\\par ");
    private final static byte[] FSC_NEWPAGE = DocWriter.getISOBytes("$newpage$");
    private final static byte[] FSC_BACKSLASH = DocWriter.getISOBytes("\\");
    private final static byte[] FSC_HEX_PREFIX = DocWriter.getISOBytes("\\\'");
    private final static byte[] FSC_UNI_PREFIX = DocWriter.getISOBytes("\\u");
    
    /**
     * The default constructor for a RtfDocument
     */
    public RtfDocument() {
        super(null);
        this.data = new RtfMemoryCache();
        this.mapper = new RtfMapper(this);
        this.documentHeader = new RtfDocumentHeader(this);
        this.documentHeader.init();
        this.previousRandomInts = new ArrayList();
        this.documentSettings = new RtfDocumentSettings(this);
    }

    /**
     * unused
     */
    public void writeContent(final OutputStream out) throws IOException
    {    	
    }
    
    /**
     * Writes the document
     *
     * @param out The <code>OutputStream</code> to write the RTF document to.
     */
    public void writeDocument(OutputStream out) {
        try {
            out.write(OPEN_GROUP);
            out.write(RtfDocument.RTF_DOCUMENT);
            this.documentHeader.writeContent(out);
            this.data.writeTo(out);
            out.write(CLOSE_GROUP);
        } catch(IOException ioe) {
            ioe.printStackTrace();
        }
    }
    
    /**
     * Opens the RtfDocument and initializes the data cache. If the data cache is
     * set to CACHE_DISK, but the cache cannot be initialized then the memory cache
     * is used.
     */
    public void open() {
        try {
            switch(this.documentSettings.getDataCacheStyle()) {
            	case RtfDataCache.CACHE_MEMORY_EFFICIENT:  
            		this.data = new RtfEfficientMemoryCache(); 
            		break;
                case RtfDataCache.CACHE_MEMORY:
                	this.data = new RtfMemoryCache();
                	break;
                case RtfDataCache.CACHE_DISK:
                	this.data = new RtfDiskCache();
                	break;
                default:
                	throw new RuntimeException(MessageLocalization.getComposedMessage("unknown"));
            }
    		
        } catch(IOException ioe) {
            System.err.println("Could not initialize disk cache. Using memory cache.");
            ioe.printStackTrace();
            this.data = new RtfMemoryCache();
        }
    }
    
    /**
     * Adds an element to the rtf document
     * 
     * @param element The element to add
     */
    public void add(RtfBasicElement element) {
        try {
            if(element instanceof RtfInfoElement) {
                this.documentHeader.addInfoElement((RtfInfoElement) element);
            } else {
                if(element instanceof RtfImage) {
                    ((RtfImage) element).setTopLevelElement(true);
                }
                element.writeContent( this.data.getOutputStream() );
                this.lastElementWritten = element;
            }
        } catch(IOException ioe) {
            ioe.printStackTrace();
        }
    }
    
    /**
     * Gets the RtfMapper object of this RtfDocument
     * 
     * @return The RtfMapper
     */
    public RtfMapper getMapper() {
        return this.mapper;
    }
    
    /**
     * Generates a random integer that is unique with respect to the document.
     * Will not return a number between -1 and -5 because some values in that range are invalid.
     * @return A random int
     */
    public int getRandomInt() {
        Integer newInt = null;
        do {
//        	do {
        		newInt = new Integer((int) (Math.random() * Integer.MAX_VALUE));
//        	} while(newInt.intValue() <= -1 && newInt.intValue() >= -5);
        } while(this.previousRandomInts.contains(newInt));
        this.previousRandomInts.add(newInt);
        return newInt.intValue();
    }
    
    /**
     * Gets the RtfDocumentHeader of this RtfDocument
     * 
     * @return The RtfDocumentHeader of this RtfDocument
     */
    public RtfDocumentHeader getDocumentHeader() {
        return this.documentHeader;
    }
    
    /**
     * Writes the given string to the given {@link OutputStream} encoding the string characters.
     * 
     * @param out destination OutputStream
     * @param str string to write
     * @param useHex if <code>true</code> hex encoding characters is preferred to unicode encoding if possible
     * @param softLineBreaks if <code>true</code> return characters are written as soft line breaks
     * 
     * @throws IOException
     */
    public void filterSpecialChar(final OutputStream out, final String str, final boolean useHex, final boolean softLineBreaks) throws IOException
    {
        if(out == null) {
            throw new NullPointerException(MessageLocalization.getComposedMessage("null.outpustream"));
        }

        final boolean alwaysUseUniCode = this.documentSettings.isAlwaysUseUnicode();
        if(str == null) {
            return;
        }
        final int len = str.length();
        if(len == 0) {
            return;
        }

        for(int k = 0; k < len; k++) {
            final char c = str.charAt(k);
            if(c < 0x20) {
                //allow return and tab only
                if(c == '\n') {
                    out.write(softLineBreaks ? FSC_LINE : FSC_PAR);
                } else if(c == '\t') {
                    out.write(FSC_TAB);                 
                } else {
                    out.write('?');
                }
            } else if((c == '\\') || (c == '{') || (c == '}')) {
                //escape
                out.write(FSC_BACKSLASH);
                out.write(c);
            } else if((c == '$') && (len-k >= FSC_NEWPAGE.length) && subMatch(str, k, FSC_NEWPAGE)) {
                out.write(FSC_PAGE_PAR);
                k += FSC_NEWPAGE.length-1;
            } else {
                if((c > 0xff) || ((c > 'z') && alwaysUseUniCode)) {
                    if(useHex && (c <= 0xff)) {
                        //encode as 2 char hex string 
                        out.write(FSC_HEX_PREFIX);
                        out.write(RtfImage.byte2charLUT, c*2, 2);
                    } else {
                        //encode as decimal, signed short value
                        out.write(FSC_UNI_PREFIX);
                        String s = Short.toString((short)c);
                        for(int x = 0; x < s.length(); x++) {
                            out.write(s.charAt(x));
                        }
                        out.write('?');
                    }
                } else {
                    out.write(c);
                }
            }
        }       
    }
    /**
     * Returns <code>true</code> if <tt>m.length</tt> characters in <tt>str</tt>, starting at offset <tt>soff</tt>
     * match the bytes in the given array <tt>m</tt>.
     * 
     * @param str the string to search for a match
     * @param soff the starting offset in str
     * @param m the array to match
     * @return <code>true</code> if there is match
     */
    private static boolean subMatch(final String str, int soff, final byte[] m)
    {
        for(int k = 0; k < m.length; k++) {
            if(str.charAt(soff++) != m[k]) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Whether to automagically generate table of contents entries when
     * adding Chapters or Sections.
     * 
     * @param autogenerate Whether to automatically generate TOC entries
     */
    public void setAutogenerateTOCEntries(boolean autogenerate) {
        this.autogenerateTOCEntries = autogenerate;
    }
    
    /**
     * Get whether to automatically generate table of contents entries
     * 
     * @return Whether to automatically generate TOC entries
     */
    public boolean getAutogenerateTOCEntries() {
        return this.autogenerateTOCEntries;
    }
    
    /**
     * Gets the RtfDocumentSettings that specify how the rtf document is generated.
     * 
     * @return The current RtfDocumentSettings.
     */
    public RtfDocumentSettings getDocumentSettings() {
        return this.documentSettings;
    }
    
    /**
     * Gets the last RtfBasicElement that was directly added to the RtfDocument.
     *  
     * @return The last RtfBasicElement that was directly added to the RtfDocument.
     */
    public RtfBasicElement getLastElementWritten() {
        return this.lastElementWritten;
    }
    
    /**
     * Helper method outputs linebreak in document if debugging is turned on.
     * @param result the OutputStream to write the linebreak to.
     * @throws IOException
     * @since 2.1.3
     */
    final public void outputDebugLinebreak(final OutputStream result) throws IOException {
    	if(this.getDocumentSettings().isOutputDebugLineBreaks())
        {
        	result.write('\n');
        }
    }
}
