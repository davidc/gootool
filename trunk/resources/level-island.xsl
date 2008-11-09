<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:transform version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:param name="level_id"/>
  <xsl:param name="level_name_id"/>
  <xsl:param name="level_text_id"/>
  <xsl:param name="level_ocd"/>

  <!-- Copy everything not matched by another rule -->
  <xsl:template match="* | comment()">
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:apply-templates/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="/island">
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:apply-templates/>

      <level id="{$level_id}"
             name="{$level_name_id}"
             text="{$level_text_id}"
             ocd="{$level_ocd}"
              />
      <!--cutscene="levelFadeOut,Chapter4End,gooTransition_out" -->

      <!-- TODO: ability to have cutscenes, oncompletes, etc. Maybe just let them specify their own <level> element -->
    </xsl:copy>

  </xsl:template>
</xsl:transform>
