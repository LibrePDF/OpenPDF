/*
 * $Id: PdfEncryption.java 4065 2009-09-16 23:09:11Z psoares33 $
 *
 * Copyright 2001-2006 Paulo Soares
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
 * https://github.com/LibrePDF/OpenPDF
 */

package com.lowagie.text.pdf;

import com.lowagie.text.ExceptionConverter;
import com.lowagie.text.pdf.crypto.ARCFOUREncryption;
import com.lowagie.text.pdf.crypto.IVGenerator;
import com.lowagie.text.error_messages.MessageLocalization;

import java.io.IOException;
import java.io.OutputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.cert.Certificate;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;



/**
 * 
 * @author Paulo Soares (psoares@consiste.pt)
 * @author Kazuya Ujihara
 */
public class PdfEncryption {

    public static final int STANDARD_ENCRYPTION_40 = 2;

    public static final int STANDARD_ENCRYPTION_128 = 3;

    public static final int AES_128 = 4;

    public static final int AES_256_V3 = 6;

    private static final byte[] pad = { (byte) 0x28, (byte) 0xBF, (byte) 0x4E,
            (byte) 0x5E, (byte) 0x4E, (byte) 0x75, (byte) 0x8A, (byte) 0x41,
            (byte) 0x64, (byte) 0x00, (byte) 0x4E, (byte) 0x56, (byte) 0xFF,
            (byte) 0xFA, (byte) 0x01, (byte) 0x08, (byte) 0x2E, (byte) 0x2E,
            (byte) 0x00, (byte) 0xB6, (byte) 0xD0, (byte) 0x68, (byte) 0x3E,
            (byte) 0x80, (byte) 0x2F, (byte) 0x0C, (byte) 0xA9, (byte) 0xFE,
            (byte) 0x64, (byte) 0x53, (byte) 0x69, (byte) 0x7A };

    private static final byte[] salt = { (byte) 0x73, (byte) 0x41, (byte) 0x6c,
            (byte) 0x54 };

    private static final byte[] metadataPad = { (byte) 255, (byte) 255,
            (byte) 255, (byte) 255 };

    /**
     * The encryption key for a particular object/generation
     */
    byte[] key;

    /** The encryption key length for a particular object/generation */
    int keySize;

    /**
     * The global encryption key
     */
    byte[] mkey;

    /**
     * Work area to prepare the object/generation bytes
     */
    byte[] extra = new byte[5];

    /** The message digest algorithm MD5 */
    MessageDigest md5;

    /**
     * The encryption key for the owner
     */
    byte[] ownerKey = new byte[32];

    /**
     * The encryption key for the user
     */
    byte[] userKey = new byte[32];

    /**
     * Additional keys for AES_256_V3
     */
    byte[] oeKey;
    byte[] ueKey;
    byte[] perms;

    /** The public key security handler for certificate encryption */
    protected PdfPublicKeySecurityHandler publicKeyHandler = null;

    int permissions;

    byte[] documentID;

    static long seq = System.currentTimeMillis();

    private int revision;

    private ARCFOUREncryption arcfour = new ARCFOUREncryption();

    /** The generic key length. It may be 40 or 128. */
    private int keyLength;

    private boolean encryptMetadata;
    
    /**
     * Indicates if the encryption is only necessary for embedded files.
     * @since 2.1.3
     */
    private boolean embeddedFilesOnly;

    private int cryptoMode;

    public PdfEncryption() {
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (Exception e) {
            throw new ExceptionConverter(e);
        }
        publicKeyHandler = new PdfPublicKeySecurityHandler();
    }

    public PdfEncryption(PdfEncryption enc) {
        this();
        if (enc.mkey != null)
            mkey = enc.mkey.clone();
        ownerKey = enc.ownerKey.clone();
        userKey = enc.userKey.clone();
        permissions = enc.permissions;
        if (enc.documentID != null)
            documentID = enc.documentID.clone();
        revision = enc.revision;
        keyLength = enc.keyLength;
        encryptMetadata = enc.encryptMetadata;
        embeddedFilesOnly = enc.embeddedFilesOnly;
        publicKeyHandler = enc.publicKeyHandler;
        if (enc.ueKey != null)
            ueKey = enc.ueKey.clone();
        if (enc.oeKey != null)
            oeKey = enc.oeKey.clone();
        if (enc.perms != null)
            perms = enc.perms.clone();
        if (enc.key != null)
            key = enc.key.clone();
        keySize = enc.keySize;
    }

    public void setCryptoMode(int mode, int kl) {
        cryptoMode = mode;
        encryptMetadata = (mode & PdfWriter.DO_NOT_ENCRYPT_METADATA) == 0;
        embeddedFilesOnly = (mode & PdfWriter.EMBEDDED_FILES_ONLY) != 0;
        mode &= PdfWriter.ENCRYPTION_MASK;
        switch (mode) {
        case PdfWriter.STANDARD_ENCRYPTION_40:
            encryptMetadata = true;
            embeddedFilesOnly = false;
            keyLength = 40;
            revision = STANDARD_ENCRYPTION_40;
            break;
        case PdfWriter.STANDARD_ENCRYPTION_128:
            embeddedFilesOnly = false;
            if (kl > 0)
                keyLength = kl;
            else
                keyLength = 128;
            revision = STANDARD_ENCRYPTION_128;
            break;
        case PdfWriter.ENCRYPTION_AES_128:
            keyLength = 128;
            revision = AES_128;
            break;
        case PdfWriter.ENCRYPTION_AES_256_V3:
            keyLength = 256;
            keySize = 32;
            revision = AES_256_V3;
            break;
        default:
            throw new IllegalArgumentException(MessageLocalization.getComposedMessage("no.valid.encryption.mode"));
        }
    }

    public int getCryptoMode() {
        return cryptoMode;
    }

    public boolean isMetadataEncrypted() {
        return encryptMetadata;
    }

    /**
     * Indicates if only the embedded files have to be encrypted.
     * @return    if true only the embedded files will be encrypted
     * @since    2.1.3
     */
    public boolean isEmbeddedFilesOnly() {
        return embeddedFilesOnly;
    }

    /**
     */
    private byte[] padPassword(byte[] userPassword) {
        byte[] userPad = new byte[32];
        if (userPassword == null) {
            System.arraycopy(pad, 0, userPad, 0, 32);
        } else {
            System.arraycopy(userPassword, 0, userPad, 0, Math.min(
                    userPassword.length, 32));
            if (userPassword.length < 32)
                System.arraycopy(pad, 0, userPad, userPassword.length,
                        32 - userPassword.length);
        }

        return userPad;
    }

    /**
     */
    private byte[] computeOwnerKey(byte[] userPad, byte[] ownerPad) {
        byte[] ownerKey = new byte[32];

        byte[] digest = md5.digest(ownerPad);
        if (revision == STANDARD_ENCRYPTION_128 || revision == AES_128) {
            byte[] mkey = new byte[keyLength / 8];
            // only use for the input as many bit as the key consists of
            for (int k = 0; k < 50; ++k)
                System.arraycopy(md5.digest(digest), 0, digest, 0, mkey.length);
            System.arraycopy(userPad, 0, ownerKey, 0, 32);
            for (int i = 0; i < 20; ++i) {
                for (int j = 0; j < mkey.length; ++j)
                    mkey[j] = (byte) (digest[j] ^ i);
                arcfour.prepareARCFOURKey(mkey);
                arcfour.encryptARCFOUR(ownerKey);
            }
        } else {
            arcfour.prepareARCFOURKey(digest, 0, 5);
            arcfour.encryptARCFOUR(userPad, ownerKey);
        }

        return ownerKey;
    }

    /**
     * 
     * ownerKey, documentID must be setup
     */
    private void setupGlobalEncryptionKey(byte[] documentID, byte[] userPad,
                                          byte[] ownerKey, int permissions) {
        this.documentID = documentID;
        this.ownerKey = ownerKey;
        this.permissions = permissions;
        // use variable keylength
        mkey = new byte[keyLength / 8];

        // fixed by ujihara in order to follow PDF reference
        md5.reset();
        md5.update(userPad);
        md5.update(ownerKey);

        byte[] ext = new byte[4];
        ext[0] = (byte) permissions;
        ext[1] = (byte) (permissions >> 8);
        ext[2] = (byte) (permissions >> 16);
        ext[3] = (byte) (permissions >> 24);
        md5.update(ext, 0, 4);
        if (documentID != null)
            md5.update(documentID);
        if (!encryptMetadata)
            md5.update(metadataPad);

        byte[] digest = new byte[mkey.length];
        System.arraycopy(md5.digest(), 0, digest, 0, mkey.length);

        // only use the really needed bits as input for the hash
        if (revision == STANDARD_ENCRYPTION_128 || revision == AES_128) {
            for (int k = 0; k < 50; ++k)
                System.arraycopy(md5.digest(digest), 0, digest, 0, mkey.length);
        }

        System.arraycopy(digest, 0, mkey, 0, mkey.length);
    }

    /**
     * 
     * mkey must be setup
     */
    // use the revision to choose the setup method
    private void setupUserKey() {
        if (revision == STANDARD_ENCRYPTION_128 || revision == AES_128) {
            md5.update(pad);
            byte[] digest = md5.digest(documentID);
            System.arraycopy(digest, 0, userKey, 0, 16);
            for (int k = 16; k < 32; ++k)
                userKey[k] = 0;
            for (int i = 0; i < 20; ++i) {
                for (int j = 0; j < mkey.length; ++j)
                    digest[j] = (byte) (mkey[j] ^ i);
                arcfour.prepareARCFOURKey(digest, 0, mkey.length);
                arcfour.encryptARCFOUR(userKey, 0, 16);
            }
        } else {
            arcfour.prepareARCFOURKey(mkey);
            arcfour.encryptARCFOUR(pad, userKey);
        }
    }

    // gets keylength and revision and uses revision to choose the initial values
    // for permissions
    public void setupAllKeys(byte[] userPassword, byte[] ownerPassword,
                             int permissions) {
        if (ownerPassword == null || ownerPassword.length == 0)
            ownerPassword = md5.digest(createDocumentId());
        permissions |= (revision == STANDARD_ENCRYPTION_128 || revision == AES_128 || revision == AES_256_V3) ? 0xfffff0c0
                : 0xffffffc0;
        permissions &= 0xfffffffc;
        this.permissions = permissions;
        documentID = createDocumentId();
        if (revision < AES_256_V3)
        {
            // PDF reference 3.5.2 Standard Security Handler, Algorithm 3.3-1
            // If there is no owner password, use the user password instead.
            byte[] userPad = padPassword(userPassword);
            byte[] ownerPad = padPassword(ownerPassword);

            this.ownerKey = computeOwnerKey(userPad, ownerPad);
            setupByUserPad(this.documentID, userPad, this.ownerKey, permissions);
        } else {
            try {
                key = IVGenerator.getIV(32);
                keySize = 32;
                computeUAndUeAlg8(userPassword);
                computeOAndOeAlg9(ownerPassword);
                computePermsAlg10(permissions);
            } catch (GeneralSecurityException e) {
                throw new ExceptionConverter(e);
            }
        }
    }

    public static byte[] createDocumentId() {
        MessageDigest md5;
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (Exception e) {
            throw new ExceptionConverter(e);
        }
        long time = System.currentTimeMillis();
        long mem = Runtime.getRuntime().freeMemory();
        String s = time + "+" + mem + "+" + (seq++);
        return md5.digest(s.getBytes());
    }

    /**
     *
     * @param documentID byte array of document id
     * @param userPassword byte array of user password
     * @param ownerKey byte array of owner key
     * @param permissions permissions
     */
    public void setupByUserPassword(byte[] documentID, byte[] userPassword,
                                    byte[] ownerKey, int permissions) {
        setupByUserPad(documentID, padPassword(userPassword), ownerKey,
                permissions);
    }

    /**
     */
    private void setupByUserPad(byte[] documentID, byte[] userPad,
                                byte[] ownerKey, int permissions) {
        setupGlobalEncryptionKey(documentID, userPad, ownerKey, permissions);
        setupUserKey();
    }

    /**
     *
     * @param documentID byte array of document id
     * @param ownerPassword byte array of owner password
     * @param userKey byte array of user key
     * @param ownerKey byte array of owner key
     * @param permissions permissions
     */
    public void setupByOwnerPassword(byte[] documentID, byte[] ownerPassword,
                                     byte[] userKey, byte[] ownerKey, int permissions) {
        setupByOwnerPad(documentID, padPassword(ownerPassword), userKey,
                ownerKey, permissions);
    }

    private void setupByOwnerPad(byte[] documentID, byte[] ownerPad,
                                 byte[] userKey, byte[] ownerKey, int permissions) {
        byte[] userPad = computeOwnerKey(ownerKey, ownerPad); // userPad will
                                                                // be set in
                                                                // this.ownerKey
        setupGlobalEncryptionKey(documentID, userPad, ownerKey, permissions); // step
                                                                                // 3
        setupUserKey();
    }

    public void setupByEncryptionKey(byte[] key, int keylength) {
        mkey = new byte[keylength / 8];
        System.arraycopy(key, 0, mkey, 0, mkey.length);
    }

    public void setHashKey(int number, int generation) {
        if (revision >= AES_256_V3)
            return;

        md5.reset(); // added by ujihara
        extra[0] = (byte) number;
        extra[1] = (byte) (number >> 8);
        extra[2] = (byte) (number >> 16);
        extra[3] = (byte) generation;
        extra[4] = (byte) (generation >> 8);
        md5.update(mkey);
        md5.update(extra);
        if (revision == AES_128)
            md5.update(salt);
        key = md5.digest();
        keySize = mkey.length + 5;
        if (keySize > 16)
            keySize = 16;
    }

    public static PdfObject createInfoId(byte[] id) {
        return createInfoId(id, createDocumentId());
    }

    public static PdfObject createInfoId(byte[] idPartOne, byte[] idPartTwo) {
        try (ByteBuffer buf = new ByteBuffer(90)) {
            buf.append('[').append('<');
            for (int k = 0; k < 16 && k < idPartOne.length; ++k)
                buf.appendHex(idPartOne[k]);
            buf.append('>').append('<');
            for (int k = 0; k < 16 && k < idPartTwo.length; ++k)
                buf.appendHex(idPartTwo[k]);
            buf.append('>').append(']');
            return new PdfLiteral(buf.toByteArray());

        } catch (IOException e) {
            throw new ExceptionConverter(e);
        }
    }

    public PdfDictionary getEncryptionDictionary() {
        PdfDictionary dic = new PdfDictionary();

        if (publicKeyHandler.getRecipientsSize() > 0) {
            PdfArray recipients = null;

            dic.put(PdfName.FILTER, PdfName.PUBSEC);
            dic.put(PdfName.R, new PdfNumber(revision));

            try {
                recipients = publicKeyHandler.getEncodedRecipients();
            } catch (Exception f) {
                throw new ExceptionConverter(f);
            }

            if (revision == STANDARD_ENCRYPTION_40) {
                dic.put(PdfName.V, new PdfNumber(1));
                dic.put(PdfName.SUBFILTER, PdfName.ADBE_PKCS7_S4);
                dic.put(PdfName.RECIPIENTS, recipients);
            } else if (revision == STANDARD_ENCRYPTION_128 && encryptMetadata) {
                dic.put(PdfName.V, new PdfNumber(2));
                dic.put(PdfName.LENGTH, new PdfNumber(128));
                dic.put(PdfName.SUBFILTER, PdfName.ADBE_PKCS7_S4);
                dic.put(PdfName.RECIPIENTS, recipients);
            } else {
                dic.put(PdfName.R, new PdfNumber(AES_128));
                dic.put(PdfName.V, new PdfNumber(4));
                dic.put(PdfName.SUBFILTER, PdfName.ADBE_PKCS7_S5);

                PdfDictionary stdcf = new PdfDictionary();
                stdcf.put(PdfName.RECIPIENTS, recipients);
                if (!encryptMetadata)
                    stdcf.put(PdfName.ENCRYPTMETADATA, PdfBoolean.PDFFALSE);

                if (revision == AES_128)
                    stdcf.put(PdfName.CFM, PdfName.AESV2);
                else
                    stdcf.put(PdfName.CFM, PdfName.V2);
                PdfDictionary cf = new PdfDictionary();
                cf.put(PdfName.DEFAULTCRYPTFILTER, stdcf);
                dic.put(PdfName.CF, cf);if (embeddedFilesOnly) {
                    dic.put(PdfName.EFF, PdfName.DEFAULTCRYPTFILTER);
                    dic.put(PdfName.STRF, PdfName.IDENTITY);
                    dic.put(PdfName.STMF, PdfName.IDENTITY);
                }
                else {
                    dic.put(PdfName.STRF, PdfName.DEFAULTCRYPTFILTER);
                    dic.put(PdfName.STMF, PdfName.DEFAULTCRYPTFILTER);
                }
            }

            MessageDigest md = null;
            byte[] encodedRecipient = null;

            try {
                md = MessageDigest.getInstance("SHA-1");
                md.update(publicKeyHandler.getSeed());
                for (int i = 0; i < publicKeyHandler.getRecipientsSize(); i++) {
                    encodedRecipient = publicKeyHandler.getEncodedRecipient(i);
                    md.update(encodedRecipient);
                }
                if (!encryptMetadata)
                    md.update(new byte[] { (byte) 255, (byte) 255, (byte) 255,
                            (byte) 255 });
            } catch (Exception f) {
                throw new ExceptionConverter(f);
            }

            byte[] mdResult = md.digest();

            setupByEncryptionKey(mdResult, keyLength);
        } else {
            dic.put(PdfName.FILTER, PdfName.STANDARD);
            dic.put(PdfName.O, new PdfLiteral(PdfContentByte
                    .escapeString(ownerKey)));
            dic.put(PdfName.U, new PdfLiteral(PdfContentByte
                    .escapeString(userKey)));
            dic.put(PdfName.P, new PdfNumber(permissions));
            dic.put(PdfName.R, new PdfNumber(revision));

            if (revision == STANDARD_ENCRYPTION_40) {
                dic.put(PdfName.V, new PdfNumber(1));
            } else if (revision == STANDARD_ENCRYPTION_128 && encryptMetadata) {
                dic.put(PdfName.V, new PdfNumber(2));
                dic.put(PdfName.LENGTH, new PdfNumber(128));
            } else if (revision == STANDARD_ENCRYPTION_128 || revision == AES_128) {
                if (!encryptMetadata)
                    dic.put(PdfName.ENCRYPTMETADATA, PdfBoolean.PDFFALSE);
                dic.put(PdfName.R, new PdfNumber(AES_128));
                dic.put(PdfName.V, new PdfNumber(4));
                dic.put(PdfName.LENGTH, new PdfNumber(128));
                PdfDictionary stdcf = new PdfDictionary();
                stdcf.put(PdfName.LENGTH, new PdfNumber(16));
                if (embeddedFilesOnly) {
                    stdcf.put(PdfName.AUTHEVENT, PdfName.EFOPEN);
                    dic.put(PdfName.EFF, PdfName.STDCF);
                    dic.put(PdfName.STRF, PdfName.IDENTITY);
                    dic.put(PdfName.STMF, PdfName.IDENTITY);
                }
                else {
                    stdcf.put(PdfName.AUTHEVENT, PdfName.DOCOPEN);
                    dic.put(PdfName.STRF, PdfName.STDCF);
                    dic.put(PdfName.STMF, PdfName.STDCF);
                }
                if (revision == AES_128)
                    stdcf.put(PdfName.CFM, PdfName.AESV2);
                else
                    stdcf.put(PdfName.CFM, PdfName.V2);
                PdfDictionary cf = new PdfDictionary();
                cf.put(PdfName.STDCF, stdcf);
                dic.put(PdfName.CF, cf);
            } else if (revision == AES_256_V3) {
                if (!encryptMetadata)
                    dic.put(PdfName.ENCRYPTMETADATA, PdfBoolean.PDFFALSE);
                dic.put(PdfName.V, new PdfNumber(5));
                dic.put(PdfName.OE, new PdfLiteral(PdfContentByte.escapeString(oeKey)));
                dic.put(PdfName.UE, new PdfLiteral(PdfContentByte.escapeString(ueKey)));
                dic.put(PdfName.PERMS, new PdfLiteral(PdfContentByte.escapeString(perms)));
                dic.put(PdfName.LENGTH, new PdfNumber(256));
                PdfDictionary stdcf = new PdfDictionary();
                stdcf.put(PdfName.LENGTH, new PdfNumber(32));
                if (embeddedFilesOnly) {
                    stdcf.put(PdfName.AUTHEVENT, PdfName.EFOPEN);
                    dic.put(PdfName.EFF, PdfName.STDCF);
                    dic.put(PdfName.STRF, PdfName.IDENTITY);
                    dic.put(PdfName.STMF, PdfName.IDENTITY);
                } else {
                    stdcf.put(PdfName.AUTHEVENT, PdfName.DOCOPEN);
                    dic.put(PdfName.STRF, PdfName.STDCF);
                    dic.put(PdfName.STMF, PdfName.STDCF);
                }
                stdcf.put(PdfName.CFM, PdfName.AESV3);
                PdfDictionary cf = new PdfDictionary();
                cf.put(PdfName.STDCF, stdcf);
                dic.put(PdfName.CF, cf);
            }
        }

        return dic;
    }

    public PdfObject getFileID() {
        return createInfoId(documentID);
    }

    public OutputStreamEncryption getEncryptionStream(OutputStream os) {
        return new OutputStreamEncryption(os, key, 0, keySize, revision);
    }

    public int calculateStreamSize(int n) {
        if (revision == AES_128 || revision == AES_256_V3)
            return (n & 0x7ffffff0) + 32;
        else
            return n;
    }

    public byte[] encryptByteArray(byte[] b) {
        try {
            ByteArrayOutputStream ba = new ByteArrayOutputStream();
            OutputStreamEncryption os2 = getEncryptionStream(ba);
            os2.write(b);
            os2.finish();
            return ba.toByteArray();
        } catch (IOException ex) {
            throw new ExceptionConverter(ex);
        }
    }

    public StandardDecryption getDecryptor() {
        return new StandardDecryption(key, 0, keySize, revision);
    }

    public byte[] decryptByteArray(byte[] b) {
        try {
            ByteArrayOutputStream ba = new ByteArrayOutputStream();
            StandardDecryption dec = getDecryptor();
            byte[] b2 = dec.update(b, 0, b.length);
            if (b2 != null)
                ba.write(b2);
            b2 = dec.finish();
            if (b2 != null)
                ba.write(b2);
            return ba.toByteArray();
        } catch (IOException ex) {
            throw new ExceptionConverter(ex);
        }
    }

    public void addRecipient(Certificate cert, int permission) {
        documentID = createDocumentId();
        publicKeyHandler.addRecipient(new PdfPublicKeyRecipient(cert,
                permission));
    }

    public byte[] computeUserPassword(byte[] ownerPassword) {
        byte[] userPad = computeOwnerKey(ownerKey, padPassword(ownerPassword));
        for (int i = 0; i < userPad.length; i++) {
            boolean match = true;
            for (int j = 0; j < userPad.length - i; j++) {
                if (userPad[i + j] != pad[j]) {
                    match = false;
                    break;
                }
            }
            if (!match) continue;
            byte[] userPassword = new byte[i];
            System.arraycopy(userPad, 0, userPassword, 0, i);
            return userPassword;
        }
        return userPad;
    }

    //
    // AESv3 (AES256 according to ISO 32000-2) support
    //
    /**
     * implements step d of Algorithm 2.A: Retrieving the file encryption key from an encrypted document in order to decrypt it (revision 6 and later) - ISO 32000-2 section 7.6.4.3.3
     */
    public void setupByOwnerPassword(byte[] documentID, byte[] ownerPassword,
            byte[] uValue, byte[] ueValue, byte[] oValue, byte[] oeValue, int permissions) throws GeneralSecurityException {
        final Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");

        byte[] hashAlg2B = hashAlg2B(ownerPassword, Arrays.copyOfRange(oValue, 40, 48), uValue);
        cipher.init(Cipher.DECRYPT_MODE,
                new SecretKeySpec(hashAlg2B, "AES"),
                new IvParameterSpec(new byte[16]));
        key = cipher.update(oeValue, 0, oeValue.length);

        this.ownerKey = oValue;
        this.userKey = uValue;
        this.documentID = documentID;
        this.permissions = permissions;
    }

    /**
     * implements step e of Algorithm 2.A: Retrieving the file encryption key from an encrypted document in order to decrypt it (revision 6 and later) - ISO 32000-2 section 7.6.4.3.3
     */
    public void setupByUserPassword(byte[] documentID, byte[] userPassword,
            byte[] uValue, byte[] ueValue, byte[] oValue, byte[] oeValue, int permissions) throws GeneralSecurityException {
        final Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");

        byte[] hashAlg2B = hashAlg2B(userPassword, Arrays.copyOfRange(uValue, 40, 48), null);
        cipher.init(Cipher.DECRYPT_MODE,
                new SecretKeySpec(hashAlg2B, "AES"),
                new IvParameterSpec(new byte[16]));
        key = cipher.update(ueValue, 0, ueValue.length);

        this.ownerKey = oValue;
        this.userKey = uValue;
        this.documentID = documentID;
        this.permissions = permissions;
    }

    /**
     * implements step f of Algorithm 2.A: Retrieving the file encryption key from an encrypted document in order to decrypt it (revision 6 and later) - ISO 32000-2 section 7.6.4.3.3
     */
    public boolean decryptAndCheckPerms(byte[] permsValue) throws GeneralSecurityException {
        final Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");

        cipher.init(Cipher.DECRYPT_MODE,
                new SecretKeySpec(key, "AES"),
                new IvParameterSpec(new byte[16]));
        byte[] decPerms = cipher.update(permsValue, 0, permsValue.length);

        permissions = (decPerms[0] & 0xff) | ((decPerms[1] & 0xff) << 8)
                | ((decPerms[2] & 0xff) << 16) | ((decPerms[2] & 0xff) << 24);
        encryptMetadata = decPerms[8] == (byte)'T';

        return decPerms[9] == (byte)'a' && decPerms[10] == (byte)'d' && decPerms[11] == (byte)'b';
    }

    /**
     * implements Algorithm 2.B: Computing a hash (revision 6 and later) - ISO 32000-2 section 7.6.4.3.4
     */
    byte[] hashAlg2B(byte[] input, byte[] salt, byte[] userKey) throws GeneralSecurityException {
        final MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
        final MessageDigest sha384 = MessageDigest.getInstance("SHA-384");
        final MessageDigest sha512 = MessageDigest.getInstance("SHA-512");
        final Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");

        if (userKey == null)
            userKey = new byte[0];

        sha256.update(input);
        sha256.update(salt);
        sha256.update(userKey);
        byte[] k = sha256.digest();

        for (int round = 0, lastEByte = 0; round < 64 || lastEByte > round-32; round++) {
            int singleSequenceSize = input.length + k.length + userKey.length;
            byte[] k1 = new byte[singleSequenceSize * 64];
            System.arraycopy(input, 0, k1, 0, input.length);
            System.arraycopy(k, 0, k1, input.length, k.length);
            System.arraycopy(userKey, 0, k1, input.length + k.length, userKey.length);
            for (int i = 1; i < 64; i++)
                System.arraycopy(k1, 0, k1, singleSequenceSize * i, singleSequenceSize);

            cipher.init(Cipher.ENCRYPT_MODE,
                    new SecretKeySpec(Arrays.copyOf(k, 16), "AES"),
                    new IvParameterSpec(Arrays.copyOfRange(k, 16, 32)));
            byte[] e = cipher.update(k1, 0, k1.length);
            lastEByte = e[e.length - 1] & 0xFF;

            switch (new BigInteger(1, Arrays.copyOf(e, 16)).remainder(BigInteger.valueOf(3)).intValue()) {
            case 0:
                k = sha256.digest(e);
                break;
            case 1:
                k = sha384.digest(e);
                break;
            case 2:
                k = sha512.digest(e);
                break;
            }
        }

        return Arrays.copyOf(k, 32);
    }

    /**
     * implements Algorithm 8: Computing the encryption dictionary’s U (user password) and
     * UE (user encryption) values (Security handlers of revision 6) - ISO 32000-2 section 7.6.4.4.7
     */
    void computeUAndUeAlg8(byte[] userPassword) throws GeneralSecurityException {
        final Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");

        if (userPassword == null)
            userPassword = new byte[0];
        else if (userPassword.length > 127)
            userPassword = Arrays.copyOf(userPassword, 127);

        byte[] userSalts = IVGenerator.getIV(16);

        userKey = new byte[48];
        System.arraycopy(userSalts, 0, userKey, 32, 16);
        byte[] hashAlg2B = hashAlg2B(userPassword, Arrays.copyOf(userSalts, 8), null);
        System.arraycopy(hashAlg2B, 0, userKey, 0, 32);

        hashAlg2B = hashAlg2B(userPassword, Arrays.copyOfRange(userSalts, 8, 16), null);
        cipher.init(Cipher.ENCRYPT_MODE,
                new SecretKeySpec(hashAlg2B, "AES"),
                new IvParameterSpec(new byte[16]));
        ueKey = cipher.update(key, 0, keySize);
    }

    /**
     * implements Algorithm 9: Computing the encryption dictionary’s O (owner password) and
     * OE (owner encryption) values (Security handlers of revision 6) - ISO 32000-2 section 7.6.4.4.8
     */
    void computeOAndOeAlg9(byte[] ownerPassword) throws GeneralSecurityException {
        final Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");

        if (ownerPassword == null)
            ownerPassword = new byte[0];
        else if (ownerPassword.length > 127)
            ownerPassword = Arrays.copyOf(ownerPassword, 127);

        byte[] ownerSalts = IVGenerator.getIV(16);

        ownerKey = new byte[48];
        System.arraycopy(ownerSalts, 0, ownerKey, 32, 16);
        byte[] hashAlg2B = hashAlg2B(ownerPassword, Arrays.copyOf(ownerSalts, 8), userKey);
        System.arraycopy(hashAlg2B, 0, ownerKey, 0, 32);

        hashAlg2B = hashAlg2B(ownerPassword, Arrays.copyOfRange(ownerSalts, 8, 16), userKey);
        cipher.init(Cipher.ENCRYPT_MODE,
                new SecretKeySpec(hashAlg2B, "AES"),
                new IvParameterSpec(new byte[16]));
        oeKey = cipher.update(key, 0, keySize);
    }

    /**
     * implements Algorithm 10: Computing the encryption dictionary’s Perms (permissions)
     * value (Security handlers of revision 6) - ISO 32000-2 section 7.6.4.4.9
     */
    void computePermsAlg10(int permissions) throws GeneralSecurityException {
        final Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");

        byte[] rawPerms = new byte[16];
        rawPerms[0] = (byte) (permissions & 0xff);
        rawPerms[1] = (byte) ((permissions & 0xff00) >> 8);
        rawPerms[2] = (byte) ((permissions & 0xff0000) >> 16);
        rawPerms[3] = (byte) ((permissions & 0xff000000) >> 24);
        rawPerms[4] = (byte) 0xff;
        rawPerms[5] = (byte) 0xff;
        rawPerms[6] = (byte) 0xff;
        rawPerms[7] = (byte) 0xff;
        rawPerms[8] = (byte) (encryptMetadata ? 'T' : 'F');
        rawPerms[9] = (byte) 'a';
        rawPerms[10] = (byte) 'd';
        rawPerms[11] = (byte) 'b';
        System.arraycopy(IVGenerator.getIV(4), 0, rawPerms, 12, 4);

        cipher.init(Cipher.ENCRYPT_MODE,
                new SecretKeySpec(key, "AES"),
                new IvParameterSpec(new byte[16]));
        perms = cipher.update(rawPerms, 0, 16);
    }
}
