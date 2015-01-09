package old.org.bouncycastle.crypto.tls;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;

import old.org.bouncycastle.crypto.Signer;
import old.org.bouncycastle.crypto.io.SignerInputStream;
import old.org.bouncycastle.crypto.params.DHParameters;
import old.org.bouncycastle.crypto.params.DHPublicKeyParameters;

class TlsDHEKeyExchange extends TlsDHKeyExchange
{
    TlsDHEKeyExchange(TlsClientContext context, int keyExchange)
    {
        super(context, keyExchange);
    }

    public void skipServerKeyExchange() throws IOException
    {
        throw new TlsFatalAlert(AlertDescription.unexpected_message);
    }

    public void processServerKeyExchange(InputStream is)
        throws IOException
    {
        SecurityParameters securityParameters = context.getSecurityParameters();

        Signer signer = initSigner(tlsSigner, securityParameters);
        InputStream sigIn = new SignerInputStream(is, signer);

        byte[] pBytes = TlsUtils.readOpaque16(sigIn);
        byte[] gBytes = TlsUtils.readOpaque16(sigIn);
        byte[] YsBytes = TlsUtils.readOpaque16(sigIn);

        byte[] sigByte = TlsUtils.readOpaque16(is);
        if (!signer.verifySignature(sigByte))
        {
            throw new TlsFatalAlert(AlertDescription.bad_certificate);
        }

        BigInteger p = new BigInteger(1, pBytes);
        BigInteger g = new BigInteger(1, gBytes);
        BigInteger Ys = new BigInteger(1, YsBytes);

        this.dhAgreeServerPublicKey = validateDHPublicKey(new DHPublicKeyParameters(Ys,
            new DHParameters(p, g)));
    }

    protected Signer initSigner(TlsSigner tlsSigner, SecurityParameters securityParameters)
    {
        Signer signer = tlsSigner.createVerifyer(this.serverPublicKey);
        signer.update(securityParameters.clientRandom, 0, securityParameters.clientRandom.length);
        signer.update(securityParameters.serverRandom, 0, securityParameters.serverRandom.length);
        return signer;
    }
}
