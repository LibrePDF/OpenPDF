package old.org.bouncycastle.i18n;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.Locale;

public class MissingEntryException extends RuntimeException 
{

    protected final String resource;
    protected final String key;
    protected final ClassLoader loader;
    protected final Locale locale;
    
    private String debugMsg;

    public MissingEntryException(String message, String resource, String key, Locale locale, ClassLoader loader) 
    {
        super(message);
        this.resource = resource;
        this.key = key;
        this.locale = locale;
        this.loader = loader;
    }
    
    public MissingEntryException(String message, Throwable cause, String resource, String key, Locale locale, ClassLoader loader) 
    {
        super(message, cause);
        this.resource = resource;
        this.key = key;
        this.locale = locale;
        this.loader = loader;
    }

    public String getKey()
    {
        return key;
    }

    public String getResource()
    {
        return resource;
    }
    
    public ClassLoader getClassLoader()
    {
        return loader;
    }
    
    public Locale getLocale()
    {
        return locale;
    }

    public String getDebugMsg()
    {
        if (debugMsg == null)
        {
            debugMsg = "Can not find entry " + key + " in resource file " + resource + " for the locale " + locale + ".";
            if (loader instanceof URLClassLoader)
            {
                URL[] urls = ((URLClassLoader) loader).getURLs();
                debugMsg += " The following entries in the classpath were searched: ";
                for (int i = 0; i != urls.length; i++)
                {
                    debugMsg += urls[i] + " ";
                }
            }
        }
        return debugMsg;
    }

}
