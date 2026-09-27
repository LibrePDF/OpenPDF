package org.openpdf.text.pdf.mdp;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.openpdf.text.pdf.AcroFields;
import org.openpdf.text.pdf.LockPermission;
import org.openpdf.text.pdf.PdfArray;
import org.openpdf.text.pdf.PdfDictionary;
import org.openpdf.text.pdf.PdfIndirectReference;
import org.openpdf.text.pdf.PdfName;
import org.openpdf.text.pdf.PdfNumber;
import org.openpdf.text.pdf.PdfObject;
import org.openpdf.text.pdf.PdfPKCS7;
import org.openpdf.text.pdf.PdfReader;
import org.openpdf.text.pdf.PdfString;
import org.openpdf.text.pdf.RandomAccessFileOrArray;
import org.openpdf.text.pdf.SignatureFieldLock;

/**
 * Validates the signatures of a document revision by revision, reporting cryptographic integrity and MDP compliance
 * separately.
 *
 * <p>For each signature the validator checks the byte range and the CMS signature. It then reads the DocMDP and
 * FieldMDP restrictions of the signatures in signing order, compares each revision with the next one, and evaluates
 * every change against the restrictions in effect before that revision. A change that no rule explicitly allows is
 * reported as unauthorized.
 *
 * <p>Certificate trust, revocation and timestamps are not evaluated. Earlier revisions are read in memory.
 *
 * <pre>
 * try (PdfReader reader = new PdfReader("signed.pdf")) {
 *     DocumentValidationReport report = new SignatureValidator().validate(reader);
 *     for (SignatureValidationResult signature : report.signatures()) {
 *         System.out.println(signature.fieldName() + ": " + signature.status());
 *     }
 * }
 * </pre>
 *
 * @since 3.0.6
 */
public class SignatureValidator {

    private static final String PDF_2_0 = "2.0";
    private static final int EOF_WINDOW = 64;

    /**
     * Creates a validator.
     */
    public SignatureValidator() {
        super();
    }

    /**
     * Validates the signatures of a document with the default security provider.
     *
     * @param reader the document
     * @return the report
     */
    public DocumentValidationReport validate(PdfReader reader) {
        return validate(reader, null);
    }

    /**
     * Validates the signatures of a document.
     *
     * @param reader   the document
     * @param provider the security provider used to verify the CMS signatures, or {@code null} for the default
     * @return the report
     */
    public DocumentValidationReport validate(PdfReader reader, String provider) {
        AcroFields fields = reader.getAcroFields();
        List<String> names = fields.getSignedFieldNamesInRevisionOrder();
        List<Diagnostic> documentDiagnostics = new ArrayList<>();
        Map<String, List<Diagnostic>> diagnostics = new LinkedHashMap<>();
        Map<String, List<MdpViolation>> violations = new LinkedHashMap<>();
        Map<String, CryptoStatus> crypto = new LinkedHashMap<>();
        List<Restrictions> restrictions = new ArrayList<>();
        MdpState declared = new MdpState();
        for (int k = 0; k < names.size(); ++k) {
            String name = names.get(k);
            diagnostics.put(name, new ArrayList<>());
            violations.put(name, new ArrayList<>());
            crypto.put(name, checkCrypto(reader, fields, name, provider, diagnostics.get(name)));
            Restrictions restriction = readRestrictions(reader, fields, name, k, declared, diagnostics.get(name));
            restriction.applyTo(declared);
            restrictions.add(restriction);
        }

        boolean mdpChecked = checkModifications(reader, fields, names, restrictions, crypto, violations,
                diagnostics, documentDiagnostics);

        List<SignatureValidationResult> results = new ArrayList<>();
        for (String name : names) {
            boolean covers = fields.signatureCoversWholeDocument(name);
            List<MdpViolation> signatureViolations = violations.get(name);
            MdpStatus mdpStatus;
            if (!mdpChecked) {
                mdpStatus = MdpStatus.NOT_CHECKED;
            } else if (!signatureViolations.isEmpty()) {
                mdpStatus = MdpStatus.UNAUTHORIZED_CHANGES;
            } else {
                mdpStatus = covers ? MdpStatus.NO_CHANGES : MdpStatus.ALLOWED_CHANGES;
            }
            results.add(new SignatureValidationResult(name, fields.getRevision(name), covers, crypto.get(name),
                    mdpStatus, status(crypto.get(name), mdpStatus, covers, diagnostics.get(name)),
                    signatureViolations, diagnostics.get(name)));
        }
        return new DocumentValidationReport(results, documentDiagnostics);
    }

    private static SignatureStatus status(CryptoStatus crypto, MdpStatus mdp, boolean covers,
            List<Diagnostic> diagnostics) {
        if (crypto == CryptoStatus.INVALID) {
            return SignatureStatus.CRYPTO_INVALID;
        }
        if (crypto == CryptoStatus.MALFORMED
                || diagnostics.stream().anyMatch(d -> d.severity() == Diagnostic.Severity.ERROR)) {
            return SignatureStatus.MALFORMED;
        }
        if (mdp == MdpStatus.UNAUTHORIZED_CHANGES) {
            return SignatureStatus.UNAUTHORIZED_CHANGE;
        }
        if (mdp == MdpStatus.NOT_CHECKED) {
            return SignatureStatus.INDETERMINATE;
        }
        return covers ? SignatureStatus.VALID : SignatureStatus.VALID_WITH_ALLOWED_CHANGES;
    }

    private static CryptoStatus checkCrypto(PdfReader reader, AcroFields fields, String name, String provider,
            List<Diagnostic> diagnostics) {
        String problem = byteRangeProblem(reader, fields, name);
        if (problem != null) {
            diagnostics.add(new Diagnostic(Diagnostic.Severity.ERROR, Diagnostic.Code.BYTE_RANGE_INVALID, problem));
            return CryptoStatus.MALFORMED;
        }
        try {
            PdfPKCS7 pkcs7 = fields.verifySignature(name, provider);
            return pkcs7.verify() ? CryptoStatus.VALID : CryptoStatus.INVALID;
        } catch (Exception e) {
            diagnostics.add(new Diagnostic(Diagnostic.Severity.ERROR, Diagnostic.Code.CMS_UNREADABLE,
                    "The CMS signature cannot be read: " + e.getMessage()));
            return CryptoStatus.MALFORMED;
        }
    }

    private static String byteRangeProblem(PdfReader reader, AcroFields fields, String name) {
        long[] range = fields.getSignatureByteRange(name);
        PdfString contents = fields.getSignatureDictionary(name).getAsString(PdfName.CONTENTS);
        if (range == null || range.length != 4) {
            return "The /ByteRange must have four entries";
        }
        if (range[0] != 0 || range[1] <= 0 || range[2] <= range[1] || range[3] < 0) {
            return "The /ByteRange must start at offset 0 and describe two ordered ranges";
        }
        if (range[2] - range[1] != contents.getOriginalBytes().length * 2L + 2) {
            return "The gap of the /ByteRange does not match the /Contents entry";
        }
        long end = range[2] + range[3];
        if (end > reader.getFileLength()) {
            return "The /ByteRange ends after the end of the file";
        }
        try {
            if (!endsWithEof(reader, end)) {
                return "The /ByteRange does not end at the end of a revision (%%EOF)";
            }
        } catch (IOException e) {
            return "The end of the signed revision cannot be read: " + e.getMessage();
        }
        return null;
    }

    private static boolean endsWithEof(PdfReader reader, long end) throws IOException {
        RandomAccessFileOrArray file = reader.getSafeFile();
        try {
            file.reOpen();
            long start = Math.max(0, end - EOF_WINDOW);
            byte[] tail = new byte[(int) (end - start)];
            file.seek(start);
            file.readFully(tail);
            return new String(tail, StandardCharsets.ISO_8859_1).contains("%%EOF");
        } finally {
            file.close();
        }
    }

    /**
     * Reads the DocMDP and FieldMDP restrictions declared by a signature and reports structural problems.
     */
    private static Restrictions readRestrictions(PdfReader reader, AcroFields fields, String name, int index,
            MdpState declared, List<Diagnostic> diagnostics) {
        References references = readReferences(fields.getSignatureDictionary(name), diagnostics);
        boolean hasDocMdp = references.hasDocMdp();
        LockPermission docMdp = references.docMdp();
        if (hasDocMdp && index > 0) {
            diagnostics.add(error(Diagnostic.Code.DOCMDP_NOT_FIRST,
                    "A DocMDP transform is only allowed in the first signature; it is ignored"));
            hasDocMdp = false;
            docMdp = null;
        } else if (hasDocMdp) {
            checkPermsEntry(reader, fields, name, diagnostics);
        }

        SignatureFieldLock lock = resolveLock(fields, name, references.fieldMdp(), diagnostics);
        if (lock != null && lock.getPermission() != null) {
            checkLockPermission(reader, declared, docMdp, lock, diagnostics);
        }
        return new Restrictions(name, hasDocMdp, docMdp, lock);
    }

    /**
     * Reads the DocMDP and FieldMDP transforms of the /Reference array of a signature dictionary.
     */
    private static References readReferences(PdfDictionary signature, List<Diagnostic> diagnostics) {
        PdfObject referenceObject = PdfReader.getPdfObject(signature.get(PdfName.REFERENCE));
        if (referenceObject == null) {
            return References.NONE;
        }
        if (!referenceObject.isArray()) {
            diagnostics.add(error(Diagnostic.Code.REFERENCE_MALFORMED, "The /Reference entry is not an array"));
            return References.NONE;
        }
        boolean hasDocMdp = false;
        LockPermission docMdp = null;
        SignatureFieldLock fieldMdp = null;
        for (PdfObject element : ((PdfArray) referenceObject).getElements()) {
            PdfObject resolved = PdfReader.getPdfObject(element);
            PdfName method = resolved instanceof PdfDictionary reference
                    ? reference.getAsName(PdfName.TRANSFORMMETHOD)
                    : null;
            if (method == null) {
                diagnostics.add(error(Diagnostic.Code.REFERENCE_MALFORMED,
                        "A signature reference is not a dictionary with a /TransformMethod"));
                continue;
            }
            PdfDictionary params = ((PdfDictionary) resolved).getAsDict(PdfName.TRANSFORMPARAMS);
            if (PdfName.DOCMDP.equals(method)) {
                hasDocMdp = true;
                docMdp = readDocMdpPermission(params, diagnostics);
            } else if (PdfName.FIELDMDP.equals(method)) {
                SignatureFieldLock lock = readFieldMdp(params, diagnostics);
                if (lock != null) {
                    fieldMdp = lock;
                }
            }
        }
        return new References(hasDocMdp, docMdp, fieldMdp);
    }

    /**
     * Reads the transform parameters of a FieldMDP reference.
     *
     * @return the lock, or {@code null} if the parameters are malformed
     */
    private static SignatureFieldLock readFieldMdp(PdfDictionary params, List<Diagnostic> diagnostics) {
        SignatureFieldLock.ParseResult result = SignatureFieldLock.parse(params);
        if (result.lock() == null) {
            diagnostics.add(error(Diagnostic.Code.REFERENCE_MALFORMED,
                    "The FieldMDP transform parameters are malformed: " + result.problems()));
        } else if (!result.problems().isEmpty()) {
            diagnostics.add(warning(Diagnostic.Code.FIELDMDP_MALFORMED,
                    "The FieldMDP transform parameters have problems: " + result.problems()));
        }
        return result.lock();
    }

    /**
     * Chooses the lock to enforce: the FieldMDP transform if present, otherwise the /Lock of the field.
     */
    private static SignatureFieldLock resolveLock(AcroFields fields, String name, SignatureFieldLock fieldMdp,
            List<Diagnostic> diagnostics) {
        SignatureFieldLock fieldLock = fields.getSignatureFieldLock(name);
        if (fieldMdp == null && fieldLock != null) {
            diagnostics.add(warning(Diagnostic.Code.LOCK_WITHOUT_FIELDMDP,
                    "The field has a /Lock but the signature has no FieldMDP transform; the /Lock is enforced"));
            return fieldLock;
        }
        if (fieldMdp != null && fieldLock != null
                && (!fieldMdp.sameFields(fieldLock) || fieldMdp.getPermission() != fieldLock.getPermission())) {
            diagnostics.add(warning(Diagnostic.Code.FIELDMDP_LOCK_MISMATCH,
                    "The FieldMDP transform differs from the /Lock of the field; the transform is enforced"));
        }
        return fieldMdp;
    }

    /**
     * Reports a lock permission /P used before PDF 2.0, or wider than the permission already in effect.
     */
    private static void checkLockPermission(PdfReader reader, MdpState declared, LockPermission docMdp,
            SignatureFieldLock lock, List<Diagnostic> diagnostics) {
        if (!isAtLeastPdf20(reader)) {
            diagnostics.add(new Diagnostic(Diagnostic.Severity.INFO, Diagnostic.Code.LOCK_PERMISSION_BEFORE_PDF_2_0,
                    "The lock permission /P is a PDF 2.0 feature, but the document declares an older version"));
        }
        LockPermission inEffect = LockPermission.mostRestrictive(declared.effectivePermission(), docMdp);
        if (lock.getPermission().isWiderThan(inEffect)) {
            diagnostics.add(warning(Diagnostic.Code.PERMISSION_WIDENING_IGNORED, "The lock permission P="
                    + lock.getPermission().getCode() + " is wider than the permission P=" + inEffect.getCode()
                    + " already in effect; the widening is ignored"));
        }
    }

    private static LockPermission readDocMdpPermission(PdfDictionary params, List<Diagnostic> diagnostics) {
        PdfNumber p = params == null ? null : params.getAsNumber(PdfName.P);
        if (p == null) {
            return LockPermission.FORM_FILLING;
        }
        LockPermission permission = LockPermission.fromCode(p.intValue());
        if (permission == null) {
            diagnostics.add(warning(Diagnostic.Code.REFERENCE_MALFORMED,
                    "The DocMDP permission P=" + p.intValue() + " is not valid; no restriction is applied"));
        }
        return permission;
    }

    private static void checkPermsEntry(PdfReader reader, AcroFields fields, String name,
            List<Diagnostic> diagnostics) {
        PdfDictionary perms = reader.getCatalog().getAsDict(PdfName.PERMS);
        PdfObject docMdp = perms == null ? null : perms.get(PdfName.DOCMDP);
        PdfObject value = fields.getFieldItem(name).getMerged(0).get(PdfName.V);
        if (docMdp == null || !docMdp.isIndirect() || value == null || !value.isIndirect()
                || ((PdfIndirectReference) docMdp).getNumber() != ((PdfIndirectReference) value).getNumber()) {
            diagnostics.add(warning(Diagnostic.Code.PERMS_DOCMDP_MISMATCH,
                    "The catalog /Perms /DocMDP entry does not point to the certification signature"));
        }
    }

    private static boolean isAtLeastPdf20(PdfReader reader) {
        String header = reader.getPdfVersion();
        PdfName catalogVersion = reader.getCatalog().getAsName(PdfName.VERSION);
        return isAtLeast(header, PDF_2_0)
                || catalogVersion != null && isAtLeast(PdfName.decodeName(catalogVersion.toString()), PDF_2_0);
    }

    private static boolean isAtLeast(String version, String required) {
        if (version == null) {
            return false;
        }
        try {
            return Double.parseDouble(version) >= Double.parseDouble(required);
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Compares each signed revision with the next one and evaluates the changes.
     *
     * @return {@code true} if the modifications were evaluated
     */
    private static boolean checkModifications(PdfReader reader, AcroFields fields, List<String> names,
            List<Restrictions> restrictions, Map<String, CryptoStatus> crypto,
            Map<String, List<MdpViolation>> violations, Map<String, List<Diagnostic>> diagnostics,
            List<Diagnostic> documentDiagnostics) {
        if (names.isEmpty()) {
            return false;
        }
        if (reader.isEncrypted()) {
            documentDiagnostics.add(warning(Diagnostic.Code.MDP_NOT_CHECKED,
                    "Modification detection is not supported for encrypted documents"));
            return false;
        }
        if (crypto.values().stream().anyMatch(status -> status != CryptoStatus.VALID)) {
            documentDiagnostics.add(warning(Diagnostic.Code.MDP_NOT_CHECKED,
                    "Modification detection needs every signature to be cryptographically valid"));
            return false;
        }
        List<PdfReader> revisions = new ArrayList<>();
        try {
            for (String name : names) {
                revisions.add(new PdfReader(fields.extractRevision(name)));
            }
            MdpState state = new MdpState();
            boolean changed = false;
            for (int k = 0; k < names.size(); ++k) {
                restrictions.get(k).applyTo(state);
                PdfReader newer;
                if (k + 1 < names.size()) {
                    newer = revisions.get(k + 1);
                } else if (!fields.signatureCoversWholeDocument(names.get(k))) {
                    newer = reader;
                } else {
                    break;
                }
                List<Change> changes = RevisionDiff.compare(revisions.get(k), newer);
                changed |= !changes.isEmpty();
                MdpPolicy.evaluate(changes, state, k + 2, violations, diagnostics);
            }
            if (changed && state.isEmpty()) {
                documentDiagnostics.add(new Diagnostic(Diagnostic.Severity.INFO, Diagnostic.Code.NO_MDP_RESTRICTION,
                        "No signature restricts later changes with DocMDP or FieldMDP"));
            }
            return true;
        } catch (Exception e) {
            documentDiagnostics.add(error(Diagnostic.Code.REVISION_UNREADABLE,
                    "A revision of the document cannot be compared: " + e.getMessage()));
            return false;
        } finally {
            for (PdfReader revision : revisions) {
                revision.close();
            }
        }
    }

    private static Diagnostic error(Diagnostic.Code code, String message) {
        return new Diagnostic(Diagnostic.Severity.ERROR, code, message);
    }

    private static Diagnostic warning(Diagnostic.Code code, String message) {
        return new Diagnostic(Diagnostic.Severity.WARNING, code, message);
    }

    /**
     * The DocMDP and FieldMDP transforms found in the /Reference array of a signature.
     */
    private record References(boolean hasDocMdp, LockPermission docMdp, SignatureFieldLock fieldMdp) {

        static final References NONE = new References(false, null, null);
    }

    /**
     * The restrictions declared by one signature.
     */
    private record Restrictions(String signatureName, boolean hasDocMdp, LockPermission docMdp,
            SignatureFieldLock lock) {

        void applyTo(MdpState state) {
            if (hasDocMdp && docMdp != null) {
                state.addDocMdp(signatureName, docMdp);
            }
            if (lock != null) {
                state.addLock(signatureName, lock);
            }
        }
    }
}
