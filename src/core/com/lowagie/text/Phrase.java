/*
 * $Id: Phrase.java 4065 2009-09-16 23:09:11Z psoares33 $
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import com.lowagie.text.error_messages.MessageLocalization;

import com.lowagie.text.pdf.HyphenationEvent;

/**
 * A <CODE>Phrase</CODE> is a series of <CODE>Chunk</CODE>s.
 * <P>
 * A <CODE>Phrase</CODE> has a main <CODE>Font</CODE>, but some chunks
 * within the phrase can have a <CODE>Font</CODE> that differs from the
 * main <CODE>Font</CODE>. All the <CODE>Chunk</CODE>s in a <CODE>Phrase</CODE>
 * have the same <CODE>leading</CODE>.
 * <P>
 * Example:
 * <BLOCKQUOTE><PRE>
 * // When no parameters are passed, the default leading = 16
 * <STRONG>Phrase phrase0 = new Phrase();</STRONG>
 * <STRONG>Phrase phrase1 = new Phrase("this is a phrase");</STRONG>
 * // In this example the leading is passed as a parameter
 * <STRONG>Phrase phrase2 = new Phrase(16, "this is a phrase with leading 16");</STRONG>
 * // When a Font is passed (explicitly or embedded in a chunk), the default leading = 1.5 * size of the font
 * <STRONG>Phrase phrase3 = new Phrase("this is a phrase with a red, normal font Courier, size 12", FontFactory.getFont(FontFactory.COURIER, 12, Font.NORMAL, new Color(255, 0, 0)));</STRONG>
 * <STRONG>Phrase phrase4 = new Phrase(new Chunk("this is a phrase"));</STRONG>
 * <STRONG>Phrase phrase5 = new Phrase(18, new Chunk("this is a phrase", FontFactory.getFont(FontFactory.HELVETICA, 16, Font.BOLD, new Color(255, 0, 0)));</STRONG>
 * </PRE></BLOCKQUOTE>
 *
 * @see		Element
 * @see		Chunk
 * @see		Paragraph
 * @see		Anchor
 */

public class Phrase extends ArrayList implements TextElementArray {
    
    // constants
	private static final long serialVersionUID = 2643594602455068231L;

	// membervariables
	/** This is the leading of this phrase. */
    protected float leading = Float.NaN;
    
    /** This is the font of this phrase. */
    protected Font font;
    
    /** Null, unless the Phrase has to be hyphenated.
     * @since	2.1.2
     */
    protected HyphenationEvent hyphenation = null;
    
    // constructors
    
    /**
     * Constructs a <CODE>Phrase</CODE> without specifying a leading.
     */
    public Phrase() {
        this(16);
    }
    
    /**
     * Copy constructor for <CODE>Phrase</CODE>.
     */
    public Phrase(Phrase phrase) {
        super();
        this.addAll(phrase);
        leading = phrase.getLeading();
        font = phrase.getFont();
        setHyphenation(phrase.getHyphenation());
    }

	/**
     * Constructs a <CODE>Phrase</CODE> with a certain leading.
     *
     * @param	leading		the leading
     */
    public Phrase(float leading) {
        this.leading = leading;
        font = new Font();
    }
    
    /**
     * Constructs a <CODE>Phrase</CODE> with a certain <CODE>Chunk</CODE>.
     *
     * @param	chunk		a <CODE>Chunk</CODE>
     */
    public Phrase(Chunk chunk) {
        super.add(chunk);
        font = chunk.getFont();
        setHyphenation(chunk.getHyphenation());
    }

	/**
     * Constructs a <CODE>Phrase</CODE> with a certain <CODE>Chunk</CODE>
     * and a certain leading.
     *
     * @param	leading	the leading
     * @param	chunk		a <CODE>Chunk</CODE>
     */
    public Phrase(float leading, Chunk chunk) {
        this.leading = leading;
        super.add(chunk);
        font = chunk.getFont();
        setHyphenation(chunk.getHyphenation());
    }
    
    /**
     * Constructs a <CODE>Phrase</CODE> with a certain <CODE>String</CODE>.
     *
     * @param	string		a <CODE>String</CODE>
     */
    public Phrase(String string) {
        this(Float.NaN, string, new Font());
    }
    
    /**
     * Constructs a <CODE>Phrase</CODE> with a certain <CODE>String</CODE> and a certain <CODE>Font</CODE>.
     *
     * @param	string		a <CODE>String</CODE>
     * @param	font		a <CODE>Font</CODE>
     */
    public Phrase(String string, Font font) {
        this(Float.NaN, string, font);
    }
    
    /**
     * Constructs a <CODE>Phrase</CODE> with a certain leading and a certain <CODE>String</CODE>.
     *
     * @param	leading	the leading
     * @param	string		a <CODE>String</CODE>
     */
    public Phrase(float leading, String string) {
        this(leading, string, new Font());
    }
    
    /**
     * Constructs a <CODE>Phrase</CODE> with a certain leading, a certain <CODE>String</CODE>
     * and a certain <CODE>Font</CODE>.
     *
     * @param	leading	the leading
     * @param	string		a <CODE>String</CODE>
     * @param	font		a <CODE>Font</CODE>
     */
    public Phrase(float leading, String string, Font font) {
        this.leading = leading;
        this.font = font;
    	/* bugfix by August Detlefsen */
        if (string != null && string.length() != 0) {
            super.add(new Chunk(string, font));
        }
    }
    
    // implementation of the Element-methods
    
    /**
     * Processes the element by adding it (or the different parts) to an
     * <CODE>ElementListener</CODE>.
     *
     * @param	listener	an <CODE>ElementListener</CODE>
     * @return	<CODE>true</CODE> if the element was processed successfully
     */
    public boolean process(ElementListener listener) {
        try {
            for (Iterator i = iterator(); i.hasNext(); ) {
                listener.add((Element) i.next());
            }
            return true;
        }
        catch(DocumentException de) {
            return false;
        }
    }
    
    /**
     * Gets the type of the text element.
     *
     * @return	a type
     */    
    public int type() {
        return Element.PHRASE;
    }
    
    /**
     * Gets all the chunks in this element.
     *
     * @return	an <CODE>ArrayList</CODE>
     */ 
    public ArrayList getChunks() {
        ArrayList tmp = new ArrayList();
        for (Iterator i = iterator(); i.hasNext(); ) {
            tmp.addAll(((Element) i.next()).getChunks());
        }
        return tmp;
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
    
    // overriding some of the ArrayList-methods
    
    /**
     * Adds a <CODE>Chunk</CODE>, an <CODE>Anchor</CODE> or another <CODE>Phrase</CODE>
     * to this <CODE>Phrase</CODE>.
     *
     * @param	index	index at which the specified element is to be inserted
     * @param	o   	an object of type <CODE>Chunk</CODE>, <CODE>Anchor</CODE> or <CODE>Phrase</CODE>
     * @throws	ClassCastException	when you try to add something that isn't a <CODE>Chunk</CODE>, <CODE>Anchor</CODE> or <CODE>Phrase</CODE>
     */
    public void add(int index, Object o) {
    	if (o == null) return;
        try {
            Element element = (Element) o;
            if (element.type() == Element.CHUNK) {
                Chunk chunk = (Chunk) element;
                if (!font.isStandardFont()) {
                    chunk.setFont(font.difference(chunk.getFont()));
                }
                if (hyphenation != null && chunk.getHyphenation() == null && !chunk.isEmpty()) {
                	chunk.setHyphenation(hyphenation);
                }
                super.add(index, chunk);
            }
            else if (element.type() == Element.PHRASE ||
            element.type() == Element.ANCHOR ||
            element.type() == Element.ANNOTATION ||
            element.type() == Element.TABLE || // line added by David Freels
            element.type() == Element.YMARK || 
            element.type() == Element.MARKED) {
                super.add(index, element);
            }
            else {
                throw new ClassCastException(String.valueOf(element.type()));
            }
        }
        catch(ClassCastException cce) {
            throw new ClassCastException(MessageLocalization.getComposedMessage("insertion.of.illegal.element.1", cce.getMessage()));
        }
    }
    
    /**
     * Adds a <CODE>Chunk</CODE>, <CODE>Anchor</CODE> or another <CODE>Phrase</CODE>
     * to this <CODE>Phrase</CODE>.
     *
     * @param	o	an object of type <CODE>Chunk</CODE>, <CODE>Anchor</CODE> or <CODE>Phrase</CODE>
     * @return	a boolean
     * @throws	ClassCastException	when you try to add something that isn't a <CODE>Chunk</CODE>, <CODE>Anchor</CODE> or <CODE>Phrase</CODE>
     */
    public boolean add(Object o) {
    	if (o == null) return false;
        if (o instanceof String) {
            return super.add(new Chunk((String) o, font));
        }
        if (o instanceof RtfElementInterface) {
        	return super.add(o);
        }
        try {
            Element element = (Element) o;
            switch(element.type()) {
                case Element.CHUNK:
                    return addChunk((Chunk) o);
                case Element.PHRASE:
                case Element.PARAGRAPH:
                    Phrase phrase = (Phrase) o;
                    boolean success = true;
                    Element e;
                    for (Iterator i = phrase.iterator(); i.hasNext(); ) {
                        e = (Element) i.next();
                        if (e instanceof Chunk) {
                            success &= addChunk((Chunk)e);
                        }
                        else {
                            success &= this.add(e);
                        }
                    }
                    return success;
                case Element.MARKED:
                case Element.ANCHOR:
                case Element.ANNOTATION:
                case Element.TABLE: // case added by David Freels
                case Element.PTABLE: // case added by mr. Karen Vardanyan
                	// This will only work for PDF!!! Not for RTF/HTML
                case Element.LIST:
                case Element.YMARK:
                	return super.add(o);
                    default:
                        throw new ClassCastException(String.valueOf(element.type()));
            }
        }
        catch(ClassCastException cce) {
            throw new ClassCastException(MessageLocalization.getComposedMessage("insertion.of.illegal.element.1", cce.getMessage()));
        }
    }
    
    /**
     * Adds a collection of <CODE>Chunk</CODE>s
     * to this <CODE>Phrase</CODE>.
     *
     * @param	collection	a collection of <CODE>Chunk</CODE>s, <CODE>Anchor</CODE>s and <CODE>Phrase</CODE>s.
     * @return	<CODE>true</CODE> if the action succeeded, <CODE>false</CODE> if not.
     * @throws	ClassCastException	when you try to add something that isn't a <CODE>Chunk</CODE>, <CODE>Anchor</CODE> or <CODE>Phrase</CODE>
     */
    public boolean addAll(Collection collection) {
        for (Iterator iterator = collection.iterator(); iterator.hasNext(); ) {
            this.add(iterator.next());
        }
        return true;
    }
    
    /**
     * Adds a Chunk.
     * <p>
     * This method is a hack to solve a problem I had with phrases that were split between chunks
     * in the wrong place.
     * @param chunk a Chunk to add to the Phrase
     * @return true if adding the Chunk succeeded
     */
    protected boolean addChunk(Chunk chunk) {
    	Font f = chunk.getFont();
    	String c = chunk.getContent();
        if (font != null && !font.isStandardFont()) {
            f = font.difference(chunk.getFont());
        }
        if (size() > 0 && !chunk.hasAttributes()) {
            try {
                Chunk previous = (Chunk) get(size() - 1);
                if (!previous.hasAttributes()
                		&& (f == null
                		|| f.compareTo(previous.getFont()) == 0)
                		&& !"".equals(previous.getContent().trim())
                		&& !"".equals(c.trim())) {
                    previous.append(c);
                    return true;
                }
            }
            catch(ClassCastException cce) {
            }
        }
        Chunk newChunk = new Chunk(c, f);
        newChunk.setAttributes(chunk.getAttributes());
        if (hyphenation != null && newChunk.getHyphenation() == null && !newChunk.isEmpty()) {
        	newChunk.setHyphenation(hyphenation);
        }
        return super.add(newChunk);
    }
    
    /**
     * Adds a <CODE>Object</CODE> to the <CODE>Paragraph</CODE>.
     *
     * @param	object		the object to add.
     */
    protected void addSpecial(Object object) {
        super.add(object);
    }
    
    // other methods that change the member variables
    
    /**
     * Sets the leading of this phrase.
     *
     * @param	leading		the new leading
     */
    
    public void setLeading(float leading) {
        this.leading = leading;
    }
    
    /**
     * Sets the main font of this phrase.
     * @param font	the new font
     */
    public void setFont(Font font) {
    	this.font = font;
    }
    
    // methods to retrieve information

	/**
     * Gets the leading of this phrase.
     *
     * @return	the linespacing
     */
    public float getLeading() {
        if (Float.isNaN(leading) && font != null) {
            return font.getCalculatedLeading(1.5f);
        }
        return leading;
    }

    /**
     * Checks you if the leading of this phrase is defined.
     *
     * @return	true if the leading is defined
     */ 
    public boolean hasLeading() {
        if (Float.isNaN(leading)) {
            return false;
        }
        return true;
    }

	/**
     * Gets the font of the first <CODE>Chunk</CODE> that appears in this <CODE>Phrase</CODE>.
     *
     * @return	a <CODE>Font</CODE>
     */  
    public Font getFont() {
        return font;
    }

	/**
     * Returns the content as a String object.
     * This method differs from toString because toString will return an ArrayList with the toString value of the Chunks in this Phrase.
     */
    public String getContent() {
    	StringBuffer buf = new StringBuffer();
    	for (Iterator i = getChunks().iterator(); i.hasNext(); ) {
    		buf.append(i.next().toString());
    	}
    	return buf.toString();
    }
    
    /**
     * Checks is this <CODE>Phrase</CODE> contains no or 1 empty <CODE>Chunk</CODE>.
     *
     * @return	<CODE>false</CODE> if the <CODE>Phrase</CODE>
     * contains more than one or more non-empty<CODE>Chunk</CODE>s.
     */
    public boolean isEmpty() {
        switch(size()) {
            case 0:
                return true;
            case 1:
                Element element = (Element) get(0);
                if (element.type() == Element.CHUNK && ((Chunk) element).isEmpty()) {
                    return true;
                }
                return false;
                default:
                    return false;
        }
    }
    
    /**
     * Getter for the hyphenation settings.
     * @return	a HyphenationEvent
     * @since	2.1.2
     */
    public HyphenationEvent getHyphenation() {
		return hyphenation;
	}

    /**
     * Setter for the hyphenation.
     * @param	hyphenation	a HyphenationEvent instance
     * @since	2.1.2
     */
	public void setHyphenation(HyphenationEvent hyphenation) {
		this.hyphenation = hyphenation;
	}
	
    // kept for historical reasons; people should use FontSelector
    // eligible for deprecation, but the methods are mentioned in the book p277.
    
    /**
     * Constructs a Phrase that can be used in the static getInstance() method.
     * @param	dummy	a dummy parameter
     */
    private Phrase(boolean dummy) {
    }
    
    /**
     * Gets a special kind of Phrase that changes some characters into corresponding symbols.
     * @param string
     * @return a newly constructed Phrase
     */
    public static final Phrase getInstance(String string) {
    	return getInstance(16, string, new Font());
    }
    
    /**
     * Gets a special kind of Phrase that changes some characters into corresponding symbols.
     * @param leading
     * @param string
     * @return a newly constructed Phrase
     */
    public static final Phrase getInstance(int leading, String string) {
    	return getInstance(leading, string, new Font());
    }
    
    /**
     * Gets a special kind of Phrase that changes some characters into corresponding symbols.
     * @param leading
     * @param string
     * @param font
     * @return a newly constructed Phrase
     */
    public static final Phrase getInstance(int leading, String string, Font font) {
    	Phrase p = new Phrase(true);
    	p.setLeading(leading);
    	p.font = font;
    	if (font.getFamily() != Font.SYMBOL && font.getFamily() != Font.ZAPFDINGBATS && font.getBaseFont() == null) {
            int index;
            while((index = SpecialSymbol.index(string)) > -1) {
                if (index > 0) {
                    String firstPart = string.substring(0, index);
                    ((ArrayList)p).add(new Chunk(firstPart, font));
                    string = string.substring(index);
                }
                Font symbol = new Font(Font.SYMBOL, font.getSize(), font.getStyle(), font.getColor());
                StringBuffer buf = new StringBuffer();
                buf.append(SpecialSymbol.getCorrespondingSymbol(string.charAt(0)));
                string = string.substring(1);
                while (SpecialSymbol.index(string) == 0) {
                    buf.append(SpecialSymbol.getCorrespondingSymbol(string.charAt(0)));
                    string = string.substring(1);
                }
                ((ArrayList)p).add(new Chunk(buf.toString(), symbol));
            }
        }
        if (string != null && string.length() != 0) {
        	((ArrayList)p).add(new Chunk(string, font));
        }
    	return p;
    }

}
