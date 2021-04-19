/*
 * $Id: PdfCopyFieldsImp.java 4065 2009-09-16 23:09:11Z psoares33 $
 * Copyright 2004 by Paulo Soares.
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
 * https://github.com/LibrePDF/OpenPDF
 */
package com.lowagie.text.pdf;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.ExceptionConverter;
import com.lowagie.text.error_messages.MessageLocalization;
import com.lowagie.text.exceptions.BadPasswordException;
import com.lowagie.text.pdf.AcroFields.Item;

/**
 * @author psoares
 */
class PdfCopyFieldsImp extends PdfWriter {

    private static final PdfName iTextTag = new PdfName("_iTextTag_");
    private static final Integer zero = 0;
    private final List<PdfReader> readers = new ArrayList<>();
    Map<PdfReader, IntHashtable> readers2intrefs = new HashMap<>();
    private final Map<PdfReader, IntHashtable> pages2intrefs = new HashMap<>();
    private final Map<PdfReader, IntHashtable> visited = new HashMap<>();
    List<AcroFields> fields = new ArrayList<>();
    RandomAccessFileOrArray file;
    Map<String, Object> fieldTree = new HashMap<>();
    List<PdfIndirectReference> pageRefs = new ArrayList<>();
    List<PdfDictionary> pageDics = new ArrayList<>();
    PdfDictionary resources = new PdfDictionary();
    PdfDictionary form;
    boolean closing = false;
    Document nd;
    private Map<PdfArray, List<Integer>> tabOrder;
    private final List<Object> calculationOrder = new ArrayList<>();
    private List<Object> calculationOrderRefs;
    private boolean hasSignature;
    
    PdfCopyFieldsImp(OutputStream os) throws DocumentException {
        this(os, '\0');
    }
    
    PdfCopyFieldsImp(OutputStream os, char pdfVersion) throws DocumentException {
        super(new PdfDocument(), os);
        pdf.addWriter(this);
        if (pdfVersion != 0)
            super.setPdfVersion(pdfVersion);
        nd = new Document();
        nd.addDocListener(pdf);
    }
    
    void addDocument(PdfReader reader, List<Integer> pagesToKeep) throws DocumentException, IOException {
        if (!readers2intrefs.containsKey(reader) && reader.isTampered())
            throw new DocumentException(MessageLocalization.getComposedMessage("the.document.was.reused"));
        reader = new PdfReader(reader);        
        reader.selectPages(pagesToKeep);
        if (reader.getNumberOfPages() == 0)
            return;
        reader.setTampered(false);
        addDocument(reader);
    }
    
    void addDocument(PdfReader reader) throws DocumentException, IOException {
        if (!reader.isOpenedWithFullPermissions())
            throw new BadPasswordException(MessageLocalization.getComposedMessage("pdfreader.not.opened.with.owner.password"));
        openDoc();
        if (readers2intrefs.containsKey(reader)) {
            reader = new PdfReader(reader);
        }
        else {
            if (reader.isTampered())
                throw new DocumentException(MessageLocalization.getComposedMessage("the.document.was.reused"));
            reader.consolidateNamedDestinations();
            reader.setTampered(true);
        }
        reader.shuffleSubsetNames();
        readers2intrefs.put(reader, new IntHashtable());
        readers.add(reader);
        int len = reader.getNumberOfPages();
        IntHashtable refs = new IntHashtable();
        for (int p = 1; p <= len; ++p) {
            refs.put(reader.getPageOrigRef(p).getNumber(), 1);
            reader.releasePage(p);
        }
        pages2intrefs.put(reader, refs);
        visited.put(reader, new IntHashtable());
        fields.add(reader.getAcroFields());
        updateCalculationOrder(reader);
    }

    private static String getCOName(PRIndirectReference ref) {
        StringBuilder name = new StringBuilder();
        while (ref != null) {
            PdfObject obj = PdfReader.getPdfObject(ref);
            if (obj == null || obj.type() != PdfObject.DICTIONARY) {
                break;
            }
            PdfDictionary dic = (PdfDictionary) obj;
            PdfString t = dic.getAsString(PdfName.T);
            if (t != null) {
                name.insert(0, t.toUnicodeString() + ".");
            }
            ref = (PRIndirectReference) dic.get(PdfName.PARENT);
        }
        if (name.toString().endsWith(".")) {
            name = new StringBuilder(name.substring(0, name.length() - 1));
        }
        return name.toString();
    }

    /**
     * @since    2.1.5; before 2.1.5 the method was private
     */
    protected void updateCalculationOrder(PdfReader reader) {
        PdfDictionary catalog = reader.getCatalog();
        PdfDictionary acro = catalog.getAsDict(PdfName.ACROFORM);
        if (acro == null)
            return;
        PdfArray co = acro.getAsArray(PdfName.CO);
        if (co == null || co.size() == 0)
            return;
        AcroFields af = reader.getAcroFields();
        for (int k = 0; k < co.size(); ++k) {
            PdfObject obj = co.getPdfObject(k);
            if (obj == null || !obj.isIndirect())
                continue;
            String name = getCOName((PRIndirectReference) obj);
            if (af.getFieldItem(name) == null)
                continue;
            name = "." + name;
            if (calculationOrder.contains(name))
                continue;
            calculationOrder.add(name);
        }
    }

    private void propagate(PdfObject obj, boolean restricted) {
        if (obj == null) {
            return;
        }
        if (obj instanceof PdfIndirectReference) {
            return;
        }
        switch (obj.type()) {
            case PdfObject.DICTIONARY:
            case PdfObject.STREAM: {
                PdfDictionary dic = (PdfDictionary) obj;
                for (PdfName key : dic.getKeys()) {
                    if (restricted && (key.equals(PdfName.PARENT) || key.equals(PdfName.KIDS)))
                        continue;
                    PdfObject ob = dic.get(key);
                    if (ob != null && ob.isIndirect()) {
                        PRIndirectReference ind = (PRIndirectReference) ob;
                        if (!setVisited(ind) && !isPage(ind)) {
                            PdfIndirectReference ref = getNewReference(ind);
                            propagate(PdfReader.getPdfObjectRelease(ind), restricted);
                        }
                    } else {
                        propagate(ob, restricted);
                    }
                }
                break;
            }
            case PdfObject.ARRAY: {
                //PdfArray arr = new PdfArray();
                for (PdfObject ob : ((PdfArray)obj).getElements()) {
                    if (ob != null && ob.isIndirect()) {
                        PRIndirectReference ind = (PRIndirectReference) ob;
                        if (!isVisited(ind) && !isPage(ind)) {
                            PdfIndirectReference ref = getNewReference(ind);
                            propagate(PdfReader.getPdfObjectRelease(ind), restricted);
                        }
                    } else {
                        propagate(ob, restricted);
                    }
                }
                break;
            }
            case PdfObject.INDIRECT: {
                throw new RuntimeException(MessageLocalization.getComposedMessage("reference.pointing.to.reference"));
            }
        }
    }
    
    private void adjustTabOrder(PdfArray annots, PdfIndirectReference ind, PdfNumber nn) {
        int v = nn.intValue();
        List<Integer> t = tabOrder.get(annots);
        if (t == null) {
            t = new ArrayList<>();
            int size = annots.size() - 1;
            for (int k = 0; k < size; ++k) {
                t.add(zero);
            }
            t.add(v);
            tabOrder.put(annots, t);
            annots.add(ind);
        }
        else {
            int size = t.size() - 1;
            for (int k = size; k >= 0; --k) {
                if (t.get(k) <= v) {
                    t.add(k + 1, v);
                    annots.add(k + 1, ind);
                    size = -2;
                    break;
                }
            }
            if (size != -2) {
                t.add(0, v);
                annots.add(0, ind);
            }
        }
    }

    /**
     * @deprecated use {@link PdfCopyFieldsImp#branchForm(Map, PdfIndirectReference, String)}
     */
    @Deprecated
    @SuppressWarnings({"unchecked", "rawtypes"})
    protected PdfArray branchForm(HashMap level, PdfIndirectReference parent, String fname) throws IOException {
        return branchForm((Map<String, Object>) level, parent, fname);
    }

    protected PdfArray branchForm(Map<String, Object> level, PdfIndirectReference parent, String fname) throws IOException {
        PdfArray arr = new PdfArray();
        for (Map.Entry<String, Object> entry : level.entrySet()) {
            String name = entry.getKey();
            PdfIndirectReference ind = getPdfIndirectReference();
            PdfDictionary dic = new PdfDictionary();
            if (parent != null)
                dic.put(PdfName.PARENT, parent);
            dic.put(PdfName.T, new PdfString(name, PdfObject.TEXT_UNICODE));
            String fname2 = fname + "." + name;
            int coidx = calculationOrder.indexOf(fname2);
            if (coidx >= 0)
                calculationOrderRefs.set(coidx, ind);
            Object obj = entry.getValue();
            if (obj instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> map = (Map<String, Object>) obj;
                dic.put(PdfName.KIDS, branchForm(map, ind, fname2));
                arr.add(ind);
                addToBody(dic, ind);
            } else {
                List<?> list = (List<?>) obj;
                dic.mergeDifferent((PdfDictionary) list.get(0));
                if (list.size() == 3) {
                    dic.mergeDifferent((PdfDictionary) list.get(2));
                    int page = (Integer) list.get(1);
                    PdfDictionary pageDic = pageDics.get(page - 1);
                    PdfArray annots = pageDic.getAsArray(PdfName.ANNOTS);
                    if (annots == null) {
                        annots = new PdfArray();
                        pageDic.put(PdfName.ANNOTS, annots);
                    }
                    PdfNumber nn = (PdfNumber) dic.get(iTextTag);
                    dic.remove(iTextTag);
                    adjustTabOrder(annots, ind, nn);
                } else {
                    PdfArray kids = new PdfArray();
                    for (int k = 1; k < list.size(); k += 2) {
                        int page = (Integer) list.get(k);
                        PdfDictionary pageDic = pageDics.get(page - 1);
                        PdfArray annots = pageDic.getAsArray(PdfName.ANNOTS);
                        if (annots == null) {
                            annots = new PdfArray();
                            pageDic.put(PdfName.ANNOTS, annots);
                        }
                        PdfDictionary widget = new PdfDictionary();
                        widget.merge((PdfDictionary) list.get(k + 1));
                        widget.put(PdfName.PARENT, ind);
                        PdfNumber nn = (PdfNumber) widget.get(iTextTag);
                        widget.remove(iTextTag);
                        PdfIndirectReference wref = addToBody(widget).getIndirectReference();
                        adjustTabOrder(annots, wref, nn);
                        kids.add(wref);
                        propagate(widget, false);
                    }
                    dic.put(PdfName.KIDS, kids);
                }
                arr.add(ind);
                addToBody(dic, ind);
                propagate(dic, false);
            }
        }
        return arr;
    }
    
    protected void createAcroForms() throws IOException {
        if (fieldTree.isEmpty())
            return;
        form = new PdfDictionary();
        form.put(PdfName.DR, resources);
        propagate(resources, false);
        form.put(PdfName.DA, new PdfString("/Helv 0 Tf 0 g "));
        tabOrder = new HashMap<>();
        calculationOrderRefs = new ArrayList<>(calculationOrder);
        form.put(PdfName.FIELDS, branchForm(fieldTree, null, ""));
        if (hasSignature)
            form.put(PdfName.SIGFLAGS, new PdfNumber(3));
        PdfArray co = new PdfArray();
        for (Object obj : calculationOrderRefs) {
            if (obj instanceof PdfIndirectReference)
                co.add((PdfIndirectReference) obj);
        }
        if (co.size() > 0)
            form.put(PdfName.CO, co);
    }
    
    public void close() {
        if (closing) {
            super.close();
            return;
        }
        closing = true;
        try {
            closeIt();
        }
        catch (Exception e) {
            throw new ExceptionConverter(e);
        }
    }
    
    /**
     * Creates the new PDF by merging the fields and forms.
     */
    protected void closeIt() throws IOException {
        for (PdfReader reader : readers) {
            reader.removeFields();
        }
        for (PdfReader reader : readers) {
            for (int page = 1; page <= reader.getNumberOfPages(); ++page) {
                pageRefs.add(getNewReference(reader.getPageOrigRef(page)));
                pageDics.add(reader.getPageN(page));
            }
        }
        mergeFields();
        createAcroForms();
        for (PdfReader reader : readers) {
            for (int page = 1; page <= reader.getNumberOfPages(); ++page) {
                PdfDictionary dic = reader.getPageN(page);
                PdfIndirectReference pageRef = getNewReference(reader.getPageOrigRef(page));
                PdfIndirectReference parent = root.addPageRef(pageRef);
                dic.put(PdfName.PARENT, parent);
                propagate(dic, false);
            }
        }
        for (Map.Entry<PdfReader, IntHashtable> entry : readers2intrefs.entrySet()) {
            PdfReader reader = entry.getKey();
            try {
                file = reader.getSafeFile();
                file.reOpen();
                IntHashtable t = entry.getValue();
                int[] keys = t.toOrderedKeys();
                for (int key : keys) {
                    PRIndirectReference ref = new PRIndirectReference(reader, key);
                    addToBody(PdfReader.getPdfObjectRelease(ref), t.get(key));
                }
            } finally {
                try {
                    file.close();
                    reader.close();
                } catch (Exception e) {
                    // empty on purpose
                }
            }
        }
        pdf.close();
    }
    
    void addPageOffsetToField(Map<String, Item> fd, int pageOffset) {
        if (pageOffset == 0)
            return;
        for (Item item : fd.values()) {
            for (int k = 0; k < item.size(); ++k) {
                int p = item.getPage(k);
                item.forcePage(k, p + pageOffset);
            }
        }
    }

    void createWidgets(List<Object> list, Item item) {
        for (int k = 0; k < item.size(); ++k) {
            list.add(item.getPage(k)); // add an Integer
            PdfDictionary merged = item.getMerged(k);
            PdfObject dr = merged.get(PdfName.DR);
            if (dr != null)
                PdfFormField.mergeResources(resources, (PdfDictionary)PdfReader.getPdfObject(dr));
            PdfDictionary widget = new PdfDictionary();
            for (PdfName key : merged.getKeys()) {
                if (widgetKeys.containsKey(key))
                    widget.put(key, merged.get(key));
            }
            widget.put(iTextTag, new PdfNumber(item.getTabOrder(k) + 1));
            list.add(widget); // add a PdfDictionary
        }
    }

    void mergeField(String name, Item item) {
        Map<String, Object> map = fieldTree;
        StringTokenizer tk = new StringTokenizer(name, ".");
        if (!tk.hasMoreTokens())
            return;
        while (true) {
            String s = tk.nextToken();
            Object obj = map.get(s);
            if (tk.hasMoreTokens()) {
                if (obj == null) {
                    Map<String, Object> tempMap = new HashMap<>();
                    map.put(s, tempMap);
                    map = tempMap;
                } else if (obj instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> castMap = (Map<String, Object>) obj;
                    map = castMap;
                }
                else
                    return;
            }
            else {
                if (obj instanceof Map) {
                    return;
                }
                PdfDictionary merged = item.getMerged(0);
                if (obj == null) {
                    PdfDictionary field = new PdfDictionary();
                    if (PdfName.SIG.equals(merged.get(PdfName.FT)))
                        hasSignature = true;
                    for (PdfName key : merged.getKeys()) {
                        if (fieldKeys.containsKey(key))
                            field.put(key, merged.get(key));
                    }
                    List<Object> list = new ArrayList<>();
                    list.add(field);
                    createWidgets(list, item);
                    map.put(s, list);
                }
                else {
                    @SuppressWarnings("unchecked")
                    List<Object> list = (List<Object>) obj;
                    PdfDictionary field = (PdfDictionary)list.get(0);
                    PdfName type1 = (PdfName)field.get(PdfName.FT);
                    PdfName type2 = (PdfName)merged.get(PdfName.FT);
                    if (type1 == null || !type1.equals(type2))
                        return;
                    int flag1 = 0;
                    PdfObject f1 = field.get(PdfName.FF);
                    if (f1 != null && f1.isNumber())
                        flag1 = ((PdfNumber)f1).intValue();
                    int flag2 = 0;
                    PdfObject f2 = merged.get(PdfName.FF);
                    if (f2 != null && f2.isNumber())
                        flag2 = ((PdfNumber)f2).intValue();
                    if (type1.equals(PdfName.BTN)) {
                        if (((flag1 ^ flag2) & PdfFormField.FF_PUSHBUTTON) != 0)
                            return;
                        if ((flag1 & PdfFormField.FF_PUSHBUTTON) == 0 && ((flag1 ^ flag2) & PdfFormField.FF_RADIO) != 0)
                            return;
                    }
                    else if (type1.equals(PdfName.CH)) {
                        if (((flag1 ^ flag2) & PdfFormField.FF_COMBO) != 0)
                            return;
                    }
                    createWidgets(list, item);
                }
                return;
            }
        }
    }
    
    void mergeWithMaster(Map<String, Item> fd) {
        for (Map.Entry<String, Item> entry : fd.entrySet()) {
            String name = entry.getKey();
            mergeField(name, entry.getValue());
        }
    }
    
    void mergeFields() {
        int pageOffset = 0;
        for (int k = 0; k < fields.size(); ++k) {
            Map<String, Item> fd = fields.get(k).getAllFields();
            addPageOffsetToField(fd, pageOffset);
            mergeWithMaster(fd);
            pageOffset += readers.get(k).getNumberOfPages();
        }
    }

    public PdfIndirectReference getPageReference(int page) {
        return pageRefs.get(page - 1);
    }
    
    protected PdfDictionary getCatalog(PdfIndirectReference rootObj) {
        try {
            PdfDictionary cat = pdf.getCatalog(rootObj);
            if (form != null) {
                PdfIndirectReference ref = addToBody(form).getIndirectReference();
                cat.put(PdfName.ACROFORM, ref);
            }
            return cat;
        }
        catch (IOException e) {
            throw new ExceptionConverter(e);
        }
    }

    protected PdfIndirectReference getNewReference(PRIndirectReference ref) {
        return new PdfIndirectReference(0, getNewObjectNumber(ref.getReader(), ref.getNumber(), 0));
    }
    
    protected int getNewObjectNumber(PdfReader reader, int number, int generation) {
        IntHashtable refs = readers2intrefs.get(reader);
        int n = refs.get(number);
        if (n == 0) {
            n = getIndirectReferenceNumber();
            refs.put(number, n);
        }
        return n;
    }
    
    
    /**
     * Sets a reference to "visited" in the copy process.
     * @param    ref    the reference that needs to be set to "visited"
     * @return    true if the reference was set to visited
     */
    protected boolean setVisited(PRIndirectReference ref) {
        IntHashtable refs = visited.get(ref.getReader());
        if (refs != null)
            return (refs.put(ref.getNumber(), 1) != 0);
        else
            return false;
    }
    
    /**
     * Checks if a reference has already been "visited" in the copy process.
     * @param    ref    the reference that needs to be checked
     * @return    true if the reference was already visited
     */
    protected boolean isVisited(PRIndirectReference ref) {
        IntHashtable refs = visited.get(ref.getReader());
        if (refs != null)
            return refs.containsKey(ref.getNumber());
        else
            return false;
    }
    
    protected boolean isVisited(PdfReader reader, int number) {
        IntHashtable refs = readers2intrefs.get(reader);
        return refs.containsKey(number);
    }

    /**
     * Checks if a reference refers to a page object.
     * @param    ref    the reference that needs to be checked
     * @return    true is the reference refers to a page object.
     */
    protected boolean isPage(PRIndirectReference ref) {
        IntHashtable refs = pages2intrefs.get(ref.getReader());
        if (refs != null)
            return refs.containsKey(ref.getNumber());
        else
            return false;
    }

    RandomAccessFileOrArray getReaderFile(PdfReader reader) {
            return file;
    }

    public void openDoc() {
        if (!nd.isOpen())
            nd.open();
    }    
    
    protected static final Map<PdfName, Integer> widgetKeys = new HashMap<>();
    protected static final Map<PdfName, Integer> fieldKeys = new HashMap<>();
    static {
        Integer one = 1;
        widgetKeys.put(PdfName.SUBTYPE, one);
        widgetKeys.put(PdfName.CONTENTS, one);
        widgetKeys.put(PdfName.RECT, one);
        widgetKeys.put(PdfName.NM, one);
        widgetKeys.put(PdfName.M, one);
        widgetKeys.put(PdfName.F, one);
        widgetKeys.put(PdfName.BS, one);
        widgetKeys.put(PdfName.BORDER, one);
        widgetKeys.put(PdfName.AP, one);
        widgetKeys.put(PdfName.AS, one);
        widgetKeys.put(PdfName.C, one);
        widgetKeys.put(PdfName.A, one);
        widgetKeys.put(PdfName.STRUCTPARENT, one);
        widgetKeys.put(PdfName.OC, one);
        widgetKeys.put(PdfName.H, one);
        widgetKeys.put(PdfName.MK, one);
        widgetKeys.put(PdfName.DA, one);
        widgetKeys.put(PdfName.Q, one);
        fieldKeys.put(PdfName.AA, one);
        fieldKeys.put(PdfName.FT, one);
        fieldKeys.put(PdfName.TU, one);
        fieldKeys.put(PdfName.TM, one);
        fieldKeys.put(PdfName.FF, one);
        fieldKeys.put(PdfName.V, one);
        fieldKeys.put(PdfName.DV, one);
        fieldKeys.put(PdfName.DS, one);
        fieldKeys.put(PdfName.RV, one);
        fieldKeys.put(PdfName.OPT, one);
        fieldKeys.put(PdfName.MAXLEN, one);
        fieldKeys.put(PdfName.TI, one);
        fieldKeys.put(PdfName.I, one);
        fieldKeys.put(PdfName.LOCK, one);
        fieldKeys.put(PdfName.SV, one);
    }
}
