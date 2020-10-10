package com.wurmonline.server.players;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.utils.DbUtilities;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;

public final class Achievement
  implements MiscConstants
{
  private final Timestamp dateAchieved;
  private final int achievement;
  private int counter = 0;
  private long holder = 0L;
  private int localId = -1;
  private static final String INSERT = DbConnector.isUseSqlite() ? "INSERT OR IGNORE INTO ACHIEVEMENTS (PLAYER,ACHIEVEMENT,COUNTER) VALUES (?,?,?)" : "INSERT IGNORE INTO ACHIEVEMENTS (PLAYER,ACHIEVEMENT,COUNTER) VALUES (?,?,?)";
  private static final String INSERT_TRANSFER = DbConnector.isUseSqlite() ? "INSERT OR IGNORE INTO ACHIEVEMENTS (PLAYER,ACHIEVEMENT,COUNTER,ADATE) VALUES (?,?,?,?)" : "INSERT IGNORE INTO ACHIEVEMENTS (PLAYER,ACHIEVEMENT,COUNTER,ADATE) VALUES (?,?,?,?)";
  private static final String UPDATE_COUNTER = "UPDATE ACHIEVEMENTS SET COUNTER=? WHERE ID=?";
  private static final Logger logger = Logger.getLogger(Achievement.class.getName());
  private static final ConcurrentHashMap<Integer, AchievementTemplate> templates = new ConcurrentHashMap();
  protected static final List<AchievementTemplate> personalGoalSilverTemplates = new LinkedList();
  protected static final List<AchievementTemplate> personalGoalGoldTemplates = new LinkedList();
  protected static final List<AchievementTemplate> personalGoalDiamondTemplates = new LinkedList();
  
  public Achievement(int aAchievement, Timestamp date, long achiever, int timesTriggered, int localid)
  {
    this.achievement = aAchievement;
    setHolder(achiever);
    this.dateAchieved = date;
    this.counter = timesTriggered;
    this.localId = localid;
    if (getTemplate(aAchievement) == null) {
      addTemplate(new AchievementTemplate(aAchievement, "Unknown", false));
    }
  }
  
  public final Timestamp getDateAchieved()
  {
    return this.dateAchieved;
  }
  
  public final int getAchievement()
  {
    return this.achievement;
  }
  
  public final int getCounter()
  {
    return this.counter;
  }
  
  final int[] setCounter(int aCounter)
  {
    if (this.counter != aCounter)
    {
      this.counter = aCounter;
      Connection dbcon = null;
      PreparedStatement ps = null;
      try
      {
        dbcon = DbConnector.getPlayerDbCon();
        ps = dbcon.prepareStatement("UPDATE ACHIEVEMENTS SET COUNTER=? WHERE ID=?");
        ps.setInt(1, this.counter);
        ps.setInt(2, this.localId);
        ps.executeUpdate();
      }
      catch (SQLException ex)
      {
        logger.log(Level.WARNING, "Failed to save achievement " + this.achievement + " counter " + aCounter + " for " + this.holder);
      }
      finally
      {
        DbUtilities.closeDatabaseObjects(ps, null);
        DbConnector.returnConnection(dbcon);
      }
      return getTriggeredAchievements();
    }
    return EMPTY_INT_ARRAY;
  }
  
  final int[] getTriggeredAchievements()
  {
    return getTemplate().getAchievementsTriggered();
  }
  
  public final AchievementTemplate getTemplate()
  {
    return getTemplate(this.achievement);
  }
  
  public final long getHolder()
  {
    return this.holder;
  }
  
  private final void setHolder(long aHolder)
  {
    this.holder = aHolder;
  }
  
  public final void create()
  {
    Connection dbcon = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    try
    {
      dbcon = DbConnector.getPlayerDbCon();
      ps = dbcon.prepareStatement(INSERT_TRANSFER, 1);
      ps.setLong(1, this.holder);
      ps.setInt(2, this.achievement);
      ps.setInt(3, this.counter);
      ps.setTimestamp(4, getDateAchieved());
      ps.executeUpdate();
      rs = ps.getGeneratedKeys();
      if (rs.next()) {
        this.localId = rs.getInt(1);
      }
    }
    catch (SQLException ex)
    {
      logger.log(Level.WARNING, "Failed to save achievement " + this.achievement + " for " + this.holder, ex);
    }
    finally
    {
      DbUtilities.closeDatabaseObjects(ps, rs);
      DbConnector.returnConnection(dbcon);
    }
    Achievements.addAchievement(this, true);
  }
  
  public final void sendNewAchievement(Creature creature)
  {
    creature.getCommunicator().sendAchievement(this, true);
  }
  
  public final void sendUpdateAchievement(Creature creature)
  {
    creature.getCommunicator().sendAchievement(this, false);
  }
  
  public final void sendUpdatePersonalGoal(Creature creature)
  {
    creature.getCommunicator().updatePersonalGoal(this, true);
  }
  
  final int getTriggerOnCounter()
  {
    return getTemplate().getTriggerOnCounter();
  }
  
  static final void addTemplate(AchievementTemplate template)
  {
    templates.put(Integer.valueOf(template.getNumber()), template);
    if (template.isPersonalGoal()) {
      if (template.getType() == 3) {
        personalGoalSilverTemplates.add(template);
      } else if (template.getType() == 4) {
        personalGoalGoldTemplates.add(template);
      } else if (template.getType() == 5) {
        personalGoalDiamondTemplates.add(template);
      }
    }
  }
  
  static final void removeTemplate(AchievementTemplate template)
  {
    templates.remove(Integer.valueOf(template.getNumber()));
  }
  
  @Nullable
  public static final AchievementTemplate getTemplate(int number)
  {
    return (AchievementTemplate)templates.get(Integer.valueOf(number));
  }
  
  @Nullable
  public static final AchievementTemplate getTemplate(String name)
  {
    for (AchievementTemplate achievement : templates.values()) {
      if (achievement.getName().equalsIgnoreCase(name)) {
        return achievement;
      }
    }
    return null;
  }
  
  public final boolean isInVisible()
  {
    return getTemplate().isInvisible();
  }
  
  public final boolean isPlaySoundOnUpdate()
  {
    return getTemplate().isPlaySoundOnUpdate();
  }
  
  public final boolean isOneTimer()
  {
    return getTemplate().isOneTimer();
  }
  
  public static final LinkedList<AchievementTemplate> getSteelAchievements(Creature creature)
  {
    LinkedList<AchievementTemplate> toReturn = new LinkedList();
    for (AchievementTemplate achievement : templates.values()) {
      if (achievement.getType() == 2) {
        if ((creature.getPower() > 0) || 
          (creature.getName().equalsIgnoreCase(achievement.getCreator().toLowerCase()))) {
          toReturn.add(achievement);
        }
      }
    }
    return toReturn;
  }
  
  public static final AchievementTemplate getRandomPersonalGoldAchievement(Random personalRandom)
  {
    int num = personalRandom.nextInt(personalGoalGoldTemplates.size());
    return (AchievementTemplate)personalGoalGoldTemplates.get(num);
  }
  
  public static final AchievementTemplate getRandomPersonalSilverAchievement(Random personalRandom)
  {
    int num = personalRandom.nextInt(personalGoalSilverTemplates.size());
    return (AchievementTemplate)personalGoalSilverTemplates.get(num);
  }
  
  public static final AchievementTemplate getRandomPersonalDiamondAchievement(Random personalRandom)
  {
    int num = personalRandom.nextInt(personalGoalDiamondTemplates.size());
    return (AchievementTemplate)personalGoalDiamondTemplates.get(num);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\players\Achievement.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */