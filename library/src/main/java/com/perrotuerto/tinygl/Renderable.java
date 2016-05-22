package com.perrotuerto.tinygl;

/**
 * @author jp.lorandi@cfyar.com Date: 2/1/15 Time: 5:43 PM
 */
public interface Renderable {

  public float[] transform();
  public void render(float[] mvp);
  public void update(float deltaTime);

}
