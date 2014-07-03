<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>SEAD Active Repositories</title>
</head>
<link href="login_css/bootstrap.css" rel="stylesheet">
<link href="login_css/common.css" rel="stylesheet">
<link href="login_img/icons/favicon.ico" type="image/ico" rel="Shortcut Icon"></link>

<script src="//code.jquery.com/jquery-1.8.1.min.js"
	type="text/javascript"></script>
<script src="//code.jquery.com/jquery-latest.js"></script>
<script src="login_scripts/commons.js"></script>
<script src="login_scripts/json2.js"></script>
<script src="login_scripts/homepage.js"></script>
<script src="login_scripts/jquery-cookie.js"></script>
<script src="login_scripts/jquery.tablesorter.min.js"></script>
<script src="login_scripts/table2CSV.js"></script>

<%
	String projects = (String) request.getAttribute("projects");
%>

<script type="text/javascript">
  var projects = '<%=projects%>';
  var medici = null;
var serversLeft=null;
var statsLeft=null;

	$(function() {

		$("#home-loading").show();
		var div_html="";

		serversLeft = ${projects}.length;
		statsLeft=serversLeft;
		for (var i=0; i<${projects}.length; i++) {
			var curServer = ${projects}[i];
			addRow(i);

			$.ajax({
				type : "GET",
				url : "GetProjectInfo",
				data: {server:  curServer},
				dataType : "json",
				success : projInfoJsonParser,
				error : projInfoErrorParser
			});

			$.ajax({
				type : "GET",
				url : "GetSysInfo",
				data: {server:  curServer},
				dataType : "json",
				success : sysInfoJsonParser,
				error : sysInfoErrorParser
			});
		}
	});

	
	function addRow(rowId) {
		$("#projects>tbody").append("<tr  id=\"" + rowId + "\"><td/><td/><td/><td/><td/><td/><td/><td/><td/><td/></tr>");
	}

	function checkDone() {

		if((serversLeft==0)&&(statsLeft==0)) {
			$("#home-loading").hide();
			$("#projects").tablesorter();
		}
	}

	
	function projInfoJsonParser(json) {
		var current = decodeURIComponent(this.url.substr(this.url.indexOf("http")));
		var index = jQuery.inArray(current, ${projects});
		 $("#" + index + ">td:eq(0)").html("<a href=\"" + current + "/..\">" + json['project.name'] + "</a>");
		 $("#" + index + ">td:eq(1)").html("<div>" +  json['project.description']  + "</div>");
		 $("#" + index + ">td:eq(9)").html( "<a href=\"" + json['project.url'] + "\">" + json['project.url'] + "</a>");
		serversLeft = serversLeft-1;
		checkDone();
	}
	
	function sysInfoJsonParser(json) {

		var jstring = JSON.stringify(json);
		var current = decodeURIComponent(this.url.substr(this.url.indexOf("http")));
		var index = jQuery.inArray(current, ${projects});
	 
		 $("#" + index + ">td:eq(2)").html("<a href=\"" + current + "/../discovery\">" + json["Public Preprint Collections"] + "</a>");
		 $("#" + index + ">td:eq(3)").html("<a href=\"" + current + "/../discovery\">" + json["Published Collections"] + "</a>");
		 $("#" + index + ">td:eq(4)").html(json["Total Views"] );
		 $("#" + index + ">td:eq(5)").html(json["Number of Users"] );
		 $("#" + index + ">td:eq(6)").html(json["Collections "]);
		 $("#" + index + ">td:eq(7)").html( json.Datasets);
		 $("#" + index + ">td:eq(8)").html(json["Bytes from uploaded dataset"]);

		statsLeft = statsLeft-1;
		checkDone();
	}


	function projInfoErrorParser(jqXHR, textStatus, errorThrown) {
		var current = decodeURIComponent(this.url.substr(this.url.indexOf("http")));
		var index = jQuery.inArray(current, ${projects});
		 $("#" + index + ">td:eq(0)").html("<a href=\"" + current + "/..\">" + current + "</a>");
		 $("#" + index + ">td:eq(1)").css("color", "#880000").html( "Unable to contact server to retrieve information" );
		serversLeft = serversLeft-1;
		checkDone();
	}

	function sysInfoErrorParser(jqXHR, textStatus, errorThrown) {
		var current = decodeURIComponent(this.url.substr(this.url.indexOf("http")));
		var index = jQuery.inArray(current, ${projects});
		 $("#" + index + ">td:eq(2)").css("color", "#880000").html( "Unable to retrieve current stats" );
		statsLeft = statsLeft-1;
		checkDone();

	}

	function getCSVData(){
		var csv_value=$('#projects').table2CSV({delivery:'value'});
		 $("#csv_text").val(csv_value);	
		$("#exportForm").submit();
	}

</script>

<body>
	<div id="banner">
		<map name="bannermap" id="bannermap">
			<area id="projectURL" href="http://sead-data.net" target="_blank"
				coords="0,0,600,84" shape="rect">
		</map>
		<img id="projectLogo" usemap="#bannermap"
			src="login_img/header-image.png" >
	</div>
	<div style='height: 100px;'></div>
	<div style='margin-top: 0px;margin-left: 50px; margin-right: 50px;' class="page-header">
		<h1>SEAD Active Repositories</h1>
	<p>Welcome to SEAD Central where you can see how different groups are using SEAD and see whether they are allowing early access to data 

collections (Preprint Collections) and if they've published data (Published Collections - which have DOI's and long-term accessibility). Click on the links in the 

table below to go to a given project's data collections or head to <a href = "http://seadva.d2i.indiana.edu:8181/sead-access/">SEAD's Virtual Archive</a> to 

search globally across the collections preserved through SEAD. Or go to <a href="http://vivo-vis-test.slis.indiana.edu/vivo/">SEAD's Researcher Profile 

service</a> and view researcher's data citations.</p>
<p>If you have data to archive, check out the SEAD Open repository below, or <a href = 'mailto:SEADdatanet@umich.edu.'>contact us (SEADdatanet@umich.edu)</a> about setting up a space for your group.</p>

	</div>
	<div id="home-loading"
		style='float: right; width: auto; margin-left: auto; margin-right: 50px; margin-top: -15px; display: none' >

		Loading ...&nbsp;&nbsp;<img src="login_img/loading.gif"></img>
	</div>

	<div

		id="xmlBody"><table id='projects' class='tablesorter'>
			<thead><tr>
				<th>Group Repository</th>
				<th>Description</th>
				<th>Preprint Collections</th>
				<th>Published Collections</th>
				<th>Activity Level (Data Page Views)</th>
				<th>Group Members</th>
				<th>Total Collections</th>
				<th>Total Datasets</th>
				<th>Total Data Size</th>
				<th>Group Home Page</th>
			</tr></thead>
<tbody></tbody>
		</table>
<form action="export" method ="post"  id="exportForm"> 
<input type="hidden" name="csv_text" id="csv_text">
<input type="submit" value="Export as CSV" 
       onclick="getCSVData()"
</form>

	</div>
</body>
</html>