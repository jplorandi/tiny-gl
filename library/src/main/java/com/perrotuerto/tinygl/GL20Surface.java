package com.perrotuerto.tinygl;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import com.perrotuerto.tinygl.core.ShaderProgram;
import com.perrotuerto.tinygl.core.Texture;
import com.perrotuerto.tinygl.core.TextureManager;

import java.io.IOException;
import java.util.concurrent.Callable;

import static com.perrotuerto.tinygl.GLUtils.next2Pow;
import static com.perrotuerto.tinygl.GLUtils.textAsBitmap;

/**
 * @author jp.lorandi@cfyar.com Date: 1/30/15 Time: 10:52 PM
 */
public class GL20Surface extends GLSurfaceView {

  public static AssetManager assetManager;
  private CeeSuiteRenderer dr;
  private ImageView imageView;


  public GL20Surface(Context context) {
    super(context);
    init(context);
  }

  public GL20Surface(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(context);
  }

  private void init(Context context) {
    this.assetManager = context.getAssets();
    if (GLUtils.karbon == null && !isInEditMode()) {
      GLUtils.karbon = Typeface.createFromAsset(assetManager, "fonts/Karbon-Light.otf");
    }

    try {
      if (!isInEditMode()) {
//        super.setZOrderOnTop(true);

        this.setEGLContextClientVersion(2);
        this.setPreserveEGLContextOnPause(true);
        this.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        this.getHolder().setFormat(PixelFormat.TRANSLUCENT);
        this.setVisibility(View.VISIBLE);
      } else {
        //this.setVisibility(View.INVISIBLE);
      }
//
      Log.i("GL20Surface", "Initialized OK");
    } catch (Exception e) {
      e.printStackTrace();
      Log.e("GL20Surface", "While initializing");
    }

  }

  @Override
  public boolean onTouchEvent(final MotionEvent event) {
    //return super.onTouchEvent(event);
    switch (event.getActionMasked()) {
      case MotionEvent.ACTION_DOWN:
        Log.i("DoodleSurfaceView", "drawing started");
        dr.actionDown(event);
        return true;
      case MotionEvent.ACTION_UP:
        Log.i("DoodleSurfaceView", "drawing done");
        dr.actionUp(event);
//        dr.runOnGlThread(new Callable() {
//          @Override
//          public Object call() throws Exception {
//
//            createKernelLeaf(event);
//
//            return null;
//
//          }
//        });
        return true;
      case MotionEvent.ACTION_MOVE:
        Log.i("touch move", "drawing happening");
        dr.actionMove(event);
        return true;
    }
    return false;
  }

  private void createKernelLeaf(MotionEvent event) throws IOException {
    final Bitmap text = textAsBitmap("Tpqg", 72, Color.argb(192, 255, 255, 255));
    Texture texture = TextureManager.get(text, true);
//    if (imageView != null) {
//      imageView.post(new Runnable() {
//        @Override
//        public void run() {
//          imageView.setImageBitmap(text);
//
//        }
//      });
//    }
    text.recycle();

//            Texture texture = TextureManager.get("textures/smooth_tool.png", true);

    Log.i("Initial", String.format("TextureId: %s", texture.getTextureId()));

    ShaderProgram program = ShaderProgram.get(
        "shaders/simple-vert.glsl",
        "shaders/circle-text-frag.glsl",
        new String[]{"a_pos", "a_tex", "a_col"},
        new String[]{"u_MVPMatrix", "time","tod", "iResolution", "toolTexture", "framebuffer"}
    );

    dr.addMM(event.getX(), event.getY(), text.getWidth()/2, text.getHeight()/2, texture, program, 1f, 1f, 1f, 1f);
//        overlayRenderer.addMM(-100f, -100f, 150f, 150f, texture, program, 0f, 0f, 1f, 1f);
  }

  public void setImageView(ImageView imageView) {
    this.imageView = imageView;
  }

  @Override
  public void setRenderer(GLSurfaceView.Renderer renderer) {
    super.setRenderer(renderer);
    this.dr = (CeeSuiteRenderer) renderer;
  }

/*
  @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
  public void onDestroy(){
    updateTexture = false;
    mSurfaceTexture.release();
    if(mCamera != null){
      mCamera.stopPreview();
      mCamera.setPreviewCallback(null);
      mCamera.release();
    }

    mCamera = null;
  }
*/
}
