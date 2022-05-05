#ifdef GL_ES
    precision mediump float;
#endif

varying vec4 v_color;
varying vec2 v_texCoords;
uniform sampler2D u_texture;
uniform mat4 u_projTrans;

vec2 v_texCoord0;

const float pi = 3.141592654;
const vec4 accretion = vec4(1, 0.1, 0.6, 1);

uniform vec2 u_bh;
uniform vec2 u_res;
uniform float u_time;

void main() {
    vec4 color = texture(u_texture, v_texCoords);

    vec4 pix_coord = floor(gl_FragCoord / 4.0) * 4.0;
	vec2 offset = pix_coord.xy - u_bh;
    vec2 uv = (pix_coord.xy - u_bh) / u_res.yy;
    
	float dist = length(offset);
	float r = length(uv);
    float theta = atan(uv.y, uv.x);
    bool spiral = fract(2.5 * theta / pi + 7.0 * pow(r, 0.4) - 0.3 * u_time) < 0.5;
    
    if (spiral) {
        float re = sin((r * 50.0 + u_time * 5.0) * 1.0) / 2.0 + 1.0;
        re *= max(0.0, 1.5 - r * 4.0);
        color *= mix(color, mix(vec4(1, 1, 1, 1), accretion, re), 0.5);
    }
    
    gl_FragColor = color;
}