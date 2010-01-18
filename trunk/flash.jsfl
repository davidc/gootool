// store directory to a variable
//var configDir = fl.configDirectory;
// display directory in the Output panel
//fl.trace(fl.configDirectory);


//var movie = selectMovie();
var movie = FLfile.read("file:///C|/Dev/gootool/movie/2dboyLogo.movie.xml");

var resources = FLfile.read("file:///C|/Games/WorldOfGoo/res/movie/2dboyLogo.movie.xml");
var rootDir = "file:///C|/Games/WorldOfGoo/";


// select resources file
// select root dir for images

if (movie) {
  fl.trace("IMPORT STARTING----->");
  processMovie(movie);
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

function processMovie(movie)
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
  var fileUrl = rootDir + "/res/movie/Chapter1End/creature.png";
  fl.trace(fileUrl);
  doc.importFile(fileUrl, true);
  return;


  var xml = new XML(movie);

  var lastFrameTime = xml.attribute("length")[0];
  var lastFrame = Math.round(lastFrameTime * fps);
  fl.trace("lastFrame = " + lastFrame);

  var actors = xml.descendants("actor");
  fl.trace("actors:" + actors.length());

  for (var i = 0; i < actors.length(); ++i) {
    var actor = actors[i];

    var type = actor.attribute("type")[0];
    var layerName;
    if (type == 'image') {
      var imageId = actor.descendants("image")[0].attribute("id")[0];
      layerName = imageId.toLowerCase();
      if (layerName.substr(0, 12) == "image_movie_")
        layerName = layerName.substr(12);
      layerName = layerName.substr(layerName.indexOf("_") + 1).replace("_", " ");
      //.replace("_", " ");

    }
    else {
      alert("Unknown actor type " + type + ", aborting");
      return;
    }
    //		fl.trace(actor);
    var layerNum = timeline.addNewLayer(layerName);
    //		layer

    var keyframes = actor.descendants("keyframe");

    //		timeline.insertKeyframe(lastFrame);

    var prevFrame = 0;
    for (var j = 1; j < keyframes.length(); ++j) {
      var time = keyframes[j].attribute("time")[0];
      var frame = Math.round(time * fps);
      if (frame != 0) {
        //				timeline.insertKeyframe(frame);
      }
      timeline.createMotionTween(prevFrame, j);
      //					timeline.setFrameProperty("tweenType", "motion");
      prevFrame = j;
    }

    //		timeline.setSelectedFrames(0, lastFrame);
    timeline.setFrameProperty("tweenType", "motion");

  }
  // delete the original layer
  timeline.deleteLayer(actors.length());


}


