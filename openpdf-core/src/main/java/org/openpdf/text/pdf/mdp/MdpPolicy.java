package org.openpdf.text.pdf.mdp;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Evaluates the changes of one revision against the MDP restrictions in effect before it.
 *
 * <p>Every restriction is checked on its own: a change violates the DocMDP or lock permission of each signature whose
 * permission does not allow it, and the FieldMDP lock of each signature that locks the affected field. Checking each
 * permission independently is what makes a later attempt to widen a permission ineffective.
 */
final class MdpPolicy {

    private MdpPolicy() {
    }

    /**
     * Evaluates the changes of a revision.
     *
     * @param changes     the changes of the revision
     * @param state       the restrictions in effect before the revision
     * @param revision    the number of the revision
     * @param violations  receives the violations, by the name of the signature they invalidate
     * @param diagnostics receives the diagnostics, by signature name
     */
    static void evaluate(List<Change> changes, MdpState state, int revision,
            Map<String, List<MdpViolation>> violations, Map<String, List<Diagnostic>> diagnostics) {
        for (Change change : changes) {
            if (change.role() == ChangeRole.SIGNATURE_DICTIONARY) {
                // a signature dictionary is never edited after signing, whatever the permissions
                add(violations, change.fieldName(), violation(revision, change, "signature integrity"));
                continue;
            }
            for (MdpState.Constraint constraint : state.constraints()) {
                if (constraint.permission() != null && !change.allowance().isAllowedBy(constraint.permission())) {
                    add(violations, constraint.signatureName(),
                            violation(revision, change, constraint.describePermission()));
                } else if (constraint.docMdp() && change.allowance() == Allowance.NEW_SIGNATURE_FIELD) {
                    add(diagnostics, constraint.signatureName(), new Diagnostic(Diagnostic.Severity.WARNING,
                            Diagnostic.Code.LATE_SIGNATURE_FIELD, "Revision " + revision + ": the signature field "
                            + change.fieldName() + " was created after the certification"));
                }
                if (constraint.lock() != null && change.lockRelevant() && change.fieldName() != null
                        && change.kind() != Change.Kind.ADDED && constraint.lock().locks(change.fieldName())) {
                    add(violations, constraint.signatureName(),
                            violation(revision, change, "FieldMDP lock of " + constraint.signatureName()));
                }
            }
        }
    }

    private static MdpViolation violation(int revision, Change change, String constraint) {
        String message = change.description() + (change.key() == null ? "" : " (/" + change.key() + ")");
        return new MdpViolation(revision, change.role(), change.fieldName(), constraint, message);
    }

    private static <T> void add(Map<String, List<T>> map, String signatureName, T value) {
        List<T> values = map.computeIfAbsent(signatureName, name -> new ArrayList<>());
        if (!values.contains(value)) {
            values.add(value);
        }
    }
}
