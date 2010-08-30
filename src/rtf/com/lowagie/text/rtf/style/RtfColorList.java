/*
 * $Id: RtfColorList.java 3580 2008-08-06 15:52:00Z howard_s $
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
import java.util.ArrayList;

import com.lowagie.text.DocWriter;
import com.lowagie.text.rtf.RtfElement;
import com.lowagie.text.rtf.RtfExtendedElement;
import com.lowagie.text.rtf.document.RtfDocument;


/**
 * The RtfColorList stores all colors that appear in the document. Black
 * and White are always added
 * 
 * @version $Id: RtfColorList.java 3580 2008-08-06 15:52:00Z howard_s $
 * @author Mark Hall (Mark.Hall@mail.room3b.eu)
 * @author Thomas Bickel (tmb99@inode.at)
 */
public class RtfColorList extends RtfElement implements RtfExtendedElement {

    /**
     * Constant for the beginning of the color table
     */
    private static final byte[] COLOR_TABLE = DocWriter.getISOBytes("\\colortbl");
    
    /**
     * ArrayList containing all colors of this RtfColorList
     */
    ArrayList colorList = new ArrayList();
    
    /**
     * Constructs a new RtfColorList for the RtfDocument. Will add the default
     * black and white colors.
     * 
     * @param doc The RtfDocument this RtfColorList belongs to
     */
    public RtfColorList(RtfDocument doc) {
        super(doc);
        colorList.add(new RtfColor(doc, 0, 0, 0, 0));
        colorList.add(new RtfColor(doc, 255, 255, 255, 1));
    }
    
    /**
     * Returns the index of the given RtfColor in the color list. If the RtfColor
     * is not in the list of colors, then it is added.
     * 
     * @param color The RtfColor for which to get the index
     * @return The index of the RtfColor
     */
    public int getColorNumber(RtfColor color) {
        int colorIndex = -1;
        for(int i = 0; i < colorList.size(); i++) {
            if(colorList.get(i).equals(color)) {
                colorIndex = i;
            }
        }
        if(colorIndex == -1) {
            colorIndex = colorList.size();
            colorList.add(color);
        }
        return colorIndex;
    }
    
    /**
     * unused
     */
    public void writeContent(final OutputStream out) throws IOException
    {    	
    }
    
    /**
     * Write the definition part of the color list. Calls the writeDefinition
     * methods of the RtfColors in the color list. 
     */
    public void writeDefinition(final OutputStream result) throws IOException
    {
        result.write(OPEN_GROUP);
        result.write(COLOR_TABLE);
        for(int i = 0; i < colorList.size(); i++) {
            RtfColor color = (RtfColor) colorList.get(i);
            color.writeDefinition(result);
        }
        result.write(CLOSE_GROUP);
        this.document.outputDebugLinebreak(result);    	
    }
    

}
