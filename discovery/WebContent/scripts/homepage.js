var div_html = '';
var title = '';
var creator = '';
var contact = '';
var keyword = '';

function homePageJsonParser(json) {

	var uri = '';
	var abs = '';
	var displayTitle = '';
	var jsonString = JSON.stringify(json);
	var obj = jQuery.parseJSON(jsonString);

	if (obj != null) {
		if (obj.sparql != null) {
			if (obj.sparql.results != null && obj.sparql.results.length == 0) {
				$("#home-loading").hide();
				return;
			}
		}
	}

	for ( var i = 0; i < obj.sparql.results.result.length; i++) {
		abs = '';
		var jsonBinding = obj.sparql.results.result[i].binding;
		for ( var j = 0; j < jsonBinding.length; j++) {
			$.each(jsonBinding[j], function(key, value) {
				if (value == 'tagID') {
					uri = jsonBinding[j]['uri'];
				} else if (value == 'title') {
					title = jsonBinding[j]['literal'];
					displayTitle = title;
					if (title.indexOf("/") != -1) {
						displayTitle = title
								.substring(title.lastIndexOf("/") + 1);
					}
				} else if (value == 'abstract') {
					abs = jsonBinding[j]['literal'];
				}
			});
		}

		div_html += "<div class='well'><h4 style='margin-top:-10px;' class='page-header'>"
				+ "<a href='contents.html?i="
				+ uri
				+ "&t="
				+ displayTitle
				+ "'>"
				+ displayTitle
				+ "</a></h4><div style='margin-top:-20px;'><p><b>Authors: </b> $author$</p><p><b>Contacts: </b> $contact$</p><p><b>Keywords: </b><i>$keyword$</i></p>"
				+ "<b>Abstract: </b>"
				+ abs.substring(0, 750)
				+ "... <a href='contents.html?i="
				+ uri
				+ "&t="
				+ displayTitle
				+ "'>Click here for more details.</a></div></div>";
		$.ajax({
			type : "GET",
			url : "GetCreators",
			dataType : "json",
			data : "tagID=" + uri,
			// success : homePageAuthorsJsonParser,
			success : function(json) {
				homePageAttributesJsonParser(json, "creator");
			},
			async : false
		});

		$.ajax({
			type : "GET",
			url : "GetContacts",
			dataType : "json",
			data : "tagID=" + uri,
			// success : homePageContactsJsonParser,
			success : function(json) {
				homePageAttributesJsonParser(json, "contact");
			},
			async : false
		});

		$.ajax({
			type : "GET",
			url : "GetKeywords",
			dataType : "json",
			data : "tagID=" + uri,
			success : function(json) {
				homePageAttributesJsonParser(json, "keyword");
			},
			async : false
		});
	}
	$("#home-loading").hide();
	$("#xmlBody").html(div_html);
}

function getAttributesForHomePage(jsonBinding, element) {
	$.each(jsonBinding, function(key, value) {
		if (value == 'creator' || value == 'contact') {
			var temp = jsonBinding['literal'];
			var name = temp.substring(0, temp.indexOf(':') - 1);
			var url = temp.substring(temp.indexOf(':') + 1);

			if (value == 'creator') {
				creator += "<a href='" + url + "' target=_blank>" + name + "</a>, ";
			} else if (value == 'contact') {
				contact += "<a href='" + url + "' target=_blank>" + name + "</a>, ";
			}
		}

		else if (value == 'keyword') {
			var temp = jsonBinding['uri'];
			temp = temp.substring(temp.indexOf("#") + 1);
			temp = decodeURIComponent(temp);
			
			//replaceAll + from temp
			while (temp.indexOf("+") != -1) {
				temp = temp.replace('+', " ");
			}
			keyword += "<a href='" + instanceURL_Tag + temp + "' target=_blank>" + temp + "</a>, ";
		}
	});
}

function homePageAttributesJsonParser(json, element) {

	if (element == 'creator')
		creator = '';
	else if (element == 'contact')
		contact = '';
	else if (element == 'keyword')
		keyword = '';

	var jsonString = JSON.stringify(json);
	var obj = jQuery.parseJSON(jsonString);
	if (obj.sparql.results.result != null) {
		if (obj.sparql.results.result.length == null) {
			var jsonBinding = obj.sparql.results.result.binding;
			getAttributesForHomePage(jsonBinding, element);
		} else {
			for ( var i = 0; i < obj.sparql.results.result.length; i++) {
				var jsonBinding = obj.sparql.results.result[i].binding;
				getAttributesForHomePage(jsonBinding, element);
			}
		}
	}

	if (element == 'creator')
		div_html = div_html.replace("$author$", creator.substring(0, creator
				.lastIndexOf(",")));
	else if (element == 'contact')
		div_html = div_html.replace("$contact$", contact.substring(0, contact
				.lastIndexOf(",")));
	else if (element == 'keyword')
		div_html = div_html.replace("$keyword$", keyword.substring(0, keyword
				.lastIndexOf(",")));

}
