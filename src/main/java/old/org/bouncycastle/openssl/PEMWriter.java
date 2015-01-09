package old.org.bouncycastle.openssl;

import java.io.IOException;
import java.io.Writer;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;

import old.org.bouncycastle.util.io.pem.PemGenerationException;
import old.org.bouncycastle.util.io.pem.PemObjectGenerator;
import old.org.bouncycastle.util.io.pem.PemWriter;

/**
 * General purpose writer for OpenSSL PEM objects.
 */
public class PEMWriter
    extends PemWriter
{
    private String provider;

    /**
     * Base constructor.
     * 
     * @param out output stream to use.
     */
    public PEMWriter(Writer out)
    {
        this(out, "BC");
    }

    public PEMWriter(
        Writer  out,
        String  provider)
    {
        super(out);

        this.provider = provider;
    }

    public void writeObject(
        Object  obj)
        throws IOException
    {
        try
        {
            super.writeObject(new MiscPEMGenerator(obj));
        }
        catch (PemGenerationException e)
        {
            if (e.getCause() instanceof IOException)
            {
                throw (IOException)e.getCause();
            }

            throw e;
        }
    }

    public void writeObject(
        PemObjectGenerator obj)
        throws IOException
    {
        super.writeObject(obj);
    }

    public void writeObject(
        Object       obj,
        String       algorithm,
        char[]       password,
        SecureRandom random)
        throws IOException
    {
        try
        {
            super.writeObject(new MiscPEMGenerator(obj, algorithm, password, random, provider));
        }
        catch (NoSuchProviderException e)
        {
            throw new EncryptionException(e.getMessage(), e);
        }
    }
}
