package old.org.bouncycastle.jcajce;

import java.security.AlgorithmParameterGenerator;
import java.security.AlgorithmParameters;
import java.security.KeyFactory;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
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

public class DefaultJcaJceHelper
    implements JcaJceHelper
{
    public Cipher createCipher(
        String algorithm)
        throws NoSuchAlgorithmException, NoSuchPaddingException
    {
        return Cipher.getInstance(algorithm);
    }

    public Mac createMac(String algorithm)
        throws NoSuchAlgorithmException
    {
        return Mac.getInstance(algorithm);
    }

    public KeyAgreement createKeyAgreement(String algorithm)
        throws NoSuchAlgorithmException
    {
        return KeyAgreement.getInstance(algorithm);
    }

    public AlgorithmParameterGenerator createAlgorithmParameterGenerator(String algorithm)
        throws NoSuchAlgorithmException
    {
        return AlgorithmParameterGenerator.getInstance(algorithm);
    }

    public AlgorithmParameters createAlgorithmParameters(String algorithm)
        throws NoSuchAlgorithmException
    {
        return AlgorithmParameters.getInstance(algorithm);
    }

    public KeyGenerator createKeyGenerator(String algorithm)
        throws NoSuchAlgorithmException
    {
        return KeyGenerator.getInstance(algorithm);
    }

    public KeyFactory createKeyFactory(String algorithm)
        throws NoSuchAlgorithmException
    {
        return KeyFactory.getInstance(algorithm);
    }

    public KeyPairGenerator createKeyPairGenerator(String algorithm)
        throws NoSuchAlgorithmException
    {
        return KeyPairGenerator.getInstance(algorithm);
    }

    public MessageDigest createDigest(String algorithm)
        throws NoSuchAlgorithmException
    {
        return MessageDigest.getInstance(algorithm);
    }

    public Signature createSignature(String algorithm)
        throws NoSuchAlgorithmException
    {
        return Signature.getInstance(algorithm);
    }

    public CertificateFactory createCertificateFactory(String algorithm)
        throws NoSuchAlgorithmException, CertificateException
    {
        return CertificateFactory.getInstance(algorithm);
    }

    public AsymmetricKeyUnwrapper createAsymmetricUnwrapper(AlgorithmIdentifier keyEncryptionAlgorithm, PrivateKey keyEncryptionKey)
    {
        return new JceAsymmetricKeyUnwrapper(keyEncryptionAlgorithm, keyEncryptionKey);
    }

    public SymmetricKeyUnwrapper createSymmetricUnwrapper(AlgorithmIdentifier keyEncryptionAlgorithm, SecretKey keyEncryptionKey)
    {
        return new JceSymmetricKeyUnwrapper(keyEncryptionAlgorithm, keyEncryptionKey);
    }
}
