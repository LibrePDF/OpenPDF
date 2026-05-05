/*
 * SPDX-License-Identifier: MPL-2.0 OR LGPL-2.1+
 * Copyright (c) 2026 the OpenPDF contributors.
 *
 * Dual licensed under the Mozilla Public License 2.0 (https://www.mozilla.org/MPL/2.0/)
 * or the GNU Lesser General Public License 2.1+ (https://www.gnu.org/licenses/old-licenses/lgpl-2.1.html).
 */
package org.openpdf.text.pdf.security;

import java.security.MessageDigest;
import org.openpdf.text.pdf.PdfDate;
import org.openpdf.text.pdf.PdfDictionary;
import org.openpdf.text.pdf.PdfName;
import org.openpdf.text.pdf.PdfSignature;
import org.openpdf.text.pdf.TSAClient;

/**
 * Helper that produces a PDF 2.0 document timestamp signature dictionary
 * ({@code /Type /DocTimeStamp}, {@code /SubFilter /ETSI.RFC3161}) as defined by ISO 32000-2
 * §12.8.5 / ETSI EN 319 142-1.
 *
 * <p>Document timestamps are technically signatures: they cover the full document {@code /ByteRange}
 * and contain an RFC 3161 {@code TimeStampToken} as their {@code /Contents}. They are used to anchor
 * a document in time without requiring a signing identity, and are the foundation for PAdES B-T /
 * PAdES B-LTA profiles.
 *
 * <p>Typical use:
 * <pre>{@code
 * PdfStamper stp = PdfStamper.createSignature(reader, out, '\0', null, true);
 * PdfSignatureAppearance sap = stp.getSignatureAppearance();
 * sap.setSignDate(Calendar.getInstance());
 * PdfSignature sig = DocumentTimestamp.newDictionary();
 * sap.setCryptoDictionary(sig);
 * // ... reserve space for /Contents, then call DocumentTimestamp.timestamp(...) and embed the
 * // returned token bytes via sap.close(...).
 * }</pre>
 */
public final class DocumentTimestamp {

    private DocumentTimestamp() {
        // utility
    }

    /**
     * @return a new {@link PdfSignature} dictionary with {@code /Type /DocTimeStamp} and
     *         {@code /SubFilter /ETSI.RFC3161}
     */
    public static PdfSignature newDictionary() {
        // Filter is mandatory; Adobe.PPKLite is the customary public-key filter used for both
        // signatures and document timestamps. Type is overridden to DocTimeStamp per ISO 32000-2.
        PdfSignature sig = new PdfSignature(PdfName.ADOBE_PPKLITE, PdfName.ETSI_RFC3161);
        sig.put(PdfName.TYPE, PdfName.DOC_TIME_STAMP);
        return sig;
    }

    /**
     * Convenience builder that fills in a freshly minted {@link PdfDictionary} suitable for use as
     * the {@code /CryptoDictionary} of a signature appearance, including the current signing time.
     *
     * @return a {@link PdfSignature} dictionary with {@code /Type /DocTimeStamp},
     *         {@code /SubFilter /ETSI.RFC3161} and {@code /M} (current time)
     */
    public static PdfSignature newDictionary(java.util.Calendar signDate) {
        PdfSignature sig = newDictionary();
        if (signDate != null) {
            sig.setDate(new PdfDate(signDate));
        }
        return sig;
    }

    /**
     * Requests an RFC 3161 timestamp token over {@code data} from {@code tsa}. The returned bytes
     * are the DER-encoded {@code TimeStampToken} that must be placed into the signature
     * dictionary's {@code /Contents} entry as a hexadecimal string.
     *
     * @param tsa  the time-stamp authority client
     * @param data the data to be timestamped (typically the digest of the byte range)
     * @return the encoded time-stamp token, or {@code null} if the TSA refused to issue one
     * @throws Exception if the TSA request fails for any reason
     */
    public static byte[] timestamp(TSAClient tsa, byte[] data) throws Exception {
        if (tsa == null || data == null) {
            return null;
        }
        MessageDigest md = tsa.getMessageDigest();
        byte[] imprint = md.digest(data);
        return tsa.getTimeStampToken(null, imprint);
    }
}


