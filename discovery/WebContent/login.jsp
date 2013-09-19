<!DOCTYPE html>
<!--#include virtual="common.html" -->
<html lang="en">
<head>
<meta charset="utf-8">
<title id="home-title">Login</title>
<link href="login_css/bootstrap.css" rel="stylesheet">
<link href="login_css/common.css" rel="stylesheet">
<style type="text/css">

/* Override some defaults */
html,body {
	background-color: #eee;
}

body {
	padding-top: 40px;
}

.container {
	width: 300px;
	float: right;
	margin-top: 50px;
	margin-right: 7%;
	/* margin-right: 130px; */
}

/* The white background content wrapper */
.container>.content {
	background-color: #fff;
	padding: 20px;
	margin: 0 -20px;
	-webkit-border-radius: 10px 10px 10px 10px;
	-moz-border-radius: 10px 10px 10px 10px;
	border-radius: 10px 10px 10px 10px;
	-webkit-box-shadow: 0 1px 2px rgba(0, 0, 0, .15);
	-moz-box-shadow: 0 1px 2px rgba(0, 0, 0, .15);
	box-shadow: 0 1px 2px rgba(0, 0, 0, .15);
}

.login-form {
	margin-left: 65px;
}

legend {
	margin-right: -50px;
	font-weight: bold;
	color: #404040;
}

/* div#banner {
	position: absolute;
	background-image: url("img/bkgrnd_repeat_x.png");
	background-repeat: repeat-x;
	background-repeat-x: repeat;
	background-repeat-y: no-repeat;
	width: 100%;
	left: 0px;
	top: 0px;
}
 */
</style>

</head>

<script src="http://code.jquery.com/jquery-1.8.1.min.js"
	type="text/javascript"></script>
<script src="http://code.jquery.com/ui/1.9.1/jquery-ui.js"></script>
<script src="login_scripts/commons.js"></script>
	
<%
String project_info = (String)request.getAttribute("projectInfo"); 
%>

<script type="text/javascript">
    var projInfo = '<%=project_info%>';

	var query = '';
	$(function() {
	    loadProjectInfo(projInfo);
		$("#btnLogin").click(
				function() {

					$('#errorpanel').hide();
					//$("#loginForm").submit();
					var url = window.location.href;

					query = url.indexOf("?") == -1 ? '' : url.substring(url
							.indexOf("?") + 1);
					var userName = $('#txtUserName').val();
					var password = $('#txtPassword').val();

					$.ajax({
						type : "POST",
						url : "DoLogin",
						dataType : "json",
						data : "userName=" + userName + "&password=" + password
								+ "&remainingQuery=" + query,
						success : showNCEDCollection,
						error : redirectToErrorPage
					});
				});
	});

	function showNCEDCollection(json) {
		if (query == '') {
			window.location.replace("home");
		} else {
			window.location.replace("contents.html?" + query);
		}
	}
	
	function redirectToErrorPage(jqXHR, textStatus, errorThrown) {
		if (jqXHR.responseText == 'Unauthorized') {
			$('#errorpanel').show();
		} else {
			window.location.replace("error.html");
		}
	}

	
</script>
<body>

	<div id="banner">
		<map name="bannermap" id="bannermap">
			<area id="projectURL" href="http://sead-data.net" target="_blank" coords="0,0,600,134" shape="rect">
		</map>
		<img id="projectLogo" usemap="#bannermap" src="login_img/header-image.png"
			style="border: none;">
	</div>
	<!-- <img class="img-rounded" src='login_img/header-image.png'
		style='width: 4000px; margin-top: -3%;'></img> -->
	<div style='margin-top: 10%; margin-left: 2%; margin-right: 2%;'>
		<table>
			<tbody>
				<tr>
					<td id="projectDesc"><h4>The NCED Mission</h4>
						<p align="justify">
							<b>Understanding Landscape and Ecosystem Co-evolution</b>
						</p>
						<p align="justify">
							<strong>NCED</strong>&nbsp;(the National Center for Earth-surface
							Dynamics)&nbsp;is a <a
								style="color: rgb(39, 99, 140); text-decoration: none;"
								href="http://nsf.gov">National Science Foundation</a>&nbsp;(NSF)&nbsp;<a
								style="color: rgb(39, 99, 140); text-decoration: none;"
								href="http://www.nsf.gov/od/oia/programs/stc/">Science and
								Technology Center</a>&nbsp;(STC).&nbsp;We began operation&nbsp;in
							August 2002 and are headquartered at the&nbsp;<a
								style="color: rgb(39, 99, 140); text-decoration: none;"
								href="http://www.safl.umn.edu/">St. Anthony Falls
								Laboratory&nbsp;</a>(SAFL) at the University&nbsp;of Minnesota.
						</p>
						<p align="justify">
							<strong>NCED's mission is to predict the coupled
								dynamics and co-evolution of landscapes and their ecosystems in
								order to transform management and restoration of the
								Earth-surface environment.</strong>
						</p>
						<p align="justify">We pursue this mission by&nbsp;creating
							partnerships&nbsp;&amp;&nbsp;integrating researchers and
							practitioners&nbsp;from the physical, biological, and social
							sciences.&nbsp;</p>
						<p align="justify">
							<strong>Our research focuses on channel networks and
								their surroundings.</strong>&nbsp;We strive to answer one <strong>overarching
								question</strong>:
						</p>
						<p align="justify">
							<em>"How will the coupled system of physical, biological,
								geochemical, and human processes that shape the surface of the
								Earth respond to changes in climate, land use, environmental
								management, and other forcings?"</em>
						</p>
					</td>
					<td width='400px'>
						<div class="container">
							<div class="content">
								<div class="row">
									<div class="login-form">
										<h2>Login</h2>
										<!-- <FORM action="Login" method="post" id='loginForm'>
											<fieldset> -->
										<div class="clearfix">
											<input type="text" placeholder="Username" name="userName"
												id="txtUserName">
										</div>
										<div class="clearfix">
											<input type="password" placeholder="Password" name="password"
												id="txtPassword">
										</div>
										<div id='errorpanel' style='display:none'>
											<font color='red'>The user name or password is
												incorrect.</font>
										</div>
										<button class="btn primary" id="btnLogin">Sign in</button>
										<!-- </fieldset>
										</form> -->
									</div>
								</div>
							</div>
						</div> <!-- /container --></td>
				</tr>
			</tbody>
		</table>

	</div>
</body>
</html>