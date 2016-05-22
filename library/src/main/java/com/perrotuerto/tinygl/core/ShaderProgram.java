package com.perrotuerto.tinygl.core;

import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.util.Log;

import com.perrotuerto.tinygl.GL20Surface;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

import javax.microedition.khronos.opengles.GL11Ext;

import static com.perrotuerto.tinygl.GLUtils.checkGl20Error;

/**
 * @author jp.lorandi@cfyar.com Date: 1/30/15 Time: 10:47 PM
 */
public class ShaderProgram {
  private static final Logger log =
      LoggerFactory.getLogger(ShaderProgram.class);

  private int[] tmp = new int[10];

  public static final String remoteFeeder = "http://192.168.56.1/shader";

  private String pathToVertexProgram;
  private String pathToFragmentProgram;
  private int vertexId = -1;
  private int fragId = -1;
  private int progId = -1;

  private Map<String, Integer> attributes = new HashMap<String, Integer>();
  private Map<String, Integer> uniforms = new HashMap<String, Integer>();

  private static Map<ShaderProgramKey, ShaderProgram> cache = new HashMap<ShaderProgramKey, ShaderProgram>();

  private WeakReference<ShaderProgramKey> myKey;

  public static ShaderProgram get(String pathToVertexProgram,
                                  String pathToFragmentProgram,
                                  String [] attributes, String [] uniforms) {

    ShaderProgramKey key = getKey(pathToVertexProgram,
                                  pathToFragmentProgram,
                                  attributes,
                                  uniforms);

    ShaderProgram rval = cache.get(key);
    if (rval == null) {
      rval = new ShaderProgram(pathToVertexProgram, pathToFragmentProgram, attributes, uniforms, key);
      cache.put(key, rval);
    }

    return rval;
  }

  private static ShaderProgramKey getKey(String pathToVertexProgram, String pathToFragmentProgram,
                                         String[] attributes, String[] uniforms) {
    ShaderProgramKey rval = new ShaderProgramKey();
    rval.vertexPath = pathToVertexProgram;
    rval.fragmentPath = pathToFragmentProgram;
    rval.attributes = attributes;
    rval.uniforms = uniforms;

    return rval;
  }

  private ShaderProgramKey getKey() {
    ShaderProgramKey rval = new ShaderProgramKey();
    rval.vertexPath = pathToVertexProgram;
    rval.fragmentPath = pathToFragmentProgram;
    rval.attributes = attributes.keySet().toArray(new String[attributes.keySet().size()]);
    rval.uniforms = uniforms.keySet().toArray(new String[uniforms.keySet().size()]);

    return rval;
  }

  private ShaderProgram(String pathToVertexProgram, String pathToFragmentProgram,
                        String[] attributes, String[] uniforms, ShaderProgramKey key) {

    this.pathToVertexProgram = pathToVertexProgram;
    this.pathToFragmentProgram = pathToFragmentProgram;
    try {
      loadCompileLink(pathToVertexProgram, pathToFragmentProgram, attributes, uniforms);

      myKey = new WeakReference<>(key);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void loadCompileLink(ShaderProgramKey key) throws IOException {
    loadCompileLink(key.vertexPath, key.fragmentPath, key.attributes, key.uniforms);
  }

  private void loadCompileLink(String pathToVertexProgram, String pathToFragmentProgram,
                               String[] attributes, String[] uniforms) throws IOException {
    loadProgram(
        loadFile(pathToVertexProgram),
        loadFile(pathToFragmentProgram), attributes);

    for (String attribute : attributes) {
      this.attributes.put(attribute, getAttribute(attribute));
    }

    for (String uniform : uniforms) {
      this.uniforms.put(uniform, getUniform(uniform));
    }
  }

  private String loadFile(String file) throws IOException {
    StringBuilder out = new StringBuilder();
    InputStream in = GL20Surface.assetManager.open(file);
    InputStreamReader inputStreamReader = new InputStreamReader(in);
    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

    String buf = null;
    while ((buf = bufferedReader.readLine()) != null) {
      out.append(buf);
      out.append("\r\n");
    }

    String outVal = out.toString();
//    Log.i("ShaderProgram", String.format("LoadFile: %s", outVal));
    return outVal;
  }

  protected int loadShader(String strSource, int iType) {
    int[] compiled = new int[1];
    int iShader = GLES20.glCreateShader(iType);
    GLES20.glShaderSource(iShader, strSource);
    GLES20.glCompileShader(iShader);
    GLES20.glGetShaderiv(iShader, GLES20.GL_COMPILE_STATUS, compiled, 0);
    if (compiled[0] == 0) {
      log.warn("Compilation {}", GLES20.glGetShaderInfoLog(iShader));
      log.warn("Compilation Error {}", GLES20.glGetError());
      return 0;
    }
    return iShader;
  }

  protected int loadProgram(String pathToVertexProgram, String pathToFragmentProgram,
                            String[] attributes) {

    vertexId = loadShader(pathToVertexProgram, GLES20.GL_VERTEX_SHADER);
    if (vertexId <= 0) {
      log.error("Vertex Shader Failed");
      return 0;
    }
    fragId = loadShader(pathToFragmentProgram, GLES20.GL_FRAGMENT_SHADER);
    if (fragId <= 0) {
      log.error("Fragment Shader Failed");
      return 0;
    }

    progId = GLES20.glCreateProgram();

    GLES20.glAttachShader(progId, vertexId);
    GLES20.glAttachShader(progId, fragId);

    for (int i = 0; i < attributes.length; i++) {
      String attribute = attributes[i];
      GLES20.glBindAttribLocation(progId, i, attribute);
    }

    GLES20.glLinkProgram(progId);

    GLES20.glGetProgramiv(progId, GLES20.GL_LINK_STATUS, tmp, 0);
    if (tmp[0] <= 0) {
      log.error("Linking Failed.");
      return 0;
    } else {
      log.info("Linking Succeeded: {}, for ProgId: {}", tmp[0], progId);
    }

//    GLES20.glDetachShader(progId, vertexId);
//    GLES20.glDetachShader(progId, fragId);

//    log.debug(, String.format("Shader Attribute a_pos: %d", getAttribute("a_pos")));

//    GLES20.glDeleteShader(vertexId);
//    GLES20.glDeleteShader(fragId);
    return progId;
  }

  public void checkValid() {
    if (!isValid()) {
      if (myKey == null || myKey.get() == null)
        myKey = new WeakReference<>(getKey());

      String s = myKey != null ? myKey.get().toString() : "[NullKey]";

      log.warn("ShaderProgram invalid: id: {} key: {}", progId, s);

      deleteAndRelink();
    }
  }

  private void deleteAndRelink()  {
    if (myKey == null) {
      myKey = new WeakReference<ShaderProgramKey>(getKey());
    }

    try {
      GLES20.glDeleteProgram(progId);
      checkGl20Error("after deleting program");
      progId = -1;
      loadCompileLink(myKey.get());
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public boolean isValid() {
    return isValid(progId);
  }

  public boolean isValid(int progId) {
//    GLES20.glGetProgramiv(progId, GLES20.GL_VALIDATE_STATUS, tmp, 0);
//    return tmp[0] == GLES20.GL_TRUE;

    return GLES20.glIsProgram(progId);

  }

  public void bind(Mesh stroke) {
    checkGl20Error("ShaderProgram bind 0");
    GLES20.glUseProgram(progId);

    checkGl20Error("ShaderProgram bind 1");
    stroke.setAttributeIds(
        attribute("a_pos"),
        attribute("a_tex"),
        attribute("a_col")
    );
    checkGl20Error("ShaderProgram bind 2");


  }

  public int attribute(String name) {
    return attributes.get(name) == null ? -1 : attributes.get(name);
  }
  public int uniform(String name) {
    return uniforms.get(name) == null ? -1 : uniforms.get(name);
  }

  private int getAttribute(String attributeName) {
    int rval =
        GLES20.glGetAttribLocation(progId, attributeName);

    log.debug("Bound attribute for '{}' id: {}", attributeName, rval);

    return rval;
  }

  private int getUniform(String attributeName) {
    int rval =
        GLES20.glGetUniformLocation(progId, attributeName);

    log.debug("Bound uniform for '{}' id: {}", attributeName, rval);

    return rval;
  }

  public void unbind() {
    GLES20.glUseProgram(0);
  }

  public void setUniformM4(String name, float[] value) {
    int id = uniform(name);
    checkValid();
    GLES20.glUseProgram(progId);
    checkGl20Error("Post Use Program for uM4");
    GLES20.glUniformMatrix4fv(id, 1, false, value, 0);
    //GLES20.glUseProgram(0);
  }

  public void setUniformV4(String name, float[] value) {
    int id = uniform(name);
    GLES20.glUseProgram(progId);
    GLES20.glUniform4fv(id, 1, value, 0);
  }

  public void setUniformV2(String name, float[] value) {
    int id = uniform(name);
    GLES20.glUseProgram(progId);
    GLES20.glUniform2fv(id, 1, value, 0);
  }

  public void setUniformV(String name, float value) {
    int id = uniform(name);
    GLES20.glUseProgram(progId);
    GLES20.glUniform1f(id, value);
  }

  public void setUniformI2(String name, int[] value) {
    int id = uniform(name);
    GLES20.glUseProgram(progId);
    GLES20.glUniform2i(id, value[0], value[1]);
  }

  public void setUniformTexture(String name, Texture texture, int unit) {
    int id = uniform(name);
//    log.info("uniform id: {} for texture id: {}", id, texture.getTextureId());
    GLES20.glActiveTexture(GLES20.GL_TEXTURE0 + unit);
    checkGl20Error("After setting active texture");

    // Bind the texture to this unit.
    if (texture.key.external)
      GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, texture.getTextureId());
    else
      GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture.getTextureId());
    checkGl20Error("After binding active texture");

    // Tell the texture uniform sampler to use this texture in the shader by binding to texture unit 0.
    GLES20.glUniform1i(id, unit);
    checkGl20Error("After setting shader uniform for texture");

//    Log.i("ShaderProgram", String
//        .format("Bound uniform id[%d] texture with id: %d to unit: %d under name: '%s'", id,
//                texture.getTextureId(), unit, name));
  }

  public void setUniformF(String name, float value) {
    int id = uniform(name);
    GLES20.glUseProgram(progId);
    GLES20.glUniform1f(id, value);
  }


}
