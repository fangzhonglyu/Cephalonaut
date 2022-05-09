#ifdef GL_ES
    precision mediump float;
#endif

varying vec4 v_color;
varying vec2 v_texCoords;
uniform sampler2D u_texture;
uniform mat4 u_projTrans;

vec2 v_texCoord0;

const float pi = 3.141592654;
const vec4 accretion = vec4(1.0, 0.6, 0.0, 1.0);

const float R2 = 300.0;

uniform vec3 u_bh[20];
uniform int u_bh_count;

uniform vec2 u_res;
uniform float u_time;

void main() {
    vec4 color = texture(u_texture, v_texCoords);
	
	for (int i = 0; i < u_bh_count; i++) {
		
		vec4 pix_coord = floor(gl_FragCoord / 4.0) * 4.0;
		// vec2 offset = pix_coord.xy - u_bh[i].xy;
		// vec2 uv = offset / u_res.yy;
		
		// float dist = length(offset);
		// if (dist > u_bh[i].z || dist < 40) continue;
		
		// float r = length(uv);
		// float theta = atan(uv.y, uv.x);
		// bool spiral = fract(2.5 * theta / pi + 7.0 * pow(r, 0.4) - 0.5 * u_time) < 0.5;
		
		// vec4 c = spiral ? accretion : vec4(.6, .5, .3, 0.);
		
		// float re = sin((r * 50.0 + u_time * 5.0) * 1.0) / 2.0 + 1.0;
		// re *= max(0.0, 1.5 - r * 4.0);
		// color *= mix(color, c, re);
		
		vec2 offset = pix_coord.xy - u_bh[i].xy;
		vec2 uv = offset / u_res.yy;
		
		float r = length(offset);
		if (r > u_bh[i].z || r < 40) continue;
		float r2 = r / u_res.y;
		float theta = atan(uv.y, uv.x);
		bool spiral = fract(2.5 * theta / pi + 7.0 * pow(r2, 0.4) - 0.5 * u_time) < 0.5;
		
		vec4 c = spiral ? accretion : vec4(.6, .5, .3, 0.);
		
		float re = sin((r2 * 50.0 + u_time * 5.0) * 1.0) / 2.0 + 1.0;
		re *= smoothstep(0, u_bh[i].z - 45, u_bh[i].z - r);
		color = mix(color, c, re * 0.3);
	}
    
    gl_FragColor = color;
}