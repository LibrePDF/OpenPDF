package old.org.bouncycastle.i18n;

import old.org.bouncycastle.i18n.filter.Filter;
import old.org.bouncycastle.i18n.filter.TrustedInput;
import old.org.bouncycastle.i18n.filter.UntrustedInput;
import old.org.bouncycastle.i18n.filter.UntrustedUrlInput;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.Format;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.TimeZone;

public class LocalizedMessage 
{

    protected final String id;
    protected final String resource;
    
    // ISO-8859-1 is the default encoding
    public static final String DEFAULT_ENCODING = "ISO-8859-1";
    protected String encoding = DEFAULT_ENCODING;
    
    protected FilteredArguments arguments;
    protected FilteredArguments extraArgs = null;
    
    protected Filter filter = null;
    
    protected ClassLoader loader = null;
    
    /**
     * Constructs a new LocalizedMessage using <code>resource</code> as the base name for the 
     * RessourceBundle and <code>id</code> as the message bundle id the resource file. 
     * @param resource base name of the resource file 
     * @param id the id of the corresponding bundle in the resource file
     * @throws NullPointerException if <code>resource</code> or <code>id</code> is <code>null</code>
     */
    public LocalizedMessage(String resource,String id) throws NullPointerException
    {
        if (resource == null || id == null)
        {
            throw new NullPointerException();
        }
        this.id = id;
        this.resource = resource;
        arguments = new FilteredArguments();
    }
    
    /**
     * Constructs a new LocalizedMessage using <code>resource</code> as the base name for the 
     * RessourceBundle and <code>id</code> as the message bundle id the resource file. 
     * @param resource base name of the resource file 
     * @param id the id of the corresponding bundle in the resource file
     * @param encoding the encoding of the resource file
     * @throws NullPointerException if <code>resource</code> or <code>id</code> is <code>null</code>
     * @throws UnsupportedEncodingException if the encoding is not supported
     */
    public LocalizedMessage(String resource,String id, String encoding) throws NullPointerException, UnsupportedEncodingException
    {
        if (resource == null || id == null)
        {
            throw new NullPointerException();
        }
        this.id = id;
        this.resource = resource;
        arguments = new FilteredArguments();
        if (!Charset.isSupported(encoding))
        {
            throw new UnsupportedEncodingException("The encoding \"" + encoding + "\" is not supported.");
        }
        this.encoding = encoding;
    }
    
    /**
     * Constructs a new LocalizedMessage using <code>resource</code> as the base name for the 
     * RessourceBundle and <code>id</code> as the message bundle id the resource file. 
     * @param resource base name of the resource file 
     * @param id the id of the corresponding bundle in the resource file
     * @param arguments an array containing the arguments for the message
     * @throws NullPointerException if <code>resource</code> or <code>id</code> is <code>null</code>
     */
    public LocalizedMessage(String resource, String id, Object[] arguments) throws NullPointerException
    {
        if (resource == null || id == null || arguments == null)
        {
            throw new NullPointerException();
        }
        this.id = id;
        this.resource = resource;
        this.arguments = new FilteredArguments(arguments);
    }
    
    /**
     * Constructs a new LocalizedMessage using <code>resource</code> as the base name for the 
     * RessourceBundle and <code>id</code> as the message bundle id the resource file. 
     * @param resource base name of the resource file 
     * @param id the id of the corresponding bundle in the resource file
     * @param encoding the encoding of the resource file
     * @param arguments an array containing the arguments for the message
     * @throws NullPointerException if <code>resource</code> or <code>id</code> is <code>null</code>
     * @throws UnsupportedEncodingException if the encoding is not supported
     */
    public LocalizedMessage(String resource, String id, String encoding, Object[] arguments) throws NullPointerException, UnsupportedEncodingException
    {
        if (resource == null || id == null || arguments == null)
        {
            throw new NullPointerException();
        }
        this.id = id;
        this.resource = resource;
        this.arguments = new FilteredArguments(arguments);
        if (!Charset.isSupported(encoding))
        {
            throw new UnsupportedEncodingException("The encoding \"" + encoding + "\" is not supported.");
        }
        this.encoding = encoding;
    }
    
    /**
     * Reads the entry <code>id + "." + key</code> from the resource file and returns a 
     * formated message for the given Locale and TimeZone.
     * @param key second part of the entry id
     * @param loc the used {@link Locale}
     * @param timezone the used {@link TimeZone}
     * @return a Strng containing the localized message
     * @throws MissingEntryException if the resource file is not available or the entry does not exist.
     */
    public String getEntry(String key,Locale loc, TimeZone timezone) throws MissingEntryException
    {
        String entry = id;
        if (key != null)
        {
            entry += "." + key;
        }
        
        try
        {
            ResourceBundle bundle;
            if (loader == null)
            {
                bundle = ResourceBundle.getBundle(resource,loc);
            }
            else
            {
                bundle = ResourceBundle.getBundle(resource, loc, loader);
            }
            String result = bundle.getString(entry);
            if (!encoding.equals(DEFAULT_ENCODING))
            {
                result = new String(result.getBytes(DEFAULT_ENCODING), encoding);
            }
            if (!arguments.isEmpty())
            {
                result = formatWithTimeZone(result,arguments.getFilteredArgs(loc),loc,timezone);
            }
            result = addExtraArgs(result, loc);
            return result;
        }
        catch (MissingResourceException mre)
        {
            throw new MissingEntryException("Can't find entry " + entry + " in resource file " + resource + ".",
                    resource,
                    entry,
                    loc,
                    loader != null ? loader : this.getClassLoader()); 
        }
        catch (UnsupportedEncodingException use)
        {
            // should never occur - cause we already test this in the constructor
            throw new RuntimeException(use);
        }
    }
    
    protected String formatWithTimeZone(
            String template,
            Object[] arguments, 
            Locale locale,
            TimeZone timezone) 
    {
        MessageFormat mf = new MessageFormat(" ");
        mf.setLocale(locale);
        mf.applyPattern(template);
        if (!timezone.equals(TimeZone.getDefault())) 
        {
            Format[] formats = mf.getFormats();
            for (int i = 0; i < formats.length; i++) 
            {
                if (formats[i] instanceof DateFormat) 
                {
                    DateFormat temp = (DateFormat) formats[i];
                    temp.setTimeZone(timezone);
                    mf.setFormat(i,temp);
                }
            }
        }
        return mf.format(arguments);
    }
    
    protected String addExtraArgs(String msg, Locale locale)
    {
        if (extraArgs != null)
        {
            StringBuffer sb = new StringBuffer(msg);
            Object[] filteredArgs = extraArgs.getFilteredArgs(locale);
            for (int i = 0; i < filteredArgs.length; i++)
            {
                sb.append(filteredArgs[i]);
            }
            msg = sb.toString();
        }
        return msg;
    }
    
    /**
     * Sets the {@link Filter} that is used to filter the arguments of this message
     * @param filter the {@link Filter} to use. <code>null</code> to disable filtering.
     */
    public void setFilter(Filter filter)
    {
        arguments.setFilter(filter);
        if (extraArgs != null)
        {
            extraArgs.setFilter(filter);
        }
        this.filter = filter;
    }
    
    /**
     * Returns the current filter.
     * @return the current filter
     */
    public Filter getFilter()
    {
        return filter;
    }
    
    /**
     * Set the {@link ClassLoader} which loads the resource files. If it is set to <code>null</code>
     * then the default {@link ClassLoader} is used. 
     * @param loader the {@link ClassLoader} which loads the resource files
     */
    public void setClassLoader(ClassLoader loader)
    {
        this.loader = loader;
    }
    
    /**
     * Returns the {@link ClassLoader} which loads the resource files or <code>null</code>
     * if the default ClassLoader is used.
     * @return the {@link ClassLoader} which loads the resource files
     */
    public ClassLoader getClassLoader()
    {
        return loader;
    }
    
    /**
     * Returns the id of the message in the resource bundle.
     * @return the id of the message
     */
    public String getId()
    {
        return id;
    }
    
    /**
     * Returns the name of the resource bundle for this message
     * @return name of the resource file
     */
    public String getResource()
    {
        return resource;
    }
    
    /**
     * Returns an <code>Object[]</code> containing the message arguments.
     * @return the message arguments
     */
    public Object[] getArguments()
    {
        return arguments.getArguments();
    }
    
    /**
     * 
     * @param extraArg
     */
    public void setExtraArgument(Object extraArg)
    {
        setExtraArguments(new Object[] {extraArg});
    }
    
    /**
     * 
     * @param extraArgs
     */
    public void setExtraArguments(Object[] extraArgs)
    {
        if (extraArgs != null)
        {
            this.extraArgs = new FilteredArguments(extraArgs);
            this.extraArgs.setFilter(filter);
        }
        else
        {
            this.extraArgs = null;
        }
    }
    
    /**
     * 
     * @return
     */
    public Object[] getExtraArgs()
    {
        return (extraArgs == null) ? null : extraArgs.getArguments();
    }
    
    protected class FilteredArguments
    {
        protected static final int NO_FILTER = 0;
        protected static final int FILTER = 1;
        protected static final int FILTER_URL = 2;
        
        protected Filter filter = null;
        
        protected boolean[] isLocaleSpecific;
        protected int[] argFilterType;
        protected Object[] arguments;
        protected Object[] unpackedArgs;
        protected Object[] filteredArgs;
        
        FilteredArguments()
        {
            this(new Object[0]);
        }
        
        FilteredArguments(Object[] args)
        {
            this.arguments = args;
            this.unpackedArgs = new Object[args.length];
            this.filteredArgs = new Object[args.length];
            this.isLocaleSpecific = new boolean[args.length];
            this.argFilterType = new int[args.length];
            for (int i = 0; i < args.length; i++)
            {
                if (args[i] instanceof TrustedInput)
                {
                    this.unpackedArgs[i] = ((TrustedInput) args[i]).getInput();
                    argFilterType[i] = NO_FILTER;
                }
                else if (args[i] instanceof UntrustedInput)
                {
                    this.unpackedArgs[i] = ((UntrustedInput) args[i]).getInput();
                    if (args[i] instanceof UntrustedUrlInput)
                    {
                        argFilterType[i] = FILTER_URL;
                    }
                    else
                    {
                        argFilterType[i] = FILTER;
                    }
                }
                else
                {
                    this.unpackedArgs[i] = args[i];
                    argFilterType[i] = FILTER;
                }
                
                // locale specific
                this.isLocaleSpecific[i] = (this.unpackedArgs[i] instanceof LocaleString);
            }
        }
        
        public boolean isEmpty()
        {
            return unpackedArgs.length == 0;
        }
        
        public Object[] getArguments()
        {
            return arguments;
        }
        
        public Object[] getFilteredArgs(Locale locale)
        {
            Object[] result = new Object[unpackedArgs.length];
            for (int i = 0; i < unpackedArgs.length; i++)
            {
                Object arg;
                if (filteredArgs[i] != null)
                {
                    arg = filteredArgs[i];
                }
                else
                {
                    arg = unpackedArgs[i];
                    if (isLocaleSpecific[i])
                    {
                        // get locale
                        arg = ((LocaleString) arg).getLocaleString(locale);
                        arg = filter(argFilterType[i], arg);
                    }
                    else
                    {
                        arg = filter(argFilterType[i], arg);
                        filteredArgs[i] = arg;
                    }
                }
                result[i] = arg;
            }
            return result;
        }
        
        private Object filter(int type, Object obj)
        {
            if (filter != null)
            {
                Object o = (null == obj) ? "null" : obj;
                switch (type)
                {
                case NO_FILTER:
                    return o;
                case FILTER:
                    return filter.doFilter(o.toString());
                case FILTER_URL:
                    return filter.doFilterUrl(o.toString());
                default:
                    return null;
                }
            }
            else
            {
                return obj;
            }
        }

        public Filter getFilter()
        {
            return filter;
        }

        public void setFilter(Filter filter)
        {
            if (filter != this.filter)
            {
                for (int i = 0; i < unpackedArgs.length; i++)
                {
                    filteredArgs[i] = null;
                }
            }
            this.filter = filter;
        }
        
    }
    
    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        sb.append("Resource: \"").append(resource);
        sb.append("\" Id: \"").append(id).append("\"");
        sb.append(" Arguments: ").append(arguments.getArguments().length).append(" normal");
        if (extraArgs != null && extraArgs.getArguments().length > 0)
        {
            sb.append(", ").append(extraArgs.getArguments().length).append(" extra");
        }
        sb.append(" Encoding: ").append(encoding);
        sb.append(" ClassLoader: ").append(loader);
        return sb.toString();
    }

}
