/*
 * $Id: RtfParagraph.java 3670 2009-02-01 09:13:48Z blowagie $
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

import com.lowagie.text.Chunk;
import com.lowagie.text.DocWriter;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Paragraph;
import com.lowagie.text.rtf.RtfBasicElement;
import com.lowagie.text.rtf.RtfElement;
import com.lowagie.text.rtf.document.RtfDocument;
import com.lowagie.text.rtf.graphic.RtfImage;
import com.lowagie.text.rtf.style.RtfFont;
import com.lowagie.text.rtf.style.RtfParagraphStyle;


/**
 * The RtfParagraph is an extension of the RtfPhrase that adds alignment and
 * indentation properties. It wraps a Paragraph.
 * 
 * @version $Id: RtfParagraph.java 3670 2009-02-01 09:13:48Z blowagie $
 * @author Mark Hall (Mark.Hall@mail.room3b.eu)
 * @author Thomas Bickel (tmb99@inode.at)
 */
public class RtfParagraph extends RtfPhrase {

    /**
     * Constant for the end of a paragraph
     */
    public static final byte[] PARAGRAPH = DocWriter.getISOBytes("\\par");
    
    /**
     * An optional RtfParagraphStyle to use for styling.
     */
    protected RtfParagraphStyle paragraphStyle = null;
    
    /**
     * Constructs a RtfParagraph belonging to a RtfDocument based on a Paragraph.
     * 
     * @param doc The RtfDocument this RtfParagraph belongs to
     * @param paragraph The Paragraph that this RtfParagraph is based on
     */
    public RtfParagraph(RtfDocument doc, Paragraph paragraph) {
        super(doc);
        RtfFont baseFont = null;
        if(paragraph.getFont() instanceof RtfParagraphStyle) {
            this.paragraphStyle = this.document.getDocumentHeader().getRtfParagraphStyle(((RtfParagraphStyle) paragraph.getFont()).getStyleName());
            baseFont = this.paragraphStyle;
        } else {
            baseFont = new RtfFont(this.document, paragraph.getFont());
            this.paragraphStyle = new RtfParagraphStyle(this.document, this.document.getDocumentHeader().getRtfParagraphStyle("Normal"));
            this.paragraphStyle.setAlignment(paragraph.getAlignment());
            this.paragraphStyle.setFirstLineIndent((int) (paragraph.getFirstLineIndent() * RtfElement.TWIPS_FACTOR));
            this.paragraphStyle.setIndentLeft((int) (paragraph.getIndentationLeft() * RtfElement.TWIPS_FACTOR));
            this.paragraphStyle.setIndentRight((int) (paragraph.getIndentationRight() * RtfElement.TWIPS_FACTOR));
            this.paragraphStyle.setSpacingBefore((int) (paragraph.getSpacingBefore() * RtfElement.TWIPS_FACTOR));
            this.paragraphStyle.setSpacingAfter((int) (paragraph.getSpacingAfter() * RtfElement.TWIPS_FACTOR));
            if(paragraph.hasLeading()) {
                this.paragraphStyle.setLineLeading((int) (paragraph.getLeading() * RtfElement.TWIPS_FACTOR));
            }
            this.paragraphStyle.setKeepTogether(paragraph.getKeepTogether());
        }        
        for(int i = 0; i < paragraph.size(); i++) {
            Element chunk = (Element) paragraph.get(i);
            if(chunk instanceof Chunk) {
                ((Chunk) chunk).setFont(baseFont.difference(((Chunk) chunk).getFont()));
            } else if(chunk instanceof RtfImage) {
                ((RtfImage) chunks.get(i)).setAlignment(this.paragraphStyle.getAlignment());
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
     * Set whether this RtfParagraph must stay on the same page as the next one.
     *  
     * @param keepTogetherWithNext Whether this RtfParagraph must keep together with the next.
     */
    public void setKeepTogetherWithNext(boolean keepTogetherWithNext) {
        this.paragraphStyle.setKeepTogetherWithNext(keepTogetherWithNext);
    }
    
    /**
     * Writes the content of this RtfParagraph. First paragraph specific data is written
     * and then the RtfChunks of this RtfParagraph are added.
     */    
    public void writeContent(final OutputStream result) throws IOException
    {
        result.write(PARAGRAPH_DEFAULTS);
        result.write(PLAIN);

        if(inTable) {
            result.write(IN_TABLE);
        }
        
        if(this.paragraphStyle != null) {
            this.paragraphStyle.writeBegin(result);
        }
        result.write(DocWriter.getISOBytes("\\plain"));
        
        for(int i = 0; i < chunks.size(); i++) {
        	RtfBasicElement rbe = (RtfBasicElement)chunks.get(i);
        	rbe.writeContent(result);
        }
        
        if(this.paragraphStyle != null) {
            this.paragraphStyle.writeEnd(result);
        }
        
        if(!inTable) {
            result.write(PARAGRAPH);
        }
        this.document.outputDebugLinebreak(result);
    }        
    
    /**
     * Gets the left indentation of this RtfParagraph.
     * 
     * @return The left indentation.
     */
    public int getIndentLeft() {
        return this.paragraphStyle.getIndentLeft();
    }
    
    /**
     * Sets the left indentation of this RtfParagraph.
     * 
     * @param indentLeft The left indentation to use.
     */
    public void setIndentLeft(int indentLeft) {
        this.paragraphStyle.setIndentLeft(indentLeft);
    }
    
    /**
     * Gets the right indentation of this RtfParagraph.
     * 
     * @return The right indentation.
     */
    public int getIndentRight()  {
        return this.paragraphStyle.getIndentRight();
    }
    
    /**
     * Sets the right indentation of this RtfParagraph.
     * 
     * @param indentRight The right indentation to use.
     */
    public void setIndentRight(int indentRight) {
        this.paragraphStyle.setIndentRight(indentRight);
    }
}
