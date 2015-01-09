package old.org.bouncycastle.crypto.tls;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;

import old.org.bouncycastle.asn1.x509.KeyUsage;
import old.org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import old.org.bouncycastle.asn1.x509.X509CertificateStructure;
import old.org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import old.org.bouncycastle.crypto.agreement.DHBasicAgreement;
import old.org.bouncycastle.crypto.generators.DHBasicKeyPairGenerator;
import old.org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import old.org.bouncycastle.crypto.params.DHKeyGenerationParameters;
import old.org.bouncycastle.crypto.params.DHParameters;
import old.org.bouncycastle.crypto.params.DHPrivateKeyParameters;
import old.org.bouncycastle.crypto.params.DHPublicKeyParameters;
import old.org.bouncycastle.crypto.util.PublicKeyFactory;
import old.org.bouncycastle.util.BigIntegers;

/**
 * TLS 1.0 DH key exchange.
 */
class TlsDHKeyExchange implements TlsKeyExchange
{
    protected static final BigInteger ONE = BigInteger.valueOf(1);
    protected static final BigInteger TWO = BigInteger.valueOf(2);

    protected TlsClientContext context;
    protected int keyExchange;
    protected TlsSigner tlsSigner;

    protected AsymmetricKeyParameter serverPublicKey = null;
    protected DHPublicKeyParameters dhAgreeServerPublicKey = null;
    protected TlsAgreementCredentials agreementCredentials;
    protected DHPrivateKeyParameters dhAgreeClientPrivateKey = null;

    TlsDHKeyExchange(TlsClientContext context, int keyExchange)
    {
        switch (keyExchange)
        {
            case KeyExchangeAlgorithm.DH_RSA:
            case KeyExchangeAlgorithm.DH_DSS:
                this.tlsSigner = null;
                break;
            case KeyExchangeAlgorithm.DHE_RSA:
                this.tlsSigner = new TlsRSASigner();
                break;
            case KeyExchangeAlgorithm.DHE_DSS:
                this.tlsSigner = new TlsDSSSigner();
                break;
            default:
                throw new IllegalArgumentException("unsupported key exchange algorithm");
        }

        this.context = context;
        this.keyExchange = keyExchange;
    }

    public void skipServerCertificate() throws IOException
    {
        throw new TlsFatalAlert(AlertDescription.unexpected_message);
    }

    public void processServerCertificate(Certificate serverCertificate) throws IOException
    {
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

        if (tlsSigner == null)
        {
            try
            {
                this.dhAgreeServerPublicKey = validateDHPublicKey((DHPublicKeyParameters)this.serverPublicKey);
            }
            catch (ClassCastException e)
            {
                throw new TlsFatalAlert(AlertDescription.certificate_unknown);
            }

            TlsUtils.validateKeyUsage(x509Cert, KeyUsage.keyAgreement);
        }
        else
        {
            if (!tlsSigner.isValidPublicKey(this.serverPublicKey))
            {
                throw new TlsFatalAlert(AlertDescription.certificate_unknown);
            }

            TlsUtils.validateKeyUsage(x509Cert, KeyUsage.digitalSignature);
        }

        // TODO 
        /*
         * Perform various checks per RFC2246 7.4.2: "Unless otherwise specified, the
         * signing algorithm for the certificate must be the same as the algorithm for the
         * certificate key."
         */
    }

    public void skipServerKeyExchange() throws IOException
    {
        // OK
    }

    public void processServerKeyExchange(InputStream is)
        throws IOException
    {
        throw new TlsFatalAlert(AlertDescription.unexpected_message);
    }

    public void validateCertificateRequest(CertificateRequest certificateRequest)
        throws IOException
    {
        short[] types = certificateRequest.getCertificateTypes();
        for (int i = 0; i < types.length; ++i)
        {
            switch (types[i])
            {
                case ClientCertificateType.rsa_sign:
                case ClientCertificateType.dss_sign:
                case ClientCertificateType.rsa_fixed_dh:
                case ClientCertificateType.dss_fixed_dh:
                case ClientCertificateType.ecdsa_sign:
                    break;
                default:
                    throw new TlsFatalAlert(AlertDescription.illegal_parameter);
            }
        }
    }

    public void skipClientCredentials() throws IOException
    {
        this.agreementCredentials = null;
    }

    public void processClientCredentials(TlsCredentials clientCredentials) throws IOException
    {
        if (clientCredentials instanceof TlsAgreementCredentials)
        {
            // TODO Validate client cert has matching parameters (see 'areCompatibleParameters')?

            this.agreementCredentials = (TlsAgreementCredentials)clientCredentials;
        }
        else if (clientCredentials instanceof TlsSignerCredentials)
        {
            // OK
        }
        else
        {
            throw new TlsFatalAlert(AlertDescription.internal_error);
        }
    }

    public void generateClientKeyExchange(OutputStream os) throws IOException
    {
        /*
         * RFC 2246 7.4.7.2 If the client certificate already contains a suitable
         * Diffie-Hellman key, then Yc is implicit and does not need to be sent again. In
         * this case, the Client Key Exchange message will be sent, but will be empty.
         */
        if (agreementCredentials != null)
        {
            TlsUtils.writeUint24(0, os);
        }
        else
        {
            generateEphemeralClientKeyExchange(dhAgreeServerPublicKey.getParameters(), os);
        }
    }

    public byte[] generatePremasterSecret() throws IOException
    {
        if (agreementCredentials != null)
        {
            return agreementCredentials.generateAgreement(dhAgreeServerPublicKey);
        }

        return calculateDHBasicAgreement(dhAgreeServerPublicKey, dhAgreeClientPrivateKey);
    }

    protected boolean areCompatibleParameters(DHParameters a, DHParameters b)
    {
        return a.getP().equals(b.getP()) && a.getG().equals(b.getG());
    }

    protected byte[] calculateDHBasicAgreement(DHPublicKeyParameters publicKey,
        DHPrivateKeyParameters privateKey)
    {
        DHBasicAgreement dhAgree = new DHBasicAgreement();
        dhAgree.init(dhAgreeClientPrivateKey);
        BigInteger agreement = dhAgree.calculateAgreement(dhAgreeServerPublicKey);
        return BigIntegers.asUnsignedByteArray(agreement);
    }

    protected AsymmetricCipherKeyPair generateDHKeyPair(DHParameters dhParams)
    {
        DHBasicKeyPairGenerator dhGen = new DHBasicKeyPairGenerator();
        dhGen.init(new DHKeyGenerationParameters(context.getSecureRandom(), dhParams));
        return dhGen.generateKeyPair();
    }

    protected void generateEphemeralClientKeyExchange(DHParameters dhParams, OutputStream os)
        throws IOException
    {
        AsymmetricCipherKeyPair dhAgreeClientKeyPair = generateDHKeyPair(dhParams);
        this.dhAgreeClientPrivateKey = (DHPrivateKeyParameters)dhAgreeClientKeyPair.getPrivate();

        BigInteger Yc = ((DHPublicKeyParameters)dhAgreeClientKeyPair.getPublic()).getY();
        byte[] keData = BigIntegers.asUnsignedByteArray(Yc);
        TlsUtils.writeUint24(keData.length + 2, os);
        TlsUtils.writeOpaque16(keData, os);
    }

    protected DHPublicKeyParameters validateDHPublicKey(DHPublicKeyParameters key)
        throws IOException
    {
        BigInteger Y = key.getY();
        DHParameters params = key.getParameters();
        BigInteger p = params.getP();
        BigInteger g = params.getG();

        if (!p.isProbablePrime(2))
        {
            throw new TlsFatalAlert(AlertDescription.illegal_parameter);
        }
        if (g.compareTo(TWO) < 0 || g.compareTo(p.subtract(TWO)) > 0)
        {
            throw new TlsFatalAlert(AlertDescription.illegal_parameter);
        }
        if (Y.compareTo(TWO) < 0 || Y.compareTo(p.subtract(ONE)) > 0)
        {
            throw new TlsFatalAlert(AlertDescription.illegal_parameter);
        }

        // TODO See RFC 2631 for more discussion of Diffie-Hellman validation

        return key;
    }
}
