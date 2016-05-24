uniform mat4 u_MVPMatrix;
uniform vec4 time;
uniform vec2 tod;

attribute vec4 a_pos;
attribute vec2 a_tex;
attribute vec4 a_col;

varying vec4 v_pos;
varying vec2 v_abs_pos;
varying vec4 v_tex;
varying vec4 v_col;

//varying float iGlobalTime;

void main() {
   v_abs_pos = a_pos.xy;
   v_pos = u_MVPMatrix * a_pos;
   v_tex = vec4(a_tex, 0.0, 0.0);
   v_col = a_col;

//   iGlobalTime = time.a;

   gl_Position = v_pos;
}
