/*
 * $Id: EnumerateTTC.java 4065 2009-09-16 23:09:11Z psoares33 $
 * $Name$
 *
 * Copyright 2001, 2002 by Paulo Soares.
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

package com.lowagie.text.pdf;

import java.io.IOException;
import java.util.HashMap;
import com.lowagie.text.error_messages.MessageLocalization;

import com.lowagie.text.DocumentException;
/** Enumerates all the fonts inside a True Type Collection.
 *
 * @author  Paulo Soares (psoares@consiste.pt)
 */
class EnumerateTTC extends TrueTypeFont{

    protected String[] names;

    EnumerateTTC(String ttcFile) throws DocumentException, IOException {
        fileName = ttcFile;
        rf = new RandomAccessFileOrArray(ttcFile);
        findNames();
    }

    EnumerateTTC(byte ttcArray[]) throws DocumentException, IOException {
        fileName = "Byte array TTC";
        rf = new RandomAccessFileOrArray(ttcArray);
        findNames();
    }
    
    void findNames() throws DocumentException, IOException {
        tables = new HashMap();
        
        try {
            String mainTag = readStandardString(4);
            if (!mainTag.equals("ttcf"))
                throw new DocumentException(MessageLocalization.getComposedMessage("1.is.not.a.valid.ttc.file", fileName));
            rf.skipBytes(4);
            int dirCount = rf.readInt();
            names = new String[dirCount];
            int dirPos = rf.getFilePointer();
            for (int dirIdx = 0; dirIdx < dirCount; ++dirIdx) {
                tables.clear();
                rf.seek(dirPos);
                rf.skipBytes(dirIdx * 4);
                directoryOffset = rf.readInt();
                rf.seek(directoryOffset);
                if (rf.readInt() != 0x00010000)
                    throw new DocumentException(MessageLocalization.getComposedMessage("1.is.not.a.valid.ttf.file", fileName));
                int num_tables = rf.readUnsignedShort();
                rf.skipBytes(6);
                for (int k = 0; k < num_tables; ++k) {
                    String tag = readStandardString(4);
                    rf.skipBytes(4);
                    int table_location[] = new int[2];
                    table_location[0] = rf.readInt();
                    table_location[1] = rf.readInt();
                    tables.put(tag, table_location);
                }
                names[dirIdx] = getBaseFont();
            }
        }
        finally {
            if (rf != null)
                rf.close();
        }
    }
    
    String[] getNames() {
        return names;
    }

}
