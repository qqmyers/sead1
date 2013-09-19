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


<script src="http://code.jquery.com/jquery-1.8.1.min.js"
	type="text/javascript"></script>
<script src="http://code.jquery.com/ui/1.9.1/jquery-ui.js"></script>
<script src="login_scripts/commons.js"></script>
<script src="login_scripts/json2.js"></script>
<script src="login_scripts/contentspage.js"></script>

<%
String project_info = (String)request.getAttribute("projectInfo"); 
%>

<script type="text/javascript">
  var projInfo = '<%=project_info%>';
  var medici_URL = null;

	$(function() {
		loadProjectInfo(projInfo);
		medici_URL = "<%= request.getAttribute("medici") %>";
		var url = window.location.href;
		tagID = url.substring(url.indexOf("?") + 3, url.length);
		
		$("#contents-loading").show();

		createBlock(0,"#xmlBody");
		var acrLink = medici_URL+ collection_Path + tagID;
		$("#acrlink0").attr("href", acrLink);
		

		$.ajax({
			type : "GET",
			url : "GetContents",
			dataType : "json",
			data : "tagID=" + tagID + "&title=" + title,
			success : contentsPageJsonParser,
			error : contentsPageErrorParser
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
		<img id="projectLogo" usemap="#bannermap" src="login_img/header-image.png"
			style="border: none;"  height="135px">
	</div>
	<div id="contents-loading"
		style="width: 300px; margin-left: auto; margin-right: auto; margin-top: 200px; display: none">
		<img src="login_img/loading.gif"></img>
	</div>
	<div
		style="width: auto; margin-top: 150px; margin-left: 100px; margin-right: 100px; visibility:hidden;"
		id="xmlBody" >
		
	</div>
</body>
</html>