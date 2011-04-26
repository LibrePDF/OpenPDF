/*
 * $Id: HtmlParser.java 3373 2008-05-12 16:21:24Z xlv $
 *
 * Copyright 2001, 2002 by Bruno Lowagie.
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

package com.lowagie.text.html;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.lowagie.text.DocListener;
import com.lowagie.text.ExceptionConverter;
import com.lowagie.text.xml.XmlParser;

/**
 * This class can be used to parse some HTML files.
 */

public class HtmlParser extends XmlParser {
    
/**
 * Constructs an HtmlParser.
 */
    
    public HtmlParser() {
        super();
    }
    
/**
 * Parses a given file.
 * @param document the document the parser will write to
 * @param is the InputSource with the content
 */
    
    public void go(DocListener document, InputSource is) {
        try {
            parser.parse(is, new SAXmyHtmlHandler(document));
        }
        catch(SAXException se) {
            throw new ExceptionConverter(se);
        }
        catch(IOException ioe) {
            throw new ExceptionConverter(ioe);
        }
    }
    
/**
 * Parses a given file that validates with the iText DTD and writes the content to a document.
 * @param document the document the parser will write to
 * @param is the InputSource with the content
 */
    
    public static void parse(DocListener document, InputSource is) {
        HtmlParser p = new HtmlParser();
        p.go(document, is);
    }
    
/**
 * Parses a given file.
 * @param document the document the parser will write to
 * @param file the file with the content
 */
    
    public void go(DocListener document, String file) {
        try {
            parser.parse(file, new SAXmyHtmlHandler(document));
        }
        catch(SAXException se) {
            throw new ExceptionConverter(se);
        }
        catch(IOException ioe) {
            throw new ExceptionConverter(ioe);
        }
    }
    
/**
 * Parses a given file that validates with the iText DTD and writes the content to a document.
 * @param document the document the parser will write to
 * @param file the file with the content
 */
    
    public static void parse(DocListener document, String file) {
        HtmlParser p = new HtmlParser();
        p.go(document, file);
    }
    
/**
 * Parses a given file.
 * @param document the document the parser will write to
 * @param is the InputStream with the content
 */
    
    public void go(DocListener document, InputStream is) {
        try {
            parser.parse(new InputSource(is), new SAXmyHtmlHandler(document));
        }
        catch(SAXException se) {
            throw new ExceptionConverter(se);
        }
        catch(IOException ioe) {
            throw new ExceptionConverter(ioe);
        }
    }
    
/**
 * Parses a given file that validates with the iText DTD and writes the content to a document.
 * @param document the document the parser will write to
 * @param is the InputStream with the content
 */
    
    public static void parse(DocListener document, InputStream is) {
        HtmlParser p = new HtmlParser();
        p.go(document, new InputSource(is));
    }
    
/**
 * Parses a given file.
 * @param document the document the parser will write to
 * @param is the Reader with the content
 */
    
    public void go(DocListener document, Reader is) {
        try {
            parser.parse(new InputSource(is), new SAXmyHtmlHandler(document));
        }
        catch(SAXException se) {
            throw new ExceptionConverter(se);
        }
        catch(IOException ioe) {
            throw new ExceptionConverter(ioe);
        }
    }
    
/**
 * Parses a given file that validates with the iText DTD and writes the content to a document.
 * @param document the document the parser will write to
 * @param is the Reader with the content
 */
    
    public static void parse(DocListener document, Reader is) {
        HtmlParser p = new HtmlParser();
        p.go(document, new InputSource(is));
    }
}