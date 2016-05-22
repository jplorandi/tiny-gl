package com.perrotuerto.tinygl.core;

import android.graphics.Bitmap;
import android.util.Log;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * @author jp.lorandi@cfyar.com Date: 1/30/15 Time: 5:33 PM
 */
public class TextureManager {

  private static final Map<TextureKey, Texture> cache = new HashMap<TextureKey, Texture>();
  private static File cacheDir;
  public static ExecutorService service = Executors.newSingleThreadExecutor();

  public static Texture external() {
    TextureKey key = new TextureKey(true, true);

    Texture rval = cache.get(key);
    if (rval == null) {
      rval = new Texture(key);

    }

    cache.put(rval.key, rval);

    return rval;
  }

  public static Texture get(String diskName, boolean load) {
    TextureKey key = getKey(diskName);
    Texture rval = cache.get(key);
    if (rval == null) {
      rval = new Texture(key);

    }

    cache.put(rval.key, rval);

    if (load)
      rval.load();

    return rval;
  }

  public static void deleteTmp() {
    File [] toErase = cacheDir.listFiles(new FileFilter() {
      @Override
      public boolean accept(File pathname) {
        return pathname.getName().startsWith("tmp") && pathname.getName().endsWith(".png");
      }
    });

    for (File aToErase : toErase) {
      aToErase.delete();
    }
  }

  public static Texture get(Bitmap bitmap, boolean load) throws IOException {

    File outFile = File.createTempFile("text", ".png", cacheDir);
//    if (!outFile.createNewFile())
//      Log.w("TextureManager", "Cache dir access prohibited?!");

    Log.i("Texture Manager", String.format("Creating temp file: %s", outFile.getAbsolutePath()));
    String diskName = outFile.getAbsolutePath();

    FileOutputStream fileOutputStream = new FileOutputStream(outFile);
    bitmap.compress(Bitmap.CompressFormat.PNG, 80, fileOutputStream);
    fileOutputStream.close();

    TextureKey key = getKey(diskName);
    Texture rval = cache.get(key);
    if (rval == null) {
      rval = new Texture(key);

    }

    cache.put(rval.key, rval);

    if (load)
      rval.load();

    return rval;
  }

  private static TextureKey getKey(String diskName) {
    TextureKey rval = new TextureKey();
    rval.diskName = diskName;

    return rval;
  }

  public static File getCacheDir() {
    return cacheDir;
  }

  public static void setCacheDir(File cacheDir) {
    TextureManager.cacheDir = cacheDir;
  }

}
