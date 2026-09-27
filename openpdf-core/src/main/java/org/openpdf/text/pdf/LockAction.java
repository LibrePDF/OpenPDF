package org.openpdf.text.pdf;

/**
 * The value of the {@code /Action} entry of a signature field lock dictionary (ISO 32000, 12.7.5.5).
 *
 * @see SignatureFieldLock
 */
public enum LockAction {

    /**
     * All fields in the document are locked.
     */
    ALL(PdfName.ALL),

    /**
     * Only the fields listed in {@code /Fields} are locked.
     */
    INCLUDE(PdfName.INCLUDE),

    /**
     * All fields except the ones listed in {@code /Fields} are locked.
     */
    EXCLUDE(PdfName.EXCLUDE);

    private final PdfName pdfName;

    LockAction(PdfName pdfName) {
        this.pdfName = pdfName;
    }

    /**
     * Returns the action matching a PDF name.
     *
     * @param name the {@code /Action} value
     * @return the matching action or {@code null} if the name is not a valid action
     */
    public static LockAction fromPdfName(PdfName name) {
        for (LockAction action : values()) {
            if (action.pdfName.equals(name)) {
                return action;
            }
        }
        return null;
    }

    /**
     * Returns the PDF name used to serialize this action.
     *
     * @return the PDF name
     */
    public PdfName getPdfName() {
        return pdfName;
    }
}
