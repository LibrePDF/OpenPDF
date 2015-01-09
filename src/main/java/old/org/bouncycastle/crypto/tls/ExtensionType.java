package old.org.bouncycastle.crypto.tls;

/**
 * RFC 4366 2.3
 */
public class ExtensionType
{
    public static final int server_name = 0;
    public static final int max_fragment_length = 1;
    public static final int client_certificate_url = 2;
    public static final int trusted_ca_keys = 3;
    public static final int truncated_hmac = 4;
    public static final int status_request = 5;

    /*
     * RFC 4492
     */
    public static final int elliptic_curves = 10;
    public static final int ec_point_formats = 11;

    /*
     * RFC 5054 2.8.1
     */
    public static final int srp = 12;

    /*
     * RFC 5746 6
     */
    public static final int renegotiation_info = 0xff01;
}
