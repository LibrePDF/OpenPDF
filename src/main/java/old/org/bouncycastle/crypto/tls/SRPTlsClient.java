package old.org.bouncycastle.crypto.tls;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Hashtable;

import old.org.bouncycastle.util.Arrays;

public abstract class SRPTlsClient implements TlsClient
{
    public static final Integer EXT_SRP = new Integer(ExtensionType.srp);

    protected TlsCipherFactory cipherFactory;
    protected byte[] identity;
    protected byte[] password;

    protected TlsClientContext context;

    protected int selectedCompressionMethod;
    protected int selectedCipherSuite;

    public SRPTlsClient(byte[] identity, byte[] password)
    {
        this(new DefaultTlsCipherFactory(), identity, password);
    }

    public SRPTlsClient(TlsCipherFactory cipherFactory, byte[] identity, byte[] password)
    {
        this.cipherFactory = cipherFactory;
        this.identity = Arrays.clone(identity);
        this.password = Arrays.clone(password);
    }

    public void init(TlsClientContext context)
    {
        this.context = context;
    }

    public int[] getCipherSuites()
    {
        return new int[] {
            CipherSuite.TLS_SRP_SHA_DSS_WITH_AES_256_CBC_SHA,
            CipherSuite.TLS_SRP_SHA_DSS_WITH_AES_128_CBC_SHA,
            CipherSuite.TLS_SRP_SHA_DSS_WITH_3DES_EDE_CBC_SHA,
            CipherSuite.TLS_SRP_SHA_RSA_WITH_AES_256_CBC_SHA,
            CipherSuite.TLS_SRP_SHA_RSA_WITH_AES_128_CBC_SHA,
            CipherSuite.TLS_SRP_SHA_RSA_WITH_3DES_EDE_CBC_SHA,
            CipherSuite.TLS_SRP_SHA_WITH_AES_256_CBC_SHA,
            CipherSuite.TLS_SRP_SHA_WITH_AES_128_CBC_SHA,
            CipherSuite.TLS_SRP_SHA_WITH_3DES_EDE_CBC_SHA, };
    }

    public Hashtable getClientExtensions() throws IOException
    {
        Hashtable clientExtensions = new Hashtable();

        ByteArrayOutputStream srpData = new ByteArrayOutputStream();
        TlsUtils.writeOpaque8(this.identity, srpData);
        clientExtensions.put(EXT_SRP, srpData.toByteArray());

        return clientExtensions;
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
        // There is no server response for the SRP extension
    }

    public TlsKeyExchange getKeyExchange() throws IOException
    {
        switch (selectedCipherSuite)
        {
            case CipherSuite.TLS_SRP_SHA_WITH_3DES_EDE_CBC_SHA:
            case CipherSuite.TLS_SRP_SHA_WITH_AES_128_CBC_SHA:
            case CipherSuite.TLS_SRP_SHA_WITH_AES_256_CBC_SHA:
                return createSRPKeyExchange(KeyExchangeAlgorithm.SRP);

            case CipherSuite.TLS_SRP_SHA_RSA_WITH_3DES_EDE_CBC_SHA:
            case CipherSuite.TLS_SRP_SHA_RSA_WITH_AES_128_CBC_SHA:
            case CipherSuite.TLS_SRP_SHA_RSA_WITH_AES_256_CBC_SHA:
                return createSRPKeyExchange(KeyExchangeAlgorithm.SRP_RSA);

            case CipherSuite.TLS_SRP_SHA_DSS_WITH_3DES_EDE_CBC_SHA:
            case CipherSuite.TLS_SRP_SHA_DSS_WITH_AES_128_CBC_SHA:
            case CipherSuite.TLS_SRP_SHA_DSS_WITH_AES_256_CBC_SHA:
                return createSRPKeyExchange(KeyExchangeAlgorithm.SRP_DSS);

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
            case CipherSuite.TLS_SRP_SHA_WITH_3DES_EDE_CBC_SHA:
            case CipherSuite.TLS_SRP_SHA_RSA_WITH_3DES_EDE_CBC_SHA:
            case CipherSuite.TLS_SRP_SHA_DSS_WITH_3DES_EDE_CBC_SHA:
                return cipherFactory.createCipher(context, EncryptionAlgorithm._3DES_EDE_CBC,
                    DigestAlgorithm.SHA);

            case CipherSuite.TLS_SRP_SHA_WITH_AES_128_CBC_SHA:
            case CipherSuite.TLS_SRP_SHA_RSA_WITH_AES_128_CBC_SHA:
            case CipherSuite.TLS_SRP_SHA_DSS_WITH_AES_128_CBC_SHA:
                return cipherFactory.createCipher(context, EncryptionAlgorithm.AES_128_CBC,
                    DigestAlgorithm.SHA);

            case CipherSuite.TLS_SRP_SHA_WITH_AES_256_CBC_SHA:
            case CipherSuite.TLS_SRP_SHA_RSA_WITH_AES_256_CBC_SHA:
            case CipherSuite.TLS_SRP_SHA_DSS_WITH_AES_256_CBC_SHA:
                return cipherFactory.createCipher(context, EncryptionAlgorithm.AES_256_CBC,
                    DigestAlgorithm.SHA);

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

    protected TlsKeyExchange createSRPKeyExchange(int keyExchange)
    {
        return new TlsSRPKeyExchange(context, keyExchange, identity, password);
    }
}
