/*
 * $Id: RtfTab.java 3580 2008-08-06 15:52:00Z howard_s $
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
 * Co-Developer of the code is Mark Hall. Portions created by the Co-Developer are
 * Copyright (C) 2006 by Mark Hall. All Rights Reserved
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

import com.lowagie.text.DocWriter;
import com.lowagie.text.rtf.RtfAddableElement;

/**
 * The RtfTab encapsulates a tab position and tab type in a paragraph.
 * To add tabs to a paragraph construct new RtfTab objects with the desired
 * tab position and alignment and then add them to the paragraph. In the actual
 * text the tabs are then defined as standard \t characters.<br /><br />
 * 
 * <code>RtfTab tab = new RtfTab(300, RtfTab.TAB_LEFT_ALIGN);<br />
 * Paragraph para = new Paragraph();<br />
 * para.add(tab);<br />
 * para.add("This paragraph has a\ttab defined.");</code>
 * 
 * @version $Id: RtfTab.java 3580 2008-08-06 15:52:00Z howard_s $
 * @author Mark Hall (Mark.Hall@mail.room3b.eu)
 * @author Thomas Bickel (tmb99@inode.at)
 */
public class RtfTab extends RtfAddableElement {

	/**
	 * A tab where the text is left aligned.
	 */
	public static final int TAB_LEFT_ALIGN = 0;
	/**
	 * A tab where the text is center aligned.
	 */
	public static final int TAB_CENTER_ALIGN = 1;
	/**
	 * A tab where the text is right aligned.
	 */
	public static final int TAB_RIGHT_ALIGN = 2;
	/**
	 * A tab where the text is aligned on the decimal character. Which
	 * character that is depends on the language settings of the viewer.
	 */
	public static final int TAB_DECIMAL_ALIGN = 3;
	
	/**
	 * The tab position in twips.
	 */
	private int position = 0;
	/**
	 * The tab alignment.
	 */
	private int type = TAB_LEFT_ALIGN;
	
	/**
	 * Constructs a new RtfTab with the given position and type. The position
	 * is in standard iText points. The type is one of the tab alignment
	 * constants defined in the RtfTab.
	 * 
	 * @param position The position of the tab in points.
	 * @param type The tab type constant.
	 */
	public RtfTab(float position, int type) {
		this.position = (int) Math.round(position * TWIPS_FACTOR);
		switch(type) {
		case TAB_LEFT_ALIGN: this.type = TAB_LEFT_ALIGN; break;
		case TAB_CENTER_ALIGN: this.type = TAB_CENTER_ALIGN; break;
		case TAB_RIGHT_ALIGN: this.type = TAB_RIGHT_ALIGN; break;
		case TAB_DECIMAL_ALIGN: this.type = TAB_DECIMAL_ALIGN; break;
		default: this.type = TAB_LEFT_ALIGN; break;
		}
	}
	
	/**
	 * Writes the tab settings.
	 */
    public void writeContent(final OutputStream result) throws IOException
    {
    	switch(this.type) {
    		case TAB_CENTER_ALIGN: result.write(DocWriter.getISOBytes("\\tqc")); break;
    		case TAB_RIGHT_ALIGN: result.write(DocWriter.getISOBytes("\\tqr")); break;
    		case TAB_DECIMAL_ALIGN: result.write(DocWriter.getISOBytes("\\tqdec")); break;
        }
        result.write(DocWriter.getISOBytes("\\tx"));
        result.write(intToByteArray(this.position));    	
    }
	
}
