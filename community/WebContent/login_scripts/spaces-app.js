var seadSpaces = {};

seadSpaces.doInfoAjax = function(url, i) {
	return $.ajax({
		type : "GET",
		timeout : '10000',
		url : "GetSysInfo",
		data : {
			server : url
		},
		dataType : "json"
	});
}

seadSpaces.doConfigAjax = function(url) {
	return $.ajax({
		type : "GET",
		timeout : '10000',
		url : "GetProjectInfo",
		data : {
			server : url
		},
		dataType : "json"
	});
}

seadSpaces.abbreviateNumber = function(value) {
	var newValue = value;
	if (value >= 1000) {
		var suffixes = [ "", "k", "m", "b", "t" ];
		var suffixNum = Math.floor(("" + value).length / 3);
		var shortValue = '';
		for (var precision = 2; precision >= 1; precision--) {
			shortValue = parseFloat((suffixNum != 0 ? (value / Math.pow(1000,
					suffixNum)) : value).toPrecision(precision));
			var dotLessShortValue = (shortValue + '').replace(
					/[^a-zA-Z 0-9]+/g, '');
			if (dotLessShortValue.length <= 2) {
				break;
			}
		}
		if (shortValue % 1 != 0)
			shortNum = shortValue.toFixed(1);
		newValue = shortValue + suffixes[suffixNum];
	}
	return newValue;
}

seadSpaces.initSort = function() {
	var options = {
		valueNames : [ 'name', 'fade-in-content', 'published', 'views_raw',
				'collections_raw', 'teammates', 'datasets_raw', 'bytes_raw' ]
	};

	var spaceList = new List('project-spaces-dashboard', options);
	spaceList.sort('name', {
		order : "asc"
	});

	$('#views_raw').click(function() {
		$('.sort').removeClass('active');
		$(this).addClass('active');
		spaceList.sort('views_raw', {
			order : "desc"
		});

	});

	$('#teammates').click(function() {
		$('.sort').removeClass('active');
		$(this).addClass('active');
		spaceList.sort('teammates', {
			order : "desc"
		});

	});

	$('#bytes_raw').click(function() {
		$('.sort').removeClass('active');
		$(this).addClass('active');
		spaceList.sort('bytes_raw', {
			order : "desc"
		});

	});

	$('#name').click(function() {
		$('.sort').removeClass('active');
		$(this).addClass('active');
		spaceList.sort('name', {
			order : "asc"
		});

	});

	$('#collections_raw').click(function() {
		$('.sort').removeClass('active');
		$(this).addClass('active');
		spaceList.sort('collections_raw', {
			order : "desc"
		});

	});

	$('#datasets_raw').click(function() {
		$('.sort').removeClass('active');
		$(this).addClass('active');
		spaceList.sort('datasets_raw', {
			order : "desc"
		});

	});

	$('#filter-published').click(function() {
		spaceList.filter(function(item) {
			if (item.values().published > 0) {
				return true;

			} else {
				return false;
			}

		});
		$('.filter').removeClass('active');
		$(this).addClass('active');
		return false;
	});

	$('#reset-button').click(function() {
		$('#search-field').val('');
		spaceList.search();
		spaceList.sort('name', {
			order : "asc"
		});
		spaceList.filter();
		$('.filter').removeClass('active');
		$('.sort').removeClass('active');
	});

	$('#loading-spinner').remove();
}

seadSpaces.formatBytes = function(bytes, decimals) {
	if (bytes == 0)
		return '0 Byte';
	var k = 1000;
	var dm = decimals + 1 || 3;
	var sizes = [ 'Bytes', 'KB', 'MB', 'GB', 'TB', 'PB', 'EB', 'ZB', 'YB' ];
	var i = Math.floor(Math.log(bytes) / Math.log(k));
	return (bytes / Math.pow(k, i)).toPrecision(dm) + ' ' + sizes[i];
}

seadSpaces.buildGrid = function(size, i, projectName, projectDescription,
		projectLogo, projectColor, projectBg, datasets_display, datasets_raw,
		users, users_raw, views, views_raw, collections, collections_raw,
		published, bytes, value) {
	if (projectName == null) {
		projectName = value + ' is currently busy or offline';
	}

	if (bytes !== null) {
		if (bytes.indexOf('GB') > 0 || bytes.indexOf('bytes') > 0
				|| bytes.indexOf('MB') > 0 || bytes.indexOf('TB') > 0
				|| bytes.indexOf('KB') > 0) {
			// hide values that are not being served in bytes
			bytes = null;
		} else {
			var bytes_raw = bytes;
			bytes = seadSpaces.formatBytes(bytes, 2);
		}
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
	page += '<div class="space-stats">';
	page += '<h4><a href="' + value + '" class="name">' + projectName
			+ '</a></h4>';
	page += '<ul>';

	if (!views_raw) {
		views_raw = 0;
	}
	page += '<li class="views_raw">' + views_raw + '</li>';
	if (views) {
		page += '<li class="views" title="' + views
				+ ' views"><i class="fa fa-lg fa-eye"></i> ' + views + '</li>';
	}

	if (!users_raw) {
		users_raw = 0;
	}
	page += '<li class="users_raw">' + users_raw + '</li>';
	if (users) {
		page += '<li class="teammates" title="' + users
				+ ' contributors"><i class="fa fa-lg fa-user"></i> ' + users
				+ '</li>';
	}

	if (!collections_raw) {
		collections_raw = 0;
	}
	page += '<li class="collections_raw" title="' + collections_raw
			+ ' collections"> '
			+ collections_raw + '</li>';
	if (collections) {
		page += '<li class="collections" title="' + collections
				+ ' collections"><i class="fa fa-lg fa-folder"></i> '
				+ collections + '</li>';
	}

	if (!datasets_raw) {
		datasets_raw = 0;
	}
	page += '<li class="datasets_raw">' + datasets_raw + '</li>';
	if (datasets_display) {
		page += '<li class="datasets" title="' + datasets_display
				+ ' datasets"><i class="fa fa-lg fa-database"></i> '
				+ datasets_display + '</li>';
	}

	if (!bytes_raw) {
		bytes_raw = 0;
	}
	page += '<li class="bytes_raw">' + bytes_raw + '</li>';
	if (bytes) {
		page += '<li class="bytes" title="' + bytes
				+ '"><i class="fa fa-lg fa-hdd-o"></i> ' + bytes + '</li>';
	}

	if (published) {
		page += '<li title="'
				+ published
				+ ' published dataset"><i class="fa fa-lg fa-folder-open"></i> <span class="published">'
				+ published + '</span></li>';
	}
	page += '<ul>';
	page += '</div>';
	page += '</div>';
	page += '</div>';
	$('.project-spaces-dashboard .row-fluid').append(page);

	if (i == size) {
		seadSpaces.initSort();
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
							.getSpaces("https://sead.ncsa.illinois.edu/projects/spaces"))
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
											if(value == 'https://sead-open.ncsa.illinois.edu/acr'||value == 'https://sead-demo.ncsa.illinois.edu/acr'){i++;return;} // hide SEAD open and SEAD Demo
											$
													.when(
															seadSpaces
																	.doConfigAjax(value),
															seadSpaces
																	.doInfoAjax(value))
													.then(
															function(config,
																	info) {

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

																var bytes = info[0]["Total number of bytes"];
																var datasets_display = seadSpaces
																		.abbreviateNumber(info[0]["Datasets"]);
																var datasets_raw = info[0]["Datasets"];
																var users = seadSpaces
																		.abbreviateNumber(info[0]["Number of Users"]);
																var users_raw = info[0]["Number of Users"];
																var views = seadSpaces
																		.abbreviateNumber(info[0]["Total Views"]);
																var views_raw = info[0]["Total Views"];
																var collections = seadSpaces
																		.abbreviateNumber(info[0]["Collections"]);
																var collections_raw = info[0]["Collections"];
																var published = seadSpaces
																		.abbreviateNumber(info[0]["Published Collections"]);
																seadSpaces
																		.buildGrid(
																				size,
																				i,
																				projectName,
																				projectDescription,
																				projectLogo,
																				projectColor,
																				projectBg,
																				datasets_display,
																				datasets_raw,
																				users,
																				users_raw,
																				views,
																				views_raw,
																				collections,
																				collections_raw,
																				published,
																				bytes,
																				value);
																i++;
															})
													.fail(
															function(response) {
																seadSpaces
																		.buildGrid(
																				size,
																				i,
																				null,
																				null,
																				null,
																				null,
																				null,
																				null,
																				null,
																				null,
																				null,
																				null,
																				null,
																				null,
																				null,
																				null,
																				null,
																				value);
																i++;
															});

										});

					});

}

seadSpaces.demoSpace = function(){

    var value = 'https://sead-demo.ncsa.illinois.edu/acr';
	$
			.when(
					seadSpaces
							.doConfigAjax(value),
					seadSpaces
							.doInfoAjax(value))
			.then(
					function(config,
							info) {

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
												6) == "images") {
							projectLogo = value
									+ '/'
									+ projectLogo;
						}

						var bytes = info[0]["Total number of bytes"];
						var datasets_display = seadSpaces
								.abbreviateNumber(info[0]["Datasets"]);
						var datasets_raw = info[0]["Datasets"];
						var users = seadSpaces
								.abbreviateNumber(info[0]["Number of Users"]);
						var users_raw = info[0]["Number of Users"];
						var views = seadSpaces
								.abbreviateNumber(info[0]["Total Views"]);
						var views_raw = info[0]["Total Views"];
						var collections = seadSpaces
								.abbreviateNumber(info[0]["Collections"]);
						var collections_raw = info[0]["Collections"];
						var published = seadSpaces
								.abbreviateNumber(info[0]["Published Collections"]);
						
						
					
						if (projectName == null) {
							projectName = value + ' is currently busy or offline';
						}

						if (bytes !== null) {
							if (bytes.indexOf('GB') > 0 || bytes.indexOf('bytes') > 0
									|| bytes.indexOf('MB') > 0 || bytes.indexOf('TB') > 0
									|| bytes.indexOf('KB') > 0) {
								// hide values that are not being served in bytes
								bytes = null;
							} else {
								var bytes_raw = bytes;
								bytes = seadSpaces.formatBytes(bytes, 2);
							}
						}

						var page = '';
					
						page += '<div class="space-wrapper">';
						page += '<a href="' + value + '"><div class="fade-wrapper">';
						
						if (projectLogo || projectBg) {
							page += '<div class="fade-out" style="background-image:url(' + projectBg + ');">';
						} else {
							page += '<div class="fade-out">';
						}
						page += '<div class="space-stats"><div class="space-name"><img src="' + projectLogo + '" alt="" />' + projectName + '</div></div>';
						page += '</div>';
						
						if (projectDescription) {
							page += '<div class="fade-in"><div class="fade-in-content">'
									+ projectDescription.replace(/<(?:.|\n)*?>/gm, '')
									+ '</div></div>';
						}
						page += '</div></a>';

						page += '<ul>';

						if (!views_raw) {
							views_raw = 0;
						}
						page += '<li class="views_raw">' + views_raw + '</li>';
						if (views) {
							page += '<li class="views" title="' + views
									+ ' views"><i class="fa fa-lg fa-eye"></i> ' + views + '</li>';
						}

						if (!users_raw) {
							users_raw = 0;
						}
						page += '<li class="users_raw">' + users_raw + '</li>';
						if (users) {
							page += '<li class="teammates" title="' + users
									+ ' contributors"><i class="fa fa-lg fa-user"></i> ' + users
									+ '</li>';
						}

						if (!collections_raw) {
							collections_raw = 0;
						}
						page += '<li class="collections_raw" title="' + collections_raw
								+ ' collections"> '
								+ collections_raw + '</li>';
						if (collections) {
							page += '<li class="collections" title="' + collections
									+ ' collections"><i class="fa fa-lg fa-folder"></i> '
									+ collections + '</li>';
						}

						if (!datasets_raw) {
							datasets_raw = 0;
						}
						page += '<li class="datasets_raw">' + datasets_raw + '</li>';
						if (datasets_display) {
							page += '<li class="datasets" title="' + datasets_display
									+ ' datasets"><i class="fa fa-lg fa-database"></i> '
									+ datasets_display + '</li>';
						}

						if (!bytes_raw) {
							bytes_raw = 0;
						}
						page += '<li class="bytes_raw">' + bytes_raw + '</li>';
						if (bytes) {
							page += '<li class="bytes" title="' + bytes
									+ '"><i class="fa fa-lg fa-hdd-o"></i> ' + bytes + '</li>';
						}

						if (published) {
							page += '<li title="'
									+ published
									+ ' published dataset"><i class="fa fa-lg fa-folder-open"></i> <span class="published">'
									+ published + '</span></li>';
						}
						page += '<ul>';
						page += '</div>';
						page += '</div>';

					$('.sead-demo').append(page);

					})
			.fail(
					function(response) {
						
					});

}

seadSpaces.init();
seadSpaces.demoSpace();