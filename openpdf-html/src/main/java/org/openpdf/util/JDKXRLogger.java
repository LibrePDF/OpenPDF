/*
 * {{{ header & license
 * Copyright (c) 2004, 2005 Joshua Marinacci
 * Copyright (c) 2007 Wisconsin Court System
 * Copyright (c) 2008 Patrick Wright
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
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

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.lang.Boolean.parseBoolean;

/**
 * An {@link XRLogger} interface that uses {@code java.util.logging}.
 */
public class JDKXRLogger implements XRLogger {

    @Override
    public void log(String where, Level level, String msg) {
        getLogger(where).log(level, msg);
    }

    @Override
    public void log(String where, Level level, String msg, @Nullable Throwable th) {
        getLogger(where).log(level, msg, th);
    }

    @Override
    public void setLevel(String logger, Level level) {
        getLogger(logger).setLevel(level);
    }

    /**
     * Same purpose as Logger.getLogger(), except that the static initialization
     * for XRLog will initialize the LogManager with logging levels and other
     * configuration. Use this instead of Logger.getLogger()
     */
    @CheckReturnValue
    private static Logger getLogger(String log) {
        return Logger.getLogger(log);
    }

    public JDKXRLogger() {
        Properties props = retrieveLoggingProperties();

        if (!XRLog.isLoggingEnabled()) {
            Configuration.setConfigLogger(Logger.getLogger(XRLog.CONFIG));
            return;
        }
        initializeJDKLogManager(props);

        Configuration.setConfigLogger(Logger.getLogger(XRLog.CONFIG));
    }

    @CheckReturnValue
    private static Properties retrieveLoggingProperties() {
        // pull logging properties from configuration
        // they are all prefixed as shown
        String prefix = "xr.util-logging.";
        Properties props = new Properties();
        Iterator<String> iter = Configuration.keysByPrefix(prefix);
        while (iter.hasNext()) {
            String fullKey = iter.next();
            String key = fullKey.substring(prefix.length());
            String value = Configuration.valueFor(fullKey);
            props.setProperty(key, value);
        }
        return props;
    }

    private static void initializeJDKLogManager(final Properties fsLoggingProperties) {
        final List<Logger> loggers = retrieveLoggers();

        configureLoggerHandlerForwarding(fsLoggingProperties, loggers);

        // load our properties into our log manager
        Map<String, Handler> handlers = new HashMap<>();
        Map<String, String> handlerFormatterMap = new HashMap<>();

        for (Object k : fsLoggingProperties.keySet()) {
            String key = (String) k;
            String prop = fsLoggingProperties.getProperty(key);
            if (key.endsWith("level")) {
                configureLogLevel(key.substring(0, key.lastIndexOf('.')), prop);
            } else if (key.endsWith("handlers")) {
                handlers = configureLogHandlers(loggers, prop);
            } else if (key.endsWith("formatter")) {
                String k2 = key.substring(0, key.length() - ".formatter".length());
                handlerFormatterMap.put(k2, prop);
            }
        }

        // formatters apply to a specific handler we have initialized previously,
        // hence we need to wait until we've parsed the handler class
        for (Map.Entry<String, String> entry : handlerFormatterMap.entrySet()) {
            String formatterClassName = entry.getValue();
            assignFormatter(handlers, entry.getKey(), formatterClassName);
        }
    }

    private static void configureLoggerHandlerForwarding(Properties fsLoggingProperties, List<Logger> loggers) {
        String val = fsLoggingProperties.getProperty("use-parent-handler");

        boolean flag = parseBoolean(val);
        for (Logger logger : loggers) {
            logger.setUseParentHandlers(flag);
        }
    }

    private static void assignFormatter(Map<String, Handler> handlers, String handlerClassName, String formatterClassName) {
        Handler handler = handlers.get(handlerClassName);

        if (handler != null) {
            try {
                @SuppressWarnings("unchecked")
                Class<Formatter> fclass = (Class<Formatter>) Class.forName(formatterClassName);
                Formatter formatter = fclass.getConstructor().newInstance();
                handler.setFormatter(formatter);
            } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException e) {
                throw new XRRuntimeException("Could not initialize logging properties; " +
                        "Can't instantiate Formatter class " + formatterClassName + ": " + e.getClass().getSimpleName(), e);
            }
        }
    }

    /**
     * Returns a List of all Logger instances used by Flying Saucer from the JDK LogManager; these will
     * be automatically created if they aren't already available.
     */
    @CheckReturnValue
    private static List<Logger> retrieveLoggers() {
        List<String> loggerNames = XRLog.listRegisteredLoggers();
        List<Logger> loggers = new ArrayList<>(loggerNames.size());

        for (String loggerName : loggerNames) {
            loggers.add(Logger.getLogger(loggerName));
        }

        return loggers;
    }

    /**
     * For each logger provided, assigns the logger an instance of the named log output handlers. Will attempt
     * to instantiate each handler; any which can't be instantiated will cause the method to throw a RuntimeException.
     *
     * @param loggers          List of Logger instances.
     * @param handlerClassList A space-separated string (following the configuration convention for JDK logging
     *                         configuration files, for handlers) of FQN of log handlers.
     * @return Map of handler class names to handler instances.
     */
    @CheckReturnValue
    private static Map<String, Handler> configureLogHandlers(List<Logger> loggers, final String handlerClassList) {
        final String[] names = handlerClassList.split(" ");
        final Map<String, Handler> handlers = new HashMap<>(names.length);

        for (final String name : names) {
            try {
                @SuppressWarnings("unchecked")
                Class<Handler> handlerClass = (Class<Handler>) Class.forName(name);
                Handler handler = handlerClass.getConstructor().newInstance();
                handlers.put(name, handler);
                String hl = Configuration.valueFor("xr.util-logging." + name + ".level", "INFO");
                handler.setLevel(LoggerUtil.parseLogLevel(hl, Level.INFO));
            } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException e) {
                throw new XRRuntimeException("Could not initialize logging properties; " +
                        "Can't instantiate Handler class " + name + ": " + e.getClass().getSimpleName(), e);
            }
        }

        // now assign each handler to each FS logger
        for (Logger logger : loggers) {
            for (Handler handler : handlers.values()) {
                logger.addHandler(handler);
            }
        }
        return handlers;
    }

    /**
     * Parses the levelValue into a Level instance and assigns to the Logger instance named by loggerName; if
     * the levelValue is invalid (e.g. misspelled), assigns Level.OFF to the logger.
     */
    private static void configureLogLevel(String loggerName, String levelValue) {
        final Level level = LoggerUtil.parseLogLevel(levelValue, Level.OFF);
        final Logger logger = Logger.getLogger(loggerName);
        logger.setLevel(level);
    }
}
