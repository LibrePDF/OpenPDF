package old.org.bouncycastle.jce.provider;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Principal;
import java.security.PublicKey;
import java.security.cert.CertPath;
import java.security.cert.CertPathBuilder;
import java.security.cert.CertPathBuilderException;
import java.security.cert.CertPathBuilderResult;
import java.security.cert.CertPathValidator;
import java.security.cert.CertPathValidatorException;
import java.security.cert.CertPathValidatorResult;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.TrustAnchor;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.security.auth.x500.X500Principal;

import old.org.bouncycastle.asn1.ASN1InputStream;
import old.org.bouncycastle.asn1.DERObject;
import old.org.bouncycastle.asn1.x509.CRLDistPoint;
import old.org.bouncycastle.asn1.x509.CRLReason;
import old.org.bouncycastle.asn1.x509.DistributionPoint;
import old.org.bouncycastle.asn1.x509.DistributionPointName;
import old.org.bouncycastle.asn1.x509.GeneralName;
import old.org.bouncycastle.asn1.x509.GeneralNames;
import old.org.bouncycastle.asn1.x509.TargetInformation;
import old.org.bouncycastle.asn1.x509.X509Extensions;
import old.org.bouncycastle.jce.exception.ExtCertPathValidatorException;
import old.org.bouncycastle.x509.ExtendedPKIXBuilderParameters;
import old.org.bouncycastle.x509.ExtendedPKIXParameters;
import old.org.bouncycastle.x509.PKIXAttrCertChecker;
import old.org.bouncycastle.x509.X509AttributeCertificate;
import old.org.bouncycastle.x509.X509CertStoreSelector;

class RFC3281CertPathUtilities
{

    private static final String TARGET_INFORMATION = X509Extensions.TargetInformation
        .getId();

    private static final String NO_REV_AVAIL = X509Extensions.NoRevAvail
        .getId();

    private static final String CRL_DISTRIBUTION_POINTS = X509Extensions.CRLDistributionPoints
        .getId();

    private static final String AUTHORITY_INFO_ACCESS = X509Extensions.AuthorityInfoAccess
        .getId();

    protected static void processAttrCert7(X509AttributeCertificate attrCert,
        CertPath certPath, CertPath holderCertPath,
        ExtendedPKIXParameters pkixParams) throws CertPathValidatorException
    {
        // TODO:
        // AA Controls
        // Attribute encryption
        // Proxy
        Set set = attrCert.getCriticalExtensionOIDs();
        // 7.1
        // process extensions

        // target information checked in step 6 / X509AttributeCertStoreSelector
        if (set.contains(TARGET_INFORMATION))
        {
            try
            {
                TargetInformation.getInstance(CertPathValidatorUtilities
                    .getExtensionValue(attrCert, TARGET_INFORMATION));
            }
            catch (AnnotatedException e)
            {
                throw new ExtCertPathValidatorException(
                    "Target information extension could not be read.", e);
            }
            catch (IllegalArgumentException e)
            {
                throw new ExtCertPathValidatorException(
                    "Target information extension could not be read.", e);
            }
        }
        set.remove(TARGET_INFORMATION);
        for (Iterator it = pkixParams.getAttrCertCheckers().iterator(); it
            .hasNext();)
        {
            ((PKIXAttrCertChecker) it.next()).check(attrCert, certPath,
                holderCertPath, set);
        }
        if (!set.isEmpty())
        {
            throw new CertPathValidatorException(
                "Attribute certificate contains unsupported critical extensions: "
                    + set);
        }
    }

    /**
     * Checks if an attribute certificate is revoked.
     * 
     * @param attrCert Attribute certificate to check if it is revoked.
     * @param paramsPKIX PKIX parameters.
     * @param issuerCert The issuer certificate of the attribute certificate
     *            <code>attrCert</code>.
     * @param validDate The date when the certificate revocation status should
     *            be checked.
     * @param certPathCerts The certificates of the certification path to be
     *            checked.
     * 
     * @throws CertPathValidatorException if the certificate is revoked or the
     *             status cannot be checked or some error occurs.
     */
    protected static void checkCRLs(X509AttributeCertificate attrCert,
        ExtendedPKIXParameters paramsPKIX, X509Certificate issuerCert,
        Date validDate, List certPathCerts) throws CertPathValidatorException
    {
        if (paramsPKIX.isRevocationEnabled())
        {
            // check if revocation is available
            if (attrCert.getExtensionValue(NO_REV_AVAIL) == null)
            {
                CRLDistPoint crldp = null;
                try
                {
                    crldp = CRLDistPoint.getInstance(CertPathValidatorUtilities
                        .getExtensionValue(attrCert, CRL_DISTRIBUTION_POINTS));
                }
                catch (AnnotatedException e)
                {
                    throw new CertPathValidatorException(
                        "CRL distribution point extension could not be read.",
                        e);
                }
                try
                {
                    CertPathValidatorUtilities
                        .addAdditionalStoresFromCRLDistributionPoint(crldp,
                            paramsPKIX);
                }
                catch (AnnotatedException e)
                {
                    throw new CertPathValidatorException(
                        "No additional CRL locations could be decoded from CRL distribution point extension.",
                        e);
                }
                CertStatus certStatus = new CertStatus();
                ReasonsMask reasonsMask = new ReasonsMask();

                AnnotatedException lastException = null;
                boolean validCrlFound = false;
                // for each distribution point
                if (crldp != null)
                {
                    DistributionPoint dps[] = null;
                    try
                    {
                        dps = crldp.getDistributionPoints();
                    }
                    catch (Exception e)
                    {
                        throw new ExtCertPathValidatorException(
                            "Distribution points could not be read.", e);
                    }
                    try
                    {
                        for (int i = 0; i < dps.length
                            && certStatus.getCertStatus() == CertStatus.UNREVOKED
                            && !reasonsMask.isAllReasons(); i++)
                        {
                            ExtendedPKIXParameters paramsPKIXClone = (ExtendedPKIXParameters) paramsPKIX
                                .clone();
                            checkCRL(dps[i], attrCert, paramsPKIXClone,
                                validDate, issuerCert, certStatus, reasonsMask,
                                certPathCerts);
                            validCrlFound = true;
                        }
                    }
                    catch (AnnotatedException e)
                    {
                        lastException = new AnnotatedException(
                            "No valid CRL for distribution point found.", e);
                    }
                }

                /*
                 * If the revocation status has not been determined, repeat the
                 * process above with any available CRLs not specified in a
                 * distribution point but issued by the certificate issuer.
                 */

                if (certStatus.getCertStatus() == CertStatus.UNREVOKED
                    && !reasonsMask.isAllReasons())
                {
                    try
                    {
                        /*
                         * assume a DP with both the reasons and the cRLIssuer
                         * fields omitted and a distribution point name of the
                         * certificate issuer.
                         */
                        DERObject issuer = null;
                        try
                        {

                            issuer = new ASN1InputStream(
                                ((X500Principal) attrCert.getIssuer()
                                    .getPrincipals()[0]).getEncoded())
                                .readObject();
                        }
                        catch (Exception e)
                        {
                            throw new AnnotatedException(
                                "Issuer from certificate for CRL could not be reencoded.",
                                e);
                        }
                        DistributionPoint dp = new DistributionPoint(
                            new DistributionPointName(0, new GeneralNames(
                                new GeneralName(GeneralName.directoryName,
                                    issuer))), null, null);
                        ExtendedPKIXParameters paramsPKIXClone = (ExtendedPKIXParameters) paramsPKIX
                            .clone();
                        checkCRL(dp, attrCert, paramsPKIXClone, validDate,
                            issuerCert, certStatus, reasonsMask, certPathCerts);
                        validCrlFound = true;
                    }
                    catch (AnnotatedException e)
                    {
                        lastException = new AnnotatedException(
                            "No valid CRL for distribution point found.", e);
                    }
                }

                if (!validCrlFound)
                {
                    throw new ExtCertPathValidatorException(
                        "No valid CRL found.", lastException);
                }
                if (certStatus.getCertStatus() != CertStatus.UNREVOKED)
                {
                    String message = "Attribute certificate revocation after "
                        + certStatus.getRevocationDate();
                    message += ", reason: "
                        + RFC3280CertPathUtilities.crlReasons[certStatus
                            .getCertStatus()];
                    throw new CertPathValidatorException(message);
                }
                if (!reasonsMask.isAllReasons()
                    && certStatus.getCertStatus() == CertStatus.UNREVOKED)
                {
                    certStatus.setCertStatus(CertStatus.UNDETERMINED);
                }
                if (certStatus.getCertStatus() == CertStatus.UNDETERMINED)
                {
                    throw new CertPathValidatorException(
                        "Attribute certificate status could not be determined.");
                }

            }
            else
            {
                if (attrCert.getExtensionValue(CRL_DISTRIBUTION_POINTS) != null
                    || attrCert.getExtensionValue(AUTHORITY_INFO_ACCESS) != null)
                {
                    throw new CertPathValidatorException(
                        "No rev avail extension is set, but also an AC revocation pointer.");
                }
            }
        }
    }

    protected static void additionalChecks(X509AttributeCertificate attrCert,
        ExtendedPKIXParameters pkixParams) throws CertPathValidatorException
    {
        // 1
        for (Iterator it = pkixParams.getProhibitedACAttributes().iterator(); it
            .hasNext();)
        {
            String oid = (String) it.next();
            if (attrCert.getAttributes(oid) != null)
            {
                throw new CertPathValidatorException(
                    "Attribute certificate contains prohibited attribute: "
                        + oid + ".");
            }
        }
        for (Iterator it = pkixParams.getNecessaryACAttributes().iterator(); it
            .hasNext();)
        {
            String oid = (String) it.next();
            if (attrCert.getAttributes(oid) == null)
            {
                throw new CertPathValidatorException(
                    "Attribute certificate does not contain necessary attribute: "
                        + oid + ".");
            }
        }
    }

    protected static void processAttrCert5(X509AttributeCertificate attrCert,
        ExtendedPKIXParameters pkixParams) throws CertPathValidatorException
    {
        try
        {
            attrCert.checkValidity(CertPathValidatorUtilities
                .getValidDate(pkixParams));
        }
        catch (CertificateExpiredException e)
        {
            throw new ExtCertPathValidatorException(
                "Attribute certificate is not valid.", e);
        }
        catch (CertificateNotYetValidException e)
        {
            throw new ExtCertPathValidatorException(
                "Attribute certificate is not valid.", e);
        }
    }

    protected static void processAttrCert4(X509Certificate acIssuerCert,
        ExtendedPKIXParameters pkixParams) throws CertPathValidatorException
    {
        Set set = pkixParams.getTrustedACIssuers();
        boolean trusted = false;
        for (Iterator it = set.iterator(); it.hasNext();)
        {
            TrustAnchor anchor = (TrustAnchor) it.next();
            if (acIssuerCert.getSubjectX500Principal().getName("RFC2253")
                .equals(anchor.getCAName())
                || acIssuerCert.equals(anchor.getTrustedCert()))
            {
                trusted = true;
            }
        }
        if (!trusted)
        {
            throw new CertPathValidatorException(
                "Attribute certificate issuer is not directly trusted.");
        }
    }

    protected static void processAttrCert3(X509Certificate acIssuerCert,
        ExtendedPKIXParameters pkixParams) throws CertPathValidatorException
    {
        if (acIssuerCert.getKeyUsage() != null
            && (!acIssuerCert.getKeyUsage()[0] && !acIssuerCert.getKeyUsage()[1]))
        {
            throw new CertPathValidatorException(
                "Attribute certificate issuer public key cannot be used to validate digital signatures.");
        }
        if (acIssuerCert.getBasicConstraints() != -1)
        {
            throw new CertPathValidatorException(
                "Attribute certificate issuer is also a public key certificate issuer.");
        }
    }

    protected static CertPathValidatorResult processAttrCert2(
        CertPath certPath, ExtendedPKIXParameters pkixParams)
        throws CertPathValidatorException
    {
        CertPathValidator validator = null;
        try
        {
            validator = CertPathValidator.getInstance("PKIX", BouncyCastleProvider.PROVIDER_NAME);
        }
        catch (NoSuchProviderException e)
        {
            throw new ExtCertPathValidatorException(
                "Support class could not be created.", e);
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new ExtCertPathValidatorException(
                "Support class could not be created.", e);
        }
        try
        {
            return validator.validate(certPath, pkixParams);
        }
        catch (CertPathValidatorException e)
        {
            throw new ExtCertPathValidatorException(
                "Certification path for issuer certificate of attribute certificate could not be validated.",
                e);
        }
        catch (InvalidAlgorithmParameterException e)
        {
            // must be a programming error
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * Searches for a holder public key certificate and verifies its
     * certification path.
     * 
     * @param attrCert the attribute certificate.
     * @param pkixParams The PKIX parameters.
     * @return The certificate path of the holder certificate.
     * @throws AnnotatedException if
     *             <ul>
     *             <li>no public key certificate can be found although holder
     *             information is given by an entity name or a base certificate
     *             ID
     *             <li>support classes cannot be created
     *             <li>no certification path for the public key certificate can
     *             be built
     *             </ul>
     */
    protected static CertPath processAttrCert1(
        X509AttributeCertificate attrCert, ExtendedPKIXParameters pkixParams)
        throws CertPathValidatorException
    {
        CertPathBuilderResult result = null;
        // find holder PKCs
        Set holderPKCs = new HashSet();
        if (attrCert.getHolder().getIssuer() != null)
        {
            X509CertStoreSelector selector = new X509CertStoreSelector();
            selector.setSerialNumber(attrCert.getHolder().getSerialNumber());
            Principal[] principals = attrCert.getHolder().getIssuer();
            for (int i = 0; i < principals.length; i++)
            {
                try
                {
                    if (principals[i] instanceof X500Principal)
                    {
                        selector.setIssuer(((X500Principal)principals[i])
                            .getEncoded());
                    }
                    holderPKCs.addAll(CertPathValidatorUtilities
                        .findCertificates(selector, pkixParams.getStores()));
                }
                catch (AnnotatedException e)
                {
                    throw new ExtCertPathValidatorException(
                        "Public key certificate for attribute certificate cannot be searched.",
                        e);
                }
                catch (IOException e)
                {
                    throw new ExtCertPathValidatorException(
                        "Unable to encode X500 principal.", e);
                }
            }
            if (holderPKCs.isEmpty())
            {
                throw new CertPathValidatorException(
                    "Public key certificate specified in base certificate ID for attribute certificate cannot be found.");
            }
        }
        if (attrCert.getHolder().getEntityNames() != null)
        {
            X509CertStoreSelector selector = new X509CertStoreSelector();
            Principal[] principals = attrCert.getHolder().getEntityNames();
            for (int i = 0; i < principals.length; i++)
            {
                try
                {
                    if (principals[i] instanceof X500Principal)
                    {
                        selector.setIssuer(((X500Principal) principals[i])
                            .getEncoded());
                    }
                    holderPKCs.addAll(CertPathValidatorUtilities
                        .findCertificates(selector, pkixParams.getStores()));
                }
                catch (AnnotatedException e)
                {
                    throw new ExtCertPathValidatorException(
                        "Public key certificate for attribute certificate cannot be searched.",
                        e);
                }
                catch (IOException e)
                {
                    throw new ExtCertPathValidatorException(
                        "Unable to encode X500 principal.", e);
                }
            }
            if (holderPKCs.isEmpty())
            {
                throw new CertPathValidatorException(
                    "Public key certificate specified in entity name for attribute certificate cannot be found.");
            }
        }
        // verify cert paths for PKCs
        ExtendedPKIXBuilderParameters params = (ExtendedPKIXBuilderParameters) ExtendedPKIXBuilderParameters
            .getInstance(pkixParams);
        CertPathValidatorException lastException = null;
        for (Iterator it = holderPKCs.iterator(); it.hasNext();)
        {
            X509CertStoreSelector selector = new X509CertStoreSelector();
            selector.setCertificate((X509Certificate) it.next());
            params.setTargetConstraints(selector);
            CertPathBuilder builder = null;
            try
            {
                builder = CertPathBuilder.getInstance("PKIX", BouncyCastleProvider.PROVIDER_NAME);
            }
            catch (NoSuchProviderException e)
            {
                throw new ExtCertPathValidatorException(
                    "Support class could not be created.", e);
            }
            catch (NoSuchAlgorithmException e)
            {
                throw new ExtCertPathValidatorException(
                    "Support class could not be created.", e);
            }
            try
            {
                result = builder.build(ExtendedPKIXBuilderParameters
                    .getInstance(params));
            }
            catch (CertPathBuilderException e)
            {
                lastException = new ExtCertPathValidatorException(
                    "Certification path for public key certificate of attribute certificate could not be build.",
                    e);
            }
            catch (InvalidAlgorithmParameterException e)
            {
                // must be a programming error
                throw new RuntimeException(e.getMessage());
            }
        }
        if (lastException != null)
        {
            throw lastException;
        }
        return result.getCertPath();
    }

    /**
     * 
     * Checks a distribution point for revocation information for the
     * certificate <code>attrCert</code>.
     * 
     * @param dp The distribution point to consider.
     * @param attrCert The attribute certificate which should be checked.
     * @param paramsPKIX PKIX parameters.
     * @param validDate The date when the certificate revocation status should
     *            be checked.
     * @param issuerCert Certificate to check if it is revoked.
     * @param reasonMask The reasons mask which is already checked.
     * @param certPathCerts The certificates of the certification path to be
     *            checked.
     * @throws AnnotatedException if the certificate is revoked or the status
     *             cannot be checked or some error occurs.
     */
    private static void checkCRL(DistributionPoint dp,
        X509AttributeCertificate attrCert, ExtendedPKIXParameters paramsPKIX,
        Date validDate, X509Certificate issuerCert, CertStatus certStatus,
        ReasonsMask reasonMask, List certPathCerts) throws AnnotatedException
    {

        /*
         * 4.3.6 No Revocation Available
         * 
         * The noRevAvail extension, defined in [X.509-2000], allows an AC
         * issuer to indicate that no revocation information will be made
         * available for this AC.
         */
        if (attrCert.getExtensionValue(X509Extensions.NoRevAvail.getId()) != null)
        {
            return;
        }
        Date currentDate = new Date(System.currentTimeMillis());
        if (validDate.getTime() > currentDate.getTime())
        {
            throw new AnnotatedException("Validation time is in future.");
        }

        // (a)
        /*
         * We always get timely valid CRLs, so there is no step (a) (1).
         * "locally cached" CRLs are assumed to be in getStore(), additional
         * CRLs must be enabled in the ExtendedPKIXParameters and are in
         * getAdditionalStore()
         */

        Set crls = CertPathValidatorUtilities.getCompleteCRLs(dp, attrCert,
            currentDate, paramsPKIX);
        boolean validCrlFound = false;
        AnnotatedException lastException = null;
        Iterator crl_iter = crls.iterator();

        while (crl_iter.hasNext()
            && certStatus.getCertStatus() == CertStatus.UNREVOKED
            && !reasonMask.isAllReasons())
        {
            try
            {
                X509CRL crl = (X509CRL) crl_iter.next();

                // (d)
                ReasonsMask interimReasonsMask = RFC3280CertPathUtilities
                    .processCRLD(crl, dp);

                // (e)
                /*
                 * The reasons mask is updated at the end, so only valid CRLs
                 * can update it. If this CRL does not contain new reasons it
                 * must be ignored.
                 */
                if (!interimReasonsMask.hasNewReasons(reasonMask))
                {
                    continue;
                }

                // (f)
                Set keys = RFC3280CertPathUtilities.processCRLF(crl, attrCert,
                    null, null, paramsPKIX, certPathCerts);
                // (g)
                PublicKey key = RFC3280CertPathUtilities.processCRLG(crl, keys);

                X509CRL deltaCRL = null;

                if (paramsPKIX.isUseDeltasEnabled())
                {
                    // get delta CRLs
                    Set deltaCRLs = CertPathValidatorUtilities.getDeltaCRLs(
                        currentDate, paramsPKIX, crl);
                    // we only want one valid delta CRL
                    // (h)
                    deltaCRL = RFC3280CertPathUtilities.processCRLH(deltaCRLs,
                        key);
                }

                /*
                 * CRL must be be valid at the current time, not the validation
                 * time. If a certificate is revoked with reason keyCompromise,
                 * cACompromise, it can be used for forgery, also for the past.
                 * This reason may not be contained in older CRLs.
                 */

                /*
                 * in the chain model signatures stay valid also after the
                 * certificate has been expired, so they do not have to be in
                 * the CRL vality time
                 */

                if (paramsPKIX.getValidityModel() != ExtendedPKIXParameters.CHAIN_VALIDITY_MODEL)
                {
                    /*
                     * if a certificate has expired, but was revoked, it is not
                     * more in the CRL, so it would be regarded as valid if the
                     * first check is not done
                     */
                    if (attrCert.getNotAfter().getTime() < crl.getThisUpdate()
                        .getTime())
                    {
                        throw new AnnotatedException(
                            "No valid CRL for current time found.");
                    }
                }

                RFC3280CertPathUtilities.processCRLB1(dp, attrCert, crl);

                // (b) (2)
                RFC3280CertPathUtilities.processCRLB2(dp, attrCert, crl);

                // (c)
                RFC3280CertPathUtilities.processCRLC(deltaCRL, crl, paramsPKIX);

                // (i)
                RFC3280CertPathUtilities.processCRLI(validDate, deltaCRL,
                    attrCert, certStatus, paramsPKIX);

                // (j)
                RFC3280CertPathUtilities.processCRLJ(validDate, crl, attrCert,
                    certStatus);

                // (k)
                if (certStatus.getCertStatus() == CRLReason.removeFromCRL)
                {
                    certStatus.setCertStatus(CertStatus.UNREVOKED);
                }

                // update reasons mask
                reasonMask.addReasons(interimReasonsMask);
                validCrlFound = true;
            }
            catch (AnnotatedException e)
            {
                lastException = e;
            }
        }
        if (!validCrlFound)
        {
            throw lastException;
        }
    }
}
