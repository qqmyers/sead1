/*
Copyright (C) 2009  Ilmari Heikkinen <ilmari.heikkinen@gmail.com>

Permission is hereby granted, free of charge, to any person
obtaining a copy of this software and associated documentation
files (the "Software"), to deal in the Software without
restriction, including without limitation the rights to use,
copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the
Software is furnished to do so, subject to the following
conditions:

The above copyright notice and this permission notice shall be
included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
OTHER DEALINGS IN THE SOFTWARE.

Edited by: Luis J Mendez / NCSA

*/

document.writeln('<script id="ppix-vert" type="x-shader\/x-vertex">\r\n      #version 120\r\n      attribute vec3 Vertex;\r\n      attribute vec3 Normal;\r\n      attribute vec2 TexCoord;\r\n\r\n      uniform mat4 PMatrix;\r\n      uniform mat4 MVMatrix;\r\n      uniform mat3 NMatrix;\r\n\r\n      uniform float LightConstantAtt;\r\n      uniform float LightLinearAtt;\r\n      uniform float LightQuadraticAtt;\r\n\r\n      uniform vec4 LightPos;\r\n      \r\n      varying vec3 normal, lightDir, eyeVec;\r\n      varying vec2 texCoord0;\r\n      varying float attenuation;\r\n\r\n      void main()\r\n      {\r\n        vec3 lightVector;\r\n        vec4 v = vec4(Vertex, 1.0);\r\n\r\n        texCoord0 = TexCoord;\r\n\r\n        normal = normalize(NMatrix * Normal);\r\n\r\n        vec4 worldPos = MVMatrix * v;\r\n        lightVector = vec3(LightPos - worldPos);\r\n        lightDir = normalize(lightVector);\r\n        float dist = length(lightVector);\r\n\r\n        eyeVec = -vec3(worldPos);\r\n\r\n        attenuation = 1.0 \/ (LightConstantAtt + LightLinearAtt*dist + LightQuadraticAtt * dist*dist);\r\n        \r\n        gl_Position = PMatrix * worldPos;\r\n      }\r\n\r\n    <\/script>\r\n	');

document.writeln('<script id="ppix-frag" type="x-shader\/x-fragment">\r\n      #version 120\r\n      uniform vec4 LightDiffuse;\r\n      uniform vec4 LightSpecular;\r\n      uniform vec4 MaterialSpecular;\r\n      uniform vec4 MaterialDiffuse;\r\n      uniform vec4 MaterialAmbient;\r\n      uniform vec4 GlobalAmbient;\r\n      uniform float MaterialShininess;\r\n      \r\n      uniform sampler2D DiffTex, SpecTex;\r\n      \r\n      varying vec3 normal, lightDir, eyeVec;\r\n      varying vec2 texCoord0;\r\n      varying float attenuation;\r\n\r\n      void main()\r\n      {\r\n        vec4 color = GlobalAmbient * MaterialAmbient;\r\n        vec4 tex = texture2D(DiffTex, vec2(texCoord0.s, 1.0-texCoord0.t));\r\n        vec4 diffuse = tex;\r\n        \r\n        float lambertTerm = dot(normal, lightDir);\r\n\r\n        if (lambertTerm > 0.0) {\r\n          color += diffuse * lambertTerm * attenuation;\r\n\r\n          vec3 E = normalize(eyeVec);\r\n          vec3 R = reflect(-lightDir, normal);\r\n          \r\n          float specular = pow( max(dot(R, E), 0.0), MaterialShininess );\r\n\r\n          color += MaterialSpecular * LightSpecular * specular * attenuation * tex.r;\r\n        }\r\n        color.a = MaterialDiffuse.a;\r\n\r\n        gl_FragColor = color;\r\n      }\r\n    <\/script>');

//Matrix/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

Math.cot = function(z) { return 1.0 / Math.tan(z); }

/*
  Matrix utilities, using the OpenGL element order where
  the last 4 elements are the translation column.

  Uses flat arrays as matrices for performance.

  Most operations have in-place variants to avoid allocating temporary matrices.

  Naming logic:
    Matrix.method operates on a 4x4 Matrix and returns a new Matrix.
    Matrix.method3x3 operates on a 3x3 Matrix and returns a new Matrix. Not all operations have a 3x3 version (as 3x3 is usually only used for the normal matrix: Matrix.transpose3x3(Matrix.inverseTo3x3(mat4x4)))
    Matrix.method[3x3]InPlace(args, target) stores its result in the target matrix.

    Matrix.scale([sx, sy, sz]) -- non-uniform scale by vector
    Matrix.scale1(s)           -- uniform scale by scalar
    Matrix.scale3(sx, sy, sz)  -- non-uniform scale by scalars
    
    Ditto for translate.
*/
Matrix = {
  identity : [
    1.0, 0.0, 0.0, 0.0,
    0.0, 1.0, 0.0, 0.0,
    0.0, 0.0, 1.0, 0.0,
    0.0, 0.0, 0.0, 1.0
  ],

  newIdentity : function() {
    return [
      1.0, 0.0, 0.0, 0.0,
      0.0, 1.0, 0.0, 0.0,
      0.0, 0.0, 1.0, 0.0,
      0.0, 0.0, 0.0, 1.0
    ];
  },

  newIdentity3x3 : function() {
    return [
      1.0, 0.0, 0.0,
      0.0, 1.0, 0.0,
      0.0, 0.0, 1.0
    ];
  },

  copyMatrix : function(src, dst) {
    for (var i=0; i<16; i++) dst[i] = src[i];
    return dst;
  },

  to3x3 : function(m) {
    return [
      m[0], m[1], m[2],
      m[4], m[5], m[6],
      m[8], m[9], m[10]
    ];
  },

  // orthonormal matrix inverse
  inverseON : function(m) {
    var n = this.transpose4x4(m);
    var t = [m[12], m[13], m[14]];
    n[3] = n[7] = n[11] = 0;
    n[12] = -Vec3.dot([n[0], n[4], n[8]], t);
    n[13] = -Vec3.dot([n[1], n[5], n[9]], t);
    n[14] = -Vec3.dot([n[2], n[6], n[10]], t);
    return n;
  },

  inverseTo3x3 : function(m) {
    return this.inverseTo3x3InPlace(m, this.newIdentity3x3());
  },

  inverseTo3x3InPlace : function(m,n) {
    var a11 = m[10]*m[5]-m[6]*m[9],
        a21 = -m[10]*m[1]+m[2]*m[9],
        a31 = m[6]*m[1]-m[2]*m[5],
        a12 = -m[10]*m[4]+m[6]*m[8],
        a22 = m[10]*m[0]-m[2]*m[8],
        a32 = -m[6]*m[0]+m[2]*m[4],
        a13 = m[9]*m[4]-m[5]*m[8],
        a23 = -m[9]*m[0]+m[1]*m[8],
        a33 = m[5]*m[0]-m[1]*m[4];
    var det = m[0]*(a11) + m[1]*(a12) + m[2]*(a13);
    if (det == 0) // no inverse
      return n;
    var idet = 1 / det;
    n[0] = idet*a11;
    n[1] = idet*a21;
    n[2] = idet*a31;
    n[3] = idet*a12;
    n[4] = idet*a22;
    n[5] = idet*a32;
    n[6] = idet*a13;
    n[7] = idet*a23;
    n[8] = idet*a33;
    return n;
  },

  inverse3x3 : function(m) {
    return this.inverse3x3InPlace(m, this.newIdentity3x3());
  },
  
  inverse3x3InPlace : function(m,n) {
    var a11 = m[8]*m[4]-m[5]*m[7],
        a21 = -m[8]*m[1]+m[2]*m[7],
        a31 = m[5]*m[1]-m[2]*m[4],
        a12 = -m[8]*m[3]+m[5]*m[6],
        a22 = m[8]*m[0]-m[2]*m[6],
        a32 = -m[5]*m[0]+m[2]*m[3],
        a13 = m[7]*m[4]-m[4]*m[8],
        a23 = -m[7]*m[0]+m[1]*m[6],
        a33 = m[4]*m[0]-m[1]*m[3];
    var det = m[0]*(a11) + m[1]*(a12) + m[2]*(a13);
    if (det == 0) // no inverse
      return [1,0,0,0,1,0,0,0,1];
    var idet = 1 / det;
    n[0] = idet*a11;
    n[1] = idet*a21;
    n[2] = idet*a31;
    n[3] = idet*a12;
    n[4] = idet*a22;
    n[5] = idet*a32;
    n[6] = idet*a13;
    n[7] = idet*a23;
    n[8] = idet*a33;
    return n;
  },

  frustum : function (left, right, bottom, top, znear, zfar) {
    var X = 2*znear/(right-left);
    var Y = 2*znear/(top-bottom);
    var A = (right+left)/(right-left);
    var B = (top+bottom)/(top-bottom);
    var C = -(zfar+znear)/(zfar-znear);
    var D = -2*zfar*znear/(zfar-znear);

    return [
      X, 0, 0, 0,
      0, Y, 0, 0,
      A, B, C, -1,
      0, 0, D, 0
    ];
 },

  perspective : function (fovy, aspect, znear, zfar) {
    var ymax = znear * Math.tan(fovy * Math.PI / 360.0);
    var ymin = -ymax;
    var xmin = ymin * aspect;
    var xmax = ymax * aspect;

    return this.frustum(xmin, xmax, ymin, ymax, znear, zfar);
  },

  ortho : function (left, right, bottom, top, znear, zfar) {
    var tX = -(right+left)/(right-left);
    var tY = -(top+bottom)/(top-bottom);
    var tZ = -(zfar+znear)/(zfar-znear);
    var X = 2 / (right-left);
    var Y = 2 / (top-bottom);
    var Z = -2 / (zfar-znear)

    return [
      X, 0, 0, 0,
      0, Y, 0, 0,
      0, 0, Z, 0,
      tX, tY, tZ, 1
    ];
  },

  ortho2D : function(left, right, bottom, top) {
    return this.ortho(left, right, bottom, top, -1, 1);
  },

  mul4x4 : function (a,b) {
    return this.mul4x4InPlace(a,b,this.newIdentity());
  },

  mul4x4InPlace : function (a, b, c) {
        c[0] =   b[0] * a[0] +
                 b[0+1] * a[4] +
                 b[0+2] * a[8] +
                 b[0+3] * a[12];
        c[0+1] = b[0] * a[1] +
                 b[0+1] * a[5] +
                 b[0+2] * a[9] +
                 b[0+3] * a[13];
        c[0+2] = b[0] * a[2] +
                 b[0+1] * a[6] +
                 b[0+2] * a[10] +
                 b[0+3] * a[14];
        c[0+3] = b[0] * a[3] +
                 b[0+1] * a[7] +
                 b[0+2] * a[11] +
                 b[0+3] * a[15];
        c[4] =   b[4] * a[0] +
                 b[4+1] * a[4] +
                 b[4+2] * a[8] +
                 b[4+3] * a[12];
        c[4+1] = b[4] * a[1] +
                 b[4+1] * a[5] +
                 b[4+2] * a[9] +
                 b[4+3] * a[13];
        c[4+2] = b[4] * a[2] +
                 b[4+1] * a[6] +
                 b[4+2] * a[10] +
                 b[4+3] * a[14];
        c[4+3] = b[4] * a[3] +
                 b[4+1] * a[7] +
                 b[4+2] * a[11] +
                 b[4+3] * a[15];
        c[8] =   b[8] * a[0] +
                 b[8+1] * a[4] +
                 b[8+2] * a[8] +
                 b[8+3] * a[12];
        c[8+1] = b[8] * a[1] +
                 b[8+1] * a[5] +
                 b[8+2] * a[9] +
                 b[8+3] * a[13];
        c[8+2] = b[8] * a[2] +
                 b[8+1] * a[6] +
                 b[8+2] * a[10] +
                 b[8+3] * a[14];
        c[8+3] = b[8] * a[3] +
                 b[8+1] * a[7] +
                 b[8+2] * a[11] +
                 b[8+3] * a[15];
        c[12] =   b[12] * a[0] +
                 b[12+1] * a[4] +
                 b[12+2] * a[8] +
                 b[12+3] * a[12];
        c[12+1] = b[12] * a[1] +
                 b[12+1] * a[5] +
                 b[12+2] * a[9] +
                 b[12+3] * a[13];
        c[12+2] = b[12] * a[2] +
                 b[12+1] * a[6] +
                 b[12+2] * a[10] +
                 b[12+3] * a[14];
        c[12+3] = b[12] * a[3] +
                 b[12+1] * a[7] +
                 b[12+2] * a[11] +
                 b[12+3] * a[15];
    return c;
  },

  mulv4 : function (a, v) {
    c = new Array(4);
    for (var i=0; i<4; ++i) {
      var x = 0;
      for (var k=0; k<4; ++k)
        x += v[k] * a[k*4+i];
      c[i] = x;
    }
    return c;
  },

  rotate : function (angle, axis) {
    axis = Vec3.normalize(axis);
    var x=axis[0], y=axis[1], z=axis[2];
    var c = Math.cos(angle);
    var c1 = 1-c;
    var s = Math.sin(angle);
    return [
      x*x*c1+c, y*x*c1+z*s, z*x*c1-y*s, 0,
      x*y*c1-z*s, y*y*c1+c, y*z*c1+x*s, 0,
      x*z*c1+y*s, y*z*c1-x*s, z*z*c1+c, 0,
      0,0,0,1
    ];
  },
  rotateInPlace : function(angle, axis, m) {
    axis = Vec3.normalize(axis);
    var x=axis[0], y=axis[1], z=axis[2];
    var c = Math.cos(angle);
    var c1 = 1-c;
    var s = Math.sin(angle);
    var tmpMatrix = this.tmpMatrix;
    var tmpMatrix2 = this.tmpMatrix2;
    tmpMatrix[0] = x*x*c1+c; tmpMatrix[1] = y*x*c1+z*s; tmpMatrix[2] = z*x*c1-y*s; tmpMatrix[3] = 0;
    tmpMatrix[4] = x*y*c1-z*s; tmpMatrix[5] = y*y*c1+c; tmpMatrix[6] = y*z*c1+x*s; tmpMatrix[7] = 0;
    tmpMatrix[8] = x*z*c1+y*s; tmpMatrix[9] = y*z*c1-x*s; tmpMatrix[10] = z*z*c1+c; tmpMatrix[11] = 0;
    tmpMatrix[12] = 0; tmpMatrix[13] = 0; tmpMatrix[14] = 0; tmpMatrix[15] = 1;
    this.copyMatrix(m, tmpMatrix2);
    return this.mul4x4InPlace(tmpMatrix2, tmpMatrix, m);
  },

  scale : function(v) {
    return [
      v[0], 0, 0, 0,
      0, v[1], 0, 0,
      0, 0, v[2], 0,
      0, 0, 0, 1
    ];
  },
  scale3 : function(x,y,z) {
    return [
      x, 0, 0, 0,
      0, y, 0, 0,
      0, 0, z, 0,
      0, 0, 0, 1
    ];
  },
  scale1 : function(s) {
    return [
      s, 0, 0, 0,
      0, s, 0, 0,
      0, 0, s, 0,
      0, 0, 0, 1
    ];
  },
  scale3InPlace : function(x, y, z, m) {
    var tmpMatrix = this.tmpMatrix;
    var tmpMatrix2 = this.tmpMatrix2;
    tmpMatrix[0] = x; tmpMatrix[1] = 0; tmpMatrix[2] = 0; tmpMatrix[3] = 0;
    tmpMatrix[4] = 0; tmpMatrix[5] = y; tmpMatrix[6] = 0; tmpMatrix[7] = 0;
    tmpMatrix[8] = 0; tmpMatrix[9] = 0; tmpMatrix[10] = z; tmpMatrix[11] = 0;
    tmpMatrix[12] = 0; tmpMatrix[13] = 0; tmpMatrix[14] = 0; tmpMatrix[15] = 1;
    this.copyMatrix(m, tmpMatrix2);
    return this.mul4x4InPlace(tmpMatrix2, tmpMatrix, m);
  },
  scale1InPlace : function(s, m) { return this.scale3InPlace(s, s, s, m); },
  scaleInPlace : function(s, m) { return this.scale3InPlace(s[0],s[1],s[2],m); },

  translate3 : function(x,y,z) {
    return [
      1, 0, 0, 0,
      0, 1, 0, 0,
      0, 0, 1, 0,
      x, y, z, 1
    ];
  },

  translate : function(v) {
    return this.translate3(v[0], v[1], v[2]);
  },
  tmpMatrix : [0,0,0,0, 0,0,0,0, 0,0,0,0, 0,0,0,0],
  tmpMatrix2 : [0,0,0,0, 0,0,0,0, 0,0,0,0, 0,0,0,0],
  translate3InPlace : function(x,y,z,m) {
    var tmpMatrix = this.tmpMatrix;
    var tmpMatrix2 = this.tmpMatrix2;
    tmpMatrix[0] = 1; tmpMatrix[1] = 0; tmpMatrix[2] = 0; tmpMatrix[3] = 0;
    tmpMatrix[4] = 0; tmpMatrix[5] = 1; tmpMatrix[6] = 0; tmpMatrix[7] = 0;
    tmpMatrix[8] = 0; tmpMatrix[9] = 0; tmpMatrix[10] = 1; tmpMatrix[11] = 0;
    tmpMatrix[12] = x; tmpMatrix[13] = y; tmpMatrix[14] = z; tmpMatrix[15] = 1;
    this.copyMatrix(m, tmpMatrix2);
    return this.mul4x4InPlace(tmpMatrix2, tmpMatrix, m);
  },
  translateInPlace : function(v,m){ return this.translate3InPlace(v[0], v[1], v[2], m); },

  lookAt : function (eye, center, up) {
    var z = Vec3.direction(eye, center);
    var x = Vec3.normalizeInPlace(Vec3.cross(up, z));
    var y = Vec3.normalizeInPlace(Vec3.cross(z, x));

    var m = [
      x[0], y[0], z[0], 0,
      x[1], y[1], z[1], 0,
      x[2], y[2], z[2], 0,
      0, 0, 0, 1
    ];

    var t = [
      1, 0, 0, 0,
      0, 1, 0, 0,
      0, 0, 1, 0,
      -eye[0], -eye[1], -eye[2], 1
    ];

    return this.mul4x4(m,t);
  },

  transpose4x4 : function(m) {
    return [
      m[0], m[4], m[8], m[12],
      m[1], m[5], m[9], m[13],
      m[2], m[6], m[10], m[14],
      m[3], m[7], m[11], m[15]
    ];
  },

  transpose4x4InPlace : function(m) {
    var tmp = 0.0;
    tmp = m[1]; m[1] = m[4]; m[4] = tmp;
    tmp = m[2]; m[2] = m[8]; m[8] = tmp;
    tmp = m[3]; m[3] = m[12]; m[12] = tmp;
    tmp = m[6]; m[6] = m[9]; m[9] = tmp;
    tmp = m[7]; m[7] = m[13]; m[13] = tmp;
    tmp = m[11]; m[11] = m[14]; m[14] = tmp;
    return m;
  },

  transpose3x3 : function(m) {
    return [
      m[0], m[3], m[6],
      m[1], m[4], m[7],
      m[2], m[5], m[8]
    ];
  },

  transpose3x3InPlace : function(m) {
    var tmp = 0.0;
    tmp = m[1]; m[1] = m[3]; m[3] = tmp;
    tmp = m[2]; m[2] = m[6]; m[6] = tmp;
    tmp = m[5]; m[5] = m[7]; m[7] = tmp;
    return m;
  },
  
  billboard : function(m) {
    this.billboardInPlace(this.copyMatrix(m));
  },
  
  billboardInPlace : function(m) {
    m[0] = 1.0; m[1] = 0.0; m[2] = 0.0;
    m[4] = 0.0; m[5] = 1.0; m[6] = 0.0;
    m[8] = 0.0; m[9] = 0.0; m[10] = 1.0;
    return m;
  }
}

Vec3 = {
  make : function() { return [0,0,0]; },
  copy : function(v) { return [v[0],v[1],v[2]]; },

  add : function (u,v) {
    return [u[0]+v[0], u[1]+v[1], u[2]+v[2]];
  },

  sub : function (u,v) {
    return [u[0]-v[0], u[1]-v[1], u[2]-v[2]];
  },

  negate : function (u) {
    return [-u[0], -u[1], -u[2]];
  },

  direction : function (u,v) {
    return this.normalizeInPlace(this.sub(u,v));
  },

  length : function(v) {
    return Math.sqrt(v[0]*v[0] + v[1]*v[1] + v[2]*v[2]);
  },

  lengthSquare : function(v) {
    return v[0]*v[0] + v[1]*v[1] + v[2]*v[2];
  },

  normalizeInPlace : function(v) {
    var imag = 1.0 / this.length(v);
    v[0] *= imag; v[1] *= imag; v[2] *= imag;
    return v;
  },

  normalize : function(v) {
    return this.normalizeInPlace(this.copy(v));
  },

  scale : function(f, v) {
    return [f*v[0], f*v[1], f*v[2]];
  },

  dot : function(u,v) {
    return u[0]*v[0] + u[1]*v[1] + u[2]*v[2];
  },

  inner : function(u,v) {
    return [u[0]*v[0], u[1]*v[1], u[2]*v[2]];
  },

  cross : function(u,v) {
    return [
      u[1]*v[2] - u[2]*v[1],
      u[2]*v[0] - u[0]*v[2],
      u[0]*v[1] - u[1]*v[0]
    ];
  }
}

//GL_UTIL/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

Texture = function(gl) {
  this.gl = gl;
};
Texture.prototype = {
  target : 'TEXTURE_2D',
  generateMipmaps : true,
  width : null,
  height : null,
  data : null,
  changed : false,

  upload : function() {
    var gl = this.gl;
    var target = gl[this.target];
    if (this.image)
      gl.texImage2D(target, 0, this.image);
    else
      gl.texImage2D(target, 0, gl.RGBA, this.width, this.height, 0, gl.RGBA, this.data);
  },
  
  regenerateMipmap : function() {
    var gl = this.gl;
    var target = gl[this.target];
    gl.texParameteri(target, gl.TEXTURE_MIN_FILTER, gl.LINEAR);
    if (this.generateMipmaps) {
      gl.generateMipmap(target);
      gl.texParameteri(target, gl.TEXTURE_MIN_FILTER, gl.LINEAR_MIPMAP_LINEAR);
    }
  },
  
  compile: function(){
    var gl = this.gl;
    var target = gl[this.target];
    this.textureObject = gl.createTexture();
    Stats.textureCreationCount++;
    gl.bindTexture(target, this.textureObject);
    this.upload();
    gl.texParameteri(target, gl.TEXTURE_WRAP_S, gl.CLAMP_TO_EDGE);
    gl.texParameteri(target, gl.TEXTURE_WRAP_T, gl.CLAMP_TO_EDGE);
    gl.texParameteri(target, gl.TEXTURE_MAG_FILTER, gl.LINEAR);
    this.regenerateMipmap();
  },
  
  use : function() {
    if (this.textureObject == null)
      this.compile();
    this.gl.bindTexture(this.gl[this.target], this.textureObject);
    if (this.changed) {
      this.upload();
      this.regenerateMipmap();
      this.changed = false;
    }
  }
};


function checkError(gl, msg) {
  var e = gl.getError();
  if (e != 0) {
    log("Error " + e + " at " + msg);
  }
  return e;
}

function throwError(gl, msg) {
  var e = gl.getError();
  if (e != 0) {
    throw(new Error("Error " + e + " at " + msg));
  }
}


Shader = function(gl){
  this.gl = gl;
  this.shaders = [];
  this.uniformLocations = {};
  this.attribLocations = {};
  for (var i=1; i<arguments.length; i++) {
    this.shaders.push(arguments[i]);
  }
}
Shader.getShader = function(gl, id) {
  var shaderScript = document.getElementById(id);
  if (!shaderScript) {
    throw(new Error("No shader element with id: "+id));
  }

  var str = "";
  var k = shaderScript.firstChild;
  while (k) {
    if (k.nodeType == 3)
      str += k.textContent;
    k = k.nextSibling;
  }

  var shader;
  if (shaderScript.type == "x-shader/x-fragment") {
    shader = gl.createShader(gl.FRAGMENT_SHADER);
  } else if (shaderScript.type == "x-shader/x-vertex") {
    shader = gl.createShader(gl.VERTEX_SHADER);
  } else {
    throw(new Error("Unknown shader type "+shaderScript.type));
  }

  gl.shaderSource(shader, str);
  gl.compileShader(shader);

  if (gl.getShaderParameter(shader, gl.COMPILE_STATUS) != 1) {
    var ilog = gl.getShaderInfoLog(shader);
    gl.deleteShader(shader);
    throw(new Error("Failed to compile shader "+shaderScript.id + ", Shader info log: " + ilog));
  }
  return shader;
}

Shader.loadShaderArray = function(gl, shaders) {
  var id = gl.createProgram();
  var shaderObjs = [];
  for (var i=0; i<shaders.length; ++i) {
    try {
      var sh = this.getShader(gl, shaders[i]);
      shaderObjs.push(sh);
      gl.attachShader(id, sh);
    } catch (e) {
      var pr = {program: id, shaders: shaderObjs};
      this.deleteShader(gl, pr);
      throw (e);
    }
  }
  var prog = {program: id, shaders: shaderObjs};
  gl.linkProgram(id);
  gl.validateProgram(id);
  if (gl.getProgramParameter(id, gl.LINK_STATUS) != 1) {
    this.deleteShader(gl,prog);
    throw(new Error("Failed to link shader"));
  }
  if (gl.getProgramParameter(id, gl.VALIDATE_STATUS) != 1) {
    this.deleteShader(gl,prog);
    throw(new Error("Failed to validate shader"));
  }
  return prog;
}
Shader.loadShader = function(gl) {
  var sh = [];
  for (var i=1; i<arguments.length; ++i)
    sh.push(arguments[i]);
  return this.loadShaderArray(gl, sh);
}

Shader.deleteShader = function(gl, sh) {
  gl.useProgram(null);
  sh.shaders.forEach(function(s){
    gl.detachShader(sh.program, s);
    gl.deleteShader(s);
  });
  gl.deleteProgram(sh.program);
}
Shader.prototype = {
  id : null,
  gl : null,
  compiled : false,
  shader : null,
  shaders : [],

  destroy : function() {
    if (this.shader != null) 
      Shader.deleteShader(this.gl, this.shader);
  },

  compile : function() {
    this.shader = Shader.loadShaderArray(this.gl, this.shaders);
  },

  use : function() {
    if (this.shader == null)
      this.compile();
    this.gl.useProgram(this.shader.program);
  },
  
  getInfoLog : function() {
    if (this.shader == null) 
      this.compile();
    var gl = this.gl;
    var plog = gl.getProgramInfoLog(this.shader.program);
    var slog = this.shader.shaders.map(function(s){ return gl.getShaderInfoLog(s); }).join("\n\n");
    return plog + "\n\n" + slog;
  },

  uniform1fv : function(name, value) {
    var loc = this.uniform(name);
    this.gl.uniform1fv(loc, value);
  },

  uniform2fv : function(name, value) {
    var loc = this.uniform(name);
    this.gl.uniform2fv(loc, value);
  },

  uniform3fv : function(name, value) {
    var loc = this.uniform(name);
    this.gl.uniform3fv(loc, value);
  },

  uniform4fv : function(name, value) {
    var loc = this.uniform(name);
    this.gl.uniform4fv(loc, value);
  },
  
  uniform1f : function(name, value) {
    var loc = this.uniform(name);
    this.gl.uniform1f(loc, value);
  },

  uniform2f : function(name, v1,v2) {
    var loc = this.uniform(name);
    this.gl.uniform2f(loc, v1,v2);
  },

  uniform3f : function(name, v1,v2,v3) {
    var loc = this.uniform(name);
    this.gl.uniform3f(loc, v1,v2,v3);
  },

  uniform4f : function(name, v1,v2,v3,v4) {
    var loc = this.uniform(name);
    this.gl.uniform4f(loc, v1, v2, v3, v4);
  },
  
  uniform1iv : function(name, value) {
    var loc = this.uniform(name);
    this.gl.uniform1iv(loc, value);
  },

  uniform2iv : function(name, value) {
    var loc = this.uniform(name);
    this.gl.uniform2iv(loc, value);
  },

  uniform3iv : function(name, value) {
    var loc = this.uniform(name);
    this.gl.uniform3iv(loc, value);
  },

  uniform4iv : function(name, value) {
    var loc = this.uniform(name);
    this.gl.uniform4iv(loc, value);
  },

  uniform1i : function(name, value) {
    var loc = this.uniform(name);
    this.gl.uniform1i(loc, value);
  },

  uniform2i : function(name, v1,v2) {
    var loc = this.uniform(name);
    this.gl.uniform2i(loc, v1,v2);
  },

  uniform3i : function(name, v1,v2,v3) {
    var loc = this.uniform(name);
    this.gl.uniform3i(loc, v1,v2,v3);
  },

  uniform4i : function(name, v1,v2,v3,v4) {
    var loc = this.uniform(name);
    this.gl.uniform4i(loc, v1, v2, v3, v4);
  },

  uniformMatrix4fv : function(name, value) {
    var loc = this.uniform(name);
    this.gl.uniformMatrix4fv(loc, false, value);
  },

  uniformMatrix3fv : function(name, value) {
    var loc = this.uniform(name);
    this.gl.uniformMatrix3fv(loc, false, value);
  },

  uniformMatrix2fv : function(name, value) {
    var loc = this.uniform(name);
    this.gl.uniformMatrix2fv(loc, false, value);
  },

  attrib : function(name) {
    if (this.attribLocations[name] == null) {
      var loc = this.gl.getAttribLocation(this.shader.program, name);
      this.attribLocations[name] = loc;
    }
    return this.attribLocations[name];
  },

  uniform : function(name) {
    if (this.uniformLocations[name] == null) {
      var loc = this.gl.getUniformLocation(this.shader.program, name);
      this.uniformLocations[name] = loc;
    }
    return this.uniformLocations[name];
  }
}

Filter = function(gl, shader) {
  Shader.apply(this, arguments);
}
Filter.prototype = new Shader();
Filter.prototype.apply = function(init) {
  this.use();
  var va = this.attrib("Vertex");
  var ta = this.attrib("TexCoord");
  var vbo = Quad.getCachedVBO(this.gl);
  if (init) init(this);
  vbo.draw(va, null, ta);
}

VBO = function(gl) {
  this.gl = gl;
  this.data = [];
  this.elementsVBO = null;
  for (var i=1; i<arguments.length; i++) {
    if (arguments[i].elements)
      this.elements = arguments[i];
    else
      this.data.push(arguments[i]);
  }
}

VBO.prototype = {
  initialized : false,
  length : 0,
  vbos : null,
  type : 'TRIANGLES',
  elementsVBO : null,
  elements : null,

  setData : function() {
    this.destroy();
    this.data = [];
    for (var i=0; i<arguments.length; i++) {
      if (arguments[i].elements)
        this.elements = arguments[i];
      else
        this.data.push(arguments[i]);
    }
  },

  destroy : function() {
    if (this.vbos != null)
      for (var i=0; i<this.vbos.length; i++)
        this.gl.deleteBuffer(this.vbos[i]);
    if (this.elementsVBO != null)
      this.gl.deleteBuffer(this.elementsVBO);
    this.length = this.elementsLength = 0;
    this.vbos = this.elementsVBO = null;
    this.initialized = false;
  },

  init : function() {
    this.destroy();
    var gl = this.gl;
   
    gl.getError();
    var vbos = [];
    var length = 0;
    for (var i=0; i<this.data.length; i++)
      vbos.push(gl.createBuffer());
    if (this.elements != null)
      this.elementsVBO = gl.createBuffer();
    try {
      throwError(gl, "genBuffers");
      for (var i = 0; i<this.data.length; i++) {
        var d = this.data[i];
        var dlen = Math.floor(d.data.length / d.size);
        if (i == 0 || dlen < length)
            length = dlen;
        if (!d.floatArray)
          d.floatArray = new WebGLFloatArray(d.data);
        gl.bindBuffer(gl.ARRAY_BUFFER, vbos[i]);
        throwError(gl, "bindBuffer");
        gl.bufferData(gl.ARRAY_BUFFER, d.floatArray, gl.STATIC_DRAW);
        throwError(gl, "bufferData");
      }
      if (this.elementsVBO != null) {
        var d = this.elements;
        this.elementsLength = d.data.length;
        this.elementsType = d.type == gl.UNSIGNED_BYTE ? d.type : gl.UNSIGNED_SHORT;
        gl.bindBuffer(gl.ELEMENT_ARRAY_BUFFER, this.elementsVBO);
        throwError(gl, "bindBuffer ELEMENT_ARRAY_BUFFER");
        if (this.elementsType == gl.UNSIGNED_SHORT && !d.ushortArray) {
          d.ushortArray = new WebGLUnsignedShortArray(d.data);
          gl.bufferData(gl.ELEMENT_ARRAY_BUFFER, d.ushortArray, gl.STATIC_DRAW);
        } else if (this.elementsType == gl.UNSIGNED_BYTE && !d.ubyteArray) {
          d.ubyteArray = new WebGLUnsignedByteArray(d.data);
          gl.bufferData(gl.ELEMENT_ARRAY_BUFFER, d.ubyteArray, gl.STATIC_DRAW);
        }
        throwError(gl, "bufferData ELEMENT_ARRAY_BUFFER");
      }
    } catch(e) {
      for (var i=0; i<vbos.length; i++)
        gl.deleteBuffer(vbos[i]);
      throw(e);
    }

    gl.bindBuffer(gl.ARRAY_BUFFER, null);
    gl.bindBuffer(gl.ELEMENT_ARRAY_BUFFER, null);

    this.length = length;
    this.vbos = vbos;
  
    this.initialized = true;
  },

  use : function() {
    if (!this.initialized) this.init();
    var gl = this.gl;
    for (var i=0; i<arguments.length; i++) {
      var arg = arguments[i];
      if (arg == null || arg == -1 || !this.vbos[i]) continue;
      gl.bindBuffer(gl.ARRAY_BUFFER, this.vbos[i]);
      gl.vertexAttribPointer(arg, this.data[i].size, gl.FLOAT, false, 0, 0);
      gl.enableVertexAttribArray(arg);
    }
    if (this.elementsVBO != null) {
      gl.bindBuffer(gl.ELEMENT_ARRAY_BUFFER, this.elementsVBO);
    }
  },

  draw : function() {
    var args = [];
    this.use.apply(this, arguments);
    var gl = this.gl;
    if (this.elementsVBO != null) {
      gl.drawElements(gl[this.type], this.elementsLength, this.elementsType, 0);
    } else {
      gl.drawArrays(gl[this.type], 0, this.length);
    }
  }
}

FBO = function(gl, width, height, use_depth) {
  this.gl = gl;
  this.width = width;
  this.height = height;
  if (use_depth != null)
    this.useDepth = use_depth;
}
FBO.prototype = {
  initialized : false,
  useDepth : true,
  fbo : null,
  rbo : null,
  texture : null,

  destroy : function() {
    if (this.fbo) this.gl.deleteFramebuffer(this.fbo);
    if (this.rbo) this.gl.deleteRenderbuffer(this.rbo);
    if (this.texture) this.gl.deleteTexture(this.texture);
  },

  init : function() {
    var gl = this.gl;
    var w = this.width, h = this.height;
    var fbo = this.fbo != null ? this.fbo : gl.createFramebuffer();
    var rb;

    gl.bindFramebuffer(gl.FRAMEBUFFER, fbo);
    checkError(gl, "FBO.init bindFramebuffer");
    if (this.useDepth) {
      rb = this.rbo != null ? this.rbo : gl.createRenderbuffer();
      gl.bindRenderbuffer(gl.RENDERBUFFER, rb);
      checkError(gl, "FBO.init bindRenderbuffer");
      gl.renderbufferStorage(gl.RENDERBUFFER, gl.DEPTH_COMPONENT16, w, h);
      checkError(gl, "FBO.init renderbufferStorage");
    }

    var tex = this.texture != null ? this.texture : gl.createTexture();
    gl.bindTexture(gl.TEXTURE_2D, tex);
    try {
      gl.texImage2D(gl.TEXTURE_2D, 0, gl.RGBA, w, h, 0, gl.RGBA, gl.UNSIGNED_BYTE, null);
    } catch (e) { // argh, no null texture support
      var tmp = this.getTempCanvas(w,h);
      gl.texImage2D(gl.TEXTURE_2D, 0, tmp);
    }
    gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_WRAP_S, gl.CLAMP_TO_EDGE);
    gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_WRAP_T, gl.CLAMP_TO_EDGE);
    gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_MAG_FILTER, gl.LINEAR);
    gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_MIN_FILTER, gl.LINEAR);
    checkError(gl, "FBO.init tex");

    gl.framebufferTexture2D(gl.FRAMEBUFFER, gl.COLOR_ATTACHMENT0, gl.TEXTURE_2D, tex, 0);
    checkError(gl, "FBO.init bind tex");

    if (this.useDepth) {
      gl.framebufferRenderbuffer(gl.FRAMEBUFFER, gl.DEPTH_ATTACHMENT, gl.RENDERBUFFER, rb);
      checkError(gl, "FBO.init bind depth buffer");
    }

    var fbstat = gl.checkFramebufferStatus(gl.FRAMEBUFFER);
    if (fbstat != gl.FRAMEBUFFER_COMPLETE) {
      var glv;
      for (var v in gl) {
        try { glv = gl[v]; } catch (e) { glv = null; }
        if (glv == fbstat) { fbstat = v; break; }}
        log("Framebuffer status: " + fbstat);
    }
    checkError(gl, "FBO.init check fbo");

    this.fbo = fbo;
    this.rbo = rb;
    this.texture = tex;
    this.initialized = true;
  },

  getTempCanvas : function(w, h) {
    if (!FBO.tempCanvas) {
      FBO.tempCanvas = document.createElement('canvas');
    }
    FBO.tempCanvas.width = w;
    FBO.tempCanvas.height = h;
    return FBO.tempCanvas;
  },

  use : function() {
    if (!this.initialized) this.init();
    this.gl.bindFramebuffer(this.gl.FRAMEBUFFER, this.fbo);
  }
}

function makeGLErrorWrapper(gl, fname) {
    return (function() {
        var rv;
        try {
            rv = gl[fname].apply(gl, arguments);
        } catch (e) {
            throw(new Error("GL error " + e.name + " in "+fname+ "\n"+ e.message+"\n" +arguments.callee.caller));
        }
        var e = gl.getError();
        if (e != 0) {
            throw(new Error("GL error "+e+" in "+fname));
        }
        return rv;
    });
}

function wrapGLContext(gl) {
    var wrap = {};
    for (var i in gl) {
      try {
        if (typeof gl[i] == 'function') {
            wrap[i] = makeGLErrorWrapper(gl, i);
        } else {
            wrap[i] = gl[i];
        }
      } catch (e) {
        // log("wrapGLContext: Error accessing " + i);
      }
    }
    wrap.getError = function(){ return gl.getError(); };
    return wrap;
}


Quad = {
  vertices : [
    -1,-1,0,
    1,-1,0,
    -1,1,0,
    1,-1,0,
    1,1,0,
    -1,1,0
  ],
  normals : [
    0,0,-1,
    0,0,-1,
    0,0,-1,
    0,0,-1,
    0,0,-1,
    0,0,-1
  ],
  texcoords : [
    0,0,
    1,0,
    0,1,
    1,0,
    1,1,
    0,1
  ],
  indices : [0,1,2,1,5,2],
  makeVBO : function(gl) {
    return new VBO(gl,
        {size:3, data: this.vertices},
        {size:3, data: this.normals},
        {size:2, data: this.texcoords}
    )
  },
  cache: {},
  getCachedVBO : function(gl) {
    if (!this.cache[gl])
      this.cache[gl] = this.makeVBO(gl);
    return this.cache[gl];
  }
}
Cube = {
  vertices : [  0.5, -0.5,  0.5, // +X
                0.5, -0.5, -0.5,
                0.5,  0.5, -0.5,
                0.5,  0.5,  0.5,

                0.5,  0.5,  0.5, // +Y
                0.5,  0.5, -0.5,
                -0.5,  0.5, -0.5,
                -0.5,  0.5,  0.5,

                0.5,  0.5,  0.5, // +Z
                -0.5,  0.5,  0.5,
                -0.5, -0.5,  0.5,
                0.5, -0.5,  0.5,

                -0.5, -0.5,  0.5, // -X
                -0.5,  0.5,  0.5,
                -0.5,  0.5, -0.5,
                -0.5, -0.5, -0.5,

                -0.5, -0.5,  0.5, // -Y
                -0.5, -0.5, -0.5,
                0.5, -0.5, -0.5,
                0.5, -0.5,  0.5,

                -0.5, -0.5, -0.5, // -Z
                -0.5,  0.5, -0.5,
                0.5,  0.5, -0.5,
                0.5, -0.5, -0.5,
      ],

  normals : [ 1, 0, 0,
              1, 0, 0,
              1, 0, 0,
              1, 0, 0,

              0, 1, 0,
              0, 1, 0,
              0, 1, 0,
              0, 1, 0,

              0, 0, 1,
              0, 0, 1,
              0, 0, 1,
              0, 0, 1,

              -1, 0, 0,
              -1, 0, 0,
              -1, 0, 0,
              -1, 0, 0,

              0,-1, 0,
              0,-1, 0,
              0,-1, 0,
              0,-1, 0,

              0, 0,-1,
              0, 0,-1,
              0, 0,-1,
              0, 0,-1
      ],

  texcoords :  [
    0,0,  0,1,  1,1, 1,0,
    0,0,  0,1,  1,1, 1,0,
    0,0,  0,1,  1,1, 1,0,
    0,0,  0,1,  1,1, 1,0,
    0,0,  0,1,  1,1, 1,0,
    0,0,  0,1,  1,1, 1,0
  ],
      
  indices : [],
  create : function(){
    for (var i = 0; i < 6; i++) {
      this.indices.push(i*4 + 0);
      this.indices.push(i*4 + 1);
      this.indices.push(i*4 + 3);
      this.indices.push(i*4 + 1);
      this.indices.push(i*4 + 2);
      this.indices.push(i*4 + 3);
    }
  },

  makeVBO : function(gl) {
    return new VBO(gl,
        {size:3, data: this.vertices},
        {size:3, data: this.normals},
        {size:2, data: this.texcoords},
        {elements: true, data: this.indices}
    )
  },
  cache : {},
  getCachedVBO : function(gl) {
    if (!this.cache[gl])
      this.cache[gl] = this.makeVBO(gl);
    return this.cache[gl];
  }
}
Cube.create();

Sphere = {
  vertices : [],
  normals : [],
  indices : [],
  create : function(){
    var r = 0.75;
    var self = this;
    function vert(theta, phi)
    {
      var r = 0.75;
      var x, y, z, nx, ny, nz;

      nx = Math.sin(theta) * Math.cos(phi);
      ny = Math.sin(phi);
      nz = Math.cos(theta) * Math.cos(phi);
      self.normals.push(nx);
      self.normals.push(ny);
      self.normals.push(nz);

      x = r * Math.sin(theta) * Math.cos(phi);
      y = r * Math.sin(phi);
      z = r * Math.cos(theta) * Math.cos(phi);
      self.vertices.push(x);
      self.vertices.push(y);
      self.vertices.push(z);
    }
    for (var phi = -Math.PI/2; phi < Math.PI/2; phi += Math.PI/20) {
      var phi2 = phi + Math.PI/20;
      for (var theta = -Math.PI/2; theta <= Math.PI/2; theta += Math.PI/20) {
        vert(theta, phi);
        vert(theta, phi2);
      }
    }
  }
}

Sphere.create();

Geometry = {
  Cube : Cube,
  Quad : Quad,
  Sphere : Sphere
};

try {
  if (!window.CanvasArrayBuffer)
    CanvasArrayBuffer = WebGLArrayBuffer;
  if (!window.CanvasFloatArray)
    CanvasFloatArray = WebGLFloatArray;
  if (!window.CanvasUnsignedIntArray)
    CanvasUnsignedIntArray = WebGLUnsignedIntArray;
  if (!window.CanvasIntArray)
    CanvasIntArray = WebGLIntArray;
  if (!window.CanvasUnsignedShortArray)
    CanvasUnsignedShortArray = WebGLUnsignedShortArray;
  if (!window.CanvasShortArray)
    CanvasShortArray = WebGLShortArray;
  if (!window.CanvasUnsignedByteArray)
    CanvasUnsignedByteArray = WebGLUnsignedByteArray;
  if (!window.CanvasByteArray)
    CanvasByteArray = WebGLByteArray;
} catch(e) {}
try {
  if (!window.WebGLArrayBuffer)
    WebGLArrayBuffer = CanvasArrayBuffer;
  if (!window.WebGLFloatArray)
    WebGLFloatArray = CanvasFloatArray;
  if (!window.WebGLUnsignedIntArray)
    WebGLUnsignedIntArray = CanvasUnsignedIntArray;
  if (!window.WebGLIntArray)
    WebGLIntArray = CanvasIntArray;
  if (!window.WebGLUnsignedShortArray)
    WebGLUnsignedShortArray = CanvasUnsignedShortArray;
  if (!window.WebGLShortArray)
    WebGLShortArray = CanvasShortArray;
  if (!window.WebGLUnsignedByteArray)
    WebGLUnsignedByteArray = CanvasUnsignedByteArray;
  if (!window.WebGLByteArray)
    WebGLByteArray = CanvasByteArray;
} catch(e) {}

log=function(msg) {
  var c = document.getElementById('c');
  var ctx = c.getContext('2d');
  ctx.font = '14px Sans-serif';
  ctx.textAlign = 'center';
  ctx.fillStyle = '#c24';
  ctx.fillText(msg,c.width/2,c.height/2,c.width-20);
}
GL_CONTEXT_ID = null;
getGLContext = function(c){
  var find=function(a,f){for(var i=0,j;j=a[i],i++<a.length;)if(f(j))return j};
  if (!GL_CONTEXT_ID)
    GL_CONTEXT_ID = find(['experimental-webgl','webgl','webkit-3d','moz-webgl'],function(n){try{return c.getContext(n)}catch(e){}});
  if (!GL_CONTEXT_ID) {
    log("No WebGL context found. Click here for more details.");
    var a = document.createElement('a');
    a.href = "http://khronos.org/webgl/wiki/Getting_a_WebGL_Implementation";
    c.parentNode.insertBefore(a, c);
    a.appendChild(c);
  }
  else return c.getContext(GL_CONTEXT_ID); 
}

Stats = {
  shaderBindCount : 0,
  materialUpdateCount : 0,
  uniformSetCount : 0,
  textureSetCount : 0,
  textureCreationCount : 0,
  reset : function(){
    this.shaderBindCount = 0;
    this.materialUpdateCount = 0;
    this.uniformSetCount = 0;
    this.textureSetCount = 0;
    this.textureCreationCount = 0;
  },
  print : function(elem) {
    elem.textContent = 'Shader bind count: ' + this.shaderBindCount + '\n' +
                       'Material update count: ' + this.materialUpdateCount + '\n' +
                       'Uniform set count: ' + this.uniformSetCount + '\n' +
                       'Texture creation count: ' + this.textureCreationCount + '\n' +
                       'Texture set count: ' + this.textureSetCount + '\n' +
                       '';
  }
}

//SCENE_GRAPH/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

Node = function(model) {
  this.model = model;
  this.initialize();
}
Node.prototype = {
  model : null,
  position : null,
  rotation : null,
  scaling : null,
  polygonOffset : null,
  scaleAfterRotate : false,
  depthMask : true,
  display : true,
  
  initialize : function() {
    this.matrix = Matrix.newIdentity();
    this.material = new Material();
    this.normalMatrix = Matrix.newIdentity3x3();
    this.rotation = {angle : 0, axis : [0,1,0]};
    this.position = [0, 0, 0];
    this.scaling = [1, 1, 1];
    this.frameListeners = [];
    this.childNodes = [];
  },
  
  draw : function(gl, state, perspectiveMatrix) {
    if (!this.model || !this.display) return;
    if (this.material)
      this.material.apply(gl, state, perspectiveMatrix, this.matrix, this.normalMatrix);
    if (this.model.gl == null) this.model.gl = gl;
    if (this.polygonOffset)
      gl.polygonOffset(this.polygonOffset.factor, this.polygonOffset.units);
    if (this.depthMask == false)
      gl.depthMask(false);
    this.model.draw(
      state.currentShader.attrib('Vertex'),
      state.currentShader.attrib('Normal'),
      state.currentShader.attrib('TexCoord')
    );
    if (this.depthMask == false)
      gl.depthMask(true);
    if (this.polygonOffset)
      gl.polygonOffset(0.0, 0.0);
  },
  
  addFrameListener : function(f) {
    this.frameListeners.push(f);
  },
  
  update : function(t, dt) {
    var a = [];
    for (var i=0; i<this.frameListeners.length; i++) {
      a.push(this.frameListeners[i]);
    }
    for (var i=0; i<a.length; i++) {
      if (this.frameListeners.indexOf(a[i]) != -1)
        a[i].call(this, t, dt, this);
    }
    for (var i=0; i<this.childNodes.length; i++)
      this.childNodes[i].update(t, dt);
  },
  
  appendChild : function(c) {
    this.childNodes.push(c);
  },
  
  updateTransform : function(matrix) {
    Matrix.copyMatrix(matrix, this.matrix);
    var p = this.position;
    var s = this.scaling;
    var doScaling = (s[0] != 1) || (s[1] != 1) || (s[2] != 1);
    if (p[0] || p[1] || p[2])
      Matrix.translateInPlace(p, this.matrix);
    if (this.scaleAfterRotate && doScaling)
      Matrix.scaleInPlace(s, this.matrix);
    if (this.rotation.angle != 0)
      Matrix.rotateInPlace(this.rotation.angle, this.rotation.axis, this.matrix);
    if (!this.scaleAfterRotate && this.scaling)
      Matrix.scaleInPlace(s, this.matrix);
    if (this.isBillboard)
      Matrix.billboardInPlace(this.matrix);
    Matrix.inverseTo3x3InPlace(this.matrix, this.normalMatrix);
    Matrix.transpose3x3InPlace(this.normalMatrix);
    for (var i=0; i<this.childNodes.length; i++)
      this.childNodes[i].updateTransform(this.matrix);
  },
  
  collectDrawList : function(arr) {
    if (!arr) arr = [];
    arr.push(this);
    for (var i=0; i<this.childNodes.length; i++)
      this.childNodes[i].collectDrawList(arr);
    return arr;
  }
};

// image quad with canvas as texture
// draw text string to canvas, upload with texImage2D
// material has 
GLText = function(string, font) {
}
GLText.prototype = {
}

Material = function(shader) {
  this.shader = shader;
  this.textures = {};
  for (var i in this.textures) delete this.textures[i];
  this.floats = {};
  for (var i in this.floats) delete this.floats[i];
  this.ints = {};
  for (var i in this.ints) delete this.ints[i];
};
Material.prototype = {
  copyValue : function(v){
    if (typeof v == 'number') return v;
    var a = [];
    for (var i=0; i<v.length; i++) a[i] = v[i];
    return a;
  },
  
  copy : function(){
    var m = new Material();
    for (var i in this.floats) m.floats[i] = this.copyValue(this.floats[i]);
    for (var i in this.ints) m.ints[i] = this.copyValue(this.ints[i]);
    for (var i in this.textures) m.textures[i] = this.textures[i];
    m.shader = this.shader;
    return m;
  },
  
  apply : function(gl, state, perspectiveMatrix, matrix, normalMatrix) {
    var shader = this.shader;
    if (shader && shader.gl == null) shader.gl = gl;
    if (state.currentShader != shader) {
      shader.use()
      shader.uniformMatrix4fv("PMatrix", perspectiveMatrix);
      Stats.uniformSetCount++;
      state.currentShader = this.shader;
      Stats.shaderBindCount++;
    }
    state.currentShader.uniformMatrix4fv("MVMatrix", matrix);
    state.currentShader.uniformMatrix3fv("NMatrix", normalMatrix);
    Stats.uniformSetCount += 2;
    if (state.currentMaterial == this) return;
    state.currentMaterial = this;
    Stats.materialUpdateCount++;
    this.applyTextures(gl, state);
    this.applyFloats();
    this.applyInts();
  },
  
  applyTextures : function(gl, state) {
    var texUnit = 0;
    for (var name in this.textures) {
      var tex = this.textures[name];
      if (tex && tex.gl == null) tex.gl = gl;
      if (state.textures[texUnit] != tex) {
        state.textures[texUnit] = tex;
        gl.activeTexture(gl.TEXTURE0+texUnit);
        tex.use();
        Stats.textureSetCount++;
      }
      this.shader.uniform1i(name, texUnit);
      Stats.uniformSetCount++;
      ++texUnit;
    }
  },
  
  applyFloats : function() {
    var shader = this.shader;
    for (var name in this.floats) {
      var uf = this.floats[name];
      Stats.uniformSetCount++;
      if (uf.length == null) {
        shader.uniform1f(name, uf);
      } else {
        switch (uf.length) {
          case 4:
            shader.uniform4f(name, uf[0], uf[1], uf[2], uf[3]);
            break;
          case 3:
            shader.uniform3f(name, uf[0], uf[1], uf[2]);
            break;
          case 16:
            shader.uniformMatrix4fv(name, uf);
            break;
          case 9:
            shader.uniformMatrix3fv(name, uf);
            break;
          case 2:
            shader.uniform2f(name, uf[0], uf[1]);
            break;
          default:
            shader.uniform1fv(name, uf);
        }
      }
    }
  },
  
  applyInts : function() {
    var shader = this.shader;
    for (var name in this.ints) {
      var uf = this.ints[name];
      Stats.uniformSetCount++;
      if (uf.length == null) {
        shader.uniform1i(name, uf);
      } else {
        switch (uf.length) {
          case 4:
            shader.uniform4i(name, uf[0], uf[1], uf[2], uf[3]);
            break;
          case 3:
            shader.uniform3i(name, uf[0], uf[1], uf[2]);
            break;
          case 2:
            shader.uniform2i(name, uf[0], uf[1]);
            break;
          default:
            shader.uniform1iv(name, uf);
        }
      }
    }
  }
  
};

GLDrawState = function(){
  this.textures = [];
};
GLDrawState.prototype = {
  textures : null,
  currentMaterial : null,
  currentShader : null
};

Camera = function() {
  this.initialize();
};
Camera.prototype = {
  fov : 30,
  targetFov : 30,
  zNear : 1,
  zFar : 100,
  useLookAt : true,
  ortho : false,
  stereo : false,
  stereoSeparation : 0.025,

  initialize : function() {
    this.position = [5,5,5];
    this.lookAt = [0,0,0]
    this.up = [0,1,0];
    this.frameListeners = [];
  },
  addFrameListener : Node.prototype.addFrameListener,
  update : function(t, dt) {
    var a = [];
    for (var i=0; i<this.frameListeners.length; i++) {
      a.push(this.frameListeners[i]);
    }
    for (var i=0; i<a.length; i++) {
      if (this.frameListeners.indexOf(a[i]) != -1)
        a[i].call(this, t, dt, this);
    }
    if (this.targetFov && this.fov != this.targetFov)
      this.fov += (this.targetFov - this.fov) * (1-Math.pow(0.7, (dt/30)));
  },
  getLookMatrix : function() {
    if (this.useLookAt)
      return Matrix.lookAt(this.position, this.lookAt, this.up);
    else
      return Matrix.newIdentity();
  },
  drawViewport : function(gl, x, y, width, height, scene) {
    gl.enable(gl.SCISSOR_TEST);
    gl.viewport(x,y,width,height);
    gl.scissor(x,y,width,height);
    var perspective = this.ortho ?
      Matrix.ortho(x, width, -height, -y, this.zNear, this.zFar) :
      Matrix.perspective(this.fov, width/height, this.zNear, this.zFar);
    scene.updateTransform(this.getLookMatrix());
    var drawList = scene.collectDrawList();
    var state = new GLDrawState();
    for (var i=0; i<drawList.length; i++) {
      var d = drawList[i];
      d.draw(gl, state, perspective);
    }
    gl.disable(gl.SCISSOR_TEST);
  },
  
  draw : function(gl, width, height, scene) {
    if (this.stereo) {
      var p = this.position;
      var rightV = Vec3.cross(this.up, Vec3.sub(this.lookAt, this.position));
      var sep = Vec3.scale(this.stereoSeparation/2, rightV);

      this.position = Vec3.sub(p, sep);
      this.drawViewport(gl, 0, 0, width/2, height, scene);
      
      this.position = Vec3.add(p, sep);
      this.drawViewport(gl, width/2, 0, width/2, height, scene);

      this.position = p;
    } else {
      this.drawViewport(gl, 0, 0, width, height, scene);
    }
  }
};

//SCENE_UTIL/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

Mouse = {
  LEFT : 0,
  MIDDLE : 1,
  RIGHT : 2
}

var drawInterval = null;

Scene = function(canvas, scene, cam) {
  if (!scene) scene = new Node();
  if (!cam) cam = Scene.getDefaultCamera();
  this.canvas = canvas;
  this.gl = getGLContext(canvas);
  this.clearBits = this.gl.COLOR_BUFFER_BIT |
                   this.gl.DEPTH_BUFFER_BIT |
                   this.gl.STENCIL_BUFFER_BIT;
  this.scene = scene;
  this.camera = cam;
  this.mouse = {
    x : 0,
    y : 0,
    pressure : 1.0,
    tiltX : 0.0,
    tiltY : 0.0,
    deviceType : 0,
    left: false,
    middle: false,
    right: false
  };
  this.setupEventListeners();
  this.startFrameLoop();
};
Scene.getDefaultCamera = function() {
  var cam = new Camera();
  cam.lookAt = [0, 1.0, 0];
  cam.position = [Math.cos(1)*6, 3, Math.sin(1)*6];
  cam.fov = 45;
  cam.angle = 1;
  return cam;
};
Scene.prototype = {
  frameDuration : 15,
  time : 0,
  timeDir : 1,
  timeSpeed : 1,
  previousTime : 0,
  frameTimes : [],

  bg : [1,1,1,1],
  clear : true,
  useDepth : true,

  startFrameLoop : function() {
    this.previousTime = new Date;
    clearInterval(drawInterval);
    var t = this;
    drawInterval = setInterval(function(){ t.draw(); }, this.frameDuration);
  },

  updateMouse : function(ev) {
    this.mouse.deviceType = ev.mozDeviceType || 0;
    this.mouse.tiltX = ev.mozTiltX || 0;
    this.mouse.tiltY = ev.mozTiltY || 0;
    this.mouse.pressure = ev.mozPressure || 0;
    this.mouse.x = ev.clientX;
    this.mouse.y = ev.clientY;
  },
  
  setupEventListeners : function() {
    var t = this;
    window.addEventListener('mousedown',  function(ev){
      switch (ev.button) {
      case Mouse.LEFT:
        t.mouse.left = true; break;
      case Mouse.RIGHT:
        t.mouse.right = true; break;
      case Mouse.MIDDLE:
        t.mouse.middle = true; break;
      }
      t.updateMouse(ev);
    }, false);
    window.addEventListener('mouseup', function(ev) {
      switch (ev.button) {
      case Mouse.LEFT:
        t.mouse.left = false; break;
      case Mouse.RIGHT:
        t.mouse.right = false; break;
      case Mouse.MIDDLE:
        t.mouse.middle = false; break;
      }
      t.updateMouse(ev);
    }, false);
    window.addEventListener('mousemove', function(ev) {
      t.updateMouse(ev);
    }, false);
  },
  
  clear : function() {
  	this.gl.clearColor(.55, .55, .122, 1);
  },

  draw : function() {
    var newTime = new Date;
    var real_dt = newTime - this.previousTime;
    var dt = this.timeDir * this.timeSpeed * real_dt;
    this.time += dt;
    this.previousTime = newTime;
    
    this.camera.update(this.time, dt);
    this.scene.update(this.time, dt);

    if (this.drawOnlyWhenChanged && !this.changed) return;

    this.gl.clearColor(this.bg[0], this.bg[1], this.bg[2], this.bg[3]);
    if (this.clear)
      this.gl.clear(this.clearBits);
    if (this.useDepth)
      this.gl.enable(this.gl.DEPTH_TEST);
    else
      this.gl.disable(this.gl.DEPTH_TEST);
    if (this.blend)
      this.gl.enable(this.gl.BLEND);
    else
      this.gl.disable(this.gl.BLEND);

    if (this.blendFuncSrc && this.blendFuncDst) {
      this.gl.blendFunc(this.gl[this.blendFuncSrc], this.gl[this.blendFuncDst]);
    }
    
    this.camera.draw(this.gl, this.canvas.width, this.canvas.height, this.scene);
    
    this.updateFps(this.frameTimes, real_dt);
    if (!this.firstFrameDoneTime) this.firstFrameDoneTime = new Date();
    this.changed = false;
  },

  updateFps : function(frames,real_dt) {
    var fps = document.getElementById('fps');
    if (!fps) return;
    var ctx = fps.getContext('2d');
    ctx.clearRect(0,0,fps.width,fps.height);
    frames.push(1000 / (1+real_dt));
    if (frames.length > 200) 
      frames.shift();
    for (var i=0; i<frames.length; i++) {
      ctx.fillRect(i,fps.height,1,-frames[i]/3);
    }
  }
};

var $ = function(id){ return document.getElementById(id); };

Cube = function() {
  Node.call(this, Geometry.Cube.getCachedVBO());
  this.position = [0,0,0];
  this.material = DefaultMaterial.get();
};
Cube.prototype = new Node;
Cube.prototype.makeBounce = function() {
  this.addFrameListener(function(t, dt) {
    var y = 2*Math.abs(Math.sin(t / 400));
    this.position[1] = y;
  });
  return this;
};

DefaultMaterial = {
  get : function() {
    if (!this.cached) { 
      var shader = new Shader(null, 'ppix-vert', 'ppix-frag');
      this.cached = this.setupMaterial(shader);
    }
    return this.cached.copy();
  },
  
  setupMaterial : function(shader) {
    var m = new Material(shader);
    m.floats.MaterialSpecular = [0.95, 0.9, 0.9, 1];
    m.floats.MaterialDiffuse = [0.60, 0.6, 0.65, 1];
    m.floats.MaterialAmbient = [1, 1, 1, 1];
    m.floats.MaterialShininess = 1.5;

    m.floats.LightPos = [7, 7, 7, 1.0];
    m.floats.GlobalAmbient = [0.1, 0.1, 0.2, 1];
    m.floats.LightSpecular = [0.9, 1.0, 1.0, 1];
    m.floats.LightDiffuse = [0.8, 0.9, 0.95, 1];
    m.floats.LightAmbient = [0.1, 0.1, 0.1, 1];
    m.floats.LightConstantAtt = 0.0;
    m.floats.LightLinearAtt = 0.1;
    m.floats.LightQuadraticAtt = 0.0;
    return m;
  }
      
}

        // the goal here is to make simple things simple
        
        // Reasonable defaults:
        // - default shader [with multi-texturing (diffuse, specular, normal?)]
        // - camera position
        // - scene navigation controls
        
        // Simple things:
        // - drawing things with lighting
        // - making things move [like CSS transitions?]
        // - text 
        // - images
        // - painter's algorithm for draw list sort
        // - loading and displaying models
        // - picking

        // Easy fancy things:
        // - rendering to FBOs (scene.renderTarget = fbo)
        
        /*
        ren.scene.addFrameListener(function(t,dt) {
          var l = Matrix.mulv4(ren.camera.getLookMatrix(), [7, 7, 7, 1.0]);
          this.material.floats.LightPos = l
        });
        */

//OBJ_LOADER/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

Obj = function(){};
Obj.load = function(fileData) {
  var o = new Obj();

  o.parse(fileData);
  return o;
}
Obj.prototype = {
  load: function(fileData) {
    
    
          self.parse(fileData);

  },

  onerror : function(xhr) {
    alert("Error: "+xhr.status);
  },

  makeVBO : function(gl) {
    if (this.texcoords) {
      return new VBO(gl,
          {size:3, data: this.vertices},
          {size:3, data: this.normals},
          {size:2, data: this.texcoords}
      )
    } else {
      return new VBO(gl,
          {size:3, data: this.vertices},
          {size:3, data: this.normals}
      )
    }
  },

  cache : {},
  getCachedVBO : function(gl) {
    if (!this.cache[gl])
      this.cache[gl] = this.makeVBO(gl);
    return this.cache[gl];
  },

  parse : function(data) {
    var t = new Date;
    var geo_faces = [],
        nor_faces = [],
        tex_faces = [],
        raw_vertices = [],
        raw_normals = [],
        raw_texcoords = [];
    var lines = data.split("\n");
    var hashChar = '#'.charCodeAt(0);
    for (var i=0; i<lines.length; i++) {
      var l = lines[i];
      var vals = l.replace(/^\s+|\s+$/g, '').split(/[\s]+/);
	  //var vals = l.replace(/^\s+|\s+$/g, '').split(/[\s\/]+/);
      if (vals.length == 0) continue;
      if (vals[0].charCodeAt(0) == hashChar) continue;
      switch (vals[0]) {
        case "g": // named object mesh [group]?
          break;
        case "v":
          raw_vertices.push(parseFloat(vals[1]));
          raw_vertices.push(parseFloat(vals[2]));
          raw_vertices.push(parseFloat(vals[3]));
          break;
        case "vn":
          raw_normals.push(parseFloat(vals[1]));
          raw_normals.push(parseFloat(vals[2]));
          raw_normals.push(parseFloat(vals[3]));
          break;
        case "vt":
          raw_texcoords.push(parseFloat(vals[1]));
          raw_texcoords.push(parseFloat(vals[2]));
          break;
        case "f":
          // triangulate the face as triangle fan
          var faces = [];
          for (var j=1, v; j<vals.length; j++) {
            if (j > 3) {
              faces.push(faces[0]);
              faces.push(v);
            }
            v = vals[j];
            faces.push(v);
          }
          for (var j=0; j<faces.length; j++) {
            var f = faces[j];
            //var a = f.split("/");
            var a = f.split(/[\s\/]+/);
            geo_faces.push(parseInt(a[0]) - 1);
            if (a.length > 1)
              tex_faces.push(parseInt(a[1]) - 1);
            if (a.length > 2)
              nor_faces.push(parseInt(a[2]) - 1);
          }
          break;
      }
    }
    this.vertices = this.lookup_faces(raw_vertices, geo_faces, 3);
    if (tex_faces.length > 0)
      this.texcoords = this.lookup_faces(raw_texcoords, tex_faces, 2);
    if (nor_faces.length > 0 && !this.overrideNormals)
      this.normals = this.lookup_faces(raw_normals, nor_faces, 3);
    else
      this.normals = this.calculate_normals(this.vertices);
    var bbox = {min:[0,0,0], max:[0,0,0]};
    for (var i=0; i<raw_vertices.length; i+=3) {
      var x = raw_vertices[i],
          y = raw_vertices[i+1],
          z = raw_vertices[i+2];
      if (x < bbox.min[0]) bbox.min[0] = x;
      else if (x > bbox.max[0]) bbox.max[0] = x;
      if (y < bbox.min[1]) bbox.min[1] = y;
      else if (y > bbox.max[1]) bbox.max[1] = y;
      if (z < bbox.min[2]) bbox.min[2] = z;
      else if (z > bbox.max[2]) bbox.max[2] = z;
    }
    bbox.width = bbox.max[0] - bbox.min[0];
    bbox.height = bbox.max[1] - bbox.min[1];
    bbox.depth = bbox.max[2] - bbox.min[2];
    bbox.diameter = Math.max(bbox.width, bbox.height, bbox.depth);
    this.boundingBox = bbox;
    this.parseTime = new Date() - t;
  },

  lookup_faces : function(verts, faces, sz) {
    var v = [];
    for (var i=0; i<faces.length; i++) {
      var offset = faces[i] * sz;
      for (var j=0; j<sz; j++)
        v.push(verts[offset+j]);
    }
    return v;
  },

  calculate_normals : function(verts) {
    var norms = [];
    for (var i=0; i<verts.length; i+=9) {
      var normal = this.find_normal(
        verts[i  ], verts[i+1], verts[i+2],
        verts[i+3], verts[i+4], verts[i+5],
        verts[i+6], verts[i+7], verts[i+8]);
      for (var j=0; j<3; j++) {
        norms.push(normal[0]);
        norms.push(normal[1]);
        norms.push(normal[2]);
      }
    }
    return norms;
  },

  find_normal : function(x0,y0,z0, x1,y1,z1, x2,y2,z2) {
    var u = [x0-x1, y0-y1, z0-z1];
    var v = [x1-x2, y1-y2, z1-z2];
    var w = [x2-x0, y2-y0, z2-z0];
    var n = Vec3.cross(u,v);
    if (Vec3.lengthSquare(n) == 0)
      n = Vec3.cross(v,w);
    if (Vec3.lengthSquare(n) == 0)
      n = Vec3.cross(w,u);
    if (Vec3.lengthSquare(n) == 0)
      n = [0,0,1];
    return Vec3.normalize(n);
  }

}

//INITIALIZATION CODE/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    
  

    alert_webGL = function(string) {
		
        alert(string);

    }

    hide_webGL = function(){
    	//g_s.clear();
       //g_ctx.clearRect(0,0,480,360);
        clearInterval(drawInterval);

    }
    
        //Global Node & Scene
    	var g_n = new Node();   

     
    
    texture_webGL = function(t_num){
    
        var tex = new Texture();
        tex.image = new Image();
        if (t_num == 1)
        	tex.image.src = 'images/texture_metal.jpg';
        else if(t_num == 2)
        	tex.image.src = 'images/texture_wood.jpg';
        else if(t_num == 3)
        	tex.image.src = 'images/texture_grass.jpg';
        $('info').innerHTML = 'Loading texture (56kB)...';
        tex.image.onload = function(){
        	   $('info').innerHTML = '';
    		   g_n.material = DefaultMaterial.get();
               g_n.material.textures.DiffTex = tex;
               s.scene.appendChild(g_n);
        }
    }

         
    init_webGL = function(fileData) {

		var c = $('c');
		var g_s = new Scene($('c'));  
       
        $('info').innerHTML = 'Loading model...';
        var w = Obj.load(fileData);
        //w.onload = function() {
          g_s.camera.position = [0, 2, 7];
          var tex = new Texture();
          tex.image = new Image();
          tex.image.src = 'images/texture_wood.jpg';
          $('info').innerHTML = 'Loading image texture...';
          tex.image.onload = function(){
            $('info').innerHTML = '';
            
            var sc = 4.0 / (w.boundingBox.diameter);
            g_n.scaling = [sc, sc, sc];
            g_n.model = w.makeVBO();
            g_n.position[1] = 0.5;
//             g_n.rotation.axis = [1,0,0];
//             g_n.rotation.angle = -Math.PI/2;
            g_n.material = DefaultMaterial.get();
            g_n.material.textures.DiffTex = tex;
            
            g_n.material.floats.LightDiffuse = [1,1,1,1];
          g_n.material.floats.MaterialShininess = 6.0;
          g_n.material.floats.MaterialDiffuse = [1,1,1,1];

           
            g_s.scene.appendChild(g_n);
            
            var xRot = new Node();
          xRot.rotation.axis = [0, 1, 0];
          var yRot = new Node();
          yRot.rotation.axis = [1, 0, 0];
          yRot.appendChild(xRot);
          xRot.appendChild(g_n);
          var wheelHandler = function(ev) {
            var ds = ((ev.detail || ev.wheelDelta) < 0) ? 1.1 : (1 / 1.1);
            if (ev.shiftKey) {
              yRot.scaling[0] *= ds;
              yRot.scaling[1] *= ds;
              yRot.scaling[2] *= ds;
            } else {
              g_s.camera.targetFov *= ds;
            }
            g_s.changed = true;
            ev.preventDefault();
          };
          g_s.camera.addFrameListener(function() {
            if (Math.abs(this.targetFov - this.fov) > 0.01) {
              g_s.changed = true;
            }
          });
          c.addEventListener('DOMMouseScroll', wheelHandler, false);
          c.addEventListener('mousewheel', wheelHandler, false);
          
           c.addEventListener('mousedown', function(ev){ 
            this.dragging = true;
            this.sx = ev.clientX;
            this.sy = ev.clientY;
            ev.preventDefault();
          }, false);
          window.addEventListener('mousemove', function(ev) {
            if (c.dragging) {
              var dx = ev.clientX - c.sx, dy = ev.clientY - c.sy;
              c.sx = ev.clientX, c.sy = ev.clientY;
              if (g_s.mouse.left) {
                xRot.rotation.angle += dx / 200;
                yRot.rotation.angle += dy / 200;
              } else if (g_s.mouse.middle) {
                yRot.position[0] += dx * 0.01 * (g_s.camera.fov / 45);
                yRot.position[1] -= dy * 0.01 * (g_s.camera.fov / 45);
              }
              ev.preventDefault();
              g_s.changed = true;
            }
          }, false);
          window.addEventListener('mouseup', function(ev) {
            if (c.dragging) {
              c.dragging = false;
              ev.preventDefault();
            }
          }, false);
          g_s.changed = true;
			g_s.scene.appendChild(yRot);
          }
        //}
      }
	  