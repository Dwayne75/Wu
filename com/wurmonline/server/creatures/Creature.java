package com.wurmonline.server.creatures;

import com.wurmonline.communication.SocketConnection;
import com.wurmonline.math.TilePos;
import com.wurmonline.math.Vector2f;
import com.wurmonline.math.Vector3f;
import com.wurmonline.mesh.GrassData;
import com.wurmonline.mesh.GrassData.FlowerType;
import com.wurmonline.mesh.GrassData.GrowthStage;
import com.wurmonline.mesh.MeshIO;
import com.wurmonline.mesh.Tiles;
import com.wurmonline.mesh.Tiles.Tile;
import com.wurmonline.server.Constants;
import com.wurmonline.server.FailedException;
import com.wurmonline.server.Features.Feature;
import com.wurmonline.server.HistoryManager;
import com.wurmonline.server.Items;
import com.wurmonline.server.LoginHandler;
import com.wurmonline.server.Message;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Players;
import com.wurmonline.server.PlonkData;
import com.wurmonline.server.Server;
import com.wurmonline.server.ServerEntry;
import com.wurmonline.server.Servers;
import com.wurmonline.server.Team;
import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.WurmCalendar;
import com.wurmonline.server.WurmId;
import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.behaviours.ActionEntry;
import com.wurmonline.server.behaviours.ActionStack;
import com.wurmonline.server.behaviours.Behaviour;
import com.wurmonline.server.behaviours.BehaviourDispatcher;
import com.wurmonline.server.behaviours.Behaviours;
import com.wurmonline.server.behaviours.ItemBehaviour;
import com.wurmonline.server.behaviours.MethodsItems;
import com.wurmonline.server.behaviours.MethodsStructure;
import com.wurmonline.server.behaviours.NoSuchActionException;
import com.wurmonline.server.behaviours.NoSuchBehaviourException;
import com.wurmonline.server.behaviours.Seat;
import com.wurmonline.server.behaviours.Terraforming;
import com.wurmonline.server.behaviours.TileFieldBehaviour;
import com.wurmonline.server.behaviours.TileRockBehaviour;
import com.wurmonline.server.behaviours.Vehicle;
import com.wurmonline.server.behaviours.Vehicles;
import com.wurmonline.server.bodys.Body;
import com.wurmonline.server.bodys.Wound;
import com.wurmonline.server.bodys.Wounds;
import com.wurmonline.server.combat.ArmourTemplate;
import com.wurmonline.server.combat.ArmourTemplate.ArmourType;
import com.wurmonline.server.combat.Battle;
import com.wurmonline.server.combat.BattleEvent;
import com.wurmonline.server.combat.Battles;
import com.wurmonline.server.combat.CombatConstants;
import com.wurmonline.server.combat.CombatEngine;
import com.wurmonline.server.combat.SpecialMove;
import com.wurmonline.server.combat.Weapon;
import com.wurmonline.server.creatures.ai.CreatureAI;
import com.wurmonline.server.creatures.ai.CreatureAIData;
import com.wurmonline.server.creatures.ai.CreaturePathFinder;
import com.wurmonline.server.creatures.ai.CreaturePathFinderAgg;
import com.wurmonline.server.creatures.ai.CreaturePathFinderNPC;
import com.wurmonline.server.creatures.ai.DecisionStack;
import com.wurmonline.server.creatures.ai.NoPathException;
import com.wurmonline.server.creatures.ai.Order;
import com.wurmonline.server.creatures.ai.Path;
import com.wurmonline.server.creatures.ai.PathFinder;
import com.wurmonline.server.creatures.ai.PathTile;
import com.wurmonline.server.deities.Deities;
import com.wurmonline.server.deities.Deity;
import com.wurmonline.server.economy.Economy;
import com.wurmonline.server.economy.MonetaryConstants;
import com.wurmonline.server.economy.Shop;
import com.wurmonline.server.effects.Effect;
import com.wurmonline.server.endgames.EndGameItems;
import com.wurmonline.server.epic.EpicMission;
import com.wurmonline.server.epic.EpicMissionEnum;
import com.wurmonline.server.epic.EpicServerStatus;
import com.wurmonline.server.highways.Route;
import com.wurmonline.server.intra.MountTransfer;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemFactory;
import com.wurmonline.server.items.ItemSettings;
import com.wurmonline.server.items.ItemSettings.CorpsePermissions;
import com.wurmonline.server.items.ItemSpellEffects;
import com.wurmonline.server.items.ItemTemplate;
import com.wurmonline.server.items.ItemTemplateFactory;
import com.wurmonline.server.items.ItemTypes;
import com.wurmonline.server.items.NoSpaceException;
import com.wurmonline.server.items.NoSuchTemplateException;
import com.wurmonline.server.items.Possessions;
import com.wurmonline.server.items.Recipe;
import com.wurmonline.server.items.Recipes;
import com.wurmonline.server.items.Trade;
import com.wurmonline.server.items.TradingWindow;
import com.wurmonline.server.kingdom.Appointments;
import com.wurmonline.server.kingdom.GuardTower;
import com.wurmonline.server.kingdom.King;
import com.wurmonline.server.kingdom.Kingdom;
import com.wurmonline.server.kingdom.Kingdoms;
import com.wurmonline.server.modifiers.DoubleValueModifier;
import com.wurmonline.server.modifiers.ModifierTypes;
import com.wurmonline.server.players.Abilities;
import com.wurmonline.server.players.Achievements;
import com.wurmonline.server.players.Cultist;
import com.wurmonline.server.players.ItemBonus;
import com.wurmonline.server.players.KingdomIp;
import com.wurmonline.server.players.MovementEntity;
import com.wurmonline.server.players.MusicPlayer;
import com.wurmonline.server.players.PermissionsByPlayer;
import com.wurmonline.server.players.PermissionsPlayerList;
import com.wurmonline.server.players.PermissionsPlayerList.ISettings;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.players.PlayerInfo;
import com.wurmonline.server.players.PlayerInfoFactory;
import com.wurmonline.server.players.PlayerKills;
import com.wurmonline.server.players.SpellResistance;
import com.wurmonline.server.players.Titles.Title;
import com.wurmonline.server.questions.SimplePopup;
import com.wurmonline.server.questions.TestQuestion;
import com.wurmonline.server.questions.TraderManagementQuestion;
import com.wurmonline.server.skills.NoSuchSkillException;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.skills.SkillSystem;
import com.wurmonline.server.skills.Skills;
import com.wurmonline.server.skills.SkillsFactory;
import com.wurmonline.server.sounds.SoundPlayer;
import com.wurmonline.server.spells.SpellEffect;
import com.wurmonline.server.structures.Blocker;
import com.wurmonline.server.structures.Blocking;
import com.wurmonline.server.structures.BlockingResult;
import com.wurmonline.server.structures.BridgePart;
import com.wurmonline.server.structures.Door;
import com.wurmonline.server.structures.Fence;
import com.wurmonline.server.structures.Floor;
import com.wurmonline.server.structures.NoSuchStructureException;
import com.wurmonline.server.structures.NoSuchWallException;
import com.wurmonline.server.structures.Structure;
import com.wurmonline.server.structures.Structures;
import com.wurmonline.server.structures.Wall;
import com.wurmonline.server.tutorial.MissionPerformed;
import com.wurmonline.server.tutorial.MissionPerformer;
import com.wurmonline.server.tutorial.MissionTrigger;
import com.wurmonline.server.tutorial.MissionTriggers;
import com.wurmonline.server.utils.CreatureLineSegment;
import com.wurmonline.server.utils.StringUtil;
import com.wurmonline.server.utils.logging.TileEvent;
import com.wurmonline.server.villages.Citizen;
import com.wurmonline.server.villages.Guard;
import com.wurmonline.server.villages.GuardPlan;
import com.wurmonline.server.villages.NoSuchVillageException;
import com.wurmonline.server.villages.Reputation;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.villages.VillageRole;
import com.wurmonline.server.villages.Villages;
import com.wurmonline.server.weather.Weather;
import com.wurmonline.server.webinterface.WcEpicKarmaCommand;
import com.wurmonline.server.webinterface.WcKillCommand;
import com.wurmonline.server.webinterface.WcTrelloDeaths;
import com.wurmonline.server.zones.Den;
import com.wurmonline.server.zones.Dens;
import com.wurmonline.server.zones.FaithZone;
import com.wurmonline.server.zones.FocusZone;
import com.wurmonline.server.zones.HiveZone;
import com.wurmonline.server.zones.NoSuchZoneException;
import com.wurmonline.server.zones.Trap;
import com.wurmonline.server.zones.VirtualZone;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zone;
import com.wurmonline.server.zones.Zones;
import com.wurmonline.shared.constants.AttitudeConstants;
import com.wurmonline.shared.constants.CounterTypes;
import com.wurmonline.shared.constants.CreatureTypes;
import com.wurmonline.shared.constants.ProtoConstants;
import com.wurmonline.shared.exceptions.WurmServerException;
import com.wurmonline.shared.util.MovementChecker;
import com.wurmonline.shared.util.MulticolorLineSegment;
import com.wurmonline.shared.util.StringUtilities;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class Creature
  implements ItemTypes, CounterTypes, MiscConstants, CreatureTypes, TimeConstants, ProtoConstants, CombatConstants, ModifierTypes, CreatureTemplateIds, MonetaryConstants, AttitudeConstants, PermissionsPlayerList.ISettings, Comparable<Creature>
{
  protected Skills skills;
  private int respawnCounter = 0;
  private static final int NPCRESPAWN = 600;
  protected CreatureStatus status;
  private long id;
  private static final double skillLost = 0.25D;
  public static final double MAX_LEAD_DEPTH = -0.71D;
  public long loggerCreature1 = -10L;
  private long loggerCreature2 = -10L;
  public int combatRound = 0;
  public SpecialMove specialMove = null;
  protected boolean isVehicleCommander = false;
  public Creature lastOpponent;
  public int opponentCounter = 0;
  protected boolean _enterVehicle = false;
  protected MountAction mountAction = null;
  public boolean addingAfterTeleport = false;
  private static final Item[] emptyItems = new Item[0];
  protected long linkedTo = -10L;
  private boolean isInDuelRing = false;
  private static final DoubleValueModifier willowMod = new DoubleValueModifier(-0.15000000596046448D);
  public boolean shouldStandStill = false;
  public byte opportunityAttackCounter = 0;
  private long lastSentToolbelt = 0L;
  protected static final float submergedMinDepth = -5.0F;
  protected static final Logger logger = Logger.getLogger(Creature.class.getName());
  protected CreatureTemplate template;
  protected Vehicle hitchedTo = null;
  protected MusicPlayer musicPlayer;
  private boolean inHostilePerimeter = false;
  private int hugeMoveCounter = 0;
  protected String name = "Noname";
  protected String petName = "";
  protected Possessions possessions;
  protected Communicator communicator;
  private VisionArea visionArea;
  private final Behaviour behaviour;
  protected ActionStack actions;
  private Structure structure;
  public int numattackers;
  protected Map<Long, Long> attackers;
  public Creature opponent = null;
  private Set<Long> riders = null;
  private Set<Item> keys;
  protected byte fightlevel = 0;
  protected boolean guest = false;
  protected boolean isTeleporting = false;
  private long startTeleportTime = Long.MIN_VALUE;
  public boolean faithful = true;
  private Door currentDoor = null;
  private float teleportX = -1.0F;
  private float teleportY = -1.0F;
  protected int teleportLayer = 0;
  protected int teleportFloorLevel = 0;
  protected boolean justSpawned = false;
  public String spawnWeapon = "";
  public String spawnArmour = "";
  private LinkedList<int[]> openedTiles;
  private int carriedWeight = 0;
  private static final float DEGS_TO_RADS = 0.017453292F;
  private TradeHandler tradeHandler;
  public Village citizenVillage;
  public Village currentVillage;
  private Set<Item> itemsTaken = null;
  private Set<Item> itemsDropped = null;
  protected MovementScheme movementScheme;
  protected Battle battle = null;
  private Set<Long> stealthBreakers = null;
  private Set<DoubleValueModifier> visionModifiers;
  private final ConcurrentHashMap<Item, Float> weaponsUsed = new ConcurrentHashMap();
  private final ConcurrentHashMap<AttackAction, UsedAttackData> attackUsed = new ConcurrentHashMap();
  public long lastSavedPos = System.currentTimeMillis() - Server.rand.nextInt(1800000);
  protected byte guardSecondsLeft = 0;
  private byte fightStyle = 2;
  private boolean milked = false;
  private boolean sheared = false;
  private boolean isRiftSummoned = false;
  public long target = -10L;
  public Creature leader = null;
  public long dominator = -10L;
  public float zoneBonus = 0.0F;
  private byte currentDeity = 0;
  public byte fleeCounter = 0;
  public boolean isLit = false;
  private int encumbered = 70000;
  private int moveslow = 40000;
  private int cantmove = 140000;
  private byte tilesMoved = 0;
  private byte pathfindcounter = 0;
  protected Map<Creature, Item> followers = null;
  protected static final Creature[] emptyCreatures = new Creature[0];
  public byte currentKingdom = 0;
  protected short damageCounter = 0;
  private final DoubleValueModifier woundMoveMod = new DoubleValueModifier(7, -0.25D);
  public long lastParry = 0L;
  public VolaTile currentTile;
  public int staminaPollCounter = 0;
  private DecisionStack decisions = null;
  private static final float HUNGER_RANGE = 20535.0F;
  public boolean goOffline = false;
  private Item bestLightsource = null;
  private Item bestCompass = null;
  private Item bestToolbelt = null;
  private Item bestBeeSmoker = null;
  public boolean lightSourceChanged = false;
  public boolean lastSentHasCompass = false;
  private CombatHandler combatHandler = null;
  private int pollCounter = 0;
  private static final int secondsBetweenItemPolls = 10800;
  private static final int secondsBetweenTraderCoolingPolls = 600;
  private int heatCheckTick = 0;
  private int mountPollCounter = 10;
  protected int breedCounter = 0;
  private boolean visibleToPlayers = false;
  private boolean forcedBreed = false;
  private boolean hasSpiritStamina = false;
  protected boolean hasSpiritFavorgain = false;
  public boolean hasAddedToAttack = false;
  private static final long LOG_ELAPSED_TIME_THRESHOLD = Constants.lagThreshold;
  private static final boolean DO_MORE_ELAPSED_TIME_MEASUREMENTS = false;
  protected boolean hasSentPoison = false;
  int pathRecalcLength = 0;
  protected boolean isInPvPZone = false;
  protected boolean isInNonPvPZone = false;
  protected boolean isInFogZone = false;
  private static final Set<Long> pantLess = new HashSet();
  private static final Map<Long, Set<MovementEntity>> illusions = new ConcurrentHashMap();
  protected boolean isInOwnBattleCamp = false;
  private boolean doLavaDamage = false;
  private boolean doAreaDamage = false;
  protected float webArmourModTime = 0.0F;
  private ArrayList<Effect> effects;
  private ServerEntry destination;
  private static CreaturePathFinder pathFinder = new CreaturePathFinder();
  private static CreaturePathFinderAgg pathFinderAgg = new CreaturePathFinderAgg();
  private static CreaturePathFinderNPC pathFinderNPC = new CreaturePathFinderNPC();
  public long vehicle = -10L;
  protected byte seatType = -1;
  protected int teleports = 0;
  private long lastWaystoneChecked = -10L;
  private long ownerId = -10L;
  private boolean checkedHotItemsAfterLogin = false;
  private boolean ignoreSaddleDamage = false;
  private boolean isPlacingItem = false;
  private Item placementItem = null;
  private float[] pendingPlacement = null;
  
  static
  {
    pathFinder.startRunning();
    pathFinderAgg.startRunning();
    pathFinderNPC.startRunning();
  }
  
  public static void shutDownPathFinders()
  {
    pathFinder.shutDown();
    pathFinderAgg.shutDown();
    pathFinderNPC.shutDown();
  }
  
  public static final CreaturePathFinder getPF()
  {
    return pathFinder;
  }
  
  public static final CreaturePathFinderAgg getPFA()
  {
    return pathFinderAgg;
  }
  
  public static final CreaturePathFinderNPC getPFNPC()
  {
    return pathFinderNPC;
  }
  
  protected Creature()
    throws Exception
  {
    this.behaviour = Behaviours.getInstance().getBehaviour((short)4);
    this.communicator = new CreatureCommunicator(this);
    this.actions = new ActionStack();
    this.movementScheme = new MovementScheme(this);
    this.pollCounter = Server.rand.nextInt(10800);
  }
  
  public void checkTrap()
  {
    if (!isDead())
    {
      Trap trap = Trap.getTrap(this.currentTile.tilex, this.currentTile.tiley, getLayer());
      if (getPower() >= 3)
      {
        if (trap != null) {
          getCommunicator().sendNormalServerMessage("A " + trap.getName() + " is here.");
        }
      }
      else if (trap != null)
      {
        boolean trigger = false;
        if (trap.getKingdom() != getKingdomId())
        {
          if ((getKingdomId() == 0) && (!isAggHuman()))
          {
            trigger = false;
            if ((this.riders != null) && (this.riders.size() > 0)) {
              for (Long rider : this.riders) {
                try
                {
                  Creature rr = Server.getInstance().getCreature(rider.longValue());
                  if (rr.getKingdomId() != trap.getKingdom()) {
                    trigger = true;
                  }
                }
                catch (NoSuchCreatureException localNoSuchCreatureException) {}catch (NoSuchPlayerException localNoSuchPlayerException) {}
              }
            }
          }
          else
          {
            trigger = true;
          }
        }
        else if (trap.getVillage() > 0) {
          try
          {
            Village vill = Villages.getVillage(trap.getVillage());
            if (vill.isEnemy(this)) {
              trigger = true;
            }
          }
          catch (NoSuchVillageException localNoSuchVillageException1) {}
        }
        if (trigger) {
          trap.doEffect(this, this.currentTile.tilex, this.currentTile.tiley, getLayer());
        }
      }
    }
  }
  
  public void sendDetectTrap(Trap trap)
  {
    if ((trap != null) && (Server.rand.nextInt(100) < getDetectDangerBonus())) {
      getCommunicator().sendAlertServerMessage("TRAP!", (byte)4);
    }
  }
  
  public final void calculateFloorLevel(VolaTile tile, boolean forceAddFloorLayer)
  {
    calculateFloorLevel(tile, forceAddFloorLayer, false);
  }
  
  public final void calculateFloorLevel(VolaTile tile, boolean forceAddFloorLayer, boolean wasOnBridge)
  {
    try
    {
      if ((tile.getStructure() != null) && (tile.getStructure().isTypeHouse()))
      {
        if ((getFloorLevel() == 0) && (!wasOnBridge))
        {
          if (!isPlayer())
          {
            float oldposz = getPositionZ();
            if (oldposz >= -1.25D)
            {
              float newPosz = Zones.calculateHeight(getPosX(), getPosY(), isOnSurface()) + (tile.getFloors(-10, 10).length == 0 ? 0.0F : 0.25F);
              float diffz = oldposz - newPosz;
              setPositionZ(newPosz);
              if ((this.currentTile != null) && (getVisionArea() != null)) {
                moved(0, 0, (int)(diffz * 10.0F), 0, 0);
              }
            }
          }
        }
        else
        {
          int targetFloorLevel = tile.getDropFloorLevel(getFloorLevel());
          if (targetFloorLevel != getFloorLevel())
          {
            if (!isPlayer()) {
              pushToFloorLevel(targetFloorLevel);
            }
          }
          else if (forceAddFloorLayer) {
            if (!isPlayer())
            {
              float oldposz = getPositionZ();
              
              float newPosz = Zones.calculateHeight(getPosX(), getPosY(), isOnSurface()) + (tile.getFloors(-10, 10).length == 0 ? 0.0F : 0.25F);
              float diffz = oldposz - newPosz;
              setPositionZ(newPosz);
              if ((this.currentTile != null) && (getVisionArea() != null)) {
                moved(0, 0, (int)(diffz * 10.0F), 0, 0);
              }
            }
          }
        }
      }
      else if ((tile.getStructure() == null) || (!tile.getStructure().isTypeBridge())) {
        if (getFloorLevel() >= 0) {
          if (!isPlayer())
          {
            float oldposz = getPositionZ();
            if (oldposz >= 0.0F)
            {
              float newPosz = Zones.calculateHeight(getPosX(), getPosY(), isOnSurface());
              float diffz = oldposz - newPosz;
              
              setPositionZ(newPosz);
              if ((this.currentTile != null) && (getVisionArea() != null)) {
                moved(0, 0, (int)(diffz * 10.0F), 0, 0);
              }
            }
          }
        }
      }
    }
    catch (NoSuchZoneException localNoSuchZoneException) {}
  }
  
  public int compareTo(Creature otherCreature)
  {
    return getName().compareTo(otherCreature.getName());
  }
  
  public boolean setNewTile(@Nullable VolaTile newtile, int diffZ, boolean ignoreBridge)
  {
    if ((newtile != null) && ((getTileX() != newtile.tilex) || (getTileY() != newtile.tiley)))
    {
      logger.log(Level.WARNING, getName() + " set to " + newtile.tilex + "," + newtile.tiley + " but at " + getTileX() + "," + 
        getTileY(), new Exception());
      if (this.currentTile != null)
      {
        logger.log(Level.WARNING, "old is " + this.currentTile.tilex + "(" + getPosX() + "), " + this.currentTile.tiley + "(" + 
          getPosY() + "), vehic=" + getVehicle());
        if (isPlayer()) {
          ((Player)this).intraTeleport((this.currentTile.tilex << 2) + 2, (this.currentTile.tiley << 2) + 2, getPositionZ(), 
            getStatus().getRotation(), getLayer(), "on wrong tile");
        }
      }
      return false;
    }
    boolean wasInDuelRing = false;
    Set<FocusZone> oldFocusZones = null;
    HiveZone oldHiveZone = null;
    boolean oldHiveClose = false;
    long oldBridgeId = getBridgeId();
    int[] opened;
    if (this.currentTile != null)
    {
      if (isPlayer())
      {
        Item ring = Zones.isWithinDuelRing(this.currentTile.tilex, this.currentTile.tiley, this.currentTile.isOnSurface());
        if (ring != null) {
          wasInDuelRing = true;
        }
        oldFocusZones = FocusZone.getZonesAt(this.currentTile.tilex, this.currentTile.tiley);
        oldHiveZone = Zones.getHiveZoneAt(this.currentTile.tilex, this.currentTile.tiley, this.currentTile.isOnSurface());
        if (oldHiveZone != null) {
          oldHiveClose = oldHiveZone.isClose(this.currentTile.tilex, this.currentTile.tiley);
        }
      }
      if ((newtile != null) && (!isDead()))
      {
        this.currentTile.checkOpportunityAttacks(this);
        if (this.currentTile != null)
        {
          int diffX = newtile.tilex - this.currentTile.tilex;
          int diffY = newtile.tiley - this.currentTile.tiley;
          if (diffX != 0) {
            sendDetectTrap(Trap.getTrap(newtile.tilex + diffX, newtile.tiley, getLayer()));
          }
          if (diffY != 0) {
            sendDetectTrap(Trap.getTrap(newtile.tilex, newtile.tiley + diffY, getLayer()));
          }
          if ((diffY != 0) && (diffX != 0))
          {
            sendDetectTrap(Trap.getTrap(newtile.tilex + diffX, newtile.tiley + diffY, getLayer()));
          }
          else if (diffX != 0)
          {
            sendDetectTrap(Trap.getTrap(newtile.tilex + diffX, newtile.tiley - 1, getLayer()));
            sendDetectTrap(Trap.getTrap(newtile.tilex + diffX, newtile.tiley + 1, getLayer()));
          }
          else if (diffY != 0)
          {
            sendDetectTrap(Trap.getTrap(newtile.tilex + 1, newtile.tiley + diffY, getLayer()));
            sendDetectTrap(Trap.getTrap(newtile.tilex - 1, newtile.tiley + diffY, getLayer()));
          }
          if (this.currentTile != newtile) {
            this.currentTile.removeCreature(this);
          }
        }
        if (isPlayer()) {
          addTileMoved();
        }
      }
      else
      {
        this.currentTile.removeCreature(this);
      }
      if ((this.currentTile != null) && (isPlayer()) && (this.currentTile != newtile)) {
        if (this.openedTiles != null)
        {
          ListIterator<int[]> openedIterator = this.openedTiles.listIterator();
          while (openedIterator.hasNext())
          {
            opened = (int[])openedIterator.next();
            if ((newtile == null) || (opened[0] != newtile.getTileX()) || 
              (opened[1] != newtile.getTileY())) {
              try
              {
                getCommunicator().sendTileDoor((short)opened[0], (short)opened[1], false);
                openedIterator.remove();
                
                MineDoorPermission md = MineDoorPermission.getPermission((short)opened[0], (short)opened[1]);
                if (md != null) {
                  md.close(this);
                }
              }
              catch (IOException localIOException) {}
            }
          }
          if (this.openedTiles.isEmpty()) {
            this.openedTiles = null;
          }
        }
      }
      if ((this.currentTile != null) && (newtile != null))
      {
        this.currentTile = newtile;
        checkTrap();
        if (isDead()) {
          return false;
        }
        if ((!isPlayer()) && (!ignoreBridge)) {
          checkBridgeMove(this.currentTile, newtile, diffZ);
        }
      }
      else if ((newtile != null) && (!ignoreBridge))
      {
        if (!isPlayer()) {
          checkBridgeMove(null, newtile, diffZ);
        }
      }
    }
    this.currentTile = newtile;
    if (this.currentTile != null)
    {
      if (!isRidden())
      {
        boolean wasOnBridge = false;
        if (oldBridgeId != -10L) {
          if (oldBridgeId != getBridgeId()) {
            wasOnBridge = true;
          }
        }
        calculateFloorLevel(this.currentTile, false, wasOnBridge);
      }
      Set<FocusZone> newFocusZones = FocusZone.getZonesAt(this.currentTile.tilex, this.currentTile.tiley);
      if (!isPlayer())
      {
        this.isInPvPZone = false;
        this.isInNonPvPZone = false;
        for (FocusZone fz : newFocusZones)
        {
          if (fz.isPvP())
          {
            this.isInPvPZone = true;
            break;
          }
          if (fz.isNonPvP())
          {
            this.isInNonPvPZone = true;
            break;
          }
        }
        this.tilesMoved = ((byte)(this.tilesMoved + 1));
        if (this.tilesMoved >= 10)
        {
          if ((isDominated()) || (isHorse())) {
            try
            {
              savePosition(this.currentTile.getZone().getId());
            }
            catch (IOException localIOException1) {}
          }
          this.tilesMoved = 0;
        }
      }
      if (isPlayer())
      {
        try
        {
          savePosition(this.currentTile.getZone().getId());
        }
        catch (IOException localIOException2) {}
        for (FocusZone fz : newFocusZones)
        {
          if (fz.isFog()) {
            if (!this.isInFogZone)
            {
              this.isInFogZone = true;
              getCommunicator().sendSpecificWeather(0.85F);
            }
          }
          if (fz.isPvP())
          {
            if (!this.isInPvPZone)
            {
              if (!isOnPvPServer())
              {
                achievement(4);
                getCommunicator().sendAlertServerMessage("You enter the " + fz
                  .getName() + " PvP area. Other players may attack you here.", (byte)4);
              }
              else
              {
                getCommunicator().sendAlertServerMessage("You enter the " + fz.getName() + " area.", (byte)4);
              }
              sendAttitudeChange();
            }
            this.isInPvPZone = true;
            break;
          }
          if (fz.isNonPvP())
          {
            if (!this.isInNonPvPZone)
            {
              if (isOnPvPServer()) {
                getCommunicator().sendSafeServerMessage("You enter the " + fz
                  .getName() + " No-PvP area. Other players may no longer attack you here.", (byte)2);
              } else {
                getCommunicator().sendSafeServerMessage("You enter the " + fz.getName() + " No-PvP area.", (byte)2);
              }
              sendAttitudeChange();
            }
            this.isInNonPvPZone = true;
            break;
          }
          if ((fz.isName()) || (fz.isNamePopup()) || (fz.isNoBuild()) || (fz.isPremSpawnOnly())) {
            if ((oldFocusZones == null) || (!oldFocusZones.contains(fz))) {
              if ((fz.isName()) || (fz.isNoBuild()) || (fz.isPremSpawnOnly()))
              {
                getCommunicator().sendSafeServerMessage("You enter the " + fz.getName() + " area.", (byte)2);
              }
              else
              {
                SimplePopup sp = new SimplePopup(this, "Entering " + fz.getName(), "You enter the " + fz.getName() + " area.", fz.getDescription());
                sp.sendQuestion();
              }
            }
          }
        }
        if (oldFocusZones != null) {
          for (FocusZone fz : oldFocusZones)
          {
            if (fz.isFog()) {
              if ((newFocusZones == null) || (!newFocusZones.contains(fz)))
              {
                this.isInFogZone = false;
                getCommunicator().checkSendWeather();
              }
            }
            if (fz.isPvP())
            {
              if ((newFocusZones == null) || (!newFocusZones.contains(fz)))
              {
                this.isInPvPZone = false;
                if (isOnPvPServer()) {
                  getCommunicator().sendSafeServerMessage("You leave the " + fz.getName() + " area.", (byte)2);
                } else {
                  getCommunicator().sendSafeServerMessage("You leave the " + fz.getName() + " PvP area.", (byte)2);
                }
                sendAttitudeChange();
              }
            }
            else if (fz.isNonPvP())
            {
              if ((newFocusZones == null) || (!newFocusZones.contains(fz)))
              {
                this.isInNonPvPZone = false;
                sendAttitudeChange();
                if (isOnPvPServer()) {
                  getCommunicator().sendAlertServerMessage("You leave the " + fz
                    .getName() + " No-PvP area. Other players may attack you here.", (byte)2);
                } else {
                  getCommunicator().sendAlertServerMessage("You leave the " + fz.getName() + " No-PvP area.", (byte)2);
                }
              }
            }
            else if ((fz.isName()) || (fz.isNamePopup()) || (fz.isNoBuild()) || (fz.isPremSpawnOnly())) {
              if ((newFocusZones == null) || (!newFocusZones.contains(fz))) {
                if ((fz.isName()) || (fz.isNoBuild()) || (fz.isPremSpawnOnly()))
                {
                  getCommunicator().sendSafeServerMessage("You leave the " + fz.getName() + " area.", (byte)2);
                }
                else
                {
                  SimplePopup sp = new SimplePopup(this, "Leaving " + fz.getName(), "You leave the " + fz.getName() + " area.");
                  sp.sendQuestion();
                }
              }
            }
          }
        }
        if (!WurmCalendar.isSeasonWinter())
        {
          HiveZone newHiveZone = Zones.getHiveZoneAt(this.currentTile.tilex, this.currentTile.tiley, isOnSurface());
          boolean newHiveClose = newHiveZone == null ? false : newHiveZone.isClose(this.currentTile.tilex, this.currentTile.tiley);
          boolean domestic = newHiveZone != null;
          if ((oldHiveClose) && (!newHiveClose)) {
            getCommunicator().sendSafeServerMessage("The sounds of bees decreases as you move further away from the hive.", (byte)(domestic ? 0 : 2));
          }
          if ((oldHiveZone == null) && (newHiveZone != null)) {
            getCommunicator().sendSafeServerMessage("You hear bees, maybe you are getting close to a hive.", (byte)(domestic ? 0 : 2));
          } else if ((oldHiveZone != null) && (newHiveZone == null)) {
            getCommunicator().sendSafeServerMessage("The sounds of bees disappears in the distance.", 
              (byte)(oldHiveZone.getCurrentHive().getTemplateId() == 1175 ? 0 : 2));
          }
          if ((!oldHiveClose) && (newHiveClose)) {
            if (newHiveZone.getCurrentHive().hasTwoQueens()) {
              getCommunicator().sendSafeServerMessage("The bees noise is getting louder, sounds like there is unusual activity in the hive.", (byte)(domestic ? 0 : 2));
            } else {
              getCommunicator().sendSafeServerMessage("The bees noise is getting louder, maybe you are getting closer to their hive.", (byte)(domestic ? 0 : 2));
            }
          }
        }
        this.isInDuelRing = false;
        Item ring = Zones.isWithinDuelRing(this.currentTile.tilex, this.currentTile.tiley, this.currentTile.isOnSurface());
        if (ring != null)
        {
          Kingdom k = Kingdoms.getKingdom(ring.getAuxData());
          if (k != null)
          {
            if (ring.getAuxData() == getKingdomId()) {
              this.isInDuelRing = true;
            }
            if (!wasInDuelRing)
            {
              getCommunicator().sendAlertServerMessage("You enter the duelling area of " + k.getName() + ".", (byte)4);
              if (this.isInDuelRing) {
                getCommunicator().sendAlertServerMessage("People from your own kingdom may slay you here without penalty.", (byte)4);
              }
            }
          }
        }
        else if (wasInDuelRing)
        {
          getCommunicator().sendSafeServerMessage("You leave the duelling area.", (byte)2);
        }
        if (!Servers.localServer.HOMESERVER) {
          if (isOnSurface()) {
            if ((getFaith() > 0.0F) && (Server.rand.nextInt(100) < getFaith())) {
              if (EndGameItems.getArtifactAtTile(this.currentTile.tilex, this.currentTile.tiley) != null) {
                if (getDeity() != null) {
                  getCommunicator().sendSafeServerMessage(
                    getDeity().name + " urges you to deeply investigate the area!");
                }
              }
            }
          }
        }
      }
      if ((isPlayer()) && (!this.currentTile.isTransition)) {
        if ((getVisionArea() != null) && (getVisionArea().isInitialized())) {
          checkOpenMineDoor();
        }
      }
    }
    if (this.currentTile != null)
    {
      checkInvisDetection();
      boolean hostilePerimeter = false;
      if (isPlayer())
      {
        Village lVill = Villages.getVillageWithPerimeterAt(getTileX(), getTileY(), true);
        if (lVill != null) {
          if ((lVill.kingdom == getKingdomId()) && (lVill.isEnemy(this)))
          {
            if (!this.inHostilePerimeter) {
              getCommunicator().sendAlertServerMessage("You are now within the hostile perimeter of " + lVill
                .getName() + " and will be attacked by kingdom guards.", (byte)4);
            }
            hostilePerimeter = true;
          }
        }
      }
      if ((!hostilePerimeter) && (this.inHostilePerimeter))
      {
        getCommunicator().sendSafeServerMessage("You are now outside the hostile perimeters.");
        this.inHostilePerimeter = false;
      }
      if (hostilePerimeter) {
        this.inHostilePerimeter = true;
      }
      if (isPlayer()) {
        MissionTriggers.activateTriggerPlate(this, this.currentTile.tilex, this.currentTile.tiley, getLayer());
      }
    }
    return true;
  }
  
  public final boolean isInOwnDuelRing()
  {
    return this.isInDuelRing;
  }
  
  public final boolean hasOpenedMineDoor(int tilex, int tiley)
  {
    if (this.openedTiles == null) {
      return false;
    }
    for (int[] openedTile : this.openedTiles) {
      if ((openedTile[0] == tilex) && (openedTile[1] == tiley)) {
        return true;
      }
    }
    return false;
  }
  
  public void checkOpenMineDoor()
  {
    if (this.currentTile != null)
    {
      Set<int[]> oldM = Terraforming.getAllMineDoors(this.currentTile.tilex, this.currentTile.tiley);
      if (oldM != null) {
        for (int[] checkedTile : oldM) {
          if (!hasOpenedMineDoor(checkedTile[0], checkedTile[1])) {
            try
            {
              boolean ok = false;
              MineDoorPermission md = MineDoorPermission.getPermission(checkedTile[0], checkedTile[1]);
              if (md != null) {
                if (md.mayPass(this))
                {
                  ok = true;
                  if (isPlayer())
                  {
                    VolaTile tile = Zones.getOrCreateTile(checkedTile[0], checkedTile[1], true);
                    if (getEnemyPresense() > 0) {
                      if ((tile == null) || (tile.getVillage() == null)) {
                        md.setClosingTime(System.currentTimeMillis() + (
                          Servers.isThisAChaosServer() ? 30000L : 120000L));
                      }
                    }
                  }
                }
                else if (md.isWideOpen())
                {
                  ok = true;
                }
              }
              if (ok)
              {
                if (this.openedTiles == null) {
                  this.openedTiles = new LinkedList();
                }
                this.openedTiles.add(checkedTile);
                getMovementScheme().touchFreeMoveCounter();
                getVisionArea().checkCaves(false);
                getCommunicator().sendTileDoor((short)checkedTile[0], (short)checkedTile[1], true);
                md.open(this);
              }
            }
            catch (IOException localIOException) {}
          }
        }
      }
    }
  }
  
  public Creature(CreatureTemplate aTemplate)
    throws Exception
  {
    this();
    this.template = aTemplate;
    getMovementScheme().initalizeModifiersWithTemplate();
    this.name = aTemplate.getName();
    this.skills = aTemplate.getSkills();
  }
  
  public Item getBestLightsource()
  {
    return this.bestLightsource;
  }
  
  public Item getBestCompass()
  {
    return this.bestCompass;
  }
  
  public Item getBestToolbelt()
  {
    return this.bestToolbelt;
  }
  
  public Item getBestBeeSmoker()
  {
    return this.bestBeeSmoker;
  }
  
  public void setBestLightsource(@Nullable Item item, boolean override)
  {
    if ((override) || ((getVisionArea() != null) && (getVisionArea().isInitialized())))
    {
      this.bestLightsource = item;
      this.lightSourceChanged = true;
    }
  }
  
  public void setBestCompass(Item item)
  {
    this.bestCompass = item;
  }
  
  public void setBestToolbelt(@Nullable Item item)
  {
    this.bestToolbelt = item;
  }
  
  public void setBestBeeSmoker(Item item)
  {
    this.bestBeeSmoker = item;
  }
  
  public void resetCompassLantern()
  {
    this.bestCompass = null;
    this.bestToolbelt = null;
    if ((this.bestLightsource != null) && ((!this.bestLightsource.isOnFire()) || (this.bestLightsource.getOwnerId() != getWurmId())))
    {
      this.bestLightsource = null;
      this.lightSourceChanged = true;
    }
    this.bestBeeSmoker = null;
  }
  
  public void pollToolbelt()
  {
    if ((this.bestToolbelt != null) && (this.lastSentToolbelt != this.bestToolbelt.getWurmId()))
    {
      getCommunicator().sendToolbelt(this.bestToolbelt);
      this.lastSentToolbelt = this.bestToolbelt.getWurmId();
    }
    else if ((this.bestToolbelt == null) && (this.lastSentToolbelt != 0L))
    {
      getCommunicator().sendToolbelt(this.bestToolbelt);
      this.lastSentToolbelt = 0L;
    }
  }
  
  public void resetLastSentToolbelt()
  {
    this.lastSentToolbelt = 0L;
  }
  
  public void pollCompassLantern()
  {
    if (!this.lastSentHasCompass)
    {
      if (this.bestCompass != null)
      {
        getCommunicator().sendCompass(this.bestCompass);
        this.lastSentHasCompass = true;
      }
    }
    else if (this.bestCompass == null)
    {
      getCommunicator().sendCompass(this.bestCompass);
      this.lastSentHasCompass = false;
    }
    pollToolbelt();
    if (this.lightSourceChanged == true)
    {
      if (this.bestLightsource != null)
      {
        if (getCurrentTile() != null) {
          getCurrentTile().setHasLightSource(this, this.bestLightsource);
        }
      }
      else if (this.bestLightsource == null) {
        if (getCurrentTile() != null)
        {
          getCurrentTile().setHasLightSource(this, null);
          this.isLit = false;
        }
      }
      this.lightSourceChanged = false;
    }
  }
  
  public boolean isMute()
  {
    return false;
  }
  
  public boolean hasSleepBonus()
  {
    return false;
  }
  
  public void setOpponent(@Nullable Creature _opponent)
  {
    if ((_opponent != null) && (this.target == -10L)) {
      if (!isPrey()) {
        setTarget(_opponent.getWurmId(), true);
      }
    }
    if ((_opponent != null) && (_opponent.getAttackers() >= _opponent.getMaxGroupAttackSize())) {
      if ((!_opponent.isPlayer()) || (isPlayer())) {
        return;
      }
    }
    if (this.opponent != _opponent) {
      if ((_opponent != null) && (isPlayer()) && (_opponent.isPlayer()))
      {
        this.battle = Battles.getBattleFor(this, _opponent);
        this.battle.addEvent(new BattleEvent((short)-1, getName(), _opponent.getName()));
      }
    }
    this.opponent = _opponent;
    if (this.opponent != null)
    {
      this.opponent.getCommunicator().changeAttitude(getWurmId(), getAttitude(this.opponent));
      if (!this.opponent.equals(this.lastOpponent))
      {
        resetWeaponsUsed();
        resetAttackUsed();
        getCombatHandler().setCurrentStance(-1, (byte)0);
        this.lastOpponent = this.opponent;
        this.combatRound = 0;
        if ((isPlayer()) && (this.opponent.isPlayer()))
        {
          if ((this.opponent.getKingdomId() != getKingdomId()) && (getKingdomId() != 0))
          {
            Kingdom k = Kingdoms.getKingdom(getKingdomId());
            k.lastConfrontationTileX = getTileX();
            k.lastConfrontationTileY = getTileY();
          }
          if (getDeity() != null)
          {
            getDeity().lastConfrontationTileX = getTileX();
            getDeity().lastConfrontationTileY = getTileY();
          }
        }
      }
    }
    else
    {
      resetWeaponsUsed();
      resetAttackUsed();
    }
    this.status.sendStateString();
    if (isPlayer()) {
      if (this.opponent == null)
      {
        getCommunicator().sendSpecialMove((short)-1, "N/A");
        getCommunicator().sendCombatOptions(CombatHandler.NO_COMBAT_OPTIONS, (short)-1);
        getCombatHandler().setSentAttacks(false);
      }
      else
      {
        getCombatHandler().setSentAttacks(false);
        getCombatHandler().calcAttacks(false);
      }
    }
  }
  
  public boolean mayRaiseFightLevel()
  {
    if (this.combatRound > 2) {
      if (this.fightlevel < 5)
      {
        if (this.fightlevel == 0) {
          return true;
        }
        if (this.fightlevel == 1) {
          return getFightingSkill().getKnowledge(0.0D) > 30.0D;
        }
        if (this.fightlevel == 2) {
          return getBodyControl() > 25.0D;
        }
        if (this.fightlevel == 3) {
          return getMindSpeed().getKnowledge(0.0D) > 25.0D;
        }
        if (this.fightlevel == 4) {
          return getSoulDepth().getKnowledge(0.0D) > 25.0D;
        }
      }
    }
    return false;
  }
  
  public CombatHandler getCombatHandler()
  {
    if (this.combatHandler == null) {
      this.combatHandler = new CombatHandler(this);
    }
    return this.combatHandler;
  }
  
  public void removeTarget(long targetId)
  {
    this.actions.removeTarget(targetId);
  }
  
  public boolean isPlayer()
  {
    return false;
  }
  
  public boolean isLegal()
  {
    return true;
  }
  
  public Item getDraggedItem()
  {
    return this.movementScheme.getDraggedItem();
  }
  
  public void setDraggedItem(@Nullable Item dragged)
  {
    this.movementScheme.setDraggedItem(dragged);
  }
  
  public Door getCurrentDoor()
  {
    return this.currentDoor;
  }
  
  public void setCurrentDoor(@Nullable Door door)
  {
    this.currentDoor = door;
  }
  
  public Battle getBattle()
  {
    return this.battle;
  }
  
  public void setBattle(@Nullable Battle batle)
  {
    this.battle = batle;
  }
  
  public void setCitizenVillage(@Nullable Village newVillage)
  {
    this.citizenVillage = newVillage;
    if (this.citizenVillage != null)
    {
      setVillageSkillModifier(this.citizenVillage.getSkillModifier());
      if (this.citizenVillage.kingdom != getKingdomId()) {
        try
        {
          setKingdomId(this.citizenVillage.kingdom, true);
        }
        catch (IOException localIOException) {}
      }
    }
    else
    {
      setVillageSkillModifier(0.0D);
    }
    refreshAttitudes();
  }
  
  public Village getCitizenVillage()
  {
    return this.citizenVillage;
  }
  
  public void setFightingStyle(byte style)
  {
    setFightingStyle(style, false);
  }
  
  public void setFightingStyle(byte style, boolean loading)
  {
    String mess = "";
    if (style == 2) {
      mess = "You will now fight defensively.";
    } else if (style == 1) {
      mess = "You will now fight aggressively.";
    } else {
      mess = "You will now fight normally.";
    }
    if (isFighting()) {
      getCommunicator().sendCombatNormalMessage(mess);
    } else {
      getCommunicator().sendNormalServerMessage(mess);
    }
    getCombatHandler().setFightingStyle(style);
    this.fightStyle = style;
    getCommunicator().sendFightStyle(this.fightStyle);
    this.status.sendStateString();
    if (!loading) {
      saveFightMode(this.fightStyle);
    }
  }
  
  public byte getFightStyle()
  {
    return this.fightStyle;
  }
  
  public float getBaseCombatRating()
  {
    if (isPlayer()) {
      return this.template.getBaseCombatRating();
    }
    if (getLoyalty() > 0.0F) {
      return (isReborn() ? 0.7F : 0.5F) * this.template.getBaseCombatRating() * this.status.getBattleRatingTypeModifier();
    }
    return this.template.getBaseCombatRating() * this.status.getBattleRatingTypeModifier();
  }
  
  public float getBonusCombatRating()
  {
    return this.template.getBonusCombatRating();
  }
  
  public final boolean isOkToKillBy(Creature attacker)
  {
    if ((!Servers.localServer.HOMESERVER) && (!Servers.localServer.isChallengeServer())) {
      return true;
    }
    if (!attacker.isFriendlyKingdom(getKingdomId())) {
      return true;
    }
    if (Servers.isThisAChaosServer()) {
      return true;
    }
    if (getKingdomTemplateId() == 3) {
      return true;
    }
    if (hasAttackedUnmotivated()) {
      return true;
    }
    if (attacker.isDuelOrSpar(this)) {
      return true;
    }
    if (getReputation() < 0) {
      return true;
    }
    if (isInOwnDuelRing()) {
      return true;
    }
    if (Zones.isWithinDuelRing(getTileX(), getTileY(), true) != null) {
      return true;
    }
    if (attacker.getCitizenVillage() != null)
    {
      if (attacker.getCitizenVillage().isEnemy(getCitizenVillage())) {
        return true;
      }
      if (Servers.localServer.PVPSERVER)
      {
        Village v = Villages.getVillageWithPerimeterAt(attacker.getTileX(), attacker.getTileY(), true);
        if ((v == attacker.getCitizenVillage()) && (getCurrentVillage() == v)) {
          return true;
        }
        if (attacker.getCitizenVillage().isEnemy(this)) {
          return true;
        }
        if (attacker.getCitizenVillage().isAlly(getCitizenVillage())) {
          return false;
        }
      }
    }
    if (isInPvPZone()) {
      return true;
    }
    return false;
  }
  
  public final boolean isEnemyOnChaos(Creature creature)
  {
    if (Servers.isThisAChaosServer()) {
      if (isInSameAlliance(creature)) {
        return false;
      }
    }
    return false;
  }
  
  public final boolean isInSameAlliance(Creature creature)
  {
    if (getCitizenVillage() == null) {
      return false;
    }
    if (creature.getCitizenVillage() == null) {
      return false;
    }
    return getCitizenVillage().getAllianceNumber() == creature.getCitizenVillage().getAllianceNumber();
  }
  
  public boolean hasAttackedUnmotivated()
  {
    if (isDominated()) {
      if (getDominator() != null) {
        return getDominator().hasAttackedUnmotivated();
      }
    }
    SpellEffects effs = getSpellEffects();
    if (effs == null) {
      return false;
    }
    SpellEffect eff = effs.getSpellEffect((byte)64);
    if (eff != null) {
      return true;
    }
    return false;
  }
  
  public void setUnmotivatedAttacker()
  {
    if (isNpc()) {
      return;
    }
    if ((!Servers.isThisAPvpServer()) || (!Servers.localServer.HOMESERVER)) {
      return;
    }
    if (getKingdomTemplateId() != 3)
    {
      SpellEffects effs = getSpellEffects();
      if (effs == null) {
        effs = createSpellEffects();
      }
      SpellEffect eff = effs.getSpellEffect((byte)64);
      if (eff == null)
      {
        setVisible(false);
        logger.log(Level.INFO, getName() + " set unmotivated attacker at ", new Exception());
        
        eff = new SpellEffect(getWurmId(), (byte)64, 100.0F, (int)(Servers.isThisATestServer() ? 120L : 1800L), (byte)1, (byte)1, true);
        
        effs.addSpellEffect(eff);
        setVisible(true);
        getCommunicator()
          .sendAlertServerMessage("You have received the hunted status and may be attacked without penalty for half an hour.");
        if (getCitizenVillage() != null) {
          getCitizenVillage().setVillageRep(getCitizenVillage().getVillageReputation() + 10);
        }
        Achievements ach = Achievements.getAchievementObject(getWurmId());
        if ((ach != null) && (ach.getAchievement(369) != null))
        {
          achievement(373);
          removeTitle(Titles.Title.Knigt);
          addTitle(Titles.Title.FallenKnight);
        }
      }
      else
      {
        eff.setTimeleft(1800);
        sendUpdateSpellEffect(eff);
      }
    }
  }
  
  public void addAttacker(Creature creature)
  {
    if (!isDuelOrSpar(creature))
    {
      if ((isSpiritGuard()) && (getCitizenVillage() != null) && (!getCitizenVillage().containsTarget(creature))) {
        getCitizenVillage().addTarget(creature);
      }
      if (this.attackers == null) {
        this.attackers = new HashMap();
      }
      if (creature.isPlayer())
      {
        if (!isInvulnerable()) {
          setSecondsToLogout(getSecondsToLogout());
        }
        if (isPlayer())
        {
          if (!isOkToKillBy(creature)) {
            if (!creature.hasBeenAttackedBy(getWurmId())) {
              creature.setUnmotivatedAttacker();
            }
          }
        }
        else if (isRidden())
        {
          if ((creature.getCitizenVillage() == null) || (getCurrentVillage() != creature.getCitizenVillage())) {
            for (Long riderLong : getRiders()) {
              try
              {
                Creature rider = Server.getInstance().getCreature(riderLong.longValue());
                if (rider != creature)
                {
                  if ((!creature.hasBeenAttackedBy(rider.getWurmId())) && 
                    (!creature.hasBeenAttackedBy(getWurmId()))) {
                    if (!rider.isOkToKillBy(creature)) {
                      creature.setUnmotivatedAttacker();
                    }
                  }
                  rider.addAttacker(creature);
                }
              }
              catch (NoSuchCreatureException localNoSuchCreatureException) {}catch (NoSuchPlayerException localNoSuchPlayerException) {}
            }
          }
        }
        else if (getHitched() != null)
        {
          if (Servers.localServer.HOMESERVER) {
            if ((creature.getCitizenVillage() == null) || (getCurrentVillage() != creature.getCitizenVillage())) {
              if (!getHitched().isCreature()) {
                try
                {
                  Item i = Items.getItem(getHitched().wurmid);
                  long ownid = i.getLastOwnerId();
                  try
                  {
                    if (ownid != creature.getWurmId())
                    {
                      byte kingd = Players.getInstance().getKingdomForPlayer(ownid);
                      if ((creature.isFriendlyKingdom(kingd)) && 
                        (!creature.hasBeenAttackedBy(ownid))) {
                        creature.setUnmotivatedAttacker();
                      }
                    }
                  }
                  catch (Exception localException) {}
                }
                catch (NoSuchItemException nsi)
                {
                  logger.log(Level.INFO, getHitched().wurmid + " no such item:", nsi);
                }
              }
            }
          }
        }
        else if (isDominated())
        {
          if (Servers.localServer.HOMESERVER)
          {
            this.attackers.put(Long.valueOf(creature.getWurmId()), Long.valueOf(System.currentTimeMillis()));
            if ((creature.isFriendlyKingdom(getKingdomId())) && 
              (!creature.hasBeenAttackedBy(this.dominator)) && 
              (!creature.hasBeenAttackedBy(getWurmId()))) {
              if (creature != getDominator()) {
                creature.setUnmotivatedAttacker();
              }
            }
          }
        }
        else if (getCurrentVillage() != null) {
          if (Servers.localServer.HOMESERVER)
          {
            Brand brand = Creatures.getInstance().getBrand(getWurmId());
            if (brand != null) {
              try
              {
                Village villageBrand = Villages.getVillage((int)brand.getBrandId());
                if (getCurrentVillage() == villageBrand) {
                  if (creature.getCitizenVillage() != villageBrand) {
                    if (!villageBrand.isEnemy(creature.getCitizenVillage())) {
                      creature.setUnmotivatedAttacker();
                    }
                  }
                }
              }
              catch (NoSuchVillageException nsv)
              {
                brand.deleteBrand();
              }
            }
          }
        }
        if (!creature.hasAddedToAttack) {
          this.attackers.put(Long.valueOf(creature.getWurmId()), Long.valueOf(System.currentTimeMillis()));
        }
      }
      else if (!creature.hasAddedToAttack)
      {
        this.attackers.put(Long.valueOf(creature.getWurmId()), Long.valueOf(System.currentTimeMillis()));
      }
      if (!creature.hasAddedToAttack)
      {
        this.numattackers += 1;
        creature.hasAddedToAttack = true;
      }
    }
  }
  
  public int getSecondsToLogout()
  {
    return 300;
  }
  
  public boolean hasBeenAttackedBy(long _id)
  {
    if (!isPlayer()) {
      return false;
    }
    if (this.attackers == null) {
      return false;
    }
    Long l = Long.valueOf(_id);
    return this.attackers.keySet().contains(l);
  }
  
  public long[] getLatestAttackers()
  {
    if ((this.attackers != null) && (this.attackers.size() > 0))
    {
      Long[] lKeys = (Long[])this.attackers.keySet().toArray(new Long[this.attackers.size()]);
      long[] toReturn = new long[lKeys.length];
      for (int x = 0; x < toReturn.length; x++) {
        toReturn[x] = lKeys[x].longValue();
      }
      return toReturn;
    }
    return EMPTY_LONG_PRIMITIVE_ARRAY;
  }
  
  protected long[] getAttackerIds()
  {
    if (this.attackers == null) {
      return EMPTY_LONG_PRIMITIVE_ARRAY;
    }
    Long[] longs = (Long[])this.attackers.keySet().toArray(new Long[this.attackers.size()]);
    long[] ll = new long[longs.length];
    for (int x = 0; x < longs.length; x++) {
      ll[x] = longs[x].longValue();
    }
    return ll;
  }
  
  public void trimAttackers(boolean delete)
  {
    if (delete)
    {
      this.attackers = null;
    }
    else if ((this.attackers != null) && (this.attackers.size() > 0))
    {
      Long[] lKeys = (Long[])this.attackers.keySet().toArray(new Long[this.attackers.size()]);
      for (Long lLKey : lKeys)
      {
        Long time = (Long)this.attackers.get(lLKey);
        if (WurmId.getType(lLKey.longValue()) == 1)
        {
          if (System.currentTimeMillis() - time.longValue() > 180000L) {
            this.attackers.remove(lLKey);
          }
        }
        else if (System.currentTimeMillis() - time.longValue() > 300000L) {
          this.attackers.remove(lLKey);
        }
      }
      if (this.attackers.isEmpty()) {
        this.attackers = null;
      }
    }
  }
  
  public void setMilked(boolean aMilked)
  {
    this.milked = aMilked;
  }
  
  public void setSheared(boolean isSheared)
  {
    this.sheared = isSheared;
  }
  
  public boolean isMilked()
  {
    return this.milked;
  }
  
  public boolean isSheared()
  {
    return this.sheared;
  }
  
  public int getAttackers()
  {
    return this.numattackers;
  }
  
  public final boolean hasBeenAttackedWithin(int seconds)
  {
    if (this.attackers != null) {
      for (Long l : this.attackers.values()) {
        if (System.currentTimeMillis() - l.longValue() < seconds * 1000) {
          return true;
        }
      }
    }
    return false;
  }
  
  public void setCurrentVillage(Village newVillage)
  {
    if (this.currentVillage == null)
    {
      if (newVillage != null)
      {
        getCommunicator().sendNormalServerMessage("You enter " + newVillage.getName() + ".");
        newVillage.checkIfRaiseAlert(this);
        if ((isPlayer()) && (getHighwayPathDestination().length() > 0) && (getHighwayPathDestination().equalsIgnoreCase(newVillage.getName())))
        {
          getCommunicator().sendNormalServerMessage("You have arrived at your destination.");
          setLastWaystoneChecked(-10L);
          setHighwayPath("", null);
          if (isPlayer()) {
            for (Item waystone : Items.getWaystones())
            {
              VolaTile vt = Zones.getTileOrNull(waystone.getTileX(), waystone.getTileY(), waystone.isOnSurface());
              if (vt != null) {
                for (VirtualZone vz : vt.getWatchers()) {
                  try
                  {
                    if (vz.getWatcher().getWurmId() == getWurmId())
                    {
                      getCommunicator().sendWaystoneData(waystone);
                      break;
                    }
                  }
                  catch (Exception e)
                  {
                    logger.log(Level.WARNING, e.getMessage(), e);
                  }
                }
              }
            }
          }
        }
        if (getLogger() != null) {
          getLogger().log(Level.INFO, getName() + " enters " + newVillage.getName() + ".");
        }
      }
    }
    else if (!this.currentVillage.equals(newVillage))
    {
      if (newVillage == null)
      {
        getCommunicator().sendNormalServerMessage("You leave " + this.currentVillage.getName() + ".");
        if (!isFighting()) {
          this.currentVillage.removeTarget(this);
        }
        if (getLogger() != null) {
          getLogger().log(Level.INFO, getName() + " leaves " + this.currentVillage.getName() + ".");
        }
      }
      if (newVillage != null)
      {
        getCommunicator().sendNormalServerMessage("You enter " + newVillage.getName() + ".");
        newVillage.checkIfRaiseAlert(this);
        if (getLogger() != null) {
          getLogger().log(Level.INFO, getName() + " enters " + newVillage.getName() + ".");
        }
      }
    }
    this.currentVillage = newVillage;
  }
  
  public Village getCurrentVillage()
  {
    return this.currentVillage;
  }
  
  public boolean isVisible()
  {
    return this.status.visible;
  }
  
  public void setVisible(boolean visible)
  {
    this.status.visible = visible;
    if (getStatus().offline)
    {
      this.status.visible = false;
    }
    else
    {
      int tilex = getTileX();
      int tiley = getTileY();
      try
      {
        Zone zone = Zones.getZone(tilex, tiley, isOnSurface());
        VolaTile tile = zone.getOrCreateTile(tilex, tiley);
        if (visible) {
          try
          {
            if (!isDead()) {
              tile.makeVisible(this);
            }
          }
          catch (NoSuchCreatureException nsc)
          {
            logger.log(Level.INFO, nsc.getMessage() + " " + this.id + ", " + this.name, nsc);
          }
          catch (NoSuchPlayerException nsp)
          {
            logger.log(Level.INFO, nsp.getMessage() + " " + this.id + ", " + this.name, nsp);
          }
        } else {
          tile.makeInvisible(this);
        }
      }
      catch (NoSuchZoneException nsz)
      {
        logger.log(Level.INFO, getName() + " outside of bounds when going invis.");
      }
      if (isPlayer()) {
        if (!this.status.visible) {
          Players.getInstance().partChannels((Player)this);
        } else {
          Players.getInstance().joinChannels((Player)this);
        }
      }
      this.status.sendStateString();
    }
  }
  
  public void calculateZoneBonus(int tilex, int tiley, boolean surfaced)
  {
    try
    {
      if (Servers.localServer.HOMESERVER)
      {
        if (this.currentKingdom == 0) {
          this.currentKingdom = Servers.localServer.KINGDOM;
        }
      }
      else {
        setCurrentKingdom(getCurrentKingdom());
      }
      this.zoneBonus = 0.0F;
      Deity deity = getDeity();
      if (deity != null)
      {
        FaithZone z = Zones.getFaithZone(tilex, tiley, surfaced);
        if (z != null)
        {
          if (z.getCurrentRuler() == deity)
          {
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
    }
    catch (NoSuchZoneException nsz)
    {
      logger.log(Level.WARNING, "No faith zone at " + tilex + "," + tiley + ", surf=" + surfaced);
    }
  }
  
  public boolean mustChangeTerritory()
  {
    return false;
  }
  
  protected byte getLastTaggedKingdom()
  {
    return this.currentKingdom;
  }
  
  public void setCurrentKingdom(byte newKingdom)
  {
    if (this.currentKingdom == 0)
    {
      if (newKingdom != 0)
      {
        getCommunicator().sendNormalServerMessage("You enter " + Kingdoms.getNameFor(newKingdom) + ".");
        if ((Servers.localServer.isChallengeOrEpicServer()) && (getLastTaggedKingdom() != newKingdom))
        {
          if (mustChangeTerritory()) {
            getCommunicator().sendSafeServerMessage("You feel an energy boost, as if " + 
              getDeity().getName() + " turns " + 
              getDeity().getHisHerItsString() + " eyes at you.");
          }
          setLastTaggedTerr(newKingdom);
        }
        if (newKingdom != getKingdomId()) {
          achievement(374);
        }
        if (this.musicPlayer != null) {
          if (this.musicPlayer.isItOkToPlaySong(true)) {
            if (newKingdom != getKingdomTemplateId())
            {
              if ((Kingdoms.getKingdomTemplateFor(newKingdom) == 3) && 
                (Kingdoms.getKingdomTemplateFor(getKingdomId()) != 3)) {
                this.musicPlayer.checkMUSIC_TERRITORYHOTS_SND();
              } else if (Kingdoms.getKingdomTemplateFor(getKingdomId()) == 3) {
                this.musicPlayer.checkMUSIC_TERRITORYWL_SND();
              }
            }
            else {
              playAnthem();
            }
          }
        }
      }
    }
    else if (newKingdom != this.currentKingdom)
    {
      if (newKingdom == 0) {
        getCommunicator().sendNormalServerMessage("You leave " + Kingdoms.getNameFor(this.currentKingdom) + ".");
      }
      if (newKingdom != 0)
      {
        getCommunicator().sendNormalServerMessage("You enter " + Kingdoms.getNameFor(newKingdom) + ".");
        if (getPower() <= 0) {
          if (this.musicPlayer != null) {
            if (this.musicPlayer.isItOkToPlaySong(true)) {
              if (newKingdom != getKingdomId())
              {
                achievement(374);
                if ((newKingdom == 3) && (getKingdomId() != 3)) {
                  this.musicPlayer.checkMUSIC_TERRITORYHOTS_SND();
                } else if (getKingdomId() == 3) {
                  this.musicPlayer.checkMUSIC_TERRITORYWL_SND();
                }
                Appointments p = King.getCurrentAppointments(newKingdom);
                if (p != null)
                {
                  long secret = p.getOfficialForId(1500);
                  if (secret > 0L) {
                    try
                    {
                      Creature c = Server.getInstance().getCreature(secret);
                      if (c.getMindLogical().skillCheck(40.0D, 0.0D, false, 1.0F) > 0.0D) {
                        c.getCommunicator().sendNormalServerMessage("Your informers relay information that " + 
                          getName() + " has entered your territory.");
                      }
                    }
                    catch (Exception localException) {}
                  }
                }
              }
              else
              {
                playAnthem();
              }
            }
          }
        }
      }
    }
    this.currentKingdom = newKingdom;
  }
  
  public void setCurrentDeity(Deity deity)
  {
    if (deity != null)
    {
      if (this.currentDeity != deity.number)
      {
        this.currentDeity = ((byte)deity.number);
        getCommunicator().sendNormalServerMessage("You feel the presence of " + deity.name + ".");
      }
    }
    else if (this.currentDeity != 0)
    {
      getCommunicator().sendNormalServerMessage("You no longer feel the presence of " + 
        Deities.getDeity(this.currentDeity).name + ".");
      this.currentDeity = 0;
    }
  }
  
  public Creature(long aId)
    throws Exception
  {
    this();
    setWurmId(aId, 0.0F, 0.0F, 0.0F, 0);
    this.skills = SkillsFactory.createSkills(aId);
  }
  
  public final void loadTemplate()
  {
    this.template = this.status.getTemplate();
    getMovementScheme().initalizeModifiersWithTemplate();
    this.breedCounter = ((Servers.isThisAPvpServer() ? 900 : 2000) + Server.rand.nextInt(1000));
  }
  
  public Creature setWurmId(long aId, float posx, float posy, float aRot, int layer)
    throws Exception
  {
    this.id = aId;
    this.status = CreatureStatusFactory.createCreatureStatus(this, posx, posy, aRot, layer);
    
    getMovementScheme().setBridgeId(getBridgeId());
    return this;
  }
  
  public void postLoad()
    throws Exception
  {
    loadSkills();
    if ((!isDead()) && (!isOffline())) {
      createVisionArea();
    }
    if (getTemplate().getCreatureAI() != null) {
      getTemplate().getCreatureAI().creatureCreated(this);
    }
  }
  
  public TradeHandler getTradeHandler()
  {
    if (this.tradeHandler == null) {
      this.tradeHandler = new TradeHandler(this, getStatus().getTrade());
    }
    return this.tradeHandler;
  }
  
  public void endTrade()
  {
    this.tradeHandler.end();
    this.tradeHandler = null;
  }
  
  public void addItemTaken(Item item)
  {
    if (this.itemsTaken == null) {
      this.itemsTaken = new HashSet();
    }
    this.itemsTaken.add(item);
  }
  
  public void addItemDropped(Item item)
  {
    checkTheftWarnQuestion();
    if (this.itemsDropped == null) {
      this.itemsDropped = new HashSet();
    }
    this.itemsDropped.add(item);
  }
  
  protected void sendItemsTaken()
  {
    if (this.itemsTaken != null)
    {
      Map<Integer, Integer> diffItems = new HashMap();
      Map<String, Integer> foodItems = new HashMap();
      for (Item item : this.itemsTaken) {
        if (item.isFood())
        {
          String name = item.getName();
          if (foodItems.containsKey(name))
          {
            Integer num = (Integer)foodItems.get(name);
            int nums = num.intValue();
            nums++;
            foodItems.put(name, Integer.valueOf(nums));
          }
          else
          {
            foodItems.put(name, Integer.valueOf(1));
          }
        }
        else
        {
          Integer templateId = Integer.valueOf(item.getTemplateId());
          if (diffItems.containsKey(templateId))
          {
            Integer num = (Integer)diffItems.get(templateId);
            int nums = num.intValue();
            nums++;
            diffItems.put(templateId, Integer.valueOf(nums));
          }
          else
          {
            diffItems.put(templateId, Integer.valueOf(1));
          }
        }
      }
      for (Integer key : diffItems.keySet()) {
        try
        {
          ItemTemplate lTemplate = ItemTemplateFactory.getInstance().getTemplate(key.intValue());
          Integer num = (Integer)diffItems.get(key);
          int number = num.intValue();
          if (number == 1)
          {
            getCommunicator().sendNormalServerMessage("You get " + lTemplate.getNameWithGenus() + ".");
            if (isVisible()) {
              Server.getInstance().broadCastAction(this.name + " gets " + lTemplate.getNameWithGenus() + ".", this, 5);
            }
          }
          else
          {
            getCommunicator().sendNormalServerMessage("You get " + 
              StringUtilities.getWordForNumber(number) + " " + lTemplate.sizeString + lTemplate
              .getPlural() + ".");
            if (isVisible()) {
              Server.getInstance().broadCastAction(this.name + " gets " + 
                StringUtilities.getWordForNumber(number) + " " + lTemplate.sizeString + lTemplate
                .getPlural() + ".", this, 5);
            }
          }
        }
        catch (NoSuchTemplateException nst)
        {
          logger.log(Level.WARNING, nst.getMessage(), nst);
        }
      }
      for (String key : foodItems.keySet())
      {
        Integer num = (Integer)foodItems.get(key);
        int number = num.intValue();
        if (number == 1)
        {
          getCommunicator().sendNormalServerMessage("You get " + StringUtilities.addGenus(key) + ".");
          if (isVisible()) {
            Server.getInstance().broadCastAction(this.name + " gets " + StringUtilities.addGenus(key) + ".", this, 5);
          }
        }
        else
        {
          getCommunicator().sendNormalServerMessage("You get " + StringUtilities.getWordForNumber(number) + " " + key + ".");
          if (isVisible()) {
            Server.getInstance().broadCastAction(this.name + " gets " + StringUtilities.getWordForNumber(number) + " " + key + ".", this, 5);
          }
        }
      }
      this.itemsTaken = null;
    }
  }
  
  public boolean isIgnored(long playerId)
  {
    return false;
  }
  
  public void sendItemsDropped()
  {
    if (this.itemsDropped != null)
    {
      Map<Integer, Integer> diffItems = new HashMap();
      Map<String, Integer> foodItems = new HashMap();
      for (Item item : this.itemsDropped) {
        if (item.isFood())
        {
          String name = item.getName();
          if (foodItems.containsKey(name))
          {
            Integer num = (Integer)foodItems.get(name);
            int nums = num.intValue();
            nums++;
            foodItems.put(name, Integer.valueOf(nums));
          }
          else
          {
            foodItems.put(name, Integer.valueOf(1));
          }
        }
        else
        {
          Integer templateId = Integer.valueOf(item.getTemplateId());
          if (diffItems.containsKey(templateId))
          {
            Integer num = (Integer)diffItems.get(templateId);
            int nums = num.intValue();
            nums++;
            diffItems.put(templateId, Integer.valueOf(nums));
          }
          else
          {
            diffItems.put(templateId, Integer.valueOf(1));
          }
        }
      }
      for (Integer key : diffItems.keySet()) {
        try
        {
          ItemTemplate lTemplate = ItemTemplateFactory.getInstance().getTemplate(key.intValue());
          Integer num = (Integer)diffItems.get(key);
          int number = num.intValue();
          if (number == 1)
          {
            getCommunicator().sendNormalServerMessage("You drop " + lTemplate.getNameWithGenus() + ".");
            Server.getInstance().broadCastAction(this.name + " drops " + lTemplate.getNameWithGenus() + ".", this, 
              Math.max(3, lTemplate.getSizeZ() / 10));
          }
          else
          {
            getCommunicator().sendNormalServerMessage("You drop " + 
              StringUtilities.getWordForNumber(number) + " " + lTemplate.getPlural() + ".");
            Server.getInstance()
              .broadCastAction(this.name + " drops " + 
              StringUtilities.getWordForNumber(number) + " " + lTemplate
              .getPlural() + ".", this, 5);
          }
        }
        catch (NoSuchTemplateException nst)
        {
          logger.log(Level.WARNING, nst.getMessage(), nst);
        }
      }
      for (String key : foodItems.keySet())
      {
        Integer num = (Integer)foodItems.get(key);
        int number = num.intValue();
        if (number == 1)
        {
          getCommunicator().sendNormalServerMessage("You drop " + StringUtilities.addGenus(key) + ".");
          Server.getInstance().broadCastAction(this.name + " drops " + StringUtilities.addGenus(key) + ".", this, 5);
        }
        else
        {
          getCommunicator().sendNormalServerMessage("You drop " + 
            StringUtilities.getWordForNumber(number) + " " + key + ".");
          Server.getInstance().broadCastAction(this.name + " drops " + StringUtilities.getWordForNumber(number) + " " + key + ".", this, 5);
        }
      }
      this.itemsDropped = null;
    }
  }
  
  public String getNameWithGenus()
  {
    if ((isUnique()) || (isPlayer())) {
      return getName();
    }
    if (this.name.toLowerCase().compareTo(this.template.getName().toLowerCase()) != 0) {
      return "the " + getName();
    }
    if (this.template.isVowel(getName().substring(0, 1))) {
      return "an " + getName();
    }
    return "a " + getName();
  }
  
  public String getHoverText()
  {
    return "";
  }
  
  public void setTrade(@Nullable Trade trade)
  {
    this.status.setTrade(trade);
  }
  
  public Trade getTrade()
  {
    return this.status.getTrade();
  }
  
  public boolean isTrading()
  {
    return this.status.isTrading();
  }
  
  public boolean isLeadable(Creature potentialLeader)
  {
    if (this.hitchedTo != null) {
      return false;
    }
    if ((this.riders != null) && (this.riders.size() > 0)) {
      return false;
    }
    if (isDominated())
    {
      if (getDominator() != null) {
        return getDominator().equals(potentialLeader);
      }
      return false;
    }
    return this.template.isLeadable();
  }
  
  public boolean isOffline()
  {
    return getStatus().offline;
  }
  
  public boolean isLoggedOut()
  {
    return false;
  }
  
  public boolean isStayonline()
  {
    return getStatus().stayOnline;
  }
  
  public boolean setStayOnline(boolean stayOnline)
  {
    return getStatus().setStayOnline(stayOnline);
  }
  
  void setOffline(boolean offline)
  {
    getStatus().setOffline(offline);
  }
  
  public Creature getLeader()
  {
    return this.leader;
  }
  
  public void setWounded()
  {
    removeIllusion();
    if (this.damageCounter == 0) {
      addWoundMod();
    }
    playAnimation("wounded", false);
    this.damageCounter = ((short)(int)(30.0F * ItemBonus.getHurtingReductionBonus(this)));
    setStealth(false);
    getStatus().sendStateString();
  }
  
  private void addWoundMod()
  {
    getMovementScheme().addModifier(this.woundMoveMod);
    if (isPlayer()) {
      getCommunicator().sendAddSpellEffect(SpellEffectsEnum.WOUNDMOVE, 100000, 100.0F);
    }
  }
  
  public void removeWoundMod()
  {
    getMovementScheme().removeModifier(this.woundMoveMod);
    if (isPlayer()) {
      getCommunicator().sendRemoveSpellEffect(SpellEffectsEnum.WOUNDMOVE);
    }
  }
  
  public boolean isEncumbered()
  {
    return this.carriedWeight >= this.encumbered;
  }
  
  public boolean isMoveSlow()
  {
    return this.carriedWeight >= this.moveslow;
  }
  
  public boolean isCantMove()
  {
    return this.carriedWeight >= this.cantmove;
  }
  
  public int getMovePenalty()
  {
    if (isMoveSlow()) {
      return 5;
    }
    if (isEncumbered()) {
      return 10;
    }
    if (isCantMove()) {
      return 20;
    }
    return 0;
  }
  
  public final int getMoveSlow()
  {
    return this.moveslow;
  }
  
  private void setMoveLimits()
  {
    if (getPower() > 1)
    {
      this.moveslow = Integer.MAX_VALUE;
      this.encumbered = Integer.MAX_VALUE;
      this.cantmove = Integer.MAX_VALUE;
      if (this.movementScheme.stealthMod == null) {
        this.movementScheme.stealthMod = new DoubleValueModifier(-(80.0D - Math.min(79.0D, getBodyControl())) / 100.0D);
      } else {
        this.movementScheme.stealthMod.setModifier(-(80.0D - Math.min(79.0D, getBodyControl())) / 100.0D);
      }
    }
    else
    {
      try
      {
        Skill strength = this.skills.getSkill(102);
        
        this.moveslow = ((int)strength.getKnowledge(0.0D) * 2000);
        this.encumbered = ((int)strength.getKnowledge(0.0D) * 3500);
        this.cantmove = ((int)strength.getKnowledge(0.0D) * 7000);
        if (this.movementScheme.stealthMod == null) {
          this.movementScheme.stealthMod = new DoubleValueModifier(-(80.0D - Math.min(79.0D, getBodyControl())) / 100.0D);
        } else {
          this.movementScheme.stealthMod.setModifier(-(80.0D - Math.min(79.0D, getBodyControl())) / 100.0D);
        }
      }
      catch (NoSuchSkillException nss)
      {
        logger.log(Level.WARNING, "No strength skill for " + this, nss);
      }
    }
  }
  
  public void calcBaseMoveMod()
  {
    if (this.carriedWeight < this.moveslow)
    {
      this.movementScheme.setEncumbered(false);
      this.movementScheme.setBaseModifier(1.0F);
    }
    else if (this.carriedWeight >= this.cantmove)
    {
      this.movementScheme.setEncumbered(true);
      this.movementScheme.setBaseModifier(0.05F);
      getCommunicator().sendAlertServerMessage("You are encumbered and move extremely slow.");
    }
    else if (this.carriedWeight >= this.encumbered)
    {
      this.movementScheme.setEncumbered(false);
      this.movementScheme.setBaseModifier(0.25F);
    }
    else if (this.carriedWeight >= this.moveslow)
    {
      this.movementScheme.setEncumbered(false);
      this.movementScheme.setBaseModifier(0.75F);
    }
  }
  
  public void addCarriedWeight(int weight)
  {
    boolean canTriggerPlonk = false;
    if (isPlayer())
    {
      if (this.carriedWeight < this.moveslow)
      {
        if (this.carriedWeight + weight >= this.cantmove)
        {
          this.movementScheme.setEncumbered(true);
          this.movementScheme.setBaseModifier(0.05F);
          getCommunicator().sendAlertServerMessage("You are encumbered and move extremely slow.");
          canTriggerPlonk = true;
        }
        else if (this.carriedWeight + weight >= this.encumbered)
        {
          this.movementScheme.setBaseModifier(0.25F);
          canTriggerPlonk = true;
        }
        else if (this.carriedWeight + weight >= this.moveslow)
        {
          this.movementScheme.setBaseModifier(0.75F);
          canTriggerPlonk = true;
        }
      }
      else if (this.carriedWeight < this.encumbered)
      {
        if (this.carriedWeight + weight >= this.cantmove)
        {
          this.movementScheme.setEncumbered(true);
          this.movementScheme.setBaseModifier(0.05F);
          getCommunicator().sendAlertServerMessage("You are encumbered and move extremely slow.");
          canTriggerPlonk = true;
        }
        else if (this.carriedWeight + weight >= this.encumbered)
        {
          this.movementScheme.setBaseModifier(0.25F);
          canTriggerPlonk = true;
        }
      }
      else if (this.carriedWeight < this.cantmove) {
        if (this.carriedWeight + weight >= this.cantmove)
        {
          this.movementScheme.setEncumbered(true);
          this.movementScheme.setBaseModifier(0.05F);
          getCommunicator().sendAlertServerMessage("You are encumbered and move extremely slow.");
          canTriggerPlonk = true;
        }
      }
      if ((canTriggerPlonk) && 
        (!PlonkData.ENCUMBERED.hasSeenThis(this))) {
        PlonkData.ENCUMBERED.trigger(this);
      }
      this.carriedWeight += weight;
      if (getVehicle() != -10L)
      {
        Creature c = Creatures.getInstance().getCreatureOrNull(getVehicle());
        if (c != null)
        {
          c.ignoreSaddleDamage = true;
          c.getMovementScheme().update();
        }
      }
    }
    else
    {
      this.carriedWeight += weight;
      this.ignoreSaddleDamage = true;
      this.movementScheme.update();
    }
  }
  
  public boolean removeCarriedWeight(int weight)
  {
    if (isPlayer())
    {
      if (this.carriedWeight >= this.cantmove)
      {
        if (this.carriedWeight - weight < this.moveslow)
        {
          this.movementScheme.setEncumbered(false);
          this.movementScheme.setBaseModifier(1.0F);
          getCommunicator().sendAlertServerMessage("You can now move again.");
        }
        else if (this.carriedWeight - weight < this.encumbered)
        {
          this.movementScheme.setEncumbered(false);
          this.movementScheme.setBaseModifier(0.75F);
          getCommunicator().sendAlertServerMessage("You can now move again.");
        }
        else if (this.carriedWeight - weight < this.cantmove)
        {
          this.movementScheme.setEncumbered(false);
          this.movementScheme.setBaseModifier(0.25F);
          getCommunicator().sendAlertServerMessage("You can now move again.");
        }
      }
      else if (this.carriedWeight >= this.encumbered)
      {
        if (this.carriedWeight - weight < this.moveslow)
        {
          this.movementScheme.setEncumbered(false);
          this.movementScheme.setBaseModifier(1.0F);
        }
        else if (this.carriedWeight - weight < this.encumbered)
        {
          this.movementScheme.setEncumbered(false);
          this.movementScheme.setBaseModifier(0.75F);
        }
      }
      else if (this.carriedWeight >= this.moveslow) {
        if (this.carriedWeight - weight < this.moveslow)
        {
          this.movementScheme.setEncumbered(false);
          this.movementScheme.setBaseModifier(1.0F);
        }
      }
      this.carriedWeight -= weight;
      if (getVehicle() != -10L)
      {
        Creature c = Creatures.getInstance().getCreatureOrNull(getVehicle());
        if (c != null)
        {
          c.ignoreSaddleDamage = true;
          c.getMovementScheme().update();
        }
      }
    }
    else
    {
      this.carriedWeight -= weight;
      this.ignoreSaddleDamage = true;
      this.movementScheme.update();
    }
    if (this.carriedWeight < 0)
    {
      logger.log(Level.WARNING, "Carried weight is less than 0 for " + this);
      if ((this instanceof Player)) {
        logger.log(Level.INFO, this.name + " now carries " + this.carriedWeight + " AFTER removing " + weight + " gs. Modifier is:" + this.movementScheme
          .getSpeedModifier() + ".");
      }
      return false;
    }
    return true;
  }
  
  public boolean canCarry(int weight)
  {
    try
    {
      if (getPower() > 1) {
        return true;
      }
      Skill strength = this.skills.getSkill(102);
      
      return strength.getKnowledge(0.0D) * 7000.0D > weight + this.carriedWeight;
    }
    catch (NoSuchSkillException nss)
    {
      logger.log(Level.WARNING, "No strength skill for " + this);
    }
    return false;
  }
  
  public int getCarryCapacityFor(int weight)
  {
    try
    {
      Skill strength = this.skills.getSkill(102);
      
      return (int)(strength.getKnowledge(0.0D) * 7000.0D - this.carriedWeight) / weight;
    }
    catch (NoSuchSkillException nss)
    {
      logger.log(Level.WARNING, "No strength skill for " + this);
    }
    return 0;
  }
  
  public int getCarriedWeight()
  {
    return this.carriedWeight;
  }
  
  public int getSaddleBagsCarriedWeight()
  {
    for (Item i : getBody().getAllItems()) {
      if (i.isSaddleBags())
      {
        float mod = 0.5F;
        if (i.getTemplateId() == 1334) {
          mod = 0.6F;
        }
        return (int)(i.getFullWeight() * mod);
      }
    }
    return 0;
  }
  
  public int getCarryingCapacityLeft()
  {
    try
    {
      Skill strength = this.skills.getSkill(102);
      
      return (int)(strength.getKnowledge(0.0D) * 7000.0D) - this.carriedWeight;
    }
    catch (NoSuchSkillException nss)
    {
      logger.log(Level.WARNING, "No strength skill for " + this);
    }
    return 0;
  }
  
  public void setTeleportPoints(short x, short y, int layer, int floorLevel)
  {
    setTeleportPoints((x << 2) + 2.0F, (y << 2) + 2.0F, layer, floorLevel);
  }
  
  public void setTeleportPoints(float x, float y, int layer, int floorLevel)
  {
    this.teleportX = x;
    this.teleportY = y;
    this.teleportLayer = layer;
    this.teleportFloorLevel = floorLevel;
  }
  
  public void setTeleportLayer(int layer)
  {
    this.teleportLayer = layer;
  }
  
  public void setTeleportFloorLevel(int floorLevel)
  {
    this.teleportFloorLevel = floorLevel;
  }
  
  public int getTeleportLayer()
  {
    return this.teleportLayer;
  }
  
  public int getTeleportFloorLevel()
  {
    return this.teleportFloorLevel;
  }
  
  public VolaTile getCurrentTile()
  {
    if (this.currentTile != null) {
      return this.currentTile;
    }
    if (this.status != null)
    {
      int tilex = getTileX();
      int tiley = getTileY();
      try
      {
        Zone zone = Zones.getZone(tilex, tiley, isOnSurface());
        
        return zone.getOrCreateTile(tilex, tiley);
      }
      catch (NoSuchZoneException localNoSuchZoneException) {}
    }
    return null;
  }
  
  public int getCurrentTileNum()
  {
    int tilex = getTileX();
    int tiley = getTileY();
    if (isOnSurface()) {
      return Server.surfaceMesh.getTile(tilex, tiley);
    }
    return Server.caveMesh.getTile(tilex, tiley);
  }
  
  public void addItemsToTrade()
  {
    if (isTrader()) {
      getTradeHandler().addItemsToTrade();
    }
  }
  
  public boolean startTeleporting()
  {
    disembark(false);
    return startTeleporting(false);
  }
  
  public float getTeleportX()
  {
    return this.teleportX;
  }
  
  public float getTeleportY()
  {
    return this.teleportY;
  }
  
  public boolean shouldStopTrading(boolean firstCall)
  {
    if (isTrading())
    {
      if ((getTrade().creatureOne != null) && (getTrade().creatureOne.isPlayer())) {
        if (getTrade().creatureOne.shouldStopTrading(false))
        {
          getTrade().creatureOne.getCommunicator().sendAlertServerMessage("You took too long to trade and " + 
            getName() + " takes care of another customer.");
          getTrade().end(this, false);
          return true;
        }
      }
      if ((getTrade().creatureTwo != null) && (getTrade().creatureTwo.isPlayer())) {
        if (getTrade().creatureTwo.shouldStopTrading(false))
        {
          getTrade().creatureTwo.getCommunicator().sendAlertServerMessage("You took too long to trade and " + 
            getName() + " takes care of another customer.");
          getTrade().end(this, false);
          return true;
        }
      }
    }
    return false;
  }
  
  public boolean startTeleporting(boolean enterVehicle)
  {
    if (this.teleportLayer < 0) {
      if (Tiles.isSolidCave(Tiles.decodeType(Server.caveMesh.getTile((int)this.teleportX >> 2, (int)this.teleportY >> 2))))
      {
        getCommunicator().sendAlertServerMessage("The teleportation target is in rock!");
        return false;
      }
    }
    stopLeading();
    this._enterVehicle = enterVehicle;
    if (!enterVehicle)
    {
      Creatures.getInstance().setCreatureDead(this);
      Players.getInstance().setCreatureDead(this);
    }
    this.startTeleportTime = System.currentTimeMillis();
    this.communicator.setReady(false);
    if (this.status.isTrading()) {
      this.status.getTrade().end(this, false);
    }
    if (this.movementScheme.draggedItem != null) {
      MethodsItems.stopDragging(this, this.movementScheme.draggedItem);
    }
    int tileX = getTileX();
    int tileY = getTileY();
    try
    {
      destroyVisionArea();
      if (!isDead())
      {
        Zone zone = Zones.getZone(tileX, tileY, isOnSurface());
        zone.deleteCreature(this, true);
      }
    }
    catch (NoSuchZoneException nsz)
    {
      logger.log(Level.WARNING, getName() + " tried to teleport to nonexistant zone at " + tileX + ", " + tileY);
    }
    catch (NoSuchCreatureException nsc)
    {
      logger.log(Level.WARNING, this + " creature doesn't exist?", nsc);
    }
    catch (NoSuchPlayerException nsp)
    {
      logger.log(Level.WARNING, this + " player doesn't exist?", nsp);
    }
    this.status.setPositionX(this.teleportX);
    this.status.setPositionY(this.teleportY);
    try
    {
      this.status.setLayer(this.teleportLayer >= 0 ? 0 : -1);
      boolean setOffZ = false;
      if (this.mountAction != null) {
        setOffZ = true;
      }
      if (setOffZ)
      {
        this.status.setPositionZ(Math.max(
          Zones.calculateHeight(this.teleportX, this.teleportY, isOnSurface()) + this.mountAction.getOffZ(), this.mountAction
          .getOffZ()));
        getMovementScheme().offZ = this.mountAction.getOffZ();
      }
      else
      {
        VolaTile targetTile = Zones.getTileOrNull((int)(this.teleportX / 4.0F), (int)(this.teleportY / 4.0F), this.teleportLayer >= 0);
        
        float height = this.teleportFloorLevel > 0 ? this.teleportFloorLevel * 3 : 0.0F;
        if (targetTile != null)
        {
          getMovementScheme().setGroundOffset((int)(height * 10.0F), true);
          calculateFloorLevel(targetTile, true);
        }
        this.status.setPositionZ(Zones.calculateHeight(this.teleportX, this.teleportY, isOnSurface()) + height);
      }
    }
    catch (NoSuchZoneException nsz)
    {
      logger.log(Level.WARNING, getName() + " tried to teleport to nonexistant zone at " + this.teleportX + ", " + this.teleportY);
    }
    getMovementScheme().setPosition(this.teleportX, this.teleportY, this.status.getPositionZ(), this.status.getRotation(), getLayer());
    
    getMovementScheme().haltSpeedModifier();
    
    boolean zoneExists = true;
    try
    {
      this.status.savePosition(getWurmId(), isPlayer(), 
        Zones.getZoneIdFor((int)this.teleportX >> 2, (int)this.teleportY >> 2, isOnSurface()), true);
    }
    catch (IOException iox)
    {
      logger.log(Level.WARNING, iox.getMessage(), iox);
    }
    catch (NoSuchZoneException nsz)
    {
      logger.log(Level.INFO, getName() + " no zone at " + ((int)this.teleportX >> 2) + ", " + ((int)this.teleportY >> 2) + ", surf=" + 
        isOnSurface());
      zoneExists = false;
    }
    try
    {
      if (zoneExists) {
        Zones.getZone((int)this.teleportX >> 2, (int)this.teleportY >> 2, isOnSurface()).addCreature(this.id);
      }
      Server.getInstance().addCreatureToPort(this);
    }
    catch (Exception ex)
    {
      logger.log(Level.WARNING, getName() + " failed to recreate vision area after teleporting: " + ex.getMessage());
    }
    return true;
  }
  
  public long getPlayingTime()
  {
    return System.currentTimeMillis();
  }
  
  public void teleport()
  {
    teleport(true);
  }
  
  public void teleport(boolean destroyVisionArea)
  {
    this.communicator.setReady(true);
    if (destroyVisionArea) {
      try
      {
        Zone newzone = Zones.getZone(getTileX(), getTileY(), isOnSurface());
        this.addingAfterTeleport = true;
        newzone.addCreature(this.id);
        sendActionControl("", false, 0);
        try
        {
          createVisionArea();
        }
        catch (Exception ex)
        {
          logger.log(Level.WARNING, "Failed to create visionArea:" + ex.getMessage(), ex);
        }
        Server.getInstance().addCreatureToPort(this);
      }
      catch (NoSuchZoneException nsz)
      {
        logger.log(Level.WARNING, getName() + " tried to teleport to nonexistant zone at " + getTileX() + ", " + 
          getTileY());
      }
      catch (NoSuchCreatureException nsc)
      {
        logger.log(Level.WARNING, "This creature doesn't exist?", nsc);
      }
      catch (NoSuchPlayerException nsp)
      {
        logger.log(Level.WARNING, "This player doesn't exist?", nsp);
      }
    }
    this.addingAfterTeleport = false;
    stopTeleporting();
  }
  
  public void cancelTeleport()
  {
    this.teleportX = -1.0F;
    this.teleportY = -1.0F;
    this.teleportLayer = 0;
    this.startTeleportTime = Long.MIN_VALUE;
  }
  
  public void sendMountData()
  {
    if (this._enterVehicle)
    {
      if (this.mountAction != null)
      {
        this.mountAction.sendData(this);
        MountTransfer mt = MountTransfer.getTransferFor(getWurmId());
        if (mt != null) {
          mt.remove(getWurmId());
        }
      }
      setMountAction(null);
    }
  }
  
  public void stopTeleporting()
  {
    if (isTeleporting())
    {
      this.teleportX = -1.0F;
      this.teleportY = -1.0F;
      this.teleportLayer = 0;
      this.startTeleportTime = Long.MIN_VALUE;
      if (!this._enterVehicle)
      {
        getMovementScheme().setMooredMod(false);
        getMovementScheme().addWindImpact((byte)0);
        disembark(false);
        setMountAction(null);
        calcBaseMoveMod();
      }
      if (isPlayer())
      {
        ((Player)this).sentClimbing = 0L;
        ((Player)this).sentMountSpeed = 0L;
        ((Player)this).sentWind = 0L;
        if (!this._enterVehicle) {
          try
          {
            if (getLayer() >= 0) {
              getVisionArea().getSurface().checkIfEnemyIsPresent(false);
            } else {
              getVisionArea().getUnderGround().checkIfEnemyIsPresent(false);
            }
          }
          catch (Exception localException) {}
        }
      }
      this._enterVehicle = false;
      if ((!getCommunicator().stillLoggingIn()) || (!isPlayer())) {
        setTeleporting(false);
      }
      if (this.justSpawned) {
        this.justSpawned = false;
      }
    }
  }
  
  public boolean isWithinTeleportTime()
  {
    return System.currentTimeMillis() - this.startTeleportTime < 30000L;
  }
  
  public final boolean isTeleporting()
  {
    return this.isTeleporting;
  }
  
  public final void setTeleporting(boolean teleporting)
  {
    this.isTeleporting = teleporting;
  }
  
  public Body getBody()
  {
    return this.status.getBody();
  }
  
  public String examine()
  {
    return this.template.examine();
  }
  
  public boolean spamMode()
  {
    return false;
  }
  
  public byte getSex()
  {
    if (this.status.getSex() == Byte.MAX_VALUE) {
      return this.template.getSex();
    }
    return this.status.getSex();
  }
  
  public boolean setSex(byte sex)
  {
    this.status.setSex(sex);
    if (this.currentTile != null)
    {
      setVisible(false);
      setVisible(true);
    }
    return true;
  }
  
  public final void spawnFreeItems()
  {
    if (Features.Feature.FREE_ITEMS.isEnabled())
    {
      if ((this.spawnWeapon != null) && (this.spawnWeapon.length() > 0))
      {
        TestQuestion.createAndInsertItems(this, 319, 319, 40.0F, true, (byte)-1);
        try
        {
          int w = Integer.parseInt(this.spawnWeapon);
          int lTemplate = 0;
          boolean shield = false;
          switch (w)
          {
          case 1: 
            lTemplate = 21;
            shield = true;
            break;
          case 2: 
            lTemplate = 81;
            break;
          case 3: 
            lTemplate = 90;
            shield = true;
            break;
          case 4: 
            lTemplate = 87;
            break;
          case 5: 
            lTemplate = 292;
            shield = true;
            break;
          case 6: 
            lTemplate = 290;
            break;
          case 7: 
            lTemplate = 706;
            break;
          case 8: 
            lTemplate = 705;
          }
          if (lTemplate > 0) {
            try
            {
              TestQuestion.createAndInsertItems(this, lTemplate, lTemplate, 40.0F, true, (byte)-1);
              if (shield) {
                TestQuestion.createAndInsertItems(this, 84, 84, 40.0F, true, (byte)-1);
              }
            }
            catch (Exception ex)
            {
              logger.log(Level.INFO, "Failed to create item for spawning.", ex);
              getCommunicator().sendAlertServerMessage("Failed to spawn weapon.");
            }
          }
        }
        catch (Exception ex)
        {
          getCommunicator().sendAlertServerMessage("Failed to spawn weapon.");
        }
      }
      this.spawnWeapon = null;
      if ((this.spawnArmour != null) && (this.spawnArmour.length() > 0)) {
        try
        {
          int arm = Integer.parseInt(this.spawnArmour);
          float ql = 20.0F;
          byte matType = -1;
          switch (arm)
          {
          case 1: 
            ql = 40.0F;
            TestQuestion.createAndInsertItems(this, 274, 279, ql, true, (byte)-1);
            
            TestQuestion.createAndInsertItems(this, 278, 278, ql, true, (byte)-1);
            
            TestQuestion.createAndInsertItems(this, 274, 274, ql, true, (byte)-1);
            
            TestQuestion.createAndInsertItems(this, 277, 277, ql, true, (byte)-1);
            
            break;
          case 2: 
            ql = 60.0F;
            TestQuestion.createAndInsertItems(this, 103, 108, ql, true, (byte)-1);
            
            TestQuestion.createAndInsertItems(this, 103, 103, ql, true, (byte)-1);
            
            TestQuestion.createAndInsertItems(this, 105, 105, ql, true, (byte)-1);
            
            TestQuestion.createAndInsertItems(this, 106, 106, ql, true, (byte)-1);
            
            break;
          case 3: 
            ql = 20.0F;
            TestQuestion.createAndInsertItems(this, 280, 287, ql, true, (byte)-1);
            
            TestQuestion.createAndInsertItems(this, 284, 284, ql, true, (byte)-1);
            
            TestQuestion.createAndInsertItems(this, 280, 280, ql, true, (byte)-1);
            
            TestQuestion.createAndInsertItems(this, 283, 283, ql, true, (byte)-1);
          }
        }
        catch (Exception ex)
        {
          getCommunicator().sendAlertServerMessage("Failed to spawn weapon.");
        }
      }
      this.spawnArmour = null;
    }
  }
  
  public Communicator getCommunicator()
  {
    return this.communicator;
  }
  
  public void addKey(Item key, boolean loading)
  {
    if (this.keys == null) {
      this.keys = new HashSet();
    }
    if (!this.keys.contains(key))
    {
      this.keys.add(key);
      if (!loading)
      {
        Item[] itemarr = getInventory().getAllItems(false);
        if (!unlockItems(key, itemarr)) {
          unlockItems(key, getBody().getAllItems());
        }
        updateGates(key, false);
      }
    }
  }
  
  public void removeKey(Item key, boolean loading)
  {
    if (this.keys != null)
    {
      if (this.keys.remove(key)) {
        if (!loading)
        {
          Item[] itemarr = getInventory().getAllItems(false);
          if (!lockItems(key, itemarr)) {
            lockItems(key, getBody().getAllItems());
          }
          updateGates(key, true);
        }
      }
      if (this.keys.isEmpty()) {
        this.keys = null;
      }
    }
  }
  
  public void updateGates(Item key, boolean removedKey)
  {
    VolaTile t = getCurrentTile();
    if (t != null)
    {
      Door[] doors = t.getDoors();
      if (doors != null) {
        for (Door lDoor : doors) {
          lDoor.updateDoor(this, key, removedKey);
        }
      }
    }
    else
    {
      logger.log(Level.WARNING, getName() + " was on null tile.", new Exception());
    }
  }
  
  public void updateGates()
  {
    VolaTile t = getCurrentTile();
    if (t != null)
    {
      Door[] doors = t.getDoors();
      if (doors != null) {
        for (Door lDoor : doors)
        {
          lDoor.removeCreature(this);
          if (lDoor.covers(getPosX(), getPosY(), getPositionZ(), getFloorLevel(), followsGround())) {
            lDoor.addCreature(this);
          }
        }
      }
    }
    else
    {
      logger.log(Level.WARNING, getName() + " was on null tile.", new Exception());
    }
  }
  
  public boolean unlockItems(Item key, Item[] items)
  {
    for (Item lItem : items) {
      if ((lItem.isLockable()) && (lItem.getLockId() != -10L)) {
        try
        {
          Item lock = Items.getItem(lItem.getLockId());
          long[] keyarr = lock.getKeyIds();
          for (long lElement : keyarr) {
            if (lElement == key.getWurmId())
            {
              if (!lItem.isEmpty(false)) {
                if (lItem.getOwnerId() == getWurmId()) {
                  getCommunicator().sendHasMoreItems(-1L, lItem.getWurmId());
                } else {
                  getCommunicator().sendHasMoreItems(lItem.getTopParent(), lItem.getWurmId());
                }
              }
              return true;
            }
          }
        }
        catch (NoSuchItemException nsi)
        {
          logger.log(Level.WARNING, nsi.getMessage(), nsi);
        }
      }
    }
    return false;
  }
  
  public boolean lockItems(Item key, Item[] items)
  {
    boolean stillUnlocked = false;
    for (Item lItem : items) {
      if ((lItem.isLockable()) && (lItem.getLockId() != -10L)) {
        try
        {
          Item lock = Items.getItem(lItem.getLockId());
          long[] keyarr = lock.getKeyIds();
          boolean thisLock = false;
          for (long lElement : keyarr)
          {
            for (Item key2 : this.keys) {
              if (lElement == key2.getWurmId()) {
                stillUnlocked = true;
              }
            }
            if (lElement == key.getWurmId()) {
              thisLock = true;
            }
          }
          if ((thisLock) && (!stillUnlocked))
          {
            Object contItems = lItem.getItems();
            for (Item item : (Set)contItems) {
              item.removeWatcher(this, true);
            }
            return true;
          }
          if (thisLock) {
            return true;
          }
        }
        catch (NoSuchItemException nsi)
        {
          logger.log(Level.WARNING, nsi.getMessage(), nsi);
        }
      }
    }
    return false;
  }
  
  public boolean hasKeyForLock(Item lock)
  {
    if (lock.getWurmId() == getWurmId()) {
      return true;
    }
    if ((this.keys == null) || (this.keys.isEmpty())) {
      return false;
    }
    if (lock.getWurmId() == 5390789413122L) {
      if (lock.getParentId() == 5390755858690L)
      {
        boolean ok = true;
        if (!hasAbility(Abilities.getAbilityForItem(809, this))) {
          ok = false;
        }
        if (!hasAbility(Abilities.getAbilityForItem(808, this))) {
          ok = false;
        }
        if (!hasAbility(Abilities.getAbilityForItem(798, this))) {
          ok = false;
        }
        if (!hasAbility(Abilities.getAbilityForItem(810, this))) {
          ok = false;
        }
        if (!hasAbility(Abilities.getAbilityForItem(807, this))) {
          ok = false;
        }
        if (!ok)
        {
          getCommunicator().sendAlertServerMessage("There is some mysterious enchantment on this lock!");
          return ok;
        }
      }
    }
    long[] keyarr = lock.getKeyIds();
    long lElement;
    for (lElement : keyarr) {
      for (Item key : this.keys) {
        if (lElement == key.getWurmId()) {
          return true;
        }
      }
    }
    return false;
  }
  
  public boolean hasAllKeysForLock(Item lock)
  {
    for (long aKey : lock.getKeyIds())
    {
      boolean foundit = false;
      for (Item key : getKeys()) {
        if (aKey == key.getWurmId())
        {
          foundit = true;
          break;
        }
      }
      if (!foundit) {
        return false;
      }
    }
    return true;
  }
  
  public Item[] getKeys()
  {
    Item[] toReturn = new Item[0];
    if (this.keys != null) {
      toReturn = (Item[])this.keys.toArray(new Item[this.keys.size()]);
    }
    return toReturn;
  }
  
  public boolean isOnSurface()
  {
    return this.status.isOnSurface();
  }
  
  public void setLayer(int layer, boolean removeFromTile)
  {
    if (getStatus().getLayer() != layer) {
      if ((isPlayer()) || (removeFromTile))
      {
        if (this.currentTile != null)
        {
          if (!(this instanceof Player)) {
            setPositionZ(Zones.calculatePosZ(getPosX(), getPosY(), getCurrentTile(), isOnSurface(), isFloating(), 
              getPositionZ(), this, getBridgeId()));
          }
          getStatus().setLayer(layer);
          if ((getVehicle() != -10L) && (isVehicleCommander()))
          {
            Vehicle vehic = Vehicles.getVehicleForId(getVehicle());
            if (vehic != null)
            {
              boolean ok = true;
              if (vehic.creature) {
                try
                {
                  Creature cretVehicle = Server.getInstance().getCreature(vehic.wurmid);
                  if (layer < 0)
                  {
                    int tile = Server.caveMesh.getTile(cretVehicle.getTileX(), cretVehicle.getTileY());
                    if (!Tiles.isSolidCave(Tiles.decodeType(tile))) {
                      cretVehicle.setLayer(layer, false);
                    }
                  }
                  else
                  {
                    cretVehicle.setLayer(layer, false);
                  }
                }
                catch (NoSuchCreatureException nsi)
                {
                  logger.log(Level.WARNING, this + ", cannot get creature for vehicle: " + vehic + " due to " + nsi
                    .getMessage(), nsi);
                }
                catch (NoSuchPlayerException nsp)
                {
                  logger.log(Level.WARNING, this + ", cannot get creature for vehicle: " + vehic + " due to " + nsp
                    .getMessage(), nsp);
                }
              } else {
                try
                {
                  Item itemVehicle = Items.getItem(vehic.wurmid);
                  if (layer < 0)
                  {
                    int caveTile = Server.caveMesh.getTile((int)itemVehicle.getPosX() >> 2, 
                      (int)itemVehicle.getPosY() >> 2);
                    if (Tiles.isSolidCave(Tiles.decodeType(caveTile))) {
                      ok = false;
                    }
                  }
                  if (ok)
                  {
                    itemVehicle.newLayer = ((byte)layer);
                    
                    Zone zone = null;
                    try
                    {
                      zone = Zones.getZone((int)itemVehicle.getPosX() >> 2, 
                        (int)itemVehicle.getPosY() >> 2, itemVehicle.isOnSurface());
                      zone.removeItem(itemVehicle, true, true);
                    }
                    catch (NoSuchZoneException nsz)
                    {
                      logger.log(Level.WARNING, itemVehicle
                        .getName() + " this shouldn't happen: " + nsz.getMessage() + " at " + (
                        (int)itemVehicle.getPosX() >> 2) + ", " + (
                        (int)itemVehicle.getPosY() >> 2), nsz);
                    }
                    try
                    {
                      zone = Zones.getZone((int)itemVehicle.getPosX() >> 2, 
                        (int)itemVehicle.getPosY() >> 2, layer >= 0);
                      
                      zone.addItem(itemVehicle, false, false, false);
                    }
                    catch (NoSuchZoneException nsz)
                    {
                      logger.log(Level.WARNING, itemVehicle
                        .getName() + " this shouldn't happen: " + nsz.getMessage() + " at " + (
                        (int)itemVehicle.getPosX() >> 2) + ", " + (
                        (int)itemVehicle.getPosY() >> 2), nsz);
                    }
                    itemVehicle.newLayer = Byte.MIN_VALUE;
                    Seat[] seats = vehic.hitched;
                    if (seats != null) {
                      for (int x = 0; x < seats.length; x++) {
                        if (seats[x] != null)
                        {
                          if (seats[x].occupant != -10L) {
                            try
                            {
                              Creature c = Server.getInstance().getCreature(seats[x].occupant);
                              
                              c.getStatus().setLayer(layer);
                              c.getCurrentTile().newLayer(c);
                            }
                            catch (NoSuchPlayerException nsp)
                            {
                              logger.log(Level.WARNING, getName() + " " + nsp.getMessage(), nsp);
                            }
                            catch (NoSuchCreatureException nsc)
                            {
                              logger.log(Level.WARNING, getName() + " " + nsc.getMessage(), nsc);
                            }
                          }
                        }
                        else {
                          logger.log(Level.WARNING, getName() + " " + vehic.name + ": lacking seat " + x, new Exception());
                        }
                      }
                    }
                  }
                }
                catch (NoSuchItemException is)
                {
                  logger.log(Level.WARNING, getName() + " " + is.getMessage(), is);
                }
              }
              if (ok)
              {
                Seat[] seats = vehic.seats;
                if (seats != null) {
                  for (int x = 0; x < seats.length; x++) {
                    if (x > 0) {
                      if (seats[x] != null)
                      {
                        if (seats[x].occupant != -10L) {
                          try
                          {
                            Creature c = Server.getInstance().getCreature(seats[x].occupant);
                            c.getStatus().setLayer(layer);
                            c.getCurrentTile().newLayer(c);
                            if (c.isPlayer()) {
                              if (c.isOnSurface())
                              {
                                c.getCommunicator().sendNormalServerMessage("You leave the cave.");
                              }
                              else
                              {
                                c.getCommunicator().sendNormalServerMessage("You enter the cave.");
                                if (c.getVisionArea() != null) {
                                  c.getVisionArea().initializeCaves();
                                }
                              }
                            }
                          }
                          catch (NoSuchPlayerException nsp)
                          {
                            logger.log(Level.WARNING, getName() + " " + nsp.getMessage(), nsp);
                          }
                          catch (NoSuchCreatureException nsc)
                          {
                            logger.log(Level.WARNING, getName() + " " + nsc.getMessage(), nsc);
                          }
                        }
                      }
                      else {
                        logger.log(Level.WARNING, getName() + " " + vehic.name + ": lacking seat " + x, new Exception());
                      }
                    }
                  }
                }
              }
            }
          }
          this.currentTile.newLayer(this);
        }
        else
        {
          getStatus().setLayer(layer);
        }
        if (isPlayer())
        {
          if ((layer < 0) && (getVisionArea() != null)) {
            getVisionArea().checkCaves(true);
          }
          if (layer < 0) {
            getCommunicator().sendNormalServerMessage("You enter the cave.");
          } else {
            getCommunicator().sendNormalServerMessage("You leave the cave.");
          }
          Village v = Villages.getVillage(getTileX(), getTileY(), true);
          if (v != null) {
            if (v.isEnemy(this))
            {
              Guard[] guards = v.getGuards();
              for (int gx = 0; gx < guards.length; gx++) {
                if (guards[gx].getCreature().isWithinDistanceTo(this, 20.0F)) {
                  if (visibilityCheck(guards[gx].getCreature(), 0.0F))
                  {
                    v.checkIfRaiseAlert(this);
                    break;
                  }
                }
              }
            }
          }
        }
      }
      else
      {
        getStatus().setLayer(layer);
        getCurrentTile().newLayer(this);
      }
    }
  }
  
  public int getLayer()
  {
    return getStatus().getLayer();
  }
  
  public void setPositionX(float pos)
  {
    this.status.setPositionX(pos);
  }
  
  public void setPositionY(float pos)
  {
    this.status.setPositionY(pos);
  }
  
  public void setPositionZ(float pos)
  {
    this.status.setPositionZ(pos);
  }
  
  public void setRotation(float aRot)
  {
    this.status.setRotation(aRot);
  }
  
  public void turnTo(float newRot)
  {
    setRotation(normalizeAngle(newRot));
    
    moved(0, 0, 0, 0, 0);
  }
  
  public void turnBy(float turnAmount)
  {
    setRotation(normalizeAngle(this.status.getRotation() + turnAmount));
    
    moved(0, 0, 0, 0, 0);
  }
  
  public void submerge()
  {
    try
    {
      float lOldPosZ = getPositionZ();
      float lNewPosZ = isFloating() ? this.template.offZ : Zones.calculateHeight(getPosX(), getPosY(), true) / 2.0F;
      
      moved(0, 0, (int)((lNewPosZ - lOldPosZ) * 10.0F), 0, 0);
    }
    catch (NoSuchZoneException localNoSuchZoneException) {}
  }
  
  public void surface()
  {
    float lOldPosZ = getPositionZ();
    float lNewPosZ = isFloating() ? this.template.offZ : -1.25F;
    setPositionZ(lNewPosZ);
    moved(0, 0, (int)((lNewPosZ - lOldPosZ) * 10.0F), 0, 0);
  }
  
  public void almostSurface()
  {
    float _oldPosZ = getPositionZ();
    float _newPosZ = -2.0F;
    setPositionZ(-2.0F);
    moved(0, 0, (int)((-2.0F - _oldPosZ) * 10.0F), 0, 0);
  }
  
  public void setCommunicator(Communicator comm)
  {
    this.communicator = comm;
  }
  
  public void loadPossessions(long inventoryId)
    throws Exception
  {
    try
    {
      this.possessions = new Possessions(this, inventoryId);
    }
    catch (Exception ex)
    {
      logger.log(Level.INFO, ex.getMessage(), ex);
      this.status.createNewPossessions();
    }
  }
  
  public long createPossessions()
    throws Exception
  {
    this.possessions = new Possessions(this);
    return this.possessions.getInventory().getWurmId();
  }
  
  public Behaviour getBehaviour()
  {
    return this.behaviour;
  }
  
  public final boolean hasFightDistanceTo(Creature _target)
  {
    if (Math.abs(getStatus().getPositionX() - _target.getStatus().getPositionX()) > Math.abs(getStatus().getPositionY() - _target
      .getStatus().getPositionY())) {
      return Math.abs(getStatus().getPositionX() - _target.getStatus().getPositionX()) < 8.0F;
    }
    return Math.abs(getStatus().getPositionY() - _target.getStatus().getPositionY()) < 8.0F;
  }
  
  public static final int rangeTo(Creature performer, Creature target)
  {
    if (Math.abs(performer.getStatus().getPositionX() - target.getStatus().getPositionX()) > Math.abs(performer.getStatus()
      .getPositionY() - target.getStatus().getPositionY())) {
      return (int)Math.abs(performer.getStatus().getPositionX() - target.getStatus().getPositionX());
    }
    return (int)Math.abs(performer.getStatus().getPositionY() - target.getStatus().getPositionY());
  }
  
  private static final float calcModPosX(double sinRot, double cosRot, float widthCM, float lengthCM)
  {
    return (float)(cosRot * widthCM - sinRot * lengthCM);
  }
  
  private static final float calcModPosY(double sinRot, double cosRot, float widthCM, float lengthCM)
  {
    return (float)(widthCM * sinRot + lengthCM * cosRot);
  }
  
  private static Vector2f rotate(float angle, Vector2f center, Vector2f point)
  {
    double rads = angle * 3.141592653589793D / 180.0D;
    Vector2f nPoint = new Vector2f();
    nPoint.x = ((float)(center.x + (point.x - center.x) * Math.cos(rads) + (point.y - center.y) * Math.sin(rads)));
    nPoint.y = ((float)(center.y - (point.x - center.x) * Math.sin(rads) + (point.y - center.y) * Math.cos(rads)));
    return nPoint;
  }
  
  private static final boolean isLeftOf(Vector2f point, float posX)
  {
    return posX < point.x;
  }
  
  private static final boolean isRightOf(Vector2f point, float posX)
  {
    return posX > point.x;
  }
  
  private static final boolean isAbove(Vector2f point, float posY)
  {
    return posY > point.y;
  }
  
  private static final boolean isBelow(Vector2f point, float posY)
  {
    return posY < point.y;
  }
  
  private static final int closestPoint(Vector2f[] points, Vector2f pos, Vector2f[] ignore)
  {
    boolean canIgnore = ignore != null;
    float min = 10000.0F;
    int index = -1;
    for (int i = 0; i < points.length; i++) {
      if (canIgnore)
      {
        boolean doIgnore = false;
        for (int x = 0; x < ignore.length; x++) {
          if (points[i] == ignore[x]) {
            doIgnore = true;
          }
        }
        if (doIgnore) {}
      }
      else
      {
        float len = pos.subtract(points[i]).length();
        if (len < min)
        {
          index = i;
          min = len;
        }
      }
    }
    return index;
  }
  
  public static final float rangeToInDec(Creature performer, Creature target)
  {
    if ((target.getTemplate().hasBoundingBox()) && (Features.Feature.CREATURE_COMBAT_CHANGES.isEnabled()))
    {
      float minX = target.getTemplate().getBoundMinX() * target.getStatus().getSizeMod();
      float minY = target.getTemplate().getBoundMinY() * target.getStatus().getSizeMod();
      float maxX = target.getTemplate().getBoundMaxX() * target.getStatus().getSizeMod();
      float maxY = target.getTemplate().getBoundMaxY() * target.getStatus().getSizeMod();
      Vector2f center = new Vector2f(target.getStatus().getPositionX(), target.getStatus().getPositionY());
      
      float PX = performer.getStatus().getPositionX();
      float PY = performer.getStatus().getPositionY();
      Vector3f cpos = new Vector3f(center.x, center.y, 1.0F);
      
      float rotation = target.getStatus().getRotation();
      
      Vector3f mp1 = new Vector3f(minX, minY, 0.0F);
      Vector3f mp2 = new Vector3f(maxX, maxY, 0.0F);
      BoxMatrix M = new BoxMatrix(true);
      BoundBox box = new BoundBox(M, mp1, mp2);
      box.M.translate(cpos);
      box.M.rotate(rotation + 180.0F, false, false, true);
      
      Vector3f ppos = new Vector3f(PX, PY, 0.5F);
      if (box.isPointInBox(ppos)) {
        return box.distOutside(ppos, cpos) * 10.0F;
      }
      return box.distOutside(ppos, cpos) * 10.0F;
    }
    if (Math.abs(performer.getStatus().getPositionX() - target.getStatus().getPositionX()) > Math.abs(performer.getStatus()
      .getPositionY() - target.getStatus().getPositionY())) {
      return Math.abs(performer.getStatus().getPositionX() - target.getStatus().getPositionX()) * 10.0F;
    }
    return Math.abs(performer.getStatus().getPositionY() - target.getStatus().getPositionY()) * 10.0F;
  }
  
  public static int rangeTo(Creature performer, Item aTarget)
  {
    if (Math.abs(performer.getStatus().getPositionX() - aTarget.getPosX()) > Math.abs(performer.getStatus().getPositionY() - aTarget
      .getPosY())) {
      return (int)Math.abs(performer.getStatus().getPositionX() - aTarget.getPosX());
    }
    return (int)Math.abs(performer.getStatus().getPositionY() - aTarget.getPosY());
  }
  
  public void setAction(Action action)
  {
    this.actions.addAction(action);
  }
  
  public ActionStack getActions()
  {
    return this.actions;
  }
  
  public Action getCurrentAction()
    throws NoSuchActionException
  {
    return this.actions.getCurrentAction();
  }
  
  public Item getLeadingItem(Creature follower)
  {
    return null;
  }
  
  public Creature getFollowedCreature(Item leadingItem)
  {
    return null;
  }
  
  public boolean isItemLeading(Item item)
  {
    return false;
  }
  
  public void addFollower(Creature follower, @Nullable Item leadingItem)
  {
    if (this.followers == null) {
      this.followers = new HashMap();
    }
    this.followers.put(follower, leadingItem);
  }
  
  public Creature[] getFollowers()
  {
    if ((this.followers == null) || (this.followers.size() == 0)) {
      return emptyCreatures;
    }
    return (Creature[])this.followers.keySet().toArray(new Creature[this.followers.size()]);
  }
  
  public final int getNumberOfFollowers()
  {
    if (this.followers == null) {
      return 0;
    }
    return this.followers.size();
  }
  
  public void stopLeading()
  {
    if (this.followers != null)
    {
      Creature[] followArr = (Creature[])this.followers.keySet().toArray(new Creature[this.followers.size()]);
      for (Creature lElement : followArr) {
        lElement.setLeader(null);
      }
      this.followers = null;
    }
  }
  
  public boolean mayLeadMoreCreatures()
  {
    return (this.followers == null) || (this.followers.size() < 10);
  }
  
  public final boolean isLeading(Creature checked)
  {
    for (Creature c : getFollowers())
    {
      for (Creature c2 : getFollowers())
      {
        for (Creature c3 : getFollowers())
        {
          for (Creature c4 : getFollowers())
          {
            for (Creature c5 : getFollowers())
            {
              for (Creature c6 : getFollowers())
              {
                for (Creature c7 : getFollowers()) {
                  if (c7.getWurmId() == checked.getWurmId()) {
                    return true;
                  }
                }
                if (c6.getWurmId() == checked.getWurmId()) {
                  return true;
                }
              }
              if (c5.getWurmId() == checked.getWurmId()) {
                return true;
              }
            }
            if (c4.getWurmId() == checked.getWurmId()) {
              return true;
            }
          }
          if (c3.getWurmId() == checked.getWurmId()) {
            return true;
          }
        }
        if (c2.getWurmId() == checked.getWurmId()) {
          return true;
        }
      }
      if (c.getWurmId() == checked.getWurmId()) {
        return true;
      }
    }
    return false;
  }
  
  public void setLeader(@Nullable Creature leadingCreature)
  {
    if (leadingCreature == this)
    {
      logger.log(Level.WARNING, getName() + " tries to lead itself at ", new Exception());
      return;
    }
    clearOrders();
    if (this.leader == null)
    {
      if (leadingCreature != null)
      {
        if (isLeading(leadingCreature)) {
          return;
        }
        this.leader = leadingCreature;
        Creatures.getInstance().setLastLed(getWurmId(), this.leader.getWurmId());
        Server.getInstance().broadCastAction(getNameWithGenus() + " now follows " + this.leader.getNameWithGenus() + ".", this.leader, this, 5);
        
        this.leader.getCommunicator().sendNormalServerMessage("You start leading " + 
          getNameWithGenus() + ".");
        getCommunicator().sendNormalServerMessage("You start following " + this.leader.getNameWithGenus() + ".");
      }
    }
    else if (leadingCreature == null)
    {
      Server.getInstance().broadCastAction(getNameWithGenus() + " stops following " + this.leader.getNameWithGenus() + ".", this.leader, this, 5);
      
      this.leader.getCommunicator().sendNormalServerMessage("You stop leading " + 
        getNameWithGenus() + ".");
      getCommunicator().sendNormalServerMessage("You stop following " + this.leader.getNameWithGenus() + ".");
      this.leader.removeFollower(this);
      
      this.leader = null;
    }
  }
  
  public void removeFollower(Creature follower)
  {
    if (this.followers != null) {
      this.followers.remove(follower);
    }
  }
  
  public void putInWorld()
  {
    try
    {
      Zone z = Zones.getZone(getTileX(), getTileY(), getLayer() >= 0);
      z.addCreature(getWurmId());
    }
    catch (NoSuchZoneException nsz)
    {
      logger.log(Level.WARNING, getName() + " " + nsz.getMessage(), nsz);
    }
    catch (NoSuchPlayerException nsp)
    {
      logger.log(Level.WARNING, getName() + " " + nsp.getMessage(), nsp);
    }
    catch (NoSuchCreatureException nsc)
    {
      logger.log(Level.WARNING, getName() + " " + nsc.getMessage(), nsc);
    }
  }
  
  public static final double getRange(Creature performer, double targetX, double targetY)
  {
    double diffx = Math.abs(performer.getPosX() - targetX);
    double diffy = Math.abs(performer.getPosY() - targetY);
    return Math.sqrt(diffx * diffx + diffy * diffy);
  }
  
  public static final double getTileRange(Creature performer, int targetX, int targetY)
  {
    double diffx = Math.abs(performer.getTileX() - targetX);
    double diffy = Math.abs(performer.getTileY() - targetY);
    return Math.sqrt(diffx * diffx + diffy * diffy);
  }
  
  public boolean isWithinTileDistanceTo(int tileX, int tileY, int heigh1tOffset, int maxDist)
  {
    int ptilex = getTileX();
    int ptiley = getTileY();
    if ((ptilex > tileX + maxDist) || (ptilex < tileX - maxDist) || (ptiley > tileY + maxDist) || (ptiley < tileY - maxDist)) {
      return false;
    }
    return true;
  }
  
  public boolean isWithinDistanceTo(@Nonnull Item item, float maxDist)
  {
    return isWithinDistanceTo(item.getPos3f(), maxDist);
  }
  
  public boolean isWithinDistanceTo(@Nonnull Vector3f targetPos, float maxDist)
  {
    return isWithinDistanceTo(targetPos.x, targetPos.y, targetPos.z, maxDist);
  }
  
  public boolean isWithinDistanceTo(float aPosX, float aPosY, float aPosZ, float maxDist)
  {
    return (Math.abs(getStatus().getPositionX() + getAltOffZ() - aPosX) <= maxDist) && 
      (Math.abs(getStatus().getPositionY() - aPosY) <= maxDist);
  }
  
  public boolean isWithinDistanceTo(Creature targetCret, float maxDist)
  {
    return (Math.abs(getStatus().getPositionX() - targetCret.getPosX()) <= maxDist) && 
      (Math.abs(getStatus().getPositionY() - targetCret.getPosY()) <= maxDist);
  }
  
  public boolean isWithinDistanceTo(float aPosX, float aPosY, float aPosZ, float maxDist, float modifier)
  {
    return (Math.abs(getStatus().getPositionX() - (aPosX + modifier)) < maxDist) && 
      (Math.abs(getStatus().getPositionY() - (aPosY + modifier)) < maxDist);
  }
  
  public boolean isWithinDistanceToZ(float aPosZ, float maxDist, boolean addHalfHeight)
  {
    return Math.abs(getStatus().getPositionZ() + (addHalfHeight ? getHalfHeightDecimeters() / 10.0F : 0.0F) - aPosZ) < maxDist;
  }
  
  public boolean isWithinDistanceTo(int aPosX, int aPosY, int maxDistance)
  {
    return (Math.abs(getTileX() - aPosX) <= maxDistance) && 
      (Math.abs(getTileY() - aPosY) <= maxDistance);
  }
  
  public void creatureMoved(Creature creature, int diffX, int diffY, int diffZ)
  {
    if ((this.leader != null) && (this.leader.equals(creature))) {
      if (!isRidden()) {
        if ((diffX != 0) || (diffY != 0)) {
          followLeader(diffX, diffY);
        }
      }
    }
    if (isTypeFleeing())
    {
      if ((creature.isPlayer()) && (isBred())) {
        return;
      }
      if ((creature.isPlayer()) || (creature.isAggHuman()) || (creature.isHuman()) || (creature.isCarnivore()) || 
        (creature.isMonster()))
      {
        Vector2f mypos = new Vector2f(getPosX(), getPosY());
        float newDistance = new Vector2f(creature.getPosX() + diffX / 10.0F, creature.getPosY() + diffY / 10.0F).distance(mypos);
        if (newDistance < new Vector2f(creature.getPosX(), creature.getPosY()).distance(mypos)) {
          if (Features.Feature.CREATURE_MOVEMENT_CHANGES.isEnabled())
          {
            int baseCounter = (int)(Math.max(1.0F, creature.getBaseCombatRating() - getBaseCombatRating()) * 5.0F);
            if (baseCounter - newDistance > 0.0F) {
              setFleeCounter((int)Math.min(60.0F, Math.max(3.0F, baseCounter - newDistance)));
            }
          }
          else
          {
            setFleeCounter(60);
          }
        }
      }
    }
  }
  
  public final boolean isPrey()
  {
    return this.template.isPrey();
  }
  
  public final boolean isSpy()
  {
    return (this.status.modtype == 8) && ((this.template.getTemplateId() == 84) || (this.template.getTemplateId() == 10) || (this.template.getTemplateId() == 12));
  }
  
  public void delete()
  {
    Server.getInstance().addCreatureToRemove(this);
  }
  
  public void destroyVisionArea()
  {
    if (this.visionArea != null) {
      this.visionArea.destroy();
    }
    this.visionArea = null;
  }
  
  public void createVisionArea()
    throws Exception
  {
    if (this.visionArea != null) {
      this.visionArea.destroy();
    }
    this.visionArea = new VisionArea(this, this.template.getVision());
  }
  
  public String getHisHerItsString()
  {
    if (this.status.getSex() == 0) {
      return "his";
    }
    if (this.status.getSex() == 1) {
      return "her";
    }
    return "its";
  }
  
  public String getHimHerItString()
  {
    if (this.status.getSex() == 0) {
      return "him";
    }
    if (this.status.getSex() == 1) {
      return "her";
    }
    return "it";
  }
  
  public boolean mayAttack(@Nullable Creature cret)
  {
    return (this.status.getStunned() <= 0.0F) && (!this.status.isUnconscious());
  }
  
  public boolean isStunned()
  {
    return this.status.getStunned() > 0.0F;
  }
  
  public boolean isUnconscious()
  {
    return this.status.isUnconscious();
  }
  
  public String getHeSheItString()
  {
    if (this.status.getSex() == 0) {
      return "he";
    }
    if (this.status.getSex() == 1) {
      return "she";
    }
    return "it";
  }
  
  public void stopCurrentAction()
  {
    try
    {
      String toSend = this.actions.stopCurrentAction(false);
      if (toSend.length() > 0) {
        this.communicator.sendNormalServerMessage(toSend);
      }
      sendActionControl("", false, 0);
    }
    catch (NoSuchActionException localNoSuchActionException) {}
  }
  
  public void maybeInterruptAction(int damage)
  {
    try
    {
      Action act = this.actions.getCurrentAction();
      if (act.isVulnerable()) {
        if (getBodyControlSkill().skillCheck(damage / 100.0F, this.zoneBonus, false, 1.0F) < 0.0D)
        {
          String toSend = this.actions.stopCurrentAction(false);
          if (toSend.length() > 0) {
            this.communicator.sendNormalServerMessage(toSend);
          }
          sendActionControl("", false, 0);
        }
      }
    }
    catch (NoSuchActionException localNoSuchActionException) {}
  }
  
  public float getCombatDamage(Item bodyPart)
  {
    short pos = bodyPart.getPlace();
    if ((pos == 13) || (pos == 14)) {
      return getHandDamage();
    }
    if (pos == 34) {
      return getKickDamage();
    }
    if (pos == 1) {
      return getHeadButtDamage();
    }
    if (pos == 29) {
      return getBiteDamage();
    }
    if (pos == 2) {
      return getBreathDamage();
    }
    return 0.0F;
  }
  
  public String getAttackStringForBodyPart(Item bodypart)
  {
    if ((bodypart.getPlace() == 13) || (bodypart.getPlace() == 14)) {
      return this.template.getHandDamString();
    }
    if (bodypart.getPlace() == 34) {
      return this.template.getKickDamString();
    }
    if (bodypart.getPlace() == 29) {
      return this.template.getBiteDamString();
    }
    if (bodypart.getPlace() == 1) {
      return this.template.getHeadButtDamString();
    }
    if (bodypart.getPlace() == 2) {
      return this.template.getBreathDamString();
    }
    return this.template.getHandDamString();
  }
  
  public float getBodyWeaponSpeed(Item bodypart)
  {
    float size = this.template.getSize();
    if ((bodypart.getPlace() == 13) || (bodypart.getPlace() == 14)) {
      return size + 1.0F;
    }
    if (bodypart.getPlace() == 34) {
      return size + 2.0F;
    }
    if (bodypart.getPlace() == 29) {
      return size + 2.5F;
    }
    if (bodypart.getPlace() == 1) {
      return size + 3.0F;
    }
    if (bodypart.getPlace() == 2) {
      return size + 3.5F;
    }
    return 4.0F;
  }
  
  public Item getArmour(byte location)
    throws NoArmourException, NoSpaceException
  {
    Item bodyPart = null;
    try
    {
      barding = isHorse();
      if (barding) {
        bodyPart = this.status.getBody().getBodyPart(2);
      } else {
        bodyPart = this.status.getBody().getBodyPart(location);
      }
      if (location == 29) {
        return getArmour((byte)1);
      }
      Set<Item> its = bodyPart.getItems();
      for (Item item : its) {
        if (item.isArmour())
        {
          byte[] spaces = item.getBodySpaces();
          for (byte lSpace : spaces) {
            if ((lSpace == location) || (barding)) {
              return item;
            }
          }
        }
      }
    }
    catch (NoArmourException noa)
    {
      boolean barding;
      throw noa;
    }
    catch (Exception ex)
    {
      throw new NoSpaceException(ex);
    }
    throw new NoArmourException("No armour worn on bodypart " + location);
  }
  
  public Item getCarriedItem(int itemTemplateId)
  {
    Item inventory = getInventory();
    Item[] items = inventory.getAllItems(false);
    Item[] arrayOfItem1 = items;int i = arrayOfItem1.length;
    for (Item localItem1 = 0; localItem1 < i; localItem1++)
    {
      lItem = arrayOfItem1[localItem1];
      if (lItem.getTemplateId() == itemTemplateId) {
        return lItem;
      }
    }
    Item body = getBody().getBodyItem();
    items = body.getAllItems(false);
    Item[] arrayOfItem2 = items;localItem1 = arrayOfItem2.length;
    for (Item lItem = 0; lItem < localItem1; lItem++)
    {
      Item lItem = arrayOfItem2[lItem];
      if (lItem.getTemplateId() == itemTemplateId) {
        return lItem;
      }
    }
    return null;
  }
  
  public Item getEquippedItem(byte location)
    throws NoSuchItemException, NoSpaceException
  {
    try
    {
      Set<Item> wornItems = this.status.getBody().getBodyPart(location).getItems();
      for (Item item : wornItems) {
        if ((!item.isArmour()) && (!item.isBodyPartAttached())) {
          return item;
        }
      }
    }
    catch (NullPointerException npe)
    {
      if (this.status == null) {
        logger.log(Level.WARNING, "status is null for creature" + getName(), npe);
      } else if (this.status.getBody() == null) {
        logger.log(Level.WARNING, "body is null for creature" + getName(), npe);
      } else if (this.status.getBody().getBodyPart(location) == null) {
        logger.log(Level.WARNING, "body inventoryspace(" + location + ") is null for creature" + getName(), npe);
      } else {
        logger.log(Level.WARNING, "seems wornItems for inventoryspace was null for creature" + getName(), npe);
      }
      throw new NoSuchItemException("No equippedItem on bodypart " + location, npe);
    }
    throw new NoSuchItemException("No equippedItem on bodypart " + location);
  }
  
  public Item getEquippedWeapon(byte location)
    throws NoSpaceException
  {
    return getEquippedWeapon(location, true);
  }
  
  public Item getEquippedWeapon(byte location, boolean allowBow)
    throws NoSpaceException
  {
    return getEquippedWeapon(location, allowBow, false);
  }
  
  public Item getEquippedWeapon(byte location, boolean allowBow, boolean fetchBodypart)
    throws NoSpaceException
  {
    Item bodyPart = null;
    try
    {
      bodyPart = this.status.getBody().getBodyPart(location);
      if (isAnimal()) {
        return bodyPart;
      }
      if (((bodyPart.getPlace() != 37) && (bodyPart.getPlace() != 38) && 
        (bodyPart.getPlace() != 13) && (bodyPart.getPlace() != 14)) || (
        (!isPlayer()) && (fetchBodypart))) {
        return bodyPart;
      }
      Set<Item> wornItems = bodyPart.getItems();
      for (Item item : wornItems) {
        if ((!item.isArmour()) && (!item.isBodyPartAttached())) {
          if ((Weapon.getBaseDamageForWeapon(item) > 0.0F) || ((item.isWeaponBow()) && (allowBow))) {
            return item;
          }
        }
      }
      if ((bodyPart.getPlace() == 37) || (bodyPart.getPlace() == 38))
      {
        int handSlot = bodyPart.getPlace() == 37 ? 13 : 14;
        
        bodyPart = this.status.getBody().getBodyPart(handSlot);
      }
    }
    catch (NullPointerException npe)
    {
      if (this.status == null) {
        logger.log(Level.WARNING, "status is null for creature" + getName(), npe);
      } else if (this.status.getBody() == null) {
        logger.log(Level.WARNING, "body is null for creature" + getName(), npe);
      } else if (this.status.getBody().getBodyPart(location) == null) {
        logger.log(Level.WARNING, "body inventoryspace(" + location + ") is null for creature" + getName(), npe);
      } else {
        logger.log(Level.WARNING, "seems wornItems for inventoryspace was null for creature" + getName(), npe);
      }
      throw new NoSpaceException("No  bodypart " + location, npe);
    }
    return bodyPart;
  }
  
  public int getTotalInventoryWeightGrams()
  {
    Body body = this.status.getBody();
    int weight = 0;
    Item[] items = body.getAllItems();
    for (Item lItem : items) {
      weight += lItem.getFullWeight();
    }
    Item[] inventoryItems = this.possessions.getInventory().getAllItems(true);
    for (int x = 0; x < items.length; x++) {
      weight += inventoryItems[x].getFullWeight();
    }
    return weight;
  }
  
  public void startPersonalAction(short action, long subject, long _target)
  {
    try
    {
      BehaviourDispatcher.action(this, this.communicator, subject, _target, action);
    }
    catch (FailedException localFailedException) {}catch (NoSuchBehaviourException localNoSuchBehaviourException) {}catch (NoSuchCreatureException localNoSuchCreatureException) {}catch (NoSuchItemException localNoSuchItemException) {}catch (NoSuchPlayerException localNoSuchPlayerException) {}catch (NoSuchWallException localNoSuchWallException) {}
  }
  
  public void setFighting()
  {
    if (this.opponent != null)
    {
      if ((getPower() > 0) && (!isVisible()))
      {
        setOpponent(null);
        return;
      }
      try
      {
        Action lCurrentAction = null;
        try
        {
          lCurrentAction = getCurrentAction();
        }
        catch (NoSuchActionException localNoSuchActionException) {}
        if ((lCurrentAction == null) || (lCurrentAction.getNumber() != 114)) {
          BehaviourDispatcher.action(this, this.communicator, -1L, this.opponent.getWurmId(), (short)114);
        } else if (lCurrentAction != null) {
          sendToLoggers("busy " + lCurrentAction.getActionString() + " seconds " + lCurrentAction
            .getCounterAsFloat() + " " + lCurrentAction.getTarget() + ", path is null:" + (this.status
            .getPath() == null), (byte)4);
        }
        this.status.setPath(null);
      }
      catch (FailedException fe)
      {
        setOpponent(null);
      }
      catch (NoSuchBehaviourException nsb)
      {
        setTarget(-10L, true);
        setOpponent(null);
        
        logger.log(Level.WARNING, nsb.getMessage(), nsb);
      }
      catch (NoSuchCreatureException nsc)
      {
        setTarget(-10L, true);
        setOpponent(null);
      }
      catch (NoSuchItemException nsi)
      {
        setTarget(-10L, true);
        setOpponent(null);
        
        logger.log(Level.WARNING, nsi.getMessage(), nsi);
      }
      catch (NoSuchPlayerException nsp)
      {
        setTarget(-10L, true);
        setOpponent(null);
      }
      catch (NoSuchWallException nsw)
      {
        setOpponent(null);
        logger.log(Level.WARNING, nsw.getMessage(), nsw);
      }
    }
  }
  
  public void attackTarget()
  {
    if (this.target != -10L) {
      if ((this.opponent == null) || (this.opponent.getWurmId() != this.target))
      {
        long start = System.nanoTime();
        
        Creature tg = getTarget();
        if ((tg != null) && ((tg.isDead()) || (tg.isOffline())))
        {
          setTarget(-10L, true);
        }
        else if ((isDominated()) && (tg != null) && (tg.isDominated()) && (getDominator() == tg.getDominator()))
        {
          setTarget(-10L, true);
          setOpponent(null);
        }
        else if (tg != null)
        {
          if (rangeTo(this, tg) < com.wurmonline.server.behaviours.Actions.actionEntrys[114].getRange())
          {
            if ((!isPlayer()) && (tg.getFloorLevel() != getFloorLevel())) {
              if (isSpiritGuard())
              {
                pushToFloorLevel(getTarget().getFloorLevel());
              }
              else if (tg.getFloorLevel() != getFloorLevel())
              {
                Floor[] floors = getCurrentTile().getFloors(
                  Math.min(getFloorLevel(), tg.getFloorLevel()) * 30, 
                  Math.max(getFloorLevel(), tg.getFloorLevel()) * 30);
                for (Floor f : floors) {
                  if (tg.getFloorLevel() > getFloorLevel())
                  {
                    if (f.getFloorLevel() == getFloorLevel() + 1) {
                      if (((f.isOpening()) && (canOpenDoors())) || (f.isStair()))
                      {
                        pushToFloorLevel(f.getFloorLevel());
                        break;
                      }
                    }
                  }
                  else if (f.getFloorLevel() == getFloorLevel()) {
                    if (((f.isOpening()) && (canOpenDoors())) || (f.isStair()))
                    {
                      pushToFloorLevel(f.getFloorLevel() - 1);
                      break;
                    }
                  }
                }
              }
            }
            if (tg.getLayer() != getLayer()) {
              if ((!tg.getCurrentTile().isTransition) || (!getCurrentTile().isTransition)) {
                return;
              }
            }
            if ((tg != this.opponent) && (tg.getAttackers() >= tg.getMaxGroupAttackSize()))
            {
              ArrayList<MulticolorLineSegment> segments = new ArrayList();
              segments.add(new CreatureLineSegment(tg));
              segments.add(new MulticolorLineSegment(" is too crowded with attackers. You find no space.", (byte)0));
              
              getCommunicator().sendColoredMessageCombat(segments);
              
              return;
            }
            if (!CombatHandler.prerequisitesFail(this, tg, true, getPrimWeapon()))
            {
              if (!tg.isTeleporting())
              {
                setOpponent(tg);
                if ((!tg.isPlayer()) && (this.fightlevel > 1))
                {
                  this.fightlevel = ((byte)(this.fightlevel / 2));
                  if (isPlayer()) {
                    getCommunicator().sendFocusLevel(getWurmId());
                  }
                }
                if (!isPlayer()) {
                  this.status.setMoving(false);
                }
                ArrayList<MulticolorLineSegment> segments = new ArrayList();
                segments.add(new CreatureLineSegment(this));
                segments.add(new MulticolorLineSegment(" try to " + CombatEngine.getAttackString(this, getPrimWeapon()) + " ", (byte)0));
                segments.add(new CreatureLineSegment(tg));
                segments.add(new MulticolorLineSegment(".", (byte)0));
                
                getCommunicator().sendColoredMessageCombat(segments);
                if ((isPlayer()) || (isDominated()))
                {
                  ((MulticolorLineSegment)segments.get(1)).setText(" tries to " + CombatEngine.getAttackString(this, getPrimWeapon()) + " ");
                  tg.getCommunicator().sendColoredMessageCombat(segments);
                  if ((isDominated()) && (getDominator() != null) && (getDominator().isPlayer())) {
                    getDominator().getCommunicator().sendColoredMessageCombat(segments);
                  }
                }
                else
                {
                  ((MulticolorLineSegment)segments.get(1)).setText(" moves in to attack ");
                  tg.getCommunicator().sendColoredMessageCombat(segments);
                }
              }
            }
            else if ((!isPlayer()) && (Server.rand.nextInt(50) == 0)) {
              setTarget(-10L, true);
            }
          }
          else if (isSpellCaster())
          {
            if (rangeTo(this, tg) < 24) {
              if ((!isPlayer()) && (tg.getFloorLevel() == getFloorLevel())) {
                if (getLayer() == tg.getLayer()) {
                  if ((getFavor() >= 100.0F) && (Server.rand.nextInt(10) == 0))
                  {
                    setOpponent(tg);
                    short spellAction = 420;
                    switch (this.template.getTemplateId())
                    {
                    case 110: 
                      if (Server.rand.nextInt(3) == 0) {
                        spellAction = 485;
                      }
                      if (Server.rand.nextBoolean()) {
                        spellAction = 414;
                      }
                      break;
                    case 111: 
                      if (Server.rand.nextInt(3) == 0) {
                        spellAction = 550;
                      }
                      if (Server.rand.nextBoolean()) {
                        spellAction = 549;
                      }
                      break;
                    default: 
                      spellAction = 420;
                    }
                    if (this.opponent != null) {
                      try
                      {
                        long itemId = -10L;
                        try
                        {
                          Item bodyHand = getBody().getBodyPart(14);
                          itemId = bodyHand.getWurmId();
                        }
                        catch (Exception ex)
                        {
                          logger.log(Level.INFO, getName() + ": No hand.");
                        }
                        if ((spellAction == 420) || (spellAction == 414)) {
                          BehaviourDispatcher.action(this, this.communicator, itemId, Tiles.getTileId(this.opponent.getTileX(), this.opponent.getTileY(), 0), spellAction);
                        } else {
                          BehaviourDispatcher.action(this, this.communicator, itemId, this.opponent.getWurmId(), spellAction);
                        }
                      }
                      catch (Exception ex)
                      {
                        logger.log(Level.INFO, getName() + " casting " + spellAction + ":" + ex.getMessage(), ex);
                      }
                    }
                  }
                }
              }
            }
          }
        }
        else
        {
          setTarget(-10L, true);
        }
      }
    }
  }
  
  public void moan()
  {
    if (isDominated())
    {
      if (getDominator() != null) {
        getDominator().getCommunicator().sendNormalServerMessage("You sense a disturbance in " + getNameWithGenus() + ".");
      }
      if (isAnimal()) {
        Server.getInstance().broadCastAction(getNameWithGenus() + " grunts.", this, 5);
      } else {
        Server.getInstance().broadCastAction(getNameWithGenus() + " moans.", this, 5);
      }
    }
  }
  
  private void frolic()
  {
    if (isDominated())
    {
      if (getDominator() != null) {
        getDominator().getCommunicator().sendNormalServerMessage("You sense a sudden calm in " + getNameWithGenus() + ".");
      }
      if (isAnimal()) {
        Server.getInstance().broadCastAction(getNameWithGenus() + " purrs.", this, 5);
      } else {
        Server.getInstance().broadCastAction(getNameWithGenus() + " hizzes.", this, 5);
      }
    }
  }
  
  private boolean isOutOfBounds()
  {
    return (getTileX() < 0) || (getTileX() > Zones.worldTileSizeX - 1) || (getTileY() < 0) || 
      (getTileY() > Zones.worldTileSizeY - 1);
  }
  
  private boolean isFlying()
  {
    return false;
  }
  
  public boolean healRandomWound(int power)
  {
    if (getBody().getWounds() != null)
    {
      Wound[] wounds = getBody().getWounds().getWounds();
      if (wounds.length > 0)
      {
        int num = Server.rand.nextInt(wounds.length);
        if (wounds[num].getSeverity() / 1000.0F < power)
        {
          wounds[num].heal();
          return true;
        }
        wounds[num].modifySeverity(-power * 1000);
        return true;
      }
    }
    return false;
  }
  
  protected void decreaseOpportunityCounter()
  {
    if (this.opportunityAttackCounter > 0) {
      this.opportunityAttackCounter = ((byte)(this.opportunityAttackCounter - 1));
    }
  }
  
  private int lastSecond = 1;
  static long firstCreature = -10L;
  static int pollChecksPer = 301;
  static final int breedPollCounter = 201;
  int breedTick = 0;
  private int lastPolled = Server.rand.nextInt(pollChecksPer);
  private CreatureAIData aiData = null;
  
  public CreatureAIData getCreatureAIData()
  {
    if (this.template.getCreatureAI() != null)
    {
      if (this.aiData == null)
      {
        this.aiData = this.template.getCreatureAI().createCreatureAIData();
        this.aiData.setCreature(this);
      }
      return this.aiData;
    }
    return null;
  }
  
  public boolean poll()
    throws Exception
  {
    if (this.template.getCreatureAI() != null)
    {
      boolean toDestroy = this.template.getCreatureAI().pollCreature(this, System.currentTimeMillis() - getCreatureAIData().getLastPollTime());
      getCreatureAIData().setLastPollTime(System.currentTimeMillis());
      
      return toDestroy;
    }
    if (this.breedTick++ >= 201)
    {
      checkBreedCounter();
      this.breedTick = 0;
    }
    if (isNpcTrader()) {
      if (this.heatCheckTick++ >= 600)
      {
        getInventory().pollCoolingItems(this, 600000L);
        this.heatCheckTick = 0;
      }
    }
    if ((isVisibleToPlayers()) || (isTrader()) || (this.lastPolled == 0) || (this.status.getPath() != null) || (this.target != -10L) || (isUnique()) || (isNpc()))
    {
      if (firstCreature == -10L) {
        firstCreature = getWurmId();
      }
      this.lastPolled = (pollChecksPer - 1);
    }
    else
    {
      this.lastPolled -= 1;
      return false;
    }
    long start = System.nanoTime();
    try
    {
      if (this.fleeCounter > 0) {
        this.fleeCounter = ((byte)(this.fleeCounter - 1));
      }
      setHugeMoveCounter(getHugeMoveCounter() - 1);
      decreaseOpportunityCounter();
      if (this.guardSecondsLeft > 0) {
        this.guardSecondsLeft = ((byte)(this.guardSecondsLeft - 1));
      }
      if (getPathfindCounter() > 100)
      {
        if (isSpiritGuard()) {
          logger.log(Level.WARNING, getName() + " " + getWurmId() + " pathfind " + getPathfindCounter() + ". Target was " + this.target + ". Surfaced=" + 
            isOnSurface());
        }
        setPathfindcounter(0);
        
        setTarget(-10L, true);
        if (isDominated())
        {
          logger.log(Level.WARNING, getName() + " was dominated and failed to find path.");
          if (getDominator() != null) {
            getDominator().getCommunicator().sendNormalServerMessage("The " + 
              getName() + " fails to follow your orders.");
          }
          if (this.decisions != null) {
            this.decisions.clearOrders();
          }
        }
      }
      boolean bool1;
      if (getTemplate().getTemplateId() == 88)
      {
        if ((!WurmCalendar.isNight()) && 
          (getLayer() >= 0))
        {
          die(false, "Wraith in Daylight");
          float lElapsedTime;
          return true;
        }
      }
      else if (isOutOfBounds())
      {
        handleCreatureOutOfBounds();
        float lElapsedTime;
        return true;
      }
      if ((this.opponentCounter > 0) && (this.opponent == null)) {
        if (--this.opponentCounter == 0)
        {
          this.lastOpponent = null;
          getCombatHandler().setCurrentStance(-1, (byte)15);
          this.combatRound = 0;
        }
      }
      this.status.pollDetectInvis();
      if (isStunned()) {
        getStatus().setStunned((byte)(int)(getStatus().getStunned() - 1.0F), false);
      }
      boolean disease;
      if (!isDead())
      {
        if (getSpellEffects() != null) {
          getSpellEffects().poll();
        }
        pollNPCChat();
        if (this.actions.poll(this))
        {
          attackTarget();
          if (isFighting()) {
            setFighting();
          } else if (!isDead())
          {
            if (Server.getSecondsUptime() != this.lastSecond)
            {
              this.lastSecond = Server.getSecondsUptime();
              if ((!isRidden()) && (isNeedFood()) && (canEat())) {
                if (Server.rand.nextInt(60) == 0)
                {
                  findFood();
                  if (hasTrait(7)) {
                    if (Zone.hasSpring(getTileX(), getTileY())) {
                      if (Server.rand.nextInt(5) == 0) {
                        frolic();
                      }
                    }
                  }
                  if ((!isRidden()) && (hasTrait(12)) && (Server.rand.nextInt(10) == 0)) {
                    if (getLeader() != null)
                    {
                      Server.getInstance().broadCastAction(getName() + " refuses to move on.", this, 5);
                      setLeader(null);
                    }
                  }
                }
              }
              checkStealthing();
              pollNPC();
              checkEggLaying();
              if ((!isRidden()) && (!pollAge()))
              {
                checkMove();
                startUsingPath();
              }
              if (getStatus().pollFat())
              {
                disease = getStatus().disease >= 100;
                String deathCause = "starvation";
                if (disease) {
                  deathCause = "disease";
                }
                Server.getInstance().broadCastAction(
                  getNameWithGenus() + " rolls with the eyes, ejects " + getHisHerItsString() + " tongue and dies from " + deathCause + ".", this, 5);
                
                logger.log(Level.INFO, getName() + " dies from " + deathCause + ".");
                die(false, deathCause);
              }
              else
              {
                checkForEnemies();
              }
            }
          }
          else {
            logger.log(Level.INFO, getName() + " died when attacking?");
          }
        }
      }
      if ((this.webArmourModTime > 0.0F) && (this.webArmourModTime-- <= 1.0F))
      {
        this.webArmourModTime = 0.0F;
        if (getMovementScheme().setWebArmourMod(false, 0.0F)) {
          getMovementScheme().setWebArmourMod(false, 0.0F);
        }
        if ((!isFighting()) && (this.fightlevel > 0))
        {
          this.fightlevel = ((byte)Math.max(0, this.fightlevel - 1));
          if (isPlayer()) {
            getCommunicator().sendFocusLevel(getWurmId());
          }
        }
      }
      if (System.currentTimeMillis() - this.lastSavedPos > 3600000L)
      {
        this.lastSavedPos = (System.currentTimeMillis() + Server.rand.nextInt(3600) * 1000);
        savePosition(this.status.getZoneId());
        getStatus().save();
        if ((getTemplateId() == 78) || (getTemplateId() == 79) || 
          (getTemplateId() == 80) || (getTemplateId() == 81) || 
          (getTemplateId() == 68)) {
          if (!EpicServerStatus.doesGiveItemMissionExist(getWurmId()))
          {
            float lElapsedTime;
            return true;
          }
        }
      }
      if (!this.status.dead)
      {
        if (this.damageCounter > 0)
        {
          this.damageCounter = ((short)(this.damageCounter - 1));
          if (this.damageCounter <= 0)
          {
            removeWoundMod();
            getStatus().sendStateString();
          }
        }
        breakout();
        pollItems();
        if (this.tradeHandler != null) {
          this.tradeHandler.balance();
        }
        sendItemsTaken();
        sendItemsDropped();
        if (isVehicle()) {
          pollMount();
        }
        if (getBody() != null) {
          getBody().poll();
        } else {
          logger.log(Level.WARNING, getName() + "'s body is null.");
        }
        if (this.template.isMilkable()) {
          if ((!canEat()) && (Server.rand.nextInt(7200) == 0)) {
            setMilked(false);
          }
        }
        if (this.template.isWoolProducer())
        {
          if ((!canEat()) && (Server.rand.nextInt(14400) == 0)) {
            setSheared(false);
          }
        }
        else {
          removeRandomItems();
        }
        pollStamina();
        pollFavor();
        pollLoyalty();
        trimAttackers(false);
        this.numattackers = 0;
        this.hasAddedToAttack = false;
        if (isSpiritGuard()) {
          if ((this.citizenVillage != null) && (this.target == -10L) && (this.citizenVillage.targets.size() > 0)) {
            this.citizenVillage.assignTargets();
          }
        }
        if ((this.hitchedTo != null) || (isRidden())) {
          this.goOffline = false;
        }
        if ((!isUnique()) && (this.goOffline) && (!isFighting()) && (isDominated()) && 
          (Players.getInstance().getPlayerOrNull(this.dominator) == null))
        {
          logger.log(Level.INFO, getName() + " going offline.");
          Creatures.getInstance().setCreatureOffline(this);
          this.goOffline = false;
          float lElapsedTime;
          return true;
        }
        float lElapsedTime;
        return (isTransferring()) || (!isOnCurrentServer());
      }
      float[] xy;
      if (this.respawnCounter > 0)
      {
        this.respawnCounter -= 1;
        if (this.respawnCounter == 0)
        {
          xy = Player.findRandomSpawnX(true, true);
          try
          {
            setLayer(0, true);
            setPositionX(xy[0]);
            setPositionY(xy[1]);
            setPositionZ(calculatePosZ());
            respawn();
            Zone zone = Zones.getZone(getTileX(), getTileY(), isOnSurface());
            zone.addCreature(getWurmId());
            savePosition(zone.getId());
            float lElapsedTime;
            return false;
          }
          catch (NoSuchZoneException localNoSuchZoneException) {}catch (NoSuchCreatureException localNoSuchCreatureException) {}catch (NoSuchPlayerException localNoSuchPlayerException) {}catch (Exception localException) {}
        }
      }
      float lElapsedTime;
      return true;
    }
    finally
    {
      this.shouldStandStill = false;
      float lElapsedTime = (float)(System.nanoTime() - start) / 1000000.0F;
      if (lElapsedTime > (float)LOG_ELAPSED_TIME_THRESHOLD) {
        logger.info("Polled Creature id, " + getWurmId() + ", which took " + lElapsedTime + " millis.");
      }
    }
  }
  
  public void setWebArmourModTime(float time)
  {
    this.webArmourModTime = time;
  }
  
  public boolean isSpellCaster()
  {
    return this.template.isCaster();
  }
  
  public boolean isSummoner()
  {
    return this.template.isSummoner();
  }
  
  public boolean isRespawn()
  {
    return false;
  }
  
  private void handleCreatureOutOfBounds()
  {
    logger.log(Level.WARNING, getName() + " was out of bounds. Killing.");
    Creatures.getInstance().setCreatureDead(this);
    Players.getInstance().setCreatureDead(this);
    destroy();
  }
  
  protected void checkBreedCounter()
  {
    if (this.breedCounter > 0) {
      this.breedCounter -= 201;
    }
    if (this.breedCounter < 0) {
      this.breedCounter = 0;
    }
    if (this.breedCounter == 0)
    {
      if ((this.leader == null) && (!isDominated())) {
        if (isInTheMoodToBreed(false)) {
          checkBreedingPossibility();
        }
      }
      float mod = (float)Servers.localServer.getBreedingTimer();
      if (mod <= 0.0F) {
        mod = 1.0F;
      }
      int base = (int)(84000.0F / mod);
      if (checkPregnancy(false))
      {
        base = (int)(Servers.isThisAPvpServer() ? 2000.0F / mod : 84000.0F / mod);
        this.forcedBreed = true;
      }
      else
      {
        base = (int)(Servers.isThisAPvpServer() ? 900.0F / mod : 2000.0F / mod);
        this.forcedBreed = false;
      }
      this.breedCounter = (base + (int)(Server.rand.nextInt(Math.max(1000, 100 * Math.abs(20 - getStatus().age))) / mod));
    }
  }
  
  public void pollLoyalty()
  {
    if (isDominated()) {
      if (getStatus().pollLoyalty())
      {
        if (getDominator() != null)
        {
          getDominator().getCommunicator().sendAlertServerMessage(getNameWithGenus() + " is tame no more.", (byte)2);
          if (getDominator().getPet() == this) {
            getDominator().setPet(-10L);
          }
        }
        setDominator(-10L);
      }
    }
  }
  
  public boolean isInRock()
  {
    if (getLayer() < 0) {
      if (Tiles.isSolidCave(Tiles.decodeType(Server.caveMesh.getTile(getTileX(), getTileY())))) {
        return true;
      }
    }
    return false;
  }
  
  public void findFood()
  {
    if (this.currentTile != null) {
      if (!graze())
      {
        Item[] items = this.currentTile.getItems();
        for (Item lItem : items) {
          if (lItem.isEdibleBy(this))
          {
            if (lItem.getTemplateId() != 272)
            {
              eat(lItem);
              return;
            }
            if (lItem.isCorpseLootable())
            {
              eat(lItem);
              return;
            }
          }
        }
      }
    }
  }
  
  public int eat(Item item)
  {
    int hungerStilled = MethodsItems.eat(this, item);
    if (hungerStilled > 0)
    {
      getStatus().modifyHunger(-hungerStilled, item.getNutritionLevel());
      Server.getInstance().broadCastAction(
        getNameWithGenus() + " eats " + item.getNameWithGenus() + ".", this, 5);
    }
    else if (item.getTemplateId() != 272)
    {
      Server.getInstance().broadCastAction(
        getNameWithGenus() + " eats " + item.getNameWithGenus() + ".", this, 5);
    }
    return hungerStilled;
  }
  
  public boolean graze()
  {
    if ((isGrazer()) && (isOnSurface()))
    {
      if (hasTrait(13))
      {
        if (Server.rand.nextBoolean())
        {
          try
          {
            Skill str = this.skills.getSkill(102);
            if (str.getKnowledge() > 15.0D) {
              str.setKnowledge(str.getKnowledge() - 0.003000000026077032D, false);
            }
          }
          catch (NoSuchSkillException nss)
          {
            this.skills.learn(102, 20.0F);
          }
          return false;
        }
      }
      else if (Server.rand.nextBoolean()) {
        try
        {
          Skill str = this.skills.getSkill(102);
          double templateStr = getTemplate().getSkills().getSkill(102).getKnowledge();
          if (str.getKnowledge() < templateStr) {
            str.setKnowledge(str.getKnowledge() + 0.029999999329447746D, false);
          }
        }
        catch (NoSuchSkillException e)
        {
          this.skills.learn(102, 20.0F);
        }
        catch (Exception localException) {}
      }
      int tile = Server.surfaceMesh.getTile(this.currentTile.tilex, this.currentTile.tiley);
      byte type = Tiles.decodeType(tile);
      Village v = Villages.getVillage(this.currentTile.tilex, this.currentTile.tiley, this.currentTile.isOnSurface());
      if (!hasTrait(22)) {
        return grazeNonCorrupt(tile, type, v);
      }
      return grazeCorrupt(tile, type);
    }
    return false;
  }
  
  private boolean grazeCorrupt(int tile, byte type)
  {
    if ((type == Tiles.Tile.TILE_MYCELIUM.id) || (type == Tiles.Tile.TILE_FIELD.id) || (type == Tiles.Tile.TILE_FIELD2.id))
    {
      getStatus().modifyHunger(55536, 0.9F);
      if (Server.rand.nextInt(20) == 0) {
        if ((type == Tiles.Tile.TILE_FIELD.id) || (type == Tiles.Tile.TILE_FIELD2.id))
        {
          TileFieldBehaviour.graze(this.currentTile.tilex, this.currentTile.tiley, tile);
        }
        else if (type == Tiles.Tile.TILE_MYCELIUM.id)
        {
          GrassData.GrowthStage growthStage = GrassData.GrowthStage.decodeTileData(Tiles.decodeData(tile));
          if (growthStage == GrassData.GrowthStage.SHORT)
          {
            Server.setSurfaceTile(this.currentTile.tilex, this.currentTile.tiley, 
              Tiles.decodeHeight(tile), Tiles.Tile.TILE_DIRT_PACKED.id, (byte)0);
          }
          else
          {
            growthStage = growthStage.getPreviousStage();
            Server.setSurfaceTile(this.currentTile.tilex, this.currentTile.tiley, 
              Tiles.decodeHeight(tile), Tiles.Tile.TILE_MYCELIUM.id, 
              GrassData.encodeGrassTileData(growthStage, GrassData.FlowerType.NONE));
          }
          Players.getInstance().sendChangedTile(this.currentTile.tilex, this.currentTile.tiley, true, true);
        }
      }
      Server.getInstance().broadCastAction(getNameWithGenus() + " grazes.", this, 5);
      return true;
    }
    return false;
  }
  
  private boolean grazeNonCorrupt(int tile, byte type, Village v)
  {
    if ((type == Tiles.Tile.TILE_GRASS.id) || (type == Tiles.Tile.TILE_FIELD.id) || (type == Tiles.Tile.TILE_FIELD2.id) || (type == Tiles.Tile.TILE_STEPPE.id) || (type == Tiles.Tile.TILE_ENCHANTED_GRASS.id))
    {
      getStatus().modifyHunger(55536, type == Tiles.Tile.TILE_STEPPE.id ? 0.5F : 0.9F);
      if (Server.rand.nextInt(20) == 0)
      {
        int enchGrassPackChance = 120;
        if (v == null) {
          enchGrassPackChance = 80;
        } else if (v.getCreatureRatio() > Village.OPTIMUMCRETRATIO) {
          enchGrassPackChance = 240;
        }
        if ((type == Tiles.Tile.TILE_FIELD.id) || (type == Tiles.Tile.TILE_FIELD2.id))
        {
          TileFieldBehaviour.graze(this.currentTile.tilex, this.currentTile.tiley, tile);
        }
        else if ((type == Tiles.Tile.TILE_GRASS.id) || (type == Tiles.Tile.TILE_STEPPE.id) || ((type == Tiles.Tile.TILE_ENCHANTED_GRASS.id) && 
          (Server.rand.nextInt(enchGrassPackChance) == 0)))
        {
          GrassData.GrowthStage growthStage = GrassData.GrowthStage.decodeTileData(Tiles.decodeData(tile));
          if (growthStage == GrassData.GrowthStage.SHORT)
          {
            Server.setSurfaceTile(this.currentTile.tilex, this.currentTile.tiley, 
              Tiles.decodeHeight(tile), Tiles.Tile.TILE_DIRT_PACKED.id, (byte)0);
          }
          else
          {
            growthStage = growthStage.getPreviousStage();
            Server.setSurfaceTile(this.currentTile.tilex, this.currentTile.tiley, 
              Tiles.decodeHeight(tile), Tiles.Tile.TILE_GRASS.id, 
              GrassData.encodeGrassTileData(growthStage, GrassData.FlowerType.NONE));
          }
          Players.getInstance().sendChangedTile(this.currentTile.tilex, this.currentTile.tiley, true, true);
        }
      }
      Server.getInstance().broadCastAction(getNameWithGenus() + " grazes.", this, 5);
      return true;
    }
    return false;
  }
  
  public boolean pollAge()
  {
    long start = System.nanoTime();
    try
    {
      int maxAge = this.template.getMaxAge();
      if (isReborn()) {
        maxAge = 14;
      }
      if (getStatus().pollAge(maxAge))
      {
        sendDeathString();
        die(true, "Old Age");
        bool = true;return bool;
      }
      boolean bool = false;return bool;
    }
    finally {}
  }
  
  public void sendDeathString()
  {
    if (!isOffline())
    {
      String act = "hiccups";
      int x = Server.rand.nextInt(6);
      if (x == 0) {
        act = "drools";
      } else if (x == 1) {
        act = "faints";
      } else if (x == 2) {
        act = "makes a weird gurgly sound";
      } else if (x == 3) {
        act = "falls down";
      } else if (x == 4) {
        act = "rolls over";
      }
      Server.getInstance().broadCastAction(getNameWithGenus() + " " + act + " and dies.", this, 5);
    }
  }
  
  public void pollFavor()
  {
    if (((isSpellCaster()) || (isSummoner())) && 
      (Server.rand.nextInt(30) == 0)) {
      try
      {
        setFavor(getFavor() + 10.0F);
      }
      catch (Exception localException) {}
    }
  }
  
  public boolean isSalesman()
  {
    return this.template.getTemplateId() == 9;
  }
  
  public boolean isAvatar()
  {
    return (this.template.getTemplateId() == 78) || 
      (this.template.getTemplateId() == 79) || 
      (this.template.getTemplateId() == 80) || 
      (this.template.getTemplateId() == 81) || 
      (this.template.getTemplateId() == 68);
  }
  
  public void removeRandomItems()
  {
    if (!isTrading()) {
      if (isNpcTrader()) {
        if (Server.rand.nextInt(86400) == 0) {
          try
          {
            this.actions.getCurrentAction();
          }
          catch (NoSuchActionException nsa)
          {
            Shop myshop = Economy.getEconomy().getShop(this);
            if (myshop.getOwnerId() == -10L)
            {
              Shop kingsMoney = Economy.getEconomy().getKingsShop();
              if (kingsMoney.getMoney() > 0L)
              {
                int value = 0;
                
                value = (int)(kingsMoney.getMoney() / Shop.getNumTraders());
                if (!Servers.localServer.HOMESERVER)
                {
                  value = (int)(value * (1.0F + Zones.getPercentLandForKingdom(getKingdomId()) / 100.0F));
                  value = (int)(value * (1.0F + Items.getBattleCampControl(getKingdomId()) / 10.0F));
                }
                if (value > 0) {
                  if (myshop != null) {
                    if (myshop.getMoney() < Servers.localServer.getTraderMaxIrons()) {
                      if ((myshop.getSellRatio() > 0.1F) || (Server.getInstance().isPS())) {
                        if ((Server.getInstance().isPS()) || (Servers.localServer.id != 15) || (kingsMoney.getMoney() > 2000000L))
                        {
                          myshop.setMoney(myshop.getMoney() + value);
                          kingsMoney.setMoney(kingsMoney.getMoney() - value);
                        }
                      }
                    }
                  }
                }
              }
            }
            else if (canAutoDismissMerchant(myshop))
            {
              try
              {
                Item sign = ItemFactory.createItem(209, 10.0F + Server.rand
                  .nextFloat() * 10.0F, getName());
                sign.setDescription("Due to poor business I have moved on. Thank you for your time. " + 
                  getName());
                
                sign.setLastOwnerId(myshop.getOwnerId());
                sign.putItemInfrontof(this);
                sign.setIsPlanted(true);
              }
              catch (Exception e)
              {
                logger.log(Level.WARNING, e.getMessage() + " " + getName() + " at " + 
                  getTileX() + ", " + getTileY(), e);
              }
              TraderManagementQuestion.dismissMerchant(this, getWurmId());
            }
          }
        }
      }
    }
  }
  
  private boolean canAutoDismissMerchant(Shop myshop)
  {
    if (myshop.howLongEmpty() > 7257600000L) {
      return true;
    }
    PlayerInfo pinf = PlayerInfoFactory.getPlayerInfoWithWurmId(myshop.getOwnerId());
    if (pinf != null) {
      try
      {
        if (!pinf.loaded) {
          pinf.load();
        }
        if ((pinf.lastLogin == 0L) && (System.currentTimeMillis() - pinf.lastLogout > 7257600000L))
        {
          logger.log(Level.INFO, pinf
          
            .getName() + " last login was " + 
            Server.getTimeFor(System.currentTimeMillis() - pinf.lastLogout) + " ago.");
          
          return true;
        }
        return false;
      }
      catch (IOException localIOException) {}
    }
    return true;
  }
  
  public float getArmourMod()
  {
    return this.template.getNaturalArmour();
  }
  
  public final Vector2f getPos2f()
  {
    return getStatus().getPosition2f();
  }
  
  public final Vector3f getPos3f()
  {
    return getStatus().getPosition3f();
  }
  
  public final float getPosX()
  {
    return getStatus().getPositionX();
  }
  
  public final float getPosY()
  {
    return getStatus().getPositionY();
  }
  
  public final float getPositionZ()
  {
    return getStatus().getPositionZ();
  }
  
  @Nonnull
  public final TilePos getTilePos()
  {
    return TilePos.fromXY(getTileX(), getTileY());
  }
  
  public final int getTileX()
  {
    return (int)getPosX() >> 2;
  }
  
  public final int getTileY()
  {
    return (int)getPosY() >> 2;
  }
  
  public final int getPosZDirts()
  {
    return (int)(getPositionZ() * 10.0F);
  }
  
  public final void pollItems()
  {
    resetCompassLantern();
    this.pollCounter += 1;
    
    boolean triggerPoll = false;
    if (isHorse()) {
      if (getBody().getAllItems().length > 0) {
        triggerPoll = true;
      }
    }
    if ((isPlayer()) || (((!isReborn()) && (!isHuman())) || ((this.pollCounter > 10800) || ((triggerPoll) && (this.pollCounter > 60L)))))
    {
      if ((!this.checkedHotItemsAfterLogin) && (isPlayer()))
      {
        this.checkedHotItemsAfterLogin = true;
        long timeSinceLastCoolingCheck = System.currentTimeMillis() - PlayerInfoFactory.createPlayerInfo(getName()).getLastLogout();
        
        getInventory().pollCoolingItems(this, timeSinceLastCoolingCheck);
      }
      getInventory().pollOwned(this);
      getBody().getBodyItem().pollOwned(this);
      if (triggerPoll)
      {
        getInventory().pollCoolingItems(this, (this.pollCounter - 1) * 1000L);
        getBody().getBodyItem().pollCoolingItems(this, (this.pollCounter - 1) * 1000L);
      }
    }
    if ((this.pollCounter > 10800) || ((triggerPoll) && (this.pollCounter > 60L))) {
      this.pollCounter = 0;
    }
    pollCompassLantern();
  }
  
  public boolean isLastDeath()
  {
    return false;
  }
  
  public boolean isOnHostileHomeServer()
  {
    return false;
  }
  
  public final void setReputationEffects()
  {
    if (Servers.localServer.HOMESERVER) {
      if (((!isPlayer()) && (isDominated())) || (isRidden()) || (getHitched() != null)) {
        if (this.attackers != null) {
          for (Long attl : this.attackers.keySet()) {
            try
            {
              Creature attacker = Server.getInstance().getCreature(attl.longValue());
              if ((attacker.isPlayer()) || (attacker.isDominated())) {
                if (isRidden())
                {
                  if ((attacker.getCitizenVillage() == null) || 
                    (getCurrentVillage() != attacker.getCitizenVillage())) {
                    for (Long riderLong : getRiders()) {
                      try
                      {
                        Creature rider = Server.getInstance().getCreature(riderLong.longValue());
                        if ((rider != attacker) && 
                          (!rider.isOkToKillBy(attacker)))
                        {
                          attacker.setUnmotivatedAttacker();
                          attacker.setReputation(attacker.getReputation() - 10);
                        }
                      }
                      catch (NoSuchPlayerException localNoSuchPlayerException) {}
                    }
                  }
                }
                else if (getHitched() != null)
                {
                  if ((attacker.getCitizenVillage() == null) || 
                    (getCurrentVillage() != attacker.getCitizenVillage())) {
                    if (!getHitched().isCreature()) {
                      try
                      {
                        Item i = Items.getItem(getHitched().wurmid);
                        long ownid = i.getLastOwnerId();
                        if (ownid != attacker.getWurmId()) {
                          try
                          {
                            byte kingd = Players.getInstance().getKingdomForPlayer(ownid);
                            if ((attacker.isFriendlyKingdom(kingd)) && 
                              (!attacker.hasBeenAttackedBy(ownid)))
                            {
                              boolean ok = false;
                              try
                              {
                                Creature owner = Server.getInstance().getCreature(ownid);
                                if (owner.isOkToKillBy(attacker)) {
                                  ok = true;
                                }
                              }
                              catch (NoSuchCreatureException localNoSuchCreatureException) {}
                              if (!ok)
                              {
                                attacker.setUnmotivatedAttacker();
                                attacker.setReputation(attacker.getReputation() - 10);
                              }
                            }
                          }
                          catch (Exception localException) {}
                        }
                      }
                      catch (NoSuchItemException nsi)
                      {
                        logger.log(Level.INFO, getHitched().wurmid + " no such item:", nsi);
                      }
                    }
                  }
                }
                else if (isDominated())
                {
                  if (attacker.isFriendlyKingdom(getKingdomId()))
                  {
                    boolean ok = false;
                    try
                    {
                      Creature owner = Server.getInstance().getCreature(this.dominator);
                      if ((attacker == owner) || (owner.isOkToKillBy(attacker))) {
                        ok = true;
                      }
                    }
                    catch (NoSuchCreatureException localNoSuchCreatureException1) {}
                    if (!ok)
                    {
                      attacker.setUnmotivatedAttacker();
                      attacker.setReputation(attacker.getReputation() - 10);
                    }
                  }
                }
                else if (getCurrentVillage() != null)
                {
                  Brand brand = Creatures.getInstance().getBrand(getWurmId());
                  if (brand != null) {
                    try
                    {
                      Village villageBrand = Villages.getVillage((int)brand.getBrandId());
                      if (getCurrentVillage() == villageBrand) {
                        if (attacker.getCitizenVillage() != villageBrand)
                        {
                          attacker.setUnmotivatedAttacker();
                          attacker.setReputation(attacker.getReputation() - 10);
                        }
                      }
                    }
                    catch (NoSuchVillageException nsv)
                    {
                      brand.deleteBrand();
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
  
  public void die(boolean freeDeath, String reasonOfDeath)
  {
    WcKillCommand wkc = new WcKillCommand(WurmId.getNextWCCommandId(), getWurmId());
    if (Servers.isThisLoginServer()) {
      wkc.sendFromLoginServer();
    } else {
      wkc.sendToLoginServer();
    }
    if (isPregnant()) {
      Offspring.deleteSettings(getWurmId());
    }
    if (getTemplate().getCreatureAI() != null)
    {
      boolean fullOverride = getTemplate().getCreatureAI().creatureDied(this);
      if (fullOverride) {
        return;
      }
    }
    if (getTemplate().getTemplateId() == 105)
    {
      try
      {
        Item water = ItemFactory.createItem(128, 100.0F, "");
        getInventory().insertItem(water);
      }
      catch (NoSuchTemplateException nst)
      {
        logger.log(Level.WARNING, getName() + " No template for item id " + 128);
      }
      catch (FailedException e)
      {
        logger.log(Level.WARNING, getName() + " failed for item id " + 128);
      }
      Weather weather = Server.getWeather();
      if (weather != null) {
        weather.modifyFogTarget(-0.025F);
      }
    }
    if ((isUnique()) && (!isReborn()))
    {
      Player[] ps = Players.getInstance().getPlayers();
      HashSet<Player> lootReceivers = new HashSet();
      for (Player p : ps) {
        if ((p != null) && (p.getInventory() != null) && 
          (p.isWithinDistanceTo(this, 300.0F)) && (p.isPaying())) {
          try
          {
            Item blood = ItemFactory.createItem(866, 100.0F, "");
            blood.setData2(this.template.getTemplateId());
            p.getInventory().insertItem(blood);
            lootReceivers.add(p);
          }
          catch (NoSuchTemplateException nst)
          {
            logger.log(Level.WARNING, p.getName() + " No template for item id " + 866);
          }
          catch (FailedException fe)
          {
            logger.log(Level.WARNING, p.getName() + " " + fe.getMessage() + ":" + 866);
          }
        }
      }
      setPathing(false, true);
      if (isDragon())
      {
        Object primeLooters = new HashSet();
        Object leecher = new HashSet();
        for (Player looter : lootReceivers)
        {
          bStrength = looter.getBodyStrength();
          Skill bControl = looter.getBodyControlSkill();
          Skill fighting = looter.getFightingSkill();
          if (((bStrength != null) && (bStrength.getRealKnowledge() >= 30.0D)) || ((bControl != null) && 
            (bControl.getRealKnowledge() >= 30.0D)) || ((fighting != null) && 
            (fighting.getRealKnowledge() >= 65.0D)) || 
            (looter.isPriest())) {
            ((Set)primeLooters).add(looter);
          } else {
            ((Set)leecher).add(looter);
          }
        }
        Skill bStrength;
        int lootTemplate = 371;
        if ((getTemplate().getTemplateId() == 16) || (getTemplate().getTemplateId() == 89) || 
          (getTemplate().getTemplateId() == 91) || 
          (getTemplate().getTemplateId() == 90) || 
          (getTemplate().getTemplateId() == 92)) {
          lootTemplate = 372;
        }
        try
        {
          distributeDragonScaleOrHide((Set)primeLooters, (Set)leecher, lootTemplate);
        }
        catch (NoSuchTemplateException nst)
        {
          logger.log(Level.WARNING, "No template for " + lootTemplate + "! Players to receive were:");
          bStrength = lootReceivers.iterator();
        }
        while (bStrength.hasNext())
        {
          Player p = (Player)bStrength.next();
          
          logger.log(Level.WARNING, p.getName());
        }
      }
    }
    removeIllusion();
    setReputationEffects();
    getCombatHandler().clearMoveStack();
    getCommunicator().setGroundOffset(0, true);
    setDoLavaDamage(false);
    setDoAreaEffect(false);
    if (isPlayer()) {
      for (int x = 0; x < 5; x++) {
        getStatus().decreaseFat();
      }
    }
    this.combatRound = 0;
    Item corpse = null;
    int tilex = getTileX();
    int tiley = getTileY();
    try
    {
      boolean wasHunted = hasAttackedUnmotivated();
      if (isPlayer())
      {
        Item i = getDraggedItem();
        if ((i != null) && (
          (i.getTemplateId() == 539) || (i.getTemplateId() == 186) || 
          (i.getTemplateId() == 445) || (i.getTemplateId() == 1125))) {
          achievement(72);
        }
        if (getVehicle() != -10L)
        {
          Vehicle vehic = Vehicles.getVehicleForId(getVehicle());
          if ((vehic != null) && (vehic.getPilotId() == getWurmId())) {
            try
            {
              Item c = Items.getItem(getVehicle());
              if (c.getTemplateId() == 539) {
                achievement(71);
              }
            }
            catch (NoSuchItemException localNoSuchItemException1) {}
          }
        }
        if (!PlonkData.DEATH.hasSeenThis(this)) {
          PlonkData.DEATH.trigger(this);
        }
      }
      if (getDraggedItem() != null) {
        MethodsItems.stopDragging(this, getDraggedItem());
      }
      stopLeading();
      if (this.leader != null) {
        this.leader.removeFollower(this);
      }
      clearLinks();
      disableLink();
      disembark(false);
      if (!hasNoServerSound()) {
        SoundPlayer.playSound(getDeathSound(), this, 1.6F);
      }
      if (this.musicPlayer != null) {
        this.musicPlayer.checkMUSIC_DYING1_SND();
      }
      Creatures.getInstance().setCreatureDead(this);
      Players.getInstance().setCreatureDead(this);
      if (getSpellEffects() != null) {
        getSpellEffects().destroy(true);
      }
      if (this.currentVillage != null) {
        this.currentVillage.removeTarget(getWurmId(), true);
      }
      setOpponent(null);
      this.target = -10L;
      try
      {
        getCurrentAction().stop(false);
      }
      catch (NoSuchActionException localNoSuchActionException1) {}
      this.actions.clear();
      if (isKing())
      {
        King king = King.getKing(getKingdomId());
        if (king != null)
        {
          if (king.getChallengeAcceptedDate() > 0L) {
            if (System.currentTimeMillis() > king.getChallengeAcceptedDate()) {
              king.setFailedChallenge();
            }
          }
          if (isInOwnDuelRing()) {
            if (!king.hasFailedAllChallenges()) {
              king.setFailedChallenge();
            }
          }
        }
      }
      getCommunicator().sendSafeServerMessage("You are dead.");
      getCommunicator().sendCombatSafeMessage("You are dead.");
      Server.getInstance().broadCastAction(getNameWithGenus() + " is dead. R.I.P.", this, 5);
      if ((!isPlayer()) && ((isTrader()) || (isSalesman()) || (isBartender()) || ((this.template != null) && ((this.template.id == 63) || (this.template.id == 62)))))
      {
        String message = "(" + getWurmId() + ") died at [" + getTileX() + ", " + getTileY() + "] surf=" + isOnSurface() + " with the reason of death being " + reasonOfDeath;
        int counter;
        if (this.attackers != null) {
          if (this.attackers.size() > 0)
          {
            message = message + ". numAttackers=" + this.attackers.size() + " :";
            
            counter = 0;
            for (localNoSuchItemException1 = this.attackers.keySet().iterator(); localNoSuchItemException1.hasNext();)
            {
              long playerID = ((Long)localNoSuchItemException1.next()).longValue();
              
              counter++;
              String name = PlayerInfoFactory.getPlayerName(playerID);
              if (name.equals("Unknown")) {
                try
                {
                  Creature cret = Creatures.getInstance().getCreature(playerID);
                  name = cret.getName();
                }
                catch (NoSuchCreatureException localNoSuchCreatureException) {}
              }
              message = message + " " + name + (counter == this.attackers.size() ? "." : ",");
            }
          }
        }
        Players.getInstance().sendGmMessage(null, getName(), message, false);
        
        String templateAndName = (getTemplate() != null ? getTemplate().getName() : "Important creature") + " " + getName() + " died";
        logger.warning(templateAndName + " " + message);
        WcTrelloDeaths wtd = new WcTrelloDeaths(templateAndName, message);
        wtd.sendToLoginServer();
      }
      int valueo;
      byte bitx;
      int valuex;
      VolaTile vvtile;
      if (((Servers.localServer.PVPSERVER) || (!Features.Feature.PVE_DEATHTABS.isEnabled())) || (
      
        (!isGhost()) && (!this.template.isNoCorpse()) && (
        (getCreatureAIData() == null) || ((getCreatureAIData() != null) && (getCreatureAIData().doesDropCorpse())))))
      {
        corpse = ItemFactory.createItem(272, 100.0F, null);
        corpse.setPosXY(getStatus().getPositionX(), getStatus().getPositionY());
        corpse.setPosZ(calculatePosZ());
        corpse.onBridge = getBridgeId();
        if (hasCustomSize()) {
          corpse.setSizes((int)(corpse.getSizeX() * (getSizeModX() & 0xFF) / 64.0F), 
            (int)(corpse.getSizeY() * (getSizeModY() & 0xFF) / 64.0F), 
            (int)(corpse.getSizeZ() * (getSizeModZ() & 0xFF) / 64.0F));
        }
        corpse.setRotation(normalizeAngle(getStatus().getRotation() - 180.0F));
        
        int nameLength = 10 + this.name.length() + getStatus().getAgeString().length() + 1 + getStatus().getTypeString().length();
        int nameLengthNoType = 10 + this.name.length() + getStatus().getAgeString().length();
        int nameLengthNoAge = 10 + this.name.length() + 1 + getStatus().getTypeString().length();
        if (isPlayer())
        {
          corpse.setName("corpse of " + this.name);
        }
        else if (nameLength < 40)
        {
          corpse.setName("corpse of " + 
            getStatus().getAgeString() + " " + (nameLength < 40 ? getStatus().getTypeString() : "") + this.name
            .toLowerCase());
        }
        else if (nameLengthNoAge < 40)
        {
          corpse.setName("corpse of " + getStatus().getTypeString() + this.name.toLowerCase());
        }
        else if (nameLengthNoType < 40)
        {
          corpse.setName("corpse of " + getStatus().getAgeString() + " " + this.name.toLowerCase());
        }
        else if (("corpse of " + this.name).length() < 40)
        {
          corpse.setName("corpse of " + this.name.toLowerCase());
        }
        else
        {
          StringTokenizer strt = new StringTokenizer(this.name.toLowerCase());
          int maxNumber = strt.countTokens();
          String coname = "corpse of " + strt.nextToken();
          int number = 1;
          while (strt.hasMoreTokens())
          {
            number++;
            String next = strt.nextToken();
            if ((maxNumber < 4) || ((maxNumber > 4) && (number > 4)))
            {
              if ((coname + " " + next).length() >= 40) {
                break;
              }
              coname = coname + " ";
              coname = coname + next;
            }
          }
          corpse.setName(coname);
        }
        byte extra1 = -1;
        byte extra2 = this.status.modtype;
        if ((this.template.isHorse) || (this.template.isBlackOrWhite)) {
          extra1 = this.template.getColourCode(this.status);
        }
        if (isReborn())
        {
          corpse.setDamage(20.0F);
          corpse.setButchered();
          corpse.setAllData(this.template.getTemplateId(), 1, extra1, extra2);
        }
        else
        {
          corpse.setAllData(this.template.getTemplateId(), getStatus().fat << 1, extra1, extra2);
        }
        int lootId;
        if (isUnique())
        {
          Server.getInstance().broadCastNormal(getNameWithGenus() + " has been slain.");
          if ((!Servers.localServer.EPIC) && (!isReborn())) {
            try
            {
              boolean drop = false;
              if (isDragon()) {
                drop = Server.rand.nextInt(10) == 0;
              } else {
                drop = Server.rand.nextBoolean();
              }
              if (drop)
              {
                int item = 795 + Server.rand.nextInt(16);
                if (item == 1009) {
                  item = 807;
                } else if (item == 805) {
                  item = 808;
                }
                Item epicItem = ItemFactory.createItem(item, 60 + Server.rand
                  .nextInt(20), "");
                epicItem.setOwnerId(corpse.getWurmId());
                epicItem.setLastOwnerId(corpse.getWurmId());
                if (isDragon()) {
                  epicItem.setAuxData((byte)2);
                }
                logger.info("Dropping a " + epicItem.getName() + " (" + epicItem.getWurmId() + ")  for the slaying of " + corpse.getName());
                corpse.insertItem(epicItem);
              }
            }
            catch (NoSuchTemplateException nst)
            {
              logger.log(Level.WARNING, "No template for item id 866");
            }
            catch (FailedException fe)
            {
              logger.log(Level.WARNING, fe.getMessage() + ":" + 866);
            }
          } else if ((Servers.localServer.EPIC) && (!Servers.localServer.HOMESERVER)) {
            if (isDragon()) {
              try
              {
                boolean dropLoot = Server.rand.nextBoolean();
                if (dropLoot)
                {
                  lootId = CreatureTemplateCreator.getDragonLoot(this.template.getTemplateId());
                  if (lootId > 0)
                  {
                    Item loot = ItemFactory.createItem(lootId, 60 + Server.rand
                      .nextInt(20), "");
                    logger.info("Dropping a " + loot.getName() + " (" + loot.getWurmId() + ") for the slaying of " + corpse.getName());
                    corpse.insertItem(loot);
                    loot.setOwnerId(corpse.getWurmId());
                  }
                }
              }
              catch (Exception localException1) {}
            }
          }
        }
        if ((isPlayer()) && (!wasHunted) && (getReputation() >= 0) && 
          (!isInPvPZone()) && (Servers.localServer.KINGDOM != 0) && 
          (!isOnHostileHomeServer()))
        {
          boolean killedInVillageWar = false;
          if (this.attackers != null) {
            for (Long l : this.attackers.keySet()) {
              try
              {
                Creature c = Creatures.getInstance().getCreature(l.longValue());
                if ((c.getCitizenVillage() != null) && (c.getCitizenVillage().isEnemy(this))) {
                  if (Servers.isThisAPvpServer())
                  {
                    logger.log(Level.INFO, getName() + " was killed by " + c.getName() + " during village war. May be looted.");
                    
                    killedInVillageWar = true;
                  }
                }
              }
              catch (Exception localException2) {}
            }
          }
          if (!killedInVillageWar) {
            corpse.setProtected(true);
          }
        }
        corpse.setAuxData(getKingdomId());
        corpse.setWeight((int)Math.min(50000.0F, this.status.body.getWeight(this.status.fat)), false);
        corpse.setLastOwnerId(getWurmId());
        if (isKingdomGuard()) {
          corpse.setDamage(50.0F);
        }
        if (getSex() == 1) {
          corpse.setFemale(true);
        }
        if (this.template.isHorse)
        {
          String col = this.template.getColourName(this.status);
          corpse.setDescription(col);
        }
        else if (this.template.isBlackOrWhite)
        {
          if ((!hasTrait(15)) && (!hasTrait(16)) && (!hasTrait(18)) && 
            (!hasTrait(24)) && (!hasTrait(25)) && (!hasTrait(23))) {
            if (hasTrait(17)) {
              corpse.setDescription("black");
            }
          }
        }
        if ((!isPlayer()) && (!Servers.isThisAPvpServer()))
        {
          Brand brand = Creatures.getInstance().getBrand(getWurmId());
          if (brand != null) {
            try
            {
              corpse.setWasBrandedTo(brand.getBrandId());
              
              PermissionsPlayerList allowedList = getPermissionsPlayerList();
              PermissionsByPlayer[] pbpList = allowedList.getPermissionsByPlayer();
              byte bito = ItemSettings.CorpsePermissions.COMMANDER.getBit();
              valueo = ItemSettings.CorpsePermissions.COMMANDER.getValue();
              bitx = ItemSettings.CorpsePermissions.EXCLUDE.getBit();
              valuex = ItemSettings.CorpsePermissions.EXCLUDE.getValue();
              Village bVill = null;
              for (PermissionsByPlayer pbp : pbpList) {
                if (pbp.getPlayerId() == -60L)
                {
                  if (bVill == null) {
                    bVill = Villages.getVillage((int)brand.getBrandId());
                  }
                  int value = 0;
                  if (pbp.hasPermission(bito)) {
                    value += valueo;
                  }
                  if (pbp.hasPermission(bitx)) {
                    value += valuex;
                  }
                  if (value != 0) {
                    for (Citizen citz : bVill.getCitizens()) {
                      if ((citz.isPlayer()) && (citz.getRole().mayBrand())) {
                        ItemSettings.addPlayer(corpse.getWurmId(), citz.wurmId, value);
                      }
                    }
                  }
                }
              }
              for (PermissionsByPlayer pbp : pbpList) {
                if (pbp.getPlayerId() != -60L)
                {
                  int value = 0;
                  if (pbp.hasPermission(bito)) {
                    value += valueo;
                  }
                  if (pbp.hasPermission(bitx)) {
                    value += valuex;
                  }
                  if (value != 0) {
                    ItemSettings.addPlayer(corpse.getWurmId(), pbp.getPlayerId(), value);
                  }
                }
              }
            }
            catch (NoSuchVillageException e)
            {
              Creatures.getInstance().setBrand(getWurmId(), -10L);
            }
          }
        }
        vvtile = Zones.getOrCreateTile(tilex, tiley, isOnSurface());
        vvtile.addItem(corpse, false, getWurmId(), false);
      }
      else if ((isGhost()) || (this.template.isNoCorpse()))
      {
        int[] butcheredItems = getTemplate().getItemsButchered();
        for (int x = 0; x < butcheredItems.length; x++) {
          try
          {
            ItemFactory.createItem(butcheredItems[x], 20.0F + Server.rand.nextFloat() * 80.0F, getPosX(), getPosY(), Server.rand
              .nextInt() * 360, isOnSurface(), (byte)0, getStatus().getBridgeId(), getName());
          }
          catch (FailedException fe)
          {
            logger.log(Level.WARNING, fe.getMessage());
          }
          catch (NoSuchTemplateException nst)
          {
            logger.log(Level.WARNING, nst.getMessage());
          }
        }
      }
      VolaTile vtile = Zones.getOrCreateTile(tilex, tiley, isOnSurface());
      boolean keepItems = isTransferring();
      if (!isOnCurrentServer()) {
        keepItems = true;
      }
      if ((getDeity() != null) && (getDeity().isDeathItemProtector()) && (getFaith() >= 70.0F) && (getFavor() >= 35.0F))
      {
        if (Server.rand.nextInt(2) > 0)
        {
          getCommunicator().sendNormalServerMessage(
            getDeity().name + " is with you and keeps your items safe!");
          keepItems = true;
        }
        else
        {
          getCommunicator().sendNormalServerMessage(
            getDeity().name + " can't keep your items safe this time.");
        }
      }
      else if (isDeathProtected()) {
        if (Server.rand.nextInt(2) > 0)
        {
          getCommunicator().sendNormalServerMessage("Etheral strands of web attach to your items and keep them safe, close to your spirit!");
          
          keepItems = true;
        }
        else
        {
          getCommunicator().sendNormalServerMessage("Your items could not be kept safe this time.");
        }
      }
      Set<Item> worn;
      if (isPlayer()) {
        try
        {
          Item legs = getBody().getBodyPart(19);
          boolean found = false;
          worn = legs.getItems();
          if (worn != null) {
            for (Item w : worn) {
              if (w.isArmour())
              {
                found = true;
                break;
              }
            }
          }
          if (!found) {
            pantLess.add(Long.valueOf(getWurmId()));
          }
        }
        catch (NoSpaceException localNoSpaceException) {}
      }
      boolean insertItem = true;
      
      boolean dropNewbieItems = false;
      if (this.attackers != null) {
        for (Long cid : this.attackers.keySet()) {
          if (WurmId.getType(cid.longValue()) == 0) {
            if ((!Servers.localServer.isChallengeServer()) || (getPlayingTime() > 86400000L))
            {
              dropNewbieItems = true;
              break;
            }
          }
        }
      }
      Item inventory = getInventory();
      Item[] invarr = inventory.getAllItems(true);
      for (int x = 0; x < invarr.length; x++)
      {
        if (invarr[x].isTraded()) {
          if (getTrade() != null) {
            invarr[x].getTradeWindow().removeItem(invarr[x]);
          }
        }
        boolean destroyChall = false;
        if (Features.Feature.FREE_ITEMS.isEnabled()) {
          if (invarr[x].isChallengeNewbieItem()) {
            if ((invarr[x].isArmour()) || (invarr[x].isWeapon()) || (invarr[x].isShield())) {
              destroyChall = true;
            }
          }
        }
        if (destroyChall) {
          Items.destroyItem(invarr[x].getWurmId());
        } else if ((invarr[x].isArtifact()) || ((!keepItems) && (!invarr[x].isNoDrop()) && (
          (!invarr[x].isNewbieItem()) || (dropNewbieItems) || ((invarr[x].isHollow()) && 
          (!invarr[x].isTent()))))) {
          try
          {
            Item parent = invarr[x].getParent();
            if ((inventory.equals(parent)) || (parent.getTemplateId() == 824))
            {
              parent.dropItem(invarr[x].getWurmId(), true);
              invarr[x].setBusy(false);
              if ((corpse == null) || (!corpse.insertItem(invarr[x], true))) {
                if ((invarr[x].isTent()) && (invarr[x].isNewbieItem())) {
                  Items.destroyItem(invarr[x].getWurmId());
                } else {
                  vtile.addItem(invarr[x], false, false);
                }
              }
            }
          }
          catch (NoSuchItemException nsi)
          {
            logger.log(Level.WARNING, getName() + " " + invarr[x].getName() + ":" + nsi.getMessage(), nsi);
          }
        } else if ((!invarr[x].isArtifact()) && (!keepItems)) {
          try
          {
            Item parent = invarr[x].getParent();
            invarr[x].setBusy(false);
            
            insertItem = !parent.isNoDrop();
            if (invarr[x].getTemplateId() == 443) {
              if ((getStrengthSkill() > 21.0D ? 1 : 0) == 0) {
                if ((getFaith() > 35.0F ? 1 : 0) == 0)
                {
                  insertItem = false;
                  if (!invarr[x].setDamage(invarr[x].getDamage() + 0.3F, true)) {
                    insertItem = true;
                  }
                }
              }
            }
            if (insertItem)
            {
              parent.dropItem(invarr[x].getWurmId(), false);
              inventory.insertItem(invarr[x], true);
            }
          }
          catch (NoSuchItemException nsi)
          {
            logger.log(Level.WARNING, getName() + " " + invarr[x].getName() + ":" + nsi.getMessage(), nsi);
          }
        }
      }
      Item[] boditems = getBody().getContainersAndWornItems();
      for (int x = 0; x < boditems.length; x++)
      {
        if (boditems[x].isTraded()) {
          if (getTrade() != null) {
            boditems[x].getTradeWindow().removeItem(boditems[x]);
          }
        }
        if ((boditems[x].isArtifact()) || ((!keepItems) && (!boditems[x].isNoDrop()) && (
          (!boditems[x].isNewbieItem()) || (dropNewbieItems) || ((boditems[x].isHollow()) && 
          (!boditems[x].isTent())))))
        {
          if (boditems[x].isHollow())
          {
            Item[] containedItems = boditems[x].getAllItems(false);
            for (Item lContainedItem : containedItems) {
              if ((lContainedItem.isNoDrop()) || (
                (lContainedItem.isNewbieItem()) && (!dropNewbieItems) && 
                (!lContainedItem.isHollow()))) {
                try
                {
                  lContainedItem.setBusy(false);
                  Item parent = lContainedItem.getParent();
                  parent.dropItem(lContainedItem.getWurmId(), false);
                  inventory.insertItem(lContainedItem, true);
                }
                catch (NoSuchItemException nsi)
                {
                  logger.log(Level.WARNING, getName() + ":" + nsi.getMessage(), nsi);
                }
              }
            }
          }
          try
          {
            Item parent = boditems[x].getParent();
            parent.dropItem(boditems[x].getWurmId(), true);
            boditems[x].setBusy(false);
            if ((corpse == null) || (!corpse.insertItem(boditems[x], true))) {
              if ((boditems[x].isTent()) && (boditems[x].isNewbieItem())) {
                Items.destroyItem(invarr[x].getWurmId());
              } else {
                vtile.addItem(boditems[x], false, false);
              }
            }
          }
          catch (NoSuchItemException nsi)
          {
            logger.log(Level.WARNING, getName() + ":" + nsi.getMessage(), nsi);
          }
        }
        else if ((!boditems[x].isArtifact()) && (!keepItems))
        {
          try
          {
            Item parent = boditems[x].getParent();
            boditems[x].setBusy(false);
            insertItem = !parent.isNoDrop();
            if (boditems[x].getTemplateId() == 443) {
              if ((getStrengthSkill() > 21.0D ? 1 : 0) == 0) {
                if ((getFaith() > 35.0F ? 1 : 0) == 0)
                {
                  insertItem = false;
                  if (!boditems[x].setDamage(boditems[x].getDamage() + 0.3F, true)) {
                    insertItem = true;
                  }
                }
              }
            }
            if (insertItem)
            {
              parent.dropItem(boditems[x].getWurmId(), false);
              inventory.insertItem(boditems[x], true);
            }
          }
          catch (NoSuchItemException nsi)
          {
            logger.log(Level.WARNING, getName() + " " + boditems[x].getName() + ":" + nsi.getMessage(), nsi);
          }
        }
      }
    }
    catch (FailedException fe)
    {
      logger.log(Level.WARNING, getName() + ":" + fe.getMessage(), fe);
    }
    catch (NoSuchTemplateException nst)
    {
      logger.log(Level.WARNING, getName() + ":" + nst.getMessage(), nst);
    }
    if (corpse != null)
    {
      if (isSuiciding()) {
        if (corpse.getAllItems(true).length == 0)
        {
          Items.destroyItem(corpse.getWurmId());
          corpse = null;
        }
      }
    }
    else {
      playAnimation("die", false);
    }
    try
    {
      setBridgeId(-10L);
      getBody().healFully();
    }
    catch (Exception ex)
    {
      logger.log(Level.WARNING, getName() + ex.getMessage(), ex);
    }
    if ((isTransferring()) || (!isOnCurrentServer())) {
      return;
    }
    if ((getTemplateId() == 78) || (getTemplateId() == 79) || 
      (getTemplateId() == 80) || (getTemplateId() == 81) || 
      (getTemplateId() == 68)) {
      EpicServerStatus.avatarCreatureKilled(getWurmId());
    }
    setDeathEffects(freeDeath, tilex, tiley);
    if (EpicServerStatus.doesTraitorMissionExist(getWurmId())) {
      EpicServerStatus.traitorCreatureKilled(getWurmId());
    }
  }
  
  private void distributeDragonScaleOrHide(Set<Player> primeLooters, Set<Player> leecher, int lootTemplate)
    throws NoSuchTemplateException
  {
    ItemTemplate itemt = ItemTemplateFactory.getInstance().getTemplate(lootTemplate);
    float lootNums = calculateDragonLootMultiplier();
    float totalWeightToDistribute = calculateDragonLootTotalWeight(itemt, lootNums) * (lootTemplate == 371 ? 3.0F : 1.0F);
    
    float leecherShare = 0.0F;
    if (leecher.size() > 0) {
      leecherShare = totalWeightToDistribute / 5.0F;
    }
    float primeShare = totalWeightToDistribute - leecherShare;
    if (leecher.size() > 0)
    {
      float lSplit = leecherShare / leecher.size();
      float pSplit = primeShare / primeLooters.size();
      if (lSplit > pSplit)
      {
        leecherShare = pSplit * 0.9F * leecher.size();
        primeShare = totalWeightToDistribute - leecherShare;
      }
    }
    splitDragonLootTo(primeLooters, itemt, lootTemplate, primeShare);
    splitDragonLootTo(leecher, itemt, lootTemplate, leecherShare);
  }
  
  private final float calculateDragonLootTotalWeight(ItemTemplate template, float lootMult)
  {
    return 1.0F + template.getWeightGrams() * lootMult;
  }
  
  private final float calculateDragonLootMultiplier()
  {
    float lootNums = 1.0F;
    if (!Servers.isThisAnEpicServer()) {
      lootNums = Math.max(1.0F, 1.0F + Server.rand.nextFloat() * 3.0F);
    }
    return lootNums;
  }
  
  private void splitDragonLootTo(Set<Player> lootReceivers, ItemTemplate itemt, int lootTemplate, float totalWeight)
  {
    if (lootReceivers.size() == 0) {
      return;
    }
    float receivers = lootReceivers.size();
    float weight = totalWeight / receivers;
    for (Player p : lootReceivers) {
      try
      {
        double power = 0.0D;
        try
        {
          Skill butchering = p.getSkills().getSkill(10059);
          power = Math.max(0.0D, butchering.skillCheck(10.0D, 0.0D, false, 10.0F));
        }
        catch (NoSuchSkillException nss)
        {
          Skill butchering = p.getSkills().learn(10059, 1.0F);
          power = Math.max(0.0D, butchering.skillCheck(10.0D, 0.0D, false, 10.0F));
        }
        Item loot = ItemFactory.createItem(lootTemplate, (float)(80.0D + power / 5.0D), "");
        String creatureName = getTemplate().getName().toLowerCase();
        if (!loot.getName().contains(creatureName)) {
          loot.setName(creatureName.toLowerCase() + " " + itemt.getName());
        }
        loot.setData2(this.template.getTemplateId());
        loot.setWeight((int)weight, true);
        p.getInventory().insertItem(loot);
        lootReceivers.add(p);
      }
      catch (NoSuchTemplateException nst)
      {
        logger.log(Level.WARNING, p.getName() + " No template for item id " + lootTemplate);
      }
      catch (FailedException fe)
      {
        logger.log(Level.WARNING, p.getName() + " " + fe.getMessage() + ":" + lootTemplate);
      }
    }
  }
  
  public boolean isSuiciding()
  {
    return false;
  }
  
  public Item[] getAllItems()
  {
    Set<Item> allitems = new HashSet();
    Item inventory = getInventory();
    allitems.add(inventory);
    Item body = getBody().getBodyItem();
    allitems.add(body);
    Item[] boditems = body.getAllItems(true);
    Item[] arrayOfItem1 = boditems;int i = arrayOfItem1.length;
    for (Item localItem1 = 0; localItem1 < i; localItem1++)
    {
      lBoditem = arrayOfItem1[localItem1];
      allitems.add(lBoditem);
    }
    Item[] invitems = inventory.getAllItems(true);
    Item[] arrayOfItem2 = invitems;localItem1 = arrayOfItem2.length;
    for (Item lBoditem = 0; lBoditem < localItem1; lBoditem++)
    {
      Item lInvitem = arrayOfItem2[lBoditem];
      allitems.add(lInvitem);
    }
    return (Item[])allitems.toArray(new Item[allitems.size()]);
  }
  
  public void checkWorkMusic()
  {
    if (this.musicPlayer != null) {
      this.musicPlayer.checkMUSIC_VILLAGEWORK_SND();
    }
  }
  
  public boolean isFightingSpiritGuard()
  {
    return (this.opponent != null) && (this.opponent.isSpiritGuard());
  }
  
  public boolean isFighting(long opponentid)
  {
    return (this.opponent != null) && (this.opponent.getWurmId() == opponentid);
  }
  
  public void setFleeCounter(int newCounter)
  {
    setFleeCounter(newCounter, false);
  }
  
  public void setFleeCounter(int newCounter, boolean warded)
  {
    if ((newCounter <= 0) || (newCounter < this.fleeCounter)) {
      return;
    }
    if (((!isPlayer()) && (!isUnique()) && ((!isDominated()) || (warded))) || (isPrey())) {
      if ((warded) || (isPrey()))
      {
        this.fleeCounter = ((byte)newCounter);
        sendToLoggers("updated flee counter: " + this.fleeCounter);
      }
    }
  }
  
  public void setTarget(long targ, boolean switchTarget)
  {
    if (targ == getWurmId()) {
      targ = -10L;
    }
    if (isPrey()) {
      return;
    }
    if ((targ != -10L) && (getVehicle() != -10L)) {
      try
      {
        Creature cret = Server.getInstance().getCreature(this.target);
        if (cret.getHitched() != null)
        {
          Vehicle v = Vehicles.getVehicleForId(getVehicle());
          if ((v != null) && (v == cret.getHitched()))
          {
            getCommunicator().sendNormalServerMessage("You cannot target " + cret.getName() + " while on the same vehicle.");
            targ = -10L;
          }
        }
      }
      catch (NoSuchPlayerException|NoSuchCreatureException localNoSuchPlayerException) {}
    }
    if (this.loggerCreature1 != -10L) {
      logger.log(Level.FINE, getName() + " target=" + targ, new Exception());
    }
    if (targ == -10L)
    {
      getCommunicator().sendCombatStatus(0.0F, 0.0F, (byte)0);
      if ((this.opponent != null) && (this.opponent.getWurmId() == this.target)) {
        setOpponent(null);
      }
      if (this.target != targ) {
        try
        {
          Creature cret = Server.getInstance().getCreature(this.target);
          cret.getCommunicator().changeAttitude(getWurmId(), getAttitude(cret));
        }
        catch (NoSuchCreatureException localNoSuchCreatureException) {}catch (NoSuchPlayerException localNoSuchPlayerException1) {}
      }
      this.target = targ;
      getCommunicator().sendTarget(targ);
      
      VolaTile t = Zones.getTileOrNull(getTileX(), getTileY(), isOnSurface());
      if (t != null) {
        t.sendUpdateTarget(this);
      }
      this.status.sendStateString();
    }
    else if ((this.target == -10L) || (switchTarget))
    {
      if ((this.target != targ) && ((getBaseCombatRating() > 10.0F) || (this.fleeCounter <= 0)))
      {
        if (this.target != -10L) {
          try
          {
            Creature cret = Server.getInstance().getCreature(this.target);
            cret.getCommunicator().changeAttitude(getWurmId(), getAttitude(cret));
          }
          catch (NoSuchCreatureException localNoSuchCreatureException1) {}catch (NoSuchPlayerException localNoSuchPlayerException2) {}
        }
        try
        {
          Creature cret = Server.getInstance().getCreature(targ);
          if ((isSpiritGuard()) && (this.citizenVillage != null))
          {
            VolaTile currTile = cret.getCurrentTile();
            if ((currTile.getTileX() < this.citizenVillage.getStartX() - 5) || 
              (currTile.getTileX() > this.citizenVillage.getEndX() + 5) || 
              (currTile.getTileY() < this.citizenVillage.getStartY() - 5) || 
              (currTile.getTileY() > this.citizenVillage.getEndY() + 5))
            {
              if (cret.opponent == this)
              {
                cret.setOpponent(null);
                cret.setTarget(-10L, true);
                cret.getCommunicator().sendNormalServerMessage("The " + 
                  getName() + " suddenly becomes hazy and hard to target.");
              }
              targ = -10L;
              setOpponent(null);
              if (this.status.getPath() == null) {
                getMoveTarget(0);
              }
            }
            else
            {
              this.citizenVillage.cryForHelp(this, false);
            }
          }
          if (targ != -10L) {
            cret.getCommunicator().changeAttitude(getWurmId(), getAttitude(cret));
          }
        }
        catch (NoSuchCreatureException localNoSuchCreatureException2) {}catch (NoSuchPlayerException localNoSuchPlayerException3) {}
        this.target = targ;
        getCommunicator().sendTarget(targ);
        
        VolaTile t = Zones.getTileOrNull(getTileX(), getTileY(), isOnSurface());
        if (t != null) {
          t.sendUpdateTarget(this);
        }
        this.status.sendStateString();
      }
    }
  }
  
  public boolean modifyFightSkill(int dtilex, int dtiley)
  {
    boolean pvp = false;
    Map<Creature, Double> lSkillReceivers = null;
    
    boolean activatedTrigger = false;
    double sumskill;
    HashSet<Creature> lootReceivers;
    Object psk;
    if (!isNoSkillgain())
    {
      lSkillReceivers = new HashMap();
      
      long now = System.currentTimeMillis();
      
      double kskill = 0.0D;
      sumskill = 0.0D;
      boolean wasHelped = false;
      if ((this.attackers != null) && (this.attackers.size() > 0))
      {
        ArrayList<Long> possibleTriggerOwners = new ArrayList();
        for (Iterator localIterator3 = this.attackers.keySet().iterator(); localIterator3.hasNext();)
        {
          long l = ((Long)localIterator3.next()).longValue();
          if ((now - ((Long)this.attackers.get(Long.valueOf(l))).longValue() < 600000L) && 
            (WurmId.getType(l) == 0) && (
            (!isPlayer()) || (!Players.getInstance().isOverKilling(l, getWurmId())))) {
            possibleTriggerOwners.add(Long.valueOf(l));
          }
        }
        if (!possibleTriggerOwners.isEmpty()) {
          try
          {
            Player player = Players.getInstance().getPlayer((Long)possibleTriggerOwners.get(Server.rand.nextInt(possibleTriggerOwners.size())));
            MissionTrigger[] trigs = MissionTriggers.getMissionTriggersWith(getTemplate().getTemplateId(), 491, getWurmId());
            MissionTrigger t;
            EpicMission em;
            float karmaGained;
            Iterator localIterator5;
            for (t : trigs)
            {
              em = EpicServerStatus.getEpicMissionForMission(t.getMissionRequired());
              if (em != null)
              {
                EpicMissionEnum missionEnum = EpicMissionEnum.getMissionForType(em.getMissionType());
                if ((missionEnum != null) && (EpicMissionEnum.isMissionKarmaGivenOnKill(missionEnum)))
                {
                  float karmaSplit = missionEnum.getKarmaBonusDiffMult() * em.getDifficulty();
                  karmaGained = karmaSplit / EpicServerStatus.getNumberRequired(em.getDifficulty(), missionEnum);
                  karmaGained = (float)Math.ceil(karmaGained / possibleTriggerOwners.size());
                  for (localIterator5 = possibleTriggerOwners.iterator(); localIterator5.hasNext();)
                  {
                    long id = ((Long)localIterator5.next()).longValue();
                    try
                    {
                      Player p = Players.getInstance().getPlayer(id);
                      if ((Deities.getFavoredKingdom(em.getEpicEntityId()) == p.getKingdomTemplateId()) || (Servers.localServer.EPIC != true))
                      {
                        MissionPerformer mp = MissionPerformed.getMissionPerformer(id);
                        if (mp == null)
                        {
                          mp = MissionPerformed.startNewMission(t.getMissionRequired(), id, 1.0F);
                        }
                        else
                        {
                          MissionPerformed mperf = mp.getMission(t.getMissionRequired());
                          if (mperf == null) {
                            MissionPerformed.startNewMission(t.getMissionRequired(), id, 1.0F);
                          }
                        }
                        p.modifyKarma((int)karmaGained);
                        if (p.isPaying())
                        {
                          p.setScenarioKarma((int)(p.getScenarioKarma() + karmaGained));
                          if (Servers.localServer.EPIC)
                          {
                            WcEpicKarmaCommand wcek = new WcEpicKarmaCommand(WurmId.getNextWCCommandId(), new long[] { p.getWurmId() }, new int[] { p.getScenarioKarma() }, em.getEpicEntityId());
                            wcek.sendToLoginServer();
                          }
                        }
                      }
                    }
                    catch (NoSuchPlayerException localNoSuchPlayerException) {}
                  }
                }
              }
            }
            MissionTriggers.activateTriggers(player, getTemplate().getTemplateId(), 491, getWurmId(), 1);
            activatedTrigger = true;
          }
          catch (NoSuchPlayerException localNoSuchPlayerException8) {}
        }
        for (Map.Entry<Long, Long> entry : this.attackers.entrySet())
        {
          long attackerId = ((Long)entry.getKey()).longValue();
          long attackTime = ((Long)entry.getValue()).longValue();
          if (now - attackTime < 600000L) {
            if (WurmId.getType(attackerId) == 0)
            {
              pvp = true;
              if ((!isPlayer()) || (!Players.getInstance().isOverKilling(attackerId, getWurmId()))) {
                try
                {
                  Player player = Players.getInstance().getPlayer(attackerId);
                  if (!isDuelOrSpar(player))
                  {
                    kskill = player.getFightingSkill().getRealKnowledge();
                    lSkillReceivers.put(player, new Double(kskill));
                    sumskill += kskill;
                  }
                  if ((!isPlayer()) && (!isSpiritGuard()) && (!isKingdomGuard())) {
                    if ((player.isPlayer()) && (!player.isDead())) {
                      player.checkCoinAward(this.attackers.size() * (isDomestic() ? 50 : isBred() ? 20 : 100));
                    }
                  }
                  if ((isChampion()) && (player.isPlayer())) {
                    if ((getKingdomId() != player.getKingdomId()) || (player.isEnemyOnChaos(this)))
                    {
                      player.addTitle(Titles.Title.ChampSlayer);
                      if (player.isChampion())
                      {
                        player.modifyChampionPoints(30);
                        Servers.localServer.createChampTwit(player.getName() + " slays " + getName() + " and gains 30 champion points");
                      }
                    }
                  }
                }
                catch (NoSuchPlayerException localNoSuchPlayerException2) {}
              }
            }
            else
            {
              try
              {
                Creature c = Creatures.getInstance().getCreature(attackerId);
                if (c.isDominated())
                {
                  kskill = c.getFightingSkill().getKnowledge();
                  lSkillReceivers.put(c, new Double(kskill));
                  sumskill += kskill;
                }
                else if ((c.isSpiritGuard()) || (c.isKingdomGuard()))
                {
                  if (!isPlayer()) {
                    wasHelped = true;
                  }
                }
              }
              catch (NoSuchCreatureException localNoSuchCreatureException) {}
            }
          }
        }
      }
      kskill = getFightingSkill().getRealKnowledge();
      getFightingSkill().touch();
      if ((isPlayer()) && (kskill <= 10.0D)) {
        kskill = 0.0D;
      }
      if (!isPlayer())
      {
        kskill = getBaseCombatRating();
        kskill += getBonusCombatRating();
        if (kskill > 2.0D) {
          if ((!isReborn()) && (!isUndead())) {
            kskill *= 5.0D;
          } else if (getTemplate().getTemplateId() == 69) {
            kskill *= 0.20000000298023224D;
          }
        }
      }
      else
      {
        getFightingSkill().setKnowledge(Math.max(1.0D, getFightingSkill().getKnowledge() - 0.25D), false);
      }
      if (kskill > 0.0D)
      {
        if ((!isSpiritGuard()) && (!isKingdomGuard()) && (!isWarGuard()))
        {
          lootReceivers = new HashSet();
          for (Map.Entry<Creature, Double> entry : lSkillReceivers.entrySet())
          {
            Creature p = (Creature)entry.getKey();
            psk = (Double)entry.getValue();
            double pskill = ((Double)psk).doubleValue();
            double percentSkillGained = pskill / sumskill;
            double diff = kskill - pskill;
            
            double lMod = 0.20000000298023224D;
            if (diff > 1.0D) {
              lMod = Math.sqrt(diff);
            } else if (diff < -1.0D) {
              lMod = kskill / pskill;
            }
            if (!isPlayer())
            {
              lMod /= (Servers.localServer.isChallengeServer() ? 2.0D : 7.0D);
              if (pskill > 70.0D)
              {
                double tomax = 100.0D - pskill;
                double modifier = tomax / (Servers.localServer.isChallengeServer() ? 30.0F : 500.0F);
                lMod *= modifier;
              }
              if (wasHelped) {
                lMod *= 0.10000000149011612D;
              }
            }
            else if ((pskill > 50.0D) && (kskill < 20.0D))
            {
              lMod = 0.0D;
            }
            else if (getKingdomId() == p.getKingdomId())
            {
              lMod = 0.0D;
            }
            if (kskill <= 0.0D) {
              lMod = 0.0D;
            }
            double skillGained = percentSkillGained * lMod * 0.25D * ItemBonus.getKillEfficiencyBonus(p);
            if (skillGained > 0.0D)
            {
              p.getFightingSkill().touch();
              if ((p.isPaying()) || (pskill < 20.0D))
              {
                if (pskill + skillGained > 100.0D) {
                  p.getFightingSkill().setKnowledge(pskill + (100.0D - pskill) / 100.0D, false);
                } else {
                  p.getFightingSkill().setKnowledge(pskill + skillGained, false);
                }
                p.getFightingSkill().maybeSetMinimum();
              }
              p.getFightingSkill().checkInitialTitle();
            }
            if (!isPlayer())
            {
              if (p.isPlayer())
              {
                if (p.isUndead())
                {
                  ((Player)p).getSaveFile().undeadKills += 1;
                  ((Player)p).getSaveFile().setUndeadData();
                  p.achievement(335);
                }
                if (isUnique()) {
                  HistoryManager.addHistory(p.getName(), "slayed " + getName());
                }
                int tid = getTemplate().getTemplateId();
                try
                {
                  if (CreatureTemplate.isDragon(tid)) {
                    ((Player)p).addTitle(Titles.Title.DragonSlayer);
                  } else if ((tid == 11) || (tid == 27)) {
                    ((Player)p).addTitle(Titles.Title.TrollSlayer);
                  } else if ((tid == 20) || (tid == 22)) {
                    ((Player)p).addTitle(Titles.Title.GiantSlayer);
                  } else if (isUnique()) {
                    ((Player)p).addTitle(Titles.Title.UniqueSlayer);
                  }
                }
                catch (Exception ex)
                {
                  logger.log(Level.WARNING, getName() + " and " + p.getName() + ":" + ex.getMessage());
                }
                switch (this.status.modtype)
                {
                case 1: 
                  p.achievement(253);
                  break;
                case 2: 
                  p.achievement(254);
                  break;
                case 3: 
                  p.achievement(255);
                  break;
                case 4: 
                  p.achievement(256);
                  break;
                case 5: 
                  p.achievement(257);
                  break;
                case 6: 
                  p.achievement(258);
                  break;
                case 7: 
                  p.achievement(259);
                  break;
                case 8: 
                  p.achievement(260);
                  break;
                case 9: 
                  p.achievement(261);
                  break;
                case 10: 
                  p.achievement(262);
                  break;
                case 11: 
                  p.achievement(263);
                  break;
                case 99: 
                  p.achievement(264);
                  break;
                }
                if (tid == 58) {
                  p.achievement(225);
                } else if (tid == 21) {
                  p.achievement(228);
                } else if (tid == 25) {
                  p.achievement(231);
                } else if (tid == 11) {
                  p.achievement(235);
                } else if (tid == 10) {
                  p.achievement(237);
                } else if (tid == 54) {
                  p.achievement(239);
                } else if (tid == 56) {
                  p.achievement(243);
                } else if (tid == 57) {
                  p.achievement(244);
                } else if (tid == 55) {
                  p.achievement(265);
                } else if (tid == 43) {
                  p.achievement(268);
                } else if ((tid == 42) || (tid == 12)) {
                  p.achievement(269);
                } else if (CreatureTemplate.isFullyGrownDragon(tid)) {
                  p.achievement(270);
                } else if (CreatureTemplate.isDragonHatchling(tid)) {
                  p.achievement(271);
                } else if (tid == 20) {
                  p.achievement(272);
                } else if (tid == 23) {
                  p.achievement(273);
                } else if (tid == 27) {
                  p.achievement(274);
                } else if (tid == 68) {
                  p.achievement(276);
                } else if (tid == 70) {
                  p.achievement(277);
                } else if (tid == 71) {
                  p.achievement(278);
                } else if (tid == 72) {
                  p.achievement(279);
                } else if (tid == 73) {
                  p.achievement(280);
                } else if (tid == 74) {
                  p.achievement(281);
                } else if (tid == 75) {
                  p.achievement(282);
                } else if (tid == 76) {
                  p.achievement(283);
                } else if (tid == 77) {
                  p.achievement(284);
                } else if (tid == 78) {
                  p.achievement(285);
                } else if (tid == 79) {
                  p.achievement(286);
                } else if (tid == 80) {
                  p.achievement(287);
                } else if (tid == 81) {
                  p.achievement(288);
                } else if (tid == 82) {
                  p.achievement(289);
                } else if (tid == 83) {
                  p.achievement(291);
                } else if (tid == 84) {
                  p.achievement(290);
                } else if (tid == 85) {
                  p.achievement(292);
                } else if (tid == 59) {
                  p.achievement(313);
                } else if (tid == 15) {
                  p.achievement(314);
                } else if (tid == 14) {
                  p.achievement(315);
                } else if (tid == 13) {
                  p.achievement(316);
                } else if (tid == 22) {
                  p.achievement(307);
                } else if (tid == 26) {
                  p.achievement(308);
                } else if ((tid == 64) || (tid == 65)) {
                  p.achievement(309);
                } else if ((tid == 49) || (tid == 3) || (tid == 50)) {
                  p.achievement(310);
                } else if (tid == 44) {
                  p.achievement(311);
                } else if (tid == 51) {
                  p.achievement(312);
                } else if (tid == 106) {
                  p.achievement(378);
                } else if (tid == 107) {
                  p.achievement(379);
                } else if (tid == 108) {
                  p.achievement(380);
                } else if (tid == 109) {
                  p.achievement(381);
                }
                if ((isDefendKingdom()) && 
                  (!isFriendlyKingdom(p.getKingdomId()))) {
                  p.achievement(275);
                }
                if (isReborn()) {
                  p.achievement(248);
                }
              }
            }
            else if ((isKing()) && (p.isPlayer())) {
              if (p.getKingdomId() != getKingdomId())
              {
                ((Player)p).addTitle(Titles.Title.Kingslayer);
                HistoryManager.addHistory(p.getName(), "slayed " + getName());
              }
            }
            if ((isPlayer()) && (p.isPlayer()) && (!isUndead()))
            {
              if (p.isUndead())
              {
                ((Player)p).getSaveFile().undeadPlayerKills += 1;
                ((Player)p).getSaveFile().setUndeadData();
                p.achievement(339);
              }
              logger.log(Level.INFO, p.getName() + " killed " + getName() + " as champ=" + p.isChampion() + ". Diff=" + diff + " mod=" + lMod + " skillGained=" + skillGained + " pskill=" + pskill + " kskill=" + kskill);
              if (skillGained > 0.0D)
              {
                p.achievement(8);
                Item weapon = p.getPrimWeapon();
                if (weapon != null)
                {
                  if (weapon.isWeaponBow()) {
                    p.achievement(11);
                  } else if (weapon.isWeaponSword()) {
                    p.achievement(14);
                  } else if (weapon.isWeaponCrush()) {
                    p.achievement(17);
                  } else if (weapon.isWeaponAxe()) {
                    p.achievement(20);
                  } else if (weapon.isWeaponKnife()) {
                    p.achievement(25);
                  }
                  if (weapon.getTemplateId() == 314) {
                    p.achievement(27);
                  } else if (weapon.getTemplateId() == 567) {
                    p.achievement(29);
                  } else if (weapon.getTemplateId() == 20) {
                    p.achievement(30);
                  }
                }
                Item[] bodyItems = p.getBody().getAllItems();
                int clothArmourFound = 0;
                int dragonPiecesFound = 0;
                for (Item i : bodyItems) {
                  if (i.isArmour()) {
                    if (i.isCloth()) {
                      clothArmourFound++;
                    } else if (i.isDragonArmour()) {
                      if ((i.getTemplateId() == 476) || 
                        (i.getTemplateId() == 475)) {
                        dragonPiecesFound++;
                      }
                    }
                  }
                }
                if (clothArmourFound >= 8) {
                  p.achievement(31);
                }
                if (dragonPiecesFound >= 2) {
                  p.achievement(32);
                }
                if (pantLess.contains(Long.valueOf(getWurmId()))) {
                  achievement(33);
                }
              }
            }
            if ((isPlayer()) && (kskill > 40.0D) && (lMod > 0.0D) && 
              (p.isChampion()))
            {
              PlayerKills pk = Players.getInstance().getPlayerKillsFor(p.getWurmId());
              if (System.currentTimeMillis() - pk.getLastKill(getWurmId()) > 86400000L) {
                if (pk.getNumKills(getWurmId()) < 10L)
                {
                  p.modifyChampionPoints(1);
                  Servers.localServer.createChampTwit(p.getName() + " slays " + getName() + " and gains 1 champion point because of difficulty");
                }
              }
            }
          }
        }
        else
        {
          for (Object entry : lSkillReceivers.entrySet())
          {
            Creature p = (Creature)((Map.Entry)entry).getKey();
            if (p.isPlayer()) {
              if (!isFriendlyKingdom(p.getKingdomId())) {
                if (isSpiritGuard()) {
                  p.achievement(267);
                }
              }
            }
          }
        }
      }
      else {
        for (Object entry : lSkillReceivers.entrySet())
        {
          Creature p = (Creature)((Map.Entry)entry).getKey();
          if (p.isPlayer()) {
            if (!isFriendlyKingdom(p.getKingdomId())) {
              if (isSpiritGuard()) {
                p.achievement(267);
              }
            }
          }
        }
      }
    }
    else if (!isUndead())
    {
      if ((this.attackers != null) && (this.attackers.size() > 0))
      {
        ArrayList<Long> possibleTriggerOwners = new ArrayList();
        for (Iterator localIterator1 = this.attackers.keySet().iterator(); localIterator1.hasNext();)
        {
          long l = ((Long)localIterator1.next()).longValue();
          if ((WurmId.getType(l) == 0) && (
            (!isPlayer()) || (!Players.getInstance().isOverKilling(l, getWurmId())))) {
            possibleTriggerOwners.add(Long.valueOf(l));
          }
        }
        if (!possibleTriggerOwners.isEmpty()) {
          try
          {
            Player player = Players.getInstance().getPlayer((Long)possibleTriggerOwners.get(Server.rand.nextInt(possibleTriggerOwners.size())));
            MissionTriggers.activateTriggers(player, getTemplate().getTemplateId(), 491, 
              getWurmId(), 1);
            
            MissionTrigger[] trigs = MissionTriggers.getMissionTriggersWith(getTemplate().getTemplateId(), 491, getWurmId());
            MissionTrigger[] arrayOfMissionTrigger1 = trigs;sumskill = arrayOfMissionTrigger1.length;
            MissionTrigger t;
            EpicMission em;
            float karmaGained;
            for (double d1 = 0; d1 < sumskill; d1++)
            {
              t = arrayOfMissionTrigger1[d1];
              
              em = EpicServerStatus.getEpicMissionForMission(t.getMissionRequired());
              if (em != null)
              {
                EpicMissionEnum missionEnum = EpicMissionEnum.getMissionForType(em.getMissionType());
                if ((missionEnum != null) && (EpicMissionEnum.isMissionKarmaGivenOnKill(missionEnum)))
                {
                  float karmaSplit = missionEnum.getKarmaBonusDiffMult() * em.getDifficulty();
                  karmaGained = karmaSplit / EpicServerStatus.getNumberRequired(em.getDifficulty(), missionEnum);
                  karmaGained = (float)Math.ceil(karmaGained / possibleTriggerOwners.size());
                  for (psk = possibleTriggerOwners.iterator(); ((Iterator)psk).hasNext();)
                  {
                    long id = ((Long)((Iterator)psk).next()).longValue();
                    try
                    {
                      Player p = Players.getInstance().getPlayer(id);
                      if ((Deities.getFavoredKingdom(em.getEpicEntityId()) == p.getKingdomTemplateId()) || (Servers.localServer.EPIC != true))
                      {
                        MissionPerformer mp = MissionPerformed.getMissionPerformer(id);
                        if (mp == null)
                        {
                          mp = MissionPerformed.startNewMission(t.getMissionRequired(), id, 1.0F);
                        }
                        else
                        {
                          MissionPerformed mperf = mp.getMission(t.getMissionRequired());
                          if (mperf == null) {
                            MissionPerformed.startNewMission(t.getMissionRequired(), id, 1.0F);
                          }
                        }
                        p.modifyKarma((int)karmaGained);
                        if (p.isPaying())
                        {
                          p.setScenarioKarma((int)(p.getScenarioKarma() + karmaGained));
                          if (Servers.localServer.EPIC)
                          {
                            WcEpicKarmaCommand wcek = new WcEpicKarmaCommand(WurmId.getNextWCCommandId(), new long[] { p.getWurmId() }, new int[] { p.getScenarioKarma() }, em.getEpicEntityId());
                            wcek.sendToLoginServer();
                          }
                        }
                      }
                    }
                    catch (NoSuchPlayerException localNoSuchPlayerException3) {}
                  }
                }
              }
            }
            activatedTrigger = true;
          }
          catch (NoSuchPlayerException localNoSuchPlayerException6) {}
        }
        for (Map.Entry<Long, Long> entry : this.attackers.entrySet())
        {
          long attackerId = ((Long)entry.getKey()).longValue();
          if (WurmId.getType(attackerId) == 0)
          {
            pvp = true;
            try
            {
              Player player = Players.getInstance().getPlayer(attackerId);
              if (!isFriendlyKingdom(player.getKingdomId())) {
                if (isKingdomGuard()) {
                  player.achievement(266);
                }
              }
            }
            catch (NoSuchPlayerException localNoSuchPlayerException7) {}
          }
        }
      }
    }
    pantLess.remove(Long.valueOf(getWurmId()));
    return (pvp) || ((lSkillReceivers != null) && (lSkillReceivers.size() > 0));
  }
  
  @Nullable
  public Creature getTarget()
  {
    Creature toReturn = null;
    if (this.target != -10L) {
      try
      {
        toReturn = Server.getInstance().getCreature(this.target);
      }
      catch (NoSuchCreatureException nsc)
      {
        setTarget(-10L, true);
      }
      catch (NoSuchPlayerException nsp)
      {
        setTarget(-10L, true);
      }
    }
    return toReturn;
  }
  
  public void setDeathEffects(boolean freeDeath, int dtilex, int dtiley)
  {
    boolean respawn = false;
    removeWoundMod();
    modifyFightSkill(dtilex, dtiley);
    if ((isSpiritGuard()) && (this.citizenVillage != null)) {
      respawn = true;
    } else if ((isKingdomGuard()) || ((isNpc()) && (isRespawn()))) {
      respawn = true;
    }
    if (respawn)
    {
      setDestroyed();
      if (this.name.endsWith("traitor")) {
        try
        {
          setName(getNameWithoutPrefixes());
        }
        catch (Exception ex)
        {
          logger.log(Level.WARNING, getName() + ", " + getWurmId() + ": failed to remove traitor name.");
        }
      }
      try
      {
        this.status.setDead(true);
      }
      catch (IOException ioex)
      {
        logger.log(Level.WARNING, getName() + ", " + getWurmId() + ": Set dead manually.");
      }
      if (isSpiritGuard())
      {
        Village vil = this.citizenVillage;
        if (vil != null)
        {
          vil.deleteGuard(this, false);
          vil.plan.returnGuard(this);
        }
        else
        {
          destroy();
        }
      }
      else if (isKingdomGuard())
      {
        GuardTower tower = Kingdoms.getTower(this);
        if (tower != null)
        {
          try
          {
            tower.returnGuard(this);
          }
          catch (IOException iox)
          {
            logger.log(Level.WARNING, iox.getMessage(), iox);
          }
        }
        else
        {
          logger.log(Level.INFO, getName() + ", " + getWurmId() + " without tower, destroying.");
          destroy();
        }
      }
      else
      {
        this.respawnCounter = 600;
      }
    }
    else
    {
      destroy();
    }
    getStatus().setStunned(0.0F, false);
    trimAttackers(true);
  }
  
  public void respawn()
  {
    if (getVisionArea() == null) {
      try
      {
        if (!isNpc()) {
          if ((this.skills.getSkill(10052).getKnowledge(0.0D) > this.template.getSkills().getSkill(10052).getKnowledge(0.0D) * 2.0D) || 
            (100.0D - this.skills.getSkill(10052).getKnowledge(0.0D) < 30.0D) || 
            
            (this.skills.getSkill(10052).getKnowledge(0.0D) < this.template.getSkills().getSkill(10052).getKnowledge(0.0D) / 2.0D))
          {
            this.skills.delete();
            this.skills.clone(this.template.getSkills().getSkills());
            this.skills.save();
            getStatus().age = 0;
          }
          else if (getStatus().age >= this.template.getMaxAge() - 1)
          {
            getStatus().age = 0;
          }
        }
        this.status.setDead(false);
        this.pollCounter = 0;
        this.lastPolled = 0;
        setDisease((byte)0);
        getStatus().removeWounds();
        getStatus().modifyStamina(65535.0F);
        getStatus().refresh(0.5F, false);
        
        createVisionArea();
      }
      catch (Exception ex)
      {
        logger.log(Level.WARNING, getName() + ":" + ex.getMessage(), ex);
      }
    } else {
      logger.log(Level.WARNING, getName() + " already has a visionarea.", new Exception());
    }
    Server.getInstance().broadCastAction(getNameWithGenus() + " has arrived.", this, 10);
  }
  
  public boolean hasColoredChat()
  {
    return false;
  }
  
  public int getCustomGreenChat()
  {
    return 140;
  }
  
  public int getCustomRedChat()
  {
    return 255;
  }
  
  public int getCustomBlueChat()
  {
    return 0;
  }
  
  public final boolean isFaithful()
  {
    return this.faithful;
  }
  
  public boolean isFighting()
  {
    return this.opponent != null;
  }
  
  public MovementScheme getMovementScheme()
  {
    return this.movementScheme;
  }
  
  public boolean isOnGround()
  {
    return this.movementScheme.onGround;
  }
  
  public void pollStamina()
  {
    this.staminaPollCounter = Math.max(0, --this.staminaPollCounter);
    if (this.staminaPollCounter == 0) {
      if ((!isUndead()) && (WurmId.getType(this.id) == 0))
      {
        int hungMod = 4;
        int thirstMod = (int)(5.0F * ItemBonus.getReplenishBonus(this));
        if ((getSpellEffects() != null) && 
          (getSpellEffects().getSpellEffect((byte)74) != null))
        {
          hungMod = 2;
          thirstMod = 2;
        }
        hungMod = (int)(hungMod * ItemBonus.getReplenishBonus(this));
        boolean reduceHunger = true;
        if ((getDeity() != null) && (getDeity().number == 4)) {
          if (isOnSurface())
          {
            int tile = Server.surfaceMesh.getTile(getTileX(), getTileY());
            if (Tiles.getTile(Tiles.decodeType(tile)).isMycelium()) {
              reduceHunger = false;
            }
          }
        }
        int hunger;
        int hunger;
        if (reduceHunger)
        {
          this.status.decreaseCCFPValues();
          hunger = this.status.modifyHunger((int)(hungMod * (2.0F - this.status.getNutritionlevel())), 1.0F);
        }
        else
        {
          hunger = this.status.modifyHunger(-4, 0.99F);
        }
        int thirst = this.status.modifyThirst(thirstMod);
        
        float hungpercent = 1.0F;
        if (hunger > 45000)
        {
          hungpercent = Math.max(1.0F, 65535 - hunger) / 20535.0F;
          hungpercent *= hungpercent;
        }
        float thirstpercent = Math.max(65535 - thirst, 1.0F) / 65535.0F;
        
        thirstpercent = thirstpercent * thirstpercent * thirstpercent;
        if ((this.status.hasNormalRegen()) && (!isFighting()))
        {
          float toModify = 0.6F;
          if (isStealth()) {
            toModify = 0.06F;
          }
          toModify = toModify * hungpercent * thirstpercent;
          double staminaModifier = this.status.getModifierValuesFor(1);
          if ((getDeity() != null) && (getDeity().staminaBonus) && (getFaith() >= 20.0F) && (getFavor() >= 10.0F)) {
            staminaModifier += 0.25D;
          }
          if (this.hasSpiritStamina) {
            staminaModifier *= 1.1D;
          }
          if (hasSleepBonus()) {
            toModify = Math.max(0.006F, toModify * (float)(1.0D + staminaModifier) * 3.0F);
          } else {
            toModify = Math.max(0.004F, toModify * (float)(1.0D + staminaModifier));
          }
          if (hasSpellEffect((byte)95)) {
            toModify *= 0.5F;
          }
          if (((getPower() == 0) && (getVehicle() == -10L) && (getPositionZ() + getAltOffZ() < -1.45D)) || 
            (isUsingLastGasp())) {
            toModify = 0.0F;
          } else {
            this.status.modifyStamina2(toModify);
          }
        }
        this.status.setNormalRegen(true);
      }
      else
      {
        if (isNeedFood())
        {
          if (Server.rand.nextInt(600) == 0) {
            if ((hasTrait(14)) || (isPregnant())) {
              this.status.modifyHunger(1500, 1.0F);
            } else if (!isCarnivore()) {
              this.status.modifyHunger(700, 1.0F);
            } else {
              this.status.modifyHunger(150, 1.0F);
            }
          }
        }
        else {
          this.status.modifyHunger(-1, 0.5F);
        }
        if ((isRegenerating()) || (isUnique())) {
          if (Server.rand.nextInt(10) == 0) {
            healTick();
          }
        }
        if (Server.rand.nextInt(100) == 0)
        {
          if ((!isFighting()) || (isUnique())) {
            this.status.resetCreatureStamina();
          }
          if ((!isSwimming()) && (!isUnique()) && (!isSubmerged())) {
            if (getPositionZ() + getAltOffZ() <= -1.25D) {
              if ((getVehicle() == -10L) && (this.hitchedTo == null) && (!isRidden()) && (getLeader() == null)) {
                if (!Tiles.isSolidCave(Tiles.decodeType(getCurrentTileNum()))) {
                  addWoundOfType(null, (byte)7, 2, false, 1.0F, false, 4000.0F + Server.rand
                    .nextFloat() * 3000.0F);
                }
              }
            }
          }
        }
        this.status.setNormalRegen(true);
      }
    }
  }
  
  public final boolean checkPregnancy(boolean insta)
  {
    Offspring offspring = Offspring.getOffspring(getWurmId());
    if (offspring != null) {
      if ((!offspring.isChecked()) || (insta)) {
        if ((Server.rand.nextInt(4) == 0) || (insta))
        {
          float creatureRatio = 10.0F;
          if (getCurrentVillage() != null) {
            creatureRatio = getCurrentVillage().getCreatureRatio();
          }
          if (((this.status.hunger > 60000) && (this.status.fat <= 2)) || ((creatureRatio < Village.OPTIMUMCRETRATIO) && 
            (Server.rand.nextInt(Math.max((int)(creatureRatio / 2.0F), 1)) == 0))) {
            if (Server.rand.nextInt(3) == 0)
            {
              Offspring.deleteSettings(getWurmId());
              getCommunicator().sendAlertServerMessage("You suddenly bleed immensely and lose your unborn child due to malnourishment!");
              
              Server.getInstance().broadCastAction(getNameWithGenus() + " bleeds immensely due to miscarriage.", this, 5);
              if (Server.rand.nextInt(5) == 0) {
                die(false, "Miscarriage");
              }
              return false;
            }
          }
          if (offspring.decreaseDaysLeft()) {
            try
            {
              int cid = this.template.getChildTemplateId();
              if (cid <= 0) {
                cid = this.template.getTemplateId();
              }
              CreatureTemplate temp = CreatureTemplateFactory.getInstance().getTemplate(cid);
              String newname = temp.getName();
              byte sex = temp.keepSex ? temp.getSex() : (byte)Server.rand.nextInt(2);
              if (isHorse())
              {
                if (Server.rand.nextBoolean()) {
                  newname = Offspring.generateGenericName();
                } else if (sex == 1) {
                  newname = Offspring.generateFemaleName();
                } else {
                  newname = Offspring.generateMaleName();
                }
                newname = LoginHandler.raiseFirstLetter(newname);
              }
              if (isUnicorn())
              {
                if (Server.rand.nextBoolean()) {
                  newname = Offspring.generateGenericName();
                } else if (sex == 1) {
                  newname = Offspring.generateFemaleUnicornName();
                } else {
                  newname = Offspring.generateMaleUnicornName();
                }
                newname = LoginHandler.raiseFirstLetter(newname);
              }
              boolean zombie = false;
              if (cid == 66)
              {
                zombie = true;
                if (sex == 1) {
                  newname = LoginHandler.raiseFirstLetter("Daughter of " + this.name);
                } else {
                  newname = LoginHandler.raiseFirstLetter("Son of " + this.name);
                }
                if (getKingdomTemplateId() != 3)
                {
                  cid = 25;
                  zombie = false;
                }
              }
              Creature newCreature = doNew(cid, true, getPosX(), getPosY(), Server.rand
                .nextFloat() * 360.0F, getLayer(), newname, sex, (byte)(isAggHuman() ? getKingdomId() : 0), 
                (byte)(Server.rand.nextBoolean() ? getStatus().modtype : 0), zombie, (byte)1);
              
              getCommunicator().sendAlertServerMessage("You give birth to " + newCreature.getName() + "!");
              newCreature.getStatus().setTraitBits(offspring.getTraits());
              newCreature.getStatus().setInheritance(offspring.getTraits(), offspring.getMother(), offspring
                .getFather());
              newCreature.getStatus().saveCreatureName(newname);
              if (zombie)
              {
                if (getPet() != null)
                {
                  getCommunicator().sendNormalServerMessage(getPet().getNameWithGenus() + " stops following you.");
                  if (getPet().getLeader() == this) {
                    getPet().setLeader(null);
                  }
                  getPet().setDominator(-10L);
                  setPet(-10L);
                }
                newCreature.setDominator(getWurmId());
                newCreature.setLoyalty(100.0F);
                setPet(newCreature.getWurmId());
                newCreature.getSkills().delete();
                newCreature.getSkills().clone(this.skills.getSkills());
                Skill[] cskills = newCreature.getSkills().getSkills();
                for (Skill lCskill : cskills) {
                  lCskill.setKnowledge(Math.min(40.0D, lCskill.getKnowledge() * 0.5D), false);
                }
                newCreature.getSkills().save();
              }
              newCreature.setVisible(false);
              newCreature.setVisible(true);
              Server.getInstance().broadCastAction(
                getNameWithGenus() + " gives birth to " + newCreature.getNameWithGenus() + "!", this, 5);
              return true;
            }
            catch (NoSuchCreatureTemplateException nst)
            {
              logger.log(Level.WARNING, 
                getName() + " gives birth to nonexistant template:" + this.template.getChildTemplateId());
            }
            catch (Exception ex)
            {
              logger.log(Level.WARNING, ex.getMessage(), ex);
            }
          }
        }
      }
    }
    return false;
  }
  
  private long getTraits()
  {
    return this.status.traits;
  }
  
  public void mate(Creature father, @Nullable Creature breeder)
  {
    boolean inbred = false;
    if ((father.getFather() == getFather()) || (father.getMother() == getMother()) || (father.getWurmId() == getFather()) || 
      (father.getMother() == getWurmId())) {
      inbred = true;
    }
    new Offspring(getWurmId(), father.getWurmId(), breeder == null ? Traits.calcNewTraits(inbred, getTraits(), father.getTraits()) : Traits.calcNewTraits(breeder.getAnimalHusbandrySkillValue(), inbred, getTraits(), father.getTraits()), (byte)(this.template.daysOfPregnancy + Server.rand.nextInt(5)), false);
    logger.log(Level.INFO, getName() + " gender=" + getSex() + " just got pregnant with " + father.getName() + " gender=" + father.getSex() + ".");
  }
  
  public boolean isBred()
  {
    return hasTrait(63);
  }
  
  static boolean isInbred(Creature maleCreature, Creature femaleCreature)
  {
    return (maleCreature.getFather() == femaleCreature.getFather()) || (maleCreature.getMother() == femaleCreature.getMother()) || 
      (maleCreature.getWurmId() == femaleCreature.getFather()) || 
      (maleCreature.getMother() == femaleCreature.getWurmId());
  }
  
  public boolean isPregnant()
  {
    return getOffspring() != null;
  }
  
  public Offspring getOffspring()
  {
    return Offspring.getOffspring(getWurmId());
  }
  
  private void healTick()
  {
    if (this.status.damage > 0) {
      try
      {
        Wound[] w = getBody().getWounds().getWounds();
        if (w.length > 0) {
          w[0].modifySeverity(65236);
        }
      }
      catch (Exception ex)
      {
        logger.log(Level.WARNING, ex.getMessage(), ex);
      }
    }
  }
  
  public void wearItems()
  {
    Item inventory = getInventory();
    Body body = getBody();
    Set<Item> invitems = inventory.getItems();
    Item[] invarr = (Item[])invitems.toArray(new Item[invitems.size()]);
    for (Item lElement : invarr)
    {
      Item bodyPart;
      byte lslot;
      Item parent;
      if ((lElement.isWeapon()) && ((!isPlayer()) || ((lElement.getTemplateId() != 7) && (!lElement.isWeaponKnife()))))
      {
        try
        {
          byte rslot = isPlayer() ? 38 : 14;
          bodyPart = body.getBodyPart(rslot);
          if (bodyPart.testInsertItem(lElement))
          {
            Item parent = lElement.getParent();
            parent.dropItem(lElement.getWurmId(), false);
            bodyPart.insertItem(lElement);
          }
          else
          {
            lslot = isPlayer() ? 37 : 13;
            bodyPart = body.getBodyPart(lslot);
            if (bodyPart.testInsertItem(lElement))
            {
              parent = lElement.getParent();
              parent.dropItem(lElement.getWurmId(), false);
              bodyPart.insertItem(lElement);
            }
          }
        }
        catch (NoSuchItemException nsi)
        {
          logger.log(Level.WARNING, getName() + " " + nsi.getMessage(), nsi);
        }
        catch (NoSpaceException nsp)
        {
          logger.log(Level.WARNING, getName() + " " + nsp.getMessage(), nsp);
        }
      }
      else if (lElement.isShield())
      {
        try
        {
          Item bodyPart = body.getBodyPart(44);
          bodyPart.insertItem(lElement);
        }
        catch (NoSpaceException e)
        {
          e.printStackTrace();
        }
      }
      else
      {
        byte[] places = lElement.getBodySpaces();
        bodyPart = places;lslot = bodyPart.length;
        for (parent = 0; parent < lslot; parent++)
        {
          byte lPlace = bodyPart[parent];
          try
          {
            Item bodyPart = body.getBodyPart(lPlace);
            if (bodyPart.testInsertItem(lElement))
            {
              Item parent = lElement.getParent();
              parent.dropItem(lElement.getWurmId(), false);
              bodyPart.insertItem(lElement);
              break;
            }
          }
          catch (NoSpaceException nsp)
          {
            if ((!Servers.localServer.testServer) && (lPlace != 28)) {
              logger.log(Level.WARNING, getName() + ":" + nsp.getMessage(), nsp);
            }
          }
          catch (NoSuchItemException nsi)
          {
            logger.log(Level.WARNING, getName() + ":" + nsi.getMessage(), nsi);
          }
        }
      }
    }
  }
  
  public float getStaminaMod()
  {
    int hunger = this.status.getHunger();
    int thirst = this.status.getThirst();
    
    float newhungpercent = 1.0F;
    if (hunger > 45000)
    {
      newhungpercent = Math.max(1.0F, 65535 - hunger) / 20535.0F;
      newhungpercent *= newhungpercent;
    }
    float thirstpercent = Math.max(65535 - thirst, 1.0F) / 65535.0F;
    
    thirstpercent = thirstpercent * thirstpercent * thirstpercent;
    return 1.0F - newhungpercent * thirstpercent;
  }
  
  public Skills getSkills()
  {
    return this.skills;
  }
  
  public double getSoulStrengthVal()
  {
    return getSoulStrength().getKnowledge(0.0D);
  }
  
  public Skill getClimbingSkill()
  {
    try
    {
      return this.skills.getSkill(10073);
    }
    catch (NoSuchSkillException nss) {}
    return this.skills.learn(10073, 1.0F);
  }
  
  public double getLockPickingSkillVal()
  {
    try
    {
      return this.skills.getSkill(10076).getKnowledge(0.0D);
    }
    catch (NoSuchSkillException nss) {}
    return 1.0D;
  }
  
  public double getLockSmithingSkill()
  {
    try
    {
      return this.skills.getSkill(10034).getKnowledge(0.0D);
    }
    catch (NoSuchSkillException nss) {}
    return 1.0D;
  }
  
  public double getStrengthSkill()
  {
    try
    {
      if (isPlayer()) {
        return this.skills.getSkill(102).getKnowledge(0.0D);
      }
      return this.skills.getSkill(102).getKnowledge();
    }
    catch (NoSuchSkillException nss) {}
    return 1.0D;
  }
  
  public Skill getStealSkill()
  {
    try
    {
      return this.skills.getSkill(10075);
    }
    catch (NoSuchSkillException nss) {}
    return this.skills.learn(10075, 1.0F);
  }
  
  public Skill getStaminaSkill()
  {
    try
    {
      return this.skills.getSkill(103);
    }
    catch (NoSuchSkillException nss) {}
    return this.skills.learn(103, 1.0F);
  }
  
  public final double getAnimalHusbandrySkillValue()
  {
    try
    {
      return this.skills.getSkill(10085).getKnowledge(0.0D);
    }
    catch (NoSuchSkillException nss) {}
    return this.skills.learn(10085, 1.0F).getKnowledge(0.0D);
  }
  
  public double getBodyControl()
  {
    try
    {
      return this.skills.getSkill(104).getKnowledge(0.0D);
    }
    catch (NoSuchSkillException nss) {}
    return this.skills.learn(104, 1.0F).getKnowledge(0.0D);
  }
  
  public Skill getBodyControlSkill()
  {
    try
    {
      return this.skills.getSkill(104);
    }
    catch (NoSuchSkillException nss) {}
    return this.skills.learn(104, 1.0F);
  }
  
  public Skill getFightingSkill()
  {
    if (!isPlayer()) {
      return getWeaponLessFightingSkill();
    }
    try
    {
      return this.skills.getSkill(1023);
    }
    catch (NoSuchSkillException nss) {}
    return this.skills.learn(1023, 1.0F);
  }
  
  public Skill getWeaponLessFightingSkill()
  {
    try
    {
      return this.skills.getSkill(10052);
    }
    catch (NoSuchSkillException nss)
    {
      try
      {
        return this.skills.learn(10052, 
          (float)this.template.getSkills().getSkill(10052).getKnowledge(0.0D));
      }
      catch (NoSuchSkillException nss2)
      {
        logger.log(Level.WARNING, "Template for " + getName() + " has no weaponless skill?");
        return this.skills.learn(10052, 20.0F);
      }
      catch (Exception ex)
      {
        logger.log(Level.WARNING, ex.getMessage() + " template for " + getName() + " has skills?");
      }
    }
    return this.skills.learn(10052, 20.0F);
  }
  
  public byte getAttitude(Creature aTarget)
  {
    if (this.opponent == aTarget) {
      return 2;
    }
    if ((aTarget.isNpc()) && (isNpc()) && (aTarget.getKingdomId() == getKingdomId())) {
      return 1;
    }
    if (isDominated())
    {
      if (getDominator() != null)
      {
        if (getDominator() == aTarget) {
          return 1;
        }
        if (getDominator() == aTarget.getDominator()) {
          return 1;
        }
        return aTarget.getAttitude(getDominator());
      }
      if (getLoyalty() > 0.0F) {
        if (((aTarget.getReputation() >= 0) || (aTarget.getKingdomTemplateId() == 3)) && 
          (isFriendlyKingdom(aTarget.getKingdomId()))) {
          return 0;
        }
      }
    }
    if (aTarget.isDominated())
    {
      Creature lDominator = aTarget.getDominator();
      if (lDominator != null)
      {
        if (lDominator == this) {
          return 1;
        }
        if ((aTarget.isHorse()) && (aTarget.isRidden()))
        {
          if ((isHungry()) && (isCarnivore()))
          {
            if (Server.rand.nextInt(5) == 0) {
              for (Long riderLong : aTarget.getRiders()) {
                try
                {
                  Creature rider = Server.getInstance().getCreature(riderLong.longValue());
                  if (getAttitude(rider) == 2) {
                    return 2;
                  }
                }
                catch (Exception ex)
                {
                  logger.log(Level.WARNING, ex.getMessage());
                }
              }
            }
            return 0;
          }
        }
        else {
          return getAttitude(lDominator);
        }
      }
      if ((isFriendlyKingdom(aTarget.getKingdomId())) && (aTarget.getLoyalty() > 0.0F)) {
        return 0;
      }
    }
    if (getPet() != null) {
      if (aTarget == getPet()) {
        return 1;
      }
    }
    if (isInvulnerable()) {
      return 0;
    }
    if (aTarget.isInvulnerable()) {
      return 0;
    }
    if ((!isPlayer()) && (aTarget.getCultist() != null))
    {
      if (aTarget.getCultist().hasFearEffect()) {
        return 0;
      }
      if (aTarget.getCultist().hasLoveEffect()) {
        return 1;
      }
    }
    if ((isReborn()) && (!aTarget.equals(getTarget())) && (!aTarget.equals(this.opponent)) && 
      (aTarget.getKingdomId() == getKingdomId())) {
      return 0;
    }
    if ((onlyAttacksPlayers()) && 
      (!aTarget.isPlayer())) {
      return 0;
    }
    if ((!isPlayer()) && 
      (aTarget.onlyAttacksPlayers())) {
      return 0;
    }
    if (Servers.isThisAChaosServer()) {
      if ((getCitizenVillage() != null) && (getCitizenVillage().isEnemy(aTarget))) {
        return 2;
      }
    }
    boolean atta;
    Village lVill;
    if (isAggHuman())
    {
      if ((aTarget instanceof Player))
      {
        atta = true;
        if (isAnimal()) {
          if (aTarget.getDeity() != null) {
            if ((aTarget.getDeity().befriendCreature) && 
              (aTarget.getFaith() > 60.0F) && (aTarget.getFavor() >= 30.0F)) {
              atta = false;
            }
          }
        }
        if (getLoyalty() > 0.0F) {
          if (((aTarget.getReputation() >= 0) || (aTarget.getKingdomTemplateId() == 3)) && 
            (isFriendlyKingdom(aTarget.getKingdomId()))) {
            atta = false;
          }
        }
        if (atta) {
          return 2;
        }
      }
      else if (((aTarget.isSpiritGuard()) && (aTarget.getCitizenVillage() == null)) || (aTarget.isKingdomGuard()))
      {
        if ((getLoyalty() <= 0.0F) && (!isUnique()) && ((!isHorse()) || (!isRidden()))) {
          return 2;
        }
      }
      else if (aTarget.isRidden())
      {
        if ((isHungry()) && (isCarnivore())) {
          if (Server.rand.nextInt(5) == 0) {
            for (Long riderLong : aTarget.getRiders()) {
              try
              {
                Creature rider = Server.getInstance().getCreature(riderLong.longValue());
                if (getAttitude(rider) == 2) {
                  return 2;
                }
              }
              catch (Exception ex)
              {
                logger.log(Level.WARNING, ex.getMessage());
              }
            }
          }
        }
        return 0;
      }
    }
    else
    {
      if ((aTarget.getKingdomId() != 0) && (!isFriendlyKingdom(aTarget.getKingdomId())) && (
        (isDefendKingdom()) || ((isAggWhitie()) && (aTarget.getKingdomTemplateId() != 3)))) {
        return 2;
      }
      if (isSpiritGuard())
      {
        if (this.citizenVillage != null)
        {
          if ((aTarget instanceof Player))
          {
            if (this.citizenVillage.isEnemy(aTarget.citizenVillage)) {
              return 2;
            }
            if (this.citizenVillage.getReputation(aTarget) <= -30) {
              return 2;
            }
            if (this.citizenVillage.isEnemy(aTarget)) {
              return 2;
            }
            if (this.citizenVillage.isAlly(aTarget)) {
              return 1;
            }
            if (this.citizenVillage.isCitizen(aTarget)) {
              return 1;
            }
            if (!isFriendlyKingdom(aTarget.getKingdomId())) {
              return 2;
            }
            return 0;
          }
          if (aTarget.getKingdomId() != 0)
          {
            if (!isFriendlyKingdom(getKingdomId())) {
              return 2;
            }
            return 0;
          }
          if (aTarget.isRidden())
          {
            for (Long riderLong : aTarget.getRiders()) {
              try
              {
                Creature rider = Server.getInstance().getCreature(riderLong.longValue());
                if (!isFriendlyKingdom(rider.getKingdomId())) {
                  return 2;
                }
              }
              catch (Exception ex)
              {
                logger.log(Level.WARNING, ex.getMessage());
              }
            }
            return 0;
          }
        }
      }
      else if (isKingdomGuard())
      {
        if (aTarget.getKingdomId() != 0)
        {
          if (!isFriendlyKingdom(aTarget.getKingdomId())) {
            return 2;
          }
          if ((aTarget.getKingdomTemplateId() != 3) && (aTarget.getReputation() <= -100)) {
            return 2;
          }
          if (aTarget.isPlayer())
          {
            lVill = Villages.getVillageWithPerimeterAt(getTileX(), getTileY(), true);
            if ((lVill != null) && (lVill.kingdom == getKingdomId())) {
              if (lVill.isEnemy(aTarget)) {
                return 2;
              }
            }
          }
        }
        else if ((aTarget.isAggHuman()) && (!aTarget.isUnique()))
        {
          if (aTarget.getCurrentKingdom() == getKingdomId()) {
            if ((aTarget.getLoyalty() <= 0.0F) && (!aTarget.isRidden())) {
              return 2;
            }
          }
        }
        if (aTarget.isRidden()) {
          for (Long riderLong : aTarget.getRiders()) {
            try
            {
              Creature rider = Server.getInstance().getCreature(riderLong.longValue());
              if (getAttitude(rider) == 2) {
                return 2;
              }
            }
            catch (Exception ex)
            {
              logger.log(Level.WARNING, ex.getMessage());
            }
          }
        }
      }
    }
    if ((isCarnivore()) && (aTarget.isPrey()) && (Server.rand.nextInt(10) == 0) && (canEat())) {
      if ((aTarget.getCurrentVillage() == null) && (aTarget.getHitched() == null)) {
        return 2;
      }
    }
    return 0;
  }
  
  public final byte getCurrentKingdom()
  {
    return Zones.getKingdom(getTileX(), getTileY());
  }
  
  public boolean isFriendlyKingdom(byte targetKingdom)
  {
    if ((getKingdomId() == 0) || (targetKingdom == 0)) {
      return false;
    }
    if (getKingdomId() == targetKingdom) {
      return true;
    }
    Kingdom myKingd = Kingdoms.getKingdom(getKingdomId());
    if (myKingd != null) {
      return myKingd.isAllied(targetKingdom);
    }
    return false;
  }
  
  public Possessions getPossessions()
  {
    return this.possessions;
  }
  
  public Item getInventory()
  {
    if (this.possessions != null) {
      return this.possessions.getInventory();
    }
    logger.warning("Posessions was null for " + this.id);
    return null;
  }
  
  public static final Item createItem(int templateId, float qualityLevel)
    throws Exception
  {
    Item item = ItemFactory.createItem(templateId, qualityLevel, (byte)0, (byte)0, null);
    return item;
  }
  
  public void save()
    throws IOException
  {
    this.possessions.save();
    this.status.save();
    this.skills.save();
  }
  
  public void savePosition(int zoneid)
    throws IOException
  {
    this.status.savePosition(this.id, false, zoneid, false);
  }
  
  public boolean isGuest()
  {
    return this.guest;
  }
  
  public void setGuest(boolean g)
  {
    this.guest = g;
  }
  
  public CreatureTemplate getTemplate()
  {
    return this.template;
  }
  
  public void refreshAttitudes()
  {
    if (this.visionArea != null) {
      this.visionArea.refreshAttitudes();
    }
    if (this.currentTile != null) {
      this.currentTile.checkChangedAttitude(this);
    }
  }
  
  public static Creature doNew(int templateid, byte ctype, float aPosX, float aPosY, float aRot, int layer, String name, byte gender)
    throws Exception
  {
    return doNew(templateid, true, aPosX, aPosY, aRot, layer, name, gender, (byte)0, ctype, false);
  }
  
  public static Creature doNew(int templateid, float aPosX, float aPosY, float aRot, int layer, String name, byte gender)
    throws Exception
  {
    return doNew(templateid, aPosX, aPosY, aRot, layer, name, gender, (byte)0);
  }
  
  public static Creature doNew(int templateid, float aPosX, float aPosY, float aRot, int layer, String name, byte gender, byte kingdom)
    throws Exception
  {
    return doNew(templateid, true, aPosX, aPosY, aRot, layer, name, gender, kingdom, (byte)0, false);
  }
  
  public static Creature doNew(int templateid, boolean createPossessions, float aPosX, float aPosY, float aRot, int layer, String name, byte gender, byte kingdom, byte ctype, boolean reborn)
    throws Exception
  {
    return doNew(templateid, createPossessions, aPosX, aPosY, aRot, layer, name, gender, kingdom, ctype, reborn, (byte)0);
  }
  
  public static Creature doNew(int templateid, boolean createPossessions, float aPosX, float aPosY, float aRot, int layer, String name, byte gender, byte kingdom, byte ctype, boolean reborn, byte age)
    throws Exception
  {
    return doNew(templateid, createPossessions, aPosX, aPosY, aRot, layer, name, gender, kingdom, ctype, reborn, age, 0);
  }
  
  public static Creature doNew(int templateid, boolean createPossessions, float aPosX, float aPosY, float aRot, int layer, String name, byte gender, byte kingdom, byte ctype, boolean reborn, byte age, int floorLevel)
    throws Exception
  {
    Creature toReturn = (!reborn) && ((templateid == 1) || (templateid == 113)) ? new Npc(CreatureTemplateFactory.getInstance().getTemplate(templateid)) : new Creature(CreatureTemplateFactory.getInstance().getTemplate(templateid));
    
    long wid = WurmId.getNextCreatureId();
    try
    {
      while (Creatures.getInstance().getCreature(wid) != null) {
        wid = WurmId.getNextCreatureId();
      }
    }
    catch (Exception localException) {}
    toReturn.setWurmId(wid, aPosX, aPosY, normalizeAngle(aRot), layer);
    if (name.length() > 0) {
      toReturn.setName(name);
    }
    if (toReturn.getTemplate().isRoyalAspiration()) {
      if (toReturn.getTemplate().getTemplateId() == 62) {
        kingdom = 1;
      } else if (toReturn.getTemplate().getTemplateId() == 63) {
        kingdom = 3;
      }
    }
    if (reborn) {
      toReturn.getStatus().reborn = true;
    }
    if (floorLevel > 0) {
      toReturn.pushToFloorLevel(floorLevel);
    } else {
      toReturn.setPositionZ(toReturn.calculatePosZ());
    }
    if (age <= 0) {
      toReturn.getStatus().age = ((int)(1.0F + Server.rand.nextFloat() * Math.min(48, toReturn.getTemplate().getMaxAge())));
    } else {
      toReturn.getStatus().age = age;
    }
    if ((toReturn.isGhost()) || (toReturn.isKingdomGuard()) || (reborn)) {
      toReturn.getStatus().age = 12;
    }
    if (ctype != 0) {
      toReturn.getStatus().modtype = ctype;
    }
    if (toReturn.isUnique()) {
      toReturn.getStatus().age = (12 + (int)(Server.rand.nextFloat() * (toReturn.getTemplate().getMaxAge() - 12)));
    }
    toReturn.getStatus().kingdom = kingdom;
    if ((Kingdoms.getKingdom(kingdom) != null) && (Kingdoms.getKingdom(kingdom).getTemplate() == 3))
    {
      toReturn.setAlignment(-50.0F);
      toReturn.setDeity(Deities.getDeity(4));
      toReturn.setFaith(1.0F);
    }
    toReturn.setSex(gender);
    
    Creatures.getInstance().addCreature(toReturn, false, false);
    
    toReturn.loadSkills();
    
    toReturn.createPossessions();
    
    toReturn.getBody().createBodyParts();
    if (!toReturn.isAnimal()) {
      if (createPossessions)
      {
        createBasicItems(toReturn);
        toReturn.wearItems();
      }
    }
    if ((toReturn.isHorse()) || (toReturn.getTemplate().isBlackOrWhite)) {
      if (Server.rand.nextInt(10) == 0) {
        setRandomColor(toReturn);
      }
    }
    Creatures.getInstance().sendToWorld(toReturn);
    toReturn.createVisionArea();
    
    toReturn.save();
    if (reborn) {
      toReturn.getStatus().setReborn(true);
    }
    if (ctype != 0) {
      toReturn.getStatus().setType(ctype);
    }
    toReturn.getStatus().setKingdom(kingdom);
    if (kingdom == 3)
    {
      toReturn.setAlignment(-50.0F);
      toReturn.setDeity(Deities.getDeity(4));
      toReturn.setFaith(1.0F);
    }
    Server.getInstance().broadCastAction(toReturn.getNameWithGenus() + " has arrived.", toReturn, 10);
    if (toReturn.isUnique())
    {
      Server.getInstance().broadCastSafe("Rumours of " + toReturn.getName() + " are starting to spread.");
      Servers.localServer.spawnedUnique();
      logger.log(Level.INFO, "Unique " + toReturn.getName() + " spawned @ " + toReturn.getTileX() + ", " + toReturn.getTileY() + ", wurmID = " + toReturn.getWurmId());
    }
    if (toReturn.getTemplate().getCreatureAI() != null) {
      toReturn.getTemplate().getCreatureAI().creatureCreated(toReturn);
    }
    return toReturn;
  }
  
  public float getSecondsPlayed()
  {
    return 1.0F;
  }
  
  public static void createBasicItems(Creature toReturn)
  {
    try
    {
      Item inventory = toReturn.getInventory();
      if (toReturn.getTemplate().getTemplateId() == 11)
      {
        Item club = createItem(314, 45.0F);
        inventory.insertItem(club);
        
        Item paper = getRareRecipe("Da Wife", 1250, 1251, 1252, 1253);
        if (paper != null) {
          inventory.insertItem(paper);
        }
      }
      else if (toReturn.getTemplate().getTemplateId() == 23)
      {
        Item paper = getRareRecipe("Granny Gobin", 1255, 1256, 1257, 1258);
        if (paper != null) {
          inventory.insertItem(paper);
        }
      }
      else if (toReturn.getTemplate().getTemplateId() == 75)
      {
        Item swo = createItem(81, 85.0F);
        ItemSpellEffects effs = new ItemSpellEffects(swo.getWurmId());
        effs.addSpellEffect(new SpellEffect(swo.getWurmId(), (byte)33, 90.0F, 20000000));
        inventory.insertItem(swo);
        Item helmOne = createItem(285, 75.0F);
        Item helmTwo = createItem(285, 75.0F);
        helmOne.setMaterial((byte)9);
        helmTwo.setMaterial((byte)9);
        inventory.insertItem(helmOne);
        inventory.insertItem(helmTwo);
      }
      else if (toReturn.isUnique())
      {
        if (toReturn.getTemplate().getTemplateId() == 26)
        {
          Item sword = createItem(80, 45.0F);
          inventory.insertItem(sword);
          Item shield = createItem(4, 45.0F);
          inventory.insertItem(shield);
          Item goboHat = createItem(1014, 55.0F);
          inventory.insertItem(goboHat);
        }
        else if (toReturn.getTemplate().getTemplateId() == 27)
        {
          Item club = createItem(314, 65.0F);
          inventory.insertItem(club);
          Item trollCrown = createItem(1015, 70.0F);
          inventory.insertItem(trollCrown);
        }
        else if ((toReturn.getTemplate().getTemplateId() == 22) || 
          (toReturn.getTemplate().getTemplateId() == 20))
        {
          Item club = createItem(314, 65.0F);
          inventory.insertItem(club);
        }
        else if (!CreatureTemplate.isDragonHatchling(toReturn.getTemplate().getTemplateId()))
        {
          if (!CreatureTemplate.isFullyGrownDragon(toReturn.getTemplate().getTemplateId())) {}
        }
      }
      return;
    }
    catch (Exception ex)
    {
      logger.log(Level.INFO, "Failed to create items for creature.", ex);
    }
  }
  
  public Item getPrimWeapon()
  {
    return getPrimWeapon(false);
  }
  
  public Item getPrimWeapon(boolean onlyBodyPart)
  {
    Item primWeapon = null;
    if (isAnimal()) {
      try
      {
        if (getHandDamage() > 0.0F) {
          return getEquippedWeapon((byte)14);
        }
        if (getKickDamage() > 0.0F) {
          return getEquippedWeapon((byte)34);
        }
        if (getHeadButtDamage() > 0.0F) {
          return getEquippedWeapon((byte)1);
        }
        if (getBiteDamage() > 0.0F) {
          return getEquippedWeapon((byte)29);
        }
        if (getBreathDamage() > 0.0F) {
          return getEquippedWeapon((byte)2);
        }
      }
      catch (NoSpaceException nsp)
      {
        logger.log(Level.WARNING, getName() + nsp.getMessage(), nsp);
      }
    }
    try
    {
      byte slot = isPlayer() ? 38 : 14;
      primWeapon = getEquippedWeapon(slot, true);
    }
    catch (NoSpaceException nsp)
    {
      logger.log(Level.WARNING, nsp.getMessage(), nsp);
    }
    if (primWeapon == null) {
      try
      {
        byte slot = isPlayer() ? 37 : 13;
        primWeapon = getEquippedWeapon(slot, true);
        if (!primWeapon.isTwoHanded()) {
          primWeapon = null;
        } else if (getShield() != null) {
          primWeapon = null;
        }
      }
      catch (NoSpaceException nsp)
      {
        logger.log(Level.WARNING, nsp.getMessage(), nsp);
      }
    }
    return primWeapon;
  }
  
  public Item getLefthandWeapon()
  {
    try
    {
      byte slot = isPlayer() ? 37 : 13;
      Set<Item> wornItems = this.status.getBody().getBodyPart(slot).getItems();
      if (wornItems != null) {
        for (Item item : wornItems) {
          if ((!item.isArmour()) && (!item.isBodyPartAttached())) {
            if (item.getDamagePercent() > 0) {
              return item;
            }
          }
        }
      }
    }
    catch (NoSpaceException nsp)
    {
      logger.log(Level.WARNING, nsp.getMessage(), nsp);
    }
    return null;
  }
  
  public Item getLefthandItem()
  {
    try
    {
      byte slot = isPlayer() ? 37 : 13;
      Set<Item> wornItems = this.status.getBody().getBodyPart(slot).getItems();
      if (wornItems != null) {
        for (Item item : wornItems) {
          if ((!item.isArmour()) && (!item.isBodyPartAttached())) {
            return item;
          }
        }
      }
    }
    catch (NoSpaceException nsp)
    {
      logger.log(Level.WARNING, nsp.getMessage(), nsp);
    }
    return null;
  }
  
  public Item getRighthandItem()
  {
    try
    {
      byte slot = isPlayer() ? 38 : 14;
      Set<Item> wornItems = this.status.getBody().getBodyPart(slot).getItems();
      if (wornItems != null) {
        for (Item item : wornItems) {
          if ((!item.isArmour()) && (!item.isBodyPartAttached())) {
            return item;
          }
        }
      }
    }
    catch (NoSpaceException nsp)
    {
      logger.log(Level.WARNING, nsp.getMessage(), nsp);
    }
    return null;
  }
  
  public Item getRighthandWeapon()
  {
    try
    {
      byte slot = isPlayer() ? 38 : 14;
      Set<Item> wornItems = this.status.getBody().getBodyPart(slot).getItems();
      if (wornItems != null) {
        for (Item item : wornItems) {
          if ((!item.isArmour()) && (!item.isBodyPartAttached())) {
            if (item.getDamagePercent() > 0) {
              return item;
            }
          }
        }
      }
    }
    catch (NoSpaceException nsp)
    {
      logger.log(Level.WARNING, nsp.getMessage(), nsp);
    }
    return null;
  }
  
  public Item getWornBelt()
  {
    try
    {
      byte slot = isPlayer() ? 43 : 34;
      Set<Item> wornItems = this.status.getBody().getBodyPart(slot).getItems();
      if (wornItems != null) {
        for (Item item : wornItems) {
          if (item.isBelt()) {
            return item;
          }
        }
      }
    }
    catch (NoSpaceException nsp)
    {
      logger.log(Level.WARNING, nsp.getMessage(), nsp);
    }
    return null;
  }
  
  public Item[] getSecondaryWeapons()
  {
    Set<Item> toReturn = new HashSet();
    if (getBiteDamage() > 0.0F) {
      try
      {
        toReturn.add(getEquippedWeapon((byte)29));
      }
      catch (NoSpaceException nsp)
      {
        logger.log(Level.WARNING, getName() + " no face.");
      }
    }
    if (getHeadButtDamage() > 0.0F) {
      try
      {
        toReturn.add(getEquippedWeapon((byte)1));
      }
      catch (NoSpaceException nsp)
      {
        logger.log(Level.WARNING, getName() + " no head.");
      }
    }
    if (getKickDamage() > 0.0F) {
      try
      {
        if ((isAnimal()) || (isMonster())) {
          toReturn.add(getEquippedWeapon((byte)34));
        } else {
          try
          {
            getArmour((byte)34);
          }
          catch (NoArmourException nsp)
          {
            if (getCarryingCapacityLeft() > 40000) {
              toReturn.add(getEquippedWeapon((byte)34));
            }
          }
        }
      }
      catch (NoSpaceException nsp)
      {
        logger.log(Level.WARNING, getName() + " no legs.");
      }
    }
    if (getBreathDamage() > 0.0F) {
      try
      {
        toReturn.add(getEquippedWeapon((byte)2));
      }
      catch (NoSpaceException nsp)
      {
        logger.log(Level.WARNING, getName() + " no torso.");
      }
    }
    if (getShield() == null) {
      try
      {
        if ((getPrimWeapon() == null) || (!getPrimWeapon().isTwoHanded())) {
          if (isPlayer()) {
            toReturn.add(getEquippedWeapon((byte)37, false));
          } else {
            toReturn.add(getEquippedWeapon((byte)13, false));
          }
        }
      }
      catch (NoSpaceException nsp)
      {
        logger.log(Level.WARNING, getName() + " - no arm. This may be possible later but not now." + nsp.getMessage(), nsp);
      }
    }
    if (!toReturn.isEmpty()) {
      return (Item[])toReturn.toArray(new Item[toReturn.size()]);
    }
    return emptyItems;
  }
  
  public Item getShield()
  {
    Item shield = null;
    try
    {
      byte slot = isPlayer() ? 44 : 3;
      shield = getEquippedItem(slot);
      if (!shield.isShield()) {
        shield = null;
      }
    }
    catch (NoSuchItemException localNoSuchItemException) {}catch (NoSpaceException nsp)
    {
      logger.log(Level.WARNING, nsp.getMessage(), nsp);
    }
    return shield;
  }
  
  public float getSpeed()
  {
    if (getCreatureAIData() != null) {
      return getCreatureAIData().getSpeed();
    }
    return this.template.getSpeed();
  }
  
  public int calculateSize()
  {
    int centimetersHigh = getBody().getCentimetersHigh();
    int centimetersLong = getBody().getCentimetersLong();
    int centimetersWide = getBody().getCentimetersWide();
    int size = 3;
    if ((centimetersHigh > 400) || (centimetersLong > 400) || (centimetersWide > 400)) {
      size = 5;
    } else if ((centimetersHigh > 200) || (centimetersLong > 200) || (centimetersWide > 200)) {
      size = 4;
    } else if ((centimetersHigh > 100) || (centimetersLong > 100) || (centimetersWide > 100)) {
      size = 3;
    } else if ((centimetersHigh > 50) || (centimetersLong > 50) || (centimetersWide > 50)) {
      size = 2;
    } else {
      size = 1;
    }
    return size;
  }
  
  public void say(String message)
  {
    if (this.currentTile != null) {
      this.currentTile.broadCastMessage(new Message(this, (byte)0, ":Local", "<" + getName() + "> " + message));
    }
  }
  
  public void say(String message, boolean emote)
  {
    if (this.currentTile != null) {
      if (!emote) {
        say(message);
      } else {
        this.currentTile.broadCastMessage(new Message(this, (byte)6, ":Local", getName() + " " + message));
      }
    }
  }
  
  public void sendEquipment(Creature receiver)
  {
    if (receiver.addItemWatched(getBody().getBodyItem()))
    {
      receiver.getCommunicator().sendOpenInventoryWindow(getBody().getBodyItem().getWurmId(), getName());
      getBody().getBodyItem().addWatcher(getBody().getBodyItem().getWurmId(), receiver);
      
      Wounds w = getBody().getWounds();
      if (w != null)
      {
        Wound[] wounds = w.getWounds();
        for (Wound lWound : wounds) {
          try
          {
            Item bodypart = getBody().getBodyPartForWound(lWound);
            receiver.getCommunicator().sendAddWound(lWound, bodypart);
          }
          catch (NoSpaceException nsp)
          {
            logger.log(Level.INFO, nsp.getMessage(), nsp);
          }
        }
      }
    }
    if (receiver.getPower() >= 2) {
      if (receiver.addItemWatched(getInventory()))
      {
        receiver.getCommunicator().sendOpenInventoryWindow(getInventory().getWurmId(), getName() + " inventory");
        getInventory().addWatcher(getInventory().getWurmId(), receiver);
      }
    }
  }
  
  public final void startUsingPath()
  {
    if (this.setTargetNOID)
    {
      setTarget(-10L, true);
      this.setTargetNOID = false;
    }
    if (this.creatureToBlinkTo != null)
    {
      if (!this.creatureToBlinkTo.isDead())
      {
        logger.log(Level.INFO, getName() + " at " + getTileX() + "," + getTileY() + " " + 
          getLayer() + "  blingking to " + this.creatureToBlinkTo.getTileX() + "," + this.creatureToBlinkTo
          .getTileY() + "," + this.creatureToBlinkTo.getLayer());
        blinkTo(this.creatureToBlinkTo.getTileX(), this.creatureToBlinkTo.getTileY(), this.creatureToBlinkTo
          .getLayer(), this.creatureToBlinkTo.getFloorLevel());
        this.status.setPath(null);
        this.receivedPath = false;
        setPathing(false, true);
      }
      this.creatureToBlinkTo = null;
    }
    if (this.receivedPath)
    {
      this.receivedPath = false;
      setPathing(false, false);
      if (this.status.getPath() != null)
      {
        sendToLoggers("received path to " + this.status.getPath().getTargetTile().getTileX() + "," + this.status
          .getPath().getTargetTile().getTileY(), (byte)2);
        if (this.status.getPath().getSize() >= 4) {
          this.pathRecalcLength = (this.status.getPath().getSize() / 2);
        } else {
          this.pathRecalcLength = 0;
        }
        this.status.setMoving(true);
        if ((moveAlongPath()) || (isTeleporting()))
        {
          this.status.setPath(null);
          this.status.setMoving(false);
        }
      }
    }
  }
  
  protected void hunt()
  {
    if (!isPathing())
    {
      Path path = null;
      boolean findPath = false;
      if (Server.rand.nextInt(2 * Math.max(1, this.template.getAggressivity())) == 0)
      {
        this.setTargetNOID = true;
        return;
      }
      if ((isAnimal()) || (isDominated()))
      {
        path = this.status.getPath();
        if (path == null) {
          findPath = true;
        }
      }
      else
      {
        findPath = true;
      }
      if (findPath) {
        startPathing(10);
      } else if (path == null) {
        startPathing(100);
      }
    }
  }
  
  public void setAlertSeconds(int seconds)
  {
    this.guardSecondsLeft = ((byte)seconds);
  }
  
  public byte getAlertSeconds()
  {
    return this.guardSecondsLeft;
  }
  
  public void callGuards()
  {
    if (this.guardSecondsLeft > 0)
    {
      getCommunicator().sendNormalServerMessage("You already called the guards. Wait a few seconds.", (byte)3);
      return;
    }
    this.guardSecondsLeft = 10;
    if (getVisionArea() != null) {
      if (isOnSurface())
      {
        if (getVisionArea().getSurface() != null) {
          getVisionArea().getSurface().callGuards();
        }
      }
      else if (getVisionArea().getUnderGround() != null) {
        getVisionArea().getUnderGround().callGuards();
      }
    }
  }
  
  public final boolean isPathing()
  {
    return this.isPathing;
  }
  
  public final void setPathing(boolean pathing, boolean removeFromPathing)
  {
    this.isPathing = pathing;
    if (removeFromPathing) {
      if ((isHuman()) || (isGhost()) || (isUnique())) {
        pathFinderNPC.removeTarget(this);
      } else if (isAggHuman()) {
        pathFinderAgg.removeTarget(this);
      } else {
        pathFinder.removeTarget(this);
      }
    }
  }
  
  private boolean isPathing = false;
  private boolean setTargetNOID = false;
  private Creature creatureToBlinkTo = null;
  public boolean receivedPath = false;
  private PathTile targetPathTile = null;
  
  public final void startPathingToTile(PathTile p)
  {
    if (this.creatureToBlinkTo == null)
    {
      this.targetPathTile = p;
      if (p != null)
      {
        sendToLoggers("heading to specific " + p.getTileX() + "," + p.getTileY(), (byte)2);
        setPathing(true, false);
        if ((isHuman()) || (isGhost()) || (isUnique())) {
          pathFinderNPC.addTarget(this, p);
        } else if (isAggHuman()) {
          pathFinderAgg.addTarget(this, p);
        } else {
          pathFinder.addTarget(this, p);
        }
      }
    }
  }
  
  static int totx = 0;
  static int toty = 0;
  static int movesx = 0;
  static int movesy = 0;
  protected static final String NOPATH = "No pathing now";
  
  public final void startPathing(int seed)
  {
    if (this.creatureToBlinkTo == null)
    {
      PathTile p = getMoveTarget(seed);
      if (p != null) {
        startPathingToTile(p);
      }
    }
  }
  
  public final void checkMove()
    throws NoSuchCreatureException, NoSuchPlayerException
  {
    if (this.hitchedTo != null) {
      return;
    }
    if (isSentinel()) {
      return;
    }
    if ((isHorse()) || (isUnicorn()))
    {
      Item torsoItem = getWornItem((byte)2);
      if (torsoItem != null) {
        if ((torsoItem.isSaddleLarge()) || (torsoItem.isSaddleNormal())) {
          return;
        }
      }
    }
    Npc localNpc1;
    Npc npc;
    if (isDominated())
    {
      if (hasOrders()) {
        if (this.target == -10L)
        {
          if (this.status.getPath() == null)
          {
            if (!isPathing()) {
              startPathing(0);
            }
          }
          else if ((moveAlongPath()) || (isTeleporting()))
          {
            this.status.setPath(null);
            this.status.setMoving(false);
            if (isSpy())
            {
              Creature linkedToc = getCreatureLinkedTo();
              if (isWithinSpyDist(linkedToc))
              {
                turnTowardsCreature(linkedToc);
                Npc[] arrayOfNpc = Creatures.getInstance().getNpcs();int i = arrayOfNpc.length;
                for (localNpc1 = 0; localNpc1 < i; localNpc1++)
                {
                  npc = arrayOfNpc[localNpc1];
                  if ((!npc.isDead()) && (isSpyFriend(npc))) {
                    if (npc.isWithinDistanceTo(this, 400.0F)) {
                      if (npc.longTarget == null)
                      {
                        npc.longTarget = new LongTarget(linkedToc.getTileX(), linkedToc.getTileY(), 0, linkedToc.isOnSurface(), linkedToc.getFloorLevel(), npc);
                        if (!npc.isWithinDistanceTo(linkedToc, 100.0F))
                        {
                          int seed = Server.rand.nextInt(5);
                          String mess = "Think I'll go hunt for " + linkedToc.getName() + " a bit...";
                          switch (seed)
                          {
                          case 0: 
                            mess = linkedToc.getName() + " is in trouble now!";
                            break;
                          case 1: 
                            mess = "Going to check out what " + linkedToc.getName() + " is doing.";
                            break;
                          case 2: 
                            mess = "Heading to slay " + linkedToc.getName() + ".";
                            break;
                          case 3: 
                            mess = "Going to get me the scalp of " + linkedToc.getName() + " today.";
                            break;
                          case 4: 
                            mess = "Poor " + linkedToc.getName() + " won't know what hit " + linkedToc.getHimHerItString() + ".";
                            break;
                          default: 
                            mess = "Think I'll go hunt for " + linkedToc.getName() + " a bit...";
                          }
                          VolaTile tile = npc.getCurrentTile();
                          if (tile != null)
                          {
                            Message m = new Message(npc, (byte)0, ":Local", "<" + npc.getName() + "> " + mess);
                            tile.broadCastMessage(m);
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
        else if (this.status.getPath() != null)
        {
          if ((moveAlongPath()) || (isTeleporting()))
          {
            this.status.setPath(null);
            this.status.setMoving(false);
          }
        }
        else {
          hunt();
        }
      }
    }
    else if (this.leader == null) {
      if ((!this.shouldStandStill) && (!this.status.isUnconscious()) && (this.status.getStunned() == 0.0F)) {
        if (isMoveGlobal())
        {
          if (this.status.getPath() != null)
          {
            if ((moveAlongPath()) || (isTeleporting()))
            {
              this.status.setPath(null);
              this.status.setMoving(false);
            }
          }
          else if ((isHunter()) && (this.target != -10L) && (this.fleeCounter <= 0))
          {
            hunt();
          }
          else
          {
            if (Server.rand.nextInt(100) == 0)
            {
              PathTile targ = getPersonalTargetTile();
              if (targ != null) {
                if (!this.isPathing) {
                  startPathingToTile(targ);
                }
              }
            }
            if (this.status.moving)
            {
              if (Server.rand.nextInt(100) < 5) {
                this.status.setMoving(false);
              }
            }
            else
            {
              int mod = 1;
              int max = 2000;
              if (((isCareful()) && (getStatus().damage > 10000)) || (this.loggerCreature1 > 0L)) {
                mod = 19;
              } else if ((isBred()) || (isBranded()) || (isCaredFor())) {
                max = 20000;
              } else if ((isNpc()) && (!isAggHuman()) && (getCitizenVillage() != null)) {
                max = 200 + (int)(getWurmId() % 100L) * 3;
              }
              if ((Server.rand.nextInt(Math.max(1, max - this.template.getMoveRate() * mod)) < 5) || (shouldFlee()))
              {
                this.status.setMoving(true);
              }
              else if ((Features.Feature.CREATURE_MOVEMENT_CHANGES.isEnabled()) && (
                (Server.rand.nextInt(Math.max(1, 1000 - this.template.getMoveRate())) < 5) || (this.loggerCreature1 > 0L)))
              {
                Fence[] arrayOfFence = getCurrentTile().getAllFences();localNpc1 = arrayOfFence.length;
                for (npc = 0; npc < localNpc1; npc++)
                {
                  Fence f = arrayOfFence[npc];
                  if ((f.isHorizontal()) && (Math.abs(f.getPositionY() - getPosY()) < 1.25F))
                  {
                    takeSimpleStep();
                    break;
                  }
                  if ((!f.isHorizontal()) && (Math.abs(f.getPositionX() - getPosX()) < 1.25F))
                  {
                    takeSimpleStep();
                    break;
                  }
                }
              }
            }
            if ((this.status.moving) && (!isTeleporting())) {
              takeSimpleStep();
            }
          }
        }
        else if (this.status.getPath() == null)
        {
          if (!isTeleporting())
          {
            if (!isPathing())
            {
              if ((this.target == -10L) || (shouldFlee()))
              {
                int mod = 1;
                int max = 2000;
                if ((isCareful()) && (getStatus().damage > 10000)) {
                  mod = 19;
                }
                if (this.loggerCreature1 > 0L) {
                  mod = 19;
                }
                int seed = Server.rand.nextInt(Math.max(2, max - this.template.getMoveRate() * mod));
                if (getPositionZ() < 0.0F) {
                  seed -= 100;
                }
                if ((seed < 8) || ((isSpiritGuard()) && (this.citizenVillage != this.currentVillage)) || (shouldFlee())) {
                  startPathing(seed);
                }
              }
              else
              {
                hunt();
              }
              if (Features.Feature.CREATURE_MOVEMENT_CHANGES.isEnabled())
              {
                if ((Server.rand.nextInt(Math.max(1, 1000 - this.template.getMoveRate())) < 5) || (this.loggerCreature1 > 0L))
                {
                  float xMod = getPosX() % 4.0F;
                  float yMod = getPosY() % 4.0F;
                  if ((xMod > 3.5F) || (xMod < 0.5F) || (yMod > 3.5F) || (yMod < 0.5F)) {
                    takeSimpleStep();
                  }
                }
                if ((shouldFlee()) && (getPathfindCounter() > 10) && (this.targetPathTile != null)) {
                  if ((getTileX() != this.targetPathTile.getTileX()) || (getTileY() != this.targetPathTile.getTileY()))
                  {
                    if ((getPathfindCounter() % 50 == 0) && (Server.rand.nextFloat() < 0.05F)) {
                      turnTowardsTile((short)this.targetPathTile.getTileX(), (short)this.targetPathTile.getTileY());
                    }
                    takeSimpleStep();
                  }
                }
              }
            }
            else
            {
              sendToLoggers("still pathing");
            }
          }
          else
          {
            this.status.setPath(null);
            this.status.setMoving(false);
          }
        }
        else if ((moveAlongPath()) || (isTeleporting()))
        {
          this.status.setPath(null);
          this.status.setMoving(false);
        }
      }
    }
  }
  
  public float getMoveModifier(int tile)
  {
    short height = Tiles.decodeHeight(tile);
    if (height < 2) {
      return 0.5F * this.status.getMovementTypeModifier();
    }
    return Tiles.getTile(Tiles.decodeType(tile)).speed * this.status.getMovementTypeModifier();
  }
  
  public boolean mayManageGuards()
  {
    if (this.citizenVillage != null) {
      return this.citizenVillage.isActionAllowed((short)67, this);
    }
    return false;
  }
  
  public boolean isMoving()
  {
    return this.status.isMoving();
  }
  
  public static final float normalizeAngle(float angle)
  {
    return MovementChecker.normalizeAngle(angle);
  }
  
  public final void checkBridgeMove(VolaTile oldTile, VolaTile newtile, int diffZ)
  {
    Object localObject;
    BridgePart bp;
    if ((getBridgeId() == -10L) && (newtile.getStructure() != null))
    {
      BridgePart[] bridgeParts = newtile.getBridgeParts();
      if (bridgeParts != null)
      {
        BridgePart[] arrayOfBridgePart1 = bridgeParts;int i = arrayOfBridgePart1.length;
        for (localObject = 0; localObject < i; localObject++)
        {
          bp = arrayOfBridgePart1[localObject];
          if (bp.isFinished())
          {
            boolean enter = false;
            
            float nz = Zones.calculatePosZ(getPosX(), getPosY(), newtile, isOnSurface(), 
              isFloating(), getPositionZ(), this, bp.getStructureId());
            float oldPosZ = getPositionZ() + diffZ / 10.0F;
            float diff = Math.abs(oldPosZ - nz);
            
            float maxDiff = 1.3F;
            if (oldTile != null)
            {
              if ((bp.getDir() == 0) || (bp.getDir() == 4))
              {
                if (oldTile.getTileY() == newtile.getTileY()) {
                  if (diff < 1.3F) {
                    if (bp.hasAnExit()) {
                      enter = true;
                    }
                  }
                }
              }
              else if (oldTile.getTileX() == newtile.getTileX()) {
                if (diff < 1.3F) {
                  if (bp.hasAnExit()) {
                    enter = true;
                  }
                }
              }
            }
            else {
              enter = diff < 1.3F;
            }
            if (enter)
            {
              setBridgeId(bp.getStructureId());
              int movedz = (int)((getPositionZ() - nz) * 10.0F);
              setPositionZ(nz);
              moved(0, 0, movedz, 0, 0);
              break;
            }
          }
        }
      }
    }
    else if (getBridgeId() != -10L)
    {
      boolean leave = true;
      BridgePart bp;
      if (oldTile != null)
      {
        BridgePart[] bridgeParts = oldTile.getBridgeParts();
        if (bridgeParts != null)
        {
          BridgePart[] arrayOfBridgePart2 = bridgeParts;localObject = arrayOfBridgePart2.length;
          for (bp = 0; bp < localObject; bp++)
          {
            bp = arrayOfBridgePart2[bp];
            if (bp.isFinished()) {
              if ((bp.getDir() == 0) || (bp.getDir() == 4))
              {
                if (oldTile.getTileX() != newtile.getTileX()) {
                  leave = false;
                }
              }
              else if (oldTile.getTileY() != newtile.getTileY()) {
                leave = false;
              }
            }
          }
        }
      }
      if (leave) {
        if ((newtile.getStructure() == null) || (newtile.getStructure().getWurmId() != getBridgeId()))
        {
          setBridgeId(-10L);
        }
        else
        {
          BridgePart[] bridgeParts = newtile.getBridgeParts();
          boolean foundBridge = false;
          localObject = bridgeParts;bp = localObject.length;
          for (bp = 0; bp < bp; bp++)
          {
            BridgePart bp = localObject[bp];
            
            foundBridge = true;
            if (!bp.isFinished())
            {
              setBridgeId(-10L);
              return;
            }
          }
          if (foundBridge)
          {
            localObject = bridgeParts;bp = localObject.length;
            for (bp = 0; bp < bp; bp++)
            {
              BridgePart bp = localObject[bp];
              if ((bp.isFinished()) && (bp.hasAnExit()))
              {
                setBridgeId(bp.getStructureId());
                return;
              }
            }
          }
        }
      }
    }
  }
  
  public boolean moveAlongPath()
  {
    long start = System.nanoTime();
    try
    {
      Path path = null;
      
      int mvs = 2;
      if (this.target != -10L) {
        mvs = 3;
      }
      if (getSize() >= 5) {
        mvs += 3;
      }
      for (int x = 0; x < mvs; x++)
      {
        path = this.status.getPath();
        if ((path != null) && (!path.isEmpty()))
        {
          PathTile next = path.getFirst();
          if ((next.getTileX() == getCurrentTile().tilex) && (next.getTileY() == getCurrentTile().tiley))
          {
            boolean canRemove = true;
            float diffY;
            double totalDist;
            if (next.hasSpecificPos())
            {
              float diffX = this.status.getPositionX() - next.getPosX();
              diffY = this.status.getPositionY() - next.getPosY();
              totalDist = Math.sqrt(diffX * diffX + diffY * diffY);
              float lMod = getMoveModifier((isOnSurface() ? Server.surfaceMesh : Server.caveMesh)
                .getTile((int)this.status.getPositionX() >> 2, (int)this.status.getPositionY() >> 2));
              if (totalDist > getSpeed() * lMod) {
                canRemove = false;
              }
            }
            if (canRemove)
            {
              path.removeFirst();
              Floor[] floors;
              if (getTarget() != null) {
                if ((getTarget().getTileX() == getTileX()) && (getTarget().getTileY() == getTileY()) && 
                  (getTarget().getFloorLevel() != getFloorLevel())) {
                  if (isSpiritGuard())
                  {
                    pushToFloorLevel(getTarget().getFloorLevel());
                  }
                  else if (canOpenDoors())
                  {
                    floors = getCurrentTile().getFloors(
                      Math.min(getFloorLevel(), getTarget().getFloorLevel()) * 30, 
                      Math.max(getFloorLevel(), getTarget().getFloorLevel()) * 30);
                    diffY = floors;totalDist = diffY.length;
                    for (double d1 = 0; d1 < totalDist; d1++)
                    {
                      Floor f = diffY[d1];
                      if (getTarget().getFloorLevel() > getFloorLevel())
                      {
                        if (f.getFloorLevel() == getFloorLevel() + 1) {
                          if ((f.isOpening()) || (f.isAPlan()))
                          {
                            pushToFloorLevel(f.getFloorLevel());
                            break;
                          }
                        }
                      }
                      else if (f.getFloorLevel() == getFloorLevel()) {
                        if ((f.isOpening()) || (f.isAPlan()))
                        {
                          pushToFloorLevel(f.getFloorLevel() - 1);
                          break;
                        }
                      }
                    }
                  }
                }
              }
              if (path.isEmpty())
              {
                floors = 1;return floors;
              }
              next = path.getFirst();
            }
          }
          float lPosX = this.status.getPositionX();
          float lPosY = this.status.getPositionY();
          float lPosZ = this.status.getPositionZ();
          float lRotation = this.status.getRotation();
          
          double lNewRotation = next.hasSpecificPos() ? Math.atan2(next.getPosY() - lPosY, next.getPosX() - lPosX) : Math.atan2((next.getTileY() << 2) + 2 - lPosY, (next.getTileX() << 2) + 2 - lPosX);
          lRotation = (float)(lNewRotation * 57.29577951308232D) + 90.0F;
          int lOldTileX = (int)lPosX >> 2;
          int lOldTileY = (int)lPosY >> 2;
          MeshIO lMesh;
          MeshIO lMesh;
          if (isOnSurface()) {
            lMesh = Server.surfaceMesh;
          } else {
            lMesh = Server.caveMesh;
          }
          float lMod = getMoveModifier(lMesh.getTile(lOldTileX, lOldTileY));
          float lXPosMod = (float)Math.sin(lRotation * 0.017453292F) * getSpeed() * lMod;
          float lYPosMod = -(float)Math.cos(lRotation * 0.017453292F) * getSpeed() * lMod;
          
          int lNewTileX = (int)(lPosX + lXPosMod) >> 2;
          int lNewTileY = (int)(lPosY + lYPosMod) >> 2;
          
          int lDiffTileX = lNewTileX - lOldTileX;
          int lDiffTileY = lNewTileY - lOldTileY;
          if ((Math.abs(lDiffTileX) > 1) || (Math.abs(lDiffTileY) > 1)) {
            logger.log(Level.WARNING, getName() + "," + getWurmId() + " diffTileX=" + lDiffTileX + ", y=" + lDiffTileY);
          }
          if ((lDiffTileX != 0) || (lDiffTileY != 0))
          {
            if (!isOnSurface()) {
              if (Tiles.isSolidCave(Tiles.decodeType(lMesh.getTile(lNewTileX, lNewTileY))))
              {
                rotateRandom(lRotation, 45);
                try
                {
                  takeSimpleStep();
                }
                catch (NoSuchPlayerException localNoSuchPlayerException) {}catch (NoSuchCreatureException localNoSuchCreatureException) {}
                localNoSuchCreatureException = 1;return localNoSuchCreatureException;
              }
            }
            if (!isGhost())
            {
              BlockingResult result = Blocking.getBlockerBetween(this, getPosX(), getPosY(), lPosX + lXPosMod, lPosY + lYPosMod, 
                getPositionZ(), getPositionZ(), isOnSurface(), isOnSurface(), false, 6, true, -10L, 
                
                getBridgeId(), getBridgeId(), followsGround());
              if (result != null)
              {
                boolean foundDoor = false;
                for (Blocker blocker : result.getBlockerArray()) {
                  if (blocker.isDoor()) {
                    if (!blocker.canBeOpenedBy(this, false)) {
                      foundDoor = true;
                    }
                  }
                }
                if (!foundDoor)
                {
                  path.clear();
                  boolean bool1 = true;return bool1;
                }
              }
            }
            if ((!next.hasSpecificPos()) && (next.getTileX() == lNewTileX) && (next.getTileY() == lNewTileY)) {
              path.removeFirst();
            }
            movesx += lDiffTileX;
            movesy += lDiffTileY;
          }
          int lOldPosX = (int)(lPosX * 10.0F);
          int lOldPosY = (int)(lPosY * 10.0F);
          int lOldPosZ = (int)(lPosZ * 10.0F);
          lPosX += lXPosMod;
          lPosY += lYPosMod;
          if ((lPosX >= Zones.worldTileSizeX - 1 << 2) || (lPosX < 0.0F) || (lPosY < 0.0F) || (lPosY >= Zones.worldTileSizeY - 1 << 2))
          {
            destroy();
            
            ??? = 1;return ???;
          }
          lPosZ = calculatePosZ();
          int lNewShortZ = (int)(lPosZ * 10.0F);
          if (lPosZ < -0.5D) {
            if (isSubmerged())
            {
              boolean bool2;
              if ((isFloating()) && (lNewShortZ > this.template.offZ * 10.0F))
              {
                rotateRandom(lRotation, 100);
                if (this.target != -10L) {
                  setTarget(-10L, true);
                }
                bool2 = true;return bool2;
              }
              if ((!isFloating()) && (lPosZ > -5.0F) && (lOldPosZ < lNewShortZ))
              {
                rotateRandom(lRotation, 100);
                if (this.target != -10L) {
                  setTarget(-10L, true);
                }
                bool2 = true;return bool2;
              }
              if (lPosZ < -5.0F)
              {
                if (x == 3) {
                  if (isFloating())
                  {
                    lPosZ = this.template.offZ;
                  }
                  else
                  {
                    float newdiff = Math.max(-1.0F, Math.min(1.0F, (float)Server.rand.nextGaussian()));
                    float newPosZ = Math.max(lPosZ, 
                      Math.min(-5.0F, getPositionZ() + newdiff));
                    lPosZ = newPosZ;
                  }
                }
              }
              else if (x == 3) {
                if (isFloating()) {
                  lPosZ = this.template.offZ;
                }
              }
            }
            else
            {
              lPosZ = Math.max(-1.25F, lPosZ);
              if (isFloating()) {
                lPosZ = Math.max(this.template.offZ, lPosZ);
              }
            }
          }
          this.status.setPositionX(lPosX);
          this.status.setPositionY(lPosY);
          this.status.setPositionZ(lPosZ);
          this.status.setRotation(lRotation);
          int lDiffPosX = (int)(lPosX * 10.0F) - lOldPosX;
          int lDiffPosY = (int)(lPosY * 10.0F) - lOldPosY;
          int lDiffPosZ = (int)(lPosZ * 10.0F) - lOldPosZ;
          
          moved(lDiffPosX, lDiffPosY, lDiffPosZ, lDiffTileX, lDiffTileY);
        }
      }
      if (path != null)
      {
        if ((this.pathRecalcLength > 0) && (path.getSize() <= this.pathRecalcLength))
        {
          x = 1;return x;
        }
        x = path.isEmpty();return x;
      }
      x = 1;return x;
    }
    finally {}
  }
  
  protected boolean startDestroyingWall(Wall wall)
  {
    try
    {
      BehaviourDispatcher.action(this, this.communicator, getEquippedWeapon((byte)14).getWurmId(), wall.getId(), (short)180);
    }
    catch (FailedException fe)
    {
      return true;
    }
    catch (NoSuchBehaviourException nsb)
    {
      logger.log(Level.WARNING, nsb.getMessage(), nsb);
      return true;
    }
    catch (NoSuchCreatureException nsc)
    {
      logger.log(Level.WARNING, nsc.getMessage(), nsc);
      return true;
    }
    catch (NoSuchItemException nsi)
    {
      logger.log(Level.WARNING, nsi.getMessage(), nsi);
      return true;
    }
    catch (NoSuchPlayerException nsp)
    {
      logger.log(Level.WARNING, nsp.getMessage(), nsp);
      return true;
    }
    catch (NoSuchWallException nsw)
    {
      logger.log(Level.WARNING, nsw.getMessage(), nsw);
      return true;
    }
    catch (NoSpaceException nsp)
    {
      logger.log(Level.WARNING, nsp.getMessage(), nsp);
      return true;
    }
    return false;
  }
  
  public void followLeader(int diffX, int diffY)
  {
    float iposx = this.leader.getStatus().getPositionX();
    float iposy = this.leader.getStatus().getPositionY();
    float diffx = iposx - this.status.getPositionX();
    float diffy = iposy - this.status.getPositionY();
    int diff = (int)Math.max(Math.abs(diffx), Math.abs(diffy));
    if ((diffx < 0.0F) && (this.status.getPositionX() < 10.0F)) {
      return;
    }
    if ((diffy < 0.0F) && (this.status.getPositionY() < 10.0F)) {
      return;
    }
    if ((diffy > 0.0F) && (this.status.getPositionY() > Zones.worldMeterSizeY - 10.0F)) {
      return;
    }
    if ((diffx > 0.0F) && (this.status.getPositionX() > Zones.worldMeterSizeX - 10.0F)) {
      return;
    }
    if (diff > 35)
    {
      logger.log(Level.INFO, this.leader.getName() + " moved " + diff + "diffx=" + diffx + ", diffy=" + diffy);
      setLeader(null);
    }
    else if ((diffx > 4.0F) || (diffy > 4.0F) || (diffx < -4.0F) || (diffy < -4.0F))
    {
      float lPosX = this.status.getPositionX();
      float lPosY = this.status.getPositionY();
      float lPosZ = this.status.getPositionZ();
      int lOldTileX = (int)lPosX >> 2;
      int lOldTileY = (int)lPosY >> 2;
      double lNewrot = Math.atan2(iposy - lPosY, iposx - lPosX);
      lNewrot = lNewrot * 57.29577951308232D + 90.0D;
      if (lNewrot > 360.0D) {
        lNewrot -= 360.0D;
      }
      if (lNewrot < 0.0D) {
        lNewrot += 360.0D;
      }
      float movex = 0.0F;
      float movey = 0.0F;
      if (diffx < -4.0F) {
        movex = diffx + 4.0F;
      } else if (diffx > 4.0F) {
        movex = diffx - 4.0F;
      }
      if (diffy < -4.0F) {
        movey = diffy + 4.0F;
      } else if (diffy > 4.0F) {
        movey = diffy - 4.0F;
      }
      float lXPosMod = (float)Math.sin(lNewrot * 0.01745329238474369D) * Math.abs(movex + Server.rand.nextFloat());
      float lYPosMod = -(float)Math.cos(lNewrot * 0.01745329238474369D) * Math.abs(movey + Server.rand.nextFloat());
      
      float newPosX = lPosX + lXPosMod;
      float newPosY = lPosY + lYPosMod;
      
      int lNewTileX = (int)newPosX >> 2;
      int lNewTileY = (int)newPosY >> 2;
      
      int lDiffTileX = lNewTileX - lOldTileX;
      int lDiffTileY = lNewTileY - lOldTileY;
      if ((lDiffTileX != 0) || (lDiffTileY != 0)) {
        if (!isGhost()) {
          if ((this.leader.getBridgeId() < 0L) && (getBridgeId() < 0L))
          {
            BlockingResult result = Blocking.getBlockerBetween(this, lPosX, lPosY, newPosX, newPosY, 
              getPositionZ(), this.leader.getPositionZ(), isOnSurface(), isOnSurface(), false, 2, -1L, 
              getBridgeId(), getBridgeId(), followsGround());
            if (result != null)
            {
              Blocker first = result.getFirstBlocker();
              if (!first.isDoor())
              {
                this.leader.sendToLoggers("Your floor level " + this.leader.getFloorLevel() + ", creature: " + 
                  getFloorLevel());
                setLeader(null);
                return;
              }
              if ((!first.canBeOpenedBy(this.leader, false)) && (!first.canBeOpenedBy(this, false)))
              {
                this.leader.sendToLoggers("Your floor level " + this.leader.getFloorLevel() + ", creature: " + 
                  getFloorLevel());
                setLeader(null);
                return;
              }
            }
          }
        }
      }
      if ((!this.leader.isOnSurface()) && (!isOnSurface())) {
        if (Tiles.isSolidCave(Tiles.decodeType(Server.caveMesh.getTile((int)newPosX >> 2, (int)newPosY >> 2))))
        {
          newPosX = iposx;
          newPosY = iposy;
        }
      }
      float newPosZ = calculatePosZ();
      if ((!isSwimming()) && (newPosZ < -0.71D) && (newPosZ < lPosZ))
      {
        setLeader(null);
        this.status.setPositionZ(newPosZ);
      }
      else
      {
        newPosZ = Math.max(-1.25F, newPosZ);
        if (isFloating()) {
          newPosZ = Math.max(this.template.offZ, newPosZ);
        }
        setRotation((float)lNewrot);
        int tilex = (int)lPosX >> 2;
        int tiley = (int)lPosY >> 2;
        
        int newtilex = (int)newPosX >> 2;
        int newtiley = (int)newPosY >> 2;
        this.status.setPositionX(newPosX);
        this.status.setPositionY(newPosY);
        this.status.setPositionZ(newPosZ);
        int lDiffPosX = (int)(newPosX * 10.0F) - (int)(lPosX * 10.0F);
        int lDiffPosY = (int)(newPosY * 10.0F) - (int)(lPosY * 10.0F);
        int lDiffPosZ = (int)(newPosZ * 10.0F) - (int)(lPosZ * 10.0F);
        
        moved(lDiffPosX, lDiffPosY, lDiffPosZ, newtilex - tilex, newtiley - tiley);
      }
    }
  }
  
  public void sendAttitudeChange()
  {
    if (this.currentTile != null) {
      this.currentTile.checkChangedAttitude(this);
    }
  }
  
  public final void takeSimpleStep()
    throws NoSuchCreatureException, NoSuchPlayerException
  {
    long start = 0L;
    try
    {
      int mvs = 2;
      if (this.target != -10L) {
        mvs = 3;
      }
      if (getSize() >= 5) {
        mvs += 3;
      }
      for (int x = 0; x < mvs; x++)
      {
        float lPosX = this.status.getPositionX();
        float lPosY = this.status.getPositionY();
        float lPosZ = this.status.getPositionZ();
        float lRotation = this.status.getRotation();
        int lOldPosX = (int)(lPosX * 10.0F);
        int lOldPosY = (int)(lPosY * 10.0F);
        int lOldPosZ = (int)(lPosZ * 10.0F);
        
        int lOldTileX = (int)lPosX >> 2;
        int lOldTileY = (int)lPosY >> 2;
        if (this.target == -10L)
        {
          if (isOnSurface())
          {
            int rand = Server.rand.nextInt(100);
            if (rand < 10)
            {
              float lXPosMod = (float)Math.sin(lRotation * 0.017453292F) * 12.0F;
              float lYPosMod = -(float)Math.cos(lRotation * 0.017453292F) * 12.0F;
              
              int lNewTileX = Zones.safeTileX((int)(lPosX + lXPosMod) >> 2);
              int lNewTileY = Zones.safeTileY((int)(lPosY + lYPosMod) >> 2);
              int tile = Zones.getTileIntForTile(lNewTileX, lNewTileY, getLayer());
              if (isTargetTileTooHigh(lNewTileX, lNewTileY, tile, lPosZ < 0.0F))
              {
                short[] lLowestNode = getLowestTileCorner((short)lOldTileX, (short)lOldTileY);
                
                turnTowardsTile(lLowestNode[0], lLowestNode[1]);
              }
            }
            else if (rand < 12)
            {
              rotateRandom(lRotation, 100);
            }
            else if (rand < 15)
            {
              lRotation = normalizeAngle(lRotation + Server.rand.nextInt(100));
            }
          }
          else
          {
            int rand = Server.rand.nextInt(100);
            if (rand < 2) {
              rotateRandom(lRotation, 100);
            } else if (rand < 5) {
              lRotation = normalizeAngle(lRotation + Server.rand.nextInt(100));
            }
          }
        }
        else {
          turnTowardsCreature(getTarget());
        }
        lRotation = normalizeAngle(lRotation);
        float lMoveModifier;
        float lMoveModifier;
        if (!isOnSurface()) {
          lMoveModifier = getMoveModifier(Server.caveMesh.getTile(lOldTileX, lOldTileY));
        } else {
          lMoveModifier = getMoveModifier(Server.surfaceMesh.getTile(lOldTileX, lOldTileY));
        }
        float lXPosMod = (float)Math.sin(lRotation * 0.017453292F) * getSpeed() * lMoveModifier;
        float lYPosMod = -(float)Math.cos(lRotation * 0.017453292F) * getSpeed() * lMoveModifier;
        
        int lNewTileX = (int)(lPosX + lXPosMod) >> 2;
        int lNewTileY = (int)(lPosY + lYPosMod) >> 2;
        
        int lDiffTileX = lNewTileX - lOldTileX;
        int lDiffTileY = lNewTileY - lOldTileY;
        if ((lDiffTileX != 0) || (lDiffTileY != 0)) {
          if (!isGhost())
          {
            if (!isOnSurface())
            {
              if (Tiles.isSolidCave(Tiles.decodeType(Server.caveMesh.getTile(lOldTileX, lOldTileY))))
              {
                logger.log(Level.INFO, getName() + " is in rock at takesimplestep. Dying.");
                die(false, "Suffocated in Rock");
                return;
              }
              if (Tiles.isSolidCave(Tiles.decodeType(Server.caveMesh.getTile(lNewTileX, lNewTileY))))
              {
                if (this.currentTile.isTransition)
                {
                  sendToLoggers(lPosZ + " setting to surface then moving.");
                  if ((!Tiles.isMineDoor(Tiles.decodeType(Server.caveMesh.getTile(getTileX(), getTileY())))) || 
                    (MineDoorPermission.getPermission(getTileX(), getTileY()).mayPass(this))) {
                    setLayer(0, true);
                  } else {
                    rotateRandom(lRotation, 45);
                  }
                  return;
                }
                rotateRandom(lRotation, 45);
              }
            }
            else if (Tiles.Tile.TILE_LAVA.id == Tiles.decodeType(Server.surfaceMesh.getTile(lNewTileX, lNewTileY)))
            {
              rotateRandom(lRotation, 45); return;
            }
            BlockingResult result;
            BlockingResult result;
            if (Features.Feature.CREATURE_MOVEMENT_CHANGES.isEnabled()) {
              result = Blocking.getBlockerBetween(this, lPosX, lPosY, lPosX + lXPosMod, lPosY + lYPosMod, 
                getPositionZ(), getPositionZ(), isOnSurface(), isOnSurface(), false, 6, -1L, 
                getBridgeId(), getBridgeId(), followsGround());
            } else {
              result = Blocking.getBlockerBetween(this, lPosX, lPosY, lPosX + lXPosMod, lPosY + lYPosMod, 
                getPositionZ(), getPositionZ(), isOnSurface(), isOnSurface(), false, 6, -1L, 
                getBridgeId(), getBridgeId(), followsGround());
            }
            if (result != null)
            {
              Blocker first = result.getFirstBlocker();
              if ((isKingdomGuard()) || (isSpiritGuard()))
              {
                if (!first.isDoor()) {
                  rotateRandom(lRotation, 100);
                }
              }
              else
              {
                if (Features.Feature.CREATURE_MOVEMENT_CHANGES.isEnabled())
                {
                  turnTowardsTile((short)getTileX(), (short)getTileY());
                  rotateRandom(this.status.getRotation(), 45);
                  x = 0;
                  
                  getStatus().setMoving(false);
                  continue;
                }
                rotateRandom(lRotation, 100);
                return;
              }
            }
            VolaTile t = Zones.getOrCreateTile(lNewTileX, lNewTileY, isOnSurface());
            VolaTile myt = getCurrentTile();
            if (((t.isGuarded()) && (myt != null) && (!myt.isGuarded())) || ((isAnimal()) && (t.hasFire())))
            {
              rotateRandom(lRotation, 100);
              return;
            }
          }
        }
        lPosX += lXPosMod;
        lPosY += lYPosMod;
        if ((lPosX >= Zones.worldTileSizeX - 1 << 2) || (lPosX < 0.0F) || (lPosY < 0.0F) || (lPosY >= Zones.worldTileSizeY - 1 << 2))
        {
          destroy();
          return;
        }
        if (getFloorLevel() == 0)
        {
          try
          {
            lPosZ = Zones.calculateHeight(lPosX, lPosY, isOnSurface());
          }
          catch (NoSuchZoneException nsz)
          {
            logger.log(Level.WARNING, this.name + " moved out of zone.");
          }
          if (isFloating()) {
            lPosZ = Math.max(this.template.offZ, lPosZ);
          }
          int lNewShortZ = (int)(lPosZ * 10.0F);
          if (lPosZ < 0.5D)
          {
            if (isSubmerged())
            {
              if ((isFloating()) && (lNewShortZ > this.template.offZ * 10.0F))
              {
                rotateRandom(lRotation, 100);
                if (this.target != -10L) {
                  setTarget(-10L, true);
                }
                return;
              }
              if ((!isFloating()) && (lPosZ > -5.0F) && (lOldPosZ < lNewShortZ))
              {
                rotateRandom(lRotation, 100);
                if (this.target != -10L) {
                  setTarget(-10L, true);
                }
                return;
              }
              if (lPosZ < -5.0F)
              {
                if (x == 3) {
                  if (isFloating())
                  {
                    lPosZ = this.template.offZ;
                  }
                  else
                  {
                    float newdiff = Math.max(-1.0F, Math.min(1.0F, (float)Server.rand.nextGaussian()));
                    
                    float newPosZ = Math.max(lPosZ, Math.min(-5.0F, getPositionZ() + newdiff));
                    lPosZ = newPosZ;
                  }
                }
              }
              else if (x == 3) {
                if (isFloating()) {
                  lPosZ = this.template.offZ;
                }
              }
            }
            if (((lPosZ > -2.0F) || (lOldPosZ <= -20)) && ((lOldPosZ < 0) || (this.target != -10L)) && (isSwimming()))
            {
              lPosZ = Math.max(-1.25F, lPosZ);
              if (isFloating()) {
                lPosZ = Math.max(this.template.offZ, lPosZ);
              }
            }
            else if ((lPosZ < -0.5D) && (!isSubmerged()))
            {
              rotateRandom(lRotation, 100);
              if (this.target != -10L) {
                setTarget(-10L, true);
              }
            }
          }
          else if (isSubmerged())
          {
            if (lOldPosZ < lNewShortZ)
            {
              rotateRandom(lRotation, 100);
              if (this.target != -10L) {
                setTarget(-10L, true);
              }
              return;
            }
          }
        }
        this.status.setPositionX(lPosX);
        this.status.setPositionY(lPosY);
        if (Structure.isGroundFloorAtPosition(lPosX, lPosY, isOnSurface())) {
          this.status.setPositionZ(lPosZ + 0.25F);
        } else {
          this.status.setPositionZ(lPosZ);
        }
        this.status.setRotation(lRotation);
        int lDiffPosX = (int)(lPosX * 10.0F) - lOldPosX;
        int lDiffPosY = (int)(lPosY * 10.0F) - lOldPosY;
        int lDiffPosZ = (int)(lPosZ * 10.0F) - lOldPosZ;
        moved(lDiffPosX, lDiffPosY, lDiffPosZ, lDiffTileX, lDiffTileY);
      }
    }
    finally {}
  }
  
  public void rotateRandom(float aRot, int degrees)
  {
    aRot -= degrees;
    aRot += Server.rand.nextInt(degrees * 2);
    aRot = normalizeAngle(aRot);
    this.status.setRotation(aRot);
    moved(0, 0, 0, 0, 0);
  }
  
  public int getAttackDistance()
  {
    return this.template.getSize();
  }
  
  public void moved(int diffX, int diffY, int diffZ, int aDiffTileX, int aDiffTileY)
  {
    if (!isDead())
    {
      try
      {
        if ((isPlayer()) || (isWagoner()))
        {
          this.movementScheme.move(diffX, diffY, diffZ);
          if (((isWagoner()) || (hasLink())) && (getVisionArea() != null)) {
            try
            {
              getVisionArea().move(aDiffTileX, aDiffTileY);
            }
            catch (IOException iox)
            {
              return;
            }
          }
          try
          {
            getCurrentTile().creatureMoved(this.id, diffX, diffY, diffZ, aDiffTileX, aDiffTileY);
          }
          catch (NoSuchPlayerException localNoSuchPlayerException) {}catch (NoSuchCreatureException localNoSuchCreatureException) {}
          if ((hasLink()) && (getVisionArea() != null)) {
            getVisionArea().linkZones(aDiffTileX, aDiffTileY);
          }
        }
        else
        {
          try
          {
            getVisionArea().move(aDiffTileX, aDiffTileY);
          }
          catch (IOException iox)
          {
            return;
          }
          try
          {
            getCurrentTile().creatureMoved(this.id, diffX, diffY, diffZ, aDiffTileX, aDiffTileY);
          }
          catch (NoSuchPlayerException localNoSuchPlayerException1) {}catch (NoSuchCreatureException localNoSuchCreatureException1) {}
          getVisionArea().linkZones(aDiffTileX, aDiffTileY);
        }
      }
      catch (NullPointerException ex)
      {
        try
        {
          if (!isPlayer()) {
            createVisionArea();
          }
          return;
        }
        catch (Exception localException) {}
      }
      if ((diffX != 0) || (diffY != 0))
      {
        try
        {
          if ((isPlayer()) && (this.actions.getCurrentAction().isInterruptedAtMove()))
          {
            boolean stop = true;
            if (this.actions.getCurrentAction().getNumber() == 136) {
              getCommunicator().sendToggle(3, false);
            } else if ((this.actions.getCurrentAction().getNumber() == 329) || 
              (this.actions.getCurrentAction().getNumber() == 162)) {
              if (getVehicle() != -10L) {
                stop = false;
              }
            }
            if (stop)
            {
              this.communicator.sendSafeServerMessage("You must not move while doing that.");
              stopCurrentAction();
            }
          }
        }
        catch (NoSuchActionException localNoSuchActionException) {}
        if ((aDiffTileX != 0) || (aDiffTileY != 0)) {
          if (this.musicPlayer != null) {
            this.musicPlayer.moveTile(getCurrentTileNum(), getPositionZ() <= 0.0F);
          }
        }
      }
      if (this.status.isTrading())
      {
        Trade trade = this.status.getTrade();
        Creature lOpponent = null;
        if (trade.creatureOne == this) {
          lOpponent = trade.creatureTwo;
        } else {
          lOpponent = trade.creatureOne;
        }
        if (rangeTo(this, lOpponent) > 6) {
          trade.end(this, false);
        }
      }
    }
  }
  
  public void stopFighting()
  {
    if (this.actions != null) {
      this.actions.removeAttacks(this);
    }
  }
  
  public void turnTowardsCreature(Creature targ)
  {
    if (targ != null)
    {
      double lNewrot = Math.atan2(targ.getStatus().getPositionY() - getStatus().getPositionY(), targ.getStatus()
        .getPositionX() - getStatus().getPositionX());
      setRotation((float)(lNewrot * 57.29577951308232D) + 90.0F);
      if (isSubmerged()) {
        try
        {
          float currFloor = Zones.calculateHeight(getPosX(), getPosY(), isOnSurface());
          
          float maxHeight = isFloating() ? this.template.offZ : Math.min(targ.getPositionZ(), 
            Math.max(-5.0F, currFloor));
          float oldHeight = getPositionZ();
          int diff = (int)(maxHeight * 10.0F) - (int)(oldHeight * 10.0F);
          moved(0, 0, diff, 0, 0);
          return;
        }
        catch (NoSuchZoneException localNoSuchZoneException) {}
      }
      moved(0, 0, 0, 0, 0);
    }
  }
  
  public void turnTowardsTile(short tilex, short tiley)
  {
    double lNewrot = Math.atan2((tiley << 2) + 2 - getStatus().getPositionY(), (tilex << 2) + 2 - 
      getStatus().getPositionX());
    setRotation((float)(lNewrot * 57.29577951308232D) + 90.0F);
    moved(0, 0, 0, 0, 0);
  }
  
  public long getWurmId()
  {
    return this.id;
  }
  
  public int getTemplateId()
  {
    return -10;
  }
  
  public String getNameWithoutPrefixes()
  {
    return this.name;
  }
  
  public String getNameWithoutFatStatus()
  {
    if (getStatus() != null) {
      return getStatus().getAgeString() + " " + getStatus().getTypeString() + this.name;
    }
    return "Unknown";
  }
  
  public String getName()
  {
    String fullName = this.name;
    if (isWagoner()) {
      return fullName;
    }
    if ((isAnimal()) || (isMonster())) {
      if (this.name.toLowerCase().compareTo(this.template.getName().toLowerCase()) == 0) {
        fullName = getPrefixes() + this.name.toLowerCase();
      } else {
        fullName = getPrefixes() + StringUtilities.raiseFirstLetterOnly(this.name);
      }
    }
    if (this.petName.length() > 0) {
      return fullName + " '" + this.petName + "'";
    }
    return fullName;
  }
  
  public String getNamePossessive()
  {
    String toReturn = getName();
    if (toReturn.endsWith("s")) {
      return toReturn + "'";
    }
    return toReturn + "'s";
  }
  
  public String getPrefixes()
  {
    if (isUnique()) {
      return "The " + getStatus().getAgeString() + " " + getStatus().getFatString() + getStatus().getTypeString();
    }
    return getStatus().getAgeString() + " " + getStatus().getFatString() + getStatus().getTypeString();
  }
  
  public void setName(String _name)
  {
    this.name = _name;
  }
  
  public void setPetName(String aPetName)
  {
    if (aPetName == null) {
      this.petName = "";
    } else {
      this.petName = aPetName.substring(0, Math.min(19, aPetName.length()));
    }
  }
  
  public String getColourName()
  {
    return this.template.getColourName(this.status);
  }
  
  public String getColourName(int trait)
  {
    return this.template.getTemplateColourName(trait);
  }
  
  public CreatureStatus getStatus()
  {
    return this.status;
  }
  
  public VisionArea getVisionArea()
  {
    return this.visionArea;
  }
  
  public void trainSkill(String sname)
    throws Exception
  {
    Skill skill = this.skills.getSkill(sname);
    String message = getName() + " trains some " + sname + ", but learns nothing new.";
    double knowledge = skill.getKnowledge(0.0D);
    skill.skillCheck(50.0D, 0.0D, false, 3600.0F);
    if (skill.getKnowledge(0.0D) > knowledge) {
      message = getName() + " trains some  " + sname + " and now have skill " + skill.getKnowledge(0.0D);
    }
    logger.log(Level.INFO, message);
  }
  
  public void setSkill(int skill, float val)
  {
    try
    {
      Skill sktomod = this.skills.getSkill(skill);
      sktomod.setKnowledge(val, false);
    }
    catch (NoSuchSkillException nss)
    {
      this.skills.learn(skill, val);
    }
  }
  
  public void sendSkills()
  {
    try
    {
      loadAffinities();
      skilltree = this.skills.getSkillTree();
      for (Integer number : skilltree.keySet()) {
        try
        {
          Skill skill = (Skill)skilltree.get(number);
          int[] needed = skill.getDependencies();
          int parentSkillId = 0;
          if (needed.length > 0) {
            parentSkillId = needed[0];
          }
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
          getCommunicator().sendAddSkill(number.intValue(), parentSkillId, skill.getName(), (float)skill.getRealKnowledge(), 
            (float)skill.getMinimumValue(), skill.affinity);
        }
        catch (NullPointerException np)
        {
          logger.log(Level.WARNING, "Inconsistency: " + 
            getName() + " forgetting skill with number " + number.intValue(), np);
        }
      }
    }
    catch (Exception ex2)
    {
      Map<Integer, Skill> skilltree;
      logger.log(Level.WARNING, "Failed to load and create skills for creature with name " + this.name + ":" + ex2
        .getMessage(), ex2);
    }
  }
  
  public void loadSkills()
    throws Exception
  {
    if (this.skills == null) {
      logger.log(Level.WARNING, "Skills object is null in creature " + this.name);
    }
    try
    {
      if (!isPlayer())
      {
        if (this.skills.getId() != -10L) {
          this.skills.initializeSkills();
        }
      }
      else if (!this.guest)
      {
        getCommunicator().sendAddSkill(2147483646, 0, "Characteristics", 0.0F, 0.0F, 0);
        getCommunicator().sendAddSkill(2147483643, 0, "Religion", 0.0F, 0.0F, 0);
        getCommunicator().sendAddSkill(Integer.MAX_VALUE, 0, "Skills", 0.0F, 0.0F, 0);
        this.skills.load();
      }
      boolean created = false;
      if ((this.skills.isTemplate()) || (this.skills.getSkills().length == 0))
      {
        Skills newSkills = SkillsFactory.createSkills(this.id);
        newSkills.clone(this.skills.getSkills());
        this.skills = newSkills;
        created = true;
        if (!this.guest) {
          this.skills.save();
        }
        this.skills.addTempSkills();
      }
      if (created)
      {
        if (isUndead())
        {
          this.skills.learn(1023, 30.0F);
          this.skills.learn(10052, 50.0F);
          
          this.skills.getSkill(102).setKnowledge(25.0D, false);
          this.skills.getSkill(103).setKnowledge(25.0D, false);
        }
        if ((Servers.localServer.testServer) && (Servers.localServer.entryServer)) {
          if (WurmId.getType(this.id) == 0)
          {
            int level = 20;
            this.skills.learn(1023, level);
            this.skills.learn(10025, level);
            this.skills.learn(10006, level);
            this.skills.learn(10023, level);
            this.skills.learn(10022, level);
            this.skills.learn(10020, level);
            this.skills.learn(10021, level);
            this.skills.learn(10019, level);
            this.skills.learn(10001, level);
            this.skills.learn(10024, level);
            this.skills.learn(10005, level);
            this.skills.learn(10027, level);
            this.skills.learn(10028, level);
            this.skills.learn(10026, level);
            this.skills.learn(10064, level);
            this.skills.learn(10061, level);
            this.skills.learn(10062, level);
            this.skills.learn(10063, level);
            this.skills.learn(1002, level / 2.0F);
            this.skills.learn(1003, level / 2.0F);
            
            this.skills.learn(10056, level);
            this.skills.getSkill(104).setKnowledge(23.0D, false);
            this.skills.getSkill(1).setKnowledge(3.0D, false);
            this.skills.getSkill(102).setKnowledge(23.0D, false);
            this.skills.getSkill(103).setKnowledge(23.0D, false);
            
            this.skills.learn(10053, level);
            this.skills.learn(10054, level);
            level = (int)(Server.rand.nextFloat() * 100.0F);
            this.skills.learn(1030, level);
            this.skills.learn(10081, level);
            this.skills.learn(10079, level);
            this.skills.learn(10080, level);
          }
        }
      }
      setMoveLimits();
    }
    catch (Exception ex2)
    {
      logger.log(Level.WARNING, "Failed to load and create skills for creature with name " + this.name + ":" + ex2
        .getMessage(), ex2);
    }
  }
  
  public void addStructureTile(VolaTile toAdd, byte structureType)
  {
    if (this.structure == null)
    {
      this.structure = Structures.createStructure(structureType, this.name + "'s planned structure", 
        WurmId.getNextPlanId(), toAdd.tilex, toAdd.tiley, 
        
        isOnSurface());
      this.status.setBuildingId(this.structure.getWurmId());
    }
    else
    {
      try
      {
        this.structure.addBuildTile(toAdd, false);
        if (structureType == 0) {
          this.structure.clearAllWallsAndMakeWallsForStructureBorder(toAdd);
        }
      }
      catch (NoSuchZoneException nsz)
      {
        getCommunicator().sendNormalServerMessage("You can't build there.", (byte)3);
      }
    }
  }
  
  public long getBuildingId()
  {
    return this.status.buildingId;
  }
  
  public String getUndeadModelName()
  {
    if (getUndeadType() == 1)
    {
      if (this.status.sex == 0) {
        return "model.creature.humanoid.human.player.zombie.male" + WurmCalendar.getSpecialMapping(true);
      }
      if (this.status.sex == 1) {
        return "model.creature.humanoid.human.player.zombie.female" + WurmCalendar.getSpecialMapping(true);
      }
      return "model.creature.humanoid.human.player.zombie" + WurmCalendar.getSpecialMapping(true);
    }
    if (getUndeadType() == 2) {
      return "model.creature.humanoid.human.skeleton" + WurmCalendar.getSpecialMapping(true);
    }
    if (getUndeadType() == 3) {
      return "model.creature.humanoid.human.spirit.shadow" + WurmCalendar.getSpecialMapping(true);
    }
    return getModelName();
  }
  
  public String getModelName()
  {
    if (isReborn())
    {
      if (this.status.sex == 0) {
        return this.template.getModelName() + ".zombie.male" + WurmCalendar.getSpecialMapping(true);
      }
      if (this.status.sex == 1) {
        return this.template.getModelName() + ".zombie.female" + WurmCalendar.getSpecialMapping(true);
      }
      return this.template.getModelName() + ".zombie" + WurmCalendar.getSpecialMapping(true);
    }
    if (this.template.isHorse)
    {
      String col = "grey";
      if (hasTrait(15)) {
        col = "brown";
      } else if (hasTrait(16)) {
        col = "gold";
      } else if (hasTrait(17)) {
        col = "black";
      } else if (hasTrait(18)) {
        col = "white";
      } else if (hasTrait(24)) {
        col = "piebaldPinto";
      } else if (hasTrait(25)) {
        col = "bloodBay";
      } else if (hasTrait(23)) {
        col = "ebonyBlack";
      }
      StringBuilder s = new StringBuilder();
      s.append(this.template.getModelName());
      s.append('.');
      s.append(col.toLowerCase());
      if (this.status.sex == 0) {
        s.append(".male");
      }
      if (this.status.sex == 1) {
        s.append(".female");
      }
      if (this.status.disease > 0) {
        s.append(".diseased");
      }
      s.append(WurmCalendar.getSpecialMapping(true));
      return s.toString();
    }
    if (this.template.isBlackOrWhite)
    {
      StringBuilder s = new StringBuilder();
      s.append(this.template.getModelName());
      if (this.status.sex == 0) {
        s.append(".male");
      }
      if (this.status.sex == 1) {
        s.append(".female");
      }
      if ((!hasTrait(15)) && (!hasTrait(16)) && (!hasTrait(18)) && 
        (!hasTrait(24)) && (!hasTrait(25)) && (!hasTrait(23))) {
        if (hasTrait(17)) {
          s.append(".black");
        }
      }
      if (this.status.disease > 0) {
        s.append(".diseased");
      }
      s.append(WurmCalendar.getSpecialMapping(true));
      return s.toString();
    }
    StringBuilder s = new StringBuilder();
    s.append(this.template.getModelName());
    if (this.status.sex == 0) {
      s.append(".male");
    }
    if (this.status.sex == 1) {
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
        s.append("diseased.");
      }
    }
    else
    {
      s.append('.');
      if (this.status.disease > 0) {
        s.append("diseased.");
      }
    }
    s.append(WurmCalendar.getSpecialMapping(false));
    return s.toString();
  }
  
  public String getHitSound()
  {
    return this.template.getHitSound(getSex());
  }
  
  public String getDeathSound()
  {
    return this.template.getDeathSound(getSex());
  }
  
  public final boolean hasNoServerSound()
  {
    return this.template.noServerSounds();
  }
  
  public void setStructure(@Nullable Structure struct)
  {
    if (struct == null) {
      this.status.setBuildingId(-10L);
    }
    this.structure = struct;
  }
  
  public float getNoticeChance()
  {
    if ((this.template.getTemplateId() == 29) || (this.template.getTemplateId() == 28) || 
      (this.template.getTemplateId() == 4)) {
      return 0.2F;
    }
    if (this.template.getTemplateId() == 5) {
      return 0.3F;
    }
    if ((this.template.getTemplateId() == 31) || (this.template.getTemplateId() == 30) || 
      (this.template.getTemplateId() == 6)) {
      return 0.4F;
    }
    if (this.template.getTemplateId() == 7) {
      return 0.6F;
    }
    if ((this.template.getTemplateId() == 33) || 
      (this.template.getTemplateId() == 32) || (this.template.getTemplateId() == 8)) {
      return 0.65F;
    }
    return 1.0F;
  }
  
  public Structure getStructure()
    throws NoSuchStructureException
  {
    if (this.structure == null) {
      throw new NoSuchStructureException("This creature has no structure");
    }
    return this.structure;
  }
  
  public boolean hasLink()
  {
    return false;
  }
  
  public short getCentimetersLong()
  {
    return this.status.getBody().getCentimetersLong();
  }
  
  public short getCentimetersHigh()
  {
    return this.status.getBody().getCentimetersHigh();
  }
  
  public short getCentimetersWide()
  {
    return this.status.getBody().getCentimetersWide();
  }
  
  public void setCentimetersLong(short centimetersLong)
  {
    this.status.getBody().setCentimetersLong(centimetersLong);
  }
  
  public void setCentimetersHigh(short centimetersHigh)
  {
    this.status.getBody().setCentimetersHigh(centimetersHigh);
  }
  
  public void setCentimetersWide(short centimetersWide)
  {
    this.status.getBody().setCentimetersWide(centimetersWide);
  }
  
  public float getWeight()
  {
    return this.status.getBody().getWeight(getStatus().fat);
  }
  
  public int getSize()
  {
    return this.template.getSize();
  }
  
  public boolean isClimber()
  {
    return this.template.climber;
  }
  
  public boolean addItemWatched(Item watched)
  {
    return true;
  }
  
  public boolean removeItemWatched(Item watched)
  {
    return true;
  }
  
  public boolean isItemWatched(Item watched)
  {
    return true;
  }
  
  public boolean isPaying()
  {
    return true;
  }
  
  public boolean isReallyPaying()
  {
    return true;
  }
  
  public int getPower()
  {
    return 0;
  }
  
  public void dropItem(Item item)
  {
    long parentId = item.getParentId();
    
    item.setPosXY(getPosX(), getPosY());
    if (parentId != -10L) {
      try
      {
        Item parent = Items.getItem(parentId);
        parent.dropItem(item.getWurmId(), false);
      }
      catch (Exception ex)
      {
        logger.log(Level.WARNING, ex.getMessage(), ex);
      }
    }
    int tilex = getTileX();
    int tiley = getTileY();
    try
    {
      Zone zone = Zones.getZone(tilex, tiley, isOnSurface());
      VolaTile t = zone.getOrCreateTile(tilex, tiley);
      if (t != null)
      {
        t.addItem(item, false, false);
      }
      else
      {
        int x = Server.rand.nextInt(Zones.worldTileSizeX);
        int y = Server.rand.nextInt(Zones.worldTileSizeY);
        t = Zones.getOrCreateTile(x, y, true);
        t.addItem(item, false, false);
      }
    }
    catch (NoSuchZoneException nsz)
    {
      logger.log(Level.WARNING, nsz.getMessage(), nsz);
    }
  }
  
  public void setDestroyed()
  {
    if (this.decisions != null)
    {
      this.decisions.clearOrders();
      this.decisions = null;
    }
    getStatus().setPath(null);
    try
    {
      savePosition(-10);
    }
    catch (IOException iox)
    {
      logger.log(Level.WARNING, iox.getMessage(), iox);
    }
    this.damageCounter = 0;
    this.status.dead = true;
    setLeader(null);
    if (this.followers != null) {
      stopLeading();
    }
    if (isTrading()) {
      getTrade().end(this, true);
    }
    setTarget(-10L, true);
    destroyVisionArea();
    if (isVehicle()) {
      Vehicles.destroyVehicle(getWurmId());
    }
  }
  
  public void destroy()
  {
    if (isDominated()) {
      setDominator(-10L);
    }
    getCurrentTile().deleteCreature(this);
    setDestroyed();
    if (getSpellEffects() != null) {
      getSpellEffects().destroy(false);
    }
    try
    {
      this.skills.delete();
    }
    catch (Exception ex)
    {
      logger.log(Level.INFO, "Error when deleting creature skills: " + ex.getMessage(), ex);
    }
    try
    {
      Item[] items = this.possessions.getInventory().getAllItems(true);
      for (int x = 0; x < items.length; x++) {
        if (!items[x].isUnique()) {
          Items.destroyItem(items[x].getWurmId());
        } else {
          dropItem(items[x]);
        }
      }
      Items.destroyItem(this.possessions.getInventory().getWurmId());
    }
    catch (Exception e)
    {
      logger.log(Level.INFO, "Error when decaying items: " + e.getMessage(), e);
    }
    try
    {
      Item[] items = getBody().getBodyItem().getAllItems(true);
      for (int x = 0; x < items.length; x++) {
        if (!items[x].isUnique()) {
          Items.destroyItem(items[x].getWurmId());
        } else {
          dropItem(items[x]);
        }
      }
      Items.destroyItem(getBody().getBodyItem().getWurmId());
    }
    catch (Exception e)
    {
      logger.log(Level.INFO, "Error when decaying body items: " + e.getMessage(), e);
    }
    if (this.citizenVillage != null)
    {
      Village vill = this.citizenVillage;
      Guard[] guards = this.citizenVillage.getGuards();
      Guard[] arrayOfGuard1 = guards;int i = arrayOfGuard1.length;
      for (Guard localGuard1 = 0; localGuard1 < i; localGuard1++)
      {
        lGuard = arrayOfGuard1[localGuard1];
        if (lGuard.getCreature() == this)
        {
          vill.deleteGuard(this, false);
          if (isSpiritGuard()) {
            vill.plan.destroyGuard(this);
          }
        }
      }
      Wagoner[] wagoners = vill.getWagoners();
      Wagoner[] arrayOfWagoner1 = wagoners;localGuard1 = arrayOfWagoner1.length;
      for (Guard lGuard = 0; lGuard < localGuard1; lGuard++)
      {
        Wagoner wagoner = arrayOfWagoner1[lGuard];
        if (wagoner.getWurmId() == getWurmId()) {
          vill.deleteWagoner(this);
        }
      }
    }
    if (isNpcTrader()) {
      if (Economy.getEconomy().getShop(this, true) != null)
      {
        if (Economy.getEconomy().getShop(this, true).getMoney() > 0L) {
          Economy.getEconomy().getKingsShop().setMoney(
            Economy.getEconomy().getKingsShop().getMoney() + 
            Economy.getEconomy().getShop(this, true).getMoney());
        }
        Economy.deleteShop(this.id);
      }
    }
    if (isKingdomGuard())
    {
      GuardTower tower = Kingdoms.getTower(this);
      if (tower != null) {
        try
        {
          tower.destroyGuard(this);
        }
        catch (IOException iox)
        {
          logger.log(Level.WARNING, iox.getMessage(), iox);
        }
      }
    }
    Creatures.getInstance().permanentlyDelete(this);
  }
  
  public boolean isBreakFence()
  {
    return this.template.isBreakFence();
  }
  
  public boolean isCareful()
  {
    return this.template.isCareful();
  }
  
  public final void attackTower()
  {
    if (isOnSurface()) {
      if (!isFriendlyKingdom(getCurrentKingdom())) {
        for (int x = Zones.safeTileX(getTileX() - 3); x < Zones.safeTileX(getTileX() + 3); x++) {
          for (int y = Zones.safeTileY(getTileY() - 3); y < Zones.safeTileY(getTileY() + 3); y++)
          {
            VolaTile t = Zones.getTileOrNull(x, y, isOnSurface());
            if (t != null)
            {
              Item[] items = t.getItems();
              for (Item i : items) {
                if (i.isGuardTower()) {
                  if (!isFriendlyKingdom(i.getKingdom()))
                  {
                    GuardTower tower = Kingdoms.getTower(i);
                    if (i.getCurrentQualityLevel() > 50.0F)
                    {
                      if (tower != null) {
                        tower.sendAttackWarning();
                      }
                      turnTowardsTile((short)i.getTileX(), (short)i.getTileY());
                      playAnimation("fight_strike", false);
                      Server.getInstance().broadCastAction(
                        getName() + " attacks the " + i.getName() + ".", this, 5);
                      
                      i.setDamage(i.getDamage() + (float)(getStrengthSkill() / 1000.0D));
                      if (Server.rand.nextInt(300) == 0)
                      {
                        if (Server.rand.nextBoolean()) {
                          ItemBehaviour.spawnCommander(i, i.getKingdom());
                        }
                        for (int n = 0; n < 2 + Server.rand.nextInt(4); n++) {
                          ItemBehaviour.spawnSoldier(i, i.getKingdom());
                        }
                      }
                    }
                    else if ((!Servers.localServer.HOMESERVER) && (Server.rand.nextInt(300) == 0))
                    {
                      if ((tower != null) && (!tower.hasLiveGuards()))
                      {
                        Server.getInstance().broadCastAction(
                          getName() + " conquers the " + tower.getName() + "!", this, 5);
                        Server.getInstance()
                          .broadCastSafe(getName() + " conquers " + tower.getName() + ".");
                        Kingdoms.convertTowersWithin(i.getTileX() - 10, i.getTileY() - 10, i
                          .getTileX() + 10, i.getTileY() + 10, getKingdomId());
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
  
  public void breakout()
  {
    if ((!isDominated()) && (((!isCaveDweller()) && (!isBreakFence())) || ((this.status.hunger >= 60000) || (isUnique())))) {
      if ((!isSubmerged()) && (Server.rand.nextInt(100) == 0))
      {
        Village breakoutVillage = Zones.getVillage(getTileX(), getTileY(), isOnSurface());
        if ((breakoutVillage != null) && (breakoutVillage.isPermanent)) {
          return;
        }
        if (isBreakFence()) {
          if (this.currentTile != null)
          {
            if (this.currentTile.getStructure() != null)
            {
              Wall[] walls = this.currentTile.getWallsForLevel(getFloorLevel());
              if (walls.length > 0)
              {
                Wall tobreak = walls[Server.rand.nextInt(walls.length)];
                if (!tobreak.isIndestructible())
                {
                  Server.getInstance().broadCastAction("The " + 
                    getName() + " smashes the " + tobreak.getName() + ".", this, 5);
                  if (isUnique()) {
                    tobreak.setDamage(tobreak.getDamage() + 100.0F);
                  } else {
                    tobreak.setDamage(tobreak.getDamage() + (float)getStrengthSkill() / 10.0F * tobreak
                      .getDamageModifier());
                  }
                }
              }
            }
            boolean onSurface = true;
            if (((isOnSurface()) || (this.currentTile.isTransition)) && (isUnique()))
            {
              VolaTile t = Zones.getTileOrNull(getTileX() + 1, getTileY(), true);
              if (t != null)
              {
                Wall[] walls = t.getWallsForLevel(Math.max(0, getFloorLevel()));
                if (walls.length > 0) {
                  for (Wall tobreak : walls) {
                    if (!tobreak.isIndestructible()) {
                      if ((tobreak.getTileX() == getTileX() + 1) && (!tobreak.isHorizontal()))
                      {
                        Server.getInstance().broadCastAction("The " + 
                          getName() + " smashes the " + tobreak.getName() + ".", this, 5);
                        if (isUnique()) {
                          tobreak.setDamage(tobreak.getDamage() + 100.0F);
                        } else {
                          tobreak.setDamage(tobreak.getDamage() + (float)getStrengthSkill() / 10.0F * tobreak
                            .getDamageModifier());
                        }
                      }
                    }
                  }
                }
              }
              t = Zones.getTileOrNull(getTileX() - 1, getTileY(), true);
              if (t != null)
              {
                Wall[] walls = t.getWallsForLevel(Math.max(0, getFloorLevel()));
                if (walls.length > 0) {
                  for (Wall tobreak : walls) {
                    if (!tobreak.isIndestructible()) {
                      if ((tobreak.getTileX() == getTileX()) && (!tobreak.isHorizontal()))
                      {
                        Server.getInstance().broadCastAction("The " + 
                          getName() + " smashes the " + tobreak.getName() + ".", this, 5);
                        if (isUnique()) {
                          tobreak.setDamage(tobreak.getDamage() + 100.0F);
                        } else {
                          tobreak.setDamage(tobreak.getDamage() + (float)getStrengthSkill() / 10.0F * tobreak
                            .getDamageModifier());
                        }
                      }
                    }
                  }
                }
              }
              t = Zones.getTileOrNull(getTileX(), getTileY() - 1, true);
              if (t != null)
              {
                Wall[] walls = t.getWallsForLevel(Math.max(0, getFloorLevel()));
                if (walls.length > 0) {
                  for (Wall tobreak : walls) {
                    if (!tobreak.isIndestructible()) {
                      if ((tobreak.getTileY() == getTileY()) && (tobreak.isHorizontal()))
                      {
                        Server.getInstance().broadCastAction("The " + 
                          getName() + " smashes the " + tobreak.getName() + ".", this, 5);
                        if (isUnique()) {
                          tobreak.setDamage(tobreak.getDamage() + 100.0F);
                        } else {
                          tobreak.setDamage(tobreak.getDamage() + (float)getStrengthSkill() / 10.0F * tobreak
                            .getDamageModifier());
                        }
                      }
                    }
                  }
                }
              }
              t = Zones.getTileOrNull(getTileX(), getTileY() + 1, true);
              if (t != null)
              {
                Wall[] walls = t.getWallsForLevel(Math.max(0, getFloorLevel()));
                if (walls.length > 0) {
                  for (Wall tobreak : walls) {
                    if (!tobreak.isIndestructible()) {
                      if ((tobreak.getTileY() == getTileY() + 1) && (tobreak.isHorizontal()))
                      {
                        Server.getInstance().broadCastAction("The " + 
                          getName() + " smashes the " + tobreak.getName() + ".", this, 5);
                        if (isUnique()) {
                          tobreak.setDamage(tobreak.getDamage() + 100.0F);
                        } else {
                          tobreak.setDamage(tobreak.getDamage() + (float)getStrengthSkill() / 10.0F * tobreak
                            .getDamageModifier());
                        }
                      }
                    }
                  }
                }
              }
            }
            Fence[] fences = this.currentTile.getFencesForLevel(this.currentTile.isTransition ? 0 : getFloorLevel());
            boolean onlyHoriz = false;
            boolean onlyVert = false;
            int currQl;
            int damage;
            if (fences == null)
            {
              if (isOnSurface())
              {
                if (fences == null)
                {
                  VolaTile t = Zones.getTileOrNull(this.currentTile.getTileX() + 1, this.currentTile.getTileY(), true);
                  if (t != null)
                  {
                    fences = t.getFencesForLevel(getFloorLevel());
                    onlyVert = true;
                  }
                }
                if (fences == null)
                {
                  VolaTile t = Zones.getTileOrNull(this.currentTile.getTileX(), this.currentTile.getTileY() + 1, true);
                  if (t != null)
                  {
                    fences = t.getFencesForLevel(getFloorLevel());
                    onlyHoriz = true;
                  }
                }
              }
              if (this.currentTile.isTransition)
              {
                if (!isOnSurface())
                {
                  VolaTile t = Zones.getTileOrNull(this.currentTile.getTileX(), this.currentTile.getTileY(), true);
                  if (t != null) {
                    fences = t.getFencesForLevel(Math.max(0, getFloorLevel()));
                  }
                  if (fences == null)
                  {
                    t = Zones.getTileOrNull(this.currentTile.getTileX() + 1, this.currentTile.getTileY(), true);
                    if (t != null)
                    {
                      fences = t.getFencesForLevel(Math.max(0, getFloorLevel()));
                      onlyVert = true;
                    }
                  }
                  if (fences == null)
                  {
                    t = Zones.getTileOrNull(this.currentTile.getTileX(), this.currentTile.getTileY() + 1, true);
                    if (t != null)
                    {
                      fences = t.getFencesForLevel(Math.max(0, getFloorLevel()));
                      onlyHoriz = true;
                    }
                  }
                }
                if (getFloorLevel() <= 0) {
                  if (Tiles.isMineDoor(Tiles.decodeType(Zones.getTileIntForTile(this.currentTile.tilex, this.currentTile.tiley, 0))))
                  {
                    currQl = Server.getWorldResource(this.currentTile.tilex, this.currentTile.tiley);
                    damage = 1000;
                    currQl = Math.max(0, currQl - 1000);
                    Server.setWorldResource(this.currentTile.tilex, this.currentTile.tiley, currQl);
                    try
                    {
                      MethodsStructure.sendDestroySound(this, getBody().getBodyPart(13), 
                        Tiles.decodeType(Server.surfaceMesh.getTile(this.currentTile.tilex, this.currentTile.tiley)) == 25);
                    }
                    catch (Exception ex)
                    {
                      logger.log(Level.INFO, getName() + ex.getMessage());
                    }
                    if (currQl == 0)
                    {
                      TileEvent.log(this.currentTile.tilex, this.currentTile.tiley, 0, getWurmId(), 174);
                      
                      TileEvent.log(this.currentTile.tilex, this.currentTile.tiley, -1, getWurmId(), 174);
                      if (Tiles.decodeType(Server.caveMesh.getTile(this.currentTile.tilex, this.currentTile.tiley)) == Tiles.Tile.TILE_CAVE_EXIT.id) {
                        Server.setSurfaceTile(this.currentTile.tilex, this.currentTile.tiley, 
                          Tiles.decodeHeight(Server.surfaceMesh.getTile(this.currentTile.tilex, this.currentTile.tiley)), Tiles.Tile.TILE_HOLE.id, (byte)0);
                      } else {
                        Server.setSurfaceTile(this.currentTile.tilex, this.currentTile.tiley, 
                          Tiles.decodeHeight(Server.surfaceMesh.getTile(this.currentTile.tilex, this.currentTile.tiley)), Tiles.Tile.TILE_ROCK.id, (byte)0);
                      }
                      Players.getInstance().sendChangedTile(this.currentTile.tilex, this.currentTile.tiley, true, true);
                      
                      MineDoorPermission.deleteMineDoor(this.currentTile.tilex, this.currentTile.tiley);
                      
                      Server.getInstance().broadCastAction(
                        getName() + " damages a door and the last parts fall down with a crash.", this, 5);
                    }
                    else
                    {
                      Server.getInstance().broadCastAction(getName() + " damages the door.", this, 5);
                    }
                  }
                }
              }
            }
            if (fences != null)
            {
              Fence[] arrayOfFence1 = fences;damage = arrayOfFence1.length;
              for (ex = 0; ex < damage; ex++)
              {
                Fence f = arrayOfFence1[ex];
                if (!f.isIndestructible()) {
                  if (f.isHorizontal())
                  {
                    if (!onlyVert)
                    {
                      Server.getInstance().broadCastAction("The " + 
                        getName() + " smashes the " + f.getName() + ".", this, 5);
                      if (isUnique())
                      {
                        f.setDamage(f.getDamage() + Server.rand.nextInt(100));
                      }
                      else
                      {
                        if (f.getVillage() != null) {
                          f.getVillage().addTarget(this);
                        }
                        f.setDamage(f.getDamage() + (float)getStrengthSkill() / 10.0F * f
                          .getDamageModifier());
                      }
                    }
                  }
                  else if (!onlyHoriz)
                  {
                    Server.getInstance().broadCastAction("The " + 
                      getName() + " smashes the " + f.getName() + ".", this, 5);
                    if (isUnique())
                    {
                      f.setDamage(f.getDamage() + Server.rand.nextInt(100));
                    }
                    else
                    {
                      if (f.getVillage() != null) {
                        f.getVillage().addTarget(this);
                      }
                      f.setDamage(f.getDamage() + (float)getStrengthSkill() / 10.0F * f
                        .getDamageModifier());
                    }
                  }
                }
              }
            }
          }
        }
        if (isUnique()) {
          if ((!isOnSurface()) && (Server.rand.nextInt(500) == 0))
          {
            boolean breakReinforcement = isUnique();
            int tx = Zones.safeTileX(getTileX() - 1);
            int ty = Zones.safeTileY(getTileY());
            int t = Zones.getTileIntForTile(tx, ty, 0);
            if (Tiles.isMineDoor(Tiles.decodeType(t)))
            {
              int currQl = Server.getWorldResource(tx, ty);
              try
              {
                MethodsStructure.sendDestroySound(this, getBody().getBodyPart(13), 
                  Tiles.decodeType(Server.surfaceMesh.getTile(tx, ty)) == 25);
                
                currQl = Math.max(0, currQl - 1000);
                Server.setWorldResource(tx, ty, currQl);
                if (currQl == 0)
                {
                  TileEvent.log(tx, ty, 0, getWurmId(), 174);
                  TileEvent.log(tx, ty, -1, getWurmId(), 174);
                  if (Tiles.decodeType(Server.caveMesh.getTile(tx, ty)) == Tiles.Tile.TILE_CAVE_EXIT.id) {
                    Server.setSurfaceTile(tx, ty, 
                      Tiles.decodeHeight(Server.surfaceMesh.getTile(tx, ty)), Tiles.Tile.TILE_HOLE.id, (byte)0);
                  } else {
                    Server.setSurfaceTile(tx, ty, 
                      Tiles.decodeHeight(Server.surfaceMesh.getTile(tx, ty)), Tiles.Tile.TILE_ROCK.id, (byte)0);
                  }
                  Players.getInstance().sendChangedTile(tx, ty, true, true);
                  MineDoorPermission.deleteMineDoor(tx, ty);
                  Server.getInstance().broadCastAction(getNameWithGenus() + " damages a door and the last parts fall down with a crash.", this, 5);
                }
              }
              catch (Exception ex)
              {
                logger.log(Level.WARNING, ex.getMessage());
              }
            }
            t = Zones.getTileIntForTile(tx, ty, -1);
            if ((breakReinforcement) && (Tiles.decodeType(t) == Tiles.Tile.TILE_CAVE_WALL_REINFORCED.id))
            {
              Server.caveMesh.setTile(tx, ty, 
                Tiles.encode(Tiles.decodeHeight(t), Tiles.Tile.TILE_CAVE_WALL.id, Tiles.decodeData(t)));
              Players.getInstance().sendChangedTile(tx, ty, false, true);
            }
            if (Tiles.decodeType(t) == Tiles.Tile.TILE_CAVE_WALL.id)
            {
              Village v = Zones.getVillage(tx, ty, true);
              if ((v == null) || (isOnPvPServer()) || (isUnique()))
              {
                TileRockBehaviour.createInsideTunnel(tx, ty, t, this, 145 + Server.rand.nextInt(3), 2, false, null);
                if (v != null) {
                  v.addTarget(this);
                }
              }
            }
            tx = Zones.safeTileX(getTileX());
            ty = Zones.safeTileY(getTileY() - 1);
            t = Zones.getTileIntForTile(tx, ty, -1);
            if ((breakReinforcement) && (Tiles.decodeType(t) == Tiles.Tile.TILE_CAVE_WALL_REINFORCED.id))
            {
              Server.caveMesh.setTile(tx, ty, 
                Tiles.encode(Tiles.decodeHeight(t), Tiles.Tile.TILE_CAVE_WALL.id, Tiles.decodeData(t)));
              Players.getInstance().sendChangedTile(tx, ty, false, true);
            }
            if (Tiles.decodeType(t) == Tiles.Tile.TILE_CAVE_WALL.id)
            {
              Village v = Zones.getVillage(tx, ty, true);
              if ((v == null) || (isOnPvPServer()) || (isUnique()))
              {
                TileRockBehaviour.createInsideTunnel(tx, ty, t, this, 145 + Server.rand.nextInt(3), 3, false, null);
                if (v != null) {
                  v.addTarget(this);
                }
              }
            }
            tx = Zones.safeTileX(getTileX() + 1);
            ty = Zones.safeTileY(getTileY());
            t = Zones.getTileIntForTile(tx, ty, -1);
            if ((breakReinforcement) && (Tiles.decodeType(t) == Tiles.Tile.TILE_CAVE_WALL_REINFORCED.id))
            {
              Server.caveMesh.setTile(tx, ty, 
                Tiles.encode(Tiles.decodeHeight(t), Tiles.Tile.TILE_CAVE_WALL.id, Tiles.decodeData(t)));
              Players.getInstance().sendChangedTile(tx, ty, false, true);
            }
            if (Tiles.decodeType(t) == Tiles.Tile.TILE_CAVE_WALL.id)
            {
              Village v = Zones.getVillage(tx, ty, true);
              if ((v == null) || (isOnPvPServer()) || (isUnique()))
              {
                TileRockBehaviour.createInsideTunnel(tx, ty, t, this, 145 + Server.rand.nextInt(3), 4, false, null);
                if (v != null) {
                  v.addTarget(this);
                }
              }
            }
            tx = Zones.safeTileX(getTileX());
            ty = Zones.safeTileY(getTileY() + 1);
            t = Zones.getTileIntForTile(tx, ty, -1);
            if ((breakReinforcement) && (Tiles.decodeType(t) == Tiles.Tile.TILE_CAVE_WALL_REINFORCED.id))
            {
              Server.caveMesh.setTile(tx, ty, 
                Tiles.encode(Tiles.decodeHeight(t), Tiles.Tile.TILE_CAVE_WALL.id, Tiles.decodeData(t)));
              Players.getInstance().sendChangedTile(tx, ty, false, true);
            }
            if (Tiles.decodeType(t) == Tiles.Tile.TILE_CAVE_WALL.id)
            {
              Village v = Zones.getVillage(tx, ty, true);
              if ((v == null) || (isOnPvPServer()) || (isUnique()))
              {
                TileRockBehaviour.createInsideTunnel(tx, ty, t, this, 145 + Server.rand.nextInt(3), 5, false, null);
                if (v != null) {
                  v.addTarget(this);
                }
              }
            }
          }
        }
      }
    }
  }
  
  public int getMaxHuntDistance()
  {
    if (isDominated()) {
      return 20;
    }
    return this.template.getMaxHuntDistance();
  }
  
  public Path findPath(int targetX, int targetY, @Nullable PathFinder pathfinder)
    throws NoPathException
  {
    Path path = null;
    PathFinder pf = pathfinder != null ? pathfinder : new PathFinder();
    setPathfindcounter(getPathfindCounter() + 1);
    if ((getPathfindCounter() < 10) || (this.target != -10L) || (getPower() > 0))
    {
      if ((isSpiritGuard()) && (this.citizenVillage != null))
      {
        if (this.target == -10L)
        {
          if (isWithinTileDistanceTo(targetX, targetY, (int)(this.status.getPositionZ() + getAltOffZ()) >> 2, 
            getMaxHuntDistance())) {
            path = pf.findPath(this, getTileX(), getTileY(), targetX, targetY, isOnSurface(), 10);
          }
        }
        else {
          try
          {
            path = pf.findPath(this, getTileX(), getTileY(), targetX, targetY, isOnSurface(), 10);
          }
          catch (NoPathException nsp)
          {
            if (this.currentVillage == this.citizenVillage)
            {
              if ((targetX < this.citizenVillage.getStartX() - 5) || (targetX > this.citizenVillage.getEndX() + 5) || 
                (targetY < this.citizenVillage.getStartY() - 5) || (targetY > this.citizenVillage.getEndY() + 5))
              {
                this.setTargetNOID = true;
              }
              else if (getTarget() != null)
              {
                this.creatureToBlinkTo = getTarget();
                
                return null;
              }
            }
            else if (getTarget() != null)
            {
              this.creatureToBlinkTo = getTarget();
              
              return null;
            }
          }
        }
      }
      else if (isWithinTileDistanceTo(targetX, targetY, (int)this.status.getPositionZ() >> 2, Math.max(getMaxHuntDistance(), this.template.getVision()))) {
        path = pf.findPath(this, getTileX(), getTileY(), targetX, targetY, isOnSurface(), 5);
      } else if ((isUnique()) || (isKingdomGuard()) || (isDominated()) || (this.template.isTowerBasher())) {
        if (this.target == -10L) {
          path = pf.findPath(this, getTileX(), getTileY(), targetX, targetY, isOnSurface(), 5);
        } else {
          this.setTargetNOID = true;
        }
      }
    }
    else {
      throw new NoPathException("No pathing now");
    }
    if (path != null) {
      setPathfindcounter(0);
    }
    return path;
  }
  
  public boolean isSentinel()
  {
    return this.template.isSentinel();
  }
  
  public boolean isNpc()
  {
    return false;
  }
  
  public boolean isTrader()
  {
    if (isReborn()) {
      return false;
    }
    if ((this.template.getTemplateId() == 1) && (!isPlayer())) {
      return false;
    }
    return this.template.isTrader();
  }
  
  public boolean canEat()
  {
    return getStatus().canEat();
  }
  
  public boolean isHungry()
  {
    return getStatus().isHungry();
  }
  
  public boolean isNeedFood()
  {
    return this.template.isNeedFood();
  }
  
  public boolean isMoveRandom()
  {
    return this.template.isMoveRandom();
  }
  
  public boolean isSwimming()
  {
    return this.template.isSwimming();
  }
  
  public boolean isAnimal()
  {
    return this.template.isAnimal();
  }
  
  public boolean isHuman()
  {
    return this.template.isHuman();
  }
  
  public boolean isRegenerating()
  {
    return (this.template.isRegenerating()) || (isUndead());
  }
  
  public boolean isDragon()
  {
    return this.template.isDragon();
  }
  
  public boolean isTypeFleeing()
  {
    return (isSpy()) || (this.template.isFleeing());
  }
  
  public boolean isMonster()
  {
    return this.template.isMonster();
  }
  
  public boolean isInvulnerable()
  {
    return this.template.isInvulnerable();
  }
  
  public boolean isNpcTrader()
  {
    return this.template.isNpcTrader();
  }
  
  public boolean isAggHuman()
  {
    if (isReborn()) {
      return true;
    }
    return this.template.isAggHuman();
  }
  
  public boolean isMoveLocal()
  {
    return (this.template.isMoveLocal()) && (this.status.modtype != 99);
  }
  
  public boolean isMoveGlobal()
  {
    boolean shouldMove = false;
    if (Features.Feature.CREATURE_MOVEMENT_CHANGES.isEnabled()) {
      if (getCurrentTile().getVillage() != null) {
        if (((isBred()) || (isBranded()) || (isCaredFor())) && (this.target == -10L)) {
          shouldMove = true;
        }
      }
    }
    return (this.template.isMoveGlobal()) || (this.status.modtype == 99) || (shouldMove);
  }
  
  public boolean shouldFlee()
  {
    if (Features.Feature.CREATURE_MOVEMENT_CHANGES.isEnabled())
    {
      if (getCurrentTile().getVillage() != null) {
        if ((isBred()) || (isBranded()) || (isCaredFor())) {
          return false;
        }
      }
      if (getStatus().isChampion()) {
        return false;
      }
      if (this.fleeCounter > 0)
      {
        Long[] visibleCreatures = getVisionArea().getSurface().getCreatures();
        for (Long lCret : visibleCreatures) {
          try
          {
            Creature cret = Server.getInstance().getCreature(lCret.longValue());
            if (((cret.getPower() == 0) || (Servers.localServer.testServer)) && (
              (cret.isPlayer()) || (cret.isAggHuman()) || (cret.isCarnivore()) || 
              (cret.isMonster()) || (cret.isHunter())))
            {
              float modifier = 1.0F;
              if ((getCurrentTile().getVillage() != null) && (cret.isPlayer())) {
                modifier = 2.0F;
              }
              sendToLoggers("checking if should flee from " + cret.getName() + ": " + (cret
                .getBaseCombatRating() - Math.abs(cret.getPos2f().distance(getPos2f()) / 4.0F)) + " vs " + 
                getBaseCombatRating() * modifier);
              if (cret.getBaseCombatRating() - Math.abs(cret.getPos2f().distance(getPos2f()) / 2.0F) > getBaseCombatRating() * modifier) {
                return true;
              }
            }
          }
          catch (NoSuchPlayerException|NoSuchCreatureException localNoSuchPlayerException) {}
        }
      }
      return false;
    }
    if (getStatus().isChampion()) {
      return false;
    }
    return this.fleeCounter > 0;
  }
  
  public boolean isGrazer()
  {
    return this.template.isGrazer();
  }
  
  public boolean isHerd()
  {
    return this.template.isHerd();
  }
  
  public boolean isHunter()
  {
    return this.template.isHunter();
  }
  
  public boolean isMilkable()
  {
    return (this.template.isMilkable()) && (getSex() == 1) && (getStatus().age >= 3);
  }
  
  public boolean isReborn()
  {
    return getStatus().reborn;
  }
  
  public boolean isDominatable(Creature aDominator)
  {
    if ((getLeader() != null) && (getLeader() != aDominator)) {
      return false;
    }
    if ((isRidden()) || (this.hitchedTo != null)) {
      return false;
    }
    return this.template.isDominatable();
  }
  
  public final int getAggressivity()
  {
    return this.template.getAggressivity();
  }
  
  final byte getCombatDamageType()
  {
    return this.template.getCombatDamageType();
  }
  
  final float getBreathDamage()
  {
    if (isUndead()) {
      return 10.0F;
    }
    if (isReborn()) {
      return Math.max(3.0F, this.template.getBreathDamage());
    }
    return this.template.getBreathDamage();
  }
  
  public float getHandDamage()
  {
    if (isUndead()) {
      return 5.0F;
    }
    if (isReborn()) {
      return Math.max(3.0F, this.template.getHandDamage());
    }
    return this.template.getHandDamage();
  }
  
  public float getBiteDamage()
  {
    if (isUndead()) {
      return 8.0F;
    }
    if (isReborn()) {
      return Math.max(5.0F, this.template.getBiteDamage());
    }
    return this.template.getBiteDamage();
  }
  
  public float getKickDamage()
  {
    if (isReborn()) {
      return Math.max(2.0F, this.template.getKickDamage());
    }
    return this.template.getKickDamage();
  }
  
  public float getHeadButtDamage()
  {
    if (isReborn()) {
      return Math.max(4.0F, this.template.getKickDamage());
    }
    return this.template.getHeadButtDamage();
  }
  
  public Logger getLogger()
  {
    return null;
  }
  
  public boolean isUnique()
  {
    return this.template.isUnique();
  }
  
  public boolean isKingdomGuard()
  {
    return this.template.isKingdomGuard();
  }
  
  public boolean isGuard()
  {
    return (isKingdomGuard()) || (isSpiritGuard()) || (isWarGuard());
  }
  
  public boolean isGhost()
  {
    return this.template.isGhost();
  }
  
  public boolean unDead()
  {
    return this.template.isUndead();
  }
  
  public final boolean onlyAttacksPlayers()
  {
    return this.template.onlyAttacksPlayers();
  }
  
  public boolean isSpiritGuard()
  {
    return this.template.isSpiritGuard();
  }
  
  public boolean isZombieSummoned()
  {
    return this.template.getTemplateId() == 69;
  }
  
  public boolean isBartender()
  {
    return this.template.isBartender();
  }
  
  public boolean isDefendKingdom()
  {
    return this.template.isDefendKingdom();
  }
  
  public boolean isNotFemale()
  {
    return getSex() != 1;
  }
  
  public boolean isAggWhitie()
  {
    return (this.template.isAggWhitie()) || (isReborn());
  }
  
  public boolean isHerbivore()
  {
    return this.template.isHerbivore();
  }
  
  public boolean isCarnivore()
  {
    return this.template.isCarnivore();
  }
  
  public boolean isOmnivore()
  {
    return this.template.isOmnivore();
  }
  
  public boolean isCaveDweller()
  {
    return this.template.isCaveDweller();
  }
  
  public boolean isSubmerged()
  {
    return this.template.isSubmerged();
  }
  
  public boolean isEggLayer()
  {
    return this.template.isEggLayer();
  }
  
  public int getEggTemplateId()
  {
    return this.template.getEggTemplateId();
  }
  
  public int getMaxGroupAttackSize()
  {
    if (isUnique()) {
      return 100;
    }
    float mod = getStatus().getBattleRatingTypeModifier();
    return (int)Math.max(this.template.getMaxGroupAttackSize(), this.template.getMaxGroupAttackSize() * mod);
  }
  
  public int getGroupSize()
  {
    int nums = 0;
    for (int x = Math.max(0, getCurrentTile().getTileX() - 3); x < Math.min(getCurrentTile().getTileX() + 3, Zones.worldTileSizeX - 1); x++) {
      for (int y = Math.max(0, getCurrentTile().getTileY() - 3); y < Math.min(getCurrentTile().getTileY() + 3, Zones.worldTileSizeY - 1); y++)
      {
        VolaTile t = Zones.getTileOrNull(x, y, isOnSurface());
        if (t != null) {
          if (t.getCreatures().length > 0)
          {
            Creature[] xret = t.getCreatures();
            for (Creature lElement : xret) {
              if ((lElement.getTemplate().getTemplateId() == this.template.getTemplateId()) || 
                (lElement.getTemplate().getTemplateId() == this.template.getLeaderTemplateId())) {
                nums++;
              }
            }
          }
        }
      }
    }
    return nums;
  }
  
  public final TilePos getAdjacentTilePos(TilePos pos)
  {
    switch (Server.rand.nextInt(8))
    {
    case 0: 
      return pos.East();
    case 1: 
      return pos.South();
    case 2: 
      return pos.West();
    case 3: 
      return pos.North();
    case 4: 
      return pos.NorthEast();
    case 5: 
      return pos.NorthWest();
    case 6: 
      return pos.SouthWest();
    case 7: 
      return pos.SouthEast();
    }
    return pos;
  }
  
  public void checkEggLaying()
  {
    if (isEggLayer()) {
      if (this.template.getTemplateId() == 53)
      {
        if (Server.rand.nextInt(7200) == 0) {
          if (WurmCalendar.isAfterEaster())
          {
            destroy();
            Server.getInstance().broadCastAction(getNameWithGenus() + " suddenly vanishes down into a hole!", this, 10);
          }
          else
          {
            try
            {
              Item egg = ItemFactory.createItem(466, 50.0F, null);
              egg.putItemInfrontof(this);
              Server.getInstance().broadCastAction(getNameWithGenus() + " throws something in the air!", this, 10);
            }
            catch (Exception ex)
            {
              logger.log(Level.WARNING, ex.getMessage(), ex);
            }
          }
        }
      }
      else if ((this.status.getSex() == 1) && (isNeedFood()) && (!canEat())) {
        if ((Items.mayLayEggs()) || (isUnique())) {
          if (Server.rand.nextInt('丠' * (isUnique() ? 1000 : 1)) == 0) {
            if (isOnSurface())
            {
              byte type = Tiles.decodeType(Server.surfaceMesh.getTile(getCurrentTile().tilex, 
                getCurrentTile().tiley));
              if ((type == Tiles.Tile.TILE_GRASS.id) || (type == Tiles.Tile.TILE_FIELD.id) || (type == Tiles.Tile.TILE_FIELD2.id) || (type == Tiles.Tile.TILE_DIRT.id) || (type == Tiles.Tile.TILE_DIRT_PACKED.id))
              {
                int templateId = 464;
                if (this.template.getSize() > 4) {
                  templateId = 465;
                }
                try
                {
                  Item egg = ItemFactory.createItem(templateId, 99.0F, getPosX(), getPosY(), this.status
                    .getRotation(), isOnSurface(), (byte)0, getStatus().getBridgeId(), null);
                  if ((templateId == 465) || (Server.rand.nextInt(5) == 0)) {
                    egg.setData1(this.template.getEggTemplateId());
                  }
                }
                catch (NoSuchTemplateException nst)
                {
                  logger.log(Level.WARNING, nst.getMessage(), nst);
                }
                catch (FailedException fe)
                {
                  logger.log(Level.WARNING, fe.getMessage(), fe);
                }
                this.status.hunger = 60000;
              }
            }
          }
        }
      }
    }
  }
  
  public boolean isNoSkillFor(Creature attacker)
  {
    if (((isKingdomGuard()) || (isSpiritGuard()) || (isZombieSummoned()) || ((isPlayer()) && (attacker.isPlayer())) || (isWarGuard())) && 
      (isFriendlyKingdom(attacker.getKingdomId()))) {
      return true;
    }
    if ((isPlayer()) && (attacker.isPlayer()))
    {
      if (Players.getInstance().isOverKilling(attacker.getWurmId(), getWurmId())) {
        return true;
      }
      if (((Player)this).getSaveFile().getIpaddress().equals(((Player)attacker).getSaveFile().getIpaddress())) {
        return true;
      }
    }
    return false;
  }
  
  public int[] forageForFood(VolaTile currTile)
  {
    int[] toReturn = { -1, -1 };
    if ((canEat()) && (isNeedFood())) {
      for (int x = -2; x <= 2; x++) {
        for (int y = -2; y <= 2; y++)
        {
          VolaTile t = Zones.getTileOrNull(Zones.safeTileX(currTile.getTileX() + x), 
            Zones.safeTileY(currTile.getTileY() + y), isOnSurface());
          if (t != null)
          {
            Item[] its = t.getItems();
            for (Item lIt : its) {
              if (lIt.isEdibleBy(this)) {
                if (Server.rand.nextInt(10) == 0)
                {
                  sendToLoggers("Found " + lIt.getName());
                  toReturn[0] = Zones.safeTileX(currTile.getTileX() + x);
                  toReturn[1] = Zones.safeTileY(currTile.getTileY() + y);
                  return toReturn;
                }
              }
            }
          }
          if ((isGrazer()) && (canEat()) && (Server.rand.nextInt(9) == 0))
          {
            byte type = Zones.getTextureForTile(Zones.safeTileX(currTile.getTileX() + x), 
              Zones.safeTileY(currTile.getTileY() + y), getLayer());
            if ((type == Tiles.Tile.TILE_GRASS.id) || (type == Tiles.Tile.TILE_FIELD.id) || (type == Tiles.Tile.TILE_FIELD2.id) || (type == Tiles.Tile.TILE_STEPPE.id) || (type == Tiles.Tile.TILE_ENCHANTED_GRASS.id))
            {
              sendToLoggers("Found grass or field");
              toReturn[0] = Zones.safeTileX(currTile.getTileX() + x);
              toReturn[1] = Zones.safeTileY(currTile.getTileY() + y);
              return toReturn;
            }
          }
        }
      }
    }
    return toReturn;
  }
  
  public void blinkTo(int tilex, int tiley, int layer, int floorLevel)
  {
    getCurrentTile().deleteCreatureQuick(this);
    try
    {
      setPositionX((tilex << 2) + 2);
      setPositionY((tiley << 2) + 2);
      setLayer(Math.min(0, layer), false);
      if (floorLevel > 0) {
        pushToFloorLevel(floorLevel);
      } else {
        setPositionZ(Zones.calculateHeight(getStatus().getPositionX(), getStatus().getPositionY(), isOnSurface()));
      }
      Zone z = Zones.getZone(tilex, tiley, layer >= 0);
      z.addCreature(getWurmId());
    }
    catch (Exception ex)
    {
      logger.log(Level.WARNING, getName() + " - " + tilex + ", " + tiley + ", " + layer + ", " + floorLevel + ": " + ex.getMessage(), ex);
    }
  }
  
  public final boolean isBeachDweller()
  {
    return this.template.isBeachDweller();
  }
  
  public final boolean isWoolProducer()
  {
    return this.template.isWoolProducer();
  }
  
  public boolean isTargetTileTooHigh(int targetX, int targetY, int currentTileNum, boolean swimming)
  {
    if (getFloorLevel() > 0) {
      return false;
    }
    if (isFlying()) {
      return false;
    }
    short currheight = Tiles.decodeHeight(currentTileNum);
    
    short[] lSteepness = getTileSteepness(targetX, targetY, isOnSurface());
    if ((swimming) && (lSteepness[0] < 65336) && (currheight > lSteepness[0]) && (!isFloating())) {
      return true;
    }
    if (isBeachDweller())
    {
      if ((currheight > 20) && (lSteepness[0] > currheight)) {
        return true;
      }
      if ((currheight < 0) && (lSteepness[0] > 0) && (!WurmCalendar.isNight())) {
        return true;
      }
    }
    if (isOnSurface())
    {
      VolaTile stile = Zones.getTileOrNull(targetX, targetY, isOnSurface());
      if (stile != null) {
        if ((stile.getStructure() != null) && (stile.getStructure().isTypeBridge())) {
          if (stile.getStructure().isHorizontal())
          {
            if ((stile.getStructure().getMaxX() == stile.getTileX()) || 
              (stile.getStructure().getMinX() == stile.getTileX())) {
              return false;
            }
          }
          else if ((stile.getStructure().getMaxY() == stile.getTileY()) || 
            (stile.getStructure().getMinY() == stile.getTileY())) {
            return false;
          }
        }
      }
    }
    if (currheight < 500) {
      return false;
    }
    if ((!swimming) && 
      (lSteepness[0] - currheight > 60.0D * Math.max(1.0D, getTileRange(this, targetX, targetY))) && (lSteepness[1] > 20))
    {
      if (Creatures.getInstance().isLog()) {
        logger.log(Level.INFO, 
        
          getName() + " Skipping moving up since avg steep=" + lSteepness[0] + "=" + (lSteepness[0] - currheight) + ">" + 60.0D * 
          
          Math.max(1.0D, getTileRange(this, targetX, targetY)) + " at " + targetX + "," + targetY + " from " + 
          
          getTileX() + ", " + getTileY());
      }
      return true;
    }
    if ((!swimming) && 
      (currheight - lSteepness[0] > 60.0D * Math.max(1.0D, getTileRange(this, targetX, targetY))) && (lSteepness[1] > 20))
    {
      if (Creatures.getInstance().isLog()) {
        logger.log(Level.INFO, 
        
          getName() + " Skipping moving down since avg steep=" + lSteepness[0] + "=" + (lSteepness[0] - currheight) + ">" + 60.0D * 
          
          Math.max(1.0D, getTileRange(this, targetX, targetY)) + " at " + targetX + "," + targetY + " from " + 
          
          getTileX() + ", " + getTileY());
      }
      return true;
    }
    return false;
  }
  
  public final long getBridgeId()
  {
    if (getStatus().getPosition() != null) {
      return getStatus().getPosition().getBridgeId();
    }
    return -10L;
  }
  
  public final boolean isWarGuard()
  {
    return this.template.isWarGuard();
  }
  
  public PathTile getMoveTarget(int seed)
  {
    if (getStatus() == null) {
      return null;
    }
    long now = System.currentTimeMillis();
    
    float lPosX = this.status.getPositionX();
    float lPosY = this.status.getPositionY();
    boolean hasTarget = false;
    int tilePosX = (int)lPosX >> 2;
    int tilePosY = (int)lPosY >> 2;
    int tx = tilePosX;
    int ty = tilePosY;
    try
    {
      Creature targ;
      if ((this.target == -10L) || ((this.fleeCounter > 0) && (this.target == -10L)))
      {
        boolean flee = false;
        int j;
        if ((isDominated()) && (this.fleeCounter <= 0))
        {
          if (hasOrders())
          {
            Order order = getFirstOrder();
            if (order.isTile())
            {
              boolean swimming = false;
              int ctile = isOnSurface() ? Server.surfaceMesh.getTile(tx, ty) : Server.caveMesh.getTile(tx, ty);
              if (Tiles.decodeHeight(ctile) <= 0) {
                swimming = true;
              }
              int tile = Zones.getTileIntForTile(order.getTileX(), order.getTileY(), getLayer());
              if ((!Tiles.isSolidCave(Tiles.decodeType(tile))) && (
                (Tiles.decodeHeight(tile) > 0) || (swimming))) {
                if (isOnSurface())
                {
                  if (!isTargetTileTooHigh(order.getTileX(), order.getTileY(), tile, swimming))
                  {
                    hasTarget = true;
                    tilePosX = order.getTileX();
                    tilePosY = order.getTileY();
                  }
                }
                else
                {
                  hasTarget = true;
                  tilePosX = order.getTileX();
                  tilePosY = order.getTileY();
                }
              }
            }
            else if (order.isCreature())
            {
              Creature lTarget = order.getCreature();
              if (lTarget != null) {
                if (lTarget.isDead())
                {
                  removeOrder(order);
                }
                else
                {
                  hasTarget = true;
                  tilePosX = lTarget.getCurrentTile().tilex;
                  tilePosY = lTarget.getCurrentTile().tiley;
                }
              }
            }
          }
        }
        else if ((isTypeFleeing()) || (shouldFlee()))
        {
          float[][] rangeHeatmap;
          Long[] visibleCreatures;
          float currentVal;
          if (Features.Feature.CREATURE_MOVEMENT_CHANGES.isEnabled())
          {
            if ((isOnSurface()) && (getVisionArea() != null) && (getVisionArea().getSurface() != null))
            {
              int heatmapSize = this.template.getVision() * 2 + 1;
              rangeHeatmap = new float[heatmapSize][heatmapSize];
              int j;
              for (int i = 0; i < heatmapSize; i++) {
                for (j = 0; j < heatmapSize; j++) {
                  rangeHeatmap[i][j] = -100.0F;
                }
              }
              visibleCreatures = getVisionArea().getSurface().getCreatures();
              for (Long lCret : visibleCreatures) {
                try
                {
                  Creature cret = Server.getInstance().getCreature(lCret.longValue());
                  float tileModifier = 0.0F;
                  
                  int diffX = (int)(cret.getPosX() - getPosX()) >> 2;
                  int diffY = (int)(cret.getPosY() - getPosY()) >> 2;
                  for (int i = 0; i < heatmapSize; i++) {
                    for (j = 0; j < heatmapSize; j++)
                    {
                      int deltaX = Math.abs(this.template.getVision() + diffX - i);
                      int deltaY = Math.abs(this.template.getVision() + diffY - j);
                      if (((cret.getPower() == 0) || (Servers.localServer.testServer)) && (
                        (cret.isPlayer()) || (cret.isAggHuman()) || (cret.isCarnivore()) || 
                        (cret.isMonster()) || (cret.isHunter())))
                      {
                        tileModifier = cret.getBaseCombatRating();
                        if ((cret.isBred()) || (cret.isBranded()) || (cret.isCaredFor())) {
                          tileModifier /= 3.0F;
                        }
                        if (cret.isDominated()) {
                          tileModifier /= 3.0F;
                        }
                        tileModifier -= Math.max(deltaX, deltaY);
                      }
                      else
                      {
                        tileModifier = 1.0F;
                      }
                      rangeHeatmap[i][j] += tileModifier;
                    }
                  }
                }
                catch (NoSuchPlayerException|NoSuchCreatureException localNoSuchPlayerException) {}
              }
              currentVal = rangeHeatmap[this.template.getVision()][this.template.getVision()];
              int currentValCount = 1;
              
              int currentTileHeight = Tiles.decodeHeight(Server.surfaceMesh.getTile(Zones.safeTileX(getTileX()), Zones.safeTileY(getTileY())));
              for (int y = 0; y < heatmapSize; y++) {
                for (int x = 0; x < heatmapSize; x++)
                {
                  int tileHeight = Tiles.decodeHeight(Server.surfaceMesh.getTile(Zones.safeTileX(getTileX() + x - this.template.getVision()), 
                    Zones.safeTileY(getTileY() + y - this.template.getVision())));
                  if ((!isSubmerged()) && (tileHeight < 0))
                  {
                    if (!isSwimming()) {
                      rangeHeatmap[x][y] += 100 + -tileHeight;
                    } else {
                      rangeHeatmap[x][y] += -tileHeight;
                    }
                  }
                  else if (tileHeight > 0) {
                    rangeHeatmap[x][y] += Math.abs(currentTileHeight - tileHeight) / 15;
                  }
                  float testVal = rangeHeatmap[x][y];
                  if (testVal == currentVal)
                  {
                    currentValCount++;
                  }
                  else if (testVal < currentVal)
                  {
                    currentValCount = 1;
                    currentVal = testVal;
                  }
                }
              }
              for (int y = 0; (y < heatmapSize) && (!flee); y++) {
                for (int x = 0; (x < heatmapSize) && (!flee); x++) {
                  if ((currentVal == rangeHeatmap[x][y]) && (Server.rand.nextInt((int)Math.max(1.0F, currentValCount * 0.75F)) == 0))
                  {
                    tilePosX = tx + x - this.template.getVision();
                    tilePosY = ty + y - this.template.getVision();
                    flee = true;
                  }
                }
              }
              if (!flee)
              {
                y = null;return y;
              }
            }
          }
          else if (isOnSurface())
          {
            Long[] crets;
            if (Server.rand.nextBoolean())
            {
              if ((getCurrentTile() != null) && (getCurrentTile().getVillage() != null))
              {
                crets = getVisionArea().getSurface().getCreatures();
                rangeHeatmap = crets;visibleCreatures = rangeHeatmap.length;
                for (currentVal = 0; currentVal < visibleCreatures; currentVal++)
                {
                  Object lCret = rangeHeatmap[currentVal];
                  try
                  {
                    Creature cret = Server.getInstance().getCreature(((Long)lCret).longValue());
                    if ((cret.getPower() == 0) && (
                      (cret.isPlayer()) || (cret.isAggHuman()) || (cret.isCarnivore()) || 
                      (cret.isMonster())))
                    {
                      if (cret.getPosX() > getPosX()) {
                        tilePosX -= Server.rand.nextInt(6);
                      } else {
                        tilePosX += Server.rand.nextInt(6);
                      }
                      if (cret.getPosY() > getPosY()) {
                        tilePosY -= Server.rand.nextInt(6);
                      } else {
                        tilePosY += Server.rand.nextInt(6);
                      }
                      flee = true;
                      break;
                    }
                  }
                  catch (Exception localException1) {}
                }
              }
            }
            else
            {
              crets = Players.getInstance().getPlayers();rangeHeatmap = crets.length;
              for (visibleCreatures = 0; visibleCreatures < rangeHeatmap; visibleCreatures++)
              {
                Player p = crets[visibleCreatures];
                if (((p.getPower() == 0) || (Servers.localServer.testServer)) && (p.getVisionArea() != null) && 
                  (p.getVisionArea().getSurface() != null) && 
                  (p.getVisionArea().getSurface().containsCreature(this)))
                {
                  if (p.getPosX() > getPosX()) {
                    tilePosX -= Server.rand.nextInt(6);
                  } else {
                    tilePosX += Server.rand.nextInt(6);
                  }
                  if (p.getPosY() > getPosY()) {
                    tilePosY -= Server.rand.nextInt(6);
                  } else {
                    tilePosY += Server.rand.nextInt(6);
                  }
                  flee = true;
                  break;
                }
              }
            }
          }
          if (isSpy())
          {
            int[] empty = { -1, -1 };
            
            int[] newarr = getSpySpot(empty);
            if ((newarr[0] > 0) && (newarr[1] > 0))
            {
              flee = true;
              tilePosX = newarr[0];
              tilePosY = newarr[1];
            }
          }
        }
        if ((isMoveLocal()) && (!flee) && (!hasTarget))
        {
          VolaTile currTile = getCurrentTile();
          if ((isUnique()) && (Server.rand.nextInt(10) == 0))
          {
            Den den = Dens.getDen(this.template.getTemplateId());
            if ((den != null) && ((den.getTilex() != tx) || (den.getTiley() != ty)))
            {
              tilePosX = den.getTilex();
              tilePosY = den.getTiley();
            }
          }
          else if (currTile != null)
          {
            int rand = Server.rand.nextInt(9);
            int tpx = currTile.getTileX() + 4 - rand;
            rand = Server.rand.nextInt(9);
            int tpy = currTile.getTileY() + 4 - rand;
            totx += currTile.getTileX() - tpx;
            toty += currTile.getTileY() - tpy;
            
            int[] foodSpot = forageForFood(currTile);
            boolean abort = false;
            if (Server.rand.nextBoolean()) {
              if (foodSpot[0] != -1)
              {
                tpx = foodSpot[0];
                tpy = foodSpot[1];
              }
              else if ((this.template.isTowerBasher()) && (Servers.localServer.PVPSERVER))
              {
                GuardTower closestTower = Kingdoms.getClosestEnemyTower(getTileX(), getTileY(), true, this);
                if (closestTower != null)
                {
                  tilePosX = closestTower.getTower().getTileX();
                  tilePosY = closestTower.getTower().getTileY();
                  abort = true;
                }
              }
              else if (isWarGuard())
              {
                tilePosX = Zones.safeTileX(tpx);
                tilePosY = Zones.safeTileY(tpy);
                if (!isOnSurface())
                {
                  int[] tiles = { tilePosX, tilePosY };
                  if (getCurrentTile().isTransition)
                  {
                    setLayer(0, true);
                  }
                  else
                  {
                    tiles = findRandomCaveExit(tiles);
                    if ((tiles[0] != tilePosX) && (tiles[1] != tilePosY))
                    {
                      tilePosX = tiles[0];
                      tilePosY = tiles[1];
                      abort = true;
                    }
                    else
                    {
                      setLayer(0, true);
                    }
                  }
                }
                else
                {
                  GuardTower gt = Kingdoms.getClosestTower(getTileX(), getTileY(), true);
                  boolean towerFound = false;
                  if ((gt != null) && (gt.getKingdom() == getKingdomId())) {
                    towerFound = true;
                  }
                  Item wtarget = Kingdoms.getClosestWarTarget(tx, ty, this);
                  if (wtarget != null) {
                    if ((!towerFound) || (getTileRange(this, wtarget.getTileX(), wtarget.getTileY()) < getTileRange(this, gt.getTower().getTileX(), gt.getTower().getTileY()))) {
                      if (!isWithinTileDistanceTo(wtarget.getTileX(), wtarget.getTileY(), wtarget
                        .getFloorLevel(), 15))
                      {
                        rand = Server.rand.nextInt(9);
                        tilePosX = Zones.safeTileX(wtarget.getTileX() + 4 - rand);
                        rand = Server.rand.nextInt(9);
                        tilePosY = Zones.safeTileY(wtarget.getTileY() + 4 - rand);
                        setTarget(-10L, true);
                        sendToLoggers("No target. Heading to my camp at " + tilePosX + "," + tilePosY);
                        abort = true;
                      }
                    }
                  }
                  if ((!abort) && (towerFound) && (!isWithinTileDistanceTo(gt.getTower().getTileX(), gt.getTower().getTileY(), gt
                    .getTower().getFloorLevel(), 15)))
                  {
                    rand = Server.rand.nextInt(9);
                    tilePosX = Zones.safeTileX(gt.getTower().getTileX() + 4 - rand);
                    rand = Server.rand.nextInt(9);
                    tilePosY = Zones.safeTileY(gt.getTower().getTileY() + 4 - rand);
                    setTarget(-10L, true);
                    sendToLoggers("No target. Heading to my tower at " + tilePosX + "," + tilePosY);
                    abort = true;
                  }
                }
              }
            }
            tpx = Zones.safeTileX(tpx);
            tpy = Zones.safeTileY(tpy);
            if (!abort)
            {
              VolaTile t = Zones.getOrCreateTile(tpx, tpy, isOnSurface());
              VolaTile myt = getCurrentTile();
              if ((!t.isGuarded()) || ((myt != null) && (myt.isGuarded()) && (!t.hasFire())))
              {
                boolean swimming = false;
                int ctile = isOnSurface() ? Server.surfaceMesh.getTile(tx, ty) : Server.caveMesh.getTile(tx, ty);
                if (Tiles.decodeHeight(ctile) <= 0) {
                  swimming = true;
                }
                int tile = Zones.getTileIntForTile(tpx, tpy, getLayer());
                if ((!Tiles.isSolidCave(Tiles.decodeType(tile))) && (
                  (Tiles.decodeHeight(tile) > 0) || (swimming))) {
                  if (isOnSurface())
                  {
                    boolean stepOnBridge = false;
                    if (Server.rand.nextInt(5) == 0) {
                      for (VolaTile stile : this.currentTile.getThisAndSurroundingTiles(1)) {
                        if ((stile.getStructure() != null) && (stile.getStructure().isTypeBridge())) {
                          if (stile.getStructure().isHorizontal())
                          {
                            if ((stile.getStructure().getMaxX() == stile.getTileX()) || 
                              (stile.getStructure().getMinX() == stile.getTileX())) {
                              if (getTileY() == stile.getTileY())
                              {
                                tilePosX = stile.getTileX();
                                tilePosY = stile.getTileY();
                                stepOnBridge = true;
                                break;
                              }
                            }
                          }
                          else if ((stile.getStructure().getMaxY() == stile.getTileY()) || 
                            (stile.getStructure().getMinY() == stile.getTileY())) {
                            if (getTileX() == stile.getTileX())
                            {
                              tilePosX = stile.getTileX();
                              tilePosY = stile.getTileY();
                              stepOnBridge = true;
                              break;
                            }
                          }
                        }
                      }
                    }
                    if (!stepOnBridge) {
                      if (!isTargetTileTooHigh(tpx, tpy, tile, swimming)) {
                        if ((t == null) || (t.getCreatures().length < 3))
                        {
                          tilePosX = tpx;
                          tilePosY = tpy;
                        }
                      }
                    }
                  }
                  else if ((t == null) || (t.getCreatures().length < 3))
                  {
                    tilePosX = tpx;
                    tilePosY = tpy;
                  }
                }
              }
            }
          }
        }
        else if ((isSpiritGuard()) && (!hasTarget))
        {
          if (this.citizenVillage != null)
          {
            int[] tiles = { tilePosX, tilePosY };
            if (!isOnSurface())
            {
              if (getCurrentTile().isTransition)
              {
                setLayer(0, true);
              }
              else
              {
                tiles = findRandomCaveExit(tiles);
                tilePosX = tiles[0];
                tilePosY = tiles[1];
                if ((tilePosX == tx) || (tilePosY == ty)) {}
              }
            }
            else
            {
              int x = this.citizenVillage.startx + Server.rand.nextInt(this.citizenVillage.endx - this.citizenVillage.startx);
              
              int y = this.citizenVillage.starty + Server.rand.nextInt(this.citizenVillage.endy - this.citizenVillage.starty);
              VolaTile t = Zones.getTileOrNull(x, y, isOnSurface());
              if (t != null)
              {
                if (t.getStructure() == null)
                {
                  tilePosX = x;
                  tilePosY = y;
                }
              }
              else
              {
                tilePosX = x;
                tilePosY = y;
              }
            }
          }
          else
          {
            VolaTile currTile = getCurrentTile();
            if (currTile != null)
            {
              int rand = Server.rand.nextInt(5);
              int tpx = currTile.getTileX() + 2 - rand;
              rand = Server.rand.nextInt(5);
              int tpy = currTile.getTileY() + 2 - rand;
              VolaTile t = Zones.getTileOrNull(tilePosX, tilePosY, isOnSurface());
              tpx = Zones.safeTileX(tpx);
              tpy = Zones.safeTileY(tpy);
              if (t == null)
              {
                tilePosX = tpx;
                tilePosY = tpy;
              }
            }
            else if (!isDead())
            {
              currTile = Zones.getOrCreateTile(tilePosX, tilePosY, isOnSurface());
              logger.log(Level.WARNING, getName() + " stuck on no tile at " + getTileX() + "," + getTileY() + "," + 
                isOnSurface());
            }
          }
        }
        else if ((isKingdomGuard()) && (!hasTarget))
        {
          int[] tiles = { tilePosX, tilePosY };
          if (!isOnSurface())
          {
            tiles = findRandomCaveExit(tiles);
            tilePosX = tiles[0];
            tilePosY = tiles[1];
            if ((tilePosX != tx) && (tilePosY != ty)) {
              hasTarget = true;
            }
          }
          if ((!hasTarget) && (Server.rand.nextInt(40) == 0))
          {
            GuardTower gt = Kingdoms.getTower(this);
            if (gt != null)
            {
              int tpx = gt.getTower().getTileX();
              int tpy = gt.getTower().getTileY();
              tilePosX = tpx;
              tilePosY = tpy;
              hasTarget = true;
            }
          }
          if (!hasTarget)
          {
            VolaTile currTile = getCurrentTile();
            int rand = Server.rand.nextInt(5);
            int tpx = Zones.safeTileX(currTile.getTileX() + 2 - rand);
            rand = Server.rand.nextInt(5);
            int tpy = Zones.safeTileY(currTile.getTileY() + 2 - rand);
            VolaTile t = Zones.getOrCreateTile(tpx, tpy, isOnSurface());
            if ((t.getKingdom() == getKingdomId()) || (currTile.getKingdom() != getKingdomId())) {
              if (t.getStructure() == null)
              {
                tilePosX = tpx;
                tilePosY = tpy;
              }
            }
          }
        }
        if ((!isCaveDweller()) && (!isOnSurface()) && (getCurrentTile().isTransition) && (tilePosX == tx) && (tilePosY == ty)) {
          if ((!Tiles.isMineDoor(Tiles.decodeType(Server.surfaceMesh.getTile(tx, ty)))) || 
            (MineDoorPermission.getPermission(tx, ty).mayPass(this))) {
            setLayer(0, true);
          }
        }
      }
      else if (this.target != -10L)
      {
        targ = getTarget();
        if (targ != null)
        {
          if ((targ.getCultist() != null) && (targ.getCultist().hasFearEffect())) {
            setTarget(-10L, true);
          }
          currTile = targ.getCurrentTile();
          if (currTile != null)
          {
            tilePosX = currTile.tilex;
            tilePosY = currTile.tiley;
            if (seed == 100)
            {
              tilePosX = currTile.tilex - 1 + Server.rand.nextInt(3);
              tilePosY = currTile.tiley - 1 + Server.rand.nextInt(3);
            }
            if ((isSpellCaster()) || (isSummoner()))
            {
              tilePosX = Server.rand.nextBoolean() ? currTile.tilex - (Server.rand.nextBoolean() ? 0 : 5) : currTile.tilex + (Server.rand.nextBoolean() ? 0 : 5);
              tilePosY = Server.rand.nextBoolean() ? currTile.tiley - (Server.rand.nextBoolean() ? 0 : 5) : currTile.tiley + (Server.rand.nextBoolean() ? 0 : 5);
            }
            int targGroup = targ.getGroupSize();
            int myGroup = getGroupSize();
            if (isOnSurface() != currTile.isOnSurface())
            {
              boolean changeLayer = false;
              if (getCurrentTile().isTransition) {
                changeLayer = true;
              }
              if (isSpiritGuard())
              {
                if (this.currentVillage == this.citizenVillage)
                {
                  if (this.citizenVillage != null)
                  {
                    if ((currTile.getTileX() < this.citizenVillage.getStartX() - 5) || 
                      (currTile.getTileX() > this.citizenVillage.getEndX() + 5) || 
                      (currTile.getTileY() < this.citizenVillage.getStartY() - 5) || 
                      (currTile.getTileY() > this.citizenVillage.getEndY() + 5))
                    {
                      if (this.citizenVillage.isOnSurface() == isOnSurface()) {
                        try
                        {
                          changeLayer = false;
                          tilePosX = this.citizenVillage.getToken().getTileX();
                          tilePosY = this.citizenVillage.getToken().getTileY();
                        }
                        catch (NoSuchItemException nsi)
                        {
                          logger.log(Level.WARNING, getName() + " no token for village " + this.citizenVillage);
                        }
                      }
                      setTarget(-10L, true);
                    }
                    else
                    {
                      blinkTo(tilePosX, tilePosY, targ.getLayer(), targ.getFloorLevel());
                      nsi = null;return nsi;
                    }
                  }
                  else {
                    setTarget(-10L, true);
                  }
                }
                else if (this.citizenVillage != null)
                {
                  int[] tiles;
                  if ((currTile.getTileX() < this.citizenVillage.getStartX() - 5) || 
                    (currTile.getTileX() > this.citizenVillage.getEndX() + 5) || 
                    (currTile.getTileY() < this.citizenVillage.getStartY() - 5) || 
                    (currTile.getTileY() > this.citizenVillage.getEndY() + 5))
                  {
                    if (this.citizenVillage.isOnSurface() == isOnSurface())
                    {
                      try
                      {
                        tilePosX = this.citizenVillage.getToken().getTileX();
                        tilePosY = this.citizenVillage.getToken().getTileY();
                        changeLayer = false;
                      }
                      catch (NoSuchItemException nsi)
                      {
                        logger.log(Level.WARNING, getName() + " no token for village " + this.citizenVillage);
                      }
                    }
                    else if (!changeLayer)
                    {
                      tiles = new int[] { tilePosX, tilePosY };
                      if (isOnSurface()) {
                        tiles = findRandomCaveEntrance(tiles);
                      } else {
                        tiles = findRandomCaveExit(tiles);
                      }
                      tilePosX = tiles[0];
                      tilePosY = tiles[1];
                    }
                    setTarget(-10L, true);
                  }
                  else
                  {
                    blinkTo(tilePosX, tilePosY, targ.getLayer(), 0);
                    tiles = null;return tiles;
                  }
                }
                else
                {
                  setTarget(-10L, true);
                }
              }
              else if (isUnique())
              {
                Den den = Dens.getDen(this.template.getTemplateId());
                if (den != null)
                {
                  tilePosX = den.getTilex();
                  tilePosY = den.getTiley();
                  if (!changeLayer)
                  {
                    int[] tiles = { tilePosX, tilePosY };
                    if (!isOnSurface()) {
                      tiles = findRandomCaveExit(tiles);
                    }
                    tilePosX = tiles[0];
                    tilePosY = tiles[1];
                  }
                  setTarget(-10L, true);
                }
                else if (!isOnSurface())
                {
                  if (!changeLayer)
                  {
                    int[] tiles = { tilePosX, tilePosY };
                    
                    tiles = findRandomCaveExit(tiles);
                    
                    tilePosX = tiles[0];
                    tilePosY = tiles[1];
                  }
                }
              }
              else if (isKingdomGuard())
              {
                if (getCurrentKingdom() == getKingdomId())
                {
                  if (isWithinTileDistanceTo(currTile.getTileX(), currTile.getTileY(), 
                    (int)targ.getPositionZ(), this.template.getMaxHuntDistance()))
                  {
                    if (!changeLayer)
                    {
                      int[] tiles = { tilePosX, tilePosY };
                      if (isOnSurface()) {
                        tiles = findRandomCaveEntrance(tiles);
                      } else {
                        tiles = findRandomCaveExit(tiles);
                      }
                      tilePosX = tiles[0];
                      tilePosY = tiles[1];
                    }
                  }
                  else {
                    setTarget(-10L, true);
                  }
                }
                else
                {
                  changeLayer = false;
                  setTarget(-10L, true);
                }
              }
              else if (getSize() > 3)
              {
                changeLayer = false;
                setTarget(-10L, true);
              }
              else
              {
                VolaTile t = getCurrentTile();
                if (((isAggHuman()) || (isHunter()) || (isDominated())) && 
                  ((!currTile.isGuarded()) || ((t != null) && (t.isGuarded()))) && 
                  (isWithinTileDistanceTo(currTile.getTileX(), currTile.getTileY(), 
                  (int)targ.getPositionZ(), this.template.getMaxHuntDistance())))
                {
                  if (!changeLayer)
                  {
                    int[] tiles = { tilePosX, tilePosY };
                    if (isOnSurface()) {
                      tiles = findRandomCaveEntrance(tiles);
                    } else {
                      tiles = findRandomCaveExit(tiles);
                    }
                    tilePosX = tiles[0];
                    tilePosY = tiles[1];
                  }
                }
                else {
                  setTarget(-10L, true);
                }
              }
              if (changeLayer) {
                if ((!Tiles.isMineDoor(Tiles.decodeType(Server.surfaceMesh.getTile(tx, ty)))) || 
                  (MineDoorPermission.getPermission(tx, ty).mayPass(this))) {
                  setLayer(isOnSurface() ? -1 : 0, true);
                }
              }
            }
            else if (isSpiritGuard())
            {
              if (this.currentVillage == this.citizenVillage)
              {
                if (this.citizenVillage != null)
                {
                  tilePosX = currTile.getTileX();
                  tilePosY = currTile.getTileY();
                  if ((targ.getCultist() != null) && (targ.getCultist().hasFearEffect()))
                  {
                    tilePosX = this.citizenVillage.getStartX() - 5 + Server.rand.nextInt(this.citizenVillage.getDiameterX() + 10);
                    
                    tilePosY = this.citizenVillage.getStartY() - 5 + Server.rand.nextInt(this.citizenVillage.getDiameterY() + 10);
                  }
                  else if ((currTile.getTileX() < this.citizenVillage.getStartX() - 5) || 
                    (currTile.getTileX() > this.citizenVillage.getEndX() + 5) || 
                    (currTile.getTileY() < this.citizenVillage.getStartY() - 5) || 
                    (currTile.getTileY() > this.citizenVillage.getEndY() + 5))
                  {
                    try
                    {
                      tilePosX = this.citizenVillage.getToken().getTileX();
                      tilePosY = this.citizenVillage.getToken().getTileY();
                    }
                    catch (NoSuchItemException nsi)
                    {
                      logger.log(Level.WARNING, getName() + " no token for village " + this.citizenVillage);
                    }
                    setTarget(-10L, true);
                  }
                  else
                  {
                    this.citizenVillage.cryForHelp(this, false);
                  }
                }
                else if (isWithinTileDistanceTo(currTile.getTileX(), currTile.getTileY(), 
                  (int)targ.getPositionZ(), this.template.getMaxHuntDistance()))
                {
                  logger.log(Level.WARNING, "Why does this happen to a " + getName() + " at " + 
                    getCurrentTile().tilex + ", " + getCurrentTile().tiley);
                  tilePosX = currTile.getTileX();
                  tilePosY = currTile.getTileY();
                }
                else
                {
                  setTarget(-10L, true);
                }
              }
              else if (this.citizenVillage != null)
              {
                tilePosX = currTile.getTileX();
                tilePosY = currTile.getTileY();
                if ((currTile.getTileX() < this.citizenVillage.getStartX() - 5) || 
                  (currTile.getTileX() > this.citizenVillage.getEndX() + 5) || 
                  (currTile.getTileY() < this.citizenVillage.getStartY() - 5) || 
                  (currTile.getTileY() > this.citizenVillage.getEndY() + 5))
                {
                  try
                  {
                    tilePosX = this.citizenVillage.getToken().getTileX();
                    tilePosY = this.citizenVillage.getToken().getTileY();
                  }
                  catch (NoSuchItemException nsi)
                  {
                    logger.log(Level.WARNING, getName() + " no token for village " + this.citizenVillage);
                  }
                  setTarget(-10L, true);
                }
                else
                {
                  this.citizenVillage.cryForHelp(this, true);
                }
              }
              else
              {
                if (isWithinTileDistanceTo(currTile.getTileX(), currTile.getTileY(), 
                  (int)targ.getPositionZ(), this.template.getMaxHuntDistance()))
                {
                  if (Server.rand.nextInt(100) != 0)
                  {
                    tilePosX = currTile.getTileX();
                    tilePosY = currTile.getTileY();
                  }
                  else
                  {
                    setTarget(-10L, true);
                  }
                }
                else {
                  setTarget(-10L, true);
                }
                logger.log(Level.WARNING, getName() + " no citizen village.");
              }
            }
            else if (isUnique())
            {
              Den den = Dens.getDen(this.template.getTemplateId());
              if (den != null)
              {
                if ((Math.abs(currTile.getTileX() - den.getTilex()) > this.template.getVision()) || 
                  (Math.abs(currTile.getTileY() - den.getTiley()) > this.template.getVision())) {
                  if (Server.rand.nextInt(10) == 0)
                  {
                    if (!isFighting())
                    {
                      setTarget(-10L, true);
                      tilePosX = den.getTilex();
                      tilePosY = den.getTiley();
                    }
                  }
                  else if (isWithinTileDistanceTo(currTile.getTileX(), currTile.getTileY(), 
                    (int)targ.getPositionZ(), this.template.getMaxHuntDistance()))
                  {
                    tilePosX = currTile.getTileX();
                    tilePosY = currTile.getTileY();
                    if ((getSize() < 5) && (targ.getBridgeId() != -10L) && 
                      (getBridgeId() < 0L))
                    {
                      int[] tiles = findBestBridgeEntrance(targ.getTileX(), targ
                        .getTileY(), targ.getLayer(), targ
                        .getBridgeId());
                      if (tiles[0] > 0)
                      {
                        tilePosX = tiles[0];
                        tilePosY = tiles[1];
                        if ((getTileX() == tilePosX) && (getTileY() == tilePosY))
                        {
                          tilePosX = currTile.tilex;
                          tilePosY = currTile.tiley;
                        }
                      }
                    }
                    else if (getBridgeId() != targ.getBridgeId())
                    {
                      int[] tiles = findBestBridgeEntrance(targ.getTileX(), targ
                        .getTileY(), targ.getLayer(), getBridgeId());
                      if (tiles[0] > 0)
                      {
                        tilePosX = tiles[0];
                        tilePosY = tiles[1];
                        if ((getTileX() == tilePosX) && (getTileY() == tilePosY))
                        {
                          tilePosX = currTile.tilex;
                          tilePosY = currTile.tiley;
                        }
                      }
                    }
                    if (seed == 100)
                    {
                      tilePosX = currTile.tilex - 1 + Server.rand.nextInt(3);
                      tilePosY = currTile.tiley - 1 + Server.rand.nextInt(3);
                    }
                  }
                  else if (!isFighting())
                  {
                    setTarget(-10L, true);
                  }
                }
              }
              else if (isWithinTileDistanceTo(currTile.getTileX(), currTile.getTileY(), 
                (int)targ.getPositionZ(), this.template.getMaxHuntDistance()))
              {
                if (seed == 100)
                {
                  tilePosX = currTile.tilex - 1 + Server.rand.nextInt(3);
                  tilePosY = currTile.tiley - 1 + Server.rand.nextInt(3);
                }
                else
                {
                  tilePosX = currTile.getTileX();
                  tilePosY = currTile.getTileY();
                  if ((getSize() < 5) && (targ.getBridgeId() != -10L) && 
                    (getBridgeId() < 0L))
                  {
                    int[] tiles = findBestBridgeEntrance(targ.getTileX(), targ
                      .getTileY(), targ.getLayer(), targ
                      .getBridgeId());
                    if (tiles[0] > 0)
                    {
                      tilePosX = tiles[0];
                      tilePosY = tiles[1];
                      if ((getTileX() == tilePosX) && (getTileY() == tilePosY))
                      {
                        tilePosX = currTile.tilex;
                        tilePosY = currTile.tiley;
                      }
                    }
                  }
                  else if (getBridgeId() != targ.getBridgeId())
                  {
                    int[] tiles = findBestBridgeEntrance(targ.getTileX(), targ
                      .getTileY(), targ.getLayer(), getBridgeId());
                    if (tiles[0] > 0)
                    {
                      tilePosX = tiles[0];
                      tilePosY = tiles[1];
                      if ((getTileX() == tilePosX) && (getTileY() == tilePosY))
                      {
                        tilePosX = currTile.tilex;
                        tilePosY = currTile.tiley;
                      }
                    }
                  }
                }
              }
              else if (!isFighting()) {
                setTarget(-10L, true);
              }
            }
            else if (isKingdomGuard())
            {
              if (getCurrentKingdom() == getKingdomId())
              {
                if (isWithinTileDistanceTo(currTile.getTileX(), currTile.getTileY(), 
                  (int)targ.getPositionZ(), this.template.getMaxHuntDistance()))
                {
                  GuardTower gt = Kingdoms.getTower(this);
                  if (gt != null)
                  {
                    int tpx = gt.getTower().getTileX();
                    int tpy = gt.getTower().getTileY();
                    if ((targGroup < myGroup * getMaxGroupAttackSize()) && 
                      (targ.isWithinTileDistanceTo(tpx, tpy, (int)gt.getTower().getPosZ(), 50)))
                    {
                      if ((targ.getCultist() != null) && (targ.getCultist().hasFearEffect()))
                      {
                        if (Server.rand.nextBoolean()) {
                          tilePosX = Math.max(currTile.getTileX() + 10, getTileX());
                        } else {
                          tilePosX = Math.min(currTile.getTileX() - 10, getTileX());
                        }
                        if (Server.rand.nextBoolean()) {
                          tilePosX = Math.max(currTile.getTileY() + 10, getTileY());
                        } else {
                          tilePosX = Math.min(currTile.getTileY() - 10, getTileY());
                        }
                      }
                      else if (seed == 100)
                      {
                        tilePosX = currTile.tilex - 1 + Server.rand.nextInt(3);
                        tilePosY = currTile.tiley - 1 + Server.rand.nextInt(3);
                      }
                      else
                      {
                        tilePosX = currTile.getTileX();
                        tilePosY = currTile.getTileY();
                        if ((targ.getBridgeId() != -10L) && (getBridgeId() < 0L))
                        {
                          int[] tiles = findBestBridgeEntrance(targ.getTileX(), targ
                            .getTileY(), targ.getLayer(), targ
                            .getBridgeId());
                          if (tiles[0] > 0)
                          {
                            tilePosX = tiles[0];
                            tilePosY = tiles[1];
                            if ((getTileX() == tilePosX) && (getTileY() == tilePosY))
                            {
                              tilePosX = currTile.tilex;
                              tilePosY = currTile.tiley;
                            }
                          }
                        }
                        else if (getBridgeId() != targ.getBridgeId())
                        {
                          int[] tiles = findBestBridgeEntrance(targ.getTileX(), targ
                            .getTileY(), targ.getLayer(), getBridgeId());
                          if (tiles[0] > 0)
                          {
                            tilePosX = tiles[0];
                            tilePosY = tiles[1];
                            if ((getTileX() == tilePosX) && (getTileY() == tilePosY))
                            {
                              tilePosX = currTile.tilex;
                              tilePosY = currTile.tiley;
                            }
                          }
                        }
                      }
                    }
                    else
                    {
                      tilePosX = tpx;
                      tilePosY = tpy;
                      setTarget(-10L, true);
                    }
                  }
                  else if (seed == 100)
                  {
                    tilePosX = currTile.tilex - 1 + Server.rand.nextInt(3);
                    tilePosY = currTile.tiley - 1 + Server.rand.nextInt(3);
                  }
                  else
                  {
                    tilePosX = currTile.getTileX();
                    tilePosY = currTile.getTileY();
                  }
                }
                else
                {
                  setTarget(-10L, true);
                }
              }
              else {
                setTarget(-10L, true);
              }
            }
            else if ((targ.getCultist() != null) && (targ.getCultist().hasFearEffect()))
            {
              if (Server.rand.nextBoolean()) {
                tilePosX = Math.max(currTile.getTileX() + 10, getTileX());
              } else {
                tilePosX = Math.min(currTile.getTileX() - 10, getTileX());
              }
              if (Server.rand.nextBoolean()) {
                tilePosX = Math.max(currTile.getTileY() + 10, getTileY());
              } else {
                tilePosX = Math.min(currTile.getTileY() - 10, getTileY());
              }
            }
            else
            {
              boolean abort = false;
              boolean towerFound = false;
              if (isWarGuard())
              {
                GuardTower gt = Kingdoms.getClosestTower(getTileX(), getTileY(), true);
                if ((gt != null) && (gt.getKingdom() == getKingdomId())) {
                  towerFound = true;
                }
                Item wtarget = Kingdoms.getClosestWarTarget(tx, ty, this);
                if (wtarget != null) {
                  if ((!towerFound) || (getTileRange(this, wtarget.getTileX(), wtarget.getTileY()) < getTileRange(this, gt.getTower().getTileX(), gt.getTower().getTileY()))) {
                    if (!isWithinTileDistanceTo(wtarget.getTileX(), wtarget.getTileY(), wtarget
                      .getFloorLevel(), 15))
                    {
                      int rand = Server.rand.nextInt(9);
                      tilePosX = Zones.safeTileX(wtarget.getTileX() + 4 - rand);
                      rand = Server.rand.nextInt(9);
                      tilePosY = Zones.safeTileY(wtarget.getTileY() + 4 - rand);
                      abort = true;
                      setTarget(-10L, true);
                      sendToLoggers("Heading to my camp at " + tilePosX + "," + tilePosY);
                    }
                  }
                }
                if ((!abort) && (towerFound) && (!isWithinTileDistanceTo(gt.getTower().getTileX(), gt.getTower().getTileY(), gt
                  .getTower().getFloorLevel(), 15)))
                {
                  int rand = Server.rand.nextInt(9);
                  tilePosX = Zones.safeTileX(gt.getTower().getTileX() + 4 - rand);
                  rand = Server.rand.nextInt(9);
                  tilePosY = Zones.safeTileY(gt.getTower().getTileY() + 4 - rand);
                  abort = true;
                  setTarget(-10L, true);
                  sendToLoggers("Heading to my tower at " + tilePosX + "," + tilePosY);
                }
              }
              if (!abort)
              {
                VolaTile t = getCurrentTile();
                if ((targGroup <= myGroup * getMaxGroupAttackSize()) && 
                  ((isAggHuman()) || (isHunter())) && (
                  (!currTile.isGuarded()) || ((t != null) && (t.isGuarded()))))
                {
                  if (isWithinTileDistanceTo(currTile.getTileX(), currTile.getTileY(), 
                    (int)targ.getPositionZ(), this.template.getMaxHuntDistance()))
                  {
                    if ((targ.getKingdomId() != 0) && 
                      (!isFriendlyKingdom(targ.getKingdomId())) && (
                      (isDefendKingdom()) || ((isAggWhitie()) && 
                      (targ.getKingdomTemplateId() != 3))))
                    {
                      if (!isFighting()) {
                        if (seed == 100)
                        {
                          tilePosX = currTile.tilex - 1 + Server.rand.nextInt(3);
                          tilePosY = currTile.tiley - 1 + Server.rand.nextInt(3);
                        }
                        else
                        {
                          tilePosX = currTile.getTileX();
                          tilePosY = currTile.getTileY();
                          setTarget(targ.getWurmId(), false);
                        }
                      }
                    }
                    else if (isSubmerged())
                    {
                      try
                      {
                        float z = Zones.calculateHeight(targ.getPosX(), targ
                          .getPosY(), targ.isOnSurface());
                        if (z < -5.0F)
                        {
                          if (seed == 100)
                          {
                            tilePosX = currTile.tilex - 1 + Server.rand.nextInt(3);
                            tilePosY = currTile.tiley - 1 + Server.rand.nextInt(3);
                          }
                          else
                          {
                            tilePosX = currTile.getTileX();
                            tilePosY = currTile.getTileY();
                          }
                        }
                        else
                        {
                          int[] tiles = { tilePosX, tilePosY };
                          if (isOnSurface()) {
                            tiles = findRandomDeepSpot(tiles);
                          }
                          tilePosX = tiles[0];
                          tilePosY = tiles[1];
                          setTarget(-10L, true);
                        }
                      }
                      catch (NoSuchZoneException nsz)
                      {
                        setTarget(-10L, true);
                      }
                    }
                    else if (seed == 100)
                    {
                      tilePosX = currTile.tilex - 1 + Server.rand.nextInt(3);
                      tilePosY = currTile.tiley - 1 + Server.rand.nextInt(3);
                    }
                    else
                    {
                      tilePosX = currTile.getTileX();
                      tilePosY = currTile.getTileY();
                      if ((getSize() < 5) && 
                        (targ.getBridgeId() != -10L) && 
                        (getBridgeId() < 0L))
                      {
                        int[] tiles = findBestBridgeEntrance(targ.getTileX(), targ
                          .getTileY(), targ.getLayer(), targ
                          .getBridgeId());
                        if (tiles[0] > 0)
                        {
                          tilePosX = tiles[0];
                          tilePosY = tiles[1];
                          if ((getTileX() == tilePosX) && (getTileY() == tilePosY))
                          {
                            tilePosX = currTile.tilex;
                            tilePosY = currTile.tiley;
                          }
                        }
                      }
                      else if (getBridgeId() != targ.getBridgeId())
                      {
                        int[] tiles = findBestBridgeEntrance(targ.getTileX(), targ
                          .getTileY(), targ.getLayer(), getBridgeId());
                        if (tiles[0] > 0)
                        {
                          tilePosX = tiles[0];
                          tilePosY = tiles[1];
                          if ((getTileX() == tilePosX) && (getTileY() == tilePosY))
                          {
                            tilePosX = currTile.tilex;
                            tilePosY = currTile.tiley;
                          }
                        }
                      }
                    }
                  }
                  else if (!isFighting()) {
                    setTarget(-10L, true);
                  }
                }
                else if (!isFighting()) {
                  setTarget(-10L, true);
                }
              }
            }
          }
          else
          {
            setTarget(-10L, true);
          }
        }
        else
        {
          setTarget(-10L, true);
        }
      }
      if ((tilePosX == tx) && (tilePosY == ty))
      {
        targ = null;return targ;
      }
      tilePosX = Zones.safeTileX(tilePosX);
      tilePosY = Zones.safeTileY(tilePosY);
      if (!isOnSurface())
      {
        int tile = Server.caveMesh.getTile(tilePosX, tilePosY);
        if ((!Tiles.isSolidCave(Tiles.decodeType(tile))) && (
          (Tiles.decodeHeight(tile) > -getHalfHeightDecimeters()) || (isSwimming()) || (isSubmerged())))
        {
          currTile = new PathTile(tilePosX, tilePosY, tile, isOnSurface(), getFloorLevel());return currTile;
        }
      }
      else
      {
        tile = Server.surfaceMesh.getTile(tilePosX, tilePosY);
        if ((Tiles.decodeHeight(tile) > -getHalfHeightDecimeters()) || (isSwimming()) || (isSubmerged()))
        {
          currTile = new PathTile(tilePosX, tilePosY, tile, isOnSurface(), getFloorLevel());return currTile;
        }
      }
      setTarget(-10L, true);
      if ((isDominated()) && (hasOrders())) {
        removeOrder(getFirstOrder());
      }
      int tile = null;return tile;
    }
    catch (ArrayIndexOutOfBoundsException iao)
    {
      iao = iao;
      
      logger.log(Level.WARNING, getName() + " " + tilePosX + ", " + tilePosY + iao.getMessage(), iao);
      VolaTile currTile = null;return currTile;
    }
    finally {}
  }
  
  public final boolean isBridgeBlockingAttack(Creature attacker, boolean justChecking)
  {
    if ((isInvulnerable()) || (attacker.isInvulnerable())) {
      return true;
    }
    if ((getPositionZ() + getAltOffZ() < 0.0F) && (attacker.getBridgeId() > 0L)) {
      return true;
    }
    if ((attacker.getPositionZ() + getAltOffZ() < 0.0F) && (getBridgeId() > 0L)) {
      return true;
    }
    if ((!justChecking) && (getFloorLevel() != attacker.getFloorLevel()) && (getBridgeId() != attacker.getBridgeId()) && 
      (getSize() < 5) && (attacker.getSize() < 5)) {
      return true;
    }
    return false;
  }
  
  public final PathTile getPersonalTargetTile()
  {
    float lPosX = this.status.getPositionX();
    float lPosY = this.status.getPositionY();
    int tilePosX = (int)lPosX >> 2;
    int tilePosY = (int)lPosY >> 2;
    int tx = tilePosX;
    int ty = tilePosY;
    VolaTile currTile = getCurrentTile();
    if (currTile != null)
    {
      int[] foodSpot = forageForFood(currTile);
      if (foodSpot[0] != -1)
      {
        tilePosX = foodSpot[0];
        tilePosY = foodSpot[1];
      }
      else if ((this.template.isTowerBasher()) && (Servers.localServer.PVPSERVER))
      {
        GuardTower closestTower = Kingdoms.getClosestEnemyTower(getTileX(), getTileY(), true, this);
        if (closestTower != null)
        {
          tilePosX = closestTower.getTower().getTileX();
          tilePosY = closestTower.getTower().getTileY();
        }
      }
    }
    if ((tilePosX == tx) && (tilePosY == ty)) {
      return null;
    }
    tilePosX = Zones.safeTileX(tilePosX);
    tilePosY = Zones.safeTileY(tilePosY);
    if (!isOnSurface())
    {
      int tile = Server.caveMesh.getTile(tilePosX, tilePosY);
      if ((!Tiles.isSolidCave(Tiles.decodeType(tile))) && (
        (Tiles.decodeHeight(tile) > -getHalfHeightDecimeters()) || (isSwimming()) || (isSubmerged()))) {
        return new PathTile(tilePosX, tilePosY, tile, isOnSurface(), -1);
      }
    }
    else
    {
      int tile = Server.surfaceMesh.getTile(tilePosX, tilePosY);
      if ((Tiles.decodeHeight(tile) > -getHalfHeightDecimeters()) || (isSwimming()) || (isSubmerged())) {
        return new PathTile(tilePosX, tilePosY, tile, isOnSurface(), 0);
      }
    }
    return null;
  }
  
  public final int getHalfHeightDecimeters()
  {
    return getCentimetersHigh() / 20;
  }
  
  public int[] findRandomCaveExit(int[] tiles)
  {
    int startx = Math.max(0, this.currentTile.tilex - 20);
    int endx = Math.min(Zones.worldTileSizeX - 1, this.currentTile.tilex + 20);
    int starty = Math.max(0, this.currentTile.tiley - 20);
    int endy = Math.min(Zones.worldTileSizeY - 1, this.currentTile.tiley + 20);
    if (this.citizenVillage != null) {
      if (Server.rand.nextInt(2) == 0)
      {
        startx = Math.max(0, this.citizenVillage.getStartX() - 5);
        endx = Math.min(Zones.worldTileSizeX - 1, this.citizenVillage.getEndX() + 5);
        starty = Math.max(0, this.citizenVillage.getStartY() - 5);
        endy = Math.min(Zones.worldTileSizeY - 1, this.citizenVillage.getEndY() + 5);
        int x = this.citizenVillage.startx + Server.rand.nextInt(this.citizenVillage.endx - this.citizenVillage.startx);
        int y = this.citizenVillage.starty + Server.rand.nextInt(this.citizenVillage.endy - this.citizenVillage.starty);
        if (!Tiles.isSolidCave(Tiles.decodeType(Server.caveMesh.getTile(x, y))))
        {
          tiles[0] = x;
          tiles[1] = y;
          setPathfindcounter(0);
        }
      }
    }
    int rand = Server.rand.nextInt(endx - startx);
    startx += rand;
    
    rand = Server.rand.nextInt(endy - starty);
    starty += rand;
    for (int x = startx; x < endx; x++) {
      for (int y = starty; y < endy; y++) {
        if (Tiles.decodeType(Server.caveMesh.getTile(x, y)) == Tiles.Tile.TILE_CAVE_EXIT.id)
        {
          tiles[0] = x;
          tiles[1] = y;
          setPathfindcounter(0);
          
          return tiles;
        }
      }
    }
    return tiles;
  }
  
  public int[] findRandomDeepSpot(int[] tiles)
  {
    int startx = Zones.safeTileX(this.currentTile.tilex - 50);
    int endx = Zones.safeTileX(this.currentTile.tilex + 50);
    int starty = Zones.safeTileY(this.currentTile.tiley - 50);
    int endy = Zones.safeTileY(this.currentTile.tiley + 50);
    
    int rand = Server.rand.nextInt(endx - startx);
    startx += rand;
    
    rand = Server.rand.nextInt(endy - starty);
    starty += rand;
    for (int x = startx; x < Math.min(endx, startx + 10); x++) {
      for (int y = starty; y < Math.min(endy, starty + 10); y++) {
        if (Tiles.decodeHeight(Server.surfaceMesh.getTile(x, y)) < -50.0F)
        {
          tiles[0] = x;
          tiles[1] = y;
          
          return tiles;
        }
      }
    }
    return tiles;
  }
  
  public final boolean isSpyTarget(Creature c)
  {
    if ((c.isDead()) || (c.getPower() > 0)) {
      return false;
    }
    if ((getTemplate().getTemplateId() == 84) && (c.getKingdomId() != 3)) {
      return true;
    }
    if ((getTemplate().getTemplateId() == 12) && (c.getKingdomId() != 1)) {
      return true;
    }
    if ((getTemplate().getTemplateId() == 10) && (c.getKingdomId() != 2)) {
      return true;
    }
    return false;
  }
  
  public final boolean isSpyFriend(Creature c)
  {
    if ((c.isAggHuman()) || (c.getCitizenVillage() == null)) {
      return false;
    }
    if ((getTemplate().getTemplateId() == 84) && (c.getKingdomId() == 3)) {
      return true;
    }
    if ((getTemplate().getTemplateId() == 12) && (c.getKingdomId() == 1)) {
      return true;
    }
    if ((getTemplate().getTemplateId() == 10) && (c.getKingdomId() == 2)) {
      return true;
    }
    return false;
  }
  
  public final boolean isWithinSpyDist(Creature c)
  {
    return (c != null) && (c.isWithinTileDistanceTo(getTileX(), getTileY(), 100, 40));
  }
  
  public int[] getSpySpot(int[] suggested)
  {
    if (isSpy())
    {
      Creature linkedToc = getCreatureLinkedTo();
      if ((linkedToc == null) || (!linkedToc.isDead()) || (!isWithinSpyDist(linkedToc)))
      {
        this.linkedTo = -10L;
        for (Player player : Players.getInstance().getPlayers()) {
          if ((isSpyTarget(player)) && (!player.isDead())) {
            if (isWithinSpyDist(player))
            {
              linkedToc = player;
              setLinkedTo(player.getWurmId(), false);
              break;
            }
          }
        }
      }
      if (linkedToc != null)
      {
        int targX = linkedToc.getTileX() + 15 + Server.rand.nextInt(6);
        if (getTileX() < linkedToc.getTileX()) {
          targX = linkedToc.getTileX() - 15 - Server.rand.nextInt(6);
        }
        int targY = linkedToc.getTileY() + 15 + Server.rand.nextInt(6);
        if (getTileY() < linkedToc.getTileY()) {
          targX = linkedToc.getTileY() - 15 - Server.rand.nextInt(6);
        }
        targX = Zones.safeTileX(targX);
        targY = Zones.safeTileX(targY);
        return new int[] { targX, targY };
      }
    }
    return suggested;
  }
  
  public int[] findRandomCaveEntrance(int[] tiles)
  {
    int startx = Math.max(0, this.currentTile.tilex - 20);
    int endx = Math.min(Zones.worldTileSizeX - 1, this.currentTile.tilex + 20);
    int starty = Math.max(0, this.currentTile.tiley - 20);
    int endy = Math.min(Zones.worldTileSizeY - 1, this.currentTile.tiley + 20);
    if (this.citizenVillage != null)
    {
      startx = Math.max(0, this.citizenVillage.getStartX() - 5);
      endx = Math.min(Zones.worldTileSizeX - 1, this.citizenVillage.getEndX() + 5);
      starty = Math.max(0, this.citizenVillage.getStartY() - 5);
      endy = Math.min(Zones.worldTileSizeY - 1, this.citizenVillage.getEndY() + 5);
    }
    int rand = Server.rand.nextInt(endx - startx);
    startx += rand;
    
    rand = Server.rand.nextInt(endy - starty);
    starty += rand;
    boolean passMineDoors = (isKingdomGuard()) || (isGhost()) || (isUnique());
    for (int x = startx; x < Math.min(endx, startx + 10); x++) {
      for (int y = starty; y < Math.min(endy, starty + 10); y++) {
        if ((Tiles.decodeType(Server.surfaceMesh.getTile(x, y)) == Tiles.Tile.TILE_HOLE.id) || ((passMineDoors) && 
          (Tiles.isMineDoor(Tiles.decodeType(Server.surfaceMesh.getTile(x, y))))))
        {
          tiles[0] = x;
          tiles[1] = y;
          
          return tiles;
        }
      }
    }
    return tiles;
  }
  
  public int[] findBestBridgeEntrance(int tilex, int tiley, int layer, long bridgeId)
  {
    VolaTile t = Zones.getTileOrNull(tilex, tiley, layer >= 0);
    if (t != null) {
      if (t.getStructure() != null) {
        if (t.getStructure().getWurmId() == bridgeId) {
          return t.getStructure().findBestBridgeEntrance(this, tilex, tiley, layer, bridgeId, this.pathfindcounter);
        }
      }
    }
    return Structure.noEntrance;
  }
  
  public int getAbilityTitleVal()
  {
    return this.template.abilityTitle;
  }
  
  public String getAbilityTitle()
  {
    if (this.template.abilityTitle > -1) {
      return Abilities.getAbilityString(this.template.abilityTitle) + " ";
    }
    return "";
  }
  
  public boolean isLogged()
  {
    return false;
  }
  
  public float getFaith()
  {
    return this.template.getFaith();
  }
  
  public Skill getChannelingSkill()
  {
    Skill channeling = null;
    try
    {
      channeling = this.skills.getSkill(10067);
    }
    catch (NoSuchSkillException nss)
    {
      if (getFaith() >= 10.0F) {
        channeling = this.skills.learn(10067, 1.0F);
      }
    }
    return channeling;
  }
  
  public Skill getMindLogical()
  {
    Skill toReturn = null;
    try
    {
      toReturn = getSkills().getSkill(100);
    }
    catch (NoSuchSkillException nss)
    {
      toReturn = getSkills().learn(100, 1.0F);
    }
    return toReturn;
  }
  
  public Skill getMindSpeed()
  {
    Skill toReturn = null;
    try
    {
      toReturn = getSkills().getSkill(101);
    }
    catch (NoSuchSkillException nss)
    {
      toReturn = getSkills().learn(101, 1.0F);
    }
    return toReturn;
  }
  
  public Skill getSoulDepth()
  {
    Skill toReturn = null;
    try
    {
      toReturn = getSkills().getSkill(106);
    }
    catch (NoSuchSkillException nss)
    {
      toReturn = getSkills().learn(106, 1.0F);
    }
    return toReturn;
  }
  
  public Skill getBreedingSkill()
  {
    Skill toReturn;
    try
    {
      toReturn = getSkills().getSkill(10085);
    }
    catch (NoSuchSkillException nss)
    {
      Skill toReturn;
      toReturn = getSkills().learn(10085, 1.0F);
    }
    return toReturn;
  }
  
  public Skill getSoulStrength()
  {
    Skill toReturn = null;
    try
    {
      toReturn = getSkills().getSkill(105);
    }
    catch (NoSuchSkillException nss)
    {
      toReturn = getSkills().learn(105, 1.0F);
    }
    return toReturn;
  }
  
  public Skill getBodyStrength()
  {
    Skill toReturn = null;
    try
    {
      toReturn = getSkills().getSkill(102);
    }
    catch (NoSuchSkillException nss)
    {
      toReturn = getSkills().learn(102, 1.0F);
    }
    return toReturn;
  }
  
  public Deity getDeity()
  {
    return this.template.getDeity();
  }
  
  public boolean isActionFaithful(Action action)
  {
    if (getDeity() != null) {
      if (this.faithful) {
        return getDeity().isActionFaithful(action);
      }
    }
    return true;
  }
  
  public void performActionOkey(Action action)
  {
    if (getDeity() != null) {
      if (!getDeity().performActionOkey(this, action)) {
        getCommunicator().sendNormalServerMessage(getDeity().name + " noticed you!");
      }
    }
  }
  
  public boolean checkLoyaltyProgram()
  {
    return false;
  }
  
  public boolean maybeModifyAlignment(float modification)
  {
    return false;
  }
  
  public boolean isPriest()
  {
    return (isSpellCaster()) || (isSummoner());
  }
  
  public float getAlignment()
  {
    return this.template.getAlignment();
  }
  
  float creatureFavor = 100.0F;
  
  public float getFavor()
  {
    if ((isSpellCaster()) || (isSummoner())) {
      return this.creatureFavor;
    }
    return this.template.getFaith();
  }
  
  public float getFavorLinked()
  {
    return this.template.getFaith();
  }
  
  public void setFavor(float favor)
    throws IOException
  {
    if ((isSpellCaster()) || (isSummoner())) {
      this.creatureFavor = favor;
    }
  }
  
  public void depleteFavor(float favorToRemove, boolean combatSpell)
    throws IOException
  {
    if ((isSpellCaster()) || (isSummoner())) {
      setFavor(getFavor() - favorToRemove);
    }
  }
  
  public boolean mayChangeDeity(int targetDeity)
  {
    return true;
  }
  
  public boolean isNewbie()
  {
    return false;
  }
  
  public boolean maySteal()
  {
    return true;
  }
  
  public boolean isAtWarWith(Creature creature)
  {
    if ((this.citizenVillage != null) && (creature.citizenVillage != null)) {
      return this.citizenVillage.isEnemy(creature.citizenVillage);
    }
    return false;
  }
  
  public boolean isChampion()
  {
    return false;
  }
  
  public boolean modifyChampionPoints(int championPointsModifier)
  {
    return false;
  }
  
  public int getFatigueLeft()
  {
    return 20000;
  }
  
  public boolean checkPrayerFaith()
  {
    return false;
  }
  
  public boolean isAlive()
  {
    return !this.status.dead;
  }
  
  public boolean isDead()
  {
    return this.status.dead;
  }
  
  public byte getKingdomId()
  {
    if (!Servers.isThisAPvpServer())
    {
      Village bVill = getBrandVillage();
      if (bVill != null) {
        return bVill.kingdom;
      }
    }
    return this.status.kingdom;
  }
  
  public byte getKingdomTemplateId()
  {
    Kingdom k = Kingdoms.getKingdom(getKingdomId());
    if (k != null) {
      return k.getTemplate();
    }
    return 0;
  }
  
  public int getReputation()
  {
    return this.template.getReputation();
  }
  
  public void playAnthem()
  {
    if (this.musicPlayer != null)
    {
      if (getKingdomTemplateId() == 3) {
        this.musicPlayer.checkMUSIC_ANTHEMHOTS_SND();
      }
      if (getKingdomId() == 1) {
        this.musicPlayer.checkMUSIC_ANTHEMJENN_SND();
      }
      if (getKingdomId() == 2) {
        this.musicPlayer.checkMUSIC_ANTHEMMOLREHAN_SND();
      }
    }
  }
  
  public boolean isTransferring()
  {
    return false;
  }
  
  public boolean isOnCurrentServer()
  {
    return true;
  }
  
  public boolean setKingdomId(byte kingdom)
    throws IOException
  {
    return setKingdomId(kingdom, false, true);
  }
  
  public boolean setKingdomId(byte kingdom, boolean forced)
    throws IOException
  {
    return setKingdomId(kingdom, forced, true);
  }
  
  public boolean setKingdomId(byte kingdom, boolean forced, boolean setTimeStamp)
    throws IOException
  {
    return setKingdomId(kingdom, forced, setTimeStamp, true);
  }
  
  public boolean setKingdomId(byte kingdom, boolean forced, boolean setTimeStamp, boolean online)
    throws IOException
  {
    boolean sendUpdate = false;
    if (getKingdomId() != kingdom)
    {
      if (isKing())
      {
        getCommunicator().sendNormalServerMessage("You are the king, and may not change kingdom!");
        return false;
      }
      Village v = getCitizenVillage();
      if ((!forced) && (v != null)) {
        if (v.getMayor().getId() == getWurmId()) {
          try
          {
            getCommunicator().sendNormalServerMessage("You are the mayor of " + v
              .getName() + ", and may not change kingdom!");
            return false;
          }
          catch (Exception ex)
          {
            return false;
          }
        }
      }
      if ((Kingdoms.getKingdomTemplateFor(getKingdomId()) == 3) && 
        (Kingdoms.getKingdomTemplateFor(kingdom) != 3))
      {
        if ((getDeity() != null) && (getDeity().number == 4))
        {
          setDeity(null);
          setFaith(0.0F);
          setAlignment(Math.max(1.0F, getAlignment()));
        }
      }
      else if ((Kingdoms.getKingdomTemplateFor(kingdom) == 3) && 
        (Kingdoms.getKingdomTemplateFor(getKingdomId()) != 3)) {
        if ((getDeity() == null) || (getDeity().number == 1) || 
          (getDeity().number == 2) || 
          (getDeity().number == 3))
        {
          setDeity(Deities.getDeity(4));
          setAlignment(Math.min(getAlignment(), -50.0F));
          setFaith(1.0F);
        }
      }
      if ((getKingdomId() != 0) && (!forced))
      {
        if (this.citizenVillage != null) {
          this.citizenVillage.removeCitizen(this);
        }
        if ((kingdom != 0) && (Servers.localServer.PVPSERVER)) {
          increaseChangedKingdom(setTimeStamp);
        }
        sendUpdate = true;
      }
      clearRoyalty();
      setTeam(null, true);
      if ((isPlayer()) && (getCommunicator() != null) && (hasLink())) {
        if ((Servers.localServer.PVPSERVER) && (!Servers.localServer.testServer)) {
          try
          {
            KingdomIp kip = KingdomIp.getKIP(getCommunicator().getConnection().getIp(), getKingdomId());
            if (kip != null) {
              kip.logon(kingdom);
            }
          }
          catch (Exception iox)
          {
            logger.log(Level.INFO, getName() + " " + iox.getMessage());
          }
        }
      }
      this.status.setKingdom(kingdom);
      if (isPlayer())
      {
        if ((Servers.localServer.isChallengeOrEpicServer()) || (Servers.isThisAChaosServer()) || (Servers.localServer.PVPSERVER))
        {
          if (getCommunicator().getConnection() != null) {
            try
            {
              if (getCommunicator().getConnection().getIp() != null)
              {
                KingdomIp kip = KingdomIp.getKIP(getCommunicator().getConnection().getIp());
                if (kip != null) {
                  kip.setKingdom(kingdom);
                }
              }
            }
            catch (NullPointerException localNullPointerException) {}
          }
          if (((Server.getInstance().isPS()) && (Servers.localServer.PVPSERVER)) || (Servers.isThisAChaosServer())) {
            ((Player)this).getSaveFile().setChaosKingdom(kingdom);
          }
        }
        Players.getInstance().registerNewKingdom(this);
        setVotedKing(false);
      }
      playAnthem();
      Creatures.getInstance().setCreatureDead(this);
      setTarget(-10L, true);
      if ((sendUpdate) && (online))
      {
        setVisible(false);
        setVisible(true);
      }
      if (this.citizenVillage != null) {
        if (!forced) {
          this.citizenVillage.removeCitizen(this);
        } else if (this.citizenVillage.getMayor().wurmId == getWurmId()) {
          this.citizenVillage.convertToKingdom(kingdom, true, setTimeStamp);
        }
      }
    }
    return true;
  }
  
  public boolean hasVotedKing()
  {
    return true;
  }
  
  public void checkForEnemies()
  {
    checkForEnemies(false);
  }
  
  public void checkForEnemies(boolean overrideRandomChance)
  {
    if ((isWarGuard()) || (isKingdomGuard()))
    {
      GuardTower gt = Kingdoms.getClosestTower(getTileX(), getTileY(), true);
      if ((gt != null) && (gt.getKingdom() == getKingdomId()) && 
        (System.currentTimeMillis() - gt.getLastSentWarning() < 180000L)) {
        overrideRandomChance = true;
      }
    }
    if (!overrideRandomChance)
    {
      if (Server.rand.nextInt((isKingdomGuard()) || (isWarGuard()) ? 20 : 100) != 0) {}
    }
    else if (getVisionArea() != null) {
      try
      {
        if (isOnSurface()) {
          getVisionArea().getSurface().checkForEnemies();
        } else {
          getVisionArea().getUnderGround().checkForEnemies();
        }
      }
      catch (Exception ep)
      {
        logger.log(Level.WARNING, ep.getMessage(), ep);
      }
    }
  }
  
  public boolean sendTransfer(Server senderServer, String targetIp, int targetPort, String serverpass, int targetServerId, int tilex, int tiley, boolean surfaced, boolean toOrFromEpic, byte targetKingdomId)
  {
    logger.log(Level.WARNING, "Sendtransfer called in creature", new Exception());
    return false;
  }
  
  public boolean mayChangeKingdom(Creature converter)
  {
    return false;
  }
  
  public boolean isOfCustomKingdom()
  {
    Kingdom k = Kingdoms.getKingdom(getKingdomId());
    if ((k != null) && (k.isCustomKingdom())) {
      return true;
    }
    return false;
  }
  
  public void punishSkills(double aMod, boolean pvp)
  {
    if ((getCultist() != null) && (getCultist().isNoDecay())) {
      return;
    }
    try
    {
      Skill bodyStr = this.skills.getSkill(102);
      bodyStr.setKnowledge(bodyStr.getKnowledge() - 0.009999999776482582D, false);
      
      Skill body = this.skills.getSkill(1);
      body.setKnowledge(body.getKnowledge() - 0.009999999776482582D, false);
    }
    catch (NoSuchSkillException nss)
    {
      this.skills.learn(102, 1.0F);
      logger.log(Level.WARNING, getName() + " learnt body strength.");
    }
    if (!pvp)
    {
      Skill[] sk = this.skills.getSkills();
      int nums = 0;
      for (Skill lElement : sk) {
        if ((lElement.getType() == 4) || (lElement.getType() == 2)) {
          if ((lElement.getNumber() != 1023) && (Server.rand.nextInt(10) == 0)) {
            if ((lElement.getKnowledge(0.0D) > 2.0D) && (lElement.getKnowledge(0.0D) < 99.0D))
            {
              lElement.setKnowledge(Math.max(1.0D, lElement.getKnowledge() - aMod), false);
              nums++;
              if (nums > 4) {
                break;
              }
            }
          }
        }
      }
    }
  }
  
  public long getMoney()
  {
    return 0L;
  }
  
  public boolean addMoney(long moneyToAdd)
    throws IOException
  {
    return false;
  }
  
  public boolean chargeMoney(long moneyToCharge)
    throws IOException
  {
    return false;
  }
  
  public boolean hasCustomColor()
  {
    if (getPower() > 0) {
      return true;
    }
    if (hasCustomKingdom()) {
      return true;
    }
    if (this.status.hasCustomColor()) {
      return true;
    }
    return (this.template.getColorRed() != 255) || (this.template.getColorGreen() != 255) || (this.template.getColorBlue() != 255);
  }
  
  public boolean hasCustomKingdom()
  {
    return (getKingdomId() > 4) || (getKingdomId() < 0);
  }
  
  public byte getColorRed()
  {
    if (this.status.hasCustomColor()) {
      return this.status.getColorRed();
    }
    return (byte)this.template.getColorRed();
  }
  
  public byte getColorGreen()
  {
    if (this.status.hasCustomColor()) {
      return this.status.getColorGreen();
    }
    return (byte)this.template.getColorGreen();
  }
  
  public byte getColorBlue()
  {
    if (this.status.hasCustomColor()) {
      return this.status.getColorBlue();
    }
    return (byte)this.template.getColorBlue();
  }
  
  public boolean hasCustomSize()
  {
    if (this.status.getSizeMod() != 1.0F) {
      return true;
    }
    return (this.template.getSizeModX() != 64) || (this.template.getSizeModY() != 64) || (this.template.getSizeModZ() != 64);
  }
  
  public byte getSizeModX()
  {
    return (byte)(int)Math.min(255.0F, this.template.getSizeModX() * this.status.getSizeMod());
  }
  
  public byte getSizeModY()
  {
    return (byte)(int)Math.min(255.0F, this.template.getSizeModY() * this.status.getSizeMod());
  }
  
  public byte getSizeModZ()
  {
    return (byte)(int)Math.min(255.0F, this.template.getSizeModZ() * this.status.getSizeMod());
  }
  
  public boolean isClimbing()
  {
    return true;
  }
  
  public boolean acceptsInvitations()
  {
    return false;
  }
  
  public Cultist getCultist()
  {
    return null;
  }
  
  public static short[] getTileSteepness(int tilex, int tiley, boolean surfaced)
  {
    short highest = -100;
    short lowest = 32000;
    for (int x = 0; x <= 1; x++) {
      for (int y = 0; y <= 1; y++) {
        if ((tilex + x < Zones.worldTileSizeX) && (tiley + y < Zones.worldTileSizeY))
        {
          short height = 0;
          if (surfaced) {
            height = Tiles.decodeHeight(Server.surfaceMesh.getTile(tilex + x, tiley + y));
          } else {
            height = Tiles.decodeHeight(Server.caveMesh.getTile(tilex + x, tiley + y));
          }
          if (height > highest) {
            highest = height;
          }
          if (height < lowest) {
            lowest = height;
          }
        }
      }
    }
    int med = (highest + lowest) / 2;
    return new short[] { (short)med, (short)(highest - lowest) };
  }
  
  public short[] getLowestTileCorner(short tilex, short tiley)
  {
    short lowestX = tilex;
    short lowestY = tiley;
    short lowest = 32000;
    for (int x = 0; x <= 1; x++) {
      for (int y = 0; y <= 1; y++) {
        if ((tilex + x < Zones.worldTileSizeX) && (tiley + y < Zones.worldTileSizeY))
        {
          short height = Tiles.decodeHeight(Server.surfaceMesh.getTile(tilex + x, tiley + y));
          if (height < lowest)
          {
            lowest = height;
            lowestX = (short)(tilex + x);
            lowestY = (short)(tiley + y);
          }
        }
      }
    }
    return new short[] { lowestX, lowestY };
  }
  
  public Titles.Title getSecondTitle()
  {
    return null;
  }
  
  public Titles.Title getTitle()
  {
    return null;
  }
  
  public String getTitleString()
  {
    String suff = "";
    if (getTitle() != null) {
      if (getTitle().isRoyalTitle())
      {
        if ((getAppointments() != 0L) || (isAppointed())) {
          suff = suff + getKingdomTitle();
        }
      }
      else {
        suff = suff + getTitle().getName(isNotFemale());
      }
    }
    if ((Features.Feature.COMPOUND_TITLES.isEnabled()) && (getSecondTitle() != null))
    {
      if (getTitle() != null) {
        suff = suff + " ";
      }
      if (getSecondTitle().isRoyalTitle())
      {
        if ((getAppointments() != 0L) || (isAppointed())) {
          suff = suff + getKingdomTitle();
        }
      }
      else {
        suff = suff + getSecondTitle().getName(isNotFemale());
      }
    }
    return suff;
  }
  
  public String getKingdomTitle()
  {
    return "";
  }
  
  public float getSpellDamageProtectBonus()
  {
    return getBonusForSpellEffect((byte)19);
  }
  
  public float getDetectDangerBonus()
  {
    if (getKingdomTemplateId() == 3) {
      return 50.0F + ItemBonus.getDetectionBonus(this);
    }
    SpellEffects eff = getSpellEffects();
    if (eff != null)
    {
      SpellEffect effbon = eff.getSpellEffect((byte)21);
      if (effbon != null) {
        return effbon.power + ItemBonus.getDetectionBonus(this);
      }
    }
    return ItemBonus.getDetectionBonus(this);
  }
  
  public float getBonusForSpellEffect(byte enchantment)
  {
    SpellEffects eff = getSpellEffects();
    if (eff != null)
    {
      SpellEffect skillgain = eff.getSpellEffect(enchantment);
      if (skillgain != null) {
        return skillgain.power;
      }
    }
    return 0.0F;
  }
  
  public float getNoLocateItemBonus(int dist)
  {
    Item[] bodyItems = getBody().getContainersAndWornItems();
    float maxBonus = 0.0F;
    Item maxItem = null;
    for (int x = 0; x < bodyItems.length; x++) {
      if ((bodyItems[x].isEnchantableJewelry()) || (bodyItems[x].isArtifact())) {
        if (bodyItems[x].getNolocateBonus() > maxBonus)
        {
          maxBonus = bodyItems[x].getNolocateBonus();
          maxItem = bodyItems[x];
        }
      }
    }
    if (maxItem != null)
    {
      maxBonus = (maxBonus + maxItem.getCurrentQualityLevel()) / 2.0F;
      ItemSpellEffects effs = maxItem.getSpellEffects();
      if (effs == null) {
        effs = new ItemSpellEffects(maxItem.getWurmId());
      }
      SpellEffect eff = effs.getSpellEffect((byte)29);
      if (Servers.isThisAnEpicOrChallengeServer())
      {
        if (dist < 200)
        {
          if (eff != null) {
            eff.setPower(eff.power - 0.2F);
          }
        }
        else if (eff != null) {
          eff.setPower(eff.power - 0.0F);
        }
      }
      else if (eff != null) {
        eff.setPower(eff.power - 0.2F);
      }
    }
    return maxBonus;
  }
  
  public int getNumberOfShopItems()
  {
    Set<Item> ite = getInventory().getItems();
    int nums = 0;
    for (Item i : ite) {
      if (!i.isCoin()) {
        nums++;
      }
    }
    return nums;
  }
  
  public final void addNewbieBuffs()
  {
    if (getPlayingTime() < 86400000L)
    {
      SpellEffects effs = createSpellEffects();
      
      SpellEffect eff = effs.getSpellEffect((byte)74);
      if (eff == null)
      {
        getCommunicator().sendSafeServerMessage("You require less food and drink as a new player.");
        
        eff = new SpellEffect(getWurmId(), (byte)74, 100.0F, (int)((86400000L - getPlayingTime()) / 1000L), (byte)1, (byte)0, true);
        
        effs.addSpellEffect(eff);
      }
      SpellEffect range = effs.getSpellEffect((byte)73);
      if (range == null)
      {
        getCommunicator().sendSafeServerMessage("Creatures and monsters are less aggressive to new players.");
        
        range = new SpellEffect(getWurmId(), (byte)73, 100.0F, (int)((86400000L - getPlayingTime()) / 1000L), (byte)1, (byte)0, true);
        
        effs.addSpellEffect(range);
      }
      SpellEffect health = effs.getSpellEffect((byte)75);
      if (health == null)
      {
        getCommunicator().sendSafeServerMessage("You regenerate health faster as a new player.");
        
        health = new SpellEffect(getWurmId(), (byte)75, 100.0F, (int)((86400000L - getPlayingTime()) / 1000L), (byte)1, (byte)0, true);
        
        effs.addSpellEffect(health);
      }
    }
  }
  
  public SpellEffects getSpellEffects()
  {
    return getStatus().spellEffects;
  }
  
  public void sendUpdateSpellEffect(SpellEffect effect)
  {
    if (effect.type != 43)
    {
      SpellEffectsEnum spellEffect = SpellEffectsEnum.getEnumByName(effect.getName());
      if (spellEffect != SpellEffectsEnum.NONE) {
        getCommunicator().sendAddSpellEffect(effect.id, spellEffect, effect.timeleft, effect.power);
      } else {
        getCommunicator().sendAddSpellEffect(effect.id, effect.getName(), effect.type, effect.getSpellEffectType(), effect
          .getSpellInfluenceType(), effect.timeleft, effect.power);
      }
    }
  }
  
  public void sendAddSpellEffect(SpellEffect effect)
  {
    if (effect.type != 43)
    {
      SpellEffectsEnum spellEffect = SpellEffectsEnum.getEnumByName(effect.getName());
      if (spellEffect != SpellEffectsEnum.NONE) {
        getCommunicator().sendAddSpellEffect(effect.id, spellEffect, effect.timeleft, effect.power);
      } else {
        getCommunicator().sendAddSpellEffect(effect.id, effect.getName(), effect.type, effect.getSpellEffectType(), effect
          .getSpellInfluenceType(), effect.timeleft, effect.power);
      }
    }
    if (effect.type == 23) {
      getCombatHandler().addDodgeModifier(willowMod);
    } else if (effect.type == 39) {
      getMovementScheme().setChargeMoveMod(true);
    }
  }
  
  public void removeSpellEffect(SpellEffect effect)
  {
    if (effect.type != 43)
    {
      SpellEffectsEnum spellEffect = SpellEffectsEnum.getEnumByName(effect.getName());
      if (spellEffect != SpellEffectsEnum.NONE) {
        getCommunicator().sendRemoveSpellEffect(effect.id, spellEffect);
      }
      getCommunicator().sendNormalServerMessage("You are no longer affected by " + effect.getName() + ".");
    }
    else
    {
      sendRemovePhantasms();
    }
    if (effect.type == 23)
    {
      getCombatHandler().removeDodgeModifier(willowMod);
    }
    else if (effect.type == 39)
    {
      getMovementScheme().setChargeMoveMod(false);
    }
    else if (effect.type == 64)
    {
      setVisible(false);
      refreshAttitudes();
      setVisible(true);
    }
    else if (effect.type == 72)
    {
      setModelName("Human");
    }
  }
  
  public final void removeIllusion()
  {
    if (getSpellEffects() != null)
    {
      SpellEffect ill = getSpellEffects().getSpellEffect((byte)72);
      if (ill != null) {
        getSpellEffects().removeSpellEffect(ill);
      }
    }
  }
  
  public SpellEffects createSpellEffects()
  {
    if (getStatus().spellEffects == null) {
      getStatus().spellEffects = new SpellEffects(getWurmId());
    }
    return getStatus().spellEffects;
  }
  
  public boolean dispelSpellEffect(double power)
  {
    boolean toret = false;
    if (getMovementScheme().setWebArmourMod(false, 0.0F))
    {
      getMovementScheme().setWebArmourMod(false, 0.0F);
      toret = true;
    }
    if (getSpellEffects() != null)
    {
      SpellEffect[] speffs = getSpellEffects().getEffects();
      for (int x = 0; x < speffs.length; x++) {
        if ((speffs[x].type != 64) && (speffs[x].type != 74) && (speffs[x].type != 73) && (speffs[x].type != 75)) {
          if (Server.rand.nextInt(Math.max(1, (int)speffs[x].power)) < power)
          {
            getSpellEffects().removeSpellEffect(speffs[x]);
            if ((speffs[x].type == 22) && 
              (getCurrentTile() != null)) {
              getCurrentTile().setNewRarityShader(this);
            }
            return true;
          }
        }
      }
    }
    return toret;
  }
  
  public byte getFarwalkerSeconds()
  {
    return 0;
  }
  
  public Creature getDominator()
  {
    if (this.dominator == -10L) {
      return null;
    }
    try
    {
      return Server.getInstance().getCreature(this.dominator);
    }
    catch (Exception localException) {}
    return null;
  }
  
  public Item getWornItem(byte bodyPart)
  {
    try
    {
      return getEquippedItem(bodyPart);
    }
    catch (NoSpaceException nsp)
    {
      return null;
    }
    catch (NoSuchItemException nsi) {}
    return null;
  }
  
  public boolean hasBridle()
  {
    if ((isHorse()) || (isUnicorn()))
    {
      Item neckItem = getWornItem((byte)17);
      if (neckItem != null) {
        return neckItem.isBridle();
      }
    }
    return false;
  }
  
  private float calcHorseShoeBonus(boolean mounting)
  {
    float bonus = 0.0F;
    float leftFootB = 0.0F;
    float rightFootB = 0.0F;
    float leftHandB = 0.0F;
    float rightHandB = 0.0F;
    try
    {
      Item leftFoot = getEquippedItem((byte)15);
      leftFootB += Math.max(10.0F, leftFoot.getCurrentQualityLevel()) / 2000.0F;
      leftFootB += leftFoot.getSpellSpeedBonus() / 2000.0F;
      leftFootB += leftFoot.getRarity() * 0.03F;
      if ((!mounting) && (!this.ignoreSaddleDamage)) {
        leftFoot.setDamage(leftFoot.getDamage() + 0.001F);
      }
    }
    catch (NoSpaceException nsp)
    {
      logger.log(Level.WARNING, getName() + " No left foot.");
    }
    catch (NoSuchItemException localNoSuchItemException) {}
    try
    {
      Item rightFoot = getEquippedItem((byte)16);
      rightFootB += Math.max(10.0F, rightFoot.getCurrentQualityLevel()) / 2000.0F;
      rightFootB += rightFoot.getSpellSpeedBonus() / 2000.0F;
      rightFootB += rightFoot.getRarity() * 0.03F;
      if ((!mounting) && (!this.ignoreSaddleDamage)) {
        rightFoot.setDamage(rightFoot.getDamage() + 0.001F);
      }
    }
    catch (NoSpaceException nsp)
    {
      logger.log(Level.WARNING, getName() + " No left foot.");
    }
    catch (NoSuchItemException localNoSuchItemException1) {}
    try
    {
      Item rightHand = getEquippedItem((byte)14);
      rightHandB += Math.max(10.0F, rightHand.getCurrentQualityLevel()) / 2000.0F;
      rightHandB += rightHand.getSpellSpeedBonus() / 2000.0F;
      rightHandB += rightHand.getRarity() * 0.03F;
      if ((!mounting) && (!this.ignoreSaddleDamage)) {
        rightHand.setDamage(rightHand.getDamage() + 0.001F);
      }
    }
    catch (NoSpaceException nsp)
    {
      logger.log(Level.WARNING, getName() + " No left foot.");
    }
    catch (NoSuchItemException localNoSuchItemException2) {}
    try
    {
      Item leftHand = getEquippedItem((byte)13);
      leftHandB += Math.max(10.0F, leftHand.getCurrentQualityLevel()) / 2000.0F;
      leftHandB += leftHand.getSpellSpeedBonus() / 2000.0F;
      leftHandB += leftHand.getRarity() * 0.03F;
      if ((!mounting) && (!this.ignoreSaddleDamage)) {
        leftHand.setDamage(leftHand.getDamage() + 0.001F);
      }
    }
    catch (NoSpaceException nsp)
    {
      logger.log(Level.WARNING, getName() + " No left foot.");
    }
    catch (NoSuchItemException localNoSuchItemException3) {}
    bonus += leftHandB;
    bonus += rightHandB;
    bonus += leftFootB;
    bonus += rightFootB;
    
    return bonus;
  }
  
  public boolean hasHands()
  {
    return this.template.hasHands;
  }
  
  public boolean isDominated()
  {
    return this.dominator > 0L;
  }
  
  public boolean setDominator(long newdominator)
  {
    if (newdominator == -10L)
    {
      if (this.decisions != null)
      {
        this.decisions.clearOrders();
        this.decisions = null;
      }
      try
      {
        setKingdomId((byte)0);
      }
      catch (IOException iox)
      {
        logger.log(Level.WARNING, iox.getMessage(), iox);
      }
      setLoyalty(0.0F);
      setLeader(null);
    }
    if (newdominator != this.dominator)
    {
      this.dominator = newdominator;
      getStatus().setDominator(this.dominator);
      sendAttitudeChange();
      return true;
    }
    return false;
  }
  
  public boolean hasPet()
  {
    return false;
  }
  
  public boolean isOnFire()
  {
    return this.template.isOnFire();
  }
  
  public byte getFireRadius()
  {
    return this.template.getFireRadius();
  }
  
  public int getPaintMode()
  {
    return this.template.getPaintMode();
  }
  
  public boolean addOrder(Order order)
  {
    if (this.decisions == null) {
      this.decisions = new DecisionStack();
    }
    return this.decisions.addOrder(order);
  }
  
  public void clearOrders()
  {
    if (this.decisions != null) {
      this.decisions.clearOrders();
    }
    getStatus().setPath(null);
    getStatus().setMoving(false);
    setTarget(-10L, true);
  }
  
  public Order getFirstOrder()
  {
    if (this.decisions != null) {
      return this.decisions.getFirst();
    }
    return null;
  }
  
  public void removeOrder(Order order)
  {
    if (this.decisions != null) {
      this.decisions.removeOrder(order);
    }
  }
  
  public boolean hasOrders()
  {
    if (this.decisions != null) {
      return this.decisions.hasOrders();
    }
    return false;
  }
  
  public boolean mayReceiveOrder()
  {
    if (this.decisions != null) {
      return this.decisions.mayReceiveOrders();
    }
    if (isDominated())
    {
      this.decisions = new DecisionStack();
      return true;
    }
    return false;
  }
  
  public Creature getPet()
  {
    return null;
  }
  
  public void modifyLoyalty(float modifier)
  {
    if (getStatus().modifyLoyalty(modifier))
    {
      if (getDominator() != null)
      {
        getDominator().getCommunicator().sendAlertServerMessage(getNameWithGenus() + " is tame no more.", (byte)2);
        getDominator().setPet(-10L);
      }
      setDominator(-10L);
    }
  }
  
  public void setLoyalty(float loyalty)
  {
    getStatus().setLoyalty(loyalty);
  }
  
  public float getLoyalty()
  {
    return getStatus().loyalty;
  }
  
  public ArmourTemplate.ArmourType getArmourType()
  {
    return this.template.getArmourType();
  }
  
  public boolean isFrozen()
  {
    return false;
  }
  
  protected void setLastVehicle(long _lastvehicle, byte _seatType)
  {
    this.status.setVehicle(_lastvehicle, _seatType);
  }
  
  public void setVehicle(long vehicle, boolean teleport, byte _seatType)
  {
    setVehicle(vehicle, teleport, _seatType, -1, -1);
  }
  
  public void setVehicle(long _vehicle, boolean teleport, byte _seatType, int tilex, int tiley)
  {
    if (_vehicle == -10L)
    {
      if (this.vehicle != -10L)
      {
        removeIllusion();
        if (getVisionArea() != null)
        {
          if (getVisionArea().getSurface() != null) {
            getVisionArea().getSurface().clearMovementForCreature(this.vehicle);
          }
          if (getVisionArea().getUnderGround() != null) {
            getVisionArea().getUnderGround().clearMovementForCreature(this.vehicle);
          }
        }
        if (WurmId.getType(this.vehicle) == 1)
        {
          setLastVehicle(-10L, (byte)-1);
          try
          {
            Creature lVehicle = Server.getInstance().getCreature(this.vehicle);
            lVehicle.removeRider(getWurmId());
            if (teleport)
            {
              Structure struct = getActualTileVehicle().getStructure();
              if ((struct != null) && (!struct.mayPass(this))) {
                try
                {
                  float newposx = lVehicle.getPosX();
                  float newposy = lVehicle.getPosY();
                  tilex = (int)newposx / 4;
                  tiley = (int)newposy / 4;
                }
                catch (Exception ex)
                {
                  logger.log(Level.WARNING, ex.getMessage(), ex);
                }
              }
            }
          }
          catch (NoSuchCreatureException nsi)
          {
            logger.log(Level.WARNING, getName() + " " + nsi.getMessage(), nsi);
          }
          catch (NoSuchPlayerException nsp)
          {
            logger.log(Level.WARNING, getName() + " " + nsp.getMessage(), nsp);
          }
        }
        else
        {
          try
          {
            Item ivehic = Items.getItem(this.vehicle);
            
            boolean atTransferBorder = false;
            if ((getTileX() < 20) || (getTileX() > Zones.worldTileSizeX - 20) || (getTileY() < 20) || 
              (getTileY() > Zones.worldTileSizeX - 20)) {
              atTransferBorder = true;
            }
            if ((!ivehic.isBoat()) || ((!isTransferring()) && (!atTransferBorder)))
            {
              setLastVehicle(-10L, (byte)-1);
              if (teleport)
              {
                Structure struct = getActualTileVehicle().getStructure();
                if ((struct != null) && (struct.isTypeHouse()) && (!struct.mayPass(this))) {
                  try
                  {
                    Creature dragger = Items.getDragger(ivehic);
                    float newposx = dragger == null ? ivehic.getPosX() : dragger.getPosX();
                    float newposy = dragger == null ? ivehic.getPosY() : dragger.getPosY();
                    tilex = (int)newposx / 4;
                    tiley = (int)newposy / 4;
                  }
                  catch (Exception ex)
                  {
                    logger.log(Level.WARNING, ex.getMessage(), ex);
                  }
                }
                if ((struct != null) && (struct.isTypeBridge())) {
                  try
                  {
                    Creature dragger = Items.getDragger(ivehic);
                    float newposx = dragger == null ? ivehic.getPosX() : dragger.getPosX();
                    float newposy = dragger == null ? ivehic.getPosY() : dragger.getPosY();
                    tilex = (int)newposx / 4;
                    tiley = (int)newposy / 4;
                  }
                  catch (Exception ex)
                  {
                    logger.log(Level.WARNING, ex.getMessage(), ex);
                  }
                }
              }
            }
          }
          catch (NoSuchItemException nsi)
          {
            setLastVehicle(-10L, (byte)-1);
          }
        }
      }
      getMovementScheme().offZ = 0.0F;
    }
    this.vehicle = _vehicle;
    this.seatType = _seatType;
    if (!isPlayer()) {
      setLastVehicle(_vehicle, _seatType);
    }
    if (this.vehicle != -10L)
    {
      removeIllusion();
      Vehicle vehic = Vehicles.getVehicleForId(this.vehicle);
      if (vehic != null)
      {
        clearDestination();
        setFarwalkerSeconds((byte)0);
        getMovementScheme().setFarwalkerMoveMod(false);
        this.movementScheme.setEncumbered(false);
        this.movementScheme.setBaseModifier(1.0F);
        setStealth(false);
        float offx = 0.0F;
        float offy = 0.0F;
        for (int x = 0; x < vehic.seats.length; x++) {
          if (vehic.seats[x].occupant == getWurmId())
          {
            offx = vehic.seats[x].offx;
            offy = vehic.seats[x].offy;
            
            break;
          }
        }
        if (vehic.creature) {
          try
          {
            Creature lVehicle = Server.getInstance().getCreature(this.vehicle);
            float r = (-lVehicle.getStatus().getRotation() + 180.0F) * 3.1415927F / 180.0F;
            
            float s = (float)Math.sin(r);
            float c = (float)Math.cos(r);
            float xo = s * -offx - c * -offy;
            float yo = c * -offx + s * -offy;
            float newposx = lVehicle.getPosX() + xo;
            float newposy = lVehicle.getPosY() + yo;
            getMovementScheme().setVehicleRotation(lVehicle.getStatus().getRotation());
            getStatus().setRotation(lVehicle.getStatus().getRotation());
            setBridgeId(lVehicle.getBridgeId());
            setTeleportPoints(newposx, newposy, lVehicle.getLayer(), lVehicle.getFloorLevel());
            if ((getVisionArea() != null) && ((int)newposx >> 2 == getTileX()) && ((int)newposy >> 2 == getTileY()))
            {
              embark(newposx, newposy, getPositionZ(), getStatus().getRotation(), this.teleportLayer, "Embarking " + vehic.name, null, lVehicle, vehic);
            }
            else if (!getCommunicator().stillLoggingIn())
            {
              int tx = getTileX();
              int ty = getTileY();
              int nx = (int)newposx >> 2;
              int ny = (int)newposy >> 2;
              try
              {
                if ((hasLink()) && (getVisionArea() != null))
                {
                  getVisionArea().move(nx - tx, ny - ty);
                  embark(newposx, newposy, getPositionZ(), getStatus().getRotation(), this.teleportLayer, "Embarking " + vehic.name, null, lVehicle, vehic);
                  
                  getVisionArea().linkZones(nx - tx, ny - ty);
                }
              }
              catch (IOException ex)
              {
                startTeleporting(true);
                lVehicle.setLeader(null);
                lVehicle.addRider(getWurmId());
                
                sendMountData();
                if (isVehicleCommander()) {
                  getCommunicator().sendTeleport(true, false, vehic.commandType);
                } else {
                  getCommunicator().sendTeleport(false, false, (byte)0);
                }
              }
            }
            else
            {
              startTeleporting(true);
              lVehicle.setLeader(null);
              lVehicle.addRider(getWurmId());
              
              sendMountData();
              if (isVehicleCommander()) {
                getCommunicator().sendTeleport(true, false, vehic.commandType);
              } else {
                getCommunicator().sendTeleport(false, false, (byte)0);
              }
            }
          }
          catch (NoSuchCreatureException nsi)
          {
            logger.log(Level.WARNING, getName() + " " + nsi.getMessage(), nsi);
          }
          catch (NoSuchPlayerException nsp)
          {
            logger.log(Level.WARNING, getName() + " " + nsp.getMessage(), nsp);
          }
        } else {
          try
          {
            Item lVehicle = Items.getItem(vehic.wurmid);
            
            float r = (-lVehicle.getRotation() + 180.0F) * 3.1415927F / 180.0F;
            
            float s = (float)Math.sin(r);
            float c = (float)Math.cos(r);
            float xo = s * -offx - c * -offy;
            float yo = c * -offx + s * -offy;
            float newposx = lVehicle.getPosX() + xo;
            float newposy = lVehicle.getPosY() + yo;
            getMovementScheme().setVehicleRotation(lVehicle.getRotation());
            getStatus().setRotation(lVehicle.getRotation());
            
            setBridgeId(lVehicle.getBridgeId());
            if ((getVisionArea() != null) && ((int)newposx >> 2 == getTileX()) && ((int)newposy >> 2 == getTileY()))
            {
              embark(newposx, newposy, getPositionZ(), getStatus().getRotation(), this.teleportLayer, "Embarking " + vehic.name, lVehicle, null, vehic);
            }
            else
            {
              setTeleportPoints(newposx, newposy, lVehicle.isOnSurface() ? 0 : -1, lVehicle.getFloorLevel());
              if (isVehicleCommander())
              {
                if (lVehicle.getKingdom() != getKingdomId())
                {
                  Server.getInstance().broadCastAction(
                    LoginHandler.raiseFirstLetter(lVehicle.getName()) + " is now the property of " + 
                    Kingdoms.getNameFor(getKingdomId()) + "!", this, 10);
                  String message = StringUtil.format("You declare the %s the property of %s.", new Object[] {lVehicle
                  
                    .getName(), 
                    Kingdoms.getNameFor(getKingdomId()) });
                  getCommunicator().sendNormalServerMessage(message);
                  lVehicle.setLastOwnerId(getWurmId());
                }
                else if (Servers.isThisAChaosServer())
                {
                  Village v = Villages.getVillageForCreature(lVehicle.getLastOwnerId());
                  if ((v == null) || (v.isEnemy(getCitizenVillage())))
                  {
                    String vehname = getName();
                    if (getCitizenVillage() != null) {
                      vehname = getCitizenVillage().getName();
                    }
                    Server.getInstance().broadCastAction(
                      LoginHandler.raiseFirstLetter(lVehicle.getName()) + " is now the property of " + vehname + "!", this, 10);
                    
                    String message = StringUtil.format("You declare the %s the property of %s.", new Object[] {lVehicle
                    
                      .getName(), vehname });
                    
                    getCommunicator().sendNormalServerMessage(message);
                    lVehicle.setLastOwnerId(getWurmId());
                  }
                }
                lVehicle.setAuxData(getKingdomId());
                setEmbarkTeleportVehicle(newposx, newposy, vehic, lVehicle);
              }
              else
              {
                setEmbarkTeleportVehicle(newposx, newposy, vehic, lVehicle);
              }
            }
          }
          catch (NoSuchItemException nsi)
          {
            logger.log(Level.WARNING, getName() + " " + nsi.getMessage(), nsi);
          }
        }
      }
    }
    else if (teleport)
    {
      if ((tilex > -1) || (tiley > -1))
      {
        int ntx = tilex - getTileX();
        int nty = tiley - getTileY();
        
        float posz = getStatus().getPositionZ();
        posz = Zones.calculatePosZ(getPosX(), getPosY(), getCurrentTile(), 
          isOnSurface(), false, posz, this, getBridgeId());
        try
        {
          if ((hasLink()) && (getVisionArea() != null))
          {
            getVisionArea().move(ntx, nty);
            intraTeleport(tilex * 4 + 2, tiley * 4 + 2, posz, getStatus().getRotation(), 
              getLayer(), "left vehicle");
            
            getVisionArea().linkZones(ntx, nty);
          }
        }
        catch (IOException ex)
        {
          setTeleportPoints((short)tilex, (short)tiley, getLayer(), 0);
          startTeleporting(false);
          getCommunicator().sendTeleport(false, true, (byte)0);
        }
      }
      else
      {
        Structure struct = getCurrentTile() != null ? getCurrentTile().getStructure() : Structures.getStructureForTile(getTileX(), getTileY(), isOnSurface());
        if ((struct == null) || (struct.mayPass(this)))
        {
          float posz = getStatus().getPositionZ();
          posz = Zones.calculatePosZ(getPosX(), getPosY(), getCurrentTile(), 
            isOnSurface(), false, posz, this, getBridgeId());
          intraTeleport(getStatus().getPositionX(), getStatus().getPositionY(), posz, getStatus().getRotation(), 
            getLayer(), "left vehicle");
        }
      }
      getMovementScheme().addWindImpact((byte)0);
      calcBaseMoveMod();
      getMovementScheme().commandingBoat = false;
      getCurrentTile().sendAttachCreature(getWurmId(), -1L, 0.0F, 0.0F, 0.0F, 0);
    }
    else
    {
      if (!getMovementScheme().isIntraTeleporting())
      {
        getMovementScheme().addWindImpact((byte)0);
        calcBaseMoveMod();
        getMovementScheme().setMooredMod(false);
        getMovementScheme().commandingBoat = false;
      }
      getCurrentTile().sendAttachCreature(getWurmId(), -1L, 0.0F, 0.0F, 0.0F, 0);
    }
  }
  
  public void intraTeleport(float posx, float posy, float posz, float aRot, int layer, String reason)
  {
    if ((Servers.isThisATestServer()) && (reason.contains("in rock")))
    {
      posx = getMovementScheme().xOld;
      posy = getMovementScheme().yOld;
    }
    this.teleports += 1;
    if (isDead()) {
      return;
    }
    posx = Math.max(0.0F, Math.min(posx, Zones.worldMeterSizeX - 1.0F));
    posy = Math.max(0.0F, Math.min(posy, Zones.worldMeterSizeY - 1.0F));
    VolaTile t = getCurrentTile();
    if (t != null) {
      t.deleteCreatureQuick(this);
    } else {
      logger.log(Level.INFO, getName() + " no current tile when intrateleporting.");
    }
    getStatus().setPositionX(posx);
    getStatus().setPositionY(posy);
    getStatus().setPositionZ(posz);
    getStatus().setRotation(aRot);
    if ((layer == 0) && (Zones.getTextureForTile((int)posx >> 2, (int)posy >> 2, layer) == Tiles.Tile.TILE_HOLE.id)) {
      layer = -1;
    }
    boolean visionAreaInitialized = false;
    if (getVisionArea() != null) {
      visionAreaInitialized = getVisionArea().isInitialized();
    }
    if ((!reason.contains("Embarking")) && (!reason.contains("left vehicle")))
    {
      logger.log(Level.INFO, getName() + " intrateleport to " + posx + "," + posy + ", " + posz + ", layer " + layer + " currentTile:null=" + (t == null) + " reason=" + reason + " hasVisionArea=" + (
        getVisionArea() != null) + ", initialized=" + visionAreaInitialized + " vehicle=" + this.vehicle, new Exception());
      if (getPower() >= 3) {
        getCommunicator().sendAlertServerMessage("IntraTeleporting " + reason);
      }
    }
    getMovementScheme().setPosition(posx, posy, posz, aRot, layer);
    
    putInWorld();
    getMovementScheme().haltSpeedModifier();
    
    getCommunicator().setReady(false);
    getMovementScheme().setMooredMod(false);
    addCarriedWeight(0);
    try
    {
      sendActionControl("", false, 0);
      this.actions.stopCurrentAction(false);
    }
    catch (NoSuchActionException localNoSuchActionException) {}
    getMovementScheme().commandingBoat = false;
    getMovementScheme().addWindImpact((byte)0);
    
    getCommunicator().sendTeleport(true);
    disembark(false);
    getMovementScheme().addIntraTeleport(getTeleportCounter());
  }
  
  public Vector3f getActualPosVehicle()
  {
    Vector3f toReturn = new Vector3f(getPosX(), getPosY(), getPositionZ());
    if (this.vehicle != -10L)
    {
      Vehicle vehic = Vehicles.getVehicleForId(this.vehicle);
      if (vehic != null)
      {
        float offx = 0.0F;
        float offy = 0.0F;
        for (int x = 0; x < vehic.seats.length; x++) {
          if (vehic.seats[x].occupant == getWurmId())
          {
            offx = vehic.seats[x].offx;
            offy = vehic.seats[x].offy;
            break;
          }
        }
        if (vehic.creature) {
          try
          {
            Creature lVehicle = Server.getInstance().getCreature(this.vehicle);
            float r = (-lVehicle.getStatus().getRotation() + 180.0F) * 3.1415927F / 180.0F;
            float s = (float)Math.sin(r);
            float c = (float)Math.cos(r);
            float xo = s * -offx - c * -offy;
            float yo = c * -offx + s * -offy;
            float newposx = lVehicle.getPosX() + xo;
            float newposy = lVehicle.getPosY() + yo;
            
            toReturn.setX(newposx);
            toReturn.setY(newposy);
          }
          catch (NoSuchCreatureException|NoSuchPlayerException localNoSuchCreatureException) {}
        } else {
          try
          {
            Item lVehicle = Items.getItem(vehic.wurmid);
            float r = (-lVehicle.getRotation() + 180.0F) * 3.1415927F / 180.0F;
            float s = (float)Math.sin(r);
            float c = (float)Math.cos(r);
            float xo = s * -offx - c * -offy;
            float yo = c * -offx + s * -offy;
            float newposx = lVehicle.getPosX() + xo;
            float newposy = lVehicle.getPosY() + yo;
            
            toReturn.setX(newposx);
            toReturn.setY(newposy);
          }
          catch (NoSuchItemException localNoSuchItemException) {}
        }
      }
    }
    return toReturn;
  }
  
  protected VolaTile getActualTileVehicle()
  {
    Vector3f v = getActualPosVehicle();
    int nx = (int)v.x >> 2;
    int ny = (int)v.y >> 2;
    
    return Zones.getOrCreateTile(nx, ny, isOnSurface());
  }
  
  protected void setEmbarkTeleportVehicle(float newposx, float newposy, Vehicle vehic, Item lVehicle)
  {
    if (!getCommunicator().stillLoggingIn())
    {
      int tx = getTileX();
      int ty = getTileY();
      int nx = (int)newposx >> 2;
      int ny = (int)newposy >> 2;
      try
      {
        if (((hasLink()) || (isWagoner())) && (getVisionArea() != null))
        {
          getVisionArea().move(nx - tx, ny - ty);
          embark(newposx, newposy, getPositionZ(), getStatus().getRotation(), this.teleportLayer, "Embarking " + vehic.name, lVehicle, null, vehic);
          
          getVisionArea().linkZones(nx - tx, ny - ty);
        }
      }
      catch (IOException ex)
      {
        startTeleporting(true);
        sendMountData();
        getCommunicator().sendTeleport(true, false, vehic.commandType);
      }
    }
    else
    {
      startTeleporting(true);
      sendMountData();
      if (isVehicleCommander()) {
        getCommunicator().sendTeleport(true, false, vehic.commandType);
      } else {
        getCommunicator().sendTeleport(false, false, (byte)0);
      }
    }
  }
  
  private void embark(float posx, float posy, float posz, float aRot, int layer, String reason, @Nullable Item lVehicle, Creature cVehicle, Vehicle vehic)
  {
    if (!isVehicleCommander()) {
      stopLeading();
    }
    VolaTile t = getCurrentTile();
    if (t != null) {
      t.deleteCreatureQuick(this);
    } else {
      logger.log(Level.INFO, getName() + " no current tile when intrateleporting.");
    }
    getStatus().setPositionX(posx);
    getStatus().setPositionY(posy);
    getStatus().setPositionZ(posz);
    getStatus().setRotation(aRot);
    if ((layer == 0) && (Zones.getTextureForTile((int)posx >> 2, (int)posy >> 2, layer) == Tiles.Tile.TILE_HOLE.id)) {
      layer = -1;
    }
    boolean setOffZ = false;
    if (this.mountAction != null) {
      setOffZ = true;
    }
    if (setOffZ)
    {
      if (lVehicle != null)
      {
        float targetZ = lVehicle.getPosZ();
        this.status.setPositionZ(targetZ + this.mountAction.getOffZ());
      }
      else if (cVehicle != null)
      {
        float cretZ = cVehicle.getStatus().getPositionZ();
        this.status.setPositionZ(cretZ + this.mountAction.getOffZ());
      }
      getMovementScheme().offZ = this.mountAction.getOffZ();
    }
    getMovementScheme().setPosition(posx, posy, this.status.getPositionZ(), this.status.getRotation(), getLayer());
    putInWorld();
    getMovementScheme().haltSpeedModifier();
    
    getCommunicator().setReady(false);
    if (this.status.isTrading()) {
      this.status.getTrade().end(this, false);
    }
    if (this.movementScheme.draggedItem != null) {
      MethodsItems.stopDragging(this, this.movementScheme.draggedItem);
    }
    try
    {
      sendActionControl("", false, 0);
      this.actions.stopCurrentAction(false);
    }
    catch (NoSuchActionException localNoSuchActionException) {}
    this._enterVehicle = true;
    if (cVehicle != null)
    {
      cVehicle.setLeader(null);
      cVehicle.addRider(getWurmId());
    }
    sendMountData();
    if (isVehicleCommander())
    {
      if (lVehicle != null)
      {
        if (lVehicle.getKingdom() != getKingdomId())
        {
          Server.getInstance().broadCastAction(
            LoginHandler.raiseFirstLetter(lVehicle.getName()) + " is now the property of " + 
            Kingdoms.getNameFor(getKingdomId()) + "!", this, 10);
          String message = StringUtil.format("You declare the %s the property of %s.", new Object[] {lVehicle
            .getName(), Kingdoms.getNameFor(getKingdomId()) });
          getCommunicator().sendNormalServerMessage(message);
          lVehicle.setLastOwnerId(getWurmId());
        }
        else if (Servers.isThisAChaosServer())
        {
          Village v = Villages.getVillageForCreature(lVehicle.getLastOwnerId());
          if ((v == null) || (v.isEnemy(getCitizenVillage())))
          {
            String vehname = getName();
            if (getCitizenVillage() != null) {
              vehname = getCitizenVillage().getName();
            }
            Server.getInstance().broadCastAction(
              LoginHandler.raiseFirstLetter(lVehicle.getName()) + " is now the property of " + vehname + "!", this, 10);
            
            String message = StringUtil.format("You declare the %s the property of %s.", new Object[] {lVehicle
              .getName(), vehname });
            getCommunicator().sendNormalServerMessage(message);
            lVehicle.setLastOwnerId(getWurmId());
          }
        }
        lVehicle.setAuxData(getKingdomId());
      }
      getCommunicator().sendTeleport(true, false, vehic.commandType);
    }
    else
    {
      getCommunicator().sendTeleport(true, false, (byte)0);
    }
    getMovementScheme().addIntraTeleport(getTeleportCounter());
  }
  
  public void disembark(boolean teleport)
  {
    disembark(teleport, -1, -1);
  }
  
  public void disembark(boolean teleport, int tilex, int tiley)
  {
    if (this.vehicle > -10L)
    {
      Vehicle vehic = Vehicles.getVehicleForId(this.vehicle);
      if (vehic != null)
      {
        if (vehic.pilotId == getWurmId())
        {
          setVehicleCommander(false);
          
          vehic.pilotId = -10L;
          getCommunicator().setVehicleController(-1L, -1L, 0.0F, 0.0F, 0.0F, -2000.0F, 2000.0F, 2000.0F, 0.0F, 0);
          try
          {
            Item item = Items.getItem(this.vehicle);
            item.savePosition();
          }
          catch (Exception localException) {}
        }
        else if (vehic.pilotId != -10L)
        {
          try
          {
            Item item = Items.getItem(this.vehicle);
            item.savePosition();
            Creature pilot = Server.getInstance().getCreature(vehic.pilotId);
            if ((!vehic.creature) && (item.isBoat())) {
              pilot.getMovementScheme().addMountSpeed((short)vehic.calculateNewBoatSpeed(true));
            } else if (vehic.creature) {
              vehic.updateDraggedSpeed(true);
            }
          }
          catch (WurmServerException localWurmServerException) {}catch (Exception localException1) {}
        }
        String vehicName = Vehicle.getVehicleName(vehic);
        if (vehic.isChair())
        {
          getCommunicator().sendNormalServerMessage(StringUtil.format("You get up from the %s.", new Object[] { vehicName }));
          Server.getInstance().broadCastAction(StringUtil.format("%s gets up from the %s.", new Object[] { getName(), vehicName }), this, 5);
        }
        else
        {
          getCommunicator().sendNormalServerMessage(StringUtil.format("You leave the %s.", new Object[] { vehicName }));
          Server.getInstance().broadCastAction(StringUtil.format("%s leaves the %s.", new Object[] { getName(), vehicName }), this, 5);
        }
        setVehicle(-10L, teleport, (byte)-1, tilex, tiley);
        int found = 0;
        for (int x = 0; x < vehic.seats.length; x++) {
          if (vehic.seats[x].occupant == getWurmId())
          {
            vehic.seats[x].occupant = -10L;
            found++;
          }
        }
        if (found > 1) {
          logger.log(Level.INFO, 
            StringUtil.format("%s was occupying %d seats on %s.", new Object[] {
            getName(), Integer.valueOf(found), vehicName }));
        }
      }
      else
      {
        setVehicle(-10L, teleport, (byte)-1, tilex, tiley);
      }
    }
  }
  
  public int getTeleportCounter()
  {
    return 0;
  }
  
  public long getVehicle()
  {
    return this.vehicle;
  }
  
  public byte getSeatType()
  {
    return this.seatType;
  }
  
  public Vehicle getMountVehicle()
  {
    return Vehicles.getVehicleForId(getWurmId());
  }
  
  public boolean isVehicleCommander()
  {
    return this.isVehicleCommander;
  }
  
  public double getVillageSkillModifier()
  {
    return 0.0D;
  }
  
  public String getEmotePrefix()
  {
    return this.template.getName();
  }
  
  public void playAnimation(String animationName, boolean looping)
  {
    if (this.currentTile != null) {
      this.currentTile.sendAnimation(this, animationName, looping, -10L);
    }
  }
  
  public void playAnimation(String animationName, boolean looping, long aTarget)
  {
    if (this.currentTile != null) {
      this.currentTile.sendAnimation(this, animationName, looping, aTarget);
    }
  }
  
  public void sendStance(byte stance)
  {
    if (this.currentTile != null) {
      this.currentTile.sendStance(this, stance);
    }
  }
  
  public void sendDamage(float damPercent)
  {
    if (this.currentTile != null) {
      this.currentTile.sendCreatureDamage(this, damPercent);
    }
  }
  
  public int getEnemyPresense()
  {
    return 0;
  }
  
  public boolean mayMute()
  {
    return false;
  }
  
  public boolean hasNoReimbursement()
  {
    return true;
  }
  
  public boolean isDeathProtected()
  {
    return false;
  }
  
  public long mayChangeVillageInMillis()
  {
    return 0L;
  }
  
  public boolean hasGlow()
  {
    if (getPower() > 0) {
      return true;
    }
    return this.template.isGlowing();
  }
  
  public boolean mayOpportunityAttack()
  {
    if (isStunned()) {
      return false;
    }
    if (this.opportunityAttackCounter > 0) {
      return false;
    }
    return getCombatHandler().getOpportunityAttacks() < getFightingSkill().getKnowledge(0.0D) / 10.0D;
  }
  
  public boolean opportunityAttack(Creature creature)
  {
    if (creature.isInvulnerable()) {
      return false;
    }
    if (!creature.isVisibleTo(this)) {
      return false;
    }
    if ((isPlayer()) && (creature.isPlayer()) && (!Servers.isThisAPvpServer()) && (!isDuelOrSpar(creature))) {
      return false;
    }
    if ((isFighting()) || (creature.getWurmId() == this.target)) {
      if ((!isPlayer()) || (!creature.isPlayer()))
      {
        if (isBridgeBlockingAttack(creature, false)) {
          return false;
        }
        if (mayOpportunityAttack()) {
          if ((getLayer() == creature.getLayer()) && 
            (getMindSpeed().skillCheck(getCombatHandler().getOpportunityAttacks() * 10, 0.0D, false, 1.0F) > 0.0D))
          {
            if (this.opponent == null) {
              setOpponent(creature);
            }
            return getCombatHandler().attack(creature, 10, true, 2.0F, null);
          }
        }
      }
    }
    return false;
  }
  
  public boolean isSparring(Creature _opponent)
  {
    return false;
  }
  
  public boolean isDuelling(Creature _opponent)
  {
    return false;
  }
  
  public boolean isDuelOrSpar(Creature _opponent)
  {
    return false;
  }
  
  public boolean isStealth()
  {
    return this.status.stealth;
  }
  
  public void setStealth(boolean stealth)
  {
    if (this.status.setStealth(stealth))
    {
      if (stealth)
      {
        this.stealthBreakers = new HashSet();
        if (isPlayer()) {
          getCommunicator().sendNormalServerMessage("You attempt to hide from others.", (byte)4);
        }
        this.movementScheme.setStealthMod(true);
      }
      else
      {
        if (this.stealthBreakers != null) {
          this.stealthBreakers.clear();
        }
        getCommunicator().sendNormalServerMessage("You no longer hide.", (byte)4);
        this.movementScheme.setStealthMod(false);
      }
      checkInvisDetection();
    }
  }
  
  public void checkInvisDetection()
  {
    if (getBody().getBodyItem() != null) {
      getCurrentTile().checkVisibility(this, (!isVisible()) || (isStealth()));
    }
  }
  
  public boolean visibilityCheck(Creature watcher, float difficultyModifier)
  {
    if (!isVisible()) {
      return (getPower() > 0) && (getPower() <= watcher.getPower());
    }
    if (isStealth())
    {
      if ((getPower() > 0) && (getPower() <= watcher.getPower())) {
        return true;
      }
      if (getPower() < watcher.getPower()) {
        return true;
      }
      if (watcher.isUnique()) {
        return true;
      }
      if (this.stealthBreakers != null) {
        if (this.stealthBreakers.contains(Long.valueOf(watcher.getWurmId()))) {
          return true;
        }
      }
      int distModifier = (int)Math.max(Math.abs(watcher.getPosX() - getPosX()), 
        Math.abs(watcher.getPosY() - getPosY()));
      if ((watcher.getCurrentTile() == getCurrentTile()) || 
        (watcher.isDetectInvis()) || 
        (Server.rand.nextInt((int)(100.0F + difficultyModifier + distModifier)) < watcher.getDetectDangerBonus() / 5.0F) || 
        (watcher.getMindLogical().skillCheck(getBodyControl() + difficultyModifier + distModifier, 0.0D, true, 1.0F) > 0.0D))
      {
        if (this.stealthBreakers == null) {
          this.stealthBreakers = new HashSet();
        }
        this.stealthBreakers.add(Long.valueOf(watcher.getWurmId()));
        return true;
      }
      return false;
    }
    return true;
  }
  
  public boolean isDetectInvis()
  {
    if (this.template.isDetectInvis()) {
      return true;
    }
    if (this.status.detectInvisCounter > 0) {
      return true;
    }
    return false;
  }
  
  public boolean isVisibleTo(Creature watcher)
  {
    return isVisibleTo(watcher, false);
  }
  
  public boolean isVisibleTo(Creature watcher, boolean ignoreStealth)
  {
    if (!isVisible()) {
      return (getPower() > 0) && (getPower() <= watcher.getPower());
    }
    if ((isStealth()) && (!ignoreStealth))
    {
      if ((getPower() > 0) && (getPower() <= watcher.getPower())) {
        return true;
      }
      if (getPower() < watcher.getPower()) {
        return true;
      }
      if ((watcher.isUnique()) || (watcher.isDetectInvis())) {
        return true;
      }
      if ((this.stealthBreakers != null) && (this.stealthBreakers.contains(Long.valueOf(watcher.getWurmId())))) {
        return true;
      }
      return false;
    }
    return true;
  }
  
  public void addVisionModifier(DoubleValueModifier modifier)
  {
    if (this.visionModifiers == null) {
      this.visionModifiers = new HashSet();
    }
    this.visionModifiers.add(modifier);
  }
  
  public void removeVisionModifier(DoubleValueModifier modifier)
  {
    if (this.visionModifiers != null) {
      this.visionModifiers.remove(modifier);
    }
  }
  
  public double getVisionMod()
  {
    if (this.visionModifiers == null) {
      return 0.0D;
    }
    double doubleModifier = 0.0D;
    for (DoubleValueModifier lDoubleValueModifier : this.visionModifiers) {
      doubleModifier += lDoubleValueModifier.getModifier();
    }
    return doubleModifier;
  }
  
  public int[] getCombatMoves()
  {
    return this.template.getCombatMoves();
  }
  
  public boolean isGuide()
  {
    return this.template.isTutorial();
  }
  
  public int getTutorialLevel()
  {
    return 9999;
  }
  
  public boolean skippedTutorial()
  {
    return false;
  }
  
  public String getCurrentMissionInstruction()
  {
    return "";
  }
  
  public boolean isNoSkillgain()
  {
    return (this.template.isNoSkillgain()) || (isBred()) || (!isPaying());
  }
  
  public boolean isAutofight()
  {
    return false;
  }
  
  public final float getDamageModifier(boolean pvp)
  {
    float damMod = (float)(120.0D - getStrengthSkill()) / 100.0F;
    if ((isPlayer()) && (pvp) && (Servers.localServer.PVPSERVER))
    {
      damMod = (float)(1.0D - 0.15D * Math.log(Math.max(20.0D, getStrengthSkill()) * 0.800000011920929D - 15.0D));
      damMod = Math.max(Math.min(damMod, 1.0F), 0.2F);
    }
    if (hasSpellEffect((byte)96)) {
      damMod *= 1.1F;
    }
    if (getCultist() != null)
    {
      float percent = getCultist().getHalfDamagePercentage();
      if (percent > 0.0F) {
        if (isChampion())
        {
          float red = 1.0F - 0.1F * percent;
          damMod *= red;
        }
        else
        {
          float red = 1.0F - 0.3F * percent;
          damMod *= red;
        }
      }
    }
    return damMod;
  }
  
  public String toString()
  {
    return "Creature [id: " + this.id + ", name: " + this.name + ", Tile: " + this.currentTile + ", Template: " + this.template + ", Status: " + this.status + ']';
  }
  
  public void sendToLoggers(String tolog)
  {
    sendToLoggers(tolog, (byte)2);
  }
  
  public void sendToLoggers(String tolog, byte restrictedToPower)
  {
    if (this.loggerCreature1 != -10L) {
      try
      {
        Creature receiver = Server.getInstance().getCreature(this.loggerCreature1);
        receiver.getCommunicator().sendLogMessage(getName() + " [" + tolog + "]");
      }
      catch (Exception ex)
      {
        this.loggerCreature1 = -10L;
      }
    }
    if (this.loggerCreature2 != -10L) {
      try
      {
        Creature receiver = Server.getInstance().getCreature(this.loggerCreature2);
        receiver.getCommunicator().sendLogMessage(getName() + " [" + tolog + "]");
      }
      catch (Exception ex)
      {
        this.loggerCreature2 = -10L;
      }
    }
  }
  
  public long getAppointments()
  {
    return 0L;
  }
  
  public boolean isFloating()
  {
    return this.template.isFloating();
  }
  
  public boolean hasAppointment(int aid)
  {
    return false;
  }
  
  public boolean isKing()
  {
    return false;
  }
  
  public final boolean isEligibleForKingdomBonus()
  {
    if (hasCustomKingdom())
    {
      King king = King.getKing(getKingdomId());
      if (king != null) {
        return king.currentLand > 2.0F;
      }
      return false;
    }
    return true;
  }
  
  public String getAppointmentTitles()
  {
    return "";
  }
  
  public boolean isRoyalAnnouncer()
  {
    return King.isOfficial(1510, getWurmId(), getKingdomId());
  }
  
  public boolean isRoyalChef()
  {
    return King.isOfficial(1509, getWurmId(), getKingdomId());
  }
  
  public boolean isRoyalPriest()
  {
    return King.isOfficial(1506, getWurmId(), getKingdomId());
  }
  
  public boolean isRoyalSmith()
  {
    return King.isOfficial(1503, getWurmId(), getKingdomId());
  }
  
  public boolean isRoyalExecutioner()
  {
    return King.isOfficial(1508, getWurmId(), getKingdomId());
  }
  
  public boolean isEconomicAdvisor()
  {
    return King.isOfficial(1505, getWurmId(), getKingdomId());
  }
  
  public boolean isInformationOfficer()
  {
    return King.isOfficial(1500, getWurmId(), getKingdomId());
  }
  
  public String getAnnounceString()
  {
    return getName() + '!';
  }
  
  public boolean isAppointed()
  {
    return false;
  }
  
  public boolean isArcheryMode()
  {
    return false;
  }
  
  public MusicPlayer getMusicPlayer()
  {
    return this.musicPlayer;
  }
  
  public int getPushCounter()
  {
    return 200;
  }
  
  public Seat getSeat()
  {
    return null;
  }
  
  public void setMountAction(@Nullable MountAction act)
  {
    this.mountAction = act;
  }
  
  public byte getCRCounterBonus()
  {
    return 0;
  }
  
  public boolean isNoAttackVehicles()
  {
    return !this.template.attacksVehicles;
  }
  
  public int getMaxNumActions()
  {
    return 10;
  }
  
  public int hashCode()
  {
    int prime = 31;
    int result = 1;
    result = 31 * result + (int)(this.id ^ this.id >>> 32);
    result = 31 * result + (isPlayer() ? 1231 : 1237);
    return result;
  }
  
  public boolean equals(Object obj)
  {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof Creature)) {
      return false;
    }
    Creature other = (Creature)obj;
    if (this.id != other.id) {
      return false;
    }
    if (isPlayer() != other.isPlayer()) {
      return false;
    }
    return true;
  }
  
  public boolean seesPlayerAssistantWindow()
  {
    return false;
  }
  
  public void setHitched(@Nullable Vehicle _hitched, boolean loading)
  {
    this.hitchedTo = _hitched;
    if (this.hitchedTo != null)
    {
      clearOrders();
      this.seatType = 2;
      if (!loading) {
        getStatus().setVehicle(this.hitchedTo.wurmid, this.seatType);
      }
    }
    else
    {
      this.seatType = -1;
      getStatus().setVehicle(-10L, this.seatType);
    }
  }
  
  public Vehicle getHitched()
  {
    return this.hitchedTo;
  }
  
  public boolean isPlayerAssistant()
  {
    return false;
  }
  
  public boolean isVehicle()
  {
    return this.template.isVehicle;
  }
  
  public Set<Long> getRiders()
  {
    return this.riders;
  }
  
  public boolean isRidden()
  {
    return (this.riders != null) && (this.riders.size() > 0);
  }
  
  public boolean isRiddenBy(long wurmid)
  {
    return (this.riders != null) && (this.riders.contains(Long.valueOf(wurmid)));
  }
  
  public void addRider(long newrider)
  {
    if (this.riders == null) {
      this.riders = new HashSet();
    }
    this.riders.add(Long.valueOf(newrider));
  }
  
  public void removeRider(long lostrider)
  {
    if (this.riders == null) {
      this.riders = new HashSet();
    }
    this.riders.remove(Long.valueOf(lostrider));
  }
  
  protected void forceMountSpeedChange()
  {
    this.mountPollCounter = 0;
    pollMount();
  }
  
  boolean switchv = true;
  
  private void pollMount()
  {
    if (isRidden()) {
      if ((this.mountPollCounter <= 0) || (Server.rand.nextInt(100) == 0))
      {
        Vehicle vehic = Vehicles.getVehicleForId(getWurmId());
        if (vehic != null) {
          try
          {
            Creature rider = Server.getInstance().getCreature(vehic.getPilotSeat().occupant);
            byte val = vehic.calculateNewMountSpeed(this, false);
            if (this.switchv) {
              val = (byte)(val - 1);
            }
            this.switchv = (!this.switchv);
            rider.getMovementScheme().addMountSpeed((short)val);
          }
          catch (NoSuchCreatureException localNoSuchCreatureException) {}catch (NoSuchPlayerException localNoSuchPlayerException) {}
        }
        this.mountPollCounter = 20;
      }
      else
      {
        this.mountPollCounter -= 1;
      }
    }
  }
  
  public boolean mayChangeSpeed()
  {
    return this.mountPollCounter <= 0;
  }
  
  public float getMountSpeedPercent(boolean mounting)
  {
    float factor = 0.5F;
    if (getStatus().getHunger() < 45000) {
      factor += 0.2F;
    }
    if (getStatus().getHunger() < 10000) {
      factor += 0.1F;
    }
    if (getStatus().damage < 10000) {
      factor += 0.1F;
    } else if (getStatus().damage > 20000) {
      factor -= 0.5F;
    } else if (getStatus().damage > 45000) {
      factor -= 0.7F;
    }
    if ((isHorse()) || (isUnicorn()))
    {
      float hbonus = calcHorseShoeBonus(mounting);
      sendToLoggers("Horse shoe bonus " + hbonus + " so factor from " + factor + " to " + (factor + hbonus));
      factor += hbonus;
    }
    float tperc = getTraitMovePercent(mounting);
    
    sendToLoggers("Trait move percent= " + tperc + " so factor from " + factor + " to " + (factor + tperc));
    factor += tperc;
    if (isRidden())
    {
      try
      {
        if ((Servers.isThisAnEpicOrChallengeServer()) && (getBonusForSpellEffect((byte)22) > 0.0F)) {
          factor -= 0.2F * (getBonusForSpellEffect((byte)22) / 100.0F);
        }
      }
      catch (Exception ex)
      {
        ex.printStackTrace();
      }
      Item torsoItem = getWornItem((byte)2);
      if (torsoItem != null) {
        if ((torsoItem.isSaddleLarge()) || (torsoItem.isSaddleNormal()))
        {
          factor += Math.max(10.0F, torsoItem.getCurrentQualityLevel()) / 1000.0F;
          factor += torsoItem.getRarity() * 0.03F;
          factor += torsoItem.getSpellSpeedBonus() / 2000.0F;
          if ((!mounting) && (!this.ignoreSaddleDamage)) {
            torsoItem.setDamage(torsoItem.getDamage() + 0.001F);
          }
          this.ignoreSaddleDamage = false;
        }
      }
      sendToLoggers("After saddle move percent= " + factor);
      factor *= getMovementScheme().getSpeedModifier();
      sendToLoggers("After speedModifier " + getMovementScheme().getSpeedModifier() + " move percent= " + factor);
    }
    return factor;
  }
  
  private int getCarriedMountWeight()
  {
    int currWeight = getCarriedWeight();
    int bagsWeight = getSaddleBagsCarriedWeight();
    currWeight -= bagsWeight;
    if (isRidden()) {
      for (Long lLong : this.riders) {
        try
        {
          Creature _rider = Server.getInstance().getCreature(lLong.longValue());
          currWeight += Math.max(30000, _rider.getStatus().fat * 1000);
          currWeight += _rider.getCarriedWeight();
        }
        catch (NoSuchCreatureException localNoSuchCreatureException) {}catch (NoSuchPlayerException localNoSuchPlayerException) {}
      }
    }
    return currWeight;
  }
  
  public boolean hasTraits()
  {
    return this.status.traits != 0L;
  }
  
  public boolean hasTrait(int traitbit)
  {
    if (this.status.traits != 0L) {
      return this.status.isTraitBitSet(traitbit);
    }
    return false;
  }
  
  public boolean hasAbility(int abilityBit)
  {
    return false;
  }
  
  public boolean hasFlag(int flagBit)
  {
    return false;
  }
  
  public String getTaggedItemName()
  {
    return "";
  }
  
  public long getTaggedItemId()
  {
    return -10L;
  }
  
  public boolean removeRandomNegativeTrait()
  {
    if (this.status.traits != 0L) {
      return this.status.removeRandomNegativeTrait();
    }
    return false;
  }
  
  private float getTraitMovePercent(boolean mounting)
  {
    float traitMod = 0.0F;
    Creature r = null;
    boolean moving = false;
    if ((isRidden()) && (getMountVehicle() != null)) {
      try
      {
        r = Server.getInstance().getCreature(getMountVehicle().pilotId);
        moving = r.isMoving();
      }
      catch (NoSuchCreatureException localNoSuchCreatureException) {}catch (NoSuchPlayerException localNoSuchPlayerException) {}
    }
    int cweight = getCarriedMountWeight();
    if ((!mounting) && (this.status.traits != 0L))
    {
      Skill sstrength = getSoulStrength();
      if (this.status.isTraitBitSet(1)) {
        if (isHorse())
        {
          if (sstrength.skillCheck(20.0D, 0.0D, !moving, 1.0F) <= 0.0D) {}
        }
        else {
          traitMod += 0.1F;
        }
      }
      if ((!this.status.isTraitBitSet(15)) && (!this.status.isTraitBitSet(16)) && 
        (!this.status.isTraitBitSet(17)) && (!this.status.isTraitBitSet(18)) && 
        (!this.status.isTraitBitSet(24)) && (!this.status.isTraitBitSet(25)) && 
        (this.status.isTraitBitSet(23))) {
        if (isHorse())
        {
          if (sstrength.skillCheck(20.0D, 0.0D, !moving, 1.0F) <= 0.0D) {}
        }
        else {
          traitMod += 0.025F;
        }
      }
      if (this.status.isTraitBitSet(4)) {
        if (isHorse())
        {
          if (sstrength.skillCheck(20.0D, 0.0D, !moving, 1.0F) <= 0.0D) {}
        }
        else {
          traitMod += 0.2F;
        }
      }
      if (this.status.isTraitBitSet(8)) {
        if (isHorse())
        {
          if (sstrength.skillCheck(20.0D, 0.0D, !moving, 1.0F) >= 0.0D) {}
        }
        else {
          traitMod -= 0.1F;
        }
      }
      if (this.status.isTraitBitSet(9)) {
        if (isHorse())
        {
          if (sstrength.skillCheck(20.0D, 0.0D, !moving, 1.0F) >= 0.0D) {}
        }
        else {
          traitMod -= 0.3F;
        }
      }
      if (this.status.isTraitBitSet(6)) {
        if (isHorse())
        {
          if (sstrength.skillCheck(20.0D, 0.0D, !moving, 1.0F) <= 0.0D) {}
        }
        else {
          traitMod += 0.1F;
        }
      }
      float wmod = 0.0F;
      if (this.status.isTraitBitSet(3)) {
        if (isHorse())
        {
          if (sstrength.skillCheck(20.0D, 0.0D, !moving, 1.0F) <= 0.0D) {}
        }
        else {
          wmod += 10000.0F;
        }
      }
      if (this.status.isTraitBitSet(5)) {
        if (isHorse())
        {
          if (sstrength.skillCheck(20.0D, 0.0D, !moving, 1.0F) <= 0.0D) {}
        }
        else {
          wmod += 20000.0F;
        }
      }
      if (this.status.isTraitBitSet(11)) {
        if (isHorse())
        {
          if (sstrength.skillCheck(20.0D, 0.0D, !moving, 1.0F) >= 0.0D) {}
        }
        else {
          wmod -= 30000.0F;
        }
      }
      if (this.status.isTraitBitSet(6)) {
        if (isHorse())
        {
          if (sstrength.skillCheck(20.0D, 0.0D, !moving, 1.0F) <= 0.0D) {}
        }
        else {
          wmod += 10000.0F;
        }
      }
      if (cweight > getStrengthSkill() * 5000.0D + wmod) {
        traitMod = (float)(traitMod - 0.15D * (cweight - getStrengthSkill() * 5000.0D - wmod) / 50000.0D);
      }
    }
    else if (cweight > getStrengthSkill() * 5000.0D)
    {
      traitMod = (float)(traitMod - 0.15D * (cweight - getStrengthSkill() * 5000.0D) / 50000.0D);
    }
    return traitMod;
  }
  
  public boolean isHorse()
  {
    return this.template.isHorse;
  }
  
  public boolean isUnicorn()
  {
    return this.template.isUnicorn();
  }
  
  public boolean cantRideUntame()
  {
    return this.template.cantRideUntamed();
  }
  
  public static void setRandomColor(Creature creature)
  {
    if (Server.rand.nextInt(3) == 0) {
      creature.getStatus().setTraitBit(15, true);
    } else if (Server.rand.nextInt(3) == 0) {
      creature.getStatus().setTraitBit(16, true);
    } else if (Server.rand.nextInt(3) == 0) {
      creature.getStatus().setTraitBit(17, true);
    } else if (Server.rand.nextInt(3) == 0) {
      creature.getStatus().setTraitBit(18, true);
    } else if (Server.rand.nextInt(6) == 0) {
      creature.getStatus().setTraitBit(24, true);
    } else if (Server.rand.nextInt(12) == 0) {
      creature.getStatus().setTraitBit(25, true);
    } else if (Server.rand.nextInt(24) == 0) {
      creature.getStatus().setTraitBit(23, true);
    }
  }
  
  public boolean mayMate(Creature potentialMate)
  {
    if ((isDead()) || (potentialMate.isDead())) {
      return false;
    }
    if ((potentialMate.getTemplate().getMateTemplateId() == this.template.getTemplateId()) || (
      (this.template.getTemplateId() == 96) && (potentialMate.getTemplate().getTemplateId() == 96)))
    {
      if ((this.template.getAdultFemaleTemplateId() != -1) || 
        (this.template.getAdultMaleTemplateId() != -1)) {
        return false;
      }
      if (potentialMate.getSex() != getSex()) {
        if (potentialMate.getWurmId() != getWurmId()) {
          return true;
        }
      }
    }
    return false;
  }
  
  private boolean checkBreedingPossibility()
  {
    Creature[] crets = getCurrentTile().getCreatures();
    if ((!isKingdomGuard()) && (!isGhost()) && (!isHuman()) && (crets.length > 0)) {
      if (mayMate(crets[0])) {
        if ((!crets[0].isPregnant()) && (!isPregnant())) {
          try
          {
            BehaviourDispatcher.action(this, getCommunicator(), -1L, crets[0].getWurmId(), (short)379);
            return true;
          }
          catch (Exception ex)
          {
            return false;
          }
        }
      }
    }
    return false;
  }
  
  public boolean isInTheMoodToBreed(boolean forced)
  {
    if (getStatus().getHunger() > 10000) {
      return false;
    }
    if ((this.template.getAdultFemaleTemplateId() != -1) || (this.template.getAdultMaleTemplateId() != -1)) {
      return false;
    }
    if (getStatus().age <= 3) {
      return false;
    }
    return (this.breedCounter == 0) || ((forced) && (!this.forcedBreed));
  }
  
  public int getBreedCounter()
  {
    return this.breedCounter;
  }
  
  public void resetBreedCounter()
  {
    this.forcedBreed = true;
    this.breedCounter = ((Servers.isThisAPvpServer() ? 900 : 2000) + Server.rand.nextInt(Math.max(1000, 100 * Math.abs(20 - getStatus().age))));
  }
  
  public long getMother()
  {
    return this.status.mother;
  }
  
  public long getFather()
  {
    return this.status.father;
  }
  
  public int getMeditateX()
  {
    return 0;
  }
  
  public int getMeditateY()
  {
    return 0;
  }
  
  public void setDisease(byte newDisease)
  {
    boolean changed = false;
    if ((getStatus().disease > 0) && (newDisease <= 0))
    {
      if (getPower() < 2) {
        setVisible(false);
      }
      changed = true;
      getCommunicator().sendSafeServerMessage("You feel a lot better now as your disease is gone.", (byte)2);
      if (isPlayer()) {
        getCommunicator().sendRemoveSpellEffect(SpellEffectsEnum.DISEASE);
      }
    }
    else if ((isPlayer()) && (newDisease > 0))
    {
      getCommunicator().sendAddSpellEffect(SpellEffectsEnum.DISEASE, 100000, 
        getStatus().disease);
      achievement(173);
    }
    if ((getStatus().disease == 0) && (newDisease == 1))
    {
      if ((isUnique()) || (isKingdomGuard()) || (isGhost()) || (this.status.modtype == 11)) {
        return;
      }
      if (getPower() < 2) {
        setVisible(false);
      }
      changed = true;
      getCommunicator().sendAlertServerMessage("You scratch yourself. What did you catch now?", (byte)2);
    }
    getStatus().setDisease(newDisease);
    if ((changed) && (getPower() < 2)) {
      setVisible(true);
    }
  }
  
  public byte getDisease()
  {
    return getStatus().disease;
  }
  
  public long getLastGroomed()
  {
    return getStatus().lastGroomed;
  }
  
  public void setLastGroomed(long newLastGroomed)
  {
    getStatus().setLastGroomed(newLastGroomed);
  }
  
  public boolean canBeGroomed()
  {
    return System.currentTimeMillis() - getLastGroomed() > 3600000L;
  }
  
  public boolean isDomestic()
  {
    return this.template.domestic;
  }
  
  public boolean isLinked()
  {
    return this.linkedTo != -10L;
  }
  
  public Creature getCreatureLinkedTo()
  {
    try
    {
      return Server.getInstance().getCreature(this.linkedTo);
    }
    catch (Exception localException) {}
    return null;
  }
  
  public int getNumLinks()
  {
    return 0;
  }
  
  public Creature[] getLinks()
  {
    return emptyCreatures;
  }
  
  public void setLinkedTo(long wid, boolean linkback)
  {
    this.linkedTo = wid;
  }
  
  public void disableLink()
  {
    setLinkedTo(-10L, true);
  }
  
  public boolean isMissionairy()
  {
    return true;
  }
  
  public long getLastChangedPriestType()
  {
    return 0L;
  }
  
  public long getLastChangedJoat()
  {
    return 0L;
  }
  
  public Team getTeam()
  {
    return null;
  }
  
  public boolean isTeamLeader()
  {
    return false;
  }
  
  public boolean mayInviteTeam()
  {
    return false;
  }
  
  public void poisonChanged(boolean hadPoison, Wound w)
  {
    if (hadPoison)
    {
      if (!isPoisoned())
      {
        getCommunicator().sendRemoveSpellEffect(w.getWurmId(), SpellEffectsEnum.POISON);
        this.hasSentPoison = false;
      }
    }
    else if (!this.hasSentPoison)
    {
      getCommunicator().sendAddSpellEffect(SpellEffectsEnum.POISON.createId(w.getWurmId()), SpellEffectsEnum.POISON, 100000, w
        .getPoisonSeverity());
      this.hasSentPoison = true;
    }
  }
  
  public final void sendAllPoisonEffect()
  {
    Wounds w = getBody().getWounds();
    if (w != null) {
      if (w.getWounds() != null)
      {
        Wound[] warr = w.getWounds();
        for (int a = 0; a < warr.length; a++) {
          if (warr[a].isPoison())
          {
            getCommunicator().sendAddSpellEffect(SpellEffectsEnum.POISON.createId(warr[a].getWurmId()), SpellEffectsEnum.POISON, 100000, warr[a]
            
              .getPoisonSeverity());
            this.hasSentPoison = true;
          }
        }
      }
    }
  }
  
  public final boolean isPoisoned()
  {
    Wounds w = getBody().getWounds();
    if (w != null) {
      if (w.getWounds() != null)
      {
        Wound[] warr = w.getWounds();
        for (int a = 0; a < warr.length; a++) {
          if (warr[a].isPoison()) {
            return true;
          }
        }
      }
    }
    return false;
  }
  
  public boolean mayEmote()
  {
    return false;
  }
  
  public boolean hasSkillGain()
  {
    return true;
  }
  
  public boolean setHasSkillGain(boolean hasSkillGain)
  {
    return true;
  }
  
  public boolean isFavorOverHeated()
  {
    return false;
  }
  
  public long getOverHeatTime()
  {
    return 0L;
  }
  
  public long getChampTimeStamp()
  {
    return 0L;
  }
  
  public long getLastChangedCluster()
  {
    return 0L;
  }
  
  public boolean isInTheNorthWest()
  {
    return (getTileX() < Zones.worldTileSizeX / 3) && (getTileY() < Zones.worldTileSizeY / 3);
  }
  
  public boolean isInTheNorth()
  {
    return getTileY() < Zones.worldTileSizeX / 3;
  }
  
  public boolean isInTheNorthEast()
  {
    return (getTileX() > Zones.worldTileSizeX - Zones.worldTileSizeX / 3) && (getTileY() < Zones.worldTileSizeY / 3);
  }
  
  public boolean isInTheEast()
  {
    return getTileX() > Zones.worldTileSizeX - Zones.worldTileSizeX / 3;
  }
  
  public boolean isInTheSouthEast()
  {
    return (getTileX() > Zones.worldTileSizeX - Zones.worldTileSizeX / 3) && 
      (getTileY() > Zones.worldTileSizeY - Zones.worldTileSizeY / 3);
  }
  
  public boolean isInTheSouth()
  {
    return getTileY() > Zones.worldTileSizeY - Zones.worldTileSizeY / 3;
  }
  
  public boolean isInTheSouthWest()
  {
    return (getTileX() < Zones.worldTileSizeX / 3) && (getTileY() > Zones.worldTileSizeY - Zones.worldTileSizeY / 3);
  }
  
  public boolean isInTheWest()
  {
    return getTileX() < Zones.worldTileSizeX / 3;
  }
  
  public int getGlobalMapPlacement()
  {
    if (isInTheNorthWest()) {
      return 7;
    }
    if (isInTheNorthEast()) {
      return 1;
    }
    if (isInTheSouthEast()) {
      return 3;
    }
    if (isInTheSouthWest()) {
      return 5;
    }
    if (isInTheNorth()) {
      return 0;
    }
    if (isInTheEast()) {
      return 2;
    }
    if (isInTheSouth()) {
      return 4;
    }
    if (isInTheWest()) {
      return 6;
    }
    return -1;
  }
  
  public boolean mayDestroy(Item item)
  {
    if (item.isDestroyable(getWurmId())) {
      return true;
    }
    if ((item.isOwnerDestroyable()) && (!item.isLocked()))
    {
      Village village = Zones.getVillage(item.getTilePos(), item.isOnSurface());
      if (village != null) {
        return village.isActionAllowed((short)83, this);
      }
      if (item.isUnfinished())
      {
        if ((item.getRealTemplate() != null) && (item.getRealTemplate().isKingdomMarker()) && 
          (getKingdomId() != item.getAuxData())) {
          return true;
        }
      }
      else {
        return true;
      }
    }
    if (item.isEnchantedTurret())
    {
      VolaTile t = Zones.getTileOrNull(item.getTileX(), item.getTileY(), item.isOnSurface());
      if (t != null) {
        if ((t.getVillage() != null) && (t.getVillage().isPermanent)) {
          if (t.getVillage().kingdom == item.getKingdom()) {
            return false;
          }
        }
      }
    }
    return false;
  }
  
  public boolean isCaredFor()
  {
    if ((isUnique()) || (onlyAttacksPlayers())) {
      return false;
    }
    return Creatures.getInstance().isCreatureProtected(getWurmId());
  }
  
  public long getCareTakerId()
  {
    return Creatures.getInstance().getCreatureProtectorFor(getWurmId());
  }
  
  public boolean isCaredFor(Player player)
  {
    return getCareTakerId() == player.getWurmId();
  }
  
  public boolean isBrandedBy(int villageId)
  {
    Village bVill = getBrandVillage();
    return (bVill != null) && (bVill.getId() == villageId);
  }
  
  public boolean isBranded()
  {
    Village bVill = getBrandVillage();
    return bVill != null;
  }
  
  public boolean isOnDeed()
  {
    Village bVill = getBrandVillage();
    if (bVill == null) {
      return false;
    }
    Village pVill = Villages.getVillage(getTileX(), getTileY(), true);
    return (pVill != null) && (bVill.getId() == pVill.getId());
  }
  
  public boolean isHitched()
  {
    return getHitched() != null;
  }
  
  public int getNumberOfPossibleCreatureTakenCareOf()
  {
    return 0;
  }
  
  public final void setHasSpiritStamina(boolean hasStaminaGain)
  {
    this.hasSpiritStamina = hasStaminaGain;
  }
  
  public boolean mayUseLastGasp()
  {
    return false;
  }
  
  public boolean isUsingLastGasp()
  {
    return false;
  }
  
  public final float addToWeaponUsed(Item weapon, float time)
  {
    Float ftime = (Float)this.weaponsUsed.get(weapon);
    if (ftime == null) {
      ftime = Float.valueOf(time);
    } else {
      ftime = Float.valueOf(ftime.floatValue() + time);
    }
    this.weaponsUsed.put(weapon, ftime);
    return ftime.floatValue();
  }
  
  public final UsedAttackData addToAttackUsed(AttackAction act, float time, int rounds)
  {
    UsedAttackData data = (UsedAttackData)this.attackUsed.get(act);
    if (data == null)
    {
      data = new UsedAttackData(time, rounds);
    }
    else
    {
      data.setTime(data.getTime() + time);
      data.setRounds(data.getRounds() + rounds);
    }
    this.attackUsed.put(act, data);
    return data;
  }
  
  public final void updateAttacksUsed(float time)
  {
    for (AttackAction key : this.attackUsed.keySet())
    {
      UsedAttackData data = (UsedAttackData)this.attackUsed.get(key);
      if (data != null) {
        data.update(data.getTime() - time);
      }
    }
  }
  
  public final UsedAttackData getUsedAttackData(AttackAction act)
  {
    return (UsedAttackData)this.attackUsed.get(act);
  }
  
  public final float deductFromWeaponUsed(Item weapon, float swingTime)
  {
    Float ftime = (Float)this.weaponsUsed.get(weapon);
    if (ftime == null) {
      ftime = Float.valueOf(swingTime);
    }
    while (ftime.floatValue() >= swingTime) {
      ftime = Float.valueOf(ftime.floatValue() - swingTime);
    }
    this.weaponsUsed.put(weapon, ftime);
    return ftime.floatValue();
  }
  
  public final void resetWeaponsUsed()
  {
    this.weaponsUsed.clear();
  }
  
  public final void resetAttackUsed()
  {
    this.attackUsed.clear();
    if (this.combatHandler != null) {
      this.combatHandler.resetSecAttacks();
    }
  }
  
  public byte getFightlevel()
  {
    if (this.fightlevel < 0) {
      this.fightlevel = 0;
    }
    if (this.fightlevel > 5) {
      this.fightlevel = 5;
    }
    return this.fightlevel;
  }
  
  public String getFightlevelString()
  {
    int fl = getFightlevel();
    if (fl < 0) {
      fl = 0;
    }
    if (fl >= 5) {
      fl = 5;
    }
    return com.wurmonline.server.combat.Attack.focusStrings[fl];
  }
  
  public void increaseFightlevel(int delta)
  {
    this.fightlevel = ((byte)(this.fightlevel + delta));
    if (this.fightlevel > 5) {
      this.fightlevel = 5;
    }
    if (this.fightlevel < 0) {
      this.fightlevel = 0;
    }
  }
  
  public boolean wasKickedOffBoat()
  {
    return false;
  }
  
  public boolean isOnPermaReputationGrounds()
  {
    if (this.currentVillage != null) {
      if (this.currentVillage.getReputationObject(getWurmId()) != null) {
        return this.currentVillage.getReputationObject(getWurmId()).isPermanent();
      }
    }
    return false;
  }
  
  public boolean hasFingerEffect()
  {
    return false;
  }
  
  public boolean hasFingerOfFoBonus()
  {
    return false;
  }
  
  public boolean hasCrownInfluence()
  {
    return false;
  }
  
  public int getEpicServerId()
  {
    return -1;
  }
  
  public byte getEpicServerKingdom()
  {
    return 0;
  }
  
  public final boolean attackingIntoIllegalDuellingRing(int targetX, int targetY, boolean surfaced)
  {
    if (surfaced)
    {
      Item ring1 = Zones.isWithinDuelRing(getCurrentTile().getTileX(), getCurrentTile().getTileY(), 
        getCurrentTile().isOnSurface());
      
      Item ring = Zones.isWithinDuelRing(targetX, targetY, surfaced);
      if (ring != ring1) {
        return true;
      }
    }
    return false;
  }
  
  public final boolean addWoundOfType(@Nullable Creature attacker, byte woundType, int pos, boolean randomizePos, float armourMod, boolean calculateArmour, double damage)
  {
    return addWoundOfType(attacker, woundType, pos, randomizePos, armourMod, calculateArmour, damage, 0.0F, 0.0D);
  }
  
  public final boolean hasSpellEffect(byte spellEffect)
  {
    if (getSpellEffects() != null) {
      return getSpellEffects().getSpellEffect(spellEffect) != null;
    }
    return false;
  }
  
  public final void reduceStoneSkin()
  {
    if (getSpellEffects() != null)
    {
      SpellEffect sk = getSpellEffects().getSpellEffect((byte)68);
      if (sk != null) {
        if (sk.getPower() > 34.0F) {
          sk.setPower(sk.getPower() - 34.0F);
        } else {
          getSpellEffects().removeSpellEffect(sk);
        }
      }
    }
  }
  
  public final void removeTrueStrike()
  {
    if (getSpellEffects() != null)
    {
      SpellEffect sk = getSpellEffects().getSpellEffect((byte)67);
      if (sk != null) {
        getSpellEffects().removeSpellEffect(sk);
      }
    }
  }
  
  public final boolean addWoundOfType(@Nullable Creature attacker, byte woundType, int pos, boolean randomizePos, float armourMod, boolean calculateArmour, double damage, float infection, double poison)
  {
    if ((woundType == 8) || (woundType == 4) || (woundType == 10)) {
      if ((getCultist() != null) && (getCultist().hasNoElementalDamage())) {
        return false;
      }
    }
    if (hasSpellEffect((byte)69)) {
      damage *= 0.800000011920929D;
    }
    try
    {
      if (randomizePos) {
        pos = getBody().getRandomWoundPos();
      }
      if (calculateArmour)
      {
        armourMod = getArmourMod();
        if ((armourMod == 1.0F) || (isVehicle()) || (isKingdomGuard())) {
          try
          {
            byte protectionSlot = ArmourTemplate.getArmourPosition((byte)pos);
            Item armour = getArmour(protectionSlot);
            if (!isKingdomGuard()) {
              armourMod = ArmourTemplate.calculateDR(armour, woundType);
            } else {
              armourMod *= ArmourTemplate.calculateDR(armour, woundType);
            }
            armour.setDamage(
            
              (float)(armour.getDamage() + damage * armourMod / 30000.0D * armour.getDamageModifier() * ArmourTemplate.getArmourDamageModFor(armour, woundType)));
            if (getBonusForSpellEffect((byte)22) > 0.0F) {
              if (armourMod >= 1.0F) {
                armourMod = 0.2F + (1.0F - getBonusForSpellEffect((byte)22) / 100.0F) * 0.6F;
              } else {
                armourMod = Math.min(armourMod, 0.2F + 
                  (1.0F - getBonusForSpellEffect((byte)22) / 100.0F) * 0.6F);
              }
            }
          }
          catch (NoArmourException localNoArmourException) {}
        }
      }
      if ((pos == 1) || (pos == 29)) {
        damage *= ItemBonus.getFaceDamReductionBonus(this);
      }
      damage *= Wound.getResistModifier(this, woundType);
      if (woundType == 8) {
        return CombatEngine.addColdWound(attacker, this, pos, damage, armourMod);
      }
      if (woundType == 7) {
        return CombatEngine.addDrownWound(attacker, this, pos, damage, armourMod);
      }
      if (woundType == 9) {
        return CombatEngine.addInternalWound(attacker, this, pos, damage, armourMod);
      }
      if (woundType == 10) {
        return CombatEngine.addAcidWound(attacker, this, pos, damage, armourMod);
      }
      if (woundType == 4) {
        return CombatEngine.addFireWound(attacker, this, pos, damage, armourMod);
      }
      if (woundType == 6) {
        return CombatEngine.addRotWound(attacker, this, pos, damage, armourMod, infection);
      }
      if (woundType == 5) {
        return CombatEngine.addWound(attacker, this, woundType, pos, damage, armourMod, "poison", null, infection, (float)poison, false, true);
      }
      return CombatEngine.addWound(attacker, this, woundType, pos, damage, armourMod, "hit", null, infection, (float)poison, false, true);
    }
    catch (NoSpaceException nsp)
    {
      logger.log(Level.WARNING, getName() + " no armour space on loc " + pos);
    }
    catch (Exception e)
    {
      logger.log(Level.WARNING, e.getMessage(), e);
    }
    return false;
  }
  
  public float addSpellResistance(short spellId)
  {
    return 1.0F;
  }
  
  public SpellResistance getSpellResistance(short spellId)
  {
    return null;
  }
  
  public final boolean isInPvPZone()
  {
    if (this.isInNonPvPZone) {
      return false;
    }
    return this.isInPvPZone;
  }
  
  public final boolean isOnPvPServer()
  {
    if (this.isInNonPvPZone) {
      return false;
    }
    if (Servers.localServer.PVPSERVER) {
      return true;
    }
    if (this.isInPvPZone) {
      return true;
    }
    if (this.isInDuelRing) {
      return true;
    }
    return false;
  }
  
  public short getHotaWins()
  {
    return 0;
  }
  
  public void setVehicleCommander(boolean isCommander)
  {
    this.isVehicleCommander = isCommander;
  }
  
  public long getFace()
  {
    return 0L;
  }
  
  public byte getRarity()
  {
    return 0;
  }
  
  public byte getRarityShader()
  {
    if (getBonusForSpellEffect((byte)22) > 70.0F) {
      return 2;
    }
    if (getBonusForSpellEffect((byte)22) > 0.0F) {
      return 1;
    }
    return 0;
  }
  
  public int getKarma()
  {
    if ((isSpellCaster()) || (isSummoner())) {
      return 10000;
    }
    return 0;
  }
  
  public long getTimeToSummonCorpse()
  {
    return 0L;
  }
  
  public boolean maySummonCorpse()
  {
    return false;
  }
  
  public final void pushToFloorLevel(int floorLevel)
  {
    try
    {
      if (!isPlayer())
      {
        float oldposz = getPositionZ();
        float newPosz = Zones.calculateHeight(getPosX(), getPosY(), isOnSurface()) + floorLevel * 3 + 0.25F;
        
        float diffz = oldposz - newPosz;
        getStatus().setPositionZ(newPosz, true);
        if ((this.currentTile != null) && (getVisionArea() != null)) {
          moved(0, 0, (int)(diffz * 10.0F), 0, 0);
        }
      }
    }
    catch (NoSuchZoneException localNoSuchZoneException) {}
  }
  
  public final float calculatePosZ()
  {
    return Zones.calculatePosZ(getPosX(), getPosY(), getCurrentTile(), isOnSurface(), isFloating(), getPositionZ(), this, 
      getBridgeId());
  }
  
  public final boolean canOpenDoors()
  {
    return this.template.canOpenDoors();
  }
  
  public final int getFloorLevel(boolean ignoreVehicleOffset)
  {
    try
    {
      float vehicleOffsetToRemove = 0.0F;
      if (ignoreVehicleOffset)
      {
        long vehicleId = getVehicle();
        if (vehicleId != -10L)
        {
          Vehicle vehicle = Vehicles.getVehicleForId(vehicleId);
          if (vehicle == null)
          {
            logger.log(Level.WARNING, "Unknown vehicle for id: " + vehicleId + " resulting in possinly incorrect floor level!");
          }
          else
          {
            Seat seat = vehicle.getSeatFor(this.id);
            if (seat == null) {
              logger.log(Level.WARNING, "Unable to find the seat the player: " + this.id + " supposedly is on, Vehicle id: " + vehicleId + ". Resulting in possibly incorrect floor level calculation.");
            } else {
              vehicleOffsetToRemove = Math.max(getAltOffZ(), seat.offz);
            }
          }
        }
      }
      float playerPosZ = getPositionZ() + getAltOffZ();
      float groundHeight = Math.max(0.0F, Zones.calculateHeight(getPosX(), getPosY(), isOnSurface()));
      float posZ = Math.max(0.0F, (playerPosZ - groundHeight - vehicleOffsetToRemove + 0.5F) * 10.0F);
      
      return (int)posZ / 30;
    }
    catch (NoSuchZoneException snz) {}
    return 0;
  }
  
  public final int getFloorLevel()
  {
    return getFloorLevel(false);
  }
  
  public boolean fireTileLog()
  {
    return false;
  }
  
  public byte getBlood()
  {
    return 0;
  }
  
  public Shop getShop()
  {
    return Economy.getEconomy().getShop(this);
  }
  
  public int getScenarioKarma()
  {
    return 0;
  }
  
  public boolean knowsKarmaSpell(int karmaSpellActionNum)
  {
    if ((isSpellCaster()) || (isSummoner())) {
      return true;
    }
    return false;
  }
  
  public float getFireResistance()
  {
    return this.template.fireResistance;
  }
  
  public boolean checkCoinAward(int chance)
  {
    return false;
  }
  
  public float getColdResistance()
  {
    return this.template.coldResistance;
  }
  
  public float getDiseaseResistance()
  {
    return this.template.diseaseResistance;
  }
  
  public float getPhysicalResistance()
  {
    return this.template.physicalResistance;
  }
  
  public float getPierceResistance()
  {
    return this.template.pierceResistance;
  }
  
  public float getSlashResistance()
  {
    return this.template.slashResistance;
  }
  
  public float getCrushResistance()
  {
    return this.template.crushResistance;
  }
  
  public float getBiteResistance()
  {
    return this.template.biteResistance;
  }
  
  public float getPoisonResistance()
  {
    return this.template.poisonResistance;
  }
  
  public float getWaterResistance()
  {
    return this.template.waterResistance;
  }
  
  public float getAcidResistance()
  {
    return this.template.acidResistance;
  }
  
  public float getInternalResistance()
  {
    return this.template.internalResistance;
  }
  
  public float getFireVulnerability()
  {
    return this.template.fireVulnerability;
  }
  
  public float getColdVulnerability()
  {
    return this.template.coldVulnerability;
  }
  
  public float getDiseaseVulnerability()
  {
    return this.template.diseaseVulnerability;
  }
  
  public float getPhysicalVulnerability()
  {
    return this.template.physicalVulnerability;
  }
  
  public float getPierceVulnerability()
  {
    return this.template.pierceVulnerability;
  }
  
  public float getSlashVulnerability()
  {
    return this.template.slashVulnerability;
  }
  
  public float getCrushVulnerability()
  {
    return this.template.crushVulnerability;
  }
  
  public float getBiteVulnerability()
  {
    return this.template.biteVulnerability;
  }
  
  public float getPoisonVulnerability()
  {
    return this.template.poisonVulnerability;
  }
  
  public float getWaterVulnerability()
  {
    return this.template.waterVulnerability;
  }
  
  public float getAcidVulnerability()
  {
    return this.template.acidVulnerability;
  }
  
  public float getInternalVulnerability()
  {
    return this.template.internalVulnerability;
  }
  
  public boolean hasAnyAbility()
  {
    return false;
  }
  
  public static final Set<MovementEntity> getIllusionsFor(long wurmid)
  {
    return (Set)illusions.get(Long.valueOf(wurmid));
  }
  
  public static final long getWurmIdForIllusion(long illusionId)
  {
    for (Set<MovementEntity> set : illusions.values()) {
      for (MovementEntity entity : set) {
        if (entity.getWurmid() == illusionId) {
          return entity.getCreatorId();
        }
      }
    }
    return -10L;
  }
  
  public void addIllusion(MovementEntity entity)
  {
    Set<MovementEntity> entities = (Set)illusions.get(Long.valueOf(getWurmId()));
    if (entities == null)
    {
      entities = new HashSet();
      illusions.put(Long.valueOf(getWurmId()), entities);
    }
    entities.add(entity);
  }
  
  public boolean isUndead()
  {
    return false;
  }
  
  public byte getUndeadType()
  {
    return 0;
  }
  
  public String getUndeadTitle()
  {
    return "";
  }
  
  public final void setBridgeId(long bid)
  {
    setBridgeId(bid, true);
  }
  
  public final void setBridgeId(long bid, boolean sendToSelf)
  {
    this.status.getPosition().setBridgeId(bid);
    if (getMovementScheme() != null) {
      getMovementScheme().setBridgeId(bid);
    }
    if (getCurrentTile() != null) {
      getCurrentTile().sendSetBridgeId(this, bid, sendToSelf);
    }
  }
  
  public long getMoneyEarnedBySellingLastHour()
  {
    return 0L;
  }
  
  public final void calcBattleCampBonus()
  {
    Item closest = null;
    for (FocusZone fz : FocusZone.getZonesAt(getTileX(), getTileY())) {
      if (fz.isBattleCamp()) {
        for (Item wartarget : Items.getWarTargets()) {
          if ((closest == null) || 
            (getRange(this, wartarget.getPosX(), wartarget.getPosY()) < getRange(this, closest.getPosX(), closest
            .getPosY()))) {
            closest = wartarget;
          }
        }
      }
    }
    if (closest != null) {
      this.isInOwnBattleCamp = (closest.getKingdom() == getKingdomId());
    }
    this.isInOwnBattleCamp = false;
    logger.log(Level.INFO, getName() + " set battle camp bonus to " + this.isInOwnBattleCamp);
  }
  
  public final boolean hasBattleCampBonus()
  {
    return this.isInOwnBattleCamp;
  }
  
  public boolean isVisibleToPlayers()
  {
    return this.visibleToPlayers;
  }
  
  public void setVisibleToPlayers(boolean aVisibleToPlayers)
  {
    this.visibleToPlayers = aVisibleToPlayers;
  }
  
  public boolean isDoLavaDamage()
  {
    return this.doLavaDamage;
  }
  
  public void setDoLavaDamage(boolean aDoLavaDamage)
  {
    this.doLavaDamage = aDoLavaDamage;
  }
  
  public final boolean doLavaDamage()
  {
    setDoLavaDamage(false);
    if ((!isInvulnerable()) && (!isGhost()) && (!isUnique())) {
      if ((getDeity() == null) || (!getDeity().mountainGod) || (getFaith() < 35.0F)) {
        if (getFarwalkerSeconds() <= 0)
        {
          Wound wound = null;
          boolean dead = false;
          try
          {
            byte pos = getBody().getRandomWoundPos((byte)10);
            if (Server.rand.nextInt(10) <= 6) {
              if (getBody().getWounds() != null)
              {
                wound = getBody().getWounds().getWoundAtLocation(pos);
                if (wound != null)
                {
                  dead = wound.modifySeverity(
                    (int)(5000.0F + Server.rand.nextInt(5000) * (100.0F - getSpellDamageProtectBonus()) / 100.0F));
                  wound.setBandaged(false);
                  setWounded();
                }
              }
            }
            if (wound == null) {
              if ((!isGhost()) && (!isUnique()) && 
                (!isKingdomGuard())) {
                dead = addWoundOfType(null, (byte)4, pos, false, 1.0F, true, 5000.0F + Server.rand
                  .nextInt(5000) * (100.0F - 
                  getSpellDamageProtectBonus()) / 100.0F);
              }
            }
            getCommunicator().sendAlertServerMessage("You are burnt by lava!");
            if (dead)
            {
              achievement(142);
              return true;
            }
          }
          catch (Exception ex)
          {
            logger.log(Level.WARNING, getName() + " " + ex.getMessage(), ex);
          }
        }
      }
    }
    return false;
  }
  
  public boolean isDoAreaDamage()
  {
    return this.doAreaDamage;
  }
  
  public void setDoAreaEffect(boolean aDoAreaDamage)
  {
    this.doAreaDamage = aDoAreaDamage;
  }
  
  public byte getPathfindCounter()
  {
    return this.pathfindcounter;
  }
  
  public void setPathfindcounter(int i)
  {
    this.pathfindcounter = ((byte)i);
  }
  
  public int getHugeMoveCounter()
  {
    return this.hugeMoveCounter;
  }
  
  public void setHugeMoveCounter(int aHugeMoveCounter)
  {
    this.hugeMoveCounter = Math.max(0, aHugeMoveCounter);
  }
  
  public float getArmourLimitingFactor()
  {
    return 0.0F;
  }
  
  public final float getAltOffZ()
  {
    if (getVehicle() != -10L)
    {
      Vehicle vehic = Vehicles.getVehicleForId(getVehicle());
      if (vehic != null)
      {
        Seat s = vehic.getSeatFor(getWurmId());
        if (s != null) {
          return s.getAltOffz();
        }
      }
    }
    return 0.0F;
  }
  
  public final boolean followsGround()
  {
    return (getBridgeId() == -10L) && ((!isPlayer()) || (getMovementScheme().onGround)) && (getFloorLevel() == 0);
  }
  
  public final boolean isWagoner()
  {
    return this.template.getTemplateId() == 114;
  }
  
  @Nullable
  public final Wagoner getWagoner()
  {
    if (isWagoner()) {
      return Wagoner.getWagoner(this.id);
    }
    return null;
  }
  
  public String getTypeName()
  {
    return getTemplate().getName();
  }
  
  public String getObjectName()
  {
    if (isWagoner()) {
      return getName();
    }
    return this.petName;
  }
  
  public boolean setObjectName(String aNewName, Creature aCreature)
  {
    setVisible(false);
    setPetName(aNewName);
    setVisible(true);
    this.status.setChanged(true);
    return true;
  }
  
  public boolean isActualOwner(long playerId)
  {
    return false;
  }
  
  public boolean isOwner(Creature creature)
  {
    return isOwner(creature.getWurmId());
  }
  
  public boolean isOwner(long playerId)
  {
    if (isWagoner())
    {
      Wagoner wagoner = getWagoner();
      if (wagoner != null) {
        return wagoner.getOwnerId() == playerId;
      }
      return false;
    }
    Village bVill = getBrandVillage();
    return (bVill != null) && (bVill.isMayor(playerId));
  }
  
  public boolean canChangeOwner(Creature creature)
  {
    return false;
  }
  
  public boolean canChangeName(Creature creature)
  {
    if (isWagoner()) {
      return false;
    }
    if (creature.getPower() > 1) {
      return true;
    }
    Village bVill = getBrandVillage();
    if (bVill == null) {
      return false;
    }
    return bVill.isMayor(creature);
  }
  
  public boolean setNewOwner(long playerId)
  {
    if (isWagoner())
    {
      Wagoner wagoner = getWagoner();
      if (wagoner != null)
      {
        wagoner.setOwnerId(playerId);
        return true;
      }
      return false;
    }
    return false;
  }
  
  public String getOwnerName()
  {
    return "";
  }
  
  public String getWarning()
  {
    if (isWagoner()) {
      return "";
    }
    Village bVill = getBrandVillage();
    if (bVill == null) {
      return "NEEDS TO BE BRANDED FOR PERMISSIONS TO WORK";
    }
    return "";
  }
  
  public PermissionsPlayerList getPermissionsPlayerList()
  {
    return AnimalSettings.getPermissionsPlayerList(getWurmId());
  }
  
  public boolean isManaged()
  {
    return true;
  }
  
  public boolean isManageEnabled(Player player)
  {
    return false;
  }
  
  public String mayManageText(Player player)
  {
    if (isWagoner()) {
      return "";
    }
    Village bVill = getBrandVillage();
    if (bVill != null) {
      return "Settlement \"" + bVill.getName() + "\" may manage";
    }
    return "";
  }
  
  public String mayManageHover(Player aPlayer)
  {
    return "";
  }
  
  public String messageOnTick()
  {
    return "";
  }
  
  public String questionOnTick()
  {
    return "";
  }
  
  public String messageUnTick()
  {
    return "";
  }
  
  public String questionUnTick()
  {
    return "";
  }
  
  public String getSettlementName()
  {
    String sName = "";
    
    Village bVill = isWagoner() ? this.citizenVillage : getBrandVillage();
    if (bVill != null) {
      sName = bVill.getName();
    }
    if (sName.length() > 0) {
      return "Citizens of \"" + sName + "\"";
    }
    return "";
  }
  
  public String getAllianceName()
  {
    String aName = "";
    Village bVill = isWagoner() ? this.citizenVillage : getBrandVillage();
    if (bVill != null) {
      aName = bVill.getAllianceName();
    }
    if (aName.length() > 0) {
      return "Alliance of \"" + aName + "\"";
    }
    return "";
  }
  
  public String getKingdomName()
  {
    return "";
  }
  
  public boolean canAllowEveryone()
  {
    return true;
  }
  
  public String getRolePermissionName()
  {
    Village bVill = getBrandVillage();
    if (bVill != null) {
      return "Brand Permission of \"" + bVill.getName() + "\"";
    }
    return "";
  }
  
  public boolean isCitizen(Creature creature)
  {
    Village bVill = isWagoner() ? this.citizenVillage : getBrandVillage();
    if (bVill != null) {
      return bVill.isCitizen(creature);
    }
    return false;
  }
  
  public boolean isAllied(Creature creature)
  {
    Village bVill = isWagoner() ? this.citizenVillage : getBrandVillage();
    if (bVill != null) {
      return bVill.isAlly(creature);
    }
    return false;
  }
  
  public boolean isSameKingdom(Creature creature)
  {
    return false;
  }
  
  public void addGuest(long guestId, int aSettings)
  {
    AnimalSettings.addPlayer(getWurmId(), guestId, aSettings);
  }
  
  public void removeGuest(long guestId)
  {
    AnimalSettings.removePlayer(getWurmId(), guestId);
  }
  
  public void addDefaultCitizenPermissions()
  {
    if (!getPermissionsPlayerList().exists(-30L))
    {
      int value = AnimalSettings.Animal1Permissions.COMMANDER.getValue();
      addNewGuest(-30L, value);
    }
  }
  
  public boolean isGuest(Creature creature)
  {
    return isGuest(creature.getWurmId());
  }
  
  public boolean isGuest(long playerId)
  {
    return AnimalSettings.isGuest(this, playerId);
  }
  
  public int getMaxAllowed()
  {
    return AnimalSettings.getMaxAllowed();
  }
  
  public void addNewGuest(long guestId, int aSettings)
  {
    AnimalSettings.addPlayer(getWurmId(), guestId, aSettings);
  }
  
  public Village getBrandVillage()
  {
    Brand brand = Creatures.getInstance().getBrand(getWurmId());
    if (brand != null) {
      try
      {
        return Villages.getVillage((int)brand.getBrandId());
      }
      catch (NoSuchVillageException nsv)
      {
        brand.deleteBrand();
      }
    }
    return null;
  }
  
  public final boolean canHavePermissions()
  {
    if ((isWagoner()) && (Features.Feature.WAGONER.isEnabled())) {
      return true;
    }
    return getBrandVillage() != null;
  }
  
  public final boolean mayLead(Creature creature)
  {
    if (mayCommand(creature)) {
      return true;
    }
    Village bvill = getBrandVillage();
    if (bvill != null)
    {
      VillageRole vr = bvill.getRoleFor(creature);
      return vr.mayLead();
    }
    Village cvill = getCurrentVillage();
    if (cvill != null)
    {
      VillageRole vr = cvill.getRoleFor(creature);
      return vr.mayLead();
    }
    return true;
  }
  
  public final boolean mayShowPermissions(Creature creature)
  {
    return (canHavePermissions()) && (mayManage(creature));
  }
  
  public final boolean canManage(Creature creature)
  {
    if (isWagoner())
    {
      Wagoner wagoner = getWagoner();
      if (wagoner != null)
      {
        if (wagoner.getOwnerId() == creature.getWurmId()) {
          return true;
        }
        if ((creature.getCitizenVillage() != null) && (creature.getCitizenVillage() == this.citizenVillage) && 
          (creature.getCitizenVillage().isMayor(creature))) {
          return true;
        }
      }
    }
    if (AnimalSettings.isExcluded(this, creature)) {
      return false;
    }
    Village vill = getBrandVillage();
    if (AnimalSettings.canManage(this, creature, vill)) {
      return true;
    }
    if (creature.getCitizenVillage() == null) {
      return false;
    }
    if (vill == null) {
      return false;
    }
    if (!vill.isCitizen(creature)) {
      return false;
    }
    return vill.isActionAllowed((short)663, creature);
  }
  
  public final boolean mayManage(Creature creature)
  {
    if ((creature.getPower() > 1) && (!isPlayer())) {
      return true;
    }
    return canManage(creature);
  }
  
  public final boolean maySeeHistory(Creature creature)
  {
    if (isWagoner())
    {
      Wagoner wagoner = getWagoner();
      if (wagoner != null)
      {
        if (wagoner.getOwnerId() == creature.getWurmId()) {
          return true;
        }
        if ((creature.getCitizenVillage() != null) && (creature.getCitizenVillage() == this.citizenVillage) && 
          (creature.getCitizenVillage().isMayor(creature))) {
          return true;
        }
      }
    }
    if ((creature.getPower() > 1) && (!isPlayer())) {
      return true;
    }
    Village bVill = getBrandVillage();
    return (bVill != null) && (bVill.isMayor(creature));
  }
  
  public final boolean mayCommand(Creature creature)
  {
    if (AnimalSettings.isExcluded(this, creature)) {
      return false;
    }
    return AnimalSettings.mayCommand(this, creature, getBrandVillage());
  }
  
  public final boolean mayPassenger(Creature creature)
  {
    if (AnimalSettings.isExcluded(this, creature)) {
      return false;
    }
    return AnimalSettings.mayPassenger(this, creature, getBrandVillage());
  }
  
  public final boolean mayAccessHold(Creature creature)
  {
    if (AnimalSettings.isExcluded(this, creature)) {
      return false;
    }
    return AnimalSettings.mayAccessHold(this, creature, getBrandVillage());
  }
  
  public final boolean mayUse(Creature creature)
  {
    if (AnimalSettings.isExcluded(this, creature)) {
      return false;
    }
    return AnimalSettings.mayUse(this, creature, getBrandVillage());
  }
  
  public final boolean publicMayUse(Creature creature)
  {
    if (AnimalSettings.isExcluded(this, creature)) {
      return false;
    }
    return AnimalSettings.publicMayUse(this);
  }
  
  public ServerEntry getDestination()
  {
    return this.destination;
  }
  
  public void setDestination(ServerEntry destination)
  {
    if ((destination != null) && (!destination.isChallengeOrEpicServer()) && (!destination.LOGINSERVER) && (destination != Servers.localServer)) {
      this.destination = destination;
    }
  }
  
  public void clearDestination()
  {
    this.destination = null;
  }
  
  public int getVillageId()
  {
    if (getCitizenVillage() != null) {
      return getCitizenVillage().getId();
    }
    return 0;
  }
  
  private static Item getRareRecipe(String sig, int commonRecipeId, int rareRecipeId, int supremeRecipeId, int fantasticRecipeId)
  {
    int rno = Server.rand.nextInt(Servers.isThisATestServer() ? 100 : 1000);
    if (rno < 100)
    {
      int recipeId = -10;
      if ((rno == 0) && (fantasticRecipeId != -10)) {
        recipeId = fantasticRecipeId;
      } else if ((rno < 6) && (supremeRecipeId != -10)) {
        recipeId = supremeRecipeId;
      } else if ((rno < 31) && (rareRecipeId != -10)) {
        recipeId = rareRecipeId;
      } else if ((rno >= 50) && (commonRecipeId != -10)) {
        recipeId = commonRecipeId;
      }
      if (recipeId == -10) {
        return null;
      }
      Recipe recipe = Recipes.getRecipeById((short)recipeId);
      if (recipe == null) {
        return null;
      }
      int pp = Server.rand.nextBoolean() ? 1272 : 748;
      int itq = 20 + Server.rand.nextInt(50);
      try
      {
        Item newItem = ItemFactory.createItem(pp, itq, (byte)0, recipe.getLootableRarity(), null);
        
        newItem.setInscription(recipe, sig, 1550103);
        return newItem;
      }
      catch (FailedException e)
      {
        logger.log(Level.WARNING, e.getMessage(), e);
      }
      catch (NoSuchTemplateException e)
      {
        logger.log(Level.WARNING, e.getMessage(), e);
      }
    }
    return null;
  }
  
  public short getDamageCounter()
  {
    return this.damageCounter;
  }
  
  public void setDamageCounter(short damageCounter)
  {
    this.damageCounter = damageCounter;
  }
  
  public List<Route> getHighwayPath()
  {
    return null;
  }
  
  public String getHighwayPathDestination()
  {
    return "";
  }
  
  public long getLastWaystoneChecked()
  {
    return this.lastWaystoneChecked;
  }
  
  public void setLastWaystoneChecked(long waystone)
  {
    this.lastWaystoneChecked = waystone;
    Wagoner wagoner = getWagoner();
    if ((isWagoner()) && (wagoner != null)) {
      wagoner.setLastWaystoneId(waystone);
    }
  }
  
  public boolean embarkOn(long wurmId, byte type)
  {
    try
    {
      Item item = Items.getItem(wurmId);
      Vehicle vehicle = Vehicles.getVehicle(item);
      if (vehicle != null)
      {
        Seat[] seats = vehicle.getSeats();
        for (int x = 0; x < seats.length; x++) {
          if ((seats[x].getType() == type) && (!seats[x].isOccupied()))
          {
            seats[x].occupy(vehicle, this);
            if (type == 0) {
              vehicle.pilotId = getWurmId();
            }
            setVehicleCommander(type == 0);
            MountAction m = new MountAction(null, item, vehicle, x, type == 0, vehicle.seats[x].offz);
            setMountAction(m);
            setVehicle(item.getWurmId(), true, type);
            return true;
          }
        }
      }
    }
    catch (NoSuchItemException e)
    {
      logger.log(Level.WARNING, e.getMessage(), e);
    }
    return false;
  }
  
  public ArrayList<Effect> getEffects()
  {
    return this.effects;
  }
  
  public void addEffect(Effect e)
  {
    if (e == null) {
      return;
    }
    if (this.effects == null) {
      this.effects = new ArrayList();
    }
    this.effects.add(e);
  }
  
  public void removeEffect(Effect e)
  {
    if ((this.effects == null) || (e == null)) {
      return;
    }
    this.effects.remove(e);
    if (this.effects.isEmpty()) {
      this.effects = null;
    }
  }
  
  public void updateEffects()
  {
    if (this.effects == null) {
      return;
    }
    for (Effect e : this.effects) {
      e.setPosXYZ(getPosX(), getPosY(), getPositionZ(), false);
    }
  }
  
  public boolean isPlacingItem()
  {
    return this.isPlacingItem;
  }
  
  public void setPlacingItem(boolean placingItem)
  {
    this.isPlacingItem = placingItem;
    if (!placingItem) {
      setPlacementItem(null);
    }
  }
  
  public void setPlacingItem(boolean placingItem, Item placementItem)
  {
    this.isPlacingItem = placingItem;
    setPlacementItem(placementItem);
  }
  
  public Item getPlacementItem()
  {
    return this.placementItem;
  }
  
  public void setPlacementItem(Item placementItem)
  {
    this.placementItem = placementItem;
    if (placementItem == null) {
      this.pendingPlacement = null;
    }
  }
  
  public void setPendingPlacement(float xPos, float yPos, float zPos, float rot)
  {
    if (this.placementItem != null) {
      this.pendingPlacement = new float[] { this.placementItem.getPosX(), this.placementItem.getPosY(), this.placementItem.getPosZ(), this.placementItem.getRotation(), xPos, yPos, zPos, Math.abs(rot - this.placementItem.getRotation()) > 180.0F ? rot - 360.0F : rot };
    } else {
      this.pendingPlacement = null;
    }
  }
  
  public float[] getPendingPlacement()
  {
    return this.pendingPlacement;
  }
  
  public boolean canUseWithEquipment()
  {
    for (Item subjectItem : getBody().getContainersAndWornItems()) {
      if (subjectItem.isCreatureWearableOnly()) {
        if (subjectItem.isSaddleLarge())
        {
          if (getSize() <= 4) {
            return false;
          }
          if (isKingdomGuard()) {
            return false;
          }
        }
        else if (subjectItem.isSaddleNormal())
        {
          if (getSize() > 4) {
            return false;
          }
          if (isKingdomGuard()) {
            return false;
          }
        }
        else if (subjectItem.isHorseShoe())
        {
          if ((!isHorse()) && ((!isUnicorn()) || (
            (subjectItem.getMaterial() != 7) && 
            (subjectItem.getMaterial() != 8) && 
            (subjectItem.getMaterial() != 96)))) {
            return false;
          }
        }
        else if (subjectItem.isBarding())
        {
          if (!isHorse()) {
            return false;
          }
        }
      }
    }
    return true;
  }
  
  public void mute(boolean mute, String reason, long expiry) {}
  
  public void setLegal(boolean mode) {}
  
  public void setAutofight(boolean mode) {}
  
  public void setFaithMode(boolean mode) {}
  
  public void saveFightMode(byte mode) {}
  
  public void setLastTaggedTerr(byte newKingdom) {}
  
  public void addChallengeScore(int type, float scoreAdded) {}
  
  public void startTrading() {}
  
  public void setSpam(boolean spam) {}
  
  public void setSecondsToLogout(int seconds) {}
  
  public void modifyRanking() {}
  
  public void dropLeadingItem(Item item) {}
  
  public void pollNPC() {}
  
  public void pollNPCChat() {}
  
  private void checkStealthing() {}
  
  public void playPersonalSound(String soundName) {}
  
  public void sendDeityEffectBonuses() {}
  
  public void sendRemoveDeityEffectBonus(int effectNumber) {}
  
  public void sendAddDeityEffectBonus(int effectNumber) {}
  
  public void setAbilityTitle(int newTitle) {}
  
  public void modifyFaith(float modifier) {}
  
  public void setFaith(float faith)
    throws IOException
  {}
  
  public void setDeity(@Nullable Deity deity)
    throws IOException
  {}
  
  public void setAlignment(float align)
    throws IOException
  {}
  
  public void setPriest(boolean priest) {}
  
  public void setChangedDeity()
    throws IOException
  {}
  
  public void setRealDeath(byte realdeathcounter)
    throws IOException
  {}
  
  public void decreaseFatigue() {}
  
  public void setReputation(int reputation) {}
  
  public void setVotedKing(boolean voted) {}
  
  public void clearRoyalty() {}
  
  public void increaseChangedKingdom(boolean setTimeStamp)
    throws IOException
  {}
  
  public void setMoney(long newMoney)
    throws IOException
  {}
  
  public void setClimbing(boolean climbing)
    throws IOException
  {}
  
  public void setSecondTitle(Titles.Title title) {}
  
  public void setTitle(Titles.Title title) {}
  
  public void setFinestAppointment() {}
  
  public void sendRemovePhantasms() {}
  
  protected void setFarwalkerSeconds(byte seconds) {}
  
  public void activeFarwalkerAmulet(Item amulet) {}
  
  public void setPet(long petId) {}
  
  public void toggleFrozen(Creature freezer) {}
  
  public void setVillageSkillModifier(double newModifier) {}
  
  public void checkTheftWarnQuestion() {}
  
  public void setTheftWarned(boolean warned) {}
  
  public void checkChallengeWarnQuestion() {}
  
  public void setChallengeWarned(boolean warned) {}
  
  public void addEnemyPresense() {}
  
  public void removeEnemyPresense() {}
  
  public void setDeathProtected(boolean _deathProtected) {}
  
  public void loadAffinities() {}
  
  public void increaseAffinity(int skillnumber, int value) {}
  
  public void decreaseAffinity(int skillnumber, int value) {}
  
  public void setChangedTileCounter() {}
  
  public void setTutorialLevel(int newLevel) {}
  
  public void missionFinished(boolean reward, boolean sendpopup) {}
  
  public void addAppointment(int aid) {}
  
  public void removeAppointment(int aid) {}
  
  public void setPushCounter(int val) {}
  
  public void activePotion(Item potion) {}
  
  public void setCheated(String reason) {}
  
  public void setFlag(int number, boolean value) {}
  
  public void setAbility(int number, boolean value) {}
  
  public void setTagItem(long itemId, String itemName) {}
  
  public void setMeditateX(int tilex) {}
  
  public void setMeditateY(int tiley) {}
  
  public void setLastKingdom() {}
  
  public void addLink(Creature creature) {}
  
  public void removeLink(long wurmid) {}
  
  public void clearLinks() {}
  
  public void setFatigue(int fatigueToAdd) {}
  
  public void setPriestType(byte type) {}
  
  public void setPrayerSeconds(int prayerSeconds) {}
  
  public void resetJoat() {}
  
  public void setTeam(@Nullable Team newTeam, boolean sendRemove) {}
  
  public void setMayInviteTeam(boolean mayInvite) {}
  
  public void sendSystemMessage(String message) {}
  
  public void sendHelpMessage(String message) {}
  
  public void makeEmoteSound() {}
  
  public void addFavorHeatSeconds(float seconds) {}
  
  public void becomeChamp() {}
  
  public void revertChamp() {}
  
  public void setLastChangedCluster() {}
  
  public void setHasSpiritFavorgain(boolean hasFavorGain) {}
  
  public void setHasSpiritFervor(boolean hasSpiritFervor) {}
  
  public void useLastGasp() {}
  
  public void setKickedOffBoat(boolean kicked) {}
  
  public void setHasFingerEffect(boolean eff) {}
  
  public void sendHasFingerEffect() {}
  
  public void setHasCrownEffect(boolean eff) {}
  
  public void sendHasCrownEffect() {}
  
  public void setCrownInfluence(int influence) {}
  
  public void setHotaWins(short wins) {}
  
  public void achievement(int achievementId) {}
  
  public void addTitle(Titles.Title title) {}
  
  public void removeTitle(Titles.Title title) {}
  
  public void achievement(int achievementId, int counterModifier) {}
  
  protected void addTileMovedDragging() {}
  
  protected void addTileMovedRiding() {}
  
  protected void addTileMoved() {}
  
  protected void addTileMovedDriving() {}
  
  protected void addTileMovedPassenger() {}
  
  public void setKarma(int newKarma) {}
  
  public void modifyKarma(int points) {}
  
  public void sendActionControl(String actionString, boolean start, int timeLeft) {}
  
  public void setScenarioKarma(int newKarma) {}
  
  public void addMoneyEarnedBySellingLastHour(long money) {}
  
  public void setModelName(String newModelName) {}
  
  public void setArmourLimitingFactor(float factor, boolean initializing) {}
  
  public void recalcLimitingFactor(Item currentItem) {}
  
  public void setIsManaged(boolean newIsManaged, Player player) {}
  
  public void setHighwayPath(String newDestination, List<Route> newPath) {}
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\creatures\Creature.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */