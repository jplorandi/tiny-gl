package com.perrotuerto.tinygl.core;

import java.util.Arrays;

/**
 * @author jp.lorandi@cfyar.com Date: 1/30/15 Time: 10:47 PM
 */
public class ShaderProgramKey {
  public String vertexPath;
  public String fragmentPath;
  public String[] uniforms;
  public String[] attributes;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    ShaderProgramKey that = (ShaderProgramKey) o;

    if (!Arrays.equals(attributes, that.attributes)) {
      return false;
    }
    if (!fragmentPath.equals(that.fragmentPath)) {
      return false;
    }
    if (!Arrays.equals(uniforms, that.uniforms)) {
      return false;
    }
    if (!vertexPath.equals(that.vertexPath)) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int result = vertexPath.hashCode();
    result = 31 * result + fragmentPath.hashCode();
    result = 31 * result + Arrays.hashCode(uniforms);
    result = 31 * result + Arrays.hashCode(attributes);
    return result;
  }

  @Override
  public String toString() {
    return "ShaderProgramKey{" +
           "vertexPath='" + vertexPath + '\'' +
           ", fragmentPath='" + fragmentPath + '\'' +
           ", uniforms=" + Arrays.toString(uniforms) +
           ", attributes=" + Arrays.toString(attributes) +
           '}';
  }
}
