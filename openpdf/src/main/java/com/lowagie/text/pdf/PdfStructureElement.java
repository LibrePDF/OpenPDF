/*
 * $Id: PdfStructureElement.java 4065 2009-09-16 23:09:11Z psoares33 $
 *
 * Copyright 2005 by Paulo Soares.
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

import com.lowagie.text.error_messages.MessageLocalization;

/**
 * This is a node in a document logical structure. It may contain a mark point or it may contain other nodes.
 *
 * @author Paulo Soares (psoares@consiste.pt)
 */
public class PdfStructureElement extends PdfDictionary {

    /**
     * Holds value of property kids.
     */
    private PdfStructureElement parent;
    private PdfStructureTreeRoot top;

    /**
     * Holds value of property reference.
     */
    private PdfIndirectReference reference;

    /**
     * Creates a new instance of PdfStructureElement.
     *
     * @param parent        the parent of this node
     * @param structureType the type of structure. It may be a standard type or a user type mapped by the role map
     */
    public PdfStructureElement(PdfStructureElement parent, PdfName structureType) {
        top = parent.top;
        init(parent, structureType);
        this.parent = parent;
        put(PdfName.P, parent.reference);
        put(PdfName.TYPE, new PdfName("StructElem"));
    }

    /**
     * Creates a new instance of PdfStructureElement.
     *
     * @param parent        the parent of this node
     * @param structureType the type of structure. It may be a standard type or a user type mapped by the role map
     */
    public PdfStructureElement(PdfStructureTreeRoot parent, PdfName structureType) {
        top = parent;
        init(parent, structureType);
        put(PdfName.P, parent.getReference());
        put(PdfName.TYPE, new PdfName("StructElem"));
    }

    private void init(PdfDictionary parent, PdfName structureType) {
        PdfObject kido = parent.get(PdfName.K);
        PdfArray kids = null;
        if (kido != null && !kido.isArray()) {
            throw new IllegalArgumentException(
                    MessageLocalization.getComposedMessage("the.parent.has.already.another.function"));
        }
        if (kido == null) {
            kids = new PdfArray();
            parent.put(PdfName.K, kids);
        } else {
            kids = (PdfArray) kido;
        }
        kids.add(this);
        put(PdfName.S, structureType);
        reference = top.getWriter().getPdfIndirectReference();
    }

    /**
     * Gets the parent of this node.
     *
     * @return the parent of this node
     */
    public PdfDictionary getParent() {
        return parent;
    }

    void setPageMark(int page, int mark) {
        if (mark >= 0) {
            put(PdfName.K, new PdfNumber(mark));
        }
        top.setPageMark(page, reference);
    }

    /**
     * Gets the reference this object will be written to.
     *
     * @return the reference this object will be written to
     * @since 2.1.6 method removed in 2.1.5, but restored in 2.1.6
     */
    public PdfIndirectReference getReference() {
        return this.reference;
    }

    /**
     * References a whole annotation from this structure element, as required to tag annotations
     * (typically links) in a tagged or PDF/UA document. This
     * <ul>
     *   <li>adds an OBJR entry pointing at the annotation to this element's kids,</li>
     *   <li>stores a unique {@code StructParent} key on the annotation, and</li>
     *   <li>registers that key in the structure parent tree, mapping it directly to this element.</li>
     * </ul>
     * The annotation must already have been added to the document (for example through
     * {@link PdfWriter#addAnnotation(PdfAnnotation)}) and must live on the current page. Any visible
     * content of the annotation (such as the link text) should be marked before calling this method so
     * that it becomes a preceding kid of this element.
     *
     * @param annotation the annotation to reference; its indirect reference is used for the OBJR entry
     */
    public void addAnnotation(PdfAnnotation annotation) {
        PdfWriter writer = top.getWriter();
        int structParent = top.obtainStructureParentIndex();
        annotation.put(PdfName.STRUCTPARENT, new PdfNumber(structParent));
        top.setObjectParent(structParent, reference);

        PdfDictionary objr = new PdfDictionary(PdfName.OBJR);
        objr.put(PdfName.OBJ, annotation.getIndirectReference());
        objr.put(PdfName.PG, writer.getCurrentPage());
        addKid(objr);
    }

    private void addKid(PdfObject kid) {
        PdfObject k = get(PdfName.K);
        PdfArray kids;
        if (k == null) {
            kids = new PdfArray();
            put(PdfName.K, kids);
        } else if (k.isArray()) {
            kids = (PdfArray) k;
        } else {
            kids = new PdfArray();
            kids.add(k);
            put(PdfName.K, kids);
        }
        kids.add(kid);
    }
}
