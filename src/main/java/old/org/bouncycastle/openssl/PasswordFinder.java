package old.org.bouncycastle.openssl;

/**
 * call back to allow a password to be fetched when one is requested.
 */
public interface PasswordFinder
{
    public char[] getPassword();
}
