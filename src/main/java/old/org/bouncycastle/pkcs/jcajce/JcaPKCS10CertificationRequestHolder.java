package old.org.bouncycastle.pkcs.jcajce;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Hashtable;

import old.org.bouncycastle.asn1.pkcs.CertificationRequest;
import old.org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import old.org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import old.org.bouncycastle.asn1.x9.X9ObjectIdentifiers;
import old.org.bouncycastle.jcajce.DefaultJcaJceHelper;
import old.org.bouncycastle.jcajce.JcaJceHelper;
import old.org.bouncycastle.jcajce.NamedJcaJceHelper;
import old.org.bouncycastle.jcajce.ProviderJcaJceHelper;
import old.org.bouncycastle.pkcs.PKCS10CertificationRequestHolder;

public class JcaPKCS10CertificationRequestHolder
    extends PKCS10CertificationRequestHolder
{
    private static Hashtable keyAlgorithms = new Hashtable();

    static
    {
        //
        // key types
        //
        keyAlgorithms.put(PKCSObjectIdentifiers.rsaEncryption, "RSA");
        keyAlgorithms.put(X9ObjectIdentifiers.id_dsa, "DSA");
    }

    private JcaJceHelper helper = new DefaultJcaJceHelper();

    public JcaPKCS10CertificationRequestHolder(CertificationRequest certificationRequest)
    {
        super(certificationRequest);
    }

    public JcaPKCS10CertificationRequestHolder(byte[] encoding)
        throws IOException
    {
        super(encoding);
    }

    public JcaPKCS10CertificationRequestHolder(PKCS10CertificationRequestHolder requestHolder)
    {
        super(requestHolder.toASN1Structure());
    }

    public JcaPKCS10CertificationRequestHolder setProvider(String providerName)
    {
        helper = new NamedJcaJceHelper(providerName);

        return this;
    }

    public JcaPKCS10CertificationRequestHolder setProvider(Provider provider)
    {
        helper = new ProviderJcaJceHelper(provider);

        return this;
    }

    public PublicKey getPublicKey()
        throws InvalidKeyException, NoSuchAlgorithmException
    {
        try
        {
            SubjectPublicKeyInfo keyInfo = this.getSubjectPublicKeyInfo();
            X509EncodedKeySpec xspec = new X509EncodedKeySpec(keyInfo.getEncoded());
            KeyFactory kFact;

            try
            {
                kFact = helper.createKeyFactory(keyInfo.getAlgorithmId().getAlgorithm().getId());
            }
            catch (NoSuchAlgorithmException e)
            {
                //
                // try an alternate
                //
                if (keyAlgorithms.get(keyInfo.getAlgorithmId().getAlgorithm()) != null)
                {
                    String  keyAlgorithm = (String)keyAlgorithms.get(keyInfo.getAlgorithmId().getAlgorithm());

                    kFact = helper.createKeyFactory(keyAlgorithm);
                }
                else
                {
                    throw e;
                }
            }

            return kFact.generatePublic(xspec);
        }
        catch (InvalidKeySpecException e)
        {
            throw new InvalidKeyException("error decoding public key");
        }
        catch (IOException e)
        {
            throw new InvalidKeyException("error extracting key encoding");
        }
        catch (NoSuchProviderException e)
        {
            throw new NoSuchAlgorithmException("cannot find provider: " + e.getMessage());
        }
    }
}
