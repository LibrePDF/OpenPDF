/*
 * $Id: JBIG2Image.java 4065 2009-09-16 23:09:11Z psoares33 $
 *
 * Copyright 2009 by Nigel Kerr.
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
 * the Initial Developer are Copyright (C) 1999-2009 by Bruno Lowagie.
 * All Rights Reserved.
 * Co-Developer of the code is Paulo Soares. Portions created by the Co-Developer
 * are Copyright (C) 2000-2009 by Paulo Soares. All Rights Reserved.
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

package com.lowagie.text.pdf.codec;

import com.lowagie.text.ExceptionConverter;
import com.lowagie.text.Image;
import com.lowagie.text.ImgJBIG2;
import com.lowagie.text.pdf.RandomAccessFileOrArray;
import com.lowagie.text.error_messages.MessageLocalization;

/**
 * Support for JBIG2 Images.
 * This class assumes that we are always embedding into a pdf.
 * 
 * @since 2.1.5
 */
public class JBIG2Image {

	/**
	 * Gets a byte array that can be used as a /JBIG2Globals,
	 * or null if not applicable to the given jbig2.
	 * @param	ra	an random access file or array
	 * @return	a byte array
	 */
	public static byte[] getGlobalSegment(RandomAccessFileOrArray ra ) {
		try {
			JBIG2SegmentReader sr = new JBIG2SegmentReader(ra);
			sr.read();
			return sr.getGlobal(true);
		} catch (Exception e) {
	        return null;
	    }
	}
	
	/**
	 * returns an Image representing the given page.
	 * @param ra	the file or array containing the image
	 * @param page	the page number of the image
	 * @return	an Image object
	 */
	public static Image getJbig2Image(RandomAccessFileOrArray ra, int page) {
		if (page < 1)
            throw new IllegalArgumentException(MessageLocalization.getComposedMessage("the.page.number.must.be.gt.eq.1"));
		
		try {
			JBIG2SegmentReader sr = new JBIG2SegmentReader(ra);
			sr.read();
			JBIG2SegmentReader.JBIG2Page p = sr.getPage(page);
			Image img = new ImgJBIG2(p.pageBitmapWidth, p.pageBitmapHeight, p.getData(true), sr.getGlobal(true));
			return img;
		} catch (Exception e) {
	        throw new ExceptionConverter(e);
	    }
	}

	/***
	 * Gets the number of pages in a JBIG2 image.
	 * @param ra	a random acces file array containing a JBIG2 image
	 * @return	the number of pages
	 */
	public static int getNumberOfPages(RandomAccessFileOrArray ra) {
		try {
			JBIG2SegmentReader sr = new JBIG2SegmentReader(ra);
			sr.read();
			return sr.numberOfPages();
		} catch (Exception e) {
	        throw new ExceptionConverter(e);
	    }
    }
	
	
}
