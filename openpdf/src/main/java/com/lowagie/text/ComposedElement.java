/*
 * $Id: Element.java 3672 2009-02-01 15:32:09Z blowagie $
 *
 * Copyright 1999, 2000, 2001, 2002 by Bruno Lowagie.
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
 */

package com.lowagie.text;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Interface for composed elements.
 *
 * @see Anchor
 * @see Cell
 * @see Chapter
 * @see Paragraph
 * @see Phrase
 * @see Section
 */

public interface ComposedElement<E extends Element> extends Element {
    /**
     * Gets all the children of the current element.
     *
     * @return    an <CODE> ArrayList<Element></CODE>
     */
    public Collection<? extends E> getChildren();

    /**
     * Gets the size of the list.
     *
     * @return    a <CODE>size</CODE>
     */
    default int size() {
        return getChildren().size();
    }

    /**
     * Returns <CODE>true</CODE> if the list is empty.
     *
     * @return <CODE>true</CODE> if the list is empty
     */
    default boolean isEmpty() {
        return getChildren().isEmpty();
    }

    /**
     * Gets all the chunks in this element.
     *
     * @return    an <CODE>ArrayList</CODE>
     */
    default ArrayList<Chunk> getChunks() {
        return ComposedElement.getChunks(getChildren());
    }

    /**
     * Processes the element by adding it (or the different parts) to an
     * <CODE>ElementListener</CODE>.
     *
     * @param    listener        the <CODE>ElementListener</CODE>
     * @return    <CODE>true</CODE> if the element was processed successfully
     */
    default boolean process(ElementListener listener) {
        return ComposedElement.process(listener, getChildren());
    }

    public static ArrayList<Chunk> getChunks(Iterable<? extends Element> elements) {
        ArrayList<Chunk> tmp = new ArrayList<>();
        for(Element e : elements) {
            tmp.addAll(e.getChunks());
        }
        return tmp;
    }

    public static boolean process(ElementListener listener, Iterable<? extends Element> elements) {
        try {
            if(elements!=null) for (Element e : elements) {
                listener.add(e);
            }
            return true;
        } catch(DocumentException de) {
            return false;
        }
    }
}