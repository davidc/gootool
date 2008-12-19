<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:transform version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <!-- Copy everything not matched by another rule -->
  <xsl:template match="* | comment()">
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:apply-templates/>
    </xsl:copy>
  </xsl:template>

  <!-- Append our particle generator to the end -->
  <xsl:template match="/effects">
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:apply-templates/>


      <particleeffect name="snowStormWindow" maxparticles="60" rate="0.1">
        <particle image="IMAGE_FX_SNOWFLAKE1"
                  rotspeed="-2,2"
                  rotation="-180,180"
                  scale="0.3,1"
                  fade="false"
                  directed="false"
                  additive="false"
                  lifespan="1.5,1.5"
                  speed="3.0,5.0"
                  movedir="-90"
                  movedirvar="30"
                  acceleration="0,0">
          <axialsinoffset amp="5,25" freq="0.5,4" phaseshift="0.2,0.4" axis="x"/>
        </particle>
      </particleeffect>

    </xsl:copy>

  </xsl:template>
</xsl:transform>
