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
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
/** Reads an FDF form and makes the fields available
 * @author Paulo Soares (psoares@consiste.pt)
 */
public class FdfReader extends PdfReader {
    
    HashMap fields;
    String fileSpec;
    PdfName encoding;
    
    /** Reads an FDF form.
     * @param filename the file name of the form
     * @throws IOException on error
     */    
    public FdfReader(String filename) throws IOException {
        super(filename);
    }
    
    /** Reads an FDF form.
     * @param pdfIn the byte array with the form
     * @throws IOException on error
     */    
    public FdfReader(byte pdfIn[]) throws IOException {
        super(pdfIn);
    }
    
    /** Reads an FDF form.
     * @param url the URL of the document
     * @throws IOException on error
     */    
    public FdfReader(URL url) throws IOException {
        super(url);
    }
    
    /** Reads an FDF form.
     * @param is the <CODE>InputStream</CODE> containing the document. The stream is read to the
     * end but is not closed
     * @throws IOException on error
     */    
    public FdfReader(InputStream is) throws IOException {
        super(is);
    }
    
    protected void readPdf() throws IOException {
        fields = new HashMap();
        try {
            tokens.checkFdfHeader();
            rebuildXref();
            readDocObj();
        }
        finally {
            try {
                tokens.close();
            }
            catch (Exception e) {
                // empty on purpose
            }
        }
        readFields();
    }
    
    protected void kidNode(PdfDictionary merged, String name) {
        PdfArray kids = merged.getAsArray(PdfName.KIDS);
        if (kids == null || kids.isEmpty()) {
            if (name.length() > 0)
                name = name.substring(1);
            fields.put(name, merged);
        }
        else {
            merged.remove(PdfName.KIDS);
            for (int k = 0; k < kids.size(); ++k) {
                PdfDictionary dic = new PdfDictionary();
                dic.merge(merged);
                PdfDictionary newDic = kids.getAsDict(k);
                PdfString t = newDic.getAsString(PdfName.T);
                String newName = name;
                if (t != null)
                    newName += "." + t.toUnicodeString();
                dic.merge(newDic);
                dic.remove(PdfName.T);
                kidNode(dic, newName);
            }
        }
    }
    
    protected void readFields() {
        catalog = trailer.getAsDict(PdfName.ROOT);
        PdfDictionary fdf = catalog.getAsDict(PdfName.FDF);
        if (fdf == null)
            return;
        PdfString fs = fdf.getAsString(PdfName.F);
        if (fs != null)
            fileSpec = fs.toUnicodeString();
        PdfArray fld = fdf.getAsArray(PdfName.FIELDS);
        if (fld == null)
            return;
        encoding = fdf.getAsName(PdfName.ENCODING);
        PdfDictionary merged = new PdfDictionary();
        merged.put(PdfName.KIDS, fld);
        kidNode(merged, "");
    }

    /** Gets all the fields. The map is keyed by the fully qualified
     * field name and the value is a merged <CODE>PdfDictionary</CODE>
     * with the field content.
     * @return all the fields
     */    
    public HashMap getFields() {
        return fields;
    }
    
    /** Gets the field dictionary.
     * @param name the fully qualified field name
     * @return the field dictionary
     */    
    public PdfDictionary getField(String name) {
        return (PdfDictionary)fields.get(name);
    }
    
    /** Gets the field value or <CODE>null</CODE> if the field does not
     * exist or has no value defined.
     * @param name the fully qualified field name
     * @return the field value or <CODE>null</CODE>
     */    
    public String getFieldValue(String name) {
        PdfDictionary field = (PdfDictionary)fields.get(name);
        if (field == null)
            return null;
        PdfObject v = getPdfObject(field.get(PdfName.V));
        if (v == null)
            return null;
        if (v.isName())
            return PdfName.decodeName(((PdfName)v).toString());
        else if (v.isString()) {
            PdfString vs = (PdfString)v;
            if (encoding == null || vs.getEncoding() != null)
                return vs.toUnicodeString();
            byte b[] = vs.getBytes();
            if (b.length >= 2 && b[0] == (byte)254 && b[1] == (byte)255)
                return vs.toUnicodeString();
            try {
                if (encoding.equals(PdfName.SHIFT_JIS))
                    return new String(b, "SJIS");
                else if (encoding.equals(PdfName.UHC))
                    return new String(b, "MS949");
                else if (encoding.equals(PdfName.GBK))
                    return new String(b, "GBK");
                else if (encoding.equals(PdfName.BIGFIVE))
                    return new String(b, "Big5");
            }
            catch (Exception e) {
            }
            return vs.toUnicodeString();
        }
        return null;
    }
    
    /** Gets the PDF file specification contained in the FDF.
     * @return the PDF file specification contained in the FDF
     */    
    public String getFileSpec() {
        return fileSpec;
    }
}