/*
 * $Id: PdfWriter.java 4095 2009-11-12 14:40:41Z blowagie $
 *
 * Copyright 1999, 2000, 2001, 2002 Bruno Lowagie
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
import java.awt.color.ICC_Profile;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.security.cert.Certificate;
import com.lowagie.text.error_messages.MessageLocalization;

import com.lowagie.text.DocListener;
import com.lowagie.text.DocWriter;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.ExceptionConverter;
import com.lowagie.text.Image;
import com.lowagie.text.ImgJBIG2;
import com.lowagie.text.ImgWMF;
import com.lowagie.text.Rectangle;
import com.lowagie.text.Table;
import com.lowagie.text.pdf.collection.PdfCollection;
import com.lowagie.text.pdf.events.PdfPageEventForwarder;
import com.lowagie.text.pdf.interfaces.PdfAnnotations;
import com.lowagie.text.pdf.interfaces.PdfDocumentActions;
import com.lowagie.text.pdf.interfaces.PdfEncryptionSettings;
import com.lowagie.text.pdf.interfaces.PdfPageActions;
import com.lowagie.text.pdf.interfaces.PdfVersion;
import com.lowagie.text.pdf.interfaces.PdfViewerPreferences;
import com.lowagie.text.pdf.interfaces.PdfXConformance;
import com.lowagie.text.pdf.interfaces.PdfRunDirection;
import com.lowagie.text.pdf.internal.PdfVersionImp;
import com.lowagie.text.pdf.internal.PdfXConformanceImp;
import com.lowagie.text.xml.xmp.XmpWriter;

/**
 * A <CODE>DocWriter</CODE> class for PDF.
 * <P>
 * When this <CODE>PdfWriter</CODE> is added
 * to a certain <CODE>PdfDocument</CODE>, the PDF representation of every Element
 * added to this Document will be written to the outputstream.</P>
 */

public class PdfWriter extends DocWriter implements
	PdfViewerPreferences,
	PdfEncryptionSettings,
	PdfVersion,
	PdfDocumentActions,
	PdfPageActions,
	PdfXConformance,
	PdfRunDirection,
	PdfAnnotations {

	/**
	 * The highest generation number possible.
	 * @since	iText 2.1.6
	 */
	public static final int GENERATION_MAX = 65535;
	
// INNER CLASSES

    /**
     * This class generates the structure of a PDF document.
     * <P>
     * This class covers the third section of Chapter 5 in the 'Portable Document Format
     * Reference Manual version 1.3' (page 55-60). It contains the body of a PDF document
     * (section 5.14) and it can also generate a Cross-reference Table (section 5.15).
     *
     * @see		PdfWriter
     * @see		PdfObject
     * @see		PdfIndirectObject
     */

    public static class PdfBody {

        // inner classes

        /**
         * <CODE>PdfCrossReference</CODE> is an entry in the PDF Cross-Reference table.
         */

        static class PdfCrossReference implements Comparable {

            // membervariables
            private int type;

            /**	Byte offset in the PDF file. */
            private int offset;

            private int refnum;
            /**	generation of the object. */
            private int generation;

            // constructors
            /**
             * Constructs a cross-reference element for a PdfIndirectObject.
             * @param refnum
             * @param	offset		byte offset of the object
             * @param	generation	generation number of the object
             */

            PdfCrossReference(int refnum, int offset, int generation) {
                type = 0;
                this.offset = offset;
                this.refnum = refnum;
                this.generation = generation;
            }

            /**
             * Constructs a cross-reference element for a PdfIndirectObject.
             * @param refnum
             * @param	offset		byte offset of the object
             */

            PdfCrossReference(int refnum, int offset) {
                type = 1;
                this.offset = offset;
                this.refnum = refnum;
                this.generation = 0;
            }

            PdfCrossReference(int type, int refnum, int offset, int generation) {
                this.type = type;
                this.offset = offset;
                this.refnum = refnum;
                this.generation = generation;
            }

            int getRefnum() {
                return refnum;
            }

            /**
             * Returns the PDF representation of this <CODE>PdfObject</CODE>.
             * @param os
             * @throws IOException
             */

            public void toPdf(OutputStream os) throws IOException {
                StringBuffer off = new StringBuffer("0000000000").append(offset);
                off.delete(0, off.length() - 10);
                StringBuffer gen = new StringBuffer("00000").append(generation);
                gen.delete(0, gen.length() - 5);

                off.append(' ').append(gen).append(generation == GENERATION_MAX ? " f \n" : " n \n");
                os.write(getISOBytes(off.toString()));
            }

            /**
             * Writes PDF syntax to the OutputStream
             * @param midSize
             * @param os
             * @throws IOException
             */
            public void toPdf(int midSize, OutputStream os) throws IOException {
                os.write((byte)type);
                while (--midSize >= 0)
                    os.write((byte)((offset >>> (8 * midSize)) & 0xff));
                os.write((byte)((generation >>> 8) & 0xff));
                os.write((byte)(generation & 0xff));
            }

            /**
             * @see java.lang.Comparable#compareTo(java.lang.Object)
             */
            public int compareTo(Object o) {
                PdfCrossReference other = (PdfCrossReference)o;
                return (refnum < other.refnum ? -1 : (refnum==other.refnum ? 0 : 1));
            }

            /**
             * @see java.lang.Object#equals(java.lang.Object)
             */
            public boolean equals(Object obj) {
                if (obj instanceof PdfCrossReference) {
                    PdfCrossReference other = (PdfCrossReference)obj;
                    return (refnum == other.refnum);
                }
                else
                    return false;
            }

            /**
             * @see java.lang.Object#hashCode()
             */
            public int hashCode() {
				return refnum;
			}

        }

        private static final int OBJSINSTREAM = 200;

        // membervariables

        /** array containing the cross-reference table of the normal objects. */
        private TreeSet xrefs;
        private int refnum;
        /** the current byte position in the body. */
        private int position;
        private PdfWriter writer;
        private ByteBuffer index;
        private ByteBuffer streamObjects;
        private int currentObjNum;
        private int numObj = 0;

        // constructors

        /**
         * Constructs a new <CODE>PdfBody</CODE>.
         * @param writer
         */
        PdfBody(PdfWriter writer) {
            xrefs = new TreeSet();
            xrefs.add(new PdfCrossReference(0, 0, GENERATION_MAX));
            position = writer.getOs().getCounter();
            refnum = 1;
            this.writer = writer;
        }

        // methods

        void setRefnum(int refnum) {
            this.refnum = refnum;
        }

        private PdfWriter.PdfBody.PdfCrossReference addToObjStm(PdfObject obj, int nObj) throws IOException {
            if (numObj >= OBJSINSTREAM)
                flushObjStm();
            if (index == null) {
                index = new ByteBuffer();
                streamObjects = new ByteBuffer();
                currentObjNum = getIndirectReferenceNumber();
                numObj = 0;
            }
            int p = streamObjects.size();
            int idx = numObj++;
            PdfEncryption enc = writer.crypto;
            writer.crypto = null;
            obj.toPdf(writer, streamObjects);
            writer.crypto = enc;
            streamObjects.append(' ');
            index.append(nObj).append(' ').append(p).append(' ');
            return new PdfWriter.PdfBody.PdfCrossReference(2, nObj, currentObjNum, idx);
        }

        private void flushObjStm() throws IOException {
            if (numObj == 0)
                return;
            int first = index.size();
            index.append(streamObjects);
            PdfStream stream = new PdfStream(index.toByteArray());
            stream.flateCompress(writer.getCompressionLevel());
            stream.put(PdfName.TYPE, PdfName.OBJSTM);
            stream.put(PdfName.N, new PdfNumber(numObj));
            stream.put(PdfName.FIRST, new PdfNumber(first));
            add(stream, currentObjNum);
            index = null;
            streamObjects = null;
            numObj = 0;
        }

        /**
         * Adds a <CODE>PdfObject</CODE> to the body.
         * <P>
         * This methods creates a <CODE>PdfIndirectObject</CODE> with a
         * certain number, containing the given <CODE>PdfObject</CODE>.
         * It also adds a <CODE>PdfCrossReference</CODE> for this object
         * to an <CODE>ArrayList</CODE> that will be used to build the
         * Cross-reference Table.
         *
         * @param		object			a <CODE>PdfObject</CODE>
         * @return		a <CODE>PdfIndirectObject</CODE>
         * @throws IOException
         */

        PdfIndirectObject add(PdfObject object) throws IOException {
            return add(object, getIndirectReferenceNumber());
        }

        PdfIndirectObject add(PdfObject object, boolean inObjStm) throws IOException {
            return add(object, getIndirectReferenceNumber(), inObjStm);
        }

        /**
         * Gets a PdfIndirectReference for an object that will be created in the future.
         * @return a PdfIndirectReference
         */

        PdfIndirectReference getPdfIndirectReference() {
            return new PdfIndirectReference(0, getIndirectReferenceNumber());
        }

        int getIndirectReferenceNumber() {
            int n = refnum++;
            xrefs.add(new PdfCrossReference(n, 0, GENERATION_MAX));
            return n;
        }

        /**
         * Adds a <CODE>PdfObject</CODE> to the body given an already existing
         * PdfIndirectReference.
         * <P>
         * This methods creates a <CODE>PdfIndirectObject</CODE> with the number given by
         * <CODE>ref</CODE>, containing the given <CODE>PdfObject</CODE>.
         * It also adds a <CODE>PdfCrossReference</CODE> for this object
         * to an <CODE>ArrayList</CODE> that will be used to build the
         * Cross-reference Table.
         *
         * @param		object			a <CODE>PdfObject</CODE>
         * @param		ref		        a <CODE>PdfIndirectReference</CODE>
         * @return		a <CODE>PdfIndirectObject</CODE>
         * @throws IOException
         */

        PdfIndirectObject add(PdfObject object, PdfIndirectReference ref) throws IOException {
            return add(object, ref.getNumber());
        }

        PdfIndirectObject add(PdfObject object, PdfIndirectReference ref, boolean inObjStm) throws IOException {
            return add(object, ref.getNumber(), inObjStm);
        }

        PdfIndirectObject add(PdfObject object, int refNumber) throws IOException {
            return add(object, refNumber, true); // to false
        }

        PdfIndirectObject add(PdfObject object, int refNumber, boolean inObjStm) throws IOException {
            if (inObjStm && object.canBeInObjStm() && writer.isFullCompression()) {
                PdfCrossReference pxref = addToObjStm(object, refNumber);
                PdfIndirectObject indirect = new PdfIndirectObject(refNumber, object, writer);
                if (!xrefs.add(pxref)) {
                    xrefs.remove(pxref);
                    xrefs.add(pxref);
                }
                return indirect;
            }
            else {
                PdfIndirectObject indirect = new PdfIndirectObject(refNumber, object, writer);
                PdfCrossReference pxref = new PdfCrossReference(refNumber, position);
                if (!xrefs.add(pxref)) {
                    xrefs.remove(pxref);
                    xrefs.add(pxref);
                }
                indirect.writeTo(writer.getOs());
                position = writer.getOs().getCounter();
                return indirect;
            }
        }

        /**
         * Returns the offset of the Cross-Reference table.
         *
         * @return		an offset
         */

        int offset() {
            return position;
        }

        /**
         * Returns the total number of objects contained in the CrossReferenceTable of this <CODE>Body</CODE>.
         *
         * @return	a number of objects
         */

        int size() {
            return Math.max(((PdfCrossReference)xrefs.last()).getRefnum() + 1, refnum);
        }

        /**
         * Returns the CrossReferenceTable of the <CODE>Body</CODE>.
         * @param os
         * @param root
         * @param info
         * @param encryption
         * @param fileID
         * @param prevxref
         * @throws IOException
         */

        void writeCrossReferenceTable(OutputStream os, PdfIndirectReference root, PdfIndirectReference info, PdfIndirectReference encryption, PdfObject fileID, int prevxref) throws IOException {
            int refNumber = 0;
            if (writer.isFullCompression()) {
                flushObjStm();
                refNumber = getIndirectReferenceNumber();
                xrefs.add(new PdfCrossReference(refNumber, position));
            }
            PdfCrossReference entry = (PdfCrossReference)xrefs.first();
            int first = entry.getRefnum();
            int len = 0;
            ArrayList sections = new ArrayList();
            for (Iterator i = xrefs.iterator(); i.hasNext(); ) {
                entry = (PdfCrossReference)i.next();
                if (first + len == entry.getRefnum())
                    ++len;
                else {
                    sections.add(new Integer(first));
                    sections.add(new Integer(len));
                    first = entry.getRefnum();
                    len = 1;
                }
            }
            sections.add(new Integer(first));
            sections.add(new Integer(len));
            if (writer.isFullCompression()) {
                int mid = 4;
                int mask = 0xff000000;
                for (; mid > 1; --mid) {
                    if ((mask & position) != 0)
                        break;
                    mask >>>= 8;
                }
                ByteBuffer buf = new ByteBuffer();

                for (Iterator i = xrefs.iterator(); i.hasNext(); ) {
                    entry = (PdfCrossReference) i.next();
                    entry.toPdf(mid, buf);
                }
                PdfStream xr = new PdfStream(buf.toByteArray());
                buf = null;
                xr.flateCompress(writer.getCompressionLevel());
                xr.put(PdfName.SIZE, new PdfNumber(size()));
                xr.put(PdfName.ROOT, root);
                if (info != null) {
                    xr.put(PdfName.INFO, info);
                }
                if (encryption != null)
                    xr.put(PdfName.ENCRYPT, encryption);
                if (fileID != null)
                    xr.put(PdfName.ID, fileID);
                xr.put(PdfName.W, new PdfArray(new int[]{1, mid, 2}));
                xr.put(PdfName.TYPE, PdfName.XREF);
                PdfArray idx = new PdfArray();
                for (int k = 0; k < sections.size(); ++k)
                    idx.add(new PdfNumber(((Integer)sections.get(k)).intValue()));
                xr.put(PdfName.INDEX, idx);
                if (prevxref > 0)
                    xr.put(PdfName.PREV, new PdfNumber(prevxref));
                PdfEncryption enc = writer.crypto;
                writer.crypto = null;
                PdfIndirectObject indirect = new PdfIndirectObject(refNumber, xr, writer);
                indirect.writeTo(writer.getOs());
                writer.crypto = enc;
            }
            else {
                os.write(getISOBytes("xref\n"));
                Iterator i = xrefs.iterator();
                for (int k = 0; k < sections.size(); k += 2) {
                    first = ((Integer)sections.get(k)).intValue();
                    len = ((Integer)sections.get(k + 1)).intValue();
                    os.write(getISOBytes(String.valueOf(first)));
                    os.write(getISOBytes(" "));
                    os.write(getISOBytes(String.valueOf(len)));
                    os.write('\n');
                    while (len-- > 0) {
                        entry = (PdfCrossReference) i.next();
                        entry.toPdf(os);
                    }
                }
            }
        }
    }

    /**
     * <CODE>PdfTrailer</CODE> is the PDF Trailer object.
     * <P>
     * This object is described in the 'Portable Document Format Reference Manual version 1.3'
     * section 5.16 (page 59-60).
     */

    static class PdfTrailer extends PdfDictionary {

        // membervariables

        int offset;

        // constructors

        /**
         * Constructs a PDF-Trailer.
         *
         * @param		size		the number of entries in the <CODE>PdfCrossReferenceTable</CODE>
         * @param		offset		offset of the <CODE>PdfCrossReferenceTable</CODE>
         * @param		root		an indirect reference to the root of the PDF document
         * @param		info		an indirect reference to the info object of the PDF document
         * @param encryption
         * @param fileID
         * @param prevxref
         */

        PdfTrailer(int size, int offset, PdfIndirectReference root, PdfIndirectReference info, PdfIndirectReference encryption, PdfObject fileID, int prevxref) {
            this.offset = offset;
            put(PdfName.SIZE, new PdfNumber(size));
            put(PdfName.ROOT, root);
            if (info != null) {
                put(PdfName.INFO, info);
            }
            if (encryption != null)
                put(PdfName.ENCRYPT, encryption);
            if (fileID != null)
                put(PdfName.ID, fileID);
            if (prevxref > 0)
                put(PdfName.PREV, new PdfNumber(prevxref));
        }

        /**
         * Returns the PDF representation of this <CODE>PdfObject</CODE>.
         * @param writer
         * @param os
         * @throws IOException
         */
        public void toPdf(PdfWriter writer, OutputStream os) throws IOException {
            os.write(getISOBytes("trailer\n"));
            super.toPdf(null, os);
            os.write(getISOBytes("\nstartxref\n"));
            os.write(getISOBytes(String.valueOf(offset)));
            os.write(getISOBytes("\n%%EOF\n"));
        }
    }

//	ESSENTIALS

//	Construct a PdfWriter instance

    /**
     * Constructs a <CODE>PdfWriter</CODE>.
     */
    protected PdfWriter() {
    }

    /**
     * Constructs a <CODE>PdfWriter</CODE>.
     * <P>
     * Remark: a PdfWriter can only be constructed by calling the method
     * <CODE>getInstance(Document document, OutputStream os)</CODE>.
     *
     * @param	document	The <CODE>PdfDocument</CODE> that has to be written
     * @param	os			The <CODE>OutputStream</CODE> the writer has to write to.
     */

    protected PdfWriter(PdfDocument document, OutputStream os) {
        super(document, os);
        pdf = document;
        directContent = new PdfContentByte(this);
        directContentUnder = new PdfContentByte(this);
    }

    /**
     * Use this method to get an instance of the <CODE>PdfWriter</CODE>.
     *
     * @param	document	The <CODE>Document</CODE> that has to be written
     * @param	os	The <CODE>OutputStream</CODE> the writer has to write to.
     * @return	a new <CODE>PdfWriter</CODE>
     *
     * @throws	DocumentException on error
     */

    public static PdfWriter getInstance(Document document, OutputStream os)
    throws DocumentException {
        PdfDocument pdf = new PdfDocument();
        document.addDocListener(pdf);
        PdfWriter writer = new PdfWriter(pdf, os);
        pdf.addWriter(writer);
        return writer;
    }

    /**
     * Use this method to get an instance of the <CODE>PdfWriter</CODE>.
     *
     * @return a new <CODE>PdfWriter</CODE>
     * @param document The <CODE>Document</CODE> that has to be written
     * @param os The <CODE>OutputStream</CODE> the writer has to write to.
     * @param listener A <CODE>DocListener</CODE> to pass to the PdfDocument.
     * @throws DocumentException on error
     */

    public static PdfWriter getInstance(Document document, OutputStream os, DocListener listener)
    throws DocumentException {
        PdfDocument pdf = new PdfDocument();
        pdf.addDocListener(listener);
        document.addDocListener(pdf);
        PdfWriter writer = new PdfWriter(pdf, os);
        pdf.addWriter(writer);
        return writer;
    }

//	the PdfDocument instance

    /** the pdfdocument object. */
    protected PdfDocument pdf;

    /**
     * Gets the <CODE>PdfDocument</CODE> associated with this writer.
     * @return the <CODE>PdfDocument</CODE>
     */

    PdfDocument getPdfDocument() {
        return pdf;
    }

    /**
     * Use this method to get the info dictionary if you want to
     * change it directly (add keys and values to the info dictionary).
     * @return the info dictionary
     */
    public PdfDictionary getInfo() {
        return pdf.getInfo();
    }

    /**
     * Use this method to get the current vertical page position.
     * @param ensureNewLine Tells whether a new line shall be enforced. This may cause side effects
     *   for elements that do not terminate the lines they've started because those lines will get
     *   terminated.
     * @return The current vertical page position.
     */
    public float getVerticalPosition(boolean ensureNewLine) {
        return pdf.getVerticalPosition(ensureNewLine);
    }

    /**
     * Sets the initial leading for the PDF document.
     * This has to be done before the document is opened.
     * @param	leading	the initial leading
     * @since	2.1.6
     * @throws	DocumentException	if you try setting the leading after the document was opened.
     */
    public void setInitialLeading(float leading) throws DocumentException {
    	if (open)
    		throw new DocumentException(MessageLocalization.getComposedMessage("you.can.t.set.the.initial.leading.if.the.document.is.already.open"));
    	pdf.setLeading(leading);
    }
    
//	the PdfDirectContentByte instances

/*
 * You should see Direct Content as a canvas on which you can draw
 * graphics and text. One canvas goes on top of the page (getDirectContent),
 * the other goes underneath (getDirectContentUnder).
 * You can always the same object throughout your document,
 * even if you have moved to a new page. Whatever you add on
 * the canvas will be displayed on top or under the current page.
 */

    /** The direct content in this document. */
    protected PdfContentByte directContent;

    /** The direct content under in this document. */
    protected PdfContentByte directContentUnder;

    /**
     * Use this method to get the direct content for this document.
     * There is only one direct content, multiple calls to this method
     * will allways retrieve the same object.
     * @return the direct content
     */

    public PdfContentByte getDirectContent() {
        if (!open)
            throw new RuntimeException(MessageLocalization.getComposedMessage("the.document.is.not.open"));
        return directContent;
    }

    /**
     * Use this method to get the direct content under for this document.
     * There is only one direct content, multiple calls to this method
     * will always retrieve the same object.
     * @return the direct content
     */

    public PdfContentByte getDirectContentUnder() {
        if (!open)
            throw new RuntimeException(MessageLocalization.getComposedMessage("the.document.is.not.open"));
        return directContentUnder;
    }

    /**
     * Resets all the direct contents to empty.
     * This happens when a new page is started.
     */
    void resetContent() {
        directContent.reset();
        directContentUnder.reset();
    }

//	PDF body

/*
 * A PDF file has 4 parts: a header, a body, a cross-reference table, and a trailer.
 * The body contains all the PDF objects that make up the PDF document.
 * Each element gets a reference (a set of numbers) and the byte position of
 * every object is stored in the cross-reference table.
 * Use these methods only if you know what you're doing.
 */

    /** body of the PDF document */
    protected PdfBody body;

    /**
     * Adds the local destinations to the body of the document.
     * @param dest the <CODE>HashMap</CODE> containing the destinations
     * @throws IOException on error
     */

    void addLocalDestinations(TreeMap dest) throws IOException {
        for (Iterator i = dest.entrySet().iterator(); i.hasNext();) {
            Map.Entry entry = (Map.Entry) i.next();
            String name = (String) entry.getKey();
            Object obj[] = (Object[]) entry.getValue();
            PdfDestination destination = (PdfDestination)obj[2];
            if (obj[1] == null)
                obj[1] = getPdfIndirectReference();
            if (destination == null)
                addToBody(new PdfString("invalid_" + name), (PdfIndirectReference)obj[1]);
            else
                addToBody(destination, (PdfIndirectReference)obj[1]);
        }
    }

    /**
     * Use this method to add a PDF object to the PDF body.
     * Use this method only if you know what you're doing!
     * @param object
     * @return a PdfIndirectObject
     * @throws IOException
     */
    public PdfIndirectObject addToBody(PdfObject object) throws IOException {
        PdfIndirectObject iobj = body.add(object);
        return iobj;
    }

    /**
     * Use this method to add a PDF object to the PDF body.
     * Use this method only if you know what you're doing!
     * @param object
     * @param inObjStm
     * @return a PdfIndirectObject
     * @throws IOException
     */
    public PdfIndirectObject addToBody(PdfObject object, boolean inObjStm) throws IOException {
        PdfIndirectObject iobj = body.add(object, inObjStm);
        return iobj;
    }

    /**
     * Use this method to add a PDF object to the PDF body.
     * Use this method only if you know what you're doing!
     * @param object
     * @param ref
     * @return a PdfIndirectObject
     * @throws IOException
     */
    public PdfIndirectObject addToBody(PdfObject object, PdfIndirectReference ref) throws IOException {
        PdfIndirectObject iobj = body.add(object, ref);
        return iobj;
    }

    /**
     * Use this method to add a PDF object to the PDF body.
     * Use this method only if you know what you're doing!
     * @param object
     * @param ref
     * @param inObjStm
     * @return a PdfIndirectObject
     * @throws IOException
     */
    public PdfIndirectObject addToBody(PdfObject object, PdfIndirectReference ref, boolean inObjStm) throws IOException {
        PdfIndirectObject iobj = body.add(object, ref, inObjStm);
        return iobj;
    }

    /**
     * Use this method to add a PDF object to the PDF body.
     * Use this method only if you know what you're doing!
     * @param object
     * @param refNumber
     * @return a PdfIndirectObject
     * @throws IOException
     */
    public PdfIndirectObject addToBody(PdfObject object, int refNumber) throws IOException {
        PdfIndirectObject iobj = body.add(object, refNumber);
        return iobj;
    }

    /**
     * Use this method to add a PDF object to the PDF body.
     * Use this method only if you know what you're doing!
     * @param object
     * @param refNumber
     * @param inObjStm
     * @return a PdfIndirectObject
     * @throws IOException
     */
    public PdfIndirectObject addToBody(PdfObject object, int refNumber, boolean inObjStm) throws IOException {
        PdfIndirectObject iobj = body.add(object, refNumber, inObjStm);
        return iobj;
    }

    /**
     * Use this to get an <CODE>PdfIndirectReference</CODE> for an object that
     * will be created in the future.
     * Use this method only if you know what you're doing!
     * @return the <CODE>PdfIndirectReference</CODE>
     */

    public PdfIndirectReference getPdfIndirectReference() {
        return body.getPdfIndirectReference();
    }

    int getIndirectReferenceNumber() {
        return body.getIndirectReferenceNumber();
    }

    /**
     * Returns the outputStreamCounter.
     * @return the outputStreamCounter
     */
    OutputStreamCounter getOs() {
        return os;
    }


//	PDF Catalog

/*
 * The Catalog is also called the root object of the document.
 * Whereas the Cross-Reference maps the objects number with the
 * byte offset so that the viewer can find the objects, the
 * Catalog tells the viewer the numbers of the objects needed
 * to render the document.
 */

    protected PdfDictionary getCatalog(PdfIndirectReference rootObj)
    {
        PdfDictionary catalog = pdf.getCatalog(rootObj);
        // [F12] tagged PDF
        if (tagged) {
            try {
                getStructureTreeRoot().buildTree();
            }
            catch (Exception e) {
                throw new ExceptionConverter(e);
            }
            catalog.put(PdfName.STRUCTTREEROOT, structureTreeRoot.getReference());
            PdfDictionary mi = new PdfDictionary();
            mi.put(PdfName.MARKED, PdfBoolean.PDFTRUE);
            if (userProperties)
                mi.put(PdfName.USERPROPERTIES, PdfBoolean.PDFTRUE);
            catalog.put(PdfName.MARKINFO, mi);
        }
        // [F13] OCG
        if (!documentOCG.isEmpty()) {
        	fillOCProperties(false);
        	catalog.put(PdfName.OCPROPERTIES, OCProperties);
        }
        return catalog;
    }

    /** Holds value of property extraCatalog this is used for Output Intents. */
    protected PdfDictionary extraCatalog;

    /**
     * Sets extra keys to the catalog.
     * @return the catalog to change
     */
    public PdfDictionary getExtraCatalog() {
        if (extraCatalog == null)
            extraCatalog = new PdfDictionary();
        return this.extraCatalog;
    }

//	PdfPages

/*
 * The page root keeps the complete page tree of the document.
 * There's an entry in the Catalog that refers to the root
 * of the page tree, the page tree contains the references
 * to pages and other page trees.
 */

    /** The root of the page tree. */
    protected PdfPages root = new PdfPages(this);
    /** The PdfIndirectReference to the pages. */
    protected ArrayList pageReferences = new ArrayList();
    /** The current page number. */
    protected int currentPageNumber = 1;
    /**
     * The value of the Tabs entry in the page dictionary.
     * @since	2.1.5
     */
    protected PdfName tabs = null;

    /**
     * Use this method to make sure the page tree has a linear structure
     * (every leave is attached directly to the root).
     * Use this method to allow page reordering with method reorderPages.
     */
     public void setLinearPageMode() {
        root.setLinearMode(null);
    }

    /**
     * Use this method to reorder the pages in the document.
     * A <CODE>null</CODE> argument value only returns the number of pages to process.
     * It is advisable to issue a <CODE>Document.newPage()</CODE> before using this method.
     * @return the total number of pages
     * @param order an array with the new page sequence. It must have the
     * same size as the number of pages.
     * @throws DocumentException if all the pages are not present in the array
     */
    public int reorderPages(int order[]) throws DocumentException {
        return root.reorderPages(order);
    }

    /**
     * Use this method to get a reference to a page existing or not.
     * If the page does not exist yet the reference will be created
     * in advance. If on closing the document, a page number greater
     * than the total number of pages was requested, an exception
     * is thrown.
     * @param page the page number. The first page is 1
     * @return the reference to the page
     */
    public PdfIndirectReference getPageReference(int page) {
        --page;
        if (page < 0)
            throw new IndexOutOfBoundsException(MessageLocalization.getComposedMessage("the.page.number.must.be.gt.eq.1"));
        PdfIndirectReference ref;
        if (page < pageReferences.size()) {
            ref = (PdfIndirectReference)pageReferences.get(page);
            if (ref == null) {
                ref = body.getPdfIndirectReference();
                pageReferences.set(page, ref);
            }
        }
        else {
            int empty = page - pageReferences.size();
            for (int k = 0; k < empty; ++k)
                pageReferences.add(null);
            ref = body.getPdfIndirectReference();
            pageReferences.add(ref);
        }
        return ref;
    }

    /**
     * Gets the pagenumber of this document.
     * This number can be different from the real pagenumber,
     * if you have (re)set the page number previously.
     * @return a page number
     */

    public int getPageNumber() {
        return pdf.getPageNumber();
    }

    PdfIndirectReference getCurrentPage() {
        return getPageReference(currentPageNumber);
    }

    public int getCurrentPageNumber() {
        return currentPageNumber;
    }

    /**
     * Sets the value for the Tabs entry in the page tree.
     * @param	tabs	Can be PdfName.R, PdfName.C or PdfName.S.
     * Since the Adobe Extensions Level 3, it can also be PdfName.A
     * or PdfName.W
     * @since	2.1.5
     */
    public void setTabs(PdfName tabs) {
    	this.tabs = tabs;
    }

    /**
     * Returns the value to be used for the Tabs entry in the page tree.
     * @since	2.1.5
     */
    public PdfName getTabs() {
    	return tabs;
    }

    /**
     * Adds some <CODE>PdfContents</CODE> to this Writer.
     * <P>
     * The document has to be open before you can begin to add content
     * to the body of the document.
     *
     * @return a <CODE>PdfIndirectReference</CODE>
     * @param page the <CODE>PdfPage</CODE> to add
     * @param contents the <CODE>PdfContents</CODE> of the page
     * @throws PdfException on error
     */

    PdfIndirectReference add(PdfPage page, PdfContents contents) throws PdfException {
        if (!open) {
            throw new PdfException(MessageLocalization.getComposedMessage("the.document.is.not.open"));
        }
        PdfIndirectObject object;
        try {
            object = addToBody(contents);
        }
        catch(IOException ioe) {
            throw new ExceptionConverter(ioe);
        }
        page.add(object.getIndirectReference());
        // [U5]
        if (group != null) {
            page.put(PdfName.GROUP, group);
            group = null;
        }
        else if (rgbTransparencyBlending) {
            PdfDictionary pp = new PdfDictionary();
            pp.put(PdfName.TYPE, PdfName.GROUP);
            pp.put(PdfName.S, PdfName.TRANSPARENCY);
            pp.put(PdfName.CS, PdfName.DEVICERGB);
            page.put(PdfName.GROUP, pp);
        }
        root.addPage(page);
        currentPageNumber++;
        return null;
    }

//	page events

/*
 * Page events are specific for iText, not for PDF.
 * Upon specific events (for instance when a page starts
 * or ends), the corresponding method in the page event
 * implementation that is added to the writer is invoked.
 */

    /** The <CODE>PdfPageEvent</CODE> for this document. */
    private PdfPageEvent pageEvent;

    /**
     * Sets the <CODE>PdfPageEvent</CODE> for this document.
     * @param event the <CODE>PdfPageEvent</CODE> for this document
     */

    public void setPageEvent(PdfPageEvent event) {
    	if (event == null) this.pageEvent = null;
    	else if (this.pageEvent == null) this.pageEvent = event;
    	else if (this.pageEvent instanceof PdfPageEventForwarder) ((PdfPageEventForwarder)this.pageEvent).addPageEvent(event);
    	else {
    		PdfPageEventForwarder forward = new PdfPageEventForwarder();
    		forward.addPageEvent(this.pageEvent);
    		forward.addPageEvent(event);
    		this.pageEvent = forward;
    	}
    }

    /**
     * Gets the <CODE>PdfPageEvent</CODE> for this document or <CODE>null</CODE>
     * if none is set.
     * @return the <CODE>PdfPageEvent</CODE> for this document or <CODE>null</CODE>
     * if none is set
     */

    public PdfPageEvent getPageEvent() {
        return pageEvent;
    }

//	Open and Close methods + method that create the PDF

    /** A number referring to the previous Cross-Reference Table. */
    protected int prevxref = 0;

    /**
     * Signals that the <CODE>Document</CODE> has been opened and that
     * <CODE>Elements</CODE> can be added.
     * <P>
     * When this method is called, the PDF-document header is
     * written to the outputstream.
     * @see com.lowagie.text.DocWriter#open()
     */
    public void open() {
        super.open();
        try {
        	pdf_version.writeHeader(os);
            body = new PdfBody(this);
            if (pdfxConformance.isPdfX32002()) {
                PdfDictionary sec = new PdfDictionary();
                sec.put(PdfName.GAMMA, new PdfArray(new float[]{2.2f,2.2f,2.2f}));
                sec.put(PdfName.MATRIX, new PdfArray(new float[]{0.4124f,0.2126f,0.0193f,0.3576f,0.7152f,0.1192f,0.1805f,0.0722f,0.9505f}));
                sec.put(PdfName.WHITEPOINT, new PdfArray(new float[]{0.9505f,1f,1.089f}));
                PdfArray arr = new PdfArray(PdfName.CALRGB);
                arr.add(sec);
                setDefaultColorspace(PdfName.DEFAULTRGB, addToBody(arr).getIndirectReference());
            }
        }
        catch(IOException ioe) {
            throw new ExceptionConverter(ioe);
        }
    }

    /**
     * Signals that the <CODE>Document</CODE> was closed and that no other
     * <CODE>Elements</CODE> will be added.
     * <P>
     * The pages-tree is built and written to the outputstream.
     * A Catalog is constructed, as well as an Info-object,
     * the reference table is composed and everything is written
     * to the outputstream embedded in a Trailer.
     * @see com.lowagie.text.DocWriter#close()
     */
    public void close() {
        if (open) {
            if ((currentPageNumber - 1) != pageReferences.size())
                throw new RuntimeException("The page " + pageReferences.size() +
                " was requested but the document has only " + (currentPageNumber - 1) + " pages.");
            pdf.close();
            try {
                addSharedObjectsToBody();
                // add the root to the body
                PdfIndirectReference rootRef = root.writePageTree();
                // make the catalog-object and add it to the body
                PdfDictionary catalog = getCatalog(rootRef);
                // [C9] if there is XMP data to add: add it
                if (xmpMetadata != null) {
                	PdfStream xmp = new PdfStream(xmpMetadata);
                	xmp.put(PdfName.TYPE, PdfName.METADATA);
                	xmp.put(PdfName.SUBTYPE, PdfName.XML);
                    if (crypto != null && !crypto.isMetadataEncrypted()) {
                        PdfArray ar = new PdfArray();
                        ar.add(PdfName.CRYPT);
                        xmp.put(PdfName.FILTER, ar);
                    }
                	catalog.put(PdfName.METADATA, body.add(xmp).getIndirectReference());
                }
                // [C10] make pdfx conformant
                if (isPdfX()) {
                    pdfxConformance.completeInfoDictionary(getInfo());
                    pdfxConformance.completeExtraCatalog(getExtraCatalog());
                }
                // [C11] Output Intents
                if (extraCatalog != null) {
                    catalog.mergeDifferent(extraCatalog);
                }

                writeOutlines(catalog, false);

                // add the Catalog to the body
                PdfIndirectObject indirectCatalog = addToBody(catalog, false);
                // add the info-object to the body
                PdfIndirectObject infoObj = addToBody(getInfo(), false);

                // [F1] encryption
                PdfIndirectReference encryption = null;
                PdfObject fileID = null;
                body.flushObjStm();
                if (crypto != null) {
                    PdfIndirectObject encryptionObject = addToBody(crypto.getEncryptionDictionary(), false);
                    encryption = encryptionObject.getIndirectReference();
                    fileID = crypto.getFileID();
                }
                else
                    fileID = PdfEncryption.createInfoId(PdfEncryption.createDocumentId());

                // write the cross-reference table of the body
                body.writeCrossReferenceTable(os, indirectCatalog.getIndirectReference(),
                    infoObj.getIndirectReference(), encryption,  fileID, prevxref);

                // make the trailer
                // [F2] full compression
                if (fullCompression) {
                    os.write(getISOBytes("startxref\n"));
                    os.write(getISOBytes(String.valueOf(body.offset())));
                    os.write(getISOBytes("\n%%EOF\n"));
                }
                else {
                    PdfTrailer trailer = new PdfTrailer(body.size(),
                    body.offset(),
                    indirectCatalog.getIndirectReference(),
                    infoObj.getIndirectReference(),
                    encryption,
                    fileID, prevxref);
                    trailer.toPdf(this, os);
                }
                super.close();
            }
            catch(IOException ioe) {
                throw new ExceptionConverter(ioe);
            }
        }
    }

    protected void addSharedObjectsToBody() throws IOException {
        // [F3] add the fonts
        for (Iterator it = documentFonts.values().iterator(); it.hasNext();) {
            FontDetails details = (FontDetails)it.next();
            details.writeFont(this);
        }
        // [F4] add the form XObjects
        for (Iterator it = formXObjects.values().iterator(); it.hasNext();) {
            Object objs[] = (Object[])it.next();
            PdfTemplate template = (PdfTemplate)objs[1];
            if (template != null && template.getIndirectReference() instanceof PRIndirectReference)
                continue;
            if (template != null && template.getType() == PdfTemplate.TYPE_TEMPLATE) {
                addToBody(template.getFormXObject(compressionLevel), template.getIndirectReference());
            }
        }
        // [F5] add all the dependencies in the imported pages
        for (Iterator it = importedPages.values().iterator(); it.hasNext();) {
            currentPdfReaderInstance = (PdfReaderInstance)it.next();
            currentPdfReaderInstance.writeAllPages();
        }
        currentPdfReaderInstance = null;
        // [F6] add the spotcolors
        for (Iterator it = documentColors.values().iterator(); it.hasNext();) {
            ColorDetails color = (ColorDetails)it.next();
            addToBody(color.getSpotColor(this), color.getIndirectReference());
        }
        // [F7] add the pattern
        for (Iterator it = documentPatterns.keySet().iterator(); it.hasNext();) {
            PdfPatternPainter pat = (PdfPatternPainter)it.next();
            addToBody(pat.getPattern(compressionLevel), pat.getIndirectReference());
        }
        // [F8] add the shading patterns
        for (Iterator it = documentShadingPatterns.keySet().iterator(); it.hasNext();) {
            PdfShadingPattern shadingPattern = (PdfShadingPattern)it.next();
            shadingPattern.addToBody();
        }
        // [F9] add the shadings
        for (Iterator it = documentShadings.keySet().iterator(); it.hasNext();) {
            PdfShading shading = (PdfShading)it.next();
            shading.addToBody();
        }
        // [F10] add the extgstate
        for (Iterator it = documentExtGState.entrySet().iterator(); it.hasNext();) {
            Map.Entry entry = (Map.Entry) it.next();
            PdfDictionary gstate = (PdfDictionary) entry.getKey();
            PdfObject obj[] = (PdfObject[]) entry.getValue();
            addToBody(gstate, (PdfIndirectReference)obj[1]);
        }
        // [F11] add the properties
        for (Iterator it = documentProperties.entrySet().iterator(); it.hasNext();) {
            Map.Entry entry = (Map.Entry) it.next();
            Object prop = entry.getKey();
            PdfObject[] obj = (PdfObject[]) entry.getValue();
            if (prop instanceof PdfLayerMembership){
                PdfLayerMembership layer = (PdfLayerMembership)prop;
                addToBody(layer.getPdfObject(), layer.getRef());
            }
            else if ((prop instanceof PdfDictionary) && !(prop instanceof PdfLayer)){
                addToBody((PdfDictionary)prop, (PdfIndirectReference)obj[1]);
            }
        }
        // [F13] add the OCG layers
        for (Iterator it = documentOCG.iterator(); it.hasNext();) {
            PdfOCG layer = (PdfOCG)it.next();
            addToBody(layer.getPdfObject(), layer.getRef());
        }
    }

// Root data for the PDF document (used when composing the Catalog)

//  [C1] Outlines (bookmarks)

     /**
      * Use this method to get the root outline
      * and construct bookmarks.
      * @return the root outline
      */

     public PdfOutline getRootOutline() {
         return directContent.getRootOutline();
     }

     protected java.util.List newBookmarks;

    /**
     * Sets the bookmarks. The list structure is defined in
     * {@link SimpleBookmark}.
     * @param outlines the bookmarks or <CODE>null</CODE> to remove any
     */
    public void setOutlines(java.util.List outlines) {
        newBookmarks = outlines;
    }

    protected void writeOutlines(PdfDictionary catalog, boolean namedAsNames) throws IOException {
        if (newBookmarks == null || newBookmarks.isEmpty())
            return;
        PdfDictionary top = new PdfDictionary();
        PdfIndirectReference topRef = getPdfIndirectReference();
        Object kids[] = SimpleBookmark.iterateOutlines(this, topRef, newBookmarks, namedAsNames);
        top.put(PdfName.FIRST, (PdfIndirectReference)kids[0]);
        top.put(PdfName.LAST, (PdfIndirectReference)kids[1]);
        top.put(PdfName.COUNT, new PdfNumber(((Integer)kids[2]).intValue()));
        addToBody(top, topRef);
        catalog.put(PdfName.OUTLINES, topRef);
    }

//	[C2] PdfVersion interface
     /** possible PDF version (header) */
     public static final char VERSION_1_2 = '2';
     /** possible PDF version (header) */
     public static final char VERSION_1_3 = '3';
     /** possible PDF version (header) */
     public static final char VERSION_1_4 = '4';
     /** possible PDF version (header) */
     public static final char VERSION_1_5 = '5';
     /** possible PDF version (header) */
     public static final char VERSION_1_6 = '6';
     /** possible PDF version (header) */
     public static final char VERSION_1_7 = '7';

     /** possible PDF version (catalog) */
     public static final PdfName PDF_VERSION_1_2 = new PdfName("1.2");
     /** possible PDF version (catalog) */
     public static final PdfName PDF_VERSION_1_3 = new PdfName("1.3");
     /** possible PDF version (catalog) */
     public static final PdfName PDF_VERSION_1_4 = new PdfName("1.4");
     /** possible PDF version (catalog) */
     public static final PdfName PDF_VERSION_1_5 = new PdfName("1.5");
     /** possible PDF version (catalog) */
     public static final PdfName PDF_VERSION_1_6 = new PdfName("1.6");
     /** possible PDF version (catalog) */
     public static final PdfName PDF_VERSION_1_7 = new PdfName("1.7");

    /** Stores the version information for the header and the catalog. */
    protected PdfVersionImp pdf_version = new PdfVersionImp();

    /** @see com.lowagie.text.pdf.interfaces.PdfVersion#setPdfVersion(char) */
    public void setPdfVersion(char version) {
        pdf_version.setPdfVersion(version);
    }

    /** @see com.lowagie.text.pdf.interfaces.PdfVersion#setAtLeastPdfVersion(char) */
    public void setAtLeastPdfVersion(char version) {
    	pdf_version.setAtLeastPdfVersion(version);
    }

	/** @see com.lowagie.text.pdf.interfaces.PdfVersion#setPdfVersion(com.lowagie.text.pdf.PdfName) */
	public void setPdfVersion(PdfName version) {
		pdf_version.setPdfVersion(version);
	}

	/**
	 * @see com.lowagie.text.pdf.interfaces.PdfVersion#addDeveloperExtension(com.lowagie.text.pdf.PdfDeveloperExtension)
	 * @since	2.1.6
	 */
	public void addDeveloperExtension(PdfDeveloperExtension de) {
		pdf_version.addDeveloperExtension(de);
	}
	
	/**
	 * Returns the version information.
	 */
	PdfVersionImp getPdfVersion() {
		return pdf_version;
	}

//  [C3] PdfViewerPreferences interface

	// page layout (section 13.1.1 of "iText in Action")

	/** A viewer preference */
	public static final int PageLayoutSinglePage = 1;
	/** A viewer preference */
	public static final int PageLayoutOneColumn = 2;
	/** A viewer preference */
	public static final int PageLayoutTwoColumnLeft = 4;
	/** A viewer preference */
	public static final int PageLayoutTwoColumnRight = 8;
	/** A viewer preference */
	public static final int PageLayoutTwoPageLeft = 16;
	/** A viewer preference */
	public static final int PageLayoutTwoPageRight = 32;

    // page mode (section 13.1.2 of "iText in Action")

    /** A viewer preference */
    public static final int PageModeUseNone = 64;
    /** A viewer preference */
    public static final int PageModeUseOutlines = 128;
    /** A viewer preference */
    public static final int PageModeUseThumbs = 256;
    /** A viewer preference */
    public static final int PageModeFullScreen = 512;
    /** A viewer preference */
    public static final int PageModeUseOC = 1024;
    /** A viewer preference */
    public static final int PageModeUseAttachments = 2048;

    // values for setting viewer preferences in iText versions older than 2.x

    /** A viewer preference */
    public static final int HideToolbar = 1 << 12;
    /** A viewer preference */
    public static final int HideMenubar = 1 << 13;
    /** A viewer preference */
    public static final int HideWindowUI = 1 << 14;
    /** A viewer preference */
    public static final int FitWindow = 1 << 15;
    /** A viewer preference */
    public static final int CenterWindow = 1 << 16;
    /** A viewer preference */
    public static final int DisplayDocTitle = 1 << 17;

    /** A viewer preference */
    public static final int NonFullScreenPageModeUseNone = 1 << 18;
    /** A viewer preference */
    public static final int NonFullScreenPageModeUseOutlines = 1 << 19;
    /** A viewer preference */
    public static final int NonFullScreenPageModeUseThumbs = 1 << 20;
    /** A viewer preference */
    public static final int NonFullScreenPageModeUseOC = 1 << 21;

    /** A viewer preference */
    public static final int DirectionL2R = 1 << 22;
    /** A viewer preference */
    public static final int DirectionR2L = 1 << 23;

    /** A viewer preference */
    public static final int PrintScalingNone = 1 << 24;

    /** @see com.lowagie.text.pdf.interfaces.PdfViewerPreferences#setViewerPreferences(int) */
    public void setViewerPreferences(int preferences) {
        pdf.setViewerPreferences(preferences);
    }

    /** @see com.lowagie.text.pdf.interfaces.PdfViewerPreferences#addViewerPreference(com.lowagie.text.pdf.PdfName, com.lowagie.text.pdf.PdfObject) */
    public void addViewerPreference(PdfName key, PdfObject value) {
    	pdf.addViewerPreference(key, value);
    }

//  [C4] Page labels

    /**
     * Use this method to add page labels
     * @param pageLabels the page labels
     */
    public void setPageLabels(PdfPageLabels pageLabels) {
        pdf.setPageLabels(pageLabels);
    }

//  [C5] named objects: named destinations, javascript, embedded files

    /**
     * Adds named destinations in bulk.
     * Valid keys and values of the map can be found in the map
     * that is created by SimpleNamedDestination.
     * @param	map	a map with strings as keys for the names,
     * 			and structured strings as values for the destinations
     * @param	page_offset	number of pages that has to be added to
     * 			the page numbers in the destinations (useful if you
     *          use this method in combination with PdfCopy).
     * @since	iText 5.0
     */
    public void addNamedDestinations(Map map, int page_offset) {
    	Map.Entry entry;
    	int page;
    	String dest;
    	PdfDestination destination;
    	for (Iterator i = map.entrySet().iterator(); i.hasNext(); ) {
    		entry = (Map.Entry)i.next();
    		dest = (String)entry.getValue();
    		page = Integer.parseInt(dest.substring(0, dest.indexOf(" ")));
    		destination = new PdfDestination(dest.substring(dest.indexOf(" ") + 1));
    		addNamedDestination((String)entry.getKey(), page + page_offset, destination);
    	}
    }
    
    /**
     * Adds one named destination.
     * @param	name	the name for the destination
     * @param	page	the page number where you want to jump to
     * @param	dest	an explicit destination
     * @since	iText 5.0
     */
    public void addNamedDestination(String name, int page, PdfDestination dest) {
    	dest.addPage(getPageReference(page));
    	pdf.localDestination(name, dest);
    }
    
     /**
      * Use this method to add a JavaScript action at the document level.
      * When the document opens, all this JavaScript runs.
      * @param js The JavaScript action
      */
     public void addJavaScript(PdfAction js) {
         pdf.addJavaScript(js);
     }

     /**
      * Use this method to add a JavaScript action at the document level.
      * When the document opens, all this JavaScript runs.
      * @param code the JavaScript code
      * @param unicode select JavaScript unicode. Note that the internal
      * Acrobat JavaScript engine does not support unicode,
      * so this may or may not work for you
      */
     public void addJavaScript(String code, boolean unicode) {
         addJavaScript(PdfAction.javaScript(code, this, unicode));
     }

     /**
      * Use this method to adds a JavaScript action at the document level.
      * When the document opens, all this JavaScript runs.
      * @param code the JavaScript code
      */
     public void addJavaScript(String code) {
         addJavaScript(code, false);
     }
     /**
      * Use this method to add a JavaScript action at the document level.
      * When the document opens, all this JavaScript runs.
      * @param name	The name of the JS Action in the name tree
      * @param js The JavaScript action
      */
     public void addJavaScript(String name, PdfAction js) {
         pdf.addJavaScript(name, js);
     }

     /**
      * Use this method to add a JavaScript action at the document level.
      * When the document opens, all this JavaScript runs.
      * @param name	The name of the JS Action in the name tree
      * @param code the JavaScript code
      * @param unicode select JavaScript unicode. Note that the internal
      * Acrobat JavaScript engine does not support unicode,
      * so this may or may not work for you
      */
     public void addJavaScript(String name, String code, boolean unicode) {
         addJavaScript(name, PdfAction.javaScript(code, this, unicode));
     }

     /**
      * Use this method to adds a JavaScript action at the document level.
      * When the document opens, all this JavaScript runs.
      * @param name	The name of the JS Action in the name tree
      * @param code the JavaScript code
      */
     public void addJavaScript(String name, String code) {
         addJavaScript(name, code, false);
     }

     /**
      * Use this method to add a file attachment at the document level.
      * @param description the file description
      * @param fileStore an array with the file. If it's <CODE>null</CODE>
      * the file will be read from the disk
      * @param file the path to the file. It will only be used if
      * <CODE>fileStore</CODE> is not <CODE>null</CODE>
      * @param fileDisplay the actual file name stored in the pdf
      * @throws IOException on error
      */
     public void addFileAttachment(String description, byte fileStore[], String file, String fileDisplay) throws IOException {
         addFileAttachment(description, PdfFileSpecification.fileEmbedded(this, file, fileDisplay, fileStore));
     }

     /**
      * Use this method to add a file attachment at the document level.
      * @param description the file description
      * @param fs the file specification
      */
     public void addFileAttachment(String description, PdfFileSpecification fs) throws IOException {
         pdf.addFileAttachment(description, fs);
     }

     /**
      * Use this method to add a file attachment at the document level.
      * @param fs the file specification
      */
     public void addFileAttachment(PdfFileSpecification fs) throws IOException {
         addFileAttachment(null, fs);
     }

// [C6] Actions (open and additional)

     /** action value */
     public static final PdfName DOCUMENT_CLOSE = PdfName.WC;
     /** action value */
     public static final PdfName WILL_SAVE = PdfName.WS;
     /** action value */
     public static final PdfName DID_SAVE = PdfName.DS;
     /** action value */
     public static final PdfName WILL_PRINT = PdfName.WP;
     /** action value */
     public static final PdfName DID_PRINT = PdfName.DP;

    /** @see com.lowagie.text.pdf.interfaces.PdfDocumentActions#setOpenAction(java.lang.String) */
    public void setOpenAction(String name) {
         pdf.setOpenAction(name);
     }

    /** @see com.lowagie.text.pdf.interfaces.PdfDocumentActions#setOpenAction(com.lowagie.text.pdf.PdfAction) */
    public void setOpenAction(PdfAction action) {
         pdf.setOpenAction(action);
     }

    /** @see com.lowagie.text.pdf.interfaces.PdfDocumentActions#setAdditionalAction(com.lowagie.text.pdf.PdfName, com.lowagie.text.pdf.PdfAction) */
    public void setAdditionalAction(PdfName actionType, PdfAction action) throws DocumentException {
         if (!(actionType.equals(DOCUMENT_CLOSE) ||
         actionType.equals(WILL_SAVE) ||
         actionType.equals(DID_SAVE) ||
         actionType.equals(WILL_PRINT) ||
         actionType.equals(DID_PRINT))) {
             throw new DocumentException(MessageLocalization.getComposedMessage("invalid.additional.action.type.1", actionType.toString()));
         }
         pdf.addAdditionalAction(actionType, action);
     }

//  [C7] portable collections

    /**
     * Use this method to add the Collection dictionary.
     * @param collection a dictionary of type PdfCollection
     */
    public void setCollection(PdfCollection collection) {
        setAtLeastPdfVersion(VERSION_1_7);
        pdf.setCollection(collection);
    }

//  [C8] AcroForm

    /** signature value */
    public static final int SIGNATURE_EXISTS = 1;
    /** signature value */
    public static final int SIGNATURE_APPEND_ONLY = 2;

    /** @see com.lowagie.text.pdf.interfaces.PdfAnnotations#getAcroForm() */
    public PdfAcroForm getAcroForm() {
        return pdf.getAcroForm();
    }

    /** @see com.lowagie.text.pdf.interfaces.PdfAnnotations#addAnnotation(com.lowagie.text.pdf.PdfAnnotation) */
    public void addAnnotation(PdfAnnotation annot) {
        pdf.addAnnotation(annot);
    }

    void addAnnotation(PdfAnnotation annot, int page) {
        addAnnotation(annot);
    }

    /** @see com.lowagie.text.pdf.interfaces.PdfAnnotations#addCalculationOrder(com.lowagie.text.pdf.PdfFormField) */
    public void addCalculationOrder(PdfFormField annot) {
        pdf.addCalculationOrder(annot);
    }

    /** @see com.lowagie.text.pdf.interfaces.PdfAnnotations#setSigFlags(int) */
    public void setSigFlags(int f) {
        pdf.setSigFlags(f);
    }

//  [C9] Metadata

    /** XMP Metadata for the document. */
    protected byte[] xmpMetadata = null;

    /**
     * Use this method to set the XMP Metadata.
     * @param xmpMetadata The xmpMetadata to set.
     */
    public void setXmpMetadata(byte[] xmpMetadata) {
        this.xmpMetadata = xmpMetadata;
    }

    /**
     * Use this method to set the XMP Metadata for each page.
     * @param xmpMetadata The xmpMetadata to set.
     */
    public void setPageXmpMetadata(byte[] xmpMetadata) {
        pdf.setXmpMetadata(xmpMetadata);
    }

    /**
     * Use this method to creates XMP Metadata based
     * on the metadata in the PdfDocument.
     */
    public void createXmpMetadata() {
        setXmpMetadata(createXmpMetadataBytes());
    }

    /**
     * @return an XmpMetadata byte array
     */
    private byte[] createXmpMetadataBytes() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            XmpWriter xmp = new XmpWriter(baos, pdf.getInfo(), pdfxConformance.getPDFXConformance());
            xmp.close();
        }
        catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return baos.toByteArray();
    }

//  [C10] PDFX Conformance
    /** A PDF/X level. */
    public static final int PDFXNONE = 0;
    /** A PDF/X level. */
    public static final int PDFX1A2001 = 1;
    /** A PDF/X level. */
    public static final int PDFX32002 = 2;
    /** PDFA-1A level. */
    public static final int PDFA1A = 3;
    /** PDFA-1B level. */
    public static final int PDFA1B = 4;

    /** Stores the PDF/X level. */
    private PdfXConformanceImp pdfxConformance = new PdfXConformanceImp();

    /** @see com.lowagie.text.pdf.interfaces.PdfXConformance#setPDFXConformance(int) */
    public void setPDFXConformance(int pdfx) {
        if (pdfxConformance.getPDFXConformance() == pdfx)
            return;
        if (pdf.isOpen())
            throw new PdfXConformanceException(MessageLocalization.getComposedMessage("pdfx.conformance.can.only.be.set.before.opening.the.document"));
        if (crypto != null)
            throw new PdfXConformanceException(MessageLocalization.getComposedMessage("a.pdfx.conforming.document.cannot.be.encrypted"));
        if (pdfx == PDFA1A || pdfx == PDFA1B)
            setPdfVersion(VERSION_1_4);
        else if (pdfx != PDFXNONE)
            setPdfVersion(VERSION_1_3);
        pdfxConformance.setPDFXConformance(pdfx);
    }

    /** @see com.lowagie.text.pdf.interfaces.PdfXConformance#getPDFXConformance() */
    public int getPDFXConformance() {
        return pdfxConformance.getPDFXConformance();
    }

    /** @see com.lowagie.text.pdf.interfaces.PdfXConformance#isPdfX() */
    public boolean isPdfX() {
        return pdfxConformance.isPdfX();
    }

//  [C11] Output intents
    /**
     * Sets the values of the output intent dictionary. Null values are allowed to
     * suppress any key.
     *
     * @param outputConditionIdentifier a value
     * @param outputCondition           a value, "PDFA/A" to force GTS_PDFA1, otherwise cued by pdfxConformance.
     * @param registryName              a value
     * @param info                      a value
     * @param colorProfile              a value
     * @since 2.1.5
     * @throws IOException on error
     */
    public void setOutputIntents(String outputConditionIdentifier, String outputCondition, String registryName, String info, ICC_Profile colorProfile) throws IOException {
        getExtraCatalog();
        PdfDictionary out = new PdfDictionary(PdfName.OUTPUTINTENT);
        if (outputCondition != null)
            out.put(PdfName.OUTPUTCONDITION, new PdfString(outputCondition, PdfObject.TEXT_UNICODE));
        if (outputConditionIdentifier != null)
            out.put(PdfName.OUTPUTCONDITIONIDENTIFIER, new PdfString(outputConditionIdentifier, PdfObject.TEXT_UNICODE));
        if (registryName != null)
            out.put(PdfName.REGISTRYNAME, new PdfString(registryName, PdfObject.TEXT_UNICODE));
        if (info != null)
            out.put(PdfName.INFO, new PdfString(info, PdfObject.TEXT_UNICODE));
        if (colorProfile != null) {
            PdfStream stream = new PdfICCBased(colorProfile, compressionLevel);
            out.put(PdfName.DESTOUTPUTPROFILE, addToBody(stream).getIndirectReference());
        }

        PdfName intentSubtype;
        if (pdfxConformance.isPdfA1() || "PDFA/1".equals(outputCondition)) {
            intentSubtype = PdfName.GTS_PDFA1;
        }
        else {
            intentSubtype = PdfName.GTS_PDFX;
        }

        out.put(PdfName.S, intentSubtype);

        extraCatalog.put(PdfName.OUTPUTINTENTS, new PdfArray(out));
    }

   /**
     * Sets the values of the output intent dictionary. Null values are allowed to
     * suppress any key.
     *
     * Prefer the <CODE>ICC_Profile</CODE>-based version of this method.
     * @param outputConditionIdentifier a value
     * @param outputCondition           a value, "PDFA/A" to force GTS_PDFA1, otherwise cued by pdfxConformance.
     * @param registryName              a value
     * @param info                      a value
     * @param destOutputProfile         a value
     * @since 1.x
     *
     * @throws IOException
     */
    public void setOutputIntents(String outputConditionIdentifier, String outputCondition, String registryName, String info, byte destOutputProfile[]) throws IOException {
        ICC_Profile colorProfile = (destOutputProfile == null) ? null : ICC_Profile.getInstance(destOutputProfile);
        setOutputIntents(outputConditionIdentifier, outputCondition, registryName, info, colorProfile);
    }


    /**
     * Use this method to copy the output intent dictionary
     * from another document to this one.
     * @param reader the other document
     * @param checkExistence <CODE>true</CODE> to just check for the existence of a valid output intent
     * dictionary, <CODE>false</CODE> to insert the dictionary if it exists
     * @throws IOException on error
     * @return <CODE>true</CODE> if the output intent dictionary exists, <CODE>false</CODE>
     * otherwise
     */
    public boolean setOutputIntents(PdfReader reader, boolean checkExistence) throws IOException {
        PdfDictionary catalog = reader.getCatalog();
        PdfArray outs = catalog.getAsArray(PdfName.OUTPUTINTENTS);
        if (outs == null)
            return false;
        if (outs.isEmpty())
            return false;
        PdfDictionary out = outs.getAsDict(0);
        PdfObject obj = PdfReader.getPdfObject(out.get(PdfName.S));
        if (obj == null || !PdfName.GTS_PDFX.equals(obj))
            return false;
        if (checkExistence)
            return true;
        PRStream stream = (PRStream)PdfReader.getPdfObject(out.get(PdfName.DESTOUTPUTPROFILE));
        byte destProfile[] = null;
        if (stream != null) {
            destProfile = PdfReader.getStreamBytes(stream);
        }
        setOutputIntents(getNameString(out, PdfName.OUTPUTCONDITIONIDENTIFIER), getNameString(out, PdfName.OUTPUTCONDITION),
            getNameString(out, PdfName.REGISTRYNAME), getNameString(out, PdfName.INFO), destProfile);
        return true;
    }

    private static String getNameString(PdfDictionary dic, PdfName key) {
        PdfObject obj = PdfReader.getPdfObject(dic.get(key));
        if (obj == null || !obj.isString())
            return null;
        return ((PdfString)obj).toUnicodeString();
    }

// PDF Objects that have an impact on the PDF body

//  [F1] PdfEncryptionSettings interface

    // types of encryption

    /** Type of encryption */
    public static final int STANDARD_ENCRYPTION_40 = 0;
    /** Type of encryption */
    public static final int STANDARD_ENCRYPTION_128 = 1;
    /** Type of encryption */
    public static final int ENCRYPTION_AES_128 = 2;
    /** Mask to separate the encryption type from the encryption mode. */
    static final int ENCRYPTION_MASK = 7;
    /** Add this to the mode to keep the metadata in clear text */
    public static final int DO_NOT_ENCRYPT_METADATA = 8;
    /**
     * Add this to the mode to keep encrypt only the embedded files.
     * @since 2.1.3
     */
    public static final int EMBEDDED_FILES_ONLY = 24;

    // permissions

    /** The operation permitted when the document is opened with the user password
     *
     * @since 2.0.7
     */
    public static final int ALLOW_PRINTING = 4 + 2048;

    /** The operation permitted when the document is opened with the user password
     *
     * @since 2.0.7
     */
    public static final int ALLOW_MODIFY_CONTENTS = 8;

    /** The operation permitted when the document is opened with the user password
     *
     * @since 2.0.7
     */
    public static final int ALLOW_COPY = 16;

    /** The operation permitted when the document is opened with the user password
     *
     * @since 2.0.7
     */
    public static final int ALLOW_MODIFY_ANNOTATIONS = 32;

    /** The operation permitted when the document is opened with the user password
     *
     * @since 2.0.7
     */
    public static final int ALLOW_FILL_IN = 256;

    /** The operation permitted when the document is opened with the user password
     *
     * @since 2.0.7
     */
    public static final int ALLOW_SCREENREADERS = 512;

    /** The operation permitted when the document is opened with the user password
     *
     * @since 2.0.7
     */
    public static final int ALLOW_ASSEMBLY = 1024;

    /** The operation permitted when the document is opened with the user password
     *
     * @since 2.0.7
     */
    public static final int ALLOW_DEGRADED_PRINTING = 4;

    /** @deprecated As of iText 2.0.7, use {@link #ALLOW_PRINTING} instead. Scheduled for removal at or after 2.2.0 */
    public static final int AllowPrinting = ALLOW_PRINTING;
    /** @deprecated As of iText 2.0.7, use {@link #ALLOW_MODIFY_CONTENTS} instead. Scheduled for removal at or after 2.2.0 */
    public static final int AllowModifyContents = ALLOW_MODIFY_CONTENTS;
    /** @deprecated As of iText 2.0.7, use {@link #ALLOW_COPY} instead. Scheduled for removal at or after 2.2.0 */
    public static final int AllowCopy = ALLOW_COPY;
    /** @deprecated As of iText 2.0.7, use {@link #ALLOW_MODIFY_ANNOTATIONS} instead. Scheduled for removal at or after 2.2.0 */
    public static final int AllowModifyAnnotations = ALLOW_MODIFY_ANNOTATIONS;
    /** @deprecated As of iText 2.0.7, use {@link #ALLOW_FILL_IN} instead. Scheduled for removal at or after 2.2.0 */
    public static final int AllowFillIn = ALLOW_FILL_IN;
    /** @deprecated As of iText 2.0.7, use {@link #ALLOW_SCREENREADERS} instead. Scheduled for removal at or after 2.2.0 */
    public static final int AllowScreenReaders = ALLOW_SCREENREADERS;
    /** @deprecated As of iText 2.0.7, use {@link #ALLOW_ASSEMBLY} instead. Scheduled for removal at or after 2.2.0 */
    public static final int AllowAssembly = ALLOW_ASSEMBLY;
    /** @deprecated As of iText 2.0.7, use {@link #ALLOW_DEGRADED_PRINTING} instead. Scheduled for removal at or after 2.2.0 */
    public static final int AllowDegradedPrinting = ALLOW_DEGRADED_PRINTING;

    // Strength of the encryption (kept for historical reasons)
    /** @deprecated As of iText 2.0.7, use {@link #STANDARD_ENCRYPTION_40} instead. Scheduled for removal at or after 2.2.0 */
    public static final boolean STRENGTH40BITS = false;
    /** @deprecated As of iText 2.0.7, use {@link #STANDARD_ENCRYPTION_128} instead. Scheduled for removal at or after 2.2.0 */
    public static final boolean STRENGTH128BITS = true;

    /** Contains the business logic for cryptography. */
    protected PdfEncryption crypto;
    PdfEncryption getEncryption() {
        return crypto;
    }

    /** @see com.lowagie.text.pdf.interfaces.PdfEncryptionSettings#setEncryption(byte[], byte[], int, int) */
    public void setEncryption(byte userPassword[], byte ownerPassword[], int permissions, int encryptionType) throws DocumentException {
        if (pdf.isOpen())
            throw new DocumentException(MessageLocalization.getComposedMessage("encryption.can.only.be.added.before.opening.the.document"));
        crypto = new PdfEncryption();
        crypto.setCryptoMode(encryptionType, 0);
        crypto.setupAllKeys(userPassword, ownerPassword, permissions);
    }

    /** @see com.lowagie.text.pdf.interfaces.PdfEncryptionSettings#setEncryption(java.security.cert.Certificate[], int[], int) */
    public void setEncryption(Certificate[] certs, int[] permissions, int encryptionType) throws DocumentException {
        if (pdf.isOpen())
            throw new DocumentException(MessageLocalization.getComposedMessage("encryption.can.only.be.added.before.opening.the.document"));
        crypto = new PdfEncryption();
        if (certs != null) {
            for (int i=0; i < certs.length; i++) {
                crypto.addRecipient(certs[i], permissions[i]);
            }
        }
        crypto.setCryptoMode(encryptionType, 0);
        crypto.getEncryptionDictionary();
    }

    /**
     * Sets the encryption options for this document. The userPassword and the
     *  ownerPassword can be null or have zero length. In this case the ownerPassword
     *  is replaced by a random string. The open permissions for the document can be
     *  AllowPrinting, AllowModifyContents, AllowCopy, AllowModifyAnnotations,
     *  AllowFillIn, AllowScreenReaders, AllowAssembly and AllowDegradedPrinting.
     *  The permissions can be combined by ORing them.
     * @param userPassword the user password. Can be null or empty
     * @param ownerPassword the owner password. Can be null or empty
     * @param permissions the user permissions
     * @param strength128Bits <code>true</code> for 128 bit key length, <code>false</code> for 40 bit key length
     * @throws DocumentException if the document is already open
     * @deprecated As of iText 2.0.3, replaced by (@link #setEncryption(byte[], byte[], int, int)}. Scheduled for removal at or after 2.2.0
     */
    public void setEncryption(byte userPassword[], byte ownerPassword[], int permissions, boolean strength128Bits) throws DocumentException {
        setEncryption(userPassword, ownerPassword, permissions, strength128Bits ? STANDARD_ENCRYPTION_128 : STANDARD_ENCRYPTION_40);
    }

    /**
     * Sets the encryption options for this document. The userPassword and the
     *  ownerPassword can be null or have zero length. In this case the ownerPassword
     *  is replaced by a random string. The open permissions for the document can be
     *  AllowPrinting, AllowModifyContents, AllowCopy, AllowModifyAnnotations,
     *  AllowFillIn, AllowScreenReaders, AllowAssembly and AllowDegradedPrinting.
     *  The permissions can be combined by ORing them.
     * @param strength <code>true</code> for 128 bit key length, <code>false</code> for 40 bit key length
     * @param userPassword the user password. Can be null or empty
     * @param ownerPassword the owner password. Can be null or empty
     * @param permissions the user permissions
     * @throws DocumentException if the document is already open
     * @deprecated As of iText 2.0.3, replaced by (@link #setEncryption(byte[], byte[], int, int)}. Scheduled for removal at or after 2.2.0
     */
    public void setEncryption(boolean strength, String userPassword, String ownerPassword, int permissions) throws DocumentException {
        setEncryption(getISOBytes(userPassword), getISOBytes(ownerPassword), permissions, strength ? STANDARD_ENCRYPTION_128 : STANDARD_ENCRYPTION_40);
    }

    /**
     * Sets the encryption options for this document. The userPassword and the
     *  ownerPassword can be null or have zero length. In this case the ownerPassword
     *  is replaced by a random string. The open permissions for the document can be
     *  AllowPrinting, AllowModifyContents, AllowCopy, AllowModifyAnnotations,
     *  AllowFillIn, AllowScreenReaders, AllowAssembly and AllowDegradedPrinting.
     *  The permissions can be combined by ORing them.
     * @param encryptionType the type of encryption. It can be one of STANDARD_ENCRYPTION_40, STANDARD_ENCRYPTION_128 or ENCRYPTION_AES128.
     * Optionally DO_NOT_ENCRYPT_METADATA can be ored to output the metadata in cleartext
     * @param userPassword the user password. Can be null or empty
     * @param ownerPassword the owner password. Can be null or empty
     * @param permissions the user permissions
     * @throws DocumentException if the document is already open
     * @deprecated As of iText 2.0.3, replaced by (@link #setEncryption(byte[], byte[], int, int)}. Scheduled for removal at or after 2.2.0
     */
    public void setEncryption(int encryptionType, String userPassword, String ownerPassword, int permissions) throws DocumentException {
        setEncryption(getISOBytes(userPassword), getISOBytes(ownerPassword), permissions, encryptionType);
    }

//  [F2] compression

    /** Holds value of property fullCompression. */
    protected boolean fullCompression = false;

    /**
     * Use this method to find out if 1.5 compression is on.
     * @return the 1.5 compression status
     */
    public boolean isFullCompression() {
        return this.fullCompression;
    }

    /**
     * Use this method to set the document's compression to the
     * PDF 1.5 mode with object streams and xref streams.
     * It can be set at any time but once set it can't be unset.
     * <p>
     * If set before opening the document it will also set the pdf version to 1.5.
     */
    public void setFullCompression() {
        this.fullCompression = true;
        setAtLeastPdfVersion(VERSION_1_5);
    }

    /**
     * The compression level of the content streams.
     * @since 2.1.3
     */
    protected int compressionLevel = PdfStream.DEFAULT_COMPRESSION;

    /**
     * Returns the compression level used for streams written by this writer.
     * @return the compression level (0 = best speed, 9 = best compression, -1 is default)
     * @since 2.1.3
     */
    public int getCompressionLevel() {
        return compressionLevel;
    }

    /**
     * Sets the compression level to be used for streams written by this writer.
     * @param compressionLevel a value between 0 (best speed) and 9 (best compression)
     * @since 2.1.3
     */
    public void setCompressionLevel(int compressionLevel) {
        if (compressionLevel < PdfStream.NO_COMPRESSION || compressionLevel > PdfStream.BEST_COMPRESSION)
            this.compressionLevel = PdfStream.DEFAULT_COMPRESSION;
        else
            this.compressionLevel = compressionLevel;
    }

//  [F3] adding fonts

    /** The fonts of this document */
    protected LinkedHashMap documentFonts = new LinkedHashMap();

    /** The font number counter for the fonts in the document. */
    protected int fontNumber = 1;

    /**
     * Adds a <CODE>BaseFont</CODE> to the document but not to the page resources.
     * It is used for templates.
     * @param bf the <CODE>BaseFont</CODE> to add
     * @return an <CODE>Object[]</CODE> where position 0 is a <CODE>PdfName</CODE>
     * and position 1 is an <CODE>PdfIndirectReference</CODE>
     */

    FontDetails addSimple(BaseFont bf) {
        if (bf.getFontType() == BaseFont.FONT_TYPE_DOCUMENT) {
            return new FontDetails(new PdfName("F" + (fontNumber++)), ((DocumentFont)bf).getIndirectReference(), bf);
        }
        FontDetails ret = (FontDetails)documentFonts.get(bf);
        if (ret == null) {
            PdfXConformanceImp.checkPDFXConformance(this, PdfXConformanceImp.PDFXKEY_FONT, bf);
            ret = new FontDetails(new PdfName("F" + (fontNumber++)), body.getPdfIndirectReference(), bf);
            documentFonts.put(bf, ret);
        }
        return ret;
    }

    void eliminateFontSubset(PdfDictionary fonts) {
        for (Iterator it = documentFonts.values().iterator(); it.hasNext();) {
            FontDetails ft = (FontDetails)it.next();
            if (fonts.get(ft.getFontName()) != null)
                ft.setSubset(false);
        }
    }

//  [F4] adding (and releasing) form XObjects

    /** The form XObjects in this document. The key is the xref and the value
        is Object[]{PdfName, template}.*/
    protected HashMap formXObjects = new HashMap();

    /** The name counter for the form XObjects name. */
    protected int formXObjectsCounter = 1;

    /**
     * Adds a template to the document but not to the page resources.
     * @param template the template to add
     * @param forcedName the template name, rather than a generated one. Can be null
     * @return the <CODE>PdfName</CODE> for this template
     */

    PdfName addDirectTemplateSimple(PdfTemplate template, PdfName forcedName) {
        PdfIndirectReference ref = template.getIndirectReference();
        Object obj[] = (Object[])formXObjects.get(ref);
        PdfName name = null;
        try {
            if (obj == null) {
                if (forcedName == null) {
                    name = new PdfName("Xf" + formXObjectsCounter);
                    ++formXObjectsCounter;
                }
                else
                    name = forcedName;
                if (template.getType() == PdfTemplate.TYPE_IMPORTED) {
                    // If we got here from PdfCopy we'll have to fill importedPages
                    PdfImportedPage ip = (PdfImportedPage)template;
                    PdfReader r = ip.getPdfReaderInstance().getReader();
                    if (!importedPages.containsKey(r)) {
                        importedPages.put(r, ip.getPdfReaderInstance());
                    }
                    template = null;
                }
                formXObjects.put(ref, new Object[]{name, template});
            }
            else
                name = (PdfName)obj[0];
        }
        catch (Exception e) {
            throw new ExceptionConverter(e);
        }
        return name;
    }

    /**
     * Use this method to releases the memory used by a template.
     * This method writes the template to the output.
     * The template can still be added to any content
     * but changes to the template itself won't have any effect.
     * @param tp the template to release
     * @throws IOException on error
     */
    public void releaseTemplate(PdfTemplate tp) throws IOException {
        PdfIndirectReference ref = tp.getIndirectReference();
        Object[] objs = (Object[])formXObjects.get(ref);
        if (objs == null || objs[1] == null)
            return;
        PdfTemplate template = (PdfTemplate)objs[1];
        if (template.getIndirectReference() instanceof PRIndirectReference)
            return;
        if (template.getType() == PdfTemplate.TYPE_TEMPLATE) {
            addToBody(template.getFormXObject(compressionLevel), template.getIndirectReference());
            objs[1] = null;
        }
    }

//  [F5] adding pages imported form other PDF documents

    protected HashMap importedPages = new HashMap();

    /**
     * Use this method to get a page from other PDF document.
     * The page can be used as any other PdfTemplate.
     * Note that calling this method more than once with the same parameters
     * will retrieve the same object.
     * @param reader the PDF document where the page is
     * @param pageNumber the page number. The first page is 1
     * @return the template representing the imported page
     */
    public PdfImportedPage getImportedPage(PdfReader reader, int pageNumber) {
        PdfReaderInstance inst = (PdfReaderInstance)importedPages.get(reader);
        if (inst == null) {
            inst = reader.getPdfReaderInstance(this);
            importedPages.put(reader, inst);
        }
        return inst.getImportedPage(pageNumber);
    }

    /**
     * Use this method to writes the reader to the document
     * and free the memory used by it.
     * The main use is when concatenating multiple documents
     * to keep the memory usage restricted to the current
     * appending document.
     * @param reader the <CODE>PdfReader</CODE> to free
     * @throws IOException on error
     */
    public void freeReader(PdfReader reader) throws IOException {
        currentPdfReaderInstance = (PdfReaderInstance)importedPages.get(reader);
        if (currentPdfReaderInstance == null)
            return;
        currentPdfReaderInstance.writeAllPages();
        currentPdfReaderInstance = null;
        importedPages.remove(reader);
    }

    /**
     * Use this method to gets the current document size.
     * This size only includes the data already written
     * to the output stream, it does not include templates or fonts.
     * It is useful if used with <CODE>freeReader()</CODE>
     * when concatenating many documents and an idea of
     * the current size is needed.
     * @return the approximate size without fonts or templates
     */
    public int getCurrentDocumentSize() {
        return body.offset() + body.size() * 20 + 0x48;
    }

    protected PdfReaderInstance currentPdfReaderInstance;

    protected int getNewObjectNumber(PdfReader reader, int number, int generation) {
        return currentPdfReaderInstance.getNewObjectNumber(number, generation);
    }

    RandomAccessFileOrArray getReaderFile(PdfReader reader) {
        return currentPdfReaderInstance.getReaderFile();
    }

//  [F6] spot colors

    /** The colors of this document */
    protected HashMap documentColors = new HashMap();

    /** The color number counter for the colors in the document. */
    protected int colorNumber = 1;

    PdfName getColorspaceName() {
        return new PdfName("CS" + (colorNumber++));
    }

    /**
     * Adds a <CODE>SpotColor</CODE> to the document but not to the page resources.
     * @param spc the <CODE>SpotColor</CODE> to add
     * @return an <CODE>Object[]</CODE> where position 0 is a <CODE>PdfName</CODE>
     * and position 1 is an <CODE>PdfIndirectReference</CODE>
     */
    ColorDetails addSimple(PdfSpotColor spc) {
        ColorDetails ret = (ColorDetails)documentColors.get(spc);
        if (ret == null) {
            ret = new ColorDetails(getColorspaceName(), body.getPdfIndirectReference(), spc);
            documentColors.put(spc, ret);
        }
        return ret;
    }

//  [F7] document patterns

    /** The patterns of this document */
    protected HashMap documentPatterns = new HashMap();

    /** The pattern number counter for the colors in the document. */
    protected int patternNumber = 1;

    PdfName addSimplePattern(PdfPatternPainter painter) {
        PdfName name = (PdfName)documentPatterns.get(painter);
        try {
            if ( name == null ) {
                name = new PdfName("P" + patternNumber);
                ++patternNumber;
                documentPatterns.put(painter, name);
            }
        } catch (Exception e) {
            throw new ExceptionConverter(e);
        }
        return name;
    }

//  [F8] shading patterns

    protected HashMap documentShadingPatterns = new HashMap();

    void addSimpleShadingPattern(PdfShadingPattern shading) {
        if (!documentShadingPatterns.containsKey(shading)) {
            shading.setName(patternNumber);
            ++patternNumber;
            documentShadingPatterns.put(shading, null);
            addSimpleShading(shading.getShading());
        }
    }

//  [F9] document shadings

    protected HashMap documentShadings = new HashMap();

    void addSimpleShading(PdfShading shading) {
        if (!documentShadings.containsKey(shading)) {
            documentShadings.put(shading, null);
            shading.setName(documentShadings.size());
        }
    }

// [F10] extended graphics state (for instance for transparency)

    protected HashMap documentExtGState = new HashMap();

    PdfObject[] addSimpleExtGState(PdfDictionary gstate) {
        if (!documentExtGState.containsKey(gstate)) {
            PdfXConformanceImp.checkPDFXConformance(this, PdfXConformanceImp.PDFXKEY_GSTATE, gstate);
            documentExtGState.put(gstate, new PdfObject[]{new PdfName("GS" + (documentExtGState.size() + 1)), getPdfIndirectReference()});
        }
        return (PdfObject[])documentExtGState.get(gstate);
    }

//  [F11] adding properties (OCG, marked content)

    protected HashMap documentProperties = new HashMap();
    PdfObject[] addSimpleProperty(Object prop, PdfIndirectReference refi) {
        if (!documentProperties.containsKey(prop)) {
            if (prop instanceof PdfOCG)
            	PdfXConformanceImp.checkPDFXConformance(this, PdfXConformanceImp.PDFXKEY_LAYER, null);
            documentProperties.put(prop, new PdfObject[]{new PdfName("Pr" + (documentProperties.size() + 1)), refi});
        }
        return (PdfObject[])documentProperties.get(prop);
    }

    boolean propertyExists(Object prop) {
        return documentProperties.containsKey(prop);
    }

//  [F12] tagged PDF

    protected boolean tagged = false;
    protected PdfStructureTreeRoot structureTreeRoot;

    /**
     * Mark this document for tagging. It must be called before open.
     */
    public void setTagged() {
        if (open)
            throw new IllegalArgumentException(MessageLocalization.getComposedMessage("tagging.must.be.set.before.opening.the.document"));
        tagged = true;
    }

    /**
     * Check if the document is marked for tagging.
     * @return <CODE>true</CODE> if the document is marked for tagging
     */
    public boolean isTagged() {
        return tagged;
    }

    /**
     * Gets the structure tree root. If the document is not marked for tagging it will return <CODE>null</CODE>.
     * @return the structure tree root
     */
    public PdfStructureTreeRoot getStructureTreeRoot() {
        if (tagged && structureTreeRoot == null)
            structureTreeRoot = new PdfStructureTreeRoot(this);
        return structureTreeRoot;
    }

//  [F13] Optional Content Groups
    /** A hashSet containing all the PdfLayer objects. */
    protected HashSet documentOCG = new HashSet();
    /** An array list used to define the order of an OCG tree. */
    protected ArrayList documentOCGorder = new ArrayList();
    /** The OCProperties in a catalog dictionary. */
    protected PdfOCProperties OCProperties;
    /** The RBGroups array in an OCG dictionary */
    protected PdfArray OCGRadioGroup = new PdfArray();
    /**
     * The locked array in an OCG dictionary
     * @since   2.1.2
     */
    protected PdfArray OCGLocked = new PdfArray();

    /**
     * Use this method to get the <B>Optional Content Properties Dictionary</B>.
     * Each call fills the dictionary with the current layer state.
     * It's advisable to only call this method right before close
     * and do any modifications at that time.
     * @return the Optional Content Properties Dictionary
     */
    public PdfOCProperties getOCProperties() {
        fillOCProperties(true);
        return OCProperties;
    }

    /**
     * Use this method to set a collection of optional content groups
     * whose states are intended to follow a "radio button" paradigm.
     * That is, the state of at most one optional content group
     * in the array should be ON at a time: if one group is turned
     * ON, all others must be turned OFF.
     * @param group the radio group
     */
    public void addOCGRadioGroup(ArrayList group) {
        PdfArray ar = new PdfArray();
        for (int k = 0; k < group.size(); ++k) {
            PdfLayer layer = (PdfLayer)group.get(k);
            if (layer.getTitle() == null)
                ar.add(layer.getRef());
        }
        if (ar.size() == 0)
            return;
        OCGRadioGroup.add(ar);
    }

    /**
     * Use this method to lock an optional content group.
     * The state of a locked group cannot be changed through the user interface
     * of a viewer application. Producers can use this entry to prevent the visibility
     * of content that depends on these groups from being changed by users.
     * @param layer	the layer that needs to be added to the array of locked OCGs
     * @since	2.1.2
     */
    public void lockLayer(PdfLayer layer) {
        OCGLocked.add(layer.getRef());
    }

    private static void getOCGOrder(PdfArray order, PdfLayer layer) {
        if (!layer.isOnPanel())
            return;
        if (layer.getTitle() == null)
            order.add(layer.getRef());
        ArrayList children = layer.getChildren();
        if (children == null)
            return;
        PdfArray kids = new PdfArray();
        if (layer.getTitle() != null)
            kids.add(new PdfString(layer.getTitle(), PdfObject.TEXT_UNICODE));
        for (int k = 0; k < children.size(); ++k) {
            getOCGOrder(kids, (PdfLayer)children.get(k));
        }
        if (kids.size() > 0)
            order.add(kids);
    }

    private void addASEvent(PdfName event, PdfName category) {
        PdfArray arr = new PdfArray();
        for (Iterator it = documentOCG.iterator(); it.hasNext();) {
            PdfLayer layer = (PdfLayer)it.next();
            PdfDictionary usage = (PdfDictionary)layer.get(PdfName.USAGE);
            if (usage != null && usage.get(category) != null)
                arr.add(layer.getRef());
        }
        if (arr.size() == 0)
            return;
        PdfDictionary d = (PdfDictionary)OCProperties.get(PdfName.D);
        PdfArray arras = (PdfArray)d.get(PdfName.AS);
        if (arras == null) {
            arras = new PdfArray();
            d.put(PdfName.AS, arras);
        }
        PdfDictionary as = new PdfDictionary();
        as.put(PdfName.EVENT, event);
        as.put(PdfName.CATEGORY, new PdfArray(category));
        as.put(PdfName.OCGS, arr);
        arras.add(as);
    }

    /**
     * @since 2.1.2
     */
    protected void fillOCProperties(boolean erase) {
        if (OCProperties == null)
            OCProperties = new PdfOCProperties();
        if (erase) {
            OCProperties.remove(PdfName.OCGS);
            OCProperties.remove(PdfName.D);
        }
        if (OCProperties.get(PdfName.OCGS) == null) {
            PdfArray gr = new PdfArray();
            for (Iterator it = documentOCG.iterator(); it.hasNext();) {
                PdfLayer layer = (PdfLayer)it.next();
                gr.add(layer.getRef());
            }
            OCProperties.put(PdfName.OCGS, gr);
        }
        if (OCProperties.get(PdfName.D) != null)
            return;
        ArrayList docOrder = new ArrayList(documentOCGorder);
        for (Iterator it = docOrder.iterator(); it.hasNext();) {
            PdfLayer layer = (PdfLayer)it.next();
            if (layer.getParent() != null)
                it.remove();
        }
        PdfArray order = new PdfArray();
        for (Iterator it = docOrder.iterator(); it.hasNext();) {
            PdfLayer layer = (PdfLayer)it.next();
            getOCGOrder(order, layer);
        }
        PdfDictionary d = new PdfDictionary();
        OCProperties.put(PdfName.D, d);
        d.put(PdfName.ORDER, order);
        PdfArray gr = new PdfArray();
        for (Iterator it = documentOCG.iterator(); it.hasNext();) {
            PdfLayer layer = (PdfLayer)it.next();
            if (!layer.isOn())
                gr.add(layer.getRef());
        }
        if (gr.size() > 0)
            d.put(PdfName.OFF, gr);
        if (OCGRadioGroup.size() > 0)
            d.put(PdfName.RBGROUPS, OCGRadioGroup);
        if (OCGLocked.size() > 0)
            d.put(PdfName.LOCKED, OCGLocked);
        addASEvent(PdfName.VIEW, PdfName.ZOOM);
        addASEvent(PdfName.VIEW, PdfName.VIEW);
        addASEvent(PdfName.PRINT, PdfName.PRINT);
        addASEvent(PdfName.EXPORT, PdfName.EXPORT);
        d.put(PdfName.LISTMODE, PdfName.VISIBLEPAGES);
    }

    void registerLayer(PdfOCG layer) {
        PdfXConformanceImp.checkPDFXConformance(this, PdfXConformanceImp.PDFXKEY_LAYER, null);
        if (layer instanceof PdfLayer) {
            PdfLayer la = (PdfLayer)layer;
            if (la.getTitle() == null) {
                if (!documentOCG.contains(layer)) {
                    documentOCG.add(layer);
                    documentOCGorder.add(layer);
                }
            }
            else {
                documentOCGorder.add(layer);
            }
        }
        else
            throw new IllegalArgumentException(MessageLocalization.getComposedMessage("only.pdflayer.is.accepted"));
    }

//  User methods to change aspects of the page

//  [U1] page size

    /**
     * Use this method to get the size of the media box.
     * @return a Rectangle
     */
    public Rectangle getPageSize() {
        return pdf.getPageSize();
    }

    /**
     * Use this method to set the crop box.
     * The crop box should not be rotated even if the page is rotated.
     * This change only takes effect in the next page.
     * @param crop the crop box
     */
    public void setCropBoxSize(Rectangle crop) {
        pdf.setCropBoxSize(crop);
    }

    /**
     * Use this method to set the page box sizes.
     * Allowed names are: "crop", "trim", "art" and "bleed".
     * @param boxName the box size
     * @param size the size
     */
    public void setBoxSize(String boxName, Rectangle size) {
        pdf.setBoxSize(boxName, size);
    }

    /**
     * Use this method to get the size of a trim, art, crop or bleed box,
     * or null if not defined.
     * @param boxName crop, trim, art or bleed
     */
    public Rectangle getBoxSize(String boxName) {
        return pdf.getBoxSize(boxName);
    }

//  [U2] take care of empty pages

    /**
     * Use this method to make sure a page is added,
     * even if it's empty. If you use setPageEmpty(false),
     * invoking newPage() after a blank page will add a newPage.
     * setPageEmpty(true) won't have any effect.
     * @param pageEmpty the state
     */
    public void setPageEmpty(boolean pageEmpty) {
        if (pageEmpty)
            return;
        pdf.setPageEmpty(pageEmpty);
    }

    /**
     * Checks if a newPage() will actually generate a new page.
     * @return true if a new page will be generated, false otherwise
     * @since 2.1.8
     */
    public boolean isPageEmpty() {
        return pdf.isPageEmpty();
    }

//  [U3] page actions (open and close)

    /** action value */
    public static final PdfName PAGE_OPEN = PdfName.O;
    /** action value */
    public static final PdfName PAGE_CLOSE = PdfName.C;

    /** @see com.lowagie.text.pdf.interfaces.PdfPageActions#setPageAction(com.lowagie.text.pdf.PdfName, com.lowagie.text.pdf.PdfAction) */
    public void setPageAction(PdfName actionType, PdfAction action) throws DocumentException {
          if (!actionType.equals(PAGE_OPEN) && !actionType.equals(PAGE_CLOSE))
              throw new DocumentException(MessageLocalization.getComposedMessage("invalid.page.additional.action.type.1", actionType.toString()));
          pdf.setPageAction(actionType, action);
      }

    /** @see com.lowagie.text.pdf.interfaces.PdfPageActions#setDuration(int) */
    public void setDuration(int seconds) {
         pdf.setDuration(seconds);
     }

    /** @see com.lowagie.text.pdf.interfaces.PdfPageActions#setTransition(com.lowagie.text.pdf.PdfTransition) */
    public void setTransition(PdfTransition transition) {
         pdf.setTransition(transition);
     }

//  [U4] Thumbnail image

    /**
     * Use this method to set the thumbnail image for the current page.
     * @param image the image
     * @throws PdfException on error
     * @throws DocumentException or error
     */
    public void setThumbnail(Image image) throws PdfException, DocumentException {
        pdf.setThumbnail(image);
    }

//  [U5] Transparency groups

    /**
     * A group attributes dictionary specifying the attributes
     * of the page's page group for use in the transparent
     * imaging model
     */
    protected PdfDictionary group;

    /**
     * Use this method to get the group dictionary.
     * @return Value of property group.
     */
    public PdfDictionary getGroup() {
        return this.group;
    }

    /**
     * Use this method to set the group dictionary.
     * @param group New value of property group.
     */
    public void setGroup(PdfDictionary group) {
        this.group = group;
    }

//  [U6] space char ratio

    /** The default space-char ratio. */
    public static final float SPACE_CHAR_RATIO_DEFAULT = 2.5f;
    /** Disable the inter-character spacing. */
    public static final float NO_SPACE_CHAR_RATIO = 10000000f;

    /**
     * The ratio between the extra word spacing and the extra character spacing.
     * Extra word spacing will grow <CODE>ratio</CODE> times more than extra character spacing.
     */
    private float spaceCharRatio = SPACE_CHAR_RATIO_DEFAULT;

    /**
     * Use this method to gets the space/character extra spacing ratio
     * for fully justified text.
     * @return the space/character extra spacing ratio
     */
    public float getSpaceCharRatio() {
        return spaceCharRatio;
    }

    /**
     * Use this method to set the ratio between the extra word spacing and
     * the extra character spacing when the text is fully justified.
     * Extra word spacing will grow <CODE>spaceCharRatio</CODE> times more
     * than extra character spacing. If the ratio is <CODE>PdfWriter.NO_SPACE_CHAR_RATIO</CODE>
     * then the extra character spacing will be zero.
     * @param spaceCharRatio the ratio between the extra word spacing and the extra character spacing
     */
    public void setSpaceCharRatio(float spaceCharRatio) {
        if (spaceCharRatio < 0.001f)
            this.spaceCharRatio = 0.001f;
        else
            this.spaceCharRatio = spaceCharRatio;
    }

//  [U7] run direction (doesn't actually do anything)

    /** Use the default run direction. */
    public static final int RUN_DIRECTION_DEFAULT = 0;
    /** Do not use bidirectional reordering. */
    public static final int RUN_DIRECTION_NO_BIDI = 1;
    /** Use bidirectional reordering with left-to-right
     * preferential run direction.
     */
    public static final int RUN_DIRECTION_LTR = 2;
    /** Use bidirectional reordering with right-to-left
     * preferential run direction.
     */
    public static final int RUN_DIRECTION_RTL = 3;

    protected int runDirection = RUN_DIRECTION_NO_BIDI;

    /**
     * Use this method to set the run direction.
     * This is only used as a placeholder as it does not affect anything.
     * @param runDirection the run direction
     */
    public void setRunDirection(int runDirection) {
        if (runDirection < RUN_DIRECTION_NO_BIDI || runDirection > RUN_DIRECTION_RTL)
            throw new RuntimeException(MessageLocalization.getComposedMessage("invalid.run.direction.1", runDirection));
        this.runDirection = runDirection;
    }

    /**
     * Use this method to set the run direction.
     * @return the run direction
     */
    public int getRunDirection() {
        return runDirection;
    }

//  [U8] user units

     protected float userunit = 0f;
    /**
     * Use this method to get the user unit.
     * A user unit is a value that defines the default user space unit.
     * The minimum UserUnit is 1 (1 unit = 1/72 inch).
     * The maximum UserUnit is 75,000.
     * Note that this userunit only works starting with PDF1.6!
     * @return Returns the userunit.
     */
    public float getUserunit() {
        return userunit;
    }
    /**
     * Use this method to set the user unit.
     * A UserUnit is a value that defines the default user space unit.
     * The minimum UserUnit is 1 (1 unit = 1/72 inch).
     * The maximum UserUnit is 75,000.
     * Note that this userunit only works starting with PDF1.6!
     * @param userunit The userunit to set.
     * @throws DocumentException on error
     */
     public void setUserunit(float userunit) throws DocumentException {
 		if (userunit < 1f || userunit > 75000f) throw new DocumentException(MessageLocalization.getComposedMessage("userunit.should.be.a.value.between.1.and.75000"));
         this.userunit = userunit;
         setAtLeastPdfVersion(VERSION_1_6);
     }

// Miscellaneous topics

//  [M1] Color settings

    protected PdfDictionary defaultColorspace = new PdfDictionary();
    /**
     * Use this method to get the default colorspaces.
     * @return the default colorspaces
     */
    public PdfDictionary getDefaultColorspace() {
        return defaultColorspace;
    }

    /**
     * Use this method to sets the default colorspace that will be applied
     * to all the document. The colorspace is only applied if another colorspace
     * with the same name is not present in the content.
     * <p>
     * The colorspace is applied immediately when creating templates and
     * at the page end for the main document content.
     * @param key the name of the colorspace. It can be <CODE>PdfName.DEFAULTGRAY</CODE>, <CODE>PdfName.DEFAULTRGB</CODE>
     * or <CODE>PdfName.DEFAULTCMYK</CODE>
     * @param cs the colorspace. A <CODE>null</CODE> or <CODE>PdfNull</CODE> removes any colorspace with the same name
     */
    public void setDefaultColorspace(PdfName key, PdfObject cs) {
        if (cs == null || cs.isNull())
            defaultColorspace.remove(key);
        defaultColorspace.put(key, cs);
    }

//  [M2] spot patterns

    protected HashMap documentSpotPatterns = new HashMap();
    protected ColorDetails patternColorspaceRGB;
    protected ColorDetails patternColorspaceGRAY;
    protected ColorDetails patternColorspaceCMYK;

    ColorDetails addSimplePatternColorspace(Color color) {
        int type = ExtendedColor.getType(color);
        if (type == ExtendedColor.TYPE_PATTERN || type == ExtendedColor.TYPE_SHADING)
            throw new RuntimeException(MessageLocalization.getComposedMessage("an.uncolored.tile.pattern.can.not.have.another.pattern.or.shading.as.color"));
        try {
            switch (type) {
                case ExtendedColor.TYPE_RGB:
                    if (patternColorspaceRGB == null) {
                        patternColorspaceRGB = new ColorDetails(getColorspaceName(), body.getPdfIndirectReference(), null);
                        PdfArray array = new PdfArray(PdfName.PATTERN);
                        array.add(PdfName.DEVICERGB);
                        addToBody(array, patternColorspaceRGB.getIndirectReference());
                    }
                    return patternColorspaceRGB;
                case ExtendedColor.TYPE_CMYK:
                    if (patternColorspaceCMYK == null) {
                        patternColorspaceCMYK = new ColorDetails(getColorspaceName(), body.getPdfIndirectReference(), null);
                        PdfArray array = new PdfArray(PdfName.PATTERN);
                        array.add(PdfName.DEVICECMYK);
                        addToBody(array, patternColorspaceCMYK.getIndirectReference());
                    }
                    return patternColorspaceCMYK;
                case ExtendedColor.TYPE_GRAY:
                    if (patternColorspaceGRAY == null) {
                        patternColorspaceGRAY = new ColorDetails(getColorspaceName(), body.getPdfIndirectReference(), null);
                        PdfArray array = new PdfArray(PdfName.PATTERN);
                        array.add(PdfName.DEVICEGRAY);
                        addToBody(array, patternColorspaceGRAY.getIndirectReference());
                    }
                    return patternColorspaceGRAY;
                case ExtendedColor.TYPE_SEPARATION: {
                    ColorDetails details = addSimple(((SpotColor)color).getPdfSpotColor());
                    ColorDetails patternDetails = (ColorDetails)documentSpotPatterns.get(details);
                    if (patternDetails == null) {
                        patternDetails = new ColorDetails(getColorspaceName(), body.getPdfIndirectReference(), null);
                        PdfArray array = new PdfArray(PdfName.PATTERN);
                        array.add(details.getIndirectReference());
                        addToBody(array, patternDetails.getIndirectReference());
                        documentSpotPatterns.put(details, patternDetails);
                    }
                    return patternDetails;
                }
                default:
                    throw new RuntimeException(MessageLocalization.getComposedMessage("invalid.color.type"));
            }
        }
        catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

//  [M3] Images

    /**
     * Use this method to get the strictImageSequence status.
     * @return value of property strictImageSequence
     */
    public boolean isStrictImageSequence() {
        return pdf.isStrictImageSequence();
    }

    /**
     * Use this method to set the image sequence, so that it follows
     * the text in strict order (or not).
     * @param strictImageSequence new value of property strictImageSequence
     *
     */
    public void setStrictImageSequence(boolean strictImageSequence) {
        pdf.setStrictImageSequence(strictImageSequence);
    }

    /**
     * Use this method to clear text wrapping around images (if applicable).
     * @throws DocumentException
     */
    public void clearTextWrap() throws DocumentException {
        pdf.clearTextWrap();
    }

    /** Dictionary, containing all the images of the PDF document */
    protected PdfDictionary imageDictionary = new PdfDictionary();

    /** This is the list with all the images in the document. */
    private HashMap images = new HashMap();

    /**
     * Use this method to adds an image to the document
     * but not to the page resources. It is used with
     * templates and <CODE>Document.add(Image)</CODE>.
     * Use this method only if you know what you're doing!
     * @param image the <CODE>Image</CODE> to add
     * @return the name of the image added
     * @throws PdfException on error
     * @throws DocumentException on error
     */
    public PdfName addDirectImageSimple(Image image) throws PdfException, DocumentException {
        return addDirectImageSimple(image, null);
    }

    /**
     * Adds an image to the document but not to the page resources.
     * It is used with templates and <CODE>Document.add(Image)</CODE>.
     * Use this method only if you know what you're doing!
     * @param image the <CODE>Image</CODE> to add
     * @param fixedRef the reference to used. It may be <CODE>null</CODE>,
     * a <CODE>PdfIndirectReference</CODE> or a <CODE>PRIndirectReference</CODE>.
     * @return the name of the image added
     * @throws PdfException on error
     * @throws DocumentException on error
     */
    public PdfName addDirectImageSimple(Image image, PdfIndirectReference fixedRef) throws PdfException, DocumentException {
        PdfName name;
        // if the images is already added, just retrieve the name
        if (images.containsKey(image.getMySerialId())) {
            name = (PdfName) images.get(image.getMySerialId());
        }
        // if it's a new image, add it to the document
        else {
            if (image.isImgTemplate()) {
                name = new PdfName("img" + images.size());
                if(image instanceof ImgWMF){
                    try {
                        ImgWMF wmf = (ImgWMF)image;
                        wmf.readWMF(PdfTemplate.createTemplate(this, 0, 0));
                    }
                    catch (Exception e) {
                        throw new DocumentException(e);
                    }
                }
            }
            else {
                PdfIndirectReference dref = image.getDirectReference();
                if (dref != null) {
                    PdfName rname = new PdfName("img" + images.size());
                    images.put(image.getMySerialId(), rname);
                    imageDictionary.put(rname, dref);
                    return rname;
                }
                Image maskImage = image.getImageMask();
                PdfIndirectReference maskRef = null;
                if (maskImage != null) {
                    PdfName mname = (PdfName)images.get(maskImage.getMySerialId());
                    maskRef = getImageReference(mname);
                }
                PdfImage i = new PdfImage(image, "img" + images.size(), maskRef);
                if (image instanceof ImgJBIG2) {
                    byte[] globals = ((ImgJBIG2) image).getGlobalBytes();
                    if (globals != null) {
                        PdfDictionary decodeparms = new PdfDictionary();
                        decodeparms.put(PdfName.JBIG2GLOBALS, getReferenceJBIG2Globals(globals));
                        i.put(PdfName.DECODEPARMS, decodeparms);
                    }
                }
                if (image.hasICCProfile()) {
                    PdfICCBased icc = new PdfICCBased(image.getICCProfile(), image.getCompressionLevel());
                    PdfIndirectReference iccRef = add(icc);
                    PdfArray iccArray = new PdfArray();
                    iccArray.add(PdfName.ICCBASED);
                    iccArray.add(iccRef);
                    PdfArray colorspace = i.getAsArray(PdfName.COLORSPACE);
                    if (colorspace != null) {
                        if (colorspace.size() > 1 && PdfName.INDEXED.equals(colorspace.getPdfObject(0)))
                            colorspace.set(1, iccArray);
                        else
                            i.put(PdfName.COLORSPACE, iccArray);
                    }
                    else
                        i.put(PdfName.COLORSPACE, iccArray);
                }
                add(i, fixedRef);
                name = i.name();
            }
            images.put(image.getMySerialId(), name);
        }
        return name;
    }

    /**
     * Writes a <CODE>PdfImage</CODE> to the outputstream.
     *
     * @param pdfImage the image to be added
     * @return a <CODE>PdfIndirectReference</CODE> to the encapsulated image
     * @throws PdfException when a document isn't open yet, or has been closed
     */

    PdfIndirectReference add(PdfImage pdfImage, PdfIndirectReference fixedRef) throws PdfException {
        if (! imageDictionary.contains(pdfImage.name())) {
            PdfXConformanceImp.checkPDFXConformance(this, PdfXConformanceImp.PDFXKEY_IMAGE, pdfImage);
            if (fixedRef instanceof PRIndirectReference) {
                PRIndirectReference r2 = (PRIndirectReference)fixedRef;
                fixedRef = new PdfIndirectReference(0, getNewObjectNumber(r2.getReader(), r2.getNumber(), r2.getGeneration()));
            }
            try {
                if (fixedRef == null)
                    fixedRef = addToBody(pdfImage).getIndirectReference();
                else
                    addToBody(pdfImage, fixedRef);
            }
            catch(IOException ioe) {
                throw new ExceptionConverter(ioe);
            }
            imageDictionary.put(pdfImage.name(), fixedRef);
            return fixedRef;
        }
        return (PdfIndirectReference) imageDictionary.get(pdfImage.name());
    }

    /**
     * return the <CODE>PdfIndirectReference</CODE> to the image with a given name.
     *
     * @param name the name of the image
     * @return a <CODE>PdfIndirectReference</CODE>
     */

    PdfIndirectReference getImageReference(PdfName name) {
        return (PdfIndirectReference) imageDictionary.get(name);
    }

    protected PdfIndirectReference add(PdfICCBased icc) {
        PdfIndirectObject object;
        try {
            object = addToBody(icc);
        }
        catch(IOException ioe) {
            throw new ExceptionConverter(ioe);
        }
        return object.getIndirectReference();
    }

    /**
     * A HashSet with Stream objects containing JBIG2 Globals
     * @since 2.1.5
     */
    protected HashMap JBIG2Globals = new HashMap();
    /**
     * Gets an indirect reference to a JBIG2 Globals stream.
     * Adds the stream if it hasn't already been added to the writer.
	 * @param	content a byte array that may already been added to the writer inside a stream object.
     * @since  2.1.5
     */
    protected PdfIndirectReference getReferenceJBIG2Globals(byte[] content) {
        if (content == null) return null;
        PdfStream stream;
        for (Iterator i = JBIG2Globals.keySet().iterator(); i.hasNext(); ) {
            stream = (PdfStream) i.next();
            if (Arrays.equals(content, stream.getBytes())) {
                return (PdfIndirectReference) JBIG2Globals.get(stream);
            }
        }
        stream = new PdfStream(content);
        PdfIndirectObject ref;
        try {
            ref = addToBody(stream);
        } catch (IOException e) {
            return null;
        }
        JBIG2Globals.put(stream, ref.getIndirectReference());
        return ref.getIndirectReference();
    }

//  [M4] Old table functionality; do we still need it?

    /**
     * Checks if a <CODE>Table</CODE> fits the current page of the <CODE>PdfDocument</CODE>.
     *
     * @param   table   the table that has to be checked
     * @param   margin  a certain margin
     * @return  <CODE>true</CODE> if the <CODE>Table</CODE> fits the page, <CODE>false</CODE> otherwise.
     */

    public boolean fitsPage(Table table, float margin) {
        return pdf.bottom(table) > pdf.indentBottom() + margin;
    }

    /**
     * Checks if a <CODE>Table</CODE> fits the current page of the <CODE>PdfDocument</CODE>.
     *
     * @param   table  the table that has to be checked
     * @return  <CODE>true</CODE> if the <CODE>Table</CODE> fits the page, <CODE>false</CODE> otherwise.
     */

    public boolean fitsPage(Table table) {
        return fitsPage(table, 0);
    }
//  [F12] tagged PDF
    /**
     * A flag indicating the presence of structure elements that contain user properties attributes.
     */
    private boolean userProperties;

    /**
     * Gets the flag indicating the presence of structure elements that contain user properties attributes.
     * @return the user properties flag
     */
    public boolean isUserProperties() {
        return this.userProperties;
    }

    /**
     * Sets the flag indicating the presence of structure elements that contain user properties attributes.
     * @param userProperties the user properties flag
     */
    public void setUserProperties(boolean userProperties) {
        this.userProperties = userProperties;
    }

    /**
     * Holds value of property RGBTranparency.
     */
    private boolean rgbTransparencyBlending;

    /**
     * Gets the transparency blending colorspace.
     * @return <code>true</code> if the transparency blending colorspace is RGB, <code>false</code>
     * if it is the default blending colorspace
     * @since 2.1.0
     */
    public boolean isRgbTransparencyBlending() {
        return this.rgbTransparencyBlending;
    }

    /**
     * Sets the transparency blending colorspace to RGB. The default blending colorspace is
     * CMYK and will result in faded colors in the screen and in printing. Calling this method
     * will return the RGB colors to what is expected. The RGB blending will be applied to all subsequent pages
     * until other value is set.
     * Note that this is a generic solution that may not work in all cases.
     * @param rgbTransparencyBlending <code>true</code> to set the transparency blending colorspace to RGB, <code>false</code>
     * to use the default blending colorspace
     * @since 2.1.0
     */
    public void setRgbTransparencyBlending(boolean rgbTransparencyBlending) {
        this.rgbTransparencyBlending = rgbTransparencyBlending;
    }
}
