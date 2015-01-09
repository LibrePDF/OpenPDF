package old.org.bouncycastle.cms;

import java.io.IOException;
import java.io.OutputStream;
import java.security.AlgorithmParameters;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Iterator;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import old.org.bouncycastle.asn1.ASN1EncodableVector;
import old.org.bouncycastle.asn1.ASN1ObjectIdentifier;
import old.org.bouncycastle.asn1.ASN1Set;
import old.org.bouncycastle.asn1.BERSequenceGenerator;
import old.org.bouncycastle.asn1.BERSet;
import old.org.bouncycastle.asn1.DERInteger;
import old.org.bouncycastle.asn1.DERSet;
import old.org.bouncycastle.asn1.DERTaggedObject;
import old.org.bouncycastle.asn1.cms.AttributeTable;
import old.org.bouncycastle.asn1.cms.CMSObjectIdentifiers;
import old.org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import old.org.bouncycastle.operator.GenericKey;
import old.org.bouncycastle.operator.OutputEncryptor;

/**
 * General class for generating a CMS enveloped-data message stream.
 * <p>
 * A simple example of usage.
 * <pre>
 *      CMSEnvelopedDataStreamGenerator edGen = new CMSEnvelopedDataStreamGenerator();
 *
 *      edGen.addRecipientInfoGenerator(new JceKeyTransRecipientInfoGenerator(recipientCert).setProvider("BC"));
 *
 *      ByteArrayOutputStream  bOut = new ByteArrayOutputStream();
 *      
 *      OutputStream out = edGen.open(
 *                              bOut, new JceCMSContentEncryptorBuilder(CMSAlgorithm.DES_EDE3_CBC)
 *                                              .setProvider("BC").build());
 *      out.write(data);
 *      
 *      out.close();
 * </pre>
 */
public class CMSEnvelopedDataStreamGenerator
    extends CMSEnvelopedGenerator
{
    private Object              _originatorInfo = null;
    private Object              _unprotectedAttributes = null;
    private int                 _bufferSize;
    private boolean             _berEncodeRecipientSet;

    /**
     * base constructor
     */
    public CMSEnvelopedDataStreamGenerator()
    {
    }

    /**
     * constructor allowing specific source of randomness
     * @param rand instance of SecureRandom to use
     */
    public CMSEnvelopedDataStreamGenerator(
        SecureRandom rand)
    {
        super(rand);
    }

    /**
     * Set the underlying string size for encapsulated data
     * 
     * @param bufferSize length of octet strings to buffer the data.
     */
    public void setBufferSize(
        int bufferSize)
    {
        _bufferSize = bufferSize;
    }

    /**
     * Use a BER Set to store the recipient information
     */
    public void setBEREncodeRecipients(
        boolean berEncodeRecipientSet)
    {
        _berEncodeRecipientSet = berEncodeRecipientSet;
    }

    private DERInteger getVersion()
    {
        if (_originatorInfo != null || _unprotectedAttributes != null)
        {
            return new DERInteger(2);
        }
        else
        {
            return new DERInteger(0);
        }
    }
    
    /**
     * generate an enveloped object that contains an CMS Enveloped Data
     * object using the given provider and the passed in key generator.
     * @throws IOException
     * @deprecated
     */
    private OutputStream open(
        OutputStream out,
        String       encryptionOID,
        KeyGenerator keyGen,
        Provider     provider)
        throws NoSuchAlgorithmException, CMSException
    {
        Provider            encProvider = keyGen.getProvider();
        SecretKey           encKey = keyGen.generateKey();
        AlgorithmParameters params = generateParameters(encryptionOID, encKey, encProvider);

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

        it = recipientInfoGenerators.iterator();

        while (it.hasNext())
        {
            RecipientInfoGenerator recipient = (RecipientInfoGenerator)it.next();

            recipientInfos.add(recipient.generate(new GenericKey(encKey)));
        }

        return open(out, encryptionOID, encKey, params, recipientInfos, encProvider);
    }

    private OutputStream doOpen(
        ASN1ObjectIdentifier dataType,
        OutputStream         out,
        OutputEncryptor encryptor)
        throws IOException, CMSException
    {
        ASN1EncodableVector recipientInfos = new ASN1EncodableVector();
        GenericKey encKey = encryptor.getKey();
        Iterator it = recipientInfoGenerators.iterator();

        while (it.hasNext())
        {
            RecipientInfoGenerator recipient = (RecipientInfoGenerator)it.next();

            recipientInfos.add(recipient.generate(encKey));
        }

        return open(dataType, out, recipientInfos, encryptor);
    }

    protected OutputStream open(
        ASN1ObjectIdentifier dataType,
        OutputStream         out,
        ASN1EncodableVector  recipientInfos,
        OutputEncryptor      encryptor)
        throws IOException
    {
        //
        // ContentInfo
        //
        BERSequenceGenerator cGen = new BERSequenceGenerator(out);

        cGen.addObject(CMSObjectIdentifiers.envelopedData);

        //
        // Encrypted Data
        //
        BERSequenceGenerator envGen = new BERSequenceGenerator(cGen.getRawOutputStream(), 0, true);

        envGen.addObject(getVersion());

        if (_berEncodeRecipientSet)
        {
            envGen.getRawOutputStream().write(new BERSet(recipientInfos).getEncoded());
        }
        else
        {
            envGen.getRawOutputStream().write(new DERSet(recipientInfos).getEncoded());
        }

        BERSequenceGenerator eiGen = new BERSequenceGenerator(envGen.getRawOutputStream());

        eiGen.addObject(dataType);

        AlgorithmIdentifier encAlgId = encryptor.getAlgorithmIdentifier();

        eiGen.getRawOutputStream().write(encAlgId.getEncoded());

        OutputStream octetStream = CMSUtils.createBEROctetOutputStream(
            eiGen.getRawOutputStream(), 0, false, _bufferSize);

        OutputStream cOut = encryptor.getOutputStream(octetStream);

        return new CmsEnvelopedDataOutputStream(cOut, cGen, envGen, eiGen);
    }

    protected OutputStream open(
        OutputStream        out,
        String              encryptionOID,
        SecretKey           encKey,
        AlgorithmParameters params,
        ASN1EncodableVector recipientInfos,
        String              provider)
        throws NoSuchAlgorithmException, NoSuchProviderException, CMSException
    {
        return open(out, encryptionOID, encKey, params, recipientInfos, CMSUtils.getProvider(provider));
    }

    protected OutputStream open(
        OutputStream        out,
        String              encryptionOID,
        SecretKey           encKey,
        AlgorithmParameters params,
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
            
            cGen.addObject(CMSObjectIdentifiers.envelopedData);
            
            //
            // Encrypted Data
            //
            BERSequenceGenerator envGen = new BERSequenceGenerator(cGen.getRawOutputStream(), 0, true);
            
            envGen.addObject(getVersion());

            if (_berEncodeRecipientSet)
            {
                envGen.getRawOutputStream().write(new BERSet(recipientInfos).getEncoded());
            }
            else
            {
                envGen.getRawOutputStream().write(new DERSet(recipientInfos).getEncoded());
            }

            BERSequenceGenerator eiGen = new BERSequenceGenerator(envGen.getRawOutputStream());
            
            eiGen.addObject(CMSObjectIdentifiers.data);

            Cipher cipher = CMSEnvelopedHelper.INSTANCE.createSymmetricCipher(encryptionOID, provider);

            cipher.init(Cipher.ENCRYPT_MODE, encKey, params, rand);

            //
            // If params are null we try and second guess on them as some providers don't provide
            // algorithm parameter generation explicitly but instead generate them under the hood.
            //
            if (params == null)
            {
                params = cipher.getParameters();
            }

            AlgorithmIdentifier encAlgId = getAlgorithmIdentifier(encryptionOID, params);
                        
            eiGen.getRawOutputStream().write(encAlgId.getEncoded());

            OutputStream octetStream = CMSUtils.createBEROctetOutputStream(
                eiGen.getRawOutputStream(), 0, false, _bufferSize);

            CipherOutputStream cOut = new CipherOutputStream(octetStream, cipher);

            return new CmsEnvelopedDataOutputStream(cOut, cGen, envGen, eiGen);
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
    }

    protected OutputStream open(
        OutputStream        out,
        ASN1EncodableVector recipientInfos,
        OutputEncryptor     encryptor)
        throws CMSException
    {
        try
        {
            //
            // ContentInfo
            //
            BERSequenceGenerator cGen = new BERSequenceGenerator(out);

            cGen.addObject(CMSObjectIdentifiers.envelopedData);

            //
            // Encrypted Data
            //
            BERSequenceGenerator envGen = new BERSequenceGenerator(cGen.getRawOutputStream(), 0, true);

            envGen.addObject(getVersion());

            if (_berEncodeRecipientSet)
            {
                envGen.getRawOutputStream().write(new BERSet(recipientInfos).getEncoded());
            }
            else
            {
                envGen.getRawOutputStream().write(new DERSet(recipientInfos).getEncoded());
            }

            BERSequenceGenerator eiGen = new BERSequenceGenerator(envGen.getRawOutputStream());

            eiGen.addObject(CMSObjectIdentifiers.data);

            AlgorithmIdentifier encAlgId = encryptor.getAlgorithmIdentifier();

            eiGen.getRawOutputStream().write(encAlgId.getEncoded());

            OutputStream octetStream = CMSUtils.createBEROctetOutputStream(
                eiGen.getRawOutputStream(), 0, false, _bufferSize);

            return new CmsEnvelopedDataOutputStream(encryptor.getOutputStream(octetStream), cGen, envGen, eiGen);
        }
        catch (IOException e)
        {
            throw new CMSException("exception decoding algorithm parameters.", e);
        }
    }

    /**
     * generate an enveloped object that contains an CMS Enveloped Data
     * object using the given provider.
     * @throws IOException
     * @deprecated
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
     * @deprecated
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
     * @deprecated
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
     * @deprecated
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

    /**
     * generate an enveloped object that contains an CMS Enveloped Data
     * object using the given encryptor.
     */
    public OutputStream open(
        OutputStream    out,
        OutputEncryptor encryptor)
        throws CMSException, IOException
    {
        return doOpen(new ASN1ObjectIdentifier(CMSObjectIdentifiers.data.getId()), out, encryptor);
    }

    /**
     * generate an enveloped object that contains an CMS Enveloped Data
     * object using the given encryptor and marking the data as being of the passed
     * in type.
     */
    public OutputStream open(
        ASN1ObjectIdentifier dataType,
        OutputStream         out,
        OutputEncryptor      encryptor)
        throws CMSException, IOException
    {
        return doOpen(dataType, out, encryptor);
    }

    private class CmsEnvelopedDataOutputStream
        extends OutputStream
    {
        private OutputStream   _out;
        private BERSequenceGenerator _cGen;
        private BERSequenceGenerator _envGen;
        private BERSequenceGenerator _eiGen;
    
        public CmsEnvelopedDataOutputStream(
            OutputStream   out,
            BERSequenceGenerator cGen,
            BERSequenceGenerator envGen,
            BERSequenceGenerator eiGen)
        {
            _out = out;
            _cGen = cGen;
            _envGen = envGen;
            _eiGen = eiGen;
        }
    
        public void write(
            int b)
            throws IOException
        {
            _out.write(b);
        }
        
        public void write(
            byte[] bytes,
            int    off,
            int    len)
            throws IOException
        {
            _out.write(bytes, off, len);
        }
        
        public void write(
            byte[] bytes)
            throws IOException
        {
            _out.write(bytes);
        }
        
        public void close()
            throws IOException
        {
            _out.close();
            _eiGen.close();

            if (unprotectedAttributeGenerator != null)
            {
                AttributeTable attrTable = unprotectedAttributeGenerator.getAttributes(new HashMap());
      
                ASN1Set unprotectedAttrs = new BERSet(attrTable.toASN1EncodableVector());

                _envGen.addObject(new DERTaggedObject(false, 1, unprotectedAttrs));
            }
    
            _envGen.close();
            _cGen.close();
        }
    }
}
