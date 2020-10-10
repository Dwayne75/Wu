package com.wurmonline.server.spells;

import com.wurmonline.server.HistoryManager;
import com.wurmonline.server.Items;
import com.wurmonline.server.Server;
import com.wurmonline.server.Servers;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.villages.Villages;
import com.wurmonline.server.zones.Zones;

public class LandOfTheDead
  extends ReligiousSpell
{
  public static final int RANGE = 50;
  
  public LandOfTheDead()
  {
    super("Land of the Dead", 435, 60, 300, 70, 70, 259200000L);
    this.targetItem = true;
    this.targetTile = true;
    this.description = "summons the souls of the deceased";
    this.type = 0;
  }
  
  boolean precondition(Skill castSkill, Creature performer, int tilex, int tiley, int layer)
  {
    if (!Servers.isThisAPvpServer())
    {
      performer.getCommunicator().sendNormalServerMessage("Libila cannot grant that power right now.");
      return false;
    }
    return true;
  }
  
  boolean precondition(Skill castSkill, Creature performer, Item target)
  {
    if (!Servers.isThisAPvpServer())
    {
      performer.getCommunicator().sendNormalServerMessage("Libila cannot grant that power right now.");
      return false;
    }
    return true;
  }
  
  void doEffect(Skill castSkill, double power, Creature performer, int tilex, int tiley, int layer, int heightOffset)
  {
    castLandOfTheDead(performer, performer.getTileX(), performer.getTileY(), Math.max(10.0D, power));
  }
  
  void doEffect(Skill castSkill, double power, Creature performer, Item target)
  {
    castLandOfTheDead(performer, performer.getTileX(), performer.getTileY(), Math.max(10.0D, power));
  }
  
  private void castLandOfTheDead(Creature performer, int startx, int starty, double power)
  {
    performer.getCommunicator().sendNormalServerMessage("You call back souls of the dead! Possess the shells! RISE! RISE!", (byte)2);
    
    Server.getInstance().broadCastAction(performer.getName() + " commands the souls of the dead to return and possess the shells of the deceased!", performer, 10);
    Village v = Villages.getVillage(startx, starty, true);
    if (v == null) {
      for (int x = -50; x < 50; x += 5) {
        for (int y = -50; y < 50; y += 5)
        {
          v = Villages.getVillage(startx + x, starty + y, true);
          if (v != null) {
            break;
          }
        }
      }
    }
    if (v != null) {
      HistoryManager.addHistory(performer.getName(), "Casts land of the dead near " + v.getName());
    } else {
      HistoryManager.addHistory(performer.getName(), "Casts land of the dead");
    }
    int minx = Zones.safeTileX(startx - (int)(200.0D * power / 100.0D));
    int miny = Zones.safeTileY(starty - (int)(200.0D * power / 100.0D));
    int endx = Zones.safeTileX(startx + (int)(200.0D * power / 100.0D));
    int endy = Zones.safeTileY(starty + (int)(200.0D * power / 100.0D));
    Item[] its = Items.getAllItems();
    int maxCorpses = (int)power;
    for (int itx = 0; itx < its.length; itx++) {
      if (its[itx].getZoneId() > -1) {
        if (its[itx].getTemplateId() == 272)
        {
          int centerx = its[itx].getTileX();
          int centery = its[itx].getTileY();
          if ((centerx < endx) && (centerx > minx) && (centery < endy) && (centery > miny)) {
            if ((Rebirth.mayRaise(performer, its[itx], false)) && (maxCorpses > 0))
            {
              Rebirth.raise(power, performer, its[itx], true);
              maxCorpses--;
            }
          }
        }
      }
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\spells\LandOfTheDead.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */