package com.wurmonline.server.spells;

import com.wurmonline.server.Server;
import com.wurmonline.server.bodys.Body;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.CreatureStatus;
import com.wurmonline.server.deities.DbRitual;
import com.wurmonline.server.deities.Deities;
import com.wurmonline.server.deities.Deity;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.players.PlayerInfo;
import com.wurmonline.server.skills.Skill;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.logging.Logger;

public class RiteEvent
{
  protected static Logger logger = Logger.getLogger(RiteEvent.class.getName());
  protected static final int MAXIMUM_RITES_CAST = Integer.MAX_VALUE;
  protected static HashMap<Integer, RiteEvent> riteEvents = new HashMap();
  protected static int lastRiteId = 0;
  public static int lastClaimId = -1;
  protected ArrayList<Long> claimedReward = new ArrayList();
  protected int id;
  protected long casterId;
  protected int spellId;
  protected int deityNum;
  protected int templateDeity;
  protected long castTime;
  protected long duration;
  protected long expiration;
  
  public RiteEvent(int id, long casterId, int spellId, int deityNum, long castTime, long duration)
  {
    this.casterId = casterId;
    this.spellId = spellId;
    this.deityNum = deityNum;
    Deity baseDeity = Deities.getDeity(deityNum);
    if (baseDeity != null)
    {
      this.templateDeity = baseDeity.getTemplateDeity();
    }
    else
    {
      logger.warning(String.format("No template deity found for deity with ID %d when creating a RiteEvent.", new Object[] { Integer.valueOf(deityNum) }));
      this.templateDeity = deityNum;
    }
    this.castTime = castTime;
    this.duration = duration;
    this.expiration = (castTime + duration);
    if (id < 0)
    {
      for (int i = lastRiteId; i < Integer.MAX_VALUE; i++)
      {
        RiteEvent result = (RiteEvent)riteEvents.putIfAbsent(Integer.valueOf(i), this);
        if (result == null)
        {
          this.id = i;
          lastRiteId = i;
          break;
        }
      }
      DbRitual.createRiteEvent(this);
    }
    else
    {
      this.id = id;
      riteEvents.put(Integer.valueOf(id), this);
      if (id > lastRiteId) {
        lastRiteId = id;
      }
    }
  }
  
  public int getId()
  {
    return this.id;
  }
  
  public long getCasterId()
  {
    return this.casterId;
  }
  
  public int getSpellId()
  {
    return this.spellId;
  }
  
  public int getDeityNum()
  {
    return this.deityNum;
  }
  
  public long getCastTime()
  {
    return this.castTime;
  }
  
  public long getDuration()
  {
    return this.duration;
  }
  
  public static void addRitualClaim(int id, long playerId, int ritualCastsId, long claimTime)
  {
    RiteEvent event = (RiteEvent)riteEvents.get(Integer.valueOf(ritualCastsId));
    if (event == null)
    {
      logger.warning(String.format("Could not load Ritual Claim for player %d because RiteEvent %d does not exist.", new Object[] {
        Long.valueOf(playerId), Integer.valueOf(ritualCastsId) }));
      return;
    }
    event.claimedReward.add(Long.valueOf(playerId));
    if (lastClaimId < id) {
      lastClaimId = id;
    }
  }
  
  public static class RiteOfSpringEvent
    extends RiteEvent
  {
    public RiteOfSpringEvent(int id, long casterId, int spellId, int deityNum, long castTime, long duration)
    {
      super(casterId, spellId, deityNum, castTime, duration);
    }
    
    public boolean claimRiteReward(Player player)
    {
      if (!super.claimRiteReward(player)) {
        return false;
      }
      player.getCommunicator().sendSafeServerMessage("You feel enlightened!", (byte)2);
      awardBasicBonuses(player, player.getMindLogical());
      return true;
    }
  }
  
  public static class RiteOfTheSunEvent
    extends RiteEvent
  {
    public RiteOfTheSunEvent(int id, long casterId, int spellId, int deityNum, long castTime, long duration)
    {
      super(casterId, spellId, deityNum, castTime, duration);
    }
    
    public boolean claimRiteReward(Player player)
    {
      if (!super.claimRiteReward(player)) {
        return false;
      }
      player.getCommunicator().sendSafeServerMessage("You feel a sudden surge of energy!", (byte)2);
      awardBasicBonuses(player, player.getStaminaSkill());
      player.getBody().healFully();
      float nut = (80 + Server.rand.nextInt(19)) / 100.0F;
      player.getStatus().refresh(nut, false);
      return true;
    }
  }
  
  public static class RiteOfCropEvent
    extends RiteEvent
  {
    public RiteOfCropEvent(int id, long casterId, int spellId, int deityNum, long castTime, long duration)
    {
      super(casterId, spellId, deityNum, castTime, duration);
    }
    
    public boolean claimRiteReward(Player player)
    {
      if (!super.claimRiteReward(player)) {
        return false;
      }
      player.getCommunicator().sendSafeServerMessage("You feel a wave of warmth!", (byte)2);
      awardBasicBonuses(player, player.getSoulDepth());
      return true;
    }
  }
  
  public static class RiteOfDeathEvent
    extends RiteEvent
  {
    public RiteOfDeathEvent(int id, long casterId, int spellId, int deityNum, long castTime, long duration)
    {
      super(casterId, spellId, deityNum, castTime, duration);
    }
    
    public boolean claimRiteReward(Player player)
    {
      if (!super.claimRiteReward(player)) {
        return false;
      }
      player.getCommunicator().sendSafeServerMessage("You feel a sudden surge of power!", (byte)2);
      awardBasicBonuses(player, player.getSoulStrength());
      return true;
    }
  }
  
  public static void createGenericRiteEvent(int id, long casterId, int spellId, int deityNum, long castTime, long duration)
  {
    switch (spellId)
    {
    case 403: 
      new RiteOfSpringEvent(id, casterId, spellId, deityNum, castTime, duration);
      break;
    case 402: 
      new RiteOfDeathEvent(id, casterId, spellId, deityNum, castTime, duration);
      break;
    case 401: 
      new RiteOfTheSunEvent(id, casterId, spellId, deityNum, castTime, duration);
      break;
    case 400: 
      new RiteOfCropEvent(id, casterId, spellId, deityNum, castTime, duration);
    }
  }
  
  public static boolean isActive(int spellid)
  {
    for (RiteEvent event : riteEvents.values()) {
      if (((spellid != 400) || ((event instanceof RiteOfCropEvent))) && 
      
        ((spellid != 403) || ((event instanceof RiteOfSpringEvent))) && 
        
        ((spellid != 402) || ((event instanceof RiteOfDeathEvent))) && (
        
        (spellid != 401) || ((event instanceof RiteOfTheSunEvent)))) {
        if (event.expiration > System.currentTimeMillis()) {
          return true;
        }
      }
    }
    return false;
  }
  
  protected void awardBasicBonuses(Player player, Skill skill)
  {
    double currentKnowledge = skill.getKnowledge();
    double bonus = (100.0D - currentKnowledge) * 0.002D;
    skill.setKnowledge(currentKnowledge + bonus, false);
    player.getSaveFile().addToSleep(18000);
  }
  
  public boolean claimRiteReward(Player player)
  {
    if (player.getDeity().getTemplateDeity() != this.templateDeity) {
      return false;
    }
    if (this.expiration < System.currentTimeMillis()) {
      return false;
    }
    if (this.claimedReward.contains(Long.valueOf(player.getWurmId()))) {
      return false;
    }
    this.claimedReward.add(Long.valueOf(player.getWurmId()));
    lastClaimId += 1;
    DbRitual.createRiteClaim(lastClaimId, player.getWurmId(), this.id, System.currentTimeMillis());
    return true;
  }
  
  public static void checkRiteRewards(Player player)
  {
    for (RiteEvent event : riteEvents.values()) {
      event.claimRiteReward(player);
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\spells\RiteEvent.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */