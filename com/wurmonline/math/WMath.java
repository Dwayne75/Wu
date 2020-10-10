package com.wurmonline.math;

public final class WMath
{
  private static final float pi = 3.1415927F;
  public static final float pi2 = 6.2831855F;
  public static final float DEG_TO_RAD = 0.017453292F;
  public static final float RAD_TO_DEG = 57.295776F;
  public static final float FAR_AWAY = Float.MAX_VALUE;
  
  public static float atan2(float y, float x)
  {
    if (y == 0.0F) {
      return 0.0F;
    }
    float coeff_1 = 0.7853982F;
    float coeff_2 = 2.3561945F;
    float abs_y = Math.abs(y);
    float angle = 0.0F;
    if (x >= 0.0F)
    {
      float r = (x - abs_y) / (x + abs_y);
      angle = 0.7853982F - 0.7853982F * r;
    }
    else
    {
      float r = (x + abs_y) / (abs_y - x);
      angle = 2.3561945F - 0.7853982F * r;
    }
    if (y < 0.0F) {
      return -angle;
    }
    return angle;
  }
  
  public static int floor(float f)
  {
    return f > 0.0F ? (int)f : -(int)-f;
  }
  
  public static float abs(float f)
  {
    return f >= 0.0F ? f : -f;
  }
  
  public static float getRadFromDeg(float deg)
  {
    return 0.017453292F * deg;
  }
  
  public static float getDegFromRad(float rad)
  {
    return 57.295776F * rad;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\math\WMath.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */