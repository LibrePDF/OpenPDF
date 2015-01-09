package old.org.bouncycastle.x509;

import old.org.bouncycastle.util.Selector;
import old.org.bouncycastle.util.Store;

import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.util.Collection;

public class X509Store
    implements Store
{
    public static X509Store getInstance(String type, X509StoreParameters parameters)
        throws NoSuchStoreException
    {
        try
        {
            X509Util.Implementation impl = X509Util.getImplementation("X509Store", type);

            return createStore(impl, parameters);
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new NoSuchStoreException(e.getMessage());
        }
    }

    public static X509Store getInstance(String type, X509StoreParameters parameters, String provider)
        throws NoSuchStoreException, NoSuchProviderException
    {
        return getInstance(type, parameters, X509Util.getProvider(provider));
    }

    public static X509Store getInstance(String type, X509StoreParameters parameters, Provider provider)
        throws NoSuchStoreException
    {
        try
        {
            X509Util.Implementation impl = X509Util.getImplementation("X509Store", type, provider);

            return createStore(impl, parameters);
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new NoSuchStoreException(e.getMessage());
        }
    }

    private static X509Store createStore(X509Util.Implementation impl, X509StoreParameters parameters)
    {
        X509StoreSpi spi = (X509StoreSpi)impl.getEngine();

        spi.engineInit(parameters);

        return new X509Store(impl.getProvider(), spi);
    }

    private Provider     _provider;
    private X509StoreSpi _spi;

    private X509Store(
        Provider provider,
        X509StoreSpi spi)
    {
        _provider = provider;
        _spi = spi;
    }

    public Provider getProvider()
    {
       return _provider;
    }

    public Collection getMatches(Selector selector)
    {
        return _spi.engineGetMatches(selector);
    }
}
