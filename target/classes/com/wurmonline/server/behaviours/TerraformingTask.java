package com.wurmonline.server.behaviours;

import com.wurmonline.mesh.MeshIO;
import com.wurmonline.mesh.Tiles;
import com.wurmonline.mesh.Tiles.Tile;
import com.wurmonline.server.LoginHandler;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.Players;
import com.wurmonline.server.Server;
import com.wurmonline.server.ServerEntry;
import com.wurmonline.server.Servers;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.ItemFactory;
import com.wurmonline.server.kingdom.Kingdom;
import com.wurmonline.server.kingdom.Kingdoms;
import com.wurmonline.server.meshgen.ImprovedNoise;
import com.wurmonline.server.meshgen.IslandAdder;
import com.wurmonline.server.structures.Fence;
import com.wurmonline.server.structures.Wall;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.villages.Villages;
import com.wurmonline.server.zones.Den;
import com.wurmonline.server.zones.FocusZone;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zones;
import com.wurmonline.shared.constants.ItemMaterials;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TerraformingTask
  implements MiscConstants, ItemMaterials
{
  private int counter = 0;
  private final int task;
  private final byte kingdom;
  private final int entityId;
  private final String entityName;
  private int startX = 0;
  private int startY = 0;
  private int startHeight = 0;
  private TerraformingTask next = null;
  private final int tasksRemaining;
  private int totalTasks = 0;
  private final Random random = new Random();
  private final boolean firstTask;
  public static final int ERUPT = 0;
  public static final int INDENT = 1;
  public static final int PLATEAU = 2;
  public static final int CRATERS = 3;
  public static final int MULTIPLATEAU = 4;
  public static final int RAVINE = 5;
  public static final int ISLAND = 6;
  public static final int MULTIRAVINE = 7;
  private int radius = 0;
  private int length = 0;
  private int direction = 0;
  private MeshIO topLayer;
  private MeshIO rockLayer;
  private final String[] prefixes = { "Et", "De", "Old", "Gaz", "Mak", "Fir", "Fyre", "Eld", "Vagn", "Mag", "Lav", "Volc", "Rad", "Ash", "Ask" };
  private final String[] suffixes = { "na", "cuse", "fir", "egap", "dire", "haul", "vann", "un", "lik", "ingan", "enken", "mosh", "kil", "atrask", "eskap" };
  private static final Logger logger = Logger.getLogger(TerraformingTask.class.getName());
  
  public TerraformingTask(int whatToDo, byte targetKingdom, String epicEntityName, int epicEntityId, int tasksLeft, boolean isFirstTask)
  {
    this.task = whatToDo;
    this.kingdom = targetKingdom;
    this.entityName = epicEntityName;
    this.entityId = epicEntityId;
    if (tasksLeft < 0) {
      if (this.task == 3)
      {
        int numCratersRoot = 1;
        this.totalTasks = (1 + 1 * this.random.nextInt(Math.max(1, 1)));
        tasksLeft = this.totalTasks;
      }
      else if ((this.task == 4) || (this.task == 7))
      {
        this.totalTasks = (1 + this.random.nextInt(2));
        tasksLeft = this.totalTasks;
      }
    }
    this.firstTask = isFirstTask;
    this.tasksRemaining = tasksLeft;
    
    this.startX = 0;
    this.startY = 0;
    Server.getInstance().addTerraformingTask(this);
  }
  
  public void setSXY(int sx, int sy)
  {
    this.startX = sx;
    this.startY = sy;
  }
  
  private void setHeight(int sheight)
  {
    this.startHeight = sheight;
  }
  
  private void setTotalTasks(int total)
  {
    this.totalTasks = total;
  }
  
  public final boolean setCoordinates()
  {
    boolean toReturn = false;
    switch (this.task)
    {
    case 0: 
      toReturn = eruptCoord();
      break;
    case 1: 
      toReturn = indentCoord();
      break;
    case 2: 
    case 4: 
      toReturn = plateauCoord();
      break;
    case 3: 
      toReturn = craterCoord();
      break;
    case 5: 
    case 7: 
      toReturn = ravineCoord();
      break;
    case 6: 
      toReturn = islandCoord();
      break;
    default: 
      toReturn = false;
    }
    return toReturn;
  }
  
  public boolean poll()
  {
    if (this.next != null) {
      return this.next.poll();
    }
    if (this.counter == 0) {
      if ((this.startX != 0) && (this.startY != 0)) {
        sendEffect();
      } else if ((this.startX == 0) && (this.startY == 0) && (setCoordinates())) {
        sendEffect();
      } else {
        return true;
      }
    }
    if (this.counter == 60)
    {
      terraform();
      if (this.tasksRemaining == 0) {
        return true;
      }
    }
    if (this.counter == 65) {
      if (this.tasksRemaining > 0)
      {
        this.next = new TerraformingTask(this.task, this.kingdom, this.entityName, this.entityId, this.tasksRemaining - 1, false);
        
        this.next.setCoordinates();
        
        this.next.setSXY(this.startX, this.startY);
        if ((this.task == 4) || (this.task == 7))
        {
          if (this.random.nextBoolean())
          {
            int modx = (this.startX + this.radius - (this.startX - this.radius)) / (1 + this.random.nextInt(4));
            int mody = (this.startY + this.radius - (this.startY - this.radius)) / (1 + this.random.nextInt(4));
            if (this.random.nextBoolean()) {
              modx = -modx;
            }
            if (this.random.nextBoolean()) {
              mody = -mody;
            }
            if (this.startX + modx > Zones.worldTileSizeX - 200) {
              this.startX -= 200;
            }
            if (this.startY + mody > Zones.worldTileSizeY - 200) {
              this.startY -= 200;
            }
            if (this.startX + modx < 200) {
              this.startX += 200;
            }
            if (this.startY + mody < 200) {
              this.startY += 200;
            }
            this.next.setSXY(this.startX + modx, this.startY + mody);
          }
        }
        else if (this.task == 3)
        {
          int modx = (int)(this.random.nextGaussian() * 3.0D * this.radius);
          int mody = (int)(this.random.nextGaussian() * 3.0D * this.radius);
          if (this.startX + modx > Zones.worldTileSizeX - 200) {
            this.startX -= modx;
          }
          if (this.startY + mody > Zones.worldTileSizeY - 200) {
            this.startY -= mody;
          }
          if (this.startX + modx < 200) {
            this.startX += modx;
          }
          if (this.startY + mody < 200) {
            this.startY += mody;
          }
          this.next.setSXY(this.startX + modx, this.startY + mody);
        }
        this.next.setHeight(this.startHeight);
        this.next.setTotalTasks(this.totalTasks);
      }
    }
    this.counter += 1;
    return false;
  }
  
  private void terraform()
  {
    switch (this.task)
    {
    case 0: 
      erupt();
      break;
    case 1: 
      indent();
      break;
    case 2: 
    case 4: 
      plateau();
      break;
    case 3: 
      crater();
      break;
    case 5: 
    case 7: 
      ravine();
      break;
    case 6: 
      island();
      break;
    }
  }
  
  private final boolean ravineCoord()
  {
    boolean toReturn = false;
    this.radius = (5 + this.random.nextInt(5));
    
    this.length = (20 + this.random.nextInt(40));
    this.direction = this.random.nextInt(8);
    for (int runs = 0; runs < 20; runs++) {
      if (this.firstTask)
      {
        this.startX = this.random.nextInt(Zones.worldTileSizeX);
        this.startY = this.random.nextInt(Zones.worldTileSizeY);
        if (this.startX > Zones.worldTileSizeX - 200) {
          this.startX -= 200;
        }
        if (this.startY > Zones.worldTileSizeY - 200) {
          this.startY -= 200;
        }
        if (this.startX < 200) {
          this.startX += 200;
        }
        if (this.startY < 200) {
          this.startY += 200;
        }
        if (Tiles.decodeHeight(Server.surfaceMesh.getTile(this.startX, this.startY)) > 0) {
          if (isOutsideOwnKingdom(this.startX, this.startY))
          {
            toReturn = true;
            break;
          }
        }
      }
      else
      {
        return true;
      }
    }
    return toReturn;
  }
  
  private void ravine()
  {
    if ((this.totalTasks > 0) && 
      (this.totalTasks % 2 == 0)) {
      this.direction = this.totalTasks;
    }
    IslandAdder isl = new IslandAdder(Server.surfaceMesh, Server.rockMesh);
    Map<Integer, Set<Integer>> changes = null;
    changes = isl.createRavine(Zones.safeTileX(this.startX - this.radius), Zones.safeTileY(this.startY - this.radius), this.length, this.direction);
    logger.log(Level.INFO, "Ravine at " + this.startX + "," + this.startY);
    if (changes != null)
    {
      int minx = Zones.worldTileSizeX;
      int miny = Zones.worldTileSizeY;
      int maxx = 0;
      int maxy = 0;
      for (Map.Entry<Integer, Set<Integer>> me : changes.entrySet())
      {
        x = (Integer)me.getKey();
        if (x.intValue() < minx) {
          minx = x.intValue();
        }
        if (x.intValue() > maxx) {
          maxx = x.intValue();
        }
        Set<Integer> set = (Set)me.getValue();
        for (Integer y : set)
        {
          if (y.intValue() < miny) {
            miny = y.intValue();
          }
          if (y.intValue() > maxy) {
            maxy = y.intValue();
          }
          Terraforming.forceSetAsRock(x.intValue(), y.intValue(), Tiles.Tile.TILE_CAVE_WALL_ORE_GLIMMERSTEEL.id, 100);
          changeTile(x.intValue(), y.intValue());
          Players.getInstance().sendChangedTile(x.intValue(), y.intValue(), true, true);
          destroyStructures(x.intValue(), y.intValue());
        }
      }
      try
      {
        Integer x;
        ItemFactory.createItem(696, 99.0F, (minx + (maxx - minx) / 2) * 4 + 2, (miny + (maxy - miny) / 2) * 4 + 2, this.random
          .nextFloat() * 350.0F, true, (byte)57, (byte)0, -10L, null);
      }
      catch (Exception ex)
      {
        logger.log(Level.WARNING, ex.getMessage(), ex);
      }
    }
  }
  
  private static final void destroyStructures(int x, int y)
  {
    VolaTile t = Zones.getTileOrNull(x, y, true);
    if (t != null)
    {
      short[] steepness = Creature.getTileSteepness(x, y, true);
      if (t.getStructure() != null) {
        if (steepness[1] > 40) {
          for (Wall w : t.getWalls()) {
            w.setAsPlan();
          }
        }
      }
      for (Fence f : t.getFences()) {
        if (steepness[1] > 40) {
          f.destroy();
        }
      }
    }
  }
  
  private final boolean craterCoord()
  {
    boolean toReturn = false;
    this.radius = (10 + this.random.nextInt(20));
    for (int runs = 0; runs < 20; runs++) {
      if ((this.firstTask) && (this.startX <= 0) && (this.startY <= 0))
      {
        this.startX = this.random.nextInt(Zones.worldTileSizeX);
        this.startY = this.random.nextInt(Zones.worldTileSizeY);
        if (Tiles.decodeHeight(Server.surfaceMesh.getTile(this.startX, this.startY)) > 0) {
          if (isOutsideOwnKingdom(this.startX, this.startY))
          {
            toReturn = true;
            break;
          }
        }
      }
      else
      {
        return true;
      }
    }
    return toReturn;
  }
  
  public final void changeTile(int x, int y)
  {
    VolaTile tile1 = Zones.getTileOrNull(x, y, true);
    Creature localCreature1;
    Creature lCret2;
    if (tile1 != null)
    {
      Creature[] crets = tile1.getCreatures();
      Creature[] arrayOfCreature1 = crets;int i = arrayOfCreature1.length;
      for (localCreature1 = 0; localCreature1 < i; localCreature1++)
      {
        lCret2 = arrayOfCreature1[localCreature1];
        lCret2.setChangedTileCounter();
      }
      tile1.change();
    }
    VolaTile tile2 = Zones.getTileOrNull(x, y, false);
    if (tile2 != null)
    {
      Creature[] crets = tile2.getCreatures();
      Creature[] arrayOfCreature2 = crets;localCreature1 = arrayOfCreature2.length;
      for (lCret2 = 0; lCret2 < localCreature1; lCret2++)
      {
        Creature lCret2 = arrayOfCreature2[lCret2];
        lCret2.setChangedTileCounter();
      }
      tile2.change();
    }
  }
  
  private void crater()
  {
    boolean ok = true;
    if (this.radius == 0) {
      this.radius = (10 + this.random.nextInt(20));
    }
    for (int x = 0; x < 10; x++)
    {
      int sx = Zones.safeTileX(this.startX - this.radius);
      int sy = Zones.safeTileY(this.startY - this.radius);
      int ex = Zones.safeTileX(this.startX + this.radius);
      int ey = Zones.safeTileY(this.startY + this.radius);
      
      Set<Village> blockers = Villages.getVillagesWithin(sx, sy, ex, ey);
      if ((blockers == null) || (blockers.size() == 0))
      {
        ok = true;
        break;
      }
      for (Village v : blockers) {
        logger.log(Level.WARNING, v.getName() + " is in the way at " + sx + "," + sy + " to " + ex + "," + ey);
      }
      ok = false;
      
      int modx = (int)(this.random.nextGaussian() * this.radius);
      int mody = (int)(this.random.nextGaussian() * this.radius);
      if (this.startX + modx > Zones.worldTileSizeX - 200) {
        this.startX -= modx;
      }
      if (this.startY + mody > Zones.worldTileSizeY - 200) {
        this.startY -= mody;
      }
      if (this.startX + modx < 200) {
        this.startX += modx;
      }
      if (this.startY + mody < 200) {
        this.startY += mody;
      }
      if (Servers.localServer.testServer) {
        logger.log(Level.INFO, "MOdx=" + modx + ", mody=" + mody + " radius=" + this.radius + " yields sx=" + (this.startX + modx) + " sy " + (this.startY + mody));
      }
      setSXY(this.startX + modx, this.startY + mody);
    }
    if (!ok)
    {
      logger.log(Level.INFO, "Avoiding Crater at " + this.startX + "," + this.startY + " radius=" + this.radius);
      return;
    }
    Map<Integer, Set<Integer>> changes = null;
    IslandAdder isl = new IslandAdder(Server.surfaceMesh, Server.rockMesh);
    
    int sx = Zones.safeTileX(this.startX - this.radius);
    int sy = Zones.safeTileY(this.startY - this.radius);
    int ex = Zones.safeTileX(this.startX + this.radius);
    int ey = Zones.safeTileY(this.startY + this.radius);
    changes = isl.createCrater(sx, sy, ex, ey);
    logger.log(Level.INFO, "Crater at " + this.startX + "," + this.startY + " radius=" + this.radius);
    if (changes != null)
    {
      int minx = Zones.worldTileSizeX;
      int miny = Zones.worldTileSizeY;
      int maxx = 0;
      int maxy = 0;
      for (Map.Entry<Integer, Set<Integer>> me : changes.entrySet())
      {
        x = (Integer)me.getKey();
        if (x.intValue() < minx) {
          minx = x.intValue();
        }
        if (x.intValue() > maxx) {
          maxx = x.intValue();
        }
        Set<Integer> set = (Set)me.getValue();
        for (Integer y : set)
        {
          if (y.intValue() < miny) {
            miny = y.intValue();
          }
          if (y.intValue() > maxy) {
            maxy = y.intValue();
          }
          Terraforming.forceSetAsRock(x.intValue(), y.intValue(), Tiles.Tile.TILE_CAVE_WALL_ORE_GLIMMERSTEEL.id, 100);
          changeTile(x.intValue(), y.intValue());
          Players.getInstance().sendChangedTile(x.intValue(), y.intValue(), true, true);
          destroyStructures(x.intValue(), y.intValue());
        }
      }
      try
      {
        Integer x;
        ItemFactory.createItem(696, 99.0F, this.startX * 4 + 2, this.startY * 4 + 2, this.random
          .nextFloat() * 350.0F, true, (byte)57, (byte)0, -10L, null);
      }
      catch (Exception exs)
      {
        logger.log(Level.WARNING, exs.getMessage(), exs);
      }
    }
  }
  
  private final boolean indentCoord()
  {
    boolean toReturn = false;
    this.radius = (10 + this.random.nextInt(20));
    for (int runs = 0; runs < 20; runs++)
    {
      this.startX = this.random.nextInt(Zones.worldTileSizeX);
      this.startY = this.random.nextInt(Zones.worldTileSizeY);
      if (Tiles.decodeHeight(Server.surfaceMesh.getTile(this.startX, this.startY)) > 0) {
        if (isOutsideOwnKingdom(this.startX, this.startY))
        {
          toReturn = true;
          break;
        }
      }
    }
    return toReturn;
  }
  
  private void indent()
  {
    Map<Integer, Set<Integer>> changes = null;
    IslandAdder isl = new IslandAdder(Server.surfaceMesh, Server.rockMesh);
    changes = isl.createRockIndentation(Zones.safeTileX(this.startX - this.radius), Zones.safeTileY(this.startY - this.radius), 
      Zones.safeTileX(this.startX + this.radius), Zones.safeTileY(this.startY + this.radius));
    logger.log(Level.INFO, "Rock Indentation at " + this.startX + "," + this.startY);
    int minx;
    int miny;
    int maxx;
    int maxy;
    if (changes != null)
    {
      minx = Zones.worldTileSizeX;
      miny = Zones.worldTileSizeY;
      maxx = 0;
      maxy = 0;
      for (Map.Entry<Integer, Set<Integer>> me : changes.entrySet())
      {
        x = (Integer)me.getKey();
        if (x.intValue() < minx) {
          minx = x.intValue();
        }
        if (x.intValue() > maxx) {
          maxx = x.intValue();
        }
        Set<Integer> set = (Set)me.getValue();
        for (Integer y : set)
        {
          if (y.intValue() < miny) {
            miny = y.intValue();
          }
          if (y.intValue() > maxy) {
            maxy = y.intValue();
          }
          Terraforming.forceSetAsRock(x.intValue(), y.intValue(), (byte)1, 100);
          changeTile(x.intValue(), y.intValue());
          Players.getInstance().sendChangedTile(x.intValue(), y.intValue(), true, true);
          destroyStructures(x.intValue(), y.intValue());
        }
      }
    }
    Integer x;
  }
  
  private final boolean isOutsideOwnKingdom(int tilex, int tiley)
  {
    byte kingdomId = Zones.getKingdom(tilex, tiley);
    Kingdom k = Kingdoms.getKingdom(kingdomId);
    if ((k == null) || (k.getTemplate() != this.kingdom)) {
      return true;
    }
    return false;
  }
  
  private boolean eruptCoord()
  {
    boolean toReturn = false;
    int maxTries = 20;
    for (int x = 0; x < 20; x++)
    {
      Den d = Zones.getRandomTop();
      this.radius = (10 + this.random.nextInt(20));
      if ((this.startX > 0) || (this.startY > 0)) {
        break;
      }
      if (d != null) {
        if (isOutsideOwnKingdom(d.getTilex(), d.getTiley()))
        {
          this.startX = d.getTilex();
          this.startY = d.getTiley();
          toReturn = true;
          break;
        }
      }
    }
    return toReturn;
  }
  
  private void erupt()
  {
    IslandAdder isl = new IslandAdder(Server.surfaceMesh, Server.rockMesh);
    Map<Integer, Set<Integer>> changes = null;
    changes = isl.createVolcano(Zones.safeTileX(this.startX - this.radius), Zones.safeTileY(this.startY - this.radius), 
      Zones.safeTileX(this.startX + this.radius), Zones.safeTileY(this.startY + this.radius));
    logger.log(Level.INFO, "Volcano Eruption at " + this.startX + "," + this.startY);
    if (changes != null)
    {
      int minx = Zones.worldTileSizeX;
      int miny = Zones.worldTileSizeY;
      int maxx = 0;
      int maxy = 0;
      for (Map.Entry<Integer, Set<Integer>> me : changes.entrySet())
      {
        x = (Integer)me.getKey();
        if (x.intValue() < minx) {
          minx = x.intValue();
        }
        if (x.intValue() > maxx) {
          maxx = x.intValue();
        }
        Set<Integer> set = (Set)me.getValue();
        for (Integer y : set)
        {
          if (y.intValue() < miny) {
            miny = y.intValue();
          }
          if (y.intValue() > maxy) {
            maxy = y.intValue();
          }
          Terraforming.forceSetAsRock(x.intValue(), y.intValue(), Tiles.Tile.TILE_CAVE_WALL_ORE_ADAMANTINE.id, 100);
          changeTile(x.intValue(), y.intValue());
          Players.getInstance().sendChangedTile(x.intValue(), y.intValue(), true, true);
          Players.getInstance().sendChangedTile(x.intValue(), y.intValue(), false, true);
          destroyStructures(x.intValue(), y.intValue());
        }
      }
      Integer x;
      String name = "Unknown";
      if (Server.rand.nextBoolean())
      {
        name = this.prefixes[Server.rand.nextInt(this.prefixes.length)];
        if (Server.rand.nextInt(10) > 0) {
          name = name + this.suffixes[Server.rand.nextInt(this.suffixes.length)];
        }
      }
      if (Server.rand.nextBoolean())
      {
        name = this.suffixes[Server.rand.nextInt(this.suffixes.length)];
        if (Server.rand.nextInt(10) > 0) {
          name = name + this.prefixes[Server.rand.nextInt(this.prefixes.length)];
        }
      }
      name = LoginHandler.raiseFirstLetter(name);
      new FocusZone(minx, maxx, miny, maxy, (byte)1, name, "", true);
    }
  }
  
  private final boolean plateauCoord()
  {
    boolean toReturn = false;
    this.radius = (10 + this.random.nextInt(20));
    for (int runs = 0; runs < 20; runs++) {
      if (this.firstTask)
      {
        this.startX = this.random.nextInt(Zones.worldTileSizeX);
        this.startY = this.random.nextInt(Zones.worldTileSizeY);
        this.startHeight = 200;
        if (Tiles.decodeHeight(Server.surfaceMesh.getTile(this.startX, this.startY)) > 0) {
          if (isOutsideOwnKingdom(this.startX, this.startY))
          {
            toReturn = true;
            break;
          }
        }
      }
      else
      {
        return true;
      }
    }
    return toReturn;
  }
  
  private void plateau()
  {
    int modx = 0;
    int mody = 0;
    boolean ok = true;
    if (!this.firstTask) {
      for (int x = 0; x < 20; x++)
      {
        modx = (this.startX + this.radius - (this.startX - this.radius)) / (1 + this.random.nextInt(4));
        mody = (this.startY + this.radius - (this.startY - this.radius)) / (1 + this.random.nextInt(4));
        if (this.random.nextBoolean()) {
          modx = -modx;
        }
        if (this.random.nextBoolean()) {
          mody = -mody;
        }
        int sx = Zones.safeTileX(this.startX + modx - this.radius);
        int ex = Zones.safeTileX(this.startX + modx + this.radius);
        int sy = Zones.safeTileY(this.startY + mody - this.radius);
        int ey = Zones.safeTileY(this.startY + mody + this.radius);
        Set<Village> vills = Villages.getVillagesWithin(sx, sy, ex, ey);
        if ((vills == null) || (vills.size() == 0))
        {
          ok = true;
          break;
        }
        ok = false;
      }
    }
    if (!ok)
    {
      logger.log(Level.INFO, "Skipping Plateu at " + this.startX + "," + this.startY);
      return;
    }
    int sx = Zones.safeTileX(this.startX + modx - this.radius);
    int ex = Zones.safeTileX(this.startX + modx + this.radius);
    int sy = Zones.safeTileY(this.startY + mody - this.radius);
    int ey = Zones.safeTileY(this.startY + mody + this.radius);
    
    IslandAdder isl = new IslandAdder(Server.surfaceMesh, Server.rockMesh);
    
    Map<Integer, Set<Integer>> changes = null;
    changes = isl.createPlateau(sx, sy, ex, ey, this.startHeight + this.random.nextInt(150));
    logger.log(Level.INFO, "Plateu at " + this.startX + "," + this.startY);
    int minx;
    int miny;
    int maxx;
    int maxy;
    if (changes != null)
    {
      minx = Zones.worldTileSizeX;
      miny = Zones.worldTileSizeY;
      maxx = 0;
      maxy = 0;
      for (Map.Entry<Integer, Set<Integer>> me : changes.entrySet())
      {
        x = (Integer)me.getKey();
        if (x.intValue() < minx) {
          minx = x.intValue();
        }
        if (x.intValue() > maxx) {
          maxx = x.intValue();
        }
        Set<Integer> set = (Set)me.getValue();
        for (Integer y : set)
        {
          if (y.intValue() < miny) {
            miny = y.intValue();
          }
          if (y.intValue() > maxy) {
            maxy = y.intValue();
          }
          changeTile(x.intValue(), y.intValue());
          Players.getInstance().sendChangedTile(x.intValue(), y.intValue(), true, true);
          destroyStructures(x.intValue(), y.intValue());
        }
      }
    }
    Integer x;
  }
  
  public void sendEffect()
  {
    int tx = 0;
    int ty = 0;
    switch (this.task)
    {
    case 0: 
      Players.getInstance().sendGlobalNonPersistantComplexEffect(-10L, (short)12, this.startX, this.startY, 
        Tiles.decodeHeightAsFloat(Server.surfaceMesh.getTile(this.startX, this.startY)), this.radius, this.direction, this.length, this.kingdom, (byte)this.entityId);
      
      break;
    case 1: 
      Players.getInstance().sendGlobalNonPersistantComplexEffect(-10L, (short)13, this.startX, this.startY, 
        Tiles.decodeHeightAsFloat(Server.surfaceMesh.getTile(this.startX, this.startY)), this.radius, this.direction, this.length, this.kingdom, (byte)this.entityId);
      
      break;
    case 2: 
    case 4: 
      Players.getInstance().sendGlobalNonPersistantComplexEffect(-10L, (short)11, this.startX, this.startY, 
        Tiles.decodeHeightAsFloat(Server.surfaceMesh.getTile(this.startX, this.startY)), this.radius, this.direction, this.length, this.kingdom, (byte)this.entityId);
      
      break;
    case 3: 
      Players.getInstance().sendGlobalNonPersistantComplexEffect(-10L, (short)10, this.startX, this.startY, 
        Tiles.decodeHeightAsFloat(Server.surfaceMesh.getTile(0, 0)), this.radius, this.tasksRemaining, this.direction, this.kingdom, (byte)this.entityId);
      
      break;
    case 5: 
    case 7: 
      Players.getInstance().sendGlobalNonPersistantComplexEffect(-10L, (short)14, 
        Zones.safeTileX(this.startX - this.radius), Zones.safeTileY(this.startY - this.radius), 
        Tiles.decodeHeightAsFloat(Server.surfaceMesh.getTile(this.startX, this.startY)), this.radius, this.length, this.direction, this.kingdom, (byte)this.entityId);
      
      break;
    case 6: 
      Players.getInstance().sendGlobalNonPersistantComplexEffect(-10L, (short)15, this.startX, this.startY, 
        Tiles.decodeHeightAsFloat(Server.surfaceMesh.getTile(this.startX, this.startY)), this.radius, this.length, this.direction, this.kingdom, (byte)this.entityId);
      
      break;
    }
  }
  
  private final boolean islandCoord()
  {
    this.rockLayer = Server.rockMesh;
    this.topLayer = Server.surfaceMesh;
    
    int minSize = Zones.worldTileSizeX / 15;
    for (int i = 800; i >= minSize; i--) {
      for (int j = 0; j < 2; j++)
      {
        int width = i;
        int height = width;
        int x = this.random.nextInt(Zones.worldTileSizeX - width - 128) + 64;
        int y = this.random.nextInt(Zones.worldTileSizeY - width - 128) + 64;
        if (isIslandOk(x, y, x + width, y + height))
        {
          this.startX = (x + width / 2);
          this.startY = (y + width / 2);
          this.length = (width / 2);
          this.radius = (height / 2);
          logger.info("Found island location " + i + " @ " + (x + width / 2) + ", " + (y + height / 2));
          return true;
        }
      }
    }
    return false;
  }
  
  private void island()
  {
    Map<Integer, Set<Integer>> changes = null;
    changes = addIsland(this.startX - this.length, this.startX + this.length, this.startY - this.radius, this.startY + this.radius);
    if (changes != null) {
      for (Map.Entry<Integer, Set<Integer>> me : changes.entrySet())
      {
        x = (Integer)me.getKey();
        Set<Integer> set = (Set)me.getValue();
        for (Integer y : set)
        {
          changeTile(x.intValue(), y.intValue());
          Players.getInstance().sendChangedTile(x.intValue(), y.intValue(), true, true);
          destroyStructures(x.intValue(), y.intValue());
        }
      }
    }
    Integer x;
  }
  
  public Map<Integer, Set<Integer>> addToChanges(Map<Integer, Set<Integer>> changes, int x, int y)
  {
    Set<Integer> s = (Set)changes.get(Integer.valueOf(x));
    if (s == null) {
      s = new HashSet();
    }
    if (!s.contains(Integer.valueOf(y))) {
      s.add(Integer.valueOf(y));
    }
    changes.put(Integer.valueOf(x), s);
    return changes;
  }
  
  public final boolean isIslandOk(int x0, int y0, int x1, int y1)
  {
    int xm = (x1 + x0) / 2;
    int ym = (y1 + y0) / 2;
    for (int x = x0; x < x1; x++)
    {
      double xd = (x - xm) * 2.0D / (x1 - x0);
      for (int y = y0; y < y1; y++)
      {
        double yd = (y - ym) * 2.0D / (y1 - y0);
        double d = Math.sqrt(xd * xd + yd * yd);
        if (d < 1.0D)
        {
          int height = Tiles.decodeHeight(this.topLayer.data[(x | y << this.topLayer.getSizeLevel())]);
          if (height > -2) {
            return false;
          }
        }
      }
    }
    return true;
  }
  
  public Map<Integer, Set<Integer>> addIsland(int x0, int y0, int x1, int y1)
  {
    int xm = (x1 + x0) / 2;
    int ym = (y1 + y0) / 2;
    double dirOffs = this.random.nextDouble() * 3.141592653589793D * 2.0D;
    
    int branchCount = this.random.nextInt(7) + 3;
    
    Map<Integer, Set<Integer>> changes = new HashMap();
    float[] branches = new float[branchCount];
    for (int i = 0; i < branchCount; i++) {
      branches[i] = (this.random.nextFloat() * 0.25F + 0.75F);
    }
    ImprovedNoise noise = new ImprovedNoise(this.random.nextLong());
    for (int x = x0; x < x1; x++)
    {
      double xd = (x - xm) * 2.0D / (x1 - x0);
      for (int y = y0; y < y1; y++)
      {
        double yd = (y - ym) * 2.0D / (y1 - y0);
        double od = Math.sqrt(xd * xd + yd * yd);
        
        double dir = (Math.atan2(yd, xd) + 3.141592653589793D) / 6.283185307179586D + dirOffs;
        while (dir < 0.0D) {
          dir += 1.0D;
        }
        while (dir >= 1.0D) {
          dir -= 1.0D;
        }
        int branch = (int)(dir * branchCount);
        float step = (float)dir * branchCount - branch;
        float last = branches[branch];
        float nextBranch = branches[((branch + 1) % branchCount)];
        
        float pow = last + (nextBranch - last) * step;
        double d = od;
        d /= pow;
        if (d < 1.0D)
        {
          d *= d;
          d *= d;
          d = 1.0D - d;
          
          int height = Tiles.decodeHeight(this.topLayer.data[(x | y << this.topLayer.getSizeLevel())]);
          float n = (float)(noise.perlinNoise(x, y) * 64.0D) + 100.0F;
          n *= 2.0F;
          int hh = (int)(height + (n - height) * d);
          
          byte type = Tiles.Tile.TILE_DIRT.id;
          if (hh > 5) {
            if (this.random.nextInt(100) == 0) {
              type = Tiles.Tile.TILE_GRASS.id;
            }
          }
          if (hh > 0) {
            hh = (int)(hh + 0.07F);
          } else {
            hh = (int)(hh - 0.07F);
          }
          this.topLayer.data[(x | y << this.topLayer.getSizeLevel())] = Tiles.encode((short)hh, type, 0);
          changes = addToChanges(changes, x, y);
        }
      }
    }
    for (int x = x0; x < x1; x++)
    {
      double xd = (x - xm) * 2.0D / (x1 - x0);
      for (int y = y0; y < y1; y++)
      {
        double yd = (y - ym) * 2.0D / (y1 - y0);
        double d = Math.sqrt(xd * xd + yd * yd);
        double od = d * (x1 - x0);
        
        double dir = (Math.atan2(yd, xd) + 3.141592653589793D) / 6.283185307179586D + dirOffs;
        while (dir < 0.0D) {
          dir += 1.0D;
        }
        while (dir >= 1.0D) {
          dir -= 1.0D;
        }
        int branch = (int)(dir * branchCount);
        float step = (float)dir * branchCount - branch;
        float last = branches[branch];
        float nextBranch = branches[((branch + 1) % branchCount)];
        
        float pow = last + (nextBranch - last) * step;
        d /= pow;
        
        int height = Tiles.decodeHeight(this.topLayer.data[(x | y << this.topLayer.getSizeLevel())]);
        int dd = this.rockLayer.data[(x | y << this.topLayer.getSizeLevel())];
        
        float hh = height / 10.0F - 8.0F;
        
        d = 1.0D - d;
        if (d < 0.0D) {
          d = 0.0D;
        }
        d = Math.sin(d * 3.141592653589793D) * 2.0D - 1.0D;
        if (d < 0.0D) {
          d = 0.0D;
        }
        float n = (float)noise.perlinNoise(x / 2.0D, y / 2.0D);
        if (n > 0.5F) {
          n -= (n - 0.5F) * 2.0F;
        }
        n /= 0.5F;
        if (n < 0.0F) {
          n = 0.0F;
        }
        hh = (float)(hh + n * (x1 - x0) / 8.0F * d);
        
        this.rockLayer.data[(x | y << this.topLayer.getSizeLevel())] = Tiles.encode(hh, Tiles.decodeType(dd), 
          Tiles.decodeData(dd));
        changes = addToChanges(changes, x, y);
        float ddd = (float)od / 16.0F;
        if (ddd < 1.0F)
        {
          ddd = ddd * 2.0F - 1.0F;
          if (ddd > 1.0F) {
            ddd = 1.0F;
          }
          if (ddd < 0.0F) {
            ddd = 0.0F;
          }
          dd = this.topLayer.data[(x | y << this.topLayer.getSizeLevel())];
          float hh1 = Tiles.decodeHeightAsFloat(this.topLayer.data[(x | y << this.topLayer.getSizeLevel())]);
          hh = Tiles.decodeHeightAsFloat(this.rockLayer.data[(x | y << this.topLayer.getSizeLevel())]);
          
          hh += (hh1 - hh) * ddd;
          this.topLayer.data[(x | y << this.topLayer.getSizeLevel())] = Tiles.encode(hh, Tiles.decodeType(dd), 
            Tiles.decodeData(dd));
          changes = addToChanges(changes, x, y);
        }
        else
        {
          dd = this.topLayer.data[(x | y << this.topLayer.getSizeLevel())];
          hh = Tiles.decodeHeightAsFloat(this.topLayer.data[(x | y << this.topLayer.getSizeLevel())]);
          hh = hh * 0.5F + (int)hh / 2 * 2 * 0.5F;
          if (hh > 0.0F) {
            hh += 0.07F;
          } else {
            hh -= 0.07F;
          }
          this.topLayer.data[(x | y << this.topLayer.getSizeLevel())] = Tiles.encode(hh, Tiles.decodeType(dd), 
            Tiles.decodeData(dd));
          changes = addToChanges(changes, x, y);
        }
      }
    }
    for (int x = x0; x < x1; x++)
    {
      double xd = (x - xm) * 2.0D / (x1 - x0);
      for (int y = y0; y < y1; y++)
      {
        double yd = (y - ym) * 2.0D / (y1 - y0);
        double d = Math.sqrt(xd * xd + yd * yd);
        double od = d * (x1 - x0);
        boolean rock = true;
        for (int xx = 0; xx < 2; xx++) {
          for (int yy = 0; yy < 2; yy++)
          {
            int height = Tiles.decodeHeight(this.topLayer.data[(x | y << this.topLayer.getSizeLevel())]);
            int groundHeight = Tiles.decodeHeight(this.rockLayer.data[(x | y << this.topLayer.getSizeLevel())]);
            if (groundHeight < height)
            {
              rock = false;
            }
            else
            {
              int dd = this.topLayer.data[(x | y << this.topLayer.getSizeLevel())];
              this.topLayer.data[(x | y << this.topLayer.getSizeLevel())] = Tiles.encode((short)groundHeight, 
                Tiles.decodeType(dd), Tiles.decodeData(dd));
              changes = addToChanges(changes, x, y);
            }
          }
        }
        if (rock)
        {
          float ddd = (float)od / 16.0F;
          if (ddd < 1.0F)
          {
            int dd = this.topLayer.data[(x | y << this.topLayer.getSizeLevel())];
            this.topLayer.data[(x | y << this.topLayer.getSizeLevel())] = Tiles.encode(Tiles.decodeHeight(dd), Tiles.Tile.TILE_LAVA.id, -1);
            
            changes = addToChanges(changes, x, y);
          }
          else
          {
            int dd = this.topLayer.data[(x | y << this.topLayer.getSizeLevel())];
            this.topLayer.data[(x | y << this.topLayer.getSizeLevel())] = Tiles.encode(Tiles.decodeHeight(dd), Tiles.Tile.TILE_ROCK.id, 0);
            
            changes = addToChanges(changes, x, y);
          }
        }
      }
    }
    return changes;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\behaviours\TerraformingTask.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */