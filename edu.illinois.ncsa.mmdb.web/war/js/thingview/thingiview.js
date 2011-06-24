/**
 * @author mr.doob / http://mrdoob.com/
 * based on http://papervision3d.googlecode.com/svn/trunk/as3/trunk/src/org/papervision3d/objects/primitives/Plane.as
 */

var Plane = function ( width, height, segments_width, segments_height ) {

	THREE.Geometry.call( this );

	var ix, iy,
	width_half = width / 2,
	height_half = height / 2,
	gridX = segments_width || 1,
	gridY = segments_height || 1,
	gridX1 = gridX + 1,
	gridY1 = gridY + 1,
	segment_width = width / gridX,
	segment_height = height / gridY;


	for( iy = 0; iy < gridY1; iy++ ) {

		for( ix = 0; ix < gridX1; ix++ ) {

			var x = ix * segment_width - width_half;
			var y = iy * segment_height - height_half;

			this.vertices.push( new THREE.Vertex( new THREE.Vector3( x, - y, 0 ) ) );

		}

	}

	for( iy = 0; iy < gridY; iy++ ) {

		for( ix = 0; ix < gridX; ix++ ) {

			var a = ix + gridX1 * iy;
			var b = ix + gridX1 * ( iy + 1 );
			var c = ( ix + 1 ) + gridX1 * ( iy + 1 );
			var d = ( ix + 1 ) + gridX1 * iy;

			this.faces.push( new THREE.Face4( a, b, c, d ) );
			this.uvs.push( [
						new THREE.UV( ix / gridX, iy / gridY ),
						new THREE.UV( ix / gridX, ( iy + 1 ) / gridY ),
						new THREE.UV( ( ix + 1 ) / gridX, ( iy + 1 ) / gridY ),
						new THREE.UV( ( ix + 1 ) / gridX, iy / gridY )
					] );

		}

	}

	this.computeCentroids();
	this.computeFaceNormals();
	this.sortFacesByMaterial();

};

Plane.prototype = new THREE.Geometry();
Plane.prototype.constructor = Plane;

/**
 * ThingIview
 */

initThingView = function(url, thing_width, thing_height){
	 
      thingiurlbase = "js/thingview";
      thingiview = new Thingiview("viewer");
      thingiview.setWidth(thing_width);
      thingiview.setHeight(thing_height);
      thingiview.setObjectColor('#C0D8F0');
	  thingiview.setBackgroundColor('#ffffff');
      thingiview.initScene();
      thingiview.loadOBJString(url);
      
      
  }
  
  hideThingView = function(){
   thingiview.hide();
   }
   
   saveImgThingView = function(){
   return thingiview.saveImage();

   }

Thingiview = function(containerId) {
  scope = this;
  
  this.containerId  = containerId;
  var container     = document.getElementById(containerId);
  var camera   = null;
  var scene    = null;
  var renderer = null;
  var object   = null;
  var plane    = null;
  
  var ambientLight     = null;
  var directionalLight = null;
  var pointLight       = null;
  
  var targetXRotation             = 0;
  var targetXRotationOnMouseDown  = 0;
  var mouseX                      = 0;
  var mouseXOnMouseDown           = 0;

  var targetYRotation             = 0;
  var targetYRotationOnMouseDown  = 0;
  var mouseY                      = 0;
  var mouseYOnMouseDown           = 0;

  var mouseDown                  = false;
  var mouseOver                  = false;
  
  var windowHalfX = window.innerWidth / 2;
  var windowHalfY = window.innerHeight / 2

  var view         = null;
  var infoMessage  = null;
  var progressBar  = null;
  var alertBox     = null;
  
  var timer        = null;
  var rotateTimer  = null;

  var cameraView = 'diagonal';
  var cameraZoom = 0;
  var rotate = false;
  var backgroundColor = '#606060';
  var objectMaterial = 'solid';
  var objectColor = 0xffffff;
  var showPlane = false;
  var isWebGl = false;
  var width;
  var height;
  var geometry;
  
  this.hide = function() {
    clearInterval(rotateTimer);
    rotateTimer = null;
    renderer.setSize(1, 1);
    renderer.domElement.width = renderer.domElement.height;
  }
  
  this.saveImage = function() { 
  	return renderer.domElement.toDataURL();
  }
  
  this.setWidth = function(new_width) {
  width = new_width;
  }

  this.setHeight = function(new_height) {
	 height = new_height;
  }

  this.initScene = function() {
    container.style.position = 'relative';
    container.innerHTML      = '';

  	camera = new THREE.Camera(50, width/ height, 1, 100000);
  	camera.updateMatrix();

  	scene  = new THREE.Scene();

    ambientLight = new THREE.AmbientLight(0x202020);
    scene.addLight(ambientLight);
    
    directionalLight = new THREE.DirectionalLight(0xffffff, 0.5);
    directionalLight.position.x = 1;
    directionalLight.position.y = 1;
    directionalLight.position.z = 2;
    directionalLight.position.normalize();
    scene.addLight(directionalLight);
    
    pointLight = new THREE.PointLight(0xffffff);
    pointLight.position.x = 0;
    pointLight.position.y = -25;
    pointLight.position.z = 10;
    scene.addLight(pointLight);

    progressBar = document.createElement('div');
    progressBar.style.position = 'absolute';
    progressBar.style.top = '0px';
    progressBar.style.left = '0px';
    progressBar.style.backgroundColor = 'red';
    progressBar.style.padding = '5px';
    progressBar.style.display = 'none';
    progressBar.style.overflow = 'visible';
    progressBar.style.whiteSpace = 'nowrap';
    progressBar.style.zIndex = 100;
    container.appendChild(progressBar);
    
    alertBox = document.createElement('div');
    alertBox.id = 'alertBox';
    alertBox.style.position = 'absolute';
    alertBox.style.top = '25%';
    alertBox.style.left = '25%';
    alertBox.style.width = '50%';
    alertBox.style.height = '50%';
    alertBox.style.backgroundColor = '#dddddd';
    alertBox.style.padding = '10px';
    alertBox.style.display = 'none';
    alertBox.style.zIndex = 100;
    container.appendChild(alertBox);

    if (showPlane) {
      loadPlaneGeometry();
    }
    
    this.setCameraView(cameraView);
    this.setObjectMaterial(objectMaterial);

    testCanvas = document.createElement('canvas');
    try {
      if (testCanvas.getContext('experimental-webgl')) {
        isWebGl = true;
        renderer = new THREE.WebGLRenderer();
      } else {
        renderer = new THREE.CanvasRenderer();
      }
    } catch(e) {
      renderer = new THREE.CanvasRenderer();
    }

  	renderer.setSize(width, height);
    renderer.domElement.style.backgroundColor = backgroundColor;
  	container.appendChild(renderer.domElement);
  	window.addEventListener('mousemove',      onRendererMouseMove,     false);    
    renderer.domElement.addEventListener('mouseover',      onRendererMouseOver,     false);
    renderer.domElement.addEventListener('mouseout',       onRendererMouseOut,      false);
  	renderer.domElement.addEventListener('mousedown',      onRendererMouseDown,     false);
    window.addEventListener('mouseup',        onRendererMouseUp,       false);

  	renderer.domElement.addEventListener('touchstart',     onRendererTouchStart,    false);
  	renderer.domElement.addEventListener('touchend',       onRendererTouchEnd,      false);
  	renderer.domElement.addEventListener('touchmove',      onRendererTouchMove,     false);

    renderer.domElement.addEventListener('DOMMouseScroll', onRendererScroll,        false);
  	renderer.domElement.addEventListener('mousewheel',     onRendererScroll,        false);
  	renderer.domElement.addEventListener('gesturechange',  onRendererGestureChange, false);
  }
  
  onRendererScroll = function(event) {
  event.preventDefault();

    var rolled = 0;

    if (event.wheelDelta === undefined) {
      rolled = -40 * event.detail;
    } else {
      rolled = event.wheelDelta;
    }

    if (rolled > 0) {
      scope.setCameraZoom(+10);
    } else {
      scope.setCameraZoom(-10);
    }
  }

  onRendererGestureChange = function(event) {
    event.preventDefault();

    if (event.scale > 1) {
      scope.setCameraZoom(+5);
    } else {
      scope.setCameraZoom(-5);
    }
  }

  onRendererMouseOver = function(event) {
    mouseOver = true;
    if (timer == null) {
      timer = setInterval(sceneLoop, 1000/60);
    }
  }

  onRendererMouseDown = function(event) {
    event.preventDefault();
  	mouseDown = true;
  	
  	clearInterval(rotateTimer);
    rotateTimer = null;
    
  	mouseXOnMouseDown = event.clientX - windowHalfX;
  	mouseYOnMouseDown = event.clientY - windowHalfY;

  	targetXRotationOnMouseDown = targetXRotation;
  	targetYRotationOnMouseDown = targetYRotation;
  }

  onRendererMouseMove = function(event) {

    if (mouseDown) {
  	  mouseX = event.clientX - windowHalfX;
  	  xrot = targetXRotationOnMouseDown + (mouseX - mouseXOnMouseDown) * 0.02;

  	  mouseY = event.clientY - windowHalfY;
  	  yrot = targetYRotationOnMouseDown + (mouseY - mouseYOnMouseDown) * 0.02;
  	  
  	  targetXRotation = xrot;
  	  targetYRotation = yrot;
	  }
  }

  onRendererMouseUp = function(event) {
    if (mouseDown) {
      mouseDown = false;
      if (!mouseOver) {
        clearInterval(timer);
        timer = null;
      }
    }
  }

  onRendererMouseOut = function(event) {
    if (!mouseDown) {
      clearInterval(timer);
      timer = null;
    }
    mouseOver = false;
  }

  onRendererTouchStart = function(event) {
    targetXRotation = object.rotation.z;
    targetYRotation = object.rotation.x;

    timer = setInterval(sceneLoop, 1000/60);

  	if (event.touches.length == 1) {
  		event.preventDefault();

  		mouseXOnMouseDown = event.touches[0].pageX - windowHalfX;
  		targetXRotationOnMouseDown = targetXRotation;

  		mouseYOnMouseDown = event.touches[0].pageY - windowHalfY;
  		targetYRotationOnMouseDown = targetYRotation;
  	}
  }

  onRendererTouchEnd = function(event) {
    clearInterval(timer);
    timer = null;
  }

  onRendererTouchMove = function(event) {
  	if (event.touches.length == 1) {
  		event.preventDefault();

  		mouseX = event.touches[0].pageX - windowHalfX;
  		targetXRotation = targetXRotationOnMouseDown + (mouseX - mouseXOnMouseDown) * 0.05;

  		mouseY = event.touches[0].pageY - windowHalfY;
  		targetYRotation = targetYRotationOnMouseDown + (mouseY - mouseYOnMouseDown) * 0.05;
  	}
  }

  sceneLoop = function() {
    if (object) {
      if (showPlane) {
        plane.rotation.z = object.rotation.z = (targetXRotation - object.rotation.z) * 0.2;
        plane.rotation.x = object.rotation.x = (targetYRotation - object.rotation.x) * 0.2;
      } else {
        object.rotation.z = (targetXRotation - object.rotation.z) * 0.2;
        object.rotation.x = (targetYRotation - object.rotation.x) * 0.2;
      }

      camera.updateMatrix();
      object.updateMatrix();
      
      if (showPlane) {
        plane.updateMatrix();
      }

    	renderer.render(scene, camera);
    }
  }

  rotateLoop = function() {
    targetXRotation += 0.05;
    sceneLoop();
  }

  this.setShowPlane = function(show) {
    showPlane = show;
    
    if (show) {
      if (scene && !plane) {
        loadPlaneGeometry();
      }
      plane.material[0].opacity = 1;
    } else {
      if (scene && plane) {
        plane.material[0].opacity = 0;
      }
    }
    
    sceneLoop();
  }

  this.setRotation = function(rotate) {
    rotation = rotate;
    
    if (rotate) {
      rotateTimer = setInterval(rotateLoop, 1000/60);
    } else {
      clearInterval(rotateTimer);
      rotateTimer = null;
    }
  }

  this.setCameraView = function(dir) {
    cameraView = dir;

    targetXRotation       = 0;
    targetYRotation       = 0;

    if (object) {
      object.rotation.x = 0;
      object.rotation.y = 0;
      object.rotation.z = 0;
    }

    if (showPlane && object) {
      plane.rotation.x = object.rotation.x;
      plane.rotation.y = object.rotation.y;
      plane.rotation.z = object.rotation.z;
    }
    
    if (dir == 'top') {
      camera.position.y = 0;
      camera.position.z = 100;

      camera.target.position.z = 0;
      if (showPlane) {
        plane.flipSided = false;
      }
    } else if (dir == 'side') {

      camera.position.y = -70;
      camera.position.z = 70;
      targetYRotation = -4.5;

      camera.target.position.z = 0;
      if (showPlane) {
        plane.flipSided = false;
      }
    } else if (dir == 'bottom') {
      camera.position.y = 0;
      camera.position.z = -100;

      camera.target.position.z = 0;
      if (showPlane) {
        plane.flipSided = true;
      }
    } else {
      camera.position.y = -70;
      camera.position.z = 70;

      camera.target.position.z = 0;
      if (showPlane) {
        plane.flipSided = false;
      }
    }

    mouseX            = targetXRotation;
    mouseXOnMouseDown = targetXRotation;
    
    mouseY            = targetYRotation;
    mouseYOnMouseDown = targetYRotation;
    
    sceneLoop();
  }

  this.setCameraZoom = function(factor) {
    cameraZoom = factor;
    
    if (cameraView == 'bottom') {
      if (camera.position.z + factor > 0) {
        factor = 0;
      }
    } else {
      if (camera.position.z - factor < 0) {
        factor = 0;
      }
    }
    
    if (cameraView == 'top') {
      camera.position.z -= factor;
    } else if (cameraView == 'bottom') {
      camera.position.z += factor;
    } else if (cameraView == 'side') {
      camera.position.y += factor;
      camera.position.z -= factor;
    } else {
      camera.position.y += factor;
      camera.position.z -= factor;
    }

    sceneLoop();
  }

  this.setObjectMaterial = function(type) {
    objectMaterial = type;

    loadObjectGeometry();
  }

  this.setBackgroundColor = function(color) {
    backgroundColor = color
    
    if (renderer) {
      renderer.domElement.style.backgroundColor = color;
    }
  }

  this.setObjectColor = function(color) {
    objectColor = parseInt(color.replace(/\#/g, ''), 16);
    
    loadObjectGeometry();
  }

  this.loadSTL = function(url) {
    scope.newWorker('loadSTL', url);
  }

  this.loadOBJ = function(url) {
    scope.newWorker('loadOBJ', url);
  }
  
  this.loadSTLString = function(STLString) {
    scope.newWorker('loadSTLString', STLString);
  }
  
  this.loadSTLBinary = function(STLBinary) {
    scope.newWorker('loadSTLBinary', STLBinary);
  }
  
  this.loadOBJString = function(OBJString) {
    scope.newWorker('loadOBJString', OBJString);
  }

  this.loadJSON = function(url) {
    scope.newWorker('loadJSON', url);
  }

  this.loadArray = function(array) {
    geometry = new STLGeometry(array);
    loadObjectGeometry();
    clearInterval(rotateTimer);
    rotateTimer = null;
    rotateTimer = setInterval(rotateLoop, 1000/60);
  }

  this.newWorker = function(cmd, param) {
    clearInterval(rotateTimer);
    rotateTimer = null;
  	
    var worker = new WorkerFacade(thingiurlbase + '/thingiloader.js');
    
    worker.onmessage = function(event) {
      if (event.data.status == "complete") {
        progressBar.innerHTML = 'Initializing geometry...';
        geometry = new STLGeometry(event.data.content);
        loadObjectGeometry();
        progressBar.innerHTML = '';
        progressBar.style.display = 'none';

        clearInterval(rotateTimer);
        rotateTimer = null;
        rotateTimer = setInterval(rotateLoop, 1000/60);
        log("finished loading " + geometry.faces.length + " faces.");
      } else if (event.data.status == "progress") {
        progressBar.style.display = 'block';
        progressBar.style.width = event.data.content;
      } else if (event.data.status == "message") {
        progressBar.style.display = 'block';
        progressBar.innerHTML = event.data.content;
      } else if (event.data.status == "alert") {
        scope.displayAlert(event.data.content);
      } else {
        alert('Error: ' + event.data);
        log('Unknown Worker Message: ' + event.data);
      }
    }

    worker.onerror = function(error) {
      log(error);
      error.preventDefault();
    }

    worker.postMessage({'cmd':cmd, 'param':param});
  }

  this.displayAlert = function(msg) {
    if (msg == "large object!") {
      msg = "This object is very large and will take a long time to load and be very slow."
      
      if (!isWebGl) {
        msg = msg + " For the best performance we recommend a <a href=\"http://www.khronos.org/webgl/wiki/Getting_a_WebGL_Implementation\">WebGL enabled browser</a> such as Minefield."
      }
    }
    
    msg = msg + "<br/><br/><center><input type=\"button\" value=\"Ok\" onclick=\"document.getElementById('alertBox').style.display='none'\"></center>"
    
    alertBox.innerHTML = msg;
    alertBox.style.display = 'block';
 
  }

  function loadPlaneGeometry() {
    plane = new THREE.Mesh(new Plane(100, 100, 10, 10), new THREE.MeshBasicMaterial({color:0xafafaf,wireframe:true}));
    scene.addObject(plane);
  }

  function loadObjectGeometry() {
    if (scene && geometry) {
      if (objectMaterial == 'wireframe') {
        material = new THREE.MeshBasicMaterial({color:objectColor,wireframe:true});
      } else {
        if (isWebGl) {
          material = new THREE.MeshLambertMaterial({color:objectColor, shading: THREE.FlatShading});
        } else {
          material = new THREE.MeshLambertMaterial({color:objectColor, shading: THREE.FlatShading});
        }
      }   

      if (object) {
        object.materials = [new THREE.MeshBasicMaterial({color:0xffffff, opacity:0})];
        scene.removeObject(object);        
      }

      object = new THREE.Mesh(geometry, material);
  		scene.addObject(object);

      if (objectMaterial != 'wireframe') {
        object.overdraw = true;
      }
      
      object.updateMatrix();
    
      targetXRotation = 0;
      targetYRotation = 0;

      sceneLoop();
    }
  }

};

var STLGeometry = function(STLArray) {
	THREE.Geometry.call(this);

	var scope = this;
	
  for (var i=0; i<STLArray[0].length; i++) {    
    v(STLArray[0][i][0], STLArray[0][i][1], STLArray[0][i][2]);
  }

  for (var i=0; i<STLArray[2].length; i++) {
    f3(STLArray[2][i][0], STLArray[2][i][1], STLArray[2][i][2]);
  }

  function v(x, y, z) {
    scope.vertices.push( new THREE.Vertex( new THREE.Vector3( x, y, z ) ) );
  }

  function f3(a, b, c) {
    scope.faces.push( new THREE.Face3( a, b, c ) );
  }

  this.computeCentroids();
	this.computeFaceNormals();
	this.sortFacesByMaterial();
}

STLGeometry.prototype = new THREE.Geometry();
STLGeometry.prototype.constructor = STLGeometry;

function log(msg) {
  if (this.console) {
    console.log(msg);
  }
}

var WorkerFacade;
if(!!window.Worker){
    WorkerFacade = (function(){
        return function(path){
            return new window.Worker(path);
        };
    }());
} else {
    WorkerFacade = (function(){
        var workers = {}, masters = {}, loaded = false;
        var that = function(path){
            var theworker = {}, loaded = false, callings = [];
            theworker.postToWorkerFunction = function(args){
                try{
                    workers[path]({"data":args});
                }catch(err){
                    theworker.onerror(err);
                }
            };
            theworker.postMessage = function(params){
                if(!loaded){
                    callings.push(params);
                    return;
                }
                theworker.postToWorkerFunction(params);
            };
            masters[path] = theworker;
            var scr = document.createElement("SCRIPT");
            scr.src = path;
            scr.type = "text/javascript";
            scr.onload = function(){
                loaded = true;
                while(callings.length > 0){
                    theworker.postToWorkerFunction(callings[0]);
                    callings.shift();
                }
            };
            document.body.appendChild(scr);
            
            var binaryscr = document.createElement("SCRIPT");
            binaryscr.src = thingiurlbase + '/binaryReader.js';
            binaryscr.type = "text/javascript";
            document.body.appendChild(binaryscr);
            
            return theworker;
        };
        that.fake = true;
        that.add = function(pth, worker){
            workers[pth] = worker;
            return function(param){
                masters[pth].onmessage({"data": param});
            };
        };
        that.toString = function(){
            return "FakeWorker('"+path+"')";
        };
        return that;
    }());
}