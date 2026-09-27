package org.openpdf.text.pdf.mdp;

/**
 * One semantic change between two revisions.
 *
 * @param role         the part of the document that changed
 * @param kind         what happened to it
 * @param fieldName    the fully qualified name of the affected field, or {@code null}
 * @param key          the changed dictionary key, or {@code null}
 * @param allowance    the lowest permission under which the change is allowed
 * @param lockRelevant {@code true} if a FieldMDP lock on {@code fieldName} forbids the change
 * @param description  a human readable description
 */
record Change(ChangeRole role, Kind kind, String fieldName, String key, Allowance allowance, boolean lockRelevant,
        String description) {

    /**
     * What happened to the changed part.
     */
    enum Kind {

        /**
         * The part was added.
         */
        ADDED,

        /**
         * The part was removed.
         */
        REMOVED,

        /**
         * The part was modified.
         */
        MODIFIED
    }
}
