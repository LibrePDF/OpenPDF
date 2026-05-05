/*
 * SPDX-License-Identifier: MPL-2.0 OR LGPL-2.1+
 * Copyright (c) 2026 the OpenPDF contributors.
 *
 * Dual licensed under the Mozilla Public License 2.0 (https://www.mozilla.org/MPL/2.0/)
 * or the GNU Lesser General Public License 2.1+ (https://www.gnu.org/licenses/old-licenses/lgpl-2.1.html).
 */
package org.openpdf.text.pdf.security;

import java.io.IOException;
import java.security.cert.CRL;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.openpdf.text.pdf.PdfArray;
import org.openpdf.text.pdf.PdfDictionary;
import org.openpdf.text.pdf.PdfIndirectReference;
import org.openpdf.text.pdf.PdfName;
import org.openpdf.text.pdf.PdfStream;
import org.openpdf.text.pdf.PdfWriter;

/**
 * Builds a Document Security Store (DSS) dictionary as defined in ISO 32000-2 §12.8.4.3 / ETSI
 * EN 319 142-1 (PAdES). The DSS lets a verifier validate document signatures long after the
 * issuing CA's CRL or OCSP responder has gone away (so-called Long Term Validation, LTV).
 *
 * <p>This implementation supports the document-wide arrays — {@code /Certs}, {@code /CRLs} and
 * {@code /OCSPs}. Per-signature {@code /VRI} entries are intentionally left as a follow-up: the
 * three top-level arrays already let most verifiers (including Adobe Acrobat) validate older
 * signatures at any future point in time.
 *
 * <p>Typical use:
 * <pre>{@code
 * DocumentSecurityStore dss = new DocumentSecurityStore();
 * dss.addCertificate(signerCert);
 * dss.addCertificate(issuerCert);
 * dss.addCrl(issuerCrl);
 * dss.addOcsp(ocspResponseDer);
 * stamper.getWriter().getExtraCatalog().put(PdfName.DSS, dss.build(stamper.getWriter()));
 * }</pre>
 */
public class DocumentSecurityStore {

    private final List<byte[]> certs = new ArrayList<>();
    private final List<byte[]> crls = new ArrayList<>();
    private final List<byte[]> ocsps = new ArrayList<>();

    /**
     * Adds a DER-encoded certificate to the {@code /Certs} array.
     *
     * @param cert the certificate
     * @throws CertificateEncodingException if the certificate cannot be DER-encoded
     */
    public void addCertificate(X509Certificate cert) throws CertificateEncodingException {
        if (cert != null) {
            certs.add(cert.getEncoded());
        }
    }

    /**
     * Adds a DER-encoded CRL to the {@code /CRLs} array.
     *
     * @param crl the certificate revocation list
     * @throws java.security.cert.CRLException if the CRL cannot be DER-encoded
     */
    public void addCrl(X509CRL crl) throws java.security.cert.CRLException {
        if (crl != null) {
            crls.add(crl.getEncoded());
        }
    }

    /**
     * Adds a CRL whose encoded form is already known. The bytes must be a DER-encoded
     * {@code CertificateList} (RFC 5280).
     *
     * @param encodedCrl DER-encoded CRL bytes
     */
    public void addCrl(byte[] encodedCrl) {
        if (encodedCrl != null) {
            crls.add(encodedCrl.clone());
        }
    }

    /**
     * Adds a DER-encoded OCSP {@code BasicOCSPResponse} (RFC 6960) to the {@code /OCSPs} array.
     *
     * @param encodedBasicOcspResponse DER-encoded BasicOCSPResponse bytes
     */
    public void addOcsp(byte[] encodedBasicOcspResponse) {
        if (encodedBasicOcspResponse != null) {
            ocsps.add(encodedBasicOcspResponse.clone());
        }
    }

    /**
     * @param items existing CRLs that should be added; non-{@link X509CRL} entries are ignored
     * @throws java.security.cert.CRLException if any CRL cannot be DER-encoded
     */
    public void addCrls(Collection<? extends CRL> items) throws java.security.cert.CRLException {
        if (items == null) {
            return;
        }
        for (CRL c : items) {
            if (c instanceof X509CRL) {
                addCrl((X509CRL) c);
            }
        }
    }

    /**
     * Builds and writes the DSS dictionary to the given writer, returning a dictionary suitable
     * for storing under {@code /DSS} in the document catalog.
     *
     * @param writer the output writer; used to allocate indirect references for the embedded streams
     * @return the {@code /DSS} dictionary (Type=DSS) populated with /Certs, /CRLs, /OCSPs arrays
     *         (entries are omitted when empty)
     * @throws IOException if writing to the writer fails
     */
    public PdfDictionary build(PdfWriter writer) throws IOException {
        PdfDictionary dss = new PdfDictionary(PdfName.DSS);
        if (!certs.isEmpty()) {
            dss.put(PdfName.CERTS, toStreamArray(writer, certs));
        }
        if (!crls.isEmpty()) {
            dss.put(PdfName.CRLS, toStreamArray(writer, crls));
        }
        if (!ocsps.isEmpty()) {
            dss.put(PdfName.OCSPS, toStreamArray(writer, ocsps));
        }
        return dss;
    }

    private static PdfArray toStreamArray(PdfWriter writer, List<byte[]> blobs) throws IOException {
        PdfArray array = new PdfArray();
        for (byte[] b : blobs) {
            PdfStream s = new PdfStream(b);
            s.flateCompress(writer.getCompressionLevel());
            PdfIndirectReference ref = writer.addToBody(s).getIndirectReference();
            array.add(ref);
        }
        return array;
    }
}

