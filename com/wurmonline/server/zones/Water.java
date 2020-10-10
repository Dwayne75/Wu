package com.wurmonline.server.zones;

import com.wurmonline.mesh.MeshIO;
import com.wurmonline.mesh.Tiles;
import com.wurmonline.mesh.Tiles.Tile;
import com.wurmonline.server.Constants;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.Players;
import com.wurmonline.server.Server;
import com.wurmonline.server.ServerDirInfo;
import com.wurmonline.server.ServerEntry;
import com.wurmonline.server.Servers;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.weather.Weather;
import java.io.IOException;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class Water
  extends Thread
  implements MiscConstants
{
  public boolean shouldStop = false;
  private static final int ROW_COUNT = 64;
  private static final Logger logger = Logger.getLogger(Water.class.getName());
  private static MeshIO waterMesh;
  static final int[][] heightsArr = new int[1 << Constants.meshSize][1 << Constants.meshSize];
  static final float[][] addedWaterArr = new float[1 << Constants.meshSize][1 << Constants.meshSize];
  static final float[][] currentWaterArr = new float[1 << Constants.meshSize][1 << Constants.meshSize];
  static final float[][] reservoirArr = new float[1 << Constants.meshSize][1 << Constants.meshSize];
  private static final int INIT = 0;
  private static final int FLOW = 1;
  private static final int ADD_WATER = 2;
  private static final int EVAPORATE = 3;
  private static final int UPDATE = 4;
  private static int phase = 0;
  private static final long SPRINGRAND = Servers.localServer.id + 127312634L;
  private final Random waterRand = new Random();
  private final ConcurrentHashMap<Long, WaterGenerator> springs = new ConcurrentHashMap();
  public static final int MAX_WATERLEVEL_DECI = 10;
  public static final int MAX_WATERLEVEL_CM = 100;
  private final ConcurrentHashMap<Long, WaterGenerator> changedTileCorners = new ConcurrentHashMap();
  private int rowsPerRun = Math.max(1, Zones.worldTileSizeY / 64);
  private final int sleepTime = 15;
  private float nowRain = 0.0F;
  
  public Water()
  {
    super("Water-Thread");
  }
  
  public static final void loadWaterMesh()
  {
    long start = System.nanoTime();
    try
    {
      waterMesh = MeshIO.open(ServerDirInfo.getFileDBPath() + "water.map");
    }
    catch (IOException iex)
    {
      float lElapsedTime;
      logger.log(Level.INFO, "water mesh doesn't exist.. creating..");
      int[] waterArr = new int[(1 << Constants.meshSize) * (1 << Constants.meshSize)];
      for (int x = 0; x < (1 << Constants.meshSize) * (1 << Constants.meshSize); x++) {
        waterArr[x] = 0;
      }
      try
      {
        waterMesh = MeshIO.createMap(ServerDirInfo.getFileDBPath() + "water.map", Constants.meshSize, waterArr);
      }
      catch (IOException iox)
      {
        logger.log(Level.SEVERE, "Failed to create water mesh. Exiting.", iox);
        System.exit(0);
      }
    }
    finally
    {
      float lElapsedTime;
      float lElapsedTime = (float)(System.nanoTime() - start) / 1000000.0F;
      logger.info("Loading water mesh, size: " + waterMesh.getSize() + " took " + lElapsedTime + " ms");
    }
  }
  
  public final void loadSprings()
  {
    int numSprings = (1 << Constants.meshSize) / 64;
    logger.log(Level.INFO, "NUMBER OF SPRINGS=" + numSprings);
    Random wrand = new Random(SPRINGRAND);
    for (int x = 0; x < numSprings; x++)
    {
      int tx = wrand.nextInt(Zones.worldTileSizeX);
      int ty = wrand.nextInt(Zones.worldTileSizeY);
      if (isAboveWater(tx, ty))
      {
        int th = 25 + wrand.nextInt(25);
        WaterGenerator wg = new WaterGenerator(tx, ty, true, 0, th);
        this.springs.put(Long.valueOf(wg.getTileId()), wg);
        logger.log(Level.INFO, "Spring at " + tx + "," + ty + " =" + th);
      }
    }
    logger.log(Level.INFO, "NUMBER OF ACTIVE SPRINGS=" + this.springs.size());
    for (WaterGenerator generator : this.springs.values()) {
      generator.updateItem();
    }
  }
  
  public static int getCaveWater(int tilex, int tiley)
  {
    int value = waterMesh.getTile(tilex, tiley);
    int toReturn = value >> 16 & 0xFFFF;
    return toReturn;
  }
  
  public static void setCaveWater(int tilex, int tiley, int newValue)
  {
    int value = waterMesh.getTile(tilex, tiley);
    if ((value >> 16 & 0xFFFF) != newValue) {
      waterMesh.setTile(tilex, tiley, ((Math.max(0, newValue) & 0xFFFF) << 16) + (value & 0xFFFF));
    }
  }
  
  public static int getSurfaceWater(int tilex, int tiley)
  {
    int value = waterMesh.getTile(tilex, tiley);
    int toReturn = value & 0xFFFF;
    return toReturn;
  }
  
  public static void setSurfaceWater(int tilex, int tiley, int newValue)
  {
    int value = waterMesh.getTile(tilex, tiley);
    if ((value & 0xFFFF) != newValue) {
      waterMesh.setTile(tilex, tiley, (value & 0xFFFF0000) + (
        Math.max(0, newValue) & 0xFFFF));
    }
  }
  
  private static float getWaterLevel(int tilex, int tiley)
  {
    return currentWaterArr[tilex][tiley];
  }
  
  private static void setWaterLevel(int tilex, int tiley, float newValue)
  {
    currentWaterArr[tilex][tiley] = newValue;
  }
  
  private static float getAddedWater(int tilex, int tiley)
  {
    return addedWaterArr[tilex][tiley];
  }
  
  private static void clrAddedWater(int tilex, int tiley)
  {
    addedWaterArr[tilex][tiley] = 0.0F;
  }
  
  private static void incAddedWater(int tilex, int tiley, float newValue)
  {
    addedWaterArr[tilex][tiley] += newValue;
  }
  
  private static float getReservoir(int tilex, int tiley)
  {
    return reservoirArr[tilex][tiley];
  }
  
  private static void setReservoir(int tilex, int tiley, float newValue)
  {
    reservoirArr[tilex][tiley] = newValue;
  }
  
  private static int getHeightCm(int tilex, int tiley)
  {
    return heightsArr[tilex][tiley];
  }
  
  private static void setHeightDm(int tilex, int tiley, int newValue)
  {
    heightsArr[tilex][tiley] = (newValue * 10);
  }
  
  public final boolean isAboveWater(int x, int y)
  {
    return Tiles.decodeHeight(Server.surfaceMesh.getTile(x, y)) > 3;
  }
  
  public void run()
  {
    logger.log(Level.INFO, "WATER ROWS PER RUN=" + this.rowsPerRun + " SLEEPTIME=" + 15 + " SHOULD STOP=" + this.shouldStop);
    for (int y = 0; y < 1 << Constants.meshSize; y++) {
      for (int x = 0; x < 1 << Constants.meshSize; x++) {
        setWaterLevel(x, y, getSurfaceWater(x, y) * 10);
      }
    }
    int currY = 0;
    float maxRain = 0.0F;
    while (!this.shouldStop)
    {
      int xTiles = Zones.worldTileSizeX;
      try
      {
        sleep(15L);
        float rain = Server.getWeather().getRain();
        float evaporation = Server.getWeather().getEvaporationRate();
        for (int y = 0; y < this.rowsPerRun; y++)
        {
          for (int x = 0; x < xTiles; x++) {
            if (isAboveWater(x, currY)) {
              if ((x > 0) && (x < Zones.worldTileSizeX) && (currY > 0) && (currY < Zones.worldTileSizeY)) {
                switch (phase)
                {
                case 0: 
                  int encodedTile = Server.surfaceMesh.getTile(x, currY);
                  setHeightDm(x, currY, Tiles.decodeHeight(encodedTile));
                  clrAddedWater(x, currY);
                  break;
                case 1: 
                  doFlow(x, currY);
                  break;
                case 2: 
                  addWater(x, currY, rain);
                  break;
                case 3: 
                  doEvaporation(x, currY, evaporation);
                  break;
                case 4: 
                  float newWaterLevel = getWaterLevel(x, currY);
                  
                  int waterLevel = (int)(newWaterLevel / 10.0F);
                  setSurfaceWater(x, currY, waterLevel);
                  WaterGenerator wg = WaterGenerator.getWG(x, currY);
                  if (wg != null) {
                    wg.setHeight(waterLevel);
                  } else {
                    wg = new WaterGenerator(x, currY, 0, waterLevel);
                  }
                  addWater(wg);
                }
              }
            }
          }
          currY++;
          if (currY >= Zones.worldTileSizeY) {
            currY = 0;
          }
        }
        if (currY == 0)
        {
          phase += 1;
          if (phase > 4) {
            phase = 0;
          }
        }
      }
      catch (InterruptedException localInterruptedException) {}
      try
      {
        waterMesh.saveNextDirtyRow();
      }
      catch (IOException ex)
      {
        this.shouldStop = true;
      }
      if ((int)(this.nowRain * 100.0F) > (int)(maxRain * 100.0F)) {
        maxRain = this.nowRain;
      }
    }
    logger.info("Water mesh thread has finished");
    try
    {
      waterMesh.saveAll();
      waterMesh.close();
    }
    catch (IOException iox)
    {
      logger.log(Level.WARNING, "Failed to save watermesh!", iox);
    }
  }
  
  private final void doFlow(int x, int y)
  {
    float[][] moveArr = new float[3][3];
    for (int xx = -1; xx <= 1; xx++) {
      for (int yy = -1; yy <= 1; yy++) {
        moveArr[(xx + 1)][(yy + 1)] = 0.0F;
      }
    }
    float amountToFlow = 1.0F;
    int lowerCount = 1;
    while (lowerCount > 0)
    {
      lowerCount = 0;
      float lowDiffs = 0.0F;
      float nwl = getWaterLevel(x, y) + moveArr[1][1];
      float wht = getHeightCm(x, y) + nwl;
      boolean ok = nwl > 0.1F;
      if (ok) {
        for (int xx = -1; xx <= 1; xx++) {
          for (int yy = -1; yy <= 1; yy++) {
            if ((xx != 0) || (yy != 0))
            {
              float cht = getHeightCm(x + xx, y + yy) + getWaterLevel(x + xx, y + yy) + moveArr[(xx + 1)][(yy + 1)];
              
              float diff = wht - cht;
              if (diff > 0.0F)
              {
                lowerCount++;
                lowDiffs += diff;
              }
            }
          }
        }
      }
      float toMovePerCm = 0.0F;
      if ((lowerCount > 0) && (lowDiffs > 0.0F))
      {
        toMovePerCm = amountToFlow / lowDiffs;
        for (int xx = -1; xx <= 1; xx++) {
          for (int yy = -1; yy <= 1; yy++) {
            if ((xx != 0) || (yy != 0))
            {
              float cht = getHeightCm(x + xx, y + yy) + getWaterLevel(x + xx, y + yy) + moveArr[(xx + 1)][(yy + 1)];
              
              float diff = wht - cht;
              if ((ok) && (diff > 0.0F))
              {
                float toMove = toMovePerCm * diff;
                moveArr[(xx + 1)][(yy + 1)] += toMove;
                moveArr[1][1] -= toMove;
                incAddedWater(x + xx, y + yy, toMove);
                incAddedWater(x, y, -toMove);
              }
            }
          }
        }
      }
    }
  }
  
  private final void addWater(int x, int y, float rain)
  {
    if (rain > 0.0F)
    {
      if (rain > this.nowRain) {
        this.nowRain = rain;
      }
      incAddedWater(x, y, rain);
    }
    WaterGenerator sp = (WaterGenerator)this.springs.get(Long.valueOf(Tiles.getTileId(x, y, 0, true)));
    if (sp != null) {
      incAddedWater(x, y, sp.getHeight() + this.waterRand.nextInt(25));
    }
  }
  
  private float getMaxWaterLeakage(byte code)
  {
    switch (code)
    {
    case 0: 
      return 0.1F;
    case 1: 
      return 0.2F;
    case 2: 
      return 0.5F;
    }
    return 0.0F;
  }
  
  private float getMaxWaterInfiltration(byte code)
  {
    switch (code)
    {
    case 0: 
      return 0.0F;
    case 1: 
      return 0.25F;
    case 2: 
      return 0.5F;
    case 3: 
      return 1.0F;
    }
    return 0.0F;
  }
  
  private float getMaxWaterReservoir(byte code)
  {
    switch (code)
    {
    case 0: 
      return 3.0F;
    case 1: 
      return 7.0F;
    case 2: 
      return 15.0F;
    }
    return 0.0F;
  }
  
  private void doEvaporation(int x, int y, float evaporation)
  {
    float newWaterLevel = Math.max(0.0F, getWaterLevel(x, y) + getAddedWater(x, y));
    
    int encodedTile = Server.surfaceMesh.getTile(x, y);
    byte type = Tiles.decodeType(encodedTile);
    Tiles.Tile tile = Tiles.getTile(type);
    float maxWaterLeakage = getMaxWaterLeakage(tile.getWaterLeakageCode());
    float maxWaterInfiltration = getMaxWaterInfiltration(tile.getWaterInfiltrationCode());
    float maxWaterReservoir = getMaxWaterReservoir(tile.getWaterReservoirCode());
    
    float currentReservoir = getReservoir(x, y);
    if ((maxWaterLeakage > 0.0F) && (currentReservoir > 0.0F)) {
      currentReservoir = Math.max(0.0F, currentReservoir - maxWaterLeakage);
    }
    float tempNewWaterLevel = getWaterLevel(x, y) + getAddedWater(x, y);
    if ((maxWaterInfiltration > 0.0F) && (tempNewWaterLevel > 0.0F))
    {
      float room = maxWaterReservoir - currentReservoir;
      float toMove = Math.min(Math.min(room, maxWaterInfiltration), tempNewWaterLevel);
      currentReservoir += toMove;
      incAddedWater(x, y, -toMove);
    }
    setReservoir(x, y, currentReservoir);
    setWaterLevel(x, y, newWaterLevel);
  }
  
  public final void addWater(WaterGenerator addedChange)
  {
    if (addedChange.changed()) {
      this.changedTileCorners.put(Long.valueOf(addedChange.getTileId()), addedChange);
    }
  }
  
  public final void propagateChanges()
  {
    for (WaterGenerator generator : this.changedTileCorners.values())
    {
      if (generator.changedSinceReset()) {
        for (Player player : Players.getInstance().getPlayers()) {
          if (player.isWithinTileDistanceTo(generator.x, generator.y, 0, 20)) {
            player.getCommunicator().sendWater(generator.x, generator.y, generator.layer, generator.getHeight());
          }
        }
      }
      generator.setReset(true);
    }
    this.changedTileCorners.clear();
  }
  
  private final void tickGenerators()
  {
    for (WaterGenerator spring : this.springs.values())
    {
      int oldSurfaceVal = getSurfaceWater(spring.x, spring.y);
      if (oldSurfaceVal < 200)
      {
        this.waterRand.setSeed(spring.x + spring.y);
        
        setSurfaceWater(spring.x, spring.y, oldSurfaceVal + 1 + this.waterRand.nextInt(3));
        spring.setHeight(getSurfaceWater(spring.x, spring.y));
        
        addWater(spring);
      }
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\zones\Water.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */