/*
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
 * http://www.lowagie.com/iText/
 */
package com.lowagie.text.pdf;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import com.lowagie.text.error_messages.MessageLocalization;

import com.lowagie.text.xml.simpleparser.IanaEncodings;
import com.lowagie.text.xml.simpleparser.SimpleXMLDocHandler;
import com.lowagie.text.xml.simpleparser.SimpleXMLParser;

/**
 *
 * @author Paulo Soares (psoares@consiste.pt)
 */
public final class SimpleNamedDestination implements SimpleXMLDocHandler {
    
    private HashMap xmlNames;
    private HashMap xmlLast;

    private SimpleNamedDestination() {
    }
    
    public static HashMap getNamedDestination(PdfReader reader, boolean fromNames) {
        IntHashtable pages = new IntHashtable();
        int numPages = reader.getNumberOfPages();
        for (int k = 1; k <= numPages; ++k)
            pages.put(reader.getPageOrigRef(k).getNumber(), k);
        HashMap names = fromNames ? reader.getNamedDestinationFromNames() : reader.getNamedDestinationFromStrings();
        for (Iterator it = names.entrySet().iterator(); it.hasNext();) {
            Map.Entry entry = (Map.Entry)it.next();
            PdfArray arr = (PdfArray)entry.getValue();
            StringBuffer s = new StringBuffer();
            try {
                s.append(pages.get(arr.getAsIndirectObject(0).getNumber()));
                s.append(' ').append(arr.getPdfObject(1).toString().substring(1));
                for (int k = 2; k < arr.size(); ++k)
                    s.append(' ').append(arr.getPdfObject(k).toString());
                entry.setValue(s.toString());
            }
            catch (Exception e) {
                it.remove();
            }
        }
        return names;
    }
    
    /**
     * Exports the destinations to XML. The DTD for this XML is:
     * <p>
     * <pre>
     * &lt;?xml version='1.0' encoding='UTF-8'?&gt;
     * &lt;!ELEMENT Name (#PCDATA)&gt;
     * &lt;!ATTLIST Name
     *    Page CDATA #IMPLIED
     * &gt;
     * &lt;!ELEMENT Destination (Name)*&gt;
     * </pre>
     * @param names the names
     * @param out the export destination. The stream is not closed
     * @param encoding the encoding according to IANA conventions
     * @param onlyASCII codes above 127 will always be escaped with &amp;#nn; if <CODE>true</CODE>,
     * whatever the encoding
     * @throws IOException on error
     */
    public static void exportToXML(HashMap names, OutputStream out, String encoding, boolean onlyASCII) throws IOException {
        String jenc = IanaEncodings.getJavaEncoding(encoding);
        Writer wrt = new BufferedWriter(new OutputStreamWriter(out, jenc));
        exportToXML(names, wrt, encoding, onlyASCII);
    }
    
    /**
     * Exports the destinations to XML.
     * @param names the names
     * @param wrt the export destination. The writer is not closed
     * @param encoding the encoding according to IANA conventions
     * @param onlyASCII codes above 127 will always be escaped with &amp;#nn; if <CODE>true</CODE>,
     * whatever the encoding
     * @throws IOException on error
     */
    public static void exportToXML(HashMap names, Writer wrt, String encoding, boolean onlyASCII) throws IOException {
        wrt.write("<?xml version=\"1.0\" encoding=\"");
        wrt.write(SimpleXMLParser.escapeXML(encoding, onlyASCII));
        wrt.write("\"?>\n<Destination>\n");
        for (Iterator it = names.entrySet().iterator(); it.hasNext();) {
            Map.Entry entry = (Map.Entry)it.next();
            String key = (String)entry.getKey();
            String value = (String)entry.getValue();
            wrt.write("  <Name Page=\"");
            wrt.write(SimpleXMLParser.escapeXML(value, onlyASCII));
            wrt.write("\">");
            wrt.write(SimpleXMLParser.escapeXML(escapeBinaryString(key), onlyASCII));
            wrt.write("</Name>\n");
        }
        wrt.write("</Destination>\n");
        wrt.flush();
    }
    
    /**
     * Import the names from XML.
     * @param in the XML source. The stream is not closed
     * @throws IOException on error
     * @return the names
     */
    public static HashMap importFromXML(InputStream in) throws IOException {
        SimpleNamedDestination names = new SimpleNamedDestination();
        SimpleXMLParser.parse(names, in);
        return names.xmlNames;
    }
    
    /**
     * Import the names from XML.
     * @param in the XML source. The reader is not closed
     * @throws IOException on error
     * @return the names
     */
    public static HashMap importFromXML(Reader in) throws IOException {
        SimpleNamedDestination names = new SimpleNamedDestination();
        SimpleXMLParser.parse(names, in);
        return names.xmlNames;
    }

    static PdfArray createDestinationArray(String value, PdfWriter writer) {
        PdfArray ar = new PdfArray();
        StringTokenizer tk = new StringTokenizer(value);
        int n = Integer.parseInt(tk.nextToken());
        ar.add(writer.getPageReference(n));
        if (!tk.hasMoreTokens()) {
            ar.add(PdfName.XYZ);
            ar.add(new float[]{0, 10000, 0});
        }
        else {
            String fn = tk.nextToken();
            if (fn.startsWith("/"))
                fn = fn.substring(1);
            ar.add(new PdfName(fn));
            for (int k = 0; k < 4 && tk.hasMoreTokens(); ++k) {
                fn = tk.nextToken();
                if (fn.equals("null"))
                    ar.add(PdfNull.PDFNULL);
                else
                    ar.add(new PdfNumber(fn));
            }
        }
        return ar;
    }
    
    public static PdfDictionary outputNamedDestinationAsNames(HashMap names, PdfWriter writer) {
        PdfDictionary dic = new PdfDictionary();
        for (Iterator it = names.entrySet().iterator(); it.hasNext();) {
            Map.Entry entry = (Map.Entry)it.next();
            try {
                String key = (String)entry.getKey();
                String value = (String)entry.getValue();
                PdfArray ar = createDestinationArray(value, writer);
                PdfName kn = new PdfName(key);
                dic.put(kn, ar);
            }
            catch (Exception e) {
                // empty on purpose
            }            
        }
        return dic;
    }
    
    public static PdfDictionary outputNamedDestinationAsStrings(HashMap names, PdfWriter writer) throws IOException {
        HashMap n2 = new HashMap(names);
        for (Iterator it = n2.entrySet().iterator(); it.hasNext();) {
            Map.Entry entry = (Map.Entry)it.next();
            try {
                String value = (String)entry.getValue();
                PdfArray ar = createDestinationArray(value, writer);
                entry.setValue(writer.addToBody(ar).getIndirectReference());
            }
            catch (Exception e) {
                it.remove();
            }
        }
        return PdfNameTree.writeTree(n2, writer);
    }
    
    public static String escapeBinaryString(String s) {
        StringBuffer buf = new StringBuffer();
        char cc[] = s.toCharArray();
        int len = cc.length;
        for (int k = 0; k < len; ++k) {
            char c = cc[k];
            if (c < ' ') {
                buf.append('\\');
                String octal = "00" + Integer.toOctalString(c);
                buf.append(octal.substring(octal.length() - 3));
            }
            else if (c == '\\')
                buf.append("\\\\");
            else
                buf.append(c);
        }
        return buf.toString();
    }
    
    public static String unEscapeBinaryString(String s) {
        StringBuffer buf = new StringBuffer();
        char cc[] = s.toCharArray();
        int len = cc.length;
        for (int k = 0; k < len; ++k) {
            char c = cc[k];
            if (c == '\\') {
                if (++k >= len) {
                    buf.append('\\');
                    break;
                }
                c = cc[k];
                if (c >= '0' && c <= '7') {
                    int n = c - '0';
                    ++k;
                    for (int j = 0; j < 2 && k < len; ++j) {
                        c = cc[k];
                        if (c >= '0' && c <= '7') {
                            ++k;
                            n = n * 8 + c - '0';
                        }
                        else {
                            break;
                        }
                    }
                    --k;
                    buf.append((char)n);
                }
                else
                    buf.append(c);
            }
            else
                buf.append(c);
        }
        return buf.toString();
    }
    
    public void endDocument() {
    }
    
    public void endElement(String tag) {
        if (tag.equals("Destination")) {
            if (xmlLast == null && xmlNames != null)
                return;
            else
                throw new RuntimeException(MessageLocalization.getComposedMessage("destination.end.tag.out.of.place"));
        }
        if (!tag.equals("Name"))
            throw new RuntimeException(MessageLocalization.getComposedMessage("invalid.end.tag.1", tag));
        if (xmlLast == null || xmlNames == null)
            throw new RuntimeException(MessageLocalization.getComposedMessage("name.end.tag.out.of.place"));
        if (!xmlLast.containsKey("Page"))
            throw new RuntimeException(MessageLocalization.getComposedMessage("page.attribute.missing"));
        xmlNames.put(unEscapeBinaryString((String)xmlLast.get("Name")), xmlLast.get("Page"));
        xmlLast = null;
    }
    
    public void startDocument() {
    }
    
    public void startElement(String tag, HashMap h) {
        if (xmlNames == null) {
            if (tag.equals("Destination")) {
                xmlNames = new HashMap();
                return;
            }
            else
                throw new RuntimeException(MessageLocalization.getComposedMessage("root.element.is.not.destination"));
        }
        if (!tag.equals("Name"))
            throw new RuntimeException(MessageLocalization.getComposedMessage("tag.1.not.allowed", tag));
        if (xmlLast != null)
            throw new RuntimeException(MessageLocalization.getComposedMessage("nested.tags.are.not.allowed"));
        xmlLast = new HashMap(h);
        xmlLast.put("Name", "");
    }
    
    public void text(String str) {
        if (xmlLast == null)
            return;
        String name = (String)xmlLast.get("Name");
        name += str;
        xmlLast.put("Name", name);
    }    
}
