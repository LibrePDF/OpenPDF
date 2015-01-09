package old.org.bouncycastle.jce.provider;

import old.org.bouncycastle.jce.exception.ExtCertPathValidatorException;
import old.org.bouncycastle.util.Selector;
import old.org.bouncycastle.x509.ExtendedPKIXParameters;
import old.org.bouncycastle.x509.X509AttributeCertStoreSelector;
import old.org.bouncycastle.x509.X509AttributeCertificate;

import java.security.InvalidAlgorithmParameterException;
import java.security.cert.CertPath;
import java.security.cert.CertPathParameters;
import java.security.cert.CertPathValidatorException;
import java.security.cert.CertPathValidatorResult;
import java.security.cert.CertPathValidatorSpi;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.Set;

/**
 * CertPathValidatorSpi implementation for X.509 Attribute Certificates la RFC 3281.
 * 
 * @see old.org.bouncycastle.x509.ExtendedPKIXParameters
 */
public class PKIXAttrCertPathValidatorSpi
    extends CertPathValidatorSpi
{

    /**
     * Validates an attribute certificate with the given certificate path.
     * 
     * <p>
     * <code>params</code> must be an instance of
     * <code>ExtendedPKIXParameters</code>.
     * <p>
     * The target constraints in the <code>params</code> must be an
     * <code>X509AttributeCertStoreSelector</code> with at least the attribute
     * certificate criterion set. Obey that also target informations may be
     * necessary to correctly validate this attribute certificate.
     * <p>
     * The attribute certificate issuer must be added to the trusted attribute
     * issuers with {@link ExtendedPKIXParameters#setTrustedACIssuers(Set)}.
     * 
     * @param certPath The certificate path which belongs to the attribute
     *            certificate issuer public key certificate.
     * @param params The PKIX parameters.
     * @return A <code>PKIXCertPathValidatorResult</code> of the result of
     *         validating the <code>certPath</code>.
     * @throws InvalidAlgorithmParameterException if <code>params</code> is
     *             inappropriate for this validator.
     * @throws CertPathValidatorException if the verification fails.
     */
    public CertPathValidatorResult engineValidate(CertPath certPath,
        CertPathParameters params) throws CertPathValidatorException,
        InvalidAlgorithmParameterException
    {
        if (!(params instanceof ExtendedPKIXParameters))
        {
            throw new InvalidAlgorithmParameterException(
                "Parameters must be a "
                    + ExtendedPKIXParameters.class.getName() + " instance.");
        }
        ExtendedPKIXParameters pkixParams = (ExtendedPKIXParameters) params;

        Selector certSelect = pkixParams.getTargetConstraints();
        if (!(certSelect instanceof X509AttributeCertStoreSelector))
        {
            throw new InvalidAlgorithmParameterException(
                "TargetConstraints must be an instance of "
                    + X509AttributeCertStoreSelector.class.getName() + " for "
                    + this.getClass().getName() + " class.");
        }
        X509AttributeCertificate attrCert = ((X509AttributeCertStoreSelector) certSelect)
            .getAttributeCert();

        CertPath holderCertPath = RFC3281CertPathUtilities.processAttrCert1(attrCert, pkixParams);
        CertPathValidatorResult result = RFC3281CertPathUtilities.processAttrCert2(certPath, pkixParams);
        X509Certificate issuerCert = (X509Certificate) certPath
            .getCertificates().get(0);
        RFC3281CertPathUtilities.processAttrCert3(issuerCert, pkixParams);
        RFC3281CertPathUtilities.processAttrCert4(issuerCert, pkixParams);
        RFC3281CertPathUtilities.processAttrCert5(attrCert, pkixParams);
        // 6 already done in X509AttributeCertStoreSelector
        RFC3281CertPathUtilities.processAttrCert7(attrCert, certPath, holderCertPath, pkixParams);
        RFC3281CertPathUtilities.additionalChecks(attrCert, pkixParams);
        Date date = null;
        try
        {
            date = CertPathValidatorUtilities
                .getValidCertDateFromValidityModel(pkixParams, null, -1);
        }
        catch (AnnotatedException e)
        {
            throw new ExtCertPathValidatorException(
                "Could not get validity date from attribute certificate.", e);
        }
        RFC3281CertPathUtilities.checkCRLs(attrCert, pkixParams, issuerCert, date, certPath.getCertificates());
        return result;
    }
}
