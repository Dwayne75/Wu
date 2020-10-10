package com.wurmonline.server.items;

import com.wurmonline.math.TilePos;
import com.wurmonline.math.Vector2f;
import com.wurmonline.math.Vector3f;
import com.wurmonline.mesh.FoliageAge;
import com.wurmonline.mesh.MeshIO;
import com.wurmonline.mesh.Tiles;
import com.wurmonline.mesh.Tiles.Tile;
import com.wurmonline.mesh.TreeData.TreeType;
import com.wurmonline.server.Constants;
import com.wurmonline.server.FailedException;
import com.wurmonline.server.Features.Feature;
import com.wurmonline.server.GeneralUtilities;
import com.wurmonline.server.HistoryManager;
import com.wurmonline.server.Items;
import com.wurmonline.server.LoginHandler;
import com.wurmonline.server.Message;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Players;
import com.wurmonline.server.Server;
import com.wurmonline.server.ServerEntry;
import com.wurmonline.server.Servers;
import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.WurmCalendar;
import com.wurmonline.server.WurmHarvestables;
import com.wurmonline.server.WurmHarvestables.Harvestable;
import com.wurmonline.server.WurmId;
import com.wurmonline.server.banks.Banks;
import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.behaviours.ArtifactBehaviour;
import com.wurmonline.server.behaviours.Behaviour;
import com.wurmonline.server.behaviours.CargoTransportationMethods;
import com.wurmonline.server.behaviours.CreatureBehaviour;
import com.wurmonline.server.behaviours.Methods;
import com.wurmonline.server.behaviours.MethodsItems;
import com.wurmonline.server.behaviours.NoSuchActionException;
import com.wurmonline.server.behaviours.NoSuchBehaviourException;
import com.wurmonline.server.behaviours.TerraformingTask;
import com.wurmonline.server.behaviours.TileRockBehaviour;
import com.wurmonline.server.behaviours.TileTreeBehaviour;
import com.wurmonline.server.behaviours.Vehicle;
import com.wurmonline.server.behaviours.Vehicles;
import com.wurmonline.server.bodys.Body;
import com.wurmonline.server.bodys.BodyTemplate;
import com.wurmonline.server.combat.ArmourTemplate;
import com.wurmonline.server.combat.ArmourTemplate.ArmourType;
import com.wurmonline.server.combat.Arrows;
import com.wurmonline.server.creatures.Brand;
import com.wurmonline.server.creatures.CombatHandler;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.CreatureStatus;
import com.wurmonline.server.creatures.CreatureTemplate;
import com.wurmonline.server.creatures.CreatureTemplateCreator;
import com.wurmonline.server.creatures.CreatureTemplateFactory;
import com.wurmonline.server.creatures.CreatureTemplateIds;
import com.wurmonline.server.creatures.Creatures;
import com.wurmonline.server.creatures.DbCreatureStatus;
import com.wurmonline.server.creatures.Delivery;
import com.wurmonline.server.creatures.MovementScheme;
import com.wurmonline.server.creatures.NoSuchCreatureException;
import com.wurmonline.server.creatures.NoSuchCreatureTemplateException;
import com.wurmonline.server.creatures.Offspring;
import com.wurmonline.server.creatures.Traits;
import com.wurmonline.server.creatures.Wagoner;
import com.wurmonline.server.creatures.Wagoner.Speech;
import com.wurmonline.server.deities.Deities;
import com.wurmonline.server.deities.Deity;
import com.wurmonline.server.economy.MonetaryConstants;
import com.wurmonline.server.economy.Shop;
import com.wurmonline.server.effects.Effect;
import com.wurmonline.server.effects.EffectFactory;
import com.wurmonline.server.epic.EpicServerStatus;
import com.wurmonline.server.highways.MethodsHighways;
import com.wurmonline.server.highways.Routes;
import com.wurmonline.server.kingdom.King;
import com.wurmonline.server.kingdom.Kingdom;
import com.wurmonline.server.kingdom.Kingdoms;
import com.wurmonline.server.meshgen.IslandAdder;
import com.wurmonline.server.players.Achievements;
import com.wurmonline.server.players.ItemBonus;
import com.wurmonline.server.players.Permissions;
import com.wurmonline.server.players.Permissions.Allow;
import com.wurmonline.server.players.Permissions.IAllow;
import com.wurmonline.server.players.PermissionsHistories;
import com.wurmonline.server.players.PermissionsPlayerList;
import com.wurmonline.server.players.PermissionsPlayerList.ISettings;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.players.PlayerInfo;
import com.wurmonline.server.players.PlayerInfoFactory;
import com.wurmonline.server.questions.NewKingQuestion;
import com.wurmonline.server.skills.NoSuchSkillException;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.skills.SkillSystem;
import com.wurmonline.server.skills.Skills;
import com.wurmonline.server.sounds.SoundPlayer;
import com.wurmonline.server.spells.Spell;
import com.wurmonline.server.spells.SpellEffect;
import com.wurmonline.server.spells.Spells;
import com.wurmonline.server.statistics.ChallengePointEnum.ChallengePoint;
import com.wurmonline.server.structures.Blocker;
import com.wurmonline.server.structures.Blocking;
import com.wurmonline.server.structures.BlockingResult;
import com.wurmonline.server.structures.Structure;
import com.wurmonline.server.tutorial.MissionTriggers;
import com.wurmonline.server.tutorial.PlayerTutorial;
import com.wurmonline.server.tutorial.PlayerTutorial.PlayerTrigger;
import com.wurmonline.server.utils.CoordUtils;
import com.wurmonline.server.utils.StringUtil;
import com.wurmonline.server.utils.logging.ItemTransfer;
import com.wurmonline.server.utils.logging.ItemTransferDatabaseLogger;
import com.wurmonline.server.villages.DeadVillage;
import com.wurmonline.server.villages.NoSuchVillageException;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.villages.Villages;
import com.wurmonline.server.weather.Weather;
import com.wurmonline.server.zones.NoSuchZoneException;
import com.wurmonline.server.zones.VirtualZone;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zone;
import com.wurmonline.server.zones.Zones;
import com.wurmonline.shared.constants.CounterTypes;
import com.wurmonline.shared.constants.EffectConstants;
import com.wurmonline.shared.constants.ItemMaterials;
import com.wurmonline.shared.constants.ProtoConstants;
import com.wurmonline.shared.constants.SoundNames;
import com.wurmonline.shared.exceptions.WurmServerException;
import com.wurmonline.shared.util.MaterialUtilities;
import com.wurmonline.shared.util.MulticolorLineSegment;
import com.wurmonline.shared.util.StringUtilities;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
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

public abstract class Item
  implements ItemTypes, TimeConstants, MiscConstants, CounterTypes, EffectConstants, ItemMaterials, SoundNames, MonetaryConstants, ProtoConstants, Comparable<Item>, Permissions.IAllow, PermissionsPlayerList.ISettings, CreatureTemplateIds
{
  long id = -10L;
  protected boolean surfaced = true;
  ItemTemplate template;
  private static final Logger logger = Logger.getLogger(Item.class.getName());
  public static final int FISHING_REEL = 0;
  public static final int FISHING_LINE = 1;
  public static final int FISHING_FLOAT = 2;
  public static final int FISHING_HOOK = 3;
  public static final int FISHING_BAIT = 4;
  private static final ItemTransferDatabaseLogger itemLogger = new ItemTransferDatabaseLogger("Item transfer logger", 500);
  boolean isBusy = false;
  @Nullable
  Set<Effect> effects;
  @Nullable
  Set<Long> keys;
  @Nullable
  Set<Creature> watchers;
  Set<Item> items;
  public long lastMaintained;
  float qualityLevel;
  float originalQualityLevel;
  int sizeX;
  int sizeY;
  int sizeZ;
  float posX;
  float posY;
  float posZ;
  float rotation;
  long parentId = -10L;
  long ownerId = -10L;
  public int zoneId = -10;
  @Nullable
  InscriptionData inscription;
  String name = "";
  String description = "";
  short place;
  boolean locked = false;
  float damage;
  ItemData data;
  @Nullable
  TradingWindow tradeWindow = null;
  int weight = 0;
  short temperature = 200;
  byte material;
  long lockid = -10L;
  public static final int maxSizeMod = 4;
  int price = 0;
  int tempChange = 0;
  byte bless = 0;
  boolean banked = false;
  public byte enchantment = 0;
  public long lastOwner = -10L;
  public boolean deleted = false;
  private int ticksSinceLastDecay = 0;
  public boolean mailed = false;
  @Nonnull
  private static final Effect[] emptyEffects = new Effect[0];
  static final Item[] emptyItems = new Item[0];
  public byte newLayer = Byte.MIN_VALUE;
  private static long lastPolledWhiteAltar = 0L;
  private static long lastPolledBlackAltar = 0L;
  public boolean transferred = false;
  private static final int REPLACE_SEED = 102539;
  private static final char dotchar = '.';
  static final float visibleDecayLimit = 50.0F;
  static final float visibleWornLimit = 25.0F;
  public long lastParentId = -10L;
  public boolean hatching = false;
  byte mailTimes = 0;
  byte auxbyte = 0;
  public long creationDate;
  public byte creationState = 0;
  public int realTemplate = -10;
  public boolean wornAsArmour = false;
  public int color = -1;
  public int color2 = -1;
  public boolean female = false;
  public String creator = "";
  int creatorMaxLength = 40;
  private static final String lit = " (lit)";
  private static final String modelit = ".lit";
  public boolean hidden = false;
  public byte rarity = 0;
  public static final long TRASHBIN_TICK = 3600L;
  private static final long TREBUCHET_RELOAD_TIME = 120L;
  public static final int MAX_CONTAINED_ITEMS_ITEMCRATE_SMALL = 150;
  public static final int MAX_CONTAINED_ITEMS_ITEMCRATE_LARGE = 300;
  public long onBridge = -10L;
  Permissions permissions = new Permissions();
  public static final byte FOOD_STATE_RAW = 0;
  public static final byte FOOD_STATE_FRIED = 1;
  public static final byte FOOD_STATE_GRILLED = 2;
  public static final byte FOOD_STATE_BOILED = 3;
  public static final byte FOOD_STATE_ROASTED = 4;
  public static final byte FOOD_STATE_STEAMED = 5;
  public static final byte FOOD_STATE_BAKED = 6;
  public static final byte FOOD_STATE_COOKED = 7;
  public static final byte FOOD_STATE_CANDIED = 8;
  public static final byte FOOD_STATE_CHOCOLATE_COATED = 9;
  public static final byte FOOD_STATE_CHOPPED_BIT = 16;
  public static final byte FOOD_STATE_MASHED_BIT = 32;
  public static final byte FOOD_STATE_WRAPPED_BIT = 64;
  public static final byte FOOD_STATE_FRESH_BIT = -128;
  public static final byte FOOD_STATE_CHOPPED_MASK = -17;
  public static final byte FOOD_STATE_MASHED_MASK = -33;
  public static final byte FOOD_STATE_WRAPPED_MASK = -65;
  public static final byte FOOD_STATE_FRESH_MASK = 127;
  private int internalVolume = 0;
  private long whenRented = 0L;
  private boolean isLightOverride = false;
  private short warmachineWinches = 0;
  public static final long DRAG_AFTER_RAM_TIME = 30000L;
  public long lastRammed = 0L;
  public long lastRamUser = -10L;
  private long lastPolled = 0L;
  private boolean wagonerWagon = false;
  private boolean replacing = false;
  private boolean isSealedOverride = false;
  private String whatHappened = "";
  private long wasBrandedTo = -10L;
  private long lastAuxPoll;
  protected boolean placedOnParent = false;
  protected boolean isChained = false;
  
  Item() {}
  
  Item(long wurmId, String aName, ItemTemplate aTemplate, float aQLevel, byte aMaterial, byte aRarity, long bridgeId, @Nullable String aCreator)
    throws IOException
  {
    if (wurmId == -10L) {
      this.id = getNextWurmId(aTemplate);
    } else {
      this.id = wurmId;
    }
    this.template = aTemplate;
    this.qualityLevel = aQLevel;
    this.originalQualityLevel = aQLevel;
    this.weight = aTemplate.getWeightGrams();
    this.name = aName;
    this.material = aMaterial;
    this.rarity = aRarity;
    this.onBridge = bridgeId;
    if ((isNamed()) && (aCreator != null) && (aCreator.length() > 0)) {
      this.creator = aCreator.substring(0, Math.min(aCreator.length(), this.creatorMaxLength));
    }
    if (!aTemplate.isBodyPart()) {
      create(this.qualityLevel, WurmCalendar.currentTime);
    }
    Items.putItem(this);
  }
  
  Item(String aName, short aPlace, ItemTemplate aTemplate, float aQualityLevel, byte aMaterial, byte aRarity, long bridgeId, String aCreator)
    throws IOException
  {
    this(-10L, aName, aTemplate, aQualityLevel, aMaterial, aRarity, bridgeId, aCreator);
    setPlace(aPlace);
  }
  
  Item(String aName, ItemTemplate aTemplate, float aQualityLevel, float aPosX, float aPosY, float aPosZ, float aRotation, byte aMaterial, byte aRarity, long bridgeId, String aCreator)
    throws IOException
  {
    this(-10L, aName, aTemplate, aQualityLevel, aMaterial, aRarity, bridgeId, aCreator);
    setPos(aPosX, aPosY, aPosZ, aRotation, bridgeId);
  }
  
  public int compareTo(Item otherItem)
  {
    return getName().compareTo(otherItem.getName());
  }
  
  public static DbStrings getDbStrings(int templateNum)
  {
    if ((templateNum >= 10) && (templateNum <= 19))
    {
      logWarn("THIS HAPPENS AT ", new Exception());
      return null;
    }
    if ((templateNum >= 50) && (templateNum <= 61)) {
      return CoinDbStrings.getInstance();
    }
    return ItemDbStrings.getInstance();
  }
  
  public static DbStrings getDbStringsByWurmId(long wurmId)
  {
    if (WurmId.getType(wurmId) == 19)
    {
      logWarn("THIS HAPPENS AT ", new Exception());
      return null;
    }
    if (WurmId.getType(wurmId) == 20) {
      return CoinDbStrings.getInstance();
    }
    return ItemDbStrings.getInstance();
  }
  
  private static long getNextWurmId(ItemTemplate template)
  {
    if (template.isTemporary()) {
      return WurmId.getNextTempItemId();
    }
    if (template.isCoin()) {
      return WurmId.getNextCoinId();
    }
    return WurmId.getNextItemId();
  }
  
  public final boolean mayLockItems()
  {
    return (isLock()) && (getTemplateId() != 167) && (getTemplateId() != 252) && 
      (getTemplateId() != 568);
  }
  
  public final boolean isBoatLock()
  {
    return getTemplateId() == 568;
  }
  
  public final boolean isBrazier()
  {
    return this.template.isBrazier();
  }
  
  public final boolean isAnchor()
  {
    return getTemplateId() == 565;
  }
  
  public final boolean isTrap()
  {
    return this.template.isTrap;
  }
  
  public final boolean isDisarmTrap()
  {
    return this.template.isDisarmTrap;
  }
  
  public final boolean isOre()
  {
    return this.template.isOre;
  }
  
  public final boolean isShard()
  {
    return this.template.isShard;
  }
  
  public final boolean isBeingWorkedOn()
  {
    if (this.isBusy) {
      return true;
    }
    Iterator<Item> it;
    if (isHollow()) {
      if (this.items != null) {
        for (it = this.items.iterator(); it.hasNext();)
        {
          Item item = (Item)it.next();
          if (item.isBeingWorkedOn()) {
            return true;
          }
        }
      }
    }
    return this.isBusy;
  }
  
  public final boolean combine(Item target, Creature performer)
    throws FailedException
  {
    if (equals(target)) {
      return false;
    }
    Item parent = null;
    if ((this.parentId != -10L) && (target.getParentId() != this.parentId)) {
      try
      {
        parent = Items.getItem(this.parentId);
        if (!parent.hasSpaceFor(target.getVolume())) {
          throw new FailedException("The container could not contain the combined items.");
        }
      }
      catch (NoSuchItemException nsi)
      {
        logInfo("Strange, combining item without parent: " + this.id);
        throw new FailedException("The container could not contain the combined items.");
      }
    }
    if ((this.ownerId == -10L) || (target.getOwnerId() == -10L)) {
      throw new FailedException("You need to carry both items to combine them.");
    }
    if ((!isCombineCold()) && (isMetal()) && (target.getTemplateId() != 204) && 
      (performer.getPower() == 0)) {
      if ((this.temperature < 3500) || (target.getTemperature() < 3500)) {
        throw new FailedException("Metal needs to be glowing hot to be combined.");
      }
    }
    if ((getTemplateId() == target.getTemplateId()) && (isCombine()))
    {
      if ((getMaterial() == target.getMaterial()) || ((isWood()) && (target.isWood())))
      {
        int allWeight = getWeightGrams() + target.getWeightGrams();
        if (isLiquid()) {
          if (!parent.hasSpaceFor(allWeight)) {
            throw new FailedException("The " + parent.getName() + " cannot contain that much " + getName() + ".");
          }
        }
        float maxW = ItemFactory.isMetalLump(getTemplateId()) ? Math.max(this.template.getWeightGrams() * 4 * 4 * 4, 64000) : this.template.getWeightGrams() * 4 * 4 * 4;
        if (allWeight <= maxW)
        {
          if (parent != null) {
            try
            {
              parent.dropItem(this.id, false);
            }
            catch (NoSuchItemException nsi)
            {
              logWarn("This item doesn't exist: " + this.id, nsi);
              return false;
            }
          }
          float newQl = (getCurrentQualityLevel() * getWeightGrams() + target.getCurrentQualityLevel() * target.getWeightGrams()) / allWeight;
          if (allWeight > 0)
          {
            if ((target.isColor()) && (isColor())) {
              setColor(WurmColor.mixColors(this.color, getWeightGrams(), target.color, target.getWeightGrams(), newQl));
            }
            if (getRarity() > target.getRarity())
            {
              if (Server.rand.nextInt(allWeight) > getWeightGrams() / 4) {
                setRarity(target.getRarity());
              }
            }
            else if (target.getRarity() > getRarity()) {
              if (Server.rand.nextInt(allWeight) > target.getWeightGrams() / 4) {
                setRarity(target.getRarity());
              }
            }
            setWeight(allWeight, false);
            setQualityLevel(newQl);
            setDamage(0.0F);
            Items.destroyItem(target.getWurmId());
            if (parent != null) {
              if (!parent.insertItem(this)) {
                try
                {
                  long powner = parent.getOwner();
                  Creature pown = Server.getInstance().getCreature(powner);
                  pown.getInventory().insertItem(this);
                }
                catch (NoSuchCreatureException nsc)
                {
                  logWarn(getName() + ", " + getWurmId() + nsc.getMessage(), nsc);
                }
                catch (NoSuchPlayerException nsc)
                {
                  logWarn(getName() + ", " + getWurmId() + nsc.getMessage(), nsc);
                }
                catch (NotOwnedException no)
                {
                  VolaTile tile = Zones.getOrCreateTile((int)getPosX() >> 2, (int)getPosY() >> 2, 
                    isOnSurface());
                  tile.addItem(this, false, false);
                  logWarn("The combined " + getName() + " was created on ground. This should not happen.");
                }
              }
            }
          }
          else
          {
            Items.destroyItem(this.id);
          }
          return true;
        }
        throw new FailedException("The combined item would be too large to handle.");
      }
      throw new FailedException("The items are of different materials.");
    }
    return false;
  }
  
  public boolean isKingdomFlag()
  {
    return this.template.isKingdomFlag;
  }
  
  @Nullable
  public InscriptionData getInscription()
  {
    return this.inscription;
  }
  
  public String getHoverText()
  {
    return "";
  }
  
  public String getName()
  {
    return getName(true);
  }
  
  public String getName(boolean showWrapped)
  {
    StringBuilder builder = new StringBuilder();
    
    int templateId = this.template.getTemplateId();
    String description = getDescription();
    String stoSend = "";
    if ((descIsName()) && (!description.isEmpty()))
    {
      builder.append('"');
      builder.append(description);
      builder.append('"');
      return builder.toString();
    }
    if (templateId == 1300)
    {
      if (getAuxData() == 1) {
        return "faintly glowing " + this.name;
      }
      return "brightly glowing " + this.name;
    }
    if (templateId == 1423) {
      if (getAuxBit(7) == true) {
        return "small " + this.name + " token";
      }
    }
    if (templateId == 1422) {
      return this.name + " hidden cache";
    }
    if (templateId == 1307) {
      if (getData1() > 0)
      {
        if (getRealTemplate() != null) {
          return 
            getRealTemplate().sizeString + getRealTemplate().getName() + " " + this.name + " [" + getAuxData() + "/" + getRealTemplate().getFragmentAmount() + "]";
        }
      }
      else
      {
        builder.append("unidentified ");
        if ((getRealTemplate() != null) && (getAuxData() >= 65)) {
          if (getRealTemplate().isWeapon()) {
            builder.append("weapon ");
          } else if (getRealTemplate().isArmour()) {
            builder.append("armour ");
          } else if (getRealTemplate().isTool()) {
            builder.append("tool ");
          } else if (getRealTemplate().isStatue()) {
            builder.append("statue ");
          } else if (getRealTemplate().isHollow()) {
            builder.append("container ");
          } else if (getRealTemplate().isRiftLoot()) {
            builder.append("rift ");
          } else if (getRealTemplate().isMetal()) {
            builder.append("metal ");
          } else if (getRealTemplate().isWood()) {
            builder.append("wooden ");
          }
        }
        builder.append(this.name);
        
        return builder.toString();
      }
    }
    if (templateId == 854) {
      return description.isEmpty() ? "sign" : description;
    }
    if (isLight()) {
      if (getTemplateId() != 37)
      {
        if (this.name.endsWith(" (lit)")) {
          this.name = this.name.replace(" (lit)", "");
        }
        if ((isOnFire()) && (!this.name.endsWith(" (lit)")) && (!this.isLightOverride)) {
          stoSend = " (lit)";
        }
      }
    }
    if ((this.template.getTemplateId() == 1243) && (isOnFire())) {
      stoSend = " (smoking)";
    }
    if (templateId == 1346)
    {
      Item reel = getFishingReel();
      if (reel != null)
      {
        if (reel.getTemplateId() == 1372) {
          return "light fishing rod";
        }
        if (reel.getTemplateId() == 1373) {
          return "medium fishing rod";
        }
        if (reel.getTemplateId() == 1374) {
          return "deep water fishing rod";
        }
        if (reel.getTemplateId() == 1375) {
          return "professional fishing rod";
        }
      }
    }
    if (((isWind()) || (this.template.isKingdomFlag)) && (getTemplateId() != 487))
    {
      if ((getTemplateId() == 579) || (getTemplateId() == 578) || 
        (getTemplateId() == 999)) {
        if (getKingdom() != 0)
        {
          builder.append(Kingdoms.getNameFor(getKingdom()));
          builder.append(' ');
        }
      }
      builder.append(this.template.getName());
      return builder.toString();
    }
    if ((this.template.isRune()) && (getRealTemplate() != null))
    {
      switch (getRealTemplate().getTemplateId())
      {
      case 1104: 
        builder.append("wooden");
        break;
      case 1103: 
        builder.append("crystal");
        break;
      case 1102: 
        builder.append("stone");
        break;
      default: 
        builder.append("unknown");
      }
      builder.append(' ');
    }
    if (isBulkItem())
    {
      int nums = getBulkNums();
      if (nums > 0) {
        try
        {
          ItemTemplate it = ItemTemplateFactory.getInstance().getTemplate(getRealTemplateId());
          
          builder.append(it.sizeString);
          if ((getAuxData() != 0) && (it.usesFoodState())) {
            builder.append(getFoodAuxByteName(it, false, true));
          }
          if (!getActualName().equalsIgnoreCase("bulk item")) {
            builder.append(getActualName());
          } else if (nums > 1) {
            builder.append(it.getPlural());
          } else {
            builder.append(it.getName());
          }
          return builder.toString();
        }
        catch (NoSuchTemplateException nst)
        {
          logWarn(
            getWurmId() + " bulk nums=" + getBulkNums() + " but template is " + getBulkTemplateId());
        }
      }
    }
    else
    {
      if (isInventoryGroup())
      {
        if (!description.isEmpty())
        {
          this.name = description;
          setDescription("");
        }
        return this.name;
      }
      if ((templateId == 853) && (getItemCount() > 0))
      {
        Item ship = getItemsAsArray()[0];
        stoSend = " [" + ship.getName() + "]";
      }
    }
    if (this.name.equals(""))
    {
      if (templateId == 179)
      {
        try
        {
          ItemTemplate temp = ItemTemplateFactory.getInstance().getTemplate(
            AdvancedCreationEntry.getTemplateId(this));
          builder.append("unfinished ");
          builder.append(temp.sizeString);
          builder.append(temp.getName());
          return builder.toString();
        }
        catch (NoSuchTemplateException nst)
        {
          logWarn(nst.getMessage(), nst);
        }
      }
      else if (templateId == 177)
      {
        int lData = getData1();
        try
        {
          if (lData != -1)
          {
            ItemTemplate temp = ItemTemplateFactory.getInstance().getTemplate(lData);
            builder.append("Pile of ");
            builder.append(temp.sizeString);
            builder.append(temp.getName());
            return builder.toString();
          }
        }
        catch (NoSuchTemplateException nst)
        {
          logInfo("Inconsistency: " + lData + " does not exist as templateid.");
        }
      }
      else
      {
        if ((templateId == 918) || (templateId == 917) || (templateId == 1017))
        {
          builder.append(this.template.getName());
          return builder.toString();
        }
        if (isWood())
        {
          builder.append(this.template.sizeString);
          builder.append(this.template.getName());
          return builder.toString();
        }
        if (((isSign()) || (isFlag())) && (getTemplateId() != 835))
        {
          if (isPlanted()) {
            if (!description.isEmpty())
            {
              builder.append('"');
              builder.append(description);
              builder.append('"');
              return builder.toString();
            }
          }
        }
        else if (templateId == 518)
        {
          if (!description.isEmpty())
          {
            builder.append("Colossus of ");
            builder.append(LoginHandler.raiseFirstLetter(description));
            return builder.toString();
          }
        }
        else if ((isDecoration()) || (isMetal()))
        {
          try
          {
            ItemTemplate temp = ItemTemplateFactory.getInstance().getTemplate(templateId);
            builder.append(temp.sizeString);
            builder.append(this.template.getName());
            builder.append(stoSend);
            return builder.toString();
          }
          catch (NoSuchTemplateException nst)
          {
            logInfo("Inconsistency: " + templateId + " does not exist as templateid.");
          }
        }
        else if (isBodyPart())
        {
          if (templateId == 11)
          {
            if (this.place == 3) {
              builder.append("left ");
            } else {
              builder.append("right ");
            }
            builder.append(this.template.getName());
            return builder.toString();
          }
          if (templateId == 14)
          {
            if (this.place == 13) {
              builder.append("left ");
            } else {
              builder.append("right ");
            }
            builder.append(this.template.getName());
            return builder.toString();
          }
          if (templateId == 15)
          {
            if (this.place == 15) {
              builder.append("left ");
            } else {
              builder.append("right ");
            }
            builder.append(this.template.getName());
            return builder.toString();
          }
        }
        else if (isButcheredItem())
        {
          try
          {
            CreatureTemplate temp = CreatureTemplateFactory.getInstance().getTemplate(getData2());
            builder.append(temp.getName());
            builder.append(' ');
            builder.append(this.template.getName());
            return builder.toString();
          }
          catch (Exception nst)
          {
            logInfo(getWurmId() + " unknown butchered creature: " + nst.getMessage(), nst);
          }
        }
      }
      builder.append(this.template.sizeString);
      builder.append(this.template.getName());
      return builder.toString();
    }
    if (((isSign()) && (getTemplateId() != 835)) || (isFlag()))
    {
      if (isPlanted()) {
        if (!description.isEmpty())
        {
          builder.append('"');
          builder.append(description);
          
          builder.append('"');
          
          return builder.toString();
        }
      }
    }
    else if (templateId == 518)
    {
      if (!description.isEmpty())
      {
        builder.append('"');
        builder.append("Colossus of ");
        builder.append(LoginHandler.raiseFirstLetter(description));
        
        builder.append('"');
        return builder.toString();
      }
    }
    else
    {
      if (((isVehicle()) && (!isChair()) && (!description.isEmpty())) || (
        (isChair()) && (!description.isEmpty()) && (getParentId() == -10L)))
      {
        builder.append('"');
        builder.append(description);
        builder.append('"');
        return builder.toString() + stoSend;
      }
      if ((templateId == 654) && (getAuxData() > 0))
      {
        if (getBless() != null) {
          builder.append("Active ");
        } else {
          builder.append("Passive ");
        }
      }
      else if ((templateId == 1239) || (templateId == 1175))
      {
        if (WurmCalendar.isSeasonWinter())
        {
          if (hasQueen()) {
            builder.append("dormant ");
          } else {
            builder.append("empty ");
          }
        }
        else if (hasTwoQueens()) {
          builder.append("noisy ");
        } else if (hasQueen()) {
          builder.append("active ");
        } else {
          builder.append("empty ");
        }
      }
      else if (usesFoodState())
      {
        builder.append(getFoodAuxByteName(this.template, false, showWrapped));
      }
      else if ((getTemplateId() == 729) && (getAuxData() > 0))
      {
        builder.append("birthday ");
      }
      else if (isSealedByPlayer())
      {
        builder.append("sealed ");
        nst = getItemsAsArray();nst = nst.length;
        for (NoSuchTemplateException localNoSuchTemplateException1 = 0; localNoSuchTemplateException1 < nst; localNoSuchTemplateException1++)
        {
          Item item = nst[localNoSuchTemplateException1];
          if ((item.isLiquid()) && (item.isDye()))
          {
            int red = WurmColor.getColorRed(item.getColor());
            int green = WurmColor.getColorGreen(item.getColor());
            int blue = WurmColor.getColorBlue(item.getColor());
            stoSend = " [" + item.getName() + "] (" + red + "/" + green + "/" + blue + ")";
            
            break;
          }
          if (item.isLiquid())
          {
            stoSend = " [" + item.getName() + "]";
            break;
          }
        }
      }
      else if ((getTemplateId() == 1162) && (getParentId() != -10L))
      {
        ItemTemplate rt = getRealTemplate();
        if (rt != null) {
          stoSend = " [" + rt.getName().replace(" ", "") + "]";
        }
      }
      else if ((getTemplateId() == 748) || (getTemplateId() == 1272))
      {
        switch (getAuxData())
        {
        case 0: 
          InscriptionData ins = getInscription();
          if ((ins != null) && (ins.hasBeenInscribed())) {
            builder.append("inscribed ");
          } else {
            builder.append("blank ");
          }
          break;
        case 1: 
          builder.append("recipe ");
          break;
        case 2: 
          builder.append("waxed ");
          break;
        default: 
          WurmHarvestables.Harvestable harvestable = WurmHarvestables.getHarvestable(getAuxData() - 8);
          if (harvestable == null) {
            builder.append("ruined ");
          }
          break;
        }
      }
    }
    if (isTrellis())
    {
      FoliageAge age = FoliageAge.fromByte(getLeftAuxData());
      stoSend = " (" + age.getAgeName() + ")";
    }
    builder.append(this.name);
    builder.append(stoSend);
    return builder.toString();
  }
  
  public String getFoodAuxByteName(ItemTemplate it, boolean full, boolean showWrapped)
  {
    StringBuilder builder = new StringBuilder();
    if (getTemplateId() == 128)
    {
      if (isSalted()) {
        builder.append("salty ");
      }
      return builder.toString();
    }
    if (isFresh()) {
      builder.append("fresh ");
    } else if (getDamage() > 90.0F) {
      builder.append("rotten ");
    } else if (getDamage() > 75.0F) {
      builder.append("moldy ");
    }
    if (full)
    {
      if (isSalted()) {
        builder.append("salted ");
      }
      if ((isFish()) && (isUnderWeight())) {
        builder.append("underweight ");
      }
    }
    if (isWrapped()) {
      if (it.canBeDistilled()) {
        builder.append("undistilled ");
      } else if (showWrapped) {
        builder.append("wrapped ");
      }
    }
    if ((full) && (builder.length() == 0)) {
      builder.append("(none) ");
    }
    switch (getRightAuxData())
    {
    case 1: 
      builder.append("fried ");
      break;
    case 2: 
      builder.append("grilled ");
      break;
    case 3: 
      builder.append("boiled ");
      break;
    case 4: 
      builder.append("roasted ");
      break;
    case 5: 
      builder.append("steamed ");
      break;
    case 6: 
      builder.append("baked ");
      break;
    case 7: 
      builder.append("cooked ");
      break;
    case 8: 
      builder.append("candied ");
      break;
    case 9: 
      builder.append("chocolate coated ");
      break;
    default: 
      if (it.canShowRaw()) {
        builder.append("raw ");
      } else if (full) {
        builder.append("(raw) ");
      }
      break;
    }
    if (isChopped()) {
      if ((it.isHerb()) || (it.isVegetable()) || (it.isFish()) || (it.isMushroom)) {
        builder.append("chopped ");
      } else if (it.isMeat()) {
        builder.append("diced ");
      } else if (it.isSpice()) {
        builder.append("ground ");
      } else if (it.canBeFermented()) {
        builder.append("unfermented ");
      } else if (it.getTemplateId() == 1249) {
        builder.append("whipped ");
      } else {
        builder.append("zombified ");
      }
    }
    if (isMashedBitSet()) {
      if (it.isMeat()) {
        builder.append("minced ");
      } else if (it.isVegetable()) {
        builder.append("mashed ");
      } else if (it.canBeFermented()) {
        builder.append("fermenting ");
      } else if (getTemplateId() == 1249) {
        builder.append("clotted ");
      }
    }
    return builder.toString();
  }
  
  public String getActualName()
  {
    return this.name;
  }
  
  public boolean isNamePlural()
  {
    return this.template.isNamePlural();
  }
  
  public final String getSignature()
  {
    if ((this.creator != null) && (this.creator.length() > 0) && (getTemplateId() != 651))
    {
      String toReturn = this.creator;
      int ql = (int)getCurrentQualityLevel();
      if (ql < 20) {
        return "";
      }
      if (ql < 90) {
        toReturn = obscureWord(this.creator, ql);
      }
      return toReturn;
    }
    return this.creator;
  }
  
  public static String obscureWord(String word, int ql)
  {
    int containfactor = ql / 10;
    char[] cword = word.toCharArray();
    Random r = new Random();
    r.setSeed(102539L);
    for (int x = 0; x < word.length(); x++) {
      if (r.nextInt(containfactor) > 0) {
        cword[x] = word.charAt(x);
      } else {
        cword[x] = '.';
      }
    }
    return String.valueOf(cword);
  }
  
  public String getNameWithGenus()
  {
    return StringUtilities.addGenus(getName(), isNamePlural());
  }
  
  public final float getNutritionLevel()
  {
    boolean hasBonus = ((this.template.hasFoodBonusWhenCold()) && (this.temperature < 300)) || ((this.template.hasFoodBonusWhenHot()) && (this.temperature > 1000));
    float ql = getCurrentQualityLevel();
    if (isHighNutrition()) {
      return 0.56F + ql / 300.0F + (hasBonus ? 0.09F : 0.0F);
    }
    if (isGoodNutrition()) {
      return 0.4F + ql / 500.0F + (hasBonus ? 0.1F : 0.0F);
    }
    if (isMediumNutrition()) {
      return 0.3F + ql / 1000.0F + (hasBonus ? 0.1F : 0.0F);
    }
    if (isLowNutrition()) {
      return 0.1F + ql / 1000.0F + (hasBonus ? 0.1F : 0.0F);
    }
    return 0.01F + ql / 1000.0F + (hasBonus ? 0.05F : 0.0F);
  }
  
  public final int getTowerModel()
  {
    byte kingdomTemplateId = getAuxData();
    if ((getAuxData() < 0) || (getAuxData() > 4))
    {
      Kingdom k = Kingdoms.getKingdom(getAuxData());
      if (k != null) {
        kingdomTemplateId = k.getTemplate();
      }
    }
    if (kingdomTemplateId == 3) {
      return 430;
    }
    if (kingdomTemplateId == 2) {
      return 528;
    }
    if (kingdomTemplateId == 4) {
      return 638;
    }
    return 384;
  }
  
  public final String getModelName()
  {
    StringBuilder builder = new StringBuilder();
    
    int templateId = this.template.getTemplateId();
    if (templateId == 177)
    {
      int lData = getData1();
      try
      {
        if (lData != -1)
        {
          builder.append(ItemTemplateFactory.getInstance().getTemplate(lData).getName());
          builder.append(".");
          builder.append(getMaterialString(getMaterial()));
          StringBuilder b2 = new StringBuilder();
          
          b2.append(this.template.getModelName());
          
          b2.append(builder.toString().replaceAll(" ", ""));
          return b2.toString();
        }
      }
      catch (NoSuchTemplateException nst)
      {
        logInfo("Inconsistency: " + lData + " does not exist as templateid.");
      }
    }
    else
    {
      if (isDragonArmour())
      {
        builder.append(getTemplate().getModelName());
        String matString = MaterialUtilities.getMaterialString(getMaterial()) + ".";
        builder.append(matString);
        String text = getDragonColorNameByColor(getColor());
        builder.append(text);
        return builder.toString();
      }
      if (templateId == 854)
      {
        builder.append(getTemplate().getModelName());
        String text = getAuxData() + ".";
        builder.append(text);
        return builder.toString();
      }
      if (templateId == 385)
      {
        builder.append("model.fallen.");
        builder.append(TileTreeBehaviour.getTreenameForMaterial(getMaterial()));
        if (this.auxbyte >= 100) {
          builder.append(".animatedfalling");
        }
        builder.append(".seasoncycle");
        return builder.toString();
      }
      if (templateId == 386) {
        try
        {
          ItemTemplate temp = ItemTemplateFactory.getInstance().getTemplate(this.realTemplate);
          if (this.template.wood)
          {
            builder.append(temp.getModelName());
            builder.append("unfinished.");
            builder.append(getMaterialString(getMaterial()));
            return builder.toString();
          }
          builder.append(temp.getModelName());
          
          builder.append("unfinished.");
          builder.append(getMaterialString(getMaterial()));
          
          return builder.toString();
        }
        catch (NoSuchTemplateException nst)
        {
          logWarn(this.realTemplate + ": " + nst.getMessage(), nst);
        }
      }
      if (templateId == 179) {
        try
        {
          int tempmodel = AdvancedCreationEntry.getTemplateId(this);
          if (tempmodel == 384) {
            tempmodel = getTowerModel();
          }
          ItemTemplate temp = ItemTemplateFactory.getInstance().getTemplate(tempmodel);
          if (temp.isTapestry())
          {
            builder.append("model.furniture.tapestry.unfinished");
            return builder.toString();
          }
          if (temp.wood)
          {
            builder.append(temp.getModelName());
            builder.append("unfinished.");
            if (isVisibleDecay()) {
              if (this.damage >= 50.0F) {
                builder.append("decayed.");
              } else if (this.damage >= 25.0F) {
                builder.append("worn.");
              }
            }
            builder.append(getMaterialString(getMaterial()));
            
            return builder.toString();
          }
          builder.append(temp.getModelName());
          
          builder.append("unfinished.");
          if (isVisibleDecay()) {
            if (this.damage >= 50.0F) {
              builder.append("decayed.");
            } else if (this.damage >= 25.0F) {
              builder.append("worn.");
            }
          }
          builder.append(getMaterialString(getMaterial()));
          return builder.toString();
        }
        catch (NoSuchTemplateException nst)
        {
          logWarn(this.realTemplate + ": " + nst.getMessage(), nst);
        }
      }
      if (templateId == 1307)
      {
        builder.append(this.template.getModelName());
        if (getData1() > 0)
        {
          if (getMaterial() == 93) {
            builder.append("iron");
          } else if ((getMaterial() == 94) || (getMaterial() == 95)) {
            builder.append("steel");
          } else {
            builder.append(getMaterialString(getMaterial()));
          }
        }
        else {
          builder.append("unidentified.");
        }
        return builder.toString();
      }
      if (templateId == 1346)
      {
        builder.append(this.template.getModelName());
        Item reel = getFishingReel();
        if (reel != null) {
          if (reel.getTemplateId() == 1372) {
            builder.append("light.");
          } else if (reel.getTemplateId() == 1373) {
            builder.append("medium.");
          } else if (reel.getTemplateId() == 1374) {
            builder.append("deepwater.");
          } else if (reel.getTemplateId() == 1375) {
            builder.append("professional.");
          }
        }
        builder.append(getMaterialString(getMaterial()));
        return builder.toString();
      }
      if (templateId == 272)
      {
        try
        {
          CreatureTemplate temp = CreatureTemplateFactory.getInstance().getTemplate(getData1());
          builder.append(this.template.getModelName());
          builder.append(temp.getCorpsename());
          if (getDescription().length() > 0)
          {
            if (getDescription().contains("["))
            {
              String desc = getDescription().replace(" ", "");
              desc = desc.substring(0, desc.indexOf("["));
              builder.append(desc);
            }
            else
            {
              builder.append(getDescription().replace(" ", ""));
            }
            builder.append(".");
          }
          if (isButchered()) {
            builder.append("butchered.");
          }
          if (this.female) {
            builder.append("female.");
          }
          Kingdom k = Kingdoms.getKingdom(getKingdom());
          if ((k != null) && (k.getTemplate() != getKingdom())) {
            builder.append(Kingdoms.getSuffixFor(k.getTemplate()));
          }
          builder.append(Kingdoms.getSuffixFor(getKingdom()));
          builder.append(WurmCalendar.getSpecialMapping(false));
          return builder.toString();
        }
        catch (NoSuchCreatureTemplateException localNoSuchCreatureTemplateException) {}
      }
      else
      {
        if ((templateId == 853) || (isWagonerWagon()) || (templateId == 1410))
        {
          builder.append(getTemplate().getModelName());
          if (isWagonerWagon()) {
            builder.append("wagoner.");
          }
          if (getItemCount() > 0) {
            builder.append("loaded.");
          } else {
            builder.append("unloaded.");
          }
          builder.append(WurmCalendar.getSpecialMapping(false));
          builder.append(getMaterialString(getMaterial()));
          return builder.toString();
        }
        if ((templateId == 651) || (templateId == 1097) || (templateId == 1098))
        {
          builder.append(getTemplate().getModelName());
          switch (this.auxbyte)
          {
          case 0: 
            builder.append("green");
            break;
          case 1: 
            builder.append("blue");
            break;
          case 2: 
            builder.append("striped");
            break;
          case 3: 
            builder.append("candy");
            break;
          case 4: 
            builder.append("holly");
            break;
          default: 
            builder.append("green");
          }
          return builder.toString();
        }
        if (isSign())
        {
          builder.append(getTemplate().getModelName());
          if (isVisibleDecay()) {
            if (this.damage >= 50.0F) {
              builder.append("decayed.");
            } else if (this.damage >= 25.0F) {
              builder.append("worn.");
            }
          }
          if (getTemplateId() == 656)
          {
            builder.append(this.auxbyte);
            builder.append('.');
          }
          builder.append(getMaterialString(getMaterial()));
          return builder.toString();
        }
        if (templateId == 521)
        {
          try
          {
            CreatureTemplate temp = CreatureTemplateFactory.getInstance().getTemplate(getData1());
            builder.append(this.template.getModelName());
            builder.append(temp.getCorpsename());
            builder.append(WurmCalendar.getSpecialMapping(false));
            return builder.toString();
          }
          catch (NoSuchCreatureTemplateException localNoSuchCreatureTemplateException1) {}
        }
        else
        {
          if (templateId == 387) {
            try
            {
              ItemTemplate temp = ItemTemplateFactory.getInstance().getTemplate(this.realTemplate);
              if (this.template.wood)
              {
                builder.append(temp.getModelName());
                if (isVisibleDecay()) {
                  if (this.damage >= 50.0F) {
                    builder.append("decayed.");
                  } else if (this.damage >= 25.0F) {
                    builder.append("worn.");
                  }
                }
                builder.append(getMaterialString(getMaterial()));
                return builder.toString();
              }
              builder.append(temp.getModelName());
              builder.append(getMaterialString(getMaterial()));
              return builder.toString();
            }
            catch (NoSuchTemplateException nst)
            {
              logWarn(this.realTemplate + ": " + nst.getMessage(), nst);
            }
          }
          if (templateId == 791)
          {
            builder.append(this.template.getModelName());
            builder.append(getMaterialString(getMaterial()));
            if (WurmCalendar.isChristmas()) {
              builder.append(".red");
            } else {
              builder.append(".grey");
            }
            return builder.toString();
          }
          if (templateId == 518)
          {
            builder.append(this.template.getModelName());
            builder.append(getMaterialString(getMaterial()));
            builder.append(WurmCalendar.getSpecialMapping(true));
            builder.append(".");
            builder.append(getDescription().toLowerCase());
            return builder.toString().replaceAll(" ", "");
          }
          if (templateId == 538)
          {
            builder.append(this.template.getModelName());
            if (King.getKing((byte)2) == null) {
              builder.append("occupied.");
            }
            return builder.toString();
          }
          if (isItemSpawn())
          {
            builder.append(this.template.getModelName());
            builder.append(getAuxData());
            return builder.toString();
          }
        }
      }
    }
    if ((isWind()) || (this.template.isKingdomFlag) || (isProtectionTower()))
    {
      builder.append(this.template.getModelName());
      if (getKingdom() != 0) {
        builder.append(Kingdoms.getSuffixFor(getKingdom()));
      }
      return builder.toString();
    }
    if ((isTent()) || (this.template.useMaterialAndKingdom))
    {
      builder.append(this.template.getModelName());
      builder.append(getMaterialString(getMaterial()));
      builder.append(".");
      if (getKingdom() != 0) {
        builder.append(Kingdoms.getSuffixFor(getKingdom()));
      }
      return builder.toString();
    }
    if (this.template.templateId == 850)
    {
      builder.append(this.template.getModelName());
      if (getData1() != 0) {
        builder.append(Kingdoms.getSuffixFor((byte)getData1()));
      }
      builder.append(WurmCalendar.getSpecialMapping(true));
      builder.append(getMaterialString(getMaterial()));
      return builder.toString();
    }
    String meat;
    boolean foundMeat;
    String spit;
    if (isFire())
    {
      builder.append(this.template.getModelName());
      String cook = "";
      boolean foundCook = false;
      meat = "";
      foundMeat = false;
      spit = "";
      for (Item i : getItems())
      {
        if ((i.isFoodMaker()) && (!foundCook))
        {
          cook = i.getConcatName();
          foundCook = true;
        }
        if (((i.getTemplateId() == 368) || (i.getTemplateId() == 92)) && (!foundMeat))
        {
          meat = "meat";
          foundMeat = true;
        }
        if ((i.getTemplateId() == 369) && (!foundMeat))
        {
          meat = "fish.fillet";
          foundMeat = true;
        }
        if ((i.isFish()) && (!foundMeat))
        {
          meat = "fish";
          foundMeat = true;
        }
        if (i.getTemplate().getModelName().contains(".spit."))
        {
          spit = i.getTemplate().getModelName().substring(11);
          if (i.isRoasted()) {
            spit = spit + "roasted.";
          }
        }
      }
      if (spit.length() > 0)
      {
        builder.append(spit);
      }
      else if (foundCook)
      {
        builder.append(cook);
      }
      else if (foundMeat)
      {
        builder.append(meat);
        builder.append(".");
      }
      if (!isOnFire()) {
        builder.append("unlit");
      }
      return builder.toString();
    }
    if (getTemplateId() == 1301)
    {
      builder.append(this.template.getModelName());
      switch (getAuxData())
      {
      case 1: 
      case 2: 
      case 11: 
      case 16: 
        builder.append("meat.");
        break;
      case 3: 
        builder.append("fish.fillet.");
        break;
      case 4: 
      case 5: 
      case 14: 
      case 19: 
        builder.append("fish.");
        break;
      case 6: 
      case 7: 
        builder.append("fryingpan.");
        break;
      case 8: 
      case 9: 
      case 10: 
        builder.append("potterybowl.");
        break;
      case 12: 
      case 13: 
        builder.append("spit.pig.");
        break;
      case 15: 
        builder.append("spit.lamb.");
        break;
      case 17: 
      case 18: 
        builder.append("spit.pig.roasted.");
        break;
      case 20: 
        builder.append("spit.lamb.roasted.");
      }
      if (!isOnFire()) {
        builder.append("unlit");
      }
      return builder.toString();
    }
    if (isRoadMarker())
    {
      builder.append(this.template.getModelName());
      if (this.template.templateId == 1114)
      {
        int possibleRoutes = MethodsHighways.numberOfSetBits(getAuxData());
        if (possibleRoutes == 1) {
          builder.append("red.");
        } else if (possibleRoutes == 2) {
          if (Routes.isCatseyeUsed(this)) {
            builder.append("green.");
          } else {
            builder.append("blue.");
          }
        }
      }
      return builder.toString();
    }
    if (this.template.templateId == 1342)
    {
      builder.append(this.template.getModelName());
      if (isPlanted()) {
        builder.append("planted.");
      }
      builder.append(WurmCalendar.getSpecialMapping(false));
      builder.append(getMaterialString(getMaterial()) + ".");
      
      return builder.toString();
    }
    if (this.template.wood)
    {
      builder.append(this.template.getModelName());
      if (getTemplateId() == 1432)
      {
        boolean chicks = false;
        boolean eggs = false;
        meat = getAllItems(true);foundMeat = meat.length;
        for (spit = 0; spit < foundMeat; spit++)
        {
          Item item = meat[spit];
          if (item.getTemplateId() == 1436) {
            if (!item.isEmpty(true)) {
              chicks = true;
            } else {
              chicks = false;
            }
          }
          if (item.getTemplateId() == 1433) {
            if (!item.isEmpty(true)) {
              eggs = true;
            } else {
              eggs = false;
            }
          }
        }
        if ((eggs) && (chicks)) {
          builder.append("chicken.egg.");
        }
        if ((!eggs) && (chicks)) {
          builder.append("chicken.");
        }
        if ((eggs) && (!chicks)) {
          builder.append("egg.");
        }
        if ((!eggs) && (!chicks)) {
          builder.append("empty.");
        }
        if (isUnfinished()) {
          builder.append("unfinished.");
        }
        if (this.damage >= 60.0F) {
          builder.append("decayed.");
        }
      }
      if (getTemplateId() == 1311)
      {
        if ((isEmpty(true)) && (!isUnfinished())) {
          setAuxData((byte)0);
        }
        switch (getAuxData())
        {
        case 0: 
          builder.append("empty.");
          break;
        case 64: 
          builder.append("horse.");
          break;
        case 82: 
          builder.append("bison.");
          break;
        case 83: 
          builder.append("hellhorse.");
          break;
        case 3: 
          builder.append("cow.");
          break;
        case 49: 
          builder.append("bull.");
          break;
        case 102: 
          builder.append("ram.");
          break;
        case 65: 
          builder.append("foal.");
          break;
        case 96: 
          builder.append("sheep.");
          break;
        case 117: 
          builder.append("hellhorse.foal.");
          break;
        default: 
          builder.append("generic.");
        }
        if (isEmpty(true)) {
          builder.append("empty.");
        }
        if (isUnfinished()) {
          builder.append("unfinished.");
        }
        if (this.damage >= 60.0F) {
          builder.append("decayed.");
        }
      }
      if (((templateId == 1309) || (isCrate())) && (
        (isSealedOverride()) || (isSealedByPlayer()))) {
        builder.append("sealed.");
      }
      if ((isBulkContainer()) || (getTemplateId() == 670)) {
        if (!isCrate())
        {
          if (isFull()) {
            builder.append("full.");
          } else {
            builder.append("empty.");
          }
        }
        else if (getItemCount() > 0) {
          builder.append("full.");
        } else {
          builder.append("empty.");
        }
      }
      if ((templateId == 724) || (templateId == 725) || (templateId == 758) || (templateId == 759) || 
        (isBarrelRack()) || (templateId == 1312) || (templateId == 1309) || (templateId == 1315) || (templateId == 1393)) {
        if (isEmpty(false)) {
          builder.append("empty.");
        } else {
          builder.append("full.");
        }
      }
      if (templateId == 580) {
        if (isMerchantAbsentOrEmpty()) {
          builder.append("empty.");
        }
      }
      if (((templateId == 1239) || (templateId == 1175)) && (getAuxData() > 0)) {
        if (!WurmCalendar.isSeasonWinter()) {
          builder.append("queen.");
        }
      }
      if (isHarvestable()) {
        builder.append("harvestable.");
      }
      if (isVisibleDecay()) {
        if (this.damage >= 50.0F) {
          builder.append("decayed.");
        } else if (this.damage >= 25.0F) {
          builder.append("worn.");
        }
      }
      if ((this.damage >= 80.0F) && (templateId == 321)) {
        builder.append("decay.");
      }
      if ((templateId == 1396) && (isPlanted()))
      {
        builder.append("planted.");
        if (isOnFire()) {
          builder.append("lit.");
        }
      }
      builder.append(WurmCalendar.getSpecialMapping(false));
      builder.append(getMaterialString(getMaterial()) + ".");
      
      return builder.toString();
    }
    if (isDuelRing())
    {
      builder.append(this.template.getModelName());
      builder.append(getMaterialString(getMaterial()));
      builder.append(".");
      if (getKingdom() != 0) {
        builder.append(Kingdoms.getSuffixFor(getKingdom()));
      }
      return builder.toString();
    }
    if (getTemplateId() == 742)
    {
      String hotaModel = "model.decoration.statue.hota.";
      builder.append("model.decoration.statue.hota.");
      switch (getAuxData() % 10)
      {
      case 0: 
        if (getData1() == 1) {
          builder.append("femalefightinganaconda.");
        } else {
          builder.append("dogsfightingboar.");
        }
        break;
      case -1: 
      case 1: 
        builder.append("wolffightingbison.");
        break;
      case -2: 
      case 2: 
        builder.append("deer.");
        break;
      case -3: 
      case 3: 
        builder.append("bearfightingbull.");
        break;
      case -4: 
      case 4: 
        builder.append("blackdragon.");
        break;
      case -5: 
      case 5: 
        builder.append("ladylake.");
        break;
      case -6: 
      case 6: 
        builder.append("nogump.");
        break;
      case -7: 
      case 7: 
        builder.append("manfightingbear.");
        break;
      case -8: 
      case 8: 
        builder.append("soldemon.");
        break;
      case -9: 
      case 9: 
        builder.append("scorpion.");
        break;
      default: 
        builder.append("dogsfightingboar.");
      }
      builder.append(WurmCalendar.getSpecialMapping(false));
      builder.append(getMaterialString(getMaterial()));
      return builder.toString();
    }
    if ((getTemplateId() == 821) || (getTemplateId() == 822))
    {
      builder.append(this.template.getModelName());
      if (this.damage >= 50.0F) {
        builder.append("decayed");
      }
      return builder.toString();
    }
    if (getTemplateId() == 302)
    {
      builder.append(this.template.getModelName());
      if (getName().equalsIgnoreCase("Black bear fur")) {
        builder.append("blackbear");
      } else if (getName().equalsIgnoreCase("Brown bear fur")) {
        builder.append("brownbear");
      } else if (getName().equalsIgnoreCase("Black wolf fur")) {
        builder.append("wolf");
      } else {
        builder.append(getMaterialString(getMaterial()));
      }
      builder.append(".");
      return builder.toString();
    }
    if (getTemplateId() == 1162)
    {
      builder.append(this.template.getModelName());
      ItemTemplate rt = getRealTemplate();
      if (rt != null)
      {
        builder.append(rt.getName().replace(" ", ""));
        
        int age = getAuxData() & 0x7F;
        if (age == 0) {
          builder.append(".0");
        } else if (age < 5) {
          builder.append(".1");
        } else if (age < 10) {
          builder.append(".2");
        } else if (age < 65) {
          builder.append(".3");
        } else if (age < 75) {
          builder.append(".4");
        } else if (age < 95) {
          builder.append(".5");
        } else {
          builder.append(".6");
        }
      }
      return builder.toString();
    }
    builder.append(this.template.getModelName());
    String rtName = "";
    if ((getRealTemplateId() != -10L) && (!isLight())) {
      rtName = getRealTemplate().getName() + ".".replace(" ", "");
    }
    if (usesFoodState())
    {
      switch (getRightAuxData())
      {
      case 1: 
        builder.append("fried.");
        break;
      case 2: 
        builder.append("grilled.");
        break;
      case 3: 
        builder.append("boiled.");
        break;
      case 4: 
        builder.append("roasted.");
        break;
      case 5: 
        builder.append("steamed.");
        break;
      case 6: 
        builder.append("baked.");
        break;
      case 7: 
        builder.append("cooked.");
        break;
      case 8: 
        builder.append("candied.");
        break;
      case 9: 
        builder.append("chocolate.");
      }
      if (isChoppedBitSet())
      {
        if ((isHerb()) || (isVegetable()) || (isFish()) || (this.template.isMushroom)) {
          builder.append("chopped.");
        } else if (isMeat()) {
          builder.append("diced.");
        } else if (isSpice()) {
          builder.append("ground.");
        } else if (canBeFermented()) {
          builder.append("unfermented.");
        } else if (getTemplateId() == 1249) {
          builder.append("whipped.");
        } else {
          builder.append("zombified.");
        }
      }
      else if (isMashedBitSet()) {
        if (isMeat()) {
          builder.append("minced.");
        } else if (isVegetable()) {
          builder.append("mashed.");
        } else if (canBeFermented()) {
          builder.append("fermenting.");
        } else if (getTemplateId() == 1249) {
          builder.append("clotted.");
        } else if (isFish()) {
          builder.append("underweight.");
        }
      }
      if (isWrappedBitSet()) {
        if (canBeDistilled()) {
          builder.append("undistilled.");
        } else {
          builder.append("wrapped.");
        }
      }
      if (isFreshBitSet())
      {
        if ((isHerb()) || (isSpice())) {
          builder.append("fresh.");
        }
        if (isFish()) {
          builder.append("live.");
        }
      }
      builder.append(rtName);
      builder.append(getMaterialString(getMaterial()));
    }
    else if (getTemplateId() == 1281)
    {
      builder.append(rtName);
      builder.append(getMaterialString(getMaterial()));
    }
    else if (getTemplateId() == 729)
    {
      if (getAuxData() > 0) {
        builder.append("birthday.");
      }
      builder.append(rtName);
      builder.append(getMaterialString(getMaterial()));
    }
    else
    {
      builder.append(rtName);
      builder.append(getMaterialString(getMaterial()));
      if (((templateId == 178) || (templateId == 180) || (isFireplace()) || 
        (isBrazier()) || (templateId == 1178) || (templateId == 1301)) && 
        (isOnFire())) {
        builder.append(".lit");
      } else if ((templateId == 1243) && (isOnFire())) {
        builder.append(".smoke");
      }
    }
    return builder.toString();
  }
  
  public boolean isFull()
  {
    return getFreeVolume() < getVolume() / 2;
  }
  
  private boolean isMerchantAbsentOrEmpty()
  {
    try
    {
      TilePos tilePos = getTilePos();
      Zone zone = Zones.getZone(tilePos, this.surfaced);
      VolaTile tile = zone.getTileOrNull(tilePos);
      if (tile == null)
      {
        logWarn("No tile found in zone.");
        return true;
      }
      for (Creature creature : tile.getCreatures()) {
        if (creature.isNpcTrader())
        {
          Shop shop = creature.getShop();
          if (shop == null) {
            break;
          }
          return (shop.getOwnerId() != -10L) && (shop.getNumberOfItems() == 0);
        }
      }
    }
    catch (NoSuchZoneException nsze)
    {
      logWarn(nsze.getMessage(), nsze);
    }
    return true;
  }
  
  public final void setBusy(boolean busy)
  {
    this.isBusy = busy;
    if ((getTemplateId() == 1344) || (getTemplateId() == 1346))
    {
      setIsNoPut(busy);
      Item[] fishingItems = getFishingItems();
      if (fishingItems[0] != null)
      {
        fishingItems[0].setBusy(busy);
        fishingItems[0].setIsNoPut(busy);
      }
      if (fishingItems[1] != null)
      {
        fishingItems[1].setBusy(busy);
        fishingItems[1].setIsNoPut(busy);
      }
      if (fishingItems[2] != null) {
        fishingItems[2].setBusy(busy);
      }
      if (fishingItems[3] != null)
      {
        fishingItems[3].setBusy(busy);
        fishingItems[3].setIsNoPut(busy);
      }
      if (fishingItems[4] != null) {
        fishingItems[4].setBusy(busy);
      }
    }
  }
  
  public final String getConcatName()
  {
    return this.template.getConcatName();
  }
  
  public final boolean isBusy()
  {
    return (this.isBusy) || (this.tradeWindow != null);
  }
  
  private float getDifficulty()
  {
    return this.template.getDifficulty();
  }
  
  public boolean hasPrimarySkill()
  {
    return this.template.hasPrimarySkill();
  }
  
  public final int getPrimarySkill()
    throws NoSuchSkillException
  {
    return this.template.getPrimarySkill();
  }
  
  public final byte getAuxData()
  {
    if ((getTemplateId() == 621) && (this.auxbyte == 0) && (getItemCount() > 0)) {
      for (Item i : getItems())
      {
        if (i.getTemplateId() == 1333) {
          return 1;
        }
        if (i.getTemplateId() == 1334) {
          return 2;
        }
      }
    }
    return this.auxbyte;
  }
  
  public final byte getActualAuxData()
  {
    return this.auxbyte;
  }
  
  public final short getTemperature()
  {
    return this.temperature;
  }
  
  public final boolean isPurchased()
  {
    return this.template.isPurchased();
  }
  
  public void checkSaveDamage() {}
  
  public final void addEffect(Effect effect)
  {
    if (this.effects == null) {
      this.effects = new HashSet();
    }
    if (!this.effects.contains(effect)) {
      this.effects.add(effect);
    }
  }
  
  private void deleteEffect(Effect effect)
  {
    if ((this.effects != null) && (effect != null))
    {
      this.effects.remove(effect);
      EffectFactory.getInstance().deleteEffect(effect.getId());
      if (this.effects.isEmpty()) {
        this.effects = null;
      }
    }
  }
  
  public final void deleteAllEffects()
  {
    Iterator<Effect> it;
    if (this.effects != null) {
      for (it = this.effects.iterator(); it.hasNext();)
      {
        Effect toremove = (Effect)it.next();
        if (toremove != null) {
          EffectFactory.getInstance().deleteEffect(toremove.getId());
        }
      }
    }
    this.effects = null;
  }
  
  @Nonnull
  public final Effect[] getEffects()
  {
    if (this.effects != null) {
      return (Effect[])this.effects.toArray(new Effect[this.effects.size()]);
    }
    return emptyEffects;
  }
  
  private float getMaterialRepairTimeMod()
  {
    if (Features.Feature.METALLIC_ITEMS.isEnabled()) {
      switch (getMaterial())
      {
      case 56: 
        return 0.9F;
      case 31: 
        return 0.975F;
      case 10: 
        return 1.075F;
      case 57: 
        return 0.95F;
      case 7: 
        return 1.05F;
      case 12: 
        return 1.1F;
      case 67: 
        return 0.95F;
      case 9: 
        return 0.975F;
      case 34: 
        return 1.025F;
      case 13: 
        return 1.05F;
      }
    }
    return 1.0F;
  }
  
  public final short getRepairTime(Creature mender)
  {
    int time = 32767;
    Skills skills = mender.getSkills();
    Skill repair = null;
    try
    {
      repair = skills.getSkill(10035);
    }
    catch (NoSuchSkillException nss)
    {
      repair = skills.learn(10035, 1.0F);
    }
    if (repair == null) {
      return (short)time;
    }
    float cq = getCurrentQualityLevel();
    
    float diffcq = this.originalQualityLevel - cq;
    float weightmod = 1.0F;
    double diff = this.template.getDifficulty();
    if (this.realTemplate > 0) {
      diff = getRealTemplate().getDifficulty();
    }
    if (getWeightGrams() > 100000) {
      weightmod = Math.min(3, getWeightGrams() / 100000);
    }
    time = (int)Math.max(20.0D, (this.damage + diffcq) * weightmod / 4.0F * (100.0D - repair.getChance(diff, null, 0.0D)));
    time = (int)(time * getMaterialRepairTimeMod());
    
    time = Math.min(32767, time);
    
    return (short)time;
  }
  
  public final double repair(@Nonnull Creature mender, short aTimeleft, float initialDamage)
  {
    float timeleft = (float)Math.max(1.0D, Math.floor(aTimeleft / 10.0F));
    Skills skills = mender.getSkills();
    Skill repair = skills.getSkillOrLearn(10035);
    double power = repair.skillCheck(getDifficulty(), 0.0D, false, 1.0F);
    
    float cq = getCurrentQualityLevel();
    
    float diffcq = Math.max(this.originalQualityLevel, this.qualityLevel) - cq;
    float runeModifier = 1.0F;
    if (getSpellEffects() != null) {
      runeModifier = getSpellEffects().getRuneEffect(RuneUtilities.ModifierEffect.ENCH_REPAIRQL);
    }
    float newOrigcq = getQualityLevel() - (float)(diffcq * (100.0D - power) / 125.0D) / timeleft / (getRarity() + 1.0F) * runeModifier;
    setQualityLevel(Math.max(1.0F, newOrigcq));
    setOriginalQualityLevel(Math.max(1.0F, newOrigcq));
    
    float oldDamage = getDamage();
    
    setDamage(this.damage - initialDamage / timeleft);
    
    sendUpdate();
    if (isVisibleDecay()) {
      if (((this.damage < 50.0F) && (oldDamage >= 50.0F)) || ((this.damage < 25.0F) && (oldDamage >= 25.0F))) {
        updateModelNameOnGroundItem();
      }
    }
    return power;
  }
  
  public final long getTopParent()
  {
    if (getParentId() == -10L) {
      return this.id;
    }
    try
    {
      if (getParentId() == this.id)
      {
        logWarn(getName() + " has itself as parent!:" + this.id);
        return this.id;
      }
      Item parent = Items.getItem(getParentId());
      return parent.getTopParent();
    }
    catch (NoSuchItemException nsi)
    {
      logWarn("Item " + this.id + "," + getName() + " has parentid " + getParentId() + " but that doesn't exist?", new Exception());
    }
    return -10L;
  }
  
  public final Item getTopParentOrNull()
  {
    long topId = getTopParent();
    if (topId == -10L) {
      return null;
    }
    try
    {
      return Items.getItem(topId);
    }
    catch (NoSuchItemException nsi)
    {
      String message = StringUtil.format("Unable to find top parent with ID: %d.", new Object[] {
      
        Long.valueOf(topId) });
      logWarn(message, nsi);
    }
    return null;
  }
  
  private boolean hasNoParent()
  {
    return this.parentId == -10L;
  }
  
  private boolean hasSameOwner(Item item)
  {
    return item.getOwnerId() == this.ownerId;
  }
  
  public final int getValue()
  {
    if ((isCoin()) || (isFullprice())) {
      return this.template.getValue();
    }
    if (isChallengeNewbieItem()) {
      return 0;
    }
    int val = this.template.getValue();
    if (isCombine())
    {
      float nums = getWeightGrams() / this.template.getWeightGrams();
      val = (int)(nums * (this.template.getValue() * getQualityLevel() * getQualityLevel() / 10000.0F * ((100.0F - getDamage()) / 100.0F)));
    }
    else
    {
      val = (int)(this.template.getValue() * getQualityLevel() * getQualityLevel() / 10000.0F * ((100.0F - getDamage()) / 100.0F));
    }
    if (this.template.priceAffectedByMaterial) {
      val = (int)(val * getMaterialPriceModifier());
    }
    if (this.rarity > 0) {
      val *= this.rarity;
    }
    return val;
  }
  
  private float getMaterialPriceModifier()
  {
    if (Features.Feature.METALLIC_ITEMS.isEnabled())
    {
      switch (getMaterial())
      {
      case 56: 
        return 8.0F;
      case 30: 
        return 6.0F;
      case 31: 
        return 5.0F;
      case 10: 
        return 5.0F;
      case 57: 
        return 10.0F;
      case 7: 
        return 10.0F;
      case 12: 
        return 0.75F;
      case 67: 
        return 12.0F;
      case 8: 
        return 8.0F;
      case 9: 
        return 2.5F;
      case 13: 
        return 0.9F;
      case 96: 
        return 9.0F;
      }
    }
    else
    {
      if (this.material == -10L) {
        return 1.0F;
      }
      if (this.material == 7) {
        return 10.0F;
      }
      if (this.material == 8) {
        return 8.0F;
      }
      if ((this.material == 31) || (this.material == 30)) {
        return 6.0F;
      }
      if ((this.material == 10) || (this.material == 9)) {
        return 5.0F;
      }
      return 1.0F;
    }
    return 1.0F;
  }
  
  public final ArmourTemplate.ArmourType getArmourType()
  {
    ArmourTemplate armourTemplate = ArmourTemplate.getArmourTemplate(this);
    if (armourTemplate != null) {
      return armourTemplate.getArmourType();
    }
    return null;
  }
  
  public final boolean moveToItem(Creature mover, long targetId, boolean lastMove)
    throws NoSuchItemException, NoSuchPlayerException, NoSuchCreatureException
  {
    Item target = Items.getItem(targetId);
    if ((isNoTake()) && ((!getParent().isVehicle()) || (!target.isVehicle()))) {
      return false;
    }
    if (isComponentItem()) {
      return false;
    }
    if (isBodyPartAttached()) {
      return false;
    }
    if ((this.parentId == -10L) || (!getParent().isInventory()) || (target.getTemplateId() != 1315)) {
      if ((this.parentId == -10L) || (getParent().getTemplateId() != 1315) || (!target.isInventory())) {
        if ((getTemplate().isTransportable()) && (getTopParent() != getWurmId()) && (((!getParent().isVehicle()) && (getParent().getTemplateId() != 1312) && (getParent().getTemplateId() != 1309)) || (
          (!target.isVehicle()) && (target.getTemplateId() != 1312) && (target.getTemplateId() != 1309))))
        {
          mover.getCommunicator().sendNormalServerMessage("The " + 
            getName() + " will not fit in the " + target.getName() + ".");
          return false;
        }
      }
    }
    boolean toReturn = false;
    long lOwnerId = getOwnerId();
    Creature itemOwner = null;
    long targetOwnerId = target.getOwnerId();
    if (target.getTemplateId() == 1309)
    {
      if (!target.isPlanted())
      {
        mover.getCommunicator().sendNormalServerMessage("The wagoner container must be planted so it can accept crates.");
        return false;
      }
      if (target.getTopParent() != target.getWurmId())
      {
        mover.getCommunicator().sendNormalServerMessage(
          StringUtil.format("You are not allowed to do that! The %s must be on the ground.", new Object[] {target.getName() }));
        return false;
      }
      if (!isCrate())
      {
        mover.getCommunicator().sendNormalServerMessage("Only crates fit in the wagoner container.");
        return false;
      }
    }
    if (target.isTent())
    {
      if (target.getTopParent() != target.getWurmId())
      {
        mover.getCommunicator().sendNormalServerMessage(
          StringUtil.format("You are not allowed to do that! The %s must be on the ground.", new Object[] {target.getName() }));
        return false;
      }
      if ((target.isTent()) && (target.isNewbieItem()) && (target.getLastOwnerId() != mover.getWurmId())) {
        if (!Servers.localServer.PVPSERVER)
        {
          mover.getCommunicator().sendNormalServerMessage("You don't want to put things in other people's tents since you aren't allowed to remove them.");
          
          return false;
        }
      }
    }
    if ((target.banked) || (target.mailed)) {
      return false;
    }
    Item targetTopParent = target.getTopParentOrNull();
    if ((targetTopParent != null) && (
      (targetTopParent.getTemplateId() == 853) || (targetTopParent.getTemplateId() == 1410))) {
      return false;
    }
    if ((targetTopParent != null) && (targetTopParent.isHollow()) && (!targetTopParent.isInventory()) && (isTent()) && 
      (isNewbieItem()))
    {
      mover.getCommunicator().sendNormalServerMessage("You want to keep your tent easily retrievable.");
      return false;
    }
    if (lOwnerId != -10L)
    {
      itemOwner = Server.getInstance().getCreature(lOwnerId);
      if ((this.id == itemOwner.getBody().getBodyItem().getWurmId()) || 
        (this.id == itemOwner.getPossessions().getInventory().getWurmId())) {
        return false;
      }
      if ((targetOwnerId == -10L) || (targetOwnerId != lOwnerId)) {
        if ((itemOwner.getPower() < 3) && (!canBeDropped(true)))
        {
          if (isHollow())
          {
            if (itemOwner.equals(mover)) {
              itemOwner.getCommunicator().sendSafeServerMessage("You are not allowed to drop that. It may contain a non-droppable item.");
            }
          }
          else if (itemOwner.equals(mover)) {
            itemOwner.getCommunicator().sendSafeServerMessage("You are not allowed to drop that.");
          }
          return false;
        }
      }
    }
    boolean pickup = false;
    Creature targetOwner = null;
    if ((targetOwnerId != -10L) && (lOwnerId != targetOwnerId))
    {
      int lWeight = getFullWeight();
      if ((isLiquid()) && (target.isContainerLiquid())) {
        lWeight = Math.min(lWeight, target.getFreeVolume());
      }
      targetOwner = Server.getInstance().getCreature(targetOwnerId);
      if ((!targetOwner.canCarry(lWeight)) && (lWeight != 0))
      {
        if (targetOwner.equals(mover)) {
          targetOwner.getCommunicator().sendSafeServerMessage("You cannot carry that much.");
        }
        return false;
      }
      if (lOwnerId == -10L)
      {
        pickup = true;
        try
        {
          boolean ok = (targetOwner.isKingdomGuard()) && (targetOwner.getKingdomId() == mover.getKingdomId());
          if ((!Servers.isThisAPvpServer()) && (targetOwner.isBranded())) {
            ok = (ok) || (targetOwner.mayAccessHold(mover));
          } else {
            ok = (ok) || (targetOwner.getDominator() == mover);
          }
          if ((mover.getWurmId() != targetOwnerId) && (!ok))
          {
            mover.getCommunicator().sendNormalServerMessage("You can't give the " + 
              getName() + " to " + targetOwner.getName() + " like that.");
            return false;
          }
          Zone zone = Zones.getZone((int)getPosX() >> 2, (int)getPosY() >> 2, isOnSurface());
          VolaTile tile = zone.getTileOrNull((int)getPosX() >> 2, (int)getPosY() >> 2);
          if (tile != null)
          {
            Structure struct = tile.getStructure();
            
            VolaTile tile2 = targetOwner.getCurrentTile();
            if (tile2 != null)
            {
              if (tile.getStructure() != struct)
              {
                targetOwner.getCommunicator().sendNormalServerMessage("You can't reach the " + 
                  getName() + " through the wall.");
                return false;
              }
            }
            else if (struct != null)
            {
              targetOwner.getCommunicator().sendNormalServerMessage("You can't reach the " + 
                getName() + " through the wall.");
              return false;
            }
          }
          else
          {
            logWarn("No tile found in zone.");
            return false;
          }
        }
        catch (NoSuchZoneException nsz)
        {
          if (itemOwner != null)
          {
            logWarn(itemOwner.getName() + ":" + nsz.getMessage(), nsz);
            return false;
          }
          if (this.parentId != -10L)
          {
            logInfo("Parent is not NOID Look at the following exception:");
            Item p = Items.getItem(this.parentId);
            logWarn(nsz.getMessage() + " id=" + this.id + ' ' + getName() + ' ' + (
              WurmId.getType(this.parentId) == 6) + " parent=" + p.getName() + " ownerid=" + lOwnerId);
          }
          else
          {
            logInfo(
              targetOwner.getName() + " trying to scam ZONEID=" + getZoneId() + ", Parent=NOID " + nsz.getMessage() + " id=" + this.id + ' ' + getName() + " ownerid=" + lOwnerId + ". Close these windows sometime.");
            
            return false;
          }
        }
      }
      else if (isCreatureWearableOnly())
      {
        if ((mover.getVehicle() != -10L) && (itemOwner.getWurmId() == mover.getWurmId()))
        {
          mover.getCommunicator().sendNormalServerMessage("You need to be standing on the ground to do that.");
          return false;
        }
      }
    }
    long pid = getParentId();
    if ((pid == target.getWurmId()) || (this.id == target.getParentId())) {
      return false;
    }
    if ((!target.isHollow()) && (pid == target.getParentId())) {
      return false;
    }
    if (target.isBodyPart())
    {
      boolean found = false;
      for (int x = 0; x < getBodySpaces().length; x++) {
        if (getBodySpaces()[x] == target.getPlace()) {
          found = true;
        }
      }
      if (!found) {
        if ((target.getPlace() != 13) && (target.getPlace() != 14) && (
          (target.getPlace() < 35) || (target.getPlace() >= 48)))
        {
          if (itemOwner != null) {
            itemOwner.getCommunicator().sendNormalServerMessage("The " + 
              getName() + " will not fit in the " + target.getName() + ".");
          } else if (targetOwner != null) {
            targetOwner.getCommunicator().sendNormalServerMessage("The " + 
              getName() + " will not fit in the " + target.getName() + ".");
          } else if (mover != null) {
            mover.getCommunicator().sendNormalServerMessage("The " + 
              getName() + " will not fit in the " + target.getName() + ".");
          }
          return false;
        }
      }
    }
    Item targetParent = null;
    
    Item parent = null;
    if (this.parentId != -10L) {
      parent = Items.getItem(this.parentId);
    }
    Item topParent;
    if (target.hasNoParent())
    {
      if ((target.isNoPut()) && (!insertOverrideNoPut(this, target))) {
        return false;
      }
    }
    else
    {
      if ((target.isNoPut()) && (!insertOverrideNoPut(this, target))) {
        return false;
      }
      targetParent = Items.getItem(target.getParentId());
      if ((targetParent.isNoPut()) && (!insertOverrideNoPut(this, targetParent))) {
        return false;
      }
      if (targetParent.isMailBox())
      {
        mover.getCommunicator().sendNormalServerMessage("The spirits refuse to let you do that now.");
        return false;
      }
      topParent = null;
      try
      {
        topParent = Items.getItem(target.getTopParent());
        if ((topParent.isNoPut()) && (!insertOverrideNoPut(this, topParent))) {
          return false;
        }
        if (topParent.isMailBox())
        {
          mover.getCommunicator().sendNormalServerMessage("The spirits refuse to let you do that now.");
          return false;
        }
      }
      catch (NoSuchItemException nsi)
      {
        logWarn(nsi.getMessage(), nsi);
        return false;
      }
    }
    if (target.getTemplateId() == 1023)
    {
      if (!isUnfired())
      {
        mover.getCommunicator().sendNormalServerMessage("Only unfired clay items can be put into a kiln.");
        return false;
      }
      if (targetParent != null)
      {
        mover.getCommunicator().sendNormalServerMessage("You cannot reach that whilst it is in a kiln.");
        return false;
      }
    }
    if ((target.getTemplateId() == 1028) && (!isOre()))
    {
      mover.getCommunicator().sendNormalServerMessage("Only ore can be put into a smelter.");
      return false;
    }
    if ((target.isComponentItem()) && (targetParent == null))
    {
      mover.getCommunicator().sendNormalServerMessage("You cannot put items in the " + target.getName() + " as it does not seem to be in anything.");
      
      return false;
    }
    if (target.getTemplateId() == 1435)
    {
      if (getTemplateId() != 128)
      {
        mover.getCommunicator().sendNormalServerMessage("You can only put water into the drinker.");
        return false;
      }
    }
    else if (target.getTemplateId() == 1434) {
      if (!isSeed())
      {
        mover.getCommunicator().sendNormalServerMessage("You can only put seeds into the feeder.");
        return false;
      }
    }
    if (target.getTemplateId() == 1432)
    {
      mover.getCommunicator().sendNormalServerMessage("You can't put that there.");
      return false;
    }
    if ((target.isParentMustBeOnGround()) && (targetParent.getParentId() != -10L))
    {
      mover.getCommunicator().sendNormalServerMessage("You cannot put items on the " + target.getName() + " whilst the " + targetParent
        .getName() + " is not on the ground.");
      return false;
    }
    if ((target.getTemplateId() == 1278) && (getTemplateId() != 1276))
    {
      mover.getCommunicator().sendNormalServerMessage("Only snowballs can be put into an icebox.");
      return false;
    }
    if ((target.getTemplateId() == 1108) && (getTemplateId() != 768))
    {
      mover.getCommunicator().sendNormalServerMessage("Only wine barrels can be put on that rack.");
      return false;
    }
    if ((target.getTemplateId() == 1109) && (getTemplateId() != 189))
    {
      mover.getCommunicator().sendNormalServerMessage("Only small barrels can be put into that rack.");
      return false;
    }
    if ((target.getTemplateId() == 1110) && (getTemplateId() != 1161) && (getTemplateId() != 1162))
    {
      mover.getCommunicator().sendNormalServerMessage("Only herb and spice planters can be put into that rack.");
      return false;
    }
    if ((target.getTemplateId() == 1111) && (getTemplateId() != 1022) && (getTemplateId() != 1020))
    {
      mover.getCommunicator().sendNormalServerMessage("Only amphora can be put into that rack.");
      return false;
    }
    if ((target.getTemplateId() == 1279) && (!canLarder()) && ((!usesFoodState()) || (getAuxData() != 0)))
    {
      mover.getCommunicator().sendNormalServerMessage("Only processed food items can be put onto the shelf.");
      return false;
    }
    if ((target.getTemplateId() == 1120) && (isBarrelRack()))
    {
      mover.getCommunicator().sendNormalServerMessage("The " + getName() + " will not fit onto the shelf.");
      return false;
    }
    if ((target.isAlmanacContainer()) && (!isHarvestReport()))
    {
      mover.getCommunicator().sendNormalServerMessage("Only harvest reports can be put in " + target
        .getTemplate().getNameWithGenus() + ".");
      return false;
    }
    if ((target.getTemplateId() == 1312) && (!isCrate()))
    {
      mover.getCommunicator().sendNormalServerMessage("Only crates can be put into that rack.");
      return false;
    }
    if ((target.getTemplateId() == 1315) && ((getTemplateId() != 662) || (!isEmpty(false))))
    {
      mover.getCommunicator().sendNormalServerMessage("Only empty bsb can be put into that rack.");
      return false;
    }
    if (target.getTemplateId() == 1341)
    {
      topParent = target.getItemsAsArray();nsi = topParent.length;
      for (NoSuchItemException localNoSuchItemException1 = 0; localNoSuchItemException1 < nsi; localNoSuchItemException1++)
      {
        Item compartment = topParent[localNoSuchItemException1];
        if (compartment.doesContainerRestrictionsAllowItem(this))
        {
          target = compartment;
          break;
        }
      }
    }
    if (!target.canHold(this))
    {
      mover.getCommunicator().sendNormalServerMessage("There isn't enough room to fit " + getNameWithGenus() + " in " + target
        .getNameWithGenus() + ".");
      return false;
    }
    if ((target.getTemplateId() == 1295) && (!canLarder()) && ((!usesFoodState()) || (getAuxData() != 0)))
    {
      mover.getCommunicator().sendNormalServerMessage("Only processed food items can be put into the food tin.");
      return false;
    }
    if ((!isLiquid()) && (target.getTemplateId() == 1294))
    {
      mover.getCommunicator().sendNormalServerMessage("Only liquids may be put in a thermos.");
      return false;
    }
    if ((!isLiquid()) && (target.getTemplateId() == 1118))
    {
      mover.getCommunicator().sendNormalServerMessage("Only liquids may be put into an alchemy storage vial.");
      return false;
    }
    if ((target.containsIngredientsOnly()) && (!isFood()) && (!isLiquid()) && (!isRecipeItem()))
    {
      mover.getCommunicator().sendNormalServerMessage("Only ingredients that are used to make food can be put onto " + target.getNameWithGenus() + ".");
      return false;
    }
    if (target.getTemplateId() == 1284) {
      if ((targetParent != null) && (targetParent.getTemplateId() == 1178) && (targetParent.getParentId() != -10L))
      {
        mover.getCommunicator().sendNormalServerMessage("You can only put liquids into the boiler when the still is not on the ground.");
        return false;
      }
    }
    if (isLiquid())
    {
      if ((!target.isContainerLiquid()) && (target.getTemplateId() != 75)) {
        target = targetParent;
      }
      if ((target != null) && ((target.isContainerLiquid()) || (target.getTemplateId() == 75)))
      {
        if ((target.getTemplateId() == 1284) && (target.isEmpty(false)))
        {
          Item topParent = target.getTopParentOrNull();
          if ((topParent != null) && (topParent.getTemplateId() == 1178))
          {
            Item condenser = null;
            for (Object it = topParent.getItems().iterator(); ((Iterator)it).hasNext();)
            {
              Item contained = (Item)((Iterator)it).next();
              if (contained.getTemplateId() == 1285) {
                condenser = contained;
              }
            }
            if (condenser != null)
            {
              Item[] contents = condenser.getItemsAsArray();
              if (contents.length != 0) {
                if ((contents[0].getTemplateId() != getTemplateId()) || (contents[0].getRealTemplateId() != getRealTemplateId()) || 
                  (contents[0].getRarity() != getRarity()))
                {
                  mover.getCommunicator().sendNormalServerMessage("That would destroy the " + contents[0].getName() + ".");
                  return false;
                }
              }
            }
          }
        }
        Item contained = null;
        Item liquid = null;
        int volAvail = target.getContainerVolume();
        for (Iterator<Item> it = target.getItems().iterator(); it.hasNext();)
        {
          contained = (Item)it.next();
          if (MethodsItems.wouldDestroyLiquid(target, contained, this))
          {
            mover.getCommunicator().sendNormalServerMessage("That would destroy the liquid.");
            return false;
          }
          if (contained.isLiquid())
          {
            if (((!target.isContainerLiquid()) && (target.getTemplateId() != 75)) || ((contained.getTemplateId() == getTemplateId()) && 
              (contained.getRealTemplateId() == getRealTemplateId()) && (contained.getLeftAuxData() == getLeftAuxData()))) {
              liquid = contained;
            }
            if (contained.isSalted() != isSalted()) {
              liquid = contained;
            }
            volAvail -= contained.getWeightGrams();
          }
          else
          {
            volAvail -= contained.getVolume();
          }
        }
        contained = liquid;
        if (contained != null)
        {
          if (getRarity() != contained.getRarity())
          {
            if (mover != null) {
              mover.getCommunicator().sendNormalServerMessage("The " + 
                getName() + " or the " + contained.getName() + " would lose its rarity.");
            }
            return false;
          }
          if (isSalted() != contained.isSalted())
          {
            if (mover != null) {
              mover.getCommunicator().sendNormalServerMessage("Cannot mix salty water with non-salty water.");
            }
            return false;
          }
        }
        if (volAvail > 0)
        {
          if (volAvail < getWeightGrams())
          {
            if (contained == null)
            {
              try
              {
                Item splitItem = MethodsItems.splitLiquid(this, volAvail, mover);
                target.insertItem(splitItem);
              }
              catch (FailedException fe)
              {
                logWarn(fe.getMessage(), fe);
                return false;
              }
              catch (NoSuchTemplateException nst)
              {
                logWarn(nst.getMessage(), nst);
                return false;
              }
            }
            else
            {
              if ((contained.getTemplateId() == 417) || (getTemplateId() == 417)) {
                if (contained.getRealTemplateId() != getRealTemplateId())
                {
                  String name1 = "fruit";
                  String name2 = "fruit";
                  ItemTemplate t = contained.getRealTemplate();
                  if (t != null) {
                    name1 = t.getName();
                  }
                  ItemTemplate t2 = getRealTemplate();
                  if (t2 != null) {
                    name2 = t2.getName();
                  }
                  if (!name1.equals(name2)) {
                    contained.setName(name1 + " and " + name2 + " juice");
                  }
                  contained.setRealTemplate(-10);
                }
              }
              setWeight(getWeightGrams() - volAvail, true, targetOwner != itemOwner);
              int allWeight = contained.getWeightGrams() + volAvail;
              
              float newQl = (getCurrentQualityLevel() * volAvail + contained.getCurrentQualityLevel() * contained.getWeightGrams()) / allWeight;
              if ((contained.isColor()) && (this.color != -1)) {
                contained.setColor(WurmColor.mixColors(contained.color, contained.getWeightGrams(), this.color, volAvail, newQl));
              }
              contained.setQualityLevel(newQl);
              contained.setWeight(contained.getWeightGrams() + volAvail, true, targetOwner != itemOwner);
              contained.setDamage(0.0F);
            }
          }
          else
          {
            if (contained != null)
            {
              if ((contained.getTemplateId() == 417) || (getTemplateId() == 417)) {
                if (contained.getRealTemplateId() != getRealTemplateId())
                {
                  String name1 = "fruit";
                  String name2 = "fruit";
                  ItemTemplate t = contained.getRealTemplate();
                  if (t != null) {
                    name1 = t.getName();
                  }
                  ItemTemplate t2 = getRealTemplate();
                  if (t2 != null) {
                    name2 = t2.getName();
                  }
                  if (!name1.equals(name2)) {
                    contained.setName(name1 + " and " + name2 + " juice");
                  }
                  contained.setRealTemplate(-10);
                }
              }
              int allWeight = getWeightGrams() + contained.getWeightGrams();
              
              float newQl = (getCurrentQualityLevel() * getWeightGrams() + contained.getCurrentQualityLevel() * contained.getWeightGrams()) / allWeight;
              if ((contained.isColor()) && (this.color != -1)) {
                contained.setColor(WurmColor.mixColors(contained.color, contained.getWeightGrams(), this.color, 
                  getWeightGrams(), newQl));
              }
              contained.setQualityLevel(newQl);
              contained.setDamage(0.0F);
              contained.setWeight(allWeight, true);
              Items.destroyItem(this.id);
              
              SoundPlayer.playSound("sound.liquid.fillcontainer", this, 0.1F);
              return false;
            }
            if (target.testInsertItem(this))
            {
              if (parent != null) {
                if (!hasSameOwner(target)) {
                  parent.dropItem(this.id, false);
                } else {
                  parent.removeItem(this.id, false, false, false);
                }
              }
              setLastOwnerId(mover.getWurmId());
              target.insertItem(this);
            }
            else
            {
              return false;
            }
          }
        }
        else {
          return false;
        }
        SoundPlayer.playSound("sound.liquid.fillcontainer", this, 0.1F);
        return true;
      }
      return false;
    }
    if ((target.isContainerLiquid()) || ((targetParent != null) && 
      (targetParent.isContainerLiquid()) && (!target.isHollow())))
    {
      if (!target.isContainerLiquid()) {
        target = targetParent;
      }
      if ((target.getSizeX() >= getSizeX()) && (target.getSizeY() >= getSizeY()) && (target.getSizeZ() > getSizeZ()))
      {
        if (target.getItems().size() > 0)
        {
          Item contained = null;
          Item liquid = null;
          for (Object it = target.getItems().iterator(); ((Iterator)it).hasNext();)
          {
            contained = (Item)((Iterator)it).next();
            if (contained.isLiquid())
            {
              if ((!isFood()) && (!isRecipeItem()))
              {
                mover.getCommunicator().sendNormalServerMessage("That would destroy the liquid.");
                return false;
              }
              liquid = contained;
            }
          }
          if (liquid != null)
          {
            int used = target.getUsedVolume();
            int size = liquid.getWeightGrams();
            int free = target.getVolume() - used;
            if (free < getVolume())
            {
              if (free + size <= getVolume())
              {
                if (itemOwner != null) {
                  itemOwner.getCommunicator().sendNormalServerMessage("The " + 
                    getName() + " will not fit in the " + target.getName() + ".");
                } else if (targetOwner != null) {
                  targetOwner.getCommunicator().sendNormalServerMessage("The " + 
                    getName() + " will not fit in the " + target.getName() + ".");
                } else if (mover != null) {
                  mover.getCommunicator().sendNormalServerMessage("The " + 
                    getName() + " will not fit in the " + target.getName() + ".");
                }
                return false;
              }
              int leftNeeded = getVolume() - free;
              if ((leftNeeded < size) && (leftNeeded > 0))
              {
                liquid.setWeight(size - leftNeeded, true);
                mover.getCommunicator().sendNormalServerMessage("You spill some " + liquid.getName() + ".");
              }
              else if (leftNeeded == size)
              {
                Items.destroyItem(liquid.getWurmId());
                mover.getCommunicator().sendNormalServerMessage("You spill the " + liquid.getName() + ".");
              }
            }
          }
        }
      }
      else
      {
        if (itemOwner != null) {
          itemOwner.getCommunicator().sendNormalServerMessage("The " + 
            getName() + " will not fit in the " + target.getName() + ".");
        } else if (targetOwner != null) {
          targetOwner.getCommunicator().sendNormalServerMessage("The " + 
            getName() + " will not fit in the " + target.getName() + ".");
        } else if (mover != null) {
          mover.getCommunicator().sendNormalServerMessage("The " + 
            getName() + " will not fit in the " + target.getName() + ".");
        }
        return false;
      }
    }
    if ((target.isLockable()) && (target.getLockId() != -10L)) {
      try
      {
        Item lock = Items.getItem(target.getLockId());
        long[] keyIds = lock.getKeyIds();
        Iterator<Item> it;
        for (int x = 0; x < keyIds.length; x++)
        {
          if (this.id == keyIds[x]) {
            return false;
          }
          if (this.items != null) {
            for (it = this.items.iterator(); it.hasNext();)
            {
              Item itkey = (Item)it.next();
              if (itkey.getWurmId() == keyIds[x])
              {
                mover.getCommunicator().sendNormalServerMessage("The " + target
                  .getName() + " is locked with a key inside the " + getName() + ".");
                return false;
              }
            }
          }
        }
      }
      catch (NoSuchItemException nsi)
      {
        logWarn(target.getWurmId() + ": item has a set lock but the lock does not exist?:" + target.getLockId(), nsi);
        
        return false;
      }
    }
    int x;
    Item itkey;
    if ((targetParent != null) && (targetParent.isLockable()) && (targetParent.getLockId() != -10L)) {
      try
      {
        Item lock = Items.getItem(targetParent.getLockId());
        long[] keyIds = lock.getKeyIds();
        Iterator<Item> it;
        for (x = 0; x < keyIds.length; x++)
        {
          if (this.id == keyIds[x]) {
            return false;
          }
          if (this.items != null) {
            for (it = this.items.iterator(); it.hasNext();)
            {
              itkey = (Item)it.next();
              if (itkey.getWurmId() == keyIds[x])
              {
                mover.getCommunicator().sendNormalServerMessage("The " + target
                  .getName() + " is locked with a key inside the " + getName() + ".");
                return false;
              }
            }
          }
        }
      }
      catch (NoSuchItemException nsi)
      {
        logWarn(
          targetParent.getWurmId() + ": item has a set lock but the lock does not exist?:" + targetParent.getLockId(), nsi);
        return false;
      }
    }
    if ((targetParent != null) && (targetParent.isBulkContainer())) {
      target = targetParent;
    }
    Item topParent;
    String message;
    if (target.isBulkContainer())
    {
      if ((isEnchanted()) && (getSpellFoodBonus() == 0.0F))
      {
        if (mover != null) {
          mover.getCommunicator().sendNormalServerMessage("The " + getName() + " would lose its enchants.");
        }
        return false;
      }
      if (getRarity() > 0)
      {
        if (mover != null) {
          mover.getCommunicator().sendNormalServerMessage("The " + getName() + " would lose its rarity.");
        }
        return false;
      }
      topParent = target.getTopParentOrNull();
      if (((target.getTopParent() != target.getWurmId()) && (!target.isCrate()) && (!targetParent.isVehicle()) && 
        (targetParent.getTemplateId() != 1316)) || ((topParent != null) && (
        (topParent.getTemplateId() == 853) || (topParent.getTemplateId() == 1410))))
      {
        if (mover != null)
        {
          message = StringUtil.format("The %s needs to be on the ground.", new Object[] {target
          
            .getName() });
          mover.getCommunicator().sendNormalServerMessage(message);
        }
        return false;
      }
      if (canHaveInscription())
      {
        if ((this.inscription != null) && (this.inscription.hasBeenInscribed()))
        {
          if (mover != null) {
            if ((getAuxData() == 1) || (getAuxData() > 8)) {
              mover.getCommunicator().sendNormalServerMessage("The " + 
                getName() + " would be destroyed.");
            } else {
              mover.getCommunicator().sendNormalServerMessage("The inscription on the " + 
                getName() + " would be destroyed.");
            }
          }
          return false;
        }
        if (getAuxData() != 0)
        {
          if (mover != null) {
            mover.getCommunicator().sendNormalServerMessage("The " + getName() + " would be destroyed.");
          }
          return false;
        }
      }
      if (!isBulk())
      {
        if (mover != null) {
          mover.getCommunicator().sendNormalServerMessage("The " + getName() + " would be destroyed.");
        }
        return false;
      }
      if (isFood())
      {
        if ((target.getTemplateId() != 661) && (!target.isCrate()))
        {
          if (mover != null) {
            mover.getCommunicator().sendNormalServerMessage("The " + getName() + " would be destroyed.");
          }
          return false;
        }
        if ((isDish()) || ((usesFoodState()) && (isFreshBitSet()) && (isChoppedBitSet())))
        {
          if (mover != null) {
            mover.getCommunicator().sendNormalServerMessage("Only unprocessed food items can be stored that way.");
          }
          return false;
        }
      }
      else if ((target.getTemplateId() != 662) && (target.getTemplateId() != 1317) && (!target.isCrate()))
      {
        if (mover != null) {
          mover.getCommunicator().sendNormalServerMessage("The " + getName() + " would be destroyed.");
        }
        return false;
      }
      if (((mover == null) || (getTopParent() != mover.getInventory().getWurmId())) && 
        (MethodsItems.checkIfStealing(this, mover, null)))
      {
        if (mover != null) {
          mover.getCommunicator().sendNormalServerMessage("You're not allowed to put things into this " + target
            .getName() + ".");
        }
        return false;
      }
      if (target.isLocked())
      {
        if ((mover != null) && (!target.mayAccessHold(mover)))
        {
          mover.getCommunicator().sendNormalServerMessage("You're not allowed to put things into this " + target
            .getName() + ".");
          return false;
        }
      }
      else if ((mover != null) && (!Methods.isActionAllowed(mover, (short)7))) {
        return false;
      }
      if ((isFish()) && (isUnderWeight()))
      {
        if (mover != null) {
          mover.getCommunicator().sendNormalServerMessage("The " + 
            getName() + " is not whole, and therefore is not allowed into the " + target.getName() + ".");
        }
        return false;
      }
      if ((target.isCrate()) && (target.canAddToCrate(this))) {
        return AddBulkItemToCrate(mover, target);
      }
      if ((!target.isCrate()) && (target.hasSpaceFor(getVolume()))) {
        return AddBulkItem(mover, target);
      }
      mover.getCommunicator().sendNormalServerMessage("The " + getName() + " doesn't fit.");
      return false;
    }
    if (target.getTemplateId() == 725) {
      if (!isWeaponPolearm())
      {
        mover.getCommunicator().sendNormalServerMessage("The " + getName() + " doesn't fit.");
        return false;
      }
    }
    if (target.getTemplateId() == 724) {
      if ((!isWeapon()) || (isWeaponPolearm()))
      {
        mover.getCommunicator().sendNormalServerMessage("The " + getName() + " doesn't fit.");
        return false;
      }
    }
    if (target.getTemplateId() == 758) {
      if ((!isWeaponBow()) && (!isBowUnstringed()))
      {
        mover.getCommunicator().sendNormalServerMessage("The " + getName() + " doesn't fit.");
        return false;
      }
    }
    if (target.getTemplateId() == 759) {
      if (!isArmour())
      {
        mover.getCommunicator().sendNormalServerMessage("The " + getName() + " doesn't fit.");
        return false;
      }
    }
    if (target.getTemplateId() == 892) {
      if ((!isArmour()) && (getTemplateId() != 831))
      {
        mover.getCommunicator().sendNormalServerMessage("The " + getName() + " doesn't fit.");
        return false;
      }
    }
    if (target.getTemplateId() == 757) {
      if (getTemplateId() != 418)
      {
        mover.getCommunicator().sendNormalServerMessage("The " + getName() + " would be destroyed.");
        return false;
      }
    }
    if (target.isSaddleBags())
    {
      if (isArtifact())
      {
        mover.getCommunicator().sendNormalServerMessage("The " + getName() + " doesn't fit.");
        return false;
      }
      if ((isHollow()) && (containsItem()) && 
        (containsArtifact()))
      {
        mover.getCommunicator().sendNormalServerMessage("The " + getName() + " doesn't fit.");
        return false;
      }
    }
    if (isArtifact()) {
      if (target.isInside(new int[] { 1333, 1334 }))
      {
        mover.getCommunicator().sendNormalServerMessage("The " + getName() + " doesn't fit.");
        return false;
      }
    }
    Creature iwatcher;
    if (target.getTemplateId() == 177)
    {
      if ((isDecoration()) || (!target.mayCreatureInsertItem()))
      {
        mover.getCommunicator().sendNormalServerMessage("The " + getName() + " doesn't fit.");
        return false;
      }
      if (parent != null) {
        if (parent.getTemplateId() == 177)
        {
          parent.dropItem(this.id, false);
          parent.removeFromPile(this, !lastMove);
          if ((parent.getItemCount() < 3) && (lastMove)) {
            for (iwatcher : parent.getWatchers()) {
              iwatcher.getCommunicator().sendCloseInventoryWindow(parent.getWurmId());
            }
          } else if (lastMove) {
            parent.updatePile();
          }
        }
        else
        {
          parent.dropItem(this.id, false);
        }
      }
      setLastOwnerId(mover.getWurmId());
      target.insertIntoPile(this);
      
      toReturn = false;
      if (itemOwner != null) {
        itemOwner.addItemDropped(this);
      }
    }
    else if ((!target.isHollow()) && (targetParent != null) && (targetParent.getTemplateId() == 177))
    {
      if ((isDecoration()) || ((!targetParent.mayCreatureInsertItem()) && (mover.getPower() == 0)))
      {
        mover.getCommunicator().sendNormalServerMessage("The " + getName() + " doesn't fit.");
        return false;
      }
      if (parent != null) {
        parent.dropItem(this.id, false);
      }
      setLastOwnerId(mover.getWurmId());
      targetParent.insertIntoPile(this);
      toReturn = false;
      if (itemOwner != null) {
        itemOwner.addItemDropped(this);
      }
    }
    else
    {
      if (target.testInsertItem(this))
      {
        Item insertTarget = target.getInsertItem();
        boolean mayInsert = insertTarget == null ? false : insertTarget.mayCreatureInsertItem();
        Object p;
        if ((insertTarget.isInventory()) || (insertTarget.isInventoryGroup()))
        {
          p = getParentOrNull();
          if ((p != null) && ((((Item)p).isInventory()) || (((Item)p).isInventoryGroup()))) {
            mayInsert = true;
          }
        }
        if ((target.getTemplateId() == 1404) && ((getTemplateId() == 1272) || (getTemplateId() == 748))) {
          if (getCurrentQualityLevel() < 30.0F)
          {
            if (itemOwner != null) {
              itemOwner.getCommunicator().sendNormalServerMessage("The " + 
                getName() + " is too low quality to be used as a report.");
            } else if (targetOwner != null) {
              targetOwner.getCommunicator().sendNormalServerMessage("The " + 
                getName() + " is too low quality to be used as a report.");
            } else if (mover != null) {
              mover.getCommunicator().sendNormalServerMessage("The " + 
                getName() + " is too low quality to be used as a report.");
            }
            return false;
          }
        }
        if ((mayInsert) || ((itemOwner != null) && (itemOwner.getPower() > 0)) || ((targetOwner != null) && 
          (targetOwner.getPower() > 0)))
        {
          if (parent != null)
          {
            if (!hasSameOwner(target))
            {
              parent.dropItem(this.id, false);
              if (parent.getTemplateId() == 177)
              {
                parent.removeFromPile(this, !lastMove);
                if ((parent.getItemCount() < 3) && (lastMove))
                {
                  p = parent.getWatchers();iwatcher = p.length;
                  for (itkey = 0; itkey < iwatcher; itkey++)
                  {
                    Creature iwatcher = p[itkey];
                    iwatcher.getCommunicator().sendCloseInventoryWindow(parent.getWurmId());
                  }
                }
                else if (lastMove)
                {
                  parent.updatePile();
                }
              }
              if (targetOwner != null)
              {
                targetOwner.addItemTaken(this);
                if (parent.isItemSpawn()) {
                  targetOwner.addChallengeScore(ChallengePointEnum.ChallengePoint.ITEMSLOOTED.getEnumtype(), 0.1F);
                }
                if (pickup) {
                  MissionTriggers.activateTriggers(mover, this, 6, -10L, 1);
                }
              }
              else if (itemOwner != null)
              {
                itemOwner.addItemDropped(this);
              }
            }
            else
            {
              if (parent.getTemplateId() == 177)
              {
                parent.dropItem(this.id, false);
                parent.removeFromPile(this, !lastMove);
                if ((parent.getItemCount() < 3) && (lastMove))
                {
                  p = parent.getWatchers();iwatcher = p.length;
                  for (itkey = 0; itkey < iwatcher; itkey++)
                  {
                    Creature iwatcher = p[itkey];
                    iwatcher.getCommunicator().sendCloseInventoryWindow(parent.getWurmId());
                  }
                }
                else if (lastMove)
                {
                  parent.updatePile();
                }
              }
              else if ((getTopParentOrNull() != null) && (getTopParentOrNull().getTemplateId() == 177))
              {
                getTopParentOrNull().dropItem(this.id, false);
              }
              parent.removeItem(this.id, false, false, false);
            }
          }
          else
          {
            if (targetOwner != null)
            {
              targetOwner.addItemTaken(this);
              if (pickup) {
                MissionTriggers.activateTriggers(mover, this, 6, -10L, 1);
              }
            }
            try
            {
              Zone z = Zones.getZone(getTilePos(), isOnSurface());
              z.removeItem(this);
            }
            catch (NoSuchZoneException localNoSuchZoneException3) {}
          }
          if (!isLocked()) {
            setLastOwnerId(mover.getWurmId());
          }
          target.insertItem(this);
          return true;
        }
        if (insertTarget != null) {
          if (itemOwner != null) {
            itemOwner.getCommunicator().sendNormalServerMessage("The " + insertTarget
              .getName() + " contains too many items already.");
          } else if (targetOwner != null) {
            targetOwner.getCommunicator().sendNormalServerMessage("The " + insertTarget
              .getName() + " contains too many items already.");
          }
        }
        return false;
      }
      if (target.isHollow())
      {
        if (itemOwner != null) {
          itemOwner.getCommunicator().sendNormalServerMessage("The " + 
            getName() + " will not fit in the " + target.getName() + ".");
        } else if (targetOwner != null) {
          targetOwner.getCommunicator().sendNormalServerMessage("The " + 
            getName() + " will not fit in the " + target.getName() + ".");
        } else if (mover != null) {
          mover.getCommunicator().sendNormalServerMessage("The " + 
            getName() + " will not fit in the " + target.getName() + ".");
        }
        return false;
      }
      Item cont = Items.getItem(target.getParentId());
      if (!cont.isBodyPart()) {
        if ((cont.mayCreatureInsertItem()) || ((itemOwner != null) && (itemOwner.getPower() > 0)) || ((targetOwner != null) && 
          (targetOwner.getPower() > 0)))
        {
          if (cont.testInsertItem(this))
          {
            if (parent != null)
            {
              if (!hasSameOwner(target))
              {
                parent.dropItem(this.id, false);
                if (parent.getTemplateId() == 177) {
                  parent.removeFromPile(this);
                }
                if (targetOwner != null)
                {
                  targetOwner.addItemTaken(this);
                  if (pickup) {
                    MissionTriggers.activateTriggers(mover, this, 6, -10L, 1);
                  }
                }
                else if (itemOwner != null)
                {
                  itemOwner.addItemDropped(this);
                }
              }
              else
              {
                if (parent.getTemplateId() == 177) {
                  parent.removeFromPile(this);
                }
                parent.removeItem(this.id, false, false, false);
                toReturn = true;
              }
            }
            else
            {
              if (targetOwner != null)
              {
                targetOwner.addItemTaken(this);
                if (pickup) {
                  MissionTriggers.activateTriggers(mover, this, 6, -10L, 1);
                }
              }
              try
              {
                Zone z = Zones.getZone(getTilePos(), isOnSurface());
                z.removeItem(this);
              }
              catch (NoSuchZoneException localNoSuchZoneException2) {}
            }
            setLastOwnerId(mover.getWurmId());
            cont.insertItem(this);
          }
          else
          {
            if (itemOwner != null) {
              itemOwner.getCommunicator().sendNormalServerMessage("The " + 
                getName() + " will not fit in the " + cont.getName() + ".");
            } else if (targetOwner != null) {
              targetOwner.getCommunicator().sendNormalServerMessage("The " + 
                getName() + " will not fit in the " + cont.getName() + ".");
            }
            return false;
          }
        }
        else
        {
          if (itemOwner != null) {
            itemOwner.getCommunicator().sendNormalServerMessage("The " + cont
              .getName() + " contains too many items already.");
          } else if (targetOwner != null) {
            targetOwner.getCommunicator().sendNormalServerMessage("The " + cont
              .getName() + " contains too many items already.");
          }
          return false;
        }
      }
    }
    return toReturn;
  }
  
  public boolean AddBulkItemToCrate(Creature mover, Item target)
  {
    int remainingSpaces = target.getRemainingCrateSpace();
    if (remainingSpaces <= 0) {
      return false;
    }
    byte auxToCheck = 0;
    if (usesFoodState()) {
      if ((isFresh()) || (isLive())) {
        auxToCheck = (byte)(getAuxData() & 0x7F);
      } else {
        auxToCheck = getAuxData();
      }
    }
    Item toaddTo = target.getItemWithTemplateAndMaterial(getTemplateId(), getMaterial(), auxToCheck, getRealTemplateId());
    if (toaddTo != null)
    {
      if (MethodsItems.checkIfStealing(toaddTo, mover, null))
      {
        int tilex = (int)toaddTo.getPosX() >> 2;
        int tiley = (int)toaddTo.getPosY() >> 2;
        Village vil = Zones.getVillage(tilex, tiley, mover.isOnSurface());
        if ((mover.isLegal()) && (vil != null))
        {
          mover.getCommunicator().sendNormalServerMessage("That would be illegal here. You can check the settlement token for the local laws.");
          
          return false;
        }
        if ((mover.getDeity() != null) && (!mover.getDeity().isLibila())) {
          if (mover.faithful)
          {
            mover.getCommunicator().sendNormalServerMessage("Your deity would never allow stealing.");
            return false;
          }
        }
      }
      float percent = 1.0F;
      if ((!isFish()) || (getTemplateId() == 369)) {
        percent = getWeightGrams() / this.template.getWeightGrams();
      }
      boolean destroyOriginal = true;
      if (percent > remainingSpaces)
      {
        percent = Math.min(remainingSpaces, percent);
        destroyOriginal = false;
      }
      int templWeight = this.template.getWeightGrams();
      Item tempItem = null;
      if (!destroyOriginal) {
        try
        {
          int newWeight = (int)(templWeight * percent);
          tempItem = ItemFactory.createItem(this.template.templateId, getCurrentQualityLevel(), getMaterial(), (byte)0, null);
          
          tempItem.setWeight(newWeight, true);
          if (usesFoodState()) {
            tempItem.setAuxData(auxToCheck);
          }
          setWeight(getWeightGrams() - newWeight, true);
        }
        catch (NoSuchTemplateException nst)
        {
          logWarn("Adding to crate failed (missing template?).");
          logWarn(nst.getMessage(), nst);
          return false;
        }
        catch (FailedException fe)
        {
          logWarn("Adding to crate failed to create temp item.");
          logWarn(fe.getMessage(), fe);
          return false;
        }
      }
      if (tempItem == null) {
        tempItem = this;
      }
      float existingNumsBulk = toaddTo.getBulkNumsFloat(false);
      
      float percentAdded = percent / (existingNumsBulk + percent);
      float qlDiff = toaddTo.getQualityLevel() - getCurrentQualityLevel();
      float qlChange = percentAdded * qlDiff;
      if (qlDiff > 0.0F)
      {
        float newQl = toaddTo.getQualityLevel() - qlChange * 1.1F;
        toaddTo.setQualityLevel(Math.max(1.0F, newQl));
      }
      else if (qlDiff < 0.0F)
      {
        float newQl = toaddTo.getQualityLevel() - qlChange * 0.9F;
        toaddTo.setQualityLevel(Math.max(1.0F, newQl));
      }
      toaddTo.setWeight(toaddTo.getWeightGrams() + (int)(percent * this.template.getVolume()), true);
      if (destroyOriginal) {
        Items.destroyItem(getWurmId());
      } else {
        Items.destroyItem(tempItem.getWurmId());
      }
      mover.achievement(167, 1);
      target.updateModelNameOnGroundItem();
      return true;
    }
    try
    {
      toaddTo = ItemFactory.createItem(669, getCurrentQualityLevel(), getMaterial(), (byte)0, null);
      
      toaddTo.setRealTemplate(getTemplateId());
      if (usesFoodState())
      {
        toaddTo.setAuxData(auxToCheck);
        if (getRealTemplateId() != -10) {
          toaddTo.setData1(getRealTemplateId());
        }
        toaddTo.setName(getActualName());
        ItemMealData imd = ItemMealData.getItemMealData(getWurmId());
        if (imd != null) {
          ItemMealData.save(toaddTo.getWurmId(), imd.getRecipeId(), imd.getCalories(), imd.getCarbs(), imd.getFats(), imd
            .getProteins(), imd.getBonus(), imd.getStages(), imd.getIngredients());
        }
      }
      float percent = 1.0F;
      if ((!isFish()) || (getTemplateId() == 369)) {
        percent = getWeightGrams() / this.template.getWeightGrams();
      }
      boolean destroy = true;
      if (percent > remainingSpaces)
      {
        percent = Math.min(remainingSpaces, percent);
        destroy = false;
      }
      if (!toaddTo.setWeight((int)(percent * this.template.getVolume()), true)) {
        target.insertItem(toaddTo, true);
      }
      if (destroy)
      {
        Items.destroyItem(getWurmId());
      }
      else
      {
        int remove = (int)(this.template.getWeightGrams() * percent);
        setWeight(getWeightGrams() - remove, true);
      }
      mover.achievement(167, 1);
      target.updateModelNameOnGroundItem();
      toaddTo.setLastOwnerId(mover.getWurmId());
      return true;
    }
    catch (NoSuchTemplateException|FailedException e)
    {
      logWarn(e.getMessage(), e);
    }
    return false;
  }
  
  private static boolean insertOverrideNoPut(Item item, Item target)
  {
    if (((item.isShard()) || (item.isOre())) && 
      (target.isWarmachine())) {
      return true;
    }
    if ((item.getTemplateId() == 1139) && (target.getTemplateId() == 1175)) {
      return true;
    }
    return false;
  }
  
  public boolean AddBulkItem(Creature mover, Item target)
  {
    boolean full = target.isFull();
    byte auxToCheck = 0;
    if (usesFoodState()) {
      if ((isFresh()) || (isLive())) {
        auxToCheck = (byte)(getAuxData() & 0x7F);
      } else {
        auxToCheck = getAuxData();
      }
    }
    Item toaddTo = target.getItemWithTemplateAndMaterial(getTemplateId(), getMaterial(), auxToCheck, getRealTemplateId());
    if (toaddTo != null)
    {
      if (MethodsItems.checkIfStealing(toaddTo, mover, null))
      {
        int tilex = (int)toaddTo.getPosX() >> 2;
        int tiley = (int)toaddTo.getPosY() >> 2;
        Village vil = Zones.getVillage(tilex, tiley, mover.isOnSurface());
        if ((mover.isLegal()) && (vil != null))
        {
          mover.getCommunicator().sendNormalServerMessage("That would be illegal here. You can check the settlement token for the local laws.");
          
          return false;
        }
        if ((mover.getDeity() != null) && (!mover.getDeity().isLibila())) {
          if (mover.faithful)
          {
            mover.getCommunicator().sendNormalServerMessage("Your deity would never allow stealing.");
            return false;
          }
        }
      }
      float existingNumsBulk = toaddTo.getBulkNumsFloat(false);
      
      float percent = 1.0F;
      if ((!isFish()) || (getTemplateId() == 369)) {
        percent = getWeightGrams() / this.template.getWeightGrams();
      }
      float percentAdded = percent / (existingNumsBulk + percent);
      float qlDiff = toaddTo.getQualityLevel() - getCurrentQualityLevel();
      float qlChange = percentAdded * qlDiff;
      if (qlDiff > 0.0F)
      {
        float newQl = toaddTo.getQualityLevel() - qlChange * 1.1F;
        toaddTo.setQualityLevel(Math.max(1.0F, newQl));
      }
      else if (qlDiff < 0.0F)
      {
        float newQl = toaddTo.getQualityLevel() - qlChange * 0.9F;
        toaddTo.setQualityLevel(Math.max(1.0F, newQl));
      }
      toaddTo.setWeight(toaddTo.getWeightGrams() + (int)(percent * this.template.getVolume()), true);
      Items.destroyItem(getWurmId());
      mover.achievement(167, 1);
      if (full != target.isFull()) {
        target.updateModelNameOnGroundItem();
      }
      return true;
    }
    try
    {
      toaddTo = ItemFactory.createItem(669, getCurrentQualityLevel(), getMaterial(), (byte)0, null);
      
      toaddTo.setRealTemplate(getTemplateId());
      if (usesFoodState())
      {
        toaddTo.setAuxData(auxToCheck);
        if (getRealTemplateId() != -10) {
          toaddTo.setData1(getRealTemplateId());
        }
        toaddTo.setName(getActualName());
        ItemMealData imd = ItemMealData.getItemMealData(getWurmId());
        if (imd != null) {
          ItemMealData.save(toaddTo.getWurmId(), imd.getRecipeId(), imd.getCalories(), imd.getCarbs(), imd.getFats(), imd
            .getProteins(), imd.getBonus(), imd.getStages(), imd.getIngredients());
        }
      }
      float percent = 1.0F;
      if ((!isFish()) || (getTemplateId() == 369)) {
        percent = getWeightGrams() / this.template.getWeightGrams();
      }
      if (!toaddTo.setWeight((int)(percent * this.template.getVolume()), true)) {
        target.insertItem(toaddTo, true);
      }
      Items.destroyItem(getWurmId());
      mover.achievement(167, 1);
      if (full != target.isFull()) {
        target.updateModelNameOnGroundItem();
      }
      toaddTo.setLastOwnerId(mover.getWurmId());
      return true;
    }
    catch (NoSuchTemplateException nst)
    {
      logWarn(nst.getMessage(), nst);
    }
    catch (FailedException fe)
    {
      logWarn(fe.getMessage(), fe);
    }
    return false;
  }
  
  public void removeFromPile(Item item)
  {
    try
    {
      Zone zone = Zones.getZone((int)getPosX() >> 2, (int)getPosY() >> 2, isOnSurface());
      zone.removeItem(item);
    }
    catch (NoSuchZoneException nsz)
    {
      logWarn("Removed from nonexistant zone " + this.id);
    }
  }
  
  public void removeFromPile(Item item, boolean moving)
  {
    try
    {
      Zone zone = Zones.getZone((int)getPosX() >> 2, (int)getPosY() >> 2, isOnSurface());
      zone.removeItem(item, moving, false);
    }
    catch (NoSuchZoneException nsz)
    {
      logWarn("Removed from nonexistant zone " + this.id);
    }
  }
  
  public void updatePile()
  {
    try
    {
      Zone zone = Zones.getZone((int)getPosX() >> 2, (int)getPosY() >> 2, isOnSurface());
      zone.updatePile(this);
    }
    catch (NoSuchZoneException nsz)
    {
      logWarn("Removed from nonexistant zone " + this.id);
    }
  }
  
  public final void putInVoid()
  {
    if (this.parentId != -10L) {
      try
      {
        Item parent = Items.getItem(this.parentId);
        parent.dropItem(this.id, false);
      }
      catch (NoSuchItemException nsi)
      {
        logWarn(this.id + " had a parent that could not be found.", nsi);
      }
    }
    if (this.zoneId != -10) {
      try
      {
        Zone zone = Zones.getZone((int)getPosX() >> 2, (int)getPosY() >> 2, isOnSurface());
        zone.removeItem(this);
      }
      catch (NoSuchZoneException nsz)
      {
        logWarn(
          "No such zone: " + ((int)getPosX() >> 2) + "," + ((int)getPosY() >> 2) + "," + isOnSurface() + "?", nsz);
      }
    }
  }
  
  private void insertIntoPile(Item item)
  {
    try
    {
      item.setPosXYZ(getPosX(), getPosY(), getPosZ());
      Zone zone = Zones.getZone((int)getPosX() >> 2, (int)getPosY() >> 2, isOnSurface());
      
      zone.addItem(item);
    }
    catch (NoSuchZoneException nsz)
    {
      logWarn("added to nonexistant zone " + this.id);
    }
  }
  
  public final long getOwner()
    throws NotOwnedException
  {
    long lOwnerId = getOwnerId();
    if (lOwnerId == -10L) {
      throw new NotOwnedException("Not owned item");
    }
    return lOwnerId;
  }
  
  private Creature getOwnerOrNull()
  {
    try
    {
      return Server.getInstance().getCreature(getOwnerId());
    }
    catch (NoSuchCreatureException nsc)
    {
      logWarn(nsc.getMessage(), nsc);
    }
    catch (NoSuchPlayerException nsp)
    {
      PlayerInfo info = PlayerInfoFactory.getPlayerInfoWithWurmId(getOwnerId());
      if (info == null) {
        logWarn(nsp.getMessage(), nsp);
      }
    }
    return null;
  }
  
  public int getSurfaceArea()
  {
    float modifier = 1.0F;
    if (getSpellEffects() != null) {
      modifier = getSpellEffects().getRuneEffect(RuneUtilities.ModifierEffect.ENCH_SIZE);
    }
    if (isLiquid()) {
      return (int)(getWeightGrams() * modifier);
    }
    if (!this.template.usesSpecifiedContainerSizes()) {
      return 
      
        (int)(getSizeX() * getSizeY() * 2 + getSizeY() * getSizeZ() * 2 + getSizeX() * getSizeZ() * 2 * modifier);
    }
    return 
    
      (int)(this.template.getSizeX() * this.template.getSizeY() * 2 + this.template.getSizeY() * this.template.getSizeZ() * 2 + this.template.getSizeX() * this.template.getSizeZ() * 2 * modifier);
  }
  
  public int getVolume()
  {
    float modifier = 1.0F;
    if (getSpellEffects() != null) {
      modifier = getSpellEffects().getRuneEffect(RuneUtilities.ModifierEffect.ENCH_VOLUME);
    }
    if (isLiquid()) {
      return (int)(getWeightGrams() * modifier);
    }
    if (this.internalVolume != 0) {
      return (int)(this.internalVolume * modifier);
    }
    if (!this.template.usesSpecifiedContainerSizes()) {
      return (int)(getSizeX() * getSizeY() * getSizeZ() * modifier);
    }
    return (int)(this.template.getSizeX() * this.template.getSizeY() * this.template.getSizeZ() * modifier);
  }
  
  public int setInternalVolumeFromAuxByte()
  {
    int newVolume = 10;
    switch (getAuxData())
    {
    case 12: 
      newVolume = 1;
      break;
    case 11: 
      newVolume = 2;
      break;
    case 10: 
      newVolume = 5;
      break;
    case 9: 
      newVolume = 10;
      break;
    case 8: 
      newVolume = 20;
      break;
    case 7: 
      newVolume = 50;
      break;
    case 6: 
      newVolume = 100;
      break;
    case 5: 
      newVolume = 200;
      break;
    case 4: 
      newVolume = 500;
      break;
    case 3: 
      newVolume = 1000;
      break;
    case 2: 
      newVolume = 2000;
      break;
    case 1: 
      newVolume = 5000;
      break;
    default: 
      newVolume = 10000;
    }
    this.internalVolume = newVolume;
    return this.internalVolume;
  }
  
  public int getContainerVolume()
  {
    float modifier = 1.0F;
    if (getSpellEffects() != null) {
      modifier = getSpellEffects().getRuneEffect(RuneUtilities.ModifierEffect.ENCH_VOLUME);
    }
    if (this.internalVolume != 0) {
      return (int)(this.internalVolume * modifier);
    }
    if (this.template.usesSpecifiedContainerSizes()) {
      return (int)(this.template.getContainerVolume() * modifier);
    }
    return getVolume();
  }
  
  public final float getSizeMod()
  {
    float minMod = getTemplateId() == 344 ? 0.3F : 0.8F;
    if ((getTemplateId() == 272) || (isFish()) || (getTemplateId() == 344) || ((isCombine()) && (getWeightGrams() > 5000))) {
      return Math.max(minMod, Math.min(5.0F, (float)cubeRoot(getVolume() / this.template.getVolume())));
    }
    TreeData.TreeType ttype = Materials.getTreeTypeForWood(getMaterial());
    if ((ttype != null) && (!ttype.isFruitTree())) {
      if ((getTemplateId() == 731) || (getTemplateId() == 385))
      {
        float ageScale = ((getAuxData() >= 100 ? getAuxData() - 100 : getAuxData()) + 1) / 16.0F;
        return ageScale * 16.0F;
      }
    }
    float modifier = 1.0F;
    if (getSpellEffects() != null) {
      modifier = getSpellEffects().getRuneEffect(RuneUtilities.ModifierEffect.ENCH_SIZE);
    }
    return 1.0F * modifier;
  }
  
  public static double cubeRoot(double x)
  {
    return Math.pow(x, 0.3333333333333333D);
  }
  
  private int getUsedVolume()
  {
    int used = 0;
    for (Iterator<Item> it = getItems().iterator(); it.hasNext();)
    {
      Item i = (Item)it.next();
      if (!i.isInventoryGroup())
      {
        if ((i.isLiquid()) || (i.isBulkItem())) {
          used += i.getWeightGrams();
        } else {
          used += i.getVolume();
        }
      }
      else {
        used += i.getUsedVolume();
      }
    }
    return used;
  }
  
  public boolean hasSpaceFor(int volume)
  {
    if (isInventory()) {
      return true;
    }
    return getContainerVolume() - getUsedVolume() > volume;
  }
  
  public boolean canAddToCrate(Item toAdd)
  {
    if (!isCrate()) {
      return false;
    }
    if (!toAdd.isBulk()) {
      return false;
    }
    return getRemainingCrateSpace() > 0;
  }
  
  public final int getRemainingCrateSpace()
  {
    int count = 0;
    Item[] cargo = getItemsAsArray();
    for (Item cargoItem : cargo) {
      count += cargoItem.getBulkNums();
    }
    if (this.template.templateId == 852) {
      return 300 - count;
    }
    if (this.template.templateId == 851) {
      return 150 - count;
    }
    return 0;
  }
  
  public final ItemTemplate getTemplate()
  {
    return this.template;
  }
  
  private boolean containsItem()
  {
    for (Iterator<Item> it = getItems().iterator(); it.hasNext();)
    {
      Item next = (Item)it.next();
      if (!next.isBodyPart()) {
        return true;
      }
    }
    return false;
  }
  
  private boolean containsItemAndIsSameTypeOfItem(Item item)
  {
    for (Iterator<Item> it = getItems().iterator(); it.hasNext();)
    {
      Item next = (Item)it.next();
      if (!next.isBodyPart()) {
        if ((item.getTemplateId() == next.getTemplateId()) || ((item.isBarding()) && (next.isBarding()))) {
          return true;
        }
      }
    }
    return false;
  }
  
  private boolean containsArtifact()
  {
    for (Item item : getAllItems(true, true)) {
      if (item.isArtifact()) {
        return true;
      }
    }
    return false;
  }
  
  private boolean containsArmor()
  {
    for (Iterator<Item> it = getItems().iterator(); it.hasNext();)
    {
      Item next = (Item)it.next();
      if ((!next.isBodyPart()) && (next.isArmour())) {
        return true;
      }
    }
    return false;
  }
  
  private boolean canHoldItem(Item item)
  {
    try
    {
      Creature owner = Server.getInstance().getCreature(this.ownerId);
      if ((this.auxbyte != 1) && (this.auxbyte != 0) && 
        (owner.isPlayer())) {
        return false;
      }
      Item rightWeapon = owner.getRighthandWeapon();
      Item leftWeapon = owner.getLefthandWeapon();
      Item shield = owner.getShield();
      if ((rightWeapon == null) && (leftWeapon == null) && (shield == null)) {
        return true;
      }
      if ((rightWeapon != null) && (item.isTwoHanded())) {
        return false;
      }
      byte rHeld = owner.isPlayer() ? 38 : 14;
      if ((rightWeapon != null) && (this.place == rHeld)) {
        return false;
      }
      if ((rightWeapon != null) && (rightWeapon.isTwoHanded())) {
        return false;
      }
      byte lHeld = owner.isPlayer() ? 37 : 13;
      if ((leftWeapon != null) && (this.place == lHeld)) {
        return false;
      }
      if ((leftWeapon != null) && (item.isTwoHanded())) {
        return false;
      }
      if ((leftWeapon != null) && (leftWeapon.isTwoHanded())) {
        return false;
      }
      if ((shield != null) && (item.isTwoHanded())) {
        return false;
      }
      if ((shield != null) && (this.place == lHeld)) {
        return false;
      }
      return true;
    }
    catch (Exception ex) {}
    return false;
  }
  
  private boolean testInsertShield(Item item, Creature owner)
  {
    if (owner.isPlayer())
    {
      if (getPlace() != 44) {
        return false;
      }
      if (containsItem()) {
        return false;
      }
    }
    else if (containsItemAndIsSameTypeOfItem(item))
    {
      return false;
    }
    Item leftWeapon = owner.getLefthandWeapon();
    if (leftWeapon != null) {
      return false;
    }
    Item rightWeapon = owner.getRighthandWeapon();
    if ((rightWeapon != null) && ((rightWeapon.isWeaponBow()) || (rightWeapon.isTwoHanded()))) {
      return false;
    }
    return true;
  }
  
  private boolean testInsertItemIntoSlot(Item item, Creature owner)
  {
    byte[] slots = item.getBodySpaces();
    for (int i = 0; i < slots.length; i++) {
      if (this.place == slots[i]) {
        return !containsItem();
      }
    }
    return false;
  }
  
  private boolean testInsertItemIntoAnimalSlot(Item item, Creature owner)
  {
    byte[] slots = item.getBodySpaces();
    for (int i = 0; i < slots.length; i++) {
      if (this.place == slots[i]) {
        return !containsItemAndIsSameTypeOfItem(item);
      }
    }
    return false;
  }
  
  private boolean testInsertWeapon(Item item, Creature owner)
  {
    if ((!canHoldItem(item)) || ((containsItem()) && (!containsArmor()))) {
      return false;
    }
    if (!owner.hasHands())
    {
      boolean found = false;
      byte[] armourplaces = item.getBodySpaces();
      for (int x = 0; x < armourplaces.length; x++) {
        if (armourplaces[x] == this.place)
        {
          found = true;
          break;
        }
      }
      if (!found) {
        return false;
      }
    }
    return true;
  }
  
  private boolean testInsertItemPlayer(Item item, Creature owner)
  {
    if ((isBodyPart()) && (isEquipmentSlot()))
    {
      if ((item.isArmour()) && (!item.isShield())) {
        return false;
      }
      if (item.isShield()) {
        return testInsertShield(item, owner);
      }
      if (item.getDamagePercent() > 0) {
        return testInsertWeapon(item, owner);
      }
      if ((this.place == 37) || (this.place == 38))
      {
        if ((containsItem()) || (!canHoldItem(item)) || (item.isInventoryGroup())) {
          return false;
        }
        return true;
      }
      return testInsertItemIntoSlot(item, owner);
    }
    if ((isBodyPart()) && (!isEquipmentSlot()))
    {
      if (item.isBarding()) {
        return false;
      }
      if (((!item.isArmour()) && (!item.isBracelet())) || (item.isShield())) {
        return false;
      }
      return testInsertItemIntoSlot(item, owner);
    }
    if (isHollow()) {
      return testInsertHollowItem(item, false);
    }
    Item insertTarget = getInsertItem();
    if (insertTarget == null) {
      return false;
    }
    return insertTarget.testInsertItem(item);
  }
  
  private boolean bodyPartIsWeaponSlotNPC()
  {
    return (this.place == 13) || (this.place == 14);
  }
  
  private boolean testInsertHollowItem(Item item, boolean testItemCount)
  {
    if (isNoDrop()) {
      if (item.isArtifact()) {
        return false;
      }
    }
    if (Features.Feature.FREE_ITEMS.isEnabled()) {
      if (item.isChallengeNewbieItem()) {
        if ((this.ownerId == -10L) && (
          (item.isArmour()) || (item.isWeapon()) || (item.isShield()))) {
          return false;
        }
      }
    }
    int freevol = getFreeVolume();
    if (itemCanBeInserted(item)) {
      if ((getTemplateId() == 177) || (getTemplateId() == 0) || 
        (freevol >= item.getVolume()) || 
        (doesContainerRestrictionsAllowItem(item)))
      {
        if ((getTemplateId() == 621) && (item.isSaddleBags()))
        {
          Item parent = getParentOrNull();
          if ((parent != null) && (parent.isSaddleBags())) {
            return false;
          }
        }
        if (testItemCount) {
          return mayCreatureInsertItem();
        }
        return true;
      }
    }
    return false;
  }
  
  private boolean testInsertHumanoidNPC(Item item, Creature owner)
  {
    if (isBodyPart())
    {
      if ((item.isShield()) && (this.place == 3)) {
        return testInsertShield(item, owner);
      }
      if ((item.getDamagePercent() > 0) && (bodyPartIsWeaponSlotNPC())) {
        return testInsertWeapon(item, owner);
      }
      return testInsertItemIntoAnimalSlot(item, owner);
    }
    if (isHollow()) {
      return testInsertHollowItem(item, false);
    }
    if (!isHollow())
    {
      Item insertTarget = getInsertItem();
      if (insertTarget == null) {
        return false;
      }
      return insertTarget.testInsertItem(item);
    }
    return false;
  }
  
  private boolean testInsertAnimal(Item item, Creature owner)
  {
    if (isBodyPart()) {
      return testInsertItemIntoAnimalSlot(item, owner);
    }
    if (isHollow()) {
      return testInsertHollowItem(item, false);
    }
    if (!isHollow())
    {
      Item insertTarget = getInsertItem();
      if (insertTarget == null) {
        return false;
      }
      return insertTarget.testInsertItem(item);
    }
    return false;
  }
  
  public final Item getInsertItem()
  {
    if ((isBodyPart()) || (isEquipmentSlot()) || (isHollow())) {
      return this;
    }
    try
    {
      return getParent().getInsertItem();
    }
    catch (NoSuchItemException nsi) {}
    return null;
  }
  
  public final boolean testInsertItem(Item item)
  {
    if (item == this) {
      return false;
    }
    Creature owner = null;
    try
    {
      owner = Server.getInstance().getCreature(this.ownerId);
      if (owner.isPlayer()) {
        return testInsertItemPlayer(item, owner);
      }
      if (owner.isAnimal()) {
        return testInsertAnimal(item, owner);
      }
      return testInsertHumanoidNPC(item, owner);
    }
    catch (NoSuchPlayerException nsp)
    {
      if (Features.Feature.FREE_ITEMS.isEnabled()) {
        if (item.isChallengeNewbieItem()) {
          if ((item.isArmour()) || (item.isWeapon()) || (item.isShield())) {
            return false;
          }
        }
      }
      if (isHollow()) {
        return testInsertHollowItem(item, true);
      }
      Item insertTarget = getInsertItem();
      if (insertTarget == null) {
        return false;
      }
      return insertTarget.testInsertItem(item);
    }
    catch (NoSuchCreatureException nsc)
    {
      String msg = "Unable to find owner for body part (creature). Part: " + this.name + " ownerID: " + this.ownerId;
      logWarn(msg, nsc);
    }
    return false;
  }
  
  public int getFreeVolume()
  {
    return getContainerVolume() - getUsedVolume();
  }
  
  public final Item getFirstContainedItem()
  {
    Item[] contained = getItemsAsArray();
    if ((contained == null) || (contained.length == 0)) {
      return null;
    }
    return contained[0];
  }
  
  public final boolean insertItem(Item item)
  {
    return insertItem(item, false, false);
  }
  
  public final boolean insertItem(Item item, boolean unconditionally)
  {
    return insertItem(item, unconditionally, false);
  }
  
  public final boolean insertItem(Item item, boolean unconditionally, boolean checkItemCount)
  {
    return insertItem(item, unconditionally, checkItemCount, false);
  }
  
  public final boolean insertItem(Item item, boolean unconditionally, boolean checkItemCount, boolean isPlaced)
  {
    boolean toReturn = false;
    if (item == this)
    {
      logWarn("Tried to insert same item into an item: ", new Exception());
      return false;
    }
    if (isBodyPart())
    {
      Item armour = null;
      Item held = null;
      short lPlace;
      Iterator<Item> it;
      if (!item.isBodyPartAttached())
      {
        lPlace = getPlace();
        if (item.getDamagePercent() > 0) {
          if ((lPlace == 38) || (lPlace == 37)) {
            if (!unconditionally) {
              try
              {
                Creature owner = Server.getInstance().getCreature(this.ownerId);
                
                Item rightWeapon = owner.getRighthandWeapon();
                if ((rightWeapon != null) && (item.isTwoHanded())) {
                  return false;
                }
                if ((rightWeapon != null) && (lPlace == 38)) {
                  return false;
                }
                if ((rightWeapon != null) && (rightWeapon.isTwoHanded())) {
                  return false;
                }
                Item leftWeapon = owner.getLefthandWeapon();
                if ((leftWeapon != null) && (lPlace == 37)) {
                  return false;
                }
                if ((leftWeapon != null) && (item.isTwoHanded())) {
                  return false;
                }
                if ((leftWeapon != null) && (leftWeapon.isTwoHanded())) {
                  return false;
                }
                Item shield = owner.getShield();
                if ((shield != null) && (item.isTwoHanded())) {
                  return false;
                }
                if ((shield != null) && (lPlace == 37)) {
                  return false;
                }
                if (!owner.hasHands())
                {
                  boolean found = false;
                  byte[] armourplaces = item.getBodySpaces();
                  for (int x = 0; x < armourplaces.length; x++) {
                    if (armourplaces[x] == lPlace)
                    {
                      found = true;
                      break;
                    }
                  }
                  if (!found) {
                    return false;
                  }
                }
              }
              catch (NoSuchPlayerException localNoSuchPlayerException1) {}catch (NoSuchCreatureException localNoSuchCreatureException1) {}
            }
          }
        }
        if (item.isShield())
        {
          if (lPlace == 44)
          {
            if (!unconditionally) {
              try
              {
                Creature owner = Server.getInstance().getCreature(this.ownerId);
                Item rightWeapon = owner.getRighthandWeapon();
                if ((rightWeapon != null) && (rightWeapon.isTwoHanded())) {
                  return false;
                }
                Item leftWeapon = owner.getLefthandWeapon();
                if (leftWeapon != null) {
                  return false;
                }
              }
              catch (NoSuchPlayerException localNoSuchPlayerException2) {}catch (NoSuchCreatureException localNoSuchCreatureException2) {}
            }
          }
          else if ((lPlace == 13) || (lPlace == 14)) {
            try
            {
              Creature owner = Server.getInstance().getCreature(this.ownerId);
              owner.getCommunicator().sendNormalServerMessage("You need to wear the " + item
                .getName() + " on the left arm.");
              return false;
            }
            catch (NoSuchPlayerException localNoSuchPlayerException3) {}catch (NoSuchCreatureException localNoSuchCreatureException3) {}
          }
        }
        else if (item.isBelt()) {
          if (lPlace == 43) {
            try
            {
              Creature owner = Server.getInstance().getCreature(this.ownerId);
              if (owner.getWornBelt() != null) {
                return false;
              }
            }
            catch (NoSuchPlayerException localNoSuchPlayerException4) {}catch (NoSuchCreatureException localNoSuchCreatureException4) {}
          }
        }
        if ((this.place != 2) || (item.getTemplateId() != 740)) {
          for (it = getItems().iterator(); it.hasNext();)
          {
            Item tocheck = (Item)it.next();
            if (!tocheck.isBodyPart()) {
              if (tocheck.isArmour())
              {
                if (armour == null)
                {
                  byte[] armourplaces = tocheck.getBodySpaces();
                  for (int x = 0; x < armourplaces.length; x++) {
                    if (armourplaces[x] == lPlace) {
                      armour = tocheck;
                    }
                  }
                  if (armour == null) {
                    held = tocheck;
                  }
                }
                else if (held == null)
                {
                  held = tocheck;
                }
                else if (!unconditionally)
                {
                  return false;
                }
              }
              else if (held == null) {
                held = tocheck;
              } else if (!unconditionally) {
                return false;
              }
            }
          }
        }
      }
      if (item.isArmour())
      {
        if ((this.place == 13) || (this.place == 14))
        {
          boolean worn = false;
          if (armour == null)
          {
            byte[] armourplaces = item.getBodySpaces();
            for (int x = 0; x < armourplaces.length; x++) {
              if (armourplaces[x] == this.place)
              {
                worn = true;
                sendWear(item, (byte)this.place);
              }
            }
          }
          if (!worn)
          {
            if (held != null) {
              return false;
            }
            sendHold(item);
          }
        }
        else if (armour == null)
        {
          boolean worn = false;
          byte[] armourplaces = item.getBodySpaces();
          for (int x = 0; x < armourplaces.length; x++) {
            if (armourplaces[x] == this.place)
            {
              if ((item.isHollow()) && (!item.isEmpty(false)))
              {
                try
                {
                  Creature owner = Server.getInstance().getCreature(this.ownerId);
                  owner.getCommunicator().sendNormalServerMessage("There is not enough room in the " + item
                    .getName() + " for your " + owner
                    .getBody().getWoundLocationString(this.place) + " and all the other items in it!");
                }
                catch (NoSuchPlayerException localNoSuchPlayerException5) {}catch (NoSuchCreatureException localNoSuchCreatureException5) {}
                return false;
              }
              worn = true;
              sendWear(item, (byte)this.place);
              if (item.isRoyal()) {
                if ((this.place == 1) || (this.place == 28)) {
                  if ((item.getTemplateId() == 530) || 
                    (item.getTemplateId() == 533) || 
                    (item.getTemplateId() == 536)) {
                    if (item.getKingdom() != 0)
                    {
                      Kingdom kingdom = Kingdoms.getKingdom(item.getKingdom());
                      if ((kingdom != null) && (kingdom.existsHere()) && (kingdom.isCustomKingdom())) {
                        try
                        {
                          Creature owner = Server.getInstance().getCreature(getOwnerId());
                          if (owner.getKingdomId() == item.getKingdom()) {
                            if (!owner.isChampion())
                            {
                              King k = King.getKing(item.getKingdom());
                              if ((k == null) || (k.kingid != getOwnerId()))
                              {
                                King.createKing(item.getKingdom(), owner.getName(), owner
                                  .getWurmId(), owner.getSex());
                                
                                NewKingQuestion nk = new NewKingQuestion(owner, "New ruler!", "Congratulations!", owner.getWurmId());
                                
                                nk.sendQuestion();
                              }
                            }
                          }
                        }
                        catch (NoSuchCreatureException nsc)
                        {
                          logWarn(item.getName() + ": " + nsc.getMessage(), nsc);
                        }
                        catch (NoSuchPlayerException nsp)
                        {
                          logWarn(item.getName() + ": " + nsp.getMessage(), nsp);
                        }
                      }
                    }
                  }
                }
              }
            }
          }
          if (!worn) {
            if (!unconditionally) {
              return false;
            }
          }
        }
        else if (!unconditionally)
        {
          return false;
        }
      }
      else if (item.isShield())
      {
        if ((held != null) && (!unconditionally)) {
          return false;
        }
        if ((this.place == 13) || (this.place == 14)) {
          sendHold(item);
        } else if (this.place == 44) {
          sendWearShield(item);
        }
      }
      else if (item.isBelt())
      {
        if ((held != null) && (!unconditionally)) {
          return false;
        }
        if ((this.place == 13) || (this.place == 14)) {
          sendHold(item);
        } else if (this.place == 43) {
          sendWear(item, (byte)this.place);
        }
      }
      else
      {
        if ((held != null) && (!unconditionally)) {
          return false;
        }
        if ((this.place == 37) || (this.place == 38)) {
          sendHold(item);
        } else {
          sendWear(item, (byte)this.place);
        }
      }
      addItem(item, false);
      
      setThisAsParentFor(item, false);
      toReturn = true;
    }
    else if (isHollow())
    {
      if (isNoDrop()) {
        if (item.isArtifact()) {
          return false;
        }
      }
      int freevol = getFreeVolume();
      if ((unconditionally) || (itemCanBeInserted(item)))
      {
        boolean canInsert = true;
        if ((checkItemCount) && (!unconditionally)) {
          canInsert = mayCreatureInsertItem();
        }
        if ((unconditionally) || 
          (getTemplateId() == 177) || (
          ((getTemplateId() == 0) || (freevol >= item.getVolume()) || 
          (doesContainerRestrictionsAllowItem(item))) && (canInsert)))
        {
          if ((getTemplateId() == 621) && (item.isSaddleBags()))
          {
            Item parent = getParentOrNull();
            if ((parent != null) && (parent.isSaddleBags())) {
              return false;
            }
          }
          if (getTemplateId() == 1404) {
            if ((item.getTemplateId() == 1272) || (item.getTemplateId() == 748))
            {
              item.setTemplateId(1403);
              item.setName("blank report");
              item.sendUpdate();
            }
          }
          item.setPlacedOnParent(isPlaced);
          addItem(item, false);
          setThisAsParentFor(item, true);
          updatePileMaterial();
          
          toReturn = true;
        }
        else if (freevol <= item.getWeightGrams() * 10)
        {
          logInfo(
            getName() + " freevol(" + freevol + ")<=" + item.getName() + ".getWeightGrams()*10 (" + item.getWeightGrams() + ")", new Exception());
        }
      }
    }
    else
    {
      Item insertTarget = getInsertItem();
      if (insertTarget == null) {
        return false;
      }
      return insertTarget.insertItem(item, unconditionally);
    }
    return toReturn;
  }
  
  public boolean doesContainerRestrictionsAllowItem(Item item)
  {
    if ((getTemplateId() == 1404) && (
      (item.getTemplateId() == 748) || (item.getTemplateId() == 1272) || (item.getTemplateId() == 1403))) {
      return true;
    }
    if (getTemplate().getContainerRestrictions() == null) {
      return false;
    }
    for (ContainerRestriction cRest : getTemplate().getContainerRestrictions()) {
      if (cRest.canInsertItem(getItemsAsArray(), item)) {
        return true;
      }
    }
    return false;
  }
  
  public boolean itemCanBeInserted(Item item)
  {
    if (getTemplate().getContainerRestrictions() != null) {
      return doesContainerRestrictionsAllowItem(item);
    }
    if ((getTemplateId() == 1409) && 
      (item.getTemplateId() != 748) && (item.getTemplateId() != 1272)) {
      return false;
    }
    if (getTemplateId() == 1404)
    {
      if ((item.getTemplateId() != 748) && (item.getTemplateId() != 1272) && (item.getTemplateId() != 1403)) {
        return false;
      }
      if ((item.getTemplateId() != 1403) && ((item.getAuxData() > 0) || (item.getInscription() != null))) {
        return false;
      }
      if (getItemCount() >= Math.max(22.0D, Math.floor(getCurrentQualityLevel()))) {
        return false;
      }
      return true;
    }
    if ((item.isSaddleBags()) || (item.getTemplateId() == 621)) {
      if (!isSaddleBags())
      {
        if (!isInside(new int[] { 1333, 1334 })) {}
      }
      else {
        return false;
      }
    }
    if ((getContainerSizeX() >= item.getSizeX()) && (getContainerSizeY() >= item.getSizeY()) && (getContainerSizeZ() > item.getSizeZ())) {
      return true;
    }
    if (getTemplateId() == 177) {
      return true;
    }
    if (getTemplateId() == 0) {
      return true;
    }
    if (item.isHollow()) {
      return false;
    }
    if (((item.isCombine()) || (item.isFood()) || (item.isLiquid())) && (getFreeVolume() >= item.getVolume())) {
      return true;
    }
    return false;
  }
  
  private boolean isMultipleMaterialPileTemplate(int templateId)
  {
    if (templateId == 9) {
      return true;
    }
    return false;
  }
  
  private void updatePileMaterial()
  {
    if (getTemplateId() == 177)
    {
      boolean multipleMaterials = false;
      byte currentMaterial = 0;
      Item[] itemsArray = getItemsAsArray();
      byte firstMaterial = 0;
      int firstItem = 0;
      int currentItem = 0;
      for (int i = 0; i < itemsArray.length; i++)
      {
        currentMaterial = itemsArray[i].getMaterial();
        
        currentItem = itemsArray[i].getTemplateId();
        if (i == 0)
        {
          firstMaterial = currentMaterial;
          firstItem = currentItem;
        }
        if (currentItem != firstItem)
        {
          multipleMaterials = true;
          break;
        }
        if ((currentMaterial != firstMaterial) && (!isMultipleMaterialPileTemplate(currentItem)))
        {
          multipleMaterials = true;
          break;
        }
      }
      if (multipleMaterials)
      {
        boolean changed = false;
        if (getData1() != -1)
        {
          setData1(-1);
          changed = true;
        }
        if (getMaterial() != 0)
        {
          setMaterial((byte)0);
          changed = true;
        }
        if (changed) {
          updateModelNameOnGroundItem();
        }
      }
    }
  }
  
  private void sendHold(Item item)
  {
    String holdst = "hold";
    Creature owner = getOwnerOrNull();
    if (owner == null) {
      return;
    }
    String hand = "right hand";
    if (this.place == 37) {
      hand = "left hand";
    }
    if (item.isTwoHanded()) {
      hand = "two hands";
    }
    if (!owner.getCommunicator().stillLoggingIn())
    {
      owner.getCommunicator().sendNormalServerMessage("You hold " + item
        .getNameWithGenus().toLowerCase() + " in your " + hand + ".");
      PlayerTutorial.firePlayerTrigger(owner.getWurmId(), PlayerTutorial.PlayerTrigger.EQUIPPED_ITEM);
    }
    boolean send = true;
    if (item.isArmour())
    {
      byte[] armourplaces = item.getBodySpaces();
      for (int x = 0; x < armourplaces.length; x++) {
        if (armourplaces[x] == this.place) {
          send = false;
        }
      }
    }
    if ((item.isWeapon()) && (item.getSpellEffects() != null)) {
      owner.achievement(581);
    }
    if (send)
    {
      owner.getCurrentTile().sendWieldItem(getOwnerId(), (byte)(this.place == 37 ? 0 : 1), item
      
        .getModelName(), item.getRarity(), WurmColor.getColorRed(item.getColor()), 
        WurmColor.getColorGreen(item.getColor()), WurmColor.getColorBlue(item.getColor()), 
        WurmColor.getColorRed(item.getColor2()), 
        WurmColor.getColorGreen(item.getColor2()), 
        WurmColor.getColorBlue(item.getColor2()));
      
      byte equipementSlot = BodyTemplate.convertToItemEquipementSlot((byte)this.place);
      owner.getCurrentTile().sendWearItem(getOwnerId(), item.getTemplateId(), equipementSlot, 
        WurmColor.getColorRed(item.getColor()), WurmColor.getColorGreen(item.getColor()), WurmColor.getColorBlue(item.getColor()), 
        WurmColor.getColorRed(item.getColor2()), 
        WurmColor.getColorGreen(item.getColor2()), 
        WurmColor.getColorBlue(item.getColor2()), item
        .getMaterial(), item.getRarity());
      owner.getCombatHandler().setCurrentStance(-1, (byte)0);
    }
  }
  
  private void sendWear(Item item, byte bodyPart)
  {
    if (!item.isBodyPartAttached())
    {
      Creature owner = getOwnerOrNull();
      if (owner == null) {
        return;
      }
      if (!owner.getCommunicator().stillLoggingIn())
      {
        owner.getCommunicator().sendNormalServerMessage("You wear " + item.getNameWithGenus().toLowerCase() + ".");
        PlayerTutorial.firePlayerTrigger(owner.getWurmId(), PlayerTutorial.PlayerTrigger.EQUIPPED_ITEM);
      }
      byte equipmentSlot = item.isArmour() ? BodyTemplate.convertToArmorEquipementSlot(bodyPart) : BodyTemplate.convertToItemEquipementSlot(bodyPart);
      if ((owner.isAnimal()) && (owner.isVehicle())) {
        owner.getCurrentTile().sendHorseWear(getOwnerId(), item.getTemplateId(), item.getMaterial(), equipmentSlot, item.getAuxData());
      } else {
        owner.getCurrentTile().sendWearItem(getOwnerId(), item.getTemplateId(), equipmentSlot, 
          WurmColor.getColorRed(item.getColor()), WurmColor.getColorGreen(item.getColor()), WurmColor.getColorBlue(item.getColor()), 
          WurmColor.getColorRed(item.getColor2()), 
          WurmColor.getColorGreen(item.getColor2()), 
          WurmColor.getColorBlue(item.getColor2()), item
          .getMaterial(), item.getRarity());
      }
      if (item.isArmour()) {
        item.setWornAsArmour(true, getOwnerId());
      }
      if (item.getTemplateId() == 330) {
        owner.setHasCrownEffect(true);
      }
      if ((item.hasItemBonus()) && (!Servers.localServer.PVPSERVER)) {
        ItemBonus.calcAndAddBonus(item, owner);
      }
      if (item.isPriceEffectedByMaterial()) {
        if (item.getTemplateId() == 297) {
          owner.achievement(94);
        } else if (item.getTemplateId() == 230) {
          owner.achievement(95);
        } else if (item.getTemplateId() == 231) {
          owner.achievement(96);
        }
      }
      if ((item.isWeapon()) && (item.getSpellEffects() != null)) {
        owner.achievement(581);
      }
    }
  }
  
  private void sendWearShield(Item item)
  {
    if (!item.isBodyPartAttached())
    {
      Creature owner = getOwnerOrNull();
      if (owner == null) {
        return;
      }
      if (!owner.getCommunicator().stillLoggingIn())
      {
        owner.getCommunicator().sendNormalServerMessage("You wear " + item
          .getNameWithGenus().toLowerCase() + " as shield.");
        PlayerTutorial.firePlayerTrigger(owner.getWurmId(), PlayerTutorial.PlayerTrigger.EQUIPPED_ITEM);
      }
      byte equipementSlot = BodyTemplate.convertToItemEquipementSlot((byte)this.place);
      owner.getCurrentTile().sendWearItem(getOwnerId(), item.getTemplateId(), equipementSlot, 
        WurmColor.getColorRed(item.getColor()), WurmColor.getColorGreen(item.getColor()), 
        WurmColor.getColorBlue(item.getColor()), 
        WurmColor.getColorRed(item.getColor2()), 
        WurmColor.getColorGreen(item.getColor2()), 
        WurmColor.getColorBlue(item.getColor2()), item
        .getMaterial(), item.getRarity());
      
      owner.getCommunicator().sendToggleShield(true);
    }
  }
  
  public final boolean isEmpty(boolean checkInitialContainers)
  {
    if ((checkInitialContainers) && (getTemplate().getInitialContainers() != null))
    {
      for (Item item : getItemsAsArray()) {
        if (!item.isEmpty(false)) {
          return false;
        }
      }
      return true;
    }
    if ((this.items == null) || (this.items.isEmpty())) {
      return true;
    }
    for (Item item : getItemsAsArray()) {
      if (!item.isTemporary()) {
        return false;
      }
    }
    return true;
  }
  
  public final void addCreationWindowWatcher(Player creature)
  {
    if (this.watchers == null) {
      this.watchers = new HashSet();
    }
    if (!this.watchers.contains(creature)) {
      this.watchers.add(creature);
    }
  }
  
  public final void addWatcher(long inventoryWindow, Creature creature)
  {
    if (this.watchers == null) {
      this.watchers = new HashSet();
    }
    if (!this.watchers.contains(creature)) {
      this.watchers.add(creature);
    }
    if ((inventoryWindow >= 1L) && (inventoryWindow <= 4L))
    {
      if ((this.tradeWindow != null) && (this.tradeWindow.getWurmId() == inventoryWindow)) {
        if (this.parentId != -10L)
        {
          try
          {
            if (Items.getItem(this.parentId).isViewableBy(creature)) {
              creature.getCommunicator().sendAddToInventory(this, inventoryWindow, inventoryWindow, -1);
            }
            if (isBodyPart()) {
              sendContainedItems(inventoryWindow, creature);
            }
          }
          catch (NoSuchItemException nsi)
          {
            logWarn(this.id + " has parent " + this.parentId + " but " + nsi.getMessage(), nsi);
          }
        }
        else if ((getTemplateId() == 0) || (isBodyPart()))
        {
          creature.getCommunicator().sendAddToInventory(this, inventoryWindow, inventoryWindow, -1);
          sendContainedItems(inventoryWindow, creature);
        }
      }
    }
    else if (this.parentId != -10L)
    {
      try
      {
        if (Items.getItem(this.parentId).isViewableBy(creature))
        {
          if (isInside(new int[] { 1333, 1334 }))
          {
            Item parentBags = getFirstParent(new int[] { 1333, 1334 });
            creature.getCommunicator().sendAddToInventory(this, parentBags.getWurmId(), parentBags.getWurmId(), -1);
          }
          Item parentWindow = recursiveParentCheck();
          if ((parentWindow != null) && (parentWindow != this)) {
            creature.getCommunicator().sendAddToInventory(this, parentWindow.getWurmId(), parentWindow.getWurmId(), -1);
          }
          creature.getCommunicator().sendAddToInventory(this, inventoryWindow, inventoryWindow, -1);
        }
        if (isBodyPart()) {
          sendContainedItems(inventoryWindow, creature);
        }
      }
      catch (NoSuchItemException nsi)
      {
        logWarn(this.id + " has parent " + this.parentId + " but " + nsi.getMessage(), nsi);
      }
    }
    else if (getTemplateId() == 0)
    {
      creature.getCommunicator().sendAddToInventory(this, inventoryWindow, inventoryWindow, -1);
    }
    else if (isBodyPart())
    {
      creature.getCommunicator().sendAddToInventory(this, inventoryWindow, inventoryWindow, -1);
      sendContainedItems(inventoryWindow, creature);
    }
    else if (isHollow())
    {
      if (isBanked()) {
        creature.getCommunicator().sendAddToInventory(this, inventoryWindow, inventoryWindow, -1);
      }
      if (this.watchers.size() == 1)
      {
        VolaTile t = Zones.getTileOrNull(getTilePos(), isOnSurface());
        if (t != null) {
          if (getTopParent() == getWurmId()) {
            t.sendAnimation(creature, this, "open", false, false);
          }
        }
      }
    }
    else
    {
      creature.getCommunicator().sendAddToInventory(this, inventoryWindow, inventoryWindow, -1);
    }
  }
  
  public final void sendContainedItems(long inventoryWindow, Creature creature)
  {
    if (!isHollow()) {
      return;
    }
    if (!isViewableBy(creature)) {
      return;
    }
    int sentCount = 0;
    for (Item item : getItems())
    {
      if (sentCount >= 1000) {
        break;
      }
      if ((!isCrate()) || (!item.isBulkItem()))
      {
        item.addWatcher(inventoryWindow, creature);
        sentCount++;
      }
      else
      {
        int storageSpace = this.template.templateId == 852 ? 300 : 150;
        
        Item[] cargo = getItemsAsArray();
        for (Item cargoItem : cargo)
        {
          int count = cargoItem.getBulkNums();
          if (count > storageSpace)
          {
            ItemTemplate itemp = cargoItem.getRealTemplate();
            if (itemp != null)
            {
              String cargoName = cargoItem.getName();
              int newSize = itemp.getVolume() * storageSpace;
              int oldSize = cargoItem.getVolume();
              
              String toSend = "Trimming size of " + cargoName + " to " + newSize + " instead of " + oldSize + " at " + getTileX() + "," + getTileY();
              logInfo(toSend);
              Message mess = new Message(null, (byte)11, "GM", "<System> " + toSend);
              
              Server.getInstance().addMessage(mess);
              Players.addGmMessage("System", "Trimming crate size of " + cargoName + " to " + newSize + " instead of " + oldSize);
              
              cargoItem.setWeight(newSize, true);
            }
          }
        }
        item.addWatcher(inventoryWindow, creature);
        sentCount++;
      }
    }
    if (getItemCount() > 0) {
      creature.getCommunicator().sendIsEmpty(inventoryWindow, getWurmId());
    }
  }
  
  public final void removeWatcher(Creature creature, boolean send)
  {
    removeWatcher(creature, send, false);
  }
  
  public final void removeWatcher(Creature creature, boolean send, boolean recursive)
  {
    if (this.watchers != null) {
      if (this.watchers.contains(creature))
      {
        this.watchers.remove(creature);
        if ((this.parentId != -10L) && (send)) {
          creature.getCommunicator().sendRemoveFromInventory(this);
        }
        if (isHollow())
        {
          if (this.items != null) {
            for (Item item : this.items) {
              item.removeWatcher(creature, false, true);
            }
          }
          if ((this.watchers.isEmpty()) && (!recursive))
          {
            VolaTile t = Zones.getTileOrNull(getTilePos(), isOnSurface());
            if (t != null) {
              if ((getTopParent() == getWurmId()) || (isPlacedOnParent())) {
                t.sendAnimation(creature, this, "close", false, false);
              }
            }
          }
        }
      }
    }
  }
  
  public final Set<Creature> getWatcherSet()
  {
    return this.watchers;
  }
  
  @Nonnull
  public final Creature[] getWatchers()
    throws NoSuchCreatureException
  {
    if (this.watchers != null) {
      return (Creature[])this.watchers.toArray(new Creature[this.watchers.size()]);
    }
    throw new NoSuchCreatureException("Not watched");
  }
  
  public final boolean isViewableBy(Creature creature)
  {
    if (this.parentId == this.id)
    {
      logWarn("This shouldn't happen!");
      return true;
    }
    if ((isLockable()) && (getLockId() != -10L)) {
      try
      {
        Item lock = Items.getItem(this.lockid);
        if ((creature.hasKeyForLock(lock)) || 
          ((isDraggable()) && (MethodsItems.mayUseInventoryOfVehicle(creature, this))) || 
          ((getTemplateId() == 850) && (MethodsItems.mayUseInventoryOfVehicle(creature, this))) || (
          (isLocked()) && (mayAccessHold(creature))))
        {
          if (this.parentId != -10L) {
            return getParent().isViewableBy(creature);
          }
          return true;
        }
        return false;
      }
      catch (NoSuchItemException nsi)
      {
        logWarn(this.id + " is locked but lock " + this.lockid + " can not be found.", nsi);
        try
        {
          if (this.parentId != -10L) {
            return getParent().isViewableBy(creature);
          }
          return true;
        }
        catch (NoSuchItemException nsa)
        {
          logWarn(this.id + " has parent " + this.parentId + " but " + nsa.getMessage(), nsa);
        }
      }
    }
    if (this.parentId == -10L) {
      return true;
    }
    try
    {
      return getParent().isViewableBy(creature);
    }
    catch (NoSuchItemException nsi)
    {
      logWarn(this.id + " has parent " + this.parentId + " but " + nsi.getMessage(), nsi);
    }
    return true;
  }
  
  public final Item getParent()
    throws NoSuchItemException
  {
    if (this.parentId != -10L) {
      return Items.getItem(this.parentId);
    }
    throw new NoSuchItemException("No parent.");
  }
  
  final long getLastOwner()
  {
    return this.lastOwner;
  }
  
  public final long getLastParentId()
  {
    return this.lastParentId;
  }
  
  private void setThisAsParentFor(Item item, boolean forceUpdateParent)
  {
    if ((item.getDbStrings() instanceof FrozenItemDbStrings))
    {
      item.returnFromFreezer();
      item.deleteInDatabase();
      item.setDbStrings(ItemDbStrings.getInstance());
      logInfo("Returning from frozen: " + item.getName() + " " + item.getWurmId(), new Exception());
    }
    if (item.getWatcherSet() != null) {
      try
      {
        for (Creature watcher : item.getWatchers()) {
          if (this.watchers != null) {
            if (!this.watchers.contains(watcher)) {
              item.removeWatcher(watcher, true);
            }
          }
        }
      }
      catch (NoSuchCreatureException localNoSuchCreatureException1) {}
    }
    if (forceUpdateParent) {
      try
      {
        Item oldParent = item.getParent();
        if (!oldParent.hasSameOwner(item)) {
          oldParent.dropItem(item.getWurmId(), false);
        } else if (this != oldParent) {
          oldParent.removeItem(item.getWurmId(), false, false, false);
        }
      }
      catch (NoSuchItemException localNoSuchItemException1) {}
    }
    if ((getTemplateId() == 621) && (item.isSaddleBags())) {
      try
      {
        Creature owner = Server.getInstance().getCreature(this.ownerId);
        if ((owner.isAnimal()) && (owner.isVehicle()))
        {
          byte equipmentSlot = BodyTemplate.convertToItemEquipementSlot((byte)getParent().place);
          owner.getCurrentTile().sendHorseWear(this.ownerId, getTemplateId(), getMaterial(), equipmentSlot, getAuxData());
        }
      }
      catch (NoSuchPlayerException|NoSuchCreatureException|NoSuchItemException localNoSuchPlayerException1) {}
    }
    item.setParentId(this.id, isOnSurface());
    item.lastParentId = this.id;
    long itemOwnerId = -10L;
    long lOwnerId = getOwnerId();
    try
    {
      itemOwnerId = item.getOwner();
      if (itemOwnerId != lOwnerId) {
        item.setOwner(lOwnerId, true);
      } else if (this.watchers != null) {
        for (Creature watcher : this.watchers) {
          if ((item.watchers == null) || (!item.watchers.contains(watcher))) {
            item.addWatcher(getTopParent(), watcher);
          }
        }
      }
    }
    catch (NotOwnedException ex)
    {
      try
      {
        item.setOwner(lOwnerId, true);
        if (lOwnerId == -10L)
        {
          Iterator<Creature> it;
          if (this.watchers != null) {
            for (it = this.watchers.iterator(); it.hasNext();)
            {
              Creature watcher = (Creature)it.next();
              long inventoryWindow = item.getTopParent();
              
              item.addWatcher(inventoryWindow, watcher);
            }
          }
          if (isFire())
          {
            if ((item.isFoodMaker()) || (item.isFood()))
            {
              VolaTile t = Zones.getTileOrNull(getTilePos(), isOnSurface());
              if (t != null) {
                t.renameItem(this);
              }
            }
          }
          else if ((isWeaponContainer()) || (isBarrelRack()))
          {
            VolaTile t = Zones.getTileOrNull(getTilePos(), isOnSurface());
            if (t != null) {
              t.renameItem(this);
            }
          }
        }
      }
      catch (Exception ex2)
      {
        logWarn("Failed to set ownerId to " + lOwnerId + " for item " + item.getWurmId(), ex2);
      }
    }
  }
  
  public final void setOwner(long newOwnerId, boolean startWatching)
  {
    setOwner(newOwnerId, -10L, startWatching);
  }
  
  public final void setOwner(long newOwnerId, long newParent, boolean startWatching)
  {
    long oldOwnerId = getOwnerId();
    if ((isCoin()) && (getValue() >= 1000000)) {
      logInfo("COINLOG " + newOwnerId + ", " + getWurmId() + " banked " + this.banked + " mailed=" + this.mailed, new Exception());
    }
    Creature creature;
    if (newOwnerId != -10L)
    {
      if ((oldOwnerId == -10L) || (oldOwnerId != newOwnerId))
      {
        if (oldOwnerId == -10L)
        {
          int timesSinceLastUsed = (int)((WurmCalendar.currentTime - this.lastMaintained) / this.template.getDecayTime());
          if (timesSinceLastUsed > 0) {
            setLastMaintained(WurmCalendar.currentTime);
          }
        }
        setZoneId(-10, this.surfaced);
        setOwnerId(newOwnerId);
        this.watchers = null;
        Creature owner = null;
        try
        {
          owner = Server.getInstance().getCreature(newOwnerId);
          if (Constants.useItemTransferLog)
          {
            ItemTransfer transfer = new ItemTransfer(this.id, this.name, oldOwnerId, String.valueOf(oldOwnerId), newOwnerId, owner.getName(), System.currentTimeMillis());
            itemLogger.addToQueue(transfer);
          }
          if (isCoin())
          {
            Server.getInstance().transaction(this.id, oldOwnerId, newOwnerId, owner.getName(), getValue());
            owner.addCarriedWeight(getWeightGrams());
          }
          else if (isBodyPart())
          {
            if (isBodyPartRemoved()) {
              owner.addCarriedWeight(getWeightGrams());
            }
          }
          else
          {
            owner.addCarriedWeight(getWeightGrams());
          }
          if (startWatching) {
            try
            {
              Creature[] watcherArr = getParent().getWatchers();
              long tp = getTopParent();
              for (Creature c : watcherArr) {
                if (c == owner) {
                  addWatcher(-1L, owner);
                } else {
                  addWatcher(tp, c);
                }
              }
            }
            catch (NoSuchItemException nsi)
            {
              addWatcher(-1L, owner);
            }
          }
          if ((isArtifact()) || ((isUnique()) && (!isRoyal())))
          {
            owner.getCommunicator().sendNormalServerMessage("You will drop the " + 
              getName() + " when you leave the world.");
            if (getTemplateId() == 329) {
              owner.getCombatHandler().setRodEffect(true);
            }
            if (getTemplateId() == 335) {
              owner.setHasFingerEffect(true);
            }
            if ((getTemplateId() == 331) || (getTemplateId() == 330)) {
              owner.getCommunicator().sendAlertServerMessage("Also, when you drop the " + 
                getName() + ", any aggressive pet you have will become enraged and lose its loyalty.");
            }
            if (getTemplateId() == 338) {
              owner.getCommunicator().sendAlertServerMessage("Also, when you drop the " + 
                getName() + ", any pet you have will become enraged and lose its loyalty.");
            }
            if ((isArtifact()) && (getAuxData() > 30))
            {
              if ((Servers.isThisATestServer()) && (Servers.isThisAPvpServer()) && 
                (owner.getPower() >= 2)) {
                owner.getCommunicator().sendNormalServerMessage("Old power = " + getAuxData() + ".");
              }
              setAuxData((byte)30);
              if ((Servers.isThisATestServer()) && (Servers.isThisAPvpServer()) && 
                (owner.getPower() >= 2)) {
                owner.getCommunicator().sendNormalServerMessage("New power = " + getAuxData() + ".");
              }
            }
          }
          if (isKey()) {
            owner.addKey(this, false);
          }
        }
        catch (NoSuchPlayerException localNoSuchPlayerException1) {}catch (NoSuchCreatureException localNoSuchCreatureException1) {}
      }
      else if (this.zoneId != -10L)
      {
        logWarn(getName() + " new owner " + newOwnerId + " zone id was " + this.zoneId, new Exception());
        setZoneId(-10, true);
      }
    }
    else if (oldOwnerId != -10L)
    {
      creature = null;
      try
      {
        setZoneId(-10, true);
        creature = Server.getInstance().getCreature(oldOwnerId);
        if ((Constants.useItemTransferLog) && (!isBodyPartAttached()) && (!isInventory()))
        {
          ItemTransfer transfer = new ItemTransfer(this.id, this.name, oldOwnerId, creature.getName(), newOwnerId, "" + this.ownerId, System.currentTimeMillis());
          itemLogger.addToQueue(transfer);
        }
        if (!isLocked()) {
          setLastOwnerId(oldOwnerId);
        }
        if (isCoin())
        {
          if (getParentId() != -10L)
          {
            Server.getInstance().transaction(this.id, oldOwnerId, newOwnerId, creature.getName(), getValue());
            if (!creature.removeCarriedWeight(getWeightGrams())) {
              logWarn(getName() + " removed " + getWeightGrams(), new Exception());
            }
          }
        }
        else if (isBodyPart())
        {
          if (isBodyPartRemoved()) {
            if (!creature.removeCarriedWeight(getWeightGrams())) {
              logWarn(getName() + " removed " + getWeightGrams(), new Exception());
            }
          }
        }
        else if (!creature.removeCarriedWeight(getWeightGrams())) {
          logWarn(getName() + " removed " + getWeightGrams(), new Exception());
        }
        if (isArmour()) {
          setWornAsArmour(false, oldOwnerId);
        }
        if (isArtifact())
        {
          if (getTemplateId() == 329) {
            creature.getCombatHandler().setRodEffect(false);
          }
          if (getTemplateId() == 335) {
            creature.setHasFingerEffect(false);
          }
          if (getTemplateId() == 330) {
            creature.setHasCrownEffect(false);
          }
          if ((getTemplateId() == 331) || (getTemplateId() == 330) || 
            (getTemplateId() == 338))
          {
            boolean untame = false;
            if (newParent != -10L)
            {
              Item newParentItem = Items.getItem(newParent);
              if (newParentItem.getOwnerId() != oldOwnerId) {
                untame = true;
              }
            }
            else
            {
              untame = true;
            }
            if ((creature.getPet() != null) && (untame))
            {
              Creature pet = creature.getPet();
              creature.getCommunicator().sendAlertServerMessage("As you drop the " + 
                getName() + ", you feel rage and confusion from the " + pet
                .getName() + ".");
              creature.setPet(-10L);
              pet.setLoyalty(0.0F);
              pet.setDominator(-10L);
            }
          }
        }
        removeWatcher(creature, true);
        if (isKey()) {
          creature.removeKey(this, false);
        }
        if (isLeadCreature()) {
          if (creature.isItemLeading(this)) {
            creature.dropLeadingItem(this);
          }
        }
        if ((!isFood()) && (!isAlwaysPoll()))
        {
          long decayt = this.template.getDecayTime();
          
          int timesSinceLastUsed = (int)((WurmCalendar.currentTime - this.lastMaintained) / decayt);
          if (timesSinceLastUsed > 0) {
            setLastMaintained(WurmCalendar.currentTime);
          }
        }
      }
      catch (NoSuchPlayerException nsp)
      {
        logWarn("Removing object from unknown player: ", nsp);
      }
      catch (NoSuchCreatureException cnf)
      {
        logWarn("Removing object from unknown creature: ", cnf);
      }
      catch (Exception ex)
      {
        logWarn("Failed to save creature when dropping item with id " + this.id, ex);
      }
      setOwnerId(-10L);
    }
    else
    {
      setOwnerId(-10L);
    }
    if (isHollow()) {
      if (this.items != null) {
        for (Item item : this.items) {
          if ((!isSealedByPlayer()) || (item.getTemplateId() != 169)) {
            if (item != this) {
              item.setOwner(newOwnerId, false);
            } else {
              logWarn("Item with id " + this.id + " has itself in the inventory!");
            }
          }
        }
      }
    }
  }
  
  public final Item dropItem(long aId, boolean setPosition)
    throws NoSuchItemException
  {
    return dropItem(aId, -10L, setPosition, false);
  }
  
  public final Item dropItem(long aId, long newParent, boolean setPosition)
    throws NoSuchItemException
  {
    return dropItem(aId, newParent, setPosition, false);
  }
  
  public final Item dropItem(long aId, boolean setPosition, boolean skipPileRemoval)
    throws NoSuchItemException
  {
    return dropItem(aId, -10L, setPosition, skipPileRemoval);
  }
  
  public final Item dropItem(long aId, long newParent, boolean setPosition, boolean skipPileRemoval)
    throws NoSuchItemException
  {
    Item toReturn = removeItem(aId, setPosition, true, skipPileRemoval);
    
    toReturn.setOwner(-10L, newParent, false);
    toReturn.setParentId(-10L, this.surfaced);
    return toReturn;
  }
  
  public static int[] getDropTile(Creature creature)
    throws NoSuchZoneException
  {
    float lPosX = creature.getStatus().getPositionX();
    float lPosY = creature.getStatus().getPositionY();
    if (creature.getBridgeId() != -10L)
    {
      int newTileX = CoordUtils.WorldToTile(lPosX);
      int newTileY = CoordUtils.WorldToTile(lPosY);
      return new int[] { newTileX, newTileY };
    }
    float rot = creature.getStatus().getRotation();
    float xPosMod = (float)Math.sin(rot * 0.017453292F) * (1.0F + Server.rand.nextFloat());
    float yPosMod = -(float)Math.cos(rot * 0.017453292F) * (1.0F + Server.rand.nextFloat());
    float newPosX = lPosX + xPosMod;
    float newPosY = lPosY + yPosMod;
    BlockingResult result = Blocking.getBlockerBetween(creature, lPosX, lPosY, newPosX, newPosY, creature
      .getPositionZ(), creature.getPositionZ(), creature.isOnSurface(), creature.isOnSurface(), false, 4, -1L, creature
      .getBridgeId(), creature.getBridgeId(), false);
    if (result != null)
    {
      newPosX = lPosX + (float)Math.sin(rot * 0.017453292F) * (-1.0F + Server.rand.nextFloat());
      newPosY = lPosY - (float)Math.cos(rot * 0.017453292F) * (-1.0F + Server.rand.nextFloat());
    }
    int newTileX = CoordUtils.WorldToTile(newPosX);
    int newTileY = CoordUtils.WorldToTile(newPosY);
    return new int[] { newTileX, newTileY };
  }
  
  public final void putItemInCorner(Creature creature, int cornerX, int cornerY, boolean onSurface, long bridgeId, boolean atFeet)
    throws NoSuchItemException
  {
    float lRotation;
    float lRotation;
    if (isRoadMarker())
    {
      lRotation = 0.0F;
    }
    else
    {
      float lRotation;
      if (isTileAligned()) {
        lRotation = 90.0F * Creature.normalizeAngle((int)((creature.getStatus().getRotation() + 45.0F) / 90.0F));
      } else {
        lRotation = Creature.normalizeAngle(creature.getStatus().getRotation());
      }
    }
    long lParentId = getParentId();
    if (lParentId != -10L)
    {
      Item parent = Items.getItem(lParentId);
      parent.dropItem(getWurmId(), false);
    }
    float newPosX = CoordUtils.TileToWorld(cornerX);
    float newPosY = CoordUtils.TileToWorld(cornerY);
    if (atFeet)
    {
      newPosX = creature.getPosX();
      newPosY = creature.getPosY();
    }
    else if (!isRoadMarker())
    {
      newPosX += 0.005F;
      newPosY += 0.005F;
      if (creature.getTileX() < cornerX) {
        newPosX -= 0.01F;
      }
      if (creature.getTileY() < cornerY) {
        newPosY -= 0.01F;
      }
    }
    newPosX = Math.max(0.0F, newPosX);
    newPosY = Math.max(0.0F, newPosY);
    newPosY = Math.min(Zones.worldMeterSizeY, newPosY);
    newPosX = Math.min(Zones.worldMeterSizeX, newPosX);
    
    setOnBridge(bridgeId);
    setSurfaced(onSurface);
    float npsz = Zones.calculatePosZ(newPosX, newPosY, null, onSurface, 
      (isFloating()) && (getCurrentQualityLevel() > 10.0F), getPosZ(), creature, onBridge());
    
    setPos(newPosX, newPosY, npsz, lRotation, onBridge());
    try
    {
      Zone zone = Zones.getZone(Zones.safeTileX((int)newPosX >> 2), 
        Zones.safeTileY((int)newPosY >> 2), onSurface);
      zone.addItem(this);
    }
    catch (NoSuchZoneException sex)
    {
      logWarn(sex.getMessage(), sex);
      creature.getInventory().insertItem(this, true);
      creature.getCommunicator().sendNormalServerMessage("Unable to drop there.");
    }
  }
  
  public final void putItemInfrontof(Creature creature)
    throws NoSuchCreatureException, NoSuchItemException, NoSuchPlayerException, NoSuchZoneException
  {
    putItemInfrontof(creature, 1.0F);
  }
  
  public final void putItemInfrontof(Creature creature, float distance)
    throws NoSuchCreatureException, NoSuchItemException, NoSuchPlayerException, NoSuchZoneException
  {
    CreatureStatus creatureStatus = creature.getStatus();
    float lPosX = creatureStatus.getPositionX();
    float lPosY = creatureStatus.getPositionY();
    
    float rot = Creature.normalizeAngle(creatureStatus.getRotation());
    float xPosMod = (float)Math.sin(rot * 0.017453292F) * (distance + Server.rand.nextFloat() * distance);
    float yPosMod = -(float)Math.cos(rot * 0.017453292F) * (distance + Server.rand.nextFloat() * distance);
    
    float newPosX = lPosX + xPosMod;
    float newPosY = lPosY + yPosMod;
    
    boolean onSurface = creature.isOnSurface();
    if (distance != 0.0F)
    {
      BlockingResult result = Blocking.getBlockerBetween(creature, lPosX, lPosY, newPosX, newPosY, creature
        .getPositionZ(), creature.getPositionZ(), onSurface, onSurface, false, 4, -1L, creature
        .getBridgeId(), creature.getBridgeId(), false);
      if (result != null)
      {
        newPosX = lPosX + (float)Math.sin(rot * 0.017453292F) * (-1.0F + Server.rand.nextFloat());
        newPosY = lPosY - (float)Math.cos(rot * 0.017453292F) * (-1.0F + Server.rand.nextFloat());
      }
    }
    setOnBridge(creatureStatus.getBridgeId());
    if (onBridge() != -10L)
    {
      newPosX = lPosX;
      newPosY = lPosY;
    }
    if ((!onSurface) && (distance != 0.0F)) {
      if (Tiles.isSolidCave(Tiles.decodeType(Server.caveMesh.getTile(Zones.safeTileX((int)newPosX >> 2), 
        Zones.safeTileY((int)newPosY >> 2)))))
      {
        newPosX = lPosX;
        newPosY = lPosY;
      }
    }
    newPosX = Math.max(0.0F, newPosX);
    newPosY = Math.max(0.0F, newPosY);
    newPosY = Math.min(Zones.worldMeterSizeY, newPosY);
    newPosX = Math.min(Zones.worldMeterSizeX, newPosX);
    float lRotation;
    float lRotation;
    if (isTileAligned()) {
      lRotation = 90.0F * Creature.normalizeAngle((int)((creatureStatus.getRotation() + 45.0F) / 90.0F));
    } else {
      lRotation = Creature.normalizeAngle(creatureStatus.getRotation());
    }
    long lParentId = getParentId();
    if (lParentId != -10L)
    {
      Item parent = Items.getItem(lParentId);
      parent.dropItem(getWurmId(), false);
    }
    float npsz = Zones.calculatePosZ(newPosX, newPosY, null, onSurface, 
      (isFloating()) && (getCurrentQualityLevel() > 10.0F), getPosZ(), creature, onBridge());
    setPos(newPosX, newPosY, npsz, lRotation, onBridge());
    setSurfaced(onSurface);
    try
    {
      Zone zone = Zones.getZone(Zones.safeTileX((int)newPosX >> 2), Zones.safeTileY((int)newPosY >> 2), onSurface);
      
      zone.addItem(this);
      if ((creature.getPower() == 5) && (isBoat())) {
        creature.getCommunicator().sendNormalServerMessage("Adding to zone " + zone
          .getId() + " at " + Zones.safeTileX((int)newPosX >> 2) + ", " + 
          Zones.safeTileY((int)newPosY >> 2) + ", surf=" + onSurface);
      }
    }
    catch (NoSuchZoneException sex)
    {
      logWarn(sex.getMessage(), sex);
      creature.getInventory().insertItem(this, true);
      creature.getCommunicator().sendNormalServerMessage("Unable to drop there.");
    }
  }
  
  public final float calculatePosZ(VolaTile tile, @Nullable Creature creature)
  {
    boolean floating = (isFloating()) && (getCurrentQualityLevel() > 10.0F);
    return Zones.calculatePosZ(getPosX(), getPosY(), tile, isOnSurface(), floating, getPosZ(), creature, onBridge());
  }
  
  public final void updatePosZ(VolaTile tile)
  {
    setPosZ(calculatePosZ(tile, null));
  }
  
  public final boolean isWarmachine()
  {
    return this.template.isWarmachine();
  }
  
  private boolean isWeaponContainer()
  {
    int templateId = getTemplateId();
    switch (templateId)
    {
    case 724: 
    case 725: 
    case 758: 
    case 759: 
    case 892: 
      return true;
    }
    return false;
  }
  
  private Item removeItem(long _id, boolean setPosition, boolean ignoreWatchers, boolean skipPileRemoval)
    throws NoSuchItemException
  {
    if (!isHollow()) {
      throw new NoSuchItemException(String.valueOf(_id));
    }
    Item item = Items.getItem(_id);
    if ((item.isBodyPart()) && (item.isBodyPartAttached())) {
      throw new NoSuchItemException("Can't remove parts from a live body.");
    }
    long ownerId = getOwnerId();
    if (this.place > 0) {
      try
      {
        byte equipmentSlot = BodyTemplate.convertToItemEquipementSlot((byte)this.place);
        if ((item.isArmour()) && (item.wornAsArmour)) {
          equipmentSlot = BodyTemplate.convertToArmorEquipementSlot((byte)this.place);
        }
        Creature owner = Server.getInstance().getCreature(ownerId);
        if ((owner.isAnimal()) && (owner.isVehicle())) {
          owner.getCurrentTile().sendRemoveHorseWear(ownerId, item.getTemplateId(), equipmentSlot);
        } else {
          owner.getCurrentTile().sendRemoveWearItem(ownerId, equipmentSlot);
        }
        if ((item.hasItemBonus()) && (owner.isPlayer())) {
          ItemBonus.removeBonus(item, owner);
        }
        if (item.getTemplateId() == 330) {
          owner.setHasCrownEffect(false);
        }
      }
      catch (Exception localException) {}
    }
    removeItem(item);
    byte equipmentSlot;
    if ((getTemplateId() == 621) && (item.isSaddleBags())) {
      try
      {
        Creature owner = Server.getInstance().getCreature(ownerId);
        if ((owner.isAnimal()) && (owner.isVehicle()))
        {
          equipmentSlot = BodyTemplate.convertToItemEquipementSlot((byte)getParent().place);
          owner.getCurrentTile().sendHorseWear(ownerId, getTemplateId(), getMaterial(), equipmentSlot, getAuxData());
        }
      }
      catch (NoSuchPlayerException|NoSuchCreatureException localNoSuchPlayerException1) {}
    }
    VirtualZone localVirtualZone1;
    VirtualZone vz;
    if ((getTemplate().hasViewableSubItems()) && ((!getTemplate().isContainerWithSubItems()) || (item.isPlacedOnParent())))
    {
      VolaTile vt = Zones.getTileOrNull(getTileX(), getTileY(), isOnSurface());
      if (vt != null)
      {
        equipmentSlot = vt.getWatchers();int i = equipmentSlot.length;
        for (localVirtualZone1 = 0; localVirtualZone1 < i; localVirtualZone1++)
        {
          vz = equipmentSlot[localVirtualZone1];
          vz.getWatcher().getCommunicator().sendRemoveItem(item);
        }
      }
    }
    if (item.wornAsArmour) {
      item.setWornAsArmour(false, ownerId);
    }
    boolean send = true;
    if (item.isArmour())
    {
      byte[] armourplaces = item.getBodySpaces();
      byte[] arrayOfByte1 = armourplaces;localVirtualZone1 = arrayOfByte1.length;
      for (vz = 0; vz < localVirtualZone1; vz++)
      {
        byte armourplace = arrayOfByte1[vz];
        if (armourplace == this.place) {
          send = false;
        }
      }
    }
    else if (isFire())
    {
      if ((item.isFoodMaker()) || (item.isFood()))
      {
        VolaTile t = Zones.getTileOrNull(getTilePos(), isOnSurface());
        if (t != null) {
          t.renameItem(this);
        }
      }
    }
    VolaTile t;
    if ((isWeaponContainer()) || (isBarrelRack())) {
      if (isEmpty(false))
      {
        t = Zones.getTileOrNull(getTilePos(), isOnSurface());
        if (t != null) {
          t.renameItem(this);
        }
      }
    }
    if ((getTemplate().getContainerRestrictions() != null) && (!getTemplate().isNoPut())) {
      for (ContainerRestriction cRest : getTemplate().getContainerRestrictions()) {
        if (cRest.doesItemOverrideSlot(item))
        {
          skipAdd = false;
          for (Item i : getItems()) {
            if ((i.getTemplateId() == 1392) && (cRest.contains(i.getRealTemplateId()))) {
              skipAdd = true;
            } else if (cRest.contains(i.getTemplateId())) {
              skipAdd = true;
            }
          }
          if (!skipAdd) {
            try
            {
              Item tempSlotItem = ItemFactory.createItem(1392, 100.0F, getCreatorName());
              tempSlotItem.setRealTemplate(cRest.getEmptySlotTemplateId());
              tempSlotItem.setName(cRest.getEmptySlotName());
              
              insertItem(tempSlotItem, true);
            }
            catch (FailedException|NoSuchTemplateException localFailedException) {}
          }
        }
      }
    }
    boolean skipAdd;
    if (send)
    {
      if ((this.ownerId > 0L) && ((this.place == 37) || (this.place == 38))) {
        if (isBodyPartAttached()) {
          try
          {
            Creature owner = Server.getInstance().getCreature(ownerId);
            owner.getCurrentTile().sendWieldItem(ownerId, (byte)(this.place == 37 ? 0 : 1), "", (byte)0, 0, 0, 0, 0, 0, 0);
          }
          catch (NoSuchCreatureException localNoSuchCreatureException) {}catch (NoSuchPlayerException nsp)
          {
            logWarn(nsp.getMessage(), nsp);
          }
        }
      }
      if ((this.place == 3) && (this.ownerId > 0L) && (item.isShield())) {
        try
        {
          Creature owner = Server.getInstance().getCreature(ownerId);
          owner.getCommunicator().sendToggleShield(false);
        }
        catch (NoSuchCreatureException localNoSuchCreatureException1) {}catch (NoSuchPlayerException nsp)
        {
          logWarn(nsp.getMessage(), nsp);
        }
      }
    }
    long topParent = getTopParent();
    if (isEmpty(false))
    {
      if (this.watchers != null) {
        for (Creature watcher : this.watchers)
        {
          boolean isOwner = ownerId == watcher.getWurmId();
          long inventoryWindow = isOwner ? -1L : topParent;
          Communicator watcherComm = watcher.getCommunicator();
          if ((item.getTopParentOrNull() != null) && 
            (!item.getTopParentOrNull().isEquipmentSlot()) && 
            (!item.getTopParentOrNull().isBodyPart()) && 
            (!item.getTopParentOrNull().isInventory())) {
            watcherComm.sendRemoveFromInventory(item, inventoryWindow);
          }
          watcherComm.sendIsEmpty(inventoryWindow, getWurmId());
        }
      }
      if ((getTemplateId() == 177) && (!skipPileRemoval)) {
        try
        {
          Zone z = Zones.getZone((int)getPosX() >> 2, (int)getPosY() >> 2, isOnSurface());
          z.removeItem(this);
        }
        catch (NoSuchZoneException localNoSuchZoneException1) {}
      }
      if ((getTemplateId() == 995) || (getTemplateId() == 1422)) {
        if (isEmpty(false))
        {
          Items.destroyItem(getWurmId());
          
          item.parentId = -10L;
        }
      }
    }
    else if ((item.getTopParent() == topParent) && 
      (!isEmpty(false)) && 
      (item.getTopParentOrNull() != null) && 
      (!item.getTopParentOrNull().isInventory()) && 
      (!item.getTopParentOrNull().isBodyPart()) && 
      (!item.getTopParentOrNull().isEquipmentSlot()))
    {
      if ((this.watchers != null) && (!ignoreWatchers)) {
        for (Creature watcher : this.watchers)
        {
          boolean isOwner = ownerId == watcher.getWurmId();
          long inventoryWindow = isOwner ? -1L : topParent;
          watcher.getCommunicator().sendRemoveFromInventory(item, inventoryWindow);
        }
      }
    }
    else if (item.getTopParent() != topParent)
    {
      if ((ownerId != -10L) && (item.getOwnerId() == ownerId)) {
        if (this.watchers != null) {
          for (Creature watcher : this.watchers)
          {
            logInfo(watcher.getName() + " checking if stopping to watch " + item.getName());
            if ((item.watchers == null) || (!item.watchers.contains(watcher)))
            {
              logInfo("Removing watcher " + watcher + " in new method");
              item.removeWatcher(watcher, true);
            }
          }
        }
      }
    }
    if (setPosition) {
      item.setPosXYZ(getPosX(), getPosY(), getPosZ());
    }
    return item;
  }
  
  public final long getWurmId()
  {
    return this.id;
  }
  
  public final void removeAndEmpty()
  {
    this.deleted = true;
    if ((this.items != null) && (isHollow()))
    {
      VolaTile t = Zones.getTileOrNull(getTilePos(), isOnSurface());
      
      Item[] contained = getAllItems(true);
      if (isBulkContainer()) {
        for (Item aContained : contained) {
          Items.destroyItem(aContained.getWurmId());
        }
      } else {
        for (Item item : contained) {
          if (item.getTemplateId() == 1392) {
            Items.destroyItem(item.getWurmId());
          } else {
            item.setPosXYZ(getPosX(), getPosY(), item.calculatePosZ(t, null));
          }
        }
      }
    }
    try
    {
      Item parent = getParent();
      
      boolean pile = parent.getTemplateId() == 177;
      
      int x = (int)getPosX() >> 2;
      int y = (int)getPosY() >> 2;
      parent.dropItem(this.id, false);
      if (pile) {
        parent.removeFromPile(this);
      }
      Object its = getItems();
      itarr = (Item[])((Set)its).toArray(new Item[((Set)its).size()]);
      Item[] arrayOfItem2 = itarr;localCreature1 = arrayOfItem2.length;
      for (Creature localCreature2 = 0; localCreature2 < localCreature1; localCreature2++)
      {
        Item item = arrayOfItem2[localCreature2];
        
        dropItem(item.getWurmId(), false);
        if (!item.isTransferred()) {
          if (pile)
          {
            if (item.isLiquid()) {
              Items.decay(item.getWurmId(), item.getDbStrings());
            } else {
              try
              {
                Zone currentZone = Zones.getZone(x, y, isOnSurface());
                
                currentZone.addItem(item);
              }
              catch (NoSuchZoneException nsz)
              {
                logWarn(getName() + " id:" + this.id + " at " + x + ", " + y, nsz);
              }
            }
          }
          else if (item.isLiquid())
          {
            Items.decay(item.getWurmId(), item.getDbStrings());
          }
          else
          {
            Item topParent = parent.getTopParentOrNull();
            boolean dropToGround = false;
            if ((item.isUseOnGroundOnly()) && (topParent != null) && (topParent.getTemplateId() != 0)) {
              dropToGround = true;
            }
            if ((!dropToGround) && (parent.isBodyPartAttached())) {
              dropToGround = true;
            }
            if ((!dropToGround) && (!parent.isBodyPartAttached()) && (!parent.insertItem(item, false, true))) {
              dropToGround = true;
            }
            if (dropToGround) {
              try
              {
                Zone currentZone = Zones.getZone(x, y, isOnSurface());
                
                currentZone.addItem(item);
              }
              catch (NoSuchZoneException nsz)
              {
                logWarn(getName() + " id:" + this.id + " at " + x + ", " + y, nsz);
              }
            } else {
              try
              {
                Creature owner = Server.getInstance().getCreature(this.ownerId);
                owner.getInventory().insertItem(item);
              }
              catch (Exception localException) {}
            }
          }
        }
      }
    }
    catch (NoSuchItemException nsi)
    {
      Item[] itarr;
      Creature localCreature1;
      if (getParentId() != -10L) {
        return;
      }
      if (this.zoneId == -10L) {
        return;
      }
      try
      {
        int x = (int)getPosX() >> 2;
        int y = (int)getPosY() >> 2;
        Zone currentZone = Zones.getZone(x, y, isOnSurface());
        
        currentZone.removeItem(this);
        if (!isHollow()) {
          return;
        }
        Object iwatcher;
        try
        {
          Creature[] iwatchers = getWatchers();
          itarr = iwatchers;int k = itarr.length;
          for (localCreature1 = 0; localCreature1 < k; localCreature1++)
          {
            iwatcher = itarr[localCreature1];
            ((Creature)iwatcher).removeItemWatched(this);
            ((Creature)iwatcher).getCommunicator().sendCloseInventoryWindow(getWurmId());
            removeWatcher((Creature)iwatcher, true);
          }
        }
        catch (NoSuchCreatureException localNoSuchCreatureException1) {}
        Object its = getItems();
        Item[] itarr = (Item[])((Set)its).toArray(new Item[((Set)its).size()]);
        Item[] arrayOfItem3 = itarr;localCreature1 = arrayOfItem3.length;
        for (Creature localCreature3 = 0; localCreature3 < localCreature1; localCreature3++)
        {
          Item item = arrayOfItem3[localCreature3];
          try
          {
            dropItem(item.getWurmId(), false, true);
            if (!item.isTransferred()) {
              if (item.isLiquid()) {
                Items.decay(item.getWurmId(), item.getDbStrings());
              } else {
                currentZone.addItem(item);
              }
            }
          }
          catch (NoSuchItemException nsi2)
          {
            logWarn(
              getName() + " id:" + this.id + " at " + x + ", " + y + " failed to drop item " + item.getWurmId(), nsi2);
          }
          this.items.getClass();
        }
      }
      catch (NoSuchZoneException nsz)
      {
        logWarn(getName() + " id:" + this.id, nsz);
      }
    }
  }
  
  public final boolean isTypeRecycled()
  {
    return this.template.isRecycled;
  }
  
  public final boolean hideAddToCreationWindow()
  {
    return this.template.hideAddToCreationWindow();
  }
  
  private void hatch()
  {
    if ((isEgg()) && (getData1() > 0))
    {
      try
      {
        CreatureTemplate temp = CreatureTemplateFactory.getInstance().getTemplate(getData1());
        byte sex = temp.getSex();
        if (sex == 0) {
          if (Server.rand.nextInt(2) == 0) {
            sex = 1;
          }
        }
        if ((temp.isUnique()) || (Server.rand.nextInt(10) == 0)) {
          if ((temp.isUnique()) || (Creatures.getInstance().getNumberOfCreatures() < Servers.localServer.maxCreatures))
          {
            CreatureTemplate ct = CreatureTemplateFactory.getInstance().getTemplate(getData1());
            String cname = "";
            String description = getDescription();
            if (!description.isEmpty()) {
              cname = LoginHandler.raiseFirstLetter(description
                .substring(0, Math.min(description.length(), 10)) + " the " + ct
                .getName());
            }
            Creature c = Creature.doNew(getData1(), false, getPosX(), getPosY(), Server.rand.nextInt(360), 
              isOnSurface() ? 0 : -1, cname, sex, (byte)0, (byte)0, false, (byte)1);
            if (temp.isUnique()) {
              logInfo("Player/creature with wurmid " + getLastOwnerId() + " hatched " + c.getName() + " at " + (int)this.posX / 4 + "," + (int)this.posY / 4);
            }
            if (Servers.isThisATestServer()) {
              Players.getInstance().sendGmMessage(null, "System", "Debug: Player/creature with wurmid " + getLastOwnerId() + " hatched " + c.getName() + " at " + (int)this.posX / 4 + "," + (int)this.posY / 4, false);
            }
            if (getData1() == 48) {
              switch (Server.rand.nextInt(3))
              {
              case 0: 
                break;
              case 1: 
                c.getStatus().setTraitBit(15, true);
                break;
              case 2: 
                c.getStatus().setTraitBit(16, true);
              }
            }
          }
        }
      }
      catch (Exception ex)
      {
        logWarn(ex.getMessage() + ' ' + getData1());
      }
      setData1(-1);
    }
    else if (getTemplateId() == 466)
    {
      if (this.ownerId == -10L) {
        try
        {
          int x = (int)getPosX() >> 2;
          int y = (int)getPosY() >> 2;
          Zone currentZone = Zones.getZone(x, y, isOnSurface());
          Item i = TileRockBehaviour.createRandomGem();
          if (i != null)
          {
            i.setLastOwnerId(this.lastOwner);
            i.setPosXY(getPosX(), getPosY());
            i.setRotation(Server.rand.nextFloat() * 180.0F);
            currentZone.addItem(i);
          }
        }
        catch (Exception ex)
        {
          logWarn(ex.getMessage() + ' ' + getData1());
        }
      }
    }
  }
  
  final boolean checkDecay()
  {
    if (isHugeAltar()) {
      return false;
    }
    if ((this.qualityLevel > 0.0F) && (this.damage < 100.0F)) {
      return false;
    }
    boolean decayed = true;
    Item i;
    if (this.ownerId != -10L)
    {
      Creature owner = null;
      try
      {
        owner = Server.getInstance().getCreature(getOwnerId());
        if ((hasItemBonus()) && (owner.isPlayer())) {
          ItemBonus.removeBonus(this, owner);
        }
        try
        {
          Action act = owner.getCurrentAction();
          if (act.getSubjectId() == this.id) {
            act.stop(false);
          }
        }
        catch (NoSuchActionException localNoSuchActionException) {}
        Communicator ownerComm = owner.getCommunicator();
        if (isEgg())
        {
          if ((getTemplateId() == 466) || (getData1() > 0)) {
            ownerComm.sendNormalServerMessage(
              LoginHandler.raiseFirstLetter(getNameWithGenus()) + " hatches!");
          }
          if ((Servers.isThisATestServer()) && (getData1() > 0)) {
            Players.getInstance().sendGmMessage(null, "System", "Debug: decayed a fertile egg at " + (int)this.posX / 4 + "," + (int)this.posY / 4 + ", Data1=" + getData1(), false);
          }
          hatch();
          if (getTemplateId() == 466)
          {
            i = TileRockBehaviour.createRandomGem();
            if (i != null)
            {
              owner.getInventory().insertItem(i, true);
              ownerComm.sendNormalServerMessage(
                LoginHandler.raiseFirstLetter(new StringBuilder().append("You find something in the ").append(getName()).toString()) + "!");
            }
          }
        }
        else if (!isFishingBait())
        {
          ownerComm.sendNormalServerMessage(
            LoginHandler.raiseFirstLetter(getNameWithGenus()) + " is useless and you throw it away.");
        }
      }
      catch (NoSuchCreatureException|NoSuchPlayerException localNoSuchCreatureException) {}
    }
    else
    {
      sendDecayMess();
      if (isEgg()) {
        hatch();
      }
      if (this.hatching)
      {
        Integer x;
        if (getTemplateId() == 805)
        {
          IslandAdder adder = new IslandAdder(Server.surfaceMesh, Server.rockMesh);
          
          Map<Integer, Set<Integer>> changes = adder.forceIsland(50, 50, getTileX() - 25, getTileY() - 25);
          if (changes != null) {
            for (Map.Entry<Integer, Set<Integer>> me : changes.entrySet())
            {
              x = (Integer)me.getKey();
              Set<Integer> set = (Set)me.getValue();
              for (Integer y : set) {
                Players.getInstance().sendChangedTile(x.intValue(), y.intValue(), true, true);
              }
            }
          }
        }
        else if (getTemplateId() == 1009)
        {
          TerraformingTask task = new TerraformingTask(0, (byte)0, this.creator, 2, 0, true);
          
          task.setCoordinates();
          task.setSXY(getTileX(), getTileY());
        }
      }
    }
    Items.destroyItem(this.id);
    
    return decayed;
  }
  
  private void sendDecayMess()
  {
    String msgSuff = "";
    int dist = 0;
    if (isEgg())
    {
      if (getData1() > 0)
      {
        msgSuff = " cracks open!";
        dist = 10;
      }
      else if (getTemplateId() == 466)
      {
        msgSuff = " cracks open! Something is inside!";
        dist = 5;
      }
    }
    else if (!isTemporary())
    {
      msgSuff = " crumbles to dust.";
      dist = 2;
    }
    if (msgSuff.isEmpty()) {
      return;
    }
    String fullMsgStr = LoginHandler.raiseFirstLetter(getNameWithGenus()) + msgSuff;
    if (this.watchers != null)
    {
      for (Creature watcher : this.watchers) {
        watcher.getCommunicator().sendNormalServerMessage(fullMsgStr);
      }
      return;
    }
    if (((this.parentId != -10L) && (WurmId.getType(this.parentId) == 6)) || (isRepairable())) {
      try
      {
        TilePos tilePos = getTilePos();
        Zone currentZone = Zones.getZone(tilePos, isOnSurface());
        
        Server.getInstance().broadCastMessage(fullMsgStr, tilePos.x, tilePos.y, currentZone.isOnSurface(), dist);
      }
      catch (NoSuchZoneException nsz)
      {
        logWarn(getName() + " id:" + this.id, nsz);
      }
    }
  }
  
  public final TilePos getTilePos()
  {
    return CoordUtils.WorldToTile(getPos2f());
  }
  
  public final int getTileX()
  {
    return CoordUtils.WorldToTile(getPosX());
  }
  
  public final int getTileY()
  {
    return CoordUtils.WorldToTile(getPosY());
  }
  
  public final boolean isCorpse()
  {
    return this.template.templateId == 272;
  }
  
  public final boolean isCrate()
  {
    return (this.template.templateId == 852) || (this.template.templateId == 851);
  }
  
  public final boolean isBarrelRack()
  {
    return (this.template.templateId == 1108) || (this.template.templateId == 1109) || (this.template.templateId == 1111) || (this.template.templateId == 1110);
  }
  
  public final boolean isCarpet()
  {
    return this.template.isCarpet();
  }
  
  public final void pollCoolingItems(Creature owner, long timeSinceLastCooled)
  {
    if (isHollow()) {
      if (this.items != null)
      {
        Item[] itarr = (Item[])this.items.toArray(new Item[this.items.size()]);
        for (Item anItarr : itarr) {
          if (!anItarr.deleted) {
            anItarr.pollCoolingItems(owner, timeSinceLastCooled);
          }
        }
      }
    }
    coolInventoryItem(timeSinceLastCooled);
  }
  
  public final boolean pollOwned(Creature owner)
  {
    boolean decayed = false;
    short oldTemperature = getTemperature();
    
    long maintenanceTimeDelta = WurmCalendar.currentTime - this.lastMaintained;
    if ((isFood()) || ((isAlwaysPoll()) && (!isFlag())) || (isCorpse()) || (isPlantedFlowerpot()) || 
      (getTemplateId() == 1276) || (isInTacklebox()))
    {
      if (this.hatching) {
        return pollHatching();
      }
      long decayt = this.template.getDecayTime();
      if (this.template.templateId == 386) {
        try
        {
          decayt = ItemTemplateFactory.getInstance().getTemplate(this.realTemplate).getDecayTime();
        }
        catch (NoSuchTemplateException nst)
        {
          logInfo("No template for " + getName() + ", id=" + this.realTemplate);
        }
      } else if (this.template.templateId == 339) {
        if (ArtifactBehaviour.getOrbActivation() > 0L) {
          if (System.currentTimeMillis() - ArtifactBehaviour.getOrbActivation() > 21000L) {
            if (WurmCalendar.currentTime - getData() < 360000L)
            {
              ArtifactBehaviour.resetOrbActivation();
              Server.getInstance().broadCastAction("A deadly field surges through the air from the location of " + owner
                .getName() + " and the " + 
                getName() + "!", owner, 25);
              ArtifactBehaviour.markOrbRecipients(owner, false, 0.0F, 0.0F, 0.0F);
            }
          }
        }
      }
      if (decayt == 28800L) {
        if (this.damage == 0.0F) {
          decayt = 1382400L + (28800.0F * Math.max(1.0F, this.qualityLevel / 3.0F));
        } else {
          decayt = (28800.0F * Math.max(1.0F, this.qualityLevel / 3.0F));
        }
      }
      float lunchboxMod = 0.0F;
      if (isInLunchbox())
      {
        Item lunchbox = getParentOuterItemOrNull();
        if ((lunchbox != null) && (lunchbox.getTemplateId() == 1296)) {
          lunchboxMod = 8.0F;
        } else if ((lunchbox != null) && (lunchbox.getTemplateId() == 1297)) {
          lunchboxMod = 9.0F;
        }
        decayt *= (getRarity() / 4 + 2);
      }
      if (isInTacklebox())
      {
        lunchboxMod = 7.0F;
        decayt *= (getRarity() / 4 + 2);
      }
      int adjDelta = (int)(maintenanceTimeDelta / decayt);
      int timesSinceLastUsed = isLight() ? Math.min(1, adjDelta) : adjDelta;
      if (timesSinceLastUsed > 0)
      {
        float decayMin = 0.5F;
        if ((isFood()) && (owner.getDeity() != null) && (owner.getDeity().isItemProtector()))
        {
          if (((owner.getFaith() >= 70.0F) && (owner.getFavor() >= 35.0F)) || (isCorpse()))
          {
            if (Server.rand.nextInt(5) == 0) {
              if (this.template.destroyOnDecay) {
                decayed = setDamage(this.damage + timesSinceLastUsed * Math.max(1.0F, 10.0F - lunchboxMod));
              } else {
                decayed = setDamage(this.damage + timesSinceLastUsed * 
                  Math.max(0.5F, getDamageModifier(true) - lunchboxMod));
              }
            }
          }
          else if (this.template.destroyOnDecay) {
            decayed = setDamage(this.damage + timesSinceLastUsed * Math.max(1.0F, 10.0F - lunchboxMod));
          } else {
            decayed = setDamage(this.damage + timesSinceLastUsed * Math.max(0.5F, getDamageModifier(true) - lunchboxMod));
          }
        }
        else
        {
          if (this.template.destroyOnDecay) {
            decayed = setDamage(this.damage + timesSinceLastUsed * Math.max(1.0F, 10.0F - lunchboxMod));
          } else {
            decayed = setDamage(this.damage + timesSinceLastUsed * Math.max(0.5F, getDamageModifier(true) - lunchboxMod));
          }
          if ((isPlantedFlowerpot()) && (decayed)) {
            try
            {
              int revertType = -1;
              if (isPotteryFlowerPot()) {
                revertType = 813;
              } else if (isMarblePlanter()) {
                revertType = 1001;
              } else {
                revertType = -1;
              }
              if (revertType != -1)
              {
                Item pot = ItemFactory.createItem(revertType, getQualityLevel(), this.creator);
                pot.setLastOwnerId(getLastOwnerId());
                pot.setDescription(getDescription());
                pot.setDamage(getDamage());
                owner.getInventory().insertItem(pot);
              }
            }
            catch (NoSuchTemplateException|FailedException e)
            {
              logWarn(e.getMessage(), e);
            }
          }
        }
        if ((!decayed) && (this.lastMaintained != WurmCalendar.currentTime)) {
          setLastMaintained(WurmCalendar.currentTime);
        }
      }
    }
    else
    {
      boolean decayQl;
      int adjDelta;
      if ((Features.Feature.SADDLEBAG_DECAY.isEnabled()) && (owner != null) && 
        (!owner.isPlayer()) && (!owner.isNpc())) {
        if (isInside(new int[] { 1333, 1334 }))
        {
          long decayt = this.template.getDecayTime();
          decayQl = false;
          if (decayt == 28800L)
          {
            if (this.damage == 0.0F) {
              decayt = 1382400L + (28800.0F * Math.max(1.0F, this.qualityLevel / 3.0F));
            } else {
              decayt = (28800.0F * Math.max(1.0F, this.qualityLevel / 3.0F));
            }
            decayQl = true;
          }
          adjDelta = (int)(maintenanceTimeDelta / decayt);
          int timesSinceLastUsed = isLight() ? Math.min(1, adjDelta) : adjDelta;
          if (timesSinceLastUsed > 0)
          {
            if ((decayQl) || (Server.rand.nextInt(6) == 0))
            {
              float decayMin = 0.2F;
              if (this.template.destroyOnDecay) {
                decayed = setDamage(this.damage + timesSinceLastUsed * 4.0F);
              } else {
                decayed = setDamage(this.damage + timesSinceLastUsed * (0.2F * getDamageModifier(true)));
              }
            }
            if ((!decayed) && (this.lastMaintained != WurmCalendar.currentTime)) {
              setLastMaintained(WurmCalendar.currentTime);
            }
          }
          break label1268;
        }
      }
      if (isHollow())
      {
        if (this.items != null)
        {
          Item[] itarr = (Item[])this.items.toArray(new Item[this.items.size()]);
          for (Item anItarr : itarr) {
            if (!anItarr.deleted) {
              anItarr.pollOwned(owner);
            }
          }
        }
      }
      else if ((this.template.templateId == 166) && (maintenanceTimeDelta > 2419200L)) {
        setLastMaintained(WurmCalendar.currentTime);
      }
    }
    try
    {
      label1268:
      Item parent = getParent();
      if (parent.isBodyPartAttached()) {
        ItemBonus.checkDepleteAndRename(this, owner);
      }
    }
    catch (NoSuchItemException localNoSuchItemException) {}
    if (decayed) {
      return true;
    }
    if (isCompass())
    {
      Item bestCompass = owner.getBestCompass();
      if ((bestCompass == null) || ((bestCompass != this) && 
        (bestCompass.getCurrentQualityLevel() < getCurrentQualityLevel()))) {
        owner.setBestCompass(this);
      }
    }
    if (getTemplateId() == 1341)
    {
      Item bestTackleBox = owner.getBestTackleBox();
      if ((bestTackleBox == null) || ((bestTackleBox != this) && 
        (bestTackleBox.getCurrentQualityLevel() < getCurrentQualityLevel()))) {
        owner.setBestTackleBox(this);
      }
    }
    if (isToolbelt()) {
      try
      {
        Item parent = getParent();
        if ((parent.getPlace() == 43) && (parent.isBodyPartAttached()))
        {
          Item bestBelt = owner.getBestToolbelt();
          if ((bestBelt == null) || ((bestBelt != this) && 
            (bestBelt.getCurrentQualityLevel() < getCurrentQualityLevel()))) {
            owner.setBestToolbelt(this);
          }
        }
      }
      catch (NoSuchItemException localNoSuchItemException1) {}
    }
    if ((getTemplateId() == 1243) && (getTemperature() >= 10000))
    {
      Item bestBeeSmoker = owner.getBestBeeSmoker();
      if ((bestBeeSmoker == null) || ((bestBeeSmoker != this) && 
        (bestBeeSmoker.getCurrentQualityLevel() < getCurrentQualityLevel()))) {
        owner.setBestBeeSmoker(this);
      }
    }
    coolInventoryItem();
    if ((isLight()) && (isOnFire()))
    {
      if (owner.getBestLightsource() != null)
      {
        if ((!owner.getBestLightsource().isLightBright()) && (isLightBright())) {
          owner.setBestLightsource(this, false);
        } else if ((owner.getBestLightsource() != this) && 
          (owner.getBestLightsource().getCurrentQualityLevel() < getCurrentQualityLevel())) {
          owner.setBestLightsource(this, false);
        }
      }
      else {
        owner.setBestLightsource(this, false);
      }
      decayed = pollLightSource();
    }
    else if (getTemplateId() == 1243)
    {
      decayed = pollLightSource();
    }
    if (getTemperatureState(oldTemperature) != getTemperatureState(this.temperature)) {
      notifyWatchersTempChange();
    }
    return decayed;
  }
  
  public final void attackEnemies(boolean watchTowerpoll)
  {
    if (!Servers.localServer.PVPSERVER) {
      return;
    }
    int tileX = getTileX();
    int tileY = getTileY();
    VolaTile t;
    VolaTile[] tiles;
    float mod;
    if (watchTowerpoll)
    {
      int dist = 10;
      int x1 = Zones.safeTileX(tileX - 10);
      int x2 = Zones.safeTileX(tileX + 10);
      int y1 = Zones.safeTileY(tileY - 10);
      int y2 = Zones.safeTileY(tileY + 10);
      for (TilePos tPos : TilePos.areaIterator(x1, y1, x2, y2))
      {
        int x = tPos.x;
        int y = tPos.y;
        t = Zones.getTileOrNull(x, y, true);
        if ((t != null) && 
        
          (getKingdom() == t.getKingdom())) {
          for (Creature c : t.getCreatures()) {
            if (c.getPower() <= 0) {
              if ((!c.isUnique()) && 
                (!c.isInvulnerable()) && 
                (c.getKingdomId() != 0) && 
                (c.getTemplate().isTowerBasher())) {
                if (!c.isFriendlyKingdom(getKingdom())) {
                  if (Server.rand.nextFloat() * 200.0F < getCurrentQualityLevel())
                  {
                    tiles = Zones.getTilesSurrounding(x, y, c.isOnSurface(), 5);
                    for (VolaTile tile : tiles) {
                      tile.broadCast("The " + getName() + " fires at " + c.getNameWithGenus() + ".");
                    }
                    mod = 1.0F / c.getArmourMod();
                    Arrows.shootCreature(this, c, (int)(mod * 10000.0F));
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
      boolean isArcheryTower = getTemplateId() == 939;
      if ((!isEnchantedTurret()) && (!isArcheryTower)) {
        return;
      }
      if ((getOwnerId() != -10L) || (getParentId() != -10L)) {
        return;
      }
      if ((isEnchantedTurret()) && (!isPlanted())) {
        return;
      }
      VolaTile ts = Zones.getTileOrNull(tileX, tileY, true);
      if (ts == null) {
        return;
      }
      if (WurmCalendar.getCurrentTime() - this.lastMaintained < 320.0D * (1.0D - getCurrentQualityLevel() / 200.0F)) {
        return;
      }
      this.lastMaintained = WurmCalendar.getCurrentTime();
      HashSet<Creature> targets = new HashSet();
      
      float distanceModifier = getCurrentQualityLevel() / 100.0F * 5.0F;
      int dist = (int)((isArcheryTower ? 5 : 3) * distanceModifier);
      int x1 = Zones.safeTileX(tileX - dist);
      int x2 = Zones.safeTileX(tileX + dist);
      int y1 = Zones.safeTileY(tileY - dist);
      int y2 = Zones.safeTileY(tileY + dist);
      for (TilePos tPos : TilePos.areaIterator(x1, y1, x2, y2))
      {
        int x = tPos.x;
        y = tPos.y;
        t = Zones.getTileOrNull(x, y, true);
        if ((t != null) && 
        
          (getKingdom() == t.getKingdom()) && 
          
          (Zones.getCurrentTurret(x, y, true) == this)) {
          for (Object c : t.getCreatures()) {
            if ((!((Creature)c).isUnique()) && 
              (!((Creature)c).isInvulnerable()) && 
              (((Creature)c).getKingdomId() != 0) && (
              (((Creature)c).isPlayer()) || (((Creature)c).getTemplate().isTowerBasher())))
            {
              Village v = Villages.getVillageWithPerimeterAt(tileX, tileY, true);
              if ((!((Creature)c).isFriendlyKingdom(getKingdom())) || ((Servers.localServer.PVPSERVER) && (v != null) && 
                (v.isEnemy((Creature)c)))) {
                if (((Creature)c).getCurrentTile() != null) {
                  targets.add(c);
                }
              }
            }
          }
        }
      }
      int y;
      VolaTile t;
      if (!targets.isEmpty())
      {
        Creature[] crets = (Creature[])targets.toArray(new Creature[targets.size()]);
        Creature c = crets[Server.rand.nextInt(crets.length)];
        if (Server.rand.nextFloat() * 200.0F < getCurrentQualityLevel())
        {
          BlockingResult result = Blocking.getBlockerBetween(null, getPosX(), getPosY(), c.getPosX(), c.getPosY(), 
            getPosZ() + getTemplate().getSizeY() * 0.85F / 100.0F - 0.5F, c
            .getPositionZ() + c.getCentimetersHigh() * 0.75F / 100.0F - 0.5F, 
            isOnSurface(), c.isOnSurface(), true, 4, c
            .getWurmId(), getBridgeId(), c.getBridgeId(), false);
          if (result != null)
          {
            Blocker[] arrayOfBlocker = result.getBlockerArray();t = arrayOfBlocker.length;
            for (tiles = 0; tiles < t; tiles++)
            {
              Blocker b = arrayOfBlocker[tiles];
              if (b.getBlockPercent(c) >= 100.0F) {
                return;
              }
            }
            if (result.getTotalCover() > 0.0F) {
              return;
            }
          }
          float mod = 1.0F + c.getArmourMod();
          float distToCret = 1.0F - c.getPos3f().distance(getPos3f()) / 150.0F;
          float enchDamMod = getCurrentQualityLevel() * distToCret;
          if (isEnchantedTurret())
          {
            enchDamMod = getSpellCourierBonus();
            if (enchDamMod == 0.0F)
            {
              logInfo("Reverted turret at " + tileX + "," + getTileY());
              setTemplateId(934);
            }
          }
          Arrows.shootCreature(this, c, (int)(mod * 75.0F * enchDamMod));
        }
        else if (isEnchantedTurret())
        {
          VolaTile t = Zones.getTileOrNull(getTilePos(), isOnSurface());
          if (t != null) {
            t.sendAnimation(c, this, "shoot", false, false);
          }
        }
      }
    }
  }
  
  private boolean poll(Item parent, int parentTemp, boolean insideStructure, boolean deeded, boolean saveLastMaintained, boolean inMagicContainer, boolean inTrashbin)
  {
    if (this.hatching) {
      return pollHatching();
    }
    boolean decayed = false;
    if (Features.Feature.TRANSPORTABLE_CREATURES.isEnabled())
    {
      long delay = System.currentTimeMillis() - 900000L;
      if (delay > parent.getData()) {
        if ((getTemplateId() == 1310) && (parent.getTemplateId() == 1311)) {
          pollCreatureCages(parent);
        }
      }
    }
    if ((Features.Feature.FREE_ITEMS.isEnabled()) && 
      (isChallengeNewbieItem()) && (
      (isArmour()) || (isWeapon()) || (isShield()))) {
      if (this.ownerId == -10L)
      {
        Items.destroyItem(getWurmId());
        return true;
      }
    }
    if ((isHollow()) && (isSealedByPlayer()))
    {
      if (getTemplateId() == 768)
      {
        pollAging(insideStructure, deeded);
        if (Server.rand.nextInt(20) == 0) {
          pollFermenting();
        }
      }
      return false;
    }
    if ((getTemplateId() == 70) || (getTemplateId() == 1254))
    {
      modTemp(parent, parentTemp, insideStructure);
      return false;
    }
    Item honey;
    long decayt;
    int timesSinceLastUsed;
    if (this.template.getDecayTime() != Long.MAX_VALUE)
    {
      boolean decaytimeql = false;
      if ((isFood()) || (isHollow()) || ((isAlwaysPoll()) && (!isFlag())))
      {
        if (this.template.templateId == 339) {
          if (ArtifactBehaviour.getOrbActivation() > 0L) {
            if (System.currentTimeMillis() - ArtifactBehaviour.getOrbActivation() > 21000L) {
              if (WurmCalendar.currentTime - getData() < 360000L)
              {
                ArtifactBehaviour.resetOrbActivation();
                Server.getInstance().broadCastMessage("A deadly field surges through the air from the location of the " + 
                  getName() + "!", 
                  getTileX(), getTileY(), isOnSurface(), 25);
                
                ArtifactBehaviour.markOrbRecipients(null, false, getPosX(), getPosY(), getPosZ());
              }
            }
          }
        }
        Item sugar;
        if ((this.template.getTemplateId() == 1175) && (parent.isVehicle()) && (hasQueen()) && (WurmCalendar.currentTime - this.lastMaintained > 604800L)) {
          if (hasTwoQueens())
          {
            if (removeQueen()) {
              if (Servers.isThisATestServer()) {
                Players.getInstance().sendGmMessage(null, "System", "Debug: Removed second queen from " + getWurmId() + " as travelling.", false);
              } else {
                logger.info("Removed second queen from " + getWurmId() + " as travelling.");
              }
            }
          }
          else
          {
            sugar = getSugar();
            if (sugar != null)
            {
              Items.destroyItem(sugar.getWurmId());
            }
            else
            {
              honey = getHoney();
              if (honey != null) {
                honey.setWeight(Math.max(0, honey.getWeightGrams() - 10), true);
              } else if (Server.rand.nextInt(3) == 0) {
                if (removeQueen()) {
                  if (Servers.isThisATestServer()) {
                    Players.getInstance().sendGmMessage(null, "System", "Debug: Removed queen from " + getWurmId() + " as travelling and No Honey!", false);
                  } else {
                    logger.info("Removed queen from " + getWurmId() + " as travelling and No Honey!");
                  }
                }
              }
            }
          }
        }
        if (isHollow())
        {
          Item i;
          if ((deeded) && (isCrate()) && (parent.getTemplateId() == 1312))
          {
            setLastMaintained(WurmCalendar.currentTime);
            
            sugar = getAllItems(true);honey = sugar.length;
            for (Item localItem1 = 0; localItem1 < honey; localItem1++)
            {
              i = sugar[localItem1];
              if (i.isBulkItem()) {
                i.setLastMaintained(WurmCalendar.currentTime);
              }
            }
            return false;
          }
          if ((deeded) && (getTemplateId() == 662) && (parent.getTemplateId() == 1315))
          {
            setLastMaintained(WurmCalendar.currentTime);
            return false;
          }
          if (this.items != null)
          {
            Item[] itarr = (Item[])this.items.toArray(new Item[this.items.size()]);
            honey = itarr;Item localItem2 = honey.length;
            for (i = 0; i < localItem2; i++)
            {
              Item item = honey[i];
              if (!item.deleted) {
                item.poll(this, getTemperature(), insideStructure, deeded, saveLastMaintained, (inMagicContainer) || 
                  (isMagicContainer()), false);
              }
            }
          }
        }
        long decayTime = 1382400L;
        if ((WurmCalendar.currentTime > this.creationDate + 1382400L) || (inTrashbin) || (this.template.getDecayTime() < 3600L))
        {
          long decayt = this.template.getDecayTime();
          if (this.template.templateId == 386) {
            try
            {
              decayt = ItemTemplateFactory.getInstance().getTemplate(this.realTemplate).getDecayTime();
            }
            catch (NoSuchTemplateException nst)
            {
              logInfo("No template for " + getName() + ", id=" + this.realTemplate);
            }
          }
          if (decayt == 28800L)
          {
            if (this.damage == 0.0F) {
              decayt = 1382400L + (28800.0F * Math.max(1.0F, this.qualityLevel / 3.0F));
            } else {
              decayt = (28800.0F * Math.max(1.0F, this.qualityLevel / 3.0F));
            }
            decaytimeql = true;
          }
          if (inTrashbin) {
            if ((!isHollow()) || (!isLocked())) {
              decayt = Math.min(decayt, 28800L);
            }
          }
          int timesSinceLastUsed = (int)((WurmCalendar.currentTime - this.lastMaintained) / decayt);
          if (timesSinceLastUsed > 0)
          {
            if (inTrashbin) {
              if ((!isHollow()) || (!isLocked()))
              {
                if (getDamage() > 0.0F)
                {
                  Items.destroyItem(getWurmId());
                  return true;
                }
                return setDamage(getDamage() + 0.1F);
              }
            }
            int num = 2;
            float decayMin = 0.5F;
            if (isFood()) {
              decayMin = 1.0F;
            }
            if ((!isBulk()) && (this.template.templateId != 74)) {
              if (!isLight())
              {
                if (insideStructure) {
                  num = 10;
                }
                if (deeded) {
                  num += 4;
                }
              }
            }
            boolean decay = true;
            
            float dm = getDecayMultiplier();
            if (dm > 1.0F)
            {
              this.ticksSinceLastDecay += timesSinceLastUsed;
              timesSinceLastUsed = (int)(this.ticksSinceLastDecay / dm);
              if (timesSinceLastUsed > 0)
              {
                this.ticksSinceLastDecay -= (int)(timesSinceLastUsed * dm);
              }
              else
              {
                decay = false;
                setLastMaintained(WurmCalendar.currentTime);
              }
            }
            if (decay) {
              if ((decaytimeql) || (isBulkItem()) || (Server.rand.nextInt(num) == 0)) {
                if (this.template.positiveDecay)
                {
                  if (getTemplateId() == 738)
                  {
                    setQualityLevel(Math.min(100.0F, this.qualityLevel + (100.0F - this.qualityLevel) * (100.0F - this.qualityLevel) / 10000.0F));
                    
                    checkGnome();
                  }
                }
                else if ((isMagicContainer()) || (!inMagicContainer) || (
                  ((isLight()) || (isFireplace())) && (isOnFire())))
                {
                  if (((isLight()) || (isFireplace())) && (isOnFire())) {
                    pollLightSource();
                  }
                  if (this.template.destroyOnDecay) {
                    decayed = setDamage(this.damage + timesSinceLastUsed * 10);
                  } else if ((isBulkItem()) && (getBulkNums() > 0)) {
                    try
                    {
                      ItemTemplate t = ItemTemplateFactory.getInstance().getTemplate(
                        getRealTemplateId());
                      if (getWeightGrams() < t.getVolume())
                      {
                        Items.destroyItem(getWurmId());
                        decayed = true;
                      }
                      else
                      {
                        float mod = 0.05F;
                        
                        decayed = setWeight((int)(getWeightGrams() - getWeightGrams() * timesSinceLastUsed * 0.05F), true);
                      }
                    }
                    catch (NoSuchTemplateException nst)
                    {
                      Items.destroyItem(getWurmId());
                      decayed = true;
                    }
                  } else {
                    decayed = setDamage(this.damage + timesSinceLastUsed * 
                      Math.max(decayMin, getDamageModifier(true)));
                  }
                }
              }
            }
            if ((!decayed) && (this.lastMaintained != WurmCalendar.currentTime)) {
              setLastMaintained(WurmCalendar.currentTime);
            }
          }
        }
      }
      else if (getTemplateId() == 1162)
      {
        if (WurmCalendar.currentTime - this.lastMaintained > 604800L) {
          advancePlanterWeek();
        }
      }
      else if ((WurmCalendar.currentTime - this.creationDate > 1382400L) || (inTrashbin) || (this.template.getDecayTime() < 3600L))
      {
        decayt = this.template.getDecayTime();
        if (this.template.templateId == 386) {
          try
          {
            decayt = ItemTemplateFactory.getInstance().getTemplate(this.realTemplate).getDecayTime();
          }
          catch (NoSuchTemplateException nst)
          {
            logInfo("No template for " + getName() + ", id=" + this.realTemplate);
          }
        }
        if (decayt == 28800L)
        {
          if (this.damage == 0.0F) {
            decayt = 1382400L + (28800.0F * Math.max(1.0F, this.qualityLevel / 3.0F));
          } else {
            decayt = (28800.0F * Math.max(1.0F, this.qualityLevel / 3.0F));
          }
          decaytimeql = true;
        }
        if (inTrashbin) {
          if ((!isHollow()) || (!isLocked())) {
            decayt = Math.min(decayt, 28800L);
          }
        }
        timesSinceLastUsed = (int)((WurmCalendar.currentTime - this.lastMaintained) / decayt);
        if (timesSinceLastUsed > 0)
        {
          if (inTrashbin) {
            if ((!isHollow()) || (!isLocked()))
            {
              if (getDamage() > 0.0F)
              {
                Items.destroyItem(getWurmId());
                return true;
              }
              return setDamage(getDamage() + 0.1F);
            }
          }
          int num = 2;
          if ((!isBulk()) && (this.template.templateId != 74))
          {
            if ((insideStructure) && (!this.template.positiveDecay)) {
              num = 10;
            }
            if (deeded) {
              num += 4;
            }
          }
          boolean decay = true;
          if (getDecayMultiplier() > 1.0F)
          {
            this.ticksSinceLastDecay += timesSinceLastUsed;
            timesSinceLastUsed = (int)(this.ticksSinceLastDecay / getDecayMultiplier());
            if (timesSinceLastUsed > 0)
            {
              this.ticksSinceLastDecay -= (int)(timesSinceLastUsed * getDecayMultiplier());
            }
            else
            {
              decay = false;
              setLastMaintained(WurmCalendar.currentTime);
            }
          }
          if (decay) {
            if ((decaytimeql) || (isBulkItem()) || (Server.rand.nextInt(num) == 0)) {
              if ((this.template.positiveDecay) && (!inTrashbin))
              {
                if (getTemplateId() == 738)
                {
                  setQualityLevel(Math.min(100.0F, this.qualityLevel + (100.0F - this.qualityLevel) * (100.0F - this.qualityLevel) / 10000.0F));
                  checkGnome();
                }
              }
              else if ((isMagicContainer()) || (!inMagicContainer)) {
                if (this.template.destroyOnDecay) {
                  decayed = setDamage(this.damage + timesSinceLastUsed * 10);
                } else if ((isBulkItem()) && (getBulkNums() > 0)) {
                  try
                  {
                    ItemTemplate t = ItemTemplateFactory.getInstance().getTemplate(
                      getRealTemplateId());
                    if (getWeightGrams() < t.getVolume())
                    {
                      Items.destroyItem(getWurmId());
                      decayed = true;
                    }
                    else
                    {
                      VolaTile tile = Zones.getOrCreateTile(getTileX(), getTileY(), true);
                      float mod;
                      if (tile.getVillage() != null)
                      {
                        float mod = 0.0F;
                        
                        decay = false;
                        setLastMaintained(WurmCalendar.currentTime);
                      }
                      else
                      {
                        mod = 0.05F;
                      }
                      decayed = setWeight((int)(getWeightGrams() - getWeightGrams() * timesSinceLastUsed * mod), true);
                    }
                  }
                  catch (NoSuchTemplateException nst)
                  {
                    Items.destroyItem(getWurmId());
                    decayed = true;
                  }
                } else {
                  decayed = setDamage(this.damage + timesSinceLastUsed * Math.max(1.0F, getDamageModifier(true)));
                }
              }
            }
          }
          if ((!decayed) && (this.lastMaintained != WurmCalendar.currentTime)) {
            setLastMaintained(WurmCalendar.currentTime);
          }
        }
      }
    }
    else
    {
      if ((saveLastMaintained) && (this.lastMaintained - WurmCalendar.currentTime > 1209600L) && 
        (!isRiftLoot())) {
        setLastMaintained(WurmCalendar.currentTime);
      }
      if (isHollow()) {
        if (this.items != null)
        {
          Item[] itarr = (Item[])this.items.toArray(new Item[this.items.size()]);
          for (Item item : itarr) {
            if (!item.deleted) {
              item.poll(this, getTemperature(), insideStructure, deeded, saveLastMaintained, (inMagicContainer) || 
                (isMagicContainer()), false);
            }
          }
        }
      }
    }
    if (Features.Feature.CHICKEN_COOPS.isEnabled()) {
      ChickenCoops.poll(this);
    }
    if (!decayed) {
      modTemp(parent, parentTemp, insideStructure);
    }
    return decayed;
  }
  
  private void pollCreatureCages(Item parent)
  {
    for (Creature creature : Creatures.getInstance().getCreatures()) {
      if (creature.getWurmId() == getData())
      {
        parent.setDamage(parent.damage + 10.0F / parent.getCurrentQualityLevel());
        parent.setData(System.currentTimeMillis());
        if (parent.getDamage() >= 80.0F) {
          try
          {
            int layer;
            int layer;
            if (isOnSurface()) {
              layer = 0;
            } else {
              layer = -1;
            }
            parent.setName("creature cage [Empty]");
            Creature getCreature = Creatures.getInstance().getCreature(getData());
            Creatures cstat = Creatures.getInstance();
            getCreature.getStatus().setDead(false);
            cstat.removeCreature(getCreature);
            cstat.addCreature(getCreature, false);
            getCreature.putInWorld();
            CreatureBehaviour.blinkTo(getCreature, getPosX(), getPosY(), layer, getPosZ(), getBridgeId(), getFloorLevel());
            parent.setAuxData((byte)0);
            Items.destroyItem(getWurmId());
            CargoTransportationMethods.updateItemModel(parent);
            
            DbCreatureStatus.setLoaded(0, getCreature.getWurmId());
          }
          catch (NoSuchCreatureException|IOException ex)
          {
            ex.printStackTrace();
          }
        }
      }
    }
  }
  
  public final void ageTrellis()
  {
    if (System.currentTimeMillis() - this.lastPolled < 86400000L) {
      return;
    }
    this.lastPolled = System.currentTimeMillis();
    
    int age = getLeftAuxData();
    if (age != 15)
    {
      int chance = Server.rand.nextInt(225);
      if ((chance <= (16 - age) * (16 - age)) || (!isPlanted())) {
        if (Server.rand.nextInt(5) == 0)
        {
          age++;
          if (chance > 8) {
            if (WurmCalendar.isNight()) {
              SoundPlayer.playSound("sound.birdsong.owl.short", getTileX(), getTileY(), true, 4.0F);
            } else {
              SoundPlayer.playSound("sound.ambient.day.crickets", getTileX(), getTileY(), true, 0.0F);
            }
          }
          setLeftAuxData(age);
          
          updateName();
        }
      }
    }
    else
    {
      int chance = Server.rand.nextInt(15);
      if (chance == 1)
      {
        setLeftAuxData(0);
        
        updateName();
      }
    }
  }
  
  public final boolean poll(boolean insideStructure, boolean deeded, long seed)
  {
    boolean decayed = false;
    int templateId = -1;
    templateId = getTemplateId();
    if ((Features.Feature.FREE_ITEMS.isEnabled()) && 
      (isChallengeNewbieItem()) && (
      (isArmour()) || (isWeapon()) || (isShield()))) {
      if (this.ownerId == -10L)
      {
        Items.destroyItem(getWurmId());
        return true;
      }
    }
    if ((templateId == 339) && 
      (ArtifactBehaviour.getOrbActivation() > 0L) && 
      (System.currentTimeMillis() - ArtifactBehaviour.getOrbActivation() > 21000L)) {
      if (WurmCalendar.currentTime - getData() < 360000L)
      {
        ArtifactBehaviour.resetOrbActivation();
        Server.getInstance().broadCastMessage("A deadly field surges through the air from the location of the " + 
          getName() + "!", 
          getTileX(), getTileY(), isOnSurface(), 25);
        ArtifactBehaviour.markOrbRecipients(null, false, getPosX(), getPosY(), getPosZ());
      }
    }
    if (this.hatching) {
      return pollHatching();
    }
    if (getTemplateId() == 1437) {
      if (WurmCalendar.getCurrentTime() - this.lastMaintained > 604800L)
      {
        addSnowmanItem();
        setLastMaintained(WurmCalendar.getCurrentTime());
      }
    }
    if ((isHollow()) && (isSealedByPlayer()))
    {
      if (templateId == 768)
      {
        pollAging(insideStructure, deeded);
        if (Server.rand.nextInt(20) == 0) {
          pollFermenting();
        }
      }
      return false;
    }
    float decayMin;
    int timesSinceLastUsed;
    boolean decay;
    Item pot;
    VolaTile tile;
    if (this.template.getDecayTime() != Long.MAX_VALUE)
    {
      if ((isHollow()) || (isFood()) || (isAlwaysPoll()))
      {
        long decayt = this.template.getDecayTime();
        if (templateId == 386) {
          try
          {
            decayt = ItemTemplateFactory.getInstance().getTemplate(this.realTemplate).getDecayTime();
          }
          catch (NoSuchTemplateException nst)
          {
            logInfo("No template for " + getName() + ", id=" + this.realTemplate);
          }
        }
        if (decayt == 28800L) {
          if (this.damage == 0.0F) {
            decayt = 1382400L + (28800.0F * Math.max(1.0F, this.qualityLevel / 3.0F));
          } else {
            decayt = (28800.0F * Math.max(1.0F, this.qualityLevel / 3.0F));
          }
        }
        if ((deeded) && (isDecoration()) && (templateId != 74)) {
          decayt *= 3L;
        }
        int timesSinceLastUsed = (int)((WurmCalendar.currentTime - this.lastMaintained) / decayt);
        int destroyed;
        if ((isHollow()) && (!isSealedByPlayer()))
        {
          boolean lastm = seed == 1L;
          if (this.items != null)
          {
            Item[] pollItems1 = (Item[])this.items.toArray(new Item[this.items.size()]);
            destroyed = 0;
            boolean trashBin = templateId == 670;
            long lasto = 0L;
            for (Item pollItem : pollItems1) {
              if (!pollItem.deleted) {
                if (!trashBin)
                {
                  pollItem.poll(this, getTemperature(), insideStructure, deeded, lastm, 
                    isMagicContainer(), false);
                }
                else
                {
                  if ((lasto > 0L) && (lasto != pollItem.getLastOwnerId())) {
                    if (destroyed > 0) {
                      try
                      {
                        Creature lLastOwner = Server.getInstance().getCreature(lasto);
                        lLastOwner.achievement(160, destroyed);
                        destroyed = 0;
                      }
                      catch (NoSuchCreatureException|NoSuchPlayerException nsc)
                      {
                        Achievements.triggerAchievement(lasto, 160, destroyed);
                        destroyed = 0;
                      }
                    }
                  }
                  lasto = pollItem.getLastOwnerId();
                  if (pollItem.isHollow()) {
                    for (Item it : pollItem.getItemsAsArray()) {
                      if (it.poll(pollItem, getTemperature(), insideStructure, deeded, lastm, 
                        isMagicContainer(), true)) {
                        destroyed++;
                      }
                    }
                  }
                  if (pollItem.poll(this, getTemperature(), insideStructure, deeded, lastm, 
                    isMagicContainer(), true)) {
                    destroyed++;
                  }
                  if (destroyed >= 100) {
                    break;
                  }
                }
              }
            }
            if (destroyed > 0) {
              if (lasto > 0L) {
                try
                {
                  Creature lastoner = Server.getInstance().getCreature(lasto);
                  lastoner.achievement(160, destroyed);
                  destroyed = 0;
                }
                catch (NoSuchCreatureException|NoSuchPlayerException nsc)
                {
                  Achievements.triggerAchievement(lasto, 160, destroyed);
                  destroyed = 0;
                }
              }
            }
          }
          else if (isCorpse())
          {
            if ((getData1() == 67) || 
              (getData1() == 36) || 
              (getData1() == 35) || 
              (getData1() == 34)) {
              decayed = setDamage(100.0F);
            } else if (Servers.localServer.isChallengeServer()) {
              if (getData1() != 1) {
                if (WurmCalendar.currentTime - this.creationDate > 28800L) {
                  decayed = setDamage(100.0F);
                }
              }
            }
          }
          checkDrift();
        }
        attackEnemies(false);
        if (isSpringFilled()) {
          if (isSourceSpring())
          {
            if (Server.rand.nextInt(100) == 0)
            {
              int volAvail = getFreeVolume();
              if (volAvail > 0)
              {
                Item liquid = null;
                for (Item next : getItems()) {
                  if (next.isLiquid()) {
                    liquid = next;
                  }
                }
                if (liquid != null)
                {
                  if (liquid.getTemplateId() == 763) {
                    liquid.setWeight(liquid.getWeightGrams() + 10, true);
                  }
                }
                else {
                  try
                  {
                    Random r = new Random(getWurmId());
                    Item source = ItemFactory.createItem(763, 80.0F + r.nextFloat() * 20.0F, "");
                    
                    insertItem(source, true);
                  }
                  catch (NoSuchTemplateException nst)
                  {
                    logInfo(nst.getMessage(), nst);
                  }
                  catch (FailedException fe)
                  {
                    logInfo(fe.getMessage(), fe);
                  }
                }
              }
            }
          }
          else if ((Zone.hasSpring(getTileX(), getTileY())) || (isAutoFilled())) {
            MethodsItems.fillContainer(this, null, false);
          }
        }
        if ((timesSinceLastUsed > 0) && (!decayed) && (!hasNoDecay()))
        {
          if (templateId == 74) {
            if (isOnFire())
            {
              for (int i = 0; i < timesSinceLastUsed; i++)
              {
                createDaleItems();
                decayed = setDamage(this.damage + 1.0F * getDamageModifier(true));
                if (decayed) {
                  break;
                }
              }
              if ((!decayed) && (this.lastMaintained != WurmCalendar.currentTime)) {
                setLastMaintained(WurmCalendar.currentTime);
              }
              return decayed;
            }
          }
          if ((templateId != 37) || (getTemperature() <= 200)) {
            if ((WurmCalendar.currentTime > this.creationDate + 1382400L) || 
              (isAlwaysPoll()) || 
              (this.template.getDecayTime() < 3600L) || (
              (Servers.localServer.isChallengeOrEpicServer()) && (this.template.destroyOnDecay)))
            {
              float decayMin = 0.5F;
              
              boolean decay = true;
              if ((deeded) && (getTemplateId() == 1311) && (isEmpty(true))) {
                decay = false;
              }
              if ((!Servers.isThisAPvpServer()) && (deeded) && (isEnchantedTurret())) {
                decay = false;
              }
              if ((isSign()) || (isStreetLamp()) || (isFlag()) || (isDecoration())) {
                if ((isPlanted()) || ((isDecoration()) && (!this.template.decayOnDeed()))) {
                  if ((deeded) && ((!isAlwaysPoll()) || (isFlag()) || (this.template.isCooker()))) {
                    decay = false;
                  }
                }
              }
              float dm = getDecayMultiplier();
              if (dm > 1.0F)
              {
                this.ticksSinceLastDecay += timesSinceLastUsed;
                timesSinceLastUsed = (int)(this.ticksSinceLastDecay / dm);
                if (timesSinceLastUsed > 0)
                {
                  this.ticksSinceLastDecay -= (int)(timesSinceLastUsed * dm);
                }
                else
                {
                  decay = false;
                  setLastMaintained(WurmCalendar.currentTime);
                }
              }
              if (decay)
              {
                if (insideStructure)
                {
                  if (isFood()) {
                    decayMin = 1.0F;
                  }
                  if (this.template.destroyOnDecay) {
                    decayed = setDamage(this.damage + timesSinceLastUsed * 10);
                  } else if (Server.rand.nextInt(deeded ? 12 : 8) == 0) {
                    decayed = setDamage(this.damage + timesSinceLastUsed * 
                      Math.max(decayMin, getDamageModifier(true)));
                  }
                }
                else
                {
                  if (isFood()) {
                    decayMin = 2.0F;
                  }
                  if (this.template.destroyOnDecay)
                  {
                    if (Servers.localServer.isChallengeServer()) {
                      decayed = setDamage(100.0F);
                    } else {
                      decayed = setDamage(this.damage + timesSinceLastUsed * 10);
                    }
                  }
                  else {
                    decayed = setDamage(this.damage + timesSinceLastUsed * 
                      Math.max(decayMin, getDamageModifier(true)));
                  }
                }
              }
              else {
                this.lastMaintained = (WurmCalendar.currentTime + Server.rand.nextInt(10) == 0L ? 1L : 0L);
              }
              if ((!decayed) && (this.lastMaintained != WurmCalendar.currentTime) && 
                (!isRiftLoot())) {
                setLastMaintained(WurmCalendar.currentTime);
              }
            }
          }
        }
      }
      else if (!hasNoDecay())
      {
        if (getTemplateId() == 1162)
        {
          if (WurmCalendar.currentTime - this.lastMaintained > 604800L) {
            advancePlanterWeek();
          }
        }
        else if ((WurmCalendar.currentTime > this.creationDate + 1382400L) || (this.template.getDecayTime() < 3600L))
        {
          templateId = getTemplateId();
          long decayt = this.template.getDecayTime();
          if (templateId == 386) {
            try
            {
              decayt = ItemTemplateFactory.getInstance().getTemplate(this.realTemplate).getDecayTime();
            }
            catch (NoSuchTemplateException nst)
            {
              logInfo("No template for " + getName() + ", id=" + this.realTemplate);
            }
          }
          decayMin = 0.5F;
          if (decayt == 28800L)
          {
            if (this.damage == 0.0F) {
              decayt = 1382400L + (28800.0F * Math.max(1.0F, this.qualityLevel / 3.0F));
            } else {
              decayt = (28800.0F * Math.max(1.0F, this.qualityLevel / 3.0F));
            }
            decayMin = 1.0F;
          }
          if (!isBulk())
          {
            if (deeded) {
              decayt *= 2L;
            }
            if (insideStructure) {
              decayt *= 2L;
            }
            if ((isRoadMarker()) && (!deeded) && (MethodsHighways.numberOfSetBits(getAuxData()) < 2)) {
              decayt = Math.max(1L, decayt / 10L);
            }
          }
          timesSinceLastUsed = (int)((WurmCalendar.currentTime - this.lastMaintained) / decayt);
          if (timesSinceLastUsed > 0)
          {
            decay = true;
            if (isRoadMarker())
            {
              if (isPlanted()) {
                if ((deeded) || (MethodsHighways.numberOfSetBits(getAuxData()) >= 2))
                {
                  decay = false;
                  setLastMaintained(WurmCalendar.currentTime);
                }
              }
            }
            else if ((isSign()) || (isStreetLamp()) || (isFlag()) || (isDecoration())) {
              if ((isPlanted()) || ((isDecoration()) && (!this.template.decayOnDeed()))) {
                if ((deeded) && ((!isAlwaysPoll()) || (isFlag()) || (getTemplateId() == 1178))) {
                  decay = false;
                } else if ((isStreetLamp()) && (getBless() != null)) {
                  if (MethodsHighways.onHighway(this)) {
                    decay = false;
                  }
                }
              }
            }
            float dm = getDecayMultiplier();
            if (dm > 1.0F)
            {
              this.ticksSinceLastDecay += timesSinceLastUsed;
              timesSinceLastUsed = (int)(this.ticksSinceLastDecay / dm);
              if (timesSinceLastUsed > 0)
              {
                this.ticksSinceLastDecay -= (int)(timesSinceLastUsed * dm);
              }
              else
              {
                decay = false;
                setLastMaintained(WurmCalendar.currentTime);
              }
            }
            if (decay) {
              if (insideStructure)
              {
                if (this.template.destroyOnDecay) {
                  decayed = setDamage(this.damage + timesSinceLastUsed * 40);
                } else if (Server.rand.nextInt(deeded ? 12 : 8) == 0) {
                  if (this.template.positiveDecay)
                  {
                    if (getTemplateId() == 738)
                    {
                      setQualityLevel(Math.min(100.0F, this.qualityLevel + (100.0F - this.qualityLevel) * (100.0F - this.qualityLevel) / 10000.0F));
                      
                      checkGnome();
                    }
                  }
                  else {
                    decayed = setDamage(this.damage + timesSinceLastUsed * 
                      Math.max(decayMin, getDamageModifier(true)));
                  }
                }
              }
              else if (this.template.destroyOnDecay) {
                decayed = setDamage(this.damage + timesSinceLastUsed * 10);
              } else {
                decayed = setDamage(this.damage + timesSinceLastUsed * 
                  Math.max(decayMin, getDamageModifier(true)));
              }
            }
            if ((isPlantedFlowerpot()) && (decayed)) {
              try
              {
                int revertType = -1;
                if (isPotteryFlowerPot()) {
                  revertType = 813;
                } else if (isMarblePlanter()) {
                  revertType = 1001;
                } else {
                  revertType = -1;
                }
                if (revertType != -1)
                {
                  pot = ItemFactory.createItem(revertType, getQualityLevel(), this.creator);
                  pot.setLastOwnerId(getLastOwnerId());
                  pot.setDescription(getDescription());
                  pot.setDamage(getDamage());
                  pot.setPosXYZ(getPosX(), getPosY(), getPosZ());
                  tile = Zones.getTileOrNull(getTileX(), getTileY(), isOnSurface());
                  if (tile != null) {
                    tile.addItem(pot, false, false);
                  }
                }
              }
              catch (NoSuchTemplateException|FailedException e)
              {
                logWarn(e.getMessage(), e);
              }
            }
            if ((!decayed) && (this.lastMaintained != WurmCalendar.currentTime)) {
              setLastMaintained(WurmCalendar.currentTime);
            }
          }
        }
      }
    }
    else if (this.template.hugeAltar)
    {
      if (isHollow())
      {
        boolean lastm = true;
        if (this.items != null)
        {
          Item[] itarr = (Item[])this.items.toArray(new Item[this.items.size()]);
          decayMin = itarr;timesSinceLastUsed = decayMin.length;
          for (decay = false; decay < timesSinceLastUsed; decay++)
          {
            Item it = decayMin[decay];
            if (!it.deleted) {
              it.poll(this, getTemperature(), insideStructure, deeded, true, true, false);
            }
          }
        }
      }
      pollHugeAltar();
    }
    else if (templateId == 521)
    {
      if (!isOnSurface())
      {
        setDamage(getDamage() + 0.1F);
        logInfo(getName() + " at " + getTilePos() + " on cave tile. Dealing damage.");
      }
      else
      {
        VolaTile t = Zones.getTileOrNull(getTileX(), getTileY(), isOnSurface());
        if (t != null)
        {
          if (t.isTransition)
          {
            setDamage(getDamage() + 0.1F);
            logInfo(getName() + " at " + getTilePos() + " on surface transition tile. Dealing damage.");
          }
        }
        else {
          logWarn(
            getName() + " at " + getTilePos() + " no tile on surface. Zone no. is " + getZoneId());
        }
      }
    }
    else if (templateId == 236)
    {
      checkItemSpawn();
    }
    if (templateId != 74) {
      coolOutSideItem(isAlwaysPoll(), insideStructure);
    }
    if (templateId == 445) {
      if (getData() > 0L) {
        try
        {
          Item contained = Items.getItem(getData());
          if (contained.poll(insideStructure, deeded, seed)) {
            setData(0L);
          }
        }
        catch (NoSuchItemException localNoSuchItemException) {}
      }
    }
    boolean spawn;
    if (spawnsTrees()) {
      for (int n = 0; n < 10; n++)
      {
        int x = Zones.safeTileX(getTileX() - 18 + Server.rand.nextInt(36));
        int y = Zones.safeTileY(getTileY() - 18 + Server.rand.nextInt(36));
        spawn = true;
        int meshTile;
        for (int xx = x - 1; xx <= x + 1; xx++) {
          for (int yy = y - 1; yy <= y + 1; yy++)
          {
            meshTile = Server.surfaceMesh.getTile(Zones.safeTileX(xx), Zones.safeTileY(yy));
            if (Tiles.getTile(Tiles.decodeType(meshTile)).isNormalTree())
            {
              spawn = false;
              break;
            }
          }
        }
        VolaTile t = Zones.getTileOrNull(x, y, isOnSurface());
        if (t != null)
        {
          Item[] its = t.getItems();
          meshTile = its;pot = meshTile.length;
          for (tile = 0; tile < pot; tile++)
          {
            Item i = meshTile[tile];
            if (i.isDestroyedOnDecay()) {
              Items.destroyItem(i.getWurmId());
            }
          }
        }
        if (spawn)
        {
          int tile = Server.surfaceMesh.getTile(x, y);
          if (Tiles.decodeHeight(tile) > 0.3D) {
            if (Tiles.canSpawnTree(Tiles.decodeType(tile)))
            {
              int age = 8 + Server.rand.nextInt(6);
              byte tree = (byte)Server.rand.nextInt(9);
              if (TreeData.TreeType.fromInt(tree).isFruitTree()) {
                tree = (byte)(tree + 4);
              }
              byte newData = (byte)(age << 4);
              byte newType = TreeData.TreeType.fromInt(tree).asNormalTree();
              newData = (byte)(newData + 1 & 0xFF);
              
              Server.setSurfaceTile(x, y, 
                Tiles.decodeHeight(tile), newType, newData);
              Players.getInstance().sendChangedTile(x, y, true, false);
            }
          }
        }
      }
    }
    if (killsTrees())
    {
      TilePos currPos = getTilePos();
      TilePos minPos = Zones.safeTile(currPos.add(-10, -10, null));
      TilePos maxPos = Zones.safeTile(currPos.add(10, 10, null));
      for (TilePos tPos : TilePos.areaIterator(minPos, maxPos))
      {
        int tile = Server.surfaceMesh.getTile(tPos);
        
        byte tttype = Tiles.decodeType(tile);
        Tiles.Tile theTile = Tiles.getTile(tttype);
        if ((theTile.isNormalTree()) || (tttype == Tiles.Tile.TILE_GRASS.id) || (tttype == Tiles.Tile.TILE_DIRT.id) || (tttype == Tiles.Tile.TILE_KELP.id) || (tttype == Tiles.Tile.TILE_REED.id))
        {
          Server.setSurfaceTile(tPos, Tiles.decodeHeight(tile), Tiles.Tile.TILE_MYCELIUM.id, (byte)0);
          Players.getInstance().sendChangedTile(tPos, true, false);
          break;
        }
      }
    }
    if ((isWind()) && (!insideStructure)) {
      if ((getParentId() == -10L) && 
        (isOnSurface()))
      {
        float rot = Creature.normalizeAngle(Server.getWeather().getWindRotation() + 180.0F);
        if (getRotation() != ladderRotate(rot)) {
          setRotation(rot);
        }
      }
    }
    if ((!decayed) && (!insideStructure) && (isFlickering()) && (isOnFire())) {
      if (Server.rand.nextFloat() * 10.0F < Server.getWeather().getRain()) {
        setTemperature((short)200);
      }
    }
    if (getTemplateId() == 1178) {
      if (Server.rand.nextInt(20) == 0) {
        pollDistilling();
      }
    }
    if ((!decayed) && (isTrellis())) {
      ageTrellis();
    }
    return decayed;
  }
  
  void pollAging(boolean insideStructure, boolean deeded)
  {
    if ((this.items != null) && (this.items.size() == 1))
    {
      int num = 2;
      if (!isOnSurface()) {
        num += 7;
      }
      if (insideStructure) {
        num += 4;
      }
      if (deeded) {
        num += 2;
      }
      Item[] itarr = (Item[])this.items.toArray(new Item[this.items.size()]);
      Item item = itarr[0];
      if ((!item.deleted) && (item.getTemplate().positiveDecay) && (item.isLiquid()) && (item.getAuxData() == 0))
      {
        int timesSinceLastUsed = (int)((WurmCalendar.currentTime - item.getLastMaintained()) / item.getTemplate().getDecayTime());
        if ((timesSinceLastUsed > 0) && (Server.rand.nextInt(16 - num) == 0))
        {
          float bonus = getMaterialAgingModifier();
          if (Servers.isThisATestServer()) {
            logger.info("Positive Decay added to" + item.getName() + " (" + item.getWurmId() + ") in " + 
              getName() + " (" + this.id + ")");
          }
          item.setQualityLevel(Math.min(100.0F, item.getQualityLevel() + 
            (100.0F - item.getQualityLevel()) * (100.0F - item.getQualityLevel()) / 10000.0F * bonus));
          item.setLastMaintained(WurmCalendar.currentTime);
        }
      }
    }
  }
  
  void pollFermenting()
  {
    if (getItemsAsArray().length != 2) {
      return;
    }
    long lastMaintained = 0L;
    Item liquid = null;
    Item scrap = null;
    long lastowner = -10L;
    for (Item item : getItemsAsArray())
    {
      if (lastMaintained < item.getLastMaintained()) {
        lastMaintained = item.getLastMaintained();
      }
      if (item.isLiquid())
      {
        liquid = item;
      }
      else
      {
        scrap = item;
        
        lastowner = scrap.lastOwner;
      }
    }
    if (lastMaintained < WurmCalendar.currentTime - (Servers.isThisATestServer() ? 86400L : 2419200L))
    {
      Recipe recipe = Recipes.getRecipeFor(lastowner, (byte)0, null, this, true, true);
      if (recipe == null) {
        return;
      }
      Skill primSkill = null;
      Creature lastown = null;
      float alc = 0.0F;
      boolean chefMade = false;
      double bonus = 0.0D;
      boolean showOwner = false;
      try
      {
        lastown = Server.getInstance().getCreature(lastowner);
        bonus = lastown.getVillageSkillModifier();
        alc = ((Player)lastown).getAlcohol();
        Skills skills = lastown.getSkills();
        primSkill = skills.getSkillOrLearn(recipe.getSkillId());
        if (lastown.isRoyalChef()) {
          chefMade = true;
        }
        showOwner = primSkill.getKnowledge(0.0D) > 70.0D;
      }
      catch (NoSuchCreatureException localNoSuchCreatureException) {}catch (NoSuchPlayerException localNoSuchPlayerException) {}
      int diff = recipe.getDifficulty(this);
      float power = 10.0F;
      if (primSkill != null) {
        power = (float)primSkill.skillCheck(diff + alc, null, bonus, false, recipe.getIngredientCount() + diff);
      }
      double ql = Math.min(99.0F, Math.max(1.0F, liquid.getCurrentQualityLevel() + power / 10.0F));
      if (chefMade) {
        ql = Math.max(30.0D, ql);
      }
      if (primSkill != null) {
        ql = Math.max(1.0D, Math.min(primSkill.getKnowledge(0.0D), ql));
      } else {
        ql = Math.max(1.0D, Math.min(Math.max(scrap.getAuxData(), 20), ql));
      }
      if (ql > 70.0D) {
        ql -= Math.min(20.0F, (100.0F - liquid.getCurrentQualityLevel()) / 5.0F);
      }
      if ((getRarity() > 0) || (liquid.getRarity() > 0) || (this.rarity > 0)) {
        ql = GeneralUtilities.calcRareQuality(ql, getRarity(), liquid.getRarity(), this.rarity);
      }
      byte material = recipe.getResultMaterial(this);
      try
      {
        ItemTemplate it = recipe.getResultTemplate(this);
        String owner = showOwner ? PlayerInfoFactory.getPlayerName(lastowner) : null;
        Item newItem = ItemFactory.createItem(it.getTemplateId(), (float)ql, material, (byte)0, owner);
        newItem.setWeight(liquid.getWeightGrams(), true);
        newItem.setLastOwnerId(lastowner);
        if ((RecipesByPlayer.saveRecipe(lastown, recipe, lastowner, null, this)) && (lastown != null)) {
          lastown.getCommunicator().sendServerMessage("Recipe \"" + recipe.getName() + "\" added to your cookbook.", 216, 165, 32, (byte)2);
        }
        newItem.calculateAndSaveNutrition(null, this, recipe);
        
        newItem.setName(recipe.getResultName(this));
        
        ItemTemplate rit = recipe.getResultRealTemplate(this);
        if (rit != null) {
          newItem.setRealTemplate(rit.getTemplateId());
        }
        if (recipe.hasResultState()) {
          newItem.setAuxData(recipe.getResultState());
        }
        if (lastown != null) {
          recipe.addAchievements(lastown, newItem);
        } else {
          recipe.addAchievementsOffline(lastowner, newItem);
        }
        for (Item item : getItemsAsArray()) {
          Items.destroyItem(item.getWurmId());
        }
        insertItem(newItem);
        
        updateName();
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
  }
  
  void pollDistilling()
  {
    if (getTemperature() < 1500) {
      return;
    }
    Item boiler = null;
    Item condenser = null;
    for (Item item : getItemsAsArray()) {
      if (item.getTemplateId() == 1284) {
        boiler = item;
      } else if (item.getTemplateId() == 1285) {
        condenser = item;
      }
    }
    if ((boiler == null) || (condenser == null))
    {
      logger.warning("Still broken " + getWurmId());
      return;
    }
    if (boiler.getTemperature() < 1500) {
      return;
    }
    Item[] boilerItems = boiler.getItemsAsArray();
    if (boilerItems.length != 1) {
      return;
    }
    Item undistilled = boilerItems[0];
    long lastowner = undistilled.lastOwner;
    if (undistilled.getTemperature() < 1500) {
      return;
    }
    if (condenser.getFreeVolume() <= 0) {
      return;
    }
    if (undistilled.lastMaintained > WurmCalendar.currentTime - 600L) {
      return;
    }
    Recipe recipe = Recipes.getRecipeFor(lastowner, (byte)0, null, boiler, true, true);
    if (recipe == null) {
      return;
    }
    Skill primSkill = null;
    Creature lastown = null;
    float alc = 0.0F;
    boolean chefMade = false;
    double bonus = 0.0D;
    boolean showOwner = false;
    try
    {
      lastown = Server.getInstance().getCreature(lastowner);
      bonus = lastown.getVillageSkillModifier();
      alc = ((Player)lastown).getAlcohol();
      Skills skills = lastown.getSkills();
      primSkill = skills.getSkillOrLearn(recipe.getSkillId());
      if (lastown.isRoyalChef()) {
        chefMade = true;
      }
      showOwner = primSkill.getKnowledge(0.0D) > 70.0D;
    }
    catch (NoSuchCreatureException localNoSuchCreatureException) {}catch (NoSuchPlayerException localNoSuchPlayerException) {}
    int diff = recipe.getDifficulty(boiler);
    float power = 0.0F;
    if (primSkill != null) {
      power = (float)primSkill.skillCheck(diff + alc, null, bonus, Server.rand.nextInt(60) != 0, recipe.getIngredientCount() + diff);
    }
    double ql = Math.min(99.0F, Math.max(1.0F, undistilled.getCurrentQualityLevel() + power / 10.0F));
    if (chefMade) {
      ql = Math.max(30.0D, ql);
    }
    if (primSkill != null) {
      ql = Math.max(1.0D, Math.min(primSkill.getKnowledge(0.0D), ql));
    } else {
      ql = Math.max(1.0D, Math.min(Math.max(undistilled.getCurrentQualityLevel(), 20.0F), ql));
    }
    if (ql > 70.0D) {
      ql -= Math.min(20.0F, (100.0F - undistilled.getCurrentQualityLevel()) / 5.0F);
    }
    if ((getRarity() > 0) || (undistilled.getRarity() > 0) || (this.rarity > 0)) {
      ql = GeneralUtilities.calcRareQuality(ql, getRarity(), undistilled.getRarity(), this.rarity);
    }
    undistilled.setLastMaintained(WurmCalendar.currentTime);
    int oldWeight = undistilled.getWeightGrams();
    int usedWeight = Math.min(10, oldWeight);
    Item distilled = null;
    Item[] condenserItems = condenser.getItemsAsArray();
    if (condenserItems.length == 0)
    {
      byte material = recipe.getResultMaterial(boiler);
      try
      {
        ItemTemplate it = recipe.getResultTemplate(boiler);
        String owner = showOwner ? PlayerInfoFactory.getPlayerName(lastowner) : null;
        distilled = ItemFactory.createItem(it.getTemplateId(), (float)ql, material, (byte)0, owner);
        distilled.setLastOwnerId(lastowner);
        distilled.setWeight(usedWeight, true);
        distilled.setTemperature((short)1990);
        if ((RecipesByPlayer.saveRecipe(lastown, recipe, lastowner, null, boiler)) && (lastown != null)) {
          lastown.getCommunicator().sendServerMessage("Recipe \"" + recipe.getName() + "\" added to your cookbook.", 216, 165, 32, (byte)2);
        }
        distilled.calculateAndSaveNutrition(null, undistilled, recipe);
        
        distilled.setName(recipe.getResultName(boiler));
        if (lastown != null) {
          recipe.addAchievements(lastown, distilled);
        } else {
          recipe.addAchievementsOffline(lastowner, distilled);
        }
        ItemTemplate rit = recipe.getResultRealTemplate(boiler);
        if (rit != null) {
          distilled.setRealTemplate(rit.getTemplateId());
        }
        if (recipe.hasResultState()) {
          distilled.setAuxData(recipe.getResultState());
        }
        undistilled.setWeight(oldWeight - usedWeight, true);
        condenser.insertItem(distilled);
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
    else
    {
      distilled = condenserItems[0];
      
      int newUndistilledWeight = Math.max(oldWeight - usedWeight, 0);
      
      float newQl = (distilled.getCurrentQualityLevel() * distilled.getWeightGrams() + (float)ql * usedWeight) / (distilled.getWeightGrams() + usedWeight);
      int newTemp = 1990;
      
      undistilled.setWeight(newUndistilledWeight, true);
      distilled.setQualityLevel(newQl);
      distilled.setTemperature((short)1990);
      distilled.setWeight(distilled.getWeightGrams() + usedWeight, true);
    }
    distilled.setLastMaintained(WurmCalendar.currentTime);
  }
  
  public void advancePlanterWeek()
  {
    Item parent = getParentOrNull();
    Item topParent = getTopParentOrNull();
    if ((parent != null) && (parent.getTemplateId() != 1110)) {
      return;
    }
    if ((topParent != null) && (topParent != this) && (topParent != parent)) {
      return;
    }
    int age = getAuxData() & 0x7F;
    int newAge = age + 4;
    if (newAge >= 100)
    {
      try
      {
        Item newPot = ItemFactory.createItem(1161, getCurrentQualityLevel(), getRarity(), this.creator);
        newPot.setLastOwnerId(getLastOwnerId());
        newPot.setDescription(getDescription());
        if (parent == null)
        {
          newPot.setPosXYZRotation(getPosX(), getPosY(), getPosZ(), getRotation());
          newPot.setIsPlanted(isPlanted());
          VolaTile tile = Zones.getTileOrNull(getTileX(), getTileY(), isOnSurface());
          if (tile != null) {
            tile.addItem(newPot, false, false);
          }
        }
        else
        {
          parent.insertItem(newPot, true);
        }
        Items.destroyItem(getWurmId());
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
    else
    {
      if ((newAge > 5) && (newAge < 95)) {
        newAge += 128;
      }
      setAuxData((byte)newAge);
      setLastMaintained(WurmCalendar.currentTime);
    }
  }
  
  private void checkGnome()
  {
    boolean found = false;
    if (getItems() != null) {
      for (Item i : getItemsAsArray()) {
        if (i.getTemplateId() == 373)
        {
          found = true;
          setAuxData((byte)Math.min(100, getAuxData() + i.getWeightGrams() / 20));
          Items.destroyItem(i.getWurmId());
        }
      }
    }
    if (found) {
      return;
    }
    setAuxData((byte)Math.max(0, getAuxData() - 10));
    if ((this.qualityLevel > 30.0F) && (getAuxData() == 0)) {
      doGnomeTrick();
    }
  }
  
  private float getParentDecayMultiplier(boolean includeMajor, boolean includeRune)
  {
    float toReturn = 1.0F;
    if ((includeMajor) && (
      (getTemplateId() == 1020) || (getTemplateId() == 1022)))
    {
      toReturn = (float)(toReturn * 1.5D);
      includeMajor = false;
    }
    if ((includeRune) && 
      (getSpellEffects() != null))
    {
      toReturn *= getSpellEffects().getRuneEffect(RuneUtilities.ModifierEffect.ENCH_INTERNAL_DECAY);
      includeRune = false;
    }
    Item parent = getParentOrNull();
    if ((parent != null) && ((includeMajor) || (includeRune))) {
      toReturn *= parent.getParentDecayMultiplier(includeMajor, includeRune);
    }
    return toReturn;
  }
  
  private float getDecayMultiplier()
  {
    float mult = getMaterialDecayTimeModifier();
    boolean isWrapped = (isWrapped()) && ((canBePapyrusWrapped()) || (canBeClothWrapped()));
    float wrapMod = isSalted() ? 1.5F : isWrapped ? 5.0F : 1.0F;
    mult *= wrapMod;
    
    Item parent = getParentOrNull();
    Item lunchbox;
    if (parent != null)
    {
      Item topContainer = parent.getParentOuterItemOrNull();
      if (isInLunchbox())
      {
        lunchbox = topContainer;
        if (lunchbox != null) {
          if (lunchbox.getTemplateId() == 1297) {
            mult = (float)(mult * 1.5D);
          } else if (lunchbox.getTemplateId() == 1296) {
            mult = (float)(mult * 1.25D);
          }
        }
      }
      else if ((isLiquid()) && (topContainer != null) && (topContainer.getTemplateId() == 1117))
      {
        mult = (float)(mult * 2.0D);
      }
      mult *= parent.getParentDecayMultiplier(true, true);
    }
    Item topParent = getTopParentOrNull();
    if ((topParent != null) && (topParent.getTemplateId() == 1277)) {
      for (Item container : topParent.getItemsAsArray()) {
        if (container.getTemplateId() == 1278)
        {
          if (container.getItemsAsArray().length <= 0) {
            break;
          }
          mult *= (1 + container.getItemsAsArray().length / 5); break;
        }
      }
    }
    return mult;
  }
  
  public final boolean isItemSpawn()
  {
    return this.template.isItemSpawn();
  }
  
  private void spawnItemSpawn(int[] templateTypes, float startQl, float qlValRange, int maxNums, boolean onGround)
  {
    if (this.ownerId != -10L) {
      return;
    }
    Item[] currentItems = getAllItems(true);
    boolean[] hasTypes = new boolean[templateTypes.length];
    for (Item item : currentItems) {
      for (int x = 0; x < templateTypes.length; x++) {
        if (templateTypes[x] == item.getTemplateId())
        {
          hasTypes[x] = true;
          break;
        }
      }
    }
    for (int x = 0; x < hasTypes.length; x++) {
      if (hasTypes[x] == 0) {
        for (int nums = 0; nums < maxNums; nums++) {
          try
          {
            int templateType = templateTypes[x];
            if (onGround)
            {
              ItemFactory.createItem(templateType, startQl + Server.rand.nextFloat() * qlValRange, 
                getPosX() + 0.3F, getPosY() + 0.3F, 65.0F, 
                isOnSurface(), (byte)0, -10L, "");
            }
            else
            {
              boolean isBoneCollar = templateType == 867;
              
              byte rrarity = (Server.rand.nextInt(100) == 0) || (isBoneCollar) ? 1 : 0;
              if (rrarity > 0) {
                rrarity = (Server.rand.nextInt(100) == 0) && (isBoneCollar) ? 2 : 1;
              }
              if (rrarity > 1) {
                rrarity = (Server.rand.nextInt(100) == 0) && (isBoneCollar) ? 3 : 2;
              }
              float newql = startQl + Server.rand.nextFloat() * qlValRange;
              Item toInsert = ItemFactory.createItem(templateType, newql, rrarity, "");
              if (templateType == 465) {
                toInsert.setData1(CreatureTemplateCreator.getRandomDragonOrDrakeId());
              }
              if (templateType == 371) {
                toInsert.setData1(CreatureTemplateCreator.getRandomDrakeId());
              }
              insertItem(toInsert, true);
            }
          }
          catch (FailedException|NoSuchTemplateException e)
          {
            logWarn(e.getMessage(), e);
          }
        }
      }
    }
  }
  
  public final void fillTreasureChest()
  {
    if (getAuxData() >= 0)
    {
      int[] templateTypes = { 46, 72, 144, 316 };
      
      spawnItemSpawn(templateTypes, 60.0F, 20.0F, 3, false);
    }
    if (getAuxData() >= 1)
    {
      int[] templateTypes2 = { 204, 463 };
      
      spawnItemSpawn(templateTypes2, 60.0F, 20.0F, 1, false);
    }
    if (getAuxData() >= 2)
    {
      int[] templateTypes3 = { 765, 693, 697, 456 };
      
      spawnItemSpawn(templateTypes3, 80.0F, 20.0F, 1, false);
    }
    if (getAuxData() >= 3)
    {
      int[] templateTypes4 = { 374 + Server.rand.nextInt(10), Server.rand.nextInt(10) == 0 ? 'ȍ' : 'ͅ' };
      
      spawnItemSpawn(templateTypes4, 80.0F, 20.0F, 1, false);
    }
    if (getAuxData() >= 4)
    {
      int[] templateTypes5 = { 374 + Server.rand.nextInt(10), 610 + Server.rand.nextInt(10) };
      spawnItemSpawn(templateTypes5, 50.0F, 50.0F, 1, false);
    }
    if (getAuxData() >= 5)
    {
      int[] templateTypes6 = { 349, 456, 694 };
      
      spawnItemSpawn(templateTypes6, 50.0F, 40.0F, 1, false);
    }
    if (getAuxData() >= 6)
    {
      int[] templateTypes7 = { 456, 204 };
      
      spawnItemSpawn(templateTypes7, 80.0F, 20.0F, 5, false);
    }
    if (getAuxData() >= 7)
    {
      int valrei = Server.rand.nextBoolean() ? 524 : Server.rand.nextInt(5) == 0 ? 56 : 867;
      if (Server.rand.nextInt(Players.getInstance().getNumberOfPlayers() > 200 ? 10 : 40) == 0) {
        valrei = 795 + Server.rand.nextInt(17);
      }
      if (Server.rand.nextInt(1000) == 0) {
        valrei = 465;
      }
      int[] templateTypes5 = { valrei };
      
      spawnItemSpawn(templateTypes5, 99.0F, 1.0F, 1, false);
    }
  }
  
  public final void checkItemSpawn()
  {
    if (this.ownerId != -10L) {
      return;
    }
    int templateId = this.template.getTemplateId();
    if (templateId == 236)
    {
      if ((Servers.localServer.isChallengeServer()) && 
        (WurmCalendar.getCurrentTime() > this.lastMaintained + 28800L))
      {
        VolaTile t = Zones.getTileOrNull(getTileX(), getTileY(), isOnSurface());
        if ((t != null) && (t.getItems().length < 50))
        {
          int[] templateTypes = { 46, 144 };
          spawnItemSpawn(templateTypes, 1.0F, 80.0F, 1, true);
        }
        this.lastMaintained = WurmCalendar.getCurrentTime();
      }
      return;
    }
    int[] templateTypes3;
    int[] templateTypes4;
    int valrei;
    if (System.currentTimeMillis() > getData())
    {
      int playerMod = Players.getInstance().getNumberOfPlayers() / 100;
      boolean haveManyPlayers = Players.getInstance().getNumberOfPlayers() > 200;
      int[] templateTypes5;
      switch (templateId)
      {
      case 969: 
        int[] templateTypes = { 46, 72, 144, 316 };
        
        spawnItemSpawn(templateTypes, 60.0F, 20.0F, 3 + playerMod, false);
        int[] templateTypes2 = { 204 };
        
        spawnItemSpawn(templateTypes2, 60.0F, 20.0F, 3 + playerMod, false);
        int mins2;
        int baseMins;
        int mins2;
        if (Servers.localServer.testServer)
        {
          int baseMins = 3;
          mins2 = 1;
        }
        else
        {
          baseMins = 5;
          
          int rndMins = haveManyPlayers ? 20 : 40;
          mins2 = Server.rand.nextInt(rndMins);
        }
        setData(System.currentTimeMillis() + 60000L * (baseMins + mins2));
        
        deleteAllItemspawnEffects();
        break;
      case 970: 
        int[] templateTypes2;
        int[] templateTypes4;
        int num;
        if (Servers.localServer.isChallengeServer())
        {
          int[] templateTypes = { 46 };
          
          spawnItemSpawn(templateTypes, 80.0F, 20.0F, 3 + playerMod, false);
          int[] templateTypes3 = { 204, 144 };
          
          spawnItemSpawn(templateTypes3, 80.0F, 20.0F, 5 + playerMod, false);
          
          int[] templateTypes2 = { 765, 693, 697, 456 };
          
          spawnItemSpawn(templateTypes2, 80.0F, 20.0F, 4 + playerMod, false);
          
          int[] templateTypes4 = {374 + Server.rand.nextInt(10), Server.rand.nextInt(3) == 0 ? 'Ŵ' : 'ų' };
          
          spawnItemSpawn(templateTypes4, 80.0F, 20.0F, 3, false);
          
          int valrei = Server.rand.nextBoolean() ? 524 : Server.rand.nextInt(5) == 0 ? 56 : 867;
          if (Server.rand.nextInt(haveManyPlayers ? 10 : 40) == 0) {
            valrei = 795 + Server.rand.nextInt(17);
          }
          if (Server.rand.nextInt(1000) == 0) {
            valrei = 465;
          }
          templateTypes5 = new int[] { valrei };
          
          spawnItemSpawn(templateTypes5, 99.0F, 1.0F, 1, false);
          setData(System.currentTimeMillis() + 3600000L + 60000L * Server.rand
          
            .nextInt(haveManyPlayers ? 20 : 60));
        }
        else
        {
          templateTypes2 = new int[] { 693, 697, 456 };
          
          spawnItemSpawn(templateTypes2, 80.0F, 20.0F, 4 + playerMod, false);
          
          templateTypes4 = new int[] {374 + Server.rand.nextInt(10) };
          spawnItemSpawn(templateTypes4, 80.0F, 20.0F, 1, false);
          num = Server.rand.nextInt(7);
          if (num == 0)
          {
            int valrei = 867;
            int[] templateTypes5 = { valrei };
            
            spawnItemSpawn(templateTypes5, 99.0F, 1.0F, 1, false);
          }
          else if (num == 1)
          {
            int valrei = 973 + Server.rand.nextInt(6);
            int[] templateTypes5 = { valrei };
            
            spawnItemSpawn(templateTypes5, 99.0F, 1.0F, 1, false);
          }
          else if (num == 2)
          {
            int valrei = 623;
            int[] templateTypes5 = { valrei };
            
            spawnItemSpawn(templateTypes5, 80.0F, 10.0F, 4, false);
          }
          else if (num == 3)
          {
            int valrei = 666;
            int[] templateTypes5 = { valrei };
            
            spawnItemSpawn(templateTypes5, 80.0F, 10.0F, 1, false);
          }
          setData(System.currentTimeMillis() + 86400000L + 3600000L + 60000L * Server.rand
          
            .nextInt(haveManyPlayers ? 20 : 60));
        }
        for (Player player : Players.getInstance().getPlayers()) {
          player.playPersonalSound("sound.spawn.item.central");
        }
        deleteAllItemspawnEffects();
        break;
      case 971: 
        int[] templateTypes = { 44, 46, 132, 218, 38, 72, 30, 29, 32, 28, 35, 349, 456, 45, 694, 144, 316 };
        
        spawnItemSpawn(templateTypes, 50.0F, 40.0F, 50, false);
        
        templateTypes3 = new int[] { 204 };
        spawnItemSpawn(templateTypes3, 50.0F, 40.0F, 100, false);
        
        templateTypes4 = new int[] {374 + Server.rand.nextInt(10), 610 + Server.rand.nextInt(10) };
        
        spawnItemSpawn(templateTypes4, 50.0F, 50.0F, 10, false);
        
        valrei = Server.rand.nextBoolean() ? 524 : Server.rand.nextBoolean() ? 867 : 525;
        if (Server.rand.nextInt(1000) == 0) {
          valrei = 465;
        }
        if (Server.rand.nextInt(10) == 0)
        {
          templateTypes2 = new int[] { 765, 693, 697, 785, 456, 597, valrei };
          
          spawnItemSpawn(templateTypes2, 50.0F, 40.0F, 1, false);
        }
        setData(System.currentTimeMillis() + 43200000L);
        
        int[] templateTypes2 = Players.getInstance().getPlayers();templateTypes5 = templateTypes2.length;
        for (int[] arrayOfInt1 = 0; arrayOfInt1 < templateTypes5; arrayOfInt1++)
        {
          Player player = templateTypes2[arrayOfInt1];
          player.playPersonalSound("sound.spawn.item.perimeter");
        }
        deleteAllItemspawnEffects();
        break;
      }
    }
    else
    {
      long timeout;
      long timeout;
      switch (templateId)
      {
      case 970: 
        String effectName = "central";
        timeout = 600000L;
        break;
      case 971: 
        String effectName = "perimeter";
        timeout = 1800000L;
        break;
      default: 
        return;
      }
      String effectName;
      long timeout;
      if (System.currentTimeMillis() <= getData() - timeout) {
        return;
      }
      for (Effect eff : getEffects()) {
        if (eff.getType() == 19) {
          return;
        }
      }
      logInfo("Spawning " + effectName + " effect since it doesn't exist: " + getEffects().length);
      
      Effect eff = EffectFactory.getInstance().createSpawnEff(getWurmId(), getPosX(), getPosY(), getPosZ(), isOnSurface());
      addEffect(eff);
    }
  }
  
  private void deleteAllItemspawnEffects()
  {
    for (Effect eff : getEffects()) {
      if (eff.getType() == 19) {
        deleteEffect(eff);
      }
    }
  }
  
  private void doGnomeTrick()
  {
    Village current = Zones.getVillage(getTileX(), getTileY(), true);
    
    int dist = (int)Math.max(1.0F, this.qualityLevel - 30.0F);
    int dist2 = dist * dist;
    
    Item itemToPinch = null;
    for (int x = 0; x < dist; x++) {
      for (int y = 0; y < dist; y++) {
        if (Server.rand.nextInt(dist2) == 0)
        {
          VolaTile t = Zones.getTileOrNull(getTileX(), getTileY(), isOnSurface());
          if (t != null) {
            if ((current != null) && (t.getVillage() == current))
            {
              Item[] titems = t.getItems();
              if (titems.length > 0) {
                for (Item tt : titems) {
                  if (tt != this) {
                    if (testInsertItem(tt))
                    {
                      itemToPinch = tt;
                    }
                    else
                    {
                      if (tt.isHollow())
                      {
                        Item[] pitems = tt.getAllItems(false);
                        for (Item pt : pitems) {
                          if (testInsertItem(pt))
                          {
                            itemToPinch = pt;
                            break;
                          }
                        }
                      }
                      if (itemToPinch != null) {
                        break;
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
    if (itemToPinch != null) {
      if ((itemToPinch.getSizeX() <= getSizeX()) && (itemToPinch.getSizeY() <= getSizeY()) && (itemToPinch.getSizeZ() <= getSizeZ())) {
        if ((!itemToPinch.isBulkItem()) || (!itemToPinch.isLiquid()) || (!itemToPinch.isHollow()) || 
          (!itemToPinch.isNoTake()) || (!itemToPinch.isDecoration())) {
          if (itemToPinch.getWeightGrams() <= getWeightGrams())
          {
            if (getAllItems(true).length >= 100) {
              return;
            }
            if (itemToPinch.getParentId() != -10L) {
              return;
            }
            if (getParentId() != -10L) {
              return;
            }
            try
            {
              logInfo(getName() + " " + getWurmId() + " pinching " + itemToPinch.getName());
              itemToPinch.getParent().dropItem(itemToPinch.getWurmId(), false);
              insertItem(itemToPinch, true);
              setAuxData((byte)20);
            }
            catch (NoSuchItemException nsi)
            {
              logWarn("Unexpected " + itemToPinch.getName() + " " + nsi.getMessage());
            }
          }
        }
      }
    }
  }
  
  public final float ladderRotate(float rot)
  {
    int num = (int)(rot / 11.25D);
    num /= 2;
    return (float)(num * 22.5D);
  }
  
  private void checkDrift()
  {
    if ((!isBoat()) || (!isOnSurface())) {
      return;
    }
    if (this.parentId != -10L) {
      return;
    }
    Vector3f worldPos = getPos3f();
    if (worldPos.z >= -1.0F) {
      return;
    }
    if (isMooredBoat())
    {
      if (Features.Feature.METALLIC_ITEMS.isEnabled()) {
        try
        {
          Item anchor = Items.getItem(getData());
          if (anchor.isAnchor())
          {
            float driftChance = getMaterialAnchorBonus(anchor.getMaterial());
            if (Server.rand.nextFloat() < driftChance) {
              return;
            }
          }
        }
        catch (NoSuchItemException e)
        {
          return;
        }
      }
      return;
    }
    Vehicle vehic = Vehicles.getVehicleForId(this.id);
    if (vehic == null) {
      return;
    }
    if (getCurrentQualityLevel() < 10.0F) {
      return;
    }
    if (vehic.pilotId != -10L) {
      return;
    }
    VolaTile t = Zones.getTileOrNull(getTilePos(), isOnSurface());
    if (t == null)
    {
      logWarn(getName() + " drifting, item has no tile at " + getTilePos() + " surfaced=" + isOnSurface());
      return;
    }
    float nPosX = worldPos.x + Server.getWeather().getXWind();
    float nPosY = worldPos.y + Server.getWeather().getYWind();
    
    float worldMetersX = CoordUtils.TileToWorld(Zones.worldTileSizeX);
    float worldMetersY = CoordUtils.TileToWorld(Zones.worldTileSizeY);
    if ((nPosX <= 5.0F) || (nPosX >= worldMetersX - 10.0F) || (nPosY <= 5.0F) || (nPosY >= worldMetersY - 10.0F)) {
      return;
    }
    int diffdecx = (int)(nPosX * 100.0F - worldPos.x * 100.0F);
    int diffdecy = (int)(nPosY * 100.0F - worldPos.y * 100.0F);
    if ((diffdecx == 0) && (diffdecy == 0)) {
      return;
    }
    nPosX = worldPos.x + diffdecx * 0.01F;
    nPosY = worldPos.y + diffdecy * 0.01F;
    
    TilePos testPos = CoordUtils.WorldToTile(nPosX, nPosY);
    int meshTile = Server.caveMesh.getTile(testPos);
    byte tileType = Tiles.decodeType(meshTile);
    if (Tiles.isSolidCave(tileType)) {
      if ((Tiles.decodeType(Server.caveMesh.getTile(getTilePos())) & 0xFF) != 201)
      {
        logInfo(getName() + " drifting in rock at " + getTilePos() + ".");
        return;
      }
    }
    try
    {
      if (Zones.calculateHeight(nPosX, nPosY, true) <= vehic.maxHeight)
      {
        t.moveItem(this, nPosX, nPosY, worldPos.z, 
          Creature.normalizeAngle(getRotation()), true, worldPos.z);
        MovementScheme.itemVehicle = this;
        MovementScheme.movePassengers(vehic, null, false);
      }
      if (Zones.calculateHeight(worldPos.x, worldPos.y, true) >= vehic.maxHeight) {
        setDamage(getDamage() + (diffdecx + diffdecy) * 0.1F);
      }
    }
    catch (NoSuchZoneException nsz)
    {
      Items.destroyItem(this.id);
      logInfo("ItemVehic " + getName() + " destroyed.");
    }
  }
  
  private boolean pollHatching()
  {
    if (isAbility())
    {
      if (isPlanted())
      {
        if ((int)this.damage == 3)
        {
          Server.getInstance().broadCastMessage("The " + 
            getName() + " starts to emanate a weird worrying sound.", getTileX(), 
            getTileY(), isOnSurface(), 50);
          setRarity((byte)2);
        }
        if ((int)this.damage == 50)
        {
          Server.getInstance().broadCastMessage("The " + 
            getName() + " starts to pulsate with a bright light, drawing from the ground.", 
            getTileX(), 
            getTileY(), isOnSurface(), 50);
          setRarity((byte)3);
        }
        if ((int)this.damage == 75) {
          Server.getInstance().broadCastMessage("The ground around " + 
          
            getName() + " is shivering and heaving! Something big is going to happen here soon! You have to get far away!", 
            
            getTileX(), 
            getTileY(), isOnSurface(), 50);
        } else if ((int)this.damage == 95) {
          Server.getInstance().broadCastMessage(
            LoginHandler.raiseFirstLetter(getName() + " is now completely covered in cracks. Run!"), 
            getTileX(), 
            getTileY(), isOnSurface(), 50);
        } else if ((int)this.damage == 99) {
          Server.getInstance().broadCastMessage(
            LoginHandler.raiseFirstLetter(getNameWithGenus() + " is gonna explode! Too late to run..."), 
            getTileX(), 
            getTileY(), isOnSurface(), 20);
        }
      }
    }
    else if ((int)this.damage == 85) {
      Server.getInstance().broadCastMessage("Cracks are starting to form on " + getNameWithGenus() + ".", getTileX(), 
        getTileY(), isOnSurface(), 20);
    } else if ((int)this.damage == 95) {
      Server.getInstance().broadCastMessage(
        LoginHandler.raiseFirstLetter(getNameWithGenus() + " is now completely covered in cracks."), 
        getTileX(), 
        getTileY(), isOnSurface(), 20);
    } else if ((int)this.damage == 99) {
      Server.getInstance().broadCastMessage(
        LoginHandler.raiseFirstLetter(getNameWithGenus() + " stirs as something emerges from it!"), getTileX(), 
        getTileY(), isOnSurface(), 20);
    }
    return setDamage(this.damage + 1.0F);
  }
  
  private boolean pollLightSource()
  {
    if (this.isLightOverride) {
      return false;
    }
    float fuelModifier = 1.0F;
    if (getSpellEffects() != null) {
      fuelModifier = getSpellEffects().getRuneEffect(RuneUtilities.ModifierEffect.ENCH_FUELUSE);
    }
    if (!isOnFire())
    {
      if (getTemperature() > 600) {
        setTemperature((short)200);
      }
      return false;
    }
    if (getTemplateId() == 1301)
    {
      pollWagonerCampfire(WurmCalendar.getHour());
      return false;
    }
    if (getTemplateId() == 1243)
    {
      if (Server.getSecondsUptime() % (int)(600.0F * fuelModifier) == 0)
      {
        int aux = getAuxData();
        if (aux < 0) {
          aux = 127;
        }
        setAuxData((byte)Math.max(0, aux - 1));
        if (getAuxData() <= 0) {
          setTemperature((short)200);
        }
      }
      return false;
    }
    if (getTemplateId() == 1396)
    {
      if (getBless() != null) {
        return false;
      }
      if (!isPlanted())
      {
        setTemperature((short)200);
        return false;
      }
    }
    if ((!isOilConsuming()) && (!isCandleHolder()) && (!isFireplace()))
    {
      if (!isIndestructible()) {
        if (Server.getSecondsUptime() % 60 == 0) {
          return setQualityLevel(getQualityLevel() - 0.5F);
        }
      }
      return false;
    }
    if ((!isStreetLamp()) && (!isBrazier()) && (!isFireplace()))
    {
      if (Server.getSecondsUptime() % (int)(300.0F * fuelModifier) == 0)
      {
        setAuxData((byte)Math.max(0, getAuxData() - 1));
        if (getAuxData() <= 0) {
          setTemperature((short)200);
        }
      }
      return false;
    }
    boolean onSurface = isOnSurface();
    int hour = WurmCalendar.getHour();
    boolean autoSnuffMe = isAutoLit();
    VolaTile vt = Zones.getTileOrNull(getTileX(), getTileY(), this.surfaced);
    Structure structure = vt == null ? null : vt.getStructure();
    Village village = vt == null ? null : vt.getVillage();
    if (!autoSnuffMe)
    {
      autoSnuffMe = (village != null) || (this.onBridge != -10L);
      if ((!autoSnuffMe) && (getBless() != null))
      {
        autoSnuffMe = onBridge() != -10L;
        if ((!autoSnuffMe) && (structure == null)) {
          if (onSurface)
          {
            int encodedTile = Server.surfaceMesh.getTile(getTilePos());
            autoSnuffMe = (getBless() != null) && (Tiles.isRoadType(encodedTile));
          }
          else
          {
            int encodedTile = Server.caveMesh.getTile(getTilePos());
            autoSnuffMe = (getBless() != null) && (Tiles.isReinforcedFloor(Tiles.decodeType(encodedTile)));
          }
        }
      }
    }
    if (Server.rand.nextFloat() <= 0.16F * fuelModifier)
    {
      if (!autoSnuffMe)
      {
        setAuxData((byte)Math.max(0, getAuxData() - 1));
        if (getAuxData() <= 10) {
          refuelLampFromClosestVillage();
        }
      }
      if ((isFireplace()) && (village != null)) {
        if ((structure != null) && (structure.isTypeHouse()) && (structure.isFinished())) {
          if (getAuxData() <= 10) {
            fillFromVillage(village, true);
          }
        }
      }
      if (getAuxData() <= 0)
      {
        setTemperature((short)200);
        if (isFireplace()) {
          deleteFireEffect();
        }
        return false;
      }
    }
    if ((onSurface) && (hour > 4) && (hour < 16)) {
      if (autoSnuffMe)
      {
        setTemperature((short)200);
        if (isFireplace()) {
          deleteFireEffect();
        }
      }
    }
    return false;
  }
  
  public void pollWagonerCampfire(int hour)
  {
    boolean onSurface = isOnSurface();
    
    boolean atCamp = false;
    Wagoner wagoner = Wagoner.getWagoner(this.lastOwner);
    if ((wagoner != null) && (wagoner.isIdle())) {
      atCamp = true;
    }
    boolean isLit = getTemperature() > 200;
    boolean snuff = false;
    boolean light = false;
    int aux = 0;
    if (atCamp) {
      switch (hour)
      {
      case 0: 
      case 1: 
      case 2: 
      case 3: 
      case 4: 
      case 23: 
        aux = 0;
        snuff = true;
        break;
      case 5: 
        if (getAuxData() == 0) {
          aux = Server.rand.nextInt(10) + 1;
        }
        light = true;
        wagoner.say(Wagoner.Speech.BREAKFAST);
        break;
      case 6: 
      case 7: 
      case 8: 
      case 9: 
      case 10: 
        aux = 0;
        snuff = onSurface;
        light = !onSurface;
        break;
      case 11: 
        if (getAuxData() == 0) {
          aux = Server.rand.nextInt(10) + 1;
        }
        light = true;
        wagoner.say(Wagoner.Speech.LUNCH);
        break;
      case 12: 
      case 13: 
      case 14: 
      case 15: 
      case 16: 
        aux = 0;
        snuff = onSurface;
        light = !onSurface;
        break;
      case 17: 
        if (getAuxData() == 0) {
          aux = Server.rand.nextInt(5) + 11;
        } else if ((getAuxData() >= 11) && (getAuxData() <= 15)) {
          aux = getAuxData() + 5;
        }
        light = true;
        wagoner.say(Wagoner.Speech.DINNER);
        break;
      case 18: 
      case 19: 
      case 20: 
      case 21: 
      case 22: 
        aux = 0;
        light = true;
      }
    } else {
      snuff = true;
    }
    if ((snuff) && (isLit))
    {
      setTemperature((short)200);
      deleteFireEffect();
      setAuxData((byte)aux);
      updateIfGroundItem();
    }
    else if ((light) && (!isLit))
    {
      setTemperature((short)10000);
      
      deleteFireEffect();
      
      Effect effect = EffectFactory.getInstance().createFire(getWurmId(), getPosX(), getPosY(), 
        getPosZ(), isOnSurface());
      addEffect(effect);
      setAuxData((byte)aux);
      updateIfGroundItem();
    }
    else if (aux != getAuxData())
    {
      setAuxData((byte)aux);
      updateIfGroundItem();
    }
  }
  
  private void refuelLampFromClosestVillage()
  {
    int startx = getTileX();
    int starty = getTileY();
    
    int stepSize = 7;
    for (int step = 1; step < 8; step++)
    {
      int distance = step * 7;
      for (int yOffs = -distance; yOffs <= distance; yOffs += 14)
      {
        int ys = Zones.safeTileY(starty + yOffs);
        for (int x = startx - distance; x <= startx + distance; x += 7)
        {
          Village vill = Villages.getVillage(Zones.safeTileX(x), ys, true);
          if (fillFromVillage(vill, false)) {
            return;
          }
        }
      }
      for (int xOffs = -distance; xOffs <= distance; xOffs += 14)
      {
        int xs = Zones.safeTileX(startx + xOffs);
        for (int y = starty - distance; y < starty + distance; y += 7)
        {
          Village vill = Villages.getVillage(xs, Zones.safeTileY(y), true);
          if (fillFromVillage(vill, false)) {
            return;
          }
        }
      }
    }
  }
  
  private boolean fillFromVillage(@Nullable Village vill, boolean onDeed)
  {
    if (vill != null)
    {
      int received = vill.getOilAmount(110, onDeed);
      if (received > 0) {
        setAuxData((byte)(getAuxData() + received));
      }
      return true;
    }
    return false;
  }
  
  private void checkIfLightStreetLamp()
  {
    boolean isValidStreetLamp = (isStreetLamp()) && (isPlanted()) && (getAuxData() > 0);
    boolean isValidBrazier = (isBrazier()) && (getAuxData() > 0);
    boolean isValidFireplace = (isFireplace()) && (getAuxData() > 0);
    if ((!isValidStreetLamp) && (!isValidBrazier) && (!isValidFireplace) && (getTemplateId() != 1301)) {
      return;
    }
    if (getTemperature() > 200) {
      return;
    }
    if (getTemplateId() == 1301)
    {
      pollWagonerCampfire(WurmCalendar.getHour());
      return;
    }
    boolean onSurface = isOnSurface();
    int hour = WurmCalendar.getHour();
    boolean autoLightMe = isAutoLit();
    VolaTile vt = Zones.getTileOrNull(getTileX(), getTileY(), this.surfaced);
    Structure structure = vt == null ? null : vt.getStructure();
    Village village = vt == null ? null : vt.getVillage();
    if (!autoLightMe)
    {
      autoLightMe = (village != null) || (this.onBridge != -10L);
      if ((!autoLightMe) && (getBless() != null))
      {
        autoLightMe = onBridge() != -10L;
        if ((!autoLightMe) && (structure == null)) {
          if (onSurface)
          {
            int encodedTile = Server.surfaceMesh.getTile(getTilePos());
            autoLightMe = (getBless() != null) && (Tiles.isRoadType(encodedTile));
          }
          else
          {
            int encodedTile = Server.caveMesh.getTile(getTilePos());
            autoLightMe = (getBless() != null) && (Tiles.isReinforcedFloor(Tiles.decodeType(encodedTile)));
          }
        }
      }
    }
    if ((onSurface) && ((hour < 4) || (hour > 16))) {
      if (autoLightMe)
      {
        setTemperature((short)10000);
        if (isFireplace())
        {
          deleteFireEffect();
          
          Effect effect = EffectFactory.getInstance().createFire(getWurmId(), getPosX(), getPosY(), 
            getPosZ(), isOnSurface());
          addEffect(effect);
        }
      }
    }
  }
  
  private void pollHugeAltar()
  {
    if (this.damage <= 0.0F) {
      return;
    }
    boolean heal = false;
    if (getTemplateId() == 328)
    {
      if (System.currentTimeMillis() - lastPolledBlackAltar > 600000L)
      {
        heal = true;
        lastPolledBlackAltar = System.currentTimeMillis();
      }
    }
    else if (System.currentTimeMillis() - lastPolledWhiteAltar > 600000L)
    {
      heal = true;
      lastPolledWhiteAltar = System.currentTimeMillis();
    }
    if (heal)
    {
      setDamage(this.damage - 1.0F);
      if (this.damage > 0.0F) {
        Server.getInstance().broadCastNormal("You have a sudden vision of " + getName() + " being under attack.");
      }
    }
  }
  
  private void coolItem(int ticks)
  {
    if ((getTemperature() > 200) && ((!isLight()) || (isLightOverride())) && (getTemplateId() != 1243) && (getTemplateId() != 1301))
    {
      short degrees = 5;
      if (isInsulated())
      {
        Item outer = getParentOuterItemOrNull();
        if ((outer != null) && (Server.rand.nextInt(99) < 70 + outer.getRarity() * 2)) {
          return;
        }
        degrees = 1;
      }
      short oldTemperature = getTemperature();
      setTemperature((short)Math.max(200, getTemperature() - ticks * degrees));
      if (getTemperatureState(oldTemperature) != getTemperatureState(getTemperature())) {
        notifyWatchersTempChange();
      }
    }
  }
  
  private void coolInventoryItem()
  {
    coolItem(1);
  }
  
  private void coolInventoryItem(long timeSinceLastCool)
  {
    int ticks = (int)Math.min(timeSinceLastCool / 1000L, 429496728L);
    coolItem(ticks);
  }
  
  public final boolean isOilConsuming()
  {
    return this.template.oilConsuming;
  }
  
  public boolean isCandleHolder()
  {
    return this.template.candleHolder;
  }
  
  public boolean isFireplace()
  {
    return getTemplateId() == 889;
  }
  
  private void coolOutSideItem(boolean everySecond, boolean insideStructure)
  {
    if (this.temperature > 200)
    {
      float speed = 1.0F;
      if (insideStructure) {
        speed *= 0.75F;
      } else if (Server.getWeather().getRain() > 0.2D) {
        speed *= 2.0F;
      }
      if (getRarity() > 0) {
        speed = (float)(speed * Math.pow(0.8999999761581421D, getRarity()));
      }
      int templateId = this.template.getTemplateId();
      if (getSpellEffects() != null) {
        if ((templateId == 180) || (templateId == 1023) || (templateId == 1028) || (templateId == 1178) || (templateId == 37) || (templateId == 178)) {
          if (Server.rand.nextFloat() < getSpellEffects().getRuneEffect(RuneUtilities.ModifierEffect.ENCH_FUELUSE) - 1.0F) {
            speed = 0.0F;
          }
        }
      }
      if ((getTemplateId() == 180) || (getTemplateId() == 178) || 
        (getTemplateId() == 1023) || (getTemplateId() == 1028))
      {
        if (System.currentTimeMillis() - 60000L > this.lastAuxPoll) {
          if ((getTemperature() > 200) && (getAuxData() < 30))
          {
            setAuxData((byte)(getAuxData() + 1));
            this.lastAuxPoll = System.currentTimeMillis();
          }
        }
        if (getAuxData() > 30) {
          setAuxData((byte)30);
        }
      }
      if ((templateId == 180) || (templateId == 1023) || (templateId == 1028))
      {
        setTemperature((short)(int)Math.max(200.0F, this.temperature - speed * 
          Math.max(1.0F, 11.0F - Math.max(1.0F, 20.0F * Math.max(30.0F, getCurrentQualityLevel()) / 200.0F))));
      }
      else if (templateId == 1178)
      {
        setTemperature((short)(int)Math.max(200.0F, this.temperature - speed * 0.5F * 
          Math.max(1.0F, 11.0F - Math.max(1.0F, 10.0F * Math.max(30.0F, getCurrentQualityLevel()) / 200.0F))));
      }
      else if ((templateId == 37) || (templateId == 178))
      {
        setTemperature((short)(int)Math.max(200.0F, this.temperature - speed * 
          Math.max(1.0F, 11.0F - Math.max(1.0F, 10.0F * Math.max(30.0F, getCurrentQualityLevel()) / 200.0F))));
        if (templateId == 37) {
          if (this.temperature <= 210)
          {
            if (getItems().isEmpty())
            {
              float ql = getCurrentQualityLevel();
              try
              {
                ItemFactory.createItem(141, ql, getPosX(), getPosY(), getRotation(), isOnSurface(), 
                  getRarity(), getBridgeId(), null);
              }
              catch (NoSuchTemplateException nst)
              {
                logWarn("No template for ash?" + nst.getMessage(), nst);
              }
              catch (FailedException fe)
              {
                logWarn("What's this: " + fe.getMessage(), fe);
              }
            }
            setQualityLevel(0.0F);
            deleteFireEffect();
          }
        }
      }
      else if (((isLight()) && (!isLightOverride())) || (isFireplace()) || 
        (getTemplateId() == 1243) || (getTemplateId() == 1301))
      {
        pollLightSource();
      }
      else if (everySecond)
      {
        setTemperature((short)(int)Math.max(200.0F, this.temperature - speed * 20.0F));
      }
      else
      {
        setTemperature((short)(int)Math.max(200.0F, this.temperature - speed * 800.0F * 5.0F));
      }
    }
    if (!isOnFire()) {
      if ((isStreetLamp()) || (isBrazier()) || (isFireplace()) || (getTemplateId() == 1301)) {
        checkIfLightStreetLamp();
      } else {
        deleteFireEffect();
      }
    }
  }
  
  private void modTemp(Item parent, int parentTemp, boolean insideStructure)
  {
    short oldTemperature = this.temperature;
    if (parentTemp > 200)
    {
      float qualityModifier = 1.0F;
      int parentTemplateId = parent.getTemplateId();
      float tempMod = 10.0F + 10.0F * (float)Math.min(10.0D, 
        Server.getModifiedPercentageEffect(getCurrentQualityLevel()) / 100.0D);
      
      boolean dealDam = Server.rand.nextInt(30) == 0;
      Item top = null;
      try
      {
        top = Items.getItem(getTopParent());
        int tp = top.getTemplateId();
        if ((tp == 180) || (tp == 1023) || (tp == 1028))
        {
          tempMod = (float)(tempMod + (10 + top.getRarity()) * Math.min(10.0D, Server.getModifiedPercentageEffect(top.getCurrentQualityLevel()) / 70.0D));
          if ((isFood()) && (Server.rand.nextInt(5) == 0)) {
            dealDam = true;
          }
          if ((isBulk()) && (isFood())) {
            qualityModifier = 0.8F;
          }
        }
        else if ((tp == 37) || (tp == 178) || (tp == 1178))
        {
          tempMod = (float)(tempMod + (5 + top.getRarity()) * Math.min(10.0D, Server.getModifiedPercentageEffect(top.getCurrentQualityLevel()) / 70.0D));
          if (isBulk()) {
            if (isFood())
            {
              if (tp == 37) {
                qualityModifier = 0.8F;
              }
            }
            else {
              qualityModifier = 0.8F;
            }
          }
        }
      }
      catch (NoSuchItemException localNoSuchItemException) {}
      if (!parent.equals(top)) {
        tempMod = (float)(tempMod + (7 + parent.getRarity()) * Math.min(10.0D, Server.getModifiedPercentageEffect(parent.getCurrentQualityLevel()) / 100.0D));
      }
      tempMod = Math.max(10.0F, tempMod);
      dealDam = parentTemplateId != 74;
      
      short oldTemp = getTemperature();
      short newTemp = oldTemp;
      if ((isFood()) && (oldTemp > 1500)) {
        tempMod = 1.0F;
      }
      short diff = (short)(parentTemp - oldTemp);
      if (diff > 0) {
        newTemp = (short)(int)Math.min(parentTemp, oldTemp + Math.min(diff, tempMod));
      } else if (diff < 0) {
        newTemp = (short)(int)Math.max(parentTemp, oldTemp + Math.max(diff, -tempMod));
      }
      if (isBurnable())
      {
        if (newTemp > 1000) {
          if (!isIndestructible())
          {
            if (dealDam) {
              if (isRepairable()) {
                setQualityLevel(getQualityLevel() - Math.max(2.0F, tempMod / 10.0F));
              } else {
                setDamage(getDamage() + Math.max(2.0F, tempMod / 10.0F));
              }
            }
            if (getDamage() >= 100.0F)
            {
              int w = getWeightGrams() * fuelEfficiency(getMaterial());
              int newt = parentTemp + w;
              if ((top != null) && (getTemplateId() == 1276) && 
                (top.isOnFire()) && (top.getTemplateId() != 1178))
              {
                Player[] lastOwner = Players.getInstance().getPlayers();
                for (Player player : lastOwner) {
                  if (getLastOwner() == player.getWurmId()) {
                    player.getCommunicator().sendNormalServerMessage("The " + 
                      getName() + " melts and puts out the flames, why did you do that?");
                  }
                }
                top.setTemperature((short)200);
                top.deleteFireEffect();
                return;
              }
              if (getTemplateId() != 1276) {
                parent.setTemperature((short)Math.min(30000, newt));
              }
              return;
            }
            if (getQualityLevel() <= 1.0E-4F)
            {
              int w = getWeightGrams() * fuelEfficiency(getMaterial());
              int newt = parentTemp + w;
              parent.setTemperature((short)Math.min(30000, newt));
              return;
            }
          }
        }
        this.temperature = newTemp;
      }
      else
      {
        if (getTemplateId() == 1285) {
          newTemp = (short)Math.min(300, newTemp);
        }
        setTemperature(newTemp);
        if ((isEgg()) && (newTemp > 400)) {
          if (getData1() > 0) {
            setData1(-1);
          }
        }
        if ((getTemplateId() == 128) && (isSalted()) && (newTemp > 3000) && (parent.getItemCount() == 1)) {
          try
          {
            int salts = getWeightGrams() / 1000 / 10;
            Skill skill = null;
            try
            {
              Creature performer = Server.getInstance().getCreature(getLastOwnerId());
              skill = performer.getSkills().getSkillOrLearn(10038);
            }
            catch (Exception localException) {}
            float ql = 20.0F;
            if (skill != null)
            {
              float result = (float)skill.skillCheck(50.0D, this, 0.0D, false, salts / 10);
              
              ql = 10.0F + (result + 100.0F) * 0.45F;
            }
            for (int x = 0; x < salts; x++)
            {
              Item salt = ItemFactory.createItem(349, ql, (byte)36, getRarity(), this.creator);
              salt.setLastOwnerId(getLastOwnerId());
              salt.setTemperature(newTemp);
              parent.insertItem(salt, true);
            }
            setWeight(getWeightGrams() - (salts + 1) * 100, true);
            setIsSalted(false);
          }
          catch (FailedException e)
          {
            logger.log(Level.WARNING, e.getMessage(), e);
          }
          catch (NoSuchTemplateException e)
          {
            logger.log(Level.WARNING, e.getMessage(), e);
          }
        } else if ((!isLight()) || (isLightOverride())) {
          TempStates.checkForChange(parent, this, oldTemp, newTemp, qualityModifier);
        }
      }
    }
    else if (this.temperature > 200)
    {
      if ((!isLight()) || (isLightOverride()))
      {
        float speed = 1.0F;
        if (insideStructure) {
          speed *= 0.75F;
        } else if (Server.getWeather().getRain() > 0.5D) {
          speed *= 1.5F;
        }
        if (isInLunchbox()) {
          coolItem(1);
        } else if (parent.isAlwaysPoll()) {
          setTemperature((short)(int)Math.max(200.0F, this.temperature - 20.0F * speed));
        } else {
          setTemperature((short)(int)Math.max(200.0F, this.temperature - Zones.numberOfZones / 3.0F * speed));
        }
      }
      else
      {
        pollLightSource();
      }
    }
    if (getTemperatureState(oldTemperature) != getTemperatureState(this.temperature)) {
      notifyWatchersTempChange();
    }
  }
  
  public boolean isBurnable()
  {
    return (!isLocked()) && ((isWood()) || (isCloth()) || (isMelting()) || (isLiquidInflammable()) || (isPaper())) && 
      (!isLiquidCooking()) && (getTemplateId() != 651) && (getTemplateId() != 1097) && 
      (getTemplateId() != 1098) && (getTemplateId() != 1392);
  }
  
  void notifyWatchersTempChange()
  {
    if (this.watchers == null) {
      return;
    }
    for (Creature watcher : this.watchers) {
      watcher.getCommunicator().sendUpdateInventoryItemTemperature(this);
    }
  }
  
  public byte getTemperatureState(short theTemperature)
  {
    byte result;
    byte result;
    if (theTemperature < 0)
    {
      result = -1;
    }
    else
    {
      byte result;
      if (theTemperature < 400)
      {
        result = 0;
      }
      else
      {
        byte result;
        if (theTemperature < 1000)
        {
          result = 1;
        }
        else
        {
          byte result;
          if (theTemperature < 2000)
          {
            result = 2;
          }
          else
          {
            byte result;
            if (isLiquid())
            {
              result = 3;
            }
            else
            {
              byte result;
              if (theTemperature < 3500) {
                result = 4;
              } else {
                result = 5;
              }
            }
          }
        }
      }
    }
    return result;
  }
  
  private void createDaleItems()
  {
    try
    {
      float creationQL = getQualityLevel() * getMaterialDaleModifier();
      Item coal = ItemFactory.createItem(204, creationQL, null);
      coal.setLastOwnerId(this.lastOwner);
      if ((getRarity() > 0) && 
        (Server.rand.nextInt(10) == 0)) {
        coal.setRarity(getRarity());
      }
      insertItem(coal);
      Item tar = ItemFactory.createItem(153, creationQL, null);
      tar.setLastOwnerId(this.lastOwner);
      if ((getRarity() > 0) && 
        (Server.rand.nextInt(10) == 0)) {
        tar.setRarity(getRarity());
      }
      insertItem(tar);
      Item ash = ItemFactory.createItem(141, creationQL, null);
      ash.setLastOwnerId(this.lastOwner);
      insertItem(ash);
      if ((getRarity() > 0) && 
        (Server.rand.nextInt(10) == 0)) {
        ash.setRarity(getRarity());
      }
    }
    catch (NoSuchTemplateException nst)
    {
      logWarn("No template for ash?" + nst.getMessage(), nst);
    }
    catch (FailedException fe)
    {
      logWarn("What's this: " + fe.getMessage(), fe);
    }
  }
  
  public void deleteFireEffect()
  {
    Effect toDelete = null;
    if (this.effects != null)
    {
      for (Iterator<Effect> it = this.effects.iterator(); it.hasNext();)
      {
        Effect eff = (Effect)it.next();
        if (eff.getType() == 0) {
          toDelete = eff;
        }
      }
      if (toDelete != null) {
        deleteEffect(toDelete);
      }
    }
  }
  
  private void sendStatus()
  {
    sendUpdate();
  }
  
  public final boolean isOnFire()
  {
    if (isAlwaysLit()) {
      return true;
    }
    if (this.temperature < 600) {
      return false;
    }
    if (getTemplateId() != 729) {
      return true;
    }
    return getAuxData() > 0;
  }
  
  public final Behaviour getBehaviour()
    throws NoSuchBehaviourException
  {
    return this.template.getBehaviour();
  }
  
  public final short getImageNumber()
  {
    if (getTemplateId() == 1310) {
      try
      {
        Creature storedCreature = Creatures.getInstance().getCreature(getData());
        if (storedCreature.getTemplateId() == 45) {
          return 404;
        }
      }
      catch (NoSuchCreatureException ex)
      {
        logger.log(Level.WARNING, ex.getMessage(), ex);
      }
    }
    if (getTemplateId() == 1307)
    {
      if (getData1() <= 0) {
        return 1460;
      }
      switch (getMaterial())
      {
      case 62: 
        return 1461;
      case 15: 
        return 1462;
      case 89: 
        return 1463;
      case 61: 
        return 1464;
      case 19: 
        return 1465;
      case 11: 
        return 1467;
      case 7: 
      case 96: 
        return 1468;
      case 8: 
        return 1469;
      case 10: 
        return 1470;
      case 30: 
        return 1471;
      case 13: 
        return 1472;
      case 12: 
        return 1473;
      case 34: 
        return 1474;
      }
      if (MaterialUtilities.isMetal(getMaterial())) {
        return 1467;
      }
      if (MaterialUtilities.isWood(getMaterial())) {
        return 1466;
      }
      if (MaterialUtilities.isClay(getMaterial())) {
        return 1465;
      }
      if (getRealTemplate() != null) {
        return getRealTemplate().getImageNumber();
      }
      return 1460;
    }
    if (getTemplateId() == 1346)
    {
      if (isEmpty(false)) {
        return 846;
      }
      Item reel = (Item)this.items.iterator().next();
      if (reel.isEmpty(false)) {
        return 866;
      }
      return 886;
    }
    if (getTemplateId() == 387)
    {
      try
      {
        ItemTemplate temp = ItemTemplateFactory.getInstance().getTemplate(getData1());
        return temp.getImageNumber();
      }
      catch (NoSuchTemplateException localNoSuchTemplateException) {}
    }
    else
    {
      if ((this.realTemplate > 0) && (getTemplate().useRealTemplateIcon()))
      {
        if (((getRealTemplateId() == 92) || (getRealTemplateId() == 368)) && (!isRaw())) {
          return 523;
        }
        ItemTemplate realTemplate = getRealTemplate();
        assert (realTemplate != null);
        return realTemplate.getImageNumber();
      }
      if (((getTemplateId() == 92) || (getTemplateId() == 368)) && (!isRaw())) {
        return 523;
      }
    }
    Recipe recipe = getRecipe();
    if (recipe != null) {
      if (!recipe.getResultItem().isFoodGroup()) {
        return recipe.getResultItem().getIcon();
      }
    }
    return this.template.getImageNumber();
  }
  
  public final boolean isOnSurface()
  {
    if (getZoneId() != -10L) {
      return this.surfaced;
    }
    if (getOwnerId() == -10L) {
      return this.surfaced;
    }
    try
    {
      Creature owner = Server.getInstance().getCreature(getOwnerId());
      return owner.isOnSurface();
    }
    catch (NoSuchCreatureException localNoSuchCreatureException) {}catch (NoSuchPlayerException nsp)
    {
      logInfo(this.id + " strange. Owner " + getOwnerId() + " is no creature or player.");
    }
    return this.surfaced;
  }
  
  private final String containsExamine(Creature performer)
  {
    String toReturn = " ";
    if (isFishingReel())
    {
      toReturn = toReturn + "The rod contains a " + getName() + ". ";
      Item fishingLine = getFishingLine();
      if (fishingLine == null)
      {
        String lineName = getFishingLineName();
        toReturn = toReturn + "Requires a " + lineName + " to be able to be used for fishing.";
      }
      else
      {
        toReturn = toReturn + "The reel contains a " + fishingLine.getName() + ".";
        toReturn = toReturn + fishingLine.containsExamine(performer);
      }
    }
    else if (isFishingLine())
    {
      Item fishingFloat = getFishingFloat();
      Item fishingHook = getFishingHook();
      if ((fishingFloat == null) && (fishingHook == null))
      {
        toReturn = toReturn + "The line requires a float and a fishing hook to be able to be used for fishing.";
      }
      else
      {
        if (fishingFloat == null) {
          toReturn = toReturn + "The line requires a float to be able to be used for fishing.";
        } else {
          toReturn = toReturn + fishingFloat.containsExamine(performer);
        }
        if (fishingHook == null) {
          toReturn = toReturn + "The line requires a fishing hook to be able to be used for fishing.";
        } else {
          toReturn = toReturn + fishingHook.containsExamine(performer);
        }
      }
    }
    else if (isFishingFloat())
    {
      toReturn = toReturn + "There is a " + getName() + " float on the line.";
    }
    else if (isFishingHook())
    {
      toReturn = toReturn + "There is a " + getName() + " on the end of the line. ";
      
      Item fishingBait = getFishingBait();
      if (fishingBait == null) {
        toReturn = toReturn + "There is no bait, but then again some fish like that!";
      } else {
        toReturn = toReturn + fishingBait.containsExamine(performer);
      }
    }
    else if (isFishingBait())
    {
      toReturn = toReturn + "There is " + getName() + " as bait on the fishing hook.";
    }
    return toReturn;
  }
  
  public final String examine(Creature performer)
  {
    String toReturn = this.template.getDescriptionLong();
    if (getTemplateId() == 1311) {
      if (!isEmpty(true)) {
        toReturn = this.template.getDescriptionLong() + " There is a creature in this cage.";
      } else {
        toReturn = this.template.getDescriptionLong() + " This cage is empty.";
      }
    }
    if ((this.template.templateId == 133) && (getRealTemplateId() == 1254)) {
      toReturn = toReturn.replace("tallow", "beeswax");
    }
    boolean gotDesc = false;
    Recipe recipe = getRecipe();
    if (recipe != null)
    {
      String desc = recipe.getResultItem().getResultDescription(this);
      if ((desc.length() > 0) && (!desc.startsWith("Any ")))
      {
        toReturn = desc;
        gotDesc = true;
      }
    }
    else
    {
      if (getTemplateId() == 1344)
      {
        toReturn = toReturn + " It is made from " + getMaterialString(getMaterial()) + ".";
        toReturn = toReturn + MethodsItems.getImpDesc(performer, this);
        
        Item fishingLine = getFishingLine();
        if (fishingLine == null)
        {
          toReturn = toReturn + " Requires a basic fishing line to be able to be used for fishing.";
        }
        else
        {
          toReturn = toReturn + " The pole has a " + fishingLine.getName() + " attached to the end of it.";
          toReturn = toReturn + fishingLine.containsExamine(performer);
        }
        toReturn = toReturn + MethodsItems.getRarityDesc(getRarity());
        return toReturn;
      }
      if (getTemplateId() == 1346)
      {
        toReturn = toReturn + " It is made from " + getMaterialString(getMaterial()) + ".";
        toReturn = toReturn + MethodsItems.getImpDesc(performer, this);
        
        Item fishingReel = getFishingReel();
        if (fishingReel == null) {
          toReturn = toReturn + " Requires a fishing reel to be able to be used for fishing.";
        } else {
          toReturn = toReturn + fishingReel.containsExamine(performer);
        }
        toReturn = toReturn + MethodsItems.getRarityDesc(getRarity());
        return toReturn;
      }
      if (isFishingReel())
      {
        toReturn = toReturn + " It is made from " + getMaterialString(getMaterial()) + ".";
        toReturn = toReturn + MethodsItems.getImpDesc(performer, this);
        
        Item fishingLine = getFishingLine();
        if (fishingLine == null)
        {
          String lineName = getFishingLineName();
          toReturn = toReturn + " Requires a " + lineName + " to be able to be used for fishing.";
        }
        else
        {
          toReturn = toReturn + " The reel contains a " + fishingLine.getName() + ".";
          toReturn = toReturn + fishingLine.containsExamine(performer);
        }
        toReturn = toReturn + MethodsItems.getRarityDesc(getRarity());
        return toReturn;
      }
      Item fishingHook;
      if (isFishingLine())
      {
        toReturn = toReturn + MethodsItems.getImpDesc(performer, this);
        
        Item fishingFloat = getFishingFloat();
        fishingHook = getFishingHook();
        if ((fishingFloat == null) && (fishingHook == null))
        {
          toReturn = toReturn + " The line requires a float and a fishing hook to be able to be used for fishing.";
        }
        else
        {
          if (fishingFloat == null) {
            toReturn = toReturn + " The line requires a float to be able to be used for fishing.";
          } else {
            toReturn = toReturn + fishingFloat.containsExamine(performer);
          }
          if (fishingHook == null) {
            toReturn = toReturn + " The line requires a fishing hook to be able to be used for fishing.";
          } else {
            toReturn = toReturn + fishingHook.containsExamine(performer);
          }
        }
        toReturn = toReturn + MethodsItems.getRarityDesc(getRarity());
        return toReturn;
      }
      if (isFishingHook())
      {
        toReturn = toReturn + " It is made from " + getMaterialString(getMaterial()) + ".";
        toReturn = toReturn + MethodsItems.getImpDesc(performer, this);
        
        Item fishingBait = getFishingBait();
        if (fishingBait == null) {
          toReturn = toReturn + "There is no bait, but then again some fish like that!";
        } else {
          toReturn = toReturn + fishingBait.containsExamine(performer);
        }
        toReturn = toReturn + MethodsItems.getRarityDesc(getRarity());
        return toReturn;
      }
      if (getTemplateId() == 1310)
      {
        Creature[] storedanimal = Creatures.getInstance().getCreatures();
        for (Creature cret : storedanimal) {
          if (cret.getWurmId() == getData())
          {
            String exa = cret.examine();
            performer.getCommunicator().sendNormalServerMessage(exa);
            
            Brand brand = Creatures.getInstance().getBrand(getData());
            if (brand != null) {
              try
              {
                Village v = Villages.getVillage((int)brand.getBrandId());
                performer.getCommunicator().sendNormalServerMessage("It has been branded by and belongs to the settlement of " + v
                  .getName() + ".");
              }
              catch (NoSuchVillageException nsv)
              {
                brand.deleteBrand();
              }
            }
            if (cret.isCaredFor())
            {
              long careTaker = cret.getCareTakerId();
              PlayerInfo info = PlayerInfoFactory.getPlayerInfoWithWurmId(careTaker);
              if (info != null) {
                performer.getCommunicator().sendNormalServerMessage("It is being taken care of by " + info
                  .getName() + ".");
              }
            }
            performer.getCommunicator().sendNormalServerMessage(StringUtilities.raiseFirstLetter(cret
              .getStatus().getBodyType()));
            if (cret.isDomestic()) {
              if (System.currentTimeMillis() - cret.getLastGroomed() > 172800000L) {
                performer.getCommunicator().sendNormalServerMessage("This creature could use some grooming.");
              }
            }
            if (cret.hasTraits()) {
              try
              {
                Skill breeding = performer.getSkills().getSkill(10085);
                double knowl;
                double knowl;
                if (performer.getPower() > 0) {
                  knowl = 99.99D;
                } else {
                  knowl = breeding.getKnowledge(0.0D);
                }
                if (knowl > 20.0D)
                {
                  StringBuilder buf = new StringBuilder();
                  for (int x = 0; x < 64; x++) {
                    if ((cret.hasTrait(x)) && (knowl - 20.0D > x))
                    {
                      String l = Traits.getTraitString(x);
                      if (l.length() > 0)
                      {
                        buf.append(l);
                        buf.append(' ');
                      }
                    }
                  }
                  if (buf.toString().length() > 0) {
                    performer.getCommunicator().sendNormalServerMessage(buf.toString());
                  }
                }
              }
              catch (NoSuchSkillException localNoSuchSkillException) {}
            }
            if (cret.isPregnant())
            {
              Offspring o = cret.getOffspring();
              Random rand = new Random(cret.getWurmId());
              int left = o.getDaysLeft() + rand.nextInt(3);
              performer.getCommunicator().sendNormalServerMessage(
                LoginHandler.raiseFirstLetter(cret.getHeSheItString()) + " will deliver in about " + left + (left != 1 ? " days." : " day."));
            }
            String motherfather = "";
            if (cret.getMother() != -10L) {
              try
              {
                Creature mother = Server.getInstance().getCreature(cret.getMother());
                motherfather = motherfather + StringUtilities.raiseFirstLetter(cret.getHisHerItsString()) + " mother is " + mother.getNameWithGenus() + ". ";
              }
              catch (NoSuchCreatureException|NoSuchPlayerException localNoSuchCreatureException) {}
            }
            if (cret.getFather() != -10L) {
              try
              {
                Creature father = Server.getInstance().getCreature(cret.getFather());
                motherfather = motherfather + StringUtilities.raiseFirstLetter(cret.getHisHerItsString()) + " father is " + father.getNameWithGenus() + ". ";
              }
              catch (NoSuchCreatureException|NoSuchPlayerException localNoSuchCreatureException1) {}
            }
            if (motherfather.length() > 0) {
              performer.getCommunicator().sendNormalServerMessage(motherfather);
            }
            if (cret.getStatus().getBody().getWounds() != null) {
              performer.getCommunicator().sendNormalServerMessage("This creature seems to be injured.");
            } else {
              performer.getCommunicator().sendNormalServerMessage("This creature seems healthy without any noticeable ailments.");
            }
            if (cret.isHorse()) {
              performer.getCommunicator().sendNormalServerMessage("Its colour is " + cret.getColourName() + ".");
            }
          }
        }
      }
      else if ((getTemplateId() == 92) && (isCooked()))
      {
        String animal = getMaterial() == 2 ? "animal" : getMaterialString(getMaterial());
        toReturn = "Cooked meat that originally came from some kind of " + animal + ".";
        gotDesc = true;
      }
      else if ((getTemplateId() == 368) && (isCooked()))
      {
        String animal = getMaterial() == 2 ? "animal" : getMaterialString(getMaterial());
        toReturn = "Cooked fillet of meat that originally came from some kind of " + animal + ".";
        gotDesc = true;
      }
      else if ((getTemplate().getFoodGroup() == 1201) && (isSteamed()))
      {
        toReturn = "Steamed " + this.template.getName() + " with all its flavours sealed in.";
        gotDesc = true;
      }
      else if ((getTemplateId() == 369) && (isSteamed()))
      {
        ItemTemplate it = getRealTemplate();
        String fish = it.getName() + " fillet";
        toReturn = "Steamed " + fish + " with all its flavours sealed in.";
        gotDesc = true;
      }
      else if ((getTemplate().getFoodGroup() == 1156) && (isSteamed()))
      {
        toReturn = "Steamed " + this.template.getName() + " with all its flavours sealed in.";
        gotDesc = true;
      }
    }
    if ((!gotDesc) && (this.template.descIsExam)) {
      if (this.description.length() > 0) {
        toReturn = this.description;
      }
    }
    if (this.template.templateId == 386)
    {
      try
      {
        toReturn = ItemTemplateFactory.getInstance().getTemplate(this.realTemplate).getDescriptionLong();
      }
      catch (NoSuchTemplateException nst)
      {
        logInfo("No template for " + getName() + ", id=" + this.realTemplate);
      }
    }
    else if (this.material == 0)
    {
      if ((this.realTemplate > 0) && (this.template.templateId != 1307)) {
        try
        {
          toReturn = ItemTemplateFactory.getInstance().getTemplate(this.realTemplate).getDescriptionLong();
        }
        catch (NoSuchTemplateException nst)
        {
          logInfo("No template for " + getName() + ", id=" + this.realTemplate);
        }
      }
    }
    else if (getTemplateId() == 861)
    {
      if (Servers.localServer.PVPSERVER)
      {
        toReturn = toReturn + " Anyone may take the stuff inside but at least you can lock it.";
        toReturn = toReturn + " You may tie a creature to it but that does not keep it safe.";
      }
      else
      {
        toReturn = toReturn + " Nobody may take the things inside even if it is unlocked.";
        toReturn = toReturn + " You may tie a creature to it and nobody will be able to steal it.";
      }
      toReturn = toReturn + " If left on the ground, an undamaged tent will decay within a few weeks.";
    }
    else if (this.id == 636451406482434L)
    {
      toReturn = "A simple but beautiful shirt made from the finest threads of cloth. It's as black as night, except the yellow emblem which contains the symbol of a bat.";
    }
    else if (this.id == 1810562858091778L)
    {
      toReturn = "A long and slender pen. This pen is mightier than a sword.";
    }
    else if (this.id == 357896990754562L)
    {
      toReturn = "A relic of the days of old, with a tear of Libila stained on the blade.";
    }
    else if (this.id == 156637289513474L)
    {
      toReturn = "An exquisite wave pattern glimmers down the blade of this famous katana.";
    }
    else if (this.id == 2207297627489538L)
    {
      toReturn = "A well-worn shovel that trumps them all.";
    }
    else if (this.id == 3343742719231234L)
    {
      toReturn = "For some reason, your feel tears come to your eyes as you look upon it. Or is it blood?";
    }
    else if (isUnstableRift())
    {
      toReturn = toReturn + " It is unstable and will disappear in about " + Server.getTimeFor(Math.max(0L, 1482227988600L - System.currentTimeMillis())) + ".";
    }
    if (getTemplateId() == 1307) {
      if (getData1() <= 0)
      {
        if (getAuxData() < 65) {
          toReturn = toReturn + " A chisel";
        } else {
          toReturn = toReturn + " A metal brush";
        }
        toReturn = toReturn + " would be useful to clear away some dirt and rock from it.";
      }
      else if (getRealTemplate() != null)
      {
        toReturn = "A small fragment from " + getRealTemplate().getNameWithGenus() + ". You think you could recreate the " + getRealTemplate().getName() + " if you had a bit more material.";
      }
    }
    if (getRarity() > 0) {
      toReturn = toReturn + MethodsItems.getRarityDesc(getRarity());
    }
    if (isInventory())
    {
      int itemCount = getItemCount();
      toReturn = this.template.getDescriptionLong() + " Your inventory is ";
      if (itemCount <= 25) {
        toReturn = toReturn + this.template.getDescriptionRotten() + ".";
      } else if (itemCount <= 50) {
        toReturn = toReturn + this.template.getDescriptionBad() + ".";
      } else if (itemCount <= 75) {
        toReturn = toReturn + this.template.getDescriptionNormal() + ".";
      } else if (itemCount >= 100) {
        toReturn = toReturn + "full.";
      } else {
        toReturn = toReturn + this.template.getDescriptionSuperb() + ".";
      }
      if (itemCount != 1) {
        toReturn = toReturn + " It contains " + itemCount + " items.";
      } else {
        toReturn = toReturn + " It contains " + itemCount + " item.";
      }
      return toReturn;
    }
    if (getTemplateId() == 387)
    {
      try
      {
        ItemTemplate temp = ItemTemplateFactory.getInstance().getTemplate(getData1());
        toReturn = temp.getDescriptionLong() + " It looks strangely crooked.";
      }
      catch (NoSuchTemplateException nst)
      {
        toReturn = toReturn + " It looks strangely crooked.";
        logWarn(getData1() + ": " + nst.getMessage(), nst);
      }
    }
    else if ((getTemplateId() != 74) && (getTemplateId() != 37))
    {
      String tempString = "";
      switch (getTemperatureState(this.temperature))
      {
      case -1: 
        tempString = " It is frozen.";
        break;
      case 2: 
        tempString = " It is hot.";
        break;
      case 1: 
        tempString = " It is very warm.";
        break;
      case 4: 
        tempString = " It is searing hot.";
        break;
      case 3: 
        tempString = " It is boiling.";
        break;
      case 5: 
        tempString = " It is glowing from the heat.";
        break;
      case 0: 
        break;
      }
      toReturn = toReturn + tempString;
    }
    if ((this.template.templateId == 1272) || (this.template.templateId == 748)) {
      switch (getAuxData())
      {
      case 0: 
        InscriptionData ins = getInscription();
        if ((ins != null) && (ins.hasBeenInscribed())) {
          toReturn = toReturn + " It has a message on it!";
        } else {
          toReturn = toReturn + " It is blank.";
        }
        break;
      case 1: 
        toReturn = toReturn + " It has a recipe on it!";
        break;
      case 2: 
        toReturn = toReturn + " It has a waxed coating!";
      }
    }
    if (getTemplateId() == 481)
    {
      String healString;
      String healString;
      if (this.auxbyte <= 0)
      {
        healString = " It is useless to heal with.";
      }
      else
      {
        String healString;
        if (this.auxbyte < 5)
        {
          healString = " It will help some against wounds.";
        }
        else
        {
          String healString;
          if (this.auxbyte < 10)
          {
            healString = " It will be pretty efficient against wounds.";
          }
          else
          {
            String healString;
            if (this.auxbyte < 15)
            {
              healString = " It will be good against wounds.";
            }
            else
            {
              String healString;
              if (this.auxbyte < 20) {
                healString = " It will be very good against wounds.";
              } else {
                healString = " It is supreme against wounds.";
              }
            }
          }
        }
      }
      toReturn = toReturn + healString;
    }
    if ((isBowUnstringed()) || (isWeaponBow()))
    {
      if (getMaterial() == 40) {
        toReturn = toReturn + " The willow wood used in this bow gives it good strength yet supreme flexibility.";
      }
    }
    else if ((getTemplateId() == 455) || (getTemplateId() == 454) || 
      (getTemplateId() == 456))
    {
      if (getMaterial() == 39) {
        toReturn = toReturn + " Cedar arrows are straighter and smoother than other arrows.";
      } else if (getMaterial() == 41) {
        toReturn = toReturn + " Maple arrows are smooth, uniform and take less damage than other arrows.";
      }
    }
    else if (getTemplateId() == 526)
    {
      toReturn = toReturn + " It has " + getAuxData() + " charges left.";
    }
    else if (isAbility())
    {
      if (getTemplateId() == 794) {
        toReturn = toReturn + " If solved, something very dramatic will happen.";
      } else if (getAuxData() == 2) {
        toReturn = toReturn + " It has one charge left.";
      } else {
        toReturn = toReturn + " It has " + (3 - getAuxData()) + " charges left.";
      }
    }
    else if (getTemplateId() == 726)
    {
      if (this.auxbyte != 0)
      {
        Kingdom k = Kingdoms.getKingdom(this.auxbyte);
        if (k != null)
        {
          toReturn = toReturn + " This is where the people of " + k.getName() + " meet and resolve disputes.";
          King king = King.getKing(this.auxbyte);
          if (king != null)
          {
            if (king.getChallengeAcceptedDate() > 0L)
            {
              long nca = king.getChallengeAcceptedDate();
              String sa = Server.getTimeFor(nca - System.currentTimeMillis());
              toReturn = toReturn + " The ruler must show up in " + sa + ".";
            }
            if (king.getChallengeDate() > 0L)
            {
              long nca = king.getChallengeDate();
              String sa = Server.getTimeFor(System.currentTimeMillis() - nca);
              toReturn = toReturn + " The ruler was challenged " + sa + " ago.";
            }
            long nc = king.getNextChallenge();
            if (nc > System.currentTimeMillis())
            {
              String s = Server.getTimeFor(nc - System.currentTimeMillis());
              toReturn = toReturn + " Next challenge avail in " + s + ".";
            }
            if (king.hasFailedAllChallenges())
            {
              toReturn = toReturn + " The " + king.getRulerTitle() + " has failed all challenges. Voting for removal is in progress.";
              if (((Player)performer).getSaveFile().votedKing) {
                toReturn = toReturn + " You have already voted.";
              } else {
                toReturn = toReturn + " You may now vote for removal of the current ruler.";
              }
            }
            if (performer.getPower() > 0)
            {
              performer.getLogger().log(Level.INFO, performer
                .getName() + " examining " + k.getName() + " duel ring.");
              
              toReturn = toReturn + " Challenges: " + king.getChallengeSize() + " Declined: " + king.getDeclinedChallengesNumber() + " Votes: " + king.getVotes() + ".";
            }
          }
          else
          {
            toReturn = toReturn + " There is no ruler.";
          }
        }
      }
    }
    else if (getTemplateId() == 740)
    {
      toReturn = toReturn + " It has the head of a ";
      switch (getAuxData() % 10)
      {
      case 0: 
        toReturn = toReturn + "dog"; break;
      case 1: 
        toReturn = toReturn + "pheasant"; break;
      case 2: 
        toReturn = toReturn + "stag"; break;
      case 3: 
        toReturn = toReturn + "bull"; break;
      case 4: 
        toReturn = toReturn + "dragon"; break;
      case 5: 
        toReturn = toReturn + "nymph"; break;
      case 6: 
        toReturn = toReturn + "two-headed giant"; break;
      case 7: 
        toReturn = toReturn + "bear"; break;
      case 8: 
        toReturn = toReturn + "demon"; break;
      case 9: 
        toReturn = toReturn + "rabbit"; break;
      default: 
        toReturn = toReturn + "dog";
      }
      toReturn = toReturn + " on it.";
    }
    else if (getTemplateId() == 1076)
    {
      if (getData1() > 0)
      {
        toReturn = toReturn + " It has a";
        switch (getData1())
        {
        case 1: 
          if (getData2() > 50) {
            toReturn = toReturn + " star sapphire inserted in the socket.";
          } else {
            toReturn = toReturn + " sapphire inserted in the socket.";
          }
          break;
        case 2: 
          if (getData2() > 50) {
            toReturn = toReturn + " star emerald inserted in the socket.";
          } else {
            toReturn = toReturn + "n emerald inserted in the socket.";
          }
          break;
        case 3: 
          if (getData2() > 50) {
            toReturn = toReturn + " star ruby inserted in the socket.";
          } else {
            toReturn = toReturn + " ruby inserted in the socket.";
          }
          break;
        case 4: 
          if (getData2() > 50) {
            toReturn = toReturn + " black opal inserted in the socket.";
          } else {
            toReturn = toReturn + "n opal inserted in the socket.";
          }
          break;
        case 5: 
          if (getData2() > 50) {
            toReturn = toReturn + " star diamond inserted in the socket.";
          } else {
            toReturn = toReturn + " diamond inserted in the socket.";
          }
          break;
        }
      }
      else
      {
        toReturn = toReturn + " You could add a gem in the empty socket.";
      }
    }
    else if (getTemplateId() == 1077)
    {
      if (getData1() > 0) {
        toReturn = toReturn + " It will improve skillgain for " + SkillSystem.getNameFor(getData1()) + ".";
      }
    }
    if (getColor() != -1) {
      if ((!isDragonArmour()) || (getColor2() == -1))
      {
        toReturn = toReturn + " ";
        if (isWood())
        {
          toReturn = toReturn + "Wood ";
          toReturn = toReturn + MethodsItems.getColorDesc(getColor()).toLowerCase();
        }
        else
        {
          toReturn = toReturn + MethodsItems.getColorDesc(getColor());
        }
      }
    }
    if ((supportsSecondryColor()) && (getColor2() != -1))
    {
      toReturn = toReturn + " ";
      if (isDragonArmour())
      {
        toReturn = toReturn + MethodsItems.getColorDesc(getColor2());
      }
      else
      {
        toReturn = toReturn + LoginHandler.raiseFirstLetter(getSecondryItemName());
        toReturn = toReturn + MethodsItems.getColorDesc(getColor2()).toLowerCase();
      }
    }
    if (this.lockid != -10L) {
      if (!isKey()) {
        try
        {
          Item lock = Items.getItem(this.lockid);
          if (lock.isLocked()) {
            toReturn = toReturn + " It is locked with a lock of " + lock.getLockStrength() + " quality.";
          } else {
            toReturn = toReturn + " It has a lock of " + lock.getLockStrength() + " quality, which is unlocked.";
          }
        }
        catch (NoSuchItemException nsi)
        {
          logWarn(this.id + " has a lock that can't be found: " + this.lockid, nsi);
        }
      }
    }
    if (getBless() != null) {
      if (performer.getFaith() > 20.0F) {
        if (performer.getFaith() < 30.0F) {
          toReturn = toReturn + " It has an interesting aura.";
        } else if (performer.getFaith() < 40.0F)
        {
          if (getBless().isHateGod()) {
            toReturn = toReturn + " It has a malevolent aura.";
          } else {
            toReturn = toReturn + " It has a benevolent aura.";
          }
        }
        else {
          toReturn = toReturn + " It bears an aura of " + getBless().name + ".";
        }
      }
    }
    if ((isWood()) && (!isSeedling())) {
      toReturn = toReturn + " It is made from " + getMaterialString(getMaterial()) + ".";
    }
    if (isRoyal())
    {
      Kingdom k = Kingdoms.getKingdom(getKingdom());
      if (k != null) {
        toReturn = toReturn + " It belongs to the " + k.getName() + ".";
      }
    }
    if (getTemplate().isRune()) {
      if (RuneUtilities.isEnchantRune(this)) {
        toReturn = toReturn + " It can be attached to " + RuneUtilities.getAttachmentTargets(this) + " and will " + RuneUtilities.getRuneLongDesc(RuneUtilities.getEnchantForRune(this)) + ".";
      } else if ((RuneUtilities.getModifier(RuneUtilities.getEnchantForRune(this), RuneUtilities.ModifierEffect.SINGLE_COLOR) > 0.0F) || (
        (RuneUtilities.getSpellForRune(this) != null) && (RuneUtilities.getSpellForRune(this).isTargetAnyItem()) && 
        (!RuneUtilities.getSpellForRune(this).isTargetTile()))) {
        toReturn = toReturn + " It can be used on " + RuneUtilities.getAttachmentTargets(this) + " and will " + RuneUtilities.getRuneLongDesc(RuneUtilities.getEnchantForRune(this)) + ".";
      } else {
        toReturn = toReturn + " It will " + RuneUtilities.getRuneLongDesc(RuneUtilities.getEnchantForRune(this)) + ".";
      }
    }
    if (getTemplateId() == 1423) {
      if (getData() != -1L) {
        if (getAuxData() != 0)
        {
          DeadVillage dv = Villages.getDeadVillage(getData());
          
          toReturn = toReturn + dv.getDeedName();
          if (getAuxBit(1))
          {
            toReturn = toReturn + " was founded by " + dv.getFounderName();
            if (getAuxBit(3)) {
              toReturn = toReturn + " and was inhabited for about " + DeadVillage.getTimeString(dv.getTotalAge(), false) + ".";
            } else {
              toReturn = toReturn + ".";
            }
          }
          else if (getAuxBit(3))
          {
            toReturn = toReturn + " was inhabited for about " + DeadVillage.getTimeString(dv.getTotalAge(), false) + ".";
          }
          if (getAuxBit(2))
          {
            if ((getAuxBit(1)) || (getAuxBit(3))) {
              toReturn = toReturn + " It";
            }
            toReturn = toReturn + " has been abandoned for roughly " + DeadVillage.getTimeString(dv.getTimeSinceDisband(), false);
            if (getAuxBit(0)) {
              toReturn = toReturn + " and was last mayored by " + dv.getMayorName() + ".";
            } else {
              toReturn = toReturn + ".";
            }
          }
          else
          {
            if ((getAuxBit(1)) || (getAuxBit(3))) {
              toReturn = toReturn + " It";
            }
            toReturn = toReturn + " was last mayored by " + dv.getMayorName() + ".";
          }
        }
      }
    }
    if ((!isNewbieItem()) && (!isChallengeNewbieItem())) {
      if (getTemplateId() != 1310) {
        toReturn = toReturn + MethodsItems.getImpDesc(performer, this);
      }
    }
    if (isArtifact()) {
      toReturn = toReturn + " It may drop on the ground if you log out.";
    }
    if ((getTemplateId() == 937) || (getTemplateId() == 445)) {
      toReturn = toReturn + " It has been " + (getTemplateId() == 937 ? "weighted " : "winched ") + getWinches() + " times and currently has a firing angle of about " + (45 + getAuxData() * 5) + " degrees.";
    }
    if (((isDecoration()) || (isNoTake())) && (this.ownerId == -10L)) {
      if (getTemplateId() != 1310) {
        toReturn = toReturn + " Ql: " + this.qualityLevel + ", Dam: " + this.damage + ".";
      }
    }
    if (getTemplateId() == 866) {
      try
      {
        toReturn = toReturn + " This came from the " + CreatureTemplateFactory.getInstance().getTemplate(getData2()).getName() + ".";
      }
      catch (NoSuchCreatureTemplateException e)
      {
        logger.warning(String.format("Item %s [id %s] does not have valid blood data.", new Object[] { getName(), Long.valueOf(getWurmId()) }));
      }
    }
    if ((isGem()) && (getData1() > 0))
    {
      int d = getData1();
      if (d < 10) {
        toReturn = toReturn + " It emits faint power.";
      } else if (d < 20) {
        toReturn = toReturn + " It emits some power.";
      } else if (d < 50) {
        toReturn = toReturn + " It emits power.";
      } else if (d < 100) {
        toReturn = toReturn + " It emits quite a lot of power.";
      } else if (d < 150) {
        toReturn = toReturn + " It emits very much power.";
      } else {
        toReturn = toReturn + " It emits huge amounts of power.";
      }
    }
    if (isArtifact())
    {
      toReturn = toReturn + " It ";
      
      int powerPercent = (int)Math.floor(this.auxbyte * 1.0F / 30.0F * 100.0F);
      if (!ArtifactBehaviour.mayUseItem(this, null)) {
        toReturn = toReturn + "seems dormant but ";
      }
      if (powerPercent > 99) {
        toReturn = toReturn + "emits an enormous sense of power.";
      } else if (powerPercent > 82) {
        toReturn = toReturn + "emits a huge sense of power.";
      } else if (powerPercent > 65) {
        toReturn = toReturn + "emits a strong sense of power.";
      } else if (powerPercent > 48) {
        toReturn = toReturn + "emits a fair sense of power.";
      } else if (powerPercent > 31) {
        toReturn = toReturn + "emits some sense of power.";
      } else if (powerPercent > 14) {
        toReturn = toReturn + "emits a weak sense of power.";
      } else {
        toReturn = toReturn + "emits almost no sense of power.";
      }
      if (this.auxbyte <= 20) {
        if (this.auxbyte > 10) {
          toReturn = toReturn + " It will need to be recharged at the huge altar eventually.";
        } else if ((this.auxbyte <= 10) && (this.auxbyte > 0)) {
          toReturn = toReturn + " It will need to be recharged at the huge altar soon.";
        } else {
          toReturn = toReturn + " It will need to be recharged at the huge altar immediately or it will disappear.";
        }
      }
      if (performer.getPower() > 0) {
        toReturn = toReturn + " " + this.auxbyte + " charges remain. (" + powerPercent + "%)";
      }
    }
    if (getTemplateId() == 538)
    {
      if (King.getKing((byte)2) == null) {
        toReturn = toReturn + " It is occupied by a sword.";
      }
    }
    else if (getTemplateId() == 654)
    {
      boolean needBless = getBless() == null;
      switch (getAuxData())
      {
      case 1: 
        toReturn = toReturn + (needBless ? " Once blessed can" : " Can") + " help convert a sand tile to a clay tile.";
        break;
      case 2: 
        toReturn = toReturn + (needBless ? " Once blessed can" : " Can") + " help convert a grass or mycelium tile to a peat tile.";
        break;
      case 3: 
        toReturn = toReturn + (needBless ? " Once blessed can" : " Can") + " help convert a steppe tile to a tar tile.";
        break;
      case 4: 
        toReturn = toReturn + (needBless ? " Once blessed can" : " Can") + " help convert a clay tile to a dirt tile.";
        break;
      case 5: 
        toReturn = toReturn + (needBless ? " Once blessed can" : " Can") + " help convert a peat tile to a dirt tile.";
        break;
      case 6: 
        toReturn = toReturn + (needBless ? " Once blessed can" : " Can") + " help convert a tar tile to a dirt tile.";
      }
    }
    else if (getTemplateId() == 1101)
    {
      if (getAuxData() < 10) {
        toReturn = toReturn + " The bottle has something like " + (10 - getAuxData()) + " drinks left.";
      } else {
        toReturn = toReturn + " The bottle is empty.";
      }
    }
    if (getTemplateId() == 1162)
    {
      String growing = "unknown";
      try
      {
        growing = ItemTemplateFactory.getInstance().getTemplate(this.realTemplate).getName();
      }
      catch (NoSuchTemplateException nst)
      {
        logInfo("No template for " + getName() + ", id=" + this.realTemplate);
      }
      int age = getAuxData() & 0x7F;
      if (age == 0) {
        toReturn = toReturn + " you see bare dirt, maybe its too early to see whats growing.";
      } else if (age < 5) {
        toReturn = toReturn + " you see some shoots poking through the dirt.";
      } else if (age < 10) {
        toReturn = toReturn + " you see some shoots of " + growing + ".";
      } else if (age < 65) {
        toReturn = toReturn + " you see some " + growing + " growing, looks in its prime of life.";
      } else if (age < 75) {
        toReturn = toReturn + " you see some " + growing + " growing, looks a bit old now.";
      } else if (age < 95) {
        toReturn = toReturn + " you see some " + growing + " growing, looks ready to be picked.";
      } else {
        toReturn = toReturn + " you see woody " + growing + ", looks like it needs replacing.";
      }
    }
    if (((getTemplateId() == 490) || (getTemplateId() == 491)) && 
      (getExtra() != -1L)) {
      toReturn = toReturn + " It has a keep net attached to it for storing freshly caught fish.";
    }
    if (isMooredBoat()) {
      toReturn = toReturn + " It is moored here.";
    }
    if (getTemplateId() == 464) {
      if (getData1() > 0) {
        toReturn = toReturn + " You sense it could be fertile.";
      } else {
        toReturn = toReturn + " You sense that it is infertile.";
      }
    }
    if ((isFood()) || (isLiquid()))
    {
      float nut = getNutritionLevel();
      if (isWrapped())
      {
        if ((canBePapyrusWrapped()) || (canBeClothWrapped()))
        {
          toReturn = toReturn + " It has been wrapped to reduce decay.";
          return toReturn;
        }
        if (canBeRawWrapped())
        {
          toReturn = toReturn + " It has been wrapped ready to cook in a cooker of some kind.";
          return toReturn;
        }
      }
      if (nut > 0.9D) {
        toReturn = toReturn + " This has a high nutrition value.";
      } else if (nut > 0.7D) {
        toReturn = toReturn + " This has a good nutrition value.";
      } else if (nut > 0.5D) {
        toReturn = toReturn + " This has a medium nutrition value.";
      } else if (nut > 0.3D) {
        toReturn = toReturn + " This has a poor nutrition value.";
      } else {
        toReturn = toReturn + " This has a very low nutrition value.";
      }
      if (isSalted()) {
        toReturn = toReturn + " Tastes like it has some salt in it.";
      }
      if (recipe != null) {
        if (performer.getSkills().getSkillOrLearn(recipe.getSkillId()).getKnowledge(0.0D) > 30.0D)
        {
          String creat = getCreatorName();
          if (creat.length() > 0) {
            toReturn = toReturn + " Made by " + creat + " on " + WurmCalendar.getDateFor(this.creationDate);
          } else {
            toReturn = toReturn + " Created on " + WurmCalendar.getDateFor(this.creationDate);
          }
        }
      }
      if ((performer.getPower() > 1) || (performer.hasFlag(51))) {
        toReturn = toReturn + " (testers only: Calories:" + getCaloriesByWeight() + ", Carbs:" + getCarbsByWeight() + ", Fats:" + getFatsByWeight() + ", Proteins:" + getProteinsByWeight() + ", Bonus:" + (getBonus() & 0xFF) + ", Nutrition:" + (int)(nut * 100.0F) + "%" + (recipe != null ? ", Recipe:" + recipe.getName() + " (" + recipe.getRecipeId() + ")" : "") + ", Stages:" + getFoodStages() + ", Ingredients:" + getFoodIngredients() + ".)";
      }
    }
    if ((getTemplateId() == 1175) || (getTemplateId() == 1239)) {
      switch (getAuxData())
      {
      case 0: 
        toReturn = toReturn + " You cannot hear or see any activity around the hive.";
        break;
      case 1: 
        toReturn = toReturn + " You see and hear bees flying in and around the hive.";
        break;
      case 2: 
        toReturn = toReturn + " You see and hear there is more than the usual activity in the hive, could be there is a queen about to leave.";
      }
    }
    if (WurmId.getType(this.lastOwner) == 1)
    {
      Wagoner wagoner = Wagoner.getWagoner(this.lastOwner);
      if (wagoner != null)
      {
        toReturn = toReturn + " This is owned by " + wagoner.getName() + ".";
        if (getTemplateId() == 1112) {
          setData(-10L);
        }
      }
    }
    return toReturn;
  }
  
  public final byte getEnchantmentDamageType()
  {
    if ((this.enchantment == 91) || (this.enchantment == 90) || (this.enchantment == 92)) {
      return this.enchantment;
    }
    return 0;
  }
  
  public final void sendColoredSalveImbue(Communicator comm, String salveName, String damageType, byte color)
  {
    ArrayList<MulticolorLineSegment> segments = new ArrayList();
    segments.add(new MulticolorLineSegment("It is imbued with special abilities from a ", (byte)0));
    segments.add(new MulticolorLineSegment(salveName, (byte)16));
    segments.add(new MulticolorLineSegment(" and will deal ", (byte)0));
    segments.add(new MulticolorLineSegment(damageType, color));
    segments.add(new MulticolorLineSegment(" damage.", (byte)0));
    comm.sendColoredMessageEvent(segments);
  }
  
  public final void sendColoredDemise(Communicator comm, String demiseName, String targetName)
  {
    ArrayList<MulticolorLineSegment> segments = new ArrayList();
    segments.add(new MulticolorLineSegment("It is enchanted with ", (byte)0));
    segments.add(new MulticolorLineSegment(demiseName, (byte)16));
    segments.add(new MulticolorLineSegment(" and will be more effective against ", (byte)0));
    segments.add(new MulticolorLineSegment(targetName, (byte)17));
    segments.add(new MulticolorLineSegment(".", (byte)0));
    comm.sendColoredMessageEvent(segments);
  }
  
  public final void sendColoredSmear(Communicator comm, SpellEffect eff)
  {
    ArrayList<MulticolorLineSegment> segments = new ArrayList();
    segments.add(new MulticolorLineSegment("It has been smeared with a ", (byte)0));
    segments.add(new MulticolorLineSegment(eff.getName(), (byte)16));
    segments.add(new MulticolorLineSegment(", and it ", (byte)0));
    segments.add(new MulticolorLineSegment(eff.getLongDesc(), (byte)17));
    segments.add(new MulticolorLineSegment(" [", (byte)0));
    segments.add(new MulticolorLineSegment(String.format("%d", new Object[] { Integer.valueOf((int)eff.getPower()) }), (byte)18));
    segments.add(new MulticolorLineSegment("]", (byte)0));
    comm.sendColoredMessageEvent(segments);
  }
  
  public final void sendColoredRune(Communicator comm, SpellEffect eff)
  {
    ArrayList<MulticolorLineSegment> segments = new ArrayList();
    segments.add(new MulticolorLineSegment("A ", (byte)0));
    segments.add(new MulticolorLineSegment(eff.getName(), (byte)16));
    segments.add(new MulticolorLineSegment(" has been attached, so it ", (byte)0));
    segments.add(new MulticolorLineSegment(eff.getLongDesc(), (byte)17));
    comm.sendColoredMessageEvent(segments);
  }
  
  public final void sendColoredEnchant(Communicator comm, SpellEffect eff)
  {
    ArrayList<MulticolorLineSegment> segments = new ArrayList();
    segments.add(new MulticolorLineSegment(eff.getName(), (byte)16));
    segments.add(new MulticolorLineSegment(" has been cast on it, so it ", (byte)0));
    segments.add(new MulticolorLineSegment(eff.getLongDesc(), (byte)17));
    segments.add(new MulticolorLineSegment(" [", (byte)0));
    segments.add(new MulticolorLineSegment(String.format("%d", new Object[] { Integer.valueOf((int)eff.getPower()) }), (byte)18));
    segments.add(new MulticolorLineSegment("]", (byte)0));
    comm.sendColoredMessageEvent(segments);
  }
  
  public final void sendEnchantmentStrings(Communicator comm)
  {
    if (this.enchantment != 0) {
      if (this.enchantment == 91)
      {
        sendColoredSalveImbue(comm, "salve of fire", "fire", (byte)20);
      }
      else if (this.enchantment == 92)
      {
        sendColoredSalveImbue(comm, "salve of frost", "frost", (byte)21);
      }
      else if (this.enchantment == 90)
      {
        sendColoredSalveImbue(comm, "potion of acid", "acid", (byte)19);
      }
      else
      {
        Spell ench = Spells.getEnchantment(this.enchantment);
        if (ench != null) {
          if (ench == Spells.SPELL_DEMISE_ANIMAL) {
            sendColoredDemise(comm, ench.getName(), "animals");
          } else if (ench == Spells.SPELL_DEMISE_LEGENDARY) {
            sendColoredDemise(comm, ench.getName(), "legendary creatures");
          } else if (ench == Spells.SPELL_DEMISE_MONSTER) {
            sendColoredDemise(comm, ench.getName(), "monsters");
          } else if (ench == Spells.SPELL_DEMISE_HUMAN) {
            sendColoredDemise(comm, ench.getName(), "humans");
          } else {
            comm.sendNormalServerMessage("It is enchanted with " + ench.name + ", and " + ench.effectdesc);
          }
        }
      }
    }
    ItemSpellEffects eff = getSpellEffects();
    if (eff != null)
    {
      SpellEffect[] speffs = eff.getEffects();
      for (SpellEffect speff : speffs) {
        if (speff.isSmeared()) {
          sendColoredSmear(comm, speff);
        } else if (speff.type < -10L) {
          sendColoredRune(comm, speff);
        } else {
          sendColoredEnchant(comm, speff);
        }
      }
    }
  }
  
  public final void sendExtraStrings(Communicator comm)
  {
    if (Features.Feature.TOWER_CHAINING.isEnabled()) {
      if ((getTemplateId() == 236) || (isKingdomMarker())) {
        if (isChained()) {
          comm.sendNormalServerMessage(String.format("The %s is chained to the kingdom influence.", new Object[] {
            getName() }));
        } else {
          comm.sendNormalServerMessage(String.format("The %s is not chained to the kingdom influence.", new Object[] {
            getName() }));
        }
      }
    }
  }
  
  public final float getSpellEffectPower(byte aEnchantment)
  {
    ItemSpellEffects eff = getSpellEffects();
    if (eff != null)
    {
      SpellEffect skillgain = eff.getSpellEffect(aEnchantment);
      if (skillgain != null) {
        return skillgain.getPower();
      }
    }
    return 0.0F;
  }
  
  public final float getSkillSpellImprovement(int skillNum)
  {
    switch (skillNum)
    {
    case 1014: 
      return getSpellEffectPower((byte)78);
    case 1008: 
      return getSpellEffectPower((byte)79);
    case 1016: 
    case 10010: 
    case 10011: 
      return getSpellEffectPower((byte)77);
    case 10016: 
      return getSpellEffectPower((byte)80);
    case 10012: 
    case 10013: 
    case 10014: 
      return getSpellEffectPower((byte)81);
    case 1031: 
    case 1032: 
      return getSpellEffectPower((byte)82);
    case 10015: 
      return getSpellEffectPower((byte)83);
    case 10017: 
      return getSpellEffectPower((byte)84);
    case 10082: 
      return getSpellEffectPower((byte)85);
    case 10074: 
      return getSpellEffectPower((byte)86);
    case 1013: 
      return getSpellEffectPower((byte)87);
    case 1007: 
      return getSpellEffectPower((byte)88);
    case 1005: 
    case 10044: 
      return getSpellEffectPower((byte)89);
    case 10059: 
      return getSpellEffectPower((byte)99);
    }
    return 0.0F;
  }
  
  public final String getExamineAsBml(Creature performer)
  {
    StringBuilder buf = new StringBuilder();
    
    buf.append("text{text=\"" + examine(performer) + "\"};");
    if (this.enchantment != 0)
    {
      Spell ench = Spells.getEnchantment(this.enchantment);
      if (ench != null) {
        buf.append("text{text=\"It is enchanted with " + ench.name + ", and " + ench.effectdesc + "\"};");
      }
    }
    ItemSpellEffects eff = getSpellEffects();
    if (eff != null)
    {
      SpellEffect[] speffs = eff.getEffects();
      for (SpellEffect speff : speffs) {
        buf.append("text{text=\"" + speff.getName() + " has been cast on it, so it " + speff.getLongDesc() + " [" + (int)speff.power + "]\"};");
      }
    }
    return buf.toString();
  }
  
  public final float getSpellSkillBonus()
  {
    if ((isArtifact()) && (isWeapon())) {
      return 99.0F;
    }
    if (getBonusForSpellEffect((byte)13) > 0.0F) {
      return getBonusForSpellEffect((byte)13);
    }
    return getBonusForSpellEffect((byte)47);
  }
  
  public final float getWeaponSpellDamageBonus()
  {
    if ((isArtifact()) && (getTemplateId() == 340)) {
      return 99.0F;
    }
    return getBonusForSpellEffect((byte)18) * ItemBonus.getWeaponSpellDamageIncreaseBonus(this.ownerId);
  }
  
  public final float getSpellRotModifier()
  {
    if ((isArtifact()) && (getTemplateId() == 340)) {
      return 99.0F;
    }
    return getBonusForSpellEffect((byte)18) * ItemBonus.getWeaponSpellDamageIncreaseBonus(this.ownerId);
  }
  
  public final float getSpellFrostDamageBonus()
  {
    return getBonusForSpellEffect((byte)33) * ItemBonus.getWeaponSpellDamageIncreaseBonus(this.ownerId);
  }
  
  public final float getSpellExtraDamageBonus()
  {
    return getBonusForSpellEffect((byte)45) * ItemBonus.getWeaponSpellDamageIncreaseBonus(this.ownerId);
  }
  
  public final float getSpellEssenceDrainModifier()
  {
    return getBonusForSpellEffect((byte)63);
  }
  
  public final float getSpellLifeTransferModifier()
  {
    if ((isArtifact()) && (getTemplateId() == 337)) {
      return 99.0F;
    }
    return getBonusForSpellEffect((byte)26);
  }
  
  public final float getSpellMindStealModifier()
  {
    return getBonusForSpellEffect((byte)31);
  }
  
  public final float getSpellNimbleness()
  {
    if ((isArtifact()) && (isWeapon())) {
      return 99.0F;
    }
    return getBonusForSpellEffect((byte)32);
  }
  
  public final float getSpellDamageBonus()
  {
    if ((isArtifact()) && (isWeaponSword())) {
      return 99.0F;
    }
    return getBonusForSpellEffect((byte)14) * ItemBonus.getWeaponSpellDamageIncreaseBonus(this.ownerId);
  }
  
  public final float getSpellVenomBonus()
  {
    return getBonusForSpellEffect((byte)27) * ItemBonus.getWeaponSpellDamageIncreaseBonus(this.ownerId);
  }
  
  public final float getSpellPainShare()
  {
    if ((isArtifact()) && ((isShield()) || (isArmour()))) {
      return 80.0F;
    }
    return getBonusForSpellEffect((byte)17);
  }
  
  public final float getNolocateBonus()
  {
    if ((isArtifact()) && 
      (getTemplateId() == 329)) {
      return 99.0F;
    }
    return getBonusForSpellEffect((byte)29);
  }
  
  public final float getSpellSlowdown()
  {
    if (((isArtifact()) && (isShield())) || ((isRoyal()) && (isArmour()))) {
      return 99.0F;
    }
    return getBonusForSpellEffect((byte)46);
  }
  
  public final float getSpellFoodBonus()
  {
    return getBonusForSpellEffect((byte)15);
  }
  
  public final float getSpellLocFishBonus()
  {
    return getBonusForSpellEffect((byte)48);
  }
  
  public final float getSpellLocEnemyBonus()
  {
    return getBonusForSpellEffect((byte)50);
  }
  
  public final float getSpellLocChampBonus()
  {
    return getBonusForSpellEffect((byte)49);
  }
  
  public final boolean isLocateItem()
  {
    return (getBonusForSpellEffect((byte)50) > 0.0F) || 
      (getBonusForSpellEffect((byte)48) > 0.0F) || 
      (getBonusForSpellEffect((byte)49) > 0.0F);
  }
  
  public final float getSpellSpeedBonus()
  {
    if (getBonusForSpellEffect((byte)16) > 0.0F) {
      return getBonusForSpellEffect((byte)16);
    }
    return getBonusForSpellEffect((byte)47);
  }
  
  public final float getSpellCourierBonus()
  {
    if (getBonusForSpellEffect((byte)20) <= 0.0F) {
      return getBonusForSpellEffect((byte)44);
    }
    return getBonusForSpellEffect((byte)20);
  }
  
  public final float getSpellDarkMessengerBonus()
  {
    return getBonusForSpellEffect((byte)44);
  }
  
  public final float getBonusForSpellEffect(byte aEnchantment)
  {
    ItemSpellEffects eff = getSpellEffects();
    if (eff != null)
    {
      SpellEffect skillgain = eff.getSpellEffect(aEnchantment);
      if (skillgain != null) {
        return skillgain.power;
      }
    }
    return 0.0F;
  }
  
  public final SpellEffect getSpellEffect(byte aEnchantment)
  {
    ItemSpellEffects eff = getSpellEffects();
    if (eff != null)
    {
      SpellEffect skillgain = eff.getSpellEffect(aEnchantment);
      if (skillgain != null) {
        return skillgain;
      }
    }
    return null;
  }
  
  public final int getDamagePercent()
  {
    if (getWeaponSpellDamageBonus() > 0.0F) {
      return this.template.getDamagePercent() + (int)(5.0F * getWeaponSpellDamageBonus() / 100.0F);
    }
    return this.template.getDamagePercent();
  }
  
  public final int getFullWeight()
  {
    return getFullWeight(false);
  }
  
  public final int getFullWeight(boolean calcCorrectBulkWeight)
  {
    int lWeight = getWeightGrams();
    ItemTemplate temp;
    if ((calcCorrectBulkWeight) && (isBulkItem()))
    {
      float nums = getBulkNumsFloat(true);
      temp = getRealTemplate();
      if (temp != null) {
        lWeight = (int)(temp.getWeightGrams() * nums);
      }
    }
    if (isHollow())
    {
      Set<Item> allItems = getItems();
      for (Item it : allItems) {
        if (it != this) {
          lWeight += it.getFullWeight(calcCorrectBulkWeight);
        } else {
          logWarn(getName() + " Wurmid=" + getWurmId() + " contains itself!");
        }
      }
    }
    return lWeight;
  }
  
  public final byte[] getBodySpaces()
  {
    return this.template.getBodySpaces();
  }
  
  public final long[] getKeyIds()
  {
    if ((this.keys == null) || (this.keys.isEmpty())) {
      return EMPTY_LONG_PRIMITIVE_ARRAY;
    }
    long[] keyids = new long[this.keys.size()];
    int x = 0;
    for (Long key : this.keys)
    {
      keyids[x] = key.longValue();
      x++;
    }
    return keyids;
  }
  
  public final void addKey(long keyid)
  {
    if (this.keys == null) {
      this.keys = new HashSet();
    }
    if (!this.keys.contains(Long.valueOf(keyid)))
    {
      this.keys.add(Long.valueOf(keyid));
      addNewKey(keyid);
    }
  }
  
  public final void removeKey(long keyid)
  {
    if (this.keys == null) {
      return;
    }
    if (!this.keys.contains(Long.valueOf(keyid))) {
      return;
    }
    this.keys.remove(Long.valueOf(keyid));
    removeNewKey(keyid);
  }
  
  public final boolean isUnlockedBy(long keyId)
  {
    if ((this.keys != null) && (keyId != -10L)) {
      return this.keys.contains(Long.valueOf(keyId));
    }
    return false;
  }
  
  public final void lock()
  {
    setLocked(true);
  }
  
  public final void unlock()
  {
    setLocked(false);
  }
  
  public boolean isEquipmentSlot()
  {
    return this.template.isEquipmentSlot();
  }
  
  public final boolean isLocked()
  {
    if (isKey()) {
      return false;
    }
    if (isLock()) {
      return getLocked();
    }
    if ((!isLockable()) || (this.lockid == -10L)) {
      return false;
    }
    try
    {
      Item lock = Items.getItem(this.lockid);
      
      boolean isAffectedLock = (lock.getTemplateId() == 568) || (lock.getTemplateId() == 194) || (lock.getTemplateId() == 193);
      if ((!lock.isLocked()) && (isAffectedLock))
      {
        logInfo(getName() + "(" + getWurmId() + ") had lock (" + lock.getWurmId() + ") that was unlocked. So was auto-locked as should not have been in that state.");
        
        lock.setLocked(true);
      }
      return lock.isLocked();
    }
    catch (NoSuchItemException e)
    {
      logWarn(getName() + "," + getWurmId() + ":" + e.getMessage(), e);
    }
    return false;
  }
  
  public boolean isServerPortal()
  {
    return this.template.isServerPortal;
  }
  
  public final boolean isColor()
  {
    return this.template.isColor;
  }
  
  public final boolean templateIsColorable()
  {
    return this.template.colorable;
  }
  
  public final boolean isPuppet()
  {
    return this.template.puppet;
  }
  
  public final boolean isOverrideNonEnchantable()
  {
    return this.template.overrideNonEnchantable;
  }
  
  public final int getDragonColor()
  {
    int creatureTemplate = getData2();
    switch (creatureTemplate)
    {
    case 16: 
    case 103: 
      return WurmColor.createColor(215, 40, 40);
    case 18: 
    case 89: 
      return WurmColor.createColor(10, 10, 10);
    case 17: 
    case 90: 
      return WurmColor.createColor(10, 210, 10);
    case 19: 
    case 92: 
      return WurmColor.createColor(255, 255, 255);
    case 91: 
    case 104: 
      return WurmColor.createColor(40, 40, 215);
    }
    return WurmColor.createColor(100, 100, 100);
  }
  
  public final String getDragonColorNameByColor(int color)
  {
    if (color == WurmColor.createColor(215, 40, 40)) {
      return "red";
    }
    if (color == WurmColor.createColor(10, 10, 10)) {
      return "black";
    }
    if (color == WurmColor.createColor(10, 210, 10)) {
      return "green";
    }
    if (color == WurmColor.createColor(255, 255, 255)) {
      return "white";
    }
    if (color == WurmColor.createColor(40, 40, 215)) {
      return "blue";
    }
    return "";
  }
  
  public final String getDragonColorName()
  {
    try
    {
      CreatureTemplate temp = CreatureTemplateFactory.getInstance().getTemplate(getData2());
      String pre = temp.getName();
      StringTokenizer st = new StringTokenizer(pre);
      pre = st.nextToken();
      return pre.toLowerCase();
    }
    catch (Exception localException) {}
    return "";
  }
  
  public final String getLockStrength()
  {
    String lockStrength = "fantastic";
    float lQualityLevel = getCurrentQualityLevel();
    
    int qlDivTen = (int)lQualityLevel / 10;
    switch (qlDivTen)
    {
    case 0: 
      lockStrength = "very poor"; break;
    case 1: 
      lockStrength = "poor"; break;
    case 2: 
      lockStrength = "below average"; break;
    case 3: 
      lockStrength = "okay"; break;
    case 4: 
      lockStrength = "above average"; break;
    case 5: 
      lockStrength = "pretty good"; break;
    case 6: 
      lockStrength = "good"; break;
    case 7: 
      lockStrength = "very good"; break;
    case 8: 
      lockStrength = "exceptional"; break;
    default: 
      lockStrength = "fantastic";
    }
    return lockStrength;
  }
  
  public final void setSizes(int aSizeX, int aSizeY, int aSizeZ)
  {
    if ((aSizeX == 0) || (aSizeY == 0) || (aSizeZ == 0))
    {
      Items.destroyItem(this.id);
      return;
    }
    setSizeX(aSizeX);
    setSizeY(aSizeY);
    setSizeZ(aSizeZ);
    
    sendStatus();
  }
  
  final boolean depleteSizeWith(int aSizeX, int aSizeY, int aSizeZ)
  {
    int prevSizeX = getSizeX();
    int prevSizeY = getSizeY();
    int prevSizeZ = getSizeZ();
    
    int prevol = prevSizeX * prevSizeY * prevSizeZ;
    int vol = aSizeX * aSizeY * aSizeZ;
    if (logger.isLoggable(Level.FINER)) {
      logger.finer("id: " + this.id + ", vol: " + vol + " prevol: " + prevol + ' ' + prevSizeX + ' ' + prevSizeY + ' ' + prevSizeZ);
    }
    if (vol >= prevol)
    {
      Items.destroyItem(this.id);
      return true;
    }
    float factor = vol / prevol;
    
    prevSizeX = Math.max(1, prevSizeX - (int)(prevSizeX * factor));
    prevSizeY = Math.max(1, prevSizeY - (int)(prevSizeY * factor));
    prevSizeZ = Math.max(1, prevSizeZ - (int)(prevSizeZ * factor));
    
    int newPrevSz = prevSizeZ;
    int newPrevSy = prevSizeY;
    int newPrevSx = prevSizeX;
    if (prevSizeZ < prevSizeY)
    {
      newPrevSz = prevSizeY;
      newPrevSy = prevSizeZ;
    }
    if (prevSizeZ < prevSizeX)
    {
      newPrevSx = prevSizeZ;
      newPrevSy = prevSizeX;
    }
    if (prevSizeY < prevSizeX)
    {
      newPrevSy = prevSizeY;
      newPrevSz = prevSizeX;
    }
    setSizeX(newPrevSx);
    setSizeY(newPrevSy);
    setSizeZ(newPrevSz);
    
    sendStatus();
    return false;
  }
  
  public final void sleep(Creature sleeper, boolean epicServer)
    throws IOException
  {
    if ((this.template.artifact) && (sleeper != null))
    {
      if (getOwnerId() == sleeper.getWurmId()) {
        sleeper.dropItem(this);
      }
      return;
    }
    if (isHollow())
    {
      Item[] allItems = getAllItems(true, false);
      for (Item allit : allItems) {
        if (allit.hasDroppableItem(epicServer)) {
          allit.sleep(sleeper, epicServer);
        } else {
          allit.sleepNonRecursive(sleeper, epicServer);
        }
      }
    }
    if (!this.template.alwaysLoaded) {
      Items.removeItem(this.id);
    }
  }
  
  public final boolean hasDroppableItem(boolean epicServer)
  {
    if (!isHollow()) {
      return false;
    }
    for (Item i : this.items) {
      if (i.isArtifact()) {
        return true;
      }
    }
    return false;
  }
  
  public final void sleepNonRecursive(Creature sleeper, boolean epicServer)
    throws IOException
  {
    if ((this.template.artifact) && (sleeper != null))
    {
      if (getOwnerId() == sleeper.getWurmId()) {
        sleeper.dropItem(this);
      }
      return;
    }
    if (!this.template.alwaysLoaded) {
      Items.removeItem(this.id);
    }
  }
  
  public final Item[] getAllItems(boolean getLockedItems)
  {
    return getAllItems(getLockedItems, true);
  }
  
  @Nonnull
  public final Item[] getAllItems(boolean getLockedItems, boolean loadArtifacts)
  {
    if (!isHollow()) {
      return emptyItems;
    }
    if ((this.lockid != -10L) && (!getLockedItems)) {
      return emptyItems;
    }
    if ((!loadArtifacts) && (this.template.artifact)) {
      return emptyItems;
    }
    Set<Item> allItems = new HashSet();
    for (Item item : getItems())
    {
      allItems.add(item);
      
      Collections.addAll(allItems, item.getAllItems(getLockedItems, loadArtifacts));
    }
    return (Item[])allItems.toArray(new Item[allItems.size()]);
  }
  
  public final Item findFirstContainedItem(int templateid)
  {
    Item[] its = getAllItems(false);
    for (int x = 0; x < its.length; x++) {
      if (its[x].getTemplateId() == templateid) {
        return its[x];
      }
    }
    return null;
  }
  
  @Nullable
  public final Item findItem(int templateid)
  {
    for (Item item : getItems()) {
      if (item.getTemplateId() == templateid) {
        return item;
      }
    }
    return null;
  }
  
  public final Item findItem(int templateid, boolean searchInGroups)
  {
    for (Item item : getItems())
    {
      if (item.getTemplateId() == templateid) {
        return item;
      }
      if ((item.isInventoryGroup()) && (searchInGroups))
      {
        Item inGroup = item.findItem(templateid, false);
        if (inGroup != null) {
          return inGroup;
        }
      }
    }
    return null;
  }
  
  public final float getCurrentQualityLevel()
  {
    return this.qualityLevel * Math.max(1.0F, 100.0F - this.damage) / 100.0F;
  }
  
  public final byte getRadius()
  {
    if (getSpellEffects() != null)
    {
      float modifier = getSpellEffects().getRuneEffect(RuneUtilities.ModifierEffect.ENCH_GLOW) - 1.0F;
      if (modifier > 0.0F) {
        return (byte)(int)Math.min(127.0F, 127.0F * modifier - 20.0F);
      }
    }
    float ql = getCurrentQualityLevel();
    boolean qlAbove20 = ql > 20.0F;
    float baseRange = ql / (qlAbove20 ? 100.0F : 20.0F);
    if (isLightBright()) {
      return (byte)(int)(qlAbove20 ? baseRange * 127.0F : (baseRange - 1.0F) * 32.0F);
    }
    return (byte)(int)(qlAbove20 ? baseRange * 64.0F : (baseRange - 1.0F) * 64.0F);
  }
  
  public final boolean isCrystal()
  {
    return (this.material == 52) || (isDiamond());
  }
  
  public final boolean isDiamond()
  {
    return this.material == 54;
  }
  
  private float getMaterialDaleModifier()
  {
    switch (getMaterial())
    {
    case 64: 
      return 1.1F;
    }
    return 1.0F;
  }
  
  private float getMaterialDamageModifier()
  {
    if (Features.Feature.METALLIC_ITEMS.isEnabled())
    {
      switch (getMaterial())
      {
      case 56: 
        return 0.4F;
      case 30: 
        return 0.95F;
      case 31: 
        return 0.9F;
      case 10: 
        return 1.15F;
      case 57: 
        return 0.6F;
      case 7: 
        return 1.2F;
      case 12: 
        return 1.3F;
      case 67: 
        return 0.5F;
      case 8: 
        return 1.025F;
      case 9: 
        return 0.8F;
      case 34: 
        return 1.2F;
      case 13: 
        return 1.25F;
      case 96: 
        return 0.9F;
      case 38: 
        return 0.8F;
      case 35: 
        return 0.2F;
      }
    }
    else
    {
      if (getMaterial() == 9) {
        return 0.8F;
      }
      if (getMaterial() == 57) {
        return 0.6F;
      }
      if (getMaterial() == 56) {
        return 0.4F;
      }
    }
    if (isFishingLine()) {
      switch (getTemplateId())
      {
      case 1347: 
        return 1.2F;
      case 1348: 
        return 1.0F;
      case 1349: 
        return 0.9F;
      case 1350: 
        return 0.8F;
      case 1351: 
        return 0.7F;
      }
    }
    return 1.0F;
  }
  
  private float getMaterialDecayModifier()
  {
    if (Features.Feature.METALLIC_ITEMS.isEnabled())
    {
      switch (getMaterial())
      {
      case 56: 
        return 0.4F;
      case 30: 
        return 0.95F;
      case 31: 
        return 0.85F;
      case 10: 
        return 0.95F;
      case 57: 
        return 0.6F;
      case 7: 
        return 0.4F;
      case 12: 
        return 0.8F;
      case 67: 
        return 0.5F;
      case 8: 
        return 0.7F;
      case 9: 
        return 0.7F;
      case 34: 
        return 0.925F;
      case 13: 
        return 1.2F;
      case 96: 
        return 0.8F;
      case 38: 
        return 0.8F;
      case 35: 
        return 0.9F;
      }
    }
    else
    {
      if (getMaterial() == 9) {
        return 0.8F;
      }
      if (getMaterial() == 57) {
        return 0.6F;
      }
      if (getMaterial() == 56) {
        return 0.4F;
      }
    }
    return 1.0F;
  }
  
  private float getMaterialFlexibiltyModifier()
  {
    switch (getMaterial())
    {
    case 40: 
      return 0.7F;
    }
    return 1.0F;
  }
  
  private float getMaterialDecayTimeModifier()
  {
    switch (getMaterial())
    {
    case 39: 
      return 1.5F;
    }
    return 1.0F;
  }
  
  public int getMaterialBowDifficulty()
  {
    switch (getMaterial())
    {
    case 40: 
      return 5;
    }
    return 0;
  }
  
  public int getMaterialArrowDifficulty()
  {
    switch (getMaterial())
    {
    case 39: 
      return 5;
    case 41: 
      return 3;
    }
    return 0;
  }
  
  public float getMaterialArrowDamageModifier()
  {
    switch (getMaterial())
    {
    case 41: 
      return 0.8F;
    }
    return 1.0F;
  }
  
  private float getMaterialAgingModifier()
  {
    switch (getMaterial())
    {
    case 38: 
      return 1.1F;
    }
    return 1.05F;
  }
  
  public float getMaterialFragrantModifier()
  {
    switch (getMaterial())
    {
    case 39: 
      return 0.9F;
    case 65: 
      return 0.85F;
    case 37: 
      return 0.95F;
    case 43: 
      return 0.75F;
    case 88: 
      return 0.75F;
    case 42: 
      return 0.8F;
    case 51: 
      return 0.8F;
    }
    return 1.0F;
  }
  
  public final float getDamageModifier()
  {
    return getDamageModifier(false);
  }
  
  public final float getDamageModifier(boolean decayDamage)
  {
    return getDamageModifier(decayDamage, false);
  }
  
  public final float getDamageModifier(boolean decayDamage, boolean flexibilityDamage)
  {
    float rotMod = 1.0F;
    float materialMod = 1.0F;
    if (decayDamage) {
      materialMod *= getMaterialDecayModifier();
    } else if (flexibilityDamage) {
      materialMod *= getMaterialFlexibiltyModifier();
    } else {
      materialMod *= getMaterialDamageModifier();
    }
    if (getSpellRotModifier() > 0.0F) {
      rotMod += getSpellRotModifier() / 100.0F;
    }
    if (isCrude()) {
      rotMod *= 10.0F;
    }
    if (isCrystal())
    {
      rotMod *= 0.1F;
    }
    else if (isFood())
    {
      if (isHighNutrition()) {
        rotMod += (isSalted() ? 5 : 10);
      }
      if (isGoodNutrition()) {
        rotMod += (isSalted() ? 2 : 5);
      }
      if (isMediumNutrition()) {
        rotMod = (float)(rotMod + (isSalted() ? 1.5D : 3.0D));
      }
    }
    if (isInTacklebox()) {
      rotMod *= 0.5F;
    }
    Item parent = getParentOrNull();
    if ((parent != null) && (parent.getTemplateId() == 1342)) {
      rotMod *= 0.5F;
    }
    if (getRarity() > 0) {
      rotMod = (float)(rotMod * Math.pow(0.9D, getRarity()));
    }
    if (getSpellEffects() != null) {
      rotMod *= getSpellEffects().getRuneEffect(RuneUtilities.ModifierEffect.ENCH_DECAY);
    }
    return 100.0F * rotMod / Math.max(1.0F, this.qualityLevel * (100.0F - this.damage) / 100.0F) * materialMod;
  }
  
  public final boolean isTraded()
  {
    return this.tradeWindow != null;
  }
  
  public final void setTradeWindow(@Nullable TradingWindow _tradeWindow)
  {
    this.tradeWindow = _tradeWindow;
  }
  
  public final TradingWindow getTradeWindow()
  {
    return this.tradeWindow;
  }
  
  public final String getTasteString()
  {
    String toReturn = "royal, noble, and utterly delicious!";
    float ql = getCurrentQualityLevel();
    if (ql < 5.0F) {
      toReturn = "rotten, bad, evil and dangerous.";
    } else if (ql < 20.0F) {
      toReturn = "extremely bad.";
    } else if (ql < 30.0F) {
      toReturn = "pretty bad.";
    } else if (ql < 40.0F) {
      toReturn = "okay.";
    } else if (ql < 50.0F) {
      toReturn = "pretty good.";
    } else if (ql < 60.0F) {
      toReturn = "good.";
    } else if (ql < 70.0F) {
      toReturn = "very good.";
    } else if (ql < 80.0F) {
      toReturn = "extremely good.";
    } else if (ql < 90.0F) {
      toReturn = "so good you almost feel like singing.";
    }
    return toReturn;
  }
  
  public static byte fuelEfficiency(byte material)
  {
    switch (material)
    {
    case 14: 
      return 2;
    case 58: 
      return 8;
    case 59: 
      return 4;
    }
    return 1;
  }
  
  public static String getMaterialString(byte material)
  {
    return MaterialUtilities.getMaterialString(material);
  }
  
  public final boolean isHollow()
  {
    return this.template.isHollow();
  }
  
  public final boolean isWeaponSlash()
  {
    return this.template.weaponslash;
  }
  
  public final boolean isShield()
  {
    return this.template.shield;
  }
  
  public final boolean isArmour()
  {
    return this.template.armour;
  }
  
  public final boolean isBracelet()
  {
    return this.template.isBracelet();
  }
  
  public final boolean isFood()
  {
    return this.template.isFood();
  }
  
  public boolean isNamed()
  {
    if ((this.realTemplate > 0) && (!isDish()))
    {
      ItemTemplate realTemplate = getRealTemplate();
      assert (realTemplate != null);
      return realTemplate.namedCreator;
    }
    return this.template.namedCreator;
  }
  
  public final boolean isOnePerTile()
  {
    return this.template.onePerTile;
  }
  
  public final boolean isFourPerTile()
  {
    return this.template.fourPerTile;
  }
  
  public final boolean isTenPerTile()
  {
    return this.template.tenPerTile;
  }
  
  public final boolean isTrellis()
  {
    return this.template.isTrellis();
  }
  
  final boolean isMagic()
  {
    return this.template.magic;
  }
  
  public final boolean isEgg()
  {
    return this.template.egg;
  }
  
  public final boolean isFieldTool()
  {
    return this.template.fieldtool;
  }
  
  public final boolean isBodyPart()
  {
    return this.template.bodypart;
  }
  
  public final boolean isBodyPartAttached()
  {
    return (this.template.bodypart) && (this.auxbyte != 100);
  }
  
  public final boolean isBodyPartRemoved()
  {
    return (this.template.bodypart) && (this.auxbyte == 100);
  }
  
  public final boolean isInventory()
  {
    return this.template.inventory;
  }
  
  public final boolean isInventoryGroup()
  {
    return this.template.isInventoryGroup();
  }
  
  public final boolean isDragonArmour()
  {
    return this.template.isDragonArmour;
  }
  
  public int getImproveItem()
  {
    return this.template.getImproveItem();
  }
  
  public final boolean isMiningtool()
  {
    return this.template.miningtool;
  }
  
  public final boolean isWand()
  {
    return (getTemplateId() == 315) || (getTemplateId() == 176);
  }
  
  final boolean isCompass()
  {
    return this.template.isCompass;
  }
  
  final boolean isToolbelt()
  {
    return this.template.isToolbelt;
  }
  
  public final boolean isBelt()
  {
    return this.template.isBelt;
  }
  
  public final boolean isCarpentryTool()
  {
    return this.template.carpentrytool;
  }
  
  final boolean isSmithingTool()
  {
    return this.template.smithingtool;
  }
  
  public final boolean isWeaponBow()
  {
    return this.template.bow;
  }
  
  public final boolean isArrow()
  {
    switch (getTemplateId())
    {
    case 454: 
    case 455: 
    case 456: 
      return true;
    }
    return false;
  }
  
  public final boolean isBowUnstringed()
  {
    return this.template.bowUnstringed;
  }
  
  public final boolean isWeaponPierce()
  {
    return this.template.weaponpierce;
  }
  
  public final boolean isWeaponCrush()
  {
    return this.template.weaponcrush;
  }
  
  public final boolean isWeaponAxe()
  {
    return this.template.weaponaxe;
  }
  
  public final boolean isWeaponSword()
  {
    return this.template.weaponsword;
  }
  
  public final boolean isWeaponPolearm()
  {
    return this.template.weaponPolearm;
  }
  
  public final boolean isWeaponKnife()
  {
    return this.template.weaponknife;
  }
  
  public final boolean isWeaponMisc()
  {
    return this.template.weaponmisc;
  }
  
  public final boolean isDiggingtool()
  {
    return this.template.diggingtool;
  }
  
  public final boolean isNoTrade()
  {
    return this.template.notrade;
  }
  
  public final boolean isSeed()
  {
    return this.template.seed;
  }
  
  public final boolean isSeedling()
  {
    switch (getTemplateId())
    {
    case 917: 
    case 918: 
    case 1017: 
      return true;
    }
    return false;
  }
  
  public final boolean isAbility()
  {
    return this.template.isAbility;
  }
  
  public final boolean isLiquid()
  {
    return this.template.liquid;
  }
  
  public final boolean isDye()
  {
    switch (getTemplateId())
    {
    case 431: 
    case 432: 
    case 433: 
    case 434: 
    case 435: 
    case 438: 
      return true;
    }
    return false;
  }
  
  public final boolean isLightBright()
  {
    return this.template.brightLight;
  }
  
  public static byte getRLight(int brightness)
  {
    return (byte)(255 * brightness / 255);
  }
  
  public static byte getGLight(int brightness)
  {
    return (byte)(239 * brightness / 255);
  }
  
  public static byte getBLight(int brightness)
  {
    return (byte)(173 * brightness / 255);
  }
  
  public final long getDecayTime()
  {
    return this.template.getDecayTime();
  }
  
  public final boolean isRefreshedOnUse()
  {
    return this.template.getDecayTime() == 28800L;
  }
  
  public final boolean isDish()
  {
    return this.template.isDish;
  }
  
  public final boolean isMelting()
  {
    return this.template.melting;
  }
  
  public final boolean isMeat()
  {
    return this.template.meat;
  }
  
  public final boolean isSign()
  {
    return this.template.sign;
  }
  
  public final boolean isFence()
  {
    return this.template.fence;
  }
  
  public final boolean isVegetable()
  {
    return this.template.vegetable;
  }
  
  public final boolean isRoadMarker()
  {
    return this.template.isRoadMarker();
  }
  
  public final boolean isPaveable()
  {
    return this.template.isPaveable();
  }
  
  public final boolean isCavePaveable()
  {
    return this.template.isCavePaveable();
  }
  
  public final boolean containsIngredientsOnly()
  {
    return this.template.containsIngredientsOnly();
  }
  
  public final boolean isShelf()
  {
    return this.template.isShelf();
  }
  
  public final boolean isComponentItem()
  {
    return this.template.isComponentItem();
  }
  
  public final boolean isParentMustBeOnGround()
  {
    return this.template.isParentMustBeOnGround();
  }
  
  final boolean isVillageRecruitmentBoard()
  {
    return this.template.templateId == 835;
  }
  
  public final boolean isBed()
  {
    return this.template.bed;
  }
  
  public final boolean isNewbieItem()
  {
    return (this.template.newbieItem) && (this.auxbyte > 0);
  }
  
  public final boolean isMilk()
  {
    return this.template.isMilk;
  }
  
  public final boolean isCheese()
  {
    return this.template.isCheese;
  }
  
  public final boolean isChallengeNewbieItem()
  {
    return (this.template.challengeNewbieItem) && (this.auxbyte > 0);
  }
  
  public final boolean isWood()
  {
    if (this.material == 0)
    {
      if (this.realTemplate > 0)
      {
        ItemTemplate realTemplate = getRealTemplate();
        assert (realTemplate != null);
        return realTemplate.wood;
      }
      return this.template.wood;
    }
    return Materials.isWood(this.material);
  }
  
  public final boolean isStone()
  {
    if (this.material == 0) {
      return this.template.stone;
    }
    return Materials.isStone(this.material);
  }
  
  public final boolean isCombineCold()
  {
    return this.template.isCombineCold();
  }
  
  public final boolean isGem()
  {
    return this.template.gem;
  }
  
  final boolean isFlickering()
  {
    return this.template.flickeringLight;
  }
  
  public final ItemTemplate getRealTemplate()
  {
    try
    {
      return ItemTemplateFactory.getInstance().getTemplate(this.realTemplate);
    }
    catch (NoSuchTemplateException localNoSuchTemplateException) {}
    return null;
  }
  
  public final boolean isMetal()
  {
    if (this.material == 0)
    {
      if (this.realTemplate > 0)
      {
        ItemTemplate realTemplate = getRealTemplate();
        assert (realTemplate != null);
        return realTemplate.isMetal();
      }
      return this.template.isMetal();
    }
    return Materials.isMetal(this.material);
  }
  
  public final boolean isLeather()
  {
    if (this.material == 0)
    {
      if (this.realTemplate > 0)
      {
        ItemTemplate realTemplate = getRealTemplate();
        assert (realTemplate != null);
        return realTemplate.leather;
      }
      return this.template.leather;
    }
    return Materials.isLeather(this.material);
  }
  
  public final boolean isPaper()
  {
    if (this.material == 0)
    {
      if (this.realTemplate > 0)
      {
        ItemTemplate realTemplate = getRealTemplate();
        assert (realTemplate != null);
        return realTemplate.paper;
      }
      return this.template.paper;
    }
    return Materials.isPaper(this.material);
  }
  
  public final boolean isCloth()
  {
    if (this.material == 0)
    {
      if (this.realTemplate > 0)
      {
        ItemTemplate realTemplate = getRealTemplate();
        assert (realTemplate != null);
        return realTemplate.cloth;
      }
      return this.template.cloth;
    }
    return Materials.isCloth(this.material);
  }
  
  public final boolean isWool()
  {
    if (this.material == 0)
    {
      if (this.realTemplate > 0)
      {
        ItemTemplate realTemplate = getRealTemplate();
        assert (realTemplate != null);
        return realTemplate.getMaterial() == 69;
      }
      return this.template.getMaterial() == 69;
    }
    return this.material == 69;
  }
  
  public final boolean isPottery()
  {
    if (this.material == 0)
    {
      if (this.realTemplate > 0)
      {
        ItemTemplate realTemplate = getRealTemplate();
        assert (realTemplate != null);
        return realTemplate.pottery;
      }
      return this.template.pottery;
    }
    return Materials.isPottery(this.material);
  }
  
  public final boolean isPlantedFlowerpot()
  {
    return this.template.isPlantedFlowerpot();
  }
  
  public final boolean isPotteryFlowerPot()
  {
    int tempId = getTemplateId();
    switch (tempId)
    {
    case 812: 
    case 813: 
    case 814: 
    case 815: 
    case 816: 
    case 817: 
    case 818: 
    case 819: 
    case 820: 
      return true;
    }
    return false;
  }
  
  public final boolean isMarblePlanter()
  {
    int tempId = getTemplateId();
    switch (tempId)
    {
    case 1001: 
    case 1002: 
    case 1003: 
    case 1004: 
    case 1005: 
    case 1006: 
    case 1007: 
    case 1008: 
      return true;
    }
    return false;
  }
  
  public final boolean isMagicStaff()
  {
    return this.template.isMagicStaff();
  }
  
  public final boolean isImproveUsingTypeAsMaterial()
  {
    return this.template.isImproveUsingTypeAsMaterial();
  }
  
  public final boolean isLight()
  {
    return (this.template.light) || (this.isLightOverride);
  }
  
  public final boolean isContainerLiquid()
  {
    return this.template.containerliquid;
  }
  
  public final boolean isLiquidInflammable()
  {
    return this.template.liquidinflammable;
  }
  
  public final boolean isHealingSalve()
  {
    return this.template.getTemplateId() == 650;
  }
  
  public final boolean isForgeOrOven()
  {
    return (this.template.getTemplateId() == 180) || (this.template.getTemplateId() == 178);
  }
  
  public final boolean isSpawnPoint()
  {
    return this.template.getTemplateId() == 1016;
  }
  
  final boolean isWeaponMelee()
  {
    return this.template.weaponmelee;
  }
  
  public final boolean isFish()
  {
    return this.template.fish;
  }
  
  public final boolean isMailBox()
  {
    return (this.template.templateId >= 510) && (this.template.templateId <= 513);
  }
  
  public final boolean isUnenchantedTurret()
  {
    return this.template.templateId == 934;
  }
  
  public final boolean isEnchantedTurret()
  {
    switch (this.template.templateId)
    {
    case 940: 
    case 941: 
    case 942: 
    case 968: 
      return true;
    }
    return false;
  }
  
  public final boolean isMarketStall()
  {
    return this.template.templateId == 580;
  }
  
  public final boolean isWeapon()
  {
    return this.template.weapon;
  }
  
  public final boolean isTool()
  {
    return this.template.tool;
  }
  
  public final boolean isCookingTool()
  {
    return this.template.isCookingTool();
  }
  
  public final boolean isLock()
  {
    return this.template.lock;
  }
  
  public final boolean templateIndestructible()
  {
    return this.template.indestructible;
  }
  
  public final boolean isKey()
  {
    return this.template.key;
  }
  
  public final boolean isBulkContainer()
  {
    return this.template.bulkContainer;
  }
  
  public final boolean isTopParentPile()
  {
    Item item = getTopParentOrNull();
    if (item == null) {
      return false;
    }
    return item.getTemplateId() == 177;
  }
  
  public final Item getItemWithTemplateAndMaterial(int stemplateId, int smaterial, byte auxByte, int srealTemplateId)
  {
    for (Item i : getItems()) {
      if ((i.getRealTemplateId() == stemplateId) && (smaterial == i.getMaterial()) && (i.getAuxData() == auxByte) && (((srealTemplateId == -10) && 
        (i.getData1() == -1)) || (i.getData1() == srealTemplateId))) {
        return i;
      }
    }
    return null;
  }
  
  public final boolean isBulkItem()
  {
    return getTemplateId() == 669;
  }
  
  public final boolean isBulk()
  {
    return this.template.bulk;
  }
  
  public final boolean isFire()
  {
    return this.template.isFire;
  }
  
  public final boolean canBeDropped(boolean checkTraded)
  {
    if (isNoDrop()) {
      return false;
    }
    if ((checkTraded) && (isTraded())) {
      return false;
    }
    if ((this.items == null) || (!isHollow())) {
      return true;
    }
    for (Item item : this.items)
    {
      if (!item.canBeDropped(true)) {
        return false;
      }
      if (item.isNoTrade()) {
        return false;
      }
    }
    return true;
  }
  
  public final boolean isWind()
  {
    return this.template.isWind;
  }
  
  public final boolean isFlag()
  {
    return this.template.isFlag;
  }
  
  public final boolean isRepairable()
  {
    if (this.permissions.hasPermission(Permissions.Allow.NO_REPAIR.getBit())) {
      return false;
    }
    return isRepairableDefault();
  }
  
  public final boolean isRepairableDefault()
  {
    if ((this.realTemplate > 0) && (getTemplateId() != 1307))
    {
      ItemTemplate realTemplate = getRealTemplate();
      assert (realTemplate != null);
      return (realTemplate.repairable) || (realTemplate.templateId == 74) || (realTemplate.templateId == 480);
    }
    if (getTemplateId() == 179) {
      return true;
    }
    return this.template.repairable;
  }
  
  public final boolean isRoyal()
  {
    return this.template.isRoyal;
  }
  
  public final boolean isTemporary()
  {
    return this.template.temporary;
  }
  
  public final boolean isCombine()
  {
    return this.template.combine;
  }
  
  public final boolean templateIsLockable()
  {
    return this.template.lockable;
  }
  
  public final boolean isUnfired()
  {
    return this.template.isUnfired;
  }
  
  public final boolean canHaveInscription()
  {
    return this.template.canHaveInscription();
  }
  
  public final boolean isAlmanacContainer()
  {
    return this.template.isAlmanacContainer();
  }
  
  public final boolean isHarvestReport()
  {
    if ((getTemplateId() == 1272) || (getTemplateId() == 748)) {
      return getAuxData() > 8;
    }
    return false;
  }
  
  @Nullable
  public final WurmHarvestables.Harvestable getHarvestable()
  {
    return WurmHarvestables.getHarvestable(getAuxData() - 8);
  }
  
  public final boolean hasData()
  {
    return this.template.hasdata;
  }
  
  public final boolean hasExtraData()
  {
    return this.template.hasExtraData();
  }
  
  public final boolean isDraggable()
  {
    return (this.template.draggable) && (!isNoDrag());
  }
  
  public final boolean isVillageDeed()
  {
    return this.template.villagedeed;
  }
  
  public final boolean isTransmutable()
  {
    return this.template.isTransmutable;
  }
  
  public final boolean isFarwalkerItem()
  {
    return this.template.farwalkerItem;
  }
  
  public final boolean isHomesteadDeed()
  {
    return this.template.homesteaddeed;
  }
  
  public final boolean isNoRename()
  {
    return this.template.norename;
  }
  
  public final boolean isNoNutrition()
  {
    return this.template.isNoNutrition();
  }
  
  public final boolean isLowNutrition()
  {
    return this.template.isLowNutrition();
  }
  
  public final boolean isMediumNutrition()
  {
    return this.template.isMediumNutrition();
  }
  
  public final boolean isHighNutrition()
  {
    return this.template.isHighNutrition();
  }
  
  public final boolean isGoodNutrition()
  {
    return this.template.isGoodNutrition();
  }
  
  public final boolean isFoodMaker()
  {
    return this.template.isFoodMaker();
  }
  
  public final boolean canLarder()
  {
    return this.template.canLarder();
  }
  
  public final boolean templateAlwaysLit()
  {
    return this.template.alwaysLit;
  }
  
  public final boolean isEpicTargetItem()
  {
    if (this.realTemplate > 0)
    {
      ItemTemplate realTemplate = getRealTemplate();
      assert (realTemplate != null);
      return realTemplate.isEpicTargetItem;
    }
    return this.template.isEpicTargetItem;
  }
  
  private final boolean checkPlantedPermissions(Creature creature)
  {
    VolaTile vt = Zones.getTileOrNull(getTileX(), getTileY(), this.surfaced);
    if (vt != null)
    {
      Structure structure = vt.getStructure();
      if ((structure != null) && (structure.isTypeHouse())) {
        return structure.isActionAllowed(creature, (short)685);
      }
      Village village = vt.getVillage();
      if (village != null) {
        return village.isActionAllowed((short)685, creature);
      }
    }
    return false;
  }
  
  public final boolean isTurnable(@Nonnull Creature turner)
  {
    if ((getParentId() != -10L) && ((getParentOrNull() != getTopParentOrNull()) || (!getParentOrNull().getTemplate().hasViewableSubItems()) || (
      (getParentOrNull().getTemplate().isContainerWithSubItems()) && (!isPlacedOnParent())))) {
      return false;
    }
    if (isOwnedByWagoner()) {
      return false;
    }
    if ((isTurnable()) && (!isPlanted())) {
      return true;
    }
    if (turner.getPower() >= 2) {
      return true;
    }
    if ((isPlanted()) && (checkPlantedPermissions(turner))) {
      return true;
    }
    return ((isOwnerTurnable()) || (isPlanted())) && (this.lastOwner == turner.getWurmId());
  }
  
  public final boolean isMoveable(@Nonnull Creature mover)
  {
    if (getParentId() != -10L) {
      return false;
    }
    if (isOwnedByWagoner()) {
      return false;
    }
    if (isEpicTargetItem()) {
      if (EpicServerStatus.getRitualMissionForTarget(getWurmId()) != null) {
        return false;
      }
    }
    if ((!isNoMove()) && (!isPlanted())) {
      return true;
    }
    if (mover.getPower() >= 2) {
      return true;
    }
    if ((isPlanted()) && (checkPlantedPermissions(mover))) {
      return true;
    }
    return ((isOwnerMoveable()) || (isPlanted())) && (this.lastOwner == mover.getWurmId());
  }
  
  public final boolean isGuardTower()
  {
    return this.template.isGuardTower();
  }
  
  public final boolean isHerb()
  {
    return this.template.herb;
  }
  
  public final boolean isSpice()
  {
    return this.template.spice;
  }
  
  final boolean isFruit()
  {
    return this.template.fruit;
  }
  
  public final boolean templateIsNoMove()
  {
    return this.template.isNoMove;
  }
  
  final boolean isPoison()
  {
    return this.template.poison;
  }
  
  public final boolean isOutsideOnly()
  {
    return this.template.outsideonly;
  }
  
  public final boolean isInsideOnly()
  {
    return this.template.insideOnly;
  }
  
  public final boolean isCoin()
  {
    return this.template.coin;
  }
  
  public final int getRentCost()
  {
    switch (this.auxbyte)
    {
    case 0: 
      return 0;
    case 1: 
      return 100;
    case 2: 
      return 1000;
    case 3: 
      return 10000;
    case 4: 
      return 100000;
    case 5: 
      return 10;
    case 6: 
      return 25;
    case 7: 
      return 50;
    }
    return 0;
  }
  
  public final boolean isDecoration()
  {
    if ((this.realTemplate > 0) && (getTemplateId() != 1162) && 
      (!isPlanted()) && (getTemplateId() != 1307))
    {
      ItemTemplate realTemplate = getRealTemplate();
      assert (realTemplate != null);
      
      VolaTile v = Zones.getTileOrNull(getTilePos(), isOnSurface());
      if ((v != null) && 
        (v.getNumberOfDecorations(getFloorLevel()) >= 15)) {
        return false;
      }
      return realTemplate.decoration;
    }
    if (this.template.decoration) {
      return true;
    }
    return (this.template.decorationWhenPlanted) && (isPlanted());
  }
  
  public final boolean isBag()
  {
    return this.template.isBag;
  }
  
  public final boolean isQuiver()
  {
    return this.template.isQuiver;
  }
  
  public final boolean isFullprice()
  {
    return this.template.fullprice;
  }
  
  public final boolean isNoSellback()
  {
    return this.template.isNoSellBack;
  }
  
  public final boolean isBarding()
  {
    return this.template.isBarding();
  }
  
  public final boolean isRope()
  {
    return this.template.isRope();
  }
  
  public final boolean isAlwaysPoll()
  {
    return this.template.alwayspoll;
  }
  
  public final boolean isProtectionTower()
  {
    return this.template.protectionTower;
  }
  
  public final boolean isFloating()
  {
    if ((this.template.templateId == 1396) && (isPlanted())) {
      return true;
    }
    return this.template.floating;
  }
  
  final boolean isButcheredItem()
  {
    return this.template.isButcheredItem;
  }
  
  public final boolean isNoWorkParent()
  {
    return this.template.noWorkParent;
  }
  
  public final boolean isNoBank()
  {
    return this.template.nobank;
  }
  
  public final boolean isAlwaysBankable()
  {
    return this.template.alwaysBankable;
  }
  
  public final boolean isLeadCreature()
  {
    return this.template.isLeadCreature;
  }
  
  public final boolean isLeadMultipleCreatures()
  {
    return this.template.isLeadMultipleCreatures;
  }
  
  public final boolean descIsExam()
  {
    return this.template.descIsExam;
  }
  
  public final int getPrice()
  {
    return this.price;
  }
  
  public final boolean isDomainItem()
  {
    return this.template.domainItem;
  }
  
  public final boolean isCrude()
  {
    return this.template.isCrude();
  }
  
  public final boolean isMinable()
  {
    return this.template.minable;
  }
  
  public final boolean isEnchantableJewelry()
  {
    return this.template.isEnchantableJewelry;
  }
  
  public final boolean isUseOnGroundOnly()
  {
    if ((this.realTemplate > 0) && (getTemplateId() != 1307))
    {
      ItemTemplate realTemplate = getRealTemplate();
      assert (realTemplate != null);
      return realTemplate.useOnGroundOnly;
    }
    return this.template.useOnGroundOnly;
  }
  
  public final boolean isHugeAltar()
  {
    return this.template.hugeAltar;
  }
  
  public final boolean isDestroyHugeAltar()
  {
    return this.template.destroysHugeAltar;
  }
  
  public final boolean isArtifact()
  {
    return this.template.artifact;
  }
  
  public final boolean isUnique()
  {
    return this.template.unique;
  }
  
  public final boolean isTwoHanded()
  {
    return this.template.isTwohanded;
  }
  
  public final boolean isServerBound()
  {
    return this.template.isServerBound;
  }
  
  public final boolean isKingdomMarker()
  {
    return this.template.kingdomMarker;
  }
  
  public final boolean isDestroyable(long destroyerId)
  {
    if (this.template.destroyable) {
      return true;
    }
    if (!this.template.ownerDestroyable) {
      return false;
    }
    long lockId = getLockId();
    if (lockId == -10L) {
      return this.lastOwner == destroyerId;
    }
    try
    {
      Item lock = Items.getItem(lockId);
      if (lock.lastOwner == destroyerId) {
        return true;
      }
    }
    catch (NoSuchItemException nsi)
    {
      if (this.lastOwner == destroyerId) {
        return true;
      }
    }
    return false;
  }
  
  public final boolean isDrinkable()
  {
    return this.template.drinkable;
  }
  
  public final boolean isVehicle()
  {
    return this.template.isVehicle();
  }
  
  public final boolean isChair()
  {
    return this.template.isChair;
  }
  
  public final boolean isCart()
  {
    return this.template.isCart;
  }
  
  public final boolean isWagonerWagon()
  {
    return this.wagonerWagon;
  }
  
  public final void setWagonerWagon(boolean isWagonerWagon)
  {
    this.wagonerWagon = isWagonerWagon;
  }
  
  public final boolean isBoat()
  {
    return (this.template.isVehicle()) && (this.template.isFloating());
  }
  
  public final boolean isMooredBoat()
  {
    return (isBoat()) && (getData() != -1L);
  }
  
  public final boolean isRechargeable()
  {
    return this.template.isRechargeable();
  }
  
  public final boolean isMineDoor()
  {
    return this.template.isMineDoor;
  }
  
  public final boolean isOwnerDestroyable()
  {
    return this.template.ownerDestroyable;
  }
  
  public final boolean isHitchTarget()
  {
    return this.template.isHitchTarget();
  }
  
  public final boolean isRiftAltar()
  {
    return this.template.isRiftAltar();
  }
  
  public final boolean isRiftItem()
  {
    return this.template.isRiftItem();
  }
  
  public final boolean isRiftLoot()
  {
    return this.template.isRiftLoot();
  }
  
  public final boolean hasItemBonus()
  {
    return this.template.isHasItemBonus();
  }
  
  final boolean isPriceEffectedByMaterial()
  {
    return this.template.priceAffectedByMaterial;
  }
  
  public final boolean isDeathProtection()
  {
    return this.template.isDeathProtection;
  }
  
  public final boolean isInPvPZone()
  {
    return Zones.isOnPvPServer(getTilePos());
  }
  
  public final void getContainedItems()
  {
    if ((!isHollow()) && (!isBodyPart())) {
      return;
    }
    Set<Item> set = Items.getContainedItems(this.id);
    if (set == null) {
      return;
    }
    for (Item item : set) {
      if (item.getOwnerId() != this.ownerId)
      {
        logWarn(
          item.getName() + " at " + ((int)item.getPosX() >> 2) + ", " + ((int)item.getPosY() >> 2) + " with id " + item.getWurmId() + " doesn't have the same owner as " + getName() + " with id " + this.id + ". Deleting.");
        
        Items.decay(item.getWurmId(), item.getDbStrings());
      }
      else
      {
        addItem(item, true);
      }
    }
  }
  
  public final boolean isHolyItem()
  {
    if (!this.template.holyItem) {
      return false;
    }
    switch (getMaterial())
    {
    case 7: 
    case 8: 
      return true;
    case 67: 
    case 96: 
      if (Features.Feature.METALLIC_ITEMS.isEnabled()) {
        return true;
      }
      break;
    }
    return false;
  }
  
  public final boolean isHolyItem(Deity deity)
  {
    return (deity != null) && (deity.holyItem == getTemplateId());
  }
  
  final boolean isPassFullData()
  {
    return this.template.passFullData;
  }
  
  public final boolean isMeditation()
  {
    return this.template.isMeditation;
  }
  
  public final boolean isTileAligned()
  {
    if (this.realTemplate > 0)
    {
      ItemTemplate realTemplate = getRealTemplate();
      assert (realTemplate != null);
      return realTemplate.isTileAligned;
    }
    return this.template.isTileAligned;
  }
  
  public final boolean isNewDeed()
  {
    return getTemplateId() == 663;
  }
  
  public final boolean isOldDeed()
  {
    return ((isVillageDeed()) || (isHomesteadDeed())) && (!isNewDeed());
  }
  
  final boolean isLiquidCooking()
  {
    return this.template.isLiquidCooking();
  }
  
  final boolean isForm()
  {
    return this.template.isForm;
  }
  
  public final boolean isFlower()
  {
    return this.template.isFlower;
  }
  
  public final boolean isNaturePlantable()
  {
    return this.template.isNaturePlantable;
  }
  
  public final boolean isBanked()
  {
    if ((this.banked) && (this.parentId != -10L) && 
      (!Banks.isItemBanked(getWurmId())))
    {
      logger.warning("Bugged item showing as banked: " + toString());
      setBanked(false);
    }
    return this.banked;
  }
  
  public final boolean isAltar()
  {
    return this.template.isAltar;
  }
  
  public final Deity getBless()
  {
    if (this.bless > 0)
    {
      Deity deity = Deities.getDeity(this.bless);
      if (deity != null) {
        return deity;
      }
    }
    return null;
  }
  
  public final void setData(long aData)
  {
    if (aData == -10L) {
      setData(-1, -1);
    } else {
      setData((int)(aData >> 32), (int)aData);
    }
  }
  
  public final long getData()
  {
    int data1 = getData1();
    int data2 = getData2();
    if ((data1 == -1) || (data2 == -1)) {
      return -1L;
    }
    return (data2 & 0xFFFFFFFF) + BigInteger.valueOf(data1 & 0xFFFFFFFF).shiftLeft(32).longValue();
  }
  
  public final void setExtra(long aExtra)
  {
    if (aExtra == -10L) {
      setExtra(-1, -1);
    } else {
      setExtra((int)(aExtra >> 32), (int)aExtra);
    }
  }
  
  public final long getExtra()
  {
    int extra1 = getExtra1();
    int extra2 = getExtra2();
    if ((extra1 == -1) || (extra2 == -1)) {
      return -1L;
    }
    return (extra2 & 0xFFFFFFFF) + BigInteger.valueOf(extra1 & 0xFFFFFFFF).shiftLeft(32).longValue();
  }
  
  public final void setDataXY(int aTileX, int aTileY)
  {
    setData1(aTileX << 16 | aTileY);
  }
  
  public final short getDataX()
  {
    int data1 = getData1();
    if (data1 == -1) {
      return -1;
    }
    return (short)(data1 >> 16 & 0xFFFF);
  }
  
  public final short getDataY()
  {
    int data1 = getData1();
    if (data1 == -1) {
      return -1;
    }
    return (short)(getData1() & 0xFFFF);
  }
  
  public final void setSizes(int aWeight)
  {
    ItemTemplate lTemplate = getTemplate();
    
    int sizeX = lTemplate.getSizeX();
    int sizeY = lTemplate.getSizeY();
    int sizeZ = lTemplate.getSizeZ();
    float mod = aWeight / lTemplate.getWeightGrams();
    if (mod > 64.0F)
    {
      setSizeZ(sizeZ * 4);
      setSizeY(sizeY * 4);
      setSizeX(sizeX * 4);
    }
    else if (mod > 16.0F)
    {
      setSizeZ(sizeZ * 4);
      setSizeY(sizeY * 4);
      mod = mod / 4.0F * 4.0F;
      setSizeX((int)(sizeX * mod));
    }
    else if (mod > 4.0F)
    {
      setSizeZ(sizeZ * 4);
      mod /= 4.0F;
      setSizeY((int)(sizeY * mod));
      setSizeX(sizeX);
    }
    else
    {
      setSizes(Math.max(1, (int)(sizeX * mod)), 
        Math.max(1, (int)(sizeY * mod)), 
        Math.max(1, (int)(sizeZ * mod)));
    }
  }
  
  public final ItemSpellEffects getSpellEffects()
  {
    return ItemSpellEffects.getSpellEffects(this.id);
  }
  
  public final float getDamageModifierForItem(Item item)
  {
    float mod = 0.0F;
    if (isStone())
    {
      if (item.getTemplateId() == 20) {
        mod = 0.007F;
      } else if (item.isWeaponCrush()) {
        mod = 0.003F;
      } else if (item.isWeaponAxe()) {
        mod = 0.0015F;
      } else if (item.isWeaponSlash()) {
        mod = 0.001F;
      } else if (item.isWeaponPierce()) {
        mod = 0.001F;
      } else if (item.isWeaponMisc()) {
        mod = 0.001F;
      }
    }
    else if (getMaterial() == 38)
    {
      if (item.isWeaponAxe()) {
        mod = 0.007F;
      } else if (item.isWeaponCrush()) {
        mod = 0.003F;
      } else if (item.isWeaponSlash()) {
        mod = 0.005F;
      } else if (item.isWeaponPierce()) {
        mod = 0.002F;
      } else if (item.isWeaponMisc()) {
        mod = 0.001F;
      }
    }
    else if ((isWood()) || (isCloth()) || (isFood()))
    {
      if (item.isWeaponAxe()) {
        mod = 0.003F;
      } else if (item.isWeaponCrush()) {
        mod = 0.002F;
      } else if (item.isWeaponSlash()) {
        mod = 0.0015F;
      } else if (item.isWeaponPierce()) {
        mod = 0.001F;
      } else if (item.isWeaponMisc()) {
        mod = 7.0E-4F;
      }
    }
    else if (isMetal())
    {
      if (item.isWeaponAxe()) {
        mod = 0.001F;
      } else if (item.isWeaponCrush()) {
        mod = 0.0015F;
      } else if (item.isWeaponSlash()) {
        mod = 0.001F;
      } else if (item.isWeaponPierce()) {
        mod = 5.0E-4F;
      } else if (item.isWeaponMisc()) {
        mod = 0.001F;
      }
    }
    else if (item.isWeaponAxe()) {
      mod = 0.001F;
    } else if (item.isWeaponCrush()) {
      mod = 0.0015F;
    } else if (item.isWeaponSlash()) {
      mod = 0.001F;
    } else if (item.isWeaponPierce()) {
      mod = 5.0E-4F;
    } else if (item.isWeaponMisc()) {
      mod = 0.001F;
    }
    if (isTent()) {
      mod *= 50.0F;
    }
    return mod;
  }
  
  public final Vector2f getPos2f()
  {
    if ((this.parentId == -10L) && (!isBodyPartAttached()) && (!isInventory())) {
      return new Vector2f(this.posX, this.posY);
    }
    if (this.ownerId != -10L) {
      try
      {
        Creature creature = Server.getInstance().getCreature(this.ownerId);
        return creature.getPos2f();
      }
      catch (NoSuchCreatureException e)
      {
        if (!Items.isItemLoaded(this.parentId)) {
          return new Vector2f(this.posX, this.posY);
        }
        try
        {
          Item parent = Items.getItem(this.parentId);
          return parent.getPos2f();
        }
        catch (NoSuchItemException e1)
        {
          logWarn("This REALLY shouldn't happen!", e1);
        }
      }
      catch (NoSuchPlayerException ignored)
      {
        if (!Items.isItemLoaded(this.parentId)) {
          return new Vector2f(this.posX, this.posY);
        }
        try
        {
          Item parent = Items.getItem(this.parentId);
          return parent.getPos2f();
        }
        catch (NoSuchItemException e)
        {
          logWarn("This REALLY shouldn't happen!", e);
        }
      }
    }
    if (Items.isItemLoaded(this.parentId)) {
      try
      {
        Item parent = Items.getItem(this.parentId);
        return parent.getPos2f();
      }
      catch (NoSuchItemException nsi2)
      {
        logWarn("This REALLY shouldn't happen!", nsi2);
      }
    }
    return new Vector2f(this.posX, this.posY);
  }
  
  @Nonnull
  public final Vector3f getPos3f()
  {
    if ((this.parentId == -10L) && (!isBodyPartAttached()) && (!isInventory())) {
      return new Vector3f(this.posX, this.posY, this.posZ);
    }
    if (this.ownerId != -10L) {
      try
      {
        Creature creature = Server.getInstance().getCreature(this.ownerId);
        return creature.getPos3f();
      }
      catch (NoSuchCreatureException nsc)
      {
        if (!Items.isItemLoaded(this.parentId)) {
          return new Vector3f(this.posX, this.posY, this.posZ);
        }
        try
        {
          Item parent = Items.getItem(this.parentId);
          return parent.getPos3f();
        }
        catch (NoSuchItemException nsi)
        {
          logWarn("This REALLY shouldn't happen!", nsi);
        }
      }
      catch (NoSuchPlayerException nsp)
      {
        if (!Items.isItemLoaded(this.parentId)) {
          return new Vector3f(this.posX, this.posY, this.posZ);
        }
        try
        {
          Item parent = Items.getItem(this.parentId);
          return parent.getPos3f();
        }
        catch (NoSuchItemException nsi)
        {
          logWarn("This REALLY shouldn't happen!", nsi);
        }
      }
    }
    if (Items.isItemLoaded(this.parentId)) {
      try
      {
        Item parent = Items.getItem(this.parentId);
        return parent.getPos3f();
      }
      catch (NoSuchItemException nsi2)
      {
        logWarn("This REALLY shouldn't happen!", nsi2);
      }
    }
    return new Vector3f(this.posX, this.posY, this.posZ);
  }
  
  public final float getPosX()
  {
    if ((this.parentId == -10L) && (!isBodyPartAttached()) && (!isInventory())) {
      return this.posX;
    }
    if (this.ownerId != -10L) {
      try
      {
        Creature creature = Server.getInstance().getCreature(this.ownerId);
        return creature.getStatus().getPositionX();
      }
      catch (NoSuchCreatureException nsc)
      {
        if (Items.isItemLoaded(this.parentId)) {
          try
          {
            Item parent = Items.getItem(this.parentId);
            return parent.getPosX();
          }
          catch (NoSuchItemException nsi)
          {
            logWarn("This REALLY shouldn't happen!", nsi);
          }
        }
      }
      catch (NoSuchPlayerException nsp)
      {
        if (Items.isItemLoaded(this.parentId)) {
          try
          {
            Item parent = Items.getItem(this.parentId);
            return parent.getPosX();
          }
          catch (NoSuchItemException nsi)
          {
            logWarn("This REALLY shouldn't happen!", nsi);
          }
        }
      }
    } else if (Items.isItemLoaded(this.parentId)) {
      try
      {
        Item parent = Items.getItem(this.parentId);
        if (!parent.isTemporary()) {
          return parent.getPosX();
        }
      }
      catch (NoSuchItemException nsi)
      {
        logWarn("This REALLY shouldn't happen!", nsi);
      }
    }
    return this.posX;
  }
  
  public final float getPosXRaw()
  {
    return this.posX;
  }
  
  public final float getPosY()
  {
    if ((this.parentId == -10L) && (!isBodyPartAttached()) && (!isInventory())) {
      return this.posY;
    }
    if (this.ownerId != -10L) {
      try
      {
        Creature creature = Server.getInstance().getCreature(this.ownerId);
        return creature.getStatus().getPositionY();
      }
      catch (NoSuchCreatureException nsc)
      {
        if (Items.isItemLoaded(this.parentId)) {
          try
          {
            Item parent = Items.getItem(this.parentId);
            return parent.getPosY();
          }
          catch (NoSuchItemException nsi)
          {
            logWarn("This REALLY shouldn't happen!", nsi);
          }
        }
      }
      catch (NoSuchPlayerException nsp)
      {
        if (Items.isItemLoaded(this.parentId)) {
          try
          {
            Item parent = Items.getItem(this.parentId);
            return parent.getPosY();
          }
          catch (NoSuchItemException nsi)
          {
            logWarn("This REALLY shouldn't happen!", nsi);
          }
        }
      }
    } else if (Items.isItemLoaded(this.parentId)) {
      try
      {
        Item parent = Items.getItem(this.parentId);
        return parent.getPosY();
      }
      catch (NoSuchItemException nsi)
      {
        logWarn("This REALLY shouldn't happen!", nsi);
      }
    }
    return this.posY;
  }
  
  public final float getPosYRaw()
  {
    return this.posY;
  }
  
  public final float getPosZ()
  {
    if ((this.parentId != -10L) || (isBodyPartAttached()) || (isInventory())) {
      if (this.ownerId != -10L) {
        try
        {
          Creature creature = Server.getInstance().getCreature(this.ownerId);
          return creature.getStatus().getPositionZ() + creature.getAltOffZ();
        }
        catch (NoSuchCreatureException nsc)
        {
          if (Items.isItemLoaded(this.parentId)) {
            try
            {
              Item parent = Items.getItem(this.parentId);
              return parent.getPosZ();
            }
            catch (NoSuchItemException nsi)
            {
              logWarn("This REALLY shouldn't happen!", nsi);
            }
          }
        }
        catch (NoSuchPlayerException nsp)
        {
          if (Items.isItemLoaded(this.parentId)) {
            try
            {
              Item parent = Items.getItem(this.parentId);
              return parent.getPosZ();
            }
            catch (NoSuchItemException nsi)
            {
              logWarn("This REALLY shouldn't happen!", nsi);
            }
          }
        }
      } else if (Items.isItemLoaded(this.parentId)) {
        try
        {
          Item parent = Items.getItem(this.parentId);
          return parent.getPosZ();
        }
        catch (NoSuchItemException nsi)
        {
          logWarn("This REALLY shouldn't happen!", nsi);
        }
      }
    }
    if (isFloating()) {
      return Math.max(0.0F, this.posZ);
    }
    return this.posZ;
  }
  
  public final float getPosZRaw()
  {
    return this.posZ;
  }
  
  public final boolean isEdibleBy(Creature creature)
  {
    if (creature.getTemplate().getTemplateId() == 93) {
      return isFish();
    }
    if ((isDish()) || ((isMeat()) && ((!isBodyPart()) || (isBodyPartRemoved()))) || (isFish())) {
      return (creature.isCarnivore()) || (creature.isOmnivore());
    }
    if ((isSeed()) || (getTemplateId() == 620)) {
      return (creature.isHerbivore()) || (creature.isOmnivore());
    }
    if (isVegetable()) {
      return (creature.isHerbivore()) || (creature.isOmnivore());
    }
    if (isFood()) {
      return creature.isOmnivore();
    }
    return false;
  }
  
  public final byte getKingdom()
  {
    if ((isRoyal()) || (getTemplateId() == 272)) {
      return getAuxData();
    }
    if ((isKingdomMarker()) || 
      (isWind()) || 
      (isVehicle()) || (this.template.isKingdomFlag) || 
      
      (isDuelRing()) || 
      (isEpicTargetItem()) || 
      (isWarTarget()) || 
      (isTent()) || (this.template.useMaterialAndKingdom) || 
      
      (isEnchantedTurret()) || 
      (isProtectionTower())) {
      return getAuxData();
    }
    return 0;
  }
  
  public final boolean isWithin(int startX, int endX, int startY, int endY)
  {
    return (getTileX() >= startX) && (getTileX() <= endX) && 
      (getTileY() >= startY) && (getTileY() <= endY);
  }
  
  public boolean isDuelRing()
  {
    return this.template.isDuelRing;
  }
  
  public final long getBridgeId()
  {
    return this.onBridge;
  }
  
  public final boolean mayCreatureInsertItem()
  {
    if (isInventoryGroup())
    {
      Item parent = getParentOrNull();
      return (parent != null) && (parent.mayCreatureInsertItem());
    }
    if ((isInventory()) && (getNumItemsNotCoins() < 100)) {
      return true;
    }
    return getItemCount() < 100;
  }
  
  public final Item getParentOrNull()
  {
    try
    {
      return getParent();
    }
    catch (NoSuchItemException nsi) {}
    return null;
  }
  
  public final int getNumItemsNotCoins()
  {
    if (this.items == null) {
      return 0;
    }
    int toReturn = 0;
    for (Item nItem : this.items) {
      if ((!nItem.isCoin()) && (!nItem.isInventoryGroup()) && (nItem.getTemplateId() != 666)) {
        toReturn++;
      } else if (nItem.isInventoryGroup()) {
        toReturn += nItem.getNumItemsNotCoins();
      }
    }
    return toReturn;
  }
  
  public int getNumberCages()
  {
    if (this.items == null) {
      return 0;
    }
    int toReturn = 0;
    for (Item nItem : getAllItems(true)) {
      if (nItem.getTemplateId() == 1311) {
        toReturn++;
      }
    }
    return toReturn;
  }
  
  public final int getFat()
  {
    return getData2() >> 1 & 0xFF;
  }
  
  public final boolean isHealing()
  {
    return this.template.healing;
  }
  
  public final int getAlchemyType()
  {
    return this.template.alchemyType;
  }
  
  public final boolean isButchered()
  {
    return (getData2() & 0x1) == 1;
  }
  
  public final boolean isMovingItem()
  {
    return this.template.isMovingItem;
  }
  
  public final boolean spawnsTrees()
  {
    return this.template.spawnsTrees;
  }
  
  public final boolean killsTrees()
  {
    return this.template.killsTrees;
  }
  
  public final boolean isNonDeedable()
  {
    return this.template.nonDeedable;
  }
  
  public final int getBulkTemplateId()
  {
    return getData1();
  }
  
  public final int getBulkNums()
  {
    if (isBulkItem())
    {
      ItemTemplate itemp = getRealTemplate();
      if (itemp != null) {
        return Math.max(1, getWeightGrams() / itemp.getVolume());
      }
      return 0;
    }
    return getData2();
  }
  
  public final float getBulkNumsFloat(boolean useMaxOne)
  {
    if (isBulkItem())
    {
      ItemTemplate itemp = getRealTemplate();
      if (itemp != null)
      {
        if (useMaxOne) {
          return Math.max(1.0F, getWeightGrams() / itemp.getVolume());
        }
        return getWeightGrams() / itemp.getVolume();
      }
      return 0.0F;
    }
    return getData2();
  }
  
  public final int getPlacedItemCount()
  {
    int itemsCount = 0;
    boolean normalContainer = getTemplate().isContainerWithSubItems();
    for (Item item : getItems()) {
      if (((normalContainer) && (item.isPlacedOnParent())) || (!normalContainer)) {
        itemsCount++;
      }
    }
    return itemsCount;
  }
  
  public final int getItemCount()
  {
    int itemsCount = 0;
    for (Item item : getItems()) {
      if (!item.isInventoryGroup()) {
        itemsCount++;
      } else {
        itemsCount += item.getItemCount();
      }
    }
    return itemsCount;
  }
  
  public final void setBulkTemplateId(int newid)
  {
    setData1(newid);
  }
  
  public final void updateName()
  {
    if (getParentId() != -10L)
    {
      sendUpdate();
      return;
    }
    if ((this.zoneId <= 0) || (this.parentId != -10L)) {
      return;
    }
    VolaTile t = Zones.getTileOrNull(getTileX(), getTileY(), isOnSurface());
    if (t != null) {
      t.renameItem(this);
    }
  }
  
  public final void setButchered()
  {
    setData2(1);
    if (this.ownerId != -10L) {
      return;
    }
    if (this.zoneId == -10) {
      return;
    }
    try
    {
      Zone z = Zones.getZone(this.zoneId);
      z.removeItem(this);
      z.addItem(this);
    }
    catch (NoSuchZoneException nsz)
    {
      logWarn(
        "Zone at " + ((int)getPosX() >> 2) + "," + ((int)getPosY() >> 2) + ",surf=" + isOnSurface() + " no such zone.");
    }
  }
  
  final void updateParents()
  {
    sendUpdate();
    try
    {
      Item parent = getParent();
      parent.updateParents();
    }
    catch (NoSuchItemException localNoSuchItemException) {}
  }
  
  public void sendUpdate()
  {
    if (this.watchers == null) {
      return;
    }
    for (Creature watcher : this.watchers) {
      watcher.getCommunicator().sendUpdateInventoryItem(this);
    }
  }
  
  public boolean isCreatureWearableOnly()
  {
    return this.template.isCreatureWearableOnly();
  }
  
  public final boolean isUnfinished()
  {
    return (this.template.getTemplateId() == 386) || (this.template.getTemplateId() == 179);
  }
  
  public final void updateIfGroundItem()
  {
    if ((getParentId() != -10L) || (this.zoneId == -10)) {
      return;
    }
    try
    {
      Zone z = Zones.getZone(this.zoneId);
      z.removeItem(this);
      z.addItem(this);
    }
    catch (NoSuchZoneException nsz)
    {
      logWarn(
        "Zone at " + ((int)getPosX() >> 2) + "," + ((int)getPosY() >> 2) + ",surf=" + isOnSurface() + " no such zone. Item: " + this);
    }
  }
  
  public final void updateModelNameOnGroundItem()
  {
    if ((getParentId() != -10L) || (this.zoneId == -10)) {
      return;
    }
    try
    {
      Zone z = Zones.getZone(this.zoneId);
      z.updateModelName(this);
    }
    catch (NoSuchZoneException nsz)
    {
      logWarn(
        "Zone at " + ((int)getPosX() >> 2) + "," + ((int)getPosY() >> 2) + ",surf=" + isOnSurface() + " no such zone. Item: " + this);
    }
  }
  
  public final void updatePos()
  {
    if (getParentId() != -10L) {
      try
      {
        Item parent = getParent();
        if (parent.getTemplateId() == 177)
        {
          parent.removeFromPile(this);
          parent.insertIntoPile(this);
        }
        else
        {
          parent.dropItem(this.id, false);
          parent.insertItem(this, true);
        }
      }
      catch (NoSuchItemException nsi)
      {
        logWarn("Item with id " + getWurmId() + " has no parent item with id " + getParentId(), new Exception());
      }
    } else if (this.zoneId != -10) {
      try
      {
        Zone z = Zones.getZone(this.zoneId);
        z.removeItem(this);
        z.addItem(this);
      }
      catch (NoSuchZoneException nsz)
      {
        logWarn(
          "Zone at " + ((int)getPosX() >> 2) + "," + ((int)getPosY() >> 2) + ",surf=" + isOnSurface() + " no such zone. Item: " + this);
      }
    }
  }
  
  public final boolean isStreetLamp()
  {
    return this.template.streetlamp;
  }
  
  public final boolean isSaddleLarge()
  {
    return this.template.getTemplateId() == 622;
  }
  
  public final boolean isSaddleNormal()
  {
    return this.template.getTemplateId() == 621;
  }
  
  public final boolean isHorseShoe()
  {
    return this.template.getTemplateId() == 623;
  }
  
  public final boolean isBridle()
  {
    return this.template.getTemplateId() == 624;
  }
  
  public final void setTempPositions(float posx, float posy, float posz, float rot)
  {
    this.posX = posx;
    this.posY = posy;
    this.posZ = posz;
    this.rotation = rot;
  }
  
  public final void setTempXPosition(float posx)
  {
    this.posX = posx;
  }
  
  public final void setTempYPosition(float posy)
  {
    this.posY = posy;
  }
  
  public final void setTempZandRot(float posz, float rot)
  {
    this.posZ = posz;
    this.rotation = rot;
  }
  
  public final byte getLeftAuxData()
  {
    return (byte)(this.auxbyte >> 4 & 0xF);
  }
  
  public final byte getRightAuxData()
  {
    return (byte)(this.auxbyte & 0xF);
  }
  
  public final boolean isDestroyedOnDecay()
  {
    return this.template.destroyOnDecay;
  }
  
  public final boolean isWarTarget()
  {
    return this.template.isWarTarget;
  }
  
  public final boolean isVisibleDecay()
  {
    return this.template.visibleDecay;
  }
  
  public final boolean isNoDiscard()
  {
    return (isCoin()) || 
      (isNewbieItem()) || 
      (isChallengeNewbieItem()) || 
      (isLiquid()) || 
      (this.template.isNoDiscard()) || 
      (isBodyPart()) || 
      (this.template.getTemplateId() == 862) || 
      (this.template.getValue() > 5000) || 
      (getValue() > 100) || 
      (isIndestructible()) || 
      (getSpellEffects() != null) || (this.enchantment != 0) || 
      
      (isMagic()) || 
      (isNoDrop()) || 
      (isInventory()) || 
      (isNoTrade()) || 
      (getRarity() > 0) || (
      (isHollow()) && (!isEmpty(false)));
  }
  
  public final boolean isInstaDiscard()
  {
    return this.template.isInstaDiscard();
  }
  
  public final void setLeftAuxData(int ldata)
  {
    setAuxData((byte)(getRightAuxData() + (ldata << 4 & 0xF0)));
  }
  
  public final void setRightAuxData(int rdata)
  {
    setAuxData((byte)((getLeftAuxData() << 4) + (rdata & 0xF)));
  }
  
  public final boolean isEpicPortal()
  {
    return this.template.isEpicPortal;
  }
  
  public final boolean isUnstableRift()
  {
    return this.template.isUnstableRift();
  }
  
  public final void setSurfaced(boolean newValue)
  {
    this.surfaced = newValue;
    if ((isHollow()) && (this.items != null)) {
      for (Item item : getAllItems(true, true)) {
        item.setSurfacedNotRecursive(this.surfaced);
      }
    }
  }
  
  private final void setSurfacedNotRecursive(boolean newValue)
  {
    this.surfaced = newValue;
  }
  
  abstract void create(float paramFloat, long paramLong)
    throws IOException;
  
  abstract void load()
    throws Exception;
  
  public abstract void loadEffects();
  
  public abstract void bless(int paramInt);
  
  public abstract void enchant(byte paramByte);
  
  abstract void setPlace(short paramShort);
  
  public abstract short getPlace();
  
  public abstract void setLastMaintained(long paramLong);
  
  public abstract long getLastMaintained();
  
  public abstract long getOwnerId();
  
  public abstract boolean setOwnerId(long paramLong);
  
  public abstract boolean getLocked();
  
  public abstract void setLocked(boolean paramBoolean);
  
  public abstract int getTemplateId();
  
  public abstract void setTemplateId(int paramInt);
  
  public abstract void setZoneId(int paramInt, boolean paramBoolean);
  
  public abstract int getZoneId();
  
  public abstract boolean setDescription(@Nonnull String paramString);
  
  @Nonnull
  public abstract String getDescription();
  
  public abstract boolean setInscription(@Nonnull String paramString1, @Nonnull String paramString2);
  
  public abstract boolean setInscription(@Nonnull String paramString1, @Nonnull String paramString2, int paramInt);
  
  public abstract void setName(@Nonnull String paramString);
  
  public abstract void setName(String paramString, boolean paramBoolean);
  
  public abstract float getRotation();
  
  public abstract void setPosXYZRotation(float paramFloat1, float paramFloat2, float paramFloat3, float paramFloat4);
  
  public abstract void setPosXYZ(float paramFloat1, float paramFloat2, float paramFloat3);
  
  public abstract void setPosXY(float paramFloat1, float paramFloat2);
  
  public abstract void setPosX(float paramFloat);
  
  public abstract void setPosY(float paramFloat);
  
  public abstract void setPosZ(float paramFloat);
  
  public abstract void setPos(float paramFloat1, float paramFloat2, float paramFloat3, float paramFloat4, long paramLong);
  
  public abstract void savePosition();
  
  public abstract void setRotation(float paramFloat);
  
  public abstract Set<Item> getItems();
  
  public abstract Item[] getItemsAsArray();
  
  public abstract void setParentId(long paramLong, boolean paramBoolean);
  
  public abstract long getParentId();
  
  abstract void setSizeX(int paramInt);
  
  abstract void setSizeY(int paramInt);
  
  abstract void setSizeZ(int paramInt);
  
  public abstract int getSizeX();
  
  public int getSizeX(boolean useModifier)
  {
    if (useModifier) {
      return getSizeX();
    }
    return this.sizeX;
  }
  
  public abstract int getSizeY();
  
  public int getSizeY(boolean useModifier)
  {
    if (useModifier) {
      return getSizeY();
    }
    return this.sizeY;
  }
  
  public abstract int getSizeZ();
  
  public int getSizeZ(boolean useModifier)
  {
    if (useModifier) {
      return getSizeZ();
    }
    return this.sizeZ;
  }
  
  public int getContainerSizeX()
  {
    float modifier = 1.0F;
    if (getSpellEffects() != null) {
      modifier = getSpellEffects().getRuneEffect(RuneUtilities.ModifierEffect.ENCH_SIZE);
    }
    if (this.template.usesSpecifiedContainerSizes()) {
      return (int)(this.template.getContainerSizeX() * modifier);
    }
    return getSizeX();
  }
  
  public int getContainerSizeY()
  {
    float modifier = 1.0F;
    if (getSpellEffects() != null) {
      modifier = getSpellEffects().getRuneEffect(RuneUtilities.ModifierEffect.ENCH_SIZE);
    }
    if (this.template.usesSpecifiedContainerSizes()) {
      return (int)(this.template.getContainerSizeY() * modifier);
    }
    return getSizeY();
  }
  
  public int getContainerSizeZ()
  {
    float modifier = 1.0F;
    if (getSpellEffects() != null) {
      modifier = getSpellEffects().getRuneEffect(RuneUtilities.ModifierEffect.ENCH_SIZE);
    }
    if (this.template.usesSpecifiedContainerSizes()) {
      return (int)(this.template.getContainerSizeZ() * modifier);
    }
    return getSizeZ();
  }
  
  public abstract int getWeightGrams();
  
  public int getWeightGrams(boolean useModifier)
  {
    if (useModifier) {
      return getWeightGrams();
    }
    return this.weight;
  }
  
  public abstract boolean setWeight(int paramInt, boolean paramBoolean1, boolean paramBoolean2);
  
  public abstract boolean setWeight(int paramInt, boolean paramBoolean);
  
  public abstract void setOriginalQualityLevel(float paramFloat);
  
  public abstract float getOriginalQualityLevel();
  
  public abstract boolean setDamage(float paramFloat, boolean paramBoolean);
  
  public abstract void setData1(int paramInt);
  
  public abstract void setData2(int paramInt);
  
  public abstract void setData(int paramInt1, int paramInt2);
  
  public abstract int getData1();
  
  public abstract int getData2();
  
  public abstract void setExtra1(int paramInt);
  
  public abstract void setExtra2(int paramInt);
  
  public abstract void setExtra(int paramInt1, int paramInt2);
  
  public abstract int getExtra1();
  
  public abstract int getExtra2();
  
  public abstract void setAllData(int paramInt1, int paramInt2, int paramInt3, int paramInt4);
  
  public abstract void setTemperature(short paramShort);
  
  public abstract byte getMaterial();
  
  public abstract void setMaterial(byte paramByte);
  
  public abstract long getLockId();
  
  public abstract void setLockId(long paramLong);
  
  public abstract void setPrice(int paramInt);
  
  abstract void addItem(@Nullable Item paramItem, boolean paramBoolean);
  
  abstract void removeItem(Item paramItem);
  
  public abstract void setBanked(boolean paramBoolean);
  
  public abstract void setLastOwnerId(long paramLong);
  
  public final long getLastOwnerId()
  {
    return this.lastOwner;
  }
  
  public abstract void setAuxData(byte paramByte);
  
  final byte getCreationState()
  {
    return this.creationState;
  }
  
  public abstract void setCreationState(byte paramByte);
  
  public final int getRealTemplateId()
  {
    return this.realTemplate;
  }
  
  public abstract void setRealTemplate(int paramInt);
  
  final boolean isWornAsArmour()
  {
    return this.wornAsArmour;
  }
  
  abstract void setWornAsArmour(boolean paramBoolean, long paramLong);
  
  public final int getColor()
  {
    if ((isStreetLamp()) || (isLight()) || (isLightBright())) {
      if (this.color == WurmColor.createColor(0, 0, 0)) {
        setColor(WurmColor.createColor(1, 1, 1));
      }
    }
    if (getTemplateId() == 531) {
      return WurmColor.createColor(40, 40, 215);
    }
    if (getTemplateId() == 534) {
      return WurmColor.createColor(215, 40, 40);
    }
    if (getTemplateId() == 537) {
      return WurmColor.createColor(10, 10, 10);
    }
    return this.color;
  }
  
  public final int getColor2()
  {
    if (getTemplateId() == 531) {
      return WurmColor.createColor(0, 130, 0);
    }
    if (getTemplateId() == 534) {
      return WurmColor.createColor(255, 255, 0);
    }
    if (getTemplateId() == 537) {
      return WurmColor.createColor(110, 0, 150);
    }
    return this.color2;
  }
  
  public final String getSecondryItemName()
  {
    return this.template.getSecondryItemName();
  }
  
  public final byte getMailTimes()
  {
    return this.mailTimes;
  }
  
  public abstract void setColor(int paramInt);
  
  public abstract void setColor2(int paramInt);
  
  final boolean isFemale()
  {
    return this.female;
  }
  
  public final boolean isMushroom()
  {
    return this.template.isMushroom();
  }
  
  public abstract void setFemale(boolean paramBoolean);
  
  public final boolean isTransferred()
  {
    return this.transferred;
  }
  
  public final boolean isMagicContainer()
  {
    return this.template.magicContainer;
  }
  
  public final boolean willLeaveServer(boolean leaving, boolean changingCluster, boolean ownerDeity)
  {
    if (isBodyPartAttached()) {
      return true;
    }
    if (isServerBound()) {
      if ((isVillageDeed()) || (isHomesteadDeed()))
      {
        if (getData2() > 0)
        {
          if (leaving) {
            setTransferred(true);
          }
          return false;
        }
        if (leaving) {
          setTransferred(false);
        }
      }
      else
      {
        if (getTemplateId() == 166)
        {
          if (leaving) {
            setTransferred(true);
          }
          return false;
        }
        if ((getTemplateId() == 300) || (getTemplateId() == 1129))
        {
          if (getData() > 0L)
          {
            if (leaving) {
              setTransferred(true);
            }
            return false;
          }
          if (leaving) {
            setTransferred(false);
          }
        }
        else
        {
          if (isRoyal())
          {
            if (leaving) {
              setTransferred(true);
            }
            return false;
          }
          if (isArtifact())
          {
            if (leaving) {
              try
              {
                getParent().dropItem(getWurmId(), false);
                String act;
                String act;
                String act;
                String act;
                String act;
                String act;
                switch (Server.rand.nextInt(6))
                {
                case 0: 
                  act = "is reported to have disappeared."; break;
                case 1: 
                  act = "is gone missing."; break;
                case 2: 
                  act = "returned to the depths."; break;
                case 3: 
                  act = "seems to have decided to leave."; break;
                case 4: 
                  act = "has found a new location."; break;
                default: 
                  act = "has vanished.";
                }
                HistoryManager.addHistory("The " + getName(), act);
                int onethird = Zones.worldTileSizeX / 3;
                int ntx = onethird + Server.rand.nextInt(onethird);
                int nty = onethird + Server.rand.nextInt(onethird);
                float npx = ntx * 4 + 2;
                float npy = nty * 4 + 2;
                setPosXY(npx, npy);
                
                Zone z = Zones.getZone(ntx, nty, true);
                z.addItem(this);
              }
              catch (NoSuchItemException nsi)
              {
                logWarn(getName() + ", " + getWurmId() + " no parent " + nsi.getMessage(), nsi);
              }
              catch (NoSuchZoneException nz)
              {
                logWarn(getName() + ", " + getWurmId() + " no zone " + nz.getMessage(), nz);
              }
            }
            return false;
          }
          if (leaving) {
            setTransferred(true);
          }
          return false;
        }
      }
    }
    if ((changingCluster) && (!ownerDeity))
    {
      if (isNewbieItem()) {
        return true;
      }
      if (leaving) {
        setTransferred(true);
      }
      return false;
    }
    if (isTransferred()) {
      setTransferred(false);
    }
    return true;
  }
  
  public final int getFloorLevel()
  {
    try
    {
      Vector2f pos2f = getPos2f();
      TilePos tilePos = CoordUtils.WorldToTile(pos2f);
      Zone zone = Zones.getZone(tilePos, isOnSurface());
      VolaTile tile = zone.getOrCreateTile(tilePos);
      if (tile.getStructure() == null) {
        return 0;
      }
      float posZ = getPosZ();
      
      long bridgeId = getBridgeId();
      float z2;
      float z2;
      if (bridgeId > 0L) {
        z2 = Zones.calculatePosZ(pos2f.x, pos2f.y, tile, isOnSurface(), false, posZ, null, bridgeId);
      } else {
        z2 = Zones.calculateHeight(pos2f.x, pos2f.y, isOnSurface());
      }
      return (int)(Math.max(0.0F, posZ - z2 + 0.5F) * 10.0F) / 30;
    }
    catch (NoSuchZoneException snz) {}
    return 0;
  }
  
  public abstract void setTransferred(boolean paramBoolean);
  
  abstract void addNewKey(long paramLong);
  
  abstract void removeNewKey(long paramLong);
  
  public final boolean isMailed()
  {
    return this.mailed;
  }
  
  public abstract void setMailed(boolean paramBoolean);
  
  abstract void clear(long paramLong1, String paramString1, float paramFloat1, float paramFloat2, float paramFloat3, float paramFloat4, String paramString2, String paramString3, float paramFloat5, byte paramByte1, byte paramByte2, long paramLong2);
  
  final boolean isHidden()
  {
    return this.hidden;
  }
  
  public final boolean isSpringFilled()
  {
    return this.template.isSpringFilled;
  }
  
  public final boolean isTurnable()
  {
    if (this.permissions.hasPermission(Permissions.Allow.NOT_TURNABLE.getBit())) {
      return false;
    }
    return templateTurnable();
  }
  
  public final boolean templateTurnable()
  {
    return this.template.turnable;
  }
  
  public final boolean templateNoTake()
  {
    if ((isUnfinished()) && (this.realTemplate > 0))
    {
      ItemTemplate realTemplate = getRealTemplate();
      assert (realTemplate != null);
      return realTemplate.isUnfinishedNoTake;
    }
    return this.template.notake;
  }
  
  public final boolean isColorable()
  {
    if (this.permissions.hasPermission(Permissions.Allow.NOT_PAINTABLE.getBit())) {
      return false;
    }
    return templateIsColorable();
  }
  
  public final boolean isSourceSpring()
  {
    return this.template.isSourceSpring;
  }
  
  public final boolean isSource()
  {
    return this.template.isSource;
  }
  
  public byte getRarity()
  {
    return this.rarity;
  }
  
  public final boolean isDredgingTool()
  {
    return this.template.isDredgingTool;
  }
  
  public final boolean isTent()
  {
    return this.template.isTent();
  }
  
  public final boolean isUseMaterialAndKingdom()
  {
    return this.template.useMaterialAndKingdom;
  }
  
  public final void setProtected(boolean isProtected)
  {
    Items.setProtected(getWurmId(), isProtected);
  }
  
  public final boolean isCorpseLootable()
  {
    return !Items.isProtected(this);
  }
  
  public static ItemTransferDatabaseLogger getItemlogger()
  {
    return itemLogger;
  }
  
  public final boolean isColorComponent()
  {
    return this.template.isColorComponent;
  }
  
  public abstract void setHidden(boolean paramBoolean);
  
  public abstract void setOwnerStuff(ItemTemplate paramItemTemplate);
  
  public abstract boolean setRarity(byte paramByte);
  
  public long onBridge()
  {
    return this.onBridge;
  }
  
  public void setOnBridge(long theBridge)
  {
    this.onBridge = theBridge;
  }
  
  public final void setSettings(int newSettings)
  {
    this.permissions.setPermissionBits(newSettings);
  }
  
  public final Permissions getSettings()
  {
    return this.permissions;
  }
  
  public final int hashCode()
  {
    int prime = 31;
    int result = 1;
    result = 31 * result + (int)(this.id ^ this.id >>> 32);
    return result;
  }
  
  public final boolean equals(Object obj)
  {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof Item)) {
      return false;
    }
    Item other = (Item)obj;
    if (this.id != other.id) {
      return false;
    }
    return true;
  }
  
  @Nonnull
  public String toString()
  {
    String toReturn = "Item [ID: " + getWurmId() + ", Name: " + getName();
    if ((getTemplate() != null) && 
      (getTemplate().getName() != null)) {
      toReturn = toReturn + ", Template: " + getTemplate().getName();
    }
    if ((getRealTemplate() != null) && 
      (getRealTemplate().getName() != null)) {
      toReturn = toReturn + ", Realtemplate: " + getRealTemplate().getName();
    }
    toReturn = toReturn + ", QL: " + getQualityLevel() + ", Rarity: " + getRarity();
    toReturn = toReturn + ", Tile: " + getTileX() + ',' + getTileY() + ']';
    
    return toReturn;
  }
  
  public boolean isInTheNorthWest()
  {
    return (getTileX() < Zones.worldTileSizeX / 3) && 
      (getTileY() < Zones.worldTileSizeY / 3);
  }
  
  public boolean isInTheNorthEast()
  {
    return (getTileX() > Zones.worldTileSizeX - Zones.worldTileSizeX / 3) && 
      (getTileY() < Zones.worldTileSizeY / 3);
  }
  
  public boolean isInTheSouthEast()
  {
    return (getTileX() > Zones.worldTileSizeX - Zones.worldTileSizeX / 3) && 
      (getTileY() > Zones.worldTileSizeY - Zones.worldTileSizeY / 3);
  }
  
  public boolean isInTheSouthWest()
  {
    return (getTileX() < Zones.worldTileSizeX / 3) && 
      (getTileY() > Zones.worldTileSizeY - Zones.worldTileSizeY / 3);
  }
  
  public boolean isInTheNorth()
  {
    return getTileY() < Zones.worldTileSizeY / 3;
  }
  
  public boolean isInTheEast()
  {
    return getTileX() > Zones.worldTileSizeX - Zones.worldTileSizeX / 3;
  }
  
  public boolean isInTheSouth()
  {
    return getTileY() > Zones.worldTileSizeY - Zones.worldTileSizeY / 3;
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
    return -1;
  }
  
  public boolean isOwnedByWagoner()
  {
    if (WurmId.getType(this.lastOwner) == 1) {
      return Wagoner.getWagoner(this.lastOwner) != null;
    }
    return false;
  }
  
  public final boolean isWagonerCamp()
  {
    long data = getData();
    if (data == -1L) {
      return false;
    }
    if (WurmId.getType(data) == 1) {
      if (Wagoner.getWagoner(data) != null) {
        return true;
      }
    }
    setData(-10L);
    return false;
  }
  
  public abstract void setDbStrings(DbStrings paramDbStrings);
  
  public abstract DbStrings getDbStrings();
  
  public abstract void setMailTimes(byte paramByte);
  
  public abstract void moveToFreezer();
  
  public abstract void returnFromFreezer();
  
  public abstract void deleteInDatabase();
  
  public final int getPotionTemplateIdForBlood()
  {
    int toReturn = -1;
    switch (getData2())
    {
    case 27: 
      toReturn = 881;
      break;
    case 26: 
      toReturn = 874;
      break;
    case 16: 
      toReturn = 872;
      break;
    case 103: 
      toReturn = 871;
      break;
    case 18: 
      toReturn = 876;
      break;
    case 17: 
      toReturn = 886;
      break;
    case 19: 
      toReturn = 888;
      break;
    case 22: 
      toReturn = 879;
      break;
    case 20: 
      toReturn = 883;
      break;
    case 70: 
      toReturn = 880;
      break;
    case 89: 
      toReturn = 884;
      break;
    case 92: 
      toReturn = 878;
      break;
    case 91: 
      toReturn = 1413;
      break;
    case 104: 
      toReturn = 877;
      break;
    case 90: 
      toReturn = 875;
      break;
    default: 
      toReturn = 884;
    }
    return toReturn;
  }
  
  public static final int getRandomImbuePotionTemplateId()
  {
    int toReturn = -1;
    switch (Server.rand.nextInt(17))
    {
    case 0: 
      toReturn = 881;
      break;
    case 1: 
      toReturn = 874;
      break;
    case 2: 
      toReturn = 872;
      break;
    case 3: 
      toReturn = 871;
      break;
    case 4: 
      toReturn = 876;
      break;
    case 5: 
      toReturn = 886;
      break;
    case 6: 
      toReturn = 888;
      break;
    case 7: 
      toReturn = 879;
      break;
    case 8: 
      toReturn = 883;
      break;
    case 9: 
      toReturn = 880;
      break;
    case 10: 
      toReturn = 882;
      break;
    case 11: 
      toReturn = 878;
      break;
    case 12: 
      toReturn = 873;
      break;
    case 13: 
      toReturn = 877;
      break;
    case 14: 
      toReturn = 875;
      break;
    case 15: 
      toReturn = 1413;
      break;
    case 16: 
    default: 
      toReturn = 884;
    }
    return toReturn;
  }
  
  public final boolean isSmearable()
  {
    return this.template.isSmearable();
  }
  
  public final boolean canBePapyrusWrapped()
  {
    return this.template.canBePapyrusWrapped();
  }
  
  public final boolean canBeRawWrapped()
  {
    return this.template.canBeRawWrapped();
  }
  
  public final boolean canBeClothWrapped()
  {
    return this.template.canBeClothWrapped();
  }
  
  public final byte getEnchantForPotion()
  {
    byte toReturn = -1;
    switch (getTemplateId())
    {
    case 881: 
      toReturn = 86;
      break;
    case 874: 
      toReturn = 79;
      break;
    case 871: 
      toReturn = 77;
      break;
    case 876: 
      toReturn = 81;
      break;
    case 886: 
      toReturn = 90;
      break;
    case 888: 
      toReturn = 92;
      break;
    case 879: 
      toReturn = 84;
      break;
    case 883: 
      toReturn = 88;
      break;
    case 880: 
      toReturn = 85;
      break;
    case 872: 
      toReturn = 78;
      break;
    case 873: 
      toReturn = 76;
      break;
    case 875: 
      toReturn = 80;
      break;
    case 877: 
      toReturn = 82;
      break;
    case 878: 
      toReturn = 83;
      break;
    case 887: 
      toReturn = 91;
      break;
    case 884: 
      toReturn = 89;
      break;
    case 882: 
      toReturn = 87;
      break;
    case 1413: 
      toReturn = 99;
      break;
    case 1091: 
      toReturn = 98;
      break;
    default: 
      toReturn = -1;
    }
    return toReturn;
  }
  
  public final boolean mayFireTrebuchet()
  {
    return WurmCalendar.currentTime - getLastMaintained() > 120L;
  }
  
  public final boolean canBeAlwaysLit()
  {
    return (isLight()) && (!templateAlwaysLit());
  }
  
  public final boolean canBeAutoFilled()
  {
    return isSpringFilled();
  }
  
  public final boolean canBeAutoLit()
  {
    return (isLight()) && (!templateAlwaysLit());
  }
  
  public final boolean canBePlanted()
  {
    return this.template.isPlantable();
  }
  
  public final boolean isPlantOneAWeek()
  {
    return this.template.isPlantOneAWeeek();
  }
  
  public final boolean descIsName()
  {
    return this.template.descIsName;
  }
  
  public final boolean canBeSealedByPlayer()
  {
    if (!this.template.canBeSealed()) {
      return false;
    }
    Item[] items = getItemsAsArray();
    return (items.length == 1) && (items[0].isLiquid());
  }
  
  public final boolean canBePeggedByPlayer()
  {
    if (!this.template.canBePegged()) {
      return false;
    }
    Item[] items = getItemsAsArray();
    return (items.length == 1) && (items[0].isLiquid()) && (!items[0].isFermenting());
  }
  
  public boolean canChangeCreator()
  {
    return true;
  }
  
  public final boolean canDisableDecay()
  {
    return true;
  }
  
  public final boolean canDisableDestroy()
  {
    return !templateIndestructible();
  }
  
  public boolean canDisableDrag()
  {
    return this.template.draggable;
  }
  
  public boolean canDisableDrop()
  {
    return !this.template.nodrop;
  }
  
  public boolean canDisableEatAndDrink()
  {
    return (isFood()) || (isLiquid());
  }
  
  public boolean canDisableImprove()
  {
    return !this.template.isNoImprove();
  }
  
  public final boolean canDisableLocking()
  {
    return templateIsLockable();
  }
  
  public final boolean canDisableLockpicking()
  {
    return templateIsLockable();
  }
  
  public final boolean canDisableMoveable()
  {
    return !templateIsNoMove();
  }
  
  public final boolean canDisableOwnerMoveing()
  {
    return !this.template.isOwnerMoveable;
  }
  
  public final boolean canDisableOwnerTurning()
  {
    return !this.template.isOwnerTurnable;
  }
  
  public final boolean canDisablePainting()
  {
    return templateIsColorable();
  }
  
  public boolean canDisablePut()
  {
    return !this.template.isNoPut;
  }
  
  public boolean canDisableRepair()
  {
    return isRepairableDefault();
  }
  
  public boolean canDisableRuneing()
  {
    return !this.template.isNotRuneable;
  }
  
  public final boolean canDisableSpellTarget()
  {
    return !this.template.cannotBeSpellTarget();
  }
  
  public final boolean canDisableTake()
  {
    return !templateNoTake();
  }
  
  public final boolean canDisableTurning()
  {
    return templateTurnable();
  }
  
  public final boolean canHaveCourier()
  {
    return (isMailBox()) || (isSpringFilled()) || (isUnenchantedTurret()) || (isPuppet());
  }
  
  public final boolean canHaveDakrMessenger()
  {
    return (isMailBox()) || (isSpringFilled()) || (isUnenchantedTurret()) || (isPuppet());
  }
  
  @Nonnull
  public final String getCreatorName()
  {
    if ((this.creator != null) && (!this.creator.isEmpty())) {
      return this.creator;
    }
    return "";
  }
  
  public abstract float getDamage();
  
  public abstract float getQualityLevel();
  
  public final boolean hasCourier()
  {
    return this.permissions.hasPermission(Permissions.Allow.HAS_COURIER.getBit());
  }
  
  public final boolean hasDarkMessenger()
  {
    return this.permissions.hasPermission(Permissions.Allow.HAS_DARK_MESSENGER.getBit());
  }
  
  public final boolean hasNoDecay()
  {
    return this.permissions.hasPermission(Permissions.Allow.DECAY_DISABLED.getBit());
  }
  
  public final boolean isAlwaysLit()
  {
    if (this.permissions.hasPermission(Permissions.Allow.ALWAYS_LIT.getBit())) {
      return true;
    }
    return templateAlwaysLit();
  }
  
  public final boolean isAutoFilled()
  {
    return this.permissions.hasPermission(Permissions.Allow.AUTO_FILL.getBit());
  }
  
  public final boolean isAutoLit()
  {
    return this.permissions.hasPermission(Permissions.Allow.AUTO_LIGHT.getBit());
  }
  
  public final boolean isIndestructible()
  {
    if (this.permissions.hasPermission(Permissions.Allow.NO_BASH.getBit())) {
      return true;
    }
    if (getTemplateId() == 1112) {
      if ((getData() != -1L) || (Items.isWaystoneInUse(getWurmId()))) {
        return true;
      }
    }
    if ((getTemplateId() == 1309) && (isSealedByPlayer())) {
      return true;
    }
    return templateIndestructible();
  }
  
  public final boolean isNoDrag()
  {
    if (this.permissions.hasPermission(Permissions.Allow.NO_DRAG.getBit())) {
      return true;
    }
    return !this.template.draggable;
  }
  
  public final boolean isNoDrop()
  {
    if (this.permissions.hasPermission(Permissions.Allow.NO_DROP.getBit())) {
      return true;
    }
    if (this.realTemplate > 0)
    {
      ItemTemplate realTemplate = getRealTemplate();
      assert (realTemplate != null);
      return realTemplate.nodrop;
    }
    return this.template.nodrop;
  }
  
  public boolean isNoEatOrDrink()
  {
    return this.permissions.hasPermission(Permissions.Allow.NO_EAT_OR_DRINK.getBit());
  }
  
  public final boolean isNoImprove()
  {
    if (this.permissions.hasPermission(Permissions.Allow.NO_IMPROVE.getBit())) {
      return true;
    }
    return this.template.isNoImprove();
  }
  
  public final boolean isNoMove()
  {
    if (this.permissions.hasPermission(Permissions.Allow.NOT_MOVEABLE.getBit())) {
      return true;
    }
    return templateIsNoMove();
  }
  
  public final boolean isNoPut()
  {
    if (this.permissions.hasPermission(Permissions.Allow.NO_PUT.getBit())) {
      return true;
    }
    if (getTemplateId() == 1342) {
      return (!isPlanted()) && (getData() == -1L);
    }
    return this.template.isNoPut;
  }
  
  public boolean isNoRepair()
  {
    return !isRepairable();
  }
  
  public final boolean isNoTake()
  {
    if (this.permissions.hasPermission(Permissions.Allow.NO_TAKE.getBit())) {
      return true;
    }
    if (templateNoTake()) {
      return true;
    }
    if (((getTemplateId() == 1312) || (getTemplateId() == 1309)) && (!isEmpty(false))) {
      return true;
    }
    if ((getTemplateId() == 1315) && (!isEmpty(false))) {
      return true;
    }
    return false;
  }
  
  public final boolean isNoTake(Creature creature)
  {
    if (isNoTake()) {
      return true;
    }
    if ((getTemplateId() == 272) && (this.wasBrandedTo != -10L))
    {
      if (mayCommand(creature)) {
        return false;
      }
      return true;
    }
    return false;
  }
  
  public final boolean isNotLockable()
  {
    return !isLockable();
  }
  
  public final boolean isLockable()
  {
    if (this.permissions.hasPermission(Permissions.Allow.NOT_LOCKABLE.getBit())) {
      return false;
    }
    return templateIsLockable();
  }
  
  public final boolean isNotLockpickable()
  {
    return !isLockpickable();
  }
  
  public final boolean isLockpickable()
  {
    if (this.permissions.hasPermission(Permissions.Allow.NOT_LOCKPICKABLE.getBit())) {
      return false;
    }
    return templateIsLockable();
  }
  
  public final boolean isNotPaintable()
  {
    return !isColorable();
  }
  
  public boolean isNotRuneable()
  {
    if (this.permissions.hasPermission(Permissions.Allow.NOT_RUNEABLE.getBit())) {
      return true;
    }
    return this.template.isNotRuneable;
  }
  
  public final boolean isNotSpellTarget()
  {
    if (this.permissions.hasPermission(Permissions.Allow.NO_SPELLS.getBit())) {
      return true;
    }
    return this.template.cannotBeSpellTarget();
  }
  
  public final boolean isNotTurnable()
  {
    return !isTurnable();
  }
  
  public boolean isOwnerMoveable()
  {
    if (this.permissions.hasPermission(Permissions.Allow.OWNER_MOVEABLE.getBit())) {
      return true;
    }
    return this.template.isOwnerMoveable;
  }
  
  public boolean isOwnerTurnable()
  {
    if (this.permissions.hasPermission(Permissions.Allow.OWNER_TURNABLE.getBit())) {
      return true;
    }
    return this.template.isOwnerTurnable;
  }
  
  public final boolean isPlanted()
  {
    return this.permissions.hasPermission(Permissions.Allow.PLANTED.getBit());
  }
  
  public final boolean isSealedByPlayer()
  {
    if (this.isSealedOverride) {
      return true;
    }
    if (this.permissions.hasPermission(Permissions.Allow.SEALED_BY_PLAYER.getBit())) {
      return true;
    }
    return false;
  }
  
  public abstract void setCreator(String paramString);
  
  public abstract boolean setDamage(float paramFloat);
  
  public final void setHasCourier(boolean aCourier)
  {
    this.permissions.setPermissionBit(Permissions.Allow.HAS_COURIER.getBit(), aCourier);
    
    savePermissions();
  }
  
  public final void setHasDarkMessenger(boolean aDarkmessenger)
  {
    this.permissions.setPermissionBit(Permissions.Allow.HAS_DARK_MESSENGER.getBit(), aDarkmessenger);
    
    savePermissions();
  }
  
  public final void setHasNoDecay(boolean aNoDecay)
  {
    this.permissions.setPermissionBit(Permissions.Allow.DECAY_DISABLED.getBit(), aNoDecay);
  }
  
  public final void setIsAlwaysLit(boolean aAlwaysLit)
  {
    this.permissions.setPermissionBit(Permissions.Allow.ALWAYS_LIT.getBit(), aAlwaysLit);
  }
  
  public final void setIsAutoFilled(boolean aAutoFill)
  {
    this.permissions.setPermissionBit(Permissions.Allow.AUTO_FILL.getBit(), aAutoFill);
  }
  
  public final void setIsAutoLit(boolean aAutoLight)
  {
    this.permissions.setPermissionBit(Permissions.Allow.AUTO_LIGHT.getBit(), aAutoLight);
  }
  
  public final void setIsIndestructible(boolean aNoDestroy)
  {
    this.permissions.setPermissionBit(Permissions.Allow.NO_BASH.getBit(), aNoDestroy);
  }
  
  public void setIsNoDrag(boolean aNoDrag)
  {
    this.permissions.setPermissionBit(Permissions.Allow.NO_DRAG.getBit(), aNoDrag);
  }
  
  public void setIsNoDrop(boolean aNoDrop)
  {
    this.permissions.setPermissionBit(Permissions.Allow.NO_DROP.getBit(), aNoDrop);
  }
  
  public void setIsNoEatOrDrink(boolean aNoEatOrDrink)
  {
    this.permissions.setPermissionBit(Permissions.Allow.NO_EAT_OR_DRINK.getBit(), aNoEatOrDrink);
  }
  
  public void setIsNoImprove(boolean aNoImprove)
  {
    this.permissions.setPermissionBit(Permissions.Allow.NO_IMPROVE.getBit(), aNoImprove);
  }
  
  public final void setIsNoMove(boolean aNoMove)
  {
    this.permissions.setPermissionBit(Permissions.Allow.NOT_MOVEABLE.getBit(), aNoMove);
  }
  
  public void setIsNoPut(boolean aNoPut)
  {
    this.permissions.setPermissionBit(Permissions.Allow.NO_PUT.getBit(), aNoPut);
  }
  
  public void setIsNoRepair(boolean aNoRepair)
  {
    this.permissions.setPermissionBit(Permissions.Allow.NO_REPAIR.getBit(), aNoRepair);
  }
  
  public final void setIsNoTake(boolean aNoTake)
  {
    this.permissions.setPermissionBit(Permissions.Allow.NO_TAKE.getBit(), aNoTake);
  }
  
  public final void setIsNotLockable(boolean aNoLock)
  {
    this.permissions.setPermissionBit(Permissions.Allow.NOT_LOCKABLE.getBit(), aNoLock);
  }
  
  public final void setIsNotLockpickable(boolean aNoLockpick)
  {
    this.permissions.setPermissionBit(Permissions.Allow.NOT_LOCKPICKABLE.getBit(), aNoLockpick);
  }
  
  public final void setIsNotPaintable(boolean aNoPaint)
  {
    this.permissions.setPermissionBit(Permissions.Allow.NOT_PAINTABLE.getBit(), aNoPaint);
  }
  
  public void setIsNotRuneable(boolean aNoRune)
  {
    this.permissions.setPermissionBit(Permissions.Allow.NOT_RUNEABLE.getBit(), aNoRune);
  }
  
  public final void setIsNotSpellTarget(boolean aNoSpells)
  {
    this.permissions.setPermissionBit(Permissions.Allow.NO_SPELLS.getBit(), aNoSpells);
  }
  
  public final void setIsNotTurnable(boolean aNoTurn)
  {
    this.permissions.setPermissionBit(Permissions.Allow.NOT_TURNABLE.getBit(), aNoTurn);
  }
  
  public void setIsOwnerMoveable(boolean aOwnerMove)
  {
    this.permissions.setPermissionBit(Permissions.Allow.OWNER_MOVEABLE.getBit(), aOwnerMove);
  }
  
  public void setIsOwnerTurnable(boolean aOwnerTurn)
  {
    this.permissions.setPermissionBit(Permissions.Allow.OWNER_TURNABLE.getBit(), aOwnerTurn);
  }
  
  public final void setIsPlanted(boolean aPlant)
  {
    boolean wasPlanted = isPlanted();
    this.permissions.setPermissionBit(Permissions.Allow.PLANTED.getBit(), aPlant);
    if (isRoadMarker()) {
      if ((!aPlant) && (wasPlanted))
      {
        MethodsHighways.removeLinksTo(this);
        setIsNoTake(false);
      }
      else if ((aPlant) && (!wasPlanted))
      {
        setIsNoTake(true);
        this.replacing = false;
      }
    }
    if (getTemplateId() == 1396) {
      if ((!aPlant) && (wasPlanted))
      {
        setTemperature((short)200);
        updateIfGroundItem();
      }
      else if ((aPlant) && (!wasPlanted))
      {
        updateIfGroundItem();
      }
    }
    if (getTemplateId() == 1342) {
      if ((!aPlant) && (wasPlanted)) {
        setIsNoMove(false);
      } else if ((aPlant) && (!wasPlanted)) {
        setIsNoMove(true);
      }
    }
    if (getTemplateId() == 677) {
      if ((!aPlant) && (wasPlanted))
      {
        Items.removeGmSign(this);
        setIsNoTake(false);
      }
      else if ((aPlant) && (!wasPlanted))
      {
        Items.addGmSign(this);
        setIsNoTake(true);
      }
    }
    if (getTemplateId() == 1309) {
      if ((!aPlant) && (wasPlanted))
      {
        Items.removeWagonerContainer(this);
        setData(-1L);
      }
      else if ((!aPlant) || (wasPlanted)) {}
    }
    savePermissions();
  }
  
  public void setIsSealedByPlayer(boolean aSealed)
  {
    this.permissions.setPermissionBit(Permissions.Allow.SEALED_BY_PLAYER.getBit(), aSealed);
    this.isSealedOverride = false;
    Item item;
    if (!aSealed)
    {
      for (item : getItemsAsArray()) {
        item.setLastMaintained(WurmCalendar.getCurrentTime());
      }
      if (getTemplateId() == 1309) {
        Delivery.freeContainer(getWurmId());
      }
    }
    updateName();
    setIsNoPut(aSealed);
    
    savePermissions();
    
    Item topParent = getTopParentOrNull();
    long inventoryWindow;
    if ((topParent != null) && (topParent.isHollow()))
    {
      if (this.watchers == null) {
        return;
      }
      inventoryWindow = getTopParent();
      if (topParent.isInventory()) {
        inventoryWindow = -1L;
      }
      for (Creature watcher : this.watchers)
      {
        watcher.getCommunicator().sendRemoveFromInventory(this, inventoryWindow);
        watcher.getCommunicator().sendAddToInventory(this, inventoryWindow, -1L, -1);
      }
    }
  }
  
  public abstract boolean setQualityLevel(float paramFloat);
  
  public abstract void savePermissions();
  
  public String getObjectName()
  {
    return getDescription().replace("\"", "'");
  }
  
  public String getTypeName()
  {
    return getActualName().replace("\"", "'");
  }
  
  public boolean setObjectName(String newName, Creature creature)
  {
    return setDescription(newName);
  }
  
  public boolean isActualOwner(long playerId)
  {
    return playerId == this.lastOwner;
  }
  
  public boolean isOwner(Creature creature)
  {
    return isOwner(creature.getWurmId());
  }
  
  public boolean isOwner(long playerId)
  {
    if (isOwnedByWagoner()) {
      return false;
    }
    if (isBed())
    {
      VolaTile vt = Zones.getTileOrNull(getTilePos(), isOnSurface());
      Structure structure = vt != null ? vt.getStructure() : null;
      if ((structure != null) && (structure.isTypeHouse()) && (structure.isFinished())) {
        return structure.isOwner(playerId);
      }
      return false;
    }
    return isActualOwner(playerId);
  }
  
  public boolean canChangeName(Creature creature)
  {
    if (getTemplateId() == 272) {
      return false;
    }
    return (isOwner(creature)) || (creature.getPower() >= 2);
  }
  
  public boolean canChangeOwner(Creature creature)
  {
    if ((isBed()) || (isNoTrade()) || (getTemplateId() == 272)) {
      return false;
    }
    return (creature.getPower() > 1) || (isOwner(creature));
  }
  
  public boolean setNewOwner(long playerId)
  {
    if (ItemSettings.exists(getWurmId()))
    {
      ItemSettings.remove(getWurmId());
      PermissionsHistories.addHistoryEntry(getWurmId(), System.currentTimeMillis(), -10L, "Auto", "Cleared Permissions");
    }
    setLastOwnerId(playerId);
    return true;
  }
  
  public String getOwnerName()
  {
    return PlayerInfoFactory.getPlayerName(this.lastOwner);
  }
  
  public String getWarning()
  {
    if (isOwnedByWagoner()) {
      return "WAGONER OWNS THIS";
    }
    if (isBed())
    {
      VolaTile vt = Zones.getTileOrNull(getTilePos(), isOnSurface());
      Structure structure = vt != null ? vt.getStructure() : null;
      if ((structure == null) || (!structure.isTypeHouse())) {
        return "BED NEEDS TO BE INSIDE A BUILDING TO WORK";
      }
      if (!structure.isFinished()) {
        return "BED NEEDS TO BE IN FINISHED BUILDING TO WORK";
      }
      return "";
    }
    if (getTemplateId() == 1271) {
      return "";
    }
    if (getTemplateId() == 272)
    {
      if (this.wasBrandedTo != -10L) {
        return "VIEW ONLY";
      }
      return "NEEDS TO HAVE BEEN BRANDED TO SEE PERMISSIONS";
    }
    if (getLockId() == -10L) {
      return "NEEDS TO HAVE A LOCK FOR PERMISSIONS TO WORK";
    }
    if (!isLocked()) {
      return "NEEDS TO BE LOCKED OTHERWISE EVERYONE CAN USE THIS";
    }
    return "";
  }
  
  public PermissionsPlayerList getPermissionsPlayerList()
  {
    return ItemSettings.getPermissionsPlayerList(getWurmId());
  }
  
  public boolean isManaged()
  {
    return this.permissions.hasPermission(Permissions.Allow.SETTLEMENT_MAY_MANAGE.getBit());
  }
  
  public boolean isManageEnabled(Player player)
  {
    return false;
  }
  
  public void setIsManaged(boolean newIsManaged, Player player) {}
  
  public String mayManageText(Player aPlayer)
  {
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
    if (isOwnedByWagoner()) {
      return "";
    }
    if (isBed())
    {
      VolaTile vt = Zones.getTileOrNull(getTilePos(), isOnSurface());
      Structure structure = vt != null ? vt.getStructure() : null;
      if ((structure != null) && (structure.isTypeHouse()) && (structure.isFinished())) {
        return structure.getSettlementName();
      }
      return "";
    }
    if (getTemplateId() == 272)
    {
      if (this.wasBrandedTo != -10L) {
        try
        {
          Village lbVillage = Villages.getVillage((int)this.wasBrandedTo);
          return "Citizens of \"" + lbVillage.getName() + "\"";
        }
        catch (NoSuchVillageException e)
        {
          logger.log(Level.WARNING, e.getMessage(), e);
        }
      }
    }
    else
    {
      Village loVillage = Villages.getVillageForCreature(this.lastOwner);
      if (loVillage != null) {
        return "Citizens of \"" + loVillage.getName() + "\"";
      }
    }
    return "";
  }
  
  public String getAllianceName()
  {
    if (isOwnedByWagoner()) {
      return "";
    }
    if (isBed())
    {
      VolaTile vt = Zones.getTileOrNull(getTileX(), getTileY(), isOnSurface());
      Structure structure = vt != null ? vt.getStructure() : null;
      if ((structure != null) && (structure.isTypeHouse()) && (structure.isFinished())) {
        return structure.getAllianceName();
      }
      return "";
    }
    if (getTemplateId() == 272)
    {
      if (this.wasBrandedTo != -10L) {
        try
        {
          Village lbVillage = Villages.getVillage((int)this.wasBrandedTo);
          if ((lbVillage != null) && (lbVillage.getAllianceNumber() > 0)) {
            return "Alliance of \"" + lbVillage.getAllianceName() + "\"";
          }
        }
        catch (NoSuchVillageException e)
        {
          logger.log(Level.WARNING, e.getMessage(), e);
        }
      }
    }
    else
    {
      Village loVillage = Villages.getVillageForCreature(this.lastOwner);
      if ((loVillage != null) && (loVillage.getAllianceNumber() > 0)) {
        return "Alliance of \"" + loVillage.getAllianceName() + "\"";
      }
    }
    return "";
  }
  
  public String getKingdomName()
  {
    String toReturn = "";
    byte kingdom = getKingdom();
    if ((isVehicle()) && (kingdom != 0)) {
      toReturn = "Kingdom of \"" + Kingdoms.getNameFor(kingdom) + "\"";
    }
    return toReturn;
  }
  
  public boolean canAllowEveryone()
  {
    return true;
  }
  
  public String getRolePermissionName()
  {
    return "";
  }
  
  public boolean isCitizen(Creature creature)
  {
    if (isOwnedByWagoner()) {
      return false;
    }
    if (isBed())
    {
      VolaTile vt = Zones.getTileOrNull(getTilePos(), isOnSurface());
      Structure structure = vt != null ? vt.getStructure() : null;
      if ((structure != null) && (structure.isTypeHouse()) && (structure.isFinished())) {
        return structure.isCitizen(creature);
      }
      return false;
    }
    if (getTemplateId() == 272)
    {
      if (this.wasBrandedTo != -10L) {
        try
        {
          Village lbVillage = Villages.getVillage((int)this.wasBrandedTo);
          if (lbVillage != null) {
            return lbVillage.isCitizen(creature);
          }
        }
        catch (NoSuchVillageException e)
        {
          logger.log(Level.WARNING, e.getMessage(), e);
        }
      }
    }
    else
    {
      Village ownerVillage = Villages.getVillageForCreature(getLastOwnerId());
      if (ownerVillage != null) {
        return ownerVillage.isCitizen(creature);
      }
    }
    return false;
  }
  
  public boolean isAllied(Creature creature)
  {
    if (isOwnedByWagoner()) {
      return false;
    }
    if (isBed())
    {
      VolaTile vt = Zones.getTileOrNull(getTilePos(), isOnSurface());
      Structure structure = vt != null ? vt.getStructure() : null;
      if ((structure != null) && (structure.isTypeHouse()) && (structure.isFinished())) {
        return structure.isAllied(creature);
      }
      return false;
    }
    if (getTemplateId() == 272)
    {
      if (this.wasBrandedTo != -10L) {
        try
        {
          Village lbVillage = Villages.getVillage((int)this.wasBrandedTo);
          if (lbVillage != null) {
            return lbVillage.isAlly(creature);
          }
        }
        catch (NoSuchVillageException e)
        {
          logger.log(Level.WARNING, e.getMessage(), e);
        }
      }
    }
    else
    {
      Village ownerVillage = Villages.getVillageForCreature(getLastOwnerId());
      if (ownerVillage != null) {
        return ownerVillage.isAlly(creature);
      }
    }
    return false;
  }
  
  public boolean isSameKingdom(Creature creature)
  {
    if (isOwnedByWagoner()) {
      return false;
    }
    if (isBed())
    {
      VolaTile vt = Zones.getTileOrNull(getTilePos(), isOnSurface());
      Structure structure = vt != null ? vt.getStructure() : null;
      if ((structure != null) && (structure.isTypeHouse()) && (structure.isFinished())) {
        return structure.isSameKingdom(creature);
      }
      return false;
    }
    if ((isVehicle()) && (getKingdom() != 0)) {
      return getKingdom() == creature.getKingdomId();
    }
    return Players.getInstance().getKingdomForPlayer(getLastOwnerId()) == creature.getKingdomId();
  }
  
  public void addGuest(long guestId, int settings)
  {
    ItemSettings.addPlayer(this.id, guestId, settings);
  }
  
  public void removeGuest(long guestId)
  {
    ItemSettings.removePlayer(this.id, guestId);
  }
  
  public void addDefaultCitizenPermissions() {}
  
  public boolean isGuest(Creature creature)
  {
    return isGuest(creature.getWurmId());
  }
  
  public boolean isGuest(long playerId)
  {
    return ItemSettings.isGuest(this, playerId);
  }
  
  public void save()
    throws IOException
  {}
  
  public int getMaxAllowed()
  {
    return ItemSettings.getMaxAllowed();
  }
  
  public final boolean canHavePermissions()
  {
    if (isOwnedByWagoner()) {
      return false;
    }
    return (isLockable()) || (isBed()) || (getTemplateId() == 1271) || (
      (getTemplateId() == 272) && (this.wasBrandedTo != -10L));
  }
  
  public final boolean mayShowPermissions(Creature creature)
  {
    if (isOwnedByWagoner()) {
      return false;
    }
    if (isBed())
    {
      VolaTile vt = Zones.getTileOrNull(getTilePos(), isOnSurface());
      Structure structure = vt != null ? vt.getStructure() : null;
      return (structure != null) && (structure.isTypeHouse()) && (structure.isFinished()) && (mayManage(creature));
    }
    if (getTemplateId() == 1271) {
      return mayManage(creature);
    }
    if (getTemplateId() == 272) {
      return (this.wasBrandedTo != -10L) && (creature.getPower() > 1);
    }
    return (isLocked()) && (mayManage(creature));
  }
  
  public final boolean canManage(Creature creature)
  {
    if (isOwnedByWagoner()) {
      return false;
    }
    if (ItemSettings.isExcluded(this, creature)) {
      return false;
    }
    return ItemSettings.canManage(this, creature);
  }
  
  public final boolean mayManage(Creature creature)
  {
    if (isOwnedByWagoner()) {
      return false;
    }
    if (isBed())
    {
      VolaTile vt = Zones.getTileOrNull(getTilePos(), isOnSurface());
      Structure structure = vt != null ? vt.getStructure() : null;
      return (structure != null) && (structure.isTypeHouse()) && (structure.isFinished()) && 
        (structure.mayManage(creature));
    }
    if (creature.getPower() > 1) {
      return true;
    }
    return canManage(creature);
  }
  
  public final boolean maySeeHistory(Creature creature)
  {
    if (isOwnedByWagoner()) {
      return false;
    }
    if (creature.getPower() > 1) {
      return true;
    }
    return isOwner(creature);
  }
  
  public final boolean mayCommand(Creature creature)
  {
    if (isOwnedByWagoner()) {
      return false;
    }
    if (ItemSettings.isExcluded(this, creature)) {
      return false;
    }
    return (canHavePermissions()) && (ItemSettings.mayCommand(this, creature));
  }
  
  public final boolean mayPassenger(Creature creature)
  {
    if (isOwnedByWagoner()) {
      return false;
    }
    if (ItemSettings.isExcluded(this, creature)) {
      return false;
    }
    if ((isChair()) && (getData() == creature.getWurmId())) {
      return true;
    }
    return (canHavePermissions()) && (ItemSettings.mayPassenger(this, creature));
  }
  
  public final boolean mayAccessHold(Creature creature)
  {
    if (isOwnedByWagoner()) {
      return false;
    }
    if (ItemSettings.isExcluded(this, creature)) {
      return false;
    }
    if (!canHavePermissions()) {
      return true;
    }
    return ItemSettings.mayAccessHold(this, creature);
  }
  
  public final boolean mayUseBed(Creature creature)
  {
    if (isOwnedByWagoner()) {
      return false;
    }
    if (!ItemSettings.exists(getWurmId()))
    {
      VolaTile vt = Zones.getTileOrNull(getTilePos(), isOnSurface());
      Structure structure = vt != null ? vt.getStructure() : null;
      return (structure != null) && (structure.isTypeHouse());
    }
    if (ItemSettings.isExcluded(this, creature)) {
      return false;
    }
    return ItemSettings.mayUseBed(this, creature);
  }
  
  public final boolean mayFreeSleep(Creature creature)
  {
    if (isOwnedByWagoner()) {
      return false;
    }
    if (!ItemSettings.exists(getWurmId()))
    {
      VolaTile vt = Zones.getTileOrNull(getTilePos(), isOnSurface());
      Structure structure = vt != null ? vt.getStructure() : null;
      return (structure != null) && (structure.isTypeHouse()) && 
        (structure.mayPass(creature));
    }
    if (ItemSettings.isExcluded(this, creature)) {
      return false;
    }
    return ItemSettings.mayFreeSleep(this, creature);
  }
  
  public final boolean mayDrag(Creature creature)
  {
    if (isOwnedByWagoner()) {
      return false;
    }
    if ((creature.isOnPvPServer()) && (!isMooredBoat()) && (!isLocked())) {
      return true;
    }
    if (ItemSettings.isExcluded(this, creature)) {
      return false;
    }
    return (canHavePermissions()) && (ItemSettings.mayDrag(this, creature));
  }
  
  public final boolean mayPostNotices(Creature creature)
  {
    if (ItemSettings.isExcluded(this, creature)) {
      return false;
    }
    return (canHavePermissions()) && (ItemSettings.mayPostNotices(this, creature));
  }
  
  public final boolean mayAddPMs(Creature creature)
  {
    if (ItemSettings.isExcluded(this, creature)) {
      return false;
    }
    return (canHavePermissions()) && (ItemSettings.mayAddPMs(this, creature));
  }
  
  public long getWhenRented()
  {
    return this.whenRented;
  }
  
  public void setWhenRented(long when)
  {
    this.whenRented = when;
  }
  
  private static void logInfo(String msg)
  {
    logger.info(msg);
  }
  
  private static void logInfo(String msg, Throwable thrown)
  {
    logger.log(Level.INFO, msg, thrown);
  }
  
  private static void logWarn(String msg)
  {
    logger.warning(msg);
  }
  
  private static void logWarn(String msg, Throwable thrown)
  {
    logger.log(Level.WARNING, msg, thrown);
  }
  
  public boolean isPotable()
  {
    return this.template.isPotable();
  }
  
  public boolean usesFoodState()
  {
    return this.template.usesFoodState();
  }
  
  public boolean isRecipeItem()
  {
    return this.template.isRecipeItem();
  }
  
  public boolean isAlcohol()
  {
    return this.template.isAlcohol();
  }
  
  public boolean canBeDistilled()
  {
    return this.template.canBeDistilled();
  }
  
  public boolean canBeFermented()
  {
    return this.template.canBeFermented();
  }
  
  public boolean isCrushable()
  {
    return this.template.isCrushable();
  }
  
  public boolean isSurfaceOnly()
  {
    return this.template.isSurfaceOnly();
  }
  
  public boolean hasSeeds()
  {
    return this.template.hasSeeds();
  }
  
  public int getAlcoholStrength()
  {
    return this.template.getAlcoholStrength();
  }
  
  public void closeAll()
  {
    if (isHollow()) {
      if (this.watchers != null)
      {
        Creature[] watcherArray = (Creature[])this.watchers.toArray(new Creature[this.watchers.size()]);
        for (Creature watcher : watcherArray) {
          close(watcher);
        }
      }
    }
  }
  
  public void close(Creature performer)
  {
    if (isHollow()) {
      if (performer.getCommunicator().sendCloseInventoryWindow(getWurmId())) {
        if (getParentId() == -10L)
        {
          removeWatcher(performer, true);
        }
        else
        {
          boolean found = false;
          try
          {
            Creature[] crets = getParent().getWatchers();
            for (int x = 0; x < crets.length; x++) {
              if (crets[x].getWurmId() == performer.getWurmId())
              {
                found = true;
                break;
              }
            }
          }
          catch (NoSuchItemException localNoSuchItemException) {}catch (NoSuchCreatureException localNoSuchCreatureException) {}
          if (!found) {
            removeWatcher(performer, true);
          }
        }
      }
    }
  }
  
  public boolean hasQueen()
  {
    return getAuxData() > 0;
  }
  
  public boolean hasTwoQueens()
  {
    return getAuxData() > 1;
  }
  
  public void addQueen()
  {
    int queens = getAuxData();
    if (queens > 1) {
      return;
    }
    setAuxData((byte)(queens + 1));
    
    updateHiveModel();
    if (queens == 0) {
      Zones.addHive(this, false);
    }
  }
  
  public boolean removeQueen()
  {
    int queens = getAuxData();
    if (queens == 0) {
      return false;
    }
    setAuxData((byte)(queens - 1));
    
    updateHiveModel();
    if (queens == 1) {
      Zones.removeHive(this, false);
    }
    return true;
  }
  
  void updateHiveModel()
  {
    if ((getTemplateId() == 1239) || (hasTwoQueens())) {
      updateName();
    } else {
      try
      {
        Zone z = Zones.getZone(this.zoneId);
        z.removeItem(this);
        z.addItem(this);
      }
      catch (NoSuchZoneException nsz)
      {
        logWarn(
          "Zone at " + ((int)getPosX() >> 2) + "," + ((int)getPosY() >> 2) + ",surf=" + isOnSurface() + " no such zone. Item: " + this);
      }
    }
  }
  
  public boolean isPStateNone()
  {
    if (usesFoodState()) {
      return getLeftAuxData() == 0;
    }
    return true;
  }
  
  public void setRaw()
  {
    if (usesFoodState()) {
      setRightAuxData(0);
    }
  }
  
  public boolean isRaw()
  {
    if (usesFoodState()) {
      return getRightAuxData() == 0;
    }
    return false;
  }
  
  public void setIsFried()
  {
    if (usesFoodState()) {
      setRightAuxData(1);
    }
  }
  
  public boolean isFried()
  {
    if (usesFoodState()) {
      return getRightAuxData() == 1;
    }
    return false;
  }
  
  public void setIsGrilled()
  {
    if (usesFoodState()) {
      setRightAuxData(2);
    }
  }
  
  public boolean isGrilled()
  {
    if (usesFoodState()) {
      return getRightAuxData() == 2;
    }
    return false;
  }
  
  public void setIsBoiled()
  {
    if (usesFoodState()) {
      setRightAuxData(3);
    }
  }
  
  public boolean isBoiled()
  {
    if (usesFoodState()) {
      return getRightAuxData() == 3;
    }
    return false;
  }
  
  public void setIsRoasted()
  {
    if (usesFoodState()) {
      setRightAuxData(4);
    }
  }
  
  public boolean isRoasted()
  {
    if (usesFoodState()) {
      return getRightAuxData() == 4;
    }
    return false;
  }
  
  public void setIsSteamed()
  {
    if (usesFoodState()) {
      setRightAuxData(5);
    }
  }
  
  public boolean isSteamed()
  {
    if (usesFoodState()) {
      return getRightAuxData() == 5;
    }
    return false;
  }
  
  public void setIsBaked()
  {
    if (usesFoodState()) {
      setRightAuxData(6);
    }
  }
  
  public boolean isBaked()
  {
    if (usesFoodState()) {
      return getRightAuxData() == 6;
    }
    return false;
  }
  
  public void setIsCooked()
  {
    if (usesFoodState()) {
      setRightAuxData(7);
    }
  }
  
  public boolean isCooked()
  {
    if (usesFoodState()) {
      return getRightAuxData() == 7;
    }
    return false;
  }
  
  public void setIsCandied()
  {
    if (usesFoodState()) {
      setRightAuxData(8);
    }
  }
  
  public boolean isCandied()
  {
    if (usesFoodState()) {
      return getRightAuxData() == 8;
    }
    return false;
  }
  
  public void setIsChocolateCoated()
  {
    if (usesFoodState()) {
      setRightAuxData(9);
    }
  }
  
  public boolean isChocolateCoated()
  {
    if (usesFoodState()) {
      return getRightAuxData() == 9;
    }
    return false;
  }
  
  private boolean isChoppedBitSet()
  {
    return (getAuxData() & 0x10) != 0;
  }
  
  private void setIsChoppedBit(boolean setBit)
  {
    setAuxData((byte)((getAuxData() & 0xFFFFFFEF) + (setBit ? 16 : 0)));
  }
  
  private boolean isMashedBitSet()
  {
    return (getAuxData() & 0x20) != 0;
  }
  
  private void setIsMashedBit(boolean setBit)
  {
    setAuxData((byte)((getAuxData() & 0xFFFFFFDF) + (setBit ? 32 : 0)));
  }
  
  private boolean isWrappedBitSet()
  {
    return (getAuxData() & 0x40) != 0;
  }
  
  private void setIsWrappedBit(boolean setBit)
  {
    setAuxData((byte)((getAuxData() & 0xFFFFFFBF) + (setBit ? 64 : 0)));
  }
  
  private boolean isFreshBitSet()
  {
    return (getAuxData() & 0xFFFFFF80) != 0;
  }
  
  private void setIsFreshBit(boolean setBit)
  {
    setAuxData((byte)((getAuxData() & 0x7F) + (setBit ? Byte.MIN_VALUE : 0)));
  }
  
  public void setIsChopped(boolean isChopped)
  {
    if (usesFoodState()) {
      setIsChoppedBit(isChopped);
    }
  }
  
  public boolean isChopped()
  {
    if (usesFoodState()) {
      return isChoppedBitSet();
    }
    return false;
  }
  
  public void setIsDiced(boolean isDiced)
  {
    if (usesFoodState()) {
      setIsChoppedBit(isDiced);
    }
  }
  
  public boolean isDiced()
  {
    if (usesFoodState()) {
      return isChoppedBitSet();
    }
    return false;
  }
  
  public void setIsGround(boolean isGround)
  {
    if (usesFoodState()) {
      setIsChoppedBit(isGround);
    }
  }
  
  public boolean isGround()
  {
    if (usesFoodState()) {
      return isChoppedBitSet();
    }
    return false;
  }
  
  public void setIsUnfermented(boolean isUnfermented)
  {
    if (usesFoodState()) {
      setIsChoppedBit(isUnfermented);
    }
  }
  
  public boolean isUnfermented()
  {
    if (usesFoodState()) {
      return isChoppedBitSet();
    }
    return false;
  }
  
  public void setIsZombiefied(boolean isZombiefied)
  {
    if (usesFoodState()) {
      setIsChoppedBit(isZombiefied);
    }
  }
  
  public boolean isZombiefied()
  {
    if (usesFoodState()) {
      return isChoppedBitSet();
    }
    return false;
  }
  
  public void setIsWhipped(boolean isWhipped)
  {
    if ((usesFoodState()) && (getTemplateId() == 1249)) {
      setIsChoppedBit(isWhipped);
    }
  }
  
  public boolean isWhipped()
  {
    if ((usesFoodState()) && (getTemplateId() == 1249)) {
      return isChoppedBitSet();
    }
    return false;
  }
  
  public void setIsMashed(boolean isMashed)
  {
    if ((usesFoodState()) && (isVegetable())) {
      setIsMashedBit(isMashed);
    }
  }
  
  public boolean isMashed()
  {
    if ((usesFoodState()) && (isVegetable())) {
      return isMashedBitSet();
    }
    return false;
  }
  
  public void setIsMinced(boolean isMinced)
  {
    if ((usesFoodState()) && (isMeat())) {
      setIsMashedBit(isMinced);
    }
  }
  
  public boolean isMinced()
  {
    if ((usesFoodState()) && (isMeat())) {
      return isMashedBitSet();
    }
    return false;
  }
  
  public void setIsFermenting(boolean isFermenting)
  {
    if ((usesFoodState()) && (canBeFermented())) {
      setIsMashedBit(isFermenting());
    }
  }
  
  public boolean isFermenting()
  {
    if ((usesFoodState()) && (canBeFermented())) {
      return isMashedBitSet();
    }
    return false;
  }
  
  public void setIsUnderWeight(boolean isUnderWeight)
  {
    if ((usesFoodState()) && (isFish())) {
      setIsMashedBit(isUnderWeight);
    }
  }
  
  public boolean isUnderWeight()
  {
    if ((usesFoodState()) && (isFish())) {
      return isMashedBitSet();
    }
    return false;
  }
  
  public void setIsClotted(boolean isClotted)
  {
    if ((usesFoodState()) && (getTemplateId() == 1249)) {
      setIsMashedBit(isClotted);
    }
  }
  
  public boolean isClotted()
  {
    if ((usesFoodState()) && (getTemplateId() == 1249)) {
      return isMashedBitSet();
    }
    return false;
  }
  
  public void setIsWrapped(boolean isWrapped)
  {
    if (usesFoodState()) {
      setIsWrappedBit(isWrapped);
    }
  }
  
  public boolean isWrapped()
  {
    if (usesFoodState()) {
      return isWrappedBitSet();
    }
    return false;
  }
  
  public void setIsUndistilled(boolean isUndistilled)
  {
    if (usesFoodState()) {
      setIsWrappedBit(isUndistilled);
    }
  }
  
  public boolean isUndistilled()
  {
    if (usesFoodState()) {
      return isWrappedBitSet();
    }
    return false;
  }
  
  public void setIsFresh(boolean isFresh)
  {
    if ((isHerb()) || (isSpice())) {
      setIsFreshBit(isFresh);
    }
  }
  
  public boolean isFresh()
  {
    if ((isHerb()) || (isSpice())) {
      return isFreshBitSet();
    }
    return false;
  }
  
  public void setIsSalted(boolean isSalted)
  {
    if ((usesFoodState()) && ((isDish()) || (isLiquid())))
    {
      setIsFreshBit(isSalted);
      updateName();
    }
  }
  
  public boolean isSalted()
  {
    if ((usesFoodState()) && ((isDish()) || (isLiquid()))) {
      return isFreshBitSet();
    }
    return false;
  }
  
  public void setIsLive(boolean isLive)
  {
    if (isFish()) {
      setIsFreshBit(isLive);
    }
  }
  
  public boolean isLive()
  {
    if (isFish()) {
      return isFreshBitSet();
    }
    return false;
  }
  
  public boolean isCorrectFoodState(byte cookState, byte physicalState)
  {
    if (cookState != -1) {
      if ((cookState & 0xF) == 7)
      {
        if (getRightAuxData() == 0) {
          return false;
        }
      }
      else if (getRightAuxData() != (cookState & 0xF)) {
        return false;
      }
    }
    if (physicalState != -1)
    {
      if ((physicalState & 0x7F) == 0) {
        return (getLeftAuxData() & 0x7) == (physicalState >>> 4 & 0x7);
      }
      return getLeftAuxData() == (physicalState >>> 4 & 0xF);
    }
    return true;
  }
  
  public byte getFoodStages()
  {
    ItemMealData imd = ItemMealData.getItemMealData(getWurmId());
    if (imd != null) {
      return imd.getStages();
    }
    return 0;
  }
  
  public byte getFoodIngredients()
  {
    ItemMealData imd = ItemMealData.getItemMealData(getWurmId());
    if (imd != null) {
      return imd.getIngredients();
    }
    return 0;
  }
  
  public float getFoodComplexity()
  {
    float stageDif = getFoodStages() / 10.0F;
    float ingredientDif = getFoodIngredients() / 30.0F;
    float totalDif = stageDif * ingredientDif * 2.0F;
    return totalDif;
  }
  
  float calcFoodPercentage()
  {
    float rarityMod = 1.0F + getRarity() * getRarity() * 0.1F;
    float percentage = getCurrentQualityLevel() / 100.0F * rarityMod;
    return percentage;
  }
  
  public float getCaloriesByWeight()
  {
    return getCaloriesByWeight(getWeightGrams());
  }
  
  public float getCaloriesByWeight(int weight)
  {
    return getCalories() * weight / 1000.0F;
  }
  
  public short getCalories()
  {
    if ((getTemplateId() == 488) && (getRealTemplateId() == 488)) {
      return 0;
    }
    float percentage = calcFoodPercentage();
    if (!this.template.calcNutritionValues()) {
      return (short)(int)(this.template.getCalories() * percentage);
    }
    ItemMealData imd = ItemMealData.getItemMealData(getWurmId());
    if (imd != null) {
      return (short)(int)(imd.getCalories() * percentage);
    }
    return (short)(int)(this.template.getCalories() * percentage);
  }
  
  public float getCarbsByWeight()
  {
    return getCarbsByWeight(getWeightGrams());
  }
  
  public float getCarbsByWeight(int weight)
  {
    return getCarbs() * weight / 1000.0F;
  }
  
  public short getCarbs()
  {
    if ((getTemplateId() == 488) && (getRealTemplateId() == 488)) {
      return 0;
    }
    float percentage = calcFoodPercentage();
    if (!this.template.calcNutritionValues()) {
      return (short)(int)(this.template.getCarbs() * percentage);
    }
    if (!this.template.calcNutritionValues()) {
      return this.template.getCarbs();
    }
    ItemMealData imd = ItemMealData.getItemMealData(getWurmId());
    if (imd != null) {
      return (short)(int)(imd.getCarbs() * percentage);
    }
    return (short)(int)(this.template.getCarbs() * percentage);
  }
  
  public float getFatsByWeight()
  {
    return getFatsByWeight(getWeightGrams());
  }
  
  public float getFatsByWeight(int weight)
  {
    return getFats() * weight / 1000.0F;
  }
  
  public short getFats()
  {
    if ((getTemplateId() == 488) && (getRealTemplateId() == 488)) {
      return 0;
    }
    float percentage = calcFoodPercentage();
    if (!this.template.calcNutritionValues()) {
      return (short)(int)(this.template.getFats() * percentage);
    }
    ItemMealData imd = ItemMealData.getItemMealData(getWurmId());
    if (imd != null) {
      return (short)(int)(imd.getFats() * percentage);
    }
    return (short)(int)(this.template.getFats() * percentage);
  }
  
  public float getProteinsByWeight()
  {
    return getProteinsByWeight(getWeightGrams());
  }
  
  public float getProteinsByWeight(int weight)
  {
    return getProteins() * weight / 1000.0F;
  }
  
  public short getProteins()
  {
    if ((getTemplateId() == 488) && (getRealTemplateId() == 488)) {
      return 0;
    }
    float percentage = calcFoodPercentage();
    if (!this.template.calcNutritionValues()) {
      return (short)(int)(this.template.getProteins() * percentage);
    }
    ItemMealData imd = ItemMealData.getItemMealData(getWurmId());
    if (imd != null) {
      return (short)(int)(imd.getProteins() * percentage);
    }
    return (short)(int)(this.template.getProteins() * percentage);
  }
  
  public int getBonus()
  {
    if ((getTemplateId() == 488) && (getRealTemplateId() == 488)) {
      return -1;
    }
    ItemMealData imd = ItemMealData.getItemMealData(getWurmId());
    if (imd != null) {
      return imd.getBonus() & 0xFF;
    }
    return -1;
  }
  
  @Nullable
  public Recipe getRecipe()
  {
    if ((getTemplateId() == 488) && (getRealTemplateId() == 488)) {
      return null;
    }
    ItemMealData imd = ItemMealData.getItemMealData(getWurmId());
    if ((imd != null) && (imd.getRecipeId() > -1)) {
      return Recipes.getRecipeById(imd.getRecipeId());
    }
    return null;
  }
  
  public void calculateAndSaveNutrition(@Nullable Item source, Item target, Recipe recipe)
  {
    byte stages = 1;
    byte ingredients = 0;
    if ((source != null) && (!source.isCookingTool()))
    {
      stages = (byte)(stages + source.getFoodStages());
      ingredients = (byte)(ingredients + (source.getFoodIngredients() + 1));
    }
    if (target.isFoodMaker())
    {
      Map<Integer, Item> items = new ConcurrentHashMap();
      for (Item item : target.getItemsAsArray()) {
        items.put(Integer.valueOf(item.getTemplateId()), item);
      }
      for (??? = items.values().iterator(); ((Iterator)???).hasNext();)
      {
        Item item = (Item)((Iterator)???).next();
        
        stages = (byte)(stages + item.getFoodStages());
        ingredients = (byte)(ingredients + (item.getFoodIngredients() + 1));
      }
    }
    else
    {
      stages = (byte)(stages + target.getFoodStages());
      ingredients = (byte)(ingredients + (target.getFoodIngredients() + 1));
    }
    short calories = this.template.getCalories();
    short carbs = this.template.getCarbs();
    short fats = this.template.getFats();
    short proteins = this.template.getProteins();
    float fatsTotal;
    float proteinsTotal;
    if (this.template.calcNutritionValues())
    {
      float caloriesTotal = 0.0F;
      float carbsTotal = 0.0F;
      fatsTotal = 0.0F;
      proteinsTotal = 0.0F;
      int weight = 0;
      Ingredient ingredient;
      int iweight;
      if ((source != null) && (!source.isCookingTool()))
      {
        ingredient = recipe.getActiveItem();
        
        iweight = ingredient == null ? source.getWeightGrams() : (int)(source.getWeightGrams() * ((100.0F - ingredient.getLoss()) / 100.0F));
        caloriesTotal += source.getCaloriesByWeight(iweight);
        carbsTotal += source.getCarbsByWeight(iweight);
        fatsTotal += source.getFatsByWeight(iweight);
        proteinsTotal += source.getProteinsByWeight(iweight);
        weight += iweight;
      }
      if (target.isFoodMaker())
      {
        for (Item item : target.getItemsAsArray())
        {
          Ingredient ingredient = recipe.findMatchingIngredient(item);
          
          int iweight = ingredient == null ? item.getWeightGrams() : (int)(item.getWeightGrams() * ((100.0F - ingredient.getLoss()) / 100.0F));
          caloriesTotal += item.getCaloriesByWeight(iweight);
          carbsTotal += item.getCarbsByWeight(iweight);
          fatsTotal += item.getFatsByWeight(iweight);
          proteinsTotal += item.getProteinsByWeight(iweight);
          weight += iweight;
        }
      }
      else
      {
        Ingredient ingredient = recipe.getTargetItem();
        
        int iweight = ingredient == null ? target.getWeightGrams() : (int)(target.getWeightGrams() * ((100.0F - ingredient.getLoss()) / 100.0F));
        caloriesTotal += target.getCaloriesByWeight(iweight);
        carbsTotal += target.getCarbsByWeight(iweight);
        fatsTotal += target.getFatsByWeight(iweight);
        proteinsTotal += target.getProteinsByWeight(iweight);
        weight += iweight;
      }
      float rarityMod = 1.0F + getRarity() * getRarity() * 0.1F;
      calories = (short)(int)(caloriesTotal * 1000.0F / weight * rarityMod);
      carbs = (short)(int)(carbsTotal * 1000.0F / weight * rarityMod);
      fats = (short)(int)(fatsTotal * 1000.0F / weight * rarityMod);
      proteins = (short)(int)(proteinsTotal * 1000.0F / weight * rarityMod);
    }
    int ibonus = 0;
    if ((source != null) && (recipe.hasActiveItem()) && (recipe.getActiveItem().getTemplateId() != 14))
    {
      ibonus += source.getTemplateId();
      if (!Server.getInstance().isPS()) {
        ibonus += source.getRarity();
      }
    }
    Item cooker;
    if (recipe.hasCooker())
    {
      cooker = target.getTopParentOrNull();
      if (cooker != null)
      {
        ibonus += cooker.getTemplateId();
        if (!Server.getInstance().isPS()) {
          ibonus += cooker.getRarity();
        }
      }
    }
    ibonus += target.getTemplateId();
    if (target.isFoodMaker())
    {
      cooker = target.getItemsAsArray();fatsTotal = cooker.length;
      for (proteinsTotal = 0; proteinsTotal < fatsTotal; proteinsTotal++)
      {
        Item item = cooker[proteinsTotal];
        
        ibonus += item.getTemplateId();
        if (item.usesFoodState()) {
          ibonus += item.getAuxData();
        }
        ibonus += item.getMaterial();
        ibonus += item.getRealTemplateId();
        if (!Server.getInstance().isPS()) {
          ibonus += item.getRarity();
        }
      }
    }
    else
    {
      if (target.usesFoodState()) {
        ibonus += target.getAuxData();
      }
      ibonus += target.getMaterial();
      ibonus += target.getRealTemplateId();
      if (getTemplateId() == 272) {
        ibonus += target.getData1();
      }
      if (!Server.getInstance().isPS()) {
        ibonus += target.getRarity();
      }
    }
    byte bonus = (byte)(ibonus % SkillSystem.getNumberOfSkillTemplates());
    ItemMealData.save(getWurmId(), recipe.getRecipeId(), calories, carbs, fats, proteins, bonus, stages, ingredients);
  }
  
  public void setHarvestable(boolean harvestable)
  {
    if (this.template.isHarvestable())
    {
      if ((harvestable) && (isPlanted())) {
        setRightAuxData(1);
      } else {
        setRightAuxData(0);
      }
      updateModelNameOnGroundItem();
    }
  }
  
  public boolean isHarvestable()
  {
    return (this.template.isHarvestable()) && (getRightAuxData() == 1) && (isPlanted()) && 
      (getLeftAuxData() > FoliageAge.YOUNG_FOUR.getAgeId()) && (getLeftAuxData() < FoliageAge.OVERAGED.getAgeId());
  }
  
  public Item getHoney()
  {
    Item[] items = getItemsAsArray();
    for (Item item : items) {
      if (item.getTemplateId() == 70) {
        return item;
      }
    }
    return null;
  }
  
  public Item getSugar()
  {
    Item[] items = getItemsAsArray();
    for (Item item : items) {
      if (item.getTemplateId() == 1139) {
        return item;
      }
    }
    return null;
  }
  
  public int getWaxCount()
  {
    int waxCount = 0;
    Item[] items = getItemsAsArray();
    for (Item item : items) {
      if (item.getTemplateId() == 1254) {
        waxCount++;
      }
    }
    return waxCount;
  }
  
  public Item getVinegar()
  {
    Item[] items = getItemsAsArray();
    for (Item item : items) {
      if (item.getTemplateId() == 1246) {
        return item;
      }
    }
    return null;
  }
  
  abstract boolean saveInscription();
  
  public boolean setInscription(Recipe recipe, String theInscriber, int thePenColour)
  {
    this.inscription = new InscriptionData(getWurmId(), recipe, theInscriber, thePenColour);
    setAuxData((byte)1);
    setName("\"" + recipe.getName() + "\"", true);
    setRarity(recipe.getLootableRarity());
    return saveInscription();
  }
  
  public final boolean isMoonMetal()
  {
    switch (getMaterial())
    {
    case 56: 
    case 57: 
    case 67: 
      return true;
    }
    return false;
  }
  
  public final boolean isAlloyMetal()
  {
    switch (getMaterial())
    {
    case 9: 
    case 30: 
    case 31: 
    case 96: 
      return true;
    }
    return false;
  }
  
  public final String debugLog(int depth)
  {
    StringBuilder buf = new StringBuilder();
    buf.append(toString()).append("\n");
    buf.append("Last Owner: ").append(this.lastOwner).append(" | Owner: ").append(this.ownerId)
      .append(" | Parent: ").append(this.parentId).append("\n");
    buf.append("Material: ").append(getMaterialString(getMaterial())).append(" | Rarity: ").append(getRarity())
      .append(" | Description: ").append(getDescription()).append("\n");
    try
    {
      StackTraceElement[] steArr = new Throwable().getStackTrace();
      for (int i = 1; i < steArr.length; i++)
      {
        if (i >= depth) {
          break;
        }
        buf.append(steArr[i].getClassName()).append(".").append(steArr[i].getMethodName()).append(":").append(steArr[i].getLineNumber()).append("\n");
      }
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
    return buf.toString();
  }
  
  public final boolean isSorceryItem()
  {
    switch (getTemplateId())
    {
    case 794: 
    case 795: 
    case 796: 
    case 797: 
    case 798: 
    case 799: 
    case 800: 
    case 801: 
    case 802: 
    case 803: 
    case 804: 
    case 805: 
    case 806: 
    case 807: 
    case 808: 
    case 809: 
    case 810: 
      return true;
    }
    return false;
  }
  
  public final boolean isEnchanted()
  {
    return (getSpellEffects() != null) && (getSpellEffects().getEffects().length > 0);
  }
  
  public final boolean isDebugLogged()
  {
    return (this.rarity > 0) || (isMoonMetal()) || (isEnchanted()) || (isSorceryItem()) || 
      ((!getDescription().isEmpty()) && (!isBulkItem()) && (!isCorpse()) && (!isTemporary())) || (
      (!isBodyPart()) && (!isFood()) && (!isMushroom()) && (!isTypeRecycled()) && (!isCorpse()) && (!isInventory()) && 
      (!isBulk()) && (!isBulkItem()) && (getTemplateId() != 177) && (getLastOwnerId() > 0L) && 
      (getTemplateId() != 314) && (getTemplateId() != 1392));
  }
  
  public void setLightOverride(boolean lightOverride)
  {
    this.isLightOverride = lightOverride;
  }
  
  public boolean isLightOverride()
  {
    return this.isLightOverride;
  }
  
  public Item getOuterItemOrNull()
  {
    if (!getTemplate().isComponentItem()) {
      return this;
    }
    if (getParentOrNull() == null) {
      return null;
    }
    return getParentOrNull().getOuterItemOrNull();
  }
  
  public Item getParentOuterItemOrNull()
  {
    Item parent = getParentOrNull();
    if (parent != null) {
      return parent.getOuterItemOrNull();
    }
    return null;
  }
  
  public int getMaxItemCount()
  {
    if (getTemplateId() == 1295)
    {
      Item outer = getOuterItemOrNull();
      if (outer != null) {
        return outer.getMaxItemCount();
      }
    }
    return getTemplate().getMaxItemCount();
  }
  
  public int getMaxItemWeight()
  {
    if (getTemplateId() == 1295)
    {
      Item outer = getOuterItemOrNull();
      if (outer != null) {
        return outer.getMaxItemWeight();
      }
    }
    return getTemplate().getMaxItemWeight();
  }
  
  public boolean canHold(Item target)
  {
    if ((getMaxItemCount() > -1) && (getItemCount() >= getMaxItemCount())) {
      return false;
    }
    if ((getMaxItemWeight() > -1) && 
      (getFullWeight() - getWeightGrams() + target.getFullWeight() > getMaxItemWeight())) {
      return false;
    }
    return true;
  }
  
  public boolean isInsulated()
  {
    Item parent = getParentOrNull();
    return (parent != null) && (parent.getTemplate().isInsulated());
  }
  
  public boolean isInLunchbox()
  {
    if ((!canLarder()) && (!isLiquid())) {
      return false;
    }
    Item lunchbox = getParentOuterItemOrNull();
    return (lunchbox != null) && (
      (lunchbox.getTemplateId() == 1296) || 
      (lunchbox.getTemplateId() == 1297));
  }
  
  public boolean isInTacklebox()
  {
    Item parent = getParentOrNull();
    if ((parent == null) || (parent.getTemplateId() == 1341)) {
      return false;
    }
    Item container = getParentOuterItemOrNull();
    return (container != null) && 
      (container.getTemplateId() == 1341);
  }
  
  public short getWinches()
  {
    return this.warmachineWinches;
  }
  
  public void setWinches(short newWinches)
  {
    this.warmachineWinches = newWinches;
  }
  
  public void setReplacing(boolean replacing)
  {
    this.replacing = replacing;
  }
  
  public boolean isReplacing()
  {
    return this.replacing;
  }
  
  public void setIsSealedOverride(boolean overrideSealed)
  {
    this.isSealedOverride = overrideSealed;
    closeAll();
    updateName();
  }
  
  public boolean isSealedOverride()
  {
    return this.isSealedOverride;
  }
  
  public void setWhatHappened(String destroyReason)
  {
    this.whatHappened = destroyReason;
  }
  
  public String getWhatHappened()
  {
    return this.whatHappened;
  }
  
  public void setWasBrandedTo(long wasBrandedTo)
  {
    this.wasBrandedTo = wasBrandedTo;
  }
  
  public long getWasBrandedTo()
  {
    return this.wasBrandedTo;
  }
  
  public boolean isSaddleBags()
  {
    return getTemplate().isSaddleBags();
  }
  
  public boolean isFishingReel()
  {
    return getTemplate().isFishingReel();
  }
  
  public boolean isFishingLine()
  {
    return getTemplate().isFishingLine();
  }
  
  public boolean isFishingFloat()
  {
    return getTemplate().isFishingFloat();
  }
  
  public boolean isFishingHook()
  {
    return getTemplate().isFishingHook();
  }
  
  public boolean isFishingBait()
  {
    return getTemplate().isFishingBait();
  }
  
  public Item[] getFishingItems()
  {
    Item fishingReel = null;
    Item fishingLine = null;
    Item fishingFloat = null;
    Item fishingHook = null;
    Item fishingBait = null;
    if ((getTemplateId() == 1344) || (getTemplateId() == 1346))
    {
      if (getTemplateId() == 1344)
      {
        fishingLine = getFishingLine();
      }
      else
      {
        fishingReel = getFishingReel();
        if (fishingReel != null) {
          fishingLine = fishingReel.getFishingLine();
        }
      }
      if (fishingLine != null)
      {
        fishingFloat = fishingLine.getFishingFloat();
        fishingHook = fishingLine.getFishingHook();
      }
      if (fishingHook != null) {
        fishingBait = fishingHook.getFishingBait();
      }
    }
    return new Item[] { fishingReel, fishingLine, fishingFloat, fishingHook, fishingBait };
  }
  
  @Nullable
  public Item getFishingReel()
  {
    Item[] containsItems = getItemsAsArray();
    if (containsItems.length > 0) {
      for (Item contains : containsItems) {
        if (contains.isFishingReel()) {
          return contains;
        }
      }
    }
    return null;
  }
  
  @Nullable
  public Item getFishingLine()
  {
    Item[] containsItems = getItemsAsArray();
    if (containsItems.length > 0) {
      for (Item contains : containsItems) {
        if (contains.isFishingLine()) {
          return contains;
        }
      }
    }
    return null;
  }
  
  public String getFishingLineName()
  {
    String lineName = "";
    switch (getTemplateId())
    {
    case 1372: 
      lineName = "light fishing line";
      break;
    case 1373: 
      lineName = "medium fishing line";
      break;
    case 1374: 
      lineName = "heavy fishing line";
      break;
    case 1375: 
      lineName = "braided fishing line";
    }
    return lineName;
  }
  
  @Nullable
  public Item getFishingHook()
  {
    Item[] containsItems = getItemsAsArray();
    if (containsItems.length > 0) {
      for (Item contains : containsItems) {
        if (contains.isFishingHook()) {
          return contains;
        }
      }
    }
    return null;
  }
  
  @Nullable
  public Item getFishingFloat()
  {
    Item[] containsItems = getItemsAsArray();
    if (containsItems.length > 0) {
      for (Item contains : containsItems) {
        if (contains.isFishingFloat()) {
          return contains;
        }
      }
    }
    return null;
  }
  
  @Nullable
  public Item getFishingBait()
  {
    Item[] containsItems = getItemsAsArray();
    if (containsItems.length > 0) {
      for (Item contains : containsItems) {
        if (contains.isFishingBait()) {
          return contains;
        }
      }
    }
    return null;
  }
  
  public boolean isFlyTrap()
  {
    if (getTemplateId() != 76) {
      return false;
    }
    Item[] items = getItemsAsArray();
    if (items.length >= 1)
    {
      boolean hasHoney = false;
      boolean hasVinegar = false;
      int count = 0;
      boolean contaminated = false;
      for (Item contained : items) {
        if (contained.getTemplateId() == 70) {
          hasHoney = true;
        } else if (contained.getTemplateId() == 1246) {
          hasVinegar = true;
        } else if (contained.getTemplateId() == 1359) {
          count++;
        } else {
          contaminated = true;
        }
      }
      if ((hasHoney) && (hasVinegar)) {
        contaminated = true;
      }
      return ((hasHoney) || (hasVinegar)) && (!contaminated) && (count < 99);
    }
    return false;
  }
  
  public boolean isInside(int... itemTemplateIds)
  {
    Item parent = getParentOrNull();
    if (parent != null)
    {
      for (int itemTemplateId : itemTemplateIds) {
        if (parent.getTemplateId() == itemTemplateId) {
          return true;
        }
      }
      return parent.isInside(itemTemplateIds);
    }
    return false;
  }
  
  public Item getFirstParent(int... itemTemplateIds)
  {
    Item parent = getParentOrNull();
    if (parent != null)
    {
      for (int itemTemplateId : itemTemplateIds) {
        if (parent.getTemplateId() == itemTemplateId) {
          return parent;
        }
      }
      return parent.getFirstParent(itemTemplateIds);
    }
    return null;
  }
  
  public boolean isInsidePlacedContainer()
  {
    if (getParentOrNull() != null)
    {
      if (getParentOrNull().isPlacedOnParent()) {
        return true;
      }
      return getParentOrNull().isInsidePlacedContainer();
    }
    return false;
  }
  
  public boolean isInsidePlaceableContainer()
  {
    if (getParentOrNull() != null) {
      if ((getParentOrNull().getTemplate().hasViewableSubItems()) && 
        ((!getParentOrNull().getTemplate().isContainerWithSubItems()) || (isPlacedOnParent())) && 
        (getTopParent() == getParentId())) {
        return true;
      }
    }
    return false;
  }
  
  public boolean isProcessedFood()
  {
    return (getName().contains("mashed ")) || (getName().contains("chopped "));
  }
  
  public boolean doesShowSlopes()
  {
    return getTemplate().doesShowSlopes();
  }
  
  public boolean supportsSecondryColor()
  {
    return getTemplate().supportsSecondryColor();
  }
  
  public float getMaterialImpBonus()
  {
    if (Features.Feature.METALLIC_ITEMS.isEnabled()) {
      switch (getMaterial())
      {
      case 30: 
        return 1.025F;
      case 10: 
        return 1.05F;
      case 12: 
        return 1.1F;
      case 34: 
        return 1.025F;
      case 13: 
        return 1.075F;
      }
    }
    return 1.0F;
  }
  
  public static float getMaterialCreationBonus(byte material)
  {
    if (Features.Feature.METALLIC_ITEMS.isEnabled()) {
      switch (material)
      {
      case 30: 
        return 0.1F;
      case 31: 
        return 0.05F;
      case 12: 
        return 0.05F;
      case 34: 
        return 0.05F;
      }
    }
    return 0.0F;
  }
  
  public static float getMaterialLockpickBonus(byte material)
  {
    if (Features.Feature.METALLIC_ITEMS.isEnabled())
    {
      switch (material)
      {
      case 56: 
        return 0.05F;
      case 10: 
        return -0.05F;
      case 57: 
        return 0.05F;
      case 7: 
        return -0.025F;
      case 12: 
        return -0.05F;
      case 67: 
        return 0.05F;
      case 8: 
        return 0.025F;
      case 9: 
        return 0.05F;
      case 34: 
        return -0.025F;
      case 13: 
        return -0.025F;
      }
    }
    else
    {
      if (material == 10) {
        return -0.05F;
      }
      if (material == 9) {
        return 0.05F;
      }
    }
    return 0.0F;
  }
  
  public static float getMaterialAnchorBonus(byte material)
  {
    if (Features.Feature.METALLIC_ITEMS.isEnabled()) {
      switch (material)
      {
      case 56: 
        return 1.5F;
      case 30: 
        return 0.9F;
      case 31: 
        return 0.85F;
      case 10: 
        return 0.95F;
      case 57: 
        return 1.25F;
      case 7: 
        return 1.7F;
      case 11: 
        return 0.85F;
      case 12: 
        return 1.0F;
      case 67: 
        return 1.25F;
      case 8: 
        return 0.975F;
      case 9: 
        return 0.85F;
      case 34: 
        return 0.8F;
      case 13: 
        return 0.75F;
      case 96: 
        return 1.0F;
      }
    }
    return 1.0F;
  }
  
  public int getMaxPlaceableItems()
  {
    return (getContainerSizeY() + getContainerSizeZ()) / 10;
  }
  
  public boolean getAuxBit(int bitLoc)
  {
    return (getAuxData() >> bitLoc & 0x1) == 1;
  }
  
  public void setAuxBit(int bitLoc, boolean value)
  {
    if (!value) {
      setAuxData((byte)(getAuxData() & (1 << bitLoc ^ 0xFFFFFFFF)));
    } else {
      setAuxData((byte)(getAuxData() | 1 << bitLoc));
    }
  }
  
  public boolean isPlacedOnParent()
  {
    return this.placedOnParent;
  }
  
  public abstract void setPlacedOnParent(boolean paramBoolean);
  
  @Nullable
  public Item recursiveParentCheck()
  {
    Item parent = getParentOrNull();
    if (parent != null)
    {
      if ((parent.getTemplate().hasViewableSubItems()) && (
        (!parent.getTemplate().isContainerWithSubItems()) || (isPlacedOnParent()))) {
        return this;
      }
      return parent.recursiveParentCheck();
    }
    return null;
  }
  
  public void setChained(boolean chained)
  {
    this.isChained = chained;
  }
  
  public boolean isChained()
  {
    return this.isChained;
  }
  
  public void addSnowmanItem()
  {
    if (getTemplateId() != 1437) {
      return;
    }
    if (!isEmpty(true)) {
      return;
    }
    int snowId = 1276;
    float rand = Server.rand.nextFloat() * 100.0F;
    if (rand < 1.0E-4F) {
      snowId = 381;
    } else if (rand < 0.001F) {
      snowId = 380;
    } else if (rand < 0.01F) {
      snowId = 1397;
    } else if (rand < 0.02F) {
      snowId = 205;
    }
    try
    {
      Item toPlace = ItemFactory.createItem(snowId, Server.rand.nextFloat() * 50.0F + 50.0F, (byte)0, null);
      toPlace.setPos(0.0F, 0.0F, 0.8F, Server.rand.nextFloat() * 360.0F, onBridge());
      toPlace.setLastOwnerId(getWurmId());
      if (insertItem(toPlace, false, false, true))
      {
        VolaTile vt = Zones.getTileOrNull(getTileX(), getTileY(), isOnSurface());
        if (vt != null) {
          for (VirtualZone vz : vt.getWatchers()) {
            if (vz.isVisible(this, vt))
            {
              vz.getWatcher().getCommunicator().sendItem(toPlace, -10L, false);
              if ((toPlace.isLight()) && (toPlace.isOnFire())) {
                vt.addLightSource(toPlace);
              }
              if (toPlace.getEffects().length > 0) {
                for (Effect e : toPlace.getEffects()) {
                  vz.addEffect(e, false);
                }
              }
              if (toPlace.getColor() != -1) {
                vz.sendRepaint(toPlace.getWurmId(), (byte)WurmColor.getColorRed(toPlace.getColor()), 
                  (byte)WurmColor.getColorGreen(toPlace.getColor()), (byte)WurmColor.getColorBlue(toPlace.getColor()), (byte)-1, (byte)0);
              }
              if (toPlace.getColor2() != -1) {
                vz.sendRepaint(toPlace.getWurmId(), (byte)WurmColor.getColorRed(toPlace.getColor2()), 
                  (byte)WurmColor.getColorGreen(toPlace.getColor2()), (byte)WurmColor.getColorBlue(toPlace.getColor2()), (byte)-1, (byte)1);
              }
            }
          }
        }
      }
      else
      {
        Items.destroyItem(toPlace.getWurmId());
      }
    }
    catch (FailedException|NoSuchTemplateException localFailedException) {}
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\items\Item.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */