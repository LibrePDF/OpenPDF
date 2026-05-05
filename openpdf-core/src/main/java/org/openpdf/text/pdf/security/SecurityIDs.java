/*
 * SPDX-License-Identifier: MPL-2.0 OR LGPL-2.1+
 * Copyright (c) 2026 the OpenPDF contributors.
 *
 * Dual licensed under the Mozilla Public License 2.0 (https://www.mozilla.org/MPL/2.0/)
 * or the GNU Lesser General Public License 2.1+ (https://www.gnu.org/licenses/old-licenses/lgpl-2.1.html).
 */
package org.openpdf.text.pdf.security;

/**
 * Central catalogue of cryptographic Object Identifier (OID) constants used by the OpenPDF
 * signature and encryption code. Grouping these in one place keeps {@code PdfPKCS7} and friends
 * free of scattered string literals and makes it easier to add new algorithms (for example, the
 * NIST FIPS 204 / FIPS 205 post-quantum signature suites).
 */
public final class SecurityIDs {

    private SecurityIDs() {
        // utility
    }

    // ---- CMS / PKCS#7 content types ----
    public static final String ID_PKCS7_DATA = "1.2.840.113549.1.7.1";
    public static final String ID_PKCS7_SIGNED_DATA = "1.2.840.113549.1.7.2";
    public static final String ID_PKCS7_ENVELOPED_DATA = "1.2.840.113549.1.7.3";

    // ---- Classical signature algorithms (key OIDs) ----
    public static final String ID_RSA = "1.2.840.113549.1.1.1";
    public static final String ID_RSASSA_PSS = "1.2.840.113549.1.1.10";
    public static final String ID_DSA = "1.2.840.10040.4.1";
    public static final String ID_ECDSA = "1.2.840.10045.2.1";
    public static final String ID_ED25519 = "1.3.101.112";
    public static final String ID_ED448 = "1.3.101.113";

    // ---- Post-quantum signature algorithms (NIST FIPS 204: ML-DSA / Dilithium) ----
    public static final String ID_ML_DSA_44 = "2.16.840.1.101.3.4.3.17";
    public static final String ID_ML_DSA_65 = "2.16.840.1.101.3.4.3.18";
    public static final String ID_ML_DSA_87 = "2.16.840.1.101.3.4.3.19";

    // ---- Post-quantum signature algorithms (NIST FIPS 205: SLH-DSA / SPHINCS+) ----
    public static final String ID_SLH_DSA_SHA2_128S = "2.16.840.1.101.3.4.3.20";
    public static final String ID_SLH_DSA_SHA2_128F = "2.16.840.1.101.3.4.3.21";
    public static final String ID_SLH_DSA_SHA2_192S = "2.16.840.1.101.3.4.3.22";
    public static final String ID_SLH_DSA_SHA2_192F = "2.16.840.1.101.3.4.3.23";
    public static final String ID_SLH_DSA_SHA2_256S = "2.16.840.1.101.3.4.3.24";
    public static final String ID_SLH_DSA_SHA2_256F = "2.16.840.1.101.3.4.3.25";
    public static final String ID_SLH_DSA_SHAKE_128S = "2.16.840.1.101.3.4.3.26";
    public static final String ID_SLH_DSA_SHAKE_128F = "2.16.840.1.101.3.4.3.27";

    // ---- Post-quantum KEM (NIST FIPS 203: ML-KEM / Kyber) ----
    public static final String ID_ML_KEM_512 = "2.16.840.1.101.3.4.4.1";
    public static final String ID_ML_KEM_768 = "2.16.840.1.101.3.4.4.2";
    public static final String ID_ML_KEM_1024 = "2.16.840.1.101.3.4.4.3";

    // ---- Digest algorithms ----
    public static final String ID_SHA1 = "1.3.14.3.2.26";
    public static final String ID_SHA224 = "2.16.840.1.101.3.4.2.4";
    public static final String ID_SHA256 = "2.16.840.1.101.3.4.2.1";
    public static final String ID_SHA384 = "2.16.840.1.101.3.4.2.2";
    public static final String ID_SHA512 = "2.16.840.1.101.3.4.2.3";
    public static final String ID_SHA3_224 = "2.16.840.1.101.3.4.2.7";
    public static final String ID_SHA3_256 = "2.16.840.1.101.3.4.2.8";
    public static final String ID_SHA3_384 = "2.16.840.1.101.3.4.2.9";
    public static final String ID_SHA3_512 = "2.16.840.1.101.3.4.2.10";
    public static final String ID_SHAKE128 = "2.16.840.1.101.3.4.2.11";
    public static final String ID_SHAKE256 = "2.16.840.1.101.3.4.2.12";

    // ---- CMS content-encryption algorithms ----
    public static final String ID_AES_256_CBC = "2.16.840.1.101.3.4.1.42";

    // ---- CMS signed-data attribute OIDs ----
    public static final String ID_CONTENT_TYPE = "1.2.840.113549.1.9.3";
    public static final String ID_MESSAGE_DIGEST = "1.2.840.113549.1.9.4";
    public static final String ID_SIGNING_TIME = "1.2.840.113549.1.9.5";
    public static final String ID_AA_SIGNING_CERTIFICATE_V2 = "1.2.840.113549.1.9.16.2.47";
    public static final String ID_ADBE_REVOCATION = "1.2.840.113583.1.1.8";
}

