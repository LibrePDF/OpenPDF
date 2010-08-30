/*
 * $Id: RtfDestinationListTable.java 3735 2009-02-26 01:44:03Z xlv $
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

import com.lowagie.text.Element;
import com.lowagie.text.rtf.list.RtfList;
import com.lowagie.text.rtf.list.RtfListLevel;
import com.lowagie.text.rtf.parser.RtfImportMgr;
import com.lowagie.text.rtf.parser.RtfParser;
import com.lowagie.text.rtf.parser.ctrlwords.RtfCtrlWordData;

/**
 * <code>RtfDestinationListTable</code> handles data destined for the List
 * Table destination
 * 
 * @author Howard Shank (hgshank@yahoo.com)
 * @since 2.1.0
 */
public class RtfDestinationListTable extends RtfDestination {
	/**
	 * The RtfImportHeader to add List mappings to.
	 */
	private RtfImportMgr importHeader = null;

	private RtfList newList = null;
	
	private int currentLevel = -1;
	private RtfListLevel currentListLevel = null;
	private int currentListMappingNumber = 0;
	private int currentSubGroupCount = 0;
	
	public RtfDestinationListTable() {
		super(null);
	}

	public RtfDestinationListTable(RtfParser parser) {
		super(parser);
		this.importHeader = parser.getImportManager();
	}

	public void setParser(RtfParser parser) {
		this.rtfParser = parser;
		this.importHeader = parser.getImportManager();
		this.setToDefaults();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.lowagie.text.rtf.parser.destinations.RtfDestination#handleOpenNewGroup()
	 */
	public boolean handleOpeningSubGroup() {
		this.currentSubGroupCount++;
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.lowagie.text.rtf.direct.RtfDestination#closeDestination()
	 */
	public boolean closeDestination() {
//		RtfListTable t = this.rtfParser.getRtfDocument().getDocumentHeader().getListTable();
//		List newlist = new List(List.UNORDERED, List.NUMERICAL, 0xb7);
//		// newlist.set
//		newList = new RtfList(this.rtfParser.getRtfDocument(), newlist);
//		this.rtfParser.getRtfDocument().add(newList);
//		//newList.set
//		// mylist.
//		// this.rtfParser.setTokeniserStateSkipGroup();

//		// may have to create an import mapping for lists because
		// there may be existing lists in the document with a duplicate ID.
		if (this.newList != null) {
			this.rtfParser.getRtfDocument().add(this.newList);
		}
		return true;
	}

	public boolean handleControlWord(RtfCtrlWordData ctrlWordData) {
		boolean result = true;
		boolean skipCtrlWord = false;

		if (this.rtfParser.isImport()) {
			skipCtrlWord = true;
			if (ctrlWordData.ctrlWord.equals("listtable")) {
				result = true;
				this.currentListMappingNumber = 0;
				
			} else
				/* Picture info for icons/images for lists */
				if (ctrlWordData.ctrlWord.equals("listpicture"))/* DESTINATION */{
					skipCtrlWord = true;
					// this.rtfParser.setTokeniserStateSkipGroup();
					result = true;
				} else
					/* list */
					if (ctrlWordData.ctrlWord.equals("list")) /* DESTINATION */{
						skipCtrlWord = true;
						this.newList = new RtfList(this.rtfParser.getRtfDocument());
						this.newList.setListType(RtfList.LIST_TYPE_NORMAL);	// set default
						this.currentLevel = -1;
						this.currentListMappingNumber++;
						this.currentSubGroupCount = 0;
						result = true;
					} else if (ctrlWordData.ctrlWord.equals("listtemplateid")) /* // List item*/ {
						// ignore this because it gets regenerated in every document
						skipCtrlWord = true;
						result = true;
					} else if (ctrlWordData.ctrlWord.equals("listsimple")) /* // List item*/ {
						// is value 0 or 1
						if(ctrlWordData.hasParam && ctrlWordData.param == "1") {
							this.newList.setListType(RtfList.LIST_TYPE_SIMPLE);
						} else
						{
							this.newList.setListType(RtfList.LIST_TYPE_NORMAL);
						}
						skipCtrlWord = true;
						result = true;
						// this gets set internally. Don't think it should be imported
					} else if (ctrlWordData.ctrlWord.equals("listhybrid")) /* // List item*/ {
						this.newList.setListType(RtfList.LIST_TYPE_HYBRID);
						skipCtrlWord = true;
						result = true;
						// this gets set internally. Don't think it should be imported
					} else if (ctrlWordData.ctrlWord.equals("listrestarthdn")) /* // List item*/ {
						skipCtrlWord = true;
						result = true;
					} else if (ctrlWordData.ctrlWord.equals("listid")) {	// List item cannot be between -1 and -5
						// needs to be mapped for imports and is recreated
						// we have the new id and the old id. Just add it to the mapping table here.
						skipCtrlWord = true;
						result = true;
					} else if (ctrlWordData.ctrlWord.equals("listname"))/* // List item*/ {
						this.newList.setName(ctrlWordData.param);
						skipCtrlWord = true;
						result = true;
					} else if (ctrlWordData.ctrlWord.equals("liststyleid"))/* // List item*/ {
						skipCtrlWord = true;
						result = true;
					} else if (ctrlWordData.ctrlWord.equals("liststylename"))/* // List item*/ {
						skipCtrlWord = true;
						result = true;
					} else
						/* listlevel */
						if (ctrlWordData.ctrlWord.equals("listlevel")) /* DESTINATION There are 1 or 9 listlevels per list */{
							this.currentLevel++;
							this.currentListLevel = this.newList.getListLevel(this.currentLevel);
							this.currentListLevel.setTentative(false);
							skipCtrlWord = true;
							result = true;
						} else if (ctrlWordData.ctrlWord.equals("leveljc")) { // listlevel item justify
							// this is the old number. Only use it if the current type is not set
							if( this.currentListLevel.getAlignment()== RtfListLevel.LIST_TYPE_UNKNOWN) {
								switch(ctrlWordData.intValue()) {
								case 0:
									this.currentListLevel.setAlignment(Element.ALIGN_LEFT);
									break;
								case 1:
									this.currentListLevel.setAlignment(Element.ALIGN_CENTER);
									break;
								case 2:
									this.currentListLevel.setAlignment(Element.ALIGN_RIGHT);
									break;
								}
							}
							skipCtrlWord = true;
							result = true;
						} else if (ctrlWordData.ctrlWord.equals("leveljcn")) { // listlevel item
							//justify
							// if this exists, use it and it overrides the old setting
							switch(ctrlWordData.intValue()) {
							case 0:
								this.currentListLevel.setAlignment(Element.ALIGN_LEFT);
								break;
							case 1:
								this.currentListLevel.setAlignment(Element.ALIGN_CENTER);
								break;
							case 2:
								this.currentListLevel.setAlignment(Element.ALIGN_RIGHT);
								break;
							}
							skipCtrlWord = true;
							result = true;
						} else if (ctrlWordData.ctrlWord.equals("levelstartat")) {
							this.currentListLevel.setListStartAt(ctrlWordData.intValue());
							skipCtrlWord = true;
							result = true;
						} else if (ctrlWordData.ctrlWord.equals("lvltentative")) {
							this.currentListLevel.setTentative(true);
							skipCtrlWord = true;
							result = true;
						} else if (ctrlWordData.ctrlWord.equals("levelold")) {
							// old style. ignore
							skipCtrlWord = true;
							result = true;
						} else if (ctrlWordData.ctrlWord.equals("levelprev")) {
							// old style. ignore
							skipCtrlWord = true;
							result = true;
						} else if (ctrlWordData.ctrlWord.equals("levelprevspace")) {
							// old style. ignore
							skipCtrlWord = true;
							result = true;
						} else if (ctrlWordData.ctrlWord.equals("levelspace")) {
							skipCtrlWord = true;
							result = true;
						} else if (ctrlWordData.ctrlWord.equals("levelindent")) {
							skipCtrlWord = true;
							result = true;
						} else if (ctrlWordData.ctrlWord.equals("leveltext")) {/* FIX */
							skipCtrlWord = true;
							result = true;
						}  else if (ctrlWordData.ctrlWord.equals("levelfollow")) {
							this.currentListLevel.setLevelFollowValue(ctrlWordData.intValue());
							skipCtrlWord = true;
							result = true;
						} else if (ctrlWordData.ctrlWord.equals("levellegal")) {
							this.currentListLevel.setLegal(ctrlWordData.param=="1"?true:false);
							skipCtrlWord = true;
							result = true;
						} else if (ctrlWordData.ctrlWord.equals("levelnorestart")) {
							skipCtrlWord = true;
							result = true;
						} else if (ctrlWordData.ctrlWord.equals("chrfmt")) {/* FIX */
							// set an attribute pair
							skipCtrlWord = true;
							result = true;
						} else if (ctrlWordData.ctrlWord.equals("levelpicture")) {
							skipCtrlWord = true;
							result = true;
						} else if (ctrlWordData.ctrlWord.equals("li")) {
							// set an attribute pair
							skipCtrlWord = true;
							result = true;
						} else if (ctrlWordData.ctrlWord.equals("fi")) {
							// set an attribute pair
							skipCtrlWord = true;
							result = true;
						} else if (ctrlWordData.ctrlWord.equals("jclisttab")) {
							// set an attribute pair
							skipCtrlWord = true;
							result = true;
						} else if (ctrlWordData.ctrlWord.equals("tx")) {
							// set an attribute pair
							skipCtrlWord = true;
							result = true;
						} else
							/* number */
							if (ctrlWordData.ctrlWord.equals("levelnfc")) /* old style */ {
								if( this.currentListLevel.getListType()== RtfListLevel.LIST_TYPE_UNKNOWN) {
									this.currentListLevel.setListType(ctrlWordData.intValue()+RtfListLevel.LIST_TYPE_BASE);
								}
								skipCtrlWord = true;
								result = true;
							} else if (ctrlWordData.ctrlWord.equals("levelnfcn")) /* new style takes priority over levelnfc.*/ {
								this.currentListLevel.setListType(ctrlWordData.intValue()+RtfListLevel.LIST_TYPE_BASE);
								skipCtrlWord = true;
								result = true;
							} else
									/* level text */
									if (ctrlWordData.ctrlWord.equals("leveltemplateid")) {
										// ignore. this value is regenerated in each document.
										skipCtrlWord = true;
										result = true;
									} else
										/* levelnumber */
										if (ctrlWordData.ctrlWord.equals("levelnumbers")) {
											skipCtrlWord = true;
											result = true;
										}
		}

		if (this.rtfParser.isConvert()) {
			if (ctrlWordData.ctrlWord.equals("shppict")) {
				result = true;
			}
			if (ctrlWordData.ctrlWord.equals("nonshppict")) {
				skipCtrlWord = true;
				this.rtfParser.setTokeniserStateSkipGroup();
				result = true;
			}
		}
		if (!skipCtrlWord) {
			switch (this.rtfParser.getConversionType()) {
			case RtfParser.TYPE_IMPORT_FULL:
				// writeBuffer();
				// writeText(ctrlWordData.toString());
				result = true;
				break;
			case RtfParser.TYPE_IMPORT_FRAGMENT:
				// writeBuffer();
				// writeText(ctrlWordData.toString());
				result = true;
				break;
			case RtfParser.TYPE_CONVERT:
				result = true;
				break;
			default: // error because is should be an import or convert
				result = false;
			break;
			}
		}

		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.lowagie.text.rtf.direct.RtfDestination#handleGroupEnd()
	 */
	public boolean handleCloseGroup() {
		this.currentSubGroupCount--;
		if(this.newList != null && this.currentSubGroupCount == 0) {
			this.importHeader.importList(Integer.toString(this.currentListMappingNumber), 
					Integer.toString(this.newList.getListNumber()));
			this.rtfParser.getRtfDocument().add(this.newList);
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.lowagie.text.rtf.direct.RtfDestination#handleGroupStart()
	 */
	public boolean handleOpenGroup() {
		// TODO Auto-generated method stub
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.lowagie.text.rtf.direct.RtfDestination#handleCharacter(int)
	 */
	public boolean handleCharacter(int ch) {
		// TODO Auto-generated method stub
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.lowagie.text.rtf.parser.destinations.RtfDestination#setToDefaults()
	 */
	public void setToDefaults() {
		// TODO Auto-generated method stub

	}

}
