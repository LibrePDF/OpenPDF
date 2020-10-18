# OpenPDF is an open source Java library for PDF files #

OpenPDF is a Java library for creating and editing PDF files with a LGPL and MPL open source license. OpenPDF is the LGPL/MPL open source successor of iText, and is based on a fork, of a fork, of iText 4 svn tag. We welcome contributions from other developers. Please feel free to submit pull-requests and bugreports to this GitHub repository.

 [![Build Status](https://travis-ci.org/LibrePDF/OpenPDF.svg?branch=master)](https://travis-ci.org/LibrePDF/OpenPDF) 
 [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.librepdf/openpdf/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.librepdf/openpdf) 
 [![License (LGPL version 3.0)](https://img.shields.io/badge/license-GNU%20LGPL%20version%203.0-blue.svg?style=flat-square)](http://opensource.org/licenses/LGPL-3.0) 
 [![License (MPL)](https://img.shields.io/badge/license-Mozilla%20Public%20License-yellow.svg?style=flat-square)](http://opensource.org/licenses/MPL-2.0)
 
 [![Join the chat at https://gitter.im/LibrePDF/OpenPDF](https://badges.gitter.im/LibrePDF/OpenPDF.svg)](https://gitter.im/LibrePDF/OpenPDF) 
 [![Join the chat at https://gitter.im/LibrePDF/code_of_conduct](https://badges.gitter.im/LibrePDF/code_of_conduct.svg)](https://gitter.im/LibrePDF/code_of_conduct)

## OpenPDF version 1.3.22 released 2020-09-18 ##
Get version 1.3.22 here - https://github.com/LibrePDF/OpenPDF/releases/tag/1.3.22

- [Previous releases](https://github.com/LibrePDF/OpenPDF/releases)


## Use OpenPDF as Maven dependency
Add this to your pom.xml file to use the latest version of OpenPDF:

        <dependency>
            <groupId>com.github.librepdf</groupId>
            <artifactId>openpdf</artifactId>
            <version>1.3.22</version>
        </dependency>

## License ##

GNU General Lesser Public License (LGPL) version 2.1 - https://www.gnu.org/licenses/old-licenses/lgpl-2.1.html

Mozilla Public License Version 2.0 - http://www.mozilla.org/MPL/2.0/

We want OpenPDF to consist of source code which is consistently licensed with the LGPL and MPL licences only. This also means that any new contributions to the project must have a dual LGPL and MPL license only.

## Documentation ##
- [Examples](pdf-toolbox/src/test/java/com/lowagie/examples/)
- [JavaDoc](https://librepdf.github.io/OpenPDF/docs-1-3-17/)
- [Tutorial](https://github.com/LibrePDF/OpenPDF/wiki/Tutorial) (wiki, work in progress)

## Background ##

OpenPDF is open source software with a LGPL and MPL license. It is a fork of iText version 4, more specifically iText svn tag 4.2.0, which was hosted publicly on sourceforge with LGPL and MPL license headers in the source code, and lgpl and mpl license documents in the svn repository.
Beginning with version 5.0 of iText, the developers have moved to the AGPL to improve their ability to sell commercial licenses.

## Projects using OpenPDF ##
- Spring Framework https://github.com/spring-projects/spring-framework
- flyingsaucer https://github.com/flyingsaucerproject/flyingsaucer
- Confluence PDF Export
- Digital Signature Service - https://github.com/esig/dss
- OpenCMS, Nuxeo Web Framework, QR Invoice Library and many closed source commercial applications as well.
- Full list here: https://mvnrepository.com/artifact/com.github.librepdf/openpdf/usages

## Android support ##
OpenPDF now has Android support, more info here: [Android-support](https://github.com/LibrePDF/OpenPDF/wiki/Android-support)

## Contributing ##
Release the hounds!  Please send all pull requests.
Make sure that your contributions can be released with a dual LGPL and MPL license. In particular, pull requests to the OpenPDF project must only contain code that you have written yourself. GPL or AGPL licensed code will not be acceptable.

### Coding Style ###
- Code indentation style is 4 spaces.
- Generally try to preserve the coding style in the file you are modifying.

## Dependencies ##
### Required Dependencies: ###
 - Java 8 or later is required to use OpenPDF. All versions Java 8 to Java OpenJDK 13 have been tested to work.

### UTF-8 Fonts: ###

As of 1.3.21 the UTF-8 Liberation fonts moved to its own module, to reduce the size of the OpenPDF
jar. If you want to use the bundled UTF-8 fonts, please add the following dependency to your project
and use the class `org.librepdf.openpdf.fonts.Liberation`.

        <dependency>
            <groupId>com.github.librepdf</groupId>
            <artifactId>openpdf-fonts-extra</artifactId>
            <version>${openpdf.version}</version>
        </dependency>

### Optional: ###

  - [BouncyCastle](https://www.bouncycastle.org/) (BouncyCastle is used to sign PDF files, so it's a recommended dependency)
    - Provider
    - PKIX/CMS
 - [TwelveMonkeys imageio-tiff](https://github.com/haraldk/TwelveMonkeys/) - optional by default, but required if TIFF image support is needed.
 - JUnit 5 - for unit testing
 - JFreeChart - for testing graphical examples
   - JFreeChart
   - JCommon
   - Servlet
 - DOM4j is required for the pdf-swing submodule.
 - Apache FOP

## Credits ##
Significant [Contributors to OpenPDF](https://github.com/LibrePDF/OpenPDF/graphs/contributors) on GitHub:

* [@andreasrosdal](https://github.com/andreasrosdal) - Andreas Røsdal - Maintainer of OpenPDF from 1.0 to 1.3.15, now retired from OpenPDF development.
* [@daviddurand](https://github.com/daviddurand) - David G. Durand
* [@tlxtellef](https://github.com/tlxtellef) - Tellef
* [@asturio](https://github.com/asturio) - Claudio Clemens
* [@ymasory](https://github.com/ymasory)
* [@albfernandez](https://github.com/albfernandez) - Alberto Fernández
* [@noavarice](https://github.com/noavarice)
* [@bengolder](https://github.com/bengolder) - Benjamin Golder
* [@glarfs](https://github.com/glarfs)
* [@Kindrat](https://github.com/Kindrat)
* [@syakovyn](https://github.com/syakovyn)
* [@ubermichael](https://github.com/ubermichael) - Michael Joyce
* [@weiyeh](https://github.com/weiyeh)
* [@SuperPat45](https://github.com/SuperPat45)
* [@lapo-luchini](https://github.com/lapo-luchini) - Lapo Luchini
* [@MartinKocour](https://github.com/MartinKocour) - Martin Kocour
* [@jokimaki](https://github.com/jokimaki)
* [@sullis](https://github.com/sullis)
* [@jeffrey-easyesi](https://github.com/jeffrey-easyesi)
* [@V-F](https://github.com/V-F)
* [@sixdouglas](https://github.com/sixdouglas) - Douglas Six
* [@razilein](https://github.com/razilein) - Sita Geßner
* [@PalAditya](https://github.com/PalAditya) - Aditya Pal
* [@rammetzger](https://github.com/rammetzger)
* [@codecracker2014](https://github.com/codecracker2014)
* [@mluppi](https://github.com/mluppi) - M. Luppi
* [@vic0075](https://github.com/vic0075)
* [@armin-weber](https://github.com/armin-weber)
* [@dandybudach](https://github.com/dandybudach) - Dandy Budach
* [@salsolatragus](https://github.com/salsolatragus) - Sven Amann
* [@suiaing](https://github.com/suiaing)
* [@prashantbhat](https://github.com/prashantbhat) - Prashant Bhat


Also, a very special thanks to the iText developers ;)
