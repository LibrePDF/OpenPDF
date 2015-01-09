package old.org.bouncycastle.crypto.tls;

/**
 * RFC 2246 7.4.4
 */
public class ClientCertificateType
{
    public static final short rsa_sign = 1;
    public static final short dss_sign = 2;
    public static final short rsa_fixed_dh = 3;
    public static final short dss_fixed_dh = 4;

    /*
     * RFC 4492 5.5
     */
    public static final short ecdsa_sign = 64;
    public static final short rsa_fixed_ecdh = 65;
    public static final short ecdsa_fixed_ecdh = 66;
}
