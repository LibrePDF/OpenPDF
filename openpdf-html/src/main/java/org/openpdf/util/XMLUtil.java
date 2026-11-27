/*
 * {{{ header & license
 * XMLUtil.java
 * Copyright (c) 2004, 2005 Patrick Wright
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * }}}
 */
package org.openpdf.util;

import org.openpdf.resource.FSEntityResolver;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.logging.Level;

/**
 * Utility class for XML processing using DOM
 */
public class XMLUtil {
    private static final String EXTERNAL_GENERAL_ENTITIES = "http://xml.org/sax/features/external-general-entities";

    public static Document documentFromString(final String documentContents)
        throws Exception {

        return createDocumentBuilder().parse(new InputSource(new StringReader(documentContents)));
    }

    public static Document documentFromFile(final String filename)
        throws Exception {

        return createDocumentBuilder().parse(new File(filename).toURI().toURL().openStream());
    }

    /**
     * Parses an XML document without fetching anything the document refers to. A document type declaration is
     * allowed, because XHTML documents usually have one, but external general entities are not read, and DTDs and
     * entity sets are only read from the copies bundled with this library (see {@link FSEntityResolver}).
     * Every other external DTD or parameter entity is replaced with empty content.
     *
     * @param source the document to parse
     * @return the parsed document
     */
    public static Document documentFromStream(final InputStream source)
        throws SAXException, IOException, ParserConfigurationException {

        return createDocumentBuilder().parse(source);
    }

    private static DocumentBuilder createDocumentBuilder()
        throws ParserConfigurationException {

        DocumentBuilderFactory fact = DocumentBuilderFactory.newInstance();
        fact.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        fact.setXIncludeAware(false);
        setFeature(fact, EXTERNAL_GENERAL_ENTITIES, false);
        setAttribute(fact, XMLConstants.ACCESS_EXTERNAL_DTD, "");
        setAttribute(fact, XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        DocumentBuilder builder = fact.newDocumentBuilder();

        // Answers every external DTD and parameter entity itself, so the parser never opens a URL from the document.
        FSEntityResolver resolver = FSEntityResolver.instance();
        builder.setEntityResolver((publicId, systemId) -> resolver.resolveEntity(publicId, systemId));
        builder.setErrorHandler(null);

        return builder;
    }

    private static void setFeature(DocumentBuilderFactory fact, String feature, boolean value) {
        try {
            fact.setFeature(feature, value);
        } catch (ParserConfigurationException e) {
            XRLog.load(Level.WARNING, "XML parser does not support the feature " + feature, e);
        }
    }

    private static void setAttribute(DocumentBuilderFactory fact, String attribute, String value) {
        try {
            fact.setAttribute(attribute, value);
        } catch (IllegalArgumentException e) {
            XRLog.load(Level.WARNING, "XML parser does not support the attribute " + attribute, e);
        }
    }
}
