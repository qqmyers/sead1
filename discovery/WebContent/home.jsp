<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title id="home-title">Public Data Repository</title>
</head>
<link href="login_css/bootstrap.css" rel="stylesheet">
<link href="login_css/common.css" rel="stylesheet">
<script src="http://code.jquery.com/jquery-1.8.1.min.js"
	type="text/javascript"></script>
<script src="http://code.jquery.com/jquery-latest.js"></script>
<script src="login_scripts/commons.js"></script>
<script src="login_scripts/json2.js"></script>
<script src="login_scripts/homepage.js"></script>
<script src="login_scripts/jquery-cookie.js"></script>
<script src="login_scripts/filtrify.sead.min.js"></script>
<link rel="stylesheet" href="login_css/filtrify.sead.css">

<%
	String project_info = (String) request.getAttribute("projectInfo");
%>

<script type="text/javascript">
  var projInfo = '<%=project_info%>';
  var medici = null;

	$(function() {
		$("#home-loading").show();
		loadProjectInfo(projInfo);
		medici = '<%=request.getAttribute("medici")%>';

		$.ajax({
			type : "GET",
			url : "GetPublishedCollections",
			dataType : "json",
			success : homePageJsonParser,
			error : homePageErrorParser
		});
		if('<%=request.getAttribute("isAnonymous")%>'=='true') {
			$("#btnLogout").css('visibility','hidden');
		}	
		$("#btnLogout").click(function() {
			SSOLogout(); 
			window.location.replace("DoLogout");
		});

		$("div#reset span").click(function() {
			filterreset();
		});
	});

	function homePageErrorParser(jqXHR, textStatus, errorThrown) {
		if (jqXHR.responseText == 'Unauthorized') {
			window.location.replace("login");
		} else {
			window.location.replace("error.html");
		}
	}

</script>

<body>
	<div id="banner">
		<map name="bannermap" id="bannermap">
			<area id="projectURL" href="http://sead-data.net" target="_blank"
				coords="0,0,600,134" shape="rect">
		</map>
		<img id="projectLogo" usemap="#bannermap"
			src="login_img/header-image.png" style="border: none;" height="135px">
	</div>
	<div style='height: 150px;'></div>
	<div style='float: right;'>
		<button class="btn primary" id="btnLogout" style='margin-right: 10px;'>Logout</button>
	</div>
	<div style='margin-top: 0px;' class="page-header">
		<h1 style="margin-left: 20px;" id="projectCollections">Collections</h1>
	<div id="home-loading"
		style="float:right; width: 120px; margin-left: auto; margin-right: 10px; margin-top: -20px; display: none">
		<img src="login_img/loading.gif"></img>
	</div></div>
	
	<div class="row-fluid">
	<div class="span3">
	<div class="well" style="margin-left:20px;background-color:#DFDFDF;">
	<h3>Search By</h3>
	<div id="facetedSearch" class="well" style="margin-left:20px"></div>
<div id="reset" style="display:none;float:right;color:blue;margin-right:10px;margin-top:10px;"><span>Reset Filters</span></div>
	<div id="legend" class="well" style="margin-left:20px"><i>Viewing all collections.</i></div>
</div>
<div class="well" style="margin-left:20px;background-color:#DFDFDF;">
<h4>About:</h4>
<div id="projectDesc"></div>
	</div>
</div>
	<div

		id="xmlBody" class="span8"></div>
	</div>
</body>
</html>
