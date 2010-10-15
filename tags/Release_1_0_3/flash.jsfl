fl.outputPanel.clear()

// store directory to a variable
//var configDir = fl.configDirectory;
// display directory in the Output panel
//fl.trace(fl.configDirectory);


//var movie = selectMovie();
var movie = FLfile.read("file:///C|/Dev/gootool/movie/2dboyLogo.movie.xml");

var resourcesXml = FLfile.read("file:///C|/Dev/wog-extracted/res/movie/2dboyLogo/2dboyLogo.resrc.xml");
var rootDir = "file:///C|/Games/WorldOfGoo1.30/";

var resources = readResources(resourcesXml, rootDir);

// select resources file
// select root dir for images

if (movie) {
  fl.trace("IMPORT STARTING----->");
  processMovie(movie, resources);
}
else {
  fl.trace("Movie not found");
}


function selectMovie()
{
  var obsoleteDWPreviewAreaObject = {};
  var macFormatStr = "XML File|TEXT[*.xml||";
  var winFormatStr = "XML File|*.xml||";
  fileURL = fl.browseForFileURL("open", "Open WoG Movie XML", obsoleteDWPreviewAreaObject, macFormatStr, winFormatStr);
  if (!fileURL || !fileURL.length)
    return;
  //	alert("No file selected");
  //	fl.trace(fileURL);

  var ending = fileURL.slice(-4);
  if (FLfile.exists(fileURL) && ending == '.xml')
  {
    var contents = FLfile.read(fileURL);
    //		fl.trace(contents);
    return contents;
  }
}

function processMovie(movie, resources)
{
  var doc = fl.createDocument();
  if (!doc)
    return;

  doc.width = 800;
  doc.height = 600;
  var timeline = doc.getTimeline();

  var fps = 30; // TODO detect

  doc.frameRate = fps;

  //	var bi = new BitmapItem(rootDir  + "res/movie/Chapter1End/creature.png");
  //	fl.trace(bi);
  //	doc.library.addNewItem("bitmap", "creature");
  //var fileUrl = rootDir  + "/res/movie/Chapter1End/creature.png";
  //fl.trace(fileUrl);
  //doc.importFile(fileUrl, true);


  var movieXml = new XML(movie);

  var lastFrameTime = movieXml.attribute("length")[0];
  var lastFrame = Math.round(lastFrameTime * fps);
  fl.trace("lastFrame = " + lastFrame);

  var actors = movieXml.descendants("actor");
  fl.trace("actors:" + actors.length());

  var importedAlready = Array();

  for (var i = 0; i < actors.length(); ++i) {
    var actor = actors[i];

    var type = actor.attribute("type")[0];
    var layerName;
    var libItem;
    if (type == 'image') {
      var imageId = actor.descendants("image")[0].attribute("id")[0];
      layerName = imageId.toLowerCase();
      if (layerName.substr(0, 12) == "image_movie_")
        layerName = layerName.substr(12);
      layerName = layerName.substr(layerName.indexOf("_") + 1).replace("_", " ");
      if (!resources[imageId]) {
        alert("Can't find image for resource " + imageId + ", aborting");
        return;
      }

      libItem = importedAlready[imageId];
      if (!libItem) {
        doc.importFile(resources[imageId], true);
        //weak....
        var libItemIndex = doc.library.findItemIndex(resources[imageId].substr(resources[imageId].lastIndexOf('/') + 1));
        libItem = doc.library.items[libItemIndex];
        importedAlready[imageId] = libItem;
      }
    }
    else {
      alert("Unknown actor type " + type + ", aborting");
      return;
    }
    //		fl.trace(actor);
    var layerNum = timeline.addNewLayer(layerName);
    //		layer

    var keyframes = actor.descendants("keyframe");

    //		timeline.insertBlankKeyframe(lastFrame);
    //		timeline.createMotionTween(0, lastFrame);

    var prevFrame = 0;

    var lastX = 0;
    var lastY = 0;
    var lastAngle;
    var lastScaleX;
    var lastScaleY;
    var element;

    for (var j = 0; j < keyframes.length(); ++j) {

      var x = parseFloat(keyframes[j].attribute("x")[0]);
      var y = parseFloat(keyframes[j].attribute("y")[0]);
      var angle = 360 - parseFloat(keyframes[j].attribute("angle")[0]);
      var scaleX = parseFloat(keyframes[j].attribute("scale-x")[0]);
      if (!scaleX) scaleX = 1;
      var scaleY = parseFloat(keyframes[j].attribute("scale-y")[0]);
      if (!scaleY) scaleY = 1;
      var alpha = (parseInt(keyframes[j].attribute("alpha")[0]) * 100) / 255;

      var time = keyframes[j].attribute("time")[0];
      var frame = Math.round(time * fps);

      fl.trace("frame = " + frame + ", x = " + x + ", y = " + y + ", alpha = " + alpha);
      fl.trace("scale-x = " + scaleX + ", scale-y = " + scaleY + ", angle = " + angle);

      //			var layer = timeline.layers[layerNum];
      //			var frame = layer.frames[frameIndex];

      timeline.setSelectedFrames(frame, frame + 1);
      timeline.setSelectedLayers(layerNum);
      timeline.currentFrame = frame;
      if (j == 0) {
        fl.trace("adding " + libItem.name);
        doc.addItem({x:0,y:0}, libItem);
        element = timeline.layers[layerNum].frames[0].elements[0];
        //			timeline.createMotionTween(0, lastFrame);
      }
      else {
        timeline.insertKeyframe(frame);
      }

      //			timeline.layers[layerNum].frames[frame].tweenType = 'motion';

      var instance = timeline.layers[layerNum].frames[frame].elements[0];

      fl.trace("instance of " + instance.libraryItem.name + " on layer " + layerNum);

      //			instance.depth = parseFloat(actor.attribute("depth")[0]);
      //			instance.left = x - (instance.width/2);
      //			instance.top = y - (instance.height/2);
      doc.selection = [instance];
      //doc.selectNone();
      //instance.selected = true;
      fl.trace("sel = " + doc.selection);
      fl.trace("move by y = " + (y - lastY));
      doc.moveSelectionBy({x:(x - lastX), y:(y - lastY)});
      doc.setInstanceAlpha(alpha);

      instance.scaleX = scaleX;
      instance.scaleY = scaleY;
      instance.rotation = angle;
      //	doc.scaleSelection(scaleX, scaleY);
      //	doc.rotateSelection(angle);//-lastAngle);

      lastX = x;
      lastY = y;
      lastAngle = angle;
      lastScaleX = scaleX;
      lastScaleY = scaleY;

      //					timeline.setFrameProperty("tweenType", "motion");
      prevFrame = j;
    }

    //		timeline.setSelectedFrames(0, lastFrame);
    //		timeline.setFrameProperty("tweenType", "motion");
    //		timeline.createMotionTween(0, lastFrame);
    //if (i == 5) break;
  }
  // delete the original layer
  timeline.deleteLayer(actors.length());
}

function readResources(resourcesString, rootDir)
{
  var resourcesXml = new XML(resourcesString);
  var resources = Array();
  var resourcesNodes = resourcesXml.descendants("Resources");

  for (var i = 0; i < resourcesNodes.length(); ++i) {
    var resourcesEl = resourcesNodes[i];

    var defaultPath = rootDir;
    var defaultIdPrefix = "";

    for (var j = 0; j < resourcesEl.children().length(); ++j) {
      var node = resourcesEl.children()[j];
      if (node.name() == "SetDefaults") {
        defaultPath = rootDir + node.attribute("path")[0];
        defaultIdPrefix = node.attribute("idprefix")[0];
      }
      else if (node.name() == "Image") {
        var id = defaultIdPrefix + node.attribute("id")[0];
        var f = defaultPath + node.attribute("path")[0] + ".png";
        resources[id] = f;
        //				fl.trace(id + "->" + f);
      }
      else if (node.name() == "Sound") {
        var id = defaultIdPrefix + node.attribute("id")[0];
        var f = defaultPath + node.attribute("path")[0] + ".ogg";
        resources[id] = f;
        //				fl.trace(id + "->" + f);
      }
    }
  }
  return resources;
}
