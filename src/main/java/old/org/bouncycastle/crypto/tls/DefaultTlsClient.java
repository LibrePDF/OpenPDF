package old.org.bouncycastle.crypto.tls;

import java.io.IOException;
import java.util.Hashtable;

public abstract class DefaultTlsClient implements TlsClient
{
    protected TlsCipherFactory cipherFactory;

    protected TlsClientContext context;

    protected int selectedCipherSuite;
    protected int selectedCompressionMethod;

    public DefaultTlsClient()
    {
        this(new DefaultTlsCipherFactory());
    }

    public DefaultTlsClient(TlsCipherFactory cipherFactory)
    {
        this.cipherFactory = cipherFactory;
    }

    public void init(TlsClientContext context)
    {
        this.context = context;
    }

    public int[] getCipherSuites()
    {
        return new int[] {
            CipherSuite.TLS_DHE_RSA_WITH_AES_256_CBC_SHA,
            CipherSuite.TLS_DHE_DSS_WITH_AES_256_CBC_SHA,
            CipherSuite.TLS_DHE_RSA_WITH_AES_128_CBC_SHA,
            CipherSuite.TLS_DHE_DSS_WITH_AES_128_CBC_SHA,
            CipherSuite.TLS_DHE_RSA_WITH_3DES_EDE_CBC_SHA,
            CipherSuite.TLS_DHE_DSS_WITH_3DES_EDE_CBC_SHA,
            CipherSuite.TLS_RSA_WITH_AES_256_CBC_SHA,
            CipherSuite.TLS_RSA_WITH_AES_128_CBC_SHA,
            CipherSuite.TLS_RSA_WITH_3DES_EDE_CBC_SHA,
        };
    }

    public Hashtable getClientExtensions()
    {
        return null;
    }

    public short[] getCompressionMethods()
    {
        return new short[] { CompressionMethod.NULL };
    }

    public void notifySessionID(byte[] sessionID)
    {
        // Currently ignored 
    }

    public void notifySelectedCipherSuite(int selectedCipherSuite)
    {
        this.selectedCipherSuite = selectedCipherSuite;
    }

    public void notifySelectedCompressionMethod(short selectedCompressionMethod)
    {
        this.selectedCompressionMethod = selectedCompressionMethod;
    }

    public void notifySecureRenegotiation(boolean secureRenegotiation) throws IOException
    {
        if (!secureRenegotiation)
        {
            /*
             * RFC 5746 3.4. If the extension is not present, the server does not support
             * secure renegotiation; set secure_renegotiation flag to FALSE. In this case,
             * some clients may want to terminate the handshake instead of continuing; see
             * Section 4.1 for discussion.
             */
//            throw new TlsFatalAlert(AlertDescription.handshake_failure);
        }
    }

    public void processServerExtensions(Hashtable serverExtensions)
    {
    }

    public TlsKeyExchange getKeyExchange() throws IOException
    {
        switch (selectedCipherSuite)
        {
            case CipherSuite.TLS_RSA_WITH_3DES_EDE_CBC_SHA:
            case CipherSuite.TLS_RSA_WITH_AES_128_CBC_SHA:
            case CipherSuite.TLS_RSA_WITH_AES_256_CBC_SHA:
                return createRSAKeyExchange();

            case CipherSuite.TLS_DH_DSS_WITH_3DES_EDE_CBC_SHA:
            case CipherSuite.TLS_DH_DSS_WITH_AES_128_CBC_SHA:
            case CipherSuite.TLS_DH_DSS_WITH_AES_256_CBC_SHA:
                return createDHKeyExchange(KeyExchangeAlgorithm.DH_DSS);

            case CipherSuite.TLS_DH_RSA_WITH_3DES_EDE_CBC_SHA:
            case CipherSuite.TLS_DH_RSA_WITH_AES_128_CBC_SHA:
            case CipherSuite.TLS_DH_RSA_WITH_AES_256_CBC_SHA:
                return createDHKeyExchange(KeyExchangeAlgorithm.DH_RSA);

            case CipherSuite.TLS_DHE_DSS_WITH_3DES_EDE_CBC_SHA:
            case CipherSuite.TLS_DHE_DSS_WITH_AES_128_CBC_SHA:
            case CipherSuite.TLS_DHE_DSS_WITH_AES_256_CBC_SHA:
                return createDHEKeyExchange(KeyExchangeAlgorithm.DHE_DSS);

            case CipherSuite.TLS_DHE_RSA_WITH_3DES_EDE_CBC_SHA:
            case CipherSuite.TLS_DHE_RSA_WITH_AES_128_CBC_SHA:
            case CipherSuite.TLS_DHE_RSA_WITH_AES_256_CBC_SHA:
                return createDHEKeyExchange(KeyExchangeAlgorithm.DHE_RSA);

            case CipherSuite.TLS_ECDH_ECDSA_WITH_3DES_EDE_CBC_SHA:
            case CipherSuite.TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA:
            case CipherSuite.TLS_ECDH_ECDSA_WITH_AES_256_CBC_SHA:
                return createECDHKeyExchange(KeyExchangeAlgorithm.ECDH_ECDSA);

            case CipherSuite.TLS_ECDHE_ECDSA_WITH_3DES_EDE_CBC_SHA:
            case CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA:
            case CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA:
                return createECDHEKeyExchange(KeyExchangeAlgorithm.ECDHE_ECDSA);

            case CipherSuite.TLS_ECDH_RSA_WITH_3DES_EDE_CBC_SHA:
            case CipherSuite.TLS_ECDH_RSA_WITH_AES_128_CBC_SHA:
            case CipherSuite.TLS_ECDH_RSA_WITH_AES_256_CBC_SHA:
                return createECDHKeyExchange(KeyExchangeAlgorithm.ECDH_RSA);

            case CipherSuite.TLS_ECDHE_RSA_WITH_3DES_EDE_CBC_SHA:
            case CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA:
            case CipherSuite.TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA:
                return createECDHEKeyExchange(KeyExchangeAlgorithm.ECDHE_RSA);

            default:
                /*
                 * Note: internal error here; the TlsProtocolHandler verifies that the
                 * server-selected cipher suite was in the list of client-offered cipher
                 * suites, so if we now can't produce an implementation, we shouldn't have
                 * offered it!
                 */
                throw new TlsFatalAlert(AlertDescription.internal_error);
        }
    }

    public TlsCompression getCompression() throws IOException
    {
        switch (selectedCompressionMethod)
        {
            case CompressionMethod.NULL:
                return new TlsNullCompression();

            default:
                /*
                 * Note: internal error here; the TlsProtocolHandler verifies that the
                 * server-selected compression method was in the list of client-offered compression
                 * methods, so if we now can't produce an implementation, we shouldn't have
                 * offered it!
                 */
                throw new TlsFatalAlert(AlertDescription.internal_error);
        }
    }

    public TlsCipher getCipher() throws IOException
    {
        switch (selectedCipherSuite)
        {
            case CipherSuite.TLS_RSA_WITH_3DES_EDE_CBC_SHA:
            case CipherSuite.TLS_DH_DSS_WITH_3DES_EDE_CBC_SHA:
            case CipherSuite.TLS_DH_RSA_WITH_3DES_EDE_CBC_SHA:
            case CipherSuite.TLS_DHE_DSS_WITH_3DES_EDE_CBC_SHA:
            case CipherSuite.TLS_DHE_RSA_WITH_3DES_EDE_CBC_SHA:
            case CipherSuite.TLS_ECDH_ECDSA_WITH_3DES_EDE_CBC_SHA:
            case CipherSuite.TLS_ECDHE_ECDSA_WITH_3DES_EDE_CBC_SHA:
            case CipherSuite.TLS_ECDH_RSA_WITH_3DES_EDE_CBC_SHA:
            case CipherSuite.TLS_ECDHE_RSA_WITH_3DES_EDE_CBC_SHA:
                return cipherFactory.createCipher(context, EncryptionAlgorithm._3DES_EDE_CBC, DigestAlgorithm.SHA);

            case CipherSuite.TLS_RSA_WITH_AES_128_CBC_SHA:
            case CipherSuite.TLS_DH_DSS_WITH_AES_128_CBC_SHA:
            case CipherSuite.TLS_DH_RSA_WITH_AES_128_CBC_SHA:
            case CipherSuite.TLS_DHE_DSS_WITH_AES_128_CBC_SHA:
            case CipherSuite.TLS_DHE_RSA_WITH_AES_128_CBC_SHA:
            case CipherSuite.TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA:
            case CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA:
            case CipherSuite.TLS_ECDH_RSA_WITH_AES_128_CBC_SHA:
            case CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA:
                return cipherFactory.createCipher(context, EncryptionAlgorithm.AES_128_CBC, DigestAlgorithm.SHA);

            case CipherSuite.TLS_RSA_WITH_AES_256_CBC_SHA:
            case CipherSuite.TLS_DH_DSS_WITH_AES_256_CBC_SHA:
            case CipherSuite.TLS_DH_RSA_WITH_AES_256_CBC_SHA:
            case CipherSuite.TLS_DHE_DSS_WITH_AES_256_CBC_SHA:
            case CipherSuite.TLS_DHE_RSA_WITH_AES_256_CBC_SHA:
            case CipherSuite.TLS_ECDH_ECDSA_WITH_AES_256_CBC_SHA:
            case CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA:
            case CipherSuite.TLS_ECDH_RSA_WITH_AES_256_CBC_SHA:
            case CipherSuite.TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA:
                return cipherFactory.createCipher(context, EncryptionAlgorithm.AES_256_CBC, DigestAlgorithm.SHA);

            default:
                /*
                 * Note: internal error here; the TlsProtocolHandler verifies that the
                 * server-selected cipher suite was in the list of client-offered cipher
                 * suites, so if we now can't produce an implementation, we shouldn't have
                 * offered it!
                 */
                throw new TlsFatalAlert(AlertDescription.internal_error);
        }
    }

    protected TlsKeyExchange createDHKeyExchange(int keyExchange)
    {
        return new TlsDHKeyExchange(context, keyExchange);
    }

    protected TlsKeyExchange createDHEKeyExchange(int keyExchange)
    {
        return new TlsDHEKeyExchange(context, keyExchange);
    }

    protected TlsKeyExchange createECDHKeyExchange(int keyExchange)
    {
        return new TlsECDHKeyExchange(context, keyExchange);
    }

    protected TlsKeyExchange createECDHEKeyExchange(int keyExchange)
    {
        return new TlsECDHEKeyExchange(context, keyExchange);
    }

    protected TlsKeyExchange createRSAKeyExchange()
    {
        return new TlsRSAKeyExchange(context);
    }
}
