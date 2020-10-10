package com.wurmonline.server.skills;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Players;
import com.wurmonline.server.ServerEntry;
import com.wurmonline.server.Servers;
import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.WurmId;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.CreatureTemplate;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.utils.DbUtilities;
import com.wurmonline.shared.constants.CounterTypes;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;

public abstract class Skills
  implements MiscConstants, CounterTypes, TimeConstants
{
  private static final ConcurrentHashMap<Long, Set<Skill>> creatureSkillsMap = new ConcurrentHashMap();
  Map<Integer, Skill> skills;
  long id = -10L;
  String templateName = null;
  private static Logger logger = Logger.getLogger(Skills.class.getName());
  public boolean paying = true;
  public boolean priest = false;
  public boolean hasSkillGain = true;
  private static final String moveWeek = "UPDATE SKILLS SET WEEK2=DAY7";
  private static final String moveDay6 = "UPDATE SKILLS SET DAY7=DAY6";
  private static final String moveDay5 = "UPDATE SKILLS SET DAY6=DAY5";
  private static final String moveDay4 = "UPDATE SKILLS SET DAY5=DAY4";
  private static final String moveDay3 = "UPDATE SKILLS SET DAY4=DAY3";
  private static final String moveDay2 = "UPDATE SKILLS SET DAY3=DAY2";
  private static final String moveDay1 = "UPDATE SKILLS SET DAY2=DAY1";
  private static final String moveDay0 = "UPDATE SKILLS SET DAY1=VALUE";
  public static final float minChallengeValue = 21.0F;
  
  Skills()
  {
    this.skills = new TreeMap();
  }
  
  public boolean isTemplate()
  {
    return this.templateName != null;
  }
  
  boolean isPersonal()
  {
    return this.id != -10L;
  }
  
  private static final void switchWeek()
  {
    Connection dbcon = null;
    PreparedStatement ps = null;
    try
    {
      dbcon = DbConnector.getPlayerDbCon();
      ps = dbcon.prepareStatement("UPDATE SKILLS SET WEEK2=DAY7");
      ps.executeUpdate();
    }
    catch (SQLException ex)
    {
      logger.log(Level.WARNING, "moveWeek: UPDATE SKILLS SET WEEK2=DAY7 - " + ex.getMessage(), ex);
    }
    finally
    {
      DbUtilities.closeDatabaseObjects(ps, null);
      DbConnector.returnConnection(dbcon);
    }
  }
  
  private static final String getSkillSwitchString(int day)
  {
    switch (day)
    {
    case 0: 
      return "UPDATE SKILLS SET DAY1=VALUE";
    case 1: 
      return "UPDATE SKILLS SET DAY2=DAY1";
    case 2: 
      return "UPDATE SKILLS SET DAY3=DAY2";
    case 3: 
      return "UPDATE SKILLS SET DAY4=DAY3";
    case 4: 
      return "UPDATE SKILLS SET DAY5=DAY4";
    case 5: 
      return "UPDATE SKILLS SET DAY6=DAY5";
    case 6: 
      return "UPDATE SKILLS SET DAY7=DAY6";
    }
    logger.log(Level.WARNING, "This shouldn't happen: " + day);
    return "UPDATE SKILLS SET DAY7=DAY6";
  }
  
  private static final void switchDay(int day)
  {
    Connection dbcon = null;
    PreparedStatement ps = null;
    String psString = getSkillSwitchString(day);
    try
    {
      dbcon = DbConnector.getPlayerDbCon();
      ps = dbcon.prepareStatement(psString);
      ps.executeUpdate();
    }
    catch (SQLException ex)
    {
      logger.log(Level.WARNING, "Day: " + day + " - " + ex.getMessage(), ex);
    }
    finally
    {
      DbUtilities.closeDatabaseObjects(ps, null);
      DbConnector.returnConnection(dbcon);
    }
  }
  
  public static void switchSkills(long now)
  {
    if (!Servers.localServer.LOGINSERVER)
    {
      if (now - Servers.localServer.getSkillWeekSwitch() > 604800000L)
      {
        logger.log(Level.INFO, "Switching skill week");
        switchWeek();
        Servers.localServer.setSkillWeekSwitch(now);
      }
      if (now - Servers.localServer.getSkillDaySwitch() > 86400000L)
      {
        logger.log(Level.INFO, "Switching skill day");
        switchDay(6);
        switchDay(5);
        switchDay(4);
        switchDay(3);
        switchDay(2);
        switchDay(1);
        switchDay(0);
        Servers.localServer.setSkillDaySwitch(now);
      }
    }
    else
    {
      Servers.localServer.setSkillDaySwitch(now);
      Servers.localServer.setSkillWeekSwitch(now);
    }
  }
  
  public TempSkill learnTemp(int skillNumber, float startValue)
  {
    TempSkill skill = new TempSkill(skillNumber, startValue, this);
    int[] needed = skill.getDependencies();
    for (int x = 0; x < needed.length; x++) {
      if (!this.skills.containsKey(Integer.valueOf(needed[x]))) {
        learnTemp(needed[x], 1.0F);
      }
    }
    if (this.id != -10L) {
      if (WurmId.getType(this.id) == 0)
      {
        int parentSkillId = 0;
        if (needed.length > 0) {
          parentSkillId = needed[0];
        }
        try
        {
          if (parentSkillId != 0)
          {
            int parentType = SkillSystem.getTypeFor(parentSkillId);
            if (parentType == 0) {
              parentSkillId = Integer.MAX_VALUE;
            }
          }
          else if (skill.getType() == 1)
          {
            parentSkillId = 2147483646;
          }
          else
          {
            parentSkillId = Integer.MAX_VALUE;
          }
          Affinity[] affs = Affinities.getAffinities(this.id);
          if (affs.length > 0) {
            for (int x = 0; x < affs.length; x++) {
              if (affs[x].skillNumber == skillNumber) {
                skill.affinity = affs[x].number;
              }
            }
          }
          Players.getInstance().getPlayer(this.id).getCommunicator().sendAddSkill(skillNumber, parentSkillId, skill
            .getName(), startValue, startValue, skill.affinity);
        }
        catch (NoSuchPlayerException nsp)
        {
          logger.log(Level.WARNING, "skillNumber: " + skillNumber + ", startValue: " + startValue, nsp);
        }
      }
    }
    skill.touch();
    this.skills.put(Integer.valueOf(skillNumber), skill);
    return skill;
  }
  
  @Nonnull
  public Skill learn(int skillNumber, float startValue)
  {
    return learn(skillNumber, startValue, true);
  }
  
  @Nonnull
  public Skill learn(int skillNumber, float startValue, boolean sendAdd)
  {
    Skill skill = new DbSkill(skillNumber, startValue, this);
    int[] needed = skill.getDependencies();
    int aNeeded;
    for (aNeeded : needed) {
      if (!this.skills.containsKey(Integer.valueOf(aNeeded))) {
        learn(aNeeded, 1.0F);
      }
    }
    if ((this.id != -10L) && (WurmId.getType(this.id) == 0))
    {
      int parentSkillId = 0;
      if (needed.length > 0) {
        parentSkillId = needed[0];
      }
      try
      {
        int parentType;
        if (parentSkillId != 0)
        {
          parentType = SkillSystem.getTypeFor(parentSkillId);
          if (parentType == 0) {
            parentSkillId = Integer.MAX_VALUE;
          }
        }
        else if (skill.getType() == 1)
        {
          parentSkillId = 2147483646;
        }
        else
        {
          parentSkillId = Integer.MAX_VALUE;
        }
        for (Affinity aff : Affinities.getAffinities(this.id)) {
          if (aff.skillNumber == skillNumber) {
            skill.affinity = aff.number;
          }
        }
        Communicator comm = Players.getInstance().getPlayer(this.id).getCommunicator();
        if (sendAdd) {
          comm.sendAddSkill(skillNumber, parentSkillId, skill
          
            .getName(), startValue, startValue, skill.affinity);
        } else {
          comm.sendUpdateSkill(skillNumber, startValue, skill.affinity);
        }
      }
      catch (NoSuchPlayerException nsp)
      {
        logger.log(Level.WARNING, "skillNumber: " + skillNumber + ", startValue: " + startValue, nsp);
      }
    }
    skill.touch();
    this.skills.put(Integer.valueOf(skillNumber), skill);
    try
    {
      skill.save();
      save();
    }
    catch (Exception ex)
    {
      logger.log(Level.WARNING, "Failed to save skill " + skill.getName() + "(" + skillNumber + ")", ex);
    }
    return skill;
  }
  
  @Nonnull
  public Skill getSkill(String name)
    throws NoSuchSkillException
  {
    Skill toReturn = null;
    for (Iterator<Skill> it = this.skills.values().iterator(); it.hasNext();)
    {
      Skill checked = (Skill)it.next();
      if (checked.getName().equals(name))
      {
        toReturn = checked;
        break;
      }
    }
    if (toReturn == null) {
      throw new NoSuchSkillException("Unknown skill - " + name + ", total number of skills known is: " + this.skills.size());
    }
    return toReturn;
  }
  
  @Nonnull
  public Skill getSkill(int number)
    throws NoSuchSkillException
  {
    Skill toReturn = (Skill)this.skills.get(Integer.valueOf(number));
    if (toReturn == null) {
      throw new NoSuchSkillException("Unknown skill - " + SkillSystem.getNameFor(number) + ", total number of skills known is: " + this.skills.size());
    }
    return toReturn;
  }
  
  public final void switchSkillNumbers(Skill skillOne, Skill skillTwo)
  {
    int numberOne = skillTwo.getNumber();
    try
    {
      skillTwo.setNumber(skillOne.getNumber());
      this.skills.put(Integer.valueOf(skillTwo.number), skillTwo);
      
      skillTwo.setKnowledge(skillTwo.knowledge, false, false);
    }
    catch (IOException iox2)
    {
      logger.log(Level.INFO, iox2.getMessage());
    }
    try
    {
      skillOne.setNumber(numberOne);
      this.skills.put(Integer.valueOf(skillOne.number), skillOne);
      
      skillOne.setKnowledge(skillOne.knowledge, false, false);
    }
    catch (IOException iox)
    {
      logger.log(Level.INFO, iox.getMessage());
    }
  }
  
  @Nonnull
  public Skill getSkillOrLearn(int number)
  {
    Skill toReturn = (Skill)this.skills.get(Integer.valueOf(number));
    if (toReturn == null) {
      return learn(number, 1.0F);
    }
    return toReturn;
  }
  
  public void checkDecay()
  {
    Set<Skill> memorySkills = new HashSet();
    Set<Skill> otherSkills = new HashSet();
    Set<Map.Entry<Integer, Skill>> toRemove = new HashSet();
    for (Iterator<Map.Entry<Integer, Skill>> it = this.skills.entrySet().iterator(); it.hasNext();)
    {
      Map.Entry<Integer, Skill> entry = (Map.Entry)it.next();
      Skill toCheck = (Skill)entry.getValue();
      try
      {
        if (toCheck.getType() == 1) {
          memorySkills.add(toCheck);
        } else {
          otherSkills.add(toCheck);
        }
      }
      catch (NullPointerException np)
      {
        toRemove.add(entry);
      }
    }
    for (Iterator<Skill> it = memorySkills.iterator(); it.hasNext();)
    {
      Skill mem = (Skill)it.next();
      mem.checkDecay();
    }
    for (Iterator<Skill> it = otherSkills.iterator(); it.hasNext();)
    {
      Skill other = (Skill)it.next();
      other.checkDecay();
    }
    for (Iterator<Map.Entry<Integer, Skill>> it = toRemove.iterator(); it.hasNext();)
    {
      Map.Entry<Integer, Skill> entry = (Map.Entry)it.next();
      Integer toremove = (Integer)entry.getKey();
      this.skills.remove(toremove);
    }
  }
  
  public Map<Integer, Skill> getSkillTree()
  {
    return this.skills;
  }
  
  public Skill[] getSkills()
  {
    Skill[] toReturn = new Skill[this.skills.size()];
    int i = 0;
    for (Iterator<Skill> it = this.skills.values().iterator(); it.hasNext();)
    {
      toReturn[i] = ((Skill)it.next());
      i++;
    }
    return toReturn;
  }
  
  public Skill[] getSkillsNoTemp()
  {
    Set<Skill> noTemps = new HashSet();
    for (Iterator<Skill> it = this.skills.values().iterator(); it.hasNext();)
    {
      Skill isTemp = (Skill)it.next();
      if (!isTemp.isTemporary()) {
        noTemps.add(isTemp);
      }
    }
    Skill[] toReturn = (Skill[])noTemps.toArray(new Skill[noTemps.size()]);
    return toReturn;
  }
  
  public void clone(Skill[] skillarr)
  {
    this.skills = new TreeMap();
    for (int x = 0; x < skillarr.length; x++) {
      if ((!skillarr[x].isTemporary()) && (!(skillarr[x] instanceof TempSkill)))
      {
        DbSkill newSkill = new DbSkill(skillarr[x].getNumber(), skillarr[x].knowledge, this);
        this.skills.put(Integer.valueOf(skillarr[x].getNumber()), newSkill);
        try
        {
          newSkill.touch();
          newSkill.save();
        }
        catch (Exception iox)
        {
          logger.log(Level.WARNING, "Failed to save skill " + newSkill.getName() + " for " + this.id, iox);
        }
      }
      else
      {
        TempSkill newSkill = new TempSkill(skillarr[x].getNumber(), skillarr[x].knowledge, this);
        this.skills.put(Integer.valueOf(skillarr[x].getNumber()), newSkill);
        newSkill.touch();
      }
    }
  }
  
  public long getId()
  {
    return this.id;
  }
  
  public static final void clearCreatureLoadMap()
  {
    creatureSkillsMap.clear();
  }
  
  public static final void loadAllCreatureSkills()
    throws Exception
  {
    Connection dbcon = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    try
    {
      dbcon = DbConnector.getCreatureDbCon();
      ps = dbcon.prepareStatement("SELECT * FROM SKILLS");
      rs = ps.executeQuery();
      while (rs.next())
      {
        Skill skill = new DbSkill(rs.getLong("ID"), rs.getInt("NUMBER"), rs.getDouble("VALUE"), rs.getDouble("MINVALUE"), rs.getLong("LASTUSED"));
        long owner = rs.getLong("OWNER");
        
        Set<Skill> skills = (Set)creatureSkillsMap.get(Long.valueOf(owner));
        if (skills == null) {
          skills = new HashSet();
        }
        skills.add(skill);
        creatureSkillsMap.put(Long.valueOf(owner), skills);
      }
    }
    finally
    {
      DbUtilities.closeDatabaseObjects(ps, rs);
      DbConnector.returnConnection(dbcon);
    }
  }
  
  public static final void fillCreatureTempSkills(Creature creature)
  {
    Skills cSkills = creature.getSkills();
    Map<Integer, Skill> treeSkills = creature.getSkills().getSkillTree();
    CreatureTemplate template = creature.getTemplate();
    try
    {
      Skills tSkills = template.getSkills();
      for (Skill ts : tSkills.getSkills()) {
        if (!treeSkills.containsKey(Integer.valueOf(ts.getNumber()))) {
          cSkills.learnTemp(ts.getNumber(), (float)ts.knowledge);
        }
      }
    }
    catch (Exception e)
    {
      logger.log(Level.WARNING, "Unknown error while checking temp skill for creature: " + creature.getWurmId() + ".", e);
    }
  }
  
  public final void initializeSkills()
  {
    Set<Skill> skillSet = (Set)creatureSkillsMap.get(Long.valueOf(this.id));
    if (skillSet == null) {
      return;
    }
    for (Skill skill : skillSet)
    {
      Skill dbSkill = new DbSkill(skill.id, this, skill.getNumber(), skill.knowledge, skill.minimum, skill.lastUsed);
      
      this.skills.put(Integer.valueOf(dbSkill.getNumber()), dbSkill);
    }
  }
  
  public String getTemplateName()
  {
    return this.templateName;
  }
  
  public void saveDirty()
    throws IOException
  {
    if (this.id != -10L) {
      if (WurmId.getType(this.id) == 0) {
        for (Skill skill : this.skills.values()) {
          skill.saveValue(true);
        }
      }
    }
  }
  
  public void save()
    throws IOException
  {
    if (this.id != -10L) {
      if (WurmId.getType(this.id) == 0) {
        for (Skill skill : this.skills.values()) {
          if (skill.isDirty()) {
            skill.saveValue(true);
          }
        }
      }
    }
  }
  
  public final void addTempSkills()
  {
    float initialTempValue = WurmId.getType(this.id) == 0 ? Servers.localServer.getSkilloverallval() : 1.0F;
    for (int i = 0; i < SkillList.skillArray.length; i++)
    {
      Integer key = Integer.valueOf(SkillList.skillArray[i]);
      if (!this.skills.containsKey(key)) {
        if ((key.intValue() == 1023) && (WurmId.getType(this.id) == 0)) {
          learnTemp(key.intValue(), Servers.localServer.getSkillfightval());
        } else if ((key.intValue() == 100) && 
          (WurmId.getType(this.id) == 0)) {
          learnTemp(key.intValue(), Servers.localServer.getSkillmindval());
        } else if ((key.intValue() == 104) && 
          (WurmId.getType(this.id) == 0)) {
          learnTemp(key.intValue(), Servers.localServer.getSkillbcval());
        } else {
          learnTemp(key.intValue(), initialTempValue);
        }
      }
    }
  }
  
  public abstract void load()
    throws Exception;
  
  public abstract void delete()
    throws Exception;
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\skills\Skills.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */