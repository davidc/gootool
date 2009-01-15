<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:transform version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <!-- Copy everything not matched by another rule -->
  <xsl:template match="* | comment()">
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:apply-templates/>
    </xsl:copy>
  </xsl:template>

  <!-- Change existing balls to UglyProduct -->
  <xsl:template match="/level/BallInstance">
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:attribute name="type">UglyProduct</xsl:attribute>
      <xsl:apply-templates/>
    </xsl:copy>
  </xsl:template>

  <!-- But we must leave the first 4 balls alone, since Ugly balls don't have strand definitions -->
  <xsl:template match="/level/BallInstance[@id &lt; 4]">
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:apply-templates/>
    </xsl:copy>
  </xsl:template>


  <xsl:template match="/level">
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <!-- set visual debug. set attributes after copying them, so we can overwrite -->
      <xsl:attribute name="visualdebug">true</xsl:attribute>
      <xsl:apply-templates/>

      <!-- Here is where we can insert nodes at the end of the file -->

      <!--<xsl:text>hi</xsl:text>-->
    </xsl:copy>
  </xsl:template>

</xsl:transform>
