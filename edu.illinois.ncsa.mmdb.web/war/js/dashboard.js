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

	olmap = new OpenLayers.Map('summaryMap', {
		projection : mercator
	});
	// map.addControl(new OpenLayers.Control.MousePosition());

	var osm = new OpenLayers.Layer.OSM(undefined, [
			"//a.tile.openstreetmap.org/${z}/${x}/${y}.png",
			"//b.tile.openstreetmap.org/${z}/${x}/${y}.png",
			"//c.tile.openstreetmap.org/${z}/${x}/${y}.png" ]);
	olmap.addLayer(osm);

	layerList = $.parseJSON(JSON.stringify(json));

	var bounds = new OpenLayers.Bounds();

	if (layerList.length > 0) {
		// if layerList has more than 1 layer
		for (var i = 0; i < layerList.length; i++) {
			var l = layerList[i];

			var layer = new OpenLayers.Layer.WMS("WMS", geoProxyUrl, {
				layers : l.layerName,
				transparent : true
			}, {
				isBaseLayer : false,
				opacity : 0.8
			});
			olmap.addLayer(layer);

			// calculating bounding box to include all datasets
			var e = l.extents.split(',');
			bounds.extend(new OpenLayers.LonLat(parseFloat(e[0]),
					parseFloat(e[1])));
			bounds.extend(new OpenLayers.LonLat(parseFloat(e[2]),
					parseFloat(e[3])));
		}

		$("#mapMsg")
				.html(
						"<center><a href=\""
								+ "#geo\" id=\"geobrowseUrl\">Go to GeoBrowser</a></center>");
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
		var obj = $.parseJSON(JSON.stringify(json));
		for (var i = 0; i < obj.length; i++) {
			var entry = obj[i];
			barArray[barArray.length] = [ entry[0], entry[1] ];
		}
	}
	if (barArray.length < 2) {
		barArray[0] = [ 'No Files In Collection', 0 ];
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
$("#datatable").append($('<table/>').addClass("treetable").append( $('<thead/>').append($('<tr/>').html('<th>Name</th><th>Size</th>'))).append($('<tbody/>')));

	$.ajax({
		type : "GET",
		url : "resteasy/collections",
		dataType : "json",
		success : datatableJsonCollectionParser,
		error : datatableErrorParser
	});

}

function datatableErrorParser(jqXHR, textStatus, errorThrown) {
	$("#datatable").html("Error: Unable to load Collections");
}

function datatableJsonDataParser(json) {
	var obj = $.parseJSON(JSON.stringify(json));
	var i = 0;
	$.each(obj, function(item, props) {
		if (item != '@context') {

			$('#datatable tbody').append(getDataRow(null, i, props['Title'],
					props['Identifier'], props['Size(Bytes)']));
			i++;
		}
	});
	
	activateTable();

}

function datatableJsonCollectionParser(json) {

	var obj = $.parseJSON(JSON.stringify(json));
	var i = 0;
	$.each(obj, function(item, props) {
		if (item != '@context') {
			$('#datatable tbody').append(getCollectionRow(null, i, props['Title'],
					props['Identifier']));
			i++;
		}
	});


	$.ajax({
		type : "GET",
		url : "resteasy/datasets",
		dataType : "json",
		success : datatableJsonDataParser,
		error : datatableErrorParser
	});

}

function getDataRow(parentId, childId, name, uri, size) {
	var newRow = $('<tr/>');
	if (parentId != null) {
		childId = parentId + '-d' + childId;
	} else {
		childId = 'd' + childId;
	}
	newRow.attr('data-tt-id', childId);
	if (parentId != null) {
		newRow.attr('data-tt-parent-id', parentId);
	}

	newRow.append($('<td/>').append($('<span/>').addClass('file').append($('<a/>').attr('href', '#dataset?id=' + uri).html(name))));
	newRow.append($('<td/>').html(roundNumber((size / 1024), 2) + ' KB'));
return (newRow);
}

function getCollectionRow(parentId, childId, name, uri, size) {
	var newRow = $('<tr/>');
	if (parentId != null) {
		childId = parentId + '-' + childId;
	}
	newRow.attr('data-tt-id', childId);
	if (parentId != null) {
		newRow.attr('data-tt-parent-id', parentId);
	}
	newRow.append($('<td/>').append($('<span/>').addClass('folder').append($('<a/>').attr(
			'href', '#collection?uri=' + uri).html(name))));
	newRow.append($('<td/>').html('--'));
//return newRow;
	return newRow.add( $('<tr/>').attr('data-tt-id',childId + "-0").attr('data-tt-parent-id', childId));
}

function activateTable() {
	var table = $('#datatable table');
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


table .treetable("unloadBranch", node);
var rows=$();
					// Render loader/spinner while loading
					$
							.ajax(
									{
										async : false, // Must be false,
										// otherwise
										// loadBranch happens after
										// showChildren?
										type : "GET",
										url : "resteasy/collections/" + encodeURIComponent(tagID)
												+ "/datasets",
										dataType : "json",
									})
							.done(
									function(jsonObj) {

										var obj = $.parseJSON(JSON
												.stringify(jsonObj));
										var i = 0;

										$
												.each(
														obj,
														function(item, props) {
															if (item != '@context') {
																rows = rows.add(getDataRow(
																		node.id,
																		i,
																		props['Title'],
																		props['Identifier'],
																		props['Size(Bytes)']));
																var category = FindCategoryFromType(props['Mimetype']);
																if (datasetDistribution[category]) {
																	datasetDistribution[category] = datasetDistribution[category] + 1;
																} else {
																	datasetDistribution[category] = 1;
																}
																i++;
															}
														});

		

										$
												.ajax(
														{
															async : false, // Must
															// be
															// false,
															// otherwise
															// loadBranch
															// happens after
															// showChildren?
															type : "GET",
															url : "resteasy/collections/"
																	+ encodeURIComponent(tagID)

																	+ "/collections",
															dataType : "json",
														})
												.done(
														function(jsonObj) {

															var obj = $
																	.parseJSON(JSON
																			.stringify(jsonObj));
															var i = 0;
															$
																	.each(
																			obj,
																			function(
																					item,
																					props) {
																				if (item != '@context') {
																									rows = 	rows.add(									getCollectionRow(
																							node.id,
																							i,
																							props['Title'],
																							props
['Identifier']));
																					i++;
																				}
																			});

$('#datatable table tr[data-tt-id="'+node.id +'"]').after(rows);

$('#datatable table') .treetable("loadBranch", node, rows);
		
													try {
																drawChart();
															} catch (err) {
																// Google chart
																// isn't
																// working...
															}
														});
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
		url : "resteasy/datasets/recent",
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
	var i = 0;
	$.each(obj, function(item, props) {
		if (item != '@context') {
			creator = props['Uploaded By'];
			timestamp = props['Date'];

			creator = creator.substring(creator.lastIndexOf("/") + 1);
			date[0] = $.format.date(timestamp, 'yyyy');
			date[1] = $.format.date(timestamp, 'MMM');
			date[2] = $.format.date(timestamp, 'dd');
			date[3] = $.format.date(timestamp, 'hh:mm:ss');
			displayTitle = props['Title'];
			var displayTitleAfter = displayTitle.length > 12 ? displayTitle
					.substring(0, 11)
					+ '...' : displayTitle;
			uri = props['Identifier'];

			div_html += '<div class="recent media">'
					+ '<a class="pull-left" href="' + '#dataset?id='
					+ uri
					+ '"> <img title="'
					+ displayTitle
					+ '" class="media-object" src="'
					+ './api/image/preview/small/'
					+ uri
					+ '" /> </a>'
					+ '<div class="media-body">'
					+ '<a href="'
					+ '#dataset?id='
					+ uri
					+ '" title="'
					+ displayTitle
					+ '">'
					+ displayTitleAfter
					+ '</a></br>'
					+ map[creator]
					+ '</br>'
					+ date[0]
					+ ' '
					+ date[1]
					+ ' '
					+ date[2]
					+ ' '
					+ date[3]
					+ '</div></div>';
		}

	});

	$("#recentuploads").addClass('well');

	$("#recentuploads").html(div_html);
}

var datasetDistribution;

function callOnLoad() {
	getTeamMembers();

	loadRecentUploads();

	loadTableContent();

	$.ajax({
		url : '//www.google.com/jsapi',
		dataType : 'script',
		cache : true,
		success : function() {
			google.load('visualization', '1', {
				'packages' : [ 'corechart' ],
				'callback' : drawChart
			});
		}
	});

}
