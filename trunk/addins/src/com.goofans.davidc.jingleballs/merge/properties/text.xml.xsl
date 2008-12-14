<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:transform version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

 <!-- Copy everything not matched by another rule -->
 <xsl:template match="* | comment()">
  <xsl:copy>
   <xsl:copy-of select="@*"/>
   <xsl:apply-templates/>
  </xsl:copy>
 </xsl:template>

 <!-- Append our strings to the end -->
 <xsl:template match="/strings">
  <xsl:copy>
   <xsl:copy-of select="@*"/>
   <xsl:apply-templates/>

    <string id="SIGNPOST_JINGLEBALLS_1"
            text="'Twas the night before Christmas, when all through the house|Not a creature was stirring, not even a mouse.|The Goo Balls were nestled all snug by the fire.|In hope of avoiding the Sign Painter's ire.|Fat chance. Let's put them to work.|-Ebenezer Sign Painter"
            />
    <string id="SIGNPOST_JINGLEBALLS_2"
            text="A bundle of toys lying here 'neath the tree?|Could Chapter Six be there just waiting for me?|Bah, only more Goo Balls in festive attire.|But yummy, let's roast them all over the fire."
            />
    <string id="SIGNPOST_JINGLEBALLS_3"
            text="As I drew in my head, and was turning around|Up the chimney escaped the Goo Balls with a bound.|But I heard them exclaim, 'ere they climbed out of sight,|&quot;Happy Christmas to all, and to all a good-night!&quot;"
            />
  </xsl:copy>

 </xsl:template>
</xsl:transform>
