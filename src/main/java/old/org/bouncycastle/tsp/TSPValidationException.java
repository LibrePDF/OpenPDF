package old.org.bouncycastle.tsp;

/**
 * Exception thrown if a TSP request or response fails to validate.
 * <p>
 * If a failure code is associated with the exception it can be retrieved using
 * the getFailureCode() method.
 */
public class TSPValidationException
    extends TSPException
{
    private int failureCode = -1;
    
    public TSPValidationException(String message)
    {
        super(message);
    }

    public TSPValidationException(String message, int failureCode)
    {
        super(message);
        this.failureCode = failureCode;
    }
    
    /**
     * Return the failure code associated with this exception - if one is set.
     * 
     * @return the failure code if set, -1 otherwise.
     */
    public int getFailureCode()
    {
        return failureCode;
    }
}
