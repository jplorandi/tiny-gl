precision highp float;

uniform sampler2D toolTexture;
//uniform sampler2D framebuffer;
uniform vec2 iResolution;
uniform vec4 time;
uniform vec2 tod;

varying vec4 v_pos;
varying vec4 v_tex;
varying vec4 v_col;
//varying float iGlobalTime;

void main() {
    float iGlobalTime = tod.y;

  //gl_FragColor = vec4(v_pos.xyz, 1.0);
    vec4 diffuse = texture2D(toolTexture, v_tex.xy);
    //vec2 uv = gl_FragCoord.xy / iResolution.xy;
	gl_FragColor = vec4(v_tex.xy,0.5+0.5*sin(iGlobalTime),1.0) + (diffuse * 0.01);
//  vec4 mixed = v_pos * v_tex * 0.01;
//  gl_FragColor = (v_col + mixed) * vec4(v_pos.xy, fract(time.w), 1.0);
//  gl_FragColor = texture2D(framebuffer, vec2(v_tex.x, v_tex.y * flip));
}