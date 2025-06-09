<!-- ...................................................................... -->
<!-- XHTML Images Module  ................................................. -->
<!-- file: xhtml-image-1.mod

     This is XHTML, a reformulation of HTML as a modular XML application.
     Copyright 1998-2001 W3C (MIT, INRIA, Keio), All Rights Reserved.

     This DTD module is identified by the PUBLIC and SYSTEM identifiers:

       PUBLIC "-//W3C//ELEMENTS XHTML Images 1.0//EN"
       SYSTEM "http://www.w3.org/TR/xhtml-modularization/DTD/xhtml-image-1.mod"

     Revisions:
     (none)
     ....................................................................... -->

<!-- Images

        img

     This module provides markup to support basic image embedding.
-->

<!-- To avoid problems with text-only UAs as well as to make
     image content understandable and navigable to users of
     non-visual UAs, you need to provide a description with
     the 'alt' attribute, and avoid server-side image maps.
-->

<!ENTITY % img.element  "INCLUDE" >
<![%img.element;[
<!ENTITY % img.content  "EMPTY" >
<!ENTITY % img.qname  "img" >
<!ELEMENT %img.qname;  %img.content; >
<!-- end of img.element -->]]>

<!ENTITY % img.attlist  "INCLUDE" >
<![%img.attlist;[
<!ATTLIST %img.qname;
      %Common.attrib;
      src          %URI.datatype;           #REQUIRED
      alt          %Text.datatype;          #REQUIRED
      longdesc     %URI.datatype;           #IMPLIED
      height       %Length.datatype;        #IMPLIED
      width        %Length.datatype;        #IMPLIED
>
<!-- end of img.attlist -->]]>

<!-- end of xhtml-image-1.mod -->
