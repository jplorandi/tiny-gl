package com.perrotuerto.tinygl;

import android.opengl.GLES20;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Created by Juan on 5/27/2016.
 */
public abstract class AbstractRenderer implements CeeSuiteRenderer {
  public static final int MILLISECONDS_IN_DAY = 24 * 60 * 60 * 1000;
  public float[] time = new float[4];
  public float[] tod = new float[2];
  protected Runner runner = new Runner();
  protected float[] iCamera = new float[2];
  protected float[] iResolution = new float[2];
  protected float[] mProjMatrix = new float[16];
  protected float[] mVMatrix = new float[16];
  protected float[] mMVPMatrix = new float[16];
  protected float deltaTime;
  protected List<MaterialMesh> bucket = new ArrayList<MaterialMesh>();
  protected List<Renderable> renderBucket = new ArrayList<Renderable>();
  protected MaterialMesh currentMM;
  protected long ltc = 0;
  protected float tfps = 0;

  @Override
  public void runOnGlThread(Callable callable) {
    runner.runOnGlThread(callable);
  }

  protected void setupInitialFrameState() {

    GLES20.glClearColor(0f, 0f, 0f, 0f);
    GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
    GLES20.glEnable(GLES20.GL_CULL_FACE);
    GLES20.glDisable(GLES20.GL_DEPTH_TEST);
  }

  protected void computeTime() {
    final Calendar calendar = Calendar.getInstance();
    time[0] = calendar.get(Calendar.YEAR);
    time[1] = calendar.get(Calendar.MONTH);
    time[2] = calendar.get(Calendar.DAY_OF_MONTH);
    long ctc = System.currentTimeMillis();
    time[3] = (ctc % MILLISECONDS_IN_DAY) / 1000f;

    tod[0] = (int) Math.floor(time[3]);
    tod[1] = (time[3] - tod[0]) % 60;

    long delta = ctc - ltc;
    deltaTime = (delta / 1000f);
    tfps = 1f / deltaTime;
    ltc = ctc;

//    if (fps != null)
//      fps.post(new Runnable() {
//        @Override
//        public void run() {
//          fps.setText(String.format("FPS: %.2f", tfps));
//        }
//      });

  }
}
