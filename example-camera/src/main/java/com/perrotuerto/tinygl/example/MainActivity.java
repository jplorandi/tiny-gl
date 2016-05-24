package com.perrotuerto.tinygl.example;

import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;

import com.perrotuerto.tinygl.GL20Surface;
import com.perrotuerto.tinygl.MaterialMesh;
import com.perrotuerto.tinygl.core.ShaderProgram;
import com.perrotuerto.tinygl.core.Texture;
import com.perrotuerto.tinygl.core.TextureManager;

import java.util.concurrent.Callable;

public class MainActivity extends AppCompatActivity {

  private CameraEffectsRenderer renderer;
  private ShaderProgram plain;
  private MaterialMesh cameraMesh;
  private ShaderProgram fire;
  private ShaderProgram hue;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                         WindowManager.LayoutParams.FLAG_FULLSCREEN);
    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

    super.onCreate(savedInstanceState);
    setContentView(R.layout.camera_main);

    GL20Surface gl20Surface = (GL20Surface) findViewById(R.id.surface);
    renderer = new CameraEffectsRenderer(this);
    gl20Surface.setRenderer(renderer);
    gl20Surface.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);

    renderer.runOnGlThread(new Callable() {
      @Override
      public Object call() throws Exception {
        Texture texture = TextureManager.get("textures/tex16.png", true);

        plain = ShaderProgram.get(
            "shaders/simple-vert.glsl",
            "shaders/cam-frag.glsl",
            new String[]{"a_pos", "a_tex", "a_col"},
            new String[]{"u_MVPMatrix", "time", "tod", "iResolution", "toolTexture", "framebuffer"}
        );

        fire = ShaderProgram.get(
            "shaders/simple-vert.glsl",
//            "shaders/cam-frag.glsl",
            "shaders/fire-frag.glsl",
            new String[]{"a_pos", "a_tex", "a_col"},
            new String[]{"u_MVPMatrix", "time", "tod", "iResolution", "toolTexture", "framebuffer"}
        );

        hue = ShaderProgram.get(
            "shaders/simple-vert.glsl",
//            "shaders/cam-frag.glsl",
            "shaders/hue-frag.glsl",
            new String[]{"a_pos", "a_tex", "a_col"},
            new String[]{"u_MVPMatrix", "time", "tod", "iResolution", "toolTexture", "framebuffer"}
        );

        cameraMesh = renderer.addMM(
            -1, -1, -1, -1,
            renderer.getCameraTexture(), plain, 1f, 1f, 1f, 1f);
//        cameraMesh = renderer.addMM(
//            -1, -1, -1, -1,
//            texture, fire, 1f, 1f, 1f, 1f);

        return null;
      }
    });

  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_main, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();

    //noinspection SimplifiableIfStatement
    if (id == R.id.action_settings) {
      return true;
    }

    return super.onOptionsItemSelected(item);
  }
}
