/*
 * {{{ header & license
 * Copyright (c) 2004, 2005, 2008 Joshua Marinacci, Patrick Wright
 * Copyright (c) 2008 Patrick Wright
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

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import static java.util.Collections.unmodifiableList;
import static java.util.Objects.requireNonNull;


/**
 * Utility class for using the java.util.logging package. Relies on the standard
 * configuration for logging, but gives easier access to the various logs
 * (plumbing.load, .init, .render)
 *
 * @author empty
 */
public class XRLog {

    private static final List<String> LOGGER_NAMES = new ArrayList<>(20);
    public static final String CONFIG = registerLoggerByName("org.openpdf.config");
    public static final String EXCEPTION = registerLoggerByName("org.openpdf.exception");
    public static final String GENERAL = registerLoggerByName("org.openpdf.general");
    public static final String INIT = registerLoggerByName("org.openpdf.init");
    public static final String JUNIT = registerLoggerByName("org.openpdf.junit");
    public static final String LOAD = registerLoggerByName("org.openpdf.load");
    public static final String MATCH = registerLoggerByName("org.openpdf.match");
    public static final String CASCADE = registerLoggerByName("org.openpdf.cascade");
    public static final String XML_ENTITIES = registerLoggerByName("org.openpdf.load.xml-entities");
    public static final String CSS_PARSE = registerLoggerByName("org.openpdf.css-parse");
    public static final String LAYOUT = registerLoggerByName("org.openpdf.layout");
    public static final String RENDER = registerLoggerByName("org.openpdf.render");

    private static XRLogger loggerImpl = new JDKXRLogger();
    private static boolean loggingEnabled = Configuration.isTrue("xr.util-logging.loggingEnabled", true);

    private static String registerLoggerByName(final String loggerName) {
        LOGGER_NAMES.add(loggerName);
        return loggerName;
    }

    /**
     * Returns a list of all loggers that will be accessed by XRLog. Each entry is a String with a logger
     * name, which can be used to retrieve the logger using the corresponding Logging API; example name might be
     * "org.openpdf.config"
     *
     * @return List of loggers, never null.
     */
    @CheckReturnValue
    public static List<String> listRegisteredLoggers() {
        return unmodifiableList(LOGGER_NAMES);
    }


    public static void cssParse(String msg) {
        cssParse(Level.INFO, msg);
    }

    public static void cssParse(Level level, String msg) {
        log(CSS_PARSE, level, msg);
    }

    public static void cssParse(Level level, String msg, Throwable th) {
        log(CSS_PARSE, level, msg, th);
    }

    public static void xmlEntities(String msg) {
        xmlEntities(Level.INFO, msg);
    }

    public static void xmlEntities(Level level, String msg) {
        log(XML_ENTITIES, level, msg);
    }

    public static void xmlEntities(Level level, String msg, Throwable th) {
        log(XML_ENTITIES, level, msg, th);
    }

    public static void cascade(String msg) {
        cascade(Level.INFO, msg);
    }

    public static void cascade(Level level, String msg) {
        log(CASCADE, level, msg);
    }

    public static void cascade(Level level, String msg, Throwable th) {
        log(CASCADE, level, msg, th);
    }

    public static void exception(String msg) {
        exception(msg, null);
    }

    public static void exception(String msg, @Nullable Throwable th) {
        log(EXCEPTION, Level.WARNING, msg, th);
    }

    public static void general(String msg) {
        general(Level.INFO, msg);
    }

    public static void general(Level level, String msg) {
        log(GENERAL, level, msg);
    }

    public static void general(Level level, String msg, Throwable th) {
        log(GENERAL, level, msg, th);
    }

    public static void init(String msg) {
        init(Level.INFO, msg);
    }

    public static void init(Level level, String msg) {
        log(INIT, level, msg);
    }

    public static void init(Level level, String msg, Throwable th) {
        log(INIT, level, msg, th);
    }

    public static void junit(String msg) {
        junit(Level.FINEST, msg);
    }

    public static void junit(Level level, String msg) {
        log(JUNIT, level, msg);
    }

    public static void junit(Level level, String msg, Throwable th) {
        log(JUNIT, level, msg, th);
    }

    public static void load(String msg) {
        load(Level.INFO, msg);
    }

    public static void load(Level level, String msg) {
        log(LOAD, level, msg);
    }

    public static void load(Level level, String msg, Throwable th) {
        log(LOAD, level, msg, th);
    }

    public static void match(String msg) {
        match(Level.INFO, msg);
    }

    public static void match(Level level, String msg) {
        log(MATCH, level, msg);
    }

    public static void match(Level level, String msg, Throwable th) {
        log(MATCH, level, msg, th);
    }

    public static void layout(String msg) {
        layout(Level.INFO, msg);
    }

    public static void layout(Level level, String msg) {
        log(LAYOUT, level, msg);
    }

    public static void layout(Level level, String msg, Throwable th) {
        log(LAYOUT, level, msg, th);
    }

    public static void render(String msg) {
        render(Level.INFO, msg);
    }

    public static void render(Level level, String msg) {
        log(RENDER, level, msg);
    }

    public static void render(Level level, String msg, Throwable th) {
        log(RENDER, level, msg, th);
    }

    public static void log(String where, Level level, String msg) {
        if (isLoggingEnabled()) {
            loggerImpl.log(where, level, msg);
        }
    }

    public static void log(String where, Level level, String msg, @Nullable Throwable th) {
        if (isLoggingEnabled()) {
            loggerImpl.log(where, level, msg, th);
        }
    }

    public static void setLevel(String log, Level level) {
        loggerImpl.setLevel(log, level);
    }

    /**
     * Whether logging is on or off.
     *
     * @return Returns true if logging is enabled, false if not. Corresponds
     * to configuration file property xr.util-logging.loggingEnabled, or to
     * value passed to setLoggingEnabled(bool).
     */
    @CheckReturnValue
    public static boolean isLoggingEnabled() {
        return loggingEnabled;
    }

    /**
     * Turns logging on or off, without affecting logging configuration.
     *
     * @param loggingEnabled Flag whether logging is enabled or not;
     *                       if false, all logging calls fail silently. Corresponds
     *                       to configuration file property xr.util-logging.loggingEnabled
     */
    public static void setLoggingEnabled(boolean loggingEnabled) {
        XRLog.loggingEnabled = loggingEnabled;
    }

    @CheckReturnValue
    public static XRLogger getLoggerImpl() {
        return loggerImpl;
    }

    public static void setLoggerImpl(XRLogger loggerImpl) {
        XRLog.loggerImpl = requireNonNull(loggerImpl);
    }
}
