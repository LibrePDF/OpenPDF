package old.org.bouncycastle.pkcs;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import old.org.bouncycastle.asn1.ASN1Object;
import old.org.bouncycastle.asn1.ASN1ObjectIdentifier;
import old.org.bouncycastle.asn1.ASN1Set;
import old.org.bouncycastle.asn1.pkcs.Attribute;
import old.org.bouncycastle.asn1.pkcs.CertificationRequest;
import old.org.bouncycastle.asn1.pkcs.CertificationRequestInfo;
import old.org.bouncycastle.asn1.x500.X500Name;
import old.org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import old.org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import old.org.bouncycastle.operator.ContentVerifier;
import old.org.bouncycastle.operator.ContentVerifierProvider;

/**
 * Holding class for a PKCS#10 certification request.
 */
public class PKCS10CertificationRequestHolder
{
    private static Attribute[] EMPTY_ARRAY = new Attribute[0];

    private CertificationRequest certificationRequest;

    private static CertificationRequest parseBytes(byte[] encoding)
        throws IOException
    {
        try
        {
            return CertificationRequest.getInstance(ASN1Object.fromByteArray(encoding));
        }
        catch (ClassCastException e)
        {
            throw new PKCSIOException("malformed data: " + e.getMessage(), e);
        }
        catch (IllegalArgumentException e)
        {
            throw new PKCSIOException("malformed data: " + e.getMessage(), e);
        }
    }

    /**
     * Create a PKCS10CertificationRequestHolder from an underlying ASN.1 structure.
     *
     * @param certificationRequest the underlying ASN.1 structure representing a request.
     */
    public PKCS10CertificationRequestHolder(CertificationRequest certificationRequest)
    {
         this.certificationRequest = certificationRequest;
    }

    /**
     * Create a PKCS10CertificationRequestHolder from the passed in bytes.
     *
     * @param encoded BER/DER encoding of the CertificationRequest structure.
     * @throws IOException in the event of corrupted data, or an incorrect structure.
     */
    public PKCS10CertificationRequestHolder(byte[] encoded)
        throws IOException
    {
        this(parseBytes(encoded));
    }

    /**
     * Return the underlying ASN.1 structure for this request.
     *
     * @return a CertificateRequest object.
     */
    public CertificationRequest toASN1Structure()
    {
         return certificationRequest;
    }

    /**
     * Return the subject on this request.
     *
     * @return the X500Name representing the request's subject.
     */
    public X500Name getSubject()
    {
        return X500Name.getInstance(certificationRequest.getCertificationRequestInfo().getSubject());
    }

    /**
     * Return the details of the signature algorithm used to create this request.
     *
     * @return the AlgorithmIdentifier describing the signature algorithm used to create this request.
     */
    public AlgorithmIdentifier getSignatureAlgorithm()
    {
        return certificationRequest.getSignatureAlgorithm();
    }

    /**
     * Return the bytes making up the signature associated with this request.
     *
     * @return the request signature bytes.
     */
    public byte[] getSignature()
    {
        return certificationRequest.getSignature().getBytes();
    }

    /**
     * Return the SubjectPublicKeyInfo describing the public key this request is carrying.
     *
     * @return the public key ASN.1 structure contained in the request.
     */
    public SubjectPublicKeyInfo getSubjectPublicKeyInfo()
    {
        return certificationRequest.getCertificationRequestInfo().getSubjectPublicKeyInfo();
    }

    /**
     * Return the attributes, if any associated with this request.
     *
     * @return an array of Attribute, zero length if none present.
     */
    public Attribute[] getAttributes()
    {
        ASN1Set attrSet = certificationRequest.getCertificationRequestInfo().getAttributes();

        if (attrSet == null)
        {
            return EMPTY_ARRAY;
        }

        Attribute[] attrs = new Attribute[attrSet.size()];

        for (int i = 0; i != attrSet.size(); i++)
        {
            attrs[i] = Attribute.getInstance(attrSet.getObjectAt(i));
        }

        return attrs;
    }

    /**
     * Return an  array of attributes matching the passed in type OID.
     *
     * @param type the type of the attribute being looked for.
     * @return an array of Attribute of the requested type, zero length if none present.
     */
    public Attribute[] getAttributes(ASN1ObjectIdentifier type)
    {
        ASN1Set    attrSet = certificationRequest.getCertificationRequestInfo().getAttributes();

        if (attrSet == null)
        {
            return EMPTY_ARRAY;
        }
        
        List list = new ArrayList();

        for (int i = 0; i != attrSet.size(); i++)
        {
            Attribute attr = Attribute.getInstance(attrSet.getObjectAt(i));
            if (attr.getAttrType().equals(type))
            {
                list.add(attr);
            }
        }

        if (list.size() == 0)
        {
            return EMPTY_ARRAY;
        }

        return (Attribute[])list.toArray(new Attribute[list.size()]);
    }

    public byte[] getEncoded()
        throws IOException
    {
        return certificationRequest.getEncoded();
    }

    /**
     * Validate the signature on the PKCS10 certification request in this holder.
     *
     * @param verifierProvider a ContentVerifierProvider that can generate a verifier for the signature.
     * @return true if the signature is valid, false otherwise.
     * @throws PKCSException if the signature cannot be processed or is inappropriate.
     */
    public boolean isSignatureValid(ContentVerifierProvider verifierProvider)
        throws PKCSException
    {
        CertificationRequestInfo requestInfo = certificationRequest.getCertificationRequestInfo();

        ContentVerifier verifier;

        try
        {
            verifier = verifierProvider.get(certificationRequest.getSignatureAlgorithm());

            OutputStream sOut = verifier.getOutputStream();

            sOut.write(requestInfo.getDEREncoded());

            sOut.close();
        }
        catch (Exception e)
        {
            throw new PKCSException("unable to process signature: " + e.getMessage(), e);
        }

        return verifier.verify(certificationRequest.getSignature().getBytes());
    }

    public boolean equals(Object o)
    {
        if (o == this)
        {
            return true;
        }

        if (!(o instanceof PKCS10CertificationRequestHolder))
        {
            return false;
        }

        PKCS10CertificationRequestHolder other = (PKCS10CertificationRequestHolder)o;

        return this.toASN1Structure().equals(other.toASN1Structure());
    }

    public int hashCode()
    {
        return this.toASN1Structure().hashCode();
    }
}
