package com.wurmonline.server.spells;

import com.wurmonline.server.bodys.Body;
import com.wurmonline.server.bodys.Wound;
import com.wurmonline.server.bodys.Wounds;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.players.Cultist;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.utils.CreatureLineSegment;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zones;
import com.wurmonline.shared.util.MulticolorLineSegment;
import java.util.ArrayList;
import java.util.List;

public class LightOfFo
  extends ReligiousSpell
{
  public static final int RANGE = 4;
  
  public LightOfFo()
  {
    super("Light of Fo", 438, 15, 60, 40, 33, 120000L);
    this.targetTile = true;
    this.healing = true;
    this.description = "covers an area with healing energy, healing multiple wounds from allies";
    this.type = 2;
  }
  
  void doEffect(Skill castSkill, double power, Creature performer, int tilex, int tiley, int layer, int heightOffset)
  {
    performer.getCommunicator().sendNormalServerMessage("You place the Mark of Fo in the area, declaring a sanctuary.");
    
    int sx = Zones.safeTileX(tilex - (int)Math.max(1.0D, power / 10.0D + performer.getNumLinks()));
    int sy = Zones.safeTileY(tiley - (int)Math.max(1.0D, power / 10.0D + performer.getNumLinks()));
    int ex = Zones.safeTileX(tilex + (int)Math.max(1.0D, power / 10.0D + performer.getNumLinks()));
    int ey = Zones.safeTileY(tiley + (int)Math.max(1.0D, power / 10.0D + performer.getNumLinks()));
    int totalHealed = 0;
    for (int x = sx; x <= ex; x++) {
      for (int y = sy; y <= ey; y++)
      {
        VolaTile t = Zones.getTileOrNull(x, y, performer.isOnSurface());
        if (t != null) {
          for (Creature lCret : t.getCreatures())
          {
            boolean doHeal = false;
            if ((lCret.getKingdomId() == performer.getKingdomId()) || 
              (lCret.getAttitude(performer) == 1)) {
              doHeal = true;
            }
            Village lVill = lCret.getCitizenVillage();
            if (lVill != null) {
              if (lVill.isEnemy(performer)) {
                doHeal = false;
              }
            }
            Village pVill = performer.getCitizenVillage();
            if (pVill != null) {
              if (pVill.isEnemy(lCret)) {
                doHeal = false;
              }
            }
            if (doHeal) {
              if ((lCret.getBody() != null) && (lCret.getBody().getWounds() != null))
              {
                Wounds tWounds = lCret.getBody().getWounds();
                
                double healingPool = 16375.0D;
                healingPool += 98250.0D * (power / 100.0D);
                if ((performer.getCultist() != null) && (performer.getCultist().healsFaster())) {
                  healingPool *= 2.0D;
                }
                double resistance = SpellResist.getSpellResistance(lCret, getNumber());
                healingPool *= resistance;
                
                int woundsHealed = 0;
                int maxWoundHeal = (int)(healingPool * 0.2D);
                Wound[] arrayOfWound1 = tWounds.getWounds();int k = arrayOfWound1.length;
                Wound w;
                for (Wound localWound1 = 0; localWound1 < k; localWound1++)
                {
                  w = arrayOfWound1[localWound1];
                  if (woundsHealed >= 5) {
                    break;
                  }
                  if (w.getSeverity() >= maxWoundHeal)
                  {
                    healingPool -= maxWoundHeal;
                    SpellResist.addSpellResistance(lCret, getNumber(), maxWoundHeal);
                    w.modifySeverity(-maxWoundHeal);
                    woundsHealed++;
                  }
                }
                Object targetWound;
                while ((woundsHealed < 5) && (tWounds.getWounds().length > 0))
                {
                  targetWound = tWounds.getWounds()[0];
                  
                  Wound[] arrayOfWound2 = tWounds.getWounds();localWound1 = arrayOfWound2.length;
                  for (w = 0; w < localWound1; w++)
                  {
                    Wound w = arrayOfWound2[w];
                    if (w.getSeverity() > ((Wound)targetWound).getSeverity()) {
                      targetWound = w;
                    }
                  }
                  SpellResist.addSpellResistance(lCret, 249, ((Wound)targetWound).getSeverity());
                  ((Wound)targetWound).heal();
                  woundsHealed++;
                }
                if (woundsHealed < 5) {
                  for (Wound w : tWounds.getWounds())
                  {
                    if (woundsHealed >= 5) {
                      break;
                    }
                    if (w.getSeverity() <= maxWoundHeal)
                    {
                      SpellResist.addSpellResistance(lCret, getNumber(), w.getSeverity());
                      w.heal();
                      woundsHealed++;
                    }
                    else
                    {
                      SpellResist.addSpellResistance(lCret, getNumber(), maxWoundHeal);
                      w.modifySeverity(-maxWoundHeal);
                      woundsHealed++;
                    }
                  }
                }
                VolaTile tt = Zones.getTileOrNull(lCret.getTileX(), lCret
                  .getTileY(), lCret
                  .isOnSurface());
                if (tt != null) {
                  tt.sendAttachCreatureEffect(lCret, (byte)11, (byte)0, (byte)0, (byte)0, (byte)0);
                }
                totalHealed++;
                
                String heal = performer == lCret ? "heal" : "heals";
                Object segments = new ArrayList();
                ((ArrayList)segments).add(new CreatureLineSegment(performer));
                ((ArrayList)segments).add(new MulticolorLineSegment(" " + heal + " some of your wounds with " + getName() + ".", (byte)0));
                lCret.getCommunicator().sendColoredMessageCombat((List)segments);
              }
            }
          }
        }
      }
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\spells\LightOfFo.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */