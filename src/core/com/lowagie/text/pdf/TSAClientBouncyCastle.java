/*
 * $Id: TSAClientBouncyCastle.java 4065 2009-09-16 23:09:11Z psoares33 $
 *
 * Copyright 2009 Martin Brunecky, Aiken Sam
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

import java.io.*;
import java.math.*;
import java.net.*;
import com.lowagie.text.error_messages.MessageLocalization;

import org.bouncycastle.asn1.cmp.*;
import org.bouncycastle.asn1.x509.*;
import org.bouncycastle.tsp.*;

import com.lowagie.text.pdf.codec.Base64;

/**
 * Time Stamp Authority Client interface implementation using Bouncy Castle
 * org.bouncycastle.tsp package.
 * <p>
 * Created by Aiken Sam, 2006-11-15, refactored by Martin Brunecky, 07/15/2007
 * for ease of subclassing.
 * </p>
 * @since	2.1.6
 */
public class TSAClientBouncyCastle implements TSAClient {
    /** URL of the Time Stamp Authority */
	protected String tsaURL;
	/** TSA Username */
    protected String tsaUsername;
    /** TSA password */
    protected String tsaPassword;
    /** Estimate of the received time stamp token */
    protected int tokSzEstimate;
    
    /**
     * Creates an instance of a TSAClient that will use BouncyCastle.
     * @param url String - Time Stamp Authority URL (i.e. "http://tsatest1.digistamp.com/TSA")
     */
    public TSAClientBouncyCastle(String url) {
        this(url, null, null, 4096);
    }
    
    /**
     * Creates an instance of a TSAClient that will use BouncyCastle.
     * @param url String - Time Stamp Authority URL (i.e. "http://tsatest1.digistamp.com/TSA")
     * @param username String - user(account) name
     * @param password String - password
     */
    public TSAClientBouncyCastle(String url, String username, String password) {
        this(url, username, password, 4096);
    }
    
    /**
     * Constructor.
     * Note the token size estimate is updated by each call, as the token
     * size is not likely to change (as long as we call the same TSA using
     * the same imprint length).
     * @param url String - Time Stamp Authority URL (i.e. "http://tsatest1.digistamp.com/TSA")
     * @param username String - user(account) name
     * @param password String - password
     * @param tokSzEstimate int - estimated size of received time stamp token (DER encoded)
     */
    public TSAClientBouncyCastle(String url, String username, String password, int tokSzEstimate) {
        this.tsaURL       = url;
        this.tsaUsername  = username;
        this.tsaPassword  = password;
        this.tokSzEstimate = tokSzEstimate;
    }
    
    /**
     * Get the token size estimate.
     * Returned value reflects the result of the last succesfull call, padded
     * @return an estimate of the token size
     */
    public int getTokenSizeEstimate() {
        return tokSzEstimate;
    }
    
    /**
     * Get RFC 3161 timeStampToken.
     * Method may return null indicating that timestamp should be skipped.
     * @param caller PdfPKCS7 - calling PdfPKCS7 instance (in case caller needs it)
     * @param imprint byte[] - data imprint to be time-stamped
     * @return byte[] - encoded, TSA signed data of the timeStampToken
     * @throws Exception - TSA request failed
     * @see com.lowagie.text.pdf.TSAClient#getTimeStampToken(com.lowagie.text.pdf.PdfPKCS7, byte[])
     */
    public byte[] getTimeStampToken(PdfPKCS7 caller, byte[] imprint) throws Exception {
        return getTimeStampToken(imprint);
    }
    
    /**
     * Get timestamp token - Bouncy Castle request encoding / decoding layer
     */
    protected byte[] getTimeStampToken(byte[] imprint) throws Exception {
        byte[] respBytes = null;
        try {
            // Setup the time stamp request
            TimeStampRequestGenerator tsqGenerator = new TimeStampRequestGenerator();
            tsqGenerator.setCertReq(true);
            // tsqGenerator.setReqPolicy("1.3.6.1.4.1.601.10.3.1");
            BigInteger nonce = BigInteger.valueOf(System.currentTimeMillis());
            TimeStampRequest request = tsqGenerator.generate(X509ObjectIdentifiers.id_SHA1.getId() , imprint, nonce);
            byte[] requestBytes = request.getEncoded();
            
            // Call the communications layer
            respBytes = getTSAResponse(requestBytes);
            
            // Handle the TSA response
            TimeStampResponse response = new TimeStampResponse(respBytes);
            
            // validate communication level attributes (RFC 3161 PKIStatus)
            response.validate(request);
            PKIFailureInfo failure = response.getFailInfo();
            int value = (failure == null) ? 0 : failure.intValue();
            if (value != 0) {
                // @todo: Translate value of 15 error codes defined by PKIFailureInfo to string
                throw new Exception(MessageLocalization.getComposedMessage("invalid.tsa.1.response.code.2", tsaURL, String.valueOf(value)));
            }
            // @todo: validate the time stap certificate chain (if we want
            //        assure we do not sign using an invalid timestamp).
            
            // extract just the time stamp token (removes communication status info)
            TimeStampToken  tsToken = response.getTimeStampToken();
            if (tsToken == null) {
                throw new Exception(MessageLocalization.getComposedMessage("tsa.1.failed.to.return.time.stamp.token.2", tsaURL, response.getStatusString()));
            }
            TimeStampTokenInfo info = tsToken.getTimeStampInfo(); // to view details
            byte[] encoded = tsToken.getEncoded();
            long stop = System.currentTimeMillis();
            
            // Update our token size estimate for the next call (padded to be safe)
            this.tokSzEstimate = encoded.length + 32;
            return encoded;
        } catch (Exception e) {
            throw e;
        } catch (Throwable t) {
            throw new Exception(MessageLocalization.getComposedMessage("failed.to.get.tsa.response.from.1", tsaURL), t);
        }
    }
    
    /**
     * Get timestamp token - communications layer
     * @return - byte[] - TSA response, raw bytes (RFC 3161 encoded)
     */
    protected byte[] getTSAResponse(byte[] requestBytes) throws Exception {
        // Setup the TSA connection
        URL url = new URL(tsaURL);
        URLConnection tsaConnection;
        tsaConnection = (URLConnection) url.openConnection();
        
        tsaConnection.setDoInput(true);
        tsaConnection.setDoOutput(true);
        tsaConnection.setUseCaches(false);
        tsaConnection.setRequestProperty("Content-Type", "application/timestamp-query");
        //tsaConnection.setRequestProperty("Content-Transfer-Encoding", "base64");
        tsaConnection.setRequestProperty("Content-Transfer-Encoding", "binary");
        
        if ((tsaUsername != null) && !tsaUsername.equals("") ) {
            String userPassword = tsaUsername + ":" + tsaPassword;
            tsaConnection.setRequestProperty("Authorization", "Basic " +
                new String(Base64.encodeBytes(userPassword.getBytes())));
        }
        OutputStream out = tsaConnection.getOutputStream();
        out.write(requestBytes);
        out.close();
        
        // Get TSA response as a byte array
        InputStream inp = tsaConnection.getInputStream();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int bytesRead = 0;
        while ((bytesRead = inp.read(buffer, 0, buffer.length)) >= 0) {
            baos.write(buffer, 0, bytesRead);
        }
        byte[] respBytes = baos.toByteArray();
        
        String encoding = tsaConnection.getContentEncoding();
        if (encoding != null && encoding.equalsIgnoreCase("base64")) {
            respBytes = Base64.decode(new String(respBytes));
        }
        return respBytes;
    }    
}
