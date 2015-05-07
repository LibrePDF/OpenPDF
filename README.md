# OpenPDF is a Java PDF library, forked from the venerable iText #

## Background ##

Beginning with version 5.0 the developers have moved to the AGPL to improve their ability to sell commercial licenses.
I fully respect the developers' wishes and rights.  However, I also respect opensource software.  The plan is to pick up
keep things free and open.  One of the other intents is to boil this code down to the essentials.  Anything outside
of creating PDF documents will be moved to an extension library.  This library is for core PDF needs and concerns.

## Changes ##
This repo has the following changes from the old "original" 4.2.0 version:
 - compilation now supports also java 8, but compatibility level is in maven pom set to java 7
 - in the case of unexpected end of PDF file the IOException is thrown (not PDFNull)
 - merged patch from Steven to fix NPE in XFA Form (escapeSom method)
 - merged UnembedFontPdfSmartCopy functionality from Vicente Alencar
 - merged RTF Footer functionality from ubermichael
 - compatibility fix to support the newest (1.52) bouncy castle libraries based/inspired by flex-developments fixes
 - some NPE fixes and tweaked maven support

## Contributing ##
Release the hounds!  Please send all pull requests.

