package com.perrotuerto.tinygl.painter

import android.app.Activity
import android.view.MotionEvent
import com.perrotuerto.tinygl.CeeSuiteRenderer
import com.perrotuerto.tinygl.MaterialMesh
import com.perrotuerto.tinygl.Renderable
import com.perrotuerto.tinygl.core.ShaderProgram
import com.perrotuerto.tinygl.core.Texture

import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import java.util.concurrent.Callable

/**
 * Created by Juan on 5/27/2016.
 */
class PaintRenderer implements CeeSuiteRenderer {
  private Activity parent

  PaintRenderer(Activity parent) {
    this.parent = parent
  }

  @Override
  MaterialMesh addMM(float x, float y, float width, float height, Texture texture, ShaderProgram program, float r, float g, float b, float a) {
    return null
  }

  @Override
  void runOnGlThread(Callable callable) {

  }

  @Override
  void actionDown(MotionEvent event) {

  }

  @Override
  void actionUp(MotionEvent event) {

  }

  @Override
  void actionMove(MotionEvent event) {

  }

  @Override
  void addRenderable(Renderable renderable) {

  }

  @Override
  void addAllRenderable(List<? extends Renderable> renderables) {

  }

  @Override
  void requestUpdate() {

  }

  @Override
  void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {

  }

  @Override
  void onSurfaceChanged(GL10 gl10, int i, int i1) {

  }

  @Override
  void onDrawFrame(GL10 gl10) {

  }
}
