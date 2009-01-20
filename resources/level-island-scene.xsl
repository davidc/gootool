<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:transform version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:param name="level_id"/>
  <xsl:param name="level_name_id"/>

  <xsl:variable name="firstLevelX" select="-610"/>
  <xsl:variable name="firstLevelY" select="950"/>

  <xsl:variable name="offsetX" select="280"/>
  <xsl:variable name="offsetY" select="70"/>

  <xsl:variable name="levelsPerColumn" select="6"/>

  <xsl:variable name="addedLevels">
    <xsl:choose>
      <xsl:when test="/scene/@addedLevels">
        <xsl:value-of select="number(/scene/@addedLevels)"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="0"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:variable>

  <xsl:variable name="ourLevelX" select="$firstLevelX + (floor($addedLevels div $levelsPerColumn) * $offsetX)"/>
  <xsl:variable name="ourLevelY" select="$firstLevelY + (($addedLevels mod $levelsPerColumn) * $offsetY)"/>

  <!-- Copy everything not matched by another rule -->
  <xsl:template match="* | comment()">
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:apply-templates/>
    </xsl:copy>
  </xsl:template>

  <!-- Stretch the background -->
  <!-- original: <SceneLayer name="bg" depth="-480" x="0" y="525.59" scalex="3.095" scaley="1.604" rotation="0" alpha="1" colorize="255,255,255" image="IMAGE_SCENE_ISLAND1_BG"   /-->
  <xsl:template match="/scene/SceneLayer[@name='bg']">
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:attribute name="scaley">2</xsl:attribute>
      <xsl:attribute name="y">600</xsl:attribute>

      <xsl:apply-templates/>
    </xsl:copy>
  </xsl:template>

  <!-- Add our button to the buttongroup -->
  <xsl:template match="/scene/buttongroup[@id='levelMarkerGroup']">
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:apply-templates/>

      <button id="lb_{$level_id}" depth="0" x="{$ourLevelX}" y="{$ourLevelY}" scalex="1.008" scaley="0.848"
              rotation="-0.5" alpha="1" colorize="255,255,255"
              up="IMAGE_SCENE_ISLAND1_LEVELMARKERA_UP" over="IMAGE_SCENE_ISLAND1_LEVELMARKERA_OVER"
              onclick="pl_{$level_id}"/>
      <!--onmouseenter="ss_{$level_id}" onmouseexit="hs_{$level_id}"-->

    </xsl:copy>
  </xsl:template>


  <xsl:template match="/scene">
    <xsl:copy>
      <xsl:copy-of select="@*"/>

      <!-- Keep track of added levels -->
      <xsl:attribute name="addedLevels">
        <xsl:value-of select="$addedLevels+1"/>
      </xsl:attribute>

      <!-- Set max Y on the scene. Original maxy was 1100. -->
      <xsl:if test="$addedLevels+1 >= $levelsPerColumn">
        <xsl:attribute name="maxy">
          <xsl:value-of select="1100 + ($levelsPerColumn * $offsetY)"/>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="$addedLevels+1 &lt; $levelsPerColumn">
        <xsl:attribute name="maxy">
          <xsl:value-of select="1100 + (($addedLevels+1) * $offsetY)"/>
        </xsl:attribute>
      </xsl:if>

      <xsl:apply-templates/>

      <!-- Add the text label and the OCD flag -->
      <label id="txt_{$level_id}" depth="0.1" x="{$ourLevelX + 30}" y="{$ourLevelY}" align="left"
             rotation="6.337" scale="0.7" overlay="false" screenspace="false"
             font="FONT_INGAME36" text="{$level_name_id}"/>

      <SceneLayer id="ocd_{$level_id}" name="OCD_flag1" depth="-0.1" x="{$ourLevelX}" y="{$ourLevelY + 20}"
                  scalex="0.7" scaley="0.7" rotation="17.59" alpha="1" colorize="255,255,255" image="IMAGE_SCENE_ISLAND1_OCD_FLAG1"
                  anim="ocdFlagWave" animspeed="1"/>

    </xsl:copy>
  </xsl:template>
</xsl:transform>
