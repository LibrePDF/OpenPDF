package old.org.bouncycastle.cms.jcajce;

import java.io.OutputStream;
import java.util.zip.DeflaterOutputStream;

import old.org.bouncycastle.asn1.ASN1ObjectIdentifier;
import old.org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import old.org.bouncycastle.operator.OutputCompressor;

public class ZlibCompressor
    implements OutputCompressor
{
    private static final String  ZLIB    = "1.2.840.113549.1.9.16.3.8";

    public AlgorithmIdentifier getAlgorithmIdentifier()
    {
        return new AlgorithmIdentifier(new ASN1ObjectIdentifier(ZLIB));
    }

    public OutputStream getOutputStream(OutputStream comOut)
    {
        return new DeflaterOutputStream(comOut);
    }
}
