/*
 * $Id: PdfSmartCopy.java 3991 2009-06-18 21:21:09Z psoares33 $
 *
 * Copyright 2007 Michael Neuweiler and Bruno Lowagie
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
 * This class was written by Michael Neuweiler based on hints given by Bruno Lowagie
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
package com.lowagie.text.pdf;

import java.io.IOException;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.ExceptionConverter;


/**
 * PdfSmartCopy has the same functionality as PdfCopy,
 * but when resources (such as fonts, images,...) are
 * encountered, a reference to these resources is saved
 * in a cache, so that they can be reused.
 * This requires more memory, but reduces the file size
 * of the resulting PDF document.
 */

public class PdfSmartCopy extends PdfCopy {

    /** the cache with the streams and references. */
    private Map<ByteStore, PdfIndirectReference> streamMap = null;

    /** Creates a PdfSmartCopy instance.
     * @param os the OutputStream
     * @param document the document
     * @throws DocumentException on error */
    public PdfSmartCopy(Document document, OutputStream os) throws DocumentException {
        super(document, os);
        this.streamMap = new HashMap<>();
    }
    /**
     * Translate a PRIndirectReference to a PdfIndirectReference
     * In addition, translates the object numbers, and copies the
     * referenced object to the output file if it wasn't available
     * in the cache yet. If it's in the cache, the reference to
     * the already used stream is returned.
     * 
     * NB: PRIndirectReferences (and PRIndirectObjects) really need to know what
     * file they came from, because each file has its own namespace. The translation
     * we do from their namespace to ours is *at best* heuristic, and guaranteed to
     * fail under some circumstances.
     */
    protected PdfIndirectReference copyIndirect(PRIndirectReference in) throws IOException, BadPdfFormatException {
        PdfObject srcObj = PdfReader.getPdfObjectRelease(in);
        ByteStore streamKey = null;
        boolean validStream = false;
        if (srcObj == null) {
            return null;
        }
        if (srcObj.isStream()) {
            try {
                streamKey = new ByteStore((PRStream)srcObj);
                validStream = true;
                PdfIndirectReference streamRef = streamMap.get(streamKey);
                if (streamRef != null) {
                    return streamRef;
                }
            }
            catch (IOException ioe) {
                //
            }
        }

        PdfIndirectReference theRef;
        RefKey key = new RefKey(in);
        IndirectReferences iRef = indirects.get(key);
        if (iRef != null) {
            theRef = iRef.getRef();
            if (iRef.getCopied()) {
                return theRef;
            }
        } else {
            theRef = body.getPdfIndirectReference();
            iRef = new IndirectReferences(theRef);
            indirects.put(key, iRef);
        }
        if (srcObj.isDictionary()) {
            PdfObject type = PdfReader.getPdfObjectRelease(((PdfDictionary)srcObj).get(PdfName.TYPE));
            if (PdfName.PAGE.equals(type)) {
                return theRef;
            }
        }
        iRef.setCopied();

        if (validStream) {
            streamMap.put(streamKey, theRef);
        }

        PdfObject obj = copyObject(srcObj);
        addToBody(obj, theRef);
        return theRef;
    }

    static class ByteStore {
        private byte[] b;
        private int hash;
        private MessageDigest md5;
        
        private final int MAX_LEVELS = 100;
        
        ByteStore(PRStream str) throws IOException {
            try {
                md5 = MessageDigest.getInstance("MD5");
            }
            catch (Exception e) {
                throw new ExceptionConverter(e);
            }
            ByteBuffer bb = new ByteBuffer();
            int level = MAX_LEVELS;
            serObject(str, level, bb);
            this.b = bb.toByteArray();
            md5 = null;
        }

        private void serObject(PdfObject obj, int level, ByteBuffer bb) throws IOException {
            if (level <= 0) {
                throw new IOException("Max level reached");
            }
            if (obj == null) {
                bb.append("$Lnull");
                return;
            }
            obj = PdfReader.getPdfObject(obj);
            if (obj.isStream()) {
                bb.append("$B");
                serDic((PdfDictionary)obj, level - 1, bb);
                md5.reset();
                bb.append(md5.digest(PdfReader.getStreamBytesRaw((PRStream)obj)));
            }
            else if (obj.isDictionary()) {
                serDic((PdfDictionary)obj, level - 1, bb);
            }
            else if (obj.isArray()) {
                serArray((PdfArray)obj, level - 1, bb);
            }
            else if (obj.isString()) {
                bb.append("$S").append(obj.toString());
            }
            else if (obj.isName()) {
                bb.append("$N").append(obj.toString());
            }
            else
                bb.append("$L").append(obj.toString());
        }
        
        private void serDic(PdfDictionary dic, int level, ByteBuffer bb) throws IOException {
            bb.append("$D");
            if (level <= 0) {
                throw new IOException("Max level reached");
            }
            Object[] keys = dic.getKeys().toArray();
            Arrays.sort(keys);
            for (Object key : keys) {
                serObject((PdfObject) key, level, bb);
                serObject(dic.get((PdfName) key), level, bb);
            }
        }
        
        private void serArray(PdfArray array, int level, ByteBuffer bb) throws IOException {
            bb.append("$A");
            if (level <= 0) {
                throw new IOException("Max level reached");
            }
            for (int k = 0; k < array.size(); ++k) {
                serObject(array.getPdfObject(k), level, bb);
            }
        }
        


        public boolean equals(Object obj) {
            if (!(obj instanceof ByteStore)) {
                return false;
            }
            if (hashCode() != obj.hashCode()) {
                return false;
            }
            return Arrays.equals(b, ((ByteStore)obj).b);
        }

        public int hashCode() {
            if (hash == 0) {
                for (byte b1 : b) {
                    hash = hash * 31 + (b1 & 0xff);
                }
            }
            return hash;
        }
    }
}