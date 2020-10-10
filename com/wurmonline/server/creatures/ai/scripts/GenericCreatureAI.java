package com.wurmonline.server.creatures.ai.scripts;

import com.wurmonline.math.Vector2f;
import com.wurmonline.server.Server;
import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.CreatureStatus;
import com.wurmonline.server.creatures.Creatures;
import com.wurmonline.server.creatures.NoSuchCreatureException;
import com.wurmonline.server.creatures.ai.CreatureAI;
import com.wurmonline.server.creatures.ai.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;

public abstract class GenericCreatureAI
  extends CreatureAI
  implements TimeConstants
{
  private static final int T_NEWMOVEMENT = 0;
  private static final long TD_NEWMOVEMENT = 30000L;
  
  protected boolean pollMovement(final Creature c, long delta)
  {
    final GenericCreatureAIData aiData = (GenericCreatureAIData)c.getCreatureAIData();
    if (aiData.isMovementFrozen()) {
      return false;
    }
    if (c.getStatus().getPath() == null)
    {
      if (c.getTarget() != null)
      {
        if (!c.getTarget().isWithinDistanceTo(c, 6.0F)) {
          c.startPathingToTile(getMovementTarget(c, c.getTarget().getTileX(), c.getTarget().getTileY()));
        } else {
          c.setOpponent(c.getTarget());
        }
      }
      else if (c.getLatestAttackers().length > 0)
      {
        long[] attackers = c.getLatestAttackers();
        ArrayList<Creature> attackerList = new ArrayList();
        for (long a : attackers) {
          try
          {
            attackerList.add(Creatures.getInstance().getCreature(a));
          }
          catch (NoSuchCreatureException localNoSuchCreatureException) {}
        }
        Collections.sort(attackerList, new Comparator()
        {
          public int compare(Creature creature1, Creature creature2)
          {
            float distance1 = creature1.getPos2f().distance(c.getPos2f());
            float distance2 = creature2.getPos2f().distance(c.getPos2f());
            if (aiData.doesPreferPlayers())
            {
              if ((creature1.isPlayer()) && (!creature2.isPlayer())) {
                distance1 *= aiData.getPrefersPlayersModifier();
              }
              if ((!creature1.isPlayer()) && (creature2.isPlayer())) {
                distance2 *= aiData.getPrefersPlayersModifier();
              }
            }
            if (distance1 < distance2) {
              return -1;
            }
            if (distance2 > distance1) {
              return 1;
            }
            return 0;
          }
        });
        boolean gotTarget = false;
        while (!gotTarget)
        {
          if (attackerList.isEmpty()) {
            break;
          }
          Creature newTarget = (Creature)attackerList.remove(0);
          if (newTarget.isWithinDistanceTo(c, c.getMaxHuntDistance()))
          {
            c.setTarget(newTarget.getWurmId(), true);
            if (!c.getTarget().isWithinDistanceTo(c, 6.0F)) {
              c.startPathingToTile(getMovementTarget(c, c.getTarget().getTileX(), c.getTarget().getTileY()));
            }
            gotTarget = true;
          }
        }
      }
      else if ((aiData.hasTether()) && (!c.isWithinTileDistanceTo((int)aiData.getTetherPos().x, (int)aiData.getTetherPos().y, 0, aiData.getTetherDistance())))
      {
        c.startPathingToTile(getMovementTarget(c, (int)aiData.getTetherPos().x, (int)aiData.getTetherPos().y));
      }
      else if (!addPathToInteresting(c, delta))
      {
        increaseTimer(c, delta, new int[] { 0 });
        if (isTimerReady(c, 0, 30000L)) {
          if (Server.rand.nextFloat() < aiData.getRandomMovementChance())
          {
            simpleMovementTick(c);
            resetTimer(c, new int[] { 0 });
          }
        }
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
  
  public void creatureCreated(Creature c)
  {
    GenericCreatureAIData aiData = (GenericCreatureAIData)c.getCreatureAIData();
    if (aiData.hasTether()) {
      aiData.setTether(c.getTileX(), c.getTileY());
    }
  }
  
  protected abstract boolean addPathToInteresting(Creature paramCreature, long paramLong);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\creatures\ai\scripts\GenericCreatureAI.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */