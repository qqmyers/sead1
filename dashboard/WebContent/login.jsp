<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="utf-8">
<title>ACR Dashboard Login</title>
<link href="login_css/bootstrap.css" rel="stylesheet">
<link href="login_css/common.css" rel="stylesheet">


</head>

<script src="//code.jquery.com/jquery-1.8.1.min.js"
	type="text/javascript"></script>
<script src="//code.jquery.com/ui/1.9.1/jquery-ui.js"></script>


<%
	String project_info = (String) request.getAttribute("projectInfo");
	String status_code = (String) request.getAttribute("statusCode");
	Boolean isAnonymous = (Boolean) request.getAttribute("isAnonymous");
	String medici = (String) request.getAttribute("medici");
	String googleClientId=(String)request.getAttribute("googleClientId");
%>


<script type="text/javascript">
	var query = '';

    var projInfo   = '<%=project_info%>';
    var authStatus = '<%=status_code%>';
    var anon = '<%=isAnonymous%>';
    var medici = '<%=medici%>';
   	var googleClientId = '<%=googleClientId%>';
	var userName = "";
	var password="";
	var googleAccessToken="";

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
	
		$("#btnLogin").click( function() {
			localLogin(false);
		});
		$("#btnGoogle").click(function() {
			
			gapi.auth.authorize({
				client_id: googleClientId,
				scope: 'email profile',
			 	access_type: 'online',
   				immediate: 'false',
   				authuser: -1,
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
			success : showRequestedResource,
			error : redirectToErrorPage
		});

	}

	function showRequestedResource(json) {
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
		} else {
			<!--cross-site issue preventing remote login - just allow this (user will have to login to--> 
			<!--medici manually if anonymous doesn't provide access -->
			showRequestedResource(null);
		}
	}
</script>
<script src="https://apis.google.com/js/client.js?onload=OnLoadCallback"></script>
<body>
	<div id="banner">
		<a href="http://sead-data.net/"><img id="logo" alt="SEAD Logo"
			src="login_images/logo.png"></a>
	</div>
	<br>

	<div id="main">
		<table>
			<tbody>
				<tr>
					<td width=60%>
						<h2>Active Content Repository</h2>
						<h1>Dashboard</h1>
					</td>
					<td width=400px>
						<div class="container">
							<div class="content">
								<div class="row">
									<div class="login-form">
										<h3 text-align="left">Login</h3>
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
