package org.openpdf.text.pdf;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.openpdf.text.error_messages.MessageLocalization;

/**
 * The set of form fields a signature field locks once it is signed (ISO 32000, 12.7.5.5, signature field lock
 * dictionary).
 *
 * <p>The lock is a property of the (unsigned) signature field, written as its {@code /Lock} entry. When the field is
 * signed, {@link PdfSignatureAppearance} adds the matching FieldMDP transform to the signature {@code /Reference}.
 *
 * <p>Field names are fully qualified. A name also locks the descendants of that field, so {@code "a"} locks
 * {@code "a.b"}.
 *
 * <p>Instances are immutable.
 *
 * @since 3.0.6
 */
public final class SignatureFieldLock {

    private static final PdfName TRANSFORM_VERSION = new PdfName("1.2");

    private final LockAction action;
    private final List<String> fieldNames;
    private final LockPermission permission;

    private SignatureFieldLock(LockAction action, List<String> fieldNames, LockPermission permission) {
        this.action = action;
        this.fieldNames = Collections.unmodifiableList(new ArrayList<>(fieldNames));
        this.permission = permission;
    }

    /**
     * Creates a lock on all the fields of the document.
     *
     * @return the lock
     */
    public static SignatureFieldLock all() {
        return new SignatureFieldLock(LockAction.ALL, Collections.emptyList(), null);
    }

    /**
     * Creates a lock on the listed fields only.
     *
     * @param fieldNames the fully qualified names of the fields to lock
     * @return the lock
     * @throws IllegalArgumentException if no name is given, or a name is blank, malformed or duplicated
     */
    public static SignatureFieldLock include(String... fieldNames) {
        return of(LockAction.INCLUDE, Arrays.asList(requireNames(fieldNames)), null);
    }

    /**
     * Creates a lock on all the fields except the listed ones.
     *
     * @param fieldNames the fully qualified names of the fields that stay unlocked
     * @return the lock
     * @throws IllegalArgumentException if no name is given, or a name is blank, malformed or duplicated
     */
    public static SignatureFieldLock exclude(String... fieldNames) {
        return of(LockAction.EXCLUDE, Arrays.asList(requireNames(fieldNames)), null);
    }

    /**
     * Creates a lock from its parts.
     *
     * @param action     the lock action
     * @param fieldNames the fully qualified field names, empty for {@link LockAction#ALL}
     * @param permission the PDF 2.0 {@code /P} permission, or {@code null} to omit it
     * @return the lock
     * @throws IllegalArgumentException if the names do not match the action, or a name is blank, malformed or
     *                                  duplicated
     */
    public static SignatureFieldLock of(LockAction action, Collection<String> fieldNames,
            LockPermission permission) {
        Objects.requireNonNull(action, "action");
        Objects.requireNonNull(fieldNames, "fieldNames");
        if (action == LockAction.ALL && !fieldNames.isEmpty()) {
            throw new IllegalArgumentException(
                    MessageLocalization.getComposedMessage("lock.action.all.does.not.accept.field.names"));
        }
        if (action != LockAction.ALL && fieldNames.isEmpty()) {
            throw new IllegalArgumentException(
                    MessageLocalization.getComposedMessage("lock.action.1.requires.field.names", action));
        }
        Set<String> unique = new LinkedHashSet<>();
        for (String name : fieldNames) {
            if (!isValidFieldName(name)) {
                throw new IllegalArgumentException(
                        MessageLocalization.getComposedMessage("invalid.lock.field.name.1", name));
            }
            if (!unique.add(name)) {
                throw new IllegalArgumentException(
                        MessageLocalization.getComposedMessage("duplicate.lock.field.name.1", name));
            }
        }
        return new SignatureFieldLock(action, new ArrayList<>(unique), permission);
    }

    /**
     * Reads a signature field lock dictionary, or the transform parameters dictionary of a FieldMDP reference.
     *
     * <p>Parsing is lenient: problems are reported in the result rather than thrown. The lock is {@code null} only
     * when the action is missing or invalid.
     *
     * @param dictionary the dictionary to read, may be {@code null}
     * @return the parse result
     */
    public static ParseResult parse(PdfDictionary dictionary) {
        List<String> problems = new ArrayList<>();
        if (dictionary == null) {
            problems.add("missing dictionary");
            return new ParseResult(null, problems);
        }
        PdfObject actionObject = PdfReader.getPdfObjectRelease(dictionary.get(PdfName.ACTION));
        LockAction action = actionObject != null && actionObject.isName()
                ? LockAction.fromPdfName((PdfName) actionObject)
                : null;
        if (action == null) {
            problems.add(actionObject == null ? "missing /Action" : "invalid /Action " + actionObject);
            return new ParseResult(null, problems);
        }
        List<String> names = new ArrayList<>();
        PdfObject fieldsObject = PdfReader.getPdfObjectRelease(dictionary.get(PdfName.FIELDS));
        if (fieldsObject != null && !fieldsObject.isArray()) {
            problems.add("/Fields is not an array");
        } else if (fieldsObject != null) {
            readFieldNames((PdfArray) fieldsObject, names, problems);
        }
        if (action == LockAction.ALL && !names.isEmpty()) {
            problems.add("/Fields is ignored for /Action /All");
            names.clear();
        } else if (action != LockAction.ALL && fieldsObject == null) {
            problems.add("missing /Fields for /Action " + action.getPdfName());
        }
        LockPermission permission = readPermission(dictionary, problems);
        return new ParseResult(new SignatureFieldLock(action, names, permission), problems);
    }

    /**
     * Adds the valid, distinct names of a /Fields array to {@code names}, reporting the invalid entries.
     */
    private static void readFieldNames(PdfArray fields, List<String> names, List<String> problems) {
        for (PdfObject element : fields.getElements()) {
            PdfObject resolved = PdfReader.getPdfObjectRelease(element);
            if (resolved == null || !resolved.isString()) {
                problems.add("/Fields contains a non string entry");
                continue;
            }
            String name = ((PdfString) resolved).toUnicodeString();
            if (!isValidFieldName(name)) {
                problems.add("/Fields contains the invalid name '" + name + "'");
            } else if (!names.contains(name)) {
                names.add(name);
            }
        }
    }

    /**
     * Reads the lock permission /P.
     *
     * @return the permission, or {@code null} if /P is missing or invalid
     */
    private static LockPermission readPermission(PdfDictionary dictionary, List<String> problems) {
        PdfObject permissionObject = PdfReader.getPdfObjectRelease(dictionary.get(PdfName.P));
        if (permissionObject == null) {
            return null;
        }
        LockPermission permission = permissionObject.isNumber()
                ? LockPermission.fromCode(((PdfNumber) permissionObject).intValue())
                : null;
        if (permission == null) {
            problems.add("invalid /P " + permissionObject);
        }
        return permission;
    }

    private static String[] requireNames(String[] fieldNames) {
        Objects.requireNonNull(fieldNames, "fieldNames");
        return fieldNames;
    }

    private static boolean isValidFieldName(String name) {
        if (name == null || name.isBlank()) {
            return false;
        }
        for (String part : name.split("\\.", -1)) {
            if (part.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns a copy of this lock with the given PDF 2.0 {@code /P} permission.
     *
     * @param permission the permission, or {@code null} to remove it
     * @return the new lock
     */
    public SignatureFieldLock withPermission(LockPermission permission) {
        return new SignatureFieldLock(action, fieldNames, permission);
    }

    /**
     * Returns the lock action.
     *
     * @return the action
     */
    public LockAction getAction() {
        return action;
    }

    /**
     * Returns the fully qualified field names of the lock.
     *
     * @return an unmodifiable list, empty for {@link LockAction#ALL}
     */
    public List<String> getFieldNames() {
        return fieldNames;
    }

    /**
     * Returns the PDF 2.0 {@code /P} permission of the lock.
     *
     * @return the permission or {@code null} if the lock does not restrict the permissions
     */
    public LockPermission getPermission() {
        return permission;
    }

    /**
     * Tells if this lock applies to a field.
     *
     * @param fullyQualifiedName the fully qualified name of the field
     * @return {@code true} if the field is locked
     */
    public boolean locks(String fullyQualifiedName) {
        return switch (action) {
            case ALL -> true;
            case INCLUDE -> matchesAny(fullyQualifiedName);
            case EXCLUDE -> !matchesAny(fullyQualifiedName);
        };
    }

    private boolean matchesAny(String fullyQualifiedName) {
        for (String name : fieldNames) {
            if (fullyQualifiedName.equals(name) || fullyQualifiedName.startsWith(name + ".")) {
                return true;
            }
        }
        return false;
    }

    /**
     * Builds the signature field lock dictionary ({@code /Type /SigFieldLock}).
     *
     * @return a new dictionary
     */
    public PdfDictionary toPdfDictionary() {
        PdfDictionary dictionary = new PdfDictionary(PdfName.SIGFIELDLOCK);
        putEntries(dictionary);
        return dictionary;
    }

    /**
     * Builds the transform parameters dictionary of the FieldMDP reference matching this lock.
     *
     * @return a new dictionary
     */
    public PdfDictionary toTransformParams() {
        PdfDictionary dictionary = new PdfDictionary(PdfName.TRANSFORMPARAMS);
        putEntries(dictionary);
        dictionary.put(PdfName.V, TRANSFORM_VERSION);
        return dictionary;
    }

    private void putEntries(PdfDictionary dictionary) {
        dictionary.put(PdfName.ACTION, action.getPdfName());
        if (action != LockAction.ALL) {
            PdfArray fields = new PdfArray();
            for (String name : fieldNames) {
                fields.add(new PdfString(name, PdfObject.TEXT_UNICODE));
            }
            dictionary.put(PdfName.FIELDS, fields);
        }
        if (permission != null) {
            dictionary.put(PdfName.P, new PdfNumber(permission.getCode()));
        }
    }

    /**
     * Tells if two locks lock the same fields, ignoring the order of the names and the permission.
     *
     * @param other the other lock
     * @return {@code true} if the action and the set of names are the same
     */
    public boolean sameFields(SignatureFieldLock other) {
        return other != null && action == other.action
                && new LinkedHashSet<>(fieldNames).equals(new LinkedHashSet<>(other.fieldNames));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SignatureFieldLock other)) {
            return false;
        }
        return action == other.action && fieldNames.equals(other.fieldNames) && permission == other.permission;
    }

    @Override
    public int hashCode() {
        return Objects.hash(action, fieldNames, permission);
    }

    @Override
    public String toString() {
        return "SignatureFieldLock{action=" + action + ", fieldNames=" + fieldNames + ", permission=" + permission
                + "}";
    }

    /**
     * The result of {@link #parse(PdfDictionary)}.
     *
     * @param lock     the lock, or {@code null} if the dictionary has no valid action
     * @param problems the problems found, empty if the dictionary is well formed
     */
    public record ParseResult(SignatureFieldLock lock, List<String> problems) {

        /**
         * Creates a parse result.
         *
         * @param lock     the lock, or {@code null} if the dictionary has no valid action
         * @param problems the problems found
         */
        public ParseResult {
            problems = List.copyOf(problems);
        }

        /**
         * Tells if the dictionary was well formed.
         *
         * @return {@code true} if a lock was read and no problem was found
         */
        public boolean isWellFormed() {
            return lock != null && problems.isEmpty();
        }
    }
}
