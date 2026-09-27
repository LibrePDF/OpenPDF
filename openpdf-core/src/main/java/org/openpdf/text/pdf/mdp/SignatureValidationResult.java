package org.openpdf.text.pdf.mdp;

import java.util.List;
import java.util.Objects;

/**
 * The validation result of one signature. Cryptographic integrity and modification detection are reported
 * separately: an intact signature does not imply that every later change was allowed.
 *
 * @param fieldName           the signature field name
 * @param revision            the revision of the signature, starting from 1
 * @param coversWholeDocument {@code true} if the signature covers the whole file
 * @param cryptoStatus        the cryptographic result
 * @param mdpStatus           the modification detection result
 * @param status              the overall result
 * @param violations          the changes that violate a restriction imposed by this signature
 * @param diagnostics         the other findings about this signature
 * @since 3.0.6
 */
public record SignatureValidationResult(String fieldName, int revision, boolean coversWholeDocument,
        CryptoStatus cryptoStatus, MdpStatus mdpStatus, SignatureStatus status, List<MdpViolation> violations,
        List<Diagnostic> diagnostics) {

    /**
     * Creates a result.
     *
     * @param fieldName           the signature field name
     * @param revision            the revision of the signature
     * @param coversWholeDocument {@code true} if the signature covers the whole file
     * @param cryptoStatus        the cryptographic result
     * @param mdpStatus           the modification detection result
     * @param status              the overall result
     * @param violations          the violations
     * @param diagnostics         the diagnostics
     */
    public SignatureValidationResult {
        Objects.requireNonNull(fieldName, "fieldName");
        Objects.requireNonNull(cryptoStatus, "cryptoStatus");
        Objects.requireNonNull(mdpStatus, "mdpStatus");
        Objects.requireNonNull(status, "status");
        violations = List.copyOf(violations);
        diagnostics = List.copyOf(diagnostics);
    }

    /**
     * Tells if the signature is valid and fully conformant: the status is {@link SignatureStatus#VALID} or
     * {@link SignatureStatus#VALID_WITH_ALLOWED_CHANGES}, and there is no diagnostic of severity
     * {@link Diagnostic.Severity#WARNING} or higher.
     *
     * @return {@code true} if the signature is fully conformant
     */
    public boolean isFullyConformant() {
        if (status != SignatureStatus.VALID && status != SignatureStatus.VALID_WITH_ALLOWED_CHANGES) {
            return false;
        }
        return diagnostics.stream().allMatch(d -> d.severity() == Diagnostic.Severity.INFO);
    }

    /**
     * Tells if a diagnostic with the given code was reported for this signature.
     *
     * @param code the diagnostic code
     * @return {@code true} if such a diagnostic exists
     */
    public boolean hasDiagnostic(Diagnostic.Code code) {
        return diagnostics.stream().anyMatch(d -> d.code() == code);
    }
}
