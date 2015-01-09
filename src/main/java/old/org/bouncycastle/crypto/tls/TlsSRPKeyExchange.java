package old.org.bouncycastle.crypto.tls;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;

import old.org.bouncycastle.asn1.x509.KeyUsage;
import old.org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import old.org.bouncycastle.asn1.x509.X509CertificateStructure;
import old.org.bouncycastle.crypto.CryptoException;
import old.org.bouncycastle.crypto.Signer;
import old.org.bouncycastle.crypto.agreement.srp.SRP6Client;
import old.org.bouncycastle.crypto.agreement.srp.SRP6Util;
import old.org.bouncycastle.crypto.digests.SHA1Digest;
import old.org.bouncycastle.crypto.io.SignerInputStream;
import old.org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import old.org.bouncycastle.crypto.util.PublicKeyFactory;
import old.org.bouncycastle.util.BigIntegers;

/**
 * TLS 1.1 SRP key exchange.
 */
class TlsSRPKeyExchange implements TlsKeyExchange
{
    protected TlsClientContext context;
    protected int keyExchange;
    protected TlsSigner tlsSigner;
    protected byte[] identity;
    protected byte[] password;

    protected AsymmetricKeyParameter serverPublicKey = null;

    protected byte[] s = null;
    protected BigInteger B = null;
    protected SRP6Client srpClient = new SRP6Client();

    TlsSRPKeyExchange(TlsClientContext context, int keyExchange, byte[] identity, byte[] password)
    {
        switch (keyExchange)
        {
            case KeyExchangeAlgorithm.SRP:
                this.tlsSigner = null;
                break;
            case KeyExchangeAlgorithm.SRP_RSA:
                this.tlsSigner = new TlsRSASigner();
                break;
            case KeyExchangeAlgorithm.SRP_DSS:
                this.tlsSigner = new TlsDSSSigner();
                break;
            default:
                throw new IllegalArgumentException("unsupported key exchange algorithm");
        }

        this.context = context;
        this.keyExchange = keyExchange;
        this.identity = identity;
        this.password = password;
    }

    public void skipServerCertificate() throws IOException
    {
        if (tlsSigner != null)
        {
            throw new TlsFatalAlert(AlertDescription.unexpected_message);
        }
    }

    public void processServerCertificate(Certificate serverCertificate) throws IOException
    {
        if (tlsSigner == null)
        {
            throw new TlsFatalAlert(AlertDescription.unexpected_message);
        }

        X509CertificateStructure x509Cert = serverCertificate.certs[0];
        SubjectPublicKeyInfo keyInfo = x509Cert.getSubjectPublicKeyInfo();

        try
        {
            this.serverPublicKey = PublicKeyFactory.createKey(keyInfo);
        }
        catch (RuntimeException e)
        {
            throw new TlsFatalAlert(AlertDescription.unsupported_certificate);
        }

        if (!tlsSigner.isValidPublicKey(this.serverPublicKey))
        {
            throw new TlsFatalAlert(AlertDescription.certificate_unknown);
        }

        TlsUtils.validateKeyUsage(x509Cert, KeyUsage.digitalSignature);
        
        // TODO 
        /*
         * Perform various checks per RFC2246 7.4.2: "Unless otherwise specified, the
         * signing algorithm for the certificate must be the same as the algorithm for the
         * certificate key."
         */
    }

    public void skipServerKeyExchange() throws IOException
    {
        throw new TlsFatalAlert(AlertDescription.unexpected_message);
    }

    public void processServerKeyExchange(InputStream is) throws IOException
    {
        SecurityParameters securityParameters = context.getSecurityParameters();

        InputStream sigIn = is;
        Signer signer = null;

        if (tlsSigner != null)
        {
            signer = initSigner(tlsSigner, securityParameters);
            sigIn = new SignerInputStream(is, signer);
        }

        byte[] NBytes = TlsUtils.readOpaque16(sigIn);
        byte[] gBytes = TlsUtils.readOpaque16(sigIn);
        byte[] sBytes = TlsUtils.readOpaque8(sigIn);
        byte[] BBytes = TlsUtils.readOpaque16(sigIn);

        if (signer != null)
        {
            byte[] sigByte = TlsUtils.readOpaque16(is);

            if (!signer.verifySignature(sigByte))
            {
                throw new TlsFatalAlert(AlertDescription.bad_certificate);
            }
        }

        BigInteger N = new BigInteger(1, NBytes);
        BigInteger g = new BigInteger(1, gBytes);

        // TODO Validate group parameters (see RFC 5054)
//        handler.failWithError(AlertLevel.fatal, AlertDescription.insufficient_security);

        this.s = sBytes;

        /*
         * RFC 5054 2.5.3: The client MUST abort the handshake with an "illegal_parameter"
         * alert if B % N = 0.
         */
        try
        {
            this.B = SRP6Util.validatePublicValue(N, new BigInteger(1, BBytes));
        }
        catch (CryptoException e)
        {
            throw new TlsFatalAlert(AlertDescription.illegal_parameter);
        }

        this.srpClient.init(N, g, new SHA1Digest(), context.getSecureRandom());
    }

    public void validateCertificateRequest(CertificateRequest certificateRequest)
        throws IOException
    {
        throw new TlsFatalAlert(AlertDescription.unexpected_message);
    }

    public void skipClientCredentials() throws IOException
    {
        // OK
    }

    public void processClientCredentials(TlsCredentials clientCredentials) throws IOException
    {
        throw new TlsFatalAlert(AlertDescription.internal_error);
    }

    public void generateClientKeyExchange(OutputStream os) throws IOException
    {
        byte[] keData = BigIntegers.asUnsignedByteArray(srpClient.generateClientCredentials(s,
            this.identity, this.password));
        TlsUtils.writeUint24(keData.length + 2, os);
        TlsUtils.writeOpaque16(keData, os);
    }

    public byte[] generatePremasterSecret() throws IOException
    {
        try
        {
            // TODO Check if this needs to be a fixed size
            return BigIntegers.asUnsignedByteArray(srpClient.calculateSecret(B));
        }
        catch (CryptoException e)
        {
            throw new TlsFatalAlert(AlertDescription.illegal_parameter);
        }
    }

    protected Signer initSigner(TlsSigner tlsSigner, SecurityParameters securityParameters)
    {
        Signer signer = tlsSigner.createVerifyer(this.serverPublicKey);
        signer.update(securityParameters.clientRandom, 0, securityParameters.clientRandom.length);
        signer.update(securityParameters.serverRandom, 0, securityParameters.serverRandom.length);
        return signer;
    }
}
