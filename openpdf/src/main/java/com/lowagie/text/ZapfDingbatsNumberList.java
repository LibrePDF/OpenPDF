/*
 * Copyright 2003 by Michael Niedermair.
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

/**
 *
 * A special-version of <CODE>LIST</CODE> which use zapfdingbats-numbers (1..10).
 *
 * @see com.lowagie.text.List
 * @author Michael Niedermair and Bruno Lowagie
 */

public class ZapfDingbatsNumberList extends List {
    /**
     * which type
     */
    protected int type;

    /**
     * Creates a ZapdDingbatsNumberList
     * @param type the type of list
     */
    public ZapfDingbatsNumberList(int type) {
        super(true);
        this.type = type;
        float fontsize = symbol.getFont().getSize();
        symbol.setFont(FontFactory.getFont(FontFactory.ZAPFDINGBATS, fontsize, Font.NORMAL));
        postSymbol = " ";
    }

    /**
     * Creates a ZapdDingbatsNumberList
     * @param type the type of list
     * @param symbolIndent    indent
     */
    public ZapfDingbatsNumberList(int type, int symbolIndent) {
        super(true, symbolIndent);
        this.type = type;
        float fontsize = symbol.getFont().getSize();
        symbol.setFont(FontFactory.getFont(FontFactory.ZAPFDINGBATS, fontsize, Font.NORMAL));
        postSymbol = " ";
    }

    /**
     * set the type
     *
     * @param type
     */
    public void setType(int type) {
        this.type = type;
    }

    /**
     * get the type
     *
     * @return    char-number
     */
    public int getType() {
        return type;
    }

    /**
     * Adds an <CODE>Object</CODE> to the <CODE>List</CODE>.
     *
     * @param    element    the object to add.
     * @return true if adding the object succeeded
     */
    public boolean add(Element element) {
        if (element instanceof ListItem) {
            ListItem item = (ListItem) element;
            Chunk chunk = new Chunk(preSymbol, symbol.getFont());
            switch (type ) {
                case 0:
                    chunk.append(String.valueOf((char)(first + content.size() + 171)));
                    break;
                case 1:
                    chunk.append(String.valueOf((char)(first + content.size() + 181)));
                    break;
                case 2:
                    chunk.append(String.valueOf((char)(first + content.size() + 191)));
                    break;
                default:
                    chunk.append(String.valueOf((char)(first + content.size() + 201)));
            }
            chunk.append(postSymbol);
            item.setListSymbol(chunk);
            item.setIndentationLeft(symbolIndent, autoindent);
            item.setIndentationRight(0);
            return content.add(item);
        } else if (element instanceof List) {
            List nested = (List) element;
            nested.setIndentationLeft(nested.getIndentationLeft() + symbolIndent);
            first--;
            return content.add(nested);
        }
        return false;
    }
}
