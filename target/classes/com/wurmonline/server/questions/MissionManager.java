package com.wurmonline.server.questions;

import com.wurmonline.mesh.Tiles;
import com.wurmonline.mesh.Tiles.Tile;
import com.wurmonline.server.Items;
import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.WurmId;
import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.behaviours.ActionEntry;
import com.wurmonline.server.behaviours.Actions;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.CreatureTemplate;
import com.wurmonline.server.creatures.CreatureTemplateFactory;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemTemplate;
import com.wurmonline.server.items.ItemTemplateFactory;
import com.wurmonline.server.players.Achievement;
import com.wurmonline.server.players.AchievementTemplate;
import com.wurmonline.server.players.PlayerInfo;
import com.wurmonline.server.players.PlayerInfoFactory;
import com.wurmonline.server.skills.SkillSystem;
import com.wurmonline.server.skills.SkillTemplate;
import com.wurmonline.server.sounds.SoundPlayer;
import com.wurmonline.server.structures.Fence;
import com.wurmonline.server.structures.FenceGate;
import com.wurmonline.server.structures.NoSuchStructureException;
import com.wurmonline.server.structures.Structure;
import com.wurmonline.server.structures.Structures;
import com.wurmonline.server.structures.Wall;
import com.wurmonline.server.tutorial.Mission;
import com.wurmonline.server.tutorial.MissionTargets;
import com.wurmonline.server.tutorial.MissionTrigger;
import com.wurmonline.server.tutorial.MissionTriggers;
import com.wurmonline.server.tutorial.Missions;
import com.wurmonline.server.tutorial.SpecialEffects;
import com.wurmonline.server.tutorial.TriggerEffect;
import com.wurmonline.server.tutorial.TriggerEffects;
import com.wurmonline.server.tutorial.Triggers2Effects;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.villages.Villages;
import com.wurmonline.shared.constants.CounterTypes;
import com.wurmonline.shared.constants.CreatureTypes;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.annotation.Nullable;

public class MissionManager
  extends Question
  implements CounterTypes, TimeConstants, CreatureTypes
{
  private int level = 0;
  private int missionId = 0;
  private int triggerId = 0;
  private int effectId = 0;
  private static final float REWSK1 = 0.001F;
  private static final float REWSK2 = 0.002F;
  private static final float REWSK3 = 0.01F;
  private static final float REWSK4 = 0.05F;
  private static final float REWSK5 = 0.1F;
  private static final float REWSK6 = 1.0F;
  private static final float REWSK7 = 10.0F;
  private static final float REWSK8 = 20.0F;
  private static final String percent = "%";
  private static final String percentComma = "%,";
  private static final int INTRO = 0;
  private static final int CREATE_MISSION = 1;
  private static final int EDIT_MISSION = 2;
  private static final int EDIT_TRIGGER = 3;
  private static final int CREATE_TRIGGER = 4;
  private static final int LIST_MISSIONS = 5;
  private static final int LIST_TRIGGERS = 6;
  private static final int EDIT_EFFECT = 7;
  private static final int CREATE_EFFECT = 8;
  private static final int LIST_EFFECTS = 9;
  private static Logger logger = Logger.getLogger(MissionManager.class.getName());
  private LinkedList<ItemTemplate> itemplates = new LinkedList();
  private LinkedList<Item> ritems = new LinkedList();
  private LinkedList<MissionTrigger> mtriggers = new LinkedList();
  private LinkedList<MissionTrigger> utriggers = new LinkedList();
  private LinkedList<SkillTemplate> stemplates = new LinkedList();
  private LinkedList<ActionEntry> actionEntries = new LinkedList();
  private LinkedList<Mission> missionsAvail = new LinkedList();
  private LinkedList<SpecialEffects> effectsAvail = new LinkedList();
  private LinkedList<CreatureTemplate> creaturesAvail = new LinkedList();
  private LinkedList<TriggerEffect> teffects = new LinkedList();
  private LinkedList<TriggerEffect> ueffects = new LinkedList();
  private LinkedList<AchievementTemplate> myAchievements = null;
  private LinkedList<Byte> creaturesTypes = new LinkedList();
  private final String targName;
  private final long missionRulerId;
  private boolean listMineOnly = false;
  private boolean dontListMine = false;
  private boolean onlyCurrent = false;
  private int includeM = 0;
  private boolean incMInactive = true;
  private boolean typeSystem = true;
  private boolean typeGM = true;
  private boolean typePlayer = true;
  private long listForUser = -10L;
  private String userName = "";
  private String groupName = "";
  private boolean incTInactive = true;
  private int showT = 0;
  private boolean incEInactive = true;
  private int showE = 0;
  private long currentTargetId = -10L;
  private int sortBy = 2;
  private String origQuestion = "";
  private String origTitle = "";
  private String lastQuestion = "";
  private String lastTitle = "";
  private String errorText = "";
  private byte creatorType = 0;
  private String sbacks = "";
  public static byte CAN_SEE_EPIC_MISSIONS = 2;
  private static final String red = "color=\"255,127,127\"";
  private static final String green = "color=\"127,255,127\"";
  private static final String orange = "color=\"255,177,40\"";
  private static final String blue = "color=\"140,140,255\";";
  private static final String hoverActive = ";hover=\"Active\"";
  private static final String hoverInactive = ";hover=\"Inactive\"";
  private static final String hoverNoTriggers = ";hover=\"No Triggers\"";
  private static final String hoverCurrentTarget = ";hover=\"Current Target\"";
  private static final String hoverNotImplemented = ";hover=\"Not implemented (yet)\"";
  private static final String line = "label{type=\"bold\";text=\"- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -\"}";
  
  public MissionManager(Creature aResponder, String aTitle, String aQuestion, long aTarget, String targetName, long _missionRulerId)
  {
    super(aResponder, aTitle, aQuestion, 86, aTarget);
    this.targName = targetName;
    this.missionRulerId = _missionRulerId;
    this.currentTargetId = aTarget;
    this.origQuestion = aQuestion;
    this.origTitle = aTitle;
    this.lastQuestion = aQuestion;
    this.lastTitle = aTitle;
    if (aResponder.getPower() <= 1)
    {
      this.listMineOnly = true;
      this.typeSystem = false;
      this.typeGM = false;
    }
  }
  
  public void answer(Properties aAnswers)
  {
    Item ruler = null;
    try
    {
      ruler = Items.getItem(this.missionRulerId);
      if (ruler.getOwnerId() != getResponder().getWurmId())
      {
        getResponder().getCommunicator().sendNormalServerMessage("You are not the ruler of this mission ruler.");
        return;
      }
    }
    catch (NoSuchItemException nsi)
    {
      getResponder().getCommunicator().sendNormalServerMessage("The mission ruler is gone!");
      return;
    }
    setAnswer(aAnswers);
    parseAnswer(ruler);
  }
  
  private boolean parseAnswer(Item ruler)
  {
    boolean back = getBooleanProp("back");
    if (back) {
      return parseBack();
    }
    switch (this.level)
    {
    case 0: 
      return parseIntro();
    case 1: 
    case 2: 
      return parseMission(ruler);
    case 5: 
      return parseMissionList();
    case 3: 
    case 4: 
      return parseTrigger(ruler);
    case 6: 
      return parseTriggerList();
    case 7: 
    case 8: 
      return parseEffect(ruler);
    case 9: 
      return parseEffectsList();
    }
    return false;
  }
  
  private boolean parseBack()
  {
    String[] backs = this.sbacks.split(Pattern.quote("|"));
    if (backs.length > 0)
    {
      this.errorText = "";
      
      StringBuilder buf = new StringBuilder();
      if (backs.length > 1)
      {
        buf.append(backs[0]);
        for (int s = 1; s < backs.length - 1; s++) {
          buf.append("|" + backs[s]);
        }
      }
      this.sbacks = buf.toString();
      String[] lparts = backs[(backs.length - 1)].split(",");
      int newLevel = Integer.parseInt(lparts[0]);
      this.missionId = Integer.parseInt(lparts[1]);
      this.triggerId = Integer.parseInt(lparts[2]);
      this.effectId = Integer.parseInt(lparts[3]);
      switch (newLevel)
      {
      case 0: 
        return showIntro();
      case 1: 
      case 2: 
        return editMission(this.missionId, null);
      case 5: 
        this.sortBy = 2;
        return showMissionList();
      case 3: 
      case 4: 
        return editTrigger(this.triggerId, null);
      case 6: 
        this.sortBy = 2;
        return showTriggerList(this.missionId);
      case 7: 
      case 8: 
        return editEffect(this.effectId, null);
      case 9: 
        this.sortBy = 2;
        return showEffectsList(this.missionId, this.triggerId);
      }
    }
    else
    {
      return showIntro();
    }
    return false;
  }
  
  private boolean parseIntro()
  {
    this.sbacks = "0,0,0,0";
    this.missionId = 0;
    this.triggerId = 0;
    this.effectId = 0;
    if (getResponder().getPower() > 0)
    {
      this.listMineOnly = getBooleanProp("listmine");
      this.dontListMine = getBooleanProp("nolistmine");
      this.onlyCurrent = getBooleanProp("onlyCurrent");
      this.includeM = getIntProp("includeM");
      this.incMInactive = getBooleanProp("incMInactive");
      this.typeSystem = getBooleanProp("typeSystem");
      this.typeGM = getBooleanProp("typeGM");
      this.typePlayer = getBooleanProp("typePlayer");
    }
    this.groupName = getStringProp("groupName");
    if (this.groupName == null) {
      this.groupName = "";
    }
    String specialName = getStringProp("specialName");
    parsePlayerName(specialName);
    
    this.incTInactive = getBooleanProp("incTInactive");
    this.incEInactive = getBooleanProp("incEInactive");
    this.showE = getIntProp("showE");
    
    boolean listMissions = getBooleanProp("listMissions");
    boolean listTriggers = getBooleanProp("listTriggers");
    boolean listEffects = getBooleanProp("listEffects");
    boolean createMission = getBooleanProp("createMission");
    boolean createTrigger = getBooleanProp("createTrigger");
    boolean createEffect = getBooleanProp("createEffect");
    this.sortBy = 2;
    if (listMissions) {
      return showMissionList();
    }
    if (listTriggers) {
      return showTriggerList(0);
    }
    if (listEffects) {
      return showEffectsList(0, 0);
    }
    if (createMission) {
      return createNewMission(null);
    }
    if (createTrigger) {
      return createNewTrigger(null);
    }
    if (createEffect) {
      return createNewEffect(null);
    }
    return false;
  }
  
  private boolean parseMissionList()
  {
    this.missionId = 0;
    this.triggerId = 0;
    boolean filter = getBooleanProp("filter");
    boolean editMission = getBooleanProp("editMission");
    boolean showStats = getBooleanProp("showStats");
    boolean listTriggers = getBooleanProp("listTriggers");
    boolean listEffects = getBooleanProp("listEffects");
    boolean createMission = getBooleanProp("createMission");
    if (filter)
    {
      if (getResponder().getPower() > 0)
      {
        this.includeM = Integer.parseInt(getAnswer().getProperty("includeM"));
        this.incMInactive = getBooleanProp("incMInactive");
      }
      this.groupName = getAnswer().getProperty("groupName");
      if (this.groupName == null) {
        this.groupName = "";
      }
      String specialName = getAnswer().getProperty("specialName");
      parsePlayerName(specialName);
      
      return showMissionList();
    }
    if (createMission)
    {
      this.sbacks = (this.sbacks + "|" + 5 + ",0,0,0");
      return createNewMission(null);
    }
    String sel = getAnswer().getProperty("sel");
    int mid = Integer.parseInt(sel);
    if (editMission)
    {
      this.sbacks = (this.sbacks + "|" + 5 + ",0,0,0");
      if (mid == 0) {
        return createNewMission(null);
      }
      return editMission(mid, null);
    }
    if (showStats) {
      return showStats(mid);
    }
    if (listTriggers)
    {
      this.sortBy = 2;
      this.sbacks = (this.sbacks + "|" + 5 + ",0,0,0");
      return showTriggerList(mid);
    }
    if (listEffects)
    {
      this.sortBy = 2;
      this.sbacks = (this.sbacks + "|" + 5 + ",0,0,0");
      return showEffectsList(mid, 0);
    }
    for (String key : getAnswer().stringPropertyNames()) {
      if (key.startsWith("sort"))
      {
        String sid = key.substring(4);
        this.sortBy = Integer.parseInt(sid);
        break;
      }
    }
    return showMissionList();
  }
  
  private boolean parseTriggerList()
  {
    boolean filter = getBooleanProp("filter");
    boolean editTrigger = getBooleanProp("editTrigger");
    boolean listEffects = getBooleanProp("listEffects");
    boolean createTrigger = getBooleanProp("createTrigger");
    if (filter)
    {
      this.incTInactive = getBooleanProp("incTInactive");
      this.showT = Integer.parseInt(getAnswer().getProperty("showT"));
      
      return showTriggerList(this.missionId);
    }
    if (createTrigger)
    {
      this.sbacks = (this.sbacks + "|" + 6 + "," + this.missionId + ",0,0");
      return createNewTrigger(null);
    }
    String sel = getAnswer().getProperty("sel");
    int tid = Integer.parseInt(sel);
    if (editTrigger)
    {
      this.sbacks = (this.sbacks + "|" + 6 + "," + this.missionId + ",0,0");
      if (tid == 0) {
        return createNewTrigger(null);
      }
      return editTrigger(tid, null);
    }
    if (listEffects)
    {
      this.sortBy = 2;
      this.sbacks = (this.sbacks + "|" + 6 + "," + this.missionId + ",0,0");
      return showEffectsList(this.missionId, this.triggerId);
    }
    for (String key : getAnswer().stringPropertyNames())
    {
      if (key.startsWith("sort"))
      {
        String sid = key.substring(4);
        this.sortBy = Integer.parseInt(sid);
        break;
      }
      if (key.startsWith("delT"))
      {
        String sid = key.substring(4);
        int trigId = Integer.parseInt(sid);
        MissionTrigger trg = MissionTriggers.getTriggerWithId(trigId);
        if (trg == null)
        {
          getResponder().getCommunicator().sendNormalServerMessage("Cannot find trigger!"); break;
        }
        trg.destroy();
        getResponder().getCommunicator().sendNormalServerMessage("You delete the trigger " + trg
          .getName() + ".");
        
        break;
      }
    }
    return showTriggerList(this.missionId);
  }
  
  private boolean parseEffectsList()
  {
    boolean filter = getBooleanProp("filter");
    boolean editEffect = getBooleanProp("editEffect");
    boolean showTriggers = getBooleanProp("showTriggers");
    boolean createEffect = getBooleanProp("createEffect");
    if (filter)
    {
      this.incEInactive = getBooleanProp("incEInactive");
      this.showE = Integer.parseInt(getAnswer().getProperty("showE"));
      
      return showEffectsList(this.missionId, this.triggerId);
    }
    if (createEffect)
    {
      this.sbacks = (this.sbacks + "|" + 9 + "," + this.missionId + "," + this.triggerId + ",0");
      return createNewEffect(null);
    }
    String sel = getAnswer().getProperty("sel");
    int eid = Integer.parseInt(sel);
    if (editEffect)
    {
      this.sbacks = (this.sbacks + "|" + 9 + "," + this.missionId + "," + this.triggerId + ",0");
      if (eid == 0) {
        return createNewEffect(null);
      }
      return editEffect(eid, null);
    }
    if (showTriggers) {
      getResponder().getCommunicator().sendNormalServerMessage("Not Implemented (yet)");
    } else {
      for (String key : getAnswer().stringPropertyNames()) {
        if (key.startsWith("sort"))
        {
          String sid = key.substring(4);
          this.sortBy = Integer.parseInt(sid);
          break;
        }
      }
    }
    return showEffectsList(this.missionId, this.triggerId);
  }
  
  private void parsePlayerName(String playerName)
  {
    if ((playerName != null) && (playerName.length() > 0))
    {
      PlayerInfo pf = PlayerInfoFactory.createPlayerInfo(playerName);
      try
      {
        pf.load();
        this.listForUser = pf.wurmId;
        this.userName = pf.getName();
      }
      catch (IOException iox)
      {
        getResponder().getCommunicator().sendNormalServerMessage("No such player: " + playerName + ".");
        this.listForUser = -10L;
        this.userName = "";
      }
    }
    else
    {
      this.listForUser = -10L;
      this.userName = "";
    }
  }
  
  private boolean showIntro()
  {
    MissionManager mm = new MissionManager(getResponder(), this.origTitle, this.origQuestion, this.target, this.targName, this.missionRulerId);
    
    cloneValues(mm);
    mm.level = 0;
    mm.sendQuestion();
    return true;
  }
  
  private boolean showMissionList()
  {
    if ((getResponder().getLogger() != null) && (getResponder().getPower() > 0)) {
      getResponder().getLogger().info(getResponder() + ": Listing MISSIONS");
    }
    MissionManager mm = new MissionManager(getResponder(), "Mission list", "Mission list", this.target, this.targName, this.missionRulerId);
    
    cloneValues(mm);
    mm.level = 5;
    mm.sendMissionList();
    return true;
  }
  
  private boolean editMission(int mid, @Nullable Properties ans)
  {
    String name = "";
    if (ans == null)
    {
      if (mid == 0)
      {
        getResponder().getCommunicator().sendNormalServerMessage("No mission selected!");
        return false;
      }
      Mission msn = Missions.getMissionWithId(mid);
      if (msn == null)
      {
        getResponder().getCommunicator().sendNormalServerMessage("Cannot find mission!");
        return false;
      }
      name = msn.getName();
    }
    else
    {
      name = ans.getProperty("name", "");
    }
    if ((getResponder().getLogger() != null) && (getResponder().getPower() > 0)) {
      getResponder().getLogger().info(getResponder() + ": Edit MISSION with name " + name + " and id " + mid);
    }
    MissionManager mm = new MissionManager(getResponder(), "Edit mission", "Edit mission:", this.target, this.targName, this.missionRulerId);
    
    cloneValues(mm);
    if (ans == null)
    {
      mm.level = 2;
      mm.missionId = mid;
    }
    else
    {
      getResponder().getCommunicator().sendAlertServerMessage(this.errorText);
      mm.level = this.level;
      mm.setAnswer(getAnswer());
    }
    mm.sendManageMission();
    return true;
  }
  
  private boolean editTrigger(int tid, @Nullable Properties ans)
  {
    String name = "";
    if (ans == null)
    {
      if (tid == 0)
      {
        getResponder().getCommunicator().sendNormalServerMessage("No trigger selected!");
        return false;
      }
      MissionTrigger trg = MissionTriggers.getTriggerWithId(tid);
      if (trg == null)
      {
        this.errorText = "Cannot find trigger!";
        getResponder().getCommunicator().sendNormalServerMessage(this.errorText);
        return false;
      }
      name = trg.getName();
    }
    else
    {
      name = ans.getProperty("name", "");
    }
    if ((getResponder().getLogger() != null) && (getResponder().getPower() > 0)) {
      getResponder().getLogger().info(getResponder() + ": Edit TRIGGER with name " + name + " and id " + tid);
    }
    MissionManager mm = new MissionManager(getResponder(), "Mission triggers", "Edit mission trigger", this.target, this.targName, this.missionRulerId);
    
    cloneValues(mm);
    if (ans == null)
    {
      mm.triggerId = tid;
      mm.level = 3;
    }
    else
    {
      getResponder().getCommunicator().sendAlertServerMessage(this.errorText);
      mm.level = this.level;
      mm.setAnswer(ans);
    }
    mm.sendManageTrigger();
    return true;
  }
  
  private boolean editEffect(int eid, @Nullable Properties ans)
  {
    String name = "";
    if (ans == null)
    {
      if (eid == 0)
      {
        getResponder().getCommunicator().sendNormalServerMessage("No mission selected!");
        return false;
      }
      TriggerEffect eff = TriggerEffects.getTriggerEffect(eid);
      if (eff == null)
      {
        getResponder().getCommunicator().sendNormalServerMessage("Cannot find effect!");
        return false;
      }
      name = eff.getName();
    }
    else
    {
      name = ans.getProperty("name", "");
    }
    MissionManager mm = new MissionManager(getResponder(), "Trigger effect", "Edit trigger effect " + name, this.target, this.targName, this.missionRulerId);
    
    cloneValues(mm);
    mm.triggerId = 0;
    if (ans == null)
    {
      mm.effectId = eid;
      mm.level = 7;
    }
    else
    {
      getResponder().getCommunicator().sendAlertServerMessage(this.errorText);
      mm.level = this.level;
      mm.setAnswer(ans);
    }
    mm.sendManageEffect();
    return true;
  }
  
  private boolean showStats(int mid)
  {
    if (mid == 0)
    {
      getResponder().getCommunicator().sendNormalServerMessage("No mission selected!");
      return false;
    }
    Mission msn = Missions.getMissionWithId(mid);
    if (msn == null)
    {
      getResponder().getCommunicator().sendNormalServerMessage("Cannot find mission!");
      return false;
    }
    MissionStats ms = new MissionStats(getResponder(), "Mission statistics", "Statistics for " + msn.getName(), msn.getId());
    ms.setRoot(this);
    ms.sendQuestion();
    return true;
  }
  
  private boolean showTriggerList(int mid)
  {
    String mQuestion;
    String mTitle;
    String mQuestion;
    if (mid == 0)
    {
      String mTitle = "Trigger List";
      mQuestion = "Listing all mission triggers";
    }
    else
    {
      Mission msn = Missions.getMissionWithId(mid);
      if (msn == null)
      {
        getResponder().getCommunicator().sendNormalServerMessage("Cannot find mission!");
        return false;
      }
      mTitle = msn.getName() + " Trigger List";
      mQuestion = "Listing mission " + msn.getName() + " triggers";
    }
    if ((getResponder().getLogger() != null) && (getResponder().getPower() > 0)) {
      getResponder().getLogger().info(getResponder() + ": " + mTitle);
    }
    MissionManager mm = new MissionManager(getResponder(), mTitle, mQuestion, this.target, this.targName, this.missionRulerId);
    cloneValues(mm);
    mm.level = 6;
    mm.missionId = mid;
    mm.sendTriggerList();
    return true;
  }
  
  private boolean showEffectsList(int mid, int tid)
  {
    String mQuestion;
    String mTitle;
    String mQuestion;
    if (mid == 0)
    {
      String mQuestion;
      if (tid == 0)
      {
        String mTitle = "Mission Effects List";
        mQuestion = "Listing all mission trigger effects";
      }
      else
      {
        MissionTrigger mt = MissionTriggers.getTriggerWithId(tid);
        if (mt == null)
        {
          getResponder().getCommunicator().sendNormalServerMessage("Cannot find trigger!");
          return false;
        }
        String mTitle = "Trigger " + mt.getName() + " Effects List";
        mQuestion = "Listing trigger " + mt.getName() + "  effects";
      }
    }
    else
    {
      Mission msn = Missions.getMissionWithId(mid);
      if (msn == null)
      {
        getResponder().getCommunicator().sendNormalServerMessage("Cannot find mission!");
        return false;
      }
      String mQuestion;
      if (tid == 0)
      {
        String mTitle = msn.getName() + " Trigger Effects List";
        mQuestion = "Listing mission " + msn.getName() + " trigger effects";
      }
      else
      {
        MissionTrigger mt = MissionTriggers.getTriggerWithId(tid);
        if (mt == null)
        {
          getResponder().getCommunicator().sendNormalServerMessage("Cannot find trigger!");
          return false;
        }
        mTitle = msn.getName() + " Trigger " + mt.getName() + " Effects List";
        mQuestion = "Listing trigger " + mt.getName() + "  effects";
      }
    }
    if ((getResponder().getLogger() != null) && (getResponder().getPower() > 0)) {
      getResponder().getLogger().info(getResponder() + ": " + mTitle);
    }
    MissionManager mm = new MissionManager(getResponder(), mTitle, mQuestion, this.target, this.targName, this.missionRulerId);
    cloneValues(mm);
    mm.level = 9;
    
    mm.missionId = mid;
    mm.triggerId = tid;
    mm.sendEffectList();
    return true;
  }
  
  private boolean createNewMission(@Nullable Properties ans)
  {
    if ((getResponder().getLogger() != null) && (getResponder().getPower() > 0)) {
      getResponder().getLogger().info(getResponder() + ": Create new MISSION");
    }
    MissionManager mm = new MissionManager(getResponder(), "Create New Mission", "Create New Mission:", this.target, this.targName, this.missionRulerId);
    
    cloneValues(mm);
    mm.level = 1;
    mm.missionId = 0;
    mm.setAnswer(ans);
    mm.sendManageMission();
    return true;
  }
  
  private boolean createNewTrigger(@Nullable Properties ans)
  {
    if ((getResponder().getLogger() != null) && (getResponder().getPower() > 0)) {
      getResponder().getLogger().info(getResponder() + ": Create TRIGGER");
    }
    MissionManager mm = new MissionManager(getResponder(), "New mission trigger", "Create a new mission trigger", this.target, this.targName, this.missionRulerId);
    
    cloneValues(mm);
    mm.level = 4;
    mm.missionId = 0;
    mm.triggerId = 0;
    mm.setAnswer(ans);
    mm.sendManageTrigger();
    return true;
  }
  
  private boolean createNewEffect(@Nullable Properties ans)
  {
    if ((getResponder().getLogger() != null) && (getResponder().getPower() > 0)) {
      getResponder().getLogger().info(getResponder() + ": Create EFFECT");
    }
    MissionManager mm = new MissionManager(getResponder(), "New mission effect", "Create a new mission trigger effect", this.target, this.targName, this.missionRulerId);
    
    cloneValues(mm);
    mm.level = 8;
    mm.missionId = 0;
    mm.triggerId = 0;
    mm.effectId = 0;
    mm.setAnswer(ans);
    mm.sendManageEffect();
    return true;
  }
  
  private void cloneValues(MissionManager mm)
  {
    mm.missionId = this.missionId;
    mm.triggerId = this.triggerId;
    mm.effectId = this.effectId;
    mm.listMineOnly = this.listMineOnly;
    mm.dontListMine = this.dontListMine;
    mm.onlyCurrent = this.onlyCurrent;
    mm.includeM = this.includeM;
    mm.incMInactive = this.incMInactive;
    mm.typeSystem = this.typeSystem;
    mm.typeGM = this.typeGM;
    mm.typePlayer = this.typePlayer;
    mm.listForUser = this.listForUser;
    mm.userName = this.userName;
    mm.groupName = this.groupName;
    mm.currentTargetId = this.currentTargetId;
    mm.origQuestion = this.origQuestion;
    mm.origTitle = this.origTitle;
    mm.lastQuestion = this.lastQuestion;
    mm.lastTitle = this.lastTitle;
    mm.incTInactive = this.incTInactive;
    mm.showT = this.showT;
    mm.incEInactive = this.incEInactive;
    mm.showE = this.showE;
    mm.sbacks = this.sbacks;
    mm.errorText = this.errorText;
    mm.sortBy = this.sortBy;
    
    mm.creatorType = this.creatorType;
    
    mm.itemplates = this.itemplates;
    mm.ritems = this.ritems;
    mm.mtriggers = this.mtriggers;
    mm.stemplates = this.stemplates;
    mm.actionEntries = this.actionEntries;
    mm.missionsAvail = this.missionsAvail;
    mm.effectsAvail = this.effectsAvail;
    mm.creaturesAvail = this.creaturesAvail;
    mm.myAchievements = this.myAchievements;
    mm.creaturesTypes = this.creaturesTypes;
  }
  
  protected void cloneAndSendManageEffect(@Nullable String sound)
  {
    MissionManager mm = new MissionManager(getResponder(), this.lastTitle, this.lastQuestion, this.target, this.targName, this.missionRulerId);
    
    cloneValues(mm);
    mm.level = this.level;
    mm.setAnswer(getAnswer());
    if (sound != null) {
      mm.getAnswer().setProperty("sound", sound);
    }
    mm.sendManageEffect();
  }
  
  protected boolean reshow()
  {
    MissionManager mm = new MissionManager(getResponder(), this.lastTitle, this.lastQuestion, this.target, this.targName, this.missionRulerId);
    
    cloneValues(mm);
    mm.level = this.level;
    mm.lastQuestion = this.lastQuestion;
    mm.lastTitle = this.lastTitle;
    mm.sbacks = this.sbacks;
    switch (this.level)
    {
    case 0: 
      mm.sendQuestion();
      return true;
    case 1: 
    case 2: 
      mm.setAnswer(getAnswer());
      mm.sendManageMission();
      return true;
    case 5: 
      mm.sendMissionList();
      return true;
    case 3: 
    case 4: 
      mm.setAnswer(getAnswer());
      mm.sendManageTrigger();
      return true;
    case 6: 
      mm.sendTriggerList();
      return true;
    case 7: 
    case 8: 
      mm.setAnswer(getAnswer());
      mm.sendManageEffect();
      return true;
    case 9: 
      mm.sendEffectList();
      return true;
    }
    return false;
  }
  
  private void sendManageTrigger()
  {
    MissionTrigger trg = null;
    String name = "";
    boolean inactive = false;
    String desc = "";
    int itemUsedId = -10;
    int actionId = 0;
    long trgTarget = 0L;
    boolean useCurrentTarget = true;
    String tgtString = "";
    boolean spawnpoint = false;
    String trigsecs = "0";
    int missionRequired = 0;
    String stateFromString = "0.0";
    String stateToString = "0.0";
    if (getAnswer() != null)
    {
      name = getStringProp("name");
      inactive = getBooleanProp("inactive");
      desc = getStringProp("desc");
      
      itemUsedId = indexItemTemplate("onItemCreatedId", "Item Created");
      actionId = indexActionId("actionId", "action");
      trgTarget = getLongProp("targetid");
      useCurrentTarget = getBooleanProp("useCurrentTarget");
      tgtString = MissionTriggers.getTargetAsString(getResponder(), trgTarget);
      spawnpoint = getBooleanProp("spawnpoint");
      trigsecs = getStringProp("seconds");
      missionRequired = indexMission("missionRequired", "available missions");
      stateFromString = getStringProp("stateFrom");
      stateToString = getStringProp("stateTo");
    }
    else if (this.triggerId > 0)
    {
      trg = MissionTriggers.getTriggerWithId(this.triggerId);
      if (trg != null)
      {
        name = trg.getName();
        inactive = trg.isInactive();
        desc = trg.getDescription();
        itemUsedId = trg.getItemUsedId();
        actionId = trg.getOnActionPerformed();
        trgTarget = trg.getTarget();
        useCurrentTarget = trgTarget == this.currentTargetId;
        tgtString = MissionTriggers.getTargetAsString(getResponder(), trg.getTarget());
        if (getResponder().getPower() > 0) {
          spawnpoint = trg.isSpawnPoint();
        }
        trigsecs = Integer.toString(trg.getSeconds());
        missionRequired = trg.getMissionRequired();
        stateFromString = Float.toString(trg.getStateRequired());
        stateToString = Float.toString(trg.getStateEnd());
      }
    }
    else
    {
      tgtString = "None";
    }
    String currentString = MissionTriggers.getTargetAsString(getResponder(), this.currentTargetId);
    
    StringBuilder buf = new StringBuilder();
    buf.append("border{border{size=\"20,20\";null;null;varray{rescale=\"true\";harray{label{type='bold';text=\"" + this.question + "    \"};label{type=\"bolditalic\";" + "color=\"255,127,127\"" + ";text=\"" + this.errorText + "\"}}}harray{button{id=\"back\";text=\"Back\"};label{text=\"  \"}}null;}null;scroll{vertical=\"true\";horizontal=\"false\";varray{rescale=\"true\";passthrough{id=\"id\";text=\"" + 
    
      getId() + "\"}");
    
    buf.append("harray{label{text=\"Name (max 40 chars)\"};input{id=\"name\";maxchars=\"40\";text=\"" + name + "\"};label{text=\" \"};checkbox{id=\"inactive\";selected=\"" + inactive + "\"};label{text=\"Inactive \"};}");
    
    buf.append("label{text=\"Description (max 100 chars)\"};");
    buf.append("input{id=\"intro\";maxchars=\"400\"text=\"" + desc + "\";maxlines=\"1\"};");
    buf.append("text{text=\"\"}");
    buf.append("header{text=\"How triggered\"}");
    
    buf.append("harray{label{text=\"On item used\"};" + 
    
      dropdownItemTemplates("onItemCreatedId", itemUsedId, false) + "}");
    
    String curColour = "";
    if ((trgTarget == this.currentTargetId) && (trgTarget > 0L)) {
      curColour = "color=\"140,140,255\";";
    }
    if (getResponder().getPower() > 0)
    {
      buf.append("harray{label{text=\"Action performed\"};" + 
      
        dropdownActions("actionId", (short)actionId) + "label{text=\" on target \"};label{" + curColour + "text=\"" + tgtString + "\"};}");
      
      buf.append("harray{checkbox{text=\"\";id=\"useCurrentTarget\";selected=\"" + useCurrentTarget + "\"}label{text=\"Use current: \"};label{" + "color=\"140,140,255\";" + "text=\"" + currentString + "\"};label{text=\" Current Id: \"};input{id=\"targetid\";text=\"" + this.currentTargetId + "\";}}");
    }
    else
    {
      buf.append("harray{label{text=\"Action performed\"};" + 
      
        dropdownActions("actionId", (short)actionId) + "label{text=\" on target \"};label{" + curColour + "text=\"" + tgtString + "\"};}");
      
      buf.append("harray{checkbox{text=\"\";id=\"useCurrentTarget\";selected=\"" + useCurrentTarget + "\"}label{text=\"Use current: \"};label{" + "color=\"140,140,255\";" + "text=\"" + currentString + "\"};passthrough{id=\"targetid\";text=\"" + trgTarget + "\"}}");
    }
    buf.append("text{text=\"\"}");
    buf.append("header{text=\"General\"}");
    buf.append("harray{" + (
      getResponder().getPower() > 0 ? "label{text=\"Target is Spawn Point \"};checkbox{id=\"spawnpoint\";selected=\"" + spawnpoint + "\"};" : "") + "label{text=\"Seconds to trigger \"};input{id=\"seconds\";text=\"" + trigsecs + "\";maxchars=\"2\"};}");
    
    buf.append("harray{label{text=\"Trigger Mission \"};" + 
    
      dropdownMissions("missionRequired", missionRequired, true) + "}");
    
    buf.append("harray{label{text=\"Triggered state from \"};input{id=\"stateFrom\";maxchars=\"5\";text=\"" + stateFromString + "\"};label{text=\" to \"};input{id=\"stateTo\";maxchars=\"5\";text=\"" + stateToString + "\"};label{" + "color=\"140,140,255\";" + "text=\"  See Note 4.\";hover=\"The state 'to' is only valid if greater than the 'from'. Leave 'to' as '0.0' if not required.\"}label{text=\"  % Use '.' for decimals.\"}};");
    
    buf.append("text{text=\"\"}");
    if (this.level == 4)
    {
      buf.append(appendChargeInfo());
      buf.append("harray{button{text=\"Create Trigger\";id=\"createTrigger\"};}");
    }
    else
    {
      buf.append("harray{button{text=\"Update Trigger\";id=\"updateTrigger\"};label{text=\"  \"};button{text=\"Delete Trigger\";id=\"deleteTrigger\"hover=\"This will delete " + name + "\";confirm=\"You are about to delete " + name + ".\";question=\"Do you really want to do that?\"};label{text=\"  \"};button{id=\"cloneTrigger\";text=\"Clone Trigger\"hover=\"This will show a copy of this trigger for creation\";};}");
    }
    buf.append("text{type=\"bold\";text=\"Notes:\"}");
    buf.append("text{type=\"italic\";text=\"1. Creating a trigger on a tile with the action 'Step On' will disregard the item used.\"}");
    buf.append("text{type=\"italic\";text=\"2. Using the 'Create' action will only trigger when the selected item is manufactured or finished by polishing.\"}");
    buf.append("text{type=\"italic\";text=\"3. Be careful with restartable and second chance missions. They can really mess things up.\"}");
    buf.append("text{type=\"italic\";text=\"4. The state 'to' is only valid if greater than the 'from'. Leave 'to' as '0.0' if not required.\"}");
    if (trg != null)
    {
      buf.append("text{type=\"italic\";text=\"5. A trigger effect that triggers on Not Started (0) will start a new mission and set its state to 1.\"}");
      buf.append("text{type=\"italic\";text=\"6. A trigger effect may increase the state of a mission (does not have to be the mission that this trigger is attached to).\"}");
      buf.append("text{type=\"italic\";text=\"7. If the state of a mission is higher than 0 it is considered started.\"}");
      buf.append("text{text=\"    If it is 100.0% the mission is finished. If the state is set to -1.0 it has failed.\"}");
      
      buf.append("label{type=\"bold\";text=\"- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -\"}");
      buf.append("harray{label{type=\"bold\";text=\"Effects:     \"}button{id=\"createEffect\";text=\"Create New Effect\"};label{text=\"  All Effects\"}" + 
      
        dropdownUnlinkedEffects("linkEffects", 0) + "label{text=\" \"}button{id=\"linkEffect\";text=\"Link Effect\"};}");
      
      MissionTrigger[] trigs = { trg };
      TriggerEffect[] teffs = TriggerEffects.getFilteredEffects(trigs, getResponder(), this.showE, this.incEInactive, this.dontListMine, this.listMineOnly, this.listForUser, false);
      if (teffs.length > 0)
      {
        Arrays.sort(teffs);
        buf.append("table{rows=\"1\";cols=\"7\";");
        buf.append("label{text=\"\"};label{text=\"Id\"};label{text=\"Name\"};label{text=\"+State\"};label{text=\"Type\"};label{text=\"\"};label{text=\"\"};");
        for (TriggerEffect lEff : teffs)
        {
          String colour = "color=\"127,255,127\"";
          String hover = ";hover=\"Active\"";
          if (lEff.isInactive())
          {
            colour = "color=\"255,127,127\"";
            hover = ";hover=\"Inactive\"";
          }
          else if (lEff.hasTargetOf(this.currentTargetId, getResponder()))
          {
            colour = "color=\"140,140,255\";";
            hover = ";hover=\"Current Target\"";
          }
          String clrtxt = "label{text=\"         \";hover=\"no text to clear\"}";
          if (!lEff.getTextDisplayed().isEmpty()) {
            clrtxt = "harray{label{text=\" \"};button{id=\"clrE" + lEff.getId() + "\";text=\"Clear Text\"};}";
          }
          buf.append("harray{button{id=\"edtE" + lEff
            .getId() + "\";text=\"Edit\"};label{text=\" \"};};label{" + colour + "text=\"" + lEff
            
            .getId() + " \"" + hover + "};label{" + colour + "text=\"" + lEff
            .getName() + " \"" + hover + "};label{" + colour + "text=\"" + lEff
            .getMissionStateChange() + " \"" + hover + "};label{" + colour + "text=\"" + lEff
            .getType() + " \"" + hover + "};" + clrtxt + "harray{label{text=\" \"};button{id=\"unlE" + lEff
            
            .getId() + "\";text=\"Unlink\"};};");
        }
        buf.append("}");
      }
      else
      {
        buf.append("label{text=\"No trigger effects found\"}");
      }
    }
    buf.append("}};null;null;}");
    if (trg == null) {
      getResponder().getCommunicator().sendBml(600, 400, true, true, buf.toString(), 200, 200, 200, this.title);
    } else {
      getResponder().getCommunicator().sendBml(630, 600, true, true, buf.toString(), 200, 200, 200, this.title);
    }
  }
  
  private long indexExistingItem(String key, long existingItem)
  {
    long ans = 0L;
    String sIndex = getStringProp(key);
    if ((sIndex != null) && (!sIndex.isEmpty())) {
      if (getResponder().getPower() > 0) {
        try
        {
          long newexistingItem = Long.parseLong(sIndex);
          if (newexistingItem != existingItem)
          {
            try
            {
              Item old = Items.getItem(existingItem);
              getResponder().getInventory().insertItem(old);
              getResponder().getCommunicator().sendNormalServerMessage("Your old item was returned.");
            }
            catch (NoSuchItemException nsi)
            {
              getResponder().getCommunicator().sendNormalServerMessage("The previous reward item could not be located.");
            }
            ans = newexistingItem;
          }
        }
        catch (NumberFormatException nfe)
        {
          if (this.errorText.isEmpty()) {
            this.errorText = "Failed to parse value for existingItem.";
          }
        }
      } else {
        try
        {
          int index = Integer.parseInt(sIndex);
          if (index > 0)
          {
            ans = ((Item)this.ritems.get(index - 1)).getWurmId();
          }
          else if (existingItem > 0L)
          {
            Items.destroyItem(existingItem);
            getResponder().getCommunicator().sendNormalServerMessage("The old reward item was lost.");
            ans = 0L;
          }
        }
        catch (NumberFormatException nfe)
        {
          if (this.errorText.isEmpty()) {
            this.errorText = "Failed to parse value for existingItem.";
          }
        }
      }
    }
    return ans;
  }
  
  private int indexItemTemplate(String key, String type)
  {
    int ans = 0;
    String sIndex = getStringProp(key);
    if ((sIndex != null) && (!sIndex.isEmpty())) {
      try
      {
        int index = Integer.parseInt(sIndex);
        if (index != 0) {
          try
          {
            ans = ((ItemTemplate)this.itemplates.get(index - 1)).getTemplateId();
          }
          catch (Exception ex)
          {
            if (this.errorText.isEmpty()) {
              this.errorText = ("Unknown " + type + "?");
            }
          }
        }
      }
      catch (NumberFormatException nfe)
      {
        if (this.errorText.isEmpty()) {
          this.errorText = ("Failed to parse value for " + type + ".");
        }
      }
    }
    return ans;
  }
  
  private int indexLayer(String key, String type)
  {
    if (indexBoolean(key, type)) {
      return 0;
    }
    return -1;
  }
  
  private boolean indexBoolean(String key, String type)
  {
    String sIndex = getStringProp(key);
    if ((sIndex != null) && (!sIndex.isEmpty())) {
      try
      {
        int index = Integer.parseInt(sIndex);
        return index == 0;
      }
      catch (NumberFormatException nfe)
      {
        if (this.errorText.isEmpty()) {
          this.errorText = ("Failed to parse value for " + type + ".");
        }
      }
    }
    return false;
  }
  
  private int indexSpecialEffect(String key, String type, boolean doChecks)
  {
    int ans = 0;
    String sIndex = getStringProp(key);
    if ((sIndex != null) && (!sIndex.isEmpty()))
    {
      try
      {
        int index = Integer.parseInt(sIndex);
        try
        {
          ans = ((SpecialEffects)this.effectsAvail.get(index)).getId();
        }
        catch (Exception ex)
        {
          if (this.errorText.isEmpty()) {
            this.errorText = ("Unknown " + type + "?");
          }
        }
      }
      catch (NumberFormatException nfe)
      {
        if (this.errorText.isEmpty()) {
          this.errorText = ("Failed to parse value for " + type + ".");
        }
      }
      if ((doChecks) && (ans != 0))
      {
        if (ans == 1) {
          if (!maySetToOpen(getResponder(), this.currentTargetId))
          {
            ans = 0;
            if (this.errorText.isEmpty()) {
              this.errorText = "You may not set that to open. Try a fence gate you can open.";
            }
          }
        }
        if (ans != 0)
        {
          SpecialEffects sp = SpecialEffects.getEffect(ans);
          if (sp.getPowerRequired() > getResponder().getPower())
          {
            ans = 0;
            if (this.errorText.isEmpty()) {
              this.errorText = "Invalid effect. Set to no effect.";
            }
          }
        }
      }
    }
    return ans;
  }
  
  private int indexSkillNum(String key, String type)
  {
    int ans = 0;
    String sIndex = getStringProp(key);
    if ((sIndex != null) && (!sIndex.isEmpty())) {
      try
      {
        int index = Integer.parseInt(sIndex);
        if (index != 0) {
          try
          {
            ans = ((SkillTemplate)this.stemplates.get(index - 1)).getNumber();
          }
          catch (Exception ex)
          {
            if (this.errorText.isEmpty()) {
              this.errorText = ("Unknown " + type + "?");
            }
          }
        }
      }
      catch (NumberFormatException nfe)
      {
        if (this.errorText.isEmpty()) {
          this.errorText = ("Failed to parse value for " + type + ".");
        }
      }
    }
    return ans;
  }
  
  private float indexSkillVal(String key, String type)
  {
    float ans = 0.0F;
    String sIndex = getStringProp(key);
    if ((sIndex != null) && (!sIndex.isEmpty())) {
      try
      {
        int index = Integer.parseInt(sIndex);
        if (index != 0)
        {
          if (index == 1) {
            return 0.001F;
          }
          if (getResponder().getPower() > 0)
          {
            switch (index)
            {
            case 2: 
              return 0.002F;
            case 3: 
              return 0.01F;
            case 4: 
              return 0.05F;
            case 5: 
              return 0.1F;
            case 6: 
              return 1.0F;
            case 7: 
              return 10.0F;
            case 8: 
              return 20.0F;
            }
            return 0.001F;
          }
          return 0.001F;
        }
      }
      catch (NumberFormatException nfe)
      {
        if (this.errorText.isEmpty()) {
          this.errorText = ("Failed to parse value for " + type + ".");
        }
      }
    }
    return ans;
  }
  
  private float indexStateChange(String key, int specialEffect)
  {
    float val = getFloatProp(key, -101.0F, 100.0F, true);
    if ((val != 0.0F) && (specialEffect == 1))
    {
      val = 0.0F;
      if (this.errorText.isEmpty()) {
        this.errorText = "State change can only be 0 for effect OPEN DOOR because it triggers several times and blocks half through passing the door.";
      }
    }
    return val;
  }
  
  private int indexTileType(String key)
  {
    int index = getIntProp("newTileType", 0, 8, false);
    switch (index)
    {
    case 0: 
      return 0;
    case 1: 
      return Tiles.Tile.TILE_TREE.id;
    case 2: 
      return Tiles.Tile.TILE_DIRT.id;
    case 3: 
      return Tiles.Tile.TILE_GRASS.id;
    case 4: 
      return Tiles.Tile.TILE_FIELD.id;
    case 5: 
      return Tiles.Tile.TILE_SAND.id;
    case 6: 
      return Tiles.Tile.TILE_MYCELIUM.id;
    case 7: 
      return Tiles.Tile.TILE_CAVE_WALL.id;
    case 8: 
      return Tiles.Tile.TILE_CAVE_WALL_ORE_IRON.id;
    }
    return 0;
  }
  
  private int indexTrigger(String key, String type)
  {
    int ans = 0;
    String sIndex = getStringProp(key);
    if ((sIndex != null) && (!sIndex.isEmpty())) {
      try
      {
        int index = Integer.parseInt(sIndex);
        if (index != 0) {
          try
          {
            ans = ((MissionTrigger)this.mtriggers.get(index - 1)).getId();
          }
          catch (Exception ex)
          {
            if (this.errorText.isEmpty()) {
              this.errorText = ("Unknown " + type + "?");
            }
          }
        }
      }
      catch (NumberFormatException nfe)
      {
        if (this.errorText.isEmpty()) {
          this.errorText = ("Failed to parse value for " + type + ".");
        }
      }
    }
    return ans;
  }
  
  private int indexUnlinkedTrigger(String key, String type)
  {
    int ans = 0;
    String sIndex = getStringProp(key);
    if ((sIndex != null) && (!sIndex.isEmpty())) {
      try
      {
        int index = Integer.parseInt(sIndex);
        if (index != 0) {
          try
          {
            ans = ((MissionTrigger)this.utriggers.get(index - 1)).getId();
          }
          catch (Exception ex)
          {
            if (this.errorText.isEmpty()) {
              this.errorText = ("Unknown " + type + "?");
            }
          }
        }
      }
      catch (NumberFormatException nfe)
      {
        if (this.errorText.isEmpty()) {
          this.errorText = ("Failed to parse value for " + type + ".");
        }
      }
    }
    return ans;
  }
  
  private int indexEffect(String key, String type)
  {
    int ans = 0;
    String sIndex = getStringProp(key);
    if ((sIndex != null) && (!sIndex.isEmpty())) {
      try
      {
        int index = Integer.parseInt(sIndex);
        if (index != 0) {
          try
          {
            ans = ((TriggerEffect)this.teffects.get(index - 1)).getId();
          }
          catch (Exception ex)
          {
            if (this.errorText.isEmpty()) {
              this.errorText = ("Unknown " + type + "?");
            }
          }
        }
      }
      catch (NumberFormatException nfe)
      {
        if (this.errorText.isEmpty()) {
          this.errorText = ("Failed to parse value for " + type + ".");
        }
      }
    }
    return ans;
  }
  
  private int indexUnlinkedEffect(String key, String type)
  {
    int ans = 0;
    String sIndex = getStringProp(key);
    if ((sIndex != null) && (!sIndex.isEmpty())) {
      try
      {
        int index = Integer.parseInt(sIndex);
        if (index != 0) {
          try
          {
            ans = ((TriggerEffect)this.ueffects.get(index - 1)).getId();
          }
          catch (Exception ex)
          {
            if (this.errorText.isEmpty()) {
              this.errorText = ("Unknown " + type + "?");
            }
          }
        }
      }
      catch (NumberFormatException nfe)
      {
        if (this.errorText.isEmpty()) {
          this.errorText = ("Failed to parse value for " + type + ".");
        }
      }
    }
    return ans;
  }
  
  private int indexAchievementId(String key, String type)
  {
    int ans = 0;
    String sIndex = getStringProp(key);
    if ((sIndex != null) && (!sIndex.isEmpty())) {
      try
      {
        int index = Integer.parseInt(sIndex);
        if (index != 0) {
          try
          {
            ans = ((AchievementTemplate)this.myAchievements.get(index - 1)).getNumber();
          }
          catch (Exception ex)
          {
            if (this.errorText.isEmpty()) {
              this.errorText = ("Unknown " + type + "?");
            }
          }
        }
      }
      catch (NumberFormatException nfe)
      {
        if (this.errorText.isEmpty()) {
          this.errorText = ("Failed to parse value for " + type + ".");
        }
      }
    }
    return ans;
  }
  
  private int indexActionId(String key, String type)
  {
    int ans = 0;
    String sIndex = getStringProp(key);
    if ((sIndex != null) && (!sIndex.isEmpty())) {
      try
      {
        int index = Integer.parseInt(sIndex);
        if (index != 0) {
          try
          {
            ans = ((ActionEntry)this.actionEntries.get(index - 1)).getNumber();
          }
          catch (Exception ex)
          {
            if (this.errorText.isEmpty()) {
              this.errorText = ("Unknown " + type + "?");
            }
          }
        }
      }
      catch (NumberFormatException nfe)
      {
        if (this.errorText.isEmpty()) {
          this.errorText = ("Failed to parse value for " + type + ".");
        }
      }
    }
    return ans;
  }
  
  private int indexMission(String key, String type)
  {
    int ans = 0;
    String sIndex = getStringProp(key);
    if ((sIndex != null) && (!sIndex.isEmpty())) {
      try
      {
        int index = Integer.parseInt(sIndex);
        if (index != 0) {
          try
          {
            ans = ((Mission)this.missionsAvail.get(index - 1)).getId();
          }
          catch (Exception ex)
          {
            if (this.errorText.isEmpty()) {
              this.errorText = "Unknown mission?";
            }
          }
        }
      }
      catch (NumberFormatException nfe)
      {
        if (this.errorText.isEmpty()) {
          this.errorText = ("Failed to parse value for " + type + ".");
        }
      }
    }
    return ans;
  }
  
  private int indexCreatureTemplate(String key, String type)
  {
    int ans = 0;
    String sIndex = getStringProp(key);
    if ((sIndex != null) && (!sIndex.isEmpty())) {
      try
      {
        int index = Integer.parseInt(sIndex);
        if (index != 0) {
          try
          {
            ans = ((CreatureTemplate)this.creaturesAvail.get(index - 1)).getTemplateId();
          }
          catch (Exception ex)
          {
            if (this.errorText.isEmpty()) {
              this.errorText = "Unknown creature?";
            }
          }
        }
      }
      catch (NumberFormatException nfe)
      {
        if (this.errorText.isEmpty()) {
          this.errorText = ("Failed to parse value for " + type + ".");
        }
      }
    }
    return ans;
  }
  
  private byte indexCreatureType(String key, String type)
  {
    byte ans = 0;
    String sIndex = getStringProp(key);
    if ((sIndex != null) && (!sIndex.isEmpty())) {
      try
      {
        int index = Byte.parseByte(sIndex);
        try
        {
          ans = ((Byte)this.creaturesTypes.get(index)).byteValue();
        }
        catch (Exception ex)
        {
          if (this.errorText.isEmpty()) {
            this.errorText = ("Unknown " + type + "?");
          }
        }
      }
      catch (NumberFormatException nfe)
      {
        if (this.errorText.isEmpty()) {
          this.errorText = ("Failed to parse value for " + type + ".");
        }
      }
    }
    return ans;
  }
  
  private void sendManageEffect()
  {
    String name = "";
    boolean inactive = false;
    String desc = "";
    
    boolean startSkill = false;
    boolean stopSkill = false;
    boolean destroysInventory = false;
    
    int itemTemplate = 0;
    byte itemMaterial = 0;
    boolean newbieItem = false;
    String sql = "0";
    String snumbers = "0";
    String sbytevalue = "0";
    String sexistingItem = "0";
    String sexistingContainer = "0";
    boolean destroysTarget = false;
    
    int rewardSkillNum = 0;
    String srewardSkillVal = "0";
    
    String sModifyTileX = "0";
    String sModifyTileY = "0";
    int newTileType = 0;
    String snewTileData = "0";
    
    String sSpawnTileX = "0";
    String sSpawnTileY = "0";
    int creatureSpawn = 0;
    String sCreatureAge = "0";
    String creatureName = "";
    byte creatureType = 0;
    
    String sTeleportX = "0";
    String sTeleportY = "0";
    int teleportLayer = 0;
    int specialEffect = 0;
    
    int achievement = 0;
    
    int missionAffected = 0;
    String sStateChange = "0.0";
    
    String soundName = "";
    
    int missionToActivate = 0;
    int missionToDeactivate = 0;
    int triggerToActivate = 0;
    int triggerToDeactivate = 0;
    int effectToActivate = 0;
    int effectToDeactivate = 0;
    
    String sWinSizeX = "0";
    String sWinSizeY = "0";
    String topText = "";
    String textDisplayed = "";
    if (getAnswer() != null)
    {
      name = getStringProp("name");
      inactive = getBooleanProp("inactive");
      desc = getStringProp("desc");
      
      startSkill = getBooleanProp("startSkill");
      stopSkill = getBooleanProp("stopSkill");
      destroysInventory = getBooleanProp("destroysInventory");
      
      itemTemplate = indexItemTemplate("itemReward", "Reward Item");
      itemMaterial = getByteProp("itemMaterial");
      newbieItem = getBooleanProp("newbie");
      sql = getStringProp("ql");
      snumbers = getStringProp("numbers");
      sbytevalue = getStringProp("bytevalue");
      sexistingItem = getStringProp("existingItem");
      sexistingContainer = getStringProp("rewardTargetContainerId");
      destroysTarget = getBooleanProp("destroysTarget");
      
      rewardSkillNum = indexSkillNum("rewardSkillNum", "Reward Skill Number");
      srewardSkillVal = getStringProp("rewardSkillVal");
      
      sModifyTileX = getStringProp("modifyTileX");
      sModifyTileY = getStringProp("modifyTileY");
      newTileType = getIntProp("newTileType");
      snewTileData = getStringProp("newTileData");
      
      sSpawnTileX = getStringProp("spawnTileX");
      sSpawnTileY = getStringProp("spawnTileY");
      creatureSpawn = indexCreatureTemplate("creatureSpawn", "Creature Spawn");
      sCreatureAge = getStringProp("creatureAge");
      creatureName = getStringProp("creatureName");
      creatureType = indexCreatureType("creatureType", "Creature Type");
      
      sTeleportX = getStringProp("teleportTileX");
      sTeleportY = getStringProp("teleportTileY");
      teleportLayer = indexLayer("teleportLayer", "Teleport layer");
      specialEffect = indexSpecialEffect("specialEffect", "Special Effect", false);
      
      achievement = getIntProp("achievement");
      
      missionAffected = indexMission("missionId", "mission id");
      sStateChange = getStringProp("missionStateChange");
      
      soundName = getStringProp("sound");
      
      missionToActivate = indexMission("missionToActivate", "mission to activate");
      missionToDeactivate = indexMission("missionToDeactivate", "mission to deactivate");
      triggerToActivate = indexTrigger("triggerToActivate", "trigger to activate");
      triggerToDeactivate = indexTrigger("triggerToDeactivate", "trigger to deactivate");
      effectToActivate = indexEffect("effectToActivate", "effect to activate");
      effectToDeactivate = indexEffect("effectToDeactivate", "effect to deactivate");
      
      sWinSizeX = getStringProp("winsizeX");
      sWinSizeY = getStringProp("winsizeY");
      topText = getStringProp("toptext");
      textDisplayed = getStringProp("textdisplayed");
    }
    else if (this.effectId > 0)
    {
      TriggerEffect eff = TriggerEffects.getTriggerEffect(this.effectId);
      if (eff != null)
      {
        if ((getResponder().getLogger() != null) && (getResponder().getPower() > 0) && (eff != null) && 
          (eff.getName() != null))
        {
          String buildLogString = getResponder() + ": Viewing trigger EFFECT settings for effect with name: " + eff.getName();
          buildLogString = buildLogString + (eff.getDescription() != null ? " and description = " + eff.getDescription() : "");
          getResponder().getLogger().info(buildLogString);
        }
        this.creatorType = eff.getCreatorType();
        name = eff.getName();
        inactive = eff.isInactive();
        desc = eff.getDescription();
        
        startSkill = eff.isStartSkillgain();
        stopSkill = eff.isStopSkillgain();
        destroysInventory = eff.destroysInventory();
        
        itemTemplate = eff.getRewardItem();
        itemMaterial = eff.getItemMaterial();
        newbieItem = eff.isNewbieItem();
        sql = Integer.toString(eff.getRewardQl());
        snumbers = Integer.toString(eff.getRewardNumbers());
        sbytevalue = Byte.toString(eff.getRewardByteValue());
        sexistingItem = Long.toString(eff.getExistingItemReward());
        sexistingContainer = Long.toString(eff.getRewardTargetContainerId());
        destroysTarget = eff.destroysTarget();
        
        rewardSkillNum = eff.getRewardSkillNum();
        srewardSkillVal = Float.toString(eff.getRewardSkillModifier());
        
        sModifyTileX = Integer.toString(eff.getModifyTileX());
        sModifyTileY = Integer.toString(eff.getModifyTileY());
        newTileType = eff.getNewTileType();
        snewTileData = Byte.toString(eff.getNewTileData());
        
        sSpawnTileX = Integer.toString(eff.getSpawnTileX());
        sSpawnTileY = Integer.toString(eff.getSpawnTileY());
        creatureSpawn = eff.getCreatureSpawn();
        sCreatureAge = Integer.toString(eff.getCreatureAge());
        creatureName = eff.getCreatureName();
        creatureType = eff.getCreatureType();
        
        sTeleportX = Integer.toString(eff.getTeleportX());
        sTeleportY = Integer.toString(eff.getTeleportY());
        teleportLayer = eff.getTeleportLayer();
        specialEffect = eff.getSpecialEffectId();
        
        achievement = eff.getAchievementId();
        
        missionAffected = eff.getMissionId();
        sStateChange = Float.toString(eff.getMissionStateChange());
        
        soundName = eff.getSoundName();
        
        missionToActivate = eff.getMissionToActivate();
        missionToDeactivate = eff.getMissionToDeActivate();
        triggerToActivate = eff.getTriggerToActivate();
        triggerToDeactivate = eff.getTriggerToDeActivate();
        effectToActivate = eff.getEffectToActivate();
        effectToDeactivate = eff.getEffectToDeActivate();
        
        sWinSizeX = Integer.toString(eff.getWindowSizeX());
        sWinSizeY = Integer.toString(eff.getWindowSizeY());
        topText = eff.getTopText();
        textDisplayed = eff.getTextDisplayed();
      }
      else
      {
        this.creatorType = 0;
      }
    }
    else
    {
      this.creatorType = 0;
    }
    StringBuilder buf = new StringBuilder();
    buf.append("border{border{size=\"20,20\";null;null;varray{rescale=\"true\";harray{label{type='bold';text=\"" + this.question + "    \"};label{type=\"bolditalic\";" + "color=\"255,127,127\"" + ";text=\"" + this.errorText + "\"}}}harray{button{id=\"back\";text=\"Back\"};label{text=\"  \"}}null;}null;scroll{vertical=\"true\";horizontal=\"false\";varray{rescale=\"true\";passthrough{id=\"id\";text=\"" + 
    
      getId() + "\"}");
    
    buf.append("harray{label{text=\"Name (max 40 chars)\"};input{id=\"name\";maxchars=\"40\";text=\"" + name + "\"};label{text=\" \"};checkbox{id=\"inactive\";selected=\"" + inactive + "\";hover=\"Not sure this is implemented currently\"};label{text=\"Inactive \";hover=\"Not sure this is implemented currently\"};}");
    
    buf.append("label{text=\"Description (max 400 chars)\"};");
    buf.append("input{id=\"desc\";maxchars=\"400\";text=\"" + desc + "\";maxlines=\"4\"};");
    Item i;
    if (getResponder().getPower() > 0)
    {
      buf.append("text{text=\"\"}");
      buf.append("header{text=\"Dangerous\"};");
      buf.append("harray{checkbox{id=\"startSkill\";text=\"Resumes skill gain.  \";selected=\"" + startSkill + "\"};checkbox{id=\"stopSkill\";text=\"Stops skill gain.  \";selected=\"" + stopSkill + "\"};checkbox{id=\"destroysInventory\";text=\"Destroys all (!) carried items.\";selected=\"" + destroysInventory + "\"};}");
      
      buf.append("text{text=\"\"}");
      buf.append("header{text=\"Reward\"};");
      buf.append("label{type=\"bolditalic\";text=\"Item\"}");
      buf.append("harray{label{text=\"New reward item\"};" + 
      
        dropdownItemTemplates("itemReward", itemTemplate, true) + "label{text=\" Material \"}" + 
        
        dropdownItemMaterials("itemMaterial", itemMaterial) + "label{text=\" \"}checkbox{id=\"newbieItem\";text=\"Tutorial/newbie item.\";selected=\"" + newbieItem + "\"};}");
      
      buf.append("harray{label{text=\"Quality \"};input{id=\"ql\";text=\"" + sql + "\";maxchars=\"2\"};label{text=\" Numbers \"};input{id=\"numbers\";text=\"" + snumbers + "\";maxchars=\"2\"};label{text=\" Byte value \"};input{id=\"bytevalue\";text=\"" + sbytevalue + "\";maxchars=\"3\"};}");
      
      buf.append("harray{label{text=\"Existing item reward id \"};input{id=\"existingItem\";text=\"" + sexistingItem + "\"};label{text=\"Existing container id to insert into \"};input{id=\"rewardTargetContainerId\";text=\"" + sexistingContainer + "\"};}");
      
      String hoverDel = ";hover=\"If you select Destroy target, the target that triggers this effect will be destroyed:\"";
      buf.append("harray{checkbox{id=\"destroysTarget\";selected=\"" + destroysTarget + "\"};label{text=\"Destroy target \"" + ";hover=\"If you select Destroy target, the target that triggers this effect will be destroyed:\"" + "};}");
      
      buf.append("label{type=\"bolditalic\";text=\"Skill\"}");
      buf.append("harray{label{text=\"New skill Reward\"};" + 
      
        dropdownSkillTemplates("rewardSkillNum", rewardSkillNum) + "label{text=\"Skill Reward value\"};" + 
        
        dropdownSkillValues("rewardSkillVal", srewardSkillVal) + "}");
      
      buf.append("text{text=\"\"}");
      buf.append("header{text=\"Tiles\"};");
      buf.append("label{type=\"bolditalic\";text=\"Modify\"}");
      buf.append("harray{label{text=\"Modify Tile X \"};input{id=\"modifyTileX\";text=\"" + sModifyTileX + "\";maxchars=\"4\"};label{text=\" Y \"};input{id=\"modifyTileY\";text=\"" + sModifyTileY + "\";maxchars=\"4\"};label{text=\" Type \"};" + 
      
        dropdownTileTypes("newTileType", newTileType) + "label{text=\" Data \"};input{id=\"newTileData\";text=\"" + snewTileData + "\";maxchars=\"3\"};}");
      
      buf.append("label{type=\"bolditalic\";text=\"Spawn\"}");
      buf.append("harray{label{text=\"Spawn Tile X \"};input{id=\"spawnTileX\";text=\"" + sSpawnTileX + "\";maxchars=\"4\"};label{text=\" Y \"};input{id=\"spawnTileY\";text=\"" + sSpawnTileY + "\";maxchars=\"4\"};label{text=\" Template \"};" + 
      
        dropdownCreatureTemplates("creatureSpawn", creatureSpawn) + "}");
      
      buf.append("harray{label{text=\" Age \"};input{id=\"creatureAge\";text=\"" + sCreatureAge + "\";maxchars=\"2\"};label{text=\" Name (instead of template name) (max 20 chars) \"};input{id=\"creatureName\";text=\"" + creatureName + "\";maxchars=\"40\"};label{text=\" Type \"};" + 
      
        dropdownCreatureTypes("creatureType", creatureType) + "}");
      
      buf.append("label{type=\"bolditalic\";text=\"Teleport / Special\"}");
      buf.append("harray{label{text=\"Teleport/Special effect Tile X \"};input{id=\"teleportTileX\";text=\"" + sTeleportX + "\";maxchars=\"4\"};label{text=\" Y \"};input{id=\"teleportTileY\";text=\"" + sTeleportY + "\";maxchars=\"4\"};label{text=\"Surface\"};" + 
      
        dropdownBoolean("teleportLayer", teleportLayer >= 0) + "}");
    }
    else
    {
      buf.append("text{text=\"\"}");
      buf.append("header{text=\"Reward\"};");
      buf.append("label{type=\"bolditalic\";text=\"Item\"}");
      try
      {
        long itemReward = Long.parseLong(sexistingItem);
        if (itemReward > 0L)
        {
          i = Items.getItem(itemReward);
          
          buf.append("label{text=\"Existing item reward: " + i.getName() + "\"};");
          buf.append("passthrough{id=\"existingItem\";text=\"" + sexistingItem + "\"}");
        }
        else
        {
          buf.append("harray{label{text=\"Your Item Reward (ql)\"};" + 
          
            dropdownInventory("existingItem", itemReward) + "}");
        }
      }
      catch (NoSuchItemException nsi)
      {
        buf.append("harray{label{text=\"Existing item reward: \"};label{color=\"255,127,127\"text=\"Not Found!\"}}");
      }
      catch (NumberFormatException localNumberFormatException) {}
      buf.append("text{text=\"\"}");
      buf.append("header{text=\"Tiles\"};");
    }
    buf.append("harray{label{text=\"Special effect \"};" + 
    
      dropdownSpecialEffects("specialEffect", specialEffect) + "}");
    
    buf.append("label{type=\"bolditalic\";text=\"Achievement\"}");
    buf.append("harray{label{text=\"Trigger Achievement \"};" + 
    
      dropdownAchievements("achievement", achievement) + "}");
    
    buf.append("text{text=\"\"}");
    buf.append("header{text=\"General\"};");
    buf.append("label{type=\"bolditalic\";text=\"Mission\"}");
    
    String hoverState = ";hover=\"An effect may increase the state of any mission.\"";
    buf.append("harray{label{text=\"Mission state affected \";hover=\"An effect may increase the state of any mission.\"};" + 
    
      dropdownMissions("missionId", missionAffected, true) + "label{text=\"State change \"" + ";hover=\"An effect may increase the state of any mission.\"" + "};input{id=\"missionStateChange\";maxchars=\"5\";text=\"" + sStateChange + "\"};label{text=\"%. Use '.' for decimals.\"" + ";hover=\"An effect may increase the state of any mission.\"" + "}}");
    
    buf.append("label{type=\"bolditalic\";text=\"Sound\"}");
    buf.append("harray{label{text=\"Sound mapping (max 50 chars)\"};input{id=\"sound\";text=\"" + soundName + "\";maxchars=\"50\"};label{text=\" \"};button{id=\"playSound\";text=\"Play Sound\"}label{text=\" \"};button{id=\"listSounds\";text=\"Show Sound List\"}}");
    
    buf.append("label{type=\"bolditalic\";text=\"Activate / Deactivate\"}");
    buf.append("harray{label{text=\"Mission to activate \"};" + 
    
      dropdownMissions("missionToActivate", missionToActivate, false) + "label{text=\"Mission to deactivate \"};" + 
      
      dropdownMissions("missionToDeactivate", missionToDeactivate, false) + "}");
    
    buf.append("harray{label{text=\"Trigger to activate \"};" + 
    
      dropdownTriggers("triggerToActivate", triggerToActivate, true) + "label{text=\"Trigger to deactivate \"};" + 
      
      dropdownTriggers("triggerToDeactivate", triggerToDeactivate, false) + "}");
    
    buf.append("harray{label{text=\"Effect to activate \"};" + 
    
      dropdownEffects("effectToActivate", effectToActivate, true) + "label{text=\"Effect to deactivate \"};" + 
      
      dropdownEffects("effectToDeactivate", effectToDeactivate, false) + "}");
    
    buf.append("text{text=\"\"}");
    buf.append("header{text=\"Popup\"};");
    buf.append("harray{label{text=\"Window Size Width \"};input{id=\"winsizeX\";text=\"" + sWinSizeX + "\";maxchars=\"3\"};label{text=\" Height \"};input{id=\"winsizeY\";text=\"" + sWinSizeY + "\";maxchars=\"3\"};label{text=\"  \"};button{id=\"testText\";text=\"Verify Popup\";hover=\"Effect will be redisplayed after verification.\"}}");
    
    buf.append("label{text=\"Top Text displayed (max 1000 chars)\"}");
    buf.append("input{id=\"toptext\";maxchars=\"1000\";maxlines=\"3\";text=\"" + topText + "\"}");
    
    buf.append("label{text=\"Text displayed (Can be normal text or BML) (max 1000 chars)\"}");
    buf.append("input{id=\"textdisplayed\";maxchars=\"1000\";maxlines=\"6\";text=\"" + textDisplayed + "\"}");
    
    buf.append("text{text=\"\"}");
    if (this.level == 8)
    {
      buf.append(appendChargeInfo());
      buf.append("harray{button{id=\"createEffect\";text=\"Create Effect\";hover=\"After creation, this will redisplay this effect\"}}");
    }
    else
    {
      buf.append("harray{button{id=\"updateEffect\";text=\"Update Effect\"}label{text=\"  \"}button{id=\"deleteEffect\";text=\"Delete Effect\"hover=\"This will delete effect " + name + "\";confirm=\"You are about to delete effect " + name + ".\";question=\"Do you really want to do that?\"}label{text=\"  \"}button{id=\"cloneEffect\";text=\"Clone Effect\"hover=\"This will show a copy of this effect for creation\";}}");
    }
    if (this.level == 7)
    {
      buf.append("label{type=\"bold\";text=\"- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -\"}");
      buf.append("harray{label{type=\"bold\";text=\"Triggers:     \"}button{id=\"createTrigger\";text=\"Create New Trigger\"};label{text=\"  Triggers\"}" + 
      
        dropdownUnlinkedTriggers("trigs", 0) + "label{text=\" \"}button{id=\"linkTrigger\";text=\"Link Trigger\"};}");
      
      MissionTrigger[] trigs = Triggers2Effects.getTriggersForEffect(this.effectId, this.incTInactive);
      if (trigs.length > 0)
      {
        Arrays.sort(trigs);
        buf.append("table{rows=\"1\";cols=\"7\";");
        buf.append("label{text=\"\"};label{text=\"Id\"};label{text=\"Name\"};label{text=\"State\"};label{text=\"Action\"};label{text=\"Target\"};label{text=\"\"};");
        for (MissionTrigger trigger : trigs)
        {
          String colour = "color=\"127,255,127\"";
          String hover = ";hover=\"Active\"";
          if (trigger.isInactive())
          {
            colour = "color=\"255,127,127\"";
            hover = ";hover=\"Inactive\"";
          }
          else if (trigger.hasTargetOf(this.currentTargetId, getResponder()))
          {
            colour = "color=\"140,140,255\";";
            hover = ";hover=\"Current Target\"";
          }
          buf.append("harray{button{id=\"edtT" + trigger
            .getId() + "\";text=\"Edit\"};label{text=\"  \"}};label{" + colour + "text=\"" + trigger
            
            .getId() + " \"" + hover + "};label{" + colour + "text=\"" + trigger
            .getName() + " \"" + hover + "};label{" + colour + "text=\"" + trigger
            .getStateRange() + " \"" + hover + "};label{" + colour + "text=\"" + trigger
            .getActionString() + " \"" + hover + "};label{" + colour + "text=\"" + trigger
            .getTargetAsString(getResponder()) + "\"" + hover + "};harray{label{text=\"  \"}button{id=\"unlT" + trigger
            
            .getId() + "\";text=\"Unlink\"hover=\"This will unlink the trigger " + trigger
            .getName() + " from this effect\";};};");
        }
        buf.append("}");
      }
      else
      {
        buf.append("label{text=\"No triggers found\"}");
      }
    }
    buf.append("}};null;null;}");
    getResponder().getCommunicator().sendBml(600, 600, true, true, buf.toString(), 200, 200, 200, this.title);
  }
  
  private String dropdownTriggers(String id, int current, boolean reload)
  {
    StringBuilder buf = new StringBuilder();
    int def = 0;
    int counter = 0;
    buf.append("dropdown{id=\"" + id + "\";options=\"");
    buf.append("None");
    MissionTrigger[] triggers;
    if (reload)
    {
      triggers = MissionTriggers.getFilteredTriggers(getResponder(), current, this.creatorType, this.listForUser, this.dontListMine, this.listMineOnly);
      
      Arrays.sort(triggers);
      this.mtriggers.clear();
      for (MissionTrigger trig : triggers)
      {
        this.mtriggers.add(trig);
        counter++;
        buf.append(",");
        buf.append(trig.getName());
        if ((current > 0) && (current == trig.getId())) {
          def = counter;
        }
      }
    }
    else
    {
      for (MissionTrigger trig : this.mtriggers)
      {
        counter++;
        buf.append(",");
        buf.append(trig.getName());
        if ((current > 0) && (current == trig.getId())) {
          def = counter;
        }
      }
    }
    buf.append("\";default=\"" + def + "\"}");
    return buf.toString();
  }
  
  private String dropdownUnlinkedTriggers(String id, int show)
  {
    StringBuilder buf = new StringBuilder();
    buf.append("dropdown{id=\"" + id + "\";options=\"");
    buf.append("None");
    MissionTrigger[] utrigs = MissionTriggers.getFilteredTriggers(getResponder(), show, this.incTInactive, 0, 0);
    
    Arrays.sort(utrigs);
    this.utriggers.clear();
    for (MissionTrigger trig : utrigs)
    {
      this.utriggers.add(trig);
      buf.append(",");
      buf.append(trig.getName());
    }
    buf.append("\";default=\"0\"}");
    return buf.toString();
  }
  
  private String dropdownEffects(String id, int current, boolean reload)
  {
    StringBuilder buf = new StringBuilder();
    int def = 0;
    int counter = 0;
    buf.append("dropdown{id=\"" + id + "\";options=\"");
    buf.append("None");
    TriggerEffect[] effects;
    if (reload)
    {
      effects = TriggerEffects.getFilteredEffects(getResponder(), 0, this.incEInactive, this.dontListMine, this.listMineOnly, this.listForUser);
      
      Arrays.sort(effects);
      this.teffects.clear();
      for (TriggerEffect eff : effects)
      {
        this.teffects.add(eff);
        counter++;
        buf.append(",");
        buf.append(eff.getName());
        if ((current > 0) && (current == eff.getId())) {
          def = counter;
        }
      }
    }
    else
    {
      for (TriggerEffect eff : this.teffects)
      {
        counter++;
        buf.append(",");
        buf.append(eff.getName());
        if ((current > 0) && (current == eff.getId())) {
          def = counter;
        }
      }
    }
    buf.append("\";default=\"" + def + "\"}");
    return buf.toString();
  }
  
  private String dropdownUnlinkedEffects(String id, int show)
  {
    StringBuilder buf = new StringBuilder();
    buf.append("dropdown{id=\"" + id + "\";options=\"");
    buf.append("None");
    TriggerEffect[] effects = TriggerEffects.getFilteredEffects(getResponder(), show, this.incEInactive, this.dontListMine, this.listMineOnly, this.listForUser);
    
    Arrays.sort(effects);
    this.ueffects.clear();
    for (TriggerEffect eff : effects)
    {
      this.ueffects.add(eff);
      buf.append(",");
      buf.append(eff.getName());
    }
    buf.append("\";default=\"0\"}");
    return buf.toString();
  }
  
  private String dropdownMissions(String id, int current, boolean reload)
  {
    StringBuilder buf = new StringBuilder();
    int counter = 0;
    int def = 0;
    buf.append("dropdown{id=\"" + id + "\";options=\"");
    buf.append("None");
    Mission[] missions;
    if (reload)
    {
      missions = Missions.getFilteredMissions(getResponder(), this.includeM, this.incMInactive, this.dontListMine, this.listMineOnly, this.listForUser, this.groupName, this.onlyCurrent, this.currentTargetId);
      
      this.missionsAvail.clear();
      for (Mission lMission : missions)
      {
        this.missionsAvail.add(lMission);
        counter++;
        buf.append(",");
        buf.append(lMission.getName());
        if ((current > 0) && (current == lMission.getId())) {
          def = counter;
        }
      }
    }
    else
    {
      for (Mission lMission : this.missionsAvail)
      {
        counter++;
        buf.append(",");
        buf.append(lMission.getName());
        if ((current > 0) && (current == lMission.getId())) {
          def = counter;
        }
      }
    }
    buf.append("\";default=\"" + def + "\"}");
    return buf.toString();
  }
  
  private String dropdownSkillTemplates(String id, int current)
  {
    StringBuilder buf = new StringBuilder();
    SkillTemplate[] stemps = SkillSystem.getAllSkillTemplates();
    
    Arrays.sort(stemps);
    int def = 0;
    int counter = 0;
    this.stemplates.clear();
    
    buf.append("dropdown{id=\"" + id + "\";options=\"");
    buf.append("None");
    for (SkillTemplate lStemp : stemps) {
      if (lStemp.isMission())
      {
        counter++;
        buf.append(",");
        this.stemplates.add(lStemp);
        buf.append(lStemp.getName());
        if ((current > 0) && (current == lStemp.getNumber())) {
          def = counter;
        }
      }
    }
    buf.append("\";default=\"" + def + "\"}");
    return buf.toString();
  }
  
  private String dropdownSpecialEffects(String id, int current)
  {
    StringBuilder buf = new StringBuilder();
    int def = 0;
    int counter = 0;
    buf.append("dropdown{id=\"specialEffect\";options=\"");
    this.effectsAvail.clear();
    SpecialEffects[] effects = SpecialEffects.getEffects();
    for (SpecialEffects lEffect : effects) {
      if (getResponder().getPower() >= lEffect.getPowerRequired())
      {
        this.effectsAvail.add(lEffect);
        if (counter > 0) {
          buf.append(",");
        }
        buf.append(lEffect.getName());
        if ((current > 0) && (current == lEffect.getId())) {
          def = counter;
        } else if ((current == 0) && (lEffect.getId() == 0)) {
          def = counter;
        }
        counter++;
      }
    }
    buf.append("\";default=\"" + def + "\"}");
    return buf.toString();
  }
  
  private String dropdownSkillValues(String id, String current)
  {
    StringBuilder buf = new StringBuilder();
    buf.append("dropdown{id=\"" + id + "\";options=\"");
    buf.append("0,");
    buf.append(0.001F);
    buf.append("%");
    buf.append(", ");
    buf.append(0.002F);
    buf.append("%,");
    buf.append(0.01F);
    buf.append("%,");
    buf.append(0.05F);
    buf.append("%,");
    buf.append(0.1F);
    buf.append("%,");
    buf.append(1.0F);
    buf.append(", ");
    buf.append(10.0F);
    buf.append(", ");
    buf.append(20.0F);
    
    int def = 0;
    try
    {
      float cv = Float.parseFloat(current);
      if (cv == 0.001F) {
        def = 1;
      } else if (cv == 0.002F) {
        def = 2;
      } else if (cv == 0.01F) {
        def = 3;
      } else if (cv == 0.05F) {
        def = 4;
      } else if (cv == 0.1F) {
        def = 5;
      } else if (cv == 1.0F) {
        def = 6;
      } else if (cv == 10.0F) {
        def = 7;
      } else if (cv == 20.0F) {
        def = 8;
      }
    }
    catch (NumberFormatException localNumberFormatException) {}
    buf.append("\";default=\"" + def + "\"}");
    return buf.toString();
  }
  
  private String dropdownItemMaterials(String id, byte current)
  {
    StringBuilder buf = new StringBuilder();
    int def = 0;
    int counter = 0;
    buf.append("dropdown{id=\"" + id + "\";options=\"");
    for (int x = 0; x < 96; x++)
    {
      if (x == 0)
      {
        buf.append("standard");
      }
      else
      {
        buf.append(",");
        buf.append(Item.getMaterialString((byte)x));
      }
      if ((current > 0) && (current == x)) {
        def = counter;
      }
      counter++;
    }
    buf.append("\";default=\"" + def + "\"}");
    return buf.toString();
  }
  
  private String dropdownCreatureTemplates(String id, int current)
  {
    StringBuilder buf = new StringBuilder();
    CreatureTemplate[] crets = CreatureTemplateFactory.getInstance().getTemplates();
    int def = 0;
    int counter = 0;
    buf.append("dropdown{id='creatureSpawn';options=\"");
    for (int x = 0; x < crets.length; x++)
    {
      if (x == 0)
      {
        buf.append("none");
      }
      else if (crets[x].baseCombatRating < 15.0F)
      {
        buf.append(",");
        this.creaturesAvail.add(crets[x]);
        buf.append(crets[x].getName());
        counter++;
      }
      if ((current > 0) && (current == crets[x].getTemplateId())) {
        def = counter;
      }
    }
    buf.append("\";default=\"" + def + "\"}");
    return buf.toString();
  }
  
  private String dropdownBoolean(String id, boolean current)
  {
    StringBuilder buf = new StringBuilder();
    buf.append("dropdown{id=\"" + id + "\";options=\"");
    buf.append("True,");
    buf.append("False");
    buf.append("\";default=\"" + (current ? 0 : 1) + "\"}");
    return buf.toString();
  }
  
  private String dropdownCreatureTypes(String id, byte current)
  {
    StringBuilder buf = new StringBuilder();
    int def = 0;
    this.creaturesTypes.clear();
    buf.append("dropdown{id=\"" + id + "\";options=\"");
    buf.append("None,");
    this.creaturesTypes.add(Byte.valueOf((byte)0));
    buf.append("fierce ");
    this.creaturesTypes.add(Byte.valueOf((byte)1));
    buf.append(", ");
    buf.append("angry ");
    this.creaturesTypes.add(Byte.valueOf((byte)2));
    buf.append(", ");
    buf.append("raging ");
    this.creaturesTypes.add(Byte.valueOf((byte)3));
    buf.append(", ");
    buf.append("slow ");
    this.creaturesTypes.add(Byte.valueOf((byte)4));
    buf.append(", ");
    buf.append("alert ");
    this.creaturesTypes.add(Byte.valueOf((byte)5));
    buf.append(", ");
    buf.append("greenish ");
    this.creaturesTypes.add(Byte.valueOf((byte)6));
    buf.append(", ");
    buf.append("lurking ");
    this.creaturesTypes.add(Byte.valueOf((byte)7));
    buf.append(", ");
    buf.append("sly ");
    this.creaturesTypes.add(Byte.valueOf((byte)8));
    buf.append(", ");
    buf.append("hardened ");
    this.creaturesTypes.add(Byte.valueOf((byte)9));
    buf.append(", ");
    buf.append("scared ");
    this.creaturesTypes.add(Byte.valueOf((byte)10));
    buf.append(", ");
    buf.append("diseased ");
    this.creaturesTypes.add(Byte.valueOf((byte)11));
    buf.append(", ");
    buf.append("champion ");
    this.creaturesTypes.add(Byte.valueOf((byte)99));
    
    def = current & 0xFF;
    if (current == 99) {
      def = 12;
    }
    buf.append("\";default=\"" + def + "\"}");
    return buf.toString();
  }
  
  private String dropdownItemTemplates(String id, int current, boolean incSpecial)
  {
    StringBuilder buf = new StringBuilder();
    ItemTemplate[] templates = ItemTemplateFactory.getInstance().getTemplates();
    
    Arrays.sort(templates);
    int def = 0;
    int counter = 0;
    this.itemplates.clear();
    buf.append("dropdown{id=\"" + id + "\";options=\"");
    buf.append("None");
    for (ItemTemplate lTemplate : templates) {
      if ((lTemplate.isMissionItem()) || ((incSpecial) && (lTemplate.getTemplateId() == 791)))
      {
        buf.append(",");
        this.itemplates.add(lTemplate);
        if (lTemplate.isMetal()) {
          buf.append(lTemplate.sizeString + Item.getMaterialString(lTemplate.getMaterial()) + " " + lTemplate
            .getName());
        } else if (lTemplate.bowUnstringed) {
          buf.append(lTemplate.sizeString + lTemplate.getName() + " [unstringed]");
        } else {
          buf.append(lTemplate.sizeString + lTemplate.getName());
        }
        counter++;
        if ((current > 0) && (current == lTemplate.getTemplateId())) {
          def = counter;
        }
      }
    }
    buf.append("\";default=\"" + def + "\"}");
    return buf.toString();
  }
  
  private String dropdownInventory(String id, long itemReward)
  {
    StringBuilder buf = new StringBuilder();
    int def = 0;
    int counter = 0;
    Item[] itemarr = getResponder().getInventory().getAllItems(false);
    buf.append("dropdown{id=\"" + id + "\";options=\"");
    buf.append("None");
    for (Item lElement : itemarr) {
      if ((!lElement.isNoDrop()) && (!lElement.isArtifact()))
      {
        buf.append(",");
        this.ritems.add(lElement);
        buf.append(lElement.getName() + " - " + (int)lElement.getQualityLevel());
        
        counter++;
        if ((itemReward > 0L) && (itemReward == lElement.getWurmId())) {
          def = counter;
        }
      }
    }
    buf.append("\";default=\"" + def + "\"}");
    return buf.toString();
  }
  
  private String dropdownAchievements(String id, int current)
  {
    StringBuilder buf = new StringBuilder();
    int def = 0;
    int counter = 0;
    this.myAchievements = Achievement.getSteelAchievements(getResponder());
    if (getResponder().getPower() > 0) {
      this.myAchievements.add(Achievement.getTemplate(141));
    }
    buf.append("dropdown{id='achievement';options=\"");
    buf.append("None");
    for (AchievementTemplate template : this.myAchievements)
    {
      counter++;
      buf.append(",");
      buf.append(template.getName() + " (" + template.getCreator() + ")");
      if ((current > 0) && (template.getNumber() == current)) {
        def = counter;
      }
    }
    buf.append("\";default=\"" + def + "\"}");
    return buf.toString();
  }
  
  private String dropdownActions(String id, short current)
  {
    StringBuilder buf = new StringBuilder();
    
    ActionEntry[] acts = (ActionEntry[])Actions.actionEntrys.clone();
    
    Arrays.sort(acts);
    int def = 0;
    int counter = 0;
    this.actionEntries.clear();
    buf.append("dropdown{id=\"" + id + "\";options=\"");
    buf.append("None");
    for (ActionEntry lAct : acts) {
      if (lAct.isMission())
      {
        counter++;
        buf.append(",");
        buf.append(lAct.getActionString());
        this.actionEntries.add(lAct);
        if ((current > 0) && (current == lAct.getNumber())) {
          def = counter;
        }
      }
    }
    buf.append("\";default=\"" + def + "\"}");
    return buf.toString();
  }
  
  private String dropdownTileTypes(String id, int current)
  {
    StringBuilder buf = new StringBuilder();
    int def = 0;
    buf.append("dropdown{id=\"newTileType\";options=\"");
    buf.append("None,");
    buf.append("Tree,");
    buf.append("Dirt,");
    buf.append("Grass,");
    buf.append("Field,");
    buf.append("Sand,");
    buf.append("Mycelium,");
    buf.append("Cave wall,");
    buf.append("Iron ore");
    if (current == Tiles.Tile.TILE_TREE.id) {
      def = 1;
    } else if (current == Tiles.Tile.TILE_DIRT.id) {
      def = 2;
    } else if (current == Tiles.Tile.TILE_GRASS.id) {
      def = 3;
    } else if (current == Tiles.Tile.TILE_FIELD.id) {
      def = 4;
    } else if (current == Tiles.Tile.TILE_SAND.id) {
      def = 5;
    } else if (current == Tiles.Tile.TILE_MYCELIUM.id) {
      def = 6;
    } else if (current == Tiles.Tile.TILE_CAVE_WALL.id) {
      def = 7;
    } else if (current == Tiles.Tile.TILE_CAVE_WALL_ORE_IRON.id) {
      def = 8;
    }
    buf.append("\";default=\"" + def + "\"}");
    return buf.toString();
  }
  
  private void sendManageMission()
  {
    String name = "";
    String group = this.groupName;
    boolean inactive = false;
    boolean hidden = false;
    String intro = "";
    boolean faildeath = false;
    boolean secondChance = false;
    boolean mayBeRestarted = false;
    String sdays = "0";
    String shours = "0";
    String sminutes = "0";
    String sseconds = "0";
    int height = 400;
    int days;
    int hours;
    int minutes;
    if (getAnswer() != null)
    {
      name = getStringProp("name");
      group = getStringProp("groupName");
      intro = getStringProp("intro");
      inactive = getBooleanProp("inactive");
      hidden = getBooleanProp("hidden");
      faildeath = getBooleanProp("faildeath");
      mayBeRestarted = getBooleanProp("mayBeRestarted");
      secondChance = getBooleanProp("secondChance");
      
      sdays = getStringProp("days");
      shours = getStringProp("hours");
      sminutes = getStringProp("minutes");
      sseconds = getStringProp("seconds");
    }
    else if (this.missionId > 0)
    {
      Mission m = Missions.getMissionWithId(this.missionId);
      if (m != null)
      {
        if ((getResponder().getLogger() != null) && (getResponder().getPower() > 0) && 
          (m.getName() != null)) {
          getResponder().getLogger().info(getResponder() + ": Viewing mission settings for mission with name: " + m.getName());
        }
        name = m.getName();
        group = m.getGroupName();
        inactive = m.isInactive();
        hidden = m.isHidden();
        intro = m.getInstruction();
        faildeath = m.isFailOnDeath();
        secondChance = m.hasSecondChance();
        mayBeRestarted = m.mayBeRestarted();
        if (m.getMaxTimeSeconds() > 0)
        {
          int left = m.getMaxTimeSeconds();
          days = (int)(left / 86400L);
          left = (int)(left - 86400L * days);
          hours = (int)(left / 3600L);
          left = (int)(left - 3600L * hours);
          minutes = (int)(left / 60L);
          left = (int)(left - 60L * minutes);
          int seconds = left;
          
          sdays = "" + days;
          shours = "" + hours;
          sminutes = "" + minutes;
          sseconds = "" + seconds;
        }
      }
    }
    StringBuilder buf = new StringBuilder();
    buf.append("border{border{size=\"20,20\";null;null;varray{rescale=\"true\";harray{label{type='bold';text=\"" + this.question + "    \"};label{type=\"bolditalic\";" + "color=\"255,127,127\"" + ";text=\"" + this.errorText + "\"}}}harray{button{id=\"back\";text=\"Back\"};label{text=\"  \"}}null;}null;scroll{vertical=\"true\";horizontal=\"false\";varray{rescale=\"true\";passthrough{id=\"id\";text=\"" + 
    
      getId() + "\"}");
    
    buf.append("harray{label{text=\"Name (max 100 chars)\"};input{id=\"name\";maxchars=\"100\";text=\"" + name + "\"};label{text=\"  Group (20 chars)\"};input{id=\"groupName\";maxchars=\"20\";text=\"" + group + "\"};}");
    
    buf.append("harray{checkbox{id=\"inactive\";selected=\"" + inactive + "\"};label{text=\"Inactive \"};checkbox{id=\"hidden\";selected=\"" + hidden + "\"};label{text=\"Hidden from players \"};}");
    
    buf.append("label{text=\"Introduction (max 400 chars) - Note: This text will appear as a popup when mission starts.\"};");
    buf.append("input{id=\"intro\";maxchars=\"400\"text=\"" + intro + "\";maxlines=\"4\"};");
    buf.append("text{text=\"\"}");
    buf.append("header{text=\"Fail\"};");
    buf.append("harray{checkbox{id=\"faildeath\";selected=\"" + faildeath + "\"};label{text=\"Fail on death \"};}");
    
    buf.append("text{text=\"A mission may fail after a certain time period. Set this limit here.\"}");
    buf.append("harray{label{text=\"Max time: days\"};input{id=\"days\";text=\"" + sdays + "\";maxchars=\"3\"};label{text=\" hours\"};input{id=\"hours\";text=\"" + shours + "\";maxchars=\"2\"};label{text=\" minutes\"};input{id=\"minutes\";text=\"" + sminutes + "\";maxchars=\"2\"};label{text=\" seconds\"};input{id=\"seconds\";text=\"" + sseconds + "\";maxchars=\"2\"};}");
    
    buf.append("text{text=\"\"}");
    buf.append("header{text=\"Restart\"};");
    buf.append("harray{checkbox{id=\"secondChance\";selected=\"" + secondChance + "\";text=\"New chance if fail \"};label{text=\"  \"};checkbox{id=\"mayBeRestarted\";selected=\"" + mayBeRestarted + "\";text=\"May restart when finished \"};}");
    
    buf.append("text{text=\"\"}");
    if (this.missionId == 0)
    {
      buf.append(appendChargeInfo());
      buf.append("harray{button{id=\"createMission\";text=\"Create New Mission\"};}");
    }
    else
    {
      height = 500;
      buf.append("harray{button{id=\"updateMission\";text=\"Update Mission\"};label{text=\"  \"};button{id=\"deleteMission\";text=\"Delete Mission \"hover=\"This will delete " + name + "\";confirm=\"You are about to delete " + name + ".\";question=\"Do you really want to do that?\"};label{text=\"  \"};button{id=\"cloneMission\";text=\"Clone Mission\"hover=\"This will show a copy of this mission for creation\";};}");
      
      buf.append("label{type=\"bold\";text=\"- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -\"}");
      buf.append("harray{label{type=\"bold\";text=\"Triggers:     \"}button{id=\"createTrigger\";text=\"Create New Trigger\"};label{text=\"  Unlinked Triggers\"}" + 
      
        dropdownUnlinkedTriggers("unTrigs", 2) + "label{text=\" \"}button{id=\"linkTrigger\";text=\"Link Trigger\"};}");
      
      MissionTrigger[] trigs = MissionTriggers.getFilteredTriggers(getResponder(), 0, this.incTInactive, this.missionId, 0);
      MissionTrigger trigger;
      if (trigs.length > 0)
      {
        Arrays.sort(trigs);
        buf.append("table{rows=\"1\";cols=\"7\";");
        buf.append("label{text=\"\"};label{text=\"Id\"};label{text=\"Name\"};label{text=\"State\"};label{text=\"Action\"};label{text=\"Target\"};label{text=\"\"};");
        for (trigger : trigs)
        {
          String colour = "color=\"127,255,127\"";
          String hover = ";hover=\"Active\"";
          if (trigger.isInactive())
          {
            colour = "color=\"255,127,127\"";
            hover = ";hover=\"Inactive\"";
          }
          else if (trigger.hasTargetOf(this.currentTargetId, getResponder()))
          {
            colour = "color=\"140,140,255\";";
            hover = ";hover=\"Current Target\"";
          }
          buf.append("button{id=\"edtT" + trigger.getId() + "\";text=\"Edit\"};label{" + colour + "text=\"" + trigger
            .getId() + " \"" + hover + "};label{" + colour + "text=\"" + trigger
            .getName() + " \"" + hover + "};label{" + colour + "text=\"" + trigger
            .getStateRange() + " \"" + hover + "};label{" + colour + "text=\"" + trigger
            .getActionString() + " \"" + hover + "};label{" + colour + "text=\"" + trigger
            .getTargetAsString(getResponder()) + "\"" + hover + "};harray{label{text=\"  \"}button{id=\"unlT" + trigger
            
            .getId() + "\";text=\"Unlink\"hover=\"This will unlink the trigger " + trigger
            .getName() + " from this mission\";};};");
        }
        buf.append("}");
      }
      else
      {
        buf.append("label{text=\"No triggers found\"}");
      }
      buf.append("label{type=\"bold\";text=\"- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -\"}");
      
      buf.append("harray{label{type=\"bold\";text=\"Trigger effects:     \"}button{id=\"createEffect\";text=\"Create New Effect\"};}");
      
      TriggerEffect[] teffs = TriggerEffects.getFilteredEffects(trigs, getResponder(), this.showE, this.incEInactive, this.dontListMine, this.listMineOnly, this.listForUser, false);
      TriggerEffect lEff;
      if (teffs.length > 0)
      {
        Arrays.sort(teffs);
        buf.append("table{rows=\"1\";cols=\"7\";");
        buf.append("label{text=\"\"};label{text=\"Id\"};label{text=\"Name\"};label{text=\"+State\"};label{text=\"Type\"};label{text=\"\"};label{text=\"\"};");
        
        hours = teffs;minutes = hours.length;
        for (trigger = 0; trigger < minutes; trigger++)
        {
          lEff = hours[trigger];
          
          String colour = "color=\"127,255,127\"";
          String hover = ";hover=\"Active\"";
          if (lEff.isInactive())
          {
            colour = "color=\"255,127,127\"";
            hover = ";hover=\"Inactive\"";
          }
          else if (lEff.hasTargetOf(this.currentTargetId, getResponder()))
          {
            colour = "color=\"140,140,255\";";
            hover = ";hover=\"Current Target\"";
          }
          String clrtxt = "label{type=\"italic\";text=\"   no text\";hover=\"no text to clear\"}";
          if (!lEff.getTextDisplayed().isEmpty()) {
            clrtxt = "harray{label{text=\" \"};button{id=\"clrE" + lEff.getId() + "\";text=\"Clear Text\"};}";
          }
          buf.append("button{id=\"edtE" + lEff.getId() + "\";text=\"Edit\"};label{" + colour + "text=\"" + lEff
            .getId() + " \"" + hover + "};label{" + colour + "text=\"" + lEff
            .getName() + " \"" + hover + "};label{" + colour + "text=\"" + lEff
            .getMissionStateChange() + " \"" + hover + "};label{" + colour + "text=\"" + lEff
            .getType() + " \"" + hover + "};" + clrtxt + "harray{label{text=\" \"}};");
        }
        buf.append("}");
      }
      else
      {
        buf.append("label{text=\"No trigger effects found\"}");
      }
      buf.append("label{type=\"bold\";text=\"- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -\"}");
      
      buf.append("label{type=\"bold\";text=\"Other Mission effects that change this missions state:\"}");
      buf.append("harray{label{text=\"  Unlinked Effects\"}" + 
      
        dropdownUnlinkedEffects("unEffs", 2) + "label{text=\" \"}button{id=\"linkEffect\";text=\"Link Effect\"};}");
      
      TriggerEffect[] meffs = TriggerEffects.getFilteredEffects(trigs, getResponder(), this.showE, this.incEInactive, this.dontListMine, this.listMineOnly, this.listForUser, this.missionId);
      if (meffs.length > 0)
      {
        Arrays.sort(meffs);
        buf.append("table{rows=\"1\";cols=\"7\";");
        buf.append("label{text=\"\"};label{text=\"Id\"};label{text=\"Name\"};label{text=\"+State\"};label{text=\"Type\"};label{text=\"\"};label{text=\"\"};");
        
        minutes = meffs;trigger = minutes.length;
        for (lEff = 0; lEff < trigger; lEff++)
        {
          TriggerEffect lEff = minutes[lEff];
          
          String colour = "color=\"127,255,127\"";
          String hover = ";hover=\"Active\"";
          if (lEff.isInactive())
          {
            colour = "color=\"255,127,127\"";
            hover = ";hover=\"Inactive\"";
          }
          else if (lEff.hasTargetOf(this.currentTargetId, getResponder()))
          {
            colour = "color=\"140,140,255\";";
            hover = ";hover=\"Current Target\"";
          }
          String clrtxt = "label{type=\"italic\";text=\"   no text\";hover=\"no text to clear\"}";
          if (!lEff.getTextDisplayed().isEmpty()) {
            clrtxt = "harray{label{text=\" \"};button{id=\"clrE" + lEff.getId() + "\";text=\"Clear Text\"};}";
          }
          buf.append("button{id=\"edtE" + lEff.getId() + "\";text=\"Edit\"};label{" + colour + "text=\"" + lEff
            .getId() + " \"" + hover + "};label{" + colour + "text=\"" + lEff
            .getName() + " \"" + hover + "};label{" + colour + "text=\"" + lEff
            .getMissionStateChange() + " \"" + hover + "};label{" + colour + "text=\"" + lEff
            .getType() + " \"" + hover + "};" + clrtxt + "harray{label{text=\" \"}button{id=\"unlE" + lEff
            
            .getId() + "\";text=\"Unlink\"hover=\"This will unlink effect " + lEff
            .getName() + " from this mission\";};};");
        }
        buf.append("}");
      }
      else
      {
        buf.append("label{text=\"No mission effects found\"}");
      }
    }
    buf.append("}};null;null;}");
    
    getResponder().getCommunicator().sendBml(500, height, true, true, buf.toString(), 200, 200, 200, this.title);
  }
  
  private String appendChargeInfo()
  {
    StringBuilder buf = new StringBuilder();
    if (getResponder().getPower() == 0)
    {
      Item ruler = null;
      try
      {
        ruler = Items.getItem(this.missionRulerId);
        if (ruler.getOwnerId() != getResponder().getWurmId()) {
          buf.append("text{text=\"You are not the ruler of this mission ruler.\"}");
        } else if (ruler.getAuxData() <= 0) {
          buf.append("text{text=\"Your " + ruler.getName() + " contains no charges. You can not create new mission functionality.\"}");
        } else {
          buf.append("text{text=\"Your " + ruler.getName() + " contains " + ruler.getAuxData() + " charges. Creating a new mission functionality uses up 1 charge.\"}");
        }
      }
      catch (NoSuchItemException nsi)
      {
        buf.append("text{text=\"The mission ruler is gone!\"}");
      }
      buf.append("text{text=\"\"}");
    }
    return buf.toString();
  }
  
  public void sendQuestion()
  {
    StringBuilder buf = new StringBuilder(getBmlHeaderNoQuestion());
    buf.append("header{text=\"Missions\"}");
    if (getResponder().getPower() > 0)
    {
      buf.append("harray{label{text=\"Filter:\"};checkbox{id=\"listmine\";text=\"Only my missions  \"" + 
      
        addSel(this.listMineOnly) + "};checkbox{id=\"nolistmine\";text=\"Not my missions  \"" + 
        addSel(this.dontListMine) + "};checkbox{id=\"onlyCurrent\";text=\"Only current target missions  \"" + 
        addSel(this.onlyCurrent) + "}}");
      
      buf.append("harray{label{text=\"Show:\"};radio{group=\"includeM\";id=\"0\";text=\"All  \"" + 
      
        addSel(this.includeM == 0) + "};radio{group=\"includeM\";id=\"" + 1 + "\";text=\"With triggers  \"" + 
        addSel(this.includeM == 1) + "};radio{group=\"includeM\";id=\"" + 2 + "\";text=\"Without triggers  \"" + 
        addSel(this.includeM == 2) + "};label{text=\"     \"};checkbox{id=\"incMInactive\";text=\"Include inactive\"" + 
        
        addSel(this.incMInactive) + "};}");
      
      buf.append("harray{label{text=\"Type: \"};checkbox{id=\"typeSystem\";text=\"System  \"" + 
      
        addSel(this.typeSystem) + "};checkbox{id=\"typeGM\";text=\"GM  \"" + 
        addSel(this.typeGM) + "};checkbox{id=\"typePlayer\";text=\"Player \"" + 
        addSel(this.typePlayer) + "};};");
      
      buf.append("harray{label{text=\"Group: \"};input{id=\"groupName\";text=\"" + this.groupName + "\";maxchars=\"20\"}label{text=\" Player: \"};input{id=\"specialName\";text=\"" + this.userName + "\";maxchars=\"30\"}};");
      
      buf.append("text{text=\"\"}");
    }
    else
    {
      buf.append("harray{label{text=\"Group: \"};input{id=\"groupName\";text=\"" + this.groupName + "\";maxchars=\"20\"}};");
      
      buf.append(appendChargeInfo());
    }
    buf.append("harray{button{id=\"listMissions\";text=\"List Missions\"}label{text=\"  \"};button{id=\"createMission\";text=\"Create New Mission \"};}");
    
    buf.append("label{type=\"bold\";text=\"- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -\"}");
    buf.append("header{text=\"Triggers\"}");
    buf.append("harray{label{text=\"Show:\"};radio{group=\"showT\";id=\"0\";text=\"All  \"" + 
    
      addSel(this.showT == 0) + "};radio{group=\"showT\";id=\"" + 1 + "\";text=\"Linked to missions\"" + 
      addSel(this.showT == 1) + "}radio{group=\"showT\";id=\"" + 2 + "\";text=\"Unlinked\"" + 
      addSel(this.showT == 2) + "}label{text=\"     \"};checkbox{id=\"incTInactive\";text=\"Include inactive  \"" + 
      
      addSel(this.incTInactive) + "};}");
    
    buf.append("harray{button{id=\"listTriggers\";text=\"List Triggers \"};label{text=\"  \"};button{id=\"createTrigger\";text=\"Create New Trigger  \"};}");
    
    buf.append("label{type=\"bold\";text=\"- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -\"}");
    buf.append("header{text=\"Effects\"}");
    buf.append("harray{label{text=\"Show:\"};radio{group=\"showE\";id=\"0\";text=\"All  \"" + 
    
      addSel(this.showE == 0) + "};radio{group=\"showE\";id=\"" + 1 + "\";text=\"Linked to trigger\"" + 
      addSel(this.showE == 1) + "}radio{group=\"showE\";id=\"" + 2 + "\";text=\"Unlinked\"" + 
      addSel(this.showE == 2) + "}label{text=\"     \"};checkbox{id=\"incEInactive\";text=\"Include inactive  \"" + 
      
      addSel(this.incEInactive) + "};}");
    
    buf.append("harray{button{id=\"listEffects\";text=\"List Effects\"}label{text=\"  \"};button{id=\"createEffect\";text=\"Create New Effect \"}}");
    
    buf.append("label{type=\"bold\";text=\"- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -\"}");
    buf.append("header{text=\"Current Target\"}");
    String tgt = MissionTriggers.getTargetAsString(getResponder(), this.currentTargetId);
    buf.append("harray{label{color=\"140,140,255\";text=\"" + tgt + "\"}" + (
    
      getResponder().getPower() > 0 ? "label{color=\"140,140,255\";text=\"(Id:" + this.currentTargetId + ")\"}" : "") + "}");
    
    buf.append("}};null;null;}");
    getResponder().getCommunicator().sendBml(450, 450, true, true, buf.toString(), 200, 200, 200, this.title);
  }
  
  public static final boolean maySetToOpen(Creature resp, long targetId)
  {
    if (resp.getPower() == 0)
    {
      if (WurmId.getType(targetId) == 5)
      {
        Wall w = Wall.getWall(targetId);
        if (w != null) {
          try
          {
            Structure s = Structures.getStructure(w.getStructureId());
            for (Item key : resp.getKeys()) {
              if (key.getWurmId() == s.getWritId()) {
                return true;
              }
            }
          }
          catch (NoSuchStructureException localNoSuchStructureException) {}
        }
        return false;
      }
      if (WurmId.getType(targetId) == 7)
      {
        Fence f = Fence.getFence(targetId);
        if (f.isDoor())
        {
          FenceGate fg = FenceGate.getFenceGate(targetId);
          if (fg == null) {
            return false;
          }
          if (fg.canBeOpenedBy(resp, false)) {
            return true;
          }
        }
      }
      return false;
    }
    return true;
  }
  
  private boolean parseEffect(Item ruler)
  {
    boolean delete = getBooleanProp("deleteEffect");
    TriggerEffect eff = TriggerEffects.getTriggerEffect(this.effectId);
    if (eff == null)
    {
      if (delete)
      {
        getResponder().getCommunicator().sendNormalServerMessage("You tried to delete a non-existing trigger effect.");
        return reshow();
      }
      eff = new TriggerEffect();
      eff.setCreatorName(getResponder().getName());
      eff.setLastModifierName(getResponder().getName());
    }
    else if (delete)
    {
      eff.destroy();
      getResponder().getCommunicator().sendNormalServerMessage("You delete the trigger effect.");
      return parseBack();
    }
    this.errorText = "";
    String name = eff.getName();
    String description = eff.getDescription();
    
    boolean inActive = eff.isInactive();
    
    boolean startSkill = eff.isStartSkillgain();
    boolean stopSkill = eff.isStopSkillgain();
    boolean destroysInventory = eff.destroysInventory();
    
    int itemReward = eff.getRewardItem();
    byte itemMaterial = eff.getItemMaterial();
    boolean newbie = eff.isNewbieItem();
    int ql = eff.getRewardQl();
    int numbers = eff.getRewardNumbers();
    byte bytevalue = eff.getRewardByteValue();
    long existingItem = eff.getExistingItemReward();
    long rewardTargetContainerId = eff.getRewardTargetContainerId();
    boolean destroysTarget = eff.destroysTarget();
    
    int rewardSkillNum = eff.getRewardSkillNum();
    float rewardSkillVal = eff.getRewardSkillModifier();
    
    int modifyTileX = eff.getModifyTileX();
    int modifyTileY = eff.getModifyTileY();
    int newTileType = eff.getNewTileType();
    byte newTileData = eff.getNewTileData();
    
    int spawnTileX = eff.getSpawnTileX();
    int spawnTileY = eff.getSpawnTileY();
    int creatureSpawn = eff.getCreatureSpawn();
    int creatureAge = eff.getCreatureAge();
    String creatureName = eff.getCreatureName();
    byte creatureType = eff.getCreatorType();
    
    int teleportX = eff.getTeleportX();
    int teleportY = eff.getTeleportY();
    int teleportLayer = eff.getTeleportLayer();
    
    int specialEffect = eff.getSpecialEffectId();
    
    int achievementId = eff.getAchievementId();
    
    int missionAffectedId = eff.getMissionId();
    
    float missionStateChange = eff.getMissionStateChange();
    
    int missionActivated = eff.getMissionToActivate();
    int missionDeActivated = eff.getMissionToDeActivate();
    
    int triggerActivated = eff.getTriggerToActivate();
    int triggerDeActivated = eff.getTriggerToDeActivate();
    
    int effectActivated = eff.getEffectToActivate();
    int effectDeActivated = eff.getEffectToDeActivate();
    
    String sound = eff.getSoundName();
    
    int setWindowSizeX = eff.getWindowSizeX();
    int setWindowSizeY = eff.getWindowSizeY();
    String textdisplayed = eff.getTextDisplayed();
    String top = eff.getTopText();
    
    boolean cloneEffect = getBooleanProp("cloneEffect");
    if (cloneEffect)
    {
      this.sbacks = (this.sbacks + "|" + 7 + "," + this.missionId + "," + this.triggerId + "," + this.effectId);
      return createNewEffect(getAnswer());
    }
    sound = getStringProp("sound");
    boolean playSound = getBooleanProp("playSound");
    if (playSound)
    {
      if (!sound.isEmpty()) {
        SoundPlayer.playSound(sound, getResponder(), 1.5F);
      } else {
        getResponder().getCommunicator().sendNormalServerMessage("No sound mapped", (byte)1);
      }
      return reshow();
    }
    boolean listSounds = getBooleanProp("listSounds");
    if (listSounds)
    {
      SoundList sl = new SoundList(getResponder(), "SoundList", "Select the Sound to use");
      sl.setSelected(sound);
      sl.setRoot(this);
      sl.sendQuestion();
      return false;
    }
    setWindowSizeX = getIntProp("winsizeX");
    setWindowSizeY = getIntProp("winsizeY");
    top = getStringProp("toptext");
    textdisplayed = getStringProp("textdisplayed");
    boolean testText = getBooleanProp("testText");
    if (testText)
    {
      if ((!top.isEmpty()) && (!textdisplayed.isEmpty()))
      {
        MissionPopup pop = new MissionPopup(getResponder(), "Mission progress", "");
        if ((setWindowSizeX > 0) && (setWindowSizeY > 0))
        {
          pop.windowSizeX = setWindowSizeX;
          pop.windowSizeY = setWindowSizeY;
        }
        pop.setToSend(textdisplayed);
        pop.setTop(top);
        pop.setRoot(this);
        pop.sendQuestion();
        return false;
      }
      return reshow();
    }
    boolean updateEffect = getBooleanProp("updateEffect");
    boolean createEffect = getBooleanProp("createEffect");
    if ((updateEffect) || (createEffect))
    {
      name = getStringProp("name", 3);
      description = getStringProp("desc");
      inActive = getBooleanProp("inactive");
      if (getResponder().getPower() > 0)
      {
        startSkill = getBooleanProp("startSkill");
        stopSkill = getBooleanProp("stopSkill");
        destroysInventory = getBooleanProp("destroysInventory");
        
        itemReward = indexItemTemplate("itemReward", "Item Reward");
        itemMaterial = getByteProp("itemMaterial");
        newbie = getBooleanProp("newbieItem");
        ql = getIntProp("ql", 0, 100, true);
        numbers = getIntProp("numbers");
        bytevalue = getByteProp("bytevalue");
        rewardTargetContainerId = readContainerId();
        destroysTarget = getBooleanProp("destroysTarget");
        
        rewardSkillNum = indexSkillNum("rewardSkillNum", "Reward Skill Number");
        rewardSkillVal = indexSkillVal("rewardSkillVal", "Reward Skill Value");
        
        modifyTileX = getIntProp("modifyTileX");
        modifyTileY = getIntProp("modifyTileY");
        newTileData = getByteProp("newTileData");
        newTileType = indexTileType("newTileType");
        
        spawnTileX = getIntProp("spawnTileX");
        spawnTileY = getIntProp("spawnTileY");
        creatureSpawn = indexCreatureTemplate("creatureSpawn", "Creature Spawn");
        creatureAge = getIntProp("creatureAge");
        creatureName = getStringProp("creatureName");
        creatureType = indexCreatureType("creatureType", "Creature Type");
        
        teleportX = getIntProp("teleportTileX");
        teleportY = getIntProp("teleportTileY");
        teleportLayer = indexLayer("teleportLayer", "Teleport Layer");
      }
      specialEffect = indexSpecialEffect("specialEffect", "Special Effect", true);
      
      achievementId = indexAchievementId("achievement", "achievement");
      
      missionAffectedId = indexMission("missionId", "mission Id");
      missionStateChange = indexStateChange("missionStateChange", specialEffect);
      
      missionActivated = indexMission("missionToActivate", "mission To Activate");
      missionDeActivated = indexMission("missionToDeactivate", "mission To Deactivate");
      
      triggerActivated = indexTrigger("triggerToActivate", "trigger To Activate");
      triggerDeActivated = indexTrigger("triggerToDeactivate", "trigger To Deactivate");
      
      effectActivated = indexTrigger("effectToActivate", "effect To Activate");
      effectDeActivated = indexTrigger("effectToDeactivate", "effect To Deactivate");
      if ((getResponder().getPower() > 0) || (this.level == 8)) {
        existingItem = indexExistingItem("existingItem", existingItem);
      }
      if (this.errorText.length() > 0) {
        return editEffect(this.effectId, getAnswer());
      }
      boolean changed = false;
      if (!name.equals(eff.getName()))
      {
        eff.setName(name);
        changed = true;
      }
      if (!description.equals(eff.getDescription()))
      {
        eff.setDescription(description);
        changed = true;
      }
      if (inActive != eff.isInactive())
      {
        eff.setInactive(inActive);
        changed = true;
      }
      if (itemReward != eff.getRewardItem())
      {
        eff.setRewardItem(itemReward);
        changed = true;
      }
      if (modifyTileX != eff.getModifyTileX())
      {
        eff.setModifyTileX(modifyTileX);
        changed = true;
      }
      if (modifyTileY != eff.getModifyTileY())
      {
        eff.setModifyTileY(modifyTileY);
        changed = true;
      }
      if (newTileType != eff.getNewTileType())
      {
        eff.setNewTileType(newTileType);
        changed = true;
      }
      if (newTileData != eff.getNewTileData())
      {
        eff.setNewTileData(newTileData);
        changed = true;
      }
      if (spawnTileX != eff.getSpawnTileX())
      {
        eff.setSpawnTileX(spawnTileX);
        changed = true;
      }
      if (spawnTileY != eff.getSpawnTileY())
      {
        eff.setSpawnTileY(spawnTileY);
        changed = true;
      }
      if (creatureSpawn != eff.getCreatureSpawn())
      {
        eff.setCreatureSpawn(creatureSpawn);
        changed = true;
      }
      if (creatureAge != eff.getCreatureAge())
      {
        eff.setCreatureAge(creatureAge);
        changed = true;
      }
      if (!creatureName.equals(eff.getCreatureName()))
      {
        eff.setCreatureName(creatureName);
        changed = true;
      }
      if (creatureType != eff.getCreatureType())
      {
        eff.setCreatureType(creatureType);
        changed = true;
      }
      if (teleportX != eff.getTeleportX())
      {
        eff.setTeleportX(teleportX);
        changed = true;
      }
      if (teleportY != eff.getTeleportY())
      {
        eff.setTeleportY(teleportY);
        changed = true;
      }
      if (teleportLayer != eff.getTeleportLayer())
      {
        eff.setTeleportLayer(teleportLayer);
        changed = true;
      }
      if (missionActivated != eff.getMissionToActivate())
      {
        eff.setMissionToActivate(missionActivated);
        changed = true;
      }
      if (missionDeActivated != eff.getMissionToDeActivate())
      {
        eff.setMissionToDeActivate(missionDeActivated);
        changed = true;
      }
      if (triggerActivated != eff.getTriggerToActivate())
      {
        eff.setTriggerToActivate(triggerActivated);
        changed = true;
      }
      if (triggerDeActivated != eff.getTriggerToDeActivate())
      {
        eff.setTriggerToDeActivate(triggerDeActivated);
        changed = true;
      }
      if (effectActivated != eff.getEffectToActivate())
      {
        eff.setEffectToActivate(effectActivated);
        changed = true;
      }
      if (effectDeActivated != eff.getEffectToDeActivate())
      {
        eff.setEffectToDeActivate(effectDeActivated);
        changed = true;
      }
      if (itemMaterial != eff.getItemMaterial())
      {
        eff.setItemMaterial(itemMaterial);
        changed = true;
      }
      if (newbie != eff.isNewbieItem())
      {
        eff.setNewbieItem(newbie);
        changed = true;
      }
      if (startSkill != eff.isStartSkillgain())
      {
        eff.setStartSkillgain(startSkill);
        changed = true;
      }
      if (stopSkill != eff.isStopSkillgain())
      {
        eff.setStopSkillgain(stopSkill);
        changed = true;
      }
      if (destroysInventory != eff.destroysInventory())
      {
        eff.setDestroyInventory(destroysInventory);
        changed = true;
      }
      if (ql != eff.getRewardQl())
      {
        eff.setRewardQl(ql);
        changed = true;
      }
      if (numbers != eff.getRewardNumbers())
      {
        eff.setRewardNumbers(numbers);
        changed = true;
      }
      if (bytevalue != eff.getRewardByteValue())
      {
        eff.setRewardByteValue(bytevalue);
        changed = true;
      }
      if (existingItem != eff.getExistingItemReward())
      {
        if (existingItem > 0L) {
          try
          {
            Item i = Items.getItem(existingItem);
            if ((i.getOwnerId() != getResponder().getWurmId()) || (i.isTraded()) || (i.isTransferred()) || (i.mailed))
            {
              getResponder().getCommunicator().sendAlertServerMessage("The " + i
                .getName() + " may not be selected as reward right now.");
              existingItem = 0L;
            }
            if (existingItem > 0L) {
              i.putInVoid();
            }
          }
          catch (NoSuchItemException nsi)
          {
            existingItem = 0L;
          }
        }
        eff.setExistingItemReward(existingItem);
        changed = true;
      }
      if (rewardTargetContainerId != eff.getRewardTargetContainerId())
      {
        eff.setRewardTargetContainerId(rewardTargetContainerId);
        changed = true;
      }
      if (specialEffect != eff.getSpecialEffectId())
      {
        eff.setSpecialEffect(specialEffect);
        changed = true;
        if ((eff.getTeleportX() > 0) || (eff.getTeleportY() > 0)) {
          getResponder().getCommunicator().sendNormalServerMessage("The special effect will affect tile " + eff
            .getTeleportX() + "," + eff.getTeleportY() + ".");
        }
      }
      if (achievementId != eff.getAchievementId())
      {
        eff.setAchievementId(achievementId);
        changed = true;
      }
      if (rewardSkillNum != eff.getRewardSkillNum())
      {
        eff.setRewardSkillNum(rewardSkillNum);
        changed = true;
      }
      if (rewardSkillVal != eff.getRewardSkillModifier())
      {
        eff.setRewardSkillVal(rewardSkillVal);
        changed = true;
      }
      if (missionAffectedId != eff.getMissionId())
      {
        eff.setMission(missionAffectedId);
        changed = true;
      }
      if (missionStateChange != eff.getMissionStateChange())
      {
        eff.setMissionStateChange(missionStateChange);
        changed = true;
      }
      if (destroysTarget != eff.destroysTarget())
      {
        eff.setDestroysTarget(destroysTarget);
        changed = true;
      }
      if (!sound.equals(eff.getSoundName()))
      {
        eff.setSoundName(sound);
        changed = true;
      }
      if (setWindowSizeY != eff.getWindowSizeY())
      {
        eff.setWindowSizeY(setWindowSizeY);
        changed = true;
      }
      if (setWindowSizeX != eff.getWindowSizeX())
      {
        eff.setWindowSizeX(setWindowSizeX);
        changed = true;
      }
      if (!top.equals(eff.getTopText()))
      {
        eff.setTopText(top);
        changed = true;
      }
      if (!textdisplayed.equals(eff.getTextDisplayed()))
      {
        eff.setTextDisplayed(textdisplayed);
        changed = true;
      }
      if (this.level == 7)
      {
        if (changed)
        {
          eff.setLastModifierName(getResponder().getName());
          eff.update();
          getResponder().getCommunicator().sendNormalServerMessage("You update the effect " + eff.getName() + ".");
        }
        return parseBack();
      }
      if (changed)
      {
        if (getResponder().getPower() == 0)
        {
          if (ruler.getAuxData() <= 0)
          {
            getResponder().getCommunicator().sendAlertServerMessage("Your " + ruler
              .getName() + " contains no charges. You can not create a new trigger effect.");
            return parseBack();
          }
          ruler.setAuxData((byte)(ruler.getAuxData() - 1));
          getResponder().getCommunicator().sendAlertServerMessage("You spend a charge from your " + ruler
            .getName() + ". It now has " + ruler.getAuxData() + " charges left.");
          
          eff.setCreatorType((byte)3);
        }
        else
        {
          eff.setCreatorType((byte)1);
        }
        eff.setOwnerId(getResponder().getWurmId());
        eff.create();
        TriggerEffects.addTriggerEffect(eff);
        this.effectId = eff.getId();
        getResponder().getCommunicator().sendNormalServerMessage("You create the effect " + eff.getName() + ".");
        return editEffect(this.effectId, null);
      }
      if (!changed) {
        getResponder().getCommunicator().sendNormalServerMessage("You change nothing.");
      }
      return parseBack();
    }
    boolean createTrigger = getBooleanProp("createTrigger");
    if (createTrigger)
    {
      this.sbacks = (this.sbacks + "|" + this.level + "," + this.missionId + "," + this.triggerId + "," + this.effectId);
      return createNewTrigger(null);
    }
    boolean linkTrigger = getBooleanProp("linkTrigger");
    int trig;
    if (linkTrigger)
    {
      trig = indexUnlinkedTrigger("trigs", "Unlinked trigger");
      if (trig > 0)
      {
        MissionTrigger mtrig = MissionTriggers.getTriggerWithId(trig);
        if (mtrig != null)
        {
          Triggers2Effects.addLink(mtrig.getId(), this.effectId, false);
          getResponder().getCommunicator().sendNormalServerMessage("You link the trigger '" + mtrig
            .getName() + "' to the effect '" + name + "'.");
        }
      }
      return reshow();
    }
    for (String key : getAnswer().stringPropertyNames())
    {
      boolean edtT = key.startsWith("edtT");
      boolean delT = key.startsWith("delT");
      boolean unlT = key.startsWith("unlT");
      if ((edtT) || (delT) || (unlT))
      {
        String sid = key.substring(4);
        int tid = Integer.parseInt(sid);
        MissionTrigger trg = MissionTriggers.getTriggerWithId(tid);
        if (trg == null)
        {
          this.errorText = "Cannot find trigger!";
          getResponder().getCommunicator().sendNormalServerMessage(this.errorText);
          return reshow();
        }
        if (edtT)
        {
          this.sbacks = (this.sbacks + "|" + this.level + "," + this.missionId + "," + this.triggerId + ",0");
          return editTrigger(tid, null);
        }
        if (delT) {
          return reshow();
        }
        if (unlT)
        {
          Triggers2Effects.deleteLink(tid, this.effectId);
          getResponder().getCommunicator().sendNormalServerMessage("You unlink the trigger '" + trg
            .getName() + "' from the effect '" + name + "'.");
          return editEffect(this.effectId, null);
        }
      }
    }
    return false;
  }
  
  private long readContainerId()
  {
    String svalue = getStringProp("rewardTargetContainerId");
    long id = 0L;
    try
    {
      id = Long.parseLong(svalue);
    }
    catch (NumberFormatException nfe)
    {
      if (this.errorText.isEmpty()) {
        this.errorText = "Failed to parse value for rewardTargetContainerId.";
      }
    }
    if (id > 0L) {
      try
      {
        Item i = Items.getItem(id);
        if ((i.isTraded()) || (i.isTransferred()) || (i.mailed))
        {
          id = 0L;
          if (this.errorText.isEmpty()) {
            this.errorText = ("The " + i.getName() + " may not be used as container right now.");
          }
        }
      }
      catch (NoSuchItemException nsi)
      {
        id = 0L;
        if (this.errorText.isEmpty()) {
          this.errorText = "Container not found!";
        }
      }
    }
    return id;
  }
  
  private boolean parseMission(Item ruler)
  {
    boolean delete = getBooleanProp("deleteMission");
    Mission m = Missions.getMissionWithId(this.missionId);
    if (m == null)
    {
      if (delete)
      {
        getResponder().getCommunicator().sendNormalServerMessage("You tried to delete a non-existing mission.");
        return showMissionList();
      }
      m = new Mission(getResponder().getName(), getResponder().getName());
    }
    else if (delete)
    {
      m.destroy();
      getResponder().getCommunicator().sendNormalServerMessage("You delete the mission.");
      return showMissionList();
    }
    this.errorText = "";
    String name = m.getName();
    String group = m.getGroupName();
    String intro = m.getInstruction();
    boolean inactive = m.isInactive();
    boolean hidden = m.isHidden();
    boolean faildeath = m.isFailOnDeath();
    boolean mayBeRestarted = m.mayBeRestarted();
    boolean hasSecondChance = m.hasSecondChance();
    int maxTimeSeconds = m.getMaxTimeSeconds();
    
    boolean cloneMission = getBooleanProp("cloneMission");
    if (cloneMission)
    {
      this.sbacks = (this.sbacks + "|" + 2 + "," + this.missionId + ",0,0");
      return createNewMission(getAnswer());
    }
    boolean updateMission = getBooleanProp("updateMission");
    boolean createMission = getBooleanProp("createMission");
    if ((updateMission) || (createMission))
    {
      name = getStringProp("name", 3);
      group = getStringProp("groupName");
      intro = getStringProp("intro");
      inactive = getBooleanProp("inactive");
      hidden = getBooleanProp("hidden");
      faildeath = getBooleanProp("faildeath");
      mayBeRestarted = getBooleanProp("mayBeRestarted");
      hasSecondChance = getBooleanProp("secondChance");
      
      int days = getIntProp("days");
      maxTimeSeconds = (int)(days * 86400L);
      int hours = getIntProp("hours", 0, 23, false);
      maxTimeSeconds = (int)(maxTimeSeconds + hours * 3600L);
      int minutes = getIntProp("minutes", 0, 59, false);
      maxTimeSeconds = (int)(maxTimeSeconds + minutes * 60L);
      int secs = getIntProp("seconds", 0, 59, false);
      maxTimeSeconds += secs;
      if (this.errorText.length() > 0) {
        return editMission(this.missionId, getAnswer());
      }
      boolean changed = false;
      if (!name.equals(m.getName()))
      {
        m.setName(name);
        changed = true;
      }
      if (!group.equals(m.getGroupName()))
      {
        m.setGroupName(group);
        changed = true;
      }
      if (!intro.equals(m.getInstruction()))
      {
        m.setInstruction(intro);
        changed = true;
      }
      if (inactive != m.isInactive())
      {
        m.setInactive(inactive);
        changed = true;
      }
      if (hidden != m.isHidden())
      {
        m.setIsHidden(hidden);
        changed = true;
      }
      if (faildeath != m.isFailOnDeath())
      {
        m.setFailOnDeath(faildeath);
        changed = true;
      }
      if (mayBeRestarted != m.mayBeRestarted())
      {
        m.setMayBeRestarted(mayBeRestarted);
        changed = true;
      }
      if (hasSecondChance != m.hasSecondChance())
      {
        m.setSecondChance(hasSecondChance);
        changed = true;
      }
      if (maxTimeSeconds != m.getMaxTimeSeconds())
      {
        m.setMaxTimeSeconds(maxTimeSeconds);
        changed = true;
      }
      if (this.level == 1)
      {
        if (changed)
        {
          if (getResponder().getPower() == 0)
          {
            if (ruler.getAuxData() <= 0)
            {
              getResponder().getCommunicator().sendAlertServerMessage("Your " + ruler
                .getName() + " contains no charges. You can not create a new mission.");
              
              return showMissionList();
            }
            ruler.setAuxData((byte)(ruler.getAuxData() - 1));
            getResponder().getCommunicator().sendAlertServerMessage("You spend a charge from your " + ruler
              .getName() + ". It now has " + ruler.getAuxData() + " charges left.");
            
            m.setCreatorType((byte)3);
          }
          else
          {
            m.setCreatorType((byte)1);
          }
          m.setOwnerId(getResponder().getWurmId());
          m.create();
          Missions.addMission(m);
          getResponder().getCommunicator().sendNormalServerMessage("You create the mission " + m.getName() + ".");
          this.missionId = m.getId();
          return editMission(this.missionId, null);
        }
        getResponder().getCommunicator().sendNormalServerMessage("You decide not to create a new mission.");
        
        return parseBack();
      }
      if (changed)
      {
        m.setLastModifierName(getResponder().getName());
        m.update();
        getResponder().getCommunicator().sendNormalServerMessage("You update the mission " + m.getName() + ".");
      }
      if (!changed) {
        getResponder().getCommunicator().sendNormalServerMessage("You change nothing.");
      }
      return parseBack();
    }
    boolean createTrigger = getBooleanProp("createTrigger");
    if (createTrigger)
    {
      this.sbacks = (this.sbacks + "|" + this.level + "," + this.missionId + "," + this.triggerId + "," + this.effectId);
      return createNewTrigger(null);
    }
    boolean linkTrigger = getBooleanProp("linkTrigger");
    if (linkTrigger)
    {
      int utrig = indexUnlinkedTrigger("unTrigs", "Unlinked trigger");
      if (utrig > 0)
      {
        MissionTrigger mtrig = MissionTriggers.getTriggerWithId(utrig);
        if (mtrig != null)
        {
          mtrig.setMissionRequirement(this.missionId);
          getResponder().getCommunicator().sendNormalServerMessage("You link the trigger '" + mtrig
            .getName() + "' to the mission '" + name + "'.");
        }
      }
      return reshow();
    }
    boolean linkEffect = getBooleanProp("linkEffect");
    TriggerEffect mEff;
    if (linkEffect)
    {
      this.sbacks = (this.sbacks + "|" + this.level + "," + this.missionId + "," + this.triggerId + "," + this.effectId);
      
      int uEff = indexUnlinkedEffect("unEffs", "Unlinked effects");
      if (uEff > 0)
      {
        mEff = TriggerEffects.getTriggerEffect(uEff);
        if (mEff != null)
        {
          mEff.setMission(this.missionId);
          getResponder().getCommunicator().sendNormalServerMessage("You link the effect '" + mEff
            .getName() + "' to the mision '" + name + "'.");
        }
      }
      return reshow();
    }
    boolean createEffect = getBooleanProp("createEffect");
    if (createEffect)
    {
      this.sbacks = (this.sbacks + "|" + this.level + "," + this.missionId + "," + this.triggerId + "," + this.effectId);
      return createNewEffect(null);
    }
    for (String key : getAnswer().stringPropertyNames())
    {
      boolean edtT = key.startsWith("edtT");
      boolean delT = key.startsWith("delT");
      boolean unlT = key.startsWith("unlT");
      if ((edtT) || (delT) || (unlT))
      {
        String sid = key.substring(4);
        int tid = Integer.parseInt(sid);
        MissionTrigger trg = MissionTriggers.getTriggerWithId(tid);
        if (trg == null)
        {
          this.errorText = "Cannot find trigger!";
          getResponder().getCommunicator().sendNormalServerMessage(this.errorText);
          return reshow();
        }
        if (edtT)
        {
          this.sbacks = (this.sbacks + "|" + this.level + "," + this.missionId + "," + this.triggerId + ",0");
          return editTrigger(tid, null);
        }
        if (delT) {
          return reshow();
        }
        if (unlT)
        {
          trg.setMissionRequirement(0);
          getResponder().getCommunicator().sendNormalServerMessage("You unlink the trigger '" + trg
            .getName() + "' from the mission.");
          return reshow();
        }
      }
      boolean edtE = key.startsWith("edtE");
      boolean delE = key.startsWith("delE");
      boolean unlE = key.startsWith("unlE");
      boolean clrE = key.startsWith("clrE");
      if ((edtE) || (delE) || (unlE) || (clrE))
      {
        String sid = key.substring(4);
        int eid = Integer.parseInt(sid);
        TriggerEffect te = TriggerEffects.getTriggerEffect(eid);
        if (te == null)
        {
          this.errorText = "Cannot find effect!";
          getResponder().getCommunicator().sendNormalServerMessage(this.errorText);
          return reshow();
        }
        if (edtE)
        {
          this.sbacks = (this.sbacks + "|" + this.level + "," + this.missionId + "," + this.triggerId + ",0");
          return editEffect(eid, null);
        }
        if (delE) {
          return reshow();
        }
        if (unlE)
        {
          te.setMission(0);
          getResponder().getCommunicator().sendNormalServerMessage("You unlink the effect '" + te
            .getName() + "' from the mission.");
          
          return reshow();
        }
        if (clrE)
        {
          te.setTextDisplayed("");
          te.setTopText("");
          getResponder().getCommunicator().sendNormalServerMessage("You clear the text for trigger effect " + te
            .getName() + ".");
          logger.log(Level.INFO, getResponder().getName() + " cleared text of effect " + te.getName());
          return reshow();
        }
      }
    }
    return false;
  }
  
  private boolean parseTrigger(Item ruler)
  {
    boolean delete = getBooleanProp("deleteTrigger");
    MissionTrigger trg = MissionTriggers.getTriggerWithId(this.triggerId);
    if (trg == null)
    {
      if (delete)
      {
        getResponder().getCommunicator().sendNormalServerMessage("You tried to delete a non-existing mission trigger.");
        return showTriggerList(this.missionId);
      }
      trg = new MissionTrigger();
      trg.setCreatorName(getResponder().getName());
      trg.setLastModifierName(getResponder().getName());
    }
    else if (delete)
    {
      trg.destroy();
      getResponder().getCommunicator().sendNormalServerMessage("You delete the mission trigger.");
      return showTriggerList(this.missionId);
    }
    if ((getResponder().getLogger() != null) && (getResponder().getPower() > 0) && (trg.getName() != null)) {
      getResponder().getLogger().info(getResponder() + ": Editing mission trigger with trigger name: " + trg.getName() + " and description " + trg.getDescription());
    }
    this.errorText = "";
    String name = trg.getName();
    
    boolean inActive = trg.isInactive();
    String description = trg.getDescription();
    
    int onItemCreatedId = trg.getItemUsedId();
    
    int onActionPerformed = trg.getOnActionPerformed();
    
    long onActionTargetId = trg.getTarget();
    boolean useCurrentTarget = onActionTargetId == this.currentTargetId;
    
    boolean spawnpoint = false;
    spawnpoint = trg.isSpawnPoint();
    int seconds = trg.getSeconds();
    
    int missionRequired = trg.getMissionRequired();
    
    float stateFrom = trg.getStateRequired();
    float stateTo = trg.getStateEnd();
    
    boolean cloneTrigger = getBooleanProp("cloneTrigger");
    if (cloneTrigger)
    {
      this.sbacks = (this.sbacks + "|" + 3 + "," + this.missionId + "," + this.triggerId + ",0");
      return createNewTrigger(getAnswer());
    }
    boolean updateTrigger = getBooleanProp("updateTrigger");
    boolean createTrigger = getBooleanProp("createTrigger");
    if ((updateTrigger) || (createTrigger))
    {
      name = getStringProp("name", 3);
      description = getStringProp("desc");
      inActive = getBooleanProp("inactive");
      spawnpoint = getBooleanProp("spawnpoint");
      onItemCreatedId = indexItemTemplate("onItemCreatedId", "Item Created");
      onActionPerformed = indexActionId("actionId", "action");
      useCurrentTarget = getBooleanProp("useCurrentTarget");
      if (useCurrentTarget)
      {
        if ((getResponder().getPower() == 0) && (this.target <= 0L))
        {
          if (this.errorText.isEmpty()) {
            this.errorText = "The trigger needs a valid target.";
          }
        }
        else {
          onActionTargetId = this.currentTargetId;
        }
      }
      else
      {
        onActionTargetId = getLongProp("targetid");
        if ((getResponder().getPower() == 0) && (onActionTargetId <= 0L)) {
          if (this.errorText.isEmpty()) {
            this.errorText = "The trigger needs a valid target.";
          }
        }
      }
      this.currentTargetId = onActionTargetId;
      missionRequired = indexMission("missionRequired", "available missions");
      if (onActionPerformed == 475) {
        if (WurmId.getType(onActionTargetId) == 3) {
          if (getResponder().getPower() <= 0)
          {
            int tilex = Tiles.decodeTileX(onActionTargetId);
            int tiley = Tiles.decodeTileY(onActionTargetId);
            Village v = Villages.getVillage(tilex, tiley, true);
            if ((v == null) || (v != getResponder().getCitizenVillage()))
            {
              if (this.errorText.isEmpty()) {
                this.errorText = "You are only allowed to set the step on trigger action in your own settlement.";
              }
              onActionPerformed = 0;
            }
          }
        }
      }
      stateFrom = getFloatProp("stateFrom", -1.0F, 100.0F, true);
      stateTo = getFloatProp("stateTo", -1.0F, 100.0F, true);
      if (stateTo < stateFrom) {
        stateTo = 0.0F;
      }
      seconds = getIntProp("seconds", 0, 59, false);
      if (this.errorText.length() > 0) {
        return editTrigger(this.triggerId, getAnswer());
      }
      boolean changed = false;
      if (!name.equals(trg.getName()))
      {
        trg.setName(name);
        changed = true;
      }
      if (!description.equals(trg.getDescription()))
      {
        trg.setDescription(description);
        changed = true;
      }
      if (inActive != trg.isInactive())
      {
        trg.setInactive(inActive);
        changed = true;
      }
      if (spawnpoint != trg.isSpawnPoint())
      {
        trg.setIsSpawnpoint(spawnpoint);
        changed = true;
      }
      if (onActionTargetId != trg.getTarget())
      {
        if (this.level == 3) {
          MissionTargets.removeMissionTrigger(trg, false);
        }
        trg.setOnTargetId(onActionTargetId);
        if (this.level == 3) {
          MissionTargets.addMissionTrigger(trg);
        }
        changed = true;
      }
      if (missionRequired != trg.getMissionRequired())
      {
        trg.setMissionRequirement(missionRequired);
        changed = true;
      }
      if (stateFrom != trg.getStateRequired())
      {
        trg.setStateRequirement(stateFrom);
        changed = true;
      }
      if (stateTo != trg.getStateEnd())
      {
        trg.setStateEnd(stateTo);
        changed = true;
      }
      if ((seconds > 0) && (Action.isQuick(onActionPerformed)))
      {
        seconds = 0;
        getResponder().getCommunicator().sendAlertServerMessage("Seconds were set to 0 for that action since it is quick.");
      }
      if (seconds != trg.getSeconds())
      {
        trg.setSeconds(seconds);
        changed = true;
      }
      if (onItemCreatedId != trg.getItemUsedId())
      {
        trg.setOnItemUsedId(onItemCreatedId);
        changed = true;
      }
      if (onActionPerformed != trg.getOnActionPerformed())
      {
        trg.setOnActionPerformed(onActionPerformed);
        changed = true;
      }
      if (this.level == 3)
      {
        if (changed)
        {
          trg.setLastModifierName(getResponder().getName());
          trg.update();
          getResponder().getCommunicator().sendNormalServerMessage("You change the trigger.");
        }
        return parseBack();
      }
      if (changed)
      {
        if (getResponder().getPower() == 0)
        {
          if (ruler.getAuxData() <= 0)
          {
            getResponder().getCommunicator().sendAlertServerMessage("Your " + ruler
              .getName() + " contains no charges. You can not create a new mission trigger.");
            return parseBack();
          }
          ruler.setAuxData((byte)(ruler.getAuxData() - 1));
          getResponder().getCommunicator().sendAlertServerMessage("You spend a charge from your " + ruler
            .getName() + ". It now has " + ruler.getAuxData() + " charges left.");
          
          trg.setCreatorType((byte)3);
        }
        else
        {
          trg.setCreatorType((byte)1);
        }
        trg.setOwnerId(getResponder().getWurmId());
        trg.create();
        this.triggerId = trg.getId();
        MissionTriggers.addMissionTrigger(trg);
        getResponder().getCommunicator().sendNormalServerMessage("You create the trigger " + trg.getName() + ".");
        return editTrigger(this.triggerId, null);
      }
      if (!changed) {
        getResponder().getCommunicator().sendNormalServerMessage("You change nothing.");
      }
      return parseBack();
    }
    boolean createEffect = getBooleanProp("createEffect");
    if (createEffect)
    {
      this.sbacks = (this.sbacks + "|" + this.level + "," + this.missionId + "," + this.triggerId + ",0");
      return createNewEffect(null);
    }
    boolean linkEffect = getBooleanProp("linkEffect");
    int effe;
    if (linkEffect)
    {
      effe = indexUnlinkedEffect("linkEffects", "all effects");
      if (effe > 0)
      {
        TriggerEffect meff = TriggerEffects.getTriggerEffect(effe);
        if (meff != null)
        {
          Triggers2Effects.addLink(this.triggerId, meff.getId(), false);
          getResponder().getCommunicator().sendNormalServerMessage("You link the effect '" + meff
            .getName() + "' to the trigger '" + name + "'.");
        }
      }
      return editTrigger(this.triggerId, null);
    }
    for (String key : getAnswer().stringPropertyNames())
    {
      boolean edtE = key.startsWith("edtE");
      boolean delE = key.startsWith("delE");
      boolean unlE = key.startsWith("unlE");
      boolean clrE = key.startsWith("clrE");
      if ((edtE) || (delE) || (unlE) || (clrE))
      {
        String sid = key.substring(4);
        int eid = Integer.parseInt(sid);
        TriggerEffect te = TriggerEffects.getTriggerEffect(eid);
        if (te == null)
        {
          this.errorText = "Cannot find effect!";
          getResponder().getCommunicator().sendNormalServerMessage(this.errorText);
          return reshow();
        }
        if (edtE)
        {
          this.sbacks = (this.sbacks + "|" + this.level + "," + this.missionId + "," + this.triggerId + ",0");
          return editEffect(eid, null);
        }
        if (delE) {
          return reshow();
        }
        if (unlE)
        {
          Triggers2Effects.deleteLink(this.triggerId, eid);
          getResponder().getCommunicator().sendNormalServerMessage("You unlink the effect '" + te
            .getName() + "' from the trigger '" + name + "'.");
          return editTrigger(this.triggerId, null);
        }
        if (clrE)
        {
          te.setTextDisplayed("");
          te.setTopText("");
          getResponder().getCommunicator().sendNormalServerMessage("You clear the text for trigger effect " + te
            .getName() + ".");
          logger.log(Level.INFO, getResponder().getName() + " cleared text of effect " + te.getName());
          return reshow();
        }
      }
    }
    return false;
  }
  
  private void sendMissionList()
  {
    StringBuilder buf = new StringBuilder();
    
    buf.append("border{border{size=\"20,40\";null;null;varray{rescale=\"true\";harray{label{type='bold';text=\"" + this.question + "\"};");
    if (getResponder().getPower() > 0)
    {
      buf.append("label{text=\"Group: \";hover=\"TODO\"};input{id=\"groupName\";text=\"" + this.groupName + "\";maxchars=\"20\"}label{text=\" Player: \"};input{id=\"specialName\";text=\"" + this.userName + "\";maxchars=\"30\"}}");
      
      buf.append("harray{checkbox{id=\"incMInactive\";text=\"Include inactive      \"" + 
        addSel(this.incMInactive) + ";hover=\"TODO\"};label{text=\"Include:\"};radio{group=\"includeM\";id=\"" + 0 + "\";text=\"All  \"" + 
        
        addSel(this.includeM == 0) + ";hover=\"TODO\"};radio{group=\"includeM\";id=\"" + 1 + "\";text=\"With triggers  \"" + 
        addSel(this.includeM == 1) + ";hover=\"TODO\"};radio{group=\"includeM\";id=\"" + 2 + "\";text=\"Without triggers  \"" + 
        addSel(this.includeM == 2) + ";hover=\"TODO\"};}");
    }
    else
    {
      buf.append("label{text=\"Group: \";hover=\"TODO\"};input{id=\"groupName\";text=\"" + this.groupName + "\";maxchars=\"20\"}};");
      
      buf.append(appendChargeInfo());
    }
    buf.append("}varray{harray{label{text=\"           \"};button{text=\"Back\";id=\"back\"};label{text=\" \"}}harray{label{text=\" \"};button{text=\"Apply Filter\";id=\"filter\"};label{text=\" \"}}}null;}null;scroll{vertical=\"true\";horizontal=\"false\";varray{rescale=\"true\";passthrough{id=\"id\";text=\"" + 
    
      getId() + "\"}");
    int absSortBy = Math.abs(this.sortBy);
    int upDown = Integer.signum(this.sortBy);
    
    buf.append("table{rows=\"1\";cols=\"6\";label{text=\"\"};" + 
    
      colHeader("Id", 1, this.sortBy) + 
      colHeader("Group", 2, this.sortBy) + 
      colHeader("Name", 3, this.sortBy) + 
      colHeader("Owner", 4, this.sortBy) + 
      colHeader("Last Modified", 5, this.sortBy));
    
    Mission[] missions = Missions.getFilteredMissions(getResponder(), this.includeM, this.incMInactive, this.dontListMine, this.listMineOnly, this.listForUser, this.groupName, this.onlyCurrent, this.currentTargetId);
    if (missions.length > 0)
    {
      switch (absSortBy)
      {
      case 1: 
        Arrays.sort(missions, new MissionManager.1(this, upDown));
        
        break;
      case 2: 
        Arrays.sort(missions, new MissionManager.2(this));
        
        Arrays.sort(missions, new MissionManager.3(this, upDown));
        
        break;
      case 3: 
        Arrays.sort(missions, new MissionManager.4(this, upDown));
        
        break;
      case 4: 
        Arrays.sort(missions, new MissionManager.5(this, upDown));
        
        Arrays.sort(missions, new MissionManager.6(this, upDown));
        
        break;
      case 5: 
        Arrays.sort(missions, new MissionManager.7(this, upDown));
      }
      for (Mission mission : missions)
      {
        String colour = "color=\"127,255,127\"";
        String hover = ";hover=\"Active\"";
        if (!mission.hasTriggers())
        {
          colour = "color=\"255,177,40\"";
          hover = ";hover=\"No Triggers\"";
        }
        else if (mission.isInactive())
        {
          colour = "color=\"255,127,127\"";
          hover = ";hover=\"Inactive\"";
        }
        else if (mission.hasTargetOf(this.currentTargetId, getResponder()))
        {
          colour = "color=\"140,140,255\";";
          hover = ";hover=\"Current Target\"";
        }
        buf.append("radio{group=\"sel\";id=\"" + mission.getId() + "\"};label{" + colour + "text=\"" + mission
          .getId() + " \"" + hover + "};label{" + colour + "text=\"" + mission
          .getGroupName() + "\"" + hover + "};label{" + colour + "text=\"" + mission
          .getName() + " \"" + hover + "};label{" + colour + "text=\"" + mission
          .getOwnerName() + " \"" + hover + "};label{" + colour + "text=\"" + mission
          .getLastModifiedString() + " \"" + hover + "};");
      }
    }
    buf.append("}");
    buf.append("radio{group=\"sel\";id=\"0\";selected=\"true\";text=\"None\"}");
    
    buf.append("}};null;");
    buf.append("varray{rescale=\"true\";");
    
    buf.append("text{text=\"Select mission and choose what to do\"}");
    buf.append("harray{button{id=\"editMission\";text=\"Edit Mission\";hover=\"If 'None' is selected then this will create a new mission.\"};label{text=\"  \"};button{id=\"showStats\";text=\"Show Stats \"};label{text=\"  \"};button{id=\"listTriggers\";text=\"List Triggers\";hover=\"If 'None' is selected then this will list all triggers.\"}label{text=\"  \"};button{id=\"listEffects\";text=\"List Effects \";hover=\"If 'None' is selected then this will list all effects.\"}label{text=\"  Or \"};button{id=\"createMission\";text=\"Create New Mission \"}}");
    
    buf.append("}");
    buf.append("}");
    getResponder().getCommunicator().sendBml(600, 500, true, true, buf.toString(), 200, 200, 200, this.title);
  }
  
  private void sendTriggerList()
  {
    StringBuilder buf = new StringBuilder();
    
    buf.append("border{border{size=\"20,40\";null;null;varray{rescale=\"true\";harray{label{type='bold';text=\"" + this.question + "\"};}");
    
    buf.append("harray{checkbox{id=\"incTInactive\";text=\"Include inactive      \"" + 
      addSel(this.incTInactive) + ";hover=\"TODO\"};label{text=\"Show:\"};radio{group=\"showT\";id=\"" + 0 + "\";text=\"All  \"" + 
      
      addSel(this.showT == 0) + "};radio{group=\"showT\";id=\"" + 1 + "\";text=\"Linked to missions\"" + 
      addSel(this.showT == 1) + "}radio{group=\"showT\";id=\"" + 2 + "\";text=\"Unlinked\"" + 
      addSel(this.showT == 2) + "}}");
    
    buf.append("}varray{harray{label{text=\"           \"};button{text=\"Back\";id=\"back\"};label{text=\" \"}}harray{label{text=\" \"};button{text=\"Apply Filter\";id=\"filter\"};label{text=\" \"}}}null;}null;scroll{vertical=\"true\";horizontal=\"false\";varray{rescale=\"true\";passthrough{id=\"id\";text=\"" + 
    
      getId() + "\"}");
    
    int absSortBy = Math.abs(this.sortBy);
    int upDown = Integer.signum(this.sortBy);
    MissionTrigger[] trigs = MissionTriggers.getFilteredTriggers(getResponder(), this.showT, this.incTInactive, this.missionId, 0);
    if (trigs.length > 0)
    {
      switch (absSortBy)
      {
      case 1: 
        Arrays.sort(trigs, new MissionManager.8(this, upDown));
        
        break;
      case 2: 
        Arrays.sort(trigs, new MissionManager.9(this, upDown));
        
        break;
      case 3: 
        Arrays.sort(trigs, new MissionManager.10(this, upDown));
        
        break;
      case 4: 
        Arrays.sort(trigs, new MissionManager.11(this, upDown));
        
        break;
      case 5: 
        Arrays.sort(trigs, new MissionManager.12(this, upDown));
      }
      buf.append("table{rows=\"1\";cols=\"7\";label{text=\"\"};" + 
      
        colHeader("Id", 1, this.sortBy) + 
        colHeader("Name", 2, this.sortBy) + 
        colHeader("State", 3, this.sortBy) + 
        colHeader("Action", 4, this.sortBy) + 
        colHeader("Target", 5, this.sortBy) + "label{text=\"\"};");
      for (MissionTrigger trigger : trigs)
      {
        String colour = "color=\"127,255,127\"";
        String hover = ";hover=\"Active\"";
        if (trigger.isInactive())
        {
          colour = "color=\"255,127,127\"";
          hover = ";hover=\"Inactive\"";
        }
        else if (trigger.hasTargetOf(this.currentTargetId, getResponder()))
        {
          colour = "color=\"140,140,255\";";
          hover = ";hover=\"Current Target\"";
        }
        buf.append("radio{group=\"sel\";id=\"" + trigger.getId() + "\"};label{" + colour + "text=\"" + trigger
          .getId() + " \"" + hover + "};label{" + colour + "text=\"" + trigger
          .getName() + " \"" + hover + "};label{" + colour + "text=\"" + trigger
          .getStateRange() + " \"" + hover + "};label{" + colour + "text=\"" + trigger
          .getActionString() + "\"" + hover + "};label{" + colour + "text=\"" + trigger
          .getTargetAsString(getResponder()) + "\"" + hover + "};harray{label{text=\" \"}button{id=\"delT" + trigger
          
          .getId() + "\";text=\"Delete\"hover=\"This will delete " + trigger
          .getName() + "\";confirm=\"You are about to delete " + trigger
          .getName() + ".\";question=\"Do you really want to do that?\"};};");
      }
      buf.append("}");
    }
    buf.append("radio{group=\"sel\";id=\"0\";selected=\"true\";text=\"None\"}");
    buf.append("}};null;");
    
    buf.append("varray{rescale=\"true\";");
    
    buf.append("text{text=\"Select trigger and choose what to do\"}");
    buf.append("harray{button{id=\"editTrigger\";text=\"Edit Trigger\"};label{text=\"  \"};button{id=\"listEffects\";text=\"List Effects \";hover=\"If 'None' is selected then this will list all effects.\"}label{text=\"  Or \"};button{id=\"createTrigger\";text=\"Create New Trigger\"}}");
    
    buf.append("}");
    buf.append("}");
    
    getResponder().getCommunicator().sendBml(500, 400, true, true, buf.toString(), 200, 200, 200, this.title);
  }
  
  private void sendEffectList()
  {
    StringBuilder buf = new StringBuilder();
    
    buf.append("border{border{size=\"20,40\";null;null;varray{rescale=\"true\";harray{label{type='bold';text=\"" + this.question + "\"};}");
    
    buf.append("harray{checkbox{id=\"incEInactive\";text=\"Include inactive  \"" + 
      addSel(this.incEInactive) + "};label{text=\"Show:\"};radio{group=\"showE\";id=\"" + 0 + "\";text=\"All  \"" + 
      
      addSel(this.showE == 0) + "};radio{group=\"showE\";id=\"" + 1 + "\";text=\"Linked to trigger\"" + 
      addSel(this.showE == 1) + "}radio{group=\"showE\";id=\"" + 2 + "\";text=\"Unlinked\"" + 
      addSel(this.showE == 2) + "}}");
    
    buf.append("}varray{harray{label{text=\"           \"};button{text=\"Back\";id=\"back\"};label{text=\" \"}}harray{label{text=\" \"};button{text=\"Apply Filter\";id=\"filter\"};label{text=\" \"}}}null;}null;scroll{vertical=\"true\";horizontal=\"false\";varray{rescale=\"true\";passthrough{id=\"id\";text=\"" + 
    
      getId() + "\"}");
    
    int absSortBy = Math.abs(this.sortBy);
    int upDown = Integer.signum(this.sortBy);
    
    boolean showAll = (this.missionId == 0) && (this.triggerId == 0);
    MissionTrigger[] trigs = MissionTriggers.getFilteredTriggers(getResponder(), this.showT, this.incTInactive, this.missionId, this.triggerId);
    TriggerEffect[] effs = TriggerEffects.getFilteredEffects(trigs, getResponder(), this.showE, this.incEInactive, this.dontListMine, this.listMineOnly, this.listForUser, showAll);
    if (effs.length > 0)
    {
      switch (absSortBy)
      {
      case 1: 
        Arrays.sort(effs, new MissionManager.13(this, upDown));
        
        break;
      case 2: 
        Arrays.sort(effs, new MissionManager.14(this, upDown));
        
        break;
      case 3: 
        Arrays.sort(effs, new MissionManager.15(this, upDown));
        
        break;
      case 4: 
        Arrays.sort(effs, new MissionManager.16(this, upDown));
      }
      buf.append("table{rows=\"1\";cols=\"7\";");
      buf.append("label{text=\"\"};" + 
        colHeader("Id", 1, this.sortBy) + 
        colHeader("Name", 2, this.sortBy) + 
        colHeader("+State", 3, this.sortBy) + 
        colHeader("Type", 4, this.sortBy) + "label{text=\"\"};label{text=\"\"};");
      for (TriggerEffect lEff : effs)
      {
        String colour = "color=\"127,255,127\"";
        String hover = ";hover=\"Active\"";
        if (lEff.isInactive())
        {
          colour = "color=\"255,127,127\"";
          hover = ";hover=\"Inactive\"";
        }
        else if (lEff.hasTargetOf(this.currentTargetId, getResponder()))
        {
          colour = "color=\"140,140,255\";";
          hover = ";hover=\"Current Target\"";
        }
        String clrtxt = "label{text=\"  no text to clear\"}";
        if (!lEff.getTextDisplayed().isEmpty()) {
          clrtxt = "harray{label{text=\" \";}button{id=\"clrE" + lEff.getId() + "\";text=\"Clear Text\"};}";
        }
        buf.append("radio{group=\"sel\";id=\"" + lEff.getId() + "\"};label{" + colour + "text=\"" + lEff
          .getId() + " \"" + hover + "};label{" + colour + "text=\"" + lEff
          .getName() + " \"" + hover + "};label{" + colour + "text=\"" + lEff
          .getMissionStateChange() + " \"" + hover + "};label{" + colour + "text=\"" + lEff
          .getType() + " \"" + hover + "};harray{label{text=\" \";}button{id=\"delE" + lEff
          
          .getId() + "\";text=\"Delete\"hover=\"This will delete " + lEff
          .getName() + "\";confirm=\"You are about to delete " + lEff
          .getName() + ".\";question=\"Do you really want to do that?\"};};" + clrtxt);
      }
      buf.append("}");
    }
    buf.append("radio{group=\"sel\";id=\"0\";selected=\"true\";text=\"None\"}");
    
    buf.append("}};null;");
    
    buf.append("varray{rescale=\"true\";");
    
    buf.append("text{text=\"Select effect and choose what to do\"}");
    buf.append("harray{button{id=\"editEffect\";text=\"Edit Effect\"};label{text=\"  \"};button{id=\"showTriggers\";text=\"Show Triggers\";hover=\"Not implemented (yet)\"};label{text=\"  Or \"};button{id=\"createEffect\";text=\"Create New Effect\"}}");
    
    buf.append("}");
    buf.append("}");
    
    getResponder().getCommunicator().sendBml(500, 400, true, true, buf.toString(), 200, 200, 200, this.title);
  }
  
  private String addSel(boolean value)
  {
    return ";selected=\"" + (value ? "true" : "false") + "\"";
  }
  
  private int getIntProp(String key)
  {
    int value = 0;
    String svalue = getStringProp(key);
    if ((svalue != null) && (!svalue.isEmpty())) {
      try
      {
        value = Integer.parseInt(svalue);
      }
      catch (NumberFormatException nfe)
      {
        if (this.errorText.isEmpty()) {
          this.errorText = ("Failed to parse value for " + key + ".");
        }
      }
    }
    return value;
  }
  
  private int getIntProp(String key, int min, int max, boolean restrict)
  {
    int value = getIntProp(key);
    if ((max > min) && (restrict)) {
      if (restrict)
      {
        if (value > max) {
          value = max;
        }
        if (value < min) {
          value = min;
        }
      }
      else if ((value < min) || (value > max))
      {
        if (this.errorText.isEmpty()) {
          this.errorText = (key + " not in required range " + min + "-" + max + ".");
        }
        value = min;
      }
    }
    return value;
  }
  
  private byte getByteProp(String key)
  {
    byte value = 0;
    String svalue = getStringProp(key);
    if ((svalue != null) && (!svalue.isEmpty())) {
      try
      {
        value = Byte.parseByte(svalue);
      }
      catch (NumberFormatException nfe)
      {
        if (this.errorText.isEmpty()) {
          this.errorText = ("Failed to parse value for " + key + ".");
        }
      }
    }
    return value;
  }
  
  private float getFloatProp(String key, float min, float max, boolean restrict)
  {
    float value = 0.0F;
    String svalue = getStringProp(key);
    if ((svalue != null) && (!svalue.isEmpty())) {
      try
      {
        value = Float.parseFloat(svalue);
        if ((max > min) && (restrict)) {
          if (restrict)
          {
            if (value > max) {
              value = max;
            }
            if (value < min) {
              value = min;
            }
          }
          else if ((value < min) || (value > max))
          {
            if (this.errorText.isEmpty()) {
              this.errorText = (key + " not in required range " + min + "-" + max + ".");
            }
            value = min;
          }
        }
      }
      catch (NumberFormatException nfe)
      {
        if (this.errorText.isEmpty()) {
          this.errorText = ("Failed to parse value for " + key + ".");
        }
      }
    }
    return value;
  }
  
  private long getLongProp(String key)
  {
    long value = 0L;
    String svalue = getStringProp(key);
    if ((svalue != null) && (!svalue.isEmpty())) {
      try
      {
        value = Long.parseLong(svalue);
      }
      catch (NumberFormatException nfe)
      {
        if (this.errorText.isEmpty()) {
          this.errorText = ("Failed to parse value for " + key + ".");
        }
      }
    }
    return value;
  }
  
  private String getStringProp(String key, int minLength)
  {
    String value = getStringProp(key);
    if ((value == null) || (value.length() < minLength)) {
      if (this.errorText.isEmpty()) {
        this.errorText = ("Please select a name with at least " + minLength + " characters.");
      }
    }
    if (value == null) {
      return "";
    }
    return value;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\questions\MissionManager.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */