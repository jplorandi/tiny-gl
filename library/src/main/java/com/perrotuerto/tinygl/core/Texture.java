package com.perrotuerto.tinygl.core;

import android.graphics.Bitmap;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.util.Log;

import com.perrotuerto.tinygl.GL20Surface;
import com.perrotuerto.tinygl.PNGDecoder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;
import java.util.concurrent.Callable;


import static com.perrotuerto.tinygl.GLUtils.checkGl20Error;
import static com.perrotuerto.tinygl.GLUtils.next2Pow;

/**
 * @author jp.lorandi@cfyar.com Date: 1/30/15 Time: 5:32 PM
 */
public class Texture {

  private static final Logger log =
      LoggerFactory.getLogger(Texture.class);

  public int id = -1;
  public TextureKey key;

  public boolean flip = true;
  public boolean saved = false;
  public boolean saving = false;
  public int width = -1;
  public int height = -1;



  private int[] tmp = new int[1];

  Texture(TextureKey key) {
    this.key = key;

  }

  private Texture(int id, int width, int height) {
    this.id = id;
    this.width = width;
    this.height = height;
    this.key = new TextureKey();
  }

  public void allocate(int width, int height) {
    if (!key.external)
      allocateInternal(width, height);
    else
      allocateExternal(width, height);
  }

  private void allocateExternal(int width, int height) {
    this.width = width;
    this.height = height;

    if (id == -1) {
      GLES20.glGenTextures(1, tmp, 0);
      setTextureId(tmp[0]);
    }

    GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, id);
//    final byte[] content = new byte[width * height * 4];
//    Arrays.fill(content, (byte) 0xFF);
//    final ByteBuffer wrapper = ByteBuffer.wrap(content);
//
//    GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, width, height, 0,
//                        GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, wrapper);

//    GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,
//                           GLES20.GL_NEAREST_MIPMAP_NEAREST);
//    GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER,
//                           GLES20.GL_LINEAR);
//    GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,
//                           GLES20.GL_REPEAT);
//    GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,
//                           GLES20.GL_REPEAT);

    GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
    GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
    GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
    GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
  }

  private void allocateInternal(int width, int height) {
    this.width = width;
    this.height = height;

    GLES20.glGenTextures(1, tmp, 0);
    setTextureId(tmp[0]);

    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, id);
    final byte[] content = new byte[width * height * 4];
    Arrays.fill(content, (byte) 0xFF);
    final ByteBuffer wrapper = ByteBuffer.wrap(content);

    GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, width, height, 0,
                        GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, wrapper);
    GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,
                           GLES20.GL_NEAREST_MIPMAP_NEAREST);
    GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER,
                           GLES20.GL_LINEAR);
    GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,
                           GLES20.GL_REPEAT);
    GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,
                           GLES20.GL_REPEAT);
  }

  public void checkValid() {
    if (!GLES20.glIsTexture(getTextureId())) {
      Log.w("Texture", "Reloading texture");
      tmp[0] = getTextureId();
      GLES20.glDeleteTextures(1, tmp, 0);
      //setTextureId(-1);
      load();
    }
  }

  public void load() {
    if (key.external) {
      log.warn("Cannot load an external texture");
      return;
    }

    while (saving) {
      try {
        Thread.sleep(50);
      } catch (InterruptedException ignored) {
      }
    }

    if (getTextureId() == -1) {
      GLES20.glGenTextures(1, tmp, 0);
      setTextureId(tmp[0]);
    }

    InputStream input = null;
    try {

      File file = new File(key.diskName);
      if (!file.exists()) {
        input = GL20Surface.assetManager.open(key.diskName);
      } else
        input = new FileInputStream(file);

      PNGDecoder decoder = new PNGDecoder(input);
      byte[] bytes = new byte[4 * decoder.getWidth()
                              * decoder.getHeight()];
      ByteBuffer buf = ByteBuffer.wrap(bytes);
      buf.position(0);

      boolean twopot =
          next2Pow(decoder.getWidth()) == decoder.getWidth() &&
          decoder.getWidth() == decoder.getHeight();

      Log.d("Texture",
            String.format(
                "Loading texture: '%s' size: %d x %d RGB: %b alpha: %b channel: %b format: %s twopot: %b",
                key.diskName, decoder.getWidth(), decoder.getHeight(),
                decoder.isRGB(), decoder.hasAlpha(),
                decoder.hasAlphaChannel(),
                decoder.decideTextureFormat(PNGDecoder.Format.RGBA),
                twopot)
      );


      int minFilter = twopot ? GLES20.GL_NEAREST : GLES20.GL_NEAREST;

      decoder.decode(buf, decoder.getWidth() * 4, PNGDecoder.Format.RGBA);
      buf.position(0);

      GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, getTextureId());
      checkGl20Error("Texture Bind Texture");
      GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA,
                          decoder.getWidth(), decoder.getHeight(), 0, GLES20.GL_RGBA,
                          GLES20.GL_UNSIGNED_BYTE, buf);
      checkGl20Error("Texture TexImage");

      GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,
                             minFilter);
      checkGl20Error("Texture Parameters MIN");
      GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER,
                             GLES20.GL_NEAREST);
      checkGl20Error("Texture Parameters MAX");
      GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,
                             GLES20.GL_REPEAT);
      GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,
                             GLES20.GL_REPEAT);
      checkGl20Error("Texture Parameters");

      Log.d("Texture",
            String.format("Texture loaded from disk: '%s'", key.diskName));

    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      if (input != null) {
        try {
          input.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }

  public int getTextureId() {
    return id;
  }

  public void setTextureId(int id) {
    this.id = id;
  }

  /*
  public static int[] savePixels(int x, int y, int w, int h, GL10 gl) {
    int b[] = new int[w * h];
    IntBuffer ib = IntBuffer.wrap(b);
    ib.position(0);
    gl.glReadPixels(x, y, w, h, GL10.GL_RGBA, GL10.GL_UNSIGNED_BYTE, ib);

    return b;
  }
    */
  private static Bitmap rearrangeColors(int w, int h, int[] b) {
		/*
		 * remember, that OpenGL bitmap is incompatible with Android bitmap and
		 * so, some correction need.
		 */
    int bt[] = new int[w * h];
    for (int i = 0; i < h; i++) {
      for (int j = 0; j < w; j++) {
        int pix = b[i * w + j];
        int pb = (pix >> 16) & 0xff;
        int pr = (pix << 16) & 0x00ff0000;
        int pix1 = (pix & 0xff00ff00) | pr | pb;
        bt[(h - i - 1) * w + j] = pix1;
      }
    }
    Bitmap sb = Bitmap.createBitmap(bt, w, h, Bitmap.Config.ARGB_8888);
    return sb;
  }

  public static void savePNG(File filePath, Bitmap bmp) {
    if (bmp == null) {
      Log.e("Texture", "Bitmap null on call to savePNG");
      return;
    }

    Log.d("Texture",
          String.format("Saving bitmap to: '%s'",
                        filePath.getAbsolutePath()));
    try {
      FileOutputStream fos = new FileOutputStream(filePath);
      bmp.compress(Bitmap.CompressFormat.PNG, 100, fos);
      try {
        fos.flush();
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      try {
        fos.close();
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    } catch (FileNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  public File createTempPngFile(String name) {
    try {
      File rval = File.createTempFile(name, ".png", TextureManager.getCacheDir());
      rval.deleteOnExit();  //TODO: this doesn't work in Android the same as on PCs
      return rval;
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }

  /*
  public Texture save(String filePath, GL10 gl) {
    Texture rval = new Texture(-1, width, height);
    rval.saving = true;
    rval.flip = false;
    int[] bmp = savePixels(0, 0, width, height, gl);
    // Bitmap bmp = savePixels(0, 0, width, height, gl);
    // rval.load(gl, bmp);
    TextureManager.service.submit(new SaveAsPng(bmp, filePath, rval));

    return rval;
  }
    */


  private class SaveAsPng implements Callable<String> {

    private final Texture fboTex;
    private final String filePath;
    private int[] bitmap;

    private SaveAsPng(int[] bitmap, String filePath,
                      Texture fboBackingTexture) {
      Log.d("FboBackingTexture", "Starting async work");
      this.bitmap = bitmap;
      this.fboTex = fboBackingTexture;
      this.filePath = filePath;
      this.fboTex.saving = true;
    }

    @Override
    public String call() throws Exception {
      File rval = createTempPngFile(filePath);
      Bitmap bm = rearrangeColors(fboTex.width, fboTex.height, bitmap);
      fboTex.savePNG(rval, bm);
      bm.recycle();
      this.bitmap = null;
      fboTex.key.diskName = rval.getAbsolutePath();
      fboTex.saved = true;
      fboTex.saving = false;
      return rval.getAbsolutePath();
    }
  }

}
