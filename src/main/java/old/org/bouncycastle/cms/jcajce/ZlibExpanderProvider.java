package old.org.bouncycastle.cms.jcajce;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.InflaterInputStream;

import old.org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import old.org.bouncycastle.operator.InputExpander;
import old.org.bouncycastle.operator.InputExpanderProvider;
import old.org.bouncycastle.util.io.StreamOverflowException;

public class ZlibExpanderProvider
    implements InputExpanderProvider
{
    private final long limit;

    public ZlibExpanderProvider()
    {
        this.limit = -1;
    }

    /**
     * Create a provider which caps the number of expanded bytes that can be produced when the
     * compressed stream is parsed.
     *
     * @param limit max number of bytes allowed in an expanded stream.
     */
    public ZlibExpanderProvider(long limit)
    {
        this.limit = limit;
    }

    public InputExpander get(final AlgorithmIdentifier algorithm)
    {
        return new InputExpander()
        {
            public AlgorithmIdentifier getAlgorithmIdentifier()
            {
                return algorithm;
            }

            public InputStream getInputStream(InputStream comIn)
            {
                InputStream s = new InflaterInputStream(comIn);                
                if (limit >= 0)
                {
                    s = new LimitedInputStream(s, limit);
                }
                return s;
            }
        };
    }

    private static class LimitedInputStream
        extends FilterInputStream
    {
        private long remaining;

        public LimitedInputStream(InputStream input, long limit)
        {
            super(input);

            this.remaining = limit;
        }

        public int read()
            throws IOException
        {
            // Only a single 'extra' byte will ever be read
            if (remaining >= 0)
            {
                int b = super.in.read();
                if (b < 0 || --remaining >= 0)
                {
                    return b;
                }
            }

            throw new StreamOverflowException("expanded byte limit exceeded");
        }

        public int read(byte[] buf, int off, int len)
            throws IOException
        {
            if (len < 1)
            {
                // This will give correct exceptions/returns for strange lengths
                return super.read(buf, off, len);
            }

            if (remaining < 1)
            {
                // Will either return EOF or throw exception
                read();
                return -1;
            }

            /*
             * Limit the underlying request to 'remaining' bytes. This ensures the
             * caller will see the full 'limit' bytes before getting an exception.
             * Also, only one extra byte will ever be read.
             */
            int actualLen = (remaining > len ? len : (int)remaining);
            int numRead = super.in.read(buf, off, actualLen);
            if (numRead > 0)
            {
                remaining -= numRead;
            }
            return numRead;
        }
    }
}
