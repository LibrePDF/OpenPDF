/*
 * $Id: PdfAnnotationsImp.java 3912 2009-04-26 08:38:15Z blowagie $
 *
 * Copyright 2006 Bruno Lowagie
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

package com.lowagie.text.pdf.internal;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import com.lowagie.text.Annotation;
import com.lowagie.text.ExceptionConverter;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfAcroForm;
import com.lowagie.text.pdf.PdfAction;
import com.lowagie.text.pdf.PdfAnnotation;
import com.lowagie.text.pdf.PdfArray;
import com.lowagie.text.pdf.PdfFileSpecification;
import com.lowagie.text.pdf.PdfFormField;
import com.lowagie.text.pdf.PdfName;
import com.lowagie.text.pdf.PdfObject;
import com.lowagie.text.pdf.PdfRectangle;
import com.lowagie.text.pdf.PdfString;
import com.lowagie.text.pdf.PdfWriter;

public class PdfAnnotationsImp {

    /**
     * This is the AcroForm object for the complete document.
     */
    protected PdfAcroForm acroForm;

    /**
     * This is the array containing the references to annotations
     * that were added to the document.
     */
    protected ArrayList annotations;
    
    /**
     * This is an array containing references to some delayed annotations
     * (that were added for a page that doesn't exist yet).
     */
    protected ArrayList delayedAnnotations = new ArrayList();
    
    
    public PdfAnnotationsImp(PdfWriter writer) {
    	acroForm = new PdfAcroForm(writer);
    }
    
    /**
     * Checks if the AcroForm is valid.
     */
    public boolean hasValidAcroForm() {
    	return acroForm.isValid();
    }
    
    /**
     * Gets the AcroForm object.
     * @return the PdfAcroform object of the PdfDocument
     */
    public PdfAcroForm getAcroForm() {
        return acroForm;
    }
    
    public void setSigFlags(int f) {
        acroForm.setSigFlags(f);
    }
    
    public void addCalculationOrder(PdfFormField formField) {
        acroForm.addCalculationOrder(formField);
    }
    
    public void addAnnotation(PdfAnnotation annot) {
        if (annot.isForm()) {
            PdfFormField field = (PdfFormField)annot;
            if (field.getParent() == null)
                addFormFieldRaw(field);
        }
        else
            annotations.add(annot);
    }
    
    public void addPlainAnnotation(PdfAnnotation annot) {
    	annotations.add(annot);
    }
    
    void addFormFieldRaw(PdfFormField field) {
        annotations.add(field);
        ArrayList kids = field.getKids();
        if (kids != null) {
            for (int k = 0; k < kids.size(); ++k)
                addFormFieldRaw((PdfFormField)kids.get(k));
        }
    }
    
    public boolean hasUnusedAnnotations() {
    	return !annotations.isEmpty();
    }

    public void resetAnnotations() {
        annotations = delayedAnnotations;
        delayedAnnotations = new ArrayList();
    }
    
    public PdfArray rotateAnnotations(PdfWriter writer, Rectangle pageSize) {
        PdfArray array = new PdfArray();
        int rotation = pageSize.getRotation() % 360;
        int currentPage = writer.getCurrentPageNumber();
        for (int k = 0; k < annotations.size(); ++k) {
            PdfAnnotation dic = (PdfAnnotation)annotations.get(k);
            int page = dic.getPlaceInPage();
            if (page > currentPage) {
                delayedAnnotations.add(dic);
                continue;
            }
            if (dic.isForm()) {
                if (!dic.isUsed()) {
                    HashMap templates = dic.getTemplates();
                    if (templates != null)
                        acroForm.addFieldTemplates(templates);
                }
                PdfFormField field = (PdfFormField)dic;
                if (field.getParent() == null)
                    acroForm.addDocumentField(field.getIndirectReference());
            }
            if (dic.isAnnotation()) {
                array.add(dic.getIndirectReference());
                if (!dic.isUsed()) {
                    PdfRectangle rect = (PdfRectangle)dic.get(PdfName.RECT);
                    if (rect != null) {
                    	switch (rotation) {
                        	case 90:
                        		dic.put(PdfName.RECT, new PdfRectangle(
                        				pageSize.getTop() - rect.bottom(),
										rect.left(),
										pageSize.getTop() - rect.top(),
										rect.right()));
                        		break;
                        	case 180:
                        		dic.put(PdfName.RECT, new PdfRectangle(
                        				pageSize.getRight() - rect.left(),
										pageSize.getTop() - rect.bottom(),
										pageSize.getRight() - rect.right(),
										pageSize.getTop() - rect.top()));
                        		break;
                        	case 270:
                        		dic.put(PdfName.RECT, new PdfRectangle(
                        				rect.bottom(),
										pageSize.getRight() - rect.left(),
										rect.top(),
										pageSize.getRight() - rect.right()));
                        		break;
                    	}
                    }
                }
            }
            if (!dic.isUsed()) {
                dic.setUsed();
                try {
                    writer.addToBody(dic, dic.getIndirectReference());
                }
                catch (IOException e) {
                    throw new ExceptionConverter(e);
                }
            }
        }
        return array;
    }
    
    public static PdfAnnotation convertAnnotation(PdfWriter writer, Annotation annot, Rectangle defaultRect) throws IOException {
        switch(annot.annotationType()) {
           case Annotation.URL_NET:
               return new PdfAnnotation(writer, annot.llx(), annot.lly(), annot.urx(), annot.ury(), new PdfAction((URL) annot.attributes().get(Annotation.URL)));
           case Annotation.URL_AS_STRING:
               return new PdfAnnotation(writer, annot.llx(), annot.lly(), annot.urx(), annot.ury(), new PdfAction((String) annot.attributes().get(Annotation.FILE)));
           case Annotation.FILE_DEST:
               return new PdfAnnotation(writer, annot.llx(), annot.lly(), annot.urx(), annot.ury(), new PdfAction((String) annot.attributes().get(Annotation.FILE), (String) annot.attributes().get(Annotation.DESTINATION)));
           case Annotation.SCREEN:
               boolean sparams[] = (boolean[])annot.attributes().get(Annotation.PARAMETERS);
               String fname = (String) annot.attributes().get(Annotation.FILE);
               String mimetype = (String) annot.attributes().get(Annotation.MIMETYPE);
               PdfFileSpecification fs;
               if (sparams[0])
                   fs = PdfFileSpecification.fileEmbedded(writer, fname, fname, null);
               else
                   fs = PdfFileSpecification.fileExtern(writer, fname);
               PdfAnnotation ann = PdfAnnotation.createScreen(writer, new Rectangle(annot.llx(), annot.lly(), annot.urx(), annot.ury()),
                       fname, fs, mimetype, sparams[1]);
               return ann;
           case Annotation.FILE_PAGE:
               return new PdfAnnotation(writer, annot.llx(), annot.lly(), annot.urx(), annot.ury(), new PdfAction((String) annot.attributes().get(Annotation.FILE), ((Integer) annot.attributes().get(Annotation.PAGE)).intValue()));
           case Annotation.NAMED_DEST:
               return new PdfAnnotation(writer, annot.llx(), annot.lly(), annot.urx(), annot.ury(), new PdfAction(((Integer) annot.attributes().get(Annotation.NAMED)).intValue()));
           case Annotation.LAUNCH:
               return new PdfAnnotation(writer, annot.llx(), annot.lly(), annot.urx(), annot.ury(), new PdfAction((String) annot.attributes().get(Annotation.APPLICATION),(String) annot.attributes().get(Annotation.PARAMETERS),(String) annot.attributes().get(Annotation.OPERATION),(String) annot.attributes().get(Annotation.DEFAULTDIR)));
           default:
        	   return new PdfAnnotation(writer, defaultRect.getLeft(), defaultRect.getBottom(), defaultRect.getRight(), defaultRect.getTop(), new PdfString(annot.title(), PdfObject.TEXT_UNICODE), new PdfString(annot.content(), PdfObject.TEXT_UNICODE));
       }
   }
}
