/*
 * $Id: PdfPublicKeySecurityHandler.java 4055 2009-08-30 23:47:33Z psoares33 $
 * $Name$
 *
 * Copyright 2006 Paulo Soares
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
 * the Initial Developer are Copyright (C) 1999-2007 by Bruno Lowagie.
 * All Rights Reserved.
 * Co-Developer of the code is Paulo Soares. Portions created by the Co-Developer
 * are Copyright (C) 2000-2007 by Paulo Soares. All Rights Reserved.
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

/*
      The below 2 methods are from pdfbox.

      private DERObject createDERForRecipient(byte[] in, X509Certificate cert) ;
      private KeyTransRecipientInfo computeRecipientInfo(X509Certificate x509certificate, byte[] abyte0);

      2006-11-22 Aiken Sam.
 */

/*
  Copyright (c) 2003-2006, www.pdfbox.org
  All rights reserved.

  Redistribution and use in source and binary forms, with or without
  modification, are permitted provided that the following conditions are met:

  1. Redistributions of source code must retain the above copyright notice,
     this list of conditions and the following disclaimer.
  2. Redistributions in binary form must reproduce the above copyright notice,
     this list of conditions and the following disclaimer in the documentation
     and/or other materials provided with the distribution.
  3. Neither the name of pdfbox; nor the names of its
     contributors may be used to endorse or promote products derived from this
     software without specific prior written permission.

  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
  ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

  http://www.pdfbox.org

 */

package org.openpdf.text.pdf;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.AlgorithmParameterGenerator;
import java.security.AlgorithmParameters;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import org.bouncycastle.asn1.ASN1Encoding;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1OutputStream;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Set;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DERSet;
import org.bouncycastle.asn1.cms.ContentInfo;
import org.bouncycastle.asn1.cms.EncryptedContentInfo;
import org.bouncycastle.asn1.cms.EnvelopedData;
import org.bouncycastle.asn1.cms.IssuerAndSerialNumber;
import org.bouncycastle.asn1.cms.KeyTransRecipientInfo;
import org.bouncycastle.asn1.cms.RecipientIdentifier;
import org.bouncycastle.asn1.cms.RecipientInfo;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.TBSCertificate;

/**
 * @author Aiken Sam (aikensam@ieee.org)
 */
public class PdfPublicKeySecurityHandler {

    static final int SEED_LENGTH = 20;

    /** Legacy CMS content-encryption: RC2/40-bit (used when the security handler version is V=1/2/4). */
    private static final String CONTENT_ENC_RC2 = "1.2.840.113549.3.2";
    /** PDF 2.0 CMS content-encryption: AES-256-CBC (used when the security handler version is V=5). */
    private static final String CONTENT_ENC_AES256_CBC = "2.16.840.1.101.3.4.1.42";

    private final List<PdfPublicKeyRecipient> recipients;

    private byte[] seed = new byte[SEED_LENGTH];

    /**
     * If {@code true}, the recipient list is wrapped using AES-256-CBC and SHA-256 hashing as required
     * by ISO 32000-2 §7.6.5 (PDF 2.0 / public-key security handler V=5). Defaults to {@code false} for
     * back-compatibility with V&le;4.
     */
    private boolean useAes256;

    /**
     * If {@code true}, each recipient certificate's content-encryption key is additionally wrapped
     * for a post-quantum ML-KEM recipient, alongside the classical RSA wrapping. Off by default.
     */
    private boolean hybridRecipients;

    public PdfPublicKeySecurityHandler() {
        KeyGenerator key;
        try {
            key = KeyGenerator.getInstance("AES");
            key.init(192, new SecureRandom());
            SecretKey sk = key.generateKey();
            System.arraycopy(sk.getEncoded(), 0, seed, 0, SEED_LENGTH); // create the
            // 20 bytes
            // seed
        } catch (NoSuchAlgorithmException e) {
            seed = SecureRandom.getSeed(SEED_LENGTH);
        }

        recipients = new ArrayList<>();
    }

    /**
     * Selects the CMS content-encryption algorithm. When {@code true}, recipients are wrapped using
     * AES-256-CBC (ISO 32000-2 §7.6.5, PDF 2.0). When {@code false}, the legacy RC2/40-bit algorithm
     * is used (PDF 1.7 and earlier, V&le;4). Should be set to {@code true} when the encryption mode
     * is {@link PdfWriter#ENCRYPTION_AES_256_V3}.
     *
     * @param useAes256 {@code true} to enable PDF 2.0 AES-256 wrapping
     */
    public void setUseAes256(boolean useAes256) {
        this.useAes256 = useAes256;
    }

    /**
     * Enables hybrid recipient mode: each classical {@code KeyTransRecipientInfo} is paired with an
     * additional ML-KEM recipient wrapping the same content-encryption key, so the document remains
     * decryptable if either cryptosystem is broken. Recipients without a corresponding ML-KEM key
     * are encoded with the classical recipient only. Off by default.
     *
     * @param hybridRecipients {@code true} to enable hybrid wrapping
     */
    public void setHybridRecipients(boolean hybridRecipients) {
        this.hybridRecipients = hybridRecipients;
    }

    /**
     * @return whether hybrid recipient mode is currently enabled
     */
    public boolean isHybridRecipients() {
        return hybridRecipients;
    }

    public void addRecipient(PdfPublicKeyRecipient recipient) {
        recipients.add(recipient);
    }

    protected byte[] getSeed() {
        return seed.clone();
    }

    /*
     * public PdfPublicKeyRecipient[] getRecipients() { recipients.toArray();
     * return (PdfPublicKeyRecipient[])recipients.toArray(); }
     */

    public int getRecipientsSize() {
        return recipients.size();
    }

    public byte[] getEncodedRecipient(int index) throws IOException,
            GeneralSecurityException {
        // Certificate certificate = recipient.getX509();
        PdfPublicKeyRecipient recipient = recipients
                .get(index);
        byte[] cms = recipient.getCms();

        if (cms != null) {
            return cms;
        }

        Certificate certificate = recipient.getCertificate();
        int permission = recipient.getPermission();

        permission |= 0xfffff0c0;
        permission &= 0xfffffffc;
        permission += 1;

        byte[] pkcs7input = new byte[24];

        byte one = (byte) (permission);
        byte two = (byte) (permission >> 8);
        byte three = (byte) (permission >> 16);
        byte four = (byte) (permission >> 24);

        System.arraycopy(seed, 0, pkcs7input, 0, 20); // put this seed in the pkcs7
        // input

        pkcs7input[20] = four;
        pkcs7input[21] = three;
        pkcs7input[22] = two;
        pkcs7input[23] = one;

        ASN1Primitive obj = createDERForRecipient(pkcs7input, recipient);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        final ASN1OutputStream derOutputStream = ASN1OutputStream.create(baos, ASN1Encoding.DER);

        derOutputStream.writeObject(obj);

        cms = baos.toByteArray();

        recipient.setCms(cms);

        return cms;
    }

    public PdfArray getEncodedRecipients() throws IOException {
        PdfArray encodedRecipients = new PdfArray();
        byte[] cms;
        for (int i = 0; i < recipients.size(); i++) {
            try {
                cms = getEncodedRecipient(i);
                encodedRecipients.add(new PdfLiteral(PdfContentByte.escapeString(cms)));
            } catch (GeneralSecurityException | IOException e) {
                encodedRecipients = null;
                break;
            }
        }
        return encodedRecipients;
    }

    private ASN1Primitive createDERForRecipient(byte[] in, PdfPublicKeyRecipient recipient)
            throws IOException, GeneralSecurityException {

        X509Certificate cert = (X509Certificate) recipient.getCertificate();
        if (useAes256) {
            return createAes256EnvelopedData(in, recipient, cert);
        }
        return createRc2EnvelopedData(in, recipient, cert);
    }

    /**
     * Builds a CMS EnvelopedData using AES-256-CBC content encryption, as required for PDF 2.0
     * public-key encryption (ISO 32000-2 §7.6.5).
     */
    private ASN1Primitive createAes256EnvelopedData(byte[] in, PdfPublicKeyRecipient recipient,
            X509Certificate cert) throws IOException, GeneralSecurityException {
        // Generate a random 256-bit content-encryption key and a random 128-bit IV.
        KeyGenerator keygenerator = KeyGenerator.getInstance("AES");
        keygenerator.init(256, new SecureRandom());
        SecretKey secretkey = keygenerator.generateKey();

        byte[] iv = new byte[16];
        new SecureRandom().nextBytes(iv);
        // AES-256-CBC with PKCS#5/#7 padding is mandated by RFC 3565 and ISO 32000-2 §7.6.5
        // for the CMS EnvelopedData content encryption used in PDF 2.0 public-key encryption.
        // The ciphertext here is the file encryption seed wrapped for a certificate recipient;
        // it is not user data and is not exposed to a padding-oracle channel — any decryption
        // failure aborts PDF parsing without leaking padding validity. Authenticated modes
        // (AES-GCM, ChaCha20-Poly1305) are not permitted by the PDF specification for PUBSEC.
        // codacy-disable-next-line
        // nosemgrep: java.lang.security.audit.crypto.use-of-aes-cbc.use-of-aes-cbc
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding"); // NOSONAR java:S5542
        cipher.init(Cipher.ENCRYPT_MODE, secretkey, new IvParameterSpec(iv));
        byte[] encrypted = cipher.doFinal(in);

        // AES-CBC algorithm parameters are an OCTET STRING containing the IV (RFC 3565).
        ASN1Primitive paramsObj = new DEROctetString(iv);
        AlgorithmIdentifier contentAlg = new AlgorithmIdentifier(
                new ASN1ObjectIdentifier(CONTENT_ENC_AES256_CBC), paramsObj);
        EncryptedContentInfo encryptedcontentinfo = new EncryptedContentInfo(
                PKCSObjectIdentifiers.data, contentAlg, new DEROctetString(encrypted));

        DERSet derset = new DERSet(buildRecipientInfos(recipient, cert, secretkey.getEncoded()));
        EnvelopedData env = new EnvelopedData(null, derset, encryptedcontentinfo, (ASN1Set) null);
        return new ContentInfo(PKCSObjectIdentifiers.envelopedData, env).toASN1Primitive();
    }

    /**
     * Legacy CMS EnvelopedData using RC2 / 40-bit content encryption, retained for PDF 1.7 and
     * earlier (V&le;4) public-key encryption.
     */
    private ASN1Primitive createRc2EnvelopedData(byte[] in, PdfPublicKeyRecipient recipient,
            X509Certificate cert) throws IOException, GeneralSecurityException {

        String s = CONTENT_ENC_RC2;

        AlgorithmParameterGenerator algorithmparametergenerator = AlgorithmParameterGenerator
                .getInstance(s);
        AlgorithmParameters algorithmparameters = algorithmparametergenerator
                .generateParameters();
        ByteArrayInputStream bytearrayinputstream = new ByteArrayInputStream(
                algorithmparameters.getEncoded("ASN.1"));
        ASN1InputStream asn1inputstream = new ASN1InputStream(bytearrayinputstream);
        ASN1Primitive derobject = asn1inputstream.readObject();
        KeyGenerator keygenerator = KeyGenerator.getInstance(s);
        keygenerator.init(128);
        SecretKey secretkey = keygenerator.generateKey();
        Cipher cipher = Cipher.getInstance(s);
        cipher.init(1, secretkey, algorithmparameters);
        byte[] abyte1 = cipher.doFinal(in);
        DEROctetString deroctetstring = new DEROctetString(abyte1);
        DERSet derset = new DERSet(buildRecipientInfos(recipient, cert, secretkey.getEncoded()));
        AlgorithmIdentifier algorithmidentifier = new AlgorithmIdentifier(
                new ASN1ObjectIdentifier(s), derobject);
        EncryptedContentInfo encryptedcontentinfo = new EncryptedContentInfo(
                PKCSObjectIdentifiers.data, algorithmidentifier, deroctetstring);
        EnvelopedData env = new EnvelopedData(null, derset, encryptedcontentinfo, (ASN1Set) null);
        return new ContentInfo(PKCSObjectIdentifiers.envelopedData, env).toASN1Primitive();
    }

    /**
     * Builds the {@code RecipientInfos} set for an EnvelopedData. The default implementation produces
     * a single classical {@link KeyTransRecipientInfo} (RSA wrapping). When {@link #isHybridRecipients()}
     * is {@code true} and the recipient has an ML-KEM public key configured, an additional
     * {@code OtherRecipientInfo} (RFC 9629 KEMRecipientInfo) is added wrapping the same CEK.
     *
     * @param recipient the high-level recipient descriptor (carries optional PQC public key)
     * @param cert      the recipient X.509 certificate
     * @param cek       the raw content-encryption key bytes that must be wrapped for the recipient
     * @return an array of CMS {@link RecipientInfo} structures
     */
    protected RecipientInfo[] buildRecipientInfos(PdfPublicKeyRecipient recipient,
            X509Certificate cert, byte[] cek) throws GeneralSecurityException, IOException {
        java.util.List<RecipientInfo> infos = new java.util.ArrayList<>(2);
        infos.add(new RecipientInfo(computeRecipientInfo(cert, cek)));
        if (hybridRecipients && recipient.getPqcPublicKey() != null) {
            RecipientInfo extra = org.openpdf.text.pdf.security.MlKemRecipientWrapper
                    .wrap(recipient.getPqcPublicKey(), cek);
            if (extra != null) {
                infos.add(extra);
            }
        }
        return infos.toArray(new RecipientInfo[0]);
    }

    private KeyTransRecipientInfo computeRecipientInfo(
            X509Certificate x509certificate, byte[] abyte0)
            throws GeneralSecurityException, IOException {
        ASN1InputStream asn1inputstream = new ASN1InputStream(
                new ByteArrayInputStream(x509certificate.getTBSCertificate()));
        TBSCertificate tbsCertificate = TBSCertificate.getInstance(asn1inputstream.readObject());
        AlgorithmIdentifier algorithmidentifier = tbsCertificate.getSubjectPublicKeyInfo().getAlgorithm();
        IssuerAndSerialNumber issuerandserialnumber = new IssuerAndSerialNumber(
                tbsCertificate.getIssuer(), tbsCertificate.getSerialNumber().getValue());
        Cipher cipher = Cipher.getInstance(algorithmidentifier.getAlgorithm().getId());
        cipher.init(1, x509certificate);
        DEROctetString deroctetstring = new DEROctetString(cipher.doFinal(abyte0));
        RecipientIdentifier recipId = new RecipientIdentifier(issuerandserialnumber);
        return new KeyTransRecipientInfo(recipId, algorithmidentifier, deroctetstring);
    }
}
