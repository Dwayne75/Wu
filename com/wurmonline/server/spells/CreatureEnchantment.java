package com.wurmonline.server.spells;

import com.wurmonline.server.Server;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.SpellEffects;
import com.wurmonline.server.deities.Deity;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.shared.constants.AttitudeConstants;

public class CreatureEnchantment
  extends ReligiousSpell
  implements AttitudeConstants
{
  float durationModifier = 20.0F;
  
  CreatureEnchantment(String aName, int aNum, int aCastingTime, int aCost, int aDifficulty, int aLevel, long aCooldown)
  {
    super(aName, aNum, aCastingTime, aCost, aDifficulty, aLevel, aCooldown);
    this.targetCreature = true;
  }
  
  boolean precondition(Skill castSkill, Creature performer, Creature target)
  {
    if (target.isReborn())
    {
      performer.getCommunicator().sendNormalServerMessage(target.getNameWithGenus() + " doesn't seem affected.", (byte)3);
      
      return false;
    }
    if (!target.equals(performer))
    {
      if ((!isOffensive()) && (target.getKingdomId() != 0) && 
        (!performer.isFriendlyKingdom(target.getKingdomId())))
      {
        performer.getCommunicator().sendNormalServerMessage("Nothing happens as you try to cast this on an enemy.", (byte)3);
        
        return false;
      }
      if ((performer.getDeity() != null) && (!isOffensive()))
      {
        if (target.getAttitude(performer) != 2) {
          return true;
        }
        performer.getCommunicator().sendNormalServerMessage(performer
          .getDeity().getName() + " would never help the infidel " + target.getName() + ".", (byte)3);
        
        return false;
      }
      return true;
    }
    return true;
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
      eff = new SpellEffect(target.getWurmId(), sp.getEnchantment(), (float)power, Math.max(1, seconds), (byte)9, (byte)(sp.isOffensive() ? 1 : 0), true);
      
      effs.addSpellEffect(eff);
    }
    else if (eff.getPower() < power)
    {
      eff.setPower((float)power);
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
        performer.getCommunicator().sendNormalServerMessage(target.getNameWithGenus() + " now has " + this.effectdesc, (byte)2);
      }
      target.getCommunicator().sendNormalServerMessage("You now have " + this.effectdesc);
      
      eff = new SpellEffect(target.getWurmId(), this.enchantment, (float)power, (int)Math.max(1.0D, Math.max(1.0D, power) * (this.durationModifier + performer.getNumLinks())), (byte)9, (byte)(isOffensive() ? 1 : 0), true);
      
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
      target.getCommunicator().sendNormalServerMessage("You will now receive " + this.effectdesc);
      eff.setPower((float)power);
      eff.setTimeleft(Math.max(eff.timeleft, 
        (int)Math.max(1.0D, Math.max(1.0D, power) * (this.durationModifier + performer.getNumLinks()))));
      target.sendUpdateSpellEffect(eff);
      Server.getInstance().broadCastAction(performer.getName() + " looks pleased.", performer, 5);
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\spells\CreatureEnchantment.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */