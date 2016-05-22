package com.perrotuerto.tinygl;

import android.opengl.GLSurfaceView;
import android.view.MotionEvent;

import com.perrotuerto.tinygl.core.ShaderProgram;
import com.perrotuerto.tinygl.core.Texture;

import java.util.List;
import java.util.concurrent.Callable;

/**
 * @author jp.lorandi@cfyar.com Date: 1/30/15 Time: 11:13 PM
 */
public interface CeeSuiteRenderer extends GLSurfaceView.Renderer {

  public MaterialMesh addMM(float x, float y, float width, float height, Texture texture,
                    ShaderProgram program,
                    float r, float g, float b, float a);

  public void runOnGlThread(Callable callable);

  void actionDown(MotionEvent event);

  void actionUp(MotionEvent event);

  void actionMove(MotionEvent event);

  void addRenderable(Renderable renderable);
  void addAllRenderable(List<? extends Renderable> renderables);

  void requestUpdate();
}
