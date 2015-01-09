package old.org.bouncycastle.operator.bc;

import old.org.bouncycastle.asn1.nist.NISTObjectIdentifiers;
import old.org.bouncycastle.asn1.oiw.OIWObjectIdentifiers;
import old.org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import old.org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import old.org.bouncycastle.crypto.Digest;
import old.org.bouncycastle.crypto.digests.MD4Digest;
import old.org.bouncycastle.crypto.digests.MD5Digest;
import old.org.bouncycastle.crypto.digests.SHA1Digest;
import old.org.bouncycastle.crypto.digests.SHA224Digest;
import old.org.bouncycastle.crypto.digests.SHA256Digest;
import old.org.bouncycastle.crypto.digests.SHA384Digest;
import old.org.bouncycastle.operator.OperatorCreationException;

class BcUtil
{
    static Digest createDigest(AlgorithmIdentifier digAlg)
        throws OperatorCreationException
    {
        Digest dig;

        if (digAlg.getAlgorithm().equals(OIWObjectIdentifiers.idSHA1))
        {
            dig = new SHA1Digest();
        }
        else if (digAlg.getAlgorithm().equals(NISTObjectIdentifiers.id_sha224))
        {
            dig = new SHA224Digest();
        }
        else if (digAlg.getAlgorithm().equals(NISTObjectIdentifiers.id_sha256))
        {
            dig = new SHA256Digest();
        }
        else if (digAlg.getAlgorithm().equals(NISTObjectIdentifiers.id_sha384))
        {
            dig = new SHA384Digest();
        }
        else if (digAlg.getAlgorithm().equals(NISTObjectIdentifiers.id_sha512))
        {
            dig = new SHA384Digest();
        }
        else if (digAlg.getAlgorithm().equals(PKCSObjectIdentifiers.md5))
        {
            dig = new MD5Digest();
        }
        else if (digAlg.getAlgorithm().equals(PKCSObjectIdentifiers.md4))
        {
            dig = new MD4Digest();
        }
        else
        {
            throw new OperatorCreationException("cannot recognise digest");
        }

        return dig;
    }
}
