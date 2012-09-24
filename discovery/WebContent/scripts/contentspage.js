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

function populateEntries(jsonBinding) {

	for ( var j = 0; j < jsonBinding.length; j++) {
		$.each(jsonBinding[j], function(key, value) {
			if (value == 'tagID') {
				uri = jsonBinding[j]['uri'];

			} else if (value == 'title') {
				title = jsonBinding[j]['literal'];
				displayTitle = title;

				if (title.indexOf("/") != -1) {
					displayTitle = title.substring(title.lastIndexOf("/") + 1);
				}

			} else if (value == 'length') {
				length = jsonBinding[j]['literal']['content'];

			}
			if (abs == '' && value == 'abstract') {
				abs = jsonBinding[j]['literal'];
			}
		});
	}

	if (uri.indexOf("Dataset") != -1) {
		urlToLoad = "http://sead.ncsa.illinois.edu/nced/#dataset?id=" + uri;
		div_html_datasets += "<tr><td align='center'><i class='icon-file'></i></td><td>"
				+ "<a href='"
				+ urlToLoad
				+ "' target='_blank'>"
				+ displayTitle
				+ "</a></td><td>"
				+ roundNumber((length / 1024), 2)
				+ " KB</td></tr>";
	} else {
		urlToLoad = "contents.html?i=" + uri + "&t=" + displayTitle;
		div_html_collections += "<tr><td align='center'><i class='icon-folder-close'></i></td><td>"
				+ "<a href='"
				+ urlToLoad
				+ "'>"
				+ displayTitle
				+ "</a></td><td>N/A</td></tr>";
	}
}

function contentsPageJsonParser(jsonObj) {

	div_html_collections = '';
	div_html_datasets = '';
	title = '';
	length = '';
	main_html = '';
	uri = '';
	displayTitle = '';
	abs = '';

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
	if (abs != "") {
		main_html = "<div class='well'><h3 style='margin-top:-5px;' class='page-header'>Abstract:</h3><p style='margin-top:-25px;'><b>Authors: </b>$author$</p>"
				+ abs + "<br /><p><b>Descriptors:</b> $descriptor$</p></div><br/>";
	}
	
	$.ajax({
		type : "GET",
		url : "GetCreators",
		dataType : "json",
		data : "tagID=" + tagID,
		success : contentsPageAuthorsJsonParser,
		async : false
	});
	
	$.ajax({
		type : "GET",
		url : "GetDescriptors",
		dataType : "json",
		data : "tagID=" + tagID,
		success : contentsPageDescriptorsJsonParser,
		async : false
	});
	
	main_html += "<table class='table table-striped'><tbody><tr><th width='10'></th><th width='500'>File Name</th><th width='100'>Size</th></tr>"
			+ div_html_collections + div_html_datasets + "</tbody></table>";
	$("#contents-loading").hide();
	$("#xmlBody").html(main_html);
}

function contentsPageDescriptorsJsonParser(json){
	
	descriptor = '';
	var jsonString = JSON.stringify(json);
	var obj = jQuery.parseJSON(jsonString);

	if (obj.sparql.results.result != null) {
		if (obj.sparql.results.result.length == null) {
			var jsonBinding = obj.sparql.results.result.binding;
			getDescriptorsForContentsPage(jsonBinding, 1);
		} else {
			for ( var i = 0; i < obj.sparql.results.result.length; i++) {
				var jsonBinding = obj.sparql.results.result[i].binding;
				var index = i+1;
				getDescriptorsForContentsPage(jsonBinding, index);
			}
		}
	}
	main_html = main_html.replace("$descriptor$", descriptor.substring(0, descriptor
			.lastIndexOf(",")));
}

function contentsPageAuthorsJsonParser(json) {

	creator = '';
	var jsonString = JSON.stringify(json);
	var obj = jQuery.parseJSON(jsonString);

	if (obj.sparql.results.result != null) {
		if (obj.sparql.results.result.length == null) {
			var jsonBinding = obj.sparql.results.result.binding;
			getAuthorNamesForContentsPage(jsonBinding);
		} else {
			for ( var i = 0; i < obj.sparql.results.result.length; i++) {
				var jsonBinding = obj.sparql.results.result[i].binding;
				getAuthorNamesForContentsPage(jsonBinding);
			}
		}
	}
	main_html = main_html.replace("$author$", creator.substring(0, creator
			.lastIndexOf(",")));
}

function getAuthorNamesForContentsPage(jsonBinding) {
	$.each(jsonBinding,
			function(key, value) {
				if (value == 'creator') {
					var tempCreator = jsonBinding['literal'];
					var creatorName = tempCreator.substring(0, tempCreator
							.indexOf(':') - 1);
					var creatorURL = tempCreator.substring(tempCreator
							.indexOf(':') + 1);

					creator += "<a href='" + creatorURL + "'>" + creatorName
							+ "</a>, ";
				}
			});
}

function getDescriptorsForContentsPage(jsonBinding, index) {
	
	$.each(jsonBinding,
			function(key, value) {
				if (value == 'descriptor') {
					var tempDescriptor = jsonBinding['literal'];
					descriptor += "<a href='http://sead.ncsa.illinois.edu/nced/#dataset?id=" + tempDescriptor + "' target=_blank>image_" + index
							+ "</a>, ";
				}
			});
}
