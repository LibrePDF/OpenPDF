package old.org.bouncycastle.i18n;

import java.util.Locale;

/**
 * Base class for all Exceptions with localized messages.
 */
public class LocalizedException extends Exception 
{

    protected ErrorBundle message;
    private Throwable cause;
    
    /**
     * Constructs a new LocalizedException with the specified localized message.
     * @param message the {@link ErrorBundle} that contains the message for the exception
     */
    public LocalizedException(ErrorBundle message) 
    {
        super(message.getText(Locale.getDefault()));
        this.message = message;
    }
    
    /**
     * Constructs a new LocalizedException with the specified localized message and cause.
     * @param message the {@link ErrorBundle} that contains the message for the exception
     * @param throwable the cause
     */
    public LocalizedException(ErrorBundle message, Throwable throwable) 
    {
        super(message.getText(Locale.getDefault()));
        this.message = message;
        this.cause = throwable;
    }
    
    /**
     * Returns the localized error message of the exception.
     * @return the localized error message as {@link ErrorBundle}
     */
    public ErrorBundle getErrorMessage() 
    {
        return message;
    }

    public Throwable getCause()
    {
        return cause;
    }
}
