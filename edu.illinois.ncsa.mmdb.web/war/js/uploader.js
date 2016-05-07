initUploader = function() {
	initializeUploader();
}

var uploadcount = 0;
var queue = [];

initializeUploader = function() {
	dropBox = document.getElementById("box");

	document.getElementById('files').addEventListener('change', handleFiles,
			false);

	dropBox.addEventListener("dragenter", function(event) {
		document.getElementById("box").style.backgroundColor = '#eeeeee';
		event.preventDefault();
		event.stopPropagation();
	}, true);

	dropBox.addEventListener("dragexit", function(event) {
		document.getElementById("box").style.backgroundColor = '#ffffff';
		event.preventDefault();
		event.stopPropagation();
	}, true);

	dropBox.addEventListener("dragover", function(event) {
		event.preventDefault();
		event.stopPropagation();
	}, true);

	dropBox.addEventListener("drop", function(event) {
		document.getElementById("box").style.backgroundColor = '#ffffff';
		drop(event);
		event.preventDefault();
		event.stopPropagation();
	}, false);

	uploadcount = 0;
}

function handleFiles(evt) {
	var files = evt.target.files;
	var count = files.length;

	for (var i = 0; i < count; i++) {
		if (files[i].size != 0) {
			addFileToQueue(files[i]);
		} else {
			var newDiv = document.createElement('div');
			newDiv.innerHTML = "Skipping 0 length file: " + files[i].name;
			document.getElementById("list").appendChild(newDiv);
		}
	}
}

function drop(evt) {
	var my = evt.dataTransfer.files;
	var files = evt.dataTransfer.files;
	var count = files.length;
	var folders = false;
	console.log("Uploading " + files.length + " files");
	if (files.length == 0) {
		console.log("Found folder");
		alert("The HTML5 drag and drop currently does not surport folders. Please try the Batch Upload Tools.");
	}
	for (var i = 0; i < count; i++) {
		console.log("file type" + files[i].type + " | " + files[i].size + " | "
				+ files[i].name);
		if (files[i].size > 1048576) {
			addFileToQueue(files[i]);
		} else {
			if (files[i].size == 0) {
				var newDiv = document.createElement('div');
				newDiv.innerHTML = "Skipping 0 length file: " + files[i].name;
				document.getElementById("list").appendChild(newDiv);
			} else {
				// Anonymous function to create 'closure' (pass files[i] by
				// value)
				(function(curFile) {
					var reader = new FileReader();

					reader.onload = function(result) {
						addFileToQueue(curFile);
					};
					reader.onerror = function() {
						console.log("Found folder");
						alert("The HTML5 drag and drop currently does not surport folders. You can drop multiple files here, or use SEAD's desktop upload tool.");
					};
					reader.readAsArrayBuffer(files[i]);
				})(files[i]);

			}
		}
	}
}

function addFileToQueue(file) {
	var name = file.name.toString();
	var size = file.size.toString();
	var uploadKey = null;
	var xhttp = new XMLHttpRequest();
	xhttp.onreadystatechange = function() {
	    if (xhttp.readyState == 4 && xhttp.status == 200) {
	      var keyObj = JSON.parse(xhttp.responseText);
	      uploadKey = keyObj.uploadkey;
	      
	    }
	  };
	  xhttp.open("GET", "UploadBlob", false);
	  xhttp.send();
	  
	// Anonymous function to create 'closure' (pass num by value)
	(function(num) {
		// Add to presenter interface
		console.log(name + " | " + num.toString());

		// Add to presenter interface
		dndAppletFileDropped(num, name, size);

		var fd = new FormData();
		fd.append(name, file);
		if(uploadKey!=null) {
		  fd.append("uploadkey",uploadKey);
		}

		var xhr = new XMLHttpRequest();
		xhr.upload.count = num;
		xhr.upload.name = name;
		xhr.upload.addEventListener("progress", uploadProgress, false);
		xhr.addEventListener("load", uploadComplete, false);
		xhr.addEventListener("error", uploadFailed, false);
		xhr.addEventListener("abort", uploadCanceled, false);
		// TODO if uploading multiple files in parallel
		// - files can be finished uploading in random order in case of
		// async.
		xhr.open("POST", "UploadBlob", true);
		xhr.send(fd);
	})(uploadcount++);
}

function uploadProgress(evt) {
	if (evt.lengthComputable) {
		var percentage = Math.round((evt.loaded * 100) / evt.total);
		console.log("Progress: " + percentage + " "
				+ evt.target.count.toString() + evt.target.name);
		dndAppletProgressIndex(percentage, evt.target.count);
	}
}

function uploadComplete(evt) {
	var replaced = new String(evt.target.responseText);
	var replacedall = replaced.replace(/<[^>]+>/g, "");
	var trimmed = replacedall.replace(/^\s\s*/, "").replace(/\s\s*$/, "");
	console.log("Complete: " + evt.target.upload.count.toString());
	dndAppletFileUploaded(trimmed, evt.target.upload.count.toString());
}

function uploadFailed(evt) {
	var newDiv = document.createElement('div');
	newDiv.innerHTML = "Error uploading file: " + evt.target.upload.name;
	document.getElementById("list").appendChild(newDiv);
}

function uploadCanceled(evt) {
	var newDiv = document.createElement('div');
	newDiv.innerHTML = "Aborted reading file: " + evt.target.upload.name;
	document.getElementById("list").appendChild(newDiv);
}
