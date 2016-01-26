var sparql_Service_Path = 'resteasy/sparql';
var dataset_Path = '#dataset?id=';
var image_Path = './api/image/preview/small/';
var collection_Path = '#collection?uri=';
var tag_Path = '#tag?title=';

/*
 * function toggleVisibility(elementID) { var divElement =
 * document.getElementById(elementID); var buttonElement =
 * document.getElementById("btn" + elementID); if (divElement.style.display ==
 * "block") { divElement.style.display = "none"; buttonElement.innerHTML = "+"; }
 * else { divElement.style.display = "block"; buttonElement.innerHTML = "-"; } }
 */

function roundNumber(num, dec) {
	var result = Math.round(num * Math.pow(10, dec)) / Math.pow(10, dec);
	return result;
}

function loadPublishedCollections() {
	/*
	 * $("#home-loading").show();
	 */

	$.ajax({
		type : "GET",
		url : "resteasy/collections/published",
		dataType : "json",
		success : homePageJsonParser,
		error : homePageErrorParser
	});

}

function homePageErrorParser(jqXHR, textStatus, errorThrown) {
	$("div#xmlBody").html("Error: Unable to load Published Datasets");
}

function loadProjectInfo(pI) {
	try {
		// FIXME: Handle non-JSON compliant text strings from Medici
		// escaping '\n' chars but other chars, i.e. single/double quotes, /,
		// etc.,
		// which are used in the JSON structure (hence can't be handled by a
		// global search/replace on the whole string) are also a problem
		// Should eventually be dealt with when generating JSON responses in
		// Medici versus on the receiving end
		var response = pI.replace(/\n/g, "\\n");

		var jsonObj = $.parseJSON(response);
		var map = new Object();
		var nameURI;
		var descURI;
		var urlURI;
		for (var i = 0; jsonObj.sparql.results.result
				&& i < jsonObj.sparql.results.result.length; i++) {

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

		}

		$('#home-title').html(map['name'] + " Public Data Repository");
		$('#projectDesc').html(map['description']);
		$('#projectCollections').html(map['name'] + " Collections");
		$('#projectURL').attr("href", (map['url']));
	} catch (err) {
		// Do nothing - default values will stay
	}
}

var creators = new Array();
var creatornames = new Array();
var contacts = new Array();
var contactnames = new Array();
var keywords = new Array();
var descriptors = new Array();
var coll_location = new Array();
var pubversions = new Array();
var abstract = '';
var title = '';

function pageBiblioJsonParser(id, json) {

	creators = new Array();
	contacts = new Array();
	keywords = new Array();
	creatornames = new Array();
	contactnames = new Array();

	descriptors = new Array();
	title = '';
	abstract = '';

	coll_location = new Array();
	pubversions = new Array();
	getBiblioAttributesForPage(json);

	$("#collectionTitle" + id + ">div").html(title);

	if (document.title == "Contents: ")
		document.title = "Contents: " + title;

	// Next line only used in Contents Page
	$("#collname").html(title);

	if (abstract != "") {
		// Only set abstract if it isn't already set (homepage writes a short
		// summary
		// that should not be overwritten here)
		if (!($("#abstract" + id + ">pre").html().length)) {
			$("#abstract" + id + ">pre").html(abstract);
			$("#abstract" + id).css("visibility", "visible");
		}
	}
	var versionHtml = '';

	if (pubversions.length != 0) {

		for (var i = 0; i < pubversions.length; i++) {
			var pid = pubversions[i]['External Identifier'];
			var versionnum = pubversions[i]['version number'];
			var pubdate = pubversions[i]['publication_date'];
			if (versionnum != null) {
				versionHtml = versionHtml + "<div>Archived Version: "
						+ versionnum + ",";
				if (pubdate != null) {
					versionHtml = versionHtml + " " + pubdate + ",";
				}
				if (pid != null) {
					versionHtml = versionHtml
							+ " PID = <a target = \'blank\' href=\'" + pid
							+ "\'>" + pid + "</a></div>";
				} else {
					versionHtml = versionHtml
							+ " <i>Publication in process</i>";
				}

			}
		}
	}

	$("#versions" + id).html(
			"<div><a href = '" + collection_Path + uri
					+ "'>Current Version</a></div>" + versionHtml);
	if (creators.length != 0) {
		var creatorString = creators[0];
		var datacreatorString = creatornames[0];
		for (var i = 1; i < creators.length; i++) {
			creatorString += "; " + creators[i];
			datacreatorString += ", " + creatornames[i];
		}

		$("#authors" + id).html("<b>Authors: </b>" + creatorString);
		$("#authors" + id).css("visibility", "visible");
		$("#coll" + id).attr("data-authors", datacreatorString);
	}
	if (contacts.length != 0) {
		var contactString = contacts[0];
		var datacontactString = contactnames[0];
		for (var i = 1; i < contacts.length; i++) {
			contactString += "; " + contacts[i];
			datacontactString += ", " + contactnames[i];
		}
		$("#contacts" + id).html("<b>Contacts: </b>" + contactString);
		$("#contacts" + id).css("visibility", "visible");
		$("#coll" + id).attr("data-contacts", datacontactString);
	}
	if (coll_location.length != 0) {
		var datalocationString = coll_location[0];
		for (var i = 1; i < coll_location.length; i++) {
			datalocationString += ", " + coll_location[i];
		}

		$("#location" + id).html("<b>Location: </b>" + datalocationString);
		$("#location" + id).css("visibility", "visible");
		$("#coll" + id).attr("data-location", datalocationString);
	}
	if (keywords.length != 0) {
		var keywordString = "<i><a href='" + tag_Path + keywords[0] + "'>"
				+ keywords[0] + "</a></i>";
		var datakeywordString = keywords[0];
		for (var i = 1; i < keywords.length; i++) {
			keywordString += ", <i><a href='" + tag_Path + keywords[i] + "'>"
					+ keywords[i] + "</a></i>";
			datakeywordString += ", " + keywords[i];
		}

		$("#keywords" + id).html("<b>Keywords: </b>" + keywordString);
		$("#keywords" + id).css("visibility", "visible");
		$("#coll" + id).attr("data-Keywords", datakeywordString);
	}
	if (descriptors.length != 0) {
		descriptorString = "";
		for (var i = 0; i < descriptors.length; i++) {
			if (descriptors[i]) {
				descriptorString += " <a href='" + dataset_Path
						+ descriptors[i]
						+ "'><img class='discoveryPreview' src='" + image_Path
						+ descriptors[i] + "' alt = 'Click to view' />"
						+ "</a> ";
			}
		}

		$("#description" + id).html(
				"<b>Descriptive Data: </b>" + descriptorString);
		$("#description" + id).css("visibility", "visible");
	}
}

function parsePeople(people, type) {
	if (people != null) {
		if (jQuery.isArray(people)) {
			for (var i = 0; i < people.length; i++) {
				parsePerson(people[i], type);
			}
		} else {
			parsePerson(people, type);
		}
	}
}

function parsePerson(person, type) {

	var html = '';
	var name = '';
	if ((person.indexOf(':') != -1) && (person.indexOf('vivo') != -1)) {
		name = person.substring(0, person.indexOf(':') - 1);
		var url = person.substring(person.indexOf(':') + 1);
		html = "<a href='" + url + "' target=_blank>" + name + "</a>";
	} else {
		$.ajax({
			type : "GET",
			url : "resteasy/people/" + encodeURIComponent(person),
			dataType : "json",
			async : false,
			success : function(data) {
				name = data.familyName + ", " + data.givenName;
				html = "<a href='" + data['@id'] + "' target=_blank>" + name
						+ "</a>";
			},
			error : function() {
				html = person;
				name = person;
			}
		});
	}

	if (type == 'creator') {
		if (creators.indexOf(html) == -1) {
			creators.push(html);
			name = name.replace(",", "\\,\\");
			creatornames.push(name);
		}
	} else if (type == 'contact') {
		if (contacts.indexOf(html) == -1) {
			contacts.push(html);
			name = name.replace(",", "\\,\\");
			contactnames.push(name);

		}
	}
}
function getIf(term) {
	if (term != null) {
		return term;
	} else {
		return "";
	}
}

function getBiblioAttributesForPage(pub) {
	parsePeople(pub.Creator, "creator");
	parsePeople(pub.Contact, "contact");
	abstract = getIf(pub.Abstract);
	title = getIf(pub.Title);
	uri = pub.Identifier;
	if (pub.Location != null) {
		if (jQuery.isArray(pub.Location)) {
			coll_location = pub.Location;
		} else {
			coll_location.push(pub.Location);
		}
	}

	// DcTerms - the 'right' way to do this
	if (pub.Descriptor != null) {
		if (jQuery.isArray(pub.Descriptor)) {
			descriptors = pub.Descriptor;
		} else {
			descriptors.push(pub.Descriptor);
		}
	}
	// Also pick up dc:elements description (user metadata) that is a
	// dataset ID (since we can add a dataset as metadata)
	if (pub.Description != null) {
		if (jQuery.isArray(pub.Description)) {
			for (var i = 0; i < pub.Description.length; i++) {
				var desc = pub.Description[i];
				if (desc.indexOf('tag:') == 0) {
					descriptors.push(desc);
				}
			}
		} else {
			if (pub.Description.indexOf("tag:") == 0) {

				descriptors.push(pub.Description);
			}
		}
	}

	if (pub.Keyword != null) {
		if (jQuery.isArray(pub.Keyword)) {
			for (var i = 0; i < pub.Keyword.length; i++) {
				word = pub.Keyword[i];
				word = word.substring(word.indexOf("#") + 1);
				word = decodeURIComponent(word);
				// replaceAll + from word
				while (word.indexOf("+") != -1) {
					word = word.replace('+', " ");
				}
				keywords.push(word);
			}
		} else {
			word = pub.Keyword.substring(pub.Keyword.indexOf("#") + 1);
			word = decodeURIComponent(word);
			// replaceAll + from word
			while (word.indexOf("+") != -1) {
				word = word.replace('+', " ");
			}
			keywords.push(word);
		}
	}

	if (pub['Published Version'] != null) {
		if (jQuery.isArray(pub['Published Version'])) {
			pubversions = pub['Published Version'];
		} else {
			pubversions.push(pub['Published Version']);
		}
	}
}
function createBlock(id, element) {
	$(element).append($("<div/>").attr("id", "coll" + id));
	$("#coll" + id).append($("<div/>").attr("name", id));

	$("#coll" + id).append(
			$("<div/>").attr("id", "title" + id).css('background-color',
					'#dfdfdf'));
	$("#title" + id).append(
			$("<table/>").append(
					$("<tbody/>").append(
							$("<tr/>").append(
									$("<td/>").append(
											$("<h1/>").append("<div/>").attr(
													"id",
													"collectionTitle" + id)
													.css("padding-top", "5px")
													.css("padding-left", "5px")
													.css("padding-bottom",
															"3px"))))));
	$("#title" + id + ">table>tbody>tr").append($("<td/>"));
	$("#coll" + id).append(
			$("<div/>").attr("class", "well").attr("id", "div" + id));
	$("#div" + id).append(
			$("<div>Available Versions</div>").attr("class", "versionlist"));
	$("#div" + id + ">div").append(
			($("<p/>")).attr("id", "versions" + id)
					.css("visibility", "visible"));
	$("#div" + id).append(
			($("<p/>")).attr("id", "authors" + id).css("visibility", "hidden"));
	$("#div" + id)
			.append(
					($("<p/>")).attr("id", "contacts" + id).css("visibility",
							"hidden"));
	$("#div" + id)
			.append(
					($("<p/>")).attr("id", "keywords" + id).css("visibility",
							"hidden"));
	$("#div" + id)
			.append(
					($("<p/>")).attr("id", "location" + id).css("visibility",
							"hidden"));

	$("#div" + id)
			.append(
					($("<p/>"))
							.attr("id", "abstract" + id)
							.css("visibility", "hidden")
							.html(
									"<b>Abstract: </b><pre style='background-color: #ffffff'></pre><a/>"));
	$("#div" + id).append(
			($("<p/>")).attr("id", "description" + id).css("visibility",
					"hidden"));

	$("#div" + id).append(
			($("<p/>")).attr("id", "contents" + id).append($("<a/>")));
}
