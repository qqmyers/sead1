//var g_cubeOBJ = "v -0.500000 -0.500000 1.00000 v 0.500000 -0.500000 1.00000 v 0.500000 0.500000 1.00000 v -0.500000 0.500000 1.00000 v 0.500000 -0.500000 0.000000 v -0.500000 -0.500000 0.000000 v -0.500000 0.500000 0.000000 v 0.500000 0.500000 0.000000 vn  0.0  0.0  1.0 vn  0.0  0.0 -1.0 f 1 2 3 f 1 3 4 f 5 6 7 f 5 7 8 f 6 1 4 f 6 4 7 f 2 5 8 f 2 8 3 f 6 5 2 f 6 2 1 f 4 3 8 f 4 8 7"
//var g_cubeOBJparsed = g_cubeOBJ.split(" ");

var g_ctx;
var g_renderEngine;
var g_testObject;
var g_camera;
var g_clipSpace;
var g_FPSManager;

var g_keyPressed = [];

var g_vertices = 1;
var g_polygons;
var g_change_color;

var g_canvasWidth = 480;
var g_canvasHeight = 360;

var g_MonkeySpin = 0;
var g_frameCount = 0;

var g_xMax = 0; 
var g_yMax = 0;
var g_zMax = 0;
var g_scale = 1;

var g_objloading = 0;
var stopRotating;

function Matrix_html5(){

    var rowCount, columnCount;
    var tempArray = new Array(4);
    var arrayCount;
	
    for (arrayCount=0; arrayCount<4; arrayCount++){
        tempArray[arrayCount] = new Array(4);
	}

    for (rowCount = 0; rowCount < 4; rowCount++){
        for (columnCount = 0; columnCount < 4; columnCount++){
    	    if (columnCount != rowCount ) {
    	        tempArray[rowCount][columnCount] = 0; 	    
    	    }   else {
    	    	    tempArray[rowCount][columnCount] = 1;     
    	    }
    	    
        }
	}

    return tempArray;
}


function Normal(x,y,z){
	
  this.x = x;
  this.y = y;
  this.z = z;
  
}

function Vertex(x,y,z){

  this.x = x;
  this.y = y;
  this.z = z;
  this.w = 1;

}

function Polygon(verticesArray){
	
	this.vertex = verticesArray;
	this.color = "rgba("+Math.round(Math.random()*20)+", "+Math.round(Math.random()*20)+","+Math.round(Math.random()*50+200)+", 0.7)";
	
}

function FPSManager(){

 	this.dateNow; 	 
 	this.timeNow;
 	this.timeLater;
 	
 	this.update = function(){
 		this.dateNow = new Date();
 		this.timeNow = this.dateNow.getTime();
        g_ctx.fillText(1000/(this.timeNow-this.timeLater), 10, 50);
        this.timeLater = this.timeNow;
 	}
 	 	 
}

function OBJLoader(){

        g_ctx.fillText("Loading OBJ: " + g_objloading + "%", 10, 50);
		
}

function setInstructions(){

        g_ctx.fillText("LEFT KEY: Scale Down, RIGHT KEY: Scale Up", 5, 10);
		
}

function Object(){
	
	//this.verticesBuffer = [];
  	//this.indicesBuffer = [];
  	this.verticesArray = [];
  	this.polygonsArray = [];
  	this.centroidVerticesArray = [];
  	this.transformMatrix = new Matrix_html5();
  	this.normalsArray = [];
	
	this.num_polygons = 0;
	this.num_vertices = 0;
	
	this.findMax = function(x,y,z){
	    
	    if(Math.abs(x) > g_xMax){	
	        g_xMax = x;
		}
	    
		if (Math.abs(y) > g_yMax){
		    g_yMax = y;
		}
		
		if (Math.abs(z) > g_zMax){
			g_zMax = z;
		}
	}

  	this.computeCentroid = function(){ 
  	    var averageX, averageY, averageZ;
  	    var polygonCount;
  	    var vertexCount;
		
		this.num_polygons = this.polygonsArray.length;
		
  	    for (polygonCount = 0; polygonCount < this.polygonsArray.length; polygonCount++)
  	    {
  	   	    averageX = 0.0;
  	   	    averageY = 0.0;
  	   	    averageZ = 0.0;
  	   	   
  	   	    for (vertexCount = 0; vertexCount < this.polygonsArray[polygonCount].vertex.length; vertexCount++) {
  	   	   	    averageX += this.polygonsArray[polygonCount].vertex[vertexCount].x;
  	   	   	    averageY += this.polygonsArray[polygonCount].vertex[vertexCount].y;
  	   	   	    averageZ += this.polygonsArray[polygonCount].vertex[vertexCount].z;
  	   	    }
  	   	  
  	   	    averageX = averageX / this.polygonsArray[polygonCount].vertex.length;
  	   	    averageY = averageY / this.polygonsArray[polygonCount].vertex.length;
  	   	    averageZ = averageZ / this.polygonsArray[polygonCount].vertex.length;
  	    
  	        this.centroidVerticesArray.push( new Vertex(averageX,averageY,averageZ));
  	    }  	  
  	}
	
    this.loadOBJ = function(cubeOBJ2){
          	  
        var dataCount;
        var verticesCount;
        var verticesArray = [];
		var vfaceCount = 0;

		this.cubeOBJparsed = cubeOBJ2.split(/[\s\/]+/);
 	   
        for (dataCount = 0; dataCount < this.cubeOBJparsed.length; dataCount++){
            
		    //g_ctx.clearRect(0,0,g_canvasWidth,g_canvasHeight);
			g_objloading = ((dataCount+1)/this.cubeOBJparsed.length) * 100;
			//OBJLoader();
		    
		
		    if (this.cubeOBJparsed[dataCount] == "v"){
			
			    this.findMax(parseFloat(this.cubeOBJparsed[dataCount+1]),
          	  	parseFloat(this.cubeOBJparsed[dataCount+2]),
          	  	parseFloat(this.cubeOBJparsed[dataCount+3]));
			
          	    this.verticesArray.push(new Vertex(parseFloat(this.cubeOBJparsed[dataCount+1]),
          	  	parseFloat(this.cubeOBJparsed[dataCount+2]),
          	  	parseFloat(this.cubeOBJparsed[dataCount+3])));
				this.num_vertices = this.num_vertices + 1;
          	}
			
          	else if (this.cubeOBJparsed[dataCount] == "f"){	      
          	  	verticesArray = [];
          	  	verticesCount = dataCount+1;
          	  	   	   
          	  	while (this.cubeOBJparsed[verticesCount] != "f" && this.cubeOBJparsed[verticesCount] > 0 && this.cubeOBJparsed[verticesCount] < 1000000000000000 && this.cubeOBJparsed[verticesCount] != null && typeof(this.cubeOBJparsed[verticesCount]) !== 'undefined' && verticesCount < this.cubeOBJparsed.length  ) {    
          	  	   	vfaceCount = vfaceCount + 1;
          	  	   	verticesCount = verticesCount+1;  
          	    }

				verticesCount = verticesCount - vfaceCount;

				if (vfaceCount >= 6){
				    vfaceCount = 0;
				    while (this.cubeOBJparsed[verticesCount] != "f" && this.cubeOBJparsed[verticesCount] > 0 && this.cubeOBJparsed[verticesCount] < 1000000000000000 && this.cubeOBJparsed[verticesCount] != null && typeof(this.cubeOBJparsed[verticesCount]) !== 'undefined' && verticesCount < this.cubeOBJparsed.length  ) {    
					    vfaceCount = vfaceCount + 1;
						//if it's odd, then push that value... 
						if (vfaceCount%2 != 0){
						    verticesArray.push(this.verticesArray[this.cubeOBJparsed[verticesCount]-1]);   
          	  	   	    }
						verticesCount = verticesCount+1;  
						
          	        }
				}
				else{
				    while (this.cubeOBJparsed[verticesCount] != "f" && this.cubeOBJparsed[verticesCount] > 0 && this.cubeOBJparsed[verticesCount] < 10000000000000000 && this.cubeOBJparsed[verticesCount] != null && typeof(this.cubeOBJparsed[verticesCount]) !== 'undefined' && verticesCount < this.cubeOBJparsed.length  ) {    
					    verticesArray.push(this.verticesArray[this.cubeOBJparsed[verticesCount]-1]);   
          	  	   	    verticesCount = verticesCount+1;  
          	        }
				}
					
				vfaceCount = 0;
          	  	this.polygonsArray.push(new Polygon(verticesArray));          
          	}
        }

        this.computeCentroid();	 	
    }
	
	this.getVertices = function(){

	    return this.num_vertices;
	
	}
	
	this.getPolygons = function(){

	    return this.num_polygons;
	
	}

}
  
function Camera(){
	
	this.transformMatrix = new Matrix_html5(); 
  
}

function RenderINT() {

   this.clipSpace = new Matrix_html5();
   this.clipSpace = [ [1.305, 0, 0, 0],
   	                  [0, 1.740, 0, 0],
   	                  [0, 0, 1.002, 1],
   	                  [0,0,-2.002, 0]
                    ];
					
	g_camera.transformMatrix = [ [1, 0, 0, 0],
									 [0, 1, 0, 0],
									 [0, 0, g_scale, 0],
									 [0,0,0, 0]
								   ];
   	   
   	    var polygonCount;
 	    var zClip;
        var zDepthOfPolygons = [];
        var zDepthOfPolygonsSorted = [];
        var polygonIndicesDepthSorted = [];
 	 	 
 	    var finalMatrix = MatrixMultiplication(g_testObject.transformMatrix,g_camera.transformMatrix);
 	    finalMatrix = MatrixMultiplication(finalMatrix, this.clipSpace);
         
 	    for (polygonCount = 0; polygonCount < g_testObject.polygonsArray.length;polygonCount++){
 	 	    zClip = g_testObject.centroidVerticesArray[polygonCount].x*finalMatrix[0][2] 
 	 	          + g_testObject.centroidVerticesArray[polygonCount].y*finalMatrix[1][2] 
 	 	          + g_testObject.centroidVerticesArray[polygonCount].z*finalMatrix[2][2] 
 	 	          + g_testObject.centroidVerticesArray[polygonCount].w*finalMatrix[3][2];
 	 	         
 	 	    zDepthOfPolygons.push(zClip);
 	    }

        zDepthOfPolygonsSorted = zDepthOfPolygons.slice();
        zDepthOfPolygonsSorted.sort(function(a,b){return a - b});
         
        var sortCount, chaosCount;

        for (sortCount = 0; sortCount < zDepthOfPolygonsSorted.length; sortCount++){
            for (chaosCount = 0; chaosCount < zDepthOfPolygons.length; chaosCount++){
          	    if (zDepthOfPolygonsSorted[sortCount] == zDepthOfPolygons[chaosCount]){
          	        polygonIndicesDepthSorted.push(chaosCount);
          	        zDepthOfPolygons[chaosCount] = 0;  
          	    }
            }
        }
 
        var drawingPolygon;
        var xScreen = [];
        var yScreen = [];
        var clipVertex;
        var vertexCount;
		
		var changedColor = 0;
	
        for (polygonCount = 0; polygonCount < polygonIndicesDepthSorted.length;polygonCount++){
	
 	        drawingPolygon = g_testObject.polygonsArray[polygonIndicesDepthSorted[polygonCount]];
 	        xScreen = [];
 	        yScreen = [];
			
			if (g_change_color > 0){
			    switch(g_change_color){
			    case 1:
   				    drawingPolygon.color = "rgba("+Math.round(Math.random()*50+200)+", "+Math.round(Math.random()*20)+","+Math.round(Math.random()*20)+", 0.7)";
			        break;
				
				case 2:
				    drawingPolygon.color = "rgba("+Math.round(Math.random()*20)+", "+Math.round(Math.random()*50+200)+","+Math.round(Math.random()*20)+", 0.7)";
				    break;
				
				case 3:
				    drawingPolygon.color = "rgba("+Math.round(Math.random()*20)+", "+Math.round(Math.random()*20)+","+Math.round(Math.random()*50+200)+", 0.7)";
				    break;
					
			    case 4:
				    drawingPolygon.color = "rgba("+Math.round(Math.random()*20)+", "+Math.round(Math.random()*20)+","+Math.round(Math.random()*20)+", 0.7)";
				    break;
					
			    case 5:
				    drawingPolygon.color = "rgba("+Math.round(Math.random()*35+220)+", "+Math.round(Math.random()*35+220)+","+Math.round(Math.random()*35+220)+", 0.7)";
				    break;
				}
			    
			  changedColor = 1;
			}
 	  
 	        for (vertexCount = 0; vertexCount < drawingPolygon.vertex.length; vertexCount++){
 	      	    clipVertex = vertexMultiplication(drawingPolygon.vertex[vertexCount],finalMatrix);
 	      	    xScreen.push ( (clipVertex.x*g_canvasWidth)/(2*clipVertex.w) + g_canvasWidth/2 );
 	      	    yScreen.push ( (clipVertex.y*g_canvasHeight)/(2*clipVertex.w) + g_canvasHeight/2 );
 	        }
 	      
 	        this.drawPolygonINT(xScreen,yScreen,drawingPolygon.color);    
 	    }   
		
		if (changedColor == 1){
		    g_change_color = 0;
		}
		
}

   drawPolygonINT = function(xCoords,yCoords, color){
   	   
   	    var coordsCounter = xCoords.length;
   	   
 	 	g_ctx.fillStyle = color;
        g_ctx.beginPath();
        g_ctx.moveTo(xCoords[0],yCoords[0]);
		
        for (coordsCounter = 1; coordsCounter < xCoords.length; coordsCounter++){
            g_ctx.lineTo(xCoords[coordsCounter],yCoords[coordsCounter]);
	    }
 
        g_ctx.moveTo(xCoords[0],yCoords[0]);
        g_ctx.stroke();
        g_ctx.fill();
        g_ctx.closePath();
 	}
	
//////////////////////////////////////////////////////////////////////////
//    RENDERING   ///////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////
	
function RenderEngine(){

   this.clipSpace = new Matrix_html5();   
   this.clipSpace = [ [1.305, 0, 0, 0],
   	                  [0, 1.740, 0, 0],
   	                  [0, 0, 1.002, 1],
   	                  [0,0,2.002, 0]
                    ];
                
   this.render = function(){
   
       // this.clipSpace[0][0] = g_scale;
		//this.clipSpace[1][1] = g_scale;
		//this.clipSpace[2][2] = g_scale;
   	   
   	    var polygonCount;
 	    var zClip;
        var zDepthOfPolygons = [];
        var zDepthOfPolygonsSorted = [];
        var polygonIndicesDepthSorted = [];
 	 	 
 	    var finalMatrix = MatrixMultiplication(g_testObject.transformMatrix,g_camera.transformMatrix);
 	    finalMatrix = MatrixMultiplication(finalMatrix, this.clipSpace);
         
 	    for (polygonCount = 0; polygonCount < g_testObject.polygonsArray.length;polygonCount++){
 	 	    zClip = g_testObject.centroidVerticesArray[polygonCount].x*finalMatrix[0][2] 
 	 	          + g_testObject.centroidVerticesArray[polygonCount].y*finalMatrix[1][2] 
 	 	          + g_testObject.centroidVerticesArray[polygonCount].z*finalMatrix[2][2] 
 	 	          + g_testObject.centroidVerticesArray[polygonCount].w*finalMatrix[3][2];
 	 	         
 	 	    zDepthOfPolygons.push(zClip);
 	    }

        zDepthOfPolygonsSorted = zDepthOfPolygons.slice();
        zDepthOfPolygonsSorted.sort(function(a,b){return a - b});
         
        var sortCount, chaosCount;

        for (sortCount = 0; sortCount < zDepthOfPolygonsSorted.length; sortCount++){
            for (chaosCount = 0; chaosCount < zDepthOfPolygons.length; chaosCount++){
          	    if (zDepthOfPolygonsSorted[sortCount] == zDepthOfPolygons[chaosCount]){
          	        polygonIndicesDepthSorted.push(chaosCount);
          	        zDepthOfPolygons[chaosCount] = 0;  
          	    }
            }
        }
 
        var drawingPolygon;
        var xScreen = [];
        var yScreen = [];
        var clipVertex;
        var vertexCount;
		
		var changedColor = 0;
	
        for (polygonCount = 0; polygonCount < polygonIndicesDepthSorted.length;polygonCount++){
	
 	        drawingPolygon = g_testObject.polygonsArray[polygonIndicesDepthSorted[polygonCount]];
 	        xScreen = [];
 	        yScreen = [];
 	  
 	        for (vertexCount = 0; vertexCount < drawingPolygon.vertex.length; vertexCount++){
 	      	    clipVertex = vertexMultiplication(drawingPolygon.vertex[vertexCount],finalMatrix);
 	      	    xScreen.push ( (clipVertex.x*g_canvasWidth)/(2*clipVertex.w) + g_canvasWidth/2 );
 	      	    yScreen.push ( (clipVertex.y*g_canvasHeight)/(2*clipVertex.w) + g_canvasHeight/2 );
 	        }
 	      
 	        this.drawPolygon(xScreen,yScreen,drawingPolygon.color);    
 	    }   
		
    }
   
   this.drawPolygon = function(xCoords,yCoords, color){
   	   
   	    var coordsCounter = xCoords.length;
   	   
 	 	g_ctx.fillStyle = color;
        g_ctx.beginPath();
        g_ctx.moveTo(xCoords[0],yCoords[0]);
		
        for (coordsCounter = 1; coordsCounter < xCoords.length; coordsCounter++){
            g_ctx.lineTo(xCoords[coordsCounter],yCoords[coordsCounter]);
	    }
 
        g_ctx.moveTo(xCoords[0],yCoords[0]);
        g_ctx.stroke();
        g_ctx.fill();
        g_ctx.closePath();
 	}
	
}
    
function TranslationMatrix(x,y,z) {
	
    var outputMatrix = new Matrix_html5();

    outputMatrix[3][0] = x;
    outputMatrix[3][1] = y;
    outputMatrix[3][2] = z;

    return outputMatrix;
     
}

function vertexMultiplication(vertex,matrix){

    var outputVertex = new Vertex(0,0,0);

    outputVertex.x = vertex.x*matrix[0][0]+vertex.y*matrix[1][0]+vertex.z*matrix[2][0]+vertex.w*matrix[3][0];
    outputVertex.y = vertex.x*matrix[0][1]+vertex.y*matrix[1][1]+vertex.z*matrix[2][1]+vertex.w*matrix[3][1];
    outputVertex.z = vertex.x*matrix[0][2]+vertex.y*matrix[1][2]+vertex.z*matrix[2][2]+vertex.w*matrix[3][2];
    outputVertex.w = vertex.x*matrix[0][3]+vertex.y*matrix[1][3]+vertex.z*matrix[2][3]+vertex.w*matrix[3][3];

    return outputVertex;

}
  
function MatrixMultiplication(MatrixA,MatrixB) {
	
    var rowCount, columnCount;
    var outputMatrix = new Matrix_html5();

    for (rowCount = 0; rowCount < 4; rowCount++){
        for (columnCount = 0; columnCount < 4; columnCount++) {
    	    outputMatrix[rowCount][columnCount] = MatrixA[rowCount][0]*MatrixB[0][columnCount] +
    	                                          MatrixA[rowCount][1]*MatrixB[1][columnCount] +
    	                                          MatrixA[rowCount][2]*MatrixB[2][columnCount] +
    	                                          MatrixA[rowCount][3]*MatrixB[3][columnCount];
	    }
    }

    return outputMatrix;
     
}


function RotationXMatrix(angle) {

    var outputMatrix = new Matrix_html5();

    outputMatrix[0][0] = 1;
    outputMatrix[0][1] = 0;
    outputMatrix[0][2] = 0;
    outputMatrix[0][3] = 0;
    outputMatrix[1][0] = 0;
    outputMatrix[1][1] = Math.cos(angle);
    outputMatrix[1][2] = Math.sin(angle);
    outputMatrix[1][3] = 0;
    outputMatrix[2][0] = 0;
    outputMatrix[2][1] = -Math.sin(angle);
    outputMatrix[2][2] = Math.cos(angle);
    outputMatrix[2][3] = 0;
    outputMatrix[3][0] = 0;
    outputMatrix[3][1] = 0;
    outputMatrix[3][2] = 0;
    outputMatrix[3][3] = 1;

    return outputMatrix;
     
}


function RotationYMatrix(angle) {

    var outputMatrix = new Matrix_html5();

    outputMatrix[0][0] = Math.cos(angle);
    outputMatrix[0][1] = 0;
    outputMatrix[0][2] = -Math.sin(angle);
    outputMatrix[0][3] = 0;
    outputMatrix[1][0] = 0;
    outputMatrix[1][1] = 1;
    outputMatrix[1][2] = 0;
    outputMatrix[1][3] = 0;
    outputMatrix[2][0] = Math.sin(angle);
    outputMatrix[2][1] = 0;
    outputMatrix[2][2] = Math.cos(angle);
    outputMatrix[2][3] = 0;
    outputMatrix[3][0] = 0;
    outputMatrix[3][1] = 0;
    outputMatrix[3][2] = 0;
    outputMatrix[3][3] = 1;

    return outputMatrix;
     
}


function RotationZMatrix(angle) {
	
    var outputMatrix = new Matrix_html5();

    outputMatrix[0][0] = Math.cos(angle);
    outputMatrix[0][1] = Math.sin(angle);
    outputMatrix[0][2] = 0;
    outputMatrix[0][3] = 0;
    outputMatrix[1][0] = -Math.sin(angle);
    outputMatrix[1][1] = Math.cos(angle);
    outputMatrix[1][2] = 0;
    outputMatrix[1][3] = 0;
    outputMatrix[2][0] = 0;
    outputMatrix[2][1] = 0;
    outputMatrix[2][2] = 1;
    outputMatrix[2][3] = 0;
    outputMatrix[3][0] = 0;
    outputMatrix[3][1] = 0;
    outputMatrix[3][2] = 0;
    outputMatrix[3][3] = 1;

    return outputMatrix;
     
}
  
function initialize(cubeOBJ) {

    g_ctx = document.getElementById("canvas").getContext("2d");

    g_testObject = new Object();
    g_camera = new Camera();
    //g_FPSManager = new FPSManager();
    g_renderEngine = new RenderEngine();

    window.document.onkeypress = onKeyPress;

    g_testObject.loadOBJ(cubeOBJ);
	g_ctx.clearRect(0,0,g_canvasWidth,g_canvasHeight);
	/*g_testObject.transformMatrix = [ [1, 1, 1, 0],
									 [0, 0, 0, 0],
									 [0, 0, 0, 0],
									 [0,0,0, 0]
								   ];
    
	*/
	g_testObject.transformMatrix = RotationXMatrix(1.57);
    g_testObject.transformMatrix = MatrixMultiplication(g_testObject.transformMatrix,RotationYMatrix(g_MonkeySpin));
    g_testObject.transformMatrix = MatrixMultiplication(g_testObject.transformMatrix,TranslationMatrix(0,0,-5));
    /////g_testObject.transformMatrix = TranslationMatrix(0,0,-5);
    /////g_testObject.transformMatrix = RotationXMatrix(1);
    /////g_testObject.transformMatrix = RotationXMatrix(g_testZ);
    /////g_testObject.transformMatrix = MatrixMultiplication(g_testObject.transformMatrix,TranslationMatrix(0,0,-3));
    g_camera.transformMatrix = new Matrix_html5();
	g_camera.transformMatrix = [ [1, 0, 0, 0],
									 [0, 1, 0, 0],
									 [0, 0, g_scale, 0],
									 [0,0,0, 0]
								   ];
    
	//g_renderEngine.render();

    //60 FPS? Ha, I wish
    rotate = setInterval(nextFrame, 16.67);
	
}


function nextFrame() {

    g_frameCount++;
    stopRotating = 0;
       
    g_ctx.clearRect(0,0,g_canvasWidth,g_canvasHeight);
    g_testObject.transformMatrix = new Matrix_html5();
    g_testObject.transformMatrix = RotationXMatrix(1.57);
    g_testObject.transformMatrix = MatrixMultiplication(g_testObject.transformMatrix,RotationYMatrix(g_frameCount/200));
    g_testObject.transformMatrix = MatrixMultiplication(g_testObject.transformMatrix,TranslationMatrix(0,0,-5));
   
   //setInstructions();
   g_renderEngine.render();
    //g_FPSManager.update();
	
 
}

/**
 * A key has been released, so handle it
 */
function onKeyPress(event) {
  event = event || window.event;
  g_keyPressed[event.keyCode] = false;
  
  if(event.keyCode == 37){
      g_ctx.clearRect(0,0,g_canvasWidth,g_canvasHeight);
      g_scale = g_scale * 1.5;
      RenderINT();
  }
  else if(event.keyCode == 39){
      g_ctx.clearRect(0,0,g_canvasWidth,g_canvasHeight);
      g_scale = g_scale * .75;
      RenderINT();
  }
  else if(event.keyCode == 38){
  
  if(stopRotating == 0){
     clearInterval(rotate);
     stopRotating = 1;
     }
     else {
     clearInterval(rotate);
     rotate = setInterval(nextFrame, 16.67);
     stopRotating = 0;
     }
  }
}