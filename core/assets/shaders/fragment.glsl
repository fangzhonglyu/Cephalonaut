#ifdef GL_ES
    precision mediump float;
#endif

varying vec4 v_color;
varying vec2 v_texCoords;
uniform sampler2D u_texture;
uniform mat4 u_projTrans;

vec2 v_texCoord0;
uniform float u_time;

// black hole
#define PI 3.14159

const float R0 = 50.0;
const float R1 = 60.0;
const float R2 = 300.0;

const int P = 1;

uniform vec2 u_bh;
uniform vec2 u_res;

void main() {	
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
