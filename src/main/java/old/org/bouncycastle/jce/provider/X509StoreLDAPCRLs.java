package old.org.bouncycastle.jce.provider;

import old.org.bouncycastle.jce.X509LDAPCertStoreParameters;
import old.org.bouncycastle.util.Selector;
import old.org.bouncycastle.util.StoreException;
import old.org.bouncycastle.x509.X509CRLStoreSelector;
import old.org.bouncycastle.x509.X509StoreParameters;
import old.org.bouncycastle.x509.X509StoreSpi;
import old.org.bouncycastle.x509.util.LDAPStoreHelper;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * A SPI implementation of Bouncy Castle <code>X509Store</code> for getting
 * certificate revocation lists from an LDAP directory.
 *
 * @see old.org.bouncycastle.x509.X509Store
 */
public class X509StoreLDAPCRLs extends X509StoreSpi
{

    private LDAPStoreHelper helper;

    public X509StoreLDAPCRLs()
    {
    }

    /**
     * Initializes this LDAP CRL store implementation.
     *
     * @param params <code>X509LDAPCertStoreParameters</code>.
     * @throws IllegalArgumentException if <code>params</code> is not an instance of
     *                                  <code>X509LDAPCertStoreParameters</code>.
     */
    public void engineInit(X509StoreParameters params)
    {
        if (!(params instanceof X509LDAPCertStoreParameters))
        {
            throw new IllegalArgumentException(
                "Initialization parameters must be an instance of "
                    + X509LDAPCertStoreParameters.class.getName() + ".");
        }
        helper = new LDAPStoreHelper((X509LDAPCertStoreParameters)params);
    }

    /**
     * Returns a collection of matching CRLs from the LDAP location.
     * <p/>
     * The selector must be a of type <code>X509CRLStoreSelector</code>. If
     * it is not an empty collection is returned.
     * <p/>
     * The issuer should be a reasonable criteria for a selector.
     *
     * @param selector The selector to use for finding.
     * @return A collection with the matches.
     * @throws StoreException if an exception occurs while searching.
     */
    public Collection engineGetMatches(Selector selector) throws StoreException
    {
        if (!(selector instanceof X509CRLStoreSelector))
        {
            return Collections.EMPTY_SET;
        }
        X509CRLStoreSelector xselector = (X509CRLStoreSelector)selector;
        Set set = new HashSet();
        // test only delta CRLs should be selected
        if (xselector.isDeltaCRLIndicatorEnabled())
        {
            set.addAll(helper.getDeltaCertificateRevocationLists(xselector));
        }
        // nothing specified
        else
        {
            set.addAll(helper.getDeltaCertificateRevocationLists(xselector));
            set.addAll(helper.getAttributeAuthorityRevocationLists(xselector));
            set
                .addAll(helper
                    .getAttributeCertificateRevocationLists(xselector));
            set.addAll(helper.getAuthorityRevocationLists(xselector));
            set.addAll(helper.getCertificateRevocationLists(xselector));
        }
        return set;
    }
}
