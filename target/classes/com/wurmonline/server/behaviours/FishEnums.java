package com.wurmonline.server.behaviours;

import com.wurmonline.server.zones.NoSuchZoneException;
import com.wurmonline.server.zones.Zones;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FishEnums
{
  private static final Logger logger = Logger.getLogger(FishEnums.class.getName());
  private static final byte testTypeId = -1;
  public static final int MIN_DEPTH_SPECIAL_FISH = -100;
  
  static int getWaterDepth(float posx, float posy, boolean isOnSurface)
  {
    try
    {
      return (int)(-Zones.calculateHeight(posx, posy, isOnSurface) * 10.0F);
    }
    catch (NoSuchZoneException e)
    {
      logger.log(Level.WARNING, e.getMessage(), e);
    }
    return 5;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\behaviours\FishEnums.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */