/*
 * Copyright 2004 Paulo Soares
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
 * Contributions by:
 * Lubos Strapko
 *
 * If you didn't download this code from the following link, you should check if
 * you aren't using an obsolete version:
 * https://github.com/LibrePDF/OpenPDF
 */

package com.lowagie.text.html.simpleparser;

import java.util.HashMap;
import java.util.Map;

import com.lowagie.text.html.Markup;

public class StyleSheet {

    private final Map<String, Map<String, String>> classMap = new HashMap<>();
    private final Map<String, Map<String, String>> tagMap = new HashMap<>();

    /**
     * @deprecated please use #applyStyle(String tag, Map&lt;String, String&gt; props) this method will be
     * removed in 2.0
     * @param props a HashMap
     * @param tag the tag
     */
    @Deprecated
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void applyStyle(String tag, HashMap props) {
        applyStyle(tag, (Map<String, String>) props);
    }

    public void applyStyle(String tag, Map<String, String> props) {
        Map<String, String> map = tagMap.get(tag.toLowerCase());
        if (map != null) {
            Map<String, String> temp = new HashMap<>(map);
            temp.putAll(props);
            props.putAll(temp);
        }
        String cm = props.get(Markup.HTML_ATTR_CSS_CLASS);
        if (cm == null)
            return;
        map = classMap.get(cm.toLowerCase());
        if (map == null)
            return;
        props.remove(Markup.HTML_ATTR_CSS_CLASS);
        Map<String, String> temp = new HashMap<>(map);
        temp.putAll(props);
        props.putAll(temp);
    }

    public void loadStyle(String style, Map<String, String> props) {
        classMap.put(style.toLowerCase(), props);
    }

    public void loadStyle(String style, String key, String value) {
        style = style.toLowerCase();
        Map<String, String> props = classMap.computeIfAbsent(style, k -> new HashMap<>());
        props.put(key, value);
    }

    public void loadTagStyle(String tag, Map<String, String> props) {
        tagMap.put(tag.toLowerCase(), props);
    }

    public void loadTagStyle(String tag, String key, String value) {
        tag = tag.toLowerCase();
        Map<String, String> props = tagMap.computeIfAbsent(tag, k -> new HashMap<>());
        props.put(key, value);
    }

}
