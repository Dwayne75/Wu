package com.wurmonline.server.creatures.ai.scripts;

import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.ai.CreatureAI;
import com.wurmonline.server.creatures.ai.CreatureAIData;
import com.wurmonline.server.zones.NoSuchZoneException;
import com.wurmonline.server.zones.Zones;

public class FishAI
  extends CreatureAI
{
  protected boolean pollMovement(Creature c, long delta)
  {
    FishAI.FishAIData aiData = (FishAI.FishAIData)c.getCreatureAIData();
    float targetX = aiData.getTargetPosX();
    float targetY = aiData.getTargetPosY();
    if ((targetX < 0.0F) || (targetY < 0.0F)) {
      return false;
    }
    if ((c.getPosX() != targetX) || (c.getPosY() != targetY))
    {
      float diffX = c.getPosX() - targetX;
      float diffY = c.getPosY() - targetY;
      float totalDiff = (float)Math.sqrt(diffX * diffX + diffY * diffY);
      float movementSpeed = aiData.getSpeed() * aiData.getMovementSpeedModifier();
      if (totalDiff < movementSpeed) {
        movementSpeed = totalDiff;
      }
      double lRotation = Math.atan2(targetY - c.getPosY(), targetX - c.getPosX()) * 57.29577951308232D + 90.0D;
      float lXPosMod = (float)Math.sin(lRotation * 0.01745329238474369D) * movementSpeed;
      float lYPosMod = -(float)Math.cos(lRotation * 0.01745329238474369D) * movementSpeed;
      int lNewTileX = (int)(c.getPosX() + lXPosMod) >> 2;
      int lNewTileY = (int)(c.getPosY() + lYPosMod) >> 2;
      int lDiffTileX = lNewTileX - c.getTileX();
      int lDiffTileY = lNewTileY - c.getTileY();
      
      c.setPositionX(c.getPosX() + lXPosMod);
      c.setPositionY(c.getPosY() + lYPosMod);
      c.setRotation((float)lRotation);
      try
      {
        float minZ = Math.min(-0.1F, Zones.calculateHeight(c.getPosX(), c.getPosY(), c.isOnSurface()));
        if (c.getPositionZ() < minZ) {
          c.setPositionZ(minZ + Math.abs(minZ * 0.2F));
        } else if (c.getPositionZ() < minZ * 0.15F) {
          c.setPositionZ(minZ * 0.15F);
        }
      }
      catch (NoSuchZoneException localNoSuchZoneException) {}
      c.moved((int)(lXPosMod * 100.0F), (int)(lYPosMod * 100.0F), 0, lDiffTileX, lDiffTileY);
    }
    return false;
  }
  
  protected boolean pollAttack(Creature c, long delta)
  {
    return false;
  }
  
  protected boolean pollBreeding(Creature c, long delta)
  {
    return false;
  }
  
  public CreatureAIData createCreatureAIData()
  {
    return new FishAI.FishAIData(this);
  }
  
  public void creatureCreated(Creature c) {}
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\creatures\ai\scripts\FishAI.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */