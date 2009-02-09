<?xml version="1.0" encoding="UTF-8"?>
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
            text="'Twas the night before Christmas, when all through the house|Not a creature was stirring, not even a mouse.|The Goo Balls were nestled all snug by the fire.|In hope of avoiding the Sign Painter's ire.|Fat chance. Let's open the present early.|-Ebenezer Sign Painter"
            es="Era la noche antes de Navidad, y en toda la casa|ni una criatura se movía, ni tan sólo un ratón.|Las Bolas de Goo se acurrucaban junto al fuego|deseando escapar de la ira del escritor de carteles.|Gran oportunidad; pongámoslas a trabajar.|-el escritor de carteles de las Navidades pasadas."
            ru="В рождественскую ночь, когда все дома у меня|И даже мышь не шевельнется в норке.|Все шарики устроились уютно у огня.|В надежде избежать Авторской порки.|Но нет им шанса отдохнуть. Придется отправляться снова в путь.|-трудолюбивый Автор"
            de="In der Nacht vor dem Christfest, da regte im Haus|sich niemand und nichts, nicht mal eine Maus.|Die Goo-Bälle machten es sich vor dem Feuer bequem.|In der Hoffnung der Wut des Schildermalers zu entgehen.|Gute Chancen. Lasst uns das Geschenk früh öffnen.|-Ebenezer Schildmaler"
            nl="'t Was de nacht voor Kerstmis, en in heel het huis,|Was geen teken van leven, zelfs niet van een muis.|De Goo-ballen lagen bij het vuur te dromen,|In de hoop aan de bordjesschilder te ontkomen.|Mooi niet! Laten we het cadeau alvast openmaken.|-Ebenezer Bordjesschilder"
            fr="C'était la veille de Noël, lorsque dans toute la maison|Personne, pas même une souris, ne laissait entendre un son.|Les Boules de Goo étaient nichées bien au chaud près de l'âtre.|Dans l'espoir d'éviter la colère du peintre des pancartes.|Pas de chance. Ouvrons les cadeaux en avance.|-le peintre des pancartes Ebenezer"
            />
    <string id="SIGNPOST_JINGLEBALLS_2"
            text="A bundle of toys lying here 'neath the tree?|Could Chapter Six be there just waiting for me?|Bah, only more Goo Balls in festive attire.|But yummy, let's roast them all over the fire."
            es="¿Hay acaso regalos esperando bajo el árbol?|¿Estará allí el Capítulo 6 esperándome?|Bah, sólo más navideñas Bolas de Goo.|Ñam ñam, vamos a asar las castañas."
            ru="Гора игрушек, возлегающих под елкой?|Или шестая часть игры там ждет меня?|Эх нет, и только шарики Гуу одеты как с иголки.|И значит нужно их поджарить, сидя у огня."
            de="Ein großer Geschenkeberg liegt neben dem Baum so fein?|Könnte es das wartende, sechste Kapitel sein?|Bah, nur noch mehr Goo-Bälle im festlichen Gewand.|Lasst uns sie über dem Feuer rösten mit knusprigem Rand."
            nl="Zie ik speelgoed liggen onder de boom?|Hoofdstuk Zes misschien, waar ik al tijden van droom?|Bah, meer Goo ballen met muts en baard.|Maar wel lekker, geroosterd boven de haard."
            fr="Qu'est-ce derrière l'arbre, un tas de jouets?|Est-ce le chapitre six que depuis longtemps j'attendais?|Oh, ce ne sont que des Boules de Goo dans un déguisement joyeux.|Miam, faisons-les rôtir au-dessus du feu."
            />
    <string id="SIGNPOST_JINGLEBALLS_3"
            text="As I drew in my head, and was turning around|Up the chimney escaped the Goo Balls with a bound.|But I heard them exclaim, 'ere they climbed out of sight,|&quot;Happy Christmas to all, and to all a good-night!&quot;"
            es="Tal y como había imaginado, y así sucedía|por la chimenea escapaban las Bolas de Goo.|Pero las oí gritar, al escapar de mi vista,|&quot;¡Feliz Navidad a todos y Próspero Año Nuevo!&quot; "
            ru="Я обернулся так, что растянул всю шею, что за наказанье|И дымоход скрывает шариков бегущих, что есть мочи.|Но прежде чем убраться с глаз долой, услышал я их восклицанье,|&quot;Счастливого Рождества, и всем спокойной ночи!&quot;"
            de="Dann wollt' ich die Fensterläden zuzieh'n|und sah den Haufen durch den Kamin entfliehen.|Doch ich hört' sie noch rufen, von fern klang es sacht:,|&quot;Frohe Weihnachten allen, - und allen gut' Nacht!&quot;"
            nl="Toen ik even niet oplette, namen ze de benen,|En zijn de Goo-ballen door de schoorsteen verdwenen!|Maar ik hoorde ze roepen, door de schacht,|&quot;Vrolijk kerstfeest allemaal, en een goede nacht!&quot;"
            fr="Alors que je levais la tête, en regardant partout|Par la cheminée s'échappaient les boules de Goo.|Alors qu'elles disparaissaient, leurs paroles j'entendis,|&quot;Joyeux Noël à tous, et à tous une bonne nuit!&quot;"
            />
  </xsl:copy>

 </xsl:template>
</xsl:transform>
