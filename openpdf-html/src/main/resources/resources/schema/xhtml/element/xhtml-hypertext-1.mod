<!-- ...................................................................... -->
<!-- XHTML Hypertext Module  .............................................. -->
<!-- file: xhtml-hypertext-1.mod

     This is XHTML, a reformulation of HTML as a modular XML application.
     Copyright 1998-2001 W3C (MIT, INRIA, Keio), All Rights Reserved.

     This DTD module is identified by the PUBLIC and SYSTEM identifiers:

       PUBLIC "-//W3C//ELEMENTS XHTML Hypertext 1.0//EN"
       SYSTEM "http://www.w3.org/TR/xhtml-modularization/DTD/xhtml-hypertext-1.mod"

     Revisions:
     (none)
     ....................................................................... -->

<!-- Hypertext

        a

     This module declares the anchor ('a') element type, which
     defines the source of a hypertext link. The destination
     (or link 'target') is identified via its 'id' attribute
     rather than the 'name' attribute as was used in HTML.
-->

<!-- ............  Anchor Element  ............ -->

<!ENTITY % a.element  "INCLUDE" >
<![%a.element;[
<!ENTITY % a.content
     "( #PCDATA | %InlNoAnchor.mix; )*"
>
<!ENTITY % a.qname  "a" >
<!ELEMENT %a.qname;  %a.content; >
<!-- end of a.element -->]]>

<!ENTITY % a.attlist  "INCLUDE" >
<![%a.attlist;[
<!ATTLIST %a.qname;
      %Common.attrib;
      href         %URI.datatype;           #IMPLIED
      charset      %Charset.datatype;       #IMPLIED
      type         %ContentType.datatype;   #IMPLIED
      hreflang     %LanguageCode.datatype;  #IMPLIED
      rel          %LinkTypes.datatype;     #IMPLIED
      rev          %LinkTypes.datatype;     #IMPLIED
      accesskey    %Character.datatype;     #IMPLIED
      tabindex     %Number.datatype;        #IMPLIED
>
<!-- end of a.attlist -->]]>

<!-- end of xhtml-hypertext-1.mod -->
