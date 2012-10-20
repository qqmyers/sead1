var div_html = '';
var title = '';
var creator = '';
var contact = '';

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

function homePageContactsJsonParser(json) {

	contact = '';
	var jsonString = JSON.stringify(json);
	var obj = jQuery.parseJSON(jsonString);
	if (obj.sparql.results.result != null) {
		if (obj.sparql.results.result.length == null) {
			var jsonBinding = obj.sparql.results.result.binding;
			getContactNamesForHomePage(jsonBinding);
		} else {
			for ( var i = 0; i < obj.sparql.results.result.length; i++) {
				var jsonBinding = obj.sparql.results.result[i].binding;
				getContactNamesForHomePage(jsonBinding);
			}
		}
	}
	div_html = div_html.replace("$contact$", contact.substring(0, contact
			.lastIndexOf(",")));
}

function getContactNamesForHomePage(jsonBinding) {
	$.each(jsonBinding,
			function(key, value) {
				if (value == 'contact') {
					var tempContact = jsonBinding['literal'];

					var contactName = tempContact.substring(0, tempContact
							.indexOf(':') - 1);
					var contactURL = tempContact.substring(tempContact
							.indexOf(':') + 1);

					contact += "<a href='" + contactURL + "' target='_blank'>" + contactName
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
				+ "</a></h4><div style='margin-top:-20px;'><p><b>Authors: </b> $author$</p><p><b>Contacts: </b> $contact$</p>"
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
			success : homePageAuthorsJsonParser,
			async : false
		});

		$.ajax({
			type : "GET",
			url : "GetContacts",
			dataType : "json",
			data : "tagID=" + uri,
			success : homePageContactsJsonParser,
			async : false
		});
	}
	$("#home-loading").hide();
	$("#xmlBody").html(div_html);
}
