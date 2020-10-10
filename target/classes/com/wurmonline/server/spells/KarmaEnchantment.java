package com.wurmonline.server.spells;

import com.wurmonline.server.Server;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.SpellEffects;
import com.wurmonline.server.deities.Deity;
import com.wurmonline.server.skills.Skill;

public class KarmaEnchantment
  extends KarmaSpell
{
  float durationModifier = 20.0F;
  
  public KarmaEnchantment(String aName, int aNum, int aCastingTime, int aCost, int aDifficulty, int aLevel, long aCooldown)
  {
    super(aName, aNum, aCastingTime, aCost, aDifficulty, aLevel, aCooldown);
    this.targetCreature = true;
    this.targetTile = true;
  }
  
  boolean precondition(Skill castSkill, Creature performer, Creature target)
  {
    if (target != null)
    {
      if (target.isReborn())
      {
        performer.getCommunicator().sendNormalServerMessage(target.getName() + " doesn't seem affected.", (byte)3);
        
        return true;
      }
      if (!target.equals(performer))
      {
        if ((!this.offensive) && (target.getKingdomId() != 0) && 
          (!performer.isFriendlyKingdom(target.getKingdomId())))
        {
          performer.getCommunicator().sendNormalServerMessage("Nothing happens as you try to cast this on an enemy.", (byte)3);
          
          return false;
        }
        if (performer.getDeity() != null)
        {
          if (target.getDeity() != null)
          {
            if ((!this.offensive) && (!performer.getDeity().accepts(target.getDeity().getAlignment())))
            {
              performer.getCommunicator().sendNormalServerMessage(performer
                .getDeity().getName() + " would never help the infidel " + target.getName() + ".", (byte)3);
              
              return false;
            }
            return true;
          }
          return true;
        }
        return true;
      }
      return true;
    }
    return false;
  }
  
  public static final void doImmediateEffect(int number, int seconds, double power, Creature target)
  {
    Spell sp = Spells.getSpell(number);
    SpellEffects effs = target.getSpellEffects();
    if (effs == null) {
      effs = target.createSpellEffects();
    }
    SpellEffect eff = effs.getSpellEffect(sp.getEnchantment());
    if (eff == null)
    {
      eff = new SpellEffect(target.getWurmId(), sp.getEnchantment(), sp.getEnchantment() == 68 ? 100.0F : (float)power, Math.max(1, seconds), (byte)9, (byte)(sp.isOffensive() ? 1 : 0), true);
      
      effs.addSpellEffect(eff);
    }
    else if (eff.getPower() < power)
    {
      eff.setPower(sp.getEnchantment() == 68 ? 100.0F : (float)power);
      
      eff.setTimeleft(Math.max(eff.timeleft, Math.max(1, seconds)));
      target.sendUpdateSpellEffect(eff);
    }
  }
  
  void doEffect(Skill castSkill, double power, Creature performer, Creature target)
  {
    SpellEffects effs = target.getSpellEffects();
    if (effs == null) {
      effs = target.createSpellEffects();
    }
    SpellEffect eff = effs.getSpellEffect(this.enchantment);
    if (eff == null)
    {
      if (target != performer) {
        performer.getCommunicator().sendNormalServerMessage(target.getName() + " now has " + this.effectdesc, (byte)2);
      }
      target.getCommunicator().sendNormalServerMessage("You now have " + this.effectdesc, (byte)2);
      
      eff = new SpellEffect(target.getWurmId(), this.enchantment, this.enchantment == 68 ? 100.0F : (float)power, (int)(Math.max(1.0D, power) * (this.durationModifier + performer.getNumLinks())), (byte)9, (byte)(isOffensive() ? 1 : 0), true);
      
      effs.addSpellEffect(eff);
      Server.getInstance().broadCastAction(performer.getName() + " looks pleased.", performer, 5);
    }
    else if (eff.getPower() > power)
    {
      performer.getCommunicator().sendNormalServerMessage("You frown as you fail to improve the power.", (byte)3);
      
      Server.getInstance().broadCastAction(performer.getName() + " frowns.", performer, 5);
    }
    else
    {
      if (target != performer) {
        performer.getCommunicator().sendNormalServerMessage("You succeed in improving the power of the " + this.name + ".", (byte)2);
      }
      target.getCommunicator().sendNormalServerMessage("You will now receive improved " + this.effectdesc, (byte)2);
      
      eff.setPower((float)power);
      eff.setTimeleft(Math.max(eff.timeleft, 
      
        (int)(Math.max(1.0D, this.enchantment == 68 ? 100.0D : power) * (this.durationModifier + performer.getNumLinks()))));
      target.sendUpdateSpellEffect(eff);
      Server.getInstance().broadCastAction(performer.getName() + " looks pleased.", performer, 5);
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\spells\KarmaEnchantment.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */