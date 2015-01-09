package old.org.bouncycastle.cms.jcajce;

import java.io.OutputStream;
import java.security.AlgorithmParameterGenerator;
import java.security.AlgorithmParameters;
import java.security.GeneralSecurityException;
import java.security.Provider;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.RC2ParameterSpec;

import old.org.bouncycastle.asn1.ASN1ObjectIdentifier;
import old.org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import old.org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import old.org.bouncycastle.cms.CMSException;
import old.org.bouncycastle.jcajce.DefaultJcaJceHelper;
import old.org.bouncycastle.jcajce.NamedJcaJceHelper;
import old.org.bouncycastle.jcajce.ProviderJcaJceHelper;
import old.org.bouncycastle.jcajce.io.MacOutputStream;
import old.org.bouncycastle.operator.GenericKey;
import old.org.bouncycastle.operator.MacCalculator;

public class JceCMSMacCalculatorBuilder
{
    private final ASN1ObjectIdentifier macOID;
    private final int                  keySize;

    private EnvelopedDataHelper helper = new EnvelopedDataHelper(new DefaultJcaJceHelper());
    private SecureRandom random;
    private MacOutputStream macOutputStream;

    public JceCMSMacCalculatorBuilder(ASN1ObjectIdentifier macOID)
    {
        this(macOID, -1);
    }

    public JceCMSMacCalculatorBuilder(ASN1ObjectIdentifier macOID, int keySize)
    {
        this.macOID = macOID;
        this.keySize = keySize;
    }

    public JceCMSMacCalculatorBuilder setProvider(Provider provider)
    {
        this.helper = new EnvelopedDataHelper(new ProviderJcaJceHelper(provider));

        return this;
    }

    public JceCMSMacCalculatorBuilder setProvider(String providerName)
    {
        this.helper = new EnvelopedDataHelper(new NamedJcaJceHelper(providerName));

        return this;
    }

    public JceCMSMacCalculatorBuilder setSecureRandom(SecureRandom random)
    {
        this.random = random;

        return this;
    }

    public MacCalculator build()
        throws CMSException
    {
        return new CMSOutputEncryptor(macOID, keySize, random);
    }

    private class CMSOutputEncryptor
        implements MacCalculator
    {
        private SecretKey encKey;
        private AlgorithmIdentifier algorithmIdentifier;
        private Mac mac;
        private SecureRandom random;

        CMSOutputEncryptor(ASN1ObjectIdentifier macOID, int keySize, SecureRandom random)
            throws CMSException
        {
            KeyGenerator keyGen = helper.createKeyGenerator(macOID);

            if (random == null)
            {
                random = new SecureRandom();
            }

            this.random = random;

            if (keySize < 0)
            {
                keyGen.init(random);
            }
            else
            {
                keyGen.init(keySize, random);
            }

            encKey = keyGen.generateKey();

            AlgorithmParameterSpec paramSpec = generateParameterSpec(macOID, encKey);

            algorithmIdentifier = helper.getAlgorithmIdentifier(macOID, paramSpec);
            mac = helper.createContentMac(encKey, algorithmIdentifier);
        }

        public AlgorithmIdentifier getAlgorithmIdentifier()
        {
            return algorithmIdentifier;
        }

        public OutputStream getOutputStream()
        {
            return new MacOutputStream(mac);
        }

        public byte[] getMac()
        {
            return mac.doFinal();
        }

        public GenericKey getKey()
        {
            return new GenericKey(encKey);
        }

        protected AlgorithmParameterSpec generateParameterSpec(ASN1ObjectIdentifier macOID, SecretKey encKey)
            throws CMSException
        {
            try
            {
                if (macOID.equals(PKCSObjectIdentifiers.RC2_CBC))
                {
                    byte[] iv = new byte[8];

                    random.nextBytes(iv);

                    return new RC2ParameterSpec(encKey.getEncoded().length * 8, iv);
                }

                AlgorithmParameterGenerator pGen = helper.createAlgorithmParameterGenerator(macOID);

                AlgorithmParameters p = pGen.generateParameters();

                return p.getParameterSpec(IvParameterSpec.class);
            }
            catch (GeneralSecurityException e)
            {
                return null;
            }
        }
    }
}
