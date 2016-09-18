# OpenPDF is a Java PDF library, forked from the venerable iText #

OpenPDF is a Java library for creating and editing PDF files with a MIT license.

## OpenPDF version 1.0 released 2016-05-13##
Get version 1.0 here - https://github.com/rtfarte/OpenPDF/releases/tag/1.0

## License ##

MIT License - http://www.opensource.org/licenses/mit-license.php

## Use OpenPDF as Maven dependency
Add this to your pom.xml file:

        <dependency>
            <groupId>com.github.bengolder</groupId>
            <artifactId>openpdf</artifactId>
            <version>1.0</version>
        </dependency>
        <dependency>
            <groupId>org.bouncycastle</groupId>
            <artifactId>bcprov-jdk15on</artifactId>
            <version>1.54</version>
        </dependency>


## Background ##

Beginning with version 5.0 of iText, the developers have moved to the AGPL to improve their ability to sell commercial licenses.
I fully respect the developers' wishes and rights.  However, I also respect open-source software.  The plan is to
keep things free and open.  One of the other intents is to boil this code down to the essentials.  Anything outside
of creating PDF documents will be moved to an extension library.  This library is for core PDF needs and concerns.

## Changes ##
This repo has the following changes from the old "original" 4.2.0 version:
 - compilation now also supports Java 8, but compatibility level in maven pom is set to Java 7
 - in the case of unexpected end of PDF file the IOException is thrown (not PDFNull)
 - merged patch from Steven to fix NPE in XFA Form (escapeSom method)
 - merged UnembedFontPdfSmartCopy functionality from Vicente Alencar
 - merged RTF Footer functionality from ubermichael
 - compatibility fix to support the newest (1.54) bouncy castle libraries based/inspired by flex-developments fixes
 - some NPE fixes and tweaked maven support

## Contributing ##
Release the hounds!  Please send all pull requests.

## Dependencies ##

### Required: ###

BouncyCastle:
  Provider
  PKIX/CMS

PDFRenderer

DOM4j

### Optional: ###
JUnit 4 - for unit testing

JFreeChart: - for testing graphical examples
  JFreeChart
  JCommon
  Servlet


  
