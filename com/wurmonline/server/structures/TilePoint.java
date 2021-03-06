package com.wurmonline.server.structures;

public class TilePoint
{
  private int tileX;
  private int tileY;
  
  public TilePoint(int pTileX, int pTileY)
  {
    this.tileX = pTileX;
    this.tileY = pTileY;
  }
  
  public void setTileX(int val)
  {
    this.tileX = val;
  }
  
  public int getTileX()
  {
    return this.tileX;
  }
  
  public void setTileY(int val)
  {
    this.tileY = val;
  }
  
  public int getTileY()
  {
    return this.tileY;
  }
  
  public String toString()
  {
    return "[" + this.tileX + "," + this.tileY + "]";
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\structures\TilePoint.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */