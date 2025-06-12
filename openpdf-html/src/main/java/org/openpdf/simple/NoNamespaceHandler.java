/*
 *
 * XhtmlDocument.java
 * Copyright (c) 2004, 2005 Torbjoern Gannholm
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
 *
 */

package org.openpdf.simple;

import com.google.errorprone.annotations.CheckReturnValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;
import org.openpdf.css.extend.TreeResolver;
import org.openpdf.css.sheet.StylesheetInfo;
import org.openpdf.extend.NamespaceHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.openpdf.css.sheet.StylesheetInfo.Origin.AUTHOR;
import static org.openpdf.css.sheet.StylesheetInfo.mediaTypes;

/**
 * Handles a general XML document
 *
 * @author Torbjoern Gannholm
 */
public class NoNamespaceHandler implements NamespaceHandler {

    private static final String _namespace = "http://www.w3.org/XML/1998/namespace";

    @Override
    @CheckReturnValue
    public String getNamespace() {
        return _namespace;
    }

    @Override
    @CheckReturnValue
    public String getAttributeValue(Element e, String attrName) {
        return e.getAttribute(attrName);
    }

    @Override
    @CheckReturnValue
    public String getAttributeValue(Element e, @Nullable String namespaceURI, String attrName) {
        if (TreeResolver.NO_NAMESPACE.equals(namespaceURI)) {
            return e.getAttribute(attrName);
        } else if (namespaceURI == null) {
            if (e.getLocalName() == null) { // No namespaces
                return e.getAttribute(attrName);
            } else {
                NamedNodeMap attrs = e.getAttributes();
                int l = attrs.getLength();
                for (int i = 0; i < l; i++) {
                    Attr attr = (Attr)attrs.item(i);
                    if (attrName.equals(attr.getLocalName())) {
                        return attr.getValue();
                    }
                }

                return "";
            }
        } else {
            return e.getAttributeNS(namespaceURI, attrName);
        }
    }

    @Override
    @Nullable
    @CheckReturnValue
    public String getClass(Element e) {
        return null;
    }

    @Override
    @Nullable
    @CheckReturnValue
    public String getID(Element e) {
        return null;
    }

    @Override
    @CheckReturnValue
    public String getLang(Element e) {
        return e.getAttribute("lang");
    }

    @Override
    @Nullable
    @CheckReturnValue
    public String getElementStyling(Element e) {
        return null;
    }

    @Override
    @Nullable
    @CheckReturnValue
    public String getNonCssStyling(Element e) {
        return null;
    }

    @Override
    @Nullable
    @CheckReturnValue
    public String getLinkUri(Element e) {
        return null;
    }

    @Override
    @Nullable
    @CheckReturnValue
    public String getDocumentTitle(Document doc) {
        return null;
    }

    @Override
    @Nullable
    @CheckReturnValue
    public String getAnchorName(@Nullable Element e) {
        return null;
    }

    @Override
    @CheckReturnValue
    public boolean isImageElement(Element e) {
        return false;
    }

    @Override
    @Nullable
    @CheckReturnValue
    public String getImageSourceURI(Element e) {
        return null;
    }

    @Override
    @CheckReturnValue
    public boolean isFormElement(Element e) {
        return false;
    }

    private static final Pattern _typePattern = Pattern.compile("type\\s?=\\s?");
    private static final Pattern _hrefPattern = Pattern.compile("href\\s?=\\s?");
    private static final Pattern _alternatePattern = Pattern.compile("alternate\\s?=\\s?");
    private static final Pattern _mediaPattern = Pattern.compile("media\\s?=\\s?");

    @Override
    @CheckReturnValue
    public List<StylesheetInfo> getStylesheets(Document doc) {
        List<StylesheetInfo> list = new ArrayList<>();
        //get the processing-instructions (actually for XmlDocuments)
        //type and href are required to be set
        NodeList nl = doc.getChildNodes();
        for (int i = 0, len = nl.getLength(); i < len; i++) {
            Node node = nl.item(i);
            if (node.getNodeType() != Node.PROCESSING_INSTRUCTION_NODE) continue;
            ProcessingInstruction piNode = (ProcessingInstruction) node;
            if (!piNode.getTarget().equals("xml-stylesheet")) {
                continue;
            }

            String pi = piNode.getData();
            Matcher m = _alternatePattern.matcher(pi);
            if (m.matches()) {
                int start = m.end();
                String alternate = pi.substring(start + 1, pi.indexOf(pi.charAt(start), start + 1));
                //TODO: handle alternate stylesheets
                if (alternate.equals("yes")) continue;//DON'T get alternate stylesheets for now
            }
            String type = detectType(pi);
            //TODO: handle other stylesheet types
            if (!Objects.equals(type, "text/css")) continue; // for now

            StylesheetInfo info = new StylesheetInfo(AUTHOR, detectUri(pi), mediaTypes(detectMediaTypes(pi)), null);
            list.add(info);
        }

        return list;
    }

    @Nullable
    @CheckReturnValue
    private String detectType(String pi) {
        Matcher m = _typePattern.matcher(pi);
        if (m.find()) {
            int start = m.end();
            return pi.substring(start + 1, pi.indexOf(pi.charAt(start), start + 1));
        }
        return null;
    }

    @Nullable
    @CheckReturnValue
    private String detectUri(String pi) {
        Matcher m = _hrefPattern.matcher(pi);
        if (m.find()) {
            int start = m.end();
            return pi.substring(start + 1, pi.indexOf(pi.charAt(start), start + 1));
        }
        return null;
    }

    @NonNull
    @CheckReturnValue
    private String detectMediaTypes(String pi) {
        Matcher m = _mediaPattern.matcher(pi);
        if (m.find()) {
            int start = m.end();
            return pi.substring(start + 1, pi.indexOf(pi.charAt(start), start + 1));
        } else {
            return "screen";
        }
    }

    @Override
    @CheckReturnValue
    public Optional<StylesheetInfo> getDefaultStylesheet() {
        return Optional.empty();
    }

}
