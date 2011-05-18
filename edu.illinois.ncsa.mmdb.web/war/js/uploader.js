initUploader = function(){

	initializeUploader();		
	
}

var uploading = false;
var count = 0;
var processing = null;
var queue = [];
var error_occurred = false;

initializeUploader = function(){
	dropBox =  document.getElementById("box");
	
	document.getElementById('files').addEventListener('change', handleFiles, false);
	
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

	count = 0;
}

var reader;

function uploadFile(){
	
	reader = new FileReader();
	reader.index = count;

	//Begin processing next in queue
	processing = queue.shift();
	
	reader.file = processing;
	
	//Handle upload cases
	reader.onprogress = LoadProgress;
	reader.onerror = LoadError;
	reader.onloadend = LoadEnd;
	reader.onabort = LoadAbort;
	
	//TODO detect folder, resume uploading
	
	reader.readAsBinaryString(processing);
	
}

function handleFiles(evt){
	var files = evt.target.files;
	
	for( var i = 0, f; f = files[i]; i++){
		
		var name = new String(files[i].name);
		var size = new String(files[i].size);
		
		//Add to presenter interface
		dndAppletFileDropped(name, size);
		
		queue.push(files[i]);
	}
	
	if(uploading == false){
		uploading = true;
		uploadFile();
	}
}

function drop(evt) {

	document.getElementById("box").style.backgroundColor='#ffffff';
	evt.stopPropagation();
	evt.preventDefault();

	var files = evt.dataTransfer.files;
	var count = files.length;

	for (var i = 0; i < count; i++) {
		
		/*var worker = new Worker('js/synchronous_uploader.js')
		
		worker.addEventListener('message', function(evt) {
		  alert("Worker said:");
		}, false);
		
		worker.postMessage(files[i]); // Send data to our worker.
		*/

		var name = new String(files[i].name);
		var size = new String(files[i].size);
		
		//Add to presenter interface
		dndAppletFileDropped(name, size);
		
		queue.push(files[i]);
		
	}
	
	if(uploading == false){
		uploading = true;
		uploadFile();
	}
	
}

var xhr;

function LoadEnd(evt) {
	if(!error_occurred){	
	
				index = evt.target.index;
				file = evt.target.file;
				binary = evt.target.result;
		
				//Construct the POST request
				xhr = false;
				//xhr.abort();
				
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
							
							dndAppletFileUploaded(trimmed, index.toString());
							count += 1;
							
							if(queue.length > 0){
								uploadFile();
							}
							else uploading = false;
						}
						else{
							//Retry Upload
							var newFile  = document.createElement('div');
							newFile.innerHTML = "Server Error, Status: " + xhr.status;
							document.getElementById("list").appendChild(newFile);
						}
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
	else {
		count += 1;
		
		if(queue.length > 0){
			uploadFile();
		}
		else 
			uploading = false;
		
		error_occurred = false;
		
	}
	
}

function LoadProgress(evt) {
	
	if (evt.lengthComputable) {
		var percentage = Math.round((evt.loaded * 100) / evt.total);
		//if (percentage < 100)
			dndAppletProgressIndex(percentage, evt.target.index);
	}

}

function LoadError(evt) {
	var newFile  = document.createElement('div');
	
	switch(evt.target.error.code) {
		case evt.target.error.NOT_FOUND_ERR:
			newFile.innerHTML = "File not found";
			document.getElementById("list").appendChild(newFile);
			break;
	
		case evt.target.error.NOT_READABLE_ERR:
			newFile.innerHTML = "ERROR: File size too large -" + evt.target.file.name;
			document.getElementById("list").appendChild(newFile);
			break;
	
		case evt.target.error.ABORT_ERR:
			newFile.innerHTML = "Abort Error";
			document.getElementById("list").appendChild(newFile);
			break; 
			
		case evt.target.error.ENCODING_ERR:
			newFile.innerHTML = "Encoding error";
			document.getElementById("list").appendChild(newFile);
			break;
			
		case evt.target.error.NO_MODIFICATION_ALLOWED_ERR:
			newFile.innerHTML = "No modification allowed error";
			document.getElementById("list").appendChild(newFile);
			break;
			
		case evt.target.error.INVALID_STATE_ERR:
			newFile.innerHTML = "Invalid state error";
			document.getElementById("list").appendChild(newFile);
			break;
			
		case evt.target.error.SYNTAX_ERR:
			newFile.innerHTML = "Syntax Error";
			document.getElementById("list").appendChild(newFile);
			break;
	
		default:
			newFile.innerHTML = "Read error";
			document.getElementById("list").appendChild(newFile);
	}
	
	reader.abort();
	error_occurred = true;
	
}

function LoadAbort(evt) {
	var newFile  = document.createElement('div');
	newFile.innerHTML = "Aborted reading file";
	document.getElementById("list").appendChild(newFile);

}






