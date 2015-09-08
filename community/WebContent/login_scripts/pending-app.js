var seadSpaces = {};

seadSpaces.doConfigAjax = function(url) {
	return $.ajax({
		type : "GET",
		timeout : '10000',
		url : url + "/resteasy/sys/config",
		dataType : "json"
	});
}

seadSpaces.doPendingAjax = function(url) {
	return $
			.ajax({
				type : "GET",
				timeout : '10000',
				url : url
						+ "/resteasy/collections/metadata/http%3A%2F%2Fsead-data.net%2Fterms%2FProposedForPublication/literal/true",
				dataType : "json"
			});
}

seadSpaces.buildPendingGrid = function(size, i, projectName,
		projectDescription, projectLogo, projectColor, projectBg, value,
		pendingRequests) {
	if (projectName == null) {
		projectName = value + ' is currently busy/offline';
	}

	var page = '';
	page += '<div class="span4">';
	page += '<div class="space-wrapper">';
	page += '<a href="' + value + '"><div class="fade-wrapper">';
	if (projectLogo || projectBg) {
		page += '<div class="fade-out" style="background-image:url('
				+ projectLogo + '),url(' + projectBg + ');">';
	} else {
		page += '<div class="fade-out">';
	}
	page += '</div>';
	if (projectDescription) {
		page += '<div class="fade-in"><div class="fade-in-content">'
				+ projectDescription.replace(/<(?:.|\n)*?>/gm, '')
				+ '</div></div>';
	}
	page += '</div></a>';
	page += '<div class="request">';
	page += '<h4><a href="' + value + '" class="name">' + projectName
			+ '</a></h4>';

	if (pendingRequests != null) {
		page += '<h5>Pending Requests</h5><ul>';

		$.each(pendingRequests, function() {
			page += '<li class="collection">' + '<a href=\"' + this.url + '\">'
					+ this.Collection + '</a></li>';
		});
		page += '</ul>';
	}

	page += '</div>';
	page += '</div>';
	page += '</div>';
	$('.project-spaces-dashboard .row-fluid').append(page);

	if (i == size) {
		$('#loading-spinner').remove();
	}
}

seadSpaces.getSpaces = function(url) {
	return $.get(url, function(spaces) {
	});
}

seadSpaces.init = function() {

	var i = 1;
	var spaces = '';

	$
			.when(
					seadSpaces
							.getSpaces("//sead.ncsa.illinois.edu/projects/spaces"))
			.done(
					function(spaces) {

						spaces = spaces.replace(/\"/g, '');
						spaces = spaces.replace('[', '');
						spaces = spaces.replace(']', '');
						spaces = spaces.split(',');

						var size = spaces.length;
						$
								.each(
										spaces,
										function(key, value) {
											$
													.when(
															seadSpaces
																	.doConfigAjax(value),
															seadSpaces
																	.doPendingAjax(value))
													.then(
															function(config,
																	requests) {
																var pendingRequests = [];
																$
																		.each(
																				requests[0],
																				function(
																						item) {

																					if (item != "@context") {
																						pendingRequests
																								.push({
																									"Collection" : requests[0][item].Title,
																									"url" : value
																											+ "#collection?uri="
																											+ requests[0][item].Identifier
																								});
																					}
																				});

																var projectName = config[0]["project.name"];
																var projectDescription = config[0]["project.description"];
																var projectLogo = config[0]["project.header.logo"];
																var projectColor = config[0]["project.header.title.color"];
																var projectBg = config[0]["project.header.background"];

																if (typeof projectBg !== 'undefined'
																		&& projectBg
																				.substring(
																						0,
																						8) == "resteasy") {
																	projectBg = value
																			+ '/'
																			+ projectBg;
																}
																if (typeof projectBg !== 'undefined'
																		&& projectBg
																				.substring(
																						0,
																						6) == "images") {
																	projectBg = value
																			+ '/'
																			+ projectBg;
																}
																if (typeof projectLogo !== 'undefined'
																	&& projectLogo
																			.substring(
																					0,
																					8) == "resteasy") {
																projectLogo = value
																		+ '/'
																		+ projectLogo;
																}
																if (typeof projectLogo !== 'undefined'
																		&& projectLogo
																				.substring(
																						0,
																						6) == "images") {
																	projectLogo = value
																			+ '/'
																			+ projectLogo;
																}

																seadSpaces
																		.buildPendingGrid(
																				size,
																				i,
																				projectName,
																				projectDescription,
																				projectLogo,
																				projectColor,
																				projectBg,
																				value,
																				pendingRequests);
																i++;
															})
													.fail(
															function(response) {

																seadSpaces
																		.buildPendingGrid(
																				size,
																				i,
																				null,
																				null,
																				null,
																				null,
																				null,
																				value,
																				null);
																i++;

															});

										});

					});

}

seadSpaces.init();