package old.org.bouncycastle.cms;

import old.org.bouncycastle.asn1.ASN1Set;

interface AuthAttributesProvider
{
    ASN1Set getAuthAttributes();
}
