/*
 * $Id: RtfWriter2.java 4065 2009-09-16 23:09:11Z psoares33 $
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

package com.lowagie.text.rtf;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.EventListener;
import com.lowagie.text.error_messages.MessageLocalization;

import com.lowagie.text.DocWriter;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.HeaderFooter;
import com.lowagie.text.Rectangle;
import com.lowagie.text.rtf.document.RtfDocument;
import com.lowagie.text.rtf.document.RtfDocumentSettings;
import com.lowagie.text.rtf.parser.RtfImportMappings;
import com.lowagie.text.rtf.parser.RtfParser;
import com.lowagie.text.rtf.text.RtfNewPage;

/**
 * The RtfWriter allows the creation of rtf documents via the iText system
 *
 * Version: $Id: RtfWriter2.java 4065 2009-09-16 23:09:11Z psoares33 $
 * @author Mark Hall (Mark.Hall@mail.room3b.eu)
 */
public class RtfWriter2 extends DocWriter {
    /**
     * The RtfDocument this RtfWriter is creating
     */
    private RtfDocument rtfDoc = null;
    
    /**
     * Constructs a new RtfWriter that listens to the specified Document and
     * writes its output to the OutputStream.
     * 
     * @param doc The Document that this RtfWriter listens to
     * @param os The OutputStream to write to
     */
    protected RtfWriter2(Document doc, OutputStream os) {
        super(doc, os);
        doc.addDocListener(this);
        rtfDoc = new RtfDocument();
    }

    /**
     * Static method to generate RtfWriters
     * 
     * @param doc The Document that this RtfWriter listens to
     * @param os The OutputStream to write to
     * @return The new RtfWriter
     */
    public static RtfWriter2 getInstance(Document doc, OutputStream os) {
        return new RtfWriter2(doc, os);
    }

    /**
     * Sets the header to use
     * 
     * @param hf The HeaderFooter to use
     */
    public void setHeader(HeaderFooter hf) {
        this.rtfDoc.getDocumentHeader().setHeader(hf);
    }
    
    /**
     * Resets the header
     */
    public void resetHeader() {
        this.rtfDoc.getDocumentHeader().setHeader(null);
    }
    
    /**
     * Sets the footer to use
     * 
     * @param hf The HeaderFooter to use
     */
    public void setFooter(HeaderFooter hf) {
        this.rtfDoc.getDocumentHeader().setFooter(hf);
    }
    
    /**
     * Resets the footer
     */
    public void resetFooter() {
        this.rtfDoc.getDocumentHeader().setFooter(null);
    }

    /**
     * This method is not supported in the RtfWriter
     * @param i Unused
     */
    public void setPageCount(int i) {}
    
    /**
     * This method is not supported in the RtfWriter
     */
    public void resetPageCount() {}

    /**
     * This method is not supported in the RtfWriter
     */
    public void clearTextWrap() {}

    /**
     * Opens the RtfDocument
     */
    public void open() {
    	super.open();
        this.rtfDoc.open();
    }
    
    /**
     * Closes the RtfDocument. This causes the document to be written
     * to the specified OutputStream
     */
    public void close() {
        if (open) {
            rtfDoc.writeDocument(os);
            super.close();
            this.rtfDoc = new RtfDocument();
        }
    }

    /**
     * Adds an Element to the Document
     *
     * @param element The element to be added
     * @return <code>false</code>
     * @throws DocumentException
     */
    public boolean add(Element element) throws DocumentException {
        if (pause) {
            return false;
        }
        RtfBasicElement[] rtfElements = rtfDoc.getMapper().mapElement(element);
        if(rtfElements.length != 0) {
            for(int i = 0; i < rtfElements.length; i++) {
                if(rtfElements[i] != null) {
                    rtfDoc.add(rtfElements[i]);
                }
            }
            return true;
        } else {
            return false;
        }
    }
    
    /**
     * Adds a page break
     *
     * @return <code>false</code>
     */
    public boolean newPage() {
        rtfDoc.add(new RtfNewPage(rtfDoc));
        return true;
    }

    /**
     * Sets the page margins
     *
     * @param left The left margin
     * @param right The right margin
     * @param top The top margin
     * @param bottom The bottom margin
     * @return <code>false</code>
     */
    public boolean setMargins(float left, float right, float top, float bottom) {
        rtfDoc.getDocumentHeader().getPageSetting().setMarginLeft((int) (left * RtfElement.TWIPS_FACTOR));
        rtfDoc.getDocumentHeader().getPageSetting().setMarginRight((int) (right * RtfElement.TWIPS_FACTOR));
        rtfDoc.getDocumentHeader().getPageSetting().setMarginTop((int) (top * RtfElement.TWIPS_FACTOR));
        rtfDoc.getDocumentHeader().getPageSetting().setMarginBottom((int) (bottom * RtfElement.TWIPS_FACTOR));
        return true;
    }
    
    /**
     * Sets the size of the page
     *
     * @param rect A Rectangle representing the page
     * @return <code>false</code>
     */
    public boolean setPageSize(Rectangle rect) {
        rtfDoc.getDocumentHeader().getPageSetting().setPageSize(rect);
        return true;
    }
    
    /**
     * Whether to automagically generate table of contents entries when
     * adding Chapters or Sections.
     * 
     * @param autogenerate Whether to automatically generate TOC entries
     */
    public void setAutogenerateTOCEntries(boolean autogenerate) {
        this.rtfDoc.setAutogenerateTOCEntries(autogenerate);
    }
    
    /**
     * Gets the RtfDocumentSettings that specify how the rtf document is generated.
     * 
     * @return The current RtfDocumentSettings.
     */
    public RtfDocumentSettings getDocumentSettings() {
        return this.rtfDoc.getDocumentSettings();
    }
    
    /**
     * Adds the complete RTF document to the current RTF document being generated.
     * It will parse the font and color tables and correct the font and color references
     * so that the imported RTF document retains its formattings.
     * 
     * @param documentSource The Reader to read the RTF document from.
     * @throws IOException On errors reading the RTF document.
     * @throws DocumentException On errors adding to this RTF document.
     * @since 2.1.0
     */
    public void importRtfDocument(FileInputStream documentSource) throws IOException, DocumentException {
        importRtfDocument(documentSource, null);
    }
 
    /**
     * Adds the complete RTF document to the current RTF document being generated.
     * It will parse the font and color tables and correct the font and color references
     * so that the imported RTF document retains its formattings.
     * Uses new RtfParser object.
     * 
     * (author: Howard Shank)
     * 
     * @param documentSource The InputStream to read the RTF document from.
	 * @param events The array of event listeners. May be null
     * @throws IOException
     * @throws DocumentException
     * 
     * @see RtfParser
     * @see RtfParser#importRtfDocument(InputStream, RtfDocument)
     * @since 2.0.8
     */
    public void importRtfDocument(InputStream documentSource, EventListener[] events ) throws IOException, DocumentException {
        if(!this.open) {
            throw new DocumentException(MessageLocalization.getComposedMessage("the.document.must.be.open.to.import.rtf.documents"));
        }
    	RtfParser rtfImport = new RtfParser(this.document);
    	if(events != null) {
    		for(int idx=0;idx<events.length;idx++) {
        		rtfImport.addListener(events[idx]);
    		}
    	}
    	rtfImport.importRtfDocument(documentSource, this.rtfDoc);
    }
    
    /**
     * Adds a fragment of an RTF document to the current RTF document being generated.
     * Since this fragment doesn't contain font or color tables, all fonts and colors
     * are mapped to the default font and color. If the font and color mappings are
     * known, they can be specified via the mappings parameter.
     * 
     * @param documentSource The InputStream to read the RTF fragment from.
     * @param mappings The RtfImportMappings that contain font and color mappings to apply to the fragment.
     * @throws IOException On errors reading the RTF fragment.
     * @throws DocumentException On errors adding to this RTF fragment.
     * @since 2.1.0
     */
    public void importRtfFragment(InputStream documentSource, RtfImportMappings mappings) throws IOException, DocumentException {
        importRtfFragment(documentSource, mappings, null);
    }
    
    /**
     * Adds a fragment of an RTF document to the current RTF document being generated.
     * Since this fragment doesn't contain font or color tables, all fonts and colors
     * are mapped to the default font and color. If the font and color mappings are
     * known, they can be specified via the mappings parameter.
     * Uses new RtfParser object.
     * 
     * (author: Howard Shank)
     * 
     * @param documentSource The InputStream to read the RTF fragment from.
     * @param mappings The RtfImportMappings that contain font and color mappings to apply to the fragment.
	 * @param events The array of event listeners. May be null
     * @throws IOException On errors reading the RTF fragment.
     * @throws DocumentException On errors adding to this RTF fragment.
     * 
     * @see RtfImportMappings
     * @see RtfParser
     * @see RtfParser#importRtfFragment(InputStream, RtfDocument, RtfImportMappings)
     * @since 2.0.8
     */
    public void importRtfFragment(InputStream documentSource, RtfImportMappings mappings, EventListener[] events ) throws IOException, DocumentException {
        if(!this.open) {
            throw new DocumentException(MessageLocalization.getComposedMessage("the.document.must.be.open.to.import.rtf.fragments"));
        }
    	RtfParser rtfImport = new RtfParser(this.document);
    	if(events != null) {
    		for(int idx=0;idx<events.length;idx++) {
        		rtfImport.addListener(events[idx]);
    		}
    	}
    	rtfImport.importRtfFragment(documentSource, this.rtfDoc, mappings);
    }
    
    
    /**
     * Adds the complete RTF document to the current RTF element being generated.
     * It will parse the font and color tables and correct the font and color references
     * so that the imported RTF document retains its formattings.
     * 
     * @param elem The Element the RTF document is to be imported into.
     * @param documentSource The Reader to read the RTF document from.
     * @throws IOException On errors reading the RTF document.
     * @throws DocumentException On errors adding to this RTF document.
     * @since 2.1.4
     */
    public void importRtfDocumentIntoElement(Element elem, FileInputStream documentSource) throws IOException, DocumentException {
    	importRtfDocumentIntoElement(elem, documentSource, null);
    }
    
    /**
     * Adds the complete RTF document to the current RTF element being generated.
     * It will parse the font and color tables and correct the font and color references
     * so that the imported RTF document retains its formattings.
     * 
     * @param elem The Element the RTF document is to be imported into.
     * @param documentSource The Reader to read the RTF document from.
     * @param events The event array for listeners.
     * @throws IOException On errors reading the RTF document.
     * @throws DocumentException On errors adding to this RTF document.
     * @since 2.1.4
     */
    public void importRtfDocumentIntoElement(Element elem, FileInputStream documentSource, EventListener[] events) throws IOException, DocumentException {

    	RtfParser rtfImport = new RtfParser(this.document);
    	if(events != null) {
    		for(int idx=0;idx<events.length;idx++) {
        		rtfImport.addListener(events[idx]);
    		}
    	}
    	rtfImport.importRtfDocumentIntoElement(elem, documentSource, rtfDoc);
    }
}
