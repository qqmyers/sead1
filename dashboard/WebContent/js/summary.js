var projectPath = '';

function initialize() {
	var mapProp = {
		center : new google.maps.LatLng(51.508742, -0.120850),
		zoom : 5,
		mapTypeId : google.maps.MapTypeId.ROADMAP
	};
	var map = new google.maps.Map($("#summaryMap")[0], mapProp);
}

function drawChart() {
	var barArray = new Array();
	
	barArray[barArray.length] = ['Categories', null];
	if(datasetDistribution!=undefined) {
		for (key in datasetDistribution) {
			barArray[barArray.length] = [key, datasetDistribution[key]];
		}
	}else {
		var allDatasetDist = $('#hidden_datasetDistribution').html().trim().split(',');
		for(var i=0; i<allDatasetDist.length; i++){
			barArray[barArray.length] = [allDatasetDist[i].split('=')[0].substring(1), parseInt(allDatasetDist[i].split('=')[1])];
			//barArray[barArray.length] = ['Other', 122];
		}
	}
      /*var data = google.visualization.arrayToDataTable([
        ['Categories', null],
        ['Geo',  900],
        ['Image',  1000],
        ['Document',  1170],
        ['TimeSeries',  660]
      ]);*/
	
	var data = google.visualization.arrayToDataTable(barArray);

      var options = {
        title: 'Dataset Distribution',
        hAxis: {title: 'Category', titleTextStyle: {color: 'red'}},
        legend: {position: 'none'}
      };

      var chart = new google.visualization.ColumnChart($('#container1')[0]);
      
      if(barArray.length>1){
    	  chart.draw(data, options);
      }else {
    	  alert('No dataset in this collection');
      }

}

function loadTableContent() {

	var div_html = '';
	var title = '';
	var uri = '';
	var displayTitle = '';
	
	var tagURISet = {};
	
	var obj = $.parseJSON($("#hidden_collections").html());

	for ( var i = 0; i < obj.sparql.results.result.length; i++) {
		abs = '';
		var jsonBinding = obj.sparql.results.result[i].binding;
		for ( var j = 0; j < jsonBinding.length; j++) {
			$.each(jsonBinding[j], function(key, value) {
				if (value == 'tagID') {
					uri = jsonBinding[j]['uri'];
				} else if (value == 'title') {
					title = jsonBinding[j]['literal'];
					displayTitle = title;
					if (title.indexOf("/") != -1) {
						displayTitle = title
								.substring(title.lastIndexOf("/") + 1);
					}
				}/* else if (value == 'abstract') {
					abs = jsonBinding[j]['literal'];
				}*/
			});
		}
		
		if(!tagURISet[uri]) { 
			tagURISet[uri] = true;
	
			div_html += '<tr data-tt-id="'+i+'">'
						+ '<td><span class="folder"><a	href="'+projectPath+'/#collection?uri='
						+ uri + '" target ="_blank">' + displayTitle + '</a></span></td><td>--</td>'
						+'<tr data-tt-id="'+i+'-1" data-tt-parent-id="'+i+'">';
		
		}
	}

	$("#table tbody").html(div_html);
	
	
}	


/*{"sparql":{"results":{"result":
 * [{"binding":
 * [{"name":"uri","uri":"http://cet.ncsa.uiuc.edu/2007/person/anonymous"},{"name":"name","literal":"Anonymous"}]},{"binding":[{"name":"uri","uri":"http://cet.ncsa.uiuc.edu/2007/person/medici@ncsa.illinois.edu"},{"name":"name","literal":"Medici Admin"}]},{"binding":[{"name":"uri","uri":"http://cet.ncsa.uiuc.edu/2007/person/malviyas@indiana.edu"},{"name":"name","literal":"Saurabh Malviya"}]},{"binding":[{"name":"uri","uri":"http://cet.ncsa.uiuc.edu/2007/person/myersj4@rpi.edu"},{"name":"name","literal":"Jim Myers"}]},{"binding":[{"name":"uri","uri":"http://cet.ncsa.uiuc.edu/2007/person/liuy18@rpi.edu"},{"name":"name","literal":"Yue Liu"}]},{"binding":[{"name":"uri","uri":"http://cet.ncsa.uiuc.edu/2007/person/govinr2@rpi.edu"},{"name":"name","literal":"Ram  Krishnan"}]},{"binding":[{"name":"uri","uri":"http://cet.ncsa.uiuc.edu/2007/person/rhmcdona@indiana.edu"},{"name":"name","literal":"robert mcdonald"}]},{"binding":[{"name":"uri","uri":"http://cet.ncsa.uiuc.edu/2007/person/lmarini@illinois.edu"},{"name":"name","literal":"Luigi Marini"}]},{"binding":[{"name":"uri","uri":"http://cet.ncsa.uiuc.edu/2007/person/m.rahnemoonfar@gmail.com"},{"name":"name","literal":"Maryam Rahnemoonfar"}]},{"binding":[{"name":"uri","uri":"http://cet.ncsa.uiuc.edu/2007/person/inkouper@indiana.edu"},{"name":"name","literal":"Inna Kouper"}]},{"binding":[{"name":"uri","uri":"http://cet.ncsa.uiuc.edu/2007/person/mzaman@illinois.edu"},{"name":"name","literal":"Md Aktaruzzaman"}]},{"binding":[{"name":"uri","uri":"http://cet.ncsa.uiuc.edu/2007/person/ramprasanna.krishnan@gmail.com"},{"name":"name","literal":"ram krishnan"}]},{"binding":[{"name":"uri","uri":"http://cet.ncsa.uiuc.edu/2007/person/kavchand@indiana.edu"},{"name":"name","literal":"Kavitha Chandrasekar"}]},{"binding":[{"name":"uri","uri":"http://cet.ncsa.uiuc.edu/2007/person/malviyas@umail.iu.edu"},{"name":"name","literal":"Saurabh Malviya"}]},{"binding":[{"name":"uri","uri":"http://cet.ncsa.uiuc.edu/2007/person/kooper@illinois.edu"},{"name":"name","literal":"Rob Kooper"}]},{"binding":[{"name":"uri","uri":"http://cet.ncsa.uiuc.edu/2007/person/kumar1@illinois.edu"},{"name":"name","literal":"Praveen Kumar"}]},{"binding":[{"name":"uri","uri":"http://cet.ncsa.uiuc.edu/2007/person/lebot@rpi.edu"},{"name":"name","literal":"Tim Lebo"}]},{"binding":[{"name":"uri","uri":"http://cet.ncsa.uiuc.edu/2007/person/valentin@sdsc.edu"},{"name":"name","literal":"David Valentine"}]},{"binding":[{"name":"uri","uri":"http://cet.ncsa.uiuc.edu/2007/person/annszimmerman@gmail.com"},{"name":"name","literal":"Ann Zimmerman"}]}]},"xmlns":"http://www.w3.org/2005/sparql-results#","head":{"variable":[{"name":"uri"},{"name":"name"}]}}}*/

var map = new Object();
function parseCreators() {
	
	var creatorURI = '';
	var creatorName = '';
	
	var div_html ='<h3>Team Members</h3>';
	var obj = $.parseJSON($("#hidden_creators").html());
	/*{"name":"tagID","uri":"tag:cet.ncsa.uiuc.edu,2008:/bean/Dataset/8a48d7d7-b91a-4741-84be-116cd3eafd80"},
	{"name":"title","literal":"R08_009.DAT"},
	{"name":"creator","uri":"http://cet.ncsa.uiuc.edu/2007/person/medici@ncsa.illinois.edu"},
	{"name":"date","literal":{"content":"2013-01-18T21:33:27.000Z","datatype":"http://www.w3.org/2001/XMLSchema#dateTime"}*/

	for ( var i = 0; i < obj.sparql.results.result.length; i++) {
		var jsonBinding = obj.sparql.results.result[i].binding;
		for ( var j = 0; j < jsonBinding.length; j++) {
			$.each(jsonBinding[j], function(key, value) {
				if (value == 'uri') {
					creatorURI = jsonBinding[j]['uri'];
				} else if (value == 'name') {
					creatorName = jsonBinding[j]['literal'];
				} 
			});
		}
		url = '#'
		map[creatorURI] = creatorName;
		div_html += "<a href='" + url + "' target=_blank>" + creatorName + "</a> </br>";
	}
	$('#teammembers').html(div_html);
}

function loadRecentUploads(){
	var div_html = '';
	
	var title = '';
	var uri = '';
	var displayTitle = '';
	var creator = '';
	var date = [];
	
	var obj = $.parseJSON($("#hidden_recentuploads").html());
	/*{"name":"tagID","uri":"tag:cet.ncsa.uiuc.edu,2008:/bean/Dataset/8a48d7d7-b91a-4741-84be-116cd3eafd80"},
	{"name":"title","literal":"R08_009.DAT"},
	{"name":"creator","uri":"http://cet.ncsa.uiuc.edu/2007/person/medici@ncsa.illinois.edu"},
	{"name":"date","literal":{"content":"2013-01-18T21:33:27.000Z","datatype":"http://www.w3.org/2001/XMLSchema#dateTime"}*/

	for ( var i = 0; i < obj.sparql.results.result.length; i++) {
		abs = '';
		var jsonBinding = obj.sparql.results.result[i].binding;
		for ( var j = 0; j < jsonBinding.length; j++) {
			$.each(jsonBinding[j], function(key, value) {
				if (value == 'tagID') {
					uri = jsonBinding[j]['uri'];
				} else if (value == 'title') {
					title = jsonBinding[j]['literal'];
					displayTitle = title;
					if (title.indexOf("/") != -1) {
						displayTitle = title
								.substring(title.lastIndexOf("/") + 1);
					}
				} else if (value == 'creator') {
					creator = jsonBinding[j]['uri'];
				} else if (value == 'date') {
					timestamp = jsonBinding[j]['literal']['content'];
					date[0] = $.format.date(timestamp, 'yyyy');
					date[1] = $.format.date(timestamp, 'MMM');
					date[2] = $.format.date(timestamp, 'dd');
					date[3] = $.format.date(timestamp, 'hh:mm:ss');
					
				}
			});
		}
		
		/*<div class="media">
		<a class="pull-left" href="#"> <img class="media-object" src="http://nced.ncsa.illinois.edu/acr/api/image/preview/small/tag:cet.ncsa.uiuc.edu,2008:/bean/Dataset/8a48d7d7-b91a-4741-84be-116cd3eafd80">
		</a>
		<div class="media-body">
			<h4 class="media-heading">Media heading</h4>
			dasdadadasdsada
		</div>
	</div>*/
		displayTitleAfter = displayTitle.length>12?displayTitle.substring(0,11)+'...':displayTitle;
		div_html += '<div class="media">'
					+ '<a class="pull-left" href="'+projectPath+'/#dataset?id='+uri+'" target="_blank"> <img title="'+displayTitle+'" class="media-object" src="'+projectPath+'/api/image/preview/small/'
					+ uri + '" </img> </a>'
					+'<div class="media-body">'
					+'<a href="'+projectPath+'/#dataset?id='+uri+'" target="_blank" title="'+displayTitle+'">'+displayTitleAfter
					+'</a></br>'+map[creator]+'</br>'+date[0] +' '+ date[1]+' '+date[2] +' '+ date[3]+'</div></div>';
		
		/*div_html += '<div class="media">'
			+ '<a class="pull-left" href="http://nced.ncsa.illinois.edu/acr/#dataset?id='+uri+'" target="_blank"> <img title="'+displayTitle+'" class="media-object" src="images/nopreview-100.gif"'
			+ ' </img> </a>'
			+'<div class="media-body">'
			+'<a href="http://nced.ncsa.illinois.edu/acr/#dataset?id='+uri+'" target="_blank" title="'+displayTitle+'">'+displayTitleAfter
			+'</a></br>'+map[creator]+'</br>'+date[0] +' '+ date[1]+' '+date[2] +' '+ date[3]+'</div></div>';
*/		

	}
	//alert(displayTitle.replace(/_/g,'&#95;').replace(/\./g,'&#46;'));
	$("#recentuploads").html(div_html);
}

function roundNumber(num, dec) {
	var result = Math.round(num * Math.pow(10, dec)) / Math.pow(10, dec);
	return result;
}

function getTeamMembers() {
	var div_html ='<h3>Team Members</h3>';


	//var jsonString = JSON.stringify(json);
	var obj = $.parseJSON($("#hidden_collections").html());
	if (obj.sparql.results.result != null) {
		if (obj.sparql.results.result.length == null) {
			var jsonBinding = obj.sparql.results.result.binding;
			getAttributesForHomePage(jsonBinding, element);
		} else {
			for ( var i = 0; i < obj.sparql.results.result.length; i++) {
				var jsonBinding = obj.sparql.results.result[i].binding;
				div_html += getAttributesForHomePage(jsonBinding) + '</br>';
			}
		}
	}

	$('#teammembers').html(div_html);
	
	/*<h3>Team Members</h3></br>
	Praveen Kumar</br>
	Charles Nyugen</br>
	James Myers</br></br></br></br>	*/

}

function getAttributesForHomePage(jsonBinding) {
	var creator = '';
	$.each(jsonBinding, function(key, value) {
		if (value['name'] == 'creator') {
			var temp = value['literal'];
			var name = temp.substring(0, temp.indexOf(':') - 1);
			var url = temp.substring(temp.indexOf(':') + 1);

			creator += "<a href='" + url + "' target=_blank>" + name + "</a> ";
			 /*else if (value == 'contact') {
				contact += "<a href='" + url + "' target=_blank>" + name + "</a>, ";
			}*/
		}
	});
	
	return creator;

		/*else if (value == 'keyword') {
			var temp = jsonBinding['uri'];
			temp = temp.substring(temp.indexOf("#") + 1);
			temp = decodeURIComponent(temp);
			
			//replaceAll + from temp
			while (temp.indexOf("+") != -1) {
				temp = temp.replace('+', " ");
			}
			keyword += "<a href='" + instanceURL_Tag + temp + "' target=_blank>" + temp + "</a>, ";
		}*/
	
}

/*<a href="<%= request.getAttribute("projectPath") %>" target="_blank" >Lower Mississippi Flood Project</a></br></br>
<p> An NSF funded effort to understand the recent Mississippi River flooding event.</p>*/

function loadProjectInfo(){
	var jsonObj = $.parseJSON($("#hidden_projectInfo").html());
	var map = new Object();
	for ( var i = 0; jsonObj.sparql.results.result && i < jsonObj.sparql.results.result.length; i=i+3) {
		var jsonBinding = jsonObj.sparql.results.result;
		
		//for ( var j = 0; j < jsonBinding.length; j=j+3) {
			var j = i;
			if(jsonBinding[j+1]['binding'][2]['uri']!=undefined) {
				if(jsonBinding[j+1]['binding'][2]['uri'].indexOf('ProjectURL')!=-1){
					map['url']= jsonBinding[j+2]['binding'][2]['literal'];				
				}else if(jsonBinding[j+1]['binding'][2]['uri'].indexOf('ProjectName')!=-1) {
					map['name']= jsonBinding[j+2]['binding'][2]['literal'];	
				}else if(jsonBinding[j+1]['binding'][2]['uri'].indexOf('ProjectDescription')!=-1) {
					map['description']= jsonBinding[j+2]['binding'][2]['literal'];	
				}	
			}
			
		//}		
  	}
	$('#projectName').html(map['name']);
	$('#projectDesc').html(map['description']);
	$('#projectTitle').html(map['name']);
	
}

var datasetDistribution;

function callOnLoad() {
	google.maps.event.addDomListener(window, 'load', initialize);

	google.load("visualization", "1", {packages:["corechart"]});
	google.setOnLoadCallback(drawChart);
	projectPath = $('#hidden_projectPath').html().trim();
	parseCreators();
	loadRecentUploads();
	//getTeamMembers();
	loadTableContent();
	
	
	
	var table = $('#table');
	table.treetable({
	    expandable: true,
	    onNodeCollapse: function() {
	      var node = this;
	      table.treetable("unloadBranch", node);
	    },
	    onNodeExpand: function() {
	      var node = this;
	      var code = node.row[0].innerHTML;
	      var test = code.substring(code.indexOf('uri')+4);
	      var tagID = test.substring(0,test.indexOf('"'));
	      
	      datasetDistribution = new Object();
	      
	      // Render loader/spinner while loading
	      $.ajax({
	        async: false, // Must be false, otherwise loadBranch happens after showChildren?
	        type : "GET",
	        url: "Contents",
	        dataType : "json",
			data : "tagID=" + tagID ,
	      }).done(function(jsonObj) {
	    	  
	    	  var uri = '';
	    	  var title = '';
	    	  var displayTitle = '';
	    	  var length = '';
	    	  var div_html_datasets = '';
	    	  for ( var i = 0; jsonObj.sparql.results.result && i < jsonObj.sparql.results.result.length; ++i) {
					var jsonBinding = jsonObj.sparql.results.result[i].binding;
					
					for ( var j = 0; j < jsonBinding.length; j++) {
		    			$.each(jsonBinding[j], function(key, value) {
		    				if (value == 'tagID') {
		    					uri = jsonBinding[j]['uri'];
		
		    				} else if (value == 'title') {
		    					title = jsonBinding[j]['literal'];
		    					displayTitle = title;
		
		    					if (title.indexOf("/") != -1) {
		    						displayTitle = title.substring(title.lastIndexOf("/") + 1);
		    					}
		
		    				} else if (value == 'length') {
		    					length = jsonBinding[j]['literal']['content'];
		
		    				}
		    				
		    			});
		    		}

		    		if (uri.indexOf("Collection") == -1) {
		    			    			
		    			div_html_datasets +='<tr data-tt-id="'+node.id+'-'+i+'" data-tt-parent-id="'+node.id+'">'
						+ '<td><span class="file"><a	href="'+projectPath+'/#dataset?id='
						+ uri + '" target ="_blank">' + displayTitle + '</a></span></td>'
						+'<td>'+roundNumber((length / 1024), 2)+' KB</td></tr>';
		    			
		    			var fileExt = displayTitle.split('.')[1];
		    			var mimeType = FindCategory(fileExt);
		    			if(datasetDistribution[mimeType]){
		    				datasetDistribution[mimeType] = datasetDistribution[mimeType] +1;
		    			}else{
		    				datasetDistribution[mimeType] = 1;
		    			}
		    			
		    		} else {
		    			div_html_datasets += '<tr data-tt-id="'+node.id+'-'+i+'" data-tt-parent-id="'+node.id+'">'
						+ '<td><span class="folder"><a	href="'+projectPath+'/#collection?uri='
						+ uri + '" target ="_blank">' + displayTitle + '</a></span></td><td>--</td></tr>'
						+'<tr data-tt-id="'+node.id+'-'+i+'-1" data-tt-parent-id="'+node.id+'-'+i+'">';
		    			
		    		}
	    	  	}
		        var rows = $(div_html_datasets).filter("tr");
		
		        rows.find(".directory").parents("tr").each(function() {
		        	$("#table").treetable("move", node.id, $(this).data("ttId"));
		        });
		
		        table.treetable("loadBranch", node, rows);
		        drawChart();
		      });
	    	}
	  });
	

	loadProjectInfo();
}







