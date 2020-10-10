package com.wurmonline.server.zones;

import com.wurmonline.math.Vector3f;
import com.wurmonline.mesh.MeshIO;
import com.wurmonline.mesh.Tiles;
import com.wurmonline.mesh.Tiles.Tile;
import com.wurmonline.mesh.Tiles.TileBorderDirection;
import com.wurmonline.server.FailedException;
import com.wurmonline.server.Features.Feature;
import com.wurmonline.server.HistoryManager;
import com.wurmonline.server.Items;
import com.wurmonline.server.Message;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.MovementListener;
import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Players;
import com.wurmonline.server.Server;
import com.wurmonline.server.ServerEntry;
import com.wurmonline.server.Servers;
import com.wurmonline.server.WurmId;
import com.wurmonline.server.behaviours.MethodsCreatures;
import com.wurmonline.server.behaviours.Seat;
import com.wurmonline.server.behaviours.Vehicle;
import com.wurmonline.server.behaviours.Vehicles;
import com.wurmonline.server.bodys.Body;
import com.wurmonline.server.bodys.TempWound;
import com.wurmonline.server.bodys.Wound;
import com.wurmonline.server.bodys.Wounds;
import com.wurmonline.server.combat.CombatEngine;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.CreatureStatus;
import com.wurmonline.server.creatures.Creatures;
import com.wurmonline.server.creatures.MineDoorPermission;
import com.wurmonline.server.creatures.NoArmourException;
import com.wurmonline.server.creatures.NoSuchCreatureException;
import com.wurmonline.server.creatures.VisionArea;
import com.wurmonline.server.creatures.Wagoner;
import com.wurmonline.server.deities.Deity;
import com.wurmonline.server.economy.Economy;
import com.wurmonline.server.economy.Shop;
import com.wurmonline.server.effects.Effect;
import com.wurmonline.server.epic.EpicTargetItems;
import com.wurmonline.server.highways.HighwayFinder;
import com.wurmonline.server.highways.HighwayPos;
import com.wurmonline.server.highways.MethodsHighways;
import com.wurmonline.server.highways.Node;
import com.wurmonline.server.highways.Routes;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemFactory;
import com.wurmonline.server.items.ItemTemplate;
import com.wurmonline.server.items.ItemTemplateFactory;
import com.wurmonline.server.items.ItemTypes;
import com.wurmonline.server.items.NoSpaceException;
import com.wurmonline.server.items.NoSuchTemplateException;
import com.wurmonline.server.items.WurmColor;
import com.wurmonline.server.kingdom.Kingdoms;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.skills.NoSuchSkillException;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.skills.Skills;
import com.wurmonline.server.sounds.Sound;
import com.wurmonline.server.spells.Spell;
import com.wurmonline.server.spells.SpellResist;
import com.wurmonline.server.structures.Blocker;
import com.wurmonline.server.structures.BridgePart;
import com.wurmonline.server.structures.DbWall;
import com.wurmonline.server.structures.Door;
import com.wurmonline.server.structures.Fence;
import com.wurmonline.server.structures.FenceGate;
import com.wurmonline.server.structures.Floor;
import com.wurmonline.server.structures.NoSuchStructureException;
import com.wurmonline.server.structures.NoSuchWallException;
import com.wurmonline.server.structures.Structure;
import com.wurmonline.server.structures.StructureSupport;
import com.wurmonline.server.structures.Structures;
import com.wurmonline.server.structures.TempFence;
import com.wurmonline.server.structures.Wall;
import com.wurmonline.server.villages.NoSuchRoleException;
import com.wurmonline.server.villages.NoSuchVillageException;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.villages.Villages;
import com.wurmonline.shared.constants.BridgeConstants.BridgeState;
import com.wurmonline.shared.constants.CounterTypes;
import com.wurmonline.shared.constants.ItemMaterials;
import com.wurmonline.shared.constants.StructureConstantsEnum;
import com.wurmonline.shared.constants.StructureMaterialEnum;
import com.wurmonline.shared.constants.StructureTypeEnum;
import com.wurmonline.shared.util.MaterialUtilities;
import com.wurmonline.shared.util.MulticolorLineSegment;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class VolaTile
  implements MovementListener, ItemTypes, MiscConstants, CounterTypes, ItemMaterials
{
  private static final Logger logger = Logger.getLogger(VolaTile.class.getName());
  private VolaTileItems vitems = null;
  private Structure structure;
  private Set<Effect> effects;
  private Set<Creature> creatures;
  private Set<Wall> walls;
  private Set<Floor> floors;
  private Set<BridgePart> bridgeParts;
  private Set<MineDoorPermission> mineDoors;
  public final int tilex;
  public final int tiley;
  private final boolean surfaced;
  private Set<VirtualZone> watchers;
  private final Zone zone;
  private Set<Door> doors;
  private boolean inactive = false;
  private static final Set<StructureSupport> emptySupports = new HashSet();
  private Map<Long, Fence> fences;
  private Map<Long, Fence> magicFences;
  private Village village;
  public boolean isTransition;
  private static final Creature[] emptyCreatures = new Creature[0];
  private static final Item[] emptyItems = new Item[0];
  private static final Wall[] emptyWalls = new Wall[0];
  private static final Fence[] emptyFences = new Fence[0];
  private static final Floor[] emptyFloors = new Floor[0];
  private static final BridgePart[] emptyBridgeParts = new BridgePart[0];
  private static final VirtualZone[] emptyWatchers = new VirtualZone[0];
  private static final Effect[] emptyEffects = new Effect[0];
  private static final Door[] emptyDoors = new Door[0];
  static final Set<Wall> toRemove = new HashSet();
  
  VolaTile(int x, int y, boolean isSurfaced, Set<VirtualZone> aWatchers, Zone zon)
  {
    this.tilex = x;
    this.tiley = y;
    this.surfaced = isSurfaced;
    this.zone = zon;
    this.watchers = aWatchers;
    checkTransition();
  }
  
  private final void checkTransition()
  {
    this.isTransition = (Tiles.decodeType(Server.caveMesh.getTile(this.tilex, this.tiley)) == Tiles.Tile.TILE_CAVE_EXIT.id);
  }
  
  public boolean isOnSurface()
  {
    return this.surfaced;
  }
  
  private boolean isLava()
  {
    return ((isOnSurface()) && (Tiles.decodeType(Server.surfaceMesh.getTile(this.tilex, this.tiley)) == Tiles.Tile.TILE_LAVA.id)) || (
      (!isOnSurface()) && (Tiles.decodeType(Server.caveMesh.getTile(this.tilex, this.tiley)) == Tiles.Tile.TILE_CAVE_WALL_LAVA.id));
  }
  
  public int getNumberOfItems(int floorLevel)
  {
    if (this.vitems == null) {
      return 0;
    }
    return this.vitems.getNumberOfItems(floorLevel);
  }
  
  public final int getNumberOfDecorations(int floorLevel)
  {
    if (this.vitems == null) {
      return 0;
    }
    return this.vitems.getNumberOfDecorations(floorLevel);
  }
  
  public void addFence(Fence fence)
  {
    if (this.fences == null) {
      this.fences = new ConcurrentHashMap();
    }
    if (fence.isMagic())
    {
      if (this.magicFences == null) {
        this.magicFences = new ConcurrentHashMap();
      }
      this.magicFences.put(Long.valueOf(fence.getId()), fence);
    }
    Fence f;
    if (fence.isTemporary())
    {
      f = (Fence)this.fences.get(Long.valueOf(fence.getId()));
      if ((f != null) && (!f.isTemporary())) {
        return;
      }
    }
    this.fences.put(Long.valueOf(fence.getId()), fence);
    if (fence.getZoneId() != this.zone.getId()) {
      fence.setZoneId(this.zone.getId());
    }
    for (VirtualZone vz : getWatchers()) {
      try
      {
        vz.addFence(fence);
      }
      catch (Exception e)
      {
        logger.log(Level.WARNING, e.getMessage(), e);
      }
    }
  }
  
  void setVillage(Village aVillage)
  {
    MineDoorPermission md = MineDoorPermission.getPermission(this.tilex, this.tiley);
    if ((this.village == null) || (!this.village.equals(aVillage)))
    {
      if (this.doors != null) {
        for (Door door : this.doors) {
          if ((door instanceof FenceGate)) {
            if (aVillage != null) {
              aVillage.addGate((FenceGate)door);
            } else if (this.village != null) {
              this.village.removeGate((FenceGate)door);
            }
          }
        }
      }
      if (md != null) {
        if (aVillage != null) {
          aVillage.addMineDoor(md);
        } else if (this.village != null) {
          this.village.removeMineDoor(md);
        }
      }
      if (this.creatures != null) {
        for (Creature c : this.creatures)
        {
          c.setCurrentVillage(aVillage);
          if ((c.isWagoner()) && (aVillage == null))
          {
            Wagoner wagoner = c.getWagoner();
            if (wagoner != null) {
              wagoner.clrVillage();
            }
          }
          if ((c.isNpcTrader()) && (c.getCitizenVillage() == null))
          {
            Shop s = Economy.getEconomy().getShop(c);
            if (s.getOwnerId() == -10L) {
              if (aVillage != null) {
                try
                {
                  logger.log(Level.INFO, "Adding " + c.getName() + " as citizen to " + aVillage.getName());
                  aVillage.addCitizen(c, aVillage.getRoleForStatus((byte)3));
                }
                catch (IOException iox)
                {
                  logger.log(Level.INFO, iox.getMessage());
                }
                catch (NoSuchRoleException nsx)
                {
                  logger.log(Level.INFO, nsx.getMessage());
                }
              } else {
                c.setCitizenVillage(null);
              }
            }
          }
        }
      }
      if (this.vitems != null) {
        for (Item i : this.vitems.getAllItemsAsSet()) {
          if (i.getTemplateId() == 757)
          {
            if (aVillage != null) {
              aVillage.addBarrel(i);
            } else if (this.village != null) {
              this.village.removeBarrel(i);
            }
          }
          else if (i.getTemplateId() == 1112) {
            if (aVillage != null)
            {
              Node node = Routes.getNode(i.getWurmId());
              if (node != null) {
                node.setVillage(aVillage);
              }
            }
            else if (this.village != null)
            {
              Node node = Routes.getNode(i.getWurmId());
              if (node != null) {
                node.setVillage(null);
              }
            }
          }
        }
      }
      this.village = aVillage;
    }
    else
    {
      if (this.doors != null) {
        for (Door door : this.doors) {
          if ((door instanceof FenceGate)) {
            aVillage.addGate((FenceGate)door);
          }
        }
      }
      if (md != null) {
        aVillage.addMineDoor(md);
      }
    }
  }
  
  public Village getVillage()
  {
    return this.village;
  }
  
  public void removeFence(Fence fence)
  {
    if (this.fences != null)
    {
      Fence f = (Fence)this.fences.remove(Long.valueOf(fence.getId()));
      if (f != null)
      {
        if (f.isMagic()) {
          if (this.magicFences != null)
          {
            this.magicFences.remove(Long.valueOf(fence.getId()));
            if (this.magicFences.isEmpty()) {
              this.magicFences = null;
            }
          }
        }
        if ((fence.isTemporary()) && (!f.isTemporary()))
        {
          this.fences.put(Long.valueOf(f.getId()), f);
        }
        else
        {
          for (VirtualZone vz : getWatchers()) {
            try
            {
              vz.removeFence(f);
            }
            catch (Exception e)
            {
              logger.log(Level.WARNING, e.getMessage(), e);
            }
          }
          if (this.fences.isEmpty()) {
            this.fences = null;
          }
        }
      }
    }
  }
  
  public void addSound(Sound sound)
  {
    for (VirtualZone vz : getWatchers()) {
      try
      {
        vz.playSound(sound);
      }
      catch (Exception e)
      {
        logger.log(Level.WARNING, e.getMessage(), e);
      }
    }
  }
  
  public void updateFence(Fence fence)
  {
    for (VirtualZone vz : getWatchers()) {
      try
      {
        vz.removeFence(fence);
        vz.addFence(fence);
      }
      catch (Exception e)
      {
        logger.log(Level.WARNING, e.getMessage(), e);
      }
    }
  }
  
  public void updateMagicalFence(Fence fence)
  {
    for (VirtualZone vz : getWatchers()) {
      try
      {
        vz.addFence(fence);
      }
      catch (Exception e)
      {
        logger.log(Level.WARNING, e.getMessage(), e);
      }
    }
  }
  
  public Fence[] getFences()
  {
    if (this.fences != null) {
      return (Fence[])this.fences.values().toArray(new Fence[this.fences.size()]);
    }
    return emptyFences;
  }
  
  public Collection<Fence> getFencesList()
  {
    if (this.fences != null) {
      return this.fences.values();
    }
    return null;
  }
  
  public Fence[] getAllFences()
  {
    Set<Fence> fenceSet = new HashSet();
    if (this.fences != null) {
      for (Fence f : this.fences.values()) {
        fenceSet.add(f);
      }
    }
    VolaTile eastTile = this.zone.getTileOrNull(this.tilex + 1, this.tiley);
    if (eastTile != null)
    {
      Fence[] eastFences = eastTile.getFencesForDir(Tiles.TileBorderDirection.DIR_DOWN);
      for (int x = 0; x < eastFences.length; x++) {
        fenceSet.add(eastFences[x]);
      }
    }
    VolaTile southTile = this.zone.getTileOrNull(this.tilex, this.tiley + 1);
    if (southTile != null)
    {
      Fence[] southFences = southTile.getFencesForDir(Tiles.TileBorderDirection.DIR_HORIZ);
      for (int x = 0; x < southFences.length; x++) {
        fenceSet.add(southFences[x]);
      }
    }
    if (fenceSet.size() == 0) {
      return emptyFences;
    }
    return (Fence[])fenceSet.toArray(new Fence[fenceSet.size()]);
  }
  
  public boolean hasFenceOnCorner(int floorLevel)
  {
    if (this.fences != null) {
      if (getFencesForLevel(floorLevel).length > 0) {
        return true;
      }
    }
    VolaTile westTile = this.zone.getTileOrNull(this.tilex - 1, this.tiley);
    if (westTile != null)
    {
      Fence[] westFences = westTile.getFencesForDirAndLevel(Tiles.TileBorderDirection.DIR_HORIZ, floorLevel);
      if (westFences.length > 0) {
        return true;
      }
    }
    VolaTile northTile = this.zone.getTileOrNull(this.tilex, this.tiley - 1);
    if (northTile != null)
    {
      Fence[] northFences = northTile.getFencesForDirAndLevel(Tiles.TileBorderDirection.DIR_DOWN, floorLevel);
      if (northFences.length > 0) {
        return true;
      }
    }
    return false;
  }
  
  public Fence[] getFencesForDirAndLevel(Tiles.TileBorderDirection dir, int floorLevel)
  {
    if (this.fences != null)
    {
      Set<Fence> fenceSet = new HashSet();
      for (Fence f : this.fences.values()) {
        if ((f.getDir() == dir) && (f.getFloorLevel() == floorLevel)) {
          fenceSet.add(f);
        }
      }
      return (Fence[])fenceSet.toArray(new Fence[fenceSet.size()]);
    }
    return emptyFences;
  }
  
  public Fence[] getFencesForDir(Tiles.TileBorderDirection dir)
  {
    if (this.fences != null)
    {
      Set<Fence> fenceSet = new HashSet();
      for (Fence f : this.fences.values()) {
        if (f.getDir() == dir) {
          fenceSet.add(f);
        }
      }
      return (Fence[])fenceSet.toArray(new Fence[fenceSet.size()]);
    }
    return emptyFences;
  }
  
  public Fence[] getFencesForLevel(int floorLevel)
  {
    if (this.fences != null)
    {
      Set<Fence> fenceSet = new HashSet();
      for (Fence f : this.fences.values()) {
        if (f.getFloorLevel() == floorLevel) {
          fenceSet.add(f);
        }
      }
      return (Fence[])fenceSet.toArray(new Fence[fenceSet.size()]);
    }
    return emptyFences;
  }
  
  public Fence getFence(long id)
  {
    if (this.fences != null) {
      return (Fence)this.fences.get(Long.valueOf(id));
    }
    return null;
  }
  
  public void addDoor(Door door)
  {
    if (this.doors == null) {
      this.doors = new HashSet();
    }
    if (!this.doors.contains(door))
    {
      this.doors.add(door);
      if (this.watchers != null) {
        for (VirtualZone vz : getWatchers()) {
          try
          {
            door.addWatcher(vz);
          }
          catch (Exception e)
          {
            logger.log(Level.WARNING, e.getMessage(), e);
          }
        }
      }
    }
  }
  
  public void removeDoor(Door door)
  {
    if (this.doors != null) {
      if (this.doors.contains(door))
      {
        if (this.watchers != null) {
          for (VirtualZone vz : getWatchers()) {
            try
            {
              door.removeWatcher(vz);
            }
            catch (Exception e)
            {
              logger.log(Level.WARNING, e.getMessage(), e);
            }
          }
        }
        this.doors.remove(door);
        if (this.doors.isEmpty()) {
          this.doors = null;
        }
      }
    }
  }
  
  public void addMineDoor(MineDoorPermission door)
  {
    if (this.mineDoors == null) {
      this.mineDoors = new HashSet();
    }
    if (this.mineDoors != null) {
      if (!this.mineDoors.contains(door))
      {
        this.mineDoors.add(door);
        if (this.watchers != null) {
          for (VirtualZone vz : getWatchers()) {
            try
            {
              door.addWatcher(vz);
              vz.addMineDoor(door);
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
  
  public void removeMineDoor(MineDoorPermission door)
  {
    for (VirtualZone vz : getWatchers()) {
      try
      {
        door.removeWatcher(vz);
        vz.removeMineDoor(door);
      }
      catch (Exception e)
      {
        logger.log(Level.WARNING, e.getMessage(), e);
      }
    }
    if (this.mineDoors == null) {
      return;
    }
    this.mineDoors.remove(door);
    if (this.mineDoors.isEmpty()) {
      this.mineDoors = null;
    }
  }
  
  public void checkChangedAttitude(Creature creature)
  {
    if (this.watchers != null) {
      for (VirtualZone vz : getWatchers()) {
        try
        {
          vz.sendAttitude(creature);
        }
        catch (Exception e)
        {
          logger.log(Level.WARNING, e.getMessage(), e);
        }
      }
    }
  }
  
  public void sendUpdateTarget(Creature creature)
  {
    if (this.watchers != null) {
      for (VirtualZone vz : getWatchers()) {
        try
        {
          vz.sendUpdateHasTarget(creature);
        }
        catch (Exception e)
        {
          logger.log(Level.WARNING, e.getMessage(), e);
        }
      }
    }
  }
  
  public Door[] getDoors()
  {
    if ((this.doors != null) && (this.doors.size() > 0)) {
      return (Door[])this.doors.toArray(new Door[this.doors.size()]);
    }
    return emptyDoors;
  }
  
  public final int getTileX()
  {
    return this.tilex;
  }
  
  public final int getTileY()
  {
    return this.tiley;
  }
  
  public final float getPosX()
  {
    return (this.tilex << 2) + 2;
  }
  
  public final float getPosY()
  {
    return (this.tiley << 2) + 2;
  }
  
  public final void pollMagicFences(long time)
  {
    if (this.magicFences != null) {
      for (Fence f : this.magicFences.values()) {
        f.pollMagicFences(time);
      }
    }
  }
  
  public void pollStructures(long time)
  {
    if (this.floors != null) {
      for (Floor floor : getFloors()) {
        if (floor.poll(time, this, this.structure)) {
          removeFloor(floor);
        }
      }
    }
    Object lTempWalls;
    int x;
    if (this.walls != null)
    {
      lTempWalls = getWalls();
      for (x = 0; x < lTempWalls.length; x++) {
        lTempWalls[x].poll(time, this, this.structure);
      }
    }
    if (this.fences != null) {
      for (Fence f : getFences()) {
        f.poll(time);
      }
    }
    if (this.bridgeParts != null) {
      for (BridgePart bridgePart : getBridgeParts()) {
        if (bridgePart.poll(time, this.structure)) {
          removeBridgePart(bridgePart);
        }
      }
    }
    if (this.structure != null) {
      this.structure.poll(time);
    }
  }
  
  public void poll(boolean pollItems, int seed, boolean setAreaEffectFlag)
  {
    boolean lava = isLava();
    
    long now = System.nanoTime();
    if (this.vitems != null) {
      this.vitems.poll(pollItems, seed, lava, this.structure, isOnSurface(), this.village, now);
    }
    pollMagicFences(now);
    if (lava) {
      for (Creature c : getCreatures()) {
        c.setDoLavaDamage(true);
      }
    }
    if (setAreaEffectFlag) {
      if (getAreaEffect() != null) {
        for (Creature c : getCreatures()) {
          c.setDoAreaEffect(true);
        }
      }
    }
    pollAllDoorsOnThisTile();
    
    applyLavaDamageToWallsAndFences();
    
    checkDeletion();
    if (Servers.isThisAPvpServer()) {
      pollOnDeedEnemys();
    }
  }
  
  private void pollOnDeedEnemys()
  {
    if (getVillage() != null) {
      for (Creature c : getCreatures()) {
        if ((c.getPower() < 1) && (c.isPlayer()) && (getVillage().kingdom != c.getKingdomId())) {
          try
          {
            c.currentVillage.getToken().setLastOwnerId(System.currentTimeMillis());
          }
          catch (NoSuchItemException e)
          {
            e.printStackTrace();
          }
        }
      }
    }
  }
  
  public final boolean doAreaDamage(Creature aCreature)
  {
    boolean dead = false;
    if ((!aCreature.isInvulnerable()) && (!aCreature.isGhost()) && (!aCreature.isUnique()))
    {
      AreaSpellEffect aes = getAreaEffect();
      if (aes != null)
      {
        System.out.println("AREA DAMAGE " + aCreature.getName());
        if (aes.getFloorLevel() != aCreature.getFloorLevel())
        {
          int heightOffset = aes.getHeightOffset();
          if (heightOffset != 0)
          {
            int pz = aCreature.getPosZDirts();
            if (Math.abs(pz - heightOffset) > 10)
            {
              System.out.println("AREA DAMAGE FAILED");
              return false;
            }
          }
        }
        byte type = getAreaEffect().getType();
        Creature caster = null;
        try
        {
          caster = Server.getInstance().getCreature(aes.getCreator());
        }
        catch (NoSuchCreatureException localNoSuchCreatureException) {}catch (NoSuchPlayerException localNoSuchPlayerException) {}
        if (caster != null) {
          try
          {
            if ((aCreature.getAttitude(caster) == 2) || (
              (caster.getCitizenVillage() != null) && (caster.getCitizenVillage().isEnemy(aCreature))))
            {
              boolean ok = true;
              if ((!caster.isOnPvPServer()) || (!aCreature.isOnPvPServer()))
              {
                Village v = aCreature.getCurrentVillage();
                if ((v != null) && (!v.mayAttack(caster, aCreature))) {
                  ok = false;
                }
              }
              if (ok)
              {
                aCreature.addAttacker(caster);
                if ((type == 36) || (type == 53))
                {
                  byte pos = aCreature.getBody().getRandomWoundPos();
                  sendAttachCreatureEffect(aCreature, (byte)6, (byte)0, (byte)0, (byte)0, (byte)0);
                  
                  double damage = getAreaEffect().getPower() * 4.0D;
                  damage += 150.0D;
                  double resistance = SpellResist.getSpellResistance(aCreature, 414);
                  damage *= resistance;
                  
                  SpellResist.addSpellResistance(aCreature, 414, damage);
                  
                  damage = Spell.modifyDamage(aCreature, damage);
                  
                  dead = CombatEngine.addWound(caster, aCreature, (byte)8, pos, damage, 1.0F, "", null, 0.0F, 0.0F, false, false, true, true);
                }
                else if ((type == 35) || (type == 51))
                {
                  byte pos = aCreature.getBody().getRandomWoundPos();
                  
                  double damage = getAreaEffect().getPower() * 2.75D;
                  damage += 300.0D;
                  double resistance = SpellResist.getSpellResistance(aCreature, 420);
                  damage *= resistance;
                  
                  SpellResist.addSpellResistance(aCreature, 420, damage);
                  
                  damage = Spell.modifyDamage(aCreature, damage);
                  dead = CombatEngine.addWound(caster, aCreature, (byte)4, pos, damage, 1.0F, "", null, 0.0F, 0.0F, false, false, true, true);
                }
                else if (type == 34)
                {
                  byte pos = aCreature.getBody().getRandomWoundPos();
                  
                  double damage = getAreaEffect().getPower() * 1.0D;
                  damage += 400.0D;
                  double resistance = SpellResist.getSpellResistance(aCreature, 418);
                  damage *= resistance;
                  
                  SpellResist.addSpellResistance(aCreature, 418, damage);
                  
                  damage = Spell.modifyDamage(aCreature, damage);
                  
                  dead = CombatEngine.addWound(caster, aCreature, (byte)0, pos, damage, 1.0F, "", null, 1.0F, 0.0F, false, false, true, true);
                }
                else if (type == 37)
                {
                  sendAttachCreatureEffect(aCreature, (byte)7, (byte)0, (byte)0, (byte)0, (byte)0);
                  
                  byte pos = aCreature.getBody().getRandomWoundPos();
                  
                  double damage = getAreaEffect().getPower() * 2.0D;
                  damage += 350.0D;
                  double resistance = SpellResist.getSpellResistance(aCreature, 433);
                  damage *= resistance;
                  
                  SpellResist.addSpellResistance(aCreature, 433, damage);
                  
                  damage = Spell.modifyDamage(aCreature, damage);
                  
                  dead = CombatEngine.addWound(caster, aCreature, (byte)5, pos, damage, 1.0F, "", null, 0.0F, 3.0F, false, false, true, true);
                }
              }
            }
          }
          catch (Exception exe)
          {
            logger.log(Level.WARNING, exe.getMessage(), exe);
          }
        }
      }
    }
    return dead;
  }
  
  private void pollAllDoorsOnThisTile()
  {
    Iterator<Door> it;
    if ((this.doors != null) && (this.doors.size() > 0)) {
      for (it = this.doors.iterator(); it.hasNext();) {
        ((Door)it.next()).poll();
      }
    }
  }
  
  private void applyLavaDamageToWallsAndFences()
  {
    if (isLava())
    {
      Wall[] lTempWalls;
      int x;
      if (this.walls != null)
      {
        lTempWalls = getWalls();
        for (x = 0; x < lTempWalls.length; x++) {
          lTempWalls[x].setDamage(lTempWalls[x].getDamage() + 1.0F);
        }
      }
      if (this.fences != null) {
        for (Fence f : getFences()) {
          f.setDamage(f.getDamage() + 1.0F);
        }
      }
    }
  }
  
  private void pollAllWatchersOfThisTile()
  {
    for (VirtualZone vz : getWatchers()) {
      try
      {
        if ((vz.getWatcher() instanceof Player)) {
          if (!vz.getWatcher().hasLink()) {
            removeWatcher(vz);
          }
        }
      }
      catch (Exception e)
      {
        logger.log(Level.WARNING, e.getMessage(), e);
      }
    }
  }
  
  private void pollAllCreaturesOnThisTile(boolean lava, boolean areaEffect)
  {
    long lStart = System.nanoTime();
    
    Creature[] lTempCreatures = getCreatures();
    for (int x = 0; x < lTempCreatures.length; x++) {
      pollOneCreatureOnThisTile(lava, lTempCreatures[x], areaEffect);
    }
    if (((float)(System.nanoTime() - lStart) / 1000000.0F > 300.0F) && (!Servers.localServer.testServer))
    {
      int destroyed = 0;
      for (int y = 0; y < lTempCreatures.length; y++) {
        if (lTempCreatures[y].isDead()) {
          destroyed++;
        }
      }
      logger.log(Level.INFO, "Tile at " + this.tilex + ", " + this.tiley + " polled " + lTempCreatures.length + " creatures. Of those were " + destroyed + " destroyed. It took " + 
      
        (float)(System.nanoTime() - lStart) / 1000000.0F + " ms");
    }
  }
  
  public final boolean isVisibleToPlayers()
  {
    for (VirtualZone vz : getWatchers()) {
      try
      {
        if ((vz.getWatcher() != null) && (vz.getWatcher().isPlayer())) {
          return true;
        }
      }
      catch (Exception e)
      {
        logger.log(Level.WARNING, e.getMessage(), e);
      }
    }
    return false;
  }
  
  private void pollOneCreatureOnThisTile(boolean lava, Creature aCreature, boolean areaEffect)
  {
    try
    {
      boolean dead = false;
      if (aCreature.poll()) {
        deleteCreature(aCreature);
      } else if (lava)
      {
        if ((!aCreature.isInvulnerable()) && (!aCreature.isGhost()) && (!aCreature.isUnique())) {
          if ((aCreature.getDeity() == null) || (!aCreature.getDeity().isMountainGod()) || (aCreature.getFaith() < 35.0F)) {
            if (aCreature.getFarwalkerSeconds() <= 0)
            {
              Wound wound = null;
              try
              {
                byte pos = aCreature.getBody().getRandomWoundPos((byte)10);
                if (Server.rand.nextInt(10) <= 6) {
                  if (aCreature.getBody().getWounds() != null)
                  {
                    wound = aCreature.getBody().getWounds().getWoundAtLocation(pos);
                    if (wound != null)
                    {
                      dead = wound.modifySeverity(
                        (int)(5000.0F + Server.rand.nextInt(5000) * (100.0F - aCreature.getSpellDamageProtectBonus()) / 100.0F));
                      wound.setBandaged(false);
                      aCreature.setWounded();
                    }
                  }
                }
                if (wound == null) {
                  if ((!aCreature.isGhost()) && (!aCreature.isUnique()) && 
                    (!aCreature.isKingdomGuard())) {
                    dead = aCreature.addWoundOfType(null, (byte)4, pos, false, 1.0F, true, 5000.0F + Server.rand
                      .nextInt(5000) * (100.0F - aCreature.getSpellDamageProtectBonus()) / 100.0F, 0.0F, 0.0F, false, false);
                  }
                }
                aCreature.getCommunicator().sendAlertServerMessage("You are burnt by lava!");
                if (dead)
                {
                  aCreature.achievement(142);
                  deleteCreature(aCreature);
                }
              }
              catch (Exception ex)
              {
                logger.log(Level.WARNING, aCreature.getName() + " " + ex.getMessage(), ex);
              }
            }
          }
        }
      }
      else if ((!dead) && (areaEffect)) {
        if ((!aCreature.isInvulnerable()) && (!aCreature.isGhost()) && (!aCreature.isUnique()))
        {
          AreaSpellEffect aes = getAreaEffect();
          if ((aes != null) && (aes.getFloorLevel() == aCreature.getFloorLevel()))
          {
            byte type = aes.getType();
            Creature caster = null;
            try
            {
              caster = Server.getInstance().getCreature(aes.getCreator());
            }
            catch (NoSuchCreatureException localNoSuchCreatureException) {}catch (NoSuchPlayerException localNoSuchPlayerException) {}
            if (caster != null) {
              try
              {
                if ((aCreature.getAttitude(caster) == 2) || (
                  (caster.getCitizenVillage() != null) && (caster.getCitizenVillage().isEnemy(aCreature))))
                {
                  boolean ok = true;
                  if ((!caster.isOnPvPServer()) || (!aCreature.isOnPvPServer()))
                  {
                    Village v = aCreature.getCurrentVillage();
                    if ((v != null) && (!v.mayAttack(caster, aCreature))) {
                      ok = false;
                    }
                  }
                  if (ok) {
                    aCreature.addAttacker(caster);
                  }
                }
              }
              catch (Exception exe)
              {
                logger.log(Level.WARNING, exe.getMessage(), exe);
              }
            }
          }
        }
      }
    }
    catch (Exception ex)
    {
      logger.log(Level.WARNING, "Failed to poll creature " + aCreature.getWurmId() + " " + ex.getMessage(), ex);
      try
      {
        Server.getInstance().getCreature(aCreature.getWurmId());
      }
      catch (Exception nsc)
      {
        logger.log(Level.INFO, "Failed to locate creature. Removing from tile. Creature: " + aCreature);
        if (this.creatures != null) {
          this.creatures.remove(aCreature);
        }
      }
    }
  }
  
  public void deleteCreature(Creature creature)
  {
    creature.setNewTile(null, 0, false);
    removeCreature(creature);
    try
    {
      this.zone.deleteCreature(creature, false);
    }
    catch (NoSuchCreatureException nsc)
    {
      logger.log(Level.WARNING, nsc.getMessage(), nsc);
    }
    catch (NoSuchPlayerException nsp)
    {
      logger.log(Level.WARNING, nsp.getMessage(), nsp);
    }
    Door[] _doors = getDoors();
    for (int d = 0; d < _doors.length; d++) {
      _doors[d].removeCreature(creature);
    }
  }
  
  boolean containsCreature(Creature creature)
  {
    return (this.creatures != null) && (this.creatures.contains(creature));
  }
  
  public void deleteCreatureQuick(Creature creature)
  {
    creature.setNewTile(null, 0, false);
    this.zone.removeCreature(creature, true, false);
    Door[] lDoors = getDoors();
    for (int d = 0; d < lDoors.length; d++) {
      lDoors[d].removeCreature(creature);
    }
  }
  
  public void broadCastMulticolored(List<MulticolorLineSegment> segments, Creature performer, @Nullable Creature receiver, boolean combat, byte onScreenMessage)
  {
    Iterator<Creature> it;
    if (this.creatures != null) {
      for (it = this.creatures.iterator(); it.hasNext();)
      {
        Creature creature = (Creature)it.next();
        if ((!creature.equals(performer)) && ((receiver == null) || (!creature.equals(receiver)))) {
          if (performer.isVisibleTo(creature)) {
            if (!creature.getCommunicator().isInvulnerable()) {
              if (combat) {
                creature.getCommunicator().sendColoredMessageCombat(segments, onScreenMessage);
              } else {
                creature.getCommunicator().sendColoredMessageEvent(segments, onScreenMessage);
              }
            }
          }
        }
      }
    }
  }
  
  public void broadCastAction(String message, Creature performer, boolean combat)
  {
    broadCastAction(message, performer, null, combat);
  }
  
  public void broadCastAction(String message, Creature performer, @Nullable Creature receiver, boolean combat)
  {
    Iterator<Creature> it;
    if (this.creatures != null) {
      for (it = this.creatures.iterator(); it.hasNext();)
      {
        Creature creature = (Creature)it.next();
        if ((!creature.equals(performer)) && ((receiver == null) || (!creature.equals(receiver)))) {
          if (performer.isVisibleTo(creature)) {
            if (!creature.getCommunicator().isInvulnerable()) {
              if (combat) {
                creature.getCommunicator().sendCombatNormalMessage(message);
              } else {
                creature.getCommunicator().sendNormalServerMessage(message);
              }
            }
          }
        }
      }
    }
  }
  
  public void broadCast(String message)
  {
    Iterator<Creature> it;
    if (this.creatures != null) {
      for (it = this.creatures.iterator(); it.hasNext();)
      {
        Creature creature = (Creature)it.next();
        if (!creature.getCommunicator().isInvulnerable()) {
          creature.getCommunicator().sendNormalServerMessage(message);
        }
      }
    }
  }
  
  public void broadCastMessage(Message message)
  {
    Iterator<VirtualZone> it;
    if (this.watchers != null) {
      for (it = this.watchers.iterator(); it.hasNext();)
      {
        VirtualZone z = (VirtualZone)it.next();
        z.broadCastMessage(message);
      }
    }
  }
  
  void broadCastMessageLocal(Message message)
  {
    Iterator<Creature> it;
    if (this.creatures != null) {
      for (it = this.creatures.iterator(); it.hasNext();)
      {
        Creature creature = (Creature)it.next();
        if (!creature.getCommunicator().isInvulnerable()) {
          creature.getCommunicator().sendMessage(message);
        }
      }
    }
  }
  
  void addWatcher(VirtualZone watcher)
  {
    if (this.watchers == null) {
      this.watchers = new HashSet();
    }
    Iterator<MineDoorPermission> dit;
    if (!this.watchers.contains(watcher))
    {
      this.watchers.add(watcher);
      linkTo(watcher, false);
      Iterator<Door> dit;
      if (this.doors != null) {
        for (dit = this.doors.iterator(); dit.hasNext();)
        {
          Door door = (Door)dit.next();
          door.addWatcher(watcher);
        }
      }
      if (this.mineDoors != null) {
        for (dit = this.mineDoors.iterator(); dit.hasNext();)
        {
          MineDoorPermission door = (MineDoorPermission)dit.next();
          door.addWatcher(watcher);
        }
      }
    }
  }
  
  void removeWatcher(VirtualZone watcher)
  {
    if (this.watchers != null)
    {
      if (this.watchers.contains(watcher))
      {
        this.watchers.remove(watcher);
        linkTo(watcher, true);
        Iterator<Door> dit;
        if (this.doors != null) {
          for (dit = this.doors.iterator(); dit.hasNext();)
          {
            Door door = (Door)dit.next();
            door.removeWatcher(watcher);
          }
        }
        Iterator<MineDoorPermission> dit;
        if (this.mineDoors != null) {
          for (dit = this.mineDoors.iterator(); dit.hasNext();)
          {
            door = (MineDoorPermission)dit.next();
            door.removeWatcher(watcher);
          }
        }
        MineDoorPermission door;
        if (!isVisibleToPlayers())
        {
          dit = getCreatures();door = dit.length;
          for (MineDoorPermission localMineDoorPermission1 = 0; localMineDoorPermission1 < door; localMineDoorPermission1++)
          {
            Creature c = dit[localMineDoorPermission1];
            
            c.setVisibleToPlayers(false);
          }
        }
      }
      if (logger.isLoggable(Level.FINEST)) {
        logger.finest("Tile: " + this.tilex + ", " + this.tiley + "removing watcher " + watcher.getId());
      }
    }
    else if (logger.isLoggable(Level.FINEST))
    {
      logger.finest("Tile: " + this.tilex + ", " + this.tiley + " tried to remove but watchers is null though.");
    }
  }
  
  void addEffect(Effect effect, boolean temp)
  {
    if ((this.isTransition) && (this.surfaced))
    {
      getCaveTile().addEffect(effect, temp);
      return;
    }
    if (!temp)
    {
      if (this.effects == null) {
        this.effects = new HashSet();
      }
      this.effects.add(effect);
    }
    Iterator<VirtualZone> it;
    if (this.watchers != null) {
      for (it = this.watchers.iterator(); it.hasNext();) {
        ((VirtualZone)it.next()).addEffect(effect, temp);
      }
    }
    effect.setSurfaced(this.surfaced);
    try
    {
      effect.save();
    }
    catch (IOException iox)
    {
      logger.log(Level.INFO, iox.getMessage(), iox);
    }
  }
  
  int addCreature(Creature creature, int diffZ)
    throws NoSuchCreatureException, NoSuchPlayerException
  {
    if (this.inactive)
    {
      logger.log(Level.WARNING, "AT 1 adding " + creature.getName() + " who is at " + creature.getTileX() + ", " + creature
        .getTileY() + " to inactive tile " + this.tilex + "," + this.tiley, new Exception());
      logger.log(Level.WARNING, "The zone " + this.zone.id + " covers " + this.zone.startX + ", " + this.zone.startY + " to " + this.zone.endX + "," + this.zone.endY);
    }
    if (!creature.setNewTile(this, diffZ, false)) {
      return 0;
    }
    if (this.creatures == null) {
      this.creatures = new HashSet();
    }
    for (Creature c : this.creatures) {
      if (!c.isFriendlyKingdom(creature.getKingdomId())) {
        c.setStealth(false);
      }
    }
    this.creatures.add(creature);
    
    creature.setCurrentVillage(this.village);
    
    creature.calculateZoneBonus(this.tilex, this.tiley, this.surfaced);
    if (creature.isPlayer()) {
      try
      {
        FaithZone z = Zones.getFaithZone(this.tilex, this.tiley, this.surfaced);
        if (z != null) {
          creature.setCurrentDeity(z.getCurrentRuler());
        } else {
          creature.setCurrentDeity(null);
        }
      }
      catch (NoSuchZoneException nsz)
      {
        logger.log(Level.WARNING, "No faith zone here? " + this.tilex + ", " + this.tiley + ", surf=" + this.surfaced);
      }
    }
    if ((creature.getHighwayPathDestination().length() > 0) || (creature.isWagoner()))
    {
      HighwayPos currentHighwayPos = null;
      if (creature.getBridgeId() != -10L)
      {
        BridgePart bridgePart = Zones.getBridgePartFor(this.tilex, this.tiley, this.surfaced);
        if (bridgePart != null) {
          currentHighwayPos = MethodsHighways.getHighwayPos(bridgePart);
        }
      }
      if ((currentHighwayPos == null) && (creature.getFloorLevel() > 0))
      {
        Floor floor = Zones.getFloor(this.tilex, this.tiley, this.surfaced, creature.getFloorLevel());
        if (floor != null) {
          currentHighwayPos = MethodsHighways.getHighwayPos(floor);
        }
      }
      if (currentHighwayPos == null) {
        currentHighwayPos = MethodsHighways.getHighwayPos(this.tilex, this.tiley, this.surfaced);
      }
      if (currentHighwayPos != null)
      {
        Item waystone = getWaystone(currentHighwayPos);
        if (waystone == null) {
          waystone = getWaystone(MethodsHighways.getNewHighwayPosLinked(currentHighwayPos, (byte)1));
        }
        if (waystone == null) {
          waystone = getWaystone(MethodsHighways.getNewHighwayPosLinked(currentHighwayPos, (byte)Byte.MIN_VALUE));
        }
        if (waystone == null) {
          waystone = getWaystone(MethodsHighways.getNewHighwayPosLinked(currentHighwayPos, (byte)64));
        }
        if (waystone == null) {
          waystone = getWaystone(MethodsHighways.getNewHighwayPosLinked(currentHighwayPos, (byte)32));
        }
        if (waystone == null) {
          waystone = getWaystone(MethodsHighways.getNewHighwayPosLinked(currentHighwayPos, (byte)2));
        }
        if (waystone == null) {
          waystone = getWaystone(MethodsHighways.getNewHighwayPosLinked(currentHighwayPos, (byte)4));
        }
        if (waystone == null) {
          waystone = getWaystone(MethodsHighways.getNewHighwayPosLinked(currentHighwayPos, (byte)16));
        }
        if (waystone == null) {
          waystone = getWaystone(MethodsHighways.getNewHighwayPosLinked(currentHighwayPos, (byte)8));
        }
        if ((waystone != null) && (creature.getLastWaystoneChecked() != waystone.getWurmId())) {
          if (creature.isWagoner())
          {
            creature.setLastWaystoneChecked(waystone.getWurmId());
          }
          else
          {
            Node startNode = Routes.getNode(waystone.getWurmId());
            String goingto = creature.getHighwayPathDestination();
            if ((startNode.getVillage() == null) || (creature.currentVillage != null) || (!startNode.getVillage().getName().equalsIgnoreCase(goingto)))
            {
              creature.setLastWaystoneChecked(waystone.getWurmId());
              try
              {
                Village destinationVillage = Villages.getVillage(goingto);
                HighwayFinder.queueHighwayFinding(creature, startNode, destinationVillage, (byte)0);
              }
              catch (NoSuchVillageException e)
              {
                creature.getCommunicator().sendNormalServerMessage("Destination village (" + goingto + ") cannot be found.");
              }
            }
          }
        }
      }
    }
    return this.creatures.size();
  }
  
  @Nullable
  private Item getWaystone(@Nullable HighwayPos highwayPos)
  {
    if (highwayPos == null) {
      return null;
    }
    Item marker = MethodsHighways.getMarker(highwayPos);
    if ((marker != null) && (marker.getTemplateId() == 1112)) {
      return marker;
    }
    return null;
  }
  
  public boolean removeCreature(Creature creature)
  {
    if (this.creatures != null)
    {
      boolean removed = this.creatures.remove(creature);
      if (this.creatures.isEmpty()) {
        this.creatures = null;
      }
      if (!removed) {
        return false;
      }
      Door[] doorArr = getDoors();
      for (int d = 0; d < doorArr.length; d++) {
        if (!doorArr[d].covers(creature.getPosX(), creature.getPosY(), creature.getPositionZ(), creature.getFloorLevel(), creature
          .followsGround())) {
          doorArr[d].removeCreature(creature);
        }
      }
      Iterator<VirtualZone> it;
      if (this.watchers != null) {
        for (it = this.watchers.iterator(); it.hasNext();)
        {
          VirtualZone watchingZone = (VirtualZone)it.next();
          
          watchingZone.removeCreature(creature);
        }
      }
      return true;
    }
    return false;
  }
  
  public boolean checkOpportunityAttacks(Creature creature)
  {
    Creature[] lTempCreatures = getCreatures();
    for (int x = 0; x < lTempCreatures.length; x++) {
      if ((lTempCreatures[x] != creature) && (!lTempCreatures[x].isMoving())) {
        if (lTempCreatures[x].getAttitude(creature) == 2) {
          if (VirtualZone.isCreatureTurnedTowardsTarget(creature, lTempCreatures[x])) {
            return lTempCreatures[x].opportunityAttack(creature);
          }
        }
      }
    }
    return false;
  }
  
  public void makeInvisible(Creature creature)
  {
    Iterator<VirtualZone> it;
    if (this.watchers != null) {
      for (it = this.watchers.iterator(); it.hasNext();)
      {
        VirtualZone watchingZone = (VirtualZone)it.next();
        watchingZone.makeInvisible(creature);
      }
    }
  }
  
  public void makeVisible(Creature creature)
    throws NoSuchCreatureException, NoSuchPlayerException
  {
    if (this.watchers != null) {
      for (VirtualZone vz : getWatchers()) {
        try
        {
          vz.addCreature(creature.getWurmId(), false);
        }
        catch (Exception e)
        {
          logger.log(Level.WARNING, e.getMessage(), e);
        }
      }
    }
  }
  
  public void makeInvisible(Item item)
  {
    Iterator<VirtualZone> it;
    if (this.watchers != null) {
      for (it = this.watchers.iterator(); it.hasNext();)
      {
        VirtualZone watchingZone = (VirtualZone)it.next();
        watchingZone.removeItem(item);
      }
    }
  }
  
  public void makeVisible(Item item)
  {
    Iterator<VirtualZone> it;
    if (this.watchers != null) {
      for (it = this.watchers.iterator(); it.hasNext();)
      {
        VirtualZone watchingZone = (VirtualZone)it.next();
        watchingZone.addItem(item, this, -10L, true);
      }
    }
  }
  
  private VolaTile getCaveTile()
  {
    try
    {
      Zone z = Zones.getZone(this.tilex, this.tiley, false);
      return z.getOrCreateTile(this.tilex, this.tiley);
    }
    catch (NoSuchZoneException localNoSuchZoneException)
    {
      logger.log(Level.WARNING, "No cave tile for " + this.tilex + ", " + this.tiley);
    }
    return this;
  }
  
  private VolaTile getSurfaceTile()
  {
    try
    {
      Zone z = Zones.getZone(this.tilex, this.tiley, true);
      return z.getOrCreateTile(this.tilex, this.tiley);
    }
    catch (NoSuchZoneException localNoSuchZoneException)
    {
      logger.log(Level.WARNING, "No surface tile for " + this.tilex + ", " + this.tiley);
    }
    return this;
  }
  
  public void addItem(Item item, boolean moving, boolean starting)
  {
    addItem(item, moving, -10L, starting);
  }
  
  public void addItem(Item item, boolean moving, long creatureId, boolean starting)
  {
    if (this.inactive)
    {
      logger.log(Level.WARNING, "adding " + item.getName() + " to inactive tile " + this.tilex + "," + this.tiley + " surf=" + this.surfaced + " itemsurf=" + item
        .isOnSurface(), new Exception());
      logger.log(Level.WARNING, "The zone " + this.zone.id + " covers " + this.zone.startX + ", " + this.zone.startY + " to " + this.zone.endX + "," + this.zone.endY);
    }
    if (item.hidden) {
      return;
    }
    if ((this.isTransition) && (this.surfaced) && (!item.isVehicle()))
    {
      if (logger.isLoggable(Level.FINEST)) {
        logger.finest("Adding " + item.getName() + " to cave level instead.");
      }
      boolean stayOnSurface = false;
      if (Zones.getTextureForTile(this.tilex, this.tiley, 0) != Tiles.Tile.TILE_HOLE.id) {
        stayOnSurface = true;
      }
      if (!stayOnSurface)
      {
        getCaveTile().addItem(item, moving, starting);
        return;
      }
    }
    if (item.isTileAligned())
    {
      item.setPosXY((this.tilex << 2) + 2, (this.tiley << 2) + 2);
      item.setOwnerId(-10L);
      if (item.isFence()) {
        if (isOnSurface())
        {
          int offz = 0;
          try
          {
            offz = (int)((item.getPosZ() - Zones.calculateHeight(item.getPosX(), item.getPosY(), this.surfaced)) / 10.0F);
          }
          catch (NoSuchZoneException nsz)
          {
            logger.log(Level.WARNING, "Dropping fence item outside zones.");
          }
          float rot = Creature.normalizeAngle(item.getRotation());
          if ((rot >= 45.0F) && (rot < 135.0F))
          {
            VolaTile next = Zones.getOrCreateTile(this.tilex + 1, this.tiley, this.surfaced);
            next.addFence(new TempFence(StructureConstantsEnum.FENCE_SIEGEWALL, this.tilex + 1, this.tiley, offz, item, Tiles.TileBorderDirection.DIR_DOWN, next
              .getZone().getId(), getLayer()));
          }
          else if ((rot >= 135.0F) && (rot < 225.0F))
          {
            VolaTile next = Zones.getOrCreateTile(this.tilex, this.tiley + 1, this.surfaced);
            next.addFence(new TempFence(StructureConstantsEnum.FENCE_SIEGEWALL, this.tilex, this.tiley + 1, offz, item, Tiles.TileBorderDirection.DIR_HORIZ, next
              .getZone().getId(), getLayer()));
          }
          else if ((rot >= 225.0F) && (rot < 315.0F))
          {
            addFence(new TempFence(StructureConstantsEnum.FENCE_SIEGEWALL, this.tilex, this.tiley, offz, item, Tiles.TileBorderDirection.DIR_DOWN, 
              getZone().getId(), getLayer()));
          }
          else
          {
            addFence(new TempFence(StructureConstantsEnum.FENCE_SIEGEWALL, this.tilex, this.tiley, offz, item, Tiles.TileBorderDirection.DIR_HORIZ, 
              getZone().getId(), getLayer()));
          }
        }
      }
    }
    else if ((item.getTileX() != this.tilex) || (item.getTileY() != this.tiley))
    {
      putRandomOnTile(item);
      item.setOwnerId(-10L);
    }
    if (!this.surfaced) {
      if (Tiles.isSolidCave(Tiles.decodeType(Server.caveMesh.getTile(this.tilex, this.tiley))))
      {
        if (!getSurfaceTile().isTransition)
        {
          getSurfaceTile().addItem(item, moving, creatureId, starting);
          logger.log(Level.INFO, "adding " + item.getName() + " in rock at " + this.tilex + ", " + this.tiley + " ");
        }
        return;
      }
    }
    item.setZoneId(this.zone.getId(), this.surfaced);
    if ((!starting) && (!item.getTemplate().hovers())) {
      item.updatePosZ(this);
    }
    if (this.vitems == null) {
      this.vitems = new VolaTileItems();
    }
    if (this.vitems.addItem(item, starting))
    {
      if (item.getTemplateId() == 726) {
        Zones.addDuelRing(item);
      }
      String modelname;
      Iterator<VirtualZone> it;
      boolean onGroundLevel;
      if (!item.isDecoration())
      {
        Item pile = this.vitems.getPileItem(item.getFloorLevel());
        if ((this.vitems.checkIfCreatePileItem(item.getFloorLevel())) || (pile != null))
        {
          if (pile == null)
          {
            pile = createPileItem(item, starting);
            this.vitems.addPileItem(pile);
          }
          pile.insertItem(item, true);
          int data = pile.getData1();
          if ((data != -1) && (item.getTemplateId() != data))
          {
            pile.setData1(-1);
            pile.setName(pile.getTemplate().getName());
            modelname = pile.getTemplate().getModelName().replaceAll(" ", "") + "unknown.";
            if (this.watchers != null) {
              for (it = this.watchers.iterator(); it.hasNext();) {
                ((VirtualZone)it.next()).renameItem(pile, pile.getName(), modelname);
              }
            }
          }
        }
        else if (this.watchers != null)
        {
          onGroundLevel = true;
          if (item.getFloorLevel() > 0) {
            onGroundLevel = false;
          } else if (getFloors(0, 0).length > 0) {
            onGroundLevel = false;
          }
          modelname = getWatchers();it = modelname.length;
          for (Iterator<VirtualZone> localIterator1 = 0; localIterator1 < it; localIterator1++)
          {
            VirtualZone vz = modelname[localIterator1];
            try
            {
              if (vz.isVisible(item, this)) {
                vz.addItem(item, this, creatureId, onGroundLevel);
              }
            }
            catch (Exception e)
            {
              logger.log(Level.WARNING, e.getMessage(), e);
            }
          }
        }
      }
      else if (this.watchers != null)
      {
        boolean onGroundLevel = true;
        if (item.getFloorLevel() > 0) {
          onGroundLevel = false;
        } else if (getFloors(0, 0).length > 0) {
          onGroundLevel = false;
        }
        onGroundLevel = getWatchers();modelname = onGroundLevel.length;
        for (it = 0; it < modelname; it++)
        {
          VirtualZone vz = onGroundLevel[it];
          try
          {
            if (vz.isVisible(item, this)) {
              vz.addItem(item, this, creatureId, onGroundLevel);
            }
          }
          catch (Exception e)
          {
            logger.log(Level.WARNING, e.getMessage(), e);
          }
        }
      }
      if (item.isDomainItem()) {
        Zones.addAltar(item, moving);
      }
      if (((item.getTemplateId() == 1175) || (item.getTemplateId() == 1239)) && 
        (item.getAuxData() > 0)) {
        Zones.addHive(item, moving);
      }
      if ((item.getTemplateId() == 939) || (item.isEnchantedTurret())) {
        Zones.addTurret(item, moving);
      }
      if (item.isEpicTargetItem()) {
        EpicTargetItems.addRitualTargetItem(item);
      }
      if ((this.village != null) && (item.getTemplateId() == 757)) {
        this.village.addBarrel(item);
      }
    }
    else
    {
      item.setZoneId(this.zone.getId(), this.surfaced);
      if (!item.deleted) {
        logger.log(Level.WARNING, "tile already contained item " + item.getName() + " (ID: " + item.getWurmId() + ") at " + this.tilex + ", " + this.tiley, new Exception());
      }
    }
  }
  
  public void updatePile(Item pile)
  {
    checkIfRenamePileItem(pile);
  }
  
  protected void removeItem(Item item, boolean moving)
  {
    if (this.vitems != null) {
      if (!this.vitems.isEmpty())
      {
        if (item.getTemplateId() == 726) {
          Zones.removeDuelRing(item);
        }
        Item pileItem = this.vitems.getPileItem(item.getFloorLevel());
        if ((pileItem != null) && (item.getWurmId() == pileItem.getWurmId()))
        {
          this.vitems.removePileItem(item.getFloorLevel());
          if (this.vitems.isEmpty())
          {
            this.vitems.destroy(this);
            this.vitems = null;
          }
        }
        else if (this.vitems.removeItem(item))
        {
          this.vitems.destroy(this);
          
          this.vitems = null;
          if (pileItem != null) {
            destroyPileItem(pileItem.getFloorLevel());
          }
        }
        else if ((pileItem != null) && (this.vitems.checkIfRemovePileItem(pileItem.getFloorLevel())))
        {
          if (!moving) {
            destroyPileItem(pileItem.getFloorLevel());
          }
        }
        else if ((!item.isDecoration()) && (pileItem != null))
        {
          checkIfRenamePileItem(pileItem);
        }
        if (item.isDomainItem()) {
          Zones.removeAltar(item, moving);
        }
        if ((item.getTemplateId() == 1175) || (item.getTemplateId() == 1239)) {
          Zones.removeHive(item, moving);
        }
        if ((item.getTemplateId() == 939) || (item.isEnchantedTurret())) {
          Zones.removeTurret(item, moving);
        }
        if (item.isEpicTargetItem()) {
          EpicTargetItems.removeRitualTargetItem(item);
        }
        if (item.isKingdomMarker()) {
          if (item.getTemplateId() != 328) {
            Kingdoms.destroyTower(item);
          }
        }
        if (item.getTemplateId() == 521)
        {
          this.zone.creatureSpawn = null;
          Zone.spawnPoints -= 1;
        }
        if ((this.village != null) && (item.getTemplateId() == 757)) {
          this.village.removeBarrel(item);
        }
      }
      else
      {
        Item pileItem = this.vitems.getPileItem(item.getFloorLevel());
        if ((pileItem != null) && (item.getWurmId() == pileItem.getWurmId())) {
          this.vitems.removePileItem(item.getFloorLevel());
        } else if ((pileItem != null) && (this.vitems.checkIfRemovePileItem(item.getFloorLevel()))) {
          destroyPileItem(item.getFloorLevel());
        }
      }
    }
    if (item.isFence())
    {
      int offz = 0;
      try
      {
        offz = (int)((item.getPosZ() - Zones.calculateHeight(item.getPosX(), item.getPosY(), item
          .isOnSurface())) / 10.0F);
      }
      catch (NoSuchZoneException nsz)
      {
        logger.log(Level.WARNING, "Dropping fence item outside zones.");
      }
      float rot = Creature.normalizeAngle(item.getRotation());
      if ((rot >= 45.0F) && (rot < 135.0F))
      {
        VolaTile next = Zones.getOrCreateTile(this.tilex + 1, this.tiley, 
          (item.isOnSurface()) || (this.isTransition));
        next.removeFence(new TempFence(StructureConstantsEnum.FENCE_SIEGEWALL, this.tilex + 1, this.tiley, offz, item, Tiles.TileBorderDirection.DIR_DOWN, next
          .getZone().getId(), Math.max(0, next.getLayer())));
      }
      else if ((rot >= 135.0F) && (rot < 225.0F))
      {
        VolaTile next = Zones.getOrCreateTile(this.tilex, this.tiley + 1, 
          (item.isOnSurface()) || (this.isTransition));
        next.removeFence(new TempFence(StructureConstantsEnum.FENCE_SIEGEWALL, this.tilex, this.tiley + 1, offz, item, Tiles.TileBorderDirection.DIR_HORIZ, next
          .getZone().getId(), Math.max(0, next.getLayer())));
      }
      else if ((rot >= 225.0F) && (rot < 315.0F))
      {
        removeFence(new TempFence(StructureConstantsEnum.FENCE_SIEGEWALL, this.tilex, this.tiley, offz, item, Tiles.TileBorderDirection.DIR_DOWN, 
          getZone().getId(), Math.max(0, getLayer())));
      }
      else
      {
        removeFence(new TempFence(StructureConstantsEnum.FENCE_SIEGEWALL, this.tilex, this.tiley, offz, item, Tiles.TileBorderDirection.DIR_HORIZ, 
          getZone().getId(), Math.max(0, getLayer())));
      }
    }
    sendRemoveItem(item, moving);
    if (!moving) {
      item.setZoneId(-10, this.surfaced);
    }
  }
  
  private void checkIfRenamePileItem(Item pileItem)
  {
    if (pileItem.getData1() == -1)
    {
      int itid = -1;
      
      byte material = 0;
      boolean multipleMaterials = false;
      for (Item item : pileItem.getItems())
      {
        if (!multipleMaterials) {
          if (item.getMaterial() != material) {
            if (material == 0)
            {
              material = item.getMaterial();
            }
            else
            {
              material = 0;
              multipleMaterials = true;
            }
          }
        }
        if (itid == -1)
        {
          itid = item.getTemplateId();
        }
        else if (itid != item.getTemplateId())
        {
          itid = -1;
          break;
        }
      }
      if (itid != -1)
      {
        String name;
        String modelname;
        Iterator<VirtualZone> it;
        try
        {
          name = pileItem.getTemplate().getName();
          pileItem.setData1(itid);
          ItemTemplate template = ItemTemplateFactory.getInstance().getTemplate(itid);
          String tname = template.getName();
          name = "Pile of " + template.sizeString + tname;
          
          pileItem.setMaterial(material);
          StringBuilder build = new StringBuilder();
          build.append(pileItem.getTemplate().getModelName());
          build.append(tname);
          build.append(".");
          build.append(MaterialUtilities.getMaterialString(pileItem.getMaterial()));
          modelname = build.toString().replaceAll(" ", "").trim();
          pileItem.setName(name);
          if (this.watchers != null) {
            for (it = this.watchers.iterator(); it.hasNext();) {
              ((VirtualZone)it.next()).renameItem(pileItem, name, modelname);
            }
          }
        }
        catch (NoSuchTemplateException localNoSuchTemplateException1) {}
      }
    }
  }
  
  private void sendRemoveItem(Item item, boolean moving)
  {
    if (this.watchers != null) {
      for (VirtualZone vz : getWatchers()) {
        try
        {
          if ((!moving) || (!vz.isVisible(item, this))) {
            vz.removeItem(item);
          }
        }
        catch (Exception e)
        {
          logger.log(Level.WARNING, e.getMessage(), e);
        }
      }
    }
  }
  
  public final void sendSetBridgeId(Creature creature, long bridgeId, boolean sendToSelf)
  {
    if (this.watchers != null) {
      for (VirtualZone vz : getWatchers()) {
        try
        {
          if (sendToSelf)
          {
            if (creature.getWurmId() == vz.getWatcher().getWurmId()) {
              vz.sendBridgeId(-1L, bridgeId);
            } else if (creature.isVisibleTo(vz.getWatcher())) {
              vz.sendBridgeId(creature.getWurmId(), bridgeId);
            }
          }
          else if (creature.getWurmId() != vz.getWatcher().getWurmId()) {
            if (creature.isVisibleTo(vz.getWatcher())) {
              vz.sendBridgeId(creature.getWurmId(), bridgeId);
            }
          }
        }
        catch (Exception e)
        {
          logger.log(Level.WARNING, e.getMessage(), e);
        }
      }
    }
  }
  
  public final void sendSetBridgeId(Item item, long bridgeId)
  {
    if (this.watchers != null) {
      for (VirtualZone vz : getWatchers()) {
        try
        {
          if (vz.isVisible(item, this)) {
            vz.sendBridgeId(item.getWurmId(), bridgeId);
          }
        }
        catch (Exception e)
        {
          logger.log(Level.WARNING, e.getMessage(), e);
        }
      }
    }
  }
  
  public void removeWall(Wall wall, boolean silent)
  {
    if (wall != null)
    {
      if (this.walls != null)
      {
        this.walls.remove(wall);
        if (this.walls.size() == 0) {
          this.walls = null;
        }
      }
      if ((this.watchers != null) && (!silent)) {
        for (VirtualZone vz : getWatchers()) {
          try
          {
            vz.removeWall(this.structure.getWurmId(), wall);
          }
          catch (Exception e)
          {
            logger.log(Level.WARNING, e.getMessage(), e);
          }
        }
      }
    }
  }
  
  boolean removeEffect(Effect effect)
  {
    boolean removed = false;
    if ((this.effects != null) && (this.effects.contains(effect)))
    {
      this.effects.remove(effect);
      Iterator<VirtualZone> it;
      if (this.watchers != null) {
        for (it = this.watchers.iterator(); it.hasNext();) {
          ((VirtualZone)it.next()).removeEffect(effect);
        }
      }
      if (this.effects.size() == 0) {
        this.effects = null;
      }
      removed = true;
    }
    return removed;
  }
  
  public void creatureMoved(long creatureId, int diffX, int diffY, int diffZ, int diffTileX, int diffTileY)
    throws NoSuchCreatureException, NoSuchPlayerException
  {
    creatureMoved(creatureId, diffX, diffY, diffZ, diffTileX, diffTileY, false);
  }
  
  public void creatureMoved(long creatureId, int diffX, int diffY, int diffZ, int diffTileX, int diffTileY, boolean passenger)
    throws NoSuchCreatureException, NoSuchPlayerException
  {
    Creature creature = Server.getInstance().getCreature(creatureId);
    
    int tileX = this.tilex + diffTileX;
    int tileY = this.tiley + diffTileY;
    boolean changedLevel = false;
    boolean godown;
    VolaTile nextTile;
    if ((diffTileX != 0) || (diffTileY != 0))
    {
      if (!creature.isPlayer())
      {
        boolean following = (creature.getLeader() != null) || (creature.isRidden()) || (creature.getHitched() != null);
        godown = false;
        if ((this.surfaced) && (following)) {
          if ((creature.isRidden()) || (creature.getHitched() != null))
          {
            if (creature.getHitched() != null)
            {
              Creature rider = Server.getInstance().getCreature(creature.getHitched().pilotId);
              if (!rider.isOnSurface()) {
                godown = true;
              }
            }
            else
            {
              try
              {
                if (creature.getMountVehicle() != null)
                {
                  Creature rider = Server.getInstance().getCreature(creature.getMountVehicle().pilotId);
                  if (!rider.isOnSurface()) {
                    godown = true;
                  }
                }
                else
                {
                  logger.log(Level.WARNING, "Mount Vehicle is null for ridden " + creature.getWurmId());
                }
              }
              catch (NoSuchCreatureException localNoSuchCreatureException) {}catch (NoSuchPlayerException localNoSuchPlayerException) {}
            }
          }
          else if (creature.getLeader() != null) {
            if (!creature.getLeader().isOnSurface()) {
              godown = true;
            }
          }
        }
        if (!this.surfaced)
        {
          if ((this.isTransition) && (Tiles.isSolidCave(Tiles.decodeType(Server.caveMesh.getTile(tileX, tileY)))))
          {
            changedLevel = true;
            creature.getStatus().setLayer(0);
          }
          else if (creature.getLeader() != null)
          {
            if (creature.getLeader().isOnSurface()) {
              if (!this.isTransition)
              {
                changedLevel = true;
                creature.getStatus().setLayer(0);
              }
            }
          }
        }
        else if ((Tiles.decodeType(Server.surfaceMesh.getTile(tileX, tileY)) == Tiles.Tile.TILE_HOLE.id) || ((godown) && 
          (Tiles.isMineDoor(Tiles.decodeType(Server.surfaceMesh.getTile(tileX, tileY))))))
        {
          changedLevel = true;
          creature.getStatus().setLayer(-1);
        }
        nextTile = Zones.getTileOrNull(tileX, tileY, isOnSurface());
        if (nextTile != null)
        {
          BridgePart bp;
          int ht;
          if ((nextTile.getStructure() != null) && (creature.getBridgeId() != nextTile.getStructure().getWurmId()))
          {
            if (nextTile.getBridgeParts().length > 0) {
              for (bp : nextTile.getBridgeParts())
              {
                ht = Math.max(0, creature.getPosZDirts());
                if (Math.abs(ht - bp.getHeightOffset()) < 25) {
                  if (bp.hasAnExit()) {
                    creature.setBridgeId(nextTile.structure.getWurmId());
                  }
                }
              }
            }
          }
          else if ((creature.getBridgeId() > 0L) && (
            (nextTile.getStructure() == null) || 
            (nextTile.getStructure().getWurmId() != creature.getBridgeId())))
          {
            boolean leave = true;
            BridgePart[] parts = getBridgeParts();
            if (parts != null) {
              for (BridgePart bp : parts) {
                if (bp.isFinished()) {
                  if ((bp.getDir() == 0) || (bp.getDir() == 4))
                  {
                    if (getTileY() == nextTile.getTileY()) {
                      leave = false;
                    }
                  }
                  else if (getTileX() == nextTile.getTileX()) {
                    leave = false;
                  }
                }
              }
            }
            if (leave) {
              creature.setBridgeId(-10L);
            }
          }
        }
      }
      if ((!changedLevel) && (this.zone.covers(tileX, tileY)))
      {
        VolaTile newTile = this.zone.getOrCreateTile(tileX, tileY);
        newTile.addCreature(creature, diffZ);
      }
      else
      {
        try
        {
          this.zone.removeCreature(creature, changedLevel, false);
          
          Zone newZone = Zones.getZone(tileX, tileY, creature.isOnSurface());
          newZone.addCreature(creature.getWurmId());
        }
        catch (NoSuchZoneException sex)
        {
          logger.log(Level.INFO, sex.getMessage() + " this tile at " + this.tilex + "," + this.tiley + ", diff=" + diffTileX + ", " + diffTileY, sex);
        }
      }
      if (!passenger) {
        this.zone.createTrack(creature, this.tilex, this.tiley, diffTileX, diffTileY);
      }
    }
    if (this.isTransition)
    {
      if (!passenger) {
        updateNeighbourTileDoors(creature, this.tilex, this.tiley);
      }
    }
    else if (!passenger) {
      doorCreatureMoved(creature, diffTileX, diffTileY);
    }
    if ((!changedLevel) && (!passenger))
    {
      sex = getWatchers();godown = sex.length;
      for (nextTile = 0; nextTile < godown; nextTile++)
      {
        VirtualZone vz = sex[nextTile];
        try
        {
          if ((diffZ / 10 <= 127) && (diffZ / 10 >= -128))
          {
            if (vz.creatureMoved(creatureId, diffX, diffY, diffZ / 10, diffTileX, diffTileY))
            {
              logger.log(Level.INFO, "Forcibly removing watcher " + vz);
              removeWatcher(vz);
            }
          }
          else
          {
            if (logger.isLoggable(Level.FINEST)) {
              logger.finest(creature.getName() + " moved more than byte max (" + 127 + ") or min (" + -128 + ") in z: " + diffZ + " at " + this.tilex + ", " + this.tiley + " surfaced=" + 
              
                isOnSurface());
            }
            makeInvisible(creature);
            makeVisible(creature);
          }
        }
        catch (Exception ex)
        {
          logger.log(Level.WARNING, "Exception when " + creature.getName() + " moved at " + this.tilex + ", " + this.tiley + " tile surf=" + 
            isOnSurface() + " cret onsurf=" + creature.isOnSurface() + ": ", ex);
        }
      }
    }
    if (((creature instanceof Player)) && (!passenger) && (creature.getBridgeId() != -10L)) {
      if ((getStructure() != null) && (getStructure().isTypeBridge())) {
        getStructure().setWalkedOnBridge(System.currentTimeMillis());
      }
    }
  }
  
  private static void updateNeighbourTileDoors(Creature creature, int tilex, int tiley)
  {
    if (Tiles.isSolidCave(Tiles.decodeType(Server.caveMesh.getTile(tilex, tiley - 1))))
    {
      VolaTile newTile = Zones.getOrCreateTile(tilex, tiley - 1, true);
      newTile.getSurfaceTile().doorCreatureMoved(creature, 0, 0);
    }
    else
    {
      VolaTile newTile = Zones.getOrCreateTile(tilex, tiley - 1, false);
      newTile.getCaveTile().doorCreatureMoved(creature, 0, 0);
    }
    if (Tiles.isSolidCave(Tiles.decodeType(Server.caveMesh.getTile(tilex + 1, tiley))))
    {
      VolaTile newTile = Zones.getOrCreateTile(tilex + 1, tiley, true);
      newTile.getSurfaceTile().doorCreatureMoved(creature, 0, 0);
    }
    else
    {
      VolaTile newTile = Zones.getOrCreateTile(tilex + 1, tiley, false);
      newTile.getCaveTile().doorCreatureMoved(creature, 0, 0);
    }
    if (Tiles.isSolidCave(Tiles.decodeType(Server.caveMesh.getTile(tilex, tiley + 1))))
    {
      VolaTile newTile = Zones.getOrCreateTile(tilex, tiley + 1, true);
      newTile.getSurfaceTile().doorCreatureMoved(creature, 0, 0);
    }
    else
    {
      VolaTile newTile = Zones.getOrCreateTile(tilex, tiley + 1, false);
      newTile.getCaveTile().doorCreatureMoved(creature, 0, 0);
    }
    if (Tiles.isSolidCave(Tiles.decodeType(Server.caveMesh.getTile(tilex - 1, tiley))))
    {
      VolaTile newTile = Zones.getOrCreateTile(tilex - 1, tiley, true);
      newTile.getSurfaceTile().doorCreatureMoved(creature, 0, 0);
    }
    else
    {
      VolaTile newTile = Zones.getOrCreateTile(tilex - 1, tiley, false);
      newTile.getCaveTile().doorCreatureMoved(creature, 0, 0);
    }
  }
  
  private void doorCreatureMoved(Creature creature, int diffTileX, int diffTileY)
  {
    Iterator<Door> it;
    if (this.doors != null) {
      for (it = this.doors.iterator(); it.hasNext();)
      {
        Door door = (Door)it.next();
        door.creatureMoved(creature, diffTileX, diffTileY);
      }
    }
  }
  
  @Nonnull
  public Creature[] getCreatures()
  {
    if (this.creatures != null) {
      return (Creature[])this.creatures.toArray(new Creature[this.creatures.size()]);
    }
    return emptyCreatures;
  }
  
  public Item[] getItems()
  {
    if (this.vitems != null) {
      return this.vitems.getAllItemsAsArray();
    }
    return emptyItems;
  }
  
  final Effect[] getEffects()
  {
    if (this.effects != null) {
      return (Effect[])this.effects.toArray(new Effect[this.effects.size()]);
    }
    return emptyEffects;
  }
  
  public VirtualZone[] getWatchers()
  {
    if (this.watchers != null) {
      return (VirtualZone[])this.watchers.toArray(new VirtualZone[this.watchers.size()]);
    }
    return emptyWatchers;
  }
  
  public final int getMaxFloorLevel()
  {
    if ((!this.surfaced) || (this.isTransition)) {
      return 3;
    }
    int toRet = 0;
    if (this.floors != null)
    {
      toRet = 1;
      for (Floor f : this.floors) {
        if (f.getFloorLevel() > toRet) {
          toRet = f.getFloorLevel();
        }
      }
    }
    if (this.bridgeParts != null)
    {
      toRet = 1;
      for (BridgePart b : this.bridgeParts) {
        if (b.getFloorLevel() > toRet) {
          toRet = b.getFloorLevel();
        }
      }
    }
    return toRet;
  }
  
  public final int getDropFloorLevel(int maxFloorLevel)
  {
    int toRet = 0;
    if (this.floors != null) {
      for (Floor f : this.floors) {
        if (f.isSolid())
        {
          if (f.getFloorLevel() == maxFloorLevel) {
            return maxFloorLevel;
          }
          if ((f.getFloorLevel() < maxFloorLevel) && (f.getFloorLevel() > toRet)) {
            toRet = f.getFloorLevel();
          }
        }
      }
    }
    return toRet;
  }
  
  void sendRemoveItem(Item item)
  {
    for (VirtualZone vz : getWatchers()) {
      try
      {
        vz.removeItem(item);
      }
      catch (Exception e)
      {
        logger.log(Level.WARNING, e.getMessage(), e);
      }
    }
  }
  
  public void change()
  {
    checkTransition();
    Item stumpToDestroy = null;
    Seat seat;
    boolean onGroundLevel;
    if (this.vitems != null) {
      for (Item item : this.vitems.getAllItemsAsArray()) {
        if ((item.getTileX() == this.tilex) && (item.getTileY() == this.tiley) && (item.getBridgeId() == -10L))
        {
          boolean ok = true;
          if (item.isVehicle())
          {
            Vehicle vehic = Vehicles.getVehicle(item);
            if (vehic.getHitched().length > 0) {
              ok = false;
            } else {
              for (seat : vehic.getSeats()) {
                if (seat.occupant > 0L) {
                  ok = false;
                }
              }
            }
          }
          if (item.getTemplateId() == 731)
          {
            stumpToDestroy = item;
          }
          else
          {
            int oldFloorLevel = item.getFloorLevel();
            item.updatePosZ(this);
            Item pileItem = this.vitems.getPileItem(item.getFloorLevel());
            if (oldFloorLevel != item.getFloorLevel())
            {
              this.vitems.moveToNewFloorLevel(item, oldFloorLevel);
              logger.log(Level.INFO, item
                .getName() + " moving from " + oldFloorLevel + " fl=" + item.getFloorLevel());
            }
            if (ok) {
              if ((pileItem == null) || (item.isDecoration()))
              {
                onGroundLevel = true;
                if (item.getFloorLevel() > 0) {
                  onGroundLevel = false;
                } else if (getFloors(0, 0).length > 0) {
                  onGroundLevel = false;
                }
                VirtualZone[] arrayOfVirtualZone2 = getWatchers();seat = arrayOfVirtualZone2.length;
                for (Seat localSeat2 = 0; localSeat2 < seat; localSeat2++)
                {
                  VirtualZone vz = arrayOfVirtualZone2[localSeat2];
                  try
                  {
                    vz.removeItem(item);
                    if (vz.isVisible(item, this)) {
                      vz.addItem(item, this, onGroundLevel);
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
      }
    }
    Item pileItem;
    if (this.vitems != null) {
      for (pileItem : this.vitems.getPileItems()) {
        if (pileItem.getBridgeId() == -10L)
        {
          int oldFloorLevel = pileItem.getFloorLevel();
          pileItem.updatePosZ(this);
          boolean destroy = false;
          if (pileItem.getFloorLevel() != oldFloorLevel) {
            destroy = this.vitems.movePileItemToNewFloorLevel(pileItem, oldFloorLevel);
          }
          if (!destroy)
          {
            boolean onGroundLevel = true;
            if (pileItem.getFloorLevel() > 0) {
              onGroundLevel = false;
            } else if (getFloors(0, 0).length > 0) {
              onGroundLevel = false;
            }
            VirtualZone[] arrayOfVirtualZone1 = getWatchers();Seat localSeat1 = arrayOfVirtualZone1.length;
            for (seat = 0; seat < localSeat1; seat++)
            {
              VirtualZone vz = arrayOfVirtualZone1[seat];
              try
              {
                vz.removeItem(pileItem);
                if (vz.isVisible(pileItem, this)) {
                  vz.addItem(pileItem, this, onGroundLevel);
                }
              }
              catch (Exception e)
              {
                logger.log(Level.WARNING, e.getMessage(), e);
              }
            }
          }
          else
          {
            destroyPileItem(pileItem);
          }
        }
      }
    }
    Object it;
    if (this.effects != null) {
      for (it = this.effects.iterator(); ((Iterator)it).hasNext();)
      {
        Effect effect = (Effect)((Iterator)it).next();
        if ((effect.getTileX() == this.tilex) && (effect.getTileY() == this.tiley))
        {
          try
          {
            if (this.structure == null) {
              if (this.isTransition)
              {
                effect.setPosZ(Zones.calculateHeight(effect.getPosX(), effect.getPosY(), false));
              }
              else
              {
                long owner = effect.getOwner();
                bridgeId = -10L;
                if (WurmId.getType(owner) == 2) {
                  try
                  {
                    Item i = Items.getItem(owner);
                    bridgeId = i.onBridge();
                  }
                  catch (NoSuchItemException localNoSuchItemException1) {}
                } else if (WurmId.getType(owner) == 1) {
                  try
                  {
                    Creature c = Creatures.getInstance().getCreature(owner);
                    bridgeId = c.getBridgeId();
                  }
                  catch (NoSuchCreatureException localNoSuchCreatureException2) {}
                } else if (WurmId.getType(owner) == 0) {
                  try
                  {
                    Player p = Players.getInstance().getPlayer(owner);
                    bridgeId = p.getBridgeId();
                  }
                  catch (NoSuchPlayerException localNoSuchPlayerException2) {}
                }
                float height = Zones.calculatePosZ(effect.getPosX(), effect.getPosY(), this, this.surfaced, false, effect
                  .getPosZ(), null, bridgeId);
                effect.setPosZ(height);
              }
            }
          }
          catch (NoSuchZoneException nsz)
          {
            logger.log(Level.WARNING, effect.getId() + " moved out of zone.");
          }
          nsz = getWatchers();pileItem = nsz.length;
          for (long bridgeId = 0; bridgeId < pileItem; bridgeId++)
          {
            VirtualZone vz = nsz[bridgeId];
            try
            {
              vz.removeEffect(effect);
              vz.addEffect(effect, false);
            }
            catch (Exception e)
            {
              logger.log(Level.WARNING, e.getMessage(), e);
            }
          }
        }
      }
    }
    Object it;
    if (this.creatures != null) {
      for (it = this.creatures.iterator(); ((Iterator)it).hasNext();)
      {
        Creature creature = (Creature)((Iterator)it).next();
        if (creature.getBridgeId() == -10L) {
          if (!(creature instanceof Player)) {
            if (creature.isSubmerged())
            {
              creature.submerge();
            }
            else if (creature.getVehicle() < 0L)
            {
              float oldPosZ = creature.getStatus().getPositionZ();
              
              float newPosZ = 1.0F;
              boolean surf = this.surfaced;
              if (this.isTransition) {
                surf = false;
              }
              newPosZ = Zones.calculatePosZ(creature.getPosX(), creature.getPosY(), this, surf, false, creature
                .getPositionZ(), null, creature.getBridgeId());
              creature.setPositionZ(Math.max(-1.25F, newPosZ));
              try
              {
                creature.savePosition(this.zone.id);
              }
              catch (Exception iox)
              {
                logger.log(Level.WARNING, creature.getName() + ": " + iox.getMessage(), iox);
              }
              iox = getWatchers();boolean bool1 = iox.length;
              for (boolean bool2 = false; bool2 < bool1; bool2++)
              {
                VirtualZone vz = iox[bool2];
                try
                {
                  vz.creatureMoved(creature.getWurmId(), 0, 0, (int)((newPosZ - oldPosZ) * 10.0F), 0, 0);
                }
                catch (NoSuchCreatureException nsc)
                {
                  logger.log(Level.INFO, "Creature not found when changing height of tile.", nsc);
                }
                catch (NoSuchPlayerException nsp)
                {
                  logger.log(Level.INFO, "Player not found when changing height of tile.", nsp);
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
    }
    if (stumpToDestroy != null) {
      Items.destroyItem(stumpToDestroy.getWurmId());
    }
  }
  
  public Wall[] getWalls()
  {
    if (this.walls != null) {
      return (Wall[])this.walls.toArray(new Wall[this.walls.size()]);
    }
    return emptyWalls;
  }
  
  public BridgePart[] getBridgeParts()
  {
    if (this.bridgeParts != null) {
      return (BridgePart[])this.bridgeParts.toArray(new BridgePart[this.bridgeParts.size()]);
    }
    return emptyBridgeParts;
  }
  
  public final Set<StructureSupport> getAllSupport()
  {
    if ((this.walls == null) && (this.fences == null) && (this.floors == null)) {
      return emptySupports;
    }
    Set<StructureSupport> toReturn = new HashSet();
    if (this.walls != null) {
      for (Wall w : this.walls) {
        toReturn.add(w);
      }
    }
    if (this.fences != null) {
      toReturn.addAll(this.fences.values());
    }
    if (this.floors != null) {
      toReturn.addAll(this.floors);
    }
    return toReturn;
  }
  
  public Wall[] getWallsForLevel(int floorLevel)
  {
    if (this.walls != null)
    {
      Set<Wall> wallsSet = new HashSet();
      for (Wall w : this.walls) {
        if (w.getFloorLevel() == floorLevel) {
          wallsSet.add(w);
        }
      }
      return (Wall[])wallsSet.toArray(new Wall[wallsSet.size()]);
    }
    return emptyWalls;
  }
  
  public Wall[] getExteriorWalls()
  {
    if (this.walls != null)
    {
      Set<Wall> wallsSet = new HashSet();
      for (Wall w : this.walls) {
        if (!w.isIndoor()) {
          wallsSet.add(w);
        }
      }
      return (Wall[])wallsSet.toArray(new Wall[wallsSet.size()]);
    }
    return emptyWalls;
  }
  
  Wall getWall(long wallId)
    throws NoSuchWallException
  {
    Iterator<Wall> it;
    if (this.walls != null) {
      for (it = this.walls.iterator(); it.hasNext();)
      {
        Wall wall = (Wall)it.next();
        if (wall.getId() == wallId) {
          return wall;
        }
      }
    }
    throw new NoSuchWallException("There are no walls on this tile so cannot find wallid: " + wallId);
  }
  
  Wall getWall(int startX, int startY, int endX, int endY, boolean horizontal)
  {
    Iterator<Wall> it;
    if (this.walls != null) {
      for (it = this.walls.iterator(); it.hasNext();)
      {
        Wall wall = (Wall)it.next();
        if ((wall.getStartX() == startX) && (wall.getStartY() == startY) && (wall.getEndX() == endX) && 
          (wall.getEndY() == endY)) {
          if (wall.isHorizontal() == horizontal) {
            return wall;
          }
        }
      }
    }
    return null;
  }
  
  public Structure getStructure()
  {
    return this.structure;
  }
  
  public void deleteStructure(long wurmStructureId)
  {
    if (this.structure == null) {
      return;
    }
    if (this.structure.getWurmId() != wurmStructureId)
    {
      logger.log(Level.WARNING, "Tried to delete structure " + wurmStructureId + " from VolaTile [" + this.tilex + "," + this.tiley + "] but it was structure " + this.structure
        .getWurmId() + " so nothing was deleted."); return;
    }
    Iterator<Wall> it;
    if (this.walls != null) {
      for (it = this.walls.iterator(); it.hasNext();)
      {
        Wall wall = (Wall)it.next();
        if (wall.getStructureId() == wurmStructureId)
        {
          if ((wall.getType() == StructureTypeEnum.DOOR) || (wall.getType() == StructureTypeEnum.DOUBLE_DOOR) || 
            (wall.getType() == StructureTypeEnum.PORTCULLIS) || (wall.getType() == StructureTypeEnum.CANOPY_DOOR) || 
            (wall.isArched()))
          {
            alld = getDoors();
            for (int x = 0; x < alld.length; x++) {
              try
              {
                if (alld[x].getWall() == wall)
                {
                  alld[x].removeFromTiles();
                  
                  alld[x].delete();
                }
              }
              catch (NoSuchWallException nsw)
              {
                logger.log(Level.WARNING, nsw.getMessage(), nsw);
              }
            }
          }
          wall.delete();
          it.remove();
        }
      }
    }
    Door[] alld;
    Iterator<Floor> it;
    if (this.floors != null) {
      for (it = this.floors.iterator(); it.hasNext();)
      {
        Floor floor = (Floor)it.next();
        if (floor.getStructureId() == wurmStructureId)
        {
          floor.delete();
          it.remove();
          if (floor.isStair()) {
            Stairs.removeStair(hashCode(), floor.getFloorLevel());
          }
        }
      }
    }
    Iterator<BridgePart> it;
    if (this.bridgeParts != null) {
      for (it = this.bridgeParts.iterator(); it.hasNext();)
      {
        bridgepart = (BridgePart)it.next();
        if (bridgepart.getStructureId() == wurmStructureId)
        {
          bridgepart.delete();
          it.remove();
        }
      }
    }
    BridgePart bridgepart;
    Fence fence;
    if (this.fences != null)
    {
      it = getFences();bridgepart = it.length;
      for (alld = 0; alld < bridgepart; alld++)
      {
        fence = it[alld];
        if (fence.getFloorLevel() > 0) {
          fence.destroy();
        }
      }
    }
    VolaTile tw = Zones.getTileOrNull(getTileX() + 1, getTileY(), isOnSurface());
    Fence fence;
    if (tw != null) {
      if (tw.getStructure() == null)
      {
        bridgepart = tw.getFences();alld = bridgepart.length;
        for (fence = 0; fence < alld; fence++)
        {
          fence = bridgepart[fence];
          if (fence.getFloorLevel() > 0) {
            fence.destroy();
          }
        }
      }
    }
    VolaTile ts = Zones.getTileOrNull(getTileX(), getTileY() + 1, isOnSurface());
    if (ts != null) {
      if (ts.getStructure() == null)
      {
        alld = ts.getFences();fence = alld.length;
        for (fence = 0; fence < fence; fence++)
        {
          Fence fence = alld[fence];
          if (fence.getFloorLevel() > 0) {
            fence.destroy();
          }
        }
      }
    }
    Iterator<VirtualZone> it;
    if (this.watchers != null)
    {
      logger.log(Level.INFO, "deleteStructure " + wurmStructureId + " (Watchers  " + this.watchers.size() + ")");
      for (it = this.watchers.iterator(); it.hasNext();)
      {
        lZone = (VirtualZone)it.next();
        lZone.deleteStructure(this.structure);
      }
    }
    VirtualZone lZone;
    if (this.vitems != null)
    {
      it = this.vitems.getPileItems();lZone = it.length;
      for (fence = 0; fence < lZone; fence++)
      {
        Item pileItem = it[fence];
        float pileHeight;
        float tileHeight;
        if (pileItem.getFloorLevel() > 0)
        {
          pileHeight = pileItem.getPosZ();
          tileHeight = 0.0F;
          try
          {
            tileHeight = Zones.calculateHeight(getPosX(), getPosY(), isOnSurface());
          }
          catch (NoSuchZoneException localNoSuchZoneException1) {}
          if (tileHeight != pileHeight) {
            destroyPileItem(pileItem.getFloorLevel());
          }
        }
        else
        {
          pileItem.updatePosZ(this);
          
          pileHeight = getWatchers();tileHeight = pileHeight.length;
          for (localNoSuchZoneException1 = 0; localNoSuchZoneException1 < tileHeight; localNoSuchZoneException1++)
          {
            VirtualZone vz = pileHeight[localNoSuchZoneException1];
            try
            {
              if (vz.isVisible(pileItem, this))
              {
                vz.removeItem(pileItem);
                vz.addItem(pileItem, this, true);
              }
            }
            catch (Exception e)
            {
              logger.log(Level.WARNING, e.getMessage(), e);
            }
          }
        }
      }
      it = this.vitems.getAllItemsAsArray();lZone = it.length;
      for (fence = 0; fence < lZone; fence++)
      {
        Item item = it[fence];
        if (item.getParentId() == -10L)
        {
          item.updatePosZ(this);
          item.updateIfGroundItem();
          item.setOnBridge(-10L);
        }
      }
    }
    if (this.creatures != null) {
      for (Creature c : this.creatures)
      {
        if (!c.isPlayer())
        {
          float oldposz = c.getPositionZ();
          float newPosz = c.calculatePosZ();
          float diffz = oldposz - newPosz;
          c.setPositionZ(newPosz);
          c.moved(0, 0, (int)(diffz * 100.0F), 0, 0);
        }
        else
        {
          c.getCommunicator().setGroundOffset(0, true);
        }
        c.setBridgeId(-10L);
      }
    }
    this.structure = null;
    Iterator<Wall> it;
    if (this.walls != null) {
      for (it = this.walls.iterator(); it.hasNext();)
      {
        Wall wall = (Wall)it.next();
        long sid = wall.getStructureId();
        try
        {
          Structure struct = Structures.getStructure(sid);
          
          this.structure = struct;
          struct.addBuildTile(this, false);
        }
        catch (NoSuchStructureException sns)
        {
          logger.log(Level.WARNING, sns.getMessage(), " for wall " + wall);
        }
        catch (NoSuchZoneException nsz)
        {
          logger.log(Level.INFO, "Out of bounds?: " + nsz.getMessage(), nsz);
        }
      }
    }
  }
  
  private void updateStructureForZone(VirtualZone vzone, Structure _structure, int aTilex, int aTiley)
  {
    vzone.sendStructureWalls(_structure);
  }
  
  public void addStructure(Structure _structure)
  {
    Iterator<VirtualZone> it;
    if (this.structure == null) {
      if (this.watchers != null) {
        for (it = this.watchers.iterator(); it.hasNext();)
        {
          VirtualZone vzone = (VirtualZone)it.next();
          updateStructureForZone(vzone, _structure, this.tilex, this.tiley);
        }
      }
    }
    this.structure = _structure;
  }
  
  public void addBridge(Structure bridge)
  {
    Iterator<VirtualZone> it;
    if (this.structure == null) {
      if (this.watchers != null) {
        for (it = this.watchers.iterator(); it.hasNext();)
        {
          VirtualZone vzone = (VirtualZone)it.next();
          vzone.addStructure(bridge);
        }
      }
    }
    this.structure = bridge;
  }
  
  public void addBuildMarker(Structure _structure)
  {
    Iterator<VirtualZone> it;
    if (this.structure == null) {
      if (this.watchers != null) {
        for (it = this.watchers.iterator(); it.hasNext();)
        {
          VirtualZone vzone = (VirtualZone)it.next();
          vzone.addBuildMarker(_structure, this.tilex, this.tiley);
        }
      }
    }
    this.structure = _structure;
  }
  
  public void setStructureAtLoad(Structure _structure)
  {
    this.structure = _structure;
  }
  
  public void removeBuildMarker(Structure _structure, int _tilex, int _tiley)
  {
    if (this.structure != null)
    {
      Iterator<VirtualZone> it;
      if (this.watchers != null) {
        for (it = this.watchers.iterator(); it.hasNext();)
        {
          VirtualZone vzone = (VirtualZone)it.next();
          vzone.removeBuildMarker(_structure, _tilex, _tiley);
        }
      }
      this.structure = null;
    }
    else
    {
      logger.log(Level.INFO, "Hmm tried to remove buildmarker from a tile that didn't contain it.");
    }
  }
  
  public void finalizeBuildPlan(long oldStructureId, long newStructureId)
  {
    Iterator<VirtualZone> it;
    if (this.structure != null) {
      if (this.watchers != null) {
        for (it = this.watchers.iterator(); it.hasNext();)
        {
          VirtualZone lZone = (VirtualZone)it.next();
          lZone.finalizeBuildPlan(oldStructureId, newStructureId);
        }
      }
    }
  }
  
  public void addWall(StructureTypeEnum type, int x1, int y1, int x2, int y2, float qualityLevel, long structureId, boolean isIndoor)
  {
    if (logger.isLoggable(Level.FINEST)) {
      logger.finest("StructureID: " + structureId + " adding wall at " + x1 + "-" + y1 + "," + x2 + "-" + y2 + ", QL: " + qualityLevel);
    }
    Wall inside = new DbWall(type, this.tilex, this.tiley, x1, y1, x2, y2, qualityLevel, structureId, StructureMaterialEnum.WOOD, isIndoor, 0, getLayer());
    addWall(inside);
    updateWall(inside);
  }
  
  public void addWall(Wall wall)
  {
    if (this.walls == null) {
      this.walls = new HashSet();
    }
    boolean removedOneWall = false;
    for (Wall w : this.walls)
    {
      removedOneWall = false;
      if ((wall.heightOffset == 0) && (w.heightOffset == 0)) {
        if ((wall.x1 == w.x1) && (wall.x2 == w.x2) && (wall.y1 == w.y1) && (wall.y2 == w.y2))
        {
          if (wall.getType().value <= w.getType().value)
          {
            removedOneWall = true;
            break;
          }
          if (w.getType().value < wall.getType().value) {
            toRemove.add(w);
          }
        }
      }
    }
    if (!removedOneWall) {
      this.walls.add(wall);
    }
    if (removedOneWall) {
      logger.log(Level.INFO, "Not adding wall at " + wall.getTileX() + ", " + wall.getTileY() + ", structure: " + this.structure);
    }
    for (Wall torem : toRemove)
    {
      logger.log(Level.INFO, "Deleting wall at " + torem.getTileX() + ", " + torem.getTileY() + ", structure: " + this.structure);
      
      torem.delete();
    }
    toRemove.clear();
  }
  
  public void updateFloor(Floor floor)
  {
    if (this.structure != null)
    {
      if (this.watchers != null) {
        for (VirtualZone vz : getWatchers()) {
          try
          {
            vz.updateFloor(this.structure.getWurmId(), floor);
          }
          catch (Exception e)
          {
            logger.log(Level.WARNING, e.getMessage(), e);
          }
        }
      }
      try
      {
        floor.save();
      }
      catch (IOException iox)
      {
        logger.log(Level.WARNING, "Failed to save structure floor: " + floor.getId() + '.', (Throwable)iox);
      }
      if (floor.isFinished()) {
        if (this.vitems != null) {
          for (Item item : this.vitems.getAllItemsAsArray())
          {
            item.updatePosZ(this);
            item.updateIfGroundItem();
          }
        }
      }
      if (this.vitems != null) {
        for (Item pile : this.vitems.getPileItems())
        {
          pile.updatePosZ(this);
          for (VirtualZone vz : getWatchers()) {
            try
            {
              if (vz.isVisible(pile, this))
              {
                vz.removeItem(pile);
                vz.addItem(pile, this, true);
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
  
  public void updateBridgePart(BridgePart bridgePart)
  {
    if ((!isOnSurface()) && (!Features.Feature.CAVE_BRIDGES.isEnabled()))
    {
      getSurfaceTile().updateBridgePart(bridgePart);
    }
    else if (this.structure != null)
    {
      if (this.watchers != null) {
        for (VirtualZone vz : getWatchers()) {
          try
          {
            vz.updateBridgePart(this.structure.getWurmId(), bridgePart);
          }
          catch (Exception e)
          {
            logger.log(Level.WARNING, e.getMessage(), e);
          }
        }
      }
      try
      {
        bridgePart.save();
      }
      catch (IOException iox)
      {
        logger.log(Level.WARNING, "Failed to save structure bridge part: " + bridgePart.getId() + '.', (Throwable)iox);
      }
      if (bridgePart.getState() != BridgeConstants.BridgeState.COMPLETED.getCode())
      {
        if (this.vitems != null)
        {
          for (Item item : this.vitems.getAllItemsAsArray()) {
            if (item.onBridge() == this.structure.getWurmId())
            {
              item.setOnBridge(-10L);
              for (VirtualZone vz : getWatchers()) {
                try
                {
                  if ((item.getParentId() == -10L) && (vz.isVisible(item, this)))
                  {
                    vz.removeItem(item);
                    item.setPosZ(-3000.0F);
                    vz.addItem(item, this, true);
                  }
                }
                catch (Exception e)
                {
                  logger.log(Level.WARNING, e.getMessage(), e);
                }
              }
            }
          }
          for (Item pile : this.vitems.getPileItems()) {
            if (pile.onBridge() == this.structure.getWurmId())
            {
              pile.setOnBridge(-10L);
              for (VirtualZone vz : getWatchers()) {
                try
                {
                  if (vz.isVisible(pile, this))
                  {
                    pile.setPosZ(-3000.0F);
                    vz.removeItem(pile);
                    vz.addItem(pile, this, true);
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
        if (this.creatures != null) {
          for (iox = this.creatures.iterator(); ((Iterator)iox).hasNext();)
          {
            Creature c = (Creature)((Iterator)iox).next();
            if (c.getBridgeId() == this.structure.getWurmId())
            {
              c.setBridgeId(-10L);
              if (!c.isPlayer())
              {
                float oldposz = c.getPositionZ();
                float newPosz = c.calculatePosZ();
                float diffz = oldposz - newPosz;
                c.setPositionZ(newPosz);
                c.moved(0, 0, (int)(diffz * 100.0F), 0, 0);
              }
            }
          }
        }
      }
    }
  }
  
  public void updateWall(Wall wall)
  {
    if (this.structure != null)
    {
      if (this.watchers != null) {
        for (VirtualZone vz : getWatchers()) {
          try
          {
            vz.updateWall(this.structure.getWurmId(), wall);
          }
          catch (Exception e)
          {
            logger.log(Level.WARNING, e.getMessage(), e);
          }
        }
      }
      if (this.structure.isFinalized()) {
        try
        {
          wall.save();
        }
        catch (IOException iox)
        {
          logger.log(Level.WARNING, "Failed to save structure wall: " + wall.getId() + '.', iox);
        }
      }
    }
  }
  
  void linkTo(VirtualZone aZone, boolean aRemove)
  {
    linkStructureToZone(aZone, aRemove);
    linkFencesToZone(aZone, aRemove);
    linkDoorsToZone(aZone, aRemove);
    linkMineDoorsToZone(aZone, aRemove);
    linkCreaturesToZone(aZone, aRemove);
    linkItemsToZone(aZone, aRemove);
    linkPileToZone(aZone, aRemove);
    linkEffectsToZone(aZone, aRemove);
    linkAreaEffectsToZone(aZone, aRemove);
  }
  
  private void linkAreaEffectsToZone(VirtualZone aZone, boolean aRemove)
  {
    if (aZone.getWatcher().isPlayer())
    {
      AreaSpellEffect ae = getAreaEffect();
      if ((ae != null) && (!aRemove) && (aZone.covers(this.tilex, this.tiley))) {
        aZone.addAreaSpellEffect(ae, true);
      } else if (ae != null) {
        aZone.removeAreaSpellEffect(ae);
      }
    }
  }
  
  private void linkDoorsToZone(VirtualZone aZone, boolean aRemove)
  {
    Iterator<Door> it;
    if (this.doors != null)
    {
      Iterator<Door> it;
      if ((!aRemove) && (aZone.covers(this.tilex, this.tiley))) {
        for (it = this.doors.iterator(); it.hasNext();)
        {
          Door door = (Door)it.next();
          aZone.addDoor(door);
        }
      } else {
        for (it = this.doors.iterator(); it.hasNext();)
        {
          Door door = (Door)it.next();
          aZone.removeDoor(door);
        }
      }
    }
  }
  
  private void linkMineDoorsToZone(VirtualZone aZone, boolean aRemove)
  {
    Iterator<MineDoorPermission> it;
    if (this.mineDoors != null)
    {
      Iterator<MineDoorPermission> it;
      if ((!aRemove) && (aZone.covers(this.tilex, this.tiley))) {
        for (it = this.mineDoors.iterator(); it.hasNext();)
        {
          MineDoorPermission door = (MineDoorPermission)it.next();
          aZone.addMineDoor(door);
        }
      } else {
        for (it = this.mineDoors.iterator(); it.hasNext();)
        {
          MineDoorPermission door = (MineDoorPermission)it.next();
          aZone.removeMineDoor(door);
        }
      }
    }
  }
  
  private void linkFencesToZone(VirtualZone aZone, boolean aRemove)
  {
    if (this.fences != null) {
      if ((!aRemove) && (aZone.covers(this.tilex, this.tiley))) {
        for (Fence f : getFences()) {
          aZone.addFence(f);
        }
      } else {
        for (Fence f : getFences()) {
          aZone.removeFence(f);
        }
      }
    }
  }
  
  protected void linkCreaturesToZone(VirtualZone aZone, boolean aRemove)
  {
    if (this.creatures != null)
    {
      Creature[] crets = getCreatures();
      if ((!aRemove) && (aZone.covers(this.tilex, this.tiley))) {
        for (int x = 0; x < crets.length; x++) {
          if (!crets[x].isDead()) {
            try
            {
              aZone.addCreature(crets[x].getWurmId(), false);
            }
            catch (NoSuchCreatureException cnf)
            {
              this.creatures.remove(crets[x]);
              logger.log(Level.INFO, crets[x].getName() + "," + cnf.getMessage(), cnf);
            }
            catch (NoSuchPlayerException nsp)
            {
              this.creatures.remove(crets[x]);
              logger.log(Level.INFO, crets[x].getName() + "," + nsp.getMessage(), nsp);
            }
          }
        }
      } else {
        for (int x = 0; x < crets.length; x++) {
          try
          {
            aZone.deleteCreature(crets[x], true);
          }
          catch (NoSuchPlayerException nsp)
          {
            logger.log(Level.INFO, crets[x].getName() + "," + nsp.getMessage(), nsp);
          }
          catch (NoSuchCreatureException nsc)
          {
            logger.log(Level.INFO, crets[x].getName() + "," + nsc.getMessage(), nsc);
          }
        }
      }
    }
  }
  
  private void linkPileToZone(VirtualZone aZone, boolean aRemove)
  {
    if (this.vitems != null) {
      for (Item pileItem : this.vitems.getPileItems()) {
        if (pileItem != null) {
          if ((!aRemove) && (aZone.covers(this.tilex, this.tiley)))
          {
            if (aZone.isVisible(pileItem, this))
            {
              boolean onGroundLevel = true;
              if (pileItem.getFloorLevel() > 0) {
                onGroundLevel = false;
              } else if (getFloors(0, 0).length > 0) {
                onGroundLevel = false;
              }
              aZone.addItem(pileItem, this, onGroundLevel);
            }
            else
            {
              aZone.removeItem(pileItem);
            }
          }
          else {
            aZone.removeItem(pileItem);
          }
        }
      }
    }
  }
  
  public byte getKingdom()
  {
    return Zones.getKingdom(this.tilex, this.tiley);
  }
  
  private void linkItemsToZone(VirtualZone aZone, boolean aRemove)
  {
    Item[] lTempItemsLink;
    if (this.vitems != null) {
      if ((!aRemove) && (aZone.covers(this.tilex, this.tiley)))
      {
        lTempItemsLink = this.vitems.getAllItemsAsArray();
        for (int x = 0; x < lTempItemsLink.length; x++)
        {
          Item pileItem = this.vitems.getPileItem(lTempItemsLink[x].getFloorLevel());
          if ((pileItem == null) || (lTempItemsLink[x].isDecoration())) {
            if (aZone.isVisible(lTempItemsLink[x], this))
            {
              boolean onGroundLevel = true;
              if (lTempItemsLink[x].getFloorLevel() > 0) {
                onGroundLevel = false;
              } else if (getFloors(0, 0).length > 0) {
                onGroundLevel = false;
              }
              if (!aZone.addItem(lTempItemsLink[x], this, onGroundLevel)) {
                try
                {
                  Items.getItem(lTempItemsLink[x].getWurmId());
                  removeItem(lTempItemsLink[x], false);
                  Zone z = Zones.getZone(lTempItemsLink[x].getTileX(), lTempItemsLink[x].getTileY(), 
                    isOnSurface());
                  z.addItem(lTempItemsLink[x]);
                  logger.log(Level.INFO, this.tilex + ", " + this.tiley + " removing " + lTempItemsLink[x].getName() + " with id " + lTempItemsLink[x]
                    .getWurmId() + " and added it to " + lTempItemsLink[x]
                    .getTileX() + "," + lTempItemsLink[x].getTileY() + " where it belongs.");
                }
                catch (NoSuchItemException nsi)
                {
                  logger.log(Level.INFO, this.tilex + ", " + this.tiley + " removing " + lTempItemsLink[x].getName() + " with id " + lTempItemsLink[x]
                    .getWurmId() + " since it doesn't belong here.");
                  removeItem(lTempItemsLink[x], false);
                }
                catch (NoSuchZoneException nsz)
                {
                  logger.log(Level.INFO, this.tilex + ", " + this.tiley + " removed " + lTempItemsLink[x].getName() + " with id " + lTempItemsLink[x]
                    .getWurmId() + ". It is in no valid zone.");
                }
              }
            }
            else
            {
              aZone.removeItem(lTempItemsLink[x]);
            }
          }
        }
      }
      else
      {
        for (Item item : this.vitems.getAllItemsAsSet()) {
          if (item.getSizeZ() < 500) {
            aZone.removeItem(item);
          }
        }
      }
    }
  }
  
  private void linkEffectsToZone(VirtualZone aZone, boolean aRemove)
  {
    Iterator<Effect> it;
    if (this.effects != null)
    {
      Iterator<Effect> it;
      if ((!aRemove) && (aZone.covers(this.tilex, this.tiley))) {
        for (it = this.effects.iterator(); it.hasNext();)
        {
          Effect effect = (Effect)it.next();
          aZone.addEffect(effect, false);
        }
      } else {
        for (it = this.effects.iterator(); it.hasNext();)
        {
          Effect effect = (Effect)it.next();
          aZone.removeEffect(effect);
        }
      }
    }
  }
  
  private void linkStructureToZone(VirtualZone aZone, boolean aRemove)
  {
    if (this.structure != null)
    {
      if (logger.isLoggable(Level.FINEST)) {
        logger.log(Level.INFO, "linkStructureToZone: " + this.structure.getWurmId() + " " + aZone.getId());
      }
      if (!aRemove) {
        aZone.addStructure(this.structure);
      } else {
        aZone.removeStructure(this.structure);
      }
    }
  }
  
  private boolean checkDeletion()
  {
    if ((this.creatures == null) || (this.creatures.size() == 0)) {
      if ((this.vitems == null) || (this.vitems.isEmpty())) {
        if ((this.walls == null) || (this.walls.size() == 0)) {
          if (this.structure == null) {
            if (this.fences == null) {
              if ((this.doors == null) || (this.doors.size() == 0)) {
                if ((this.effects == null) || (this.effects.size() == 0)) {
                  if ((this.floors == null) || (this.floors.size() == 0)) {
                    if ((this.mineDoors == null) || (this.mineDoors.size() == 0))
                    {
                      this.zone.removeTile(this);
                      return true;
                    }
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
  
  public void changeStructureName(String newName)
  {
    Iterator<VirtualZone> it;
    if ((this.watchers != null) && (this.structure != null)) {
      for (it = this.watchers.iterator(); it.hasNext();)
      {
        VirtualZone vzone = (VirtualZone)it.next();
        vzone.changeStructureName(this.structure.getWurmId(), newName);
      }
    }
  }
  
  private final Item createPileItem(Item posItem, boolean starting)
  {
    try
    {
      Item pileItem = ItemFactory.createItem(177, 60.0F, null);
      
      float newXPos = (this.tilex << 2) + 1 + Server.rand.nextFloat() * 2.0F;
      float newYPos = (this.tiley << 2) + 1 + Server.rand.nextFloat() * 2.0F;
      
      float height = posItem.getPosZ();
      if (Server.getSecondsUptime() > 0) {
        height = Zones.calculatePosZ(newXPos, newYPos, this, isOnSurface(), false, posItem
          .getPosZ(), null, posItem.onBridge());
      }
      pileItem.setPos(newXPos, newYPos, height, posItem
        .getRotation(), posItem.getBridgeId());
      
      pileItem.setZoneId(this.zone.getId(), this.surfaced);
      int data = posItem.getTemplateId();
      pileItem.setData1(data);
      byte material = 0;
      boolean multipleMaterials = false;
      if (this.vitems != null) {
        for (Item item : this.vitems.getAllItemsAsArray()) {
          if ((!item.isDecoration()) && (item.getFloorLevel() == pileItem.getFloorLevel()))
          {
            if (!starting) {
              sendRemoveItem(item, false);
            }
            if (!multipleMaterials) {
              if (item.getMaterial() != material) {
                if (material == 0)
                {
                  material = item.getMaterial();
                }
                else
                {
                  material = 0;
                  multipleMaterials = true;
                }
              }
            }
            if (!item.equals(posItem)) {
              pileItem.insertItem(item, true);
            }
            if ((data != -1) && (item.getTemplateId() != data))
            {
              pileItem.setData1(-1);
              data = -1;
            }
          }
        }
      }
      String name = pileItem.getName();
      String modelname = pileItem.getModelName();
      Object template;
      String tname;
      StringBuilder build;
      if (data != -1)
      {
        template = ItemTemplateFactory.getInstance().getTemplate(data);
        tname = ((ItemTemplate)template).getName();
        name = "Pile of " + ((ItemTemplate)template).sizeString + tname;
        if (material == 0) {
          pileItem.setMaterial(((ItemTemplate)template).getMaterial());
        } else {
          pileItem.setMaterial(material);
        }
        build = new StringBuilder();
        build.append(pileItem.getTemplate().getModelName());
        build.append(tname);
        build.append(".");
        build.append(MaterialUtilities.getMaterialString(material));
        modelname = build.toString().replaceAll(" ", "").trim();
        pileItem.setName(name);
      }
      if ((!starting) && (this.watchers != null))
      {
        template = getWatchers();tname = template.length;
        for (build = 0; build < tname; build++)
        {
          VirtualZone vz = template[build];
          try
          {
            if (vz.isVisible(pileItem, this))
            {
              boolean onGroundLevel = true;
              if (pileItem.getFloorLevel() > 0) {
                onGroundLevel = false;
              } else if (getFloors(0, 0).length > 0) {
                onGroundLevel = false;
              }
              vz.addItem(pileItem, this, onGroundLevel);
              if (data != -1) {
                vz.renameItem(pileItem, name, modelname);
              }
            }
          }
          catch (Exception e)
          {
            logger.log(Level.WARNING, e.getMessage(), e);
          }
        }
      }
      return pileItem;
    }
    catch (FailedException fe)
    {
      logger.log(Level.WARNING, fe.getMessage(), fe);
    }
    catch (NoSuchTemplateException nst)
    {
      logger.log(Level.WARNING, nst.getMessage(), nst);
    }
    return null;
  }
  
  public void renameItem(Item item)
  {
    if (this.watchers != null) {
      for (VirtualZone vz : getWatchers()) {
        try
        {
          if (vz.isVisible(item, this)) {
            vz.renameItem(item, item.getName(), item.getModelName());
          }
        }
        catch (Exception e)
        {
          logger.log(Level.WARNING, e.getMessage(), e);
        }
      }
    }
  }
  
  public void putRandomOnTile(Item item)
  {
    float newPosX = (this.tilex << 2) + 0.5F + Server.rand.nextFloat() * 3.0F;
    float newPosY = (this.tiley << 2) + 0.5F + Server.rand.nextFloat() * 3.0F;
    item.setPosXY(newPosX, newPosY);
  }
  
  private void destroyPileItem(int floorLevel)
  {
    if (this.vitems != null)
    {
      Item pileItem = this.vitems.getPileItem(floorLevel);
      destroyPileItem(pileItem);
      if (floorLevel == 0) {
        this.vitems.removePileItem(floorLevel);
      }
    }
  }
  
  private final void destroyPileItem(Item pileItem)
  {
    if (pileItem != null)
    {
      try
      {
        Creature[] iwatchers = pileItem.getWatchers();
        for (int x = 0; x < iwatchers.length; x++) {
          iwatchers[x].getCommunicator().sendCloseInventoryWindow(pileItem.getWurmId());
        }
      }
      catch (NoSuchCreatureException localNoSuchCreatureException) {}
      if (this.vitems != null)
      {
        Item[] itemarra = this.vitems.getAllItemsAsArray();
        for (int x = 0; x < itemarra.length; x++) {
          if ((!itemarra[x].isDecoration()) && 
            (itemarra[x].getFloorLevel() == pileItem.getFloorLevel())) {
            this.vitems.removeItem(itemarra[x]);
          }
        }
        Item p = this.vitems.getPileItem(pileItem.getFloorLevel());
        if ((p != null) && (p != pileItem)) {
          Items.destroyItem(p.getWurmId());
        }
      }
      Items.destroyItem(pileItem.getWurmId());
    }
  }
  
  public int hashCode()
  {
    int result = this.tilex + 1;
    result += Zones.worldTileSizeY * (this.tiley + 1);
    return result * (this.surfaced ? 1 : 2);
  }
  
  public static int generateHashCode(int _tilex, int _tiley, boolean _surfaced)
  {
    int result = _tilex + 1;
    result += (_tiley + 1) * Zones.worldTileSizeY;
    return result * (_surfaced ? 1 : 2);
  }
  
  public boolean equals(Object object)
  {
    if (this == object) {
      return true;
    }
    if ((object != null) && (object.getClass() == getClass()))
    {
      VolaTile tile = (VolaTile)object;
      return (tile.getTileX() == this.tilex) && (tile.getTileY() == this.tiley) && (tile.surfaced == this.surfaced);
    }
    return false;
  }
  
  public boolean isGuarded()
  {
    if (this.village != null) {
      return this.village.guards.size() > 0;
    }
    return false;
  }
  
  public boolean hasFire()
  {
    if (this.vitems == null) {
      return false;
    }
    return this.vitems.hasFire();
  }
  
  public Zone getZone()
  {
    return this.zone;
  }
  
  boolean isInactive()
  {
    return this.inactive;
  }
  
  void setInactive(boolean aInactive)
  {
    this.inactive = aInactive;
  }
  
  public boolean isTransition()
  {
    return this.isTransition;
  }
  
  public final boolean hasOnePerTileItem(int floorLevel)
  {
    return (this.vitems != null) && (this.vitems.hasOnePerTileItem(floorLevel));
  }
  
  public final int getFourPerTileCount(int floorLevel)
  {
    if (this.vitems == null) {
      return 0;
    }
    return this.vitems.getFourPerTileCount(floorLevel);
  }
  
  public final Item getOnePerTileItem(int floorLevel)
  {
    if (this.vitems == null) {
      return null;
    }
    return this.vitems.getOnePerTileItem(floorLevel);
  }
  
  void lightningStrikeSpell(float baseDamage, Creature caster)
  {
    if ((this.structure == null) || (!this.structure.isFinished()))
    {
      if (this.creatures != null)
      {
        Creature[] crets = getCreatures();
        for (int c = 0; c < crets.length; c++)
        {
          Wound wound = null;
          if (!crets[c].isPlayer())
          {
            wound = new TempWound((byte)4, (byte)1, baseDamage, crets[c].getWurmId(), 0.0F, 0.0F, true);
          }
          else
          {
            if (Servers.localServer.PVPSERVER)
            {
              float mod = 1.0F;
              try
              {
                Item armour = crets[c].getArmour((byte)1);
                if (armour != null)
                {
                  if (armour.isMetal()) {
                    mod = 2.0F;
                  } else if ((armour.isLeather()) || (armour.isCloth())) {
                    mod = 0.5F;
                  }
                  armour.setDamage(armour.getDamage() + armour.getDamageModifier());
                }
              }
              catch (NoArmourException localNoArmourException) {}catch (NoSpaceException nsp)
              {
                logger.log(Level.WARNING, crets[c].getName() + " no armour space on loc " + 1);
              }
              crets[c].getCommunicator().sendAlertServerMessage("YOU ARE HIT BY LIGHTNING! OUCH!");
              if (Servers.isThisATestServer()) {
                crets[c].getCommunicator().sendNormalServerMessage("Lightning damage mod: " + mod);
              }
              crets[c].addWoundOfType(null, (byte)4, 1, false, 1.0F, false, baseDamage * mod, 0.0F, 0.0F, false, true);
            }
            crets[c].addAttacker(caster);
          }
        }
      }
      if (Servers.localServer.PVPSERVER) {
        if (this.vitems != null)
        {
          Item[] ttempItems = this.vitems.getAllItemsAsArray();
          for (int x = 0; x < ttempItems.length; x++) {
            if ((!ttempItems[x].isIndestructible()) && (!ttempItems[x].isHugeAltar())) {
              ttempItems[x].setDamage(ttempItems[x].getDamage() + ttempItems[x]
                .getDamageModifier() * (
                (ttempItems[x].isLocked()) || (ttempItems[x].isDecoration()) || 
                (ttempItems[x].isMetal()) || 
                (ttempItems[x].isStone()) ? 0.1F : 10.0F));
            }
          }
        }
      }
    }
  }
  
  void flashStrike()
  {
    if ((this.structure == null) || (!this.structure.isFinished()))
    {
      if (this.creatures != null)
      {
        Creature[] crets = getCreatures();
        for (int c = 0; c < crets.length; c++)
        {
          Wound wound = null;
          if (!crets[c].isPlayer())
          {
            wound = new TempWound((byte)4, (byte)1, 10000.0F, crets[c].getWurmId(), 0.0F, 0.0F, false);
          }
          else
          {
            float mod = 1.0F;
            try
            {
              Item armour = crets[c].getArmour((byte)1);
              if (armour != null)
              {
                if (armour.isMetal()) {
                  mod = 2.0F;
                } else if ((armour.isLeather()) || (armour.isCloth())) {
                  mod = 0.5F;
                }
                armour.setDamage(armour.getDamage() + armour.getDamageModifier() * 10.0F);
              }
              Item[] lItems = crets[c].getBody().getContainersAndWornItems();
              for (int x = 0; x < lItems.length; x++) {
                if (((lItems[x].isArmour()) || (lItems[x].isWeapon())) && (lItems[x].isMetal()))
                {
                  mod += 0.1F;
                  lItems[x].setDamage(lItems[x].getDamage() + lItems[x].getDamageModifier() * 10.0F);
                }
              }
            }
            catch (NoArmourException localNoArmourException) {}catch (NoSpaceException nsp)
            {
              logger.log(Level.WARNING, crets[c].getName() + " no armour space on loc " + 1);
            }
            crets[c].getCommunicator().sendAlertServerMessage("YOU ARE HIT BY LIGHTNING! OUCH!");
            
            crets[c].addWoundOfType(null, (byte)4, 1, false, 1.0F, false, 3000.0F * mod, 0.0F, 0.0F, false, false);
            HistoryManager.addHistory(crets[c].getName(), "was hit by lightning!");
            if (logger.isLoggable(Level.FINER)) {
              logger.finer(crets[c].getName() + " was hit by lightning!");
            }
            Skills skills = crets[c].getSkills();
            Skill mindspeed = null;
            try
            {
              mindspeed = skills.getSkill(101);
              double knowl = mindspeed.getKnowledge();
              mindspeed.setKnowledge(knowl + 1.0F * mod, false);
            }
            catch (NoSuchSkillException nss)
            {
              mindspeed = skills.learn(101, 21.0F);
            }
            crets[c].getCommunicator().sendNormalServerMessage("A strange dizziness runs through your head, eventually sharpening your senses.");
          }
        }
      }
      if (this.vitems != null)
      {
        Item[] ttempItems = this.vitems.getAllItemsAsArray();
        for (int x = 0; x < ttempItems.length; x++) {
          ttempItems[x].setDamage(ttempItems[x].getDamage() + ttempItems[x].getDamageModifier() * 10.0F);
        }
      }
    }
  }
  
  public void moveItem(Item item, float newPosX, float newPosY, float newPosZ, float newRot, boolean surf, float oldPosZ)
  {
    int diffdecx = (int)(newPosX * 10.0F - item.getPosX() * 10.0F);
    int diffdecy = (int)(newPosY * 10.0F - item.getPosY() * 10.0F);
    if ((diffdecx != 0) || (diffdecy != 0))
    {
      newPosX = item.getPosX() + diffdecx * 0.01F;
      newPosY = item.getPosY() + diffdecy * 0.01F;
      int newTileX = (int)newPosX >> 2;
      int newTileY = (int)newPosY >> 2;
      long newBridgeId = item.getBridgeId();
      long oldBridgeId = item.getBridgeId();
      if ((newTileX != this.tilex) || (newTileY != this.tiley) || (surf != isOnSurface()))
      {
        dt = Zones.getTileOrNull(Zones.safeTileX(newTileX), Zones.safeTileY(newTileY), surf);
        if ((item.onBridge() == -10L) && (dt != null) && (dt.getStructure() != null) && (dt.getStructure().isTypeBridge()))
        {
          if (item.getBridgeId() == -10L)
          {
            BridgePart bp = Zones.getBridgePartFor(newTileX, newTileY, surf);
            if ((bp != null) && (bp.isFinished()) && (bp.hasAnExit()))
            {
              if ((Servers.isThisATestServer()) && (item.isWagonerWagon())) {
                Players.getInstance().sendGmMessage(null, "System", "Debug: Wagon " + item.getName() + " bid:" + oldBridgeId + " z:" + item
                  .getPosZ() + " fl:" + item.getFloorLevel() + " bp:" + bp
                  .getStructureId() + " N:" + bp.getNorthExit() + " E:" + bp.getEastExit() + " S:" + bp
                  .getSouthExit() + " W:" + bp.getWestExit() + " @" + item
                  .getTileX() + "," + item.getTileY() + " to " + newTileX + "," + newTileY + "," + surf, false);
              }
              if ((newTileY < item.getTileY()) && (bp.getSouthExitFloorLevel() == item.getFloorLevel())) {
                newBridgeId = bp.getStructureId();
              } else if ((newTileX > item.getTileX()) && (bp.getWestExitFloorLevel() == item.getFloorLevel())) {
                newBridgeId = bp.getStructureId();
              } else if ((newTileY > item.getTileY()) && (bp.getNorthExitFloorLevel() == item.getFloorLevel())) {
                newBridgeId = bp.getStructureId();
              } else if ((newTileX < item.getTileX()) && (bp.getEastExitFloorLevel() == item.getFloorLevel())) {
                newBridgeId = bp.getStructureId();
              }
              if ((Servers.isThisATestServer()) && (newBridgeId != oldBridgeId)) {
                Players.getInstance().sendGmMessage(null, "System", "Debug: Wagon " + item.getName() + " obid:" + oldBridgeId + " z:" + item
                  .getPosZ() + " fl:" + item.getFloorLevel() + " nbid:" + newBridgeId + " N:" + bp
                  .getNorthExit() + " E:" + bp.getEastExit() + " S:" + bp.getSouthExit() + " W:" + bp.getWestExit() + " @" + item
                  .getTileX() + "," + item.getTileY() + " to " + newTileX + "," + newTileY + "," + surf, false);
              }
            }
            else
            {
              newBridgeId = -10L;
              item.setOnBridge(-10L);
              sendSetBridgeId(item, -10L);
              item.calculatePosZ(dt, null);
            }
          }
          else
          {
            BridgePart bp = Zones.getBridgePartFor(newTileX, newTileY, surf);
            if (bp == null)
            {
              newBridgeId = -10L;
              item.setOnBridge(-10L);
              sendSetBridgeId(item, -10L);
              item.calculatePosZ(dt, null);
            }
          }
          if (item.onBridge() != newBridgeId)
          {
            float nz = Zones.calculatePosZ(newPosX, newPosY, dt, isOnSurface(), false, oldPosZ, null, newBridgeId);
            if ((Servers.isThisATestServer()) && (item.isWagonerWagon())) {
              Players.getInstance().sendGmMessage(null, "System", "Debug: Wagon " + item.getName() + " moving onto, or off, a bridge from bid:" + oldBridgeId + " z:" + item
              
                .getPosZ() + " fl:" + item.getFloorLevel() + " to bp:" + newBridgeId + " newZ:" + nz + " @" + item
                
                .getTileX() + "," + item.getTileY() + " to " + newTileX + "," + newTileY + "," + surf, false);
            }
            if (Math.abs(oldPosZ - nz) < 10.0F) {
              if (!item.isBoat())
              {
                item.setOnBridge(newBridgeId);
                
                newPosZ = nz;
                
                sendSetBridgeId(item, newBridgeId);
              }
            }
          }
        }
        else if ((item.onBridge() > 0L) && ((dt == null) || 
          (dt.getStructure() == null) || (dt.getStructure().getWurmId() != item.onBridge())))
        {
          boolean leave = true;
          bp = Zones.getBridgePartFor(newTileX, newTileY, surf);
          if ((bp != null) && (bp.isFinished())) {
            if ((bp.getDir() == 0) || (bp.getDir() == 4))
            {
              if (getTileX() != newTileX) {
                leave = false;
              }
            }
            else if (getTileY() != newTileY) {
              leave = false;
            }
          }
          if (leave)
          {
            newBridgeId = -10L;
            item.setOnBridge(-10L);
            sendSetBridgeId(item, -10L);
          }
        }
        if (surf != isOnSurface()) {
          item.newLayer = ((byte)(isOnSurface() ? -1 : 0));
        }
        removeItem(item, true);
        if ((diffdecx != 0) && (diffdecy != 0))
        {
          item.setPosXYZRotation(newPosX, newPosY, newPosZ, newRot);
        }
        else
        {
          item.setRotation(newRot);
          if (diffdecx != 0) {
            item.setPosX(newPosX);
          }
          if (diffdecy != 0) {
            item.setPosY(newPosY);
          }
          item.setPosZ(newPosZ);
        }
        try
        {
          Zone _zone = Zones.getZone((int)newPosX >> 2, (int)newPosY >> 2, surf);
          _zone.addItem(item, true, surf != isOnSurface(), false);
        }
        catch (NoSuchZoneException nsz)
        {
          logger.log(Level.WARNING, item.getName() + ", " + nsz.getMessage(), nsz);
        }
        if (surf != isOnSurface()) {
          item.newLayer = Byte.MIN_VALUE;
        }
      }
      else
      {
        if (diffdecx != 0) {
          item.setTempXPosition(newPosX);
        }
        if (diffdecy != 0) {
          item.setTempYPosition(newPosY);
        }
        item.setTempZandRot(newPosZ, newRot);
      }
      VolaTile dt = getWatchers();nsz = dt.length;
      for (BridgePart bp = 0; bp < nsz; bp++)
      {
        VirtualZone vz = dt[bp];
        try
        {
          if (vz.isVisible(item, this))
          {
            if ((item.getFloorLevel() <= 0) && (item.onBridge() <= 0L))
            {
              if (Structure.isGroundFloorAtPosition(newPosX, newPosY, item.isOnSurface())) {
                vz.sendMoveMovingItemAndSetZ(item.getWurmId(), (byte)diffdecx, (byte)diffdecy, item
                
                  .getPosZ(), (int)(newRot * 256.0F / 360.0F));
              } else {
                vz.sendMoveMovingItem(item.getWurmId(), (byte)diffdecx, (byte)diffdecy, (int)(newRot * 256.0F / 360.0F));
              }
            }
            else {
              vz.sendMoveMovingItemAndSetZ(item.getWurmId(), (byte)diffdecx, (byte)diffdecy, item
              
                .getPosZ(), (int)(newRot * 256.0F / 360.0F));
            }
          }
          else {
            vz.removeItem(item);
          }
        }
        catch (Exception e)
        {
          logger.log(Level.WARNING, e.getMessage(), e);
        }
      }
    }
  }
  
  public void destroyEverything()
  {
    Creature[] crets = getCreatures();
    for (int x = 0; x < crets.length; x++)
    {
      crets[x].getCommunicator().sendNormalServerMessage("The rock suddenly caves in! You are crushed!");
      crets[x].die(true, "Cave collapse");
    }
    Fence[] fenceArr = getFences();
    for (int x = 0; x < fenceArr.length; x++) {
      if (fenceArr[x] != null) {
        fenceArr[x].destroy();
      }
    }
    Wall[] wallArr = getWalls();
    for (int x = 0; x < wallArr.length; x++) {
      if (wallArr[x] != null) {
        wallArr[x].destroy();
      }
    }
    Floor[] floorArr = getFloors();
    for (int x = 0; x < floorArr.length; x++) {
      if (floorArr[x] != null) {
        floorArr[x].destroy();
      }
    }
    Item[] ttempItems = getItems();
    for (int x = 0; x < ttempItems.length; x++) {
      Items.destroyItem(ttempItems[x].getWurmId());
    }
  }
  
  protected void sendNewLayerToWatchers(Item item)
  {
    logger.log(Level.INFO, "Tile at " + this.tilex + ", " + this.tiley + " sending secondary");
    for (VirtualZone vz : getWatchers()) {
      try
      {
        vz.justSendNewLayer(item);
      }
      catch (Exception e)
      {
        logger.log(Level.WARNING, e.getMessage(), e);
      }
    }
  }
  
  protected void newLayer(Item item)
  {
    for (VirtualZone vz : getWatchers()) {
      try
      {
        vz.newLayer(item);
      }
      catch (Exception e)
      {
        logger.log(Level.WARNING, e.getMessage(), e);
      }
    }
    if (!isOnSurface()) {
      for (VirtualZone vz : getSurfaceTile().getWatchers()) {
        try
        {
          vz.addItem(item, getSurfaceTile(), true);
        }
        catch (Exception e)
        {
          logger.log(Level.WARNING, e.getMessage(), e);
        }
      }
    }
  }
  
  public void newLayer(Creature creature)
  {
    if (creature.isOnSurface() != isOnSurface()) {
      for (VirtualZone vz : getWatchers()) {
        try
        {
          vz.newLayer(creature, isOnSurface());
        }
        catch (Exception e)
        {
          logger.log(Level.WARNING, e.getMessage(), e);
        }
      }
    }
    try
    {
      Zone newzone = Zones.getZone(this.tilex, this.tiley, creature.getLayer() >= 0);
      VolaTile currentTile = newzone.getOrCreateTile(this.tilex, this.tiley);
      removeCreature(creature);
      currentTile.addCreature(creature, 0);
    }
    catch (NoSuchZoneException localNoSuchZoneException1) {}catch (NoSuchPlayerException localNoSuchPlayerException1) {}catch (NoSuchCreatureException localNoSuchCreatureException1) {}
  }
  
  public void addLightSource(Item lightSource)
  {
    if (lightSource.getTemplateId() == 1243) {
      return;
    }
    for (VirtualZone vz : getWatchers()) {
      try
      {
        if (lightSource.getColor() != -1)
        {
          int lightStrength = Math.max(WurmColor.getColorRed(lightSource.getColor()), 
            WurmColor.getColorGreen(lightSource.getColor()));
          lightStrength = Math.max(lightStrength, WurmColor.getColorBlue(lightSource.getColor()));
          if (lightStrength == 0) {
            lightStrength = 1;
          }
          byte r = (byte)(WurmColor.getColorRed(lightSource.getColor()) * 128 / lightStrength);
          byte g = (byte)(WurmColor.getColorGreen(lightSource.getColor()) * 128 / lightStrength);
          byte b = (byte)(WurmColor.getColorBlue(lightSource.getColor()) * 128 / lightStrength);
          
          vz.sendAttachItemEffect(lightSource.getWurmId(), (byte)4, r, g, b, lightSource
            .getRadius());
        }
        else if (lightSource.isLightBright())
        {
          int lightStrength = (int)(80.0F + lightSource.getCurrentQualityLevel() / 100.0F * 40.0F);
          vz.sendAttachItemEffect(lightSource.getWurmId(), (byte)4, 
            Item.getRLight(lightStrength), Item.getGLight(lightStrength), Item.getBLight(lightStrength), lightSource
            .getRadius());
        }
        else
        {
          vz.sendAttachItemEffect(lightSource.getWurmId(), (byte)4, (byte)80, (byte)80, (byte)80, lightSource
            .getRadius());
        }
      }
      catch (Exception e)
      {
        logger.log(Level.WARNING, e.getMessage(), e);
      }
    }
  }
  
  public void removeLightSource(Item lightSource)
  {
    if (lightSource.getTemplateId() == 1243) {
      return;
    }
    for (VirtualZone vz : getWatchers()) {
      try
      {
        vz.sendRemoveEffect(lightSource.getWurmId(), (byte)0);
        vz.sendRemoveEffect(lightSource.getWurmId(), (byte)4);
      }
      catch (Exception e)
      {
        logger.log(Level.WARNING, e.getMessage(), e);
      }
    }
  }
  
  public void setHasLightSource(Creature creature, @Nullable Item lightSource)
  {
    if ((lightSource != null) && (lightSource.getTemplateId() == 1243)) {
      return;
    }
    for (VirtualZone vz : getWatchers()) {
      try
      {
        if (lightSource == null)
        {
          if (vz.getWatcher().getWurmId() == creature.getWurmId()) {
            vz.sendRemoveEffect(-1L, (byte)0);
          } else {
            vz.sendRemoveEffect(creature.getWurmId(), (byte)0);
          }
        }
        else if (vz.getWatcher().getWurmId() == creature.getWurmId())
        {
          if (lightSource.getColor() != -1)
          {
            int lightStrength = Math.max(WurmColor.getColorRed(lightSource.color), 
              WurmColor.getColorGreen(lightSource.color));
            lightStrength = Math.max(lightStrength, WurmColor.getColorBlue(lightSource.color));
            byte r = (byte)(WurmColor.getColorRed(lightSource.color) * 128 / lightStrength);
            byte g = (byte)(WurmColor.getColorGreen(lightSource.color) * 128 / lightStrength);
            byte b = (byte)(WurmColor.getColorBlue(lightSource.color) * 128 / lightStrength);
            
            vz.sendAttachCreatureEffect(null, (byte)0, r, g, b, lightSource
              .getRadius());
          }
          else if (lightSource.isLightBright())
          {
            int lightStrength = (int)(80.0F + lightSource.getCurrentQualityLevel() / 100.0F * 40.0F);
            vz.sendAttachCreatureEffect(null, (byte)0, 
              Item.getRLight(lightStrength), Item.getGLight(lightStrength), 
              Item.getBLight(lightStrength), lightSource.getRadius());
          }
          else
          {
            vz.sendAttachCreatureEffect(null, (byte)0, 
              Item.getRLight(80), Item.getGLight(80), Item.getBLight(80), lightSource.getRadius());
          }
        }
        else if (lightSource.getColor() != -1)
        {
          int lightStrength = Math.max(WurmColor.getColorRed(lightSource.color), 
            WurmColor.getColorGreen(lightSource.color));
          lightStrength = Math.max(lightStrength, WurmColor.getColorBlue(lightSource.color));
          byte r = (byte)(WurmColor.getColorRed(lightSource.color) * 128 / lightStrength);
          byte g = (byte)(WurmColor.getColorGreen(lightSource.color) * 128 / lightStrength);
          byte b = (byte)(WurmColor.getColorBlue(lightSource.color) * 128 / lightStrength);
          
          vz.sendAttachCreatureEffect(creature, (byte)0, r, g, b, lightSource
            .getRadius());
        }
        else if (lightSource.isLightBright())
        {
          int lightStrength = (int)(80.0F + lightSource.getCurrentQualityLevel() / 100.0F * 40.0F);
          vz.sendAttachCreatureEffect(creature, (byte)0, 
            Item.getRLight(lightStrength), Item.getGLight(lightStrength), 
            Item.getBLight(lightStrength), lightSource.getRadius());
        }
        else
        {
          vz.sendAttachCreatureEffect(creature, (byte)0, 
            Item.getRLight(80), Item.getGLight(80), Item.getBLight(80), lightSource.getRadius());
        }
      }
      catch (Exception e)
      {
        logger.log(Level.WARNING, e.getMessage(), e);
      }
    }
  }
  
  public void setHasLightSource(Creature creature, byte colorRed, byte colorGreen, byte colorBlue, byte radius)
  {
    for (VirtualZone vz : getWatchers()) {
      try
      {
        if (vz.getWatcher().getWurmId() == creature.getWurmId()) {
          vz.sendAttachCreatureEffect(null, (byte)0, colorRed, colorGreen, colorBlue, radius);
        } else {
          vz.sendAttachCreatureEffect(creature, (byte)0, colorRed, colorGreen, colorBlue, radius);
        }
      }
      catch (Exception e)
      {
        logger.log(Level.WARNING, e.getMessage(), e);
      }
    }
  }
  
  public void sendAttachCreatureEffect(Creature creature, byte effectType, byte data0, byte data1, byte data2, byte radius)
  {
    for (VirtualZone vz : getWatchers()) {
      try
      {
        if (vz.getWatcher().getWurmId() == creature.getWurmId()) {
          vz.sendAttachCreatureEffect(null, effectType, data0, data1, data2, radius);
        } else {
          vz.sendAttachCreatureEffect(creature, effectType, data0, data1, data2, radius);
        }
      }
      catch (Exception e)
      {
        logger.log(Level.WARNING, e.getMessage(), e);
      }
    }
  }
  
  public void sendRemoveCreatureEffect(Creature creature, byte effectType)
  {
    for (VirtualZone vz : getWatchers()) {
      try
      {
        if (vz.getWatcher().getWurmId() == creature.getWurmId()) {
          vz.sendRemoveEffect(-1L, effectType);
        } else {
          vz.sendRemoveEffect(creature.getWurmId(), effectType);
        }
      }
      catch (Exception e)
      {
        logger.log(Level.WARNING, e.getMessage(), e);
      }
    }
  }
  
  public void sendProjectile(long itemid, byte type, String modelName, String name, byte material, float startX, float startY, float startH, float rot, byte layer, float endX, float endY, float endH, long sourceId, long targetId, float projectedSecondsInAir, float actualSecondsInAir)
  {
    for (VirtualZone vz : getWatchers()) {
      try
      {
        if (vz.getWatcher().getWurmId() == targetId)
        {
          if (vz.getWatcher().getWurmId() == sourceId) {
            vz.sendProjectile(itemid, type, modelName, name, material, startX, startY, startH, rot, layer, endX, endY, endH, -1L, -1L, projectedSecondsInAir, actualSecondsInAir);
          } else {
            vz.sendProjectile(itemid, type, modelName, name, material, startX, startY, startH, rot, layer, endX, endY, endH, sourceId, -1L, projectedSecondsInAir, actualSecondsInAir);
          }
        }
        else if (vz.getWatcher().getWurmId() == sourceId)
        {
          if (vz.getWatcher().getWurmId() == targetId) {
            vz.sendProjectile(itemid, type, modelName, name, material, startX, startY, startH, rot, layer, endX, endY, endH, -1L, -1L, projectedSecondsInAir, actualSecondsInAir);
          } else {
            vz.sendProjectile(itemid, type, modelName, name, material, startX, startY, startH, rot, layer, endX, endY, endH, -1L, targetId, projectedSecondsInAir, actualSecondsInAir);
          }
        }
        else {
          vz.sendProjectile(itemid, type, modelName, name, material, startX, startY, startH, rot, layer, endX, endY, endH, sourceId, targetId, projectedSecondsInAir, actualSecondsInAir);
        }
      }
      catch (Exception e)
      {
        logger.log(Level.WARNING, e.getMessage(), e);
      }
    }
  }
  
  public void sendNewProjectile(long itemid, byte type, String modelName, String name, byte material, Vector3f startingPosition, Vector3f startingVelocity, Vector3f endingPosition, float rotation, boolean surface)
  {
    for (VirtualZone vz : getWatchers()) {
      try
      {
        vz.sendNewProjectile(itemid, type, modelName, name, material, startingPosition, startingVelocity, endingPosition, rotation, surface);
      }
      catch (Exception e)
      {
        logger.log(Level.WARNING, e.getMessage(), e);
      }
    }
  }
  
  public void sendHorseWear(long creatureId, int itemId, byte material, byte slot, byte aux_data)
  {
    for (VirtualZone vz : getWatchers()) {
      try
      {
        vz.sendHorseWear(creatureId, itemId, material, slot, aux_data);
      }
      catch (Exception e)
      {
        logger.log(Level.WARNING, e.getMessage(), e);
      }
    }
  }
  
  public void sendRemoveHorseWear(long creatureId, int itemId, byte slot)
  {
    for (VirtualZone vz : getWatchers()) {
      try
      {
        vz.sendRemoveHorseWear(creatureId, itemId, slot);
      }
      catch (Exception e)
      {
        logger.log(Level.WARNING, e.getMessage(), e);
      }
    }
  }
  
  public void sendBoatAttachment(long itemId, int templateId, byte material, byte slot, byte aux)
  {
    for (VirtualZone vz : getWatchers()) {
      try
      {
        vz.sendBoatAttachment(itemId, templateId, material, slot, aux);
      }
      catch (Exception e)
      {
        logger.log(Level.WARNING, e.getMessage(), e);
      }
    }
  }
  
  public void sendBoatDetachment(long itemId, int templateId, byte slot)
  {
    for (VirtualZone vz : getWatchers()) {
      try
      {
        vz.sendBoatDetachment(itemId, templateId, slot);
      }
      catch (Exception e)
      {
        logger.log(Level.WARNING, e.getMessage(), e);
      }
    }
  }
  
  public void sendWearItem(long creatureId, int itemId, byte bodyPart, int colorRed, int colorGreen, int colorBlue, int secondaryColorRed, int secondaryColorGreen, int secondaryColorBlue, byte material, byte rarity)
  {
    for (VirtualZone vz : getWatchers()) {
      try
      {
        if (vz.getWatcher().getWurmId() == creatureId) {
          vz.sendWearItem(-1L, itemId, bodyPart, colorRed, colorGreen, colorBlue, secondaryColorRed, secondaryColorGreen, secondaryColorBlue, material, rarity);
        } else {
          vz.sendWearItem(creatureId, itemId, bodyPart, colorRed, colorGreen, colorBlue, secondaryColorRed, secondaryColorGreen, secondaryColorBlue, material, rarity);
        }
      }
      catch (Exception e)
      {
        logger.log(Level.WARNING, e.getMessage(), e);
      }
    }
  }
  
  public void sendRemoveWearItem(long creatureId, byte bodyPart)
  {
    for (VirtualZone vz : getWatchers()) {
      try
      {
        if (vz.getWatcher().getWurmId() == creatureId) {
          vz.sendRemoveWearItem(-1L, bodyPart);
        } else {
          vz.sendRemoveWearItem(creatureId, bodyPart);
        }
      }
      catch (Exception e)
      {
        logger.log(Level.WARNING, e.getMessage(), e);
      }
    }
  }
  
  public void sendWieldItem(long creatureId, byte slot, String modelname, byte rarity, int colorRed, int colorGreen, int colorBlue, int secondaryColorRed, int secondaryColorGreen, int secondaryColorBlue)
  {
    for (VirtualZone vz : getWatchers()) {
      try
      {
        if (vz.getWatcher().getWurmId() == creatureId) {
          vz.sendWieldItem(-1L, slot, modelname, rarity, colorRed, colorGreen, colorBlue, secondaryColorRed, secondaryColorGreen, secondaryColorBlue);
        } else {
          vz.sendWieldItem(creatureId, slot, modelname, rarity, colorRed, colorGreen, colorBlue, secondaryColorRed, secondaryColorGreen, secondaryColorBlue);
        }
      }
      catch (Exception e)
      {
        logger.log(Level.WARNING, e.getMessage(), e);
      }
    }
  }
  
  public void sendUseItem(Creature creature, String modelname, byte rarity, int colorRed, int colorGreen, int colorBlue, int secondaryColorRed, int secondaryColorGreen, int secondaryColorBlue)
  {
    for (VirtualZone vz : getWatchers()) {
      try
      {
        if (vz.getWatcher().getWurmId() == creature.getWurmId()) {
          vz.sendUseItem(null, modelname, rarity, colorRed, colorGreen, colorBlue, secondaryColorRed, secondaryColorGreen, secondaryColorBlue);
        } else if (creature.isVisibleTo(vz.getWatcher())) {
          vz.sendUseItem(creature, modelname, rarity, colorRed, colorGreen, colorBlue, secondaryColorRed, secondaryColorGreen, secondaryColorBlue);
        }
      }
      catch (Exception e)
      {
        logger.log(Level.WARNING, e.getMessage(), e);
      }
    }
  }
  
  public void sendStopUseItem(Creature creature)
  {
    for (VirtualZone vz : getWatchers()) {
      try
      {
        if (vz.getWatcher().getWurmId() == creature.getWurmId()) {
          vz.sendStopUseItem(null);
        } else if (creature.isVisibleTo(vz.getWatcher())) {
          vz.sendStopUseItem(creature);
        }
      }
      catch (Exception e)
      {
        logger.log(Level.WARNING, e.getMessage(), e);
      }
    }
  }
  
  public void sendAnimation(Creature creature, String animationName, boolean looping, long target)
  {
    for (VirtualZone vz : getWatchers()) {
      try
      {
        if (vz.getWatcher().getWurmId() == creature.getWurmId()) {
          vz.sendAnimation(null, animationName, looping, target);
        } else if (creature.isVisibleTo(vz.getWatcher())) {
          vz.sendAnimation(creature, animationName, looping, target);
        }
      }
      catch (Exception e)
      {
        logger.log(Level.WARNING, e.getMessage(), e);
      }
    }
  }
  
  public void sendStance(Creature creature, byte stance)
  {
    for (VirtualZone vz : getWatchers()) {
      try
      {
        if (vz.getWatcher().getWurmId() == creature.getWurmId()) {
          vz.sendStance(null, stance);
        } else if (creature.isVisibleTo(vz.getWatcher())) {
          vz.sendStance(creature, stance);
        }
      }
      catch (Exception e)
      {
        logger.log(Level.WARNING, e.getMessage(), e);
      }
    }
  }
  
  public void sendAnimation(Creature initiator, Item item, String animationName, boolean looping, boolean freeze)
  {
    for (VirtualZone vz : getWatchers()) {
      try
      {
        Creature watcher = vz.getWatcher();
        if (watcher != null) {
          if ((vz.isVisible(item, this)) && ((initiator == null) || (initiator.isVisibleTo(watcher)))) {
            watcher.getCommunicator().sendAnimation(item.getWurmId(), animationName, looping, freeze);
          }
        }
      }
      catch (Exception e)
      {
        logger.log(Level.WARNING, e.getMessage(), e);
      }
    }
  }
  
  public void sendCreatureDamage(Creature creature, float damPercent)
  {
    for (VirtualZone vz : getWatchers()) {
      try
      {
        if (creature.isVisibleTo(vz.getWatcher())) {
          vz.sendCreatureDamage(creature, damPercent);
        }
      }
      catch (Exception e)
      {
        logger.log(Level.WARNING, e.getMessage(), e);
      }
    }
  }
  
  public void sendFishingLine(Creature creature, float posX, float posY, byte floatType)
  {
    for (VirtualZone vz : getWatchers()) {
      try
      {
        if (creature.isVisibleTo(vz.getWatcher())) {
          vz.sendFishingLine(creature, posX, posY, floatType);
        }
      }
      catch (Exception e)
      {
        logger.log(Level.WARNING, e.getMessage(), e);
      }
    }
  }
  
  public void sendFishHooked(Creature creature, byte fishType, long fishId)
  {
    for (VirtualZone vz : getWatchers()) {
      try
      {
        if (creature.isVisibleTo(vz.getWatcher())) {
          vz.sendFishHooked(creature, fishType, fishId);
        }
      }
      catch (Exception e)
      {
        logger.log(Level.WARNING, e.getMessage(), e);
      }
    }
  }
  
  public void sendFishingStopped(Creature creature)
  {
    for (VirtualZone vz : getWatchers()) {
      try
      {
        if (creature.isVisibleTo(vz.getWatcher())) {
          vz.sendFishingStopped(creature);
        }
      }
      catch (Exception e)
      {
        logger.log(Level.WARNING, e.getMessage(), e);
      }
    }
  }
  
  public void sendSpearStrike(Creature creature, float posX, float posY)
  {
    for (VirtualZone vz : getWatchers()) {
      try
      {
        if (creature.isVisibleTo(vz.getWatcher())) {
          vz.sendSpearStrike(creature, posX, posY);
        }
      }
      catch (Exception e)
      {
        logger.log(Level.WARNING, e.getMessage(), e);
      }
    }
  }
  
  public void sendRepaint(Item item)
  {
    boolean noPaint = item.color == -1;
    boolean noPaint2 = item.color2 == -1;
    for (VirtualZone vz : getWatchers()) {
      try
      {
        vz.sendRepaint(item.getWurmId(), (byte)WurmColor.getColorRed(item.getColor()), 
          (byte)WurmColor.getColorGreen(item.getColor()), (byte)WurmColor.getColorBlue(item.getColor()), (byte)-1, noPaint ? 0 : (byte)0);
        if (item.supportsSecondryColor()) {
          vz.sendRepaint(item.getWurmId(), (byte)WurmColor.getColorRed(item.getColor2()), 
            (byte)WurmColor.getColorGreen(item.getColor2()), (byte)WurmColor.getColorBlue(item.getColor2()), (byte)-1, noPaint2 ? 0 : (byte)1);
        }
      }
      catch (Exception e)
      {
        logger.log(Level.WARNING, e.getMessage(), e);
      }
    }
  }
  
  public void sendAttachCreature(long creatureId, long targetId, float offx, float offy, float offz, int seatId)
  {
    for (VirtualZone vz : getWatchers()) {
      try
      {
        if (vz.getWatcher().getWurmId() == creatureId) {
          vz.sendAttachCreature(-1L, targetId, offx, offy, offz, seatId);
        } else if (vz.getWatcher().getWurmId() == targetId) {
          vz.sendAttachCreature(creatureId, -1L, offx, offy, offz, seatId);
        } else {
          vz.sendAttachCreature(creatureId, targetId, offx, offy, offz, seatId);
        }
      }
      catch (Exception e)
      {
        logger.log(Level.WARNING, e.getMessage(), e);
      }
    }
  }
  
  public void sendAttachCreature(long creatureId, long targetId, float offx, float offy, float offz, int seatId, boolean ignoreOrigin)
  {
    for (VirtualZone vz : getWatchers()) {
      try
      {
        if (vz.getWatcher().getWurmId() == creatureId)
        {
          if (!ignoreOrigin) {
            vz.sendAttachCreature(-1L, targetId, offx, offy, offz, seatId);
          }
        }
        else if (vz.getWatcher().getWurmId() == targetId) {
          vz.sendAttachCreature(creatureId, -1L, offx, offy, offz, seatId);
        } else {
          vz.sendAttachCreature(creatureId, targetId, offx, offy, offz, seatId);
        }
      }
      catch (Exception e)
      {
        logger.log(Level.WARNING, e.getMessage(), e);
      }
    }
  }
  
  public Set<VolaTile> getThisAndSurroundingTiles(int dist)
  {
    Set<VolaTile> surr = new HashSet();
    VolaTile t = null;
    for (int x = -dist; x <= dist; x++) {
      for (int y = -dist; y <= dist; y++)
      {
        t = Zones.getTileOrNull(Zones.safeTileX(this.tilex + x), Zones.safeTileY(this.tiley + y), this.surfaced);
        if (t != null) {
          surr.add(t);
        }
      }
    }
    return surr;
  }
  
  public void checkDiseaseSpread()
  {
    int dist = 1;
    if ((this.village != null) && 
      (this.village.getCreatureRatio() < Village.OPTIMUMCRETRATIO)) {
      dist = 2;
    }
    Set<VolaTile> set = getThisAndSurroundingTiles(dist);
    for (VolaTile t : set)
    {
      Creature[] crets = t.getCreatures();
      for (Creature c : crets) {
        if ((!c.isPlayer()) && (!c.isKingdomGuard()) && (!c.isSpiritGuard()) && (!c.isUnique())) {
          if (Server.rand.nextInt(100) == 0) {
            if (c.getDisease() == 0)
            {
              logger.log(Level.INFO, "Disease spreads to " + c.getName() + " at " + t);
              c.setDisease((byte)1);
            }
          }
        }
      }
    }
  }
  
  public void checkVisibility(Creature watched, boolean makeInvis)
  {
    float lStealthMod;
    float lStealthMod;
    if (makeInvis) {
      lStealthMod = MethodsCreatures.getStealthTerrainModifier(watched, this.tilex, this.tiley, this.surfaced);
    } else {
      lStealthMod = 0.0F;
    }
    for (VirtualZone vz : getWatchers()) {
      try
      {
        if (vz.getWatcher().getWurmId() != watched.getWurmId()) {
          if ((makeInvis) && (!watched.visibilityCheck(vz.getWatcher(), lStealthMod))) {
            vz.makeInvisible(watched);
          } else {
            try
            {
              vz.addCreature(watched.getWurmId(), false);
            }
            catch (NoSuchCreatureException localNoSuchCreatureException) {}catch (NoSuchPlayerException localNoSuchPlayerException) {}
          }
        }
      }
      catch (Exception e)
      {
        logger.log(Level.WARNING, e.getMessage(), e);
      }
    }
  }
  
  public void checkCaveOpening()
  {
    for (VirtualZone vz : getWatchers()) {
      try
      {
        vz.getWatcher().getVisionArea().checkCaves(false);
      }
      catch (Exception e)
      {
        logger.log(Level.WARNING, e.getMessage(), e);
      }
    }
  }
  
  public void setNewFace(Creature c)
  {
    for (VirtualZone vz : getWatchers()) {
      try
      {
        vz.setNewFace(c);
      }
      catch (Exception e)
      {
        logger.log(Level.WARNING, e.getMessage(), e);
      }
    }
  }
  
  public void setNewRarityShader(Creature c)
  {
    for (VirtualZone vz : getWatchers()) {
      try
      {
        vz.setNewRarityShader(c);
      }
      catch (Exception e)
      {
        logger.log(Level.WARNING, e.getMessage(), e);
      }
    }
  }
  
  public void sendActionControl(Creature c, String actionString, boolean start, int timeLeft)
  {
    for (VirtualZone vz : getWatchers()) {
      try
      {
        vz.sendActionControl(c.getWurmId(), actionString, start, timeLeft);
      }
      catch (Exception e)
      {
        logger.log(Level.WARNING, e.getMessage(), e);
      }
    }
  }
  
  public void sendActionControl(Item item, String actionString, boolean start, int timeLeft)
  {
    for (VirtualZone vz : getWatchers()) {
      try
      {
        vz.sendActionControl(item.getWurmId(), actionString, start, timeLeft);
      }
      catch (Exception e)
      {
        logger.log(Level.WARNING, e.getMessage(), e);
      }
    }
  }
  
  public void sendRotate(Item item, float rotation)
  {
    for (VirtualZone vz : getWatchers()) {
      try
      {
        vz.sendRotate(item, rotation);
      }
      catch (Exception e)
      {
        logger.log(Level.WARNING, e.getMessage(), e);
      }
    }
  }
  
  public int getLayer()
  {
    if (this.surfaced) {
      return 0;
    }
    return -1;
  }
  
  public void sendAddTileEffect(AreaSpellEffect effect, boolean loop)
  {
    for (VirtualZone vz : getWatchers()) {
      try
      {
        vz.addAreaSpellEffect(effect, loop);
      }
      catch (Exception e)
      {
        logger.log(Level.WARNING, e.getMessage(), e);
      }
    }
  }
  
  public void sendAddQuickTileEffect(byte effect, int floorOffset)
  {
    for (VirtualZone vz : getWatchers()) {
      try
      {
        vz.sendAddTileEffect(this.tilex, this.tiley, getLayer(), effect, floorOffset, false);
      }
      catch (Exception e)
      {
        logger.log(Level.WARNING, e.getMessage(), e);
      }
    }
  }
  
  public void sendRemoveTileEffect(AreaSpellEffect effect)
  {
    for (VirtualZone vz : getWatchers()) {
      try
      {
        vz.removeAreaSpellEffect(effect);
      }
      catch (Exception e)
      {
        logger.log(Level.WARNING, e.getMessage(), e);
      }
    }
  }
  
  public void updateFenceState(Fence fence)
  {
    for (VirtualZone vz : getWatchers()) {
      try
      {
        vz.updateFenceDamageState(fence);
      }
      catch (Exception e)
      {
        logger.log(Level.WARNING, e.getMessage(), e);
      }
    }
  }
  
  public void updateTargetStatus(long targetId, byte type, float status)
  {
    for (VirtualZone vz : getWatchers()) {
      try
      {
        vz.updateTargetStatus(targetId, type, status);
      }
      catch (Exception e)
      {
        logger.log(Level.WARNING, e.getMessage(), e);
      }
    }
  }
  
  public void updateWallDamageState(Wall wall)
  {
    for (VirtualZone vz : getWatchers()) {
      try
      {
        vz.updateWallDamageState(wall);
      }
      catch (Exception e)
      {
        logger.log(Level.WARNING, e.getMessage(), e);
      }
    }
  }
  
  public void updateFloorDamageState(Floor floor)
  {
    for (VirtualZone vz : getWatchers()) {
      try
      {
        vz.updateFloorDamageState(floor);
      }
      catch (Exception e)
      {
        logger.log(Level.WARNING, e.getMessage(), e);
      }
    }
  }
  
  public void updateBridgePartDamageState(BridgePart bridgePart)
  {
    for (VirtualZone vz : getWatchers()) {
      try
      {
        vz.updateBridgePartDamageState(bridgePart);
      }
      catch (Exception e)
      {
        logger.log(Level.WARNING, e.getMessage(), e);
      }
    }
  }
  
  private AreaSpellEffect getAreaEffect()
  {
    return AreaSpellEffect.getEffect(this.tilex, this.tiley, getLayer());
  }
  
  public final boolean isInPvPZone()
  {
    return Zones.isOnPvPServer(this.tilex, this.tiley);
  }
  
  public final void lightLamps()
  {
    if (this.vitems != null) {
      for (Item i : this.vitems.getAllItemsAsSet()) {
        if (i.isStreetLamp()) {
          if (i.isPlanted())
          {
            i.setAuxData((byte)120);
            i.setTemperature((short)10000);
          }
        }
      }
    }
  }
  
  public String toString()
  {
    return "VolaTile [X: " + this.tilex + ", Y: " + this.tiley + ", surf=" + this.surfaced + "]";
  }
  
  public Floor[] getFloors(int startHeightOffset, int endHeightOffset)
  {
    if (this.floors == null) {
      return emptyFloors;
    }
    List<Floor> toReturn = new ArrayList();
    for (Floor floor : this.floors) {
      if ((floor.getHeightOffset() >= startHeightOffset) && (floor.getHeightOffset() <= endHeightOffset)) {
        toReturn.add(floor);
      }
    }
    return (Floor[])toReturn.toArray(new Floor[toReturn.size()]);
  }
  
  @Nullable
  public Floor getFloor(int floorLevel)
  {
    if (this.floors != null) {
      for (Floor floor : this.floors) {
        if (floor.getFloorLevel() == floorLevel) {
          return floor;
        }
      }
    }
    return null;
  }
  
  public Floor[] getFloors()
  {
    if (this.floors == null) {
      return emptyFloors;
    }
    return (Floor[])this.floors.toArray(new Floor[this.floors.size()]);
  }
  
  public final void addFloor(Floor floor)
  {
    if (this.floors == null) {
      this.floors = new HashSet();
    }
    this.floors.add(floor);
    if (floor.isStair()) {
      Stairs.addStair(hashCode(), floor.getFloorLevel());
    }
    if (this.vitems != null) {
      for (Item pile : this.vitems.getPileItems()) {
        pile.updatePosZ(this);
      }
    }
    if (this.watchers != null) {
      for (VirtualZone vz : getWatchers()) {
        try
        {
          vz.updateFloor(this.structure.getWurmId(), floor);
        }
        catch (Exception e)
        {
          logger.log(Level.WARNING, e.getMessage(), e);
        }
      }
    }
  }
  
  public final void removeFloor(Blocker floor)
  {
    if (this.floors != null)
    {
      Floor toRem = null;
      for (Floor fl : this.floors) {
        if (fl.getId() == floor.getId())
        {
          toRem = fl;
          break;
        }
      }
      if (toRem != null) {
        removeFloor(toRem);
      }
    }
  }
  
  public final void removeFloor(Floor floor)
  {
    int floorLevel = floor.getFloorLevel();
    if (this.floors != null)
    {
      this.floors.remove(floor);
      if (floor.isStair()) {
        Stairs.removeStair(hashCode(), floorLevel);
      }
      if (this.floors.size() == 0) {
        this.floors = null;
      }
    }
    if (this.structure == null) {
      return;
    }
    VirtualZone localVirtualZone1;
    VirtualZone vz;
    if (this.watchers != null)
    {
      VirtualZone[] arrayOfVirtualZone1 = getWatchers();int i = arrayOfVirtualZone1.length;
      for (localVirtualZone1 = 0; localVirtualZone1 < i; localVirtualZone1++)
      {
        vz = arrayOfVirtualZone1[localVirtualZone1];
        try
        {
          vz.removeFloor(this.structure.getWurmId(), floor);
        }
        catch (Exception e)
        {
          logger.log(Level.WARNING, e.getMessage(), e);
        }
      }
    }
    Object pileItem;
    if (floorLevel > 0)
    {
      destroyPileItem(floorLevel);
    }
    else if (this.vitems != null)
    {
      pileItem = this.vitems.getPileItem(floorLevel);
      if (pileItem != null)
      {
        ((Item)pileItem).updatePosZ(this);
        
        VirtualZone[] arrayOfVirtualZone2 = getWatchers();localVirtualZone1 = arrayOfVirtualZone2.length;
        for (vz = 0; vz < localVirtualZone1; vz++)
        {
          VirtualZone vz = arrayOfVirtualZone2[vz];
          try
          {
            if (vz.isVisible((Item)pileItem, this))
            {
              vz.removeItem((Item)pileItem);
              vz.addItem((Item)pileItem, this, true);
            }
          }
          catch (Exception e)
          {
            logger.log(Level.WARNING, e.getMessage(), e);
          }
        }
      }
    }
    if (this.vitems != null) {
      for (Item item : this.vitems.getAllItemsAsArray()) {
        if ((item.isDecoration()) && (item.getFloorLevel() == floorLevel))
        {
          item.updatePosZ(this);
          item.updateIfGroundItem();
        }
      }
    }
    if (this.creatures != null) {
      for (pileItem = this.creatures.iterator(); ((Iterator)pileItem).hasNext();)
      {
        Creature c = (Creature)((Iterator)pileItem).next();
        if (c.getFloorLevel() == floorLevel) {
          if (!c.isPlayer())
          {
            float oldposz = c.getPositionZ();
            float newPosz = c.calculatePosZ();
            float diffz = oldposz - newPosz;
            c.setPositionZ(newPosz);
            c.moved(0, 0, (int)(diffz * 100.0F), 0, 0);
          }
        }
      }
    }
    checkDeletion();
  }
  
  public final void addBridgePart(BridgePart bridgePart)
  {
    if (this.bridgeParts == null) {
      this.bridgeParts = new HashSet();
    }
    this.bridgeParts.add(bridgePart);
    if (this.vitems != null) {
      for (Item pile : this.vitems.getPileItems()) {
        pile.updatePosZ(this);
      }
    }
    if (this.watchers != null) {
      for (VirtualZone vz : getWatchers()) {
        try
        {
          vz.updateBridgePart(this.structure.getWurmId(), bridgePart);
        }
        catch (Exception e)
        {
          logger.log(Level.WARNING, e.getMessage(), e);
        }
      }
    }
  }
  
  public final void removeBridgePart(BridgePart bridgePart)
  {
    if (this.bridgeParts != null)
    {
      this.bridgeParts.remove(bridgePart);
      if (this.bridgeParts.size() == 0) {
        this.bridgeParts = null;
      }
    }
    if (this.structure == null) {
      return;
    }
    if (this.watchers != null) {
      for (VirtualZone vz : getWatchers()) {
        try
        {
          vz.removeBridgePart(this.structure.getWurmId(), bridgePart);
        }
        catch (Exception e)
        {
          logger.log(Level.WARNING, e.getMessage(), e);
        }
      }
    }
    if (this.vitems != null) {
      for (Item pile : this.vitems.getPileItems())
      {
        pile.setOnBridge(-10L);
        pile.updatePosZ(this);
        for (VirtualZone vz : getWatchers()) {
          try
          {
            if (vz.isVisible(pile, this))
            {
              vz.removeItem(pile);
              vz.addItem(pile, this, true);
            }
          }
          catch (Exception e)
          {
            logger.log(Level.WARNING, e.getMessage(), e);
          }
        }
      }
    }
    if (this.vitems != null) {
      for (Item item : this.vitems.getAllItemsAsArray()) {
        if (item.getBridgeId() == this.structure.getWurmId())
        {
          item.setOnBridge(-10L);
          item.updatePosZ(this);
          item.updateIfGroundItem();
        }
      }
    }
    if (this.creatures != null) {
      for (??? = this.creatures.iterator(); ((Iterator)???).hasNext();)
      {
        Creature c = (Creature)((Iterator)???).next();
        if (c.getBridgeId() == this.structure.getWurmId())
        {
          c.setBridgeId(-10L);
          if (!c.isPlayer())
          {
            float oldposz = c.getPositionZ();
            float newPosz = c.calculatePosZ();
            float diffz = oldposz - newPosz;
            c.setPositionZ(newPosz);
            c.moved(0, 0, (int)(diffz * 100.0F), 0, 0);
          }
        }
      }
    }
    checkDeletion();
  }
  
  public final Floor getTopFloor()
  {
    if (this.floors != null)
    {
      Floor toret = null;
      for (Floor floor : this.floors) {
        if ((toret == null) || (floor.getFloorLevel() > toret.getFloorLevel())) {
          toret = floor;
        }
      }
      return toret;
    }
    return null;
  }
  
  public final Fence getTopFence()
  {
    if (this.fences != null)
    {
      Fence toret = null;
      for (Fence f : this.fences.values()) {
        if ((toret == null) || (f.getFloorLevel() > toret.getFloorLevel())) {
          toret = f;
        }
      }
      return toret;
    }
    return null;
  }
  
  public final Wall getTopWall()
  {
    if (this.walls != null)
    {
      Wall toret = null;
      for (Wall f : this.walls) {
        if (((toret == null) || (f.getFloorLevel() > toret.getFloorLevel())) && 
          (f.isFinished())) {
          toret = f;
        }
      }
      return toret;
    }
    return null;
  }
  
  public final boolean isNextTo(VolaTile t)
  {
    if ((t == null) || (t.getLayer() != getLayer())) {
      return false;
    }
    if (((t.getTileX() == getTileX() - 1) || (t.getTileX() == getTileX() + 1)) && (t.getTileY() == getTileY())) {
      return true;
    }
    if ((t.getTileX() == getTileX()) && ((t.getTileY() == getTileY() - 1) || (t.getTileY() == getTileY() + 1))) {
      return true;
    }
    return false;
  }
  
  public final void damageFloors(int minFloorLevel, int maxFloorLevel, float addedDamage)
  {
    Floor[] floorArr = getFloors(minFloorLevel * 30, maxFloorLevel * 30);
    for (Floor floor : floorArr)
    {
      floor.setDamage(floor.getDamage() + addedDamage);
      if (floor.getDamage() >= 100.0F) {
        removeFloor(floor);
      }
    }
  }
  
  public final boolean hasStair(int floorLevel)
  {
    return Stairs.hasStair(hashCode(), floorLevel);
  }
  
  public Item findHive(int hiveType)
  {
    if (this.vitems != null) {
      for (Item item : this.vitems.getAllItemsAsArray()) {
        if (item.getTemplateId() == hiveType) {
          return item;
        }
      }
    }
    return null;
  }
  
  public Item findHive(int hiveType, boolean withQueen)
  {
    if (this.vitems != null) {
      for (Item item : this.vitems.getAllItemsAsArray()) {
        if (item.getTemplateId() == hiveType)
        {
          if ((withQueen) && (item.getAuxData() > 0)) {
            return item;
          }
          if ((!withQueen) && (item.getAuxData() == 0)) {
            return item;
          }
        }
      }
    }
    return null;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\zones\VolaTile.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */