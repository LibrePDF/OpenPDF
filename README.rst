**iText is a Java PDF library originally dual licensed under MPL/LGPL**

Beginning with version 5.0 the developers have moved to the AGPL to improve their ability to sell commercial licenses.
While I fully respect the developers' wishes and rights, I feel it's important to make the final MPL/LGPL version easily available.

I have compiled the project using Java 6. The jars are available under the Downloads tab. Since iText 4.2.0 is compatible with JDK 4+, you may wish to recompile yourself to get JDK 4/5 compatiblity.
To do so:

::

 cd iText-4.2.0/src
 ant jar
 ant jar.rups
 ant jar.rtf

Additionally, version 5.0 breaks binary compatibility by changing package names from ``com.lowagie`` to ``com.itextpdf``. To offer compatibility with compatibly-licensed code targeting 5.0, I've also produced a jar of 4.2.0 using ``com.itextpdf``. See the Downloads tab. You can find the source in the ``com.lowagie`` branch of the repository. You can compile with the instructions above.