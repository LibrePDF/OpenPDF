package old.org.bouncycastle.cms;

import java.io.IOException;
import java.security.AlgorithmParameterGenerator;
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.crypto.SecretKey;
import javax.crypto.spec.RC2ParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import old.org.bouncycastle.asn1.ASN1Object;
import old.org.bouncycastle.asn1.DEREncodable;
import old.org.bouncycastle.asn1.DERNull;
import old.org.bouncycastle.asn1.DERObjectIdentifier;
import old.org.bouncycastle.asn1.DEROctetString;
import old.org.bouncycastle.asn1.cms.KEKIdentifier;
import old.org.bouncycastle.asn1.kisa.KISAObjectIdentifiers;
import old.org.bouncycastle.asn1.nist.NISTObjectIdentifiers;
import old.org.bouncycastle.asn1.ntt.NTTObjectIdentifiers;
import old.org.bouncycastle.asn1.pkcs.PBKDF2Params;
import old.org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import old.org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import old.org.bouncycastle.asn1.x9.X9ObjectIdentifiers;

/**
 * General class for generating a CMS enveloped-data message.
 */
public class CMSEnvelopedGenerator
{
    public static final String  DES_EDE3_CBC    = PKCSObjectIdentifiers.des_EDE3_CBC.getId();
    public static final String  RC2_CBC         = PKCSObjectIdentifiers.RC2_CBC.getId();
    public static final String  IDEA_CBC        = "1.3.6.1.4.1.188.7.1.1.2";
    public static final String  CAST5_CBC       = "1.2.840.113533.7.66.10";
    public static final String  AES128_CBC      = NISTObjectIdentifiers.id_aes128_CBC.getId();
    public static final String  AES192_CBC      = NISTObjectIdentifiers.id_aes192_CBC.getId();
    public static final String  AES256_CBC      = NISTObjectIdentifiers.id_aes256_CBC.getId();
    public static final String  CAMELLIA128_CBC = NTTObjectIdentifiers.id_camellia128_cbc.getId();
    public static final String  CAMELLIA192_CBC = NTTObjectIdentifiers.id_camellia192_cbc.getId();
    public static final String  CAMELLIA256_CBC = NTTObjectIdentifiers.id_camellia256_cbc.getId();
    public static final String  SEED_CBC        = KISAObjectIdentifiers.id_seedCBC.getId();

    public static final String  DES_EDE3_WRAP   = PKCSObjectIdentifiers.id_alg_CMS3DESwrap.getId();
    public static final String  AES128_WRAP     = NISTObjectIdentifiers.id_aes128_wrap.getId();
    public static final String  AES192_WRAP     = NISTObjectIdentifiers.id_aes192_wrap.getId();
    public static final String  AES256_WRAP     = NISTObjectIdentifiers.id_aes256_wrap.getId();
    public static final String  CAMELLIA128_WRAP = NTTObjectIdentifiers.id_camellia128_wrap.getId();
    public static final String  CAMELLIA192_WRAP = NTTObjectIdentifiers.id_camellia192_wrap.getId();
    public static final String  CAMELLIA256_WRAP = NTTObjectIdentifiers.id_camellia256_wrap.getId();
    public static final String  SEED_WRAP       = KISAObjectIdentifiers.id_npki_app_cmsSeed_wrap.getId();

    public static final String  ECDH_SHA1KDF    = X9ObjectIdentifiers.dhSinglePass_stdDH_sha1kdf_scheme.getId();
    public static final String  ECMQV_SHA1KDF   = X9ObjectIdentifiers.mqvSinglePass_sha1kdf_scheme.getId();

    final List oldRecipientInfoGenerators = new ArrayList();
    final List recipientInfoGenerators = new ArrayList();

    protected CMSAttributeTableGenerator unprotectedAttributeGenerator = null;

    final SecureRandom rand;

    /**
     * base constructor
     */
    public CMSEnvelopedGenerator()
    {
        this(new SecureRandom());
    }

    /**
     * constructor allowing specific source of randomness
     * @param rand instance of SecureRandom to use
     */
    public CMSEnvelopedGenerator(
        SecureRandom rand)
    {
        this.rand = rand;
    }

    public void setUnprotectedAttributeGenerator(CMSAttributeTableGenerator unprotectedAttributeGenerator)
    {
        this.unprotectedAttributeGenerator = unprotectedAttributeGenerator;
    }

    /**
     * add a recipient.
     *
     * @deprecated use the addRecipientGenerator and JceKeyTransRecipientInfoGenerator
     * @param cert recipient's public key certificate
     * @exception IllegalArgumentException if there is a problem with the certificate
     */
    public void addKeyTransRecipient(
        X509Certificate cert)
        throws IllegalArgumentException
    {
        KeyTransIntRecipientInfoGenerator ktrig = new KeyTransIntRecipientInfoGenerator();
        ktrig.setRecipientCert(cert);

        oldRecipientInfoGenerators.add(ktrig);
    }

    /**
     * add a recipient
     *
     * @deprecated use the addRecipientGenerator and JceKeyTransRecipientInfoGenerator
     * @param key the public key used by the recipient
     * @param subKeyId the identifier for the recipient's public key
     * @exception IllegalArgumentException if there is a problem with the key
     */
    public void addKeyTransRecipient(
        PublicKey   key,
        byte[]      subKeyId)
        throws IllegalArgumentException
    {
        KeyTransIntRecipientInfoGenerator ktrig = new KeyTransIntRecipientInfoGenerator();
        ktrig.setRecipientPublicKey(key);
        ktrig.setSubjectKeyIdentifier(new DEROctetString(subKeyId));

        oldRecipientInfoGenerators.add(ktrig);
    }

    /**
     * add a KEK recipient.
     *
     * @deprecated use the addRecipientGenerator and JceKEKRecipientInfoGenerator
     * @param key the secret key to use for wrapping
     * @param keyIdentifier the byte string that identifies the key
     */
    public void addKEKRecipient(
        SecretKey   key,
        byte[]      keyIdentifier)
    {
        addKEKRecipient(key, new KEKIdentifier(keyIdentifier, null, null));
    }

    /**
     * add a KEK recipient.
     *
     * @deprecated use the addRecipientGenerator and JceKEKRecipientInfoGenerator
     * @param key the secret key to use for wrapping
     * @param kekIdentifier a KEKIdentifier structure (identifies the key)
     */
    public void addKEKRecipient(
        SecretKey       key,
        KEKIdentifier   kekIdentifier)
    {
        KEKIntRecipientInfoGenerator kekrig = new KEKIntRecipientInfoGenerator();
        kekrig.setKEKIdentifier(kekIdentifier);
        kekrig.setKeyEncryptionKey(key);

        oldRecipientInfoGenerators.add(kekrig);
    }

    /**
     * @deprecated use addRecipientGenerator and JcePasswordRecipientInfoGenerator
     * @param pbeKey PBE key
     * @param kekAlgorithmOid key encryption algorithm to use.
     */
    public void addPasswordRecipient(
        CMSPBEKey pbeKey,
        String    kekAlgorithmOid)
    {
        PBKDF2Params params = new PBKDF2Params(pbeKey.getSalt(), pbeKey.getIterationCount());

        PasswordIntRecipientInfoGenerator prig = new PasswordIntRecipientInfoGenerator();
        prig.setKeyDerivationAlgorithm(new AlgorithmIdentifier(PKCSObjectIdentifiers.id_PBKDF2, params));
        prig.setKeyEncryptionKey(new SecretKeySpec(pbeKey.getEncoded(kekAlgorithmOid), kekAlgorithmOid));

        oldRecipientInfoGenerators.add(prig);
    }

    /**
     * Add a key agreement based recipient.
     *
     * @deprecated use the addRecipientGenerator and JceKeyAgreeRecipientInfoGenerator
     * @param agreementAlgorithm key agreement algorithm to use.
     * @param senderPrivateKey private key to initialise sender side of agreement with.
     * @param senderPublicKey sender public key to include with message.
     * @param recipientCert recipient's public key certificate.
     * @param cekWrapAlgorithm OID for key wrapping algorithm to use.
     * @param provider provider to use for the agreement calculation.
     * @exception NoSuchProviderException if the specified provider cannot be found
     * @exception NoSuchAlgorithmException if the algorithm requested cannot be found
     * @exception InvalidKeyException if the keys are inappropriate for the algorithm specified
     */
    public void addKeyAgreementRecipient(
        String           agreementAlgorithm,
        PrivateKey       senderPrivateKey,
        PublicKey        senderPublicKey,
        X509Certificate  recipientCert,
        String           cekWrapAlgorithm,
        String           provider)
        throws NoSuchProviderException, NoSuchAlgorithmException, InvalidKeyException
    {
        addKeyAgreementRecipient(agreementAlgorithm, senderPrivateKey, senderPublicKey, recipientCert,  cekWrapAlgorithm, CMSUtils.getProvider(provider));
    }

    /**
     * Add a key agreement based recipient.
     *
     * @deprecated use the addRecipientGenerator and JceKeyAgreeRecipientInfoGenerator
     * @param agreementAlgorithm key agreement algorithm to use.
     * @param senderPrivateKey private key to initialise sender side of agreement with.
     * @param senderPublicKey sender public key to include with message.
     * @param recipientCert recipient's public key certificate.
     * @param cekWrapAlgorithm OID for key wrapping algorithm to use.
     * @param provider provider to use for the agreement calculation.
     * @exception NoSuchAlgorithmException if the algorithm requested cannot be found
     * @exception InvalidKeyException if the keys are inappropriate for the algorithm specified
     */
    public void addKeyAgreementRecipient(
        String           agreementAlgorithm,
        PrivateKey       senderPrivateKey,
        PublicKey        senderPublicKey,
        X509Certificate  recipientCert,
        String           cekWrapAlgorithm,
        Provider         provider)
        throws NoSuchAlgorithmException, InvalidKeyException
    {
        addKeyAgreementRecipients(agreementAlgorithm, senderPrivateKey, senderPublicKey,
            Collections.singletonList(recipientCert), cekWrapAlgorithm, provider);
    }

    /**
     * Add multiple key agreement based recipients (sharing a single KeyAgreeRecipientInfo structure).
     *
     * @deprecated use the addRecipientGenerator and JceKeyAgreeRecipientInfoGenerator
     * @param agreementAlgorithm key agreement algorithm to use.
     * @param senderPrivateKey private key to initialise sender side of agreement with.
     * @param senderPublicKey sender public key to include with message.
     * @param recipientCerts recipients' public key certificates.
     * @param cekWrapAlgorithm OID for key wrapping algorithm to use.
     * @param provider provider to use for the agreement calculation.
     * @exception NoSuchAlgorithmException if the algorithm requested cannot be found
     * @exception InvalidKeyException if the keys are inappropriate for the algorithm specified
     */
    public void addKeyAgreementRecipients(
        String           agreementAlgorithm,
        PrivateKey       senderPrivateKey,
        PublicKey        senderPublicKey,
        Collection       recipientCerts,
        String           cekWrapAlgorithm,
        String           provider)
        throws NoSuchProviderException, NoSuchAlgorithmException, InvalidKeyException
    {
        addKeyAgreementRecipients(agreementAlgorithm, senderPrivateKey, senderPublicKey, recipientCerts, cekWrapAlgorithm, CMSUtils.getProvider(provider));
    }

    /**
     * Add multiple key agreement based recipients (sharing a single KeyAgreeRecipientInfo structure).
     *
     * @deprecated use the addRecipientGenerator and JceKeyAgreeRecipientInfoGenerator
     * @param agreementAlgorithm key agreement algorithm to use.
     * @param senderPrivateKey private key to initialise sender side of agreement with.
     * @param senderPublicKey sender public key to include with message.
     * @param recipientCerts recipients' public key certificates.
     * @param cekWrapAlgorithm OID for key wrapping algorithm to use.
     * @param provider provider to use for the agreement calculation.
     * @exception NoSuchAlgorithmException if the algorithm requested cannot be found
     * @exception InvalidKeyException if the keys are inappropriate for the algorithm specified
     */
    public void addKeyAgreementRecipients(
        String           agreementAlgorithm,
        PrivateKey       senderPrivateKey,
        PublicKey        senderPublicKey,
        Collection       recipientCerts,
        String           cekWrapAlgorithm,
        Provider         provider)
        throws NoSuchAlgorithmException, InvalidKeyException
    {
        /* TODO
         * "a recipient X.509 version 3 certificate that contains a key usage extension MUST
         * assert the keyAgreement bit."
         */

        KeyAgreeIntRecipientInfoGenerator karig = new KeyAgreeIntRecipientInfoGenerator();
        karig.setKeyAgreementOID(new DERObjectIdentifier(agreementAlgorithm));
        karig.setKeyEncryptionOID(new DERObjectIdentifier(cekWrapAlgorithm));
        karig.setRecipientCerts(recipientCerts);
        karig.setSenderKeyPair(new KeyPair(senderPublicKey, senderPrivateKey));

        oldRecipientInfoGenerators.add(karig);
    }

    /**
     * Add a generator to produce the recipient info required.
     * 
     * @param recipientGenerator a generator of a recipient info object.
     */
    public void addRecipientInfoGenerator(RecipientInfoGenerator recipientGenerator)
    {
        recipientInfoGenerators.add(recipientGenerator);
    }

    protected AlgorithmIdentifier getAlgorithmIdentifier(String encryptionOID, AlgorithmParameters params) throws IOException
    {
        DEREncodable asn1Params;
        if (params != null)
        {
            asn1Params = ASN1Object.fromByteArray(params.getEncoded("ASN.1"));
        }
        else
        {
            asn1Params = DERNull.INSTANCE;
        }

        return new AlgorithmIdentifier(
            new DERObjectIdentifier(encryptionOID),
            asn1Params);
    }

    protected AlgorithmParameters generateParameters(String encryptionOID, SecretKey encKey, Provider encProvider)
        throws CMSException
    {
        try
        {
            AlgorithmParameterGenerator pGen = AlgorithmParameterGenerator.getInstance(encryptionOID, encProvider);

            if (encryptionOID.equals(RC2_CBC))
            {
                byte[]  iv = new byte[8];

                rand.nextBytes(iv);

                try
                {
                    pGen.init(new RC2ParameterSpec(encKey.getEncoded().length * 8, iv), rand);
                }
                catch (InvalidAlgorithmParameterException e)
                {
                    throw new CMSException("parameters generation error: " + e, e);
                }
            }

            return pGen.generateParameters();
        }
        catch (NoSuchAlgorithmException e)
        {
            return null;
        }
    }
}
