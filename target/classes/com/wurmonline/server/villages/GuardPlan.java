package com.wurmonline.server.villages;

import com.wurmonline.server.Features.Feature;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Server;
import com.wurmonline.server.ServerEntry;
import com.wurmonline.server.Servers;
import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.CreatureTemplate;
import com.wurmonline.server.creatures.CreatureTemplateIds;
import com.wurmonline.server.creatures.NoSuchCreatureException;
import com.wurmonline.server.economy.Economy;
import com.wurmonline.server.economy.MonetaryConstants;
import com.wurmonline.server.economy.Shop;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.kingdom.Kingdoms;
import com.wurmonline.server.zones.NoSuchZoneException;
import com.wurmonline.server.zones.Zone;
import com.wurmonline.server.zones.Zones;
import java.util.LinkedList;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class GuardPlan
  implements CreatureTemplateIds, TimeConstants, MiscConstants, MonetaryConstants
{
  public static final int GUARD_PLAN_NONE = 0;
  public static final int GUARD_PLAN_LIGHT = 1;
  public static final int GUARD_PLAN_MEDIUM = 2;
  public static final int GUARD_PLAN_HEAVY = 3;
  final LinkedList<Creature> freeGuards = new LinkedList();
  private static final Logger logger = Logger.getLogger(GuardPlan.class.getName());
  public int type = 0;
  final int villageId;
  long lastChangedPlan;
  public long moneyLeft;
  private int siegeCount = 0;
  private int waveCounter = 0;
  private long lastSentWarning = 0L;
  private static final long polltime = 500000L;
  long lastDrained = 0L;
  float drainModifier = 0.0F;
  private static final float maxDrainModifier = 5.0F;
  private static final float drainCumulateFigure = 0.5F;
  private int upkeepCounter = 0;
  int hiredGuardNumber = 0;
  private static final int maxGuards = Servers.localServer.isChallengeOrEpicServer() ? 20 : 50;
  private static final long minMoneyDrained = 7500L;
  
  GuardPlan(int aType, int aVillageId)
  {
    this.type = aType;
    this.villageId = aVillageId;
    
    create();
  }
  
  GuardPlan(int aVillageId)
  {
    this.villageId = aVillageId;
    load();
  }
  
  final Village getVillage()
    throws NoSuchVillageException
  {
    return Villages.getVillage(this.villageId);
  }
  
  public final String getName()
  {
    if (this.type == 3) {
      return "Heavy";
    }
    if (this.type == 1) {
      return "Light";
    }
    if (this.type == 2) {
      return "Medium";
    }
    return "None";
  }
  
  public final long getTimeLeft()
  {
    try
    {
      if ((getVillage().isPermanent) || (!Servers.localServer.isUpkeep())) {
        return 29030400000L;
      }
    }
    catch (NoSuchVillageException nsv)
    {
      logger.log(Level.WARNING, this.villageId + ", " + nsv.getMessage(), nsv);
    }
    return (this.moneyLeft / Math.max(1.0D, calculateUpkeep(false)) * 500000.0D);
  }
  
  public double calculateUpkeep(boolean calculateFraction)
  {
    long monthlyCost = getMonthlyCost();
    
    double upkeep = monthlyCost * 2.0667989417989417E-4D;
    
    return upkeep;
  }
  
  public final long getMoneyLeft()
  {
    return this.moneyLeft;
  }
  
  public static final long getCostForGuards(int numGuards)
  {
    if (Servers.localServer.isChallengeOrEpicServer()) {
      return numGuards * 10000 + (numGuards - 1) * numGuards / 2 * 100 * 50;
    }
    return numGuards * Villages.GUARD_UPKEEP;
  }
  
  public final long getMonthlyCost()
  {
    if (!Servers.localServer.isUpkeep()) {
      return 0L;
    }
    try
    {
      Village vill = getVillage();
      
      long cost = vill.getNumTiles() * Villages.TILE_UPKEEP;
      cost += vill.getPerimeterNonFreeTiles() * Villages.PERIMETER_UPKEEP;
      cost += getCostForGuards(this.hiredGuardNumber);
      if (vill.isCapital()) {
        cost = ((float)cost * 0.5F);
      }
      if (vill.hasToomanyCitizens()) {
        cost *= 2L;
      }
      return Math.max(Villages.MINIMUM_UPKEEP, cost);
    }
    catch (NoSuchVillageException sv)
    {
      logger.log(Level.WARNING, "Guardplan for village " + this.villageId + ": Village not found. Deleting.", sv);
      delete();
    }
    return 10000L;
  }
  
  public final boolean mayRaiseUpkeep()
  {
    return System.currentTimeMillis() - this.lastChangedPlan > 604800000L;
  }
  
  public final boolean mayLowerUpkeep()
  {
    return true;
  }
  
  public final long calculateUpkeepTimeforType(int upkeeptype)
  {
    int origType = this.type;
    this.type = upkeeptype;
    long timeleft = getTimeLeft();
    this.type = origType;
    return timeleft;
  }
  
  public final long calculateMonthlyUpkeepTimeforType(int upkeeptype)
  {
    int origType = this.type;
    this.type = upkeeptype;
    long cost = getMonthlyCost();
    this.type = origType;
    return cost;
  }
  
  protected long getDisbandMoneyLeft()
  {
    return this.moneyLeft;
  }
  
  private void pollGuards()
  {
    if (this.type != 0) {
      try
      {
        Village village = getVillage();
        int _maxGuards = getConvertedGuardNumber(village);
        Guard[] guards = village.getGuards();
        if (guards.length < _maxGuards)
        {
          try
          {
            Item villToken = village.getToken();
            byte sex = 0;
            if (Server.rand.nextInt(2) == 0) {
              sex = 1;
            }
            int templateId = 32;
            if (Kingdoms.getKingdomTemplateFor(village.kingdom) == 3) {
              templateId = 33;
            }
            for (int x = 0; x < Math.min(this.siegeCount + 1, _maxGuards - guards.length); x++) {
              try
              {
                if (this.freeGuards.isEmpty())
                {
                  Creature newc = Creature.doNew(templateId, villToken.getPosX(), villToken.getPosY(), Server.rand
                    .nextInt(360), village.isOnSurface() ? 0 : -1, "", sex, village.kingdom);
                  
                  village.createGuard(newc, System.currentTimeMillis());
                }
                else
                {
                  Creature toReturn = (Creature)this.freeGuards.removeFirst();
                  if (toReturn.getTemplate().getTemplateId() != templateId)
                  {
                    removeReturnedGuard(toReturn.getWurmId());
                    toReturn.destroy();
                    Creature newc = Creature.doNew(templateId, villToken.getPosX(), villToken
                      .getPosY(), Server.rand.nextInt(360), village.isOnSurface() ? 0 : -1, "", sex, village.kingdom);
                    
                    village.createGuard(newc, System.currentTimeMillis());
                  }
                  else
                  {
                    village.createGuard(toReturn, System.currentTimeMillis());
                    removeReturnedGuard(toReturn.getWurmId());
                    putGuardInWorld(toReturn);
                  }
                }
              }
              catch (Exception ex)
              {
                logger.log(Level.WARNING, ex.getMessage(), ex);
              }
            }
          }
          catch (NoSuchItemException nsi)
          {
            logger.log(Level.WARNING, "Village " + village.getName() + " has no token.");
          }
          if (this.siegeCount > 0) {
            this.siegeCount += 3;
          }
        }
        village.checkForEnemies();
      }
      catch (NoSuchVillageException nsv)
      {
        logger.log(Level.WARNING, "No village for guardplan with villageid " + this.villageId, nsv);
      }
    } else {
      try
      {
        Village village = getVillage();
        
        Guard[] guards = village.getGuards();
        if (guards.length < this.hiredGuardNumber) {
          if ((this.hiredGuardNumber <= 10) || (guards.length <= 10) || (this.siegeCount == 0))
          {
            try
            {
              Item villToken = village.getToken();
              if (Features.Feature.TOWER_CHAINING.isEnabled()) {
                if (!villToken.isChained())
                {
                  this.waveCounter += 1;
                  if (this.waveCounter % 3 != 0) {
                    return;
                  }
                }
              }
              byte sex = 0;
              if (Server.rand.nextInt(2) == 0) {
                sex = 1;
              }
              int templateId = 32;
              if (village.kingdom == 3) {
                templateId = 33;
              }
              int minguards = Math.max(1, this.hiredGuardNumber / 10);
              for (int x = 0; x < Math.min(this.siegeCount + minguards, this.hiredGuardNumber - guards.length); x++) {
                try
                {
                  if (this.freeGuards.isEmpty())
                  {
                    Creature newc = Creature.doNew(templateId, villToken.getPosX(), villToken
                      .getPosY(), Server.rand.nextInt(360), village.isOnSurface() ? 0 : -1, "", sex, village.kingdom);
                    
                    village.createGuard(newc, System.currentTimeMillis());
                  }
                  else
                  {
                    Creature toReturn = (Creature)this.freeGuards.removeFirst();
                    if (toReturn.getTemplate().getTemplateId() != templateId)
                    {
                      removeReturnedGuard(toReturn.getWurmId());
                      toReturn.destroy();
                      Creature newc = Creature.doNew(templateId, villToken.getPosX(), villToken
                        .getPosY(), Server.rand.nextInt(360), village.isOnSurface() ? 0 : -1, "", sex, village.kingdom);
                      
                      village.createGuard(newc, System.currentTimeMillis());
                    }
                    else
                    {
                      village.createGuard(toReturn, System.currentTimeMillis());
                      removeReturnedGuard(toReturn.getWurmId());
                      putGuardInWorld(toReturn);
                    }
                  }
                }
                catch (Exception ex)
                {
                  logger.log(Level.WARNING, ex.getMessage(), ex);
                }
              }
            }
            catch (NoSuchItemException nsi)
            {
              logger.log(Level.WARNING, "Village " + village.getName() + " has no token.");
            }
            if (this.siegeCount > 0) {
              this.siegeCount += 3;
            }
          }
        }
        village.checkForEnemies();
      }
      catch (NoSuchVillageException nsv)
      {
        logger.log(Level.WARNING, "No village for guardplan with villageid " + this.villageId, nsv);
      }
    }
  }
  
  public void startSiege()
  {
    this.siegeCount = 1;
  }
  
  public boolean isUnderSiege()
  {
    return this.siegeCount > 0;
  }
  
  public int getSiegeCount()
  {
    return this.siegeCount;
  }
  
  private void putGuardInWorld(Creature guard)
  {
    try
    {
      Item token = getVillage().getToken();
      guard.setPositionX(token.getPosX());
      guard.setPositionY(token.getPosY());
      try
      {
        guard.setLayer(token.isOnSurface() ? 0 : -1, false);
        guard.setPositionZ(Zones.calculateHeight(guard.getPosX(), guard.getPosY(), token.isOnSurface()));
        
        guard.respawn();
        Zone zone = Zones.getZone(guard.getTileX(), guard.getTileY(), guard.isOnSurface());
        zone.addCreature(guard.getWurmId());
        guard.savePosition(zone.getId());
      }
      catch (NoSuchZoneException nsz)
      {
        logger.log(Level.WARNING, "Guard: " + guard.getWurmId() + ": " + nsz.getMessage(), nsz);
      }
      catch (NoSuchCreatureException nsc)
      {
        logger.log(Level.WARNING, "Guard: " + guard.getWurmId() + ": " + nsc.getMessage(), nsc);
        getVillage().deleteGuard(guard, false);
      }
      catch (NoSuchPlayerException nsp)
      {
        logger.log(Level.WARNING, "Guard: " + guard.getWurmId() + ": " + nsp.getMessage(), nsp);
      }
      catch (Exception ex)
      {
        logger.log(Level.WARNING, "Failed to return village guard: " + ex.getMessage(), ex);
      }
    }
    catch (NoSuchItemException nsi)
    {
      logger.log(Level.WARNING, nsi.getMessage(), nsi);
    }
    catch (NoSuchVillageException nsv)
    {
      logger.log(Level.WARNING, nsv.getMessage(), nsv);
    }
  }
  
  public final void returnGuard(Creature guard)
  {
    if (!this.freeGuards.contains(guard))
    {
      this.freeGuards.add(guard);
      addReturnedGuard(guard.getWurmId());
    }
  }
  
  private boolean pollUpkeep()
  {
    try
    {
      if (getVillage().isPermanent) {
        return false;
      }
    }
    catch (NoSuchVillageException localNoSuchVillageException1) {}
    if (!Servers.localServer.isUpkeep()) {
      return false;
    }
    long upkeep = calculateUpkeep(true);
    if (this.moneyLeft - upkeep <= 0L)
    {
      try
      {
        logger.log(Level.INFO, getVillage().getName() + " disbanding. Money left=" + this.moneyLeft + ", upkeep=" + upkeep);
      }
      catch (NoSuchVillageException nsv)
      {
        logger.log(Level.INFO, nsv.getMessage(), nsv);
      }
      return true;
    }
    if (upkeep >= 100L) {
      try
      {
        logger.log(Level.INFO, getVillage().getName() + " upkeep=" + upkeep);
      }
      catch (NoSuchVillageException nsv)
      {
        logger.log(Level.INFO, nsv.getMessage(), nsv);
      }
    }
    updateGuardPlan(this.type, this.moneyLeft - Math.max(1L, upkeep), this.hiredGuardNumber);
    this.upkeepCounter += 1;
    if (this.upkeepCounter == 2)
    {
      this.upkeepCounter = 0;
      Shop shop = Economy.getEconomy().getKingsShop();
      if (shop != null)
      {
        if (upkeep <= 1L) {
          shop.setMoney(shop.getMoney() + Math.max(1L, upkeep));
        } else {
          shop.setMoney(shop.getMoney() + upkeep);
        }
      }
      else {
        logger.log(Level.WARNING, "No shop when " + this.villageId + " paying upkeep.");
      }
    }
    long tl = getTimeLeft();
    if (tl < 3600000L) {
      try
      {
        getVillage().broadCastAlert("The village is disbanding within the hour. You may add upkeep money to the village coffers at the token immediately.", (byte)2);
        
        getVillage().broadCastAlert("Any traders who are citizens of " + getVillage().getName() + " will disband without refund.");
      }
      catch (NoSuchVillageException nsv)
      {
        logger.log(Level.WARNING, "No Village? " + this.villageId, nsv);
      }
    } else if (tl < 86400000L)
    {
      if (System.currentTimeMillis() - this.lastSentWarning > 3600000L)
      {
        this.lastSentWarning = System.currentTimeMillis();
        try
        {
          getVillage().broadCastAlert("The village is disbanding within 24 hours. You may add upkeep money to the village coffers at the token.", (byte)2);
          
          getVillage().broadCastAlert("Any traders who are citizens of " + getVillage().getName() + " will disband without refund.");
        }
        catch (NoSuchVillageException nsv)
        {
          logger.log(Level.WARNING, "No Village? " + this.villageId, nsv);
        }
      }
    }
    else if (tl < 604800000L) {
      if (System.currentTimeMillis() - this.lastSentWarning > 3600000L)
      {
        this.lastSentWarning = System.currentTimeMillis();
        try
        {
          getVillage().broadCastAlert("The village is disbanding within one week. Due to the low morale this gives, the guards have ceased their general maintenance of structures.", (byte)4);
          
          getVillage().broadCastAlert("Any traders who are citizens of " + getVillage().getName() + " will disband without refund.");
        }
        catch (NoSuchVillageException nsv)
        {
          logger.log(Level.WARNING, "No Village? " + this.villageId, nsv);
        }
      }
    }
    return false;
  }
  
  public final void destroyGuard(Creature guard)
  {
    this.freeGuards.remove(guard);
    removeReturnedGuard(guard.getWurmId());
  }
  
  final boolean poll()
  {
    pollGuards();
    if (this.siegeCount > 0)
    {
      this.siegeCount -= 1;
      
      this.siegeCount = Math.min(this.siegeCount, 9);
      try
      {
        if (!getVillage().isAlerted()) {
          this.siegeCount = Math.max(0, this.siegeCount - 1);
        }
      }
      catch (NoSuchVillageException nsv)
      {
        logger.log(Level.WARNING, nsv.getMessage());
      }
    }
    if ((this.drainModifier > 0.0F) && (System.currentTimeMillis() - this.lastDrained > 172800000L))
    {
      this.drainModifier = 0.0F;
      saveDrainMod();
    }
    return pollUpkeep();
  }
  
  public final long getLastDrained()
  {
    return this.lastDrained;
  }
  
  public static final int getMaxGuards(Village village)
  {
    return getMaxGuards(village.getDiameterX(), village.getDiameterY());
  }
  
  public static final int getMaxGuards(int diameterX, int diameterY)
  {
    return Math.min(maxGuards, Math.max(3, diameterX * diameterY / 49));
  }
  
  public final int getNumHiredGuards()
  {
    return this.hiredGuardNumber;
  }
  
  public final int getConvertedGuardNumber(Village village)
  {
    int max = getMaxGuards(village);
    if (this.type == 1) {
      max = Math.max(1, max / 4);
    }
    if (this.type == 2) {
      Math.max(1, max /= 2);
    }
    return max;
  }
  
  public final void changePlan(int newPlan, int newNumberOfGuards)
  {
    this.lastChangedPlan = System.currentTimeMillis();
    int changeInGuards = newNumberOfGuards - getNumHiredGuards();
    updateGuardPlan(newPlan, this.moneyLeft, newNumberOfGuards);
    if (changeInGuards < 0) {
      try
      {
        Village village = getVillage();
        int deleted = 0;
        
        changeInGuards = Math.abs(changeInGuards);
        if (this.freeGuards.size() > 0)
        {
          Creature[] crets = (Creature[])this.freeGuards.toArray(new Creature[this.freeGuards.size()]);
          for (int x = 0; x < Math.min(crets.length, changeInGuards); x++)
          {
            deleted++;
            removeReturnedGuard(crets[x].getWurmId());
            crets[x].destroy();
          }
        }
        if (deleted < changeInGuards)
        {
          Guard[] guards = village.getGuards();
          for (int x = 0; x < Math.min(guards.length, changeInGuards - deleted); x++) {
            if (guards[x].creature.isSpiritGuard()) {
              village.deleteGuard(guards[x].creature, true);
            }
          }
        }
      }
      catch (NoSuchVillageException nsv)
      {
        logger.log(Level.WARNING, "Village lacking for plan " + this.villageId, nsv);
      }
    }
  }
  
  public final void addMoney(long moneyAdded)
  {
    if (moneyAdded > 0L) {
      updateGuardPlan(this.type, this.moneyLeft + moneyAdded, this.hiredGuardNumber);
    }
  }
  
  public final long getTimeToNextDrain()
  {
    try
    {
      if (getVillage().isPermanent) {
        return 86400000L;
      }
    }
    catch (NoSuchVillageException nsv)
    {
      logger.log(Level.WARNING, this.villageId + ", " + nsv.getMessage(), nsv);
      return 86400000L;
    }
    return this.lastDrained + 86400000L - System.currentTimeMillis();
  }
  
  public final long getMoneyDrained()
  {
    try
    {
      if (getVillage().isPermanent) {
        return 0L;
      }
    }
    catch (NoSuchVillageException nsv)
    {
      logger.log(Level.WARNING, this.villageId + ", " + nsv.getMessage(), nsv);
      return 0L;
    }
    return Math.min((float)this.moneyLeft, (1.0F + this.drainModifier) * Math.max(7500.0F, (float)getMonthlyCost() * 0.15F));
  }
  
  public long drainMoney()
  {
    long moneyToDrain = getMoneyDrained();
    drainGuardPlan(this.moneyLeft - moneyToDrain);
    this.drainModifier = (0.5F + this.drainModifier);
    saveDrainMod();
    return moneyToDrain;
  }
  
  public final void fixGuards()
  {
    try
    {
      Guard[] gs = getVillage().getGuards();
      for (int x = 0; x < gs.length; x++) {
        if (gs[x].creature.isDead())
        {
          getVillage().deleteGuard(gs[x].creature, false);
          
          returnGuard(gs[x].creature);
          logger.log(Level.INFO, "Destroyed dead guard for " + getVillage().getName());
        }
      }
    }
    catch (NoSuchVillageException nsv)
    {
      logger.log(Level.WARNING, "Village lacking for plan " + this.villageId, nsv);
    }
  }
  
  public final float getProsperityModifier()
  {
    if (getMoneyLeft() > 1000000L) {
      return 1.05F;
    }
    return 1.0F;
  }
  
  public void updateGuardPlan(long aMoneyLeft)
  {
    updateGuardPlan(this.type, aMoneyLeft, this.hiredGuardNumber);
  }
  
  abstract void create();
  
  abstract void load();
  
  public abstract void updateGuardPlan(int paramInt1, long paramLong, int paramInt2);
  
  abstract void delete();
  
  abstract void addReturnedGuard(long paramLong);
  
  abstract void removeReturnedGuard(long paramLong);
  
  abstract void saveDrainMod();
  
  abstract void deleteReturnedGuards();
  
  public abstract void addPayment(String paramString, long paramLong1, long paramLong2);
  
  abstract void drainGuardPlan(long paramLong);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\villages\GuardPlan.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */