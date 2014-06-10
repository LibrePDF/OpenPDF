/*
 * $Id: PdfDocument.java 4098 2009-11-16 13:27:45Z blowagie $
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

package com.lowagie.text.pdf;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import com.lowagie.text.error_messages.MessageLocalization;

import com.lowagie.text.Anchor;
import com.lowagie.text.Annotation;
import com.lowagie.text.BadElementException;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.ExceptionConverter;
import com.lowagie.text.Font;
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
import com.lowagie.text.Section;
import com.lowagie.text.SimpleTable;
import com.lowagie.text.Table;
import com.lowagie.text.pdf.collection.PdfCollection;
import com.lowagie.text.pdf.draw.DrawInterface;
import com.lowagie.text.pdf.internal.PdfAnnotationsImp;
import com.lowagie.text.pdf.internal.PdfViewerPreferencesImp;
import java.text.DecimalFormat;

/**
 * <CODE>PdfDocument</CODE> is the class that is used by <CODE>PdfWriter</CODE>
 * to translate a <CODE>Document</CODE> into a PDF with different pages.
 * <P>
 * A <CODE>PdfDocument</CODE> always listens to a <CODE>Document</CODE>
 * and adds the Pdf representation of every <CODE>Element</CODE> that is
 * added to the <CODE>Document</CODE>.
 *
 * @see		com.lowagie.text.Document
 * @see		com.lowagie.text.DocListener
 * @see		PdfWriter
 * @since	2.0.8 (class was package-private before)
 */

public class PdfDocument extends Document {

    /**
     * <CODE>PdfInfo</CODE> is the PDF InfoDictionary.
     * <P>
     * A document's trailer may contain a reference to an Info dictionary that provides information
     * about the document. This optional dictionary may contain one or more keys, whose values
     * should be strings.<BR>
     * This object is described in the 'Portable Document Format Reference Manual version 1.3'
     * section 6.10 (page 120-121)
     * @since	2.0.8 (PdfDocument was package-private before)
     */

    public static class PdfInfo extends PdfDictionary {

        /**
         * Construct a <CODE>PdfInfo</CODE>-object.
         */

        PdfInfo() {
            super();
            addProducer();
            addCreationDate();
        }

        /**
         * Constructs a <CODE>PdfInfo</CODE>-object.
         *
         * @param		author		name of the author of the document
         * @param		title		title of the document
         * @param		subject		subject of the document
         */

        PdfInfo(String author, String title, String subject) {
            this();
            addTitle(title);
            addSubject(subject);
            addAuthor(author);
        }

        /**
         * Adds the title of the document.
         *
         * @param	title		the title of the document
         */

        void addTitle(String title) {
            put(PdfName.TITLE, new PdfString(title, PdfObject.TEXT_UNICODE));
        }

        /**
         * Adds the subject to the document.
         *
         * @param	subject		the subject of the document
         */

        void addSubject(String subject) {
            put(PdfName.SUBJECT, new PdfString(subject, PdfObject.TEXT_UNICODE));
        }

        /**
         * Adds some keywords to the document.
         *
         * @param	keywords		the keywords of the document
         */

        void addKeywords(String keywords) {
            put(PdfName.KEYWORDS, new PdfString(keywords, PdfObject.TEXT_UNICODE));
        }

        /**
         * Adds the name of the author to the document.
         *
         * @param	author		the name of the author
         */

        void addAuthor(String author) {
            put(PdfName.AUTHOR, new PdfString(author, PdfObject.TEXT_UNICODE));
        }

        /**
         * Adds the name of the creator to the document.
         *
         * @param	creator		the name of the creator
         */

        void addCreator(String creator) {
            put(PdfName.CREATOR, new PdfString(creator, PdfObject.TEXT_UNICODE));
        }

        /**
         * Adds the name of the producer to the document.
         */

        void addProducer() {
            put(PdfName.PRODUCER, new PdfString(getVersion()));
        }

        /**
         * Adds the date of creation to the document.
         */

        void addCreationDate() {
            PdfString date = new PdfDate();
            put(PdfName.CREATIONDATE, date);
            put(PdfName.MODDATE, date);
        }

        void addkey(String key, String value) {
            if (key.equals("Producer") || key.equals("CreationDate"))
                return;
            put(new PdfName(key), new PdfString(value, PdfObject.TEXT_UNICODE));
        }
    }

    /**
     * <CODE>PdfCatalog</CODE> is the PDF Catalog-object.
     * <P>
     * The Catalog is a dictionary that is the root node of the document. It contains a reference
     * to the tree of pages contained in the document, a reference to the tree of objects representing
     * the document's outline, a reference to the document's article threads, and the list of named
     * destinations. In addition, the Catalog indicates whether the document's outline or thumbnail
     * page images should be displayed automatically when the document is viewed and whether some location
     * other than the first page should be shown when the document is opened.<BR>
     * In this class however, only the reference to the tree of pages is implemented.<BR>
     * This object is described in the 'Portable Document Format Reference Manual version 1.3'
     * section 6.2 (page 67-71)
     */

    static class PdfCatalog extends PdfDictionary {

    	/** The writer writing the PDF for which we are creating this catalog object. */
        PdfWriter writer;

        /**
         * Constructs a <CODE>PdfCatalog</CODE>.
         *
         * @param		pages		an indirect reference to the root of the document's Pages tree.
         * @param writer the writer the catalog applies to
         */

        PdfCatalog(PdfIndirectReference pages, PdfWriter writer) {
            super(CATALOG);
            this.writer = writer;
            put(PdfName.PAGES, pages);
        }

        /**
         * Adds the names of the named destinations to the catalog.
         * @param localDestinations the local destinations
         * @param documentLevelJS the javascript used in the document
         * @param documentFileAttachment	the attached files
         * @param writer the writer the catalog applies to
         */
        void addNames(TreeMap localDestinations, HashMap documentLevelJS, HashMap documentFileAttachment, PdfWriter writer) {
            if (localDestinations.isEmpty() && documentLevelJS.isEmpty() && documentFileAttachment.isEmpty())
                return;
            try {
                PdfDictionary names = new PdfDictionary();
                if (!localDestinations.isEmpty()) {
                    PdfArray ar = new PdfArray();
                    for (Iterator i = localDestinations.entrySet().iterator(); i.hasNext();) {
                        Map.Entry entry = (Map.Entry) i.next();
                        String name = (String) entry.getKey();
                        Object obj[] = (Object[]) entry.getValue();
                        if (obj[2] == null) //no destination
                            continue;
                        PdfIndirectReference ref = (PdfIndirectReference)obj[1];
                        ar.add(new PdfString(name, null));
                        ar.add(ref);
                    }
                    if (ar.size() > 0) {
                        PdfDictionary dests = new PdfDictionary();
                        dests.put(PdfName.NAMES, ar);
                        names.put(PdfName.DESTS, writer.addToBody(dests).getIndirectReference());
                    }
                }
                if (!documentLevelJS.isEmpty()) {
                    PdfDictionary tree = PdfNameTree.writeTree(documentLevelJS, writer);
                    names.put(PdfName.JAVASCRIPT, writer.addToBody(tree).getIndirectReference());
                }
                if (!documentFileAttachment.isEmpty()) {
                    names.put(PdfName.EMBEDDEDFILES, writer.addToBody(PdfNameTree.writeTree(documentFileAttachment, writer)).getIndirectReference());
                }
                if (names.size() > 0)
                    put(PdfName.NAMES, writer.addToBody(names).getIndirectReference());
            }
            catch (IOException e) {
                throw new ExceptionConverter(e);
            }
        }

        /**
         * Adds an open action to the catalog.
         * @param	action	the action that will be triggered upon opening the document
         */
        void setOpenAction(PdfAction action) {
            put(PdfName.OPENACTION, action);
        }


        /**
         * Sets the document level additional actions.
         * @param actions   dictionary of actions
         */
        void setAdditionalActions(PdfDictionary actions) {
            try {
                put(PdfName.AA, writer.addToBody(actions).getIndirectReference());
            } catch (Exception e) {
                throw new ExceptionConverter(e);
            }
        }
    }

// CONSTRUCTING A PdfDocument/PdfWriter INSTANCE

    /**
     * Constructs a new PDF document.
     */
    public PdfDocument() {
        super();
        addProducer();
        addCreationDate();
    }

    /** The <CODE>PdfWriter</CODE>. */
    protected PdfWriter writer;

    /**
     * Adds a <CODE>PdfWriter</CODE> to the <CODE>PdfDocument</CODE>.
     *
     * @param writer the <CODE>PdfWriter</CODE> that writes everything
     *                     what is added to this document to an outputstream.
     * @throws DocumentException on error
     */
    public void addWriter(PdfWriter writer) throws DocumentException {
        if (this.writer == null) {
            this.writer = writer;
            annotationsImp = new PdfAnnotationsImp(writer);
            return;
        }
        throw new DocumentException(MessageLocalization.getComposedMessage("you.can.only.add.a.writer.to.a.pdfdocument.once"));
    }

// LISTENER METHODS START

//	[L0] ElementListener interface

    /** This is the PdfContentByte object, containing the text. */
    protected PdfContentByte text;

    /** This is the PdfContentByte object, containing the borders and other Graphics. */
    protected PdfContentByte graphics;

    /** This represents the leading of the lines. */
    protected float leading = 0;

    /**
     * Getter for the current leading.
     * @return	the current leading
     * @since	2.1.2
     */
    public float getLeading() {
    	return leading;
    }
    
    /**
     * Setter for the current leading.
     * @param	leading the current leading
     * @since	2.1.6
     */
    void setLeading(float leading) {
    	this.leading = leading;
    }

    /** This represents the current alignment of the PDF Elements. */
    protected int alignment = Element.ALIGN_LEFT;

    /** This is the current height of the document. */
    protected float currentHeight = 0;

    /**
     * Signals that onParagraph is valid (to avoid that a Chapter/Section title is treated as a Paragraph).
     * @since 2.1.2
     */
    protected boolean isSectionTitle = false;

    /**
     * Signals that the current leading has to be subtracted from a YMark object when positive.
     * @since 2.1.2
     */
    protected int leadingCount = 0;

    /** The current active <CODE>PdfAction</CODE> when processing an <CODE>Anchor</CODE>. */
    protected PdfAction anchorAction = null;

    /**
     * Signals that an <CODE>Element</CODE> was added to the <CODE>Document</CODE>.
     *
     * @param element the element to add
     * @return <CODE>true</CODE> if the element was added, <CODE>false</CODE> if not.
     * @throws DocumentException when a document isn't open yet, or has been closed
     */
    public boolean add(Element element) throws DocumentException {
        if (writer != null && writer.isPaused()) {
            return false;
        }
        try {
            switch(element.type()) {
                // Information (headers)
                case Element.HEADER:
                    info.addkey(((Meta)element).getName(), ((Meta)element).getContent());
                    break;
                case Element.TITLE:
                    info.addTitle(((Meta)element).getContent());
                    break;
                case Element.SUBJECT:
                    info.addSubject(((Meta)element).getContent());
                    break;
                case Element.KEYWORDS:
                    info.addKeywords(((Meta)element).getContent());
                    break;
                case Element.AUTHOR:
                    info.addAuthor(((Meta)element).getContent());
                    break;
                case Element.CREATOR:
                    info.addCreator(((Meta)element).getContent());
                    break;
                case Element.PRODUCER:
                    // you can not change the name of the producer
                    info.addProducer();
                    break;
                case Element.CREATIONDATE:
                    // you can not set the creation date, only reset it
                    info.addCreationDate();
                    break;

                // content (text)
                case Element.CHUNK: {
                    // if there isn't a current line available, we make one
                    if (line == null) {
                        carriageReturn();
                    }

                    // we cast the element to a chunk
                    PdfChunk chunk = new PdfChunk((Chunk) element, anchorAction);
                    // we try to add the chunk to the line, until we succeed
                    {
                        PdfChunk overflow;
                        while ((overflow = line.add(chunk)) != null) {
                            carriageReturn();
                            chunk = overflow;
                            chunk.trimFirstSpace();
                        }
                    }
                    pageEmpty = false;
                    if (chunk.isAttribute(Chunk.NEWPAGE)) {
                        newPage();
                    }
                    break;
                }
                case Element.ANCHOR: {
                	leadingCount++;
                    Anchor anchor = (Anchor) element;
                    String url = anchor.getReference();
                    leading = anchor.getLeading();
                    if (url != null) {
                        anchorAction = new PdfAction(url);
                    }
                    // we process the element
                    element.process(this);
                    anchorAction = null;
                    leadingCount--;
                    break;
                }
                case Element.ANNOTATION: {
                    if (line == null) {
                        carriageReturn();
                    }
                    Annotation annot = (Annotation) element;
                    Rectangle rect = new Rectangle(0, 0);
                    if (line != null)
                    	rect = new Rectangle(annot.llx(indentRight() - line.widthLeft()), annot.ury(indentTop() - currentHeight - 20), annot.urx(indentRight() - line.widthLeft() + 20), annot.lly(indentTop() - currentHeight));
                    PdfAnnotation an = PdfAnnotationsImp.convertAnnotation(writer, annot, rect);
                    annotationsImp.addPlainAnnotation(an);
                    pageEmpty = false;
                    break;
                }
                case Element.PHRASE: {
                	leadingCount++;
                    // we cast the element to a phrase and set the leading of the document
                    leading = ((Phrase) element).getLeading();
                    // we process the element
                    element.process(this);
                    leadingCount--;
                    break;
                }
                case Element.PARAGRAPH: {
                	leadingCount++;
                    // we cast the element to a paragraph
                    Paragraph paragraph = (Paragraph) element;
                    addSpacing(paragraph.getSpacingBefore(), leading, paragraph.getFont());

                    // we adjust the parameters of the document
                    alignment = paragraph.getAlignment();
                    leading = paragraph.getTotalLeading();
                    carriageReturn();

                    // we don't want to make orphans/widows
                    if (currentHeight + line.height() + leading > indentTop() - indentBottom()) {
                        newPage();
                    }
                    indentation.indentLeft += paragraph.getIndentationLeft();
                    indentation.indentRight += paragraph.getIndentationRight();
                    carriageReturn();

                    PdfPageEvent pageEvent = writer.getPageEvent();
                    if (pageEvent != null && !isSectionTitle)
                        pageEvent.onParagraph(writer, this, indentTop() - currentHeight);

                    // if a paragraph has to be kept together, we wrap it in a table object
                    if (paragraph.getKeepTogether()) {
                    	carriageReturn();
                        PdfPTable table = new PdfPTable(1);
                        table.setWidthPercentage(100f);
                        PdfPCell cell = new PdfPCell();
                        cell.addElement(paragraph);
                        cell.setBorder(Table.NO_BORDER);
                        cell.setPadding(0);
                        table.addCell(cell);
                        indentation.indentLeft -= paragraph.getIndentationLeft();
                        indentation.indentRight -= paragraph.getIndentationRight();
                        this.add(table);
                        indentation.indentLeft += paragraph.getIndentationLeft();
                        indentation.indentRight += paragraph.getIndentationRight();
                    }
                    else {
                    	line.setExtraIndent(paragraph.getFirstLineIndent());
                    	element.process(this);
                        carriageReturn();
                        addSpacing(paragraph.getSpacingAfter(), paragraph.getTotalLeading(), paragraph.getFont());
                    }

                    if (pageEvent != null && !isSectionTitle)
                        pageEvent.onParagraphEnd(writer, this, indentTop() - currentHeight);

                    alignment = Element.ALIGN_LEFT;
                    indentation.indentLeft -= paragraph.getIndentationLeft();
                    indentation.indentRight -= paragraph.getIndentationRight();
                    carriageReturn();
                    leadingCount--;
                    break;
                }
                case Element.SECTION:
                case Element.CHAPTER: {
                    // Chapters and Sections only differ in their constructor
                    // so we cast both to a Section
                    Section section = (Section) element;
                    PdfPageEvent pageEvent = writer.getPageEvent();

                    boolean hasTitle = section.isNotAddedYet()
                    	&& section.getTitle() != null;

                    // if the section is a chapter, we begin a new page
                    if (section.isTriggerNewPage()) {
                        newPage();
                    }

                    if (hasTitle) {
                    	float fith = indentTop() - currentHeight;
                    	int rotation = pageSize.getRotation();
                    	if (rotation == 90 || rotation == 180)
                    		fith = pageSize.getHeight() - fith;
                    	PdfDestination destination = new PdfDestination(PdfDestination.FITH, fith);
                    	while (currentOutline.level() >= section.getDepth()) {
                    		currentOutline = currentOutline.parent();
                    	}
                    	PdfOutline outline = new PdfOutline(currentOutline, destination, section.getBookmarkTitle(), section.isBookmarkOpen());
                    	currentOutline = outline;
                    }

                    // some values are set
                    carriageReturn();
                    indentation.sectionIndentLeft += section.getIndentationLeft();
                    indentation.sectionIndentRight += section.getIndentationRight();

                    if (section.isNotAddedYet() && pageEvent != null)
                        if (element.type() == Element.CHAPTER)
                            pageEvent.onChapter(writer, this, indentTop() - currentHeight, section.getTitle());
                        else
                            pageEvent.onSection(writer, this, indentTop() - currentHeight, section.getDepth(), section.getTitle());

                    // the title of the section (if any has to be printed)
                    if (hasTitle) {
                        isSectionTitle = true;
                        add(section.getTitle());
                        isSectionTitle = false;
                    }
                    indentation.sectionIndentLeft += section.getIndentation();
                    // we process the section
                    element.process(this);
                    flushLines();
                    // some parameters are set back to normal again
                    indentation.sectionIndentLeft -= (section.getIndentationLeft() + section.getIndentation());
                    indentation.sectionIndentRight -= section.getIndentationRight();

                    if (section.isComplete() && pageEvent != null)
                        if (element.type() == Element.CHAPTER)
                            pageEvent.onChapterEnd(writer, this, indentTop() - currentHeight);
                        else
                            pageEvent.onSectionEnd(writer, this, indentTop() - currentHeight);

                    break;
                }
                case Element.LIST: {
                    // we cast the element to a List
                    List list = (List) element;
                    if (list.isAlignindent()) {
                    	list.normalizeIndentation();
                    }
                    // we adjust the document
                    indentation.listIndentLeft += list.getIndentationLeft();
                    indentation.indentRight += list.getIndentationRight();
                    // we process the items in the list
                    element.process(this);
                    // some parameters are set back to normal again
                    indentation.listIndentLeft -= list.getIndentationLeft();
                    indentation.indentRight -= list.getIndentationRight();
                    carriageReturn();
                    break;
                }
                case Element.LISTITEM: {
                	leadingCount++;
                    // we cast the element to a ListItem
                    ListItem listItem = (ListItem) element;

                    addSpacing(listItem.getSpacingBefore(), leading, listItem.getFont());

                    // we adjust the document
                    alignment = listItem.getAlignment();
                    indentation.listIndentLeft += listItem.getIndentationLeft();
                    indentation.indentRight += listItem.getIndentationRight();
                    leading = listItem.getTotalLeading();
                    carriageReturn();

                    // we prepare the current line to be able to show us the listsymbol
                    line.setListItem(listItem);
                    // we process the item
                    element.process(this);

                    addSpacing(listItem.getSpacingAfter(), listItem.getTotalLeading(), listItem.getFont());

                    // if the last line is justified, it should be aligned to the left
                    if (line.hasToBeJustified()) {
                    	line.resetAlignment();
                    }
                    // some parameters are set back to normal again
                    carriageReturn();
                    indentation.listIndentLeft -= listItem.getIndentationLeft();
                    indentation.indentRight -= listItem.getIndentationRight();
                    leadingCount--;
                    break;
                }
                case Element.RECTANGLE: {
                    Rectangle rectangle = (Rectangle) element;
                    graphics.rectangle(rectangle);
                    pageEmpty = false;
                    break;
                }
                case Element.PTABLE: {
                    PdfPTable ptable = (PdfPTable)element;
                    if (ptable.size() <= ptable.getHeaderRows())
                        break; //nothing to do

                    // before every table, we add a new line and flush all lines
                    ensureNewLine();
                    flushLines();

                    addPTable(ptable);
                    pageEmpty = false;
                    newLine();
                    break;
                }
                case Element.MULTI_COLUMN_TEXT: {
                    ensureNewLine();
                    flushLines();
                    MultiColumnText multiText = (MultiColumnText) element;
                    float height = multiText.write(writer.getDirectContent(), this, indentTop() - currentHeight);
                    currentHeight += height;
                    text.moveText(0, -1f* height);
                    pageEmpty = false;
                    break;
                }
                case Element.TABLE : {
                    if (element instanceof SimpleTable) {
                    	PdfPTable ptable = ((SimpleTable)element).createPdfPTable();
                    	if (ptable.size() <= ptable.getHeaderRows())
                    		break; //nothing to do

                    	// before every table, we add a new line and flush all lines
                    	ensureNewLine();
                    	flushLines();
                    	addPTable(ptable);
                    	pageEmpty = false;
                    	break;
                    } else if (element instanceof Table) {
                    	try {
                       		PdfPTable ptable = ((Table)element).createPdfPTable();
                       		if (ptable.size() <= ptable.getHeaderRows())
                       			break; //nothing to do
                       		// before every table, we add a new line and flush all lines
                       		ensureNewLine();
                       		flushLines();
                       		addPTable(ptable);
                       		pageEmpty = false;
                       		break;
                    	}
                    	catch(BadElementException bee) {
                    		// constructing the PdfTable
                    		// Before the table, add a blank line using offset or default leading
                    		float offset = ((Table)element).getOffset();
                    		if (Float.isNaN(offset))
                    			offset = leading;
                    		carriageReturn();
                    		lines.add(new PdfLine(indentLeft(), indentRight(), alignment, offset));
                    		currentHeight += offset;
                    		addPdfTable((Table)element);
                    	}
					} else {
						return false;
					}
                    break;
                }
                case Element.JPEG:
                case Element.JPEG2000:
                case Element.JBIG2:
                case Element.IMGRAW:
                case Element.IMGTEMPLATE: {
                    //carriageReturn(); suggestion by Marc Campforts
                    add((Image) element);
                    break;
                }
                case Element.YMARK: {
                    DrawInterface zh = (DrawInterface)element;
                    zh.draw(graphics, indentLeft(), indentBottom(), indentRight(), indentTop(), indentTop() - currentHeight - (leadingCount > 0 ? leading : 0));
                    pageEmpty = false;
                    break;
                }
                case Element.MARKED: {
                	MarkedObject mo;
                	if (element instanceof MarkedSection) {
                		mo = ((MarkedSection)element).getTitle();
                		if (mo != null) {
                			mo.process(this);
                		}
                	}
                	mo = (MarkedObject)element;
                	mo.process(this);
                	break;
                }
                default:
                    return false;
            }
            lastElementType = element.type();
            return true;
        }
        catch(Exception e) {
            throw new DocumentException(e);
        }
    }

//	[L1] DocListener interface

    /**
     * Opens the document.
     * <P>
     * You have to open the document before you can begin to add content
     * to the body of the document.
     */
    public void open() {
        if (!open) {
            super.open();
            writer.open();
            rootOutline = new PdfOutline(writer);
            currentOutline = rootOutline;
        }
        try {
            initPage();
        }
        catch(DocumentException de) {
            throw new ExceptionConverter(de);
        }
    }

//	[L2] DocListener interface

    /**
     * Closes the document.
     * <B>
     * Once all the content has been written in the body, you have to close
     * the body. After that nothing can be written to the body anymore.
     */
    public void close() {
        if (close) {
            return;
        }
        try {
        	boolean wasImage = (imageWait != null);
            newPage();
            if (imageWait != null || wasImage) newPage();
            if (annotationsImp.hasUnusedAnnotations())
                throw new RuntimeException(MessageLocalization.getComposedMessage("not.all.annotations.could.be.added.to.the.document.the.document.doesn.t.have.enough.pages"));
            PdfPageEvent pageEvent = writer.getPageEvent();
            if (pageEvent != null)
                pageEvent.onCloseDocument(writer, this);
            super.close();

            writer.addLocalDestinations(localDestinations);
            calculateOutlineCount();
            writeOutlines();
        }
        catch(Exception e) {
            throw ExceptionConverter.convertException(e);
        }

        writer.close();
    }

//	[L3] DocListener interface
    protected int textEmptySize;

    // [C9] Metadata for the page
    /** XMP Metadata for the page. */
    protected byte[] xmpMetadata = null;
	/**
	 * Use this method to set the XMP Metadata.
	 * @param xmpMetadata The xmpMetadata to set.
	 */
	public void setXmpMetadata(byte[] xmpMetadata) {
		this.xmpMetadata = xmpMetadata;
	}

    /**
     * Makes a new page and sends it to the <CODE>PdfWriter</CODE>.
     *
     * @return a <CODE>boolean</CODE>
     */
    public boolean newPage() {
        lastElementType = -1;
        if (isPageEmpty()) {
        	setNewPageSizeAndMargins();
            return false;
        }
    	if (!open || close) {
    		throw new RuntimeException(MessageLocalization.getComposedMessage("the.document.is.not.open"));
    	}
        PdfPageEvent pageEvent = writer.getPageEvent();
        if (pageEvent != null)
            pageEvent.onEndPage(writer, this);

        //Added to inform any listeners that we are moving to a new page (added by David Freels)
        super.newPage();

        // the following 2 lines were added by Pelikan Stephan
        indentation.imageIndentLeft = 0;
        indentation.imageIndentRight = 0;

        try {
            // we flush the arraylist with recently written lines
        	flushLines();

        	// we prepare the elements of the page dictionary

        	// [U1] page size and rotation
        	int rotation = pageSize.getRotation();

        	// [C10]
        	if (writer.isPdfX()) {
        		if (thisBoxSize.containsKey("art") && thisBoxSize.containsKey("trim"))
        			throw new PdfXConformanceException(MessageLocalization.getComposedMessage("only.one.of.artbox.or.trimbox.can.exist.in.the.page"));
        		if (!thisBoxSize.containsKey("art") && !thisBoxSize.containsKey("trim")) {
        			if (thisBoxSize.containsKey("crop"))
        				thisBoxSize.put("trim", thisBoxSize.get("crop"));
        			else
        				thisBoxSize.put("trim", new PdfRectangle(pageSize, pageSize.getRotation()));
        		}
        	}

        	// [M1]
        	pageResources.addDefaultColorDiff(writer.getDefaultColorspace());
            if (writer.isRgbTransparencyBlending()) {
                PdfDictionary dcs = new PdfDictionary();
                dcs.put(PdfName.CS, PdfName.DEVICERGB);
                pageResources.addDefaultColorDiff(dcs);
            }
        	PdfDictionary resources = pageResources.getResources();

        	// we create the page dictionary

        	PdfPage page = new PdfPage(new PdfRectangle(pageSize, rotation), thisBoxSize, resources, rotation);
        	page.put(PdfName.TABS, writer.getTabs());

            // we complete the page dictionary

            // [C9] if there is XMP data to add: add it
            if (xmpMetadata != null) {
            	PdfStream xmp = new PdfStream(xmpMetadata);
            	xmp.put(PdfName.TYPE, PdfName.METADATA);
            	xmp.put(PdfName.SUBTYPE, PdfName.XML);
            	PdfEncryption crypto = writer.getEncryption();
                if (crypto != null && !crypto.isMetadataEncrypted()) {
                    PdfArray ar = new PdfArray();
                    ar.add(PdfName.CRYPT);
                    xmp.put(PdfName.FILTER, ar);
                }
            	page.put(PdfName.METADATA, writer.addToBody(xmp).getIndirectReference());
            }

        	// [U3] page actions: transition, duration, additional actions
        	if (this.transition!=null) {
        		page.put(PdfName.TRANS, this.transition.getTransitionDictionary());
        		transition = null;
        	}
        	if (this.duration>0) {
        		page.put(PdfName.DUR,new PdfNumber(this.duration));
        		duration = 0;
        	}
        	if (pageAA != null) {
        		page.put(PdfName.AA, writer.addToBody(pageAA).getIndirectReference());
        		pageAA = null;
        	}

        	// [U4] we add the thumbs
        	if (thumb != null) {
        		page.put(PdfName.THUMB, thumb);
        		thumb = null;
        	}

        	// [U8] we check if the userunit is defined
        	if (writer.getUserunit() > 0f) {
        		page.put(PdfName.USERUNIT, new PdfNumber(writer.getUserunit()));
        	}

        	// [C5] and [C8] we add the annotations
        	if (annotationsImp.hasUnusedAnnotations()) {
        		PdfArray array = annotationsImp.rotateAnnotations(writer, pageSize);
        		if (array.size() != 0)
        			page.put(PdfName.ANNOTS, array);
        	}

        	// [F12] we add tag info
        	if (writer.isTagged())
        		page.put(PdfName.STRUCTPARENTS, new PdfNumber(writer.getCurrentPageNumber() - 1));

            if (text.size() > textEmptySize)
        		text.endText();
        	else
        		text = null;
        	writer.add(page, new PdfContents(writer.getDirectContentUnder(), graphics, text, writer.getDirectContent(), pageSize));
        	// we initialize the new page
        	initPage();
        }
        catch(DocumentException de) {
        	// maybe this never happens, but it's better to check.
        	throw new ExceptionConverter(de);
        }
        catch (IOException ioe) {
            throw new ExceptionConverter(ioe);
        }
        return true;
    }

//	[L4] DocListener interface

    /**
     * Sets the pagesize.
     *
     * @param pageSize the new pagesize
     * @return <CODE>true</CODE> if the page size was set
     */
    public boolean setPageSize(Rectangle pageSize) {
        if (writer != null && writer.isPaused()) {
            return false;
        }
        nextPageSize = new Rectangle(pageSize);
        return true;
    }

//	[L5] DocListener interface

    /** margin in x direction starting from the left. Will be valid in the next page */
    protected float nextMarginLeft;

    /** margin in x direction starting from the right. Will be valid in the next page */
    protected float nextMarginRight;

    /** margin in y direction starting from the top. Will be valid in the next page */
    protected float nextMarginTop;

    /** margin in y direction starting from the bottom. Will be valid in the next page */
    protected float nextMarginBottom;

    /**
     * Sets the margins.
     *
     * @param	marginLeft		the margin on the left
     * @param	marginRight		the margin on the right
     * @param	marginTop		the margin on the top
     * @param	marginBottom	the margin on the bottom
     * @return	a <CODE>boolean</CODE>
     */
    public boolean setMargins(float marginLeft, float marginRight, float marginTop, float marginBottom) {
        if (writer != null && writer.isPaused()) {
            return false;
        }
        nextMarginLeft = marginLeft;
        nextMarginRight = marginRight;
        nextMarginTop = marginTop;
        nextMarginBottom = marginBottom;
        return true;
    }

//	[L6] DocListener interface

    /**
     * @see com.lowagie.text.DocListener#setMarginMirroring(boolean)
     */
    public boolean setMarginMirroring(boolean MarginMirroring) {
        if (writer != null && writer.isPaused()) {
            return false;
        }
        return super.setMarginMirroring(MarginMirroring);
    }
    
    /**
     * @see com.lowagie.text.DocListener#setMarginMirroring(boolean)
     * @since	2.1.6
     */
    public boolean setMarginMirroringTopBottom(boolean MarginMirroringTopBottom) {
        if (writer != null && writer.isPaused()) {
            return false;
        }
        return super.setMarginMirroringTopBottom(MarginMirroringTopBottom);
    }

//	[L7] DocListener interface

    /**
     * Sets the page number.
     *
     * @param	pageN		the new page number
     */
    public void setPageCount(int pageN) {
        if (writer != null && writer.isPaused()) {
            return;
        }
        super.setPageCount(pageN);
    }

//	[L8] DocListener interface

    /**
     * Sets the page number to 0.
     */
    public void resetPageCount() {
        if (writer != null && writer.isPaused()) {
            return;
        }
        super.resetPageCount();
    }

//	[L9] DocListener interface

    /**
     * Changes the header of this document.
     *
     * @param header the new header
     */
    public void setHeader(HeaderFooter header) {
        if (writer != null && writer.isPaused()) {
            return;
        }
        super.setHeader(header);
    }

//	[L10] DocListener interface

    /**
     * Resets the header of this document.
     */
    public void resetHeader() {
        if (writer != null && writer.isPaused()) {
            return;
        }
        super.resetHeader();
    }

//	[L11] DocListener interface

    /**
     * Changes the footer of this document.
     *
     * @param	footer		the new footer
     */
    public void setFooter(HeaderFooter footer) {
        if (writer != null && writer.isPaused()) {
            return;
        }
        super.setFooter(footer);
    }

//	[L12] DocListener interface

    /**
     * Resets the footer of this document.
     */
    public void resetFooter() {
        if (writer != null && writer.isPaused()) {
            return;
        }
        super.resetFooter();
    }

// DOCLISTENER METHODS END

    /** Signals that OnOpenDocument should be called. */
    protected boolean firstPageEvent = true;

    /**
     * Initializes a page.
     * <P>
     * If the footer/header is set, it is printed.
     * @throws DocumentException on error
     */
    protected void initPage() throws DocumentException {
        // the pagenumber is incremented
        pageN++;

        // initialization of some page objects
        annotationsImp.resetAnnotations();
        pageResources = new PageResources();

        writer.resetContent();
        graphics = new PdfContentByte(writer);
        text = new PdfContentByte(writer);
        text.reset();
        text.beginText();
        textEmptySize = text.size();

    	markPoint = 0;
        setNewPageSizeAndMargins();
        imageEnd = -1;
        indentation.imageIndentRight = 0;
        indentation.imageIndentLeft = 0;
        indentation.indentBottom = 0;
        indentation.indentTop = 0;
        currentHeight = 0;

        // backgroundcolors, etc...
        thisBoxSize = new HashMap(boxSize);
        if (pageSize.getBackgroundColor() != null
        || pageSize.hasBorders()
        || pageSize.getBorderColor() != null) {
            add(pageSize);
        }

        float oldleading = leading;
        int oldAlignment = alignment;
        // if there is a footer, the footer is added
        doFooter();
        // we move to the left/top position of the page
        text.moveText(left(), top());
        doHeader();
        pageEmpty = true;
        // if there is an image waiting to be drawn, draw it
        try {
            if (imageWait != null) {
                add(imageWait);
                imageWait = null;
            }
        }
        catch(Exception e) {
            throw new ExceptionConverter(e);
        }
        leading = oldleading;
        alignment = oldAlignment;
        carriageReturn();

        PdfPageEvent pageEvent = writer.getPageEvent();
        if (pageEvent != null) {
            if (firstPageEvent) {
                pageEvent.onOpenDocument(writer, this);
            }
            pageEvent.onStartPage(writer, this);
        }
        firstPageEvent = false;
    }

    /** The line that is currently being written. */
    protected PdfLine line = null;
    /** The lines that are written until now. */
    protected ArrayList lines = new ArrayList();

    /**
     * Adds the current line to the list of lines and also adds an empty line.
     * @throws DocumentException on error
     */
    protected void newLine() throws DocumentException {
        lastElementType = -1;
        carriageReturn();
        if (lines != null && !lines.isEmpty()) {
            lines.add(line);
            currentHeight += line.height();
        }
        line = new PdfLine(indentLeft(), indentRight(), alignment, leading);
    }

    /**
     * If the current line is not empty or null, it is added to the arraylist
     * of lines and a new empty line is added.
     */
    protected void carriageReturn() {
        // the arraylist with lines may not be null
        if (lines == null) {
            lines = new ArrayList();
        }
        // If the current line is not null
        if (line != null) {
            // we check if the end of the page is reached (bugfix by Francois Gravel)
            if (currentHeight + line.height() + leading < indentTop() - indentBottom()) {
                // if so nonempty lines are added and the height is augmented
                if (line.size() > 0) {
                    currentHeight += line.height();
                    lines.add(line);
                    pageEmpty = false;
                }
            }
            // if the end of the line is reached, we start a new page
            else {
                newPage();
            }
        }
        if (imageEnd > -1 && currentHeight > imageEnd) {
            imageEnd = -1;
            indentation.imageIndentRight = 0;
            indentation.imageIndentLeft = 0;
        }
        // a new current line is constructed
        line = new PdfLine(indentLeft(), indentRight(), alignment, leading);
    }

    /**
     * Gets the current vertical page position.
     * @param ensureNewLine Tells whether a new line shall be enforced. This may cause side effects
     *   for elements that do not terminate the lines they've started because those lines will get
     *   terminated.
     * @return The current vertical page position.
     */
    public float getVerticalPosition(boolean ensureNewLine) {
        // ensuring that a new line has been started.
        if (ensureNewLine) {
          ensureNewLine();
        }
        return top() -  currentHeight - indentation.indentTop;
    }

    /** Holds the type of the last element, that has been added to the document. */
    protected int lastElementType = -1;

    /**
     * Ensures that a new line has been started.
     */
    protected void ensureNewLine() {
      try {
        if ((lastElementType == Element.PHRASE) ||
            (lastElementType == Element.CHUNK)) {
          newLine();
          flushLines();
        }
      } catch (DocumentException ex) {
        throw new ExceptionConverter(ex);
        }
    }

    /**
     * Writes all the lines to the text-object.
     *
     * @return the displacement that was caused
     * @throws DocumentException on error
     */
    protected float flushLines() throws DocumentException {
        // checks if the ArrayList with the lines is not null
        if (lines == null) {
            return 0;
        }
        // checks if a new Line has to be made.
        if (line != null && line.size() > 0) {
            lines.add(line);
            line = new PdfLine(indentLeft(), indentRight(), alignment, leading);
        }

        // checks if the ArrayList with the lines is empty
        if (lines.isEmpty()) {
            return 0;
        }

        // initialization of some parameters
        Object currentValues[] = new Object[2];
        PdfFont currentFont = null;
        float displacement = 0;
        PdfLine l;
        Float lastBaseFactor = new Float(0);
        currentValues[1] = lastBaseFactor;
        // looping over all the lines
        for (Iterator i = lines.iterator(); i.hasNext(); ) {

            // this is a line in the loop
            l = (PdfLine) i.next();

            float moveTextX = l.indentLeft() - indentLeft() + indentation.indentLeft + indentation.listIndentLeft + indentation.sectionIndentLeft;
            text.moveText(moveTextX, -l.height());
            // is the line preceded by a symbol?
            if (l.listSymbol() != null) {
                ColumnText.showTextAligned(graphics, Element.ALIGN_LEFT, new Phrase(l.listSymbol()), text.getXTLM() - l.listIndent(), text.getYTLM(), 0);
            }

            currentValues[0] = currentFont;

            writeLineToContent(l, text, graphics, currentValues, writer.getSpaceCharRatio());

            currentFont = (PdfFont)currentValues[0];
            displacement += l.height();
            text.moveText(-moveTextX, 0);

        }
        lines = new ArrayList();
        return displacement;
    }

    /** The characters to be applied the hanging punctuation. */
    static final String hangingPunctuation = ".,;:'";

    /**
     * Writes a text line to the document. It takes care of all the attributes.
     * <P>
     * Before entering the line position must have been established and the
     * <CODE>text</CODE> argument must be in text object scope (<CODE>beginText()</CODE>).
     * @param line the line to be written
     * @param text the <CODE>PdfContentByte</CODE> where the text will be written to
     * @param graphics the <CODE>PdfContentByte</CODE> where the graphics will be written to
     * @param currentValues the current font and extra spacing values
     * @param ratio
     * @throws DocumentException on error
     */
    void writeLineToContent(PdfLine line, PdfContentByte text, PdfContentByte graphics, Object currentValues[], float ratio)  throws DocumentException {
        PdfFont currentFont = (PdfFont)(currentValues[0]);
        float lastBaseFactor = ((Float)(currentValues[1])).floatValue();
        PdfChunk chunk;
        int numberOfSpaces;
        int lineLen;
        boolean isJustified;
        float hangingCorrection = 0;
        float hScale = 1;
        float lastHScale = Float.NaN;
        float baseWordSpacing = 0;
        float baseCharacterSpacing = 0;
        float glueWidth = 0;

        numberOfSpaces = line.numberOfSpaces();
        lineLen = line.GetLineLengthUtf32();
        // does the line need to be justified?
        isJustified = line.hasToBeJustified() && (numberOfSpaces != 0 || lineLen > 1);
        int separatorCount = line.getSeparatorCount();
        if (separatorCount > 0) {
        	glueWidth = line.widthLeft() / separatorCount;
        }
        else if (isJustified) {
            if (line.isNewlineSplit() && line.widthLeft() >= (lastBaseFactor * (ratio * numberOfSpaces + lineLen - 1))) {
                if (line.isRTL()) {
                    text.moveText(line.widthLeft() - lastBaseFactor * (ratio * numberOfSpaces + lineLen - 1), 0);
                }
                baseWordSpacing = ratio * lastBaseFactor;
                baseCharacterSpacing = lastBaseFactor;
            }
            else {
                float width = line.widthLeft();
                PdfChunk last = line.getChunk(line.size() - 1);
                if (last != null) {
                    String s = last.toString();
                    char c;
                    if (s.length() > 0 && hangingPunctuation.indexOf((c = s.charAt(s.length() - 1))) >= 0) {
                        float oldWidth = width;
                        width += last.font().width(c) * 0.4f;
                        hangingCorrection = width - oldWidth;
                    }
                }
                float baseFactor = width / (ratio * numberOfSpaces + lineLen - 1);
                baseWordSpacing = ratio * baseFactor;
                baseCharacterSpacing = baseFactor;
                lastBaseFactor = baseFactor;
            }
        }

        int lastChunkStroke = line.getLastStrokeChunk();
        int chunkStrokeIdx = 0;
        float xMarker = text.getXTLM();
        float baseXMarker = xMarker;
        float yMarker = text.getYTLM();
        boolean adjustMatrix = false;
        float tabPosition = 0;

        // looping over all the chunks in 1 line
        for (Iterator j = line.iterator(); j.hasNext(); ) {
            chunk = (PdfChunk) j.next();
            Color color = chunk.color();
            hScale = 1;

            if (chunkStrokeIdx <= lastChunkStroke) {
                float width;
                if (isJustified) {
                    width = chunk.getWidthCorrected(baseCharacterSpacing, baseWordSpacing);
                }
                else {
                    width = chunk.width();
                }
                if (chunk.isStroked()) {
                    PdfChunk nextChunk = line.getChunk(chunkStrokeIdx + 1);
                    if (chunk.isSeparator()) {
                    	width = glueWidth;
                    	Object[] sep = (Object[])chunk.getAttribute(Chunk.SEPARATOR);
                        DrawInterface di = (DrawInterface)sep[0];
                        Boolean vertical = (Boolean)sep[1];
                        float fontSize = chunk.font().size();
                        float ascender = chunk.font().getFont().getFontDescriptor(BaseFont.ASCENT, fontSize);
                        float descender = chunk.font().getFont().getFontDescriptor(BaseFont.DESCENT, fontSize);
                        if (vertical.booleanValue()) {
                        	di.draw(graphics, baseXMarker, yMarker + descender, baseXMarker + line.getOriginalWidth(), ascender - descender, yMarker);
                        }
                        else {
                        	di.draw(graphics, xMarker, yMarker + descender, xMarker + width, ascender - descender, yMarker);
                        }
                    }
                    if (chunk.isTab()) {
                    	Object[] tab = (Object[])chunk.getAttribute(Chunk.TAB);
                        DrawInterface di = (DrawInterface)tab[0];
                        tabPosition = ((Float)tab[1]).floatValue() + ((Float)tab[3]).floatValue();
                        float fontSize = chunk.font().size();
                        float ascender = chunk.font().getFont().getFontDescriptor(BaseFont.ASCENT, fontSize);
                        float descender = chunk.font().getFont().getFontDescriptor(BaseFont.DESCENT, fontSize);
                        if (tabPosition > xMarker) {
                        	di.draw(graphics, xMarker, yMarker + descender, tabPosition, ascender - descender, yMarker);
                        }
                        float tmp = xMarker;
                    	xMarker = tabPosition;
                    	tabPosition = tmp;
                    }
                    if (chunk.isAttribute(Chunk.BACKGROUND)) {
                        float subtract = lastBaseFactor;
                        if (nextChunk != null && nextChunk.isAttribute(Chunk.BACKGROUND))
                            subtract = 0;
                        if (nextChunk == null)
                            subtract += hangingCorrection;
                        float fontSize = chunk.font().size();
                        float ascender = chunk.font().getFont().getFontDescriptor(BaseFont.ASCENT, fontSize);
                        float descender = chunk.font().getFont().getFontDescriptor(BaseFont.DESCENT, fontSize);
                        Object bgr[] = (Object[])chunk.getAttribute(Chunk.BACKGROUND);
                        graphics.setColorFill((Color)bgr[0]);
                        float extra[] = (float[])bgr[1];
                        graphics.rectangle(xMarker - extra[0],
                            yMarker + descender - extra[1] + chunk.getTextRise(),
                            width - subtract + extra[0] + extra[2],
                            ascender - descender + extra[1] + extra[3]);
                        graphics.fill();
                        graphics.setGrayFill(0);
                    }
                    if (chunk.isAttribute(Chunk.UNDERLINE)) {
                        float subtract = lastBaseFactor;
                        if (nextChunk != null && nextChunk.isAttribute(Chunk.UNDERLINE))
                            subtract = 0;
                        if (nextChunk == null)
                            subtract += hangingCorrection;
                        Object unders[][] = (Object[][])chunk.getAttribute(Chunk.UNDERLINE);
                        Color scolor = null;
                        for (int k = 0; k < unders.length; ++k) {
                            Object obj[] = unders[k];
                            scolor = (Color)obj[0];
                            float ps[] = (float[])obj[1];
                            if (scolor == null)
                                scolor = color;
                            if (scolor != null)
                                graphics.setColorStroke(scolor);
                            float fsize = chunk.font().size();
                            graphics.setLineWidth(ps[0] + fsize * ps[1]);
                            float shift = ps[2] + fsize * ps[3];
                            int cap2 = (int)ps[4];
                            if (cap2 != 0)
                                graphics.setLineCap(cap2);
                            graphics.moveTo(xMarker, yMarker + shift);
                            graphics.lineTo(xMarker + width - subtract, yMarker + shift);
                            graphics.stroke();
                            if (scolor != null)
                                graphics.resetGrayStroke();
                            if (cap2 != 0)
                                graphics.setLineCap(0);
                        }
                        graphics.setLineWidth(1);
                    }
                    if (chunk.isAttribute(Chunk.ACTION)) {
                        float subtract = lastBaseFactor;
                        if (nextChunk != null && nextChunk.isAttribute(Chunk.ACTION))
                            subtract = 0;
                        if (nextChunk == null)
                            subtract += hangingCorrection;
                        text.addAnnotation(new PdfAnnotation(writer, xMarker, yMarker, xMarker + width - subtract, yMarker + chunk.font().size(), (PdfAction)chunk.getAttribute(Chunk.ACTION)));
                    }
                    if (chunk.isAttribute(Chunk.REMOTEGOTO)) {
                        float subtract = lastBaseFactor;
                        if (nextChunk != null && nextChunk.isAttribute(Chunk.REMOTEGOTO))
                            subtract = 0;
                        if (nextChunk == null)
                            subtract += hangingCorrection;
                        Object obj[] = (Object[])chunk.getAttribute(Chunk.REMOTEGOTO);
                        String filename = (String)obj[0];
                        if (obj[1] instanceof String)
                            remoteGoto(filename, (String)obj[1], xMarker, yMarker, xMarker + width - subtract, yMarker + chunk.font().size());
                        else
                            remoteGoto(filename, ((Integer)obj[1]).intValue(), xMarker, yMarker, xMarker + width - subtract, yMarker + chunk.font().size());
                    }
                    if (chunk.isAttribute(Chunk.LOCALGOTO)) {
                        float subtract = lastBaseFactor;
                        if (nextChunk != null && nextChunk.isAttribute(Chunk.LOCALGOTO))
                            subtract = 0;
                        if (nextChunk == null)
                            subtract += hangingCorrection;
                        localGoto((String)chunk.getAttribute(Chunk.LOCALGOTO), xMarker, yMarker, xMarker + width - subtract, yMarker + chunk.font().size());
                    }
                    if (chunk.isAttribute(Chunk.LOCALDESTINATION)) {
                        float subtract = lastBaseFactor;
                        if (nextChunk != null && nextChunk.isAttribute(Chunk.LOCALDESTINATION))
                            subtract = 0;
                        if (nextChunk == null)
                            subtract += hangingCorrection;
                        localDestination((String)chunk.getAttribute(Chunk.LOCALDESTINATION), new PdfDestination(PdfDestination.XYZ, xMarker, yMarker + chunk.font().size(), 0));
                    }
                    if (chunk.isAttribute(Chunk.GENERICTAG)) {
                        float subtract = lastBaseFactor;
                        if (nextChunk != null && nextChunk.isAttribute(Chunk.GENERICTAG))
                            subtract = 0;
                        if (nextChunk == null)
                            subtract += hangingCorrection;
                        Rectangle rect = new Rectangle(xMarker, yMarker, xMarker + width - subtract, yMarker + chunk.font().size());
                        PdfPageEvent pev = writer.getPageEvent();
                        if (pev != null)
                            pev.onGenericTag(writer, this, rect, (String)chunk.getAttribute(Chunk.GENERICTAG));
                    }
                    if (chunk.isAttribute(Chunk.PDFANNOTATION)) {
                        float subtract = lastBaseFactor;
                        if (nextChunk != null && nextChunk.isAttribute(Chunk.PDFANNOTATION))
                            subtract = 0;
                        if (nextChunk == null)
                            subtract += hangingCorrection;
                        float fontSize = chunk.font().size();
                        float ascender = chunk.font().getFont().getFontDescriptor(BaseFont.ASCENT, fontSize);
                        float descender = chunk.font().getFont().getFontDescriptor(BaseFont.DESCENT, fontSize);
                        PdfAnnotation annot = PdfFormField.shallowDuplicate((PdfAnnotation)chunk.getAttribute(Chunk.PDFANNOTATION));
                        annot.put(PdfName.RECT, new PdfRectangle(xMarker, yMarker + descender, xMarker + width - subtract, yMarker + ascender));
                        text.addAnnotation(annot);
                    }
                    float params[] = (float[])chunk.getAttribute(Chunk.SKEW);
                    Float hs = (Float)chunk.getAttribute(Chunk.HSCALE);
                    if (params != null || hs != null) {
                        float b = 0, c = 0;
                        if (params != null) {
                            b = params[0];
                            c = params[1];
                        }
                        if (hs != null)
                            hScale = hs.floatValue();
                        text.setTextMatrix(hScale, b, c, 1, xMarker, yMarker);
                    }
                    if (chunk.isAttribute(Chunk.CHAR_SPACING)) {
                    	Float cs = (Float) chunk.getAttribute(Chunk.CHAR_SPACING);
						text.setCharacterSpacing(cs.floatValue());
					}
                    if (chunk.isImage()) {
                        Image image = chunk.getImage();
                        float matrix[] = image.matrix();
                        matrix[Image.CX] = xMarker + chunk.getImageOffsetX() - matrix[Image.CX];
                        matrix[Image.CY] = yMarker + chunk.getImageOffsetY() - matrix[Image.CY];
                        graphics.addImage(image, matrix[0], matrix[1], matrix[2], matrix[3], matrix[4], matrix[5]);
                        text.moveText(xMarker + lastBaseFactor + image.getScaledWidth() - text.getXTLM(), 0);
                    }
                }
                xMarker += width;
                ++chunkStrokeIdx;
            }

            if (chunk.font().compareTo(currentFont) != 0) {
                currentFont = chunk.font();
                text.setFontAndSize(currentFont.getFont(), currentFont.size());
            }
            float rise = 0;
            Object textRender[] = (Object[])chunk.getAttribute(Chunk.TEXTRENDERMODE);
            int tr = 0;
            float strokeWidth = 1;
            Color strokeColor = null;
            Float fr = (Float)chunk.getAttribute(Chunk.SUBSUPSCRIPT);
            if (textRender != null) {
                tr = ((Integer)textRender[0]).intValue() & 3;
                if (tr != PdfContentByte.TEXT_RENDER_MODE_FILL)
                    text.setTextRenderingMode(tr);
                if (tr == PdfContentByte.TEXT_RENDER_MODE_STROKE || tr == PdfContentByte.TEXT_RENDER_MODE_FILL_STROKE) {
                    strokeWidth = ((Float)textRender[1]).floatValue();
                    if (strokeWidth != 1)
                        text.setLineWidth(strokeWidth);
                    strokeColor = (Color)textRender[2];
                    if (strokeColor == null)
                        strokeColor = color;
                    if (strokeColor != null)
                        text.setColorStroke(strokeColor);
                }
            }
            if (fr != null)
                rise = fr.floatValue();
            if (color != null)
                text.setColorFill(color);
            if (rise != 0)
                text.setTextRise(rise);
            if (chunk.isImage()) {
                adjustMatrix = true;
            }
            else if (chunk.isHorizontalSeparator()) {
            	PdfTextArray array = new PdfTextArray();
            	array.add(-glueWidth * 1000f / chunk.font.size() / hScale);
            	text.showText(array);
            }
            else if (chunk.isTab()) {
            	PdfTextArray array = new PdfTextArray();
            	array.add((tabPosition - xMarker) * 1000f / chunk.font.size() / hScale);
            	text.showText(array);
            }
            // If it is a CJK chunk or Unicode TTF we will have to simulate the
            // space adjustment.
            else if (isJustified && numberOfSpaces > 0 && chunk.isSpecialEncoding()) {
                if (hScale != lastHScale) {
                    lastHScale = hScale;
                    text.setWordSpacing(baseWordSpacing / hScale);
                    text.setCharacterSpacing(baseCharacterSpacing / hScale + text.getCharacterSpacing());
                }
                String s = chunk.toString();
                int idx = s.indexOf(' ');
                if (idx < 0)
                    text.showText(s);
                else {
                    float spaceCorrection = - baseWordSpacing * 1000f / chunk.font.size() / hScale;
                    PdfTextArray textArray = new PdfTextArray(s.substring(0, idx));
                    int lastIdx = idx;
                    while ((idx = s.indexOf(' ', lastIdx + 1)) >= 0) {
                        textArray.add(spaceCorrection);
                        textArray.add(s.substring(lastIdx, idx));
                        lastIdx = idx;
                    }
                    textArray.add(spaceCorrection);
                    textArray.add(s.substring(lastIdx));
                    text.showText(textArray);
                }
            }
            else {
                if (isJustified && hScale != lastHScale) {
                    lastHScale = hScale;
                    text.setWordSpacing(baseWordSpacing / hScale);
                    text.setCharacterSpacing(baseCharacterSpacing / hScale + text.getCharacterSpacing());
                }
                text.showText(chunk.toString());
            }

            if (rise != 0)
                text.setTextRise(0);
            if (color != null)
                text.resetRGBColorFill();
            if (tr != PdfContentByte.TEXT_RENDER_MODE_FILL)
                text.setTextRenderingMode(PdfContentByte.TEXT_RENDER_MODE_FILL);
            if (strokeColor != null)
                text.resetRGBColorStroke();
            if (strokeWidth != 1)
                text.setLineWidth(1);
            if (chunk.isAttribute(Chunk.SKEW) || chunk.isAttribute(Chunk.HSCALE)) {
                adjustMatrix = true;
                text.setTextMatrix(xMarker, yMarker);
            }
            if (chunk.isAttribute(Chunk.CHAR_SPACING)) {
				text.setCharacterSpacing(baseCharacterSpacing);
            }
        }
        if (isJustified) {
            text.setWordSpacing(0);
            text.setCharacterSpacing(0);
            if (line.isNewlineSplit())
                lastBaseFactor = 0;
        }
        if (adjustMatrix)
            text.moveText(baseXMarker - text.getXTLM(), 0);
        currentValues[0] = currentFont;
        currentValues[1] = new Float(lastBaseFactor);
    }

    protected Indentation indentation = new Indentation();

    /**
     * @since	2.0.8 (PdfDocument was package-private before)
     */
    public static class Indentation {

        /** This represents the current indentation of the PDF Elements on the left side. */
        float indentLeft = 0;

        /** Indentation to the left caused by a section. */
        float sectionIndentLeft = 0;

        /** This represents the current indentation of the PDF Elements on the left side. */
        float listIndentLeft = 0;

        /** This is the indentation caused by an image on the left. */
        float imageIndentLeft = 0;

        /** This represents the current indentation of the PDF Elements on the right side. */
        float indentRight = 0;

        /** Indentation to the right caused by a section. */
        float sectionIndentRight = 0;

        /** This is the indentation caused by an image on the right. */
        float imageIndentRight = 0;

        /** This represents the current indentation of the PDF Elements on the top side. */
        float indentTop = 0;

        /** This represents the current indentation of the PDF Elements on the bottom side. */
        float indentBottom = 0;
    }

    /**
     * Gets the indentation on the left side.
     *
     * @return	a margin
     */

    protected float indentLeft() {
        return left(indentation.indentLeft + indentation.listIndentLeft + indentation.imageIndentLeft + indentation.sectionIndentLeft);
    }

    /**
     * Gets the indentation on the right side.
     *
     * @return	a margin
     */

    protected float indentRight() {
        return right(indentation.indentRight + indentation.sectionIndentRight + indentation.imageIndentRight);
    }

    /**
     * Gets the indentation on the top side.
     *
     * @return	a margin
     */

    protected float indentTop() {
        return top(indentation.indentTop);
    }

    /**
     * Gets the indentation on the bottom side.
     *
     * @return	a margin
     */

    float indentBottom() {
        return bottom(indentation.indentBottom);
    }

    /**
     * Adds extra space.
     * This method should probably be rewritten.
     */
    protected void addSpacing(float extraspace, float oldleading, Font f) {
    	if (extraspace == 0) return;
    	if (pageEmpty) return;
    	if (currentHeight + line.height() + leading > indentTop() - indentBottom()) return;
        leading = extraspace;
        carriageReturn();
        if (f.isUnderlined() || f.isStrikethru()) {
            f = new Font(f);
            int style = f.getStyle();
            style &= ~Font.UNDERLINE;
            style &= ~Font.STRIKETHRU;
            f.setStyle(style);
        }
        Chunk space = new Chunk(" ", f);
        space.process(this);
        carriageReturn();
        leading = oldleading;
    }

//	Info Dictionary and Catalog

    /** some meta information about the Document. */
    protected PdfInfo info = new PdfInfo();

    /**
     * Gets the <CODE>PdfInfo</CODE>-object.
     *
     * @return	<CODE>PdfInfo</COPE>
     */

    PdfInfo getInfo() {
        return info;
    }

    /**
     * Gets the <CODE>PdfCatalog</CODE>-object.
     *
     * @param pages an indirect reference to this document pages
     * @return <CODE>PdfCatalog</CODE>
     */

    PdfCatalog getCatalog(PdfIndirectReference pages) {
        PdfCatalog catalog = new PdfCatalog(pages, writer);

        // [C1] outlines
        if (rootOutline.getKids().size() > 0) {
            catalog.put(PdfName.PAGEMODE, PdfName.USEOUTLINES);
            catalog.put(PdfName.OUTLINES, rootOutline.indirectReference());
        }

        // [C2] version
        writer.getPdfVersion().addToCatalog(catalog);

        // [C3] preferences
        viewerPreferences.addToCatalog(catalog);

        // [C4] pagelabels
        if (pageLabels != null) {
            catalog.put(PdfName.PAGELABELS, pageLabels.getDictionary(writer));
        }

        // [C5] named objects
        catalog.addNames(localDestinations, getDocumentLevelJS(), documentFileAttachment, writer);

        // [C6] actions
        if (openActionName != null) {
            PdfAction action = getLocalGotoAction(openActionName);
            catalog.setOpenAction(action);
        }
        else if (openActionAction != null)
            catalog.setOpenAction(openActionAction);
        if (additionalActions != null)   {
            catalog.setAdditionalActions(additionalActions);
        }

        // [C7] portable collections
        if (collection != null) {
        	catalog.put(PdfName.COLLECTION, collection);
        }

        // [C8] AcroForm
        if (annotationsImp.hasValidAcroForm()) {
            try {
                catalog.put(PdfName.ACROFORM, writer.addToBody(annotationsImp.getAcroForm()).getIndirectReference());
            }
            catch (IOException e) {
                throw new ExceptionConverter(e);
            }
        }

        return catalog;
    }

//	[C1] outlines

    /** This is the root outline of the document. */
    protected PdfOutline rootOutline;

    /** This is the current <CODE>PdfOutline</CODE> in the hierarchy of outlines. */
    protected PdfOutline currentOutline;

    /**
     * Adds a named outline to the document .
     * @param outline the outline to be added
     * @param name the name of this local destination
     */
    void addOutline(PdfOutline outline, String name) {
        localDestination(name, outline.getPdfDestination());
    }

    /**
     * Gets the root outline. All the outlines must be created with a parent.
     * The first level is created with this outline.
     * @return the root outline
     */
    public PdfOutline getRootOutline() {
        return rootOutline;
    }


    /**
     * Updates the count in the outlines.
     */
    void calculateOutlineCount() {
        if (rootOutline.getKids().size() == 0)
            return;
        traverseOutlineCount(rootOutline);
    }

    /**
     * Recursive method to update the count in the outlines.
     */
    void traverseOutlineCount(PdfOutline outline) {
        ArrayList kids = outline.getKids();
        PdfOutline parent = outline.parent();
        if (kids.isEmpty()) {
            if (parent != null) {
                parent.setCount(parent.getCount() + 1);
            }
        }
        else {
            for (int k = 0; k < kids.size(); ++k) {
                traverseOutlineCount((PdfOutline)kids.get(k));
            }
            if (parent != null) {
                if (outline.isOpen()) {
                    parent.setCount(outline.getCount() + parent.getCount() + 1);
                }
                else {
                    parent.setCount(parent.getCount() + 1);
                    outline.setCount(-outline.getCount());
                }
            }
        }
    }

    /**
     * Writes the outline tree to the body of the PDF document.
     */
    void writeOutlines() throws IOException {
        if (rootOutline.getKids().size() == 0)
            return;
        outlineTree(rootOutline);
        writer.addToBody(rootOutline, rootOutline.indirectReference());
    }

    /**
     * Recursive method used to write outlines.
     */
    void outlineTree(PdfOutline outline) throws IOException {
        outline.setIndirectReference(writer.getPdfIndirectReference());
        if (outline.parent() != null)
            outline.put(PdfName.PARENT, outline.parent().indirectReference());
        ArrayList kids = outline.getKids();
        int size = kids.size();
        for (int k = 0; k < size; ++k)
            outlineTree((PdfOutline)kids.get(k));
        for (int k = 0; k < size; ++k) {
            if (k > 0)
                ((PdfOutline)kids.get(k)).put(PdfName.PREV, ((PdfOutline)kids.get(k - 1)).indirectReference());
            if (k < size - 1)
                ((PdfOutline)kids.get(k)).put(PdfName.NEXT, ((PdfOutline)kids.get(k + 1)).indirectReference());
        }
        if (size > 0) {
            outline.put(PdfName.FIRST, ((PdfOutline)kids.get(0)).indirectReference());
            outline.put(PdfName.LAST, ((PdfOutline)kids.get(size - 1)).indirectReference());
        }
        for (int k = 0; k < size; ++k) {
            PdfOutline kid = (PdfOutline)kids.get(k);
            writer.addToBody(kid, kid.indirectReference());
        }
    }

//  [C3] PdfViewerPreferences interface

	/** Contains the Viewer preferences of this PDF document. */
    protected PdfViewerPreferencesImp viewerPreferences = new PdfViewerPreferencesImp();
    /** @see com.lowagie.text.pdf.interfaces.PdfViewerPreferences#setViewerPreferences(int) */
    void setViewerPreferences(int preferences) {
        this.viewerPreferences.setViewerPreferences(preferences);
    }

    /** @see com.lowagie.text.pdf.interfaces.PdfViewerPreferences#addViewerPreference(com.lowagie.text.pdf.PdfName, com.lowagie.text.pdf.PdfObject) */
    void addViewerPreference(PdfName key, PdfObject value) {
    	this.viewerPreferences.addViewerPreference(key, value);
    }

//	[C4] Page labels

    protected PdfPageLabels pageLabels;
    /**
     * Sets the page labels
     * @param pageLabels the page labels
     */
    void setPageLabels(PdfPageLabels pageLabels) {
        this.pageLabels = pageLabels;
    }

//	[C5] named objects: local destinations, javascript, embedded files

    /**
     * Implements a link to other part of the document. The jump will
     * be made to a local destination with the same name, that must exist.
     * @param name the name for this link
     * @param llx the lower left x corner of the activation area
     * @param lly the lower left y corner of the activation area
     * @param urx the upper right x corner of the activation area
     * @param ury the upper right y corner of the activation area
     */
    void localGoto(String name, float llx, float lly, float urx, float ury) {
        PdfAction action = getLocalGotoAction(name);
        annotationsImp.addPlainAnnotation(new PdfAnnotation(writer, llx, lly, urx, ury, action));
    }

    /**
     * Implements a link to another document.
     * @param filename the filename for the remote document
     * @param name the name to jump to
     * @param llx the lower left x corner of the activation area
     * @param lly the lower left y corner of the activation area
     * @param urx the upper right x corner of the activation area
     * @param ury the upper right y corner of the activation area
     */
    void remoteGoto(String filename, String name, float llx, float lly, float urx, float ury) {
        annotationsImp.addPlainAnnotation(new PdfAnnotation(writer, llx, lly, urx, ury, new PdfAction(filename, name)));
    }

    /**
     * Implements a link to another document.
     * @param filename the filename for the remote document
     * @param page the page to jump to
     * @param llx the lower left x corner of the activation area
     * @param lly the lower left y corner of the activation area
     * @param urx the upper right x corner of the activation area
     * @param ury the upper right y corner of the activation area
     */
    void remoteGoto(String filename, int page, float llx, float lly, float urx, float ury) {
        addAnnotation(new PdfAnnotation(writer, llx, lly, urx, ury, new PdfAction(filename, page)));
    }

    /** Implements an action in an area.
     * @param action the <CODE>PdfAction</CODE>
     * @param llx the lower left x corner of the activation area
     * @param lly the lower left y corner of the activation area
     * @param urx the upper right x corner of the activation area
     * @param ury the upper right y corner of the activation area
     */
    void setAction(PdfAction action, float llx, float lly, float urx, float ury) {
        addAnnotation(new PdfAnnotation(writer, llx, lly, urx, ury, action));
    }

    /**
     * Stores the destinations keyed by name. Value is
     * <CODE>Object[]{PdfAction,PdfIndirectReference,PdfDestintion}</CODE>.
     */
    protected TreeMap localDestinations = new TreeMap();

    PdfAction getLocalGotoAction(String name) {
        PdfAction action;
        Object obj[] = (Object[])localDestinations.get(name);
        if (obj == null)
            obj = new Object[3];
        if (obj[0] == null) {
            if (obj[1] == null) {
                obj[1] = writer.getPdfIndirectReference();
            }
            action = new PdfAction((PdfIndirectReference)obj[1]);
            obj[0] = action;
            localDestinations.put(name, obj);
        }
        else {
            action = (PdfAction)obj[0];
        }
        return action;
    }

    /**
     * The local destination to where a local goto with the same
     * name will jump to.
     * @param name the name of this local destination
     * @param destination the <CODE>PdfDestination</CODE> with the jump coordinates
     * @return <CODE>true</CODE> if the local destination was added,
     * <CODE>false</CODE> if a local destination with the same name
     * already existed
     */
    boolean localDestination(String name, PdfDestination destination) {
        Object obj[] = (Object[])localDestinations.get(name);
        if (obj == null)
            obj = new Object[3];
        if (obj[2] != null)
            return false;
        obj[2] = destination;
        localDestinations.put(name, obj);
        if (!destination.hasPage())
        	destination.addPage(writer.getCurrentPage());
        return true;
    }

    /**
     * Stores a list of document level JavaScript actions.
     */
    int jsCounter;
    protected HashMap documentLevelJS = new HashMap();
    protected static final DecimalFormat SIXTEEN_DIGITS = new DecimalFormat("0000000000000000");
    void addJavaScript(PdfAction js) {
        if (js.get(PdfName.JS) == null)
            throw new RuntimeException(MessageLocalization.getComposedMessage("only.javascript.actions.are.allowed"));
        try {
            documentLevelJS.put(SIXTEEN_DIGITS.format(jsCounter++), writer.addToBody(js).getIndirectReference());
        }
        catch (IOException e) {
            throw new ExceptionConverter(e);
        }
    }
    void addJavaScript(String name, PdfAction js) {
        if (js.get(PdfName.JS) == null)
            throw new RuntimeException(MessageLocalization.getComposedMessage("only.javascript.actions.are.allowed"));
        try {
            documentLevelJS.put(name, writer.addToBody(js).getIndirectReference());
        }
        catch (IOException e) {
            throw new ExceptionConverter(e);
        }
    }

    HashMap getDocumentLevelJS() {
    	return documentLevelJS;
    }

    protected HashMap documentFileAttachment = new HashMap();

    void addFileAttachment(String description, PdfFileSpecification fs) throws IOException {
        if (description == null) {
        	PdfString desc = (PdfString)fs.get(PdfName.DESC);
        	if (desc == null) {
        		description = "";
        	}
        	else {
        		description = PdfEncodings.convertToString(desc.getBytes(), null);
        	}
        }
        fs.addDescription(description, true);
        if (description.length() == 0)
            description = "Unnamed";
        String fn = PdfEncodings.convertToString(new PdfString(description, PdfObject.TEXT_UNICODE).getBytes(), null);
        int k = 0;
        while (documentFileAttachment.containsKey(fn)) {
            ++k;
            fn = PdfEncodings.convertToString(new PdfString(description + " " + k, PdfObject.TEXT_UNICODE).getBytes(), null);
        }
        documentFileAttachment.put(fn, fs.getReference());
    }

    HashMap getDocumentFileAttachment() {
        return documentFileAttachment;
    }

//	[C6] document level actions

    protected String openActionName;

    void setOpenAction(String name) {
        openActionName = name;
        openActionAction = null;
    }

    protected PdfAction openActionAction;
    void setOpenAction(PdfAction action) {
        openActionAction = action;
        openActionName = null;
    }

    protected PdfDictionary additionalActions;
    void addAdditionalAction(PdfName actionType, PdfAction action)  {
        if (additionalActions == null)  {
            additionalActions = new PdfDictionary();
        }
        if (action == null)
            additionalActions.remove(actionType);
        else
            additionalActions.put(actionType, action);
        if (additionalActions.size() == 0)
            additionalActions = null;
    }

//	[C7] portable collections

    protected PdfCollection collection;

    /**
     * Sets the collection dictionary.
     * @param collection a dictionary of type PdfCollection
     */
	public void setCollection(PdfCollection collection) {
		this.collection = collection;
	}

//	[C8] AcroForm

	PdfAnnotationsImp annotationsImp;

    /**
     * Gets the AcroForm object.
     * @return the PdfAcroform object of the PdfDocument
     */
    PdfAcroForm getAcroForm() {
        return annotationsImp.getAcroForm();
    }

    void setSigFlags(int f) {
        annotationsImp.setSigFlags(f);
    }

    void addCalculationOrder(PdfFormField formField) {
        annotationsImp.addCalculationOrder(formField);
    }

    void addAnnotation(PdfAnnotation annot) {
        pageEmpty = false;
        annotationsImp.addAnnotation(annot);
    }

//	[F12] tagged PDF

    protected int markPoint;

	int getMarkPoint() {
	    return markPoint;
	}

	void incMarkPoint() {
	    ++markPoint;
	}

//	[U1] page sizes

    /** This is the size of the next page. */
    protected Rectangle nextPageSize = null;

    /** This is the size of the several boxes of the current Page. */
    protected HashMap thisBoxSize = new HashMap();

    /** This is the size of the several boxes that will be used in
     * the next page. */
    protected HashMap boxSize = new HashMap();

    void setCropBoxSize(Rectangle crop) {
        setBoxSize("crop", crop);
    }

    void setBoxSize(String boxName, Rectangle size) {
        if (size == null)
            boxSize.remove(boxName);
        else
            boxSize.put(boxName, new PdfRectangle(size));
    }

    protected void setNewPageSizeAndMargins() {
        pageSize = nextPageSize;
    	if (marginMirroring && (getPageNumber() & 1) == 0) {
            marginRight = nextMarginLeft;
            marginLeft = nextMarginRight;
        }
        else {
            marginLeft = nextMarginLeft;
            marginRight = nextMarginRight;
        }
    	if (marginMirroringTopBottom && (getPageNumber() & 1) == 0) {
    		marginTop = nextMarginBottom;
    		marginBottom = nextMarginTop;
    	}
    	else {
    		marginTop = nextMarginTop;
    		marginBottom = nextMarginBottom;
    	}
    }

    /**
     * Gives the size of a trim, art, crop or bleed box, or null if not defined.
     * @param boxName crop, trim, art or bleed
     */
    Rectangle getBoxSize(String boxName) {
    	PdfRectangle r = (PdfRectangle)thisBoxSize.get(boxName);
    	if (r != null) return r.getRectangle();
    	return null;
    }

//	[U2] empty pages

    /** This checks if the page is empty. */
    private boolean pageEmpty = true;

    void setPageEmpty(boolean pageEmpty) {
        this.pageEmpty = pageEmpty;
    }

    boolean isPageEmpty() {
        return writer == null || (writer.getDirectContent().size() == 0 && writer.getDirectContentUnder().size() == 0 && (pageEmpty || writer.isPaused()));
    }

//	[U3] page actions

    /** The duration of the page */
    protected int duration=-1; // negative values will indicate no duration

    /** The page transition */
    protected PdfTransition transition=null;

    /**
     * Sets the display duration for the page (for presentations)
     * @param seconds   the number of seconds to display the page
     */
    void setDuration(int seconds) {
        if (seconds > 0)
            this.duration=seconds;
        else
            this.duration=-1;
    }

    /**
     * Sets the transition for the page
     * @param transition   the PdfTransition object
     */
    void setTransition(PdfTransition transition) {
        this.transition=transition;
    }

    protected PdfDictionary pageAA = null;
    void setPageAction(PdfName actionType, PdfAction action) {
        if (pageAA == null) {
            pageAA = new PdfDictionary();
        }
        pageAA.put(actionType, action);
    }

//	[U8] thumbnail images

    protected PdfIndirectReference thumb;
    void setThumbnail(Image image) throws PdfException, DocumentException {
        thumb = writer.getImageReference(writer.addDirectImageSimple(image));
    }

//	[M0] Page resources contain references to fonts, extgstate, images,...

    /** This are the page resources of the current Page. */
    protected PageResources pageResources;

    PageResources getPageResources() {
        return pageResources;
    }

//	[M3] Images

    /** Holds value of property strictImageSequence. */
    protected boolean strictImageSequence = false;

    /** Getter for property strictImageSequence.
     * @return Value of property strictImageSequence.
     *
     */
    boolean isStrictImageSequence() {
        return this.strictImageSequence;
    }

    /** Setter for property strictImageSequence.
     * @param strictImageSequence New value of property strictImageSequence.
     *
     */
    void setStrictImageSequence(boolean strictImageSequence) {
        this.strictImageSequence = strictImageSequence;
    }

    /** This is the position where the image ends. */
    protected float imageEnd = -1;

	/**
	 * Method added by Pelikan Stephan
	 */
	public void clearTextWrap() {
		float tmpHeight = imageEnd - currentHeight;
		if (line != null) {
			tmpHeight += line.height();
		}
		if ((imageEnd > -1) && (tmpHeight > 0)) {
			carriageReturn();
			currentHeight += tmpHeight;
		}
	}

    /** This is the image that could not be shown on a previous page. */
    protected Image imageWait = null;

    /**
     * Adds an image to the document.
     * @param image the <CODE>Image</CODE> to add
     * @throws PdfException on error
     * @throws DocumentException on error
     */

    protected void add(Image image) throws PdfException, DocumentException {

        if (image.hasAbsoluteY()) {
            graphics.addImage(image);
            pageEmpty = false;
            return;
        }

        // if there isn't enough room for the image on this page, save it for the next page
        if (currentHeight != 0 && indentTop() - currentHeight - image.getScaledHeight() < indentBottom()) {
            if (!strictImageSequence && imageWait == null) {
                imageWait = image;
                return;
            }
            newPage();
            if (currentHeight != 0 && indentTop() - currentHeight - image.getScaledHeight() < indentBottom()) {
                imageWait = image;
                return;
            }
        }
        pageEmpty = false;
        // avoid endless loops
        if (image == imageWait)
            imageWait = null;
        boolean textwrap = (image.getAlignment() & Image.TEXTWRAP) == Image.TEXTWRAP
        && !((image.getAlignment() & Image.MIDDLE) == Image.MIDDLE);
        boolean underlying = (image.getAlignment() & Image.UNDERLYING) == Image.UNDERLYING;
        float diff = leading / 2;
        if (textwrap) {
            diff += leading;
        }
        float lowerleft = indentTop() - currentHeight - image.getScaledHeight() -diff;
        float mt[] = image.matrix();
        float startPosition = indentLeft() - mt[4];
        if ((image.getAlignment() & Image.RIGHT) == Image.RIGHT) startPosition = indentRight() - image.getScaledWidth() - mt[4];
        if ((image.getAlignment() & Image.MIDDLE) == Image.MIDDLE) startPosition = indentLeft() + ((indentRight() - indentLeft() - image.getScaledWidth()) / 2) - mt[4];
        if (image.hasAbsoluteX()) startPosition = image.getAbsoluteX();
        if (textwrap) {
            if (imageEnd < 0 || imageEnd < currentHeight + image.getScaledHeight() + diff) {
                imageEnd = currentHeight + image.getScaledHeight() + diff;
            }
            if ((image.getAlignment() & Image.RIGHT) == Image.RIGHT) {
            	// indentation suggested by Pelikan Stephan
            	indentation.imageIndentRight += image.getScaledWidth() + image.getIndentationLeft();
            }
            else {
            	// indentation suggested by Pelikan Stephan
            	indentation.imageIndentLeft += image.getScaledWidth() + image.getIndentationRight();
            }
        }
        else {
        	if ((image.getAlignment() & Image.RIGHT) == Image.RIGHT) startPosition -= image.getIndentationRight();
        	else if ((image.getAlignment() & Image.MIDDLE) == Image.MIDDLE) startPosition += image.getIndentationLeft() - image.getIndentationRight();
        	else startPosition += image.getIndentationLeft();
        }
        graphics.addImage(image, mt[0], mt[1], mt[2], mt[3], startPosition, lowerleft - mt[5]);
        if (!(textwrap || underlying)) {
            currentHeight += image.getScaledHeight() + diff;
            flushLines();
            text.moveText(0, - (image.getScaledHeight() + diff));
            newLine();
        }
    }

//	[M4] Adding a PdfPTable

    /** Adds a <CODE>PdfPTable</CODE> to the document.
     * @param ptable the <CODE>PdfPTable</CODE> to be added to the document.
     * @throws DocumentException on error
     */
    void addPTable(PdfPTable ptable) throws DocumentException {
        ColumnText ct = new ColumnText(writer.getDirectContent());
        // if the table prefers to be on a single page, and it wouldn't
        //fit on the current page, start a new page.
        if (ptable.getKeepTogether() && !fitsPage(ptable, 0f) && currentHeight > 0)  {
        	newPage();
        }
        // add dummy paragraph if we aren't at the top of a page, so that
        // spacingBefore will be taken into account by ColumnText
        if (currentHeight > 0) {
	        Paragraph p = new Paragraph();
	        p.setLeading(0);
	        ct.addElement(p);
        }
        ct.addElement(ptable);
        boolean he = ptable.isHeadersInEvent();
        ptable.setHeadersInEvent(true);
        int loop = 0;
        while (true) {
            ct.setSimpleColumn(indentLeft(), indentBottom(), indentRight(), indentTop() - currentHeight);
            int status = ct.go();
            if ((status & ColumnText.NO_MORE_TEXT) != 0) {
                text.moveText(0, ct.getYLine() - indentTop() + currentHeight);
                currentHeight = indentTop() - ct.getYLine();
                break;
            }
            if (indentTop() - currentHeight == ct.getYLine())
                ++loop;
            else
                loop = 0;
            if (loop == 3) {
                add(new Paragraph("ERROR: Infinite table loop"));
                break;
            }
            newPage();
        }
        ptable.setHeadersInEvent(he);
    }

    /**
     * Checks if a <CODE>PdfPTable</CODE> fits the current page of the <CODE>PdfDocument</CODE>.
     *
     * @param	table	the table that has to be checked
     * @param	margin	a certain margin
     * @return	<CODE>true</CODE> if the <CODE>PdfPTable</CODE> fits the page, <CODE>false</CODE> otherwise.
     */

    boolean fitsPage(PdfPTable table, float margin) {
    	if (!table.isLockedWidth()) {
    		float totalWidth = (indentRight() - indentLeft()) * table.getWidthPercentage() / 100;
    		table.setTotalWidth(totalWidth);
    	}
        // ensuring that a new line has been started.
        ensureNewLine();
        return table.getTotalHeight() + ((currentHeight > 0) ? table.spacingBefore() : 0f)
        	<= indentTop() - currentHeight - indentBottom() - margin;
    }

//	[M4'] Adding a Table

	/**
	 * This is a helper class for adding a Table to a document.
	 * @since	2.0.8 (PdfDocument was package-private before)
	 */
    protected static class RenderingContext {
        float pagetop = -1;
        float oldHeight = -1;

        PdfContentByte cellGraphics = null;

        float lostTableBottom;

        float maxCellBottom;
        float maxCellHeight;

        Map rowspanMap;
        Map pageMap = new HashMap();
        /**
         * A PdfPTable
         */
        public PdfTable table;

        /**
         * Consumes the rowspan
         * @param c
         * @return a rowspan.
         */
        public int consumeRowspan(PdfCell c) {
            if (c.rowspan() == 1) {
                return 1;
            }

            Integer i = (Integer) rowspanMap.get(c);
            if (i == null) {
                i = new Integer(c.rowspan());
            }

            i = new Integer(i.intValue() - 1);
            rowspanMap.put(c, i);

            if (i.intValue() < 1) {
                return 1;
            }
            return i.intValue();
        }

        /**
         * Looks at the current rowspan.
         * @param c
         * @return the current rowspan
         */
        public int currentRowspan(PdfCell c) {
            Integer i = (Integer) rowspanMap.get(c);
            if (i == null) {
                return c.rowspan();
            } else {
                return i.intValue();
            }
        }

        public int cellRendered(PdfCell cell, int pageNumber) {
            Integer i = (Integer) pageMap.get(cell);
            if (i == null) {
                i = new Integer(1);
            } else {
                i = new Integer(i.intValue() + 1);
            }
            pageMap.put(cell, i);

            Integer pageInteger = new Integer(pageNumber);
            Set set = (Set) pageMap.get(pageInteger);

            if (set == null) {
                set = new HashSet();
                pageMap.put(pageInteger, set);
            }

            set.add(cell);

            return i.intValue();
        }

        public int numCellRendered(PdfCell cell) {
            Integer i = (Integer) pageMap.get(cell);
            if (i == null) {
                i = new Integer(0);
            }
            return i.intValue();
        }

        public boolean isCellRenderedOnPage(PdfCell cell, int pageNumber) {
            Integer pageInteger = new Integer(pageNumber);
            Set set = (Set) pageMap.get(pageInteger);

            if (set != null) {
                return set.contains(cell);
            }

            return false;
        }
    };

	/**
	 * Adds a new table to the document.
	 * @param t				Table to add.  Rendered rows will be deleted after processing.
	 * @throws DocumentException
	 * @since	iText 2.0.8
	 */
    private void addPdfTable(Table t) throws DocumentException {
        // before every table, we flush all lines
        flushLines();

    	PdfTable table = new PdfTable(t, indentLeft(), indentRight(), indentTop() - currentHeight);
        RenderingContext ctx = new RenderingContext();
        ctx.pagetop = indentTop();
        ctx.oldHeight = currentHeight;
        ctx.cellGraphics = new PdfContentByte(writer);
        ctx.rowspanMap = new HashMap();
        ctx.table = table;

		// initialization of parameters
		PdfCell cell;

		// drawing the table
		ArrayList headercells = table.getHeaderCells();
        ArrayList cells = table.getCells();
        ArrayList rows = extractRows(cells, ctx);
        boolean isContinue = false;
		while (!cells.isEmpty()) {
			// initialization of some extra parameters;
			ctx.lostTableBottom = 0;

			// loop over the cells
			boolean cellsShown = false;

            // draw the cells (line by line)
            Iterator iterator = rows.iterator();

            boolean atLeastOneFits = false;
            while (iterator.hasNext()) {
                ArrayList row = (ArrayList) iterator.next();
                analyzeRow(rows, ctx);
                renderCells(ctx, row, table.hasToFitPageCells() & atLeastOneFits);

                if (!mayBeRemoved(row)) {
                    break;
                }
                consumeRowspan(row, ctx);
                iterator.remove();
                atLeastOneFits = true;
            }

//          compose cells array list for subsequent code
            cells.clear();
            Set opt = new HashSet();
            iterator = rows.iterator();
            while (iterator.hasNext()) {
                ArrayList row = (ArrayList) iterator.next();

                Iterator cellIterator = row.iterator();
                while (cellIterator.hasNext()) {
                    cell = (PdfCell) cellIterator.next();

                    if (!opt.contains(cell)) {
                        cells.add(cell);
                        opt.add(cell);
                    }
                }
            }

			// we paint the graphics of the table after looping through all the cells
			Rectangle tablerec = new Rectangle(table);
			tablerec.setBorder(table.getBorder());
			tablerec.setBorderWidth(table.getBorderWidth());
			tablerec.setBorderColor(table.getBorderColor());
			tablerec.setBackgroundColor(table.getBackgroundColor());
			PdfContentByte under = writer.getDirectContentUnder();
			under.rectangle(tablerec.rectangle(top(), indentBottom()));
			under.add(ctx.cellGraphics);
			// bugfix by Gerald Fehringer: now again add the border for the table
			// since it might have been covered by cell backgrounds
			tablerec.setBackgroundColor(null);
			tablerec = tablerec.rectangle(top(), indentBottom());
			tablerec.setBorder(table.getBorder());
			under.rectangle(tablerec);
			// end bugfix

            ctx.cellGraphics = new PdfContentByte(null);
			// if the table continues on the next page

			if (!rows.isEmpty()) {
				isContinue = true;
				graphics.setLineWidth(table.getBorderWidth());
				if (cellsShown && (table.getBorder() & Rectangle.BOTTOM) == Rectangle.BOTTOM) {
					// Draw the bottom line

					// the color is set to the color of the element
					Color tColor = table.getBorderColor();
					if (tColor != null) {
						graphics.setColorStroke(tColor);
					}
					graphics.moveTo(table.getLeft(), Math.max(table.getBottom(), indentBottom()));
					graphics.lineTo(table.getRight(), Math.max(table.getBottom(), indentBottom()));
					graphics.stroke();
					if (tColor != null) {
						graphics.resetRGBColorStroke();
					}
				}

				// old page
				pageEmpty = false;
                float difference = ctx.lostTableBottom;

				// new page
				newPage();

				// G.F.: if something added in page event i.e. currentHeight > 0
				float heightCorrection = 0;
				boolean somethingAdded = false;
				if (currentHeight > 0) {
					heightCorrection = 6;
					currentHeight += heightCorrection;
					somethingAdded = true;
					newLine();
					flushLines();
					indentation.indentTop = currentHeight - leading;
					currentHeight = 0;
				}
				else {
                    flushLines();
				}

				// this part repeats the table headers (if any)
				int size = headercells.size();
				if (size > 0) {
					// this is the top of the headersection
					cell = (PdfCell) headercells.get(0);
					float oldTop = cell.getTop(0);
					// loop over all the cells of the table header
					for (int i = 0; i < size; i++) {
						cell = (PdfCell) headercells.get(i);
						// calculation of the new cellpositions
						cell.setTop(indentTop() - oldTop + cell.getTop(0));
						cell.setBottom(indentTop() - oldTop + cell.getBottom(0));
						ctx.pagetop = cell.getBottom();
						// we paint the borders of the cell
						ctx.cellGraphics.rectangle(cell.rectangle(indentTop(), indentBottom()));
						// we write the text of the cell
						ArrayList images = cell.getImages(indentTop(), indentBottom());
						for (Iterator im = images.iterator(); im.hasNext();) {
							cellsShown = true;
							Image image = (Image) im.next();
							graphics.addImage(image);
						}
						lines = cell.getLines(indentTop(), indentBottom());
						float cellTop = cell.getTop(indentTop());
						text.moveText(0, cellTop-heightCorrection);
						float cellDisplacement = flushLines() - cellTop+heightCorrection;
						text.moveText(0, cellDisplacement);
					}

					currentHeight = indentTop() - ctx.pagetop + table.cellspacing();
					text.moveText(0, ctx.pagetop - indentTop() - currentHeight);
				}
				else {
					if (somethingAdded) {
						ctx.pagetop = indentTop();
						text.moveText(0, -table.cellspacing());
					}
				}
				ctx.oldHeight = currentHeight - heightCorrection;

				// calculating the new positions of the table and the cells
				size = Math.min(cells.size(), table.columns());
				int i = 0;
				while (i < size) {
					cell = (PdfCell) cells.get(i);
					if (cell.getTop(-table.cellspacing()) > ctx.lostTableBottom) {
						float newBottom = ctx.pagetop - difference + cell.getBottom();
						float neededHeight = cell.remainingHeight();
						if (newBottom > ctx.pagetop - neededHeight) {
							difference += newBottom - (ctx.pagetop - neededHeight);
						}
					}
					i++;
				}
				size = cells.size();
				table.setTop(indentTop());
				table.setBottom(ctx.pagetop - difference + table.getBottom(table.cellspacing()));
				for (i = 0; i < size; i++) {
					cell = (PdfCell) cells.get(i);
					float newBottom = ctx.pagetop - difference + cell.getBottom();
					float newTop = ctx.pagetop - difference + cell.getTop(-table.cellspacing());
					if (newTop > indentTop() - currentHeight) {
						newTop = indentTop() - currentHeight;
					}

					cell.setTop(newTop );
					cell.setBottom(newBottom );
				}
			}
		}

        float tableHeight = table.getTop() - table.getBottom();
        // bugfix by Adauto Martins when have more than two tables and more than one page
        // If continuation of table in other page (bug report #1460051)
        if (isContinue) {
        	currentHeight = tableHeight;
        	text.moveText(0, -(tableHeight - (ctx.oldHeight * 2)));
        } else {
        	currentHeight = ctx.oldHeight + tableHeight;
        	text.moveText(0, -tableHeight);
        }
        // end bugfix
        pageEmpty = false;
    }

    protected void analyzeRow(ArrayList rows, RenderingContext ctx) {
        ctx.maxCellBottom = indentBottom();

        // determine whether row(index) is in a rowspan
        int rowIndex = 0;

        ArrayList row = (ArrayList) rows.get(rowIndex);
        int maxRowspan = 1;
        Iterator iterator = row.iterator();
        while (iterator.hasNext()) {
            PdfCell cell = (PdfCell) iterator.next();
            maxRowspan = Math.max(ctx.currentRowspan(cell), maxRowspan);
        }
        rowIndex += maxRowspan;

        boolean useTop = true;
        if (rowIndex == rows.size()) {
            rowIndex = rows.size() - 1;
            useTop = false;
        }

        if (rowIndex < 0 || rowIndex >= rows.size()) return;

        row = (ArrayList) rows.get(rowIndex);
        iterator = row.iterator();
        while (iterator.hasNext()) {
            PdfCell cell = (PdfCell) iterator.next();
            Rectangle cellRect = cell.rectangle(ctx.pagetop, indentBottom());
            if (useTop) {
                ctx.maxCellBottom = Math.max(ctx.maxCellBottom, cellRect.getTop());
            } else {
                if (ctx.currentRowspan(cell) == 1) {
                    ctx.maxCellBottom = Math.max(ctx.maxCellBottom, cellRect.getBottom());
                }
            }
        }
    }

    protected boolean mayBeRemoved(ArrayList row) {
        Iterator iterator = row.iterator();
        boolean mayBeRemoved = true;
        while (iterator.hasNext()) {
            PdfCell cell = (PdfCell) iterator.next();

            mayBeRemoved &= cell.mayBeRemoved();
        }
        return mayBeRemoved;
    }

    protected void consumeRowspan(ArrayList row, RenderingContext ctx) {
        Iterator iterator = row.iterator();
        while (iterator.hasNext()) {
            PdfCell c = (PdfCell) iterator.next();
            ctx.consumeRowspan(c);
        }
    }

    protected ArrayList extractRows(ArrayList cells, RenderingContext ctx) {
        PdfCell cell;
        PdfCell previousCell = null;
        ArrayList rows = new ArrayList();
        java.util.List rowCells = new ArrayList();

        Iterator iterator = cells.iterator();
        while (iterator.hasNext()) {
            cell = (PdfCell) iterator.next();

            boolean isAdded = false;

            boolean isEndOfRow = !iterator.hasNext();
            boolean isCurrentCellPartOfRow = !iterator.hasNext();

            if (previousCell != null) {
                if (cell.getLeft() <= previousCell.getLeft()) {
                    isEndOfRow = true;
                    isCurrentCellPartOfRow = false;
                }
            }

            if (isCurrentCellPartOfRow) {
                rowCells.add(cell);
                isAdded = true;
            }

            if (isEndOfRow) {
                if (!rowCells.isEmpty()) {
                    // add to rowlist
                    rows.add(rowCells);
                }

                // start a new list for next line
                rowCells = new ArrayList();
            }

            if (!isAdded) {
                rowCells.add(cell);
            }

            previousCell = cell;
        }

        if (!rowCells.isEmpty()) {
            rows.add(rowCells);
        }

        // fill row information with rowspan cells to get complete "scan lines"
        for (int i = rows.size() - 1; i >= 0; i--) {
            ArrayList row = (ArrayList) rows.get(i);
            // iterator through row
            for (int j = 0; j < row.size(); j++) {
                PdfCell c = (PdfCell) row.get(j);
                int rowspan = c.rowspan();
                // fill in missing rowspan cells to complete "scan line"
                for (int k = 1; k < rowspan && rows.size() < i+k; k++) {
                    ArrayList spannedRow = ((ArrayList) rows.get(i + k));
                    if (spannedRow.size() > j)
                    	spannedRow.add(j, c);
                }
            }
        }

        return rows;
    }

    protected void renderCells(RenderingContext ctx, java.util.List cells, boolean hasToFit) throws DocumentException {
        PdfCell cell;
        Iterator iterator;
        if (hasToFit) {
            iterator = cells.iterator();
            while (iterator.hasNext()) {
            	cell = (PdfCell) iterator.next();
            	if (!cell.isHeader()) {
            		if (cell.getBottom() < indentBottom()) return;
            	}
            }
        }
        iterator = cells.iterator();

        while (iterator.hasNext()) {
            cell = (PdfCell) iterator.next();
            if (!ctx.isCellRenderedOnPage(cell, getPageNumber())) {

                float correction = 0;
                if (ctx.numCellRendered(cell) >= 1) {
                    correction = 1.0f;
                }

                lines = cell.getLines(ctx.pagetop, indentBottom() - correction);

                // if there is still text to render we render it
                if (lines != null && !lines.isEmpty()) {
                    // we write the text
                    float cellTop = cell.getTop(ctx.pagetop - ctx.oldHeight);
                    text.moveText(0, cellTop);
                    float cellDisplacement = flushLines() - cellTop;

                    text.moveText(0, cellDisplacement);
                    if (ctx.oldHeight + cellDisplacement > currentHeight) {
                        currentHeight = ctx.oldHeight + cellDisplacement;
                    }

                    ctx.cellRendered(cell, getPageNumber());
                }
                float indentBottom = Math.max(cell.getBottom(), indentBottom());
                Rectangle tableRect = ctx.table.rectangle(ctx.pagetop, indentBottom());
                indentBottom = Math.max(tableRect.getBottom(), indentBottom);

                // we paint the borders of the cells
                Rectangle cellRect = cell.rectangle(tableRect.getTop(), indentBottom);
 				//cellRect.setBottom(cellRect.bottom());
                if (cellRect.getHeight() > 0) {
                    ctx.lostTableBottom = indentBottom;
                    ctx.cellGraphics.rectangle(cellRect);
                }

                // and additional graphics
                ArrayList images = cell.getImages(ctx.pagetop, indentBottom());
                for (Iterator i = images.iterator(); i.hasNext();) {
                    Image image = (Image) i.next();
                    graphics.addImage(image);
                }

            }
        }
    }

    /**
     * Returns the bottomvalue of a <CODE>Table</CODE> if it were added to this document.
     *
     * @param	table	the table that may or may not be added to this document
     * @return	a bottom value
     */
    float bottom(Table table) {
        // constructing a PdfTable
        PdfTable tmp = new PdfTable(table, indentLeft(), indentRight(), indentTop() - currentHeight);
        return tmp.getBottom();
    }

//	[M5] header/footer
    protected void doFooter() throws DocumentException {
    	if (footer == null) return;
		// Begin added by Edgar Leonardo Prieto Perilla
    	// Avoid footer indentation
    	float tmpIndentLeft = indentation.indentLeft;
    	float tmpIndentRight = indentation.indentRight;
    	// Begin added: Bonf (Marc Schneider) 2003-07-29
        float tmpListIndentLeft = indentation.listIndentLeft;
        float tmpImageIndentLeft = indentation.imageIndentLeft;
        float tmpImageIndentRight = indentation.imageIndentRight;
        // End added: Bonf (Marc Schneider) 2003-07-29

        indentation.indentLeft = indentation.indentRight = 0;
        // Begin added: Bonf (Marc Schneider) 2003-07-29
        indentation.listIndentLeft = 0;
        indentation.imageIndentLeft = 0;
        indentation.imageIndentRight = 0;
        // End added: Bonf (Marc Schneider) 2003-07-29
        // End Added by Edgar Leonardo Prieto Perilla
        footer.setPageNumber(pageN);
        leading = footer.paragraph().getTotalLeading();
        add(footer.paragraph());
        // adding the footer limits the height
        indentation.indentBottom = currentHeight;
        text.moveText(left(), indentBottom());
        flushLines();
        text.moveText(-left(), -bottom());
        footer.setTop(bottom(currentHeight));
        footer.setBottom(bottom() - (0.75f * leading));
        footer.setLeft(left());
        footer.setRight(right());
        graphics.rectangle(footer);
        indentation.indentBottom = currentHeight + leading * 2;
        currentHeight = 0;
        // Begin added by Edgar Leonardo Prieto Perilla
        indentation.indentLeft = tmpIndentLeft;
        indentation.indentRight = tmpIndentRight;
        // Begin added: Bonf (Marc Schneider) 2003-07-29
        indentation.listIndentLeft = tmpListIndentLeft;
        indentation.imageIndentLeft = tmpImageIndentLeft;
        indentation.imageIndentRight = tmpImageIndentRight;
        // End added: Bonf (Marc Schneider) 2003-07-29
        // End added by Edgar Leonardo Prieto Perilla
    }

    protected void doHeader() throws DocumentException {
        // if there is a header, the header = added
        if (header == null) return;
		// Begin added by Edgar Leonardo Prieto Perilla
		// Avoid header indentation
		float tmpIndentLeft = indentation.indentLeft;
		float tmpIndentRight = indentation.indentRight;
        // Begin added: Bonf (Marc Schneider) 2003-07-29
        float tmpListIndentLeft = indentation.listIndentLeft;
        float tmpImageIndentLeft = indentation.imageIndentLeft;
        float tmpImageIndentRight = indentation.imageIndentRight;
        // End added: Bonf (Marc Schneider) 2003-07-29
        indentation.indentLeft = indentation.indentRight = 0;
        //  Added: Bonf
        indentation.listIndentLeft = 0;
        indentation.imageIndentLeft = 0;
        indentation.imageIndentRight = 0;
        // End added: Bonf
        // Begin added by Edgar Leonardo Prieto Perilla
		header.setPageNumber(pageN);
        leading = header.paragraph().getTotalLeading();
        text.moveText(0, leading);
        add(header.paragraph());
        newLine();
        indentation.indentTop = currentHeight - leading;
        header.setTop(top() + leading);
        header.setBottom(indentTop() + leading * 2 / 3);
        header.setLeft(left());
        header.setRight(right());
        graphics.rectangle(header);
        flushLines();
        currentHeight = 0;
        // Begin added by Edgar Leonardo Prieto Perilla
        // Restore indentation
		indentation.indentLeft = tmpIndentLeft;
		indentation.indentRight = tmpIndentRight;
        // Begin added: Bonf (Marc Schneider) 2003-07-29
        indentation.listIndentLeft = tmpListIndentLeft;
        indentation.imageIndentLeft = tmpImageIndentLeft;
        indentation.imageIndentRight = tmpImageIndentRight;
        // End added: Bonf (Marc Schneider) 2003-07-29
		// End Added by Edgar Leonardo Prieto Perilla
    }
}
