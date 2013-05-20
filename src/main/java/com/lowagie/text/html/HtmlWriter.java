/*
 * $Id: HtmlWriter.java 4065 2009-09-16 23:09:11Z psoares33 $
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
 * of this file under either the MPL or the GNU LIBRARY GENERAL PUBLIC LICENSE
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the MPL as stated above or under the terms of the GNU
 * Library General Public License as published by the Free Software Foundation;
 * either version 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU LIBRARY GENERAL PUBLIC LICENSE for more
 * details.
 *
 * If you didn't download this code from the following link, you should check if
 * you aren't using an obsolete version:
 * http://www.lowagie.com/iText/
 */

package com.lowagie.text.html;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.EmptyStackException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.Stack;
import com.lowagie.text.error_messages.MessageLocalization;

import com.lowagie.text.Anchor;
import com.lowagie.text.Annotation;
import com.lowagie.text.BadElementException;
import com.lowagie.text.Cell;
import com.lowagie.text.Chunk;
import com.lowagie.text.DocWriter;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.ExceptionConverter;
import com.lowagie.text.Font;
import com.lowagie.text.Header;
import com.lowagie.text.HeaderFooter;
import com.lowagie.text.Image;
import com.lowagie.text.List;
import com.lowagie.text.ListItem;
import com.lowagie.text.MarkedObject;
import com.lowagie.text.MarkedSection;
import com.lowagie.text.Meta;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.Row;
import com.lowagie.text.Section;
import com.lowagie.text.SimpleTable;
import com.lowagie.text.Table;
import com.lowagie.text.pdf.BaseFont;

/**
 * A <CODE>DocWriter</CODE> class for HTML.
 * <P>
 * An <CODE>HtmlWriter</CODE> can be added as a <CODE>DocListener</CODE>
 * to a certain <CODE>Document</CODE> by getting an instance.
 * Every <CODE>Element</CODE> added to the original <CODE>Document</CODE>
 * will be written to the <CODE>OutputStream</CODE> of this <CODE>HtmlWriter</CODE>.
 * <P>
 * Example:
 * <BLOCKQUOTE><PRE>
 * // creation of the document with a certain size and certain margins
 * Document document = new Document(PageSize.A4, 50, 50, 50, 50);
 * try {
 *    // this will write HTML to the Standard OutputStream
 *    <STRONG>HtmlWriter.getInstance(document, System.out);</STRONG>
 *    // this will write HTML to a file called text.html
 *    <STRONG>HtmlWriter.getInstance(document, new FileOutputStream("text.html"));</STRONG>
 *    // this will write HTML to for instance the OutputStream of a HttpServletResponse-object
 *    <STRONG>HtmlWriter.getInstance(document, response.getOutputStream());</STRONG>
 * }
 * catch(DocumentException de) {
 *    System.err.println(de.getMessage());
 * }
 * // this will close the document and all the OutputStreams listening to it
 * <STRONG>document.close();</CODE>
 * </PRE></BLOCKQUOTE>
 */

public class HtmlWriter extends DocWriter {
    
    // static membervariables (tags)
    
/** This is a possible HTML-tag. */
    public static final byte[] BEGINCOMMENT = getISOBytes("<!-- ");
    
/** This is a possible HTML-tag. */
    public static final byte[] ENDCOMMENT = getISOBytes(" -->");
    
/** This is a possible HTML-tag. */
    public static final String NBSP = "&nbsp;";
    
    // membervariables
    
/** This is the current font of the HTML. */
    protected Stack currentfont = new Stack();
    
/** This is the standard font of the HTML. */
    protected Font standardfont = new Font();
    
/** This is a path for images. */
    protected String imagepath = null;
    
/** Stores the page number. */
    protected int pageN = 0;
    
/** This is the textual part of a header */
    protected HeaderFooter header = null;
    
/** This is the textual part of the footer */
    protected HeaderFooter footer = null;
    
/** Store the markup properties of a MarkedObject. */
    protected Properties markup = new Properties();
    
    // constructor
    
/**
 * Constructs a <CODE>HtmlWriter</CODE>.
 *
 * @param doc     The <CODE>Document</CODE> that has to be written as HTML
 * @param os      The <CODE>OutputStream</CODE> the writer has to write to.
 */
    
    protected HtmlWriter(Document doc, OutputStream os) {
        super(doc, os);
        
        document.addDocListener(this);
        this.pageN = document.getPageNumber();
        try {
            os.write(LT);
            os.write(getISOBytes(HtmlTags.HTML));
            os.write(GT);
            os.write(NEWLINE);
            os.write(TAB);
            os.write(LT);
            os.write(getISOBytes(HtmlTags.HEAD));
            os.write(GT);
        }
        catch(IOException ioe) {
            throw new ExceptionConverter(ioe);
        }
    }
    
    // get an instance of the HtmlWriter
    
/**
 * Gets an instance of the <CODE>HtmlWriter</CODE>.
 *
 * @param document  The <CODE>Document</CODE> that has to be written
 * @param os  The <CODE>OutputStream</CODE> the writer has to write to.
 * @return  a new <CODE>HtmlWriter</CODE>
 */
    
    public static HtmlWriter getInstance(Document document, OutputStream os) {
        return new HtmlWriter(document, os);
    }
    
    // implementation of the DocListener methods
    
/**
 * Signals that an new page has to be started.
 *
 * @return  <CODE>true</CODE> if this action succeeded, <CODE>false</CODE> if not.
 */
    
    public boolean newPage() {
        try {
            writeStart(HtmlTags.DIV);
            write(" ");
            write(HtmlTags.STYLE);
            write("=\"");
            writeCssProperty(Markup.CSS_KEY_PAGE_BREAK_BEFORE, Markup.CSS_VALUE_ALWAYS);
            write("\" /");
            os.write(GT);
        }
        catch(IOException ioe) {
            throw new ExceptionConverter(ioe);
        }
        return true;
    }
    
/**
 * Signals that an <CODE>Element</CODE> was added to the <CODE>Document</CODE>.
 * 
 * @param element a high level object that has to be translated to HTML
 * @return  <CODE>true</CODE> if the element was added, <CODE>false</CODE> if not.
 * @throws  DocumentException when a document isn't open yet, or has been closed
 */
    
    public boolean add(Element element) throws DocumentException {
        if (pause) {
            return false;
        }
        if (open && !element.isContent()) {
				throw new DocumentException(MessageLocalization.getComposedMessage("the.document.is.open.you.can.only.add.elements.with.content"));
        }
        try {
            switch(element.type()) {
                case Element.HEADER:
                    try {
                        Header h = (Header) element;
                        if (HtmlTags.STYLESHEET.equals(h.getName())) {
                            writeLink(h);
                        }
                        else if (HtmlTags.JAVASCRIPT.equals(h.getName())) {
                            writeJavaScript(h);
                        }
                        else {
                            writeHeader(h);
                        }
                    }
                    catch(ClassCastException cce) {
                    }
                    return true;
                case Element.SUBJECT:
                case Element.KEYWORDS:
                case Element.AUTHOR:
                    Meta meta = (Meta) element;
                    writeHeader(meta);
                    return true;
                case Element.TITLE:
                    addTabs(2);
                    writeStart(HtmlTags.TITLE);
                    os.write(GT);
                    addTabs(3);
                    write(HtmlEncoder.encode(((Meta)element).getContent()));
                    addTabs(2);
                    writeEnd(HtmlTags.TITLE);
                    return true;
                case Element.CREATOR:
                    writeComment("Creator: " + HtmlEncoder.encode(((Meta)element).getContent()));
                    return true;
                case Element.PRODUCER:
                    writeComment("Producer: " + HtmlEncoder.encode(((Meta)element).getContent()));
                    return true;
                case Element.CREATIONDATE:
                    writeComment("Creationdate: " + HtmlEncoder.encode(((Meta)element).getContent()));
                    return true;
                case Element.MARKED:
                	if (element instanceof MarkedSection) {
                		MarkedSection ms = (MarkedSection)element;
                		addTabs(1);
                        writeStart(HtmlTags.DIV);
                        writeMarkupAttributes(ms.getMarkupAttributes());
                        os.write(GT);
                		MarkedObject mo = ((MarkedSection)element).getTitle();
                		if (mo != null) {
                			markup = mo.getMarkupAttributes();
                			mo.process(this);
                		}
                		ms.process(this);
                        writeEnd(HtmlTags.DIV);
                        return true;
                	}
                	else {
                		MarkedObject mo = (MarkedObject) element;
                		markup = mo.getMarkupAttributes();
                    	return mo.process(this);
                	}
                default:
                    write(element, 2);
                    return true;
            }
        }
        catch(IOException ioe) {
            throw new ExceptionConverter(ioe);
        }
    }
    
/**
 * Signals that the <CODE>Document</CODE> has been opened and that
 * <CODE>Elements</CODE> can be added.
 * <P>
 * The <CODE>HEAD</CODE>-section of the HTML-document is written.
 */
    
    public void open() {
        super.open();
        try {
            writeComment(Document.getVersion());
            writeComment("CreationDate: " + new Date().toString());
            addTabs(1);
            writeEnd(HtmlTags.HEAD);
            addTabs(1);
            writeStart(HtmlTags.BODY);
            if (document.leftMargin() > 0) {
                write(HtmlTags.LEFTMARGIN, String.valueOf(document.leftMargin()));
            }
            if (document.rightMargin() > 0) {
                write(HtmlTags.RIGHTMARGIN, String.valueOf(document.rightMargin()));
            }
            if (document.topMargin() > 0) {
                write(HtmlTags.TOPMARGIN, String.valueOf(document.topMargin()));
            }
            if (document.bottomMargin() > 0) {
                write(HtmlTags.BOTTOMMARGIN, String.valueOf(document.bottomMargin()));
            }
            if (pageSize.getBackgroundColor() != null) {
                write(HtmlTags.BACKGROUNDCOLOR, HtmlEncoder.encode(pageSize.getBackgroundColor()));
            }
            if (document.getJavaScript_onLoad() != null) {
                write(HtmlTags.JAVASCRIPT_ONLOAD, HtmlEncoder.encode(document.getJavaScript_onLoad()));
            }
            if (document.getJavaScript_onUnLoad() != null) {
                write(HtmlTags.JAVASCRIPT_ONUNLOAD, HtmlEncoder.encode(document.getJavaScript_onUnLoad()));
            }
            if (document.getHtmlStyleClass() != null) {
                write(Markup.HTML_ATTR_CSS_CLASS, document.getHtmlStyleClass());
            }
            os.write(GT);
            initHeader(); // line added by David Freels
        }
        catch(IOException ioe) {
            throw new ExceptionConverter(ioe);
        }
    }
    
/**
 * Signals that the <CODE>Document</CODE> was closed and that no other
 * <CODE>Elements</CODE> will be added.
 */
    
    public void close() {
        try {
            initFooter(); // line added by David Freels
            addTabs(1);
            writeEnd(HtmlTags.BODY);
            os.write(NEWLINE);
            writeEnd(HtmlTags.HTML);
            super.close();
        }
        catch(IOException ioe) {
            throw new ExceptionConverter(ioe);
        }
    }
    
    // some protected methods
    
/**
 * Adds the header to the top of the </CODE>Document</CODE>
 */
    
    protected void initHeader() {
        if (header != null) {
            try {
                add(header.paragraph());
            }
            catch(Exception e) {
                throw new ExceptionConverter(e);
            }
        }
    }
    
/**
 *  Adds the header to the top of the </CODE>Document</CODE>
 */
    
    protected void initFooter() {
        if (footer != null) {
            try {
                // Set the page number. HTML has no notion of a page, so it should always
                // add up to 1
                footer.setPageNumber(pageN + 1);
                add(footer.paragraph());
            }
            catch(Exception e) {
                throw new ExceptionConverter(e);
            }
        }
    }
    
/**
 * Writes a Metatag in the header.
 *
 * @param   meta   the element that has to be written
 * @throws  IOException
 */
    
    protected void writeHeader(Meta meta) throws IOException {
        addTabs(2);
        writeStart(HtmlTags.META);
        switch(meta.type()) {
            case Element.HEADER:
                write(HtmlTags.NAME, ((Header) meta).getName());
                break;
            case Element.SUBJECT:
                write(HtmlTags.NAME, HtmlTags.SUBJECT);
                break;
            case Element.KEYWORDS:
                write(HtmlTags.NAME, HtmlTags.KEYWORDS);
                break;
            case Element.AUTHOR:
                write(HtmlTags.NAME, HtmlTags.AUTHOR);
                break;
        }
        write(HtmlTags.CONTENT, HtmlEncoder.encode(meta.getContent()));
        writeEnd();
    }
    
/**
 * Writes a link in the header.
 *
 * @param   header   the element that has to be written
 * @throws  IOException
 */
    
    protected void writeLink(Header header) throws IOException {
        addTabs(2);
        writeStart(HtmlTags.LINK);
        write(HtmlTags.REL, header.getName());
        write(HtmlTags.TYPE, HtmlTags.TEXT_CSS);
        write(HtmlTags.REFERENCE, header.getContent());
        writeEnd();
    }
    
/**
 * Writes a JavaScript section or, if the markup attribute HtmlTags.URL is set, a JavaScript reference in the header.
 *
 * @param   header   the element that has to be written
 * @throws  IOException
 */
    
    protected void writeJavaScript(Header header) throws IOException {
        addTabs(2);
        writeStart(HtmlTags.SCRIPT);
        write(HtmlTags.LANGUAGE, HtmlTags.JAVASCRIPT);
        if (markup.size() > 0) {
          /* JavaScript reference example:
           *
           * <script language="JavaScript" src="/myPath/MyFunctions.js"/>
           */ 
          writeMarkupAttributes(markup);
          os.write(GT);
          writeEnd(HtmlTags.SCRIPT);
        }
        else {
          /* JavaScript coding convention:
           *
           * <script language="JavaScript" type="text/javascript">
           * <!--
           * // ... JavaScript methods ...
           * //-->
           * </script>
           */ 
          write(HtmlTags.TYPE, Markup.HTML_VALUE_JAVASCRIPT);
          os.write(GT);
          addTabs(2);
          write(new String(BEGINCOMMENT) + "\n");
          write(header.getContent());
          addTabs(2);
          write("//" + new String(ENDCOMMENT));
          addTabs(2);
          writeEnd(HtmlTags.SCRIPT);
        }
    }
    
/**
 * Writes some comment.
 * <P>
 * This method writes some comment.
 *
 * @param comment   the comment that has to be written
 * @throws  IOException
 */
    
    protected void writeComment(String comment) throws IOException {
        addTabs(2);
        os.write(BEGINCOMMENT);
        write(comment);
        os.write(ENDCOMMENT);
    }
    
    // public methods
    
/**
 * Changes the standardfont.
 *
 * @param standardfont  The font
 */
    
    public void setStandardFont(Font standardfont) {
        this.standardfont = standardfont;
    }
    
/**
 * Checks if a given font is the same as the font that was last used.
 *
 * @param   font    the font of an object
 * @return  true if the font differs
 */
    
    public boolean isOtherFont(Font font) {
        try {
            Font cFont = (Font) currentfont.peek();
            if (cFont.compareTo(font) == 0) return false;
            return true;
        }
        catch(EmptyStackException ese) {
            if (standardfont.compareTo(font) == 0) return false;
            return true;
        }
    }
    
/**
 * Sets the basepath for images.
 * <P>
 * This is especially useful if you add images using a file,
 * rather than an URL. In PDF there is no problem, since
 * the images are added inline, but in HTML it is sometimes
 * necessary to use a relative path or a special path to some
 * images directory.
 *
 * @param imagepath the new imagepath
 */
    
    public void setImagepath(String imagepath) {
        this.imagepath = imagepath;
    }
    
/**
 * Resets the imagepath.
 */
    
    public void resetImagepath() {
        imagepath = null;
    }
    
/**
 * Changes the header of this document.
 *
 * @param header    the new header
 */
    
    public void setHeader(HeaderFooter header) {
        this.header = header;
    }
    
/**
 * Changes the footer of this document.
 *
 * @param footer    the new footer
 */
    
    public void setFooter(HeaderFooter footer) {
        this.footer = footer;
    }
    
/**
 * Signals that a <CODE>String</CODE> was added to the <CODE>Document</CODE>.
 * 
 * @param string a String to add to the HTML
 * @return  <CODE>true</CODE> if the string was added, <CODE>false</CODE> if not.
 */
    
    public boolean add(String string) {
        if (pause) {
            return false;
        }
        try
        {
            write(string);
            return true;
        }
        catch(IOException ioe) {
            throw new ExceptionConverter(ioe);
        }
    }
    
/**
 * Writes the HTML representation of an element.
 *
 * @param   element     the element
 * @param   indent      the indentation
 * @throws IOException
 */
    
    protected void write(Element element, int indent) throws IOException {
        Properties styleAttributes = null;
        switch(element.type()) {
        	case Element.MARKED: {
        		try {
					add(element);
				} catch (DocumentException e) {
					e.printStackTrace();
				}
        		return;
        	}
            case Element.CHUNK:
            {
                Chunk chunk = (Chunk) element;
                // if the chunk contains an image, return the image representation
                Image image = chunk.getImage();
                if (image != null) {
                    write(image, indent);
                    return;
                }
                
                if (chunk.isEmpty()) return;
                HashMap attributes = chunk.getAttributes();
                if (attributes != null && attributes.get(Chunk.NEWPAGE) != null) {
                    return;
                }
                boolean tag = isOtherFont(chunk.getFont()) || markup.size() > 0;
                if (tag) {
                    // start span tag
                    addTabs(indent);
                    writeStart(HtmlTags.SPAN);
                    if (isOtherFont(chunk.getFont())) {
                        write(chunk.getFont(), null);
                    }
                    writeMarkupAttributes(markup);
                    os.write(GT);
                }
                if (attributes != null && attributes.get(Chunk.SUBSUPSCRIPT) != null) {
                    // start sup or sub tag
                    if (((Float)attributes.get(Chunk.SUBSUPSCRIPT)).floatValue() > 0) {
                        writeStart(HtmlTags.SUP);
                    }
                    else {
                        writeStart(HtmlTags.SUB);
                    }
                    os.write(GT);
                }
                // contents
                write(HtmlEncoder.encode(chunk.getContent()));
                if (attributes != null && attributes.get(Chunk.SUBSUPSCRIPT) != null) {
                    // end sup or sub tag
                    os.write(LT);
                    os.write(FORWARD);
                    if (((Float)attributes.get(Chunk.SUBSUPSCRIPT)).floatValue() > 0) {
                        write(HtmlTags.SUP);
                    }
                    else {
                        write(HtmlTags.SUB);
                    }
                    os.write(GT);
                }
                if (tag) {
                    // end tag
                    writeEnd(Markup.HTML_TAG_SPAN);
                }
                return;
            }
            case Element.PHRASE:
            {
                Phrase phrase = (Phrase) element;
                styleAttributes = new Properties();
                if (phrase.hasLeading()) styleAttributes.setProperty(Markup.CSS_KEY_LINEHEIGHT, phrase.getLeading() + "pt");
                
                // start tag
                addTabs(indent);
                writeStart(Markup.HTML_TAG_SPAN);
                writeMarkupAttributes(markup);
                write(phrase.getFont(), styleAttributes);
                os.write(GT);
                currentfont.push(phrase.getFont());
                // contents
                for (Iterator i = phrase.iterator(); i.hasNext(); ) {
                    write((Element) i.next(), indent + 1);
                }
                // end tag
                addTabs(indent);
                writeEnd(Markup.HTML_TAG_SPAN);
                currentfont.pop();
                return;
            }
            case Element.ANCHOR:
            {
                Anchor anchor = (Anchor) element;
                styleAttributes = new Properties();
                if (anchor.hasLeading()) styleAttributes.setProperty(Markup.CSS_KEY_LINEHEIGHT, anchor.getLeading() + "pt");
                
                // start tag
                addTabs(indent);
                writeStart(HtmlTags.ANCHOR);
                if (anchor.getName() != null) {
                    write(HtmlTags.NAME, anchor.getName());
                }
                if (anchor.getReference() != null) {
                    write(HtmlTags.REFERENCE, anchor.getReference());
                }
                writeMarkupAttributes(markup);
                write(anchor.getFont(), styleAttributes);
                os.write(GT);
                currentfont.push(anchor.getFont());
                // contents
                for (Iterator i = anchor.iterator(); i.hasNext(); ) {
                    write((Element) i.next(), indent + 1);
                }
                // end tag
                addTabs(indent);
                writeEnd(HtmlTags.ANCHOR);
                currentfont.pop();
                return;
            }
            case Element.PARAGRAPH:
            {
                Paragraph paragraph = (Paragraph) element;
                styleAttributes = new Properties();
                if (paragraph.hasLeading()) styleAttributes.setProperty(Markup.CSS_KEY_LINEHEIGHT, paragraph.getTotalLeading() + "pt");
                // start tag
                addTabs(indent);
                writeStart(HtmlTags.DIV);
                writeMarkupAttributes(markup);
                String alignment = HtmlEncoder.getAlignment(paragraph.getAlignment());
                if (!"".equals(alignment)) {
                    write(HtmlTags.ALIGN, alignment);
                }
                write(paragraph.getFont(), styleAttributes);
                os.write(GT);
                currentfont.push(paragraph.getFont());
                // contents
                for (Iterator i = paragraph.iterator(); i.hasNext(); ) {
                    write((Element)i.next(), indent + 1);
                }
                // end tag
                addTabs(indent);
                writeEnd(HtmlTags.DIV);
                currentfont.pop();
                return;
            }
            case Element.SECTION:
            case Element.CHAPTER:
            {
                // part of the start tag + contents
                writeSection((Section) element, indent);
                return;
            }
            case Element.LIST:
            {
                List list = (List) element;
                // start tag
                addTabs(indent);
                if (list.isNumbered()) {
                    writeStart(HtmlTags.ORDEREDLIST);
                }
                else {
                    writeStart(HtmlTags.UNORDEREDLIST);
                }
                writeMarkupAttributes(markup);
                os.write(GT);
                // contents
                for (Iterator i = list.getItems().iterator(); i.hasNext(); ) {
                    write((Element) i.next(), indent + 1);
                }
                // end tag
                addTabs(indent);
                if (list.isNumbered()) {
                    writeEnd(HtmlTags.ORDEREDLIST);
                }
                else {
                    writeEnd(HtmlTags.UNORDEREDLIST);
                }
                return;
            }
            case Element.LISTITEM:
            {
                ListItem listItem = (ListItem) element;
                styleAttributes = new Properties();
                if (listItem.hasLeading()) styleAttributes.setProperty(Markup.CSS_KEY_LINEHEIGHT, listItem.getTotalLeading() + "pt");
                
                // start tag
                addTabs(indent);
                writeStart(HtmlTags.LISTITEM);
                writeMarkupAttributes(markup);
                write(listItem.getFont(), styleAttributes);
                os.write(GT);
                currentfont.push(listItem.getFont());
                // contents
                for (Iterator i = listItem.iterator(); i.hasNext(); ) {
                    write((Element) i.next(), indent + 1);
                }
                // end tag
                addTabs(indent);
                writeEnd(HtmlTags.LISTITEM);
                currentfont.pop();
                return;
            }
            case Element.CELL:
            {
                Cell cell = (Cell) element;
                
                // start tag
                addTabs(indent);
                if (cell.isHeader()) {
                    writeStart(HtmlTags.HEADERCELL);
                }
                else {
                    writeStart(HtmlTags.CELL);
                }
                writeMarkupAttributes(markup);
                if (cell.getBorderWidth() != Rectangle.UNDEFINED) {
                    write(HtmlTags.BORDERWIDTH, String.valueOf(cell.getBorderWidth()));
                }
                if (cell.getBorderColor() != null) {
                    write(HtmlTags.BORDERCOLOR, HtmlEncoder.encode(cell.getBorderColor()));
                }
                if (cell.getBackgroundColor() != null) {
                    write(HtmlTags.BACKGROUNDCOLOR, HtmlEncoder.encode(cell.getBackgroundColor()));
                }
                String alignment = HtmlEncoder.getAlignment(cell.getHorizontalAlignment());
                if (!"".equals(alignment)) {
                    write(HtmlTags.HORIZONTALALIGN, alignment);
                }
                alignment = HtmlEncoder.getAlignment(cell.getVerticalAlignment());
                if (!"".equals(alignment)) {
                    write(HtmlTags.VERTICALALIGN, alignment);
                }
                if (cell.getWidthAsString() != null) {
                    write(HtmlTags.WIDTH, cell.getWidthAsString());
                }
                if (cell.getColspan() != 1) {
                    write(HtmlTags.COLSPAN, String.valueOf(cell.getColspan()));
                }
                if (cell.getRowspan() != 1) {
                    write(HtmlTags.ROWSPAN, String.valueOf(cell.getRowspan()));
                }
                if (cell.getMaxLines() == 1) {
                	write(HtmlTags.STYLE, "white-space: nowrap;");
                }
                os.write(GT);
                // contents
                if (cell.isEmpty()) {
                    write(NBSP);
                } else {
                    for (Iterator i = cell.getElements(); i.hasNext(); ) {
                        write((Element) i.next(), indent + 1);
                    }
                }
                // end tag
                addTabs(indent);
                if (cell.isHeader()) {
                    writeEnd(HtmlTags.HEADERCELL);
                }
                else {
                    writeEnd(HtmlTags.CELL);
                }
                return;
            }
            case Element.ROW:
            {
                Row row = (Row) element;
                
                // start tag
                addTabs(indent);
                writeStart(HtmlTags.ROW);
                writeMarkupAttributes(markup);
                os.write(GT);
                // contents
                Element cell;
                for (int i = 0; i < row.getColumns(); i++) {
                    if ((cell = (Element)row.getCell(i)) != null) {
                        write(cell, indent + 1);
                    }
                }
                // end tag
                addTabs(indent);
                writeEnd(HtmlTags.ROW);
                return;
            }
            case Element.TABLE:
            {
            	Table table;
            	try {
            		table = (Table) element;
            	}
            	catch(ClassCastException cce) {
            		try {
						table = ((SimpleTable)element).createTable();
					} catch (BadElementException e) {
						throw new ExceptionConverter(e);
					}
            	}
                table.complete();
                // start tag
                addTabs(indent);
                writeStart(HtmlTags.TABLE);
                writeMarkupAttributes(markup);
                os.write(SPACE);
                write(HtmlTags.WIDTH);
                os.write(EQUALS);
                os.write(QUOTE);
                write(String.valueOf(table.getWidth()));
                if (!table.isLocked()){
                    write("%");
                }
                os.write(QUOTE);
                String alignment = HtmlEncoder.getAlignment(table.getAlignment());
                if (!"".equals(alignment)) {
                    write(HtmlTags.ALIGN, alignment);
                }
                write(HtmlTags.CELLPADDING, String.valueOf(table.getPadding()));
                write(HtmlTags.CELLSPACING, String.valueOf(table.getSpacing()));
                if (table.getBorderWidth() != Rectangle.UNDEFINED) {
                    write(HtmlTags.BORDERWIDTH, String.valueOf(table.getBorderWidth()));
                }
                if (table.getBorderColor() != null) {
                    write(HtmlTags.BORDERCOLOR, HtmlEncoder.encode(table.getBorderColor()));
                }
                if (table.getBackgroundColor() != null) {
                    write(HtmlTags.BACKGROUNDCOLOR, HtmlEncoder.encode(table.getBackgroundColor()));
                }
                os.write(GT);
                // contents
                Row row;
                for (Iterator iterator = table.iterator(); iterator.hasNext(); ) {
                    row = (Row) iterator.next();
                    write(row, indent + 1);
                }
                // end tag
                addTabs(indent);
                writeEnd(HtmlTags.TABLE);
                return;
            }
            case Element.ANNOTATION:
            {
                Annotation annotation = (Annotation) element;
                writeComment(annotation.title() + ": " + annotation.content());
                return;
            }
            case Element.IMGRAW:
            case Element.JPEG:
            case Element.JPEG2000:
            case Element.IMGTEMPLATE:
            {
                Image image = (Image) element;
                if (image.getUrl() == null) {
                    return;
                }
                
                // start tag
                addTabs(indent);
                writeStart(HtmlTags.IMAGE);
                String path = image.getUrl().toString();
                if (imagepath != null) {
                    if (path.indexOf('/') > 0) {
                        path = imagepath + path.substring(path.lastIndexOf('/') + 1);
                    }
                    else {
                        path = imagepath + path;
                    }
                }
                write(HtmlTags.URL, path);
                if ((image.getAlignment() & Image.RIGHT) > 0) {
                    write(HtmlTags.ALIGN, HtmlTags.ALIGN_RIGHT);
                }
                else if ((image.getAlignment() & Image.MIDDLE) > 0) {
                    write(HtmlTags.ALIGN, HtmlTags.ALIGN_MIDDLE);
                }
                else {
                    write(HtmlTags.ALIGN, HtmlTags.ALIGN_LEFT);
                }
                if (image.getAlt() != null) {
                    write(HtmlTags.ALT, image.getAlt());
                }
                write(HtmlTags.PLAINWIDTH, String.valueOf(image.getScaledWidth()));
                write(HtmlTags.PLAINHEIGHT, String.valueOf(image.getScaledHeight()));
                writeMarkupAttributes(markup);
                writeEnd();
                return;
            }
            
            default:
                return;
        }
    }
    
/**
 * Writes the HTML representation of a section.
 *
 * @param   section     the section to write
 * @param   indent      the indentation
 * @throws IOException
 */
    
    protected void writeSection(Section section, int indent) throws IOException {
        if (section.getTitle() != null) {
            int depth = section.getDepth() - 1;
            if (depth > 5) {
                depth = 5;
            }
            Properties styleAttributes = new Properties();
            if (section.getTitle().hasLeading()) styleAttributes.setProperty(Markup.CSS_KEY_LINEHEIGHT, section.getTitle().getTotalLeading() + "pt");
            // start tag
            addTabs(indent);
            writeStart(HtmlTags.H[depth]);
            write(section.getTitle().getFont(), styleAttributes);
            String alignment = HtmlEncoder.getAlignment(section.getTitle().getAlignment());
            if (!"".equals(alignment)) {
                write(HtmlTags.ALIGN, alignment);
            }
            writeMarkupAttributes(markup);
            os.write(GT);
            currentfont.push(section.getTitle().getFont());
            // contents
            for (Iterator i = section.getTitle().iterator(); i.hasNext(); ) {
                write((Element)i.next(), indent + 1);
            }
            // end tag
            addTabs(indent);
            writeEnd(HtmlTags.H[depth]);
            currentfont.pop();
        }
        for (Iterator i = section.iterator(); i.hasNext(); ) {
            write((Element) i.next(), indent);
        }
    }
    
    /**
     * Writes the representation of a <CODE>Font</CODE>.
     *
     * @param font              a <CODE>Font</CODE>
     * @param styleAttributes   the style of the font
     * @throws IOException
     */
    
    protected void write(Font font, Properties styleAttributes) throws IOException {
        if (font == null || !isOtherFont(font) /* || styleAttributes == null*/) return;
        write(" ");
        write(HtmlTags.STYLE);
        write("=\"");
        if (styleAttributes != null) {
            String key;
            for (Enumeration e = styleAttributes.propertyNames(); e.hasMoreElements(); ) {
                key = (String)e.nextElement();
                writeCssProperty(key, styleAttributes.getProperty(key));
            }
        }
        if (isOtherFont(font)) {
            writeCssProperty(Markup.CSS_KEY_FONTFAMILY, font.getFamilyname());
            
            if (font.getSize() != Font.UNDEFINED) {
                writeCssProperty(Markup.CSS_KEY_FONTSIZE, font.getSize() + "pt");
            }
            if (font.getColor() != null) {
                writeCssProperty(Markup.CSS_KEY_COLOR, HtmlEncoder.encode(font.getColor()));
            }
            
            int fontstyle = font.getStyle();
            BaseFont bf = font.getBaseFont();
            if (bf != null) {
                String ps = bf.getPostscriptFontName().toLowerCase();
                if (ps.indexOf("bold") >= 0) {
                    if (fontstyle == Font.UNDEFINED)
                        fontstyle = 0;
                    fontstyle |= Font.BOLD;
                }
                if (ps.indexOf("italic") >= 0 || ps.indexOf("oblique") >= 0) {
                    if (fontstyle == Font.UNDEFINED)
                        fontstyle = 0;
                    fontstyle |= Font.ITALIC;
                }
            }
            if (fontstyle != Font.UNDEFINED && fontstyle != Font.NORMAL) {
                switch (fontstyle & Font.BOLDITALIC) {
                    case Font.BOLD:
                        writeCssProperty(Markup.CSS_KEY_FONTWEIGHT, Markup.CSS_VALUE_BOLD);
                        break;
                    case Font.ITALIC:
                        writeCssProperty(Markup.CSS_KEY_FONTSTYLE, Markup.CSS_VALUE_ITALIC);
                        break;
                    case Font.BOLDITALIC:
                        writeCssProperty(Markup.CSS_KEY_FONTWEIGHT, Markup.CSS_VALUE_BOLD);
                        writeCssProperty(Markup.CSS_KEY_FONTSTYLE, Markup.CSS_VALUE_ITALIC);
                        break;
                }
                
                // CSS only supports one decoration tag so if both are specified
                // only one of the two will display
                if ((fontstyle & Font.UNDERLINE) > 0) {
                    writeCssProperty(Markup.CSS_KEY_TEXTDECORATION, Markup.CSS_VALUE_UNDERLINE);
                }
                if ((fontstyle & Font.STRIKETHRU) > 0) {
                    writeCssProperty(Markup.CSS_KEY_TEXTDECORATION, Markup.CSS_VALUE_LINETHROUGH);
                }
            }
        }
        write("\"");
    }
    
    /**
     * Writes out a CSS property.
     * @param prop a CSS property
     * @param value the value of the CSS property
     * @throws IOException
     */
    protected void writeCssProperty(String prop, String value) throws IOException {
        write(new StringBuffer(prop).append(": ").append(value).append("; ").toString());
    }
}
