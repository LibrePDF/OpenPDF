package org.openpdf.text.pdf.mdp;

import java.util.Objects;

/**
 * A finding of the validation that is not a change violation: structural problems, inconsistencies between the
 * {@code /Lock} of a field and the FieldMDP of its signature, or notes about the evaluation.
 *
 * @param severity the severity
 * @param code     the kind of finding
 * @param message  a human readable description
 * @since 3.0.6
 */
public record Diagnostic(Severity severity, Code code, String message) {

    /**
     * Creates a diagnostic.
     *
     * @param severity the severity
     * @param code     the kind of finding
     * @param message  a human readable description
     */
    public Diagnostic {
        Objects.requireNonNull(severity, "severity");
        Objects.requireNonNull(code, "code");
        Objects.requireNonNull(message, "message");
    }

    /**
     * The severity of a diagnostic.
     */
    public enum Severity {

        /**
         * Information only; it does not affect conformance.
         */
        INFO,

        /**
         * The document works but is not fully conformant, or its behaviour differs between viewers.
         */
        WARNING,

        /**
         * The structure is malformed.
         */
        ERROR
    }

    /**
     * The kind of finding.
     */
    public enum Code {

        /**
         * The {@code /ByteRange} does not describe a revision correctly.
         */
        BYTE_RANGE_INVALID,

        /**
         * The CMS container cannot be parsed.
         */
        CMS_UNREADABLE,

        /**
         * The {@code /Reference} array of a signature dictionary is malformed.
         */
        REFERENCE_MALFORMED,

        /**
         * A DocMDP transform is used by a signature that is not the first one.
         */
        DOCMDP_NOT_FIRST,

        /**
         * The catalog {@code /Perms /DocMDP} entry does not point to the certification signature.
         */
        PERMS_DOCMDP_MISMATCH,

        /**
         * A signed field has a {@code /Lock} but its signature has no FieldMDP transform; the lock is enforced.
         */
        LOCK_WITHOUT_FIELDMDP,

        /**
         * The FieldMDP transform parameters differ from the {@code /Lock} of the field; the transform is enforced.
         */
        FIELDMDP_LOCK_MISMATCH,

        /**
         * The FieldMDP transform parameters are malformed.
         */
        FIELDMDP_MALFORMED,

        /**
         * A lock permission tries to widen the permission in effect; the widening is ignored.
         */
        PERMISSION_WIDENING_IGNORED,

        /**
         * A lock permission is used in a document whose version is lower than PDF 2.0.
         */
        LOCK_PERMISSION_BEFORE_PDF_2_0,

        /**
         * A signature field was created after a certification; viewers handle this differently.
         */
        LATE_SIGNATURE_FIELD,

        /**
         * The document has no DocMDP or FieldMDP restriction, so later changes are not restricted.
         */
        NO_MDP_RESTRICTION,

        /**
         * The modification detection was not run.
         */
        MDP_NOT_CHECKED,

        /**
         * A revision of the document could not be read.
         */
        REVISION_UNREADABLE
    }
}
