/*
 * {{{ header & license
 * LoggerUtil.java
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

import java.util.logging.Level;

/**
 * Utility class for working with java.logging Logger classes
 *
 * @author Patrick Wright
 */
public class LoggerUtil {
    
    static Level parseLogLevel(String val, Level defaultLogLevel) {
        return switch (val) {
            case "ALL" -> Level.ALL;
            case "CONFIG" -> Level.CONFIG;
            case "FINE" -> Level.FINE;
            case "FINER" -> Level.FINER;
            case "FINEST" -> Level.FINEST;
            case "INFO" -> Level.INFO;
            case "OFF" -> Level.OFF;
            case "SEVERE" -> Level.SEVERE;
            case "WARNING" -> Level.WARNING;
            default -> defaultLogLevel;
        };
    }
}
