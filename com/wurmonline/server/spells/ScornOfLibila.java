package com.wurmonline.server.spells;

import com.wurmonline.server.bodys.Body;
import com.wurmonline.server.bodys.Wound;
import com.wurmonline.server.bodys.Wounds;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.players.Cultist;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.structures.Structure;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zones;

public class ScornOfLibila
  extends DamageSpell
{
  public static final int RANGE = 4;
  public static final double BASE_DAMAGE = 4000.0D;
  public static final double DAMAGE_PER_POWER = 40.0D;
  public static final int RADIUS = 3;
  
  public ScornOfLibila()
  {
    super("Scorn of Libila", 448, 15, 40, 50, 40, 120000L);
    
    this.targetTile = true;
    this.offensive = true;
    this.healing = true;
    this.description = "covers an area with draining energy, causing internal wounds on enemies and healing allies";
    this.type = 2;
  }
  
  void doEffect(Skill castSkill, double power, Creature performer, int tilex, int tiley, int layer, int heightOffset)
  {
    performer.getCommunicator().sendNormalServerMessage("You place the Mark of Libila where you stand, declaring a sanctuary.");
    
    Structure currstr = performer.getCurrentTile().getStructure();
    
    int radiusBonus = (int)(power / 40.0D);
    int sx = Zones.safeTileX(performer.getTileX() - 3 - radiusBonus - performer.getNumLinks());
    int sy = Zones.safeTileY(performer.getTileY() - 3 - radiusBonus - performer.getNumLinks());
    int ex = Zones.safeTileX(performer.getTileX() + 3 + radiusBonus + performer.getNumLinks());
    int ey = Zones.safeTileY(performer.getTileY() + 3 + radiusBonus + performer.getNumLinks());
    
    calculateArea(sx, sy, ex, ey, tilex, tiley, layer, currstr);
    int damdealt = 3;
    int maxRiftPart = 5;
    Creature[] crets;
    for (int x = sx; x <= ex; x++) {
      for (int y = sy; y <= ey; y++)
      {
        boolean isValidTargetTile = false;
        if ((tilex == x) && (tiley == y))
        {
          isValidTargetTile = true;
        }
        else
        {
          int currAreaX = x - sx;
          int currAreaY = y - sy;
          if (this.area[currAreaX][currAreaY] == 0) {
            isValidTargetTile = true;
          }
        }
        if (isValidTargetTile)
        {
          VolaTile t = Zones.getTileOrNull(x, y, performer.isOnSurface());
          if (t != null)
          {
            crets = t.getCreatures();
            for (Creature lCret : crets) {
              if (!lCret.isInvulnerable()) {
                if (lCret.getAttitude(performer) == 2)
                {
                  t.sendAttachCreatureEffect(lCret, (byte)8, (byte)0, (byte)0, (byte)0, (byte)0);
                  
                  damdealt += 3;
                  
                  double damage = calculateDamage(lCret, power, 4000.0D, 40.0D);
                  if (!lCret.addWoundOfType(performer, (byte)9, 1, false, 1.0F, false, damage, 0.0F, 0.0F, false, true)) {
                    lCret.setTarget(performer.getWurmId(), false);
                  }
                }
              }
            }
          }
        }
      }
    }
    for (int x = sx; (x <= ex) && (damdealt > 0); x++) {
      for (int y = sy; (y <= ey) && (damdealt > 0); y++)
      {
        VolaTile t = Zones.getTileOrNull(x, y, performer.isOnSurface());
        if (t != null)
        {
          Creature[] crets = t.getCreatures();
          for (Creature lCret : crets) {
            if ((lCret.getAttitude(performer) == 1) || 
              ((lCret.getAttitude(performer) == 0) && (!lCret.isAggHuman())) || 
              (lCret.getKingdomId() == performer.getKingdomId())) {
              if ((lCret.getBody() != null) && (lCret.getBody().getWounds() != null))
              {
                Wounds tWounds = lCret.getBody().getWounds();
                
                double healingPool = 58950.0D;
                healingPool += 58950.0D * (power / 100.0D);
                if ((performer.getCultist() != null) && (performer.getCultist().healsFaster())) {
                  healingPool *= 2.0D;
                }
                double resistance = SpellResist.getSpellResistance(lCret, 249);
                healingPool *= resistance;
                
                int woundsHealed = 0;
                int maxWoundHeal = (int)(healingPool * 0.33D);
                Wound[] arrayOfWound1 = tWounds.getWounds();int m = arrayOfWound1.length;
                Wound w;
                for (Wound localWound1 = 0; localWound1 < m; localWound1++)
                {
                  w = arrayOfWound1[localWound1];
                  if ((woundsHealed >= 3) || (damdealt <= 0)) {
                    break;
                  }
                  if (w.getSeverity() >= maxWoundHeal)
                  {
                    healingPool -= maxWoundHeal;
                    SpellResist.addSpellResistance(lCret, 249, maxWoundHeal);
                    w.modifySeverity(-maxWoundHeal);
                    woundsHealed++;
                    damdealt--;
                  }
                }
                Object targetWound;
                while ((woundsHealed < 3) && (damdealt > 0) && (tWounds.getWounds().length > 0))
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
                  damdealt--;
                }
                if ((woundsHealed < 3) && (damdealt > 0) && (tWounds.getWounds().length > 0)) {
                  for (Wound w : tWounds.getWounds())
                  {
                    if ((woundsHealed >= 3) || (damdealt <= 0)) {
                      break;
                    }
                    if (w.getSeverity() <= maxWoundHeal)
                    {
                      SpellResist.addSpellResistance(lCret, 249, w.getSeverity());
                      w.heal();
                      woundsHealed++;
                      damdealt--;
                    }
                    else
                    {
                      SpellResist.addSpellResistance(lCret, getNumber(), maxWoundHeal);
                      w.modifySeverity(-maxWoundHeal);
                      woundsHealed++;
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\spells\ScornOfLibila.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */