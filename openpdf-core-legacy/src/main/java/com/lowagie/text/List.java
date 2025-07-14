/*
 * $Id: List.java 3373 2008-05-12 16:21:24Z xlv $
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
 * https://github.com/LibrePDF/OpenPDF
 */

package com.lowagie.text;

import com.lowagie.text.factories.RomanAlphabetFactory;
import java.util.ArrayList;

/**
 * A <CODE>List</CODE> contains several <CODE>ListItem</CODE>s.
 * <p>
 * <B>Example 1:</B>
 * <BLOCKQUOTE><PRE>
 * <STRONG>List list = new List(true, 20);</STRONG>
 * <STRONG>list.add(new ListItem("First line"));</STRONG>
 * <STRONG>list.add(new ListItem("The second line is longer to see what happens once the end of the line is reached.
 * Will it start on a new line?"));</STRONG>
 * <STRONG>list.add(new ListItem("Third line"));</STRONG>
 * </PRE></BLOCKQUOTE>
 * <p>
 * The result of this code looks like this:
 * <OL>
 * <LI>
 * First line
 * </LI>
 * <LI>
 * The second line is longer to see what happens once the end of the line is reached. Will it start on a new line?
 * </LI>
 * <LI>
 * Third line
 * </LI>
 * </OL>
 *
 * <B>Example 2:</B>
 * <BLOCKQUOTE><PRE>
 * <STRONG>List overview = new List(false, 10);</STRONG>
 * <STRONG>overview.add(new ListItem("This is an item"));</STRONG>
 * <STRONG>overview.add("This is another item");</STRONG>
 * </PRE></BLOCKQUOTE>
 * <p>
 * The result of this code looks like this:
 * <UL>
 * <LI>
 * This is an item
 * </LI>
 * <LI>
 * This is another item
 * </LI>
 * </UL>
 *
 * @see Element
 * @see ListItem
 */

public class List implements TextElementArray {

    // constants

    /**
     * a possible value for the numbered parameter
     */
    public static final boolean ORDERED = true;
    /**
     * a possible value for the numbered parameter
     */
    public static final boolean UNORDERED = false;
    /**
     * a possible value for the lettered parameter
     */
    public static final boolean NUMERICAL = false;
    /**
     * a possible value for the lettered parameter
     */
    public static final boolean ALPHABETICAL = true;
    /**
     * a possible value for the lettered parameter
     */
    public static final boolean UPPERCASE = false;
    /**
     * a possible value for the lettered parameter
     */
    public static final boolean LOWERCASE = true;

    // member variables

    /**
     * This is the <CODE>ArrayList</CODE> containing the different <CODE>ListItem</CODE>s.
     */
    protected java.util.List<Element> list = new ArrayList<>();

    /**
     * Indicates if the list has to be numbered.
     */
    protected boolean numbered = false;
    /**
     * Indicates if the listsymbols are numerical or alphabetical.
     */
    protected boolean lettered = false;
    /**
     * Indicates if the listsymbols are lowercase or uppercase.
     */
    protected boolean lowercase = false;
    /**
     * Indicates if the indentation has to be set automatically.
     */
    protected boolean autoindent = false;
    /**
     * Indicates if the indentation of all the items has to be aligned.
     */
    protected boolean alignindent = false;

    /**
     * This variable indicates the first number of a numbered list.
     */
    protected int first = 1;
    /**
     * This is the listsymbol of a list that is not numbered.
     */
    protected Chunk symbol = new Chunk("- ");
    /**
     * In case you are using numbered/lettered lists, this String is added before the number/letter.
     *
     * @since iText 2.1.1
     */
    protected String preSymbol = "";
    /**
     * In case you are using numbered/lettered lists, this String is added after the number/letter.
     *
     * @since iText 2.1.1
     */
    protected String postSymbol = ". ";

    /**
     * The indentation of this list on the left side.
     */
    protected float indentationLeft = 0;
    /**
     * The indentation of this list on the right side.
     */
    protected float indentationRight = 0;
    /**
     * The indentation of the listitems.
     */
    protected float symbolIndent = 0;

    // constructors

    /**
     * Constructs a <CODE>List</CODE>.
     */
    public List() {
        this(false, false);
    }

    /**
     * Constructs a <CODE>List</CODE> with a specific symbol indentation.
     *
     * @param symbolIndent the symbol indentation
     * @since iText 2.0.8
     */
    public List(float symbolIndent) {
        this.symbolIndent = symbolIndent;
    }

    /**
     * Constructs a <CODE>List</CODE>.
     *
     * @param numbered a boolean
     */
    public List(boolean numbered) {
        this(numbered, false);
    }

    /**
     * Constructs a <CODE>List</CODE>.
     *
     * @param numbered a boolean
     * @param lettered has the list to be 'numbered' with letters
     */
    public List(boolean numbered, boolean lettered) {
        this.numbered = numbered;
        this.lettered = lettered;
        this.autoindent = true;
        this.alignindent = true;
    }

    /**
     * Constructs a <CODE>List</CODE>.
     * <p>
     * Remark: the parameter <VAR>symbolIndent</VAR> is important for instance when generating PDF-documents; it
     * indicates the indentation of the listsymbol. It is not important for HTML-documents.
     *
     * @param numbered     a boolean
     * @param symbolIndent the indentation that has to be used for the listsymbol
     */
    public List(boolean numbered, float symbolIndent) {
        this(numbered, false, symbolIndent);
    }

    /**
     * Creates a list
     *
     * @param numbered     has the list to be numbered?
     * @param lettered     has the list to be 'numbered' with letters
     * @param symbolIndent the indentation of the symbol
     */
    public List(boolean numbered, boolean lettered, float symbolIndent) {
        this.numbered = numbered;
        this.lettered = lettered;
        this.symbolIndent = symbolIndent;
    }

    // implementation of the Element-methods

    /**
     * Processes the element by adding it (or the different parts) to an
     * <CODE>ElementListener</CODE>.
     *
     * @param listener an <CODE>ElementListener</CODE>
     * @return <CODE>true</CODE> if the element was processed successfully
     */
    public boolean process(ElementListener listener) {
        try {
            for (Object o : list) {
                listener.add((Element) o);
            }
            return true;
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
        return Element.LIST;
    }

    /**
     * Gets all the chunks in this element.
     *
     * @return an <CODE>ArrayList</CODE>
     */
    public ArrayList<Element> getChunks() {
        ArrayList<Element> tmp = new ArrayList<>();
        for (Element o : list) {
            tmp.addAll(o.getChunks());
        }
        return tmp;
    }

    // methods to set the membervariables

    /**
     * Adds an <CODE>Element</CODE> to the <CODE>List</CODE>.
     *
     * @param o the element to add.
     * @return true if adding the element succeeded
     */
    public boolean add(Element o) {
        if (o instanceof ListItem) {
            ListItem item = (ListItem) o;
            if (numbered || lettered) {
                Chunk chunk = new Chunk(preSymbol, symbol.getFont());
                int index = first + list.size();
                if (lettered) {
                    chunk.append(RomanAlphabetFactory.getString(index, lowercase));
                } else {
                    chunk.append(String.valueOf(index));
                }
                chunk.append(postSymbol);
                item.setListSymbol(chunk);
            } else {
                item.setListSymbol(symbol);
            }
            item.setIndentationLeft(symbolIndent, autoindent);
            item.setIndentationRight(0);
            return list.add(item);
        }
        return false;
    }

    /**
     * Adds a nested <CODE>List</CODE> to the <CODE>List</CODE>.
     *
     * @param nested the nested list to add.
     * @return true if adding the nested list succeeded
     */
    public boolean add(List nested) {
        nested.setIndentationLeft(nested.getIndentationLeft() + symbolIndent);
        first--;
        return list.add(nested);
    }

    /**
     * Adds a <CODE>String</CODE> to the <CODE>List</CODE>.
     *
     * @param s the string to add.
     * @return true if adding the string succeeded
     */
    public boolean add(String s) {
        return this.add(new ListItem(s));
    }

    // extra methods

    /**
     * Makes sure all the items in the list have the same indentation.
     */
    public void normalizeIndentation() {
        float max = 0;
        Element o;
        for (Object o2 : list) {
            o = (Element) o2;
            if (o instanceof ListItem) {
                max = Math.max(max, ((ListItem) o).getIndentationLeft());
            }
        }
        for (Object o1 : list) {
            o = (Element) o1;
            if (o instanceof ListItem) {
                ((ListItem) o).setIndentationLeft(max);
            }
        }
    }

    // setters

    /**
     * Sets the listsymbol.
     *
     * @param symbol a <CODE>Chunk</CODE>
     */
    public void setListSymbol(Chunk symbol) {
        this.symbol = symbol;
    }

    /**
     * Sets the listsymbol.
     * <p>
     * This is a shortcut for <CODE>setListSymbol(Chunk symbol)</CODE>.
     *
     * @param symbol a <CODE>String</CODE>
     */
    public void setListSymbol(String symbol) {
        this.symbol = new Chunk(symbol);
    }

    /**
     * Gets all the items in the list.
     *
     * @return an <CODE>ArrayList</CODE> containing <CODE>ListItem</CODE>s.
     */
    public java.util.List<Element> getItems() {
        return list;
    }

    /**
     * Gets the size of the list.
     *
     * @return a <CODE>size</CODE>
     */
    public int size() {
        return list.size();
    }

    /**
     * Returns <CODE>true</CODE> if the list is empty.
     *
     * @return <CODE>true</CODE> if the list is empty
     */
    public boolean isEmpty() {
        return list.isEmpty();
    }

    /**
     * Gets the leading of the first listitem.
     *
     * @return a <CODE>leading</CODE>
     */
    public float getTotalLeading() {
        if (list.size() < 1) {
            return -1;
        }
        ListItem item = (ListItem) list.get(0);
        return item.getTotalLeading();
    }

    /**
     * Checks if the list is numbered.
     *
     * @return <CODE>true</CODE> if the list is numbered, <CODE>false</CODE> otherwise.
     */

    public boolean isNumbered() {
        return numbered;
    }

    /**
     * @param numbered the numbered to set
     */
    public void setNumbered(boolean numbered) {
        this.numbered = numbered;
    }

    /**
     * Checks if the list is lettered.
     *
     * @return <CODE>true</CODE> if the list is lettered, <CODE>false</CODE> otherwise.
     */
    public boolean isLettered() {
        return lettered;
    }

    /**
     * @param lettered the lettered to set
     */
    public void setLettered(boolean lettered) {
        this.lettered = lettered;
    }

    /**
     * Checks if the list lettering is lowercase.
     *
     * @return <CODE>true</CODE> if it is lowercase, <CODE>false</CODE> otherwise.
     */
    public boolean isLowercase() {
        return lowercase;
    }

    // methods to retrieve information

    /**
     * @param uppercase the uppercase to set
     */
    public void setLowercase(boolean uppercase) {
        this.lowercase = uppercase;
    }

    /**
     * Checks if the indentation of list items is done automatically.
     *
     * @return the autoindent
     */
    public boolean isAutoindent() {
        return autoindent;
    }

    /**
     * @param autoindent the autoindent to set
     */
    public void setAutoindent(boolean autoindent) {
        this.autoindent = autoindent;
    }

    /**
     * Checks if all the listitems should be aligned.
     *
     * @return the alignindent
     */
    public boolean isAlignindent() {
        return alignindent;
    }

    // getters

    /**
     * @param alignindent the alignindent to set
     */
    public void setAlignindent(boolean alignindent) {
        this.alignindent = alignindent;
    }

    /**
     * Gets the first number        .
     *
     * @return a number
     */
    public int getFirst() {
        return first;
    }

    /**
     * Sets the number that has to come first in the list.
     *
     * @param first a number
     */
    public void setFirst(int first) {
        this.first = first;
    }

    /**
     * Gets the Chunk containing the symbol.
     *
     * @return a Chunk with a symbol
     */
    public Chunk getSymbol() {
        return symbol;
    }

    /**
     * Gets the indentation of this paragraph on the left side.
     *
     * @return the indentation
     */
    public float getIndentationLeft() {
        return indentationLeft;
    }

    /**
     * Sets the indentation of this paragraph on the left side.
     *
     * @param indentation the new indentation
     */
    public void setIndentationLeft(float indentation) {
        this.indentationLeft = indentation;
    }

    /**
     * Gets the indentation of this paragraph on the right side.
     *
     * @return the indentation
     */
    public float getIndentationRight() {
        return indentationRight;
    }

    /**
     * Sets the indentation of this paragraph on the right side.
     *
     * @param indentation the new indentation
     */
    public void setIndentationRight(float indentation) {
        this.indentationRight = indentation;
    }

    /**
     * Gets the symbol indentation.
     *
     * @return the symbol indentation
     */
    public float getSymbolIndent() {
        return symbolIndent;
    }

    /**
     * @param symbolIndent the symbolIndent to set
     */
    public void setSymbolIndent(float symbolIndent) {
        this.symbolIndent = symbolIndent;
    }

    /**
     * @see com.lowagie.text.Element#isContent()
     * @since iText 2.0.8
     */
    public boolean isContent() {
        return true;
    }

    /**
     * @see com.lowagie.text.Element#isNestable()
     * @since iText 2.0.8
     */
    public boolean isNestable() {
        return true;
    }

    /**
     * Returns the String that is after a number or letter in the list symbol.
     *
     * @return the String that is after a number or letter in the list symbol
     * @since iText 2.1.1
     */
    public String getPostSymbol() {
        return postSymbol;
    }

    /**
     * Sets the String that has to be added after a number or letter in the list symbol.
     *
     * @param postSymbol the String that has to be added after a number or letter in the list symbol.
     * @since iText 2.1.1
     */
    public void setPostSymbol(String postSymbol) {
        this.postSymbol = postSymbol;
    }

    /**
     * Returns the String that is before a number or letter in the list symbol.
     *
     * @return the String that is before a number or letter in the list symbol
     * @since iText 2.1.1
     */
    public String getPreSymbol() {
        return preSymbol;
    }

    /**
     * Sets the String that has to be added before a number or letter in the list symbol.
     *
     * @param preSymbol the String that has to be added before a number or letter in the list symbol.
     * @since iText 2.1.1
     */
    public void setPreSymbol(String preSymbol) {
        this.preSymbol = preSymbol;
    }

}