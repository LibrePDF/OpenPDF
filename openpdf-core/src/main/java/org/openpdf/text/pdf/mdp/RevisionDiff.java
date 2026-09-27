package org.openpdf.text.pdf.mdp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.openpdf.text.pdf.AcroFields;
import org.openpdf.text.pdf.PdfArray;
import org.openpdf.text.pdf.PdfDictionary;
import org.openpdf.text.pdf.PdfIndirectReference;
import org.openpdf.text.pdf.PdfName;
import org.openpdf.text.pdf.PdfNumber;
import org.openpdf.text.pdf.PdfObject;
import org.openpdf.text.pdf.PdfReader;
import org.openpdf.text.pdf.PdfString;

/**
 * Computes the semantic changes between two revisions of a document.
 *
 * <p>Both revisions are read into a snapshot of the parts the MDP rules talk about: catalog, interactive form,
 * fields (with their merged widget dictionaries), pages and annotations. The snapshots are then compared key by key,
 * and each difference becomes a {@link Change} tagged with the lowest permission under which it is allowed. Objects
 * that are rewritten without a real difference, as append mode writers often do, produce no change.
 */
final class RevisionDiff {

    private static final Set<PdfName> WIDGET_IGNORED_KEYS = Set.of(PdfName.PARENT, PdfName.KIDS, PdfName.P);
    private static final Set<PdfName> PAGE_IGNORED_KEYS = Set.of(PdfName.ANNOTS, PdfName.PARENT);
    private static final Set<PdfName> ANNOTATION_IGNORED_KEYS = Set.of(PdfName.P);
    private static final Set<PdfName> FILLING_KEYS = Set.of(PdfName.V, PdfName.AS, PdfName.AP, PdfName.RV);
    private static final int READ_ONLY_FLAG = 1;

    private final Snapshot oldRevision;
    private final Snapshot newRevision;
    private final PdfObjectComparator comparator;
    private final Map<String, Change> changes = new LinkedHashMap<>();

    private RevisionDiff(PdfReader oldReader, PdfReader newReader) {
        this.oldRevision = new Snapshot(oldReader);
        this.newRevision = new Snapshot(newReader);
        Set<Integer> tracked = new HashSet<>(oldRevision.tracked);
        tracked.addAll(newRevision.tracked);
        this.comparator = new PdfObjectComparator(tracked);
    }

    /**
     * Computes the changes from {@code oldReader} to {@code newReader}.
     *
     * @param oldReader the earlier revision
     * @param newReader the later revision
     * @return the changes, without duplicates
     */
    static List<Change> compare(PdfReader oldReader, PdfReader newReader) {
        RevisionDiff diff = new RevisionDiff(oldReader, newReader);
        diff.compareCatalog();
        diff.compareAcroForm();
        diff.compareFields();
        diff.comparePages();
        return new ArrayList<>(diff.changes.values());
    }

    private void add(ChangeRole role, Change.Kind kind, String fieldName, PdfName key, Allowance allowance,
            boolean lockRelevant, String description) {
        String keyName = key == null ? null : PdfName.decodeName(key.toString());
        String id = role + "|" + kind + "|" + fieldName + "|" + keyName + "|" + description;
        changes.putIfAbsent(id, new Change(role, kind, fieldName, keyName, allowance, lockRelevant, description));
    }

    private void compareCatalog() {
        PdfDictionary oldCatalog = oldRevision.catalog;
        PdfDictionary newCatalog = newRevision.catalog;
        for (PdfName key : union(oldCatalog, newCatalog)) {
            if (PdfName.PAGES.equals(key) || PdfName.ACROFORM.equals(key) || PdfName.TYPE.equals(key)
                    || comparator.equivalent(oldCatalog.get(key), newCatalog.get(key))) {
                continue;
            }
            Change.Kind kind = kind(oldCatalog.get(key), newCatalog.get(key));
            if (PdfName.DSS.equals(key)) {
                add(ChangeRole.DSS, kind, null, key, Allowance.ALWAYS, false, "document security store updated");
            } else if (PdfName.METADATA.equals(key)) {
                add(ChangeRole.METADATA, kind, null, key, Allowance.ALWAYS, false, "XMP metadata updated");
            } else if (PdfName.VERSION.equals(key) || PdfName.EXTENSIONS.equals(key)) {
                add(ChangeRole.CATALOG, kind, null, key, Allowance.ALWAYS, false, "version information updated");
            } else if (PdfName.PERMS.equals(key)) {
                add(ChangeRole.PERMS, kind, null, key, Allowance.NEVER, false, "permissions changed");
            } else {
                add(ChangeRole.CATALOG, kind, null, key, Allowance.NEVER, false, "catalog entry changed");
            }
        }
    }

    private void compareAcroForm() {
        PdfDictionary oldForm = oldRevision.acroForm;
        PdfDictionary newForm = newRevision.acroForm;
        if (oldForm == null || newForm == null) {
            if (oldForm != newForm) {
                add(ChangeRole.ACROFORM, oldForm == null ? Change.Kind.ADDED : Change.Kind.REMOVED, null, null,
                        Allowance.NEVER, false, "interactive form added or removed");
            }
            return;
        }
        for (PdfName key : union(oldForm, newForm)) {
            if (PdfName.FIELDS.equals(key) || comparator.equivalent(oldForm.get(key), newForm.get(key))) {
                continue;
            }
            Change.Kind kind = kind(oldForm.get(key), newForm.get(key));
            add(ChangeRole.ACROFORM, kind, null, key, acroFormAllowance(key, oldForm, newForm), false,
                    "interactive form entry changed");
        }
        compareTopLevelFields(oldForm, newForm);
    }

    private Allowance acroFormAllowance(PdfName key, PdfDictionary oldForm, PdfDictionary newForm) {
        if (PdfName.NEEDAPPEARANCES.equals(key) || PdfName.DR.equals(key) && resourcesOnlyGrew(oldForm, newForm)) {
            return Allowance.FORM_FILLING;
        }
        if (PdfName.SIGFLAGS.equals(key)) {
            return Allowance.SIGNING;
        }
        return Allowance.NEVER;
    }

    private boolean resourcesOnlyGrew(PdfDictionary oldForm, PdfDictionary newForm) {
        PdfDictionary oldResources = oldForm.getAsDict(PdfName.DR);
        PdfDictionary newResources = newForm.getAsDict(PdfName.DR);
        if (newResources == null) {
            return false;
        }
        if (oldResources == null) {
            return true;
        }
        for (PdfName category : oldResources.getKeys()) {
            PdfObject oldCategory = PdfReader.getPdfObject(oldResources.get(category));
            PdfObject newCategory = PdfReader.getPdfObject(newResources.get(category));
            if (oldCategory instanceof PdfDictionary oldDictionary
                    && newCategory instanceof PdfDictionary newDictionary) {
                for (PdfName name : oldDictionary.getKeys()) {
                    if (!comparator.equivalent(oldDictionary.get(name), newDictionary.get(name))) {
                        return false;
                    }
                }
            } else if (!comparator.equivalent(oldCategory, newCategory)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks the {@code /Fields} array: added or removed top level fields must be explained by the field comparison,
     * otherwise (for example a field without widget) the change is reported here.
     */
    private void compareTopLevelFields(PdfDictionary oldForm, PdfDictionary newForm) {
        Map<Integer, PdfObject> oldRefs = references(oldForm.getAsArray(PdfName.FIELDS));
        Map<Integer, PdfObject> newRefs = references(newForm.getAsArray(PdfName.FIELDS));
        for (Map.Entry<Integer, PdfObject> entry : newRefs.entrySet()) {
            if (oldRefs.containsKey(entry.getKey())) {
                continue;
            }
            Set<String> names = fieldNames(entry.getValue());
            if (names.isEmpty() || !newRevision.fields.keySet().containsAll(names)) {
                add(ChangeRole.ACROFORM, Change.Kind.ADDED, null, PdfName.FIELDS, Allowance.NEVER, false,
                        "form field without widget added");
            }
        }
        for (Map.Entry<Integer, PdfObject> entry : oldRefs.entrySet()) {
            if (newRefs.containsKey(entry.getKey())) {
                continue;
            }
            Set<String> names = fieldNames(entry.getValue());
            if (names.isEmpty() || !oldRevision.fields.keySet().containsAll(names)) {
                add(ChangeRole.ACROFORM, Change.Kind.REMOVED, null, PdfName.FIELDS, Allowance.NEVER, false,
                        "form field without widget removed");
            }
        }
    }

    private void compareFields() {
        Set<String> names = new LinkedHashSet<>(oldRevision.fields.keySet());
        names.addAll(newRevision.fields.keySet());
        for (String name : names) {
            FieldSnapshot oldField = oldRevision.fields.get(name);
            FieldSnapshot newField = newRevision.fields.get(name);
            if (oldField == null) {
                addedField(name, newField);
            } else if (newField == null) {
                add(ChangeRole.FIELD, Change.Kind.REMOVED, name, null, Allowance.NEVER, true, "form field removed");
            } else {
                compareField(name, oldField, newField);
            }
        }
    }

    private void addedField(String name, FieldSnapshot field) {
        if (!field.signature) {
            add(ChangeRole.FIELD, Change.Kind.ADDED, name, null, Allowance.NEVER, false, "new form field added");
        } else if (field.signatureDictionary != null
                && PdfName.DOCTIMESTAMP.equals(field.signatureDictionary.getAsName(PdfName.TYPE))) {
            add(ChangeRole.FIELD, Change.Kind.ADDED, name, null, Allowance.ALWAYS, false,
                    "document timestamp added");
        } else {
            add(ChangeRole.FIELD, Change.Kind.ADDED, name, null, Allowance.NEW_SIGNATURE_FIELD, false,
                    "new signature field added");
        }
    }

    private void compareField(String name, FieldSnapshot oldField, FieldSnapshot newField) {
        if (oldField.merged.size() != newField.merged.size()) {
            add(ChangeRole.WIDGET, Change.Kind.MODIFIED, name, null, Allowance.NEVER, true,
                    "number of widgets changed");
        }
        boolean signedNow = oldField.signatureDictionary == null && newField.signatureDictionary != null;
        int widgets = Math.min(oldField.merged.size(), newField.merged.size());
        for (int k = 0; k < widgets; ++k) {
            compareWidget(name, oldField, newField, k, signedNow);
        }
        compareSignatureDictionary(name, oldField, newField);
    }

    /**
     * Compares the merged dictionaries of the widget at index {@code k} of a field.
     */
    private void compareWidget(String name, FieldSnapshot oldField, FieldSnapshot newField, int k,
            boolean signedNow) {
        PdfDictionary oldMerged = oldField.merged.get(k);
        PdfDictionary newMerged = newField.merged.get(k);
        for (PdfName key : union(oldMerged, newMerged)) {
            if (WIDGET_IGNORED_KEYS.contains(key) || comparator.equivalent(oldMerged.get(key), newMerged.get(key))) {
                continue;
            }
            boolean fieldKey = PdfName.getFormfieldNames().contains(key);
            ChangeRole role = fieldKey ? ChangeRole.FIELD : ChangeRole.WIDGET;
            boolean readOnlyOnly = PdfName.FF.equals(key) && onlyReadOnlySet(oldMerged, newMerged);
            Allowance allowance = newField.signature
                    ? signatureFieldAllowance(key, fieldKey, signedNow, oldField, readOnlyOnly)
                    : dataFieldAllowance(key, readOnlyOnly);
            String description = signedNow && allowance == Allowance.SIGNING
                    ? "signature field signed"
                    : "form field entry changed";
            add(role, kind(oldMerged.get(key), newMerged.get(key)), name, key, allowance, !readOnlyOnly,
                    description);
        }
    }

    /**
     * Reports a signature dictionary changed after signing: once signed, it is immutable.
     */
    private void compareSignatureDictionary(String name, FieldSnapshot oldField, FieldSnapshot newField) {
        if (oldField.signatureDictionary != null && newField.signatureDictionary != null
                && (oldField.signatureReference != newField.signatureReference
                || !comparator.contentEqual(oldField.signatureDictionary, newField.signatureDictionary))) {
            add(ChangeRole.SIGNATURE_DICTIONARY, Change.Kind.MODIFIED, name, null, Allowance.NEVER, true,
                    "signature dictionary changed after signing");
        }
    }

    private static Allowance signatureFieldAllowance(PdfName key, boolean fieldKey, boolean signedNow,
            FieldSnapshot oldField, boolean readOnlyOnly) {
        if (PdfName.V.equals(key)) {
            return signedNow ? Allowance.SIGNING : Allowance.NEVER;
        }
        if (readOnlyOnly) {
            return Allowance.FORM_FILLING;
        }
        if (fieldKey) {
            return Allowance.NEVER;
        }
        if (signedNow) {
            return Allowance.SIGNING;
        }
        return oldField.signatureDictionary == null ? Allowance.FORM_FILLING : Allowance.NEVER;
    }

    private static Allowance dataFieldAllowance(PdfName key, boolean readOnlyOnly) {
        if (FILLING_KEYS.contains(key) || readOnlyOnly) {
            return Allowance.FORM_FILLING;
        }
        return Allowance.NEVER;
    }

    private static boolean onlyReadOnlySet(PdfDictionary oldMerged, PdfDictionary newMerged) {
        int oldFlags = flags(oldMerged);
        int newFlags = flags(newMerged);
        return oldFlags != newFlags && (oldFlags | READ_ONLY_FLAG) == newFlags;
    }

    private static int flags(PdfDictionary merged) {
        PdfObject flags = PdfReader.getPdfObject(merged.get(PdfName.FF));
        return flags instanceof PdfNumber number ? number.intValue() : 0;
    }

    private void comparePages() {
        List<PageSnapshot> oldPages = oldRevision.pages;
        List<PageSnapshot> newPages = newRevision.pages;
        if (oldPages.size() != newPages.size()) {
            add(ChangeRole.PAGE, oldPages.size() < newPages.size() ? Change.Kind.ADDED : Change.Kind.REMOVED, null,
                    null, Allowance.NEVER, false, "number of pages changed");
        }
        int pages = Math.min(oldPages.size(), newPages.size());
        for (int k = 0; k < pages; ++k) {
            PageSnapshot oldPage = oldPages.get(k);
            PageSnapshot newPage = newPages.get(k);
            if (!comparator.dictionaryEqual(oldPage.page, newPage.page, PAGE_IGNORED_KEYS)) {
                add(ChangeRole.PAGE, Change.Kind.MODIFIED, null, null, Allowance.NEVER, false,
                        "content or attributes of page " + (k + 1) + " changed");
            }
            compareAnnotations(k + 1, oldPage, newPage);
        }
    }

    private void compareAnnotations(int pageNumber, PageSnapshot oldPage, PageSnapshot newPage) {
        compareAddedOrChangedAnnotations(pageNumber, oldPage, newPage);
        compareRemovedAnnotations(pageNumber, oldPage, newPage);
        if (!comparator.equivalent(oldPage.directAnnotations, newPage.directAnnotations)) {
            add(ChangeRole.ANNOTATION, Change.Kind.MODIFIED, null, null, Allowance.ANNOTATIONS, false,
                    "direct annotation changed on page " + pageNumber);
        }
    }

    private void compareAddedOrChangedAnnotations(int pageNumber, PageSnapshot oldPage, PageSnapshot newPage) {
        for (Map.Entry<Integer, PdfDictionary> entry : newPage.annotations.entrySet()) {
            PdfDictionary oldAnnotation = oldPage.annotations.get(entry.getKey());
            PdfDictionary newAnnotation = entry.getValue();
            if (isWidget(newAnnotation)) {
                if (oldAnnotation == null && !newRevision.widgets.contains(entry.getKey())) {
                    add(ChangeRole.ANNOTATION, Change.Kind.ADDED, null, null, Allowance.NEVER, false,
                            "widget without form field added on page " + pageNumber);
                }
                continue;
            }
            if (oldAnnotation == null) {
                add(ChangeRole.ANNOTATION, Change.Kind.ADDED, null, null, Allowance.ANNOTATIONS, false,
                        "annotation added on page " + pageNumber);
            } else if (!comparator.dictionaryEqual(oldAnnotation, newAnnotation, ANNOTATION_IGNORED_KEYS)) {
                add(ChangeRole.ANNOTATION, Change.Kind.MODIFIED, null, null, Allowance.ANNOTATIONS, false,
                        "annotation changed on page " + pageNumber);
            }
        }
    }

    private void compareRemovedAnnotations(int pageNumber, PageSnapshot oldPage, PageSnapshot newPage) {
        for (Map.Entry<Integer, PdfDictionary> entry : oldPage.annotations.entrySet()) {
            if (newPage.annotations.containsKey(entry.getKey())) {
                continue;
            }
            if (isWidget(entry.getValue())) {
                if (!oldRevision.widgets.contains(entry.getKey())) {
                    add(ChangeRole.ANNOTATION, Change.Kind.REMOVED, null, null, Allowance.NEVER, false,
                            "widget without form field removed from page " + pageNumber);
                }
                continue;
            }
            add(ChangeRole.ANNOTATION, Change.Kind.REMOVED, null, null, Allowance.ANNOTATIONS, false,
                    "annotation removed from page " + pageNumber);
        }
    }

    private static boolean isWidget(PdfDictionary annotation) {
        return PdfName.WIDGET.equals(annotation.getAsName(PdfName.SUBTYPE));
    }

    private static Change.Kind kind(PdfObject oldValue, PdfObject newValue) {
        if (oldValue == null) {
            return Change.Kind.ADDED;
        }
        return newValue == null ? Change.Kind.REMOVED : Change.Kind.MODIFIED;
    }

    private static Set<PdfName> union(PdfDictionary a, PdfDictionary b) {
        Set<PdfName> keys = new LinkedHashSet<>(a.getKeys());
        keys.addAll(b.getKeys());
        return keys;
    }

    private static Map<Integer, PdfObject> references(PdfArray array) {
        Map<Integer, PdfObject> references = new LinkedHashMap<>();
        if (array != null) {
            for (PdfObject element : array.getElements()) {
                if (element != null && element.isIndirect()) {
                    references.put(((PdfIndirectReference) element).getNumber(), element);
                }
            }
        }
        return references;
    }

    private static Set<String> fieldNames(PdfObject field) {
        Set<String> names = new LinkedHashSet<>();
        collectFieldNames(field, null, names, new HashSet<>());
        return names;
    }

    private static void collectFieldNames(PdfObject object, String parentName, Set<String> names,
            Set<Integer> visited) {
        if (object != null && object.isIndirect() && !visited.add(((PdfIndirectReference) object).getNumber())) {
            return;
        }
        PdfObject resolved = PdfReader.getPdfObject(object);
        if (!(resolved instanceof PdfDictionary field)) {
            return;
        }
        PdfString title = field.getAsString(PdfName.T);
        String name = parentName;
        if (title != null) {
            name = parentName == null ? title.toUnicodeString() : parentName + "." + title.toUnicodeString();
        }
        PdfArray kids = field.getAsArray(PdfName.KIDS);
        if (kids == null || kids.isEmpty()) {
            if (name != null) {
                names.add(name);
            }
            return;
        }
        for (PdfObject kid : kids.getElements()) {
            collectFieldNames(kid, name, names, visited);
        }
    }

    /**
     * The parts of one revision that the comparison needs.
     */
    private static final class Snapshot {

        private final PdfDictionary catalog;
        private final PdfDictionary acroForm;
        private final Map<String, FieldSnapshot> fields = new HashMap<>();
        private final List<PageSnapshot> pages = new ArrayList<>();
        private final Set<Integer> widgets = new HashSet<>();
        private final Set<Integer> tracked = new HashSet<>();

        private Snapshot(PdfReader reader) {
            catalog = reader.getCatalog();
            acroForm = catalog.getAsDict(PdfName.ACROFORM);
            track(reader.getTrailer().get(PdfName.ROOT));
            track(catalog.get(PdfName.ACROFORM));
            track(catalog.get(PdfName.PAGES));
            if (acroForm != null) {
                trackAll(acroForm.getAsArray(PdfName.FIELDS));
            }
            for (int k = 1; k <= reader.getNumberOfPages(); ++k) {
                track(reader.getPageOrigRef(k));
                pages.add(new PageSnapshot(reader.getPageN(k)));
            }
            for (PageSnapshot page : pages) {
                tracked.addAll(page.annotations.keySet());
            }
            for (Map.Entry<String, AcroFields.Item> entry : reader.getAcroFields().getAllFields().entrySet()) {
                FieldSnapshot field = new FieldSnapshot(entry.getValue());
                fields.put(entry.getKey(), field);
                AcroFields.Item item = entry.getValue();
                for (int k = 0; k < item.size(); ++k) {
                    PdfIndirectReference widgetReference = item.getWidgetRef(k);
                    if (widgetReference != null) {
                        widgets.add(widgetReference.getNumber());
                        track(widgetReference);
                    }
                    trackParents(item.getWidget(k));
                }
                if (field.signatureReference > 0) {
                    tracked.add(field.signatureReference);
                }
            }
        }

        private void track(PdfObject object) {
            if (object != null && object.isIndirect()) {
                tracked.add(((PdfIndirectReference) object).getNumber());
            }
        }

        private void trackAll(PdfArray array) {
            if (array != null) {
                for (PdfObject element : array.getElements()) {
                    track(element);
                }
            }
        }

        private void trackParents(PdfDictionary widget) {
            PdfDictionary current = widget;
            Set<Integer> visited = new HashSet<>();
            while (current != null) {
                PdfObject parent = current.get(PdfName.PARENT);
                if (parent == null || !parent.isIndirect()
                        || !visited.add(((PdfIndirectReference) parent).getNumber())) {
                    return;
                }
                track(parent);
                current = current.getAsDict(PdfName.PARENT);
            }
        }
    }

    /**
     * A terminal form field: one merged dictionary per widget and, for signature fields, the signature dictionary.
     */
    private static final class FieldSnapshot {

        private final List<PdfDictionary> merged = new ArrayList<>();
        private final boolean signature;
        private final PdfDictionary signatureDictionary;
        private final int signatureReference;

        private FieldSnapshot(AcroFields.Item item) {
            for (int k = 0; k < item.size(); ++k) {
                merged.add(mergeWithParents(item.getWidget(k)));
            }
            PdfDictionary first = merged.get(0);
            signature = PdfName.SIG.equals(PdfReader.getPdfObject(first.get(PdfName.FT)));
            PdfObject value = first.get(PdfName.V);
            PdfObject resolved = signature ? PdfReader.getPdfObject(value) : null;
            signatureDictionary = resolved instanceof PdfDictionary dictionary ? dictionary : null;
            signatureReference = signatureDictionary != null && value.isIndirect()
                    ? ((PdfIndirectReference) value).getNumber()
                    : -1;
        }
    }

    /**
     * Merges a widget with its parent fields, the nearest value winning. Unlike {@link AcroFields.Item#getMerged},
     * the entries of the interactive form dictionary are not included, so they are only compared once.
     */
    private static PdfDictionary mergeWithParents(PdfDictionary widget) {
        PdfDictionary merged = new PdfDictionary();
        Set<Integer> visited = new HashSet<>();
        PdfDictionary current = widget;
        while (current != null) {
            for (PdfName key : current.getKeys()) {
                if (merged.get(key) == null) {
                    merged.put(key, current.get(key));
                }
            }
            PdfObject parent = current.get(PdfName.PARENT);
            if (parent == null || parent.isIndirect() && !visited.add(((PdfIndirectReference) parent).getNumber())) {
                break;
            }
            current = current.getAsDict(PdfName.PARENT);
        }
        return merged;
    }

    /**
     * A page: its dictionary and its annotations, by object number for the indirect ones.
     */
    private static final class PageSnapshot {

        private final PdfDictionary page;
        private final Map<Integer, PdfDictionary> annotations = new LinkedHashMap<>();
        private final PdfArray directAnnotations = new PdfArray();

        private PageSnapshot(PdfDictionary page) {
            this.page = page;
            PdfArray annots = page.getAsArray(PdfName.ANNOTS);
            if (annots == null) {
                return;
            }
            for (PdfObject element : annots.getElements()) {
                PdfObject resolved = PdfReader.getPdfObject(element);
                if (!(resolved instanceof PdfDictionary annotation)) {
                    continue;
                }
                if (element.isIndirect()) {
                    annotations.put(((PdfIndirectReference) element).getNumber(), annotation);
                } else {
                    directAnnotations.add(annotation);
                }
            }
        }
    }
}
