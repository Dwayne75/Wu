package com.wurmonline.shared.constants;

public final class WeatherConstants
{
  private static final double DEGS_TO_RADS = 0.017453292519943295D;
  
  public static final float getNormalizedWindX(float windRotation)
  {
    return -(float)Math.sin(windRotation * 0.017453292519943295D);
  }
  
  public static final float getNormalizedWindY(float windRotation)
  {
    return (float)Math.cos(windRotation * 0.017453292519943295D);
  }
  
  public static final float getWindX(float windRotation, float windPower)
  {
    return -(float)Math.sin(windRotation * 0.017453292519943295D) * Math.abs(windPower);
  }
  
  public static final float getWindY(float windRotation, float windPower)
  {
    return (float)Math.cos(windRotation * 0.017453292519943295D) * Math.abs(windPower);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\shared\constants\WeatherConstants.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */