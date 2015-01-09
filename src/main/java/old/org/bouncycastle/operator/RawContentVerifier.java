package old.org.bouncycastle.operator;

/**
 * Interface for ContentVerifiers that also support raw signatures that can be
 * verified using the digest of the calculated data.
 */
public interface RawContentVerifier
{
    /**
     * Verify that the expected signature value was derived from the passed in digest.
     *
     * @param digest digest calculated from the content.
     * @param expected expected value of the signature
     * @return true if the expected signature is derived from the digest, false otherwise.
     */
    boolean verify(byte[] digest, byte[] expected);
}
