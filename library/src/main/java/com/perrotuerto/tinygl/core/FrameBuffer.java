package com.perrotuerto.tinygl.core;

import android.opengl.GLES20;
import android.util.Log;

/**
 * @author jp.lorandi@cfyar.com Date: 1/30/15 Time: 10:50 PM
 */
public class FrameBuffer {

  private int fbo = -1;
  private Texture fboTexture = new Texture(new TextureKey(false));
  private boolean fboEnabled = true;
  private int width;
  private int height;

  private final int[] tmpBuffer = new int[1];

  public boolean init(int width, int height) {
    return reinit(width, height, true);
  }

  public int getTextureId() { return fboTexture.getTextureId(); }

  public boolean reinit(int width, int height,
                        boolean destroyBackingTexture) {
    this.width = width;
    this.height = height;
    //fboEnabled = checkIfContextSupportsFrameBufferObject(gl);
    if (fboEnabled) {
      if (fbo != -1) {
        //GLES20 GLES20 = (GLES20) gl;
        tmpBuffer[0] = fbo;
        GLES20.glDeleteFramebuffers(1, tmpBuffer, 0);
      }

      if (fboTexture.getTextureId() != -1 && destroyBackingTexture) {
        tmpBuffer[0] = fboTexture.getTextureId();
        GLES20.glDeleteTextures(tmpBuffer.length, tmpBuffer, 0);
      }

      fboTexture.allocate(width, height);
      fbo = createFrameBuffer(width, height, fboTexture.getTextureId());
      Log.i("FBO", "FBO supported!!!");
      Log.i("FBO",
            String.format("TextureId: %d FBO: %d", fboTexture.getTextureId(), fbo));
    } else {
      Log.e("FBO", "FBO not supported!!!");
    }
    return fboEnabled;
  }

  public void bind() {
    if (fboEnabled) {
      GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fbo);
    }
  }

  public void unbind() {
    if (fboEnabled) {
      GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
    }
  }

  public int createFrameBuffer(int width, int height,
                               int targetTextureId) {

    int framebuffer;
    int[] framebuffers = new int[1];
    GLES20.glGenFramebuffers(1, framebuffers, 0);
    framebuffer = framebuffers[0];
    GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER,
                             framebuffer);

    int depthbuffer;
    int[] renderbuffers = new int[1];
    GLES20.glGenRenderbuffers(1, renderbuffers, 0);
    depthbuffer = renderbuffers[0];

    GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER,
                              depthbuffer);
    GLES20.glRenderbufferStorage(GLES20.GL_RENDERBUFFER,
                                 GLES20.GL_DEPTH_COMPONENT16, width, height);
    GLES20.glFramebufferRenderbuffer(
        GLES20.GL_FRAMEBUFFER,
        GLES20.GL_DEPTH_ATTACHMENT,
        GLES20.GL_RENDERBUFFER, depthbuffer);

    GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER,
                                  GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D,
                                  targetTextureId, 0);
    int status = GLES20
        .glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER);
    if (status != GLES20.GL_FRAMEBUFFER_COMPLETE) {
      throw new RuntimeException("Framebuffer is not complete: "
                                 + Integer.toHexString(status));
    }
    GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
    return framebuffer;
  }

  public Texture getTexture() {
    return fboTexture;
  }


}
