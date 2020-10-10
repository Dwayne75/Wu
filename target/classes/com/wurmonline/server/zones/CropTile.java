package com.wurmonline.server.zones;

public class CropTile
  implements Comparable<CropTile>
{
  private int data;
  private int x;
  private int y;
  private int cropType;
  private boolean onSurface;
  
  public CropTile(int tileData, int tileX, int tileY, int typeOfCrop, boolean surface)
  {
    this.data = tileData;
    this.x = tileX;
    this.y = tileY;
    this.cropType = typeOfCrop;
    this.onSurface = surface;
  }
  
  public final int getData()
  {
    return this.data;
  }
  
  public final int getX()
  {
    return this.x;
  }
  
  public final int getY()
  {
    return this.y;
  }
  
  public final int getCropType()
  {
    return this.cropType;
  }
  
  public final boolean isOnSurface()
  {
    return this.onSurface;
  }
  
  public boolean equals(Object obj)
  {
    if (!(obj instanceof CropTile)) {
      return false;
    }
    CropTile c = (CropTile)obj;
    
    return (c.getCropType() == getCropType()) && (c.getData() == getData()) && 
      (c.getX() == getX()) && (c.getY() == getY()) && (c.isOnSurface() == isOnSurface());
  }
  
  public int compareTo(CropTile o)
  {
    int EQUAL = 0;
    int AFTER = 1;
    int BEFORE = -1;
    if (o.equals(this)) {
      return 0;
    }
    if (o.getCropType() > getCropType()) {
      return 1;
    }
    return -1;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\zones\CropTile.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */