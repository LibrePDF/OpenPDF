package old.org.bouncycastle.pkcs;

import java.io.IOException;

import old.org.bouncycastle.asn1.pkcs.EncryptedPrivateKeyInfo;

/**
 * Holding class for a PKCS#8 EncryptedPrivateKeyInfo structure.
 */
public class EncryptedPrivateKeyInfoHolder
{
    private EncryptedPrivateKeyInfo encryptedPrivateKeyInfo;

    public EncryptedPrivateKeyInfoHolder(EncryptedPrivateKeyInfo encryptedPrivateKeyInfo)
    {
        this.encryptedPrivateKeyInfo = encryptedPrivateKeyInfo;
    }

    public EncryptedPrivateKeyInfo toASN1Structure()
    {
         return encryptedPrivateKeyInfo;
    }

    public byte[] getEncoded()
        throws IOException
    {
        return encryptedPrivateKeyInfo.getEncoded();
    }
}
