package org.openpdf.text.pdf.mdp;

import org.openpdf.text.pdf.LockPermission;

/**
 * The lowest MDP permission under which a change is allowed.
 */
enum Allowance {

    /**
     * Allowed at every permission level: DSS updates, document timestamps, metadata and version updates.
     */
    ALWAYS,

    /**
     * Filling in an existing form field.
     */
    FORM_FILLING,

    /**
     * Signing an existing, empty signature field.
     */
    SIGNING,

    /**
     * Creating a new signature field; allowed with form filling, but viewers behave differently.
     */
    NEW_SIGNATURE_FIELD,

    /**
     * Creating, changing or deleting an annotation that is not a widget.
     */
    ANNOTATIONS,

    /**
     * Never allowed once a permission is in effect.
     */
    NEVER;

    /**
     * Tells if a change with this allowance is permitted by a permission.
     *
     * @param permission the permission in effect, {@code null} meaning no restriction
     * @return {@code true} if the change is allowed
     */
    boolean isAllowedBy(LockPermission permission) {
        if (permission == null || this == ALWAYS) {
            return true;
        }
        return switch (this) {
            case FORM_FILLING, SIGNING, NEW_SIGNATURE_FIELD -> permission.getCode() >= 2;
            case ANNOTATIONS -> permission.getCode() >= 3;
            default -> false;
        };
    }
}
