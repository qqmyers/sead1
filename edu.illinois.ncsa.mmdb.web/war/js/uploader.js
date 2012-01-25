initUploader = function(){
	initializeUploader();		
}

var uploadcount = 0;
var queue = [];

initializeUploader = function(){
	dropBox =  document.getElementById("box");
	
	document.getElementById('files').addEventListener('change', handleFiles, false);
	
	dropBox.addEventListener("dragenter", function(event) {
		document.getElementById("box").style.backgroundColor='#eeeeee';
		event.preventDefault();
		event.stopPropagation();
	}, true);
	
	dropBox.addEventListener("dragexit", function(event) {
		document.getElementById("box").style.backgroundColor='#ffffff';
		event.preventDefault();
		event.stopPropagation();
	}, true);
	
	
	dropBox.addEventListener("dragover", function(event) {
		event.preventDefault();
		event.stopPropagation();
	}, true);

	dropBox.addEventListener("drop", function(event) {
		document.getElementById("box").style.backgroundColor='#ffffff';		
		drop(event);
		event.preventDefault();
		event.stopPropagation();
	}, false); 
	
	uploadcount = 0;
}


function handleFiles(evt){
	var files = evt.target.files;
	var count = files.length;

	for (var i = 0; i < count; i++) {
		var name = new String(files[i].name);
		var size = new String(files[i].size);
		
		//Add to presenter interface
		dndAppletFileDropped(name, size);
		queue.push(files[i]);
	}
	
	uploadFile();
}

function drop(evt) {
	var files = evt.dataTransfer.files;
	var count = files.length;

	for (var i = 0; i < count; i++) {
		var name = files[i].name.toString();
		var size = files[i].size.toString();
		
		//Add to presenter interface
		dndAppletFileDropped(name, size);
		queue.push(files[i]);
	}
	
	uploadFile();
}

function uploadFile() {
	var file = queue.shift();
	
	var fd = new FormData();
	fd.append(file.name.toString(), file);

	var xhr = new XMLHttpRequest();
	xhr.upload.count=uploadcount++;
	xhr.upload.addEventListener("progress", uploadProgress, false);
	xhr.addEventListener("load", uploadComplete, false);
	xhr.addEventListener("error", uploadFailed, false);
	xhr.addEventListener("abort", uploadCanceled, false);
	// TODO if uploading multiple files in parallel
	// - files can be finished uploading in random order in case of async.
	xhr.open("POST", "UploadBlob", true);
	xhr.send(fd);
}

function uploadProgress(evt) {	
	if (evt.lengthComputable) {
		var percentage = Math.round((evt.loaded * 100) / evt.total);
		dndAppletProgressIndex(percentage, evt.target.count);
	}
}

function uploadComplete(evt) {
    var replaced = new String(evt.target.responseText);
    var replacedall = replaced.replace(/<[^>]+>/g,"");
	var trimmed = replacedall.replace(/^\s\s*/,"").replace(/\s\s*$/,"");
	
	dndAppletFileUploaded(trimmed, evt.target.upload.count.toString());
	if (queue.length > 0) {
		uploadFile();
	}
}

function uploadFailed(evt) {
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
	
	if (queue.length > 0) {
		uploadFile();
	}
}

function uploadCanceled(evt) {
	var newDiv  = document.createElement('div');
	newDiv.innerHTML = "Aborted reading file";
	document.getElementById("list").appendChild(newDiv);
}
