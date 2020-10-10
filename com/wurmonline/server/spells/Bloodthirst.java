package com.wurmonline.server.spells;

import com.wurmonline.server.Server;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemSpellEffects;
import com.wurmonline.server.skills.Skill;

public class Bloodthirst
  extends ItemEnchantment
{
  public static final int RANGE = 4;
  
  public Bloodthirst()
  {
    super("Bloodthirst", 454, 20, 50, 60, 31, 0L);
    this.targetWeapon = true;
    this.enchantment = 45;
    this.effectdesc = "will do more damage the more you hit creatures or players.";
    this.description = "increases damage dealt with weapons, improves power with usage";
  }
  
  void doEffect(Skill castSkill, double power, Creature performer, Item target)
  {
    ItemSpellEffects effs = target.getSpellEffects();
    if (effs == null) {
      effs = new ItemSpellEffects(target.getWurmId());
    }
    SpellEffect eff = effs.getSpellEffect(this.enchantment);
    if (eff == null)
    {
      performer.getCommunicator().sendNormalServerMessage("The " + target
        .getName() + " will now do more and more damage when you hit creatures or other players.", (byte)2);
      
      eff = new SpellEffect(target.getWurmId(), this.enchantment, (float)power, 20000000);
      effs.addSpellEffect(eff);
      Server.getInstance().broadCastAction(performer.getName() + " looks pleased.", performer, 5);
    }
    else if (eff.getPower() / 10.0F > power)
    {
      performer.getCommunicator().sendNormalServerMessage("You frown as you fail to improve the power.", (byte)3);
      
      Server.getInstance().broadCastAction(performer.getName() + " frowns.", performer, 5);
    }
    else
    {
      performer.getCommunicator().sendNormalServerMessage("You succeed in improving the power of the " + this.name + ".", (byte)2);
      
      eff.setPower(eff.getPower() + (float)Math.min((float)power, (10000.0F - eff.getPower()) * 0.1D));
      
      Server.getInstance().broadCastAction(performer.getName() + " looks pleased.", performer, 5);
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\spells\Bloodthirst.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */