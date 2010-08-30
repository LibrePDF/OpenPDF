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
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

import com.lowagie.text.DocWriter;

/** Writes an FDF form.
 * @author Paulo Soares (psoares@consiste.pt)
 */
public class FdfWriter {
    private static final byte[] HEADER_FDF = DocWriter.getISOBytes("%FDF-1.2\n%\u00e2\u00e3\u00cf\u00d3\n");
    HashMap fields = new HashMap();

    /** The PDF file associated with the FDF. */
    private String file;
    
    /** Creates a new FdfWriter. */    
    public FdfWriter() {
    }

    /** Writes the content to a stream.
     * @param os the stream
     * @throws IOException on error
     */    
    public void writeTo(OutputStream os) throws IOException {
        Wrt wrt = new Wrt(os, this);
        wrt.writeTo();
    }
    
    boolean setField(String field, PdfObject value) {
        HashMap map = fields;
        StringTokenizer tk = new StringTokenizer(field, ".");
        if (!tk.hasMoreTokens())
            return false;
        while (true) {
            String s = tk.nextToken();
            Object obj = map.get(s);
            if (tk.hasMoreTokens()) {
                if (obj == null) {
                    obj = new HashMap();
                    map.put(s, obj);
                    map = (HashMap)obj;
                    continue;
                }
                else if (obj instanceof HashMap)
                    map = (HashMap)obj;
                else
                    return false;
            }
            else {
                if (!(obj instanceof HashMap)) {
                    map.put(s, value);
                    return true;
                }
                else
                    return false;
            }
        }
    }
    
    void iterateFields(HashMap values, HashMap map, String name) {
        for (Iterator it = map.entrySet().iterator(); it.hasNext();) {
            Map.Entry entry = (Map.Entry) it.next();
            String s = (String) entry.getKey();
            Object obj = entry.getValue();
            if (obj instanceof HashMap)
                iterateFields(values, (HashMap)obj, name + "." + s);
            else
                values.put((name + "." + s).substring(1), obj);
        }
    }
    
    /** Removes the field value.
     * @param field the field name
     * @return <CODE>true</CODE> if the field was found and removed,
     * <CODE>false</CODE> otherwise
     */    
    public boolean removeField(String field) {
        HashMap map = fields;
        StringTokenizer tk = new StringTokenizer(field, ".");
        if (!tk.hasMoreTokens())
            return false;
        ArrayList hist = new ArrayList();
        while (true) {
            String s = tk.nextToken();
            Object obj = map.get(s);
            if (obj == null)
                return false;
            hist.add(map);
            hist.add(s);
            if (tk.hasMoreTokens()) {
                if (obj instanceof HashMap)
                    map = (HashMap)obj;
                else
                    return false;
            }
            else {
                if (obj instanceof HashMap)
                    return false;
                else
                    break;
            }
        }
        for (int k = hist.size() - 2; k >= 0; k -= 2) {
            map = (HashMap)hist.get(k);
            String s = (String)hist.get(k + 1);
            map.remove(s);
            if (!map.isEmpty())
                break;
        }
        return true;
    }
    
    /** Gets all the fields. The map is keyed by the fully qualified
     * field name and the values are <CODE>PdfObject</CODE>.
     * @return a map with all the fields
     */    
    public HashMap getFields() {
        HashMap values = new HashMap();
        iterateFields(values, fields, "");
        return values;
    }
    
    /** Gets the field value.
     * @param field the field name
     * @return the field value or <CODE>null</CODE> if not found
     */    
    public String getField(String field) {
        HashMap map = fields;
        StringTokenizer tk = new StringTokenizer(field, ".");
        if (!tk.hasMoreTokens())
            return null;
        while (true) {
            String s = tk.nextToken();
            Object obj = map.get(s);
            if (obj == null)
                return null;
            if (tk.hasMoreTokens()) {
                if (obj instanceof HashMap)
                    map = (HashMap)obj;
                else
                    return null;
            }
            else {
                if (obj instanceof HashMap)
                    return null;
                else {
                    if (((PdfObject)obj).isString())
                        return ((PdfString)obj).toUnicodeString();
                    else
                        return PdfName.decodeName(obj.toString());
                }
            }
        }
    }
    
    /** Sets the field value as a name.
     * @param field the fully qualified field name
     * @param value the value
     * @return <CODE>true</CODE> if the value was inserted,
     * <CODE>false</CODE> if the name is incompatible with
     * an existing field
     */    
    public boolean setFieldAsName(String field, String value) {
        return setField(field, new PdfName(value));
    }
    
    /** Sets the field value as a string.
     * @param field the fully qualified field name
     * @param value the value
     * @return <CODE>true</CODE> if the value was inserted,
     * <CODE>false</CODE> if the name is incompatible with
     * an existing field
     */    
    public boolean setFieldAsString(String field, String value) {
        return setField(field, new PdfString(value, PdfObject.TEXT_UNICODE));
    }
    
    /**
     * Sets the field value as a <CODE>PDFAction</CODE>. 
     * For example, this method allows setting a form submit button action using {@link PdfAction#createSubmitForm(String, Object[], int)}.
     * This method creates an <CODE>A</CODE> entry for the specified field in the underlying FDF file.
     * Method contributed by Philippe Laflamme (plaflamme)
     * @param field the fully qualified field name
     * @param action the field's action
     * @return <CODE>true</CODE> if the value was inserted,
     * <CODE>false</CODE> if the name is incompatible with
     * an existing field
     * @since	2.1.5
     */
    public boolean setFieldAsAction(String field, PdfAction action) {
    	return setField(field, action);
    }
    
    /** Sets all the fields from this <CODE>FdfReader</CODE>
     * @param fdf the <CODE>FdfReader</CODE>
     */    
    public void setFields(FdfReader fdf) {
        HashMap map = fdf.getFields();
        for (Iterator it = map.entrySet().iterator(); it.hasNext();) {
            Map.Entry entry = (Map.Entry) it.next();
            String key = (String) entry.getKey();
            PdfDictionary dic = (PdfDictionary) entry.getValue();
            PdfObject v = dic.get(PdfName.V);
            if (v != null) {
                setField(key, v);
            }
            v = dic.get(PdfName.A); // (plaflamme)
            if (v != null) {
            	setField(key, v);
            }
        }
    }
    
    /** Sets all the fields from this <CODE>PdfReader</CODE>
     * @param pdf the <CODE>PdfReader</CODE>
     */    
    public void setFields(PdfReader pdf) {
        setFields(pdf.getAcroFields());
    }
    
    /** Sets all the fields from this <CODE>AcroFields</CODE>
     * @param af the <CODE>AcroFields</CODE>
     */    
    public void setFields(AcroFields af) {
        for (Iterator it = af.getFields().entrySet().iterator(); it.hasNext();) {
            Map.Entry entry = (Map.Entry)it.next();
            String fn = (String)entry.getKey();
            AcroFields.Item item = (AcroFields.Item)entry.getValue();
            PdfDictionary dic = item.getMerged(0);
            PdfObject v = PdfReader.getPdfObjectRelease(dic.get(PdfName.V));
            if (v == null)
                continue;
            PdfObject ft = PdfReader.getPdfObjectRelease(dic.get(PdfName.FT));
            if (ft == null || PdfName.SIG.equals(ft))
                continue;
            setField(fn, v);
        }
    }
    
    /** Gets the PDF file name associated with the FDF.
     * @return the PDF file name associated with the FDF
     */
    public String getFile() {
        return this.file;
    }
    
    /** Sets the PDF file name associated with the FDF.
     * @param file the PDF file name associated with the FDF
     *
     */
    public void setFile(String file) {
        this.file = file;
    }
    
    static class Wrt extends PdfWriter {
        private FdfWriter fdf;
       
        Wrt(OutputStream os, FdfWriter fdf) throws IOException {
            super(new PdfDocument(), os);
            this.fdf = fdf;
            this.os.write(HEADER_FDF);
            body = new PdfBody(this);
        }
        
        void writeTo() throws IOException {
            PdfDictionary dic = new PdfDictionary();
            dic.put(PdfName.FIELDS, calculate(fdf.fields));
            if (fdf.file != null)
                dic.put(PdfName.F, new PdfString(fdf.file, PdfObject.TEXT_UNICODE));
            PdfDictionary fd = new PdfDictionary();
            fd.put(PdfName.FDF, dic);
            PdfIndirectReference ref = addToBody(fd).getIndirectReference();
            os.write(getISOBytes("trailer\n"));
            PdfDictionary trailer = new PdfDictionary();
            trailer.put(PdfName.ROOT, ref);
            trailer.toPdf(null, os);
            os.write(getISOBytes("\n%%EOF\n"));
            os.close();
        }
        
        
        PdfArray calculate(HashMap map) throws IOException {
            PdfArray ar = new PdfArray();
            for (Iterator it = map.entrySet().iterator(); it.hasNext();) {
                Map.Entry entry = (Map.Entry) it.next();
                String key = (String) entry.getKey();
                Object v = entry.getValue();
                PdfDictionary dic = new PdfDictionary();
                dic.put(PdfName.T, new PdfString(key, PdfObject.TEXT_UNICODE));
                if (v instanceof HashMap) {
                    dic.put(PdfName.KIDS, calculate((HashMap)v));
                }
                else if(v instanceof PdfAction) {	// (plaflamme)
                   	dic.put(PdfName.A, (PdfAction)v);
                }
                else {
                    dic.put(PdfName.V, (PdfObject)v);
                }
                ar.add(dic);
            }
            return ar;
        }
    }
}
