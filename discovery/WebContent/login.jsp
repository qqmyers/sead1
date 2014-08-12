<!DOCTYPE html>
<!--#include virtual="common.html" -->
<html lang="en">
<head>
<meta charset="utf-8">
<title id="home-title">ACR Discovery Login</title>
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

<script src="//code.jquery.com/jquery-1.8.1.min.js"
	type="text/javascript"></script>
<script src="//code.jquery.com/ui/1.9.1/jquery-ui.js"></script>
<script src="login_scripts/commons.js"></script>

<%
	String project_info = (String) request.getAttribute("projectInfo");
	String status_code = (String) request.getAttribute("statusCode");
	Boolean isAnonymous = (Boolean) request.getAttribute("isAnonymous");
	String medici = (String) request.getAttribute("medici");
	String googleClientId=(String)request.getAttribute("googleClientId");
%>


<script type="text/javascript">
    var projInfo   = '<%=project_info%>';
    var authStatus = '<%=status_code%>';
    var anon = '<%=isAnonymous%>';
    var medici = '<%=medici%>';
   	var googleClientId = '<%=googleClientId%>';
   	var userName = "";
	var password="";
	var googleAccessToken = "";
	var query = '';
	

	$(function() {

		$( "#txtUserName" ).focus();
		
		$( "#txtUserName" ).keyup(function(event) {
			if( event.which == 13) {
				$( "#txtPassword" ).focus();
			}
		});

		$( "#txtPassword" ).keyup(function(event) {
			if( event.which == 13) {
				$("#btnLogin").click();
			}
		});

		loadProjectInfo(projInfo);
		if ((authStatus == '403') && (anon == 'true')) {
			$("#forbiddenpanel").show();
		}
		$("#btnLogin").click( function() {
			localLogin(false);
		});
		$("#btnGoogle").click(function() {
			
			gapi.auth.authorize({
				client_id: googleClientId,
				scope: 'https://www.googleapis.com/auth/userinfo.email https://www.googleapis.com/auth/userinfo.profile',
			 	access_type: 'online',
   				immediate: 'false',
				authuser: -1
				}, function(authResult) {
					if(authResult && ! authResult.error) {
						googleAccessToken = authResult.access_token;
						localLogin(true);
					} 
				});
		});
	});
	
	

	function OnLoadCallback() { 
		gapi.auth.init(null);
		//Could see if the user is authenticated with google already here...
	}	

	function localLogin(usingGoogle) {
		$('#errorpanel').hide();
		var url = window.location.href;

		query = url.indexOf("?") == -1 ? '' : url.substring(url.indexOf("?") + 1);
		var data='';
		if(!usingGoogle) {
			userName = $('#txtUserName').val();
			password = $('#txtPassword').val();
			data = "userName=" + userName + "&password=" + password + "&remainingQuery=" + query;
		} else {
			data="googleAccessToken=" + googleAccessToken + "&remainingQuery=" + query;
		}

		$.ajax({
			type : "POST",
			url : "DoLogin",
			dataType : "json",
			data : data,
			success : loginToRemoteServer,
			error : redirectToErrorPage
		});
	}

	function loginToRemoteServer(json) {
					
		var remoteURL = medici + "/api/authenticate";
		var data = '';
		if(googleAccessToken == "") {
			data="username=" + userName + "&password=" + password;
		}
 		else {
			data = "googleAccessToken=" + googleAccessToken;
		}
			$.ajax({
				type : "POST",
				url : remoteURL,
				dataType : "text",
				xhrFields: {
				   withCredentials: true
				},
				crossDomain: true,
				data : data,
				success : showCollection,
				error : redirectToErrorPage
			});
	}

	function showCollection(json) {
		if (query == '') {
			window.location.replace("home");
		} else {
			window.location.reload();
		}
	}

	function redirectToErrorPage(jqXHR, textStatus, errorThrown) {
		if (jqXHR.responseText == 'Unauthorized') {

			$('#errorpanel').show();
			$('#forbiddenpanel').hide();

		} else if (jqXHR.responseText == 'Forbidden') {
			$('#forbiddenpanel').show();
			$('#errorpanel').hide();
		}
	}
</script>
<script src="https://apis.google.com/js/client.js?onload=OnLoadCallback"></script>
<body>

	<div id="banner">
		<map name="bannermap" id="bannermap">
			<area id="projectURL" href="http://sead-data.net" target="_blank"
				coords="0,0,600,134" shape="rect">
		</map>
		<img id="projectLogo" usemap="#bannermap"
			src="login_img/header-image.png" style="border: none;">
	</div>
	<!-- <img class="img-rounded" src='login_img/header-image.png'
		style='width: 4000px; margin-top: -3%;'></img> -->
	<div style='margin-top: 10%; margin-left: 2%; margin-right: 2%;'>
		<table>
			<tbody>
				<tr>
					<td id="projectDesc"><h4>SEAD ACR Discovery</h4>
						<p align="justify">
							<b>A brandable interface your project can use to provide data
								collections to the public.</b>
						</p>
						<p align="justify">Simply configure your project title and
							description in the main ACR interface/Admin page, and mark the
							collections you want to appear as 'published' (by adding a
							publication date as user metadata for that collection). The logo 
							at the top of the page can be replaced by changing the relevant image 
							files on the server.</p>
						<p align="justify">This interface will show the
							titles/abstract/tags/creators/and related metadata along with a
							list of sub-collections and files. Users can click on these and
							go to the data and collection pages in the ACR for these items.</p>
						<p align="justify">This interface provides a top-down view and
							includes only the data you're ready to share outside your team.</p>
						<p>You can require users to login to use this interface or
							allow anonymous access.</p></td>
					<td width='400px'>
						<div class="container">
							<div class="content">
								<div class="row">
									<div class="login-form">
										<h3>ACR Data Discovery</h3>
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
										<div id='errorpanel' style='display: none'>
											<font color='red'>The user name or password is
												incorrect.</font>
										</div>
										<div id='forbiddenpanel' style='display: none'>
											<font color='green'>You must be authorized to view
												these collections.</font>
										</div>
										<button class="btn primary" id="btnLogin">Login</button>
										<button class="btn primary" id="btnGoogle">Login using Google</button>
										<!-- </fieldset>
										</form> -->
									</div>
								</div>
							</div>
						</div> <!-- /container -->
					</td>
				</tr>
			</tbody>
		</table>

	</div>
</body>
</html>
