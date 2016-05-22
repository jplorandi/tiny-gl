package com.perrotuerto.tinygl;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * @author jp.lorandi@cfyar.com Date: 2/1/15 Time: 4:32 PM
 */
public class LinearSequenceAnimationPath implements AnimationPath {

  private float[] tmp = new float[2];
  private List<LinearAnimationPath> paths = new ArrayList<LinearAnimationPath>();
  int current = 0;
  float time;

  public LinearSequenceAnimationPath(LinearAnimationPath... paths) {
    for (LinearAnimationPath path : paths) {
      this.paths.add(path);
    }
    reset();
  }

  public LinearSequenceAnimationPath(List<LinearAnimationPath> paths) {
    this.paths.addAll(paths);
    reset();
  }

  public void reset() {
    current = 0;
    time = 0;
  }

  public void add(LinearAnimationPath path) {
    this.paths.add(path);
  }

  @Override
  public float[] getPosition(float deltaTime) {
    if (current == -1) {
//      Log.i("LASP", "Anim Completed");
      return tmp;
    }

    LinearAnimationPath c = paths.get(current);

    if (deltaTime > 1f)
      deltaTime = 0f; //sometimes there are delays...

    float nt = time + deltaTime;
//    Log.i("LASP", String.format("NT: %f agg: %f", nt, aggAnimTime()));
    while (aggAnimTime() < nt) {
      current++;
//      Log.i("LASP", String.format("Current: %d NT: %f agg: %f", current, nt, aggAnimTime()));
      if (current == paths.size()) {
//        Log.i("LASP", "Anim Reached End");
        current = -1;
        return tmp;
      }
      c = paths.get(current);
    }

    float timeInCurr = time - aggAnimTimePrev();

    aggAnimTxPrev();

    tmp[0] += c.dx * timeInCurr;
    tmp[1] += c.dy * timeInCurr;

//    Log.i("LASP", String.format("Pos: %f , %f", tmp[0], tmp[1]));

    time += deltaTime;

    return tmp;
  }

  private float aggAnimTime() {
    float rval = 0;
    for (int i = 0; i < paths.size(); i++) {
      LinearAnimationPath path = paths.get(i);
      rval += path.animTime;
      if (i == current) {
        break;
      }
    }

    return rval;
  }

  private float aggAnimTimePrev() {
    float rval = 0;
    for (int i = 0; i < paths.size(); i++) {
      LinearAnimationPath path = paths.get(i);
      if (i == current) {
        break;
      }
      rval += path.animTime;
    }

    return rval;
  }

  private void aggAnimTxPrev() {
    tmp[0] = 0f;
    tmp[1] = 0f;
    for (int i = 0; i < paths.size(); i++) {
      LinearAnimationPath path = paths.get(i);
      if (i == current) {
        break;
      }
      tmp[0] += path.dx * path.animTime;
      tmp[1] += path.dy * path.animTime;
    }
  }

  public static class LinearAnimationPath {

    public float dx, dy, animTime;

    public LinearAnimationPath(float dx, float dy, float animTime) {
      this.dx = dx;
      this.dy = dy;
      this.animTime = animTime;

      Log.i("LAnimPath", String.format("dx: %f dy: %f animTime: %f", dx, dy, animTime));
    }

    public static LinearAnimationPath trans(float dx, float dy, float animTime) {
      return new LinearAnimationPath(
          dx / animTime,
          dy / animTime,
          animTime
      );
    }
  }
}
