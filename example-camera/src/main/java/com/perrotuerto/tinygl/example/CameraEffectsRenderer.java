package com.perrotuerto.tinygl.example;

import android.app.Activity;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.view.MotionEvent;

import com.perrotuerto.tinygl.*;
import com.perrotuerto.tinygl.core.ShaderProgram;
import com.perrotuerto.tinygl.core.Texture;
import com.perrotuerto.tinygl.core.TextureManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static com.perrotuerto.tinygl.GLUtils.checkGl20Error;

/**
 * @author jp.lorandi@cfyar.com Date: 5/24/16 Time: 9:57 AM
 */
public class CameraEffectsRenderer extends AbstractRenderer {

  private static final Logger log =
      LoggerFactory.getLogger(CameraEffectsRenderer.class);

  private final Activity parent;

  private Camera camera;
  private SurfaceTexture surfaceTexture;
  private Texture cameraTexture;


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

    Matrix.orthoM(mProjMatrix, 0,
                  0, width, height, 0, -1, 1);

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

    for (int i = 0; i < Camera.getNumberOfCameras(); i++) {
      Camera.CameraInfo info = new Camera.CameraInfo();
      Camera.getCameraInfo(i, info);
      if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
        camera = Camera.open(i);
        break;
      }
    }
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
//      log.debug("rendering MM");
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
