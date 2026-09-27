package org.openpdf.text.pdf.mdp;

/**
 * The part of the document affected by a change between two revisions.
 *
 * @since 3.0.6
 */
public enum ChangeRole {

    /**
     * An entry of the document catalog not covered by a more specific role.
     */
    CATALOG,

    /**
     * An entry of the interactive form dictionary.
     */
    ACROFORM,

    /**
     * A form field entry, such as its value or its flags.
     */
    FIELD,

    /**
     * A widget annotation entry of a form field, such as its appearance.
     */
    WIDGET,

    /**
     * The signature dictionary of a signature that was already present.
     */
    SIGNATURE_DICTIONARY,

    /**
     * A page, or the page tree.
     */
    PAGE,

    /**
     * An annotation that is not a widget.
     */
    ANNOTATION,

    /**
     * The document security store.
     */
    DSS,

    /**
     * The {@code /Perms} dictionary of the catalog.
     */
    PERMS,

    /**
     * The XMP metadata stream.
     */
    METADATA
}
