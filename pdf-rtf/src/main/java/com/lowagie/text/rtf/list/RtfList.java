/*
 * $Id: RtfList.java 4065 2009-09-16 23:09:11Z psoares33 $
 *
 * Copyright 2008 Howard Shank (hgshank@yahoo.com)
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

package com.lowagie.text.rtf.list;

import java.awt.Color;
import java.io.IOException;
import java.io.OutputStream;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import com.lowagie.text.error_messages.MessageLocalization;

import com.lowagie.text.Chunk;
import com.lowagie.text.DocWriter;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.List;
import com.lowagie.text.ListItem;
import com.lowagie.text.RomanList;
import com.lowagie.text.factories.RomanAlphabetFactory;
import com.lowagie.text.factories.RomanNumberFactory;
import com.lowagie.text.rtf.RtfBasicElement;
import com.lowagie.text.rtf.RtfElement;
import com.lowagie.text.rtf.RtfExtendedElement;
import com.lowagie.text.rtf.document.RtfDocument;
import com.lowagie.text.rtf.style.RtfFont;
import com.lowagie.text.rtf.style.RtfFontList;
import com.lowagie.text.rtf.text.RtfParagraph;


/**
 * The RtfList stores one List. It also provides the methods to write the
 * list declaration and the list data.
 *  
 * @version $Id: RtfList.java 4065 2009-09-16 23:09:11Z psoares33 $
 * @author Mark Hall (Mark.Hall@mail.room3b.eu)
 * @author Howard Shank (hgshank@yahoo.com)
 * @since 2.1.3
 */
public class RtfList extends RtfElement implements RtfExtendedElement {


    /**
     * Constant for the list number
     * @since 2.1.3
     */
    public static final byte[] LIST_NUMBER = DocWriter.getISOBytes("\\ls");

    /**
     * Constant for the list
     */
    private static final byte[] LIST = DocWriter.getISOBytes("\\list");
    /**
     * Constant for the list id
     * @since 2.1.3
     */
    public static final byte[] LIST_ID = DocWriter.getISOBytes("\\listid");
    /**
     * Constant for the list template id
     */
    private static final byte[] LIST_TEMPLATE_ID = DocWriter.getISOBytes("\\listtemplateid");
    /**
     * Constant for the simple list
     */
    private static final byte[] LIST_SIMPLE = DocWriter.getISOBytes("\\listsimple");
    /**
     * Constant for the hybrid list
     */
    private static final byte[] LIST_HYBRID = DocWriter.getISOBytes("\\listhybrid");
    /**
     * Constant to indicate if the list restarts at each section. Word 7 compatiblity
     */
    private static final byte[] LIST_RESTARTHDN = DocWriter.getISOBytes("\\listrestarthdn");
    /**
     * Constant for the name of this list
     */
    private static final byte[] LIST_NAME = DocWriter.getISOBytes("\\listname");
    /**
     * Constant for the identifier of the style of this list. Mutually exclusive with \\liststylename
     */
    private static final byte[] LIST_STYLEID = DocWriter.getISOBytes("\\liststyleid");
    /**
     * Constant for the identifier of the style of this list. Mutually exclusive with \\liststyleid
     */
    private static final byte[] LIST_STYLENAME = DocWriter.getISOBytes("\\liststylename");

    // character properties
    /**
     * Constant for the list level value
     * @since 2.1.3
     */
    public static final byte[] LIST_LEVEL_NUMBER = DocWriter.getISOBytes("\\ilvl");
    
    
	/**
     * Constant for the old list text
     * @since 2.1.3
     */
    public static final byte[] LIST_TEXT = DocWriter.getISOBytes("\\listtext");
    /**
     * Constant for the old list number end
     * @since 2.1.3
     */
    public static final byte[] LIST_NUMBER_END = DocWriter.getISOBytes(".");
    
    

    /**
     * Constant for a tab character
     * @since 2.1.3
     */
    public static final byte[] TAB = DocWriter.getISOBytes("\\tab");
    
    /**
     * The subitems of this RtfList
     */
    private ArrayList items;
    
    /**
     * The parent list if there is one.
     */
    private RtfList parentList = null;

    /**
     * The list id
     */
    private int listID = -1;
    
    /**
     * List type of NORMAL - no control word
     * @since 2.1.3
     */
    public static final int LIST_TYPE_NORMAL = 0;				/*  Normal list type */
    
    /**
     * List type of listsimple
     * @since 2.1.3
     */
    public static final int LIST_TYPE_SIMPLE = 1;				/*  Simple list type */
    
    /**
     * List type of listhybrid
     * @since 2.1.3
     */
    public static final int LIST_TYPE_HYBRID = 2;				/*  Hybrid list type */
    
    /**
     * This RtfList type
     */
    private int listType = LIST_TYPE_HYBRID;
    
    /**
     * The name of the list if it exists 
     */
    private String name = null;
    
    /**
     * The list number of this RtfList
     */
    private int listNumber = -1;

    /**
     * The RtfList lists managed by this RtfListTable
     */
    private ArrayList listLevels = null;;

    
    /**
     * Constructs an empty RtfList object.
     * @since 2.1.3
     */
    public RtfList() {
    	super(null);
        createDefaultLevels();
    }
    
    /**
     * Set the document.
     * @param doc The RtfDocument
     * @since 2.1.3
     */
    public void setDocument(RtfDocument doc) {
    	this.document = doc;
        // get the list number or create a new one adding it to the table
        this.listNumber = document.getDocumentHeader().getListNumber(this); 

    	
    }
    /**
     * Constructs an empty RtfList object.
     * @param doc The RtfDocument this RtfList belongs to
     * @since 2.1.3
     */
    public RtfList(RtfDocument doc) {
        super(doc);
        createDefaultLevels();
        // get the list number or create a new one adding it to the table
        this.listNumber = document.getDocumentHeader().getListNumber(this); 

    }

    
    /**
     * Constructs a new RtfList for the specified List.
     * 
     * @param doc The RtfDocument this RtfList belongs to
     * @param list The List this RtfList is based on
     * @since 2.1.3
     */
    public RtfList(RtfDocument doc, List list) {
        // setup the listlevels
        // Then, setup the list data below
        
        // setup 1 listlevel if it's a simple list
        // setup 9 if it's a regular list
        // setup 9 if it's a hybrid list (default)
        super(doc);

        createDefaultLevels();
        
        this.items = new ArrayList();		// list content
        RtfListLevel ll = (RtfListLevel)this.listLevels.get(0);
        
        // get the list number or create a new one adding it to the table
        this.listNumber = document.getDocumentHeader().getListNumber(this); 
        
        if(list.getSymbolIndent() > 0 && list.getIndentationLeft() > 0) {
            ll.setFirstIndent((int) (list.getSymbolIndent() * RtfElement.TWIPS_FACTOR * -1));
            ll.setLeftIndent((int) ((list.getIndentationLeft() + list.getSymbolIndent()) * RtfElement.TWIPS_FACTOR));
        } else if(list.getSymbolIndent() > 0) {
        	ll.setFirstIndent((int) (list.getSymbolIndent() * RtfElement.TWIPS_FACTOR * -1));
        	ll.setLeftIndent((int) (list.getSymbolIndent() * RtfElement.TWIPS_FACTOR));
        } else if(list.getIndentationLeft() > 0) {
        	ll.setFirstIndent(0);
        	ll.setLeftIndent((int) (list.getIndentationLeft() * RtfElement.TWIPS_FACTOR));
        } else {
        	ll.setFirstIndent(0);
        	ll.setLeftIndent(0);
        }
        ll.setRightIndent((int) (list.getIndentationRight() * RtfElement.TWIPS_FACTOR));
        ll.setSymbolIndent((int) ((list.getSymbolIndent() + list.getIndentationLeft()) * RtfElement.TWIPS_FACTOR));
        ll.correctIndentation();
        ll.setTentative(false);
        
        if (list instanceof RomanList) {
			if (list.isLowercase()) {
				ll.setListType(RtfListLevel.LIST_TYPE_LOWER_ROMAN);
			} else {
				ll.setListType(RtfListLevel.LIST_TYPE_UPPER_ROMAN);
			}
		} else if (list.isNumbered()) {
			ll.setListType(RtfListLevel.LIST_TYPE_NUMBERED);
		} else if (list.isLettered()) {
			if (list.isLowercase()) {
				ll.setListType(RtfListLevel.LIST_TYPE_LOWER_LETTERS);
			} else {
				ll.setListType(RtfListLevel.LIST_TYPE_UPPER_LETTERS);
			}
		} 
		else {
//			Paragraph p = new Paragraph();
//			p.add(new Chunk(list.getPreSymbol()) );
//			p.add(list.getSymbol());
//			p.add(new Chunk(list.getPostSymbol()) );
//			ll.setBulletChunk(list.getSymbol());
			ll.setBulletCharacter(list.getPreSymbol() + list.getSymbol().getContent() + list.getPostSymbol());
			ll.setListType(RtfListLevel.LIST_TYPE_BULLET);
		}
        
        // now setup the actual list contents.
        for(int i = 0; i < list.getItems().size(); i++) {
            try {
                Element element = (Element) list.getItems().get(i);
                
                if(element.type() == Element.CHUNK) {
                    element = new ListItem((Chunk) element);
                }
                if(element instanceof ListItem) {
                    ll.setAlignment(((ListItem) element).getAlignment());
                }
                RtfBasicElement[] rtfElements = doc.getMapper().mapElement(element);
                for(int j = 0; j < rtfElements.length; j++) {
                    RtfBasicElement rtfElement = rtfElements[j];
                    if(rtfElement instanceof RtfList) {
                        ((RtfList) rtfElement).setParentList(this);
                    } else if(rtfElement instanceof RtfListItem) {
                        ((RtfListItem) rtfElement).setParent(ll);
                    }
                    ll.setFontNumber( new RtfFont(document, new Font(Font.TIMES_ROMAN, 10, Font.NORMAL, new Color(0, 0, 0))) );
                    if (list.getSymbol() != null && list.getSymbol().getFont() != null && !list.getSymbol().getContent().startsWith("-") && list.getSymbol().getContent().length() > 0) {
                        // only set this to bullet symbol is not default
                        ll.setBulletFont( list.getSymbol().getFont());
                        ll.setBulletCharacter(list.getSymbol().getContent().substring(0, 1));
                    } else
                	 if (list.getSymbol() != null && list.getSymbol().getFont() != null) {
                     	ll.setBulletFont(list.getSymbol().getFont());
                	 
                	 } else {
                    	ll.setBulletFont(new Font(Font.SYMBOL, 10, Font.NORMAL, new Color(0, 0, 0)));
                    } 
                    items.add(rtfElement);
                }

            } catch(DocumentException de) {
                de.printStackTrace();
            }
        }
    }
    
    /**
     * Writes the definition part of this list level
     * @param result
     * @throws IOException
     * @since 2.1.3
     */
    public void writeDefinition(final OutputStream result) throws IOException
    {
        result.write(OPEN_GROUP);
        result.write(LIST);
        result.write(LIST_TEMPLATE_ID);
        result.write(intToByteArray(document.getRandomInt()));

        int levelsToWrite = -1;
        
        switch(this.listType) {
        case LIST_TYPE_NORMAL:
        	levelsToWrite = listLevels.size();
        	break;
        case LIST_TYPE_SIMPLE:
            result.write(LIST_SIMPLE);
            result.write(intToByteArray(1)); 
        	levelsToWrite = 1;
        	break;
        case LIST_TYPE_HYBRID:
            result.write(LIST_HYBRID);
        	levelsToWrite = listLevels.size();
        	break;
    	default:
    		break;
        }
        this.document.outputDebugLinebreak(result);

        // TODO: Figure out hybrid because multi-level hybrid does not work.
        // Seems hybrid is mixed type all single level - Simple = single level
        // SIMPLE1/HYRBID
        // 1. Line 1
        // 2. Line 2
        // MULTI-LEVEL LISTS Are Simple0 - 9 levels (0-8) all single digit
        // 1. Line 1
        // 1.1. Line 1.1
        // 1.2. Line 1.2
        // 2. Line 2
         
        // write the listlevels here
        for(int i = 0; i<levelsToWrite; i++) {
        	((RtfListLevel)listLevels.get(i)).writeDefinition(result);
            this.document.outputDebugLinebreak(result);
        }
        
        result.write(LIST_ID);
        result.write(intToByteArray(this.listID));
        result.write(CLOSE_GROUP);
        this.document.outputDebugLinebreak(result);
        if(items != null) {
        for(int i = 0; i < items.size(); i++) {
            RtfElement rtfElement = (RtfElement) items.get(i);
            if(rtfElement instanceof RtfList) {
            	RtfList rl = (RtfList)rtfElement;
            	rl.writeDefinition(result);
                break;
            } else if(rtfElement instanceof RtfListItem) {
            	RtfListItem rli = (RtfListItem) rtfElement;
            	if(rli.writeDefinition(result)) break;
            }
        }    
        }
    }
    
    /**
     * Writes the content of the RtfList
     * @since 2.1.3
    */    
    public void writeContent(final OutputStream result) throws IOException
    {
        if(!this.inTable) {
            result.write(OPEN_GROUP);
        }
        
        int itemNr = 0;
        if(items != null) {
        for(int i = 0; i < items.size(); i++) {
        	
            RtfElement thisRtfElement = (RtfElement) items.get(i);
           //thisRtfElement.writeContent(result);
            if(thisRtfElement instanceof RtfListItem) {
                itemNr++;
            	RtfListItem rtfElement = (RtfListItem)thisRtfElement;
            	RtfListLevel listLevel =  rtfElement.getParent();
                if(listLevel.getListLevel() == 0) {
                    correctIndentation();
                }
                
                if(i == 0) {
                	listLevel.writeListBeginning(result);
                    writeListNumbers(result);
                }

                writeListTextBlock(result, itemNr, listLevel);
                
                rtfElement.writeContent(result);
                
                if(i < (items.size() - 1) || !this.inTable || listLevel.getListType() > 0) { // TODO Fix no paragraph on last list item in tables
                    result.write(RtfParagraph.PARAGRAPH);
                }
                this.document.outputDebugLinebreak(result);
            } else if(thisRtfElement instanceof RtfList) {
            	((RtfList)thisRtfElement).writeContent(result);
//            	((RtfList)thisRtfElement).writeListBeginning(result);
                writeListNumbers(result);
                this.document.outputDebugLinebreak(result);
            }
        }
        }
        if(!this.inTable) {
            result.write(CLOSE_GROUP);
        }
        result.write(RtfParagraph.PARAGRAPH_DEFAULTS);
    }        
    /**
     * 
     * @param result
     * @param itemNr
     * @param listLevel
     * @throws IOException
     * @since 2.1.3
     */
    protected void writeListTextBlock(final OutputStream result, int itemNr, RtfListLevel listLevel) 
    throws IOException {
    	result.write(OPEN_GROUP);
        result.write(RtfList.LIST_TEXT);
        result.write(RtfParagraph.PARAGRAPH_DEFAULTS);
        if(this.inTable) {
            result.write(RtfParagraph.IN_TABLE);
        }
        result.write(RtfFontList.FONT_NUMBER);
        if(listLevel.getListType() != RtfListLevel.LIST_TYPE_BULLET) {
            result.write(intToByteArray(listLevel.getFontNumber().getFontNumber()));
        } else {
            result.write(intToByteArray(listLevel.getFontBullet().getFontNumber()));
        }
        listLevel.writeIndentation(result);
        result.write(DELIMITER);
        if(listLevel.getListType() != RtfListLevel.LIST_TYPE_BULLET) {
            switch(listLevel.getListType()) {
                case RtfListLevel.LIST_TYPE_NUMBERED      : result.write(intToByteArray(itemNr)); break;
                case RtfListLevel.LIST_TYPE_UPPER_LETTERS : result.write(DocWriter.getISOBytes(RomanAlphabetFactory.getUpperCaseString(itemNr))); break;
                case RtfListLevel.LIST_TYPE_LOWER_LETTERS : result.write(DocWriter.getISOBytes(RomanAlphabetFactory.getLowerCaseString(itemNr))); break;
                case RtfListLevel.LIST_TYPE_UPPER_ROMAN   : result.write(DocWriter.getISOBytes(RomanNumberFactory.getUpperCaseString(itemNr))); break;
                case RtfListLevel.LIST_TYPE_LOWER_ROMAN   : result.write(DocWriter.getISOBytes(RomanNumberFactory.getLowerCaseString(itemNr))); break;
            }
            result.write(LIST_NUMBER_END);
        } else {
            this.document.filterSpecialChar(result, listLevel.getBulletCharacter(), true, false);
        }
        result.write(TAB);
        result.write(CLOSE_GROUP);
    }

    /**
     * Writes only the list number and list level number.
     * 
     * @param result The <code>OutputStream</code> to write to
     * @throws IOException On i/o errors.
     * @since 2.1.3
     */
    protected void writeListNumbers(final OutputStream result) throws IOException {
        result.write(RtfList.LIST_NUMBER);
        result.write(intToByteArray(listNumber));
    }
    /**
     * Create a default set of listlevels
     * @since 2.1.3
     */
    protected void createDefaultLevels() {
        this.listLevels = new ArrayList();	// listlevels
        for(int i=0; i<=8; i++) {
            // create a list level
            RtfListLevel ll = new RtfListLevel(this.document);
            ll.setListType(RtfListLevel.LIST_TYPE_NUMBERED);
        	ll.setFirstIndent(0);
        	ll.setLeftIndent(0);
        	ll.setLevelTextNumber(i);
            ll.setTentative(true);
            ll.correctIndentation();
            this.listLevels.add(ll);
        }

    }
    /**
     * Gets the id of this list
     * 
     * @return Returns the list number.
     * @since 2.1.3
     */
    public int getListNumber() {
        return listNumber;
    }
    
    /**
     * Sets the id of this list
     * 
     * @param listNumber The list number to set.
     * @since 2.1.3
     */
    public void setListNumber(int listNumber) {
        this.listNumber = listNumber;
    }
    
    /**
     * Sets whether this RtfList is in a table. Sets the correct inTable setting for all
     * child elements.
     * 
     * @param inTable <code>True</code> if this RtfList is in a table, <code>false</code> otherwise
     * @since 2.1.3
     */
    public void setInTable(boolean inTable) {
        super.setInTable(inTable);
        for(int i = 0; i < this.items.size(); i++) {
        	((RtfBasicElement) this.items.get(i)).setInTable(inTable);
        }
        for(int i = 0; i < this.listLevels.size(); i++) {
        	((RtfListLevel) this.listLevels.get(i)).setInTable(inTable);
        }
    }
    
    /**
     * Sets whether this RtfList is in a header. Sets the correct inTable setting for all
     * child elements.
     * 
     * @param inHeader <code>True</code> if this RtfList is in a header, <code>false</code> otherwise
     * @since 2.1.3
     */
    public void setInHeader(boolean inHeader) {
        super.setInHeader(inHeader);
        for(int i = 0; i < this.items.size(); i++) {
            ((RtfBasicElement) this.items.get(i)).setInHeader(inHeader);
        }
    }

    /**
     * Correct the indentation of this RtfList by adding left/first line indentation
     * from the parent RtfList. Also calls correctIndentation on all child RtfLists.
     * @since 2.1.3
     */
    protected void correctIndentation() {
    	// TODO: Fix
//        if(this.parentList != null) {
//            this.leftIndent = this.leftIndent + this.parentList.getLeftIndent() + this.parentList.getFirstIndent();
//        }
        for(int i = 0; i < this.items.size(); i++) {
            if(this.items.get(i) instanceof RtfList) {
                ((RtfList) this.items.get(i)).correctIndentation();
            } else if(this.items.get(i) instanceof RtfListItem) {
                ((RtfListItem) this.items.get(i)).correctIndentation();
            }
        }
    }


	/**
	 * Set the list ID number
	 * @param id
     * @since 2.1.3
	 */
	public void setID(int id) {
		this.listID = id;
	}
	/**
	 * Get the list ID number
	 * @return this list id
     * @since 2.1.3
	 */
	public int getID() {
		return this.listID;
	}

	/**
	 * @return the listType
	 * @see RtfList#LIST_TYPE_NORMAL
	 * @see RtfList#LIST_TYPE_SIMPLE
	 * @see RtfList#LIST_TYPE_HYBRID
     * @since 2.1.3
	 */
	public int getListType() {
		return listType;
	}

	/**
	 * @param listType the listType to set
	 * @see RtfList#LIST_TYPE_NORMAL
	 * @see RtfList#LIST_TYPE_SIMPLE
	 * @see RtfList#LIST_TYPE_HYBRID
     * @since 2.1.3
	 */
	public void setListType(int listType) throws InvalidParameterException {
		if(listType == LIST_TYPE_NORMAL || 
				listType == LIST_TYPE_SIMPLE || 
				listType == LIST_TYPE_HYBRID ) {
			this.listType = listType;
		}
		else {
			throw new InvalidParameterException(MessageLocalization.getComposedMessage("invalid.listtype.value"));
		}
	}

	/**
	 * @return the parentList
     * @since 2.1.3
	 */
	public RtfList getParentList() {
		return parentList;
	}

	/**
	 * @param parentList the parentList to set
     * @since 2.1.3
	 */
	public void setParentList(RtfList parentList) {
		this.parentList = parentList;
	}

	/**
	 * @return the name
     * @since 2.1.3
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
     * @since 2.1.3
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * @return the list at the index
     * @since 2.1.3
	 */
	public RtfListLevel getListLevel(int index) {
		if(listLevels != null) {
		return (RtfListLevel)this.listLevels.get(index);
		}
		else
			return null;
	}

}
