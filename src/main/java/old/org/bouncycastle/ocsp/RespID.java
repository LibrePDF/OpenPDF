package old.org.bouncycastle.ocsp;

import java.security.MessageDigest;
import java.security.PublicKey;

import javax.security.auth.x500.X500Principal;

import old.org.bouncycastle.asn1.ASN1InputStream;
import old.org.bouncycastle.asn1.ASN1OctetString;
import old.org.bouncycastle.asn1.DEROctetString;
import old.org.bouncycastle.asn1.ocsp.ResponderID;
import old.org.bouncycastle.asn1.x500.X500Name;
import old.org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;

/**
 * Carrier for a ResponderID.
 */
public class RespID
{
    ResponderID id;

    public RespID(
        ResponderID id)
    {
        this.id = id;
    }

    public RespID(
        X500Principal   name)
    {
        this.id = new ResponderID(X500Name.getInstance(name.getEncoded()));
    }

    public RespID(
        PublicKey   key)
        throws OCSPException
    {
        try
        {
            // TODO Allow specification of a particular provider
            MessageDigest digest = OCSPUtil.createDigestInstance("SHA1", null);

            ASN1InputStream aIn = new ASN1InputStream(key.getEncoded());
            SubjectPublicKeyInfo info = SubjectPublicKeyInfo.getInstance(aIn.readObject());

            digest.update(info.getPublicKeyData().getBytes());

            ASN1OctetString keyHash = new DEROctetString(digest.digest());

            this.id = new ResponderID(keyHash);
        }
        catch (Exception e)
        {
            throw new OCSPException("problem creating ID: " + e, e);
        }
    }

    public ResponderID toASN1Object()
    {
        return id;
    }

    public boolean equals(
        Object  o)
    {
        if (!(o instanceof RespID))
        {
            return false;
        }

        RespID   obj = (RespID)o;

        return id.equals(obj.id);
    }

    public int hashCode()
    {
        return id.hashCode();
    }
}
