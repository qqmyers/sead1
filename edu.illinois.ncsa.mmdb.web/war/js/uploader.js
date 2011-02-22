initUploader = function(){

	//new uploader('drop', 'status', 'uploader.php', 'list');
	initializeUploader();		
	
}

initializeUploader = function(){
	dropBox =  document.getElementById("box");
	
	dropBox.addEventListener("dragenter", function(event) {
		document.getElementById("box").style.backgroundColor='#eeeeee';
		event.stopPropagation(); 
		event.preventDefault();
	}, true);
	
	dropBox.addEventListener("dragexit", function(event) {
		document.getElementById("box").style.backgroundColor='#ffffff';
		event.stopPropagation(); 
		event.preventDefault();
	}, true);
	
	
	dropBox.addEventListener("dragover", function(event) {
		event.stopPropagation(); 
		event.preventDefault();
	}, true);

	dropBox.addEventListener("drop", drop, false); 

}

function drop(evt) {

	document.getElementById("box").style.backgroundColor='#ffffff';
	evt.stopPropagation();
	evt.preventDefault();

	var files = evt.dataTransfer.files;
	var count = files.length;

	for (var i = 0; i < count; i++) {

		var file = files[i],
		reader = new FileReader();
		
		var name = new String(file.name);
		var size = new String(file.size);
		
		//Add to presenter interface
		dndAppletFileDropped(name, size);
		
		reader.index = i;
		reader.file = file;
	
		//Handle upload
		reader.onloadend = LoadEnd;
		reader.onprogress = LoadProgress;
		
		reader.readAsBinaryString(file);
	
	}
}

function LoadEnd(evt) {
		
				file = evt.target.file;
				binary = evt.target.result;
		
				//Construct the POST request
				var xhr = false
				if(window.XMLHttpRequest){
				xhr = new XMLHttpRequest();
				}
				xhr.open('POST', 'UploadBlob', true);
				
				//Handle server response
				xhr.onreadystatechange = function(){
					if(xhr.readyState == 4){
						if(xhr.status == 200){
							//Remove <ol><li> ... </li></ol>
						    var replaced = new String(xhr.responseText);
						    var replacedall = replaced.replace(/<[^>]+>/g,"");
							var trimmed = replacedall.replace(/^\s\s*/,"").replace(/\s\s*$/,"");
							//Debugging - Check URI Response Received
							//var newFile  = document.createElement('div');
							//newFile.innerHTML = xhr.responseText;
							//document.getElementById("list").appendChild(newFile);
							dndAppletFileUploaded(trimmed);
						}
						else
							var alert;
					}
				}
				
				//Setup POST header content in data
				var boundary = 'xxxxxxxxx';
	 			var body = '--' + boundary + "\r\n";  
				body += "Content-Disposition: form-data; name=f1; filename=" + file.name + "\r\n";  
				//if null, add UNKNOWN mimetype so MimeMap can create one
				if (file.type)
					body += "Content-Type:" + file.type + "\r\n\r\n";
				else
					body += "Content-Type: application/octet-stream \r\n\r\n";  
				body += binary + "\r\n";  
				body += '--' + boundary + '--';      
				xhr.setRequestHeader('content-type', 'multipart/form-data; boundary=' + boundary);

				//Firefox only method
				if(xhr.sendAsBinary != null) { 
					xhr.sendAsBinary(body); 		
				}
				//Chrome 9+
				else {
						//Follow this Chrome dev forum for updates as its relatively new
						//http://code.google.com/p/chromium/issues/detail?id=35705
				        var data = new ArrayBuffer(body.length);
						var ui8a = new Uint8Array(data, 0);
						for (var i=0; i<body.length; i++) {
						ui8a[i] = (body.charCodeAt(i) & 0xff);
						}
						xhr.send(data);

				}
}

function LoadProgress(evt) {
	if (evt.lengthComputable) {
		var percentage = Math.round((evt.loaded * 100) / evt.total);
		dndAppletProgress(percentage);
	}

}


