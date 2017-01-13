
uniform mat4 uMVMatrix;
uniform mat4 uMVPMatrix;
uniform mat4 uShadowProjMatrix;
//uniform mat4 uNormalMatrix;


attribute vec4 aPosition;
attribute vec4 aNormal;

varying vec3 vPosition;
varying vec4 vShadowCoord;
varying vec3 vNormal;
varying vec2 vTexel;



void main() {

    vec4 p = vec4(aPosition.xyz, 1);
    gl_Position = uMVPMatrix * p;

    vPosition = (uMVMatrix * p).xyz;
    vNormal = normalize((uMVMatrix * vec4(aNormal.xyz, 0)).xyz);
    vShadowCoord = uShadowProjMatrix * p;
    vTexel = vec2(aPosition.w, aNormal.w);
}