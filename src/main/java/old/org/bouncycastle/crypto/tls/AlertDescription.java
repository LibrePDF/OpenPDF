package old.org.bouncycastle.crypto.tls;

/**
 * RFC 2246 7.2
 */
public class AlertDescription
{
    public static final short close_notify = 0;
    public static final short unexpected_message = 10;
    public static final short bad_record_mac = 20;
    public static final short decryption_failed = 21;
    public static final short record_overflow = 22;
    public static final short decompression_failure = 30;
    public static final short handshake_failure = 40;
    /* 41 is not defined, for historical reasons */
    public static final short bad_certificate = 42;
    public static final short unsupported_certificate = 43;
    public static final short certificate_revoked = 44;
    public static final short certificate_expired = 45;
    public static final short certificate_unknown = 46;
    public static final short illegal_parameter = 47;
    public static final short unknown_ca = 48;
    public static final short access_denied = 49;
    public static final short decode_error = 50;
    public static final short decrypt_error = 51;
    public static final short export_restriction = 60;
    public static final short protocol_version = 70;
    public static final short insufficient_security = 71;
    public static final short internal_error = 80;
    public static final short user_canceled = 90;
    public static final short no_renegotiation = 100;

    /*
     *  RFC 3546
     */
    public static final short unsupported_extension = 110;
    public static final short certificate_unobtainable = 111;
    public static final short unrecognized_name = 112;
    public static final short bad_certificate_status_response = 113;
    public static final short bad_certificate_hash_value = 114;

    /*
     *  RFC 4279
     */
    public static final short unknown_psk_identity = 115;
}
