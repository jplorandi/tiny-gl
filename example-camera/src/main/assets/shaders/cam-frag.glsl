#extension GL_OES_EGL_image_external : require

precision mediump float;

#define TWO_PI 6.2831854
uniform vec4 time;
uniform vec2 tod;
uniform float flip;

uniform samplerExternalOES toolTexture;
uniform sampler2D framebuffer;

varying vec4 v_pos;
varying vec4 v_tex;
varying vec4 v_col;

void main() {
    vec3 cam = texture2D(toolTexture, v_tex.xy).rgb;
    gl_FragColor = vec4(cam, 1.0);

 // gl_FragColor = vec4(v_pos.xyz, 1.0);

//  vec4 mixed = v_pos * v_tex * 0.01;
//  gl_FragColor = (v_col + mixed) * vec4(v_pos.xy, fract(time.w), 1.0);
//  gl_FragColor = texture2D(framebuffer, vec2(v_tex.x, v_tex.y * flip));
}
