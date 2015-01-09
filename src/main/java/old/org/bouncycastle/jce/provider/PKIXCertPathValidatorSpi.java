package old.org.bouncycastle.jce.provider;

import java.security.InvalidAlgorithmParameterException;
import java.security.PublicKey;
import java.security.cert.CertPath;
import java.security.cert.CertPathParameters;
import java.security.cert.CertPathValidatorException;
import java.security.cert.CertPathValidatorResult;
import java.security.cert.CertPathValidatorSpi;
import java.security.cert.PKIXCertPathChecker;
import java.security.cert.PKIXCertPathValidatorResult;
import java.security.cert.PKIXParameters;
import java.security.cert.TrustAnchor;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.security.auth.x500.X500Principal;

import old.org.bouncycastle.asn1.DEREncodable;
import old.org.bouncycastle.asn1.DERObjectIdentifier;
import old.org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import old.org.bouncycastle.jce.exception.ExtCertPathValidatorException;
import old.org.bouncycastle.x509.ExtendedPKIXParameters;

/**
 * CertPathValidatorSpi implementation for X.509 Certificate validation � la RFC
 * 3280.
 */
public class PKIXCertPathValidatorSpi
        extends CertPathValidatorSpi
{

    public CertPathValidatorResult engineValidate(
            CertPath certPath,
            CertPathParameters params)
            throws CertPathValidatorException,
            InvalidAlgorithmParameterException
    {
        if (!(params instanceof PKIXParameters))
        {
            throw new InvalidAlgorithmParameterException("Parameters must be a " + PKIXParameters.class.getName()
                    + " instance.");
        }

        ExtendedPKIXParameters paramsPKIX;
        if (params instanceof ExtendedPKIXParameters)
        {
            paramsPKIX = (ExtendedPKIXParameters)params;
        }
        else
        {
            paramsPKIX = ExtendedPKIXParameters.getInstance((PKIXParameters)params);
        }
        if (paramsPKIX.getTrustAnchors() == null)
        {
            throw new InvalidAlgorithmParameterException(
                    "trustAnchors is null, this is not allowed for certification path validation.");
        }

        //
        // 6.1.1 - inputs
        //

        //
        // (a)
        //
        List certs = certPath.getCertificates();
        int n = certs.size();

        if (certs.isEmpty())
        {
            throw new CertPathValidatorException("Certification path is empty.", null, certPath, 0);
        }

        //
        // (b)
        //
        // Date validDate = CertPathValidatorUtilities.getValidDate(paramsPKIX);

        //
        // (c)
        //
        Set userInitialPolicySet = paramsPKIX.getInitialPolicies();

        //
        // (d)
        // 
        TrustAnchor trust;
        try
        {
            trust = CertPathValidatorUtilities.findTrustAnchor((X509Certificate) certs.get(certs.size() - 1),
                    paramsPKIX.getTrustAnchors(), paramsPKIX.getSigProvider());
        }
        catch (AnnotatedException e)
        {
            throw new CertPathValidatorException(e.getMessage(), e, certPath, certs.size() - 1);
        }

        if (trust == null)
        {
            throw new CertPathValidatorException("Trust anchor for certification path not found.", null, certPath, -1);
        }

        //
        // (e), (f), (g) are part of the paramsPKIX object.
        //
        Iterator certIter;
        int index = 0;
        int i;
        // Certificate for each interation of the validation loop
        // Signature information for each iteration of the validation loop
        //
        // 6.1.2 - setup
        //

        //
        // (a)
        //
        List[] policyNodes = new ArrayList[n + 1];
        for (int j = 0; j < policyNodes.length; j++)
        {
            policyNodes[j] = new ArrayList();
        }

        Set policySet = new HashSet();

        policySet.add(RFC3280CertPathUtilities.ANY_POLICY);

        PKIXPolicyNode validPolicyTree = new PKIXPolicyNode(new ArrayList(), 0, policySet, null, new HashSet(),
                RFC3280CertPathUtilities.ANY_POLICY, false);

        policyNodes[0].add(validPolicyTree);

        //
        // (b) and (c)
        //
        PKIXNameConstraintValidator nameConstraintValidator = new PKIXNameConstraintValidator();

        // (d)
        //
        int explicitPolicy;
        Set acceptablePolicies = new HashSet();

        if (paramsPKIX.isExplicitPolicyRequired())
        {
            explicitPolicy = 0;
        }
        else
        {
            explicitPolicy = n + 1;
        }

        //
        // (e)
        //
        int inhibitAnyPolicy;

        if (paramsPKIX.isAnyPolicyInhibited())
        {
            inhibitAnyPolicy = 0;
        }
        else
        {
            inhibitAnyPolicy = n + 1;
        }

        //
        // (f)
        //
        int policyMapping;

        if (paramsPKIX.isPolicyMappingInhibited())
        {
            policyMapping = 0;
        }
        else
        {
            policyMapping = n + 1;
        }

        //
        // (g), (h), (i), (j)
        //
        PublicKey workingPublicKey;
        X500Principal workingIssuerName;

        X509Certificate sign = trust.getTrustedCert();
        try
        {
            if (sign != null)
            {
                workingIssuerName = CertPathValidatorUtilities.getSubjectPrincipal(sign);
                workingPublicKey = sign.getPublicKey();
            }
            else
            {
                workingIssuerName = new X500Principal(trust.getCAName());
                workingPublicKey = trust.getCAPublicKey();
            }
        }
        catch (IllegalArgumentException ex)
        {
            throw new ExtCertPathValidatorException("Subject of trust anchor could not be (re)encoded.", ex, certPath,
                    -1);
        }

        AlgorithmIdentifier workingAlgId = null;
        try
        {
            workingAlgId = CertPathValidatorUtilities.getAlgorithmIdentifier(workingPublicKey);
        }
        catch (CertPathValidatorException e)
        {
            throw new ExtCertPathValidatorException(
                    "Algorithm identifier of public key of trust anchor could not be read.", e, certPath, -1);
        }
        DERObjectIdentifier workingPublicKeyAlgorithm = workingAlgId.getObjectId();
        DEREncodable workingPublicKeyParameters = workingAlgId.getParameters();

        //
        // (k)
        //
        int maxPathLength = n;

        //
        // 6.1.3
        //

        if (paramsPKIX.getTargetConstraints() != null
                && !paramsPKIX.getTargetConstraints().match((X509Certificate) certs.get(0)))
        {
            throw new ExtCertPathValidatorException(
                    "Target certificate in certification path does not match targetConstraints.", null, certPath, 0);
        }

        // 
        // initialize CertPathChecker's
        //
        List pathCheckers = paramsPKIX.getCertPathCheckers();
        certIter = pathCheckers.iterator();
        while (certIter.hasNext())
        {
            ((PKIXCertPathChecker) certIter.next()).init(false);
        }

        X509Certificate cert = null;

        for (index = certs.size() - 1; index >= 0; index--)
        {
            // try
            // {
            //
            // i as defined in the algorithm description
            //
            i = n - index;

            //
            // set certificate to be checked in this round
            // sign and workingPublicKey and workingIssuerName are set
            // at the end of the for loop and initialized the
            // first time from the TrustAnchor
            //
            cert = (X509Certificate) certs.get(index);
            boolean verificationAlreadyPerformed = (index == certs.size() - 1);

            //
            // 6.1.3
            //

            RFC3280CertPathUtilities.processCertA(certPath, paramsPKIX, index, workingPublicKey,
                verificationAlreadyPerformed, workingIssuerName, sign);

            RFC3280CertPathUtilities.processCertBC(certPath, index, nameConstraintValidator);

            validPolicyTree = RFC3280CertPathUtilities.processCertD(certPath, index, acceptablePolicies,
                    validPolicyTree, policyNodes, inhibitAnyPolicy);

            validPolicyTree = RFC3280CertPathUtilities.processCertE(certPath, index, validPolicyTree);

            RFC3280CertPathUtilities.processCertF(certPath, index, validPolicyTree, explicitPolicy);

            //
            // 6.1.4
            //

            if (i != n)
            {
                if (cert != null && cert.getVersion() == 1)
                {
                    throw new CertPathValidatorException("Version 1 certificates can't be used as CA ones.", null,
                            certPath, index);
                }

                RFC3280CertPathUtilities.prepareNextCertA(certPath, index);

                validPolicyTree = RFC3280CertPathUtilities.prepareCertB(certPath, index, policyNodes, validPolicyTree,
                        policyMapping);

                RFC3280CertPathUtilities.prepareNextCertG(certPath, index, nameConstraintValidator);

                // (h)
                explicitPolicy = RFC3280CertPathUtilities.prepareNextCertH1(certPath, index, explicitPolicy);
                policyMapping = RFC3280CertPathUtilities.prepareNextCertH2(certPath, index, policyMapping);
                inhibitAnyPolicy = RFC3280CertPathUtilities.prepareNextCertH3(certPath, index, inhibitAnyPolicy);

                //
                // (i)
                //
                explicitPolicy = RFC3280CertPathUtilities.prepareNextCertI1(certPath, index, explicitPolicy);
                policyMapping = RFC3280CertPathUtilities.prepareNextCertI2(certPath, index, policyMapping);

                // (j)
                inhibitAnyPolicy = RFC3280CertPathUtilities.prepareNextCertJ(certPath, index, inhibitAnyPolicy);

                // (k)
                RFC3280CertPathUtilities.prepareNextCertK(certPath, index);

                // (l)
                maxPathLength = RFC3280CertPathUtilities.prepareNextCertL(certPath, index, maxPathLength);

                // (m)
                maxPathLength = RFC3280CertPathUtilities.prepareNextCertM(certPath, index, maxPathLength);

                // (n)
                RFC3280CertPathUtilities.prepareNextCertN(certPath, index);

                Set criticalExtensions = cert.getCriticalExtensionOIDs();
                if (criticalExtensions != null)
                {
                    criticalExtensions = new HashSet(criticalExtensions);

                    // these extensions are handled by the algorithm
                    criticalExtensions.remove(RFC3280CertPathUtilities.KEY_USAGE);
                    criticalExtensions.remove(RFC3280CertPathUtilities.CERTIFICATE_POLICIES);
                    criticalExtensions.remove(RFC3280CertPathUtilities.POLICY_MAPPINGS);
                    criticalExtensions.remove(RFC3280CertPathUtilities.INHIBIT_ANY_POLICY);
                    criticalExtensions.remove(RFC3280CertPathUtilities.ISSUING_DISTRIBUTION_POINT);
                    criticalExtensions.remove(RFC3280CertPathUtilities.DELTA_CRL_INDICATOR);
                    criticalExtensions.remove(RFC3280CertPathUtilities.POLICY_CONSTRAINTS);
                    criticalExtensions.remove(RFC3280CertPathUtilities.BASIC_CONSTRAINTS);
                    criticalExtensions.remove(RFC3280CertPathUtilities.SUBJECT_ALTERNATIVE_NAME);
                    criticalExtensions.remove(RFC3280CertPathUtilities.NAME_CONSTRAINTS);
                }
                else
                {
                    criticalExtensions = new HashSet();
                }

                // (o)
                RFC3280CertPathUtilities.prepareNextCertO(certPath, index, criticalExtensions, pathCheckers);
                
                // set signing certificate for next round
                sign = cert;

                // (c)
                workingIssuerName = CertPathValidatorUtilities.getSubjectPrincipal(sign);

                // (d)
                try
                {
                    workingPublicKey = CertPathValidatorUtilities.getNextWorkingKey(certPath.getCertificates(), index);
                }
                catch (CertPathValidatorException e)
                {
                    throw new CertPathValidatorException("Next working key could not be retrieved.", e, certPath, index);
                }

                workingAlgId = CertPathValidatorUtilities.getAlgorithmIdentifier(workingPublicKey);
                // (f)
                workingPublicKeyAlgorithm = workingAlgId.getObjectId();
                // (e)
                workingPublicKeyParameters = workingAlgId.getParameters();
            }
        }

        //
        // 6.1.5 Wrap-up procedure
        //

        explicitPolicy = RFC3280CertPathUtilities.wrapupCertA(explicitPolicy, cert);

        explicitPolicy = RFC3280CertPathUtilities.wrapupCertB(certPath, index + 1, explicitPolicy);

        //
        // (c) (d) and (e) are already done
        //

        //
        // (f)
        //
        Set criticalExtensions = cert.getCriticalExtensionOIDs();

        if (criticalExtensions != null)
        {
            criticalExtensions = new HashSet(criticalExtensions);
            // these extensions are handled by the algorithm
            criticalExtensions.remove(RFC3280CertPathUtilities.KEY_USAGE);
            criticalExtensions.remove(RFC3280CertPathUtilities.CERTIFICATE_POLICIES);
            criticalExtensions.remove(RFC3280CertPathUtilities.POLICY_MAPPINGS);
            criticalExtensions.remove(RFC3280CertPathUtilities.INHIBIT_ANY_POLICY);
            criticalExtensions.remove(RFC3280CertPathUtilities.ISSUING_DISTRIBUTION_POINT);
            criticalExtensions.remove(RFC3280CertPathUtilities.DELTA_CRL_INDICATOR);
            criticalExtensions.remove(RFC3280CertPathUtilities.POLICY_CONSTRAINTS);
            criticalExtensions.remove(RFC3280CertPathUtilities.BASIC_CONSTRAINTS);
            criticalExtensions.remove(RFC3280CertPathUtilities.SUBJECT_ALTERNATIVE_NAME);
            criticalExtensions.remove(RFC3280CertPathUtilities.NAME_CONSTRAINTS);
            criticalExtensions.remove(RFC3280CertPathUtilities.CRL_DISTRIBUTION_POINTS);
        }
        else
        {
            criticalExtensions = new HashSet();
        }

        RFC3280CertPathUtilities.wrapupCertF(certPath, index + 1, pathCheckers, criticalExtensions);

        PKIXPolicyNode intersection = RFC3280CertPathUtilities.wrapupCertG(certPath, paramsPKIX, userInitialPolicySet,
                index + 1, policyNodes, validPolicyTree, acceptablePolicies);

        if ((explicitPolicy > 0) || (intersection != null))
        {
            return new PKIXCertPathValidatorResult(trust, intersection, cert.getPublicKey());
        }

        throw new CertPathValidatorException("Path processing failed on policy.", null, certPath, index);
    }

}
