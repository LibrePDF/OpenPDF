/*
 * SPDX-License-Identifier: MPL-2.0 OR LGPL-2.1+
 * Copyright (c) 2026 the OpenPDF contributors.
 *
 * Dual licensed under the Mozilla Public License 2.0 (https://www.mozilla.org/MPL/2.0/)
 * or the GNU Lesser General Public License 2.1+ (https://www.gnu.org/licenses/old-licenses/lgpl-2.1.html).
 */
package org.openpdf.text.pdf.security;

import java.security.GeneralSecurityException;
import java.security.PublicKey;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERTaggedObject;
import org.bouncycastle.asn1.cms.RecipientInfo;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;

/**
 * Wraps a CMS content-encryption key (CEK) for an ML-KEM (FIPS 203 / Kyber) recipient as a CMS
 * {@code OtherRecipientInfo} carrying an RFC 9629 {@code KEMRecipientInfo} structure:
 *
 * <pre>
 *   OtherRecipientInfo ::= SEQUENCE {
 *       oriType  OBJECT IDENTIFIER,            -- id-ori-kem (1.2.840.113549.1.9.16.13.3)
 *       oriValue KEMRecipientInfo
 *   }
 *   KEMRecipientInfo ::= SEQUENCE {
 *       version       CMSVersion,              -- always 0
 *       rid       [0] IMPLICIT OCTET STRING,   -- SubjectKeyIdentifier (empty placeholder)
 *       kem           AlgorithmIdentifier,     -- ML-KEM-512 / 768 / 1024
 *       kemct         OCTET STRING,            -- KEM encapsulation
 *       kdf           AlgorithmIdentifier,     -- AES key wrap with padding (no KDF used)
 *       kekLength     INTEGER,
 *       wrap          AlgorithmIdentifier,     -- AES-256-WRAP-PAD (RFC 5649)
 *       encryptedKey  OCTET STRING             -- AES-Key-Wrap of the CEK
 *   }
 * </pre>
 *
 * <p>The CEK is wrapped using NIST SP 800-38F AES Key Wrap with Padding (RFC 5649); the
 * wrapping key is the leading 32 bytes of the ML-KEM shared secret. Verifiers that do not
 * understand {@code id-ori-kem} silently ignore this recipient and fall back to the classical
 * RSA recipient that is emitted alongside it.
 *
 * <p>This implementation uses only the public ASN.1 builder, the JDK 21 {@link javax.crypto.KEM}
 * API and the standard {@code AESWrapPad} {@link Cipher} transformation, so it stays compatible
 * with any modern BouncyCastle release that exposes ML-KEM.
 */
public final class MlKemRecipientWrapper {

    /** RFC 9629 OtherRecipientInfo type for KEMRecipientInfo. */
    private static final String ID_ORI_KEM = "1.2.840.113549.1.9.16.13.3";
    /** RFC 5649 AES-256 key wrap with padding. */
    private static final String ID_AES256_WRAP_PAD = "2.16.840.1.101.3.4.1.48";

    private static final Map<String, String> ML_KEM_OIDS;
    static {
        Map<String, String> m = new HashMap<>();
        m.put("ML-KEM-512", SecurityIDs.ID_ML_KEM_512);
        m.put("ML-KEM-768", SecurityIDs.ID_ML_KEM_768);
        m.put("ML-KEM-1024", SecurityIDs.ID_ML_KEM_1024);
        // The bare JCA name "ML-KEM" maps to the recommended security level.
        m.put("ML-KEM", SecurityIDs.ID_ML_KEM_768);
        ML_KEM_OIDS = Collections.unmodifiableMap(m);
    }

    private MlKemRecipientWrapper() {
        // utility
    }

    /**
     * Wraps {@code cek} for the recipient identified by {@code mlKemPublicKey}. Returns {@code null}
     * if the running JRE does not provide an ML-KEM KEM service, in which case the caller should
     * fall back to classical-only recipients.
     *
     * @param mlKemPublicKey an ML-KEM public key (JCA algorithm name {@code "ML-KEM"} or one of
     *                       {@code "ML-KEM-512"} / {@code "-768"} / {@code "-1024"})
     * @param cek            the raw content-encryption key bytes (typically 32 bytes for AES-256)
     * @return a CMS {@link RecipientInfo} carrying the wrapped CEK, or {@code null} when ML-KEM is
     *         unavailable on the running platform
     * @throws GeneralSecurityException if AES key-wrap fails for a reason other than missing
     *                                  algorithm support
     */
    public static RecipientInfo wrap(PublicKey mlKemPublicKey, byte[] cek)
            throws GeneralSecurityException {
        if (mlKemPublicKey == null || cek == null) {
            return null;
        }

        byte[] sharedSecret;
        byte[] encapsulation;
        try {
            javax.crypto.KEM kem = javax.crypto.KEM.getInstance(mlKemPublicKey.getAlgorithm());
            javax.crypto.KEM.Encapsulator enc = kem.newEncapsulator(mlKemPublicKey);
            javax.crypto.KEM.Encapsulated e = enc.encapsulate();
            sharedSecret = e.key().getEncoded();
            encapsulation = e.encapsulation();
        } catch (java.security.NoSuchAlgorithmException | java.security.InvalidKeyException ex) {
            // ML-KEM not available on this JRE — caller falls back to classical-only recipients.
            return null;
        }

        // Use the leading 32 bytes of the shared secret as the AES-256 KEK.
        byte[] kek = new byte[32];
        System.arraycopy(sharedSecret, 0, kek, 0, Math.min(sharedSecret.length, 32));
        SecretKey kekKey = new SecretKeySpec(kek, "AES");

        Cipher wrapCipher;
        try {
            wrapCipher = Cipher.getInstance("AESWrapPad");
        } catch (java.security.NoSuchAlgorithmException ex) {
            return null;
        }
        wrapCipher.init(Cipher.WRAP_MODE, kekKey);
        byte[] wrappedCek = wrapCipher.wrap(new SecretKeySpec(cek, "AES"));

        AlgorithmIdentifier wrapAlg = new AlgorithmIdentifier(new ASN1ObjectIdentifier(ID_AES256_WRAP_PAD));

        ASN1EncodableVector kemRi = new ASN1EncodableVector();
        kemRi.add(new ASN1Integer(0));                                       // version
        kemRi.add(new DERTaggedObject(false, 0, new DEROctetString(new byte[0]))); // rid (SKI)
        kemRi.add(new AlgorithmIdentifier(new ASN1ObjectIdentifier(oidFor(mlKemPublicKey)))); // kem
        kemRi.add(new DEROctetString(encapsulation));                        // kemct
        kemRi.add(wrapAlg);                                                  // kdf placeholder
        kemRi.add(new ASN1Integer(32));                                      // kekLength
        kemRi.add(wrapAlg);                                                  // wrap
        kemRi.add(new DEROctetString(wrappedCek));                           // encryptedKey

        ASN1EncodableVector ori = new ASN1EncodableVector();
        ori.add(new ASN1ObjectIdentifier(ID_ORI_KEM));
        ori.add(new DERSequence(kemRi));
        return RecipientInfo.getInstance(new DERTaggedObject(false, 4, new DERSequence(ori)));
    }

    private static String oidFor(PublicKey k) {
        String name = k.getAlgorithm();
        if (name == null) {
            return SecurityIDs.ID_ML_KEM_768;
        }
        String oid = ML_KEM_OIDS.get(name.toUpperCase(Locale.ROOT));
        return oid != null ? oid : SecurityIDs.ID_ML_KEM_768;
    }
}

