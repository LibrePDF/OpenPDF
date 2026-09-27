package org.openpdf.text.pdf.mdp;

/**
 * The overall outcome of the validation of one signature. The constants are listed from the most to the least
 * severe; when several apply, the most severe one is reported.
 *
 * @since 3.0.6
 */
public enum SignatureStatus {

    /**
     * The signature does not match the signed bytes, or the CMS signature is not valid.
     */
    CRYPTO_INVALID,

    /**
     * The signature or its related structures (byte range, references, locks) are malformed.
     */
    MALFORMED,

    /**
     * The signature is intact, but a later revision contains a change that the MDP rules in effect forbid.
     */
    UNAUTHORIZED_CHANGE,

    /**
     * The signature is intact, but the later changes could not be evaluated.
     */
    INDETERMINATE,

    /**
     * The signature is intact and every later change is allowed by the MDP rules in effect.
     */
    VALID_WITH_ALLOWED_CHANGES,

    /**
     * The signature is intact and covers the whole document.
     */
    VALID
}
