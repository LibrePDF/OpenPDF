/*
 * $Id: PdfCopyFormsImp.java 4065 2009-09-16 23:09:11Z psoares33 $
 *
 * Copyright 2009 Bruno Lowagie (inspired by Paulo Soares)
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
 * the Initial Developer are Copyright (C) 1999-2009 by Bruno Lowagie.
 * All Rights Reserved.
 * Co-Developer of the code is Paulo Soares. Portions created by the Co-Developer
 * are Copyright (C) 2000-2009 by Paulo Soares. All Rights Reserved.
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

import com.lowagie.text.DocumentException;
import java.io.OutputStream;
import java.util.HashMap;
import com.lowagie.text.error_messages.MessageLocalization;

/**
 * Allows you to add one (or more) existing PDF document(s)
 * and add the form(s) of (an)other PDF document(s).
 * @since 2.1.5
 */
class PdfCopyFormsImp extends PdfCopyFieldsImp {

    /**
   * This sets up the output document 
   * @param os The Outputstream pointing to the output document
   * @throws DocumentException
   */
    PdfCopyFormsImp(OutputStream os) throws DocumentException {
        super(os);
    }
    
    /**
     * This method feeds in the source document
     * @param reader The PDF reader containing the source document
     * @throws DocumentException
     */
    public void copyDocumentFields(PdfReader reader) throws DocumentException {
    	if (!reader.isOpenedWithFullPermissions())
            throw new IllegalArgumentException(MessageLocalization.getComposedMessage("pdfreader.not.opened.with.owner.password"));
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
        fields.add(reader.getAcroFields());
        updateCalculationOrder(reader);
    }

    /**
     * This merge fields is slightly different from the mergeFields method
     * of PdfCopyFields.
     */
    void mergeFields() {
        for (int k = 0; k < fields.size(); ++k) {
            HashMap fd = ((AcroFields)fields.get(k)).getFields();
            mergeWithMaster(fd);
        }
    }

}
