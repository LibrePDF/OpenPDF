package old.org.bouncycastle.jce.provider;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.Principal;
import java.security.cert.CertPath;
import java.security.cert.CertPathBuilderException;
import java.security.cert.CertPathBuilderResult;
import java.security.cert.CertPathBuilderSpi;
import java.security.cert.CertPathParameters;
import java.security.cert.CertPathValidator;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateParsingException;
import java.security.cert.PKIXBuilderParameters;
import java.security.cert.PKIXCertPathBuilderResult;
import java.security.cert.PKIXCertPathValidatorResult;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.security.auth.x500.X500Principal;

import old.org.bouncycastle.jce.exception.ExtCertPathBuilderException;
import old.org.bouncycastle.util.Selector;
import old.org.bouncycastle.x509.ExtendedPKIXBuilderParameters;
import old.org.bouncycastle.x509.X509AttributeCertStoreSelector;
import old.org.bouncycastle.x509.X509AttributeCertificate;
import old.org.bouncycastle.x509.X509CertStoreSelector;

public class PKIXAttrCertPathBuilderSpi
    extends CertPathBuilderSpi
{

    /**
     * Build and validate a CertPath using the given parameter.
     * 
     * @param params PKIXBuilderParameters object containing all information to
     *            build the CertPath
     */
    public CertPathBuilderResult engineBuild(CertPathParameters params)
            throws CertPathBuilderException, InvalidAlgorithmParameterException
    {
        if (!(params instanceof PKIXBuilderParameters)
                && !(params instanceof ExtendedPKIXBuilderParameters))
        {
            throw new InvalidAlgorithmParameterException(
                    "Parameters must be an instance of "
                            + PKIXBuilderParameters.class.getName() + " or "
                            + ExtendedPKIXBuilderParameters.class.getName()
                            + ".");
        }

        ExtendedPKIXBuilderParameters pkixParams;
        if (params instanceof ExtendedPKIXBuilderParameters)
        {
            pkixParams = (ExtendedPKIXBuilderParameters) params;
        }
        else
        {
            pkixParams = (ExtendedPKIXBuilderParameters) ExtendedPKIXBuilderParameters
                    .getInstance((PKIXBuilderParameters) params);
        }

        Collection targets;
        Iterator targetIter;
        List certPathList = new ArrayList();
        X509AttributeCertificate cert;

        // search target certificates

        Selector certSelect = pkixParams.getTargetConstraints();
        if (!(certSelect instanceof X509AttributeCertStoreSelector))
        {
            throw new CertPathBuilderException(
                    "TargetConstraints must be an instance of "
                            + X509AttributeCertStoreSelector.class.getName()
                            + " for "+this.getClass().getName()+" class.");
        }

        try
        {
            targets = CertPathValidatorUtilities.findCertificates((X509AttributeCertStoreSelector)certSelect, pkixParams.getStores());
        }
        catch (AnnotatedException e)
        {
            throw new ExtCertPathBuilderException("Error finding target attribute certificate.", e);
        }

        if (targets.isEmpty())
        {
            throw new CertPathBuilderException(
                    "No attribute certificate found matching targetContraints.");
        }

        CertPathBuilderResult result = null;

        // check all potential target certificates
        targetIter = targets.iterator();
        while (targetIter.hasNext() && result == null)
        {
            cert = (X509AttributeCertificate) targetIter.next();
            
            X509CertStoreSelector selector = new X509CertStoreSelector();
            Principal[] principals = cert.getIssuer().getPrincipals();
            Set issuers = new HashSet();
            for (int i = 0; i < principals.length; i++)
            {
                try
                {
                    if (principals[i] instanceof X500Principal)
                    {
                        selector.setSubject(((X500Principal)principals[i]).getEncoded());
                    }
                    issuers.addAll(CertPathValidatorUtilities.findCertificates(selector, pkixParams.getStores()));
                    issuers.addAll(CertPathValidatorUtilities.findCertificates(selector, pkixParams.getCertStores()));
                }
                catch (AnnotatedException e)
                {
                    throw new ExtCertPathBuilderException(
                        "Public key certificate for attribute certificate cannot be searched.",
                        e);
                }
                catch (IOException e)
                {
                    throw new ExtCertPathBuilderException(
                        "cannot encode X500Principal.",
                        e);
                }
            }
            if (issuers.isEmpty())
            {
                throw new CertPathBuilderException(
                    "Public key certificate for attribute certificate cannot be found.");
            }
            Iterator it = issuers.iterator();
            while (it.hasNext() && result == null)
            {
                result = build(cert, (X509Certificate)it.next(), pkixParams, certPathList);
            }
        }

        if (result == null && certPathException != null)
        {
            throw new ExtCertPathBuilderException(
                                    "Possible certificate chain could not be validated.",
                                    certPathException);
        }

        if (result == null && certPathException == null)
        {
            throw new CertPathBuilderException(
                    "Unable to find certificate chain.");
        }

        return result;
    }

    private Exception certPathException;

    private CertPathBuilderResult build(X509AttributeCertificate attrCert, X509Certificate tbvCert,
            ExtendedPKIXBuilderParameters pkixParams, List tbvPath)

    {
        // If tbvCert is readily present in tbvPath, it indicates having run
        // into a cycle in the
        // PKI graph.
        if (tbvPath.contains(tbvCert))
        {
            return null;
        }
        // step out, the certificate is not allowed to appear in a certification
        // chain
        if (pkixParams.getExcludedCerts().contains(tbvCert))
        {
            return null;
        }
        // test if certificate path exceeds maximum length
        if (pkixParams.getMaxPathLength() != -1)
        {
            if (tbvPath.size() - 1 > pkixParams.getMaxPathLength())
            {
                return null;
            }
        }

        tbvPath.add(tbvCert);

        CertificateFactory cFact;
        CertPathValidator validator;
        CertPathBuilderResult builderResult = null;

        try
        {
            cFact = CertificateFactory.getInstance("X.509", BouncyCastleProvider.PROVIDER_NAME);
            validator = CertPathValidator.getInstance("RFC3281", BouncyCastleProvider.PROVIDER_NAME);
        }
        catch (Exception e)
        {
            // cannot happen
            throw new RuntimeException(
                            "Exception creating support classes.");
        }

        try
        {
            // check whether the issuer of <tbvCert> is a TrustAnchor
            if (CertPathValidatorUtilities.findTrustAnchor(tbvCert, pkixParams.getTrustAnchors(),
                pkixParams.getSigProvider()) != null)
            {
                CertPath certPath;
                PKIXCertPathValidatorResult result;
                try
                {
                    certPath = cFact.generateCertPath(tbvPath);
                }
                catch (Exception e)
                {
                    throw new AnnotatedException(
                                            "Certification path could not be constructed from certificate list.",
                                            e);
                }

                try
                {
                    result = (PKIXCertPathValidatorResult) validator.validate(
                            certPath, pkixParams);
                }
                catch (Exception e)
                {
                    throw new AnnotatedException(
                                            "Certification path could not be validated.",
                                            e);
                }

                return new PKIXCertPathBuilderResult(certPath, result
                        .getTrustAnchor(), result.getPolicyTree(), result
                        .getPublicKey());

            }
            else
            {
                // add additional X.509 stores from locations in certificate
                try
                {
                    CertPathValidatorUtilities.addAdditionalStoresFromAltNames(tbvCert, pkixParams);
                }
                catch (CertificateParsingException e)
                {
                    throw new AnnotatedException(
                                            "No additional X.509 stores can be added from certificate locations.",
                                            e);
                }
                Collection issuers = new HashSet();
                // try to get the issuer certificate from one
                // of the stores
                try
                {
                    issuers.addAll(CertPathValidatorUtilities.findIssuerCerts(tbvCert, pkixParams));
                }
                catch (AnnotatedException e)
                {
                    throw new AnnotatedException(
                                            "Cannot find issuer certificate for certificate in certification path.",
                                            e);
                }
                if (issuers.isEmpty())
                {
                    throw new AnnotatedException(
                            "No issuer certificate for certificate in certification path found.");
                }
                Iterator it = issuers.iterator();

                while (it.hasNext() && builderResult == null)
                {
                    X509Certificate issuer = (X509Certificate) it.next();
                    // TODO Use CertPathValidatorUtilities.isSelfIssued(issuer)?
                    // if untrusted self signed certificate continue
                    if (issuer.getIssuerX500Principal().equals(
                            issuer.getSubjectX500Principal()))
                    {
                        continue;
                    }
                    builderResult = build(attrCert, issuer, pkixParams, tbvPath);
                }
            }
        }
        catch (AnnotatedException e)
        {
            certPathException = new AnnotatedException(
                            "No valid certification path could be build.", e);
        }
        if (builderResult == null)
        {
            tbvPath.remove(tbvCert);
        }
        return builderResult;
    }

}
