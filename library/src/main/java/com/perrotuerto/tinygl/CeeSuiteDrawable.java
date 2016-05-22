package com.perrotuerto.tinygl;

import com.perrotuerto.tinygl.core.Texture;

/**
 * @author jp.lorandi@cfyar.com Date: 1/30/15 Time: 11:08 PM
 */
public abstract class CeeSuiteDrawable implements Renderable {

  public abstract void setUniformMatrix(String name, float[] value);
  public abstract void setUniformVec4(String name, float [] value);
  public abstract void setUniformVec2(String name, float [] value);

  public abstract void setUniformFloat(String name, float value);
  public abstract void setUniformI2(String name, int[] value);

  public abstract void setUniformTexture(String name, Texture texture, int unit);

}
