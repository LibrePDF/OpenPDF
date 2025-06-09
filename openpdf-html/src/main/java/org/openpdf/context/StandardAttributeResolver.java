/*
 * {{{ header & license
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
 * }}}
 */
package org.openpdf.context;

import org.jspecify.annotations.Nullable;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.openpdf.css.extend.AttributeResolver;
import org.openpdf.extend.NamespaceHandler;
import org.openpdf.extend.UserAgentCallback;
import org.openpdf.extend.UserInterface;

import java.util.IdentityHashMap;
import java.util.Map;


/**
 * An instance which works together with a w3c DOM tree
 *
 * @author Torbjoern Gannholm
 */
public class StandardAttributeResolver implements AttributeResolver {
    private final NamespaceHandler nsh;
    private final UserAgentCallback uac;
    private final UserInterface ui;
    private final Map<Node, String> classAttributeCache = new IdentityHashMap<>();

    public StandardAttributeResolver(NamespaceHandler nsh, UserAgentCallback uac, UserInterface ui) {
        this.nsh = nsh;
        this.uac = uac;
        this.ui = ui;
    }

    /**
     * Gets the attributeValue attribute of the StandardAttributeResolver object
     */
    @Override
    public String getAttributeValue(Node e, String attrName) {
        return nsh.getAttributeValue((Element) e, attrName);
    }

    @Override
    public String getAttributeValue(Node e, String namespaceURI, String attrName) {
        return nsh.getAttributeValue((Element)e, namespaceURI, attrName);
    }

    /**
     * Gets the class attribute of the StandardAttributeResolver object
     */
    @Override
    @Nullable
    public String getClass(Node e) {
        return classAttributeCache.computeIfAbsent(e, (x) -> nsh.getClass((Element) e));
    }

    /**
     * Gets the iD attribute of the StandardAttributeResolver object
     */
    @Override
    @Nullable
    public String getID(Node e) {
        return nsh.getID((Element) e);
    }

    @Override
    @Nullable
    public String getNonCssStyling(Node e) {
        return nsh.getNonCssStyling((Element) e);
    }

    /**
     * Gets the elementStyling attribute of the StandardAttributeResolver object
     */
    @Override
    @Nullable
    public String getElementStyling(Node e) {
        return nsh.getElementStyling((Element) e);
    }

    /**
     * Gets the lang attribute of the StandardAttributeResolver object
     */
    @Override
    public String getLang(Node e) {
        return nsh.getLang((Element) e);
    }

    /**
     * Gets the link attribute of the StandardAttributeResolver object
     */
    @Override
    public boolean isLink(Node e) {
        return nsh.getLinkUri((Element) e) != null;
    }

    /**
     * Gets the visited attribute of the StandardAttributeResolver object
     */
    @Override
    public boolean isVisited(Node e) {
        return isLink(e) && uac.isVisited(nsh.getLinkUri((Element) e));
    }

    /**
     * Gets the hover attribute of the StandardAttributeResolver object
     */
    @Override
    public boolean isHover(Node e) {
        return ui.isHover((Element) e);
    }

    /**
     * Gets the active attribute of the StandardAttributeResolver object
     */
    @Override
    public boolean isActive(Node e) {
        return ui.isActive((Element) e);
    }

    /**
     * Gets the focus attribute of the StandardAttributeResolver object
     */
    @Override
    public boolean isFocus(Node e) {
        return ui.isFocus((Element) e);
    }
}

