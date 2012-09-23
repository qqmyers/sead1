var div_html = '';
var title = '';
var creator = '';

function homePageAuthorsJsonParser(json) {

	creator = '';
	var jsonString = JSON.stringify(json);
	var obj = jQuery.parseJSON(jsonString);

	if (obj.sparql.results.result != null) {
		if (obj.sparql.results.result.length == null) {
			var jsonBinding = obj.sparql.results.result.binding;
			getAuthorNamesForHomePage(jsonBinding);
		} else {
			for ( var i = 0; i < obj.sparql.results.result.length; i++) {
				var jsonBinding = obj.sparql.results.result[i].binding;
				getAuthorNamesForHomePage(jsonBinding);
			}
		}
	}
	div_html = div_html.replace("$author$", creator.substring(0, creator
			.lastIndexOf(",")));
}

function getAuthorNamesForHomePage(jsonBinding) {
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

function homePageJsonParser(json) {

	var uri = '';
	var abs = '';
	var displayTitle = '';
	var jsonString = JSON.stringify(json);
	var obj = jQuery.parseJSON(jsonString);

	for ( var i = 0; i < obj.sparql.results.result.length; i++) {
		abs='';
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
					if (displayTitle == 'Eel_river_quads_list') {
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
				+ "</a></h4><div style='margin-top:-20px;'><p><b>Authors: </b> $author$</p>"
				+ "<b>Abstract: </b>"
				+ abs.substring(0, abs.indexOf(".") + 1)
				+ " <a href='contents.html?i="
				+ uri
				+ "&t="
				+ displayTitle
				+ "'>Click here for more details.</a></div></div>";
		$.ajax({
			type : "GET",
			url : "GetCreators",
			dataType : "json",
			data : "tagID=" + uri,
			success : homePageAuthorsJsonParser,
			async : false
		});
	}

	$("#home-loading").hide();
	$("#xmlBody").html(div_html);
}
