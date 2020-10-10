package com.wurmonline.server.behaviours;

import com.wurmonline.mesh.MeshIO;
import com.wurmonline.mesh.Tiles;
import com.wurmonline.mesh.Tiles.Tile;
import com.wurmonline.server.Server;
import com.wurmonline.server.WurmCalendar;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.CreatureStatus;
import com.wurmonline.server.players.Player;

public enum ModifiedBy
{
  NOTHING(0),  NO_TREES(1),  NEAR_TREE(2),  NEAR_BUSH(3),  NEAR_OAK(4),  EASTER(5),  HUNGER(6),  WOUNDED(7),  NEAR_WATER(8);
  
  private final int code;
  
  private ModifiedBy(int aCode)
  {
    this.code = aCode;
  }
  
  public float chanceModifier(Creature performer, int modifier, int tilex, int tiley)
  {
    if (this == NOTHING) {
      return 0.0F;
    }
    if (this == EASTER)
    {
      if ((!performer.isPlayer()) || ((((Player)performer).isReallyPaying()) && (WurmCalendar.isEaster()) && 
        (!((Player)performer).isReimbursed()))) {
        return modifier;
      }
      return 0.0F;
    }
    if (this == HUNGER)
    {
      if (performer.getStatus().getHunger() < 20) {
        return modifier;
      }
      return 0.0F;
    }
    if (this == WOUNDED)
    {
      if (performer.getStatus().damage > 15) {
        return modifier;
      }
      return 0.0F;
    }
    MeshIO mesh = Server.surfaceMesh;
    if (isAModifier(mesh.getTile(tilex, tiley)))
    {
      if (this == NO_TREES) {
        return 0.0F;
      }
      return modifier;
    }
    for (int x = -1; x <= 1; x++) {
      for (int y = -1; y <= 1; y++) {
        if ((x == -1) || (x == 1) || (y == -1) || (y == 1)) {
          if (isAModifier(mesh.getTile(tilex + x, tiley + y)))
          {
            if (this == NO_TREES) {
              return 0.0F;
            }
            return modifier / 2;
          }
        }
      }
    }
    for (int x = -2; x <= 2; x++) {
      for (int y = -2; y <= 2; y++) {
        if ((x == -2) || (x == 2) || (y == -2) || (y == 2)) {
          if (isAModifier(mesh.getTile(tilex + x, tiley + y)))
          {
            if (this == NO_TREES) {
              return 0.0F;
            }
            return modifier / 3;
          }
        }
      }
    }
    for (int x = -5; x <= 5; x++) {
      for (int y = -5; y <= 5; y++) {
        if ((x <= -3) || (x >= 3) || (y <= -3) || (y >= 3)) {
          if (isAModifier(mesh.getTile(tilex + x, tiley + y)))
          {
            if (this == NO_TREES) {
              return 0.0F;
            }
            return modifier / 4;
          }
        }
      }
    }
    if (this == NO_TREES) {
      return modifier;
    }
    return 0.0F;
  }
  
  private boolean isAModifier(int tile)
  {
    if (this == NEAR_WATER) {
      return Tiles.decodeHeight(tile) < 5;
    }
    byte decodedType = Tiles.decodeType(tile);
    byte decodedData = Tiles.decodeData(tile);
    Tiles.Tile theTile = Tiles.getTile(decodedType);
    if (this == NEAR_OAK)
    {
      if (theTile.isNormalTree()) {
        return theTile.isOak(decodedData);
      }
    }
    else
    {
      if ((this == NEAR_TREE) || (this == NO_TREES)) {
        return theTile.isNormalTree();
      }
      if (this == NEAR_BUSH) {
        return theTile.isNormalBush();
      }
    }
    return false;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\behaviours\ModifiedBy.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */