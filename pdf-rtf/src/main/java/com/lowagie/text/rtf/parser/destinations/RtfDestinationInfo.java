/*
 * $Id: RtfDestinationInfo.java 3373 2008-05-12 16:21:24Z xlv $
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
 
package com.lowagie.text.rtf.parser.destinations;

import com.lowagie.text.Document;
import com.lowagie.text.Meta;
import com.lowagie.text.rtf.document.RtfDocument;
import com.lowagie.text.rtf.document.RtfInfoElement;
import com.lowagie.text.rtf.parser.RtfParser;
import com.lowagie.text.rtf.parser.ctrlwords.RtfCtrlWordData;

/**
 * <code>RtfDestinationInfo</code> handles data destined for the info destination
 * 
 * @author Howard Shank (hgshank@yahoo.com)
 * @since 2.0.8
 */
public class RtfDestinationInfo extends RtfDestination {
	private String elementName = "";
	private String text = "";

	
	public RtfDestinationInfo() {
		super(null);
	}
	/**
	 * Constructs a new RtfDestinationInfo.
	 * 
	 * @param parser The RtfParser object.
	 */
	public RtfDestinationInfo(RtfParser parser, String elementname) {
		super(parser);
		setToDefaults();
		this.elementName = elementname;
	}
	public void setParser(RtfParser parser) {
		this.rtfParser = parser;
		this.setToDefaults();
	}
	public void setElementName(String value) {
		this.elementName = value;
	}
	/* (non-Javadoc)
	 * @see com.lowagie.text.rtf.parser.destinations.RtfDestination#handleOpenNewGroup()
	 */
	public boolean handleOpeningSubGroup() {
		return true;
	}

	/* (non-Javadoc)
	 * @see com.lowagie.text.rtf.direct.RtfDestination#closeDestination()
	 */
	public boolean closeDestination() {
		return true;
	}

	/* (non-Javadoc)
	 * @see com.lowagie.text.rtf.direct.RtfDestination#handleGroupEnd()
	 */
	public boolean handleCloseGroup() {
		if (this.text.length() > 0) {		
			Document doc = this.rtfParser.getDocument();
			if(doc != null) {
				if(this.elementName.equals("author")){
					doc.addAuthor(this.text);
				}
				if(this.elementName.equals("title")){
					doc.addTitle(this.text);
				}
				if(this.elementName.equals("subject")){
					doc.addSubject(this.text);
				}
			} else {
				RtfDocument rtfDoc = this.rtfParser.getRtfDocument();
				if(rtfDoc != null) {
					if(this.elementName.equals("author")){
						Meta meta = new Meta(this.elementName, this.text);
						RtfInfoElement elem = new RtfInfoElement(rtfDoc, meta);
						rtfDoc.getDocumentHeader().addInfoElement(elem);
					}
					if(this.elementName.equals("title")){
						Meta meta = new Meta(this.elementName, this.text);
						RtfInfoElement elem = new RtfInfoElement(rtfDoc, meta);
						rtfDoc.getDocumentHeader().addInfoElement(elem);
					}
					if(this.elementName.equals("subject")){
						Meta meta = new Meta(this.elementName, this.text);
						RtfInfoElement elem = new RtfInfoElement(rtfDoc, meta);
						rtfDoc.getDocumentHeader().addInfoElement(elem);
					}
				}
			}
			this.setToDefaults();
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see com.lowagie.text.rtf.direct.RtfDestination#handleGroupStart()
	 */
	public boolean handleOpenGroup() {

		return true;
	}
	/* (non-Javadoc)
	 * @see com.lowagie.text.rtf.direct.RtfDestination#handleCharacter(char[])
	 */
	public boolean handleCharacter(int ch) {
		this.text += (char)ch;
		return true;
	}

	/* (non-Javadoc)
	 * @see com.lowagie.text.rtf.parser.destinations.RtfDestination#handleControlWord(com.lowagie.text.rtf.parser.ctrlwords.RtfCtrlWordData)
	 */
	public boolean handleControlWord(RtfCtrlWordData ctrlWordData) {
		elementName = ctrlWordData.ctrlWord;
		return true;
	}

	/* (non-Javadoc)
	 * @see com.lowagie.text.rtf.parser.destinations.RtfDestination#setToDefaults()
	 */
	public void setToDefaults() {
		this.text = "";
	}

}
