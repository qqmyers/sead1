<%@ page import="edu.illinois.ncsa.mmdb.web.common.ConfigurationKey.*" %>

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
			src="<%=ConfigurationKey.getConfigurationKey("discovery_banner")%>" style="border: none;" height="135px">
	</div>
	<div style='height: 150px;'></div>
	<div style='float: right;'>
		<button class="btn primary" id="btnLogout" style='margin-right: 10px;'>Logout</button>
	</div>
	<div style='margin-top: 0px;' class="page-header">
		<h1 style="margin-left: 20px;" id="projectCollections">Collections</h1>
	</div>
	<div id="home-loading"
		style="width: 300px; margin-left: auto; margin-right: auto; margin-top: 0px; display: none">
		<img src="login_img/loading.gif"></img>
	</div>
	<div
		style="width: auto; margin-top: 50px; margin-left: 100px; margin-right: 100px;"
		id="xmlBody"></div>
</body>
</html>
