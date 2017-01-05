<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<!-- local copy of UM css that references local fonts -->
<link rel="stylesheet" id="spacious_style-css" href="login_css/um-style.css?ver=3.9.1" type="text/css" media="all">
<link href='//fonts.googleapis.com/css?family=Lato&subset=latin,latin-ext' rel='stylesheet' type='text/css'>
<link href='//fonts.googleapis.com/css?family=Raleway:400,100,200,300,500,600,700,800,900' rel='stylesheet' type='text/css'>
<title>Project Spaces | SEAD </title>
<script type="text/javascript" src="login_scripts/cnss.js"></script>
<link href="login_css/bootstrap.sead-scope.css" rel="stylesheet">
<script src="//code.jquery.com/jquery-latest.js"></script>
<script src="login_scripts/json2.js"></script>
<script src="login_scripts/jquery-cookie.js"></script>
<script src="login_scripts/jquery.tablesorter.min.js"></script>
<script src="login_scripts/table2CSV.js"></script>
<script src="login_scripts/filesize.min.js"></script>

<link rel="stylesheet" id="um_style_sheet" href="login_css/um-style.css" type="text/css" media="all">
<link rel="stylesheet" id="spacious_style-css" href="login_css/project-space.css" type="text/css" media="all">

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
		$("#projects>tbody").append("<tr  id=\"" + rowId + "\"><td class='project-name'/><td class='project-preprint'/><td  class='project-publish'/><td class='project-view'/><td class='project-users'/><td class='project-collections'/><td class='project-datasets'/><td class='project-bytes'/><td class='project-link'/><td class='project-version'/></tr>");
	}

	function checkDone() {

		if((serversLeft==0)&&(statsLeft==0)) {
			$("#home-loading").hide();
  
  $("#projects").tablesorter({
                        textExtraction:  function(node) { var x = node; x = x.childNodes[0]; return x.innerHTML},
                        // sort on the first column , order asc
                        sortList: [[0,0]]
                    });
                }
        }


        function projInfoJsonParser(json) {
                var current = decodeURIComponent(this.url.substr(this.url.indexOf("http")));
                var index = jQuery.inArray(current, ${projects});
                 $("#" + index + ">td:eq(0)").html("<div ><a href=\"" + current + "/..\">" + json['project.name'] + "</div></a>");
                 if(json['project.url']) {
                   $("#" + index + ">td:eq(8)").html( "<div><a href=\"" + json['project.url'] + "\">" + json['project.url'] + "</div></a>");
                 } else {
                   $("#" + index + ">td:eq(8)").html("<div/>");
                 }
                serversLeft = serversLeft-1;
                checkDone();
        }

        function sysInfoJsonParser(json) {

                var jstring = JSON.stringify(json);
                var current = decodeURIComponent(this.url.substr(this.url.indexOf("http")));
                var index = jQuery.inArray(current, ${projects});

                 $("#" + index + ">td:eq(1)").html("<div class='sortkey' style='visibility:hidden;float:right;width:0px'>" + json["Public Preprint Collections"] + "</div><div ><a href=\"" + current + "/#listCollections\">" + json["Public Preprint Collections"] + "</a></div>");
                 $("#" + index + ">td:eq(2)").html("<div class='sortkey' style='visibility:hidden;float:right;width:0px'>" + json["Published Collections"] + "</div><div><a href=\"" + current + "/../discovery\">" + json["Published Collections"] + "</div></a>");
                 $("#" + index + ">td:eq(3)").html("<div>"+json["Total Views"]+"</div>" );
                 $("#" + index + ">td:eq(4)").html("<div>"+json["Number of Users"] +"</div>");
                 $("#" + index + ">td:eq(5)").html("<div>"+json["Collections"]+"</div>");
                 $("#" + index + ">td:eq(6)").html("<div>"+ json.Datasets+"</div>");
                 $("#" + index + ">td:eq(7)").html("<div class='sortkey' style='visibility:hidden;float:right;width:0px'>" + json["Bytes from uploaded dataset"] + "</div><div>"+filesize(parseInt(json["Bytes from uploaded dataset"]))+"</div>");
                 $("#" + index + ">td:eq(9)").html("<div>"+json['Version'] + "-" + json['Build'] + "</div>");

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
</head>


<body class="home page wide-1218">	
	<div id="page" class="hfeed site">
		<header id="masthead" class="site-header clearfix">
			<div id="header-text-nav-wrap" class="clearfix">
	
	<!-- #header-logo-image -->
	<div id="header-left-section" style="background-image:url(/projects/login_img/headback.PNG)" >
		<div id="header-logo-image">
			<a href="http://sead-data.net/" title="SEAD" rel="home"><img src="http://sead-data.net/wp-content/uploads/2014/06/logo.png" alt="SEAD"></a>
		</div>
	<!-- #header-logo-image -->

	<!-- #header-right-section social icons--> 
	<div id="header-right-section">
		<div id="header-right-sidebar" class="clearfix">
			<aside id="cnss_widget-3" class="widget widget_cnss_widget">
				<table class="cnss-social-icon" style="width:138px" border="0" cellspacing="0" cellpadding="0">
					<tbody>
						<tr>
							<td >
								<a target="_blank" title="facebook" href="https://www.facebook.com/SEADDataNet">
									<img src="http://sead-data.net/wp-content/uploads/1403285144_facebook.png" border="0" width="32" height="32" alt="facebook">
								</a>
							</td>
							<td >
								<a target="_blank" title="twitter" href="https://twitter.com/SEADdatanet">
									<img src="http://sead-data.net/wp-content/uploads/1403285215_twitter.png" border="0" width="32" height="32" alt="twitter">
								</a>
							</td>
							<td >
								<a target="_blank" title="Slideshare" href="http://www.slideshare.net/SEADdatanet">
									<img src="http://sead-data.net/wp-content/uploads/1403285311_slidershare.png" border="0" width="32" height="32" alt="Slideshare">
								</a>
							</td>
							<td ><a target="_blank" title="Rss Feed" href="http://www.sead-data.net/?cat=11,12,13,29,30,31&amp;feed=rss2"><img src="http://sead-data.net/wp-content/uploads/1415982481_rss.png" border="0" width="32" height="32" alt="Rss Feed" style="opacity: 1;"></a></td>
						</tr>
					</tbody>
				</table>
			</aside>
		</div>
		</div>
		<!-- #header-right-section --> 

                       
       <!--header navigation begin-->
					<div id="header-navigation">
						<nav id="site-navigation" class="main-navigation"
							role="navigation">
							<h1 class="menu-toggle">Menu</h1>
							<div class="menu-global-navigation-container">
								<ul id="menu-global-navigation" class="menunav-menu">
									<li class="menu-item  menu-item-has-children "><a>About</a>
										<ul class="sub-menu">
<li class="menu-item"><a
												href="http://sead-data.net/about-sead">About SEAD</a></li>
<li class="menu-item"><a
												href="http://sead-data.net/news">News</a></li>
											<li class="menu-item"><a
												href="http://sead-data.net/project-team">Project
													Team</a></li>
<li class="menu-item"><a
												href="http://sead-data.net/contact">Contact Us</a></li>

											<li class="menu-item"><a
												href="http://sead-data.net/advisory-board">Advisory
													Board</a></li>
					<li class="menu-item"><a			href="http://sead-data.net/about/publicationspresentations">Publications &amp; Presentations</a></li>
					<li class="menu-item"><a			href="http://sead-data.net/about/standards">Data Mangement Standards and Practices</a></li>
					</ul></li>

									<li class="menu-item menu-item-has-children"><a>Services</a>
										<ul class="sub-menu">
											<li class="menu-item"><a
												href="http://sead-data.net/features-tour">Features Tour</a></li>
										
										                      <li class="menu-item"><a
                        href="http://sead-data.net/support-your-data-management-plan-dmp/">Data Management Plan Support</a></li>
                                      </ul></li>
									<li class="menu-item current-menu-item menu-item-has-children"><a>Project
											Spaces</a>
									<ul class="sub-menu">
											<li class="menu-item"><a
												href="https://sead.ncsa.illinois.edu/projects">1.5 Project Spaces</a></li>
										
										                      <li class="menu-item"><a
                        href="https://sead2.ncsa.illinois.edu/spaces">2.0 Project Spaces</a></li>
                                      </ul>
</li>
									<li class="menu-item menu-item-has-children"><a>Repositories</a>
										<ul class="sub-menu">
											<li class="menu-item"><a
												href="http://sead-data.net/connect-your-repository-to-sead">Connect Your Repository To SEAD</a></li>
							<li class="menu-item"><a
												href="http://sead-data.net/partners">SEAD Partners</a></li>
</ul></li>

									<li class="menu-item menu-item-has-children"><a>Help</a>
										<ul class="sub-menu">
											<li class="menu-item"><a
												href="http://sead-data.net/faqs">FAQs</a></li>
							<li class="menu-item"><a
												href="http://sead-data.net/help-topics">Help Topics</a></li>

										</ul></li>
								</ul>
							</div>
						</nav>
					</div>
					<!--header navigation end-->

	</div><!-- #header-left-section -->
	</div>

	<div class='no-sidebar-full-width'>
		<article id='primary'>
			<div class="article entry-content clearfix">
				<p><h1><span>Project Spaces</span></h1></p>
				<p>Welcome to SEAD Project Spaces, where you and your team can collect and manage your project's active data. The table below lists all active project spaces in SEAD. Click one of the links to go to a given project's data collections (note: you will need access permission from the space's admins).</p>
				<p>You can try out Project Spaces features by going to the <a href="https://sead-demo.ncsa.illinois.edu/">Demo Space</a> or <a href="http://sead-data.net/contactus/">contact us</a> about setting up a space for your group.</p>
			</div>
		</article>
	</div>
    
    <!-- Project space table body -->
	<div class="sead-scope">
	<div id="home-loading" style='float: right; width: auto; margin-left: auto; margin-right: 50px; margin-top: -15px; display: none' >
		Loading ...&nbsp;&nbsp;<img src="login_img/loading.gif"></img>
	</div>

	<div id="exp-table">
		<table id='projects' class='tablesorter'>
			<thead>
				<tr>
					<th class="table-header-project-name">Project Space</th>
					<th class="table-header-project-preprint">Preprint Collections</th>
					<th class="table-header-project-publish">Published Collections</th>
					<th class="table-header-project-view"><div>Data Page Views</div></th>
					<th class="table-header-project-users">Group Size</th>
					<th class="table-header-project-collections"><div>Total Collections</div></th>
					<th class="table-header-project-datasets"><div>Total Datasets</div></th>
					<th class="table-header-project-bytes"><div>Total Data Size</div></th>
					<th class="table-header-project-link">Group Home Page</th>
					<th class="table-header-project-version">Version</th>
					<th class="gutter"></th>

				</tr>
			</thead>
			<tbody></tbody>
		</table>

		<form action="export" method ="post"  id="exportForm"> 
			<input type="hidden" name="csv_text" id="csv_text">
			<input type="submit" value="Export as CSV" onclick="getCSVData()">
		</form>
	</div>
</div>
</div>
</header>

<footer class="entry-meta-bar clearfix">	        			
	<div class="entry-meta clearfix"></div>
</footer>
</article>

<footer id="colophon" class="clearfix">	
			<div class="footer-socket-wrapper clearfix">
				<div class="inner-wrap">
					<div class="footer-socket-area">
						<div class="copyright"><img id="nsf" src="http://sead-data.net/wp-content/uploads/2014/06/nsf2.png" alt="NSF " width="30px" height="30px">SEAD is funded by the National Science Foundation under cooperative agreement #OCI0940824.</div>						
						<nav class="small-menu">
							<div class="menu-footer-menu-container">
								<ul id="menu-footer-menu" class="menu">	
									<a href="http://sead-data.net/contactus/">Contact Us</a>
								</li>
								</ul>
							</div>	
					</nav>	
					</div>
				</div>
			</div>
		</footer>
		<a href="#masthead" id="scroll-up" style="display: inline;"></a>	
</body>
</html>
