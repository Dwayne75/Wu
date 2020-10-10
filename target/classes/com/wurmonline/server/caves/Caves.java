package com.wurmonline.server.caves;

import java.io.PrintStream;

final class Caves
{
  private static final float MAX_CAVE_SLOPE = 8.0F;
  
  public void digHoleAt(int xFrom, int yFrom, int xTarget, int yTarget, int slope)
  {
    if (!isRockExposed(xTarget, yTarget))
    {
      System.out.println("You can't mine an entrance here.. There's too much dirt on the tile.");
      return;
    }
    if (!isCaveWall(xFrom, yFrom))
    {
      System.out.println("You can't mine an entrance here.. There's a tunnel in the way.");
      return;
    }
    for (int x = xTarget - 1; x <= xTarget + 1; x++) {
      for (int y = yTarget - 1; y <= yTarget + 1; y++) {
        if (isTerrainHole(x, y))
        {
          System.out.println("You can't mine an entrance here.. Too close to an existing entrance.");
          return;
        }
      }
    }
    if (isMinable(xTarget, yTarget))
    {
      for (int x = xFrom; x <= xFrom + 1; x++) {
        for (int y = yFrom; y <= yFrom + 1; y++) {}
      }
      mine(xFrom, yFrom, xTarget, yTarget, slope);
    }
  }
  
  public void mineAt(int xFrom, int yFrom, int xTarget, int yTarget, int slope)
  {
    if (!isMinable(xTarget, yTarget))
    {
      System.out.println("You can't mine here.. There's a tunnel in the way.");
      return;
    }
    if (!isCaveWall(xTarget, xTarget)) {
      mine(xFrom, yFrom, xTarget, yTarget, slope);
    }
  }
  
  private void mine(int xFrom, int yFrom, int xTarget, int yTarget, int slope) {}
  
  private boolean isMinable(int xTarget, int yTarget)
  {
    float lowestFloor = 100000.0F;
    float highestFloor = -100000.0F;
    int tunnels = 0;
    for (int x = xTarget; x <= xTarget + 1; x++) {
      for (int y = yTarget; y <= yTarget + 1; y++) {
        if (isExitCorner(x, y))
        {
          tunnels++;
          float h = getTerrainHeight(x, y);
          if (h < lowestFloor) {
            lowestFloor = h;
          }
          if (h > highestFloor) {
            highestFloor = h;
          }
        }
        else if (isTunnelCorner(x, y))
        {
          tunnels++;
          float h = getCaveFloorHeight(x, y);
          if (h < lowestFloor) {
            lowestFloor = h;
          }
          if (h > highestFloor) {
            highestFloor = h;
          }
        }
      }
    }
    if (tunnels == 0) {
      return true;
    }
    float diff = highestFloor - lowestFloor;
    
    return diff < 8.0F;
  }
  
  private boolean isTunnelCorner(int x, int y)
  {
    if (isCaveTunnel(x, y)) {
      return true;
    }
    if (isCaveTunnel(x - 1, y)) {
      return true;
    }
    if (isCaveTunnel(x - 1, y - 1)) {
      return true;
    }
    if (isCaveTunnel(x, y - 1)) {
      return true;
    }
    return false;
  }
  
  private boolean isExitCorner(int x, int y)
  {
    if (isCaveExit(x, y)) {
      return true;
    }
    if (isCaveExit(x - 1, y)) {
      return true;
    }
    if (isCaveExit(x - 1, y - 1)) {
      return true;
    }
    if (isCaveExit(x, y - 1)) {
      return true;
    }
    return false;
  }
  
  private float getTerrainHeight(int x, int y)
  {
    return 10.0F;
  }
  
  private float getCaveFloorHeight(int x, int y)
  {
    return 10.0F;
  }
  
  private boolean isCaveExit(int x, int y)
  {
    return true;
  }
  
  private boolean isCaveTunnel(int x, int y)
  {
    return true;
  }
  
  private boolean isCaveWall(int x, int y)
  {
    return true;
  }
  
  private boolean isTerrainHole(int x, int y)
  {
    return false;
  }
  
  private boolean isRockExposed(int x, int y)
  {
    return true;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\caves\Caves.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */