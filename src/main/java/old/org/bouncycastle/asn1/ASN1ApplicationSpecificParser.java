package old.org.bouncycastle.asn1;

import java.io.IOException;

public interface ASN1ApplicationSpecificParser
    extends DEREncodable, InMemoryRepresentable
{
    DEREncodable readObject()
        throws IOException;
}
