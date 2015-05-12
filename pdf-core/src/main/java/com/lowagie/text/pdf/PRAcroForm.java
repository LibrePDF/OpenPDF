/*
 * $Id: PRAcroForm.java 3735 2009-02-26 01:44:03Z xlv $
 *
 * Copyright 2001, 2002 by Paulo Soares.
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
 * This class written by Mark Thompson, Copyright (C) 2002 by Mark Thompson.
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * This class captures an AcroForm on input. Basically, it extends Dictionary
 * by indexing the fields of an AcroForm
 * @author Mark Thompson
 */

public class PRAcroForm extends PdfDictionary {
    
    /**
     * This class holds the information for a single field
     */
    public static class FieldInformation {
        String name;
        PdfDictionary info;
        PRIndirectReference ref;
        
        FieldInformation(String name, PdfDictionary info, PRIndirectReference ref) {
            this.name = name; this.info = info; this.ref = ref;
        }
        public String getName() { return name; }
        public PdfDictionary getInfo() { return info; }
        public PRIndirectReference getRef() { return ref; }
    };
    ArrayList fields;
    ArrayList stack;
    HashMap fieldByName;
    PdfReader reader;
    
    /**
     * Constructor
     * @param reader reader of the input file
     */
    public PRAcroForm(PdfReader reader) {
        this.reader = reader;
        fields = new ArrayList();
        fieldByName = new HashMap();
        stack = new ArrayList();
    }
    /**
     * Number of fields found
     * @return size
     */
    public int size() {
        return fields.size();
    }
    
    public ArrayList getFields() {
        return fields;
    }
    
    public FieldInformation getField(String name) {
        return (FieldInformation)fieldByName.get(name);
    }
    
    /**
     * Given the title (/T) of a reference, return the associated reference
     * @param name a string containing the path
     * @return a reference to the field, or null
     */
    public PRIndirectReference getRefByName(String name) {
        FieldInformation fi = (FieldInformation)fieldByName.get(name);
        if (fi == null) return null;
        return fi.getRef();
    }
    /**
     * Read, and comprehend the acroform
     * @param root the document root
     */
    public void readAcroForm(PdfDictionary root) {
        if (root == null)
            return;
        hashMap = root.hashMap;
        pushAttrib(root);
        PdfArray fieldlist = (PdfArray)PdfReader.getPdfObjectRelease(root.get(PdfName.FIELDS));
        iterateFields(fieldlist, null, null);
    }
    
    /**
     * After reading, we index all of the fields. Recursive.
     * @param fieldlist An array of fields
     * @param fieldDict the last field dictionary we encountered (recursively)
     * @param title the pathname of the field, up to this point or null
     */
    protected void iterateFields(PdfArray fieldlist, PRIndirectReference fieldDict, String title) {
        for (Iterator it = fieldlist.listIterator(); it.hasNext();) {
            PRIndirectReference ref = (PRIndirectReference)it.next();
            PdfDictionary dict = (PdfDictionary) PdfReader.getPdfObjectRelease(ref);
            
            // if we are not a field dictionary, pass our parent's values
            PRIndirectReference myFieldDict = fieldDict;
            String myTitle = title;
            PdfString tField = (PdfString)dict.get(PdfName.T);
            boolean isFieldDict = tField != null;
            
            if (isFieldDict) {
                myFieldDict = ref;
                if (title == null) myTitle = tField.toString();
                else myTitle = title + '.' + tField.toString();
            }
            
            PdfArray kids = (PdfArray)dict.get(PdfName.KIDS);
            if (kids != null) {
                pushAttrib(dict);
                iterateFields(kids, myFieldDict, myTitle);
                stack.remove(stack.size() - 1);   // pop
            }
            else {          // leaf node
                if (myFieldDict != null) {
                    PdfDictionary mergedDict = (PdfDictionary)stack.get(stack.size() - 1);
                    if (isFieldDict)
                        mergedDict = mergeAttrib(mergedDict, dict);
                    
                    mergedDict.put(PdfName.T, new PdfString(myTitle));
                    FieldInformation fi = new FieldInformation(myTitle, mergedDict, myFieldDict);
                    fields.add(fi);
                    fieldByName.put(myTitle, fi);
                }
            }
        }
    }
    /**
     * merge field attributes from two dictionaries
     * @param parent one dictionary
     * @param child the other dictionary
     * @return a merged dictionary
     */
    protected PdfDictionary mergeAttrib(PdfDictionary parent, PdfDictionary child) {
        PdfDictionary targ = new PdfDictionary();
        if (parent != null) targ.putAll(parent);
        
        for (Iterator it = child.getKeys().iterator(); it.hasNext();) {
            PdfName key = (PdfName) it.next();
            if (key.equals(PdfName.DR) || key.equals(PdfName.DA) ||
            key.equals(PdfName.Q)  || key.equals(PdfName.FF) ||
            key.equals(PdfName.DV) || key.equals(PdfName.V)
            || key.equals(PdfName.FT)
            || key.equals(PdfName.F)) {
                targ.put(key,child.get(key));
            }
        }
        return targ;
    }
    /**
     * stack a level of dictionary. Merge in a dictionary from this level
     */
    protected void pushAttrib(PdfDictionary dict) {
        PdfDictionary dic = null;
        if (!stack.isEmpty()) {
            dic = (PdfDictionary)stack.get(stack.size() - 1);
        }
        dic = mergeAttrib(dic, dict);
        stack.add(dic);
    }
}
