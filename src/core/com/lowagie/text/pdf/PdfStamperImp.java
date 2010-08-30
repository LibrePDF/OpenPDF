/*
 * Copyright 2003 by Paulo Soares.
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

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import com.lowagie.text.error_messages.MessageLocalization;

import org.xml.sax.SAXException;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.ExceptionConverter;
import com.lowagie.text.Image;
import com.lowagie.text.Rectangle;
import com.lowagie.text.exceptions.BadPasswordException;
import com.lowagie.text.pdf.collection.PdfCollection;
import com.lowagie.text.pdf.interfaces.PdfViewerPreferences;
import com.lowagie.text.pdf.internal.PdfViewerPreferencesImp;
import com.lowagie.text.xml.xmp.XmpReader;

class PdfStamperImp extends PdfWriter {
    HashMap readers2intrefs = new HashMap();
    HashMap readers2file = new HashMap();
    RandomAccessFileOrArray file;
    PdfReader reader;
    IntHashtable myXref = new IntHashtable();
    /** Integer(page number) -> PageStamp */
    HashMap pagesToContent = new HashMap();
    boolean closed = false;
    /** Holds value of property rotateContents. */
    private boolean rotateContents = true;
    protected AcroFields acroFields;
    protected boolean flat = false;
    protected boolean flatFreeText = false;
    protected int namePtr[] = {0};
    protected HashSet partialFlattening = new HashSet();
    protected boolean useVp = false;
    protected PdfViewerPreferencesImp viewerPreferences = new PdfViewerPreferencesImp();
    protected HashMap fieldTemplates = new HashMap();
    protected boolean fieldsAdded = false;
    protected int sigFlags = 0;
    protected boolean append;
    protected IntHashtable marked;
    protected int initialXrefSize;
    protected PdfAction openAction;

    /** Creates new PdfStamperImp.
     * @param reader the read PDF
     * @param os the output destination
     * @param pdfVersion the new pdf version or '\0' to keep the same version as the original
     * document
     * @param append
     * @throws DocumentException on error
     * @throws IOException
     */
    PdfStamperImp(PdfReader reader, OutputStream os, char pdfVersion, boolean append) throws DocumentException, IOException {
        super(new PdfDocument(), os);
        if (!reader.isOpenedWithFullPermissions())
            throw new BadPasswordException(MessageLocalization.getComposedMessage("pdfreader.not.opened.with.owner.password"));
        if (reader.isTampered())
            throw new DocumentException(MessageLocalization.getComposedMessage("the.original.document.was.reused.read.it.again.from.file"));
        reader.setTampered(true);
        this.reader = reader;
        file = reader.getSafeFile();
        this.append = append;
        if (append) {
            if (reader.isRebuilt())
                throw new DocumentException(MessageLocalization.getComposedMessage("append.mode.requires.a.document.without.errors.even.if.recovery.was.possible"));
            if (reader.isEncrypted())
                crypto = new PdfEncryption(reader.getDecrypt());
            pdf_version.setAppendmode(true);
            file.reOpen();
            byte buf[] = new byte[8192];
            int n;
            while ((n = file.read(buf)) > 0)
                this.os.write(buf, 0, n);
            file.close();
            prevxref = reader.getLastXref();
            reader.setAppendable(true);
        }
        else {
            if (pdfVersion == 0)
                super.setPdfVersion(reader.getPdfVersion());
            else
                super.setPdfVersion(pdfVersion);
        }
        super.open();
        pdf.addWriter(this);
        if (append) {
            body.setRefnum(reader.getXrefSize());
            marked = new IntHashtable();
            if (reader.isNewXrefType())
                fullCompression = true;
            if (reader.isHybridXref())
                fullCompression = false;
        }
        initialXrefSize = reader.getXrefSize();
    }

    void close(HashMap moreInfo) throws IOException {
        if (closed)
            return;
        if (useVp) {
            reader.setViewerPreferences(viewerPreferences);
            markUsed(reader.getTrailer().get(PdfName.ROOT));
        }
        if (flat)
            flatFields();
        if (flatFreeText)
        	flatFreeTextFields();
        addFieldResources();
        PdfDictionary catalog = reader.getCatalog();
        PdfDictionary pages = (PdfDictionary)PdfReader.getPdfObject(catalog.get(PdfName.PAGES));
        pages.put(PdfName.ITXT, new PdfString(Document.getRelease()));
        markUsed(pages);
        PdfDictionary acroForm = (PdfDictionary)PdfReader.getPdfObject(catalog.get(PdfName.ACROFORM), reader.getCatalog());
        if (acroFields != null && acroFields.getXfa().isChanged()) {
            markUsed(acroForm);
            if (!flat)
                acroFields.getXfa().setXfa(this);
        }
        if (sigFlags != 0) {
            if (acroForm != null) {
                acroForm.put(PdfName.SIGFLAGS, new PdfNumber(sigFlags));
                markUsed(acroForm);
                markUsed(catalog);
            }
        }
        closed = true;
        addSharedObjectsToBody();
        setOutlines();
        setJavaScript();
        addFileAttachments();
        if (openAction != null) {
            catalog.put(PdfName.OPENACTION, openAction);
        }
        if (pdf.pageLabels != null)
            catalog.put(PdfName.PAGELABELS, pdf.pageLabels.getDictionary(this));
        // OCG
        if (!documentOCG.isEmpty()) {
        	fillOCProperties(false);
        	PdfDictionary ocdict = catalog.getAsDict(PdfName.OCPROPERTIES);
        	if (ocdict == null) {
        		reader.getCatalog().put(PdfName.OCPROPERTIES, OCProperties);
        	}
        	else {
        		ocdict.put(PdfName.OCGS, OCProperties.get(PdfName.OCGS));
        		PdfDictionary ddict = ocdict.getAsDict(PdfName.D);
        		if (ddict == null) {
        			ddict = new PdfDictionary();
        			ocdict.put(PdfName.D, ddict);
        		}
        		ddict.put(PdfName.ORDER, OCProperties.getAsDict(PdfName.D).get(PdfName.ORDER));
        		ddict.put(PdfName.RBGROUPS, OCProperties.getAsDict(PdfName.D).get(PdfName.RBGROUPS));
        		ddict.put(PdfName.OFF, OCProperties.getAsDict(PdfName.D).get(PdfName.OFF));
        		ddict.put(PdfName.AS, OCProperties.getAsDict(PdfName.D).get(PdfName.AS));
            }
        }
        // metadata
        int skipInfo = -1;
        PRIndirectReference iInfo = (PRIndirectReference)reader.getTrailer().get(PdfName.INFO);
        PdfDictionary oldInfo = (PdfDictionary)PdfReader.getPdfObject(iInfo);
        String producer = null;
        if (iInfo != null)
            skipInfo = iInfo.getNumber();
        if (oldInfo != null && oldInfo.get(PdfName.PRODUCER) != null)
        	producer = oldInfo.getAsString(PdfName.PRODUCER).toString();
        if (producer == null) {
        	producer = Document.getVersion();
        }
        else if (producer.indexOf(Document.getProduct()) == -1) {
        	StringBuffer buf = new StringBuffer(producer);
        	buf.append("; modified using ");
        	buf.append(Document.getVersion());
        	producer = buf.toString();
        }
        // XMP
        byte[] altMetadata = null;
        PdfObject xmpo = PdfReader.getPdfObject(catalog.get(PdfName.METADATA));
        if (xmpo != null && xmpo.isStream()) {
            altMetadata = PdfReader.getStreamBytesRaw((PRStream)xmpo);
            PdfReader.killIndirect(catalog.get(PdfName.METADATA));
        }
        if (xmpMetadata != null) {
        	altMetadata = xmpMetadata;
        }
        // if there is XMP data to add: add it
        PdfDate date = new PdfDate();
        if (altMetadata != null) {
        	PdfStream xmp;
        	try {
        		XmpReader xmpr = new XmpReader(altMetadata);
        		if (!xmpr.replace("http://ns.adobe.com/pdf/1.3/", "Producer", producer))
        			xmpr.add("rdf:Description", "http://ns.adobe.com/pdf/1.3/", "pdf:Producer", producer);
        		if (!xmpr.replace("http://ns.adobe.com/xap/1.0/", "ModifyDate", date.getW3CDate()))
        			xmpr.add("rdf:Description", "http://ns.adobe.com/xap/1.0/", "xmp:ModifyDate", date.getW3CDate());
        		xmpr.replace("http://ns.adobe.com/xap/1.0/", "MetadataDate", date.getW3CDate());
            	xmp = new PdfStream(xmpr.serializeDoc());
        	}
        	catch(SAXException e) {
        		xmp = new PdfStream(altMetadata);
        	}
        	catch(IOException e) {
        		xmp = new PdfStream(altMetadata);
        	}
        	xmp.put(PdfName.TYPE, PdfName.METADATA);
        	xmp.put(PdfName.SUBTYPE, PdfName.XML);
        	if (crypto != null && !crypto.isMetadataEncrypted()) {
        		PdfArray ar = new PdfArray();
        		ar.add(PdfName.CRYPT);
        		xmp.put(PdfName.FILTER, ar);
        	}
        	if (append && xmpo != null) {
        		body.add(xmp, xmpo.getIndRef());
        	}
        	else {
        		catalog.put(PdfName.METADATA, body.add(xmp).getIndirectReference());
        		markUsed(catalog);
        	}
        }
        try {
            file.reOpen();
            alterContents();
            int rootN = ((PRIndirectReference)reader.trailer.get(PdfName.ROOT)).getNumber();
            if (append) {
                int keys[] = marked.getKeys();
                for (int k = 0; k < keys.length; ++k) {
                    int j = keys[k];
                    PdfObject obj = reader.getPdfObjectRelease(j);
                    if (obj != null && skipInfo != j && j < initialXrefSize) {
                        addToBody(obj, j, j != rootN);
                    }
                }
                for (int k = initialXrefSize; k < reader.getXrefSize(); ++k) {
                    PdfObject obj = reader.getPdfObject(k);
                    if (obj != null) {
                        addToBody(obj, getNewObjectNumber(reader, k, 0));
                    }
                }
            }
            else {
                for (int k = 1; k < reader.getXrefSize(); ++k) {
                    PdfObject obj = reader.getPdfObjectRelease(k);
                    if (obj != null && skipInfo != k) {
                        addToBody(obj, getNewObjectNumber(reader, k, 0), k != rootN);
                    }
                }
            }
        }
        finally {
            try {
                file.close();
            }
            catch (Exception e) {
                // empty on purpose
            }
        }
        PdfIndirectReference encryption = null;
        PdfObject fileID = null;
        if (crypto != null) {
            if (append) {
                encryption = reader.getCryptoRef();
            }
            else {
                PdfIndirectObject encryptionObject = addToBody(crypto.getEncryptionDictionary(), false);
                encryption = encryptionObject.getIndirectReference();
            }
            fileID = crypto.getFileID();
        }
        else
            fileID = PdfEncryption.createInfoId(PdfEncryption.createDocumentId());
        PRIndirectReference iRoot = (PRIndirectReference)reader.trailer.get(PdfName.ROOT);
        PdfIndirectReference root = new PdfIndirectReference(0, getNewObjectNumber(reader, iRoot.getNumber(), 0));
        PdfIndirectReference info = null;
        PdfDictionary newInfo = new PdfDictionary();
        if (oldInfo != null) {
            for (Iterator i = oldInfo.getKeys().iterator(); i.hasNext();) {
                PdfName key = (PdfName)i.next();
                PdfObject value = PdfReader.getPdfObject(oldInfo.get(key));
                newInfo.put(key, value);
            }
        }
        if (moreInfo != null) {
            for (Iterator i = moreInfo.entrySet().iterator(); i.hasNext();) {
                Map.Entry entry = (Map.Entry) i.next();
                String key = (String) entry.getKey();
                PdfName keyName = new PdfName(key);
                String value = (String) entry.getValue();
                if (value == null)
                    newInfo.remove(keyName);
                else
                    newInfo.put(keyName, new PdfString(value, PdfObject.TEXT_UNICODE));
            }
        }
        newInfo.put(PdfName.MODDATE, date);
        newInfo.put(PdfName.PRODUCER, new PdfString(producer));
        if (append) {
            if (iInfo == null)
                info = addToBody(newInfo, false).getIndirectReference();
            else
                info = addToBody(newInfo, iInfo.getNumber(), false).getIndirectReference();
        }
        else {
            info = addToBody(newInfo, false).getIndirectReference();
        }
        // write the cross-reference table of the body
        body.writeCrossReferenceTable(os, root, info, encryption, fileID, prevxref);
        if (fullCompression) {
            os.write(getISOBytes("startxref\n"));
            os.write(getISOBytes(String.valueOf(body.offset())));
            os.write(getISOBytes("\n%%EOF\n"));
        }
        else {
            PdfTrailer trailer = new PdfTrailer(body.size(),
            body.offset(),
            root,
            info,
            encryption,
            fileID, prevxref);
            trailer.toPdf(this, os);
        }
        os.flush();
        if (isCloseStream())
            os.close();
        reader.close();
    }

    void applyRotation(PdfDictionary pageN, ByteBuffer out) {
        if (!rotateContents)
            return;
        Rectangle page = reader.getPageSizeWithRotation(pageN);
        int rotation = page.getRotation();
        switch (rotation) {
            case 90:
                out.append(PdfContents.ROTATE90);
                out.append(page.getTop());
                out.append(' ').append('0').append(PdfContents.ROTATEFINAL);
                break;
            case 180:
                out.append(PdfContents.ROTATE180);
                out.append(page.getRight());
                out.append(' ');
                out.append(page.getTop());
                out.append(PdfContents.ROTATEFINAL);
                break;
            case 270:
                out.append(PdfContents.ROTATE270);
                out.append('0').append(' ');
                out.append(page.getRight());
                out.append(PdfContents.ROTATEFINAL);
                break;
        }
    }

    void alterContents() throws IOException {
        for (Iterator i = pagesToContent.values().iterator(); i.hasNext();) {
            PageStamp ps = (PageStamp)i.next();
            PdfDictionary pageN = ps.pageN;
            markUsed(pageN);
            PdfArray ar = null;
            PdfObject content = PdfReader.getPdfObject(pageN.get(PdfName.CONTENTS), pageN);
            if (content == null) {
                ar = new PdfArray();
                pageN.put(PdfName.CONTENTS, ar);
            }
            else if (content.isArray()) {
                ar = (PdfArray)content;
                markUsed(ar);
            }
            else if (content.isStream()) {
                ar = new PdfArray();
                ar.add(pageN.get(PdfName.CONTENTS));
                pageN.put(PdfName.CONTENTS, ar);
            }
            else {
                ar = new PdfArray();
                pageN.put(PdfName.CONTENTS, ar);
            }
            ByteBuffer out = new ByteBuffer();
            if (ps.under != null) {
                out.append(PdfContents.SAVESTATE);
                applyRotation(pageN, out);
                out.append(ps.under.getInternalBuffer());
                out.append(PdfContents.RESTORESTATE);
            }
            if (ps.over != null)
                out.append(PdfContents.SAVESTATE);
            PdfStream stream = new PdfStream(out.toByteArray());
            stream.flateCompress(compressionLevel);
            ar.addFirst(addToBody(stream).getIndirectReference());
            out.reset();
            if (ps.over != null) {
                out.append(' ');
                out.append(PdfContents.RESTORESTATE);
                ByteBuffer buf = ps.over.getInternalBuffer();
                out.append(buf.getBuffer(), 0, ps.replacePoint);
                out.append(PdfContents.SAVESTATE);
                applyRotation(pageN, out);
                out.append(buf.getBuffer(), ps.replacePoint, buf.size() - ps.replacePoint);
                out.append(PdfContents.RESTORESTATE);
                stream = new PdfStream(out.toByteArray());
                stream.flateCompress(compressionLevel);
                ar.add(addToBody(stream).getIndirectReference());
            }
            alterResources(ps);
        }
    }

    void alterResources(PageStamp ps) {
        ps.pageN.put(PdfName.RESOURCES, ps.pageResources.getResources());
    }

    protected int getNewObjectNumber(PdfReader reader, int number, int generation) {
        IntHashtable ref = (IntHashtable)readers2intrefs.get(reader);
        if (ref != null) {
            int n = ref.get(number);
            if (n == 0) {
                n = getIndirectReferenceNumber();
                ref.put(number, n);
            }
            return n;
        }
        if (currentPdfReaderInstance == null) {
            if (append && number < initialXrefSize)
                return number;
            int n = myXref.get(number);
            if (n == 0) {
                n = getIndirectReferenceNumber();
                myXref.put(number, n);
            }
            return n;
        }
        else
            return currentPdfReaderInstance.getNewObjectNumber(number, generation);
    }

    RandomAccessFileOrArray getReaderFile(PdfReader reader) {
        if (readers2intrefs.containsKey(reader)) {
            RandomAccessFileOrArray raf = (RandomAccessFileOrArray)readers2file.get(reader);
            if (raf != null)
                return raf;
            return reader.getSafeFile();
        }
        if (currentPdfReaderInstance == null)
            return file;
        else
            return currentPdfReaderInstance.getReaderFile();
    }

    /**
     * @param reader
     * @param openFile
     * @throws IOException
     */
    public void registerReader(PdfReader reader, boolean openFile) throws IOException {
        if (readers2intrefs.containsKey(reader))
            return;
        readers2intrefs.put(reader, new IntHashtable());
        if (openFile) {
            RandomAccessFileOrArray raf = reader.getSafeFile();
            readers2file.put(reader, raf);
            raf.reOpen();
        }
    }

    /**
     * @param reader
     */
    public void unRegisterReader(PdfReader reader) {
        if (!readers2intrefs.containsKey(reader))
            return;
        readers2intrefs.remove(reader);
        RandomAccessFileOrArray raf = (RandomAccessFileOrArray)readers2file.get(reader);
        if (raf == null)
            return;
        readers2file.remove(reader);
        try{raf.close();}catch(Exception e){}
    }

    static void findAllObjects(PdfReader reader, PdfObject obj, IntHashtable hits) {
        if (obj == null)
            return;
        switch (obj.type()) {
            case PdfObject.INDIRECT:
                PRIndirectReference iref = (PRIndirectReference)obj;
                if (reader != iref.getReader())
                    return;
                if (hits.containsKey(iref.getNumber()))
                    return;
                hits.put(iref.getNumber(), 1);
                findAllObjects(reader, PdfReader.getPdfObject(obj), hits);
                return;
            case PdfObject.ARRAY:
                PdfArray a = (PdfArray)obj;
                for (int k = 0; k < a.size(); ++k) {
                    findAllObjects(reader, a.getPdfObject(k), hits);
                }
                return;
            case PdfObject.DICTIONARY:
            case PdfObject.STREAM:
                PdfDictionary dic = (PdfDictionary)obj;
                for (Iterator it = dic.getKeys().iterator(); it.hasNext();) {
                    PdfName name = (PdfName)it.next();
                    findAllObjects(reader, dic.get(name), hits);
                }
                return;
        }
    }

    /**
     * @param fdf
     * @throws IOException
     */
    public void addComments(FdfReader fdf) throws IOException{
        if (readers2intrefs.containsKey(fdf))
            return;
        PdfDictionary catalog = fdf.getCatalog();
        catalog = catalog.getAsDict(PdfName.FDF);
        if (catalog == null)
            return;
        PdfArray annots = catalog.getAsArray(PdfName.ANNOTS);
        if (annots == null || annots.size() == 0)
            return;
        registerReader(fdf, false);
        IntHashtable hits = new IntHashtable();
        HashMap irt = new HashMap();
        ArrayList an = new ArrayList();
        for (int k = 0; k < annots.size(); ++k) {
            PdfObject obj = annots.getPdfObject(k);
            PdfDictionary annot = (PdfDictionary)PdfReader.getPdfObject(obj);
            PdfNumber page = annot.getAsNumber(PdfName.PAGE);
            if (page == null || page.intValue() >= reader.getNumberOfPages())
                continue;
            findAllObjects(fdf, obj, hits);
            an.add(obj);
            if (obj.type() == PdfObject.INDIRECT) {
                PdfObject nm = PdfReader.getPdfObject(annot.get(PdfName.NM));
                if (nm != null && nm.type() == PdfObject.STRING)
                    irt.put(nm.toString(), obj);
            }
        }
        int arhits[] = hits.getKeys();
        for (int k = 0; k < arhits.length; ++k) {
            int n = arhits[k];
            PdfObject obj = fdf.getPdfObject(n);
            if (obj.type() == PdfObject.DICTIONARY) {
                PdfObject str = PdfReader.getPdfObject(((PdfDictionary)obj).get(PdfName.IRT));
                if (str != null && str.type() == PdfObject.STRING) {
                   PdfObject i = (PdfObject)irt.get(str.toString());
                   if (i != null) {
                       PdfDictionary dic2 = new PdfDictionary();
                       dic2.merge((PdfDictionary)obj);
                       dic2.put(PdfName.IRT, i);
                       obj = dic2;
                   }
                }
            }
            addToBody(obj, getNewObjectNumber(fdf, n, 0));
        }
        for (int k = 0; k < an.size(); ++k) {
            PdfObject obj = (PdfObject)an.get(k);
            PdfDictionary annot = (PdfDictionary)PdfReader.getPdfObject(obj);
            PdfNumber page = annot.getAsNumber(PdfName.PAGE);
            PdfDictionary dic = reader.getPageN(page.intValue() + 1);
            PdfArray annotsp = (PdfArray)PdfReader.getPdfObject(dic.get(PdfName.ANNOTS), dic);
            if (annotsp == null) {
                annotsp = new PdfArray();
                dic.put(PdfName.ANNOTS, annotsp);
                markUsed(dic);
            }
            markUsed(annotsp);
            annotsp.add(obj);
        }
    }

    PageStamp getPageStamp(int pageNum) {
        PdfDictionary pageN = reader.getPageN(pageNum);
        PageStamp ps = (PageStamp)pagesToContent.get(pageN);
        if (ps == null) {
            ps = new PageStamp(this, reader, pageN);
            pagesToContent.put(pageN, ps);
        }
        return ps;
    }

    PdfContentByte getUnderContent(int pageNum) {
        if (pageNum < 1 || pageNum > reader.getNumberOfPages())
            return null;
        PageStamp ps = getPageStamp(pageNum);
        if (ps.under == null)
            ps.under = new StampContent(this, ps);
        return ps.under;
    }

    PdfContentByte getOverContent(int pageNum) {
        if (pageNum < 1 || pageNum > reader.getNumberOfPages())
            return null;
        PageStamp ps = getPageStamp(pageNum);
        if (ps.over == null)
            ps.over = new StampContent(this, ps);
        return ps.over;
    }

    void correctAcroFieldPages(int page) {
        if (acroFields == null)
            return;
        if (page > reader.getNumberOfPages())
            return;
        HashMap fields = acroFields.getFields();
        for (Iterator it = fields.values().iterator(); it.hasNext();) {
            AcroFields.Item item = (AcroFields.Item)it.next();
            for (int k = 0; k < item.size(); ++k) {
                int p = item.getPage(k).intValue();
                if (p >= page)
                    item.forcePage(k, p + 1);
            }
        }
    }

    private static void moveRectangle(PdfDictionary dic2, PdfReader r, int pageImported, PdfName key, String name) {
        Rectangle m = r.getBoxSize(pageImported, name);
        if (m == null)
            dic2.remove(key);
        else
            dic2.put(key, new PdfRectangle(m));
    }

    void replacePage(PdfReader r, int pageImported, int pageReplaced) {
        PdfDictionary pageN = reader.getPageN(pageReplaced);
        if (pagesToContent.containsKey(pageN))
            throw new IllegalStateException(MessageLocalization.getComposedMessage("this.page.cannot.be.replaced.new.content.was.already.added"));
        PdfImportedPage p = getImportedPage(r, pageImported);
        PdfDictionary dic2 = reader.getPageNRelease(pageReplaced);
        dic2.remove(PdfName.RESOURCES);
        dic2.remove(PdfName.CONTENTS);
        moveRectangle(dic2, r, pageImported, PdfName.MEDIABOX, "media");
        moveRectangle(dic2, r, pageImported, PdfName.CROPBOX, "crop");
        moveRectangle(dic2, r, pageImported, PdfName.TRIMBOX, "trim");
        moveRectangle(dic2, r, pageImported, PdfName.ARTBOX, "art");
        moveRectangle(dic2, r, pageImported, PdfName.BLEEDBOX, "bleed");
        dic2.put(PdfName.ROTATE, new PdfNumber(r.getPageRotation(pageImported)));
        PdfContentByte cb = getOverContent(pageReplaced);
        cb.addTemplate(p, 0, 0);
        PageStamp ps = (PageStamp)pagesToContent.get(pageN);
        ps.replacePoint = ps.over.getInternalBuffer().size();
    }

    void insertPage(int pageNumber, Rectangle mediabox) {
        Rectangle media = new Rectangle(mediabox);
        int rotation = media.getRotation() % 360;
        PdfDictionary page = new PdfDictionary(PdfName.PAGE);
        PdfDictionary resources = new PdfDictionary();
        PdfArray procset = new PdfArray();
        procset.add(PdfName.PDF);
        procset.add(PdfName.TEXT);
        procset.add(PdfName.IMAGEB);
        procset.add(PdfName.IMAGEC);
        procset.add(PdfName.IMAGEI);
        resources.put(PdfName.PROCSET, procset);
        page.put(PdfName.RESOURCES, resources);
        page.put(PdfName.ROTATE, new PdfNumber(rotation));
        page.put(PdfName.MEDIABOX, new PdfRectangle(media, rotation));
        PRIndirectReference pref = reader.addPdfObject(page);
        PdfDictionary parent;
        PRIndirectReference parentRef;
        if (pageNumber > reader.getNumberOfPages()) {
            PdfDictionary lastPage = reader.getPageNRelease(reader.getNumberOfPages());
            parentRef = (PRIndirectReference)lastPage.get(PdfName.PARENT);
            parentRef = new PRIndirectReference(reader, parentRef.getNumber());
            parent = (PdfDictionary)PdfReader.getPdfObject(parentRef);
            PdfArray kids = (PdfArray)PdfReader.getPdfObject(parent.get(PdfName.KIDS), parent);
            kids.add(pref);
            markUsed(kids);
            reader.pageRefs.insertPage(pageNumber, pref);
        }
        else {
            if (pageNumber < 1)
                pageNumber = 1;
            PdfDictionary firstPage = reader.getPageN(pageNumber);
            PRIndirectReference firstPageRef = reader.getPageOrigRef(pageNumber);
            reader.releasePage(pageNumber);
            parentRef = (PRIndirectReference)firstPage.get(PdfName.PARENT);
            parentRef = new PRIndirectReference(reader, parentRef.getNumber());
            parent = (PdfDictionary)PdfReader.getPdfObject(parentRef);
            PdfArray kids = (PdfArray)PdfReader.getPdfObject(parent.get(PdfName.KIDS), parent);
            int len = kids.size();
            int num = firstPageRef.getNumber();
            for (int k = 0; k < len; ++k) {
                PRIndirectReference cur = (PRIndirectReference)kids.getPdfObject(k);
                if (num == cur.getNumber()) {
                    kids.add(k, pref);
                    break;
                }
            }
            if (len == kids.size())
                throw new RuntimeException(MessageLocalization.getComposedMessage("internal.inconsistence"));
            markUsed(kids);
            reader.pageRefs.insertPage(pageNumber, pref);
            correctAcroFieldPages(pageNumber);
        }
        page.put(PdfName.PARENT, parentRef);
        while (parent != null) {
            markUsed(parent);
            PdfNumber count = (PdfNumber)PdfReader.getPdfObjectRelease(parent.get(PdfName.COUNT));
            parent.put(PdfName.COUNT, new PdfNumber(count.intValue() + 1));
            parent = parent.getAsDict(PdfName.PARENT);
        }
    }

    /** Getter for property rotateContents.
     * @return Value of property rotateContents.
     *
     */
    boolean isRotateContents() {
        return this.rotateContents;
    }

    /** Setter for property rotateContents.
     * @param rotateContents New value of property rotateContents.
     *
     */
    void setRotateContents(boolean rotateContents) {
        this.rotateContents = rotateContents;
    }

    boolean isContentWritten() {
        return body.size() > 1;
    }

    AcroFields getAcroFields() {
        if (acroFields == null) {
            acroFields = new AcroFields(reader, this);
        }
        return acroFields;
    }

    void setFormFlattening(boolean flat) {
        this.flat = flat;
    }

	void setFreeTextFlattening(boolean flat) {
		this.flatFreeText = flat;
    }

    boolean partialFormFlattening(String name) {
        getAcroFields();
        if (acroFields.getXfa().isXfaPresent())
            throw new UnsupportedOperationException(MessageLocalization.getComposedMessage("partial.form.flattening.is.not.supported.with.xfa.forms"));
        if (!acroFields.getFields().containsKey(name))
            return false;
        partialFlattening.add(name);
        return true;
    }

    void flatFields() {
        if (append)
            throw new IllegalArgumentException(MessageLocalization.getComposedMessage("field.flattening.is.not.supported.in.append.mode"));
        getAcroFields();
        HashMap fields = acroFields.getFields();
        if (fieldsAdded && partialFlattening.isEmpty()) {
            for (Iterator i = fields.keySet().iterator(); i.hasNext();) {
                partialFlattening.add(i.next());
            }
        }
        PdfDictionary acroForm = reader.getCatalog().getAsDict(PdfName.ACROFORM);
        PdfArray acroFds = null;
        if (acroForm != null) {
            acroFds = (PdfArray)PdfReader.getPdfObject(acroForm.get(PdfName.FIELDS), acroForm);
        }
        for (Iterator i = fields.entrySet().iterator(); i.hasNext();) {
            Map.Entry entry = (Map.Entry) i.next();
            String name = (String) entry.getKey();
            if (!partialFlattening.isEmpty() && !partialFlattening.contains(name))
                continue;
            AcroFields.Item item = (AcroFields.Item) entry.getValue();
            for (int k = 0; k < item.size(); ++k) {
                PdfDictionary merged = item.getMerged(k);
                PdfNumber ff = merged.getAsNumber(PdfName.F);
                int flags = 0;
                if (ff != null)
                    flags = ff.intValue();
                int page = item.getPage(k).intValue();
                PdfDictionary appDic = merged.getAsDict(PdfName.AP);
                if (appDic != null && (flags & PdfFormField.FLAGS_PRINT) != 0 && (flags & PdfFormField.FLAGS_HIDDEN) == 0) {
                    PdfObject obj = appDic.get(PdfName.N);
                    PdfAppearance app = null;
                    if (obj != null) {
                        PdfObject objReal = PdfReader.getPdfObject(obj);
                        if (obj instanceof PdfIndirectReference && !obj.isIndirect())
                            app = new PdfAppearance((PdfIndirectReference)obj);
                        else if (objReal instanceof PdfStream) {
                            ((PdfDictionary)objReal).put(PdfName.SUBTYPE, PdfName.FORM);
                            app = new PdfAppearance((PdfIndirectReference)obj);
                        }
                        else {
                            if (objReal != null && objReal.isDictionary()) {
                                PdfName as = merged.getAsName(PdfName.AS);
                                if (as != null) {
                                    PdfIndirectReference iref = (PdfIndirectReference)((PdfDictionary)objReal).get(as);
                                    if (iref != null) {
                                        app = new PdfAppearance(iref);
                                        if (iref.isIndirect()) {
                                            objReal = PdfReader.getPdfObject(iref);
                                            ((PdfDictionary)objReal).put(PdfName.SUBTYPE, PdfName.FORM);
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if (app != null) {
                        Rectangle box = PdfReader.getNormalizedRectangle(merged.getAsArray(PdfName.RECT));
                        PdfContentByte cb = getOverContent(page);
                        cb.setLiteral("Q ");
                        cb.addTemplate(app, box.getLeft(), box.getBottom());
                        cb.setLiteral("q ");
                    }
                }
                if (partialFlattening.isEmpty())
                    continue;
                PdfDictionary pageDic = reader.getPageN(page);
                PdfArray annots = pageDic.getAsArray(PdfName.ANNOTS);
                if (annots == null)
                    continue;
                for (int idx = 0; idx < annots.size(); ++idx) {
                    PdfObject ran = annots.getPdfObject(idx);
                    if (!ran.isIndirect())
                        continue;
                    PdfObject ran2 = item.getWidgetRef(k);
                    if (!ran2.isIndirect())
                        continue;
                    if (((PRIndirectReference)ran).getNumber() == ((PRIndirectReference)ran2).getNumber()) {
                        annots.remove(idx--);
                        PRIndirectReference wdref = (PRIndirectReference)ran2;
                        while (true) {
                            PdfDictionary wd = (PdfDictionary)PdfReader.getPdfObject(wdref);
                            PRIndirectReference parentRef = (PRIndirectReference)wd.get(PdfName.PARENT);
                            PdfReader.killIndirect(wdref);
                            if (parentRef == null) { // reached AcroForm
                                for (int fr = 0; fr < acroFds.size(); ++fr) {
                                    PdfObject h = acroFds.getPdfObject(fr);
                                    if (h.isIndirect() && ((PRIndirectReference)h).getNumber() == wdref.getNumber()) {
                                        acroFds.remove(fr);
                                        --fr;
                                    }
                                }
                                break;
                            }
                            PdfDictionary parent = (PdfDictionary)PdfReader.getPdfObject(parentRef);
                            PdfArray kids = parent.getAsArray(PdfName.KIDS);
                            for (int fr = 0; fr < kids.size(); ++fr) {
                                PdfObject h = kids.getPdfObject(fr);
                                if (h.isIndirect() && ((PRIndirectReference)h).getNumber() == wdref.getNumber()) {
                                    kids.remove(fr);
                                    --fr;
                                }
                            }
                            if (!kids.isEmpty())
                                break;
                            wdref = parentRef;
                        }
                    }
                }
                if (annots.isEmpty()) {
                    PdfReader.killIndirect(pageDic.get(PdfName.ANNOTS));
                    pageDic.remove(PdfName.ANNOTS);
                }
            }
        }
        if (!fieldsAdded && partialFlattening.isEmpty()) {
            for (int page = 1; page <= reader.getNumberOfPages(); ++page) {
                PdfDictionary pageDic = reader.getPageN(page);
                PdfArray annots = pageDic.getAsArray(PdfName.ANNOTS);
                if (annots == null)
                    continue;
                for (int idx = 0; idx < annots.size(); ++idx) {
                    PdfObject annoto = annots.getDirectObject(idx);
                    if ((annoto instanceof PdfIndirectReference) && !annoto.isIndirect())
                        continue;
                    if (!annoto.isDictionary() || PdfName.WIDGET.equals(((PdfDictionary)annoto).get(PdfName.SUBTYPE))) {
                        annots.remove(idx);
                        --idx;
                    }
                }
                if (annots.isEmpty()) {
                    PdfReader.killIndirect(pageDic.get(PdfName.ANNOTS));
                    pageDic.remove(PdfName.ANNOTS);
                }
            }
            eliminateAcroformObjects();
        }
    }

    void eliminateAcroformObjects() {
        PdfObject acro = reader.getCatalog().get(PdfName.ACROFORM);
        if (acro == null)
            return;
        PdfDictionary acrodic = (PdfDictionary)PdfReader.getPdfObject(acro);
        reader.killXref(acrodic.get(PdfName.XFA));
        acrodic.remove(PdfName.XFA);
        PdfObject iFields = acrodic.get(PdfName.FIELDS);
        if (iFields != null) {
            PdfDictionary kids = new PdfDictionary();
            kids.put(PdfName.KIDS, iFields);
            sweepKids(kids);
            PdfReader.killIndirect(iFields);
            acrodic.put(PdfName.FIELDS, new PdfArray());
        }
        acrodic.remove(PdfName.SIGFLAGS);
//        PdfReader.killIndirect(acro);
//        reader.getCatalog().remove(PdfName.ACROFORM);
    }

    void sweepKids(PdfObject obj) {
        PdfObject oo = PdfReader.killIndirect(obj);
        if (oo == null || !oo.isDictionary())
            return;
        PdfDictionary dic = (PdfDictionary)oo;
        PdfArray kids = (PdfArray)PdfReader.killIndirect(dic.get(PdfName.KIDS));
        if (kids == null)
            return;
        for (int k = 0; k < kids.size(); ++k) {
            sweepKids(kids.getPdfObject(k));
        }
    }

    private void flatFreeTextFields()
	{
		if (append)
			throw new IllegalArgumentException(MessageLocalization.getComposedMessage("freetext.flattening.is.not.supported.in.append.mode"));

		for (int page = 1; page <= reader.getNumberOfPages(); ++page)
		{
			PdfDictionary pageDic = reader.getPageN(page);
			PdfArray annots = pageDic.getAsArray(PdfName.ANNOTS);
			if (annots == null)
				continue;
			for (int idx = 0; idx < annots.size(); ++idx)
			{
				PdfObject annoto = annots.getDirectObject(idx);
				if ((annoto instanceof PdfIndirectReference) && !annoto.isIndirect())
					continue;

				PdfDictionary annDic = (PdfDictionary)annoto;
 				if (!((PdfName)annDic.get(PdfName.SUBTYPE)).equals(PdfName.FREETEXT))
					continue;
				PdfNumber ff = annDic.getAsNumber(PdfName.F);
                int flags = (ff != null) ? ff.intValue() : 0;

				if ( (flags & PdfFormField.FLAGS_PRINT) != 0 && (flags & PdfFormField.FLAGS_HIDDEN) == 0)
				{
					PdfObject obj1 = annDic.get(PdfName.AP);
					if (obj1 == null)
						continue;
					PdfDictionary appDic = (obj1 instanceof PdfIndirectReference) ?
							(PdfDictionary) PdfReader.getPdfObject(obj1) : (PdfDictionary) obj1;
					PdfObject obj = appDic.get(PdfName.N);
					PdfAppearance app = null;
					PdfObject objReal = PdfReader.getPdfObject(obj);

					if (obj instanceof PdfIndirectReference && !obj.isIndirect())
						app = new PdfAppearance((PdfIndirectReference)obj);
					else if (objReal instanceof PdfStream)
					{
						((PdfDictionary)objReal).put(PdfName.SUBTYPE, PdfName.FORM);
						app = new PdfAppearance((PdfIndirectReference)obj);
					}
					else
					{
						if (objReal.isDictionary())
						{
							PdfName as_p = appDic.getAsName(PdfName.AS);
							if (as_p != null)
							{
								PdfIndirectReference iref = (PdfIndirectReference)((PdfDictionary)objReal).get(as_p);
								if (iref != null)
								{
									app = new PdfAppearance(iref);
									if (iref.isIndirect())
									{
										objReal = PdfReader.getPdfObject(iref);
										((PdfDictionary)objReal).put(PdfName.SUBTYPE, PdfName.FORM);
									}
								}
							}
						}
					}
					if (app != null)
					{
						Rectangle box = PdfReader.getNormalizedRectangle(annDic.getAsArray(PdfName.RECT));
						PdfContentByte cb = getOverContent(page);
						cb.setLiteral("Q ");
						cb.addTemplate(app, box.getLeft(), box.getBottom());
						cb.setLiteral("q ");
					}
				}
			}
			for (int idx = 0; idx < annots.size(); ++idx)
			{
			    PdfDictionary annot = annots.getAsDict(idx);
				if (annot != null)
				{
					if (PdfName.FREETEXT.equals(annot.get(PdfName.SUBTYPE)))
					{
					    annots.remove(idx);
						--idx;
					}
				}
			}
			if (annots.isEmpty())
			{
				PdfReader.killIndirect(pageDic.get(PdfName.ANNOTS));
				pageDic.remove(PdfName.ANNOTS);
			}
		}
	}

    /**
     * @see com.lowagie.text.pdf.PdfWriter#getPageReference(int)
     */
    public PdfIndirectReference getPageReference(int page) {
        PdfIndirectReference ref = reader.getPageOrigRef(page);
        if (ref == null)
            throw new IllegalArgumentException(MessageLocalization.getComposedMessage("invalid.page.number.1", page));
        return ref;
    }

    /**
     * @see com.lowagie.text.pdf.PdfWriter#addAnnotation(com.lowagie.text.pdf.PdfAnnotation)
     */
    public void addAnnotation(PdfAnnotation annot) {
        throw new RuntimeException(MessageLocalization.getComposedMessage("unsupported.in.this.context.use.pdfstamper.addannotation"));
    }

    void addDocumentField(PdfIndirectReference ref) {
        PdfDictionary catalog = reader.getCatalog();
        PdfDictionary acroForm = (PdfDictionary)PdfReader.getPdfObject(catalog.get(PdfName.ACROFORM), catalog);
        if (acroForm == null) {
            acroForm = new PdfDictionary();
            catalog.put(PdfName.ACROFORM, acroForm);
            markUsed(catalog);
        }
        PdfArray fields = (PdfArray)PdfReader.getPdfObject(acroForm.get(PdfName.FIELDS), acroForm);
        if (fields == null) {
            fields = new PdfArray();
            acroForm.put(PdfName.FIELDS, fields);
            markUsed(acroForm);
        }
        if (!acroForm.contains(PdfName.DA)) {
            acroForm.put(PdfName.DA, new PdfString("/Helv 0 Tf 0 g "));
            markUsed(acroForm);
        }
        fields.add(ref);
        markUsed(fields);
    }

    void addFieldResources() throws IOException {
        if (fieldTemplates.isEmpty())
            return;
        PdfDictionary catalog = reader.getCatalog();
        PdfDictionary acroForm = (PdfDictionary)PdfReader.getPdfObject(catalog.get(PdfName.ACROFORM), catalog);
        if (acroForm == null) {
            acroForm = new PdfDictionary();
            catalog.put(PdfName.ACROFORM, acroForm);
            markUsed(catalog);
        }
        PdfDictionary dr = (PdfDictionary)PdfReader.getPdfObject(acroForm.get(PdfName.DR), acroForm);
        if (dr == null) {
            dr = new PdfDictionary();
            acroForm.put(PdfName.DR, dr);
            markUsed(acroForm);
        }
        markUsed(dr);
        for (Iterator it = fieldTemplates.keySet().iterator(); it.hasNext();) {
            PdfTemplate template = (PdfTemplate)it.next();
            PdfFormField.mergeResources(dr, (PdfDictionary)template.getResources(), this);
        }
        // if (dr.get(PdfName.ENCODING) == null) dr.put(PdfName.ENCODING, PdfName.WIN_ANSI_ENCODING);
        PdfDictionary fonts = dr.getAsDict(PdfName.FONT);
        if (fonts == null) {
            fonts = new PdfDictionary();
            dr.put(PdfName.FONT, fonts);
        }
        if (!fonts.contains(PdfName.HELV)) {
            PdfDictionary dic = new PdfDictionary(PdfName.FONT);
            dic.put(PdfName.BASEFONT, PdfName.HELVETICA);
            dic.put(PdfName.ENCODING, PdfName.WIN_ANSI_ENCODING);
            dic.put(PdfName.NAME, PdfName.HELV);
            dic.put(PdfName.SUBTYPE, PdfName.TYPE1);
            fonts.put(PdfName.HELV, addToBody(dic).getIndirectReference());
        }
        if (!fonts.contains(PdfName.ZADB)) {
            PdfDictionary dic = new PdfDictionary(PdfName.FONT);
            dic.put(PdfName.BASEFONT, PdfName.ZAPFDINGBATS);
            dic.put(PdfName.NAME, PdfName.ZADB);
            dic.put(PdfName.SUBTYPE, PdfName.TYPE1);
            fonts.put(PdfName.ZADB, addToBody(dic).getIndirectReference());
        }
        if (acroForm.get(PdfName.DA) == null) {
            acroForm.put(PdfName.DA, new PdfString("/Helv 0 Tf 0 g "));
            markUsed(acroForm);
        }
    }

    void expandFields(PdfFormField field, ArrayList allAnnots) {
        allAnnots.add(field);
        ArrayList kids = field.getKids();
        if (kids != null) {
            for (int k = 0; k < kids.size(); ++k)
                expandFields((PdfFormField)kids.get(k), allAnnots);
        }
    }

    void addAnnotation(PdfAnnotation annot, PdfDictionary pageN) {
        try {
            ArrayList allAnnots = new ArrayList();
            if (annot.isForm()) {
                fieldsAdded = true;
                getAcroFields();
                PdfFormField field = (PdfFormField)annot;
                if (field.getParent() != null)
                    return;
                expandFields(field, allAnnots);
            }
            else
                allAnnots.add(annot);
            for (int k = 0; k < allAnnots.size(); ++k) {
                annot = (PdfAnnotation)allAnnots.get(k);
                if (annot.getPlaceInPage() > 0)
                    pageN = reader.getPageN(annot.getPlaceInPage());
                if (annot.isForm()) {
                    if (!annot.isUsed()) {
                        HashMap templates = annot.getTemplates();
                        if (templates != null)
                            fieldTemplates.putAll(templates);
                    }
                    PdfFormField field = (PdfFormField)annot;
                    if (field.getParent() == null)
                        addDocumentField(field.getIndirectReference());
                }
                if (annot.isAnnotation()) {
                    PdfObject pdfobj = PdfReader.getPdfObject(pageN.get(PdfName.ANNOTS), pageN);
                    PdfArray annots = null;
                    if (pdfobj == null || !pdfobj.isArray()) {
                        annots = new PdfArray();
                        pageN.put(PdfName.ANNOTS, annots);
                        markUsed(pageN);
                    }
                    else
                       annots = (PdfArray)pdfobj;
                    annots.add(annot.getIndirectReference());
                    markUsed(annots);
                    if (!annot.isUsed()) {
                        PdfRectangle rect = (PdfRectangle)annot.get(PdfName.RECT);
                        if (rect != null && (rect.left() != 0 || rect.right() != 0 || rect.top() != 0 || rect.bottom() != 0)) {
                            int rotation = reader.getPageRotation(pageN);
                            Rectangle pageSize = reader.getPageSizeWithRotation(pageN);
                            switch (rotation) {
                                case 90:
                                    annot.put(PdfName.RECT, new PdfRectangle(
                                    pageSize.getTop() - rect.top(),
                                    rect.right(),
                                    pageSize.getTop() - rect.bottom(),
                                    rect.left()));
                                    break;
                                case 180:
                                    annot.put(PdfName.RECT, new PdfRectangle(
                                    pageSize.getRight() - rect.left(),
                                    pageSize.getTop() - rect.bottom(),
                                    pageSize.getRight() - rect.right(),
                                    pageSize.getTop() - rect.top()));
                                    break;
                                case 270:
                                    annot.put(PdfName.RECT, new PdfRectangle(
                                    rect.bottom(),
                                    pageSize.getRight() - rect.left(),
                                    rect.top(),
                                    pageSize.getRight() - rect.right()));
                                    break;
                            }
                        }
                    }
                }
                if (!annot.isUsed()) {
                    annot.setUsed();
                    addToBody(annot, annot.getIndirectReference());
                }
            }
        }
        catch (IOException e) {
            throw new ExceptionConverter(e);
        }
    }

    void addAnnotation(PdfAnnotation annot, int page) {
    	annot.setPage(page);
        addAnnotation(annot, reader.getPageN(page));
    }

    private void outlineTravel(PRIndirectReference outline) {
        while (outline != null) {
            PdfDictionary outlineR = (PdfDictionary)PdfReader.getPdfObjectRelease(outline);
            PRIndirectReference first = (PRIndirectReference)outlineR.get(PdfName.FIRST);
            if (first != null) {
                outlineTravel(first);
            }
            PdfReader.killIndirect(outlineR.get(PdfName.DEST));
            PdfReader.killIndirect(outlineR.get(PdfName.A));
            PdfReader.killIndirect(outline);
            outline = (PRIndirectReference)outlineR.get(PdfName.NEXT);
        }
    }

    void deleteOutlines() {
        PdfDictionary catalog = reader.getCatalog();
        PRIndirectReference outlines = (PRIndirectReference)catalog.get(PdfName.OUTLINES);
        if (outlines == null)
            return;
        outlineTravel(outlines);
        PdfReader.killIndirect(outlines);
        catalog.remove(PdfName.OUTLINES);
        markUsed(catalog);
    }

    void setJavaScript() throws IOException {
        HashMap djs = pdf.getDocumentLevelJS();
        if (djs.isEmpty())
            return;
        PdfDictionary catalog = reader.getCatalog();
        PdfDictionary names = (PdfDictionary)PdfReader.getPdfObject(catalog.get(PdfName.NAMES), catalog);
        if (names == null) {
            names = new PdfDictionary();
            catalog.put(PdfName.NAMES, names);
            markUsed(catalog);
        }
        markUsed(names);
        PdfDictionary tree = PdfNameTree.writeTree(djs, this);
        names.put(PdfName.JAVASCRIPT, addToBody(tree).getIndirectReference());
    }

    void addFileAttachments() throws IOException {
        HashMap fs = pdf.getDocumentFileAttachment();
        if (fs.isEmpty())
            return;
        PdfDictionary catalog = reader.getCatalog();
        PdfDictionary names = (PdfDictionary)PdfReader.getPdfObject(catalog.get(PdfName.NAMES), catalog);
        if (names == null) {
            names = new PdfDictionary();
            catalog.put(PdfName.NAMES, names);
            markUsed(catalog);
        }
        markUsed(names);
        HashMap old = PdfNameTree.readTree((PdfDictionary)PdfReader.getPdfObjectRelease(names.get(PdfName.EMBEDDEDFILES)));
        for (Iterator it = fs.entrySet().iterator(); it.hasNext();) {
            Map.Entry entry = (Map.Entry) it.next();
            String name = (String) entry.getKey();
            int k = 0;
            String nn = name;
            while (old.containsKey(nn)) {
                ++k;
                nn += " " + k;
            }
            old.put(nn, entry.getValue());
        }
        PdfDictionary tree = PdfNameTree.writeTree(old, this);
        // Remove old EmbeddedFiles object if preset
        PdfObject oldEmbeddedFiles = names.get(PdfName.EMBEDDEDFILES);
        if (oldEmbeddedFiles != null) {
            PdfReader.killIndirect(oldEmbeddedFiles);
        }

        // Add new EmbeddedFiles object
        names.put(PdfName.EMBEDDEDFILES, addToBody(tree).getIndirectReference());
    }

    /**
     * Adds or replaces the Collection Dictionary in the Catalog.
     * @param	collection	the new collection dictionary.
     */
    void makePackage( PdfCollection collection ) {
        PdfDictionary catalog = reader.getCatalog();
       	catalog.put( PdfName.COLLECTION, collection );
    }

    void setOutlines() throws IOException {
        if (newBookmarks == null)
            return;
        deleteOutlines();
        if (newBookmarks.isEmpty())
            return;
        PdfDictionary catalog = reader.getCatalog();
        boolean namedAsNames = (catalog.get(PdfName.DESTS) != null);
        writeOutlines(catalog, namedAsNames);
        markUsed(catalog);
    }

    /**
     * Sets the viewer preferences.
     * @param preferences the viewer preferences
     * @see PdfWriter#setViewerPreferences(int)
     */
    public void setViewerPreferences(int preferences) {
        useVp = true;
        this.viewerPreferences.setViewerPreferences(preferences);
    }

    /** Adds a viewer preference
     * @param key a key for a viewer preference
     * @param value the value for the viewer preference
     * @see PdfViewerPreferences#addViewerPreference
     */
    public void addViewerPreference(PdfName key, PdfObject value) {
    	useVp = true;
    	this.viewerPreferences.addViewerPreference(key, value);
    }

    /**
     * Set the signature flags.
     * @param f the flags. This flags are ORed with current ones
     */
    public void setSigFlags(int f) {
        sigFlags |= f;
    }

    /** Always throws an <code>UnsupportedOperationException</code>.
     * @param actionType ignore
     * @param action ignore
     * @throws PdfException ignore
     * @see PdfStamper#setPageAction(PdfName, PdfAction, int)
     */
    public void setPageAction(PdfName actionType, PdfAction action) throws PdfException {
        throw new UnsupportedOperationException(MessageLocalization.getComposedMessage("use.setpageaction.pdfname.actiontype.pdfaction.action.int.page"));
    }

    /**
     * Sets the open and close page additional action.
     * @param actionType the action type. It can be <CODE>PdfWriter.PAGE_OPEN</CODE>
     * or <CODE>PdfWriter.PAGE_CLOSE</CODE>
     * @param action the action to perform
     * @param page the page where the action will be applied. The first page is 1
     * @throws PdfException if the action type is invalid
     */
    void setPageAction(PdfName actionType, PdfAction action, int page) throws PdfException {
        if (!actionType.equals(PAGE_OPEN) && !actionType.equals(PAGE_CLOSE))
            throw new PdfException(MessageLocalization.getComposedMessage("invalid.page.additional.action.type.1", actionType.toString()));
        PdfDictionary pg = reader.getPageN(page);
        PdfDictionary aa = (PdfDictionary)PdfReader.getPdfObject(pg.get(PdfName.AA), pg);
        if (aa == null) {
            aa = new PdfDictionary();
            pg.put(PdfName.AA, aa);
            markUsed(pg);
        }
        aa.put(actionType, action);
        markUsed(aa);
    }

    /**
     * Always throws an <code>UnsupportedOperationException</code>.
     * @param seconds ignore
     */
    public void setDuration(int seconds) {
        throw new UnsupportedOperationException(MessageLocalization.getComposedMessage("use.setpageaction.pdfname.actiontype.pdfaction.action.int.page"));
    }

    /**
     * Always throws an <code>UnsupportedOperationException</code>.
     * @param transition ignore
     */
    public void setTransition(PdfTransition transition) {
        throw new UnsupportedOperationException(MessageLocalization.getComposedMessage("use.setpageaction.pdfname.actiontype.pdfaction.action.int.page"));
    }

    /**
     * Sets the display duration for the page (for presentations)
     * @param seconds   the number of seconds to display the page. A negative value removes the entry
     * @param page the page where the duration will be applied. The first page is 1
     */
    void setDuration(int seconds, int page) {
        PdfDictionary pg = reader.getPageN(page);
        if (seconds < 0)
            pg.remove(PdfName.DUR);
        else
            pg.put(PdfName.DUR, new PdfNumber(seconds));
        markUsed(pg);
    }

    /**
     * Sets the transition for the page
     * @param transition   the transition object. A <code>null</code> removes the transition
     * @param page the page where the transition will be applied. The first page is 1
     */
    void setTransition(PdfTransition transition, int page) {
        PdfDictionary pg = reader.getPageN(page);
        if (transition == null)
            pg.remove(PdfName.TRANS);
        else
            pg.put(PdfName.TRANS, transition.getTransitionDictionary());
        markUsed(pg);
    }

    protected void markUsed(PdfObject obj) {
        if (append && obj != null) {
            PRIndirectReference ref = null;
            if (obj.type() == PdfObject.INDIRECT)
                ref = (PRIndirectReference)obj;
            else
                ref = obj.getIndRef();
            if (ref != null)
                marked.put(ref.getNumber(), 1);
        }
    }

    protected void markUsed(int num) {
        if (append)
            marked.put(num, 1);
    }

    /**
     * Getter for property append.
     * @return Value of property append.
     */
    boolean isAppend() {
        return append;
    }

    /** Additional-actions defining the actions to be taken in
     * response to various trigger events affecting the document
     * as a whole. The actions types allowed are: <CODE>DOCUMENT_CLOSE</CODE>,
     * <CODE>WILL_SAVE</CODE>, <CODE>DID_SAVE</CODE>, <CODE>WILL_PRINT</CODE>
     * and <CODE>DID_PRINT</CODE>.
     *
     * @param actionType the action type
     * @param action the action to execute in response to the trigger
     * @throws PdfException on invalid action type
     */
    public void setAdditionalAction(PdfName actionType, PdfAction action) throws PdfException {
        if (!(actionType.equals(DOCUMENT_CLOSE) ||
        actionType.equals(WILL_SAVE) ||
        actionType.equals(DID_SAVE) ||
        actionType.equals(WILL_PRINT) ||
        actionType.equals(DID_PRINT))) {
            throw new PdfException(MessageLocalization.getComposedMessage("invalid.additional.action.type.1", actionType.toString()));
        }
        PdfDictionary aa = reader.getCatalog().getAsDict(PdfName.AA);
        if (aa == null) {
            if (action == null)
                return;
            aa = new PdfDictionary();
            reader.getCatalog().put(PdfName.AA, aa);
        }
        markUsed(aa);
        if (action == null)
            aa.remove(actionType);
        else
            aa.put(actionType, action);
    }

    /**
     * @see com.lowagie.text.pdf.PdfWriter#setOpenAction(com.lowagie.text.pdf.PdfAction)
     */
    public void setOpenAction(PdfAction action) {
        openAction = action;
    }

    /**
     * @see com.lowagie.text.pdf.PdfWriter#setOpenAction(java.lang.String)
     */
    public void setOpenAction(String name) {
        throw new UnsupportedOperationException(MessageLocalization.getComposedMessage("open.actions.by.name.are.not.supported"));
    }

    /**
     * @see com.lowagie.text.pdf.PdfWriter#setThumbnail(com.lowagie.text.Image)
     */
    public void setThumbnail(com.lowagie.text.Image image) {
        throw new UnsupportedOperationException(MessageLocalization.getComposedMessage("use.pdfstamper.setthumbnail"));
    }

    void setThumbnail(Image image, int page) throws PdfException, DocumentException {
        PdfIndirectReference thumb = getImageReference(addDirectImageSimple(image));
        reader.resetReleasePage();
        PdfDictionary dic = reader.getPageN(page);
        dic.put(PdfName.THUMB, thumb);
        reader.resetReleasePage();
    }

    public PdfContentByte getDirectContentUnder() {
        throw new UnsupportedOperationException(MessageLocalization.getComposedMessage("use.pdfstamper.getundercontent.or.pdfstamper.getovercontent"));
    }

    public PdfContentByte getDirectContent() {
        throw new UnsupportedOperationException(MessageLocalization.getComposedMessage("use.pdfstamper.getundercontent.or.pdfstamper.getovercontent"));
    }

    /**
     * Reads the OCProperties dictionary from the catalog of the existing document
     * and fills the documentOCG, documentOCGorder and OCGRadioGroup variables in PdfWriter.
     * Note that the original OCProperties of the existing document can contain more information.
     * @since	2.1.2
     */
    protected void readOCProperties() {
    	if (!documentOCG.isEmpty()) {
    		return;
    	}
    	PdfDictionary dict = reader.getCatalog().getAsDict(PdfName.OCPROPERTIES);
    	if (dict == null) {
    		return;
    	}
    	PdfArray ocgs = dict.getAsArray(PdfName.OCGS);
    	PdfIndirectReference ref;
    	PdfLayer layer;
    	HashMap ocgmap = new HashMap();
    	for (Iterator i = ocgs.listIterator(); i.hasNext(); ) {
    		ref = (PdfIndirectReference)i.next();
    		layer = new PdfLayer(null);
    		layer.setRef(ref);
    		layer.setOnPanel(false);
			layer.merge((PdfDictionary)PdfReader.getPdfObject(ref));
    		ocgmap.put(ref.toString(), layer);
    	}
    	PdfDictionary d = dict.getAsDict(PdfName.D);
    	PdfArray off = d.getAsArray(PdfName.OFF);
    	if (off != null) {
    		for (Iterator i = off.listIterator(); i.hasNext(); ) {
    			ref = (PdfIndirectReference)i.next();
    			layer = (PdfLayer)ocgmap.get(ref.toString());
    			layer.setOn(false);
    		}
    	}
    	PdfArray order = d.getAsArray(PdfName.ORDER);
    	if (order != null) {
    		addOrder(null, order, ocgmap);
    	}
    	documentOCG.addAll(ocgmap.values());
    	OCGRadioGroup = d.getAsArray(PdfName.RBGROUPS);
    	OCGLocked = d.getAsArray(PdfName.LOCKED);
    	if (OCGLocked == null)
    		OCGLocked = new PdfArray();
    }

    /**
     * Recursive method to reconstruct the documentOCGorder variable in the writer.
     * @param	parent	a parent PdfLayer (can be null)
     * @param	arr		an array possibly containing children for the parent PdfLayer
     * @param	ocgmap	a HashMap with indirect reference Strings as keys and PdfLayer objects as values.
     * @since	2.1.2
     */
    private void addOrder(PdfLayer parent, PdfArray arr, Map ocgmap) {
    	PdfObject obj;
    	PdfLayer layer;
    	for (int i = 0; i < arr.size(); i++) {
    		obj = arr.getPdfObject(i);
    		if (obj.isIndirect()) {
    			layer = (PdfLayer)ocgmap.get(obj.toString());
    			layer.setOnPanel(true);
    			registerLayer(layer);
    			if (parent != null) {
    				parent.addChild(layer);
    			}
    			if (arr.size() > i + 1 && arr.getPdfObject(i + 1).isArray()) {
    				i++;
    				addOrder(layer, (PdfArray)arr.getPdfObject(i), ocgmap);
    			}
    		}
    		else if (obj.isArray()) {
    		    PdfArray sub = (PdfArray)obj;
    			if (sub.isEmpty()) return;
    			obj = sub.getPdfObject(0);
    			if (obj.isString()) {
    				layer = new PdfLayer(obj.toString());
    				layer.setOnPanel(true);
    				registerLayer(layer);
    				if (parent != null) {
    					parent.addChild(layer);
    				}
    				PdfArray array = new PdfArray();
    				for (Iterator j = sub.listIterator(); j.hasNext(); ) {
    					array.add((PdfObject)j.next());
    				}
    				addOrder(layer, array, ocgmap);
    			}
    			else {
    				addOrder(parent, (PdfArray)obj, ocgmap);
    			}
    		}
    	}
    }

    /**
     * Gets the PdfLayer objects in an existing document as a Map
     * with the names/titles of the layers as keys.
     * @return	a Map with all the PdfLayers in the document (and the name/title of the layer as key)
     * @since	2.1.2
     */
    public Map getPdfLayers() {
    	if (documentOCG.isEmpty()) {
    		readOCProperties();
    	}
    	HashMap map = new HashMap();
    	PdfLayer layer;
    	String key;
    	for (Iterator i = documentOCG.iterator(); i.hasNext(); ) {
    		layer = (PdfLayer)i.next();
    		if (layer.getTitle() == null) {
    			key = layer.getAsString(PdfName.NAME).toString();
    		}
    		else {
    			key = layer.getTitle();
    		}
    		if (map.containsKey(key)) {
    			int seq = 2;
    			String tmp = key + "(" + seq + ")";
    			while (map.containsKey(tmp)) {
    				seq++;
    				tmp = key + "(" + seq + ")";
    			}
    			key = tmp;
    		}
			map.put(key, layer);
    	}
    	return map;
    }

    static class PageStamp {

        PdfDictionary pageN;
        StampContent under;
        StampContent over;
        PageResources pageResources;
        int replacePoint = 0;

        PageStamp(PdfStamperImp stamper, PdfReader reader, PdfDictionary pageN) {
            this.pageN = pageN;
            pageResources = new PageResources();
            PdfDictionary resources = pageN.getAsDict(PdfName.RESOURCES);
            pageResources.setOriginalResources(resources, stamper.namePtr);
        }
    }
}
