package com.perrotuerto.tinygl;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.opengl.GLES20;
import android.text.TextPaint;
import android.util.Log;


/**
 * @author jp.lorandi@cfyar.com Date: 1/30/15 Time: 5:37 PM
 */
public class GLUtils {

  public static final int MIN_SIDE = 256;

  public static void checkGl20Error(String tag) {
    final int error = GLES20.glGetError();
    if (error != GLES20.GL_NO_ERROR) {
      Log.e(tag, String.format("GLError: %d", error));
    }
  }

  public static boolean isAnyInvalid(String tag, int... value) {
    for (int idx = 0; idx < value.length; idx++) {
      int i = value[idx];
      if (i < 0) {
        Log.e(tag, String.format("Id Invalid Index: %d of %d", idx, value.length));
        return true;
      }
    }
    return false;
  }

  public static boolean isAnyInvalid(int... value) {
    return isAnyInvalid("GLAnyInvalid", value);
  }

  public static int next2Pow(int n) {
    n--;
    int shift = 1;
    while (((n + 1) & n) != 0) {
      n |= n >> shift;
      shift *= 2;
    }

    return n + 1;
  }

  public static void main(String[] args) {
    System.out.println("next2Pow(221) = " + next2Pow(221));
    System.out.println("next2Pow(0) = " + next2Pow(0));
    System.out.println("next2Pow(1) = " + next2Pow(1));
    System.out.println("next2Pow(255) = " + next2Pow(255));
    System.out.println("next2Pow(256) = " + next2Pow(256));
  }

  public static Typeface karbon = null;
  public static final float[] dimensions = new float[5]; //width, height, baseline, centerx, centery

  public static Bitmap textAsBitmap(String text, float textSize, int textColor) {
    TextPaint paint = new TextPaint();
    paint.setTypeface(karbon);
    paint.setTextSize(textSize);
    paint.setColor(textColor);
    paint.setTextAlign(Paint.Align.LEFT);
    int width = (int) (paint.measureText(text) + 0.5f); // round
    float baseline = (int) (-paint.ascent() + 0.5f); // ascent() is negative
    int height = (int) (baseline + paint.descent() + 0.5f);

    Log.i("GLUtils text as bitmap",
          String.format("width: %d height: %d baseline: %.2f", width, height, baseline));

    resizeTexture(dimensions,
                  width,
                  height , baseline);

    Bitmap image = Bitmap.createBitmap(
        (int) dimensions[0],(int) dimensions[1], Bitmap.Config.ARGB_8888);
    Canvas canvas = new Canvas(image);

    paint.setAntiAlias(true);
    int radius = (int) (dimensions[0] / 2f);
    paint.setColor(Color.argb(128, 192, 0, 192));

    Log.i("GLUtils text as bitmap",
          String.format("Circle x,y: %d,%d radius: %d", (int) dimensions[0] / 2,
                        (int) dimensions[1] / 2, radius));

    canvas.drawCircle((int) dimensions[0] / 2,(int) dimensions[1] / 2, radius, paint);

    paint.setColor(textColor);
    canvas.drawText(text,
                    (dimensions[0] - width) /2,
                    (dimensions[1] / 2) + dimensions[4] * baseline,
                    paint);
    return image;
  }

  public static void resizeTexture(float[] dimensions, int width, int height, float baseline) {
    float rwidth = next2Pow(width);
    float rheight = next2Pow(height);
    if (rwidth < MIN_SIDE)
      rwidth = MIN_SIDE;
    if (rheight < MIN_SIDE)
      rheight = MIN_SIDE;

    if (rwidth > rheight) {
      rheight = rwidth;
    } else {
      rwidth = rheight;
    }

    float ws = width / rwidth;
    float hs = height / rheight;

    dimensions[0] = rwidth;
    dimensions[1] = rheight;
    dimensions[2] = baseline * hs;
    dimensions[3] = ws;
    dimensions[4] = hs;
  }


}
