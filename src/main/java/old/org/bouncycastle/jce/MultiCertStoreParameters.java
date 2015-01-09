package old.org.bouncycastle.jce;

import java.security.cert.CertStoreParameters;
import java.util.Collection;

public class MultiCertStoreParameters
    implements CertStoreParameters
{
    private Collection certStores;
    private boolean searchAllStores;

    /**
     * Create a parameters object which specifies searching of all the passed in stores.
     *
     * @param certStores CertStores making up the multi CertStore
     */
    public MultiCertStoreParameters(Collection certStores)
    {
        this(certStores, true);
    }

    /**
     * Create a parameters object which can be to used to make a multi store made up
     * of the passed in CertStores. If the searchAllStores parameter is false, any search on
     * the multi-store will terminate as soon as a search query produces a result.
     * 
     * @param certStores CertStores making up the multi CertStore
     * @param searchAllStores true if all CertStores should be searched on request, false if a result
     * should be returned on the first successful CertStore query.
     */
    public MultiCertStoreParameters(Collection certStores, boolean searchAllStores)
    {
        this.certStores = certStores;
        this.searchAllStores = searchAllStores;
    }

    public Collection getCertStores()
    {
        return certStores;
    }

    public boolean getSearchAllStores()
    {
        return searchAllStores;
    }

    public Object clone()
    {
        return this;
    }
}
