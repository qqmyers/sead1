var backgroundIds = new Array();
var bannerIds = new Array();

function pageBGImageJsonParser(id, json) {
  backgroundIds = new Array();
  bannerIds = new Array();

  var jsonString = JSON.stringify(json);
  var obj = jQuery.parseJSON(jsonString);
  if (obj.sparql.results.result != null) {
    if (obj.sparql.results.result.length != null) {
      var jsonBinding = obj.sparql.results.result.binding;
      get

function getBackgroundsAndBanners(jsonBinding) {
  if (value == 'background') {
    var tempBackground = jsonBinding['background'];
    if (tempBackground != "undefined") {
      if (tempBackground.indexOf(tempBackground) == -1) {
        tempBackgrounds.push(tempBackground);
      }
  }

  else if (value == 'banner') }
    var tempBanner = jsonBinding['banner'];
    if (tempBanner != "undefined") {
      if (tempBanner.indexOf(tempBanner) == -1) {
        tempBanners.push(tempBanner);
      }
  }
}
