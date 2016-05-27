package com.perrotuerto.tinygl.painter

import android.opengl.GLSurfaceView
import android.os.Bundle
import android.support.annotation.Nullable
import android.support.v7.app.AppCompatActivity
import android.view.Window
import android.view.WindowManager
import com.perrotuerto.tinygl.GL20Surface
import com.perrotuerto.tinygl.core.ShaderProgram
import com.perrotuerto.tinygl.core.Texture
import com.perrotuerto.tinygl.core.TextureManager

import java.util.concurrent.Callable

class PaintActivity extends AppCompatActivity {
  PaintRenderer renderer

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    requestWindowFeature Window.FEATURE_NO_TITLE
    getWindow().setFlags WindowManager.LayoutParams.FLAG_FULLSCREEN,
        WindowManager.LayoutParams.FLAG_FULLSCREEN
    getWindow().addFlags WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON

    super.onCreate(savedInstanceState)

    setContentView(R.layout.camera_main)

    GL20Surface gl20Surface = (GL20Surface) findViewById(R.id.surface)
    renderer = new PaintRenderer(this)
    gl20Surface.setRenderer(renderer)
    gl20Surface.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY)

    renderer.runOnGlThread(new Callable() {
      @Override
      public Object call() throws Exception {
        Texture texture = TextureManager.get("textures/tex16.png", true)

        def plain = ShaderProgram.get(
            "shaders/simple-vert.glsl",
            "shaders/cam-frag.glsl",
            ["a_pos", "a_tex", "a_col"],
            ["u_MVPMatrix", "time", "tod", "iResolution", "toolTexture", "framebuffer"]
        )

        def fire = ShaderProgram.get(
            "shaders/simple-vert.glsl",
//            "shaders/cam-frag.glsl",
            "shaders/fire-frag.glsl",
            ["a_pos", "a_tex", "a_col"],
            ["u_MVPMatrix", "time", "tod", "iResolution", "toolTexture", "framebuffer"]
        )

        def hue = ShaderProgram.get(
            "shaders/simple-vert.glsl",
//            "shaders/cam-frag.glsl",
            "shaders/hue-frag.glsl",
            ["a_pos", "a_tex", "a_col"],
            ["u_MVPMatrix", "time", "tod", "iResolution", "toolTexture", "framebuffer"]
        )

        def cameraMesh = renderer.addMM(-1f, -1f, -1f, -1f,
            texture, plain, 1f, 1f, 1f, 1f)
//        cameraMesh = renderer.addMM(
//            -1, -1, -1, -1,
//            texture, fire, 1f, 1f, 1f, 1f)

        return null
      }
    })
  }


}