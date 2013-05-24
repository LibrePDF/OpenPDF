/*
 * $Id: RtfPhrase.java 3580 2008-08-06 15:52:00Z howard_s $
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

package com.lowagie.text.rtf.text;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

import com.lowagie.text.Chunk;
import com.lowagie.text.DocWriter;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Phrase;
import com.lowagie.text.rtf.RtfBasicElement;
import com.lowagie.text.rtf.RtfElement;
import com.lowagie.text.rtf.document.RtfDocument;
import com.lowagie.text.rtf.style.RtfFont;


/**
 * The RtfPhrase contains multiple RtfChunks
 * 
 * @version $Id: RtfPhrase.java 3580 2008-08-06 15:52:00Z howard_s $
 * @author Mark Hall (Mark.Hall@mail.room3b.eu)
 * @author Thomas Bickel (tmb99@inode.at)
 */
public class RtfPhrase extends RtfElement {

    /**
     * Constant for the resetting of the paragraph defaults
     */
    public static final byte[] PARAGRAPH_DEFAULTS = DocWriter.getISOBytes("\\pard");
    /**
     * Constant for resetting of font settings to their defaults
     */
    public static final byte[] PLAIN = DocWriter.getISOBytes("\\plain");
    /**
     * Constant for phrase in a table indication
     */
    public static final byte[] IN_TABLE = DocWriter.getISOBytes("\\intbl");
    /**
     * Constant for the line spacing.
     */
    public static final byte[] LINE_SPACING = DocWriter.getISOBytes("\\sl");
    
    /**
     * ArrayList containing the RtfChunks of this RtfPhrase
     */
    protected ArrayList chunks = new ArrayList();
    /**
     * The height of each line.
     */
    private int lineLeading = 0; 
    
    /**
     * A basically empty constructor that is used by the RtfParagraph.
     * 
     * @param doc The RtfDocument this RtfPhrase belongs to.
     */
    protected RtfPhrase(RtfDocument doc) {
        super(doc);
    }
    
    /**
     * Constructs a new RtfPhrase for the RtfDocument with the given Phrase
     * 
     * @param doc The RtfDocument this RtfPhrase belongs to
     * @param phrase The Phrase this RtfPhrase is based on
     */
    public RtfPhrase(RtfDocument doc, Phrase phrase) {
        super(doc);
        
        if(phrase == null) {
            return;
        }
        
        if(phrase.hasLeading()) {
            this.lineLeading = (int) (phrase.getLeading() * RtfElement.TWIPS_FACTOR);
        } else {
            this.lineLeading = 0;
        }
        
        RtfFont phraseFont = new RtfFont(null, phrase.getFont());
        for(int i = 0; i < phrase.size(); i++) {
            Element chunk = (Element) phrase.get(i);
            if(chunk instanceof Chunk) {
                ((Chunk) chunk).setFont(phraseFont.difference(((Chunk) chunk).getFont()));
            }
            try {
                RtfBasicElement[] rtfElements = doc.getMapper().mapElement(chunk);
                for(int j = 0; j < rtfElements.length; j++) {
                    chunks.add(rtfElements[j]);
                }
            } catch(DocumentException de) {
            }
        }
    }
    
    /**
     * Write the content of this RtfPhrase. First resets to the paragraph defaults
     * then if the RtfPhrase is in a RtfCell a marker for this is written and finally
     * the RtfChunks of this RtfPhrase are written.
     */    
    public void writeContent(final OutputStream result) throws IOException
    {
        result.write(PARAGRAPH_DEFAULTS);
        result.write(PLAIN);
        if(inTable) {
            result.write(IN_TABLE);
        }
        if(this.lineLeading > 0) {
            result.write(LINE_SPACING);
            result.write(intToByteArray(this.lineLeading));
        }
        for(int i = 0; i < chunks.size(); i++) {
        	RtfBasicElement rbe = (RtfBasicElement) chunks.get(i);
        	rbe.writeContent(result);
        }
    }        
    
    /**
     * Sets whether this RtfPhrase is in a table. Sets the correct inTable setting for all
     * child elements.
     * 
     * @param inTable <code>True</code> if this RtfPhrase is in a table, <code>false</code> otherwise
     */
    public void setInTable(boolean inTable) {
        super.setInTable(inTable);
        for(int i = 0; i < this.chunks.size(); i++) {
            ((RtfBasicElement) this.chunks.get(i)).setInTable(inTable);
        }
    }
    
    /**
     * Sets whether this RtfPhrase is in a header. Sets the correct inTable setting for all
     * child elements.
     * 
     * @param inHeader <code>True</code> if this RtfPhrase is in a header, <code>false</code> otherwise
     */
    public void setInHeader(boolean inHeader) {
        super.setInHeader(inHeader);
        for(int i = 0; i < this.chunks.size(); i++) {
            ((RtfBasicElement) this.chunks.get(i)).setInHeader(inHeader);
        }
    }
    
    /**
     * Sets the RtfDocument this RtfPhrase belongs to. Also sets the RtfDocument for all child
     * elements.
     * 
     * @param doc The RtfDocument to use
     */
    public void setRtfDocument(RtfDocument doc) {
        super.setRtfDocument(doc);
        for(int i = 0; i < this.chunks.size(); i++) {
            ((RtfBasicElement) this.chunks.get(i)).setRtfDocument(this.document);
        }
    }
}
