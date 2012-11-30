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
		urlToLoad = instanceURL_Dataset + uri;
		div_html_datasets += "<tr><td width='10' align='center'><i class='icon-file'></i></td><td>"
				+ "<a href='"
				+ urlToLoad
				+ "' target='_blank'>"
				+ displayTitle
				+ "</a></td><td width='100'>"
				+ roundNumber((length / 1024), 2)
				+ " KB</td></tr>";
	} else {
		urlToLoad = "contents.html?i=" + uri + "&t=" + displayTitle;
		div_html_collections += "<tr><td width='10' align='center'><i class='icon-folder-close'></i></td><td>"
				+ "<a href='"
				+ urlToLoad
				+ "'>"
				+ displayTitle
				+ "</a></td><td width='100'>N/A</td></tr>";
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
		main_html = "<div class='well'><h3 style='margin-top:-5px;' class='page-header'>Abstract</h3><p style='margin-top:-25px;'><b>Authors: </b>$author$</p><p><b>Contacts: </b>$contact$</p><p><b>Keywords: </b><i>$keyword$</i></p><p>"
				+ abs
				+ "</p><p><b>Descriptors:</b> $descriptor$</p></div><br/>";
	}

	$.ajax({
		type : "GET",
		url : "GetCreators",
		dataType : "json",
		data : "tagID=" + tagID,
		// success : contentsPageAuthorsJsonParser,
		success : function(json) {
			contentsPageAttributesJsonParser(json, "creator");
		},
		async : false
	});

	$.ajax({
		type : "GET",
		url : "GetContacts",
		dataType : "json",
		data : "tagID=" + tagID,
		// success : contentsPageContactsJsonParser,
		success : function(json) {
			contentsPageAttributesJsonParser(json, "contact");
		},
		async : false
	});

	index = 1;
	$.ajax({
		type : "GET",
		url : "GetDescriptors",
		dataType : "json",
		data : "tagID=" + tagID,
		// success : contentsPageDescriptorsJsonParser,
		success : function(json) {
			contentsPageAttributesJsonParser(json, "descriptor");
		},
		async : false
	});

	$.ajax({
		type : "GET",
		url : "GetKeywords",
		dataType : "json",
		data : "tagID=" + tagID,
		// success : contentsPageDescriptorsJsonParser,
		success : function(json) {
			contentsPageAttributesJsonParser(json, "keyword");
		},
		async : false
	});

	main_html += "<table class='table table-striped'><tbody><tr><th width='10'></th><th width='500'>File Name</th><th width='100'>Size</th></tr>"
			+ div_html_collections + div_html_datasets + "</tbody></table>";
	$("#contents-loading").hide();
	$("#xmlBody").html(main_html);
}

function getAttributesForContentPage(jsonBinding, element) {
	$.each(jsonBinding, function(key, value) {
		if (value == 'creator' || value == 'contact') {
			var temp = jsonBinding['literal'];
			var name = temp.substring(0, temp.indexOf(':') - 1);
			var url = temp.substring(temp.indexOf(':') + 1);

			if (value == 'creator') {
				creator += "<a href='" + url + "' target=_blank>" + name
						+ "</a>, ";
			} else if (value == 'contact') {
				contact += "<a href='" + url + "' target=_blank>" + name
						+ "</a>, ";
			}
		}

		else if (value == 'descriptor') {
			var tempDescriptor = jsonBinding['literal'];
			descriptor += "<a href='" + instanceURL_Dataset + tempDescriptor
					+ "' target=_blank>image_" + index + "</a>, ";
			index++;
		}

		else if (value == 'keyword') {
			var temp = jsonBinding['uri'];
			temp = temp.substring(temp.indexOf("#") + 1);
			temp = decodeURIComponent(temp);

			// replaceAll + from temp
			while (temp.indexOf("+") != -1) {
				temp = temp.replace('+', " ");
			}
			// keyword += temp + ", ";
			keyword += "<a href='" + instanceURL_Tag + temp
					+ "' target=_blank>" + temp + "</a>, ";
		}
	});
}

function contentsPageAttributesJsonParser(json, element) {

	if (element == 'creator')
		creator = '';
	else if (element == 'contact')
		contact = '';
	else if (element == 'keyword')
		keyword = '';
	else if (element == 'descriptor')
		descriptor = '';

	var jsonString = JSON.stringify(json);
	var obj = jQuery.parseJSON(jsonString);
	if (obj.sparql.results.result != null) {
		if (obj.sparql.results.result.length == null) {
			var jsonBinding = obj.sparql.results.result.binding;
			getAttributesForContentPage(jsonBinding, element);
		} else {
			for ( var i = 0; i < obj.sparql.results.result.length; i++) {
				var jsonBinding = obj.sparql.results.result[i].binding;
				getAttributesForContentPage(jsonBinding, element);
			}
		}
	}

	if (element == 'creator')
		main_html = main_html.replace("$author$", creator.substring(0, creator
				.lastIndexOf(",")));
	else if (element == 'contact')
		main_html = main_html.replace("$contact$", contact.substring(0, contact
				.lastIndexOf(",")));
	else if (element == 'keyword')
		main_html = main_html.replace("$keyword$", keyword.substring(0, keyword
				.lastIndexOf(",")));
	else if (element == 'descriptor')
		main_html = main_html.replace("$descriptor$", descriptor.substring(0,
				descriptor.lastIndexOf(",")));
}
