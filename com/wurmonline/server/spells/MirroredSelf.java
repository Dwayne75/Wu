package com.wurmonline.server.spells;

import com.wurmonline.server.Server;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.CreatureMove;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.players.MovementEntity;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.zones.VirtualZone;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zones;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MirroredSelf
  extends KarmaSpell
{
  public MirroredSelf()
  {
    super("Mirrored Self", 562, 5, 500, 20, 1, 900000L);
    this.targetTile = true;
    this.targetItem = true;
    this.description = "creates deceptive illusions of yourself around you";
  }
  
  void doEffect(Skill castSkill, double power, Creature performer, int tilex, int tiley, int layer, int heightOffset)
  {
    castMirroredSelf(performer, Math.max(10.0D, power));
  }
  
  void doEffect(Skill castSkill, double power, Creature performer, Item target)
  {
    castMirroredSelf(performer, Math.max(10.0D, power));
  }
  
  private void castMirroredSelf(Creature performer, double power)
  {
    int nums = 2 + (int)power / 10;
    int x = 0;
    int y = 0;
    for (int n = 0; n < nums; n++)
    {
      MovementEntity entity = new MovementEntity(performer.getWurmId(), System.currentTimeMillis() + 1000L * Math.max(20L, power));
      CreatureMove startPos = new CreatureMove();
      startPos.diffX = ((byte)(-1 + Server.rand.nextInt(2)));
      startPos.diffY = ((byte)(-1 + Server.rand.nextInt(2)));
      startPos.diffZ = 0;
      entity.setMovePosition(startPos);
      performer.addIllusion(entity);
      VolaTile tile = Zones.getOrCreateTile(performer.getTileX() + 0, performer.getTileY() + 0, performer
        .isOnSurface());
      for (VirtualZone vz : tile.getWatchers()) {
        try
        {
          float posZ = Zones.calculatePosZ((performer.getTileX() + 0) * 4, (performer.getTileY() + 0) * 4, tile, performer
            .isOnSurface(), false, performer.getPositionZ(), performer, -10L);
          float diffZ = performer.getPositionZ() - posZ;
          try
          {
            vz.addCreature(performer.getWurmId(), false, entity.getWurmid(), 0.0F, 0.0F, diffZ);
          }
          catch (Exception localException1) {}
        }
        catch (Exception e)
        {
          logger.log(Level.WARNING, e.getMessage(), e);
        }
      }
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\spells\MirroredSelf.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */