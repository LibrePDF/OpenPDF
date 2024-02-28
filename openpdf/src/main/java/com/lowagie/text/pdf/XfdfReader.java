/*
 *
 * Copyright 2004 by Leonard Rosenthol.
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
import com.lowagie.text.xml.simpleparser.SimpleXMLDocHandler;
import com.lowagie.text.xml.simpleparser.SimpleXMLParser;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 * Reads a XFDF.
 *
 * @author Leonard Rosenthol (leonardr@pdfsages.com)
 */
public class XfdfReader implements SimpleXMLDocHandler, FieldReader {

    // stuff used during parsing to handle state
    private boolean foundRoot = false;
    private Stack<String> fieldNames = new Stack<>();
    private Stack<String> fieldValues = new Stack<>();

    // storage for the field list and their values
    private Map<String, String> fields;
    /**
     * Storage for field values if there's more than one value for a field.
     *
     * @since 2.1.4
     */
    private Map<String, List<String>> listFields;

    // storage for the path to referenced PDF, if any
    private String fileSpec;

    /**
     * Reads an XFDF form.
     *
     * @param filename the file name of the form
     * @throws IOException on error
     */
    public XfdfReader(String filename) throws IOException {
        try (FileInputStream fin = new FileInputStream(filename)) {
            SimpleXMLParser.parse(this, fin);
        }
    }

    /**
     * Reads an XFDF form.
     *
     * @param xfdfIn the byte array with the form
     * @throws IOException on error
     */
    public XfdfReader(byte[] xfdfIn) throws IOException {
        SimpleXMLParser.parse(this, new ByteArrayInputStream(xfdfIn));
    }

    /**
     * Gets all the fields. The map is keyed by the fully qualified field name and the value is a merged
     * <CODE>PdfDictionary</CODE> with the
     * field content.
     *
     * @deprecated use {@link #getAllFields()}
     * @return all the fields
     */
    @Override
    @Deprecated
    public HashMap<String, String> getFields() {
        return (HashMap<String, String>) fields;
    }

    @Override
    public Map<String, String> getAllFields() {
        return fields;
    }

    /**
     * Gets the field value.
     *
     * @param name the fully qualified field name
     * @return the field's value
     */
    public String getField(String name) {
        return fields.get(name);
    }

    /**
     * Gets the field value or <CODE>null</CODE> if the field does not exist or has no value defined.
     *
     * @param name the fully qualified field name
     * @return the field value or <CODE>null</CODE>
     */
    @Override
    public String getFieldValue(String name) {
        return fields.get(name);
    }

    /**
     * Gets the field values for a list or <CODE>null</CODE> if the field does not exist or has no value defined.
     *
     * @param name the fully qualified field name
     * @return the field values or <CODE>null</CODE>
     * @since 2.1.4
     */
    @Override
    public List<String> getListValues(String name) {
        return listFields.get(name);
    }

    /**
     * Gets the PDF file specification contained in the FDF.
     *
     * @return the PDF file specification contained in the FDF
     */
    public String getFileSpec() {
        return fileSpec;
    }

    /**
     * Called when a start tag is found.
     *
     * @param tag the tag name
     * @param h   the tag's attributes
     */
    @Deprecated
    @Override
    @SuppressWarnings("unchecked")
    public void startElement(String tag, HashMap h) {
        startElement(tag, (Map<String, String>) h);
    }

    /**
     * Called when a start tag is found.
     *
     * @param tag the tag name
     * @param h   the tag's attributes
     */
    @Override
    public void startElement(String tag, Map<String, String> h) {
        if (!foundRoot) {
            if (!tag.equals("xfdf")) {
                throw new RuntimeException(MessageLocalization.getComposedMessage("root.element.is.not.xfdf.1", tag));
            } else {
                foundRoot = true;
            }
        }

        switch (tag) {
            case "xfdf":
                // intentionally left blank
                break;
            case "f":
                fileSpec = h.get("href");
                break;
            case "fields":
                fields = new HashMap<>();    // init it!

                listFields = new HashMap<>();
                break;
            case "field":
                String fName = h.get("name");
                fieldNames.push(fName);
                break;
            case "value":
                fieldValues.push("");
                break;
        }
    }

    /**
     * Called when an end tag is found.
     *
     * @param tag the tag name
     */
    @Override
    public void endElement(String tag) {
        if (tag.equals("value")) {
            StringBuilder fName = new StringBuilder();
            for (int k = 0; k < fieldNames.size(); ++k) {
                fName.append(".").append(fieldNames.elementAt(k));
            }
            if (fName.toString().startsWith(".")) {
                fName = new StringBuilder(fName.substring(1));
            }
            String fVal = fieldValues.pop();
            String old = fields.put(fName.toString(), fVal);
            if (old != null) {
                List<String> l = listFields.get(fName.toString());
                if (l == null) {
                    l = new ArrayList<>();
                    l.add(old);
                }
                l.add(fVal);
                listFields.put(fName.toString(), l);
            }
        } else if (tag.equals("field")) {
            if (!fieldNames.isEmpty()) {
                fieldNames.pop();
            }
        }
    }

    /**
     * Called when the document starts to be parsed.
     */
    public void startDocument() {
        fileSpec = "";
    }

    /**
     * Called after the document is parsed.
     */
    public void endDocument() {

    }

    /**
     * Called when a text element is found.
     *
     * @param str the text element, probably a fragment.
     */
    public void text(String str) {
        if (fieldNames.isEmpty() || fieldValues.isEmpty()) {
            return;
        }

        String val = fieldValues.pop();
        val += str;
        fieldValues.push(val);
    }
}
