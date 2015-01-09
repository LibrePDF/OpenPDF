package old.org.bouncycastle.i18n;

import java.io.UnsupportedEncodingException;
import java.util.Locale;
import java.util.TimeZone;

public class MessageBundle extends TextBundle
{

    /**
     * title entry key
     */
    public static final String TITLE_ENTRY = "title";
    
    /**
     * Constructs a new MessageBundle using <code>resource</code> as the base name for the 
     * RessourceBundle and <code>id</code> as the message bundle id the resource file. 
     * @param resource base name of the resource file 
     * @param id the id of the corresponding bundle in the resource file
     * @throws NullPointerException if <code>resource</code> or <code>id</code> is <code>null</code>
     */
    public MessageBundle(String resource, String id) throws NullPointerException
    {
        super(resource, id);
    }
    
    /**
     * Constructs a new MessageBundle using <code>resource</code> as the base name for the 
     * RessourceBundle and <code>id</code> as the message bundle id the resource file. 
     * @param resource base name of the resource file 
     * @param id the id of the corresponding bundle in the resource file
     * @param encoding the encoding of the resource file
     * @throws NullPointerException if <code>resource</code> or <code>id</code> is <code>null</code>
     * @throws UnsupportedEncodingException if the encoding is not supported
     */
    public MessageBundle(String resource, String id, String encoding) throws NullPointerException, UnsupportedEncodingException
    {
        super(resource, id, encoding);
    }

    /**
     * Constructs a new MessageBundle using <code>resource</code> as the base name for the 
     * RessourceBundle and <code>id</code> as the message bundle id the resource file. 
     * @param resource base name of the resource file 
     * @param id the id of the corresponding bundle in the resource file
     * @param arguments an array containing the arguments for the message
     * @throws NullPointerException if <code>resource</code> or <code>id</code> is <code>null</code>
     */
    public MessageBundle(String resource, String id, Object[] arguments) throws NullPointerException
    {
        super(resource, id, arguments);
    }
    
    /**
     * Constructs a new MessageBundle using <code>resource</code> as the base name for the 
     * RessourceBundle and <code>id</code> as the message bundle id the resource file. 
     * @param resource base name of the resource file 
     * @param id the id of the corresponding bundle in the resource file
     * @param encoding the encoding of the resource file
     * @param arguments an array containing the arguments for the message
     * @throws NullPointerException if <code>resource</code> or <code>id</code> is <code>null</code>
     * @throws UnsupportedEncodingException if the encoding is not supported
     */
    public MessageBundle(String resource, String id, String encoding, Object[] arguments) throws NullPointerException, UnsupportedEncodingException
    {
        super(resource, id, encoding, arguments);
    }
    
    /**
     * Returns the title message in the given locale and timezone.
     * @param loc the {@link Locale}
     * @param timezone the {@link TimeZone}
     * @return the title message.
     * @throws MissingEntryException if the message is not available
     */
    public String getTitle(Locale loc,TimeZone timezone) throws MissingEntryException
    {
        return getEntry(TITLE_ENTRY,loc,timezone);
    }
    
    /**
     * Returns the title message in the given locale and the default timezone.
     * @param loc the {@link Locale}
     * @return the title message.
     * @throws MissingEntryException if the message is not available
     */
    public String getTitle(Locale loc) throws MissingEntryException
    {
        return getEntry(TITLE_ENTRY,loc,TimeZone.getDefault());
    }

}
