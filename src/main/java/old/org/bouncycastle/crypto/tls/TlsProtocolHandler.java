package old.org.bouncycastle.crypto.tls;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.SecureRandom;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import old.org.bouncycastle.asn1.ASN1Object;
import old.org.bouncycastle.asn1.x500.X500Name;
import old.org.bouncycastle.crypto.prng.ThreadedSeedGenerator;
import old.org.bouncycastle.util.Arrays;

/**
 * An implementation of all high level protocols in TLS 1.0.
 */
public class TlsProtocolHandler
{
    private static final Integer EXT_RenegotiationInfo = new Integer(ExtensionType.renegotiation_info);

    /*
     * Our Connection states
     */
    private static final short CS_CLIENT_HELLO_SEND = 1;
    private static final short CS_SERVER_HELLO_RECEIVED = 2;
    private static final short CS_SERVER_CERTIFICATE_RECEIVED = 3;
    private static final short CS_SERVER_KEY_EXCHANGE_RECEIVED = 4;
    private static final short CS_CERTIFICATE_REQUEST_RECEIVED = 5;
    private static final short CS_SERVER_HELLO_DONE_RECEIVED = 6;
    private static final short CS_CLIENT_KEY_EXCHANGE_SEND = 7;
    private static final short CS_CERTIFICATE_VERIFY_SEND = 8;
    private static final short CS_CLIENT_CHANGE_CIPHER_SPEC_SEND = 9;
    private static final short CS_CLIENT_FINISHED_SEND = 10;
    private static final short CS_SERVER_CHANGE_CIPHER_SPEC_RECEIVED = 11;
    private static final short CS_DONE = 12;

    private static final byte[] emptybuf = new byte[0];

    private static final String TLS_ERROR_MESSAGE = "Internal TLS error, this could be an attack";

    /*
     * Queues for data from some protocols.
     */
    private ByteQueue applicationDataQueue = new ByteQueue();
    private ByteQueue changeCipherSpecQueue = new ByteQueue();
    private ByteQueue alertQueue = new ByteQueue();
    private ByteQueue handshakeQueue = new ByteQueue();

    /*
     * The Record Stream we use
     */
    private RecordStream rs;
    private SecureRandom random;

    private TlsInputStream tlsInputStream = null;
    private TlsOutputStream tlsOutputStream = null;

    private boolean closed = false;
    private boolean failedWithError = false;
    private boolean appDataReady = false;
    private Hashtable clientExtensions;

    private SecurityParameters securityParameters = null;

    private TlsClientContextImpl tlsClientContext = null;
    private TlsClient tlsClient = null;
    private int[] offeredCipherSuites = null;
    private short[] offeredCompressionMethods = null;
    private TlsKeyExchange keyExchange = null;
    private TlsAuthentication authentication = null;
    private CertificateRequest certificateRequest = null;

    private short connection_state = 0;

    private static SecureRandom createSecureRandom()
    {
        /*
         * We use our threaded seed generator to generate a good random seed. If the user
         * has a better random seed, he should use the constructor with a SecureRandom.
         */
        ThreadedSeedGenerator tsg = new ThreadedSeedGenerator();
        SecureRandom random = new SecureRandom();

        /*
         * Hopefully, 20 bytes in fast mode are good enough.
         */
        random.setSeed(tsg.generateSeed(20, true));

        return random;
    }

    public TlsProtocolHandler(InputStream is, OutputStream os)
    {
        this(is, os, createSecureRandom());
    }

    public TlsProtocolHandler(InputStream is, OutputStream os, SecureRandom sr)
    {
        this.rs = new RecordStream(this, is, os);
        this.random = sr;
    }

    protected void processData(short protocol, byte[] buf, int offset, int len) throws IOException
    {
        /*
         * Have a look at the protocol type, and add it to the correct queue.
         */
        switch (protocol)
        {
            case ContentType.change_cipher_spec:
                changeCipherSpecQueue.addData(buf, offset, len);
                processChangeCipherSpec();
                break;
            case ContentType.alert:
                alertQueue.addData(buf, offset, len);
                processAlert();
                break;
            case ContentType.handshake:
                handshakeQueue.addData(buf, offset, len);
                processHandshake();
                break;
            case ContentType.application_data:
                if (!appDataReady)
                {
                    this.failWithError(AlertLevel.fatal, AlertDescription.unexpected_message);
                }
                applicationDataQueue.addData(buf, offset, len);
                processApplicationData();
                break;
            default:
                /*
                 * Uh, we don't know this protocol.
                 * 
                 * RFC2246 defines on page 13, that we should ignore this.
                 */
        }
    }

    private void processHandshake() throws IOException
    {
        boolean read;
        do
        {
            read = false;
            /*
             * We need the first 4 bytes, they contain type and length of the message.
             */
            if (handshakeQueue.size() >= 4)
            {
                byte[] beginning = new byte[4];
                handshakeQueue.read(beginning, 0, 4, 0);
                ByteArrayInputStream bis = new ByteArrayInputStream(beginning);
                short type = TlsUtils.readUint8(bis);
                int len = TlsUtils.readUint24(bis);

                /*
                 * Check if we have enough bytes in the buffer to read the full message.
                 */
                if (handshakeQueue.size() >= (len + 4))
                {
                    /*
                     * Read the message.
                     */
                    byte[] buf = new byte[len];
                    handshakeQueue.read(buf, 0, len, 4);
                    handshakeQueue.removeData(len + 4);

                    /*
                     * RFC 2246 7.4.9. The value handshake_messages includes all handshake
                     * messages starting at client hello up to, but not including, this
                     * finished message. [..] Note: [Also,] Hello Request messages are
                     * omitted from handshake hashes.
                     */
                    switch (type)
                    {
                        case HandshakeType.hello_request:
                        case HandshakeType.finished:
                            break;
                        default:
                            rs.updateHandshakeData(beginning, 0, 4);
                            rs.updateHandshakeData(buf, 0, len);
                            break;
                    }

                    /*
                     * Now, parse the message.
                     */
                    processHandshakeMessage(type, buf);
                    read = true;
                }
            }
        }
        while (read);
    }

    private void processHandshakeMessage(short type, byte[] buf) throws IOException
    {
        ByteArrayInputStream is = new ByteArrayInputStream(buf);

        switch (type)
        {
            case HandshakeType.certificate:
            {
                switch (connection_state)
                {
                    case CS_SERVER_HELLO_RECEIVED:
                    {
                        // Parse the Certificate message and send to cipher suite

                        Certificate serverCertificate = Certificate.parse(is);

                        assertEmpty(is);

                        this.keyExchange.processServerCertificate(serverCertificate);

                        this.authentication = tlsClient.getAuthentication();
                        this.authentication.notifyServerCertificate(serverCertificate);

                        break;
                    }
                    default:
                        this.failWithError(AlertLevel.fatal, AlertDescription.unexpected_message);
                }

                connection_state = CS_SERVER_CERTIFICATE_RECEIVED;
                break;
            }
            case HandshakeType.finished:
                switch (connection_state)
                {
                    case CS_SERVER_CHANGE_CIPHER_SPEC_RECEIVED:
                        /*
                         * Read the checksum from the finished message, it has always 12
                         * bytes.
                         */
                        byte[] serverVerifyData = new byte[12];
                        TlsUtils.readFully(serverVerifyData, is);

                        assertEmpty(is);

                        /*
                         * Calculate our own checksum.
                         */
                        byte[] expectedServerVerifyData = TlsUtils.PRF(
                            securityParameters.masterSecret, "server finished",
                            rs.getCurrentHash(), 12);

                        /*
                         * Compare both checksums.
                         */
                        if (!Arrays.constantTimeAreEqual(expectedServerVerifyData, serverVerifyData))
                        {
                            /*
                             * Wrong checksum in the finished message.
                             */
                            this.failWithError(AlertLevel.fatal, AlertDescription.handshake_failure);
                        }

                        connection_state = CS_DONE;

                        /*
                         * We are now ready to receive application data.
                         */
                        this.appDataReady = true;
                        break;
                    default:
                        this.failWithError(AlertLevel.fatal, AlertDescription.unexpected_message);
                }
                break;
            case HandshakeType.server_hello:
                switch (connection_state)
                {
                    case CS_CLIENT_HELLO_SEND:
                        /*
                         * Read the server hello message
                         */
                        TlsUtils.checkVersion(is, this);

                        /*
                         * Read the server random
                         */
                        securityParameters.serverRandom = new byte[32];
                        TlsUtils.readFully(securityParameters.serverRandom, is);

                        byte[] sessionID = TlsUtils.readOpaque8(is);
                        if (sessionID.length > 32)
                        {
                            this.failWithError(AlertLevel.fatal, AlertDescription.illegal_parameter);
                        }

                        this.tlsClient.notifySessionID(sessionID);

                        /*
                         * Find out which CipherSuite the server has chosen and check that
                         * it was one of the offered ones.
                         */
                        int selectedCipherSuite = TlsUtils.readUint16(is);
                        if (!arrayContains(offeredCipherSuites, selectedCipherSuite)
                            || selectedCipherSuite == CipherSuite.TLS_EMPTY_RENEGOTIATION_INFO_SCSV)
                        {
                            this.failWithError(AlertLevel.fatal, AlertDescription.illegal_parameter);
                        }

                        this.tlsClient.notifySelectedCipherSuite(selectedCipherSuite);

                        /*
                         * Find out which CompressionMethod the server has chosen and check that
                         * it was one of the offered ones.
                         */
                        short selectedCompressionMethod = TlsUtils.readUint8(is);
                        if (!arrayContains(offeredCompressionMethods, selectedCompressionMethod))
                        {
                            this.failWithError(AlertLevel.fatal, AlertDescription.illegal_parameter);
                        }

                        this.tlsClient.notifySelectedCompressionMethod(selectedCompressionMethod);

                        /*
                         * RFC3546 2.2 The extended server hello message format MAY be
                         * sent in place of the server hello message when the client has
                         * requested extended functionality via the extended client hello
                         * message specified in Section 2.1. ... Note that the extended
                         * server hello message is only sent in response to an extended
                         * client hello message. This prevents the possibility that the
                         * extended server hello message could "break" existing TLS 1.0
                         * clients.
                         */

                        /*
                         * TODO RFC 3546 2.3 If [...] the older session is resumed, then
                         * the server MUST ignore extensions appearing in the client
                         * hello, and send a server hello containing no extensions.
                         */

                        // Integer -> byte[]
                        Hashtable serverExtensions = new Hashtable();

                        if (is.available() > 0)
                        {
                            // Process extensions from extended server hello
                            byte[] extBytes = TlsUtils.readOpaque16(is);

                            ByteArrayInputStream ext = new ByteArrayInputStream(extBytes);
                            while (ext.available() > 0)
                            {
                                Integer extType = new Integer(TlsUtils.readUint16(ext));
                                byte[] extValue = TlsUtils.readOpaque16(ext);

                                /*
                                 * RFC 5746 Note that sending a "renegotiation_info"
                                 * extension in response to a ClientHello containing only
                                 * the SCSV is an explicit exception to the prohibition in
                                 * RFC 5246, Section 7.4.1.4, on the server sending
                                 * unsolicited extensions and is only allowed because the
                                 * client is signaling its willingness to receive the
                                 * extension via the TLS_EMPTY_RENEGOTIATION_INFO_SCSV
                                 * SCSV. TLS implementations MUST continue to comply with
                                 * Section 7.4.1.4 for all other extensions.
                                 */

                                if (!extType.equals(EXT_RenegotiationInfo)
                                    && clientExtensions.get(extType) == null)
                                {
                                    /*
                                     * RFC 3546 2.3 Note that for all extension types
                                     * (including those defined in future), the extension
                                     * type MUST NOT appear in the extended server hello
                                     * unless the same extension type appeared in the
                                     * corresponding client hello. Thus clients MUST abort
                                     * the handshake if they receive an extension type in
                                     * the extended server hello that they did not request
                                     * in the associated (extended) client hello.
                                     */
                                    this.failWithError(AlertLevel.fatal,
                                        AlertDescription.unsupported_extension);
                                }

                                if (serverExtensions.containsKey(extType))
                                {
                                    /*
                                     * RFC 3546 2.3 Also note that when multiple
                                     * extensions of different types are present in the
                                     * extended client hello or the extended server hello,
                                     * the extensions may appear in any order. There MUST
                                     * NOT be more than one extension of the same type.
                                     */
                                    this.failWithError(AlertLevel.fatal,
                                        AlertDescription.illegal_parameter);
                                }

                                serverExtensions.put(extType, extValue);
                            }
                        }

                        assertEmpty(is);

                        /*
                         * RFC 5746 3.4. When a ServerHello is received, the client MUST
                         * check if it includes the "renegotiation_info" extension:
                         */
                        {
                            boolean secure_negotiation = serverExtensions.containsKey(EXT_RenegotiationInfo);

                            /*
                             * If the extension is present, set the secure_renegotiation
                             * flag to TRUE. The client MUST then verify that the length
                             * of the "renegotiated_connection" field is zero, and if it
                             * is not, MUST abort the handshake (by sending a fatal
                             * handshake_failure alert).
                             */
                            if (secure_negotiation)
                            {
                                byte[] renegExtValue = (byte[])serverExtensions.get(EXT_RenegotiationInfo);

                                if (!Arrays.constantTimeAreEqual(renegExtValue,
                                    createRenegotiationInfo(emptybuf)))
                                {
                                    this.failWithError(AlertLevel.fatal,
                                        AlertDescription.handshake_failure);
                                }
                            }

                            tlsClient.notifySecureRenegotiation(secure_negotiation);
                        }

                        if (clientExtensions != null)
                        {
                            tlsClient.processServerExtensions(serverExtensions);
                        }

                        this.keyExchange = tlsClient.getKeyExchange();

                        connection_state = CS_SERVER_HELLO_RECEIVED;
                        break;
                    default:
                        this.failWithError(AlertLevel.fatal, AlertDescription.unexpected_message);
                }
                break;
            case HandshakeType.server_hello_done:
                switch (connection_state)
                {
                    case CS_SERVER_CERTIFICATE_RECEIVED:

                        // There was no server key exchange message; check it's OK
                        this.keyExchange.skipServerKeyExchange();

                        // NB: Fall through to next case label

                    case CS_SERVER_KEY_EXCHANGE_RECEIVED:
                    case CS_CERTIFICATE_REQUEST_RECEIVED:

                        assertEmpty(is);

                        connection_state = CS_SERVER_HELLO_DONE_RECEIVED;

                        TlsCredentials clientCreds = null;
                        if (certificateRequest == null)
                        {
                            this.keyExchange.skipClientCredentials();
                        }
                        else
                        {
                            clientCreds = this.authentication.getClientCredentials(certificateRequest);

                            Certificate clientCert;
                            if (clientCreds == null)
                            {
                                this.keyExchange.skipClientCredentials();
                                clientCert = Certificate.EMPTY_CHAIN;
                            }
                            else
                            {
                                this.keyExchange.processClientCredentials(clientCreds);
                                clientCert = clientCreds.getCertificate();
                            }

                            sendClientCertificate(clientCert);
                        }

                        /*
                         * Send the client key exchange message, depending on the key
                         * exchange we are using in our CipherSuite.
                         */
                        sendClientKeyExchange();

                        connection_state = CS_CLIENT_KEY_EXCHANGE_SEND;

                        if (clientCreds != null && clientCreds instanceof TlsSignerCredentials)
                        {
                            TlsSignerCredentials signerCreds = (TlsSignerCredentials)clientCreds;
                            byte[] md5andsha1 = rs.getCurrentHash();
                            byte[] clientCertificateSignature = signerCreds.generateCertificateSignature(
                                md5andsha1);
                            sendCertificateVerify(clientCertificateSignature);

                            connection_state = CS_CERTIFICATE_VERIFY_SEND;
                        }

                        /*
                         * Now, we send change cipher state
                         */
                        byte[] cmessage = new byte[1];
                        cmessage[0] = 1;
                        rs.writeMessage(ContentType.change_cipher_spec, cmessage, 0,
                            cmessage.length);

                        connection_state = CS_CLIENT_CHANGE_CIPHER_SPEC_SEND;

                        /*
                         * Calculate the master_secret
                         */
                        byte[] pms = this.keyExchange.generatePremasterSecret();

                        securityParameters.masterSecret = TlsUtils.PRF(pms, "master secret",
                            TlsUtils.concat(securityParameters.clientRandom,
                                securityParameters.serverRandom), 48);

                        // TODO Is there a way to ensure the data is really overwritten?
                        /*
                         * RFC 2246 8.1. The pre_master_secret should be deleted from
                         * memory once the master_secret has been computed.
                         */
                        Arrays.fill(pms, (byte)0);

                        /*
                         * Initialize our cipher suite
                         */
                        rs.clientCipherSpecDecided(tlsClient.getCompression(), tlsClient.getCipher());

                        /*
                         * Send our finished message.
                         */
                        byte[] clientVerifyData = TlsUtils.PRF(securityParameters.masterSecret,
                            "client finished", rs.getCurrentHash(), 12);

                        ByteArrayOutputStream bos = new ByteArrayOutputStream();
                        TlsUtils.writeUint8(HandshakeType.finished, bos);
                        TlsUtils.writeOpaque24(clientVerifyData, bos);
                        byte[] message = bos.toByteArray();

                        rs.writeMessage(ContentType.handshake, message, 0, message.length);

                        this.connection_state = CS_CLIENT_FINISHED_SEND;
                        break;
                    default:
                        this.failWithError(AlertLevel.fatal, AlertDescription.handshake_failure);
                }
                break;
            case HandshakeType.server_key_exchange:
            {
                switch (connection_state)
                {
                    case CS_SERVER_HELLO_RECEIVED:

                        // There was no server certificate message; check it's OK
                        this.keyExchange.skipServerCertificate();
                        this.authentication = null;

                        // NB: Fall through to next case label

                    case CS_SERVER_CERTIFICATE_RECEIVED:

                        this.keyExchange.processServerKeyExchange(is);

                        assertEmpty(is);
                        break;

                    default:
                        this.failWithError(AlertLevel.fatal, AlertDescription.unexpected_message);
                }

                this.connection_state = CS_SERVER_KEY_EXCHANGE_RECEIVED;
                break;
            }
            case HandshakeType.certificate_request:
            {
                switch (connection_state)
                {
                    case CS_SERVER_CERTIFICATE_RECEIVED:

                        // There was no server key exchange message; check it's OK
                        this.keyExchange.skipServerKeyExchange();

                        // NB: Fall through to next case label

                    case CS_SERVER_KEY_EXCHANGE_RECEIVED:
                    {
                    	if (this.authentication == null)
                    	{
                            /*
                             * RFC 2246 7.4.4. It is a fatal handshake_failure alert
                             * for an anonymous server to request client identification.
                             */
                    		this.failWithError(AlertLevel.fatal, AlertDescription.handshake_failure);
                    	}

                        int numTypes = TlsUtils.readUint8(is);
                        short[] certificateTypes = new short[numTypes];
                        for (int i = 0; i < numTypes; ++i)
                        {
                            certificateTypes[i] = TlsUtils.readUint8(is);
                        }

                        byte[] authorities = TlsUtils.readOpaque16(is);

                        assertEmpty(is);

                        Vector authorityDNs = new Vector();

                        ByteArrayInputStream bis = new ByteArrayInputStream(authorities);
                        while (bis.available() > 0)
                        {
                            byte[] dnBytes = TlsUtils.readOpaque16(bis);
                            authorityDNs.addElement(X500Name.getInstance(ASN1Object.fromByteArray(dnBytes)));
                        }

                        this.certificateRequest = new CertificateRequest(certificateTypes,
                            authorityDNs);
                        this.keyExchange.validateCertificateRequest(this.certificateRequest);

                        break;
                    }
                    default:
                        this.failWithError(AlertLevel.fatal, AlertDescription.unexpected_message);
                }

                this.connection_state = CS_CERTIFICATE_REQUEST_RECEIVED;
                break;
            }
            case HandshakeType.hello_request:
                /*
                 * RFC 2246 7.4.1.1 Hello request This message will be ignored by the
                 * client if the client is currently negotiating a session. This message
                 * may be ignored by the client if it does not wish to renegotiate a
                 * session, or the client may, if it wishes, respond with a
                 * no_renegotiation alert.
                 */
                if (connection_state == CS_DONE)
                {
                    // Renegotiation not supported yet
                    sendAlert(AlertLevel.warning, AlertDescription.no_renegotiation);
                }
                break;
            case HandshakeType.client_key_exchange:
            case HandshakeType.certificate_verify:
            case HandshakeType.client_hello:
            default:
                // We do not support this!
                this.failWithError(AlertLevel.fatal, AlertDescription.unexpected_message);
                break;
        }
    }

    private void processApplicationData()
    {
        /*
         * There is nothing we need to do here.
         * 
         * This function could be used for callbacks when application data arrives in the
         * future.
         */
    }

    private void processAlert() throws IOException
    {
        while (alertQueue.size() >= 2)
        {
            /*
             * An alert is always 2 bytes. Read the alert.
             */
            byte[] tmp = new byte[2];
            alertQueue.read(tmp, 0, 2, 0);
            alertQueue.removeData(2);
            short level = tmp[0];
            short description = tmp[1];
            if (level == AlertLevel.fatal)
            {
                /*
                 * This is a fatal error.
                 */
                this.failedWithError = true;
                this.closed = true;
                /*
                 * Now try to close the stream, ignore errors.
                 */
                try
                {
                    rs.close();
                }
                catch (Exception e)
                {

                }
                throw new IOException(TLS_ERROR_MESSAGE);
            }
            else
            {
                /*
                 * This is just a warning.
                 */
                if (description == AlertDescription.close_notify)
                {
                    /*
                     * Close notify
                     */
                    this.failWithError(AlertLevel.warning, AlertDescription.close_notify);
                }
                /*
                 * If it is just a warning, we continue.
                 */
            }
        }
    }

    /**
     * This method is called, when a change cipher spec message is received.
     * 
     * @throws IOException If the message has an invalid content or the handshake is not
     *             in the correct state.
     */
    private void processChangeCipherSpec() throws IOException
    {
        while (changeCipherSpecQueue.size() > 0)
        {
            /*
             * A change cipher spec message is only one byte with the value 1.
             */
            byte[] b = new byte[1];
            changeCipherSpecQueue.read(b, 0, 1, 0);
            changeCipherSpecQueue.removeData(1);
            if (b[0] != 1)
            {
                /*
                 * This should never happen.
                 */
                this.failWithError(AlertLevel.fatal, AlertDescription.unexpected_message);
            }

            /*
             * Check if we are in the correct connection state.
             */
            if (this.connection_state != CS_CLIENT_FINISHED_SEND)
            {
                this.failWithError(AlertLevel.fatal, AlertDescription.handshake_failure);
            }

            rs.serverClientSpecReceived();

            this.connection_state = CS_SERVER_CHANGE_CIPHER_SPEC_RECEIVED;
        }
    }

    private void sendClientCertificate(Certificate clientCert) throws IOException
    {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TlsUtils.writeUint8(HandshakeType.certificate, bos);
        clientCert.encode(bos);
        byte[] message = bos.toByteArray();

        rs.writeMessage(ContentType.handshake, message, 0, message.length);
    }

    private void sendClientKeyExchange() throws IOException
    {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TlsUtils.writeUint8(HandshakeType.client_key_exchange, bos);
        this.keyExchange.generateClientKeyExchange(bos);
        byte[] message = bos.toByteArray();

        rs.writeMessage(ContentType.handshake, message, 0, message.length);
    }

    private void sendCertificateVerify(byte[] data) throws IOException
    {
        /*
         * Send signature of handshake messages so far to prove we are the owner of the
         * cert See RFC 2246 sections 4.7, 7.4.3 and 7.4.8
         */
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TlsUtils.writeUint8(HandshakeType.certificate_verify, bos);
        TlsUtils.writeUint24(data.length + 2, bos);
        TlsUtils.writeOpaque16(data, bos);
        byte[] message = bos.toByteArray();

        rs.writeMessage(ContentType.handshake, message, 0, message.length);
    }

    /**
     * Connects to the remote system.
     * 
     * @param verifyer Will be used when a certificate is received to verify that this
     *            certificate is accepted by the client.
     * @throws IOException If handshake was not successful.
     * 
     * @deprecated use version taking TlsClient
     */
    public void connect(CertificateVerifyer verifyer) throws IOException
    {
        this.connect(new LegacyTlsClient(verifyer));
    }

    /**
     * Connects to the remote system using client authentication
     * 
     * @param tlsClient
     * @throws IOException If handshake was not successful.
     */
    public void connect(TlsClient tlsClient) throws IOException
    {
        if (tlsClient == null)
        {
            throw new IllegalArgumentException("'tlsClient' cannot be null");
        }
        if (this.tlsClient != null)
        {
            throw new IllegalStateException("connect can only be called once");
        }

        /*
         * Send Client hello
         * 
         * First, generate some random data.
         */
        this.securityParameters = new SecurityParameters();
        this.securityParameters.clientRandom = new byte[32];
        random.nextBytes(securityParameters.clientRandom);
        TlsUtils.writeGMTUnixTime(securityParameters.clientRandom, 0);

        this.tlsClientContext = new TlsClientContextImpl(random, securityParameters);
        this.tlsClient = tlsClient;
        this.tlsClient.init(tlsClientContext);

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        TlsUtils.writeVersion(os);
        os.write(securityParameters.clientRandom);

        /*
         * Length of Session id
         */
        TlsUtils.writeUint8((short)0, os);

        /*
         * Cipher suites
         */
        this.offeredCipherSuites = this.tlsClient.getCipherSuites();

        // Integer -> byte[]
        this.clientExtensions = this.tlsClient.getClientExtensions();

        // Cipher Suites (and SCSV)
        {
            /*
             * RFC 5746 3.4. The client MUST include either an empty "renegotiation_info"
             * extension, or the TLS_EMPTY_RENEGOTIATION_INFO_SCSV signaling cipher suite
             * value in the ClientHello. Including both is NOT RECOMMENDED.
             */
            boolean noRenegExt = clientExtensions == null
                || clientExtensions.get(EXT_RenegotiationInfo) == null;

            int count = offeredCipherSuites.length;
            if (noRenegExt)
            {
                // Note: 1 extra slot for TLS_EMPTY_RENEGOTIATION_INFO_SCSV
                ++count;
            }

            TlsUtils.writeUint16(2 * count, os);
            TlsUtils.writeUint16Array(offeredCipherSuites, os);

            if (noRenegExt)
            {
                TlsUtils.writeUint16(CipherSuite.TLS_EMPTY_RENEGOTIATION_INFO_SCSV, os);
            }
        }

        // Compression methods
        this.offeredCompressionMethods = this.tlsClient.getCompressionMethods();

        TlsUtils.writeUint8((short)offeredCompressionMethods.length, os);
        TlsUtils.writeUint8Array(offeredCompressionMethods, os);

        // Extensions
        if (clientExtensions != null)
        {
            ByteArrayOutputStream ext = new ByteArrayOutputStream();

            Enumeration keys = clientExtensions.keys();
            while (keys.hasMoreElements())
            {
                Integer extType = (Integer)keys.nextElement();
                writeExtension(ext, extType, (byte[])clientExtensions.get(extType));
            }

            TlsUtils.writeOpaque16(ext.toByteArray(), os);
        }

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TlsUtils.writeUint8(HandshakeType.client_hello, bos);
        TlsUtils.writeUint24(os.size(), bos);
        bos.write(os.toByteArray());
        byte[] message = bos.toByteArray();

        safeWriteMessage(ContentType.handshake, message, 0, message.length);

        connection_state = CS_CLIENT_HELLO_SEND;

        /*
         * We will now read data, until we have completed the handshake.
         */
        while (connection_state != CS_DONE)
        {
            safeReadData();
        }

        this.tlsInputStream = new TlsInputStream(this);
        this.tlsOutputStream = new TlsOutputStream(this);
    }

    /**
     * Read data from the network. The method will return immediately, if there is still
     * some data left in the buffer, or block until some application data has been read
     * from the network.
     * 
     * @param buf The buffer where the data will be copied to.
     * @param offset The position where the data will be placed in the buffer.
     * @param len The maximum number of bytes to read.
     * @return The number of bytes read.
     * @throws IOException If something goes wrong during reading data.
     */
    protected int readApplicationData(byte[] buf, int offset, int len) throws IOException
    {
        while (applicationDataQueue.size() == 0)
        {
            /*
             * We need to read some data.
             */
            if (this.closed)
            {
                if (this.failedWithError)
                {
                    /*
                     * Something went terribly wrong, we should throw an IOException
                     */
                    throw new IOException(TLS_ERROR_MESSAGE);
                }

                /*
                 * Connection has been closed, there is no more data to read.
                 */
                return -1;
            }

            safeReadData();
        }
        len = Math.min(len, applicationDataQueue.size());
        applicationDataQueue.read(buf, offset, len, 0);
        applicationDataQueue.removeData(len);
        return len;
    }

    private void safeReadData() throws IOException
    {
        try
        {
            rs.readData();
        }
        catch (TlsFatalAlert e)
        {
            if (!this.closed)
            {
                this.failWithError(AlertLevel.fatal, e.getAlertDescription());
            }
            throw e;
        }
        catch (IOException e)
        {
            if (!this.closed)
            {
                this.failWithError(AlertLevel.fatal, AlertDescription.internal_error);
            }
            throw e;
        }
        catch (RuntimeException e)
        {
            if (!this.closed)
            {
                this.failWithError(AlertLevel.fatal, AlertDescription.internal_error);
            }
            throw e;
        }
    }

    private void safeWriteMessage(short type, byte[] buf, int offset, int len) throws IOException
    {
        try
        {
            rs.writeMessage(type, buf, offset, len);
        }
        catch (TlsFatalAlert e)
        {
            if (!this.closed)
            {
                this.failWithError(AlertLevel.fatal, e.getAlertDescription());
            }
            throw e;
        }
        catch (IOException e)
        {
            if (!closed)
            {
                this.failWithError(AlertLevel.fatal, AlertDescription.internal_error);
            }
            throw e;
        }
        catch (RuntimeException e)
        {
            if (!closed)
            {
                this.failWithError(AlertLevel.fatal, AlertDescription.internal_error);
            }
            throw e;
        }
    }

    /**
     * Send some application data to the remote system.
     * <p/>
     * The method will handle fragmentation internally.
     * 
     * @param buf The buffer with the data.
     * @param offset The position in the buffer where the data is placed.
     * @param len The length of the data.
     * @throws IOException If something goes wrong during sending.
     */
    protected void writeData(byte[] buf, int offset, int len) throws IOException
    {
        if (this.closed)
        {
            if (this.failedWithError)
            {
                throw new IOException(TLS_ERROR_MESSAGE);
            }

            throw new IOException("Sorry, connection has been closed, you cannot write more data");
        }

        /*
         * Protect against known IV attack!
         * 
         * DO NOT REMOVE THIS LINE, EXCEPT YOU KNOW EXACTLY WHAT YOU ARE DOING HERE.
         */
        safeWriteMessage(ContentType.application_data, emptybuf, 0, 0);

        do
        {
            /*
             * We are only allowed to write fragments up to 2^14 bytes.
             */
            int toWrite = Math.min(len, 1 << 14);

            safeWriteMessage(ContentType.application_data, buf, offset, toWrite);

            offset += toWrite;
            len -= toWrite;
        }
        while (len > 0);

    }

    /**
     * @return An OutputStream which can be used to send data.
     */
    public OutputStream getOutputStream()
    {
        return this.tlsOutputStream;
    }

    /**
     * @return An InputStream which can be used to read data.
     */
    public InputStream getInputStream()
    {
        return this.tlsInputStream;
    }

    /**
     * Terminate this connection with an alert.
     * <p/>
     * Can be used for normal closure too.
     * 
     * @param alertLevel The level of the alert, an be AlertLevel.fatal or AL_warning.
     * @param alertDescription The exact alert message.
     * @throws IOException If alert was fatal.
     */
    private void failWithError(short alertLevel, short alertDescription) throws IOException
    {
        /*
         * Check if the connection is still open.
         */
        if (!closed)
        {
            /*
             * Prepare the message
             */
            this.closed = true;

            if (alertLevel == AlertLevel.fatal)
            {
                /*
                 * This is a fatal message.
                 */
                this.failedWithError = true;
            }
            sendAlert(alertLevel, alertDescription);
            rs.close();
            if (alertLevel == AlertLevel.fatal)
            {
                throw new IOException(TLS_ERROR_MESSAGE);
            }
        }
        else
        {
            throw new IOException(TLS_ERROR_MESSAGE);
        }
    }

    private void sendAlert(short alertLevel, short alertDescription) throws IOException
    {
        byte[] error = new byte[2];
        error[0] = (byte)alertLevel;
        error[1] = (byte)alertDescription;

        rs.writeMessage(ContentType.alert, error, 0, 2);
    }

    /**
     * Closes this connection.
     * 
     * @throws IOException If something goes wrong during closing.
     */
    public void close() throws IOException
    {
        if (!closed)
        {
            this.failWithError(AlertLevel.warning, AlertDescription.close_notify);
        }
    }

    /**
     * Make sure the InputStream is now empty. Fail otherwise.
     * 
     * @param is The InputStream to check.
     * @throws IOException If is is not empty.
     */
    protected void assertEmpty(ByteArrayInputStream is) throws IOException
    {
        if (is.available() > 0)
        {
            throw new TlsFatalAlert(AlertDescription.decode_error);
        }
    }

    protected void flush() throws IOException
    {
        rs.flush();
    }

    private static boolean arrayContains(short[] a, short n)
    {
        for (int i = 0; i < a.length; ++i)
        {
            if (a[i] == n)
            {
                return true;
            }
        }
        return false;
    }

    private static boolean arrayContains(int[] a, int n)
    {
        for (int i = 0; i < a.length; ++i)
        {
            if (a[i] == n)
            {
                return true;
            }
        }
        return false;
    }

    private static byte[] createRenegotiationInfo(byte[] renegotiated_connection)
        throws IOException
    {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        TlsUtils.writeOpaque8(renegotiated_connection, buf);
        return buf.toByteArray();
    }

    private static void writeExtension(OutputStream output, Integer extType, byte[] extValue)
        throws IOException
    {
        TlsUtils.writeUint16(extType.intValue(), output);
        TlsUtils.writeOpaque16(extValue, output);
    }
}
