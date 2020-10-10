package com.wurmonline.server.creatures.ai.scripts;

import com.wurmonline.server.Server;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.CreatureStatus;
import com.wurmonline.server.creatures.ai.CreatureAI;
import com.wurmonline.server.creatures.ai.CreatureAIData;
import com.wurmonline.server.creatures.ai.Path;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zone;
import java.util.Random;

public class BartenderAI
  extends CreatureAI
{
  private static final long MIN_TIME_TALK = 120000L;
  private static final long MIN_TIME_NEWPATH = 30000L;
  private static final int TIMER_SPECTALK = 0;
  private static final int TIMER_NEWPATH = 1;
  
  public void creatureCreated(Creature c)
  {
    if (c.getCurrentTile().getVillage() != null) {
      ((BartenderAI.BartenderAIData)c.getCreatureAIData()).setHomeVillage(c.getCurrentTile().getVillage());
    }
  }
  
  protected boolean pollSpecialFinal(Creature c, long delta)
  {
    increaseTimer(c, delta, new int[] { 0 });
    if (!isTimerReady(c, 0, 120000L)) {
      return false;
    }
    c.say("Come and get some tasty treats!");
    resetTimer(c, new int[] { 0 });
    
    return false;
  }
  
  protected boolean pollMovement(Creature c, long delta)
  {
    BartenderAI.BartenderAIData aiData = (BartenderAI.BartenderAIData)c.getCreatureAIData();
    if (aiData.getFoodTarget() != null) {
      if ((aiData.getFoodTarget().getTileX() == c.getTileX()) && (aiData.getFoodTarget().getTileY() == c.getTileY()))
      {
        c.say("Hey " + aiData.getFoodTarget().getName() + " you look hungry, come and get some food!");
        aiData.setFoodTarget(null);
      }
    }
    if (c.getStatus().getPath() == null)
    {
      if (aiData.getHomeVillage() != null) {
        if (c.getCurrentTile().getVillage() != aiData.getHomeVillage())
        {
          c.startPathingToTile(getMovementTarget(c, aiData.getHomeVillage().getTokenX(), aiData.getHomeVillage().getTokenY()));
          return false;
        }
      }
      increaseTimer(c, delta, new int[] { 1 });
      if (isTimerReady(c, 1, 30000L))
      {
        if (Server.rand.nextInt(100) < 10)
        {
          Creature[] nearbyCreatures = c.getCurrentTile().getZone().getAllCreatures();
          for (Creature otherC : nearbyCreatures) {
            if (otherC != c) {
              if (otherC.isPlayer()) {
                if (otherC.getStatus().isHungry()) {
                  if (otherC.getCurrentTile().getVillage() == aiData.getHomeVillage()) {
                    if ((otherC.getTileX() != c.getTileX()) || (otherC.getTileY() != c.getTileY()))
                    {
                      c.startPathingToTile(getMovementTarget(c, otherC.getTileX(), otherC.getTileY()));
                      aiData.setFoodTarget(otherC);
                    }
                  }
                }
              }
            }
          }
        }
        resetTimer(c, new int[] { 1 });
      }
    }
    else
    {
      pathedMovementTick(c);
      if (c.getStatus().getPath().isEmpty())
      {
        c.getStatus().setPath(null);
        c.getStatus().setMoving(false);
      }
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
    return new BartenderAI.BartenderAIData(this);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\creatures\ai\scripts\BartenderAI.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */