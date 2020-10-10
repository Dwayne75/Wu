package com.wurmonline.server.structures;

import com.wurmonline.server.Items;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.Server;
import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.creatures.AnimalSettings;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.CreatureStatus;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.players.Permissions;
import com.wurmonline.server.players.Permissions.Allow;
import com.wurmonline.server.players.PermissionsHistories;
import com.wurmonline.server.players.PermissionsPlayerList;
import com.wurmonline.server.players.PermissionsPlayerList.ISettings;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.sounds.SoundPlayer;
import com.wurmonline.server.tutorial.MissionTriggers;
import com.wurmonline.server.zones.NoSuchTileException;
import com.wurmonline.server.zones.NoSuchZoneException;
import com.wurmonline.server.zones.VirtualZone;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zone;
import com.wurmonline.server.zones.Zones;
import com.wurmonline.shared.constants.SoundNames;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class Door
  implements MiscConstants, SoundNames, TimeConstants, PermissionsPlayerList.ISettings
{
  Wall wall;
  private static Logger logger = Logger.getLogger(Door.class.getName());
  long lock = -10L;
  long structure = -10L;
  boolean open = false;
  VolaTile outerTile;
  VolaTile innerTile;
  protected int startx;
  protected int starty;
  protected int endx;
  protected int endy;
  Set<Creature> creatures;
  Set<VirtualZone> watchers;
  short lockCounter = 0;
  boolean preAlertLockedStatus = false;
  String name = "";
  
  Door() {}
  
  Door(Wall _wall)
  {
    this.wall = _wall;
  }
  
  public final void setLockCounter(short newcounter)
  {
    if ((this.lockCounter <= 0) || (newcounter <= 0)) {
      playLockSound();
    }
    if (newcounter > this.lockCounter) {
      this.lockCounter = newcounter;
    }
  }
  
  public short getLockCounter()
  {
    return this.lockCounter;
  }
  
  public String getLockCounterTime()
  {
    int m = this.lockCounter / 120;
    int s = this.lockCounter % 120 / 2;
    if (m > 0) {
      return m + " minutes and " + s + " seconds.";
    }
    return s + " seconds.";
  }
  
  private void playLockSound()
  {
    if (this.innerTile != null)
    {
      SoundPlayer.playSound("sound.object.lockunlock", this.innerTile.tilex, this.innerTile.tiley, this.innerTile.isOnSurface(), 1.0F);
      Server.getInstance().broadCastMessage("A loud *click* is heard.", this.innerTile.tilex, this.innerTile.tiley, this.innerTile
        .isOnSurface(), 5);
    }
    else if (this.outerTile != null)
    {
      SoundPlayer.playSound("sound.object.lockunlock", this.outerTile.tilex, this.outerTile.tiley, this.outerTile.isOnSurface(), 1.0F);
      Server.getInstance().broadCastMessage("A loud *click* is heard.", this.outerTile.tilex, this.outerTile.tiley, this.outerTile
        .isOnSurface(), 5);
    }
  }
  
  public final void setStructureId(long structureId)
  {
    this.structure = structureId;
  }
  
  public final long getStructureId()
  {
    return this.structure;
  }
  
  public final void addWatcher(VirtualZone watcher)
  {
    if (this.watchers == null) {
      this.watchers = new HashSet();
    }
    if (!this.watchers.contains(watcher)) {
      this.watchers.add(watcher);
    }
  }
  
  public final void removeWatcher(VirtualZone watcher)
  {
    if (this.watchers != null) {
      if (this.watchers.contains(watcher)) {
        this.watchers.remove(watcher);
      }
    }
  }
  
  public final String getName()
  {
    return this.name;
  }
  
  public final VolaTile getOuterTile()
  {
    return this.outerTile;
  }
  
  public final VolaTile getInnerTile()
  {
    return this.innerTile;
  }
  
  public int getTileX()
  {
    if (this.outerTile != null) {
      return this.outerTile.tilex;
    }
    if (this.innerTile != null) {
      return this.innerTile.tilex;
    }
    return -1;
  }
  
  public int getTileY()
  {
    if (this.outerTile != null) {
      return this.outerTile.tiley;
    }
    if (this.innerTile != null) {
      return this.innerTile.tiley;
    }
    return -1;
  }
  
  public final Wall getWall()
    throws NoSuchWallException
  {
    if (this.wall == null) {
      throw new NoSuchWallException("null inner wall for tilex=" + getTileX() + ", tiley=" + getTileY() + " structure=" + getStructureId());
    }
    return this.wall;
  }
  
  final void calculateArea()
  {
    int innerTileStartX = this.innerTile.getTileX();
    int outerTileStartX = this.outerTile.getTileX();
    int innerTileStartY = this.innerTile.getTileY();
    int outerTileStartY = this.outerTile.getTileY();
    if (innerTileStartX == outerTileStartX)
    {
      this.starty = ((Math.min(innerTileStartY, outerTileStartY) << 2) + 2);
      this.endy = ((Math.max(innerTileStartY, outerTileStartY) << 2) + 2);
      this.startx = (innerTileStartX << 2);
      this.endx = (innerTileStartX + 1 << 2);
    }
    else
    {
      this.starty = (innerTileStartY << 2);
      this.endy = (innerTileStartY + 1 << 2);
      this.startx = ((Math.min(innerTileStartX, outerTileStartX) << 2) + 2);
      this.endx = ((Math.max(innerTileStartX, outerTileStartX) << 2) + 2);
    }
  }
  
  public final boolean isTransition()
  {
    return (this.innerTile.isTransition()) || (this.outerTile.isTransition());
  }
  
  public boolean covers(float x, float y, float posz, int floorLevel, boolean followGround)
  {
    return ((this.wall != null) && (this.wall.isWithinZ(posz + 1.0F, posz, followGround))) || ((isTransition()) && (floorLevel <= 0) && (x >= this.startx) && (x <= this.endx) && (y >= this.starty) && (y <= this.endy));
  }
  
  public void addToTiles()
  {
    try
    {
      Structure struct = Structures.getStructure(this.structure);
      this.outerTile = this.wall.getOrCreateOuterTile(struct.isSurfaced());
      this.outerTile.addDoor(this);
      this.innerTile = this.wall.getOrCreateInnerTile(struct.isSurfaced());
      this.innerTile.addDoor(this);
      calculateArea();
    }
    catch (NoSuchStructureException nss)
    {
      logger.log(Level.WARNING, "No such structure? structure: " + this.structure, nss);
    }
    catch (NoSuchZoneException nsz)
    {
      logger.log(Level.WARNING, "No such zone - wall: " + this.wall + " - " + nsz.getMessage(), nsz);
    }
    catch (NoSuchTileException nst)
    {
      logger.log(Level.WARNING, "No such tile - wall: " + this.wall + " - " + nst.getMessage(), nst);
    }
  }
  
  public final void removeFromTiles()
  {
    if (this.outerTile != null) {
      this.outerTile.removeDoor(this);
    }
    if (this.innerTile != null) {
      this.innerTile.removeDoor(this);
    }
  }
  
  public boolean canBeOpenedBy(Creature creature, boolean wentThroughDoor)
  {
    if (creature == null) {
      return false;
    }
    if ((creature.isKingdomGuard()) || (creature.isGhost())) {
      return true;
    }
    if (MissionTriggers.isDoorOpen(creature, this.wall.getId(), 1)) {
      return true;
    }
    if (creature.getPower() > 0) {
      return true;
    }
    if ((creature.getLeader() != null) && 
      (canBeOpenedBy(creature.getLeader(), false))) {
      return true;
    }
    if (!creature.canOpenDoors()) {
      return false;
    }
    return canBeUnlockedBy(creature);
  }
  
  public boolean canBeUnlockedByKey(Item key)
  {
    Item doorlock = null;
    try
    {
      doorlock = Items.getItem(this.lock);
    }
    catch (NoSuchItemException nsi)
    {
      return false;
    }
    if (doorlock.isLocked())
    {
      if (doorlock.isUnlockedBy(key.getWurmId())) {
        return true;
      }
      return false;
    }
    return false;
  }
  
  public boolean canBeUnlockedBy(Creature creature)
  {
    return mayPass(creature);
  }
  
  public void creatureMoved(Creature creature, int diffTileX, int diffTileY)
  {
    if (covers(creature.getStatus().getPositionX(), creature.getStatus().getPositionY(), creature.getPositionZ(), creature
      .getFloorLevel(), creature.followsGround()))
    {
      if (!addCreature(creature)) {
        if ((diffTileX != 0) || (diffTileY != 0)) {
          if (!canBeOpenedBy(creature, true)) {
            try
            {
              int tilex = creature.getTileX();
              int tiley = creature.getTileY();
              
              VolaTile tile = Zones.getZone(tilex, tiley, creature.isOnSurface()).getTileOrNull(tilex, tiley);
              if (tile != null)
              {
                if (tile == this.innerTile)
                {
                  int oldTileX = tilex - diffTileX;
                  int oldTileY = tiley - diffTileY;
                  if ((creature instanceof Player))
                  {
                    creature.getCommunicator().sendAlertServerMessage("You cannot enter that building.");
                    logger.log(Level.WARNING, creature.getName() + " a cheater? Passed through door at " + creature
                      .getStatus().getPositionX() + ", " + creature
                      .getStatus().getPositionY() + ", z=" + creature.getPositionZ() + ", minZ=" + this.wall
                      .getMinZ());
                    creature.setTeleportPoints((short)oldTileX, (short)oldTileY, creature
                      .getLayer(), creature.getFloorLevel());
                    creature.startTeleporting();
                    creature.getCommunicator().sendTeleport(false);
                  }
                }
              }
              else {
                logger.log(Level.WARNING, "A door on no tile at " + creature.getStatus().getPositionX() + ", " + creature
                  .getStatus().getPositionY() + ", structure: " + this.structure);
              }
            }
            catch (NoSuchZoneException nsz)
            {
              logger.log(Level.WARNING, "A door in no zone at " + creature.getStatus().getPositionX() + ", " + creature
                .getStatus().getPositionY() + ", structure: " + this.structure + " - " + nsz);
            }
          } else if (this.structure != -10L) {
            if (creature.isPlayer())
            {
              int tilex = creature.getTileX();
              int tiley = creature.getTileY();
              VolaTile tile = Zones.getTileOrNull(tilex, tiley, creature.isOnSurface());
              if (tile == this.innerTile)
              {
                if (creature.getEnemyPresense() > 0) {
                  if (tile.getVillage() == null) {
                    setLockCounter((short)120);
                  }
                }
              }
              else if (tile == this.outerTile)
              {
                int oldTileX = tilex - diffTileX;
                int oldTileY = tiley - diffTileY;
                try
                {
                  VolaTile oldtile = Zones.getZone(oldTileX, oldTileY, creature.isOnSurface()).getTileOrNull(oldTileX, oldTileY);
                  if ((oldtile != null) && (oldtile == this.innerTile)) {
                    if (creature.getEnemyPresense() > 0) {
                      if (oldtile.getVillage() == null) {
                        setLockCounter((short)120);
                      }
                    }
                  }
                }
                catch (NoSuchZoneException nsz)
                {
                  logger.log(Level.WARNING, "A door in no zone at " + creature.getStatus().getPositionX() + ", " + creature
                  
                    .getStatus().getPositionY() + ", structure: " + this.structure + " - " + nsz);
                }
              }
            }
          }
        }
      }
    }
    else {
      removeCreature(creature);
    }
  }
  
  public void updateDoor(Creature creature, Item key, boolean removedKey)
  {
    boolean isOpenToCreature = canBeOpenedBy(creature, false);
    if (removedKey)
    {
      if (this.creatures != null)
      {
        if (this.creatures.contains(creature)) {
          if (!isOpenToCreature) {
            if (canBeUnlockedByKey(key)) {
              if (isOpen())
              {
                boolean close = true;
                for (Creature checked : this.creatures) {
                  if (canBeOpenedBy(checked, false)) {
                    close = false;
                  }
                }
                if ((close) && (creature.isVisible()) && (!creature.isGhost())) {
                  close();
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
          if (canBeUnlockedByKey(key))
          {
            if ((!isOpen()) && (creature.isVisible()) && (!creature.isGhost())) {
              open();
            }
            creature.getCommunicator().sendPassable(true, this);
          }
        }
      }
    }
  }
  
  public void removeCreature(Creature creature)
  {
    if (this.creatures != null)
    {
      if (this.creatures.contains(creature))
      {
        this.creatures.remove(creature);
        creature.setCurrentDoor(null);
        creature.getCommunicator().sendPassable(false, this);
        if (isOpen())
        {
          boolean close = true;
          for (Creature checked : this.creatures) {
            if (canBeOpenedBy(checked, false)) {
              close = false;
            }
          }
          if ((close) && (creature.isVisible()) && (!creature.isGhost())) {
            close();
          }
        }
      }
      if (this.creatures.size() == 0) {
        this.creatures = null;
      }
    }
  }
  
  public boolean addCreature(Creature creature)
  {
    if (this.creatures == null) {
      this.creatures = new HashSet();
    }
    if (!this.creatures.contains(creature))
    {
      this.creatures.add(creature);
      creature.setCurrentDoor(this);
      if (canBeOpenedBy(creature, false))
      {
        if ((!isOpen()) && (creature.isVisible()) && (!creature.isGhost())) {
          open();
        }
        creature.getCommunicator().sendPassable(true, this);
      }
      return true;
    }
    return false;
  }
  
  public void setLock(long lockid)
  {
    this.lock = lockid;
    try
    {
      save();
    }
    catch (IOException iox)
    {
      logger.log(Level.WARNING, "Failed to save door for structure with id " + this.structure);
    }
  }
  
  public final long getLockId()
    throws NoSuchLockException
  {
    if (this.lock == -10L) {
      throw new NoSuchLockException("No ID");
    }
    return this.lock;
  }
  
  boolean keyFits(long keyId)
    throws NoSuchLockException
  {
    if (this.lock == -10L) {
      throw new NoSuchLockException("No ID");
    }
    try
    {
      Structure struct = Structures.getStructure(this.structure);
      if (struct.getWritId() == keyId) {
        return true;
      }
    }
    catch (NoSuchStructureException nss)
    {
      logger.log(Level.WARNING, "This door's structure does not exist! " + this.startx + ", " + this.starty + "-" + this.endx + ", " + this.endy + ", structure: " + this.structure + " - " + nss, nss);
    }
    try
    {
      Item doorlock = Items.getItem(this.lock);
      return doorlock.isUnlockedBy(keyId);
    }
    catch (NoSuchItemException nsi)
    {
      logger.log(Level.INFO, "Lock has decayed? Id was " + this.lock + ", structure: " + this.structure + " - " + nsi);
    }
    return false;
  }
  
  public final boolean isOpen()
  {
    return this.open;
  }
  
  void close()
  {
    if ((this.wall != null) && (this.wall.isFinished()))
    {
      if (this.wall.isAlwaysOpen() == true) {
        return;
      }
      if (this.innerTile != null) {
        SoundPlayer.playSound("sound.door.close", this.innerTile.tilex, this.innerTile.tiley, this.innerTile.isOnSurface(), 1.0F);
      } else if (this.outerTile != null) {
        SoundPlayer.playSound("sound.door.close", this.outerTile.tilex, this.outerTile.tiley, this.outerTile.isOnSurface(), 1.0F);
      }
    }
    this.open = false;
    Iterator<VirtualZone> it;
    if (this.watchers != null) {
      for (it = this.watchers.iterator(); it.hasNext();)
      {
        VirtualZone z = (VirtualZone)it.next();
        z.closeDoor(this);
      }
    }
  }
  
  void open()
  {
    if ((this.wall != null) && (this.wall.isFinished()))
    {
      if (this.wall.isAlwaysOpen() == true) {
        return;
      }
      if (this.innerTile != null) {
        SoundPlayer.playSound("sound.door.open", this.innerTile.tilex, this.innerTile.tiley, this.innerTile.isOnSurface(), 1.0F);
      } else if (this.outerTile != null) {
        SoundPlayer.playSound("sound.door.open", this.outerTile.tilex, this.outerTile.tiley, this.outerTile.isOnSurface(), 1.0F);
      }
    }
    this.open = true;
    Iterator<VirtualZone> it;
    if (this.watchers != null) {
      for (it = this.watchers.iterator(); it.hasNext();)
      {
        VirtualZone z = (VirtualZone)it.next();
        z.openDoor(this);
      }
    }
  }
  
  public float getQualityLevel()
  {
    return this.wall.getCurrentQualityLevel();
  }
  
  public final Item getLock()
    throws NoSuchLockException
  {
    if (this.lock == -10L) {
      throw new NoSuchLockException("No ID");
    }
    try
    {
      return Items.getItem(this.lock);
    }
    catch (NoSuchItemException nsi)
    {
      throw new NoSuchLockException(nsi);
    }
  }
  
  public final void poll()
  {
    if (this.lockCounter > 0)
    {
      this.lockCounter = ((short)(this.lockCounter - 1));
      if (this.lockCounter == 0) {
        playLockSound();
      }
    }
  }
  
  public final boolean startAlert(boolean playSound)
  {
    this.preAlertLockedStatus = isLocked();
    if (!this.preAlertLockedStatus)
    {
      lock(playSound);
      
      return true;
    }
    return false;
  }
  
  public final boolean endAlert(boolean playSound)
  {
    if (!this.preAlertLockedStatus)
    {
      unlock(playSound);
      
      return true;
    }
    return false;
  }
  
  public final void lock(boolean playSound)
  {
    try
    {
      Item lLock = getLock();
      lLock.lock();
      if (playSound) {
        playLockSound();
      }
    }
    catch (NoSuchLockException localNoSuchLockException) {}
  }
  
  public final void unlock(boolean playSound)
  {
    try
    {
      Item lLock = getLock();
      lLock.unlock();
      if (playSound) {
        playLockSound();
      }
    }
    catch (NoSuchLockException localNoSuchLockException) {}
  }
  
  public final boolean isUnlocked()
  {
    return !isLocked();
  }
  
  public final boolean isLocked()
  {
    if (this.lockCounter > 0) {
      return false;
    }
    try
    {
      Item lLock = getLock();
      return lLock.isLocked();
    }
    catch (NoSuchLockException nsi) {}
    return false;
  }
  
  public void setNewName(String newname)
  {
    this.name = newname;
    
    this.innerTile.updateWall(this.wall);
    this.outerTile.updateWall(this.wall);
  }
  
  public abstract void save()
    throws IOException;
  
  abstract void load()
    throws IOException;
  
  public abstract void delete();
  
  public abstract void setName(String paramString);
  
  public int getFloorLevel()
  {
    return this.wall.getFloorLevel();
  }
  
  public int getMaxAllowed()
  {
    return AnimalSettings.getMaxAllowed();
  }
  
  public long getWurmId()
  {
    return this.wall.getId();
  }
  
  public int getTemplateId()
  {
    return -10;
  }
  
  public String getObjectName()
  {
    return getName();
  }
  
  public boolean setObjectName(String aNewName, Creature aCreature)
  {
    setName(aNewName);
    return true;
  }
  
  public boolean isActualOwner(long playerId)
  {
    return isOwner(playerId);
  }
  
  public boolean isOwner(Creature creature)
  {
    return isOwner(creature.getWurmId());
  }
  
  public boolean isOwner(long playerId)
  {
    try
    {
      Structure struct = Structures.getStructure(this.structure);
      return struct.isOwner(playerId);
    }
    catch (NoSuchStructureException e) {}
    return false;
  }
  
  public boolean canChangeName(Creature creature)
  {
    return (isOwner(creature)) || (creature.getPower() > 1);
  }
  
  public boolean canChangeOwner(Creature creature)
  {
    return false;
  }
  
  private boolean showWarning()
  {
    try
    {
      getLock();
      return false;
    }
    catch (NoSuchLockException e) {}
    return true;
  }
  
  public String getWarning()
  {
    if (showWarning()) {
      return "NEEDS TO HAVE A LOCK!";
    }
    return "";
  }
  
  public PermissionsPlayerList getPermissionsPlayerList()
  {
    return DoorSettings.getPermissionsPlayerList(getWurmId());
  }
  
  public boolean isManaged()
  {
    if (this.wall == null) {
      return false;
    }
    return this.wall.getSettings().hasPermission(Permissions.Allow.SETTLEMENT_MAY_MANAGE.getBit());
  }
  
  public boolean isManageEnabled(Player player)
  {
    return (mayManage(player)) || (player.getPower() > 1);
  }
  
  public void setIsManaged(boolean newIsManaged, Player player)
  {
    if (this.wall != null)
    {
      if ((newIsManaged) && (DoorSettings.exists(getWurmId())))
      {
        DoorSettings.remove(getWurmId());
        if (player != null) {
          PermissionsHistories.addHistoryEntry(getWurmId(), System.currentTimeMillis(), player
            .getWurmId(), player.getName(), "Removed all permissions");
        }
      }
      this.wall.getSettings().setPermissionBit(Permissions.Allow.SETTLEMENT_MAY_MANAGE.getBit(), newIsManaged);
      this.wall.savePermissions();
    }
  }
  
  public void addDefaultCitizenPermissions() {}
  
  public boolean isCitizen(Creature aCreature)
  {
    try
    {
      Structure struct = Structures.getStructure(this.structure);
      return struct.isCitizen(aCreature);
    }
    catch (NoSuchStructureException e) {}
    return false;
  }
  
  public boolean isAllied(Creature aCreature)
  {
    try
    {
      Structure struct = Structures.getStructure(this.structure);
      return struct.isAllied(aCreature);
    }
    catch (NoSuchStructureException e) {}
    return false;
  }
  
  public boolean isSameKingdom(Creature aCreature)
  {
    try
    {
      Structure struct = Structures.getStructure(this.structure);
      return struct.isSameKingdom(aCreature);
    }
    catch (NoSuchStructureException e) {}
    return false;
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
  
  public boolean canHavePermissions()
  {
    return isLocked();
  }
  
  public boolean mayShowPermissions(Creature creature)
  {
    return (!isManaged()) && (hasLock()) && (mayManage(creature));
  }
  
  public boolean canManage(Creature creature)
  {
    if (this.wall == null) {
      return false;
    }
    Structure structure = Structures.getStructureOrNull(this.wall.getStructureId());
    if (structure == null) {
      return false;
    }
    return structure.canManage(creature);
  }
  
  public boolean mayManage(Creature creature)
  {
    if (creature.getPower() > 1) {
      return true;
    }
    return canManage(creature);
  }
  
  public boolean mayPass(Creature creature)
  {
    if (creature.getPower() > 1) {
      return true;
    }
    if (this.wall == null) {
      return true;
    }
    Structure structure = Structures.getStructureOrNull(this.wall.getStructureId());
    if (structure == null) {
      return true;
    }
    if (!isLocked()) {
      return true;
    }
    if (structure.isExcluded(creature)) {
      return false;
    }
    if (isManaged()) {
      return structure.mayPass(creature);
    }
    if (DoorSettings.isExcluded(this, creature)) {
      return false;
    }
    return DoorSettings.mayPass(this, creature);
  }
  
  public boolean mayLock(Creature creature)
  {
    if (creature.getPower() > 1) {
      return true;
    }
    if (this.wall == null) {
      return true;
    }
    Structure structure = Structures.getStructureOrNull(this.wall.getStructureId());
    if (structure == null) {
      return true;
    }
    if (structure.isExcluded(creature)) {
      return false;
    }
    if (isManaged()) {
      return structure.mayModify(creature);
    }
    return false;
  }
  
  public boolean hasLock()
  {
    try
    {
      getLock();
    }
    catch (NoSuchLockException e)
    {
      this.lock = -10L;
    }
    return this.lock != -10L;
  }
  
  public String getTypeName()
  {
    if (this.wall == null) {
      return "No Wall!";
    }
    return this.wall.getTypeName();
  }
  
  public String mayManageText(Player aPlayer)
  {
    return "Controlled By Building";
  }
  
  public String mayManageHover(Player aPlayer)
  {
    return "If ticked, then building controls entry.";
  }
  
  public String messageOnTick()
  {
    return "This will allow the building to Control this door.";
  }
  
  public String questionOnTick()
  {
    return "Are you sure?";
  }
  
  public String messageUnTick()
  {
    return "This will allow the door to be independant of the building 'May Enter' setting.";
  }
  
  public String questionUnTick()
  {
    return "Are you sure?";
  }
  
  public String getSettlementName()
  {
    try
    {
      Structure struct = Structures.getStructure(this.structure);
      return struct.getSettlementName();
    }
    catch (NoSuchStructureException e) {}
    return "";
  }
  
  public String getAllianceName()
  {
    try
    {
      Structure struct = Structures.getStructure(this.structure);
      return struct.getAllianceName();
    }
    catch (NoSuchStructureException e) {}
    return "";
  }
  
  public String getKingdomName()
  {
    try
    {
      Structure struct = Structures.getStructure(this.structure);
      return struct.getKingdomName();
    }
    catch (NoSuchStructureException e) {}
    return "";
  }
  
  public boolean canAllowEveryone()
  {
    return false;
  }
  
  public String getRolePermissionName()
  {
    return "";
  }
  
  public boolean setNewOwner(long playerId)
  {
    return false;
  }
  
  public String getOwnerName()
  {
    return "";
  }
  
  public boolean isNotLockable()
  {
    return this.wall.isNotLockable();
  }
  
  public boolean isNotLockpickable()
  {
    return this.wall.isNotLockpickable();
  }
  
  public byte getLayer()
  {
    return this.wall.getLayer();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\structures\Door.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */