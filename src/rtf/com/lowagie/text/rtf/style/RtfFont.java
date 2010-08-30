/*
 * $Id: RtfFont.java 4008 2009-07-07 09:56:52Z blowagie $
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

import java.awt.Color;
import java.io.IOException;
import java.io.OutputStream;

import com.lowagie.text.DocWriter;
import com.lowagie.text.Font;
import com.lowagie.text.rtf.RtfExtendedElement;
import com.lowagie.text.rtf.document.RtfDocument;

/**
 * The RtfFont class stores one font for an rtf document. It extends Font,
 * so can be set as a font, to allow adding of fonts with arbitrary names.
 * BaseFont fontname handling contributed by Craig Fleming. Various fixes
 * Renaud Michel, Werner Daehn.
 *
 * Version: $Id: RtfFont.java 4008 2009-07-07 09:56:52Z blowagie $
 * @author Mark Hall (Mark.Hall@mail.room3b.eu)
 * @author Craig Fleming (rythos@rhana.dhs.org)
 * @author Renaud Michel (r.michel@immedia.be)
 * @author Werner Daehn (Werner.Daehn@BusinessObjects.com)
 * @author Lidong Liu (tmslld@gmail.com)
 * @author Thomas Bickel (tmb99@inode.at)
 */
public class RtfFont extends Font implements RtfExtendedElement {
    /**
     * Constant for the font family to use ("froman")
     */
    private static final byte[] FONT_FAMILY = DocWriter.getISOBytes("\\froman");
    /**
     * Constant for the charset
     */
    private static final byte[] FONT_CHARSET = DocWriter.getISOBytes("\\fcharset");
    /**
     * Constant for the font size
     */
    public static final byte[] FONT_SIZE = DocWriter.getISOBytes("\\fs");
    /**
     * Constant for the bold flag
     */
    private static final byte[] FONT_BOLD = DocWriter.getISOBytes("\\b");
    /**
     * Constant for the italic flag
     */
    private static final byte[] FONT_ITALIC = DocWriter.getISOBytes("\\i");
    /**
     * Constant for the underline flag
     */
    private static final byte[] FONT_UNDERLINE = DocWriter.getISOBytes("\\ul");
    /**
     * Constant for the strikethrough flag
     */
    private static final byte[] FONT_STRIKETHROUGH = DocWriter.getISOBytes("\\strike");
    /**
     * Constant for the double strikethrough flag
     */
    private static final byte[] FONT_DOUBLE_STRIKETHROUGH = DocWriter.getISOBytes("\\striked");
    /**
     * Constant for the shadow flag
     */
    private static final byte[] FONT_SHADOW = DocWriter.getISOBytes("\\shad");
    /**
     * Constant for the outline flag
     */
    private static final byte[] FONT_OUTLINE = DocWriter.getISOBytes("\\outl");
    /**
     * Constant for the embossed flag
     */
    private static final byte[] FONT_EMBOSSED = DocWriter.getISOBytes("\\embo");
    /**
     * Constant for the engraved flag
     */
    private static final byte[] FONT_ENGRAVED = DocWriter.getISOBytes("\\impr");
    /**
     * Constant for hidden text flag
     */
    private static final byte[] FONT_HIDDEN = DocWriter.getISOBytes("\\v");
    
    /**
     * Constant for a plain font
     */
    public static final int STYLE_NONE = 0;
    /**
     * Constant for a bold font
     */
    public static final int STYLE_BOLD = 1;
    /**
     * Constant for an italic font
     */
    public static final int STYLE_ITALIC = 2;
    /**
     * Constant for an underlined font
     */
    public static final int STYLE_UNDERLINE = 4;
    /**
     * Constant for a strikethrough font
     */
    public static final int STYLE_STRIKETHROUGH = 8;
    /**
     * Constant for a double strikethrough font
     */
    public static final int STYLE_DOUBLE_STRIKETHROUGH = 16;
    /**
     * Constant for a shadowed font
     */
    public static final int STYLE_SHADOW = 32;
    /**
     * Constant for an outlined font
     */
    public static final int STYLE_OUTLINE = 64;
    /**
     * Constant for an embossed font
     */
    public static final int STYLE_EMBOSSED = 128;
    /**
     * Constant for an engraved font
     */
    public static final int STYLE_ENGRAVED = 256;
    /**
     * Constant for a font that hides the actual text.
     */
    public static final int STYLE_HIDDEN = 512;
    
    /**
     * Default font
     * @since 2.1.7
     */
    public static final String DEFAULT_FONT = "Times New Roman";

    /**
     * The font name. Defaults to "Times New Roman"
     */
    private String fontName = DEFAULT_FONT;
    /**
     * The font size. Defaults to 10
     */
    private int fontSize = 10;
    /**
     * The font style. Defaults to STYLE_NONE
     */
    private int fontStyle = STYLE_NONE;
    /**
     * The number of this font
     */
    private int fontNumber = 0;
    /**
     * The color of this font
     */
    private RtfColor color = null;
    /**
     * The character set to use for this font
     */
    private int charset = 0;
    /**
     * The RtfDocument this RtfFont belongs to.
     */
    protected RtfDocument document = null;
    
    /**
     * Constructs a RtfFont with the given font name and all other properties
     * at their default values.
     * 
     * @param fontName The font name to use
     */
    public RtfFont(String fontName) {
        super(Font.UNDEFINED, Font.UNDEFINED, Font.UNDEFINED, null);
        this.fontName = fontName;
    }
    
    /**
     * Constructs a RtfFont with the given font name and font size and all other
     * properties at their default values.
     * 
     * @param fontName The font name to use
     * @param size The font size to use
     */
    public RtfFont(String fontName, float size) {
        super(Font.UNDEFINED, size, Font.UNDEFINED, null);
        this.fontName = fontName;
    }
    
    /**
     * Constructs a RtfFont with the given font name, font size and font style and the
     * default color.
     * 
     * @param fontName The font name to use
     * @param size The font size to use
     * @param style The font style to use
     */
    public RtfFont(String fontName, float size, int style) {
        super(Font.UNDEFINED, size, style, null);
        this.fontName = fontName;
    }
    
    /**
     * Constructs a RtfFont with the given font name, font size, font style and
     * color.
     * 
     * @param fontName The font name to use
     * @param size the font size to use
     * @param style The font style to use
     * @param color The font color to use
     */
    public RtfFont(String fontName, float size, int style, Color color) {
        super(Font.UNDEFINED, size, style, color);
        this.fontName = fontName;
    }
    
    /**
     * Constructs a RtfFont with the given font name, font size, font style, color
     * and charset. This can be used when generating non latin-1 text.
     * 
     * @param fontName The font name to use
     * @param size the font size to use
     * @param style The font style to use
     * @param color The font color to use
     * @param charset The charset of the font content
     */
    public RtfFont(String fontName, float size, int style, Color color, int charset) {
        this(fontName, size, style, color);
        this.charset = charset;
    }

    /**
     * Special constructor for the default font
     *
     * @param doc The RtfDocument this font appears in
     * @param fontNumber The id of this font
     */
    protected RtfFont(RtfDocument doc, int fontNumber) {
        this.document = doc;
        this.fontNumber = fontNumber;
        color = new RtfColor(doc, 0, 0, 0);
    }

    /**
     * Constructs a RtfFont from a com.lowagie.text.Font
     * @param doc The RtfDocument this font appears in
     * @param font The Font to use as a base
     */
    public RtfFont(RtfDocument doc, Font font) {
        this.document = doc;
        if(font != null) {
            if(font instanceof RtfFont) {
                this.fontName = ((RtfFont) font).getFontName();
                this.charset = ((RtfFont) font).getCharset();
            } else {
                setToDefaultFamily(font.getFamilyname());
            }
            if(font.getBaseFont() != null) {
                String[][] fontNames = font.getBaseFont().getFullFontName();
                for(int i = 0; i < fontNames.length; i++) {
                    if(fontNames[i][2].equals("0")) {
                        this.fontName = fontNames[i][3];
                        break;
                    } else if(fontNames[i][2].equals("1033") || fontNames[i][2].equals("")) {
                        this.fontName = fontNames[i][3];
                    }
                }
            }

            if(this.fontName.equalsIgnoreCase("unknown")) {
                this.fontName = DEFAULT_FONT;
            }
            
            setSize(font.getSize());
            setStyle(font.getStyle());
            setColor(font.getColor());
            if(document != null) {
            	this.fontNumber = document.getDocumentHeader().getFontNumber(this);
            }
        }

        if(document != null) {
            setRtfDocument(document);
        }
    }

    /**
     * Writes the font definition
     */
    public void writeDefinition(final OutputStream result) throws IOException
    {
        result.write(FONT_FAMILY);
        result.write(FONT_CHARSET);
        result.write(intToByteArray(charset));
        result.write(DELIMITER);
        document.filterSpecialChar(result, fontName, true, false);
    }
    
    /**
     * Writes the font beginning
     *
     * @param result The <code>OutputStream</code> to write to.
     * @throws IOException On i/o errors.
     */
    public void writeBegin(final OutputStream result) throws IOException {
        if(this.fontNumber != Font.UNDEFINED) {
            result.write(RtfFontList.FONT_NUMBER);
            result.write(intToByteArray(fontNumber));
        }
        if(this.fontSize != Font.UNDEFINED) {
            result.write(FONT_SIZE);
            result.write(intToByteArray(fontSize * 2));
        }
        if(this.fontStyle != UNDEFINED) {
            if((fontStyle & STYLE_BOLD) == STYLE_BOLD) {
                result.write(FONT_BOLD);
            }
            if((fontStyle & STYLE_ITALIC) == STYLE_ITALIC) {
                result.write(FONT_ITALIC);
            }
            if((fontStyle & STYLE_UNDERLINE) == STYLE_UNDERLINE) {
                result.write(FONT_UNDERLINE);
            }
            if((fontStyle & STYLE_STRIKETHROUGH) == STYLE_STRIKETHROUGH) {
                result.write(FONT_STRIKETHROUGH);
            }
            if((fontStyle & STYLE_HIDDEN) == STYLE_HIDDEN) {
                result.write(FONT_HIDDEN);
            }
            if((fontStyle & STYLE_DOUBLE_STRIKETHROUGH) == STYLE_DOUBLE_STRIKETHROUGH) {
                result.write(FONT_DOUBLE_STRIKETHROUGH);
                result.write(intToByteArray(1));
            }
            if((fontStyle & STYLE_SHADOW) == STYLE_SHADOW) {
                result.write(FONT_SHADOW);
            }
            if((fontStyle & STYLE_OUTLINE) == STYLE_OUTLINE) {
                result.write(FONT_OUTLINE);
            }
            if((fontStyle & STYLE_EMBOSSED) == STYLE_EMBOSSED) {
                result.write(FONT_EMBOSSED);
            }
            if((fontStyle & STYLE_ENGRAVED) == STYLE_ENGRAVED) {
                result.write(FONT_ENGRAVED);
            }
        }
        if(color != null) {
            color.writeBegin(result);
        }
    }

    /**
     * Write the font end
     *
     * @param result The <code>OutputStream</code> to write to.
     * @throws IOException On i/o errors.
     */
    public void writeEnd(final OutputStream result) throws IOException{
        if(this.fontStyle != UNDEFINED) {
            if((fontStyle & STYLE_BOLD) == STYLE_BOLD) {
                result.write(FONT_BOLD);
                result.write(intToByteArray(0));
            }
            if((fontStyle & STYLE_ITALIC) == STYLE_ITALIC) {
                result.write(FONT_ITALIC);
                result.write(intToByteArray(0));
            }
            if((fontStyle & STYLE_UNDERLINE) == STYLE_UNDERLINE) {
                result.write(FONT_UNDERLINE);
                result.write(intToByteArray(0));
            }
            if((fontStyle & STYLE_STRIKETHROUGH) == STYLE_STRIKETHROUGH) {
                result.write(FONT_STRIKETHROUGH);
                result.write(intToByteArray(0));
            }
            if((fontStyle & STYLE_HIDDEN) == STYLE_HIDDEN) {
                result.write(FONT_HIDDEN);
                result.write(intToByteArray(0));
            }
            if((fontStyle & STYLE_DOUBLE_STRIKETHROUGH) == STYLE_DOUBLE_STRIKETHROUGH) {
                result.write(FONT_DOUBLE_STRIKETHROUGH);
                result.write(intToByteArray(0));
            }
            if((fontStyle & STYLE_SHADOW) == STYLE_SHADOW) {
                result.write(FONT_SHADOW);
                result.write(intToByteArray(0));
            }
            if((fontStyle & STYLE_OUTLINE) == STYLE_OUTLINE) {
                result.write(FONT_OUTLINE);
                result.write(intToByteArray(0));
            }
            if((fontStyle & STYLE_EMBOSSED) == STYLE_EMBOSSED) {
                result.write(FONT_EMBOSSED);
                result.write(intToByteArray(0));
            }
            if((fontStyle & STYLE_ENGRAVED) == STYLE_ENGRAVED) {
                result.write(FONT_ENGRAVED);
                result.write(intToByteArray(0));
            }
        }
    }

    /**
     * unused
     */
    public void writeContent(OutputStream out) throws IOException
    {    	
    }
    
    /**
     * Tests for equality of RtfFonts. RtfFonts are equal if their fontName,
     * fontSize, fontStyle and fontSuperSubscript are equal
     * 
     * @param obj The RtfFont to compare with this RtfFont
     * @return <code>True</code> if the RtfFonts are equal, <code>false</code> otherwise
     */
    public boolean equals(Object obj) {
        if(!(obj instanceof RtfFont)) {
            return false;
        }
        RtfFont font = (RtfFont) obj;
        boolean result = true;
        result = result & this.fontName.equals(font.getFontName());

        return result;
    }

    /**
     * Returns the hash code of this RtfFont. The hash code is the hash code of the
     * string containing the font name + font size + "-" + the font style + "-" + the
     * font super/supscript value.
     * 
     * @return The hash code of this RtfFont
     */
    public int hashCode() {
        return (this.fontName + this.fontSize + "-" + this.fontStyle).hashCode();
    }
    
    /**
     * Gets the font name of this RtfFont
     * 
     * @return The font name
     */
    public String getFontName() {
        return this.fontName;
    }

    /**
     * Sets the font name of this RtfFont.
     * 
     * @param fontName The font name to use 
     */
    protected void setFontName(String fontName) {
        this.fontName = fontName;
        if(document != null) {
            this.fontNumber = document.getDocumentHeader().getFontNumber(this);
        }
    }
    
    /**
     * @see com.lowagie.text.Font#getFamilyname()
     */
    public String getFamilyname() {
        return this.fontName;
    }
    
    /**
     * @see com.lowagie.text.Font#setFamily(String)
     */
    public void setFamily(String family){
        super.setFamily(family);
        setToDefaultFamily(family);
    }
    
    /**
     * Sets the correct font name from the family name.
     * 
     * @param familyname The family name to set the name to.
     */
    private void setToDefaultFamily(String familyname){
        switch (Font.getFamilyIndex(familyname)) {
            case Font.COURIER:
                this.fontName = "Courier";
                break;
            case Font.HELVETICA:
                this.fontName = "Arial";
                break;
            case Font.SYMBOL:
                this.fontName = "Symbol";
                this.charset = 2;
                break;
            case Font.TIMES_ROMAN:
                this.fontName = "Times New Roman";
                break;
            case Font.ZAPFDINGBATS:
                this.fontName = "Windings";
                break;
            default:
                this.fontName = familyname;
        }
    }
    
    /**
     * Gets the font size of this RtfFont
     * 
     * @return The font size
     */
    public int getFontSize() {
        return this.fontSize;
    }
    
    /**
     * @see com.lowagie.text.Font#setSize(float)
     */
    public void setSize(float size){
        super.setSize(size);
        this.fontSize = (int) getSize();
    }

    /**
     * Gets the font style of this RtfFont
     * 
     * @return The font style
     */
    public int getFontStyle() {
        return this.fontStyle;
    }
    
    /**
     * @see com.lowagie.text.Font#setStyle(int)
     */
    public void setStyle(int style){
        super.setStyle(style);
        this.fontStyle = getStyle();
    }
    
    /**
     * @see com.lowagie.text.Font#setStyle(String)
     */
    public void setStyle(String style) {
        super.setStyle(style);
        fontStyle = getStyle();
    }

    /**
     * Gets the charset used for constructing this RtfFont.
     * 
     * @return The charset of this RtfFont.
     */
    public int getCharset() {
        return charset;
    }

    /**
     * Sets the charset used for constructing this RtfFont.
     * 
     * @param charset The charset to use.
     */
    public void setCharset(int charset) {
        this.charset = charset;
    }

    /**
     * Gets the font number of this RtfFont
     * 
     * @return The font number
     */
    public int getFontNumber() {
        return fontNumber;
    }

    /**
     * Sets the RtfDocument this RtfFont belongs to
     * 
     * @param doc The RtfDocument to use
     */
    public void setRtfDocument(RtfDocument doc) {
        this.document = doc;
        if(document != null) {
            this.fontNumber = document.getDocumentHeader().getFontNumber(this);
        }
        if(this.color != null) {
            this.color.setRtfDocument(this.document);
        }
    }

    /**
     * Unused
     * @param inTable
     */
    public void setInTable(boolean inTable) {
    }
    
    /**
     * Unused
     * @param inHeader
     */
    public void setInHeader(boolean inHeader) {
    }
    
    /**
     * @see com.lowagie.text.Font#setColor(Color)
     */
    public void setColor(Color color) {
        super.setColor(color);
        if(color != null) {
            this.color = new RtfColor(document, color);
        } else {
            this.color = null;
        }
    }
    
    /**
     * @see com.lowagie.text.Font#setColor(int, int, int)
     */
    public void setColor(int red, int green, int blue) {
        super.setColor(red,green,blue);
        this.color = new RtfColor(document, red, green, blue);
    }

    /**
     * Transforms an integer into its String representation and then returns the bytes
     * of that string.
     *
     * @param i The integer to convert
     * @return A byte array representing the integer
     */
    protected byte[] intToByteArray(int i) {
        return DocWriter.getISOBytes(Integer.toString(i));
    }

    /**
     * Replaces the attributes that are equal to <VAR>null</VAR> with
     * the attributes of a given font.
     *
     * @param font The surrounding font
     * @return A RtfFont
     */
    public Font difference(Font font) {
        String dFamilyname = font.getFamilyname();
        if(dFamilyname == null || dFamilyname.trim().equals("") || dFamilyname.trim().equalsIgnoreCase("unknown")) {
            dFamilyname = this.fontName;
        }

        float dSize = font.getSize();
        if(dSize == Font.UNDEFINED) {
            dSize = this.getSize();
        }

        int dStyle = Font.UNDEFINED;
        if(this.getStyle() != Font.UNDEFINED && font.getStyle() != Font.UNDEFINED) {
            dStyle = this.getStyle() | font.getStyle();
        } else if(this.getStyle() != Font.UNDEFINED) {
            dStyle = this.getStyle();
        } else if(font.getStyle() != Font.UNDEFINED) {
            dStyle = font.getStyle();
        }

        Color dColor = font.getColor();
        if(dColor == null) {
            dColor = this.getColor();
        }
        
        int dCharset = this.charset;
        if(font instanceof RtfFont) {
            dCharset = ((RtfFont) font).getCharset();
        }
        
        return new RtfFont(dFamilyname, dSize, dStyle, dColor, dCharset);
    }
    
    /**
     * The <code>RtfFont</code> is never a standard font.
     * 
     * @since 2.1.0
     */
    public boolean isStandardFont() {
        return false;
    }
    
    /**
     * Compares this <code>RtfFont</code> to either a {@link com.lowagie.text.Font} or
     * an <code>RtfFont</code>.
     * 
     * @since 2.1.0
     */
    public int compareTo(Object object) {
        if (object == null) {
            return -1;
        }
        if(object instanceof RtfFont) {
            if(this.getFontName().compareTo(((RtfFont) object).getFontName()) != 0) {
                return 1;
            } else {
                return super.compareTo(object);
            }
        } else if(object instanceof Font) {
            return super.compareTo(object);
        } else {
            return -3;
        }
    }
}
