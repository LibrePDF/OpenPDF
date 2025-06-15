/* Copyright 2008 Pirion Systems Pty Ltd, 139 Warry St,
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

package org.openpdf.renderer.decrypt;

import java.io.IOException;

import org.openpdf.renderer.PDFObject;
import org.openpdf.renderer.PDFParseException;

/**
 * Produces a {@link PDFDecrypter} for documents given a (possibly non-existent)
 * Encrypt dictionary. Supports decryption of versions 1, 2 and 4 of the
 * password-based encryption mechanisms as described in PDF Reference version
 * 1.7. This means that it supports RC4 and AES encryption with keys of
 * 40-128 bits; esentially, password-protected documents with compatibility
 * up to Acrobat 8.
 *
 * @See "PDF Reference version 1.7, section 3.5: Encryption"
 * @author Luke Kirby
 */
@Deprecated
public class PDFDecrypterFactory {

    /** The name of the standard Identity CryptFilter */
    public static final String CF_IDENTITY = "Identity";

    /** Default key length for versions where key length is optional */
    private static final int DEFAULT_KEY_LENGTH = 40;

    /**
     * Create a decryptor for a given encryption dictionary. A check is
     * immediately performed that the supplied password decrypts content
     * described by the encryption specification.
     *
     * @param encryptDict the Encrypt dict as found in the document's trailer.
     *  May be null, in which case the {@link IdentityDecrypter} will
     *  be returned.
     * @param documentId the object with key "ID" in the trailer's dictionary.
     *  Should always be present if Encrypt is.
     * @param password the password to use; may be <code>null</code>
     * @return The decryptor that should be used for all encrypted data in the
     *  PDF
     * @throws IOException will typically be a {@link
     *  PDFParseException}, indicating an IO problem, an error
     *  in the structure of the document, or a failure to obtain various ciphers
     *  from the installed JCE providers
     * @throws EncryptionUnsupportedByPlatformException if the encryption
     *  is not supported by the environment in which the code is executing
     * @throws EncryptionUnsupportedByProductException if PDFRenderer does
     *  not currently support the specified encryption
     * @throws PDFAuthenticationFailureException if the supplied password
     *  was not able to 
     */
    public static PDFDecrypter createDecryptor
            (PDFObject encryptDict, PDFObject documentId, PDFPassword password)
            throws
            IOException,
            EncryptionUnsupportedByPlatformException,
            EncryptionUnsupportedByProductException,
            PDFAuthenticationFailureException {

            return IdentityDecrypter.getInstance();

    }



    /**
     * @param encryptDict the Encrypt dict as found in the document's trailer.
     * @return true if the Filter exist in the current dictionary
     */
    public static boolean isFilterExist(PDFObject encryptDict) {
        if (encryptDict != null) {
            try {
                PDFObject filter = encryptDict.getDictRef("Filter");
                return filter != null;
            } catch (IOException e) {
            }
        }
        return false;
    }

}