package com.wurmonline.server.structures;

public class BuildTile
{
  private final int tilex;
  private final int tiley;
  private final int layer;
  
  public BuildTile(int tx, int ty, int tileLayer)
  {
    this.tilex = tx;
    this.tiley = ty;
    this.layer = tileLayer;
  }
  
  public int getTileX()
  {
    return this.tilex;
  }
  
  public int getTileY()
  {
    return this.tiley;
  }
  
  public int getLayer()
  {
    return this.layer;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\structures\BuildTile.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */