/*
 * $Id:RtfAddableElement.java 3126 2008-02-07 20:30:46Z hallm $
 *
 * Copyright 2001, 2002, 2003, 2004 by Mark Hall
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
 * Co-Developer of the code is Mark Hall. Portions created by the Co-Developer are
 * Copyright (C) 2006 by Mark Hall. All Rights Reserved
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

package com.lowagie.text.rtf;

import java.io.IOException;
import java.io.OutputStream;

import com.lowagie.text.Chunk;
import com.lowagie.text.DocWriter;
import com.lowagie.text.Font;
import com.lowagie.text.rtf.document.RtfDocument;

/**
 * The RtfAddableElement is the superclass for all rtf specific elements
 * that need to be added to an iText document. It is an extension of Chunk
 * and it also implements RtfBasicElement. It is an abstract class thus it
 * cannot be instantiated itself and has to be subclassed to be used.
 * 
 * @version $Id:RtfAddableElement.java 3126 2008-02-07 20:30:46Z hallm $
 * @author Mark Hall (Mark.Hall@mail.room3b.eu)
 * @author Thomas Bickel (tmb99@inode.at)
 */
public abstract class RtfAddableElement extends Chunk implements RtfBasicElement {

	/**
	 * The RtfDocument this RtfAddableElement belongs to.
	 */
	protected RtfDocument doc = null;
	/**
	 * Whether this RtfAddableElement is contained in a table.
	 */
	protected boolean inTable = false;
	/**
	 * Whether this RtfAddableElement is contained in a header.
	 */
	protected boolean inHeader = false;
	
	/**
	 * Constructs a new RtfAddableElement. The Chunk content is
	 * set to an empty string and the font to the default Font().
	 */
	public RtfAddableElement() {
		super("", new Font());
	}

	/**
     * Writes the element content to the given output stream.
	 */
    public abstract void writeContent(OutputStream out) throws IOException;
	
	/**
	 * Sets the RtfDocument this RtfAddableElement belongs to.
	 */
	public void setRtfDocument(RtfDocument doc) {
		this.doc = doc;
	}

	/**
	 * Sets whether this RtfAddableElement is contained in a table.
	 */
	public void setInTable(boolean inTable) {
		this.inTable = inTable;
	}

	/**
	 * Sets whether this RtfAddableElement is contained in a header/footer.
	 */
	public void setInHeader(boolean inHeader) {
		this.inHeader = inHeader;
	}

    /**
     * Transforms an integer into its String representation and then returns the bytes
     * of that string.
     *
     * @param i The integer to convert
     * @return A byte array representing the integer
     */
    public byte[] intToByteArray(int i) {
        return DocWriter.getISOBytes(Integer.toString(i));
    }
    
    /**
     *  RtfAddableElement subclasses are never assumed to be empty.
     */
    public boolean isEmpty() {
        return false;
    }
}
