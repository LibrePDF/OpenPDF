/*
 * AttributeResolver.java
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
package org.openpdf.css.extend;

import org.jspecify.annotations.Nullable;
import org.w3c.dom.Node;

/**
 * In XML, an application may or may not know how to find the ID and/or class
 * and/or attribute defaults of an element. <p>
 * <p>
 * To enable matching of identity conditions, class conditions, language, and
 * attribute defaults you need to provide an AttributeResolver to the StyleMap.
 * <p>
 * <p>
 * NOTE: The application is required to look in a document's internal subset for
 * default attribute values, but the application is not required to use its
 * built-in knowledge of a namespace or look in the external subset.
 *
 * @author Torbjoern Gannholm
 */
public interface AttributeResolver {

    /**
     * Required to return null if attribute does not exist, and
     * not null if attribute exists.
     */
    @Nullable
    String getAttributeValue(Node e, String attrName);

    /**
     * Required to return null if attribute does not exist and
     * not null if attribute exists.
     */
    @Nullable
    String getAttributeValue(Node e, String namespaceURI, String attrName);

    @Nullable
    String getClass(Node e);

    @Nullable
    String getID(Node e);

    /**
     * @return The non css styling (specificity 0,0,0,0 on author styles, according to css 2.1)
     */
    @Nullable
    String getNonCssStyling(Node e);

    /**
     * @return The elementStyling value
     *         (corresponding to xhtml style attribute, specificity 1,0,0,0 according to css 2.1)
     */
    @Nullable
    String getElementStyling(Node e);

    @Nullable
    String getLang(Node e);

    /**
     * Gets the link attribute of the AttributeResolver object
     */
    boolean isLink(Node e);

    /**
     * Gets the visited attribute of the AttributeResolver object
     */
    boolean isVisited(Node e);

    /**
     * Gets the hover attribute of the AttributeResolver object
     */
    boolean isHover(Node e);

    /**
     * Gets the active attribute of the AttributeResolver object
     */
    boolean isActive(Node e);

    /**
     * Gets the focus attribute of the AttributeResolver object
     */
    boolean isFocus(Node e);

}

