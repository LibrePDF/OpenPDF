package old.org.bouncycastle.cms;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;


class CounterSignatureDigestCalculator
    implements IntDigestCalculator
{
    private final String alg;
    private final Provider provider;
    private final byte[] data;

    CounterSignatureDigestCalculator(String alg, Provider provider, byte[] data)
    {
        this.alg = alg;
        this.provider = provider;
        this.data = data;
    }

    public byte[] getDigest()
        throws NoSuchAlgorithmException
    {
        MessageDigest digest = CMSSignedHelper.INSTANCE.getDigestInstance(alg, provider);

        return digest.digest(data);
    }
}
