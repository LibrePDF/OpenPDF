package org.openpdf.text.pdf.mdp;

import java.util.List;
import java.util.Optional;

/**
 * The validation report of a document: one result per signature, in revision order, plus the findings that concern
 * the document as a whole.
 *
 * @param signatures  the signature results, in revision order
 * @param diagnostics the document level findings
 * @since 3.0.6
 */
public record DocumentValidationReport(List<SignatureValidationResult> signatures, List<Diagnostic> diagnostics) {

    /**
     * Creates a report.
     *
     * @param signatures  the signature results, in revision order
     * @param diagnostics the document level findings
     */
    public DocumentValidationReport {
        signatures = List.copyOf(signatures);
        diagnostics = List.copyOf(diagnostics);
    }

    /**
     * Returns the result of a signature.
     *
     * @param fieldName the signature field name
     * @return the result, if the field is signed
     */
    public Optional<SignatureValidationResult> getSignature(String fieldName) {
        return signatures.stream().filter(s -> s.fieldName().equals(fieldName)).findFirst();
    }

    /**
     * Tells if every signature is fully conformant.
     *
     * @return {@code true} if the document has signatures and all of them are fully conformant
     */
    public boolean isFullyConformant() {
        return !signatures.isEmpty() && signatures.stream().allMatch(SignatureValidationResult::isFullyConformant)
                && diagnostics.stream().allMatch(d -> d.severity() == Diagnostic.Severity.INFO);
    }
}
