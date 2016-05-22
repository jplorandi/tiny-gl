package com.perrotuerto.tinygl;

import android.opengl.GLES20;
import android.util.Log;

import com.perrotuerto.tinygl.core.QuadMesh;
import com.perrotuerto.tinygl.core.ShaderProgram;
import com.perrotuerto.tinygl.core.Texture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.perrotuerto.tinygl.GLUtils.checkGl20Error;

/**
 * @author jp.lorandi@cfyar.com Date: 1/30/15 Time: 11:03 PM
 */
public class MaterialMesh extends CeeSuiteDrawable {

  private static final Logger log =
      LoggerFactory.getLogger(MaterialMesh.class);

  protected QuadMesh stroke;
  protected Texture texture;
  protected ShaderProgram program;
  public Object userTag;

  public MaterialMesh(float x, float y, float width, float height, ShaderProgram program,
                      Texture texture, float r, float g, float b, float a) {
    this.program = program;
    this.texture = texture;
    stroke = QuadMesh.get(1);
    stroke.setActiveColor(r,g,b,a);

    stroke.quad(0, x, y, width, height);
    stroke.setQuads(1);
    stroke.updateVbo(0,0);

  }

  public void setProgram(ShaderProgram program) {
    this.program = program;
  }

  @Override
  public float[] transform() {
    return stroke.transform;
  }

  @Override
  public void update(float deltaTime) {

  }

  @Override
  public void render(float [] mvp) {
    checkGl20Error("MaterialMesh pre-update");
    update();
    checkGl20Error("MaterialMesh - post update");

    program.bind(stroke);
    GLES20.glEnable(GLES20.GL_BLEND);
    GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
    checkGl20Error("MaterialMesh - pre texture checks");
//    Log.w("MaterialMesh", "I touch myself: " + texture.getTextureId());
    if (texture != null) {
      texture.checkValid();
      program.setUniformTexture("toolTexture", texture, 1);
    } else {
      Log.d("MaterialMesh", "No texture");
    }

    checkGl20Error("MaterialMesh - prerender");
//    Log.w("MaterialMesh", "Rendering");
//    log.warn("RRRRRendering");

    stroke.render();
    checkGl20Error("MaterialMesh - post render");

    program.unbind();
    checkGl20Error("MaterialMesh - post shader unbind");
  }

  private void update() {
    checkGl20Error("MaterialMesh update check");
  }

  @Override
  public void setUniformMatrix(String name, float[] value) {
    update();
    program.setUniformM4(name, value);
    checkGl20Error("MaterialMesh post set uniform M4");
  }

  @Override
  public void setUniformVec4(String name, float[] value) {
    update();
    program.setUniformV4(name, value);
    checkGl20Error("MaterialMesh post set uniform V4");
  }

  @Override
  public void setUniformVec2(String name, float[] value) {
    update();
    program.setUniformV2(name, value);
    checkGl20Error("MaterialMesh post set uniform V2");
  }

  @Override
  public void setUniformFloat(String name, float value) {
    update();
    program.setUniformV(name, value);
    checkGl20Error("MaterialMesh post set uniform V2");
  }

  @Override
  public void setUniformTexture(String name, Texture texture, int unit) {
    update();
    program.setUniformTexture(name, texture, unit);
    checkGl20Error("MaterialMesh post set uniform T");
  }

  @Override
  public void setUniformI2(String name, int[] value) {
    update();
    program.setUniformI2(name, value);
    checkGl20Error("MaterialMesh post set uniform V2");

  }
}
