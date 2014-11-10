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
		url : "mmdb/discovery/GetPublishedCollections",
		dataType : "json",
		success : homePageJsonParser,
		error : homePageErrorParser
	});

}

function loadPublishedCollection(collectionId) {
	
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

	var jsonString = JSON.stringify(json);
	var obj = jQuery.parseJSON(jsonString);
	if (obj.sparql.results.result != null) {
		if (obj.sparql.results.result.length == null) {
			var jsonBinding = obj.sparql.results.result.binding;
			getBiblioBindingsForPage(jsonBinding);
		} else {
			for (var i = 0; i < obj.sparql.results.result.length; i++) {
				var jsonBinding = obj.sparql.results.result[i].binding;
				getBiblioBindingsForPage(jsonBinding);
			}
		}
	}
	// FixMe: No longer needed? (since NCED coll names no longer
	// have path info).
	if (title.indexOf("/") != -1) {
		title = title.substring(title.lastIndexOf("/") + 1);
	}
	if (!($("#collectionTitle" + id + ">a").length)) {
		$("#collectionTitle" + id).html(title);
	}
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

	if (creators.length != 0) {
		var creatorString = creators[0];
		var datacreatorString = creatornames[0];
		for (var i = 1; i < creators.length; i++) {
			creatorString += "," + creators[i];
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
			contactString += "," + contacts[i];
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
			descriptorString += " <a href='" + dataset_Path + descriptors[i]
					+ "'><img class='discoveryPreview' src='" + image_Path
					+ descriptors[i] + "' alt = 'Click to view' />" + "</a> ";

		}

		$("#description" + id).html(
				"<b>Descriptive Data: </b>" + descriptorString);
		$("#description" + id).css("visibility", "visible");
	}
}

function getBiblioBindingsForPage(jsonBinding) {
	if (jsonBinding.length == null) {
		getBiblioAttributesForPage(jsonBinding);
	} else {
		for (var i = 0; i < jsonBinding.length; i++) {
			getBiblioAttributesForPage(jsonBinding[i]);
		}
	}
}

function getBiblioAttributesForPage(jsonBinding) {

	$.each(jsonBinding, function(key, value) {
		if (value == 'creator' || value == 'contact') {
			var temp = jsonBinding['literal'];
			var name = temp.substring(0, temp.indexOf(':') - 1);
			var url = temp.substring(temp.indexOf(':') + 1);
			var html = "<a href='" + url + "' target=_blank>" + name + "</a> ";

			if (value == 'creator') {
				if (creators.indexOf(html) == -1) {
					creators.push(html);
					name = name.replace(",", "\\,\\");
					creatornames.push(name);
				}
			} else if (value == 'contact') {
				if (contacts.indexOf(html) == -1) {
					contacts.push(html);
					name = name.replace(",", "\\,\\");
					contactnames.push(name);
				}
			}
		} else if (value == 'abstract' || value == 'title'
				|| value == 'location') {
			var temp = jsonBinding['literal'];
			if (value == 'abstract') {
				if ((abstract.length > 0) && (abstract != temp)) {
					alert("Multiple abstracts");
				} else {
					abstract = temp;
				}
			} else if (value == "title") {
				if ((title.length > 0) && (title != temp)) {
					alert("Multiple titles");
				} else {
					title = temp;
				}
			} else {
				if (coll_location.indexOf(temp) == -1) {
					coll_location.push(temp);
				}
			}
		}

		else if (value == 'descriptor') {
			var tempDescriptor = jsonBinding['uri'];
			if (tempDescriptor != "undefined") {
				if (descriptors.indexOf(tempDescriptor) == -1) {
					descriptors.push(tempDescriptor);
				}
			}
		}

		else if (value == 'keyword') {
			var temp = jsonBinding['uri'];

			temp = temp.substring(temp.indexOf("#") + 1);

			temp = decodeURIComponent(temp);

			// replaceAll + from temp
			while (temp.indexOf("+") != -1) {
				temp = temp.replace('+', " ");
			}

			if (keywords.indexOf(temp) == -1)
				keywords.push(temp);
		}
	});
}

function createBlock(id, element) {
	$(element).append($("<div/>").attr("id", "coll" + id));
	$("#coll" + id).append($("<a/>").attr("name", id));

	$("#coll" + id).append(
			$("<div/>").attr("id", "title" + id).css('background-color',
					'#dfdfdf'));
	$("#title" + id).append(
			$("<table/>").append(
					$("<tbody/>").append(
							$("<tr/>").append(
									$("<td/>").append(
											$("<h1/>").attr("id",
													"collectionTitle" + id)
													.css("padding-top", "5px")
													.css("padding-left", "5px")
													.css("padding-bottom",
															"3px"))))));
	$("#title" + id + ">table>tbody>tr").append(
			$("<td/>").append(
					$("<a/>").attr("id", "acrlink" + id).append(
							$("<img/>").attr("src", "images/open_in_acr.png")
									.attr("title", "View Collection Page")
									.attr("alt", "View Collection Page").attr(
											"border", "0").css('margin-top',
											'-15px'))));
	$("#coll" + id).append(
			$("<div/>").attr("class", "well").attr("id", "div" + id));
	$("#div" + id).append(
			($("<p/>")).attr("id", "authors" + id).css("visibility", "hidden")
					.css("margin-top", "-5px"));
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
}
