/*
 * SPDX-License-Identifier: MPL-2.0 OR LGPL-2.1+
 * Copyright (c) 2026 the OpenPDF contributors.
 *
 * Dual licensed under the Mozilla Public License 2.0 (https://www.mozilla.org/MPL/2.0/)
 * or the GNU Lesser General Public License 2.1+ (https://www.gnu.org/licenses/old-licenses/lgpl-2.1.html).
 */
package org.openpdf.text.pdf.security;

import org.openpdf.text.pdf.PdfPublicKeySecurityHandler;

/**
 * Public-key security handler that produces hybrid CMS recipients: every file encryption key is
 * wrapped both for the recipient's classical (RSA / ECDH) certificate and for an ML-KEM
 * (FIPS 203) public key, when one is configured on the recipient. Verifiers that understand
 * either algorithm can open the document, so the document remains decryptable if either
 * cryptosystem is broken.
 *
 * <p>This class is a thin alias of {@link PdfPublicKeySecurityHandler} with hybrid mode pre-armed.
 * Application code may use it directly, or set the
 * {@link org.openpdf.text.pdf.PdfWriter#HYBRID_RECIPIENTS HYBRID_RECIPIENTS} flag together with
 * {@link org.openpdf.text.pdf.PdfWriter#ENCRYPTION_AES_256_V3 ENCRYPTION_AES_256_V3}.
 *
 * <p>Hybrid wrapping is opt-in and is not standardised in ISO 32000-2; the additional recipient
 * is encoded as an {@code OtherRecipientInfo} (RFC 9629 KEMRecipientInfo) so that legacy readers
 * silently ignore it and decrypt with the classical recipient only.
 */
public class HybridPublicKeySecurityHandler extends PdfPublicKeySecurityHandler {

    public HybridPublicKeySecurityHandler() {
        super();
        setHybridRecipients(true);
        setUseAes256(true);
    }
}

