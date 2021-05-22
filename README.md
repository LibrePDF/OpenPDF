# OpenPDF is an open source Java library for PDF files #

OpenPDF is a Java library for creating and editing PDF files with a LGPL and MPL open source
license. OpenPDF is the LGPL/MPL open source successor of iText, and is based on some forks of iText
4 svn tag. We welcome contributions from other developers. Please feel free to submit pull-requests
and bugreports to this GitHub repository.

![CI](https://github.com/LibrePDF/OpenPDF/workflows/OpenPDF%20CI/badge.svg)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.librepdf/openpdf/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.librepdf/openpdf)
[![License (LGPL version 3.0)](https://img.shields.io/badge/license-GNU%20LGPL%20version%202.1-blue.svg?style=flat-square)](http://opensource.org/licenses/LGPL-2.1)
[![License (MPL)](https://img.shields.io/badge/license-Mozilla%20Public%20License-yellow.svg?style=flat-square)](http://opensource.org/licenses/MPL-2.0)

[![Join the chat at https://gitter.im/LibrePDF/OpenPDF](https://badges.gitter.im/LibrePDF/OpenPDF.svg)](https://gitter.im/LibrePDF/OpenPDF)
[![Join the chat at https://gitter.im/LibrePDF/code_of_conduct](https://badges.gitter.im/LibrePDF/code_of_conduct.svg)](https://gitter.im/LibrePDF/code_of_conduct)

## OpenPDF version 1.3.26 released 2021-05-02 ##

Get version 1.3.26 here - https://github.com/LibrePDF/OpenPDF/releases/tag/1.3.26

- [Previous releases](https://github.com/LibrePDF/OpenPDF/releases)

## Use OpenPDF as Maven dependency

Add this to your pom.xml file to use the latest version of OpenPDF:

```xml

<dependency>
  <groupId>com.github.librepdf</groupId>
  <artifactId>openpdf</artifactId>
  <version>1.3.26</version>
</dependency>
```

## License ##

[GNU Lesser General Public License (LGPL), Version 2.1](https://www.gnu.org/licenses/old-licenses/lgpl-2.1)

[Mozilla Public License Version 2.0](http://www.mozilla.org/MPL/2.0/)

We want OpenPDF to consist of source code which is consistently licensed with the LGPL and MPL
licences only. This also means that any new contributions to the project must have a dual LGPL and
MPL license only.

## Documentation ##

- [Examples](pdf-toolbox/src/test/java/com/lowagie/examples)
- [JavaDoc](https://librepdf.github.io/OpenPDF/docs-1-3-17/)
- [Tutorial](https://github.com/LibrePDF/OpenPDF/wiki/Tutorial) (wiki, work in progress)
- [Migration from iText, TIFF support](https://github.com/LibrePDF/OpenPDF/wiki/Migrating-from-iText-2-and-4)

## Background ##

OpenPDF is open source software with a LGPL and MPL license. It is a fork of iText version 4, more
specifically iText svn tag 4.2.0, which was hosted publicly on sourceforge with LGPL and MPL license
headers in the source code, and LGPL and MPL license documents in the svn repository. Beginning with
version 5.0 of iText, the developers have moved to the AGPL to improve their ability to sell
commercial licenses.

OpenPDF ancestors in GitHub (in fork order):

1. [@rtfarte](https://github.com/rtfarte) / [OpenPDF](https://github.com/rtfarte/OpenPDF) - parent
   of LibrePDF/OpenPDF
2. [@kulatamicuda](https://github.com/kulatamicuda)
   / [iText-4.2.0](https://github.com/kulatamicuda/iText-4.2.0)
3. [@daviddurand](https://github.com/daviddurand)
   / [iText-4.2.0](https://github.com/daviddurand/iText-4.2.0)
4. [@ymasory](https://github.com/ymasory) / [iText-4.2.0](https://github.com/ymasory/iText-4.2.0) -
   original parent on GitHub

## Projects using OpenPDF ##

- Spring Framework https://github.com/spring-projects/spring-framework
- flyingsaucer https://github.com/flyingsaucerproject/flyingsaucer
- Confluence PDF Export
- Digital Signature Service - https://github.com/esig/dss
- OpenCMS, Nuxeo Web Framework, QR Invoice Library and many closed source commercial applications as
  well.
- Full list here: https://mvnrepository.com/artifact/com.github.librepdf/openpdf/usages

## Android ##

OpenPDF can be used with Android, more info
here: [Android-support](https://github.com/LibrePDF/OpenPDF/wiki/Android-support)

## Contributing ##

Release the hounds!  Please send all pull requests. Make sure that your contributions can be
released with a dual LGPL and MPL license. In particular, pull requests to the OpenPDF project must
only contain code that you have written yourself. GPL or AGPL licensed code will not be acceptable.

### Coding Style ###

- Code indentation style is 4 spaces.
- Generally try to preserve the coding style in the file you are modifying.

## Dependencies ##

### Required Dependencies: ###

- Java 8 or later is required to use OpenPDF. All versions Java 8 to Java OpenJDK 15 have been
  tested to work.

### UTF-8 Fonts: ###

As of 1.3.21 the UTF-8 Liberation fonts moved to its own module, to reduce the size of the OpenPDF
jar. If you want to use the bundled UTF-8 fonts, please add the following dependency to your project
and use the class `org.librepdf.openpdf.fonts.Liberation`.

```xml

<dependency>
  <groupId>com.github.librepdf</groupId>
  <artifactId>openpdf-fonts-extra</artifactId>
  <version>${openpdf.version}</version>
</dependency>
```

### Optional: ###

- [BouncyCastle](https://www.bouncycastle.org/) (BouncyCastle is used to sign PDF files, so it's a
  recommended dependency)
    - Provider (`org.bouncycastle:bcprov-jdk15on` or `org.bouncycastle:bcprov-ext-jdk15on` depending
      on which algorithm you are using)
    - PKIX/CMS (`org.bouncycastle:bcpkix-jdk15on`)
- Apache FOP (`org.apache.xmlgraphics:fop`)
- Please refer to our [pom.xml](pom.xml) to see what version is needed.

## Credits ##
Significant [Contributors to OpenPDF](https://github.com/LibrePDF/OpenPDF/graphs/contributors) on GitHub.

In GitHub User alphabetical order.

### Maintainers and Members ###
* [@asturio](https://github.com/asturio) - Claudio Clemens
* [@bengolder](https://github.com/bengolder) - Benjamin Golder
* [@daviddurand](https://github.com/daviddurand) - David G. Durand
* [@kulatamicuda](https://github.com/kulatamicuda)
* [@rtfarte](https://github.com/rtfarte) - Art Krahenbuhl
* [@tlxtellef](https://github.com/tlxtellef) - Tellef

### Contributors ###
* [@abdullahcevik](https://github.com/abdullahcevik) - Abdullah Çevik
* [@albfernandez](https://github.com/albfernandez) - Alberto Fernández
* [@andreasrosdal](https://github.com/andreasrosdal) - Andreas Røsdal - Maintainer of OpenPDF from 1.0 to 1.3.15, now retired from OpenPDF development.
* [@applikationsprogramvara](https://github.com/applikationsprogramvara)
* [@armin-weber](https://github.com/armin-weber)
* [@bsmelo](https://github.com/bsmelo) - Bruno Melo
* [@chrismaster](https://github.com/chrismaster) - Chris Master
* [@codecracker2014](https://github.com/codecracker2014)
* [@dandybudach](https://github.com/dandybudach) - Dandy Budach
* [@glarfs](https://github.com/glarfs)
* [@harrati](https://github.com/harrati)
* [@jeffrey-easyesi](https://github.com/jeffrey-easyesi)
* [@jherkel](https://github.com/jherkel)
* [@jokimaki](https://github.com/jokimaki)
* [karolbe](https://github.com/karolbe) - Karol Bryd
* [@Kindrat](https://github.com/Kindrat)
* [@lapo-luchini](https://github.com/lapo-luchini) - Lapo Luchini
* [@laurerunser](https://github.com/laurerunser) - Laure Runser
* [@macromogic](https://github.com/macromogic)
* [@mahdilamb](https://github.com/mahdilamb)
* [@MartinKocour](https://github.com/MartinKocour) - Martin Kocour
* [@mluppi](https://github.com/mluppi) - M. Luppi
* [@noavarice](https://github.com/noavarice)
* [@PalAditya](https://github.com/PalAditya) - Aditya Pal
* [@prashantbhat](https://github.com/prashantbhat) - Prashant Bhat
* [@rammetzger](https://github.com/rammetzger)
* [@razilein](https://github.com/razilein) - Sita Geßner
* [@salsolatragus](https://github.com/salsolatragus) - Sven Amann
* [@sa-sh](https://github.com/sa-sh)
* [@sixdouglas](https://github.com/sixdouglas) - Douglas Six
* [@suiaing](https://github.com/suiaing)
* [@sullis](https://github.com/sullis)
* [@SuperPat45](https://github.com/SuperPat45)
* [@syakovyn](https://github.com/syakovyn)
* [@tia-99](https://github.com/tia-99)
* [@ubermichael](https://github.com/ubermichael) - Michael Joyce
* [@VedranSlankovic](https://github.com/VedranSlankovic)
* [@V-F](https://github.com/V-F)
* [@vic0075](https://github.com/vic0075)
* [@vk-github18](https://github.com/vk-github18)
* [@weiyeh](https://github.com/weiyeh)
* [@Wugengxian](https://github.com/Wugengxian)
* [@ymasory](https://github.com/ymasory)

Also, a very special thanks to the iText developers ;)
