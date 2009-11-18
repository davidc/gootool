<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:transform version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <!-- Copy everything not matched by another rule -->
  <xsl:template match="* | comment()">
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:apply-templates/>
    </xsl:copy>
  </xsl:template>

  <!-- Delete eyes and highlights -->
  <xsl:template match="/ball/part[@name='lefteye']"/>
  <xsl:template match="/ball/part[@name='righteye']"/>
  <xsl:template match="/ball/part[@name='hilite1']"/>
  <xsl:template match="/ball/part[@name='hilite2']"/>
</xsl:transform>
