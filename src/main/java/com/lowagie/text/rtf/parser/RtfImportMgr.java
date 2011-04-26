/*
 * $Id: RtfImportMgr.java 3456 2008-05-26 15:26:57Z howard_s $
 *
 * Copyright 2007 by Howard Shank (hgshank@yahoo.com)
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
 * the Initial Developer are Copyright (C) 1999-2006 by Bruno Lowagie.
 * All Rights Reserved.
 * Co-Developer of the code is Paulo Soares. Portions created by the Co-Developer
 * are Copyright (C) 2000-2006 by Paulo Soares. All Rights Reserved.
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
package com.lowagie.text.rtf.parser;


import java.awt.Color;
import java.util.HashMap;

import com.lowagie.text.Document;
import com.lowagie.text.List;
import com.lowagie.text.rtf.document.RtfDocument;
import com.lowagie.text.rtf.list.RtfList;
import com.lowagie.text.rtf.style.RtfColor;
import com.lowagie.text.rtf.style.RtfFont;

/**
 * The RtfImportHeader stores the document header information from
 * an RTF document that is being imported. Currently font and
 * color settings are stored. The RtfImportHeader maintains a mapping
 * from font and color numbers from the imported RTF document to
 * the RTF document that is the target of the import. This guarantees
 * that the merged document has the correct font and color settings.
 * It also handles other list based items that need mapping, for example
 * stylesheets and lists.
 * 
 * @author Mark Hall (Mark.Hall@mail.room3b.eu)
 * @author Howard Shank (hgshank@yahoo.com)
 * @since 2.1.0
 */
public class RtfImportMgr {
    //TODO: Add list, stylesheet, info, etc. mappings
    /**
     * The HashMap storing the font number mappings.
     */
    private HashMap importFontMapping = null;
    /**
     * The HashMap storing the color number mappings.
     */
    private HashMap importColorMapping = null;
    /**
     * The HashMap storing the Stylesheet List number mappings.
     */
    private HashMap importStylesheetListMapping = null;
    /**
     * The HashMap storing the List number mappings.
     */
    private HashMap importListMapping = null;
    /**
     * The RtfDocument to get font and color numbers from.
     */
    private RtfDocument rtfDoc = null;
    /**
     * The Document.
     * Used for conversions, but not imports.
     */
    private Document doc = null;


    /**
     * Constructs a new RtfImportHeader.
     * 
     * @param rtfDoc The RtfDocument to get font and color numbers from.
     */
    public RtfImportMgr(RtfDocument rtfDoc, Document doc) {
        this.rtfDoc = rtfDoc;
        this.doc = doc;
        this.importFontMapping = new HashMap();
        this.importColorMapping = new HashMap();
        this.importStylesheetListMapping = new HashMap();
        this.importListMapping = new HashMap();
    }

    /**
     * Imports a font. The font name is looked up in the RtfDocumentHeader and
     * then the mapping from original font number to actual font number is added.
     * 
     * @param fontNr The original font number.
     * @param fontName The font name to look up.
     */
    public boolean importFont(String fontNr, String fontName) {
        RtfFont rtfFont = new RtfFont(fontName);
        rtfFont.setRtfDocument(this.rtfDoc);
        this.importFontMapping.put(fontNr, Integer.toString(this.rtfDoc.getDocumentHeader().getFontNumber(rtfFont)));
        return true;
    }
    /**
     * Imports a font. The font name is looked up in the RtfDocumentHeader and
     * then the mapping from original font number to actual font number is added.
     * 
     * @param fontNr The original font number.
     * @param fontName The font name to look up.
     * @param charset The character set to use for the font.
     */
    public boolean importFont(String fontNr, String fontName, int charset) {
        RtfFont rtfFont = new RtfFont(fontName);
        if(charset>= 0)
            rtfFont.setCharset(charset);
            rtfFont.setRtfDocument(this.rtfDoc);
            this.importFontMapping.put(fontNr, Integer.toString(this.rtfDoc.getDocumentHeader().getFontNumber(rtfFont)));
            return true;
    }
    /**
     * Imports a font. The font name is looked up in the RtfDocumentHeader and
     * then the mapping from original font number to actual font number is added.
     * 
     * @param fontNr The original font number.
     * @param fontName The font name to look up.
     * @param charset The character set to use for the font.
     */
    public boolean importFont(String fontNr, String fontName, String fontFamily, int charset) {
        RtfFont rtfFont = new RtfFont(fontName);

        if(charset>= 0)
            rtfFont.setCharset(charset);
        if(fontFamily != null && fontFamily.length() > 0)
            rtfFont.setFamily(fontFamily);
        rtfFont.setRtfDocument(this.rtfDoc);
        this.importFontMapping.put(fontNr, Integer.toString(this.rtfDoc.getDocumentHeader().getFontNumber(rtfFont)));
        return true;
    }
    /**
     * Performs the mapping from the original font number to the actual
     * font number in the resulting RTF document. If the font number was not
     * seen during import (thus no mapping) then 0 is returned, guaranteeing
     * that the font number is always valid.
     * 
     * @param fontNr The font number to map.
     * @return The mapped font number.
     */
    public String mapFontNr(String fontNr) {
        if(this.importFontMapping.containsKey(fontNr)) {
            return (String) this.importFontMapping.get(fontNr);
        } else {
            return "0";
        }
    }

    /**
     * Imports a color value. The color number for the color defined
     * by its red, green and blue values is determined and then the
     * resulting mapping is added.
     * 
     * @param colorNr The original color number.
     * @param color The color to import.
     */
    public void importColor(String colorNr, Color color) {
        RtfColor rtfColor = new RtfColor(this.rtfDoc, color);
        this.importColorMapping.put(colorNr, Integer.toString(rtfColor.getColorNumber()));
    }

    /**
     * Performs the mapping from the original font number to the actual font
     * number used in the RTF document. If the color number was not
     * seen during import (thus no mapping) then 0 is returned, guaranteeing
     * that the color number is always valid.
     * 
     * @param colorNr The color number to map.
     * @return The mapped color number
     */
    public String mapColorNr(String colorNr) {
        if(this.importColorMapping.containsKey(colorNr)) {
            return (String) this.importColorMapping.get(colorNr);
        } else {
            return "0";
        }
    }

    /**
     * Imports a List value. The List number for the List defined
     * is determined and then the resulting mapping is added.
     */
    public void importList(String origListNr, String newListNr) {
        this.importListMapping.put(origListNr, newListNr);
    }

    /**
     * Performs the mapping from the original list number to the actual
     * list number in the resulting RTF document. If the list number was not
     * seen during import (thus no mapping) then null is returned. There is no
     * guarantee of a valid list number.
     */
    public String mapListNr(String listNr) {
        if(this.importListMapping.containsKey(listNr)) {
            return (String) this.importListMapping.get(listNr);
        } else {
            return null;
        }
    }

    /**
     * Imports a stylesheet list value. The stylesheet number for the stylesheet defined
     * is determined and then the resulting mapping is added.
     */
    public boolean importStylesheetList(String listNr, List listIn) {
        RtfList rtfList = new RtfList(this.rtfDoc, listIn);
        rtfList.setRtfDocument(this.rtfDoc);
        // TODO HGS - Finish implementation of import
        //this.importStylesheetListMapping.put(listNr, Integer.toString(this.rtfDoc.getDocumentHeader().getRtfParagraphStyle(styleName)(rtfList)));
        return true;
    }
    /**
     * Performs the mapping from the original stylesheet number to the actual
     * stylesheet number in the resulting RTF document. If the stylesheet number was not
     * seen during import (thus no mapping) then 0 is returned, guaranteeing
     * that the stylesheet number is always valid.
     */
    public String mapStylesheetListNr(String listNr) {
        if(this.importStylesheetListMapping.containsKey(listNr)) {
            return (String) this.importStylesheetListMapping.get(listNr);
        } else {
            return "0";
        }
    }

}
