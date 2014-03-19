var tagID = '';
var div_html_collections = '';
var div_html_datasets = '';
var title = '';
var length = '';
var main_html = '';
var uri = '';
var displayTitle = '';
var abs = '';
var creator = '';
var descriptor = '';
var keyword = '';
var index = 1;


function populateEntries(jsonBinding) {
//alert("len: " + JSON.stringify(jsonBinding, undefined, 4));

	length=null;
	var isDeleted = false;
	var type = '';
	for ( var j = 0; j < jsonBinding.length; j++) {
		$.each(jsonBinding[j], function(key, value) {
			if (value == 'tagID') {
				uri = jsonBinding[j]['uri'];

			} else if (value == 'title') {
				title = jsonBinding[j]['literal'];
				displayTitle = String(title);

				if (displayTitle.indexOf("/") != -1) {
					displayTitle = displayTitle.substring(displayTitle.lastIndexOf("/") + 1);
				}

			} else if (value == 'length') {
				length = jsonBinding[j]['literal']['content'];
/*
			} else if (abs == '' && value == 'abstract') {
				abs = jsonBinding[j]['literal'];
*/
			} else if (value == 'deleted') {
				isDeleted = jsonBinding[j]['uri'];

			} else if (value == 'type') {
				type = jsonBinding[j]['uri'];
			}
		});
	}
	if (!isDeleted) {
		if (type == "http://cet.ncsa.uiuc.edu/2007/Dataset") {
			urlToLoad = medici + dataset_Path + uri;
			div_html_datasets += "<tr><td width='10' align='center'><i class='icon-file'></i></td><td>"
					+ "<a href='"
					+ urlToLoad
					+ "' target='_blank'>"
					+ displayTitle
					+ "</a></td><td width='100'>"
					+ roundNumber((length / 1024), 2) + " KB</td></tr>";
		} else if (type == "http://cet.ncsa.uiuc.edu/2007/Collection") {
			urlToLoad = "contents?i=" + uri;
			div_html_collections += "<tr><td width='10' align='center'><i class='icon-folder-close'></i></td><td>"
					+ "<a href='"
					+ urlToLoad
					+ "'>"
					+ displayTitle
					+ "</a></td><td width='100'>N/A</td></tr>";
		}
	}
}

function contentsPageJsonParser(jsonObj) {

	div_html_collections = '';
	div_html_datasets = '';
	title = '';
	length = '';
	uri = '';


	if (jsonObj.sparql.results.result != null) {
		if (jsonObj.sparql.results.result.length == null) {
			var jsonBinding = jsonObj.sparql.results.result.binding;
			populateEntries(jsonBinding);
		} else {
			for ( var i = 0; i < jsonObj.sparql.results.result.length; ++i) {
				var jsonBinding = jsonObj.sparql.results.result[i].binding;
				populateEntries(jsonBinding);
			}
		}
	}

	$("#xmlBody").append("<br/>"
		+ "<table class='table table-striped'>"
		+ "  <tbody id='contents-table-body'>"
		+ "    <tr>"
		+ "       <th width='10'></th><th width='500'>Name</th><th width='100'>Size</th>"
		+ "    </tr>"
		+ "  </tbody>"
		+ "</table>");
	
	$("#contents-table-body").append(div_html_collections).append(div_html_datasets);

	$.ajax({
		type : "GET",
		url : "GetBiblio",
		dataType : "json",
		data : "tagID=" + tagID,
		success : function(json) {
			pageBiblioJsonParser(0, json);
		},
		async : false
	});
	

	$("#contents-loading").hide();
	$("#xmlBody").css('visibility','visible');
}

