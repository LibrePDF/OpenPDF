# OpenPDF is a Java PDF library, forked from iText #

OpenPDF is a Java library for creating and editing PDF files with a LGPL and MPL open source license. OpenPDF is based on a fork of iText 4. We welcome contributions from other developers. Please feel free to submit pull-requests and bugreports to this GitHub repository.

[![Join the chat at https://gitter.im/LibrePDF/OpenPDF](https://badges.gitter.im/LibrePDF/OpenPDF.svg)](https://gitter.im/LibrePDF/OpenPDF) [![Build Status](https://api.travis-ci.org/LibrePDF/OpenPDF.png)](https://travis-ci.org/LibrePDF/OpenPDF) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.librepdf/openpdf/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.librepdf/openpdf) [![License (LGPL version 3.0)](https://img.shields.io/badge/license-GNU%20LGPL%20version%203.0-blue.svg?style=flat-square)](http://opensource.org/licenses/LGPL-3.0) [![License (MPL)](https://img.shields.io/badge/license-Mozilla%20Public%20License-yellow.svg?style=flat-square)](http://opensource.org/licenses/MPL-2.0)

## OpenPDF version 1.0.4 released 2017-10-11 ##
Get version 1.0.4 here - https://github.com/LibrePDF/OpenPDF/releases/tag/1.0.4

### Previous Versions
- [Version 1.0.3](https://github.com/LibrePDF/OpenPDF/releases/tag/1.0.3) _released 2017-07-24_
- [Version 1.0.2](https://github.com/LibrePDF/OpenPDF/releases/tag/1.0.2) _released 2017-06-03_
- [Version 1.0.1](https://github.com/LibrePDF/OpenPDF/releases/tag/1.0.1) _released 2017-01-28_
- [Version 1.0](https://github.com/LibrePDF/OpenPDF/releases/tag/1.0) _released 2016-05-03_

## License ##

GNU General Lesser Public License (LGPL) version 3.0 - http://www.gnu.org/licenses/lgpl.html

Mozilla Public License Version 2.0 - http://www.mozilla.org/MPL/2.0/


## Use OpenPDF as Maven dependency
Add this to your pom.xml file:

        <dependency>
            <groupId>com.github.librepdf</groupId>
            <artifactId>openpdf</artifactId>
            <version>1.0.4</version>
        </dependency>


## Background ##

Beginning with version 5.0 of iText, the developers have moved to the AGPL to improve their ability to sell commercial licenses. 
The OpenPDF project is a fork of iText 4, with a LGPL and MPL open source license.

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

 - BouncyCastle 1.57
   - Provider
   - PKIX/CMS
 - PDFRenderer
 - DOM4j

### Optional: ###

 - JUnit 4 - for unit testing
 - JFreeChart - for testing graphical examples
   - JFreeChart
   - JCommon
   - Servlet
