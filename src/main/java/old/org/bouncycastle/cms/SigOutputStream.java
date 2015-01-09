package old.org.bouncycastle.cms;

import java.io.IOException;
import java.io.OutputStream;
import java.security.Signature;
import java.security.SignatureException;

class SigOutputStream extends OutputStream
{
    private final Signature sig;

    SigOutputStream(Signature sig)
    {
        this.sig = sig;
    }

    public void write(byte[] b, int off, int len) throws IOException
    {
        try
        {
            sig.update(b, off, len);
        }
        catch (SignatureException e)
        {
            throw new CMSStreamException("signature problem: " + e, e);
        }
    }

    public void write(int b) throws IOException
    {
        try
        {
            sig.update((byte) b);
        }
        catch (SignatureException e)
        {
            throw new CMSStreamException("signature problem: " + e, e);
        }
    }
}