package com.wurmonline.server.spells;

import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Server;
import com.wurmonline.server.behaviours.Methods;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.NoSuchCreatureException;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.NotOwnedException;
import com.wurmonline.server.skills.Skill;

public final class Sunder
  extends ReligiousSpell
{
  public static final int RANGE = 4;
  
  Sunder()
  {
    super("Sunder", 253, 30, 50, 30, 60, 0L);
    this.targetItem = true;
    this.description = "deal damage to item";
    this.type = 1;
  }
  
  boolean precondition(Skill castSkill, Creature performer, Item target)
  {
    if ((performer.mayDestroy(target)) && (Methods.isActionAllowed(performer, (short)83, target))) {
      return true;
    }
    if (!mayBeEnchanted(target))
    {
      performer.getCommunicator().sendNormalServerMessage("Your spell will not work on that.", (byte)3);
      
      return false;
    }
    return true;
  }
  
  void doEffect(Skill castSkill, double power, Creature performer, Item target)
  {
    try
    {
      if (target.getOwner() != -10L)
      {
        Creature owner = Server.getInstance().getCreature(target.getOwner());
        if (!performer.equals(owner)) {
          owner.getCommunicator().sendNormalServerMessage(performer.getName() + " damages your " + target.getName() + ".", (byte)4);
        }
      }
    }
    catch (NoSuchCreatureException|NoSuchPlayerException|NotOwnedException localNoSuchCreatureException) {}
    float qlMod = 1.0F - target.getQualityLevel() / 200.0F;
    float damMod = 1.0F - target.getDamage() / 100.0F;
    
    float weightMod = Math.min(1.0F, 5000.0F / target.getWeightGrams());
    float sunderDamage = (float)(power / 5.0D) * qlMod * damMod * weightMod;
    target.setDamage(target.getDamage() + sunderDamage);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\spells\Sunder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */