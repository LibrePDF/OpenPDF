/*
 * $Id: PdfLister.java 3735 2009-02-26 01:44:03Z xlv $
 *
 * Copyright 2002 Mark Thompson
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
 * This class by Mark Thompson. Copyright (C) 2002 Mark Thompson
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
import java.io.PrintStream;
import java.util.Iterator;
/**
 * List a PDF file in human-readable form (for debugging reasons mostly)
 * @author Mark Thompson
 */

public class PdfLister {

	/** the printStream you want to write the output to. */
    PrintStream out;

    /**
     * Create a new lister object.
     * @param out
     */
    public PdfLister(PrintStream out) {
        this.out = out;
    }

    /**
     * Visualizes a PDF object.
     * @param object	a com.lowagie.text.pdf object
     */
    public void listAnyObject(PdfObject object)
    {
        switch (object.type()) {
        case PdfObject.ARRAY:
            listArray((PdfArray)object);
            break;
        case PdfObject.DICTIONARY:
            listDict((PdfDictionary) object);
            break;
        case PdfObject.STRING:
            out.println("(" + object.toString() + ")");
            break;
        default:
            out.println(object.toString());
            break;
        }
    }
    /**
     * Visualizes a PdfDictionary object.
     * @param dictionary	a com.lowagie.text.pdf.PdfDictionary object
     */
    public void listDict(PdfDictionary dictionary)
    {
        out.println("<<");
        PdfName key;
        PdfObject value;
        for (Iterator i = dictionary.getKeys().iterator(); i.hasNext(); ) {
            key = (PdfName) i.next();
            value = dictionary.get(key);
            out.print(key.toString());
            out.print(' ');
            listAnyObject(value);
        }
        out.println(">>");
    }

    /**
     * Visualizes a PdfArray object.
     * @param array	a com.lowagie.text.pdf.PdfArray object
     */
    public void listArray(PdfArray array)
    {
        out.println('[');
        for (Iterator i = array.listIterator(); i.hasNext(); ) {
            PdfObject item = (PdfObject)i.next();
            listAnyObject(item);
        }
        out.println(']');
    }
    /**
     * Visualizes a Stream.
     * @param stream
     * @param reader
     */
    public void listStream(PRStream stream, PdfReaderInstance reader)
    {
        try {
            listDict(stream);
            out.println("startstream");
            byte[] b = PdfReader.getStreamBytes(stream);
//                  byte buf[] = new byte[Math.min(stream.getLength(), 4096)];
//                  int r = 0;
//                  stream.openStream(reader);
//                  for (;;) {
//                      r = stream.readStream(buf, 0, buf.length);
//                      if (r == 0) break;
//                      out.write(buf, 0, r);
//                  }
//                  stream.closeStream();
            int len = b.length - 1;
            for (int k = 0; k < len; ++k) {
                if (b[k] == '\r' && b[k + 1] != '\n')
                    b[k] = (byte)'\n';
            }
            out.println(new String(b));
            out.println("endstream");
        } catch (IOException e) {
            System.err.println("I/O exception: " + e);
//          } catch (java.util.zip.DataFormatException e) {
//              System.err.println("Data Format Exception: " + e);
        }
    }
    /**
     * Visualizes an imported page
     * @param iPage
     */
    public void listPage(PdfImportedPage iPage)
    {
        int pageNum = iPage.getPageNumber();
        PdfReaderInstance readerInst = iPage.getPdfReaderInstance();
        PdfReader reader = readerInst.getReader();

        PdfDictionary page = reader.getPageN(pageNum);
        listDict(page);
        PdfObject obj = PdfReader.getPdfObject(page.get(PdfName.CONTENTS));
        if (obj == null)
            return;
        switch (obj.type) {
        case PdfObject.STREAM:
            listStream((PRStream)obj, readerInst);
            break;
        case PdfObject.ARRAY:
            for (Iterator i = ((PdfArray)obj).listIterator(); i.hasNext();) {
                PdfObject o = PdfReader.getPdfObject((PdfObject)i.next());
                listStream((PRStream)o, readerInst);
                out.println("-----------");
            }
            break;
        }
    }
}
