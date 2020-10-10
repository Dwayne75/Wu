package com.wurmonline.server.players;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.HistoryManager;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Players;
import com.wurmonline.server.Server;
import com.wurmonline.server.ServerEntry;
import com.wurmonline.server.Servers;
import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.WurmCalendar;
import com.wurmonline.server.WurmId;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.CreatureTemplateCreator;
import com.wurmonline.server.creatures.CreatureTemplateIds;
import com.wurmonline.server.creatures.DbCreatureStatus;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemFactory;
import com.wurmonline.server.statistics.ChallengePointEnum.ChallengePoint;
import com.wurmonline.server.statistics.ChallengeSummary;
import com.wurmonline.server.tutorial.PlayerTutorial;
import com.wurmonline.server.utils.DbUtilities;
import com.wurmonline.shared.constants.CounterTypes;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class Achievements
  implements CounterTypes, MiscConstants, TimeConstants, CreatureTemplateIds
{
  private static final Logger logger = Logger.getLogger(Achievements.class.getName());
  private static final Map<Long, Achievements> achievements = new ConcurrentHashMap();
  private final Map<Integer, Achievement> achievementsMap = new ConcurrentHashMap();
  private final Set<AchievementTemplate> personalGoalsSet = new HashSet();
  private static final Achievement[] emptyArray = new Achievement[0];
  private static final String loadAllAchievements = "SELECT * FROM ACHIEVEMENTS";
  private static final String deleteAllAchievementsForPlayer = "DELETE FROM ACHIEVEMENTS WHERE PLAYER=?";
  private final long wurmId;
  
  public Achievements(long holderId)
  {
    this.wurmId = holderId;
  }
  
  public Achievements(long holderId, boolean createGoals)
  {
    this(holderId);
    if (createGoals) {
      generatePersonalGoals(holderId);
    }
  }
  
  private final long getWurmId()
  {
    return this.wurmId;
  }
  
  public static boolean hasAchievement(long wurmId, int achievementId)
  {
    Achievements ach = getAchievementObject(wurmId);
    if (ach == null) {
      return false;
    }
    if (ach.getAchievement(achievementId) == null) {
      return false;
    }
    return true;
  }
  
  public static final Set<AchievementTemplate> getOldPersonalGoals(long wurmId)
  {
    Set<AchievementTemplate> initialSet = new HashSet();
    
    initialSet.add(Achievement.getTemplate(141));
    
    initialSet.add(Achievement.getTemplate(237));
    
    initialSet.add(Achievement.getTemplate(171));
    
    initialSet.add(Achievement.getTemplate(70));
    
    initialSet.add(Achievement.getTemplate(57));
    
    Random rand = new Random(wurmId);
    while (initialSet.size() < 7)
    {
      AchievementTemplate originalAch = Achievement.getRandomPersonalDiamondAchievement(rand);
      initialSet.add(originalAch);
    }
    while (initialSet.size() < 9)
    {
      AchievementTemplate originalAch = Achievement.getRandomPersonalGoldAchievement(rand);
      initialSet.add(originalAch);
    }
    while (initialSet.size() < 20)
    {
      AchievementTemplate originalAch = Achievement.getRandomPersonalSilverAchievement(rand);
      initialSet.add(originalAch);
    }
    return initialSet;
  }
  
  private final void generatePersonalGoals(long playerId)
  {
    if (!canStillWinTheGame()) {
      return;
    }
    Set<AchievementTemplate> initialSet = new HashSet();
    Set<AchievementTemplate> completedAch = new HashSet();
    for (Achievement t : getAchievements(playerId)) {
      completedAch.add(t.getTemplate());
    }
    initialSet.add(Achievement.getTemplate(141));
    
    initialSet.add(Achievement.getTemplate(237));
    
    initialSet.add(Achievement.getTemplate(171));
    
    initialSet.add(Achievement.getTemplate(70));
    
    initialSet.add(Achievement.getTemplate(57));
    
    Random rand = new Random(playerId);
    while (initialSet.size() < 7)
    {
      AchievementTemplate originalAch = Achievement.getRandomPersonalDiamondAchievement(rand);
      initialSet.add(originalAch);
    }
    while (initialSet.size() < 9)
    {
      AchievementTemplate originalAch = Achievement.getRandomPersonalGoldAchievement(rand);
      initialSet.add(originalAch);
    }
    while (initialSet.size() < 20)
    {
      originalAch = Achievement.getRandomPersonalSilverAchievement(rand);
      initialSet.add(originalAch);
    }
    this.personalGoalsSet.clear();
    for (Object originalAch = initialSet.iterator(); ((Iterator)originalAch).hasNext();)
    {
      t = (AchievementTemplate)((Iterator)originalAch).next();
      
      int count = 1;
      if (completedAch.contains(t))
      {
        this.personalGoalsSet.add(t);
      }
      else if ((((AchievementTemplate)t).getNumber() >= 300) && (((AchievementTemplate)t).getType() == 5))
      {
        AchievementTemplate newAch = Achievement.getRandomPersonalDiamondAchievement(new Random(playerId + count++));
        while ((newAch.getNumber() >= 300) || (initialSet.contains(newAch)) || (this.personalGoalsSet.contains(newAch))) {
          newAch = Achievement.getRandomPersonalDiamondAchievement(new Random(playerId + count++));
        }
        this.personalGoalsSet.add(newAch);
      }
      else if (AchievementGenerator.isRerollablePersonalGoal(((AchievementTemplate)t).getNumber()))
      {
        if (((AchievementTemplate)t).getType() == 4)
        {
          AchievementTemplate newAch = Achievement.getRandomPersonalGoldAchievement(new Random(playerId + count++));
          while ((AchievementGenerator.isRerollablePersonalGoal(newAch.getNumber())) || 
            (initialSet.contains(newAch)) || (this.personalGoalsSet.contains(newAch))) {
            newAch = Achievement.getRandomPersonalGoldAchievement(new Random(playerId + count++));
          }
          this.personalGoalsSet.add(newAch);
        }
        else if (((AchievementTemplate)t).getType() == 3)
        {
          AchievementTemplate newAch = Achievement.getRandomPersonalSilverAchievement(new Random(playerId + count++));
          while ((AchievementGenerator.isRerollablePersonalGoal(newAch.getNumber())) || 
            (initialSet.contains(newAch)) || (this.personalGoalsSet.contains(newAch))) {
            newAch = Achievement.getRandomPersonalSilverAchievement(new Random(playerId + count++));
          }
          this.personalGoalsSet.add(newAch);
        }
      }
      else
      {
        this.personalGoalsSet.add(t);
      }
    }
    AchievementTemplate toRemove = null;
    for (Object t = this.personalGoalsSet.iterator(); ((Iterator)t).hasNext();)
    {
      AchievementTemplate t = (AchievementTemplate)((Iterator)t).next();
      if (t.getNumber() == 298) {
        if (!completedAch.contains(t)) {
          toRemove = t;
        }
      }
    }
    if (toRemove != null)
    {
      this.personalGoalsSet.remove(toRemove);
      this.personalGoalsSet.add(Achievement.getTemplate(486));
    }
    this.personalGoalsSet.add(Achievement.getTemplate(344));
  }
  
  private final void generatePersonalUndeadGoals()
  {
    this.personalGoalsSet.clear();
    this.personalGoalsSet.add(Achievement.getTemplate(338));
    this.personalGoalsSet.add(Achievement.getTemplate(340));
  }
  
  public Set<AchievementTemplate> getPersonalGoals()
  {
    return this.personalGoalsSet;
  }
  
  public final boolean isPersonalGoal(AchievementTemplate template)
  {
    if (canStillWinTheGame()) {
      return this.personalGoalsSet.contains(template);
    }
    return false;
  }
  
  public final boolean hasMetAllPersonalGoals()
  {
    if (!canStillWinTheGame()) {
      return false;
    }
    for (AchievementTemplate template : this.personalGoalsSet)
    {
      Achievement a = getAchievement(template.getNumber());
      if (a == null) {
        return false;
      }
    }
    return true;
  }
  
  public static void addAchievement(Achievement achievement, boolean createGoals)
  {
    Achievements personalAchieves = (Achievements)achievements.get(Long.valueOf(achievement.getHolder()));
    if (personalAchieves == null)
    {
      personalAchieves = new Achievements(achievement.getHolder(), createGoals);
      achievements.put(Long.valueOf(achievement.getHolder()), personalAchieves);
    }
    personalAchieves.achievementsMap.put(Integer.valueOf(achievement.getAchievement()), achievement);
  }
  
  public static Achievement[] getAchievements(long creatureId)
  {
    Achievements personalSet = (Achievements)achievements.get(Long.valueOf(creatureId));
    if ((personalSet == null) || (personalSet.achievementsMap.isEmpty())) {
      return emptyArray;
    }
    return (Achievement[])personalSet.achievementsMap.values().toArray(
      new Achievement[personalSet.achievementsMap.values().size()]);
  }
  
  public static Achievements getAchievementObject(long creatureId)
  {
    Achievements personalAchieves = (Achievements)achievements.get(Long.valueOf(creatureId));
    if (personalAchieves == null)
    {
      personalAchieves = new Achievements(creatureId, true);
      achievements.put(Long.valueOf(creatureId), personalAchieves);
    }
    return personalAchieves;
  }
  
  public static Set<AchievementTemplate> getPersonalGoals(long creatureId, boolean isUndead)
  {
    Achievements personalAchieves = (Achievements)achievements.get(Long.valueOf(creatureId));
    if (personalAchieves == null)
    {
      personalAchieves = new Achievements(creatureId, true);
      achievements.put(Long.valueOf(creatureId), personalAchieves);
    }
    if (isUndead)
    {
      if (personalAchieves.getPersonalGoals().size() > 2) {
        personalAchieves.generatePersonalUndeadGoals();
      }
      return personalAchieves.getPersonalGoals();
    }
    return personalAchieves.getPersonalGoals();
  }
  
  private static final void awardKarma(AchievementTemplate template, Creature creature)
  {
    switch (template.getType())
    {
    case 3: 
      creature.setKarma(creature.getKarma() + 100);
      creature.getCommunicator().sendSafeServerMessage("You have received 100 karma for '" + template
        .getRequirement() + "'.");
      break;
    case 4: 
      creature.setKarma(creature.getKarma() + 500);
      creature.getCommunicator().sendSafeServerMessage("You have received 500 karma for '" + template
        .getRequirement() + "'.");
      break;
    case 5: 
      creature.setKarma(creature.getKarma() + 1000);
      creature.getCommunicator().sendSafeServerMessage("You have received 1000 karma for '" + template
        .getRequirement() + "'.");
      break;
    }
  }
  
  public Achievement getAchievement(int achievement)
  {
    return (Achievement)this.achievementsMap.get(Integer.valueOf(achievement));
  }
  
  private final void setWinnerEffects(Creature p)
  {
    if (!canStillWinTheGame()) {
      return;
    }
    if (!p.hasFlag(6))
    {
      p.setFlag(6, true);
      p.achievement(326);
      try
      {
        int itemTemplateId = 795 + Server.rand.nextInt(16);
        if (Server.rand.nextInt(100) == 0) {
          itemTemplateId = 465;
        }
        Item i = ItemFactory.createItem(itemTemplateId, 80 + Server.rand.nextInt(20), "");
        if (i.getTemplateId() == 465) {
          i.setData1(CreatureTemplateCreator.getRandomDragonOrDrakeId());
        }
        p.getInventory().insertItem(i);
        p.addTitle(Titles.Title.Winner);
        
        HistoryManager.addHistory(p.getName(), "has Won The Game and receives the " + i.getName() + "!");
      }
      catch (Exception nsi)
      {
        logger.log(Level.WARNING, p.getName() + " " + nsi.getMessage(), nsi);
      }
    }
  }
  
  private final void setWinnerEffectsOffline(PlayerInfo pInf)
  {
    if (!pInf.isFlagSet(6))
    {
      pInf.setFlag(6, true);
      triggerAchievement(pInf.wurmId, 326);
      try
      {
        int itemTemplateId = 795 + Server.rand.nextInt(16);
        if (Server.rand.nextInt(100) == 0) {
          itemTemplateId = 465;
        }
        Item i = ItemFactory.createItem(itemTemplateId, 80 + Server.rand.nextInt(20), "");
        if (i.getTemplateId() == 465) {
          i.setData1(CreatureTemplateCreator.getRandomDragonOrDrakeId());
        }
        long inventory = DbCreatureStatus.getInventoryIdFor(pInf.wurmId);
        i.setParentId(inventory, true);
        i.setOwnerId(pInf.wurmId);
        
        pInf.addTitle(Titles.Title.Winner);
        
        HistoryManager.addHistory(pInf.getName(), "has Won The Game and receives the " + i.getName() + "!");
      }
      catch (Exception nsi)
      {
        logger.log(Level.WARNING, pInf.getName() + " " + nsi.getMessage(), nsi);
      }
    }
  }
  
  public static void triggerAchievement(long creatureId, int achievementId, int counterModifier)
  {
    if (WurmId.getType(creatureId) != 0) {
      return;
    }
    Achievements personalAchieves = (Achievements)achievements.get(Long.valueOf(creatureId));
    if (personalAchieves == null)
    {
      personalAchieves = new Achievements(creatureId, true);
      achievements.put(Long.valueOf(creatureId), personalAchieves);
    }
    Achievement achieved = personalAchieves.getAchievement(achievementId);
    if (achieved == null)
    {
      achieved = new Achievement(achievementId, new Timestamp(System.currentTimeMillis()), creatureId, counterModifier, -1);
      
      PlayerJournal.achievementTriggered(creatureId, achievementId);
      
      achieved.create();
      if (!achieved.isInVisible()) {
        try
        {
          Player p = Players.getInstance().getPlayer(creatureId);
          achieved.sendNewAchievement(p);
          if (achievementId == 369) {
            p.addTitle(Titles.Title.Knigt);
          }
          if (achievementId == 367)
          {
            PlayerInfo pinf = PlayerInfoFactory.getPlayerInfoWithWurmId(creatureId);
            if (pinf != null)
            {
              ChallengeSummary.addToScore(pinf, ChallengePointEnum.ChallengePoint.TREASURE_CHESTS.getEnumtype(), 1.0F);
              ChallengeSummary.addToScore(pinf, ChallengePointEnum.ChallengePoint.OVERALL.getEnumtype(), 5.0F);
            }
          }
          if (personalAchieves.isPersonalGoal(achieved.getTemplate()))
          {
            achieved.sendUpdatePersonalGoal(p);
            awardKarma(achieved.getTemplate(), p);
            if (canStillWinTheGame()) {
              if (personalAchieves.hasMetAllPersonalGoals()) {
                personalAchieves.setWinnerEffects(p);
              }
            }
          }
        }
        catch (NoSuchPlayerException nsc)
        {
          PlayerInfo pInf = PlayerInfoFactory.getPlayerInfoWithWurmId(creatureId);
          if (pInf != null)
          {
            if (achievementId == 369) {
              pInf.addTitle(Titles.Title.Knigt);
            }
            if (achievementId == 367)
            {
              ChallengeSummary.addToScore(pInf, ChallengePointEnum.ChallengePoint.TREASURE_CHESTS.getEnumtype(), 1.0F);
              ChallengeSummary.addToScore(pInf, ChallengePointEnum.ChallengePoint.OVERALL.getEnumtype(), 5.0F);
            }
            if (personalAchieves.isPersonalGoal(achieved.getTemplate()))
            {
              switch (achieved.getTemplate().getType())
              {
              case 3: 
                pInf.setKarma(pInf.getKarma() + 100);
                break;
              case 4: 
                pInf.setKarma(pInf.getKarma() + 500);
                break;
              case 5: 
                pInf.setKarma(pInf.getKarma() + 1000);
                break;
              }
              if (canStillWinTheGame()) {
                if (personalAchieves.hasMetAllPersonalGoals()) {
                  personalAchieves.setWinnerEffectsOffline(pInf);
                }
              }
            }
          }
        }
      }
      triggerAchievements(creatureId, achieved, achievementId, personalAchieves, achieved.getTriggeredAchievements());
    }
    else if (!achieved.isOneTimer())
    {
      int[] triggered = achieved.setCounter(achieved.getCounter() + counterModifier);
      if (!achieved.isInVisible()) {
        try
        {
          Player p = Players.getInstance().getPlayer(creatureId);
          achieved.sendUpdateAchievement(p);
          if (achievementId == 369) {
            p.addTitle(Titles.Title.Knigt);
          }
          if (achievementId == 367)
          {
            PlayerInfo pinf = PlayerInfoFactory.getPlayerInfoWithWurmId(creatureId);
            if (pinf != null)
            {
              ChallengeSummary.addToScore(pinf, ChallengePointEnum.ChallengePoint.TREASURE_CHESTS.getEnumtype(), 1.0F);
              ChallengeSummary.addToScore(pinf, ChallengePointEnum.ChallengePoint.OVERALL.getEnumtype(), 5.0F);
            }
          }
        }
        catch (NoSuchPlayerException nsc)
        {
          PlayerInfo pInf = PlayerInfoFactory.getPlayerInfoWithWurmId(creatureId);
          if (pInf != null)
          {
            if (achievementId == 369) {
              pInf.addTitle(Titles.Title.Knigt);
            }
            if (achievementId == 367)
            {
              ChallengeSummary.addToScore(pInf, ChallengePointEnum.ChallengePoint.TREASURE_CHESTS.getEnumtype(), 1.0F);
              ChallengeSummary.addToScore(pInf, ChallengePointEnum.ChallengePoint.OVERALL.getEnumtype(), 5.0F);
            }
          }
        }
      }
      triggerAchievements(creatureId, achieved, achievementId, personalAchieves, triggered);
    }
  }
  
  public static void triggerAchievement(long creatureId, int achievementId)
  {
    if (WurmId.getType(creatureId) != 0) {
      return;
    }
    triggerAchievement(creatureId, achievementId, 1);
  }
  
  public static void sendAchievementList(Creature creature)
  {
    Achievement[] lAchievements = getAchievements(creature.getWurmId());
    creature.getCommunicator().sendAchievementList(lAchievements);
    sendPersonalGoalsList(creature);
    if (creature.isPlayer())
    {
      PlayerTutorial.sendTutorialList((Player)creature);
      PlayerJournal.sendPersonalJournal((Player)creature);
    }
  }
  
  private static void awardPremiumAchievements(Creature creature, int totalMonths)
  {
    ArrayList<Integer> achievementsList = new ArrayList();
    for (Achievement a : getAchievements(creature.getWurmId())) {
      achievementsList.add(Integer.valueOf(a.getAchievement()));
    }
    if ((totalMonths >= 1) && (!achievementsList.contains(Integer.valueOf(343)))) {
      creature.achievement(343);
    }
    if ((totalMonths >= 3) && (!achievementsList.contains(Integer.valueOf(344)))) {
      creature.achievement(344);
    }
    if ((totalMonths >= 6) && (!achievementsList.contains(Integer.valueOf(345)))) {
      creature.achievement(345);
    }
    if ((totalMonths >= 9) && (!achievementsList.contains(Integer.valueOf(346)))) {
      creature.achievement(346);
    }
    if ((totalMonths >= 13) && (!achievementsList.contains(Integer.valueOf(347)))) {
      creature.achievement(347);
    }
    if ((totalMonths >= 16) && (!achievementsList.contains(Integer.valueOf(348)))) {
      creature.achievement(348);
    }
    if ((totalMonths >= 20) && (!achievementsList.contains(Integer.valueOf(349)))) {
      creature.achievement(349);
    }
    if ((totalMonths >= 26) && (!achievementsList.contains(Integer.valueOf(350)))) {
      creature.achievement(350);
    }
    if ((totalMonths >= 36) && (!achievementsList.contains(Integer.valueOf(351)))) {
      creature.achievement(351);
    }
    if ((totalMonths >= 48) && (!achievementsList.contains(Integer.valueOf(352)))) {
      creature.achievement(352);
    }
    if ((totalMonths >= 60) && (!achievementsList.contains(Integer.valueOf(353)))) {
      creature.achievement(353);
    }
    if ((totalMonths >= 80) && (!achievementsList.contains(Integer.valueOf(354)))) {
      creature.achievement(354);
    }
    if ((totalMonths >= 120) && (!achievementsList.contains(Integer.valueOf(355)))) {
      creature.achievement(355);
    }
    creature.setFlag(61, true);
  }
  
  public static void sendPersonalGoalsList(Creature creature)
  {
    if (!canStillWinTheGame()) {
      return;
    }
    Achievements pachievements = getAchievementObject(creature.getWurmId());
    
    pachievements.generatePersonalGoals(creature.getWurmId());
    
    Set<AchievementTemplate> pset = getPersonalGoals(creature.getWurmId(), creature.isUndead());
    Map<AchievementTemplate, Boolean> goals = new ConcurrentHashMap();
    boolean awardTut = false;
    for (AchievementTemplate template : pset)
    {
      Achievement a = pachievements.getAchievement(template.getNumber());
      goals.put(template, Boolean.valueOf(a != null));
      if (a != null)
      {
        if (!creature.hasFlag(5)) {
          awardKarma(template, creature);
        }
      }
      else if (template.getNumber() == 141) {
        if (!Servers.localServer.LOGINSERVER) {
          awardTut = true;
        }
      }
    }
    if (awardTut) {
      creature.achievement(141);
    }
    if ((creature.isPlayer()) && (!creature.hasFlag(61)))
    {
      PlayerInfo player = PlayerInfoFactory.getPlayerInfoWithWurmId(creature.getWurmId());
      if ((player != null) && (player.awards != null)) {
        awardPremiumAchievements(creature, player.awards.getMonthsPaidSinceReset());
      }
    }
    if ((!creature.hasFlag(6)) && (pachievements.hasMetAllPersonalGoals())) {
      if (canStillWinTheGame()) {
        pachievements.setWinnerEffects(creature);
      }
    }
    if (!creature.hasFlag(5)) {
      creature.setFlag(5, true);
    }
    creature.getCommunicator().sendPersonalGoalsList(goals);
    if ((creature.getPlayingTime() > 7200000L) && (creature.getPlayingTime() < 21600000L)) {
      creature.getCommunicator().sendShowPersonalGoalWindow(true);
    }
  }
  
  private static void triggerAchievements(long creatureId, Achievement achieved, int achievementId, Achievements personalAchieves, int[] triggered)
  {
    for (int number : triggered) {
      if (number != achievementId)
      {
        Achievement old = personalAchieves.getAchievement(number);
        if (old != null)
        {
          if (!old.isOneTimer())
          {
            int required = old.getTriggerOnCounter() * (old.getCounter() + 1);
            if (achieved.getCounter() >= required)
            {
              int numTimes = achieved.getCounter() / old.getTriggerOnCounter() - old.getCounter();
              if (numTimes > 0) {
                triggerAchievement(creatureId, old.getAchievement(), numTimes);
              }
            }
          }
        }
        else
        {
          AchievementTemplate template = Achievement.getTemplate(number);
          if (template.getTriggerOnCounter() == 1) {
            if (template.getRequiredAchievements().length <= 1) {
              logger.log(Level.WARNING, "Achievement " + number + " has trigger on 1. Usually not good unless it's a meta achievement since it means the triggering achievement immediately gives another achievement.");
            }
          }
          if (((template.getTriggerOnCounter() > 0) && (achieved.getCounter() >= template.getTriggerOnCounter())) || (
            (template.getTriggerOnCounter() < 0) && 
            (achieved.getCounter() <= template.getTriggerOnCounter())))
          {
            boolean trigger = true;
            int[] required = template.getRequiredAchievements();
            for (int req : required) {
              if (req != achievementId)
              {
                Achievement existingReq = personalAchieves.getAchievement(req);
                if ((existingReq == null) || (existingReq.getCounter() < template.getTriggerOnCounter())) {
                  trigger = false;
                }
              }
            }
            int numTimes = achieved.getCounter() / template.getTriggerOnCounter();
            if ((trigger) && (numTimes > 0)) {
              triggerAchievement(creatureId, template.getNumber(), numTimes);
            }
          }
        }
      }
      else
      {
        logger.log(Level.WARNING, "Achievement " + achievementId + " has itself as trigger: " + number);
      }
    }
  }
  
  public static void loadAllAchievements()
    throws IOException
  {
    long start = System.nanoTime();
    int loadedAchievements = 0;
    Connection dbcon = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    try
    {
      dbcon = DbConnector.getPlayerDbCon();
      ps = dbcon.prepareStatement("SELECT * FROM ACHIEVEMENTS");
      rs = ps.executeQuery();
      while (rs.next())
      {
        Timestamp st = new Timestamp(System.currentTimeMillis());
        try
        {
          st = rs.getTimestamp("ADATE");
        }
        catch (Exception ex)
        {
          logger.log(Level.WARNING, ex.getMessage(), ex);
        }
        addAchievement(new Achievement(rs.getInt("ACHIEVEMENT"), st, rs.getLong("PLAYER"), rs
          .getInt("COUNTER"), rs.getInt("ID")), false);
        loadedAchievements++;
      }
    }
    catch (SQLException sqex)
    {
      long end;
      logger.log(Level.WARNING, "Failed to load achievements due to " + sqex.getMessage(), sqex);
      throw new IOException("Failed to load achievements", sqex);
    }
    finally
    {
      DbUtilities.closeDatabaseObjects(ps, rs);
      DbConnector.returnConnection(dbcon);
      long end = System.nanoTime();
      logger.info("Loaded " + loadedAchievements + " achievements from the database took " + (float)(end - start) / 1000000.0F + " ms");
    }
    if (canStillWinTheGame()) {
      generateAllPersonalGoals();
    }
  }
  
  private static void generateAllPersonalGoals()
  {
    long start = System.nanoTime();
    int count = 0;
    for (Achievements a : achievements.values())
    {
      a.generatePersonalGoals(a.getWurmId());
      count++;
    }
    long end = System.nanoTime();
    logger.info("Generated " + count + " personal goals, took " + (float)(end - start) / 1000000.0F + " ms");
  }
  
  public static void deleteAllAchievements(long playerId)
    throws IOException
  {
    Connection dbcon = null;
    PreparedStatement ps = null;
    try
    {
      dbcon = DbConnector.getPlayerDbCon();
      ps = dbcon.prepareStatement("DELETE FROM ACHIEVEMENTS WHERE PLAYER=?");
      ps.setLong(1, playerId);
      ps.executeUpdate();
    }
    catch (SQLException sqex)
    {
      logger.log(Level.WARNING, "Failed to delete achievements for " + playerId + ' ' + sqex.getMessage(), sqex);
      throw new IOException("Failed to delete achievements for " + playerId, sqex);
    }
    finally
    {
      DbUtilities.closeDatabaseObjects(ps, null);
      DbConnector.returnConnection(dbcon);
    }
  }
  
  public static boolean canStillWinTheGame()
  {
    return WurmCalendar.nowIsBefore(0, 1, 1, 1, 2019);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\players\Achievements.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */