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

const float R0 = 0.0;

const float P = 2.0;

uniform vec3 u_bh[20];
uniform int u_bh_count;
uniform vec2 u_res;

void main() {	
	vec2 coords = v_texCoords;
	
	for (int i = 0; i < u_bh_count; i++) {
		vec2 offset = u_bh[i].xy - gl_FragCoord.xy;
		float distance = length(offset);
		
		//vec2 uv = gl_FragCoord.xy / u_res;
		float radius = u_bh[i].z;
		
		if (distance < radius) {
			float alpha = smoothstep(0, pow(radius - R0, P), pow(radius - distance, P));
			coords += offset / u_res * alpha; // * smoothstep(R0, R1, distance);
		}
	}
	
	gl_FragColor = texture(u_texture, coords);
}
