package old.org.bouncycastle.cms;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.AlgorithmParameters;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;

import javax.crypto.Mac;
import javax.crypto.SecretKey;

import old.org.bouncycastle.asn1.DEREncodable;
import old.org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import old.org.bouncycastle.util.io.Streams;

public abstract class RecipientInformation
{
    protected RecipientId rid;
    protected AlgorithmIdentifier   keyEncAlg;
    protected AlgorithmIdentifier messageAlgorithm;
    private CMSSecureReadable     secureReadable;

    private AuthAttributesProvider additionalData;

    private byte[] resultMac;
    private RecipientOperator     operator;

    RecipientInformation(
        AlgorithmIdentifier     keyEncAlg,
        AlgorithmIdentifier     messageAlgorithm,
        CMSSecureReadable       secureReadable,
        AuthAttributesProvider  additionalData)
    {
        this.keyEncAlg = keyEncAlg;
        this.messageAlgorithm = messageAlgorithm;
        this.secureReadable = secureReadable;
        this.additionalData = additionalData;
    }

    String getContentAlgorithmName()
    {
        AlgorithmIdentifier algorithm = secureReadable.getAlgorithm();
        return CMSEnvelopedHelper.INSTANCE.getSymmetricCipherName(algorithm.getObjectId().getId());
    }

    public RecipientId getRID()
    {
        return rid;
    }

    private byte[] encodeObj(
        DEREncodable obj)
        throws IOException
    {
        if (obj != null)
        {
            return obj.getDERObject().getEncoded();
        }

        return null;
    }

    /**
     * return the object identifier for the key encryption algorithm.
     *
     * @return OID for key encryption algorithm.
     */
    public String getKeyEncryptionAlgOID()
    {
        return keyEncAlg.getObjectId().getId();
    }

    /**
     * return the ASN.1 encoded key encryption algorithm parameters, or null if
     * there aren't any.
     *
     * @return ASN.1 encoding of key encryption algorithm parameters.
     */
    public byte[] getKeyEncryptionAlgParams()
    {
        try
        {
            return encodeObj(keyEncAlg.getParameters());
        }
        catch (Exception e)
        {
            throw new RuntimeException("exception getting encryption parameters " + e);
        }
    }

    /**
     * Return an AlgorithmParameters object giving the encryption parameters
     * used to encrypt the key this recipient holds.
     *
     * @param provider the provider to generate the parameters for.
     * @return the parameters object, null if there is not one.
     * @throws CMSException            if the algorithm cannot be found, or the parameters can't be parsed.
     * @throws NoSuchProviderException if the provider cannot be found.
     */
    public AlgorithmParameters getKeyEncryptionAlgorithmParameters(
        String provider)
        throws CMSException, NoSuchProviderException
    {
        return getKeyEncryptionAlgorithmParameters(CMSUtils.getProvider(provider));
    }

    /**
     * Return an AlgorithmParameters object giving the encryption parameters
     * used to encrypt the key this recipient holds.
     *
     * @param provider the provider to generate the parameters for.
     * @return the parameters object, null if there is not one.
     * @throws CMSException if the algorithm cannot be found, or the parameters can't be parsed.
     */
    public AlgorithmParameters getKeyEncryptionAlgorithmParameters(
        Provider provider)
        throws CMSException
    {
        try
        {
            byte[] enc = this.encodeObj(keyEncAlg.getParameters());
            if (enc == null)
            {
                return null;
            }

            AlgorithmParameters params = CMSEnvelopedHelper.INSTANCE.createAlgorithmParameters(getKeyEncryptionAlgOID(), provider);

            params.init(enc, "ASN.1");

            return params;
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new CMSException("can't find parameters for algorithm", e);
        }
        catch (IOException e)
        {
            throw new CMSException("can't find parse parameters", e);
        }
    }

    protected CMSTypedStream getContentFromSessionKey(
        Key sKey,
        Provider provider)
        throws CMSException
    {
        CMSReadable readable = secureReadable.getReadable((SecretKey)sKey, provider); 

        try
        {
            return new CMSTypedStream(readable.getInputStream());
        }
        catch (IOException e)
        {
            throw new CMSException("error getting .", e);
        }
    }

    /**
     * @deprecated use getContent(Recipient)
     */
    public byte[] getContent(
        Key key,
        String provider)
        throws CMSException, NoSuchProviderException
    {
        return getContent(key, CMSUtils.getProvider(provider));
    }

    /**
     * @deprecated use getContent(Recipient)
     */
    public byte[] getContent(
        Key key,
        Provider provider)
        throws CMSException
    {
        try
        {
            return CMSUtils.streamToByteArray(getContentStream(key, provider).getContentStream());
        }
        catch (IOException e)
        {
            throw new RuntimeException("unable to parse internal stream: " + e);
        }
    }

    /**
     * Return the content digest calculated during the read of the content if one has been generated. This will
     * only happen if we are dealing with authenticated data and authenticated attributes are present.
     *
     * @return byte array containing the digest.
     */
    public byte[] getContentDigest()
    {
        if (secureReadable instanceof CMSEnvelopedHelper.CMSDigestAuthenticatedSecureReadable)
        {
            return ((CMSEnvelopedHelper.CMSDigestAuthenticatedSecureReadable)secureReadable).getDigest();
        }

        return null;
    }

    /**
     * Return the MAC calculated for the recipient. Note: this call is only meaningful once all
     * the content has been read.
     *
     * @return  byte array containing the mac.
     */
    public byte[] getMac()
    {
        if (resultMac == null)
        {
            if (operator != null)
            {
                if (operator.isMacBased())
                {
                    if (additionalData != null)
                    {
                        try
                        {
                            Streams.drain(operator.getInputStream(new ByteArrayInputStream(additionalData.getAuthAttributes().getDEREncoded())));
                        }
                        catch (IOException e)
                        {
                            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                        }
                    }
                    resultMac = operator.getMac();
                }
            }
            else
            {
                Object cryptoObject = secureReadable.getCryptoObject();
                if (cryptoObject instanceof Mac)
                {
                    resultMac = ((Mac)cryptoObject).doFinal();
                }
            }
        }

        return resultMac;
    }

    /**
     * Return the decrypted/encapsulated content in the EnvelopedData after recovering the content
     * encryption/MAC key using the passed in Recipient.
     *
     * @param recipient recipient object to use to recover content encryption key
     * @return  the content inside the EnvelopedData this RecipientInformation is associated with.
     * @throws CMSException if the content-encryption/MAC key cannot be recovered.
     */
    public byte[] getContent(
        Recipient recipient)
        throws CMSException
    {
        try
        {
            return CMSUtils.streamToByteArray(getContentStream(recipient).getContentStream());
        }
        catch (IOException e)
        {
            throw new CMSException("unable to parse internal stream: " + e.getMessage(), e);
        }
    }

    /**
     * decrypt the content and return it
     * @deprecated use getContentStream(Recipient) method
     */
    public CMSTypedStream getContentStream(Key key, String provider)
        throws CMSException, NoSuchProviderException
    {
        return getContentStream(key, CMSUtils.getProvider(provider));
    }

    /**
     * decrypt the content and return it
     * @deprecated use getContentStream(Recipient) method
     */
    public abstract CMSTypedStream getContentStream(Key key, Provider provider)
        throws CMSException;


    /**
     * Return a CMSTypedStream representing the content in the EnvelopedData after recovering the content
     * encryption/MAC key using the passed in Recipient.
     *
     * @param recipient recipient object to use to recover content encryption key
     * @return  the content inside the EnvelopedData this RecipientInformation is associated with.
     * @throws CMSException if the content-encryption/MAC key cannot be recovered.
     */
    public CMSTypedStream getContentStream(Recipient recipient)
        throws CMSException, IOException
    {
        operator = getRecipientOperator(recipient);

        if (additionalData != null)
        {
            return new CMSTypedStream(secureReadable.getInputStream());
        }

        return new CMSTypedStream(operator.getInputStream(secureReadable.getInputStream()));
    }

    protected abstract RecipientOperator getRecipientOperator(Recipient recipient)
        throws CMSException, IOException;
}
