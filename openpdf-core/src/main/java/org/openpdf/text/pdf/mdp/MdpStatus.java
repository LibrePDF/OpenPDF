package org.openpdf.text.pdf.mdp;

/**
 * The result of the modification detection (DocMDP and FieldMDP) for the revisions that follow a signature.
 *
 * @since 3.0.6
 */
public enum MdpStatus {

    /**
     * The signature covers the whole document, so there is nothing to evaluate.
     */
    NO_CHANGES,

    /**
     * The document was changed after the signature and every change is allowed.
     */
    ALLOWED_CHANGES,

    /**
     * At least one change after the signature violates a restriction imposed by this signature.
     */
    UNAUTHORIZED_CHANGES,

    /**
     * The changes were not evaluated, for example because a signature is cryptographically invalid.
     */
    NOT_CHECKED
}
