<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:transform version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:param name="level_name_string"/>
  <xsl:param name="level_text_string"/>

  <!-- Copy everything not matched by another rule -->
  <xsl:template match="* | comment()">
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:apply-templates/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="/strings">
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:apply-templates/>

      <xsl:value-of select="$level_name_string" disable-output-escaping="yes"/>
      <xsl:value-of select="$level_text_string" disable-output-escaping="yes"/>

      <!--<xsl:element name="string">-->
        <!--<xsl:attribute name="id">-->
          <!--<xsl:value-of select="$string_level_name"/>-->
        <!--</xsl:attribute>-->

        <!--<xsl:for-each select="$attrib_level_name/params/names/string">-->
        <!--<xsl:attribute name="{@lang}">-->
        <!--<xsl:value-of select="."/>-->
        <!--</xsl:attribute>-->
        <!--</xsl:for-each>-->

        <!--<xsl:value-of select=""/>-->
      <!--</xsl:element>-->
    </xsl:copy>
  </xsl:template>
</xsl:transform>
