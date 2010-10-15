<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:transform version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <!-- Copy everything not matched by another rule -->
  <xsl:template match="* | comment()">
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:apply-templates/>
    </xsl:copy>
  </xsl:template>

  <!-- Delete existing ball instances -->
  <xsl:template match="/level/BallInstance"/>

  <!-- insert drip balls into the place where the previous balls were -->
  <xsl:template match="/level/comment()[contains(., 'Balls')]">
    <xsl:copy/>
    <BallInstance type="water" x="-459.69" y="262.68" id="0" angle="0" />
    <BallInstance type="water" x="-341.64" y="279.34" id="1" angle="0" />
    <BallInstance type="water" x="-232.63" y="284.03" id="2" angle="0" />
    <BallInstance type="water" x="-122.63" y="284.03" id="3" angle="0" />
    <BallInstance type="water" x="-122.63" y="184.03" id="4" angle="0" />

    <BallInstance type="water" x="-329.74" y="411.84" id="5" angle="0" />
    <BallInstance type="water" x="-329.74" y="411.84" id="6" angle="0" />
    <BallInstance type="water" x="-329.74" y="411.84" id="7" angle="0" />
    <BallInstance type="water" x="-329.74" y="411.84" id="8" angle="0" />
    <BallInstance type="water" x="-329.74" y="411.84" id="9" angle="0" />
    <BallInstance type="water" x="-329.74" y="411.84" id="10" angle="0" />
  </xsl:template>

  <!-- Delete existing strand instances -->
  <xsl:template match="/level/Strand"/>

  <!-- Set visual debug and insert new Strands -->
  <xsl:template match="/level">
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <!-- set visual debug. set attributes after copying them, so we can overwrite -->
      <!--<xsl:attribute name="visualdebug">true</xsl:attribute>-->
      <xsl:apply-templates/>

      <!-- Here is where we can insert nodes at the end of the file -->
      <!-- As a demo, just insert the new strands at the end -->

      <Strand gb1="0" gb2="1" />
      <Strand gb1="1" gb2="2" />
      <Strand gb1="2" gb2="3" />
      <Strand gb1="3" gb2="4" />

    </xsl:copy>
  </xsl:template>
</xsl:transform>
