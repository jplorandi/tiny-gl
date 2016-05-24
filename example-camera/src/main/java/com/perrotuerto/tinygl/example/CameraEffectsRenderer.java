package com.perrotuerto.tinygl.example;

import android.app.Activity;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.view.MotionEvent;

import com.perrotuerto.tinygl.CeeSuiteRenderer;
import com.perrotuerto.tinygl.MaterialMesh;
import com.perrotuerto.tinygl.Renderable;
import com.perrotuerto.tinygl.Runner;
import com.perrotuerto.tinygl.core.ShaderProgram;
import com.perrotuerto.tinygl.core.Texture;
import com.perrotuerto.tinygl.core.TextureManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.Callable;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static com.perrotuerto.tinygl.GLUtils.checkGl20Error;

/**
 * @author jp.lorandi@cfyar.com Date: 5/24/16 Time: 9:57 AM
 */
public class CameraEffectsRenderer implements CeeSuiteRenderer {

  private static final Logger log =
      LoggerFactory.getLogger(CameraEffectsRenderer.class);

  private final Activity parent;
  private Runner runner = new Runner();

  private Camera camera;
  private SurfaceTexture surfaceTexture;
  private Texture cameraTexture;

  private float[] iCamera = new float[2];
  private float[] iResolution = new float[2];
  private float[] mProjMatrix = new float[16];
  private float[] mVMatrix = new float[16];
  private float[] mMVPMatrix = new float[16];

  public static final int MILLISECONDS_IN_DAY = 24 * 60 * 60 * 1000;
  public float[] time = new float[4];
  public float[] tod = new float[2];
  private long ltc = 0;
  private float tfps = 0;
  private float deltaTime;

  private List<MaterialMesh> bucket = new ArrayList<MaterialMesh>();
  private List<Renderable> renderBucket = new ArrayList<Renderable>();
  private MaterialMesh currentMM;



  public CameraEffectsRenderer(Activity parent) {
    this.parent = parent;
    cameraTexture = TextureManager.external();
  }

  @Override
  public MaterialMesh addMM(float x, float y, float width, float height, Texture texture,
                            ShaderProgram program, float r, float g, float b, float a) {
    if (x == -1)
      x = iResolution[0] / 2f;
    if (y == -1)
      y = iResolution[1] / 2f;

    if (width == -1) {
      width = iResolution[0];
    }
    if (height == -1) {
      height = iResolution[1];
    }

    MaterialMesh materialMesh = new MaterialMesh(x, y, width, height, program, texture,
                                                 r, g, b, a);

    //materialMesh.setUniformVec4("a_color");
    if (currentMM != null) {
      bucket.add(currentMM);
    }

    currentMM = materialMesh;
    return materialMesh;
  }

  @Override
  public void runOnGlThread(Callable callable) {
    runner.runOnGlThread(callable);
  }

  @Override
  public void actionDown(MotionEvent event) {

  }

  @Override
  public void actionUp(MotionEvent event) {

  }

  @Override
  public void actionMove(MotionEvent event) {

  }

  @Override
  public void addRenderable(Renderable renderable) {

  }

  @Override
  public void addAllRenderable(List<? extends Renderable> renderables) {

  }

  @Override
  public void requestUpdate() {

  }

  @Override
  public void onSurfaceCreated(GL10 gl, EGLConfig config) {
    // Position the eye behind the origin.
    final float eyeX = 0.0f;
    final float eyeY = 0.0f;
    final float eyeZ = 1.5f;

    // We are looking toward the distance
    final float lookX = 0.0f;
    final float lookY = 0.0f;
    final float lookZ = -5.0f;

    // Set our up vector. This is where our head would be pointing were we holding the camera.
    final float upX = 0.0f;
    final float upY = 1.0f;
    final float upZ = 0.0f;

    // Set the view matrix. This matrix can be said to represent the camera position.
    // NOTE: In OpenGL 1, a ModelView matrix is used, which is a combination of a model and
    // view matrix. In OpenGL 2, we can keep track of these matrices separately if we choose.
    Matrix.setLookAtM(mVMatrix, 0, eyeX, eyeY, eyeZ, lookX, lookY, lookZ, upX, upY, upZ);

    Matrix.setIdentityM(mVMatrix, 0);

    //XXX: reload shader?
  }

  @Override
  public void onSurfaceChanged(GL10 gl, int width, int height) {
    log.info("Output Surface changed, resize");
    GLES20.glViewport(0, 0, width, height);

//    frameBufferA.init(width, height);
//    frameBufferB.init(width, height);

    iResolution[0] = width;
    iResolution[1] = height;

    openCamera(width, height);

    //TODO: request render


  }

  private void openCamera(int width, int height) {
    cameraTexture.allocate(width, height);
    SurfaceTexture old = surfaceTexture;
    log.info("Spawning Surface Texture, OpenGL id: {}", cameraTexture.getTextureId());
    this.surfaceTexture = new SurfaceTexture(cameraTexture.getTextureId());
    surfaceTexture.setOnFrameAvailableListener(new SurfaceTexture.OnFrameAvailableListener() {
      @Override
      public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        //update texture then request render
      }
    });

    log.info("Old: {} camera: {}", old, camera);
    if (old != null) {
      old.release();
    }

    iCamera[0] = 0;
    iCamera[1] = 0;
    if (camera != null) {
      log.info("Releasing camera");
      camera.stopPreview();
      camera.release();
      camera = null;
    }

    camera = Camera.open(0);
    try {
      camera.setPreviewTexture(surfaceTexture);
    } catch (IOException ioe) {
      log.error("Monumental failure", ioe);
    }

    Camera.Parameters param = camera.getParameters();
    List<Camera.Size> pSize = param.getSupportedPreviewSizes();
    for (Camera.Size size : pSize) {
      if (size.width > iCamera[0] || size.height > iCamera[1]) {
        iCamera[0] = size.width;
        iCamera[1] = size.height;
      }
    }

    log.debug("Cam Size: {}x{}", iCamera[0], iCamera[1]);

    param.setPreviewSize((int) iCamera[0], (int) iCamera[1]);

    //TODO: orientation

    camera.setParameters(param);
    camera.startPreview();

  }

  @Override
  public void onDrawFrame(GL10 gl) {
    computeTime();
    setupInitialFrameState();

    updateCameraTexture();

    checkGl20Error("KernelRenderer");

    Matrix.setIdentityM(mVMatrix, 0);
//    int txy = (int) ((time[3] % 10) * 10);

//    Matrix.translateM(mVMatrix, 0, txy, txy, 0);
    Matrix.multiplyMM(mMVPMatrix, 0, mProjMatrix, 0, mVMatrix, 0);

//    Matrix.rotateM(mVMatrix, 0, angleInDegrees, 0.0f, 0.0f, 1.0f);

    for (MaterialMesh materialMesh : bucket) {
      materialMesh.setUniformMatrix("u_MVPMatrix", mMVPMatrix);
      materialMesh.setUniformVec2("iResolution", iResolution);
      materialMesh.setUniformVec4("time", time);
      materialMesh.setUniformVec2("tod", tod);
//      float ctc = (System.currentTimeMillis() % 1000) / 10000f;
//      ctc -= Math.floor(ctc);
//      currentMM.setUniformFloat("iGlobalTime", ctc);
      materialMesh.render(mMVPMatrix);

    }

    if (currentMM != null) {
      currentMM.setUniformVec2("iResolution", iResolution);
      currentMM.setUniformMatrix("u_MVPMatrix", mMVPMatrix);
      currentMM.setUniformVec4("time", time);
      currentMM.setUniformVec2("tod", tod);
//      float ctc = (System.currentTimeMillis() % 1000) / 10000f;
//      ctc -= Math.floor(ctc);
//      currentMM.setUniformFloat("iGlobalTime", ctc);
      currentMM.render(mMVPMatrix);
    }

    for (Renderable renderable : renderBucket) {
      renderable.update(deltaTime);
      Matrix.multiplyMM(mMVPMatrix, 0, mProjMatrix, 0, renderable.transform(), 0);
      renderable.render(mMVPMatrix);

    }

    runner.processQueue();

    //XXX: container.glv.requestRender();
  }

  private void updateCameraTexture() {

    surfaceTexture.updateTexImage();
    //surfaceTexture.getTransformMatrix();

  }

  private void setupInitialFrameState() {

    GLES20.glClearColor(0f, 0f, 0f, 0f);
    GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
    GLES20.glEnable(GLES20.GL_CULL_FACE);
    GLES20.glDisable(GLES20.GL_DEPTH_TEST);
  }

  private void computeTime() {
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

  public Texture getCameraTexture() {
    return cameraTexture;
  }

  public void onDestroy() {
    if (camera != null) {
      camera.stopPreview();
      camera.release();
      camera = null;
    }
  }


}
