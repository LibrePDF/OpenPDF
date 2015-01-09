package old.org.bouncycastle.crypto.tls;

public class SecurityParameters
{
    byte[] clientRandom = null;
    byte[] serverRandom = null;
    byte[] masterSecret = null;

    public byte[] getClientRandom()
    {
        return clientRandom;
    }

    public byte[] getServerRandom()
    {
        return serverRandom;
    }

    public byte[] getMasterSecret()
    {
        return masterSecret;
    }
}
