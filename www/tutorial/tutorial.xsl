<?xml version="1.0" encoding="UTF-8"?>

<!-- $Header$ -->
<!-- author: Bruno Lowagie -->

<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:html="http://www.w3.org/1999/xhtml"
	xmlns:site="http://www.lowagie.com/iText/site"
	exclude-result-prefixes="site html" >

	<xsl:output method="html" doctype-public="-//W3C//DTD HTML 4.0 Transitional//EN" indent="no" media-type="text/html" />

	<xsl:param name="root" />
	<xsl:param name="branch" />

<!-- metadata -->

	<xsl:template name="metadata">
		<head>
			<xsl:element name="title">iText Tutorial: <xsl:value-of select="site:metadata/site:title" />
			</xsl:element>
			<xsl:element name="meta">
				<xsl:attribute name="name">Description</xsl:attribute>
				<xsl:attribute name="content"><xsl:value-of select="site:metadata/site:summary" /></xsl:attribute>
			</xsl:element>
			<xsl:element name="meta">
				<xsl:attribute name="name">Keywords</xsl:attribute>
				<xsl:attribute name="content"><xsl:value-of select="site:metadata/site:keywords" /></xsl:attribute>
			</xsl:element>
			<xsl:element name="link">
				<xsl:attribute name="rel">stylesheet</xsl:attribute>
				<xsl:attribute name="href">.<xsl:value-of select="$root" />/style.css</xsl:attribute>
				<xsl:attribute name="type">text/css</xsl:attribute>
			</xsl:element>
		</head>
	</xsl:template>

	<xsl:template name="footer">
		<div xmlns="http://www.w3.org/1999/xhtml" id="footer">
			Page Updated: <xsl:value-of select="substring(site:metadata/site:updated, 8, 19)" />
			Copyright &#169; 1999-2005
			<xsl:for-each select="/site:page/site:metadata/site:author"><xsl:value-of select="." /><xsl:if test="position()!=last()">, </xsl:if></xsl:for-each><br />
			<a href="http://www.lowagie.com/iText/">iText</a> is a Free Java-Pdf library by Bruno Lowagie and Paulo Soares.
		</div>
	</xsl:template>

<!-- commercial stuff -->

	<xsl:template name="commercial">
		<div class="commercial"><br />
		
		<div align="Center"><a href="http://www.prchecker.info/" target="_blank" title="iText, the #1 Java-PDF library">
		<img src="http://www.prchecker.info/PR1_img.gif" alt="iText, the #1 Java-PDF library" border="0" /></a></div><br />
<br />
<script type="text/javascript"><![CDATA[<!--
google_ad_client = "pub-0340380473790570";
google_ad_width = 120;
google_ad_height = 600;
google_ad_format = "120x600_as";
google_ad_channel ="";
google_ad_type = "text_image";
google_color_border = "FFFFFF";
google_color_bg = "FFFFFF";
google_color_link = "1B09BD";
google_color_url = "100670";
google_color_text = "707070";
//-->]]></script>
<script type="text/javascript"
src="http://pagead2.googlesyndication.com/pagead/show_ads.js">
</script><br /><br />

			<div class="subtitle">Amazon books:</div>
			<xsl:for-each select="site:metadata/site:amazonbooks/site:book">
				<xsl:call-template name="amazonasin"><xsl:with-param name="asins"><xsl:value-of select="string(@asin)" /></xsl:with-param></xsl:call-template><br />
			</xsl:for-each>
			<xsl:for-each select="site:metadata/site:amazonbooks/site:keyword">
				<xsl:call-template name="amazonkeyword"><xsl:with-param name="keyword"><xsl:value-of select="." /></xsl:with-param></xsl:call-template><br />
			</xsl:for-each>
		</div>
	</xsl:template>

	<xsl:template name="amazonasin">
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

	<xsl:template name="amazonkeyword">
		<xsl:param name="keyword"/>
		
<script type="text/javascript"><![CDATA[<!--
document.write('<iframe src="http://rcm.amazon.com/e/cm?t=itisacatalofwebp&o=1&p=10&l=st1&mode=books&search=]]><xsl:value-of select="$keyword" /><![CDATA[&=1&fc1=&lc1=&lt1=&bg1=&f=ifr" width="120" height="460" border="0" frameborder="0" style="border:none;" scrolling="no" marginwidth="0" marginheight="0"></iframe>');
//-->]]></script>

	</xsl:template>

<!-- Table of Contents -->

	<xsl:template match="site:toc">
		<html>
			<xsl:call-template name="metadata" />
			<body>
				<a name="top" class="logo" href="http://www.lowagie.com/iText"><img src="http://www.lowagie.com/iText/images/logo.gif" border="0" alt="iText" /></a>
				<h1>Tutorial: iText by Example</h1>
				<h2><xsl:value-of select="site:metadata/site:title" /></h2>
				<div id="content">
					<div class="title">Introduction:</div>
					<a href="http://www.1t3xt.com/docs/book.php"><img align="right" border="0" src="http://www.1t3xt.com/img/book/lowagie_3d.jpg" /></a>
					<blockquote>
					<p><xsl:value-of select="site:intro" /></p>
					<p>This tutorial is far from complete; for a more comprehensive
					overview of iText's functionality, please buy the book
					<a href="http://www.1t3xt.com/docs/book.php">iText in Action</a>.
					You can also download the first and third chapter for free from
					<a href="http://www.manning.com/affiliate/idevaffiliate.php?id=223_53">http://manning.com/lowagie/</a>.</p>
					</blockquote>
					<div class="title">Table of contents of this online tutorial</div>
<br />
					<br /><br />
					<xsl:for-each select="./site:part">
						<xsl:element name="a">
							<xsl:attribute name="name"><xsl:value-of select="@name" /></xsl:attribute>
						</xsl:element>
						<div class="title"><xsl:value-of select="site:title" /></div>
						<xsl:for-each select="./site:chapter">
							<xsl:variable name="dir">/<xsl:value-of select="@path" /></xsl:variable>
							<xsl:variable name="name"><xsl:value-of select="translate(@path, '/', '_')" /></xsl:variable>
							<xsl:variable name="path"><xsl:value-of select="@path" />/index.xml</xsl:variable>
							<xsl:variable name="link"><xsl:value-of select="@path" />/index.php</xsl:variable>
							<xsl:for-each select="document($path)/site:page">
							<div>
							<xsl:element name="a">
								<xsl:attribute name="class">chapter</xsl:attribute>
								<xsl:attribute name="href"><xsl:value-of select="$link" /></xsl:attribute>
								<xsl:attribute name="name"><xsl:value-of select="$name" /></xsl:attribute>
								<xsl:value-of select="site:metadata/site:title"/>
							</xsl:element>
							<xsl:value-of select="site:metadata/site:summary"/>
							</div>
							</xsl:for-each>
						</xsl:for-each>
						<a class="top" href="#top">Go to top of the page</a>
					</xsl:for-each>
					<xsl:call-template name="footer" />
				</div>
				<xsl:call-template name="commercial" />
				<script src="http://www.google-analytics.com/urchin.js" type="text/javascript"></script>
				<script type="text/javascript"><![CDATA[_uacct = "UA-1749025-1";urchinTracker();]]></script>
			</body>
		</html>
	</xsl:template>

<!-- Keeping the html as is -->

	<xsl:template match="html:*">
		<xsl:copy>
			<xsl:apply-templates select="*|text()|@*" />
		</xsl:copy>
	</xsl:template>

	<xsl:template match="@*">
		<xsl:attribute name="{local-name()}"><xsl:value-of select="."/></xsl:attribute>
	</xsl:template>

<!-- links outside -->

	<xsl:template match="site:doc">
		<xsl:element name="a">
			<xsl:attribute name="href">http://www.1t3xt.info/api/<xsl:value-of select="translate(@class, '.', '/')" />.html<xsl:if test="@target">#<xsl:value-of select="@target" /></xsl:if></xsl:attribute>
			<xsl:value-of select="." />
		</xsl:element>
	</xsl:template>
	
	<xsl:template match="site:bookchapter">
		<xsl:element name="li">
			<xsl:element name="a">
				<xsl:attribute name="class">subtitle</xsl:attribute>
				<xsl:attribute name="href">http://1t3xt.info/examples/browse/?page=toc&amp;id=<xsl:value-of select="@chapter + 5" /></xsl:attribute>
				Chapter <xsl:value-of select="@chapter" />:
			</xsl:element>
			<xsl:variable name="book_toc">./chapters.xml</xsl:variable>
			<xsl:variable name="chapter"><xsl:value-of select="@chapter" /></xsl:variable>
			<xsl:for-each select="document($book_toc)/site:bookchapters/site:bookchapter">
				<xsl:if test="$chapter=./@chapter">
					<xsl:value-of select="."/>
				</xsl:if>
			</xsl:for-each>
		</xsl:element>
	</xsl:template>
	
	<xsl:template match="site:book">
		<xsl:element name="p">
			<xsl:element name="a">
				<xsl:attribute name="href">http://www.1t3xt.com/docs/book.php</xsl:attribute>
				<xsl:element name="img">
					<xsl:attribute name="src">http://www.1t3xt.com/img/book/lowagie_3d.jpg</xsl:attribute>
					<xsl:attribute name="border">0</xsl:attribute>
					<xsl:attribute name="align">right</xsl:attribute>
				</xsl:element>
			</xsl:element>
			The examples in this free online tutorial will help you getting started
			with iText. Note that most examples are two years old.
			Some of the examples may be obsolete. Also the theory that
			comes with the examples isn't always 100% accurate.
			If you want more recent examples or if you want to know more
			about the theoretical background of	PDF and iText, please consult the book
			<xsl:element name="a">
				<xsl:attribute name="href">http://www.1t3xt.com/docs/book.php</xsl:attribute>
				"iText in Action".
			</xsl:element>
			Note that the first and the third chapter of the book
			can be downloaded for free from <a href="http://www.manning.com/affiliate/idevaffiliate.php?id=223_53">http://manning.com/lowagie/</a>
		</xsl:element>
		<xsl:element name="p">
			More specifically:
			<xsl:element name="ul">
				<xsl:apply-templates select="*" />
			</xsl:element>
		</xsl:element>
	</xsl:template>

	<xsl:template match="site:download">
		<xsl:element name="a">
			<xsl:attribute name="href">..<xsl:value-of select="$root" />/downloads/<xsl:value-of select="." /></xsl:attribute>
			<xsl:value-of select="." />
		</xsl:element>
	</xsl:template>

	<xsl:template match="site:tutorial">
		<xsl:element name="a">
			<xsl:attribute name="href">.<xsl:value-of select="$root" /><xsl:value-of select="@chapter" />/index.php#<xsl:value-of select="@section" /></xsl:attribute>
			<xsl:value-of select="." />
		</xsl:element>
	</xsl:template>

	<xsl:template match="site:image">
		<xsl:element name="img">
			<xsl:attribute name="border">0</xsl:attribute>
			<xsl:attribute name="src">.<xsl:value-of select="$root" />/images/<xsl:value-of select="@source" /></xsl:attribute>
			<xsl:attribute name="alt"><xsl:value-of select="." /></xsl:attribute>
		</xsl:element>
	</xsl:template>

<!-- examples -->

	<xsl:template match="site:source">
		<xsl:param name="class" select="@class" />
		<div id="example">
			<xsl:for-each select="/site:page/site:examples/site:example">
				<xsl:if test="$class=./site:java/@src">
					Example: java
					<xsl:element name="a">
						<xsl:attribute name="href">/examples/com/lowagie/examples<xsl:value-of select="$branch" />/<xsl:value-of select="site:java/@src" />.java</xsl:attribute>
						com.lowagie.examples<xsl:value-of select="translate($branch, '/', '.')" />.<xsl:value-of select="site:java/@src" />
					</xsl:element>
					<xsl:if test="count(site:argument)!=0" >
						<xsl:for-each select="site:argument"><xsl:value-of select="string(' ')" /><xsl:value-of select="." /></xsl:for-each>
					</xsl:if><br />
					<xsl:value-of select="site:description/." />
					<xsl:if test="count(site:result)!=0" >: see
						<xsl:for-each select="site:result">
							<xsl:value-of select="string(' ')" />
							<xsl:element name="a">
								<xsl:attribute name="href">/examples/com/lowagie/examples<xsl:value-of select="$branch" />/<xsl:value-of select="." /></xsl:attribute>
								<xsl:value-of select="." />
							</xsl:element>
						</xsl:for-each>
					</xsl:if>
					<xsl:if test="count(site:path)!=0" ><br />Test this example:
						<xsl:for-each select="site:path">
							<xsl:value-of select="string(' ')" />
							<xsl:element name="a">
								<xsl:attribute name="href">/examples/com/lowagie/examples<xsl:value-of select="$branch" />/<xsl:value-of select="." /></xsl:attribute>
								<xsl:value-of select="@name" />
							</xsl:element>
						</xsl:for-each>
					</xsl:if>
					<br />
					<xsl:if test="count(site:externalresource)!=0" >
						External resources for this example:
						<xsl:for-each select="site:externalresource">
							<xsl:value-of select="string(' ')" />
							<xsl:element name="a">
								<xsl:attribute name="href">/examples/com/lowagie/examples<xsl:value-of select="$branch" />/<xsl:value-of select="." /></xsl:attribute>
								<xsl:value-of select="." />
							</xsl:element>
						</xsl:for-each><br />
					</xsl:if>
					<xsl:if test="count(site:extrajar)>0">
						Extra jars needed in your CLASSPATH:
						<xsl:for-each select="./site:extrajar"><xsl:value-of select="string(' ')" /><xsl:value-of select="." /></xsl:for-each><br />
					</xsl:if>
				</xsl:if>
			</xsl:for-each>
		</div>
	</xsl:template>

	<xsl:template match="site:jsp">
		<xsl:param name="jsp" select="@jsp" />
		<div id="example">
			<xsl:for-each select="/site:page/site:examples/site:example">
				<xsl:if test="$jsp=./site:java/@jsp">
					Example:
					<xsl:element name="a">
						<xsl:attribute name="href">http://itext.cvs.sourceforge.net/*checkout*/itext/www/tutorial<xsl:value-of select="$branch" />/<xsl:value-of select="site:java/@jsp" /></xsl:attribute>
						<xsl:value-of select="site:java/@jsp" />
					</xsl:element><br />
					<xsl:value-of select="site:description/." />
					<xsl:if test="count(site:path)!=0" ><br />Test this example:
						<xsl:for-each select="site:path">
							<xsl:value-of select="string(' ')" />
							<xsl:element name="a">
								<xsl:attribute name="href"><xsl:value-of select="." /></xsl:attribute>
								<xsl:value-of select="@name" />
							</xsl:element>
						</xsl:for-each>
					</xsl:if>
				</xsl:if>
			</xsl:for-each>
		</div>
	</xsl:template>

	<xsl:template match="site:examples">
		<xsl:param name="dir" />
		<xsl:for-each select="site:example">
			<div class="example">
				<xsl:if test="site:java/@src">
					<xsl:element name="a">
						<xsl:attribute name="class">source</xsl:attribute>
						<xsl:attribute name="href">/examples/com/lowagie/examples<xsl:value-of select="$dir" />/<xsl:value-of select="site:java/@src" />.java</xsl:attribute>
						<xsl:value-of select="site:java/@src" />
					</xsl:element><br />
				</xsl:if>
				<xsl:if test="site:java/@jsp">
					<xsl:element name="a">
						<xsl:attribute name="class">source</xsl:attribute>
						<xsl:attribute name="href">http://cvs.sourceforge.net/viewcvs.py/*checkout*/itext/www/tutorial<xsl:value-of select="$dir" />/<xsl:value-of select="site:java/@jsp" /></xsl:attribute>
						<xsl:value-of select="site:java/@jsp" />
					</xsl:element><br />
				</xsl:if>
				<div class="description"><xsl:value-of select="site:description/." /></div>
				<xsl:if test="count(site:extrajar)>0">
					<div class="small">Extra jars needed:</div>
					<ul><xsl:for-each select="./site:extrajar">
						<li><xsl:value-of select="." /></li>
					</xsl:for-each></ul>
				</xsl:if>
				<xsl:if test="count(site:argument)!=0" >
					<div class="small">Argument(s):</div>
					<ul><xsl:for-each select="site:argument">
						<li><xsl:value-of select="." /></li>
					</xsl:for-each></ul>
				</xsl:if>
				<xsl:if test="count(site:externalresource)!=0" >
					<div class="small">Input:</div>
					<ul><xsl:for-each select="site:externalresource">
						<li><xsl:element name="a"><xsl:attribute name="href">/examples/com/lowagie/examples<xsl:value-of select="$dir" />/<xsl:value-of select="." /></xsl:attribute><xsl:value-of select="." /></xsl:element></li>
					</xsl:for-each></ul>
				</xsl:if>
				<xsl:if test="count(site:result)!=0" >
					<div class="small">Output:</div>
					<ul><xsl:for-each select="site:result">
						<li><xsl:element name="a">
							<xsl:attribute name="href">/examples/com/lowagie/examples<xsl:value-of select="$dir" />/<xsl:value-of select="." /></xsl:attribute>
							<xsl:value-of select="." />
						</xsl:element></li>
					</xsl:for-each></ul>
				</xsl:if>
				<xsl:if test="count(site:path)!=0" >
					<div class="small">Servlets/JSP:</div>
					<ul><xsl:for-each select="site:path">
						<li><xsl:element name="a">
							<xsl:attribute name="href"><xsl:value-of select="." /></xsl:attribute>
							<xsl:value-of select="@name" />
						</xsl:element></li>
					</xsl:for-each></ul>
				</xsl:if>
			</div>
		</xsl:for-each>
		<div class="example">
			<div class="small">ANT script (all examples):</div>
			<ul><li>
				<xsl:element name="a">
					<xsl:attribute name="href">.<xsl:value-of select="$root" /><xsl:value-of select="$dir" />/build.xml</xsl:attribute>
					build.xml
				</xsl:element>
			</li></ul>
		</div>
	</xsl:template>

<!-- the sections -->

	<xsl:template match="site:chapter">
		<xsl:for-each select="site:section">
			<xsl:element name="a"><xsl:attribute name="name"><xsl:value-of select="@name" /></xsl:attribute></xsl:element>
			<div class="title"><xsl:value-of select="site:sectiontitle" />:</div>
			<xsl:apply-templates select="html:div" />
			<a class="top" href="#top">Go to top of the page</a>
		</xsl:for-each>
	</xsl:template>

<!-- the actual page -->

	<xsl:template match="site:page">
		<html>
			<xsl:call-template name="metadata" />
			<body>
				<a name="top" class="logo" href="http://www.lowagie.com/iText"><img src="http://www.lowagie.com/iText/images/logo.gif" border="0" alt="iText" /></a>
				<h1>Tutorial: iText by Example</h1>
				<h2><xsl:value-of select="site:metadata/site:title" /></h2>
				<xsl:element name="div">
					<xsl:attribute name="id">content</xsl:attribute>
					<xsl:element name="div">
						<xsl:attribute name="id">sidebar</xsl:attribute>
						<xsl:element name="a">
							<xsl:attribute name="class">toc</xsl:attribute>
							<xsl:attribute name="href">.<xsl:value-of select="$root" />/index.php#<xsl:value-of select="translate(substring($branch, 2), '/', '_')" /></xsl:attribute>
							Table of Contents
						</xsl:element>
						<div class="sidetitle">Sections:</div>
						<ul>
							<xsl:for-each select="site:chapter/site:section">
								<li><xsl:element name="a">
									<xsl:attribute name="href">#<xsl:value-of select="@name" /></xsl:attribute>
									<xsl:value-of select="site:sectiontitle" />
								</xsl:element></li>
							</xsl:for-each>
						</ul><br /><br />
						<div class="sidetitle">Examples:</div>
						<xsl:apply-templates select="site:examples"><xsl:with-param name="dir" select="$branch" /></xsl:apply-templates>
					</xsl:element>
					<xsl:element name="div">
						<xsl:attribute name="id">main</xsl:attribute>
						<xsl:if test="string(/site:page/site:metadata/site:title/@status)!='finished'"><font color="#FF0000" size="+2">UNDER CONSTRUCTION</font></xsl:if>
						<xsl:apply-templates select="site:chapter" />
						<xsl:call-template name="footer" />
					</xsl:element>
				</xsl:element>
				<xsl:call-template name="commercial" />
				<script src="http://www.google-analytics.com/urchin.js" type="text/javascript"></script>
				<script type="text/javascript"><![CDATA[_uacct = "UA-1749025-1";urchinTracker();]]></script>
			</body>
		</html>
	</xsl:template>

</xsl:stylesheet>