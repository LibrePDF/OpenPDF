package org.openpdf.text.pdf.mdp;

import java.util.Objects;

/**
 * A change that violates a restriction imposed by a signature.
 *
 * @param revision   the revision that introduced the change; signature revisions are numbered from 1 in signing
 *                   order, and changes after the last signature belong to the revision that follows it
 * @param role       the part of the document that changed
 * @param fieldName  the fully qualified name of the affected field, or {@code null}
 * @param constraint a description of the violated restriction, for example {@code "DocMDP P=2"}
 * @param message    a description of the change
 * @since 3.0.6
 */
public record MdpViolation(int revision, ChangeRole role, String fieldName, String constraint, String message) {

    /**
     * Creates a violation.
     *
     * @param revision   the revision that introduced the change
     * @param role       the part of the document that changed
     * @param fieldName  the fully qualified name of the affected field, or {@code null}
     * @param constraint a description of the violated restriction
     * @param message    a description of the change
     */
    public MdpViolation {
        Objects.requireNonNull(role, "role");
        Objects.requireNonNull(constraint, "constraint");
        Objects.requireNonNull(message, "message");
    }
}
