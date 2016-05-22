package com.perrotuerto.tinygl.core;

import android.opengl.GLES20;
import android.util.Log;

import java.util.LinkedList;
import java.util.List;

import static com.perrotuerto.tinygl.GLUtils.checkGl20Error;
import static com.perrotuerto.tinygl.GLUtils.isAnyInvalid;

/**
 * @author jp.lorandi@cfyar.com Date: 1/30/15 Time: 5:48 PM
 */
public class QuadMesh extends Mesh {

  private static List<QuadMesh> cache = new LinkedList<QuadMesh>();
  public static final int QUAD_STEPS = 50;

  private static int count = 0;


  public enum DrawMode {
    VertexBufferObject,
    VertexArrayObject
  }

  public DrawMode mode = DrawMode.VertexBufferObject;
//  public DrawMode mode = DrawMode.VertexArrayObject;

  public static QuadMesh get(int quads) {
    QuadMesh rval = findInCache(quads);
    if (rval == null) {
      int steps = quads / QUAD_STEPS + 1;
      rval = new QuadMesh(steps * QUAD_STEPS);
      rval.createVbo();
      count++;
      Log.d("QuadMesh", String.format("Created Meshes: %d", count));
    }
    return rval;
  }

  public static void put(QuadMesh mesh) {
    cache.add(mesh);
  }

  private static QuadMesh findInCache(int capacity) {
    QuadMesh rval = null;
    for (QuadMesh quadMesh : cache) {
      if (quadMesh.capacity >= capacity) {
        rval = quadMesh;
        break;
      }
    }
    if (rval != null) {
      cache.remove(rval);
    }

    return rval;
  }

  private int capacity;
  private int quads;

  @SuppressWarnings("PointlessArithmeticExpression")
  protected QuadMesh(int quads) {
    super(quads * 4, quads * 6);
    this.capacity = quads;
    this.iBuffer.position(0);
    for (int i = 0; i < quads; i++) {
//      this.iBuffer.position(i*6);
      this.iBuffer
          .put((short) (i * 4 + 0))
          .put((short) (i * 4 + 1))
          .put((short) (i * 4 + 2))
          .put((short) (i * 4 + 2))
          .put((short) (i * 4 + 1))
          .put((short) (i * 4 + 3));
    }
  }

  public void render() {
//    Log.d("QuadMesh", String
//        .format("Ids: %d, %d, %d, %d, %d", idData, idIndices, positionAttribute, colorAttribute,
//                textureAttribute));
    if (isAnyInvalid(idData, idIndices, positionAttribute, colorAttribute, textureAttribute)) {
      Log.d("QuadMesh", "Creating VBO JustInTime");
      createVbo();
      return;
    }

    //Log.d("Mesh", "Binding data"); checkGl20Error("Mesh");
    if (mode == DrawMode.VertexBufferObject) {
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
//      Log.d("Mesh", String.format("Drawing tris, q: %d", quads)); checkGl20Error("Mesh");
      GLES20.glDrawElements(GLES20.GL_TRIANGLES, quads * 6, GLES20.GL_UNSIGNED_SHORT, 0);
      //GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 3);

      checkGl20Error("Mesh");
      GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
      GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
    } else {
      GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
      GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);

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

      //Log.d("Mesh", "Drawing tris"); checkGl20Error("Mesh");
      GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, quads * 6);
    }
  }

  public void setQuads(int quads) {
    this.quads = quads;
  }

  public int capacity() {
    return this.capacity;
  }

  public void updateVbo(int lastSize, int size) {
    if (mode == DrawMode.VertexArrayObject)
      return;

    if (isAnyInvalid("QuadMesh/UpdateVBO",idData, idIndices)) {
      Log.i("QuadMesh", "Updating VBO");
      createVbo();
      return;
    }

    vBuffer.position(0);
    iBuffer.position(0);
//    Log.d("Mesh", String.format("Generated buffers, DataId: %d, IdxId: %d", idData, idIndices));

    GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, idData);
//    Log.d("Mesh", String.format("Buffer bound, DataId: %d", idData));

    GLES20
        .glBufferSubData(GLES20.GL_ARRAY_BUFFER, 0, vBuffer.capacity() * BYTES_PER_FLOAT, vBuffer);
//    Log.d("Mesh", String.format("Buffer sent, DataId: %d", idData));
    checkGl20Error("QuadMesh");
    GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, idIndices);
    GLES20.glBufferSubData(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0, iBuffer.capacity() * BYTES_PER_SHORT,
                           iBuffer);
    checkGl20Error("QuadMesh");
    GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
    GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);

    checkGl20Error("QuadMesh");

    Log.d("QuadMesh", String
        .format("QuadMesh[%d] updated, vBuffer size: %d iBuffer size: %d", idData,
                vBuffer.capacity(),
                iBuffer.capacity()));
  }

  public Mesh quad(int initialVertex, float x, float y,

                   float halfWidth, float halfHeight) {
    if (initialVertex > capacity * 4)
      throw new IllegalArgumentException("Can't set vertex(" + initialVertex + ") capacity: " + capacity);

    //setActiveColor(r,g,b,a);

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


    return this;
  }

  public void setActiveColor(float r, float g, float b, float a) {
    this.r = r;
    this.g = g;
    this.b = b;
    this.a = a;
  }


}
