/*
 * {{{ header & license
 * Copyright (c) 2006 Wisconsin Court System
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
package org.openpdf.pdf;

import org.jspecify.annotations.Nullable;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

public class DOMUtil {
    @Nullable
    public static Element getChild(Element parent, String name) {
        NodeList children = parent.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node n = children.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                Element elem = (Element)n;
                if (elem.getTagName().equals(name)) {
                    return elem;
                }
            }
        }
        return null;
    }

    public static List<Element> getChildren(Element parent, String name) {
        NodeList children = parent.getChildNodes();
        List<Element> result = new ArrayList<>(children.getLength());
        for (int i = 0; i < children.getLength(); i++) {
            Node n = children.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                Element elem = (Element)n;
                if (elem.getTagName().equals(name)) {
                    result.add(elem);
                }
            }
        }
        return result;
    }

    /**
     * Loads all the text content in all offspring of an element.
     * Ignores all attributes, comments and processing instructions.
     *
     * @return a String with the text content of an element (maybe an empty string but will not be null).
     */
    public static String getText(Element parent) {
        StringBuilder sb = new StringBuilder();
        getText(parent, sb);
        return sb.toString();
    }

    /**
     * Appends all text content in all offspring of an element to a StringBuilder.
     * Ignores all attributes, comments and processing instructions.
     */
    public static void getText(Element parent, StringBuilder sb) {
        NodeList children = parent.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node n = children.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                getText((Element)n, sb);
            } else if (n.getNodeType() == Node.TEXT_NODE) {
                sb.append(n.getNodeValue());
            }
        }
    }
}
