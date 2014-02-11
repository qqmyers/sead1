<html lang="en">
	<head>
		<meta charset="utf-8">
		<title>ACR Dashboard</title>
		
		<link href="login_css/bootstrap.css" rel="stylesheet">		
		
		<!-- <link rel="stylesheet" href="css/screen.css"> -->
		<link href="css/jquery.treetable.css" rel="stylesheet" type="text/css" />
		<link rel="stylesheet" href="css/jquery.treetable.theme.default.css">
		
		<!--  ol2 styles -->
		<link rel="stylesheet" href="http://openlayers.org/api/theme/default/style.css" type="text/css">
		<link href="css/dashboard.css" rel="stylesheet">		
<% 
String collections = (String) request.getAttribute("collections"); 
String recentUploads = (String) request.getAttribute("recentUploads");
String creators = (String) request.getAttribute("creators");
String projectPath = (String) request.getAttribute("projectPath");
String geobrowserUrl = (String) request.getAttribute("geobrowserUrl");
String datasetDistribution = (String) request.getAttribute("datasetDistribution");
String layersInfo = (String) request.getAttribute("layersInfo");
String geoProxyUrl = (String) request.getAttribute("geoProxyUrl");
Boolean isAnonymous = (Boolean) request.getAttribute("isAnonymous");
%>	
	</head>
	<body>
		<div id="hidden_collections" style="display:none">
			<%= collections %>
		</div>
		<div id="hidden_recentuploads" style="display:none">
			<%= recentUploads %>
		</div>
		<div id="hidden_creators" style="display:none">
			<%= creators %>
		</div>
		<div id="hidden_projectPath" style="display:none">
			<%= projectPath %>
		</div>
		<div id="hidden_datasetDistribution" style="display:none">
			<%= datasetDistribution %>
		</div>
		<div id="hidden_layersInfo" style="display:none">
			<%= layersInfo %>
		</div>
		
		<div class="navbar navbar-inverse navbar-fixed-top" style="border-bottom: 1px solid #888888;">
			<div class="container">
				<div class ="banner">
					<div id="logo">
						<a href="http://sead-data.net">
							<img class="logo" height="40px" border="0" alt="Medici" src="images/logo.png">
						</a>
					</div>
					<div class="gradient">
						<a id="projectLink" class="projectTitle"  href="" title="This system will enable researchers to actively and socially curate and share their own data.">
							<span id="projectTitle">SEAD ACR Instance</span>
						</a>
					</div>
				</div>
			
				<div class="nav-collapse collapse">
					<ul class="nav">
						<li><a href="<%= projectPath %>/#"  target="_blank">Home</a></li>
						<li><a href="<%= projectPath %>/#listDatasets" target="_blank">Data</a></li>
						<li><a href="<%= projectPath %>/#listCollections" target="_blank">Collections</a></li>
						<li><a href="<%= projectPath %>/#tags" target="_blank">Tags</a></li>
						<li><a href="<%= projectPath %>/#map" target="_blank">Map</a></li>
						<li><a href="<%= projectPath %>/#upload" target="_blank">Upload</a></li>
						<li><a href="<%= projectPath %>/#administration" target="_blank">Administration</a></li>
					</ul>
					<ul class="menuElementRight">
						<li><a href="DoLogout" id='loginout' >Logout</a></li>
					</ul>	
				</div>
			</div>
		</div>
	
		<div class="container">
			<div class="row">
				<div class="span3 fixed-height-col" style="background:#EEEEEE">			
					<h4>Project Description </h4>
					<div id="projectInfo" >	
						<!-- Link will be changed from projectPath to the URL defined in the project configuration -->
						<a href="<%= projectPath %>" target="_blank" id="projectName" ></a><br/><br/>
						<p id="projectDesc"> </p>
					</div>
					<!-- <a href="http://usgs.gov" target="_blank">http://usgs.gov</a></br>
					<a href="http://cuahsi.org" target="_blank">http://cuahsi.org</a></br>
					<a href="http://wsc-wiki.illinois.edu" target="_blank">http://wsc-wiki.illinois.edu</a> -->			
				</div>
				<div class="span6 fixed-height-col">
					<div id="summaryMap" class="map"></div>
					<div id="mapMsg"></div>
				</div>
				<div class="span3 fixed-height-col" style="background:#EEEEEE">				
					<h4>Team Members</h4>
					<div id="teammembers">
					<!-- 
					Praveen Kumar<br/>
					Charles Nyugen<br/>
					James Myers<br/><br/>
					 -->
					</div>				
				</div>
			</div>
			<div class="row">
				<div class="span3 fixed-height-col">
					<div id="container1"></div>
				</div>
				<div class="span6 fixed-height-col">				
					<table id="table">
						<thead>
							<tr>
								<th>Collection </th>
								<th>Size </th>
							</tr>
						</thead>
						<tbody>
						</tbody>
					</table>
				</div>
				<div class="span3 fixed-height-col" style="background:#EEEEEE">		
					<h4>Recent Uploads</h4>		
					<div id="recentuploads">
					</div>
				</div>
			</div>
		</div>
		<div class="bottomMenu">
			<a href="http://sead-data.net">SEAD</a> Active Content Repository. Powered by <a href="http://medici.ncsa.illinois.edu">NCSA Medici</a>: (@VERSION@)
			<a href="#feedback">Feedback</a>
		</div>
		<script type="text/javascript">
			var geoProxyUrl = '<%= geoProxyUrl %>';
			var projInfo = '<%= request.getAttribute("projectInfo") %>';
			var isAnonymous = '<%= isAnonymous %>';
			var geobrowserUrl = '<%= geobrowserUrl %>';
		</script>
		<script src="http://code.jquery.com/jquery-1.8.1.min.js" type="text/javascript"></script>
		<script src="http://code.jquery.com/jquery-latest.js"></script>		
	  	<script src="js/jquery.dateFormat-1.0.js" type="text/javascript"></script>
		<script src="js/jquery.treetable.js"></script>
	
	    <!-- ol2 -->
		<script src="http://openlayers.org/api/OpenLayers.js" type="text/javascript"></script>
			
		<!--  google map -->
		<!-- <script src="http://maps.googleapis.com/maps/api/js?key=AIzaSyARkc86gaOrSG8C_MFgtJ5lHyoXLYi7N9Q&sensor=false"></script> -->
		<script type="text/javascript" src="https://www.google.com/jsapi"></script>
		<script type="text/javascript" src="js/mimeutil.js"></script>
		<script type="text/javascript" src="js/dashboard.js"></script>
	</body>
</html>
