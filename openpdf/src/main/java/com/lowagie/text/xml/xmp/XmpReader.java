/*
 * Copyright 2008 by Bruno Lowagie
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
 * are Copyright (C) 2008 by Paulo Soares. All Rights Reserved.
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
package com.lowagie.text.xml.xmp;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.lowagie.text.ExceptionConverter;
import com.lowagie.text.xml.XmlDomWriter;

/**
 * Reads an XMP stream into an org.w3c.dom.Document objects.
 * Allows you to replace the contents of a specific tag.
 * @since 2.1.3
 */

public class XmpReader {

    private Document domDocument;
    
    /**
     * Constructs an XMP reader
     * @param	bytes	the XMP content
     * @throws ExceptionConverter 
     * @throws IOException 
     * @throws SAXException 
     */
	public XmpReader(byte[] bytes) throws SAXException, IOException {
		try {
	        DocumentBuilderFactory fact = DocumentBuilderFactory.newInstance();
	        fact.setNamespaceAware(true);
			DocumentBuilder db = fact.newDocumentBuilder();
	        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
	        domDocument = db.parse(bais);
		} catch (ParserConfigurationException e) {
			throw new ExceptionConverter(e);
		}
	}
	
	/**
	 * Replaces the content of a tag.
	 * @param	namespaceURI	the URI of the namespace
	 * @param	localName		the tag name
	 * @param	value			the new content for the tag
	 * @return	true if the content was successfully replaced
	 * @since	2.1.6 the return type has changed from void to boolean
	 */
	public boolean replace(String namespaceURI, String localName, String value) {
		NodeList nodes = domDocument.getElementsByTagNameNS(namespaceURI, localName);
		Node node;
		if (nodes.getLength() == 0)
			return false;
		for (int i = 0; i < nodes.getLength(); i++) {
			node = nodes.item(i);
			setNodeText(domDocument, node, value);
		}
		return true;
	}    
	
	/**
	 * Adds a tag.
	 * @param	namespaceURI	the URI of the namespace
	 * @param	parent			the tag name of the parent
	 * @param	localName		the name of the tag to add
	 * @param	value			the new content for the tag
	 * @return	true if the content was successfully added
	 * @since	2.1.6
	 */
	public boolean add(String parent, String namespaceURI, String localName, String value) {
		NodeList nodes = domDocument.getElementsByTagName(parent);
		if (nodes.getLength() == 0)
			return false;
		Node pNode;
		Node node;
		for (int i = 0; i < nodes.getLength(); i++) {
			pNode = nodes.item(i);
			NamedNodeMap attrs = pNode.getAttributes();
			for (int j = 0; j < attrs.getLength(); j++) {
				node = attrs.item(j);
				if (namespaceURI.equals(node.getNodeValue())) {
					node = domDocument.createElement(localName);
					node.appendChild(domDocument.createTextNode(value));
					pNode.appendChild(node);
					return true;
				}
			}
		}
		return false;
	}
	
    /**
     * Sets the text of this node. All the child's node are deleted and a new
     * child text node is created.
     * @param domDocument the <CODE>Document</CODE> that contains the node
     * @param n the <CODE>Node</CODE> to add the text to
     * @param value the text to add
     */
    public boolean setNodeText(Document domDocument, Node n, String value) {
        if (n == null)
            return false;
        Node nc = null;
        while ((nc = n.getFirstChild()) != null) {
            n.removeChild(nc);
        }
        n.appendChild(domDocument.createTextNode(value));
        return true;
    }
	
    /**
     * Writes the document to a byte array.
     */
	public byte[] serializeDoc() throws IOException {
		XmlDomWriter xw = new XmlDomWriter();
        ByteArrayOutputStream fout = new ByteArrayOutputStream();
        xw.setOutput(fout, null);
        fout.write(XmpWriter.XPACKET_PI_BEGIN.getBytes("UTF-8"));
        fout.flush();
        NodeList xmpmeta = domDocument.getElementsByTagName("x:xmpmeta");
        xw.write(xmpmeta.item(0));
        fout.flush();
		for (int i = 0; i < 20; i++) {
			fout.write(XmpWriter.EXTRASPACE.getBytes());
		}
        fout.write(XmpWriter.XPACKET_PI_END_W.getBytes());
        fout.close();
        return fout.toByteArray();
	}
}
