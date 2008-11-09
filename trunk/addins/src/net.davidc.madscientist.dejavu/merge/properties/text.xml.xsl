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

   <string id="SIGNPOST_MSDEJAVU_1"
           text="The Goo Balls felt a strange sense of deja-vu. Hadn't they been here before?|They could build bridges in their sleep now. But would there be enough of them left?|I'm back! -the absentee Sign Painter"
           />

   <string id="SIGNPOST_MSDEJAVU_2"
           text="It wasn't this high last time, was it?|I mean, I know it's been a while since I last painted up here.|I think someone's been fiddling with my work. I'm telling MOM."
           />
  </xsl:copy>

 </xsl:template>
</xsl:transform>
