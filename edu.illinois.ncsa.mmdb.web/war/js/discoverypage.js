var div_html = '';
var title = '';
var creator = '';
var contact = '';
var keyword = '';

var ft; // Filtrify

function homePageJsonParser(json) {

	var uri = '';
	var abs = '';
	var displayTitle = '';
	var jsonString = JSON.stringify(json);
	var obj = jQuery.parseJSON(jsonString);
	var publishedColls = Object.keys(obj);
	if (publishedColls.length == 0) {
				// No collections to display
				div_html += "<div class='well'><h4 style='margin-top:-10px;' class='page-header'>"
						+ "<h4>No Published Collections to Display</h4>"
						+ "<div><p>Collections are considered published when their "
						+ "<i>Publication Date</i> attribute is set. This occurs automatically "
						+ "when data is published through the SEAD Virtual Archive. For more "
						+ "information, see the documentation at <a href = 'http://sead-data.net'>"
						+ "http://sead-data.net</a></p>" + "</div></div>";
				$("#home-loading").hide();
				$("#xmlBody").html(div_html);
				return;
	}

	for (var i = 0; i < publishedColls.length; i++) {
	if(publishedColls[i] != '@context') {
			writeCollection(i, json, obj[publishedColls[i]], true);
}
	}
	$("#home-loading").hide();
	ft = $.filtrify("xmlBody", "facetedSearch", {
		close : true,
		callback : function(query, match, mismatch) {

			if (!mismatch.length) {
				$("#legend").html("<i>Viewing all collections.</i>");
				$("div#reset").hide();
			} else {
				$("div#reset").show();
				var category, tags, i, tag, legend = "<h4>Viewing:</h4>";
				for (category in query) {
					tags = query[category];
					if (tags.length) {
						legend += "<p><span>" + category + ":</span>";
						for (i = 0; i < tags.length; i++) {
							tag = tags[i];
							legend += "<em>" + tag + "</em>";
						}
						legend += "</p>";
					}
					;
				}
				;
				legend += "<p><i>" + match.length + " collection"
						+ (match.length !== 1 ? "s" : "") + " found.</i></p>";
				$("#legend").html(legend);
			}
			;
		}
	});

	$("div#reset span").click(function() {
		filterreset();
	});
	// $("#xmlBody").append(($("<div/>")).html(div_html));
}

function writeCollection(id, json, pub, topLevel) {
	var uri = pub.Identifier;
	var abs = pub.Abstract;
	var displayTitle = pub.Title;
	createBlock(id, "#xmlBody");

	$("#collectionTitle" + id + ">div").html(displayTitle);
	if (abs) {
			var summary = abs.substring(0, 750);

			if (abs.length > 750) {
				summary += "...";
				$("#abstract" + id + ">a").attr("href", "#discovery_" + uri)
						.html("more ...");
			}
			$("#abstract" + id + ">pre").html(summary);
			$("#abstract" + id).css("visibility", "visible");
			
		}
		if(topLevel==true) {
		$("#contents" + id + ">a").attr("href", "#discovery_" + uri)
			.html("View Contents Listing ...");
		} else {
			$("#contents" + id + ">a").hide();
		}

				pageBiblioJsonParser(id, pub);
}

function filterreset() {
	ft.reset();
}