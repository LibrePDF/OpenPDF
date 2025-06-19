/*
 * {{{ header & license
 * Configuration.java
 * Copyright (c) 2004, 2005 Patrick Wright
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.	See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * }}}
 */
package org.openpdf.util;

import com.google.errorprone.annotations.CheckReturnValue;
import org.jspecify.annotations.Nullable;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import static java.lang.Boolean.parseBoolean;
import static java.lang.Byte.parseByte;
import static java.lang.Double.parseDouble;
import static java.lang.Float.parseFloat;
import static java.lang.Integer.parseInt;
import static java.lang.Long.parseLong;
import static java.lang.System.err;
import static java.nio.file.Files.newInputStream;


/**
 * <p>Stores runtime configuration information for application parameters that may
 * vary on restarting. This implements the Singleton pattern, but through static
 * methods. That is, the first time Configuration is used, the properties are
 * loaded into the Singleton instance. Subsequent calls to valueFor() retrieve
 * values from the Singleton. To look up a property, use
 * {@link Configuration#valueFor(String)}.
 * </p>
 * <p> Properties may be overridden using a second properties file, or individually
 * using System properties specified on the command line. To override using a
 * second properties file, specify the System property xr-conf. This should be
 * the location of the second file relative to the CLASSPATH, or else a file
 * path, e.g.</p>
 * {@code java -Dxr-conf=resources/conf/my-props.conf}
 * <p>
 * You can also place your override properties file in your user home directory,
 * in </p>
 * {@code ${user.home}/.flyingsaucer/local.openpdf.conf}
 * <p> To override a property using the System properties, just re-define the
 * property on the command line. e.g.</p>
 * {@code java -Dxr.property-name=new_value}
 * <p>The order in which these will be read is: default properties (bundled with
 * the core), in the jar; override configuration properties; properties file in
 * user.home; and system properties.</p>
 * <p>You can override as many properties as you like. </p>
 * <p> Note that overrides are driven by the property names in the default
 * configuration file. Specifying a property name not in that file will have no
 * effect--the property will not be loaded or available for lookup.
 * Configuration is NOT used to control logging levels or output; see
 * LogStartupConfig.</p>
 * <p>
 * There are convenience conversion method for all the primitive types, in
 * methods like {@link #valueAsInt(String, int)}. A default must always be provided for these
 * methods. The default is returned if the value is not found, or if the
 * conversion from String fails. If the value is not present, or the conversion
 * fails, a warning message is written to the log.</p>
 *
 * @author Patrick Wright
 */
@SuppressWarnings("NonConstantLogger")
public class Configuration {
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(Configuration.class);

    /**
     * Our backing data store of properties.
     */
    private final Properties properties;

    /**
     * The log Level for Configuration messages; taken from show-config System property.
     */
    private Level logLevel = Level.OFF;

    /**
     * The Singleton instance of the class.
     */
    private static final Configuration sInstance = new Configuration();

    /**
     * List of LogRecords for messages from Configuration startup; used to hold these
     * temporarily as we can't use XRLog while starting up, as it depends on Configuration.
     */
    private final List<LogRecord> startupLogRecords = new ArrayList<>();

    /**
     * Logger we use internally related to configuration.
     */
    @Nullable
    private Logger configLogger;

    /**
     * The location of our default properties file; must be on the CLASSPATH.
     */
    private static final String SF_FILE_NAME = "resources/conf/openpdf.conf";

    /**
     * Default constructor. Will parse default configuration file, system properties, override properties, etc. and
     * result in a usable Configuration instance.
     *
     * @throws RuntimeException if any stage of loading configuration results in an Exception. This could happen,
     * for example, if the default configuration file was not readable.
     */
    private Configuration() {
        try {
            // read logging level from System properties
            // here we are trying to see if user wants to see logging about
            // what configuration was loaded, e.g. debugging for config itself
            String val = getSystemProperty("show-config");
            if (val != null) {
                logLevel = LoggerUtil.parseLogLevel(val, Level.OFF);
            }
            this.properties = loadDefaultProperties();

            String sysOverrideFile = getSystemPropertyOverrideFileName();
            if (sysOverrideFile != null) {
                loadOverrideProperties(sysOverrideFile);
            } else {
                String userHomeOverrideFileName = getUserHomeOverrideFileName();
                if (userHomeOverrideFileName != null) {
                    loadOverrideProperties(userHomeOverrideFileName);
                }
            }
            loadSystemProperties();
            logAfterLoad();
        } catch (RuntimeException e) {
            handleUnexpectedExceptionOnInit(e);
            throw e;
        } catch (Exception e) {
            handleUnexpectedExceptionOnInit(e);
            throw new RuntimeException(e);
        }
    }

    private void handleUnexpectedExceptionOnInit(Exception e) {
        err.println("Could not initialize configuration for Flying Saucer library. Message is: " + e.getMessage());
        log.error(e.toString(), e);
    }

    /**
     * Sets the logger which we use for Configuration-related logging. Before this is
     * called the first time, all internal log records are queued up; they are flushed to
     * the logger when this method is first called. Afterwards, all log events are written
     * to this logger. This queueing behavior helps avoid order-of-operations bugs
     * related to loading configuration information related to logging.
     *
     * @param logger Logger used for Configuration-related messages
     */
    public static void setConfigLogger(Logger logger) {
        Configuration config = instance();
        config.configLogger = logger;
        for (LogRecord lr : config.startupLogRecords) {
            logger.log(lr.getLevel(), lr.getMessage());
        }
        config.startupLogRecords.clear();
    }

    /**
     * Used internally for logging status/info about the class.
     *
     * @param level the logging level to record the message at
     * @param msg the message to log
     */
    private void println(Level level, String msg) {
        if (!logLevel.equals(Level.OFF)) {
            if (configLogger == null) {
                startupLogRecords.add(new LogRecord(level, msg));
            } else {
                configLogger.log(level, msg);
            }
        }
    }

    /**
     * Used internally to log a message about the class at level INFO
     *
     * @param msg message to log
     */
    private void info(String msg) {
        if (logLevel.intValue() <= Level.INFO.intValue()) {
            println(Level.INFO, msg);
        }
    }

    /**
     * Used internally to log a message about the class at level WARNING
     *
     * @param msg message to log
     */
    private void warning(String msg) {
        if (logLevel.intValue() <= Level.WARNING.intValue()) {
            println(Level.WARNING, msg);
        }
    }

    /**
     * Used internally to log a message about the class at level WARNING, in case an exception was thrown
     *
     * @param msg message to log
     * @param th  the exception to report
     */
    private void warning(String msg, Throwable th) {
        warning(msg);
        log.error(msg, th);
    }

    /**
     * Used internally to log a message about the class at level FINE
     *
     * @param msg message to log
     */
    private void fine(String msg) {
        if (logLevel.intValue() <= Level.FINE.intValue()) {
            println(Level.FINE, msg);
        }
    }

    /**
     * Used internally to log a message about the class at level FINER
     *
     * @param msg message to log
     */
    private void finer(String msg) {
        if (logLevel.intValue() <= Level.FINER.intValue()) {
            println(Level.FINER, msg);
        }
    }


    /**
     * Loads the default set of properties, which may be overridden.
     */
    @CheckReturnValue
    private Properties loadDefaultProperties() {
        try (InputStream readStream = GeneralUtil.openStreamFromClasspath(new DefaultCSSMarker(), SF_FILE_NAME)) {
            if (readStream == null) {
                log.warn("No configuration files found in classpath using URL {}, resorting to hard-coded fallback properties.", SF_FILE_NAME);
                return newFallbackProperties();
            } else {
                Properties properties = new Properties();
                properties.load(readStream);
                info("Configuration loaded from " + SF_FILE_NAME);
                return properties;
            }
        } catch (IOException ex) {
            throw new RuntimeException("Could not load properties file for configuration.", ex);
        }
    }

    /**
     * Loads overriding property values from a second configuration file; this
     * is optional. See class documentation.
     *
     * @param uri Path to the file, or classpath URL, where properties are defined.
     */
    private void loadOverrideProperties(String uri) {
        File f = new File(uri);
        Properties temp = new Properties();
        if (f.exists()) {
            info("Found config override file " + f.getAbsolutePath());
            try {
                try (InputStream readStream = new BufferedInputStream(newInputStream(f.toPath()))) {
                    temp.load(readStream);
                }
            } catch (IOException iex) {
                warning("Error while loading override properties file; skipping.", iex);
                return;
            }
        } else {
            try {
                URL url = new URL(uri);
                try (InputStream in = new BufferedInputStream(url.openStream())) {
                    info("Found config override URI " + uri);
                    temp.load(in);
                }
            } catch (MalformedURLException e) {
                warning("URI for override properties is malformed, skipping: '%s' (caused by: %s)".formatted(uri, e));
                return;
            } catch (IOException e) {
                warning("Overridden properties could not be loaded from URI: " + uri, e);
                return;
            }
        }

        // override existing properties
        int cnt = 0;
        for (String key : sortedKeys(properties)) {
            String val = temp.getProperty(key);
            if (val != null) {
                this.properties.setProperty(key, val);
                finer("  " + key + " -> " + val);
                cnt++;
            }
        }
        finer("Configuration: " + cnt + " properties overridden from secondary properties file.");
        // and add any new properties we don't already know about (needed for custom logging
        // configuration)
        Collection<String> allRead = sortedKeys(temp);

        cnt = 0;
        for (String key : allRead) {
            String val = temp.getProperty(key);
            if (val != null) {
                this.properties.setProperty(key, val);
                finer("  (+)" + key + " -> " + val);
                cnt++;
            }
        }
        finer("Configuration: " + cnt + " properties added from secondary properties file.");
    }

    @Nullable
    private String getSystemPropertyOverrideFileName() {
        return getSystemProperty("xr.conf");
    }

    @Nullable
    private String getUserHomeOverrideFileName() {
        String confFileName = "local.openpdf.conf";
        try {
            return Path.of(System.getProperty("user.home"), ".flyingsaucer", confFileName).toString();
        } catch (SecurityException e) {
            // can happen in a sandbox
            log.warn("Cannot read file '{}': {}", confFileName, e.toString());
            return null;
        }
    }

    private static Collection<String> sortedKeys(Properties properties) {
        List<String> keys = new ArrayList<>(properties.size());
        properties.keySet().forEach(key -> keys.add((String) key));
        Collections.sort(keys);
        return keys;
    }

    /**
     * Loads overriding property values from a System properties; this is
     * optional. See class documentation.
     */
    private void loadSystemProperties() {
        fine("Overriding loaded configuration from System properties.");
        int cnt = 0;
        for (String key : sortedKeys(properties)) {
            if (!key.startsWith("xr.")) {
                continue;
            }


            String val = getSystemProperty(key);
            if (val != null) {
                properties.setProperty(key, val);
                finer("  Overrode value for " + key);
                cnt++;
            }
        }
        fine("Configuration: " + cnt + " properties overridden from System properties.");

        // add any additional properties we don't already know about (e.g. used for extended logging properties)
        try {
            final Properties sysProps = System.getProperties();

            cnt = 0;
            for (String key : sortedKeys(properties)) {
                if (key.startsWith("xr.") && !this.properties.containsKey(key)) {
                    String val = sysProps.getProperty(key);
                    this.properties.setProperty(key, val);
                    finer("  (+) " + key);
                    cnt++;
                }
            }
        } catch (SecurityException e) {
            // skip, this will happen in Web Start or sandbox
            log.warn("Cannot read system properties: {}", e.toString());
        }
        fine("Configuration: " + cnt + " FS properties added from System properties.");
    }

    /**
     * Writes a log of loaded properties to the "plumbing.init" Logger.
     */
    private void logAfterLoad() {
        finer("Configuration contains " + properties.size() + " keys.");
        finer("List of configuration properties, after override:");
        for (String key : sortedKeys(properties)) {
            String val = properties.getProperty(key);
            finer("  " + key + " = " + val);
        }
        finer("Properties list complete.");
    }

    /**
     * Returns the value for key in the Configuration. A warning is issued to
     * the log if the property is not defined.
     *
     * @param key Name of the property.
     * @return Value assigned to the key, as a String.
     */
    @Nullable
    public static String valueFor(String key) {
        Configuration conf = instance();
        String val = conf.properties.getProperty(key);
        if (val == null) {
            conf.warning("CONFIGURATION: no value found for key " + key);
        }
        return val;
    }

    /**
     * Returns the value for key in the Configuration as a byte, or the default
     * provided value if not found or if the value is not a valid byte. A
     * warning is issued to the log if the property is not defined, or if the
     * conversion from String fails.
     *
     * @param key        Name of the property.
     * @param defaultVal Default value to return
     * @return Value assigned to the key, as a String.
     */
    public static int valueAsByte(String key, byte defaultVal) {
        String val = valueFor(key);
        if (val == null) {
            return defaultVal;
        }

        try {
            return parseByte(val);
        } catch (NumberFormatException ignore) {
            XRLog.exception("Property '" + key + "' was requested as a byte, but " +
                    "value of '" + val + "' is not a byte. Check configuration.");
            return defaultVal;
        }
    }

    /**
     * Returns the value for key in the Configuration as a short, or the default
     * provided value if not found or if the value is not a valid short. A
     * warning is issued to the log if the property is not defined, or if the
     * conversion from String fails.
     *
     * @param key        Name of the property.
     * @param defaultVal Default value to return
     * @return Value assigned to the key, as a String.
     */
    public static int valueAsShort(String key, short defaultVal) {
        String val = valueFor(key);
        if (val == null) {
            return defaultVal;
        }

        try {
            return Short.parseShort(val);
        } catch (NumberFormatException ignore) {
            XRLog.exception("Property '" + key + "' was requested as a short, but " +
                    "value of '" + val + "' is not a short. Check configuration.");
            return defaultVal;
        }
    }

    /**
     * Returns the value for key in the Configuration as an integer, or a
     * default value if not found or if the value is not a valid integer. A
     * warning is issued to the log if the property is not defined, or if the
     * conversion from String fails.
     *
     * @param key        Name of the property.
     * @param defaultVal Default value to return
     * @return Value assigned to the key, as a String.
     */
    public static int valueAsInt(String key, int defaultVal) {
        String val = valueFor(key);
        if (val == null) {
            return defaultVal;
        }

        try {
            return parseInt(val);
        } catch (NumberFormatException ignore) {
            XRLog.exception("Property '" + key + "' was requested as an integer, but " +
                    "value of '" + val + "' is not an integer. Check configuration.");
            return defaultVal;
        }
    }

    /**
     * Returns the value for key in the Configuration as a character, or a
     * default value if not found. A warning is issued to the log if the
     * property is not defined, or if the configuration value is too long
     * to be a char. If the configuration value is longer than a single
     * character, only the first character is returned.
     *
     * @param key Name of the property
     * @param defaultVal Default value to return
     * @return Value assigned to the key, as a character
     */
    public static char valueAsChar(String key, char defaultVal) {
        String val = valueFor(key);
        if (val == null) {
            return defaultVal;
        }

        if(val.length() > 1) {
            XRLog.exception("Property '" + key + "' was requested as a character. The value of '" +
                    val + "' is too long to be a char. Returning only the first character.");
        }

        return val.charAt(0);
    }

    /**
     * Returns the value for key in the Configurations a long, or the default
     * provided value if not found or if the value is not a valid long. A
     * warning is issued to the log if the property is not defined, or if the
     * conversion from String fails.
     *
     * @param key        Name of the property.
     * @param defaultVal Default value to return
     * @return Value assigned to the key, as a String.
     */
    public static long valueAsLong(String key, long defaultVal) {
        String val = valueFor(key);
        if (val == null) {
            return defaultVal;
        }

        try {
            return parseLong(val);
        } catch (NumberFormatException ignore) {
            XRLog.exception("Property '" + key + "' was requested as a long, but " +
                    "value of '" + val + "' is not a long. Check configuration.");
            return defaultVal;
        }
    }

    /**
     * Returns the value for key in the Configuration as a float, or the default
     * provided value if not found or if the value is not a valid float. A
     * warning is issued to the log if the property is not defined, or if the
     * conversion from String fails.
     *
     * @param key        Name of the property.
     * @param defaultVal Default value to return
     * @return Value assigned to the key, as a String.
     */
    public static float valueAsFloat(String key, float defaultVal) {
        String val = valueFor(key);
        if (val == null) {
            return defaultVal;
        }

        try {
            return parseFloat(val);
        } catch (NumberFormatException ignore) {
            XRLog.exception("Property '" + key + "' was requested as a float, but " +
                    "value of '" + val + "' is not a float. Check configuration.");
            return defaultVal;
        }
    }

    /**
     * Returns the value for key in the Configuration as a double, or the
     * default provided value if not found or if the value is not a valid
     * double. A warning is issued to the log if the property is not defined, or
     * if the conversion from String fails.
     *
     * @param key        Name of the property.
     * @param defaultVal Default value to return
     * @return Value assigned to the key, as a String.
     */
    public static double valueAsDouble(String key, double defaultVal) {
        String val = valueFor(key);
        if (val == null) {
            return defaultVal;
        }

        try {
            return parseDouble(val);
        } catch (NumberFormatException ignore) {
            XRLog.exception("Property '" + key + "' was requested as a double, but " +
                    "value of '" + val + "' is not a double. Check configuration.");
            return defaultVal;
        }
    }

    /**
     * Returns the value for key in the Configuration, or the default provided
     * value if not found. A warning is issued to the log if the property is not
     * defined, and if the default is null.
     *
     * @param key        Name of the property.
     * @param defaultVal Default value to return
     * @return Value assigned to the key, as a String.
     */
    public static String valueFor(String key, String defaultVal) {
        Configuration conf = instance();
        String val = conf.properties.getProperty(key);
        val = (val == null ? defaultVal : val);

        //noinspection ConstantValue
        if (val == null) {
            conf.warning("CONFIGURATION: no value found for key " + key + " and no default given.");
        }
        return val;
    }

    /**
     * Returns all configuration keys that start with prefix. Iterator will be
     * empty if no such keys are found.
     *
     * @param prefix Prefix to filter on. No regex.
     */
    public static Iterator<String> keysByPrefix(String prefix) {
        Configuration conf = instance();
        return sortedKeys(conf.properties).stream()
                .filter(key -> key.startsWith(prefix))
                .iterator();
    }


    /**
     * Returns true if the value is "true" (ignores case), or the default
     * provided value if not found or if the value is not a valid boolean (true
     * or false, ignores case). A warning is issued to the log if the property
     * is not defined, and if the default is null.
     *
     * @param key        Name of the property.
     * @param defaultVal Default value to return
     * @return Value assigned to the key, as a String.
     */
    public static boolean isTrue(String key, boolean defaultVal) {
        String val = valueFor(key);
        if (val == null) {
            return defaultVal;
        }

        if (!"true|false".contains(val)) {
            XRLog.exception("Property '" + key + "' was requested as a boolean, but " +
                    "value of '" + val + "' is not a boolean. Check configuration.");
            return defaultVal;
        } else {
            return parseBoolean(val);
        }
    }

    /**
     * Returns true if the value is not "true" (ignores case), or the default
     * provided value if not found or if the value is not a valid boolean (true
     * or false, ignores case). A warning is issued to the log if the property
     * is not defined, or the value is not a valid boolean.
     *
     * @param key        Name of the property.
     * @param defaultVal Default value to return
     * @return Value assigned to the key, as a String.
     */
    public static boolean isFalse(String key, boolean defaultVal) {
        return !isTrue(key, defaultVal);
    }

    /**
     * @return The singleton instance of the class.
     */
    private static Configuration instance() {
        return Configuration.sInstance;
    }

    /**
     * Given a property, resolves the value to a public constant field on some class, where the field is of type Object.
     * The property value must the FQN of the class and field, e.g.
     * aKey=java.awt.RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR will return the value of the
     * VALUE_INTERPOLATION_NEAREST_NEIGHBOR constant on the RendingHints class.
     *
     * @param key Name of the property
     * @param defaultValue Returned in case of error.
     * @return Value of the constant, or defaultValue in case of error.
     */
    public static Object valueFromClassConstant(String key, Object defaultValue) {
        Configuration conf = instance();
        String val = valueFor(key);
        if ( val == null ) {
            return defaultValue;
        }
        int idx = val.lastIndexOf('.');
        final String className;
        final String constant;
        try {
            className = val.substring(0, idx);
            constant = val.substring(idx + 1);
        } catch (IndexOutOfBoundsException ignore) {
            conf.warning("Property key " + key + " for object value constant is not properly formatted; " +
                    "should be FQN<dot>constant, is " + val);
            return defaultValue;
        }
        Class<?> klass;
        try {
            klass = Class.forName(className);
        } catch (ClassNotFoundException e) {
            conf.warning("Property for object value constant " + key + " is not a FQN: " + className + ", caused by: " + e);
            return defaultValue;
        }

        final Object constantValue;
        try {
            Field fld = klass.getDeclaredField(constant);
            try {
                constantValue = fld.get(klass);
            } catch (IllegalAccessException e) {
                conf.warning("Property for object value constant " + key + ", field is not public: " + className +
                        "." + constant + ", caused by: " + e);
                return defaultValue;
            }
        } catch (NoSuchFieldException e) {
            conf.warning("Property for object value constant " + key + " is not a FQN: " + className + ", caused by: " + e);
            return defaultValue;
        }
        return constantValue;
    }

    /**
     * Returns a Properties instance filled with values of last resort--in case we can't read default properties
     * file for some reason; this is to prevent Configuration init from throwing any exceptions, or ending up
     * with a completely empty configuration instance.
     */
    @SuppressWarnings("SpellCheckingInspection")
    @CheckReturnValue
    private Properties newFallbackProperties() {
        Properties props = new Properties();
        props.setProperty("xr.css.user-agent-default-css", "/resources/css/");
        props.setProperty("xr.test.files.hamlet", "/demos/browser/xhtml/hamlet.xhtml");
        props.setProperty("xr.simple-log-format", "{1} {2}:: {5}");
        props.setProperty("xr.simple-log-format-throwable", "{1} {2}:: {5}");
        props.setProperty("xr.test-config-byte", "8");
        props.setProperty("xr.test-config-short", "16");
        props.setProperty("xr.test-config-int", "100");
        props.setProperty("xr.test-config-long", "2000");
        props.setProperty("xr.test-config-float", "3000.25F");
        props.setProperty("xr.test-config-double", "4000.50D");
        props.setProperty("xr.test-config-boolean", "true");
        props.setProperty("xr.util-logging.loggingEnabled", "false");
        props.setProperty("xr.util-logging.handlers", "java.util.logging.ConsoleHandler");
        props.setProperty("xr.util-logging.use-parent-handler", "false");
        props.setProperty("xr.util-logging.java.util.logging.ConsoleHandler.level", "INFO");
        props.setProperty("xr.util-logging.java.util.logging.ConsoleHandler.formatter", "org.openpdf.util.XRSimpleLogFormatter");
        props.setProperty("xr.util-logging.org.openpdf.level", "ALL");
        props.setProperty("xr.util-logging.org.openpdf.config.level", "ALL");
        props.setProperty("xr.util-logging.org.openpdf.exception.level", "ALL");
        props.setProperty("xr.util-logging.org.openpdf.general.level", "ALL");
        props.setProperty("xr.util-logging.org.openpdf.init.level", "ALL");
        props.setProperty("xr.util-logging.org.openpdf.load.level", "ALL");
        props.setProperty("xr.util-logging.org.openpdf.load.xml-entities.level", "ALL");
        props.setProperty("xr.util-logging.org.openpdf.match.level", "ALL");
        props.setProperty("xr.util-logging.org.openpdf.cascade.level", "ALL");
        props.setProperty("xr.util-logging.org.openpdf.css-parse.level", "ALL");
        props.setProperty("xr.util-logging.org.openpdf.layout.level", "ALL");
        props.setProperty("xr.util-logging.org.openpdf.render.level", "ALL");
        props.setProperty("xr.load.xml-reader", "default");
        props.setProperty("xr.load.configure-features", "false");
        props.setProperty("xr.load.validation", "false");
        props.setProperty("xr.load.string-interning", "false");
        props.setProperty("xr.load.namespaces", "false");
        props.setProperty("xr.load.namespace-prefixes", "false");
        props.setProperty("xr.layout.whitespace.experimental", "true");
        props.setProperty("xr.layout.bad-sizing-hack", "false");
        props.setProperty("xr.renderer.viewport-repaint", "true");
        props.setProperty("xr.renderer.draw.backgrounds", "true");
        props.setProperty("xr.renderer.draw.borders", "true");
        props.setProperty("xr.renderer.debug.box-outlines", "false");
        props.setProperty("xr.renderer.replace-missing-characters", "false");
        props.setProperty("xr.renderer.missing-character-replacement", "false");
        props.setProperty("xr.text.scale", "1.0");
        props.setProperty("xr.text.aa-smoothing-level", "1");
        props.setProperty("xr.text.aa-fontsize-threshhold", "25");
        props.setProperty("xr.text.aa-rendering-hint", "RenderingHints.VALUE_TEXT_ANTIALIAS_HGRB");
        props.setProperty("xr.cache.stylesheets", "false");
        props.setProperty("xr.incremental.enabled", "false");
        props.setProperty("xr.incremental.lazyimage", "false");
        props.setProperty("xr.incremental.debug.layoutdelay", "0");
        props.setProperty("xr.incremental.repaint.print-timing", "false");
        props.setProperty("xr.use.threads", "false");
        props.setProperty("xr.use.listeners", "true");
        props.setProperty("xr.image.buffered", "false");
        props.setProperty("xr.image.scale", "LOW");
        props.setProperty("xr.image.render-quality", "java.awt.RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR");
        return props;
    }

    @Nullable
    private String getSystemProperty(String name) {
        try {
            return System.getProperty(name);
        } catch (SecurityException e) {
            // can happen in sandbox
            log.warn("Cannot read system property '{}': {}", name, e.toString());
            return null;
        }
    }
}
