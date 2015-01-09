package old.org.bouncycastle.jce.provider;

import java.io.IOException;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.PublicKey;
import java.security.cert.CertPath;
import java.security.cert.CertPathBuilder;
import java.security.cert.CertPathBuilderException;
import java.security.cert.CertPathValidatorException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.PKIXCertPathChecker;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.security.cert.X509Extension;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.security.auth.x500.X500Principal;

import old.org.bouncycastle.asn1.ASN1EncodableVector;
import old.org.bouncycastle.asn1.ASN1InputStream;
import old.org.bouncycastle.asn1.ASN1Sequence;
import old.org.bouncycastle.asn1.ASN1TaggedObject;
import old.org.bouncycastle.asn1.DEREncodable;
import old.org.bouncycastle.asn1.DERInteger;
import old.org.bouncycastle.asn1.DERObject;
import old.org.bouncycastle.asn1.DERObjectIdentifier;
import old.org.bouncycastle.asn1.DERSequence;
import old.org.bouncycastle.asn1.x509.BasicConstraints;
import old.org.bouncycastle.asn1.x509.CRLDistPoint;
import old.org.bouncycastle.asn1.x509.CRLReason;
import old.org.bouncycastle.asn1.x509.DistributionPoint;
import old.org.bouncycastle.asn1.x509.DistributionPointName;
import old.org.bouncycastle.asn1.x509.GeneralName;
import old.org.bouncycastle.asn1.x509.GeneralNames;
import old.org.bouncycastle.asn1.x509.GeneralSubtree;
import old.org.bouncycastle.asn1.x509.IssuingDistributionPoint;
import old.org.bouncycastle.asn1.x509.NameConstraints;
import old.org.bouncycastle.asn1.x509.PolicyInformation;
import old.org.bouncycastle.asn1.x509.X509Extensions;
import old.org.bouncycastle.asn1.x509.X509Name;
import old.org.bouncycastle.jce.exception.ExtCertPathValidatorException;
import old.org.bouncycastle.util.Arrays;
import old.org.bouncycastle.x509.ExtendedPKIXBuilderParameters;
import old.org.bouncycastle.x509.ExtendedPKIXParameters;
import old.org.bouncycastle.x509.X509CRLStoreSelector;
import old.org.bouncycastle.x509.X509CertStoreSelector;

public class RFC3280CertPathUtilities
{
    private static final PKIXCRLUtil CRL_UTIL = new PKIXCRLUtil();

    /**
     * If the complete CRL includes an issuing distribution point (IDP) CRL
     * extension check the following:
     * <p/>
     * (i) If the distribution point name is present in the IDP CRL extension
     * and the distribution field is present in the DP, then verify that one of
     * the names in the IDP matches one of the names in the DP. If the
     * distribution point name is present in the IDP CRL extension and the
     * distribution field is omitted from the DP, then verify that one of the
     * names in the IDP matches one of the names in the cRLIssuer field of the
     * DP.
     * </p>
     * <p/>
     * (ii) If the onlyContainsUserCerts boolean is asserted in the IDP CRL
     * extension, verify that the certificate does not include the basic
     * constraints extension with the cA boolean asserted.
     * </p>
     * <p/>
     * (iii) If the onlyContainsCACerts boolean is asserted in the IDP CRL
     * extension, verify that the certificate includes the basic constraints
     * extension with the cA boolean asserted.
     * </p>
     * <p/>
     * (iv) Verify that the onlyContainsAttributeCerts boolean is not asserted.
     * </p>
     *
     * @param dp   The distribution point.
     * @param cert The certificate.
     * @param crl  The CRL.
     * @throws AnnotatedException if one of the conditions is not met or an error occurs.
     */
    protected static void processCRLB2(
        DistributionPoint dp,
        Object cert,
        X509CRL crl)
        throws AnnotatedException
    {
        IssuingDistributionPoint idp = null;
        try
        {
            idp = IssuingDistributionPoint.getInstance(CertPathValidatorUtilities.getExtensionValue(crl,
                RFC3280CertPathUtilities.ISSUING_DISTRIBUTION_POINT));
        }
        catch (Exception e)
        {
            throw new AnnotatedException("Issuing distribution point extension could not be decoded.", e);
        }
        // (b) (2) (i)
        // distribution point name is present
        if (idp != null)
        {
            if (idp.getDistributionPoint() != null)
            {
                // make list of names
                DistributionPointName dpName = IssuingDistributionPoint.getInstance(idp).getDistributionPoint();
                List names = new ArrayList();

                if (dpName.getType() == DistributionPointName.FULL_NAME)
                {
                    GeneralName[] genNames = GeneralNames.getInstance(dpName.getName()).getNames();
                    for (int j = 0; j < genNames.length; j++)
                    {
                        names.add(genNames[j]);
                    }
                }
                if (dpName.getType() == DistributionPointName.NAME_RELATIVE_TO_CRL_ISSUER)
                {
                    ASN1EncodableVector vec = new ASN1EncodableVector();
                    try
                    {
                        Enumeration e = ASN1Sequence.getInstance(
                            ASN1Sequence.fromByteArray(CertPathValidatorUtilities.getIssuerPrincipal(crl)
                                .getEncoded())).getObjects();
                        while (e.hasMoreElements())
                        {
                            vec.add((DEREncodable)e.nextElement());
                        }
                    }
                    catch (IOException e)
                    {
                        throw new AnnotatedException("Could not read CRL issuer.", e);
                    }
                    vec.add(dpName.getName());
                    names.add(new GeneralName(X509Name.getInstance(new DERSequence(vec))));
                }
                boolean matches = false;
                // verify that one of the names in the IDP matches one
                // of the names in the DP.
                if (dp.getDistributionPoint() != null)
                {
                    dpName = dp.getDistributionPoint();
                    GeneralName[] genNames = null;
                    if (dpName.getType() == DistributionPointName.FULL_NAME)
                    {
                        genNames = GeneralNames.getInstance(dpName.getName()).getNames();
                    }
                    if (dpName.getType() == DistributionPointName.NAME_RELATIVE_TO_CRL_ISSUER)
                    {
                        if (dp.getCRLIssuer() != null)
                        {
                            genNames = dp.getCRLIssuer().getNames();
                        }
                        else
                        {
                            genNames = new GeneralName[1];
                            try
                            {
                                genNames[0] = new GeneralName(new X509Name(
                                    (ASN1Sequence)ASN1Sequence.fromByteArray(CertPathValidatorUtilities
                                        .getEncodedIssuerPrincipal(cert).getEncoded())));
                            }
                            catch (IOException e)
                            {
                                throw new AnnotatedException("Could not read certificate issuer.", e);
                            }
                        }
                        for (int j = 0; j < genNames.length; j++)
                        {
                            Enumeration e = ASN1Sequence.getInstance(genNames[j].getName().getDERObject()).getObjects();
                            ASN1EncodableVector vec = new ASN1EncodableVector();
                            while (e.hasMoreElements())
                            {
                                vec.add((DEREncodable)e.nextElement());
                            }
                            vec.add(dpName.getName());
                            genNames[j] = new GeneralName(new X509Name(new DERSequence(vec)));
                        }
                    }
                    if (genNames != null)
                    {
                        for (int j = 0; j < genNames.length; j++)
                        {
                            if (names.contains(genNames[j]))
                            {
                                matches = true;
                                break;
                            }
                        }
                    }
                    if (!matches)
                    {
                        throw new AnnotatedException(
                            "No match for certificate CRL issuing distribution point name to cRLIssuer CRL distribution point.");
                    }
                }
                // verify that one of the names in
                // the IDP matches one of the names in the cRLIssuer field of
                // the DP
                else
                {
                    if (dp.getCRLIssuer() == null)
                    {
                        throw new AnnotatedException("Either the cRLIssuer or the distributionPoint field must "
                            + "be contained in DistributionPoint.");
                    }
                    GeneralName[] genNames = dp.getCRLIssuer().getNames();
                    for (int j = 0; j < genNames.length; j++)
                    {
                        if (names.contains(genNames[j]))
                        {
                            matches = true;
                            break;
                        }
                    }
                    if (!matches)
                    {
                        throw new AnnotatedException(
                            "No match for certificate CRL issuing distribution point name to cRLIssuer CRL distribution point.");
                    }
                }
            }
            BasicConstraints bc = null;
            try
            {
                bc = BasicConstraints.getInstance(CertPathValidatorUtilities.getExtensionValue((X509Extension)cert,
                    BASIC_CONSTRAINTS));
            }
            catch (Exception e)
            {
                throw new AnnotatedException("Basic constraints extension could not be decoded.", e);
            }

            if (cert instanceof X509Certificate)
            {
                // (b) (2) (ii)
                if (idp.onlyContainsUserCerts() && (bc != null && bc.isCA()))
                {
                    throw new AnnotatedException("CA Cert CRL only contains user certificates.");
                }

                // (b) (2) (iii)
                if (idp.onlyContainsCACerts() && (bc == null || !bc.isCA()))
                {
                    throw new AnnotatedException("End CRL only contains CA certificates.");
                }
            }

            // (b) (2) (iv)
            if (idp.onlyContainsAttributeCerts())
            {
                throw new AnnotatedException("onlyContainsAttributeCerts boolean is asserted.");
            }
        }
    }

    /**
     * If the DP includes cRLIssuer, then verify that the issuer field in the
     * complete CRL matches cRLIssuer in the DP and that the complete CRL
     * contains an issuing distribution point extension with the indirectCRL
     * boolean asserted. Otherwise, verify that the CRL issuer matches the
     * certificate issuer.
     *
     * @param dp   The distribution point.
     * @param cert The certificate ot attribute certificate.
     * @param crl  The CRL for <code>cert</code>.
     * @throws AnnotatedException if one of the above conditions does not apply or an error
     *                            occurs.
     */
    protected static void processCRLB1(
        DistributionPoint dp,
        Object cert,
        X509CRL crl)
        throws AnnotatedException
    {
        DERObject idp = CertPathValidatorUtilities.getExtensionValue(crl, ISSUING_DISTRIBUTION_POINT);
        boolean isIndirect = false;
        if (idp != null)
        {
            if (IssuingDistributionPoint.getInstance(idp).isIndirectCRL())
            {
                isIndirect = true;
            }
        }
        byte[] issuerBytes = CertPathValidatorUtilities.getIssuerPrincipal(crl).getEncoded();

        boolean matchIssuer = false;
        if (dp.getCRLIssuer() != null)
        {
            GeneralName genNames[] = dp.getCRLIssuer().getNames();
            for (int j = 0; j < genNames.length; j++)
            {
                if (genNames[j].getTagNo() == GeneralName.directoryName)
                {
                    try
                    {
                        if (Arrays.areEqual(genNames[j].getName().getDERObject().getEncoded(), issuerBytes))
                        {
                            matchIssuer = true;
                        }
                    }
                    catch (IOException e)
                    {
                        throw new AnnotatedException(
                            "CRL issuer information from distribution point cannot be decoded.", e);
                    }
                }
            }
            if (matchIssuer && !isIndirect)
            {
                throw new AnnotatedException("Distribution point contains cRLIssuer field but CRL is not indirect.");
            }
            if (!matchIssuer)
            {
                throw new AnnotatedException("CRL issuer of CRL does not match CRL issuer of distribution point.");
            }
        }
        else
        {
            if (CertPathValidatorUtilities.getIssuerPrincipal(crl).equals(
                CertPathValidatorUtilities.getEncodedIssuerPrincipal(cert)))
            {
                matchIssuer = true;
            }
        }
        if (!matchIssuer)
        {
            throw new AnnotatedException("Cannot find matching CRL issuer for certificate.");
        }
    }

    protected static ReasonsMask processCRLD(
        X509CRL crl,
        DistributionPoint dp)
        throws AnnotatedException
    {
        IssuingDistributionPoint idp = null;
        try
        {
            idp = IssuingDistributionPoint.getInstance(CertPathValidatorUtilities.getExtensionValue(crl,
                RFC3280CertPathUtilities.ISSUING_DISTRIBUTION_POINT));
        }
        catch (Exception e)
        {
            throw new AnnotatedException("Issuing distribution point extension could not be decoded.", e);
        }
        // (d) (1)
        if (idp != null && idp.getOnlySomeReasons() != null && dp.getReasons() != null)
        {
            return new ReasonsMask(dp.getReasons().intValue()).intersect(new ReasonsMask(idp.getOnlySomeReasons()
                .intValue()));
        }
        // (d) (4)
        if ((idp == null || idp.getOnlySomeReasons() == null) && dp.getReasons() == null)
        {
            return ReasonsMask.allReasons;
        }
        // (d) (2) and (d)(3)
        return (dp.getReasons() == null
            ? ReasonsMask.allReasons
            : new ReasonsMask(dp.getReasons().intValue())).intersect(idp == null
            ? ReasonsMask.allReasons
            : new ReasonsMask(idp.getOnlySomeReasons().intValue()));

    }

    protected static final String CERTIFICATE_POLICIES = X509Extensions.CertificatePolicies.getId();

    protected static final String POLICY_MAPPINGS = X509Extensions.PolicyMappings.getId();

    protected static final String INHIBIT_ANY_POLICY = X509Extensions.InhibitAnyPolicy.getId();

    protected static final String ISSUING_DISTRIBUTION_POINT = X509Extensions.IssuingDistributionPoint.getId();

    protected static final String FRESHEST_CRL = X509Extensions.FreshestCRL.getId();

    protected static final String DELTA_CRL_INDICATOR = X509Extensions.DeltaCRLIndicator.getId();

    protected static final String POLICY_CONSTRAINTS = X509Extensions.PolicyConstraints.getId();

    protected static final String BASIC_CONSTRAINTS = X509Extensions.BasicConstraints.getId();

    protected static final String CRL_DISTRIBUTION_POINTS = X509Extensions.CRLDistributionPoints.getId();

    protected static final String SUBJECT_ALTERNATIVE_NAME = X509Extensions.SubjectAlternativeName.getId();

    protected static final String NAME_CONSTRAINTS = X509Extensions.NameConstraints.getId();

    protected static final String AUTHORITY_KEY_IDENTIFIER = X509Extensions.AuthorityKeyIdentifier.getId();

    protected static final String KEY_USAGE = X509Extensions.KeyUsage.getId();

    protected static final String CRL_NUMBER = X509Extensions.CRLNumber.getId();

    protected static final String ANY_POLICY = "2.5.29.32.0";

    /*
     * key usage bits
     */
    protected static final int KEY_CERT_SIGN = 5;

    protected static final int CRL_SIGN = 6;

    /**
     * Obtain and validate the certification path for the complete CRL issuer.
     * If a key usage extension is present in the CRL issuer's certificate,
     * verify that the cRLSign bit is set.
     *
     * @param crl                CRL which contains revocation information for the certificate
     *                           <code>cert</code>.
     * @param cert               The attribute certificate or certificate to check if it is
     *                           revoked.
     * @param defaultCRLSignCert The issuer certificate of the certificate <code>cert</code>.
     * @param defaultCRLSignKey  The public key of the issuer certificate
     *                           <code>defaultCRLSignCert</code>.
     * @param paramsPKIX         paramsPKIX PKIX parameters.
     * @param certPathCerts      The certificates on the certification path.
     * @return A <code>Set</code> with all keys of possible CRL issuer
     *         certificates.
     * @throws AnnotatedException if the CRL is not valid or the status cannot be checked or
     *                            some error occurs.
     */
    protected static Set processCRLF(
        X509CRL crl,
        Object cert,
        X509Certificate defaultCRLSignCert,
        PublicKey defaultCRLSignKey,
        ExtendedPKIXParameters paramsPKIX,
        List certPathCerts)
        throws AnnotatedException
    {
        // (f)

        // get issuer from CRL
        X509CertStoreSelector selector = new X509CertStoreSelector();
        try
        {
            byte[] issuerPrincipal = CertPathValidatorUtilities.getIssuerPrincipal(crl).getEncoded();
            selector.setSubject(issuerPrincipal);
        }
        catch (IOException e)
        {
            throw new AnnotatedException(
                "Subject criteria for certificate selector to find issuer certificate for CRL could not be set.", e);
        }

        // get CRL signing certs
        Collection coll;
        try
        {
            coll = CertPathValidatorUtilities.findCertificates(selector, paramsPKIX.getStores());
            coll.addAll(CertPathValidatorUtilities.findCertificates(selector, paramsPKIX.getAdditionalStores()));
            coll.addAll(CertPathValidatorUtilities.findCertificates(selector, paramsPKIX.getCertStores()));
        }
        catch (AnnotatedException e)
        {
            throw new AnnotatedException("Issuer certificate for CRL cannot be searched.", e);
        }

        coll.add(defaultCRLSignCert);

        Iterator cert_it = coll.iterator();

        List validCerts = new ArrayList();
        List validKeys = new ArrayList();

        while (cert_it.hasNext())
        {
            X509Certificate signingCert = (X509Certificate)cert_it.next();

            /*
             * CA of the certificate, for which this CRL is checked, has also
             * signed CRL, so skip the path validation, because is already done
             */
            if (signingCert.equals(defaultCRLSignCert))
            {
                validCerts.add(signingCert);
                validKeys.add(defaultCRLSignKey);
                continue;
            }
            try
            {
                CertPathBuilder builder = CertPathBuilder.getInstance("PKIX", BouncyCastleProvider.PROVIDER_NAME);
                selector = new X509CertStoreSelector();
                selector.setCertificate(signingCert);
                ExtendedPKIXParameters temp = (ExtendedPKIXParameters)paramsPKIX.clone();
                temp.setTargetCertConstraints(selector);
                ExtendedPKIXBuilderParameters params = (ExtendedPKIXBuilderParameters)ExtendedPKIXBuilderParameters
                    .getInstance(temp);
                /*
                 * if signingCert is placed not higher on the cert path a
                 * dependency loop results. CRL for cert is checked, but
                 * signingCert is needed for checking the CRL which is dependent
                 * on checking cert because it is higher in the cert path and so
                 * signing signingCert transitively. so, revocation is disabled,
                 * forgery attacks of the CRL are detected in this outer loop
                 * for all other it must be enabled to prevent forgery attacks
                 */
                if (certPathCerts.contains(signingCert))
                {
                    params.setRevocationEnabled(false);
                }
                else
                {
                    params.setRevocationEnabled(true);
                }
                List certs = builder.build(params).getCertPath().getCertificates();
                validCerts.add(signingCert);
                validKeys.add(CertPathValidatorUtilities.getNextWorkingKey(certs, 0));
            }
            catch (CertPathBuilderException e)
            {
                throw new AnnotatedException("Internal error.", e);
            }
            catch (CertPathValidatorException e)
            {
                throw new AnnotatedException("Public key of issuer certificate of CRL could not be retrieved.", e);
            }
            catch (Exception e)
            {
                throw new RuntimeException(e.getMessage());
            }
        }

        Set checkKeys = new HashSet();

        AnnotatedException lastException = null;
        for (int i = 0; i < validCerts.size(); i++)
        {
            X509Certificate signCert = (X509Certificate)validCerts.get(i);
            boolean[] keyusage = signCert.getKeyUsage();

            if (keyusage != null && (keyusage.length < 7 || !keyusage[CRL_SIGN]))
            {
                lastException = new AnnotatedException(
                    "Issuer certificate key usage extension does not permit CRL signing.");
            }
            else
            {
                checkKeys.add(validKeys.get(i));
            }
        }

        if (checkKeys.isEmpty() && lastException == null)
        {
            throw new AnnotatedException("Cannot find a valid issuer certificate.");
        }
        if (checkKeys.isEmpty() && lastException != null)
        {
            throw lastException;
        }

        return checkKeys;
    }

    protected static PublicKey processCRLG(
        X509CRL crl,
        Set keys)
        throws AnnotatedException
    {
        Exception lastException = null;
        for (Iterator it = keys.iterator(); it.hasNext();)
        {
            PublicKey key = (PublicKey)it.next();
            try
            {
                crl.verify(key);
                return key;
            }
            catch (Exception e)
            {
                lastException = e;
            }
        }
        throw new AnnotatedException("Cannot verify CRL.", lastException);
    }

    protected static X509CRL processCRLH(
        Set deltacrls,
        PublicKey key)
        throws AnnotatedException
    {
        Exception lastException = null;

        for (Iterator it = deltacrls.iterator(); it.hasNext();)
        {
            X509CRL crl = (X509CRL)it.next();
            try
            {
                crl.verify(key);
                return crl;
            }
            catch (Exception e)
            {
                lastException = e;
            }
        }

        if (lastException != null)
        {
            throw new AnnotatedException("Cannot verify delta CRL.", lastException);
        }
        return null;
    }

    protected static Set processCRLA1i(
        Date currentDate,
        ExtendedPKIXParameters paramsPKIX,
        X509Certificate cert,
        X509CRL crl)
        throws AnnotatedException
    {
        Set set = new HashSet();
        if (paramsPKIX.isUseDeltasEnabled())
        {
            CRLDistPoint freshestCRL = null;
            try
            {
                freshestCRL = CRLDistPoint
                    .getInstance(CertPathValidatorUtilities.getExtensionValue(cert, FRESHEST_CRL));
            }
            catch (AnnotatedException e)
            {
                throw new AnnotatedException("Freshest CRL extension could not be decoded from certificate.", e);
            }
            if (freshestCRL == null)
            {
                try
                {
                    freshestCRL = CRLDistPoint.getInstance(CertPathValidatorUtilities.getExtensionValue(crl,
                        FRESHEST_CRL));
                }
                catch (AnnotatedException e)
                {
                    throw new AnnotatedException("Freshest CRL extension could not be decoded from CRL.", e);
                }
            }
            if (freshestCRL != null)
            {
                try
                {
                    CertPathValidatorUtilities.addAdditionalStoresFromCRLDistributionPoint(freshestCRL, paramsPKIX);
                }
                catch (AnnotatedException e)
                {
                    throw new AnnotatedException(
                        "No new delta CRL locations could be added from Freshest CRL extension.", e);
                }
                // get delta CRL(s)
                try
                {
                    set.addAll(CertPathValidatorUtilities.getDeltaCRLs(currentDate, paramsPKIX, crl));
                }
                catch (AnnotatedException e)
                {
                    throw new AnnotatedException("Exception obtaining delta CRLs.", e);
                }
            }
        }
        return set;
    }

    protected static Set[] processCRLA1ii(
        Date currentDate,
        ExtendedPKIXParameters paramsPKIX,
        X509Certificate cert,
        X509CRL crl)
        throws AnnotatedException
    {
        Set deltaSet = new HashSet();
        X509CRLStoreSelector crlselect = new X509CRLStoreSelector();
        crlselect.setCertificateChecking(cert);

        try
        {
            crlselect.addIssuerName(crl.getIssuerX500Principal().getEncoded());
        }
        catch (IOException e)
        {
            throw new AnnotatedException("Cannot extract issuer from CRL." + e, e);
        }

        crlselect.setCompleteCRLEnabled(true);
        Set completeSet = CRL_UTIL.findCRLs(crlselect, paramsPKIX, currentDate);

        if (paramsPKIX.isUseDeltasEnabled())
        {
            // get delta CRL(s)
            try
            {
                deltaSet.addAll(CertPathValidatorUtilities.getDeltaCRLs(currentDate, paramsPKIX, crl));
            }
            catch (AnnotatedException e)
            {
                throw new AnnotatedException("Exception obtaining delta CRLs.", e);
            }
        }
        return new Set[]
            {
                completeSet,
                deltaSet};
    }



    /**
     * If use-deltas is set, verify the issuer and scope of the delta CRL.
     *
     * @param deltaCRL    The delta CRL.
     * @param completeCRL The complete CRL.
     * @param pkixParams  The PKIX paramaters.
     * @throws AnnotatedException if an exception occurs.
     */
    protected static void processCRLC(
        X509CRL deltaCRL,
        X509CRL completeCRL,
        ExtendedPKIXParameters pkixParams)
        throws AnnotatedException
    {
        if (deltaCRL == null)
        {
            return;
        }
        IssuingDistributionPoint completeidp = null;
        try
        {
            completeidp = IssuingDistributionPoint.getInstance(CertPathValidatorUtilities.getExtensionValue(
                completeCRL, RFC3280CertPathUtilities.ISSUING_DISTRIBUTION_POINT));
        }
        catch (Exception e)
        {
            throw new AnnotatedException("Issuing distribution point extension could not be decoded.", e);
        }

        if (pkixParams.isUseDeltasEnabled())
        {
            // (c) (1)
            if (!deltaCRL.getIssuerX500Principal().equals(completeCRL.getIssuerX500Principal()))
            {
                throw new AnnotatedException("Complete CRL issuer does not match delta CRL issuer.");
            }

            // (c) (2)
            IssuingDistributionPoint deltaidp = null;
            try
            {
                deltaidp = IssuingDistributionPoint.getInstance(CertPathValidatorUtilities.getExtensionValue(
                    deltaCRL, ISSUING_DISTRIBUTION_POINT));
            }
            catch (Exception e)
            {
                throw new AnnotatedException(
                    "Issuing distribution point extension from delta CRL could not be decoded.", e);
            }

            boolean match = false;
            if (completeidp == null)
            {
                if (deltaidp == null)
                {
                    match = true;
                }
            }
            else
            {
                if (completeidp.equals(deltaidp))
                {
                    match = true;
                }
            }
            if (!match)
            {
                throw new AnnotatedException(
                    "Issuing distribution point extension from delta CRL and complete CRL does not match.");
            }

            // (c) (3)
            DERObject completeKeyIdentifier = null;
            try
            {
                completeKeyIdentifier = CertPathValidatorUtilities.getExtensionValue(
                    completeCRL, AUTHORITY_KEY_IDENTIFIER);
            }
            catch (AnnotatedException e)
            {
                throw new AnnotatedException(
                    "Authority key identifier extension could not be extracted from complete CRL.", e);
            }

            DERObject deltaKeyIdentifier = null;
            try
            {
                deltaKeyIdentifier = CertPathValidatorUtilities.getExtensionValue(
                    deltaCRL, AUTHORITY_KEY_IDENTIFIER);
            }
            catch (AnnotatedException e)
            {
                throw new AnnotatedException(
                    "Authority key identifier extension could not be extracted from delta CRL.", e);
            }

            if (completeKeyIdentifier == null)
            {
                throw new AnnotatedException("CRL authority key identifier is null.");
            }

            if (deltaKeyIdentifier == null)
            {
                throw new AnnotatedException("Delta CRL authority key identifier is null.");
            }

            if (!completeKeyIdentifier.equals(deltaKeyIdentifier))
            {
                throw new AnnotatedException(
                    "Delta CRL authority key identifier does not match complete CRL authority key identifier.");
            }
        }
    }

    protected static void processCRLI(
        Date validDate,
        X509CRL deltacrl,
        Object cert,
        CertStatus certStatus,
        ExtendedPKIXParameters pkixParams)
        throws AnnotatedException
    {
        if (pkixParams.isUseDeltasEnabled() && deltacrl != null)
        {
            CertPathValidatorUtilities.getCertStatus(validDate, deltacrl, cert, certStatus);
        }
    }

    protected static void processCRLJ(
        Date validDate,
        X509CRL completecrl,
        Object cert,
        CertStatus certStatus)
        throws AnnotatedException
    {
        if (certStatus.getCertStatus() == CertStatus.UNREVOKED)
        {
            CertPathValidatorUtilities.getCertStatus(validDate, completecrl, cert, certStatus);
        }
    }

    protected static PKIXPolicyNode prepareCertB(
        CertPath certPath,
        int index,
        List[] policyNodes,
        PKIXPolicyNode validPolicyTree,
        int policyMapping)
        throws CertPathValidatorException
    {
        List certs = certPath.getCertificates();
        X509Certificate cert = (X509Certificate)certs.get(index);
        int n = certs.size();
        // i as defined in the algorithm description
        int i = n - index;
        // (b)
        //
        ASN1Sequence pm = null;
        try
        {
            pm = DERSequence.getInstance(CertPathValidatorUtilities.getExtensionValue(cert,
                RFC3280CertPathUtilities.POLICY_MAPPINGS));
        }
        catch (AnnotatedException ex)
        {
            throw new ExtCertPathValidatorException("Policy mappings extension could not be decoded.", ex, certPath,
                index);
        }
        PKIXPolicyNode _validPolicyTree = validPolicyTree;
        if (pm != null)
        {
            ASN1Sequence mappings = (ASN1Sequence)pm;
            Map m_idp = new HashMap();
            Set s_idp = new HashSet();

            for (int j = 0; j < mappings.size(); j++)
            {
                ASN1Sequence mapping = (ASN1Sequence)mappings.getObjectAt(j);
                String id_p = ((DERObjectIdentifier)mapping.getObjectAt(0)).getId();
                String sd_p = ((DERObjectIdentifier)mapping.getObjectAt(1)).getId();
                Set tmp;

                if (!m_idp.containsKey(id_p))
                {
                    tmp = new HashSet();
                    tmp.add(sd_p);
                    m_idp.put(id_p, tmp);
                    s_idp.add(id_p);
                }
                else
                {
                    tmp = (Set)m_idp.get(id_p);
                    tmp.add(sd_p);
                }
            }

            Iterator it_idp = s_idp.iterator();
            while (it_idp.hasNext())
            {
                String id_p = (String)it_idp.next();

                //
                // (1)
                //
                if (policyMapping > 0)
                {
                    boolean idp_found = false;
                    Iterator nodes_i = policyNodes[i].iterator();
                    while (nodes_i.hasNext())
                    {
                        PKIXPolicyNode node = (PKIXPolicyNode)nodes_i.next();
                        if (node.getValidPolicy().equals(id_p))
                        {
                            idp_found = true;
                            node.expectedPolicies = (Set)m_idp.get(id_p);
                            break;
                        }
                    }

                    if (!idp_found)
                    {
                        nodes_i = policyNodes[i].iterator();
                        while (nodes_i.hasNext())
                        {
                            PKIXPolicyNode node = (PKIXPolicyNode)nodes_i.next();
                            if (RFC3280CertPathUtilities.ANY_POLICY.equals(node.getValidPolicy()))
                            {
                                Set pq = null;
                                ASN1Sequence policies = null;
                                try
                                {
                                    policies = (ASN1Sequence)CertPathValidatorUtilities.getExtensionValue(cert,
                                        RFC3280CertPathUtilities.CERTIFICATE_POLICIES);
                                }
                                catch (AnnotatedException e)
                                {
                                    throw new ExtCertPathValidatorException(
                                        "Certificate policies extension could not be decoded.", e, certPath, index);
                                }
                                Enumeration e = policies.getObjects();
                                while (e.hasMoreElements())
                                {
                                    PolicyInformation pinfo = null;
                                    try
                                    {
                                        pinfo = PolicyInformation.getInstance(e.nextElement());
                                    }
                                    catch (Exception ex)
                                    {
                                        throw new CertPathValidatorException(
                                            "Policy information could not be decoded.", ex, certPath, index);
                                    }
                                    if (RFC3280CertPathUtilities.ANY_POLICY.equals(pinfo.getPolicyIdentifier().getId()))
                                    {
                                        try
                                        {
                                            pq = CertPathValidatorUtilities
                                                .getQualifierSet(pinfo.getPolicyQualifiers());
                                        }
                                        catch (CertPathValidatorException ex)
                                        {

                                            throw new ExtCertPathValidatorException(
                                                "Policy qualifier info set could not be decoded.", ex, certPath,
                                                index);
                                        }
                                        break;
                                    }
                                }
                                boolean ci = false;
                                if (cert.getCriticalExtensionOIDs() != null)
                                {
                                    ci = cert.getCriticalExtensionOIDs().contains(
                                        RFC3280CertPathUtilities.CERTIFICATE_POLICIES);
                                }

                                PKIXPolicyNode p_node = (PKIXPolicyNode)node.getParent();
                                if (RFC3280CertPathUtilities.ANY_POLICY.equals(p_node.getValidPolicy()))
                                {
                                    PKIXPolicyNode c_node = new PKIXPolicyNode(new ArrayList(), i, (Set)m_idp
                                        .get(id_p), p_node, pq, id_p, ci);
                                    p_node.addChild(c_node);
                                    policyNodes[i].add(c_node);
                                }
                                break;
                            }
                        }
                    }

                    //
                    // (2)
                    //
                }
                else if (policyMapping <= 0)
                {
                    Iterator nodes_i = policyNodes[i].iterator();
                    while (nodes_i.hasNext())
                    {
                        PKIXPolicyNode node = (PKIXPolicyNode)nodes_i.next();
                        if (node.getValidPolicy().equals(id_p))
                        {
                            PKIXPolicyNode p_node = (PKIXPolicyNode)node.getParent();
                            p_node.removeChild(node);
                            nodes_i.remove();
                            for (int k = (i - 1); k >= 0; k--)
                            {
                                List nodes = policyNodes[k];
                                for (int l = 0; l < nodes.size(); l++)
                                {
                                    PKIXPolicyNode node2 = (PKIXPolicyNode)nodes.get(l);
                                    if (!node2.hasChildren())
                                    {
                                        _validPolicyTree = CertPathValidatorUtilities.removePolicyNode(
                                            _validPolicyTree, policyNodes, node2);
                                        if (_validPolicyTree == null)
                                        {
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return _validPolicyTree;
    }

    protected static void prepareNextCertA(
        CertPath certPath,
        int index)
        throws CertPathValidatorException
    {
        List certs = certPath.getCertificates();
        X509Certificate cert = (X509Certificate)certs.get(index);
        //
        //
        // (a) check the policy mappings
        //
        ASN1Sequence pm = null;
        try
        {
            pm = DERSequence.getInstance(CertPathValidatorUtilities.getExtensionValue(cert,
                RFC3280CertPathUtilities.POLICY_MAPPINGS));
        }
        catch (AnnotatedException ex)
        {
            throw new ExtCertPathValidatorException("Policy mappings extension could not be decoded.", ex, certPath,
                index);
        }
        if (pm != null)
        {
            ASN1Sequence mappings = pm;

            for (int j = 0; j < mappings.size(); j++)
            {
                DERObjectIdentifier issuerDomainPolicy = null;
                DERObjectIdentifier subjectDomainPolicy = null;
                try
                {
                    ASN1Sequence mapping = DERSequence.getInstance(mappings.getObjectAt(j));

                    issuerDomainPolicy = DERObjectIdentifier.getInstance(mapping.getObjectAt(0));
                    subjectDomainPolicy = DERObjectIdentifier.getInstance(mapping.getObjectAt(1));
                }
                catch (Exception e)
                {
                    throw new ExtCertPathValidatorException("Policy mappings extension contents could not be decoded.",
                        e, certPath, index);
                }

                if (RFC3280CertPathUtilities.ANY_POLICY.equals(issuerDomainPolicy.getId()))
                {

                    throw new CertPathValidatorException("IssuerDomainPolicy is anyPolicy", null, certPath, index);
                }

                if (RFC3280CertPathUtilities.ANY_POLICY.equals(subjectDomainPolicy.getId()))
                {

                    throw new CertPathValidatorException("SubjectDomainPolicy is anyPolicy,", null, certPath, index);
                }
            }
        }
    }

    protected static void processCertF(
        CertPath certPath,
        int index,
        PKIXPolicyNode validPolicyTree,
        int explicitPolicy)
        throws CertPathValidatorException
    {
        //
        // (f)
        //
        if (explicitPolicy <= 0 && validPolicyTree == null)
        {
            throw new ExtCertPathValidatorException("No valid policy tree found when one expected.", null, certPath,
                index);
        }
    }

    protected static PKIXPolicyNode processCertE(
        CertPath certPath,
        int index,
        PKIXPolicyNode validPolicyTree)
        throws CertPathValidatorException
    {
        List certs = certPath.getCertificates();
        X509Certificate cert = (X509Certificate)certs.get(index);
        // 
        // (e)
        //
        ASN1Sequence certPolicies = null;
        try
        {
            certPolicies = DERSequence.getInstance(CertPathValidatorUtilities.getExtensionValue(cert,
                RFC3280CertPathUtilities.CERTIFICATE_POLICIES));
        }
        catch (AnnotatedException e)
        {
            throw new ExtCertPathValidatorException("Could not read certificate policies extension from certificate.",
                e, certPath, index);
        }
        if (certPolicies == null)
        {
            validPolicyTree = null;
        }
        return validPolicyTree;
    }

    protected static void processCertBC(
        CertPath certPath,
        int index,
        PKIXNameConstraintValidator nameConstraintValidator)
        throws CertPathValidatorException
    {
        List certs = certPath.getCertificates();
        X509Certificate cert = (X509Certificate)certs.get(index);
        int n = certs.size();
        // i as defined in the algorithm description
        int i = n - index;
        //
        // (b), (c) permitted and excluded subtree checking.
        //
        if (!(CertPathValidatorUtilities.isSelfIssued(cert) && (i < n)))
        {
            X500Principal principal = CertPathValidatorUtilities.getSubjectPrincipal(cert);
            ASN1InputStream aIn = new ASN1InputStream(principal.getEncoded());
            ASN1Sequence dns;

            try
            {
                dns = DERSequence.getInstance(aIn.readObject());
            }
            catch (Exception e)
            {
                throw new CertPathValidatorException("Exception extracting subject name when checking subtrees.", e,
                    certPath, index);
            }

            try
            {
                nameConstraintValidator.checkPermittedDN(dns);
                nameConstraintValidator.checkExcludedDN(dns);
            }
            catch (PKIXNameConstraintValidatorException e)
            {
                throw new CertPathValidatorException("Subtree check for certificate subject failed.", e, certPath,
                    index);
            }

            GeneralNames altName = null;
            try
            {
                altName = GeneralNames.getInstance(CertPathValidatorUtilities.getExtensionValue(cert,
                    RFC3280CertPathUtilities.SUBJECT_ALTERNATIVE_NAME));
            }
            catch (Exception e)
            {
                throw new CertPathValidatorException("Subject alternative name extension could not be decoded.", e,
                    certPath, index);
            }
            Vector emails = new X509Name(dns).getValues(X509Name.EmailAddress);
            for (Enumeration e = emails.elements(); e.hasMoreElements();)
            {
                String email = (String)e.nextElement();
                GeneralName emailAsGeneralName = new GeneralName(GeneralName.rfc822Name, email);
                try
                {
                    nameConstraintValidator.checkPermitted(emailAsGeneralName);
                    nameConstraintValidator.checkExcluded(emailAsGeneralName);
                }
                catch (PKIXNameConstraintValidatorException ex)
                {
                    throw new CertPathValidatorException(
                        "Subtree check for certificate subject alternative email failed.", ex, certPath, index);
                }
            }
            if (altName != null)
            {
                GeneralName[] genNames = null;
                try
                {
                    genNames = altName.getNames();
                }
                catch (Exception e)
                {
                    throw new CertPathValidatorException("Subject alternative name contents could not be decoded.", e,
                        certPath, index);
                }
                for (int j = 0; j < genNames.length; j++)
                {

                    try
                    {
                        nameConstraintValidator.checkPermitted(genNames[j]);
                        nameConstraintValidator.checkExcluded(genNames[j]);
                    }
                    catch (PKIXNameConstraintValidatorException e)
                    {
                        throw new CertPathValidatorException(
                            "Subtree check for certificate subject alternative name failed.", e, certPath, index);
                    }
                }
            }
        }
    }

    protected static PKIXPolicyNode processCertD(
        CertPath certPath,
        int index,
        Set acceptablePolicies,
        PKIXPolicyNode validPolicyTree,
        List[] policyNodes,
        int inhibitAnyPolicy)
        throws CertPathValidatorException
    {
        List certs = certPath.getCertificates();
        X509Certificate cert = (X509Certificate)certs.get(index);
        int n = certs.size();
        // i as defined in the algorithm description
        int i = n - index;
        //
        // (d) policy Information checking against initial policy and
        // policy mapping
        //
        ASN1Sequence certPolicies = null;
        try
        {
            certPolicies = DERSequence.getInstance(CertPathValidatorUtilities.getExtensionValue(cert,
                RFC3280CertPathUtilities.CERTIFICATE_POLICIES));
        }
        catch (AnnotatedException e)
        {
            throw new ExtCertPathValidatorException("Could not read certificate policies extension from certificate.",
                e, certPath, index);
        }
        if (certPolicies != null && validPolicyTree != null)
        {
            //
            // (d) (1)
            //
            Enumeration e = certPolicies.getObjects();
            Set pols = new HashSet();

            while (e.hasMoreElements())
            {
                PolicyInformation pInfo = PolicyInformation.getInstance(e.nextElement());
                DERObjectIdentifier pOid = pInfo.getPolicyIdentifier();

                pols.add(pOid.getId());

                if (!RFC3280CertPathUtilities.ANY_POLICY.equals(pOid.getId()))
                {
                    Set pq = null;
                    try
                    {
                        pq = CertPathValidatorUtilities.getQualifierSet(pInfo.getPolicyQualifiers());
                    }
                    catch (CertPathValidatorException ex)
                    {
                        throw new ExtCertPathValidatorException("Policy qualifier info set could not be build.", ex,
                            certPath, index);
                    }

                    boolean match = CertPathValidatorUtilities.processCertD1i(i, policyNodes, pOid, pq);

                    if (!match)
                    {
                        CertPathValidatorUtilities.processCertD1ii(i, policyNodes, pOid, pq);
                    }
                }
            }

            if (acceptablePolicies.isEmpty() || acceptablePolicies.contains(RFC3280CertPathUtilities.ANY_POLICY))
            {
                acceptablePolicies.clear();
                acceptablePolicies.addAll(pols);
            }
            else
            {
                Iterator it = acceptablePolicies.iterator();
                Set t1 = new HashSet();

                while (it.hasNext())
                {
                    Object o = it.next();

                    if (pols.contains(o))
                    {
                        t1.add(o);
                    }
                }
                acceptablePolicies.clear();
                acceptablePolicies.addAll(t1);
            }

            //
            // (d) (2)
            //
            if ((inhibitAnyPolicy > 0) || ((i < n) && CertPathValidatorUtilities.isSelfIssued(cert)))
            {
                e = certPolicies.getObjects();

                while (e.hasMoreElements())
                {
                    PolicyInformation pInfo = PolicyInformation.getInstance(e.nextElement());

                    if (RFC3280CertPathUtilities.ANY_POLICY.equals(pInfo.getPolicyIdentifier().getId()))
                    {
                        Set _apq = CertPathValidatorUtilities.getQualifierSet(pInfo.getPolicyQualifiers());
                        List _nodes = policyNodes[i - 1];

                        for (int k = 0; k < _nodes.size(); k++)
                        {
                            PKIXPolicyNode _node = (PKIXPolicyNode)_nodes.get(k);

                            Iterator _policySetIter = _node.getExpectedPolicies().iterator();
                            while (_policySetIter.hasNext())
                            {
                                Object _tmp = _policySetIter.next();

                                String _policy;
                                if (_tmp instanceof String)
                                {
                                    _policy = (String)_tmp;
                                }
                                else if (_tmp instanceof DERObjectIdentifier)
                                {
                                    _policy = ((DERObjectIdentifier)_tmp).getId();
                                }
                                else
                                {
                                    continue;
                                }

                                boolean _found = false;
                                Iterator _childrenIter = _node.getChildren();

                                while (_childrenIter.hasNext())
                                {
                                    PKIXPolicyNode _child = (PKIXPolicyNode)_childrenIter.next();

                                    if (_policy.equals(_child.getValidPolicy()))
                                    {
                                        _found = true;
                                    }
                                }

                                if (!_found)
                                {
                                    Set _newChildExpectedPolicies = new HashSet();
                                    _newChildExpectedPolicies.add(_policy);

                                    PKIXPolicyNode _newChild = new PKIXPolicyNode(new ArrayList(), i,
                                        _newChildExpectedPolicies, _node, _apq, _policy, false);
                                    _node.addChild(_newChild);
                                    policyNodes[i].add(_newChild);
                                }
                            }
                        }
                        break;
                    }
                }
            }

            PKIXPolicyNode _validPolicyTree = validPolicyTree;
            //
            // (d) (3)
            //
            for (int j = (i - 1); j >= 0; j--)
            {
                List nodes = policyNodes[j];

                for (int k = 0; k < nodes.size(); k++)
                {
                    PKIXPolicyNode node = (PKIXPolicyNode)nodes.get(k);
                    if (!node.hasChildren())
                    {
                        _validPolicyTree = CertPathValidatorUtilities.removePolicyNode(_validPolicyTree, policyNodes,
                            node);
                        if (_validPolicyTree == null)
                        {
                            break;
                        }
                    }
                }
            }

            //
            // d (4)
            //
            Set criticalExtensionOids = cert.getCriticalExtensionOIDs();

            if (criticalExtensionOids != null)
            {
                boolean critical = criticalExtensionOids.contains(RFC3280CertPathUtilities.CERTIFICATE_POLICIES);

                List nodes = policyNodes[i];
                for (int j = 0; j < nodes.size(); j++)
                {
                    PKIXPolicyNode node = (PKIXPolicyNode)nodes.get(j);
                    node.setCritical(critical);
                }
            }
            return _validPolicyTree;
        }
        return null;
    }

    protected static void processCertA(
        CertPath certPath,
        ExtendedPKIXParameters paramsPKIX,
        int index,
        PublicKey workingPublicKey,
        boolean verificationAlreadyPerformed,
        X500Principal workingIssuerName,
        X509Certificate sign)
        throws ExtCertPathValidatorException
    {
        List certs = certPath.getCertificates();
        X509Certificate cert = (X509Certificate)certs.get(index);
        //
        // (a) verify
        //
        if (!verificationAlreadyPerformed)
        {
            try
            {
                // (a) (1)
                //
                CertPathValidatorUtilities.verifyX509Certificate(cert, workingPublicKey,
                    paramsPKIX.getSigProvider());
            }
            catch (GeneralSecurityException e)
            {
                throw new ExtCertPathValidatorException("Could not validate certificate signature.", e, certPath, index);
            }
        }

        try
        {
            // (a) (2)
            //
            cert.checkValidity(CertPathValidatorUtilities
                .getValidCertDateFromValidityModel(paramsPKIX, certPath, index));
        }
        catch (CertificateExpiredException e)
        {
            throw new ExtCertPathValidatorException("Could not validate certificate: " + e.getMessage(), e, certPath, index);
        }
        catch (CertificateNotYetValidException e)
        {
            throw new ExtCertPathValidatorException("Could not validate certificate: " + e.getMessage(), e, certPath, index);
        }
        catch (AnnotatedException e)
        {
            throw new ExtCertPathValidatorException("Could not validate time of certificate.", e, certPath, index);
        }

        //
        // (a) (3)
        //
        if (paramsPKIX.isRevocationEnabled())
        {
            try
            {
                checkCRLs(paramsPKIX, cert, CertPathValidatorUtilities.getValidCertDateFromValidityModel(paramsPKIX,
                    certPath, index), sign, workingPublicKey, certs);
            }
            catch (AnnotatedException e)
            {
                Throwable cause = e;
                if (null != e.getCause())
                {
                    cause = e.getCause();
                }
                throw new ExtCertPathValidatorException(e.getMessage(), cause, certPath, index);
            }
        }

        //
        // (a) (4) name chaining
        //
        if (!CertPathValidatorUtilities.getEncodedIssuerPrincipal(cert).equals(workingIssuerName))
        {
            throw new ExtCertPathValidatorException("IssuerName(" + CertPathValidatorUtilities.getEncodedIssuerPrincipal(cert)
                + ") does not match SubjectName(" + workingIssuerName + ") of signing certificate.", null,
                certPath, index);
        }
    }

    protected static int prepareNextCertI1(
        CertPath certPath,
        int index,
        int explicitPolicy)
        throws CertPathValidatorException
    {
        List certs = certPath.getCertificates();
        X509Certificate cert = (X509Certificate)certs.get(index);
        //
        // (i)
        //
        ASN1Sequence pc = null;
        try
        {
            pc = DERSequence.getInstance(CertPathValidatorUtilities.getExtensionValue(cert,
                RFC3280CertPathUtilities.POLICY_CONSTRAINTS));
        }
        catch (Exception e)
        {
            throw new ExtCertPathValidatorException("Policy constraints extension cannot be decoded.", e, certPath,
                index);
        }

        int tmpInt;

        if (pc != null)
        {
            Enumeration policyConstraints = pc.getObjects();

            while (policyConstraints.hasMoreElements())
            {
                try
                {

                    ASN1TaggedObject constraint = ASN1TaggedObject.getInstance(policyConstraints.nextElement());
                    if (constraint.getTagNo() == 0)
                    {
                        tmpInt = DERInteger.getInstance(constraint, false).getValue().intValue();
                        if (tmpInt < explicitPolicy)
                        {
                            return tmpInt;
                        }
                        break;
                    }
                }
                catch (IllegalArgumentException e)
                {
                    throw new ExtCertPathValidatorException("Policy constraints extension contents cannot be decoded.",
                        e, certPath, index);
                }
            }
        }
        return explicitPolicy;
    }

    protected static int prepareNextCertI2(
        CertPath certPath,
        int index,
        int policyMapping)
        throws CertPathValidatorException
    {
        List certs = certPath.getCertificates();
        X509Certificate cert = (X509Certificate)certs.get(index);
        //
        // (i)
        //
        ASN1Sequence pc = null;
        try
        {
            pc = DERSequence.getInstance(CertPathValidatorUtilities.getExtensionValue(cert,
                RFC3280CertPathUtilities.POLICY_CONSTRAINTS));
        }
        catch (Exception e)
        {
            throw new ExtCertPathValidatorException("Policy constraints extension cannot be decoded.", e, certPath,
                index);
        }

        int tmpInt;

        if (pc != null)
        {
            Enumeration policyConstraints = pc.getObjects();

            while (policyConstraints.hasMoreElements())
            {
                try
                {
                    ASN1TaggedObject constraint = ASN1TaggedObject.getInstance(policyConstraints.nextElement());
                    if (constraint.getTagNo() == 1)
                    {
                        tmpInt = DERInteger.getInstance(constraint, false).getValue().intValue();
                        if (tmpInt < policyMapping)
                        {
                            return tmpInt;
                        }
                        break;
                    }
                }
                catch (IllegalArgumentException e)
                {
                    throw new ExtCertPathValidatorException("Policy constraints extension contents cannot be decoded.",
                        e, certPath, index);
                }
            }
        }
        return policyMapping;
    }

    protected static void prepareNextCertG(
        CertPath certPath,
        int index,
        PKIXNameConstraintValidator nameConstraintValidator)
        throws CertPathValidatorException
    {
        List certs = certPath.getCertificates();
        X509Certificate cert = (X509Certificate)certs.get(index);
        //
        // (g) handle the name constraints extension
        //
        NameConstraints nc = null;
        try
        {
            ASN1Sequence ncSeq = DERSequence.getInstance(CertPathValidatorUtilities.getExtensionValue(cert,
                RFC3280CertPathUtilities.NAME_CONSTRAINTS));
            if (ncSeq != null)
            {
                nc = new NameConstraints(ncSeq);
            }
        }
        catch (Exception e)
        {
            throw new ExtCertPathValidatorException("Name constraints extension could not be decoded.", e, certPath,
                index);
        }
        if (nc != null)
        {

            //
            // (g) (1) permitted subtrees
            //
            ASN1Sequence permitted = nc.getPermittedSubtrees();
            if (permitted != null)
            {
                try
                {
                    nameConstraintValidator.intersectPermittedSubtree(permitted);
                }
                catch (Exception ex)
                {
                    throw new ExtCertPathValidatorException(
                        "Permitted subtrees cannot be build from name constraints extension.", ex, certPath, index);
                }
            }

            //
            // (g) (2) excluded subtrees
            //
            ASN1Sequence excluded = nc.getExcludedSubtrees();
            if (excluded != null)
            {
                Enumeration e = excluded.getObjects();
                try
                {
                    while (e.hasMoreElements())
                    {
                        GeneralSubtree subtree = GeneralSubtree.getInstance(e.nextElement());
                        nameConstraintValidator.addExcludedSubtree(subtree);
                    }
                }
                catch (Exception ex)
                {
                    throw new ExtCertPathValidatorException(
                        "Excluded subtrees cannot be build from name constraints extension.", ex, certPath, index);
                }
            }
        }
    }

    /**
     * Checks a distribution point for revocation information for the
     * certificate <code>cert</code>.
     *
     * @param dp                 The distribution point to consider.
     * @param paramsPKIX         PKIX parameters.
     * @param cert               Certificate to check if it is revoked.
     * @param validDate          The date when the certificate revocation status should be
     *                           checked.
     * @param defaultCRLSignCert The issuer certificate of the certificate <code>cert</code>.
     * @param defaultCRLSignKey  The public key of the issuer certificate
     *                           <code>defaultCRLSignCert</code>.
     * @param certStatus         The current certificate revocation status.
     * @param reasonMask         The reasons mask which is already checked.
     * @param certPathCerts      The certificates of the certification path.
     * @throws AnnotatedException if the certificate is revoked or the status cannot be checked
     *                            or some error occurs.
     */
    private static void checkCRL(
        DistributionPoint dp,
        ExtendedPKIXParameters paramsPKIX,
        X509Certificate cert,
        Date validDate,
        X509Certificate defaultCRLSignCert,
        PublicKey defaultCRLSignKey,
        CertStatus certStatus,
        ReasonsMask reasonMask,
        List certPathCerts)
        throws AnnotatedException
    {
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

        Set crls = CertPathValidatorUtilities.getCompleteCRLs(dp, cert, currentDate, paramsPKIX);
        boolean validCrlFound = false;
        AnnotatedException lastException = null;
        Iterator crl_iter = crls.iterator();

        while (crl_iter.hasNext() && certStatus.getCertStatus() == CertStatus.UNREVOKED && !reasonMask.isAllReasons())
        {
            try
            {
                X509CRL crl = (X509CRL)crl_iter.next();

                // (d)
                ReasonsMask interimReasonsMask = RFC3280CertPathUtilities.processCRLD(crl, dp);

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
                Set keys = RFC3280CertPathUtilities.processCRLF(crl, cert, defaultCRLSignCert, defaultCRLSignKey,
                    paramsPKIX, certPathCerts);
                // (g)
                PublicKey key = RFC3280CertPathUtilities.processCRLG(crl, keys);

                X509CRL deltaCRL = null;

                if (paramsPKIX.isUseDeltasEnabled())
                {
                    // get delta CRLs
                    Set deltaCRLs = CertPathValidatorUtilities.getDeltaCRLs(currentDate, paramsPKIX, crl);
                    // we only want one valid delta CRL
                    // (h)
                    deltaCRL = RFC3280CertPathUtilities.processCRLH(deltaCRLs, key);
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
                 * the CRL validity time
                 */

                if (paramsPKIX.getValidityModel() != ExtendedPKIXParameters.CHAIN_VALIDITY_MODEL)
                {
                    /*
                     * if a certificate has expired, but was revoked, it is not
                     * more in the CRL, so it would be regarded as valid if the
                     * first check is not done
                     */
                    if (cert.getNotAfter().getTime() < crl.getThisUpdate().getTime())
                    {
                        throw new AnnotatedException("No valid CRL for current time found.");
                    }
                }

                RFC3280CertPathUtilities.processCRLB1(dp, cert, crl);

                // (b) (2)
                RFC3280CertPathUtilities.processCRLB2(dp, cert, crl);

                // (c)
                RFC3280CertPathUtilities.processCRLC(deltaCRL, crl, paramsPKIX);

                // (i)
                RFC3280CertPathUtilities.processCRLI(validDate, deltaCRL, cert, certStatus, paramsPKIX);

                // (j)
                RFC3280CertPathUtilities.processCRLJ(validDate, crl, cert, certStatus);

                // (k)
                if (certStatus.getCertStatus() == CRLReason.removeFromCRL)
                {
                    certStatus.setCertStatus(CertStatus.UNREVOKED);
                }

                // update reasons mask
                reasonMask.addReasons(interimReasonsMask);

                Set criticalExtensions = crl.getCriticalExtensionOIDs();
                if (criticalExtensions != null)
                {
                    criticalExtensions = new HashSet(criticalExtensions);
                    criticalExtensions.remove(X509Extensions.IssuingDistributionPoint.getId());
                    criticalExtensions.remove(X509Extensions.DeltaCRLIndicator.getId());

                    if (!criticalExtensions.isEmpty())
                    {
                        throw new AnnotatedException("CRL contains unsupported critical extensions.");
                    }
                }

                if (deltaCRL != null)
                {
                    criticalExtensions = deltaCRL.getCriticalExtensionOIDs();
                    if (criticalExtensions != null)
                    {
                        criticalExtensions = new HashSet(criticalExtensions);
                        criticalExtensions.remove(X509Extensions.IssuingDistributionPoint.getId());
                        criticalExtensions.remove(X509Extensions.DeltaCRLIndicator.getId());
                        if (!criticalExtensions.isEmpty())
                        {
                            throw new AnnotatedException("Delta CRL contains unsupported critical extension.");
                        }
                    }
                }

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

    /**
     * Checks a certificate if it is revoked.
     *
     * @param paramsPKIX       PKIX parameters.
     * @param cert             Certificate to check if it is revoked.
     * @param validDate        The date when the certificate revocation status should be
     *                         checked.
     * @param sign             The issuer certificate of the certificate <code>cert</code>.
     * @param workingPublicKey The public key of the issuer certificate <code>sign</code>.
     * @param certPathCerts    The certificates of the certification path.
     * @throws AnnotatedException if the certificate is revoked or the status cannot be checked
     *                            or some error occurs.
     */
    protected static void checkCRLs(
        ExtendedPKIXParameters paramsPKIX,
        X509Certificate cert,
        Date validDate,
        X509Certificate sign,
        PublicKey workingPublicKey,
        List certPathCerts)
        throws AnnotatedException
    {
        AnnotatedException lastException = null;
        CRLDistPoint crldp = null;
        try
        {
            crldp = CRLDistPoint.getInstance(CertPathValidatorUtilities.getExtensionValue(cert,
                RFC3280CertPathUtilities.CRL_DISTRIBUTION_POINTS));
        }
        catch (Exception e)
        {
            throw new AnnotatedException("CRL distribution point extension could not be read.", e);
        }
        try
        {
            CertPathValidatorUtilities.addAdditionalStoresFromCRLDistributionPoint(crldp, paramsPKIX);
        }
        catch (AnnotatedException e)
        {
            throw new AnnotatedException(
                "No additional CRL locations could be decoded from CRL distribution point extension.", e);
        }
        CertStatus certStatus = new CertStatus();
        ReasonsMask reasonsMask = new ReasonsMask();

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
                throw new AnnotatedException("Distribution points could not be read.", e);
            }
            if (dps != null)
            {
                for (int i = 0; i < dps.length && certStatus.getCertStatus() == CertStatus.UNREVOKED && !reasonsMask.isAllReasons(); i++)
                {
                    ExtendedPKIXParameters paramsPKIXClone = (ExtendedPKIXParameters)paramsPKIX.clone();
                    try
                    {
                        checkCRL(dps[i], paramsPKIXClone, cert, validDate, sign, workingPublicKey, certStatus, reasonsMask, certPathCerts);
                        validCrlFound = true;
                    }
                    catch (AnnotatedException e)
                    {
                        lastException = e;
                    }
                }
            }
        }

        /*
         * If the revocation status has not been determined, repeat the process
         * above with any available CRLs not specified in a distribution point
         * but issued by the certificate issuer.
         */

        if (certStatus.getCertStatus() == CertStatus.UNREVOKED && !reasonsMask.isAllReasons())
        {
            try
            {
                /*
                 * assume a DP with both the reasons and the cRLIssuer fields
                 * omitted and a distribution point name of the certificate
                 * issuer.
                 */
                DERObject issuer = null;
                try
                {
                    issuer = new ASN1InputStream(CertPathValidatorUtilities.getEncodedIssuerPrincipal(cert).getEncoded())
                        .readObject();
                }
                catch (Exception e)
                {
                    throw new AnnotatedException("Issuer from certificate for CRL could not be reencoded.", e);
                }
                DistributionPoint dp = new DistributionPoint(new DistributionPointName(0, new GeneralNames(
                    new GeneralName(GeneralName.directoryName, issuer))), null, null);
                ExtendedPKIXParameters paramsPKIXClone = (ExtendedPKIXParameters)paramsPKIX.clone();
                checkCRL(dp, paramsPKIXClone, cert, validDate, sign, workingPublicKey, certStatus, reasonsMask,
                    certPathCerts);
                validCrlFound = true;
            }
            catch (AnnotatedException e)
            {
                lastException = e;
            }
        }

        if (!validCrlFound)
        {
            if (lastException instanceof AnnotatedException)
            {
                throw lastException;
            }

            throw new AnnotatedException("No valid CRL found.", lastException);
        }
        if (certStatus.getCertStatus() != CertStatus.UNREVOKED)
        {
            String message = "Certificate revocation after " + certStatus.getRevocationDate();
            message += ", reason: " + crlReasons[certStatus.getCertStatus()];
            throw new AnnotatedException(message);
        }
        if (!reasonsMask.isAllReasons() && certStatus.getCertStatus() == CertStatus.UNREVOKED)
        {
            certStatus.setCertStatus(CertStatus.UNDETERMINED);
        }
        if (certStatus.getCertStatus() == CertStatus.UNDETERMINED)
        {
            throw new AnnotatedException("Certificate status could not be determined.");
        }
    }

    protected static int prepareNextCertJ(
        CertPath certPath,
        int index,
        int inhibitAnyPolicy)
        throws CertPathValidatorException
    {
        List certs = certPath.getCertificates();
        X509Certificate cert = (X509Certificate)certs.get(index);
        //
        // (j)
        //
        DERInteger iap = null;
        try
        {
            iap = DERInteger.getInstance(CertPathValidatorUtilities.getExtensionValue(cert,
                RFC3280CertPathUtilities.INHIBIT_ANY_POLICY));
        }
        catch (Exception e)
        {
            throw new ExtCertPathValidatorException("Inhibit any-policy extension cannot be decoded.", e, certPath,
                index);
        }

        if (iap != null)
        {
            int _inhibitAnyPolicy = iap.getValue().intValue();

            if (_inhibitAnyPolicy < inhibitAnyPolicy)
            {
                return _inhibitAnyPolicy;
            }
        }
        return inhibitAnyPolicy;
    }

    protected static void prepareNextCertK(
        CertPath certPath,
        int index)
        throws CertPathValidatorException
    {
        List certs = certPath.getCertificates();
        X509Certificate cert = (X509Certificate)certs.get(index);
        //
        // (k)
        //
        BasicConstraints bc = null;
        try
        {
            bc = BasicConstraints.getInstance(CertPathValidatorUtilities.getExtensionValue(cert,
                RFC3280CertPathUtilities.BASIC_CONSTRAINTS));
        }
        catch (Exception e)
        {
            throw new ExtCertPathValidatorException("Basic constraints extension cannot be decoded.", e, certPath,
                index);
        }
        if (bc != null)
        {
            if (!(bc.isCA()))
            {
                throw new CertPathValidatorException("Not a CA certificate");
            }
        }
        else
        {
            throw new CertPathValidatorException("Intermediate certificate lacks BasicConstraints");
        }
    }

    protected static int prepareNextCertL(
        CertPath certPath,
        int index,
        int maxPathLength)
        throws CertPathValidatorException
    {
        List certs = certPath.getCertificates();
        X509Certificate cert = (X509Certificate)certs.get(index);
        //
        // (l)
        //
        if (!CertPathValidatorUtilities.isSelfIssued(cert))
        {
            if (maxPathLength <= 0)
            {
                throw new ExtCertPathValidatorException("Max path length not greater than zero", null, certPath, index);
            }

            return maxPathLength - 1;
        }
        return maxPathLength;
    }

    protected static int prepareNextCertM(
        CertPath certPath,
        int index,
        int maxPathLength)
        throws CertPathValidatorException
    {
        List certs = certPath.getCertificates();
        X509Certificate cert = (X509Certificate)certs.get(index);

        //
        // (m)
        //
        BasicConstraints bc = null;
        try
        {
            bc = BasicConstraints.getInstance(CertPathValidatorUtilities.getExtensionValue(cert,
                RFC3280CertPathUtilities.BASIC_CONSTRAINTS));
        }
        catch (Exception e)
        {
            throw new ExtCertPathValidatorException("Basic constraints extension cannot be decoded.", e, certPath,
                index);
        }
        if (bc != null)
        {
            BigInteger _pathLengthConstraint = bc.getPathLenConstraint();

            if (_pathLengthConstraint != null)
            {
                int _plc = _pathLengthConstraint.intValue();

                if (_plc < maxPathLength)
                {
                    return _plc;
                }
            }
        }
        return maxPathLength;
    }

    protected static void prepareNextCertN(
        CertPath certPath,
        int index)
        throws CertPathValidatorException
    {
        List certs = certPath.getCertificates();
        X509Certificate cert = (X509Certificate)certs.get(index);

        //
        // (n)
        //
        boolean[] _usage = cert.getKeyUsage();

        if ((_usage != null) && !_usage[RFC3280CertPathUtilities.KEY_CERT_SIGN])
        {
            throw new ExtCertPathValidatorException(
                "Issuer certificate keyusage extension is critical and does not permit key signing.", null,
                certPath, index);
        }
    }

    protected static void prepareNextCertO(
        CertPath certPath,
        int index,
        Set criticalExtensions,
        List pathCheckers)
        throws CertPathValidatorException
    {
        List certs = certPath.getCertificates();
        X509Certificate cert = (X509Certificate)certs.get(index);
        //
        // (o)
        //

        Iterator tmpIter;
        tmpIter = pathCheckers.iterator();
        while (tmpIter.hasNext())
        {
            try
            {
                ((PKIXCertPathChecker)tmpIter.next()).check(cert, criticalExtensions);
            }
            catch (CertPathValidatorException e)
            {
                throw new CertPathValidatorException(e.getMessage(), e.getCause(), certPath, index);
            }
        }
        if (!criticalExtensions.isEmpty())
        {
            throw new ExtCertPathValidatorException("Certificate has unsupported critical extension.", null, certPath,
                index);
        }
    }

    protected static int prepareNextCertH1(
        CertPath certPath,
        int index,
        int explicitPolicy)
    {
        List certs = certPath.getCertificates();
        X509Certificate cert = (X509Certificate)certs.get(index);
        //
        // (h)
        //
        if (!CertPathValidatorUtilities.isSelfIssued(cert))
        {
            //
            // (1)
            //
            if (explicitPolicy != 0)
            {
                return explicitPolicy - 1;
            }
        }
        return explicitPolicy;
    }

    protected static int prepareNextCertH2(
        CertPath certPath,
        int index,
        int policyMapping)
    {
        List certs = certPath.getCertificates();
        X509Certificate cert = (X509Certificate)certs.get(index);
        //
        // (h)
        //
        if (!CertPathValidatorUtilities.isSelfIssued(cert))
        {
            //
            // (2)
            //
            if (policyMapping != 0)
            {
                return policyMapping - 1;
            }
        }
        return policyMapping;
    }

    protected static int prepareNextCertH3(
        CertPath certPath,
        int index,
        int inhibitAnyPolicy)
    {
        List certs = certPath.getCertificates();
        X509Certificate cert = (X509Certificate)certs.get(index);
        //
        // (h)
        //
        if (!CertPathValidatorUtilities.isSelfIssued(cert))
        {
            //
            // (3)
            //
            if (inhibitAnyPolicy != 0)
            {
                return inhibitAnyPolicy - 1;
            }
        }
        return inhibitAnyPolicy;
    }

    protected static final String[] crlReasons = new String[]
        {
            "unspecified",
            "keyCompromise",
            "cACompromise",
            "affiliationChanged",
            "superseded",
            "cessationOfOperation",
            "certificateHold",
            "unknown",
            "removeFromCRL",
            "privilegeWithdrawn",
            "aACompromise"};

    protected static int wrapupCertA(
        int explicitPolicy,
        X509Certificate cert)
    {
        //
        // (a)
        //
        if (!CertPathValidatorUtilities.isSelfIssued(cert) && (explicitPolicy != 0))
        {
            explicitPolicy--;
        }
        return explicitPolicy;
    }

    protected static int wrapupCertB(
        CertPath certPath,
        int index,
        int explicitPolicy)
        throws CertPathValidatorException
    {
        List certs = certPath.getCertificates();
        X509Certificate cert = (X509Certificate)certs.get(index);
        //
        // (b)
        //
        int tmpInt;
        ASN1Sequence pc = null;
        try
        {
            pc = DERSequence.getInstance(CertPathValidatorUtilities.getExtensionValue(cert,
                RFC3280CertPathUtilities.POLICY_CONSTRAINTS));
        }
        catch (AnnotatedException e)
        {
            throw new ExtCertPathValidatorException("Policy constraints could not be decoded.", e, certPath, index);
        }
        if (pc != null)
        {
            Enumeration policyConstraints = pc.getObjects();

            while (policyConstraints.hasMoreElements())
            {
                ASN1TaggedObject constraint = (ASN1TaggedObject)policyConstraints.nextElement();
                switch (constraint.getTagNo())
                {
                    case 0:
                        try
                        {
                            tmpInt = DERInteger.getInstance(constraint, false).getValue().intValue();
                        }
                        catch (Exception e)
                        {
                            throw new ExtCertPathValidatorException(
                                "Policy constraints requireExplicitPolicy field could not be decoded.", e, certPath,
                                index);
                        }
                        if (tmpInt == 0)
                        {
                            return 0;
                        }
                        break;
                }
            }
        }
        return explicitPolicy;
    }

    protected static void wrapupCertF(
        CertPath certPath,
        int index,
        List pathCheckers,
        Set criticalExtensions)
        throws CertPathValidatorException
    {
        List certs = certPath.getCertificates();
        X509Certificate cert = (X509Certificate)certs.get(index);
        Iterator tmpIter;
        tmpIter = pathCheckers.iterator();
        while (tmpIter.hasNext())
        {
            try
            {
                ((PKIXCertPathChecker)tmpIter.next()).check(cert, criticalExtensions);
            }
            catch (CertPathValidatorException e)
            {
                throw new ExtCertPathValidatorException("Additional certificate path checker failed.", e, certPath,
                    index);
            }
        }

        if (!criticalExtensions.isEmpty())
        {
            throw new ExtCertPathValidatorException("Certificate has unsupported critical extension", null, certPath,
                index);
        }
    }

    protected static PKIXPolicyNode wrapupCertG(
        CertPath certPath,
        ExtendedPKIXParameters paramsPKIX,
        Set userInitialPolicySet,
        int index,
        List[] policyNodes,
        PKIXPolicyNode validPolicyTree,
        Set acceptablePolicies)
        throws CertPathValidatorException
    {
        int n = certPath.getCertificates().size();
        //
        // (g)
        //
        PKIXPolicyNode intersection;

        //
        // (g) (i)
        //
        if (validPolicyTree == null)
        {
            if (paramsPKIX.isExplicitPolicyRequired())
            {
                throw new ExtCertPathValidatorException("Explicit policy requested but none available.", null,
                    certPath, index);
            }
            intersection = null;
        }
        else if (CertPathValidatorUtilities.isAnyPolicy(userInitialPolicySet)) // (g)
        // (ii)
        {
            if (paramsPKIX.isExplicitPolicyRequired())
            {
                if (acceptablePolicies.isEmpty())
                {
                    throw new ExtCertPathValidatorException("Explicit policy requested but none available.", null,
                        certPath, index);
                }
                else
                {
                    Set _validPolicyNodeSet = new HashSet();

                    for (int j = 0; j < policyNodes.length; j++)
                    {
                        List _nodeDepth = policyNodes[j];

                        for (int k = 0; k < _nodeDepth.size(); k++)
                        {
                            PKIXPolicyNode _node = (PKIXPolicyNode)_nodeDepth.get(k);

                            if (RFC3280CertPathUtilities.ANY_POLICY.equals(_node.getValidPolicy()))
                            {
                                Iterator _iter = _node.getChildren();
                                while (_iter.hasNext())
                                {
                                    _validPolicyNodeSet.add(_iter.next());
                                }
                            }
                        }
                    }

                    Iterator _vpnsIter = _validPolicyNodeSet.iterator();
                    while (_vpnsIter.hasNext())
                    {
                        PKIXPolicyNode _node = (PKIXPolicyNode)_vpnsIter.next();
                        String _validPolicy = _node.getValidPolicy();

                        if (!acceptablePolicies.contains(_validPolicy))
                        {
                            // validPolicyTree =
                            // removePolicyNode(validPolicyTree, policyNodes,
                            // _node);
                        }
                    }
                    if (validPolicyTree != null)
                    {
                        for (int j = (n - 1); j >= 0; j--)
                        {
                            List nodes = policyNodes[j];

                            for (int k = 0; k < nodes.size(); k++)
                            {
                                PKIXPolicyNode node = (PKIXPolicyNode)nodes.get(k);
                                if (!node.hasChildren())
                                {
                                    validPolicyTree = CertPathValidatorUtilities.removePolicyNode(validPolicyTree,
                                        policyNodes, node);
                                }
                            }
                        }
                    }
                }
            }

            intersection = validPolicyTree;
        }
        else
        {
            //
            // (g) (iii)
            //
            // This implementation is not exactly same as the one described in
            // RFC3280.
            // However, as far as the validation result is concerned, both
            // produce
            // adequate result. The only difference is whether AnyPolicy is
            // remain
            // in the policy tree or not.
            //
            // (g) (iii) 1
            //
            Set _validPolicyNodeSet = new HashSet();

            for (int j = 0; j < policyNodes.length; j++)
            {
                List _nodeDepth = policyNodes[j];

                for (int k = 0; k < _nodeDepth.size(); k++)
                {
                    PKIXPolicyNode _node = (PKIXPolicyNode)_nodeDepth.get(k);

                    if (RFC3280CertPathUtilities.ANY_POLICY.equals(_node.getValidPolicy()))
                    {
                        Iterator _iter = _node.getChildren();
                        while (_iter.hasNext())
                        {
                            PKIXPolicyNode _c_node = (PKIXPolicyNode)_iter.next();
                            if (!RFC3280CertPathUtilities.ANY_POLICY.equals(_c_node.getValidPolicy()))
                            {
                                _validPolicyNodeSet.add(_c_node);
                            }
                        }
                    }
                }
            }

            //
            // (g) (iii) 2
            //
            Iterator _vpnsIter = _validPolicyNodeSet.iterator();
            while (_vpnsIter.hasNext())
            {
                PKIXPolicyNode _node = (PKIXPolicyNode)_vpnsIter.next();
                String _validPolicy = _node.getValidPolicy();

                if (!userInitialPolicySet.contains(_validPolicy))
                {
                    validPolicyTree = CertPathValidatorUtilities.removePolicyNode(validPolicyTree, policyNodes, _node);
                }
            }

            //
            // (g) (iii) 4
            //
            if (validPolicyTree != null)
            {
                for (int j = (n - 1); j >= 0; j--)
                {
                    List nodes = policyNodes[j];

                    for (int k = 0; k < nodes.size(); k++)
                    {
                        PKIXPolicyNode node = (PKIXPolicyNode)nodes.get(k);
                        if (!node.hasChildren())
                        {
                            validPolicyTree = CertPathValidatorUtilities.removePolicyNode(validPolicyTree, policyNodes,
                                node);
                        }
                    }
                }
            }

            intersection = validPolicyTree;
        }
        return intersection;
    }

}
