/*
 * Copyright 2007 Thomas Bickel
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
 * LGPL license (the ?GNU LIBRARY GENERAL PUBLIC LICENSE?), in which case the
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

package com.lowagie.text.rtf.document.output;

import java.io.OutputStream;

/**
 * The RtfNilOutputStream is a dummy output stream that sends all
 * bytes to the big byte bucket in the sky. It is used to improve
 * speed in those situations where processing is required, but
 * the results are not needed.
 * 
 * @version $Id: RtfNilOutputStream.java 3361 2008-05-11 12:28:57Z hallm $
 * @author Thomas Bickel (tmb99@inode.at)
 * @author Mark Hall (Mark.Hall@mail.room3b.eu)
 */
public final class RtfNilOutputStream extends OutputStream
{
    /**
     * The number of bytes theoretically written is stored.
     */
    private long size = 0;
    
    /**
     * Constructs a new <code>RtfNilOutputStream</code>.
     */
    public RtfNilOutputStream()
    {           
    }
    
    /**
     * Gets the number of bytes that were written.
     * 
     * @return The number of bytes that were written.
     */
    public long getSize()
    {
        return size;
    }
    
    /**
     * Write an int. The size is incremented, but the actual data is thrown away.
     */
    public void write(int b)
    {
        size++;
    }
    
    /**
     * Write a <code>byte[]</code>. The size is incremented, but the actual data is thrown away.
     */
    public void write(byte[] b, int off, int len)
    {
        if (b == null) {
            throw new NullPointerException();
        } else if ((off < 0) || (off > b.length) || (len < 0) ||
               ((off + len) > b.length) || ((off + len) < 0)) {
            throw new IndexOutOfBoundsException();
        }
        size += len;
    }
}