/*
 * SPDX-License-Identifier: MPL-2.0 OR LGPL-2.1+
 * Copyright (c) 2026 the OpenPDF contributors.
 *
 * Dual licensed under the Mozilla Public License 2.0 (https://www.mozilla.org/MPL/2.0/)
 * or the GNU Lesser General Public License 2.1+ (https://www.gnu.org/licenses/old-licenses/lgpl-2.1.html).
 */
package org.openpdf.text.pdf.security;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Mapping helpers for post-quantum and modern signature algorithms used inside CMS / PKCS#7
 * SignedData structures (PDF signatures with SubFilter {@code ETSI.CAdES.detached}).
 *
 * <p>The mapping covers:
 * <ul>
 *   <li>NIST FIPS 204 (ML-DSA / Dilithium): ML-DSA-44/65/87.</li>
 *   <li>NIST FIPS 205 (SLH-DSA / SPHINCS+): all eight parameter sets.</li>
 *   <li>EdDSA: Ed25519 and Ed448.</li>
 * </ul>
 *
 * <p>The signature algorithms in this list are the ones for which the CMS {@code AlgorithmIdentifier}
 * MUST be encoded with absent parameters (RFC 8419 / FIPS 204). Callers that produce SignerInfo
 * structures should consult {@link #omitsAlgorithmParameters(String)} before adding a {@code NULL}
 * parameter to the signature algorithm identifier.
 */
public final class PqcAlgorithms {

    /** OIDs whose CMS AlgorithmIdentifier must be encoded with absent (not NULL) parameters. */
    private static final Set<String> ABSENT_PARAMS;
    /** Mapping from signature OID to JCA algorithm name (suitable for {@code Signature.getInstance}). */
    private static final Map<String, String> JCA_NAMES;
    /** Mapping from signature OID to recommended digest algorithm. */
    private static final Map<String, String> DEFAULT_DIGESTS;

    static {
        Map<String, String> jca = new HashMap<>();
        Map<String, String> dig = new HashMap<>();

        // EdDSA (RFC 8419: parameters absent; digest is implicit in the signature algorithm)
        jca.put(SecurityIDs.ID_ED25519, "Ed25519");
        jca.put(SecurityIDs.ID_ED448, "Ed448");
        dig.put(SecurityIDs.ID_ED25519, "SHA-512");
        dig.put(SecurityIDs.ID_ED448, "SHAKE256");

        // ML-DSA / Dilithium (FIPS 204)
        jca.put(SecurityIDs.ID_ML_DSA_44, "ML-DSA-44");
        jca.put(SecurityIDs.ID_ML_DSA_65, "ML-DSA-65");
        jca.put(SecurityIDs.ID_ML_DSA_87, "ML-DSA-87");
        dig.put(SecurityIDs.ID_ML_DSA_44, "SHA-256");
        dig.put(SecurityIDs.ID_ML_DSA_65, "SHA-384");
        dig.put(SecurityIDs.ID_ML_DSA_87, "SHA-512");

        // SLH-DSA / SPHINCS+ (FIPS 205)
        jca.put(SecurityIDs.ID_SLH_DSA_SHA2_128S, "SLH-DSA-SHA2-128S");
        jca.put(SecurityIDs.ID_SLH_DSA_SHA2_128F, "SLH-DSA-SHA2-128F");
        jca.put(SecurityIDs.ID_SLH_DSA_SHA2_192S, "SLH-DSA-SHA2-192S");
        jca.put(SecurityIDs.ID_SLH_DSA_SHA2_192F, "SLH-DSA-SHA2-192F");
        jca.put(SecurityIDs.ID_SLH_DSA_SHA2_256S, "SLH-DSA-SHA2-256S");
        jca.put(SecurityIDs.ID_SLH_DSA_SHA2_256F, "SLH-DSA-SHA2-256F");
        jca.put(SecurityIDs.ID_SLH_DSA_SHAKE_128S, "SLH-DSA-SHAKE-128S");
        jca.put(SecurityIDs.ID_SLH_DSA_SHAKE_128F, "SLH-DSA-SHAKE-128F");
        for (String oid : new String[]{
                SecurityIDs.ID_SLH_DSA_SHA2_128S, SecurityIDs.ID_SLH_DSA_SHA2_128F,
                SecurityIDs.ID_SLH_DSA_SHA2_192S, SecurityIDs.ID_SLH_DSA_SHA2_192F,
                SecurityIDs.ID_SLH_DSA_SHA2_256S, SecurityIDs.ID_SLH_DSA_SHA2_256F,
                SecurityIDs.ID_SLH_DSA_SHAKE_128S, SecurityIDs.ID_SLH_DSA_SHAKE_128F}) {
            dig.put(oid, "SHA-256");
        }

        JCA_NAMES = Collections.unmodifiableMap(jca);
        DEFAULT_DIGESTS = Collections.unmodifiableMap(dig);
        // Every algorithm in JCA_NAMES (EdDSA, ML-DSA, SLH-DSA) requires absent parameters.
        ABSENT_PARAMS = Collections.unmodifiableSet(new HashSet<>(jca.keySet()));
    }

    private PqcAlgorithms() {
        // utility
    }

    /**
     * @param oid signature algorithm OID
     * @return {@code true} if the algorithm requires absent (not {@code DERNull}) parameters in
     *         the CMS {@code AlgorithmIdentifier}
     */
    public static boolean omitsAlgorithmParameters(String oid) {
        return oid != null && ABSENT_PARAMS.contains(oid);
    }

    /**
     * @param oid signature algorithm OID
     * @return the JCA algorithm name (e.g. {@code "ML-DSA-65"}, {@code "Ed25519"}), or {@code null}
     *         if the OID is not a known modern / post-quantum algorithm
     */
    public static String getJcaName(String oid) {
        return JCA_NAMES.get(oid);
    }

    /**
     * @param oid signature algorithm OID
     * @return the recommended message digest for use as the CMS {@code messageDigest} attribute, or
     *         {@code null} if not applicable
     */
    public static String getDefaultDigest(String oid) {
        return DEFAULT_DIGESTS.get(oid);
    }

    /**
     * @return an unmodifiable view of all known PQC / modern signature algorithm OIDs
     */
    public static Set<String> knownOids() {
        return JCA_NAMES.keySet();
    }
}

