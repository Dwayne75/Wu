package com.wurmonline.server;

import com.wurmonline.mesh.MeshIO;
import com.wurmonline.mesh.Tiles;
import com.wurmonline.mesh.Tiles.Tile;
import com.wurmonline.server.zones.Zones;

public class MeshTile
  implements MiscConstants
{
  final MeshIO mesh;
  final int tilex;
  final int tiley;
  final int currentTile;
  final short currentHeight;
  
  public MeshTile(MeshIO mesh, int tilex, int tiley)
  {
    this.mesh = mesh;
    this.tilex = Zones.safeTileX(tilex);
    this.tiley = Zones.safeTileY(tiley);
    this.currentTile = mesh.getTile(this.tilex, this.tiley);
    this.currentHeight = Tiles.decodeHeight(this.currentTile);
  }
  
  public int getEncodedTile()
  {
    return this.currentTile;
  }
  
  public byte getTileType()
  {
    return Tiles.decodeType(this.currentTile);
  }
  
  public short getTileHeight()
  {
    return this.currentHeight;
  }
  
  public float getHeightAsFloat()
  {
    return this.currentHeight / 10.0F;
  }
  
  public byte getTileData()
  {
    return Tiles.decodeData(this.currentTile);
  }
  
  public boolean isHole()
  {
    return getTileType() == Tiles.Tile.TILE_HOLE.id;
  }
  
  public int getLowerLip()
  {
    if (getNorthSlope() == 0) {
      if ((getWestSlope() > 0) && (getEastSlope() > 0)) {
        return 0;
      }
    }
    if (getSouthSlope() == 0) {
      if ((getWestSlope() < 0) && (getEastSlope() < 0)) {
        return 4;
      }
    }
    if (getWestSlope() == 0) {
      if ((getNorthSlope() > 0) && (getSouthSlope() > 0)) {
        return 6;
      }
    }
    if (getEastSlope() == 0) {
      if ((getNorthSlope() < 0) && (getSouthSlope() < 0)) {
        return 2;
      }
    }
    return -1;
  }
  
  public short getWestSlope()
  {
    int southTile = this.mesh.getTile(this.tilex, this.tiley + 1);
    short heightSW = Tiles.decodeHeight(southTile);
    return (short)(heightSW - this.currentHeight);
  }
  
  public short getNorthSlope()
  {
    int eastTile = this.mesh.getTile(this.tilex + 1, this.tiley);
    short heightNE = Tiles.decodeHeight(eastTile);
    return (short)(heightNE - this.currentHeight);
  }
  
  public short getEastSlope()
  {
    int eastTile = this.mesh.getTile(this.tilex + 1, this.tiley);
    short heightNE = Tiles.decodeHeight(eastTile);
    int southEastTile = this.mesh.getTile(this.tilex + 1, this.tiley + 1);
    short heightSE = Tiles.decodeHeight(southEastTile);
    return (short)(heightSE - heightNE);
  }
  
  public short getSouthSlope()
  {
    int southEastTile = this.mesh.getTile(this.tilex + 1, this.tiley + 1);
    short heightSE = Tiles.decodeHeight(southEastTile);
    int southTile = this.mesh.getTile(this.tilex, this.tiley + 1);
    short heightSW = Tiles.decodeHeight(southTile);
    return (short)(heightSE - heightSW);
  }
  
  public boolean checkSlopes(int maxStraight, int maxDiagonal)
  {
    if ((Math.abs(getNorthSlope()) > maxStraight) || 
      (Math.abs(getSouthSlope()) > maxStraight) || 
      (Math.abs(getEastSlope()) > maxStraight) || 
      (Math.abs(getWestSlope()) > maxStraight)) {
      return true;
    }
    int southEastTile = this.mesh.getTile(this.tilex + 1, this.tiley + 1);
    short heightSE = Tiles.decodeHeight(southEastTile);
    if (Math.abs(this.currentHeight - heightSE) > maxDiagonal) {
      return true;
    }
    int southTile = this.mesh.getTile(this.tilex, this.tiley + 1);
    short heightS = Tiles.decodeHeight(southTile);
    int eastTile = this.mesh.getTile(this.tilex + 1, this.tiley);
    short heightE = Tiles.decodeHeight(eastTile);
    if (Math.abs(heightE - heightS) > maxDiagonal) {
      return true;
    }
    return false;
  }
  
  public boolean isFlat()
  {
    return (getNorthSlope() == 0) && (getSouthSlope() == 0) && (getEastSlope() == 0);
  }
  
  public MeshTile getNorthMeshTile()
  {
    return new MeshTile(this.mesh, this.tilex, this.tiley - 1);
  }
  
  public MeshTile getNorthWestMeshTile()
  {
    return new MeshTile(this.mesh, this.tilex - 1, this.tiley - 1);
  }
  
  public MeshTile getWestMeshTile()
  {
    return new MeshTile(this.mesh, this.tilex - 1, this.tiley);
  }
  
  public MeshTile getSouthWestMeshTile()
  {
    return new MeshTile(this.mesh, this.tilex - 1, this.tiley + 1);
  }
  
  public MeshTile getSouthMeshTile()
  {
    return new MeshTile(this.mesh, this.tilex, this.tiley + 1);
  }
  
  public MeshTile getSouthEastMeshTile()
  {
    return new MeshTile(this.mesh, this.tilex + 1, this.tiley + 1);
  }
  
  public MeshTile getEastMeshTile()
  {
    return new MeshTile(this.mesh, this.tilex + 1, this.tiley);
  }
  
  public MeshTile getNorthEastMeshTile()
  {
    return new MeshTile(this.mesh, this.tilex + 1, this.tiley + 1);
  }
  
  public boolean isUnder(int height)
  {
    if (this.currentHeight <= height) {
      return true;
    }
    if (getSouthMeshTile().getTileHeight() <= height) {
      return true;
    }
    if (getEastMeshTile().getTileHeight() <= height) {
      return true;
    }
    if (getSouthEastMeshTile().getTileHeight() <= height) {
      return true;
    }
    return false;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\MeshTile.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */