package com.wurmonline.shared.util;

import java.util.Random;

public final class TerrainUtilities
{
  private static final Random random = new Random();
  
  public static float getTreePosX(int xTile, int yTile)
  {
    random.setSeed(xTile * 31273612L + yTile * 4327864168313L);
    return random.nextFloat() * 0.75F + 0.125F;
  }
  
  public static float getTreePosY(int xTile, int yTile)
  {
    random.setSeed(xTile * 31273612L + yTile * 4327864168314L);
    return random.nextFloat() * 0.75F + 0.125F;
  }
  
  public static float getTreeRotation(int xTile, int yTile)
  {
    random.setSeed(xTile * 31273612L + yTile * 4327864168315L);
    return random.nextFloat() * 360.0F;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\shared\util\TerrainUtilities.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */