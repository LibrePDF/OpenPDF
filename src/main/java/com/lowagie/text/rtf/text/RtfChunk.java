/*
 * $Id: RtfChunk.java 3580 2008-08-06 15:52:00Z howard_s $
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

import java.awt.Color;
import java.io.IOException;
import java.io.OutputStream;

import com.lowagie.text.Chunk;
import com.lowagie.text.DocWriter;
import com.lowagie.text.rtf.RtfElement;
import com.lowagie.text.rtf.document.RtfDocument;
import com.lowagie.text.rtf.style.RtfColor;
import com.lowagie.text.rtf.style.RtfFont;


/**
 * The RtfChunk contains one piece of text. The smallest text element available
 * in iText.
 * 
 * @version $Id: RtfChunk.java 3580 2008-08-06 15:52:00Z howard_s $
 * @author Mark Hall (Mark.Hall@mail.room3b.eu)
 * @author Thomas Bickel (tmb99@inode.at)
 */
public class RtfChunk extends RtfElement {

    /**
     * Constant for the subscript flag
     */
    private static final byte[] FONT_SUBSCRIPT = DocWriter.getISOBytes("\\sub");
    /**
     * Constant for the superscript flag
     */
    private static final byte[] FONT_SUPERSCRIPT = DocWriter.getISOBytes("\\super");
    /**
     * Constant for the end of sub / superscript flag
     */
    private static final byte[] FONT_END_SUPER_SUBSCRIPT = DocWriter.getISOBytes("\\nosupersub");
    /**
     * Constant for background color.
     */
    private static final byte[] BACKGROUND_COLOR = DocWriter.getISOBytes("\\chcbpat");

    /**
     * The font of this RtfChunk
     */
    private RtfFont font = null;
    /**
     * The actual content of this RtfChunk
     */
    private String content = "";
    /**
     * Whether to use soft line breaks instead of hard ones.
     */
    private boolean softLineBreaks = false;
    /**
     * The super / subscript of this RtfChunk
     */
    private float superSubScript = 0;
    /**
     * An optional background color.
     */
    private RtfColor background = null;

    /**
     * Constructs a RtfChunk based on the content of a Chunk
     * 
     * @param doc The RtfDocument that this Chunk belongs to
     * @param chunk The Chunk that this RtfChunk is based on
     */
    public RtfChunk(RtfDocument doc, Chunk chunk) {
        super(doc);
        
        if(chunk == null) {
            return;
        }
        
        if(chunk.getAttributes() != null && chunk.getAttributes().get(Chunk.SUBSUPSCRIPT) != null) {
            this.superSubScript = ((Float)chunk.getAttributes().get(Chunk.SUBSUPSCRIPT)).floatValue();
        }
        if(chunk.getAttributes() != null && chunk.getAttributes().get(Chunk.BACKGROUND) != null) {
            this.background = new RtfColor(this.document, (Color) ((Object[]) chunk.getAttributes().get(Chunk.BACKGROUND))[0]);
        }
        font = new RtfFont(doc, chunk.getFont());
        content = chunk.getContent();
    }
    
    /**
     * Writes the content of this RtfChunk. First the font information
     * is written, then the content, and then more font information
     */ 
    public void writeContent(final OutputStream result) throws IOException
    {
        if(this.background != null) {
            result.write(OPEN_GROUP);
        }
        
        this.font.writeBegin(result);
        if(superSubScript < 0) {
            result.write(FONT_SUBSCRIPT);
        } else if(superSubScript > 0) {
            result.write(FONT_SUPERSCRIPT);
        }
        if(this.background != null) {
            result.write(BACKGROUND_COLOR);
            result.write(intToByteArray(this.background.getColorNumber()));
        }
        result.write(DELIMITER);
        document.filterSpecialChar(result, content, false, softLineBreaks || this.document.getDocumentSettings().isAlwaysGenerateSoftLinebreaks());
        
        if(superSubScript != 0) {
            result.write(FONT_END_SUPER_SUBSCRIPT);
        }
        this.font.writeEnd(result);
        
        if(this.background != null) {
            result.write(CLOSE_GROUP);
        }    	
    }
    
    /**
     * Sets the RtfDocument this RtfChunk belongs to.
     * 
     * @param doc The RtfDocument to use
     */
    public void setRtfDocument(RtfDocument doc) {
        super.setRtfDocument(doc);
        this.font.setRtfDocument(this.document);
    }
    
    /**
     * Sets whether to use soft line breaks instead of default hard ones.
     * 
     * @param softLineBreaks whether to use soft line breaks instead of default hard ones.
     */
    public void setSoftLineBreaks(boolean softLineBreaks) {
        this.softLineBreaks = softLineBreaks;
    }
    
    /**
     * Gets whether to use soft line breaks instead of default hard ones.
     * 
     * @return whether to use soft line breaks instead of default hard ones.
     */
    public boolean getSoftLineBreaks() {
        return this.softLineBreaks;
    }
}
