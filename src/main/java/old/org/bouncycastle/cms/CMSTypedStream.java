package old.org.bouncycastle.cms;

import java.io.BufferedInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

import old.org.bouncycastle.asn1.ASN1ObjectIdentifier;
import old.org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import old.org.bouncycastle.util.io.Streams;

public class CMSTypedStream
{
    private static final int BUF_SIZ = 32 * 1024;
    
    private final ASN1ObjectIdentifier      _oid;
    private final InputStream _in;

    public CMSTypedStream(
        InputStream in)
    {
        this(PKCSObjectIdentifiers.data.getId(), in, BUF_SIZ);
    }
    
    public CMSTypedStream(
         String oid,
         InputStream in)
    {
        this(new ASN1ObjectIdentifier(oid), in, BUF_SIZ);
    }
    
    public CMSTypedStream(
        String      oid,
        InputStream in,
        int         bufSize)
    {
        this(new ASN1ObjectIdentifier(oid), in, bufSize);
    }

    public CMSTypedStream(
         ASN1ObjectIdentifier oid,
         InputStream in)
    {
        this(oid, in, BUF_SIZ);
    }

    public CMSTypedStream(
        ASN1ObjectIdentifier      oid,
        InputStream in,
        int         bufSize)
    {
        _oid = oid;
        _in = new FullReaderStream(new BufferedInputStream(in, bufSize));
    }

    public ASN1ObjectIdentifier getContentType()
    {
        return _oid;
    }
    
    public InputStream getContentStream()
    {
        return _in;
    }

    public void drain() 
        throws IOException
    {
        Streams.drain(_in);
        _in.close();
    }

    private static class FullReaderStream extends FilterInputStream
    {
        FullReaderStream(InputStream in)
        {
            super(in);
        }

        public int read(byte[] buf, int off, int len) throws IOException
        {
            int totalRead = Streams.readFully(super.in, buf, off, len);
            return totalRead > 0 ? totalRead : -1;
        }
    }
}
