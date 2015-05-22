var theSpaceUrl;
var theJqscript;
var theImportcss;
var theBootcss;

function safeId(myid) {
	return myid.replace(/(:|\.|\[|\]|\/|\s|#|,|\?|=|@)/g, "");
}

function captureLinks() {

	for ( var ln = 0; ln < document.links.length; ln++) {
		var lk = document.links[ln];
		var link = lk.toString();
		var name = lk.text;

		if (!lk.toString().indexOf('javascript')) {
			link = '';
		}

		(function(num, theLink, theName, theLinkText) {
			$(theLink)
					.on(
							'click.sead',
							function() {
								var row = '<td>' + theName + '</td><td>';
								var id = safeId(num + theName);
								if

								(theLinkText.length == 0) {
									row = row + 'Link is a function</td><td>'
											+ 'Unable to follow Link';
								} else {
									row = row
											+ theLinkText
											+ '</td><td><button class="btn-primary" id="'
											+ id + '">Import...</button></td>';
								}
								$('<tr/>').addClass('warning').appendTo(
										$('#datalist')).html(row);
								$('#' + id).on('click', {
									id : id,
									url : theLinkText
								},

								sendIt);
								$('html, body').animate({
									scrollTop : 0
								}, 'fast');
								return false;
							});
		})(ln, lk, name, link);

	}
}

function releaseLinks() {
	for ( var lnum = 0; lnum < document.links.length; lnum++) {
		var lnk = document.links[lnum];
		$(lnk).off('click.sead');
	}
}

function cleanup() {
	releaseLinks();
	$('#sead-panel').remove();
	$(theBootcss).remove();
	$(theImportcss).remove();
	$(theJqscript).remove();
	$.noConflict();
}

function singleMe() {
	releaseLinks();
	$('#multimessage').hide();
	$('#datalist>tbody>tr').remove();
	$('#datalist').append('<tr/>');

	var homepage = safeId(location.href.toString());
	$('#datalist>tbody>tr').html(
			'<td>This Page:' + document.title + '</td><td>' + location.href
					+ '</td><td><button id="' + homepage +

					'">Import...</button></td>').addClass('warning');

	$('#' + homepage).on('click', {
		id : homepage,
		url : location.href
	}, sendIt);
}

function multiMe() {
	$('#multimessage').show();
	$('#datalist>tbody>tr').remove();
	captureLinks();

}

function createPanel(spaceUrl, spaceTitle, jqscript, importcss, bootcss) {
	theSpaceUrl = spaceUrl;
	theJqscript = jqscript;
	theImportcss = importcss;
	theBootcss = bootcss;

	$('<div/>').prependTo($('body')).attr('id', 'sead-panel').addClass(
			'sead-scope');

	$('#sead-panel')
			.html(
					'<div class="row-fluid"><div class="span4"><div class="row-fluid"><a href="http://sead-data.net/"><img class="span5" title="SEAD Logo" alt="SEAD" style="width: 400px;" src="https://sead.ncsa.illinois.edu/images/header-image.png"></a></div><form id="dataoption"><div class="addurl"><label class="radio" for="singledata"><input name="dtype" id="singledata" type="radio" value="singleData" >Add this URL as a dataset</label></div><div class="addlink"><label class="radio" for="multipledata"><input name="dtype" id="multipledata" type="radio" value="multiple" >Add a link(s) on this page as a dataset</label></div></form><span class="label label-info" id="multimessage">*Click link(s) in the page below to add them to the import list</span></div><div class="span7"><div class="sead-space badge span7"><a href="'
							+ spaceUrl
							+ '">You are at: '
							+ spaceTitle
							+ '</a></div><div class="table-label">Selected Import Links:</div><table class="table table-condensed table-bordered" width=100% id="datalist"><thead><th>Label</th><th>URL</th><th>SEAD ID</th></thead></table></div><div class="span1"><button style="float: right;" class="btn close" id="quit">X</button></div></div>');

	$('#multimessage').hide();
	$('#singledata').change(singleMe);
	$('#multipledata').change(multiMe);
	$('#multipledata').click();
	$('#quit').click(cleanup);
	$('html, body').animate({
		scrollTop : 0
	}, 'fast');
}

function sendIt(event) {
	$('<label/>').appendTo($('#' + event.data.id).parent()).attr('id',
			'w' + event.data.id).html("Importing...");
	$('#' + event.data.id).remove();
	$.ajax({
		type : 'POST',
		id : 'w' + event.data.id,
		url : theSpaceUrl + 'resteasy/datasets/import/'
				+ encodeURIComponent(event.data.url),
		xhrFields : {
			withCredentials : true
		},
		dataType : 'json',
		success : function(data, textStatus, jqXHR) {
			successResponse(this.id, data, textStatus, jqXHR);
		},
		error : function(jqXHR, textStatus, errorThrown) {
			ErrorParser(this.id, jqXHR, textStatus, errorThrown);
		}
	});
}

function successResponse(id, data, textStatus, jqXHR) {
	var obj = jQuery.parseJSON(JSON.stringify(data));
	var tagId = obj["Success"].substring(23);
	;

	$('#' + id).parent().parent().addClass('success').removeClass('warning');
	$('<label/>').appendTo($('#' + id).parent()).attr('id', 's' + id).html(
			"Importing to: <a href=\'" + theSpaceUrl + "#dataset?id=" + tagId
					+ "\'>" + tagId + "</a>");
	$('#' + id).remove();
}

function ErrorParser(seadid, jqXHR, textStatus, errorThrown) {
	$('#' + seadid).parent().parent().addClass('error').removeClass('warning');
	$('<label/>').appendTo($('#' + seadid).parent()).attr('id', 'e' + seadid)
			.html("Failed..." + jqXHR.status + ": " + errorThrown);
	$('#' + seadid).remove();
}
