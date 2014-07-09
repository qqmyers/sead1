<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title id="home-title">Contents</title>
</head>
<link href="login_css/bootstrap.css" rel="stylesheet">
<link href="login_css/common.css" rel="stylesheet">
<style type="text/css">
p {
	margin-bottom: 0px;
}
</style>


<script src="//code.jquery.com/jquery-1.8.1.min.js"
	type="text/javascript"></script>
<script src="//code.jquery.com/ui/1.9.1/jquery-ui.js"></script>
<script src="login_scripts/commons.js"></script>
<script src="login_scripts/json2.js"></script>
<script src="login_scripts/contentspage.js"></script>

<%
	String project_info = (String) request.getAttribute("projectInfo");
	String status_code = (String) request.getAttribute("statusCode");
%>

<script type="text/javascript">
  var projInfo = '<%=project_info%>';
  var medici = null;

	$(function() {
		loadProjectInfo(projInfo);
		medici = "<%=request.getAttribute("medici")%>";
		var url = window.location.href;
		tagID = url.substring(url.indexOf("?") + 3, url.length);

		$("#contents-loading").show();

		createBlock(0, "#xmlBody");
		var acrLink = medici + collection_Path + tagID;
		$("#acrlink0").attr("href", acrLink);

		$.ajax({
			type : "GET",
			url : "GetContents",
			dataType : "json",
			cache : false,
			data : "tagID=" + tagID + "&title=" + title,
			success : contentsPageJsonParser,
			error : contentsPageErrorParser
		});
		if('<%=request.getAttribute("isAnonymous")%>'=='true') {
			$("#btnLogout").css('visibility','hidden');
		}
		$("#btnLogout").click(function() {
			SSOLogout(); 
			window.location.replace("DoLogout");
		});

	});

	function contentsPageErrorParser(jqXHR, textStatus, errorThrown) {
		if (jqXHR.responseText == 'Unauthorized') {
			var currentURL = window.location.href;
			query = currentURL.substring(currentURL.indexOf("?"));
			window.location.replace("login" + query);
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
	</div>
	<div style='height: 150px;'></div>
	<div style='float: right;'>
		<button class="btn primary" id="btnLogout" style='margin-right: 10px;'>Logout</button>
	</div>
	<div style='margin-top: 0px;' class="page-header">
		<h1 style="margin-left: 20px;" id="projectCollections">Collections</h1>
	<div id="contents-loading"
		style="float:right; width: 120px; margin-left: auto; margin-right: 10px; margin-top: -20px; display: none">
		<img src="login_img/loading.gif"></img>
	</div></div>

<div class="row-fluid">
	<div class="span3">
	<div class="well" style="margin-left:20px;background-color:#DFDFDF;">
	<h3>Viewing: <i><span id="collname"></span></i></h3>

<div style="color:blue;margin-right:10px;margin-top:10px;"><a  id="returnanchor" href = "home">Return to Repository Home</a></div>

</div>
<div class="well" style="margin-left:20px;background-color:#DFDFDF;">
<h4>About:</h4>
<div id="projectDesc"></div>
	</div>
</div>
	<div class="span8" id="xmlBody"></div>
</body>
</html>