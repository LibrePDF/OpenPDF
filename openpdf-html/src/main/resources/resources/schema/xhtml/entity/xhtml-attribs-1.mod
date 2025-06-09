<!-- ...................................................................... -->
<!-- XHTML Common Attributes Module  ...................................... -->
<!-- file: xhtml-attribs-1.mod

     This is XHTML, a reformulation of HTML as a modular XML application.
     Copyright 1998-2001 W3C (MIT, INRIA, Keio), All Rights Reserved.

     This DTD module is identified by the PUBLIC and SYSTEM identifiers:

       PUBLIC "-//W3C//ENTITIES XHTML Common Attributes 1.0//EN"
       SYSTEM "http://www.w3.org/TR/xhtml-modularization/DTD/xhtml-attribs-1.mod"

     Revisions:
     (none)
     ....................................................................... -->

<!-- Common Attributes

     This module declares many of the common attributes for the XHTML DTD.
     %NS.decl.attrib; is declared in the XHTML Qname module.
-->

<!ENTITY % id.attrib
     "id           ID                       #IMPLIED"
>

<!ENTITY % class.attrib
     "class        NMTOKENS                 #IMPLIED"
>

<!ENTITY % title.attrib
     "title        %Text.datatype;          #IMPLIED"
>

<!ENTITY % Core.extra.attrib "" >

<!ENTITY % Core.attrib
     "%XHTML.xmlns.attrib;
      %id.attrib;
      %class.attrib;
      %title.attrib;
      %Core.extra.attrib;"
>

<!ENTITY % lang.attrib
     "xml:lang     %LanguageCode.datatype;  #IMPLIED"
>

<![%XHTML.bidi;[
<!ENTITY % dir.attrib
     "dir          ( ltr | rtl )            #IMPLIED"
>

<!ENTITY % I18n.attrib
     "%dir.attrib;
      %lang.attrib;"
>

]]>
<!ENTITY % I18n.attrib
     "%lang.attrib;"
>

<!ENTITY % Common.extra.attrib "" >

<!-- intrinsic event attributes declared previously
-->
<!ENTITY % Events.attrib "" >

<!ENTITY % Common.attrib
     "%Core.attrib;
      %I18n.attrib;
      %Events.attrib;
      %Common.extra.attrib;"
>

<!-- end of xhtml-attribs-1.mod -->
