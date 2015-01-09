package old.org.bouncycastle.cms;

import java.io.IOException;
import java.math.BigInteger;

import old.org.bouncycastle.asn1.ASN1OctetString;
import old.org.bouncycastle.asn1.DEROctetString;
import old.org.bouncycastle.asn1.cms.IssuerAndSerialNumber;
import old.org.bouncycastle.asn1.x500.X500Name;
import old.org.bouncycastle.asn1.x509.X509Extension;
import old.org.bouncycastle.cert.X509CertificateHolder;
import old.org.bouncycastle.crypto.Digest;
import old.org.bouncycastle.crypto.digests.SHA1Digest;
import old.org.bouncycastle.util.Arrays;

public class KeyAgreeRecipientId
    extends RecipientId
{
    private byte[] subjectKeyId;

    private X500Name issuer;
    private BigInteger serialNumber;

    /**
     * Construct a key agree recipient ID with the value of a public key's subjectKeyId.
     *
     * @param subjectKeyId a subjectKeyId
     */
    public KeyAgreeRecipientId(byte[] subjectKeyId)
    {
        super(keyAgree);
        super.setSubjectKeyIdentifier(new DEROctetString(subjectKeyId).getDEREncoded());

        this.subjectKeyId = subjectKeyId;
    }

    /**
     * Construct a key agree recipient ID based on the issuer and serial number of the recipient's associated
     * certificate.
     *
     * @param issuer the issuer of the recipient's associated certificate.
     * @param serialNumber the serial number of the recipient's associated certificate.
     */
    public KeyAgreeRecipientId(X500Name issuer, BigInteger serialNumber)
    {
        super(keyAgree);
        this.issuer = issuer;
        this.serialNumber = serialNumber;

        try
        {
            this.setIssuer(issuer.getDEREncoded());
        }
        catch (IOException e)
        {
            throw new IllegalArgumentException("invalid issuer: " + e.getMessage());
        }
        this.setSerialNumber(serialNumber);
    }

    public int hashCode()
    {
        int code = Arrays.hashCode(subjectKeyId);

        if (this.serialNumber != null)
        {
            code ^= this.serialNumber.hashCode();
        }

        if (this.issuer != null)
        {
            code ^= this.issuer.hashCode();
        }

        return code;
    }

    public boolean equals(
        Object  o)
    {
        if (!(o instanceof KeyAgreeRecipientId))
        {
            return false;
        }

        KeyAgreeRecipientId id = (KeyAgreeRecipientId)o;

        return Arrays.areEqual(subjectKeyId, id.subjectKeyId)
            && equalsObj(this.serialNumber, id.serialNumber)
            && equalsObj(this.issuer, id.issuer);
    }

    private boolean equalsObj(Object a, Object b)
    {
        return (a != null) ? a.equals(b) : b == null;
    }

    public boolean match(Object obj)
    {
        if (obj instanceof X509CertificateHolder)
        {
            X509CertificateHolder certHldr = (X509CertificateHolder)obj;

            if (this.getSerialNumber() != null)
            {
                IssuerAndSerialNumber iAndS = certHldr.getIssuerAndSerialNumber();

                return iAndS.getName().equals(this.issuer)
                    && iAndS.getSerialNumber().getValue().equals(this.getSerialNumber());
            }
            else if (this.getSubjectKeyIdentifier() != null)
            {
                X509Extension ext = certHldr.getExtension(X509Extension.subjectKeyIdentifier);

                if (ext == null)
                {
                    Digest dig = new SHA1Digest();
                    byte[] hash = new byte[dig.getDigestSize()];
                    byte[] spkiEnc = certHldr.getSubjectPublicKeyInfo().getDEREncoded();

                    // try the outlook 2010 calculation
                    dig.update(spkiEnc, 0, spkiEnc.length);

                    dig.doFinal(hash, 0);

                    return Arrays.areEqual(subjectKeyId, hash);
                }

                byte[] subKeyID = ASN1OctetString.getInstance(ext.getParsedValue()).getOctets();

                return Arrays.areEqual(subjectKeyId, subKeyID);
            }
        }
        else if (obj instanceof byte[])
        {
            return Arrays.areEqual(subjectKeyId, (byte[])obj);
        }
        else if (obj instanceof KeyAgreeRecipientInformation)
        {
            return ((KeyAgreeRecipientInformation)obj).getRID().equals(this);
        }

        return false;
    }
}
