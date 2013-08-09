<html lang="en">
	<head>
		<meta charset="utf-8">
		<title>Project Summary</title>
		
		<!-- Le styles -->
		<link href="css/bootstrap.css" rel="stylesheet">		
		
		<!-- <link rel="stylesheet" href="css/screen.css"> -->
		<link href="css/jquery.treetable.css" rel="stylesheet" type="text/css" />
		<link rel="stylesheet" href="css/jquery.treetable.theme.default.css">
		<link href="css/summary.css" rel="stylesheet">		
		
		<script src="http://code.jquery.com/jquery-1.8.1.min.js" type="text/javascript"></script>
		<script src="http://code.jquery.com/jquery-latest.js"></script>		
  		<script src="js/jquery.dateFormat-1.0.js" type="text/javascript"></script>
		
		<script src="js/jquery.treetable.js"></script>
		
		
		<script src="http://maps.googleapis.com/maps/api/js?key=AIzaSyARkc86gaOrSG8C_MFgtJ5lHyoXLYi7N9Q&sensor=false"></script>
		<script type="text/javascript" src="https://www.google.com/jsapi"></script>
		<script type="text/javascript" src="js/mimeutil.js"></script>
		<script type="text/javascript" src="js/summary.js"></script>
		
		
	</head>
	<body style="height: 80%; padding-top: 60px;">
		<div id="hidden_collections" style="display:none">
			<%= request.getAttribute("collections") %>
		</div>
		<div id="hidden_recentuploads" style="display:none">
			<%= request.getAttribute("recentUploads") %>
		</div>
		<div id="hidden_creators" style="display:none">
			<%= request.getAttribute("creators") %>
		</div>
		<div id="hidden_projectPath" style="display:none">
			<%= request.getAttribute("projectPath") %>
		</div>
		<div id="hidden_projectInfo" style="display:none">
			<%= request.getAttribute("projectInfo") %>
		</div>
		<div id="hidden_datasetDistribution" style="display:none">
			<%= request.getAttribute("datasetDistribution") %>
		</div>
		<div class="navbar navbar-inverse navbar-fixed-top" style="border-bottom: 1px solid #888888;">
			<!-- <div class="navbar-inner"> -->
			<div>
				<div class="container" style="width: 80.4%; padding-right: 28px;">
					<!-- <button type="button" class="btn btn-navbar" data-toggle="collapse"	data-target=".nav-collapse">
						<span class="icon-bar"></span> 
						<span class="icon-bar"></span>
						<span class="icon-bar"></span>
					</button> -->
					<div class ="banner">
						<div id="logo">
							<a href="http://sead-data.net">
							<img class="logo" height="40px" border="0" alt="Medici" src="images/logo.png">
							</a>
						</div>
						<div class="gradient">
							<div><a id="projectLink" class="projectTitle"  href="" title="This system will enable researchers to actively and socially curate and share their own data.">
												<div id="projectTitle">SEAD ACR Instance</div>
											</a></div>
						</div>

					<!-- <div class="gradient">
						<a class="headerTitle" id="projectTitle"><table
								cellspacing="0" cellpadding="0" class="headerTitle">
								<tbody>
									<tr>
										<td align="right" style="vertical-align: top;">
											<a class="gwt-Anchor" href=""title="This system will enable researchers to actively and socially curate and share their own data.">
												SEAD ACR Instance
											</a>
										</td>
									</tr>
								</tbody>
							</table></a>
					</div> -->
				</div>
					<!-- <a class="brand" href="http://sead-data.net/"><img border="0" class="logo" src="images/logo.png" alt="Medici"></a> -->
					
					<div class="nav-collapse collapse">
						<ul class="nav">
							<li><a href="#">Home</a></li>
							<li><a href="<%= request.getAttribute("projectPath") %>/#listDatasets" target="_blank">Data</a></li>
							<li><a href="<%= request.getAttribute("projectPath") %>/#listCollections" target="_blank">Collections</a></li>
							<li><a href="<%= request.getAttribute("projectPath") %>/#tags" target="_blank">Tags</a></li>
							<li><a href="<%= request.getAttribute("projectPath") %>/#map" target="_blank">Map</a></li>
							<li><a href="<%= request.getAttribute("projectPath") %>/#upload" target="_blank">Upload</a></li>
							<li><a href="<%= request.getAttribute("projectPath") %>/#administration" target="_blank">Administration</a></li>
							
						</ul>
					</div>
						<!--/.nav-collapse -->
					<!-- <div id="loginMenu" class="menuElementRight">
						<table class="navMenu" cellspacing="0" cellpadding="0">
							<tbody>
								<tr>
									<td align="left" style="vertical-align: top;">
										<div class="gwt-Hyperlink navMenuLink">
											<a href="#login">Login</a>
										</div>
									</td>
									<td align="left" style="vertical-align: top;">
										<div class="gwt-Hyperlink navMenuLink">
											<a href="#signup">Sign up</a>
										</div>
									</td>
								</tr>
							</tbody>
						</table>
					</div> -->
				</div>
				
			</div>
		</div>
	
		<div class="container" style="height: 95%; padding-top:16px;">
			<div class="row" style="height: 50%; padding-bottom: 10px">
				<div class="span4" style="width: 20%; height: 100%; background:#EEEEEE; ">			
						
						<h3>Project Description </h3>
						<div id="projectInfo" style="overflow:auto">	
							<!-- Link will be changed from projectPath to the URL defined in the project configuration -->
							<a href="<%= request.getAttribute("projectPath") %>" target="_blank" id="projectName" ></a></br></br>
							<p id="projectDesc" > </p>
						</div>
						<!-- <a href="http://usgs.gov" target="_blank">http://usgs.gov</a></br>
						<a href="http://cuahsi.org" target="_blank">http://cuahsi.org</a></br>
						<a href="http://wsc-wiki.illinois.edu" target="_blank">http://wsc-wiki.illinois.edu</a> --></br></br></br></br></br>			
				</div>
				<div class="span4" style="width: 50%; height: 100%">
					<!-- <strong>Select a dataset location at Watershed: LeSeur</strong> -->
					<div id="summaryMap" style="height: 100%">
						<!-- <iframe id="summaryMapIF"  name="inlineframe" src="http://lowermississippi.ncsa.illinois.edu/geo-webapp/" frameborder="0" scrolling="auto" width="100%" height="100%" marginwidth="5" marginheight="5" ></iframe> -->
						<!-- <iframe name="inlineframe" src="http://lowermississippi.ncsa.illinois.edu/geo-webapp/"></iframe> -->
					</div>
				</div>
				<div id="teammembers" class="span4" style="width: 20%; height: 100%; background:#EEEEEE; overflow:auto; padding:0px 10px">				
						<h3>Team Members</h3></br>
						Praveen Kumar</br>
						Charles Nyugen</br>
						James Myers</br></br></br></br>				
				</div>
			</div>
			<div class="row" style="height: 50%; padding-top: 10px">
				<div class="span4" style="width: 20%; height: 100%">
					<div id="container1" style="height: 100%"></div>
				</div>
				<div class="span4" style="width: 50%; height: 100%; overflow:auto; padding:10px 0px">				
					<!-- <table id="table" class="table table-bordered" style="width: 100%; height: 100%">
						<thead>
							<tr>
								<th width="5%"></th>
								<th width="85%">Collections</th>
								<th width="10%">Size</th>
							</tr>
						</thead>
						<tbody></tbody>
					</table> -->
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
				<div class="span4" style="width: 20%; height: 108%; background:#EEEEEE; overflow:auto; padding:0px 10px">		
					<h3>Recent Uploads</h3>		
					<!-- <h3>Recent Uploads</h3>
					<p> Praveen Kumar</p><hr>
					<p> Charles Nyugen</p><hr>
					<p> James Myers</p><hr> -->
					<div id="recentuploads">
						<!-- <div class="media">
							<a class="pull-left" href="#"> <img class="media-object" src="http://nced.ncsa.illinois.edu/acr/api/image/preview/small/tag:cet.ncsa.uiuc.edu,2008:/bean/Dataset/8a48d7d7-b91a-4741-84be-116cd3eafd80">
							</a>
							<div class="media-body">
								<h4 class="media-heading">Media heading</h4>
								dasdadadasdsada
							</div>
						</div>
						<div class="media">
							<a class="pull-left" href="#"> <img class="media-object" src="http://sead.ncsa.illinois.edu/api/image/preview/small/tag:medici@uiuc.edu,2009:data_JcTJq6ZpEZ_Rm3hmOVqXgg">
							</a>
							<div class="media-body">
								<h4 class="media-heading">Media heading</h4>
								dasdadadasdsada
							</div>
						</div>
						<div class="media">
							<a class="pull-left" href="#"> <img class="media-object" src="http://sead.ncsa.illinois.edu/api/image/preview/small/tag:medici@uiuc.edu,2009:data_JcTJq6ZpEZ_Rm3hmOVqXgg">
							</a>
							<div class="media-body">
								<h4 class="media-heading">Media heading</h4>
								dasdadadasdsada
							</div>
						</div>
						<div class="media">
							<a class="pull-left" href="#"> <img class="media-object" src="http://sead.ncsa.illinois.edu/api/image/preview/small/tag:medici@uiuc.edu,2009:data_JcTJq6ZpEZ_Rm3hmOVqXgg">
							</a>
							<div class="media-body">
								<h4 class="media-heading">Media heading</h4>
								dasdadadasdsada
							</div>
						</div> -->
					</div>
			</div>
		</div>
	</div>
</body>
<script>
	callOnLoad();
	//$("#table").treetable({ expandable: true });
</script>
</html>
