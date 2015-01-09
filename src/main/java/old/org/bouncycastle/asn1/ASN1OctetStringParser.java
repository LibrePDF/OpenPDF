package old.org.bouncycastle.asn1;

import java.io.InputStream;

public interface ASN1OctetStringParser
    extends DEREncodable, InMemoryRepresentable
{
    public InputStream getOctetStream();
}
