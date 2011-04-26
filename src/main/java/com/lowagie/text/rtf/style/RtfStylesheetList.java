/*
 * $Id: RtfStylesheetList.java 3580 2008-08-06 15:52:00Z howard_s $
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

package com.lowagie.text.rtf.style;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;

import com.lowagie.text.DocWriter;
import com.lowagie.text.rtf.RtfBasicElement;
import com.lowagie.text.rtf.RtfElement;
import com.lowagie.text.rtf.RtfExtendedElement;
import com.lowagie.text.rtf.document.RtfDocument;

/**
 * The RtfStylesheetList stores the RtfParagraphStyles that are used in the document.
 * 
 * @version $Id: RtfStylesheetList.java 3580 2008-08-06 15:52:00Z howard_s $
 * @author Mark Hall (Mark.Hall@mail.room3b.eu)
 * @author Thomas Bickel (tmb99@inode.at)
 */
public class RtfStylesheetList extends RtfElement implements RtfExtendedElement {

    /**
     * The HashMap containing the RtfParagraphStyles.
     */
    private HashMap styleMap = null;
    /**
     * Whether the default settings have been loaded.
     */
    private boolean defaultsLoaded = false;
    
    /**
     * Constructs a new RtfStylesheetList for the RtfDocument.
     * 
     * @param doc The RtfDocument this RtfStylesheetList belongs to.
     */
    public RtfStylesheetList(RtfDocument doc) {
        super(doc);
        this.styleMap = new HashMap();
    }

    /**
     * unused
     */
    public void writeContent(OutputStream out) throws IOException
    {	
    }
    
    /**
     * Register a RtfParagraphStyle with this RtfStylesheetList.
     * 
     * @param rtfParagraphStyle The RtfParagraphStyle to add.
     */
    public void registerParagraphStyle(RtfParagraphStyle rtfParagraphStyle) {
        RtfParagraphStyle tempStyle = new RtfParagraphStyle(this.document, rtfParagraphStyle);
        tempStyle.handleInheritance();
        tempStyle.setStyleNumber(this.styleMap.size());
        this.styleMap.put(tempStyle.getStyleName(), tempStyle);
    }

    /**
     * Registers all default styles. If styles with the given name have already been registered,
     * then they are NOT overwritten.
     */
    private void registerDefaultStyles() {
        defaultsLoaded = true;
        if(!this.styleMap.containsKey(RtfParagraphStyle.STYLE_NORMAL.getStyleName())) {
            registerParagraphStyle(RtfParagraphStyle.STYLE_NORMAL);
        }
        if(!this.styleMap.containsKey(RtfParagraphStyle.STYLE_HEADING_1.getStyleName())) {
            registerParagraphStyle(RtfParagraphStyle.STYLE_HEADING_1);
        }
        if(!this.styleMap.containsKey(RtfParagraphStyle.STYLE_HEADING_2.getStyleName())) {
            registerParagraphStyle(RtfParagraphStyle.STYLE_HEADING_2);
        }
        if(!this.styleMap.containsKey(RtfParagraphStyle.STYLE_HEADING_3.getStyleName())) {
            registerParagraphStyle(RtfParagraphStyle.STYLE_HEADING_3);
        }
    }

    /**
     * Gets the RtfParagraphStyle with the given name. Makes sure that the defaults
     * have been loaded.
     * 
     * @param styleName The name of the RtfParagraphStyle to get.
     * @return The RtfParagraphStyle with the given name or null.
     */
    public RtfParagraphStyle getRtfParagraphStyle(String styleName) {
        if(!defaultsLoaded) {
            registerDefaultStyles();
        }
        if(this.styleMap.containsKey(styleName)) {
            return (RtfParagraphStyle) this.styleMap.get(styleName);
        } else {
            return null;
        }
    }
    
    /**
     * Writes the definition of the stylesheet list.
     */
    public void writeDefinition(final OutputStream result) throws IOException
    {
        result.write(DocWriter.getISOBytes("{"));
        result.write(DocWriter.getISOBytes("\\stylesheet"));
        result.write(RtfBasicElement.DELIMITER);
        this.document.outputDebugLinebreak(result);
        Iterator it = this.styleMap.values().iterator();
        while(it.hasNext()) {
        	RtfParagraphStyle rps = (RtfParagraphStyle)it.next();
        	rps.writeDefinition(result);
        }
        result.write(DocWriter.getISOBytes("}"));
        this.document.outputDebugLinebreak(result);  	
    }
}
