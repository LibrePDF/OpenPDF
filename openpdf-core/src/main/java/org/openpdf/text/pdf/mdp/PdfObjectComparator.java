package org.openpdf.text.pdf.mdp;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.openpdf.text.pdf.PRStream;
import org.openpdf.text.pdf.PdfArray;
import org.openpdf.text.pdf.PdfDictionary;
import org.openpdf.text.pdf.PdfIndirectReference;
import org.openpdf.text.pdf.PdfName;
import org.openpdf.text.pdf.PdfNumber;
import org.openpdf.text.pdf.PdfObject;
import org.openpdf.text.pdf.PdfReader;
import org.openpdf.text.pdf.PdfString;

/**
 * Compares PDF objects of two revisions by content.
 *
 * <p>Indirect references are followed, except for the "tracked" objects (pages, annotations, fields, signature
 * dictionaries...) which the revision diff compares on their own: references to those are equal only when they
 * point to the same object number. {@code /Parent} entries are ignored, since they only link back to the structure
 * that is compared elsewhere. Streams are compared by dictionary and decoded content.
 */
final class PdfObjectComparator {

    private static final Set<PdfName> IGNORED_STREAM_KEYS = Set.of(PdfName.LENGTH, PdfName.FILTER,
            PdfName.DECODEPARMS);

    private final Set<Integer> identityOnly;
    private final Set<Long> assumedEqual = new HashSet<>();

    PdfObjectComparator(Set<Integer> identityOnly) {
        this.identityOnly = identityOnly;
    }

    /**
     * Tells whether two objects are equivalent, following indirect references.
     */
    boolean equivalent(PdfObject a, PdfObject b) {
        if (a == b) {
            return true;
        }
        if (a == null || b == null) {
            return isNull(a) && isNull(b);
        }
        if (a.isIndirect() && b.isIndirect()) {
            return referencesEquivalent((PdfIndirectReference) a, (PdfIndirectReference) b);
        }
        if (isIdentityOnly(a) || isIdentityOnly(b)) {
            return false;
        }
        return contentEqual(PdfReader.getPdfObject(a), PdfReader.getPdfObject(b));
    }

    /**
     * Compares two indirect references: by object number for tracked objects, by content for the others.
     */
    private boolean referencesEquivalent(PdfIndirectReference ra, PdfIndirectReference rb) {
        if (identityOnly.contains(ra.getNumber()) || identityOnly.contains(rb.getNumber())) {
            return ra.getNumber() == rb.getNumber();
        }
        long pair = ((long) ra.getNumber() << 32) | (rb.getNumber() & 0xffffffffL);
        if (!assumedEqual.add(pair)) {
            // already being compared higher in the stack: assume equal to break cycles
            return true;
        }
        boolean result = contentEqual(PdfReader.getPdfObject(ra), PdfReader.getPdfObject(rb));
        if (!result) {
            assumedEqual.remove(pair);
        }
        return result;
    }

    private boolean isIdentityOnly(PdfObject object) {
        return object.isIndirect() && identityOnly.contains(((PdfIndirectReference) object).getNumber());
    }

    /**
     * Compares two resolved objects. For dictionaries, the object itself is compared by content even when it is a
     * tracked object; only the references it contains are subject to the identity rule.
     */
    boolean contentEqual(PdfObject a, PdfObject b) {
        if (a == b) {
            return true;
        }
        if (isNull(a) || isNull(b)) {
            return isNull(a) && isNull(b);
        }
        if (a.type() != b.type()) {
            return false;
        }
        switch (a.type()) {
            case PdfObject.DICTIONARY:
                return dictionaryEqual((PdfDictionary) a, (PdfDictionary) b, Set.of());
            case PdfObject.STREAM:
                return streamEqual((PRStream) a, (PRStream) b);
            case PdfObject.ARRAY:
                return arrayEqual((PdfArray) a, (PdfArray) b);
            case PdfObject.NUMBER:
                return ((PdfNumber) a).doubleValue() == ((PdfNumber) b).doubleValue();
            case PdfObject.STRING:
                return Arrays.equals(((PdfString) a).getBytes(), ((PdfString) b).getBytes());
            default:
                return a.equals(b) || Arrays.equals(a.getBytes(), b.getBytes());
        }
    }

    /**
     * Compares two dictionaries entry by entry, skipping {@code /Parent} and the given keys.
     */
    boolean dictionaryEqual(PdfDictionary a, PdfDictionary b, Set<PdfName> ignoredKeys) {
        Set<PdfName> keys = new HashSet<>(a.getKeys());
        keys.addAll(b.getKeys());
        for (PdfName key : keys) {
            if (PdfName.PARENT.equals(key) || ignoredKeys.contains(key)) {
                continue;
            }
            if (!equivalent(a.get(key), b.get(key))) {
                return false;
            }
        }
        return true;
    }

    private boolean arrayEqual(PdfArray a, PdfArray b) {
        if (a.size() != b.size()) {
            return false;
        }
        for (int k = 0; k < a.size(); ++k) {
            if (!equivalent(a.getPdfObject(k), b.getPdfObject(k))) {
                return false;
            }
        }
        return true;
    }

    private boolean streamEqual(PRStream a, PRStream b) {
        if (!dictionaryEqual(a, b, IGNORED_STREAM_KEYS)) {
            return false;
        }
        try {
            if (Arrays.equals(PdfReader.getStreamBytesRaw(a), PdfReader.getStreamBytesRaw(b))) {
                return true;
            }
            return Arrays.equals(PdfReader.getStreamBytes(a), PdfReader.getStreamBytes(b));
        } catch (IOException e) {
            return false;
        }
    }

    private static boolean isNull(PdfObject object) {
        return object == null || object.isNull();
    }
}
