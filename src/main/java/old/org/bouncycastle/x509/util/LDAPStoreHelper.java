package old.org.bouncycastle.x509.util;

import old.org.bouncycastle.asn1.ASN1InputStream;
import old.org.bouncycastle.asn1.x509.CertificatePair;
import old.org.bouncycastle.asn1.x509.X509CertificateStructure;
import old.org.bouncycastle.jce.X509LDAPCertStoreParameters;
import old.org.bouncycastle.jce.provider.X509AttrCertParser;
import old.org.bouncycastle.jce.provider.X509CRLParser;
import old.org.bouncycastle.jce.provider.X509CertPairParser;
import old.org.bouncycastle.jce.provider.X509CertParser;
import old.org.bouncycastle.util.StoreException;
import old.org.bouncycastle.x509.X509AttributeCertStoreSelector;
import old.org.bouncycastle.x509.X509AttributeCertificate;
import old.org.bouncycastle.x509.X509CRLStoreSelector;
import old.org.bouncycastle.x509.X509CertPairStoreSelector;
import old.org.bouncycastle.x509.X509CertStoreSelector;
import old.org.bouncycastle.x509.X509CertificatePair;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.security.auth.x500.X500Principal;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.Principal;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * This is a general purpose implementation to get X.509 certificates, CRLs,
 * attribute certificates and cross certificates from a LDAP location.
 * <p/>
 * At first a search is performed in the ldap*AttributeNames of the
 * {@link old.org.bouncycastle.jce.X509LDAPCertStoreParameters} with the given
 * information of the subject (for all kind of certificates) or issuer (for
 * CRLs), respectively, if a {@link old.org.bouncycastle.x509.X509CertStoreSelector} or
 * {@link old.org.bouncycastle.x509.X509AttributeCertificate} is given with that
 * details.
 * <p/>
 * For the used schemes see:
 * <ul>
 * <li><a href="http://www.ietf.org/rfc/rfc2587.txt">RFC 2587</a>
 * <li><a
 * href="http://www3.ietf.org/proceedings/01mar/I-D/pkix-ldap-schema-01.txt">Internet
 * X.509 Public Key Infrastructure Additional LDAP Schema for PKIs and PMIs</a>
 * </ul>
 */
public class LDAPStoreHelper
{

    // TODO: cache results

    private X509LDAPCertStoreParameters params;

    public LDAPStoreHelper(X509LDAPCertStoreParameters params)
    {
        this.params = params;
    }

    /**
     * Initial Context Factory.
     */
    private static String LDAP_PROVIDER = "com.sun.jndi.ldap.LdapCtxFactory";

    /**
     * Processing referrals..
     */
    private static String REFERRALS_IGNORE = "ignore";

    /**
     * Security level to be used for LDAP connections.
     */
    private static final String SEARCH_SECURITY_LEVEL = "none";

    /**
     * Package Prefix for loading URL context factories.
     */
    private static final String URL_CONTEXT_PREFIX = "com.sun.jndi.url";

    private DirContext connectLDAP() throws NamingException
    {
        Properties props = new Properties();
        props.setProperty(Context.INITIAL_CONTEXT_FACTORY, LDAP_PROVIDER);
        props.setProperty(Context.BATCHSIZE, "0");

        props.setProperty(Context.PROVIDER_URL, params.getLdapURL());
        props.setProperty(Context.URL_PKG_PREFIXES, URL_CONTEXT_PREFIX);
        props.setProperty(Context.REFERRAL, REFERRALS_IGNORE);
        props.setProperty(Context.SECURITY_AUTHENTICATION,
            SEARCH_SECURITY_LEVEL);

        DirContext ctx = new InitialDirContext(props);
        return ctx;
    }

    private String parseDN(String subject, String dNAttributeName)
    {
        String temp = subject;
        int begin = temp.toLowerCase().indexOf(
            dNAttributeName.toLowerCase() + "=");
        if (begin == -1)
        {
            return "";
        }
        temp = temp.substring(begin + dNAttributeName.length());
        int end = temp.indexOf(',');
        if (end == -1)
        {
            end = temp.length();
        }
        while (temp.charAt(end - 1) == '\\')
        {
            end = temp.indexOf(',', end + 1);
            if (end == -1)
            {
                end = temp.length();
            }
        }
        temp = temp.substring(0, end);
        begin = temp.indexOf('=');
        temp = temp.substring(begin + 1);
        if (temp.charAt(0) == ' ')
        {
            temp = temp.substring(1);
        }
        if (temp.startsWith("\""))
        {
            temp = temp.substring(1);
        }
        if (temp.endsWith("\""))
        {
            temp = temp.substring(0, temp.length() - 1);
        }
        return temp;
    }

    private Set createCerts(List list, X509CertStoreSelector xselector)
        throws StoreException
    {
        Set certSet = new HashSet();

        Iterator it = list.iterator();
        X509CertParser parser = new X509CertParser();
        while (it.hasNext())
        {
            try
            {
                parser.engineInit(new ByteArrayInputStream((byte[])it
                    .next()));
                X509Certificate cert = (X509Certificate)parser
                    .engineRead();
                if (xselector.match((Object)cert))
                {
                    certSet.add(cert);
                }

            }
            catch (Exception e)
            {

            }
        }

        return certSet;
    }

    /**
     * Can use the subject and serial and the subject and serialNumber of the
     * certificate of the given of the X509CertStoreSelector. If a certificate
     * for checking is given this has higher precedence.
     *
     * @param xselector             The selector with the search criteria.
     * @param attrs                 Attributes which contain the certificates in the LDAP
     *                              directory.
     * @param attrNames             Attribute names in teh LDAP directory which correspond to the
     *                              subjectAttributeNames.
     * @param subjectAttributeNames Subject attribute names (like "CN", "O", "OU") to use to
     *                              search in the LDAP directory
     * @return A list of found DER encoded certificates.
     * @throws StoreException if an error occurs while searching.
     */
    private List certSubjectSerialSearch(X509CertStoreSelector xselector,
                                         String[] attrs, String attrNames[], String subjectAttributeNames[])
        throws StoreException
    {
        // TODO: support also subjectAltNames?
        List list = new ArrayList();

        String subject = null;
        String serial = null;

        subject = getSubjectAsString(xselector);

        if (xselector.getSerialNumber() != null)
        {
            serial = xselector.getSerialNumber().toString();
        }
        if (xselector.getCertificate() != null)
        {
            subject = xselector.getCertificate().getSubjectX500Principal().getName("RFC1779");
            serial = xselector.getCertificate().getSerialNumber().toString();
        }

        String attrValue = null;
        if (subject != null)
        {
            for (int i = 0; i < subjectAttributeNames.length; i++)
            {
                attrValue = parseDN(subject, subjectAttributeNames[i]);
                list
                    .addAll(search(attrNames, "*" + attrValue + "*",
                        attrs));
            }
        }
        if (serial != null && params.getSearchForSerialNumberIn() != null)
        {
            attrValue = serial;
            list.addAll(search(
                splitString(params.getSearchForSerialNumberIn()),
                                                  attrValue, attrs));
        }
        if (serial == null && subject == null)
        {
            list.addAll(search(attrNames, "*", attrs));
        }

        return list;
    }



    /**
     * Can use the subject of the forward certificate of the set certificate
     * pair or the subject of the forward
     * {@link old.org.bouncycastle.x509.X509CertStoreSelector} of the given
     * selector.
     *
     * @param xselector             The selector with the search criteria.
     * @param attrs                 Attributes which contain the attribute certificates in the
     *                              LDAP directory.
     * @param attrNames             Attribute names in the LDAP directory which correspond to the
     *                              subjectAttributeNames.
     * @param subjectAttributeNames Subject attribute names (like "CN", "O", "OU") to use to
     *                              search in the LDAP directory
     * @return A list of found DER encoded certificate pairs.
     * @throws StoreException if an error occurs while searching.
     */
    private List crossCertificatePairSubjectSearch(
        X509CertPairStoreSelector xselector, String[] attrs,
        String attrNames[], String subjectAttributeNames[])
        throws StoreException
    {
        List list = new ArrayList();

        // search for subject
        String subject = null;

        if (xselector.getForwardSelector() != null)
        {
            subject = getSubjectAsString(xselector.getForwardSelector());
        }
        if (xselector.getCertPair() != null)
        {
            if (xselector.getCertPair().getForward() != null)
            {
                subject = xselector.getCertPair().getForward()
                    .getSubjectX500Principal().getName("RFC1779");
            }
        }
        String attrValue = null;
        if (subject != null)
        {
            for (int i = 0; i < subjectAttributeNames.length; i++)
            {
                attrValue = parseDN(subject, subjectAttributeNames[i]);
                list
                    .addAll(search(attrNames, "*" + attrValue + "*",
                        attrs));
            }
        }
        if (subject == null)
        {
            list.addAll(search(attrNames, "*", attrs));
        }

        return list;
    }

    /**
     * Can use the entityName of the holder of the attribute certificate, the
     * serialNumber of attribute certificate and the serialNumber of the
     * associated certificate of the given of the X509AttributeCertSelector.
     *
     * @param xselector             The selector with the search criteria.
     * @param attrs                 Attributes which contain the attribute certificates in the
     *                              LDAP directory.
     * @param attrNames             Attribute names in the LDAP directory which correspond to the
     *                              subjectAttributeNames.
     * @param subjectAttributeNames Subject attribute names (like "CN", "O", "OU") to use to
     *                              search in the LDAP directory
     * @return A list of found DER encoded attribute certificates.
     * @throws StoreException if an error occurs while searching.
     */
    private List attrCertSubjectSerialSearch(
        X509AttributeCertStoreSelector xselector, String[] attrs,
        String attrNames[], String subjectAttributeNames[])
        throws StoreException
    {
        List list = new ArrayList();

        // search for serialNumber of associated cert,
        // serialNumber of the attribute certificate or DN in the entityName
        // of the holder

        String subject = null;
        String serial = null;

        Collection serials = new HashSet();
        Principal principals[] = null;
        if (xselector.getHolder() != null)
        {
            // serialNumber of associated cert
            if (xselector.getHolder().getSerialNumber() != null)
            {
                serials.add(xselector.getHolder().getSerialNumber()
                    .toString());
            }
            // DN in the entityName of the holder
            if (xselector.getHolder().getEntityNames() != null)
            {
                principals = xselector.getHolder().getEntityNames();
            }
        }

        if (xselector.getAttributeCert() != null)
        {
            if (xselector.getAttributeCert().getHolder().getEntityNames() != null)
            {
                principals = xselector.getAttributeCert().getHolder()
                    .getEntityNames();
            }
            // serialNumber of the attribute certificate
            serials.add(xselector.getAttributeCert().getSerialNumber()
                .toString());
        }
        if (principals != null)
        {
            // only first should be relevant
            if (principals[0] instanceof X500Principal)
            {
                subject = ((X500Principal)principals[0])
                    .getName("RFC1779");
            }
            else
            {
                // strange ...
                subject = principals[0].getName();
            }
        }
        if (xselector.getSerialNumber() != null)
        {
            serials.add(xselector.getSerialNumber().toString());
        }

        String attrValue = null;
        if (subject != null)
        {
            for (int i = 0; i < subjectAttributeNames.length; i++)
            {
                attrValue = parseDN(subject, subjectAttributeNames[i]);
                list
                    .addAll(search(attrNames, "*" + attrValue + "*",
                        attrs));
            }
        }
        if (serials.size() > 0
            && params.getSearchForSerialNumberIn() != null)
        {
            Iterator it = serials.iterator();
            while (it.hasNext())
            {
                serial = (String)it.next();
                list.addAll(search(splitString(params.getSearchForSerialNumberIn()), serial, attrs));
            }
        }
        if (serials.size() == 0 && subject == null)
        {
            list.addAll(search(attrNames, "*", attrs));
        }

        return list;
    }

    /**
     * Can use the issuer of the given of the X509CRLStoreSelector.
     *
     * @param xselector            The selector with the search criteria.
     * @param attrs                Attributes which contain the attribute certificates in the
     *                             LDAP directory.
     * @param attrNames            Attribute names in the LDAP directory which correspond to the
     *                             subjectAttributeNames.
     * @param issuerAttributeNames Issuer attribute names (like "CN", "O", "OU") to use to search
     *                             in the LDAP directory
     * @return A list of found DER encoded CRLs.
     * @throws StoreException if an error occurs while searching.
     */
    private List cRLIssuerSearch(X509CRLStoreSelector xselector,
                                 String[] attrs, String attrNames[], String issuerAttributeNames[])
        throws StoreException
    {
        List list = new ArrayList();

        String issuer = null;
        Collection issuers = new HashSet();
        if (xselector.getIssuers() != null)
        {
            issuers.addAll(xselector.getIssuers());
        }
        if (xselector.getCertificateChecking() != null)
        {
            issuers.add(getCertificateIssuer(xselector.getCertificateChecking()));
        }
        if (xselector.getAttrCertificateChecking() != null)
        {
            Principal principals[] = xselector.getAttrCertificateChecking().getIssuer().getPrincipals();
            for (int i=0; i<principals.length; i++)
            {
                if (principals[i] instanceof X500Principal)
                {
                    issuers.add(principals[i]);        
                }
            }
        }
        Iterator it = issuers.iterator();
        while (it.hasNext())
        {
            issuer = ((X500Principal)it.next()).getName("RFC1779");
            String attrValue = null;

            for (int i = 0; i < issuerAttributeNames.length; i++)
            {
                attrValue = parseDN(issuer, issuerAttributeNames[i]);
                list
                    .addAll(search(attrNames, "*" + attrValue + "*",
                        attrs));
            }
        }
        if (issuer == null)
        {
            list.addAll(search(attrNames, "*", attrs));
        }

        return list;
    }

    /**
     * Returns a <code>List</code> of encodings of the certificates, attribute
     * certificates, CRL or certificate pairs.
     *
     * @param attributeNames The attribute names to look for in the LDAP.
     * @param attributeValue The value the attribute name must have.
     * @param attrs          The attributes in the LDAP which hold the certificate,
     *                       attribute certificate, certificate pair or CRL in a found
     *                       entry.
     * @return A <code>List</code> of byte arrays with the encodings.
     * @throws StoreException if an error occurs getting the results from the LDAP
     *                        directory.
     */
    private List search(String attributeNames[], String attributeValue,
                        String[] attrs) throws StoreException
    {
        String filter = null;
        if (attributeNames == null)
        {
            filter = null;
        }
        else
        {
            filter = "";
            if (attributeValue.equals("**"))
            {
                attributeValue = "*";
            }
            for (int i = 0; i < attributeNames.length; i++)
            {
                filter += "(" + attributeNames[i] + "=" + attributeValue + ")";
            }
            filter = "(|" + filter + ")";
        }
        String filter2 = "";
        for (int i = 0; i < attrs.length; i++)
        {
            filter2 += "(" + attrs[i] + "=*)";
        }
        filter2 = "(|" + filter2 + ")";

        String filter3 = "(&" + filter + "" + filter2 + ")";
        if (filter == null)
        {
            filter3 = filter2;
        }
        List list;
        list = getFromCache(filter3);
        if (list != null)
        {
            return list;
        }
        DirContext ctx = null;
        list = new ArrayList();
        try
        {

            ctx = connectLDAP();

            SearchControls constraints = new SearchControls();
            constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);
            constraints.setCountLimit(0);
            constraints.setReturningAttributes(attrs);
            NamingEnumeration results = ctx.search(params.getBaseDN(), filter3,
                constraints);
            while (results.hasMoreElements())
            {
                SearchResult sr = (SearchResult)results.next();
                NamingEnumeration enumeration = ((Attribute)(sr
                    .getAttributes().getAll().next())).getAll();
                while (enumeration.hasMore())
                {
                    list.add(enumeration.next());
                }
            }
            addToCache(filter3, list);
        }
        catch (NamingException e)
        {
            // skip exception, unfortunately if an attribute type is not
            // supported an exception is thrown

        }
        finally
        {
            try
            {
                if (null != ctx)
                {
                    ctx.close();
                }
            }
            catch (Exception e)
            {
            }
        }
        return list;
    }

    private Set createCRLs(List list, X509CRLStoreSelector xselector)
        throws StoreException
    {
        Set crlSet = new HashSet();

        X509CRLParser parser = new X509CRLParser();
        Iterator it = list.iterator();
        while (it.hasNext())
        {
            try
            {
                parser.engineInit(new ByteArrayInputStream((byte[])it
                    .next()));
                X509CRL crl = (X509CRL)parser.engineRead();
                if (xselector.match((Object)crl))
                {
                    crlSet.add(crl);
                }
            }
            catch (StreamParsingException e)
            {

            }
        }

        return crlSet;
    }

    private Set createCrossCertificatePairs(List list,
                                            X509CertPairStoreSelector xselector) throws StoreException
    {
        Set certPairSet = new HashSet();

        int i = 0;
        while (i < list.size())
        {
            X509CertificatePair pair;
            try
            {
                // first try to decode it as certificate pair
                try
                {
                    X509CertPairParser parser = new X509CertPairParser();
                    parser.engineInit(new ByteArrayInputStream(
                        (byte[])list.get(i)));
                    pair = (X509CertificatePair)parser.engineRead();
                }
                catch (StreamParsingException e)
                {
                    // now try it to construct it the forward and reverse
                    // certificate
                    byte[] forward = (byte[])list.get(i);
                    byte[] reverse = (byte[])list.get(i + 1);
                    pair = new X509CertificatePair(new CertificatePair(
                        X509CertificateStructure
                            .getInstance(new ASN1InputStream(
                            forward).readObject()),
                        X509CertificateStructure
                            .getInstance(new ASN1InputStream(
                            reverse).readObject())));
                    i++;
                }
                if (xselector.match((Object)pair))
                {
                    certPairSet.add(pair);
                }
            }
            catch (CertificateParsingException e)
            {
                // try next
            }
            catch (IOException e)
            {
                // try next
            }
            i++;
        }

        return certPairSet;
    }

    private Set createAttributeCertificates(List list,
                                            X509AttributeCertStoreSelector xselector) throws StoreException
    {
        Set certSet = new HashSet();

        Iterator it = list.iterator();
        X509AttrCertParser parser = new X509AttrCertParser();
        while (it.hasNext())
        {
            try
            {
                parser.engineInit(new ByteArrayInputStream((byte[])it
                    .next()));
                X509AttributeCertificate cert = (X509AttributeCertificate)parser
                    .engineRead();
                if (xselector.match((Object)cert))
                {
                    certSet.add(cert);
                }
            }
            catch (StreamParsingException e)
            {

            }
        }

        return certSet;
    }

    /**
     * Returns the CRLs for issued certificates for other CAs matching the given
     * selector. <br>
     * The authorityRevocationList attribute includes revocation information
     * regarding certificates issued to other CAs.
     *
     * @param selector The CRL selector to use to find the CRLs.
     * @return A possible empty collection with CRLs
     * @throws StoreException
     */
    public Collection getAuthorityRevocationLists(X509CRLStoreSelector selector)
        throws StoreException
    {
        String[] attrs = splitString(params.getAuthorityRevocationListAttribute());
        String attrNames[] = splitString(params
            .getLdapAuthorityRevocationListAttributeName());
        String issuerAttributeNames[] = splitString(params
            .getAuthorityRevocationListIssuerAttributeName());

        List list = cRLIssuerSearch(selector, attrs, attrNames,
            issuerAttributeNames);
        Set resultSet = createCRLs(list, selector);
        if (resultSet.size() == 0)
        {
            X509CRLStoreSelector emptySelector = new X509CRLStoreSelector();
            list = cRLIssuerSearch(emptySelector, attrs, attrNames,
                issuerAttributeNames);

            resultSet.addAll(createCRLs(list, selector));
        }
        return resultSet;
    }

    /**
     * Returns the revocation list for revoked attribute certificates.
     * <p/>
     * The attributeCertificateRevocationList holds a list of attribute
     * certificates that have been revoked.
     *
     * @param selector The CRL selector to use to find the CRLs.
     * @return A possible empty collection with CRLs.
     * @throws StoreException
     */
    public Collection getAttributeCertificateRevocationLists(
        X509CRLStoreSelector selector) throws StoreException
    {
        String[] attrs = splitString(params
            .getAttributeCertificateRevocationListAttribute());
        String attrNames[] = splitString(params
            .getLdapAttributeCertificateRevocationListAttributeName());
        String issuerAttributeNames[] = splitString(params
            .getAttributeCertificateRevocationListIssuerAttributeName());

        List list = cRLIssuerSearch(selector, attrs, attrNames,
            issuerAttributeNames);
        Set resultSet = createCRLs(list, selector);
        if (resultSet.size() == 0)
        {
            X509CRLStoreSelector emptySelector = new X509CRLStoreSelector();
            list = cRLIssuerSearch(emptySelector, attrs, attrNames,
                issuerAttributeNames);

            resultSet.addAll(createCRLs(list, selector));
        }
        return resultSet;
    }

    /**
     * Returns the revocation list for revoked attribute certificates for an
     * attribute authority
     * <p/>
     * The attributeAuthorityList holds a list of AA certificates that have been
     * revoked.
     *
     * @param selector The CRL selector to use to find the CRLs.
     * @return A possible empty collection with CRLs
     * @throws StoreException
     */
    public Collection getAttributeAuthorityRevocationLists(
        X509CRLStoreSelector selector) throws StoreException
    {
        String[] attrs = splitString(params.getAttributeAuthorityRevocationListAttribute());
        String attrNames[] = splitString(params
            .getLdapAttributeAuthorityRevocationListAttributeName());
        String issuerAttributeNames[] = splitString(params
            .getAttributeAuthorityRevocationListIssuerAttributeName());

        List list = cRLIssuerSearch(selector, attrs, attrNames,
            issuerAttributeNames);
        Set resultSet = createCRLs(list, selector);
        if (resultSet.size() == 0)
        {
            X509CRLStoreSelector emptySelector = new X509CRLStoreSelector();
            list = cRLIssuerSearch(emptySelector, attrs, attrNames,
                issuerAttributeNames);

            resultSet.addAll(createCRLs(list, selector));
        }
        return resultSet;
    }

    /**
     * Returns cross certificate pairs.
     *
     * @param selector The selector to use to find the cross certificates.
     * @return A possible empty collection with {@link X509CertificatePair}s
     * @throws StoreException
     */
    public Collection getCrossCertificatePairs(
        X509CertPairStoreSelector selector) throws StoreException
    {
        String[] attrs = splitString(params.getCrossCertificateAttribute());
        String attrNames[] = splitString(params.getLdapCrossCertificateAttributeName());
        String subjectAttributeNames[] = splitString(params
            .getCrossCertificateSubjectAttributeName());
        List list = crossCertificatePairSubjectSearch(selector, attrs,
            attrNames, subjectAttributeNames);
        Set resultSet = createCrossCertificatePairs(list, selector);
        if (resultSet.size() == 0)
        {
            X509CertStoreSelector emptyCertselector = new X509CertStoreSelector();
            X509CertPairStoreSelector emptySelector = new X509CertPairStoreSelector();

            emptySelector.setForwardSelector(emptyCertselector);
            emptySelector.setReverseSelector(emptyCertselector);
            list = crossCertificatePairSubjectSearch(emptySelector, attrs,
                attrNames, subjectAttributeNames);
            resultSet.addAll(createCrossCertificatePairs(list, selector));
        }
        return resultSet;
    }

    /**
     * Returns end certificates.
     * <p/>
     * The attributeDescriptorCertificate is self signed by a source of
     * authority and holds a description of the privilege and its delegation
     * rules.
     *
     * @param selector The selector to find the certificates.
     * @return A possible empty collection with certificates.
     * @throws StoreException
     */
    public Collection getUserCertificates(X509CertStoreSelector selector)
        throws StoreException
    {
        String[] attrs = splitString(params.getUserCertificateAttribute());
        String attrNames[] = splitString(params.getLdapUserCertificateAttributeName());
        String subjectAttributeNames[] = splitString(params
            .getUserCertificateSubjectAttributeName());

        List list = certSubjectSerialSearch(selector, attrs, attrNames,
            subjectAttributeNames);
        Set resultSet = createCerts(list, selector);
        if (resultSet.size() == 0)
        {
            X509CertStoreSelector emptySelector = new X509CertStoreSelector();
            list = certSubjectSerialSearch(emptySelector, attrs, attrNames,
                subjectAttributeNames);
            resultSet.addAll(createCerts(list, selector));
        }

        return resultSet;
    }

    /**
     * Returns attribute certificates for an attribute authority
     * <p/>
     * The aAcertificate holds the privileges of an attribute authority.
     *
     * @param selector The selector to find the attribute certificates.
     * @return A possible empty collection with attribute certificates.
     * @throws StoreException
     */
    public Collection getAACertificates(X509AttributeCertStoreSelector selector)
        throws StoreException
    {
        String[] attrs = splitString(params.getAACertificateAttribute());
        String attrNames[] = splitString(params.getLdapAACertificateAttributeName());
        String subjectAttributeNames[] = splitString(params.getAACertificateSubjectAttributeName());

        List list = attrCertSubjectSerialSearch(selector, attrs, attrNames,
            subjectAttributeNames);
        Set resultSet = createAttributeCertificates(list, selector);
        if (resultSet.size() == 0)
        {
            X509AttributeCertStoreSelector emptySelector = new X509AttributeCertStoreSelector();
            list = attrCertSubjectSerialSearch(emptySelector, attrs, attrNames,
                subjectAttributeNames);
            resultSet.addAll(createAttributeCertificates(list, selector));
        }

        return resultSet;
    }

    /**
     * Returns an attribute certificate for an authority
     * <p/>
     * The attributeDescriptorCertificate is self signed by a source of
     * authority and holds a description of the privilege and its delegation
     * rules.
     *
     * @param selector The selector to find the attribute certificates.
     * @return A possible empty collection with attribute certificates.
     * @throws StoreException
     */
    public Collection getAttributeDescriptorCertificates(
        X509AttributeCertStoreSelector selector) throws StoreException
    {
        String[] attrs = splitString(params.getAttributeDescriptorCertificateAttribute());
        String attrNames[] = splitString(params
            .getLdapAttributeDescriptorCertificateAttributeName());
        String subjectAttributeNames[] = splitString(params
            .getAttributeDescriptorCertificateSubjectAttributeName());

        List list = attrCertSubjectSerialSearch(selector, attrs, attrNames,
            subjectAttributeNames);
        Set resultSet = createAttributeCertificates(list, selector);
        if (resultSet.size() == 0)
        {
            X509AttributeCertStoreSelector emptySelector = new X509AttributeCertStoreSelector();
            list = attrCertSubjectSerialSearch(emptySelector, attrs, attrNames,
                subjectAttributeNames);
            resultSet.addAll(createAttributeCertificates(list, selector));
        }

        return resultSet;
    }

    /**
     * Returns CA certificates.
     * <p/>
     * The cACertificate attribute of a CA's directory entry shall be used to
     * store self-issued certificates (if any) and certificates issued to this
     * CA by CAs in the same realm as this CA.
     *
     * @param selector The selector to find the certificates.
     * @return A possible empty collection with certificates.
     * @throws StoreException
     */
    public Collection getCACertificates(X509CertStoreSelector selector)
        throws StoreException
    {
        String[] attrs = splitString(params.getCACertificateAttribute());
        String attrNames[] = splitString(params.getLdapCACertificateAttributeName());
        String subjectAttributeNames[] = splitString(params
            .getCACertificateSubjectAttributeName());
        List list = certSubjectSerialSearch(selector, attrs, attrNames,
            subjectAttributeNames);
        Set resultSet = createCerts(list, selector);
        if (resultSet.size() == 0)
        {
            X509CertStoreSelector emptySelector = new X509CertStoreSelector();
            list = certSubjectSerialSearch(emptySelector, attrs, attrNames,
                subjectAttributeNames);
            resultSet.addAll(createCerts(list, selector));
        }
        return resultSet;
    }

    /**
     * Returns the delta revocation list for revoked certificates.
     *
     * @param selector The CRL selector to use to find the CRLs.
     * @return A possible empty collection with CRLs.
     * @throws StoreException
     */
    public Collection getDeltaCertificateRevocationLists(
        X509CRLStoreSelector selector) throws StoreException
    {
        String[] attrs = splitString(params.getDeltaRevocationListAttribute());
        String attrNames[] = splitString(params.getLdapDeltaRevocationListAttributeName());
        String issuerAttributeNames[] = splitString(params
            .getDeltaRevocationListIssuerAttributeName());
        List list = cRLIssuerSearch(selector, attrs, attrNames,
            issuerAttributeNames);
        Set resultSet = createCRLs(list, selector);
        if (resultSet.size() == 0)
        {
            X509CRLStoreSelector emptySelector = new X509CRLStoreSelector();
            list = cRLIssuerSearch(emptySelector, attrs, attrNames,
                issuerAttributeNames);

            resultSet.addAll(createCRLs(list, selector));
        }
        return resultSet;
    }

    /**
     * Returns an attribute certificate for an user.
     * <p/>
     * The attributeCertificateAttribute holds the privileges of a user
     *
     * @param selector The selector to find the attribute certificates.
     * @return A possible empty collection with attribute certificates.
     * @throws StoreException
     */
    public Collection getAttributeCertificateAttributes(
        X509AttributeCertStoreSelector selector) throws StoreException
    {
        String[] attrs = splitString(params.getAttributeCertificateAttributeAttribute());
        String attrNames[] = splitString(params
            .getLdapAttributeCertificateAttributeAttributeName());
        String subjectAttributeNames[] = splitString(params
            .getAttributeCertificateAttributeSubjectAttributeName());
        List list = attrCertSubjectSerialSearch(selector, attrs, attrNames,
            subjectAttributeNames);
        Set resultSet = createAttributeCertificates(list, selector);
        if (resultSet.size() == 0)
        {
            X509AttributeCertStoreSelector emptySelector = new X509AttributeCertStoreSelector();
            list = attrCertSubjectSerialSearch(emptySelector, attrs, attrNames,
                subjectAttributeNames);
            resultSet.addAll(createAttributeCertificates(list, selector));
        }

        return resultSet;
    }

    /**
     * Returns the certificate revocation lists for revoked certificates.
     *
     * @param selector The CRL selector to use to find the CRLs.
     * @return A possible empty collection with CRLs.
     * @throws StoreException
     */
    public Collection getCertificateRevocationLists(
        X509CRLStoreSelector selector) throws StoreException
    {
        String[] attrs = splitString(params.getCertificateRevocationListAttribute());
        String attrNames[] = splitString(params
            .getLdapCertificateRevocationListAttributeName());
        String issuerAttributeNames[] = splitString(params
            .getCertificateRevocationListIssuerAttributeName());
        List list = cRLIssuerSearch(selector, attrs, attrNames,
            issuerAttributeNames);
        Set resultSet = createCRLs(list, selector);
        if (resultSet.size() == 0)
        {
            X509CRLStoreSelector emptySelector = new X509CRLStoreSelector();
            list = cRLIssuerSearch(emptySelector, attrs, attrNames,
                issuerAttributeNames);

            resultSet.addAll(createCRLs(list, selector));
        }
        return resultSet;
    }

    private Map cacheMap = new HashMap(cacheSize);

    private static int cacheSize = 32;

    private static long lifeTime = 60 * 1000;

    private synchronized void addToCache(String searchCriteria, List list)
    {
        Date now = new Date(System.currentTimeMillis());
        List cacheEntry = new ArrayList();
        cacheEntry.add(now);
        cacheEntry.add(list);
        if (cacheMap.containsKey(searchCriteria))
        {
            cacheMap.put(searchCriteria, cacheEntry);
        }
        else
        {
            if (cacheMap.size() >= cacheSize)
            {
                // replace oldest
                Iterator it = cacheMap.entrySet().iterator();
                long oldest = now.getTime();
                Object replace = null;
                while (it.hasNext())
                {
                    Map.Entry entry = (Map.Entry)it.next();
                    long current = ((Date)((List)entry.getValue()).get(0))
                        .getTime();
                    if (current < oldest)
                    {
                        oldest = current;
                        replace = entry.getKey();
                    }
                }
                cacheMap.remove(replace);
            }
            cacheMap.put(searchCriteria, cacheEntry);
        }
    }

    private List getFromCache(String searchCriteria)
    {
        List entry = (List)cacheMap.get(searchCriteria);
        long now = System.currentTimeMillis();
        if (entry != null)
        {
            // too old
            if (((Date)entry.get(0)).getTime() < (now - lifeTime))
            {
                return null;
            }
            return (List)entry.get(1);
        }
        return null;
    }

    /*
     * spilt string based on spaces
     */
    private String[] splitString(String str)
    {
        return str.split("\\s+");
    }

    private String getSubjectAsString(X509CertStoreSelector xselector)
    {
        try
        {
            byte[] encSubject = xselector.getSubjectAsBytes();
            if (encSubject != null)
            {
                return new X500Principal(encSubject).getName("RFC1779");
            }
        }
        catch (IOException e)
        {
            throw new StoreException("exception processing name: " + e.getMessage(), e);
        }
        return null;
    }

    private X500Principal getCertificateIssuer(X509Certificate cert)
    {
        return cert.getIssuerX500Principal();
    }
}
