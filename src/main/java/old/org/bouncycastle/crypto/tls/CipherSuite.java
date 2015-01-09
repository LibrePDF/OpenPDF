package old.org.bouncycastle.crypto.tls;

/**
 * RFC 2246 A.5
 */
public class CipherSuite
{
    public static final int TLS_NULL_WITH_NULL_NULL = 0x0000;
    public static final int TLS_RSA_WITH_NULL_MD5 = 0x0001;
    public static final int TLS_RSA_WITH_NULL_SHA = 0x0002;
    public static final int TLS_RSA_EXPORT_WITH_RC4_40_MD5 = 0x0003;
    public static final int TLS_RSA_WITH_RC4_128_MD5 = 0x0004;
    public static final int TLS_RSA_WITH_RC4_128_SHA = 0x0005;
    public static final int TLS_RSA_EXPORT_WITH_RC2_CBC_40_MD5 = 0x0006;
    public static final int TLS_RSA_WITH_IDEA_CBC_SHA = 0x0007;
    public static final int TLS_RSA_EXPORT_WITH_DES40_CBC_SHA = 0x0008;
    public static final int TLS_RSA_WITH_DES_CBC_SHA = 0x0009;
    public static final int TLS_RSA_WITH_3DES_EDE_CBC_SHA = 0x000A;
    public static final int TLS_DH_DSS_EXPORT_WITH_DES40_CBC_SHA = 0x000B;
    public static final int TLS_DH_DSS_WITH_DES_CBC_SHA = 0x000C;
    public static final int TLS_DH_DSS_WITH_3DES_EDE_CBC_SHA = 0x000D;
    public static final int TLS_DH_RSA_EXPORT_WITH_DES40_CBC_SHA = 0x000E;
    public static final int TLS_DH_RSA_WITH_DES_CBC_SHA = 0x000F;
    public static final int TLS_DH_RSA_WITH_3DES_EDE_CBC_SHA = 0x0010;
    public static final int TLS_DHE_DSS_EXPORT_WITH_DES40_CBC_SHA = 0x0011;
    public static final int TLS_DHE_DSS_WITH_DES_CBC_SHA = 0x0012;
    public static final int TLS_DHE_DSS_WITH_3DES_EDE_CBC_SHA = 0x0013;
    public static final int TLS_DHE_RSA_EXPORT_WITH_DES40_CBC_SHA = 0x0014;
    public static final int TLS_DHE_RSA_WITH_DES_CBC_SHA = 0x0015;
    public static final int TLS_DHE_RSA_WITH_3DES_EDE_CBC_SHA = 0x0016;
    public static final int TLS_DH_anon_EXPORT_WITH_RC4_40_MD5 = 0x0017;
    public static final int TLS_DH_anon_WITH_RC4_128_MD5 = 0x0018;
    public static final int TLS_DH_anon_EXPORT_WITH_DES40_CBC_SHA = 0x0019;
    public static final int TLS_DH_anon_WITH_DES_CBC_SHA = 0x001A;
    public static final int TLS_DH_anon_WITH_3DES_EDE_CBC_SHA = 0x001B;

    /*
     * RFC 3268
     */
    public static final int TLS_RSA_WITH_AES_128_CBC_SHA = 0x002F;
    public static final int TLS_DH_DSS_WITH_AES_128_CBC_SHA = 0x0030;
    public static final int TLS_DH_RSA_WITH_AES_128_CBC_SHA = 0x0031;
    public static final int TLS_DHE_DSS_WITH_AES_128_CBC_SHA = 0x0032;
    public static final int TLS_DHE_RSA_WITH_AES_128_CBC_SHA = 0x0033;
    public static final int TLS_DH_anon_WITH_AES_128_CBC_SHA = 0x0034;
    public static final int TLS_RSA_WITH_AES_256_CBC_SHA = 0x0035;
    public static final int TLS_DH_DSS_WITH_AES_256_CBC_SHA = 0x0036;
    public static final int TLS_DH_RSA_WITH_AES_256_CBC_SHA = 0x0037;
    public static final int TLS_DHE_DSS_WITH_AES_256_CBC_SHA = 0x0038;
    public static final int TLS_DHE_RSA_WITH_AES_256_CBC_SHA = 0x0039;
    public static final int TLS_DH_anon_WITH_AES_256_CBC_SHA = 0x003A;

    /*
     * RFC 4279
     */
    public static final int TLS_PSK_WITH_RC4_128_SHA = 0x008A;
    public static final int TLS_PSK_WITH_3DES_EDE_CBC_SHA = 0x008B;
    public static final int TLS_PSK_WITH_AES_128_CBC_SHA = 0x008C;
    public static final int TLS_PSK_WITH_AES_256_CBC_SHA = 0x008D;
    public static final int TLS_DHE_PSK_WITH_RC4_128_SHA = 0x008E;
    public static final int TLS_DHE_PSK_WITH_3DES_EDE_CBC_SHA = 0x008F;
    public static final int TLS_DHE_PSK_WITH_AES_128_CBC_SHA = 0x0090;
    public static final int TLS_DHE_PSK_WITH_AES_256_CBC_SHA = 0x0091;
    public static final int TLS_RSA_PSK_WITH_RC4_128_SHA = 0x0092;
    public static final int TLS_RSA_PSK_WITH_3DES_EDE_CBC_SHA = 0x0093;
    public static final int TLS_RSA_PSK_WITH_AES_128_CBC_SHA = 0x0094;
    public static final int TLS_RSA_PSK_WITH_AES_256_CBC_SHA = 0x0095;

    /*
     * RFC 4492
     */
    public static final int TLS_ECDH_ECDSA_WITH_NULL_SHA = 0xC001;
    public static final int TLS_ECDH_ECDSA_WITH_RC4_128_SHA = 0xC002;
    public static final int TLS_ECDH_ECDSA_WITH_3DES_EDE_CBC_SHA = 0xC003;
    public static final int TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA = 0xC004;
    public static final int TLS_ECDH_ECDSA_WITH_AES_256_CBC_SHA = 0xC005;
    public static final int TLS_ECDHE_ECDSA_WITH_NULL_SHA = 0xC006;
    public static final int TLS_ECDHE_ECDSA_WITH_RC4_128_SHA = 0xC007;
    public static final int TLS_ECDHE_ECDSA_WITH_3DES_EDE_CBC_SHA = 0xC008;
    public static final int TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA = 0xC009;
    public static final int TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA = 0xC00A;
    public static final int TLS_ECDH_RSA_WITH_NULL_SHA = 0xC00B;
    public static final int TLS_ECDH_RSA_WITH_RC4_128_SHA = 0xC00C;
    public static final int TLS_ECDH_RSA_WITH_3DES_EDE_CBC_SHA = 0xC00D;
    public static final int TLS_ECDH_RSA_WITH_AES_128_CBC_SHA = 0xC00E;
    public static final int TLS_ECDH_RSA_WITH_AES_256_CBC_SHA = 0xC00F;
    public static final int TLS_ECDHE_RSA_WITH_NULL_SHA = 0xC010;
    public static final int TLS_ECDHE_RSA_WITH_RC4_128_SHA = 0xC011;
    public static final int TLS_ECDHE_RSA_WITH_3DES_EDE_CBC_SHA = 0xC012;
    public static final int TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA = 0xC013;
    public static final int TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA = 0xC014;
    public static final int TLS_ECDH_anon_WITH_NULL_SHA = 0xC015;
    public static final int TLS_ECDH_anon_WITH_RC4_128_SHA = 0xC016;
    public static final int TLS_ECDH_anon_WITH_3DES_EDE_CBC_SHA = 0xC017;
    public static final int TLS_ECDH_anon_WITH_AES_128_CBC_SHA = 0xC018;
    public static final int TLS_ECDH_anon_WITH_AES_256_CBC_SHA = 0xC019;

    /*
     * RFC 5054
     */
    public static final int TLS_SRP_SHA_WITH_3DES_EDE_CBC_SHA = 0xC01A;
    public static final int TLS_SRP_SHA_RSA_WITH_3DES_EDE_CBC_SHA = 0xC01B;
    public static final int TLS_SRP_SHA_DSS_WITH_3DES_EDE_CBC_SHA = 0xC01C;
    public static final int TLS_SRP_SHA_WITH_AES_128_CBC_SHA = 0xC01D;
    public static final int TLS_SRP_SHA_RSA_WITH_AES_128_CBC_SHA = 0xC01E;
    public static final int TLS_SRP_SHA_DSS_WITH_AES_128_CBC_SHA = 0xC01F;
    public static final int TLS_SRP_SHA_WITH_AES_256_CBC_SHA = 0xC020;
    public static final int TLS_SRP_SHA_RSA_WITH_AES_256_CBC_SHA = 0xC021;
    public static final int TLS_SRP_SHA_DSS_WITH_AES_256_CBC_SHA = 0xC022;

    /*
     * RFC 5289
     */
    public static final int TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA256 = 0xC023;
    public static final int TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA384 = 0xC024;
    public static final int TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA256 = 0xC025;
    public static final int TLS_ECDH_ECDSA_WITH_AES_256_CBC_SHA384 = 0xC026;
    public static final int TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256 = 0xC027;
    public static final int TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384 = 0xC028;
    public static final int TLS_ECDH_RSA_WITH_AES_128_CBC_SHA256 = 0xC029;
    public static final int TLS_ECDH_RSA_WITH_AES_256_CBC_SHA384 = 0xC02A;
    public static final int TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256 = 0xC02B;
    public static final int TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384 = 0xC02C;
    public static final int TLS_ECDH_ECDSA_WITH_AES_128_GCM_SHA256 = 0xC02D;
    public static final int TLS_ECDH_ECDSA_WITH_AES_256_GCM_SHA384 = 0xC02E;
    public static final int TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256 = 0xC02F;
    public static final int TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384 = 0xC030;
    public static final int TLS_ECDH_RSA_WITH_AES_128_GCM_SHA256 = 0xC031;
    public static final int TLS_ECDH_RSA_WITH_AES_256_GCM_SHA384 = 0xC032;

    /*
     * RFC 5746
     */
    public static final int TLS_EMPTY_RENEGOTIATION_INFO_SCSV = 0x00FF;
}
