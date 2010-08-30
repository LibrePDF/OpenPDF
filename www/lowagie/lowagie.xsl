<?xml version="1.0" encoding="UTF-8"?>

<!-- $Header: /cvsroot/itext/www/lowagie/lowagie.xsl,v 1.9 2006/04/12 12:32:51 blowagie Exp $ -->
<!-- author: Bruno Lowagie -->

<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:html="http://www.w3.org/1999/xhtml"
	xmlns:site="http://www.lowagie.com/iText/site"
	exclude-result-prefixes="site html" >

<xsl:output method="html" doctype-public="-//W3C//DTD HTML 4.0 Transitional//EN" indent="no" media-type="text/html" />

<!-- release number: change for every new release -->
<xsl:param name="releasenumber"/>
<xsl:param name="releasedate"/>

<xsl:template match="site:releasenumber" name="site:releasenumber">
<xsl:value-of select="$releasenumber"/>
</xsl:template>

<xsl:template match="site:releasedate" name="site:releasedate">
<xsl:value-of select="$releasedate"/>
</xsl:template>

<!-- releaselinks to SourceForge -->

<xsl:template match="site:releasesrc">
<xsl:element name="a">
<xsl:attribute name="href">http://prdownloads.sourceforge.net/itext/iText-src-<xsl:call-template name="site:releasenumber" />.tar.gz</xsl:attribute>
iText-src-<xsl:call-template name="site:releasenumber" />.tar.gz
</xsl:element>
or
<xsl:element name="a">
<xsl:attribute name="href">http://prdownloads.sourceforge.net/itext/iText-src-<xsl:call-template name="site:releasenumber" />.zip</xsl:attribute>
iText-src-<xsl:call-template name="site:releasenumber" />.zip
</xsl:element>
</xsl:template>

<xsl:template match="site:releasejar">
<ul>
<li>iText core: <xsl:element name="a">
<xsl:attribute name="href">http://prdownloads.sourceforge.net/itext/iText-<xsl:call-template name="site:releasenumber" />.jar</xsl:attribute>
iText-<xsl:call-template name="site:releasenumber" />.jar
</xsl:element></li>
<li>iText RTF: <xsl:element name="a">
<xsl:attribute name="href">http://prdownloads.sourceforge.net/itext/iText-rtf-<xsl:call-template name="site:releasenumber" />.jar</xsl:attribute>
iText-rtf-<xsl:call-template name="site:releasenumber" />.jar
</xsl:element></li>
<li>iText RUPS: <xsl:element name="a">
<xsl:attribute name="href">http://prdownloads.sourceforge.net/itext/iText-rups-<xsl:call-template name="site:releasenumber" />.jar</xsl:attribute>
iText-rups-<xsl:call-template name="site:releasenumber" />.jar
</xsl:element></li>
</ul>
</xsl:template>

<xsl:template match="site:releasedocs">
<xsl:element name="a">
<xsl:attribute name="href">http://prdownloads.sourceforge.net/itext/iText-docs-<xsl:call-template name="site:releasenumber" />.tar.gz</xsl:attribute>
iText-docs-<xsl:call-template name="site:releasenumber" />.tar.gz
</xsl:element>
</xsl:template>

<!-- History entries -->

<xsl:template match="site:remark"> (<xsl:value-of select="." />)</xsl:template>

<xsl:template match="site:history">
	<div xmlns="http://www.w3.org/1999/xhtml" class="title">Releases</div>
	<br xmlns="http://www.w3.org/1999/xhtml" />
	<xsl:for-each select="./*">
		<xsl:if test="local-name()='release'">
			<xsl:element name="a" namespace="http://www.w3.org/1999/xhtml">
				<xsl:attribute name="href">#<xsl:value-of select="@number" /></xsl:attribute>
				<xsl:attribute name="class">small</xsl:attribute>
				<xsl:value-of select="@name" />
			</xsl:element>
			<xsl:value-of select="string(' ')" />
			<xsl:value-of select="@date" />
		</xsl:if>
		<br xmlns="http://www.w3.org/1999/xhtml" />
	</xsl:for-each>
	<br xmlns="http://www.w3.org/1999/xhtml" /><hr xmlns="http://www.w3.org/1999/xhtml" align="Center" width="80%" /><br xmlns="http://www.w3.org/1999/xhtml" />
	<div xmlns="http://www.w3.org/1999/xhtml" class="title">Changelogs</div>
	<br xmlns="http://www.w3.org/1999/xhtml" />
	<xsl:for-each select="./*">
		<xsl:choose>
			<xsl:when test="local-name()='release'">
				<xsl:element name="a" namespace="http://www.w3.org/1999/xhtml">
					<xsl:attribute name="name"><xsl:value-of select="@number" /></xsl:attribute>
					<xsl:value-of select="@name" />
				</xsl:element>
				<xsl:apply-templates select="./site:remark" />
				<div class="small" xmlns="http://www.w3.org/1999/xhtml"><xsl:apply-templates select="./site:changelog" /></div>
			</xsl:when>
			<xsl:otherwise><br xmlns="http://www.w3.org/1999/xhtml" /><hr xmlns="http://www.w3.org/1999/xhtml" align="Center" width="10%" /></xsl:otherwise>
		</xsl:choose>
		<br xmlns="http://www.w3.org/1999/xhtml" />
	</xsl:for-each>
</xsl:template>

<!-- Amazon related stuff -->

<xsl:template match="site:amazonlist">
	<xsl:for-each select="./site:amazonproduct">
		<xsl:if test="(position() mod 3) = 1">
		<tr>
		<th><xsl:call-template name="amazonjs"><xsl:with-param name="asins"><xsl:value-of select="@asin" /></xsl:with-param></xsl:call-template></th>
		<th><xsl:if test="position()!=last()"><xsl:call-template name="amazonjs"><xsl:with-param name="asins"><xsl:value-of select="following-sibling::site:amazonproduct[position()=1]/@asin" /></xsl:with-param></xsl:call-template></xsl:if></th>
		<th><xsl:if test="position()!=last()-1 and position()!=last()"><xsl:call-template name="amazonjs"><xsl:with-param name="asins"><xsl:value-of select="following-sibling::site:amazonproduct[position()=2]/@asin" /></xsl:with-param></xsl:call-template></xsl:if></th>
		</tr>
		<tr>
		<td valign="Top" class="small">
			<xsl:apply-templates select="." /><br /><br />in Europe: try
			<xsl:element name="a">
				<xsl:attribute name="href">http://www.amazon.co.uk/exec/obidos/ASIN/<xsl:value-of select="@asin" />/catloogjecom-21</xsl:attribute>
				amazon.co.uk
			</xsl:element>
		</td>
		<td valign="Top" class="small"><xsl:if test="position()!=last()">
			<xsl:apply-templates select="following-sibling::site:amazonproduct[position()=1]" /><br /><br />in Europe: try
			<xsl:element name="a">
				<xsl:attribute name="href">http://www.amazon.co.uk/exec/obidos/ASIN/<xsl:value-of select="following-sibling::site:amazonproduct[position()=1]/@asin" />/catloogjecom-21</xsl:attribute>
				amazon.co.uk
			</xsl:element>
		</xsl:if></td>
		<td valign="Top" class="small"><xsl:if test="position()!=last()-1 and position()!=last()">
			<xsl:apply-templates select="following-sibling::site:amazonproduct[position()=2]" /><br /><br />in Europe: try
			<xsl:element name="a">
				<xsl:attribute name="href">http://www.amazon.co.uk/exec/obidos/ASIN/<xsl:value-of select="following-sibling::site:amazonproduct[position()=2]/@asin" />/catloogjecom-21</xsl:attribute>
				amazon.co.uk
			</xsl:element>
		</xsl:if></td>
		</tr>
		</xsl:if>
    </xsl:for-each>
</xsl:template>

<xsl:template match="site:amazontitle" />

<xsl:template name="amazonjs">
<xsl:param name="asins"/>
<script type="text/javascript"><![CDATA[<!--
document.write('<iframe src="http://rcm.amazon.com/e/cm?t=itisacatalofwebp&o=1&p=8&l=as1&asins=]]><xsl:value-of select="$asins" /><![CDATA[&fc1=000000&lc1=0000ff&bc1=&lt1=_blank&IS2=1&bg1=ffffff&f=ifr" width="120" height="240" scrolling="no" marginwidth="0" marginheight="0" frameborder="0" align="Center"></iframe>');
//-->]]></script>
<xsl:element name="a">
	<xsl:attribute name="href">http://www.amazon.co.uk/exec/obidos/ASIN/<xsl:value-of select="substring($asins, 0, 11)" />/catloogjecom-21</xsl:attribute>
	<xsl:attribute name="class">amazonlinks</xsl:attribute>
	amazon.co.uk-link
</xsl:element><br />
</xsl:template>

<!-- Keeping the html as is -->

<xsl:template match="html:*">
	<xsl:copy>
		<xsl:apply-templates select="*|text()|@*"/>
	</xsl:copy>
</xsl:template>

<xsl:template match="@*">
	<xsl:attribute name="{local-name()}"><xsl:value-of select="."/></xsl:attribute>
</xsl:template>

<!-- the actual page -->

<xsl:template match="site:page">
<html>

<head>
	<xsl:element name="title">iText, a Free Java-PDF Library: <xsl:value-of select="site:metadata/site:title" />
	</xsl:element>
	<xsl:element name="meta">
		<xsl:attribute name="name">Description</xsl:attribute>
		<xsl:attribute name="content"><xsl:value-of select="site:metadata/site:summary" /></xsl:attribute>
	</xsl:element>
	<xsl:element name="meta">
		<xsl:attribute name="name">Keywords</xsl:attribute>
		<xsl:attribute name="content"><xsl:value-of select="site:metadata/site:keywords" /></xsl:attribute>
	</xsl:element>
	<link rel="stylesheet" href="style.css" type="text/css" />
</head>

<body>
<xsl:element name="div">
	<xsl:attribute name="id">content</xsl:attribute>
	<xsl:apply-templates select="site:content" />

<div xmlns="http://www.w3.org/1999/xhtml" id="footer">Page Updated: <xsl:value-of select="substring(site:metadata/site:updated, 8, 19)" /><br />
Copyright &#169; 1999-2007 by Bruno Lowagie, Adolf Baeyensstraat 121, 9040 Gent, BELGIUM<br />
mailto: <a href="mailto:itext-questions@lists.sourceforge.net">itext-questions@lists.sourceforge.net</a></div>

</xsl:element>

<div id="navigation" xmlns="http://www.w3.org/1999/xhtml">
	<div id="itext" xmlns="http://www.w3.org/1999/xhtml">
		<a href="http://www.lowagie.com/iText/"><img src="images/logo.gif" /></a><br />
		a Free Java-PDF library<br />by <a class="author" HREF="http://www.lowagie.com/">Bruno Lowagie</a><br /> and <a class="author" HREF="http://itextpdf.sourceforge.net/">Paulo Soares</a>
	</div>
	<div id="links" xmlns="http://www.w3.org/1999/xhtml">
		<a class="navigation" href="http://www.lowagie.com/iText/index.html">Home @ Lowagie.com</a>
		<a class="navigation" href="http://sourceforge.net/projects/itext/">Home @ SourceForge.net</a>
		<a class="navigation" href="http://itextsharp.sourceforge.net/">iTextSharp (.NET port)</a>
		<br />
		<a class="navigation" href="download.html">Download iText</a>
		<a class="navigation" href="docs.html">Documentation</a>
		<a class="navigation" href="http://itextdocs.lowagie.com/tutorial/">iText by Example</a>
		<a class="navigation" href="svn.html">SVN Repository</a>
		<a class="navigation" href="eclipse.html">Using Eclipse</a>
		<a class="navigation" href="ant.html">ANT Scripts</a>
		<a class="navigation" href="http://1t3xt.info/tutorials/faq.php">FAQ</a>
		<br />
		<a class="navigation" href="http://lists.sourceforge.net/lists/listinfo/itext-questions">Mailing List Registration</a>
		<a class="navigation" href="http://www.1t3xt.com/about/contact.php">Mailing List Archives</a>
	</div>
</div>

<div id="sourceforge" xmlns="http://www.w3.org/1999/xhtml"><a href="http://sourceforge.net"><img src="http://sourceforge.net/sflogo.php?group_id=group_id=15255&amp;type=6" width="210" height="62" border="0" alt="SourceForge.net Logo" /></a></div>

<div id="commercial">
<div class="firefox">
<a href="http://www.prchecker.info/" target="_blank" title="iText, the #1 Java-PDF library">
<img src="http://www.prchecker.info/PR1_img.gif" alt="iText, the #1 Java-PDF library" border="0" /></a>
</div>
<a class="amazonlinks" href="amazon.html" xmlns="http://www.w3.org/1999/xhtml">Amazon books:</a>
<xsl:choose>
	<xsl:when test="count(/site:page/site:metadata/site:amazonbooks/site:book)>0">
		<xsl:call-template name="amazonjs"><xsl:with-param name="asins"><xsl:for-each select="/site:page/site:metadata/site:amazonbooks/site:book"><xsl:value-of select="string(@asin)" /><xsl:if test="position()!=last()">,</xsl:if></xsl:for-each></xsl:with-param></xsl:call-template>
	</xsl:when>
	<xsl:otherwise>
<script type="text/javascript"><![CDATA[<!--
document.write('<iframe marginwidth="0" marginheight="0" src="http://rcm.amazon.com/e/cm?t=itisacatalofwebp&o=1&p=10&l=st1&mode=books&search=JAVA&=1&fc1=&lc1=&lt1=&bg1=&f=ifr" width="120" height="460" border="0" frameborder="0" style="border:none;" scrolling="no"></iframe>');
//-->]]></script>
	</xsl:otherwise>
</xsl:choose>
<script type="text/javascript"><![CDATA[<!--
google_ad_client = "pub-0340380473790570";
google_ad_width = 120;
google_ad_height = 600;
google_ad_format = "120x600_as";
google_ad_channel ="";
google_ad_type = "text";
google_color_border = "000000";
google_color_bg = "FFFFFF";
google_color_link = "B31800";
google_color_url = "B31800";
google_color_text = "FF2200";
//-->]]></script>
<script type="text/javascript"
  src="http://pagead2.googlesyndication.com/pagead/show_ads.js">
</script>
<div class="amazonlinks">Other links:</div>
<div class="firefox"><script type="text/javascript"><![CDATA[<!--
google_ad_client = "pub-0340380473790570";
google_ad_width = 110;
google_ad_height = 32;
google_ad_format = "110x32_as_rimg";
google_cpa_choice = "CAAQ_-KZzgEaCHfyBUS9wT0_KOP143Q";
//-->]]></script>
<script type="text/javascript" src="http://pagead2.googlesyndication.com/pagead/show_ads.js">
</script></div>
</div>


<script src="http://www.google-analytics.com/urchin.js" type="text/javascript"></script>
<script type="text/javascript"><![CDATA[_uacct = "UA-1749025-1";urchinTracker();]]></script>

</body>
</html>

</xsl:template>

</xsl:stylesheet>