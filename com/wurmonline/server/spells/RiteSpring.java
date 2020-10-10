package com.wurmonline.server.spells;

import com.wurmonline.server.HistoryManager;
import com.wurmonline.server.Server;
import com.wurmonline.server.Servers;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.deities.Deities;
import com.wurmonline.server.deities.Deity;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.skills.Skill;

public class RiteSpring
  extends ReligiousSpell
{
  public static final int RANGE = 4;
  
  public RiteSpring()
  {
    super("Rite of Spring", 403, 100, 300, 60, 50, 43200000L);
    this.isRitual = true;
    this.targetItem = true;
    this.description = "followers of your god receives a permanent blessing";
    this.type = 0;
  }
  
  boolean precondition(Skill castSkill, Creature performer, Item target)
  {
    if (performer.getDeity() != null)
    {
      Deity deity = performer.getDeity();
      Deity templateDeity = Deities.getDeity(deity.getTemplateDeity());
      if ((templateDeity.getFavor() < 1000) && (!Servers.isThisATestServer()))
      {
        performer.getCommunicator().sendNormalServerMessage(deity
          .getName() + " can not grant that power right now.", (byte)3);
        
        return false;
      }
      if (target.getBless() == deity) {
        if (target.isDomainItem()) {
          return true;
        }
      }
      performer.getCommunicator().sendNormalServerMessage(
        String.format("You need to cast this spell at an altar of %s.", new Object[] {deity.getName() }), (byte)3);
    }
    return false;
  }
  
  void doEffect(Skill castSkill, double power, Creature performer, Item target)
  {
    Deity deity = performer.getDeity();
    Deity templateDeity = Deities.getDeity(deity.getTemplateDeity());
    
    performer.getCommunicator().sendNormalServerMessage("Followers of " + deity.getName() + " receive a blessing!", (byte)2);
    Server.getInstance().broadCastSafe("As the Rite of Spring is completed, followers of " + deity.getName() + " may now receive a blessing!");
    HistoryManager.addHistory(performer.getName(), "casts " + this.name + ". Followers of " + deity.getName() + " receive a blessing!");
    templateDeity.setFavor(templateDeity.getFavor() - 1000);
    
    performer.achievement(635);
    
    new RiteEvent.RiteOfSpringEvent(-10, performer.getWurmId(), getNumber(), deity.getNumber(), System.currentTimeMillis(), 86400000L);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\spells\RiteSpring.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */