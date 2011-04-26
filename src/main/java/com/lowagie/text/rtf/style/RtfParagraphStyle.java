/*
 * $Id: RtfParagraphStyle.java 3580 2008-08-06 15:52:00Z howard_s $
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
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.rtf.RtfBasicElement;
import com.lowagie.text.rtf.document.RtfDocument;
import com.lowagie.text.rtf.text.RtfParagraph;

/**
 * The RtfParagraphStyle stores all style/formatting attributes of a RtfParagraph.
 * Additionally it also supports the style name system available in RTF. The RtfParagraphStyle
 * is a Font and can thus be used as such. To use the stylesheet functionality
 * it needs to be set as the font of a Paragraph. Otherwise it will work like a
 * RtfFont. It also supports inheritance of styles.
 * 
 * @version $Id: RtfParagraphStyle.java 3580 2008-08-06 15:52:00Z howard_s $
 * @author Mark Hall (Mark.Hall@mail.room3b.eu)
 * @author Thomas Bickel (tmb99@inode.at)
 */
public class RtfParagraphStyle extends RtfFont {

    /**
     * Constant for left alignment
     */
    public static final byte[] ALIGN_LEFT = DocWriter.getISOBytes("\\ql");
    /**
     * Constant for right alignment
     */
    public static final byte[] ALIGN_RIGHT = DocWriter.getISOBytes("\\qr");
    /**
     * Constant for center alignment
     */
    public static final byte[] ALIGN_CENTER = DocWriter.getISOBytes("\\qc");
    /**
     * Constant for justified alignment
     */
    public static final byte[] ALIGN_JUSTIFY = DocWriter.getISOBytes("\\qj");
    /**
     * Constant for the first line indentation
     */
    public static final byte[] FIRST_LINE_INDENT = DocWriter.getISOBytes("\\fi");
    /**
     * Constant for left indentation
     */
    public static final byte[] INDENT_LEFT = DocWriter.getISOBytes("\\li");
    /**
     * Constant for right indentation
     */
    public static final byte[] INDENT_RIGHT = DocWriter.getISOBytes("\\ri");
    /**
     * Constant for keeping the paragraph together on one page
     */
    public static final byte[] KEEP_TOGETHER = DocWriter.getISOBytes("\\keep");
    /**
     * Constant for keeping the paragraph together with the next one on one page
     */
    public static final byte[] KEEP_TOGETHER_WITH_NEXT = DocWriter.getISOBytes("\\keepn");
    /**
     * Constant for the space after the paragraph.
     */
    public static final byte[] SPACING_AFTER = DocWriter.getISOBytes("\\sa");
    /**
     * Constant for the space before the paragraph.
     */
    public static final byte[] SPACING_BEFORE = DocWriter.getISOBytes("\\sb");

    /**
     * The NORMAL/STANDARD style.
     */
    public static final RtfParagraphStyle STYLE_NORMAL = new RtfParagraphStyle("Normal", "Arial", 12, Font.NORMAL, Color.black);
    /**
     * The style for level 1 headings.
     */
    public static final RtfParagraphStyle STYLE_HEADING_1 = new RtfParagraphStyle("heading 1", "Normal");
    /**
     * The style for level 2 headings.
     */
    public static final RtfParagraphStyle STYLE_HEADING_2 = new RtfParagraphStyle("heading 2", "Normal");
    /**
     * The style for level 3 headings.
     */
    public static final RtfParagraphStyle STYLE_HEADING_3 = new RtfParagraphStyle("heading 3", "Normal");

    /**
     * Initializes the properties of the styles.
     */
    static {
        STYLE_HEADING_1.setSize(16);
        STYLE_HEADING_1.setStyle(Font.BOLD);
        STYLE_HEADING_2.setSize(14);
        STYLE_HEADING_2.setStyle(Font.BOLDITALIC);
        STYLE_HEADING_3.setSize(13);
        STYLE_HEADING_3.setStyle(Font.BOLD);
    }
    
    /**
     * No modification has taken place when compared to the RtfParagraphStyle this RtfParagraphStyle
     * is based on. These modification markers are used to determine what needs to be
     * inherited and what not from the parent RtfParagraphStyle.
     */
    private static final int MODIFIED_NONE = 0;
    /**
     * The alignment has been modified.
     */
    private static final int MODIFIED_ALIGNMENT = 1;
    /**
     * The left indentation has been modified.
     */
    private static final int MODIFIED_INDENT_LEFT = 2;
    /**
     * The right indentation has been modified.
     */
    private static final int MODIFIED_INDENT_RIGHT = 4;
    /**
     * The spacing before a paragraph has been modified.
     */
    private static final int MODIFIED_SPACING_BEFORE = 8;
    /**
     * The spacing after a paragraph has been modified.
     */
    private static final int MODIFIED_SPACING_AFTER = 16;
    /**
     * The font name has been modified.
     */
    private static final int MODIFIED_FONT_NAME = 32;
    /**
     * The font style has been modified.
     */
    private static final int MODIFIED_FONT_SIZE = 64;
    /**
     * The font size has been modified.
     */
    private static final int MODIFIED_FONT_STYLE = 128;
    /**
     * The font color has been modified.
     */
    private static final int MODIFIED_FONT_COLOR = 256;
    /**
     * The line leading has been modified. 
     */
    private static final int MODIFIED_LINE_LEADING = 512;
    /**
     * The paragraph keep together setting has been modified.
     */
    private static final int MODIFIED_KEEP_TOGETHER = 1024;
    /**
     * The paragraph keep together with next setting has been modified.
     */
    private static final int MODIFIED_KEEP_TOGETHER_WITH_NEXT = 2048;
    
    /**
     * The alignment of the paragraph.
     */
    private int alignment = Element.ALIGN_LEFT;
    /**
     * The indentation for the first line
     */
    private int firstLineIndent = 0;
    /**
     * The left indentation of the paragraph.
     */
    private int indentLeft = 0;
    /**
     * The right indentation of the paragraph.
     */
    private int indentRight = 0;
    /**
     * The spacing before a paragraph.
     */
    private int spacingBefore = 0;
    /**
     * The spacing after a paragraph.
     */
    private int spacingAfter = 0;
    /**
     * The line leading of the paragraph.
     */
    private int lineLeading = 0;
    /**
     * Whether this RtfParagraph must stay on one page.
     */
    private boolean keepTogether = false;
    /**
     * Whether this RtfParagraph must stay on the same page as the next paragraph.
     */
    private boolean keepTogetherWithNext = false;
    /**
     * The name of this RtfParagraphStyle.
     */
    private String styleName = "";
    /**
     * The name of the RtfParagraphStyle this RtfParagraphStyle is based on.
     */
    private String basedOnName = null;
    /**
     * The RtfParagraphStyle this RtfParagraphStyle is based on.
     */
    private RtfParagraphStyle baseStyle = null;
    /**
     * Which properties have been modified when compared to the base style.
     */
    private int modified = MODIFIED_NONE;
    /**
     * The number of this RtfParagraphStyle in the stylesheet list.
     */
    private int styleNumber = -1;
    
    /**
     * Constructs a new RtfParagraphStyle with the given attributes.
     * 
     * @param styleName The name of this RtfParagraphStyle.
     * @param fontName The name of the font to use for this RtfParagraphStyle.
     * @param fontSize The size of the font to use for this RtfParagraphStyle.
     * @param fontStyle The style of the font to use for this RtfParagraphStyle.
     * @param fontColor The color of the font to use for this RtfParagraphStyle.
     */
    public RtfParagraphStyle(String styleName, String fontName, int fontSize, int fontStyle, Color fontColor) {
        super(null, new RtfFont(fontName, fontSize, fontStyle, fontColor));
        this.styleName = styleName;
    }
    
    /**
     * Constructs a new RtfParagraphStyle that is based on an existing RtfParagraphStyle.
     * 
     * @param styleName The name of this RtfParagraphStyle.
     * @param basedOnName The name of the RtfParagraphStyle this RtfParagraphStyle is based on.
     */
    public RtfParagraphStyle(String styleName, String basedOnName) {
        super(null, new Font());
        this.styleName = styleName;
        this.basedOnName = basedOnName;
    }
    
    /**
     * Constructs a RtfParagraphStyle from another RtfParagraphStyle.
     * 
     * INTERNAL USE ONLY
     * 
     * @param doc The RtfDocument this RtfParagraphStyle belongs to.
     * @param style The RtfParagraphStyle to copy settings from.
     */
    public RtfParagraphStyle(RtfDocument doc, RtfParagraphStyle style) {
        super(doc, style);
        this.document = doc;
        this.styleName = style.getStyleName();
        this.alignment = style.getAlignment();
        this.firstLineIndent = (int)(style.getFirstLineIndent() * RtfBasicElement.TWIPS_FACTOR);
        this.indentLeft = (int) (style.getIndentLeft() * RtfBasicElement.TWIPS_FACTOR);
        this.indentRight = (int) (style.getIndentRight() * RtfBasicElement.TWIPS_FACTOR);
        this.spacingBefore = (int) (style.getSpacingBefore() * RtfBasicElement.TWIPS_FACTOR);
        this.spacingAfter = (int) (style.getSpacingAfter() * RtfBasicElement.TWIPS_FACTOR);
        this.lineLeading = (int) (style.getLineLeading() * RtfBasicElement.TWIPS_FACTOR);
        this.keepTogether = style.getKeepTogether();
        this.keepTogetherWithNext = style.getKeepTogetherWithNext();
        this.basedOnName = style.basedOnName;
        this.modified = style.modified;
        this.styleNumber = style.getStyleNumber();

        if(this.document != null) {
            setRtfDocument(this.document);
        }
    }

    /**
     * Gets the name of this RtfParagraphStyle.
     * 
     * @return The name of this RtfParagraphStyle.
     */
    public String getStyleName() {
        return this.styleName;
    }
    
    /**
     * Gets the name of the RtfParagraphStyle this RtfParagraphStyle is based on.
     * 
     * @return The name of the base RtfParagraphStyle.
     */
    public String getBasedOnName() {
        return this.basedOnName;
    }
    
    /**
     * Gets the alignment of this RtfParagraphStyle.
     * 
     * @return The alignment of this RtfParagraphStyle.
     */
    public int getAlignment() {
        return this.alignment;
    }

    /**
     * Sets the alignment of this RtfParagraphStyle.
     * 
     * @param alignment The alignment to use.
     */
    public void setAlignment(int alignment) {
        this.modified = this.modified | MODIFIED_ALIGNMENT;
        this.alignment = alignment;
    }
    
    /**
     * Gets the first line indentation of this RtfParagraphStyle.
     * 
     * @return The first line indentation of this RtfParagraphStyle.
     */
    public int getFirstLineIndent() {
        return this.firstLineIndent;
    }
    
    /**
     * Sets the first line indentation of this RtfParagraphStyle. It
     * is relative to the left indentation.
     * 
     * @param firstLineIndent The first line indentation to use.
     */
    public void setFirstLineIndent(int firstLineIndent) {
        this.firstLineIndent = firstLineIndent;
    }
    
    /**
     * Gets the left indentation of this RtfParagraphStyle.
     * 
     * @return The left indentation of this RtfParagraphStyle.
     */
    public int getIndentLeft() {
        return this.indentLeft;
    }

    /**
     * Sets the left indentation of this RtfParagraphStyle.
     * 
     * @param indentLeft The left indentation to use.
     */
    public void setIndentLeft(int indentLeft) {
        this.modified = this.modified | MODIFIED_INDENT_LEFT;
        this.indentLeft = indentLeft;
    }
    
    /**
     * Gets the right indentation of this RtfParagraphStyle.
     * 
     * @return The right indentation of this RtfParagraphStyle.
     */
    public int getIndentRight() {
        return this.indentRight;
    }

    /**
     * Sets the right indentation of this RtfParagraphStyle.
     * 
     * @param indentRight The right indentation to use.
     */
    public void setIndentRight(int indentRight) {
        this.modified = this.modified | MODIFIED_INDENT_RIGHT;
        this.indentRight = indentRight;
    }
    
    /**
     * Gets the space before the paragraph of this RtfParagraphStyle..
     * 
     * @return The space before the paragraph.
     */
    public int getSpacingBefore() {
        return this.spacingBefore;
    }

    /**
     * Sets the space before the paragraph of this RtfParagraphStyle.
     * 
     * @param spacingBefore The space before to use.
     */
    public void setSpacingBefore(int spacingBefore) {
        this.modified = this.modified | MODIFIED_SPACING_BEFORE;
        this.spacingBefore = spacingBefore;
    }
    
    /**
     * Gets the space after the paragraph of this RtfParagraphStyle.
     * 
     * @return The space after the paragraph.
     */
    public int getSpacingAfter() {
        return this.spacingAfter;
    }
    
    /**
     * Sets the space after the paragraph of this RtfParagraphStyle.
     * 
     * @param spacingAfter The space after to use.
     */
    public void setSpacingAfter(int spacingAfter) {
        this.modified = this.modified | MODIFIED_SPACING_AFTER;
        this.spacingAfter = spacingAfter;
    }
    
    /**
     * Sets the font name of this RtfParagraphStyle.
     * 
     * @param fontName The font name to use 
     */
    public void setFontName(String fontName) {
        this.modified = this.modified | MODIFIED_FONT_NAME;
        super.setFontName(fontName);
    }
    
    /**
     * Sets the font size of this RtfParagraphStyle.
     * 
     * @param fontSize The font size to use.
     */
    public void setSize(float fontSize) {
        this.modified = this.modified | MODIFIED_FONT_SIZE;
        super.setSize(fontSize);
    }
    
    /**
     * Sets the font style of this RtfParagraphStyle.
     * 
     * @param fontStyle The font style to use.
     */
    public void setStyle(int fontStyle) {
        this.modified = this.modified | MODIFIED_FONT_STYLE;
        super.setStyle(fontStyle);
    }
    
    /**
     * Sets the color of this RtfParagraphStyle.
     * 
     * @param color The Color to use.
     */
    public void setColor(Color color) {
        this.modified = this.modified | MODIFIED_FONT_COLOR;
        super.setColor(color);
    }
    
    /**
     * Gets the line leading of this RtfParagraphStyle.
     * 
     * @return The line leading of this RtfParagraphStyle.
     */
    public int getLineLeading() {
        return this.lineLeading;
    }
    
    /**
     * Sets the line leading of this RtfParagraphStyle.
     * 
     * @param lineLeading The line leading to use.
     */
    public void setLineLeading(int lineLeading) {
        this.lineLeading = lineLeading;
        this.modified = this.modified | MODIFIED_LINE_LEADING;
    }
    
    /**
     * Gets whether the lines in the paragraph should be kept together in
     * this RtfParagraphStyle.
     * 
     * @return Whether the lines in the paragraph should be kept together.
     */
    public boolean getKeepTogether() {
        return this.keepTogether;
    }
    
    /**
     * Sets whether the lines in the paragraph should be kept together in
     * this RtfParagraphStyle.
     * 
     * @param keepTogether Whether the lines in the paragraph should be kept together.
     */
    public void setKeepTogether(boolean keepTogether) {
        this.keepTogether = keepTogether;
        this.modified = this.modified | MODIFIED_KEEP_TOGETHER;
    }
    
    /**
     * Gets whether the paragraph should be kept together with the next in
     * this RtfParagraphStyle.
     * 
     * @return Whether the paragraph should be kept together with the next.
     */
    public boolean getKeepTogetherWithNext() {
        return this.keepTogetherWithNext;
    }
    
    /**
     * Sets whether the paragraph should be kept together with the next in
     * this RtfParagraphStyle.
     * 
     * @param keepTogetherWithNext Whether the paragraph should be kept together with the next.
     */
    public void setKeepTogetherWithNext(boolean keepTogetherWithNext) {
        this.keepTogetherWithNext = keepTogetherWithNext;
        this.modified = this.modified | MODIFIED_KEEP_TOGETHER_WITH_NEXT;
    }
    
    /**
     * Handles the inheritance of paragraph style settings. All settings that
     * have not been modified will be inherited from the base RtfParagraphStyle.
     * If this RtfParagraphStyle is not based on another one, then nothing happens.
     */
    public void handleInheritance() {
        if(this.basedOnName != null && this.document.getDocumentHeader().getRtfParagraphStyle(this.basedOnName) != null) {
            this.baseStyle = this.document.getDocumentHeader().getRtfParagraphStyle(this.basedOnName);
            this.baseStyle.handleInheritance();
            if(!((this.modified & MODIFIED_ALIGNMENT) == MODIFIED_ALIGNMENT)) {
                this.alignment = this.baseStyle.getAlignment();
            }
            if(!((this.modified & MODIFIED_INDENT_LEFT) == MODIFIED_INDENT_LEFT)) {
                this.indentLeft = this.baseStyle.getIndentLeft();
            }
            if(!((this.modified & MODIFIED_INDENT_RIGHT) == MODIFIED_INDENT_RIGHT)) {
                this.indentRight = this.baseStyle.getIndentRight();
            }
            if(!((this.modified & MODIFIED_SPACING_BEFORE) == MODIFIED_SPACING_BEFORE)) {
                this.spacingBefore = this.baseStyle.getSpacingBefore();
            }
            if(!((this.modified & MODIFIED_SPACING_AFTER) == MODIFIED_SPACING_AFTER)) {
                this.spacingAfter = this.baseStyle.getSpacingAfter();
            }
            if(!((this.modified & MODIFIED_FONT_NAME) == MODIFIED_FONT_NAME)) {
                setFontName(this.baseStyle.getFontName());
            }
            if(!((this.modified & MODIFIED_FONT_SIZE) == MODIFIED_FONT_SIZE)) {
                setSize(this.baseStyle.getFontSize());
            }
            if(!((this.modified & MODIFIED_FONT_STYLE) == MODIFIED_FONT_STYLE)) {
                setStyle(this.baseStyle.getFontStyle());
            }
            if(!((this.modified & MODIFIED_FONT_COLOR) == MODIFIED_FONT_COLOR)) {
                setColor(this.baseStyle.getColor());
            }
            if(!((this.modified & MODIFIED_LINE_LEADING) == MODIFIED_LINE_LEADING)) {
                setLineLeading(this.baseStyle.getLineLeading());
            }
            if(!((this.modified & MODIFIED_KEEP_TOGETHER) == MODIFIED_KEEP_TOGETHER)) {
                setKeepTogether(this.baseStyle.getKeepTogether());
            }
            if(!((this.modified & MODIFIED_KEEP_TOGETHER_WITH_NEXT) == MODIFIED_KEEP_TOGETHER_WITH_NEXT)) {
                setKeepTogetherWithNext(this.baseStyle.getKeepTogetherWithNext());
            }
        }
    }
    
    /**
     * Writes the settings of this RtfParagraphStyle.
     * 
     * @param result The <code>OutputStream</code> to write to.
     * @throws IOException On i/o errors.
     */
    private void writeParagraphSettings(final OutputStream result) throws IOException {
        if(this.keepTogether) {
            result.write(RtfParagraphStyle.KEEP_TOGETHER);
        }
        if(this.keepTogetherWithNext) {
            result.write(RtfParagraphStyle.KEEP_TOGETHER_WITH_NEXT);
        }
        switch (alignment) {
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
        result.write(FIRST_LINE_INDENT);
        result.write(intToByteArray(this.firstLineIndent));
        result.write(RtfParagraphStyle.INDENT_LEFT);
        result.write(intToByteArray(indentLeft));
        result.write(RtfParagraphStyle.INDENT_RIGHT);
        result.write(intToByteArray(indentRight));
        if(this.spacingBefore > 0) {
            result.write(RtfParagraphStyle.SPACING_BEFORE);
            result.write(intToByteArray(this.spacingBefore));
        }
        if(this.spacingAfter > 0) {
            result.write(RtfParagraphStyle.SPACING_AFTER);
            result.write(intToByteArray(this.spacingAfter));
        }
        if(this.lineLeading > 0) {
            result.write(RtfParagraph.LINE_SPACING);
            result.write(intToByteArray(this.lineLeading));
        }            
    }

    /**
     * Writes the definition of this RtfParagraphStyle for the stylesheet list.
     */
    public void writeDefinition(final OutputStream result) throws IOException 
    {
        result.write(DocWriter.getISOBytes("{"));
        result.write(DocWriter.getISOBytes("\\style"));
        result.write(DocWriter.getISOBytes("\\s"));
        result.write(intToByteArray(this.styleNumber));
        result.write(RtfBasicElement.DELIMITER);
        writeParagraphSettings(result);
        super.writeBegin(result);
        result.write(RtfBasicElement.DELIMITER);
        result.write(DocWriter.getISOBytes(this.styleName));
        result.write(DocWriter.getISOBytes(";"));
        result.write(DocWriter.getISOBytes("}"));
        this.document.outputDebugLinebreak(result);   	
    }
    
    /**
     * Writes the start information of this RtfParagraphStyle.
     *
     * @param result The <code>OutputStream</code> to write to.
     * @throws IOException On i/o errors.
     */
    public void writeBegin(final OutputStream result) throws IOException {
        result.write(DocWriter.getISOBytes("\\s"));
        result.write(intToByteArray(this.styleNumber));
        writeParagraphSettings(result);
    }
    
    /**
     * Unused
     * 
     * @param result The <code>OutputStream</code> that nothing is written to
     * @throws IOException On i/o errors.
     */
    public void writeEnd(final OutputStream result) throws IOException {
    }
    
    /**
     * unused
     */
    public void writeContent(final OutputStream out) throws IOException
    {    	
    }
    
    /**
     * Tests whether two RtfParagraphStyles are equal. Equality
     * is determined via the name.
     */
    public boolean equals(Object o) {
        if(!(o instanceof RtfParagraphStyle)) {
            return false;
        }
        RtfParagraphStyle paragraphStyle = (RtfParagraphStyle) o;
        boolean result = this.getStyleName().equals(paragraphStyle.getStyleName());
        return result;
    }
    
    /**
     * Gets the hash code of this RtfParagraphStyle.
     */
    public int hashCode() {
        return this.styleName.hashCode();
    }
    
    /**
     * Gets the number of this RtfParagraphStyle in the stylesheet list.
     * 
     * @return The number of this RtfParagraphStyle in the stylesheet list.
     */
    private int getStyleNumber() {
        return this.styleNumber;
    }
    
    /**
     * Sets the number of this RtfParagraphStyle in the stylesheet list.
     * 
     * @param styleNumber The number to use.
     */
    protected void setStyleNumber(int styleNumber) {
        this.styleNumber = styleNumber;
    }
}
