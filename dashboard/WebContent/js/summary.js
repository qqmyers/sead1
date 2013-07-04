var projectPath = '';

function initialize() {
	var mapProp = {
		center : new google.maps.LatLng(29.1536, -89.2508),
		zoom : 5,
		mapTypeId : google.maps.MapTypeId.HYBRID  
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
		}
	}
	
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
	var isDeleted = null;
	
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
				} else if (value == 'deleted') {
					isDeleted = jsonBinding[j]['uri'];
				}
			});
		}
		
		if(!tagURISet[uri] && isDeleted ==null) { 
			tagURISet[uri] = true;
	
			div_html += '<tr data-tt-id="'+i+'">'
						+ '<td><span class="folder"><a	href="'+projectPath+'/#collection?uri='
						+ uri + '" target ="_blank">' + displayTitle + '</a></span></td><td>--</td>'
						+'<tr data-tt-id="'+i+'-1" data-tt-parent-id="'+i+'">';
		
		}else if(isDeleted !=null){
			isDeleted = null;
		}
	}

	$("#table tbody").html(div_html);
	
	
}	

var map = new Object();
function parseCreators() {
	
	var creatorURI = '';
	var creatorName = '';
	
	var div_html ='<h3>Team Members</h3>';
	var obj = $.parseJSON($("#hidden_creators").html());
	
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
		
		displayTitleAfter = displayTitle.length>12?displayTitle.substring(0,11)+'...':displayTitle;
		div_html += '<div class="media">'
					+ '<a class="pull-left" href="'+projectPath+'/#dataset?id='+uri+'" target="_blank"> <img style="width: 100px; height: 100px;" title="'+displayTitle+'" class="media-object" src="'+projectPath+'/api/image/preview/small/'
					+ uri + '" /> </a>'
					+'<div class="media-body">'
					+'<a href="'+projectPath+'/#dataset?id='+uri+'" target="_blank" title="'+displayTitle+'">'+displayTitleAfter
					+'</a></br>'+map[creator]+'</br>'+date[0] +' '+ date[1]+' '+date[2] +' '+ date[3]+'</div></div>';
		
		//images/nopreview-100.gif	

	}
	$("#recentuploads").html(div_html);
}

function roundNumber(num, dec) {
	var result = Math.round(num * Math.pow(10, dec)) / Math.pow(10, dec);
	return result;
}

function getTeamMembers() {
	var div_html ='<h3>Team Members</h3>';
	
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
	

}

function getAttributesForHomePage(jsonBinding) {
	var creator = '';
	$.each(jsonBinding, function(key, value) {
		if (value['name'] == 'creator') {
			var temp = value['literal'];
			var name = temp.substring(0, temp.indexOf(':') - 1);
			var url = temp.substring(temp.indexOf(':') + 1);

			creator += "<a href='" + url + "' target=_blank>" + name + "</a> ";
		}
	});
	
	return creator;	
	
}

function loadProjectInfo(){
	var jsonObj = $.parseJSON($("#hidden_projectInfo").html());
	var map = new Object();
	var nameURI;
	var descURI;
	var urlURI;
	for ( var i = 0; jsonObj.sparql.results.result && i < jsonObj.sparql.results.result.length; i++) {
		
		//for(var j =0; j<jsonObj.sparql.results.result[i].binding.length; j=j+3) {
			var jsonBinding = jsonObj.sparql.results.result[i].binding;
			
			if(jsonBinding[2]['uri']!=undefined) {
				if(jsonBinding[2]['uri'].indexOf('ProjectURL')!=-1){
					urlURI = jsonBinding[0]['uri'];						
				}else if(jsonBinding[2]['uri'].indexOf('ProjectName')!=-1) {
					nameURI = jsonBinding[0]['uri'];	
				}else if(jsonBinding[2]['uri'].indexOf('ProjectDescription')!=-1) {
					descURI = jsonBinding[0]['uri'];	
				}
			}
		//}
		
		/*if(jsonBinding[j]['binding'][2]['uri']!=undefined) {
			if(jsonBinding[j]['binding'][2]['uri'].indexOf('ProjectURL')!=-1){
				map['url']= jsonBinding[j+1]['binding'][2]['literal'];				
			}else if(jsonBinding[j]['binding'][2]['uri'].indexOf('ProjectName')!=-1) {
				map['name']= jsonBinding[j+1]['binding'][2]['literal'];	
			}else if(jsonBinding[j]['binding'][2]['uri'].indexOf('ProjectDescription')!=-1) {
				map['description']= jsonBinding[j+1]['binding'][2]['literal'];	
			}	
		}	*/		
		
  	}
	
	for ( var i = 0; jsonObj.sparql.results.result && i < jsonObj.sparql.results.result.length; i++) {
		
		//for(var j =0; j<jsonObj.sparql.results.result[i].binding.length; j=j+3)
		var jsonBinding = jsonObj.sparql.results.result[i].binding;
		//http://cet.ncsa.uiuc.edu/2007/mmdb/configuration/value
		if(jsonBinding[0]['uri']!=undefined) {
			if(jsonBinding[0]['uri'] == urlURI && jsonBinding[1]['uri'] == 'http://cet.ncsa.uiuc.edu/2007/mmdb/configuration/value'){
				map['url'] = jsonBinding[2]['literal'];						
			}else if(jsonBinding[0]['uri'] == nameURI && jsonBinding[1]['uri'] == 'http://cet.ncsa.uiuc.edu/2007/mmdb/configuration/value'){
				map['name'] = jsonBinding[2]['literal'];	
			}else if(jsonBinding[0]['uri'] == descURI && jsonBinding[1]['uri'] == 'http://cet.ncsa.uiuc.edu/2007/mmdb/configuration/value'){
				map['description'] = jsonBinding[2]['literal'];	
			}
		}
		
		
		/*if(jsonBinding[j]['binding'][2]['uri']!=undefined) {
			if(jsonBinding[j]['binding'][2]['uri'].indexOf('ProjectURL')!=-1){
				map['url']= jsonBinding[j+1]['binding'][2]['literal'];				
			}else if(jsonBinding[j]['binding'][2]['uri'].indexOf('ProjectName')!=-1) {
				map['name']= jsonBinding[j+1]['binding'][2]['literal'];	
			}else if(jsonBinding[j]['binding'][2]['uri'].indexOf('ProjectDescription')!=-1) {
				map['description']= jsonBinding[j+1]['binding'][2]['literal'];	
			}	
		}	*/		
		
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
	    	  for ( var i = 0; jsonObj.sparql.results.result && (i < jsonObj.sparql.results.result.length || jsonObj.sparql.results.result.binding); ++i) {
	    		  var jsonBinding;
	    		  if(jsonObj.sparql.results.result.length)
					jsonBinding = jsonObj.sparql.results.result[i].binding;
	    		  else
	    			  jsonBinding = jsonObj.sparql.results.result.binding; 
					
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
		    					if(length<0)
		    						length = 0- length;
		
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
		    		if(jsonObj.sparql.results.result.binding)	
		    			break;
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