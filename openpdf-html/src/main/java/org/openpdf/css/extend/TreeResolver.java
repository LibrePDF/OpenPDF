/*
 * TreeResolver.java
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
package org.openpdf.css.extend;

import com.google.errorprone.annotations.CheckReturnValue;
import org.jspecify.annotations.Nullable;
import org.w3c.dom.Node;

/**
 * Gives the css matcher access to the information it needs about the tree structure.
 * <p>
 * Elements are the "things" in the tree structure that can be matched by the matcher.
 *
 * @author scott
 */
public interface TreeResolver {
    // XXX Where should this go (used by parser, TreeResolver, and AttributeResolver
    String NO_NAMESPACE = "";

    /**
     * returns the parent element of an element, or null if this was the root element
     */
    @Nullable
    Node getParentElement(Node element);

    /**
     * returns the name of the element so that it may match against the selectors
     */
    String getElementName(Node element);

    /**
     * The previous sibling element, or null if none exists
     */
    @Nullable
    Node getPreviousSiblingElement(Node node);

    /**
     * returns true if this element is the first child element of its parent
     */
    boolean isFirstChildElement(Node element);

    /**
     * returns true if this element is the last child element of its parent
     */
    boolean isLastChildElement(Node element);

    /**
     * Returns the index of the position of the submitted element among its element node siblings.
     * @param element The node
     * @return -1 in case of error, 0 indexed position otherwise
     */
    @CheckReturnValue
    int getPositionOfElement(Node element);

    /**
     * Returns {@code true} if {@code element} has the local name
     * {@code name} and namespace URI {@code namespaceURI}.
     * @param element The node
     * @param namespaceURI The namespace to match, may be null to signify any
     * namespace.  Use {@link #NO_NAMESPACE} to signify that {@code name}
     * should only match when there is no namespace defined on {@code element}.
     * @param name The name to match, may not be null
     */
    boolean matchesElement(Node element, String namespaceURI, String name);
}
