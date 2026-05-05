package org.openpdf.text.pdf.security;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Sanity-checks that SecurityIDs OID strings are non-null, non-empty and follow OID syntax.
 */
class SecurityIDsTest {

    @Test
    void classicSignatureOidsAreValid() {
        assertValidOid(SecurityIDs.ID_RSA);
        assertValidOid(SecurityIDs.ID_DSA);
        assertValidOid(SecurityIDs.ID_ECDSA);
        assertValidOid(SecurityIDs.ID_ED25519);
        assertValidOid(SecurityIDs.ID_ED448);
    }

    @Test
    void mlDsaOidsAreValid() {
        assertValidOid(SecurityIDs.ID_ML_DSA_44);
        assertValidOid(SecurityIDs.ID_ML_DSA_65);
        assertValidOid(SecurityIDs.ID_ML_DSA_87);
    }

    @Test
    void slhDsaOidsAreValid() {
        assertValidOid(SecurityIDs.ID_SLH_DSA_SHA2_128S);
        assertValidOid(SecurityIDs.ID_SLH_DSA_SHA2_128F);
        assertValidOid(SecurityIDs.ID_SLH_DSA_SHA2_192S);
        assertValidOid(SecurityIDs.ID_SLH_DSA_SHA2_192F);
        assertValidOid(SecurityIDs.ID_SLH_DSA_SHA2_256S);
        assertValidOid(SecurityIDs.ID_SLH_DSA_SHA2_256F);
        assertValidOid(SecurityIDs.ID_SLH_DSA_SHAKE_128S);
        assertValidOid(SecurityIDs.ID_SLH_DSA_SHAKE_128F);
    }

    @Test
    void mlKemOidsAreValid() {
        assertValidOid(SecurityIDs.ID_ML_KEM_512);
        assertValidOid(SecurityIDs.ID_ML_KEM_768);
        assertValidOid(SecurityIDs.ID_ML_KEM_1024);
    }

    @Test
    void digestOidsAreValid() {
        assertValidOid(SecurityIDs.ID_SHA1);
        assertValidOid(SecurityIDs.ID_SHA256);
        assertValidOid(SecurityIDs.ID_SHA384);
        assertValidOid(SecurityIDs.ID_SHA512);
        assertValidOid(SecurityIDs.ID_SHA3_224);
        assertValidOid(SecurityIDs.ID_SHA3_256);
        assertValidOid(SecurityIDs.ID_SHA3_384);
        assertValidOid(SecurityIDs.ID_SHA3_512);
    }

    @Test
    void cmsAttributeOidsAreValid() {
        assertValidOid(SecurityIDs.ID_CONTENT_TYPE);
        assertValidOid(SecurityIDs.ID_MESSAGE_DIGEST);
        assertValidOid(SecurityIDs.ID_SIGNING_TIME);
        assertValidOid(SecurityIDs.ID_AA_SIGNING_CERTIFICATE_V2);
    }

    /** Verifies that an OID is non-null and matches the dotted-integer OID pattern. */
    private static void assertValidOid(String oid) {
        assertNotNull(oid, "OID must not be null");
        assertTrue(oid.matches("\\d+(\\.\\d+)+"),
                "Expected dotted OID notation, got: " + oid);
    }
}
