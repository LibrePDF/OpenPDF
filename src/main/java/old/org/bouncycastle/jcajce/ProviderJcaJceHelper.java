package old.org.bouncycastle.jcajce;

import java.security.AlgorithmParameterGenerator;
import java.security.AlgorithmParameters;
import java.security.KeyFactory;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.Signature;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;

import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import old.org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import old.org.bouncycastle.operator.AsymmetricKeyUnwrapper;
import old.org.bouncycastle.operator.SymmetricKeyUnwrapper;
import old.org.bouncycastle.operator.jcajce.JceAsymmetricKeyUnwrapper;
import old.org.bouncycastle.operator.jcajce.JceSymmetricKeyUnwrapper;

public class ProviderJcaJceHelper
    implements JcaJceHelper
{
    private final Provider provider;

    public ProviderJcaJceHelper(Provider provider)
    {
        this.provider = provider;
    }

    public Cipher createCipher(
        String algorithm)
        throws NoSuchAlgorithmException, NoSuchPaddingException
    {
        return Cipher.getInstance(algorithm, provider);
    }

    public Mac createMac(String algorithm)
        throws NoSuchAlgorithmException
    {
        return Mac.getInstance(algorithm, provider);
    }

    public KeyAgreement createKeyAgreement(String algorithm)
        throws NoSuchAlgorithmException
    {
        return KeyAgreement.getInstance(algorithm, provider);
    }

    public AlgorithmParameterGenerator createAlgorithmParameterGenerator(String algorithm)
        throws NoSuchAlgorithmException
    {
        return AlgorithmParameterGenerator.getInstance(algorithm, provider);
    }

    public AlgorithmParameters createAlgorithmParameters(String algorithm)
        throws NoSuchAlgorithmException
    {
        return AlgorithmParameters.getInstance(algorithm, provider);
    }

    public KeyGenerator createKeyGenerator(String algorithm)
        throws NoSuchAlgorithmException
    {
        return KeyGenerator.getInstance(algorithm, provider);
    }

    public KeyFactory createKeyFactory(String algorithm)
        throws NoSuchAlgorithmException
    {
        return KeyFactory.getInstance(algorithm, provider);
    }

    public KeyPairGenerator createKeyPairGenerator(String algorithm)
        throws NoSuchAlgorithmException
    {
        return KeyPairGenerator.getInstance(algorithm, provider);
    }

    public MessageDigest createDigest(String algorithm)
        throws NoSuchAlgorithmException
    {
        return MessageDigest.getInstance(algorithm, provider);
    }

    public Signature createSignature(String algorithm)
        throws NoSuchAlgorithmException
    {
        return Signature.getInstance(algorithm, provider);
    }

    public CertificateFactory createCertificateFactory(String algorithm)
        throws NoSuchAlgorithmException, CertificateException
    {
        return CertificateFactory.getInstance(algorithm, provider);
    }

    public AsymmetricKeyUnwrapper createAsymmetricUnwrapper(AlgorithmIdentifier keyEncryptionAlgorithm, PrivateKey keyEncryptionKey)
    {
        return new JceAsymmetricKeyUnwrapper(keyEncryptionAlgorithm, keyEncryptionKey).setProvider(provider);
    }

    public SymmetricKeyUnwrapper createSymmetricUnwrapper(AlgorithmIdentifier keyEncryptionAlgorithm, SecretKey keyEncryptionKey)
    {
        return new JceSymmetricKeyUnwrapper(keyEncryptionAlgorithm, keyEncryptionKey).setProvider(provider);
    }
}