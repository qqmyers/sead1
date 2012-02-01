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
	var folders = false;
	console.log("Uploading " + files.length + " files");
	if (files.length == 0) {
		folders = true;
	}
	for (var i = 0; i < count; i++) {
		console.log("file type" + files[i].type + " | " + files[i].size);
		if (files[i].type == "" || files[i].size == 0) {
			console.log("Found folder")
			folders = true;
		} else {
			var name = files[i].name.toString();
			var size = files[i].size.toString();
			
			//Add to presenter interface
			dndAppletFileDropped(name, size);
			queue.push(files[i]);
		}
	}
	if (queue.length > 0) {
		uploadFile();
	}
	if (folders) {
		alert("The HTML5 drag and drop currently does not surport folders. Please try the Java uploader.")
	}
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
	var newDiv  = document.createElement('div');	
	newDiv.innerHTML = "Error uploading file.";
	document.getElementById("list").appendChild(newDiv);

	if (queue.length > 0) {
		uploadFile();
	}
}

function uploadCanceled(evt) {
	var newDiv  = document.createElement('div');
	newDiv.innerHTML = "Aborted reading file";
	document.getElementById("list").appendChild(newDiv);
}
