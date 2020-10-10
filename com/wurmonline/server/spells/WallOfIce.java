package com.wurmonline.server.spells;

import com.wurmonline.mesh.Tiles.TileBorderDirection;
import com.wurmonline.server.Server;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.sounds.SoundPlayer;
import com.wurmonline.server.structures.DbFence;
import com.wurmonline.server.structures.Fence;
import com.wurmonline.server.structures.Wall;
import com.wurmonline.server.zones.NoSuchZoneException;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zone;
import com.wurmonline.server.zones.Zones;
import com.wurmonline.shared.constants.StructureConstantsEnum;

public class WallOfIce
  extends KarmaSpell
{
  public static final int RANGE = 24;
  
  public WallOfIce()
  {
    super("Wall of Ice", 556, 10, 400, 10, 1, 0L);
    this.targetTileBorder = true;
    this.offensive = true;
    this.description = "creates a magical wall of ice on a tile border";
  }
  
  boolean precondition(Skill castSkill, Creature performer, int tileBorderx, int tileBordery, int layer, int heightOffset, Tiles.TileBorderDirection dir)
  {
    VolaTile t = Zones.getTileOrNull(tileBorderx, tileBordery, layer == 0);
    Object fences;
    if (t != null)
    {
      Wall[] walls = t.getWallsForLevel(heightOffset / 30);
      Wall[] arrayOfWall1 = walls;int i = arrayOfWall1.length;
      for (Wall localWall1 = 0; localWall1 < i; localWall1++)
      {
        wall = arrayOfWall1[localWall1];
        if (wall.isHorizontal() == (dir == Tiles.TileBorderDirection.DIR_HORIZ)) {
          if ((wall.getStartX() == tileBorderx) && (wall.getStartY() == tileBordery)) {
            return false;
          }
        }
      }
      fences = t.getFencesForDir(dir);
      Object localObject1 = fences;localWall1 = localObject1.length;
      for (Wall wall = 0; wall < localWall1; wall++)
      {
        Fence f = localObject1[wall];
        if (f.getHeightOffset() == heightOffset) {
          return false;
        }
      }
    }
    Object t2;
    if (dir == Tiles.TileBorderDirection.DIR_DOWN)
    {
      VolaTile t1 = Zones.getTileOrNull(tileBorderx, tileBordery, layer == 0);
      Creature localCreature1;
      Creature c;
      if (t1 != null)
      {
        fences = t1.getCreatures();int j = fences.length;
        for (localCreature1 = 0; localCreature1 < j; localCreature1++)
        {
          c = fences[localCreature1];
          if (c.isPlayer()) {
            return false;
          }
        }
      }
      t2 = Zones.getTileOrNull(tileBorderx - 1, tileBordery, layer == 0);
      if (t2 != null)
      {
        Creature[] arrayOfCreature1 = ((VolaTile)t2).getCreatures();localCreature1 = arrayOfCreature1.length;
        for (c = 0; c < localCreature1; c++)
        {
          Creature c = arrayOfCreature1[c];
          if (c.isPlayer()) {
            return false;
          }
        }
      }
    }
    else
    {
      VolaTile t1 = Zones.getTileOrNull(tileBorderx, tileBordery, layer == 0);
      Creature localCreature2;
      Creature c;
      if (t1 != null)
      {
        t2 = t1.getCreatures();int k = t2.length;
        for (localCreature2 = 0; localCreature2 < k; localCreature2++)
        {
          c = t2[localCreature2];
          if (c.isPlayer()) {
            return false;
          }
        }
      }
      VolaTile t2 = Zones.getTileOrNull(tileBorderx, tileBordery - 1, layer == 0);
      if (t2 != null)
      {
        Creature[] arrayOfCreature2 = t2.getCreatures();localCreature2 = arrayOfCreature2.length;
        for (c = 0; c < localCreature2; c++)
        {
          Creature c = arrayOfCreature2[c];
          if (c.isPlayer()) {
            return false;
          }
        }
      }
    }
    return true;
  }
  
  void doEffect(Skill castSkill, double power, Creature performer, int tilex, int tiley, int layer, int heightOffset, Tiles.TileBorderDirection dir)
  {
    SoundPlayer.playSound("sound.religion.channel", tilex, tiley, performer.isOnSurface(), 0.0F);
    try
    {
      Zone zone = Zones.getZone(tilex, tiley, true);
      
      Fence fence = new DbFence(StructureConstantsEnum.FENCE_MAGIC_ICE, tilex, tiley, heightOffset, (float)(1.0D + power / 5.0D), dir, zone.getId(), layer);
      fence.setState(fence.getFinishState());
      fence.setQualityLevel((float)power);
      fence.improveOrigQualityLevel((float)power);
      zone.addFence(fence);
      performer.achievement(320);
      performer.getCommunicator().sendNormalServerMessage("You weave the source and create a wall.");
      Server.getInstance().broadCastAction(performer.getName() + " creates a wall.", performer, 5);
    }
    catch (NoSuchZoneException localNoSuchZoneException) {}
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\spells\WallOfIce.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */