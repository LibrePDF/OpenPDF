package old.org.bouncycastle.jce.provider;

import old.org.bouncycastle.jce.X509LDAPCertStoreParameters;
import old.org.bouncycastle.util.Selector;
import old.org.bouncycastle.util.StoreException;
import old.org.bouncycastle.x509.X509CertPairStoreSelector;
import old.org.bouncycastle.x509.X509StoreParameters;
import old.org.bouncycastle.x509.X509StoreSpi;
import old.org.bouncycastle.x509.util.LDAPStoreHelper;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * A SPI implementation of Bouncy Castle <code>X509Store</code> for getting
 * cross certificates pairs from an LDAP directory.
 *
 * @see old.org.bouncycastle.x509.X509Store
 */
public class X509StoreLDAPCertPairs extends X509StoreSpi
{

    private LDAPStoreHelper helper;

    public X509StoreLDAPCertPairs()
    {
    }

    /**
     * Initializes this LDAP cross certificate pair store implementation.
     *
     * @param parameters <code>X509LDAPCertStoreParameters</code>.
     * @throws IllegalArgumentException if <code>params</code> is not an instance of
     *                                  <code>X509LDAPCertStoreParameters</code>.
     */
    public void engineInit(X509StoreParameters parameters)
    {
        if (!(parameters instanceof X509LDAPCertStoreParameters))
        {
            throw new IllegalArgumentException(
                "Initialization parameters must be an instance of "
                    + X509LDAPCertStoreParameters.class.getName() + ".");
        }
        helper = new LDAPStoreHelper((X509LDAPCertStoreParameters)parameters);
    }

    /**
     * Returns a collection of matching cross certificate pairs from the LDAP
     * location.
     * <p/>
     * The selector must be a of type <code>X509CertPairStoreSelector</code>.
     * If it is not an empty collection is returned.
     * <p/>
     * <p/>
     * The subject should be a reasonable criteria for a selector.
     *
     * @param selector The selector to use for finding.
     * @return A collection with the matches.
     * @throws StoreException if an exception occurs while searching.
     */
    public Collection engineGetMatches(Selector selector) throws StoreException
    {
        if (!(selector instanceof X509CertPairStoreSelector))
        {
            return Collections.EMPTY_SET;
        }
        X509CertPairStoreSelector xselector = (X509CertPairStoreSelector)selector;
        Set set = new HashSet();
        set.addAll(helper.getCrossCertificatePairs(xselector));
        return set;
    }

}
