package old.org.bouncycastle.cms;

import java.io.IOException;
import java.security.AlgorithmParameterGenerator;
import java.security.AlgorithmParameters;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.InvalidParameterSpecException;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.RC2ParameterSpec;

import old.org.bouncycastle.asn1.DERObjectIdentifier;
import old.org.bouncycastle.asn1.x509.AlgorithmIdentifier;

public class CMSAuthenticatedGenerator
    extends CMSEnvelopedGenerator
{
    protected CMSAttributeTableGenerator authGen;
    protected CMSAttributeTableGenerator unauthGen;

    /**
     * base constructor
     */
    public CMSAuthenticatedGenerator()
    {
    }

    /**
     * constructor allowing specific source of randomness
     *
     * @param rand instance of SecureRandom to use
     */
    public CMSAuthenticatedGenerator(
        SecureRandom rand)
    {
        super(rand);
    }

    protected AlgorithmIdentifier getAlgorithmIdentifier(String encryptionOID, AlgorithmParameterSpec paramSpec, Provider provider)
        throws IOException, NoSuchAlgorithmException, InvalidParameterSpecException
    {
        AlgorithmParameters params = CMSEnvelopedHelper.INSTANCE.createAlgorithmParameters(encryptionOID, provider);
        params.init(paramSpec);

        return getAlgorithmIdentifier(encryptionOID, params);
    }

    protected AlgorithmParameterSpec generateParameterSpec(String encryptionOID, SecretKey encKey, Provider encProvider)
        throws CMSException
    {
        try
        {
            if (encryptionOID.equals(RC2_CBC))
            {
                byte[] iv = new byte[8];

                rand.nextBytes(iv);

                return new RC2ParameterSpec(encKey.getEncoded().length * 8, iv);
            }

            AlgorithmParameterGenerator pGen = CMSEnvelopedHelper.INSTANCE.createAlgorithmParameterGenerator(encryptionOID, encProvider);

            AlgorithmParameters p = pGen.generateParameters();

            return p.getParameterSpec(IvParameterSpec.class);
        }
        catch (GeneralSecurityException e)
        {
            return null;
        }
    }

    public void setAuthenticatedAttributeGenerator(CMSAttributeTableGenerator authGen)
    {
        this.authGen = authGen;
    }

    public void setUnauthenticatedAttributeGenerator(CMSAttributeTableGenerator unauthGen)
    {
        this.unauthGen = unauthGen;
    }

    protected Map getBaseParameters(DERObjectIdentifier contentType, AlgorithmIdentifier digAlgId, byte[] hash)
    {
        Map param = new HashMap();
        param.put(CMSAttributeTableGenerator.CONTENT_TYPE, contentType);
        param.put(CMSAttributeTableGenerator.DIGEST_ALGORITHM_IDENTIFIER, digAlgId);
        param.put(CMSAttributeTableGenerator.DIGEST,  hash.clone());
        return param;
    }
}
