package com.lowagie.text.pdf;

import java.util.ArrayList;

/**
 * The optional App dictionary which is part of the Build Properties Dictionary.
 *
 * @author Lonzak
 */
public class PdfSignatureAppDataDict extends PdfDictionary {

    public PdfSignatureAppDataDict() {
    }

    /**
     * The name of the software module used to create the signature.
     *
     * @return the name of the software module used to create the signature.
     */
    public PdfName getName() {
        return (PdfName) super.get(PdfName.NAME);
    }

    /**
     * The name of the software module used to create the signature.
     *
     * @param name sets the name of the software module used to create the signature.
     */
    public void setName(String name) {
        if (name != null && !name.trim().isEmpty()) {
            super.put(PdfName.NAME, new PdfName(name));
        }
    }

    /**
     * The software module build date. This string is normally produced by the compiler that is used to compile the
     * software, for example using the Date and Time preprocessor flags.
     *
     * @return The software module build date or null.
     */
    public PdfString getDate() {
        return (PdfString) super.get(PdfName.DATE);
    }

    /**
     * The software module build date. This string is normally produced by the compiler that is used to compile the
     * software, for example using the Date and Time preprocessor flags.
     *
     * @param date Sets the software module build date
     */
    public void setDate(String date) {
        if (date != null && !date.trim().isEmpty()) {
            super.put(PdfName.DATE, new PdfString(date, TEXT_UNICODE));
        }
    }

    /**
     * The software module revision number. It is important that signature handlers and other software modules specify a
     * unique value for R for every publicly available build of the software. If the module or handler is ever found to
     * have been defective, for signatures where the value of PreRelease is false, the value of this attribute is likely
     * to be the only way to detect that the signature was created with the defective release. A sample value might be
     * 0x00020014, for software module version 2, sub-build 0x14. Various software modules may use this entry
     * differently.
     *
     * @return The software module revision number or null.
     */
    public PdfNumber getR() {
        return (PdfNumber) super.get(PdfName.R);
    }

    /**
     * The software module revision number. It is important that signature handlers and other software modules specify a
     * unique value for R for every publicly available build of the software. If the module or handler is ever found to
     * have been defective, for signatures where the value of PreRelease is false, the value of this attribute is likely
     * to be the only way to detect that the signature was created with the defective release. A sample value might be
     * 0x00020014, for software module version 2, sub-build 0x14. Various software modules may use this entry
     * differently.
     *
     * @param r sets the software module revision number
     */
    public void setR(int r) {
        super.put(PdfName.R, new PdfNumber(r));
    }

    /**
     * A flag that can be used by the signature handler or software module to indicate that this signature was created
     * with unreleased software. If true, this signature was created with pre-release or otherwise unreleased software.
     * The default value is false
     *
     * @return true when an unreleased software was used to create the signature otherwise false or null;
     */
    public PdfBoolean getPreRelease() {
        return (PdfBoolean) super.get(PdfName.PRERELEASE);
    }

    /**
     * A flag that can be used by the signature handler or software module to indicate that this signature was created
     * with unreleased software. If true, this signature was created with pre-release or otherwise unreleased software.
     * The default value is false
     *
     * @param preRelease sets the unreleased flag indicating whether the signature was created using an unreleased
     *                   software
     */
    public void setPreRelease(boolean preRelease) {
        super.put(PdfName.PRERELEASE, new PdfBoolean(preRelease));
    }

    /**
     * Indicates the operating system, such as Windows. Currently there is no specific string format defined for the
     * value of this attribute.
     *
     * @return the operating system or null.
     */
    public PdfArray getOs() {
        return (PdfArray) super.get(PdfName.OS);
    }

    /**
     * Indicates the operating system, such as Windows. Currently there is no specific string format defined for the
     * value of this attribute.
     *
     * @param os sets the operating system
     */
    public void setOs(String os) {
        if (os != null && !os.trim().isEmpty()) {
            ArrayList<PdfString> operatingSystem = new ArrayList<>();
            operatingSystem.add(new PdfString(os));
            super.put(PdfName.OS, new PdfArray(operatingSystem));
        }
    }

    /**
     * If there is a Legal dictionary in the catalog of the PDF file, and the NonEmbeddedFonts attribute (which
     * specifies the number of fonts not embedded) in that dictionary has a non-zero value, and the viewing application
     * has a preference set to suppress the display of the warning about fonts not being embedded, then the value of
     * this attribute will be set to true (meaning that no warning need be displayed).
     *
     * @return true when no warning should be displayed otherwise false / null;
     */
    public PdfBoolean getNonEFontNoWarn() {
        return (PdfBoolean) super.get(PdfName.NONEFONTNOWARN);
    }

    /**
     * If there is a Legal dictionary in the catalog of the PDF file, and the NonEmbeddedFonts attribute (which
     * specifies the number of fonts not embedded) in that dictionary has a non-zero value, and the viewing application
     * has a preference set to suppress the display of the warning about fonts not being embedded, then the value of
     * this attribute will be set to true (meaning that no warning need be displayed).
     *
     * @param nonEFontNoWarn sets whether no warning should be displayed otherwise false;
     */
    public void setNonEFontNoWarn(boolean nonEFontNoWarn) {
        super.put(PdfName.NONEFONTNOWARN, new PdfBoolean(nonEFontNoWarn));
    }

    /**
     * If the value is true, the application was in trusted mode when signing took place. The default value is false. A
     * viewing application is in trusted mode when only reviewed code is executing, where reviewed code is code that
     * does not affect the rendering of PDF files in ways that are not covered by the PDF Reference.
     *
     * @return if the application was in trusted mode when signing took place. Might also be null if not existent.
     */
    public PdfBoolean getTrustedMode() {
        return (PdfBoolean) super.get(PdfName.TRUSTEDMODE);
    }

    /**
     * If the value is true, the application was in trusted mode when signing took place. The default value is false. A
     * viewing application is in trusted mode when only reviewed code is executing, where reviewed code is code that
     * does not affect the rendering of PDF files in ways that are not covered by the PDF Reference.
     *
     * @param trustedMode sets whether the application was in trusted mode when signing took place
     */
    public void setTrustedMode(boolean trustedMode) {
        super.put(PdfName.TRUSTEDMODE, new PdfBoolean(trustedMode));
    }

    /**
     * A text string indicating the version of the application implementation, as described by the Name attribute in
     * this dictionary. When set by Adobe Acrobat, this entry is  in the format: major.minor.micro (for example 7.0.7).
     *
     * @return a text string indicating the version of the application implementation or null.
     */
    public PdfString getrEx() {
        return (PdfString) super.get(PdfName.REX);
    }

    /**
     * A text string indicating the version of the application implementation, as described by the Name attribute in
     * this dictionary. When set by Adobe Acrobat, this entry is  in the format: major.minor.micro (for example 7.0.7).
     *
     * @param rEx sets a text string indicating the version of the application implementation
     */
    public void setrEx(String rEx) {
        if (rEx != null && !rEx.trim().isEmpty()) {
            super.put(PdfName.REX, new PdfString(rEx, TEXT_UNICODE));
        }
    }

}
