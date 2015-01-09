package old.org.bouncycastle.crypto.tls;

/**
 * RFC 2246 7.4
 */
public class HandshakeType
{
    public static final short hello_request = 0;
    public static final short client_hello = 1;
    public static final short server_hello = 2;
    public static final short certificate = 11;
    public static final short server_key_exchange = 12;
    public static final short certificate_request = 13;
    public static final short server_hello_done = 14;
    public static final short certificate_verify = 15;
    public static final short client_key_exchange = 16;
    public static final short finished = 20;
}
