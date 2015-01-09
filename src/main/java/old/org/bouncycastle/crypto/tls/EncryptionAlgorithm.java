package old.org.bouncycastle.crypto.tls;

public class EncryptionAlgorithm
{
    /*
     * Note that the values here are implementation-specific and arbitrary.
     * It is recommended not to depend on the particular values (e.g. serialization).
     */
    public static final int NULL = 0;
    public static final int RC4_40 = 1;
    public static final int RC4_128 = 2;
    public static final int RC2_CBC_40 = 3;
    public static final int IDEA_CBC = 4;
    public static final int DES40_CBC = 5;
    public static final int DES_CBC = 6;
    public static final int _3DES_EDE_CBC = 7;

    /*
     * RFC 3268
     */
    public static final int AES_128_CBC = 8;
    public static final int AES_256_CBC = 9;

    /*
     * RFC 5289
     */
    public static final int AES_128_GCM = 10;
    public static final int AES_256_GCM = 11;
}
