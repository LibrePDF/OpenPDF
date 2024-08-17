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

package com.sun.pdfview.decrypt;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import com.sun.pdfview.PDFObject;
import com.sun.pdfview.PDFParseException;
import com.sun.pdfview.PDFStringUtil;

/**
 * Standard simple decrypter for versions 1, 2 and 4 of the Standard
 * password-based decryption mechanisms, as described in section 3.5 of
 * the PDF Reference version 1.7.
 *
 * @author Luke Kirby
 */
public class StandardDecrypter implements PDFDecrypter {

    /**
     * Extra salt to add to AES-based decryption keys, as per PDF Reference 1.7
     */
    private static final byte[] AESV2_SALT = {'s', 'A', 'l', 'T'};

    /**
     * Describes an encryption algorithm to be used, declaring not only the
     * cipher type, but also key generation techniques
     */
    public enum EncryptionAlgorithm {
        RC4, AESV2;

        boolean isRC4() {
            return this == RC4;
        }

        boolean isAES() {
            return this == AESV2;
        }

    }

    /**
     * Padding used to bring passwords up to 32 bytes, as specified by the
     * first step of Algorithm 3.2 in the PDF Reference version 1.7.
     */
    private final static byte[] PW_PADDING = new byte[]{
            0x28, (byte) 0xBF, 0x4E, 0x5E, 0x4E, 0x75, (byte) 0x8A, 0x41,
            0x64, 0x00, 0x4E, 0x56, (byte) 0xFF, (byte) 0xFA, 0x01, 0x08,
            0x2E, 0x2E, 0x00, (byte) 0xB6, (byte) 0xD0, 0x68, 0x3E, (byte) 0x80,
            0x2F, 0x0C, (byte) 0xA9, (byte) 0xFE, 0x64, 0x53, 0x69, 0x7A
    };

    /**
     * The specification of the RC4 cipher for JCE interactions
     */
    private static final String CIPHER_RC4 = "RC4";
    /**
     * The key type for RC4 keys
     */
    private static final String KEY_RC4 = "RC4";

    /**
     * The specification of the AES cipher for JCE interactions. As per the
     * spec, cipher-block chanining (CBC) mode and PKCS5 padding are used
     */
    private static final String CIPHER_AES = "AES/CBC/PKCS5Padding";
    /**
     * The key type for AES keys
     */
    private static final String KEY_AES = "AES";

    /**
     * Whether the owner password was specified
     */
    private boolean ownerAuthorised = false;

    /**
     * The general encryption key; may be mutated to form individual
     * stream/string encryption keys
     */
    private byte[] generalKeyBytes;

    /**
     * The encryption algorithm being employed
     */
    private EncryptionAlgorithm encryptionAlgorithm;

    /**
     * Class constructor
     *
     * @param encryptionAlgorithm the algorithm used for encryption
     * @param documentId the contents of the ID entry of the document's trailer
     * dictionary; can be null, but according to the spec, shouldn't be. Is
     * expected to be an array of two byte sequences.
     * @param keyBitLength the length of the key in bits; should be a multiple
     * of 8 between 40 and 128
     * @param revision the revision of the Standard encryption security handler
     * being employed. Should be 2, 3 or 4.
     * @param oValue the value of the O entry from the Encrypt dictionary
     * @param uValue the value of the U entry from the Encrypt dictionary
     * @param pValue the value of the P entry from the Encrypt dictionary
     * @param encryptMetadata whether metadata is being encrypted, as identified
     * by the Encrypt dict (with default true if not explicitly identified)
     * @param password the password; not null
     * @throws IOException if there's a problem reading the file
     * @throws EncryptionUnsupportedByPlatformException if the encryption is not
     * supported by the environment in which the code is executing
     * @throws EncryptionUnsupportedByProductException if PDFRenderer does not
     * currently support the specified encryption
     */
    public StandardDecrypter(
            EncryptionAlgorithm encryptionAlgorithm,
            PDFObject documentId, int keyBitLength,
            int revision, byte[] oValue, byte[] uValue, int pValue,
            boolean encryptMetadata, PDFPassword password)
            throws
            IOException,
            EncryptionUnsupportedByProductException,
            EncryptionUnsupportedByPlatformException {

        this.encryptionAlgorithm = encryptionAlgorithm;

        // The spec (sensibly) demands that the documentId be present,
        // but we'll play it safe
        final byte[] firstDocIdValue;
        if (documentId == null) {
            firstDocIdValue = null;
        } else {
            firstDocIdValue = documentId.getAt(0).getStream();
        }

        testJceAvailability(keyBitLength);

        try {
            final List<byte[]> passwordBytePossibilities =
                    password.getPasswordBytes(false);
            for (int i = 0;
                    this.generalKeyBytes == null && i < passwordBytePossibilities.size();
                    ++i) {
                final byte[] passwordBytes = passwordBytePossibilities.get(i);
                this.generalKeyBytes = checkOwnerPassword(
                        passwordBytes, firstDocIdValue, keyBitLength,
                        revision, oValue, uValue, pValue, encryptMetadata);
                if (this.generalKeyBytes != null) {
                    // looks like the password was the owner password!
                    this.ownerAuthorised = true;
                } else {
                    // try it as the user password
                    this.generalKeyBytes = checkUserPassword(
                            passwordBytes, firstDocIdValue, keyBitLength,
                            revision, oValue, uValue, pValue, encryptMetadata);

                }
            }
        } catch (GeneralSecurityException e) {
            // Unexpected, as our test of JCE availability should have caught
            // problems with cipher availability.
            // It may well be a problem with document content?
            throw new PDFParseException("Unable to check passwords: " +
                    e.getMessage(), e);
        }

        if (this.generalKeyBytes == null) {
            throw new PDFAuthenticationFailureException(
                    "Password failed authentication for both " +
                            "owner and user password");
        }

    }

    @Override
	public ByteBuffer decryptBuffer(
            String cryptFilterName, PDFObject streamObj, ByteBuffer streamBuf)
            throws PDFParseException {

        if (cryptFilterName != null) {
            throw new PDFParseException(
                    "This encryption version does not support Crypt filters");
        }

        if (streamObj != null) {
            checkNums(streamObj.getObjNum(), streamObj.getObjGen());
        }

        final byte[] decryptionKeyBytes;
        if (streamObj == null) {
            // lack of a stream object indicates the unsalted key should be
            // used
            decryptionKeyBytes = getUnsaltedDecryptionKey();
        } else {
            decryptionKeyBytes = getObjectSaltedDecryptionKey(
                    streamObj.getObjNum(), streamObj.getObjGen());
        }
        return decryptBuffer(streamBuf, decryptionKeyBytes);
    }

    @Override
	public String decryptString(int objNum, int objGen, String inputBasicString)
            throws PDFParseException {
        final byte[] crypted = PDFStringUtil.asBytes(inputBasicString);
        final byte[] decryptionKey = getObjectSaltedDecryptionKey(objNum, objGen);
        final ByteBuffer decrypted = decryptBuffer(ByteBuffer.wrap(crypted), decryptionKey);
        return PDFStringUtil.asBasicString(decrypted.array(), decrypted.arrayOffset(), decrypted.limit());
    }

    @Override
	public boolean isOwnerAuthorised() {
        return this.ownerAuthorised;
    }

    @Override
	public boolean isEncryptionPresent() {
        return true;
    }
    
    @Override
	public boolean isEncryptionPresent(String cryptFilterName) {
        return true;
    }

    /**
     * Test that the platform (i.e., the JCE) can offer us all of the ciphers at
     * the key length we need for content decryption. This shouldn't be a
     * problem on the Java 5 platform unless a particularly restrictive policy
     * file is in place. Calling this on construction should avoid problems like
     * these being exposed as PDFParseExceptions as they're used during
     * decryption and key establishment.
     *
     * @param keyBitLength the length of the content key, in bits
     * @throws EncryptionUnsupportedByPlatformException if the platform does not
     * support the required ciphers and key lengths
     * @throws PDFParseException if there's an internal error while testing
     * cipher availability
     */
    private void testJceAvailability(int keyBitLength)
            throws
            EncryptionUnsupportedByPlatformException, PDFParseException {

        // we need to supply a little buffer for AES, which will look
        // for an initialisation vector of 16 bytes
        final byte[] junkBuffer = new byte[16];
        Arrays.fill(junkBuffer, (byte) 0xAE);
        // test using the longer key length for salted content so that
        // we can check for maximum key length problems
        final byte[] junkKey =
                new byte[getSaltedContentKeyByteLength(keyBitLength / 8)];
        Arrays.fill(junkKey, (byte) 0xAE);

        try {
            createAndInitialiseContentCipher(
                    ByteBuffer.wrap(junkBuffer),
                    junkKey);
        } catch (PDFParseException e) {
            throw new PDFParseException("Internal error; " +
                    "failed to produce test cipher: " + e.getMessage());
        } catch (NoSuchAlgorithmException e) {
            throw new EncryptionUnsupportedByPlatformException(
                    "JCE does not offer required cipher", e);
        } catch (NoSuchPaddingException e) {
            throw new EncryptionUnsupportedByPlatformException(
                    "JCE does not offer required padding", e);
        } catch (InvalidKeyException e) {
            throw new EncryptionUnsupportedByPlatformException(
                    "JCE does accept key size of " +
                            (getSaltedContentKeyByteLength() * 8) +
                            " bits- could it be a policy restriction?", e);
        } catch (InvalidAlgorithmParameterException e) {
            throw new EncryptionUnsupportedByPlatformException(
                    "JCE did not accept cipher parameter", e);
        }

        try {
            createMD5Digest();
        } catch (NoSuchAlgorithmException e) {
            throw new EncryptionUnsupportedByPlatformException(
                    "No MD5 digest available from JCE", e);
        }

        if (this.encryptionAlgorithm != EncryptionAlgorithm.RC4) {
            // we still need RC4 for U and O value checks. Check again!
            final Cipher rc4;
            try {
                rc4 = createRC4Cipher();
            } catch (GeneralSecurityException e) {
                throw new EncryptionUnsupportedByPlatformException(
                        "JCE did not offer RC4 cipher", e);
            }
            // 40 byte key is used for base U and O ciphers
            final byte[] rc4JunkKey = new byte[5];
            Arrays.fill(junkKey, (byte) 0xAE);
            try {
                initDecryption(rc4, createRC4Key(rc4JunkKey));
            } catch (InvalidKeyException ex) {
                throw new EncryptionUnsupportedByPlatformException(
                        "JCE did not accept 40-bit RC4 key; " +
                                "policy problem?",
                        ex);
            }
        }
    }

    /**
     * Decrypt a buffer
     *
     * @param encrypted the encrypted content
     * @param decryptionKeyBytes the key to use for decryption
     * @return a freshly allocated buffer containing the decrypted content
     * @throws PDFParseException if there's a problem decrypting the content
     */
    private ByteBuffer decryptBuffer(
            ByteBuffer encrypted, byte[] decryptionKeyBytes)
            throws PDFParseException {

        final Cipher cipher;
        try {
            cipher = createAndInitialiseContentCipher(
                    encrypted, decryptionKeyBytes);
        } catch (GeneralSecurityException e) {
            // we should have caught this earlier in testCipherAvailability
            throw new PDFParseException(
                    "Unable to create cipher due to platform limitation: " +
                            e.getMessage(), e);
        }

        try {
            // the decrypted content will never be more than the encrypted
            // content. Thanks to padding, this buffer will be at most 16
            // bytes bigger than the encrypted content
            final ByteBuffer decryptedBuf =
                    ByteBuffer.allocate(encrypted.remaining());
            cipher.doFinal(encrypted, decryptedBuf);
            decryptedBuf.flip();
            return decryptedBuf;
        } catch (GeneralSecurityException e) {
            throw new PDFParseException(
                    "Could not decrypt: " + e.getMessage(), e);
        }
    }

    /**
     * Setup the cipher for decryption
     *
     * @param encrypted the encrypted content; required by AES encryption so
     * that the initialisation vector can be established
     * @param decryptionKeyBytes the bytes for the decryption key
     * @return a content decryption cypher, ready to accept input
     * @throws PDFParseException if the encrypted buffer is malformed or on an
     * internal error
     * @throws NoSuchAlgorithmException if the cipher algorithm is not supported
     * by the platform
     * @throws NoSuchPaddingException if the cipher padding is not supported by
     * the platform
     * @throws InvalidKeyException if the key is invalid according to the
     * cipher, or too long
     * @throws InvalidAlgorithmParameterException if the cipher parameters are
     * bad
     */
    private Cipher createAndInitialiseContentCipher(
            ByteBuffer encrypted,
            byte[] decryptionKeyBytes)
            throws
            PDFParseException,
            NoSuchAlgorithmException,
            NoSuchPaddingException,
            InvalidKeyException,
            InvalidAlgorithmParameterException {

        final Cipher cipher;
        if (this.encryptionAlgorithm.isRC4()) {
            cipher = Cipher.getInstance(CIPHER_RC4);
            cipher.init(Cipher.DECRYPT_MODE, createRC4Key(decryptionKeyBytes));
        } else if (this.encryptionAlgorithm.isAES()) {
            cipher = createAESCipher();
            final byte[] initialisationVector = new byte[16];
            if (encrypted.remaining() >= initialisationVector.length) {
                encrypted.get(initialisationVector);
            } else {
                throw new PDFParseException(
                        "AES encrypted stream too short - " +
                                "no room for initialisation vector");
            }

            final SecretKeySpec aesKey =
                    new SecretKeySpec(decryptionKeyBytes, KEY_AES);
            final IvParameterSpec aesIv =
                    new IvParameterSpec(initialisationVector);
            cipher.init(Cipher.DECRYPT_MODE, aesKey, aesIv);
        } else {
            throw new PDFParseException(
                    "Internal error - unhandled cipher type: " +
                            this.encryptionAlgorithm);
        }
        return cipher;
    }

    /**
     * Get the unsalted content decryption key, used for streams with specific
     * crypt filters, which aren't specific to particular objects
     *
     * @return the general key
     */
    private byte[] getUnsaltedDecryptionKey() {
        return this.generalKeyBytes;
    }

    /**
     * Get a decryption key salted with an object number and object generation,
     * for use when decrypting a string or stream within an object numbered so
     *
     * @param objNum the object number
     * @param objGen the object generation
     * @return the key to be used for decrypting data associated with the object
     *         numbered so
     * @throws PDFParseException if the MD5 digest is not available
     */
    private byte[] getObjectSaltedDecryptionKey(int objNum, int objGen)
            throws PDFParseException {

        byte[] decryptionKeyBytes;
        final MessageDigest md5;
        try {
            md5 = createMD5Digest();
        } catch (NoSuchAlgorithmException e) {
            // unexpected, as we will already have tested availability
            throw new PDFParseException("Unable to get MD5 digester", e);
        }
        md5.update(this.generalKeyBytes);
        md5.update((byte) objNum);
        md5.update((byte) (objNum >> 8));
        md5.update((byte) (objNum >> 16));
        md5.update((byte) objGen);
        md5.update((byte) (objGen >> 8));
        if (this.encryptionAlgorithm == EncryptionAlgorithm.AESV2) {
            md5.update(AESV2_SALT);
        }
        final byte[] hash = md5.digest();
        final int keyLen = getSaltedContentKeyByteLength();
        decryptionKeyBytes = new byte[keyLen];
        System.arraycopy(hash, 0, decryptionKeyBytes, 0, keyLen);
        return decryptionKeyBytes;
    }

    /**
     * Get the length of a salted key
     *
     * @return length in bytes
     */
    private int getSaltedContentKeyByteLength() {
        return getSaltedContentKeyByteLength(this.generalKeyBytes.length);
    }

    /**
     * Get the length of salted keys, in bytes. Unsalted keys will be the same
     * length as {@link #generalKeyBytes}
     *
     * @param generalKeyByteLength the length of the general key, in bytes
     * @return byte length of salted keys
     */
    private int getSaltedContentKeyByteLength(int generalKeyByteLength) {
        return Math.min(generalKeyByteLength + 5, 16);
    }

    /**
     * Check that object number and object generations are well-formed. It is
     * possible for some {@link PDFObject}s to have uninitialised object numbers
     * and generations, but such objects should not required decryption
     *
     * @param objNum the object number
     * @param objGen the object generation
     * @throws PDFParseException if the object numbering indicates that they
     * aren't true object numbers
     */
    private void checkNums(int objNum, int objGen)
            throws PDFParseException {
        if (objNum < 0) {
            throw new PDFParseException(
                    "Internal error: Object has bogus object number");
        } else if (objGen < 0) {
            throw new PDFParseException(
                    "Internal error: Object has bogus generation number");
        }
    }

    /**
     * Calculate what the U value should consist of given a particular key and
     * document configuration. Correponds to Algorithms 3.4 and 3.5 of the
     * PDF Reference version 1.7
     *
     * @param generalKey the general encryption key
     * @param firstDocIdValue the value of the first element in the document's
     * ID entry in the trailer dictionary
     * @param revision the revision of the security handler
     * @return the U value for the given configuration
     * @throws GeneralSecurityException if there's an error getting required
     * ciphers, etc. (unexpected, since a check for algorithm availability is
     * performed on construction)
     * @throws EncryptionUnsupportedByProductException if the revision is not
     * supported
     */
    private byte[] calculateUValue(
            byte[] generalKey, byte[] firstDocIdValue, int revision)
            throws
            GeneralSecurityException,
            EncryptionUnsupportedByProductException {

        if (revision == 2) {

            // Algorithm 3.4: Computing the encryption dictionaryâs U (user
            // password) value (Revision 2)

            // Step 1 is provided to us as the parameter generalKey:
            //  Create an encryption key based on the user password string, as
            //  described in Algorithm 3.2

            // Step 2: Encrypt the 32-byte padding string shown in step 1 of
            // Algorithm 3.2, using an RC4 encryption function with the
            // encryption key from the preceding step.

            Cipher rc4 = createRC4Cipher();
            SecretKey key = createRC4Key(generalKey);
            initEncryption(rc4, key);
            return crypt(rc4, PW_PADDING);

        } else if (revision >= 3) {

            // Algorithm 3.5: Computing the encryption dictionaryâs U (user
            // password) value (Revision 3 or greater)

            // Step 1 is provided to us as the parameter generalKey:
            //  Create an encryption key based on the user password string, as
            //  described in Algorithm 3.2

            // Step 2: Initialize the MD5 hash function and pass the 32-byte
            // padding string shown in step 1 of Algorithm 3.2 as input to this
            // function
            MessageDigest md5 = createMD5Digest();
            md5.update(PW_PADDING);

            // Step 3: Pass the first element of the fileâs file identifier
            // array (the value of the ID entry in the documentâs trailer
            // dictionary; see Table 3.13 on page 97) to the hash function and
            // finish the hash. (See implementation note 26 in Appendix H.)
            if (firstDocIdValue != null) {
                md5.update(firstDocIdValue);
            }
            final byte[] hash = md5.digest();

            // Step 4: Encrypt the 16-byte result of the hash, using an RC4
            // encryption function with the encryption key from step 1.
            Cipher rc4 = createRC4Cipher();
            SecretKey key = createRC4Key(generalKey);
            initEncryption(rc4, key);
            final byte[] v = crypt(rc4, hash);

            // Step 5: Do the following 19 times: Take the output from the
            // previous invocation of the RC4 function and pass it as input to
            // a new invocation of the function; use an encryption key generated
            // by taking each byte of the original encryption key (obtained in
            // step 1) and performing an XOR (exclusive or) operation between
            // that byte and the single-byte value of the iteration counter
            // (from 1 to 19).
            rc4shuffle(v, generalKey, rc4);

            // Step 6: Append 16 bytes of arbitrary padding to the output from
            // the final invocation of the RC4 function and store the 32-byte
            // result as the value of the U entry in the encryption dictionary.
            assert v.length == 16;
            final byte[] entryValue = new byte[32];
            System.arraycopy(v, 0, entryValue, 0, v.length);
            System.arraycopy(v, 0, entryValue, 16, v.length);
            return entryValue;

        } else {
            throw new EncryptionUnsupportedByProductException(
                    "Unsupported standard security handler revision " +
                            revision);
        }
    }

    /**
     * Calculate what the O value of the Encrypt dict should look like given a
     * particular configuration. Not used, but useful for reference; this
     * process is reversed to determine whether a given password is the
     * owner password. Corresponds to Algorithm 3.3 of the PDF Reference
     * version 1.7.
     *
     * @see #checkOwnerPassword
     * @param ownerPassword the owner password
     * @param userPassword the user password
     * @param keyBitLength the key length in bits (40-128)
     * @param revision the security handler revision
     * @return the O value entry
     * @throws GeneralSecurityException if ciphers are unavailable or
     *  inappropriately used
     */
    private byte[] calculuateOValue(
            byte[] ownerPassword, byte[] userPassword,
            int keyBitLength, int revision)
            throws GeneralSecurityException {

        // Steps 1-4
        final byte[] rc4KeyBytes =
                getInitialOwnerPasswordKeyBytes(
                        ownerPassword, keyBitLength, revision);
        final Cipher rc4 = createRC4Cipher();
        initEncryption(rc4, createRC4Key(rc4KeyBytes));

        // Step 5: Pad or truncate the user password string as described in step
        // 1 of Algorithm 3.2.
        // Step 6: Encrypt the result of step 5, using an RC4 encryption
        // function with the encryption key obtained in step 4.
        byte[] pwvalue = crypt(rc4, padPassword(userPassword));

        // Step 7: (Revision 3 or greater) Do the following 19 times: Take the
        // output from the previous invocation of the RC4 function and pass it
        // as input to a new invocation of the function; use an encryption key
        // generated by taking each byte of the encryption key obtained in step
        // 4 and performing an XOR (exclusive or) operation between
        if (revision >= 3) {
            rc4shuffle(pwvalue, rc4KeyBytes, rc4);
        }
        assert pwvalue.length == 32;
        return pwvalue;

    }

    /**
     * Check to see whether a given password is the owner password. Corresponds
     * to algorithm 3.6 of PDF Reference version 1.7.
     *
     * @param ownerPassword the suggested owner password (may be null or
     * empty)
     * @param firstDocIdValue the byte stream from the first element of the
     *  value of the ID entry in the trailer dictionary
     * @param keyBitLength the key length in bits
     * @param revision the security handler revision
     * @param oValue the O value from the Encrypt dictionary
     * @param uValue the U value from the Encrypt dictionary
     * @param pValue the P value from the Encrypt dictionary
     * @param encryptMetadata the EncryptMetadata entry from the Encrypt dictionary
     *  (or false if not present or revision &lt;= 3)
     * @return the general/user key bytes if the owner password is currect,
     *  <code>null</code> otherwise
     * @throws GeneralSecurityException if there's a problem with
     * cipher or digest usage; unexpected
     * @throws EncryptionUnsupportedByProductException if PDFRenderer doesn't
     * support the security handler revision
     * @throws PDFParseException if the document is malformed
     */
    private byte[] checkOwnerPassword(
            byte[] ownerPassword, byte[] firstDocIdValue, int keyBitLength,
            int revision, byte[] oValue, byte[] uValue, int pValue,
            boolean encryptMetadata)
            throws
            GeneralSecurityException,
            EncryptionUnsupportedByProductException,
            PDFParseException {

        // Step 1: Compute an encryption key from the supplied password string,
        // as described in steps 1 to 4 of Algorithm 3.3.
        final byte[] rc4KeyBytes =
                getInitialOwnerPasswordKeyBytes(ownerPassword,
                        keyBitLength, revision);
        final Cipher rc4 = createRC4Cipher();
        initDecryption(rc4, createRC4Key(rc4KeyBytes));

        // Step 2:
        final byte[] possibleUserPassword;
        if (revision == 2) {
            // (Revision 2 only) Decrypt the value of the encryption
            // dictionaryâs O entry, using an RC4 encryption function with the
            // encryption key computed in step 1.

            possibleUserPassword = crypt(rc4, oValue);
        } else if (revision >= 3) {
            // (Revision 3 or greater) Do the following 20 times: Decrypt the
            // value of the encryption dictionaryâs O entry (first iteration) or
            // the output from the previous iteration (all subsequent
            // iterations), using an RC4 encryption function with a different
            // encryption key at each iteration. The key is generated by taking
            // the original key (obtained in step 1) and performing an XOR
            // (exclusive or) operation between each byte of the key and the
            // single-byte value of the iteration counter (from 19 to 0).

            // unshuffle the O entry; the unshuffle operation also
            // contains the final decryption with the original key
            possibleUserPassword = new byte[32];
            System.arraycopy(oValue, 0, possibleUserPassword, 0,
                    possibleUserPassword.length);
            rc4unshuffle(rc4, possibleUserPassword, rc4KeyBytes);
        } else {
            throw new EncryptionUnsupportedByProductException(
                    "Unsupported revision: " + revision);
        }

        // Step 3: The result of step 2 purports to be the user password.
        // Authenticate this user password using Algorithm 3.6. If it is
        // correct, the password supplied is the correct owner password.
        return checkUserPassword(
                possibleUserPassword, firstDocIdValue, keyBitLength,
                revision, oValue, uValue, pValue, encryptMetadata);

    }

    /**
     * Establish the key to be used for the generation and validation
     * of the user password via the O entry. Corresponds to steps 1-4 in
     * Algorithm 3.3 of the PDF Reference version 1.7.
     * @param ownerPassword the owner password
     * @param keyBitLength the length of the key in bits
     * @param revision the security handler revision
     * @return the key bytes to use for generation/validation of the O entry
     * @throws GeneralSecurityException if there's a problem wranling ciphers
     */
    private byte[] getInitialOwnerPasswordKeyBytes(
            byte[] ownerPassword, int keyBitLength, int revision)
            throws GeneralSecurityException {

        final MessageDigest md5 = createMD5Digest();

        // Step 1: Pad or truncate the owner password string as described in
        // step 1 of Algorithm 3.2. If there is no owner password, use the user
        // password instead. (See implementation note 27 in Appendix H.)
        // Step 2: Initialize the MD5 hash function and pass the result of step 1 as
        // input to this function.
        md5.update(padPassword(ownerPassword));

        // Step 3.(Revision 3 or greater) Do the following 50 times: Take the
        // output from the previous MD5 hash and pass it as input into a new MD5
        // hash
        final byte[] hash = md5.digest();
        if (revision >= 3) {
            for (int i = 0; i < 50; ++i) {
                md5.update(hash);
                digestTo(md5, hash);
            }
        }

        // Step 4: Create an RC4 encryption key using the first n bytes of
        // the output from the final MD5 hash, where n is always 5 for revision
        // 2 but, for revision 3 or greater, depends on the value of the
        // encryption dictionaryâs Length entry
        final byte[] rc4KeyBytes = new byte[keyBitLength / 8];
        System.arraycopy(hash, 0, rc4KeyBytes, 0, rc4KeyBytes.length);
        return rc4KeyBytes;
    }

    /**
     * Check to see whether a provided user password is correct with respect
     * to an Encrypt dict configuration. Corresponds to algorithm 3.6 of
     * the PDF Reference version 1.7
     * @param userPassword the user password to test; may be null or empty
     * @param firstDocIdValue the byte stream from the first element of the
     *  value of the ID entry in the trailer dictionary
     * @param keyBitLength the length of the key in bits
     * @param revision the security handler revision
     * @param oValue the O value from the Encrypt dictionary
     * @param uValue the U value from the Encrypt dictionary
     * @param pValue the P value from the Encrypt dictionary
     * @param encryptMetadata the EncryptMetadata entry from the Encrypt dictionary
     *  (or false if not present or revision &lt;= 3)
     * @return the general/user encryption key if the user password is correct,
     *  or null if incorrect
     * @throws GeneralSecurityException if there's a problem with
     * cipher or digest usage; unexpected
     * @throws EncryptionUnsupportedByProductException if PDFRenderer doesn't
     * support the security handler revision
     * @throws PDFParseException if the document is improperly constructed
     */
    private byte[] checkUserPassword(
            byte[] userPassword, byte[] firstDocIdValue, int keyBitLength,
            int revision, byte[] oValue, byte[] uValue, int pValue,
            boolean encryptMetadata)
            throws
            GeneralSecurityException,
            EncryptionUnsupportedByProductException,
            PDFParseException {

        // Algorithm 3.6: Authenticating the user password

        // Step 1: Perform all but the last step of Algorithm 3.4 (Revision 2)
        // or Algorithm 3.5 (Revision 3 or greater) using the supplied password
        // string
        //
        // I.e., figure out what the general key would be with the
        // given password
        // Algorithm 3.4/5,Step1:
        // Determine general key based on user password, as per Algorithm 3.2
        final byte[] generalKey = calculateGeneralEncryptionKey(
                userPassword, firstDocIdValue, keyBitLength,
                revision, oValue, pValue, encryptMetadata);
        // Algorithm 3.4/5,RemainingSteps:
        final byte[] calculatedUValue =
                calculateUValue(generalKey, firstDocIdValue, revision);

        // Step 2: If the result of step 1 is equal to the value of the
        // encryption dictionaryâs U entry (comparing on the first 16 bytes in
        // the case of Revision 3 or greater), the password supplied is the
        // correct user password. The key obtained in step 1 (that is, in the
        // first step of Algorithm 3.4 or 3.5) can be used to decrypt the
        // document using Algorithm 3.1 on page 119.
        assert calculatedUValue.length == 32;
        if (uValue.length != calculatedUValue.length) {
            throw new PDFParseException("Improper U entry length; " +
                    "expected 32, is " + uValue.length);
        }
        // Only the first 16 bytes are significant if using revision > 2
        final int numSignificantBytes = revision == 2 ? 32 : 16;
        for (int i = 0; i < numSignificantBytes; ++i) {
            if (uValue[i] != calculatedUValue[i]) {
                return null;
            }
        }
        return generalKey;
    }


    /**
     * Determine what the general encryption key is, given a configuration. This
     * corresponds to Algorithm 3.2 of PDF Reference version 1.7.
     *
     * @param userPassword the desired user password; may be null or empty
     * @param firstDocIdValue the byte stream from the first element of the
     * value of the ID entry in the trailer dictionary
     * @param keyBitLength the length of the key in bits
     * @param revision the security handler revision
     * @param oValue the O value from the Encrypt dictionary
     * @param pValue the P value from the Encrypt dictionary
     * @param encryptMetadata the EncryptMetadata entry from the Encrypt
     * dictionary (or false if not present or revision &lt;= 3)
     * @return the general encryption key
     * @throws GeneralSecurityException if an error occurs when obtaining
     *  and operating ciphers/digests
     */
    private byte[] calculateGeneralEncryptionKey(
            byte[] userPassword, byte[] firstDocIdValue, int keyBitLength,
            int revision, byte[] oValue, int pValue, boolean encryptMetadata)
            throws GeneralSecurityException {

        // Algorithm 3.2: Computing an encryption key

        // Step 1: Pad or truncate the password string to exactly 32 bytes...
        final byte[] paddedPassword = padPassword(userPassword);

        // Step 2: Initialize the MD5 hash function and pass the result of step
        // 1 as input to this function.
        MessageDigest md5 = createMD5Digest();
        md5.reset();
        md5.update(paddedPassword);

        // Step 3: Pass the value of the encryption dictionaryâs O entry to the
        // MD5 hash function. (Algorithm 3.3 shows how the O value is computed.)
        md5.update(oValue);

        // Step 4: Treat the value of the P entry as an unsigned 4-byte integer
        // and pass these bytes to the MD5 hash function, low-order byte first
        md5.update((byte) (pValue & 0xFF));
        md5.update((byte) ((pValue >> 8) & 0xFF));
        md5.update((byte) ((pValue >> 16) & 0xFF));
        md5.update((byte) (pValue >> 24));

        // Step 5: Pass the first element of the fileâs file identifier array
        // (the value of the ID entry in the documentâs trailer dictionary; see
        // Table 3.13 on page 97) to the MD5 hash function. (See implementation
        // note 26 in Appendix H.)
        if (firstDocIdValue != null) {
            md5.update(firstDocIdValue);
        }

        // Step 6: (Revision 4 or greater) If document metadata is not being
        // encrypted, pass 4 bytes with the value 0xFFFFFFFF to the MD5 hash
        // function
        if (revision >= 4 && !encryptMetadata) {
            for (int i = 0; i < 4; ++i) {
                md5.update((byte) 0xFF);
            }
        }

        // Step 7: finish the hash
        byte[] hash = md5.digest();

        final int keyLen = revision == 2 ? 5 : (keyBitLength / 8);
        final byte[] key = new byte[keyLen];

        // Step 8: (Revision 3 or greater) Do the following 50 times: Take the
        // output from the previous MD5 hash and pass the first n bytes of the
        // output as input into a new MD5 hash, where n is the number of bytes
        // of the encryption key as defined by the value of the encryption
        // dictionaryâs Length entry
        if (revision >= 3) {
            for (int i = 0; i < 50; ++i) {
                md5.update(hash, 0, key.length);
                digestTo(md5, hash);
            }
        }

        // Set the encryption key to the first n bytes of the output from the
        // final MD5 hash, where n is always 5 for revision 2 but, for revision
        // 3 or greater, depends on the value of the encryption dictionaryâs
        // Length entry.
        System.arraycopy(hash, 0, key, 0, key.length);
        return key;
    }

    /**
     * Pad a password as per step 1 of Algorithm 3.2 of the PDF Reference
     * version 1.7
     * @param password the password, may be null or empty
     * @return the padded password, always 32 bytes long
     */
    private byte[] padPassword(byte[] password) {

        if (password == null) {
            password = new byte[0];
        }

        // Step 1: Pad or truncate the password string to exactly 32 bytes. If
        // the password string is more than 32 bytes long, use only its first 32
        // bytes; if it is less than 32 bytes long, pad it by appending the
        // required number of additional bytes from the beginning of the
        // following padding string:
        // < 28 BF 4E 5E 4E 75 8A 41 64 00 4E 56 FF FA 01 08
        //   2E 2E 00 B6 D0 68 3E 80 2F 0C A9 FE 64 53 69 7A >
        // That is, if the password string is n bytes long, append the first 32
        // â n bytes of the padding string to the end of the password string. If
        // the password string is empty (zero-length), meaning there is no user
        // password, substitute the entire padding string in its place.

        byte[] padded = new byte[32];
        // limit password to 32 bytes
        final int numContributingPasswordBytes =
                password.length > padded.length ?
                padded.length : password.length;
        System.arraycopy(password, 0, padded, 0, numContributingPasswordBytes);
        // Copy padding
        if (password.length < padded.length) {
            System.arraycopy(PW_PADDING, 0, padded, password.length,
                    padded.length - password.length);
        }
        return padded;
    }

    /**
     * Encrypt some bytes
     *
     * @param cipher the cipher
     * @param input the plaintext
     * @return the crypt text
     * @throws BadPaddingException if there's bad padding
     * @throws IllegalBlockSizeException if the block size is bad
     */
    private byte[] crypt(Cipher cipher, byte[] input)
            throws IllegalBlockSizeException, BadPaddingException {
        return cipher.doFinal(input);
    }

    /**
     * Initialise a cipher for encryption
     *
     * @param cipher the cipher
     * @param key the encryption key
     * @throws InvalidKeyException if the key is invalid for the cipher
     */
    private void initEncryption(Cipher cipher, SecretKey key)
            throws InvalidKeyException {
        cipher.init(Cipher.ENCRYPT_MODE, key);
    }

    /**
     * Shuffle some input using a series of RC4 encryptions with slight
     * mutations of an given key per iteration. Shuffling happens in place.
     * Refer to the documentation of the algorithm steps where this is called.
     *
     * @param shuffle the bytes to be shuffled
     * @param key the original key
     * @param rc4 the cipher to use
     * @throws GeneralSecurityException if there's a problem with cipher
     *  operation
     */
    private void rc4shuffle(byte[] shuffle, byte[] key, Cipher rc4)
            throws GeneralSecurityException {

        final byte[] shuffleKey = new byte[key.length];
        for (int i = 1; i <= 19; ++i) {
            for (int j = 0; j < shuffleKey.length; ++j) {
                shuffleKey[j] = (byte) (key[j] ^ i);
            }
            initEncryption(rc4, createRC4Key(shuffleKey));
            cryptInPlace(rc4, shuffle);
        }
    }

    /**
     * Reverse the {@link #rc4shuffle} operation, and the operation
     * that invariable preceeds it, thereby obtaining an original message
     * @param rc4 the RC4 cipher to use
     * @param shuffle the bytes in which shuffling will take place; unshuffling
     *  happens in place
     * @param key the encryption key
     * @throws GeneralSecurityException if there's a problem with cipher
     *  operation
     */
    private void rc4unshuffle(Cipher rc4, byte[] shuffle, byte[] key)
            throws GeneralSecurityException {

        // there's an extra unshuffle at the end with the original key -
        // this is why we end with i == 0, where the shuffle key will be the key
        final byte[] shuffleKeyBytes = new byte[key.length];
        for (int i = 19; i >= 0; --i) {
            for (int j = 0; j < shuffleKeyBytes.length; ++j) {
                shuffleKeyBytes[j] = (byte) (key[j] ^ i);
            }
            initDecryption(rc4, createRC4Key(shuffleKeyBytes));
            cryptInPlace(rc4, shuffle);
        }
    }

    /**
     * Encrypt/decrypt something in place
     * @param rc4 the cipher to use; must be a stream cipher producing
     *  identical output length to input (e.g., RC4)
     * @param buffer the buffer to read input from and write output to
     * @throws IllegalBlockSizeException if an inappropriate cipher is used
     * @throws ShortBufferException if an inappropriate cipher is used
     * @throws BadPaddingException if an inappropriate cipher is used
     */
    private void cryptInPlace(Cipher rc4, byte[] buffer)
            throws IllegalBlockSizeException, ShortBufferException, BadPaddingException {
        rc4.doFinal(buffer, 0, buffer.length, buffer);
    }

    /**
     * Setup a cipher for decryption
     * @param cipher the cipher
     * @param aKey the cipher key
     * @throws InvalidKeyException if the key is of an unacceptable size or
     *  doesn't belong to the cipher
     */
    private void initDecryption(Cipher cipher, Key aKey)
            throws InvalidKeyException {
        cipher.init(Cipher.DECRYPT_MODE, aKey);
    }

    /**
     * Create a new RC4 cipher. Should always be available for supported
     * platforms.
     * @return the cipher
     * @throws NoSuchAlgorithmException if the RC4 cipher is unavailable
     * @throws NoSuchPaddingException should not happen, as no padding
     *  is specified
     */
    private Cipher createRC4Cipher()
            throws NoSuchAlgorithmException, NoSuchPaddingException {
        return Cipher.getInstance(CIPHER_RC4);
    }

    /**
     * Create a new AES cipher. Should always be available for supported
     * platforms.
     * @return the new cipher
     * @throws NoSuchAlgorithmException if the AES cipher is unavailable
     * @throws NoSuchPaddingException if the required padding is unavailable
     */
    private Cipher createAESCipher()
            throws NoSuchAlgorithmException, NoSuchPaddingException {
        return Cipher.getInstance(CIPHER_AES);
    }

    /**
     * Create an MD5 digest. Should always be available for supported
     * platforms.
     * @return the MD5 digest
     * @throws NoSuchAlgorithmException if the digest is not available
     */
    private MessageDigest createMD5Digest()
            throws NoSuchAlgorithmException {
        return MessageDigest.getInstance("MD5");
    }

    /**
     * Create an RC4 key
     *
     * @param keyBytes the bytes for the key
     * @return the key
     */
    private SecretKeySpec createRC4Key(byte[] keyBytes) {
        return new SecretKeySpec(keyBytes, KEY_RC4);
    }

    /**
     * Hash into an existing byte array
     * @param md5 the MD5 digest
     * @param hash the hash destination
     * @throws GeneralSecurityException if there's a problem hashing; e.g.,
     *  if the buffer is too small
     */
    private void digestTo(MessageDigest md5, byte[] hash)
                throws GeneralSecurityException {
        md5.digest(hash, 0, hash.length);
    }


}