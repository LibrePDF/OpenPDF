package old.org.bouncycastle.asn1;

import java.io.IOException;

public interface ASN1SetParser
    extends DEREncodable, InMemoryRepresentable
{
    public DEREncodable readObject()
        throws IOException;
}
