package old.org.bouncycastle.jce.provider;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.cert.CertPath;
import java.security.cert.CertPathValidatorException;
import java.security.cert.CertStore;
import java.security.cert.CertStoreException;
import java.security.cert.Certificate;
import java.security.cert.CertificateParsingException;
import java.security.cert.PKIXParameters;
import java.security.cert.PolicyQualifierInfo;
import java.security.cert.TrustAnchor;
import java.security.cert.X509CRL;
import java.security.cert.X509CRLSelector;
import java.security.cert.X509CertSelector;
import java.security.cert.X509Certificate;
import java.security.interfaces.DSAParams;
import java.security.interfaces.DSAPublicKey;
import java.security.spec.DSAPublicKeySpec;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.security.auth.x500.X500Principal;

import old.org.bouncycastle.asn1.ASN1InputStream;
import old.org.bouncycastle.asn1.ASN1Object;
import old.org.bouncycastle.asn1.ASN1OctetString;
import old.org.bouncycastle.asn1.ASN1OutputStream;
import old.org.bouncycastle.asn1.ASN1Sequence;
import old.org.bouncycastle.asn1.DEREnumerated;
import old.org.bouncycastle.asn1.DERGeneralizedTime;
import old.org.bouncycastle.asn1.DERIA5String;
import old.org.bouncycastle.asn1.DERObject;
import old.org.bouncycastle.asn1.DERObjectIdentifier;
import old.org.bouncycastle.asn1.DERSequence;
import old.org.bouncycastle.asn1.isismtt.ISISMTTObjectIdentifiers;
import old.org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import old.org.bouncycastle.asn1.x509.CRLDistPoint;
import old.org.bouncycastle.asn1.x509.CRLNumber;
import old.org.bouncycastle.asn1.x509.CRLReason;
import old.org.bouncycastle.asn1.x509.CertificateList;
import old.org.bouncycastle.asn1.x509.DistributionPoint;
import old.org.bouncycastle.asn1.x509.DistributionPointName;
import old.org.bouncycastle.asn1.x509.GeneralName;
import old.org.bouncycastle.asn1.x509.GeneralNames;
import old.org.bouncycastle.asn1.x509.PolicyInformation;
import old.org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import old.org.bouncycastle.asn1.x509.X509Extensions;
import old.org.bouncycastle.jce.X509LDAPCertStoreParameters;
import old.org.bouncycastle.jce.exception.ExtCertPathValidatorException;
import old.org.bouncycastle.util.Selector;
import old.org.bouncycastle.util.StoreException;
import old.org.bouncycastle.x509.ExtendedPKIXBuilderParameters;
import old.org.bouncycastle.x509.ExtendedPKIXParameters;
import old.org.bouncycastle.x509.X509AttributeCertStoreSelector;
import old.org.bouncycastle.x509.X509AttributeCertificate;
import old.org.bouncycastle.x509.X509CRLStoreSelector;
import old.org.bouncycastle.x509.X509CertStoreSelector;
import old.org.bouncycastle.x509.X509Store;

public class CertPathValidatorUtilities
{
    protected static final PKIXCRLUtil CRL_UTIL = new PKIXCRLUtil();

    protected static final String CERTIFICATE_POLICIES = X509Extensions.CertificatePolicies.getId();
    protected static final String BASIC_CONSTRAINTS = X509Extensions.BasicConstraints.getId();
    protected static final String POLICY_MAPPINGS = X509Extensions.PolicyMappings.getId();
    protected static final String SUBJECT_ALTERNATIVE_NAME = X509Extensions.SubjectAlternativeName.getId();
    protected static final String NAME_CONSTRAINTS = X509Extensions.NameConstraints.getId();
    protected static final String KEY_USAGE = X509Extensions.KeyUsage.getId();
    protected static final String INHIBIT_ANY_POLICY = X509Extensions.InhibitAnyPolicy.getId();
    protected static final String ISSUING_DISTRIBUTION_POINT = X509Extensions.IssuingDistributionPoint.getId();
    protected static final String DELTA_CRL_INDICATOR = X509Extensions.DeltaCRLIndicator.getId();
    protected static final String POLICY_CONSTRAINTS = X509Extensions.PolicyConstraints.getId();
    protected static final String FRESHEST_CRL = X509Extensions.FreshestCRL.getId();
    protected static final String CRL_DISTRIBUTION_POINTS = X509Extensions.CRLDistributionPoints.getId();
    protected static final String AUTHORITY_KEY_IDENTIFIER = X509Extensions.AuthorityKeyIdentifier.getId();

    protected static final String ANY_POLICY = "2.5.29.32.0";
    
    protected static final String CRL_NUMBER = X509Extensions.CRLNumber.getId();
    
    /*
     * key usage bits
     */
    protected static final int    KEY_CERT_SIGN = 5;
    protected static final int    CRL_SIGN = 6;

    protected static final String[] crlReasons = new String[] {
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
        "aACompromise" };
    
    /**
     * Search the given Set of TrustAnchor's for one that is the
     * issuer of the given X509 certificate. Uses the default provider
     * for signature verification.
     *
     * @param cert the X509 certificate
     * @param trustAnchors a Set of TrustAnchor's
     *
     * @return the <code>TrustAnchor</code> object if found or
     * <code>null</code> if not.
     *
     * @exception AnnotatedException
     *                if a TrustAnchor was found but the signature verification
     *                on the given certificate has thrown an exception.
     */
    protected static TrustAnchor findTrustAnchor(
        X509Certificate cert,
        Set             trustAnchors)
            throws AnnotatedException
    {
        return findTrustAnchor(cert, trustAnchors, null);
    }
    
    /**
     * Search the given Set of TrustAnchor's for one that is the
     * issuer of the given X509 certificate. Uses the specified
     * provider for signature verification, or the default provider
     * if null.
     *
     * @param cert the X509 certificate
     * @param trustAnchors a Set of TrustAnchor's
     * @param sigProvider the provider to use for signature verification
     *
     * @return the <code>TrustAnchor</code> object if found or
     * <code>null</code> if not.
     *
     * @exception AnnotatedException
     *                if a TrustAnchor was found but the signature verification
     *                on the given certificate has thrown an exception.
     */
    protected static TrustAnchor findTrustAnchor(
        X509Certificate cert,
        Set             trustAnchors,
        String          sigProvider) 
            throws AnnotatedException
    {
        TrustAnchor trust = null;
        PublicKey trustPublicKey = null;
        Exception invalidKeyEx = null;

        X509CertSelector certSelectX509 = new X509CertSelector();
        X500Principal certIssuer = getEncodedIssuerPrincipal(cert);

        try
        {
            certSelectX509.setSubject(certIssuer.getEncoded());
        }
        catch (IOException ex)
        {
            throw new AnnotatedException("Cannot set subject search criteria for trust anchor.", ex);
        }

        Iterator iter = trustAnchors.iterator();
        while (iter.hasNext() && trust == null)
        {
            trust = (TrustAnchor) iter.next();
            if (trust.getTrustedCert() != null)
            {
                if (certSelectX509.match(trust.getTrustedCert()))
                {
                    trustPublicKey = trust.getTrustedCert().getPublicKey();
                }
                else
                {
                    trust = null;
                }
            }
            else if (trust.getCAName() != null
                    && trust.getCAPublicKey() != null)
            {
                try
                {
                    X500Principal caName = new X500Principal(trust.getCAName());
                    if (certIssuer.equals(caName))
                    {
                        trustPublicKey = trust.getCAPublicKey();
                    }
                    else
                    {
                        trust = null;
                    }
                }
                catch (IllegalArgumentException ex)
                {
                    trust = null;
                }
            }
            else
            {
                trust = null;
            }

            if (trustPublicKey != null)
            {
                try
                {
                    verifyX509Certificate(cert, trustPublicKey, sigProvider);
                }
                catch (Exception ex)
                {
                    invalidKeyEx = ex;
                    trust = null;
                }
            }
        }

        if (trust == null && invalidKeyEx != null)
        {
            throw new AnnotatedException("TrustAnchor found but certificate validation failed.", invalidKeyEx);
        }

        return trust;
    }

    protected static void addAdditionalStoresFromAltNames(
            X509Certificate cert,
            ExtendedPKIXParameters pkixParams)
            throws CertificateParsingException
    {
        // if in the IssuerAltName extension an URI
        // is given, add an additinal X.509 store
        if (cert.getIssuerAlternativeNames() != null)
        {
            Iterator it = cert.getIssuerAlternativeNames().iterator();
            while (it.hasNext())
            {
                // look for URI
                List list = (List) it.next();
                if (list.get(0).equals(new Integer(GeneralName.uniformResourceIdentifier)))
                {
                    // found
                    String temp = (String) list.get(1);
                    CertPathValidatorUtilities.addAdditionalStoreFromLocation(temp, pkixParams);
                }
            }
        }
    }
    /**
     * Returns the issuer of an attribute certificate or certificate.
     * @param cert The attribute certificate or certificate.
     * @return The issuer as <code>X500Principal</code>.
     */
    protected static X500Principal getEncodedIssuerPrincipal(
        Object cert)
    {
        if (cert instanceof X509Certificate)
        {
            return ((X509Certificate)cert).getIssuerX500Principal();
        }
        else
        {
            return (X500Principal)((X509AttributeCertificate)cert).getIssuer().getPrincipals()[0];
        }
    }

    protected static Date getValidDate(PKIXParameters paramsPKIX)
    {
        Date validDate = paramsPKIX.getDate();

        if (validDate == null)
        {
            validDate = new Date();
        }

        return validDate;
    }

    protected static X500Principal getSubjectPrincipal(X509Certificate cert)
    {
        return cert.getSubjectX500Principal();
    }
    
    protected static boolean isSelfIssued(X509Certificate cert)
    {
        return cert.getSubjectDN().equals(cert.getIssuerDN());
    }
    
    
    /**
     * Extract the value of the given extension, if it exists.
     * 
     * @param ext
     *            The extension object.
     * @param oid
     *            The object identifier to obtain.
     * @throws AnnotatedException
     *             if the extension cannot be read.
     */
    protected static DERObject getExtensionValue(
        java.security.cert.X509Extension    ext,
        String                              oid)
        throws AnnotatedException
    {
        byte[]  bytes = ext.getExtensionValue(oid);
        if (bytes == null)
        {
            return null;
        }

        return getObject(oid, bytes);
    }
    
    private static DERObject getObject(
            String oid,
            byte[] ext)
            throws AnnotatedException
    {
        try
        {
            ASN1InputStream aIn = new ASN1InputStream(ext);
            ASN1OctetString octs = (ASN1OctetString)aIn.readObject();

            aIn = new ASN1InputStream(octs.getOctets());
            return aIn.readObject();
        }
        catch (Exception e)
        {
            throw new AnnotatedException("exception processing extension " + oid, e);
        }
    }
    
    protected static X500Principal getIssuerPrincipal(X509CRL crl)
    {
        return crl.getIssuerX500Principal();
    }
    
    protected static AlgorithmIdentifier getAlgorithmIdentifier(
        PublicKey key)
        throws CertPathValidatorException
    {
        try
        {
            ASN1InputStream      aIn = new ASN1InputStream(key.getEncoded());

            SubjectPublicKeyInfo info = SubjectPublicKeyInfo.getInstance(aIn.readObject());

            return info.getAlgorithmId();
        }
        catch (Exception e)
        {
            throw new ExtCertPathValidatorException("Subject public key cannot be decoded.", e);
        }
    }
    
    // crl checking


    //
    // policy checking
    // 
    
    protected static final Set getQualifierSet(ASN1Sequence qualifiers) 
        throws CertPathValidatorException
    {
        Set             pq   = new HashSet();
        
        if (qualifiers == null)
        {
            return pq;
        }
        
        ByteArrayOutputStream   bOut = new ByteArrayOutputStream();
        ASN1OutputStream        aOut = new ASN1OutputStream(bOut);
    
        Enumeration e = qualifiers.getObjects();
    
        while (e.hasMoreElements())
        {
            try
            {
                aOut.writeObject(e.nextElement());
    
                pq.add(new PolicyQualifierInfo(bOut.toByteArray()));
            }
            catch (IOException ex)
            {
                throw new ExtCertPathValidatorException("Policy qualifier info cannot be decoded.", ex);
            }
    
            bOut.reset();
        }
        
        return pq;
    }
    
    protected static PKIXPolicyNode removePolicyNode(
        PKIXPolicyNode  validPolicyTree,
        List     []        policyNodes,
        PKIXPolicyNode _node)
    {
        PKIXPolicyNode _parent = (PKIXPolicyNode)_node.getParent();
        
        if (validPolicyTree == null)
        {
            return null;
        }

        if (_parent == null)
        {
            for (int j = 0; j < policyNodes.length; j++)
            {
                policyNodes[j] = new ArrayList();
            }

            return null;
        }
        else
        {
            _parent.removeChild(_node);
            removePolicyNodeRecurse(policyNodes, _node);

            return validPolicyTree;
        }
    }
    
    private static void removePolicyNodeRecurse(
        List     []        policyNodes,
        PKIXPolicyNode  _node)
    {
        policyNodes[_node.getDepth()].remove(_node);

        if (_node.hasChildren())
        {
            Iterator _iter = _node.getChildren();
            while (_iter.hasNext())
            {
                PKIXPolicyNode _child = (PKIXPolicyNode)_iter.next();
                removePolicyNodeRecurse(policyNodes, _child);
            }
        }
    }
    
    
    protected static boolean processCertD1i(
        int                 index,
        List     []            policyNodes,
        DERObjectIdentifier pOid,
        Set                 pq)
    {
        List       policyNodeVec = policyNodes[index - 1];

        for (int j = 0; j < policyNodeVec.size(); j++)
        {
            PKIXPolicyNode node = (PKIXPolicyNode)policyNodeVec.get(j);
            Set            expectedPolicies = node.getExpectedPolicies();
            
            if (expectedPolicies.contains(pOid.getId()))
            {
                Set childExpectedPolicies = new HashSet();
                childExpectedPolicies.add(pOid.getId());
                
                PKIXPolicyNode child = new PKIXPolicyNode(new ArrayList(),
                                                           index,
                                                           childExpectedPolicies,
                                                           node,
                                                           pq,
                                                           pOid.getId(),
                                                           false);
                node.addChild(child);
                policyNodes[index].add(child);
                
                return true;
            }
        }
        
        return false;
    }

    protected static void processCertD1ii(
        int                 index,
        List     []            policyNodes,
        DERObjectIdentifier _poid,
        Set _pq)
    {
        List       policyNodeVec = policyNodes[index - 1];

        for (int j = 0; j < policyNodeVec.size(); j++)
        {
            PKIXPolicyNode _node = (PKIXPolicyNode)policyNodeVec.get(j);

            if (ANY_POLICY.equals(_node.getValidPolicy()))
            {
                Set _childExpectedPolicies = new HashSet();
                _childExpectedPolicies.add(_poid.getId());
                
                PKIXPolicyNode _child = new PKIXPolicyNode(new ArrayList(),
                                                           index,
                                                           _childExpectedPolicies,
                                                           _node,
                                                           _pq,
                                                           _poid.getId(),
                                                           false);
                _node.addChild(_child);
                policyNodes[index].add(_child);
                return;
            }
        }
    }
    
    protected static void prepareNextCertB1(
            int i,
            List[] policyNodes,
            String id_p,
            Map m_idp,
            X509Certificate cert
            ) throws AnnotatedException,CertPathValidatorException
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
                if (ANY_POLICY.equals(node.getValidPolicy()))
                {
                    Set pq = null;
                    ASN1Sequence policies = null;
                    try
                    {
                        policies = DERSequence.getInstance(getExtensionValue(cert, CERTIFICATE_POLICIES));
                    }
                    catch (Exception e)
                    {
                        throw

                        new AnnotatedException("Certificate policies cannot be decoded.", e);
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
                            throw new AnnotatedException("Policy information cannot be decoded.", ex);
                        }
                        if (ANY_POLICY.equals(pinfo.getPolicyIdentifier().getId()))
                        {
                            try
                            {
                            pq = getQualifierSet(pinfo.getPolicyQualifiers());
                            }
                            catch (CertPathValidatorException ex)
                            {
                                throw new ExtCertPathValidatorException(
                                        "Policy qualifier info set could not be built.", ex);
                            }
                            break;
                        }
                    }
                    boolean ci = false;
                    if (cert.getCriticalExtensionOIDs() != null)
                    {
                        ci = cert.getCriticalExtensionOIDs().contains(CERTIFICATE_POLICIES);
                    }

                    PKIXPolicyNode p_node = (PKIXPolicyNode)node.getParent();
                    if (ANY_POLICY.equals(p_node.getValidPolicy()))
                    {
                        PKIXPolicyNode c_node = new PKIXPolicyNode(
                                new ArrayList(), i,
                                (Set)m_idp.get(id_p),
                                p_node, pq, id_p, ci);
                        p_node.addChild(c_node);
                        policyNodes[i].add(c_node);
                    }
                    break;
                }
            }
        }
    }
    
    protected static PKIXPolicyNode prepareNextCertB2(
            int i,
            List[] policyNodes,
            String id_p,
            PKIXPolicyNode validPolicyTree) 
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
                            validPolicyTree = removePolicyNode(validPolicyTree, policyNodes, node2);
                            if (validPolicyTree == null)
                            {
                                break;
                            }
                        }
                    }
                }
            }
        }
        return validPolicyTree;
    }
    
    protected static boolean isAnyPolicy(
        Set policySet)
    {
        return policySet == null || policySet.contains(ANY_POLICY) || policySet.isEmpty();
    }
    
    protected static void addAdditionalStoreFromLocation(String location,
        ExtendedPKIXParameters pkixParams)
    {
        if (pkixParams.isAdditionalLocationsEnabled())
        {
            try
            {
                if (location.startsWith("ldap://"))
                {
                    // ldap://directory.d-trust.net/CN=D-TRUST
                    // Qualified CA 2003 1:PN,O=D-Trust GmbH,C=DE
                    // skip "ldap://"
                    location = location.substring(7);
                    // after first / baseDN starts
                    String base = null;
                    String url = null;
                    if (location.indexOf("/") != -1)
                    {
                        base = location.substring(location.indexOf("/"));
                        // URL
                        url = "ldap://"
                            + location.substring(0, location.indexOf("/"));
                    }
                    else
                    {
                        url = "ldap://" + location;
                    }
                    // use all purpose parameters
                    X509LDAPCertStoreParameters params = new X509LDAPCertStoreParameters.Builder(
                        url, base).build();
                    pkixParams.addAdditionalStore(X509Store.getInstance(
                        "CERTIFICATE/LDAP", params, BouncyCastleProvider.PROVIDER_NAME));
                    pkixParams.addAdditionalStore(X509Store.getInstance(
                        "CRL/LDAP", params, BouncyCastleProvider.PROVIDER_NAME));
                    pkixParams.addAdditionalStore(X509Store.getInstance(
                        "ATTRIBUTECERTIFICATE/LDAP", params, BouncyCastleProvider.PROVIDER_NAME));
                    pkixParams.addAdditionalStore(X509Store.getInstance(
                        "CERTIFICATEPAIR/LDAP", params, BouncyCastleProvider.PROVIDER_NAME));
                }
            }
            catch (Exception e)
            {
                // cannot happen
                throw new RuntimeException("Exception adding X.509 stores.");
            }
        }
    }

    /**
     * Return a Collection of all certificates or attribute certificates found
     * in the X509Store's that are matching the certSelect criteriums.
     *
     * @param certSelect a {@link Selector} object that will be used to select
     *            the certificates
     * @param certStores a List containing only {@link X509Store} objects. These
     *            are used to search for certificates.
     *
     * @return a Collection of all found {@link X509Certificate} or
     *         {@link old.org.bouncycastle.x509.X509AttributeCertificate} objects.
     *         May be empty but never <code>null</code>.
     */
    protected static Collection findCertificates(X509CertStoreSelector certSelect,
        List certStores) throws AnnotatedException
    {
        Set certs = new HashSet();
        Iterator iter = certStores.iterator();

        while (iter.hasNext())
        {
            Object obj = iter.next();

            if (obj instanceof X509Store)
            {
                X509Store certStore = (X509Store)obj;
                try
                {
                    certs.addAll(certStore.getMatches(certSelect));
                }
                catch (StoreException e)
                {
                    throw

                    new AnnotatedException(
                        "Problem while picking certificates from X.509 store.", e);
                }
            }
            else
            {
                CertStore certStore = (CertStore)obj;

                try
                {
                    certs.addAll(certStore.getCertificates(certSelect));
                }
                catch (CertStoreException e)
                {
                    throw new AnnotatedException(
                        "Problem while picking certificates from certificate store.",
                        e);
                }
            }
        }
        return certs;
    }

    protected static Collection findCertificates(X509AttributeCertStoreSelector certSelect,
                                                 List certStores)
    throws AnnotatedException
    {
        Set certs = new HashSet();
        Iterator iter = certStores.iterator();

        while (iter.hasNext())
        {
            Object obj = iter.next();

            if (obj instanceof X509Store)
            {
                X509Store certStore = (X509Store)obj;
                try
                {
                    certs.addAll(certStore.getMatches(certSelect));
                }
                catch (StoreException e)
                {
                    throw

                        new AnnotatedException(
                            "Problem while picking certificates from X.509 store.", e);
                }
            }
        }
        return certs;
    }

    protected static void addAdditionalStoresFromCRLDistributionPoint(
        CRLDistPoint crldp, ExtendedPKIXParameters pkixParams)
        throws AnnotatedException
    {
        if (crldp != null)
        {
            DistributionPoint dps[] = null;
            try
            {
                dps = crldp.getDistributionPoints();
            }
            catch (Exception e)
            {
                throw new AnnotatedException(
                    "Distribution points could not be read.", e);
            }
            for (int i = 0; i < dps.length; i++)
            {
                DistributionPointName dpn = dps[i].getDistributionPoint();
                // look for URIs in fullName
                if (dpn != null)
                {
                    if (dpn.getType() == DistributionPointName.FULL_NAME)
                    {
                        GeneralName[] genNames = GeneralNames.getInstance(
                            dpn.getName()).getNames();
                        // look for an URI
                        for (int j = 0; j < genNames.length; j++)
                        {
                            if (genNames[j].getTagNo() == GeneralName.uniformResourceIdentifier)
                            {
                                String location = DERIA5String.getInstance(
                                    genNames[j].getName()).getString();
                                CertPathValidatorUtilities
                                    .addAdditionalStoreFromLocation(location,
                                        pkixParams);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Add the CRL issuers from the cRLIssuer field of the distribution point or
     * from the certificate if not given to the issuer criterion of the
     * <code>selector</code>.
     * <p>
     * The <code>issuerPrincipals</code> are a collection with a single
     * <code>X500Principal</code> for <code>X509Certificate</code>s. For
     * {@link X509AttributeCertificate}s the issuer may contain more than one
     * <code>X500Principal</code>.
     *
     * @param dp The distribution point.
     * @param issuerPrincipals The issuers of the certificate or attribute
     *            certificate which contains the distribution point.
     * @param selector The CRL selector.
     * @param pkixParams The PKIX parameters containing the cert stores.
     * @throws AnnotatedException if an exception occurs while processing.
     * @throws ClassCastException if <code>issuerPrincipals</code> does not
     * contain only <code>X500Principal</code>s.
     */
    protected static void getCRLIssuersFromDistributionPoint(
        DistributionPoint dp,
        Collection issuerPrincipals,
        X509CRLSelector selector,
        ExtendedPKIXParameters pkixParams)
        throws AnnotatedException
    {
        List issuers = new ArrayList();
        // indirect CRL
        if (dp.getCRLIssuer() != null)
        {
            GeneralName genNames[] = dp.getCRLIssuer().getNames();
            // look for a DN
            for (int j = 0; j < genNames.length; j++)
            {
                if (genNames[j].getTagNo() == GeneralName.directoryName)
                {
                    try
                    {
                        issuers.add(new X500Principal(genNames[j].getName()
                            .getDERObject().getEncoded()));
                    }
                    catch (IOException e)
                    {
                        throw new AnnotatedException(
                            "CRL issuer information from distribution point cannot be decoded.",
                            e);
                    }
                }
            }
        }
        else
        {
            /*
             * certificate issuer is CRL issuer, distributionPoint field MUST be
             * present.
             */
            if (dp.getDistributionPoint() == null)
            {
                throw new AnnotatedException(
                    "CRL issuer is omitted from distribution point but no distributionPoint field present.");
            }
            // add and check issuer principals
            for (Iterator it=issuerPrincipals.iterator(); it.hasNext();)
            {
                issuers.add((X500Principal)it.next());
            }
        }
        // TODO: is not found although this should correctly add the rel name. selector of Sun is buggy here or PKI test case is invalid
        // distributionPoint
//        if (dp.getDistributionPoint() != null)
//        {
//            // look for nameRelativeToCRLIssuer
//            if (dp.getDistributionPoint().getType() == DistributionPointName.NAME_RELATIVE_TO_CRL_ISSUER)
//            {
//                // append fragment to issuer, only one
//                // issuer can be there, if this is given
//                if (issuers.size() != 1)
//                {
//                    throw new AnnotatedException(
//                        "nameRelativeToCRLIssuer field is given but more than one CRL issuer is given.");
//                }
//                DEREncodable relName = dp.getDistributionPoint().getName();
//                Iterator it = issuers.iterator();
//                List issuersTemp = new ArrayList(issuers.size());
//                while (it.hasNext())
//                {
//                    Enumeration e = null;
//                    try
//                    {
//                        e = ASN1Sequence.getInstance(
//                            new ASN1InputStream(((X500Principal) it.next())
//                                .getEncoded()).readObject()).getObjects();
//                    }
//                    catch (IOException ex)
//                    {
//                        throw new AnnotatedException(
//                            "Cannot decode CRL issuer information.", ex);
//                    }
//                    ASN1EncodableVector v = new ASN1EncodableVector();
//                    while (e.hasMoreElements())
//                    {
//                        v.add((DEREncodable) e.nextElement());
//                    }
//                    v.add(relName);
//                    issuersTemp.add(new X500Principal(new DERSequence(v)
//                        .getDEREncoded()));
//                }
//                issuers.clear();
//                issuers.addAll(issuersTemp);
//            }
//        }
        Iterator it = issuers.iterator();
        while (it.hasNext())
        {
            try
            {
                selector.addIssuerName(((X500Principal)it.next()).getEncoded());
            }
            catch (IOException ex)
            {
                throw new AnnotatedException(
                    "Cannot decode CRL issuer information.", ex);
            }
        }
    }

    private static BigInteger getSerialNumber(
            Object cert)
    {
        if (cert instanceof X509Certificate)
        {
            return ((X509Certificate) cert).getSerialNumber();
        }
        else
        {
            return ((X509AttributeCertificate) cert).getSerialNumber();
        }
    }
    
    protected static void getCertStatus(
            Date validDate,
            X509CRL crl,
            Object cert,
            CertStatus certStatus)
        throws AnnotatedException
    {
        // use BC X509CRLObject so that indirect CRLs are supported
        X509CRLObject bcCRL = null;
        try
        {
            bcCRL = new X509CRLObject(new CertificateList((ASN1Sequence) ASN1Sequence.fromByteArray(crl.getEncoded())));
        }
        catch (Exception exception)
        {
            throw new AnnotatedException("Bouncy Castle X509CRLObject could not be created.", exception);
        }
        // use BC X509CRLEntryObject, so that getCertificateIssuer() is
        // supported.
        X509CRLEntryObject crl_entry = (X509CRLEntryObject) bcCRL.getRevokedCertificate(getSerialNumber(cert));
        if (crl_entry != null
                && (getEncodedIssuerPrincipal(cert).equals(crl_entry.getCertificateIssuer()) || getEncodedIssuerPrincipal(cert)
                        .equals(getIssuerPrincipal(crl))))
        {
            DEREnumerated reasonCode = null;
            if (crl_entry.hasExtensions())
            {
                try
                {
                    reasonCode = DEREnumerated
                        .getInstance(CertPathValidatorUtilities
                            .getExtensionValue(crl_entry,
                                X509Extensions.ReasonCode.getId()));
                }
                catch (Exception e)
                {
                    new AnnotatedException(
                        "Reason code CRL entry extension could not be decoded.",
                        e);
                }
            }

            // for reason keyCompromise, caCompromise, aACompromise or
            // unspecified
            if (!(validDate.getTime() < crl_entry.getRevocationDate().getTime())
                || reasonCode == null
                || reasonCode.getValue().intValue() == 0
                || reasonCode.getValue().intValue() == 1
                || reasonCode.getValue().intValue() == 2
                || reasonCode.getValue().intValue() == 8)
            {

                // (i) or (j) (1)
                if (reasonCode != null)
                {
                    certStatus.setCertStatus(reasonCode.getValue().intValue());
                }
                // (i) or (j) (2)
                else
                {
                    certStatus.setCertStatus(CRLReason.unspecified);
                }
                certStatus.setRevocationDate(crl_entry.getRevocationDate());
            }
        }
    }

    /**
     * Fetches delta CRLs according to RFC 3280 section 5.2.4.
     *
     * @param currentDate The date for which the delta CRLs must be valid.
     * @param paramsPKIX The extended PKIX parameters.
     * @param completeCRL The complete CRL the delta CRL is for.
     * @return A <code>Set</code> of <code>X509CRL</code>s with delta CRLs.
     * @throws AnnotatedException if an exception occurs while picking the delta
     *             CRLs.
     */
    protected static Set getDeltaCRLs(Date currentDate,
        ExtendedPKIXParameters paramsPKIX, X509CRL completeCRL)
        throws AnnotatedException
    {

        X509CRLStoreSelector deltaSelect = new X509CRLStoreSelector();

        // 5.2.4 (a)
        try
        {
            deltaSelect.addIssuerName(CertPathValidatorUtilities
                .getIssuerPrincipal(completeCRL).getEncoded());
        }
        catch (IOException e)
        {
            new AnnotatedException("Cannot extract issuer from CRL.", e);
        }

        BigInteger completeCRLNumber = null;
        try
        {
            DERObject derObject = CertPathValidatorUtilities.getExtensionValue(completeCRL,
                    CRL_NUMBER);
            if (derObject != null)
            {
                completeCRLNumber = CRLNumber.getInstance(derObject).getPositiveValue();
            }
        }
        catch (Exception e)
        {
            throw new AnnotatedException(
                "CRL number extension could not be extracted from CRL.", e);
        }

        // 5.2.4 (b)
        byte[] idp = null;
        try
        {
            idp = completeCRL.getExtensionValue(ISSUING_DISTRIBUTION_POINT);
        }
        catch (Exception e)
        {
            throw new AnnotatedException(
                "Issuing distribution point extension value could not be read.",
                e);
        }

        // 5.2.4 (d)

        deltaSelect.setMinCRLNumber(completeCRLNumber == null ? null : completeCRLNumber
            .add(BigInteger.valueOf(1)));

        deltaSelect.setIssuingDistributionPoint(idp);
        deltaSelect.setIssuingDistributionPointEnabled(true);

        // 5.2.4 (c)
        deltaSelect.setMaxBaseCRLNumber(completeCRLNumber);

        // find delta CRLs
        Set temp = CRL_UTIL.findCRLs(deltaSelect, paramsPKIX, currentDate);

        Set result = new HashSet();

        for (Iterator it = temp.iterator(); it.hasNext();)
        {
            X509CRL crl = (X509CRL)it.next();
            
            if (isDeltaCRL(crl))
            {
                result.add(crl);
            }
        }

        return result;
    }

    private static boolean isDeltaCRL(X509CRL crl)
    {
        Set critical = crl.getCriticalExtensionOIDs();

        return critical.contains(RFC3280CertPathUtilities.DELTA_CRL_INDICATOR);
    }

    /**
     * Fetches complete CRLs according to RFC 3280.
     *
     * @param dp The distribution point for which the complete CRL
     * @param cert The <code>X509Certificate</code> or
     *            {@link old.org.bouncycastle.x509.X509AttributeCertificate} for
     *            which the CRL should be searched.
     * @param currentDate The date for which the delta CRLs must be valid.
     * @param paramsPKIX The extended PKIX parameters.
     * @return A <code>Set</code> of <code>X509CRL</code>s with complete
     *         CRLs.
     * @throws AnnotatedException if an exception occurs while picking the CRLs
     *             or no CRLs are found.
     */
    protected static Set getCompleteCRLs(DistributionPoint dp, Object cert,
        Date currentDate, ExtendedPKIXParameters paramsPKIX)
        throws AnnotatedException
    {
        X509CRLStoreSelector crlselect = new X509CRLStoreSelector();
        try
        {
            Set issuers = new HashSet();
            if (cert instanceof X509AttributeCertificate)
            {
                issuers.add(((X509AttributeCertificate)cert)
                    .getIssuer().getPrincipals()[0]);
            }
            else
            {
                issuers.add(getEncodedIssuerPrincipal(cert));
            }
            CertPathValidatorUtilities.getCRLIssuersFromDistributionPoint(dp, issuers, crlselect, paramsPKIX);
        }
        catch (AnnotatedException e)
        {
            new AnnotatedException(
                "Could not get issuer information from distribution point.", e);
        }
        if (cert instanceof X509Certificate)
        {
            crlselect.setCertificateChecking((X509Certificate)cert);
        }
        else if (cert instanceof X509AttributeCertificate)
        {
            crlselect.setAttrCertificateChecking((X509AttributeCertificate)cert);
        }



        crlselect.setCompleteCRLEnabled(true);

        Set crls = CRL_UTIL.findCRLs(crlselect, paramsPKIX, currentDate);

        if (crls.isEmpty())
        {
            if (cert instanceof X509AttributeCertificate)
            {
                X509AttributeCertificate aCert = (X509AttributeCertificate)cert;

                throw new AnnotatedException("No CRLs found for issuer \"" + aCert.getIssuer().getPrincipals()[0] + "\"");
            }
            else
            {
                X509Certificate xCert = (X509Certificate)cert;

                throw new AnnotatedException("No CRLs found for issuer \"" + xCert.getIssuerX500Principal() + "\"");
            }
        }
        return crls;
    }

    protected static Date getValidCertDateFromValidityModel(
        ExtendedPKIXParameters paramsPKIX, CertPath certPath, int index)
        throws AnnotatedException
    {
        if (paramsPKIX.getValidityModel() == ExtendedPKIXParameters.CHAIN_VALIDITY_MODEL)
        {
            // if end cert use given signing/encryption/... time
            if (index <= 0)
            {
                return CertPathValidatorUtilities.getValidDate(paramsPKIX);
                // else use time when previous cert was created
            }
            else
            {
                if (index - 1 == 0)
                {
                    DERGeneralizedTime dateOfCertgen = null;
                    try
                    {
                        byte[] extBytes = ((X509Certificate)certPath.getCertificates().get(index - 1)).getExtensionValue(ISISMTTObjectIdentifiers.id_isismtt_at_dateOfCertGen.getId());
                        if (extBytes != null)
                        {
                            dateOfCertgen = DERGeneralizedTime.getInstance(ASN1Object.fromByteArray(extBytes));
                        }
                    }
                    catch (IOException e)
                    {
                        throw new AnnotatedException(
                            "Date of cert gen extension could not be read.");
                    }
                    catch (IllegalArgumentException e)
                    {
                        throw new AnnotatedException(
                            "Date of cert gen extension could not be read.");
                    }
                    if (dateOfCertgen != null)
                    {
                        try
                        {
                            return dateOfCertgen.getDate();
                        }
                        catch (ParseException e)
                        {
                            throw new AnnotatedException(
                                "Date from date of cert gen extension could not be parsed.",
                                e);
                        }
                    }
                    return ((X509Certificate) certPath.getCertificates().get(
                        index - 1)).getNotBefore();
                }
                else
                {
                    return ((X509Certificate) certPath.getCertificates().get(
                        index - 1)).getNotBefore();
                }
            }
        }
        else
        {
            return getValidDate(paramsPKIX);
        }
    }

    /**
     * Return the next working key inheriting DSA parameters if necessary.
     * <p>
     * This methods inherits DSA parameters from the indexed certificate or
     * previous certificates in the certificate chain to the returned
     * <code>PublicKey</code>. The list is searched upwards, meaning the end
     * certificate is at position 0 and previous certificates are following.
     * </p>
     * <p>
     * If the indexed certificate does not contain a DSA key this method simply
     * returns the public key. If the DSA key already contains DSA parameters
     * the key is also only returned.
     * </p>
     * 
     * @param certs The certification path.
     * @param index The index of the certificate which contains the public key
     *            which should be extended with DSA parameters.
     * @return The public key of the certificate in list position
     *         <code>index</code> extended with DSA parameters if applicable.
     * @throws AnnotatedException if DSA parameters cannot be inherited.
     */
    protected static PublicKey getNextWorkingKey(List certs, int index)
        throws CertPathValidatorException
    {
        Certificate cert = (Certificate) certs.get(index);
        PublicKey pubKey = cert.getPublicKey();
        if (!(pubKey instanceof DSAPublicKey))
        {
            return pubKey;
        }
        DSAPublicKey dsaPubKey = (DSAPublicKey) pubKey;
        if (dsaPubKey.getParams() != null)
        {
            return dsaPubKey;
        }
        for (int i = index + 1; i < certs.size(); i++)
        {
            X509Certificate parentCert = (X509Certificate)certs.get(i);
            pubKey = parentCert.getPublicKey();
            if (!(pubKey instanceof DSAPublicKey))
            {
                throw new CertPathValidatorException(
                    "DSA parameters cannot be inherited from previous certificate.");
            }
            DSAPublicKey prevDSAPubKey = (DSAPublicKey) pubKey;
            if (prevDSAPubKey.getParams() == null)
            {
                continue;
            }
            DSAParams dsaParams = prevDSAPubKey.getParams();
            DSAPublicKeySpec dsaPubKeySpec = new DSAPublicKeySpec(
                dsaPubKey.getY(), dsaParams.getP(), dsaParams.getQ(), dsaParams.getG());
            try
            {
                KeyFactory keyFactory = KeyFactory.getInstance("DSA", BouncyCastleProvider.PROVIDER_NAME);
                return keyFactory.generatePublic(dsaPubKeySpec);
            }
            catch (Exception exception)
            {
                throw new RuntimeException(exception.getMessage());
            }
        }
        throw new CertPathValidatorException("DSA parameters cannot be inherited from previous certificate.");
    }
    
    /**
     * Find the issuer certificates of a given certificate.
     * 
     * @param cert
     *            The certificate for which an issuer should be found.
     * @param pkixParams
     * @return A <code>Collection</code> object containing the issuer
     *         <code>X509Certificate</code>s. Never <code>null</code>.
     * 
     * @exception AnnotatedException
     *                if an error occurs.
     */
    protected static Collection findIssuerCerts(
        X509Certificate cert,
        ExtendedPKIXBuilderParameters pkixParams)
            throws AnnotatedException
    {
        X509CertStoreSelector certSelect = new X509CertStoreSelector();
        Set certs = new HashSet();
        try
        {
            certSelect.setSubject(cert.getIssuerX500Principal().getEncoded());
        }
        catch (IOException ex)
        {
            throw new AnnotatedException(
                    "Subject criteria for certificate selector to find issuer certificate could not be set.", ex);
        }

        Iterator iter;

        try
        {
            List matches = new ArrayList();

            matches.addAll(CertPathValidatorUtilities.findCertificates(certSelect, pkixParams.getCertStores()));
            matches.addAll(CertPathValidatorUtilities.findCertificates(certSelect, pkixParams.getStores()));
            matches.addAll(CertPathValidatorUtilities.findCertificates(certSelect, pkixParams.getAdditionalStores()));

            iter = matches.iterator();
        }
        catch (AnnotatedException e)
        {
            throw new AnnotatedException("Issuer certificate cannot be searched.", e);
        }

        X509Certificate issuer = null;
        while (iter.hasNext())
        {
            issuer = (X509Certificate) iter.next();
            // issuer cannot be verified because possible DSA inheritance
            // parameters are missing
            certs.add(issuer);
        }
        return certs;
    }

    protected static void verifyX509Certificate(X509Certificate cert, PublicKey publicKey,
        String sigProvider)
            throws GeneralSecurityException
    {
        if (sigProvider == null)
        {
            cert.verify(publicKey);
        }
        else
        {
            cert.verify(publicKey, sigProvider);
        }
    }
}
