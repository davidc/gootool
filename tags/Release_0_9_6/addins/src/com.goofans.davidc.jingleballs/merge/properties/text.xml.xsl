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
            es="Era la noche antes de Navidad, y en toda la casa|ni una criatura se mov�a, ni tan s�lo un rat�n.|Las Bolas de Goo se acurrucaban junto al fuego|deseando escapar de la ira del escritor de carteles.|Gran oportunidad; pong�moslas a trabajar.|-el escritor de carteles de las Navidades pasadas."
            ru="? ?????????????? ????, ????? ??? ???? ? ????|? ???? ???? ?? ??????????? ? ?????.|??? ?????? ?????????? ????? ? ????.|? ??????? ???????? ????????? ?????.|?? ??? ?? ????? ?????????. ???????? ???????????? ????? ? ????.|-???????????? ?????"
            de="In der Nacht vor dem Christfest, da regte im Haus|sich niemand und nichts, nicht mal eine Maus.|Die Goo-B�lle machten es sich vor dem Feuer bequem.|In der Hoffnung der Wut des Schildermalers zu entgehen.|Gute Chancen. Lasst uns sie zur Arbeit bewegen|-Ebenezer Schildmaler"
            />
    <string id="SIGNPOST_JINGLEBALLS_2"
            text="A bundle of toys lying here 'neath the tree?|Could Chapter Six be there just waiting for me?|Bah, only more Goo Balls in festive attire.|But yummy, let's roast them all over the fire."
            es="�Hay acaso regalos esperando bajo el �rbol?|�Estar� all� el Cap�tulo 6 esper�ndome?|Bah, s�lo m�s navide�as Bolas de Goo.|�am �am, vamos a asar las casta�as."
            ru="???? ???????, ??????????? ??? ??????|??? ?????? ????? ???? ??? ???? ?????|?? ???, ? ?????? ?????? ??? ????? ??? ? ??????.|? ?????? ????? ?? ?????????, ???? ? ????."
            de="Ein gro�er Geschenkeberg liegt neben dem Baum so fein?|K�nnte es das wartende, sechste Kapitel sein?|Bah, nur noch mehr Goo-B�lle im festlichen Gewand.|Lasst uns sie �ber dem Feuer r�sten mit knusprigem Rand."
            />
    <string id="SIGNPOST_JINGLEBALLS_3"
            text="As I drew in my head, and was turning around|Up the chimney escaped the Goo Balls with a bound.|But I heard them exclaim, 'ere they climbed out of sight,|&quot;Happy Christmas to all, and to all a good-night!&quot;"
            es="Tal y como hab�a imaginado, y as� suced�a|por la chimenea escapaban las Bolas de Goo.|Pero las o� gritar, al escapar de mi vista,|&quot;�Feliz Navidad a todos y Pr�spero A�o Nuevo!&quot; "
            ru="? ????????? ???, ??? ???????? ??? ???, ??? ?? ?????????|? ??????? ???????? ??????? ???????, ??? ???? ????.|?? ?????? ??? ???????? ? ???? ?????, ??????? ? ?? ???????????,|&quot;??????????? ?????????, ? ???? ????????? ????!&quot;"
            de="Dann wollt' ich die Fensterl�den zuzieh'n|und sah den Haufen durch den Kamin entfliehen.|Doch ich h�rt' sie noch rufen, von fern klang es sacht:,|&quot;Frohe Weihnachten allen, - und allen gut' Nacht!&quot;"
            />
  </xsl:copy>

 </xsl:template>
</xsl:transform>