package old.org.bouncycastle.cms;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.InvalidParameterSpecException;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import old.org.bouncycastle.asn1.ASN1EncodableVector;
import old.org.bouncycastle.asn1.ASN1OctetString;
import old.org.bouncycastle.asn1.ASN1Set;
import old.org.bouncycastle.asn1.BERConstructedOctetString;
import old.org.bouncycastle.asn1.BERSet;
import old.org.bouncycastle.asn1.DEROctetString;
import old.org.bouncycastle.asn1.DERSet;
import old.org.bouncycastle.asn1.cms.AuthenticatedData;
import old.org.bouncycastle.asn1.cms.CMSObjectIdentifiers;
import old.org.bouncycastle.asn1.cms.ContentInfo;
import old.org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import old.org.bouncycastle.operator.DigestCalculator;
import old.org.bouncycastle.operator.DigestCalculatorProvider;
import old.org.bouncycastle.operator.GenericKey;
import old.org.bouncycastle.operator.MacCalculator;
import old.org.bouncycastle.operator.OperatorCreationException;
import old.org.bouncycastle.util.io.TeeOutputStream;

/**
 * General class for generating a CMS authenticated-data message.
 *
 * A simple example of usage.
 *
 * <pre>
 *      CMSAuthenticatedDataGenerator  fact = new CMSAuthenticatedDataGenerator();
 *
 *      adGen.addRecipientInfoGenerator(new JceKeyTransRecipientInfoGenerator(recipientCert).setProvider("BC"));
 *
 *      CMSAuthenticatedData         data = fact.generate(new CMSProcessableByteArray(data),
 *                              new JceCMSMacCalculatorBuilder(CMSAlgorithm.DES_EDE3_CBC).setProvider(BC).build()));
 * </pre>
 */
public class CMSAuthenticatedDataGenerator
    extends CMSAuthenticatedGenerator
{
    /**
     * base constructor
     */
    public CMSAuthenticatedDataGenerator()
    {
    }

    /**
     * Generate an authenticated data object from the passed in typedData and MacCalculator.
     *
     * @param typedData the data to have a MAC attached.
     * @param macCalculator the calculator of the MAC to be attached.
     * @return the resulting CMSAuthenticatedData object.
     * @throws CMSException on failure in encoding data or processing recipients.
     */
    public CMSAuthenticatedData generate(CMSTypedData typedData, MacCalculator macCalculator)
        throws CMSException
    {
        return generate(typedData, macCalculator, null);
    }

    /**
     * Generate an authenticated data object from the passed in typedData and MacCalculator.
     *
     * @param typedData the data to have a MAC attached.
     * @param macCalculator the calculator of the MAC to be attached.
     * @param digestCalculator calculator for computing digest of the encapsulated data.
     * @return the resulting CMSAuthenticatedData object.
     * @throws CMSException on failure in encoding data or processing recipients.    
     */
    public CMSAuthenticatedData generate(CMSTypedData typedData, MacCalculator macCalculator, final DigestCalculator digestCalculator)
        throws CMSException
    {
        ASN1EncodableVector     recipientInfos = new ASN1EncodableVector();
        ASN1OctetString         encContent;
        ASN1OctetString         macResult;

        for (Iterator it = recipientInfoGenerators.iterator(); it.hasNext();)
        {
            RecipientInfoGenerator recipient = (RecipientInfoGenerator)it.next();

            recipientInfos.add(recipient.generate(macCalculator.getKey()));
        }

        AuthenticatedData authData;

        if (digestCalculator != null)
        {
            try
            {
                ByteArrayOutputStream bOut = new ByteArrayOutputStream();
                OutputStream out = new TeeOutputStream(digestCalculator.getOutputStream(), bOut);

                typedData.write(out);

                out.close();

                encContent = new BERConstructedOctetString(bOut.toByteArray());
            }
            catch (IOException e)
            {
                throw new CMSException("unable to perform digest calculation: " + e.getMessage(), e);
            }

            Map parameters = getBaseParameters(typedData.getContentType(), digestCalculator.getAlgorithmIdentifier(), digestCalculator.getDigest());

            if (authGen == null)
            {
                authGen = new DefaultAuthenticatedAttributeTableGenerator();
            }
            ASN1Set authed = new DERSet(authGen.getAttributes(Collections.unmodifiableMap(parameters)).toASN1EncodableVector());

            try
            {
                OutputStream mOut = macCalculator.getOutputStream();

                mOut.write(authed.getDEREncoded());

                mOut.close();

                macResult = new DEROctetString(macCalculator.getMac());
            }
            catch (IOException e)
            {
                throw new CMSException("exception decoding algorithm parameters.", e);
            }
            ASN1Set unauthed = (unauthGen != null) ? new BERSet(unauthGen.getAttributes(Collections.unmodifiableMap(parameters)).toASN1EncodableVector()) : null;

            ContentInfo  eci = new ContentInfo(
                            CMSObjectIdentifiers.data,
                            encContent);

            authData = new AuthenticatedData(null, new DERSet(recipientInfos), macCalculator.getAlgorithmIdentifier(), digestCalculator.getAlgorithmIdentifier(), eci, authed, macResult, unauthed);
        }
        else
        {
            try
            {
                ByteArrayOutputStream   bOut = new ByteArrayOutputStream();
                OutputStream mOut = new TeeOutputStream(bOut, macCalculator.getOutputStream());

                typedData.write(mOut);

                mOut.close();

                encContent = new BERConstructedOctetString(bOut.toByteArray());

                macResult = new DEROctetString(macCalculator.getMac());
            }
            catch (IOException e)
            {
                throw new CMSException("exception decoding algorithm parameters.", e);
            }

            ASN1Set unauthed = (unauthGen != null) ? new BERSet(unauthGen.getAttributes(Collections.EMPTY_MAP).toASN1EncodableVector()) : null;

            ContentInfo  eci = new ContentInfo(
                            CMSObjectIdentifiers.data,
                            encContent);

            authData = new AuthenticatedData(null, new DERSet(recipientInfos), macCalculator.getAlgorithmIdentifier(), null, eci, null, macResult, unauthed);
        }

        ContentInfo contentInfo = new ContentInfo(
                CMSObjectIdentifiers.authenticatedData, authData);

        return new CMSAuthenticatedData(contentInfo, new DigestCalculatorProvider()
        {
            public DigestCalculator get(AlgorithmIdentifier digestAlgorithmIdentifier)
                throws OperatorCreationException
            {
                return digestCalculator;
            }
        });
    }

    /**
     * constructor allowing specific source of randomness
     * @param rand instance of SecureRandom to use
     * @deprecated no longer required, use simple constructor.
     */
    public CMSAuthenticatedDataGenerator(
        SecureRandom rand)
    {
        super(rand);
    }

    /**
     * generate an authenticated object that contains an CMS Authenticated Data
     * object using the given provider and the passed in key generator.
     * @deprecated
     */
    private CMSAuthenticatedData generate(
        CMSProcessable  content,
        String          macOID,
        KeyGenerator    keyGen,
        Provider        provider)
        throws NoSuchAlgorithmException, CMSException
    {
        Provider                encProvider = keyGen.getProvider();
        ASN1EncodableVector     recipientInfos = new ASN1EncodableVector();
        AlgorithmIdentifier     macAlgId;
        SecretKey               encKey;
        ASN1OctetString         encContent;
        ASN1OctetString         macResult;

        try
        {
            Mac mac = CMSEnvelopedHelper.INSTANCE.getMac(macOID, encProvider);

            AlgorithmParameterSpec params;

            encKey = keyGen.generateKey();
            params = generateParameterSpec(macOID, encKey, encProvider);

            mac.init(encKey, params);

            macAlgId = getAlgorithmIdentifier(macOID, params, encProvider);

            ByteArrayOutputStream   bOut = new ByteArrayOutputStream();
            OutputStream mOut = new TeeOutputStream(bOut, new MacOutputStream(mac));

            content.write(mOut);

            mOut.close();
            bOut.close();

            encContent = new BERConstructedOctetString(bOut.toByteArray());

            macResult = new DEROctetString(mac.doFinal());
        }
        catch (InvalidKeyException e)
        {
            throw new CMSException("key invalid in message.", e);
        }
        catch (NoSuchPaddingException e)
        {
            throw new CMSException("required padding not supported.", e);
        }
        catch (InvalidAlgorithmParameterException e)
        {
            throw new CMSException("algorithm parameters invalid.", e);
        }
        catch (IOException e)
        {
            throw new CMSException("exception decoding algorithm parameters.", e);
        }
        catch (InvalidParameterSpecException e)
        {
           throw new CMSException("exception setting up parameters.", e);
        }

        Iterator it = oldRecipientInfoGenerators.iterator();

        while (it.hasNext())
        {
            IntRecipientInfoGenerator recipient = (IntRecipientInfoGenerator)it.next();

            try
            {
                recipientInfos.add(recipient.generate(encKey, rand, provider));
            }
            catch (InvalidKeyException e)
            {
                throw new CMSException("key inappropriate for algorithm.", e);
            }
            catch (GeneralSecurityException e)
            {
                throw new CMSException("error making encrypted content.", e);
            }
        }

        for (it = recipientInfoGenerators.iterator(); it.hasNext();)
        {
            RecipientInfoGenerator recipient = (RecipientInfoGenerator)it.next();

            recipientInfos.add(recipient.generate(new GenericKey(encKey)));
        }

        ContentInfo  eci = new ContentInfo(
                CMSObjectIdentifiers.data,
                encContent);

        ContentInfo contentInfo = new ContentInfo(
                CMSObjectIdentifiers.authenticatedData,
                new AuthenticatedData(null, new DERSet(recipientInfos), macAlgId, null, eci, null, macResult, null));

        return new CMSAuthenticatedData(contentInfo);
    }

    /**
     * generate an authenticated object that contains an CMS Authenticated Data
     * object using the given provider.
     * @deprecated use addRecipientInfoGenerator method.
     */
    public CMSAuthenticatedData generate(
        CMSProcessable  content,
        String          macOID,
        String          provider)
        throws NoSuchAlgorithmException, NoSuchProviderException, CMSException
    {
        return generate(content, macOID, CMSUtils.getProvider(provider));
    }

    /**
     * generate an authenticated object that contains an CMS Authenticated Data
     * object using the given provider
     * @deprecated use addRecipientInfoGenerator method..
     */
    public CMSAuthenticatedData generate(
        CMSProcessable  content,
        String          encryptionOID,
        Provider        provider)
        throws NoSuchAlgorithmException, CMSException
    {
        KeyGenerator keyGen = CMSEnvelopedHelper.INSTANCE.createSymmetricKeyGenerator(encryptionOID, provider);

        keyGen.init(rand);

        return generate(content, encryptionOID, keyGen, provider);
    }
}