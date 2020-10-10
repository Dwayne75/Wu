package com.wurmonline.server.zones;

import com.wurmonline.math.Vector2f;
import com.wurmonline.math.Vector3f;
import com.wurmonline.mesh.Tiles;
import com.wurmonline.mesh.Tiles.Tile;
import com.wurmonline.server.Constants;
import com.wurmonline.server.Features.Feature;
import com.wurmonline.server.Items;
import com.wurmonline.server.Message;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Players;
import com.wurmonline.server.Server;
import com.wurmonline.server.ServerEntry;
import com.wurmonline.server.Servers;
import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.WurmId;
import com.wurmonline.server.behaviours.MethodsCreatures;
import com.wurmonline.server.behaviours.Seat;
import com.wurmonline.server.behaviours.Vehicle;
import com.wurmonline.server.behaviours.Vehicles;
import com.wurmonline.server.bodys.Body;
import com.wurmonline.server.bodys.BodyTemplate;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.CreatureMove;
import com.wurmonline.server.creatures.CreatureStatus;
import com.wurmonline.server.creatures.CreatureTemplate;
import com.wurmonline.server.creatures.Creatures;
import com.wurmonline.server.creatures.MineDoorPermission;
import com.wurmonline.server.creatures.MovementScheme;
import com.wurmonline.server.creatures.NoSuchCreatureException;
import com.wurmonline.server.creatures.Npc;
import com.wurmonline.server.creatures.SpellEffects;
import com.wurmonline.server.creatures.VisionArea;
import com.wurmonline.server.creatures.ai.ChatManager;
import com.wurmonline.server.creatures.ai.CreatureAI;
import com.wurmonline.server.deities.Deity;
import com.wurmonline.server.effects.Effect;
import com.wurmonline.server.endgames.EndGameItems;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemTemplate;
import com.wurmonline.server.items.NoSpaceException;
import com.wurmonline.server.items.WurmColor;
import com.wurmonline.server.kingdom.GuardTower;
import com.wurmonline.server.kingdom.King;
import com.wurmonline.server.kingdom.Kingdoms;
import com.wurmonline.server.players.Cultist;
import com.wurmonline.server.players.MovementEntity;
import com.wurmonline.server.players.MusicPlayer;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.players.PlayerInfo;
import com.wurmonline.server.players.PlayerInfoFactory;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.sounds.Sound;
import com.wurmonline.server.structures.BridgePart;
import com.wurmonline.server.structures.Door;
import com.wurmonline.server.structures.Fence;
import com.wurmonline.server.structures.FenceGate;
import com.wurmonline.server.structures.Floor;
import com.wurmonline.server.structures.NoSuchStructureException;
import com.wurmonline.server.structures.Structure;
import com.wurmonline.server.structures.Structures;
import com.wurmonline.server.structures.Wall;
import com.wurmonline.server.villages.Village;
import com.wurmonline.shared.constants.AttitudeConstants;
import com.wurmonline.shared.constants.CounterTypes;
import com.wurmonline.shared.constants.ProtoConstants;
import com.wurmonline.shared.constants.StructureTypeEnum;
import com.wurmonline.shared.util.StringUtilities;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class VirtualZone
  implements MiscConstants, CounterTypes, AttitudeConstants, ProtoConstants, TimeConstants
{
  private final Creature watcher;
  private Zone[] watchedZones;
  private Set<Item> items;
  private Set<Effect> effects;
  private Set<AreaSpellEffect> areaEffects;
  private Set<Long> finalizedBuildings;
  private Set<Door> doors;
  private Set<Fence> fences;
  private Set<MineDoorPermission> mineDoors;
  private int centerx;
  private int centery;
  private int startX = 0;
  private int endX = 0;
  private int startY = 0;
  private int endY = 0;
  private Set<Structure> structures;
  private final Map<Long, CreatureMove> creatures = new HashMap();
  private static final Logger logger = Logger.getLogger(VirtualZone.class.getName());
  private int size;
  private final int id;
  private static int ids = 0;
  private static final int worldTileSizeX = 1 << Constants.meshSize;
  private static final int worldTileSizeY = 1 << Constants.meshSize;
  private final boolean isOnSurface;
  private static final Long[] emptyLongArray = new Long[0];
  private byte MOVELIMIT = 5;
  private ArrayList<Structure> nearbyStructureList = new ArrayList();
  private static final Set<VirtualZone> allZones = new HashSet();
  private static final int surfaceToSurfaceLocalDistance = 80;
  private static final int caveToSurfaceLocalDistance = Servers.localServer.EPIC ? 20 : 80;
  private static final int surfaceToCaveLocalDistance = 20;
  private static final int caveToCaveLocalDistance = 20;
  public static final int ITEM_INSIDE_RENDERDIST = 15;
  public static final int HOUSEITEMS_RENDERDIST = 5;
  
  public VirtualZone(Creature aWatcher, int aStartX, int aStartY, int centerX, int centerY, int aSz, boolean aIsOnSurface)
  {
    this.isOnSurface = aIsOnSurface;
    this.startX = Math.max(0, aStartX);
    this.startY = Math.max(0, aStartY);
    
    this.centerx = Math.max(0, centerX);
    this.centery = Math.max(0, centerY);
    this.centerx = Math.min(worldTileSizeX - 1, centerX);
    this.centery = Math.min(worldTileSizeY - 1, centerY);
    this.endX = Math.min(worldTileSizeX - 1, this.centerx + aSz);
    this.endY = Math.min(worldTileSizeY - 1, this.centery + aSz);
    
    this.id = (ids++);
    this.size = aSz;
    this.watcher = aWatcher;
    allZones.add(this);
  }
  
  public boolean covers(int x, int y)
  {
    return (x >= this.startX) && (x <= this.endX) && (y >= this.startY) && (y <= this.endY);
  }
  
  public Creature getWatcher()
  {
    return this.watcher;
  }
  
  public int getId()
  {
    return this.id;
  }
  
  public boolean isOnSurface()
  {
    return this.isOnSurface;
  }
  
  public int getStartX()
  {
    return this.startX;
  }
  
  public int getStartY()
  {
    return this.startY;
  }
  
  public int getEndX()
  {
    return this.endX;
  }
  
  public int getEndY()
  {
    return this.endY;
  }
  
  public void initialize()
  {
    if (!this.watcher.isDead())
    {
      this.watchedZones = Zones.getZonesCoveredBy(this);
      for (int i = 0; i < this.watchedZones.length; i++) {
        try
        {
          this.watchedZones[i].addWatcher(this.id);
        }
        catch (NoSuchZoneException nze)
        {
          logger.log(Level.INFO, nze.getMessage(), nze);
        }
      }
    }
  }
  
  void addVillage(Village newVillage) {}
  
  void broadCastMessage(Message message)
  {
    if (!this.watcher.isIgnored(message.getSender().getWurmId()))
    {
      if (this.watcher.isNpc()) {
        if (message.getSender().getWurmId() != this.watcher.getWurmId()) {
          ((Npc)this.watcher).getChatManager().addLocalChat(message);
        }
      }
      if (!this.watcher.getCommunicator().isInvulnerable()) {
        this.watcher.getCommunicator().sendMessage(message);
      }
    }
  }
  
  public void callGuards()
  {
    boolean found = false;
    if (this.items != null) {
      for (Item i : this.items) {
        if (i.isKingdomMarker()) {
          if (i.getKingdom() == this.watcher.getKingdomId())
          {
            GuardTower tower = Kingdoms.getTower(i);
            if ((tower != null) && (tower.alertGuards(this.watcher)))
            {
              this.watcher.getCommunicator().sendSafeServerMessage("Guards from " + i.getName() + " runs to the rescue!");
              found = true;
            }
          }
        }
      }
    }
    if (!found) {
      this.watcher.getCommunicator().sendSafeServerMessage("No guards seem to respond to your call.");
    }
  }
  
  public int getCenterX()
  {
    return this.centerx;
  }
  
  public int getCenterY()
  {
    return this.centery;
  }
  
  int getSize()
  {
    return this.size;
  }
  
  public boolean shouldSeeCaves()
  {
    if (this.watcher.isPlayer()) {
      if (this.watcher.isOnSurface()) {
        for (int x = this.startX + 10; x <= this.endX - 10; x++) {
          for (int y = this.startY + 10; y <= this.endY - 10; y++) {
            if (Tiles.decodeType(Server.caveMesh.data[(x | y << Constants.meshSize)]) == Tiles.Tile.TILE_CAVE_EXIT.id) {
              return true;
            }
          }
        }
      } else {
        return true;
      }
    }
    return false;
  }
  
  public void move(int xChange, int yChange)
  {
    this.centerx = Math.max(0, this.centerx + xChange);
    this.centery = Math.max(0, this.centery + yChange);
    this.centerx = Math.min(worldTileSizeX - 1, this.centerx);
    this.centery = Math.min(worldTileSizeY - 1, this.centery);
    this.startX = Math.max(0, this.centerx - this.size);
    this.startY = Math.max(0, this.centery - this.size);
    this.endX = Math.min(worldTileSizeX - 1, this.centerx + this.size);
    this.endY = Math.min(worldTileSizeY - 1, this.centery + this.size);
  }
  
  private final int getSizeX()
  {
    return this.endX - this.startX;
  }
  
  private final int getSizeY()
  {
    return this.endY - this.startY;
  }
  
  public void stopWatching()
  {
    Zone[] checkedZones = Zones.getZonesCoveredBy(Math.max(0, this.startX - 100), Math.max(0, this.startY - 100), 
      Math.min(Zones.worldTileSizeX - 1, this.endX + 100), Math.min(Zones.worldTileSizeY - 1, this.endY + 100), this.isOnSurface);
    for (int x = 0; x < checkedZones.length; x++) {
      try
      {
        checkedZones[x].removeWatcher(this);
      }
      catch (NoSuchZoneException sex)
      {
        logger.log(Level.WARNING, sex.getMessage(), sex);
      }
    }
    this.watchedZones = null;
    pruneDestroy();
    this.size = 0;
    
    allZones.remove(this);
    if (Server.rand.nextInt(1000) == 0)
    {
      int cs = Creatures.getInstance().getNumberOfCreatures();
      int ps = Players.getInstance().getNumberOfPlayers();
      if (allZones.size() > ps * 2 + cs * 2 + 100) {
        logger.log(Level.INFO, "Number of virtual zones now: " + allZones.size() + ". Creatures*2=" + cs * 2 + ", players*2=" + ps * 2);
      }
    }
  }
  
  public Long[] getCreatures()
  {
    if (this.creatures != null) {
      return (Long[])this.creatures.keySet().toArray(new Long[this.creatures.size()]);
    }
    return emptyLongArray;
  }
  
  public boolean containsCreature(Creature creature)
  {
    if (this.creatures != null) {
      return this.creatures.keySet().contains(Long.valueOf(creature.getWurmId()));
    }
    return false;
  }
  
  public void refreshAttitudes()
  {
    for (Long l : this.creatures.keySet()) {
      try
      {
        Creature cret = Creatures.getInstance().getCreature(l.longValue());
        sendAttitude(cret);
      }
      catch (NoSuchCreatureException localNoSuchCreatureException) {}
    }
  }
  
  private void pruneDestroy()
  {
    removeAllStructures();
    this.finalizedBuildings = null;
    Iterator<Door> it;
    if (this.doors != null)
    {
      for (it = this.doors.iterator(); it.hasNext();)
      {
        Door door = (Door)it.next();
        door.removeWatcher(this);
      }
      this.doors = null;
    }
    if (this.creatures != null)
    {
      for (Long l : this.creatures.keySet()) {
        this.watcher.getCommunicator().sendDeleteCreature(l.longValue());
      }
      this.creatures.clear();
    }
    if (this.fences != null)
    {
      for (Iterator<Fence> it = this.fences.iterator(); it.hasNext();)
      {
        Fence fence = (Fence)it.next();
        this.watcher.getCommunicator().sendRemoveFence(fence);
      }
      this.fences = null;
    }
    if (this.items != null)
    {
      for (Iterator<Item> it = this.items.iterator(); it.hasNext();)
      {
        Item item = (Item)it.next();
        if (item.isMovingItem()) {
          this.watcher.getCommunicator().sendDeleteMovingItem(item.getWurmId());
        } else {
          this.watcher.getCommunicator().sendRemoveItem(item);
        }
      }
      this.items = null;
    }
    if (this.effects != null)
    {
      for (Iterator<Effect> it = this.effects.iterator(); it.hasNext();)
      {
        Effect effect = (Effect)it.next();
        this.watcher.getCommunicator().sendRemoveEffect(effect.getOwner());
      }
      this.effects = null;
    }
  }
  
  void addFence(Fence fence)
  {
    if (this.fences == null) {
      this.fences = new HashSet();
    }
    if (!this.fences.contains(fence)) {
      if (covers(fence.getTileX(), fence.getTileY()))
      {
        this.fences.add(fence);
        this.watcher.getCommunicator().sendAddFence(fence);
        if (fence.getDamage() >= 60.0F) {
          this.watcher.getCommunicator().sendDamageState(fence.getId(), (byte)(int)fence.getDamage());
        }
      }
    }
  }
  
  void removeFence(Fence fence)
  {
    if (this.fences != null)
    {
      if (this.fences.contains(fence)) {
        if (this.watcher != null) {
          this.watcher.getCommunicator().sendRemoveFence(fence);
        }
      }
      this.fences.remove(fence);
    }
  }
  
  public void addMineDoor(MineDoorPermission door)
  {
    if (this.mineDoors == null) {
      this.mineDoors = new HashSet();
    }
    if (!this.mineDoors.contains(door))
    {
      this.mineDoors.add(door);
      this.watcher.getCommunicator().sendAddMineDoor(door);
    }
  }
  
  public void removeMineDoor(MineDoorPermission door)
  {
    if (this.mineDoors != null) {
      if (this.mineDoors.contains(door))
      {
        if (this.watcher != null) {
          this.watcher.getCommunicator().sendRemoveMineDoor(door);
        }
        this.mineDoors.remove(door);
      }
    }
  }
  
  void renameItem(Item item, String newName, String newModelName)
  {
    if ((this.items != null) && (this.items.contains(item))) {
      if (this.watcher != null) {
        this.watcher.getCommunicator().sendRename(item, newName, newModelName);
      }
    }
  }
  
  void sendAttitude(Creature creature)
  {
    if ((this.creatures != null) && (this.creatures.keySet().contains(new Long(creature.getWurmId())))) {
      if ((this.watcher instanceof Player)) {
        this.watcher.getCommunicator().changeAttitude(creature.getWurmId(), creature.getAttitude(this.watcher));
      }
    }
  }
  
  void sendUpdateHasTarget(Creature creature)
  {
    if ((this.creatures != null) && (this.creatures.keySet().contains(new Long(creature.getWurmId())))) {
      if ((this.watcher instanceof Player)) {
        if (creature.getTarget() != null) {
          this.watcher.getCommunicator().sendHasTarget(creature.getWurmId(), true);
        } else {
          this.watcher.getCommunicator().sendHasTarget(creature.getWurmId(), false);
        }
      }
    }
  }
  
  private byte getLayer()
  {
    if (isOnSurface()) {
      return 0;
    }
    return -1;
  }
  
  boolean addCreature(long creatureId, boolean overRideRange)
    throws NoSuchCreatureException, NoSuchPlayerException
  {
    return addCreature(creatureId, overRideRange, -10L, 0.0F, 0.0F, 0.0F);
  }
  
  boolean hasReceivedLocalMessageOnChaos = false;
  
  public final boolean addCreature(long creatureId, boolean overRideRange, long copyId, float offx, float offy, float offz)
    throws NoSuchCreatureException, NoSuchPlayerException
  {
    Creature creature = Server.getInstance().getCreature(creatureId);
    if ((coversCreature(creature)) || ((this.watcher != null) && (this.watcher.isPlayer()) && (overRideRange)))
    {
      if ((this.watcher != null) && (this.watcher.getWurmId() != creatureId)) {
        if (creature.isVisibleTo(this.watcher)) {
          if ((!this.watcher.isPlayer()) || (this.watcher.hasLink()))
          {
            if ((this.creatures.keySet().contains(Long.valueOf(creatureId))) && (copyId == -10L)) {
              return false;
            }
            if (!this.creatures.keySet().contains(Long.valueOf(creatureId))) {
              if (this.watcher.isPlayer())
              {
                this.creatures.put(Long.valueOf(creatureId), new CreatureMove());
                creature.setVisibleToPlayers(true);
              }
              else
              {
                this.creatures.put(Long.valueOf(creatureId), null);
              }
            }
            if (this.watcher.hasLink())
            {
              String suff = "";
              String pre = "";
              if (!this.watcher.hasFlag(56))
              {
                if (!creature.hasFlag(24)) {
                  pre = creature.getAbilityTitle();
                }
                if ((creature.getCultist() != null) && (!creature.hasFlag(25))) {
                  suff = suff + " " + creature.getCultist().getCultistTitleShort();
                }
              }
              boolean enemy = false;
              if ((creature.getPower() > 0) && (!Servers.localServer.testServer))
              {
                if (creature.getPower() == 1) {
                  suff = " (HERO)";
                } else if (creature.getPower() == 2) {
                  suff = " (GM)";
                } else if (creature.getPower() == 3) {
                  suff = " (GOD)";
                } else if (creature.getPower() == 4) {
                  suff = " (ARCH)";
                } else if (creature.getPower() == 5) {
                  suff = " (ADMIN)";
                }
              }
              else
              {
                if (creature.isKing()) {
                  suff = suff + " [" + King.getRulerTitle(creature.getSex() == 0, creature.getKingdomId()) + "]";
                }
                if ((this.watcher.getKingdomId() != 0) && (creature.getKingdomId() != 0) && 
                  (!creature.isFriendlyKingdom(this.watcher.getKingdomId())))
                {
                  if ((creature.getPower() < 2) && (this.watcher.getPower() < 2) && 
                    (creature.isPlayer()))
                  {
                    if ((this.watcher.getCultist() != null) && (this.watcher.getCultist().getLevel() > 8) && 
                      (this.watcher.getCultist().getPath() == 3)) {
                      suff = suff + " (ENEMY)";
                    } else {
                      suff = " (ENEMY)";
                    }
                    enemy = true;
                  }
                }
                else if ((creature.getKingdomTemplateId() != 3) && (creature.getReputation() < 0))
                {
                  suff = suff + " (OUTLAW)";
                  enemy = true;
                }
                else if ((this.watcher.getCitizenVillage() != null) && (creature.isPlayer()) && 
                  (this.watcher.getCitizenVillage().isEnemy(creature)))
                {
                  suff = " (ENEMY)";
                  enemy = true;
                }
                else if (creature.hasAttackedUnmotivated())
                {
                  suff = " (HUNTED)";
                }
                else if ((!this.watcher.isPlayer()) || (!this.watcher.hasFlag(56)))
                {
                  if ((creature.getTitle() != null) || (
                    (Features.Feature.COMPOUND_TITLES.isEnabled()) && (creature.getSecondTitle() != null))) {
                    if (!creature.getTitleString().isEmpty())
                    {
                      suff = suff + " [";
                      suff = suff + creature.getTitleString();
                      suff = suff + "]";
                    }
                  }
                }
                if ((creature.isChampion()) && (creature.getDeity() != null)) {
                  suff = suff + " [Champion of " + creature.getDeity().name + "]";
                }
              }
              if ((enemy) && (creature.getPower() < 2) && (this.watcher.getPower() < 2) && 
                (creature.isPlayer())) {
                if (((creature.getFightingSkill().getRealKnowledge() > 20.0D) || (creature.getFaith() > 25.0F)) && 
                  (Servers.isThisAPvpServer())) {
                  this.watcher.addEnemyPresense();
                }
              }
              byte layer = (byte)creature.getLayer();
              if (overRideRange) {
                layer = getLayer();
              }
              String hoverText = creature.getHoverText(this.watcher);
              this.watcher.getCommunicator().sendNewCreature(copyId != -10L ? copyId : creatureId, pre + 
                StringUtilities.raiseFirstLetterOnly(creature.getName()) + suff, hoverText, creature
                .isUndead() ? creature.getUndeadModelName() : creature.getModelName(), creature
                .getStatus().getPositionX() + offx, creature
                .getStatus().getPositionY() + offy, creature
                .getStatus().getPositionZ() + offz, creature
                .getStatus().getBridgeId(), creature
                .getStatus().getRotation(), layer, 
                
                (creature.getBridgeId() <= 0L) && (!creature.isSubmerged()) && 
                ((creature.getPower() == 0) || (creature.getMovementScheme().onGround)) && 
                (creature.getFloorLevel() <= 0) && 
                (creature.getMovementScheme().getGroundOffset() <= 0.0F), false, creature
                .getTemplate().getTemplateId() != 119, creature
                .getKingdomId(), creature.getFace(), creature.getBlood(), creature.isUndead(), (copyId != -10L) || 
                (creature.isNpc()), creature.getStatus().getModType());
              if (creature.getRarityShader() != 0) {
                setNewRarityShader(creature);
              }
              if (copyId != -10L)
              {
                this.watcher.getCommunicator().setCreatureDamage(copyId, creature.getStatus().calcDamPercent());
                if (creature.getRarityShader() != 0) {
                  this.watcher.getCommunicator().updateCreatureRarity(copyId, creature.getRarityShader());
                }
              }
              for (Item item : creature.getBody().getContainersAndWornItems()) {
                if (item != null) {
                  try
                  {
                    byte armorSlot = item.isArmour() ? BodyTemplate.convertToArmorEquipementSlot((byte)item.getParent().getPlace()) : BodyTemplate.convertToItemEquipementSlot((byte)item.getParent().getPlace());
                    if ((creature.isAnimal()) && (creature.isVehicle())) {
                      this.watcher.getCommunicator().sendHorseWear(creature.getWurmId(), item
                        .getTemplateId(), item
                        .getMaterial(), armorSlot, item.getAuxData());
                    } else {
                      this.watcher.getCommunicator().sendWearItem(copyId != -10L ? copyId : creature
                        .getWurmId(), item
                        .getTemplateId(), armorSlot, 
                        WurmColor.getColorRed(item.getColor()), 
                        WurmColor.getColorGreen(item.getColor()), 
                        WurmColor.getColorBlue(item.getColor()), 
                        WurmColor.getColorRed(item.getColor2()), 
                        WurmColor.getColorGreen(item.getColor2()), 
                        WurmColor.getColorBlue(item.getColor2()), item
                        .getMaterial(), item.getRarity());
                    }
                  }
                  catch (Exception localException) {}
                }
              }
              if (creature.hasCustomColor()) {
                sendRepaint(copyId != -10L ? copyId : creatureId, creature.getColorRed(), creature.getColorGreen(), creature
                  .getColorBlue(), (byte)-1, (byte)creature.getPaintMode());
              }
              if ((creature.hasCustomSize()) || (creature.isFish())) {
                sendResizeCreature(copyId != -10L ? copyId : creatureId, creature.getSizeModX(), creature.getSizeModY(), creature
                  .getSizeModZ());
              }
              if (creature.getBestLightsource() != null) {
                addLightSource(creature, creature.getBestLightsource());
              } else if (creature.isPlayer()) {
                ((Player)creature).sendLantern(this);
              }
              if (this.watcher.isPlayer()) {
                sendCreatureDamage(creature, creature.getStatus().calcDamPercent());
              }
              if (creature.isOnFire()) {
                sendAttachCreatureEffect(creature, (byte)1, creature.getFireRadius(), (byte)-1, (byte)-1, (byte)1);
              }
              if (creature.isGhost())
              {
                this.watcher.getCommunicator().sendAttachEffect(creature.getWurmId(), (byte)2, 
                
                  (byte)(creature.isSpiritGuard() ? -56 : 100), 
                  (byte)1, creature.isSpiritGuard() ? 1 : (byte)0, (byte)1);
                this.watcher.getCommunicator().sendAttachEffect(creature.getWurmId(), (byte)3, (byte)50, 
                
                  (byte)(creature.isSpiritGuard() ? 50 : 50), (byte)50, (byte)1);
              }
              else if (creature.hasGlow())
              {
                if (creature.hasCustomColor()) {
                  this.watcher.getCommunicator().sendAttachEffect(creature.getWurmId(), (byte)3, (byte)1, (byte)1, (byte)1, (byte)1);
                } else {
                  this.watcher.getCommunicator().sendAttachEffect(creature.getWurmId(), (byte)3, creature
                    .getColorRed(), creature
                    .getColorGreen(), creature.getColorGreen(), (byte)1);
                }
              }
              sendCreatureItems(creature);
              if ((creature.isPlayer()) || (creature.isNpc())) {
                if ((!Servers.localServer.PVPSERVER) || 
                  (this.watcher.isPaying())) {
                  this.watcher.getCommunicator().sendAddLocal(creature.getName(), creatureId);
                }
              }
            }
            if (this.watcher.isTypeFleeing()) {
              if ((creature.isPlayer()) || (creature.isAggHuman()) || (creature.isHuman()) || (creature.isCarnivore()) || 
                (creature.isMonster()))
              {
                float newDistance = creature.getPos2f().distance(this.watcher.getPos2f());
                if (Features.Feature.CREATURE_MOVEMENT_CHANGES.isEnabled())
                {
                  int baseCounter = (int)(Math.max(1.0F, creature.getBaseCombatRating() - this.watcher.getBaseCombatRating()) * 5.0F);
                  if (baseCounter - newDistance > 0.0F) {
                    this.watcher.setFleeCounter((int)Math.min(60.0F, Math.max(3.0F, baseCounter - newDistance)));
                  }
                }
                else
                {
                  this.watcher.setFleeCounter(60);
                }
              }
            }
            checkIfAttack(creature, creatureId);
            byte att = creature.getAttitude(this.watcher);
            if (att != 0) {
              this.watcher.getCommunicator().changeAttitude(copyId != -10L ? copyId : creatureId, att);
            }
            if (creature.getVehicle() != -10L)
            {
              Vehicle vehic = Vehicles.getVehicleForId(creature.getVehicle());
              if (vehic != null)
              {
                Seat s = vehic.getSeatFor(creature.getWurmId());
                if (s != null) {
                  sendAttachCreature(creatureId, creature.getVehicle(), s.offx, s.offy, s.offz, vehic
                    .getSeatNumberFor(s));
                }
              }
            }
            if (creature.getHitched() != null)
            {
              Seat s = creature.getHitched().getHitchSeatFor(creature.getWurmId());
              if (s != null) {
                sendAttachCreature(creatureId, creature.getHitched().wurmid, s.offx, s.offy, s.offz, 0);
              }
            }
            if (creature.getTarget() != null) {
              this.watcher.getCommunicator().sendHasTarget(creature.getWurmId(), true);
            }
            if (creature.isRidden())
            {
              Vehicle vehic = Vehicles.getVehicleForId(creatureId);
              if (vehic != null)
              {
                Seat[] seats = vehic.getSeats();
                for (int x = 0; x < seats.length; x++) {
                  if (seats[x].isOccupied())
                  {
                    if (!this.creatures.containsKey(Long.valueOf(seats[x].occupant))) {
                      try
                      {
                        addCreature(seats[x].occupant, true);
                      }
                      catch (NoSuchCreatureException nsc)
                      {
                        logger.log(Level.INFO, nsc.getMessage(), nsc);
                      }
                      catch (NoSuchPlayerException nsp)
                      {
                        logger.log(Level.INFO, nsp.getMessage(), nsp);
                      }
                    }
                    sendAttachCreature(seats[x].occupant, creatureId, seats[x].offx, seats[x].offy, seats[x].offz, x);
                  }
                }
              }
            }
            return true;
          }
        }
      }
    }
    else {
      removeCreature(creature);
    }
    return false;
  }
  
  public void sendCreatureItems(Creature creature)
  {
    if (creature.isPlayer())
    {
      try
      {
        Item lTempItem = creature.getEquippedWeapon((byte)37);
        if ((lTempItem != null) && (!lTempItem.isBodyPartAttached())) {
          sendWieldItem(creature.getWurmId() == this.watcher.getWurmId() ? -1L : creature.getWurmId(), (byte)0, lTempItem
            .getModelName(), lTempItem.getRarity(), 
            WurmColor.getColorRed(lTempItem.getColor()), WurmColor.getColorGreen(lTempItem.getColor()), WurmColor.getColorBlue(lTempItem.getColor()), 
            WurmColor.getColorRed(lTempItem.getColor2()), WurmColor.getColorGreen(lTempItem.getColor2()), WurmColor.getColorBlue(lTempItem.getColor2()));
        }
      }
      catch (NoSpaceException nsp)
      {
        logger.log(Level.WARNING, creature
          .getName() + " could not get equipped weapon for left hand due to " + nsp.getMessage(), nsp);
      }
      try
      {
        Item lTempItem = creature.getEquippedWeapon((byte)38);
        if ((lTempItem != null) && (!lTempItem.isBodyPartAttached())) {
          sendWieldItem(creature.getWurmId() == this.watcher.getWurmId() ? -1L : creature.getWurmId(), (byte)1, lTempItem
            .getModelName(), lTempItem.getRarity(), 
            WurmColor.getColorRed(lTempItem.getColor()), WurmColor.getColorGreen(lTempItem.getColor()), WurmColor.getColorBlue(lTempItem.getColor()), 
            WurmColor.getColorRed(lTempItem.getColor2()), WurmColor.getColorGreen(lTempItem.getColor2()), WurmColor.getColorBlue(lTempItem.getColor2()));
        }
      }
      catch (NoSpaceException nsp)
      {
        logger.log(Level.WARNING, creature
          .getName() + " could not get equipped weapon for right hand due to " + nsp.getMessage(), nsp);
      }
    }
  }
  
  void newLayer(Creature creature, boolean tileIsSurfaced)
  {
    if ((creature != null) && (this.watcher.getWurmId() != creature.getWurmId()))
    {
      if (this.creatures.containsKey(Long.valueOf(creature.getWurmId())))
      {
        if (this.watcher.hasLink()) {
          this.watcher.getCommunicator().sendCreatureChangedLayer(creature.getWurmId(), (byte)creature.getLayer());
        }
        if (isOnSurface())
        {
          if ((this.watcher.getVisionArea().getUnderGround() != null) && (this.watcher.getVisionArea().getUnderGround().coversCreature(creature)))
          {
            CreatureMove cm = (CreatureMove)this.creatures.remove(Long.valueOf(creature.getWurmId()));
            
            addToVisionArea(creature, cm, this.watcher.getVisionArea().getUnderGround());
          }
          else
          {
            try
            {
              deleteCreature(creature, true);
            }
            catch (NoSuchCreatureException localNoSuchCreatureException) {}catch (NoSuchPlayerException localNoSuchPlayerException) {}
          }
        }
        else
        {
          CreatureMove cm = (CreatureMove)this.creatures.remove(Long.valueOf(creature.getWurmId()));
          addToVisionArea(creature, cm, this.watcher.getVisionArea().getSurface());
        }
      }
    }
    else if ((creature != null) && (this.watcher.getWurmId() == creature.getWurmId())) {
      if ((this.watcher.getVehicle() != -10L) && (!this.watcher.isVehicleCommander())) {
        this.watcher.getCommunicator().sendCreatureChangedLayer(-1L, (byte)creature.getLayer());
      }
    }
  }
  
  public void justSendNewLayer(Item item)
  {
    if (this.watcher.getVehicle() != item.getWurmId()) {
      this.watcher.getCommunicator().sendCreatureChangedLayer(item.getWurmId(), item.newLayer);
    }
  }
  
  public void addToVisionArea(Creature creature, CreatureMove cm, VirtualZone newzone)
  {
    newzone.addCreatureToMap(creature, cm);
    if (creature.isRidden())
    {
      Set<Long> riders = creature.getRiders();
      for (Long rider : riders)
      {
        cm = (CreatureMove)this.creatures.remove(Long.valueOf(rider.longValue()));
        try
        {
          newzone.addCreature(rider.longValue(), true);
        }
        catch (Exception nex)
        {
          logger.log(Level.WARNING, nex.getMessage(), nex);
        }
      }
    }
  }
  
  void newLayer(Item vehicle)
  {
    if (vehicle != null) {
      if ((this.items != null) && (this.items.contains(vehicle)))
      {
        byte newlayer = vehicle.isOnSurface() ? 0 : -1;
        if (vehicle.newLayer != Byte.MIN_VALUE) {
          newlayer = vehicle.newLayer;
        }
        if (this.watcher.hasLink()) {
          this.watcher.getCommunicator().sendCreatureChangedLayer(vehicle.getWurmId(), newlayer);
        }
        if (newlayer < 0)
        {
          if ((this.watcher.getVisionArea().getUnderGround() != null) && (this.watcher.getVisionArea().getUnderGround().covers(vehicle.getTileX(), vehicle.getTileY()))) {
            this.watcher.getVisionArea().getUnderGround().addItem(vehicle, null, true);
          } else {
            removeItem(vehicle);
          }
        }
        else {
          this.watcher.getVisionArea().getSurface().addItem(vehicle, null, true);
        }
        this.items.remove(vehicle);
      }
    }
  }
  
  public void addCreatureToMap(Creature creature, CreatureMove cm)
  {
    if (cm != null) {
      this.creatures.put(Long.valueOf(creature.getWurmId()), cm);
    }
  }
  
  public void checkForEnemies()
  {
    for (Long cid : this.creatures.keySet()) {
      if (this.watcher.target == -10L) {
        try
        {
          Creature creature = Server.getInstance().getCreature(cid.longValue());
          checkIfAttack(creature, cid.longValue());
        }
        catch (NoSuchCreatureException localNoSuchCreatureException) {}catch (NoSuchPlayerException localNoSuchPlayerException) {}
      } else {
        return;
      }
    }
  }
  
  private void checkIfAttack(Creature creature, long creatureId)
  {
    if (this.watcher.getTemplate().getCreatureAI() != null) {
      if (this.watcher.getTemplate().getCreatureAI().maybeAttackCreature(this.watcher, this, creature)) {
        return;
      }
    }
    if (creature.isTransferring()) {
      return;
    }
    if (this.watcher.isPlayer())
    {
      if ((creature.addingAfterTeleport) && (this.watcher.lastOpponent == creature)) {
        if (creature.isWithinDistanceTo(this.watcher.getPosX(), this.watcher.getPosY(), this.watcher
          .getPositionZ() + this.watcher.getAltOffZ(), 12.0F)) {
          this.watcher.setTarget(creatureId, false);
        }
      }
      return;
    }
    if ((creature.isNpc()) && (this.watcher.isNpc()) && 
      (creature.getAttitude(this.watcher) != 2)) {
      return;
    }
    if ((creature.getLayer() == this.watcher.getLayer()) || (this.watcher.isKingdomGuard()) || (this.watcher.isUnique()) || (this.watcher.isWarGuard()))
    {
      if (creature.fleeCounter > 0) {
        return;
      }
      if ((creature.getVehicle() > -10L) && (this.watcher.isNoAttackVehicles())) {
        return;
      }
      if (creature.getCultist() != null) {
        if ((creature.getCultist().hasFearEffect()) || (creature.getCultist().hasLoveEffect())) {
          return;
        }
      }
      if (!creature.isWithinDistanceTo(this.watcher.getPosX(), this.watcher.getPosY(), this.watcher
        .getPositionZ() + this.watcher.getAltOffZ(), 
        (this.watcher.isSpiritGuard()) || (this.watcher.isKingdomGuard()) || (this.watcher.isWarGuard()) || (this.watcher.isUnique()) ? 30.0F : 12.0F)) {
        if ((!isCreatureTurnedTowardsTarget(creature, this.watcher)) && (!this.watcher.isKingdomGuard()) && 
          (!this.watcher.isWarGuard())) {
          return;
        }
      }
      if (creature.isBridgeBlockingAttack(this.watcher, true)) {
        return;
      }
      if (this.watcher.getAttitude(creature) == 2)
      {
        if (this.watcher.target == -10L)
        {
          if (this.watcher.isKingdomGuard())
          {
            if ((creature.getCurrentTile().getKingdom() == this.watcher.getKingdomId()) || 
              (this.watcher.getKingdomId() == 0))
            {
              GuardTower gt = Kingdoms.getTower(this.watcher);
              if (gt != null)
              {
                int tpx = gt.getTower().getTileX();
                int tpy = gt.getTower().getTileY();
                if (creature.isWithinTileDistanceTo(tpx, tpy, (int)gt.getTower().getPosZ(), 50)) {
                  if (creature.isRidden())
                  {
                    if (Server.rand.nextInt(50) == 0) {
                      this.watcher.setTarget(creatureId, false);
                    }
                  }
                  else if ((creature.isPlayer()) || (creature.isDominated())) {
                    this.watcher.setTarget(creatureId, false);
                  } else if (this.watcher.getAlertSeconds() > 0) {
                    if (creature.isAggHuman())
                    {
                      this.watcher.setTarget(creatureId, false);
                      if (this.watcher.target == creatureId) {
                        GuardTower.yellHunt(this.watcher, creature, false);
                      }
                    }
                  }
                }
              }
            }
            return;
          }
          if (this.watcher.isWarGuard())
          {
            Item target = Kingdoms.getClosestWarTarget(this.watcher.getTileX(), this.watcher.getTileY(), this.watcher);
            if (target != null) {
              if (this.watcher.isWithinTileDistanceTo(target.getTileX(), target.getTileY(), 0, 15)) {
                if (creature.isWithinTileDistanceTo(target.getTileX(), target.getTileY(), 0, 5))
                {
                  this.watcher.setTarget(creatureId, false);
                  return;
                }
              }
            }
          }
          else if (this.watcher.isDominated())
          {
            this.watcher.setTarget(creatureId, false);
          }
          if (!this.watcher.isSpiritGuard()) {
            if ((creature.isRidden()) && (Server.rand.nextInt(10) == 0)) {
              this.watcher.setTarget(creatureId, false);
            } else if ((creature.isDominated()) && (Server.rand.nextInt(10) == 0)) {
              this.watcher.setTarget(creatureId, false);
            }
          }
          if (((creature instanceof Player)) && (this.watcher.isAggHuman()))
          {
            if (!creature.hasLink()) {
              return;
            }
            if ((creature.getSpellEffects() != null) && 
              (creature.getSpellEffects().getSpellEffect((byte)73) != null)) {
              if (!creature.isWithinDistanceTo(this.watcher, 7.0F)) {
                return;
              }
            }
            if ((creature.addingAfterTeleport) || 
            
              (Server.rand.nextInt(100) <= this.watcher.getAggressivity() * this.watcher.getStatus().getAggTypeModifier())) {
              this.watcher.setTarget(creatureId, false);
            }
          }
          else if ((this.watcher.isAggHuman()) && (creature.isKingdomGuard()))
          {
            if ((creature.addingAfterTeleport) || (
              (creature.getAlertSeconds() > 0) && (Server.rand.nextInt((int)Math.max(1.0F, this.watcher
              .getAggressivity() * this.watcher.getStatus().getAggTypeModifier())) == 0))) {
              this.watcher.setTarget(creatureId, false);
            }
          }
          else if ((this.watcher.isAggHuman()) && (creature.isSpiritGuard()))
          {
            if (creature.getCitizenVillage() != null)
            {
              if (!creature.getCitizenVillage().isEnemy(this.watcher)) {
                return;
              }
              if ((creature.addingAfterTeleport) || 
                (Server.rand.nextInt((int)Math.max(1.0F, this.watcher.getAggressivity() * this.watcher
                .getStatus().getAggTypeModifier())) == 0)) {
                this.watcher.setTarget(creatureId, false);
              }
            }
          }
          else if (this.watcher.isSpiritGuard())
          {
            if (this.watcher.getCitizenVillage() == null)
            {
              if ((creature.addingAfterTeleport) || (Server.rand.nextInt(100) <= 80)) {
                this.watcher.setTarget(creatureId, false);
              }
            }
            else if (creature.isRidden())
            {
              if (!this.watcher.getCitizenVillage().isEnemy(creature)) {
                return;
              }
              if (Server.rand.nextInt(100) == 0) {
                this.watcher.setTarget(creatureId, false);
              }
            }
            else if (((creature.isPlayer()) || (creature.isBreakFence()) || (creature.isDominated())) && 
              (this.watcher.getCitizenVillage().isWithinAttackPerimeter(creature.getTileX(), creature
              .getTileY())))
            {
              if (!this.watcher.getCitizenVillage().isEnemy(creature)) {
                return;
              }
              this.watcher.setTarget(creatureId, false);
            }
          }
          else
          {
            this.watcher.setTarget(creatureId, false);
          }
        }
      }
      else if (this.watcher.getTemplate().getLeaderTemplateId() > 0) {
        if ((this.watcher.leader == null) && (!this.watcher.isDominated())) {
          if (creature.getTemplate().getTemplateId() == this.watcher.getTemplate().getLeaderTemplateId()) {
            if ((!this.watcher.isHerbivore()) || (!this.watcher.isHungry())) {
              if ((creature.getPositionZ() >= -0.71D) || (
                (creature.isSwimming()) && (this.watcher.isSwimming()))) {
                if (creature.mayLeadMoreCreatures())
                {
                  creature.addFollower(this.watcher, null);
                  this.watcher.setLeader(creature);
                }
              }
            }
          }
        }
      }
    }
  }
  
  void addAreaSpellEffect(AreaSpellEffect effect, boolean loop)
  {
    if (effect != null)
    {
      if (this.areaEffects == null) {
        this.areaEffects = new HashSet();
      }
      if (!this.areaEffects.contains(effect))
      {
        this.areaEffects.add(effect);
        if (this.watcher.hasLink()) {
          this.watcher.getCommunicator().sendAddAreaSpellEffect(effect.getTilex(), effect.getTiley(), effect.getLayer(), effect
            .getType(), effect.getFloorLevel(), effect.getHeightOffset(), loop);
        }
      }
    }
  }
  
  void removeAreaSpellEffect(AreaSpellEffect effect)
  {
    if ((this.areaEffects != null) && (this.areaEffects.contains(effect)))
    {
      this.areaEffects.remove(effect);
      if ((effect != null) && (this.watcher.hasLink())) {
        this.watcher.getCommunicator().sendRemoveAreaSpellEffect(effect.getTilex(), effect.getTiley(), effect.getLayer());
      }
    }
  }
  
  public void addEffect(Effect effect, boolean temp)
  {
    if (this.effects == null) {
      this.effects = new HashSet();
    }
    if ((!this.effects.contains(effect)) || (temp))
    {
      if ((!temp) && ((WurmId.getType(effect.getOwner()) == 2) || 
        (WurmId.getType(effect.getOwner()) == 6))) {
        try
        {
          Item effectHolder = Items.getItem(effect.getOwner());
          if ((this.items == null) || (!this.items.contains(effectHolder))) {
            return;
          }
        }
        catch (NoSuchItemException nsi)
        {
          return;
        }
      }
      if (!temp) {
        this.effects.add(effect);
      }
      if (this.watcher.hasLink()) {
        this.watcher.getCommunicator().sendAddEffect(effect.getOwner(), effect.getType(), effect.getPosX(), effect
          .getPosY(), effect.getPosZ(), effect.getLayer(), effect.getEffectString(), effect
          .getTimeout(), effect.getRotationOffset());
      }
    }
  }
  
  public void removeEffect(Effect effect)
  {
    if ((this.effects != null) && (this.effects.contains(effect)))
    {
      this.effects.remove(effect);
      if (this.watcher.hasLink()) {
        this.watcher.getCommunicator().sendRemoveEffect(effect.getOwner());
      }
    }
  }
  
  void removeCreature(Creature creature)
  {
    if ((!coversCreature(creature)) || (creature.isLoggedOut()))
    {
      if ((!this.watcher.isSpiritGuard()) && (creature.getWurmId() == this.watcher.target)) {
        this.watcher.setTarget(-10L, true);
      }
      if ((this.creatures != null) && (this.creatures.keySet().contains(Long.valueOf(creature.getWurmId()))))
      {
        this.creatures.remove(Long.valueOf(creature.getWurmId()));
        checkIfEnemyIsPresent(false);
        if ((creature.getCurrentTile() == null) || (!creature.getCurrentTile().isVisibleToPlayers())) {
          creature.setVisibleToPlayers(false);
        }
        if (this.watcher.hasLink())
        {
          this.watcher.getCommunicator().sendDeleteCreature(creature.getWurmId());
          if (((creature instanceof Player)) || (creature.isNpc())) {
            this.watcher.getCommunicator().sendRemoveLocal(creature.getName());
          }
          if (this.watcher.isPlayer())
          {
            Vehicle vehic = Vehicles.getVehicleForId(creature.getWurmId());
            if (vehic != null)
            {
              Seat[] seats = vehic.getSeats();
              for (int x = 0; x < seats.length; x++) {
                if (seats[x].isOccupied()) {
                  try
                  {
                    Creature occ = Server.getInstance().getCreature(seats[x].occupant);
                    this.watcher.getCommunicator().sendRemoveLocal(occ.getName());
                  }
                  catch (NoSuchCreatureException nsc)
                  {
                    logger.log(Level.WARNING, nsc.getMessage(), nsc);
                  }
                  catch (NoSuchPlayerException nsp)
                  {
                    logger.log(Level.WARNING, nsp.getMessage(), nsp);
                  }
                }
              }
            }
          }
        }
      }
    }
  }
  
  public boolean checkIfEnemyIsPresent(boolean checkedFromOtherVirtualZone)
  {
    if ((this.watcher.isPlayer()) && (!this.watcher.isTeleporting())) {
      if ((this.watcher.hasLink()) && (this.watcher.getVisionArea() != null) && (this.watcher.getVisionArea().isInitialized()))
      {
        boolean foundEnemy = false;
        if (this.creatures != null)
        {
          Long[] crets = getCreatures();
          for (int x = 0; x < crets.length; x++) {
            if (WurmId.getType(crets[x].longValue()) == 0) {
              try
              {
                Creature creature = Server.getInstance().getCreature(crets[x].longValue());
                if ((this.watcher.getKingdomId() != 0) && (creature.getKingdomId() != 0) && 
                  (!this.watcher.isFriendlyKingdom(creature.getKingdomId())))
                {
                  if ((creature.getPower() < 2) && (this.watcher.getPower() < 2) && 
                    (creature.getFightingSkill().getRealKnowledge() > 20.0D)) {
                    foundEnemy = true;
                  }
                }
                else if ((this.watcher.getCitizenVillage() != null) && (this.watcher.getCitizenVillage().isEnemy(creature))) {
                  if ((creature.getPower() < 2) && (this.watcher.getPower() < 2) && 
                    (creature.getFightingSkill().getRealKnowledge() > 20.0D)) {
                    foundEnemy = true;
                  }
                }
              }
              catch (Exception localException) {}
            }
          }
        }
        if (!foundEnemy)
        {
          if (!checkedFromOtherVirtualZone)
          {
            boolean found = false;
            if (this.watcher.getVisionArea() != null) {
              if (this.isOnSurface) {
                found = this.watcher.getVisionArea().getUnderGround().checkIfEnemyIsPresent(true);
              } else {
                found = this.watcher.getVisionArea().getSurface().checkIfEnemyIsPresent(true);
              }
            }
            if (!found) {
              this.watcher.removeEnemyPresense();
            }
            return found;
          }
          return false;
        }
      }
    }
    return true;
  }
  
  void makeInvisible(Creature creature)
  {
    if ((this.creatures != null) && (this.creatures.keySet().contains(new Long(creature.getWurmId()))))
    {
      this.creatures.remove(new Long(creature.getWurmId()));
      checkIfEnemyIsPresent(false);
      if (this.watcher.hasLink()) {
        this.watcher.getCommunicator().sendDeleteCreature(creature.getWurmId());
      }
      if (((creature instanceof Player)) || (creature.isNpc())) {
        this.watcher.getCommunicator().sendRemoveLocal(creature.getName());
      }
    }
  }
  
  private boolean coversCreature(Creature creature)
  {
    if (creature.isDead()) {
      return false;
    }
    if (creature == this.watcher) {
      return true;
    }
    if ((creature.isPlayer()) && (Servers.localServer.PVPSERVER))
    {
      if ((this.watcher.isOnSurface()) && (creature.isOnSurface()) && 
        (this.watcher.isWithinDistanceTo(creature.getTileX(), creature.getTileY(), 80))) {
        return true;
      }
      if ((!this.watcher.isOnSurface()) && (creature.isOnSurface()) && 
        (this.watcher.isWithinDistanceTo(creature.getTileX(), creature.getTileY(), caveToSurfaceLocalDistance))) {
        return true;
      }
      if ((this.watcher.isOnSurface()) && (!creature.isOnSurface()) && 
        (this.watcher.isWithinDistanceTo(creature.getTileX(), creature.getTileY(), 20))) {
        return true;
      }
      if ((!this.watcher.isOnSurface()) && (!creature.isOnSurface()) && 
        (this.watcher.isWithinDistanceTo(creature.getTileX(), creature.getTileY(), 20))) {
        return true;
      }
      return false;
    }
    return covers(creature.getTileX(), creature.getTileY());
  }
  
  public void moveAllCreatures()
  {
    Map.Entry<Long, CreatureMove>[] arr = (Map.Entry[])this.creatures.entrySet().toArray(new Map.Entry[this.creatures.size()]);
    for (int x = 0; x < arr.length; x++) {
      if (((CreatureMove)arr[x].getValue()).timestamp != 0L) {
        try
        {
          Creature creature = Server.getInstance().getCreature(((Long)arr[x].getKey()).longValue());
          if (isMovingSameLevel(creature))
          {
            if (Structure.isGroundFloorAtPosition(creature.getPosX(), creature.getPosY(), creature.isOnSurface())) {
              this.watcher.getCommunicator().sendMoveCreatureAndSetZ(((Long)arr[x].getKey()).longValue(), ((CreatureMove)arr[x].getValue()).diffX, 
                ((CreatureMove)arr[x].getValue()).diffY, creature.getPositionZ(), ((CreatureMove)arr[x].getValue()).rotation);
            } else {
              this.watcher.getCommunicator().sendMoveCreature(((Long)arr[x].getKey()).longValue(), ((CreatureMove)arr[x].getValue()).diffX, 
                ((CreatureMove)arr[x].getValue()).diffY, ((CreatureMove)arr[x].getValue()).rotation, creature.isMoving());
            }
          }
          else {
            this.watcher.getCommunicator().sendMoveCreatureAndSetZ(((Long)arr[x].getKey()).longValue(), ((CreatureMove)arr[x].getValue()).diffX, 
              ((CreatureMove)arr[x].getValue()).diffY, creature.getPositionZ(), ((CreatureMove)arr[x].getValue()).rotation);
          }
          clearCreatureMove(creature, (CreatureMove)arr[x].getValue());
        }
        catch (NoSuchCreatureException localNoSuchCreatureException) {}catch (NoSuchPlayerException localNoSuchPlayerException) {}
      }
    }
  }
  
  private static final boolean isMovingSameLevel(Creature creature)
  {
    return (creature.getBridgeId() <= 0L) && (((creature.isPlayer()) && (creature.getMovementScheme().onGround)) || (
      (!creature.isSubmerged()) && 
      (creature.getFloorLevel() <= 0) && (creature.getMovementScheme().getGroundOffset() == 0.0F)));
  }
  
  boolean creatureMoved(long creatureId, int diffX, int diffY, int diffZ, int diffTileX, int diffTileY)
    throws NoSuchCreatureException, NoSuchPlayerException
  {
    if ((this.watcher == null) || ((this.watcher.isPlayer()) && (!this.watcher.hasLink()))) {
      return true;
    }
    Creature creature = Server.getInstance().getCreature(creatureId);
    if (!this.watcher.equals(creature))
    {
      if (coversCreature(creature))
      {
        if (creature.isVisibleTo(this.watcher))
        {
          boolean sentMultiples;
          byte tosendx;
          byte tosendy;
          if (!addCreature(creatureId, false)) {
            if (this.watcher.isPlayer())
            {
              if (this.watcher.hasLink())
              {
                if (creature.isPlayer())
                {
                  Set<MovementEntity> illusions = Creature.getIllusionsFor(creatureId);
                  if (illusions != null) {
                    for (MovementEntity e : illusions)
                    {
                      this.watcher.getCommunicator().sendMoveCreature(e.getWurmid(), 
                        e.getMovePosition().diffX, 
                        e.getMovePosition().diffY, 
                        (int)(creature.getStatus().getRotation() * 256.0F / 360.0F), true);
                      if (e.shouldExpire()) {
                        this.watcher.getCommunicator().sendDeleteCreature(e.getWurmid());
                      }
                    }
                  }
                }
                CreatureMove cmove = (CreatureMove)this.creatures.get(new Long(creatureId));
                
                boolean moveSameLevel = isMovingSameLevel(creature);
                if ((diffX != 0) || (diffY != 0) || (diffZ != 0))
                {
                  this.MOVELIMIT = ((byte)Math.max(10, 
                    Math.min(70, Creature.rangeTo(creature, this.watcher))));
                  if ((diffX + cmove.diffX > this.MOVELIMIT) || (diffX + cmove.diffX < -this.MOVELIMIT) || (diffY + cmove.diffY > this.MOVELIMIT) || (diffY + cmove.diffY < -this.MOVELIMIT) || (diffZ + cmove.diffZ > this.MOVELIMIT) || (diffZ + cmove.diffZ < -this.MOVELIMIT))
                  {
                    cmove.timestamp = System.currentTimeMillis();
                    if (moveSameLevel)
                    {
                      if (Structure.isGroundFloorAtPosition(creature.getPosX(), creature.getPosY(), creature.isOnSurface())) {
                        this.watcher.getCommunicator().sendMoveCreatureAndSetZ(creatureId, cmove.diffX, cmove.diffY, creature
                          .getPositionZ(), cmove.rotation);
                      } else {
                        this.watcher.getCommunicator().sendMoveCreature(creatureId, cmove.diffX, cmove.diffY, cmove.rotation, creature
                          .isMoving());
                      }
                    }
                    else {
                      this.watcher.getCommunicator().sendMoveCreatureAndSetZ(creatureId, cmove.diffX, cmove.diffY, creature
                        .getPositionZ(), cmove.rotation);
                    }
                    cmove.diffX = 0;
                    cmove.diffY = 0;
                    cmove.diffZ = 0;
                    cmove.rotation = ((int)(creature.getStatus().getRotation() * 256.0F / 360.0F));
                    sentMultiples = false;
                    while ((diffX > this.MOVELIMIT) || (diffX < -this.MOVELIMIT) || (diffY > this.MOVELIMIT) || (diffY < -this.MOVELIMIT) || (diffZ > this.MOVELIMIT) || (diffZ < -this.MOVELIMIT))
                    {
                      sentMultiples = true;
                      tosendx = 0;
                      tosendy = 0;
                      
                      byte tosendz = 0;
                      if (diffX > this.MOVELIMIT)
                      {
                        tosendx = this.MOVELIMIT;
                        diffX -= this.MOVELIMIT;
                      }
                      else if (diffX < -this.MOVELIMIT)
                      {
                        tosendx = (byte)-this.MOVELIMIT;
                        diffX += this.MOVELIMIT;
                      }
                      else if (diffX != 0)
                      {
                        tosendx = (byte)diffX;
                        diffX = 0;
                      }
                      if (diffY > this.MOVELIMIT)
                      {
                        tosendy = this.MOVELIMIT;
                        diffY -= this.MOVELIMIT;
                      }
                      else if (diffY < -this.MOVELIMIT)
                      {
                        tosendy = (byte)-this.MOVELIMIT;
                        diffY += this.MOVELIMIT;
                      }
                      else if (diffY != 0)
                      {
                        tosendy = (byte)diffY;
                        diffY = 0;
                      }
                      if (diffZ > this.MOVELIMIT)
                      {
                        tosendz = this.MOVELIMIT;
                        diffZ -= this.MOVELIMIT;
                      }
                      else if (diffZ < -this.MOVELIMIT)
                      {
                        tosendz = (byte)-this.MOVELIMIT;
                        diffZ += this.MOVELIMIT;
                      }
                      else if (diffZ != 0)
                      {
                        tosendz = (byte)diffZ;
                        diffZ = 0;
                      }
                      cmove.diffX = diffX;
                      cmove.diffY = diffY;
                      cmove.diffZ = diffZ;
                      if (moveSameLevel)
                      {
                        if (Structure.isGroundFloorAtPosition(creature.getPosX(), creature.getPosY(), creature.isOnSurface())) {
                          this.watcher.getCommunicator().sendMoveCreatureAndSetZ(creatureId, tosendx, tosendy, creature
                            .getPositionZ(), cmove.rotation);
                        } else {
                          this.watcher.getCommunicator().sendMoveCreature(creatureId, tosendx, tosendy, cmove.rotation, creature
                            .isMoving());
                        }
                      }
                      else {
                        this.watcher.getCommunicator().sendMoveCreatureAndSetZ(creatureId, tosendx, tosendy, creature
                          .getPositionZ(), cmove.rotation);
                      }
                    }
                    if (!sentMultiples)
                    {
                      cmove.diffX = diffX;
                      cmove.diffY = diffY;
                      cmove.diffZ = diffZ;
                    }
                  }
                  else if ((creature.getAttitude(this.watcher) == 2) || (Math.abs(diffZ) > 3))
                  {
                    if ((creature.isSubmerged()) && (creature.getPositionZ() < 0.0F))
                    {
                      if (cmove.timestamp != 0L) {
                        this.watcher.getCommunicator().sendMoveCreatureAndSetZ(creatureId, cmove.diffX, cmove.diffY, creature
                          .getPositionZ(), cmove.rotation);
                      }
                      clearCreatureMove(creature, cmove);
                      
                      this.watcher.getCommunicator().sendMoveCreatureAndSetZ(creatureId, diffX, diffY, creature
                        .getPositionZ(), 
                        (int)(creature.getStatus().getRotation() * 256.0F / 360.0F));
                    }
                    else
                    {
                      if (cmove.timestamp != 0L) {
                        if (moveSameLevel)
                        {
                          if (Structure.isGroundFloorAtPosition(creature.getPosX(), creature.getPosY(), creature.isOnSurface())) {
                            this.watcher.getCommunicator().sendMoveCreatureAndSetZ(creatureId, cmove.diffX, cmove.diffY, creature
                              .getPositionZ(), cmove.rotation);
                          } else {
                            this.watcher.getCommunicator().sendMoveCreature(creatureId, cmove.diffX, cmove.diffY, cmove.rotation, creature
                              .isMoving());
                          }
                        }
                        else {
                          this.watcher.getCommunicator().sendMoveCreatureAndSetZ(creatureId, cmove.diffX, cmove.diffY, creature
                            .getPositionZ(), cmove.rotation);
                        }
                      }
                      clearCreatureMove(creature, cmove);
                      if (moveSameLevel)
                      {
                        if (Structure.isGroundFloorAtPosition(creature.getPosX(), creature.getPosY(), creature.isOnSurface())) {
                          this.watcher.getCommunicator().sendMoveCreatureAndSetZ(creatureId, diffX, diffY, creature
                            .getPositionZ(), 
                            (int)(creature.getStatus().getRotation() * 256.0F / 360.0F));
                        } else {
                          this.watcher.getCommunicator().sendMoveCreature(creatureId, diffX, diffY, 
                            (int)(creature.getStatus().getRotation() * 256.0F / 360.0F), creature
                            .isMoving());
                        }
                      }
                      else {
                        this.watcher.getCommunicator().sendMoveCreatureAndSetZ(creatureId, diffX, diffY, creature
                          .getPositionZ(), 
                          (int)(creature.getStatus().getRotation() * 256.0F / 360.0F));
                      }
                    }
                  }
                  else
                  {
                    cmove.timestamp = System.currentTimeMillis();
                    cmove.diffX += diffX;
                    cmove.diffY += diffY;
                    cmove.diffZ += diffZ;
                    cmove.rotation = ((int)(creature.getStatus().getRotation() * 256.0F / 360.0F));
                  }
                }
                else if (creature.getAttitude(this.watcher) == 2)
                {
                  if (cmove.timestamp != 0L) {
                    if (moveSameLevel)
                    {
                      if (Structure.isGroundFloorAtPosition(creature.getPosX(), creature.getPosY(), creature.isOnSurface())) {
                        this.watcher.getCommunicator().sendMoveCreatureAndSetZ(creatureId, cmove.diffX, cmove.diffY, creature
                          .getPositionZ(), cmove.rotation);
                      } else {
                        this.watcher.getCommunicator().sendMoveCreature(creatureId, cmove.diffX, cmove.diffY, cmove.rotation, creature
                          .isMoving());
                      }
                    }
                    else {
                      this.watcher.getCommunicator().sendMoveCreatureAndSetZ(creatureId, cmove.diffX, cmove.diffY, creature
                        .getPositionZ(), cmove.rotation);
                    }
                  }
                  clearCreatureMove(creature, cmove);
                  if (moveSameLevel)
                  {
                    if (Structure.isGroundFloorAtPosition(creature.getPosX(), creature.getPosY(), creature.isOnSurface())) {
                      this.watcher.getCommunicator().sendMoveCreatureAndSetZ(creatureId, diffX, diffY, creature
                        .getPositionZ(), 
                        (int)(creature.getStatus().getRotation() * 256.0F / 360.0F));
                    } else {
                      this.watcher.getCommunicator().sendMoveCreature(creatureId, diffX, diffY, 
                        (int)(creature.getStatus().getRotation() * 256.0F / 360.0F), creature
                        .isMoving());
                    }
                  }
                  else {
                    this.watcher.getCommunicator().sendMoveCreatureAndSetZ(creatureId, diffX, diffY, creature
                      .getPositionZ(), 
                      (int)(creature.getStatus().getRotation() * 256.0F / 360.0F));
                  }
                }
                else
                {
                  cmove.timestamp = System.currentTimeMillis();
                  cmove.rotation = ((int)(creature.getStatus().getRotation() * 256.0F / 360.0F));
                }
              }
            }
            else {
              this.watcher.creatureMoved(creature, diffX, diffY, diffZ);
            }
          }
          if ((diffTileX != 0) || (diffTileY != 0))
          {
            checkIfAttack(creature, creatureId);
            if (creature.getVehicle() != -10L) {
              try
              {
                Item itemVehicle = Items.getItem(creature.getVehicle());
                Vehicle vehicle = Vehicles.getVehicle(itemVehicle);
                sentMultiples = vehicle.getSeats();tosendx = sentMultiples.length;
                for (tosendy = 0; tosendy < tosendx; tosendy++)
                {
                  Seat seat = sentMultiples[tosendy];
                  
                  PlayerInfo oInfo = PlayerInfoFactory.getPlayerInfoWithWurmId(seat.getOccupant());
                  if (oInfo != null) {
                    try
                    {
                      Player oPlayer = Players.getInstance().getPlayer(oInfo.wurmId);
                      if (oPlayer.hasLink()) {
                        checkIfAttack(oPlayer, oPlayer.getWurmId());
                      }
                    }
                    catch (NoSuchPlayerException localNoSuchPlayerException) {}
                  }
                }
              }
              catch (NoSuchItemException localNoSuchItemException) {}
            }
          }
        }
      }
      else {
        removeCreature(creature);
      }
    }
    else
    {
      if ((this.watcher.getPower() > 2) && (this.watcher.loggerCreature1 != -10L)) {
        this.watcher.getCommunicator().sendAck(this.watcher.getPosX(), this.watcher.getPosY());
      }
      if ((this.watcher.isPlayer()) && (
        (diffTileX != 0) || (diffTileY != 0))) {
        getStructuresWithinDistance(5);
      }
    }
    return false;
  }
  
  public void clearCreatureMove(Creature creature, CreatureMove cmove)
  {
    cmove.timestamp = 0L;
    cmove.diffX = 0;
    cmove.diffY = 0;
    cmove.diffZ = 0;
  }
  
  public void clearMovementForCreature(long creatureId)
  {
    CreatureMove cmove = (CreatureMove)this.creatures.get(Long.valueOf(creatureId));
    if (cmove != null)
    {
      cmove.timestamp = 0L;
      cmove.diffX = 0;
      cmove.diffY = 0;
      cmove.diffZ = 0;
    }
  }
  
  public void linkVisionArea()
  {
    checkNewZone();
    if (this.size > 0) {
      for (int x = 0; x < this.watchedZones.length; x++) {
        this.watchedZones[x].linkTo(this, this.startX, this.startY, this.endX, this.endY);
      }
    } else {
      logger.log(Level.WARNING, "Size is 0 for creature " + this.watcher.getName());
    }
  }
  
  private void checkNewZone()
  {
    if (this.size <= 0) {
      return;
    }
    Zone[] checkedZones = Zones.getZonesCoveredBy(this);
    LinkedList<Zone> newZones = new LinkedList(Arrays.asList(checkedZones));
    if (this.watchedZones == null) {
      this.watchedZones = new Zone[0];
    }
    LinkedList<Zone> oldZones = new LinkedList(Arrays.asList(this.watchedZones));
    for (ListIterator<Zone> it = newZones.listIterator(); it.hasNext();)
    {
      newZ = (Zone)it.next();
      if (logger.isLoggable(Level.FINEST)) {
        logger.finest("new zone is " + newZ.getStartX() + "," + newZ.getEndX() + "," + newZ.getStartY() + "," + newZ
          .getEndY());
      }
      for (it2 = oldZones.listIterator(); it2.hasNext();)
      {
        Zone oldZ = (Zone)it2.next();
        if (logger.isLoggable(Level.FINEST)) {
          logger.finest("old zone is " + oldZ.getStartX() + "," + oldZ.getEndX() + "," + oldZ.getStartY() + "," + oldZ
            .getEndY());
        }
        if (newZ.equals(oldZ))
        {
          it.remove();
          it2.remove();
        }
      }
    }
    Zone newZ;
    ListIterator<Zone> it2;
    for (Iterator<Zone> it = newZones.iterator(); it.hasNext();)
    {
      Zone toAdd = (Zone)it.next();
      if (logger.isLoggable(Level.FINEST)) {
        logger.finest("Adding zone " + getId() + " as watcher to " + toAdd.getId());
      }
      try
      {
        toAdd.addWatcher(this.id);
      }
      catch (NoSuchZoneException nze)
      {
        logger.log(Level.INFO, nze.getMessage(), nze);
      }
    }
    for (Iterator<Zone> it = oldZones.iterator(); it.hasNext();)
    {
      Zone toRemove = (Zone)it.next();
      try
      {
        if (logger.isLoggable(Level.FINEST)) {
          logger.finest("Removing zone " + getId() + " as watcher to " + toRemove.getId());
        }
        toRemove.removeWatcher(this);
      }
      catch (NoSuchZoneException sex)
      {
        logger.log(Level.WARNING, "Zone with id does not exist!", sex);
      }
    }
    this.watchedZones = checkedZones;
  }
  
  void deleteCreature(Creature creature, boolean removeAsTarget)
    throws NoSuchCreatureException, NoSuchPlayerException
  {
    if (this.watcher == null)
    {
      logger.log(Level.WARNING, "Watcher is null when linking: " + creature.getName(), new Exception());
      return;
    }
    if (removeAsTarget)
    {
      boolean removeTarget = true;
      if (creature.isTeleporting()) {
        removeTarget = (Math.abs(this.watcher.getPosX() - creature.getTeleportX()) > 20.0F) || (Math.abs(this.watcher.getPosY() - creature
          .getTeleportY()) > 20.0F);
      } else if (this.watcher.isTeleporting()) {
        removeTarget = (Math.abs(creature.getPosX() - this.watcher.getTeleportX()) > 20.0F) || (Math.abs(creature.getPosY() - this.watcher
          .getTeleportY()) > 20.0F);
      }
      if (creature.isDead()) {
        removeTarget = true;
      }
      if (this.watcher.isDead()) {
        removeTarget = true;
      }
      if (removeTarget)
      {
        if (creature.getWurmId() == this.watcher.target) {
          if ((this.watcher.getVisionArea() == null) || (this.watcher.getVisionArea().getSurface() == null) || 
            (!this.watcher.getVisionArea().getSurface().containsCreature(creature))) {
            this.watcher.setTarget(-10L, true);
          }
        }
        if (creature.target == this.watcher.getWurmId()) {
          if ((creature.getVisionArea() == null) || (creature.getVisionArea().getSurface() == null) || 
            (!creature.getVisionArea().getSurface().containsCreature(this.watcher))) {
            creature.setTarget(-10L, true);
          }
        }
      }
    }
    if (this.creatures != null) {
      if (this.creatures.keySet().contains(new Long(creature.getWurmId())))
      {
        this.creatures.remove(new Long(creature.getWurmId()));
        if (removeAsTarget) {
          checkIfEnemyIsPresent(false);
        }
        if (this.watcher.hasLink())
        {
          if ((this.watcher != null) && (!this.watcher.equals(creature))) {
            this.watcher.getCommunicator().sendDeleteCreature(creature.getWurmId());
          }
          if (((creature instanceof Player)) || (creature.isNpc())) {
            this.watcher.getCommunicator().sendRemoveLocal(creature.getName());
          }
        }
        if (creature.getVehicle() != -10L) {
          if (WurmId.getType(creature.getVehicle()) == 2)
          {
            Vehicle vehic = Vehicles.getVehicleForId(creature.getVehicle());
            if (vehic != null)
            {
              boolean shouldRemove = true;
              Seat[] seats = vehic.getSeats();
              for (int x = 0; x < seats.length; x++) {
                if (seats[x].isOccupied()) {
                  if (this.creatures.containsKey(Long.valueOf(seats[x].occupant)))
                  {
                    shouldRemove = false;
                    break;
                  }
                }
              }
              if (shouldRemove) {
                try
                {
                  Item vc = Items.getItem(creature.getVehicle());
                  VolaTile tile = Zones.getOrCreateTile(vc.getTileX(), vc.getTileY(), vc.isOnSurface());
                  if (!isVisible(vc, tile)) {
                    if (vc.isMovingItem())
                    {
                      this.watcher.getCommunicator().sendDeleteMovingItem(vc.getWurmId());
                    }
                    else
                    {
                      if (vc.isWarTarget()) {
                        this.watcher.getCommunicator().sendRemoveEffect(vc.getWurmId());
                      }
                      this.watcher.getCommunicator().sendRemoveItem(vc);
                    }
                  }
                }
                catch (NoSuchItemException localNoSuchItemException) {}
              }
            }
          }
        }
      }
    }
  }
  
  public final void pollVisibleVehicles()
  {
    Iterator<Item> it;
    if (this.items != null) {
      for (it = this.items.iterator(); it.hasNext();)
      {
        Item i = (Item)it.next();
        if (i.isVehicle()) {
          if (i.deleted)
          {
            it.remove();
            sendRemoveItem(i);
          }
          else
          {
            VolaTile t = Zones.getTileOrNull(i.getTileX(), i.getTileY(), i.isOnSurface());
            if (!isVisible(i, t))
            {
              it.remove();
              sendRemoveItem(i);
            }
          }
        }
      }
    }
  }
  
  public boolean isVisible(Item item, VolaTile tile)
  {
    if (item.getTemplateId() == 344) {
      return this.watcher.getPower() > 0;
    }
    if (tile == null) {
      return false;
    }
    int distancex = Math.abs(tile.getTileX() - this.centerx);
    int distancey = Math.abs(tile.getTileY() - this.centery);
    int distance = Math.max(distancex, distancey);
    if (item.isVehicle())
    {
      Vehicle vehic = Vehicles.getVehicleForId(item.getWurmId());
      if (vehic != null)
      {
        Seat[] seats = vehic.getSeats();
        for (int x = 0; x < seats.length; x++) {
          if (seats[x].isOccupied()) {
            if (this.creatures.containsKey(Long.valueOf(seats[x].occupant))) {
              return true;
            }
          }
        }
        if (this.watcher.isPlayer()) {
          return Math.max(Math.abs(this.centerx - item.getTileX()), Math.abs(this.centery - item.getTileY())) <= this.size;
        }
      }
    }
    else if (this.watcher.isPlayer())
    {
      if (item.getSizeZ() >= 500) {
        return true;
      }
    }
    if (item.isLight()) {
      return true;
    }
    if (distance > this.size) {
      return false;
    }
    int isize = item.getSizeZ();
    int mod = 3;
    if (isize >= 300) {
      mod = 128;
    } else if (isize >= 200) {
      mod = 64;
    } else if (isize >= 100) {
      mod = 32;
    } else if (isize >= 50) {
      mod = 16;
    } else if (isize >= 10) {
      mod = 8;
    }
    if (item.isBrazier()) {
      return distance <= Math.max(mod, 16);
    }
    if (item.isCarpet()) {
      mod = distance <= Math.max(mod, 10) ? Math.max(mod, 10) : mod;
    }
    Structure itemStructure = tile.getStructure();
    if ((itemStructure != null) && (itemStructure.isTypeHouse()))
    {
      if (this.watcher.isPlayer()) {
        if ((this.nearbyStructureList != null) && (this.nearbyStructureList.contains(itemStructure))) {
          return distance <= mod;
        }
      }
      if (distance > 15) {
        return false;
      }
    }
    if (logger.isLoggable(Level.FINEST)) {
      logger.finest(item.getName() + " distance=" + distance + ", size=" + item.getSizeZ() / 10);
    }
    return distance <= mod;
  }
  
  private final ArrayList<Structure> getStructuresWithinDistance(int tileDistance)
  {
    if (this.nearbyStructureList == null) {
      this.nearbyStructureList = new ArrayList();
    }
    this.nearbyStructureList.clear();
    for (int i = this.watcher.getTileX() - tileDistance; i < this.watcher.getTileX() + tileDistance; i++) {
      for (int j = this.watcher.getTileY() - tileDistance; j < this.watcher.getTileY() + tileDistance; j++)
      {
        VolaTile tile = Zones.getTileOrNull(Zones.safeTileX(i), Zones.safeTileY(j), this.watcher.isOnSurface());
        if ((tile != null) && (tile.getStructure() != null) && (tile.getStructure().isTypeHouse()) && 
          (!this.nearbyStructureList.contains(tile.getStructure()))) {
          this.nearbyStructureList.add(tile.getStructure());
        }
      }
    }
    return this.nearbyStructureList;
  }
  
  private final Structure getStructureAtWatcherPosition()
  {
    try
    {
      Zone zone = Zones.getZone(this.watcher.getTileX(), this.watcher.getTileY(), this.watcher.isOnSurface());
      
      VolaTile tile = zone.getOrCreateTile(this.watcher.getTileX(), this.watcher.getTileY());
      
      return tile.getStructure();
    }
    catch (NoSuchZoneException e)
    {
      logger.log(Level.WARNING, "Unable to find the zone at the watchers tile position.", e);
    }
    return null;
  }
  
  void sendMoveMovingItem(long aId, byte x, byte y, int rot)
  {
    if (this.watcher.hasLink()) {
      this.watcher.getCommunicator().sendMoveMovingItem(aId, x, y, rot);
    }
  }
  
  void sendMoveMovingItemAndSetZ(long aId, byte x, byte y, float z, int rot)
  {
    if (this.watcher.hasLink()) {
      this.watcher.getCommunicator().sendMoveMovingItemAndSetZ(aId, x, y, z, rot);
    }
  }
  
  boolean addItem(Item item, VolaTile tile, boolean onGroundLevel)
  {
    return addItem(item, tile, -10L, onGroundLevel);
  }
  
  boolean addItem(Item item, VolaTile tile, long creatureId, boolean onGroundLevel)
  {
    if (this.items == null) {
      this.items = new HashSet();
    }
    Item i;
    if ((item.isMovingItem()) || (covers(item.getTileX(), item.getTileY())))
    {
      if (!this.items.contains(item))
      {
        this.items.add(item);
        if (this.watcher.hasLink())
        {
          Vehicle vehic;
          Seat[] hitched;
          int x;
          boolean normalContainer;
          if (item.isMovingItem())
          {
            byte newlayer = item.isOnSurface() ? 0 : -1;
            if (item.newLayer != Byte.MIN_VALUE) {
              newlayer = item.newLayer;
            }
            this.watcher.getCommunicator().sendNewMovingItem(item.getWurmId(), item.getName(), item.getModelName(), item
              .getPosX(), item.getPosY(), item.getPosZ(), item.onBridge(), item.getRotation(), newlayer, item
              .getFloorLevel() <= 0, 
              (item.isFloating()) && (item.getCurrentQualityLevel() >= 10.0F), true, item.getMaterial(), item
              .getRarity());
            
            vehic = Vehicles.getVehicleForId(item.getWurmId());
            if (vehic != null)
            {
              Seat[] seats = vehic.getSeats();
              for (int x = 0; x < seats.length; x++) {
                if ((seats[x].isOccupied()) && (this.watcher.getWurmId() != seats[x].occupant))
                {
                  Creature occ = Server.getInstance().getCreatureOrNull(seats[x].occupant);
                  if ((occ != null) && (!occ.equals(this.watcher))) {
                    if (occ.isVisibleTo(this.watcher))
                    {
                      if (((!Servers.localServer.PVPSERVER) || (this.watcher.isPaying())) && (occ.isPlayer())) {
                        this.watcher.getCommunicator().sendAddLocal(occ.getName(), seats[x].occupant);
                      }
                      if (!this.creatures.containsKey(Long.valueOf(seats[x].occupant))) {
                        if (this.watcher.isPlayer()) {
                          this.creatures.put(Long.valueOf(creatureId), new CreatureMove());
                        } else {
                          this.creatures.put(Long.valueOf(creatureId), null);
                        }
                      }
                      sendAttachCreature(seats[x].occupant, item.getWurmId(), seats[x].offx, seats[x].offy, seats[x].offz, x);
                    }
                  }
                }
              }
              hitched = vehic.hitched;
              for (x = 0; x < hitched.length; x++) {
                if ((hitched[x].isOccupied()) && (this.creatures.containsKey(Long.valueOf(hitched[x].occupant))) && 
                  (this.watcher.getWurmId() != hitched[x].occupant))
                {
                  sendAttachCreature(hitched[x].occupant, item.getWurmId(), hitched[x].offx, hitched[x].offy, hitched[x].offz, x);
                }
                else if (this.watcher.getWurmId() == hitched[x].occupant)
                {
                  logger.log(Level.WARNING, "This should be unused code.");
                  sendAttachCreature(-1L, item.getWurmId(), hitched[x].offx, hitched[x].offy, hitched[x].offz, x);
                }
              }
            }
          }
          else
          {
            this.watcher.getCommunicator().sendItem(item, creatureId, onGroundLevel);
            if (item.isWarTarget())
            {
              this.watcher.getCommunicator().sendAddEffect(item.getWurmId(), (short)24, item
                .getPosX(), item.getPosY(), item.getData1(), (byte)(item.isOnSurface() ? 0 : -1));
              this.watcher.getCommunicator().sendTargetStatus(item.getWurmId(), (byte)item.getData2(), item
                .getData1());
            }
            if (item.getTemplate().hasViewableSubItems()) {
              if (item.getItemCount() > 0)
              {
                normalContainer = item.getTemplate().isContainerWithSubItems();
                for (vehic = item.getItems().iterator(); vehic.hasNext();)
                {
                  i = (Item)vehic.next();
                  if ((!normalContainer) || (i.isPlacedOnParent()))
                  {
                    this.watcher.getCommunicator().sendItem(i, -10L, false);
                    if ((i.isLight()) && (i.isOnFire())) {
                      addLightSource(i);
                    }
                    if (i.getEffects().length > 0) {
                      for (Effect e : i.getEffects()) {
                        addEffect(e, false);
                      }
                    }
                    if (i.getColor() != -1) {
                      sendRepaint(i.getWurmId(), (byte)WurmColor.getColorRed(i.getColor()), 
                        (byte)WurmColor.getColorGreen(i.getColor()), (byte)WurmColor.getColorBlue(i.getColor()), (byte)-1, (byte)0);
                    }
                    if (i.getColor2() != -1) {
                      sendRepaint(i.getWurmId(), (byte)WurmColor.getColorRed(i.getColor2()), 
                        (byte)WurmColor.getColorGreen(i.getColor2()), (byte)WurmColor.getColorBlue(i.getColor2()), (byte)-1, (byte)1);
                    }
                  }
                }
              }
            }
          }
          if (item.isLight()) {
            if (item.isOnFire())
            {
              addLightSource(item);
              if (item.getEffects().length > 0)
              {
                Effect[] effs = item.getEffects();
                for (int x = 0; x < effs.length; x++) {
                  addEffect(effs[x], false);
                }
              }
            }
          }
          if ((!item.isLight()) || (item.getTemplateId() == 1396))
          {
            if (item.getColor() != -1) {
              sendRepaint(item.getWurmId(), (byte)WurmColor.getColorRed(item.getColor()), 
                (byte)WurmColor.getColorGreen(item.getColor()), (byte)WurmColor.getColorBlue(item.getColor()), (byte)-1, (byte)0);
            }
            if ((item.supportsSecondryColor()) && (item.getColor2() != -1)) {
              sendRepaint(item.getWurmId(), (byte)WurmColor.getColorRed(item.getColor2()), 
                (byte)WurmColor.getColorGreen(item.getColor2()), (byte)WurmColor.getColorBlue(item.getColor2()), (byte)-1, (byte)1);
            }
          }
          if ((item.getExtra() != -1L) && ((item.getTemplateId() == 491) || (item.getTemplateId() == 490)))
          {
            Optional<Item> extraItem = Items.getItemOptional(item.getExtra());
            if (extraItem.isPresent()) {
              sendBoatAttachment(item.getWurmId(), ((Item)extraItem.get()).getTemplateId(), ((Item)extraItem.get()).getMaterial(), (byte)1, 
                ((Item)extraItem.get()).getAuxData());
            }
          }
        }
        if (item.isHugeAltar())
        {
          if (this.watcher.getMusicPlayer() != null) {
            if (this.watcher.getMusicPlayer().isItOkToPlaySong(true)) {
              if (item.getTemplateId() == 327) {
                this.watcher.getMusicPlayer().checkMUSIC_WHITELIGHT_SND();
              } else {
                this.watcher.getMusicPlayer().checkMUSIC_BLACKLIGHT_SND();
              }
            }
          }
        }
        else if (item.getTemplateId() == 518) {
          if (this.watcher.getMusicPlayer() != null) {
            if (this.watcher.getMusicPlayer().isItOkToPlaySong(true)) {
              this.watcher.getMusicPlayer().checkMUSIC_COLOSSUS_SND();
            }
          }
        }
      }
    }
    else
    {
      int tilex = item.getTileX();
      int tiley = item.getTileY();
      try
      {
        i = item.getParent();
      }
      catch (NoSuchItemException localNoSuchItemException) {}
      if ((item.getContainerSizeZ() < 500) && (!item.isVehicle()))
      {
        VolaTile vtile = Zones.getTileOrNull(tilex, tiley, this.isOnSurface);
        if (vtile != null)
        {
          if (!vtile.equals(tile)) {
            return false;
          }
          if (!covers(tile.getTileX(), tile.getTileY())) {
            vtile.removeWatcher(this);
          }
        }
        else
        {
          return false;
        }
      }
    }
    return true;
  }
  
  private void sendRemoveItem(Item item)
  {
    if (this.watcher.hasLink())
    {
      Seat[] seats;
      int x;
      if (item.isMovingItem())
      {
        this.watcher.getCommunicator().sendDeleteMovingItem(item.getWurmId());
        if (this.watcher.isPlayer())
        {
          Vehicle vehic = Vehicles.getVehicleForId(item.getWurmId());
          if (vehic != null)
          {
            seats = vehic.getSeats();
            for (x = 0; x < seats.length; x++) {
              if (seats[x].isOccupied()) {
                try
                {
                  Creature occ = Server.getInstance().getCreature(seats[x].occupant);
                  if ((occ != null) && (!occ.equals(this.watcher)))
                  {
                    if (this.creatures != null) {
                      this.creatures.remove(Long.valueOf(seats[x].occupant));
                    }
                    this.watcher.getCommunicator().sendRemoveLocal(occ.getName());
                  }
                }
                catch (NoSuchCreatureException nsc)
                {
                  logger.log(Level.WARNING, nsc.getMessage(), nsc);
                }
                catch (NoSuchPlayerException localNoSuchPlayerException) {}
              }
            }
          }
        }
      }
      else
      {
        if (item.isWarTarget()) {
          this.watcher.getCommunicator().sendRemoveEffect(item.getWurmId());
        }
        if (item.getTemplate().hasViewableSubItems()) {
          if (item.getItemCount() > 0)
          {
            boolean normalContainer = item.getTemplate().isContainerWithSubItems();
            seats = item.getAllItems(false);x = seats.length;
            for (localNoSuchPlayerException = 0; localNoSuchPlayerException < x; localNoSuchPlayerException++)
            {
              Item i = seats[localNoSuchPlayerException];
              if ((!normalContainer) || (i.isPlacedOnParent())) {
                this.watcher.getCommunicator().sendRemoveItem(i);
              }
            }
          }
        }
        this.watcher.getCommunicator().sendRemoveItem(item);
      }
    }
  }
  
  void removeItem(Item item)
  {
    if ((this.items != null) && (this.items.contains(item)))
    {
      this.items.remove(item);
      sendRemoveItem(item);
    }
  }
  
  void removeStructure(Structure structure)
  {
    if (logger.isLoggable(Level.FINEST)) {
      logger.finest(this.watcher.getName() + " removing structure " + structure);
    }
    if ((this.structures != null) && (this.structures.contains(structure)))
    {
      boolean stillHere = false;
      VolaTile[] tiles = structure.getStructureTiles();
      for (int x = 0; x < tiles.length; x++) {
        if (covers(tiles[x].getTileX(), tiles[x].getTileY()))
        {
          stillHere = true;
          break;
        }
      }
      if (!stillHere)
      {
        this.structures.remove(structure);
        this.watcher.getCommunicator().sendRemoveStructure(structure.getWurmId());
      }
    }
  }
  
  void deleteStructure(Structure structure)
  {
    if (this.structures != null) {
      if (this.structures.contains(structure))
      {
        this.structures.remove(structure);
        this.watcher.getCommunicator().sendRemoveStructure(structure.getWurmId());
      }
    }
  }
  
  private void removeAllStructures()
  {
    Iterator<Structure> it;
    if (this.structures != null) {
      for (it = this.structures.iterator(); it.hasNext();)
      {
        Structure structure = (Structure)it.next();
        this.watcher.getCommunicator().sendRemoveStructure(structure.getWurmId());
      }
    }
    this.structures = null;
  }
  
  void sendStructureWalls(Structure structure)
  {
    Wall[] wallArr = structure.getWalls();
    for (int x = 0; x < wallArr.length; x++) {
      updateWall(structure.getWurmId(), wallArr[x]);
    }
  }
  
  void addStructure(Structure structure)
  {
    if (logger.isLoggable(Level.FINEST)) {
      logger.finest(this.watcher.getName() + " adding structure " + structure);
    }
    if (this.structures == null) {
      this.structures = new HashSet();
    }
    if (!this.structures.contains(structure))
    {
      this.structures.add(structure);
      this.watcher.getCommunicator().sendAddStructure(structure.getName(), (short)structure.getCenterX(), 
        (short)structure.getCenterY(), structure.getWurmId(), structure.getStructureType(), structure
        .getLayer());
      if (structure.isTypeHouse())
      {
        this.watcher.getCommunicator().sendMultipleBuildMarkers(structure.getWurmId(), structure.getStructureTiles(), structure
          .getLayer());
        sendStructureWalls(structure);
      }
      Floor[] floorArr = structure.getFloors();
      if (floorArr != null) {
        for (int x = 0; x < floorArr.length; x++) {
          updateFloor(structure.getWurmId(), floorArr[x]);
        }
      }
      BridgePart[] bridgePartArr = structure.getBridgeParts();
      if (bridgePartArr != null) {
        for (int x = 0; x < bridgePartArr.length; x++) {
          updateBridgePart(structure.getWurmId(), bridgePartArr[x]);
        }
      }
    }
  }
  
  void addBuildMarker(Structure structure, int tilex, int tiley)
  {
    if (this.structures == null) {
      this.structures = new HashSet();
    }
    if (!this.structures.contains(structure)) {
      addStructure(structure);
    } else {
      this.watcher.getCommunicator().sendSingleBuildMarker(structure.getWurmId(), tilex, tiley, getLayer());
    }
  }
  
  void removeBuildMarker(Structure structure, int tilex, int tiley)
  {
    if ((this.structures != null) && (this.structures.contains(structure)))
    {
      boolean stillHere = false;
      VolaTile[] tiles = structure.getStructureTiles();
      for (int x = 0; x < tiles.length; x++) {
        if (covers(tiles[x].getTileX(), tiles[x].getTileY()))
        {
          stillHere = true;
          break;
        }
      }
      if (stillHere)
      {
        if (logger.isLoggable(Level.FINEST)) {
          logger.finest(this.watcher.getName() + " removing build marker for structure " + structure.getWurmId());
        }
        this.watcher.getCommunicator().sendSingleBuildMarker(structure.getWurmId(), tilex, tiley, getLayer());
      }
      else
      {
        removeStructure(structure);
      }
    }
    else
    {
      logger.log(Level.INFO, "Hmm tried to remove buildmarker from a zone that didn't contain it.");
    }
  }
  
  void finalizeBuildPlan(long oldStructureId, long newStructureId)
  {
    if (this.finalizedBuildings == null) {
      this.finalizedBuildings = new HashSet();
    }
    if (!this.finalizedBuildings.contains(new Long(newStructureId)))
    {
      try
      {
        Structure structure = Structures.getStructure(newStructureId);
        if (structure.isTypeHouse()) {
          this.watcher.getCommunicator().sendRemoveStructure(oldStructureId);
        }
        this.watcher.getCommunicator().sendAddStructure(structure.getName(), (short)structure.getCenterX(), 
          (short)structure.getCenterY(), structure.getWurmId(), structure.getStructureType(), structure
          .getLayer());
        if (structure.isTypeHouse()) {
          this.watcher.getCommunicator().sendMultipleBuildMarkers(structure.getWurmId(), structure.getStructureTiles(), structure
            .getLayer());
        }
        Wall[] wallArr = structure.getWalls();
        for (int x = 0; x < wallArr.length; x++) {
          if (wallArr[x].getType() != StructureTypeEnum.PLAN)
          {
            this.watcher.getCommunicator().sendAddWall(structure.getWurmId(), wallArr[x]);
            if (wallArr[x].getDamage() >= 60.0F) {
              this.watcher.getCommunicator().sendWallDamageState(structure.getWurmId(), wallArr[x].getId(), 
                (byte)(int)wallArr[x].getDamage());
            }
          }
        }
      }
      catch (NoSuchStructureException nss)
      {
        logger.log(Level.WARNING, "The new building doesn't exist.", nss);
      }
      this.finalizedBuildings.add(new Long(newStructureId));
    }
  }
  
  void addDoor(Door door)
  {
    if (this.doors == null) {
      this.doors = new HashSet();
    }
    if (!this.doors.contains(door))
    {
      this.doors.add(door);
      if (door.isOpen()) {
        if ((door instanceof FenceGate)) {
          openFence(((FenceGate)door).getFence(), false, true);
        } else {
          openDoor(door);
        }
      }
    }
  }
  
  void removeDoor(Door door)
  {
    if (this.doors != null) {
      this.doors.remove(door);
    }
  }
  
  public void openDoor(Door door)
  {
    this.watcher.getCommunicator().sendOpenDoor(door);
  }
  
  public void closeDoor(Door door)
  {
    this.watcher.getCommunicator().sendCloseDoor(door);
  }
  
  public void openFence(Fence fence, boolean passable, boolean changedPassable)
  {
    this.watcher.getCommunicator().sendOpenFence(fence, passable, changedPassable);
  }
  
  public void closeFence(Fence fence, boolean passable, boolean changedPassable)
  {
    this.watcher.getCommunicator().sendCloseFence(fence, passable, changedPassable);
  }
  
  public void openMineDoor(MineDoorPermission door)
  {
    this.watcher.getCommunicator().sendOpenMineDoor(door);
  }
  
  public void closeMineDoor(MineDoorPermission door)
  {
    this.watcher.getCommunicator().sendCloseMineDoor(door);
  }
  
  void updateFloor(long structureId, Floor floor)
  {
    this.watcher.getCommunicator().sendAddFloor(structureId, floor);
    if (floor.getDamage() >= 60.0F) {
      this.watcher.getCommunicator().sendWallDamageState(floor.getStructureId(), floor.getId(), (byte)(int)floor.getDamage());
    }
  }
  
  void updateBridgePart(long structureId, BridgePart bridgePart)
  {
    this.watcher.getCommunicator().sendAddBridgePart(structureId, bridgePart);
    if (bridgePart.getDamage() >= 60.0F) {
      this.watcher.getCommunicator().sendWallDamageState(bridgePart.getStructureId(), bridgePart.getId(), (byte)(int)bridgePart.getDamage());
    }
  }
  
  void updateWall(long structureId, Wall wall)
  {
    this.watcher.getCommunicator().sendAddWall(structureId, wall);
    if (wall.getDamage() >= 60.0F) {
      this.watcher.getCommunicator().sendWallDamageState(wall.getStructureId(), wall.getId(), (byte)(int)wall.getDamage());
    }
  }
  
  void removeWall(long structureId, Wall wall)
  {
    this.watcher.getCommunicator().sendRemoveWall(structureId, wall);
  }
  
  void removeFloor(long structureId, Floor floor)
  {
    this.watcher.getCommunicator().sendRemoveFloor(structureId, floor);
  }
  
  void removeBridgePart(long structureId, BridgePart bridgePart)
  {
    this.watcher.getCommunicator().sendRemoveBridgePart(structureId, bridgePart);
  }
  
  void changeStructureName(long structureId, String newName)
  {
    this.watcher.getCommunicator().sendChangeStructureName(structureId, newName);
  }
  
  void playSound(Sound sound)
  {
    this.watcher.getCommunicator().sendSound(sound);
  }
  
  public static boolean isCreatureTurnedTowardsTarget(Creature target, Creature performer)
  {
    return isCreatureTurnedTowardsTarget(target, performer, 180.0F, false);
  }
  
  public static boolean isCreatureShieldedVersusTarget(Creature target, Creature performer)
  {
    if (performer.isWithinDistanceTo(target, 1.5F))
    {
      if ((Servers.localServer.testServer) && (target.isPlayer()) && (performer.isPlayer())) {
        target.getCommunicator().sendNormalServerMessage(performer.getName() + " is so close he auto blocks you.");
      }
      return true;
    }
    return isCreatureTurnedTowardsTarget(target, performer, 135.0F, true);
  }
  
  public static boolean isCreatureTurnedTowardsItem(Item target, Creature performer, float angle)
  {
    double newrot = Math.atan2(target.getPosY() - (int)performer.getStatus().getPositionY(), target.getPosX() - 
      (int)performer.getStatus().getPositionX());
    
    float attAngle = (float)(newrot * 57.29577951308232D) + 90.0F;
    attAngle = Creature.normalizeAngle(attAngle);
    float prot = Creature.normalizeAngle(performer.getStatus().getRotation() - attAngle);
    if ((prot > angle / 2.0F) && (prot < 360.0F - angle / 2.0F)) {
      return false;
    }
    return true;
  }
  
  public static boolean isItemTurnedTowardsCreature(Creature target, Item performer, float angle)
  {
    double newrot = Math.atan2(target.getPosY() - (int)performer.getPosY(), target
      .getPosX() - (int)performer.getPosX());
    
    float attAngle = (float)(newrot * 57.29577951308232D) - 90.0F;
    attAngle = Creature.normalizeAngle(attAngle);
    float prot = Creature.normalizeAngle(performer.getRotation() - attAngle);
    if ((prot > angle / 2.0F) && (prot < 360.0F - angle / 2.0F)) {
      return false;
    }
    return true;
  }
  
  public static boolean isCreatureTurnedTowardsTarget(Creature target, Creature performer, float angle, boolean leftWinged)
  {
    boolean log = (leftWinged) && (Servers.localServer.testServer) && (target.isPlayer()) && (performer.isPlayer());
    double newrot = Math.atan2(target.getPosY() - (int)performer.getStatus().getPositionY(), target.getPosX() - 
      (int)performer.getStatus().getPositionX());
    
    float attAngle = (float)(newrot * 57.29577951308232D) + 90.0F;
    attAngle = Creature.normalizeAngle(attAngle);
    float crot = Creature.normalizeAngle(performer.getStatus().getRotation());
    float prot = Creature.normalizeAngle(attAngle - crot);
    
    float rightAngle = angle / 2.0F;
    float leftAngle = 360.0F - angle / 2.0F;
    if (leftWinged)
    {
      leftAngle -= 45.0F;
      rightAngle -= 45.0F;
    }
    leftAngle = Creature.normalizeAngle(leftAngle);
    rightAngle = Creature.normalizeAngle(rightAngle);
    if (log) {
      target.getCommunicator().sendNormalServerMessage(attAngle + ", " + crot + ", prot=" + prot);
    }
    if ((prot > rightAngle) && (prot < leftAngle))
    {
      if (log) {
        target.getCommunicator().sendNormalServerMessage("1.5 " + performer
          .getName() + " will not block you. Angle to me= " + attAngle + ", creature angle=" + crot + ", difference=" + prot + ". Max left=" + leftAngle + ", right=" + rightAngle);
      }
      return false;
    }
    if (log) {
      target.getCommunicator().sendNormalServerMessage("1.5 " + performer
        .getName() + " will block you. Angle to me= " + attAngle + ", creature angle=" + crot + ", difference=" + prot + ". Max left=" + leftAngle + ", right=" + rightAngle);
    }
    return true;
  }
  
  private void addLightSource(Item lightSource)
  {
    int colorToUse = lightSource.getColor();
    if (lightSource.getTemplateId() == 1396) {
      colorToUse = lightSource.getColor2();
    }
    if (colorToUse != -1)
    {
      int lightStrength = Math.max(WurmColor.getColorRed(colorToUse), WurmColor.getColorGreen(colorToUse));
      lightStrength = Math.max(1, Math.max(lightStrength, WurmColor.getColorBlue(colorToUse)));
      byte r = (byte)(WurmColor.getColorRed(colorToUse) * 128 / lightStrength);
      byte g = (byte)(WurmColor.getColorGreen(colorToUse) * 128 / lightStrength);
      byte b = (byte)(WurmColor.getColorBlue(colorToUse) * 128 / lightStrength);
      
      sendAttachItemEffect(lightSource.getWurmId(), (byte)4, r, g, b, lightSource.getRadius());
    }
    else if (lightSource.isLightBright())
    {
      int lightStrength = (int)(80.0F + lightSource.getCurrentQualityLevel() / 100.0F * 40.0F);
      
      sendAttachItemEffect(lightSource.getWurmId(), (byte)4, 
        Item.getRLight(lightStrength), Item.getGLight(lightStrength), Item.getBLight(lightStrength), lightSource
        .getRadius());
    }
    else
    {
      sendAttachItemEffect(lightSource.getWurmId(), (byte)4, Item.getRLight(80), 
        Item.getGLight(80), Item.getBLight(80), lightSource.getRadius());
    }
  }
  
  private void addLightSource(Creature creature, Item lightSource)
  {
    if (lightSource.getColor() != -1)
    {
      int lightStrength = Math.max(WurmColor.getColorRed(lightSource.getColor()), WurmColor.getColorGreen(lightSource.getColor()));
      lightStrength = Math.max(1, Math.max(lightStrength, WurmColor.getColorBlue(lightSource.getColor())));
      byte r = (byte)(WurmColor.getColorRed(lightSource.getColor()) * 128 / lightStrength);
      byte g = (byte)(WurmColor.getColorGreen(lightSource.getColor()) * 128 / lightStrength);
      byte b = (byte)(WurmColor.getColorBlue(lightSource.getColor()) * 128 / lightStrength);
      
      sendAttachCreatureEffect(creature, (byte)0, r, g, b, lightSource.getRadius());
    }
    else if (lightSource.isLightBright())
    {
      int lightStrength = (int)(80.0F + lightSource.getCurrentQualityLevel() / 100.0F * 40.0F);
      sendAttachCreatureEffect(creature, (byte)0, Item.getRLight(lightStrength), 
        Item.getGLight(lightStrength), Item.getBLight(lightStrength), lightSource.getRadius());
    }
    else
    {
      sendAttachCreatureEffect(creature, (byte)0, Item.getRLight(80), Item.getGLight(80), 
        Item.getBLight(80), lightSource.getRadius());
    }
  }
  
  public void sendAttachCreatureEffect(Creature creature, byte effectType, byte data0, byte data1, byte data2, byte radius)
  {
    if (creature == null) {
      this.watcher.getCommunicator().sendAttachEffect(-1L, effectType, data0, data1, data2, radius);
    } else if (this.creatures.containsKey(Long.valueOf(creature.getWurmId()))) {
      this.watcher.getCommunicator().sendAttachEffect(creature.getWurmId(), effectType, data0, data1, data2, radius);
    }
  }
  
  void sendAttachItemEffect(long targetId, byte effectType, byte data0, byte data1, byte data2, byte radius)
  {
    this.watcher.getCommunicator().sendAttachEffect(targetId, effectType, data0, data1, data2, radius);
  }
  
  void sendRemoveEffect(long targetId, byte effectType)
  {
    if (WurmId.getType(targetId) == 2) {
      this.watcher.getCommunicator().sendRemoveEffect(targetId, effectType);
    } else if ((targetId == -1L) || (this.creatures.containsKey(Long.valueOf(targetId)))) {
      this.watcher.getCommunicator().sendRemoveEffect(targetId, effectType);
    }
  }
  
  void sendHorseWear(long creatureId, int itemId, byte material, byte slot, byte aux_data)
  {
    if (this.creatures.containsKey(Long.valueOf(creatureId))) {
      this.watcher.getCommunicator().sendHorseWear(creatureId, itemId, material, slot, aux_data);
    }
  }
  
  void sendRemoveHorseWear(long creatureId, int itemId, byte slot)
  {
    if (this.creatures.containsKey(Long.valueOf(creatureId))) {
      this.watcher.getCommunicator().sendRemoveHorseWear(creatureId, itemId, slot);
    }
  }
  
  void sendBoatAttachment(long itemId, int templateId, byte material, byte slot, byte aux)
  {
    this.watcher.getCommunicator().sendHorseWear(itemId, templateId, material, slot, aux);
  }
  
  void sendBoatDetachment(long itemId, int templateId, byte slot)
  {
    this.watcher.getCommunicator().sendRemoveHorseWear(itemId, templateId, slot);
  }
  
  void sendWearItem(long creatureId, int itemId, byte bodyPart, int colorRed, int colorGreen, int colorBlue, int secondaryColorRed, int secondaryColorGreen, int secondaryColorBlue, byte material, byte rarity)
  {
    if ((creatureId == -1L) || (this.creatures.containsKey(Long.valueOf(creatureId)))) {
      this.watcher.getCommunicator().sendWearItem(creatureId, itemId, bodyPart, colorRed, colorGreen, colorBlue, secondaryColorRed, secondaryColorGreen, secondaryColorBlue, material, rarity);
    }
  }
  
  void sendRemoveWearItem(long creatureId, byte bodyPart)
  {
    if ((creatureId == -1L) || (this.creatures.containsKey(Long.valueOf(creatureId)))) {
      this.watcher.getCommunicator().sendRemoveWearItem(creatureId, bodyPart);
    }
  }
  
  void sendWieldItem(long creatureId, byte slot, String modelname, byte rarity, int colorRed, int colorGreen, int colorBlue, int secondaryColorRed, int secondaryColorGreen, int secondaryColorBlue)
  {
    if ((creatureId == -1L) || (this.creatures.containsKey(Long.valueOf(creatureId)))) {
      this.watcher.getCommunicator().sendWieldItem(creatureId, slot, modelname, rarity, colorRed, colorGreen, colorBlue, secondaryColorRed, secondaryColorGreen, secondaryColorBlue);
    }
  }
  
  void sendUseItem(Creature creature, String modelname, byte rarity, int colorRed, int colorGreen, int colorBlue, int secondaryColorRed, int secondaryColorGreen, int secondaryColorBlue)
  {
    if (creature == null) {
      this.watcher.getCommunicator().sendUseItem(-1L, modelname, rarity, colorRed, colorGreen, colorBlue, secondaryColorRed, secondaryColorGreen, secondaryColorBlue);
    } else if ((!creature.isTeleporting()) && (this.creatures.containsKey(Long.valueOf(creature.getWurmId())))) {
      this.watcher.getCommunicator().sendUseItem(creature.getWurmId(), modelname, rarity, colorRed, colorGreen, colorBlue, secondaryColorRed, secondaryColorGreen, secondaryColorBlue);
    }
  }
  
  void sendStopUseItem(Creature creature)
  {
    if (creature == null) {
      this.watcher.getCommunicator().sendStopUseItem(-1L);
    } else if ((!creature.isTeleporting()) && (this.creatures.containsKey(Long.valueOf(creature.getWurmId())))) {
      this.watcher.getCommunicator().sendStopUseItem(creature.getWurmId());
    }
  }
  
  public void sendRepaint(long wurmid, byte red, byte green, byte blue, byte alpha, byte paintType)
  {
    this.watcher.getCommunicator().sendRepaint(wurmid, red, green, blue, alpha, paintType);
  }
  
  private void sendResizeCreature(long wurmid, byte xscaleMod, byte yscaleMod, byte zscaleMod)
  {
    this.watcher.getCommunicator().sendResize(wurmid, xscaleMod, yscaleMod, zscaleMod);
  }
  
  void sendAnimation(Creature creature, String animationName, boolean looping, long target)
  {
    if (creature == null)
    {
      if (target <= 0L) {
        this.watcher.getCommunicator().sendAnimation(-1L, animationName, looping, animationName.equals("die"));
      } else {
        this.watcher.getCommunicator().sendAnimation(-1L, animationName, looping, false, target);
      }
    }
    else if (this.creatures.containsKey(Long.valueOf(creature.getWurmId()))) {
      if (target <= 0L) {
        this.watcher.getCommunicator().sendAnimation(creature.getWurmId(), animationName, looping, animationName.equals("die"));
      } else {
        this.watcher.getCommunicator().sendAnimation(creature.getWurmId(), animationName, looping, false, target);
      }
    }
  }
  
  void sendStance(Creature creature, byte stance)
  {
    if (creature == null) {
      this.watcher.getCommunicator().sendStance(-1L, stance);
    } else if (this.creatures.containsKey(Long.valueOf(creature.getWurmId()))) {
      this.watcher.getCommunicator().sendStance(creature.getWurmId(), stance);
    }
  }
  
  void sendCreatureDamage(Creature creature, float currentDamage)
  {
    if ((creature != null) && (this.watcher != null) && (!creature.equals(this.watcher)) && 
      (this.creatures.containsKey(Long.valueOf(creature.getWurmId())))) {
      this.watcher.getCommunicator().setCreatureDamage(creature.getWurmId(), currentDamage);
    }
  }
  
  void sendFishingLine(Creature creature, float posX, float posY, byte floatType)
  {
    if ((creature != null) && (this.watcher != null) && (!creature.equals(this.watcher)) && 
      (this.creatures.containsKey(Long.valueOf(creature.getWurmId())))) {
      this.watcher.getCommunicator().sendFishCasted(creature.getWurmId(), posX, posY, floatType);
    }
  }
  
  void sendFishHooked(Creature creature, byte fishType, long fishId)
  {
    if ((creature != null) && (this.watcher != null) && (!creature.equals(this.watcher)) && 
      (this.creatures.containsKey(Long.valueOf(creature.getWurmId())))) {
      this.watcher.getCommunicator().sendFishBite(fishType, fishId, creature.getWurmId());
    }
  }
  
  void sendFishingStopped(Creature creature)
  {
    if ((creature != null) && (this.watcher != null) && (!creature.equals(this.watcher)) && 
      (this.creatures.containsKey(Long.valueOf(creature.getWurmId())))) {
      this.watcher.getCommunicator().sendFishSubCommand((byte)15, creature.getWurmId());
    }
  }
  
  void sendSpearStrike(Creature creature, float posX, float posY)
  {
    if ((creature != null) && (this.watcher != null) && (!creature.equals(this.watcher)) && 
      (this.creatures.containsKey(Long.valueOf(creature.getWurmId())))) {
      this.watcher.getCommunicator().sendSpearStrike(creature.getWurmId(), posX, posY);
    }
  }
  
  void sendAttachCreature(long creatureId, long targetId, float offx, float offy, float offz, int seatId)
  {
    boolean send = true;
    if (targetId != -1L) {
      if ((WurmId.getType(targetId) == 1) || (WurmId.getType(targetId) == 0))
      {
        if (!this.creatures.containsKey(Long.valueOf(targetId))) {
          try
          {
            addCreature(targetId, true);
            send = false;
          }
          catch (NoSuchCreatureException localNoSuchCreatureException) {}catch (NoSuchPlayerException localNoSuchPlayerException) {}
        }
      }
      else if ((WurmId.getType(targetId) == 2) || 
        (WurmId.getType(targetId) == 19) || 
        (WurmId.getType(targetId) == 20)) {
        try
        {
          Item item = Items.getItem(targetId);
          if ((this.items == null) || (!this.items.contains(item))) {
            if (this.watcher.getVisionArea() != null)
            {
              if (isOnSurface())
              {
                if (!item.isOnSurface()) {
                  if ((this.watcher.getVisionArea().getUnderGround() == null) || 
                    (this.watcher.getVisionArea().getUnderGround().items == null) || 
                    (!this.watcher.getVisionArea().getUnderGround().items.contains(item)))
                  {
                    if (this.watcher.getVisionArea().getUnderGround() != null)
                    {
                      if (this.watcher.getVisionArea().getUnderGround().covers(item.getTileX(), item.getTileY())) {
                        this.watcher.getVisionArea().getUnderGround().addItem(item, null, true);
                      }
                    }
                    else {
                      addItem(item, null, true);
                    }
                    send = false;
                  }
                }
              }
              else if (item.isOnSurface()) {
                if ((this.watcher.getVisionArea().getSurface() == null) || 
                  (this.watcher.getVisionArea().getSurface().items == null) || 
                  (!this.watcher.getVisionArea().getSurface().items.contains(item)))
                {
                  if (this.watcher.getVisionArea().getSurface() != null) {
                    this.watcher.getVisionArea().getSurface().addItem(item, null, true);
                  } else {
                    addItem(item, null, true);
                  }
                  send = false;
                }
              }
            }
            else
            {
              addItem(item, null, true);
              send = false;
            }
          }
        }
        catch (NoSuchItemException localNoSuchItemException) {}
      }
    }
    if (creatureId != -1L) {
      if ((WurmId.getType(creatureId) == 1) || (WurmId.getType(creatureId) == 0))
      {
        if ((this.watcher.getWurmId() != creatureId) && (!this.creatures.containsKey(Long.valueOf(creatureId)))) {
          if (targetId != -1L) {
            try
            {
              addCreature(creatureId, true);
              send = false;
            }
            catch (NoSuchCreatureException localNoSuchCreatureException1) {}catch (NoSuchPlayerException localNoSuchPlayerException1) {}
          }
        }
      }
      else if ((WurmId.getType(creatureId) == 2) || 
        (WurmId.getType(creatureId) == 19) || 
        (WurmId.getType(creatureId) == 20)) {
        try
        {
          Item item = Items.getItem(creatureId);
          if (!this.items.contains(item)) {
            addItem(item, null, true);
          }
        }
        catch (NoSuchItemException localNoSuchItemException1) {}
      }
    }
    if (send) {
      if (creatureId == this.watcher.getWurmId()) {
        this.watcher.getCommunicator().attachCreature(-1L, targetId, offx, offy, offz, seatId);
      } else {
        this.watcher.getCommunicator().attachCreature(creatureId, targetId, offx, offy, offz, seatId);
      }
    }
  }
  
  public void sendRotate(Item item, float rotation)
  {
    if ((this.items != null) && (this.items.contains(item))) {
      this.watcher.getCommunicator().sendRotate(item.getWurmId(), rotation);
    }
  }
  
  public String toString()
  {
    return "VirtualZone [ID: " + this.id + ", Watcher: " + this.watcher.getWurmId() + ']';
  }
  
  public void sendHostileCreatures()
  {
    int nums = 0;
    String layer = "Above ground";
    if (!this.isOnSurface) {
      layer = "Below ground";
    }
    for (Long c : this.creatures.keySet()) {
      try
      {
        Creature creat = Server.getInstance().getCreature(c.longValue());
        if (creat.getAttitude(this.watcher) == 2)
        {
          int tilex = creat.getTileX();
          int tiley = creat.getTileY();
          if (this.watcher.getCurrentTile() != null)
          {
            nums++;
            int ctx = this.watcher.getCurrentTile().tilex;
            int cty = this.watcher.getCurrentTile().tiley;
            int mindist = Math.max(Math.abs(tilex - ctx), Math.abs(tiley - cty));
            int dir = MethodsCreatures.getDir(this.watcher, tilex, tiley);
            String direction = MethodsCreatures.getLocationStringFor(this.watcher.getStatus().getRotation(), dir, "you");
            
            this.watcher.getCommunicator().sendNormalServerMessage(
              EndGameItems.getDistanceString(mindist, creat.getName(), direction, false) + layer);
          }
        }
      }
      catch (NoSuchCreatureException nsc)
      {
        logger.log(Level.WARNING, nsc.getMessage(), nsc);
      }
      catch (NoSuchPlayerException nsp)
      {
        logger.log(Level.WARNING, nsp.getMessage(), nsp);
      }
    }
    if (nums == 0) {
      this.watcher.getCommunicator().sendNormalServerMessage("No hostile creatures found " + layer.toLowerCase() + ".");
    }
  }
  
  public void sendAddTileEffect(int tilex, int tiley, int layer, byte effect, int floorLevel, boolean loop)
  {
    this.watcher.getCommunicator().sendAddAreaSpellEffect(tilex, tiley, layer, effect, floorLevel, 0, loop);
  }
  
  public void sendRemoveTileEffect(int tilex, int tiley, int layer)
  {
    this.watcher.getCommunicator().sendRemoveAreaSpellEffect(tilex, tiley, layer);
  }
  
  public void updateWallDamageState(Wall wall)
  {
    this.watcher.getCommunicator().sendWallDamageState(wall.getStructureId(), wall.getId(), (byte)(int)wall.getDamage());
  }
  
  public void updateFloorDamageState(Floor floor)
  {
    this.watcher.getCommunicator().sendWallDamageState(floor.getStructureId(), floor.getId(), (byte)(int)floor.getDamage());
  }
  
  public void updateBridgePartDamageState(BridgePart bridgePart)
  {
    this.watcher.getCommunicator().sendWallDamageState(bridgePart.getStructureId(), bridgePart.getId(), (byte)(int)bridgePart.getDamage());
  }
  
  public void updateFenceDamageState(Fence fence)
  {
    this.watcher.getCommunicator().sendDamageState(fence.getId(), (byte)(int)fence.getDamage());
  }
  
  public void updateTargetStatus(long targetId, byte statusType, float status)
  {
    this.watcher.getCommunicator().sendTargetStatus(targetId, statusType, status);
  }
  
  public void setNewFace(Creature c)
  {
    if (c.getWurmId() == this.watcher.getWurmId()) {
      this.watcher.getCommunicator().sendNewFace(-10L, c.getFace());
    } else if (containsCreature(c)) {
      this.watcher.getCommunicator().sendNewFace(c.getWurmId(), c.getFace());
    }
  }
  
  public void setNewRarityShader(Creature c)
  {
    if (c.getWurmId() == this.watcher.getWurmId()) {
      this.watcher.getCommunicator().updateCreatureRarity(-10L, c.getRarityShader());
    } else if (containsCreature(c)) {
      this.watcher.getCommunicator().updateCreatureRarity(c.getWurmId(), c.getRarityShader());
    }
  }
  
  public void sendActionControl(long creatureId, String actionString, boolean start, int timeLeft)
  {
    if (creatureId == this.watcher.getWurmId()) {
      this.watcher.getCommunicator().sendActionControl(-1L, actionString, start, timeLeft);
    } else {
      this.watcher.getCommunicator().sendActionControl(creatureId, actionString, start, timeLeft);
    }
  }
  
  public void sendProjectile(long itemid, byte type, String modelName, String name, byte material, float _startX, float _startY, float startH, float rot, byte layer, float _endX, float _endY, float endH, long sourceId, long targetId, float projectedSecondsInAir, float actualSecondsInAir)
  {
    this.watcher.getCommunicator().sendProjectile(itemid, type, modelName, name, material, _startX, _startY, startH, rot, layer, _endX, _endY, endH, sourceId, targetId, projectedSecondsInAir, actualSecondsInAir);
  }
  
  public void sendNewProjectile(long itemid, byte type, String modelName, String name, byte material, Vector3f startingPosition, Vector3f startingVelocity, Vector3f endingPosition, float rotation, boolean surface)
  {
    this.watcher.getCommunicator().sendNewProjectile(itemid, type, modelName, name, material, startingPosition, startingVelocity, endingPosition, rotation, surface);
  }
  
  public final void sendBridgeId(long creatureId, long bridgeId)
  {
    this.watcher.getCommunicator().sendBridgeId(creatureId, bridgeId);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\zones\VirtualZone.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */