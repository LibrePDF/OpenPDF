/*
 * $Id: RtfProtectionSetting.java 3580 2008-08-06 15:52:00Z howard_s $
 *
 * Copyright 2008 by Howard Shank
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

package com.lowagie.text.rtf.document;

import java.io.IOException;
import java.io.OutputStream;

import com.lowagie.text.DocWriter;
import com.lowagie.text.rtf.RtfElement;


/**
 * The RtfProtectionSetting handles document protection elements 
 * 
 * @version $Id: RtfProtectionSetting.java 3580 2008-08-06 15:52:00Z howard_s $
 * @author Howard Shank (hgshank@yahoo.com)
 * @since 2.1.1 
 */
public class RtfProtectionSetting extends RtfElement {
	/**
	 * Constant for Form protection controlword
	 * Mutually exclusive
	 * @see com.lowagie.text.rtf.document.RtfProtectionSetting#REVPROT
	 * @see com.lowagie.text.rtf.document.RtfProtectionSetting#ANNOTPROT
	 * @see com.lowagie.text.rtf.document.RtfProtectionSetting#READPROT
	 * @since 2.1.1 
	 */
    private static final byte[] FORMPROT = DocWriter.getISOBytes("\\formprot");
	/**
	 * Constant for Revision protection controlword
	 * Mutually exclusive
	 * @see com.lowagie.text.rtf.document.RtfProtectionSetting#FORMPROT
	 * @see com.lowagie.text.rtf.document.RtfProtectionSetting#ANNOTPROT
	 * @see com.lowagie.text.rtf.document.RtfProtectionSetting#READPROT
	 * @since 2.1.1
	 */
    private static final byte[] REVPROT = DocWriter.getISOBytes("\\revprot");
	/**
	 * Constant for Annotation/Comment protection controlword
	 * Mutually exclusive
	 * @see com.lowagie.text.rtf.document.RtfProtectionSetting#FORMPROT
	 * @see com.lowagie.text.rtf.document.RtfProtectionSetting#REVPROT
	 * @see com.lowagie.text.rtf.document.RtfProtectionSetting#READPROT
	 * @since 2.1.1
	 */
    private static final byte[] ANNOTPROT = DocWriter.getISOBytes("\\annotprot");
	/**
	 * Constant for read only rotection controlword
	 * Mutually exclusive - exception, can be combined with ANNOTPROT
	 * for backwards compatibility
	 * @see com.lowagie.text.rtf.document.RtfProtectionSetting#FORMPROT
	 * @see com.lowagie.text.rtf.document.RtfProtectionSetting#REVPROT
	 * @see com.lowagie.text.rtf.document.RtfProtectionSetting#ANNOTPROT
	 * @since 2.1.1
	 */
    private static final byte[] READPROT = DocWriter.getISOBytes("\\readprot");
    
	/**
	 * Constant for protlevel controlword
	 * @since 2.1.1
	 */
    private static final byte[] PROTLEVEL = DocWriter.getISOBytes("\\protlevel");
	/**
	 * Constant for enforceprot controlword
	 * @since 2.1.1 
	 */
    private static final byte[] ENFORCEPROT = DocWriter.getISOBytes("\\enforceprot");
    
	/**
	 * Constant for enforceprot controlword.
	 * Implemented in Microsoft Word 2007.
	 * 
     * @since 2.1.1
	 */
    private static final byte[] READONLYRECOMMENDED = DocWriter.getISOBytes("\\readonlyrecommended");

    /**
     * Constructs a <code>RtfProtectionSetting</code> belonging to a RtfDocument
     * 
     * @param doc The <code>RtfDocument</code> this <code>RtfProtectionSetting</code> belongs to
	 * @since 2.1.1
     */
    public RtfProtectionSetting(RtfDocument doc) {
        super(doc);
    }

    /**
     * Writes the RTF protection control words
	 * @since 2.1.1
     */    
    public void writeContent(final OutputStream result) throws IOException
    {
    }
    
    /**
     * Writes the RTF protection control words
	 * @since 2.1.1
    */    
    public void writeDefinition(final OutputStream result) throws IOException
    {
    	if(document.getDocumentSettings().isDocumentProtected()) {
	    	switch(document.getDocumentSettings().getProtectionLevelRaw()) {
	    	case RtfProtection.LEVEL_FORMPROT:
	    		result.write(FORMPROT);
	    		break;
	    	case RtfProtection.LEVEL_ANNOTPROT:
	    		result.write(ANNOTPROT);
	    		break;
	    	case RtfProtection.LEVEL_REVPROT:
	    		result.write(REVPROT);
	    		break;
	    	case RtfProtection.LEVEL_READPROT:
	    		result.write(ANNOTPROT);
	    		result.write(READPROT);
	    		break;
	    	}
	    	result.write(ENFORCEPROT);	// assumes one of the above protection keywords was output.
	    	result.write((byte)'1');
	    	result.write(PROTLEVEL);
	    	result.write(document.getDocumentSettings().getProtectionLevelBytes());
    	}
    	
    	if(document.getDocumentSettings().getReadOnlyRecommended()) {
	    	result.write(READONLYRECOMMENDED);
	    	result.write(DELIMITER);
    	}
    }
}
