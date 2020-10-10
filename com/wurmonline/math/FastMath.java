package com.wurmonline.math;

import java.util.Random;

public final class FastMath
{
  public static final double DBL_EPSILON = 2.220446049250313E-16D;
  public static final float FLT_EPSILON = 1.1920929E-7F;
  public static final float ZERO_TOLERANCE = 1.0E-4F;
  public static final float ONE_THIRD = 0.33333334F;
  public static final float PI = 3.1415927F;
  public static final float TWO_PI = 6.2831855F;
  public static final float HALF_PI = 1.5707964F;
  public static final float QUARTER_PI = 0.7853982F;
  public static final float INV_PI = 0.31830987F;
  public static final float INV_TWO_PI = 0.15915494F;
  public static final float DEG_TO_RAD = 0.017453292F;
  public static final float RAD_TO_DEG = 57.295776F;
  public static final Random rand = new Random(System.currentTimeMillis());
  
  public static boolean isPowerOfTwo(int number)
  {
    return (number > 0) && ((number & number - 1) == 0);
  }
  
  public static int nearestPowerOfTwo(int number)
  {
    return (int)Math.pow(2.0D, Math.ceil(Math.log(number) / Math.log(2.0D)));
  }
  
  public static float lERP(float percent, float startValue, float endValue)
  {
    if (startValue == endValue) {
      return startValue;
    }
    return (1.0F - percent) * startValue + percent * endValue;
  }
  
  public static float acos(float fValue)
  {
    if (-1.0F < fValue)
    {
      if (fValue < 1.0F) {
        return (float)Math.acos(fValue);
      }
      return 0.0F;
    }
    return 3.1415927F;
  }
  
  public static float asin(float fValue)
  {
    if (-1.0F < fValue)
    {
      if (fValue < 1.0F) {
        return (float)Math.asin(fValue);
      }
      return 1.5707964F;
    }
    return -1.5707964F;
  }
  
  public static float atan(float fValue)
  {
    return (float)Math.atan(fValue);
  }
  
  public static float atan2(float fY, float fX)
  {
    return (float)Math.atan2(fY, fX);
  }
  
  public static float ceil(float fValue)
  {
    return (float)Math.ceil(fValue);
  }
  
  public static float reduceSinAngle(float radians)
  {
    radians %= 6.2831855F;
    if (Math.abs(radians) > 3.1415927F) {
      radians -= 6.2831855F;
    }
    if (Math.abs(radians) > 1.5707964F) {
      radians = 3.1415927F - radians;
    }
    return radians;
  }
  
  public static float sin(float fValue)
  {
    fValue = reduceSinAngle(fValue);
    if (Math.abs(fValue) <= 0.7853981633974483D) {
      return (float)Math.sin(fValue);
    }
    return (float)Math.cos(1.5707963267948966D - fValue);
  }
  
  public static float cos(float fValue)
  {
    return sin(fValue + 1.5707964F);
  }
  
  public static float exp(float fValue)
  {
    return (float)Math.exp(fValue);
  }
  
  public static float abs(float fValue)
  {
    if (fValue < 0.0F) {
      return -fValue;
    }
    return fValue;
  }
  
  public static float floor(float fValue)
  {
    return (float)Math.floor(fValue);
  }
  
  public static float invSqrt(float fValue)
  {
    return (float)(1.0D / Math.sqrt(fValue));
  }
  
  public static float log(float fValue)
  {
    return (float)Math.log(fValue);
  }
  
  public static float log(float value, float base)
  {
    return (float)(Math.log(value) / Math.log(base));
  }
  
  public static float pow(float fBase, float fExponent)
  {
    return (float)Math.pow(fBase, fExponent);
  }
  
  public static float sqr(float fValue)
  {
    return fValue * fValue;
  }
  
  public static float sqrt(float fValue)
  {
    return (float)Math.sqrt(fValue);
  }
  
  public static float tan(float fValue)
  {
    return (float)Math.tan(fValue);
  }
  
  public static int sign(int iValue)
  {
    if (iValue > 0) {
      return 1;
    }
    if (iValue < 0) {
      return -1;
    }
    return 0;
  }
  
  public static float sign(float fValue)
  {
    return Math.signum(fValue);
  }
  
  public static float determinant(double m00, double m01, double m02, double m03, double m10, double m11, double m12, double m13, double m20, double m21, double m22, double m23, double m30, double m31, double m32, double m33)
  {
    double det01 = m20 * m31 - m21 * m30;
    double det02 = m20 * m32 - m22 * m30;
    double det03 = m20 * m33 - m23 * m30;
    double det12 = m21 * m32 - m22 * m31;
    double det13 = m21 * m33 - m23 * m31;
    double det23 = m22 * m33 - m23 * m32;
    return (float)(m00 * (m11 * det23 - m12 * det13 + m13 * det12) - m01 * (m10 * det23 - m12 * det03 + m13 * det02) + m02 * (m10 * det13 - m11 * det03 + m13 * det01) - m03 * (m10 * det12 - m11 * det02 + m12 * det01));
  }
  
  public static float nextRandomFloat()
  {
    return rand.nextFloat();
  }
  
  public static int nextRandomInt(int min, int max)
  {
    return (int)(nextRandomFloat() * (max - min + 1)) + min;
  }
  
  public static int nextRandomInt()
  {
    return rand.nextInt();
  }
  
  public static Vector3f sphericalToCartesian(Vector3f sphereCoords, Vector3f store)
  {
    store.y = (sphereCoords.x * sin(sphereCoords.z));
    float a = sphereCoords.x * cos(sphereCoords.z);
    store.x = (a * cos(sphereCoords.y));
    store.z = (a * sin(sphereCoords.y));
    
    return store;
  }
  
  public static Vector3f cartesianToSpherical(Vector3f cartCoords, Vector3f store)
  {
    if (cartCoords.x == 0.0F) {
      cartCoords.x = 1.1920929E-7F;
    }
    store.x = sqrt(cartCoords.x * cartCoords.x + cartCoords.y * cartCoords.y + cartCoords.z * cartCoords.z);
    
    store.y = atan(cartCoords.z / cartCoords.x);
    if (cartCoords.x < 0.0F) {
      store.y += 3.1415927F;
    }
    store.z = asin(cartCoords.y / store.x);
    return store;
  }
  
  public static Vector3f sphericalToCartesianZ(Vector3f sphereCoords, Vector3f store)
  {
    store.z = (sphereCoords.x * sin(sphereCoords.z));
    float a = sphereCoords.x * cos(sphereCoords.z);
    store.x = (a * cos(sphereCoords.y));
    store.y = (a * sin(sphereCoords.y));
    
    return store;
  }
  
  public static Vector3f cartesianZToSpherical(Vector3f cartCoords, Vector3f store)
  {
    if (cartCoords.x == 0.0F) {
      cartCoords.x = 1.1920929E-7F;
    }
    store.x = sqrt(cartCoords.x * cartCoords.x + cartCoords.y * cartCoords.y + cartCoords.z * cartCoords.z);
    
    store.z = atan(cartCoords.z / cartCoords.x);
    if (cartCoords.x < 0.0F) {
      store.z += 3.1415927F;
    }
    store.y = asin(cartCoords.y / store.x);
    return store;
  }
  
  public static float normalize(float val, float min, float max)
  {
    if ((Float.isInfinite(val)) || (Float.isNaN(val))) {
      return 0.0F;
    }
    float range = max - min;
    while (val > max) {
      val -= range;
    }
    while (val < min) {
      val += range;
    }
    return val;
  }
  
  public static float copysign(float x, float y)
  {
    if ((y >= 0.0F) && (x <= 0.0F)) {
      return -x;
    }
    if ((y < 0.0F) && (x >= 0.0F)) {
      return -x;
    }
    return x;
  }
  
  public static float clamp(float input, float min, float max)
  {
    return input > max ? max : input < min ? min : input;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\math\FastMath.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */