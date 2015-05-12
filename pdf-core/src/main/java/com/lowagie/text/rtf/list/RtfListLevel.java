/*
 * $Id: RtfListLevel.java 3580 2008-08-06 15:52:00Z howard_s $
 *
 * Copyright 2008 by Howard Shank (hgshank@yahoo.com)
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

import com.lowagie.text.Chunk;
import com.lowagie.text.DocWriter;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.rtf.RtfElement;
import com.lowagie.text.rtf.RtfExtendedElement;
import com.lowagie.text.rtf.document.RtfDocument;
import com.lowagie.text.rtf.style.RtfColor;
import com.lowagie.text.rtf.style.RtfFont;
import com.lowagie.text.rtf.style.RtfFontList;
import com.lowagie.text.rtf.style.RtfParagraphStyle;
import com.lowagie.text.rtf.text.RtfParagraph;

/**
 * The RtfListLevel is a listlevel object in a list.
 * 
 * @version $Id: RtfListLevel.java 3580 2008-08-06 15:52:00Z howard_s $
 * @author Howard Shank (hgshank@yahoo.com)
 * @since 2.1.3
 */
public class RtfListLevel extends RtfElement implements RtfExtendedElement {
    /**
     * Constant for list level
     */
    private static final byte[] LIST_LEVEL = DocWriter.getISOBytes("\\listlevel");
    /**
     * Constant for list level
     */
    private static final byte[] LIST_LEVEL_TEMPLATE_ID = DocWriter.getISOBytes("\\leveltemplateid");
    /**
     * Constant for list level style old
     */
    private static final byte[] LIST_LEVEL_TYPE = DocWriter.getISOBytes("\\levelnfc");
    /**
     * Constant for list level style new
     */
    private static final byte[] LIST_LEVEL_TYPE_NEW = DocWriter.getISOBytes("\\levelnfcn");
    /**
     * Constant for list level alignment old
     */
    private static final byte[] LIST_LEVEL_ALIGNMENT = DocWriter.getISOBytes("\\leveljc");
    /**
     * Constant for list level alignment new
     */
    private static final byte[] LIST_LEVEL_ALIGNMENT_NEW = DocWriter.getISOBytes("\\leveljcn");
    /**
     * Constant for list level start at
     */
    private static final byte[] LIST_LEVEL_START_AT = DocWriter.getISOBytes("\\levelstartat");
    /**
     * Constant for list level text
     */
    private static final byte[] LIST_LEVEL_TEXT = DocWriter.getISOBytes("\\leveltext");
    /**
     * Constant for the beginning of the list level numbered style
     */
    private static final byte[] LIST_LEVEL_STYLE_NUMBERED_BEGIN = DocWriter.getISOBytes("\\\'02\\\'");
    /**
     * Constant for the end of the list level numbered style
     */
    private static final byte[] LIST_LEVEL_STYLE_NUMBERED_END = DocWriter.getISOBytes(".;");
    /**
     * Constant for the beginning of the list level bulleted style
     */
    private static final byte[] LIST_LEVEL_STYLE_BULLETED_BEGIN = DocWriter.getISOBytes("\\\'01");
    /**
     * Constant for the end of the list level bulleted style
     */
    private static final byte[] LIST_LEVEL_STYLE_BULLETED_END = DocWriter.getISOBytes(";");
    /**
     * Constant for the beginning of the list level numbers
     */
    private static final byte[] LIST_LEVEL_NUMBERS_BEGIN = DocWriter.getISOBytes("\\levelnumbers");
    /**
     * Constant which specifies which character follows the level text
     */
    private static final byte[] LIST_LEVEL_FOLOW = DocWriter.getISOBytes("\\levelfollow");
    /**
     * Constant which specifies the levelspace controlword
     */
    private static final byte[] LIST_LEVEL_SPACE = DocWriter.getISOBytes("\\levelspace");
    /**
     * Constant which specifies the levelindent control word
     */
    private static final byte[] LIST_LEVEL_INDENT = DocWriter.getISOBytes("\\levelindent");
    /**
     * Constant which specifies (1) if list numbers from previous levels should be converted
     * to Arabic numbers; (0) if they should be left with the format specified by their
     * own level's definition.
     */
    private static final byte[] LIST_LEVEL_LEGAL = DocWriter.getISOBytes("\\levellegal");
    /**
     * Constant which specifies 
     * (1) if this level does/does not restart its count each time a super ordinate level is incremented
     * (0) if this level does not restart its count each time a super ordinate level is incremented.
     */
    private static final byte[] LIST_LEVEL_NO_RESTART = DocWriter.getISOBytes("\\levelnorestart");
    /**
     * Constant for the list level numbers
     */
    private static final byte[] LIST_LEVEL_NUMBERS_NUMBERED = DocWriter.getISOBytes("\\\'01");
    /**
     * Constant for the end of the list level numbers
     */
    private static final byte[] LIST_LEVEL_NUMBERS_END = DocWriter.getISOBytes(";");
    
    /**
     * Constant for the first indentation
     */
    private static final byte[] LIST_LEVEL_FIRST_INDENT = DocWriter.getISOBytes("\\fi");
    /**
     * Constant for the symbol indentation
     */
    private static final byte[] LIST_LEVEL_SYMBOL_INDENT = DocWriter.getISOBytes("\\tx");
    
    /**
     * Constant for the lvltentative control word
     */
    private static final byte[] LIST_LEVEL_TENTATIVE = DocWriter.getISOBytes("\\lvltentative");
    /**
     * Constant for the levelpictureN control word
     */
    private static final byte[] LIST_LEVEL_PICTURE = DocWriter.getISOBytes("\\levelpicture");
    

    public static final int LIST_TYPE_NUMBERED = 1;
    public static final int LIST_TYPE_UPPER_LETTERS = 2;
    public static final int LIST_TYPE_LOWER_LETTERS = 3;
    public static final int LIST_TYPE_UPPER_ROMAN = 4;
    public static final int LIST_TYPE_LOWER_ROMAN = 5;

    public static final int LIST_TYPE_UNKNOWN = -1; 					/* unknown type */
    public static final int LIST_TYPE_BASE = 1000; 						/* BASE value to subtract to get RTF Value if above base*/
    public static final int LIST_TYPE_ARABIC = 1000; 					/* 0 Arabic (1, 2, 3) */
    public static final int LIST_TYPE_UPPERCASE_ROMAN_NUMERAL = 1001;	/* 1 Uppercase Roman numeral (I, II, III) */
    public static final int LIST_TYPE_LOWERCASE_ROMAN_NUMERAL = 1002;	/* 2 Lowercase Roman numeral (i, ii, iii)*/
    public static final int LIST_TYPE_UPPERCASE_LETTER = 1003;			/* 3 Uppercase letter (A, B, C)*/
    public static final int LIST_TYPE_LOWERCASE_LETTER = 1004;			/* 4 Lowercase letter (a, b, c)*/
    public static final int LIST_TYPE_ORDINAL_NUMBER = 1005;			/* 5 Ordinal number (1st, 2nd, 3rd)*/
    public static final int LIST_TYPE_CARDINAL_TEXT_NUMBER = 1006;		/* 6 Cardinal text number (One, Two Three)*/
    public static final int LIST_TYPE_ORDINAL_TEXT_NUMBER = 1007;		/* 7 Ordinal text number (First, Second, Third)*/
    public static final int LIST_TYPE_ARABIC_LEADING_ZERO = 1022;		/* 22	Arabic with leading zero (01, 02, 03, ..., 10, 11)*/
    public static final int LIST_TYPE_BULLET = 1023;					/* 23	Bullet (no number at all)*/
    public static final int LIST_TYPE_NO_NUMBER = 1255;				/*  255	No number */
/*
 
10	Kanji numbering without the digit character (*dbnum1)
11	Kanji numbering with the digit character (*dbnum2)
12	46 phonetic katakana characters in "aiueo" order (*aiueo)
13	46 phonetic katakana characters in "iroha" order (*iroha)
14	Double-byte character
15	Single-byte character
16	Kanji numbering 3 (*dbnum3)
17	Kanji numbering 4 (*dbnum4)
18	Circle numbering (*circlenum)
19	Double-byte Arabic numbering	
20	46 phonetic double-byte katakana characters (*aiueo*dbchar)
    21	46 phonetic double-byte katakana characters (*iroha*dbchar)
    22	Arabic with leading zero (01, 02, 03, ..., 10, 11)
    24	Korean numbering 2 (*ganada)
    25	Korean numbering 1 (*chosung)
    26	Chinese numbering 1 (*gb1)
    27	Chinese numbering 2 (*gb2)
    28	Chinese numbering 3 (*gb3)
    29	Chinese numbering 4 (*gb4)
    30	Chinese Zodiac numbering 1 (* zodiac1)
    31	Chinese Zodiac numbering 2 (* zodiac2) 
    32	Chinese Zodiac numbering 3 (* zodiac3)
    33	Taiwanese double-byte numbering 1
    34	Taiwanese double-byte numbering 2
    35	Taiwanese double-byte numbering 3
    36	Taiwanese double-byte numbering 4
    37	Chinese double-byte numbering 1
    38	Chinese double-byte numbering 2
    39	Chinese double-byte numbering 3
    40	Chinese double-byte numbering 4
    41	Korean double-byte numbering 1
    42	Korean double-byte numbering 2
    43	Korean double-byte numbering 3
    44	Korean double-byte numbering 4
    45	Hebrew non-standard decimal 
    46	Arabic Alif Ba Tah
    47	Hebrew Biblical standard
    48	Arabic Abjad style
    255	No number
*/
    /**
     * Whether this RtfList is numbered
     */
    private int listType = LIST_TYPE_UNKNOWN;

    /**
     * The text to use as the bullet character
     */
    private String bulletCharacter = "\u00b7"; 
    /**
     * @since 2.1.4
     */
    private Chunk bulletChunk = null;
    /**
     * The number to start counting at
     */
    private int listStartAt = 1;
    /**
     * The level of this RtfListLevel
     */
    private int listLevel = 0;
    /**
     * The first indentation of this RtfList
     */
    private int firstIndent = 0;
    /**
     * The left indentation of this RtfList
     */
    private int leftIndent = 0;
    /**
     * The right indentation of this RtfList
     */
    private int rightIndent = 0;
    /**
     * The symbol indentation of this RtfList
     */
    private int symbolIndent = 0;
    /**
     * Flag to indicate if the tentative control word should be emitted.
     */
    private boolean isTentative = true;
    /**
     * Flag to indicate if the levellegal control word should be emitted.
     * true  if any list numbers from previous levels should be converted to Arabic numbers; 
     * false if they should be left with the format specified by their own level definition.
     */
    private boolean isLegal = false;
    
    /**
     * Does the list restart numbering each time a super ordinate level is incremented
     */
    private int listNoRestart = 0;
    public static final int LIST_LEVEL_FOLLOW_TAB = 0; 
    public static final int LIST_LEVEL_FOLLOW_SPACE = 1; 
    public static final int LIST_LEVEL_FOLLOW_NOTHING = 2; 
    private int levelFollowValue = LIST_LEVEL_FOLLOW_TAB;

    /**
     * The alignment of this RtfList
     */
    private int alignment = Element.ALIGN_LEFT;
    /**
     * Which picture bullet from the \listpicture destination should be applied
     */
    private int levelPicture = -1;
    
    private int levelTextNumber = 0;
    /**
     * The RtfFont for numbered lists
     */
    private RtfFont fontNumber;
    /**
     * The RtfFont for bulleted lists
     */
    private RtfFont fontBullet;
    
    private int templateID = -1;
    
    private RtfListLevel listLevelParent = null;
    
    /** 
     * Parent list object
     */
    private RtfList parent = null;
    
	public RtfListLevel(RtfDocument doc)
	{
		super(doc);
		templateID = document.getRandomInt();
        setFontNumber( new RtfFont(document, new Font(Font.TIMES_ROMAN, 10, Font.NORMAL, new Color(0, 0, 0))));
        setBulletFont(new Font(Font.SYMBOL, 10, Font.NORMAL, new Color(0, 0, 0)));
	}
	
	public RtfListLevel(RtfDocument doc, RtfList parent)
	{
		super(doc);
		this.parent = parent;
		templateID = document.getRandomInt();
		setFontNumber( new RtfFont(document, new Font(Font.TIMES_ROMAN, 10, Font.NORMAL, new Color(0, 0, 0))));
        setBulletFont(new Font(Font.SYMBOL, 10, Font.NORMAL, new Color(0, 0, 0)));
	}
	
	public RtfListLevel(RtfListLevel ll)
	{
		super(ll.document);
		templateID = document.getRandomInt();
		this.alignment = ll.alignment;
		this.bulletCharacter = ll.bulletCharacter;
		this.firstIndent = ll.firstIndent;
		this.fontBullet = ll.fontBullet;
		this.fontNumber = ll.fontNumber;
		this.inHeader = ll.inHeader;
		this.inTable = ll.inTable;
		this.leftIndent = ll.leftIndent;
		this.listLevel = ll.listLevel;
		this.listNoRestart = ll.listNoRestart;
		this.listStartAt = ll.listStartAt;
		this.listType = ll.listType;
		this.parent = ll.parent;
		this.rightIndent = ll.rightIndent;
		this.symbolIndent = ll.symbolIndent;
	}

	/**
	 * @return the listNoRestart
	 */
	public int getListNoRestart() {
		return listNoRestart;
	}

	/**
	 * @param listNoRestart the listNoRestart to set
	 */
	public void setListNoRestart(int listNoRestart) {
		this.listNoRestart = listNoRestart;
	}

	/**
	 * @return the alignment
	 */
	public int getAlignment() {
		return alignment;
	}

	/**
	 * @param alignment the alignment to set
	 */
	public void setAlignment(int alignment) {
		this.alignment = alignment;
	}

	public void writeDefinition(final OutputStream result) throws IOException {
        result.write(OPEN_GROUP);
        result.write(LIST_LEVEL);
        result.write(LIST_LEVEL_TYPE);
        switch(this.listType) {
            case LIST_TYPE_BULLET        : result.write(intToByteArray(23)); break;
            case LIST_TYPE_NUMBERED      : result.write(intToByteArray(0)); break;
            case LIST_TYPE_UPPER_LETTERS : result.write(intToByteArray(3)); break;
            case LIST_TYPE_LOWER_LETTERS : result.write(intToByteArray(4)); break;
            case LIST_TYPE_UPPER_ROMAN   : result.write(intToByteArray(1)); break;
            case LIST_TYPE_LOWER_ROMAN   : result.write(intToByteArray(2)); break;
            /* New types */
            case LIST_TYPE_ARABIC   	 : result.write(intToByteArray(0)); break;
            case LIST_TYPE_UPPERCASE_ROMAN_NUMERAL   	 : result.write(intToByteArray(1)); break;
            case LIST_TYPE_LOWERCASE_ROMAN_NUMERAL   	 : result.write(intToByteArray(2)); break;
            case LIST_TYPE_UPPERCASE_LETTER   	 : result.write(intToByteArray(3)); break;
            case LIST_TYPE_ORDINAL_NUMBER   	 : result.write(intToByteArray(4)); break;
            case LIST_TYPE_CARDINAL_TEXT_NUMBER   	 : result.write(intToByteArray(5)); break;
            case LIST_TYPE_ORDINAL_TEXT_NUMBER   	 : result.write(intToByteArray(6)); break;
            case LIST_TYPE_LOWERCASE_LETTER   	 : result.write(intToByteArray(7)); break;
            case LIST_TYPE_ARABIC_LEADING_ZERO   	 : result.write(intToByteArray(22)); break;
            case LIST_TYPE_NO_NUMBER   	 : result.write(intToByteArray(255)); break;
            default:	// catch all for other unsupported types
            	if(this.listType >= RtfListLevel.LIST_TYPE_BASE) {
            		result.write(intToByteArray(this.listType - RtfListLevel.LIST_TYPE_BASE));
            	}
            break;
        }
        
        result.write(LIST_LEVEL_TYPE_NEW);
        switch(this.listType) {
            case LIST_TYPE_BULLET        : result.write(intToByteArray(23)); break;
            case LIST_TYPE_NUMBERED      : result.write(intToByteArray(0)); break;
            case LIST_TYPE_UPPER_LETTERS : result.write(intToByteArray(3)); break;
            case LIST_TYPE_LOWER_LETTERS : result.write(intToByteArray(4)); break;
            case LIST_TYPE_UPPER_ROMAN   : result.write(intToByteArray(1)); break;
            case LIST_TYPE_LOWER_ROMAN   : result.write(intToByteArray(2)); break;
            /* New types */
            case LIST_TYPE_ARABIC   	 : result.write(intToByteArray(0)); break;
            case LIST_TYPE_UPPERCASE_ROMAN_NUMERAL   	 : result.write(intToByteArray(1)); break;
            case LIST_TYPE_LOWERCASE_ROMAN_NUMERAL   	 : result.write(intToByteArray(2)); break;
            case LIST_TYPE_UPPERCASE_LETTER   	 : result.write(intToByteArray(3)); break;
            case LIST_TYPE_ORDINAL_NUMBER   	 : result.write(intToByteArray(4)); break;
            case LIST_TYPE_CARDINAL_TEXT_NUMBER   	 : result.write(intToByteArray(5)); break;
            case LIST_TYPE_ORDINAL_TEXT_NUMBER   	 : result.write(intToByteArray(6)); break;
            case LIST_TYPE_LOWERCASE_LETTER   	 : result.write(intToByteArray(7)); break;
            case LIST_TYPE_ARABIC_LEADING_ZERO   	 : result.write(intToByteArray(22)); break;
            case LIST_TYPE_NO_NUMBER   	 : result.write(intToByteArray(255)); break;
            default:	// catch all for other unsupported types
            	if(this.listType >= RtfListLevel.LIST_TYPE_BASE) {
            		result.write(intToByteArray(this.listType - RtfListLevel.LIST_TYPE_BASE));
            	}
            break;
        }
        result.write(LIST_LEVEL_ALIGNMENT);
        result.write(intToByteArray(0));
        result.write(LIST_LEVEL_ALIGNMENT_NEW);
        result.write(intToByteArray(0));
        result.write(LIST_LEVEL_FOLOW);
        result.write(intToByteArray(levelFollowValue));
        result.write(LIST_LEVEL_START_AT);
        result.write(intToByteArray(this.listStartAt));
        if(this.isTentative) {
            result.write(LIST_LEVEL_TENTATIVE);
        }
        if(this.isLegal) {
            result.write(LIST_LEVEL_LEGAL);
        }
        result.write(LIST_LEVEL_SPACE);
        result.write(intToByteArray(0));
        result.write(LIST_LEVEL_INDENT);
        result.write(intToByteArray(0));
        if(levelPicture != -1) {
            result.write(LIST_LEVEL_PICTURE);
            result.write(intToByteArray(levelPicture));
        }
        
        result.write(OPEN_GROUP); // { leveltext
        result.write(LIST_LEVEL_TEXT);
        result.write(LIST_LEVEL_TEMPLATE_ID);
        result.write(intToByteArray(this.templateID));
        /* NEVER seperate the LEVELTEXT elements with a return in between 
         * them or it will not fuction correctly!
         */
        // TODO Needs to be rewritten to support 1-9 levels, not just simple single level
        if(this.listType != LIST_TYPE_BULLET) {
            result.write(LIST_LEVEL_STYLE_NUMBERED_BEGIN);
            if(this.levelTextNumber < 10) {
                result.write(intToByteArray(0));
            }
            result.write(intToByteArray(this.levelTextNumber));
            result.write(LIST_LEVEL_STYLE_NUMBERED_END);
        } else {
            result.write(LIST_LEVEL_STYLE_BULLETED_BEGIN);
            this.document.filterSpecialChar(result, this.bulletCharacter, false, false);
            result.write(LIST_LEVEL_STYLE_BULLETED_END);
        }
        result.write(CLOSE_GROUP);	// } leveltext
        
        result.write(OPEN_GROUP);  // { levelnumbers
        result.write(LIST_LEVEL_NUMBERS_BEGIN);
        if(this.listType != LIST_TYPE_BULLET) {
            result.write(LIST_LEVEL_NUMBERS_NUMBERED);
        }
        result.write(LIST_LEVEL_NUMBERS_END);
        result.write(CLOSE_GROUP);// { levelnumbers
        
        // write properties now
        result.write(RtfFontList.FONT_NUMBER);
        if(this.listType != LIST_TYPE_BULLET) {
            result.write(intToByteArray(fontNumber.getFontNumber()));
        } else {
            result.write(intToByteArray(fontBullet.getFontNumber()));
        }
        result.write(DocWriter.getISOBytes("\\cf"));
//        document.getDocumentHeader().getColorNumber(new RtfColor(this.document,this.getFontNumber().getColor()));
        result.write(intToByteArray(document.getDocumentHeader().getColorNumber(new RtfColor(this.document,this.getFontNumber().getColor()))));
            
        writeIndentation(result);
        result.write(CLOSE_GROUP);
        this.document.outputDebugLinebreak(result);
        
	}
    /**
     * unused
     */    
    public void writeContent(final OutputStream result) throws IOException
    {
    }     
    
    /**
     * Writes only the list number and list level number.
     * 
     * @param result The <code>OutputStream</code> to write to
     * @throws IOException On i/o errors.
     */
    protected void writeListNumbers(final OutputStream result) throws IOException {

        if(listLevel > 0) {
            result.write(RtfList.LIST_LEVEL_NUMBER);
            result.write(intToByteArray(listLevel));
        }
    }
    
    
    /**
     * Write the indentation values for this <code>RtfList</code>.
     * 
     * @param result The <code>OutputStream</code> to write to.
     * @throws IOException On i/o errors.
     */
    public void writeIndentation(final OutputStream result) throws IOException {
        result.write(LIST_LEVEL_FIRST_INDENT);
        result.write(intToByteArray(firstIndent));
        result.write(RtfParagraphStyle.INDENT_LEFT);
        result.write(intToByteArray(leftIndent));
        result.write(RtfParagraphStyle.INDENT_RIGHT);
        result.write(intToByteArray(rightIndent));
        result.write(LIST_LEVEL_SYMBOL_INDENT);
        result.write(intToByteArray(this.leftIndent));

    }
    /**
     * Writes the initialization part of the RtfList
     * 
     * @param result The <code>OutputStream</code> to write to
     * @throws IOException On i/o errors.
     */
    public void writeListBeginning(final OutputStream result) throws IOException {
        result.write(RtfParagraph.PARAGRAPH_DEFAULTS);
        if(this.inTable) {
            result.write(RtfParagraph.IN_TABLE);
        }
        switch (this.alignment) {
            case Element.ALIGN_LEFT:
                result.write(RtfParagraphStyle.ALIGN_LEFT);
                break;
            case Element.ALIGN_RIGHT:
                result.write(RtfParagraphStyle.ALIGN_RIGHT);
                break;
            case Element.ALIGN_CENTER:
                result.write(RtfParagraphStyle.ALIGN_CENTER);
                break;
            case Element.ALIGN_JUSTIFIED:
            case Element.ALIGN_JUSTIFIED_ALL:
                result.write(RtfParagraphStyle.ALIGN_JUSTIFY);
                break;
        }
        writeIndentation(result);
        result.write(RtfFont.FONT_SIZE);
        result.write(intToByteArray(fontNumber.getFontSize() * 2));
        if(this.symbolIndent > 0) {
            result.write(LIST_LEVEL_SYMBOL_INDENT);
            result.write(intToByteArray(this.leftIndent));
        }
    }
    /**
     * Correct the indentation of this level
     */
    protected void correctIndentation() {

        if(this.listLevelParent != null) {
            this.leftIndent = this.leftIndent + this.listLevelParent.getLeftIndent() + this.listLevelParent.getFirstIndent();
        }
    }
    /**
     * Gets the list level of this RtfList
     * 
     * @return Returns the list level.
     */
    public int getListLevel() {
        return listLevel;
    }
    
    
    /**
     * Sets the list level of this RtfList. 
     * 
     * @param listLevel The list level to set.
     */
    public void setListLevel(int listLevel) {
        this.listLevel = listLevel;
    }
    
    
	public String getBulletCharacter() {
		return this.bulletCharacter;
	}
	/**
	 * @return the listStartAt
	 */
	public int getListStartAt() {
		return listStartAt;
	}
	/**
	 * @param listStartAt the listStartAt to set
	 */
	public void setListStartAt(int listStartAt) {
		this.listStartAt = listStartAt;
	}

	/**
	 * @return the firstIndent
	 */
	public int getFirstIndent() {
		return firstIndent;
	}
	/**
	 * @param firstIndent the firstIndent to set
	 */
	public void setFirstIndent(int firstIndent) {
		this.firstIndent = firstIndent;
	}
	/**
	 * @return the leftIndent
	 */
	public int getLeftIndent() {
		return leftIndent;
	}
	/**
	 * @param leftIndent the leftIndent to set
	 */
	public void setLeftIndent(int leftIndent) {
		this.leftIndent = leftIndent;
	}
	/**
	 * @return the rightIndent
	 */
	public int getRightIndent() {
		return rightIndent;
	}
	/**
	 * @param rightIndent the rightIndent to set
	 */
	public void setRightIndent(int rightIndent) {
		this.rightIndent = rightIndent;
	}
	/**
	 * @return the symbolIndent
	 */
	public int getSymbolIndent() {
		return symbolIndent;
	}
	/**
	 * @param symbolIndent the symbolIndent to set
	 */
	public void setSymbolIndent(int symbolIndent) {
		this.symbolIndent = symbolIndent;
	}
	/**
	 * @return the parent
	 */
	public RtfList getParent() {
		return parent;
	}
	/**
	 * @param parent the parent to set
	 */
	public void setParent(RtfList parent) {
		this.parent = parent;
	}
	/**
	 * @param bulletCharacter the bulletCharacter to set
	 */
	public void setBulletCharacter(String bulletCharacter) {
		this.bulletCharacter = bulletCharacter;
	}
	/**
	 * 
	 * @param bulletCharacter
	 * @since 2.1.4
	 */
	public void setBulletChunk(Chunk bulletCharacter) {
		this.bulletChunk = bulletCharacter;
	}
	/**
	 * @return the listType
	 */
	public int getListType() {
		return listType;
	}
	/**
	 * @param listType the listType to set
	 */
	public void setListType(int listType) {
		this.listType = listType;
	}
	/**
	 * set the bullet font
	 * @param f
	 */
	public void setBulletFont(Font f) {
		this.fontBullet = new RtfFont(document, f);
	}

	/**
	 * @return the fontNumber
	 */
	public RtfFont getFontNumber() {
		return fontNumber;
	}

	/**
	 * @param fontNumber the fontNumber to set
	 */
	public void setFontNumber(RtfFont fontNumber) {
		this.fontNumber = fontNumber;
	}

	/**
	 * @return the fontBullet
	 */
	public RtfFont getFontBullet() {
		return fontBullet;
	}

	/**
	 * @param fontBullet the fontBullet to set
	 */
	public void setFontBullet(RtfFont fontBullet) {
		this.fontBullet = fontBullet;
	}

	/**
	 * @return the isTentative
	 */
	public boolean isTentative() {
		return isTentative;
	}

	/**
	 * @param isTentative the isTentative to set
	 */
	public void setTentative(boolean isTentative) {
		this.isTentative = isTentative;
	}

	/**
	 * @return the isLegal
	 */
	public boolean isLegal() {
		return isLegal;
	}

	/**
	 * @param isLegal the isLegal to set
	 */
	public void setLegal(boolean isLegal) {
		this.isLegal = isLegal;
	}

	/**
	 * @return the levelFollowValue
	 */
	public int getLevelFollowValue() {
		return levelFollowValue;
	}

	/**
	 * @param levelFollowValue the levelFollowValue to set
	 */
	public void setLevelFollowValue(int levelFollowValue) {
		this.levelFollowValue = levelFollowValue;
	}

	/**
	 * @return the levelTextNumber
	 */
	public int getLevelTextNumber() {
		return levelTextNumber;
	}

	/**
	 * @param levelTextNumber the levelTextNumber to set
	 */
	public void setLevelTextNumber(int levelTextNumber) {
		this.levelTextNumber = levelTextNumber;
	}

	/**
	 * @return the listLevelParent
	 */
	public RtfListLevel getListLevelParent() {
		return listLevelParent;
	}

	/**
	 * @param listLevelParent the listLevelParent to set
	 */
	public void setListLevelParent(RtfListLevel listLevelParent) {
		this.listLevelParent = listLevelParent;
	}
}
