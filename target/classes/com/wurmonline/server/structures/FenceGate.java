package com.wurmonline.server.structures;

import com.wurmonline.server.Items;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.Players;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.CreatureStatus;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.kingdom.Kingdoms;
import com.wurmonline.server.players.Permissions;
import com.wurmonline.server.players.Permissions.Allow;
import com.wurmonline.server.players.PermissionsHistories;
import com.wurmonline.server.players.PermissionsPlayerList;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.players.PlayerInfoFactory;
import com.wurmonline.server.tutorial.MissionTriggers;
import com.wurmonline.server.villages.NoSuchVillageException;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.villages.VillageRole;
import com.wurmonline.server.villages.Villages;
import com.wurmonline.server.zones.VirtualZone;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zones;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;

public abstract class FenceGate
  extends Door
  implements MiscConstants
{
  private static final Logger logger = Logger.getLogger(FenceGate.class.getName());
  final Fence fence;
  private Village village = null;
  int villageId = -1;
  int openTime = 0;
  int closeTime = 0;
  static final Map<Long, FenceGate> gates = new ConcurrentHashMap();
  
  FenceGate(Fence aFence)
  {
    this.fence = aFence;
    gates.put(new Long(aFence.getId()), this);
    try
    {
      load();
    }
    catch (IOException iox)
    {
      logger.log(Level.WARNING, "Failed to load/save " + this.name + "," + aFence.getId(), iox);
    }
  }
  
  public final float getQualityLevel()
  {
    return this.fence.getCurrentQualityLevel();
  }
  
  public final void setVillage(@Nullable Village vill)
  {
    this.village = vill;
    if (vill != null) {
      setIsManaged(true, null);
    } else if (this.villageId != -1) {
      setIsManaged(false, null);
    }
  }
  
  public final Village getVillage()
  {
    return this.village;
  }
  
  private Village getPermissionsVillage()
  {
    Village vill = getVillage();
    if (vill != null) {
      return vill;
    }
    long wid = getOwnerId();
    if (wid != -10L) {
      return Villages.getVillageForCreature(wid);
    }
    return null;
  }
  
  public final int getVillageId()
  {
    return this.villageId;
  }
  
  public final Village getManagedByVillage()
  {
    if (this.villageId >= 0) {
      try
      {
        return Villages.getVillage(this.villageId);
      }
      catch (NoSuchVillageException localNoSuchVillageException) {}
    }
    return null;
  }
  
  public final Fence getFence()
  {
    return this.fence;
  }
  
  public final int getOpenTime()
  {
    return this.openTime;
  }
  
  public final int getCloseTime()
  {
    return this.closeTime;
  }
  
  public final void addToTiles()
  {
    this.innerTile = this.fence.getTile();
    int tilex = this.innerTile.getTileX();
    int tiley = this.innerTile.getTileY();
    this.innerTile.addDoor(this);
    if (this.fence.isHorizontal())
    {
      this.outerTile = Zones.getOrCreateTile(tilex, tiley - 1, this.fence.isOnSurface());
      this.outerTile.addDoor(this);
    }
    else
    {
      this.outerTile = Zones.getOrCreateTile(tilex - 1, tiley, this.fence.isOnSurface());
      this.outerTile.addDoor(this);
    }
    calculateArea();
  }
  
  public final boolean canBeOpenedBy(Creature creature, boolean passedThroughDoor)
  {
    if (creature == null) {
      return false;
    }
    if (MissionTriggers.isDoorOpen(creature, getWurmId(), 1)) {
      return true;
    }
    if (creature.getPower() > 1) {
      return true;
    }
    if ((creature.isKingdomGuard()) || (creature.isGhost())) {
      return true;
    }
    if ((creature.getLeader() != null) && 
      (canBeOpenedBy(creature.getLeader(), false))) {
      return true;
    }
    if (!creature.canOpenDoors()) {
      return false;
    }
    if ((this.village != null) && (this.village.isEnemy(creature))) {
      return canBeUnlockedBy(creature);
    }
    if ((creature.isPlayer()) && (mayPass(creature))) {
      return true;
    }
    return canBeUnlockedBy(creature);
  }
  
  public final boolean canBeUnlockedBy(Creature creature)
  {
    if (creature.getPower() > 1) {
      return true;
    }
    if (this.lockCounter > 0)
    {
      creature.sendToLoggers("Lock counter=" + this.lockCounter);
      return true;
    }
    if (this.lock == -10L)
    {
      creature.sendToLoggers("No lock ");
      return true;
    }
    Item doorlock = null;
    try
    {
      doorlock = Items.getItem(this.lock);
    }
    catch (NoSuchItemException nsi)
    {
      logger.log(Level.INFO, "Lock has decayed? Id was " + this.lock);
      
      creature.sendToLoggers("Lock id " + this.lock + " has decayed?");
      return true;
    }
    if (doorlock.isLocked())
    {
      Item[] items = creature.getKeys();
      for (int x = 0; x < items.length; x++) {
        if (doorlock.isUnlockedBy(items[x].getWurmId()))
        {
          creature.sendToLoggers("I have key");
          return true;
        }
      }
      if (mayLock(creature))
      {
        creature.sendToLoggers("I have Lock Permission");
        return true;
      }
    }
    else
    {
      creature.sendToLoggers("It's not locked");
      return true;
    }
    return false;
  }
  
  public final void creatureMoved(Creature creature, int diffTileX, int diffTileY)
  {
    if (covers(creature.getStatus().getPositionX(), creature.getStatus().getPositionY(), creature.getPositionZ(), creature
      .getFloorLevel(), creature.followsGround())) {
      addCreature(creature);
    } else {
      removeCreature(creature);
    }
  }
  
  public final void removeCreature(Creature creature)
  {
    if (this.creatures != null)
    {
      Iterator<VirtualZone> it;
      if (this.creatures.contains(creature))
      {
        this.creatures.remove(creature);
        if ((isOpen()) && (!creature.isGhost()))
        {
          creature.getCommunicator().sendCloseFence(this.fence, false, true);
          boolean close = true;
          for (Iterator<Creature> it = this.creatures.iterator(); it.hasNext();)
          {
            Creature checked = (Creature)it.next();
            if (canBeOpenedBy(checked, false)) {
              close = false;
            }
          }
          if (close)
          {
            close();
            if ((this.watchers != null) && (creature.isVisible())) {
              for (it = this.watchers.iterator(); it.hasNext();)
              {
                VirtualZone z = (VirtualZone)it.next();
                if (z.getWatcher() != creature) {
                  z.closeFence(this.fence, false, false);
                }
              }
            }
          }
        }
      }
      if (this.creatures.size() == 0) {
        this.creatures = null;
      }
    }
  }
  
  public final boolean containsCreature(Creature creature)
  {
    if (this.creatures == null) {
      return false;
    }
    return this.creatures.contains(creature);
  }
  
  public void updateDoor(Creature creature, Item key, boolean removedKey)
  {
    boolean isOpenToCreature = canBeOpenedBy(creature, false);
    if (removedKey)
    {
      if (this.creatures != null)
      {
        Iterator<VirtualZone> it;
        if (this.creatures.contains(creature)) {
          if (!isOpenToCreature) {
            if (canBeUnlockedByKey(key))
            {
              creature.getCommunicator().sendCloseFence(this.fence, false, true);
              if (isOpen())
              {
                boolean close = true;
                for (Iterator<Creature> it = this.creatures.iterator(); it.hasNext();)
                {
                  Creature checked = (Creature)it.next();
                  if (canBeOpenedBy(checked, false)) {
                    close = false;
                  }
                }
                if ((close) && (creature.isVisible()) && (!creature.isGhost()))
                {
                  close();
                  if ((this.watchers != null) && (creature.isVisible())) {
                    for (it = this.watchers.iterator(); it.hasNext();)
                    {
                      VirtualZone z = (VirtualZone)it.next();
                      if (z.getWatcher() != creature) {
                        z.closeFence(this.fence, false, false);
                      }
                    }
                  }
                }
              }
            }
          }
        }
        if (this.creatures.size() == 0) {
          this.creatures = null;
        }
      }
    }
    else if (this.creatures != null) {
      if (this.creatures.contains(creature)) {
        if (isOpenToCreature) {
          if (canBeUnlockedByKey(key)) {
            if ((!isOpen()) && (creature.isVisible()) && (!creature.isGhost()))
            {
              Iterator<VirtualZone> it;
              if ((this.watchers != null) && (creature.isVisible())) {
                for (it = this.watchers.iterator(); it.hasNext();)
                {
                  VirtualZone z = (VirtualZone)it.next();
                  if (z.getWatcher() != creature) {
                    z.openFence(this.fence, false, false);
                  }
                }
              }
              open();
              creature.getCommunicator().sendOpenFence(this.fence, true, true);
            }
          }
        }
      }
    }
  }
  
  public final boolean addCreature(Creature creature)
  {
    if (this.creatures == null) {
      this.creatures = new HashSet();
    }
    if (!this.creatures.contains(creature))
    {
      this.creatures.add(creature);
      if ((canBeOpenedBy(creature, false)) && (!creature.isGhost())) {
        if (!isOpen())
        {
          Iterator<VirtualZone> it;
          if ((this.watchers != null) && (creature.isVisible())) {
            for (it = this.watchers.iterator(); it.hasNext();)
            {
              VirtualZone z = (VirtualZone)it.next();
              if (z.getWatcher() != creature) {
                z.openFence(this.fence, false, false);
              }
            }
          }
          open();
          if (creature.getEnemyPresense() > 0) {
            if (getVillage() == null) {
              setLockCounter((short)120);
            }
          }
          creature.getCommunicator().sendOpenFence(this.fence, true, true);
        }
        else
        {
          creature.getCommunicator().sendOpenFence(this.fence, true, true);
        }
      }
      return true;
    }
    return false;
  }
  
  public final boolean keyFits(long keyId)
    throws NoSuchLockException
  {
    if (this.lock == -10L) {
      throw new NoSuchLockException("No ID");
    }
    try
    {
      Item doorlock = Items.getItem(this.lock);
      return doorlock.isUnlockedBy(keyId);
    }
    catch (NoSuchItemException nsi)
    {
      logger.log(Level.INFO, "Lock has decayed? Id was " + this.lock);
    }
    return false;
  }
  
  public final boolean isOpenTime()
  {
    return false;
  }
  
  final void close()
  {
    this.open = false;
  }
  
  final void open()
  {
    this.open = true;
  }
  
  public final void removeFromVillage()
  {
    if (this.village != null) {
      this.village.removeGate(this);
    }
  }
  
  public static final FenceGate getFenceGate(long id)
  {
    Long lid = new Long(id);
    FenceGate toReturn = (FenceGate)gates.get(lid);
    return toReturn;
  }
  
  public static final FenceGate[] getAllGates()
  {
    return (FenceGate[])gates.values().toArray(new FenceGate[gates.size()]);
  }
  
  public static final FenceGate[] getManagedGatesFor(Player player, int villageId, boolean includeAll)
  {
    Set<FenceGate> fenceGates = new HashSet();
    for (FenceGate gate : gates.values()) {
      if (((gate.canManage(player)) || ((villageId >= 0) && (gate.getVillageId() == villageId))) && (
        (includeAll) || (gate.hasLock()))) {
        fenceGates.add(gate);
      }
    }
    return (FenceGate[])fenceGates.toArray(new FenceGate[fenceGates.size()]);
  }
  
  public static final FenceGate[] getOwnedGatesFor(Player player)
  {
    Set<FenceGate> fenceGates = new HashSet();
    for (FenceGate gate : gates.values()) {
      if ((gate.isOwner(player)) || (gate.isActualOwner(player.getWurmId()))) {
        fenceGates.add(gate);
      }
    }
    return (FenceGate[])fenceGates.toArray(new FenceGate[fenceGates.size()]);
  }
  
  public static final void unManageGatesFor(int villageId)
  {
    for (FenceGate gate : ) {
      if (gate.getVillageId() == villageId) {
        gate.setIsManaged(false, null);
      }
    }
  }
  
  public final Village getOwnerVillage()
  {
    return Villages.getVillageForCreature(getOwnerId());
  }
  
  public final boolean covers(float x, float y, float posz, int floorLevel, boolean followGround)
  {
    return ((this.fence != null) && (this.fence.isWithinZ(posz + 1.0F, posz, followGround))) || ((isTransition()) && (floorLevel <= 0) && (x >= this.startx) && (x <= this.endx) && (y >= this.starty) && (y <= this.endy));
  }
  
  public final int getFloorLevel()
  {
    return this.fence.getFloorLevel();
  }
  
  public abstract void setOpenTime(int paramInt);
  
  public abstract void setCloseTime(int paramInt);
  
  public abstract void setLock(long paramLong);
  
  public abstract void save()
    throws IOException;
  
  abstract void load()
    throws IOException;
  
  public abstract void delete();
  
  public final long getOwnerId()
  {
    if (this.lock != -10L) {
      try
      {
        Item doorlock = Items.getItem(this.lock);
        
        return doorlock.getLastOwnerId();
      }
      catch (NoSuchItemException nsi)
      {
        return -10L;
      }
    }
    return -10L;
  }
  
  public long getWurmId()
  {
    return this.fence.getId();
  }
  
  public boolean setObjectName(String aNewName, Creature aCreature)
  {
    setName(aNewName);
    
    this.outerTile.updateFence(getFence());
    return true;
  }
  
  public boolean isActualOwner(long playerId)
  {
    long wid = getOwnerId();
    if (this.lock != -10L) {
      return wid == playerId;
    }
    return false;
  }
  
  public boolean isOwner(Creature creature)
  {
    return isOwner(creature.getWurmId());
  }
  
  public boolean isOwner(long playerId)
  {
    if (isManaged())
    {
      Village vill = getManagedByVillage();
      if (vill != null) {
        return vill.isMayor(playerId);
      }
    }
    return isActualOwner(playerId);
  }
  
  public boolean canChangeOwner(Creature creature)
  {
    return (hasLock()) && ((creature.getPower() > 1) || (isActualOwner(creature.getWurmId())));
  }
  
  public String getWarning()
  {
    if (this.lock == -10L) {
      return "NEEDS TO HAVE A LOCK FOR PERMISSIONS TO WORK";
    }
    if (!isLocked()) {
      return "NEEDS TO BE LOCKED OTHERWISE EVERYONE CAN PASS";
    }
    return "";
  }
  
  public PermissionsPlayerList getPermissionsPlayerList()
  {
    return DoorSettings.getPermissionsPlayerList(getWurmId());
  }
  
  public final boolean canHavePermissions()
  {
    return isLocked();
  }
  
  public final boolean mayShowPermissions(Creature creature)
  {
    return (hasLock()) && (mayManage(creature));
  }
  
  public boolean canManage(Creature creature)
  {
    if (DoorSettings.isExcluded(this, creature)) {
      return false;
    }
    if (DoorSettings.canManage(this, creature)) {
      return true;
    }
    if (creature.getCitizenVillage() == null) {
      return false;
    }
    Village vill = getManagedByVillage();
    if (vill == null) {
      return false;
    }
    if (!vill.isCitizen(creature)) {
      return false;
    }
    return vill.isActionAllowed((short)667, creature);
  }
  
  public boolean mayManage(Creature creature)
  {
    if (creature.getPower() > 1) {
      return true;
    }
    return canManage(creature);
  }
  
  public boolean isManaged()
  {
    if (this.fence == null) {
      return false;
    }
    return this.fence.getSettings().hasPermission(Permissions.Allow.SETTLEMENT_MAY_MANAGE.getBit());
  }
  
  public boolean isManageEnabled(Player player)
  {
    if (player.getPower() > 1) {
      return true;
    }
    if (isManaged())
    {
      Village vil = getVillage();
      if (vil != null) {
        return false;
      }
    }
    return isOwner(player);
  }
  
  public void setIsManaged(boolean newIsManaged, @Nullable Player player)
  {
    if (this.fence != null)
    {
      int oldId = this.villageId;
      if (newIsManaged)
      {
        Village v = getVillage();
        if (v != null)
        {
          setVillageId(v.getId());
        }
        else
        {
          Village cv = getOwnerVillage();
          if (cv != null) {
            setVillageId(cv.getId());
          } else {
            return;
          }
        }
      }
      else
      {
        setVillageId(-1);
      }
      if ((oldId != this.villageId) && (DoorSettings.exists(getWurmId())))
      {
        DoorSettings.remove(getWurmId());
        PermissionsHistories.addHistoryEntry(getWurmId(), System.currentTimeMillis(), -10L, "Auto", "Cleared Permissions");
      }
      this.fence.getSettings().setPermissionBit(Permissions.Allow.SETTLEMENT_MAY_MANAGE.getBit(), newIsManaged);
      this.fence.savePermissions();
      try
      {
        save();
      }
      catch (IOException e)
      {
        logger.log(Level.WARNING, e.getMessage(), e);
      }
    }
  }
  
  public String mayManageText(Player aPlayer)
  {
    String vName = "";
    Village vill = getManagedByVillage();
    if (vill != null)
    {
      vName = vill.getName();
    }
    else
    {
      vill = getVillage();
      if (vill != null)
      {
        vName = vill.getName();
      }
      else
      {
        vill = Villages.getVillageForCreature(getOwnerId());
        if (vill != null) {
          vName = vill.getName();
        }
      }
    }
    if (vName.length() > 0) {
      return "Settlement \"" + vName + "\" may manage";
    }
    return vName;
  }
  
  public String mayManageHover(Player aPlayer)
  {
    return "";
  }
  
  public String messageOnTick()
  {
    return "By selecting this you are giving full control to settlement.";
  }
  
  public String questionOnTick()
  {
    return "Are you positive you want to give your control away?";
  }
  
  public String messageUnTick()
  {
    return "By doing this you are reverting the control to owner";
  }
  
  public String questionUnTick()
  {
    return "Are you sure you want them to have control?";
  }
  
  public String getSettlementName()
  {
    String sName = "";
    Village vill = getPermissionsVillage();
    if (vill != null) {
      sName = vill.getName();
    }
    if (sName.length() == 0) {
      return sName;
    }
    return "Citizens of \"" + sName + "\"";
  }
  
  public String getAllianceName()
  {
    String aName = "";
    Village vill = getPermissionsVillage();
    if (vill != null) {
      aName = vill.getAllianceName();
    }
    if (aName.length() == 0) {
      return aName;
    }
    return "Alliance of \"" + aName + "\"";
  }
  
  public String getKingdomName()
  {
    byte kingdom = 0;
    Village vill = getPermissionsVillage();
    if (vill != null) {
      kingdom = vill.kingdom;
    } else {
      kingdom = Players.getInstance().getKingdomForPlayer(getOwnerId());
    }
    return "Kingdom of \"" + Kingdoms.getNameFor(kingdom) + "\"";
  }
  
  public void addDefaultCitizenPermissions()
  {
    if (!getPermissionsPlayerList().exists(-30L))
    {
      int value = DoorSettings.DoorPermissions.PASS.getValue();
      addGuest(-30L, value);
    }
  }
  
  public boolean isCitizen(Creature creature)
  {
    Village vill = getManagedByVillage();
    if (vill == null) {
      vill = getOwnerVillage();
    }
    if (vill != null) {
      return vill.isCitizen(creature);
    }
    return false;
  }
  
  public boolean isAllied(Creature creature)
  {
    Village vill = getManagedByVillage();
    if (vill == null) {
      vill = getOwnerVillage();
    }
    if (vill != null) {
      return vill.isAlly(creature);
    }
    return false;
  }
  
  public boolean isSameKingdom(Creature creature)
  {
    Village vill = getPermissionsVillage();
    if (vill != null) {
      return vill.kingdom == creature.getKingdomId();
    }
    return Players.getInstance().getKingdomForPlayer(getOwnerId()) == creature.getKingdomId();
  }
  
  public boolean isGuest(Creature creature)
  {
    return isGuest(creature.getWurmId());
  }
  
  public boolean isGuest(long playerId)
  {
    return DoorSettings.isGuest(this, playerId);
  }
  
  public void addGuest(long guestId, int aSettings)
  {
    DoorSettings.addPlayer(getWurmId(), guestId, aSettings);
  }
  
  public void removeGuest(long guestId)
  {
    DoorSettings.removePlayer(getWurmId(), guestId);
  }
  
  public final boolean mayPass(Creature creature)
  {
    if (!isLocked()) {
      return true;
    }
    if (DoorSettings.exists(getWurmId()))
    {
      if (DoorSettings.isExcluded(this, creature)) {
        return false;
      }
      if (DoorSettings.mayPass(this, creature)) {
        return true;
      }
    }
    if (isManaged())
    {
      Village vill = getManagedByVillage();
      VillageRole vr = vill == null ? null : vill.getRoleFor(creature);
      return (vr != null) && (vr.mayPassGates());
    }
    return isOwner(creature);
  }
  
  public final boolean mayAttachLock(Creature creature)
  {
    if (hasLock())
    {
      if (this.village != null)
      {
        VillageRole vr = this.village.getRoleFor(creature);
        return (vr != null) && (vr.mayAttachLock());
      }
      return isOwner(creature);
    }
    return true;
  }
  
  public final boolean mayLock(Creature creature)
  {
    if (DoorSettings.exists(getWurmId()))
    {
      if (DoorSettings.isExcluded(this, creature)) {
        return false;
      }
      if (DoorSettings.mayLock(this, creature)) {
        return true;
      }
    }
    if (isManaged())
    {
      Village vill = getManagedByVillage();
      VillageRole vr = vill == null ? null : vill.getRoleFor(creature);
      return (vr != null) && (vr.mayAttachLock());
    }
    return isOwner(creature);
  }
  
  public String getTypeName()
  {
    if (this.fence == null) {
      return "No Fence!";
    }
    return this.fence.getTypeName();
  }
  
  public boolean isNotLockpickable()
  {
    return this.fence.isNotLockpickable();
  }
  
  public boolean setNewOwner(long playerId)
  {
    try
    {
      if ((!isManaged()) && (DoorSettings.exists(getWurmId())))
      {
        DoorSettings.remove(getWurmId());
        PermissionsHistories.addHistoryEntry(getWurmId(), System.currentTimeMillis(), -10L, "Auto", "Cleared Permissions");
      }
      Item theLock = getLock();
      logger.info("Overwritting owner (" + theLock.getLastOwnerId() + ") of lock " + theLock.getWurmId() + " to " + playerId);
      
      theLock.setLastOwnerId(playerId);
      return true;
    }
    catch (NoSuchLockException localNoSuchLockException) {}
    return false;
  }
  
  public String getOwnerName()
  {
    try
    {
      Item theLock = getLock();
      return PlayerInfoFactory.getPlayerName(theLock.getLastOwnerId());
    }
    catch (NoSuchLockException localNoSuchLockException) {}
    return "";
  }
  
  public final boolean maySeeHistory(Creature creature)
  {
    if (creature.getPower() > 1) {
      return true;
    }
    return isOwner(creature);
  }
  
  public boolean convertToNewPermissions()
  {
    boolean didConvert = false;
    if (this.village != null)
    {
      setIsManaged(true, null);
      didConvert = true;
    }
    if (didConvert) {
      this.fence.savePermissions();
    }
    return didConvert;
  }
  
  public boolean fixForNewPermissions()
  {
    boolean didConvert = false;
    if (this.village != null)
    {
      addDefaultCitizenPermissions();
      didConvert = true;
    }
    return didConvert;
  }
  
  public abstract void setVillageId(int paramInt);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\structures\FenceGate.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */