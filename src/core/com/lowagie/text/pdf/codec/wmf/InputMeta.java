/*
 * $Id: InputMeta.java 3373 2008-05-12 16:21:24Z xlv $
 *
 * Copyright 2001, 2002 Paulo Soares
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

package com.lowagie.text.pdf.codec.wmf;

import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;

import com.lowagie.text.Utilities;

public class InputMeta {
    
    InputStream in;
    int length;
    
    public InputMeta(InputStream in) {
        this.in = in;
    }

    public int readWord() throws IOException{
        length += 2;
        int k1 = in.read();
        if (k1 < 0)
            return 0;
        return (k1 + (in.read() << 8)) & 0xffff;
    }

    public int readShort() throws IOException{
        int k = readWord();
        if (k > 0x7fff)
            k -= 0x10000;
        return k;
    }

    public int readInt() throws IOException{
        length += 4;
        int k1 = in.read();
        if (k1 < 0)
            return 0;
        int k2 = in.read() << 8;
        int k3 = in.read() << 16;
        return k1 + k2 + k3 + (in.read() << 24);
    }
    
    public int readByte() throws IOException{
        ++length;
        return in.read() & 0xff;
    }
    
    public void skip(int len) throws IOException{
        length += len;
        Utilities.skip(in, len);
    }
    
    public int getLength() {
        return length;
    }
    
    public Color readColor() throws IOException{
        int red = readByte();
        int green = readByte();
        int blue = readByte();
        readByte();
        return new Color(red, green, blue);
    }
}
