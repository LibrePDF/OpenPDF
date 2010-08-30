/*
 * $Id: TagMap.java 3373 2008-05-12 16:21:24Z xlv $
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

package com.lowagie.text.xml;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;

import com.lowagie.text.ExceptionConverter;

/**
 * The <CODE>Tags</CODE>-class maps several XHTML-tags to iText-objects.
 */

public class TagMap extends HashMap {

    private static final long serialVersionUID = -6809383366554350820L;

	class AttributeHandler extends DefaultHandler {
        
/** This is a tag */
        public static final String TAG = "tag";
        
/** This is a tag */
        public static final String ATTRIBUTE = "attribute";
        
/** This is an attribute */
        public static final String NAME = "name";
        
/** This is an attribute */
        public static final String ALIAS = "alias";
        
/** This is an attribute */
        public static final String VALUE = "value";
        
/** This is an attribute */
        public static final String CONTENT = "content";
        
/** This is the tagmap using the AttributeHandler */
        private HashMap tagMap;
        
/** This is the current peer. */
        private XmlPeer currentPeer;
        
/**
 * Constructs a new SAXiTextHandler that will translate all the events
 * triggered by the parser to actions on the <CODE>Document</CODE>-object.
 *
 * @param	tagMap  A Hashmap containing XmlPeer-objects
 */
        
        public AttributeHandler(HashMap tagMap) {
            super();
            this.tagMap = tagMap;
        }
        
/**
 * This method gets called when a start tag is encountered.
 *
 * @param   uri 		the Uniform Resource Identifier
 * @param   lname 		the local name (without prefix), or the empty string if Namespace processing is not being performed.
 * @param	tag 		the name of the tag that is encountered
 * @param	attrs		the list of attributes
 */
        
        public void startElement(String uri, String lname, String tag, Attributes attrs) {
            String name = attrs.getValue(NAME);
            String alias = attrs.getValue(ALIAS);
            String value = attrs.getValue(VALUE);
            if (name != null) {
                if(TAG.equals(tag)) {
                    currentPeer = new XmlPeer(name, alias);
                }
                else if (ATTRIBUTE.equals(tag)) {
                    if (alias != null) {
                        currentPeer.addAlias(name, alias);
                    }
                    if (value != null) {
                        currentPeer.addValue(name, value);
                    }
                }
            }
            value = attrs.getValue(CONTENT);
            if (value != null) {
                currentPeer.setContent(value);
            }
        }
        
/**
 * This method gets called when ignorable white space encountered.
 *
 * @param	ch		an array of characters
 * @param	start	the start position in the array
 * @param	length	the number of characters to read from the array
 */
        
        public void ignorableWhitespace(char[] ch, int start, int length) {
            // do nothing
        }
        
/**
 * This method gets called when characters are encountered.
 *
 * @param	ch		an array of characters
 * @param	start	the start position in the array
 * @param	length	the number of characters to read from the array
 */
        
        public void characters(char[] ch, int start, int length) {
            // do nothing
        }
        
/**
 * This method gets called when an end tag is encountered.
 *
 * @param   uri 		the Uniform Resource Identifier
 * @param   lname 		the local name (without prefix), or the empty string if Namespace processing is not being performed.
 * @param	tag		the name of the tag that ends
 */
        
        public void endElement(String uri, String lname, String tag) {
            if (TAG.equals(tag))
                tagMap.put(currentPeer.getAlias(), currentPeer);
        }
    }
    
    /**
     * Constructs a TagMap
     * @param tagfile the path to an XML file with the tagmap
     */
    public TagMap(String tagfile) {
        super();
        try {
            init(TagMap.class.getClassLoader().getResourceAsStream(tagfile));
        }catch(Exception e) {
        	try {
				init(new FileInputStream(tagfile));
			} catch (FileNotFoundException fnfe) {
				throw new ExceptionConverter(fnfe);
			}
        }
    }

    /**
     * Constructs a TagMap.
     * @param in	An InputStream with the tagmap xml
     */
    public TagMap(InputStream in) {
        super();
        init(in);
    }

    protected void init(InputStream in) {
        try {
            SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
            parser.parse(new InputSource(in), new AttributeHandler(this));
        }
        catch(Exception e) {
            throw new ExceptionConverter(e);
        }
    }


}
