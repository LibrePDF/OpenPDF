/*
 * $Id: XmlParser.java 3373 2008-05-12 16:21:24Z xlv $
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
 * https://github.com/LibrePDF/OpenPDF
 */

package com.lowagie.text.xml;

import com.lowagie.text.DocListener;
import com.lowagie.text.ExceptionConverter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Map;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * This class can be used to parse an XML file.
 */

public class XmlParser {

    /**
     * This is the instance of the parser.
     */
    protected SAXParser parser;

    /**
     * Constructs an XmlParser.
     */

    public XmlParser() {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            parser = factory.newSAXParser();
        } catch (ParserConfigurationException | SAXException pce) {
            throw new ExceptionConverter(pce);
        }
    }

    /**
     * Parses a given file that validates with the iText DTD and writes the content to a document.
     *
     * @param document The document that will listen to the parser
     * @param is       the input source with the content
     */

    public static void parse(DocListener document, InputSource is) {
        XmlParser xmlParser = new XmlParser();
        xmlParser.go(document, is);
    }

    /**
     * Parses a given file that validates with the iText DTD and writes the content to a document.
     *
     * @param document The document that will listen to the parser
     * @param is       The input source with the content
     * @param tagmap   a user defined tagmap
     */

    public static void parse(DocListener document, InputSource is, String tagmap) {
        XmlParser xmlParser = new XmlParser();
        xmlParser.go(document, is, tagmap);
    }

    /**
     * Parses a given file and writes the content to a document, using a certain tagmap.
     *
     * @param document The document that will listen to the parser
     * @param is       The input source with the content
     * @param tagmap   a user defined tagmap
     */

    public static void parse(DocListener document, InputSource is, Map<String, XmlPeer> tagmap) {
        XmlParser xmlParser = new XmlParser();
        xmlParser.go(document, is, tagmap);
    }

    /**
     * Parses a given file that validates with the iText DTD and writes the content to a document.
     *
     * @param document The document that will listen to the parser
     * @param file     The path to a file with the content
     */

    public static void parse(DocListener document, String file) {
        XmlParser xmlParser = new XmlParser();
        xmlParser.go(document, file);
    }

    /**
     * Parses a given file that validates with the iText DTD and writes the content to a document.
     *
     * @param document The document that will listen to the parser
     * @param file     The path to a file with the content
     * @param tagmap   A user defined tagmap
     */

    public static void parse(DocListener document, String file, String tagmap) {
        XmlParser xmlParser = new XmlParser();
        xmlParser.go(document, file, tagmap);
    }

    /**
     * Parses a given file and writes the content to a document, using a certain tagmap.
     *
     * @param document The document that will listen to the parser
     * @param file     The path to a file with the content
     * @param tagmap   A user defined tagmap
     */

    public static void parse(DocListener document, String file, Map<String, XmlPeer> tagmap) {
        XmlParser xmlParser = new XmlParser();
        xmlParser.go(document, file, tagmap);
    }

    /**
     * Parses a given file that validates with the iText DTD and writes the content to a document.
     *
     * @param document The document that will listen to the parser
     * @param is       The input source with the content
     */

    public static void parse(DocListener document, InputStream is) {
        XmlParser xmlParser = new XmlParser();
        xmlParser.go(document, new InputSource(is));
    }

    /**
     * Parses a given file that validates with the iText DTD and writes the content to a document.
     *
     * @param document The document that will listen to the parser
     * @param is       The input stream with the content
     * @param tagmap   A user defined tagmap
     */

    public static void parse(DocListener document, InputStream is, String tagmap) {
        XmlParser xmlParser = new XmlParser();
        xmlParser.go(document, new InputSource(is), tagmap);
    }

    /**
     * Parses a given file and writes the content to a document, using a certain tagmap.
     *
     * @param document The document that will listen to the parser
     * @param is       The InputStream with the content
     * @param tagmap   A user defined tagmap
     */

    public static void parse(DocListener document, InputStream is, Map<String, XmlPeer> tagmap) {
        XmlParser xmlParser = new XmlParser();
        xmlParser.go(document, new InputSource(is), tagmap);
    }

    /**
     * Parses a given file that validates with the iText DTD and writes the content to a document.
     *
     * @param document The document that will listen to the parser
     * @param is       The reader that reads the content
     */

    public static void parse(DocListener document, Reader is) {
        XmlParser xmlParser = new XmlParser();
        xmlParser.go(document, new InputSource(is));
    }

    /**
     * Parses a given file that validates with the iText DTD and writes the content to a document.
     *
     * @param document The document that will listen to the parser
     * @param is       The reader that reads the content
     * @param tagmap   A user defined tagmap
     */

    public static void parse(DocListener document, Reader is, String tagmap) {
        XmlParser xmlParser = new XmlParser();
        xmlParser.go(document, new InputSource(is), tagmap);
    }

    /**
     * Parses a given file and writes the content to a document, using a certain tagmap.
     *
     * @param document The document that will listen to the parser
     * @param is       The reader that reads the content
     * @param tagmap   A user defined tagmap
     */

    public static void parse(DocListener document, Reader is, Map<String, XmlPeer> tagmap) {
        XmlParser xmlParser = new XmlParser();
        xmlParser.go(document, new InputSource(is), tagmap);
    }

    /**
     * Parses a given file.
     *
     * @param document The document that will listen to the parser
     * @param is       The InputStream with the contents
     */

    public void go(DocListener document, InputSource is) {
        try {
            parser.parse(is, new SAXiTextHandler<>(document));
        } catch (SAXException | IOException se) {
            throw new ExceptionConverter(se);
        }
    }

    /**
     * Parses a given file.
     *
     * @param document The document that will listen to the parser
     * @param is       The input source with the content
     * @param tagmap   A user defined tagmap
     */

    public void go(DocListener document, InputSource is, String tagmap) {
        try {
            parser.parse(is, new SAXmyHandler(document, new TagMap(tagmap)));
        } catch (SAXException | IOException se) {
            throw new ExceptionConverter(se);
        }
    }

    /**
     * Parses a given file.
     *
     * @param document The document that will listen to the parser
     * @param is       the input source with the content
     * @param tagmap   an inputstream to a user defined tagmap
     */

    public void go(DocListener document, InputSource is, InputStream tagmap) {
        try {
            parser.parse(is, new SAXmyHandler(document, new TagMap(tagmap)));
        } catch (SAXException | IOException se) {
            throw new ExceptionConverter(se);
        }
    }

    /**
     * Parses a given file.
     *
     * @param document The document that will listen to the parser
     * @param is       the input source with the content
     * @param tagmap   a user defined tagmap
     */

    public void go(DocListener document, InputSource is, Map<String, XmlPeer> tagmap) {
        try {
            parser.parse(is, new SAXmyHandler(document, tagmap));
        } catch (SAXException | IOException se) {
            throw new ExceptionConverter(se);
        }
    }

    /**
     * Parses a given file.
     *
     * @param document The document that will listen to the parser
     * @param file     The path to a file with the content
     */

    public void go(DocListener document, String file) {
        try {
            parser.parse(file, new SAXiTextHandler<>(document));
        } catch (SAXException | IOException se) {
            throw new ExceptionConverter(se);
        }
    }

    /**
     * Parses a given file.
     *
     * @param document the document that will listen to the parser
     * @param file     the path to a file with the content
     * @param tagmap   a user defined tagmap
     */

    public void go(DocListener document, String file, String tagmap) {
        try {
            parser.parse(file, new SAXmyHandler(document, new TagMap(tagmap)));
        } catch (SAXException | IOException se) {
            throw new ExceptionConverter(se);
        }
    }

    /**
     * Parses a given file.
     *
     * @param document The document that will listen to the parser
     * @param file     the path to a file with the content
     * @param tagmap   a user defined tagmap
     */

    public void go(DocListener document, String file, Map<String, XmlPeer> tagmap) {
        try {
            parser.parse(file, new SAXmyHandler(document, tagmap));
        } catch (SAXException | IOException se) {
            throw new ExceptionConverter(se);
        }
    }
}
