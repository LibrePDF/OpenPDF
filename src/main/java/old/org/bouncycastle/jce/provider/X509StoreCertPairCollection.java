package old.org.bouncycastle.jce.provider;

import old.org.bouncycastle.util.CollectionStore;
import old.org.bouncycastle.util.Selector;
import old.org.bouncycastle.x509.X509CollectionStoreParameters;
import old.org.bouncycastle.x509.X509StoreParameters;
import old.org.bouncycastle.x509.X509StoreSpi;

import java.util.Collection;

/**
 * This class is a collection based Bouncy Castle
 * {@link old.org.bouncycastle.x509.X509Store} SPI implementation for certificate
 * pairs.
 *
 * @see old.org.bouncycastle.x509.X509Store
 * @see old.org.bouncycastle.x509.X509CertificatePair
 */
public class X509StoreCertPairCollection extends X509StoreSpi
{

    private CollectionStore _store;

    public X509StoreCertPairCollection()
    {
    }

    /**
     * Initializes this store.
     *
     * @param params The {@link X509CollectionStoreParameters}s for this store.
     * @throws IllegalArgumentException if <code>params</code> is no instance of
     *                                  <code>X509CollectionStoreParameters</code>.
     */
    public void engineInit(X509StoreParameters params)
    {
        if (!(params instanceof X509CollectionStoreParameters))
        {
            throw new IllegalArgumentException(
                "Initialization parameters must be an instance of "
                    + X509CollectionStoreParameters.class.getName()
                    + ".");
        }

        _store = new CollectionStore(((X509CollectionStoreParameters)params)
            .getCollection());
    }

    /**
     * Returns a colelction of certificate pairs which match the given
     * <code>selector</code>.
     * <p/>
     * The returned collection contains
     * {@link old.org.bouncycastle.x509.X509CertificatePair}s. The selector must be
     * a {@link old.org.bouncycastle.x509.X509CertPairStoreSelector} to select
     * certificate pairs.
     *
     * @return A collection with matching certificate pairs.
     */
    public Collection engineGetMatches(Selector selector)
    {
        return _store.getMatches(selector);
    }
}
