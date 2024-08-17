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

import com.sun.pdfview.PDFObject;
import com.sun.pdfview.PDFParseException;
import com.sun.pdfview.PDFStringUtil;

/**
 * A decrypter decrypts streams and strings in a PDF document. {@link
 * #decryptBuffer(String, PDFObject, ByteBuffer)} } should be used for decoding
 * streams, and {@link #decryptString(int, int, String)} for string values in
 * the PDF. It is possible for strings and streams to be encrypted with
 * different mechanisms, so the appropriate method must alwayus be used.
 *
 * @see "PDFReference 1.7, Section 3.5 Encryption"
 * @author Luke Kirby
 */
public interface PDFDecrypter {

    /**
     * Decrypt a buffer of data
     * @param cryptFilterName the name of the crypt filter, if V4
     *  encryption is being used, where individual crypt filters may
     *  be specified for individual streams. If encryption is not using
     *  V4 encryption (indicated by V=4 in the Encrypt dictionary) then
     *  this must be null. Null may also be specified with V4 encryption
     *  to indicate that the default filter should be used.
     * @param streamObj the object whose stream is being decrypted. The
     *  containing object's number and generation contribute to the key used for
     *  stream encrypted with the document's default encryption, so this is
     *  typically required. Should be null only if a cryptFilterName is
     *  specified, as objects with specific stream filters use the general
     *  document key, rather than a stream-specific key.
     * @param streamBuf the buffer to decrypt
     * @return a buffer containing the decrypted stream, positioned at its
     *  beginning; will only be the same buffer as streamBuf if the identity
     *  decrypter is being used
     * @throws PDFParseException if the named crypt filter does not exist, or
     *  if a crypt filter is named when named crypt filters are not supported.
     *  Problems due to incorrect passwords are revealed prior to this point.
     */
    public ByteBuffer decryptBuffer(
            String cryptFilterName,
            PDFObject streamObj,
            ByteBuffer streamBuf)
            throws PDFParseException;

    /**
     * Decrypt a {@link PDFStringUtil basic string}.
     * @param objNum the object number of the containing object
     * @param objGen the generation number of the containing object
     * @param inputBasicString the string to be decrypted
     * @return the decrypted string
     * @throws PDFParseException if the named crypt filter does not exist, or
     *  if a crypt filter is named when named crypt filters are not supported.
     *  Problems due to incorrect passwords are revealed prior to this point.
     */
    public String decryptString(int objNum, int objGen, String inputBasicString)
            throws PDFParseException;

    /**
     * Determine whether the password known by the decrypter indicates that
     * the user is the owner of the document. Can be used, in conjunction
     * with {@link #isEncryptionPresent()} to determine whether any
     * permissions apply.
     * @return whether owner authentication is being used to decrypt the
     *  document
     */
    public boolean isOwnerAuthorised();

    /**
     * Determine whether this actually applies a decryption other than
     * identity decryption.
     * @return whether encryption is present
     */
    public boolean isEncryptionPresent();

    /**
     * Determines whether decryption applies for a given crypt filter name
     * @param cryptFilterName the crypt filter name
     * @return whether the given crypt filter decrypts or not
     */
    boolean isEncryptionPresent(String cryptFilterName);
}
