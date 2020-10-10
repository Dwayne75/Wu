package com.wurmonline.mesh;

public final class CaveTile
  implements Cloneable
{
  private static final CaveTile[] tiles = new CaveTile['Ā'];
  static final CaveTile TILE_HOLE = new CaveTile("hole", 0);
  static final CaveTile TILE_ROCK = new CaveTile("Rock", 1);
  final byte id;
  
  private CaveTile(String name, int id)
  {
    this.id = ((byte)id);
    tiles[id] = this;
  }
  
  static CaveTile getTile(int id)
  {
    return tiles[id];
  }
  
  byte getId()
  {
    return this.id;
  }
  
  public static float decodeHeightAsFloat(int encodedTile)
  {
    return Tiles.decodeHeightAsFloat(encodedTile);
  }
  
  public static byte decodeCeilingTexture(int encodedTile)
  {
    return (byte)(encodedTile >> 28 & 0xF);
  }
  
  public static byte decodeFloorTexture(int encodedTile)
  {
    return (byte)(encodedTile >> 24 & 0xF);
  }
  
  public static int decodeCeilingHeight(int encodedTile)
  {
    return encodedTile >> 16 & 0xFF;
  }
  
  public static float decodeCeilingHeightAsFloat(int encodedTile)
  {
    return (encodedTile >> 16 & 0xFF) / 10.0F;
  }
  
  public static int decodeCaveTileDir(long wurmId)
  {
    return (int)(wurmId >> 48) & 0xFF;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\mesh\CaveTile.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */