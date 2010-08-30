/*
 * $Id: Executable.java 4065 2009-09-16 23:09:11Z psoares33 $
 * $Name$
 *
 * Copyright 2005 by Bruno Lowagie / Roger Mistelli
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the License.
 *
 * The Original Code is 'iText, a free JAVA-PDF library'.
 *
 * The Initial Developer of the Original Code is Bruno Lowagie. Portions created by
 * the Initial Developer are Copyright (C) 1999, 2000, 2001, 2002 by Bruno Lowagie.
 * All Rights Reserved.
 * Co-Developer of the code is Paulo Soares. Portions created by the Co-Developer
 * are Copyright (C) 2000, 2001, 2002 by Paulo Soares. All Rights Reserved.
 *
 * Contributor(s): all the names of the contributors are added in the source code
 * where applicable.
 *
 * Alternatively, the contents of this file may be used under the terms of the
 * LGPL license (the "GNU LIBRARY GENERAL PUBLIC LICENSE"), in which case the
 * provisions of LGPL are applicable instead of those above.  If you wish to
 * allow use of your version of this file only under the terms of the LGPL
 * License and not to allow others to use your version of this file under
 * the MPL, indicate your decision by deleting the provisions above and
 * replace them with the notice and other provisions required by the LGPL.
 * If you do not delete the provisions above, a recipient may use your version
 * of this file under either the MPL or the GNU LIBRARY GENERAL PUBLIC LICENSE.
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the MPL as stated above or under the terms of the GNU
 * Library General Public License as published by the Free Software Foundation;
 * either version 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Library general Public License for more
 * details.
 *
 * If you didn't download this code from the following link, you should check if
 * you aren't using an obsolete version:
 * http://www.lowagie.com/iText/
 */
package com.lowagie.tools;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import com.lowagie.text.error_messages.MessageLocalization;

/**
 * This class enables you to call an executable that will show a PDF file.
 */
public class Executable {
	
	/**
	 * The path to Acrobat Reader.
	 */
	public static String acroread = null;

	
	/**
	 * Performs an action on a PDF document.
	 * @param fileName
	 * @param parameters
	 * @param waitForTermination
	 * @return a process
	 * @throws IOException
	 */
	private static Process action(final String fileName,
			String parameters, boolean waitForTermination) throws IOException {
		Process process = null;
		if (parameters.trim().length() > 0) {
			parameters = " " + parameters.trim();
		}
		else {
			parameters = "";
		}
		if (acroread != null) {
			process = Runtime.getRuntime().exec(
					acroread + parameters + " \"" + fileName + "\"");
		}
		else if (isWindows()) {
			if (isWindows9X()) {
				process = Runtime.getRuntime().exec(
						"command.com /C start acrord32" + parameters + " \"" + fileName + "\"");
			}
			else {
				process = Runtime.getRuntime().exec(
					"cmd /c start acrord32" + parameters + " \"" + fileName + "\"");
			}
		}
		else if (isMac()) {
			if (parameters.trim().length() == 0) {
				process = Runtime.getRuntime().exec(
					new String[] { "/usr/bin/open", fileName });
			}
			else {
				process = Runtime.getRuntime().exec(
						new String[] { "/usr/bin/open", parameters.trim(), fileName });
			}
		}
		try {
			if (process != null && waitForTermination)
				process.waitFor();
		} catch (InterruptedException ie) {
		}
		return process;
	}
	
	/**
	 * Opens a PDF document.
	 * @param fileName
	 * @param waitForTermination
	 * @return a process
	 * @throws IOException
	 */
	public static final Process openDocument(String fileName,
			boolean waitForTermination) throws IOException {
		return action(fileName, "", waitForTermination);
	}

	/**
	 * Opens a PDF document.
	 * @param file
	 * @param waitForTermination
	 * @return a process
	 * @throws IOException
	 */
	public static final Process openDocument(File file,
			boolean waitForTermination) throws IOException {
		return openDocument(file.getAbsolutePath(), waitForTermination);
	}

	/**
	 * Opens a PDF document.
	 * @param fileName
	 * @return a process
	 * @throws IOException
	 */
	public static final Process openDocument(String fileName) throws IOException {
		return openDocument(fileName, false);
	}

	/**
	 * Opens a PDF document.
	 * @param file
	 * @return a process
	 * @throws IOException
	 */
	public static final Process openDocument(File file) throws IOException {
		return openDocument(file, false);
	}
	
	/**
	 * Prints a PDF document.
	 * @param fileName
	 * @param waitForTermination
	 * @return a process
	 * @throws IOException
	 */
	public static final Process printDocument(String fileName,
			boolean waitForTermination) throws IOException {
		return action(fileName, "/p", waitForTermination);
	}

	/**
	 * Prints a PDF document.
	 * @param file
	 * @param waitForTermination
	 * @return a process
	 * @throws IOException
	 */
	public static final Process printDocument(File file,
			boolean waitForTermination) throws IOException {
		return printDocument(file.getAbsolutePath(), waitForTermination);
	}

	/**
	 * Prints a PDF document.
	 * @param fileName
	 * @return a process
	 * @throws IOException
	 */
	public static final Process printDocument(String fileName) throws IOException {
		return printDocument(fileName, false);
	}

	/**
	 * Prints a PDF document.
	 * @param file
	 * @return a process
	 * @throws IOException
	 */
	public static final Process printDocument(File file) throws IOException {
		return printDocument(file, false);
	}
	
	/**
	 * Prints a PDF document without opening a Dialog box.
	 * @param fileName
	 * @param waitForTermination
	 * @return a process
	 * @throws IOException
	 */
	public static final Process printDocumentSilent(String fileName,
			boolean waitForTermination) throws IOException {
		return action(fileName, "/p /h", waitForTermination);
	}

	/**
	 * Prints a PDF document without opening a Dialog box.
	 * @param file
	 * @param waitForTermination
	 * @return a process
	 * @throws IOException
	 */
	public static final Process printDocumentSilent(File file,
			boolean waitForTermination) throws IOException {
		return printDocumentSilent(file.getAbsolutePath(), waitForTermination);
	}

	/**
	 * Prints a PDF document without opening a Dialog box.
	 * @param fileName
	 * @return a process
	 * @throws IOException
	 */
	public static final Process printDocumentSilent(String fileName) throws IOException {
		return printDocumentSilent(fileName, false);
	}

	/**
	 * Prints a PDF document without opening a Dialog box.
	 * @param file
	 * @return a process
	 * @throws IOException
	 */
	public static final Process printDocumentSilent(File file) throws IOException {
		return printDocumentSilent(file, false);
	}
	
	/**
	 * Launches a browser opening an URL.
	 *
	 * @param url the URL you want to open in the browser
	 * @throws IOException
	 */
	public static final void launchBrowser(String url) throws IOException {
		try {
			if (isMac()) {
				Class macUtils = Class.forName("com.apple.mrj.MRJFileUtils");
				Method openURL = macUtils.getDeclaredMethod("openURL", new Class[] {String.class});
				openURL.invoke(null, new Object[] {url});
			}
			else if (isWindows())
				Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url);
			else { //assume Unix or Linux
	            String[] browsers = {
	               "firefox", "opera", "konqueror", "mozilla", "netscape" };
	            String browser = null;
	            for (int count = 0; count < browsers.length && browser == null; count++)
	               if (Runtime.getRuntime().exec(new String[] {"which", browsers[count]}).waitFor() == 0)
	                  browser = browsers[count];
	            if (browser == null)
	               throw new Exception(MessageLocalization.getComposedMessage("could.not.find.web.browser"));
	            else
	               Runtime.getRuntime().exec(new String[] {browser, url});
	            }
	         }
	      catch (Exception e) {
	         throw new IOException(MessageLocalization.getComposedMessage("error.attempting.to.launch.web.browser"));
	      }
	}

	/**
	 * Checks the Operating System.
	 * 
	 * @return true if the current os is Windows
	 */
	public static boolean isWindows() {
		String os = System.getProperty("os.name").toLowerCase();
		return os.indexOf("windows") != -1 || os.indexOf("nt") != -1;
	}

	/**
	 * Checks the Operating System.
	 * 
	 * @return true if the current os is Windows
	 */
	public static boolean isWindows9X() {
		String os = System.getProperty("os.name").toLowerCase();
		return os.equals("windows 95") || os.equals("windows 98");
	}

	/**
	 * Checks the Operating System.
	 * 
	 * @return true if the current os is Apple
	 */
	public static boolean isMac() {
		String os = System.getProperty("os.name").toLowerCase();
		return os.indexOf("mac") != -1;
	}

	/**
	 * Checks the Operating System.
	 * 
	 * @return true if the current os is Linux
	 */
	public static boolean isLinux() {
		String os = System.getProperty("os.name").toLowerCase();
		return os.indexOf("linux") != -1;
	}
}
