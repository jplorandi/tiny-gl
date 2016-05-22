package com.perrotuerto.tinygl.core;

/**
 * @author jp.lorandi@cfyar.com Date: 1/30/15 Time: 5:33 PM
 */
public class TextureKey {
  public String diskName;
  public boolean temporary = false;
  public boolean external = false;

  public TextureKey() {
  }

  public TextureKey(boolean temporary) {
    this.temporary = temporary;
  }
  public TextureKey(boolean temporary, boolean external) {
    this.temporary = temporary;
    this.external = external;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    TextureKey that = (TextureKey) o;

    if (temporary != that.temporary) {
      return false;
    }

    if (diskName == null || that.diskName == null || !diskName.equals(that.diskName) ) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int result = diskName != null ? diskName.hashCode() : 0;
    result = 31 * result + (temporary ? 1 : 0);
    return result;
  }
}
