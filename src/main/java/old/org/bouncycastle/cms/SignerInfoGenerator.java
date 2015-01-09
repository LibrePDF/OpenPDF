package old.org.bouncycastle.cms;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import old.org.bouncycastle.asn1.ASN1Encodable;
import old.org.bouncycastle.asn1.ASN1ObjectIdentifier;
import old.org.bouncycastle.asn1.ASN1Set;
import old.org.bouncycastle.asn1.DERNull;
import old.org.bouncycastle.asn1.DERObjectIdentifier;
import old.org.bouncycastle.asn1.DEROctetString;
import old.org.bouncycastle.asn1.DERSet;
import old.org.bouncycastle.asn1.cms.AttributeTable;
import old.org.bouncycastle.asn1.cms.SignerIdentifier;
import old.org.bouncycastle.asn1.cms.SignerInfo;
import old.org.bouncycastle.asn1.oiw.OIWObjectIdentifiers;
import old.org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import old.org.bouncycastle.asn1.teletrust.TeleTrusTObjectIdentifiers;
import old.org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import old.org.bouncycastle.cert.X509CertificateHolder;
import old.org.bouncycastle.operator.ContentSigner;
import old.org.bouncycastle.operator.DefaultDigestAlgorithmIdentifierFinder;
import old.org.bouncycastle.operator.DigestAlgorithmIdentifierFinder;
import old.org.bouncycastle.operator.DigestCalculator;
import old.org.bouncycastle.operator.DigestCalculatorProvider;
import old.org.bouncycastle.operator.OperatorCreationException;
import old.org.bouncycastle.util.io.TeeOutputStream;

public class SignerInfoGenerator
{
    private static final Set RSA_PKCS1d5 = new HashSet();

    static
    {
        RSA_PKCS1d5.add(PKCSObjectIdentifiers.md2WithRSAEncryption);
        RSA_PKCS1d5.add(PKCSObjectIdentifiers.md4WithRSAEncryption);
        RSA_PKCS1d5.add(PKCSObjectIdentifiers.md5WithRSAEncryption);
        RSA_PKCS1d5.add(PKCSObjectIdentifiers.sha1WithRSAEncryption);
        RSA_PKCS1d5.add(PKCSObjectIdentifiers.sha224WithRSAEncryption);
        RSA_PKCS1d5.add(PKCSObjectIdentifiers.sha256WithRSAEncryption);
        RSA_PKCS1d5.add(PKCSObjectIdentifiers.sha384WithRSAEncryption);
        RSA_PKCS1d5.add(PKCSObjectIdentifiers.sha512WithRSAEncryption);
        RSA_PKCS1d5.add(OIWObjectIdentifiers.md4WithRSAEncryption);
        RSA_PKCS1d5.add(OIWObjectIdentifiers.md4WithRSA);
        RSA_PKCS1d5.add(OIWObjectIdentifiers.md5WithRSA);
        RSA_PKCS1d5.add(OIWObjectIdentifiers.sha1WithRSA);
        RSA_PKCS1d5.add(TeleTrusTObjectIdentifiers.rsaSignatureWithripemd128);
        RSA_PKCS1d5.add(TeleTrusTObjectIdentifiers.rsaSignatureWithripemd160);
        RSA_PKCS1d5.add(TeleTrusTObjectIdentifiers.rsaSignatureWithripemd256);
    }

    private final SignerIdentifier signerIdentifier;
    private final CMSAttributeTableGenerator sAttrGen;
    private final CMSAttributeTableGenerator unsAttrGen;
    private final ContentSigner signer;
    private final DigestCalculator digester;
    private final DigestAlgorithmIdentifierFinder digAlgFinder = new DefaultDigestAlgorithmIdentifierFinder();

    private byte[] calculatedDigest = null;
    private X509CertificateHolder certHolder;

    public SignerInfoGenerator(
        SignerIdentifier signerIdentifier,
        ContentSigner signer,
        DigestCalculatorProvider digesterProvider)
        throws OperatorCreationException
    {
        this(signerIdentifier, signer, digesterProvider, false);
    }

    public SignerInfoGenerator(
        SignerIdentifier signerIdentifier,
        ContentSigner signer,
        DigestCalculatorProvider digesterProvider,
        boolean isDirectSignature)
        throws OperatorCreationException
    {
        this.signerIdentifier = signerIdentifier;
        this.signer = signer;

        if (digesterProvider != null)
        {
            this.digester = digesterProvider.get(digAlgFinder.find(signer.getAlgorithmIdentifier()));
        }
        else
        {
            this.digester = null;
        }

        if (isDirectSignature)
        {
            this.sAttrGen = null;
            this.unsAttrGen = null;
        }
        else
        {
            this.sAttrGen = new DefaultSignedAttributeTableGenerator();
            this.unsAttrGen = null;
        }
    }

    public SignerInfoGenerator(
        SignerInfoGenerator original,
        CMSAttributeTableGenerator sAttrGen,
        CMSAttributeTableGenerator unsAttrGen)
    {
        this.signerIdentifier = original.signerIdentifier;
        this.signer = original.signer;
        this.digester = original.digester;
        this.sAttrGen = sAttrGen;
        this.unsAttrGen = unsAttrGen;
    }

    public SignerInfoGenerator(
        SignerIdentifier signerIdentifier,
        ContentSigner signer,
        DigestCalculatorProvider digesterProvider,
        CMSAttributeTableGenerator sAttrGen,
        CMSAttributeTableGenerator unsAttrGen)
        throws OperatorCreationException
    {
        this.signerIdentifier = signerIdentifier;
        this.signer = signer;

        if (digesterProvider != null)
        {
            this.digester = digesterProvider.get(digAlgFinder.find(signer.getAlgorithmIdentifier()));
        }
        else
        {
            this.digester = null;
        }

        this.sAttrGen = sAttrGen;
        this.unsAttrGen = unsAttrGen;
    }

    public boolean hasAssociatedCertificate()
    {
        return certHolder != null;
    }

    public X509CertificateHolder getAssociatedCertificate()
    {
        return certHolder;
    }
    
    public AlgorithmIdentifier getDigestAlgorithm()
    {
        if (digester != null)
        {
            return digester.getAlgorithmIdentifier();
        }

        return digAlgFinder.find(signer.getAlgorithmIdentifier());
    }
    
    public OutputStream getCalculatingOutputStream()
    {
        if (digester != null)
        {
            if (sAttrGen == null)
            {
                return new TeeOutputStream(digester.getOutputStream(), signer.getOutputStream());    
            }
            return digester.getOutputStream();
        }
        else
        {
            return signer.getOutputStream();
        }
    }

    public SignerInfo generate(ASN1ObjectIdentifier contentType)
        throws CMSException
    {
        try
        {
            /* RFC 3852 5.4
             * The result of the message digest calculation process depends on
             * whether the signedAttrs field is present.  When the field is absent,
             * the result is just the message digest of the content as described
             *
             * above.  When the field is present, however, the result is the message
             * digest of the complete DER encoding of the SignedAttrs value
             * contained in the signedAttrs field.
             */
            ASN1Set signedAttr = null;

            AlgorithmIdentifier digestAlg = null;

            if (sAttrGen != null)
            {
                digestAlg = digester.getAlgorithmIdentifier();
                calculatedDigest = digester.getDigest();
                Map parameters = getBaseParameters(contentType, digester.getAlgorithmIdentifier(), calculatedDigest);
                AttributeTable signed = sAttrGen.getAttributes(Collections.unmodifiableMap(parameters));

                signedAttr = getAttributeSet(signed);

                // sig must be composed from the DER encoding.
                OutputStream sOut = signer.getOutputStream();

                sOut.write(signedAttr.getEncoded(ASN1Encodable.DER));

                sOut.close();
            }
            else
            {
                if (digester != null)
                {
                    digestAlg = digester.getAlgorithmIdentifier();
                    calculatedDigest = digester.getDigest();
                }
                else
                {
                    digestAlg = digAlgFinder.find(signer.getAlgorithmIdentifier());
                    calculatedDigest = null;
                }
            }

            byte[] sigBytes = signer.getSignature();

            ASN1Set unsignedAttr = null;
            if (unsAttrGen != null)
            {
                Map parameters = getBaseParameters(contentType, digestAlg, calculatedDigest);
                parameters.put(CMSAttributeTableGenerator.SIGNATURE, sigBytes.clone());

                AttributeTable unsigned = unsAttrGen.getAttributes(Collections.unmodifiableMap(parameters));

                unsignedAttr = getAttributeSet(unsigned);
            }

            AlgorithmIdentifier digestEncryptionAlgorithm = getSignatureAlgorithm(signer.getAlgorithmIdentifier());

            return new SignerInfo(signerIdentifier, digestAlg,
                signedAttr, digestEncryptionAlgorithm, new DEROctetString(sigBytes), unsignedAttr);
        }
        catch (IOException e)
        {
            throw new CMSException("encoding error.", e);
        }
    }

    void setAssociatedCertificate(X509CertificateHolder certHolder)
    {
        this.certHolder = certHolder;
    }

    private ASN1Set getAttributeSet(
        AttributeTable attr)
    {
        if (attr != null)
        {
            return new DERSet(attr.toASN1EncodableVector());
        }

        return null;
    }

    private Map getBaseParameters(DERObjectIdentifier contentType, AlgorithmIdentifier digAlgId, byte[] hash)
    {
        Map param = new HashMap();

        if (contentType != null)
        {
            param.put(CMSAttributeTableGenerator.CONTENT_TYPE, contentType);
        }

        param.put(CMSAttributeTableGenerator.DIGEST_ALGORITHM_IDENTIFIER, digAlgId);
        param.put(CMSAttributeTableGenerator.DIGEST,  hash.clone());
        return param;
    }

    private AlgorithmIdentifier getSignatureAlgorithm(AlgorithmIdentifier sigAlgID)
        throws IOException
    {
        // RFC3370 section 3.2
        if (RSA_PKCS1d5.contains(sigAlgID.getAlgorithm()))
        {
            return new AlgorithmIdentifier(PKCSObjectIdentifiers.rsaEncryption, DERNull.INSTANCE);
        }

        return sigAlgID;
    }

    public byte[] getCalculatedDigest()
    {
        if (calculatedDigest != null)
        {
            return (byte[])calculatedDigest.clone();
        }

        return null;
    }

    public CMSAttributeTableGenerator getSignedAttributeTableGenerator()
    {
        return sAttrGen;
    }

    public CMSAttributeTableGenerator getUnsignedAttributeTableGenerator()
    {
        return unsAttrGen;
    }
}
