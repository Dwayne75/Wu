package com.wurmonline.server.players;

import com.wurmonline.communication.SocketConnection;
import com.wurmonline.server.Constants;
import com.wurmonline.server.FailedException;
import com.wurmonline.server.Features.Feature;
import com.wurmonline.server.HistoryManager;
import com.wurmonline.server.Items;
import com.wurmonline.server.LoginHandler;
import com.wurmonline.server.LoginServerWebConnection;
import com.wurmonline.server.Message;
import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Players;
import com.wurmonline.server.Server;
import com.wurmonline.server.ServerEntry;
import com.wurmonline.server.Servers;
import com.wurmonline.server.Team;
import com.wurmonline.server.WurmCalendar;
import com.wurmonline.server.WurmId;
import com.wurmonline.server.banks.Bank;
import com.wurmonline.server.banks.BankSlot;
import com.wurmonline.server.banks.BankUnavailableException;
import com.wurmonline.server.banks.Banks;
import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.behaviours.ActionStack;
import com.wurmonline.server.behaviours.MethodsCreatures;
import com.wurmonline.server.behaviours.MethodsReligion;
import com.wurmonline.server.behaviours.NoSuchActionException;
import com.wurmonline.server.behaviours.Seat;
import com.wurmonline.server.behaviours.Vehicle;
import com.wurmonline.server.behaviours.Vehicles;
import com.wurmonline.server.bodys.Body;
import com.wurmonline.server.bodys.Wound;
import com.wurmonline.server.bodys.Wounds;
import com.wurmonline.server.combat.ArmourTemplate;
import com.wurmonline.server.combat.Battle;
import com.wurmonline.server.creatures.CombatHandler;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.CreatureCommunicator;
import com.wurmonline.server.creatures.CreatureStatus;
import com.wurmonline.server.creatures.CreatureTemplate;
import com.wurmonline.server.creatures.CreatureTemplateFactory;
import com.wurmonline.server.creatures.Creatures;
import com.wurmonline.server.creatures.Delivery;
import com.wurmonline.server.creatures.MovementScheme;
import com.wurmonline.server.creatures.NoArmourException;
import com.wurmonline.server.creatures.NoSuchCreatureException;
import com.wurmonline.server.creatures.NoSuchCreatureTemplateException;
import com.wurmonline.server.creatures.Npc;
import com.wurmonline.server.creatures.PlayerMove;
import com.wurmonline.server.creatures.SpellEffects;
import com.wurmonline.server.creatures.SpellEffectsEnum;
import com.wurmonline.server.creatures.VisionArea;
import com.wurmonline.server.creatures.ai.ChatManager;
import com.wurmonline.server.deities.Deity;
import com.wurmonline.server.economy.Economy;
import com.wurmonline.server.economy.Shop;
import com.wurmonline.server.endgames.EndGameItem;
import com.wurmonline.server.endgames.EndGameItems;
import com.wurmonline.server.epic.Effectuator;
import com.wurmonline.server.highways.Route;
import com.wurmonline.server.intra.IntraServerConnection;
import com.wurmonline.server.intra.PlayerTransfer;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemFactory;
import com.wurmonline.server.items.ItemSpellEffects;
import com.wurmonline.server.items.ItemTemplate;
import com.wurmonline.server.items.ItemTemplateFactory;
import com.wurmonline.server.items.NoSpaceException;
import com.wurmonline.server.items.NoSuchTemplateException;
import com.wurmonline.server.items.Possessions;
import com.wurmonline.server.items.Recipe;
import com.wurmonline.server.items.Trade;
import com.wurmonline.server.items.WurmMail;
import com.wurmonline.server.kingdom.Appointment;
import com.wurmonline.server.kingdom.Appointments;
import com.wurmonline.server.kingdom.King;
import com.wurmonline.server.kingdom.Kingdom;
import com.wurmonline.server.kingdom.Kingdoms;
import com.wurmonline.server.modifiers.DoubleValueModifier;
import com.wurmonline.server.questions.ChallengeInfoQuestion;
import com.wurmonline.server.questions.ConchQuestion;
import com.wurmonline.server.questions.DropInfoQuestion;
import com.wurmonline.server.questions.Question;
import com.wurmonline.server.questions.Questions;
import com.wurmonline.server.questions.SimplePopup;
import com.wurmonline.server.questions.SpawnQuestion;
import com.wurmonline.server.skills.Affinities;
import com.wurmonline.server.skills.AffinitiesTimed;
import com.wurmonline.server.skills.Affinity;
import com.wurmonline.server.skills.NoSuchSkillException;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.skills.SkillSystem;
import com.wurmonline.server.skills.Skills;
import com.wurmonline.server.skills.SkillsFactory;
import com.wurmonline.server.sounds.Sound;
import com.wurmonline.server.spells.Cooldowns;
import com.wurmonline.server.spells.SpellEffect;
import com.wurmonline.server.statistics.ChallengePointEnum.ChallengePoint;
import com.wurmonline.server.statistics.ChallengeSummary;
import com.wurmonline.server.steam.SteamId;
import com.wurmonline.server.support.Tickets;
import com.wurmonline.server.support.VoteQuestion;
import com.wurmonline.server.support.VoteQuestions;
import com.wurmonline.server.tutorial.Mission;
import com.wurmonline.server.tutorial.MissionPerformed;
import com.wurmonline.server.tutorial.MissionPerformer;
import com.wurmonline.server.tutorial.MissionTrigger;
import com.wurmonline.server.tutorial.MissionTriggers;
import com.wurmonline.server.tutorial.OldMission;
import com.wurmonline.server.tutorial.TriggerEffect;
import com.wurmonline.server.tutorial.TriggerEffects;
import com.wurmonline.server.villages.Citizen;
import com.wurmonline.server.villages.PvPAlliance;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.villages.VillageRole;
import com.wurmonline.server.villages.Villages;
import com.wurmonline.server.webinterface.WcAddFriend;
import com.wurmonline.server.webinterface.WcGlobalPM;
import com.wurmonline.server.webinterface.WcRemoveFriendship;
import com.wurmonline.server.webinterface.WcVoting;
import com.wurmonline.server.zones.FaithZone;
import com.wurmonline.server.zones.NoSuchZoneException;
import com.wurmonline.server.zones.VirtualZone;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zone;
import com.wurmonline.server.zones.Zones;
import com.wurmonline.shared.constants.PlayerOnlineStatus;
import com.wurmonline.shared.constants.ProtoConstants;
import java.io.IOException;
import java.io.PrintStream;
import java.math.BigInteger;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;

public final class Player
  extends Creature
  implements ProtoConstants
{
  private static final Logger logger = Logger.getLogger(Player.class.getName());
  public static final long changeKingdomTime = 1209600000L;
  public static final long pvpDeathTime = 10800000L;
  public static final long sleepBonusIdleTimeout = 600000L;
  public static final long playerCombatTime = 300000L;
  public static final int minEnemyPresence = Servers.localServer.testServer ? 30 : 900;
  public static final int maxEnemyPresence = 1200;
  public static int newAffinityChance = 500;
  private final Set<Integer> kosPopups = new HashSet();
  private Map<Long, Creature> links = null;
  private Team team = null;
  private boolean mayInviteTeam = true;
  private Set<Long> phantasms = null;
  private static final PlayerInfo emptyInfo = new DbPlayerInfo("unkown");
  private PlayerInfo saveFile = emptyInfo;
  private long receivedLinkloss = 0L;
  private byte lastLinksSent = 0;
  private byte CRBonusCounter = 0;
  private byte CRBonus = 0;
  private byte farwalkerSeconds = 0;
  private int secondsToLogout = -1;
  public int secondsToLinkDeath = -1;
  public long lastSleepBonusActivity = 0L;
  public long lastActivity = 0L;
  public long startedSleepBonus = 0L;
  public int myceliumHealCounter = -1;
  private int favorGainSecondsLeft = 0;
  public Question question = null;
  private boolean fullyLoaded = false;
  private int lastMeditateX = -1;
  private int lastMeditateY = -1;
  public boolean justCombined = false;
  private long lastChatted = 0L;
  private long lastChattedLocal = 0L;
  private long lastMadeEmoteSound = 0L;
  public long startedTrading = System.currentTimeMillis();
  private int loginStep = 0;
  private boolean newPlayer = false;
  private LoginHandler loginHandler;
  public boolean loggedout = false;
  public byte lastKingdom = 0;
  private boolean legal = true;
  private boolean isTransferring = false;
  private Set<Item> itemsWatched = null;
  public float secondsPlayed = 1.0F;
  private int secondsPlayedSinceLinkloss = 1;
  private int pushCounter = 0;
  private long lastSentWarning;
  private boolean watchingBank = false;
  public Set<Spawnpoint> spawnpoints;
  private byte spnums = 0;
  public long sentClimbing = 0L;
  public long sentWind = 0L;
  public long sentMountSpeed = 0L;
  public boolean acceptsInvitations = false;
  private int maxNumActions = 2;
  public int stuckCounter = 0;
  public boolean GMINVULN = true;
  public boolean suiciding = false;
  public long lastSuicide = 0L;
  public short lastSentQuestion = 0;
  public int transferCounter = 0;
  public boolean moveWarned = false;
  public long moveWarnedTime = 0L;
  public long peakMoves = 0L;
  private short lastSentServerTime = 1;
  public long lastReferralQuestion = 0L;
  public boolean hasColoredChat = false;
  public int customRedChat = 255;
  public int customGreenChat = 140;
  public int customBlueChat = 0;
  private String affstring = null;
  private byte affcounter = 0;
  private boolean archeryMode = false;
  public boolean gotHash = false;
  public static final float minFavorLinked = 10.0F;
  private int moneySendCounter = 0;
  private boolean frozen = false;
  private boolean mayAttack = false;
  private boolean maySteal = false;
  public boolean sentChallenge = false;
  private int windowOfCreation = 0;
  private int windowOfAffinity = 0;
  public boolean isOnFire;
  public boolean hasReceivedInitialValreiData = false;
  private String disconnectReason = "You have been idle and was disconnected.";
  private double villageSkillModifier = 0.0D;
  private int enemyPresenceCounter = 0;
  private int sendSleepCounter = 0;
  private Set<Creature> sparrers = null;
  private Set<Creature> duellers = null;
  public long lastDecreasedFatigue = System.currentTimeMillis();
  private int colorr = 0;
  private int colorg = 0;
  private int colorb = 0;
  private boolean hasLoveEffect = false;
  public long lastStoppedDragging = 0L;
  private int conchticker = 0;
  private static final long playerTutorialCutoffNumber = 37862368084224L;
  private String eigcId = "";
  public boolean kickedOffBoat = false;
  private boolean hasFingerEffect = false;
  private boolean hasCrownEffect = false;
  private int crownInfluence = 0;
  private boolean markedByOrb;
  private int teleportCounter = 0;
  private int tilesMovedDragging = 0;
  private int tilesMovedRiding = 0;
  private int tilesMoved = 0;
  private int tilesMovedDriving = 0;
  private int tilesMovedPassenger = 0;
  protected static final int MINRANK = 1000;
  private String afkMessage = "Sorry but I am not available at the moment, please leave a message and I'll get back to you as soon as I can.";
  private boolean respondingAsGM = false;
  private byte nextActionRarity = 0;
  public boolean justCreated = false;
  private Set<MapAnnotation> mapAnnotations = new HashSet();
  private final Map<Integer, PlayerVote> playerQuestionVotes = new ConcurrentHashMap();
  private boolean gotVotes = false;
  private boolean canVote = false;
  private boolean askedForVotes = false;
  private int mailItemsWaiting = 0;
  private int deliveriesWaiting = 0;
  private int deliveriesFailed = 0;
  private boolean gmLight = true;
  private long sendResponseTo = -10L;
  private int waitingForFriendCount = -1;
  private String waitingForFriendName = "";
  private Friend.Category waitingForFriendCategory = Friend.Category.Other;
  private boolean askingFriend = false;
  private long taggedItemId = -10L;
  private String taggedItem = "";
  final Map<Long, Long> privateEffects = new ConcurrentHashMap();
  private byte rarityShader = 0;
  private int raritySeconds = 0;
  private Recipe viewingRecipe = null;
  private boolean isWritingRecipe = false;
  private boolean hasCookbookOpen = false;
  private int studied = 0;
  private long whenStudied = 0L;
  private long removePvPDeathTimer = 0L;
  private String clientVersion = "UNKNOWN";
  private String clientSystem = "UNKNOWN";
  
  private Player(int aId, SocketConnection serverConnection)
    throws Exception
  {
    super(CreatureTemplateFactory.getInstance().getTemplate(aId));
    if (Constants.useQueueToSendDataToPlayers) {
      this.communicator = new PlayerCommunicatorQueued(this, serverConnection);
    } else {
      this.communicator = new PlayerCommunicator(this, serverConnection);
    }
    serverConnection.setLogin(true);
    this.musicPlayer = new MusicPlayer(this);
  }
  
  private Player(int aId)
    throws Exception
  {
    super(CreatureTemplateFactory.getInstance().getTemplate(aId));
    if (Constants.useQueueToSendDataToPlayers) {
      this.communicator = new CreatureCommunicator(this);
    } else {
      this.communicator = new CreatureCommunicator(this);
    }
    this.musicPlayer = new MusicPlayer(this);
    this.justCreated = true;
  }
  
  public Player(PlayerInfo aSaveFile, SocketConnection serverConnection)
    throws Exception
  {
    if (Constants.useQueueToSendDataToPlayers) {
      this.communicator = new PlayerCommunicatorQueued(this, serverConnection);
    } else {
      this.communicator = new PlayerCommunicator(this, serverConnection);
    }
    serverConnection.setLogin(true);
    this.saveFile = aSaveFile;
    if ((this.saveFile.undeadType == 0) || (this.saveFile.currentServer == Servers.localServer.id))
    {
      aSaveFile.setLogin();
      
      setName(aSaveFile.getName());
      
      setWurmId(aSaveFile.getPlayerId(), 0.0F, 0.0F, 0.0F, 0);
      this.status.load();
      if (!Constants.useQueueToSendDataToPlayers)
      {
        setFightingStyle(aSaveFile.fightmode, true);
        
        this.status.checkStaminaEffects(65535);
      }
      this.template = this.status.getTemplate();
      getMovementScheme().initalizeModifiersWithTemplate();
      this.skills = SkillsFactory.createSkills(getWurmId());
      this.sentClimbing = 0L;
      setPersonalSeed();
      setFinestAppointment();
      this.musicPlayer = new MusicPlayer(this);
      if (getPlayingTime() == 0L) {
        this.justCreated = true;
      }
    }
  }
  
  public Player(PlayerInfo aSaveFile)
    throws Exception
  {
    this.communicator = new CreatureCommunicator(this);
    this.saveFile = aSaveFile;
    setName(aSaveFile.getName());
    
    setWurmId(aSaveFile.getPlayerId(), 0.0F, 0.0F, 0.0F, 0);
    this.status.load();
    
    this.template = this.status.getTemplate();
    getMovementScheme().initalizeModifiersWithTemplate();
    this.skills = SkillsFactory.createSkills(getWurmId());
    this.sentClimbing = 0L;
    this.musicPlayer = new MusicPlayer(this);
    this.justCreated = true;
  }
  
  public final void addGlobalEffect(Long effectId, int seconds)
  {
    this.privateEffects.put(effectId, Long.valueOf(System.currentTimeMillis() + seconds * 1000L));
  }
  
  public final boolean hasGlobalEffect(long id)
  {
    return this.privateEffects.get(Long.valueOf(id)) != null;
  }
  
  public final void addItemEffect(long id, int tilex, int tiley, float posz)
  {
    long effectId = id <= 0L ? Long.MAX_VALUE - Server.rand.nextInt(1000) : id;
    if (!hasGlobalEffect(effectId))
    {
      logger.log(Level.INFO, "Sending gloobal eff to " + getName() + " " + tilex + "," + tiley + " " + posz);
      getCommunicator().sendAddEffect(effectId, (short)4, tilex << 2, tiley << 2, posz, (byte)0);
      addGlobalEffect(Long.valueOf(effectId), 300);
    }
  }
  
  private final void pollGlobalEffects()
  {
    HashSet<Long> toRemove = new HashSet();
    for (Map.Entry<Long, Long> effect : this.privateEffects.entrySet()) {
      if (System.currentTimeMillis() > ((Long)effect.getValue()).longValue()) {
        toRemove.add(effect.getKey());
      }
    }
    for (Long eff : toRemove)
    {
      this.privateEffects.remove(eff);
      getCommunicator().sendRemoveEffect(eff.longValue());
    }
  }
  
  public void initialisePlayer(PlayerInfo aSaveFile)
  {
    if (Constants.useQueueToSendDataToPlayers)
    {
      setFightingStyle(aSaveFile.fightmode, true);
      
      this.status.checkStaminaEffects(65535);
    }
  }
  
  public boolean isPlayer()
  {
    return true;
  }
  
  public Logger getLogger()
  {
    return Players.getLogger(this);
  }
  
  public boolean isLogged()
  {
    return this.saveFile.logging;
  }
  
  public boolean hasColoredChat()
  {
    return this.hasColoredChat;
  }
  
  public int getCustomGreenChat()
  {
    return this.customGreenChat;
  }
  
  public int getCustomRedChat()
  {
    return this.customRedChat;
  }
  
  public int getCustomBlueChat()
  {
    return this.customBlueChat;
  }
  
  public void checkBodyInventoryConsistency()
    throws Exception
  {
    if (this.status.getBodyId() == -10L) {
      this.status.createNewBody();
    }
    if (this.status.getInventoryId() == -10L) {
      this.status.createNewPossessions();
    }
  }
  
  public void setLoginHandler(@Nullable LoginHandler handler)
  {
    this.loginHandler = handler;
  }
  
  public boolean mayHearDevTalk()
  {
    return (getPower() >= 2) || (this.saveFile.mayHearDevTalk);
  }
  
  public boolean mayHearMgmtTalk()
  {
    return (getPower() >= 1) || (this.saveFile.mayMute) || (this.saveFile.mayHearDevTalk) || (this.saveFile.playerAssistant);
  }
  
  public LoginHandler getLoginhandler()
  {
    return this.loginHandler;
  }
  
  public void setFullyLoaded()
  {
    this.fullyLoaded = true;
  }
  
  public boolean isFullyLoaded()
  {
    return this.fullyLoaded;
  }
  
  public byte getAttitude(Creature aTarget)
  {
    if (getPower() > 0)
    {
      if (getPower() >= 5) {
        return 6;
      }
      return 3;
    }
    if (this.opponent == aTarget) {
      return 2;
    }
    if (getSaveFile().pet != -10L) {
      if (aTarget.getWurmId() == getSaveFile().pet) {
        return 1;
      }
    }
    if ((aTarget.getDominator() != null) && (aTarget.getDominator() != this)) {
      return getAttitude(aTarget.getDominator());
    }
    if ((aTarget.isReborn()) && (getKingdomTemplateId() == 3)) {
      return 0;
    }
    if ((aTarget.getKingdomId() != 0) && (!isFriendlyKingdom(aTarget.getKingdomId()))) {
      return 2;
    }
    if ((aTarget.hasAttackedUnmotivated()) && (
      (aTarget.isPlayer()) || (!aTarget.isDominated()) || (aTarget.getDominator() != this))) {
      return 2;
    }
    if (this.citizenVillage != null)
    {
      if (aTarget.citizenVillage == this.citizenVillage) {
        return 1;
      }
      if (this.citizenVillage.isAlly(aTarget)) {
        return 1;
      }
      if (this.citizenVillage.isEnemy(aTarget.citizenVillage)) {
        return 2;
      }
      if (this.citizenVillage.isEnemy(aTarget)) {
        return 2;
      }
      if (aTarget.getCitizenVillage() != null) {
        if (aTarget.getCitizenVillage().isEnemy(this)) {
          return 2;
        }
      }
      if (this.citizenVillage.getReputation(aTarget) <= -30) {
        return 0;
      }
    }
    if ((getKingdomId() != 3) && (aTarget.getReputation() < 0)) {
      return 2;
    }
    if (aTarget.isAggHuman())
    {
      if ((aTarget.getKingdomId() != 0) && (isFriendlyKingdom(aTarget.getKingdomId()))) {
        if (aTarget.isDominated()) {
          return 0;
        }
      }
      return 2;
    }
    if ((aTarget.isPlayer()) && (isFriend(aTarget.getWurmId()))) {
      return 7;
    }
    return 0;
  }
  
  public void sendReligion()
  {
    getCommunicator().sendAddSkill(2147483644, 2147483643, "Favor", this.saveFile.getFavor(), this.saveFile.getFavor(), 0);
    
    getCommunicator().sendAddSkill(2147483645, 2147483643, "Faith", this.saveFile.getFaith(), this.saveFile.getFaith(), 0);
    
    getCommunicator().sendAddSkill(2147483642, 2147483643, "Alignment", this.saveFile.getAlignment(), this.saveFile
      .getAlignment(), 0);
  }
  
  public float getBaseCombatRating()
  {
    return this.template.baseCombatRating;
  }
  
  public void calculateZoneBonus(int tilex, int tiley, boolean surfaced)
  {
    try
    {
      if (Servers.localServer.HOMESERVER)
      {
        if (this.currentKingdom == 0)
        {
          this.currentKingdom = Servers.localServer.KINGDOM;
          getCommunicator().sendNormalServerMessage("You enter " + Kingdoms.getNameFor(this.currentKingdom) + ".");
        }
      }
      else {
        setCurrentKingdom(getCurrentKingdom());
      }
      float initial = this.zoneBonus;
      this.zoneBonus = 0.0F;
      Deity deity = getDeity();
      if (deity != null)
      {
        if ((isChampion()) && (getCurrentKingdom() != getKingdomId())) {
          this.zoneBonus = 50.0F;
        }
        if (!isChampion())
        {
          FaithZone z = Zones.getFaithZone(tilex, tiley, surfaced);
          if (z != null)
          {
            if (z.getCurrentRuler() == deity)
            {
              this.zoneBonus += 5.0F;
              if (getFaith() > 30.0F) {
                this.zoneBonus += 10.0F;
              }
              if (getFaith() > 90.0F) {
                this.zoneBonus += getFaith() - 90.0F;
              }
              if (Features.Feature.NEWDOMAINS.isEnabled()) {
                this.zoneBonus += z.getStrengthForTile(tilex, tiley, surfaced) / 2.0F;
              } else {
                this.zoneBonus += z.getStrength() / 2.0F;
              }
            }
            else if ((Features.Feature.NEWDOMAINS.isEnabled() ? z.getStrengthForTile(tilex, tiley, surfaced) : z.getStrength()) == 0)
            {
              if (getFaith() >= 90.0F) {
                this.zoneBonus = (5.0F + getFaith() - 90.0F);
              }
            }
          }
          else if (getFaith() >= 90.0F) {
            this.zoneBonus = (5.0F + getFaith() - 90.0F);
          }
        }
        if (initial != this.zoneBonus) {
          if (this.zoneBonus == 0.0F) {
            getCommunicator().sendRemoveSpellEffect(SpellEffectsEnum.FAITHBONUS);
          } else {
            getCommunicator().sendAddSpellEffect(SpellEffectsEnum.FAITHBONUS, 100000, this.zoneBonus);
          }
        }
      }
    }
    catch (NoSuchZoneException nsz)
    {
      logger.log(Level.WARNING, "No faith zone at " + tilex + "," + tiley + ", surf=" + surfaced);
    }
  }
  
  public final void sendKarma()
  {
    getCommunicator().sendAddSpellEffect(SpellEffectsEnum.KARMA, 100000, this.saveFile.karma);
  }
  
  public final void sendScenarioKarma()
  {
    if (Servers.localServer.EPIC) {
      getCommunicator().sendAddSpellEffect(SpellEffectsEnum.SCENARIOKARMA, 100000, this.saveFile
        .getScenarioKarma());
    }
  }
  
  public void setSpam(boolean spam)
  {
    this.saveFile.setSpamMode(spam);
    if (this.saveFile.spamMode()) {
      getCommunicator().sendNormalServerMessage("You are now in spam mode.");
    } else {
      getCommunicator().sendNormalServerMessage("You are now in nospam mode.");
    }
  }
  
  public boolean spamMode()
  {
    return this.saveFile.spamMode();
  }
  
  public void setQuestion(@Nullable Question aQuestion)
  {
    if (this.question != null) {
      Questions.removeQuestion(this.question);
    }
    this.question = aQuestion;
  }
  
  public Question getCurrentQuestion()
  {
    return this.question;
  }
  
  public void setViewingRecipe(Recipe recipe)
  {
    this.viewingRecipe = recipe;
  }
  
  public Recipe getViewingRecipe()
  {
    return this.viewingRecipe;
  }
  
  public void setIsWritingRecipe(boolean isWriting)
  {
    this.isWritingRecipe = isWriting;
  }
  
  public boolean isWritingRecipe()
  {
    return this.isWritingRecipe;
  }
  
  public void setStudied(int studied)
  {
    this.studied = studied;
    if (studied > 0) {
      this.whenStudied = WurmCalendar.getCurrentTime();
    } else {
      this.whenStudied = 0L;
    }
  }
  
  public int getStudied()
  {
    return this.studied;
  }
  
  public void setIsViewingCookbook()
  {
    this.hasCookbookOpen = true;
  }
  
  public boolean isViewingCookbook()
  {
    return this.hasCookbookOpen;
  }
  
  public List<Route> getHighwayPath()
  {
    return this.saveFile.getHighwayPath();
  }
  
  public void setHighwayPath(String newDestination, List<Route> newPath)
  {
    this.saveFile.setHighwayPath(newDestination, newPath);
  }
  
  public String getHighwayPathDestination()
  {
    return this.saveFile.getHighwayPathDestination();
  }
  
  public void setSaveFile(PlayerInfo aSaveFile)
    throws Exception
  {
    this.saveFile = aSaveFile;
    setName(aSaveFile.getName());
    if (!this.guest) {
      aSaveFile.save();
    }
    setPersonalSeed();
    setFightingStyle(aSaveFile.fightmode, true);
  }
  
  private void setPersonalSeed()
  {
    Random personalRandom = new Random(this.name.hashCode());
    if (getPower() > 0)
    {
      this.colorr = (200 + personalRandom.nextInt(50));
      this.colorg = (200 + personalRandom.nextInt(50));
      this.colorb = (200 + personalRandom.nextInt(50));
    }
    else if ((getKingdomId() > 4) || (getKingdomId() < 0))
    {
      Kingdom k = Kingdoms.getKingdomOrNull(getKingdomId());
      if (k != null)
      {
        this.colorr = k.getColorRed();
        this.colorg = k.getColorGreen();
        this.colorb = k.getColorBlue();
      }
    }
    else
    {
      this.colorr = (127 + personalRandom.nextInt(100));
      this.colorg = (127 + personalRandom.nextInt(100));
      this.colorb = (127 + personalRandom.nextInt(100));
    }
  }
  
  public byte getColorRed()
  {
    if (getPower() < 2) {
      return (byte)this.template.colorRed;
    }
    return (byte)this.colorr;
  }
  
  public byte getColorGreen()
  {
    if (getPower() < 2) {
      return (byte)this.template.colorRed;
    }
    return (byte)this.colorg;
  }
  
  public byte getColorBlue()
  {
    if (getPower() < 2) {
      return (byte)this.template.colorRed;
    }
    return (byte)this.colorb;
  }
  
  public boolean isDead()
  {
    return this.saveFile.dead;
  }
  
  private void setDead(boolean dead)
  {
    this.saveFile.setDead(dead);
  }
  
  public boolean isLegal()
  {
    return this.legal;
  }
  
  public void setLegal(boolean mode)
  {
    if (!Servers.localServer.PVPSERVER)
    {
      this.legal = true;
      getCommunicator().sendNormalServerMessage("You will always stay within legal limits in these lands.");
    }
    else if ((getKingdomTemplateId() != 3) && (
      ((getCitizenVillage() != null) && (getCitizenVillage().getMayor().wurmId == getWurmId())) || (isKing())))
    {
      this.legal = true;
      if (isKing()) {
        getCommunicator().sendNormalServerMessage("As the ruler of " + 
          Kingdoms.getNameFor(getKingdomId()) + " you may not risk joining the Horde of the Summoned by performing illegal actions.");
      } else {
        getCommunicator().sendNormalServerMessage("As the ruler of a settlement you may not risk joining the Horde of the Summoned by performing illegal actions.");
      }
    }
    else
    {
      this.legal = mode;
      if (this.legal) {
        getCommunicator().sendNormalServerMessage("You will now stay within legal limits.");
      } else {
        getCommunicator().sendNormalServerMessage("You will no longer care about local laws.");
      }
    }
    getCommunicator().sendToggle(2, this.legal);
  }
  
  public void setAutofight(boolean mode)
  {
    this.saveFile.setAutofight(mode);
    if (mode)
    {
      getCommunicator().sendNormalServerMessage("You will now select stance and special moves automatically in combat.");
      getCommunicator().sendNormalServerMessage("You may always do any available moves manually anyway.");
      getCommunicator().sendNormalServerMessage("You will still have to select normal, aggressive or defensive stance.");
      getCommunicator().sendNormalServerMessage("You will also have to shield bash, taunt or throw items manually.");
      getCommunicator().sendSpecialMove((short)-1, "N/A");
      getCommunicator().sendCombatOptions(CombatHandler.NO_COMBAT_OPTIONS, (short)-1);
    }
    else
    {
      getCommunicator().sendNormalServerMessage("You will now have to make manual stance decisions in combat.");
      if (isFighting())
      {
        getCombatHandler().setSentAttacks(false);
        getCombatHandler().calcAttacks(false);
      }
    }
    getCommunicator().sendToggle(4, mode);
  }
  
  public boolean isAutofight()
  {
    return this.saveFile.autoFighting;
  }
  
  public boolean isArcheryMode()
  {
    return this.archeryMode;
  }
  
  public void setArcheryMode(boolean mode)
  {
    this.archeryMode = mode;
    if (mode)
    {
      getCommunicator().sendNormalServerMessage("You will now throw items if you double-click an enemy.");
      getCommunicator().sendNormalServerMessage("If you wield a bow you will try to shoot instead.");
    }
    else
    {
      getCommunicator().sendNormalServerMessage("You will no longer use ranged attacks while double-clicking.");
    }
    getCommunicator().sendToggle(100, mode);
  }
  
  public void setFaithMode(boolean mode)
  {
    this.faithful = mode;
    if (this.faithful) {
      getCommunicator().sendNormalServerMessage("You will try to obey your gods wishes accordingly.");
    } else {
      getCommunicator().sendNormalServerMessage("You may now go against the will of your god.");
    }
    getCommunicator().sendToggle(1, this.faithful);
  }
  
  public void setClimbing(boolean climbing)
    throws IOException
  {
    if ((this.saveFile.climbing) && 
      (!climbing)) {
      if (this.secondsPlayed > 120.0F) {
        this.sentClimbing = System.currentTimeMillis();
      }
    }
    this.saveFile.setClimbing(climbing);
    if (climbing) {
      getCommunicator().sendNormalServerMessage("You will now attempt to climb steep areas.");
    } else {
      getCommunicator().sendNormalServerMessage("You will no longer climb.");
    }
    getCommunicator().sendClimb(climbing);
    getCommunicator().sendToggle(0, climbing);
    
    this.staminaPollCounter = 2;
  }
  
  public boolean mayChangeDeity(int targetDeity)
  {
    return this.saveFile.mayChangeDeity(targetDeity);
  }
  
  public boolean mayChangeKingdom(Creature converter)
  {
    boolean isPlayerConversion = converter != null;
    boolean convertingToCustom = (isPlayerConversion) && (converter.isOfCustomKingdom());
    if ((Servers.localServer.challengeServer) && (getPower() <= 0)) {
      return false;
    }
    if ((getCitizenVillage() != null) && (getCitizenVillage().getMayor() != null)) {
      if (getCitizenVillage().getMayor().wurmId == getWurmId())
      {
        if (!convertingToCustom)
        {
          if (isPlayerConversion) {
            converter.getCommunicator().sendNormalServerMessage("You cannot convert the mayor and their deed to a template kingdom!");
          }
          return false;
        }
        if (getCitizenVillage().isCapital())
        {
          if (isPlayerConversion) {
            converter.getCommunicator().sendNormalServerMessage("You cannot convert the mayor and their deed, because their deed is the capital of their kingdom.");
          }
          return false;
        }
        int mindist = Kingdoms.minKingdomDist;
        Village village = getCitizenVillage();
        int startX = village.getStartX() - 5 - village.getPerimeterSize() - mindist;
        int startY = village.getStartY() - 5 - village.getPerimeterSize() - mindist;
        int endX = village.getEndX() + 5 + village.getPerimeterSize() + mindist;
        int endY = village.getEndY() + 5 + village.getPerimeterSize() + mindist;
        
        int startExclusionX = village.getStartX() - 5 - village.getPerimeterSize() - mindist / 2;
        int startExclusionY = village.getStartY() - 5 - village.getPerimeterSize() - mindist / 2;
        int endExclusionX = village.getEndX() + 5 + village.getPerimeterSize() + mindist / 2;
        int endExclusionY = village.getEndY() + 5 + village.getPerimeterSize() + mindist / 2;
        
        Set<Village> nearbyVillages = Villages.getVillagesWithin(startX, startY, endX, endY);
        for (Village v : nearbyVillages) {
          if (v.getId() != getVillageId()) {
            if (v.kingdom == getKingdomId())
            {
              if (isPlayerConversion)
              {
                converter.getCommunicator().sendNormalServerMessage("You cannot convert the mayor and their deed, because there are deeds nearby of their own kingdom.");
                converter.getCommunicator().sendNormalServerMessage("If they were to convert, their deed would be very close to other deeds of their old kingdom.");
                if (getPower() >= 2) {
                  converter.getCommunicator().sendNormalServerMessage("The nearest deed is " + v.getName() + " which is located at (" + v.getTokenX() + ", " + v.getTokenY() + ")");
                }
              }
              return false;
            }
          }
        }
        if (!Zones.isKingdomBlocking(startX, startY, endX, endY, (byte)0, startExclusionX, startExclusionY, endExclusionX, endExclusionY))
        {
          if (isPlayerConversion) {
            converter.getCommunicator().sendNormalServerMessage("You cannot convert the mayor and their deed, because there cannot be any kingdom influence nearby.");
          }
          return false;
        }
      }
    }
    if (isKing())
    {
      if (isPlayerConversion) {
        converter.getCommunicator().sendNormalServerMessage("You cannot convert the king of another kingdom!");
      }
      return false;
    }
    if (convertingToCustom) {
      if (isChampion())
      {
        if (isPlayerConversion) {
          converter.getCommunicator().sendNormalServerMessage("You cannot convert a champion to your kingdom.");
        }
        return false;
      }
    }
    if (getPower() <= 0)
    {
      boolean canConvert = System.currentTimeMillis() - this.saveFile.lastChangedKindom > getChangeKingdomLimit();
      if (!canConvert)
      {
        if (isPlayerConversion) {
          converter.getCommunicator().sendNormalServerMessage(getName() + " has converted too recently.");
        }
        return false;
      }
    }
    return true;
  }
  
  public final long getChangeKingdomLimit()
  {
    if (getKingdomTemplateId() == 3) {
      return 2419200000L;
    }
    return 1209600000L;
  }
  
  public void increaseChangedKingdom(boolean setTimeStamp)
    throws IOException
  {
    this.saveFile.setChangedKingdom((byte)(this.saveFile.getChangedKingdom() + 1), setTimeStamp);
  }
  
  public void setChangedDeity()
    throws IOException
  {
    this.saveFile.setChangedDeity();
    
    achievement(556);
  }
  
  public PlayerInfo getSaveFile()
  {
    return this.saveFile;
  }
  
  public boolean isReimbursed()
  {
    return this.saveFile.isReimbursed();
  }
  
  public long getPlayingTime()
  {
    if (this.saveFile.lastLogin > 0L) {
      return this.saveFile.playingTime + System.currentTimeMillis() - this.saveFile.lastLogin;
    }
    return this.saveFile.playingTime;
  }
  
  public boolean mayLeadMoreCreatures()
  {
    return (this.followers == null) || (this.followers.size() < 4) || ((getPower() >= 2) && (this.followers.size() < 10));
  }
  
  public void dropLeadingItem(Item item)
  {
    Iterator<Creature> it;
    if ((this.followers != null) && (!this.followers.isEmpty()))
    {
      Set<Creature> toRemove = new HashSet();
      for (Iterator<Map.Entry<Creature, Item>> it = this.followers.entrySet().iterator(); it.hasNext();)
      {
        Map.Entry<Creature, Item> entry = (Map.Entry)it.next();
        Item titem = (Item)entry.getValue();
        if ((titem != null) && (titem.equals(item))) {
          toRemove.add(entry.getKey());
        }
      }
      if (toRemove.size() > 0) {
        for (it = toRemove.iterator(); it.hasNext();)
        {
          Creature creature = (Creature)it.next();
          this.followers.remove(creature);
          creature.setLeader(null);
        }
      }
    }
  }
  
  public boolean isItemLeading(Item item)
  {
    Iterator<Item> it;
    if (this.followers != null) {
      for (it = this.followers.values().iterator(); it.hasNext();)
      {
        Item litem = (Item)it.next();
        if ((litem != null) && (litem.equals(item))) {
          return true;
        }
      }
    }
    return false;
  }
  
  public Item getLeadingItem(Creature follower)
  {
    Iterator<Map.Entry<Creature, Item>> it;
    if (this.followers != null) {
      for (it = this.followers.entrySet().iterator(); it.hasNext();)
      {
        Map.Entry<Creature, Item> entry = (Map.Entry)it.next();
        Creature creature = (Creature)entry.getKey();
        if ((follower != null) && (follower.equals(creature))) {
          return (Item)entry.getValue();
        }
      }
    }
    return null;
  }
  
  public Creature getFollowedCreature(Item leadingItem)
  {
    Iterator<Map.Entry<Creature, Item>> it;
    if (this.followers != null) {
      for (it = this.followers.entrySet().iterator(); it.hasNext();)
      {
        Map.Entry<Creature, Item> entry = (Map.Entry)it.next();
        Item titem = (Item)entry.getValue();
        if ((titem != null) && (titem.equals(leadingItem))) {
          return (Creature)entry.getKey();
        }
      }
    }
    return null;
  }
  
  public void addFollower(Creature follower, Item leadingItem)
  {
    if (this.followers == null) {
      this.followers = new HashMap();
    }
    this.followers.put(follower, leadingItem);
  }
  
  public boolean addItemWatched(Item watched)
  {
    if (this.itemsWatched == null) {
      this.itemsWatched = new HashSet();
    }
    if (!this.itemsWatched.contains(watched))
    {
      this.itemsWatched.add(watched);
      return true;
    }
    return false;
  }
  
  public boolean isItemWatched(Item watched)
  {
    return true;
  }
  
  public boolean removeItemWatched(Item watched)
  {
    if (this.itemsWatched != null) {
      if (this.itemsWatched.contains(watched))
      {
        this.itemsWatched.remove(watched);
        return true;
      }
    }
    return false;
  }
  
  private Item[] getItemsWatched()
  {
    if (this.itemsWatched == null) {
      return new Item[0];
    }
    return (Item[])this.itemsWatched.toArray(new Item[this.itemsWatched.size()]);
  }
  
  private void checkItemsWatched()
  {
    if ((this.itemsWatched != null) && (this.itemsWatched.size() > 0))
    {
      Item[] itemArr = getItemsWatched();
      for (int x = 0; x < itemArr.length; x++) {
        if (!hasLink())
        {
          itemArr[x].removeWatcher(this, false);
          removeItemWatched(itemArr[x]);
        }
        else
        {
          Item checkItem = itemArr[x];
          if ((checkItem.getTemplateId() == 1342) && (checkItem.getData() != -1L)) {
            try
            {
              checkItem = Items.getItem(checkItem.getData());
            }
            catch (NoSuchItemException e)
            {
              logger.log(Level.WARNING, e.getMessage(), e);
            }
          }
          if (checkItem.getWurmId() != getVehicle()) {
            if (!isWithinDistanceTo(checkItem.getPosX(), checkItem.getPosY(), checkItem.getPosZ(), 
              (checkItem.isVehicle()) && (!checkItem.isTent()) ? Math.max(6, checkItem.getSizeZ() / 100) : 6.0F))
            {
              if (getPower() > 0) {
                logger.log(Level.INFO, "Stopping watching " + itemArr[x].getName() + " because not within distance to " + itemArr[x]
                  .getPosX() / 4.0F + ", " + itemArr[x].getPosY() / 4.0F);
              }
              getCommunicator().sendCloseInventoryWindow(itemArr[x].getWurmId());
              itemArr[x].removeWatcher(this, false);
              removeItemWatched(itemArr[x]);
              if ((itemArr[x].getTemplateId() == 1342) && (itemArr[x].getData() != -1L)) {
                getCommunicator().sendUpdateSelectBar(checkItem.getWurmId(), false);
              }
            }
          }
        }
      }
    }
    if (this.watchingBank) {
      if (this.currentVillage != null) {
        try
        {
          Item token = this.currentVillage.getToken();
          if (!isWithinDistanceTo(token.getPosX(), token.getPosY(), token.getPosZ(), 12.0F)) {
            closeBank();
          }
        }
        catch (NoSuchItemException nsi)
        {
          closeBank();
        }
      } else {
        closeBank();
      }
    }
  }
  
  public void openBank()
  {
    try
    {
      Bank bank = Banks.getBank(getWurmId());
      if (bank != null)
      {
        bank.open();
        BankSlot[] slots = bank.slots;
        if (slots != null)
        {
          String lName = bank.getCurrentVillage().getName();
          getCommunicator().sendOpenInventoryWindow(bank.id, "Bank of " + lName);
          for (int x = 0; x < slots.length; x++) {
            if (slots[x] != null) {
              slots[x].item.addWatcher(bank.id, this);
            }
          }
          this.watchingBank = true;
        }
      }
      else
      {
        getCommunicator().sendNormalServerMessage("You have no bank account.");
      }
    }
    catch (BankUnavailableException bux)
    {
      getCommunicator().sendNormalServerMessage(bux.getMessage());
    }
  }
  
  public boolean isNewTutorial()
  {
    return getWurmId() > 37862368084224L;
  }
  
  public boolean startBank(Village village)
  {
    if (Banks.startBank(getWurmId(), 5, village.getId()))
    {
      getCommunicator().sendNormalServerMessage("You open a bank account here. Congratulations!");
      return true;
    }
    Bank bank = Banks.getBank(getWurmId());
    if (bank != null) {
      try
      {
        Village vill = bank.getCurrentVillage();
        if (vill != null) {
          getCommunicator().sendNormalServerMessage("You already have a bank account in " + vill.getName() + ".");
        } else {
          getCommunicator().sendNormalServerMessage("You already have a bank account but it is unavailable. Talk to the administrators.");
        }
      }
      catch (BankUnavailableException bua)
      {
        getCommunicator().sendNormalServerMessage("You already have a bank account but need to transfer it to a village.");
      }
    }
    return false;
  }
  
  public void closeBank()
  {
    if (this.watchingBank)
    {
      this.watchingBank = false;
      Bank bank = Banks.getBank(getWurmId());
      if (bank != null)
      {
        bank.open = false;
        
        BankSlot[] slots = bank.slots;
        if (slots != null) {
          for (int x = 0; x < slots.length; x++) {
            if (slots[x] != null) {
              slots[x].item.removeWatcher(this, false);
            }
          }
        }
        getCommunicator().sendCloseInventoryWindow(bank.id);
      }
    }
  }
  
  public void trainSkill(String sname)
    throws Exception
  {}
  
  public void savePosition(int zoneid)
    throws IOException
  {
    this.status.savePosition(getWurmId(), true, zoneid, false);
  }
  
  public void save()
    throws IOException
  {
    if (this.fullyLoaded)
    {
      this.saveFile.save();
      this.status.save();
      this.status.savePosition(getWurmId(), true, this.status.getZoneId(), true);
      this.possessions.save();
      this.skills.save();
    }
  }
  
  public void sendToWorld()
  {
    try
    {
      Zones.getZone(getTileX(), getTileY(), isOnSurface()).addCreature(getWurmId());
    }
    catch (NoSuchCreatureException localNoSuchCreatureException) {}catch (NoSuchZoneException localNoSuchZoneException) {}catch (NoSuchPlayerException localNoSuchPlayerException) {}
  }
  
  public void setLogout()
  {
    if (isSignedIn()) {
      getCommunicator().signOut("");
    }
    if (this.question != null) {
      this.question.timedOut();
    }
    this.saveFile.logout();
  }
  
  public boolean isTransferring()
  {
    return this.isTransferring;
  }
  
  public void setIsTransferring(boolean _isTransferring)
  {
    this.isTransferring = _isTransferring;
  }
  
  public boolean isOnCurrentServer()
  {
    return Servers.localServer.id == getSaveFile().currentServer;
  }
  
  public void sleep()
    throws Exception
  {
    if (!this.guest)
    {
      if (getStatus() != null) {
        getStatus().savePosition(getWurmId(), true, getStatus().getZoneId(), true);
      }
      if (this.fullyLoaded)
      {
        try
        {
          setLogout();
          this.saveFile.save();
          this.status.save();
          if (!this.saveFile.hasMovedInventory()) {
            this.possessions.sleep(Servers.localServer.isChallengeOrEpicServer());
          }
          getBody().sleep(this, Servers.localServer.EPIC);
          this.skills.save();
        }
        catch (Exception ex)
        {
          logger.log(Level.WARNING, "Error when sleeping player id " + getWurmId() + " : " + ex.getMessage(), ex);
        }
      }
      else
      {
        try
        {
          if (this.possessions != null) {
            if (this.possessions.getInventory() != null)
            {
              Item[] items = this.possessions.getInventory().getAllItems(true);
              for (int x = 0; x < items.length; x++) {
                Items.removeItem(items[x].getWurmId());
              }
            }
          }
        }
        catch (Exception e)
        {
          logger.log(Level.INFO, "Error when removing inventory items while sleeping player id " + getWurmId() + " : " + e
            .getMessage(), e);
        }
        try
        {
          if (getBody() != null)
          {
            Item[] items = getBody().getAllItems();
            for (int x = 0; x < items.length; x++) {
              Items.removeItem(items[x].getWurmId());
            }
          }
        }
        catch (Exception e)
        {
          logger.log(Level.INFO, "Error when removing body items while sleeping player id " + 
            getWurmId() + " : " + e.getMessage(), e);
        }
      }
    }
    else
    {
      try
      {
        this.skills.delete();
      }
      catch (Exception ex)
      {
        logger.log(Level.INFO, "Error when deleting guest skills: " + ex.getMessage(), ex);
      }
      try
      {
        Item[] items = this.possessions.getInventory().getAllItems(true);
        for (int x = 0; x < items.length; x++) {
          if ((items[x].isUnique()) && (!items[x].isRoyal())) {
            dropItem(items[x]);
          } else {
            Items.decay(items[x].getWurmId(), items[x].getDbStrings());
          }
        }
      }
      catch (Exception e)
      {
        logger.log(Level.INFO, "Error when decaying guest items: " + e.getMessage(), e);
      }
      try
      {
        Item[] items = getBody().getAllItems();
        for (int x = 0; x < items.length; x++) {
          if ((items[x].isUnique()) && (!items[x].isRoyal())) {
            dropItem(items[x]);
          } else {
            Items.decay(items[x].getWurmId(), items[x].getDbStrings());
          }
        }
      }
      catch (Exception e)
      {
        logger.log(Level.INFO, "Error when decaying guest items: " + e.getMessage(), e);
      }
    }
    ItemBonus.clearBonuses(getWurmId());
  }
  
  public void stopLeading()
  {
    if (this.followers != null)
    {
      Creature[] folls = (Creature[])this.followers.keySet().toArray(new Creature[this.followers.size()]);
      for (int x = 0; x < folls.length; x++) {
        folls[x].setLeader(null);
      }
      this.followers.clear();
      this.followers = null;
    }
  }
  
  public boolean isLoggedOut()
  {
    return this.loggedout;
  }
  
  public void logout()
  {
    if (!this.loggedout) {
      try
      {
        stopLeading();
        Vehicle lVehicle = Vehicles.getVehicleForId(getVehicle());
        if (lVehicle != null) {
          if (lVehicle.isCreature()) {
            disembark(false);
          } else {
            try
            {
              Item item = Items.getItem(getVehicle());
              if ((!item.isBoat()) || (isChampion())) {
                disembark(false);
              }
            }
            catch (NoSuchItemException localNoSuchItemException) {}
          }
        }
        clearLinks();
        disableLink();
        if (this.battle != null) {
          this.battle.removeCreature(this);
        }
        trimAttackers(true);
        sleep();
        if (this.possessions != null) {
          this.possessions.clearOwner();
        }
        destroyVisionArea();
        if (this.movementScheme.getDraggedItem() != null) {
          Items.stopDragging(this.movementScheme.getDraggedItem());
        }
        this.loggedout = true;
        this.actions.clear();
        this.communicator.resetTicker();
        this.communicator.player = null;
        
        this.communicator.resetConnection();
        if (getSpellEffects() != null) {
          getSpellEffects().sleep();
        }
        this.communicator = new CreatureCommunicator(this);
        Questions.removeQuestions(this);
        this.question = null;
        checkItemsWatched();
        if ((getPet() != null) && (getPet().isAnimal()) && (!getPet().isReborn()) && (!getPet().isStayonline())) {
          getPet().goOffline = true;
        }
        getSaveFile().lastLogin = 0L;
        Iterator<Creature> it;
        if (this.sparrers != null) {
          for (it = this.sparrers.iterator(); it.hasNext();) {
            ((Player)it.next()).removeSparrer(this);
          }
        }
        this.sparrers = null;
        Iterator<Creature> it;
        if (this.duellers != null) {
          for (it = this.duellers.iterator(); it.hasNext();) {
            ((Player)it.next()).removeDuellist(this);
          }
        }
        getStatus().savePosition(getWurmId(), true, getStatus().getZoneId(), true);
        setTeam(null, false);
        this.duellers = null;
        logger.log(Level.INFO, "Logout complete for " + this);
        if (isUndead()) {
          IntraServerConnection.deletePlayer(getWurmId());
        }
      }
      catch (Exception ex)
      {
        logger.log(Level.WARNING, "Problem logging out player ID " + 
          getWurmId() + ", name: " + getName() + " : " + ex.getMessage(), ex);
      }
    }
  }
  
  public void logoutIn(int seconds, String reason)
  {
    if (hasLink())
    {
      this.disconnectReason = reason;
      if (this.secondsToLinkDeath < 0)
      {
        logger.log(Level.INFO, "Setting " + getName() + " to log off in " + seconds + " " + reason);
        this.secondsToLinkDeath = seconds;
        this.secondsToLogout = (seconds + 1);
        
        this.communicator.setReady(false);
      }
    }
  }
  
  public void endTrade() {}
  
  public void createSomeItems(float modifier, boolean reimburse)
  {
    if (!isUndead()) {
      try
      {
        if (Servers.localServer.testServer)
        {
          Item inventory = getInventory();
          
          float ql = 20.0F;
          Item c = createItem(274, 20.0F);
          inventory.insertItem(c);
          c.setAuxData((byte)1);
          c = createItem(274, 20.0F);
          inventory.insertItem(c);
          c.setAuxData((byte)1);
          c = createItem(279, 20.0F);
          inventory.insertItem(c);
          c.setAuxData((byte)1);
          c = createItem(277, 20.0F);
          inventory.insertItem(c);
          c.setAuxData((byte)1);
          c = createItem(277, 20.0F);
          inventory.insertItem(c);
          c.setAuxData((byte)1);
          
          c = createItem(278, 20.0F);
          inventory.insertItem(c);
          c.setAuxData((byte)1);
          c = createItem(278, 20.0F);
          inventory.insertItem(c);
          c.setAuxData((byte)1);
          c = createItem(275, 20.0F);
          inventory.insertItem(c);
          c.setAuxData((byte)1);
          c = createItem(276, 20.0F);
          inventory.insertItem(c);
          c = createItem(4, 20.0F);
          inventory.insertItem(c);
          int x = Server.rand.nextInt(3);
          if (x == 0) {
            c = createItem(87, 50.0F);
          } else if (x == 1) {
            c = createItem(21, 50.0F);
          } else if (x == 2) {
            c = createItem(290, 50.0F);
          }
          c.setAuxData((byte)1);
          inventory.insertItem(c);
          if (Server.rand.nextInt(20) == 0) {
            c.enchant((byte)(1 + Server.rand.nextInt(12)));
          }
          if (Server.rand.nextInt(20) == 0)
          {
            ItemSpellEffects effs = new ItemSpellEffects(c.getWurmId());
            
            SpellEffect eff = new SpellEffect(c.getWurmId(), (byte)(13 + Server.rand.nextInt(7)), Server.rand.nextInt(90), 20000000);
            effs.addSpellEffect(eff);
          }
          c = createItem(447, 20.0F);
          inventory.insertItem(c);
          if (getDeity() != null)
          {
            int statnumber = 0;
            switch (getDeity().number)
            {
            case 1: 
              statnumber = 505;
              break;
            case 4: 
              statnumber = 506;
              break;
            case 3: 
              statnumber = 508;
              break;
            case 2: 
              statnumber = 507;
            }
            if (statnumber != 0)
            {
              c = createItem(statnumber, 80.0F);
              c.setMaterial((byte)7);
              inventory.insertItem(c);
            }
          }
          Item q = createItem(462, 20.0F);
          inventory.insertItem(q);
          for (int a = 0; a < 20; a++)
          {
            c = createItem(455, 20.0F);
            q.insertItem(c, true);
          }
          q = createItem(462, 20.0F);
          inventory.insertItem(q);
          for (int a = 0; a < 20; a++)
          {
            c = createItem(456, 20.0F);
            q.insertItem(c, true);
          }
          for (int a = 0; a < 10; a++)
          {
            c = createItem(457, 20.0F);
            inventory.insertItem(c, true);
          }
          c = createItem(516, 20.0F);
          inventory.insertItem(c);
          c.setAuxData((byte)1);
          c = createItem(861, 20.0F);
          inventory.insertItem(c);
          c.setAuxData((byte)1);
        }
        else
        {
          Item inventory = getInventory();
          float ql = 30.0F;
          Item c = createItem(7, ql);
          inventory.insertItem(c);
          c.setAuxData((byte)1);
          c = createItem(84, ql);
          inventory.insertItem(c);
          c.setAuxData((byte)1);
          
          ql = 10.0F;
          c = createItem(8, ql);
          inventory.insertItem(c);
          c.setAuxData((byte)1);
          ql = 50.0F;
          c = createItem(143, ql);
          inventory.insertItem(c);
          c.setAuxData((byte)1);
          ql = 2.0F;
          c = createItem(77, ql);
          inventory.insertItem(c);
          c.setAuxData((byte)1);
          ql = 10.0F;
          c = createItem(1, ql);
          inventory.insertItem(c);
          c.setAuxData((byte)1);
          c = createItem(25, ql);
          inventory.insertItem(c);
          c.setAuxData((byte)1);
          c = createItem(24, ql);
          inventory.insertItem(c);
          c.setAuxData((byte)1);
          c = createItem(20, ql);
          inventory.insertItem(c);
          c.setAuxData((byte)1);
          c = createItem(27, ql);
          inventory.insertItem(c);
          c.setAuxData((byte)1);
          ql = Servers.localServer.challengeServer ? 41.0F : 20.0F;
          c = createItem(516, ql);
          inventory.insertItem(c);
          c.setAuxData((byte)1);
          ql = Servers.localServer.challengeServer ? 40.0F : 10.0F;
          c = createItem(319, ql);
          inventory.insertItem(c);
          c.setAuxData((byte)1);
          if (Servers.localServer.isChallengeServer())
          {
            ql = 40.0F;
            c = createItem(274, ql);
            inventory.insertItem(c);
            c.setAuxData((byte)1);
            c = createItem(274, ql);
            inventory.insertItem(c);
            c.setAuxData((byte)1);
            c = createItem(279, ql);
            inventory.insertItem(c);
            c.setAuxData((byte)1);
            c = createItem(277, ql);
            inventory.insertItem(c);
            c.setAuxData((byte)1);
            c = createItem(277, ql);
            inventory.insertItem(c);
            c.setAuxData((byte)1);
            c = createItem(278, ql);
            inventory.insertItem(c);
            c.setAuxData((byte)1);
            c = createItem(278, ql);
            inventory.insertItem(c);
            c.setAuxData((byte)1);
            c = createItem(275, ql);
            inventory.insertItem(c);
            c.setAuxData((byte)1);
            c = createItem(276, ql);
            inventory.insertItem(c);
            c.setAuxData((byte)1);
            int x = Server.rand.nextInt(3);
            if (x == 0) {
              c = createItem(87, 50.0F);
            } else if (x == 1) {
              c = createItem(21, 50.0F);
            } else if (x == 2) {
              c = createItem(290, 50.0F);
            }
            c.setAuxData((byte)1);
            inventory.insertItem(c);
          }
          else
          {
            ql = 50.0F;
            c = createItem(21, ql);
            inventory.insertItem(c);
            c.setAuxData((byte)1);
            ql = 30.0F;
            c = createItem(105, ql);
            inventory.insertItem(c);
            c.setAuxData((byte)1);
            c = createItem(105, ql);
            inventory.insertItem(c);
            c.setAuxData((byte)1);
            c = createItem(107, ql);
            inventory.insertItem(c);
            c.setAuxData((byte)1);
            c = createItem(106, ql);
            inventory.insertItem(c);
            c.setAuxData((byte)1);
            c = createItem(106, ql);
            inventory.insertItem(c);
            c.setAuxData((byte)1);
            c = createItem(103, ql);
            inventory.insertItem(c);
            c.setAuxData((byte)1);
            c = createItem(103, ql);
            inventory.insertItem(c);
            c.setAuxData((byte)1);
            c = createItem(108, ql);
            inventory.insertItem(c);
            c.setAuxData((byte)1);
            c = createItem(104, ql);
            inventory.insertItem(c);
            c.setAuxData((byte)1);
          }
          c = createItem(480, ql);
          c.setAuxData((byte)1);
          inventory.insertItem(c);
          c = createItem(861, ql);
          c.setAuxData((byte)1);
          inventory.insertItem(c);
          c = createItem(862, ql);
          c.setAuxData((byte)1);
          inventory.insertItem(c);
          c = createItem(781, 20.0F);
          inventory.insertItem(c);
          wearItems();
        }
      }
      catch (Exception ex)
      {
        logger.log(Level.INFO, "Failed to create some items for the test server.", ex);
      }
    }
  }
  
  public void setSecondsToLogout(int seconds)
  {
    if ((hasLink()) && (seconds > this.secondsToLogout)) {
      this.secondsToLogout = seconds;
    }
  }
  
  public void checkPaymentUpdate() {}
  
  private void pollPayment()
  {
    long tl = this.saveFile.getPaymentExpire();
    if (tl > System.currentTimeMillis())
    {
      tl -= System.currentTimeMillis();
      if ((tl < 3600000L) && (System.currentTimeMillis() - this.lastSentWarning > 600000L))
      {
        this.lastSentWarning = System.currentTimeMillis();
        getCommunicator().sendAlertServerMessage("Your premium time expires within the hour.", (byte)1);
      }
      else if (tl < 86400000L)
      {
        if (System.currentTimeMillis() - this.lastSentWarning > 3600000L)
        {
          this.lastSentWarning = System.currentTimeMillis();
          getCommunicator().sendAlertServerMessage("Your premium time expires today.", (byte)1);
        }
      }
      else if (tl < 604800000L)
      {
        if (System.currentTimeMillis() - this.lastSentWarning > 86400000L)
        {
          this.lastSentWarning = System.currentTimeMillis();
          getCommunicator().sendAlertServerMessage("Your premium time expires this week.", (byte)1);
        }
      }
    }
  }
  
  public boolean pollDead()
  {
    if (isDead())
    {
      if (this.secondsToLogout < -1) {
        return true;
      }
      if (!hasLink()) {
        if (this.secondsToLogout > -1) {
          this.secondsToLogout -= 1;
        }
      }
      if (this.secondsToLinkDeath > 0)
      {
        this.secondsToLinkDeath -= 1;
        if (this.secondsToLinkDeath == 2) {
          if (!isTransferring()) {
            this.communicator.sendShutDown(this.disconnectReason, false);
          }
        }
        if (this.secondsToLinkDeath == 0)
        {
          setFrozen(false);
          setLink(false);
        }
      }
      return this.loggedout;
    }
    return false;
  }
  
  public void receivedCmd(int cmd)
  {
    if ((!this.gotHash) && (this.secondsPlayedSinceLinkloss > 1200) && (hasLink())) {
      this.gotHash = true;
    }
  }
  
  private void pollAlcohol()
  {
    if (this.secondsPlayed % 20.0F == 0.0F) {
      if (getAlcohol() > 0.0F)
      {
        setAlcohol(getAlcohol() - 1.0F);
      }
      else if (getAlcoholAddiction() > 0L)
      {
        this.saveFile.setAlcoholTime(getAlcoholAddiction() - 1L);
        if ((getAlcoholAddiction() > 1000L) && (getAlcoholAddiction() % 100L == 0L)) {
          try
          {
            getCommunicator().sendNormalServerMessage("You tremble and shake from withdrawal.");
            getCurrentAction().setFailSecond(getCurrentAction().getCounterAsFloat() + 1.0F);
            getCurrentAction().setPower(-40.0F);
            achievement(295);
          }
          catch (NoSuchActionException localNoSuchActionException) {}
        }
      }
    }
  }
  
  private void nutcase(Cultist cultist)
  {
    int result = Server.rand.nextInt(cultist.getLevel() + 10);
    String toBroadCast = " twitches nervously.";
    switch (result)
    {
    case 1: 
      toBroadCast = " suddenly coughs and looks nervously around.";
      break;
    case 2: 
      toBroadCast = " gives you a scared look.";
      break;
    case 3: 
      toBroadCast = " stares at you with black eyes.";
      break;
    case 4: 
      toBroadCast = " shows " + getHisHerItsString() + " teeth and snarls at you.";
      break;
    case 5: 
      toBroadCast = " scorns someone invisible.";
      break;
    case 6: 
      toBroadCast = " curses loudly.";
      break;
    case 7: 
      toBroadCast = " spits and froths disgustingly.";
      break;
    case 8: 
      toBroadCast = " scratches " + getHisHerItsString() + " skin wildly for a few seconds.";
      break;
    case 9: 
      toBroadCast = " looks at you with disgust.";
      break;
    case 10: 
      toBroadCast = " suddenly whimpers.";
      break;
    case 11: 
      toBroadCast = " makes some erratic twitching moves.";
      break;
    case 12: 
      toBroadCast = " stares at the sky.";
      break;
    case 13: 
      toBroadCast = " stares at " + getHisHerItsString() + " palm.";
      break;
    case 14: 
      toBroadCast = " drools a bit.";
      break;
    case 15: 
      toBroadCast = " wipes " + getHisHerItsString() + " nose clean from some gooey snot.";
      break;
    case 16: 
      toBroadCast = " murmurs something about 'unfair.. danger...'.";
      break;
    case 17: 
      toBroadCast = " pats " + getHimHerItString() + "self on the back.";
      break;
    case 18: 
      toBroadCast = " suddenly has a haunted look in the eyes.";
      break;
    case 19: 
      toBroadCast = " screams out loud!";
      break;
    case 20: 
      toBroadCast = " looks for something on the ground.";
      break;
    case 21: 
      toBroadCast = " wipes some tears from " + getHisHerItsString() + " eyes.";
      break;
    default: 
      toBroadCast = " twitches nervously.";
    }
    getCommunicator().sendNormalServerMessage("You feel strange and out of time.");
    Server.getInstance().broadCastAction(getName() + toBroadCast, this, 5);
  }
  
  private void sendNewPhantasm(boolean insanity)
  {
    if (this.phantasms == null) {
      this.phantasms = new HashSet();
    }
    float px = getPosX() - 5.0F + Server.rand.nextFloat() * 10.0F;
    float py = getPosY() - 5.0F + Server.rand.nextFloat() * 10.0F;
    if (!isOnSurface())
    {
      px = getPosX() - 1.0F + Server.rand.nextFloat() * 2.0F;
      py = getPosY() - 1.0F + Server.rand.nextFloat() * 2.0F;
    }
    long newCid = calculatePhantasmId((int)px >> 2, (int)py >> 2, getLayer());
    if (!this.phantasms.contains(Long.valueOf(newCid)))
    {
      int templateId = 11;
      int rand = Server.rand.nextInt(10);
      if (rand == 0) {
        templateId = 12;
      } else if (rand == 1) {
        templateId = 57;
      } else if (rand == 2) {
        templateId = 18;
      } else if (rand == 3) {
        templateId = 19;
      } else if (rand == 4) {
        templateId = 23;
      } else if (rand == 5) {
        templateId = 58;
      } else if (rand == 6) {
        templateId = 35;
      }
      try
      {
        CreatureTemplate ct = CreatureTemplateFactory.getInstance().getTemplate(templateId);
        String ctname = ct.getName();
        if (insanity)
        {
          int nnam = Server.rand.nextInt(10);
          switch (nnam)
          {
          case 0: 
            ctname = "Terror";
            break;
          case 1: 
            ctname = "Pus";
            break;
          case 2: 
            ctname = "Rotten Blood";
            break;
          case 3: 
            ctname = "Silent Death";
            break;
          case 4: 
            ctname = "Sickness";
            break;
          case 5: 
            ctname = "Watcher";
            break;
          case 6: 
            ctname = "Scorn";
            break;
          case 7: 
            ctname = "Omen";
            break;
          case 8: 
            ctname = "Ratatosk";
            break;
          case 9: 
            ctname = "Pain";
            break;
          default: 
            ctname = "Doom";
          }
        }
        try
        {
          float pz = Zones.calculateHeight(px, py, isOnSurface());
          double lNewrot = Math.atan2(getStatus().getPositionY() - py, getStatus().getPositionX() - px);
          setRotation((float)(lNewrot * 57.29577951308232D) + 90.0F);
          getCommunicator().sendNewCreature(newCid, ctname, ct.getModelName(), px, py, pz, getBridgeId(), (float)lNewrot, 
          
            (byte)getLayer(), getFloorLevel() <= 0, false, true, (byte)-1, getFace(), (byte)0, false, false, (byte)0);
          
          getCommunicator().setCreatureDamage(newCid, 100.0F);
          this.phantasms.add(Long.valueOf(newCid));
        }
        catch (NoSuchZoneException localNoSuchZoneException) {}
      }
      catch (NoSuchCreatureTemplateException localNoSuchCreatureTemplateException) {}
    }
  }
  
  public static long calculatePhantasmId(int tileX, int tileY, int layer)
  {
    return ((tileX & 0xFFFF) << 40) + ((tileY & 0xFFFF) << 16) + (layer & 0xFFFF);
  }
  
  public boolean shouldStopTrading(boolean firstCall)
  {
    if (firstCall) {
      getTrade().end(this, false);
    }
    return System.currentTimeMillis() - this.startedTrading > 60000L;
  }
  
  public void startTrading()
  {
    this.startedTrading = System.currentTimeMillis();
  }
  
  private final void checkSendLinkStatus()
  {
    if (this.links != null)
    {
      int numlinks = getLinks().length;
      if (this.lastLinksSent != numlinks)
      {
        if (numlinks == 0) {
          getCommunicator().sendRemoveSpellEffect(SpellEffectsEnum.LINKS);
        } else {
          getCommunicator().sendAddSpellEffect(SpellEffectsEnum.LINKS, 100000, numlinks);
        }
        this.lastLinksSent = ((byte)numlinks);
      }
    }
  }
  
  public byte getRarity()
  {
    byte rarity = 0;
    if ((Servers.isThisATestServer()) && (this.nextActionRarity != 0))
    {
      rarity = this.nextActionRarity;
      this.nextActionRarity = 0;
    }
    else if (this.windowOfCreation > 0)
    {
      this.windowOfCreation = 0;
      
      float faintChance = 1.0F;
      
      int supPremModifier = 0;
      if (isPaying())
      {
        faintChance = 1.03F;
        supPremModifier = 3;
      }
      if (Server.rand.nextFloat() * 10000.0F <= faintChance) {
        rarity = 3;
      } else if (Server.rand.nextInt(100) <= 0 + supPremModifier) {
        rarity = 2;
      } else if (Server.rand.nextBoolean()) {
        rarity = 1;
      }
    }
    return rarity;
  }
  
  public boolean shouldGiveAffinity(int currAffinityCount, boolean isCharacteristic)
  {
    if (!Features.Feature.AFFINITY_GAINS.isEnabled()) {
      return false;
    }
    if ((isPaying()) && (this.windowOfAffinity > 0))
    {
      float chance = 1.0F / (newAffinityChance * (isCharacteristic ? 3.0F : 1.0F) * (currAffinityCount + 1));
      if (Server.rand.nextFloat() < chance)
      {
        this.windowOfAffinity = 0;
        return true;
      }
    }
    return false;
  }
  
  public void resetInactivity(boolean sleepBonus)
  {
    if ((sleepBonus) && (isSBIdleOffEnabled()) && (!getSaveFile().frozenSleep)) {
      this.lastSleepBonusActivity = System.currentTimeMillis();
    }
    this.lastActivity = System.currentTimeMillis();
  }
  
  public boolean isSBIdleOffEnabled()
  {
    return !hasFlag(43);
  }
  
  public long getSleepBonusInactivity()
  {
    return System.currentTimeMillis() - this.lastSleepBonusActivity;
  }
  
  public long getInactivity()
  {
    return System.currentTimeMillis() - this.lastActivity;
  }
  
  public boolean isBlockingPvP()
  {
    if ((Servers.localServer.isChallengeOrEpicServer()) && (!Server.getInstance().isPS()) && 
      (!Servers.isThisATestServer())) {
      return false;
    }
    return !hasFlag(44);
  }
  
  public boolean poll()
  {
    if (!isFullyLoaded())
    {
      getCommunicator().setAvailableMoves(24);
      return false;
    }
    if (this.pushCounter > 0) {
      this.pushCounter -= 1;
    }
    if (this.lastSentQuestion-- < 0) {
      this.lastSentQuestion = 0;
    }
    if (this.guardSecondsLeft > 0) {
      this.guardSecondsLeft = ((byte)(this.guardSecondsLeft - 1));
    }
    if (this.breedCounter > 0) {
      this.breedCounter -= 1;
    }
    if (this.raritySeconds > 0)
    {
      this.raritySeconds -= 1;
      if (this.raritySeconds <= 0) {
        setRarityShader((byte)0);
      }
    }
    if (this.conchticker > 0)
    {
      this.conchticker -= 1;
      if (this.conchticker == 0)
      {
        SimplePopup toSend = new SimplePopup(this, "Something in your pocket", "You suddenly notice a conch in your pocket. Maybe you should examine it?");
        toSend.sendQuestion();
      }
    }
    if (this.farwalkerSeconds > 0)
    {
      this.farwalkerSeconds = ((byte)(this.farwalkerSeconds - 1));
      if (this.farwalkerSeconds == 0)
      {
        getMovementScheme().setFarwalkerMoveMod(false);
        getCommunicator().sendRemoveSpellEffect(SpellEffectsEnum.FARWALKER);
        getStatus().sendStateString();
      }
    }
    decreaseOpportunityCounter();
    if (this.CRBonusCounter > 0)
    {
      this.CRBonusCounter = ((byte)(this.CRBonusCounter - 1));
      if (this.CRBonusCounter == 0)
      {
        if (this.CRBonus > 0)
        {
          this.CRBonus = ((byte)(this.CRBonus - 1));
          if (this.CRBonus > 0) {
            this.CRBonusCounter = 3;
          } else {
            getCommunicator().sendRemoveSpellEffect(SpellEffectsEnum.CR_BONUS);
          }
        }
        getStatus().sendStateString();
      }
    }
    if (this.removePvPDeathTimer > 0L)
    {
      this.removePvPDeathTimer -= 1L;
      if (this.removePvPDeathTimer == 0L)
      {
        Players.getInstance().removePvPDeath(getWurmId());
        if (Players.getInstance().hasPvpDeaths(getWurmId())) {
          this.removePvPDeathTimer = 10800000L;
        }
      }
    }
    if (!this.loggedout)
    {
      if ((this.lastSentServerTime = (short)(this.lastSentServerTime + 1)) >= 60)
      {
        pollGlobalEffects();
        this.saveFile.checkIfResetSellEarning();
        getCommunicator().sendServerTime();
        this.lastSentServerTime = 0;
        if ((getCultist() != null) && (getCultist().getPath() == 4)) {
          if ((getCultist().getLevel() > 5) && (Server.rand.nextInt(2000) == 0)) {
            sendNewPhantasm(true);
          }
        }
      }
      if (this.musicPlayer != null) {
        this.musicPlayer.tickSecond();
      }
      if (Servers.localServer.isChallengeServer()) {
        clearChallengeScores();
      }
      this.movementScheme.decreaseFreeMoveCounter();
      if (this.windowOfCreation > 0)
      {
        this.windowOfCreation -= 1;
      }
      else if (Server.rand.nextInt(3600) == 0)
      {
        this.windowOfCreation = 20;
        if (getCitizenVillage() != null) {
          this.windowOfCreation += (int)Math.min(10.0F, getCitizenVillage().getFaithCreateValue());
        }
      }
      if (this.windowOfAffinity > 0) {
        this.windowOfAffinity -= 1;
      } else if (Server.rand.nextInt(7200) == 0) {
        this.windowOfAffinity = 15;
      }
      if (this.transferCounter > 0) {
        if ((--this.transferCounter <= 0) && (hasLink()))
        {
          getCommunicator().sendAlertServerMessage("You may now move again.");
          getCommunicator().setReady(true);
          getMovementScheme().resumeSpeedModifier();
        }
      }
      checkSendLinkStatus();
      if ((getVisionArea() != null) && (getVisionArea().isInitialized()))
      {
        if (getVisionArea().getSurface() != null) {
          getVisionArea().getSurface().pollVisibleVehicles();
        }
        if (getVisionArea().getUnderGround() != null) {
          getVisionArea().getUnderGround().pollVisibleVehicles();
        }
      }
      if (this.justCombined)
      {
        this.justCombined = false;
        getMovementScheme().resumeSpeedModifier();
      }
      if (this.enemyPresenceCounter > 0)
      {
        if (Servers.isThisAPvpServer()) {
          this.enemyPresenceCounter += 1;
        }
        if (this.enemyPresenceCounter == minEnemyPresence)
        {
          getCommunicator().sendAlertServerMessage("Something is wrong. An irritating feeling comes over you and you cannot focus. Your normal skillgains suffer.", (byte)4);
          
          getCommunicator().sendAddSpellEffect(SpellEffectsEnum.ENEMY, 100000, 50.0F);
        }
        if (this.enemyPresenceCounter == 1200)
        {
          getCommunicator().sendAlertServerMessage("You now feel greatly disturbed by an enemy presence. While your normal skills still suffer, your aggressive actions are probably more effective.", (byte)4);
          
          getCommunicator().sendAddSpellEffect(SpellEffectsEnum.ENEMY, 100000, 100.0F);
        }
      }
      if ((this.secondsPlayed % 60.0F == 0.0F) && (isFighting())) {
        resetInactivity(true);
      }
      if (!getSaveFile().frozenSleep)
      {
        if (hasSleepBonus())
        {
          if (this.sendSleepCounter++ > 30)
          {
            getCommunicator().sendSleepInfo();
            this.sendSleepCounter = 0;
          }
        }
        else
        {
          getSaveFile().frozenSleep = true;
          getCommunicator().sendNormalServerMessage("You feel the last of your sleep bonus run out.", (byte)2);
          getCommunicator().sendSleepInfo();
        }
        if (isSBIdleOffEnabled())
        {
          if (getSleepBonusInactivity() >= 600000L)
          {
            getCommunicator().sendNormalServerMessage("Auto-freezing sleep bonus after " + 
              Server.getTimeFor(600000L) + " of inactivity.");
            
            getSaveFile().frozenSleep = true;
            getCommunicator().sendNormalServerMessage("You refrain from using your sleep bonus, but may turn it back on immediately.");
            getCommunicator().sendSleepInfo();
          }
          if ((getSleepBonusInactivity() >= 299000L) && 
            (getSleepBonusInactivity() < 300000L)) {
            getCommunicator().sendAlertServerMessage("You have been inactive and your sleep bonus will auto-freeze in " + 
              Server.getTimeFor(600000L - getSleepBonusInactivity()) + ".");
          }
          if ((getSleepBonusInactivity() >= 539000L) && 
            (getSleepBonusInactivity() < 540000L)) {
            getCommunicator().sendAlertServerMessage("You have been inactive and your sleep bonus will auto-freeze in " + 
              Server.getTimeFor(600000L - getSleepBonusInactivity()) + ".");
          }
        }
      }
      if ((this.opponentCounter > 0) && (this.opponent == null)) {
        if (--this.opponentCounter == 0)
        {
          this.lastOpponent = null;
          getCombatHandler().setCurrentStance(-1, (byte)15);
          this.combatRound = 0;
        }
      }
      if (!Servers.localServer.testServer)
      {
        pollStealAttack();
      }
      else
      {
        this.maySteal = true;
        this.mayAttack = true;
      }
      this.status.pollDetectInvis();
      
      this.stuckCounter = Math.max(0, --this.stuckCounter);
      boolean remove = false;
      if (getSpellEffects() != null) {
        getSpellEffects().poll();
      }
      attackTarget();
      if ((!isFighting()) && (this.fightlevel > 0) && (Server.getSecondsUptime() % 20 == 0))
      {
        this.fightlevel = ((byte)Math.max(0, this.fightlevel - 1));
        getCommunicator().sendFocusLevel(getWurmId());
      }
      if (this.loggedout) {
        return true;
      }
      if (isKing())
      {
        if (this.secondsPlayed == 60.0F) {
          if (!this.sentChallenge)
          {
            King king = King.getKing(getKingdomId());
            if (king != null) {
              if ((king.hasBeenChallenged()) && (king.getChallengeAcceptedDate() <= 0L)) {
                getCommunicator().sendSafeServerMessage("In two minutes you will automatically be presented with the Royal Challenge popup. You may do this manually instead by using the command /challenge");
              }
            }
          }
        }
        if (Server.rand.nextInt(10) == 0)
        {
          King king = King.getKing(getKingdomId());
          if (king != null) {
            if (king.getChallengeAcceptedDate() > 0L)
            {
              if (System.currentTimeMillis() > king.getChallengeAcceptedDate()) {
                if (isInOwnDuelRing())
                {
                  if (Servers.localServer.testServer)
                  {
                    long secs = (System.currentTimeMillis() - king.getChallengeAcceptedDate()) / 1000L;
                    getCommunicator().sendAlertServerMessage(secs + " passed of 300 on this test server.");
                    if (System.currentTimeMillis() - king.getChallengeAcceptedDate() > 300000L) {
                      king.passedChallenge();
                    }
                  }
                  else if (System.currentTimeMillis() - king.getChallengeAcceptedDate() > 1800000L)
                  {
                    king.passedChallenge();
                  }
                  if (Server.rand.nextInt(10) == 0) {
                    getCommunicator().sendAlertServerMessage("Unseen eyes watch you.");
                  }
                }
                else
                {
                  king.setFailedChallenge();
                  getCommunicator().sendAlertServerMessage("You have failed the challenge! You are now at the mercy of your subjects.");
                }
              }
            }
            else if (king.hasBeenChallenged())
            {
              if (!this.sentChallenge) {
                if (this.secondsPlayed > 180.0F) {
                  MethodsCreatures.sendChallengeKingQuestion(this);
                }
              }
            }
            else {
              this.sentChallenge = false;
            }
          }
        }
      }
      if (Server.rand.nextInt(isPriest() ? 36000 : 72000) == 0)
      {
        float mod = 1.0F;
        if (getFaith() != 0.0F) {
          mod = (100.0F - getFaith() / 2.0F) / 100.0F;
        }
        if (Servers.localServer.PVPSERVER) {
          if (getAlignment() < 0.0F)
          {
            EndGameItem altar = EndGameItems.getEvilAltar();
            if ((altar != null) && 
              (isWithinDistanceTo(altar.getItem().getPosX(), altar.getItem().getPosY(), altar
              .getItem().getPosZ(), 200.0F))) {
              mod /= 2.0F;
            }
          }
          else if (getAlignment() > 0.0F)
          {
            EndGameItem altar = EndGameItems.getGoodAltar();
            if ((altar != null) && 
              (isWithinDistanceTo(altar.getItem().getPosX(), altar.getItem().getPosY(), altar
              .getItem().getPosZ(), 200.0F))) {
              mod /= 2.0F;
            }
          }
        }
        mod = Math.min(mod, 1.0F);
        if ((getAlignment() < -2.0F) || ((getAlignment() < 0.0F) && (getFaith() == 0.0F))) {
          try
          {
            if (MethodsReligion.mayReceiveAlignment(this)) {
              setAlignment(getAlignment() + 1.0F * mod);
            }
          }
          catch (IOException iox)
          {
            logger.log(Level.WARNING, getName(), iox);
          }
        } else if ((getAlignment() > 2.0F) || ((getAlignment() > 0.0F) && (getFaith() == 0.0F))) {
          try
          {
            if (MethodsReligion.mayReceiveAlignment(this)) {
              setAlignment(getAlignment() - 1.0F * mod);
            }
          }
          catch (IOException iox)
          {
            logger.log(Level.WARNING, getName(), iox);
          }
        }
        if (this.citizenVillage != null) {
          this.citizenVillage.setLogin();
        }
      }
      pollPayment();
      if (this.secondsToLogout > -1) {
        this.secondsToLogout -= 1;
      }
      if (this.myceliumHealCounter > -1) {
        this.myceliumHealCounter -= 1;
      }
      if (getCultist() != null) {
        getCultist().poll();
      }
      this.secondsToLinkDeath -= 1;
      if (this.secondsToLinkDeath == 2) {
        if (!isTransferring()) {
          this.communicator.sendShutDown(this.disconnectReason, false);
        }
      }
      if (this.secondsToLinkDeath == 0)
      {
        setFrozen(false);
        setLink(false);
      }
      if (this.affcounter > 0) {
        if ((this.affcounter = (byte)(this.affcounter - 1)) <= 0)
        {
          getCommunicator().sendNormalServerMessage(this.affstring);
          this.affstring = null;
        }
      }
      if (this.battle != null) {
        if (System.currentTimeMillis() - this.battle.getEndTime() > 240000L) {
          this.battle.removeCreature(this);
        }
      }
      if (!isDead())
      {
        pollItems();
        
        this.saveFile.pollResistances(this.communicator);
        checkLantern();
        
        checkBreedCounter();
        spreadCrownInfluence();
        if (this.crownInfluence > 0) {
          setCrownInfluence(this.crownInfluence - 1);
        }
      }
      if (this.receivedLinkloss != 0L)
      {
        this.sentClimbing = 0L;
        this.sentWind = 0L;
        if ((this.secondsToLogout <= 0) && (
          (getCommunicator().getCurrentmove() == null) || (getCommunicator().getCurrentmove().getNext() == null)))
        {
          if ((this.battle != null) && 
            (this.opponent == null)) {
            this.battle.removeCreature(this);
          }
          if ((this.battle == null) && (this.opponent == null))
          {
            Server.getInstance().addCreatureToRemove(this);
            
            remove = true;
          }
          else if (getSaveFile().currentServer != Servers.localServer.id)
          {
            Server.getInstance().addCreatureToRemove(this);
            
            remove = true;
          }
        }
        else if (this.loggedout)
        {
          remove = true;
        }
        if (this.communicator != null) {
          this.communicator.setAvailableMoves(24);
        }
      }
      else
      {
        this.secondsPlayed += 1.0F;
        this.secondsPlayedSinceLinkloss += 1;
        if (this.sentClimbing != 0L) {
          if ((getVisionArea() != null) && (getVisionArea().isInitialized()) && (isFullyLoaded()) && (this.transferCounter == 0))
          {
            long now = System.currentTimeMillis();
            if (now - this.sentClimbing > 30000L)
            {
              getCommunicator().sendAlertServerMessage("You failed to respond in time. Disconnecting.");
              logoutIn(5, "Game client communication was disrupted and you were disconnected.");
              this.sentClimbing = 0L;
            }
          }
          else
          {
            this.sentClimbing = System.currentTimeMillis();
          }
        }
        if (this.sentWind != 0L) {
          if ((getVisionArea() != null) && (getVisionArea().isInitialized()) && (isFullyLoaded()) && (this.transferCounter == 0))
          {
            long now = System.currentTimeMillis();
            if (now - this.sentWind > 120000L) {
              this.sentWind = 0L;
            }
          }
          else
          {
            this.sentWind = System.currentTimeMillis();
          }
        }
        if (this.sentMountSpeed != 0L) {
          if ((getVisionArea() != null) && (getVisionArea().isInitialized()) && (isFullyLoaded()) && (this.transferCounter == 0))
          {
            long now = System.currentTimeMillis();
            if (now - this.sentMountSpeed > 120000L) {
              this.sentMountSpeed = 0L;
            }
          }
          else
          {
            this.sentMountSpeed = System.currentTimeMillis();
          }
        }
        if (this.communicator != null)
        {
          if (getVisionArea() != null) {
            if (getVisionArea().getSurface() != null) {
              getVisionArea().getSurface().moveAllCreatures();
            }
          }
          if (getVisionArea() != null) {
            if (getVisionArea().getUnderGround() != null) {
              getVisionArea().getUnderGround().moveAllCreatures();
            }
          }
          if (this.secondsPlayed % 10.0F == 0.0F)
          {
            if (this.teleports > 50) {
              logoutIn(5, "Teleport loop");
            }
            this.teleports = 0;
          }
          this.communicator.tickSecond();
        }
        if (this.secondsPlayed % 500.0F == 0.0F) {
          if (isChampion()) {
            if (System.currentTimeMillis() - getChampTimeStamp() > 14515200000L)
            {
              getCommunicator().sendSafeServerMessage("Your time as a Champion of " + 
                getDeity().name + " has ended. Glory to you!", (byte)2);
              Server.getInstance().broadCastSafe(
                getDeity().name + " has decided to let " + getName() + " step down as Champion. Glorious be " + 
                getHeSheItString() + " who lives forever in the Eternal Records!");
              
              revertChamp();
            }
          }
        }
      }
      pollAlcohol();
      if (this.saveFile.checkFatigue()) {
        this.communicator.sendSafeServerMessage("You feel rested.");
      }
      checkItemsWatched();
      if (isTeleporting()) {
        if (!isWithinTeleportTime()) {
          Players.getInstance().logoutPlayer(this);
        }
      }
      if (getBody() != null) {
        getBody().poll();
      } else {
        logger.log(Level.WARNING, getName() + "'s body is null.");
      }
      if ((isClimbing()) || (isTeleporting()) || (getMovementScheme().isIntraTeleporting()) || 
        (getMovementScheme().isKeyPressed())) {
        getStatus().setNormalRegen(false);
      } else if (this.vehicle != -10L)
      {
        if (this.currentTile != null)
        {
          boolean noStam = false;
          short[] steepness = Creature.getTileSteepness(this.currentTile.tilex, this.currentTile.tiley, isOnSurface());
          
          Vehicle vehic = Vehicles.getVehicleForId(this.vehicle);
          if (vehic != null)
          {
            if ((steepness[0] > -13) && (steepness[1] > 20)) {
              noStam = true;
            }
            if (!vehic.creature) {
              try
              {
                Item vehicleObj = Items.getItem(this.vehicle);
                if (vehicleObj.isBoat()) {
                  noStam = false;
                }
              }
              catch (NoSuchItemException nsi)
              {
                logger.log(Level.INFO, nsi.getMessage(), nsi);
              }
            } else if (steepness[0] < -13) {
              noStam = true;
            }
          }
          if (noStam) {
            getStatus().setNormalRegen(false);
          }
        }
        else
        {
          getStatus().setNormalRegen(false);
        }
      }
      else if (getPositionZ() + getAltOffZ() < -1.3D) {
        getStatus().setNormalRegen(false);
      }
      pollStamina();
      getStatus().pollFat();
      if (this.damageCounter > 0)
      {
        this.damageCounter = ((short)(this.damageCounter - 1));
        if (this.damageCounter <= 0)
        {
          removeWoundMod();
          getStatus().sendStateString();
        }
      }
      if ((this.webArmourModTime > 0.0F) && (this.webArmourModTime-- <= 1.0F))
      {
        this.webArmourModTime = 0.0F;
        if (getMovementScheme().setWebArmourMod(false, 0.0F)) {
          getMovementScheme().setWebArmourMod(false, 0.0F);
        }
        if ((!isPaying()) && 
          (getSkills() != null)) {
          getSkills().paying = false;
        }
      }
      if (this.secondsPlayed % 10.0F == 0.0F)
      {
        if (this.hasLoveEffect)
        {
          if ((getCultist() == null) || (getCultist().mayStartLoveEffect()))
          {
            this.hasLoveEffect = false;
            refreshAttitudes();
            getCommunicator().sendNormalServerMessage("The stream of love dissipates.");
          }
        }
        else if ((getCultist() != null) && (getCultist().hasLoveEffect())) {
          this.hasLoveEffect = true;
        }
        this.saveFile.pollReputation(System.currentTimeMillis());
        checkVehicleSpeeds();
        if (Server.rand.nextInt(6) == 0)
        {
          Cultist cultist = getCultist();
          if ((cultist != null) && (cultist.getPath() == 4) && 
            (Server.rand.nextInt(40 - Math.min(19, cultist.getLevel())) == 0)) {
            nutcase(cultist);
          }
        }
      }
      if (this.secondsPlayed % 5.0F == 0.0F)
      {
        pollFavor();
        if (this.favorGainSecondsLeft > 0) {
          this.favorGainSecondsLeft -= 5;
        }
      }
      if (this.secondsPlayed % 60.0F == 0.0F)
      {
        checkPaymentUpdate();
        if (getPower() < 2) {
          if (this.secondsPlayed == 540.0F)
          {
            if (getCommunicator().isInvulnerable()) {
              getCommunicator().sendAlertServerMessage("You will be logged off in one minute since you are invulnerable. Move around a little to prevent this.");
            }
          }
          else if (this.secondsPlayed == 600.0F) {
            if (getCommunicator().isInvulnerable())
            {
              getCommunicator().sendAlertServerMessage("You have been idle for too long and will be logged off.");
              logger.log(Level.INFO, "Logging off " + getName() + " since " + getHeSheItString() + " has been invulnerable for ten minutes.");
              
              logoutIn(10, "You have been idle for too long.");
              achievement(82);
            }
          }
        }
        try
        {
          int newActs = Math.max(2, (int)(this.skills.getSkill(100).getKnowledge(0.0D) / 10.0D));
          if (newActs != this.maxNumActions) {
            getCommunicator().sendNormalServerMessage("You may now queue " + newActs + " actions.");
          }
          this.maxNumActions = newActs;
        }
        catch (NoSuchSkillException nss)
        {
          this.skills.learn(100, 20.0F);
          this.maxNumActions = 2;
        }
      }
      if (this.moneySendCounter++ > 59)
      {
        this.moneySendCounter = 0;
        if (this.saveFile.getMoneyToSend() > 0L)
        {
          LoginServerWebConnection lsw = new LoginServerWebConnection();
          if (lsw.addMoney(this, getName(), this.saveFile.getMoneyToSend(), "Sold items"))
          {
            getCommunicator().sendSafeServerMessage("You receive " + this.saveFile.getMoneyToSend() + " iron coins.");
            this.saveFile.resetMoneyToSend();
          }
          else
          {
            getCommunicator().sendAlertServerMessage("We failed to contact the ingame bank. You may not receive the " + this.saveFile
              .getMoneyToSend() + " iron coins you have sold items for.");
          }
        }
      }
      sendItemsTaken();
      sendItemsDropped();
      trimAttackers(false);
      this.numattackers = 0;
      this.hasAddedToAttack = false;
      AffinitiesTimed.poll(this);
      if (!remove)
      {
        if (this.secondsPlayed % 60.0F == 0.0F)
        {
          Set<WurmMail> waitingMail = WurmMail.getWaitingMailFor(getWurmId());
          if (this.mailItemsWaiting != waitingMail.size())
          {
            this.mailItemsWaiting = waitingMail.size();
            if (!waitingMail.isEmpty()) {
              getCommunicator().sendServerMessage("You sense imps whispering your name and saying you have " + waitingMail
                .size() + " mail waiting to be picked up.", 255, 200, 20, (byte)2);
            }
          }
          Delivery[] waitingDeliveries = Delivery.getWaitingDeliveries(getWurmId());
          if (this.deliveriesWaiting != waitingDeliveries.length)
          {
            this.deliveriesWaiting = waitingDeliveries.length;
            if (this.deliveriesWaiting > 0) {
              getCommunicator().sendServerMessage("You sense a wagoner whispering your name and saying you have " + (this.deliveriesWaiting == 1 ? "a delivery" : new StringBuilder().append(this.deliveriesWaiting).append(" deliveries").toString()) + " waiting to be accepted.", 255, 200, 20, (byte)2);
            }
          }
          Delivery[] lostDeliveries = Delivery.getLostDeliveries(getWurmId());
          if (this.deliveriesFailed != lostDeliveries.length)
          {
            if (lostDeliveries.length > this.deliveriesFailed) {
              getCommunicator().sendServerMessage("You sense a wagoner whispering your name and saying you have one or more deliveries that have lost their wagoner.", 255, 200, 20, (byte)2);
            }
            this.deliveriesFailed = lostDeliveries.length;
          }
        }
        if ((!this.askedForVotes) && (this.secondsPlayed % 3.0F == 0.0F)) {
          getVotes();
        }
        if ((this.gotVotes) && (this.secondsPlayed % 5.0F == 0.0F)) {
          gotVotes(false);
        }
        if (this.secondsPlayed == 10.0F) {
          Tickets.sendRequiresAckMessage(this);
        }
        if (this.waitingForFriendCount == 0)
        {
          sendNormalServerMessage(this.waitingForFriendName + " is not currently available, please try again later.");
          sendFriendTimedOut();
        }
        else if (this.waitingForFriendCount >= 0)
        {
          this.waitingForFriendCount -= 1;
        }
        if ((this.whenStudied > 0L) && (this.whenStudied < WurmCalendar.getCurrentTime() - 900000L))
        {
          System.out.println(this.whenStudied + " <> " + (WurmCalendar.getCurrentTime() - 900000L));
          sendNormalServerMessage("You have forgotten whatever it was you studied.");
          setStudied(0);
        }
      }
      else if (this.waitingForFriendCount >= 0)
      {
        sendFriendTimedOut();
      }
      return remove;
    }
    this.secondsToLinkDeath -= 1;
    if (this.secondsToLinkDeath <= 0) {
      setLink(false);
    }
    this.secondsToLogout -= 1;
    this.loggedout = false;
    logout();
    if (this.waitingForFriendCount >= 0) {
      sendFriendTimedOut();
    }
    return true;
  }
  
  private void sendFriendTimedOut()
  {
    PlayerState pstate = PlayerInfoFactory.getPlayerState(this.waitingForFriendName);
    if (pstate.getServerId() == Servers.getLocalServerId())
    {
      try
      {
        Player p = Players.getInstance().getPlayer(this.waitingForFriendName);
        p.remoteAddFriend(getName(), getKingdomId(), (byte)3, false, true);
      }
      catch (NoSuchPlayerException localNoSuchPlayerException) {}
    }
    else
    {
      WcAddFriend waf = new WcAddFriend(getName(), getKingdomId(), this.waitingForFriendName, (byte)3, true);
      if (Servers.isThisLoginServer()) {
        waf.sendToPlayerServer(this.waitingForFriendName);
      } else {
        waf.sendToLoginServer();
      }
    }
    this.waitingForFriendName = "";
    this.waitingForFriendCount = -1;
  }
  
  public final boolean checkLoyaltyProgram()
  {
    if (!hasFlag(10)) {
      if ((this.saveFile.paymentExpireDate > 0L) && (!this.saveFile.isFlagSet(63))) {
        if ((System.currentTimeMillis() < this.saveFile.paymentExpireDate) || (getPower() > 0))
        {
          setPremStuff();
          return true;
        }
      }
    }
    return false;
  }
  
  private final void setPremStuff()
  {
    new Thread("setPremStuff-Thread-" + getWurmId())
    {
      public void run()
      {
        if (!Servers.localServer.LOGINSERVER)
        {
          Player.this.contactLoginServerForAwards(true);
        }
        else
        {
          Player.this.setFlag(10, true);
          AwardLadder.awardTotalLegacy(Player.this.saveFile);
        }
      }
    }.start();
  }
  
  private final void contactLoginServerForAwards(boolean sendMess)
  {
    LoginServerWebConnection lsw = new LoginServerWebConnection();
    int[] premAndSilver = lsw.getPremTimeSilvers(getWurmId());
    if (premAndSilver[0] >= 0)
    {
      setFlag(10, true);
      if (this.saveFile.awards == null)
      {
        this.saveFile.awards = new Awards(getWurmId(), 0, premAndSilver[0], 0, 0, premAndSilver[1], 0L, 0, 0, true);
      }
      else
      {
        this.saveFile.awards.setMonthsPaidEver(premAndSilver[0]);
        this.saveFile.awards.setSilversPaidEver(premAndSilver[1]);
        this.saveFile.awards.update();
      }
      AwardLadder.awardTotalLegacy(this.saveFile);
    }
    else if (sendMess)
    {
      getCommunicator().sendAlertServerMessage("The login server is unavailable. Please try later.");
    }
  }
  
  public final void setUndeadType(byte udtype)
  {
    this.saveFile.undeadType = udtype;
  }
  
  public final boolean isUndead()
  {
    return this.saveFile.undeadType != 0;
  }
  
  public final byte getUndeadType()
  {
    return this.saveFile.undeadType;
  }
  
  public final String getUndeadTitle()
  {
    if (!isUndead()) {
      return "";
    }
    switch (this.saveFile.undeadType)
    {
    case 0: 
      return "";
    case 1: 
      return "Zombie";
    case 3: 
      return "Ghost";
    case 8: 
      return "Ghoul";
    case 4: 
      return "Spectre";
    case 5: 
      return "Lich";
    case 6: 
      return "Lich King";
    case 7: 
      return "Ghast";
    }
    return "";
  }
  
  private void checkVehicleSpeeds()
  {
    if (Server.rand.nextInt(8) == 0) {
      if (getVehicle() != -10L)
      {
        Vehicle vehic = Vehicles.getVehicleForId(getVehicle());
        if (vehic != null)
        {
          if (isVehicleCommander()) {
            vehic.updateDraggedSpeed(false);
          }
          if (vehic.creature) {
            try
            {
              Creature c = Server.getInstance().getCreature(getVehicle());
              if (c.isOnFire()) {
                if (Server.rand.nextInt(10) == 0)
                {
                  int dam = (int)((1000 + Server.rand.nextInt(4000)) * (100.0F - getSpellDamageProtectBonus()) / 100.0F);
                  if (dam > 1000)
                  {
                    Wound wound = null;
                    boolean dead = false;
                    try
                    {
                      byte pos = getBody().getRandomWoundPos();
                      if (Server.rand.nextInt(10) <= 6) {
                        if (getBody().getWounds() != null)
                        {
                          wound = getBody().getWounds().getWoundAtLocation(pos);
                          if (wound != null)
                          {
                            dead = wound.modifySeverity(dam);
                            wound.setBandaged(false);
                            setWounded();
                          }
                        }
                      }
                      if (wound == null) {
                        addWoundOfType(null, (byte)4, 1, true, 1.0F, true, dam, 0.0F, 0.0F, false, false);
                      }
                      if (dead) {
                        return;
                      }
                    }
                    catch (Exception ex)
                    {
                      logger.log(Level.WARNING, getName() + ' ' + ex.getMessage(), ex);
                    }
                  }
                }
              }
            }
            catch (Exception localException1) {}
          }
        }
      }
    }
  }
  
  public float getSecondsPlayed()
  {
    return this.secondsPlayed;
  }
  
  public boolean checkPrayerFaith()
  {
    return this.saveFile.checkPrayerFaith();
  }
  
  public void setPrayerSeconds(int prayerSeconds)
  {
    this.favorGainSecondsLeft = prayerSeconds;
  }
  
  public void pollFavor()
  {
    float lMod = 1.0F;
    if (this.saveFile.getFavor() < this.saveFile.getFaith())
    {
      try
      {
        Action act = getCurrentAction();
        if (act.getNumber() == 141) {
          lMod = 0.5F;
        }
      }
      catch (NoSuchActionException localNoSuchActionException) {}
      if (this.favorGainSecondsLeft > 0) {
        lMod *= 2.0F;
      }
      if (this.hasSpiritFavorgain) {
        lMod *= 1.05F;
      }
      lMod *= (1.0F + Math.min(this.status.getFats(), 1.0F) / 3.0F);
      
      lMod = (float)(lMod * (1.149999976158142D + getMovementScheme().armourMod.getModifier()));
      if ((getDeity() != null) && (getFaith() >= 35.0F) && (getDeity().isFavorRegenerator())) {
        lMod *= 1.1F;
      }
      if (lMod > 0.0F) {
        try
        {
          this.saveFile.setFavor(this.saveFile.getFavor() + lMod * (100.0F / (Math.max(1.0F, this.saveFile.getFavor()) * 25.0F)));
        }
        catch (IOException iox)
        {
          logger.log(Level.INFO, getName() + " " + iox.getMessage(), iox);
        }
      }
    }
    else if (this.saveFile.getFavor() > this.saveFile.getFaith())
    {
      try
      {
        this.saveFile.setFavor(this.saveFile.getFaith());
      }
      catch (IOException iox)
      {
        logger.log(Level.INFO, getName() + " " + iox.getMessage(), iox);
      }
    }
  }
  
  public boolean sendTransfer(Server senderServer, String targetIp, int targetPort, String serverpass, int targetServerId, int tilex, int tiley, boolean surfaced, boolean toOrFromEpic, byte targetKingdomId)
  {
    if (isTrading()) {
      getTrade().end(this, true);
    }
    try
    {
      if (!toOrFromEpic) {
        PlayerTransfer.willItemsTransfer(this, true, targetServerId);
      }
      ServerEntry entry = Servers.getServerWithId(targetServerId);
      if ((entry != null) && (getPower() <= 0)) {
        if ((Server.getInstance().isPS()) || (!entry.isChallengeOrEpicServer()))
        {
          if (((Server.getInstance().isPS()) && (Servers.localServer.PVPSERVER)) || (Servers.isThisAChaosServer())) {
            this.saveFile.setChaosKingdom(getKingdomId());
          }
          if ((targetKingdomId == 0) && (
            ((Server.getInstance().isPS()) && (entry.PVPSERVER)) || (entry.isChaosServer()))) {
            targetKingdomId = this.saveFile.getChaosKingdom() == 0 ? 4 : this.saveFile.getChaosKingdom();
          } else if (((Server.getInstance().isPS()) && (entry.HOMESERVER)) || (Servers.isThisAChaosServer())) {
            targetKingdomId = entry.getKingdom() != 0 ? entry.getKingdom() : 4;
          }
        }
      }
      getCommunicator().setGroundOffset(0, true);
      removeIllusion();
      PlayerTransfer pt = new PlayerTransfer(senderServer, this, targetIp, targetPort, serverpass, targetServerId, tilex, tiley, surfaced, toOrFromEpic, targetKingdomId);
      
      Server.getInstance().addIntraCommand(pt);
      setLogout();
      return true;
    }
    catch (Exception ex)
    {
      logger.log(Level.WARNING, ex.getMessage(), ex);
    }
    return false;
  }
  
  public int getSecondsToLogout()
  {
    if (getSaveFile().currentServer != Servers.localServer.id) {
      return 1;
    }
    if (getPower() > 0)
    {
      this.secondsToLogout = 5;
    }
    else
    {
      boolean enemyHomeServer = (Servers.localServer.isChallengeOrEpicServer()) && (Servers.localServer.HOMESERVER) && (Servers.localServer.KINGDOM != getKingdomId());
      if ((enemyHomeServer) && 
        (getCurrentVillage() != null)) {
        return 2419200;
      }
      if (this.secondsToLogout < 300L) {
        if ((this.currentTile != null) && (this.currentTile.getKingdom() != getKingdomId())) {
          return 300;
        }
      }
      if (getEnemyPresense() > 0)
      {
        if ((Servers.localServer.PVPSERVER) && (Servers.localServer.isChallengeOrEpicServer()) && (!Servers.localServer.HOMESERVER)) {
          if ((getCurrentVillage() != null) && (this.currentTile != null) && (this.currentTile.getKingdom() == getKingdomId())) {
            return 3600;
          }
        }
        return 180;
      }
      if (this.secondsToLogout < 60L)
      {
        if (this.citizenVillage != null)
        {
          VolaTile t = getCurrentTile();
          if (t != null)
          {
            if ((t.getVillage() != null) && (t.getVillage() == this.citizenVillage)) {
              return Math.max(this.secondsToLogout, 0);
            }
            return 60;
          }
          return 0;
        }
        return 60;
      }
    }
    return this.secondsToLogout;
  }
  
  public int getSecondsPlayedSinceLinkLoss()
  {
    return this.secondsPlayedSinceLinkloss;
  }
  
  public void setLink(boolean up)
  {
    if (!up)
    {
      if (this.receivedLinkloss == 0L)
      {
        this.receivedLinkloss = System.currentTimeMillis();
        this.secondsPlayedSinceLinkloss = 1;
        this.hasSentPoison = false;
        resetLastSentToolbelt();
        if (getVehicle() != -10L)
        {
          Vehicle v = Vehicles.getVehicleForId(getVehicle());
          if ((v != null) && (v.isChair())) {
            disembark(false);
          }
        }
        setLastVehicle(getVehicle(), getSeatType());
        
        Players.getInstance().sendConnectInfo(this, " lost link.", this.receivedLinkloss, PlayerOnlineStatus.LOST_LINK);
        if (this.communicator != null) {
          this.communicator.setReady(false);
        }
        this.secondsToLogout = Math.max(this.secondsToLogout, getSecondsToLogout());
        this.hasSentPoison = false;
        logger.log(Level.INFO, this.name + " lost link " + this.secondsToLogout + " secstologout.");
        try
        {
          if ((getBody() != null) && (getBody().getBodyItem() != null)) {
            getBody().getBodyItem().removeWatcher(this, false);
          }
          if (getInventory() != null) {
            getInventory().removeWatcher(this, false);
          }
        }
        catch (Exception ex)
        {
          logger.log(Level.WARNING, getName() + " " + ex.getMessage(), ex);
        }
        cancelTeleport();
        setTeleporting(false);
        this.teleportCounter = 0;
        try
        {
          save();
          destroyVisionArea();
        }
        catch (Exception ex)
        {
          logger.log(Level.WARNING, "Failed to save player " + this.name, ex);
        }
        if (isTrading()) {
          getTrade().end(this, false);
        }
        Questions.removeQuestion(this.question);
        closeBank();
        if (isDead()) {
          Players.getInstance().logoutPlayer(this);
        }
      }
    }
    else
    {
      this.secondsPlayedSinceLinkloss = 1;
      this.receivedLinkloss = 0L;
      this.loggedout = false;
    }
  }
  
  public void setLoginStep(int step)
  {
    this.loginStep = step;
  }
  
  public int getLoginStep()
  {
    return this.loginStep;
  }
  
  public boolean isNew()
  {
    return this.newPlayer;
  }
  
  public void setNewPlayer(boolean newp)
  {
    this.newPlayer = newp;
  }
  
  public boolean hasLink()
  {
    return this.receivedLinkloss == 0L;
  }
  
  public static Player doNewPlayer(int templateId, SocketConnection serverConnection)
    throws Exception
  {
    Player toReturn = new Player(templateId, serverConnection);
    return toReturn;
  }
  
  public static Player doNewPlayer(int templateId)
    throws Exception
  {
    Player toReturn = new Player(templateId);
    return toReturn;
  }
  
  public Friend[] getFriends()
  {
    if (this.saveFile != null) {
      return this.saveFile.getFriends();
    }
    return new Friend[0];
  }
  
  @Nullable
  public final Friend getFriend(long friendId)
  {
    if (this.saveFile != null) {
      return this.saveFile.getFriend(friendId);
    }
    return null;
  }
  
  public void addFriend(long wurmId, byte catId, String note)
  {
    this.saveFile.addFriend(wurmId, catId, note, false);
    try
    {
      Player friend = Players.getInstance().getPlayer(wurmId);
      getCommunicator().sendFriend(new PlayerState(friend.getWurmId(), friend.getName(), friend
        .getLastLogin(), PlayerOnlineStatus.ONLINE), note);
    }
    catch (NoSuchPlayerException nsp)
    {
      PlayerState pstate = PlayerInfoFactory.getPlayerState(wurmId);
      if (pstate != null) {
        getCommunicator().sendFriend(pstate, note);
      }
    }
    if (this.saveFile.getFriends().length > 49) {
      achievement(150);
    }
  }
  
  public void removeFriend(long friendWurmId)
  {
    PlayerState fState = PlayerInfoFactory.getPlayerState(friendWurmId);
    String friendName = fState != null ? fState.getPlayerName() : "Unknown";
    PlayerInfoFactory.breakFriendship(getName(), getWurmId(), friendName, friendWurmId);
  }
  
  public void updateFriendData(long friendWurmId, byte catId, String note)
  {
    this.saveFile.updateFriendData(friendWurmId, catId, note);
  }
  
  public long removeFriend(String friendName)
  {
    long friendWurmId = PlayerInfoFactory.breakFriendship(getName(), getWurmId(), friendName);
    if (friendWurmId != -10L) {
      getCommunicator().sendNormalServerMessage(friendName + " is no longer on your friend list.");
    } else {
      getCommunicator().sendNormalServerMessage("Could not find a player called " + friendName + ".");
    }
    return friendWurmId;
  }
  
  public void removeMeFromFriendsList(long wurmId, String friendName)
  {
    WcRemoveFriendship wrf = new WcRemoveFriendship(getName(), getWurmId(), friendName, wurmId);
    if (!Servers.isThisLoginServer()) {
      wrf.sendToLoginServer();
    } else {
      wrf.sendFromLoginServer();
    }
  }
  
  public boolean isFriend(long wurmId)
  {
    Friend[] friends = getFriends();
    for (Friend friend : friends) {
      if (friend.getFriendId() == wurmId) {
        return true;
      }
    }
    return false;
  }
  
  public void reimburse()
  {
    if (!isUndead())
    {
      checkInitialTitles();
      checkJournalAchievements();
      if (getDeity() == null) {
        setFlag(74, true);
      }
      if (!this.saveFile.isReimbursed())
      {
        if ((!WurmCalendar.isChristmas()) && (!WurmCalendar.isEaster())) {
          try
          {
            Item inventory = getInventory();
            if (getPower() >= (Servers.localServer.testServer ? 2 : 4))
            {
              Item wand = createItem(176, 99.0F);
              inventory.insertItem(wand);
              logger.info("Reimbursed " + this.name + " with an Ebony Dev Wand: " + wand);
            }
            else if (getPower() >= 2)
            {
              Item wand = createItem(315, 99.0F);
              inventory.insertItem(wand);
              logger.info("Reimbursed " + this.name + " with an GM Wand: " + wand);
            }
          }
          catch (Exception ex)
          {
            logger.log(Level.INFO, "Failed to reimb " + this.name, ex);
          }
        }
        if (Servers.localServer.testServer)
        {
          try
          {
            Item thingy = ItemFactory.createItem(480, 70.0F, getName());
            getInventory().insertItem(thingy, true);
            Item thingy2 = ItemFactory.createItem(516, 70.0F, getName());
            getInventory().insertItem(thingy2, true);
            Item thingy3 = ItemFactory.createItem(301, 60.0F, getName());
            getInventory().insertItem(thingy3, true);
          }
          catch (Exception localException1) {}
          try
          {
            this.saveFile.setReimbursed(true);
          }
          catch (IOException localIOException) {}
        }
        if ((!WurmCalendar.isChristmas()) && (!WurmCalendar.isEaster())) {
          try
          {
            this.saveFile.setReimbursed(true);
          }
          catch (IOException localIOException1) {}
        }
      }
      if (Features.Feature.GIFT_PACKS.isEnabled()) {
        reimbursePacks(false);
      }
      reimbAnniversaryGift(false);
    }
  }
  
  public final void reimbursePacks(boolean override)
  {
    if ((!hasFlag(46)) || (override))
    {
      if (((!isPaying()) && (!override)) || (Servers.localServer.isChallengeServer())) {
        return;
      }
      try
      {
        Item thingy = ItemFactory.createItem(1097, 70.0F, getName());
        getInventory().insertItem(thingy, true);
        setFlag(46, true);
      }
      catch (Exception localException) {}
      if ((hasFlag(47)) || (override))
      {
        try
        {
          Item thingy = ItemFactory.createItem(1098, 70.0F, getName());
          getInventory().insertItem(thingy, true);
          Item mask = ItemFactory.createItem(1099, 90.0F + Server.rand.nextFloat() * 10.0F, getName());
          getInventory().insertItem(mask, true);
          setFlag(47, false);
        }
        catch (Exception localException1) {}
        try
        {
          addMoney(50000L);
        }
        catch (Exception localException2) {}
      }
    }
  }
  
  public final void reimbAnniversaryGift(boolean override)
  {
    if (Features.Feature.EXTRAGIFT.isEnabled()) {
      if ((!hasFlag(49)) || (override))
      {
        if (((!isPaying()) && (!override)) || (Servers.localServer.isChallengeServer())) {
          return;
        }
        if (getSaveFile() != null)
        {
          int daysPrem = 1;
          if ((override) || (getSaveFile().awards != null))
          {
            daysPrem = override ? 100 + Server.rand.nextInt(900) : Math.max(getSaveFile().awards.getMonthsPaidSinceReset() * 30, Math.max(getSaveFile().awards.getDaysPrem(), getSaveFile().awards.getMonthsPaidEver() * 30));
          }
          else
          {
            contactLoginServerForAwards(false);
            if (getSaveFile().awards != null) {
              daysPrem = Math.max(getSaveFile().awards.getMonthsPaidSinceReset() * 30, Math.max(getSaveFile().awards.getDaysPrem(), getSaveFile().awards.getMonthsPaidEver() * 30));
            } else {
              logger.log(Level.WARNING, getName() + " no premium time/silvers received from login server..");
            }
          }
          float ql = Math.max(20.0F, Math.min(99.99F, daysPrem / 10.0F));
          try
          {
            Item thingy = ItemFactory.createItem(1100, ql, getName());
            if (daysPrem > 900)
            {
              thingy.setRarity((byte)3);
              thingy.setMaterial((byte)34);
            }
            else if (daysPrem > 600)
            {
              thingy.setRarity((byte)2);
              thingy.setMaterial((byte)11);
            }
            else if (daysPrem > 300)
            {
              thingy.setRarity((byte)1);
              switch (Server.rand.nextInt(5))
              {
              case 0: 
                thingy.setMaterial((byte)66);
                break;
              case 1: 
                thingy.setMaterial((byte)45);
                break;
              case 2: 
                thingy.setMaterial((byte)42);
                break;
              case 3: 
                thingy.setMaterial((byte)38);
                break;
              case 4: 
                thingy.setMaterial((byte)39);
              }
            }
            else
            {
              thingy.setMaterial((byte)69);
            }
            thingy.setAuxData((byte)1);
            try
            {
              Item champy = ItemFactory.createItem(1101, ql, getName());
              if (daysPrem > 800) {
                champy.setRarity((byte)3);
              } else if (daysPrem > 600) {
                champy.setRarity((byte)2);
              } else if (daysPrem > 300) {
                champy.setRarity((byte)1);
              }
              thingy.insertItem(champy, true);
            }
            catch (Exception iox)
            {
              logger.log(Level.WARNING, iox.getMessage(), iox);
            }
            getInventory().insertItem(thingy, true);
            getCommunicator().sendSafeServerMessage("There's a new item in your inventory! Happy 10 Years Anniversary!");
          }
          catch (Exception iox2)
          {
            logger.log(Level.WARNING, iox2.getMessage(), iox2);
          }
          setFlag(49, true);
        }
      }
    }
  }
  
  public void setIpaddress(String ipaddress)
  {
    try
    {
      if (this.saveFile == null) {
        logger.log(Level.WARNING, "Savefile is null for " + this.name);
      } else {
        this.saveFile.setIpaddress(ipaddress);
      }
    }
    catch (Exception iox)
    {
      logger.log(Level.WARNING, "Failed to set ipaddress=" + ipaddress + " for player " + this.name, iox);
    }
  }
  
  public SteamId getSteamId()
  {
    return this.saveFile.getSteamId();
  }
  
  public void setSteamID(SteamId steamId)
  {
    try
    {
      if (this.saveFile == null) {
        logger.log(Level.WARNING, "Savefile is null for " + this.name);
      } else {
        this.saveFile.setSteamId(steamId);
      }
    }
    catch (Exception iox)
    {
      logger.log(Level.WARNING, "Failed to set SteamId of " + steamId.getSteamID64() + " for player " + this.name, iox);
    }
  }
  
  public boolean hasPlantedSign()
  {
    return this.saveFile.hasPlantedSign();
  }
  
  public void plantSign()
  {
    try
    {
      if (getPower() == 0) {
        this.saveFile.setPlantedSign();
      }
    }
    catch (Exception iox)
    {
      logger.log(Level.WARNING, this.name + " " + iox.getMessage(), iox);
    }
  }
  
  public void ban(String reason, long expiry)
    throws Exception
  {
    this.saveFile.setBanned(true, reason, expiry);
    Players.getInstance().addBannedIp(this.communicator.getConnection().getIp(), "[" + getName() + "] " + reason, expiry);
    logoutIn(5, "You have been banned. Reason: " + reason);
  }
  
  public Ban getBan()
  {
    if (!this.saveFile.isBanned()) {
      return null;
    }
    if (System.currentTimeMillis() <= this.saveFile.banexpiry) {
      return new PlayerBan(this.saveFile.getName(), this.saveFile.banreason, this.saveFile.banexpiry);
    }
    try
    {
      this.saveFile.setBanned(false, "", 0L);
    }
    catch (Exception iox)
    {
      logger.log(Level.WARNING, "Unbanning " + getName() + " failed!:" + iox.getMessage(), iox);
    }
    return null;
  }
  
  public boolean isIgnored(long playerId)
  {
    if (WurmId.getType(playerId) == 0) {
      return this.saveFile.isIgnored(playerId);
    }
    if (WurmId.getType(playerId) == 1) {
      try
      {
        Creature creature = Creatures.getInstance().getCreature(playerId);
        if (creature.isWagoner()) {
          return hasFlag(54);
        }
      }
      catch (NoSuchCreatureException e)
      {
        logger.log(Level.WARNING, e.getMessage(), e);
      }
    }
    return false;
  }
  
  public boolean addIgnored(long playerId)
    throws IOException
  {
    return this.saveFile.addIgnored(playerId, false);
  }
  
  public boolean removeIgnored(long playerId)
    throws IOException
  {
    return this.saveFile.removeIgnored(playerId);
  }
  
  public long[] getIgnored()
  {
    return this.saveFile.getIgnored();
  }
  
  public boolean isPaying()
  {
    return true;
  }
  
  public boolean isReallyPaying()
  {
    return true;
  }
  
  public final boolean isQAAccount()
  {
    return hasFlag(26);
  }
  
  public int getPower()
  {
    if (this.guest) {
      return 0;
    }
    return this.saveFile.getPower();
  }
  
  public void setPaymentExpire(long paymentExpire)
    throws IOException
  {
    this.saveFile.setPaymentExpire(paymentExpire);
  }
  
  public long getPaymentExpire()
  {
    return this.saveFile.getPaymentExpire();
  }
  
  public void setPower(byte power)
    throws IOException
  {
    this.saveFile.setPower(power);
  }
  
  public void setRank(int newRank)
    throws IOException
  {
    this.saveFile.setRank(newRank);
    
    getCommunicator().sendSafeServerMessage("Your battlerank is now " + newRank + ".");
  }
  
  public void modifyRanking()
  {
    StringBuilder attackerStringbuilder = new StringBuilder();
    Set<Byte> kingdomsInvolved;
    boolean affinityGiven;
    King king;
    int levelsLost;
    if ((!isNewbie()) && (isPaying()))
    {
      if ((getFightingSkill() == null) || (getFightingSkill().getKnowledge() < 20.0D))
      {
        this.attackers = null;
        return;
      }
      int rank = getRank();
      List<Player> validAttackers = new ArrayList();
      int totRank = 0;
      int highestRank = 0;
      int lowestRank = 9999;
      long now = System.currentTimeMillis();
      if ((this.attackers != null) && (this.attackers.size() > 0))
      {
        int count = 0;
        int numberUnknown = 1;
        for (Long attackerId : this.attackers.keySet())
        {
          count++;
          Long time = (Long)this.attackers.get(attackerId);
          if (WurmId.getType(attackerId.longValue()) == 0) {
            try
            {
              Player player = Players.getInstance().getPlayer(attackerId);
              attackerStringbuilder.append(player.getName());
              if (count != this.attackers.size()) {
                attackerStringbuilder.append(", ");
              }
              if ((player.isPaying()) && (!Players.getInstance().isOverKilling(attackerId.longValue(), getWurmId()))) {
                if (now - time.longValue() < 600000L)
                {
                  if ((!Servers.localServer.isChallengeServer()) || (getKingdomId() != player.getKingdomId())) {
                    try
                    {
                      Players.getInstance().addKill(attackerId.longValue(), getWurmId(), getName());
                    }
                    catch (Exception ex)
                    {
                      logger.log(Level.INFO, "Failed to add kill for " + player.getName() + ":" + 
                        getName() + " - " + ex.getMessage(), ex);
                    }
                  }
                  totRank += Math.max(player.getRank(), rank - 500);
                  highestRank = Math.max(player.getRank(), highestRank);
                  lowestRank = Math.min(player.getRank(), lowestRank);
                  validAttackers.add(player);
                }
              }
            }
            catch (NoSuchPlayerException localNoSuchPlayerException) {}
          } else if ((WurmId.getType(attackerId.longValue()) == 1) && 
            (Features.Feature.PVE_DEATHTABS.isEnabled()) && (!Servers.localServer.PVPSERVER) && 
            (!hasFlag(59))) {
            if (now - time.longValue() < 900000L)
            {
              String nameString = "Defeated Foe #" + numberUnknown;
              try
              {
                Creature creature = Creatures.getInstance().getCreature(attackerId.longValue());
                nameString = creature.getNameWithoutFatStatus();
              }
              catch (NoSuchCreatureException e)
              {
                numberUnknown++;
                boolean easterEgg = false;
                float chance = Server.rand.nextFloat();
                if (chance >= 0.99F)
                {
                  nameString = "Carebear";
                  easterEgg = true;
                }
                else if (chance >= 0.98D)
                {
                  nameString = "Wogic";
                  easterEgg = true;
                }
                else if (chance >= 0.97D)
                {
                  nameString = "Lag Monster";
                  easterEgg = true;
                }
                else if (chance >= 0.96D)
                {
                  nameString = "Test Minion";
                  easterEgg = true;
                }
                else if (chance >= 0.95D)
                {
                  nameString = "Server Bug";
                  easterEgg = true;
                }
                else if (chance >= 0.94D)
                {
                  nameString = "Developer";
                  easterEgg = true;
                }
                else if (chance >= 0.93D)
                {
                  nameString = "Server Hamster";
                  easterEgg = true;
                }
                else if (chance >= 0.92D)
                {
                  nameString = "Hell Unicorn";
                  easterEgg = true;
                }
                else if (chance >= 0.91D)
                {
                  nameString = "Heaven Scorpius";
                  easterEgg = true;
                }
                if (easterEgg)
                {
                  int random = Server.rand.nextInt(6);
                  age = "";
                  switch (random)
                  {
                  case 0: 
                    age = "adolescent";
                  case 1: 
                    age = "young";
                  case 2: 
                    age = "mature";
                  case 3: 
                    age = "aged";
                  case 4: 
                    age = "old";
                  case 5: 
                    age = "venerable";
                  }
                  age = "";
                  
                  nameString = age + " " + nameString;
                }
              }
              attackerStringbuilder.append(nameString);
              if (count != this.attackers.size()) {
                attackerStringbuilder.append(", ");
              }
            }
          }
        }
        String age;
        if (validAttackers.size() > 0)
        {
          int avgRank = totRank / validAttackers.size();
          int rankVariance = highestRank - lowestRank;
          int rankDiff = avgRank - rank;
          int points = rankDiff > 0 ? 15 - rankDiff / 25 : 15 - rankDiff / 5;
          int pointsEach = points / validAttackers.size();
          int pointsBonus = 0;
          int totalPointsGiven = 0;
          kingdomsInvolved = new HashSet();
          if (points / 2 >= validAttackers.size())
          {
            pointsEach = points / 2 / validAttackers.size();
            pointsBonus = points / 2;
          }
          else if (points < validAttackers.size())
          {
            pointsEach = 0;
            pointsBonus = points;
          }
          for (Player p : validAttackers) {
            if ((p.getKingdomId() != getKingdomId()) || (p.isEnemyOnChaos(this))) {
              try
              {
                int bonus = pointsBonus > 0 ? (int)((highestRank - p.getRank()) / 2 / rankVariance * pointsBonus) : 0;
                p.checkBattleTitle(p.getRank(), p.getRank() + pointsEach + bonus);
                p.setRank(p.getRank() + pointsEach + bonus);
                if (rank > 1000) {
                  p.setKarma(p.getKarma() + (pointsEach + bonus) * 50);
                }
                totalPointsGiven += pointsEach + bonus;
                if (!kingdomsInvolved.contains(Byte.valueOf(p.getKingdomId()))) {
                  kingdomsInvolved.add(Byte.valueOf(p.getKingdomId()));
                }
              }
              catch (IOException iox)
              {
                logger.log(Level.WARNING, getName() + ": failed to give " + pointsEach + " to " + p.getName(), iox);
              }
            }
          }
          int sknum;
          if ((!Servers.localServer.isChallengeServer()) || (Server.rand.nextInt(5) == 0))
          {
            Affinity[] affs = Affinities.getAffinities(getWurmId());
            Player randomPlayer;
            int sknum;
            AffinitiesTimed at;
            if ((affs.length > 1) || ((isChampion()) && (affs.length > 0)))
            {
              List<Player> possibleGainers = new ArrayList();
              for (Player p : validAttackers) {
                if ((p.getKingdomId() != getKingdomId()) || (p.isEnemyOnChaos(this))) {
                  possibleGainers.add(p);
                }
              }
              if (possibleGainers.size() > 0)
              {
                affinityGiven = false;
                randomPlayer = (Player)possibleGainers.get(Server.rand.nextInt(possibleGainers.size()));
                sknum = affs[Server.rand.nextInt(affs.length)].skillNumber;
                Skill deceasedSkill = getSkills().getSkillOrLearn(sknum);
                while ((!affinityGiven) && (possibleGainers.size() > 0))
                {
                  Skill killerSkill = randomPlayer.getSkills().getSkillOrLearn(sknum);
                  float chanceToGain = deceasedSkill.affinity >= killerSkill.affinity - 1 ? 1.0F : 0.5F;
                  if (Server.rand.nextFloat() <= chanceToGain)
                  {
                    if (killerSkill.affinity == 0) {
                      randomPlayer.getCommunicator().sendNormalServerMessage("You realize that you have developed an affinity for " + 
                        SkillSystem.getNameFor(sknum).toLowerCase() + ".", (byte)2);
                    } else {
                      randomPlayer.getCommunicator().sendNormalServerMessage("You realize that your affinity for " + 
                        SkillSystem.getNameFor(sknum).toLowerCase() + " has grown stronger.", (byte)2);
                    }
                    Affinities.setAffinity(randomPlayer.getWurmId(), sknum, killerSkill.affinity + 1, false);
                    
                    logger.log(Level.INFO, randomPlayer.getName() + " receives affinity " + SkillSystem.getNameFor(sknum) + " from " + getName());
                    affinityGiven = true;
                  }
                  else
                  {
                    possibleGainers.remove(randomPlayer);
                    randomPlayer = (Player)possibleGainers.get(Server.rand.nextInt(possibleGainers.size()));
                  }
                }
                Affinities.decreaseAffinity(getWurmId(), sknum, 1);
                if (!affinityGiven)
                {
                  randomPlayer = (Player)validAttackers.get(Server.rand.nextInt(validAttackers.size()));
                  if ((randomPlayer.getKingdomId() != getKingdomId()) || (randomPlayer.isEnemyOnChaos(this)))
                  {
                    at = AffinitiesTimed.getTimedAffinitiesByPlayer(randomPlayer.getWurmId(), true);
                    at.add(sknum, 604800L);
                    randomPlayer.getCommunicator().sendNormalServerMessage("You realize that you have more of an insight about " + 
                      SkillSystem.getNameFor(sknum).toLowerCase() + ".", (byte)2);
                    at.sendTimedAffinity(randomPlayer, sknum);
                    logger.log(Level.INFO, getName() + " loses affinity " + SkillSystem.getNameFor(sknum) + " from death via " + randomPlayer
                      .getName());
                  }
                }
                for (Player p : validAttackers) {
                  if ((p != randomPlayer) && (
                  
                    (p.getKingdomId() != getKingdomId()) || (p.isEnemyOnChaos(this))))
                  {
                    AffinitiesTimed at = AffinitiesTimed.getTimedAffinitiesByPlayer(p.getWurmId(), true);
                    at.add(sknum, (86400.0F * (1.0F + Server.rand.nextFloat())));
                    p.getCommunicator().sendNormalServerMessage("You realize that you have more of an insight about " + 
                      SkillSystem.getNameFor(sknum).toLowerCase() + ".", (byte)2);
                    at.sendTimedAffinity(p, sknum);
                  }
                }
              }
            }
            else
            {
              sknum = SkillSystem.getRandomSkillNum();
              for (Player p : validAttackers)
              {
                AffinitiesTimed at = AffinitiesTimed.getTimedAffinitiesByPlayer(p.getWurmId(), true);
                at.add(sknum, (3600.0F * (3.0F + Server.rand.nextFloat() * 3.0F)));
                p.getCommunicator().sendNormalServerMessage("You realize that you have more of an insight about " + 
                  SkillSystem.getNameFor(sknum).toLowerCase() + ".", (byte)2);
                at.sendTimedAffinity(p, sknum);
              }
            }
          }
          if ((!isChampion()) && (totalPointsGiven > 0)) {
            try
            {
              setRank(Math.max(1000, rank - (int)(totalPointsGiven * 0.75F)));
              Players.printRanks();
            }
            catch (IOException iox)
            {
              logger.log(Level.WARNING, getName() + ": failed to set rank to " + (rank - (int)(totalPointsGiven * 0.75F)), iox);
            }
          }
          king = King.getKing(getKingdomId());
          if (king != null)
          {
            levelsLost = getRoyalLevels();
            for (Byte b : kingdomsInvolved)
            {
              King k = King.getKing(b.byteValue());
              if (k != null)
              {
                k.addLevelsKilled(Math.max(1, levelsLost / kingdomsInvolved.size()), getName(), levelsLost);
                king.addLevelsLost(Math.max(1, levelsLost / kingdomsInvolved.size()));
              }
            }
          }
        }
      }
    }
    if (attackerStringbuilder.toString().length() > 0) {
      Players.getInstance().broadCastDeathInfo(this, attackerStringbuilder.toString());
    }
    this.attackers = null;
  }
  
  public int getRoyalLevels()
  {
    int nums = 0;
    if (isKing()) {
      nums = 20;
    }
    King k = King.getKing(getKingdomId());
    if (k != null) {
      return Appointments.getAppointments(k.era).getAppointmentLevels(getAppointments(), getWurmId()) + nums;
    }
    if (King.currentEra > 0) {
      return Appointments.getAppointments(King.currentEra).getAppointmentLevels(getAppointments(), getWurmId()) + nums;
    }
    return 0;
  }
  
  public void setAffString(String string)
  {
    if (this.affstring != null) {
      getCommunicator().sendNormalServerMessage(this.affstring);
    }
    this.affstring = string;
    this.affcounter = 10;
  }
  
  public void checkBattleTitle(int oldrank, int newrank)
  {
    if ((oldrank < 1100) && (newrank >= 1100)) {
      addTitle(Titles.Title.Warrior);
    }
    if ((oldrank < 1500) && (newrank >= 1500)) {
      addTitle(Titles.Title.Warrior_Minor);
    }
    if ((oldrank < 1900) && (newrank >= 1900)) {
      addTitle(Titles.Title.Warrior_Master);
    }
  }
  
  public void checkInitialBattleTitles()
  {
    int br = getRank();
    if (br >= 1100) {
      addTitle(Titles.Title.Warrior);
    }
    if (br >= 1500) {
      addTitle(Titles.Title.Warrior_Minor);
    }
    if (br >= 1900) {
      addTitle(Titles.Title.Warrior_Master);
    }
  }
  
  public void checkFaithTitles()
  {
    float f = getFaith();
    if (f >= 50.0F) {
      addTitle(Titles.Title.Faith);
    }
    if (f >= 70.0F) {
      addTitle(Titles.Title.Faith_Minor);
    }
    if (f >= 90.0F) {
      addTitle(Titles.Title.Faith_Master);
    }
    if (f >= 100.0F) {
      addTitle(Titles.Title.Faith_Legend);
    }
    if (f == 30.0F) {
      achievement(569);
    }
    if (f >= 40.0F) {
      maybeTriggerAchievement(608, true);
    }
    if (f >= 70.0F) {
      maybeTriggerAchievement(618, true);
    }
    if (f >= 90.0F) {
      maybeTriggerAchievement(630, true);
    }
  }
  
  public void maybeTriggerAchievement(int achievementId, boolean shouldTrigger)
  {
    if (!shouldTrigger) {
      return;
    }
    if (Achievements.hasAchievement(getWurmId(), achievementId)) {
      return;
    }
    achievement(achievementId);
  }
  
  public void checkJournalAchievements()
  {
    maybeTriggerAchievement(548, getCultist() != null);
    maybeTriggerAchievement(556, getDeity() != null);
    maybeTriggerAchievement(569, getFaith() >= 30.0F);
    maybeTriggerAchievement(570, (getCultist() != null) && (getCultist().getLevel() >= 4));
    maybeTriggerAchievement(572, getTitles().length >= 15);
    maybeTriggerAchievement(578, (getCultist() != null) && (getCultist().getLevel() >= 7));
    maybeTriggerAchievement(599, (getCultist() != null) && (getCultist().getLevel() >= 9));
    maybeTriggerAchievement(579, getTitles().length >= 30);
    maybeTriggerAchievement(591, getTitles().length >= 60);
    
    maybeTriggerAchievement(604, isPriest());
    maybeTriggerAchievement(608, getFaith() >= 40.0F);
    maybeTriggerAchievement(618, getFaith() >= 70.0F);
    maybeTriggerAchievement(630, getFaith() >= 90.0F);
    if (getDeity() != null) {
      maybeTriggerAchievement(626, getAlignment() == -100.0F);
    }
    if (hasFlag(70)) {
      addTitle(Titles.Title.Journal_T6);
    }
  }
  
  public void checkInitialTitles()
  {
    if (getPlayingTime() > 259200000L)
    {
      checkInitialBattleTitles();
      Skill[] sk = this.skills.getSkills();
      int count = 0;
      for (int x = 0; x < sk.length; x++)
      {
        sk[x].checkInitialTitle();
        if (sk[x].getKnowledge() >= 50.0D) {
          count++;
        }
      }
      if (count >= 10) {
        maybeTriggerAchievement(598, true);
      }
    }
  }
  
  private short[] getSpawnPointOutside(Village village)
  {
    if ((!isPaying()) && (Zones.isVillagePremSpawn(village))) {
      return new short[] { -1, -1 };
    }
    return village.getOutsideSpawn();
  }
  
  public void sendSpawnQuestion()
  {
    if (isUndead())
    {
      spawn((byte)0);
    }
    else
    {
      if (this.spawnpoints == null) {
        calculateSpawnPoints();
      }
      if (this.spawnpoints != null)
      {
        SpawnQuestion q = new SpawnQuestion(this, "In the darkness", "Select where you will reenter the light:", getWurmId());
        q.sendQuestion();
      }
    }
  }
  
  public void spawn(byte spawnPoint)
  {
    if (isDead())
    {
      addNewbieBuffs();
      
      setLayer(0, false);
      boolean found = false;
      this.justSpawned = true;
      if (isUndead())
      {
        float[] txty = findRandomSpawnX(false, false);
        float posX = txty[0];
        float posY = txty[1];
        setTeleportPoints(posX, posY, 0, 0);
        
        startTeleporting();
        found = true;
        getCommunicator().sendNormalServerMessage("You are cast back into the horrible light.");
      }
      else
      {
        Iterator<Spawnpoint> it;
        if (this.spawnpoints != null) {
          for (it = this.spawnpoints.iterator(); it.hasNext();)
          {
            Spawnpoint sp = (Spawnpoint)it.next();
            if (sp.number == spawnPoint)
            {
              setTeleportPoints(sp.tilex, sp.tiley, sp.surfaced ? 0 : -1, 0);
              startTeleporting();
              found = true;
              getCommunicator().sendNormalServerMessage("You are cast back into the light.");
              break;
            }
          }
        }
        if (!found)
        {
          if (Servers.localServer.randomSpawns)
          {
            float[] txty = findRandomSpawnX(true, true);
            float posX = txty[0];
            float posY = txty[1];
            setTeleportPoints(posX, posY, 0, 0);
          }
          else if ((getKingdomId() == 3) && (Servers.localServer.SPAWNPOINTLIBX != -1))
          {
            setTeleportPoints(Servers.localServer.SPAWNPOINTLIBX, Servers.localServer.SPAWNPOINTLIBY, 0, 0);
          }
          else if ((getKingdomId() == 2) && (Servers.localServer.SPAWNPOINTMOLX != -1))
          {
            setTeleportPoints(Servers.localServer.SPAWNPOINTMOLX, Servers.localServer.SPAWNPOINTMOLY, 0, 0);
          }
          else
          {
            setTeleportPoints(Servers.localServer.SPAWNPOINTJENNX, Servers.localServer.SPAWNPOINTJENNX, 0, 0);
          }
          getCommunicator().sendNormalServerMessage("You are cast back into the light where it all began.");
          startTeleporting();
        }
      }
      getCommunicator().sendTeleport(false);
      
      setDead(false);
      this.spawnpoints = null;
    }
  }
  
  private boolean calculateMissionSpawnPoint()
  {
    MissionPerformer mp = MissionPerformed.getMissionPerformer(getWurmId());
    if (mp != null)
    {
      MissionPerformed[] perfs = mp.getAllMissionsPerformed();
      for (int x = 0; x < perfs.length; x++) {
        if ((!perfs[x].isInactivated()) && (!perfs[x].isCompleted()) && (!perfs[x].isFailed()) && 
          (perfs[x].isStarted()))
        {
          Mission mission = perfs[x].getMission();
          if (mission != null)
          {
            MissionTrigger spawnPoint = MissionTriggers.getRespawnTriggerForMission(mission.getId(), perfs[x]
              .getState());
            if (spawnPoint != null)
            {
              Spawnpoint sp = spawnPoint.getSpawnPoint();
              if (sp != null)
              {
                if (this.spawnpoints != null) {
                  this.spawnpoints.clear();
                } else {
                  this.spawnpoints = new HashSet();
                }
                this.spawnpoints.add(sp);
                return true;
              }
            }
          }
        }
      }
    }
    return false;
  }
  
  public boolean sendLastMissionInformation()
  {
    Questions.removeQuestions(this);
    boolean sent = false;
    MissionPerformer mp = MissionPerformed.getMissionPerformer(getWurmId());
    if (mp != null)
    {
      if (this.saveFile.getLastTrigger() > 0)
      {
        TriggerEffect eff = TriggerEffects.getTriggerEffect(this.saveFile.getLastTrigger());
        if ((eff != null) && 
          (eff.sendTriggerDescription(this))) {
          sent = true;
        }
      }
      if (!sent)
      {
        MissionPerformed[] perfs = mp.getAllMissionsPerformed();
        for (int x = 0; x < perfs.length; x++) {
          if ((!perfs[x].isInactivated()) && (!perfs[x].isCompleted()) && (!perfs[x].isFailed()) && 
            (perfs[x].isStarted()))
          {
            Mission mission = perfs[x].getMission();
            if (mission != null) {
              if ((mission.getInstruction() != null) && (mission.getInstruction().length() > 0))
              {
                SimplePopup pop = new SimplePopup(this, "Mission start", mission.getInstruction());
                pop.sendQuestion();
                sent = true;
              }
            }
          }
        }
      }
    }
    return sent;
  }
  
  public void setLastTrigger(int triggerEffect)
  {
    this.saveFile.setLastTrigger(triggerEffect);
  }
  
  public void calculateSpawnPoints()
  {
    long start = System.currentTimeMillis();
    this.spawnpoints = new HashSet();
    if (calculateMissionSpawnPoint()) {
      return;
    }
    Object localObject;
    int i;
    Item localItem1;
    if ((isNewTutorial()) && (Servers.localServer.entryServer)) {
      if (getKingdomId() == 4)
      {
        short tpx = (short)Servers.localServer.SPAWNPOINTJENNX;
        short tpy = (short)Servers.localServer.SPAWNPOINTJENNY;
        Village[] villages = Villages.getVillages();
        localObject = villages;i = localObject.length;
        for (localItem1 = 0; localItem1 < i; localItem1++)
        {
          Village vill = localObject[localItem1];
          if ((vill.isPermanent) && (vill.kingdom == 4)) {
            try
            {
              tpx = (short)vill.getToken().getTileX();
              tpy = (short)vill.getToken().getTileY();
              if ((vill.getReputation(this) > -30) && (Zones.isGoodTileForSpawn(tpx, tpy, vill.isOnSurface(), true)))
              {
                Spawnpoint spa = new Spawnpoint(this.spnums++, vill.getName(), tpx, tpy, true);
                this.spawnpoints.add(spa);
              }
            }
            catch (NoSuchItemException localNoSuchItemException1) {}
          }
        }
        return;
      }
    }
    short tpx = (short)Servers.localServer.SPAWNPOINTJENNX;
    short tpy = (short)Servers.localServer.SPAWNPOINTJENNY;
    
    this.spnums = 0;
    Item spawn;
    if (Servers.localServer.randomSpawns)
    {
      Item[] spawns = Items.getSpawnPoints();
      localObject = spawns;i = localObject.length;
      for (localItem1 = 0; localItem1 < i; localItem1++)
      {
        spawn = localObject[localItem1];
        
        Spawnpoint spa = new Spawnpoint(this.spnums++, "Spawnpoint " + spawn.getDescription(), (short)(spawn.getTileX() - 4 + Server.rand.nextInt(9)), (short)(spawn.getTileY() - 4 + Server.rand.nextInt(9)), true);
        this.spawnpoints.add(spa);
      }
    }
    Set<Village> villageSet = new HashSet();
    Village[] arrayOfVillage1;
    if ((!Servers.localServer.HOMESERVER) || (getKingdomId() == Servers.localServer.KINGDOM))
    {
      Village[] villages = Villages.getVillages();
      arrayOfVillage1 = villages;localItem1 = arrayOfVillage1.length;
      for (spawn = 0; spawn < localItem1; spawn++)
      {
        Village vill = arrayOfVillage1[spawn];
        if (((vill.isCapital()) || (vill.isPermanent)) && (vill.kingdom == getKingdomId()))
        {
          boolean ok = true;
          if ((!isPaying()) && (!Servers.localServer.isChaosServer())) {
            if (Zones.isVillagePremSpawn(vill)) {
              ok = false;
            }
          }
          if (ok) {
            try
            {
              if ((vill.isPermanent) || (vill.isAlly(this)) || (vill.isCitizen(this)))
              {
                tpx = (short)vill.getToken().getTileX();
                tpy = (short)vill.getToken().getTileY();
                if ((vill.getReputation(this) > -30) && (Zones.isGoodTileForSpawn(tpx, tpy, vill.isOnSurface(), true)))
                {
                  Spawnpoint spa = new Spawnpoint(this.spnums++, vill.getName(), tpx, tpy, true);
                  this.spawnpoints.add(spa);
                  if (!villageSet.contains(vill)) {
                    villageSet.add(vill);
                  }
                }
                if (!Servers.localServer.entryServer)
                {
                  short[] sp = getSpawnPointOutside(vill);
                  tpx = sp[0];
                  tpy = sp[1];
                  if (Zones.isGoodTileForSpawn(tpx, tpy, vill.isOnSurface()))
                  {
                    Spawnpoint ohome = new Spawnpoint(this.spnums++, "Outside " + vill.getName(), tpx, tpy, vill.isOnSurface());
                    this.spawnpoints.add(ohome);
                    if (!villageSet.contains(vill)) {
                      villageSet.add(vill);
                    }
                  }
                }
              }
            }
            catch (NoSuchItemException localNoSuchItemException2) {}
          }
        }
        Kingdom k = Kingdoms.getKingdom(getKingdomId());
        if ((k != null) && (k.isCustomKingdom()))
        {
          Village v = Villages.getCapital(getKingdomId());
          if ((v == null) && (this.spawnpoints.isEmpty())) {
            v = Villages.getFirstVillageForKingdom(getKingdomId());
          }
          if ((v != null) && (v.getReputation(this) > -30)) {
            if (!villageSet.contains(v))
            {
              boolean ok = true;
              if ((!isPaying()) && (!Servers.localServer.isChaosServer())) {
                if (Zones.isVillagePremSpawn(v)) {
                  ok = false;
                }
              }
              if (ok) {
                try
                {
                  tpx = (short)v.getToken().getTileX();
                  tpy = (short)v.getToken().getTileY();
                }
                catch (NoSuchItemException nsi)
                {
                  logger.log(Level.WARNING, v.getName() + " no token.");
                  
                  tpx = (short)v.getTokenX();
                  tpy = (short)v.getTokenY();
                }
              }
            }
          }
        }
      }
    }
    else if (getKingdomTemplateId() == 3)
    {
      if (Servers.localServer.SPAWNPOINTLIBX > 0)
      {
        tpx = (short)Servers.localServer.SPAWNPOINTLIBX;
        tpy = (short)Servers.localServer.SPAWNPOINTLIBY;
      }
    }
    else if (getKingdomTemplateId() == 2)
    {
      if (Servers.localServer.SPAWNPOINTMOLX > 0)
      {
        tpx = (short)Servers.localServer.SPAWNPOINTMOLX;
        tpy = (short)Servers.localServer.SPAWNPOINTMOLY;
      }
    }
    else if (getKingdomTemplateId() == 1)
    {
      if (Servers.localServer.SPAWNPOINTJENNX > 0)
      {
        tpx = (short)Servers.localServer.SPAWNPOINTJENNX;
        tpy = (short)Servers.localServer.SPAWNPOINTJENNY;
      }
    }
    else
    {
      tpx = (short)Servers.localServer.SPAWNPOINTJENNX;
      tpy = (short)Servers.localServer.SPAWNPOINTJENNY;
      Village[] villages = Villages.getVillages();
      arrayOfVillage1 = villages;Item localItem2 = arrayOfVillage1.length;
      for (spawn = 0; spawn < localItem2; spawn++)
      {
        Village vill = arrayOfVillage1[spawn];
        if (((vill.isPermanent) || (vill.isCapital())) && (vill.kingdom == getKingdomId())) {
          if ((vill.isPermanent) || (vill.isAlly(this)) || (vill.isCitizen(this)))
          {
            boolean ok = true;
            if (!isPaying()) {
              if (Zones.isVillagePremSpawn(vill)) {
                ok = false;
              }
            }
            if (ok) {
              try
              {
                tpx = (short)vill.getToken().getTileX();
                tpy = (short)vill.getToken().getTileY();
                if ((vill.getReputation(this) > -30) && (Zones.isGoodTileForSpawn(tpx, tpy, vill.isOnSurface(), true)))
                {
                  Spawnpoint spa = new Spawnpoint(this.spnums++, vill.getName(), tpx, tpy, true);
                  this.spawnpoints.add(spa);
                  if (!villageSet.contains(vill)) {
                    villageSet.add(vill);
                  }
                }
                if (!Servers.localServer.entryServer)
                {
                  short[] sp = getSpawnPointOutside(vill);
                  tpx = sp[0];
                  tpy = sp[1];
                  if (Zones.isGoodTileForSpawn(sp[0], sp[1], vill.isOnSurface()))
                  {
                    Spawnpoint ohome = new Spawnpoint(this.spnums++, "Outside " + vill.getName(), tpx, tpy, vill.isOnSurface());
                    this.spawnpoints.add(ohome);
                    if (!villageSet.contains(vill)) {
                      villageSet.add(vill);
                    }
                  }
                }
              }
              catch (NoSuchItemException localNoSuchItemException3) {}
            }
          }
        }
      }
    }
    Village hometown = null;
    if (Servers.localServer.entryServer)
    {
      tpx = 468;
      tpy = 548;
    }
    VolaTile t = Zones.getTileOrNull(tpx, tpy, true);
    short[] sp;
    if (t != null)
    {
      hometown = t.getVillage();
      if (hometown != null)
      {
        if (!villageSet.contains(hometown))
        {
          boolean ok = true;
          if (!isPaying()) {
            if (Zones.isVillagePremSpawn(hometown)) {
              ok = false;
            }
          }
          if (ok)
          {
            if ((hometown.getReputation(this) > -30) && (Zones.isGoodTileForSpawn(tpx, tpy, hometown.isOnSurface(), true)))
            {
              Spawnpoint spa = new Spawnpoint(this.spnums++, hometown.getName(), tpx, tpy, true);
              this.spawnpoints.add(spa);
              
              villageSet.add(hometown);
            }
            if (!Servers.localServer.entryServer)
            {
              sp = getSpawnPointOutside(hometown);
              tpx = sp[0];
              tpy = sp[1];
              if (Zones.isGoodTileForSpawn(tpx, tpy, hometown.isOnSurface()))
              {
                Spawnpoint ohome = new Spawnpoint(this.spnums++, "Outside " + hometown.getName(), tpx, tpy, hometown.isOnSurface());
                this.spawnpoints.add(ohome);
                
                villageSet.add(hometown);
              }
            }
          }
        }
      }
      else if ((!Servers.localServer.randomSpawns) || (Items.getSpawnPoints().length == 0)) {
        if (Zones.isGoodTileForSpawn(tpx, tpy, true, true))
        {
          Spawnpoint spa = new Spawnpoint(this.spnums++, "Start", tpx, tpy, true);
          this.spawnpoints.add(spa);
        }
      }
    }
    else if ((!Servers.localServer.randomSpawns) || (Items.getSpawnPoints().length == 0))
    {
      if (Zones.isGoodTileForSpawn(tpx, tpy, true, true))
      {
        Spawnpoint spa = new Spawnpoint(this.spnums++, "Start", tpx, tpy, true);
        this.spawnpoints.add(spa);
      }
    }
    int tents = 0;
    for (Item i : Items.getTents()) {
      if ((i.getZoneId() > 0) && (i.getLastOwnerId() == getWurmId())) {
        if (tents < 50)
        {
          VolaTile tentTile = Zones.getTileOrNull(i.getTileX(), i.getTileY(), i.isOnSurface());
          if (tentTile != null) {
            if (tentTile.getKingdom() == getKingdomId())
            {
              boolean ok = true;
              if (!isPaying()) {
                if (Zones.isPremSpawnZoneAt(i.getTileX(), i.getTileY())) {
                  ok = false;
                }
              }
              if (ok)
              {
                Spawnpoint spa = new Spawnpoint(this.spnums++, "Tent " + i.getDescription(), (short)i.getTileX(), (short)i.getTileY(), i.isOnSurface());
                this.spawnpoints.add(spa);
                tents++;
              }
            }
          }
        }
        else
        {
          sendNormalServerMessage("You can only have 50 tent spawn points. Skipping the rest...");
          break;
        }
      }
    }
    if (this.citizenVillage != null)
    {
      if (hometown != this.citizenVillage) {
        if (!villageSet.contains(this.citizenVillage))
        {
          boolean ok = true;
          if (!isPaying()) {
            if (Zones.isVillagePremSpawn(this.citizenVillage)) {
              ok = false;
            }
          }
          if (ok)
          {
            villageSet.add(this.citizenVillage);
            short[] sp = this.citizenVillage.getSpawnPoint();
            tpx = sp[0];
            tpy = sp[1];
            if (Zones.isGoodTileForSpawn(tpx, tpy, this.citizenVillage.isOnSurface(), true))
            {
              Spawnpoint home = new Spawnpoint(this.spnums++, this.citizenVillage.getName(), tpx, tpy, this.citizenVillage.isOnSurface());
              this.spawnpoints.add(home);
            }
            try
            {
              sp = this.citizenVillage.getTokenCoords();
              tpx = sp[0];
              tpy = sp[1];
              
              String spawnName = "Token of " + this.citizenVillage.getName();
              if (!Zones.isGoodTileForSpawn(tpx, tpy, this.citizenVillage.isOnSurface(), true)) {
                spawnName = spawnName + " (Warning: Steep)";
              }
              Spawnpoint token = new Spawnpoint(this.spnums++, spawnName, tpx, tpy, this.citizenVillage.isOnSurface());
              this.spawnpoints.add(token);
            }
            catch (NoSuchItemException localNoSuchItemException4) {}
            sp = getSpawnPointOutside(this.citizenVillage);
            tpx = sp[0];
            tpy = sp[1];
            if ((tpx > 0) && (tpy > 0) && (Zones.isGoodTileForSpawn(tpx, tpy, this.citizenVillage.isOnSurface())))
            {
              Spawnpoint ohome = new Spawnpoint(this.spnums++, "Outside " + this.citizenVillage.getName(), tpx, tpy, this.citizenVillage.isOnSurface());
              this.spawnpoints.add(ohome);
            }
          }
        }
      }
      Village[] alliances = this.citizenVillage.getAllies();
      for (int x = 0; x < alliances.length; x++) {
        if ((!alliances[x].isDisbanding()) && (this.spnums < 40)) {
          if ((Math.abs(getTileX() - alliances[x].getTokenX()) < 100) && 
            (Math.abs(getTileY() - alliances[x].getTokenY()) < 100)) {
            if (!villageSet.contains(alliances[x])) {
              if (alliances[x].getReputation(this) > -30)
              {
                boolean ok = true;
                if (!isPaying()) {
                  if (Zones.isVillagePremSpawn(alliances[x])) {
                    ok = false;
                  }
                }
                if (ok)
                {
                  villageSet.add(alliances[x]);
                  short[] sp = alliances[x].getSpawnPoint();
                  tpx = sp[0];
                  tpy = sp[1];
                  if (Zones.isGoodTileForSpawn(tpx, tpy, alliances[x].isOnSurface(), true))
                  {
                    Spawnpoint home = new Spawnpoint(this.spnums++, alliances[x].getName(), tpx, tpy, alliances[x].isOnSurface());
                    this.spawnpoints.add(home);
                  }
                  if (!Servers.localServer.entryServer)
                  {
                    sp = getSpawnPointOutside(alliances[x]);
                    tpx = sp[0];
                    tpy = sp[1];
                    if ((tpx > 0) && (tpy > 0) && (Zones.isGoodTileForSpawn(tpx, tpy, alliances[x].isOnSurface())))
                    {
                      Spawnpoint ohome = new Spawnpoint(this.spnums++, "Outside " + alliances[x].getName(), tpx, tpy, alliances[x].isOnSurface());
                      this.spawnpoints.add(ohome);
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
    if (this.spawnpoints.size() == 0)
    {
      int tries = 0;
      while (tries < 50)
      {
        tpx = (short)Zones.safeTileX(Server.rand.nextInt(Zones.worldTileSizeX));
        tpy = (short)Zones.safeTileY(Server.rand.nextInt(Zones.worldTileSizeY));
        if (Zones.isGoodTileForSpawn(tpx, tpy, true)) {
          break;
        }
        tries++;
      }
      Spawnpoint ohome = new Spawnpoint(this.spnums++, "Somewhere", tpx, tpy, true);
      this.spawnpoints.add(ohome);
    }
    logger.info("Calculating spawn points for " + getName() + " took " + (System.currentTimeMillis() - start) + "ms");
  }
  
  public final boolean maySummonCorpse()
  {
    return System.currentTimeMillis() - this.saveFile.getLastDeath() > 300000L;
  }
  
  public final long getTimeToSummonCorpse()
  {
    return Math.max(0L, this.saveFile.getLastDeath() + 300000L - System.currentTimeMillis());
  }
  
  public void setDeathEffects(boolean freeDeath, int dtilex, int dtiley)
  {
    this.saveFile.died();
    setDead(true);
    removeWoundMod();
    getStatus().sendStateString();
    closeBank();
    if (this.isLit) {
      try
      {
        this.isLit = false;
        getCurrentTile().setHasLightSource(this, null);
      }
      catch (Exception ex)
      {
        if (logger.isLoggable(Level.FINE)) {
          logger.log(Level.FINE, "Problem checking tile for " + this);
        }
      }
    }
    this.movementScheme.haltSpeedModifier();
    
    getCommunicator().sendNormalServerMessage("You are halted on the way to the netherworld by a dark spirit, demanding knowledge.");
    
    double lMod = 0.25D;
    if (isUndead()) {
      getCommunicator().sendNormalServerMessage("The spirit refuses to let you through and throws you back with extreme force!");
    } else {
      getCommunicator().sendNormalServerMessage("The spirit touches you and you feel drained.");
    }
    if ((getDeity() != null) && (getDeity().isDeathProtector()) && 
      (getFaith() >= 60.0F) && (getFavor() >= 30.0F) && (Server.rand.nextInt(4) > 0))
    {
      getCommunicator().sendNormalServerMessage(
        getDeity().name + " is with you and keeps you safe from the spirit's touch.");
      lMod = 0.125D;
    }
    VolaTile tile = getCurrentTile();
    if ((!this.suiciding) && 
      (tile.getVillage() != null) && (
      (tile.getVillage() == getCitizenVillage()) || ((getCitizenVillage() != null) && (getCitizenVillage().isAlly(tile
      .getVillage()))))) {
      lMod *= 0.1D;
    }
    if ((getKingdomTemplateId() != 3) && (getReputation() < 0)) {
      lMod *= 5.0D;
    }
    if ((isDeathProtected()) && (Server.rand.nextInt(10) > 0))
    {
      getCommunicator().sendSafeServerMessage("The ancient symbol of the stone preserves your sanity and knowledge in the nether world.");
      
      lMod *= 0.5D;
    }
    this.status.removeWounds();
    this.status.modifyStamina2(-100.0F);
    this.status.modifyHunger(55536, 0.5F);
    this.status.modifyThirst(-10000.0F);
    if (!freeDeath)
    {
      if (this.battle != null) {
        this.battle.addCasualty(this);
      }
      boolean pvp = modifyFightSkill(dtilex, dtiley);
      modifyRanking();
      if (pvp) {
        addPvPDeath();
      }
      if (!isUndead()) {
        punishSkills(lMod, pvp);
      }
    }
    if (isTrading()) {
      getTrade().end(this, false);
    }
    if (isChampion()) {
      try
      {
        setRealDeath((byte)(this.saveFile.realdeath - 1));
        if (this.saveFile.realdeath <= 0)
        {
          revertChamp();
          HistoryManager.addHistory(getName(), "has fallen");
        }
      }
      catch (IOException iox)
      {
        logger.log(Level.WARNING, iox.getMessage(), iox);
      }
    }
    tile.deleteCreature(this);
    destroyVisionArea();
    this.suiciding = false;
    this.saveFile.clearSpellResistances(this.communicator);
    if ((!Servers.localServer.entryServer) && (getKingdomId() != 0) && (Servers.localServer.HOMESERVER) && 
      (Servers.localServer.KINGDOM != getKingdomId()))
    {
      ServerEntry entry = Servers.getClosestSpawnServer(getKingdomId());
      if ((entry != null) && (entry.isAvailable(getPower(), isPaying())))
      {
        setMissionDeathEffects();
        setDeathProtected(false);
        logger.log(Level.INFO, "Transferring " + getName() + " to " + entry.name);
        this.communicator.sendDead();
        if (!sendTransfer(Server.getInstance(), entry.INTRASERVERADDRESS, Integer.parseInt(entry.INTRASERVERPORT), entry.INTRASERVERPASSWORD, entry.id, -1, -1, true, false, 
          getKingdomId()))
        {
          logger.log(Level.WARNING, getName() + " failed to transfer.");
          sendSpawnQuestion();
        }
      }
      else
      {
        sendSpawnQuestion();
      }
    }
    else if (!isUndead())
    {
      sendSpawnQuestion();
    }
    setMissionDeathEffects();
    setDeathProtected(false);
    trimAttackers(true);
    if (!isUndead())
    {
      if (hasLink()) {
        this.communicator.sendDead();
      } else {
        Server.getInstance().addCreatureToRemove(this);
      }
    }
    else if (hasLink()) {
      sendSpawnQuestion();
    } else {
      Server.getInstance().addCreatureToRemove(this);
    }
  }
  
  private void addPvPDeath()
  {
    Players.getInstance().addPvPDeath(getWurmId());
    this.removePvPDeathTimer = 10800000L;
  }
  
  public boolean isSuiciding()
  {
    return this.suiciding;
  }
  
  public boolean mayAttack(Creature cret)
  {
    if ((cret.getPower() == 0) && (cret.isPlayer())) {
      if ((!isOnPvPServer()) && (getKingdomTemplateId() == cret.getKingdomId()) && 
        (getKingdomTemplateId() != 3)) {
        if (getKingdomId() == Servers.localServer.KINGDOM) {
          if ((getCitizenVillage() == null) || (!getCitizenVillage().isEnemy(cret))) {
            if (!isDuelOrSpar(cret)) {
              return false;
            }
          }
        }
      }
    }
    if (this.opponent == cret) {
      return super.mayAttack(cret);
    }
    if ((!cret.isPlayer()) || (this.mayAttack)) {
      return super.mayAttack(cret);
    }
    return this.mayAttack;
  }
  
  public static final float[] findRandomSpawnX(boolean checkBeach, boolean useSpawnStones)
  {
    if (useSpawnStones)
    {
      Item[] spawns = Items.getSpawnPoints();
      if (spawns.length > 0)
      {
        Item spawn = spawns[Server.rand.nextInt(spawns.length)];
        return new float[] {spawn
          .getPosX() - 12.0F + Server.rand.nextFloat() * 25.0F, spawn.getPosY() - 12.0F + Server.rand.nextFloat() * 25.0F };
      }
    }
    int tries = 0;
    while (tries++ < 1000000) {
      try
      {
        float posx = (int)(Server.rand.nextFloat() * Zones.worldMeterSizeX);
        float posy = (int)(Server.rand.nextFloat() * Zones.worldMeterSizeY);
        float posz = Zones.calculateHeight(posx, posy, true);
        if (posz > -1.0F)
        {
          short[] st = getTileSteepness((int)posx >> 2, (int)posy >> 2, true);
          if (st[1] < 20) {
            if ((!checkBeach) || (posz < 0.5F)) {
              return new float[] { posx, posy };
            }
          }
        }
      }
      catch (Exception localException) {}
    }
    float posx = (int)(Server.rand.nextFloat() * Zones.worldMeterSizeX);
    float posy = (int)(Server.rand.nextFloat() * Zones.worldMeterSizeY);
    return new float[] { posx, posy };
  }
  
  private void checkMayAttack()
  {
    Skill bodys = null;
    Skill sstrength = null;
    try
    {
      bodys = this.skills.getSkill(102);
    }
    catch (NoSuchSkillException localNoSuchSkillException) {}
    try
    {
      sstrength = this.skills.getSkill(105);
    }
    catch (NoSuchSkillException localNoSuchSkillException1) {}
    if ((bodys == null) || (sstrength == null))
    {
      this.mayAttack = false;
    }
    else if (Servers.localServer.HOMESERVER)
    {
      if ((bodys.getKnowledge(0.0D) < 20.5D) || (sstrength.getKnowledge(0.0D) < 20.5D)) {
        this.mayAttack = false;
      } else {
        this.mayAttack = true;
      }
    }
    else
    {
      try
      {
        bodys = this.skills.getSkill(1);
      }
      catch (NoSuchSkillException localNoSuchSkillException2) {}
      if (bodys == null) {
        this.mayAttack = false;
      } else if ((isGuest()) || (bodys.getKnowledge(0.0D) < 1.5D)) {
        this.mayAttack = false;
      } else {
        this.mayAttack = true;
      }
    }
    if (isUndead()) {
      this.mayAttack = true;
    }
  }
  
  public boolean maySteal()
  {
    if (this.maySteal) {
      return super.mayAttack(null);
    }
    return this.maySteal;
  }
  
  private void checkMaySteal()
  {
    Skill bodys = null;
    try
    {
      bodys = this.skills.getSkill(104);
    }
    catch (NoSuchSkillException localNoSuchSkillException) {}
    if (bodys == null) {
      this.maySteal = false;
    } else if ((isGuest()) || (bodys.getKnowledge(0.0D) < 20.5D)) {
      this.maySteal = false;
    } else {
      this.maySteal = true;
    }
  }
  
  public boolean isNewbie()
  {
    Skill bodys = null;
    try
    {
      bodys = this.skills.getSkill(1);
    }
    catch (NoSuchSkillException localNoSuchSkillException) {}
    if (bodys == null) {
      return true;
    }
    if ((isGuest()) || (bodys.getKnowledge() < 1.5D)) {
      return true;
    }
    return false;
  }
  
  public int getRank()
  {
    return this.saveFile.getRank();
  }
  
  public int getMaxRank()
  {
    return this.saveFile.getMaxRank();
  }
  
  public long getLastLogin()
  {
    return this.saveFile.getLastLogin();
  }
  
  public long getLastLogout()
  {
    return this.saveFile.getLastLogout();
  }
  
  public boolean isInvulnerable()
  {
    if (getPower() > 0) {
      return this.GMINVULN;
    }
    if (getCommunicator().isInvulnerable()) {
      return true;
    }
    return false;
  }
  
  public boolean checkTileInvulnerability()
  {
    if (getCurrentTile() != null)
    {
      if (getCurrentTile().getKingdom() != getKingdomId()) {
        return false;
      }
      if (getCurrentTile().getVillage() != null)
      {
        if ((getCurrentTile().getVillage().isCitizen(this)) || (getCurrentTile().getVillage().isAlly(this.citizenVillage))) {
          return true;
        }
        if (Servers.localServer.PVPSERVER) {
          return false;
        }
        if ((getCurrentTile().getVillage().isEnemy(this.citizenVillage)) || 
          (getCurrentTile().getVillage().getReputation(this) <= -30)) {
          return false;
        }
      }
      else if (Servers.localServer.PVPSERVER)
      {
        return false;
      }
      Creature[] crets = null;
      for (int x = getCurrentTile().tilex - 10; x <= getCurrentTile().tilex + 10; x++) {
        for (int y = getCurrentTile().tiley - 10; y <= getCurrentTile().tiley + 10; y++) {
          if ((x > 0) && (y > 0) && (x < Zones.worldTileSizeX) && (y < Zones.worldTileSizeY))
          {
            VolaTile t = Zones.getTileOrNull(x, y, isOnSurface());
            if (t != null)
            {
              crets = t.getCreatures();
              for (int c = 0; c < crets.length; c++) {
                if (!crets[c].isHuman()) {
                  if (crets[c].getAttitude(this) == 2) {
                    return true;
                  }
                }
              }
            }
          }
        }
      }
    }
    return false;
  }
  
  public int getWarnings()
  {
    return this.saveFile.getWarnings();
  }
  
  public long getLastWarned()
  {
    return this.saveFile.getLastWarned();
  }
  
  public String getWarningStats(long lastWarned)
  {
    return this.saveFile.getWarningStats(lastWarned);
  }
  
  public float getAlignment()
  {
    return this.saveFile.alignment;
  }
  
  public float getFaith()
  {
    if (isPaying()) {
      return this.saveFile.faith;
    }
    return Math.min(30.0F, this.saveFile.faith);
  }
  
  public Deity getDeity()
  {
    return this.saveFile.deity;
  }
  
  public boolean maybeModifyAlignment(float modification)
  {
    boolean checkDirection = false;
    if ((this.saveFile.getAlignment() > 0.0F) && (modification > 0.0F)) {
      checkDirection = true;
    } else if ((this.saveFile.getAlignment() < 0.0F) && (modification < 0.0F)) {
      checkDirection = true;
    }
    if (checkDirection)
    {
      if (!MethodsReligion.mayReceiveAlignment(this)) {
        return false;
      }
      MethodsReligion.setReceivedAlignment(this);
    }
    try
    {
      this.saveFile.setAlignment(this.saveFile.getAlignment() + modification);
      
      return true;
    }
    catch (IOException iox)
    {
      logger.log(Level.WARNING, getName() + " " + iox.getMessage(), iox);
    }
    return false;
  }
  
  public void setAlignment(float alignment)
    throws IOException
  {
    this.saveFile.setAlignment(alignment);
  }
  
  public void modifyFaith(float modifier)
  {
    if ((modifier > 0.0F) || (!isChampion()))
    {
      this.saveFile.modifyFaith(modifier);
      checkFaithTitles();
    }
  }
  
  public void setFaith(float faith)
    throws IOException
  {
    this.saveFile.setFaith(faith);
    checkFaithTitles();
  }
  
  public void setDeity(Deity deity)
    throws IOException
  {
    this.saveFile.setDeity(deity);
    if (deity == null) {
      getCommunicator().sendNormalServerMessage("You no longer follow a deity.");
    } else {
      getCommunicator().sendNormalServerMessage("You will now pray to " + deity.name + ".");
    }
    clearLinks();
    refreshAttitudes();
  }
  
  public void setPriest(boolean priest)
  {
    if (!priest) {
      clearLinks();
    }
    this.saveFile.setPriest(priest);
  }
  
  public boolean isPriest()
  {
    return (this.saveFile.isPaying()) && (this.saveFile.isPriest);
  }
  
  public void setCheated(String reason)
  {
    this.saveFile.setCheated(reason);
  }
  
  public float getFavor()
  {
    return this.saveFile.favor;
  }
  
  public float getFavorLinked()
  {
    float fav = this.saveFile.favor;
    if ((this.links != null) && (this.links.size() > 0)) {
      for (Creature c : this.links.values()) {
        if (c.isWithinDistanceTo(this, 20.0F)) {
          fav += Math.max(0.0F, c.getFavor() - 10.0F);
        }
      }
    }
    return fav;
  }
  
  public void setFavor(float favor)
    throws IOException
  {
    this.saveFile.setFavor(favor);
  }
  
  public void depleteFavor(float favorToRemove, boolean combatSpell)
    throws IOException
  {
    float sumremoved = 0.0F;
    if ((this.links != null) && (this.links.size() > 0)) {
      for (Creature c : this.links.values()) {
        if (c.isWithinDistanceTo(this, 20.0F)) {
          if (sumremoved < favorToRemove) {
            if (c.getFavor() > 0.0F)
            {
              float removed = Math.min(Math.max(0.0F, c.getFavor() - 10.0F), favorToRemove - sumremoved);
              
              sumremoved += removed;
              c.setFavor(c.getFavor() - removed);
            }
          }
        }
      }
    }
    setFavor(getFavor() - (favorToRemove - sumremoved));
    achievement(638, (int)Math.floor(favorToRemove - sumremoved));
    if (favorToRemove >= 50.0F) {
      achievement(619);
    }
  }
  
  public boolean isTrader()
  {
    return true;
  }
  
  public void makeEmoteSound()
  {
    this.lastMadeEmoteSound = System.currentTimeMillis();
  }
  
  public boolean mayEmote()
  {
    return System.currentTimeMillis() - this.lastMadeEmoteSound > 5000L;
  }
  
  public boolean isChampion()
  {
    return this.saveFile.realdeath > 0;
  }
  
  public void setRealDeath(byte realdeathcounter)
    throws IOException
  {
    this.saveFile.setRealDeath(realdeathcounter);
  }
  
  public boolean modifyChampionPoints(int championPointsModifier)
  {
    boolean isZero = this.saveFile.setChampionPoints(
      (short)Math.max(0, this.saveFile.championPoints + championPointsModifier));
    if (isZero) {
      getCommunicator().sendRemoveSpellEffect(SpellEffectsEnum.CHAMP_POINTS);
    } else {
      getCommunicator().sendAddSpellEffect(SpellEffectsEnum.CHAMP_POINTS, 100000, 
        getChampionPoints());
    }
    return isZero;
  }
  
  public void sendAddChampionPoints()
  {
    if (getChampionPoints() > 0) {
      getCommunicator().sendAddSpellEffect(SpellEffectsEnum.CHAMP_POINTS, 100000, 
        getChampionPoints());
    }
  }
  
  public int getChampionPoints()
  {
    return this.saveFile.championPoints;
  }
  
  public int getFatigueLeft()
  {
    if (this.saveFile.power > 0) {
      return 20000;
    }
    return this.saveFile.fatigueSecsLeft;
  }
  
  public void decreaseFatigue()
  {
    if (this.saveFile.power <= 0)
    {
      this.saveFile.decreaseFatigue();
      this.lastDecreasedFatigue = System.currentTimeMillis();
    }
  }
  
  public void setFatigue(int fatigueToAdd)
  {
    if (this.saveFile.power <= 0)
    {
      int toset = this.saveFile.hardSetFatigueSecs(fatigueToAdd);
      this.saveFile.setFatigueSecs(toset, this.saveFile.lastFatigue);
    }
  }
  
  public long getVersion()
  {
    return this.saveFile.version;
  }
  
  public void mute(boolean mute, String reason, long expiry)
  {
    this.saveFile.setMuted(mute, reason, expiry);
    this.saveFile.mutesReceived = 0;
  }
  
  public boolean isMute()
  {
    if (this.saveFile.isMute())
    {
      if (this.saveFile.muteexpiry < System.currentTimeMillis())
      {
        mute(false, "", 0L);
        return false;
      }
      return true;
    }
    return false;
  }
  
  public long getMoney()
  {
    if (Servers.localServer.id == Servers.loginServer.id) {
      return this.saveFile.money;
    }
    LoginServerWebConnection lsw = new LoginServerWebConnection();
    return lsw.getMoney(this);
  }
  
  public boolean addMoney(long moneyToAdd)
    throws IOException
  {
    if (Servers.localServer.id == Servers.loginServer.id)
    {
      this.saveFile.setMoney(this.saveFile.money + moneyToAdd);
      return true;
    }
    LoginServerWebConnection lsw = new LoginServerWebConnection();
    if (lsw.addMoney(this, getName(), moneyToAdd, DateFormat.getInstance().format(new Date()).replace(" ", "") + Server.rand
      .nextInt(100) + Servers.localServer.name))
    {
      this.saveFile.setMoney(this.saveFile.money + moneyToAdd);
      return true;
    }
    return false;
  }
  
  public boolean chargeMoney(long moneyToCharge)
    throws IOException
  {
    if (Servers.localServer.id == Servers.loginServer.id)
    {
      if (this.saveFile.money - moneyToCharge < 0L) {
        return false;
      }
      this.saveFile.setMoney(this.saveFile.money - moneyToCharge);
      return true;
    }
    LoginServerWebConnection lsw = new LoginServerWebConnection();
    long newBalance = lsw.chargeMoney(this.name, moneyToCharge);
    if (newBalance >= 0L)
    {
      this.saveFile.setMoney(newBalance);
      return true;
    }
    logger.warning(getName() + " failed to withdraw money from the bank for moneyToCharge: " + moneyToCharge);
    getCommunicator().sendAlertServerMessage("Failed to contact the bank or the balance did not match. Please try later.");
    
    return false;
  }
  
  public boolean setSex(byte sex)
  {
    try
    {
      this.status.setSex(sex);
      this.saveFile.setSex(sex);
      setVisible(false);
      if (hasLink()) {
        getCommunicator().sendChangeModelName(getWurmId(), getModelName());
      }
      setVisible(true);
    }
    catch (IOException iox)
    {
      return false;
    }
    return true;
  }
  
  public boolean isClimbing()
  {
    return this.saveFile.climbing;
  }
  
  public void setMoney(long newMoney)
    throws IOException
  {
    this.saveFile.setMoney(newMoney);
  }
  
  public boolean acceptsInvitations()
  {
    return this.acceptsInvitations;
  }
  
  private void pollStealAttack()
  {
    if ((this.secondsPlayed < 2.0F) || (this.secondsPlayed % 1000.0F == 0.0F)) {
      if (this.secondsPlayed > 999.0F)
      {
        if (this.maySteal)
        {
          checkMaySteal();
          if (!this.maySteal) {
            getCommunicator().sendAlertServerMessage("You may no longer steal things.");
          }
        }
        else
        {
          checkMaySteal();
          if (this.maySteal) {
            getCommunicator().sendAlertServerMessage("You now feel confident enough to steal things.");
          }
        }
        if (this.mayAttack)
        {
          checkMayAttack();
          if (!this.mayAttack) {
            getCommunicator().sendAlertServerMessage("You may no longer attack people.");
          }
        }
        else
        {
          checkMayAttack();
          if (this.mayAttack) {
            getCommunicator().sendSafeServerMessage("You now feel confident enough to attack other people.");
          }
        }
      }
      else
      {
        checkMaySteal();
        checkMayAttack();
      }
    }
  }
  
  public boolean pollAge()
  {
    return false;
  }
  
  public long getFace()
  {
    return this.saveFile.face;
  }
  
  public void setReputation(int reputation)
  {
    int oldrep = getReputation();
    if (getKingdomTemplateId() != 3)
    {
      if ((getPower() > 0) && (reputation < 0)) {
        return;
      }
      int diff = oldrep - reputation;
      if (diff > 0) {
        if (getCitizenVillage() != null) {
          getCitizenVillage().setVillageRep(getCitizenVillage().getVillageReputation() + diff);
        }
      }
      if (reputation < 65336)
      {
        if (!Servers.isThisAChaosServer())
        {
          if (((getCitizenVillage() == null) || (getCitizenVillage().getMayor().wurmId != getWurmId())) && 
            (!isKing()) && (oldrep > reputation)) {
            try
            {
              if (setKingdomId((byte)3))
              {
                getCommunicator().sendAlertServerMessage("You join the Horde of the Summoned.", (byte)2);
                logger.info(getName() + " joins HOTS as their reputation is " + reputation);
              }
            }
            catch (IOException iox)
            {
              logger.log(Level.WARNING, getName() + ":" + iox.getMessage(), iox);
            }
          }
          if ((oldrep > reputation) && (getKingdomTemplateId() != 3)) {
            getCommunicator().sendAlertServerMessage("Your reputation is decreasing deeply. It will take a very long time to recover. Eventually you should seek out the Horde of The Summoned.");
          }
        }
      }
      else
      {
        if ((oldrep >= 0) && (reputation < 0))
        {
          getCommunicator().sendAlertServerMessage("You are now an outlaw. Other players may now kill you on sight!", (byte)4);
          refreshVisible();
          sendAttitudeChange();
          if ((getCitizenVillage() != null) && (getCitizenVillage().getMayor().wurmId != getWurmId()) && 
            (!isKing())) {
            if (!Servers.localServer.isChallengeOrEpicServer()) {
              getCitizenVillage().removeCitizen(this);
            }
          }
        }
        else if ((reputation >= 0) && (oldrep < 0))
        {
          getCommunicator().sendSafeServerMessage("You are no longer considered an outlaw.", (byte)2);
          refreshVisible();
          sendAttitudeChange();
        }
        if ((oldrep >= -100) && (reputation < -100)) {
          getCommunicator().sendAlertServerMessage("Kingdom guards will now kill you on sight!", (byte)4);
        }
        if ((oldrep >= 65356) && (reputation < 65356)) {
          getCommunicator().sendAlertServerMessage("You are very close to joining the Horde of the Summoned!", (byte)4);
        }
      }
    }
    this.saveFile.setReputation(reputation);
    refreshAttitudes();
  }
  
  public int getReputation()
  {
    return this.saveFile.getReputation();
  }
  
  public void addTitle(Titles.Title title)
  {
    if (this.saveFile.addTitle(title))
    {
      getCommunicator().sendNormalServerMessage("You have just received the title '" + title
        .getName(isNotFemale()) + "'!", (byte)2);
      if (getTitles().length >= 15) {
        maybeTriggerAchievement(572, true);
      }
      if (getTitles().length >= 30) {
        maybeTriggerAchievement(579, true);
      }
      if (getTitles().length >= 60) {
        maybeTriggerAchievement(591, true);
      }
    }
  }
  
  public void removeTitle(Titles.Title title)
  {
    if (this.saveFile.removeTitle(title)) {
      getCommunicator().sendNormalServerMessage("You have just lost the title '" + title
        .getName(isNotFemale()) + "'!", (byte)2);
    }
    if (getTitle() == title) {
      setTitle(null);
    }
  }
  
  public Titles.Title[] getTitles()
  {
    return this.saveFile.getTitles();
  }
  
  public void setSecondTitle(@Nullable Titles.Title title)
  {
    this.saveFile.secondTitle = title;
    if (title != null) {
      if (title.isRoyalTitle()) {
        setFinestAppointment();
      }
    }
    if ((!isDead()) && (getCurrentTile() != null))
    {
      getCurrentTile().makeInvisible(this);
      try
      {
        getCurrentTile().makeVisible(this);
      }
      catch (NoSuchCreatureException|NoSuchPlayerException localNoSuchCreatureException) {}
    }
    getCommunicator().sendOwnTitles();
    if ((title != null) || (getTitle() != null)) {
      getCommunicator().sendNormalServerMessage("Your title is now " + getTitleString() + ".");
    } else {
      getCommunicator().sendNormalServerMessage("You will use no title for now.");
    }
  }
  
  public void setTitle(@Nullable Titles.Title title)
  {
    this.saveFile.title = title;
    if (title != null) {
      if (title.isRoyalTitle()) {
        setFinestAppointment();
      }
    }
    if (!Features.Feature.COMPOUND_TITLES.isEnabled())
    {
      if ((!isDead()) && (getCurrentTile() != null))
      {
        getCurrentTile().makeInvisible(this);
        try
        {
          getCurrentTile().makeVisible(this);
        }
        catch (NoSuchCreatureException localNoSuchCreatureException) {}catch (NoSuchPlayerException localNoSuchPlayerException) {}
      }
      getCommunicator().sendOwnTitles();
      if (title != null)
      {
        if (title.isRoyalTitle()) {
          getCommunicator().sendNormalServerMessage("Your title is now " + this.saveFile.kingdomtitle + ".");
        } else {
          getCommunicator().sendNormalServerMessage("Your title is now " + this.saveFile.title.getName(isNotFemale()) + ".");
        }
      }
      else {
        getCommunicator().sendNormalServerMessage("You will use no title for now.");
      }
    }
  }
  
  public Titles.Title getSecondTitle()
  {
    return this.saveFile.secondTitle;
  }
  
  public Titles.Title getTitle()
  {
    return this.saveFile.title;
  }
  
  public String getKingdomTitle()
  {
    return this.saveFile.kingdomtitle;
  }
  
  public void setFinestAppointment()
  {
    if ((this.saveFile.appointments != 0L) || (isAppointed())) {
      if (isKing())
      {
        this.saveFile.kingdomtitle = King.getRulerTitle(getSex() == 0, getKingdomId());
      }
      else
      {
        Appointments apps = King.getCurrentAppointments(getKingdomId());
        if (apps != null)
        {
          Appointment app = apps.getFinestAppointment(this.saveFile.appointments, getWurmId());
          if (app != null) {
            if (app.getType() == 1) {
              this.saveFile.kingdomtitle = ("Order of the " + app.getNameForGender(getSex()));
            } else {
              this.saveFile.kingdomtitle = app.getNameForGender(getSex());
            }
          }
        }
      }
    }
  }
  
  public boolean hasPet()
  {
    return this.saveFile.pet != -10L;
  }
  
  public boolean mayMute()
  {
    return (getPower() >= 2) || (this.saveFile.mayMute);
  }
  
  public void setPet(long petId)
  {
    this.saveFile.setPet(petId);
  }
  
  public Creature getPet()
  {
    if (this.saveFile.pet > 0L) {
      return Server.getInstance().getCreatureOrNull(this.saveFile.pet);
    }
    return null;
  }
  
  public long getAlcoholAddiction()
  {
    return this.saveFile.alcoholAddiction;
  }
  
  public long getNicotineAddiction()
  {
    return this.saveFile.nicotineAddiction;
  }
  
  public float getAlcohol()
  {
    return this.saveFile.alcohol;
  }
  
  public float getNicotine()
  {
    return this.saveFile.nicotine;
  }
  
  public void setAlcohol(float newAlcohol)
  {
    this.saveFile.setAlcohol(newAlcohol);
  }
  
  public void setNicotine(float newNicotine)
  {
    this.saveFile.setNicotine(newNicotine);
  }
  
  public boolean hasSleepBonus()
  {
    return this.saveFile.hasSleepBonus();
  }
  
  public boolean isFrozen()
  {
    return this.frozen;
  }
  
  public void toggleFrozen(Creature freezer)
  {
    if (this.frozen)
    {
      getMovementScheme().setFreezeMod(false);
      getCommunicator().sendSafeServerMessage(freezer.getName() + " gives you your movement back.");
    }
    else
    {
      getMovementScheme().setFreezeMod(true);
      getCommunicator().sendAlertServerMessage(freezer.getName() + " has paralyzed you!");
    }
    this.frozen = (!this.frozen);
  }
  
  public void setFrozen(boolean _frozen)
  {
    if (this.frozen != _frozen)
    {
      if (this.frozen)
      {
        getMovementScheme().setFreezeMod(false);
        getCommunicator().sendSafeServerMessage("You may now move again.");
      }
      else
      {
        if (Constants.devmode) {
          getCommunicator().sendAlertServerMessage("You've been frozen!");
        }
        getMovementScheme().setFreezeMod(true);
      }
      this.frozen = _frozen;
    }
  }
  
  protected void setLastVehicle(long _lastvehicle, byte _seatType)
  {
    this.saveFile.setLastVehicle(_lastvehicle);
  }
  
  public Seat getSeat()
  {
    if (this.vehicle > -10L)
    {
      Vehicle vehic = Vehicles.getVehicleForId(this.vehicle);
      if (vehic != null) {
        for (int x = 0; x < vehic.seats.length; x++) {
          if (vehic.seats[x].occupant == getWurmId()) {
            return vehic.seats[x];
          }
        }
      }
    }
    return null;
  }
  
  public void disembark(boolean teleport)
  {
    disembark(teleport, -1, -1);
  }
  
  public boolean isOnFire()
  {
    return this.isOnFire;
  }
  
  public byte getFireRadius()
  {
    if (isOnFire()) {
      return 10;
    }
    return 0;
  }
  
  public double getVillageSkillModifier()
  {
    return this.villageSkillModifier;
  }
  
  public void setVillageSkillModifier(double newModifier)
  {
    this.villageSkillModifier = newModifier;
  }
  
  public void checkLantern()
  {
    if ((getVisionArea() != null) && (getVisionArea().isInitialized())) {
      if (getPower() >= 2)
      {
        if ((this.gmLight) && (!this.isLit))
        {
          getCurrentTile().setHasLightSource(this, (byte)this.colorr, (byte)this.colorg, (byte)this.colorb, (byte)40);
          this.isLit = true;
          getCommunicator().sendNormalServerMessage("Someone blesses you with a personal light.");
        }
        else if ((!this.gmLight) && (this.isLit))
        {
          getCurrentTile().setHasLightSource(this, null);
          this.isLit = false;
          getCommunicator().sendNormalServerMessage("Your light leaves you.");
        }
      }
      else if ((!this.isLit) && 
        (isVisible()) && (
        (getPlayingTime() < 86400000L) || ((Servers.localServer.entryServer) && (isPlayerAssistant()))))
      {
        if (getBestLightsource() == null)
        {
          getCurrentTile().setHasLightSource(this, (byte)80, (byte)80, (byte)80, (byte)5);
          this.isLit = true;
          if (!isUndead()) {
            getCommunicator().sendNormalServerMessage("The deities bless you with a faint light.");
          }
        }
      }
      else if (((Servers.localServer.entryServer) && (isPlayerAssistant()) ? 1 : 0) == 0) {
        if ((this.isLit) && (
          (getPlayingTime() > 86400000L) || (!isVisible())))
        {
          getCurrentTile().setHasLightSource(this, null);
          this.isLit = false;
          if (!isUndead()) {
            getCommunicator().sendNormalServerMessage("The light leaves you.");
          }
        }
      }
    }
  }
  
  public void sendLantern(VirtualZone watcher)
  {
    if ((this.isLit) && (isVisibleTo(watcher.getWatcher()))) {
      watcher.sendAttachCreatureEffect(this, (byte)0, (byte)80, (byte)80, (byte)80, (byte)1);
    }
  }
  
  public void setTheftWarned(boolean warned)
  {
    this.saveFile.setTheftwarned(warned);
  }
  
  public void checkTheftWarnQuestion()
  {
    if (!this.saveFile.isTheftWarned)
    {
      if ((this.question != null) && (this.question.getType() == 49)) {
        return;
      }
      DropInfoQuestion quest = new DropInfoQuestion(this, "Theft prevention notification", "A word of warning!", -1L);
      quest.sendQuestion();
    }
  }
  
  public void checkChallengeWarnQuestion()
  {
    if (Servers.localServer.isChallengeServer()) {
      if (!hasFlag(27))
      {
        ChallengeInfoQuestion quest = new ChallengeInfoQuestion(this);
        quest.sendQuestion();
      }
    }
  }
  
  public void setChallengeWarned(boolean warned)
  {
    setFlag(27, true);
  }
  
  public void addEnemyPresense()
  {
    if (this.enemyPresenceCounter <= 0)
    {
      this.enemyPresenceCounter = 1;
      if ((Servers.localServer.PVPSERVER) && (Servers.localServer.isChallengeOrEpicServer()) && (!Servers.localServer.HOMESERVER)) {
        if ((getCurrentVillage() != null) && (this.currentTile != null) && (this.currentTile.getKingdom() == getKingdomId()))
        {
          setSecondsToLogout(3600);
          return;
        }
      }
      setSecondsToLogout(300);
    }
  }
  
  public void removeEnemyPresense()
  {
    if (this.enemyPresenceCounter > minEnemyPresence)
    {
      getCommunicator().sendSafeServerMessage("The feeling of insecurity and anger leaves you and you can focus better now.", (byte)4);
      
      getCommunicator().sendRemoveSpellEffect(SpellEffectsEnum.ENEMY);
    }
    this.enemyPresenceCounter = 0;
    if ((Servers.localServer.PVPSERVER) && (Servers.localServer.isChallengeOrEpicServer()) && (!Servers.localServer.HOMESERVER))
    {
      this.secondsToLogout = 0;
      this.secondsToLogout = getSecondsToLogout();
    }
  }
  
  public int getEnemyPresense()
  {
    return this.enemyPresenceCounter;
  }
  
  public boolean hasNoReimbursement()
  {
    return this.saveFile.noReimbursementLeft;
  }
  
  public boolean isDeathProtected()
  {
    return this.saveFile.deathProtected;
  }
  
  public void setDeathProtected(boolean _deathProtected)
  {
    this.saveFile.setDeathProtected(_deathProtected);
    if (_deathProtected) {
      getCommunicator().sendAddStatusEffect(SpellEffectsEnum.DEATH_PROTECTION, Integer.MAX_VALUE);
    } else {
      getCommunicator().sendRemoveSpellEffect(SpellEffectsEnum.DEATH_PROTECTION);
    }
  }
  
  public void setLastChangedVillage(long _lastChanged)
  {
    this.saveFile.setLastChangedVillage(_lastChanged);
  }
  
  public long mayChangeVillageInMillis()
  {
    return Math.max(this.saveFile.lastChangedVillage - System.currentTimeMillis() + 86400000L, 0L);
  }
  
  public void saveFightMode(byte _mode)
  {
    this.saveFile.saveFightMode(_mode);
  }
  
  public void loadAffinities()
  {
    Affinity[] affs = Affinities.getAffinities(getWurmId());
    if (affs.length > 0) {
      for (int x = 0; x < affs.length; x++) {
        try
        {
          Skill s = this.skills.getSkill(affs[x].skillNumber);
          s.affinity = affs[x].number;
        }
        catch (NoSuchSkillException localNoSuchSkillException) {}
      }
    }
  }
  
  public void increaseAffinity(int skillnumber, int aValue)
  {
    int lValue = aValue;
    Affinity[] affs = Affinities.getAffinities(getWurmId());
    if (affs.length > 0) {
      for (int x = 0; x < affs.length; x++) {
        if (affs[x].skillNumber == skillnumber) {
          lValue += affs[x].number;
        }
      }
    }
    Affinities.setAffinity(getWurmId(), skillnumber, lValue, false);
  }
  
  public void decreaseAffinity(int skillnumber, int value)
  {
    Affinities.decreaseAffinity(getWurmId(), skillnumber, value);
  }
  
  public boolean isOnHostileHomeServer()
  {
    return (!Servers.localServer.entryServer) && (Servers.localServer.HOMESERVER) && 
      (getKingdomId() != Servers.localServer.KINGDOM);
  }
  
  public void checkAffinity()
  {
    if (this.saveFile.eligibleForAffinity())
    {
      Affinity[] affs = Affinities.getAffinities(getWurmId());
      if (affs.length == 0) {
        Affinities.setAffinity(getWurmId(), SkillSystem.getRandomSkillNum(), 1, false);
      }
    }
  }
  
  public boolean isAspiringKing()
  {
    try
    {
      if (getCurrentAction().getNumber() == 353) {
        return true;
      }
    }
    catch (NoSuchActionException localNoSuchActionException) {}
    return false;
  }
  
  public boolean isSparring(Creature aOpponent)
  {
    if ((this.sparrers != null) && (this.sparrers.contains(aOpponent))) {
      return true;
    }
    return false;
  }
  
  public boolean isDuelling(Creature aOpponent)
  {
    if ((this.duellers != null) && (this.duellers.contains(aOpponent))) {
      return true;
    }
    return false;
  }
  
  public void addDuellist(Creature aOpponent)
  {
    if (this.duellers == null) {
      this.duellers = new HashSet();
    }
    this.duellers.add(aOpponent);
  }
  
  public void addSparrer(Creature aOpponent)
  {
    if (this.sparrers == null) {
      this.sparrers = new HashSet();
    }
    this.sparrers.add(aOpponent);
  }
  
  public void removeDuellist(Creature aOpponent)
  {
    if (this.duellers != null)
    {
      this.duellers.remove(aOpponent);
      getCommunicator().sendNormalServerMessage("You may no longer duel " + aOpponent.getName() + " safely.");
    }
  }
  
  public void removeSparrer(Creature aOpponent)
  {
    if (this.sparrers != null)
    {
      this.sparrers.remove(aOpponent);
      getCommunicator().sendNormalServerMessage("You may no longer spar with " + aOpponent.getName() + " safely.");
    }
  }
  
  public boolean isDuelOrSpar(Creature aOpponent)
  {
    if (aOpponent == this) {
      return true;
    }
    if (isInOwnDuelRing()) {
      return true;
    }
    return (isSparring(aOpponent)) || (isDuelling(aOpponent));
  }
  
  public void setChangedTileCounter()
  {
    getMovementScheme().touchFreeMoveCounter();
  }
  
  public int getTutorialLevel()
  {
    return this.saveFile.tutorialLevel;
  }
  
  public void setTutorialLevel(int newLevel)
  {
    this.saveFile.setTutorialLevel(newLevel);
    if ((newLevel >= 12) && (newLevel != 9999)) {
      achievement(141);
    }
  }
  
  public void missionFinished(boolean reward, boolean sendpopup)
  {
    OldMission m = OldMission.getMission(getTutorialLevel(), getKingdomId());
    if (reward) {
      if (m != null) {
        if (m.itemTemplateRewardId > 0)
        {
          Item inventory = getInventory();
          try
          {
            for (int x = 0; x < m.itemTemplateRewardNumbers; x++)
            {
              Item i = createItem(m.itemTemplateRewardId, m.itemTemplateRewardQL);
              if (m.setNewbieItemByte) {
                i.setAuxData((byte)1);
              }
              getCommunicator().sendSafeServerMessage("You receive " + i.getNameWithGenus() + ".");
              inventory.insertItem(i);
            }
          }
          catch (Exception ex)
          {
            logger.log(Level.WARNING, getName() + " failed reward " + m.itemTemplateRewardNumbers + ", " + m.itemTemplateRewardId + " for " + 
              getTutorialLevel(), ex);
          }
        }
      }
    }
    setTutorialLevel(getTutorialLevel() + 1);
    if (sendpopup) {
      if ((m != null) && (m.doneString.length() > 0))
      {
        SimplePopup popup = new SimplePopup(this, "Mission accomplished!", m.doneString);
        popup.sendQuestion();
      }
      else
      {
        SimplePopup popup = new SimplePopup(this, "Mission accomplished!", "You should go see if there are more instructions for you.");
        
        popup.sendQuestion();
      }
    }
  }
  
  public String getCurrentMissionInstruction()
  {
    if (skippedTutorial()) {
      return "You skipped the tutorial and have to reactivate it.";
    }
    if (getTutorialLevel() == 9999) {
      return "You have finished the tutorial.";
    }
    OldMission m = OldMission.getMission(getTutorialLevel(), getKingdomId());
    if (m != null)
    {
      StringBuilder toRet = new StringBuilder();
      toRet.append(m.title);
      toRet.append(": ");
      toRet.append(m.missionDescription);
      if (m.missionDescription2 != null) {
        toRet.append(m.missionDescription2);
      }
      if (m.missionDescription3 != null) {
        toRet.append(m.missionDescription3);
      }
      return toRet.toString();
    }
    return "";
  }
  
  public boolean isNearCave()
  {
    if (getVisionArea() != null) {
      return getVisionArea().isNearCave();
    }
    return !isOnSurface();
  }
  
  public byte getFarwalkerSeconds()
  {
    return this.farwalkerSeconds;
  }
  
  public void setFarwalkerSeconds(byte seconds)
  {
    this.farwalkerSeconds = seconds;
    if (isPlayer()) {
      if (this.farwalkerSeconds > 0) {
        getCommunicator().sendAddSpellEffect(SpellEffectsEnum.FARWALKER, this.farwalkerSeconds, this.farwalkerSeconds);
      } else {
        getCommunicator().sendRemoveSpellEffect(SpellEffectsEnum.FARWALKER);
      }
    }
  }
  
  public void activeFarwalkerAmulet(Item amulet)
  {
    if (getVehicle() == -10L)
    {
      setFarwalkerSeconds((byte)45);
      getMovementScheme().setFarwalkerMoveMod(true);
      getStatus().sendStateString();
      getCommunicator().sendNormalServerMessage("Your legs tingle and you feel unstoppable.");
      if (amulet.getTemplateId() == 527)
      {
        Server.getInstance().broadCastAction(getName() + " fiddles with a strange amulet.", this, 5);
        amulet.setQualityLevel(amulet.getQualityLevel() - 1.0F);
      }
      else
      {
        Server.getInstance().broadCastAction(getName() + " uses the " + amulet.getName() + ".", this, 5);
      }
    }
    else
    {
      getCommunicator().sendNormalServerMessage("Nothing happens.");
    }
  }
  
  public void activePotion(Item potion)
  {
    if (potion.getTemplateId() == 5)
    {
      if (this.CRBonus < 5)
      {
        this.CRBonusCounter = 10;
        this.CRBonus = ((byte)Math.min(5, this.CRBonus + 2));
        getCommunicator().sendNormalServerMessage("You feel nimble and sharp like a blade.");
        Server.getInstance().broadCastAction(getName() + " drinks a strange green-glowing potion.", this, 5);
        Items.destroyItem(potion.getWurmId());
        getCommunicator().sendAddSpellEffect(SpellEffectsEnum.CR_BONUS, this.CRBonusCounter, this.CRBonus);
        achievement(85);
      }
      else
      {
        getCommunicator().sendNormalServerMessage("You are already bursting with energy.");
      }
    }
    else if (potion.getTemplateId() == 836)
    {
      int skillNum = SkillSystem.getRandomSkillNum();
      Affinity[] affs = Affinities.getAffinities(getWurmId());
      boolean found = false;
      while (!found)
      {
        boolean hasAffinity = false;
        for (Affinity affinity : affs) {
          if (affinity.getSkillNumber() == skillNum)
          {
            hasAffinity = true;
            if (affinity.getNumber() >= 5) {
              break;
            }
            Affinities.setAffinity(getWurmId(), skillNum, affinity.getNumber() + 1, false);
            String skillString = SkillSystem.getNameFor(skillNum);
            found = true;
            getCommunicator().sendSafeServerMessage("Aahh! You feel better at " + skillString + "!");
            break;
          }
        }
        if ((!found) && (!hasAffinity))
        {
          Affinities.setAffinity(getWurmId(), skillNum, 1, false);
          getCommunicator().sendSafeServerMessage("Aahh! You feel better somehow.. more skillful!");
          found = true;
        }
        skillNum = SkillSystem.getRandomSkillNum();
      }
    }
    else if (potion.getTemplateId() == 834)
    {
      if (getVehicle() != -10L)
      {
        getCommunicator().sendNormalServerMessage("You suddenly notice a huge label on the potion with a crossed over boat and a crossed over horse, indicating that it will have no effect while mounted.");
        
        return;
      }
      SpellEffects effs = getSpellEffects();
      if (effs == null) {
        effs = createSpellEffects();
      }
      SpellEffect eff = effs.getSpellEffect((byte)72);
      if (eff == null)
      {
        getCommunicator().sendNormalServerMessage("You change appearance!");
        Server.getInstance().broadCastAction(getName() + " drinks a yellow potion.", this, 5);
        Items.destroyItem(potion.getWurmId());
        eff = new SpellEffect(getWurmId(), (byte)72, 100.0F, (int)(20.0F * potion.getQualityLevel()), (byte)9, (byte)0, true);
        
        effs.addSpellEffect(eff);
        int num = Server.rand.nextInt(12);
        try
        {
          switch (num)
          {
          case 0: 
            if (this.status.getSex() == 0) {
              setModelName("model.creature.humanoid.human.player.zombie.male");
            } else if (this.status.getSex() == 1) {
              setModelName("model.creature.humanoid.human.player.zombie.female");
            }
            break;
          case 1: 
            setModelName(CreatureTemplateFactory.getInstance().getTemplate(88).getModelName());
            break;
          case 2: 
            setModelName(CreatureTemplateFactory.getInstance().getTemplate(87).getModelName());
            break;
          case 3: 
            setModelName(CreatureTemplateFactory.getInstance().getTemplate(12).getModelName());
            break;
          case 4: 
            setModelName(CreatureTemplateFactory.getInstance().getTemplate(10).getModelName());
            break;
          case 5: 
            setModelName(CreatureTemplateFactory.getInstance().getTemplate(75).getModelName());
            break;
          case 6: 
            setModelName(CreatureTemplateFactory.getInstance().getTemplate(55).getModelName());
            break;
          case 7: 
            setModelName(CreatureTemplateFactory.getInstance().getTemplate(11).getModelName());
            break;
          case 8: 
            setModelName(CreatureTemplateFactory.getInstance().getTemplate(23).getModelName());
            break;
          case 9: 
            setModelName(ItemTemplateFactory.getInstance().getTemplate(814).getModelName());
            break;
          case 10: 
            setModelName(ItemTemplateFactory.getInstance().getTemplate(190).getModelName());
            break;
          case 11: 
            setModelName(ItemTemplateFactory.getInstance().getTemplate(177).getModelName());
            break;
          default: 
            logger.warning("rand.nextInt(12) returned an unexepected value: " + num);
          }
        }
        catch (Exception ex)
        {
          logger.log(Level.WARNING, ex.getMessage(), ex);
        }
      }
      else
      {
        getCommunicator().sendNormalServerMessage("You have already changed appearance.");
      }
    }
  }
  
  public void setModelName(String newModelName)
  {
    boolean wasVisible = isVisible();
    if (isVisible()) {
      setVisible(false);
    }
    this.saveFile.setModelName(newModelName);
    if (hasLink()) {
      getCommunicator().sendChangeModelName(getWurmId(), getModelName());
    }
    if ((getPower() <= 0) || (wasVisible)) {
      setVisible(true);
    }
  }
  
  public final String getModelName()
  {
    if (!this.saveFile.getModelName().equals("Human")) {
      return this.saveFile.getModelName();
    }
    StringBuilder s = new StringBuilder();
    s.append(this.template.getModelName());
    if (this.status.getSex() == 0) {
      s.append(".male");
    }
    if (this.status.getSex() == 1) {
      s.append(".female");
    }
    if (getKingdomId() != 0)
    {
      s.append('.');
      Kingdom kingdomt = Kingdoms.getKingdom(getKingdomId());
      if (kingdomt.getTemplate() != getKingdomId()) {
        s.append(Kingdoms.getSuffixFor(kingdomt.getTemplate()));
      }
      s.append(Kingdoms.getSuffixFor(getKingdomId()));
      if (this.status.disease > 0) {
        s.append("diseased");
      }
    }
    else if (this.status.disease > 0)
    {
      s.append(".diseased");
    }
    return s.toString();
  }
  
  public byte getCRCounterBonus()
  {
    return this.CRBonus;
  }
  
  public String toString()
  {
    return "Player [id: " + getWurmId() + ", name: " + this.name + ']';
  }
  
  public long getAppointments()
  {
    return this.saveFile.appointments;
  }
  
  public void addAppointment(int aid)
  {
    this.saveFile.addAppointment(aid);
    setFinestAppointment();
  }
  
  public void addAppointment(Appointment a, Creature performer)
  {
    Communicator pc = performer.getCommunicator();
    Communicator c = getCommunicator();
    King k = King.getKing(getKingdomId());
    Appointments apps = King.getCurrentAppointments(getKingdomId());
    if ((a == null) || (performer == null)) {
      return;
    }
    if (apps == null)
    {
      pc.sendNormalServerMessage("You have no titles to give out!");
      return;
    }
    if (!King.isKing(performer.getWurmId(), getKingdomId()))
    {
      pc.sendNormalServerMessage("Only the ruler of " + getName() + "'s kingdom may appoint them!");
      return;
    }
    if (!acceptsInvitations())
    {
      pc.sendNormalServerMessage(getName() + " needs to type /invitations first.");
      return;
    }
    switch (a.getType())
    {
    case 2: 
      if (apps.officials[(a.getId() - 1500)] == getWurmId())
      {
        pc.sendNormalServerMessage(getName() + " is already appointed to the office of" + a.getNameForGender(getSex()) + ".");
        return;
      }
      if (apps.isOfficeSet(a.getId()))
      {
        pc.sendNormalServerMessage("The office as " + a.getNameForGender((byte)0) + " has already been set this week.");
        return;
      }
      if (apps.officials[(a.getId() - 1500)] > 0L)
      {
        Player op = Players.getInstance().getPlayerOrNull(apps.officials[(a.getId() - 1500)]);
        if (op == null)
        {
          PlayerInfo pinf = PlayerInfoFactory.getPlayerInfoWithWurmId(apps.officials[(a.getId() - 1500)]);
          if (pinf != null) {
            pc.sendNormalServerMessage("Unable to notify " + pinf.getName() + " of their removal from office.");
          }
        }
        else
        {
          op.getCommunicator().sendNormalServerMessage("You are hereby notified that you have been removed from the office of " + a
            .getNameForGender(op.getSex()) + ".", (byte)2);
        }
      }
      apps.setOfficial(a.getId(), getWurmId());
      achievement(323);
      k.addAppointment(a);
      break;
    case 1: 
      if (hasAppointment(a.getId()))
      {
        pc.sendNormalServerMessage(getName() + " has already been appointed to the order of " + a.getNameForGender(getSex()) + ".");
        return;
      }
      if (apps.getAvailOrdersForId(a.getId()) < 1)
      {
        pc.sendNormalServerMessage("You may not award the " + a.getNameForGender(getSex()) + " to more people right now.");
        return;
      }
      achievement(325);
      addAppointment(a.getId());
      apps.useOrder(a.getId());
      break;
    case 0: 
      if (hasAppointment(a.getId()))
      {
        pc.sendNormalServerMessage(getName() + " has already been awarded the title of " + a.getNameForGender(getSex()) + ".");
        return;
      }
      if (apps.getAvailTitlesForId(a.getId()) < 1)
      {
        pc.sendNormalServerMessage("You may not award the " + a.getNameForGender(getSex()) + " to more people right now.");
        return;
      }
      addAppointment(a.getId());
      achievement(324);
      apps.useTitle(a.getId());
      break;
    default: 
      pc.sendNormalServerMessage("That appointment is invalid.");
      return;
    }
    k.addAppointment(a);
    pc.sendNormalServerMessage("You award the " + a.getNameForGender(getSex()) + " of " + Kingdoms.getNameFor(getKingdomId()) + " to " + 
      getName() + ".", (byte)2);
    c.sendNormalServerMessage("You have graciously been awarded the " + a.getNameForGender(getSex()) + " of " + 
      Kingdoms.getNameFor(getKingdomId()) + " by " + k.getRulerTitle() + " " + performer.getName() + "!", (byte)2);
    
    HistoryManager.addHistory(getName(), "receives the " + a.getNameForGender(getSex()) + " of " + 
      Kingdoms.getNameFor(getKingdomId()) + " from " + k.getRulerTitle() + " " + performer.getName() + ".");
  }
  
  public void removeAppointment(int aid)
  {
    this.saveFile.removeAppointment(aid);
    setFinestAppointment();
  }
  
  public boolean hasAppointment(int aid)
  {
    return this.saveFile.hasAppointment(aid);
  }
  
  public String getAppointmentTitles()
  {
    Appointments apps = King.getCurrentAppointments(getKingdomId());
    if (apps != null)
    {
      StringBuilder buf = new StringBuilder();
      String titles = apps.getOffices(getWurmId(), getSex() == 0);
      if (titles.length() > 0)
      {
        buf.append(getName());
        buf.append(" is ");
        buf.append(titles);
        buf.append(" of ");
        buf.append(Kingdoms.getNameFor(getKingdomId()));
        buf.append(". ");
      }
      if (this.saveFile.appointments != 0L)
      {
        titles = apps.getTitles(this.saveFile.appointments, getSex() == 0);
        if (titles.length() > 0)
        {
          buf.append(getName());
          buf.append(" is ");
          buf.append(titles);
          buf.append(". ");
        }
        titles = apps.getOrders(this.saveFile.appointments, getSex() == 0);
        if (titles.length() > 0)
        {
          buf.append(getName());
          buf.append(" has received the ");
          buf.append(titles);
          buf.append(". ");
        }
      }
      return buf.toString();
    }
    return "";
  }
  
  public String getAnnounceString()
  {
    StringBuilder buf = new StringBuilder();
    buf.append(getName());
    if (isKing())
    {
      buf.append(", ");
      buf.append(King.getRulerTitle(getSex() == 0, getKingdomId()));
    }
    buf.append(" of ");
    buf.append(Kingdoms.getNameFor(getKingdomId()));
    if ((this.saveFile.appointments != 0L) || (isAppointed()))
    {
      Appointments apps = King.getCurrentAppointments(getKingdomId());
      if (apps != null)
      {
        buf.append(", ");
        String titles = apps.getOffices(getWurmId(), getSex() == 0);
        boolean added = false;
        if (titles.length() > 0)
        {
          buf.append(titles);
          added = true;
        }
        titles = apps.getTitles(this.saveFile.appointments, getSex() == 0);
        if (titles.length() > 0)
        {
          if (added) {
            buf.append(", ");
          }
          buf.append(titles);
          added = true;
        }
        titles = apps.getOrders(this.saveFile.appointments, getSex() == 0);
        if (titles.length() > 0)
        {
          if (added) {
            buf.append(", ");
          }
          buf.append("recipient of the ");
          buf.append(titles);
        }
      }
    }
    buf.append(". ");
    return buf.toString();
  }
  
  public boolean isKing()
  {
    return King.isKing(getWurmId(), getKingdomId());
  }
  
  public void clearRoyalty()
  {
    this.saveFile.clearAppointments();
  }
  
  public void sendPopup(String title, String message)
  {
    SimplePopup popup = new SimplePopup(this, title, message);
    popup.sendQuestion();
  }
  
  public boolean isAppointed()
  {
    Appointments apps = King.getCurrentAppointments(getKingdomId());
    if (apps != null) {
      return apps.isAppointed(getWurmId());
    }
    return false;
  }
  
  public int getPushCounter()
  {
    return this.pushCounter;
  }
  
  public void setPushCounter(int val)
  {
    this.pushCounter = val;
  }
  
  public int getMaxNumActions()
  {
    return this.maxNumActions;
  }
  
  public boolean isPlayerAssistant()
  {
    return this.saveFile.isPlayerAssistant();
  }
  
  public boolean mayAppointPlayerAssistant()
  {
    return (this.saveFile.mayAppointPlayerAssistant()) || (getPower() >= 1);
  }
  
  public void setPlayerAssistant(boolean assistant)
  {
    if (assistant) {
      addTitle(Titles.Title.PA);
    } else {
      removeTitle(Titles.Title.PA);
    }
    this.saveFile.setIsPlayerAssistant(assistant);
  }
  
  public void setMayAppointPlayerAssistant(boolean assistant)
  {
    this.saveFile.setMayAppointPlayerAssistant(assistant);
  }
  
  public boolean seesPlayerAssistantWindow()
  {
    if (((!Servers.localServer.HOMESERVER) && (!Servers.localServer.EPIC)) || ((!isOnHostileHomeServer()) || (Servers.localServer.isChallengeServer()))) {
      return this.saveFile.seesPlayerAssistantWindow();
    }
    return false;
  }
  
  public boolean maySeeGVHelpWindow()
  {
    return ((isPlayerAssistant()) || (mayMute()) || (getPower() > 0)) && (!Server.getInstance().isPS());
  }
  
  public boolean seesGVHelpWindow()
  {
    return (maySeeGVHelpWindow()) && (!hasFlag(45));
  }
  
  public final void setLastTaggedTerr(byte newKingdom)
  {
    this.saveFile.setLastTaggedTerr(newKingdom);
  }
  
  public boolean mustChangeTerritory()
  {
    if ((Servers.localServer.isChallengeOrEpicServer()) && (isChampion())) {
      if (getDeity() != null) {
        if (System.currentTimeMillis() - this.saveFile.lastMovedBetweenKingdom > 259200000L) {
          return true;
        }
      }
    }
    return false;
  }
  
  public byte getLastTaggedKingdom()
  {
    return this.saveFile.lastTaggedKindom;
  }
  
  public boolean togglePlayerAssistantWindow(boolean seeWindow)
  {
    if (this.saveFile.togglePlayerAssistantWindow(seeWindow))
    {
      Players.getInstance().sendPAWindow(this);
      return true;
    }
    Players.getInstance().partPAChannel(this);
    return false;
  }
  
  public boolean toggleGVHelpWindow(boolean seeWindow)
  {
    if ((maySeeGVHelpWindow()) && (seeWindow))
    {
      Players.getInstance().sendGVHelpWindow(this);
      setFlag(45, false);
      return true;
    }
    setFlag(45, true);
    return false;
  }
  
  public int getMeditateX()
  {
    return this.lastMeditateX;
  }
  
  public int getMeditateY()
  {
    return this.lastMeditateY;
  }
  
  public void setMeditateX(int tilex)
  {
    this.lastMeditateX = tilex;
  }
  
  public void setMeditateY(int tiley)
  {
    this.lastMeditateY = tiley;
  }
  
  public Cultist getCultist()
  {
    return Cultist.getCultist(getWurmId());
  }
  
  public void addLink(Creature creature)
  {
    if (this.links == null) {
      this.links = new HashMap();
    }
    this.links.put(Long.valueOf(creature.getWurmId()), creature);
    getCommunicator().sendNormalServerMessage(creature
      .getName() + " links with your faith. You may now use " + creature.getHisHerItsString() + " favor to cast spells while " + creature
      .getHeSheItString() + " is within about 4 tiles.");
  }
  
  public void removeLink(long wurmid)
  {
    if (this.links != null) {
      this.links.remove(Long.valueOf(wurmid));
    }
  }
  
  public void pollActions()
  {
    if (!this.loggedout) {
      if (this.actions.poll(this)) {
        if (isFighting()) {
          setFighting();
        }
      }
    }
  }
  
  public int getNumLinks()
  {
    if (this.links != null) {
      return getLinks().length;
    }
    return 0;
  }
  
  public void clearLinks()
  {
    if (this.links != null)
    {
      for (Creature c : this.links.values()) {
        c.setLinkedTo(-10L, false);
      }
      this.links.clear();
      getCommunicator().sendRemoveSpellEffect(SpellEffectsEnum.LINKS);
    }
  }
  
  public void setLinkedTo(long wid, boolean linkback)
  {
    if ((this.linkedTo != -10L) && (this.linkedTo != wid)) {
      try
      {
        Creature c = Server.getInstance().getCreature(this.linkedTo);
        getCommunicator().sendNormalServerMessage("You are no longer linked to " + c.getName() + ".");
        c.getCommunicator().sendNormalServerMessage(getName() + " is no longer linked to you" + ".");
        if (linkback) {
          c.removeLink(getWurmId());
        }
      }
      catch (NoSuchCreatureException nsc)
      {
        getCommunicator().sendNormalServerMessage("You are no longer linked.");
        this.linkedTo = -10L;
      }
      catch (NoSuchPlayerException nsp)
      {
        getCommunicator().sendNormalServerMessage("You are no longer linked.");
        this.linkedTo = -10L;
      }
    }
    if ((wid != -10L) && (this.linkedTo != wid)) {
      try
      {
        Creature c = Server.getInstance().getCreature(wid);
        getCommunicator().sendNormalServerMessage("You link your faith with " + c
          .getName() + " and " + c.getHeSheItString() + " may now use your favor to cast spells while you're within about 4 tiles.");
        if (linkback) {
          c.addLink(this);
        }
        this.linkedTo = wid;
      }
      catch (NoSuchCreatureException nsc)
      {
        getCommunicator().sendNormalServerMessage("You fail to link.");
        this.linkedTo = -10L;
      }
      catch (NoSuchPlayerException nsp)
      {
        getCommunicator().sendNormalServerMessage("You fail to link.");
        this.linkedTo = -10L;
      }
    } else {
      this.linkedTo = wid;
    }
    getStatus().sendStateString();
  }
  
  public void disableLink()
  {
    setLinkedTo(-10L, true);
  }
  
  public Creature[] getLinks()
  {
    if ((this.links != null) && (this.links.size() > 0))
    {
      Set<Creature> toadd = new HashSet();
      for (Creature c : this.links.values()) {
        if (isWithinDistanceTo(c, 20.0F)) {
          toadd.add(c);
        }
      }
      if (toadd.size() == 0) {
        return emptyCreatures;
      }
      return (Creature[])toadd.toArray(new Creature[toadd.size()]);
    }
    return emptyCreatures;
  }
  
  public void sendRemovePhantasms()
  {
    if (this.phantasms != null)
    {
      for (Long c : this.phantasms) {
        getCommunicator().sendDeleteCreature(c.longValue());
      }
      this.phantasms.clear();
    }
  }
  
  public boolean isMissionairy()
  {
    return this.saveFile.priestType == 0;
  }
  
  public long getLastChangedPriestType()
  {
    return this.saveFile.lastChangedPriestType;
  }
  
  public void setPriestType(byte type)
  {
    this.saveFile.setNewPriestType(type, System.currentTimeMillis());
  }
  
  public long getLastChangedJoat()
  {
    return this.saveFile.lastChangedJoat;
  }
  
  public void resetJoat()
  {
    this.saveFile.setChangedJoat();
  }
  
  public Team getTeam()
  {
    return this.team;
  }
  
  public void setTeam(Team newTeam, boolean sendRemove)
  {
    if (newTeam == null)
    {
      this.mayInviteTeam = true;
      if (this.team != null)
      {
        this.team.creaturePartedTeam(this, sendRemove);
        
        getCommunicator().sendNormalServerMessage("You have been removed from the team.");
      }
    }
    else
    {
      if (this.team != null) {
        this.team.creaturePartedTeam(this, sendRemove);
      }
      newTeam.creatureJoinedTeam(this);
    }
    this.team = newTeam;
  }
  
  public boolean isTeamLeader()
  {
    if (this.team == null) {
      return false;
    }
    return this.team.isTeamLeader(this);
  }
  
  public boolean mayInviteTeam()
  {
    return this.mayInviteTeam;
  }
  
  public void setMayInviteTeam(boolean mayInvite)
  {
    this.mayInviteTeam = mayInvite;
  }
  
  public void chatted()
  {
    this.lastChatted = System.currentTimeMillis();
  }
  
  public boolean isActiveInChat()
  {
    return System.currentTimeMillis() - this.lastChatted < 300000L;
  }
  
  public void chattedLocal()
  {
    this.lastChattedLocal = System.currentTimeMillis();
  }
  
  public boolean isActiveInLocalChat()
  {
    return System.currentTimeMillis() - this.lastChattedLocal < 300000L;
  }
  
  public boolean hasFreeTransfer()
  {
    return this.saveFile.hasFreeTransfer;
  }
  
  public boolean hasSkillGain()
  {
    return this.saveFile.hasSkillGain;
  }
  
  public boolean setHasSkillGain(boolean hasSkillGain)
  {
    getSkills().hasSkillGain = hasSkillGain;
    return this.saveFile.setHasSkillGain(hasSkillGain);
  }
  
  private final void setMissionDeathEffects()
  {
    MissionPerformer mp = MissionPerformed.getMissionPerformer(getWurmId());
    if (mp != null)
    {
      MissionPerformed[] perfs = mp.getAllMissionsPerformed();
      for (int x = 0; x < perfs.length; x++) {
        if ((!perfs[x].isInactivated()) && (!perfs[x].isCompleted()) && (!perfs[x].isFailed()) && 
          (perfs[x].isStarted()))
        {
          Mission mission = perfs[x].getMission();
          if (mission != null) {
            if (mission.isFailOnDeath()) {
              perfs[x].setState(-1.0F, getWurmId());
            }
          }
        }
      }
    }
  }
  
  public void setDraggedItem(@Nullable Item dragged)
  {
    if (dragged == null) {
      this.lastStoppedDragging = System.currentTimeMillis();
    }
    this.movementScheme.setDraggedItem(dragged);
  }
  
  public void setLastKingdom()
  {
    this.lastKingdom = getKingdomId();
  }
  
  public long getChampTimeStamp()
  {
    return this.saveFile.championTimeStamp;
  }
  
  public void becomeChamp()
  {
    Deity deity = getDeity();
    String deityName = "deity";
    if (deity != null) {
      deityName = deity.name;
    }
    try
    {
      if (!isPriest())
      {
        setPriest(true);
        PlayerJournal.sendTierUnlock(this, (JournalTier)PlayerJournal.getAllTiers().get(Byte.valueOf((byte)10)));
      }
      setFaith(99.99F);
      setFavor(99.99F);
      setChangedDeity();
      setRealDeath((byte)3);
      getSaveFile().setChampionTimeStamp();
      Skill bodyStrength = null;
      Skill stamina = null;
      Skill bodyControl = null;
      Skill mindlogical = null;
      Skill mindspeed = null;
      Skill soulstrength = null;
      Skill souldepth = null;
      Skill prayer = null;
      Skill exorcism = null;
      Skill channeling = null;
      try
      {
        prayer = this.skills.getSkill(10066);
        prayer.setKnowledge(Math.max(prayer.getKnowledge(), Math.min(80.0D, prayer.getKnowledge() + 50.0D)), false);
      }
      catch (NoSuchSkillException nss)
      {
        this.skills.learn(10066, 50.0F);
      }
      try
      {
        channeling = this.skills.getSkill(10067);
        getSaveFile().setChampChanneling((float)channeling.getKnowledge());
        channeling.setKnowledge(
          Math.max(channeling.getKnowledge(), 
          Math.max(channeling.getKnowledge(), Math.min(80.0D, channeling.getKnowledge() + 50.0D))), false);
      }
      catch (NoSuchSkillException nss)
      {
        this.skills.learn(10067, 50.0F);
      }
      try
      {
        exorcism = this.skills.getSkill(10068);
        exorcism.setKnowledge(Math.max(exorcism.getKnowledge(), Math.min(80.0D, exorcism.getKnowledge() + 50.0D)), false);
      }
      catch (NoSuchSkillException nss)
      {
        this.skills.learn(10068, 50.0F);
      }
      try
      {
        bodyStrength = this.skills.getSkill(102);
        bodyStrength.setKnowledge(bodyStrength.getKnowledge() + 5.0D, false);
      }
      catch (NoSuchSkillException nss)
      {
        this.skills.learn(102, 30.0F);
      }
      try
      {
        stamina = this.skills.getSkill(103);
        stamina.setKnowledge(stamina.getKnowledge() + 5.0D, false);
      }
      catch (NoSuchSkillException nss)
      {
        this.skills.learn(103, 30.0F);
      }
      try
      {
        bodyControl = this.skills.getSkill(104);
        bodyControl.setKnowledge(bodyControl.getKnowledge() + 5.0D, false);
      }
      catch (NoSuchSkillException nss)
      {
        this.skills.learn(104, 30.0F);
      }
      try
      {
        mindlogical = this.skills.getSkill(100);
        mindlogical.setKnowledge(mindlogical.getKnowledge() + 5.0D, false);
      }
      catch (NoSuchSkillException nss)
      {
        this.skills.learn(100, 30.0F);
      }
      try
      {
        mindspeed = this.skills.getSkill(101);
        mindspeed.setKnowledge(mindspeed.getKnowledge() + 5.0D, false);
      }
      catch (NoSuchSkillException nss)
      {
        this.skills.learn(101, 30.0F);
      }
      try
      {
        soulstrength = this.skills.getSkill(105);
        soulstrength.setKnowledge(soulstrength.getKnowledge() + 5.0D, false);
      }
      catch (NoSuchSkillException nss)
      {
        this.skills.learn(105, 30.0F);
      }
      try
      {
        souldepth = this.skills.getSkill(106);
        souldepth.setKnowledge(souldepth.getKnowledge() + 5.0D, false);
      }
      catch (NoSuchSkillException nss)
      {
        this.skills.learn(106, 30.0F);
      }
      getCommunicator().sendNormalServerMessage("You have now become a Champion of " + deityName + "!");
      Server.getInstance().broadCastAlert(getName() + " is now a Champion of " + deityName + "!", false);
      HistoryManager.addHistory(getName(), "is now a Champion of " + deityName);
      if (this.saveFile.lastTaggedKindom == 0) {
        setLastTaggedTerr(getKingdomId());
      } else {
        setLastTaggedTerr((byte)0);
      }
      checkInitialTitles();
    }
    catch (IOException iox)
    {
      logger.log(Level.WARNING, getName() + ":" + iox.getMessage(), iox);
    }
  }
  
  public long getLastChangedCluster()
  {
    return this.saveFile.lastChangedCluster;
  }
  
  public void setLastChangedCluster()
  {
    this.saveFile.lastChangedCluster = System.currentTimeMillis();
  }
  
  public void revertChamp()
  {
    try
    {
      Deity deity = getDeity();
      String deityName = "deity";
      if (deity != null) {
        deityName = deity.name;
      }
      setFaith(50.0F);
      setFavor(50.0F);
      
      setRealDeath((byte)0);
      this.saveFile.switchChamp();
      this.saveFile.setChampionPoints((short)0);
      getCommunicator().sendRemoveSpellEffect(SpellEffectsEnum.FARWALKER);
      this.saveFile.setChampionTimeStamp();
      Skill bodyStrength = null;
      Skill stamina = null;
      Skill bodyControl = null;
      Skill mindlogical = null;
      Skill mindspeed = null;
      Skill soulstrength = null;
      Skill souldepth = null;
      Skill prayer = null;
      Skill exorcism = null;
      Skill channeling = null;
      try
      {
        prayer = this.skills.getSkill(10066);
        prayer.setKnowledge(Math.max(10.0D, prayer.getKnowledge() - 50.0D), false, true);
      }
      catch (NoSuchSkillException nss)
      {
        this.skills.learn(10066, 10.0F);
      }
      try
      {
        channeling = this.skills.getSkill(10067);
        channeling.setKnowledge(Math.max(this.saveFile.champChanneling, channeling.getKnowledge() - 50.0D), false, true);
      }
      catch (NoSuchSkillException nss)
      {
        this.skills.learn(10067, 10.0F);
      }
      try
      {
        exorcism = this.skills.getSkill(10068);
        exorcism.setKnowledge(Math.max(10.0D, exorcism.getKnowledge() - 50.0D), false, true);
      }
      catch (NoSuchSkillException nss)
      {
        this.skills.learn(10068, 10.0F);
      }
      try
      {
        bodyStrength = this.skills.getSkill(102);
        bodyStrength.setKnowledge(bodyStrength.getKnowledge() - 6.0D, false, true);
      }
      catch (NoSuchSkillException nss)
      {
        this.skills.learn(102, 20.0F);
      }
      try
      {
        stamina = this.skills.getSkill(103);
        stamina.setKnowledge(stamina.getKnowledge() - 6.0D, false, true);
      }
      catch (NoSuchSkillException nss)
      {
        this.skills.learn(103, 20.0F);
      }
      try
      {
        bodyControl = this.skills.getSkill(104);
        bodyControl.setKnowledge(bodyControl.getKnowledge() - 6.0D, false, true);
      }
      catch (NoSuchSkillException nss)
      {
        this.skills.learn(104, 20.0F);
      }
      try
      {
        mindlogical = this.skills.getSkill(100);
        mindlogical.setKnowledge(mindlogical.getKnowledge() - 6.0D, false, true);
      }
      catch (NoSuchSkillException nss)
      {
        this.skills.learn(100, 20.0F);
      }
      try
      {
        mindspeed = this.skills.getSkill(101);
        mindspeed.setKnowledge(mindspeed.getKnowledge() - 6.0D, false, true);
      }
      catch (NoSuchSkillException nss)
      {
        this.skills.learn(101, 20.0F);
      }
      try
      {
        soulstrength = this.skills.getSkill(105);
        soulstrength.setKnowledge(soulstrength.getKnowledge() - 6.0D, false, true);
      }
      catch (NoSuchSkillException nss)
      {
        this.skills.learn(105, 20.0F);
      }
      try
      {
        souldepth = this.skills.getSkill(106);
        souldepth.setKnowledge(souldepth.getKnowledge() - 6.0D, false, true);
      }
      catch (NoSuchSkillException nss)
      {
        this.skills.learn(106, 20.0F);
      }
      getCommunicator().sendNormalServerMessage("You are no longer a Champion of " + deityName + "!");
      Server.getInstance().broadCastAlert(getName() + " is no longer a Champion of " + deityName + "!", false);
      HistoryManager.addHistory(getName(), "is no longer a Champion of " + deityName);
      addTitle(Titles.Title.Champ_Previous);
    }
    catch (IOException iox)
    {
      logger.log(Level.WARNING, getName() + ":" + iox.getMessage(), iox);
    }
  }
  
  public void setVotedKing(boolean voted)
  {
    this.saveFile.setVotedKing(voted);
  }
  
  public boolean hasVotedKing()
  {
    return this.saveFile.votedKing;
  }
  
  public boolean isCaredFor()
  {
    return Creatures.getInstance().isCreatureProtected(getWurmId());
  }
  
  public long getCareTakerId()
  {
    return -10L;
  }
  
  public int getNumberOfPossibleCreatureTakenCareOf()
  {
    if (!isPaying()) {
      return 1;
    }
    return (int)(1.0D + getAnimalHusbandrySkillValue() / 10.0D);
  }
  
  public final void sendDeityEffectBonuses()
  {
    if (!Servers.localServer.PVPSERVER)
    {
      if (getDeity() != null)
      {
        if (Effectuator.getDeityWithStaminaRegain() == getDeity().number) {
          sendAddDeityEffectBonus(3);
        }
        if (Effectuator.getDeityWithCombatRating() == getDeity().number) {
          sendAddDeityEffectBonus(2);
        }
        if (Effectuator.getDeityWithSpeedBonus() == getDeity().number) {
          sendAddDeityEffectBonus(1);
        }
        if (Effectuator.getDeityWithFavorGain() == getDeity().number) {
          sendAddDeityEffectBonus(4);
        }
      }
    }
    else
    {
      if (Effectuator.getKingdomTemplateWithStaminaRegain() == getKingdomTemplateId()) {
        sendAddDeityEffectBonus(3);
      }
      if (Effectuator.getKingdomTemplateWithCombatRating() == getKingdomTemplateId()) {
        sendAddDeityEffectBonus(2);
      }
      if (Effectuator.getKingdomTemplateWithSpeedBonus() == getKingdomTemplateId()) {
        sendAddDeityEffectBonus(1);
      }
      if (Effectuator.getKingdomTemplateWithFavorGain() == getKingdomTemplateId()) {
        sendAddDeityEffectBonus(4);
      }
    }
  }
  
  public final void sendAddDeityEffectBonus(int effectNumber)
  {
    switch (effectNumber)
    {
    case 3: 
      getCommunicator().sendAddSpellEffect(SpellEffectsEnum.DEITY_STAMINAGAIN, 100000, 0.1F);
      
      setHasSpiritStamina(true);
      break;
    case 4: 
      getCommunicator().sendAddSpellEffect(SpellEffectsEnum.DEITY_FAVORGAIN, 100000, 0.05F);
      
      setHasSpiritFavorgain(true);
      break;
    case 1: 
      getCommunicator().sendAddSpellEffect(SpellEffectsEnum.DEITY_MOVEBONUS, 100000, 0.05F);
      
      getMovementScheme().setHasSpiritSpeed(true);
      break;
    case 2: 
      getCommunicator().sendAddSpellEffect(SpellEffectsEnum.DEITY_CRBONUS, 100000, 1.0F);
      
      setHasSpiritFervor(true);
      break;
    }
  }
  
  public final void setHasSpiritFervor(boolean hasSpiritFervor)
  {
    getCombatHandler().setHasSpiritFervor(hasSpiritFervor);
  }
  
  public final void setHasSpiritFavorgain(boolean hasFavorGain)
  {
    this.hasSpiritFavorgain = hasFavorGain;
  }
  
  public final void sendRemoveDeityEffectBonus(int effectNumber)
  {
    switch (effectNumber)
    {
    case 3: 
      getCommunicator().sendRemoveSpellEffect(SpellEffectsEnum.DEITY_STAMINAGAIN);
      setHasSpiritStamina(false);
      break;
    case 4: 
      getCommunicator().sendRemoveSpellEffect(SpellEffectsEnum.DEITY_FAVORGAIN);
      setHasSpiritFavorgain(false);
      break;
    case 1: 
      getCommunicator().sendRemoveSpellEffect(SpellEffectsEnum.DEITY_MOVEBONUS);
      getMovementScheme().setHasSpiritSpeed(false);
      break;
    case 2: 
      getCommunicator().sendRemoveSpellEffect(SpellEffectsEnum.DEITY_CRBONUS);
      setHasSpiritFervor(false);
      break;
    }
  }
  
  public final void setEigcClientId(String eigcIdUsed)
  {
    this.eigcId = eigcIdUsed;
  }
  
  public final String getEigcId()
  {
    return this.eigcId;
  }
  
  public boolean mayUseLastGasp()
  {
    if ((getPositionZ() + getAltOffZ() <= 0.0F) && (getStatus().getStamina() < 5000)) {
      return this.saveFile.mayUseLastGasp();
    }
    return false;
  }
  
  public void useLastGasp()
  {
    try
    {
      setClimbing(true);
      this.saveFile.useLastGasp();
      getStatus().modifyStamina(500.0F);
      getCommunicator().sendNormalServerMessage("You draw on your last inner resources and may drag yourself out of the water.");
    }
    catch (IOException iox)
    {
      logger.log(Level.WARNING, getName() + ":" + iox.getMessage());
    }
  }
  
  public final boolean isUsingLastGasp()
  {
    if (getPositionZ() + getAltOffZ() > 10.0F) {
      return false;
    }
    return this.saveFile.isUsingLastGasp();
  }
  
  public final void setKickedOffBoat(boolean kicked)
  {
    this.kickedOffBoat = kicked;
  }
  
  public final boolean wasKickedOffBoat()
  {
    return this.kickedOffBoat;
  }
  
  public void disableKosPopups(int villageId)
  {
    this.kosPopups.add(Integer.valueOf(villageId));
  }
  
  public boolean acceptsKosPopups(int villageId)
  {
    return this.kosPopups.contains(Integer.valueOf(villageId));
  }
  
  public boolean hasFingerEffect()
  {
    return this.hasFingerEffect;
  }
  
  public void setHasFingerEffect(boolean eff)
  {
    this.hasFingerEffect = eff;
    sendHasFingerEffect();
  }
  
  public void sendHasFingerEffect()
  {
    if (this.hasFingerEffect) {
      getCommunicator().sendAddSpellEffect(SpellEffectsEnum.FINGER_FO_EFFECT, 100000, 100.0F);
    } else {
      getCommunicator().sendRemoveSpellEffect(SpellEffectsEnum.FINGER_FO_EFFECT);
    }
  }
  
  public boolean hasFingerOfFoBonus()
  {
    Player[] players = Players.getInstance().getPlayers();
    for (Player p : players) {
      if (p.isWithinDistanceTo(this, 50.0F)) {
        if ((p.hasFingerEffect()) && (p.isFriendlyKingdom(getKingdomId()))) {
          return true;
        }
      }
    }
    return false;
  }
  
  public boolean hasCrownEffect()
  {
    return this.hasCrownEffect;
  }
  
  public void setHasCrownEffect(boolean eff)
  {
    this.hasCrownEffect = eff;
  }
  
  public void sendHasCrownEffect()
  {
    if (this.crownInfluence == 4) {
      getCommunicator().sendAddSpellEffect(SpellEffectsEnum.CROWN_MAGRANON_EFFECT, 100000, 100.0F);
    } else if (this.crownInfluence == 0) {
      getCommunicator().sendRemoveSpellEffect(SpellEffectsEnum.CROWN_MAGRANON_EFFECT);
    }
  }
  
  public void setCrownInfluence(int influence)
  {
    boolean send = false;
    if ((this.crownInfluence == 0) && (influence == 4)) {
      send = true;
    }
    if ((this.crownInfluence > 0) && (influence == 0)) {
      send = true;
    }
    this.crownInfluence = influence;
    if (send) {
      sendHasCrownEffect();
    }
  }
  
  public boolean hasCrownInfluence()
  {
    return this.crownInfluence > 0;
  }
  
  private final void spreadCrownInfluence()
  {
    if (hasCrownEffect())
    {
      Player[] players = Players.getInstance().getPlayers();
      for (Player p : players) {
        if (p.isWithinDistanceTo(this, 50.0F)) {
          if (p.isFriendlyKingdom(getKingdomId())) {
            p.setCrownInfluence(4);
          }
        }
      }
    }
  }
  
  public final void setMarkedByOrb(boolean marked)
  {
    this.markedByOrb = marked;
    sendMarkedByOrb();
  }
  
  public final void sendMarkedByOrb()
  {
    if (this.markedByOrb) {
      getCommunicator().sendAddSpellEffect(SpellEffectsEnum.ORB_DOOM_EFFECT, 20, 100.0F);
    } else {
      getCommunicator().sendRemoveSpellEffect(SpellEffectsEnum.ORB_DOOM_EFFECT);
    }
  }
  
  public final boolean isMarkedByOrb()
  {
    return this.markedByOrb;
  }
  
  public int getEpicServerId()
  {
    return this.saveFile.epicServerId;
  }
  
  public byte getEpicServerKingdom()
  {
    return this.saveFile.epicKingdom;
  }
  
  public short getHotaWins()
  {
    return this.saveFile.getHotaWins();
  }
  
  public void setHotaWins(short wins)
  {
    this.saveFile.setHotaWins(wins);
    getCommunicator().sendNormalServerMessage("You now have " + wins + " wins in the Hunt of the Ancients!");
  }
  
  public final int getTeleportCounter()
  {
    return this.teleportCounter;
  }
  
  public final void setTeleportCounter(int counter)
  {
    this.teleportCounter = counter;
  }
  
  public void achievement(int achievementId)
  {
    Achievements.triggerAchievement(getWurmId(), achievementId);
  }
  
  public void achievement(int achievementId, int counterModifier)
  {
    Achievements.triggerAchievement(getWurmId(), achievementId, counterModifier);
  }
  
  protected void addTileMovedDragging()
  {
    if (this.tilesMovedDragging++ > 25)
    {
      this.tilesMovedDragging = 0;
      achievement(65);
    }
  }
  
  protected void addTileMovedRiding()
  {
    if (this.tilesMovedRiding++ > 4000)
    {
      this.tilesMovedRiding = 0;
      if (getSecondsPlayed() <= 3600.0F) {
        achievement(75);
      }
      achievement(76);
    }
  }
  
  protected void addTileMoved()
  {
    if (this.tilesMoved++ > 250)
    {
      this.tilesMoved = 0;
      achievement(62);
    }
    if (!hasFlag(42))
    {
      if (this.tilesMoved == 2) {
        getCommunicator().sendPlonk((short)101);
      }
      if (this.tilesMoved == 5) {
        getCommunicator().sendPlonk((short)102);
      }
      if (this.tilesMoved == 8) {
        getCommunicator().sendPlonk((short)103);
      }
      if (this.tilesMoved == 11) {
        getCommunicator().sendPlonk((short)104);
      }
      if (this.tilesMoved == 14)
      {
        getCommunicator().sendPlonk((short)105);
        setFlag(42, true);
        if (ConchQuestion.isThisAdventureServer()) {
          try
          {
            this.conchticker = 60;
            Item conch = ItemFactory.createItem(1024, 80.0F + Server.rand.nextFloat() * 20.0F, "");
            getInventory().insertItem(conch, true);
          }
          catch (Exception localException) {}
        }
      }
    }
    if (getDraggedItem() != null) {
      addTileMovedDragging();
    }
    if (getVehicle() != -10L)
    {
      Vehicle vehic = Vehicles.getVehicleForId(getVehicle());
      if (vehic != null)
      {
        try
        {
          Item item = Items.getItem(getVehicle());
          if (item.getTemplateId() == 539) {
            if (vehic.getPilotId() == getWurmId()) {
              addTileMovedDriving();
            } else {
              addTileMovedPassenger();
            }
          }
        }
        catch (NoSuchItemException localNoSuchItemException) {}
        if ((vehic.isCreature()) && 
          (vehic.getPilotId() == getWurmId())) {
          addTileMovedRiding();
        }
      }
    }
  }
  
  protected void addTileMovedDriving()
  {
    if (this.tilesMovedDriving++ > 4000)
    {
      this.tilesMovedDriving = 0;
      achievement(73);
    }
  }
  
  protected void addTileMovedPassenger()
  {
    if (this.tilesMovedPassenger++ > 4000)
    {
      this.tilesMovedPassenger = 0;
      achievement(74);
    }
  }
  
  public void playPersonalSound(String soundName)
  {
    float offsetx = 4.0F * Server.rand.nextFloat();
    
    float offsety = 4.0F * Server.rand.nextFloat();
    try
    {
      Sound so = new Sound(soundName, getPosX() - 2.0F + offsetx, getPosY() - 2.0F + offsety, Zones.calculateHeight(
        getPosX(), getPosY(), isOnSurface()) + 1.0F, 1.0F, 1.0F, 5.0F);
      getCommunicator().sendSound(so);
    }
    catch (NoSuchZoneException localNoSuchZoneException) {}
    if (soundName.equals("sound.fx.drumroll")) {
      getCommunicator().sendRarityEvent();
    }
  }
  
  public int getKarma()
  {
    return this.saveFile.getKarma();
  }
  
  public void setKarma(int newKarma)
  {
    this.saveFile.setKarma(newKarma);
    sendKarma();
  }
  
  public void modifyKarma(int points)
  {
    if ((points > 0) || (getPower() <= 1))
    {
      this.saveFile.setKarma(points + getKarma());
      sendKarma();
    }
  }
  
  public boolean fireTileLog()
  {
    return true;
  }
  
  public void sendActionControl(String actionString, boolean start, int timeLeft)
  {
    VolaTile playerCurrentTile = getCurrentTile();
    sendToLoggers("Action string " + actionString + ", starting=" + start + ", time left " + timeLeft);
    if (playerCurrentTile == null) {
      return;
    }
    playerCurrentTile.sendActionControl(this, actionString, start, timeLeft);
  }
  
  public void setBlood(byte blood)
  {
    getSaveFile().setBlood(blood);
  }
  
  public byte getBlood()
  {
    return getSaveFile().getBlood();
  }
  
  public boolean hasAnyAbility()
  {
    if (getSaveFile().abilities != 0L) {
      return true;
    }
    return false;
  }
  
  public boolean hasAbility(int abilityBit)
  {
    if ((getPower() >= 4) && (Servers.isThisATestServer())) {
      return true;
    }
    if (getSaveFile().abilities != 0L) {
      return getSaveFile().isAbilityBitSet(abilityBit);
    }
    return false;
  }
  
  public boolean hasFlag(int flagBit)
  {
    if (getSaveFile().flags != 0L) {
      return getSaveFile().isFlagSet(flagBit);
    }
    return false;
  }
  
  public int getAbilityTitleVal()
  {
    return getSaveFile().abilityTitle;
  }
  
  public final String getAbilityTitle()
  {
    if (getSaveFile().abilityTitle > -1) {
      return Abilities.getAbilityString(getSaveFile().abilityTitle) + " ";
    }
    return "";
  }
  
  public final void setAbilityTitle(int newTitle)
  {
    getSaveFile().setCurrentAbilityTitle(newTitle);
    refreshVisible();
    getCommunicator().sendSafeServerMessage("You will henceforth be known as the " + getAbilityTitle() + getName());
  }
  
  public void setFlag(int number, boolean value)
  {
    getSaveFile().setFlag(number, value);
  }
  
  public void setAbility(int number, boolean value)
  {
    getSaveFile().setAbility(number, value);
  }
  
  public void setTagItem(long itemId, String itemName)
  {
    this.taggedItemId = itemId;
    this.taggedItem = itemName;
  }
  
  public String getTaggedItemName()
  {
    return this.taggedItem;
  }
  
  public long getTaggedItemId()
  {
    return this.taggedItemId;
  }
  
  public boolean isKingdomChat()
  {
    if (isUndead()) {
      return false;
    }
    return !hasFlag(29);
  }
  
  public boolean isTradeChannel()
  {
    if (isUndead()) {
      return false;
    }
    return !hasFlag(31);
  }
  
  public boolean showKingdomStartMessage()
  {
    return !hasFlag(35);
  }
  
  public boolean showGlobalKingdomStartMessage()
  {
    return !hasFlag(36);
  }
  
  public boolean showTradeStartMessage()
  {
    return !hasFlag(37);
  }
  
  public boolean isVillageChatShowing()
  {
    return !hasFlag(38);
  }
  
  public boolean showVillageMessage()
  {
    return !hasFlag(39);
  }
  
  public boolean isAllianceChatShowing()
  {
    return !hasFlag(40);
  }
  
  public boolean showAllianceMessage()
  {
    return !hasFlag(41);
  }
  
  public boolean isGlobalChat()
  {
    if (Servers.localServer.isChallengeServer()) {
      return false;
    }
    return !hasFlag(30);
  }
  
  public void setScenarioKarma(int newKarma)
  {
    getSaveFile().setScenarioKarma(newKarma);
    sendScenarioKarma();
  }
  
  public int getScenarioKarma()
  {
    return getSaveFile().getScenarioKarma();
  }
  
  public boolean knowsKarmaSpell(int karmaSpellActionNum)
  {
    switch (karmaSpellActionNum)
    {
    case 629: 
      return Abilities.isWorgMaster(this);
    case 686: 
      return Abilities.isIncinerator(this);
    case 547: 
      return (Abilities.isCrone(this)) || (Abilities.isOccultist(this));
    case 548: 
      return (Abilities.isNorn(this)) || (Abilities.isEnchanter(this));
    case 549: 
      return (Abilities.isSorceror(this)) || (Abilities.isSorceress(this));
    case 551: 
      return (Abilities.isInquisitor(this)) || (Abilities.isHag(this));
    case 553: 
      return (Abilities.isValkyrie(this)) || (Abilities.isBerserker(this));
    case 554: 
      return (Abilities.isSiren(this)) || (Abilities.isSpellbinder(this));
    case 555: 
      return (Abilities.isWitchHunter(this)) || (Abilities.isSoothSayer(this));
    case 557: 
      return (Abilities.isMedium(this)) || (Abilities.isSummoner(this));
    case 561: 
    case 634: 
      return Abilities.isDruid(this);
    case 630: 
    case 631: 
      return (Abilities.isNecromancer(this)) || (Abilities.isWitch(this));
    case 550: 
    case 560: 
      return (Abilities.isEvocator(this)) || (Abilities.isFortuneTeller(this));
    case 552: 
    case 562: 
      return (Abilities.isConjurer(this)) || (Abilities.isMesmeriser(this));
    case 556: 
    case 558: 
      return (Abilities.isDiviner(this)) || (Abilities.isIllusionist(this));
    }
    return false;
  }
  
  public boolean allowIncomingPMs(String senderName, byte fromPower, long senderId, boolean aFriend, byte fromKingdom, int fromServer)
  {
    if (fromPower >= 2)
    {
      if (Servers.isThisATestServer()) {
        sendNormalServerMessage("Message from GM, so allowing.");
      }
      return true;
    }
    if (isIgnored(senderId))
    {
      if (Servers.isThisATestServer()) {
        if (isFriend(senderId)) {
          sendNormalServerMessage("Message from Friend (" + senderName + "), but you are ignoring them.");
        } else {
          sendNormalServerMessage("Message from " + senderName + ", but you are ignoring them.");
        }
      }
      return false;
    }
    if ((!hasFlag(4)) && ((aFriend) || (isFriend(senderId))))
    {
      if (Servers.isThisATestServer()) {
        sendNormalServerMessage("Message from Friend, so allowing.");
      }
      return true;
    }
    if (hasFlag(1))
    {
      if (Servers.isThisATestServer()) {
        sendNormalServerMessage("Message from " + senderName + ", but no PMs set, so disallowing.");
      }
      return false;
    }
    if ((!hasFlag(2)) && (fromKingdom != getKingdomId()))
    {
      if (Servers.isThisATestServer()) {
        sendNormalServerMessage("Message from " + senderName + ", wrong kingdom (theirs " + fromKingdom + " yours " + 
          getKingdomId() + "), so disallowing.");
      }
      return false;
    }
    if ((!hasFlag(3)) && (Servers.getLocalServerId() != fromServer))
    {
      if (Servers.isThisATestServer()) {
        sendNormalServerMessage("Message from " + senderName + ", but XServer NOT set, so disallowing.");
      }
      return false;
    }
    return true;
  }
  
  public boolean respondMGMTTab(String targetName, String optionalTargetNo)
  {
    String title = "CM";
    if (getPower() >= 2) {
      title = "GM";
    }
    String tno = optionalTargetNo.length() > 0 ? " " + optionalTargetNo : "";
    String msg = "Hello, " + title + " responding to your support call" + tno + ".";
    String tname = LoginHandler.raiseFirstLetter(targetName);
    if (getSaveFile().hasPMTarget(targetName)) {
      return true;
    }
    PlayerInfo pInfo = PlayerInfoFactory.createPlayerInfo(tname);
    if (pInfo != null) {
      try
      {
        pInfo.load();
        if ((pInfo != null) && (pInfo.currentServer == Servers.getLocalServerId()))
        {
          getSaveFile().addPMTarget(targetName, pInfo.wurmId);
          
          showPM(getName(), targetName, msg, false);
          
          Player p = Players.getInstance().getPlayer(targetName);
          if (!p.sendPM(getName(), getWurmId(), msg)) {
            showPMWarn(targetName, targetName + " is not currently available.");
          }
        }
      }
      catch (IOException e)
      {
        showPMWarn(targetName, targetName + " not found.");
      }
      catch (NoSuchPlayerException nspe)
      {
        showPMWarn(targetName, targetName + " not online.");
      }
    }
    return true;
  }
  
  public boolean respondGMTab(String targetName, String optionalTargetNo)
  {
    this.respondingAsGM = true;
    String title = "CM";
    if (getPower() >= 2) {
      title = "GM";
    }
    String tno = optionalTargetNo.length() > 0 ? " " + optionalTargetNo : "";
    sendPM(targetName, "Hello, " + title + " responding to your support call" + tno + ".", false, true);
    this.respondingAsGM = false;
    return true;
  }
  
  public void sendPM(String targetName, String _message, boolean _emote, boolean override)
  {
    String tname = LoginHandler.raiseFirstLetter(targetName);
    if (getSaveFile().hasPMTarget(targetName))
    {
      sendPM((byte)2, tname, getSaveFile().getPMTargetId(targetName), _message, _emote, override);
      return;
    }
    PlayerInfo pInfo = PlayerInfoFactory.createPlayerInfo(tname);
    if (pInfo != null) {
      try
      {
        pInfo.load();
        sendPM((byte)2, tname, pInfo.wurmId, _message, _emote, override);
        return;
      }
      catch (IOException localIOException) {}
    }
    Creature npc = Creatures.getInstance().getNpc(tname);
    if (npc != null)
    {
      Npc cret = (Npc)npc;
      cret.getChatManager().addChat(getName(), _message);
      
      showPM(getName(), LoginHandler.raiseFirstLetter(targetName), _message, _emote);
      return;
    }
    if (Servers.isThisLoginServer())
    {
      sendPM((byte)2, tname, -10L, _message, _emote, override);
      return;
    }
    WcGlobalPM wgi = new WcGlobalPM(WurmId.getNextWCCommandId(), (byte)0, getPseudoPower(), getWurmId(), getName(), getKingdomId(), 0, -10L, tname, false, _message, _emote, override);
    
    wgi.sendToLoginServer();
  }
  
  public void sendPM(byte reply, String targetName, long targetId, String _message, boolean _emote, boolean override)
  {
    if (targetId == -10L)
    {
      sendNormalServerMessage("Player not found with the name " + targetName + '.');
      return;
    }
    if (reply == 2)
    {
      if ((!override) && (isIgnored(targetId)))
      {
        sendNormalServerMessage("You are ignoring " + targetName + " and can not pm.");
        getSaveFile().removePMTarget(targetName);
        return;
      }
      getSaveFile().addPMTarget(targetName, targetId);
      
      showPM(getName(), targetName, _message, _emote);
      if (isAFK()) {
        showPMWarn(targetName, "You are AFK");
      }
      boolean myFriend = isFriend(targetId);
      
      PlayerState pState = PlayerInfoFactory.getPlayerState(targetId);
      if ((pState != null) && (pState.getServerId() == Servers.getLocalServerId()))
      {
        try
        {
          Player p = Players.getInstance().getPlayer(targetName);
          if (!p.sendPM(getPseudoPower(), getName(), getWurmId(), myFriend, _message, _emote, 
            getKingdomId(), Servers.getLocalServerId(), override)) {
            sendPM((byte)6, targetName, targetId, _message, _emote, override);
          } else if (p.isAFK()) {
            sendPM((byte)7, targetName, targetId, p.getAFKMessage(), true, override);
          }
        }
        catch (NoSuchPlayerException e)
        {
          sendPM((byte)6, targetName, targetId, _message, _emote, override);
        }
      }
      else if (Servers.isThisLoginServer())
      {
        PlayerInfo pInfo = PlayerInfoFactory.createPlayerInfo(targetName);
        if (pInfo != null)
        {
          WcGlobalPM wgi = new WcGlobalPM(WurmId.getNextWCCommandId(), (byte)3, getPseudoPower(), getWurmId(), getName(), getKingdomId(), pInfo.currentServer, targetId, targetName, myFriend, _message, _emote, override);
          
          wgi.sendToServer(pInfo.currentServer);
        }
      }
      else
      {
        WcGlobalPM wgi = new WcGlobalPM(WurmId.getNextWCCommandId(), (byte)3, getPseudoPower(), getWurmId(), getName(), getKingdomId(), 0, targetId, targetName, myFriend, _message, _emote, override);
        
        wgi.sendToLoginServer();
      }
    }
    else if (reply == 5)
    {
      showPMWarn(targetName, targetName + " is ignoring you. ");
      getSaveFile().removePMTarget(targetName);
    }
    else if (reply == 6)
    {
      showPMWarn(targetName, targetName + " is not currently available, please try again later. ");
      getSaveFile().removePMTarget(targetName);
    }
    else if (reply == 7)
    {
      showPM(targetName, targetName, _message, true);
    }
    else
    {
      sendNormalServerMessage("Unknown reply " + reply + ". ");
    }
  }
  
  public boolean sendPM(byte power, String senderName, long senderId, boolean aFriend, String _message, boolean _emote, byte kingdomId, int serverId, boolean override)
  {
    if (!hasLink()) {
      return false;
    }
    if (getSaveFile().hasPMTarget(senderName))
    {
      showPM(senderName, senderName, _message, _emote);
      return true;
    }
    if ((override) || (allowIncomingPMs(senderName, power, senderId, aFriend, kingdomId, serverId)))
    {
      showPM(senderName, senderName, _message, _emote);
      getSaveFile().addPMTarget(senderName, senderId);
      return true;
    }
    return false;
  }
  
  public boolean sendPM(String senderName, long senderId, String _message)
  {
    if (!hasLink()) {
      return false;
    }
    showPM(senderName, senderName, _message, false);
    
    getSaveFile().addPMTarget(senderName, senderId);
    return true;
  }
  
  public final void showPM(String senderName, String windowTitle, String _message, boolean _emote)
  {
    Message mess = new Message(this, (byte)3, "PM: " + windowTitle, (_emote ? "" : new StringBuilder().append("<").append(senderName).append("> ").toString()) + _message);
    
    mess.setReceiver(getWurmId());
    if (_emote)
    {
      mess.setColorR(228);
      mess.setColorG(244);
      mess.setColorB(138);
    }
    Server.getInstance().addMessage(mess);
  }
  
  public final void showPMWarn(String windowTitle, String warnMessage)
  {
    Message msg = new Message(this, (byte)3, "PM: " + windowTitle, "<System> " + warnMessage);
    
    msg.setColorR(255);
    msg.setColorG(155);
    msg.setColorB(155);
    msg.setReceiver(getWurmId());
    Server.getInstance().addMessage(msg);
  }
  
  private void sendNormalServerMessage(String message)
  {
    sendServerMessage(message, -1, -1, -1);
  }
  
  private void sendServerMessage(String message, int red, int green, int blue)
  {
    Message msg = new Message(this, (byte)17, ":Event", message, red, green, blue);
    
    msg.setReceiver(getWurmId());
    Server.getInstance().addMessage(msg);
  }
  
  public void closePM(String targetName)
  {
    getSaveFile().removePMTarget(targetName);
  }
  
  private byte getPseudoPower()
  {
    byte power = (byte)getPower();
    if (power >= 2) {
      return power;
    }
    if (this.respondingAsGM) {
      return 2;
    }
    return 0;
  }
  
  public float getFireResistance()
  {
    return (hasAbility(34)) || (hasAbility(35)) || (hasAbility(44)) ? 0.85F : 0.0F;
  }
  
  public float getColdResistance()
  {
    return (hasAbility(12)) || (hasAbility(31)) ? 0.85F : 0.0F;
  }
  
  public float getDiseaseResistance()
  {
    return (hasAbility(1)) || (hasAbility(15)) ? 0.85F : 0.0F;
  }
  
  public float getPhysicalResistance()
  {
    return (hasAbility(3)) || (hasAbility(16)) ? 0.1F : 0.0F;
  }
  
  public float getPierceResistance()
  {
    return (hasAbility(2)) || (hasAbility(13)) ? 0.85F : 0.0F;
  }
  
  public float getSlashResistance()
  {
    return (hasAbility(9)) || (hasAbility(27)) ? 0.85F : 0.0F;
  }
  
  public float getCrushResistance()
  {
    return (hasAbility(11)) || (hasAbility(30)) ? 0.85F : 0.0F;
  }
  
  public float getBiteResistance()
  {
    return (hasAbility(10)) || (hasAbility(29)) ? 0.85F : 0.0F;
  }
  
  public float getPoisonResistance()
  {
    return (hasAbility(7)) || (hasAbility(20)) ? 0.85F : 0.0F;
  }
  
  public float getWaterResistance()
  {
    return (hasAbility(6)) || (hasAbility(32)) ? 0.85F : 0.0F;
  }
  
  public float getAcidResistance()
  {
    return (hasAbility(8)) || (hasAbility(24)) ? 0.85F : 0.0F;
  }
  
  public float getInternalResistance()
  {
    return (hasAbility(33)) || (hasAbility(41)) || (hasAbility(42)) || (hasAbility(43)) ? 0.85F : 0.0F;
  }
  
  public float getFireVulnerability()
  {
    return (hasAbility(33)) || (hasAbility(41)) || (hasAbility(42)) || (hasAbility(43)) ? 1.1F : 0.0F;
  }
  
  public float getColdVulnerability()
  {
    return (hasAbility(8)) || (hasAbility(24)) ? 1.1F : 0.0F;
  }
  
  public float getDiseaseVulnerability()
  {
    return (hasAbility(6)) || (hasAbility(32)) ? 1.1F : 0.0F;
  }
  
  public float getPhysicalVulnerability()
  {
    return (hasAbility(7)) || (hasAbility(20)) ? 1.05F : 0.0F;
  }
  
  public float getPierceVulnerability()
  {
    return (hasAbility(10)) || (hasAbility(29)) ? 1.1F : 0.0F;
  }
  
  public float getSlashVulnerability()
  {
    return (hasAbility(11)) || (hasAbility(30)) ? 1.1F : 0.0F;
  }
  
  public float getCrushVulnerability()
  {
    return (hasAbility(9)) || (hasAbility(27)) ? 1.1F : 0.0F;
  }
  
  public float getBiteVulnerability()
  {
    return (hasAbility(2)) || (hasAbility(13)) || (hasAbility(44)) ? 1.1F : 0.0F;
  }
  
  public float getPoisonVulnerability()
  {
    return (hasAbility(3)) || (hasAbility(16)) ? 1.1F : 0.0F;
  }
  
  public float getWaterVulnerability()
  {
    return (hasAbility(1)) || (hasAbility(15)) ? 1.1F : 0.0F;
  }
  
  public float getAcidVulnerability()
  {
    return (hasAbility(12)) || (hasAbility(31)) ? 1.1F : 0.0F;
  }
  
  public float getInternalVulnerability()
  {
    return (hasAbility(34)) || (hasAbility(35)) ? 1.1F : 0.0F;
  }
  
  public boolean isSignedIn()
  {
    return getSaveFile().isSessionFlagSet(0);
  }
  
  public void setSignedIn(boolean _signedIn)
  {
    getSaveFile().setSessionFlag(0, _signedIn);
  }
  
  public boolean canSignIn()
  {
    if ((isPlayerAssistant()) || (mayMute())) {
      return true;
    }
    return false;
  }
  
  public boolean isAFK()
  {
    return getSaveFile().isSessionFlagSet(1);
  }
  
  public void setAFK(boolean _afk)
  {
    getSaveFile().setSessionFlag(1, _afk);
  }
  
  public String getAFKMessage()
  {
    return this.afkMessage;
  }
  
  public void setAFKMessage(String newAFKMessage)
  {
    this.afkMessage = newAFKMessage;
  }
  
  public boolean isSendExtraBytes()
  {
    return true;
  }
  
  public void setSendExtraBytes(boolean _sendExtraBytes)
  {
    getSaveFile().setSessionFlag(2, _sendExtraBytes);
  }
  
  public void setClientVersion(String newVersion)
  {
    this.clientVersion = newVersion;
  }
  
  public void setClientSystem(String newSystem)
  {
    this.clientSystem = newSystem;
  }
  
  public final String getClientVersion()
  {
    return this.clientVersion;
  }
  
  public final String getClientSystem()
  {
    return this.clientSystem;
  }
  
  public void setNextActionRarity(byte newRarity)
  {
    this.nextActionRarity = newRarity;
  }
  
  public final boolean canUseFreeVillageTeleport()
  {
    return !hasFlag(21);
  }
  
  public void setUsedFreeVillageTeleport()
  {
    setFlag(21, true);
  }
  
  public boolean isCreationWindowOpen()
  {
    return getSaveFile().isSessionFlagSet(3);
  }
  
  public void setCreationWindowOpen(boolean isOpen)
  {
    getSaveFile().setSessionFlag(3, isOpen);
  }
  
  public void setPrivateMapPOIList(Set<MapAnnotation> annotations)
  {
    this.mapAnnotations = annotations;
  }
  
  private final boolean addPrivateMapPOI(MapAnnotation annotation)
  {
    if (this.mapAnnotations.size() < 500)
    {
      this.mapAnnotations.add(annotation);
      return true;
    }
    getCommunicator().sendNormalServerMessage("You can only have a maximum of 500 private annotations.");
    return false;
  }
  
  public void addMapPOI(MapAnnotation annotation, boolean send)
  {
    switch (annotation.getType())
    {
    case 0: 
      if (addPrivateMapPOI(annotation)) {
        if (send) {
          getCommunicator().sendMapAnnotations(new MapAnnotation[] { annotation });
        }
      }
      break;
    case 1: 
      if ((this.citizenVillage != null) && 
        (!this.citizenVillage.addVillageMapAnnotation(annotation, send)) && 
        (send)) {
        getCommunicator().sendNormalServerMessage("You can only have a maximum of 500 village annotations.");
      }
      break;
    case 2: 
      if (this.citizenVillage != null) {
        if (this.citizenVillage.getAllianceNumber() != 0)
        {
          PvPAlliance alliance = PvPAlliance.getPvPAlliance(this.citizenVillage.getAllianceNumber());
          if ((alliance != null) && 
            (!alliance.addAllianceMapAnnotation(annotation, send)) && 
            (send)) {
            getCommunicator().sendNormalServerMessage("You can only have a maximum of 500 alliance annotations.");
          }
        }
      }
      break;
    default: 
      logger.log(Level.WARNING, "Trying to add annotation of unknown type: " + annotation.getType());
    }
  }
  
  public final void removeMapPOI(MapAnnotation annotation)
  {
    switch (annotation.getType())
    {
    case 0: 
      removePrivatePOI(annotation);
      break;
    case 1: 
      if (this.citizenVillage != null) {
        this.citizenVillage.removeVillageMapAnnotation(annotation);
      }
      break;
    case 2: 
      if ((this.citizenVillage != null) && (this.citizenVillage.getAllianceNumber() != 0))
      {
        PvPAlliance alliance = PvPAlliance.getPvPAlliance(this.citizenVillage.getAllianceNumber());
        if (alliance != null) {
          alliance.removeAllianceMapAnnotation(annotation);
        }
      }
      break;
    default: 
      logger.log(Level.WARNING, "Trying to remove annotation of unkown type: " + annotation.getType());
    }
  }
  
  private void removePrivatePOI(MapAnnotation annotation)
  {
    if (this.mapAnnotations.contains(annotation))
    {
      this.mapAnnotations.remove(annotation);
      try
      {
        MapAnnotation.deleteAnnotation(annotation.getId());
      }
      catch (IOException iex)
      {
        logger.log(Level.WARNING, "Error when deleting annotation: " + annotation
          .getId() + " : " + iex.getMessage(), iex);
      }
    }
  }
  
  private MapAnnotation getPrivateAnnotationById(long id)
  {
    for (MapAnnotation anno : this.mapAnnotations) {
      if (anno.getId() == id) {
        return anno;
      }
    }
    return null;
  }
  
  private MapAnnotation getVillageAnnotationById(long id)
  {
    for (MapAnnotation anno : getVillageAnnotations()) {
      if (anno.getId() == id) {
        return anno;
      }
    }
    return null;
  }
  
  private MapAnnotation getAllianceAnnotationById(long id)
  {
    for (MapAnnotation anno : getAllianceAnnotations()) {
      if (anno.getId() == id) {
        return anno;
      }
    }
    return null;
  }
  
  public final MapAnnotation getAnnotation(long id, byte type)
  {
    switch (type)
    {
    case 0: 
      return getPrivateAnnotationById(id);
    case 1: 
      return getVillageAnnotationById(id);
    case 2: 
      return getAllianceAnnotationById(id);
    }
    logger.log(Level.WARNING, "There is no such annotation type : " + type);
    return null;
  }
  
  public final Set<MapAnnotation> getPrivateMapAnnotations()
  {
    return this.mapAnnotations;
  }
  
  public final Set<MapAnnotation> getVillageAnnotations()
  {
    Set<MapAnnotation> annos = new HashSet();
    if (this.citizenVillage != null) {
      annos.addAll(this.citizenVillage.getVillageMapAnnotations());
    }
    return annos;
  }
  
  public final Set<MapAnnotation> getAllianceAnnotations()
  {
    Set<MapAnnotation> annos = new HashSet();
    if ((this.citizenVillage != null) && (this.citizenVillage.getAllianceNumber() != 0))
    {
      PvPAlliance alliance = PvPAlliance.getPvPAlliance(this.citizenVillage.getAllianceNumber());
      if (alliance != null) {
        annos.addAll(alliance.getAllianceMapAnnotations());
      }
    }
    return annos;
  }
  
  public final Set<MapAnnotation> getAllMapAnnotations()
  {
    Set<MapAnnotation> annos = new HashSet();
    annos.addAll(this.mapAnnotations);
    Village vill = Villages.getVillageForCreature(this);
    if (vill != null)
    {
      annos.addAll(vill.getVillageMapAnnotations());
      if (vill.getAllianceNumber() != 0)
      {
        PvPAlliance alliance = PvPAlliance.getPvPAlliance(vill.getAllianceNumber());
        if (alliance != null) {
          annos.addAll(alliance.getAllianceMapAnnotations());
        }
      }
    }
    return annos;
  }
  
  public void createNewMapPOI(String poiName, byte type, int x, int y, String server, byte icon)
  {
    long poiPos = BigInteger.valueOf(x).shiftLeft(32).longValue() + y;
    long ownerID = 0L;
    switch (type)
    {
    case 0: 
      ownerID = getWurmId();
      break;
    case 1: 
      if (this.citizenVillage == null) {
        return;
      }
      ownerID = this.citizenVillage.getId();
      break;
    case 2: 
      if (this.citizenVillage != null)
      {
        if (this.citizenVillage.getAllianceNumber() == 0) {
          return;
        }
        ownerID = this.citizenVillage.getAllianceNumber();
      }
      break;
    default: 
      logger.log(Level.WARNING, "Trying to add annotation of unknown type: " + type);
      return;
    }
    try
    {
      MapAnnotation mapAnnotation = MapAnnotation.createNew(poiName, type, poiPos, ownerID, server, icon);
      addMapPOI(mapAnnotation, true);
    }
    catch (IOException iex)
    {
      logger.log(Level.WARNING, "Error when creating new map annotation: " + iex.getMessage(), iex);
    }
  }
  
  public void sendAllMapAnnotations()
  {
    Set<MapAnnotation> anno = getAllMapAnnotations();
    MapAnnotation[] annotations = new MapAnnotation[anno.size()];
    anno.toArray(annotations);
    getCommunicator().sendMapAnnotations(annotations);
  }
  
  public final boolean isAllowedToEditVillageMap()
  {
    if (this.citizenVillage != null)
    {
      if (this.citizenVillage.getMayor().getId() == getWurmId()) {
        return true;
      }
      Citizen citizen = this.citizenVillage.getCitizen(getWurmId());
      if (citizen != null) {
        return citizen.getRole().mayManageMap();
      }
    }
    return false;
  }
  
  public final boolean isAllowedToEditAllianceMap()
  {
    if (isAllowedToEditVillageMap()) {
      if ((this.citizenVillage != null) && (this.citizenVillage.getAllianceNumber() != 0))
      {
        PvPAlliance alliance = PvPAlliance.getPvPAlliance(this.citizenVillage.getAllianceNumber());
        if (alliance != null)
        {
          Citizen cit = this.citizenVillage.getCitizen(getWurmId());
          if (cit != null) {
            return cit.getRole().isDiplomat();
          }
        }
      }
    }
    return false;
  }
  
  public void sendClearVillageMapAnnotations()
  {
    getCommunicator().sendClearMapAnnotationsOfType((byte)1);
  }
  
  public void sendClearAllianceMapAnnotations()
  {
    getCommunicator().sendClearMapAnnotationsOfType((byte)2);
  }
  
  public long getMoneyEarnedBySellingLastHour()
  {
    return this.saveFile.getMoneyEarnedBySellingLastHour();
  }
  
  int messages = 0;
  private ConcurrentHashMap<Integer, Float> scoresToClear;
  private static final int KINGLIMIT = 100000;
  
  public void addMoneyEarnedBySellingLastHour(long money)
  {
    this.saveFile.addMoneyEarnedBySellingLastHour(money);
    if (this.messages++ < 10) {
      getCommunicator().sendSafeServerMessage("You receive " + money + " irons. Your bank account will be updated shortly.");
    } else {
      getCommunicator().sendSafeServerMessage("You receive " + money + " irons.");
    }
  }
  
  public void addPlayerVote(PlayerVote pv)
  {
    this.playerQuestionVotes.put(Integer.valueOf(pv.getQuestionId()), pv);
    checkCanVote();
  }
  
  public void removePlayerVote(int questionId)
  {
    if (this.playerQuestionVotes.containsKey(Integer.valueOf(questionId))) {
      this.playerQuestionVotes.remove(Integer.valueOf(questionId));
    }
    checkCanVote();
  }
  
  public PlayerVote getPlayerVote(int qId)
  {
    return (PlayerVote)this.playerQuestionVotes.get(Integer.valueOf(qId));
  }
  
  public boolean containsPlayerVote(int qId)
  {
    return this.playerQuestionVotes.containsKey(Integer.valueOf(qId));
  }
  
  public boolean hasVoted(int aQuestionId)
  {
    if (containsPlayerVote(aQuestionId)) {
      return getPlayerVote(aQuestionId).hasVoted();
    }
    return false;
  }
  
  public void getVotes()
  {
    this.askedForVotes = true;
    if (Servers.isThisLoginServer())
    {
      PlayerVote[] pvs = PlayerVotes.getPlayerVotes(getWurmId());
      setVotes(pvs);
      return;
    }
    int[] ids = VoteQuestions.getVoteQuestionIds(this);
    if (ids.length == 0)
    {
      this.playerQuestionVotes.clear();
      
      this.gotVotes = true;
      return;
    }
    WcVoting wv = new WcVoting(getWurmId(), VoteQuestions.getVoteQuestionIds(this));
    wv.sendToLoginServer();
  }
  
  public void setVotes(PlayerVote[] playerVotes)
  {
    this.gotVotes = false;
    this.playerQuestionVotes.clear();
    for (PlayerVote pv : playerVotes) {
      addPlayerVote(pv);
    }
    fillVotes();
    this.gotVotes = true;
  }
  
  public void fillVotes()
  {
    this.canVote = false;
    VoteQuestion[] vqs = VoteQuestions.getVoteQuestions(this);
    for (VoteQuestion vq : vqs) {
      if (!containsPlayerVote(vq.getQuestionId()))
      {
        PlayerVote pv = new PlayerVote(getWurmId(), vq.getQuestionId());
        addPlayerVote(pv);
        this.canVote = true;
      }
    }
  }
  
  public void checkCanVote()
  {
    this.canVote = false;
    VoteQuestion[] vqs = VoteQuestions.getVoteQuestions(this);
    for (VoteQuestion vq : vqs) {
      if ((vq.canVote(this)) && (!hasVoted(vq.getQuestionId()))) {
        this.canVote = true;
      }
    }
  }
  
  public void gotVotes(boolean aNewOne)
  {
    this.gotVotes = false;
    if (this.canVote) {
      if (aNewOne) {
        getCommunicator().sendServerMessage("A new Poll is just available that you can vote on, use /poll to access it.", 250, 150, 250);
      } else {
        getCommunicator().sendServerMessage("There is a Poll available that you can vote on, use /poll to access it.", 250, 150, 250);
      }
    }
  }
  
  public void toggleGMLight()
  {
    this.gmLight = (!this.gmLight);
  }
  
  public boolean askingFriend()
  {
    return this.askingFriend;
  }
  
  public String waitingForFriend()
  {
    return this.waitingForFriendName;
  }
  
  public void setAddFriendTimout(int newCount, Friend.Category friendsCategory)
  {
    this.waitingForFriendCount = newCount;
    this.waitingForFriendCategory = friendsCategory;
  }
  
  public void setAskFriend(String friendsName, Friend.Category friendsCategory)
  {
    this.waitingForFriendName = friendsName;
    this.waitingForFriendCount = 300;
    this.askingFriend = true;
    this.waitingForFriendCategory = friendsCategory;
  }
  
  public byte remoteAddFriend(String aFriendsName, byte aKingdom, byte aReply, boolean xServer, boolean xKingdom)
  {
    if ((this.waitingForFriendName.equalsIgnoreCase(aFriendsName)) && (this.askingFriend))
    {
      this.waitingForFriendName = "";
      this.waitingForFriendCount = -1;
      this.askingFriend = false;
    }
    switch (aReply)
    {
    case 6: 
      PlayerState pstate = PlayerInfoFactory.getPlayerState(aFriendsName);
      if (Servers.isThisATestServer()) {
        sendNormalServerMessage("(test only: adding " + aFriendsName + " under " + this.waitingForFriendCategory
          .name() + ".)");
      }
      addFriend(pstate.getPlayerId(), this.waitingForFriendCategory.getCatId(), "");
      return 5;
    case 4: 
      if (Servers.isThisATestServer()) {
        sendNormalServerMessage("(test only: " + aFriendsName + " is busy.)");
      }
      sendNormalServerMessage(aFriendsName + " is not currently available, please try again later.");
      break;
    case 2: 
      if (Servers.isThisATestServer()) {
        sendNormalServerMessage("(test only: " + aFriendsName + " is offline.)");
      }
      sendNormalServerMessage(aFriendsName + " is not currently available, please try again later.");
      break;
    case 3: 
      if (Servers.isThisATestServer()) {
        sendNormalServerMessage("(test only: " + aFriendsName + " did not respond in time.)");
      }
      sendNormalServerMessage(aFriendsName + " is not currently available, please try again later.");
      break;
    case 8: 
      sendNormalServerMessage(aFriendsName + " is ignoring you.");
      break;
    case 1: 
      sendNormalServerMessage("Unknown player " + aFriendsName + '.');
      break;
    case 5: 
    case 7: 
    default: 
      break;
      if ((this.waitingForFriendName.equalsIgnoreCase(aFriendsName)) && (!this.askingFriend))
      {
        this.waitingForFriendName = "";
        this.waitingForFriendCount = -1;
        this.askingFriend = false;
        if (aReply == 5)
        {
          PlayerState pstate = PlayerInfoFactory.getPlayerState(aFriendsName);
          if (Servers.isThisATestServer()) {
            sendNormalServerMessage("(test only: adding " + aFriendsName + " under " + this.waitingForFriendCategory.name() + ".)");
          }
          addFriend(pstate.getPlayerId(), this.waitingForFriendCategory.getCatId(), "");
        }
        else if (aReply == 3)
        {
          if (Servers.isThisATestServer()) {
            sendNormalServerMessage("(test only: Out of time to respond to " + aFriendsName + ".)");
          }
        }
      }
      else if (aReply == 0)
      {
        if (this.waitingForFriendName.length() == 0)
        {
          PlayerState pstate = PlayerInfoFactory.getPlayerState(aFriendsName);
          if (isIgnored(pstate.getPlayerId())) {
            return 8;
          }
          if (hasFlag(1)) {
            return 2;
          }
          if (getKingdomId() != aKingdom) {
            if (((!hasFlag(2) ? 1 : 0) | (!xKingdom ? 1 : 0)) != 0) {
              return 2;
            }
          }
          if ((xServer) && (!hasFlag(3))) {
            return 2;
          }
          sendServerMessage(aFriendsName + " is asking to be your friend. Use '/addfriend " + aFriendsName + " <category>' to allow them.", -1, -1, -1);
          
          this.waitingForFriendName = aFriendsName;
          this.waitingForFriendCount = 300;
          this.askingFriend = false;
        }
        else
        {
          return 4;
        }
      }
      break;
    }
    return 7;
  }
  
  public final float addSpellResistance(short spellId)
  {
    return this.saveFile.addSpellResistance(spellId);
  }
  
  public final SpellResistance getSpellResistance(short spellId)
  {
    return this.saveFile.getSpellResistance(spellId);
  }
  
  public final void sendSpellResistances()
  {
    this.saveFile.sendSpellResistances(this.communicator);
  }
  
  public final void setArmourLimitingFactor(float factor, boolean initializing)
  {
    this.saveFile.setArmourLimitingFactor(factor, this.communicator, initializing);
  }
  
  public final float getArmourLimitingFactor()
  {
    return this.saveFile.getArmourLimitingFactor();
  }
  
  public void recalcLimitingFactor(@Nullable Item currentItem)
  {
    Item[] boditems = getBody().getContainersAndWornItems();
    float currLimit = 0.3F;
    for (Item i : boditems) {
      if (i.isArmour())
      {
        try
        {
          if (i.equals(getArmour((byte)1))) {
            continue;
          }
        }
        catch (NoArmourException|NoSpaceException localNoArmourException) {}
        ArmourTemplate armour = ArmourTemplate.getArmourTemplate(i.getTemplateId());
        if (armour != null)
        {
          if (armour.getLimitFactor() < currLimit) {
            currLimit = armour.getLimitFactor();
          }
        }
        else {
          logger.log(Level.WARNING, "Armour is not in Armour list  " + i.getName() + ".");
        }
      }
    }
    if (currentItem != null) {
      try
      {
        if (!currentItem.equals(getArmour((byte)1)))
        {
          ArmourTemplate armour = ArmourTemplate.getArmourTemplate(currentItem.getTemplateId());
          if (armour != null) {
            if (armour.getLimitFactor() < currLimit) {
              currLimit = armour.getLimitFactor();
            }
          }
        }
      }
      catch (NoArmourException|NoSpaceException localNoArmourException2) {}
    }
    setArmourLimitingFactor(currLimit, currentItem == null);
  }
  
  public void addChallengeScore(int type, float scoreAdded)
  {
    if (Servers.localServer.isChallengeServer())
    {
      if (this.scoresToClear == null) {
        this.scoresToClear = new ConcurrentHashMap();
      }
      Float score = (Float)this.scoresToClear.get(Integer.valueOf(type));
      if (score == null) {
        score = Float.valueOf(scoreAdded);
      } else {
        score = Float.valueOf(score.floatValue() + scoreAdded);
      }
      this.scoresToClear.put(Integer.valueOf(type), score);
    }
  }
  
  private final void clearChallengeScores()
  {
    if (this.scoresToClear != null)
    {
      for (Map.Entry<Integer, Float> entry : this.scoresToClear.entrySet())
      {
        Integer type = (Integer)entry.getKey();
        Float value = (Float)entry.getValue();
        ChallengeSummary.addToScore(this.saveFile, type.intValue(), value.floatValue());
        ChallengeSummary.addToScore(this.saveFile, ChallengePointEnum.ChallengePoint.OVERALL.getEnumtype(), value.floatValue());
      }
      this.scoresToClear.clear();
    }
  }
  
  public boolean checkCoinAward(int chance)
  {
    if (Server.rand.nextInt(chance) == 0)
    {
      Shop kingsMoney = Economy.getEconomy().getKingsShop();
      if (kingsMoney.getMoney() > 100000L)
      {
        int coinRand = Server.rand.nextInt(10);
        int coin = 50;
        switch (coinRand)
        {
        case 0: 
          coin = 50;
          break;
        case 1: 
        case 2: 
        case 3: 
        case 4: 
          coin = 54;
          break;
        case 5: 
        case 6: 
        case 7: 
        case 8: 
          coin = 58;
          break;
        case 9: 
          coin = 52;
          break;
        default: 
          coin = 50;
        }
        try
        {
          float faintChance = 1.0F;
          
          int supPremModifier = 0;
          
          byte rarity = 1;
          if (isPaying())
          {
            faintChance = 1.03F;
            supPremModifier = 3;
          }
          if (Server.rand.nextFloat() * 10000.0F <= faintChance) {
            rarity = 3;
          } else if (Server.rand.nextInt(100) <= 0 + supPremModifier) {
            rarity = 2;
          }
          Item coinItem = ItemFactory.createItem(coin, 60 + Server.rand.nextInt(20), rarity, "");
          getInventory().insertItem(coinItem, true);
          kingsMoney.setMoney(kingsMoney.getMoney() - Economy.getValueFor(coin));
          getCommunicator().sendRarityEvent();
          return true;
        }
        catch (NoSuchTemplateException nst)
        {
          logger.log(Level.WARNING, "No template for item coin");
        }
        catch (FailedException fe)
        {
          logger.log(Level.WARNING, fe.getMessage() + ": coin");
        }
      }
    }
    return false;
  }
  
  public long getRespondTo()
  {
    return this.sendResponseTo;
  }
  
  public void clearRespondTo()
  {
    this.sendResponseTo = -10L;
  }
  
  public String getAllianceName()
  {
    if (getCitizenVillage() != null)
    {
      int allianceNumber = getCitizenVillage().getAllianceNumber();
      if (allianceNumber > 0)
      {
        PvPAlliance alliance = PvPAlliance.getPvPAlliance(allianceNumber);
        if (alliance != null) {
          return alliance.getName();
        }
      }
    }
    return "";
  }
  
  public String getVillageName()
  {
    if (getCitizenVillage() != null) {
      return getCitizenVillage().getName();
    }
    return "";
  }
  
  public String getKingdomName()
  {
    return Kingdoms.getNameFor(getKingdomId());
  }
  
  public long getLastChangedPath()
  {
    return this.saveFile.getLastChangedPath();
  }
  
  public void setLastChangedPath(long lastChangedPath)
  {
    this.saveFile.setLastChangedPath(lastChangedPath);
  }
  
  public long getPlotCourseCooldown()
  {
    Cooldowns cd = Cooldowns.getCooldownsFor(getWurmId(), false);
    if (cd != null) {
      return cd.isAvaibleAt(717);
    }
    return 0L;
  }
  
  public void addPlotCourseCooldown(long cooldown)
  {
    Cooldowns cd = Cooldowns.getCooldownsFor(getWurmId(), true);
    cd.addCooldown(717, System.currentTimeMillis() + cooldown, false);
  }
  
  public void checkKingdom()
  {
    if ((!Servers.isThisAChaosServer()) || (getPower() > 0)) {
      return;
    }
    String reason = "You have no kingdom.";
    byte kingdomId = getKingdomId();
    boolean changeKingdom = kingdomId == 0;
    boolean isPmk = (kingdomId < 0) || (kingdomId > 4);
    if ((!changeKingdom) && (isPmk))
    {
      Kingdom k = Kingdoms.getKingdomOrNull(getKingdomId());
      if (k == null)
      {
        reason = "Your kingdom no longer exists.";
        changeKingdom = true;
      }
      else if (!k.existsHere())
      {
        reason = k.getName() + " no longer exists here.";
        changeKingdom = true;
      }
    }
    if (changeKingdom) {
      try
      {
        getCommunicator().sendSafeServerMessage(reason + " You are now a member of " + 
          Kingdoms.getNameFor((byte)4));
        setKingdomId((byte)4, true, false);
        logger.log(Level.INFO, getName() + ": Invalid kingdom, moving to " + Kingdoms.getNameFor((byte)4));
      }
      catch (IOException localIOException) {}
    }
  }
  
  public String checkCourseRestrictions()
  {
    if ((isFighting()) || (getEnemyPresense() > 0)) {
      if (getSecondsPlayed() > 300.0F) {
        return "There are enemies in the vicinity. You fail to focus on a course.";
      }
    }
    return "";
  }
  
  public byte getRarityShader()
  {
    if (getBonusForSpellEffect((byte)22) > 70.0F) {
      return 2;
    }
    if (getBonusForSpellEffect((byte)22) > 0.0F) {
      return 1;
    }
    return this.rarityShader;
  }
  
  public void setRarityShader(byte rarityShader)
  {
    this.rarityShader = rarityShader;
    getCurrentTile().setNewRarityShader(this);
    if (rarityShader != 0) {
      this.raritySeconds = 100;
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\players\Player.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */