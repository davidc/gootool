<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:transform version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <!-- Copy everything not matched by another rule -->
  <xsl:template match="* | comment()">
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:apply-templates/>
    </xsl:copy>
  </xsl:template>

  <!-- Append our resources to the end -->
  <xsl:template match="/Resources[@id='common']">
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:apply-templates/>

      <!--<SetDefaults idprefix="" path="./"/>-->
      <!--<Image id="IMAGE_FX_SNOWFLAKE1" path="res/images/fx/snowflake1"/>-->

    </xsl:copy>

  </xsl:template>
</xsl:transform>
