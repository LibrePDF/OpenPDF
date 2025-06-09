/*
 * {{{ header & license
 * FSCatalog.java
 * Copyright (c) 2004, 2005 Patrick Wright
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.	See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * }}}
 */
package org.openpdf.resource;

import org.openpdf.util.XRLog;
import org.openpdf.util.XRRuntimeException;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import static java.util.Objects.requireNonNull;

/**
 * <p>FSCatalog loads an XML catalog file to read mappings of public IDs for
 * XML schemas/dtds, to resolve those mappings to a local store for the schemas.
 * The catalog file allows one to have a single mapping of schema IDs to local
 * files, and is useful when there are many schemas, or when schemas are broken
 * into many smaller files. Currently, FSCatalog only supports the very simple
 * mapping of public id to local URI using the public element in the catalog XML.
 * <p>
 * <p>FSCatalog is not an EntityResolver; it only parses a catalog file. See
 * {@link FSEntityResolver} for entity resolution.
 * <p>
 * <p>To use, instantiate the class, and call {@link #parseCatalog(InputSource)}
 * to retrieve a {@link java.util.Map} keyed by public ids. The class uses
 * an XMLReader instance retrieved via {@link XMLResource#newXMLReader()}, so
 * XMLReader configuration (and specification) follows that of the standard XML
 * parsing in Flying Saucer.
 * <p>
 * <p>This class is not safe for multithreaded access.
 *
 * @author Patrick Wright
 */
public class FSCatalog {
    /**
     * Default constructor
     */
    public FSCatalog() {
    }

    /**
     * Parses an XML catalog file and returns a Map of public ids to local URIs read
     * from the catalog. Only the catalog public elements are parsed.
     *
     * @param catalogURI A String URI to a catalog XML file on the classpath.
     */
    public Map<String, String> parseCatalog(String catalogURI) {
        try {
            URL url = requireNonNull(FSCatalog.class.getClassLoader().getResource(catalogURI),
                    () -> "Catalog not found in classpath: " + catalogURI);
            return parseCatalog(url);
        } catch (IOException ex) {
            XRLog.xmlEntities(Level.WARNING, "Could not open XML catalog from URI '" + catalogURI + "'", ex);
            throw new IllegalStateException("Cannot find " + catalogURI + " in classpath", ex);
        }
    }

    private Map<String, String> parseCatalog(URL url) throws IOException {
        try (InputStream s = new BufferedInputStream(url.openStream())) {
            return parseCatalog(new InputSource(s));
        }
    }

    /**
     * Parses an XML catalog file and returns a Map of public ids to local URIs read
     * from the catalog. Only the catalog public elements are parsed.
     *
     * @param inputSource A SAX InputSource to a catalog XML file on the classpath.
     */
    public Map<String, String> parseCatalog(InputSource inputSource) {
        XMLReader xmlReader = XMLResource.newXMLReader();

        CatalogContentHandler ch = new CatalogContentHandler();
        addHandlers(xmlReader, ch);
        setFeature(xmlReader);

        try {
            xmlReader.parse(inputSource);
        } catch (Exception ex) {
            throw new RuntimeException("Failed on configuring SAX to DOM transformer.", ex);
        }

        return ch.getEntityMap();
    }

    /**
     * Adds the default EntityResolved and ErrorHandler for the SAX parser.
     */
    private void addHandlers(XMLReader xmlReader, ContentHandler ch) {
        try {
            // add our own entity resolver
            xmlReader.setContentHandler(ch);
            xmlReader.setErrorHandler(new ErrorHandler() {
                @Override
                public void error(SAXParseException ex) {
                    if (XRLog.isLoggingEnabled()) {
                        XRLog.xmlEntities(Level.WARNING, ex.getMessage());
                    }
                }

                @Override
                public void fatalError(SAXParseException ex) {
                    if (XRLog.isLoggingEnabled()) {
                        XRLog.xmlEntities(Level.WARNING, ex.getMessage());
                    }
                }

                @Override
                public void warning(SAXParseException ex) {
                    if (XRLog.isLoggingEnabled()) {
                        XRLog.xmlEntities(Level.WARNING, ex.getMessage());
                    }
                }
            });
        } catch (Exception ex) {
            throw new XRRuntimeException("Failed on configuring SAX parser/XMLReader.", ex);
        }
    }

    /**
     * A SAX ContentHandler that reads an XML catalog file and builds a Map of
     * public IDs to local URIs. Currently only handles the <public> element and attributes.
     * To use, just call XMLReader.setContentHandler() with an instance of the class,
     * parse, then call getEntityMap().
     */
    private static class CatalogContentHandler extends DefaultHandler {
        private final Map<String, String> entityMap = new HashMap<>();

        /**
         * Returns a Map of public Ids to local URIs
         */
        private Map<String, String> getEntityMap() {
            return entityMap;
        }

        /**
         * Receive notification of the beginning of an element; here used to pick up the mappings
         * for public IDs to local URIs in the catalog.
         */
        @Override
        public void startElement(String namespaceURI, String localName, String qName, Attributes attributes) {
            if (localName.equalsIgnoreCase("public") ||
                    (localName.isEmpty() && qName.equalsIgnoreCase("public"))) {
                entityMap.put(attributes.getValue("publicId"), attributes.getValue("uri"));
            }
        }
    }

    /**
     * Attempts to set requested feature on the parser; logs exception if not supported
     * or not recognized.
     */
    private void setFeature(XMLReader xmlReader) {
        try {
            xmlReader.setFeature("http://xml.org/sax/features/validation", false);

            XRLog.xmlEntities(Level.FINE, "SAX Parser feature: " +
                    "http://xml.org/sax/features/validation".substring("http://xml.org/sax/features/validation".lastIndexOf('/')) +
                    " set to " +
                    xmlReader.getFeature("http://xml.org/sax/features/validation"));
        } catch (SAXNotSupportedException ex) {
            XRLog.xmlEntities(Level.WARNING,
                    "SAX feature not supported on this XMLReader: " + "http://xml.org/sax/features/validation");
        } catch (SAXNotRecognizedException ex) {
            XRLog.xmlEntities(Level.WARNING,
                    "SAX feature not recognized on this XMLReader: " +
                            "http://xml.org/sax/features/validation" + ". Feature may be properly named, but not recognized by this parser.");
        }
    }
}

