package old.org.bouncycastle.cms;

import java.security.SecureRandom;

import old.org.bouncycastle.asn1.ASN1EncodableVector;
import old.org.bouncycastle.asn1.ASN1ObjectIdentifier;
import old.org.bouncycastle.asn1.ASN1OctetString;
import old.org.bouncycastle.asn1.DEROctetString;
import old.org.bouncycastle.asn1.DERSequence;
import old.org.bouncycastle.asn1.cms.PasswordRecipientInfo;
import old.org.bouncycastle.asn1.cms.RecipientInfo;
import old.org.bouncycastle.asn1.pkcs.PBKDF2Params;
import old.org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import old.org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import old.org.bouncycastle.crypto.PBEParametersGenerator;
import old.org.bouncycastle.crypto.generators.PKCS5S2ParametersGenerator;
import old.org.bouncycastle.crypto.params.KeyParameter;
import old.org.bouncycastle.operator.GenericKey;

public abstract class PasswordRecipientInfoGenerator
    implements RecipientInfoGenerator
{
    private char[] password;
    private AlgorithmIdentifier keyDerivationAlgorithm;
    private ASN1ObjectIdentifier kekAlgorithm;
    private SecureRandom random;
    private int schemeID;
    private int keySize;
    private int blockSize;

    protected PasswordRecipientInfoGenerator(ASN1ObjectIdentifier kekAlgorithm, char[] password)
    {
        this(kekAlgorithm, password, getKeySize(kekAlgorithm), ((Integer)PasswordRecipientInformation.BLOCKSIZES.get(kekAlgorithm)).intValue());
    }

    protected PasswordRecipientInfoGenerator(ASN1ObjectIdentifier kekAlgorithm, char[] password, int keySize, int blockSize)
    {
        this.password = password;
        this.schemeID = PasswordRecipient.PKCS5_SCHEME2_UTF8;
        this.kekAlgorithm = kekAlgorithm;
        this.keySize = keySize;
        this.blockSize = blockSize;
    }

    private static int getKeySize(ASN1ObjectIdentifier kekAlgorithm)
    {
        Integer size = (Integer)PasswordRecipientInformation.KEYSIZES.get(kekAlgorithm);

        if (size == null)
        {
            throw new IllegalArgumentException("cannot find key size for algorithm: " +  kekAlgorithm);
        }

        return size.intValue();
    }

    public PasswordRecipientInfoGenerator setPasswordConversionScheme(int schemeID)
    {
        this.schemeID = schemeID;

        return this;
    }

    public PasswordRecipientInfoGenerator setSaltAndIterationCount(byte[] salt, int iterationCount)
    {
        this.keyDerivationAlgorithm = new AlgorithmIdentifier(PKCSObjectIdentifiers.id_PBKDF2, new PBKDF2Params(salt, iterationCount));

        return this;
    }

    public PasswordRecipientInfoGenerator setSecureRandom(SecureRandom random)
    {
        this.random = random;

        return this;
    }

    public RecipientInfo generate(GenericKey contentEncryptionKey)
        throws CMSException
    {
        byte[] iv = new byte[blockSize];     /// TODO: set IV size properly!

        if (random == null)
        {
            random = new SecureRandom();
        }
        
        random.nextBytes(iv);

        if (keyDerivationAlgorithm == null)
        {
            byte[] salt = new byte[20];

            random.nextBytes(salt);

            keyDerivationAlgorithm = new AlgorithmIdentifier(PKCSObjectIdentifiers.id_PBKDF2, new PBKDF2Params(salt, 1024));
        }

        PBKDF2Params params = PBKDF2Params.getInstance(keyDerivationAlgorithm.getParameters());
        byte[] derivedKey;

        if (schemeID == PasswordRecipient.PKCS5_SCHEME2)
        {
            PKCS5S2ParametersGenerator gen = new PKCS5S2ParametersGenerator();

            gen.init(PBEParametersGenerator.PKCS5PasswordToBytes(password), params.getSalt(), params.getIterationCount().intValue());

            derivedKey = ((KeyParameter)gen.generateDerivedParameters(keySize)).getKey();
        }
        else
        {
            PKCS5S2ParametersGenerator gen = new PKCS5S2ParametersGenerator();

            gen.init(PBEParametersGenerator.PKCS5PasswordToUTF8Bytes(password), params.getSalt(), params.getIterationCount().intValue());

            derivedKey = ((KeyParameter)gen.generateDerivedParameters(keySize)).getKey();
        }

        AlgorithmIdentifier kekAlgorithmId = new AlgorithmIdentifier(kekAlgorithm, new DEROctetString(iv));

        byte[] encryptedKeyBytes = generateEncryptedBytes(kekAlgorithmId, derivedKey, contentEncryptionKey);

        ASN1OctetString encryptedKey = new DEROctetString(encryptedKeyBytes);

        ASN1EncodableVector v = new ASN1EncodableVector();
        v.add(kekAlgorithm);
        v.add(new DEROctetString(iv));

        AlgorithmIdentifier keyEncryptionAlgorithm = new AlgorithmIdentifier(
            PKCSObjectIdentifiers.id_alg_PWRI_KEK, new DERSequence(v));

        return new RecipientInfo(new PasswordRecipientInfo(keyDerivationAlgorithm,
            keyEncryptionAlgorithm, encryptedKey));
    }

    protected abstract byte[] generateEncryptedBytes(AlgorithmIdentifier algorithm, byte[] derivedKey, GenericKey contentEncryptionKey)
        throws CMSException;
}