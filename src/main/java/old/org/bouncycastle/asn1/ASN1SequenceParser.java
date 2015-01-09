package old.org.bouncycastle.asn1;

import java.io.IOException;

public interface ASN1SequenceParser
    extends DEREncodable, InMemoryRepresentable
{
    DEREncodable readObject()
        throws IOException;
}
