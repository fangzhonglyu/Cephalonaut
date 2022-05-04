#ifdef GL_ES
    precision mediump float;
#endif

varying vec4 v_color;
varying vec2 v_texCoords;
uniform sampler2D u_texture;
uniform mat4 u_projTrans;

vec2 v_texCoord0;
uniform float time;

void main0();
void main1();
void main2();
void main3();

void main4() {
	gl_FragColor = texture(u_texture, v_texCoords);
}

void main() {
	main3();
}

void main0() {
    vec4 color = texture2D(u_texture, v_texCoords).rgba;
    float gray = (color.r + color.g + color.b) / 3.0;
    vec3 grayscale = vec3(gray);

    gl_FragColor = vec4(grayscale, color.a);
}

void main1() {		
	float distance = distance(v_texCoord0, vec2(950, 550));
	gl_FragColor = texture2D(u_texture, v_texCoord0 + smoothstep(time, 0, distance));
}

// black hole
#define PI 3.14159

const float M = 2.0; // mass
const float D = 1.0;
const float X = 0.15;

void main2() {
    vec2 R = vec2(1920, 1080), p = (gl_FragCoord.xy + gl_FragCoord.xy - R) / R.y;
    vec2 m = vec2(1500, 550), mp = (m + m - R) / R.y;
    
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
    
    gl_FragColor = texture(u_texture, off) * smoothstep(0., .04, length(p - mp) - X); //texture(iChannel0, uv);
}

const float R0 = 50.0;
const float R1 = 60.0;
const float R2 = 300.0;

const int P = 1;

uniform vec2 u_bh;
uniform vec2 u_res;

void main3() {	
	vec2 res = u_res;
	vec2 bh = u_bh;
	
	vec2 offset = bh - gl_FragCoord.xy;
	float distance = length(offset);
	
	vec2 uv = gl_FragCoord.xy / res;
	
	if (distance < R2) {
		float alpha = smoothstep(pow(R0, P), pow(R2, P), pow(R2 - distance, P));
		gl_FragColor = texture(u_texture, v_texCoords + offset / res * alpha); // * smoothstep(R0, R1, distance);
		gl_FragColor.a = 1.0;
		// gl_FragColor = texture(u_texture, v_texCoords + offset);
	} else {
		gl_FragColor = texture(u_texture, v_texCoords);
	}
}
