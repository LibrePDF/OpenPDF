package old.org.bouncycastle.x509;

import old.org.bouncycastle.x509.util.StreamParser;
import old.org.bouncycastle.x509.util.StreamParsingException;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.util.Collection;

/**
 *
 * This class allows access to different implementations for reading X.509
 * objects from streams.
 * <p>
 * A X509StreamParser is used to read a collection of objects or a single object
 * of a certain X.509 object structure. E.g. one X509StreamParser can read
 * certificates, another one CRLs, certification paths, attribute certificates
 * and so on. The kind of object structure is specified with the
 * <code>algorithm</code> parameter to the <code>getInstance</code> methods.
 * <p>
 * Implementations must implement the
 * {@link old.org.bouncycastle.x509.X509StreamParserSpi}.
 */
public class X509StreamParser
    implements StreamParser
{
    /**
     * Generates a StreamParser object that implements the specified type. If
     * the default provider package provides an implementation of the requested
     * type, an instance of StreamParser containing that implementation is
     * returned. If the type is not available in the default package, other
     * packages are searched.
     *
     * @param type
     *            The name of the requested X.509 object type.
     * @return a StreamParser object for the specified type.
     *
     * @exception NoSuchParserException
     *                if the requested type is not available in the default
     *                provider package or any of the other provider packages
     *                that were searched.
     */
    public static X509StreamParser getInstance(String type)
        throws NoSuchParserException
    {
        try
        {
            X509Util.Implementation impl = X509Util.getImplementation("X509StreamParser", type);

            return createParser(impl);
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new NoSuchParserException(e.getMessage());
        }
    }

    /**
     * Generates a X509StreamParser object for the specified type from the
     * specified provider.
     *
     * @param type
     *            the name of the requested X.509 object type.
     * @param provider
     *            the name of the provider.
     *
     * @return a X509StreamParser object for the specified type.
     *
     * @exception NoSuchParserException
     *                if the type is not available from the specified provider.
     *
     * @exception NoSuchProviderException
     *                if the provider can not be found.
     *
     * @see Provider
     */
    public static X509StreamParser getInstance(String type, String provider)
        throws NoSuchParserException, NoSuchProviderException
    {
        return getInstance(type, X509Util.getProvider(provider));
    }

    /**
     * Generates a X509StreamParser object for the specified type from the
     * specified provider.
     *
     * @param type
     *            the name of the requested X.509 object type.
     * @param provider
     *            the Provider to use.
     *
     * @return a X509StreamParser object for the specified type.
     *
     * @exception NoSuchParserException
     *                if the type is not available from the specified provider.
     *
     * @see Provider
     */
    public static X509StreamParser getInstance(String type, Provider provider)
        throws NoSuchParserException
    {
        try
        {
            X509Util.Implementation impl = X509Util.getImplementation("X509StreamParser", type, provider);

            return createParser(impl);
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new NoSuchParserException(e.getMessage());
        }
    }

    private static X509StreamParser createParser(X509Util.Implementation impl)
    {
        X509StreamParserSpi spi = (X509StreamParserSpi)impl.getEngine();

        return new X509StreamParser(impl.getProvider(), spi);
    }

    private Provider            _provider;
    private X509StreamParserSpi _spi;

    private X509StreamParser(
        Provider provider,
        X509StreamParserSpi spi)
    {
        _provider = provider;
        _spi = spi;
    }

    public Provider getProvider()
    {
        return _provider;
    }

    public void init(InputStream stream)
    {
        _spi.engineInit(stream);
    }

    public void init(byte[] data)
    {
        _spi.engineInit(new ByteArrayInputStream(data));
    }

    public Object read()
        throws StreamParsingException
    {
        return _spi.engineRead();
    }

    public Collection readAll()
        throws StreamParsingException
    {
        return _spi.engineReadAll();
    }
}
