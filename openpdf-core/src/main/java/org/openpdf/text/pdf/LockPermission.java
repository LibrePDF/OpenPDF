package org.openpdf.text.pdf;

/**
 * MDP access permissions, as used by the {@code /P} entry of the DocMDP transform parameters and, since PDF 2.0,
 * of the signature field lock dictionary.
 *
 * <p>The numeric values are ordered from the most restrictive to the most permissive, so a smaller value is a
 * stronger restriction.
 *
 * @see SignatureFieldLock
 */
public enum LockPermission {

    /**
     * No changes to the document are permitted ({@code /P 1}).
     */
    NO_CHANGES(PdfSignatureAppearance.CERTIFIED_NO_CHANGES_ALLOWED),

    /**
     * Filling in forms, instantiating page templates and signing are permitted ({@code /P 2}).
     */
    FORM_FILLING(PdfSignatureAppearance.CERTIFIED_FORM_FILLING),

    /**
     * Same as {@link #FORM_FILLING}, plus annotation creation, deletion and modification ({@code /P 3}).
     */
    FORM_FILLING_AND_ANNOTATIONS(PdfSignatureAppearance.CERTIFIED_FORM_FILLING_AND_ANNOTATIONS);

    private final int code;

    LockPermission(int code) {
        this.code = code;
    }

    /**
     * Returns the permission for a {@code /P} value.
     *
     * @param code the {@code /P} value
     * @return the matching permission or {@code null} if the value is not 1, 2 or 3
     */
    public static LockPermission fromCode(int code) {
        for (LockPermission permission : values()) {
            if (permission.code == code) {
                return permission;
            }
        }
        return null;
    }

    /**
     * Returns the more restrictive of two permissions. A {@code null} argument means "no restriction".
     *
     * @param a a permission or {@code null}
     * @param b a permission or {@code null}
     * @return the more restrictive permission, or {@code null} if both are {@code null}
     */
    public static LockPermission mostRestrictive(LockPermission a, LockPermission b) {
        if (a == null) {
            return b;
        }
        if (b == null) {
            return a;
        }
        return a.code <= b.code ? a : b;
    }

    /**
     * Returns the {@code /P} value.
     *
     * @return 1, 2 or 3
     */
    public int getCode() {
        return code;
    }

    /**
     * Tells if this permission allows more than {@code other}.
     *
     * @param other the permission to compare with, {@code null} meaning "no restriction"
     * @return {@code true} if this permission is less restrictive than {@code other}
     */
    public boolean isWiderThan(LockPermission other) {
        return other != null && code > other.code;
    }
}
