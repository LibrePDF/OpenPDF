package old.org.bouncycastle.operator;

import old.org.bouncycastle.asn1.x509.AlgorithmIdentifier;

public interface InputExpanderProvider
{
    InputExpander get(AlgorithmIdentifier algorithm);
}
