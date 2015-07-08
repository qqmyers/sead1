$(document).ready(function(){
	
	/* 
	Exports as CSV:
		Project Space
		Preprint Collections
		Published Collections
		Data Page Views
		Group Size
		Total Collections
		Total Datasets
		Total Datasize
		Group Homepage
		Version
	*/	

	$('#csvExport').on('click', function(){
		
		var data = [["Project Space","Preprint Collections","Published Collections","Data Page Views","Group Size"," Total Collections", "Total Datasets","Total Datasize","Group Homepage","Version"]];
		var length = $('.space-wrapper').length;
		$('.space-wrapper').each(function(i){

			var url = $(this).find('a').attr('href');
			var space = [];
			var project_space = $(this).find('h4').text();	
			var published_collections = $(this).find('.published').text();	
			var data_page_views = $(this).find('.views_raw').text();	
			var group_size = $(this).find('.users_raw').text();
			var total_collections = $(this).find('.collections_raw').text();
			var total_datasets = $(this).find('.datasets_raw').text();
			var total_datasize = $(this).find('.bytes').text();

			$.when(seadSpaces.doConfigAjax(url), seadSpaces.doInfoAjax(url)).then(function(config, info){
					
					var group_homepage = config[0]['project.url'];
					var version = info[0]['Version'];
					var public_preprint_collections = info[0]['Public Preprint Collections'];
					
					space.push(project_space+' '+url);
					space.push(public_preprint_collections);
					space.push(published_collections);
					space.push(data_page_views);
					space.push(group_size);
					space.push(total_collections);
					space.push(total_datasets);
					space.push(total_datasize);
					space.push(group_homepage);
					space.push(version);
					data.push(space);
					
					if(i===length-1){
						
						var csvContent = "data:text/csv;charset=utf-8,";
						data.forEach(function(infoArray, index){
						   dataString = infoArray.join(",");
						   csvContent += index < data.length ? dataString+ "\n" : dataString;
						});
						
						var encodedUri = encodeURI(csvContent);
						window.open(encodedUri);
						
					}

			});			
		}); 
	});
});