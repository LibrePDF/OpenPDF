package org.openpdf.text.pdf.mdp;

/**
 * The result of the cryptographic part of a signature validation: CMS signature, digest over the byte range and
 * byte range structure. Certificate trust is not evaluated.
 *
 * @since 3.0.6
 */
public enum CryptoStatus {

    /**
     * The CMS signature is valid and matches the bytes covered by the byte range.
     */
    VALID,

    /**
     * The CMS signature is not valid or does not match the covered bytes.
     */
    INVALID,

    /**
     * The byte range or the CMS container is malformed, so the signature could not be checked.
     */
    MALFORMED
}
