<?xml version="1.0" encoding="UTF-8"?>

<!-- $Date: 2008-04-18 16:50:32 -0400 (Fri, 18 Apr 2008) $ -->
<!-- author: Bruno Lowagie        -->

<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:site="http://www.lowagie.com/iText/site"
	exclude-result-prefixes="site">

	<xsl:output method="xml" indent="yes" media-type="text/xml" />

	<xsl:param name="root" />
	<xsl:param name="branch" />

	<xsl:template match="site:metadata" />
	<xsl:template match="site:section" />
	
	<xsl:template match="site:examples">
		<xsl:element name="project">
			<xsl:attribute name="name">examples</xsl:attribute>
			<xsl:attribute name="default">examples</xsl:attribute>
			<xsl:attribute name="basedir">..<xsl:value-of select="$root" /></xsl:attribute>
			<target name="install">
				<property name="tutorialsrc" value="${{basedir}}/../www/tutorial" />
				<property name="tutorial" value="${{basedir}}/tutorial" />
				<antcall target="examples" />
			</target>

			<target name="examples">
				<property name="lib" value="${{basedir}}/../lib" />
				<property name="tutorial" value="${{basedir}}/tutorial" />
				<property name="examples" value="${{basedir}}/examples" />
				<property name="webapp" value="${{basedir}}/webapp" />
				<path id="classpath">
					<pathelement location="${{examples}}" />
					<pathelement location="${{lib}}/iText.jar" />
					<xsl:for-each select="./*/site:extrajar">
						<xsl:element name="pathelement">
							<xsl:attribute name="location">${lib}/<xsl:value-of select="." /></xsl:attribute>
						</xsl:element>
					</xsl:for-each>
				</path>
				<available file="${{lib}}/iText.jar" type="file" property="itext.jar.present" />
				<fail unless="itext.jar.present" message="You need the iText.jar in this directory: ${{lib}}" />
				<xsl:for-each select="./*/site:extrajar">
					<xsl:element name="available">
						<xsl:attribute name="file">${lib}/<xsl:value-of select="." /></xsl:attribute>
						<xsl:attribute name="type">file</xsl:attribute>
						<xsl:attribute name="property"><xsl:value-of select="." />.present</xsl:attribute>
					</xsl:element>
					<xsl:element name="fail">
						<xsl:attribute name="unless"><xsl:value-of select="." />.present</xsl:attribute>
						<xsl:attribute name="message">You need <xsl:value-of select="." /> in this directory ${lib}</xsl:attribute>
					</xsl:element>
				</xsl:for-each>
				
				<javac srcdir="${{examples}}" destdir="${{examples}}" verbose="false">
					<classpath refid="classpath" />
					<xsl:element name="include">
						<xsl:attribute name="name">com/lowagie/examples<xsl:value-of select="$branch" />/*.java</xsl:attribute>
					</xsl:element>
				</javac>
				<xsl:for-each select="site:example">
					<xsl:if test="site:java/@standalone='yes'">
						<xsl:element name="java">
							<xsl:attribute name="fork">yes</xsl:attribute>
							<xsl:attribute name="dir">${examples}/com/lowagie/examples<xsl:value-of select="$branch" /></xsl:attribute>
							<xsl:attribute name="classname">com.lowagie.examples<xsl:value-of select="translate($branch, '/', '.')" />.<xsl:value-of select="site:java/@src" /></xsl:attribute>
							<xsl:for-each select="site:argument">
								<xsl:element name="arg">
									<xsl:attribute name="value"><xsl:value-of select="." /></xsl:attribute>
								</xsl:element>
							</xsl:for-each>
							<classpath refid="classpath" />
						</xsl:element>
					</xsl:if>
					<xsl:if test="site:java/@webapp">
						<xsl:element name="mkdir">
							<xsl:attribute name="dir">${webapp}/<xsl:value-of select="site:java/@webapp" />/WEB-INF/lib</xsl:attribute>
						</xsl:element>
						<xsl:element name="copy">
							<xsl:attribute name="file">${lib}/iText.jar</xsl:attribute>
							<xsl:attribute name="todir">${webapp}/<xsl:value-of select="site:java/@webapp" />/WEB-INF/lib</xsl:attribute>
							<xsl:attribute name="overwrite">no</xsl:attribute>
						</xsl:element>
						<xsl:element name="mkdir">
							<xsl:attribute name="dir">${webapp}/<xsl:value-of select="site:java/@webapp" />/WEB-INF/classes/com/lowagie/examples<xsl:value-of select="$branch" /></xsl:attribute>
						</xsl:element>
						<xsl:element name="copy">
							<xsl:attribute name="todir">${webapp}/<xsl:value-of select="site:java/@webapp" />/WEB-INF/classes/com/lowagie/examples<xsl:value-of select="$branch" /></xsl:attribute>
							<xsl:attribute name="overwrite">yes</xsl:attribute>
							<xsl:element name="fileset">
								<xsl:attribute name="dir">${examples}/com/lowagie/examples<xsl:value-of select="$branch" /></xsl:attribute>
								<include name="*.class" />
							</xsl:element>
						</xsl:element>
					</xsl:if>
				</xsl:for-each>
				<xsl:for-each select="site:webapp">
					<xsl:element name="copy">
						<xsl:attribute name="file">${tutorialsrc}<xsl:value-of select="$branch" />/<xsl:value-of select="site:welcome" /></xsl:attribute>
						<xsl:attribute name="todir">${webapp}/<xsl:value-of select="@name" /></xsl:attribute>
						<xsl:attribute name="overwrite">yes</xsl:attribute>
					</xsl:element>
					<xsl:element name="copy">
						<xsl:attribute name="todir">${webapp}/<xsl:value-of select="@name" /></xsl:attribute>
						<xsl:attribute name="overwrite">yes</xsl:attribute>
						<xsl:element name="fileset">
							<xsl:attribute name="dir">${tutorialsrc}<xsl:value-of select="$branch" /></xsl:attribute>
							<include name="*.jsp" />
						</xsl:element>
					</xsl:element>
					<xsl:element name="copy">
						<xsl:attribute name="todir">${tutorial}<xsl:value-of select="$branch" /></xsl:attribute>
						<xsl:attribute name="overwrite">yes</xsl:attribute>
						<xsl:element name="fileset">
							<xsl:attribute name="dir">${tutorialsrc}<xsl:value-of select="$branch" /></xsl:attribute>
							<include name="*.jsp" />
							<include name="web.xml" />
						</xsl:element>
					</xsl:element>
					<xsl:element name="war">
						<xsl:attribute name="destfile">${webapp}/<xsl:value-of select="@name" />.war</xsl:attribute>
						<xsl:attribute name="basedir">${webapp}/<xsl:value-of select="@name" /></xsl:attribute>
						<xsl:attribute name="webxml">${tutorialsrc}<xsl:value-of select="$branch" />/<xsl:value-of select="site:webxml" /></xsl:attribute>
					</xsl:element>
				</xsl:for-each>
				<delete>
					<fileset dir="${{examples}}" includes="**/*.class"/>
				</delete>
			</target>
		</xsl:element>
	</xsl:template>
</xsl:stylesheet>