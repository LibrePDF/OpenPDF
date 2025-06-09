<!-- ...................................................................... -->
<!-- XHTML Inline Style Module  ........................................... -->
<!-- file: xhtml-inlstyle-1.mod

     This is XHTML, a reformulation of HTML as a modular XML application.
     Copyright 1998-2001 W3C (MIT, INRIA, Keio), All Rights Reserved.

     This DTD module is identified by the PUBLIC and SYSTEM identifiers:

       PUBLIC "-//W3C//ENTITIES XHTML Inline Style 1.0//EN"
       SYSTEM "http://www.w3.org/TR/xhtml-modularization/DTD/xhtml-inlstyle-1.mod"

     Revisions:
     (none)
     ....................................................................... -->

<!-- Inline Style

     This module declares the 'style' attribute, used to support inline
     style markup. This module must be instantiated prior to the XHTML
     Common Attributes module in order to be included in %Core.attrib;.
-->

<!ENTITY % style.attrib
     "style        CDATA                    #IMPLIED"
>


<!ENTITY % Core.extra.attrib
     "%style.attrib;"
>

<!-- end of xhtml-inlstyle-1.mod -->
