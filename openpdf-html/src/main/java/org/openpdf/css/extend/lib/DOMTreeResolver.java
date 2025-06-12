/*
 * DOMTreeResolver.java
 * Copyright (c) 2005 Scott Cytacki
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
package org.openpdf.css.extend.lib;

import org.jspecify.annotations.Nullable;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.openpdf.css.extend.TreeResolver;

import static java.util.Objects.requireNonNullElseGet;

/**
 * works for a w3c DOM tree
 * @author scott
 */
public class DOMTreeResolver implements TreeResolver {
    @Nullable
    @Override
    public Node getParentElement(Node element) {
        Node parent = element.getParentNode();
        if (parent.getNodeType() != Node.ELEMENT_NODE) parent = null;
        return parent;
    }

    @Nullable
    @Override
    public Node getPreviousSiblingElement(Node element) {
        Node sibling = element.getPreviousSibling();
        while (sibling != null && sibling.getNodeType() != Node.ELEMENT_NODE) {
            sibling = sibling.getPreviousSibling();
        }
        if (sibling == null || sibling.getNodeType() != Node.ELEMENT_NODE) {
            return null;
        }
        return sibling;
    }

    @Override
    public String getElementName(Node element) {
        String name = element.getLocalName();
        if (name == null) name = element.getNodeName();
        return name;
    }

    @Override
    public boolean isFirstChildElement(Node element) {
        Node parent = element.getParentNode();
        Node currentChild = parent.getFirstChild();
        while (currentChild != null && currentChild.getNodeType() != Node.ELEMENT_NODE) {
            currentChild = currentChild.getNextSibling();
        }
        return currentChild == element;
    }

    @Override
    public boolean isLastChildElement(Node element) {
        Node parent = element.getParentNode();
        Node currentChild = parent.getLastChild();
        while (currentChild != null && currentChild.getNodeType() != Node.ELEMENT_NODE) {
            currentChild = currentChild.getPreviousSibling();
        }
        return currentChild == element;
    }

    @Override
    public boolean matchesElement(Node element, String namespaceURI, String name) {
        String localName = element.getLocalName();
        String eName = requireNonNullElseGet(localName, element::getNodeName);

        if (namespaceURI != null) {
            return name.equals(localName) && namespaceURI.equals(element.getNamespaceURI());
        } else {
            return name.equals(eName);
        }
    }

    @Override
    public int getPositionOfElement(Node element) {
        Node parent = element.getParentNode();
        NodeList nl = parent.getChildNodes();

        int elt_count = 0;
        int i = 0;
        while (i < nl.getLength()) {
            if (nl.item(i).getNodeType() == Node.ELEMENT_NODE) {
                if(nl.item(i) == element) {
                    return elt_count;
                } else {
                    elt_count++;
                }
            }
            i++;
        }

        //should not happen
        return -1;
    }
}
