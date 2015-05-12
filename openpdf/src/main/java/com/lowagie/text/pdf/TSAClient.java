/*
 * $Id: TSAClient.java 3959 2009-06-09 08:31:05Z blowagie $
 *
 * Copyright 2009 Martin Brunecky
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
 * the Initial Developer are Copyright (C) 1999-2005 by Bruno Lowagie.
 * All Rights Reserved.
 * Co-Developer of the code is Paulo Soares. Portions created by the Co-Developer
 * are Copyright (C) 2009 by Martin Brunecky. All Rights Reserved.
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

/**
 * Time Stamp Authority client (caller) interface.
 * <p>
 * Interface used by the PdfPKCS7 digital signature builder to call
 * Time Stamp Authority providing RFC 3161 compliant time stamp token.
 * @author Martin Brunecky, 07/17/2007
 * @since	2.1.6
 */
public interface TSAClient {
    /**
     * Get the time stamp token size estimate.
     * Implementation must return value large enough to accomodate the entire token
     * returned by getTimeStampToken() _prior_ to actual getTimeStampToken() call.
     * @return	an estimate of the token size
     */
    public int getTokenSizeEstimate();
    
    /**
     * Get RFC 3161 timeStampToken.
     * Method may return null indicating that timestamp should be skipped.
     * @param caller PdfPKCS7 - calling PdfPKCS7 instance (in case caller needs it)
     * @param imprint byte[] - data imprint to be time-stamped
     * @return byte[] - encoded, TSA signed data of the timeStampToken
     * @throws Exception - TSA request failed
     */
    public byte[] getTimeStampToken(PdfPKCS7 caller, byte[] imprint) throws Exception;
    
}