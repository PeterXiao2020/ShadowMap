
precision highp float;

// model-view projection matrix
uniform mat4 uMVPMatrix;

// position of the vertices
attribute vec4 aPosition;

void main() {

	gl_Position = uMVPMatrix * vec4(aPosition.xyz, 1.0f);

}