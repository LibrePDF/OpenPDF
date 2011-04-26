/*
 * $Id: XmpWriter.java 3949 2009-06-03 15:19:04Z blowagie $
 *
 * Copyright 2005 by Bruno Lowagie.
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
 * the Initial Developer are Copyright (C) 1999-2005 by Bruno Lowagie.
 * All Rights Reserved.
 * Co-Developer of the code is Paulo Soares. Portions created by the Co-Developer
 * are Copyright (C) 2000-2005 by Paulo Soares. All Rights Reserved.
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

package com.lowagie.text.xml.xmp;

import com.lowagie.text.pdf.PdfWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Iterator;
import java.util.Map;

import com.lowagie.text.pdf.PdfDate;
import com.lowagie.text.pdf.PdfDictionary;
import com.lowagie.text.pdf.PdfName;
import com.lowagie.text.pdf.PdfObject;
import com.lowagie.text.pdf.PdfString;

/**
 * With this class you can create an Xmp Stream that can be used for adding
 * Metadata to a PDF Dictionary. Remark that this class doesn't cover the
 * complete XMP specification. 
 */
public class XmpWriter {

	/** A possible charset for the XMP. */
	public static final String UTF8 = "UTF-8";
	/** A possible charset for the XMP. */
	public static final String UTF16 = "UTF-16";
	/** A possible charset for the XMP. */
	public static final String UTF16BE = "UTF-16BE";
	/** A possible charset for the XMP. */
	public static final String UTF16LE = "UTF-16LE";
	
	/** String used to fill the extra space. */
	public static final String EXTRASPACE = "                                                                                                   \n";
	
	/** You can add some extra space in the XMP packet; 1 unit in this variable represents 100 spaces and a newline. */
	protected int extraSpace;
	
	/** The writer to which you can write bytes for the XMP stream. */
	protected OutputStreamWriter writer;
	
	/** The about string that goes into the rdf:Description tags. */
	protected String about;

	/**
	 * Processing Instruction required at the start of an XMP stream
	 * @since iText 2.1.6
	 */
	public static final String XPACKET_PI_BEGIN = "<?xpacket begin=\"\uFEFF\" id=\"W5M0MpCehiHzreSzNTczkc9d\"?>\n";
	
	/**
	 * Processing Instruction required at the end of an XMP stream for XMP streams that can be updated
	 * @since iText 2.1.6
	 */
	public static final String XPACKET_PI_END_W = "<?xpacket end=\"w\"?>";
	
	/**
	 * Processing Instruction required at the end of an XMP stream for XMP streams that are read only
	 * @since iText 2.1.6
	 */
	public static final String XPACKET_PI_END_R = "<?xpacket end=\"r\"?>";
	
	/** The end attribute. */
	protected char end = 'w';
	
	/**
	 * Creates an XmpWriter. 
	 * @param os
	 * @param utfEncoding
	 * @param extraSpace
	 * @throws IOException
	 */
	public XmpWriter(OutputStream os, String utfEncoding, int extraSpace) throws IOException {
		this.extraSpace = extraSpace;
		writer = new OutputStreamWriter(os, utfEncoding);
		writer.write(XPACKET_PI_BEGIN);
		writer.write("<x:xmpmeta xmlns:x=\"adobe:ns:meta/\">\n");
		writer.write("<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\">\n");
		about = "";
	}
	
	/**
	 * Creates an XmpWriter.
	 * @param os
	 * @throws IOException
	 */
	public XmpWriter(OutputStream os) throws IOException {
		this(os, UTF8, 20);
	}
	
	/** Sets the XMP to read-only */
	public void setReadOnly() {
		end = 'r';
	}
	
	/**
	 * @param about The about to set.
	 */
	public void setAbout(String about) {
		this.about = about;
	}
	
	/**
	 * Adds an rdf:Description.
	 * @param xmlns
	 * @param content
	 * @throws IOException
	 */
	public void addRdfDescription(String xmlns, String content) throws IOException {
		writer.write("<rdf:Description rdf:about=\"");
		writer.write(about);
		writer.write("\" ");
		writer.write(xmlns);
		writer.write(">");
		writer.write(content);
		writer.write("</rdf:Description>\n");
	}
	
	/**
	 * Adds an rdf:Description.
	 * @param s
	 * @throws IOException
	 */
	public void addRdfDescription(XmpSchema s) throws IOException {
		writer.write("<rdf:Description rdf:about=\"");
		writer.write(about);
		writer.write("\" ");
		writer.write(s.getXmlns());
		writer.write(">");
		writer.write(s.toString());
		writer.write("</rdf:Description>\n");
	}
	
	/**
	 * Flushes and closes the XmpWriter.
	 * @throws IOException
	 */
	public void close() throws IOException {
		writer.write("</rdf:RDF>");
		writer.write("</x:xmpmeta>\n");
		for (int i = 0; i < extraSpace; i++) {
			writer.write(EXTRASPACE);
		}
		writer.write(end == 'r' ? XPACKET_PI_END_R : XPACKET_PI_END_W);
		writer.flush();
		writer.close();
	}
    
    /**
     * @param os
     * @param info
     * @throws IOException
     */
    public XmpWriter(OutputStream os, PdfDictionary info, int PdfXConformance) throws IOException {
        this(os);
        if (info != null) {
        	DublinCoreSchema dc = new DublinCoreSchema();
        	PdfSchema p = new PdfSchema();
        	XmpBasicSchema basic = new XmpBasicSchema();
        	PdfName key;
        	PdfObject obj;
        	for (Iterator it = info.getKeys().iterator(); it.hasNext();) {
        		key = (PdfName)it.next();
        		obj = info.get(key);
        		if (obj == null)
        			continue;
        		if (PdfName.TITLE.equals(key)) {
        			dc.addTitle(((PdfString)obj).toUnicodeString());
        		}
        		if (PdfName.AUTHOR.equals(key)) {
        			dc.addAuthor(((PdfString)obj).toUnicodeString());
        		}
        		if (PdfName.SUBJECT.equals(key)) {
        			dc.addSubject(((PdfString)obj).toUnicodeString());
        			dc.addDescription(((PdfString)obj).toUnicodeString());
        		}
        		if (PdfName.KEYWORDS.equals(key)) {
        			p.addKeywords(((PdfString)obj).toUnicodeString());
        		}
        		if (PdfName.CREATOR.equals(key)) {
        			basic.addCreatorTool(((PdfString)obj).toUnicodeString());
        		}
        		if (PdfName.PRODUCER.equals(key)) {
        			p.addProducer(((PdfString)obj).toUnicodeString());
        		}
        		if (PdfName.CREATIONDATE.equals(key)) {
        			basic.addCreateDate(((PdfDate)obj).getW3CDate());
        		}
        		if (PdfName.MODDATE.equals(key)) {
        			basic.addModDate(((PdfDate)obj).getW3CDate());
        		}
        	}
        	if (dc.size() > 0) addRdfDescription(dc);
        	if (p.size() > 0) addRdfDescription(p);
        	if (basic.size() > 0) addRdfDescription(basic);
            if (PdfXConformance == PdfWriter.PDFA1A || PdfXConformance == PdfWriter.PDFA1B) {
                PdfA1Schema a1 = new PdfA1Schema();
                if (PdfXConformance == PdfWriter.PDFA1A)
                    a1.addConformance("A");
                else
                    a1.addConformance("B");
                addRdfDescription(a1);
            }
        }
    }
    
    /**
     * @param os
     * @param info
     * @throws IOException
     */
    public XmpWriter(OutputStream os, Map info) throws IOException {
        this(os);
        if (info != null) {
        	DublinCoreSchema dc = new DublinCoreSchema();
        	PdfSchema p = new PdfSchema();
        	XmpBasicSchema basic = new XmpBasicSchema();
        	String key;
        	String value;
        	for (Iterator it = info.entrySet().iterator(); it.hasNext();) {
        		Map.Entry entry = (Map.Entry) it.next();
        		key = (String) entry.getKey();
        		value = (String) entry.getValue();
        		if (value == null)
        			continue;
        		if ("Title".equals(key)) {
        			dc.addTitle(value);
        		}
        		if ("Author".equals(key)) {
        			dc.addAuthor(value);
        		}
        		if ("Subject".equals(key)) {
        			dc.addSubject(value);
        			dc.addDescription(value);
        		}
        		if ("Keywords".equals(key)) {
        			p.addKeywords(value);
        		}
        		if ("Creator".equals(key)) {
        			basic.addCreatorTool(value);
        		}
        		if ("Producer".equals(key)) {
        			p.addProducer(value);
        		}
        		if ("CreationDate".equals(key)) {
        			basic.addCreateDate(PdfDate.getW3CDate(value));
        		}
        		if ("ModDate".equals(key)) {
        			basic.addModDate(PdfDate.getW3CDate(value));
        		}
        	}
        	if (dc.size() > 0) addRdfDescription(dc);
        	if (p.size() > 0) addRdfDescription(p);
        	if (basic.size() > 0) addRdfDescription(basic);
        }
    }
}