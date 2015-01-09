package old.org.bouncycastle.pkcs;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import old.org.bouncycastle.asn1.ASN1Encodable;
import old.org.bouncycastle.asn1.ASN1EncodableVector;
import old.org.bouncycastle.asn1.ASN1ObjectIdentifier;
import old.org.bouncycastle.asn1.DERBitString;
import old.org.bouncycastle.asn1.DERSet;
import old.org.bouncycastle.asn1.pkcs.Attribute;
import old.org.bouncycastle.asn1.pkcs.CertificationRequest;
import old.org.bouncycastle.asn1.pkcs.CertificationRequestInfo;
import old.org.bouncycastle.asn1.x500.X500Name;
import old.org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import old.org.bouncycastle.operator.ContentSigner;

/**
 * A class for creating PKCS#10 Certification requests.
 * <pre>
 * CertificationRequest ::= SEQUENCE {
 *   certificationRequestInfo  CertificationRequestInfo,
 *   signatureAlgorithm        AlgorithmIdentifier{{ SignatureAlgorithms }},
 *   signature                 BIT STRING
 * }
 *
 * CertificationRequestInfo ::= SEQUENCE {
 *   version             INTEGER { v1(0) } (v1,...),
 *   subject             Name,
 *   subjectPKInfo   SubjectPublicKeyInfo{{ PKInfoAlgorithms }},
 *   attributes          [0] Attributes{{ CRIAttributes }}
 *  }
 *
 *  Attributes { ATTRIBUTE:IOSet } ::= SET OF Attribute{{ IOSet }}
 *
 *  Attribute { ATTRIBUTE:IOSet } ::= SEQUENCE {
 *    type    ATTRIBUTE.&id({IOSet}),
 *    values  SET SIZE(1..MAX) OF ATTRIBUTE.&Type({IOSet}{\@type})
 *  }
 * </pre>
 */
public class PKCS10CertificationRequestBuilder
{
    private SubjectPublicKeyInfo publicKeyInfo;
    private X500Name subject;
    private List attributes = new ArrayList();

    /**
     * Basic constructor.
     *
     * @param subject the X.500 Name defining the certificate subject this request is for.
     * @param publicKeyInfo the info structure for the public key to be associated with this subject.
     */
    public PKCS10CertificationRequestBuilder(X500Name subject, SubjectPublicKeyInfo publicKeyInfo)
    {
        this.subject = subject;
        this.publicKeyInfo = publicKeyInfo;
    }

    /**
     * Add an attribute to the certification request we are building.
     *
     * @param attrType the OID giving the type of the attribute.
     * @param attrValue the ASN.1 structure that forms the value of the attribute.
     * @return this builder object.
     */
    public PKCS10CertificationRequestBuilder addAttribute(ASN1ObjectIdentifier attrType, ASN1Encodable attrValue)
    {
        attributes.add(new Attribute(attrType, new DERSet(attrValue)));

        return this;
    }

    /**
     * Add an attribute with multiple values to the certification request we are building.
     *
     * @param attrType the OID giving the type of the attribute.
     * @param attrValues an array of ASN.1 structures that form the value of the attribute.
     * @return this builder object.
     */
    public PKCS10CertificationRequestBuilder addAttribute(ASN1ObjectIdentifier attrType, ASN1Encodable[] attrValues)
    {
        attributes.add(new Attribute(attrType, new DERSet(attrValues)));

        return this;
    }

    /**
     * Generate an PKCS#10 request based on the past in signer.
     *
     * @param signer the content signer to be used to generate the signature validating the certificate.
     * @return a holder containing the resulting PKCS#10 certification request.
     */
    public PKCS10CertificationRequestHolder build(
        ContentSigner signer)
    {
        CertificationRequestInfo info;

        if (attributes.isEmpty())
        {
            info = new CertificationRequestInfo(subject, publicKeyInfo, null);
        }
        else
        {
            ASN1EncodableVector v = new ASN1EncodableVector();

            for (Iterator it = attributes.iterator(); it.hasNext();)
            {
                v.add(Attribute.getInstance(it.next()));
            }

            info = new CertificationRequestInfo(subject, publicKeyInfo, new DERSet(v));
        }

        try
        {
            OutputStream sOut = signer.getOutputStream();

            sOut.write(info.getDEREncoded());

            sOut.close();

            return new PKCS10CertificationRequestHolder(new CertificationRequest(info, signer.getAlgorithmIdentifier(), new DERBitString(signer.getSignature())));
        }
        catch (IOException e)
        {
            throw new IllegalStateException("cannot produce certification request signature");
        }
    }
}
