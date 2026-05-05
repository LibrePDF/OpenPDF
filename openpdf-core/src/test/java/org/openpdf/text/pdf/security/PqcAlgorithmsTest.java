package org.openpdf.text.pdf.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PqcAlgorithmsTest {

    @Test
    void edDsaOidsOmitParameters() {
        assertTrue(PqcAlgorithms.omitsAlgorithmParameters(SecurityIDs.ID_ED25519),
                "Ed25519 must omit AlgorithmIdentifier parameters (RFC 8419)");
        assertTrue(PqcAlgorithms.omitsAlgorithmParameters(SecurityIDs.ID_ED448),
                "Ed448 must omit AlgorithmIdentifier parameters (RFC 8419)");
    }

    @Test
    void mlDsaOidsOmitParameters() {
        assertTrue(PqcAlgorithms.omitsAlgorithmParameters(SecurityIDs.ID_ML_DSA_44));
        assertTrue(PqcAlgorithms.omitsAlgorithmParameters(SecurityIDs.ID_ML_DSA_65));
        assertTrue(PqcAlgorithms.omitsAlgorithmParameters(SecurityIDs.ID_ML_DSA_87));
    }

    @Test
    void slhDsaOidsOmitParameters() {
        assertTrue(PqcAlgorithms.omitsAlgorithmParameters(SecurityIDs.ID_SLH_DSA_SHA2_128S));
        assertTrue(PqcAlgorithms.omitsAlgorithmParameters(SecurityIDs.ID_SLH_DSA_SHA2_128F));
        assertTrue(PqcAlgorithms.omitsAlgorithmParameters(SecurityIDs.ID_SLH_DSA_SHA2_256S));
        assertTrue(PqcAlgorithms.omitsAlgorithmParameters(SecurityIDs.ID_SLH_DSA_SHAKE_128S));
    }

    @Test
    void classicAlgorithmsDoNotOmitParameters() {
        // RSA, DSA, ECDSA must keep DERNull parameters for CMS backward compatibility
        assertFalse(PqcAlgorithms.omitsAlgorithmParameters(SecurityIDs.ID_RSA));
        assertFalse(PqcAlgorithms.omitsAlgorithmParameters(SecurityIDs.ID_DSA));
        assertFalse(PqcAlgorithms.omitsAlgorithmParameters(SecurityIDs.ID_ECDSA));
    }

    @Test
    void nullOidReturnsFalse() {
        assertFalse(PqcAlgorithms.omitsAlgorithmParameters(null));
    }

    @Test
    void unknownOidReturnsFalse() {
        assertFalse(PqcAlgorithms.omitsAlgorithmParameters("1.2.3.4.5.99"));
    }

    @Test
    void getJcaNameReturnsCorrectName() {
        assertEquals("Ed25519", PqcAlgorithms.getJcaName(SecurityIDs.ID_ED25519));
        assertEquals("Ed448", PqcAlgorithms.getJcaName(SecurityIDs.ID_ED448));
        assertEquals("ML-DSA-44", PqcAlgorithms.getJcaName(SecurityIDs.ID_ML_DSA_44));
        assertEquals("ML-DSA-65", PqcAlgorithms.getJcaName(SecurityIDs.ID_ML_DSA_65));
        assertEquals("ML-DSA-87", PqcAlgorithms.getJcaName(SecurityIDs.ID_ML_DSA_87));
    }

    @Test
    void getJcaNameReturnsNullForClassicOids() {
        assertNull(PqcAlgorithms.getJcaName(SecurityIDs.ID_RSA));
        assertNull(PqcAlgorithms.getJcaName(SecurityIDs.ID_ECDSA));
    }

    @Test
    void knownOidsIsNotEmpty() {
        assertFalse(PqcAlgorithms.knownOids().isEmpty());
        assertTrue(PqcAlgorithms.knownOids().contains(SecurityIDs.ID_ED25519));
        assertTrue(PqcAlgorithms.knownOids().contains(SecurityIDs.ID_ML_DSA_65));
    }
}
