package com.wurmonline.server.creatures.ai.scripts;

import com.wurmonline.server.bodys.Body;
import com.wurmonline.server.bodys.Wound;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.ai.CreatureAI;
import com.wurmonline.server.creatures.ai.CreatureAIData;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.NoSpaceException;
import javax.annotation.Nullable;

public class TestDummyAI
  extends CreatureAI
{
  protected boolean pollMovement(Creature c, long delta)
  {
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
    return new TestDummyAIData();
  }
  
  public void creatureCreated(Creature c) {}
  
  public double receivedWound(Creature c, @Nullable Creature performer, byte dmgType, int dmgPosition, float armourMod, double damage)
  {
    if (performer != null) {
      try
      {
        String message = "You dealt " + String.format("%.2f", new Object[] { Double.valueOf(damage / 65535.0D * 100.0D) }) + " to " + c.getBody().getBodyPart(dmgPosition).getName() + " of type " + Wound.getName(dmgType) + ".";
        performer.getCommunicator().sendNormalServerMessage(message);
      }
      catch (NoSpaceException localNoSpaceException) {}
    }
    return 0.0D;
  }
  
  public class TestDummyAIData
    extends CreatureAIData
  {
    public TestDummyAIData() {}
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\creatures\ai\scripts\TestDummyAI.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */