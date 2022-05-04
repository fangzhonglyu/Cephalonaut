// libgdx
attribute vec4 a_position;
attribute vec4 a_color;
attribute vec2 a_texCoord0;

uniform mat4 u_projTrans;

varying vec4 v_color;
varying vec2 v_texCoords;


// black hole
#define PI 3.14159

const float M = 25.5; // mass
const float D = 5.0;
const float X = 0.15;


void main() {
    v_color = a_color;
    v_texCoords = a_texCoord0;
    gl_Position = u_projTrans * a_position;
}

/*
void main2() {
    v_color = a_color;
    v_texCoords = a_texCoord0;
    gl_Position = u_projTrans * a_position;
	
	
    vec2 R = vec2(1920, 1080), p = (gl_FragCoord.xy + gl_FragCoord.xy - R) / R.y;
    vec2 m = vec2(950, 550), mp = (m + m - R) / R.y;
    
    //float a = R.x / R.y;
    //vec2 as = vec2(a, 1);
    
    vec2 off = p - mp;
    
    float d = length(off); // distance from center of world
    //float x = length(p - mp) - X;

    float r = d * sqrt(D);
    
    float ma = PI * pow(X, 3.) * M; // mass
    float de = (ma) * 1. / (r * r);
    
    off -= de * (1. - de);
    
    off += mp;
    
    gl_Position = texture(iChannel0, off) * smoothstep(0., .04, length(p - mp) - X); //texture(iChannel0, uv);
}*/