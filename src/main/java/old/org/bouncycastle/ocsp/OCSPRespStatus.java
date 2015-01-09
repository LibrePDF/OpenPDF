package old.org.bouncycastle.ocsp;

public interface OCSPRespStatus
{
    /**
     * note 4 is not used.
     */
    public static final int SUCCESSFUL = 0;         // --Response has valid confirmations
    public static final int MALFORMED_REQUEST = 1;  // --Illegal confirmation request
    public static final int INTERNAL_ERROR = 2;     // --Internal error in issuer
    public static final int TRY_LATER = 3;          // --Try again later
    public static final int SIGREQUIRED = 5;        // --Must sign the request
    public static final int UNAUTHORIZED = 6;       //  --Request unauthorized
}
