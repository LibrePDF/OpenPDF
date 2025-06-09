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
package org.openpdf.extend;

/**
 * @author   Torbjoern Gannholm
 */
public interface UserInterface {

    /**
     * Gets the hover attribute of the UserInterface object
     *
     * @param e  PARAM
     * @return   The hover value
     */
    boolean isHover(org.w3c.dom.Element e);

    /**
     * Gets the active attribute of the UserInterface object
     *
     * @param e  PARAM
     * @return   The active value
     */
    boolean isActive(org.w3c.dom.Element e);

    /**
     * Gets the focus attribute of the UserInterface object
     *
     * @param e  PARAM
     * @return   The focus value
     */
    boolean isFocus(org.w3c.dom.Element e);
}

