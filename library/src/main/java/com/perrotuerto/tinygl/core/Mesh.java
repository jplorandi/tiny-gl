package com.perrotuerto.tinygl.core;

import android.opengl.GLES20;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import static com.perrotuerto.tinygl.GLUtils.checkGl20Error;
import static com.perrotuerto.tinygl.GLUtils.isAnyInvalid;

/**
 * @author jp.lorandi@cfyar.com Date: 1/30/15 Time: 5:32 PM
 */
public class Mesh {

  protected static final int POS_SIZE_T = 3;
  protected static final int TEX_SIZE_T = 2;
  protected static final int COL_SIZE_T = 4;

  protected static final int POS_OFFSET = 0;
  protected static final int TEX_OFFSET = POS_OFFSET + POS_SIZE_T;
  protected static final int COL_OFFSET = TEX_OFFSET + TEX_SIZE_T;

  public static final int STRIDE = POS_SIZE_T + TEX_SIZE_T + COL_SIZE_T;
  protected static final int BYTES_PER_FLOAT = 4;
  protected static final int BYTES_PER_SHORT = 2;

  public FloatBuffer vBuffer; //vbo
  public ShortBuffer iBuffer; //vbo

//  public FloatBuffer posBuffer; //arrays
//  public FloatBuffer texBuffer; //arrays

//  private float[] pos;
//  private float[] tex;

  public float[] transform = new float[16];

  protected float r = 1f;
  protected float g = 1f;
  protected float b = 1f;
  protected float a = 1f;


  int idData = -1; //for vbo, not used for varray
  int idIndices = -1; //for vbo, not used for varray
  int idTexture = -1;

  int positionAttribute = -1;
  int textureAttribute = -1;
  int colorAttribute = -1;

  public Mesh(int vertexCount, int indexCount) {

    initBuffers(vertexCount, indexCount);

  }

  private void initBuffers(int vertexCount, int indexCount) {
//    iBuffer = ShortBuffer.wrap(new short[indexCount]);
//    vBuffer = FloatBuffer.wrap(new float[vertexCount * (STRIDE)]);
    iBuffer =
        ByteBuffer.allocateDirect(indexCount * 2).order(ByteOrder.nativeOrder()).asShortBuffer();
    vBuffer =
        ByteBuffer.allocateDirect(vertexCount * STRIDE * 4).order(ByteOrder.nativeOrder())
            .asFloatBuffer();

//    Log.d("Mesh", "VBO interleaved buffers initialized");

//    pos = new float[vertexCount * 3];
//    tex = new float[vertexCount * 2];
//
//    posBuffer = FloatBuffer.wrap(pos);
//    posBuffer.position(pos.length - 1);
//    posBuffer.flip();
//
//    texBuffer = FloatBuffer.wrap(tex);
//    texBuffer.position(tex.length - 1);
//    texBuffer.flip();
//
//    Log.d("Mesh", "VAO non-interleaved buffers initialized");
  }

  public Mesh setVertex(
      int pos,
      float x, float y, float z,
      float u, float v,
      float r, float g, float b, float a) {

    vBuffer.position(pos * STRIDE);
    vBuffer.put(x).put(y).put(z);
    vBuffer.put(u).put(v);
    vBuffer.put(r).put(g).put(b).put(a);

//    posBuffer.position(pos * 3);
//    posBuffer.put(x).put(y).put(z);
//    texBuffer.position(pos * 2);
//    texBuffer.put(u).put(v);

    return this;
  }



  public void setTexture(Texture t) {
    this.idTexture = t.getTextureId();
  }

  public Mesh setVertex(
      int pos,
      float x, float y, float z,
      float u, float v) {
    return setVertex(pos,
                     x, y, z,
                     u, v,
                     r,g,b,a);
  }

  public Mesh setIndex(int pos, int... indices) {
//    Log.d("Mesh",
//          String.format("Setting indices, pos: %d, indices.length: %d iBuffer.capacity(): %d",
//                                pos, indices.length, iBuffer.capacity()) );
    iBuffer.position(pos);
    for (int i = 0; i < indices.length; i++) {
      short index = (short) indices[i];
      iBuffer.put(index);
    }

    return this;
  }

  final int[] tmp = new int[10];

  public void createVbo() {
    //TODO: delete vbo?
    if (!isAnyInvalid("Mesh/CreateVBO", idData, idIndices)) {
      Log.i("Mesh", "You need to delete the VBO before you create it!");
      return;
    }


    GLES20.glGenBuffers(2, tmp, 0);

//    loadVertexProgram(tmp[0], "some string");

//    checkProgramCompleteness();

    idData = tmp[0];
    idIndices = tmp[1];

//    dumpBuffers();

//    Log.d("Mesh", String.format("Generated buffers, DataId: %d, IdxId: %d", idData, idIndices));

    vBuffer.position(0);
    iBuffer.position(0);
//    Log.d("Mesh", String.format("Generated buffers, DataId: %d, IdxId: %d", idData, idIndices));

    GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, idData);
//    Log.d("Mesh", String.format("Buffer bound, DataId: %d", idData));

    GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, vBuffer.capacity() * BYTES_PER_FLOAT, vBuffer,
                        GLES20.GL_DYNAMIC_DRAW);
//    Log.d("Mesh", String.format("Buffer sent, DataId: %d", idData));
    checkGl20Error("Mesh");
    GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, idIndices);
    GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER, iBuffer.capacity() * BYTES_PER_SHORT,
                        iBuffer,
                        GLES20.GL_DYNAMIC_DRAW);
    checkGl20Error("Mesh");
    GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
    GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);

    checkGl20Error("Mesh");

    Log.d("Mesh", String
        .format("Mesh created, vBuffer size: %d iBuffer size: %d", vBuffer.capacity(),
                iBuffer.capacity()));
  }

  private void dumpBuffers() {
    for (int i = 0; i < iBuffer.capacity(); i++) {
      Log.d("Mesh", String.format("IB[%d]=%d", i, iBuffer.get(i)));
    }
  }

  public void render() {
    if (idData == -1) {
      createVbo();
    }

    if (isAnyInvalid(idData, idIndices, positionAttribute, colorAttribute, textureAttribute)) {
      throw new IllegalStateException("Invalid handle");
    }

    //Log.d("Mesh", "Binding data"); checkGl20Error("Mesh");
    GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, idData);
    //Log.d("Mesh", "Setting Attrib pos"); checkGl20Error("Mesh");
    GLES20.glVertexAttribPointer(positionAttribute, POS_SIZE_T, GLES20.GL_FLOAT,
                                 false, STRIDE * BYTES_PER_FLOAT, POS_OFFSET * BYTES_PER_FLOAT);
    GLES20.glEnableVertexAttribArray(positionAttribute);
    //Log.d("Mesh", "Setting Attrib tex"); checkGl20Error("Mesh");
    GLES20.glVertexAttribPointer(textureAttribute, TEX_SIZE_T, GLES20.GL_FLOAT,
                                 true, STRIDE * BYTES_PER_FLOAT, TEX_OFFSET * BYTES_PER_FLOAT);
    GLES20.glEnableVertexAttribArray(textureAttribute);
    //Log.d("Mesh", "Setting Attrib col"); checkGl20Error("Mesh");
    GLES20.glVertexAttribPointer(colorAttribute, COL_SIZE_T, GLES20.GL_FLOAT,
                                 true, STRIDE * BYTES_PER_FLOAT, COL_OFFSET * BYTES_PER_FLOAT);
    GLES20.glEnableVertexAttribArray(colorAttribute);

    //Log.d("Mesh", "Binding indexes"); checkGl20Error("Mesh");
    GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, idIndices);

    //Log.d("Mesh", "Drawing tris"); checkGl20Error("Mesh");
    GLES20.glDrawElements(GLES20.GL_TRIANGLES, iBuffer.capacity(), GLES20.GL_UNSIGNED_SHORT, 0);

    //GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 3);

    checkGl20Error("Mesh");
    GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
    GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
  }

  public void renderArray() {
//    if (idData == -1)
//      createVbo();

    vBuffer.position(POS_OFFSET);
    GLES20.glVertexAttribPointer(positionAttribute, POS_SIZE_T, GLES20.GL_FLOAT, false,
                                 STRIDE * BYTES_PER_FLOAT, vBuffer);

    GLES20.glEnableVertexAttribArray(positionAttribute);

    // Pass in the color information
    vBuffer.position(COL_OFFSET);
    GLES20.glVertexAttribPointer(colorAttribute, COL_SIZE_T, GLES20.GL_FLOAT, false,
                                 STRIDE * BYTES_PER_FLOAT, vBuffer);

    GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 3);
  }

  public Mesh quad(int initialVertex, float x, float y, float halfWidth, float halfHeight) {
    this.setVertex(
        initialVertex + 0,
        x - halfWidth,
        y - halfHeight,
        0,
        0f,
        0f
    ).setVertex(
        initialVertex + 1,
        x - halfWidth,
        y + halfHeight,
        0,
        0f,
        1f
    ).setVertex(
        initialVertex + 2,
        x + halfWidth,
        y - halfHeight,
        0,
        1f,
        0f
    ).setVertex(
        initialVertex + 3,
        x + halfWidth,
        y + halfHeight,
        0,
        1f,
        1f
    );

    setIndex(initialVertex / 4 * 6,
             initialVertex * 4 + 0, initialVertex * 4 + 1, initialVertex * 4 + 2,
             initialVertex * 4 + 2, initialVertex * 4 + 1, initialVertex * 4 + 3
    );

    return this;
  }

  public static Mesh createQuad(float x, float y, float halfWidth, float halfHeight) {
    Mesh rval = new Mesh(4, 6);

    rval.quad(0, x, y, halfWidth, halfHeight);

    return rval;
  }

  public void destroy() {

    tmp[0] = idData;
    tmp[1] = idIndices;
    GLES20.glDeleteBuffers(2, tmp, 0);

//    Log.d("Mesh", "Faux Destroyed VBOs");
  }

  public void setAttributeIds(int positionAttribute, int textureAttribute, int colorAttribute) {
    this.positionAttribute = positionAttribute;
    this.textureAttribute = textureAttribute;
    this.colorAttribute = colorAttribute;
  }
}
