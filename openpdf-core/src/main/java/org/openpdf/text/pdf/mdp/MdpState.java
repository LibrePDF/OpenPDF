package org.openpdf.text.pdf.mdp;

import java.util.ArrayList;
import java.util.List;
import org.openpdf.text.pdf.LockPermission;
import org.openpdf.text.pdf.SignatureFieldLock;

/**
 * The MDP restrictions accumulated by the signatures up to a revision. Each restriction is kept with the signature
 * that imposed it, so a violation can be attributed to that signature.
 */
final class MdpState {

    private final List<Constraint> constraints = new ArrayList<>();

    /**
     * Adds the DocMDP restriction of a certification signature.
     */
    void addDocMdp(String signatureName, LockPermission permission) {
        constraints.add(new Constraint(signatureName, permission, null, true));
    }

    /**
     * Adds the FieldMDP restriction of a signature, including its PDF 2.0 permission if any.
     */
    void addLock(String signatureName, SignatureFieldLock lock) {
        constraints.add(new Constraint(signatureName, lock.getPermission(), lock, false));
    }

    /**
     * Returns the most restrictive permission in effect, {@code null} if there is none.
     */
    LockPermission effectivePermission() {
        LockPermission permission = null;
        for (Constraint constraint : constraints) {
            permission = LockPermission.mostRestrictive(permission, constraint.permission());
        }
        return permission;
    }

    boolean hasDocMdp() {
        return constraints.stream().anyMatch(Constraint::docMdp);
    }

    boolean isEmpty() {
        return constraints.isEmpty();
    }

    List<Constraint> constraints() {
        return constraints;
    }

    /**
     * One restriction.
     *
     * @param signatureName the signature that imposed it
     * @param permission    the permission it imposes, or {@code null}
     * @param lock          the locked fields, or {@code null} for a DocMDP restriction
     * @param docMdp        {@code true} for the DocMDP restriction of a certification
     */
    record Constraint(String signatureName, LockPermission permission, SignatureFieldLock lock, boolean docMdp) {

        String describePermission() {
            return (docMdp ? "DocMDP" : "lock") + " P=" + permission.getCode() + " of " + signatureName;
        }
    }
}
