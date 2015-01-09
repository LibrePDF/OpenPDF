package old.org.bouncycastle.crypto.tls;

public class DigestAlgorithm
{
    /*
     * Note that the values here are implementation-specific and arbitrary.
     * It is recommended not to depend on the particular values (e.g. serialization).
     */
    public static final int NULL = 0;
    public static final int MD5 = 1;
    public static final int SHA = 2;

    /*
     * RFC 5289
     */
    public static final int SHA256 = 3;
    public static final int SHA384 = 4;
}
