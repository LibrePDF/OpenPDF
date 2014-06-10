/*
 * Copyright 2003 Paulo Soares
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
 *
 * The code to recognize the encoding in this class and in the convenience class IanaEncodings was taken from Apache Xerces published under the following license:
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * Part of this code is based on the Quick-and-Dirty XML parser by Steven Brandt.
 * The code for the Quick-and-Dirty parser was published in JavaWorld (java tip 128).
 * Steven Brandt and JavaWorld gave permission to use the code for free.
 * (Bruno Lowagie and Paulo Soares chose to use it under the MPL/LGPL in
 * conformance with the rest of the code).
 * The original code can be found on this url: <A HREF="http://www.javaworld.com/javatips/jw-javatip128_p.html">http://www.javaworld.com/javatips/jw-javatip128_p.html</A>.
 * It was substantially refactored by Bruno Lowagie.
 * 
 * The method 'private static String getEncodingName(byte[] b4)' was found
 * in org.apache.xerces.impl.XMLEntityManager, originaly published by the
 * Apache Software Foundation under the Apache Software License; now being
 * used in iText under the MPL.
 */
package com.lowagie.text.xml.simpleparser;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Stack;
import com.lowagie.text.error_messages.MessageLocalization;

/**
 * A simple XML and HTML parser.  This parser is, like the SAX parser,
 * an event based parser, but with much less functionality.
 * <p>
 * The parser can:
 * <p>
 * <ul>
 * <li>It recognizes the encoding used
 * <li>It recognizes all the elements' start tags and end tags
 * <li>It lists attributes, where attribute values can be enclosed in single or double quotes
 * <li>It recognizes the <code>&lt;[CDATA[ ... ]]&gt;</code> construct
 * <li>It recognizes the standard entities: &amp;amp;, &amp;lt;, &amp;gt;, &amp;quot;, and &amp;apos;, as well as numeric entities
 * <li>It maps lines ending in <code>\r\n</code> and <code>\r</code> to <code>\n</code> on input, in accordance with the XML Specification, Section 2.11
 * </ul>
 * <p>
 */
public final class SimpleXMLParser {
    /** possible states */
	private final static int UNKNOWN = 0;
	private final static int TEXT = 1;
	private final static int TAG_ENCOUNTERED = 2;
	private final static int EXAMIN_TAG = 3;
	private final static int TAG_EXAMINED = 4;
	private final static int IN_CLOSETAG = 5;
	private final static int SINGLE_TAG = 6;
	private final static int CDATA = 7;
	private final static int COMMENT = 8;
	private final static int PI = 9;
	private final static int ENTITY = 10;
	private final static int QUOTE = 11;
	private final static int ATTRIBUTE_KEY = 12;
	private final static int ATTRIBUTE_EQUAL = 13;
	private final static int ATTRIBUTE_VALUE = 14;
    
	/** the state stack */
	Stack stack;
	/** The current character. */
	int character = 0;
	/** The previous character. */
	int previousCharacter = -1;
	/** the line we are currently reading */
	int lines = 1;
	/** the column where the current character occurs */
	int columns = 0;
	/** was the last character equivalent to a newline? */
	boolean eol = false;
	/**
	 * A boolean indicating if the next character should be taken into account
	 * if it's a space character. When nospace is false, the previous character
	 * wasn't whitespace.
	 * @since 2.1.5
	 */
	boolean nowhite = false;
	/** the current state */
	int state;
	/** Are we parsing HTML? */
	boolean html;
	/** current text (whatever is encountered between tags) */
	StringBuffer text = new StringBuffer();
	/** current entity (whatever is encountered between & and ;) */
	StringBuffer entity = new StringBuffer();
	/** current tagname */
	String tag = null;
	/** current attributes */
	HashMap attributes = null;
	/** The handler to which we are going to forward document content */
	SimpleXMLDocHandler doc;
	/** The handler to which we are going to forward comments. */
	SimpleXMLDocHandlerComment comment;
	/** Keeps track of the number of tags that are open. */
	int nested = 0;
	/** the quote character that was used to open the quote. */
	int quoteCharacter = '"';
	/** the attribute key. */
	String attributekey = null;
	/** the attribute value. */
	String attributevalue = null;
    
	/**
	 * Creates a Simple XML parser object.
	 * Call go(BufferedReader) immediately after creation.
	 */
    private SimpleXMLParser(SimpleXMLDocHandler doc, SimpleXMLDocHandlerComment comment, boolean html) {
    	this.doc = doc;
    	this.comment = comment;
    	this.html = html;
    	stack = new Stack();
    	state = html ? TEXT : UNKNOWN;
    }
    
    /**
     * Does the actual parsing. Perform this immediately
     * after creating the parser object.
     */
    private void go(Reader r) throws IOException {
        BufferedReader reader;
        if (r instanceof BufferedReader)
            reader = (BufferedReader)r;
        else
            reader = new BufferedReader(r);
        doc.startDocument();
        while(true) {
			// read a new character
			if (previousCharacter == -1) {
				character = reader.read();
			}
			// or re-examine the previous character
			else {
				character = previousCharacter;
				previousCharacter = -1;
			}
			
			// the end of the file was reached
			if (character == -1) {
				if (html) {
					if (html && state == TEXT)
						flush();
					doc.endDocument();
				} else {
					throwException(MessageLocalization.getComposedMessage("missing.end.tag"));
				}
				return;
			}
            
			// dealing with  \n and \r
			if (character == '\n' && eol) {
				eol = false;
				continue;
			} else if (eol) {
				eol = false;
			} else if (character == '\n') {
				lines++;
				columns = 0;
			} else if (character == '\r') {
				eol = true;
				character = '\n';
				lines++;
				columns = 0;
			} else {
				columns++;
			}
            
			switch(state) {
            // we are in an unknown state before there's actual content
			case UNKNOWN:
                if(character == '<') {
                    saveState(TEXT);
                    state = TAG_ENCOUNTERED;
                }
                break;
            // we can encounter any content
			case TEXT:
                if(character == '<') {
                    flush();
                    saveState(state);
                    state = TAG_ENCOUNTERED;
                } else if(character == '&') {
                    saveState(state);
                    entity.setLength(0);
                    state = ENTITY;
                    nowhite = true;
                } else if (Character.isWhitespace((char)character)) {
                	if (nowhite)
                		text.append((char)character);
                	nowhite = false;
                } else {
                    text.append((char)character);
                    nowhite = true;
                }
                break;
            // we have just seen a < and are wondering what we are looking at
            // <foo>, </foo>, <!-- ... --->, etc.
			case TAG_ENCOUNTERED:
                initTag();
                if(character == '/') {
                    state = IN_CLOSETAG;
                } else if (character == '?') {
                    restoreState();
                    state = PI;
                } else {
                    text.append((char)character);
                    state = EXAMIN_TAG;
                }
                break;
            // we are processing something like this <foo ... >.
            // It could still be a <!-- ... --> or something.
			case EXAMIN_TAG:
                if(character == '>') {
                    doTag();
                    processTag(true);
                    initTag();
                    state = restoreState();
                } else if(character == '/') {
                    state = SINGLE_TAG;
                } else if(character == '-' && text.toString().equals("!-")) {
                    flush();
                    state = COMMENT;
                } else if(character == '[' && text.toString().equals("![CDATA")) {
                    flush();
                    state = CDATA;
                } else if(character == 'E' && text.toString().equals("!DOCTYP")) {
                    flush();
                    state = PI;
                } else if(Character.isWhitespace((char)character)) {
                    doTag();
                    state = TAG_EXAMINED;
                } else {
                    text.append((char)character);
                }
                break;
            // we know the name of the tag now.
			case TAG_EXAMINED:
                if(character == '>') {
                    processTag(true);
                    initTag();
                    state = restoreState();
                } else if(character == '/') {
                    state = SINGLE_TAG;
                } else if(Character.isWhitespace((char)character)) {
                    // empty
                } else {
                    text.append((char)character);
                    state = ATTRIBUTE_KEY;
                }
                break;
                
                // we are processing a closing tag: e.g. </foo>
			case IN_CLOSETAG:
                if(character == '>') {
                    doTag();
                    processTag(false);
                    if(!html && nested==0) return;
                    state = restoreState();
                } else {
                    if (!Character.isWhitespace((char)character))
                        text.append((char)character);
                }
                break;
                
            // we have just seen something like this: <foo a="b"/
            // and are looking for the final >.
			case SINGLE_TAG:
                if(character != '>')
                    throwException(MessageLocalization.getComposedMessage("expected.gt.for.tag.lt.1.gt", tag));
				doTag();
                processTag(true);
                processTag(false);
                initTag();
                if(!html && nested==0) {
                    doc.endDocument();
                    return;
                }
                state = restoreState();
                break;
                
            // we are processing CDATA
			case CDATA:
                if(character == '>'
                && text.toString().endsWith("]]")) {
                    text.setLength(text.length()-2);
                    flush();
                    state = restoreState();
                } else
                    text.append((char)character);
                break;
                
            // we are processing a comment.  We are inside
            // the <!-- .... --> looking for the -->.
			case COMMENT:
                if(character == '>'
                && text.toString().endsWith("--")) {
                    text.setLength(text.length() - 2);
                    flush();
                    state = restoreState();
                } else
                    text.append((char)character);
                break;
                
            // We are inside one of these <? ... ?> or one of these <!DOCTYPE ... >
			case PI:
                if(character == '>') {
                    state = restoreState();
                    if(state == TEXT) state = UNKNOWN;
                }
                break;
                
            // we are processing an entity, e.g. &lt;, &#187;, etc.
			case ENTITY:
                if(character == ';') {
                    state = restoreState();
                    String cent = entity.toString();
                    entity.setLength(0);
                    char ce = EntitiesToUnicode.decodeEntity(cent);
                    if (ce == '\0')
                    	text.append('&').append(cent).append(';');
                    else
                    	text.append(ce);
                } else if ((character != '#' && (character < '0' || character > '9') && (character < 'a' || character > 'z')
                    && (character < 'A' || character > 'Z')) || entity.length() >= 7) {
                    state = restoreState();
                    previousCharacter = character;
                    text.append('&').append(entity.toString());
                    entity.setLength(0);
                }
                else {
                    entity.append((char)character);
                }
                break;
            // We are processing the quoted right-hand side of an element's attribute.
			case QUOTE:
                if (html && quoteCharacter == ' ' && character == '>') {
                    flush();
                    processTag(true);
                    initTag();
                    state = restoreState();
                }
                else if (html && quoteCharacter == ' ' && Character.isWhitespace((char)character)) {
                	flush();
                    state = TAG_EXAMINED;
                }
                else if (html && quoteCharacter == ' ') {
                    text.append((char)character);
                }
                else if(character == quoteCharacter) {
                	flush();
                    state = TAG_EXAMINED;
                } else if(" \r\n\u0009".indexOf(character)>=0) {
                    text.append(' ');
                } else if(character == '&') {
                    saveState(state);
                    state = ENTITY;
                    entity.setLength(0);
                } else {
                    text.append((char)character);
                }
                break;
                
			case ATTRIBUTE_KEY:
                if(Character.isWhitespace((char)character)) {
                    flush();
                    state = ATTRIBUTE_EQUAL;
                } else if(character == '=') {
                	flush();
                    state = ATTRIBUTE_VALUE;
                } else if (html && character == '>') {
                    text.setLength(0);
                    processTag(true);
                    initTag();
                    state = restoreState();
                } else {
                    text.append((char)character);
                }
                break;
                
			case ATTRIBUTE_EQUAL:
                if(character == '=') {
                    state = ATTRIBUTE_VALUE;
                } else if(Character.isWhitespace((char)character)) {
                    // empty
                } else if (html && character == '>') {
                    text.setLength(0);
                    processTag(true);
                    initTag();
                    state = restoreState();
                } else if (html && character == '/') {
                    flush();
                    state = SINGLE_TAG;
                } else if (html) {
                    flush();
                    text.append((char)character);
                    state = ATTRIBUTE_KEY;
                } else {
                    throwException(MessageLocalization.getComposedMessage("error.in.attribute.processing"));
                }
                break;
                
			case ATTRIBUTE_VALUE:
                if(character == '"' || character == '\'') {
                    quoteCharacter = character;
                    state = QUOTE;
                } else if(Character.isWhitespace((char)character)) {
                    // empty
                } else if (html && character == '>') {
                    flush();
                    processTag(true);
                    initTag();
                    state = restoreState();
                } else if (html) {
                    text.append((char)character);
                    quoteCharacter = ' ';
                    state = QUOTE;
                } else {
                    throwException(MessageLocalization.getComposedMessage("error.in.attribute.processing"));
                }
                break;
            }
        }
    }

    /**
     * Gets a state from the stack
     * @return the previous state
     */
    private int restoreState() {
        if(!stack.empty())
            return ((Integer)stack.pop()).intValue();
        else
            return UNKNOWN;
    }
    /**
     * Adds a state to the stack.
     * @param	s	a state to add to the stack
     */
    private void saveState(int s) {
    	stack.push(new Integer(s));
    }
    /**
     * Flushes the text that is currently in the buffer.
     * The text can be ignored, added to the document
     * as content or as comment,... depending on the current state.
     */
    private void flush() {
    	switch(state){
    	case TEXT:
    	case CDATA:
            if(text.length() > 0) {
                doc.text(text.toString());
            }
            break;
    	case COMMENT:
        	if (comment != null) {
                comment.comment(text.toString());
            }
        	break;
    	case ATTRIBUTE_KEY:
            attributekey = text.toString();
            if (html)
                attributekey = attributekey.toLowerCase();
    		break;
    	case QUOTE:
    	case ATTRIBUTE_VALUE:
        	attributevalue = text.toString();
            attributes.put(attributekey,attributevalue);
            break;
    	default:
    		// do nothing
    	}
        text.setLength(0);
    }
    /**
     * Initialized the tag name and attributes.
     */
    private void initTag() {
        tag = null;
        attributes = new HashMap();
    }
    /** Sets the name of the tag. */
    private void doTag() {
    	if(tag == null)
    		tag = text.toString();
    	if (html)
    		tag = tag.toLowerCase();
    	text.setLength(0);
    }
    /**
     * processes the tag.
     * @param start	if true we are dealing with a tag that has just been opened; if false we are closing a tag.
     */
    private void processTag(boolean start) {
    	if (start) {
    		nested++;
    		doc.startElement(tag,attributes);
    	}
    	else {
            nested--;
            doc.endElement(tag);
    	}
    }
    /** Throws an exception */
    private void throwException(String s) throws IOException {
        throw new IOException(MessageLocalization.getComposedMessage("1.near.line.2.column.3", s, String.valueOf(lines), String.valueOf(columns)));
    }
    
    /**
     * Parses the XML document firing the events to the handler.
     * @param doc the document handler
     * @param r the document. The encoding is already resolved. The reader is not closed
     * @throws IOException on error
     */
    public static void parse(SimpleXMLDocHandler doc, SimpleXMLDocHandlerComment comment, Reader r, boolean html) throws IOException {
    	SimpleXMLParser parser = new SimpleXMLParser(doc, comment, html);
    	parser.go(r);
    }
    
    /**
     * Parses the XML document firing the events to the handler.
     * @param doc the document handler
     * @param in the document. The encoding is deduced from the stream. The stream is not closed
     * @throws IOException on error
     */    
    public static void parse(SimpleXMLDocHandler doc, InputStream in) throws IOException {
        byte b4[] = new byte[4];
        int count = in.read(b4);
        if (count != 4)
            throw new IOException(MessageLocalization.getComposedMessage("insufficient.length"));
        String encoding = getEncodingName(b4);
        String decl = null;
        if (encoding.equals("UTF-8")) {
            StringBuffer sb = new StringBuffer();
            int c;
            while ((c = in.read()) != -1) {
                if (c == '>')
                    break;
                sb.append((char)c);
            }
            decl = sb.toString();
        }
        else if (encoding.equals("CP037")) {
            ByteArrayOutputStream bi = new ByteArrayOutputStream();
            int c;
            while ((c = in.read()) != -1) {
                if (c == 0x6e) // that's '>' in ebcdic
                    break;
                bi.write(c);
            }
            decl = new String(bi.toByteArray(), "CP037");
        }
        if (decl != null) {
            decl = getDeclaredEncoding(decl);
            if (decl != null)
                encoding = decl;
        }
        parse(doc, new InputStreamReader(in, IanaEncodings.getJavaEncoding(encoding)));
    }
    
    private static String getDeclaredEncoding(String decl) {
        if (decl == null)
            return null;
        int idx = decl.indexOf("encoding");
        if (idx < 0)
            return null;
        int idx1 = decl.indexOf('"', idx);
        int idx2 = decl.indexOf('\'', idx);
        if (idx1 == idx2)
            return null;
        if ((idx1 < 0 && idx2 > 0) || (idx2 > 0 && idx2 < idx1)) {
            int idx3 = decl.indexOf('\'', idx2 + 1);
            if (idx3 < 0)
                return null;
            return decl.substring(idx2 + 1, idx3);
        }
        if ((idx2 < 0 && idx1 > 0) || (idx1 > 0 && idx1 < idx2)) {
            int idx3 = decl.indexOf('"', idx1 + 1);
            if (idx3 < 0)
                return null;
            return decl.substring(idx1 + 1, idx3);
        }
        return null;
    }
    
    public static void parse(SimpleXMLDocHandler doc,Reader r) throws IOException {
        parse(doc, null, r, false);
    }
    
    /**
     * Escapes a string with the appropriated XML codes.
     * @param s the string to be escaped
     * @param onlyASCII codes above 127 will always be escaped with &amp;#nn; if <CODE>true</CODE>
     * @return the escaped string
     */    
    public static String escapeXML(String s, boolean onlyASCII) {
        char cc[] = s.toCharArray();
        int len = cc.length;
        StringBuffer sb = new StringBuffer();
        for (int k = 0; k < len; ++k) {
            int c = cc[k];
            switch (c) {
                case '<':
                    sb.append("&lt;");
                    break;
                case '>':
                    sb.append("&gt;");
                    break;
                case '&':
                    sb.append("&amp;");
                    break;
                case '"':
                    sb.append("&quot;");
                    break;
                case '\'':
                    sb.append("&apos;");
                    break;
                default:
                	if ((c == 0x9) || (c == 0xA) || (c == 0xD)
                		|| ((c >= 0x20) && (c <= 0xD7FF))
                		|| ((c >= 0xE000) && (c <= 0xFFFD))
                		|| ((c >= 0x10000) && (c <= 0x10FFFF))) { 
                		if (onlyASCII && c > 127)
                			sb.append("&#").append(c).append(';');
                		else 
                			sb.append((char)c);
                	}
            }
        }
        return sb.toString();
    }
    /**
     * Returns the IANA encoding name that is auto-detected from
     * the bytes specified, with the endian-ness of that encoding where appropriate.
     * (method found in org.apache.xerces.impl.XMLEntityManager, originally published
     * by the Apache Software Foundation under the Apache Software License; now being
     * used in iText under the MPL)
     * @param b4    The first four bytes of the input.
     * @return an IANA-encoding string
     */
    private static String getEncodingName(byte[] b4) {
        
        // UTF-16, with BOM
        int b0 = b4[0] & 0xFF;
        int b1 = b4[1] & 0xFF;
        if (b0 == 0xFE && b1 == 0xFF) {
            // UTF-16, big-endian
            return "UTF-16BE";
        }
        if (b0 == 0xFF && b1 == 0xFE) {
            // UTF-16, little-endian
            return "UTF-16LE";
        }
        
        // UTF-8 with a BOM
        int b2 = b4[2] & 0xFF;
        if (b0 == 0xEF && b1 == 0xBB && b2 == 0xBF) {
            return "UTF-8";
        }
        
        // other encodings
        int b3 = b4[3] & 0xFF;
        if (b0 == 0x00 && b1 == 0x00 && b2 == 0x00 && b3 == 0x3C) {
            // UCS-4, big endian (1234)
            return "ISO-10646-UCS-4";
        }
        if (b0 == 0x3C && b1 == 0x00 && b2 == 0x00 && b3 == 0x00) {
            // UCS-4, little endian (4321)
            return "ISO-10646-UCS-4";
        }
        if (b0 == 0x00 && b1 == 0x00 && b2 == 0x3C && b3 == 0x00) {
            // UCS-4, unusual octet order (2143)
            // REVISIT: What should this be?
            return "ISO-10646-UCS-4";
        }
        if (b0 == 0x00 && b1 == 0x3C && b2 == 0x00 && b3 == 0x00) {
            // UCS-4, unusual octet order (3412)
            // REVISIT: What should this be?
            return "ISO-10646-UCS-4";
        }
        if (b0 == 0x00 && b1 == 0x3C && b2 == 0x00 && b3 == 0x3F) {
            // UTF-16, big-endian, no BOM
            // (or could turn out to be UCS-2...
            // REVISIT: What should this be?
            return "UTF-16BE";
        }
        if (b0 == 0x3C && b1 == 0x00 && b2 == 0x3F && b3 == 0x00) {
            // UTF-16, little-endian, no BOM
            // (or could turn out to be UCS-2...
            return "UTF-16LE";
        }
        if (b0 == 0x4C && b1 == 0x6F && b2 == 0xA7 && b3 == 0x94) {
            // EBCDIC
            // a la xerces1, return CP037 instead of EBCDIC here
            return "CP037";
        }
        
        // default encoding
        return "UTF-8";
    }
}
