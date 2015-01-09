package old.org.bouncycastle.i18n;

import java.io.UnsupportedEncodingException;
import java.util.Locale;

public class LocaleString extends LocalizedMessage
{

    public LocaleString(String resource, String id)
    {
        super(resource, id);
    }
    
    public LocaleString(String resource, String id, String encoding) throws NullPointerException, UnsupportedEncodingException
    {
        super(resource, id, encoding);
    }
    
    public String getLocaleString(Locale locale)
    {
        return this.getEntry(null, locale, null);
    }
    
}
