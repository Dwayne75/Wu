package com.wurmonline.server.epic;

import com.wurmonline.server.Point;
import com.wurmonline.shared.constants.ValreiConstants;
import java.util.Random;

public class ValreiFight
{
  private static final int MAP_SIZE = 7;
  private static final short MODIFIER_NORMAL = 0;
  private static final short MODIFIER_BLANK = -1;
  private static final Point test1 = new Point(0, 0);
  private static final Point test2 = new Point(0, 0);
  private final MapHex mapHex;
  private final ValreiFight.FightEntity fighter1;
  private final ValreiFight.FightEntity fighter2;
  private ValreiFightHistory fightHistory;
  private ValreiFight.ValreiFightHex[][] fightMap;
  private Random fightRand;
  
  public ValreiFight(MapHex mapHex, EpicEntity fighter1, EpicEntity fighter2)
  {
    this.mapHex = mapHex;
    this.fighter1 = new ValreiFight.FightEntity(this, fighter1);
    this.fighter2 = new ValreiFight.FightEntity(this, fighter2);
  }
  
  public ValreiFightHistory completeFight(boolean test)
  {
    this.fightHistory = new ValreiFightHistory(this.mapHex.getId(), this.mapHex.getName());
    this.fightHistory.addFighter(this.fighter1.getEntityId(), this.fighter1.getEntityName());
    this.fightHistory.addFighter(this.fighter2.getEntityId(), this.fighter2.getEntityName());
    if (test) {
      this.fightRand = new Random(System.nanoTime());
    } else {
      this.fightRand = new Random(this.fightHistory.getFightTime());
    }
    this.fightMap = createFightMap();
    
    moveEntity(this.fighter1, 1, 1);
    moveEntity(this.fighter2, 5, 5);
    
    this.fighter1.setMaxFavor(25.0F + 0.75F * this.fighter1.rollSkill(105, 106));
    this.fighter1.setMaxKarma(25.0F + 0.75F * this.fighter1.rollSkill(106, 100));
    
    this.fighter2.setMaxFavor(25.0F + 0.75F * this.fighter2.rollSkill(105, 106));
    this.fighter2.setMaxKarma(25.0F + 0.75F * this.fighter2.rollSkill(106, 100));
    
    ValreiFight.FightEntity currentFighter = this.fighter2;
    if (this.fighter1.rollInitiative() > this.fighter2.rollInitiative()) {
      currentFighter = this.fighter1;
    }
    while (!this.fightHistory.isFightCompleted())
    {
      if (takeTurn(currentFighter))
      {
        if ((this.fighter1.getHealth() <= 0.0F) && (this.fighter2.getHealth() > 0.0F)) {
          this.fightHistory.addAction((short)8, ValreiConstants.getEndFightData(this.fighter2.getEntityId()));
        } else if ((this.fighter2.getHealth() <= 0.0F) && (this.fighter1.getHealth() > 0.0F)) {
          this.fightHistory.addAction((short)8, ValreiConstants.getEndFightData(this.fighter1.getEntityId()));
        } else {
          this.fightHistory.addAction((short)8, ValreiConstants.getEndFightData(-1L));
        }
        this.fightHistory.setFightCompleted(true);
      }
      if (currentFighter == this.fighter2) {
        currentFighter = this.fighter1;
      } else {
        currentFighter = this.fighter2;
      }
    }
    if (!test) {
      this.fightHistory.saveActions();
    }
    return this.fightHistory;
  }
  
  private boolean takeTurn(ValreiFight.FightEntity e)
  {
    ValreiFight.FightEntity opponent = e == this.fighter1 ? this.fighter2 : this.fighter1;
    int actionCount = 2;
    boolean smartRound = e.rollSkill(100) > 0.0F;
    float spellRegen = e.rollSkill(100, 101);
    if (spellRegen > 0.0F)
    {
      float favorGone = e.getMaxKarma() - e.getFavor();
      float karmaGone = e.getMaxFavor() - e.getKarma();
      if (favorGone + karmaGone > 0.0F)
      {
        float favorPercent = favorGone / (favorGone + karmaGone);
        float karmaPercent = karmaGone / (favorGone + karmaGone);
        
        e.setFavor(Math.min(e.getMaxFavor(), e.getFavor() + spellRegen * favorPercent));
        e.setKarma(Math.min(e.getMaxKarma(), e.getKarma() + spellRegen * karmaPercent));
      }
    }
    while ((actionCount > 0) && (e.getHealth() > 0.0F) && (opponent.getHealth() > 0.0F))
    {
      boolean moveTowards = true;
      short currentAction = (short)(4 + this.fightRand.nextInt(4));
      int distance = e.getDistanceTo(opponent);
      if (smartRound)
      {
        short preferredAction = e.getPreferredAction();
        switch (preferredAction)
        {
        case 4: 
          if (distance > 1) {
            currentAction = 2;
          } else {
            currentAction = preferredAction;
          }
          break;
        case 5: 
          if (distance <= 2)
          {
            currentAction = 2;
            moveTowards = false;
          }
          else
          {
            currentAction = preferredAction;
          }
          break;
        }
      }
      if (((currentAction == 6) && (e.getFavor() < 20.0F)) || ((currentAction == 7) && 
        (e.getKarma() < 20.0F))) {
        if (distance > 2) {
          currentAction = 5;
        } else {
          currentAction = 4;
        }
      }
      if ((currentAction == 4) && (distance > 1))
      {
        currentAction = 2;
        moveTowards = true;
      }
      Point moveTarget = e.getTargetMove(moveTowards, opponent);
      if ((currentAction == 2) && (!isMoveValid(e, moveTarget.getX(), moveTarget.getY()))) {
        if (distance > 1) {
          currentAction = 5;
        } else {
          currentAction = 4;
        }
      }
      switch (currentAction)
      {
      case 2: 
        moveEntity(e, moveTarget.getX(), moveTarget.getY());
        actionCount--;
        break;
      case 4: 
      case 5: 
        attackEntity(e, opponent, currentAction);
        actionCount--;
        break;
      case 6: 
      case 7: 
        castSpell(e, opponent, currentAction);
        actionCount--;
      }
    }
    if ((e.getHealth() <= 0.0F) || (opponent.getHealth() <= 0.0F)) {
      return true;
    }
    return false;
  }
  
  private void moveEntity(ValreiFight.FightEntity e, int xPos, int yPos)
  {
    ValreiFight.FightEntity.access$002(e, xPos);
    ValreiFight.FightEntity.access$102(e, yPos);
    
    byte[] moveData = ValreiConstants.getMoveData(e.getEntityId(), xPos, yPos);
    this.fightHistory.addAction((short)2, moveData);
  }
  
  private void attackEntity(ValreiFight.FightEntity attacker, ValreiFight.FightEntity defender, short attackType)
  {
    float attackRoll = attackType == 4 ? attacker.rollSkill(102, 104, attacker.getAttackBuffed()) : attacker.rollSkill(104, 103, attacker.getAttackBuffed());
    float defendRoll = defender.rollSkill(103, 102, defender.getPhysDefBuffed());
    
    float damage = Math.min(attackRoll, attackRoll - defendRoll);
    if (attackRoll < 0.0F) {
      damage = -1.0F;
    } else if (defendRoll > attackRoll) {
      damage = 0.0F;
    }
    if (damage > 0.0F)
    {
      damage /= 3.0F;
      defender.setHealth(defender.getHealth() - damage);
    }
    byte[] attackData = ValreiConstants.getAttackData(attacker.getEntityId(), defender.getEntityId(), damage);
    this.fightHistory.addAction(attackType, attackData);
  }
  
  private void castSpell(ValreiFight.FightEntity caster, ValreiFight.FightEntity defender, short spellType)
  {
    float casterRoll = spellType == 6 ? caster.rollSkill(105, 106) : caster.rollSkill(106, 100);
    float defendRoll = defender.rollSkill(101, 105, defender.getSpellDefBuffed());
    
    byte s = 1;
    if (spellType == 6) {
      s = caster.getDeitySpell(defender);
    } else if (spellType == 7) {
      s = caster.getSorcerySpell(defender);
    }
    float damage = -100.0F;
    switch (s)
    {
    case 1: 
      casterRoll = spellType == 6 ? caster.rollSkill(105, 106, caster.getAttackBuffed()) : caster.rollSkill(106, 100, caster.getAttackBuffed());
      damage = Math.min(casterRoll, casterRoll - defendRoll);
      if (casterRoll < 0.0F) {
        damage = -1.0F;
      } else if (defendRoll > casterRoll) {
        damage = 0.0F;
      }
      if (damage > 0.0F)
      {
        damage /= 2.0F;
        defender.setHealth(defender.getHealth() - damage);
        if (spellType == 6) {
          caster.setFavor(caster.getFavor() - 20.0F);
        } else {
          caster.setKarma(caster.getKarma() - 20.0F);
        }
      }
      break;
    case 0: 
      damage = casterRoll;
      if (casterRoll < 0.0F) {
        damage = -1.0F;
      }
      if (damage > 0.0F)
      {
        damage /= 2.0F;
        caster.setHealth(Math.min(100.0F, caster.getHealth() + damage));
        
        caster.setFavor(caster.getFavor() - 30.0F);
      }
      break;
    case 4: 
      damage = casterRoll;
      if (casterRoll < 0.0F) {
        damage = -1.0F;
      }
      if (damage > 0.0F)
      {
        caster.setAttackBuffed(damage / 50.0F);
        caster.setFavor(caster.getFavor() - 50.0F);
      }
      break;
    case 2: 
      damage = casterRoll;
      if (casterRoll < 0.0F) {
        damage = -1.0F;
      }
      if (damage > 0.0F)
      {
        caster.setPhysDefBuffed(damage / 50.0F);
        caster.setKarma(caster.getKarma() - 60.0F);
      }
      break;
    case 3: 
      damage = casterRoll;
      if (casterRoll < 0.0F) {
        damage = -1.0F;
      }
      if (damage > 0.0F)
      {
        caster.setSpellDefBuffed(damage / 50.0F);
        caster.setKarma(caster.getKarma() - 60.0F);
      }
      break;
    }
    byte[] spellData = ValreiConstants.getSpellData(caster.getEntityId(), defender.getEntityId(), s, damage);
    this.fightHistory.addAction(spellType, spellData);
  }
  
  private ValreiFight.ValreiFightHex[][] createFightMap()
  {
    ValreiFight.ValreiFightHex[][] toReturn = new ValreiFight.ValreiFightHex[7][7];
    for (int i = 0; i < 7; i++) {
      for (int j = 0; j < 7; j++)
      {
        toReturn[i][j] = new ValreiFight.ValreiFightHex(this, i, j);
        if (j + 1 < 4)
        {
          if (i >= 4 + j) {
            toReturn[i][j].setModifier((short)-1);
          }
        }
        else if (j + 1 > 4) {
          if (i <= j - 7) {
            toReturn[i][j].setModifier((short)-1);
          }
        }
      }
    }
    return toReturn;
  }
  
  private final boolean isMoveValid(ValreiFight.FightEntity e, int mapX, int mapY)
  {
    if (this.fightMap == null) {
      return false;
    }
    if ((mapX < 0) || (mapY < 0) || (mapX >= 7) || (mapY >= 7)) {
      return false;
    }
    if (this.fightMap[mapX][mapY].getModifier() == -1) {
      return false;
    }
    ValreiFight.FightEntity opponent = e == this.fighter1 ? this.fighter2 : this.fighter1;
    if ((mapX == ValreiFight.FightEntity.access$000(opponent)) && (mapY == ValreiFight.FightEntity.access$100(opponent))) {
      return false;
    }
    if ((mapX == ValreiFight.FightEntity.access$000(e)) && (mapY == ValreiFight.FightEntity.access$100(e))) {
      return false;
    }
    return true;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\epic\ValreiFight.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */