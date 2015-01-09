package old.org.bouncycastle.cms;

import old.org.bouncycastle.util.Arrays;

import java.security.cert.X509CertSelector;

/**
 * a basic index for an originator.
 */
class OriginatorId
    extends X509CertSelector
{
    public int hashCode()
    {
        int code = Arrays.hashCode(this.getSubjectKeyIdentifier());

        if (this.getSerialNumber() != null)
        {
            code ^= this.getSerialNumber().hashCode();
        }

        if (this.getIssuerAsString() != null)
        {
            code ^= this.getIssuerAsString().hashCode();
        }

        return code;
    }

    public boolean equals(
        Object  o)
    {
        if (!(o instanceof OriginatorId))
        {
            return false;
        }

        OriginatorId id = (OriginatorId)o;

        return Arrays.areEqual(this.getSubjectKeyIdentifier(), id.getSubjectKeyIdentifier())
            && equalsObj(this.getSerialNumber(), id.getSerialNumber())
            && equalsObj(this.getIssuerAsString(), id.getIssuerAsString());
    }

    private boolean equalsObj(Object a, Object b)
    {
        return (a != null) ? a.equals(b) : b == null;
    }
}
