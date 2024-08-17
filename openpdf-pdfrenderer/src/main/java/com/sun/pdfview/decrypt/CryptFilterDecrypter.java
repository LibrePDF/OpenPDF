/*
 * Copyright 2008 Pirion Systems Pty Ltd, 139 Warry St,
 * Fortitude Valley, Queensland, Australia
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package com.sun.pdfview.decrypt;

import java.nio.ByteBuffer;
import java.util.Map;

import com.sun.pdfview.PDFObject;
import com.sun.pdfview.PDFParseException;

/**
 * Implements Version 4 standard decryption, whereby the Encrypt dictionary
 * contains a list of named 'crypt filters', each of which is the equivalent
 * of a {@link PDFDecrypter}. In addition to this list of crypt filters,
 * the name of the filter to use for streams and the default filter to use
 * for strings is specified. Requests to decode a stream with a named
 * decrypter (typically Identity) instead of the default decrypter
 * are honoured. 
 *
 * @author Luke Kirby
 */
public class CryptFilterDecrypter implements PDFDecrypter {

    /** Maps from crypt filter names to their corresponding decrypters */
    private Map<String, PDFDecrypter> decrypters;
    /** The default decrypter for stream content */
    private PDFDecrypter defaultStreamDecrypter;
    /** The default decrypter for string content */
    private PDFDecrypter defaultStringDecrypter;

    /**
     * Class constructor
     * @param decrypters a map of crypt filter names to their corresponding
     *  decrypters. Must already contain the Identity filter.
     * @param defaultStreamCryptName the crypt filter name of the default
     *  stream decrypter
     * @param defaultStringCryptName the crypt filter name of the default
     * string decrypter
     * @throws PDFParseException if one of the named defaults is not
     *  present in decrypters
     */
    public CryptFilterDecrypter(
            Map<String, PDFDecrypter> decrypters,
            String defaultStreamCryptName,
            String defaultStringCryptName)
            throws PDFParseException {

        this.decrypters = decrypters;
        assert this.decrypters.containsKey("Identity") :
                "Crypt Filter map does not contain required Identity filter";
        defaultStreamDecrypter = this.decrypters.get(defaultStreamCryptName);
        if (defaultStreamDecrypter == null) {
            throw new PDFParseException(
                    "Unknown crypt filter specified as default for streams: " +
                            defaultStreamCryptName);
        }
        defaultStringDecrypter = this.decrypters.get(defaultStringCryptName);
        if (defaultStringDecrypter == null) {
            throw new PDFParseException(
                    "Unknown crypt filter specified as default for strings: " +
                            defaultStringCryptName);
        }
    }

    @Override
	public ByteBuffer decryptBuffer(
            String cryptFilterName, PDFObject streamObj, ByteBuffer streamBuf)
            throws PDFParseException {
        final PDFDecrypter decrypter;
        if (cryptFilterName == null) {
            decrypter = defaultStreamDecrypter;
        } else {
            decrypter = decrypters.get(cryptFilterName);
            if (decrypter == null) {
                throw new PDFParseException("Unknown CryptFilter: " +
                        cryptFilterName);
            }
        }
        return decrypter.decryptBuffer(
                // elide the filter name to prevent V2 decrypters from
                // complaining about a crypt filter name
                null,
                // if there's a specific crypt filter being used then objNum
                // and objGen shouldn't contribute to the key, so we
                // should make sure that no streamObj makes its way through
                cryptFilterName != null ? null : streamObj,
                streamBuf);
    }

    @Override
	public String decryptString(int objNum, int objGen, String inputBasicString)
            throws PDFParseException {
        return defaultStringDecrypter.decryptString(objNum, objGen, inputBasicString);
    }

    @Override
	public boolean isEncryptionPresent() {
        for (final PDFDecrypter decrypter : decrypters.values()) {
            if (decrypter.isEncryptionPresent()) {
                return true;
            }
        }
        return false;
    }

    @Override
	public boolean isEncryptionPresent(String cryptFilterName) {
        PDFDecrypter decrypter = decrypters.get(cryptFilterName);
        return decrypter != null && decrypter.isEncryptionPresent(cryptFilterName);
    }

    @Override
	public boolean isOwnerAuthorised() {
        for (final PDFDecrypter decrypter : decrypters.values()) {
            if (decrypter.isOwnerAuthorised()) {
                return true;
            }
        }
        return false;
    }

}