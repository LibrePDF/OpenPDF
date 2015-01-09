package old.org.bouncycastle.jce.provider;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.cert.CRL;
import java.security.cert.CRLSelector;
import java.security.cert.CertSelector;
import java.security.cert.CertStoreException;
import java.security.cert.CertStoreParameters;
import java.security.cert.CertStoreSpi;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509CRLSelector;
import java.security.cert.X509CertSelector;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.security.auth.x500.X500Principal;

import old.org.bouncycastle.asn1.ASN1InputStream;
import old.org.bouncycastle.asn1.x509.CertificatePair;
import old.org.bouncycastle.jce.X509LDAPCertStoreParameters;

/**
 * 
 * This is a general purpose implementation to get X.509 certificates and CRLs
 * from a LDAP location.
 * <p>
 * At first a search is performed in the ldap*AttributeNames of the
 * {@link old.org.bouncycastle.jce.X509LDAPCertStoreParameters} with the given
 * information of the subject (for all kind of certificates) or issuer (for
 * CRLs), respectively, if a X509CertSelector is given with that details. For
 * CRLs, CA certificates and cross certificates a coarse search is made only for
 * entries with that content to get more possibly matchign results.
 */
public class X509LDAPCertStoreSpi
    extends CertStoreSpi
{
    private X509LDAPCertStoreParameters params;

    public X509LDAPCertStoreSpi(CertStoreParameters params)
        throws InvalidAlgorithmParameterException
    {
        super(params);

        if (!(params instanceof X509LDAPCertStoreParameters))
        {
            throw new InvalidAlgorithmParameterException(
                X509LDAPCertStoreSpi.class.getName() + ": parameter must be a " + X509LDAPCertStoreParameters.class.getName() + " object\n"
                    + params.toString());
        }

        this.params = (X509LDAPCertStoreParameters)params;
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

    private String parseDN(String subject, String subjectAttributeName)
    {
        String temp = subject;
        int begin = temp.toLowerCase().indexOf(
            subjectAttributeName.toLowerCase());
        temp = temp.substring(begin + subjectAttributeName.length());
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

    public Collection engineGetCertificates(CertSelector selector)
        throws CertStoreException
    {
        if (!(selector instanceof X509CertSelector))
        {
            throw new CertStoreException("selector is not a X509CertSelector");
        }
        X509CertSelector xselector = (X509CertSelector)selector;

        Set certSet = new HashSet();

        Set set = getEndCertificates(xselector);
        set.addAll(getCACertificates(xselector));
        set.addAll(getCrossCertificates(xselector));

        Iterator it = set.iterator();

        try
        {
            CertificateFactory cf = CertificateFactory.getInstance("X.509",
                BouncyCastleProvider.PROVIDER_NAME);
            while (it.hasNext())
            {
                byte[] bytes = (byte[])it.next();
                if (bytes == null || bytes.length == 0)
                {
                    continue;
                }

                List bytesList = new ArrayList();
                bytesList.add(bytes);

                try
                {
                    CertificatePair pair = CertificatePair
                        .getInstance(new ASN1InputStream(bytes)
                            .readObject());
                    bytesList.clear();
                    if (pair.getForward() != null)
                    {
                        bytesList.add(pair.getForward().getEncoded());
                    }
                    if (pair.getReverse() != null)
                    {
                        bytesList.add(pair.getReverse().getEncoded());
                    }
                }
                catch (IOException e)
                {

                }
                catch (IllegalArgumentException e)
                {

                }
                for (Iterator it2 = bytesList.iterator(); it2.hasNext();)
                {
                    ByteArrayInputStream bIn = new ByteArrayInputStream(
                        (byte[])it2.next());
                    try
                    {
                        Certificate cert = cf.generateCertificate(bIn);
                        // System.out.println(((X509Certificate)
                        // cert).getSubjectX500Principal());
                        if (xselector.match(cert))
                        {
                            certSet.add(cert);
                        }
                    }
                    catch (Exception e)
                    {

                    }
                }
            }
        }
        catch (Exception e)
        {
            throw new CertStoreException(
                "certificate cannot be constructed from LDAP result: " + e);
        }

        return certSet;
    }

    private Set certSubjectSerialSearch(X509CertSelector xselector,
                                        String[] attrs, String attrName, String subjectAttributeName)
        throws CertStoreException
    {
        Set set = new HashSet();
        try
        {
            if (xselector.getSubjectAsBytes() != null
                || xselector.getSubjectAsString() != null
                || xselector.getCertificate() != null)
            {
                String subject = null;
                String serial = null;
                if (xselector.getCertificate() != null)
                {
                    subject = xselector.getCertificate()
                        .getSubjectX500Principal().getName("RFC1779");
                    serial = xselector.getCertificate().getSerialNumber()
                        .toString();
                }
                else
                {
                    if (xselector.getSubjectAsBytes() != null)
                    {
                        subject = new X500Principal(xselector
                            .getSubjectAsBytes()).getName("RFC1779");
                    }
                    else
                    {
                        subject = xselector.getSubjectAsString();
                    }
                }
                String attrValue = parseDN(subject, subjectAttributeName);
                set.addAll(search(attrName, "*" + attrValue + "*", attrs));
                if (serial != null
                    && params.getSearchForSerialNumberIn() != null)
                {
                    attrValue = serial;
                    attrName = params.getSearchForSerialNumberIn();
                    set.addAll(search(attrName, "*" + attrValue + "*", attrs));
                }
            }
            else
            {
                set.addAll(search(attrName, "*", attrs));
            }
        }
        catch (IOException e)
        {
            throw new CertStoreException("exception processing selector: " + e);
        }

        return set;
    }

    private Set getEndCertificates(X509CertSelector xselector)
        throws CertStoreException
    {
        String[] attrs = {params.getUserCertificateAttribute()};
        String attrName = params.getLdapUserCertificateAttributeName();
        String subjectAttributeName = params.getUserCertificateSubjectAttributeName();

        Set set = certSubjectSerialSearch(xselector, attrs, attrName,
            subjectAttributeName);
        return set;
    }

    private Set getCACertificates(X509CertSelector xselector)
        throws CertStoreException
    {
        String[] attrs = {params.getCACertificateAttribute()};
        String attrName = params.getLdapCACertificateAttributeName();
        String subjectAttributeName = params
            .getCACertificateSubjectAttributeName();
        Set set = certSubjectSerialSearch(xselector, attrs, attrName,
            subjectAttributeName);

        if (set.isEmpty())
        {
            set.addAll(search(null, "*", attrs));
        }

        return set;
    }

    private Set getCrossCertificates(X509CertSelector xselector)
        throws CertStoreException
    {
        String[] attrs = {params.getCrossCertificateAttribute()};
        String attrName = params.getLdapCrossCertificateAttributeName();
        String subjectAttributeName = params
            .getCrossCertificateSubjectAttributeName();
        Set set = certSubjectSerialSearch(xselector, attrs, attrName,
            subjectAttributeName);

        if (set.isEmpty())
        {
            set.addAll(search(null, "*", attrs));
        }

        return set;
    }

    public Collection engineGetCRLs(CRLSelector selector)
        throws CertStoreException
    {
        String[] attrs = {params.getCertificateRevocationListAttribute()};
        if (!(selector instanceof X509CRLSelector))
        {
            throw new CertStoreException("selector is not a X509CRLSelector");
        }
        X509CRLSelector xselector = (X509CRLSelector)selector;

        Set crlSet = new HashSet();

        String attrName = params.getLdapCertificateRevocationListAttributeName();
        Set set = new HashSet();

        if (xselector.getIssuerNames() != null)
        {
            for (Iterator it = xselector.getIssuerNames().iterator(); it
                .hasNext();)
            {
                Object o = it.next();
                String attrValue = null;
                if (o instanceof String)
                {
                    String issuerAttributeName = params
                        .getCertificateRevocationListIssuerAttributeName();
                    attrValue = parseDN((String)o, issuerAttributeName);
                }
                else
                {
                    String issuerAttributeName = params
                        .getCertificateRevocationListIssuerAttributeName();
                    attrValue = parseDN(new X500Principal((byte[])o)
                        .getName("RFC1779"), issuerAttributeName);
                }
                set.addAll(search(attrName, "*" + attrValue + "*", attrs));
            }
        }
        else
        {
            set.addAll(search(attrName, "*", attrs));
        }
        set.addAll(search(null, "*", attrs));
        Iterator it = set.iterator();

        try
        {
            CertificateFactory cf = CertificateFactory.getInstance("X.509",
                BouncyCastleProvider.PROVIDER_NAME);
            while (it.hasNext())
            {
                CRL crl = cf.generateCRL(new ByteArrayInputStream((byte[])it
                    .next()));
                if (xselector.match(crl))
                {
                    crlSet.add(crl);
                }
            }
        }
        catch (Exception e)
        {
            throw new CertStoreException(
                "CRL cannot be constructed from LDAP result " + e);
        }

        return crlSet;
    }

    /**
     * Returns a Set of byte arrays with the certificate or CRL encodings.
     *
     * @param attributeName  The attribute name to look for in the LDAP.
     * @param attributeValue The value the attribute name must have.
     * @param attrs          The attributes in the LDAP which hold the certificate,
     *                       certificate pair or CRL in a found entry.
     * @return Set of byte arrays with the certificate encodings.
     */
    private Set search(String attributeName, String attributeValue,
                       String[] attrs) throws CertStoreException
    {
        String filter = attributeName + "=" + attributeValue;
        if (attributeName == null)
        {
            filter = null;
        }
        DirContext ctx = null;
        Set set = new HashSet();
        try
        {

            ctx = connectLDAP();

            SearchControls constraints = new SearchControls();
            constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);
            constraints.setCountLimit(0);
            for (int i = 0; i < attrs.length; i++)
            {
                String temp[] = new String[1];
                temp[0] = attrs[i];
                constraints.setReturningAttributes(temp);

                String filter2 = "(&(" + filter + ")(" + temp[0] + "=*))";
                if (filter == null)
                {
                    filter2 = "(" + temp[0] + "=*)";
                }
                NamingEnumeration results = ctx.search(params.getBaseDN(),
                    filter2, constraints);
                while (results.hasMoreElements())
                {
                    SearchResult sr = (SearchResult)results.next();
                    // should only be one attribute in the attribute set with
                    // one
                    // attribute value as byte array
                    NamingEnumeration enumeration = ((Attribute)(sr
                        .getAttributes().getAll().next())).getAll();
                    while (enumeration.hasMore())
                    {
                        Object o = enumeration.next();
                        set.add(o);
                    }
                }
            }
        }
        catch (Exception e)
        {
            throw new CertStoreException(
                "Error getting results from LDAP directory " + e);

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
        return set;
    }

}
