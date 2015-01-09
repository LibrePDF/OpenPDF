package old.org.bouncycastle.crypto.tls;

import java.security.SecureRandom;

class TlsClientContextImpl implements TlsClientContext
{
    private SecureRandom secureRandom;
    private SecurityParameters securityParameters;

    private Object userObject = null;

    TlsClientContextImpl(SecureRandom secureRandom, SecurityParameters securityParameters)
    {
        this.secureRandom = secureRandom;
        this.securityParameters = securityParameters;
    }

    public SecureRandom getSecureRandom()
    {
        return secureRandom;
    }

    public SecurityParameters getSecurityParameters()
    {
        return securityParameters;
    }

    public Object getUserObject()
    {
        return userObject;
    }

    public void setUserObject(Object userObject)
    {
        this.userObject = userObject;
    }
}
