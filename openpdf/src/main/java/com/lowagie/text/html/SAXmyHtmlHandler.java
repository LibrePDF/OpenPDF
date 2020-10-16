/*
 * $Id: SAXmyHtmlHandler.java 3373 2008-05-12 16:21:24Z xlv $
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

package com.lowagie.text.html;

import java.util.Properties;
import javax.annotation.Nullable;

import com.lowagie.text.DocListener;
import com.lowagie.text.DocumentException;
import com.lowagie.text.ElementTags;
import com.lowagie.text.ExceptionConverter;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.xml.SAXiTextHandler;
import org.xml.sax.Attributes;

/**
 * The <CODE>Tags</CODE>-class maps several XHTML-tags to iText-objects.
 */

public class SAXmyHtmlHandler extends SAXiTextHandler<HtmlPeer> // SAXmyHandler
{

    /**
     * These are the properties of the body section.
     */
    private Properties bodyAttributes = new Properties();

    /**
     * This is the status of the table border.
     */
    private boolean tableBorder = false;

    /**
     * Constructs a new SAXiTextHandler that will translate all the events
     * triggered by the parser to actions on the <CODE>Document</CODE>-object.
     *
     * @param document this is the document on which events must be triggered
     */
    public SAXmyHtmlHandler(DocListener document) {
        this(document, new HtmlTagMap());
    }

    /**
     * Constructs a new SAXiTextHandler that will translate all the events
     * triggered by the parser to actions on the <CODE>Document</CODE>-object.
     *
     * @param document this is the document on which events must be triggered
     * @param htmlTags a tagmap translating HTML tags to iText tags
     */
    public SAXmyHtmlHandler(DocListener document, HtmlTagMap htmlTags) {
        this(document, htmlTags, null);
    }

    public SAXmyHtmlHandler(DocListener document, HtmlTagMap htmlTags, BaseFont bf) {
        super(document, htmlTags, bf);
    }

    /**
     * Constructs a new SAXiTextHandler that will translate all the events
     * triggered by the parser to actions on the <CODE>Document</CODE>-object.
     *
     * @param document this is the document on which events must be triggered
     * @param bf the base class for the supported fonts
     */
    public SAXmyHtmlHandler(DocListener document, BaseFont bf) {
        this(document, new HtmlTagMap(), bf);
    }

    /**
     * This method gets called when a start tag is encountered.
     *
     * @param uri       the Uniform Resource Identifier
     * @param localName the local name (without prefix), or the empty string if
     *                  Namespace processing is not being performed.
     * @param name      the name of the tag that is encountered
     * @param attrs     the list of attributes
     */
    @Override
    public void startElement(String uri, String localName, String name, @Nullable Attributes attrs) {
        // super.handleStartingTags is replaced with handleStartingTags
        // suggestion by Vu Ngoc Tan/Hop
        String lowerCaseName = name.toLowerCase();
        if (HtmlTagMap.isHtml(lowerCaseName)) {
            // we do nothing
            return;
        }
        if (HtmlTagMap.isHead(lowerCaseName)) {
            // we do nothing
            return;
        }
        if (HtmlTagMap.isTitle(lowerCaseName)) {
            // we do nothing
            return;
        }
        if (HtmlTagMap.isMeta(lowerCaseName)) {
            // we look if we can change the body attributes
            String meta = null;
            String content = null;
            if (attrs != null) {
                for (int i = 0; i < attrs.getLength(); i++) {
                    String attribute = attrs.getQName(i);
                    if (attribute.equalsIgnoreCase(HtmlTags.CONTENT))
                        content = attrs.getValue(i);
                    else if (attribute.equalsIgnoreCase(HtmlTags.NAME))
                        meta = attrs.getValue(i);
                }
            }
            if (meta != null && content != null) {
                bodyAttributes.put(meta, content);
            }
            return;
        }
        if (HtmlTagMap.isLink(lowerCaseName)) {
            // we do nothing for the moment, in a later version we could extract
            // the style sheet
            return;
        }
        if (HtmlTagMap.isBody(lowerCaseName)) {
            // maybe we could extract some info about the document: color,
            // margins,...
            // but that's for a later version...
            HtmlPeer peer = new HtmlPeer(ElementTags.ITEXT, lowerCaseName);
            peer.addAlias(ElementTags.TOP, HtmlTags.TOPMARGIN);
            peer.addAlias(ElementTags.BOTTOM, HtmlTags.BOTTOMMARGIN);
            peer.addAlias(ElementTags.RIGHT, HtmlTags.RIGHTMARGIN);
            peer.addAlias(ElementTags.LEFT, HtmlTags.LEFTMARGIN);
            bodyAttributes.putAll(peer.getAttributes(attrs));
            handleStartingTags(peer.getTag(), bodyAttributes);
            return;
        }
        if (myTags.containsKey(lowerCaseName)) {
            HtmlPeer peer = myTags.get(lowerCaseName);
            if (ElementTags.TABLE.equals(peer.getTag()) || ElementTags.CELL.equals(peer.getTag())) {
                Properties p = peer.getAttributes(attrs);
                String value;
                if (ElementTags.TABLE.equals(peer.getTag())
                        && (value = p.getProperty(ElementTags.BORDERWIDTH)) != null) {
                    if (Float.parseFloat(value + "f") > 0) {
                        tableBorder = true;
                    }
                }
                if (tableBorder) {
                    p.put(ElementTags.LEFT, String.valueOf(true));
                    p.put(ElementTags.RIGHT, String.valueOf(true));
                    p.put(ElementTags.TOP, String.valueOf(true));
                    p.put(ElementTags.BOTTOM, String.valueOf(true));
                }
                handleStartingTags(peer.getTag(), p);
                return;
            }
            handleStartingTags(peer.getTag(), peer.getAttributes(attrs));
            return;
        }
        Properties attributes = new Properties();
        if (attrs != null) {
            for (int i = 0; i < attrs.getLength(); i++) {
                String attribute = attrs.getQName(i).toLowerCase();
                attributes.setProperty(attribute, attrs.getValue(i).toLowerCase());
            }
        }
        handleStartingTags(lowerCaseName, attributes);
    }

    /**
     * This method gets called when an end tag is encountered.
     *
     * @param uri       the Uniform Resource Identifier
     * @param localName the local name (without prefix), or the empty string if
     *                  Namespace processing is not being performed.
     * @param name      the name of the tag that ends
     */
    @Override
    public void endElement(String uri, String localName, String name) {
        String lowerCaseName = name.toLowerCase();
        if (ElementTags.PARAGRAPH.equals(lowerCaseName)) {
            try {
                document.add(stack.pop());
                return;
            } catch (DocumentException e) {
                throw new ExceptionConverter(e);
            }
        }
        if (HtmlTagMap.isHead(lowerCaseName)) {
            // we do nothing
            return;
        }
        if (HtmlTagMap.isTitle(lowerCaseName)) {
            if (currentChunk != null) {
                bodyAttributes.put(ElementTags.TITLE, currentChunk.getContent());
            }
            return;
        }
        if (HtmlTagMap.isMeta(lowerCaseName)) {
            // we do nothing
            return;
        }
        if (HtmlTagMap.isLink(lowerCaseName)) {
            // we do nothing
            return;
        }
        if (HtmlTagMap.isBody(lowerCaseName)) {
            // we do nothing
            return;
        }
        if (myTags.containsKey(lowerCaseName)) {
            HtmlPeer peer = myTags.get(lowerCaseName);
            if (ElementTags.TABLE.equals(peer.getTag())) {
                tableBorder = false;
            }
            super.handleEndingTags(peer.getTag());
            return;
        }
        // super.handleEndingTags is replaced with handleEndingTags
        // suggestion by Ken Auer
        handleEndingTags(lowerCaseName);
    }
}
