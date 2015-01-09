package old.org.bouncycastle.cert.cmp;

import java.io.IOException;

import old.org.bouncycastle.asn1.ASN1Object;
import old.org.bouncycastle.asn1.cmp.PKIBody;
import old.org.bouncycastle.asn1.cmp.PKIHeader;
import old.org.bouncycastle.asn1.cmp.PKIMessage;
import old.org.bouncycastle.cert.CertIOException;

/**
 * General wrapper for a generic PKIMessage
 */
public class GeneralPKIMessage
{
    private final PKIMessage pkiMessage;

    private static PKIMessage parseBytes(byte[] encoding)
        throws IOException
    {
        try
        {
            return PKIMessage.getInstance(ASN1Object.fromByteArray(encoding));
        }
        catch (ClassCastException e)
        {
            throw new CertIOException("malformed data: " + e.getMessage(), e);
        }
        catch (IllegalArgumentException e)
        {
            throw new CertIOException("malformed data: " + e.getMessage(), e);
        }
    }

    /**
     * Create a PKIMessage from the passed in bytes.
     *
     * @param encoding BER/DER encoding of the PKIMessage
     * @throws IOException in the event of corrupted data, or an incorrect structure.
     */
    public GeneralPKIMessage(byte[] encoding)
        throws IOException
    {
        this(parseBytes(encoding));
    }

    /**
     * Wrap a PKIMessage ASN.1 structure.
     *
     * @param pkiMessage base PKI message.
     */
    public GeneralPKIMessage(PKIMessage pkiMessage)
    {
        this.pkiMessage = pkiMessage;
    }

    public PKIHeader getHeader()
    {
        return pkiMessage.getHeader();
    }

    public PKIBody getBody()
    {
        return pkiMessage.getBody();
    }

    /**
     * Return true if this message has protection bits on it. A return value of true
     * indicates the message can be used to construct a ProtectedPKIMessage.
     *
     * @return true if message has protection, false otherwise.
     */
    public boolean hasProtection()
    {
        return pkiMessage.getHeader().getProtectionAlg() != null;
    }

    public PKIMessage toASN1Structure()
    {
        return pkiMessage;
    }
}
