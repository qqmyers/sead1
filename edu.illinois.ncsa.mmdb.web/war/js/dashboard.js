var olmap;
var wmsResult;
var map = new Object();
var layers = new Array();
var defaultBox = new OpenLayers.Bounds(-137.42, 19.28, -61.30, 51.62);

function loadDashboard() {
	callOnLoad();
}


function initMap() {

	$.ajax({
		type : "GET",
		url : "mmdb/dashboard/GetWMSLayers",
		dataType : "json",
		success : layersJsonParser,
		error : layersErrorParser
	});

}

function layersErrorParser(jqXHR, textStatus, errorThrown) {
	$("#map").html("Error: Unable to load Map data");
}


function layersJsonParser(json) {

	/**
	 * OpenLayers v2 initialize
	 */
	var geographic = new OpenLayers.Projection("EPSG:4326");
    var mercator = new OpenLayers.Projection("EPSG:900913");
	
    olmap = new OpenLayers.Map( 'summaryMap', {projection: mercator} );
    //map.addControl(new OpenLayers.Control.MousePosition());
    
    
    var osm = new OpenLayers.Layer.OSM(undefined,
  						["//a.tile.openstreetmap.org/${z}/${x}/${y}.png",
   						 "//b.tile.openstreetmap.org/${z}/${x}/${y}.png",
   						 "//c.tile.openstreetmap.org/${z}/${x}/${y}.png"]);
    olmap.addLayer(osm);
    
	layerList =$.parseJSON(JSON.stringify(json));
	
	var bounds = new OpenLayers.Bounds();

	if(layerList.length > 0 ) {
		// if layerList has more than 1 layer
		for(var i=0;i < layerList.length;i++) {
			var l = layerList[i];
			
		    var layer = new OpenLayers.Layer.WMS("WMS", geoProxyUrl,
		            {layers: l.layerName, transparent: true},
		            {isBaseLayer: false, opacity:0.8}); 
		    olmap.addLayer(layer);
		    
		    // calculating bounding box to include all datasets
		    var e = l.extents.split(',');
		    bounds.extend(new OpenLayers.LonLat(parseFloat(e[0]), parseFloat(e[1])));
		    bounds.extend(new OpenLayers.LonLat(parseFloat(e[2]), parseFloat(e[3])));
		}
		
		$("#mapMsg").html("<center><a href=\"" + "#geo\" id=\"geobrowseUrl\">Go to GeoBrowser</a></center>");
	} else {
		// if layerList had no layer, then use the default bounding box
		bounds = defaultBox.transform(new OpenLayers.Projection("EPSG:4326"),
				new OpenLayers.Projection("EPSG:900913"));
		$("#mapMsg").html("<center>No GeoSpatial Data</center>");
	}
	// zoom to the bounding box to include all datasets
	olmap.zoomToExtent(bounds);
}


function drawChart() {

	$.ajax({
		type : "GET",
		url : "mmdb/dashboard/GetAllDatasetsByType",
		dataType : "json",
		success : datatypesJsonParser,
		error : datatypesErrorParser
	});

}

function datatypesErrorParser(jqXHR, textStatus, errorThrown) {
	$("#datadistribution").html("Error: Unable to load data distribution");
}


function datatypesJsonParser(json) {
	var barArray = new Array();

	barArray[barArray.length] = [ '', 'Count' ];
	if (datasetDistribution != undefined) {
		for (key in datasetDistribution) {
			barArray[barArray.length] = [ key, datasetDistribution[key] ];
		}
	} else {
		var obj =$.parseJSON(JSON.stringify(json));
		for (var i = 0; i < obj.length; i++) {
			var entry = obj[i];
			barArray[barArray.length] = [
				entry[0], entry[1]];
		}
	}
	if (barArray.length < 2) {
		barArray[0]=['No Files In Collection',0];
	}


	var data = google.visualization.arrayToDataTable(barArray);

	var options = {
		title : 'Dataset Distribution',
		titleTextStyle : {
			color : 'blue',
			fontSize : '16'
		},
		hAxis : {
			title : 'File Type',
			titleTextStyle : {
				color : 'blue',
				fontSize : '14'
			}
		},
		legend : {
			position : 'none'
		},
		backgroundColor : '#F5F5F5'

	};

	var chart = new google.visualization.ColumnChart($('#datadistribution')[0]);

	chart.draw(data, options);

}

function loadTableContent() {


	$.ajax({
		type : "GET",
		url : "mmdb/dashboard/GetCollections",
		dataType : "json",
		success : datatableJsonParser,
		error : datatableErrorParser
	});

}

function datatableErrorParser(jqXHR, textStatus, errorThrown) {
	$("#datatable").html("Error: Unable to load Collections");
}


function datatableJsonParser(json) {

	var div_html = '<table class="treetable"><thead><tr>';
	     div_html += '<th>Collection</th><th>Size</th></tr></thead><tbody>';
	var title = '';
	var uri = '';
	var displayTitle = '';
	var isDeleted = null;
	var hasParent = null;

	var tagURISet = {};
	var obj =$.parseJSON(JSON.stringify(json));
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
					+ '<td><span class="folder"><a	href="' +  '#collection?uri=' + uri + '" >'
					+ displayTitle + '</a></span></td><td>--</td>'
					+ '<tr data-tt-id="' + i + '-1" data-tt-parent-id="' + i
					+ '">';

		}
		isDeleted = null;
		hasParent = null;
	}
	div_html += '</tbody></table>';

	$("#datatable").html(div_html);
	activateTable();
}

function activateTable() {
	var table = $('#datatable>table');
	table .treetable({
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
								url : "mmdb/dashboard/GetCollections",
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
										var mimetype = '';
										var isDeleted = null; 
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
																	} else if (value == 'mime') {
																		mimetype = jsonBinding[j]['literal'];
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
															+ '#dataset?id='
															+ uri
															+ '" >'
															+ displayTitle
															+ '</a></span></td>'
															+ '<td>'
															+ roundNumber(
																	(length / 1024),
																	2)
															+ ' KB</td></tr>';

													var fileExt = displayTitle
															.split('.')[1];
													var category = FindCategoryFromType(mimetype);
													if (datasetDistribution[category]) {
														datasetDistribution[category] = datasetDistribution[category] + 1;
													} else {
														datasetDistribution[category] = 1;
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
													
															+ '#collection?uri='
															+ uri
		+ '\">'
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
															$("#datatable>table")
																	.treetable(
																			"move",
																			node.id,
																			$(
																					this)
																					.data(
																							"ttId"));
														});

										table .treetable("loadBranch", node,
												rows);
										try {
											drawChart();
										} catch (err) {
											// Google chart isn't working...
										}
									});
				}
			});
}

function getTeamMembers() {
	$.ajax({
		type : "GET",
		url : "mmdb/dashboard/GetTeamMembers",
		dataType : "json",
		success : teamJsonParser,
		error : teamErrorParser
	});
}


function teamErrorParser(jqXHR, textStatus, errorThrown) {
	$("#teammembers").html("Error: Unable to load Team Members");
}


function teamJsonParser(json) {
	var creatorURI = '';
	var creatorName = '';
	var div_html = '<h4>Group Members</h4>';
	var obj = $.parseJSON(JSON.stringify(json));
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

	$.ajax({
		type : "GET",
		url : "mmdb/dashboard/GetRecentUploads",
		dataType : "json",
		success : uploadsJsonParser,
		error : uploadsErrorParser
	});

}

function uploadsErrorParser(jqXHR, textStatus, errorThrown) {
	$("#recentuploads").html("Error: Unable to load recent uploads");
}

function uploadsJsonParser(json) {

	var div_html = '<h4>Recent Uploads</h4>';
	var title = '';
	var uri = '';
	var displayTitle = '';
	var creator = '';
	var date = [];
	var isDeleted = null;

	var obj = $.parseJSON(JSON.stringify(json));
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
					+ '#dataset?id='
					+ uri
					+ '"> <img style="width: 100px; height: 100px;" title="'
					+ displayTitle + '" class="media-object" src="'
					+ './api/image/preview/small/' + uri
					+ '" /> </a>' + '<div class="media-body">' + '<a href="'
					+ '#dataset?id=' + uri
					+ '" title="' + displayTitle + '">'
					+ displayTitleAfter + '</a></br>' + map[creator] + '</br>'
					+ date[0] + ' ' + date[1] + ' ' + date[2] + ' ' + date[3]
					+ '</div></div>';
		}
		isDeleted = null;
		// images/nopreview-100.gif

	}$("#recentuploads").addClass('well');
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

function callOnLoad() {

	getTeamMembers();

	loadRecentUploads();

	loadTableContent();

	
$.ajax({
    url: '//www.google.com/jsapi',
    dataType: 'script',
    cache: true,
    success: function() {
        google.load('visualization', '1', {
            'packages': ['corechart'],
            'callback': drawChart
        });
    }
});

}
