package old.org.bouncycastle.cms;

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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import old.org.bouncycastle.asn1.ASN1EncodableVector;
import old.org.bouncycastle.asn1.ASN1ObjectIdentifier;
import old.org.bouncycastle.asn1.ASN1Set;
import old.org.bouncycastle.asn1.BERSequenceGenerator;
import old.org.bouncycastle.asn1.BERSet;
import old.org.bouncycastle.asn1.DERInteger;
import old.org.bouncycastle.asn1.DEROctetString;
import old.org.bouncycastle.asn1.DERSet;
import old.org.bouncycastle.asn1.DERTaggedObject;
import old.org.bouncycastle.asn1.cms.AuthenticatedData;
import old.org.bouncycastle.asn1.cms.CMSObjectIdentifiers;
import old.org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import old.org.bouncycastle.operator.DigestCalculator;
import old.org.bouncycastle.operator.GenericKey;
import old.org.bouncycastle.operator.MacCalculator;
import old.org.bouncycastle.util.io.TeeOutputStream;

/**
 * General class for generating a CMS authenticated-data message stream.
 * <p>
 * A simple example of usage.
 * <pre>
 *      CMSAuthenticatedDataStreamGenerator edGen = new CMSAuthenticatedDataStreamGenerator();
 *
 *      edGen.addRecipientInfoGenerator(new JceKeyTransRecipientInfoGenerator(cert).setProvider("BC"));
 *
 *      ByteArrayOutputStream  bOut = new ByteArrayOutputStream();
 *
 *      OutputStream out = edGen.open(
 *                              bOut, new JceCMSMacCalculatorBuilder(CMSAlgorithm.DES_EDE3_CBC).setProvider("BC").build());*
 *      out.write(data);
 *
 *      out.close();
 * </pre>
 */
public class CMSAuthenticatedDataStreamGenerator
    extends CMSAuthenticatedGenerator
{
    // Currently not handled
//    private Object              _originatorInfo = null;
//    private Object              _unprotectedAttributes = null;
    private int bufferSize;
    private boolean berEncodeRecipientSet;
    private MacCalculator macCalculator;

    /**
     * base constructor
     */
    public CMSAuthenticatedDataStreamGenerator()
    {
    }

    /**
     * Set the underlying string size for encapsulated data
     *
     * @param bufferSize length of octet strings to buffer the data.
     */
    public void setBufferSize(
        int bufferSize)
    {
        this.bufferSize = bufferSize;
    }

    /**
     * Use a BER Set to store the recipient information. By default recipients are
     * stored in a DER encoding.
     *
     * @param useBerEncodingForRecipients true if a BER set should be used, false if DER.
     */
    public void setBEREncodeRecipients(
        boolean useBerEncodingForRecipients)
    {
        berEncodeRecipientSet = useBerEncodingForRecipients;
    }

    /**
     * generate an authenticated data structure with the encapsulated bytes marked as DATA.
     *
     * @param out the stream to store the authenticated structure in.
     * @param macCalculator calculator for the MAC to be attached to the data.
     */
    public OutputStream open(
        OutputStream    out,
        MacCalculator   macCalculator)
        throws CMSException
    {
        return open(CMSObjectIdentifiers.data, out, macCalculator);
    }

    public OutputStream open(
        OutputStream    out,
        MacCalculator   macCalculator,
        DigestCalculator digestCalculator)
        throws CMSException
    {
        return open(CMSObjectIdentifiers.data, out, macCalculator, digestCalculator);
    }

    /**
     * generate an authenticated data structure with the encapsulated bytes marked as type dataType.
     *
     * @param dataType the type of the data been written to the object.
     * @param out the stream to store the authenticated structure in.
     * @param macCalculator calculator for the MAC to be attached to the data.
     */
    public OutputStream open(
        ASN1ObjectIdentifier dataType,
        OutputStream         out,
        MacCalculator        macCalculator)
        throws CMSException
    {
        return open(dataType, out, macCalculator, null);
    }

    /**
     * generate an authenticated data structure with the encapsulated bytes marked as type dataType.
     *
     * @param dataType the type of the data been written to the object.
     * @param out the stream to store the authenticated structure in.
     * @param macCalculator calculator for the MAC to be attached to the data.
     * @param digestCalculator calculator for computing digest of the encapsulated data.
     */
    public OutputStream open(
        ASN1ObjectIdentifier dataType,
        OutputStream         out,
        MacCalculator        macCalculator,
        DigestCalculator     digestCalculator)
        throws CMSException
    {
        this.macCalculator = macCalculator;

        try
        {
            ASN1EncodableVector recipientInfos = new ASN1EncodableVector();

            for (Iterator it = recipientInfoGenerators.iterator(); it.hasNext();)
            {
                RecipientInfoGenerator recipient = (RecipientInfoGenerator)it.next();

                recipientInfos.add(recipient.generate(macCalculator.getKey()));
            }

            //
            // ContentInfo
            //
            BERSequenceGenerator cGen = new BERSequenceGenerator(out);

            cGen.addObject(CMSObjectIdentifiers.authenticatedData);

            //
            // Authenticated Data
            //
            BERSequenceGenerator authGen = new BERSequenceGenerator(cGen.getRawOutputStream(), 0, true);

            authGen.addObject(new DERInteger(AuthenticatedData.calculateVersion(null)));

            if (berEncodeRecipientSet)
            {
                authGen.getRawOutputStream().write(new BERSet(recipientInfos).getEncoded());
            }
            else
            {
                authGen.getRawOutputStream().write(new DERSet(recipientInfos).getEncoded());
            }

            AlgorithmIdentifier macAlgId = macCalculator.getAlgorithmIdentifier();

            authGen.getRawOutputStream().write(macAlgId.getEncoded());

            if (digestCalculator != null)
            {
                authGen.addObject(new DERTaggedObject(false, 1, digestCalculator.getAlgorithmIdentifier()));
            }
            
            BERSequenceGenerator eiGen = new BERSequenceGenerator(authGen.getRawOutputStream());

            eiGen.addObject(dataType);

            OutputStream octetStream = CMSUtils.createBEROctetOutputStream(
                    eiGen.getRawOutputStream(), 0, false, bufferSize);

            OutputStream mOut;

            if (digestCalculator != null)
            {
                mOut = new TeeOutputStream(octetStream, digestCalculator.getOutputStream());
            }
            else
            {
                mOut = new TeeOutputStream(octetStream, macCalculator.getOutputStream());
            }

            return new CmsAuthenticatedDataOutputStream(macCalculator, digestCalculator, dataType, mOut, cGen, authGen, eiGen);
        }
        catch (IOException e)
        {
            throw new CMSException("exception decoding algorithm parameters.", e);
        }
    }

    private class CmsAuthenticatedDataOutputStream
        extends OutputStream
    {
        private OutputStream dataStream;
        private BERSequenceGenerator cGen;
        private BERSequenceGenerator envGen;
        private BERSequenceGenerator eiGen;
        private MacCalculator macCalculator;
        private DigestCalculator digestCalculator;
        private ASN1ObjectIdentifier contentType;

        public CmsAuthenticatedDataOutputStream(
            MacCalculator   macCalculator,
            DigestCalculator digestCalculator,
            ASN1ObjectIdentifier contentType,
            OutputStream dataStream,
            BERSequenceGenerator cGen,
            BERSequenceGenerator envGen,
            BERSequenceGenerator eiGen)
        {
            this.macCalculator = macCalculator;
            this.digestCalculator = digestCalculator;
            this.contentType = contentType;
            this.dataStream = dataStream;
            this.cGen = cGen;
            this.envGen = envGen;
            this.eiGen = eiGen;
        }

        public void write(
            int b)
            throws IOException
        {
            dataStream.write(b);
        }

        public void write(
            byte[] bytes,
            int    off,
            int    len)
            throws IOException
        {
            dataStream.write(bytes, off, len);
        }

        public void write(
            byte[] bytes)
            throws IOException
        {
            dataStream.write(bytes);
        }

        public void close()
            throws IOException
        {
            dataStream.close();
            eiGen.close();

            Map parameters;

            if (digestCalculator != null)
            {
                parameters = Collections.unmodifiableMap(getBaseParameters(contentType, digestCalculator.getAlgorithmIdentifier(), digestCalculator.getDigest()));

                if (authGen == null)
                {
                    authGen = new DefaultAuthenticatedAttributeTableGenerator();
                }
                
                ASN1Set authed = new DERSet(authGen.getAttributes(parameters).toASN1EncodableVector());

                OutputStream mOut = macCalculator.getOutputStream();

                mOut.write(authed.getDEREncoded());

                mOut.close();

                envGen.addObject(new DERTaggedObject(false, 2, authed));
            }
            else
            {
                parameters = Collections.unmodifiableMap(new HashMap());                
            }

            envGen.addObject(new DEROctetString(macCalculator.getMac()));

            if (unauthGen != null)
            {
                envGen.addObject(new DERTaggedObject(false, 3, new BERSet(unauthGen.getAttributes(parameters).toASN1EncodableVector())));
            }

            envGen.close();
            cGen.close();
        }
    }


    /**
     * constructor allowing specific source of randomness
     * @param rand instance of SecureRandom to use
     * @deprecated no longer of any use, use basic constructor.
     */
    public CMSAuthenticatedDataStreamGenerator(
        SecureRandom rand)
    {
        super(rand);
    }

    private class OldCmsAuthenticatedDataOutputStream
        extends OutputStream
    {
        private OutputStream dataStream;
        private Mac mac;
        private BERSequenceGenerator cGen;
        private BERSequenceGenerator envGen;
        private BERSequenceGenerator eiGen;

        public OldCmsAuthenticatedDataOutputStream(
            OutputStream dataStream,
            Mac mac,
            BERSequenceGenerator cGen,
            BERSequenceGenerator envGen,
            BERSequenceGenerator eiGen)
        {
            this.dataStream = dataStream;
            this.mac = mac;
            this.cGen = cGen;
            this.envGen = envGen;
            this.eiGen = eiGen;
        }

        public void write(
            int b)
            throws IOException
        {
            dataStream.write(b);
        }

        public void write(
            byte[] bytes,
            int    off,
            int    len)
            throws IOException
        {
            dataStream.write(bytes, off, len);
        }

        public void write(
            byte[] bytes)
            throws IOException
        {
            dataStream.write(bytes);
        }

        public void close()
            throws IOException
        {
            dataStream.close();
            eiGen.close();

            // [TODO] auth attributes go here
            envGen.addObject(new DEROctetString(mac.doFinal()));
            // [TODO] unauth attributes go here

            envGen.close();
            cGen.close();
        }
    }

    /**
     * generate an enveloped object that contains an CMS Enveloped Data
     * object using the given provider and the passed in key generator.
     * @throws java.io.IOException
     * @deprecated
     */
    private OutputStream open(
        OutputStream out,
        String       macOID,
        KeyGenerator keyGen,
        Provider     provider)
        throws NoSuchAlgorithmException, CMSException
    {
        Provider            encProvider = keyGen.getProvider();
        SecretKey           encKey = keyGen.generateKey();
        AlgorithmParameterSpec params = generateParameterSpec(macOID, encKey, encProvider);

        Iterator it = oldRecipientInfoGenerators.iterator();
        ASN1EncodableVector recipientInfos = new ASN1EncodableVector();

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

        return open(out, macOID, encKey, params, recipientInfos, encProvider);
    }

    protected OutputStream open(
        OutputStream        out,
        String              macOID,
        SecretKey           encKey,
        AlgorithmParameterSpec params,
        ASN1EncodableVector recipientInfos,
        String              provider)
        throws NoSuchAlgorithmException, NoSuchProviderException, CMSException
    {
        return open(out, macOID, encKey, params, recipientInfos, CMSUtils.getProvider(provider));
    }

    /**
     * @deprecated
     */
    protected OutputStream open(
        OutputStream        out,
        String              macOID,
        SecretKey           encKey,
        AlgorithmParameterSpec params,
        ASN1EncodableVector recipientInfos,
        Provider            provider)
        throws NoSuchAlgorithmException, CMSException
    {
        try
        {
            //
            // ContentInfo
            //
            BERSequenceGenerator cGen = new BERSequenceGenerator(out);

            cGen.addObject(CMSObjectIdentifiers.authenticatedData);

            //
            // Authenticated Data
            //
            BERSequenceGenerator authGen = new BERSequenceGenerator(cGen.getRawOutputStream(), 0, true);

            authGen.addObject(new DERInteger(AuthenticatedData.calculateVersion(null)));

            if (berEncodeRecipientSet)
            {
                authGen.getRawOutputStream().write(new BERSet(recipientInfos).getEncoded());
            }
            else
            {
                authGen.getRawOutputStream().write(new DERSet(recipientInfos).getEncoded());
            }

            Mac mac = CMSEnvelopedHelper.INSTANCE.getMac(macOID, provider);

            mac.init(encKey, params);

            AlgorithmIdentifier macAlgId = getAlgorithmIdentifier(macOID, params, provider);

            authGen.getRawOutputStream().write(macAlgId.getEncoded());

            BERSequenceGenerator eiGen = new BERSequenceGenerator(authGen.getRawOutputStream());

            eiGen.addObject(CMSObjectIdentifiers.data);

            OutputStream octetStream = CMSUtils.createBEROctetOutputStream(
                    eiGen.getRawOutputStream(), 0, false, bufferSize);

            OutputStream mOut = new TeeOutputStream(octetStream, new MacOutputStream(mac));

            return new OldCmsAuthenticatedDataOutputStream(mOut, mac, cGen, authGen, eiGen);
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
            throw new CMSException("algorithm parameter invalid.", e);
        }
        catch (InvalidParameterSpecException e)
        {
            throw new CMSException("algorithm parameter spec invalid.", e);
        }
        catch (IOException e)
        {
            throw new CMSException("exception decoding algorithm parameters.", e);
        }
    }

    /**
     * generate an authenticated object that contains an CMS Authenticated Data
     * object using the given provider.
     * @throws java.io.IOException
     * @deprecated use open(out, MacCalculator)
     */
    public OutputStream open(
        OutputStream    out,
        String          encryptionOID,
        String          provider)
        throws NoSuchAlgorithmException, NoSuchProviderException, CMSException, IOException
    {
        return open(out, encryptionOID, CMSUtils.getProvider(provider));
    }

    /**
     * @deprecated use open(out, MacCalculator)
     */
    public OutputStream open(
        OutputStream    out,
        String          encryptionOID,
        Provider        provider)
        throws NoSuchAlgorithmException, CMSException, IOException
    {
        KeyGenerator keyGen = CMSEnvelopedHelper.INSTANCE.createSymmetricKeyGenerator(encryptionOID, provider);

        keyGen.init(rand);

        return open(out, encryptionOID, keyGen, provider);
    }

    /**
     * generate an enveloped object that contains an CMS Enveloped Data
     * object using the given provider.
     * @deprecated use open(out, MacCalculator)
     */
    public OutputStream open(
        OutputStream    out,
        String          encryptionOID,
        int             keySize,
        String          provider)
        throws NoSuchAlgorithmException, NoSuchProviderException, CMSException, IOException
    {
        return open(out, encryptionOID, keySize, CMSUtils.getProvider(provider));
    }

    /**
     * generate an enveloped object that contains an CMS Enveloped Data
     * object using the given provider.
     * @deprecated use open(out, MacCalculator)
     */
    public OutputStream open(
        OutputStream    out,
        String          encryptionOID,
        int             keySize,
        Provider        provider)
        throws NoSuchAlgorithmException, CMSException, IOException
    {
        KeyGenerator keyGen = CMSEnvelopedHelper.INSTANCE.createSymmetricKeyGenerator(encryptionOID, provider);

        keyGen.init(keySize, rand);

        return open(out, encryptionOID, keyGen, provider);
    }
}