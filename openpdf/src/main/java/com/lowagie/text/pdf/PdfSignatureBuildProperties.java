package com.lowagie.text.pdf;

/**
 * The build data dictionary contains information from the signature handler or software module that was used to create
 * the signature. Not all entries are relevant for all entries in the build properties dictionary.
 * <p>
 * (Referenced from the "Digital Signature Build Dictionary Specification")
 *
 * @author Lonzak
 */
public class PdfSignatureBuildProperties extends PdfDictionary {

    public PdfSignatureBuildProperties() {
        super();
    }

    /**
     * Returns the {@link PdfSignatureAppDataDict} from this dictionary. If it doesn't exist, a new
     * {@link PdfSignatureAppDataDict} is added.
     *
     * @return {@link PdfSignatureAppDataDict}
     */
    public PdfSignatureAppDataDict getPdfSignatureAppProperty() {
        PdfSignatureAppDataDict appPropDic = (PdfSignatureAppDataDict) getAsDict(PdfName.APP);
        if (appPropDic == null) {
            appPropDic = new PdfSignatureAppDataDict();
            put(PdfName.APP, appPropDic);
        }
        return appPropDic;
    }
}
