/*
 * $Id: Chunk.java 4092 2009-11-11 17:58:16Z psoares33 $
 *
 * Copyright 1999, 2000, 2001, 2002 by Bruno Lowagie.
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
 * LGPL license (the "GNU LIBRARY GENERAL PUBLIC LICENSE"), in which case the
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

package com.lowagie.text;

import java.awt.Color;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import com.lowagie.text.error_messages.MessageLocalization;

import com.lowagie.text.pdf.HyphenationEvent;
import com.lowagie.text.pdf.PdfAction;
import com.lowagie.text.pdf.PdfAnnotation;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.draw.DrawInterface;

/**
 * This is the smallest significant part of text that can be added to a
 * document.
 * <P>
 * Most elements can be divided in one or more <CODE>Chunk</CODE>s. A chunk
 * is a <CODE>String</CODE> with a certain <CODE>Font</CODE>. All other
 * layout parameters should be defined in the object to which this chunk of text
 * is added.
 * <P>
 * Example: <BLOCKQUOTE>
 * 
 * <PRE>
 * 
 * <STRONG>Chunk chunk = new Chunk("Hello world",
 * FontFactory.getFont(FontFactory.COURIER, 20, Font.ITALIC, new Color(255, 0,
 * 0))); </STRONG> document.add(chunk);
 * 
 * </PRE>
 * 
 * </BLOCKQUOTE>
 */

public class Chunk implements Element {

	// public static membervariables

	/** The character stand in for an image or a separator. */
	public static final String OBJECT_REPLACEMENT_CHARACTER = "\ufffc";

	/** This is a Chunk containing a newline. */
	public static final Chunk NEWLINE = new Chunk("\n");

	/** This is a Chunk containing a newpage. */
	public static final Chunk NEXTPAGE = new Chunk("");
	static {
		NEXTPAGE.setNewPage();
	}

	// member variables

	/** This is the content of this chunk of text. */
	protected StringBuffer content = null;

	/** This is the <CODE>Font</CODE> of this chunk of text. */
	protected Font font = null;

	/** Contains some of the attributes for this Chunk. */
	protected HashMap attributes = null;

	// constructors

	/**
	 * Empty constructor.
	 */
	public Chunk() {
		this.content = new StringBuffer();
		this.font = new Font();
	}

    /**
     * A <CODE>Chunk</CODE> copy constructor.
     * @param ck the <CODE>Chunk</CODE> to be copied
     */    
    public Chunk(Chunk ck) {
        if (ck.content != null) {
            content = new StringBuffer(ck.content.toString());
        }
        if (ck.font != null) {
            font = new Font(ck.font);
        }
        if (ck.attributes != null) {
            attributes = new HashMap(ck.attributes);
        }
    }
    
	/**
	 * Constructs a chunk of text with a certain content and a certain <CODE>
	 * Font</CODE>.
	 * 
	 * @param content
	 *            the content
	 * @param font
	 *            the font
	 */
	public Chunk(String content, Font font) {
		this.content = new StringBuffer(content);
		this.font = font;
	}

	/**
	 * Constructs a chunk of text with a certain content, without specifying a
	 * <CODE>Font</CODE>.
	 * 
	 * @param content
	 *            the content
	 */
	public Chunk(String content) {
		this(content, new Font());
	}

	/**
	 * Constructs a chunk of text with a char and a certain <CODE>Font</CODE>.
	 * 
	 * @param c
	 *            the content
	 * @param font
	 *            the font
	 */
	public Chunk(char c, Font font) {
		this.content = new StringBuffer();
		this.content.append(c);
		this.font = font;
	}

	/**
	 * Constructs a chunk of text with a char, without specifying a <CODE>Font
	 * </CODE>.
	 * 
	 * @param c
	 *            the content
	 */
	public Chunk(char c) {
		this(c, new Font());
	}

	/**
	 * Constructs a chunk containing an <CODE>Image</CODE>.
	 * 
	 * @param image
	 *            the image
	 * @param offsetX
	 *            the image offset in the x direction
	 * @param offsetY
	 *            the image offset in the y direction
	 */
	public Chunk(Image image, float offsetX, float offsetY) {
		this(OBJECT_REPLACEMENT_CHARACTER, new Font());
		Image copyImage = Image.getInstance(image);
		copyImage.setAbsolutePosition(Float.NaN, Float.NaN);
		setAttribute(IMAGE, new Object[] { copyImage, new Float(offsetX),
				new Float(offsetY), Boolean.FALSE });
	}

	/**
	 * Key for drawInterface of the Separator.
	 * @since	2.1.2
	 */
	public static final String SEPARATOR = "SEPARATOR";
	
	/**
	 * Creates a separator Chunk.
     * Note that separator chunks can't be used in combination with tab chunks!
	 * @param	separator	the drawInterface to use to draw the separator.
	 * @since	2.1.2
	 */
	public Chunk(DrawInterface separator) {
		this(separator, false);
	}	
	
	/**
	 * Creates a separator Chunk.
     * Note that separator chunks can't be used in combination with tab chunks!
	 * @param	separator	the drawInterface to use to draw the separator.
	 * @param	vertical	true if this is a vertical separator
	 * @since	2.1.2
	 */
	public Chunk(DrawInterface separator, boolean vertical) {
		this(OBJECT_REPLACEMENT_CHARACTER, new Font());
		setAttribute(SEPARATOR, new Object[] {separator, Boolean.valueOf(vertical)});
	}

	/**
	 * Key for drawInterface of the tab.
	 * @since	2.1.2
	 */
	public static final String TAB = "TAB";
	
	/**
	 * Creates a tab Chunk.
     * Note that separator chunks can't be used in combination with tab chunks!
	 * @param	separator	the drawInterface to use to draw the tab.
	 * @param	tabPosition	an X coordinate that will be used as start position for the next Chunk.
	 * @since	2.1.2
	 */
	public Chunk(DrawInterface separator, float tabPosition) {
		this(separator, tabPosition, false);
	}
	
	/**
	 * Creates a tab Chunk.
     * Note that separator chunks can't be used in combination with tab chunks!
	 * @param	separator	the drawInterface to use to draw the tab.
	 * @param	tabPosition	an X coordinate that will be used as start position for the next Chunk.
	 * @param	newline		if true, a newline will be added if the tabPosition has already been reached.
	 * @since	2.1.2
	 */
	public Chunk(DrawInterface separator, float tabPosition, boolean newline) {
		this(OBJECT_REPLACEMENT_CHARACTER, new Font());
		if (tabPosition < 0) {
			throw new IllegalArgumentException(MessageLocalization.getComposedMessage("a.tab.position.may.not.be.lower.than.0.yours.is.1", String.valueOf(tabPosition)));
		}
		setAttribute(TAB, new Object[] {separator, new Float(tabPosition), Boolean.valueOf(newline), new Float(0)});
	}

	/**
	 * Constructs a chunk containing an <CODE>Image</CODE>.
	 * 
	 * @param image
	 *            the image
	 * @param offsetX
	 *            the image offset in the x direction
	 * @param offsetY
	 *            the image offset in the y direction
	 * @param changeLeading
	 *            true if the leading has to be adapted to the image
	 */
	public Chunk(Image image, float offsetX, float offsetY,
			boolean changeLeading) {
		this(OBJECT_REPLACEMENT_CHARACTER, new Font());
		setAttribute(IMAGE, new Object[] { image, new Float(offsetX),
				new Float(offsetY), Boolean.valueOf(changeLeading) });
	}

	// implementation of the Element-methods

	/**
	 * Processes the element by adding it (or the different parts) to an <CODE>
	 * ElementListener</CODE>.
	 * 
	 * @param listener
	 *            an <CODE>ElementListener</CODE>
	 * @return <CODE>true</CODE> if the element was processed successfully
	 */
	public boolean process(ElementListener listener) {
		try {
			return listener.add(this);
		} catch (DocumentException de) {
			return false;
		}
	}

	/**
	 * Gets the type of the text element.
	 * 
	 * @return a type
	 */
	public int type() {
		return Element.CHUNK;
	}

	/**
	 * Gets all the chunks in this element.
	 * 
	 * @return an <CODE>ArrayList</CODE>
	 */
	public ArrayList getChunks() {
		ArrayList tmp = new ArrayList();
		tmp.add(this);
		return tmp;
	}

	// methods that change the member variables

	/**
	 * appends some text to this <CODE>Chunk</CODE>.
	 * 
	 * @param string
	 *            <CODE>String</CODE>
	 * @return a <CODE>StringBuffer</CODE>
	 */
	public StringBuffer append(String string) {
		return content.append(string);
	}

	/**
	 * Sets the font of this <CODE>Chunk</CODE>.
	 * 
	 * @param font
	 *            a <CODE>Font</CODE>
	 */
	public void setFont(Font font) {
		this.font = font;
	}

	// methods to retrieve information

	/**
	 * Gets the font of this <CODE>Chunk</CODE>.
	 * 
	 * @return a <CODE>Font</CODE>
	 */
	public Font getFont() {
		return font;
	}

	/**
	 * Returns the content of this <CODE>Chunk</CODE>.
	 * 
	 * @return a <CODE>String</CODE>
	 */
	public String getContent() {
		return content.toString();
	}

	/**
	 * Returns the content of this <CODE>Chunk</CODE>.
	 * 
	 * @return a <CODE>String</CODE>
	 */
	public String toString() {
		return getContent();
	}

	/**
	 * Checks is this <CODE>Chunk</CODE> is empty.
	 * 
	 * @return <CODE>false</CODE> if the Chunk contains other characters than
	 *         space.
	 */
	public boolean isEmpty() {
		return (content.toString().trim().length() == 0)
				&& (content.toString().indexOf("\n") == -1)
				&& (attributes == null);
	}

	/**
	 * Gets the width of the Chunk in points.
	 * 
	 * @return a width in points
	 */
	public float getWidthPoint() {
		if (getImage() != null) {
			return getImage().getScaledWidth();
		}
		return font.getCalculatedBaseFont(true).getWidthPoint(getContent(),
				font.getCalculatedSize())
				* getHorizontalScaling();
	}

	// attributes

	/**
	 * Checks the attributes of this <CODE>Chunk</CODE>.
	 * 
	 * @return false if there aren't any.
	 */

	public boolean hasAttributes() {
		return attributes != null;
	}

	/**
	 * Gets the attributes for this <CODE>Chunk</CODE>.
	 * <P>
	 * It may be null.
	 * 
	 * @return the attributes for this <CODE>Chunk</CODE>
	 */

	public HashMap getAttributes() {
		return attributes;
	}

	/**
	 * Sets the attributes all at once.
	 * @param	attributes	the attributes of a Chunk
	 */
	public void setAttributes(HashMap attributes) {
		this.attributes = attributes;
	}

	/**
	 * Sets an arbitrary attribute.
	 * 
	 * @param name
	 *            the key for the attribute
	 * @param obj
	 *            the value of the attribute
	 * @return this <CODE>Chunk</CODE>
	 */

	private Chunk setAttribute(String name, Object obj) {
		if (attributes == null)
			attributes = new HashMap();
		attributes.put(name, obj);
		return this;
	}

	// the attributes are ordered as they appear in the book 'iText in Action'

	/** Key for text horizontal scaling. */
	public static final String HSCALE = "HSCALE";

	/**
	 * Sets the text horizontal scaling. A value of 1 is normal and a value of
	 * 0.5f shrinks the text to half it's width.
	 * 
	 * @param scale
	 *            the horizontal scaling factor
	 * @return this <CODE>Chunk</CODE>
	 */
	public Chunk setHorizontalScaling(float scale) {
		return setAttribute(HSCALE, new Float(scale));
	}

	/**
	 * Gets the horizontal scaling.
	 * 
	 * @return a percentage in float
	 */
	public float getHorizontalScaling() {
		if (attributes == null)
			return 1f;
		Float f = (Float) attributes.get(HSCALE);
		if (f == null)
			return 1f;
		return f.floatValue();
	}

	/** Key for underline. */
	public static final String UNDERLINE = "UNDERLINE";

	/**
	 * Sets an horizontal line that can be an underline or a strikethrough.
	 * Actually, the line can be anywhere vertically and has always the <CODE>
	 * Chunk</CODE> width. Multiple call to this method will produce multiple
	 * lines.
	 * 
	 * @param thickness
	 *            the absolute thickness of the line
	 * @param yPosition
	 *            the absolute y position relative to the baseline
	 * @return this <CODE>Chunk</CODE>
	 */
	public Chunk setUnderline(float thickness, float yPosition) {
		return setUnderline(null, thickness, 0f, yPosition, 0f,
				PdfContentByte.LINE_CAP_BUTT);
	}

	/**
	 * Sets an horizontal line that can be an underline or a strikethrough.
	 * Actually, the line can be anywhere vertically and has always the <CODE>
	 * Chunk</CODE> width. Multiple call to this method will produce multiple
	 * lines.
	 * 
	 * @param color
	 *            the color of the line or <CODE>null</CODE> to follow the
	 *            text color
	 * @param thickness
	 *            the absolute thickness of the line
	 * @param thicknessMul
	 *            the thickness multiplication factor with the font size
	 * @param yPosition
	 *            the absolute y position relative to the baseline
	 * @param yPositionMul
	 *            the position multiplication factor with the font size
	 * @param cap
	 *            the end line cap. Allowed values are
	 *            PdfContentByte.LINE_CAP_BUTT, PdfContentByte.LINE_CAP_ROUND
	 *            and PdfContentByte.LINE_CAP_PROJECTING_SQUARE
	 * @return this <CODE>Chunk</CODE>
	 */
	public Chunk setUnderline(Color color, float thickness, float thicknessMul,
			float yPosition, float yPositionMul, int cap) {
		if (attributes == null)
			attributes = new HashMap();
		Object obj[] = {
				color,
				new float[] { thickness, thicknessMul, yPosition, yPositionMul, cap } };
		Object unders[][] = Utilities.addToArray((Object[][]) attributes.get(UNDERLINE),
				obj);
		return setAttribute(UNDERLINE, unders);
	}
	
	/** Key for sub/superscript. */
	public static final String SUBSUPSCRIPT = "SUBSUPSCRIPT";
	
	/**
	 * Sets the text displacement relative to the baseline. Positive values rise
	 * the text, negative values lower the text.
	 * <P>
	 * It can be used to implement sub/superscript.
	 * 
	 * @param rise
	 *            the displacement in points
	 * @return this <CODE>Chunk</CODE>
	 */

	public Chunk setTextRise(float rise) {
		return setAttribute(SUBSUPSCRIPT, new Float(rise));
	}

	/**
	 * Gets the text displacement relative to the baseline.
	 * 
	 * @return a displacement in points
	 */
	public float getTextRise() {
		if (attributes != null && attributes.containsKey(SUBSUPSCRIPT)) {
			Float f = (Float) attributes.get(SUBSUPSCRIPT);
			return f.floatValue();
		}
		return 0.0f;
	}

	/** Key for text skewing. */
	public static final String SKEW = "SKEW";

	/**
	 * Skews the text to simulate italic and other effects. Try <CODE>alpha=0
	 * </CODE> and <CODE>beta=12</CODE>.
	 * 
	 * @param alpha
	 *            the first angle in degrees
	 * @param beta
	 *            the second angle in degrees
	 * @return this <CODE>Chunk</CODE>
	 */
	public Chunk setSkew(float alpha, float beta) {
		alpha = (float) Math.tan(alpha * Math.PI / 180);
		beta = (float) Math.tan(beta * Math.PI / 180);
		return setAttribute(SKEW, new float[] { alpha, beta });
	}

	/** Key for background. */
	public static final String BACKGROUND = "BACKGROUND";

	/**
	 * Sets the color of the background <CODE>Chunk</CODE>.
	 * 
	 * @param color
	 *            the color of the background
	 * @return this <CODE>Chunk</CODE>
	 */
	public Chunk setBackground(Color color) {
		return setBackground(color, 0, 0, 0, 0);
	}

	/**
	 * Sets the color and the size of the background <CODE>Chunk</CODE>.
	 * 
	 * @param color
	 *            the color of the background
	 * @param extraLeft
	 *            increase the size of the rectangle in the left
	 * @param extraBottom
	 *            increase the size of the rectangle in the bottom
	 * @param extraRight
	 *            increase the size of the rectangle in the right
	 * @param extraTop
	 *            increase the size of the rectangle in the top
	 * @return this <CODE>Chunk</CODE>
	 */
	public Chunk setBackground(Color color, float extraLeft, float extraBottom,
			float extraRight, float extraTop) {
		return setAttribute(BACKGROUND, new Object[] { color,
				new float[] { extraLeft, extraBottom, extraRight, extraTop } });
	}

	/** Key for text rendering mode. */
	public static final String TEXTRENDERMODE = "TEXTRENDERMODE";

	/**
	 * Sets the text rendering mode. It can outline text, simulate bold and make
	 * text invisible.
	 * 
	 * @param mode
	 *            the text rendering mode. It can be <CODE>
	 *            PdfContentByte.TEXT_RENDER_MODE_FILL</CODE>,<CODE>
	 *            PdfContentByte.TEXT_RENDER_MODE_STROKE</CODE>,<CODE>
	 *            PdfContentByte.TEXT_RENDER_MODE_FILL_STROKE</CODE> and <CODE>
	 *            PdfContentByte.TEXT_RENDER_MODE_INVISIBLE</CODE>.
	 * @param strokeWidth
	 *            the stroke line width for the modes <CODE>
	 *            PdfContentByte.TEXT_RENDER_MODE_STROKE</CODE> and <CODE>
	 *            PdfContentByte.TEXT_RENDER_MODE_FILL_STROKE</CODE>.
	 * @param strokeColor
	 *            the stroke color or <CODE>null</CODE> to follow the text
	 *            color
	 * @return this <CODE>Chunk</CODE>
	 */
	public Chunk setTextRenderMode(int mode, float strokeWidth,
			Color strokeColor) {
		return setAttribute(TEXTRENDERMODE, new Object[] { new Integer(mode),
				new Float(strokeWidth), strokeColor });
	}

	/** Key for split character. */
	public static final String SPLITCHARACTER = "SPLITCHARACTER";

	/**
	 * Sets the split characters.
	 * 
	 * @param splitCharacter
	 *            the <CODE>SplitCharacter</CODE> interface
	 * @return this <CODE>Chunk</CODE>
	 */

	public Chunk setSplitCharacter(SplitCharacter splitCharacter) {
		return setAttribute(SPLITCHARACTER, splitCharacter);
	}

	/** Key for hyphenation. */
	public static final String HYPHENATION = "HYPHENATION";
	
	/**
	 * sets the hyphenation engine to this <CODE>Chunk</CODE>.
	 * 
	 * @param hyphenation
	 *            the hyphenation engine
	 * @return this <CODE>Chunk</CODE>
	 */
	public Chunk setHyphenation(HyphenationEvent hyphenation) {
		return setAttribute(HYPHENATION, hyphenation);
	}

	/** Key for remote goto. */
	public static final String REMOTEGOTO = "REMOTEGOTO";

	/**
	 * Sets a goto for a remote destination for this <CODE>Chunk</CODE>.
	 * 
	 * @param filename
	 *            the file name of the destination document
	 * @param name
	 *            the name of the destination to go to
	 * @return this <CODE>Chunk</CODE>
	 */

	public Chunk setRemoteGoto(String filename, String name) {
		return setAttribute(REMOTEGOTO, new Object[] { filename, name });
	}

	/**
	 * Sets a goto for a remote destination for this <CODE>Chunk</CODE>.
	 * 
	 * @param filename
	 *            the file name of the destination document
	 * @param page
	 *            the page of the destination to go to. First page is 1
	 * @return this <CODE>Chunk</CODE>
	 */

	public Chunk setRemoteGoto(String filename, int page) {
		return setAttribute(REMOTEGOTO, new Object[] { filename,
				new Integer(page) });
	}

	/** Key for local goto. */
	public static final String LOCALGOTO = "LOCALGOTO";
	
	/**
	 * Sets a local goto for this <CODE>Chunk</CODE>.
	 * <P>
	 * There must be a local destination matching the name.
	 * 
	 * @param name
	 *            the name of the destination to go to
	 * @return this <CODE>Chunk</CODE>
	 */

	public Chunk setLocalGoto(String name) {
		return setAttribute(LOCALGOTO, name);
	}

	/** Key for local destination. */
	public static final String LOCALDESTINATION = "LOCALDESTINATION";

	/**
	 * Sets a local destination for this <CODE>Chunk</CODE>.
	 * 
	 * @param name
	 *            the name for this destination
	 * @return this <CODE>Chunk</CODE>
	 */
	public Chunk setLocalDestination(String name) {
		return setAttribute(LOCALDESTINATION, name);
	}

	/** Key for generic tag. */
	public static final String GENERICTAG = "GENERICTAG";

	/**
	 * Sets the generic tag <CODE>Chunk</CODE>.
	 * <P>
	 * The text for this tag can be retrieved with <CODE>PdfPageEvent</CODE>.
	 * 
	 * @param text
	 *            the text for the tag
	 * @return this <CODE>Chunk</CODE>
	 */

	public Chunk setGenericTag(String text) {
		return setAttribute(GENERICTAG, text);
	}
	
	/** Key for image. */
	public static final String IMAGE = "IMAGE";

	/**
	 * Returns the image.
	 * 
	 * @return the image
	 */

	public Image getImage() {
		if (attributes == null)
			return null;
		Object obj[] = (Object[]) attributes.get(Chunk.IMAGE);
		if (obj == null)
			return null;
		else {
			return (Image) obj[0];
		}
	}
	
	/** Key for Action. */
	public static final String ACTION = "ACTION";

	/**
	 * Sets an action for this <CODE>Chunk</CODE>.
	 * 
	 * @param action
	 *            the action
	 * @return this <CODE>Chunk</CODE>
	 */

	public Chunk setAction(PdfAction action) {
		return setAttribute(ACTION, action);
	}

	/**
	 * Sets an anchor for this <CODE>Chunk</CODE>.
	 * 
	 * @param url
	 *            the <CODE>URL</CODE> to link to
	 * @return this <CODE>Chunk</CODE>
	 */

	public Chunk setAnchor(URL url) {
		return setAttribute(ACTION, new PdfAction(url.toExternalForm()));
	}

	/**
	 * Sets an anchor for this <CODE>Chunk</CODE>.
	 * 
	 * @param url
	 *            the url to link to
	 * @return this <CODE>Chunk</CODE>
	 */

	public Chunk setAnchor(String url) {
		return setAttribute(ACTION, new PdfAction(url));
	}
	
	/** Key for newpage. */
	public static final String NEWPAGE = "NEWPAGE";

	/**
	 * Sets a new page tag..
	 * 
	 * @return this <CODE>Chunk</CODE>
	 */

	public Chunk setNewPage() {
		return setAttribute(NEWPAGE, null);
	}

	/** Key for annotation. */
	public static final String PDFANNOTATION = "PDFANNOTATION";

	/**
	 * Sets a generic annotation to this <CODE>Chunk</CODE>.
	 * 
	 * @param annotation
	 *            the annotation
	 * @return this <CODE>Chunk</CODE>
	 */
	public Chunk setAnnotation(PdfAnnotation annotation) {
		return setAttribute(PDFANNOTATION, annotation);
	}
	
	/**
	 * @see com.lowagie.text.Element#isContent()
	 * @since	iText 2.0.8
	 */
	public boolean isContent() {
		return true;
	}

	/**
	 * @see com.lowagie.text.Element#isNestable()
	 * @since	iText 2.0.8
	 */
	public boolean isNestable() {
		return true;
	}

	/**
     * Returns the hyphenation (if present).
     * @since	2.1.2
	 */
    public HyphenationEvent getHyphenation() {
        if (attributes == null) return null;
        return (HyphenationEvent) attributes.get(Chunk.HYPHENATION);
	}
	
	// keys used in PdfChunk
	
	/** Key for color. */
	public static final String COLOR = "COLOR";

	/** Key for encoding. */
	public static final String ENCODING = "ENCODING";

	/**
	 * Key for character spacing.
	 */
	public static final String CHAR_SPACING = "CHAR_SPACING";

	/**
	 * Sets the character spacing.
	 * 
	 * @param charSpace the character spacing value
	 * @return this <CODE>Chunk</CODE>
	 */
	public Chunk setCharacterSpacing(float charSpace) {
		return setAttribute(CHAR_SPACING, new Float(charSpace));
	}
	
	/**
	 * Gets the character spacing.
	 * 
	 * @return a value in float
	 */
	public float getCharacterSpacing() {
		if (attributes != null && attributes.containsKey(CHAR_SPACING)) {
			Float f = (Float) attributes.get(CHAR_SPACING);
			return f.floatValue();
		}
		return 0.0f;
	}
}
