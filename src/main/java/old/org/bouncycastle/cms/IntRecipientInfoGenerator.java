package old.org.bouncycastle.cms;

import java.security.GeneralSecurityException;
import java.security.Provider;
import java.security.SecureRandom;

import javax.crypto.SecretKey;

import old.org.bouncycastle.asn1.cms.RecipientInfo;

interface IntRecipientInfoGenerator
{
    /**
     * Generate a RecipientInfo object for the given key.
     * @param contentEncryptionKey the <code>SecretKey</code> to encrypt
     * @param random a source of randomness
     * @param prov the default provider to use
     * @return a <code>RecipientInfo</code> object for the given key
     * @throws GeneralSecurityException
     */
    RecipientInfo generate(SecretKey contentEncryptionKey, SecureRandom random,
        Provider prov) throws GeneralSecurityException;
}
