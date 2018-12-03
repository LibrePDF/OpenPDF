# OpenPDF is an open source Java library for PDF files #

OpenPDF is a Java library for creating and editing PDF files with a LGPL and MPL open source license. OpenPDF is the LGPL/MPL open source successor of iText, and is based on a fork, of a fork, of iText 4 svn tag. We welcome contributions from other developers. Please feel free to submit pull-requests and bugreports to this GitHub repository.

[![Join the chat at https://gitter.im/LibrePDF/OpenPDF](https://badges.gitter.im/LibrePDF/OpenPDF.svg)](https://gitter.im/LibrePDF/OpenPDF) [![Build Status](https://travis-ci.org/LibrePDF/OpenPDF.svg?branch=master)](https://travis-ci.org/LibrePDF/OpenPDF) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.librepdf/openpdf/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.librepdf/openpdf) [![License (LGPL version 3.0)](https://img.shields.io/badge/license-GNU%20LGPL%20version%203.0-blue.svg?style=flat-square)](http://opensource.org/licenses/LGPL-3.0) [![License (MPL)](https://img.shields.io/badge/license-Mozilla%20Public%20License-yellow.svg?style=flat-square)](http://opensource.org/licenses/MPL-2.0)

## OpenPDF version 1.2.7 released 2018-12-03 ##
Get version 1.2.7 here - https://github.com/LibrePDF/OpenPDF/releases/tag/1.2.7

- [Previous releases](https://github.com/LibrePDF/OpenPDF/releases)


## Use OpenPDF as Maven dependency
Add this to your pom.xml file to use the latest version of OpenPDF:

        <dependency>
            <groupId>com.github.librepdf</groupId>
            <artifactId>openpdf</artifactId>
            <version>1.2.7</version>
        </dependency>

There is a separate Java 7 compatible branch and release.

## License ##

GNU General Lesser Public License (LGPL) version 3.0 - http://www.gnu.org/licenses/lgpl.html

Mozilla Public License Version 2.0 - http://www.mozilla.org/MPL/2.0/

We want OpenPDF to consist of source code which is consistently licensed with the LGPL and MPL licences only. This also means that any new contributions to the project must have a dual LGPL and MPL license only.

## Docs ##
See [examples](pdf-toolbox/src/test/java/com/lowagie/examples/) and [JavaDoc](https://librepdf.github.io/OpenPDF/docs-1-2-7/?).

## Background ##

OpenPDF is open source software with a LGPL and MPL license. It is a fork of iText version 4, more specifically iText svn tag 4.2.0, which was hosted publicly on sourceforge with LGPL and MPL license headers in the source code, and lgpl and mpl license documents in the svn repository.
Beginning with version 5.0 of iText, the developers have moved to the AGPL to improve their ability to sell commercial licenses. 

## Contributing ##
Release the hounds!  Please send all pull requests.
Make sure that your contributions can be released with a dual LGPL and MPL license. In particular, pull requests to the OpenPDF project must only contain code that you have written yourself. GPL or AGPL licensed code will not be acceptable.

## Dependencies ##
- Java 8 or later is required to use OpenPDF. There is a separate branch for Java 7 called 'java7'.

### Required Dependencies: ###

 - [Juniversalchardet](https://github.com/albfernandez/juniversalchardet)

### Optional: ###

  - [BouncyCastle](https://www.bouncycastle.org/) 1.60
    - Provider
    - PKIX/CMS
 - [TwelveMonkeys imageio-tiff](https://github.com/haraldk/TwelveMonkeys/) - optional by default, but required if TIFF image support is needed.    
 - JUnit 5 - for unit testing
 - JFreeChart - for testing graphical examples
   - JFreeChart
   - JCommon
   - Servlet
 - DOM4j is required for the pdf-swing submodule.
