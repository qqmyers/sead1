var projectPath = '';
var olmap;
var wmsResult;
var map = new Object();
var layers = new Array();

callOnLoad();
//$("#table").treetable({ expandable: true });


function initMap() {
	/**
	 * Google map initialize
	 */
	// var mapProp = {
	// center : new google.maps.LatLng(29.1536, -89.2508),
	// zoom : 5,
	// mapTypeId : google.maps.MapTypeId.HYBRID
	// };
	// var map = new google.maps.Map($("#summaryMap")[0], mapProp);
	/**
	 * OpenLayers v2 initialize
	 */
	var geographic = new OpenLayers.Projection("EPSG:4326");
    var mercator = new OpenLayers.Projection("EPSG:900913");
	
    olmap = new OpenLayers.Map( 'summaryMap', {projection: mercator} );
    //map.addControl(new OpenLayers.Control.MousePosition());
    
    
    var osm = new OpenLayers.Layer.OSM();
    olmap.addLayer(osm);
    
	layerList = getWmsLayers();
	console.log(layerList);
	var bounds = new OpenLayers.Bounds();
	for(var i=0;i < layerList.length;i++) {
		var l = layerList[i];
		console.log(l);
	    var layer = new OpenLayers.Layer.WMS("WMS", geoProxyUrl,
	            {layers: l.layerName, transparent: true},
	            {isBaseLayer: false, opacity:0.8}); 
	    olmap.addLayer(layer);
	    
	    // calculating bounding box to include all datasets
	    var e = l.extents.split(',');
	    bounds.extend(new OpenLayers.LonLat(parseFloat(e[0]), parseFloat(e[1])));
	    bounds.extend(new OpenLayers.LonLat(parseFloat(e[2]), parseFloat(e[3])));
	}

	// zoom to the bounding box to include all datasets
	olmap.zoomToExtent(bounds);
	//olmap.setCenter(new OpenLayers.LonLat(-13762945.56, 4822412.11), 10);
}

function getWmsLayers() {
// return all available wms layers
//	var layers = ["medici:angelo_basins", "medici:angelo_roads_trails"];

	return $.parseJSON($("#hidden_layersInfo").html());
}

function drawChart() {
	var barArray = new Array();

	barArray[barArray.length] = [ 'Categories', null ];
	if (datasetDistribution != undefined) {
		for (key in datasetDistribution) {
			barArray[barArray.length] = [ key, datasetDistribution[key] ];
		}
	} else {
		var allDatasetDist = $('#hidden_datasetDistribution').html().trim()
				.split(',');
		for (var i = 0; i < allDatasetDist.length; i++) {
			barArray[barArray.length] = [
					allDatasetDist[i].split('=')[0].substring(1),
					parseInt(allDatasetDist[i].split('=')[1]) ];
		}
	}

	var data = google.visualization.arrayToDataTable(barArray);

	var options = {
		title : 'Dataset Distribution',
		hAxis : {
			title : 'Category',
			titleTextStyle : {
				color : 'red'
			}
		},
		legend : {
			position : 'none'
		}
	};

	var chart = new google.visualization.ColumnChart($('#container1')[0]);

	if (barArray.length > 1) {
		chart.draw(data, options);
	} else {
		alert('No dataset in this collection');
	}

}

function loadTableContent() {

	var div_html = '';
	var title = '';
	var uri = '';
	var displayTitle = '';
	var isDeleted = null;
	var hasParent = null;

	var tagURISet = {};
	var obj = $.parseJSON($("#hidden_collections").html());
	var resultLength;
	if (jQuery.isArray(obj.sparql.results.result) == true) {
		resultLength = obj.sparql.results.result.length;
	} else {
		resultLength = obj.sparql.results.result == undefined ? 0 : 1;
	}
	
	for (var i = 0; i < resultLength; i++) {
		var jsonBinding = resultLength > 1 ? obj.sparql.results.result[i].binding : obj.sparql.results.result.binding;
		for (var j = 0; j < jsonBinding.length; j++) {
			$.each(jsonBinding[j], function(key, value) {
				if (value == 'tagID') {
					uri = jsonBinding[j]['uri'];
				} else if (value == 'title') {
					title = jsonBinding[j]['literal'];
					displayTitle = String(title);
					if (displayTitle.indexOf("/") != -1) {
						displayTitle = displayTitle.substring(displayTitle
								.lastIndexOf("/") + 1);
					}
				} else if (value == 'deleted') {
					isDeleted = jsonBinding[j]['uri'];
				} else if (value == 'parent') {
					hasParent = jsonBinding[j]['uri'];
				}
			});
		}

		if (!tagURISet[uri] && isDeleted == null && hasParent == null) {
			tagURISet[uri] = true;

			div_html += '<tr data-tt-id="' + i + '">'
					+ '<td><span class="folder"><a	href="' + projectPath
					+ '/#collection?uri=' + uri + '" target ="_blank">'
					+ displayTitle + '</a></span></td><td>--</td>'
					+ '<tr data-tt-id="' + i + '-1" data-tt-parent-id="' + i
					+ '">';

		}
		isDeleted = null;
		hasParent = null;
	}

	$("#table tbody").html(div_html);

}
function getTeamMembers() {

	var creatorURI = '';
	var creatorName = '';

	var div_html = '';
	var obj = $.parseJSON($("#hidden_creators").html());
	for (var i = 0; i < obj.sparql.results.result.length; i++) {
		creatorURI = "#";
		var jsonBinding = obj.sparql.results.result[i].binding;
		for (var j = 0; j < jsonBinding.length; j++) {
			$.each(jsonBinding[j], function(key, value) {
				if (value == 'uri') {
					creatorURI = jsonBinding[j]['uri'];
					creatorURI = creatorURI.substring(creatorURI
							.lastIndexOf("/") + 1);

				} else if (value == 'name') {
					creatorName = jsonBinding[j]['literal'];
				}
			});
		}
		map[creatorURI] = creatorName;
		// FIXME - check to see if Anonymous can do anything rather than just
		// remove it
		// Strip Anonymous, which always has a role 'anonymous' even when it has
		// no permissions in the project
		if (creatorName != "Anonymous") {
			div_html += "<a href='mailto:" + creatorURI + "' target=_blank>"
					+ creatorName + "</a> </br>";
		}
	}
	$('#teammembers').html(div_html);
}

function loadRecentUploads() {
	var div_html = '';

	var title = '';
	var uri = '';
	var displayTitle = '';
	var creator = '';
	var date = [];
	var isDeleted = null;

	var obj = $.parseJSON($("#hidden_recentuploads").html());
	var resultLength;
	if (jQuery.isArray(obj.sparql.results.result) == true) {
		resultLength = obj.sparql.results.result.length;
	} else {
		resultLength = obj.sparql.results.result == undefined ? 0 : 1;
	}
	
	for (var i = 0; i < resultLength; i++) {
		var jsonBinding = resultLength > 1 ? obj.sparql.results.result[i].binding : obj.sparql.results.result.binding;
		for (var j = 0; j < jsonBinding.length; j++) {
			$.each(jsonBinding[j], function(key, value) {
				if (value == 'tagID') {
					uri = jsonBinding[j]['uri'];
				} else if (value == 'title') {
					title = jsonBinding[j]['literal'];
					// Medici can return numbers, booleans,etc. as well as
					// strings if the titles are, e.g. "42", "true", etc.
					displayTitle = String(title);
					if (displayTitle.indexOf("/") != -1) {
						displayTitle = displayTitle.substring(displayTitle
								.lastIndexOf("/") + 1);
					}
				} else if (value == 'creator') {
					creator = jsonBinding[j]['uri'];
					creator = creator.substring(creator.lastIndexOf("/") + 1);
				} else if (value == 'date') {
					timestamp = jsonBinding[j]['literal']['content'];
					date[0] = $.format.date(timestamp, 'yyyy');
					date[1] = $.format.date(timestamp, 'MMM');
					date[2] = $.format.date(timestamp, 'dd');
					date[3] = $.format.date(timestamp, 'hh:mm:ss');

				} else if (value == 'deleted') {
					isDeleted = jsonBinding[j]['uri'];
				}
			});
		}
		if (isDeleted == null) {
			displayTitleAfter = displayTitle.length > 12 ? displayTitle
					.substring(0, 11)
					+ '...' : displayTitle;
			div_html += '<div class="media">' + '<a class="pull-left" href="'
					+ projectPath
					+ '/#dataset?id='
					+ uri
					+ '" target="_blank"> <img style="width: 100px; height: 100px;" title="'
					+ displayTitle + '" class="media-object" src="'
					+ projectPath + '/api/image/preview/small/' + uri
					+ '" /> </a>' + '<div class="media-body">' + '<a href="'
					+ projectPath + '/#dataset?id=' + uri
					+ '" target="_blank" title="' + displayTitle + '">'
					+ displayTitleAfter + '</a></br>' + map[creator] + '</br>'
					+ date[0] + ' ' + date[1] + ' ' + date[2] + ' ' + date[3]
					+ '</div></div>';
		}
		isDeleted = null;
		// images/nopreview-100.gif

	}
	$("#recentuploads").html(div_html);
}

function roundNumber(num, dec) {
	var result = Math.round(num * Math.pow(10, dec)) / Math.pow(10, dec);
	return result;
}

function loadProjectInfo(pI) {
	// FIXME: Handle non-JSON compliant text strings from Medici
	// escaping '\n' chars but other chars, i.e. single/double quotes, /, etc.,
	// which are used in the JSON structure are also a problem
	// Should eventually be dealt with when generating JSON responses in Medici
	// versus on the receiving end
	var response = pI.replace(/\n/g, "\\n");

	var jsonObj = $.parseJSON(response);
	var map = new Object();
	var nameURI;
	var descURI;
	var urlURI;
	for (var i = 0; jsonObj.sparql.results.result
			&& i < jsonObj.sparql.results.result.length; i++) {

		// for(var j =0; j<jsonObj.sparql.results.result[i].binding.length;
		// j=j+3) {
		var jsonBinding = jsonObj.sparql.results.result[i].binding;

		if (jsonBinding[2]['uri'] != undefined) {
			if (jsonBinding[2]['uri'].indexOf('ProjectURL') != -1) {
				urlURI = jsonBinding[0]['uri'];
			} else if (jsonBinding[2]['uri'].indexOf('ProjectName') != -1) {
				nameURI = jsonBinding[0]['uri'];
			} else if (jsonBinding[2]['uri'].indexOf('ProjectDescription') != -1) {
				descURI = jsonBinding[0]['uri'];
			}
		}
		// }

		/*
		 * if(jsonBinding[j]['binding'][2]['uri']!=undefined) {
		 * if(jsonBinding[j]['binding'][2]['uri'].indexOf('ProjectURL')!=-1){
		 * map['url']= jsonBinding[j+1]['binding'][2]['literal']; }else
		 * if(jsonBinding[j]['binding'][2]['uri'].indexOf('ProjectName')!=-1) {
		 * map['name']= jsonBinding[j+1]['binding'][2]['literal']; }else
		 * if(jsonBinding[j]['binding'][2]['uri'].indexOf('ProjectDescription')!=-1) {
		 * map['description']= jsonBinding[j+1]['binding'][2]['literal']; } }
		 */

	}

	for (var i = 0; jsonObj.sparql.results.result
			&& i < jsonObj.sparql.results.result.length; i++) {

		// for(var j =0; j<jsonObj.sparql.results.result[i].binding.length;
		// j=j+3)
		var jsonBinding = jsonObj.sparql.results.result[i].binding;
		// http://cet.ncsa.uiuc.edu/2007/mmdb/configuration/value
		if (jsonBinding[0]['uri'] != undefined) {
			if (jsonBinding[0]['uri'] == urlURI
					&& jsonBinding[1]['uri'] == 'http://cet.ncsa.uiuc.edu/2007/mmdb/configuration/value') {
				map['url'] = jsonBinding[2]['literal'];
			} else if (jsonBinding[0]['uri'] == nameURI
					&& jsonBinding[1]['uri'] == 'http://cet.ncsa.uiuc.edu/2007/mmdb/configuration/value') {
				map['name'] = jsonBinding[2]['literal'];
			} else if (jsonBinding[0]['uri'] == descURI
					&& jsonBinding[1]['uri'] == 'http://cet.ncsa.uiuc.edu/2007/mmdb/configuration/value') {
				map['description'] = jsonBinding[2]['literal'];
			}
		}

		/*
		 * if(jsonBinding[j]['binding'][2]['uri']!=undefined) {
		 * if(jsonBinding[j]['binding'][2]['uri'].indexOf('ProjectURL')!=-1){
		 * map['url']= jsonBinding[j+1]['binding'][2]['literal']; }else
		 * if(jsonBinding[j]['binding'][2]['uri'].indexOf('ProjectName')!=-1) {
		 * map['name']= jsonBinding[j+1]['binding'][2]['literal']; }else
		 * if(jsonBinding[j]['binding'][2]['uri'].indexOf('ProjectDescription')!=-1) {
		 * map['description']= jsonBinding[j+1]['binding'][2]['literal']; } }
		 */

	}

	$('#projectName').html(map['name']);
	$('#projectDesc').html(map['description']);
	$('#projectTitle').html(map['name']);
	$('#projectName').attr("href", (map['url']));

}



var datasetDistribution;

function SSOLogout() {

	var remoteURL = projectPath + "/api/logout";
	$.ajax({
		async : false,
		type : "GET",
		url : remoteURL,
		dataType : "text"
	});
}

function callOnLoad() {

	if (isAnonymous == 'true') {
		// Let user login as someone else
		$('#loginout').html("Login");
		$('#loginout').attr("href", "");
	} else {
		$('#loginout').click(function() {
			SSOLogout();
			return true;
		});
	}

	projectPath = $('#hidden_projectPath').html().trim();
	getTeamMembers();
	loadRecentUploads();
	loadTableContent();

	var table = $('#table');
	table
			.treetable({
				expandable : true,
				onNodeCollapse : function() {
					var node = this;
					table.treetable("unloadBranch", node);
				},
				onNodeExpand : function() {
					var node = this;
					var code = node.row[0].innerHTML;
					var test = code.substring(code.indexOf('uri') + 4);
					var tagID = test.substring(0, test.indexOf('"'));

					datasetDistribution = new Object();

					// Render loader/spinner while loading
					$
							.ajax({
								async : false, // Must be false, otherwise
								// loadBranch happens after
								// showChildren?
								type : "GET",
								url : "Contents",
								dataType : "json",
								data : "tagID=" + tagID,
							})
							.done(
									function(jsonObj) {

										var uri = '';
										var title = '';
										var displayTitle = '';
										var length = '';
										var div_html_datasets = '';
										var type = '';
										var isDeleted = false;
										for (var i = 0; jsonObj.sparql.results.result
												&& (i < jsonObj.sparql.results.result.length || jsonObj.sparql.results.result.binding); ++i) {
											var jsonBinding;
											if (jsonObj.sparql.results.result.length)
												jsonBinding = jsonObj.sparql.results.result[i].binding;
											else
												jsonBinding = jsonObj.sparql.results.result.binding;

											for (var j = 0; j < jsonBinding.length; j++) {
												$
														.each(
																jsonBinding[j],
																function(key,
																		value) {
																	if (value == 'tagID') {
																		uri = jsonBinding[j]['uri'];

																	} else if (value == 'title') {
																		title = jsonBinding[j]['literal'];
																		displayTitle = String(title);

																		if (displayTitle
																				.indexOf("/") != -1) {
																			displayTitle = displayTitle
																					.substring(displayTitle
																							.lastIndexOf("/") + 1);
																		}

																	} else if (value == 'length') {
																		length = jsonBinding[j]['literal']['content'];
																		if (length < 0)
																			length = 0 - length;

																	} else if (value == 'deleted') {
																		isDeleted = jsonBinding[j]['uri'];

																	} else if (value == 'type') {
																		type = jsonBinding[j]['uri'];
																	}

																});

											}

											if (isDeleted == null) {
												// Without filters, we'll get
												// rows where the type is
												// neither DataSet or
												// Collection, so check for both
												// with if/else if
												if (type == "http://cet.ncsa.uiuc.edu/2007/Dataset") {

													div_html_datasets += '<tr data-tt-id="'
															+ node.id
															+ '-'
															+ i
															+ '" data-tt-parent-id="'
															+ node.id
															+ '">'
															+ '<td><span class="file"><a	href="'
															+ projectPath
															+ '/#dataset?id='
															+ uri
															+ '" target ="_blank">'
															+ displayTitle
															+ '</a></span></td>'
															+ '<td>'
															+ roundNumber(
																	(length / 1024),
																	2)
															+ ' KB</td></tr>';

													var fileExt = displayTitle
															.split('.')[1];
													var mimeType = FindCategory(fileExt);
													if (datasetDistribution[mimeType]) {
														datasetDistribution[mimeType] = datasetDistribution[mimeType] + 1;
													} else {
														datasetDistribution[mimeType] = 1;
													}

												} else if (type == "http://cet.ncsa.uiuc.edu/2007/Collection") {
													div_html_datasets += '<tr data-tt-id="'
															+ node.id
															+ '-'
															+ i
															+ '" data-tt-parent-id="'
															+ node.id
															+ '">'
															+ '<td><span class="folder"><a	href="'
															+ projectPath
															+ '/#collection?uri='
															+ uri
															+ '" target ="_blank">'
															+ displayTitle
															+ '</a></span></td><td>--</td></tr>'
															+ '<tr data-tt-id="'
															+ node.id
															+ '-'
															+ i
															+ '-1" data-tt-parent-id="'
															+ node.id
															+ '-'
															+ i
															+ '">';
												}
											} else {
												isDeleted = null;
											}

											if (jsonObj.sparql.results.result.binding)
												break;
										}
										var rows = $(div_html_datasets).filter(
												"tr");

										rows
												.find(".directory")
												.parents("tr")
												.each(
														function() {
															$("#table")
																	.treetable(
																			"move",
																			node.id,
																			$(
																					this)
																					.data(
																							"ttId"));
														});

										table.treetable("loadBranch", node,
												rows);
										try {
											drawChart();
										} catch (err) {
											// Google chart isn't working...
										}
									});
				}
			});

	loadProjectInfo(projInfo);
	// Put at the end - if this fails to load, app still ~works (yes google went
	// down and broke our app one day)
	initMap();
//	google.maps.event.addDomListener(window, 'load', initialize);

	google.load("visualization", "1", {
		packages : [ "corechart" ]
	});
	google.setOnLoadCallback(drawChart);

}
