/*
 * $Id: RtfTabGroup.java 3373 2008-05-12 16:21:24Z xlv $
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
import java.util.ArrayList;

import com.lowagie.text.rtf.RtfAddableElement;

/**
 * The RtfTabGroup is a convenience class if the same tabs are to be added
 * to multiple paragraphs.<br /><br />
 * 
 * <code>RtfTabGroup tabs = new RtfTabGroup();<br />
 * tabs.add(new RtfTab(70, RtfTab.TAB_LEFT_ALIGN));<br />
 * tabs.add(new RtfTab(160, RtfTab.TAB_CENTER_ALIGN));<br />
 * tabs.add(new RtfTab(250, RtfTab.TAB_DECIMAL_ALIGN));<br />
 * tabs.add(new RtfTab(500, RtfTab.TAB_RIGHT_ALIGN));<br />
 * Paragraph para = new Paragraph();<br />
 * para.add(tabs);<br />
 * para.add("\tLeft aligned\tCentre aligned\t12,45\tRight aligned");</code>
 * 
 * @version $Id: RtfTabGroup.java 3373 2008-05-12 16:21:24Z xlv $
 * @author Mark Hall (Mark.Hall@mail.room3b.eu)
 * @author Thomas Bickel (tmb99@inode.at)
 */
public class RtfTabGroup extends RtfAddableElement {
	/**
	 * The tabs to add.
	 */
	private ArrayList tabs = null;

	/**
	 * Constructs an empty RtfTabGroup.
	 */
	public RtfTabGroup() {
		this.tabs = new ArrayList();
	}
	
	/**
	 * Constructs a RtfTabGroup with a set of tabs.
	 * 
	 * @param tabs An ArrayList with the RtfTabs to group in this RtfTabGroup.
	 */
	public RtfTabGroup(ArrayList tabs) {
		this.tabs = new ArrayList();
		for(int i = 0; i < tabs.size(); i++) {
			if(tabs.get(i) instanceof RtfTab) {
				this.tabs.add(tabs.get(i));
			}
		}
	}
	
	/**
	 * Adds a RtfTab to the list of grouped tabs.
	 * 
	 * @param tab The RtfTab to add.
	 */
	public void add(RtfTab tab) {
		this.tabs.add(tab);
	}
	
    /**
     * Combines the tab output form all grouped tabs.
     */    
    public void writeContent(final OutputStream result) throws IOException
    {
    	for(int i = 0; i < this.tabs.size(); i++) {
    		RtfTab rt = (RtfTab) this.tabs.get(i);
    		rt.writeContent(result);
    	}
    }        
	
}
