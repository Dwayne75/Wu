package com.wurmonline.server.zones;

import com.wurmonline.math.TilePos;
import com.wurmonline.mesh.MeshIO;
import com.wurmonline.mesh.Tiles;
import com.wurmonline.mesh.Tiles.Tile;
import com.wurmonline.server.Constants;
import com.wurmonline.server.Features.Feature;
import com.wurmonline.server.Items;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Players;
import com.wurmonline.server.Server;
import com.wurmonline.server.ServerEntry;
import com.wurmonline.server.Servers;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.CreatureTemplate;
import com.wurmonline.server.creatures.CreatureTemplateFactory;
import com.wurmonline.server.creatures.CreatureTemplateIds;
import com.wurmonline.server.creatures.Creatures;
import com.wurmonline.server.creatures.MineDoorPermission;
import com.wurmonline.server.creatures.NoSuchCreatureException;
import com.wurmonline.server.creatures.VisionArea;
import com.wurmonline.server.deities.Deities;
import com.wurmonline.server.deities.Deity;
import com.wurmonline.server.effects.Effect;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemFactory;
import com.wurmonline.server.items.ItemTypes;
import com.wurmonline.server.kingdom.Kingdom;
import com.wurmonline.server.kingdom.Kingdoms;
import com.wurmonline.server.sounds.SoundPlayer;
import com.wurmonline.server.structures.Door;
import com.wurmonline.server.structures.Fence;
import com.wurmonline.server.structures.FenceGate;
import com.wurmonline.server.structures.Structure;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.villages.Villages;
import com.wurmonline.server.weather.Weather;
import com.wurmonline.shared.constants.CounterTypes;
import com.wurmonline.shared.constants.CreatureTypes;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;

public abstract class Zone
  implements CounterTypes, MiscConstants, ItemTypes, CreatureTemplateIds, CreatureTypes
{
  public static final String cvsversion = "$Id: Zone.java,v 1.55 2007-04-09 13:40:23 root Exp $";
  private static int ids = 0;
  private static final Logger logger = Logger.getLogger(Zone.class.getName());
  private Set<Village> villages;
  final int startX;
  final int endX;
  final int startY;
  final int endY;
  private final ConcurrentHashMap<Integer, VolaTile> tiles;
  private final ArrayList<VolaTile> deletionQueue;
  Set<VirtualZone> zoneWatchers;
  Set<Structure> structures;
  private Tracks tracks;
  int id;
  boolean isLoaded = true;
  final boolean isOnSurface;
  boolean loading = false;
  private final int size;
  private int creatures = 0;
  private int kingdomCreatures = 0;
  int highest = 0;
  private boolean allWater = false;
  private boolean allLand = false;
  boolean isForest = false;
  Den den = null;
  Item creatureSpawn = null;
  Item treasureChest = null;
  static int spawnPoints = 0;
  static int treasureChests = 0;
  private static final Random r = new Random();
  private static final long SPRING_PRIME = 7919L;
  private boolean hasRift = false;
  private final int spawnSeed;
  private static final VolaTile[] emptyTiles = new VolaTile[0];
  private static final VirtualZone[] emptyWatchers = new VirtualZone[0];
  private static final Structure[] emptyStructures = new Structure[0];
  private short pollTicker = 0;
  public static final int secondsBetweenPolls = 800;
  static final int zonesPolled = Math.max(2, Zones.numberOfZones * 2 / 800);
  public static final int maxZonesPolled = Zones.numberOfZones * 2 / zonesPolled;
  private static final long LOG_ELAPSED_TIME_THRESHOLD = Constants.lagThreshold;
  private static final int breedingLimit = Servers.localServer.maxCreatures / 25;
  private static final LinkedList<Long> fogSpiders = new LinkedList();
  
  Zone(int aStartX, int aEndX, int aStartY, int aEndY, boolean aIsOnSurface)
  {
    this.id = (ids++);
    this.pollTicker = ((short)(this.id / zonesPolled));
    this.startX = Zones.safeTileX(aStartX);
    this.startY = Zones.safeTileY(aStartY);
    
    this.endX = Zones.safeTileX(aEndX);
    this.endY = Zones.safeTileY(aEndY);
    
    this.size = (aEndX - aStartX + 1);
    this.isOnSurface = aIsOnSurface;
    
    this.tiles = new ConcurrentHashMap();
    this.deletionQueue = new ArrayList();
    setTypes();
    this.spawnSeed = (Zones.worldTileSizeX / 200);
  }
  
  final Item[] getAllItems()
  {
    if (this.tiles != null)
    {
      Set<Item> items = new HashSet();
      for (VolaTile tile : this.tiles.values())
      {
        Item[] its = tile.getItems();
        for (Item lIt : its) {
          items.add(lIt);
        }
      }
      return (Item[])items.toArray(new Item[items.size()]);
    }
    return new Item[0];
  }
  
  public final Creature[] getAllCreatures()
  {
    if (this.tiles != null)
    {
      Set<Creature> crets = new HashSet();
      for (VolaTile tile : this.tiles.values())
      {
        Creature[] its = tile.getCreatures();
        for (Creature lIt : its) {
          crets.add(lIt);
        }
      }
      return (Creature[])crets.toArray(new Creature[crets.size()]);
    }
    return new Creature[0];
  }
  
  private void setTypes()
  {
    if (this.isOnSurface)
    {
      int forest = 0;
      MeshIO mesh = Server.surfaceMesh;
      for (int x = this.startX; x <= this.endX; x++) {
        for (int y = this.startY; y < this.endY; y++)
        {
          int tile = mesh.getTile(x, y);
          int h = Tiles.decodeHeight(tile);
          if (h > this.highest)
          {
            this.highest = h;
            this.allWater = false;
          }
          else if (h < 0)
          {
            this.allLand = false;
          }
          if (Tiles.isTree(Tiles.decodeType(tile))) {
            forest++;
          }
        }
      }
      if (forest > this.size * this.size / 6)
      {
        if (logger.isLoggable(Level.FINEST)) {
          logger.finest("Zone at " + this.startX + "," + this.startY + "-" + this.endX + "," + this.endY + " is forest.");
        }
        this.isForest = true;
      }
    }
  }
  
  public final int getSize()
  {
    return this.size;
  }
  
  public final boolean isOnSurface()
  {
    return this.isOnSurface;
  }
  
  public final void addVillage(Village village)
  {
    if (this.villages == null) {
      this.villages = new HashSet();
    }
    if (!this.villages.contains(village))
    {
      this.villages.add(village);
      if (this.tiles != null) {
        for (VolaTile tile : this.tiles.values()) {
          if (village.covers(tile.getTileX(), tile.getTileY())) {
            tile.setVillage(village);
          }
        }
      }
      addMineDoors(village);
    }
  }
  
  public final void removeVillage(Village village)
  {
    if (this.villages == null) {
      this.villages = new HashSet();
    }
    if (this.villages.contains(village))
    {
      this.villages.remove(village);
      if (this.tiles != null) {
        for (VolaTile tile : this.tiles.values()) {
          if (village.covers(tile.getTileX(), tile.getTileY())) {
            tile.setVillage(null);
          }
        }
      }
      for (int x = this.startX; x < this.endX; x++) {
        for (int y = this.startY; y < this.endY; y++)
        {
          MineDoorPermission md = MineDoorPermission.getPermission(x, y);
          if (md != null) {
            if (village.covers(x, y)) {
              village.removeMineDoor(md);
            }
          }
        }
      }
    }
  }
  
  public final void updateVillage(Village village, boolean shouldStay)
  {
    if (this.villages == null) {
      this.villages = new HashSet();
    }
    if (this.villages.contains(village))
    {
      if (!shouldStay) {
        this.villages.remove(village);
      }
      if (this.tiles != null)
      {
        for (VolaTile tile : this.tiles.values()) {
          if ((!village.covers(tile.getTileX(), tile.getTileY())) && (tile.getVillage() == village)) {
            tile.setVillage(null);
          }
        }
        for (VolaTile tile : this.tiles.values()) {
          if (village.covers(tile.getTileX(), tile.getTileY())) {
            tile.setVillage(village);
          }
        }
      }
      for (int x = this.startX; x < this.endX; x++) {
        for (int y = this.startY; y < this.endY; y++)
        {
          MineDoorPermission md = MineDoorPermission.getPermission(x, y);
          if (md != null)
          {
            if ((!village.covers(x, y)) && (md.getVillage() == village)) {
              village.removeMineDoor(md);
            }
            if (village.covers(x, y)) {
              village.addMineDoor(md);
            }
          }
        }
      }
      addMineDoors(village);
    }
    else if (shouldStay)
    {
      addVillage(village);
    }
  }
  
  final boolean containsVillage(int x, int y)
  {
    if (this.villages != null) {
      for (Village village : this.villages) {
        if (village.covers(x, y)) {
          return true;
        }
      }
    }
    return false;
  }
  
  final Village getVillage(int x, int y)
  {
    if (this.villages != null) {
      for (Village village : this.villages) {
        if (village.covers(x, y)) {
          return village;
        }
      }
    }
    return null;
  }
  
  public final Village[] getVillages()
  {
    if (this.villages != null) {
      return (Village[])this.villages.toArray(new Village[this.villages.size()]);
    }
    return new Village[0];
  }
  
  public final void poll(int nums)
  {
    this.pollTicker = ((short)(this.pollTicker + 1));
    boolean lPollStuff = this.pollTicker >= maxZonesPolled;
    
    boolean spawnCreatures = (lPollStuff) || (Creatures.getInstance().getNumberOfCreatures() < Servers.localServer.maxCreatures - 1000);
    
    boolean checkAreaEffect = Server.rand.nextInt(5) == 0;
    long now = System.nanoTime();
    for (VolaTile lElement : this.tiles.values()) {
      lElement.poll(lPollStuff, this.pollTicker, checkAreaEffect);
    }
    for (VolaTile toDelete : this.deletionQueue) {
      this.tiles.remove(Integer.valueOf(toDelete.hashCode()));
    }
    this.deletionQueue.clear();
    
    float lElapsedTime = (float)(System.nanoTime() - now) / 1000000.0F;
    if ((logger.isLoggable(Level.FINE)) && (lElapsedTime > 200.0F)) {
      logger.fine("Zone at " + this.startX + ", " + this.startY + " polled " + this.tiles.size() + " tiles. That took " + lElapsedTime + " millis.");
    } else if ((!Servers.localServer.testServer) && (lElapsedTime > 300.0F)) {
      logger.log(Level.INFO, "Zone at " + this.startX + ", " + this.startY + " polled " + this.tiles.size() + " tiles. That took " + lElapsedTime + " millis.");
    }
    VolaTile t;
    Creature cret;
    if (isOnSurface()) {
      if ((Server.getWeather().getFog() > 0.5F) && (fogSpiders.size() < Zones.worldTileSizeX / 10))
      {
        try
        {
          TilePos tp = TilePos.fromXY(getStartX() + Server.rand.nextInt(this.size), getStartY() + Server.rand.nextInt(this.size));
          if (Tiles.decodeHeight(Server.surfaceMesh.getTile(tp)) > 0)
          {
            t = Zones.getTileOrNull(tp, true);
            if (((t == null) || ((t.getStructure() == null) && (t.getVillage() == null))) && 
              (Villages.getVillage(tp, true) == null))
            {
              CreatureTemplate ctemplate = CreatureTemplateFactory.getInstance().getTemplate(105);
              cret = Creature.doNew(105, (tp.x << 2) + 2.0F, (tp.y << 2) + 2.0F, Server.rand
                .nextInt(360), 0, "", ctemplate.getSex(), (byte)0);
              
              fogSpiders.add(Long.valueOf(cret.getWurmId()));
              if (fogSpiders.size() % 100 == 0) {
                logger.log(Level.INFO, "Now " + fogSpiders.size() + " fog spiders.");
              }
            }
          }
        }
        catch (Exception ex)
        {
          logger.log(Level.WARNING, ex.getMessage(), ex);
        }
      }
      else if ((Server.getWeather().getFog() <= 0.0F) && (fogSpiders.size() > 0))
      {
        long toDestroy = ((Long)fogSpiders.removeFirst()).longValue();
        try
        {
          Creature spider = Creatures.getInstance().getCreature(toDestroy);
          spider.destroy();
          if (fogSpiders.size() % 100 == 0) {
            logger.log(Level.INFO, "Now " + fogSpiders.size() + " fog spiders.");
          }
        }
        catch (Exception localException1) {}
      }
    }
    if (lPollStuff)
    {
      if (logger.isLoggable(Level.FINEST)) {
        logger.finest(this.id + " polling. Ticker=" + this.pollTicker + " max=" + maxZonesPolled);
      }
      this.pollTicker = 0;
      if (this.tracks != null) {
        this.tracks.decay();
      }
      FaithZone lElement;
      if (Features.Feature.NEWDOMAINS.isEnabled())
      {
        FaithZone[] lFaithZonesCovered = Zones.getFaithZonesCoveredBy(this.startX, this.startY, this.endX, this.endY, this.isOnSurface);
        t = lFaithZonesCovered;localException1 = t.length;
        for (cret = 0; cret < localException1; cret++)
        {
          lElement = t[cret];
          lElement.pollMycelium();
        }
      }
      else
      {
        FaithZone[] lFaithZonesCovered = Zones.getFaithZonesCoveredBy(this.startX, this.startY, this.endX, this.endY, this.isOnSurface);
        
        Deity old = null;
        localException1 = lFaithZonesCovered;cret = localException1.length;
        for (lElement = 0; lElement < cret; lElement++)
        {
          FaithZone lElement = localException1[lElement];
          
          old = lElement.getCurrentRuler();
          if (lElement.poll()) {
            for (int x = lElement.getStartX(); x < lElement.getEndX(); x++) {
              for (int y = lElement.getStartY(); y < lElement.getEndY(); y++)
              {
                VolaTile tile = getTileOrNull(x, y);
                if (tile != null) {
                  if (old == null) {
                    tile.broadCast("The domain of " + lElement
                      .getCurrentRuler().getName() + " now has reached this place.");
                  } else if (lElement.getCurrentRuler() != null) {
                    tile.broadCast(lElement
                      .getCurrentRuler().getName() + "'s domain now is the strongest here!");
                  } else {
                    tile.broadCast(old
                      .getName() + " has had to lose " + old.getHisHerItsString() + " hold over this area!");
                  }
                }
              }
            }
          }
        }
      }
    }
    if ((spawnCreatures) && (!isHasRift()))
    {
      boolean lSpawnKingdom = false;
      if ((this.kingdomCreatures <= 0) && (isOnSurface())) {
        if (!Servers.localServer.PVPSERVER) {
          lSpawnKingdom = Server.rand.nextInt(50) == 0;
        } else {
          lSpawnKingdom = Server.rand.nextInt(20) == 0;
        }
      }
      boolean spawnSeaHunter = false;
      
      boolean spawnSeaCreature = false;
      if (this.isOnSurface) {
        if ((this.creatures < 20) && (Servers.localServer.maxCreatures > 100))
        {
          if ((!lSpawnKingdom) && (Creatures.getInstance().getNumberOfSeaHunters() < 500)) {
            spawnSeaHunter = true;
          }
          if ((!spawnSeaHunter) && (!lSpawnKingdom)) {
            spawnSeaCreature = true;
          }
        }
      }
      if (logger.isLoggable(Level.FINEST)) {
        logger.finest(this.id + " " + ((this.den != null) && (this.creatures < 20)) + " || (" + 
          Creatures.getInstance().getNumberOfCreatures() + "<" + Servers.localServer.maxCreatures + " && (" + (this.creatures < 5) + " || " + lSpawnKingdom + "))");
      }
      boolean doSpawn = (this.den != null) || (this.creatureSpawn != null);
      if (doSpawn) {
        doSpawn = (this.creatures < 60) && (Creatures.getInstance().getNumberOfTyped() < Servers.localServer.maxTypedCreatures);
      }
      if (!doSpawn)
      {
        if ((this.creatures < 40) || (lSpawnKingdom)) {
          doSpawn = true;
        }
        if (Creatures.getInstance().getNumberOfCreatures() > Servers.localServer.maxCreatures + Servers.localServer.maxTypedCreatures)
        {
          lSpawnKingdom = false;
          doSpawn = false;
        }
      }
      boolean createDen = false;
      if (spawnPoints < Servers.localServer.maxTypedCreatures / 40) {
        if ((this.den == null) && (this.creatureSpawn == null)) {
          if (Server.rand.nextInt(10) == 0) {
            createDen = true;
          }
        }
      }
      if ((doSpawn) || (createDen))
      {
        int lSeed = Server.rand.nextInt(this.spawnSeed);
        if (lSeed == 0)
        {
          int sx = Server.rand.nextInt(this.endX - this.startX);
          int sy = Server.rand.nextInt(this.endY - this.startY);
          int tx = this.startX + sx;
          int ty = this.startY + sy;
          for (int xa = -10; xa < 10; xa++) {
            for (int ya = -10; ya < 10; ya++)
            {
              VolaTile t = Zones.getTileOrNull(tx + xa, ty + ya, this.isOnSurface);
              if (t != null)
              {
                Creature[] crets = t.getCreatures();
                for (Creature lCret : crets) {
                  if (lCret.isPlayer()) {
                    return;
                  }
                }
              }
            }
          }
          VolaTile t = getTileOrNull(tx, ty);
          if (t != null)
          {
            if (lSpawnKingdom) {
              if ((t.getWalls().length == 0) && (t.getFences().length == 0) && (t.getStructure() == null) && 
                (t.getCreatures().length == 0)) {
                spawnCreature(tx, ty, lSpawnKingdom);
              }
            }
          }
          else
          {
            Village v = Villages.getVillage(tx, ty, this.isOnSurface);
            if (v == null) {
              if (createDen)
              {
                createDen(tx, ty);
              }
              else
              {
                spawnCreature(tx, ty, lSpawnKingdom);
                if (Server.rand.nextInt(300) == 0) {
                  createTreasureChest(tx, ty);
                }
              }
            }
          }
        }
      }
      else if ((spawnSeaCreature) || (spawnSeaHunter))
      {
        spawnSeaCreature(spawnSeaHunter);
      }
    }
  }
  
  private final int getRandomSeaCreatureId()
  {
    if ((Creatures.getInstance().getNumberOfSeaMonsters() < 4) && (Server.rand.nextInt(86400) == 0)) {
      return 70;
    }
    Map<Integer, Integer> slotsForCreature = new HashMap();
    slotsForCreature.put(Integer.valueOf(100), 
      Integer.valueOf(Creatures.getInstance().getOpenSpawnSlotsForCreatureType(100)));
    slotsForCreature.put(Integer.valueOf(97), 
      Integer.valueOf(Creatures.getInstance().getOpenSpawnSlotsForCreatureType(97)));
    slotsForCreature.put(Integer.valueOf(99), 
      Integer.valueOf(Creatures.getInstance().getOpenSpawnSlotsForCreatureType(99)));
    
    Integer[] crets = new Integer[slotsForCreature.keySet().size()];
    slotsForCreature.keySet().toArray(crets);
    for (int i = crets.length - 1; i >= 0; i--)
    {
      Integer key = crets[i];
      if (((Integer)slotsForCreature.get(key)).intValue() == 0) {
        slotsForCreature.remove(key);
      }
    }
    int validCount = slotsForCreature.keySet().size();
    if (validCount == 0) {
      return 0;
    }
    int val = Server.rand.nextInt(validCount);
    if (crets.length != slotsForCreature.keySet().size())
    {
      crets = new Integer[slotsForCreature.keySet().size()];
      slotsForCreature.keySet().toArray(crets);
    }
    return crets[val].intValue();
  }
  
  private final void spawnSeaCreature(boolean spawnSeaHunter)
  {
    int template = spawnSeaHunter ? 71 : getRandomSeaCreatureId();
    if (template == 0) {
      return;
    }
    int sx = Server.rand.nextInt(this.endX - this.startX);
    int sy = Server.rand.nextInt(this.endY - this.startY);
    int tx = this.startX + sx;
    int ty = this.startY + sy;
    for (int xa = -10; xa < 10; xa++) {
      for (int ya = -10; ya < 10; ya++)
      {
        VolaTile t = Zones.getTileOrNull(tx + xa, ty + ya, this.isOnSurface);
        if (t != null)
        {
          Creature[] crets = t.getCreatures();
          for (Creature lCret : crets) {
            if (lCret.isPlayer()) {
              return;
            }
          }
        }
      }
    }
    short[] tsteep = Creature.getTileSteepness(tx, ty, true);
    if (tsteep[0] > 65336) {
      return;
    }
    try
    {
      CreatureTemplate ctemplate = CreatureTemplateFactory.getInstance().getTemplate(template);
      if (!spawnSeaHunter) {
        if (!maySpawnCreatureTemplate(ctemplate, false, false)) {
          return;
        }
      }
      byte sex = ctemplate.getSex();
      if ((sex == 0) && (!ctemplate.keepSex)) {
        if (Server.rand.nextInt(2) == 0) {
          sex = 1;
        }
      }
      Creature.doNew(template, (tx << 2) + 2.0F, (ty << 2) + 2.0F, Server.rand
        .nextInt(360), 0, "", sex);
    }
    catch (Exception ex)
    {
      logger.log(Level.WARNING, ex.getMessage(), ex);
    }
  }
  
  private final void createTreasureChest(int tx, int ty)
  {
    if (Features.Feature.TREASURE_CHESTS.isEnabled())
    {
      if (treasureChests > Zones.worldTileSizeX / 70) {
        return;
      }
      if ((!this.allWater) && ((this.isForest == true) || (Server.rand.nextInt(5) == 0)))
      {
        int tile = Server.caveMesh.getTile(tx, ty);
        if (this.isOnSurface) {
          tile = Server.surfaceMesh.getTile(tx, ty);
        }
        if (Tiles.decodeHeight(tile) > 0)
        {
          boolean ok = !Tiles.isSolidCave(Tiles.decodeType(tile));
          if (this.isOnSurface)
          {
            ok = false;
            if ((!Tiles.isMineDoor(Tiles.decodeType(tile))) && (Tiles.decodeType(tile) != Tiles.Tile.TILE_HOLE.id)) {
              ok = true;
            }
          }
          if (ok)
          {
            short[] tsteep = Creature.getTileSteepness(tx, ty, true);
            if (tsteep[1] >= 20) {
              return;
            }
            try
            {
              Item i = ItemFactory.createItem(995, 50 + Server.rand.nextInt(30), (tx << 2) + 2.0F, (ty << 2) + 2.0F, Server.rand
                .nextFloat() * 360.0F, this.isOnSurface, (byte)1, -10L, null);
              
              i.setAuxData((byte)Server.rand.nextInt(8));
              if (i.getAuxData() > 4) {
                i.setRarity((byte)2);
              }
              if (Server.rand.nextBoolean())
              {
                Item lock = ItemFactory.createItem(194, 30.0F + Server.rand.nextFloat() * 70.0F, null);
                i.setLockId(lock.getWurmId());
                int tilex = i.getTileX();
                int tiley = i.getTileY();
                SoundPlayer.playSound("sound.object.lockunlock", tilex, tiley, this.isOnSurface, 1.0F);
                i.setLocked(true);
              }
              i.fillTreasureChest();
            }
            catch (Exception fe)
            {
              logger.log(Level.WARNING, "Failed to create treasure chest: " + fe.getMessage(), fe);
            }
          }
        }
      }
    }
  }
  
  private final void createDen(int tx, int ty)
  {
    byte type = (byte)Math.max(0, Server.rand.nextInt(22) - 10);
    
    CreatureTemplate[] ctemps = CreatureTemplateFactory.getInstance().getTemplates();
    CreatureTemplate selected = ctemps[Server.rand.nextInt(ctemps.length)];
    if ((selected.hasDen()) && ((selected.isSubterranean()) || (this.isOnSurface)))
    {
      int tile = Server.caveMesh.getTile(tx, ty);
      if (this.isOnSurface) {
        tile = Server.surfaceMesh.getTile(tx, ty);
      }
      if (Tiles.decodeHeight(tile) > 0)
      {
        boolean ok = !Tiles.isSolidCave(Tiles.decodeType(tile));
        if (this.isOnSurface)
        {
          ok = false;
          if ((!Tiles.isMineDoor(Tiles.decodeType(tile))) && (Tiles.decodeType(tile) != Tiles.Tile.TILE_HOLE.id)) {
            ok = true;
          }
        }
        if (ok)
        {
          short[] tsteep = Creature.getTileSteepness(tx, ty, true);
          if (tsteep[1] >= 20) {
            return;
          }
          if (tsteep[0] > 3000) {
            return;
          }
          try
          {
            Item i = ItemFactory.createItem(521, 50 + Server.rand.nextInt(30), (tx << 2) + 2.0F, (ty << 2) + 2.0F, Server.rand
              .nextFloat() * 360.0F, this.isOnSurface, (byte)0, -10L, null);
            
            i.setAuxData(type);
            i.setData1(selected.getTemplateId());
            i.setName(selected.getDenName());
          }
          catch (Exception fe)
          {
            logger.log(Level.WARNING, "Failed to create den: " + fe.getMessage(), fe);
          }
        }
      }
    }
  }
  
  private final void spawnCreature(int tx, int ty, boolean _spawnKingdom)
  {
    int tile = 0;
    if (this.isOnSurface)
    {
      tile = Server.surfaceMesh.getTile(tx, ty);
      if ((Tiles.isMineDoor(Tiles.decodeType(tile))) || (Tiles.decodeType(tile) == Tiles.Tile.TILE_HOLE.id)) {
        return;
      }
      byte kingdom = Zones.getKingdom(tx, ty);
      byte kingdomTemplate = kingdom;
      if (kingdom == 0) {
        kingdom = Zones.getKingdom(tx + 50, ty + 50);
      }
      if (kingdom == 0) {
        kingdom = Zones.getKingdom(tx + 50, ty - 50);
      }
      if (kingdom == 0) {
        kingdom = Zones.getKingdom(tx - 50, ty + 50);
      }
      if (kingdom == 0) {
        kingdom = Zones.getKingdom(tx - 50, ty - 50);
      }
      if (kingdom == 0) {
        kingdom = Zones.getKingdom(tx + 50, ty);
      }
      if (kingdom == 0) {
        kingdom = Zones.getKingdom(tx - 50, ty);
      }
      if (kingdom == 0) {
        kingdom = Zones.getKingdom(tx, ty + 50);
      }
      if (kingdom == 0) {
        kingdom = Zones.getKingdom(tx, ty - 50);
      }
      if (kingdom == 0)
      {
        _spawnKingdom = false;
      }
      else
      {
        Kingdom k = Kingdoms.getKingdom(kingdom);
        if (k != null) {
          kingdomTemplate = k.getTemplate();
        }
      }
      float height = Tiles.decodeHeightAsFloat(tile);
      if (height > 0.0F)
      {
        if (_spawnKingdom)
        {
          short[] tsteep = Creature.getTileSteepness(tx, ty, this.isOnSurface);
          if (tsteep[1] >= 40) {
            return;
          }
          byte deity = 1;
          int creatureTemplate = 37;
          if (kingdomTemplate == 3)
          {
            deity = 4;
            creatureTemplate = 40;
          }
          else if (kingdomTemplate == 2)
          {
            creatureTemplate = 39;
            deity = 2;
          }
          else if (height < 1.0F)
          {
            creatureTemplate = 38;
            deity = 3;
          }
          try
          {
            CreatureTemplate ctemplate = CreatureTemplateFactory.getInstance().getTemplate(creatureTemplate);
            if (!maySpawnCreatureTemplate(ctemplate, false, _spawnKingdom)) {
              return;
            }
            Creature cret = Creature.doNew(creatureTemplate, (tx << 2) + 2.0F, (ty << 2) + 2.0F, Server.rand
              .nextInt(360), this.isOnSurface ? 0 : -1, "", ctemplate.getSex(), kingdom);
            
            cret.setDeity(Deities.getDeity(deity));
          }
          catch (Exception ex)
          {
            logger.log(Level.WARNING, ex.getMessage(), ex);
          }
          return;
        }
        if (this.den != null) {
          try
          {
            CreatureTemplate ctemplate = CreatureTemplateFactory.getInstance().getTemplate(this.den
              .getTemplateId());
            if (!maySpawnCreatureTemplate(ctemplate, true, false)) {
              return;
            }
            short[] tsteep = Creature.getTileSteepness(tx, ty, this.isOnSurface);
            if (tsteep[1] >= 40) {
              return;
            }
            byte sex = ctemplate.getSex();
            if ((sex == 0) && (!ctemplate.keepSex)) {
              if (Server.rand.nextInt(2) == 0) {
                sex = 1;
              }
            }
            Creature.doNew(this.den.getTemplateId(), (tx << 2) + 2.0F, (ty << 2) + 2.0F, Server.rand
              .nextInt(360), this.isOnSurface ? 0 : -1, "", sex);
          }
          catch (Exception ex)
          {
            logger.log(Level.WARNING, ex.getMessage(), ex);
          }
        }
        short[] tsteep = Creature.getTileSteepness(tx, ty, this.isOnSurface);
        if (tsteep[1] >= 40) {
          return;
        }
        if ((this.creatureSpawn != null) && (Server.rand.nextInt(10) != 0))
        {
          if (this.creatureSpawn.getData1() > 0) {
            try
            {
              CreatureTemplate ctemplate = CreatureTemplateFactory.getInstance().getTemplate(this.creatureSpawn
                .getData1());
              if (!maySpawnCreatureTemplate(ctemplate, true, false)) {
                return;
              }
              byte sex = ctemplate.getSex();
              if ((sex == 0) && (!ctemplate.keepSex)) {
                if (Server.rand.nextInt(2) == 0) {
                  sex = 1;
                }
              }
              byte ctype = this.creatureSpawn.getAuxData();
              if (Server.rand.nextInt(40) == 0) {
                ctype = 99;
              }
              Creature.doNew(ctemplate.getTemplateId(), ctype, (tx << 2) + 2.0F, (ty << 2) + 2.0F, Server.rand
                .nextInt(360), this.isOnSurface ? 0 : -1, "", sex);
              if (this.creatureSpawn.getDamage() < 99.0F) {
                this.creatureSpawn.setDamage(this.creatureSpawn.getDamage() + Server.rand.nextFloat() * 0.5F);
              } else {
                Items.destroyItem(this.creatureSpawn.getWurmId());
              }
            }
            catch (Exception ex)
            {
              logger.log(Level.WARNING, ex.getMessage(), ex);
            }
          }
        }
        else
        {
          byte type = Tiles.decodeType(tile);
          byte elev = 0;
          if (type == Tiles.Tile.TILE_LAVA.id)
          {
            if ((Tiles.decodeData(tile) & 0xFF) != 255) {
              elev = -1;
            }
          }
          else {
            for (int x = 0; x <= 1; x++) {
              for (int y = 0; y <= 1; y++)
              {
                int ntile = Server.surfaceMesh.getTile(tx + x, ty + y);
                byte ntype = Tiles.decodeType(ntile);
                if (Tiles.getTile(ntype).isNormalTree())
                {
                  type = Tiles.Tile.TILE_TREE.id;
                  break;
                }
                if (ntype == Tiles.Tile.TILE_LAVA.id)
                {
                  if ((Tiles.decodeData(ntile) & 0xFF) != 255) {
                    elev = -1;
                  }
                  type = Tiles.Tile.TILE_LAVA.id;
                  break;
                }
                if (Tiles.decodeHeight(ntile) < 0)
                {
                  if (ntype == Tiles.Tile.TILE_ROCK.id)
                  {
                    elev = 1;
                    type = Tiles.Tile.TILE_ROCK.id; break;
                  }
                  if (ntype != Tiles.Tile.TILE_SAND.id) {
                    break;
                  }
                  elev = 5;
                  type = Tiles.Tile.TILE_SAND.id; break;
                }
              }
            }
          }
          Encounter enc = SpawnTable.getRandomEncounter(type, elev);
          spawnEncounter(tx, ty, enc);
        }
      }
      else
      {
        boolean spawnSeaHunter = false;
        if (Creatures.getInstance().getNumberOfSeaHunters() < 500)
        {
          spawnSeaHunter = true;
          spawnSeaCreature(true);
        }
        if (!spawnSeaHunter) {
          spawnSeaCreature(false);
        }
      }
    }
    else
    {
      if (this.creatureSpawn != null)
      {
        tx = this.creatureSpawn.getTileX();
        ty = this.creatureSpawn.getTileY();
      }
      tile = Server.caveMesh.getTile(tx, ty);
      byte type = Tiles.decodeType(tile);
      if (!Tiles.isSolidCave(type))
      {
        if (this.creatureSpawn != null) {
          try
          {
            CreatureTemplate ctemplate = CreatureTemplateFactory.getInstance().getTemplate(this.creatureSpawn
              .getData1());
            if (!maySpawnCreatureTemplate(ctemplate, true, false)) {
              return;
            }
            byte sex = ctemplate.getSex();
            if ((sex == 0) && (!ctemplate.keepSex)) {
              if (Server.rand.nextInt(2) == 0) {
                sex = 1;
              }
            }
            byte ctype = this.creatureSpawn.getAuxData();
            if (Server.rand.nextInt(40) == 0) {
              ctype = 99;
            }
            Creature cret = Creature.doNew(ctemplate.getTemplateId(), ctype, (tx << 2) + 2.0F, (ty << 2) + 2.0F, Server.rand
              .nextInt(360), this.isOnSurface ? 0 : -1, "", sex);
            if (this.creatureSpawn.getDamage() < 99.0F) {
              this.creatureSpawn.setDamage(this.creatureSpawn.getDamage() + Server.rand.nextFloat());
            } else {
              Items.destroyItem(this.creatureSpawn.getWurmId());
            }
          }
          catch (Exception ex)
          {
            logger.log(Level.WARNING, ex.getMessage(), ex);
          }
        }
        if (Tiles.decodeHeight(tile) > 0)
        {
          Encounter enc = SpawnTable.getRandomEncounter(Tiles.Tile.TILE_CAVE.id, (byte)-1);
          spawnEncounter(tx, ty, enc);
        }
      }
    }
  }
  
  public static final boolean maySpawnCreatureTemplate(CreatureTemplate ctemplate, boolean typed, boolean kingdomCreature)
  {
    return maySpawnCreatureTemplate(ctemplate, typed, false, kingdomCreature);
  }
  
  public static final boolean maySpawnCreatureTemplate(CreatureTemplate ctemplate, boolean typed, boolean breeding, boolean kingdomCreature)
  {
    if ((ctemplate.isAggHuman()) || (ctemplate.isMonster())) {
      if (Creatures.getInstance().getNumberOfAgg() / Creatures.getInstance().getNumberOfCreatures() > Servers.localServer.percentAggCreatures / 100.0F) {
        return false;
      }
    }
    if (typed) {
      if ((Creatures.getInstance().getNumberOfTyped() < Servers.localServer.maxTypedCreatures ? 1 : 0) == 0) {
        return false;
      }
    }
    if (kingdomCreature) {
      return Creatures.getInstance().getNumberOfKingdomCreatures() < Servers.localServer.maxCreatures / (Servers.localServer.PVPSERVER ? 50 : 200);
    }
    if (Creatures.getInstance().getNumberOfNice() > Servers.localServer.maxCreatures / 2 - (breeding ? breedingLimit : 0)) {
      return false;
    }
    int nums = Creatures.getInstance().getCreatureByType(ctemplate.getTemplateId());
    if ((nums > Servers.localServer.maxCreatures * ctemplate.getMaxPercentOfCreatures()) || (
      (ctemplate.usesMaxPopulation()) && (nums >= ctemplate.getMaxPopulationOfCreatures()))) {
      return false;
    }
    return true;
  }
  
  public static final boolean hasSpring(int tilex, int tiley)
  {
    r.setSeed((tilex + tiley * Zones.worldTileSizeY) * 7919L);
    return r.nextInt(128) == 0;
  }
  
  private void spawnEncounter(int tx, int ty, Encounter enc)
  {
    Map<Integer, Integer> encTypes;
    if (enc != null)
    {
      encTypes = enc.getTypes();
      if (encTypes != null) {
        for (Integer templateId : encTypes.keySet())
        {
          boolean create = true;
          
          Integer nums = (Integer)encTypes.get(templateId);
          int n = nums.intValue();
          if (n > 1) {
            n = Math.max(1, Server.rand.nextInt(n));
          }
          try
          {
            CreatureTemplate ctemplate = CreatureTemplateFactory.getInstance().getTemplate(templateId
              .intValue());
            if ((!ctemplate.nonNewbie) || (!Constants.isNewbieFriendly))
            {
              if (!maySpawnCreatureTemplate(ctemplate, false, false)) {
                return;
              }
              for (int x = 0; x < n; x++)
              {
                byte sex = ctemplate.getSex();
                if ((sex == 0) && (!ctemplate.keepSex)) {
                  if (Server.rand.nextInt(2) == 0) {
                    sex = 1;
                  }
                }
                int tChance = Server.rand.nextInt(5);
                byte rType;
                byte rType;
                if (ctemplate.hasDen())
                {
                  byte rType;
                  if (tChance == 1)
                  {
                    int cChance = Server.rand.nextInt(20);
                    byte rType;
                    if (cChance == 1) {
                      rType = 99;
                    } else {
                      rType = (byte)Server.rand.nextInt(11);
                    }
                  }
                  else
                  {
                    rType = 0;
                  }
                }
                else
                {
                  rType = 0;
                }
                Creature cret = Creature.doNew(templateId.intValue(), rType, (tx << 2) + (1.0F + Server.rand
                  .nextFloat() * 2.0F), (ty << 2) + (1.0F + Server.rand.nextFloat() * 2.0F), Server.rand
                  .nextInt(360), this.isOnSurface ? 0 : -1, "", sex);
                if (Servers.isThisATestServer()) {
                  if (ctemplate.hasDen()) {
                    Players.getInstance().sendGmMessage(null, "System", "Debug: " + cret
                      .getNameWithGenus() + " was spawned @ " + tx + ", " + ty + ", type chance roll was " + tChance + ".", false);
                  }
                }
              }
            }
          }
          catch (Exception ex)
          {
            logger.log(Level.WARNING, ex.getMessage(), ex);
          }
        }
      }
    }
  }
  
  private VolaTile[] getTiles()
  {
    if (this.tiles != null) {
      return (VolaTile[])this.tiles.values().toArray(new VolaTile[this.tiles.size()]);
    }
    return emptyTiles;
  }
  
  private VirtualZone[] getWatchers()
  {
    if (this.zoneWatchers != null) {
      return (VirtualZone[])this.zoneWatchers.toArray(new VirtualZone[this.zoneWatchers.size()]);
    }
    return emptyWatchers;
  }
  
  public List<Creature> getPlayerWatchers()
  {
    List<Creature> playerWatchers = new ArrayList();
    if (this.zoneWatchers != null) {
      for (VirtualZone vz : this.zoneWatchers)
      {
        Creature cWatcher = vz.getWatcher();
        if ((cWatcher.isPlayer()) && (!playerWatchers.contains(cWatcher))) {
          playerWatchers.add(vz.getWatcher());
        }
      }
    }
    return playerWatchers;
  }
  
  public final int getId()
  {
    return this.id;
  }
  
  public final int getStartX()
  {
    return this.startX;
  }
  
  public final int getStartY()
  {
    return this.startY;
  }
  
  public final int getEndX()
  {
    return this.endX;
  }
  
  public final int getEndY()
  {
    return this.endY;
  }
  
  public final boolean covers(int x, int y)
  {
    return (x >= this.startX) && (x <= this.endX) && (y >= this.startY) && (y <= this.endY);
  }
  
  public final boolean isLoaded()
  {
    return this.isLoaded;
  }
  
  public final VolaTile getOrCreateTile(@Nonnull TilePos tilePos)
  {
    return getOrCreateTile(tilePos.x, tilePos.y);
  }
  
  public final VolaTile getOrCreateTile(int x, int y)
  {
    if (!covers(x, y))
    {
      logger.log(Level.WARNING, "Zone " + this.id + " at " + this.startX + ", " + this.endX + "-" + this.startY + "," + this.endY + " doesn't cover " + x + "," + y, new Exception());
      try
      {
        Zone z = Zones.getZone(x, y, isOnSurface());
        logger.log(Level.INFO, "Adding to " + z.getId());
        return z.getOrCreateTile(x, y);
      }
      catch (NoSuchZoneException nsz)
      {
        logger.log(Level.WARNING, "No such zone: " + x + ", " + y + " at ", nsz);
      }
    }
    VolaTile t = (VolaTile)this.tiles.get(Integer.valueOf(VolaTile.generateHashCode(x, y, this.isOnSurface)));
    if (t != null) {
      return t;
    }
    Set<VirtualZone> tileWatchers = new HashSet();
    Iterator localIterator;
    if (this.zoneWatchers != null) {
      for (localIterator = this.zoneWatchers.iterator(); localIterator.hasNext();)
      {
        watcher = (VirtualZone)localIterator.next();
        if (watcher.covers(x, y)) {
          tileWatchers.add(watcher);
        }
      }
    }
    VirtualZone watcher;
    VolaTile toReturn = new VolaTile(x, y, this.isOnSurface, tileWatchers, this);
    if (this.villages != null) {
      for (Village village : this.villages) {
        if (village.covers(x, y))
        {
          toReturn.setVillage(village);
          break;
        }
      }
    }
    this.tiles.put(Integer.valueOf(VolaTile.generateHashCode(x, y, this.isOnSurface)), toReturn);
    return toReturn;
  }
  
  final void removeTile(VolaTile tile)
  {
    this.deletionQueue.add(tile);
    
    tile.setInactive(true);
  }
  
  @Deprecated
  public final VolaTile getTile(int x, int y)
    throws NoSuchTileException
  {
    VolaTile t = (VolaTile)this.tiles.get(Integer.valueOf(VolaTile.generateHashCode(x, y, this.isOnSurface)));
    if (t != null) {
      return t;
    }
    throw new NoSuchTileException(x + ", " + y);
  }
  
  public final VolaTile getTileOrNull(@Nonnull TilePos tilePos)
  {
    return getTileOrNull(tilePos.x, tilePos.y);
  }
  
  public final VolaTile getTileOrNull(int x, int y)
  {
    VolaTile t = (VolaTile)this.tiles.get(Integer.valueOf(VolaTile.generateHashCode(x, y, this.isOnSurface)));
    return t;
  }
  
  public final void addEffect(Effect effect, boolean temp)
  {
    int x = effect.getTileX();
    int y = effect.getTileY();
    VolaTile tile = getOrCreateTile(x, y);
    tile.addEffect(effect, temp);
  }
  
  public final void removeEffect(Effect effect)
  {
    int x = effect.getTileX();
    int y = effect.getTileY();
    VolaTile tile = getTileOrNull(x, y);
    if (tile != null)
    {
      if (!tile.removeEffect(effect)) {
        for (VolaTile t : this.tiles.values()) {
          if (t.removeEffect(effect))
          {
            logger.log(Level.WARNING, "Aimed to delete effect at " + x + "," + y + " but found it at " + t
              .getTileX() + ", " + t
              .getTileY() + " instead.");
            return;
          }
        }
      }
    }
    else {
      logger.log(Level.WARNING, "Tile at " + x + "," + y + " failed to remove effect: No Tile Found");
    }
  }
  
  public int addCreature(long creatureId)
    throws NoSuchCreatureException, NoSuchPlayerException
  {
    Creature creature = null;
    creature = Server.getInstance().getCreature(creatureId);
    this.creatures += 1;
    if ((creature.isDefendKingdom()) || (creature.isAggWhitie())) {
      this.kingdomCreatures += 1;
    }
    if ((creature.getTemplate().getTemplateId() == 105) && 
      (!fogSpiders.contains(Long.valueOf(creatureId)))) {
      fogSpiders.add(Long.valueOf(creatureId));
    }
    int x = creature.getTileX();
    int y = creature.getTileY();
    VolaTile tile = getOrCreateTile(x, y);
    return tile.addCreature(creature, 0);
  }
  
  final void write(BufferedWriter writer)
    throws IOException
  {}
  
  public final void removeCreature(Creature creature, boolean delete, boolean removeAsTarget)
  {
    this.creatures -= 1;
    if ((creature.isDefendKingdom()) || (creature.isAggWhitie())) {
      this.kingdomCreatures -= 1;
    }
    if ((delete) && (this.zoneWatchers != null))
    {
      VirtualZone[] watchers = getWatchers();
      for (VirtualZone lWatcher : watchers) {
        try
        {
          lWatcher.deleteCreature(creature, removeAsTarget);
        }
        catch (NoSuchPlayerException nsp)
        {
          logger.log(Level.WARNING, creature.getName() + ": " + nsp.getMessage(), nsp);
        }
        catch (NoSuchCreatureException nsc)
        {
          logger.log(Level.WARNING, creature.getName() + ": " + nsc.getMessage(), nsc);
        }
      }
    }
  }
  
  public final void deleteCreature(Creature creature, boolean deleteFromTile)
    throws NoSuchCreatureException, NoSuchPlayerException
  {
    this.creatures -= 1;
    if ((creature.isDefendKingdom()) || (creature.isAggWhitie())) {
      this.kingdomCreatures -= 1;
    }
    int x;
    if (deleteFromTile)
    {
      x = creature.getTileX();
      int y = creature.getTileY();
      
      VolaTile tile = getTileOrNull(x, y);
      if (tile != null)
      {
        if (!tile.removeCreature(creature))
        {
          boolean ok = false;
          if (creature.getCurrentTile() != null) {
            if (creature.getCurrentTile().removeCreature(creature)) {
              ok = true;
            }
          }
        }
      }
      else
      {
        boolean ok = false;
        if (creature.getCurrentTile() != null) {
          if (creature.getCurrentTile().removeCreature(creature)) {
            ok = true;
          }
        }
        logger.log(Level.WARNING, this.id + " tile " + x + "," + y + " where " + creature.getName() + " should be didn't contain it. The creature.currentTile removed it=" + ok, new Exception());
      }
    }
    if (this.zoneWatchers != null)
    {
      for (VirtualZone zone : this.zoneWatchers) {
        zone.deleteCreature(creature, true);
      }
      if (this.isOnSurface)
      {
        if (creature.getVisionArea() != null)
        {
          VirtualZone vz = creature.getVisionArea().getSurface();
          this.zoneWatchers.remove(vz);
        }
      }
      else if (creature.getVisionArea() != null)
      {
        VirtualZone vz = creature.getVisionArea().getUnderGround();
        this.zoneWatchers.remove(vz);
      }
    }
  }
  
  public final void addItem(Item item)
  {
    addItem(item, false, false, false);
  }
  
  public final void addItem(Item item, boolean moving, boolean newLayer, boolean starting)
  {
    int x = item.getTileX();
    int y = item.getTileY();
    if (!covers(x, y))
    {
      logger.log(Level.WARNING, this.id + " zone at " + this.startX + ", " + this.endX + "-" + this.startY + "," + this.endY + " surf=" + this.isOnSurface + " doesn't cover " + x + " (" + item
        .getPosX() + ") ," + y + " (" + item.getPosY() + "), a " + item
        .getName() + " id " + item.getWurmId(), new Exception());
      try
      {
        Zone z = Zones.getZone(x, y, isOnSurface());
        logger.log(Level.INFO, "Adding to " + z.getId());
        z.addItem(item, moving, newLayer, starting);
      }
      catch (NoSuchZoneException nsz)
      {
        logger.log(Level.WARNING, "No such zone: " + x + ", " + y + " at ", nsz);
      }
    }
    else
    {
      if ((item.isKingdomMarker()) || (item.getTemplateId() == 996))
      {
        if (!isOnSurface())
        {
          Kingdoms.destroyTower(item);
          Items.decay(item.getWurmId(), item.getDbStrings());
          return;
        }
        if ((item.isGuardTower()) && (!moving)) {
          Zones.addGuardTower(item);
        }
      }
      else if (item.getTemplateId() == 521)
      {
        this.creatureSpawn = item;
        spawnPoints += 1;
      }
      else if (item.getTemplateId() == 995)
      {
        this.treasureChest = item;
        treasureChests += 1;
      }
      VolaTile tile = getOrCreateTile(x, y);
      tile.addItem(item, moving, starting);
      if (newLayer) {
        tile.newLayer(item);
      }
    }
  }
  
  public final void removeItem(Item item)
  {
    removeItem(item, false, false);
  }
  
  public void updateModelName(Item item)
  {
    int x = item.getTileX();
    int y = item.getTileY();
    VolaTile tile = getTileOrNull(x, y);
    if (tile != null) {
      for (VirtualZone vz : getWatchers()) {
        try
        {
          vz.getWatcher().getCommunicator().sendChangeModelName(item);
        }
        catch (Exception e)
        {
          logger.log(Level.WARNING, e.getMessage(), e);
        }
      }
    } else {
      logger.log(Level.WARNING, "Failed to remove " + item.getName() + " at " + x + ", " + y + ". Duplicate methods calling?");
    }
  }
  
  public void updatePile(Item pile)
  {
    int x = pile.getTileX();
    int y = pile.getTileY();
    
    VolaTile tile = getTileOrNull(x, y);
    if (tile != null) {
      tile.updatePile(pile);
    } else {
      logger.log(Level.WARNING, "Failed to update pile at x: " + x + " y: " + y);
    }
  }
  
  public void removeItem(Item item, boolean moving, boolean newLayer)
  {
    item.setZoneId(-10, this.isOnSurface);
    int x = item.getTileX();
    int y = item.getTileY();
    VolaTile tile = getTileOrNull(x, y);
    if (tile != null)
    {
      tile.removeItem(item, moving);
      if (newLayer) {
        tile.newLayer(item);
      }
      if (((item.isUnfinished()) || (item.isUseOnGroundOnly())) && 
        (item.getWatcherSet() != null)) {
        for (Creature cret : item.getWatcherSet()) {
          cret.getCommunicator().sendRemoveFromCreationWindow(item.getWurmId());
        }
      }
    }
    else
    {
      logger.log(Level.WARNING, "Failed to remove " + item.getName() + " at " + x + ", " + y + ". Duplicate methods calling?");
    }
    if (item.getTemplateId() == 521)
    {
      this.creatureSpawn = null;
      spawnPoints -= 1;
    }
    else if (item.getTemplateId() == 995)
    {
      this.treasureChest = null;
      treasureChests -= 1;
    }
  }
  
  public final void removeStructure(Structure structure)
  {
    if (this.structures != null) {
      this.structures.remove(structure);
    }
  }
  
  public final void addStructure(Structure structure)
  {
    if (this.structures == null) {
      this.structures = new HashSet();
    }
    if (!this.structures.contains(structure)) {
      this.structures.add(structure);
    }
  }
  
  public final void addFence(Fence fence)
  {
    int tilex = fence.getTileX();
    int tiley = fence.getTileY();
    VolaTile tile = getOrCreateTile(tilex, tiley);
    tile.addFence(fence);
  }
  
  public final void removeFence(Fence fence)
  {
    int tilex = fence.getTileX();
    int tiley = fence.getTileY();
    VolaTile tile = getOrCreateTile(tilex, tiley);
    tile.removeFence(fence);
    if ((fence.isDoor()) && (fence.isFinished()))
    {
      FenceGate gate = FenceGate.getFenceGate(fence.getId());
      if (gate != null)
      {
        gate.removeFromVillage();
        gate.removeFromTiles();
        
        gate.delete();
      }
      else
      {
        logger.log(Level.WARNING, "fencegate did not exist for fence " + this.id, new Exception());
      }
    }
  }
  
  public final Structure[] getStructures()
  {
    if (this.structures != null) {
      return (Structure[])this.structures.toArray(new Structure[this.structures.size()]);
    }
    return emptyStructures;
  }
  
  final void linkTo(VirtualZone virtualZone, int aStartX, int aStartY, int aEndX, int aEndY)
  {
    long now = System.nanoTime();
    VolaTile[] lTileArray = getTiles();
    for (VolaTile lElement : lTileArray)
    {
      int centerX = virtualZone.getCenterX();
      int centerY = virtualZone.getCenterY();
      if ((lElement.tilex < aStartX) || (lElement.tilex > aEndX) || (lElement.tiley < aStartY) || (lElement.tiley > aEndY))
      {
        lElement.removeWatcher(virtualZone);
      }
      else if ((lElement.tilex == aStartX) || (lElement.tilex == aEndX))
      {
        if ((lElement.tiley >= aStartY) || (lElement.tiley <= aEndY)) {
          lElement.addWatcher(virtualZone);
        }
      }
      else if ((lElement.tiley == aStartY) || (lElement.tiley == aEndY))
      {
        if ((lElement.tilex >= aStartX) || (lElement.tilex <= aEndX)) {
          lElement.addWatcher(virtualZone);
        }
      }
      else if (virtualZone.getWatcher().isPlayer())
      {
        lElement.linkTo(virtualZone, false);
      }
      else
      {
        int distancex = Math.abs(lElement.tilex - centerX);
        int distancey = Math.abs(lElement.tiley - centerY);
        int distance = Math.max(distancex, distancey);
        if (distance < Math.min(virtualZone.getSize() / 2, 7)) {
          lElement.linkTo(virtualZone, false);
        } else if ((distance == 10) && (this.size >= 10)) {
          lElement.linkTo(virtualZone, false);
        } else if ((distance == 20) && (this.size >= 20)) {
          lElement.linkTo(virtualZone, false);
        }
      }
    }
    float lElapsedTime = (float)(System.nanoTime() - now) / 1000000.0F;
    if (lElapsedTime > (float)LOG_ELAPSED_TIME_THRESHOLD) {
      logger.info("linkTo in zone: " + virtualZone + ", which took " + lElapsedTime + " millis.");
    }
  }
  
  final void addWatcher(int zoneNum)
    throws NoSuchZoneException
  {
    VirtualZone zone = Zones.getVirtualZone(zoneNum);
    if (this.zoneWatchers == null) {
      this.zoneWatchers = new HashSet();
    }
    VirtualZone[] warr = getWatchers();
    VirtualZone[] arrayOfVirtualZone1 = warr;int i = arrayOfVirtualZone1.length;
    VirtualZone lElement;
    for (VirtualZone localVirtualZone1 = 0; localVirtualZone1 < i; localVirtualZone1++)
    {
      lElement = arrayOfVirtualZone1[localVirtualZone1];
      if ((lElement.getWatcher() == null) || (lElement.getWatcher().getWurmId() == zone.getWatcher().getWurmId()))
      {
        if (lElement.getWatcher() != null) {
          logger.log(Level.WARNING, "Old virtualzone being removed:" + lElement.getWatcher().getName(), new Exception());
        } else {
          logger.log(Level.WARNING, "Old virtualzone being removed: watcher=null", new Exception());
        }
        removeWatcher(lElement);
      }
    }
    if (!this.zoneWatchers.contains(zone))
    {
      this.zoneWatchers.add(zone);
      VolaTile[] lTileArray = getTiles();
      VolaTile[] arrayOfVolaTile1 = lTileArray;localVirtualZone1 = arrayOfVolaTile1.length;
      for (lElement = 0; lElement < localVirtualZone1; lElement++)
      {
        VolaTile lElement = arrayOfVolaTile1[lElement];
        if (zone.covers(lElement.getTileX(), lElement.getTileY())) {
          lElement.addWatcher(zone);
        }
      }
    }
  }
  
  final void removeWatcher(VirtualZone zone)
    throws NoSuchZoneException
  {
    for (VolaTile tile : this.tiles.values()) {
      tile.removeWatcher(zone);
    }
    if (this.zoneWatchers != null) {
      this.zoneWatchers.remove(zone);
    }
  }
  
  final Track[] getTracksFor(int tilex, int tiley)
  {
    if (this.tracks == null) {
      return new Track[0];
    }
    return this.tracks.getTracksFor(tilex, tiley);
  }
  
  public final Track[] getTracksFor(int tilex, int tiley, int dist)
  {
    if (this.tracks == null) {
      return new Track[0];
    }
    return this.tracks.getTracksFor(tilex, tiley, dist);
  }
  
  private void addTrack(Track track)
  {
    if (this.tracks == null) {
      this.tracks = new Tracks();
    }
    this.tracks.addTrack(track);
  }
  
  final void createTrack(Creature creature, int tileX, int tileY, int diffTileX, int diffTileY)
  {
    if ((!creature.isGhost()) && (creature.getPower() <= 0) && (creature.getFloorLevel() <= 0) && 
      (!creature.isFish()))
    {
      long now = System.nanoTime();
      
      Track toAdd = null;
      if ((tileX + diffTileX >= 0) && (tileX + diffTileX < 1 << Constants.meshSize) && (tileY + diffTileY >= 0) && (tileX + diffTileX < 1 << Constants.meshSize))
      {
        int tilenum = Server.surfaceMesh.getTile(tileX + diffTileX, tileY + diffTileY);
        if (!this.isOnSurface) {
          tilenum = Server.caveMesh.getTile(tileX + diffTileX, tileY + diffTileY);
        } else {
          Zones.walkedTiles[(tileX + diffTileX)][(tileY + diffTileY)] = 1;
        }
        if (diffTileX < 0)
        {
          if (diffTileY == 0) {
            toAdd = new Track(creature.getWurmId(), creature.getName(), tileX - diffTileX, tileY - diffTileY, tilenum, System.currentTimeMillis(), (byte)6);
          } else if (diffTileY < 0) {
            toAdd = new Track(creature.getWurmId(), creature.getName(), tileX - diffTileX, tileY - diffTileY, tilenum, System.currentTimeMillis(), (byte)7);
          } else if (diffTileY > 0) {
            toAdd = new Track(creature.getWurmId(), creature.getName(), tileX - diffTileX, tileY - diffTileY, tilenum, System.currentTimeMillis(), (byte)5);
          }
        }
        else if (diffTileX > 0)
        {
          if (diffTileY == 0) {
            toAdd = new Track(creature.getWurmId(), creature.getName(), tileX - diffTileX, tileY - diffTileY, tilenum, System.currentTimeMillis(), (byte)2);
          } else if (diffTileY < 0) {
            toAdd = new Track(creature.getWurmId(), creature.getName(), tileX - diffTileX, tileY - diffTileY, tilenum, System.currentTimeMillis(), (byte)1);
          } else if (diffTileY > 0) {
            toAdd = new Track(creature.getWurmId(), creature.getName(), tileX - diffTileX, tileY - diffTileY, tilenum, System.currentTimeMillis(), (byte)3);
          }
        }
        else if (diffTileY > 0)
        {
          if (diffTileX == 0) {
            toAdd = new Track(creature.getWurmId(), creature.getName(), tileX - diffTileX, tileY - diffTileY, tilenum, System.currentTimeMillis(), (byte)4);
          }
        }
        else if (diffTileY < 0) {
          if (diffTileX == 0) {
            toAdd = new Track(creature.getWurmId(), creature.getName(), tileX - diffTileX, tileY - diffTileY, tilenum, System.currentTimeMillis(), (byte)0);
          }
        }
        if (toAdd != null) {
          addTrack(toAdd);
        }
        if (Server.rand.nextInt(100) == 0) {
          if (this.tracks.getTracksFor(tileX - diffTileX, tileY - diffTileY).length > 20) {
            if (creature.isOnSurface()) {
              if ((!creature.isTypeFleeing()) || 
                ((creature.getCurrentVillage() == null) && (Server.rand.nextInt(20) == 0)) || (
                (creature.getCurrentVillage() != null) && (Server.rand.nextInt(50) == 0)))
              {
                MeshIO mesh = Server.surfaceMesh;
                byte type = Tiles.decodeType(tilenum);
                if ((type == Tiles.Tile.TILE_GRASS.id) || (type == Tiles.Tile.TILE_LAWN.id) || (type == Tiles.Tile.TILE_REED.id) || (type == Tiles.Tile.TILE_DIRT.id) || (type == Tiles.Tile.TILE_MYCELIUM.id))
                {
                  mesh.setTile(tileX + diffTileX, tileY + diffTileY, Tiles.encode(Tiles.decodeHeight(tilenum), Tiles.Tile.TILE_DIRT_PACKED.id, 
                    Tiles.decodeData(tilenum)));
                  Players.getInstance().sendChangedTile(tileX + diffTileX, tileY + diffTileY, creature
                    .isOnSurface(), true);
                }
              }
            }
          }
        }
      }
      float lElapsedTime = (float)(System.nanoTime() - now) / 1000000.0F;
      if (lElapsedTime > (float)LOG_ELAPSED_TIME_THRESHOLD) {
        logger.info("createTrack, Creature id, " + creature.getWurmId() + ", which took " + lElapsedTime + " millis. - " + creature);
      }
    }
  }
  
  public final void changeTile(int x, int y)
  {
    VolaTile tile1 = getOrCreateTile(x, y);
    
    Creature[] crets = tile1.getCreatures();
    Creature[] arrayOfCreature1 = crets;int i = arrayOfCreature1.length;
    Creature lCret2;
    for (Creature localCreature1 = 0; localCreature1 < i; localCreature1++)
    {
      lCret2 = arrayOfCreature1[localCreature1];
      lCret2.setChangedTileCounter();
    }
    VolaTile tile = Zones.getTileOrNull(x - 1, y, this.isOnSurface);
    Creature[] arrayOfCreature2;
    if (tile != null)
    {
      crets = tile.getCreatures();
      arrayOfCreature2 = crets;localCreature1 = arrayOfCreature2.length;
      for (lCret2 = 0; lCret2 < localCreature1; lCret2++)
      {
        Creature lCret2 = arrayOfCreature2[lCret2];
        lCret2.setChangedTileCounter();
      }
    }
    tile = Zones.getTileOrNull(x - 1, y - 1, this.isOnSurface);
    if (tile != null)
    {
      crets = tile.getCreatures();
      arrayOfCreature2 = crets;Creature localCreature2 = arrayOfCreature2.length;
      for (lCret2 = 0; lCret2 < localCreature2; lCret2++)
      {
        Creature lCret2 = arrayOfCreature2[lCret2];
        lCret2.setChangedTileCounter();
      }
    }
    tile = Zones.getTileOrNull(x, y - 1, this.isOnSurface);
    if (tile != null)
    {
      crets = tile.getCreatures();
      arrayOfCreature2 = crets;Creature localCreature3 = arrayOfCreature2.length;
      for (lCret2 = 0; lCret2 < localCreature3; lCret2++)
      {
        Creature lCret2 = arrayOfCreature2[lCret2];
        lCret2.setChangedTileCounter();
      }
    }
    tile = Zones.getTileOrNull(x + 1, y - 1, this.isOnSurface);
    if (tile != null)
    {
      crets = tile.getCreatures();
      arrayOfCreature2 = crets;Creature localCreature4 = arrayOfCreature2.length;
      for (lCret2 = 0; lCret2 < localCreature4; lCret2++)
      {
        Creature lCret2 = arrayOfCreature2[lCret2];
        lCret2.setChangedTileCounter();
      }
    }
    tile = Zones.getTileOrNull(x + 1, y, this.isOnSurface);
    if (tile != null)
    {
      crets = tile.getCreatures();
      arrayOfCreature2 = crets;Creature localCreature5 = arrayOfCreature2.length;
      for (lCret2 = 0; lCret2 < localCreature5; lCret2++)
      {
        Creature lCret2 = arrayOfCreature2[lCret2];
        lCret2.setChangedTileCounter();
      }
    }
    tile = Zones.getTileOrNull(x - 1, y + 1, this.isOnSurface);
    if (tile != null)
    {
      crets = tile.getCreatures();
      arrayOfCreature2 = crets;Creature localCreature6 = arrayOfCreature2.length;
      for (lCret2 = 0; lCret2 < localCreature6; lCret2++)
      {
        Creature lCret2 = arrayOfCreature2[lCret2];
        lCret2.setChangedTileCounter();
      }
    }
    tile = Zones.getTileOrNull(x, y + 1, this.isOnSurface);
    if (tile != null)
    {
      crets = tile.getCreatures();
      arrayOfCreature2 = crets;Creature localCreature7 = arrayOfCreature2.length;
      for (lCret2 = 0; lCret2 < localCreature7; lCret2++)
      {
        Creature lCret2 = arrayOfCreature2[lCret2];
        lCret2.setChangedTileCounter();
      }
    }
    tile = Zones.getTileOrNull(x + 1, y + 1, this.isOnSurface);
    if (tile != null)
    {
      crets = tile.getCreatures();
      arrayOfCreature2 = crets;Creature localCreature8 = arrayOfCreature2.length;
      for (lCret2 = 0; lCret2 < localCreature8; lCret2++)
      {
        Creature lCret2 = arrayOfCreature2[lCret2];
        lCret2.setChangedTileCounter();
      }
    }
    tile1.change();
  }
  
  public final void addGates(Village village)
  {
    for (VolaTile tile : this.tiles.values())
    {
      Door[] doors = tile.getDoors();
      if (doors != null) {
        for (Door lDoor : doors) {
          if ((lDoor instanceof FenceGate)) {
            if (village.covers(tile.getTileX(), tile.getTileY())) {
              village.addGate((FenceGate)lDoor);
            }
          }
        }
      }
    }
  }
  
  public final void addMineDoors(Village village)
  {
    for (int x = this.startX; x < this.endX; x++) {
      for (int y = this.startY; y < this.endY; y++)
      {
        MineDoorPermission md = MineDoorPermission.getPermission(x, y);
        if (md != null) {
          if (village.covers(x, y)) {
            village.addMineDoor(md);
          }
        }
      }
    }
  }
  
  static int totalItems = 0;
  
  protected final void loadAllItemsForZone()
  {
    if (Items.getAllItemsForZone(this.id) != null) {
      for (Item item : Items.getAllItemsForZone(this.id)) {
        addItem(item, false, false, true);
      }
    }
  }
  
  protected void getItemsByZoneId() {}
  
  abstract void save()
    throws IOException;
  
  abstract void load()
    throws IOException;
  
  abstract void loadFences()
    throws IOException;
  
  final void checkIntegrity(Creature checker)
  {
    for (Iterator localIterator1 = this.tiles.values().iterator(); localIterator1.hasNext();)
    {
      t = (VolaTile)localIterator1.next();
      for (VolaTile t2 : this.tiles.values()) {
        if ((t != t2) && (t.tilex == t2.tilex) && (t.tiley == t2.tiley)) {
          checker.getCommunicator().sendNormalServerMessage("Z " + 
            getId() + " multiple tiles:" + t.tilex + ", " + t.tiley);
        }
      }
    }
    VolaTile t;
  }
  
  public final String toString()
  {
    StringBuilder lBuilder = new StringBuilder(200);
    lBuilder.append("Zone [id: ").append(this.id);
    lBuilder.append(", startXY: ").append(this.startX).append(',').append(this.startY);
    lBuilder.append(", endXY: ").append(this.endX).append(',').append(this.endY);
    lBuilder.append(", size: ").append(this.size);
    lBuilder.append(", highest: ").append(this.highest);
    lBuilder.append(", isForest: ").append(this.isForest);
    lBuilder.append(", isLoaded: ").append(this.isLoaded);
    lBuilder.append(", isOnSurface: ").append(this.isOnSurface);
    lBuilder.append(']');
    return super.toString();
  }
  
  public boolean isHasRift()
  {
    return this.hasRift;
  }
  
  public void setHasRift(boolean hasRift)
  {
    this.hasRift = hasRift;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\zones\Zone.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */