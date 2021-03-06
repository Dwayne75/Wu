package com.wurmonline.server.behaviours;

import com.wurmonline.mesh.MeshIO;
import com.wurmonline.mesh.Tiles;
import com.wurmonline.mesh.Tiles.Tile;
import com.wurmonline.server.Constants;
import com.wurmonline.server.FailedException;
import com.wurmonline.server.GeneralUtilities;
import com.wurmonline.server.Items;
import com.wurmonline.server.LoginHandler;
import com.wurmonline.server.MeshTile;
import com.wurmonline.server.Players;
import com.wurmonline.server.Point;
import com.wurmonline.server.Server;
import com.wurmonline.server.ServerEntry;
import com.wurmonline.server.Servers;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.CreatureStatus;
import com.wurmonline.server.creatures.MovementScheme;
import com.wurmonline.server.deities.Deity;
import com.wurmonline.server.highways.HighwayPos;
import com.wurmonline.server.highways.MethodsHighways;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemFactory;
import com.wurmonline.server.items.ItemSpellEffects;
import com.wurmonline.server.items.ItemTemplate;
import com.wurmonline.server.items.ItemTemplateFactory;
import com.wurmonline.server.items.NoSuchTemplateException;
import com.wurmonline.server.items.RuneUtilities.ModifierEffect;
import com.wurmonline.server.kingdom.Kingdoms;
import com.wurmonline.server.players.Cultist;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.skills.NoSuchSkillException;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.skills.Skills;
import com.wurmonline.server.sounds.SoundPlayer;
import com.wurmonline.server.structures.BridgePart;
import com.wurmonline.server.structures.Fence;
import com.wurmonline.server.structures.Structure;
import com.wurmonline.server.utils.logging.TileEvent;
import com.wurmonline.server.villages.NoSuchVillageException;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.villages.Villages;
import com.wurmonline.server.weather.Weather;
import com.wurmonline.server.zones.FaithZone;
import com.wurmonline.server.zones.NoSuchZoneException;
import com.wurmonline.server.zones.Trap;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zone;
import com.wurmonline.server.zones.Zones;
import com.wurmonline.shared.constants.BridgeConstants.BridgeType;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;

public final class TileRockBehaviour
  extends TileBehaviour
{
  private static final Logger logger = Logger.getLogger(TileRockBehaviour.class.getName());
  static final Random rockRandom = new Random();
  private static final int worldSizeX = 1 << Constants.meshSize;
  private static final int mineZoneSize = 32;
  private static final int mineZoneDiv = worldSizeX / 32;
  private static final int minPrayingHeightDec = 400;
  private static final byte[][] minezones = new byte[mineZoneDiv + 1][mineZoneDiv + 1];
  static final long HUGE_PRIME = 789221L;
  static final long PROSPECT_PRIME = 181081L;
  public static final long SALT_PRIME = 102533L;
  public static final long SANDSTONE_PRIME = 123307L;
  static long SOURCE_PRIME = 786431L + Server.rand.nextInt(10000);
  public static final int saltFactor = 100;
  public static final int sandstoneFactor = 64;
  static final int flintFactor = 200;
  static int sourceFactor = 1000;
  static final long FLINT_PRIME = 6883L;
  static final int MIN_QL = 20;
  static int MAX_QL = 100;
  static final int MAX_ROCK_QL = 100;
  static final long EMERALD_PRIME = 66083L;
  static final long OPAL_PRIME = 101333L;
  static final long RUBY_PRIME = 812341L;
  static final long DIAMOND_PRIME = 104711L;
  static final long SAPPHIRE_PRIME = 781661L;
  private static final short CAVE_DESCENT_RATE = 20;
  static final int MAX_CEIL = 255;
  static final int DIG_CEIL = 30;
  public static final int MIN_CEIL = 5;
  static final int DIG_CEIL_REACH = 60;
  static final short MIN_CAVE_FLOOR = -25;
  static final short MAX_SLOPE_DOWN = -40;
  static final short MIN_ROCK_UNDERWATER = -25;
  public static final short CAVE_INIT_HEIGHT = -100;
  private static final int ORE_ZONE_FACTOR = 4;
  private static int oreRand = 0;
  
  static
  {
    Random prand = new Random();
    prand.setSeed(181081L + Servers.getLocalServerId());
    Server.rand.setSeed(789221L);
    for (int x = 0; x <= mineZoneDiv; x++) {
      for (int y = 0; y <= mineZoneDiv; y++)
      {
        int num = Server.rand.nextInt(75);
        int prandnum = prand.nextInt(4);
        if (prandnum == 0) {
          minezones[x][y] = getOreId(num);
        } else {
          minezones[x][y] = Tiles.Tile.TILE_CAVE_WALL.id;
        }
      }
    }
  }
  
  TileRockBehaviour()
  {
    super((short)9);
    
    sourceFactor = Servers.isThisAHomeServer() ? 100 : 50;
  }
  
  TileRockBehaviour(short type)
  {
    super(type);
  }
  
  public List<ActionEntry> getBehavioursFor(Creature performer, int tilex, int tiley, boolean onSurface, int tile)
  {
    List<ActionEntry> toReturn = new LinkedList();
    toReturn.addAll(super.getBehavioursFor(performer, tilex, tiley, onSurface, tile));
    if (Tiles.decodeHeight(tile) > 400) {
      if ((performer.getDeity() != null) && (performer.getDeity().isMountainGod())) {
        Methods.addActionIfAbsent(toReturn, Actions.actionEntrys['']);
      }
    }
    if ((performer.getCultist() != null) && (performer.getCultist().maySpawnVolcano()))
    {
      HighwayPos highwaypos = MethodsHighways.getHighwayPos(tilex, tiley, onSurface);
      if ((highwaypos == null) || (!MethodsHighways.onHighway(highwaypos))) {
        toReturn.add(new ActionEntry((short)78, "Erupt", "erupting"));
      }
    }
    toReturn.add(Actions.actionEntrys['ʂ']);
    return toReturn;
  }
  
  public List<ActionEntry> getBehavioursFor(Creature performer, Item subject, int tilex, int tiley, boolean onSurface, int tile)
  {
    List<ActionEntry> toReturn = new LinkedList();
    toReturn.addAll(super.getBehavioursFor(performer, subject, tilex, tiley, onSurface, tile));
    if (subject.isMiningtool())
    {
      toReturn.add(new ActionEntry((short)-3, "Mining", "Mining options"));
      
      toReturn.add(Actions.actionEntrys['']);
      
      toReturn.add(Actions.actionEntrys['']);
      
      toReturn.add(Actions.actionEntrys['ã']);
      if ((performer.getPower() >= 4) && (subject.getTemplateId() == 176)) {
        toReturn.add(Actions.actionEntrys['Ȇ']);
      }
    }
    else if (subject.getTemplateId() == 782)
    {
      toReturn.add(Actions.actionEntrys['Ȇ']);
    }
    if (Tiles.decodeHeight(tile) > 400) {
      if ((performer.getDeity() != null) && (performer.getDeity().isMountainGod())) {
        Methods.addActionIfAbsent(toReturn, Actions.actionEntrys['']);
      }
    }
    if (((performer.getCultist() != null) && (performer.getCultist().maySpawnVolcano())) || (
      (subject.getTemplateId() == 176) && (performer.getPower() >= 5)))
    {
      HighwayPos highwaypos = MethodsHighways.getHighwayPos(tilex, tiley, onSurface);
      if ((highwaypos == null) || (!MethodsHighways.onHighway(highwaypos))) {
        toReturn.add(new ActionEntry((short)78, "Erupt", "erupting"));
      }
    }
    toReturn.add(Actions.actionEntrys['ʂ']);
    return toReturn;
  }
  
  public boolean action(Action act, Creature performer, int tilex, int tiley, boolean onSurface, int tile, short action, float counter)
  {
    boolean done = true;
    if (action == 1)
    {
      Communicator comm = performer.getCommunicator();
      comm.sendNormalServerMessage("You see hard rock.");
      sendVillageString(performer, tilex, tiley, true);
      Trap t = Trap.getTrap(tilex, tiley, performer.getLayer());
      if (performer.getPower() > 3)
      {
        comm.sendNormalServerMessage("Your rot: " + Creature.normalizeAngle(performer.getStatus().getRotation()) + ", Wind rot=" + 
          Server.getWeather().getWindRotation() + ", pow=" + Server.getWeather().getWindPower() + " x=" + 
          Server.getWeather().getXWind() + ", y=" + Server.getWeather().getYWind());
        comm.sendNormalServerMessage("Tile is spring=" + Zone.hasSpring(tilex, tiley));
        if (performer.getPower() >= 5) {
          comm.sendNormalServerMessage("tilex: " + tilex + ", tiley=" + tiley);
        }
        if (t != null)
        {
          String villageName = "none";
          if (t.getVillage() > 0) {
            try
            {
              villageName = Villages.getVillage(t.getVillage()).getName();
            }
            catch (NoSuchVillageException localNoSuchVillageException) {}
          }
          comm.sendNormalServerMessage("A " + t.getName() + ", ql=" + t.getQualityLevel() + " kingdom=" + 
            Kingdoms.getNameFor(t.getKingdom()) + ", vill=" + villageName + ", rotdam=" + t.getRotDamage() + " firedam=" + t
            .getFireDamage() + " speed=" + t.getSpeedBon());
        }
      }
      else if (t != null)
      {
        if ((t.getKingdom() == performer.getKingdomId()) || (performer.getDetectDangerBonus() > 0.0F))
        {
          String qlString = "average";
          if (t.getQualityLevel() < 20) {
            qlString = "low";
          } else if (t.getQualityLevel() > 80) {
            qlString = "deadly";
          } else if (t.getQualityLevel() > 50) {
            qlString = "high";
          }
          String villageName = ".";
          if (t.getVillage() > 0) {
            try
            {
              villageName = " of " + Villages.getVillage(t.getVillage()).getName() + ".";
            }
            catch (NoSuchVillageException localNoSuchVillageException1) {}
          }
          String rotDam = "";
          if (t.getRotDamage() > 0) {
            rotDam = " It has ugly black-green speckles.";
          }
          String fireDam = "";
          if (t.getFireDamage() > 0) {
            fireDam = " It has the rune of fire.";
          }
          StringBuilder buf = new StringBuilder();
          buf.append("You detect a ");
          buf.append(t.getName());
          buf.append(" here, of ");
          buf.append(qlString);
          buf.append(" quality.");
          buf.append(" It has been set by people from ");
          buf.append(Kingdoms.getNameFor(t.getKingdom()));
          buf.append(villageName);
          buf.append(rotDam);
          buf.append(fireDam);
          comm.sendNormalServerMessage(buf.toString());
        }
      }
    }
    else if (action == 141)
    {
      if ((Tiles.decodeHeight(tile) > 400) && 
        (performer.getDeity() != null) && (performer.getDeity().isMountainGod())) {
        done = MethodsReligion.pray(act, performer, counter);
      }
    }
    else if (action == 78)
    {
      HighwayPos highwaypos = MethodsHighways.getHighwayPos(tilex, tiley, onSurface);
      if ((highwaypos != null) && (MethodsHighways.onHighway(highwaypos))) {
        return true;
      }
      boolean cultistSpawn = (Methods.isActionAllowed(performer, (short)384)) && (performer.getCultist() != null) && (performer.getCultist().maySpawnVolcano());
      if ((cultistSpawn) || (performer.getPower() >= 5))
      {
        if (cultistSpawn)
        {
          if (isHoleNear(tilex, tiley))
          {
            performer.getCommunicator().sendNormalServerMessage("A cave entrance is too close.");
            return true;
          }
          if (Zones.getKingdom(tilex, tiley) != performer.getKingdomId())
          {
            performer.getCommunicator().sendNormalServerMessage("Nothing happens. Maybe you can not spawn lava too far from your own kingdom?");
            
            return true;
          }
          try
          {
            FaithZone fz = Zones.getFaithZone(tilex, tiley, performer.isOnSurface());
            if ((fz != null) && (fz.getCurrentRuler() != null) && (fz.getCurrentRuler().number != 2))
            {
              performer.getCommunicator().sendNormalServerMessage("Nothing happens. Maybe you can not spawn lava too far from Magranon's domain?");
              
              return true;
            }
          }
          catch (NoSuchZoneException nsz)
          {
            performer.getCommunicator().sendNormalServerMessage("Nothing happens. Maybe you can not spawn lava too far from Magranon's domain?");
            
            return true;
          }
          if (!Methods.isActionAllowed(performer, (short)547, tilex, tiley)) {
            return true;
          }
          done = false;
          if (counter == 1.0F)
          {
            int sx = Zones.safeTileX(tilex - 1);
            int sy = Zones.safeTileX(tiley - 1);
            int ey = Zones.safeTileX(tiley + 1);
            int ex = Zones.safeTileX(tilex + 1);
            for (int x = sx; x <= ex; x++) {
              for (int y = sy; y <= ey; y++)
              {
                VolaTile tt = Zones.getTileOrNull(x, y, onSurface);
                if (tt != null)
                {
                  Item[] its = tt.getItems();
                  for (Item i : its) {
                    if (i.isNoTake())
                    {
                      performer.getCommunicator().sendNormalServerMessage("The " + i
                        .getName() + " blocks your efforts.");
                      return true;
                    }
                  }
                }
              }
            }
            performer.getCommunicator().sendNormalServerMessage("You start concentrating on the rock.");
            Server.getInstance().broadCastAction(performer.getName() + " starts to look intensely on the rock.", performer, 5);
            if (cultistSpawn) {
              performer.sendActionControl("Erupting", true, 400);
            }
          }
        }
        if ((!cultistSpawn) || (counter > 40.0F))
        {
          done = true;
          int caveTile = Server.caveMesh.getTile(tilex, tiley);
          byte type = Tiles.decodeType(caveTile);
          if ((Tiles.isSolidCave(type)) && (!Tiles.getTile(type).isReinforcedCave()))
          {
            performer.getCommunicator().sendNormalServerMessage("The rock starts to bubble with lava.");
            Server.getInstance().broadCastAction(performer.getName() + " makes the rock boil with red hot lava.", performer, 5);
            
            int height = Tiles.decodeHeight(tile);
            
            TileEvent.log(tilex, tiley, 0, performer.getWurmId(), action);
            int nh = height + 4;
            if (cultistSpawn) {
              performer.getCultist().touchCooldown2();
            }
            Server.setSurfaceTile(tilex, tiley, (short)nh, Tiles.Tile.TILE_LAVA.id, (byte)0);
            for (int xx = 0; xx <= 1; xx++) {
              for (int yy = 0; yy <= 1; yy++) {
                try
                {
                  int tempint3 = Tiles.decodeHeight(Server.surfaceMesh.getTile(tilex + xx, tiley + yy));
                  Server.rockMesh.setTile(tilex + xx, tiley + yy, 
                    Tiles.encode((short)tempint3, Tiles.Tile.TILE_ROCK.id, (byte)0));
                }
                catch (Exception localException) {}
              }
            }
            Terraforming.setAsRock(tilex, tiley, false, true);
          }
          else
          {
            performer.getCommunicator().sendNormalServerMessage("Nothing happens.");
          }
        }
      }
    }
    else
    {
      done = super.action(act, performer, tilex, tiley, onSurface, tile, action, counter);
    }
    return done;
  }
  
  public boolean action(Action act, Creature performer, Item source, int tilex, int tiley, boolean onSurface, int heightOffset, int tile, short action, float counter)
  {
    boolean done = true;
    if ((action == 518) && ((source.getTemplateId() == 782) || (
      (performer.getPower() >= 4) && (source.getTemplateId() == 176))))
    {
      int digTileX = (int)performer.getStatus().getPositionX() + 2 >> 2;
      int digTileY = (int)performer.getStatus().getPositionY() + 2 >> 2;
      done = CaveTileBehaviour.raiseRockLevel(performer, source, digTileX, digTileY, counter, act);
    }
    else if ((source.isMiningtool()) && (action == 227))
    {
      if ((tilex < 0) || (tilex > 1 << Constants.meshSize) || (tiley < 0) || (tiley > 1 << Constants.meshSize))
      {
        performer.getCommunicator().sendNormalServerMessage("The water is too deep to mine.", (byte)3);
        return true;
      }
      if (Zones.isTileProtected(tilex, tiley))
      {
        performer.getCommunicator().sendNormalServerMessage("This tile is protected by the gods. You can not mine here.", (byte)3);
        
        return true;
      }
      short h = Tiles.decodeHeight(tile);
      if (h > -24)
      {
        boolean makingWideTunnel = false;
        if (isHoleNear(tilex, tiley)) {
          if (canHaveWideEntrance(performer, tilex, tiley))
          {
            makingWideTunnel = true;
          }
          else
          {
            performer.getCommunicator().sendNormalServerMessage("Another tunnel is too close. It would collapse.");
            return true;
          }
        }
        Point lowestCorner = findLowestCorner(performer, tilex, tiley);
        if (lowestCorner == null) {
          return true;
        }
        Point nextLowestCorner = findNextLowestCorner(performer, tilex, tiley, lowestCorner);
        if (nextLowestCorner == null) {
          return true;
        }
        Point highestCorner = findHighestCorner(tilex, tiley);
        if (highestCorner == null) {
          return false;
        }
        Point nextHighestCorner = findNextHighestCorner(tilex, tiley, highestCorner);
        if (nextHighestCorner == null) {
          return false;
        }
        if (((nextLowestCorner.getH() != lowestCorner.getH()) && 
          (isStructureNear(nextLowestCorner.getX(), nextLowestCorner.getY()))) || (
          (nextHighestCorner.getH() != highestCorner.getH()) && 
          (isStructureNear(highestCorner.getX(), highestCorner.getY()))))
        {
          performer.getCommunicator().sendNormalServerMessage("Cannot create a tunnel here as there is a structure too close.", (byte)3);
          
          return true;
        }
        for (int x = -1; x <= 1; x++) {
          for (int y = -1; y <= 1; y++)
          {
            VolaTile svt = Zones.getTileOrNull(tilex + x, tiley + y, true);
            Structure ss = svt == null ? null : svt.getStructure();
            if ((ss != null) && (ss.isTypeBridge()))
            {
              performer.getCommunicator().sendNormalServerMessage("You can't tunnel here, there is a bridge in the way.");
              return true;
            }
            VolaTile cvt = Zones.getTileOrNull(tilex + x, tiley + y, false);
            Structure cs = cvt == null ? null : cvt.getStructure();
            if ((cs != null) && (cs.isTypeBridge()))
            {
              performer.getCommunicator().sendNormalServerMessage("You can't tunnel here, there is a bridge in the way.");
              return true;
            }
          }
        }
        done = false;
        Skills skills = performer.getSkills();
        Skill mining = null;
        Skill tool = null;
        boolean insta = (performer.getPower() >= 2) && (source.isWand());
        try
        {
          mining = skills.getSkill(1008);
        }
        catch (Exception ex)
        {
          mining = skills.learn(1008, 1.0F);
        }
        try
        {
          tool = skills.getSkill(source.getPrimarySkill());
        }
        catch (Exception ex)
        {
          try
          {
            tool = skills.learn(source.getPrimarySkill(), 1.0F);
          }
          catch (NoSuchSkillException nse)
          {
            logger.log(Level.WARNING, performer.getName() + " trying to mine with an item with no primary skill: " + source
              .getName());
          }
        }
        int time = 0;
        if (counter == 1.0F)
        {
          time = Actions.getStandardActionTime(performer, mining, source, 0.0D);
          try
          {
            performer.getCurrentAction().setTimeLeft(time);
          }
          catch (NoSuchActionException nsa)
          {
            logger.log(Level.INFO, "This action does not exist?", nsa);
          }
          if (affectsHighway(tilex, tiley))
          {
            performer.getCommunicator().sendNormalServerMessage("A surface highway interferes with your tunneling operation.", (byte)3);
            
            return true;
          }
          if (!isOutInTunnelOkay(performer, tilex, tiley, makingWideTunnel)) {
            return true;
          }
          Server.getInstance().broadCastAction(performer.getName() + " starts tunneling.", performer, 5);
          performer.getCommunicator().sendNormalServerMessage("You start to tunnel.");
          performer.sendActionControl(Actions.actionEntrys['ã'].getVerbString(), true, time);
          
          source.setDamage(source.getDamage() + 0.0015F * source.getDamageModifier());
          performer.getStatus().modifyStamina(-1000.0F);
        }
        else
        {
          try
          {
            time = performer.getCurrentAction().getTimeLeft();
          }
          catch (NoSuchActionException nsa)
          {
            logger.log(Level.INFO, "This action does not exist?", nsa);
          }
          if ((counter * 10.0F <= time) && (!insta))
          {
            if ((act.currentSecond() % 5 == 0) || ((act.currentSecond() == 3) && (time < 50)))
            {
              String sstring = "sound.work.mining1";
              int x = Server.rand.nextInt(3);
              if (x == 0) {
                sstring = "sound.work.mining2";
              } else if (x == 1) {
                sstring = "sound.work.mining3";
              }
              SoundPlayer.playSound(sstring, tilex, tiley, performer.isOnSurface(), 0.0F);
              source.setDamage(source.getDamage() + 0.0015F * source.getDamageModifier());
              performer.getStatus().modifyStamina(-7000.0F);
            }
          }
          else
          {
            if (act.getRarity() != 0) {
              performer.playPersonalSound("sound.fx.drumroll");
            }
            double bonus = 0.0D;
            double power = 0.0D;
            done = true;
            int itemTemplateCreated = 146;
            float diff = 1.0F;
            int mineDir = getTunnelExit(tilex, tiley);
            if (mineDir == -1)
            {
              performer.getCommunicator().sendNormalServerMessage("The topology here makes it impossible to mine in a good way.", (byte)3);
              
              return true;
            }
            byte state = Zones.getMiningState(tilex, tiley);
            if (state == -1)
            {
              performer.getCommunicator().sendNormalServerMessage("You cannot keep mining here. The rock is unusually hard.", (byte)3);
              
              return true;
            }
            if (affectsHighway(tilex, tiley))
            {
              performer.getCommunicator().sendNormalServerMessage("A surface highway interferes with your tunneling operation.", (byte)3);
              
              return true;
            }
            if ((state >= Math.max(1, Servers.localServer.getTunnelingHits()) + Server.rand.nextInt(10)) || (insta))
            {
              int t = Server.caveMesh.getTile(tilex, tiley);
              if (Tiles.isReinforcedCave(Tiles.decodeType(t)))
              {
                performer.getCommunicator().sendNormalServerMessage("You cannot keep mining here. The rock is unusually hard.", (byte)3);
                
                return true;
              }
              Zones.deleteMiningTile(tilex, tiley);
              if (areAllTilesRockOrReinforcedRock(tilex, tiley, tile, mineDir, true, makingWideTunnel))
              {
                int drop = -20;
                if (makingWideTunnel)
                {
                  MeshTile mTileCurrent = new MeshTile(Server.surfaceMesh, tilex, tiley);
                  MeshTile mCaveCurrent = new MeshTile(Server.caveMesh, tilex, tiley);
                  
                  MeshTile mTileNorth = mTileCurrent.getNorthMeshTile();
                  if (mTileNorth.isHole())
                  {
                    MeshTile mCaveNorth = mCaveCurrent.getNorthMeshTile();
                    drop = -Math.abs(mCaveNorth.getSouthSlope());
                  }
                  MeshTile mTileWest = mTileCurrent.getWestMeshTile();
                  if (mTileWest.isHole())
                  {
                    MeshTile mCaveWest = mCaveCurrent.getWestMeshTile();
                    drop = -Math.abs(mCaveWest.getEastSlope());
                  }
                  MeshTile mTileSouth = mTileCurrent.getSouthMeshTile();
                  if (mTileSouth.isHole())
                  {
                    MeshTile mCaveSouth = mCaveCurrent.getSouthMeshTile();
                    drop = -Math.abs(mCaveSouth.getNorthSlope());
                  }
                  MeshTile mTileEast = mTileCurrent.getEastMeshTile();
                  if (mTileEast.isHole())
                  {
                    MeshTile mCaveEast = mCaveCurrent.getEastMeshTile();
                    drop = -Math.abs(mCaveEast.getWestSlope());
                  }
                }
                if (!createOutInTunnel(tilex, tiley, tile, performer, drop)) {
                  return true;
                }
              }
              else
              {
                performer.getCommunicator().sendNormalServerMessage("The ground sounds strangely hollow and brittle. You have to abandon the mining operation.", (byte)3);
                
                return true;
              }
            }
            else
            {
              if (!areAllTilesRockOrReinforcedRock(tilex, tiley, tile, mineDir, true, makingWideTunnel))
              {
                performer.getCommunicator().sendNormalServerMessage("The ground sounds strangely hollow and brittle. You have to abandon the mining operation.", (byte)3);
                
                return true;
              }
              if (!isOutInTunnelOkay(performer, tilex, tiley, makingWideTunnel)) {
                return true;
              }
            }
            if (state > 10)
            {
              int t = Server.caveMesh.getTile(tilex, tiley);
              if (Tiles.isReinforcedCave(Tiles.decodeType(t)))
              {
                performer.getCommunicator().sendNormalServerMessage("You cannot keep mining here. The rock is unusually hard.", (byte)3);
                
                return true;
              }
            }
            if (state < 76)
            {
              state = (byte)(state + 1);
              Zones.setMiningState(tilex, tiley, state, false);
              if (state > Servers.localServer.getTunnelingHits()) {
                performer.getCommunicator().sendNormalServerMessage("You will soon create an entrance.");
              }
            }
            if (tool != null) {
              bonus = tool.skillCheck(1.0D, source, 0.0D, false, counter) / 5.0D;
            }
            power = Math.max(1.0D, mining.skillCheck(1.0D, source, bonus, false, counter));
            if ((performer.getTutorialLevel() == 10) && (!performer.skippedTutorial())) {
              performer.missionFinished(true, true);
            }
            if (Server.rand.nextInt(5) == 0) {
              try
              {
                if (mining.getKnowledge(0.0D) < power) {
                  power = mining.getKnowledge(0.0D);
                }
                rockRandom.setSeed((tilex + tiley * Zones.worldTileSizeY) * 789221L);
                int m = 100;
                
                double imbueEnhancement = 1.0D + 0.23047D * source.getSkillSpellImprovement(1008) / 100.0D;
                float modifier = 1.0F;
                if (source.getSpellEffects() != null) {
                  modifier *= source.getSpellEffects().getRuneEffect(RuneUtilities.ModifierEffect.ENCH_RESGATHERED);
                }
                int max = (int)Math.min(100.0D, 20.0D + rockRandom
                  .nextInt(80) * imbueEnhancement * modifier + source.getRarity());
                power = Math.min(power, max);
                if (source.isCrude()) {
                  power = 1.0D;
                }
                Item newItem = ItemFactory.createItem(146, (float)power, performer
                  .getPosX(), performer.getPosY(), Server.rand.nextFloat() * 360.0F, performer
                  .isOnSurface(), act.getRarity(), -10L, null);
                newItem.setLastOwnerId(performer.getWurmId());
                newItem.setDataXY(tilex, tiley);
                
                performer.getCommunicator().sendNormalServerMessage("You mine some " + newItem.getName() + ".");
                Server.getInstance().broadCastAction(performer
                  .getName() + " mines some " + newItem.getName() + ".", performer, 5);
                createGem(tilex, tiley, performer, power, true, act);
              }
              catch (Exception ex)
              {
                logger.log(Level.WARNING, "Factory failed to produce item", ex);
              }
            } else {
              performer.getCommunicator().sendNormalServerMessage("You chip away at the rock.");
            }
          }
        }
      }
      else
      {
        performer.getCommunicator().sendNormalServerMessage("The water is too deep to mine.", (byte)3);
      }
    }
    else if ((source.isMiningtool()) && (action == 145))
    {
      int digTilex = (int)performer.getStatus().getPositionX() + 2 >> 2;
      int digTiley = (int)performer.getStatus().getPositionY() + 2 >> 2;
      done = mine(act, performer, source, tilex, tiley, action, counter, digTilex, digTiley);
    }
    else if ((source.isMiningtool()) && (action == 156))
    {
      if ((tilex < 0) || (tilex > 1 << Constants.meshSize) || (tiley < 0) || (tiley > 1 << Constants.meshSize))
      {
        performer.getCommunicator().sendNormalServerMessage("The water is too deep to prospect.", (byte)3);
        return true;
      }
      float h = Tiles.decodeHeight(tile);
      if (h > -25.0F)
      {
        Skills skills = performer.getSkills();
        Skill prospecting = null;
        done = false;
        try
        {
          prospecting = skills.getSkill(10032);
        }
        catch (Exception ex)
        {
          prospecting = skills.learn(10032, 1.0F);
        }
        int time = 0;
        if (counter == 1.0F)
        {
          String sstring = "sound.work.prospecting1";
          int x = Server.rand.nextInt(3);
          if (x == 0) {
            sstring = "sound.work.prospecting2";
          } else if (x == 1) {
            sstring = "sound.work.prospecting3";
          }
          SoundPlayer.playSound(sstring, tilex, tiley, performer.isOnSurface(), 1.0F);
          time = (int)Math.max(30.0D, 100.0D - prospecting.getKnowledge(source, 0.0D));
          try
          {
            performer.getCurrentAction().setTimeLeft(time);
          }
          catch (NoSuchActionException nsa)
          {
            logger.log(Level.INFO, "This action does not exist?", nsa);
          }
          performer.getCommunicator().sendNormalServerMessage("You start to gather fragments of the rock.");
          Server.getInstance().broadCastAction(performer.getName() + " starts gathering fragments of the rock.", performer, 5);
          
          performer.sendActionControl(Actions.actionEntrys[''].getVerbString(), true, time);
        }
        else
        {
          try
          {
            time = performer.getCurrentAction().getTimeLeft();
          }
          catch (NoSuchActionException nsa)
          {
            logger.log(Level.INFO, "This action does not exist?", nsa);
          }
        }
        if (counter * 10.0F > time)
        {
          performer.getStatus().modifyStamina(-3000.0F);
          prospecting.skillCheck(1.0D, source, 0.0D, false, counter);
          source.setDamage(source.getDamage() + 5.0E-4F * source.getDamageModifier());
          done = true;
          String findString = "only rock";
          LinkedList<String> list = new LinkedList();
          int m = 100;
          boolean saltExists = false;
          boolean flintExists = false;
          for (int x = -3; x <= 3; x++) {
            for (int y = -3; y <= 3; y++)
            {
              int resource = Server.getCaveResource(tilex + x, tiley + y);
              findString = "";
              if (resource == 65535)
              {
                resource = Server.rand.nextInt(10000);
                Server.setCaveResource(tilex + x, tiley + y, resource);
              }
              int itemTemplate = getItemTemplateForTile(Tiles.decodeType(Server.caveMesh.getTile(tilex + x, tiley + y)));
              if (itemTemplate != 146) {
                try
                {
                  ItemTemplate t = ItemTemplateFactory.getInstance().getTemplate(itemTemplate);
                  String qlstring = "";
                  if (prospecting.getKnowledge(0.0D) > 20.0D)
                  {
                    rockRandom.setSeed((tilex + x + (tiley + y) * Zones.worldTileSizeY) * 789221L);
                    int max = Math.min(100, 20 + rockRandom.nextInt(80));
                    qlstring = " (" + getShardQlDescription(max) + ")";
                  }
                  findString = t.getProspectName() + qlstring;
                }
                catch (NoSuchTemplateException nst)
                {
                  logger.log(Level.WARNING, performer.getName() + " - " + nst.getMessage() + ": " + itemTemplate + " at " + tilex + ", " + tiley, nst);
                }
              }
              if (prospecting.getKnowledge(0.0D) > 40.0D)
              {
                rockRandom.setSeed((tilex + x + (tiley + y) * Zones.worldTileSizeY) * 102533L);
                if (rockRandom.nextInt(100) == 0) {
                  saltExists = true;
                }
              }
              if (prospecting.getKnowledge(0.0D) > 20.0D)
              {
                rockRandom.setSeed((tilex + x + (tiley + y) * Zones.worldTileSizeY) * 6883L);
                if (rockRandom.nextInt(200) == 0) {
                  flintExists = true;
                }
              }
              if (findString.length() > 0) {
                if (!list.contains(findString)) {
                  if (Server.rand.nextBoolean()) {
                    list.addFirst(findString);
                  } else {
                    list.addLast(findString);
                  }
                }
              }
            }
          }
          int x;
          Iterator<String> it;
          if (list.isEmpty())
          {
            findString = "only rock";
          }
          else
          {
            x = 0;
            for (it = list.iterator(); it.hasNext();)
            {
              if (x == 0) {
                findString = (String)it.next();
              } else if (x == list.size() - 1) {
                findString = findString + " and " + (String)it.next();
              } else {
                findString = findString + ", " + (String)it.next();
              }
              x++;
            }
          }
          performer.getCommunicator().sendNormalServerMessage("There is " + findString + " nearby.");
          if (saltExists) {
            performer.getCommunicator().sendNormalServerMessage("You will find salt here!");
          }
          if (flintExists) {
            performer.getCommunicator().sendNormalServerMessage("You will find flint here!");
          }
        }
      }
      else
      {
        performer.getCommunicator().sendNormalServerMessage("The water is too deep to prospect.");
      }
    }
    else if ((action == 141) || (action == 78))
    {
      done = action(act, performer, tilex, tiley, onSurface, tile, action, counter);
    }
    else
    {
      done = super.action(act, performer, source, tilex, tiley, onSurface, heightOffset, tile, action, counter);
    }
    return done;
  }
  
  private static int getTunnelExit(int tilex, int tiley)
  {
    int lowestX = 100000;
    int lowestY = 100000;
    int nextLowestX = lowestX;
    int nextLowestY = lowestY;
    float lowestHeight = 100000.0F;
    float nextLowestHeight = lowestHeight;
    int sameX = lowestX;
    int sameY = lowestY;
    int lowerCount = 0;
    for (int x = 0; x <= 1; x++) {
      for (int y = 0; y <= 1; y++)
      {
        int rockTile = Server.rockMesh.getTile(tilex + x, tiley + y);
        short rockHeight = Tiles.decodeHeight(rockTile);
        if (lowestHeight == 32767.0F)
        {
          lowestHeight = rockHeight;
          lowestX = tilex + x;
          lowestY = tiley + y;
          lowerCount = 1;
        }
        else if (rockHeight < lowestHeight)
        {
          lowestHeight = rockHeight;
          lowestX = tilex + x;
          lowestY = tiley + y;
          lowerCount = 1;
        }
        else if (rockHeight == lowestHeight)
        {
          sameX = tilex + x;
          sameY = tiley + y;
          lowerCount++;
        }
      }
    }
    if (lowerCount > 2)
    {
      logger.log(Level.WARNING, "Bad tile at " + tilex + ", " + tiley);
      return -1;
    }
    if (lowerCount == 2) {
      if ((sameX - lowestX != 0) && (sameY - lowestY != 0))
      {
        logger.log(Level.WARNING, "Bad tile at " + tilex + ", " + tiley);
        return -1;
      }
    }
    int nsY = tiley + (1 - (lowestY - tiley));
    int nsRockTile = Server.rockMesh.getTile(lowestX, nsY);
    short nsRockHeight = Tiles.decodeHeight(nsRockTile);
    nextLowestHeight = nsRockHeight;
    nextLowestX = lowestX;
    nextLowestY = nsY;
    
    int weX = tilex + (1 - (lowestX - tilex));
    int weRockTile = Server.rockMesh.getTile(weX, lowestY);
    short weRockHeight = Tiles.decodeHeight(weRockTile);
    if (weRockHeight < nextLowestHeight)
    {
      nextLowestHeight = weRockHeight;
      nextLowestX = weX;
      nextLowestY = lowestY;
    }
    else if (weRockHeight == nextLowestHeight)
    {
      logger.log(Level.WARNING, "Bad tile at " + tilex + ", " + tiley);
      return -1;
    }
    if (lowestX == tilex + 0)
    {
      if (lowestY == tiley + 0)
      {
        if (nextLowestX == tilex + 1)
        {
          if (nextLowestY == tiley + 0) {
            return 3;
          }
        }
        else if (nextLowestY == tiley + 1) {
          return 2;
        }
      }
      else if (lowestY == tiley + 1) {
        if (nextLowestX == tilex + 1)
        {
          if (nextLowestY == tiley + 1) {
            return 5;
          }
        }
        else if (nextLowestY == tiley + 0) {
          return 2;
        }
      }
    }
    else if (lowestY == tiley + 0)
    {
      if (nextLowestX == tilex + 1)
      {
        if (nextLowestY == tiley + 1) {
          return 4;
        }
      }
      else if (nextLowestY == tiley + 0) {
        return 3;
      }
    }
    else if (lowestY == tiley + 1) {
      if (nextLowestX == tilex + 1)
      {
        if (nextLowestY == tiley + 0) {
          return 4;
        }
      }
      else if (nextLowestY == tiley + 1) {
        return 5;
      }
    }
    logger.log(Level.WARNING, "Bad tile at " + tilex + ", " + tiley);
    return -1;
  }
  
  private static void setTileToTransition(int tilex, int tiley)
  {
    VolaTile t = Zones.getTileOrNull(tilex, tiley, true);
    if (t != null) {
      t.isTransition = true;
    }
    t = Zones.getTileOrNull(tilex, tiley, false);
    if (t != null) {
      t.isTransition = true;
    }
  }
  
  private boolean isOutInTunnelOkay(Creature performer, int tilex, int tiley, boolean makingWideTunnel)
  {
    for (int x = -1; x <= 1; x++) {
      for (int y = -1; y <= 1; y++)
      {
        int tileNew = Server.surfaceMesh.getTile(tilex + x, tiley + y);
        if ((Tiles.decodeType(tileNew) == Tiles.Tile.TILE_HOLE.id) && (!makingWideTunnel))
        {
          performer.getCommunicator().sendNormalServerMessage("Another tunnel is too close. It would collapse.", (byte)3);
          
          return false;
        }
        if (Tiles.isMineDoor(Tiles.decodeType(tileNew)))
        {
          performer.getCommunicator().sendNormalServerMessage("Cannot make a tunnel next to a mine door.");
          return false;
        }
        if ((x >= 0) && (y >= 0))
        {
          int rockTile = Server.rockMesh.getTile(tilex + x, tiley + y);
          short rockHeight = Tiles.decodeHeight(rockTile);
          int caveTile = Server.caveMesh.getTile(tilex + x, tiley + y);
          short cheight = Tiles.decodeHeight(caveTile);
          if (!isNullWall(caveTile)) {
            if (rockHeight - cheight >= 255)
            {
              performer.getCommunicator().sendNormalServerMessage("Not enough rock height to make a tunnel there.");
              return false;
            }
          }
        }
      }
    }
    return true;
  }
  
  static boolean isHoleNear(int tilex, int tiley)
  {
    MeshIO surfMesh = Server.surfaceMesh;
    for (int x = -1; x <= 1; x++) {
      for (int y = -1; y <= 1; y++)
      {
        int tileNew = surfMesh.getTile(tilex + x, tiley + y);
        if (Tiles.decodeType(tileNew) == Tiles.Tile.TILE_HOLE.id) {
          return true;
        }
      }
    }
    return false;
  }
  
  static boolean canHaveWideEntrance(@Nullable Creature performer, int tilex, int tiley)
  {
    MeshIO surfMesh = Server.surfaceMesh;
    if (!hasValidNearbyEntrance(performer, surfMesh, tilex, tiley)) {
      return false;
    }
    MeshTile currentMT = new MeshTile(surfMesh, tilex, tiley);
    MeshTile mTileNorth = currentMT.getNorthMeshTile();
    if (mTileNorth.isHole())
    {
      int dir = mTileNorth.getLowerLip();
      if (dir == 6)
      {
        if (currentMT.getWestSlope() != 0)
        {
          if (performer != null) {
            performer.getCommunicator().sendNormalServerMessage("Current tile needs a flat border to correspond to lower part of adjacent cave entrance.");
          }
          return false;
        }
        if (currentMT.getSouthSlope() <= 0)
        {
          if (performer != null) {
            performer.getCommunicator().sendNormalServerMessage("Current tile needs to be same orientation as adjacent cave entrance.");
          }
          return false;
        }
        return true;
      }
      if (dir == 2)
      {
        if (currentMT.getEastSlope() != 0)
        {
          if (performer != null) {
            performer.getCommunicator().sendNormalServerMessage("Current tile needs a flat border to correspond to lower part of adjacent cave entrance.");
          }
          return false;
        }
        if (currentMT.getSouthSlope() >= 0)
        {
          if (performer != null) {
            performer.getCommunicator().sendNormalServerMessage("Current tile needs to be same orientation as adjacent cave entrance.");
          }
          return false;
        }
        return true;
      }
    }
    MeshTile mTileWest = currentMT.getWestMeshTile();
    if (mTileWest.isHole())
    {
      int dir = mTileWest.getLowerLip();
      if (dir == 0)
      {
        if (currentMT.getNorthSlope() != 0)
        {
          if (performer != null) {
            performer.getCommunicator().sendNormalServerMessage("Current tile needs a flat border to correspond to lower part of adjacent cave entrance.");
          }
          return false;
        }
        if (currentMT.getEastSlope() <= 0)
        {
          if (performer != null) {
            performer.getCommunicator().sendNormalServerMessage("Current tile needs to be same orientation as adjacent cave entrance.");
          }
          return false;
        }
        return true;
      }
      if (dir == 4)
      {
        if (currentMT.getSouthSlope() != 0)
        {
          if (performer != null) {
            performer.getCommunicator().sendNormalServerMessage("Current tile needs a flat border to correspond to lower part of adjacent cave entrance.");
          }
          return false;
        }
        if (currentMT.getEastSlope() >= 0)
        {
          if (performer != null) {
            performer.getCommunicator().sendNormalServerMessage("Current tile needs to be same orientation as adjacent cave entrance.");
          }
          return false;
        }
        return true;
      }
    }
    MeshTile mTileSouth = currentMT.getSouthMeshTile();
    if (mTileSouth.isHole())
    {
      int dir = mTileSouth.getLowerLip();
      if (dir == 6)
      {
        if (currentMT.getWestSlope() != 0)
        {
          if (performer != null) {
            performer.getCommunicator().sendNormalServerMessage("Current tile needs a flat border to correspond to lower part of adjacent cave entrance.");
          }
          return false;
        }
        if (currentMT.getNorthSlope() <= 0)
        {
          if (performer != null) {
            performer.getCommunicator().sendNormalServerMessage("Current tile needs to be same orientation as adjacent cave entrance.");
          }
          return false;
        }
        return true;
      }
      if (dir == 2)
      {
        if (currentMT.getEastSlope() != 0)
        {
          if (performer != null) {
            performer.getCommunicator().sendNormalServerMessage("Current tile needs a flat border to correspond to lower part of adjacent cave entrance.");
          }
          return false;
        }
        if (currentMT.getNorthSlope() >= 0)
        {
          if (performer != null) {
            performer.getCommunicator().sendNormalServerMessage("Current tile needs to be same orientation as adjacent cave entrance.");
          }
          return false;
        }
        return true;
      }
    }
    MeshTile mTileEast = currentMT.getEastMeshTile();
    if (mTileEast.isHole())
    {
      int dir = mTileEast.getLowerLip();
      if (dir == 0)
      {
        if (currentMT.getNorthSlope() != 0)
        {
          if (performer != null) {
            performer.getCommunicator().sendNormalServerMessage("Current tile needs a flat border to correspond to lower part of adjacent cave entrance.");
          }
          return false;
        }
        if (currentMT.getWestSlope() <= 0)
        {
          if (performer != null) {
            performer.getCommunicator().sendNormalServerMessage("Current tile needs to be same orientation as adjacent cave entrance.");
          }
          return false;
        }
        return true;
      }
      if (dir == 4)
      {
        if (currentMT.getSouthSlope() != 0)
        {
          if (performer != null) {
            performer.getCommunicator().sendNormalServerMessage("Current tile needs a flat border to correspond to lower part of adjacent cave entrance.");
          }
          return false;
        }
        if (currentMT.getWestSlope() >= 0)
        {
          if (performer != null) {
            performer.getCommunicator().sendNormalServerMessage("Current tile needs to be same orientation as adjacent cave entrance.");
          }
          return false;
        }
        return true;
      }
    }
    return false;
  }
  
  private static boolean hasValidNearbyEntrance(@Nullable Creature performer, MeshIO surfMesh, int tilex, int tiley)
  {
    int holeX = -1;
    int holeY = -1;
    int holeXX = -1;
    int holeYY = -1;
    for (int x = -1; x <= 1; x++) {
      for (int y = -1; y <= 1; y++) {
        if ((x != 0) && (y != 0))
        {
          int tileNew = surfMesh.getTile(tilex + x, tiley + y);
          byte type = Tiles.decodeType(tileNew);
          if (type == Tiles.Tile.TILE_HOLE.id)
          {
            if (performer != null) {
              performer.getCommunicator().sendNormalServerMessage("Cannot have cave entrances meeting diagonally.");
            }
            return false;
          }
        }
      }
    }
    for (int x = -1; x <= 1; x++) {
      for (int y = -1; y <= 1; y++) {
        if ((x != 0) || (y != 0))
        {
          int tileNew = surfMesh.getTile(tilex + x, tiley + y);
          byte type = Tiles.decodeType(tileNew);
          if (Tiles.isMineDoor(type))
          {
            if (performer != null) {
              performer.getCommunicator().sendNormalServerMessage("Cannot make a tunnel next to a mine door.");
            }
            return false;
          }
        }
      }
    }
    for (int x = -1; x <= 1; x++) {
      for (int y = -1; y <= 1; y++) {
        if ((x != 0) || (y != 0)) {
          if ((x == 0) || (y == 0))
          {
            int tileNew = surfMesh.getTile(tilex + x, tiley + y);
            if (Tiles.decodeType(tileNew) == Tiles.Tile.TILE_HOLE.id)
            {
              if (holeX != -1)
              {
                if (performer != null) {
                  performer.getCommunicator().sendNormalServerMessage("Can only make two or three tile wide cave entrances .");
                }
                return false;
              }
              holeX = tilex + x;
              holeY = tiley + y;
            }
          }
        }
      }
    }
    if (holeX == -1) {
      return true;
    }
    for (int xx = -1; xx <= 1; xx++) {
      for (int yy = -1; yy <= 1; yy++) {
        if ((xx != 0) || (yy != 0))
        {
          int tileTwo = surfMesh.getTile(holeX + xx, holeY + yy);
          if (Tiles.decodeType(tileTwo) == Tiles.Tile.TILE_HOLE.id)
          {
            if (holeXX != -1)
            {
              if (performer != null) {
                performer.getCommunicator().sendNormalServerMessage("Can only make two or three tile wide cave entrances .");
              }
              return false;
            }
            holeXX = holeX + xx;
            holeYY = holeY + yy;
            if ((tilex + xx + xx != holeXX) || (tiley + yy + yy != holeYY))
            {
              if (performer != null) {
                performer.getCommunicator().sendNormalServerMessage("Can only make two or three tile wide cave entrances .");
              }
              return false;
            }
          }
        }
      }
    }
    if (holeXX == -1) {
      return true;
    }
    for (int xxx = -1; xxx <= 1; xxx++) {
      for (int yyy = -1; yyy <= 1; yyy++) {
        if ((xxx != 0) || (yyy != 0))
        {
          int tileThree = surfMesh.getTile(holeXX + xxx, holeYY + yyy);
          if (Tiles.decodeType(tileThree) == Tiles.Tile.TILE_HOLE.id) {
            if ((holeXX + xxx != holeX) || (holeYY + yyy != holeY))
            {
              if (performer != null) {
                performer.getCommunicator().sendNormalServerMessage("Can only make two or three tile wide cave entrances .");
              }
              return false;
            }
          }
        }
      }
    }
    return true;
  }
  
  static boolean isStructureNear(int tilex, int tiley)
  {
    for (int x = -1; x <= 0; x++) {
      for (int y = -1; y <= 0; y++)
      {
        VolaTile vt = Zones.getTileOrNull(tilex + x, tiley + y, true);
        if ((vt != null) && (vt.getStructure() != null)) {
          return true;
        }
        VolaTile vtc = Zones.getTileOrNull(tilex + x, tiley + y, false);
        if ((vtc != null) && (vtc.getStructure() != null)) {
          return true;
        }
      }
    }
    return false;
  }
  
  static boolean createOutInTunnel(int tilex, int tiley, int tile, Creature performer, int mod)
  {
    MeshIO surfmesh = Server.surfaceMesh;
    MeshIO cavemesh = Server.caveMesh;
    VolaTile t = Zones.getTileOrNull(tilex, tiley, true);
    if (t != null)
    {
      Item[] items = t.getItems();
      for (Item lItem : items) {
        if (lItem.isDecoration())
        {
          performer.getCommunicator().sendNormalServerMessage(
            LoginHandler.raiseFirstLetter(lItem.getNameWithGenus()) + " on the surface disturbs your operation.");
          
          return false;
        }
      }
      if (t.getStructure() != null)
      {
        performer.getCommunicator().sendNormalServerMessage("You can't tunnel here, there is a structure in the way.");
        return false;
      }
    }
    boolean makingWideTunnel = false;
    if (isHoleNear(tilex, tiley)) {
      if (canHaveWideEntrance(performer, tilex, tiley))
      {
        makingWideTunnel = true;
      }
      else
      {
        performer.getCommunicator().sendNormalServerMessage("Another tunnel is too close. It would collapse.");
        return false;
      }
    }
    if (affectsHighway(tilex, tiley))
    {
      performer.getCommunicator().sendNormalServerMessage("A surface highway interferes with your tunneling operation.", (byte)3);
      
      return false;
    }
    Point lowestCorner = findLowestCorner(performer, tilex, tiley);
    if (lowestCorner == null) {
      return false;
    }
    Point nextLowestCorner = findNextLowestCorner(performer, tilex, tiley, lowestCorner);
    if (nextLowestCorner == null) {
      return false;
    }
    Point highestCorner = findHighestCorner(tilex, tiley);
    if (highestCorner == null) {
      return false;
    }
    Point nextHighestCorner = findNextHighestCorner(tilex, tiley, highestCorner);
    if (nextHighestCorner == null) {
      return false;
    }
    if (((nextLowestCorner.getH() != lowestCorner.getH()) && 
      (isStructureNear(nextLowestCorner.getX(), nextLowestCorner.getY()))) || (
      (nextHighestCorner.getH() != highestCorner.getH()) && 
      (isStructureNear(highestCorner.getX(), highestCorner.getY()))))
    {
      performer.getCommunicator().sendNormalServerMessage("Cannot create a tunnel here as there is a structure too close.", (byte)3);
      
      return false;
    }
    for (int x = -1; x <= 1; x++) {
      for (int y = -1; y <= 1; y++)
      {
        VolaTile svt = Zones.getTileOrNull(tilex + x, tiley + y, true);
        Structure ss = svt == null ? null : svt.getStructure();
        if ((ss != null) && (ss.isTypeBridge()))
        {
          performer.getCommunicator().sendNormalServerMessage("You can't tunnel here, there is a bridge in the way.");
          return false;
        }
        VolaTile cvt = Zones.getTileOrNull(tilex + x, tiley + y, false);
        Structure cs = cvt == null ? null : cvt.getStructure();
        if ((cs != null) && (cs.isTypeBridge()))
        {
          performer.getCommunicator().sendNormalServerMessage("You can't tunnel here, there is a bridge in the way.");
          return false;
        }
      }
    }
    int nsY = tiley + (1 - (nextLowestCorner.getY() - tiley));
    int weX = tilex + (1 - (nextLowestCorner.getX() - tilex));
    
    int nsCorner = surfmesh.getTile(nextLowestCorner.getX(), nsY);
    if (!mayLowerCornerOnSlope(lowestCorner.getH(), performer, nsCorner)) {
      return false;
    }
    int weCorner = surfmesh.getTile(weX, nextLowestCorner.getY());
    if (!mayLowerCornerOnSlope(lowestCorner.getH(), performer, weCorner)) {
      return false;
    }
    if (Tiles.isReinforcedCave(Tiles.decodeType(cavemesh.getTile(tilex, tiley)))) {
      return false;
    }
    if (makingWideTunnel) {
      performer.getCommunicator().sendNormalServerMessage("You expand a tunnel entrance!");
    } else {
      performer.getCommunicator().sendNormalServerMessage("You create a tunnel entrance!");
    }
    short targetHeight = (short)lowestCorner.getH();
    for (int x = tilex; x <= tilex + 1; x++) {
      for (int y = tiley; y <= tiley + 1; y++)
      {
        int tileNew = cavemesh.getTile(x, y);
        int rockTile = Server.rockMesh.getTile(x, y);
        short rockHeight = Tiles.decodeHeight(rockTile);
        int surfTile = Server.surfaceMesh.getTile(x, y);
        short surfHeight = Tiles.decodeHeight(surfTile);
        if ((x == tilex) && (y == tiley))
        {
          if (((x == lowestCorner.getX()) && (y == lowestCorner.getY())) || (
            (x == nextLowestCorner.getX()) && (y == nextLowestCorner.getY())))
          {
            int[] newfloorceil = getFloorAndCeiling(x, y, targetHeight, 0, true, false, performer);
            int newFloorHeight = newfloorceil[0];
            
            cavemesh.setTile(x, y, Tiles.encode((short)newFloorHeight, Tiles.Tile.TILE_CAVE_EXIT.id, (byte)0));
            VolaTile surft = Zones.getTileOrNull(x, y, true);
            if (surft != null) {
              surft.isTransition = true;
            }
            VolaTile cavet = Zones.getTileOrNull(x, y, false);
            if (cavet != null) {
              cavet.isTransition = true;
            }
            if ((rockHeight != newFloorHeight) || (surfHeight != newFloorHeight))
            {
              Server.rockMesh.setTile(x, y, Tiles.encode((short)newFloorHeight, (short)0));
              surfmesh.setTile(x, y, Tiles.encode((short)newFloorHeight, Tiles.decodeTileData(surfTile)));
              Players.getInstance().sendChangedTile(x, y, true, true);
            }
          }
          else
          {
            int[] newfloorceil = getFloorAndCeiling(x, y, targetHeight, mod, false, true, performer);
            int newFloorHeight = newfloorceil[0];
            int newCeil = newfloorceil[1];
            if ((Tiles.decodeType(tileNew) == Tiles.Tile.TILE_CAVE_WALL.id) || 
              (Tiles.decodeType(tileNew) == Tiles.Tile.TILE_CAVE_WALL_ROCKSALT.id))
            {
              cavemesh.setTile(x, y, Tiles.encode((short)newFloorHeight, Tiles.Tile.TILE_CAVE_EXIT.id, (byte)(newCeil - newFloorHeight)));
              
              VolaTile surft = Zones.getTileOrNull(x, y, true);
              if (surft != null) {
                surft.isTransition = true;
              }
              VolaTile cavet = Zones.getTileOrNull(x, y, false);
              if (cavet != null) {
                cavet.isTransition = true;
              }
            }
            else
            {
              cavemesh.setTile(x, y, Tiles.encode((short)newFloorHeight, 
                Tiles.decodeType(tileNew), (byte)(newCeil - newFloorHeight)));
            }
          }
        }
        else if (((x == lowestCorner.getX()) && (y == lowestCorner.getY())) || (
          (x == nextLowestCorner.getX()) && (y == nextLowestCorner.getY())))
        {
          int[] newfloorceil = getFloorAndCeiling(x, y, targetHeight, 0, true, false, performer);
          int newFloorHeight = newfloorceil[0];
          
          cavemesh.setTile(x, y, Tiles.encode((short)newFloorHeight, Tiles.decodeType(tileNew), (byte)0));
          if ((rockHeight != newFloorHeight) || (surfHeight != newFloorHeight))
          {
            Server.rockMesh.setTile(x, y, Tiles.encode((short)newFloorHeight, (short)0));
            surfmesh.setTile(x, y, Tiles.encode((short)newFloorHeight, Tiles.decodeTileData(surfTile)));
            Players.getInstance().sendChangedTile(x, y, true, true);
          }
        }
        else
        {
          int[] newfloorceil = getFloorAndCeiling(x, y, targetHeight, mod, false, true, performer);
          int newFloorHeight = newfloorceil[0];
          int newCeil = newfloorceil[1];
          
          cavemesh.setTile(x, y, Tiles.encode((short)newFloorHeight, 
            Tiles.decodeType(tileNew), (byte)(newCeil - newFloorHeight)));
        }
        Players.getInstance().sendChangedTile(x, y, false, true);
        for (int xx = -1; xx <= 0; xx++) {
          for (int yy = -1; yy <= 0; yy++) {
            try
            {
              Zone toCheckForChange = Zones.getZone(x + xx, y + yy, false);
              toCheckForChange.changeTile(x + xx, y + yy);
            }
            catch (NoSuchZoneException nsz)
            {
              logger.log(Level.INFO, "no such zone?: " + (x + xx) + ", " + (y + yy), nsz);
            }
          }
        }
      }
    }
    setTileToTransition(tilex, tiley);
    tile = Server.surfaceMesh.getTile(tilex, tiley);
    surfmesh.setTile(tilex, tiley, Tiles.encode(Tiles.decodeHeight(tile), Tiles.Tile.TILE_HOLE.id, Tiles.decodeData(tile)));
    
    short targetUpperHeight = (short)nextHighestCorner.getH();
    
    short tileData = Tiles.decodeTileData(Server.surfaceMesh.getTile(highestCorner.getX(), highestCorner.getY()));
    Server.surfaceMesh.setTile(highestCorner.getX(), highestCorner.getY(), 
      Tiles.encode(targetUpperHeight, tileData));
    tileData = Tiles.decodeTileData(Server.rockMesh.getTile(highestCorner.getX(), highestCorner.getY()));
    Server.rockMesh.setTile(highestCorner.getX(), highestCorner.getY(), 
      Tiles.encode(targetUpperHeight, tileData));
    tileData = Tiles.decodeTileData(Server.caveMesh.getTile(highestCorner.getX(), highestCorner.getY()));
    
    Players.getInstance().sendChangedTile(highestCorner.getX(), highestCorner.getY(), true, true);
    Players.getInstance().sendChangedTile(highestCorner.getX(), highestCorner.getY(), false, true);
    
    Players.getInstance().sendChangedTile(tilex, tiley, true, true);
    
    VolaTile to = Zones.getOrCreateTile(tilex, tiley, true);
    to.checkCaveOpening();
    return true;
  }
  
  @Nullable
  private static Point findLowestCorner(Creature performer, int tilex, int tiley)
  {
    int lowestX = 100000;
    int lowestY = 100000;
    short lowestHeight = Short.MAX_VALUE;
    for (int x = 0; x <= 1; x++) {
      for (int y = 0; y <= 1; y++)
      {
        int rockTile = Server.rockMesh.getTile(tilex + x, tiley + y);
        short rockHeight = Tiles.decodeHeight(rockTile);
        int caveTile = Server.caveMesh.getTile(tilex + x, tiley + y);
        short cheight = Tiles.decodeHeight(caveTile);
        if (!isNullWall(caveTile)) {
          if (rockHeight - cheight >= 255)
          {
            performer.getCommunicator().sendNormalServerMessage("The mountainside would risk crumbling. You cannot tunnel here.");
            
            return null;
          }
        }
        if (lowestHeight == Short.MAX_VALUE)
        {
          lowestHeight = rockHeight;
          lowestX = tilex + x;
          lowestY = tiley + y;
        }
        else if (rockHeight < lowestHeight)
        {
          lowestHeight = rockHeight;
          lowestX = tilex + x;
          lowestY = tiley + y;
        }
      }
    }
    return new Point(lowestX, lowestY, lowestHeight);
  }
  
  private static Point findNextLowestCorner(Creature performer, int tilex, int tiley, Point lowestCorner)
  {
    int nextLowestX = lowestCorner.getX();
    int nextLowestY = tiley + (1 - (lowestCorner.getY() - tiley));
    int nsRockTile = Server.rockMesh.getTile(nextLowestX, nextLowestY);
    short nextLowestHeight = Tiles.decodeHeight(nsRockTile);
    
    int weX = tilex + (1 - (lowestCorner.getX() - tilex));
    int weRockTile = Server.rockMesh.getTile(weX, lowestCorner.getY());
    short weRockHeight = Tiles.decodeHeight(weRockTile);
    if (weRockHeight < nextLowestHeight)
    {
      nextLowestHeight = weRockHeight;
      nextLowestX = weX;
      nextLowestY = lowestCorner.getY();
    }
    return new Point(nextLowestX, nextLowestY, nextLowestHeight);
  }
  
  public static Point findHighestCorner(int tilex, int tiley)
  {
    int highestX = 100000;
    int highestY = 100000;
    short highestHeight = Short.MAX_VALUE;
    for (int x = 0; x <= 1; x++) {
      for (int y = 0; y <= 1; y++)
      {
        int rockTile = Server.rockMesh.getTile(tilex + x, tiley + y);
        short rockHeight = Tiles.decodeHeight(rockTile);
        if (highestHeight == Short.MAX_VALUE)
        {
          highestHeight = rockHeight;
          highestX = tilex + x;
          highestY = tiley + y;
        }
        else if (rockHeight > highestHeight)
        {
          highestHeight = rockHeight;
          highestX = tilex + x;
          highestY = tiley + y;
        }
      }
    }
    return new Point(highestX, highestY, highestHeight);
  }
  
  public static Point findNextHighestCorner(int tilex, int tiley, Point highestCorner)
  {
    int nextHighestX = highestCorner.getX();
    int nextHighestY = tiley + (1 - (highestCorner.getY() - tiley));
    int nsRockTile = Server.rockMesh.getTile(nextHighestX, nextHighestY);
    short nextHighestHeight = Tiles.decodeHeight(nsRockTile);
    
    int weX = tilex + (1 - (highestCorner.getX() - tilex));
    int weRockTile = Server.rockMesh.getTile(weX, highestCorner.getY());
    short weRockHeight = Tiles.decodeHeight(weRockTile);
    if (weRockHeight > nextHighestHeight)
    {
      nextHighestHeight = weRockHeight;
      nextHighestX = weX;
      nextHighestY = highestCorner.getY();
    }
    return new Point(nextHighestX, nextHighestY, nextHighestHeight);
  }
  
  private static boolean mayLowerCornerOnSlope(int targetHeight, Creature performer, int checkedTile)
  {
    int nCHeight = Tiles.decodeHeight(checkedTile);
    if (nCHeight - targetHeight > 270)
    {
      performer.getCommunicator().sendNormalServerMessage("The mountainside would risk crumbling. You can't open a hole here.");
      
      return false;
    }
    return true;
  }
  
  private static boolean areAllTilesRockOrReinforcedRock(int tilex, int tiley, int tile, int direction, boolean creatingExit, boolean makingWideTunnel)
  {
    boolean checkTile = false;
    int t = 0;
    byte type = 0;
    for (int x = -1; x <= 1; x++) {
      for (int y = -1; y <= 1; y++)
      {
        t = Server.caveMesh.getTile(tilex + x, tiley + y);
        type = Tiles.decodeType(t);
        if (direction == 3)
        {
          if (y <= 0) {
            checkTile = true;
          }
        }
        else if (direction == 4)
        {
          if (x >= 0) {
            checkTile = true;
          }
        }
        else if (direction == 5)
        {
          if (y >= 0) {
            checkTile = true;
          }
        }
        else if (x <= 0) {
          checkTile = true;
        }
        if (checkTile) {
          if (creatingExit) {
            if ((type != Tiles.Tile.TILE_CAVE_WALL.id) && (type != Tiles.Tile.TILE_CAVE_WALL_ROCKSALT.id) && 
              (!Tiles.isReinforcedCave(type))) {
              if ((type != Tiles.Tile.TILE_CAVE_EXIT.id) || (!makingWideTunnel)) {
                return false;
              }
            }
          }
        }
        checkTile = false;
      }
    }
    return true;
  }
  
  static boolean isInsideTunnelOk(int tilex, int tiley, int tile, int action, int direction, Creature performer, boolean disintegrate)
  {
    if ((Tiles.decodeType(tile) == Tiles.Tile.TILE_CAVE_WALL.id) || (Tiles.decodeType(tile) == Tiles.Tile.TILE_CAVE_WALL_ROCKSALT.id) || 
      (Server.getCaveResource(tilex, tiley) <= 0) || (disintegrate)) {
      if ((Tiles.decodeHeight(tile) >= -25) || 
        (Tiles.decodeHeight(tile) == -100))
      {
        int dir = 6;
        if (direction == 3) {
          dir = 0;
        } else if (direction == 5) {
          dir = 4;
        } else if (direction == 4) {
          dir = 2;
        }
        boolean[][] solids = new boolean[3][3];
        float minHeight = 1000000.0F;
        float maxHeight = 0.0F;
        float currHeight = 100000.0F;
        float currCeil = 0.0F;
        for (int x = -1; x <= 1; x++) {
          for (int y = -1; y <= 1; y++)
          {
            int t = Server.caveMesh.getTile(tilex + x, tiley + y);
            solids[(x + 1)][(y + 1)] = Tiles.isSolidCave(Tiles.decodeType(t));
            short height = Tiles.decodeHeight(t);
            int ceil = Tiles.decodeData(t) & 0xFF;
            boolean setCurrHeight = false;
            boolean setExitheight = false;
            if (dir == 0)
            {
              if (((x == 0) && (y == 1)) || ((x == 1) && (y == 1))) {
                setCurrHeight = true;
              } else if ((y == 0) && (x >= 0)) {
                setExitheight = true;
              }
            }
            else if (dir == 2)
            {
              if (((x == 0) && (y == 0)) || ((x == 0) && (y == 1))) {
                setCurrHeight = true;
              } else if ((x == 1) && (y >= 0)) {
                setExitheight = true;
              }
            }
            else if (dir == 6)
            {
              if (((x == 1) && (y == 1)) || ((x == 1) && (y == 0))) {
                setCurrHeight = true;
              } else if ((x == 0) && (y >= 0)) {
                setExitheight = true;
              }
            }
            else if (dir == 4) {
              if (((x == 0) && (y == 0)) || ((x == 1) && (y == 0))) {
                setCurrHeight = true;
              } else if ((y == 1) && (x >= 0)) {
                setExitheight = true;
              }
            }
            if (setCurrHeight)
            {
              if (height < currHeight) {
                currHeight = height;
              }
              if (height + ceil > currCeil) {
                currCeil = height + ceil;
              }
            }
            if ((setExitheight) && (!isNullWall(t)))
            {
              if (height < minHeight) {
                minHeight = height;
              }
              if (height + ceil > maxHeight) {
                maxHeight = height + ceil;
              }
            }
          }
        }
        if (solids[0][0] == 0) {
          if ((solids[1][0] != 0) && (solids[0][1] != 0))
          {
            performer.getCommunicator().sendNormalServerMessage("The cave walls sound hollow. A dangerous side shaft could emerge.");
            
            return false;
          }
        }
        if (solids[2][0] == 0) {
          if ((solids[2][1] != 0) && (solids[1][0] != 0))
          {
            performer.getCommunicator().sendNormalServerMessage("The cave walls sound hollow. A dangerous side shaft could emerge.");
            
            return false;
          }
        }
        if (solids[0][2] == 0) {
          if ((solids[1][2] != 0) && (solids[0][1] != 0))
          {
            performer.getCommunicator().sendNormalServerMessage("The cave walls sound hollow. A dangerous side shaft could emerge.");
            
            return false;
          }
        }
        if (solids[2][2] == 0) {
          if ((solids[1][2] != 0) && (solids[2][1] != 0))
          {
            performer.getCommunicator().sendNormalServerMessage("The cave walls sound hollow. A dangerous side shaft could emerge.");
            
            return false;
          }
        }
        if (action == 147)
        {
          if (currHeight - 20.0F < minHeight) {
            minHeight = currHeight - 20.0F;
          }
        }
        else if (action == 146) {
          if (currCeil + 20.0F > maxHeight) {
            maxHeight = currCeil + 20.0F;
          }
        }
        if (maxHeight - minHeight > 254.0F)
        {
          performer.getCommunicator().sendNormalServerMessage("A dangerous crack is starting to form on the floor. You will have to find another way.");
          
          return false;
        }
        if (maxHeight - minHeight > 100.0F) {
          performer.getCommunicator().sendNormalServerMessage("You hear falling rocks from the other side of the wall. A deep shaft will probably emerge.");
        }
        return true;
      }
    }
    return false;
  }
  
  private static boolean wouldPassThroughRockLayer(int tilex, int tiley, int tile, int action)
  {
    int maxCaveFloor = -100000;
    int minRockHeight = 100000;
    for (int x = 0; x <= 1; x++) {
      for (int y = 0; y <= 1; y++)
      {
        tile = Server.caveMesh.getTile(tilex + x, tiley + y);
        short ht = Tiles.decodeHeight(tile);
        boolean allSolid = true;
        if (ht != -100)
        {
          for (int xx = -1; (xx <= 0) && (allSolid); xx++) {
            for (int yy = -1; (yy <= 0) && (allSolid); yy++)
            {
              int encodedTile = Server.caveMesh.getTile(tilex + x + xx, tiley + y + yy);
              byte type = Tiles.decodeType(encodedTile);
              if (!Tiles.isSolidCave(type)) {
                allSolid = false;
              }
            }
          }
          if (allSolid)
          {
            ht = -100;
            Server.caveMesh.setTile(tilex + x, tiley + y, Tiles.encode(ht, Tiles.decodeType(tile), (byte)0));
          }
        }
        if (ht > maxCaveFloor) {
          maxCaveFloor = ht;
        }
      }
    }
    for (int x = 0; x <= 1; x++) {
      for (int y = 0; y <= 1; y++)
      {
        tile = Server.rockMesh.getTile(tilex + x, tiley + y);
        short ht = Tiles.decodeHeight(tile);
        if (ht < minRockHeight) {
          minRockHeight = Tiles.decodeHeight(tile);
        }
      }
    }
    int mod = 0;
    if (action == 147) {
      mod = -20;
    } else if (action == 146) {
      mod = 20;
    }
    if (maxCaveFloor + mod + 30 > minRockHeight) {
      return true;
    }
    return false;
  }
  
  public static boolean createInsideTunnel(int tilex, int tiley, int tile, Creature performer, int action, int direction, boolean disintegrate, @Nullable Action act)
  {
    if (isInsideTunnelOk(tilex, tiley, tile, action, direction, performer, disintegrate))
    {
      if (wouldPassThroughRockLayer(tilex, tiley, tile, action))
      {
        int mineDir = getTunnelExit(tilex, tiley);
        if (mineDir == -1)
        {
          performer.getCommunicator().sendNormalServerMessage("The topology here makes it impossible to mine in a good way.");
          
          return false;
        }
        boolean makingWideTunnel = false;
        if (canHaveWideEntrance(performer, tilex, tiley)) {
          makingWideTunnel = true;
        }
        if (areAllTilesRockOrReinforcedRock(tilex, tiley, tile, mineDir, true, makingWideTunnel))
        {
          int t = Server.surfaceMesh.getTile(tilex, tiley);
          if ((Tiles.decodeType(t) != Tiles.Tile.TILE_ROCK.id) && (Tiles.decodeType(t) != Tiles.Tile.TILE_CLIFF.id))
          {
            performer.getCommunicator().sendNormalServerMessage("The cave walls look very unstable and dirt flows in. You would be buried alive.");
            
            return false;
          }
          if (!createOutInTunnel(tilex, tiley, tile, performer, 0)) {
            return false;
          }
        }
        else
        {
          performer.getCommunicator().sendNormalServerMessage("The cave walls look very unstable. You cannot keep mining here.");
          
          return false;
        }
      }
      else if (!createStandardTunnel(tilex, tiley, tile, performer, action, direction, disintegrate, act))
      {
        return false;
      }
      TileEvent.log(tilex, tiley, -1, performer.getWurmId(), 227);
      return true;
    }
    return false;
  }
  
  static final boolean allCornersAtRockHeight(int tilex, int tiley)
  {
    for (int x = 0; x <= 1; x++) {
      for (int y = 0; y <= 1; y++)
      {
        int cavet = Server.caveMesh.getTile(tilex + x, tiley + y);
        short caveheight = Tiles.decodeHeight(cavet);
        int ceil = Tiles.decodeData(cavet) & 0xFF;
        short rockHeight = Tiles.decodeHeight(Server.rockMesh.getTile(tilex + x, tiley + y));
        if (caveheight + ceil != rockHeight) {
          return false;
        }
      }
    }
    return true;
  }
  
  public static final int getCurrentCeilingHeight(int tilex, int tiley)
  {
    int cavet = Server.caveMesh.getTile(tilex, tiley);
    return Tiles.decodeHeight(cavet) + (Tiles.decodeData(cavet) & 0xFF);
  }
  
  private static final int getRockHeight(int tilex, int tiley)
  {
    int rockTile = Server.rockMesh.getTile(tilex, tiley);
    return Tiles.decodeHeight(rockTile);
  }
  
  private static final boolean isNullWall(int tile)
  {
    byte cavetype = Tiles.decodeType(tile);
    if (!Tiles.isSolidCave(cavetype)) {
      return false;
    }
    return (Tiles.decodeHeight(tile) == -100) && ((Tiles.decodeData(tile) & 0xFF) == 0);
  }
  
  private static final int[] getFloorAndCeiling(int tilex, int tiley, int fromHeight, int mod, boolean tryZeroCeiling, boolean tryCeilingAtRockHeight, Creature performer)
  {
    int targetFloor = fromHeight + mod;
    boolean fixedHeight = false;
    for (int x = -1; x <= 0; x++) {
      for (int y = -1; y <= 0; y++)
      {
        VolaTile vt = Zones.getTileOrNull(tilex + x, tiley + y, false);
        if ((vt != null) && (vt.getStructure() != null))
        {
          fixedHeight = true;
          int tile = Server.caveMesh.getTile(tilex + x, tiley + y);
          targetFloor = Tiles.decodeHeight(tile);
        }
      }
    }
    int targetCeiling = targetFloor + 30;
    if ((!tryZeroCeiling) && (!tryCeilingAtRockHeight) && (!fixedHeight))
    {
      if (Server.rand.nextInt(5) == 0) {
        targetCeiling = maybeAddExtraSlopes(performer, targetCeiling);
      }
      if (Server.rand.nextInt(5) == 0) {
        targetFloor = maybeAddExtraSlopes(performer, targetFloor);
      }
    }
    else if (tryZeroCeiling)
    {
      targetCeiling = targetFloor;
    }
    int rockHeight = getRockHeight(tilex, tiley);
    int tile = Server.caveMesh.getTile(tilex, tiley);
    int currentFloor = Tiles.decodeHeight(tile);
    int currentCeiling = currentFloor + (Tiles.decodeData(tile) & 0xFF);
    if ((targetFloor >= currentFloor) && (!isNullWall(tile))) {
      targetFloor = currentFloor;
    }
    if (targetCeiling <= currentCeiling)
    {
      targetCeiling = currentCeiling;
      if ((mod > 0) && (targetFloor < currentFloor) && (!isNullWall(tile))) {
        targetFloor = currentFloor;
      }
    }
    if ((targetCeiling >= rockHeight) || (tryCeilingAtRockHeight)) {
      targetCeiling = rockHeight;
    }
    if (targetFloor >= rockHeight) {
      targetFloor = rockHeight;
    }
    if (targetCeiling - targetFloor >= 255) {
      if (targetFloor < currentFloor)
      {
        targetFloor = currentCeiling - 255;
        targetCeiling = currentCeiling;
      }
      else
      {
        targetCeiling = Math.min(currentCeiling, targetFloor + 255);
      }
    }
    if ((targetCeiling < 5) && (!tryZeroCeiling)) {
      targetCeiling = 5;
    }
    return new int[] { targetFloor, targetCeiling };
  }
  
  private static final int maybeAddExtraSlopes(Creature performer, int _previousValue)
  {
    if (performer.getPower() > 0) {
      return _previousValue;
    }
    int miningSkillMod;
    int miningSkillMod;
    if ((performer instanceof Player))
    {
      Player p = (Player)performer;
      
      Skill mine = null;
      try
      {
        Skills skills = p.getSkills();
        mine = skills.getSkill(1008);
      }
      catch (NoSuchSkillException nss)
      {
        logger.info(performer.getName() + ": No such skill for mining? " + nss);
      }
      double realKnowledge;
      double realKnowledge;
      if (mine == null) {
        realKnowledge = 1.0D;
      } else {
        realKnowledge = mine.getKnowledge(0.0D);
      }
      if (realKnowledge > 90.0D) {
        return _previousValue;
      }
      int miningSkillMod;
      if (realKnowledge > 70.0D)
      {
        miningSkillMod = 1;
      }
      else
      {
        int miningSkillMod;
        if (realKnowledge > 50.0D) {
          miningSkillMod = 2;
        } else {
          miningSkillMod = 3;
        }
      }
    }
    else
    {
      miningSkillMod = 3;
    }
    int randVal = Server.rand.nextInt(miningSkillMod * 2 + 1);
    
    return _previousValue - miningSkillMod + randVal;
  }
  
  private static final void maybeCreateSource(int tilex, int tiley, Creature performer)
  {
    if ((Server.rand.nextInt(10000) == 0) || ((Servers.localServer.testServer) && 
      (performer.getPower() >= 5) && (Server.rand.nextInt(10) == 0))) {
      if ((!Servers.localServer.EPIC) || (!Servers.localServer.HOMESERVER)) {
        if ((Items.getSourceSprings().length > 0) && (Items.getSourceSprings().length < Zones.worldTileSizeX / 20)) {
          try
          {
            Item target1 = ItemFactory.createItem(767, 100.0F, tilex * 4 + 2, tiley * 4 + 2, Server.rand
              .nextInt(360), false, (byte)0, -10L, "");
            
            target1.setSizes(target1.getSizeX() + Server.rand.nextInt(1), target1
              .getSizeY() + Server.rand.nextInt(2), target1.getSizeZ() + Server.rand.nextInt(3));
            logger.log(Level.INFO, "Created " + target1
              .getName() + " at " + target1.getTileX() + " " + target1.getTileY() + " sizes " + target1
              .getSizeX() + "," + target1.getSizeY() + "," + target1.getSizeZ() + ")");
            
            Items.addSourceSpring(target1);
            performer.getCommunicator().sendSafeServerMessage("You find a source spring!");
          }
          catch (FailedException fe)
          {
            logger.log(Level.WARNING, fe.getMessage(), fe);
          }
          catch (NoSuchTemplateException nst)
          {
            logger.log(Level.WARNING, nst.getMessage(), nst);
          }
        }
      }
    }
  }
  
  private static final boolean createStandardTunnel(int tilex, int tiley, int tile, Creature performer, int action, int direction, boolean disintegrate, @Nullable Action act)
  {
    if ((Tiles.decodeType(tile) == Tiles.Tile.TILE_CAVE_WALL.id) || (Tiles.decodeType(tile) == Tiles.Tile.TILE_CAVE_WALL_ROCKSALT.id) || 
      (Server.getCaveResource(tilex, tiley) <= 0) || (disintegrate)) {
      if (areAllTilesRockOrReinforcedRock(tilex, tiley, tile, direction, false, false))
      {
        if ((Tiles.decodeHeight(tile) >= -25) || (Tiles.decodeHeight(tile) == -100))
        {
          int dir = 6;
          if (direction == 3) {
            dir = 0;
          } else if (direction == 5) {
            dir = 4;
          } else if (direction == 4) {
            dir = 2;
          }
          int mod = 0;
          if (action == 147) {
            mod = -20;
          } else if (action == 146) {
            mod = 20;
          }
          if (disintegrate) {
            Server.setCaveResource(tilex, tiley, 0);
          }
          if (dir == 0)
          {
            int fromx = tilex;
            int fromy = tiley + 1;
            int t = Server.caveMesh.getTile(fromx, fromy);
            short height = Tiles.decodeHeight(t);
            
            int fromx2 = tilex + 1;
            int fromy2 = tiley + 1;
            int t2 = Server.caveMesh.getTile(fromx2, fromy2);
            short height2 = Tiles.decodeHeight(t2);
            short avheight = (short)((height + height2) / 2);
            
            int[] newfloorceil = getFloorAndCeiling(tilex, tiley, avheight, mod, false, false, performer);
            
            int newFloorHeight = newfloorceil[0];
            if (newFloorHeight < -25) {
              newFloorHeight = -25;
            }
            int newCeil = newfloorceil[1];
            
            Server.caveMesh.setTile(tilex, tiley, 
              Tiles.encode((short)newFloorHeight, Tiles.Tile.TILE_CAVE.id, (byte)(newCeil - newFloorHeight)));
            
            maybeCreateSource(tilex, tiley, performer);
            
            t2 = Server.caveMesh.getTile(tilex + 1, tiley);
            newfloorceil = getFloorAndCeiling(tilex + 1, tiley, avheight, mod, false, false, performer);
            
            newFloorHeight = newfloorceil[0];
            if (newFloorHeight < -25) {
              newFloorHeight = -25;
            }
            newCeil = newfloorceil[1];
            
            Server.caveMesh.setTile(tilex + 1, tiley, 
              Tiles.encode((short)newFloorHeight, Tiles.decodeType(t2), (byte)(newCeil - newFloorHeight)));
            
            sendCaveTile(tilex, tiley, 0, 0);
          }
          else if (dir == 4)
          {
            int fromx = tilex;
            int fromy = tiley;
            int t = Server.caveMesh.getTile(fromx, fromy);
            short height = Tiles.decodeHeight(t);
            Server.caveMesh.setTile(tilex, tiley, Tiles.encode(height, Tiles.Tile.TILE_CAVE.id, Tiles.decodeData(t)));
            
            maybeCreateSource(tilex, tiley, performer);
            
            int fromx2 = tilex + 1;
            int fromy2 = tiley;
            int t2 = Server.caveMesh.getTile(fromx2, fromy2);
            short height2 = Tiles.decodeHeight(t2);
            short avheight = (short)((height + height2) / 2);
            
            t2 = Server.caveMesh.getTile(tilex, tiley + 1);
            int[] newfloorceil = getFloorAndCeiling(tilex, tiley + 1, avheight, mod, false, false, performer);
            int newFloorHeight = newfloorceil[0];
            if (newFloorHeight < -25) {
              newFloorHeight = -25;
            }
            int newCeil = newfloorceil[1];
            
            Server.caveMesh.setTile(tilex, tiley + 1, 
              Tiles.encode((short)newFloorHeight, Tiles.decodeType(t2), (byte)(newCeil - newFloorHeight)));
            
            t2 = Server.caveMesh.getTile(tilex + 1, tiley + 1);
            newfloorceil = getFloorAndCeiling(tilex + 1, tiley + 1, avheight, mod, false, false, performer);
            newFloorHeight = newfloorceil[0];
            if (newFloorHeight < -25) {
              newFloorHeight = -25;
            }
            newCeil = newfloorceil[1];
            
            Server.caveMesh.setTile(tilex + 1, tiley + 1, 
              Tiles.encode((short)newFloorHeight, Tiles.decodeType(t2), (byte)(newCeil - newFloorHeight)));
            
            sendCaveTile(tilex, tiley, 0, 0);
          }
          else if (dir == 2)
          {
            int fromx = tilex;
            int fromy = tiley;
            int t = Server.caveMesh.getTile(fromx, fromy);
            short height = Tiles.decodeHeight(t);
            Server.caveMesh.setTile(tilex, tiley, Tiles.encode(height, Tiles.Tile.TILE_CAVE.id, Tiles.decodeData(t)));
            
            maybeCreateSource(tilex, tiley, performer);
            
            int fromx2 = tilex;
            int fromy2 = tiley + 1;
            int t2 = Server.caveMesh.getTile(fromx2, fromy2);
            short height2 = Tiles.decodeHeight(t2);
            short avheight = (short)((height + height2) / 2);
            
            t2 = Server.caveMesh.getTile(tilex + 1, tiley);
            int[] newfloorceil = getFloorAndCeiling(tilex + 1, tiley, avheight, mod, false, false, performer);
            int newFloorHeight = newfloorceil[0];
            if (newFloorHeight < -25) {
              newFloorHeight = -25;
            }
            int newCeil = newfloorceil[1];
            
            Server.caveMesh.setTile(tilex + 1, tiley, 
              Tiles.encode((short)newFloorHeight, Tiles.decodeType(t2), (byte)(newCeil - newFloorHeight)));
            
            t2 = Server.caveMesh.getTile(tilex + 1, tiley + 1);
            newfloorceil = getFloorAndCeiling(tilex + 1, tiley + 1, avheight, mod, false, false, performer);
            newFloorHeight = newfloorceil[0];
            if (newFloorHeight < -25) {
              newFloorHeight = -25;
            }
            newCeil = newfloorceil[1];
            
            Server.caveMesh.setTile(tilex + 1, tiley + 1, 
              Tiles.encode((short)newFloorHeight, Tiles.decodeType(t2), (byte)(newCeil - newFloorHeight)));
            
            sendCaveTile(tilex, tiley, 0, 0);
          }
          else if (dir == 6)
          {
            int fromx = tilex + 1;
            int fromy = tiley;
            int t = Server.caveMesh.getTile(fromx, fromy);
            short height = Tiles.decodeHeight(t);
            
            int fromx2 = tilex + 1;
            int fromy2 = tiley + 1;
            int t2 = Server.caveMesh.getTile(fromx2, fromy2);
            short height2 = Tiles.decodeHeight(t2);
            short avheight = (short)((height + height2) / 2);
            
            int[] newfloorceil = getFloorAndCeiling(tilex, tiley, avheight, mod, false, false, performer);
            int newFloorHeight = newfloorceil[0];
            if (newFloorHeight < -25) {
              newFloorHeight = -25;
            }
            int newCeil = newfloorceil[1];
            
            Server.caveMesh.setTile(tilex, tiley, 
              Tiles.encode((short)newFloorHeight, Tiles.Tile.TILE_CAVE.id, (byte)(newCeil - newFloorHeight)));
            
            maybeCreateSource(tilex, tiley, performer);
            
            t2 = Server.caveMesh.getTile(tilex, tiley + 1);
            newfloorceil = getFloorAndCeiling(tilex, tiley + 1, avheight, mod, false, false, performer);
            newFloorHeight = newfloorceil[0];
            if (newFloorHeight < -25) {
              newFloorHeight = -25;
            }
            newCeil = newfloorceil[1];
            
            Server.caveMesh.setTile(tilex, tiley + 1, 
              Tiles.encode((short)newFloorHeight, Tiles.decodeType(t2), (byte)(newCeil - newFloorHeight)));
            
            sendCaveTile(tilex, tiley, 0, 0);
          }
          if (!performer.isPlayer())
          {
            Item gem = createGem(-1, -1, performer, Server.rand.nextFloat() * 100.0F, false, act);
            if (gem != null) {
              performer.getInventory().insertItem(gem);
            }
          }
        }
      }
      else {
        return false;
      }
    }
    return true;
  }
  
  public static final void sendCaveTile(int tilex, int tiley, int diffX, int diffY)
  {
    Players.getInstance().sendChangedTile(tilex + diffX, tiley + diffY, false, true);
    for (int x = -1; x <= 0; x++) {
      for (int y = -1; y <= 0; y++) {
        try
        {
          Zone toCheckForChange = Zones.getZone(tilex + diffX + x, tiley + diffY + y, false);
          toCheckForChange.changeTile(tilex + diffX + x, tiley + diffY + y);
        }
        catch (NoSuchZoneException nsz)
        {
          logger.log(Level.INFO, "no such zone?: " + (tilex + diffX + x) + ", " + (tiley + diffY + y), nsz);
        }
      }
    }
  }
  
  public static final boolean surroundedByWalls(int x, int y)
  {
    if (!Tiles.isSolidCave(Tiles.decodeType(Server.caveMesh.getTile(x - 1, y)))) {
      return false;
    }
    if (!Tiles.isSolidCave(Tiles.decodeType(Server.caveMesh.getTile(x + 1, y)))) {
      return false;
    }
    if (!Tiles.isSolidCave(Tiles.decodeType(Server.caveMesh.getTile(x, y - 1)))) {
      return false;
    }
    if (!Tiles.isSolidCave(Tiles.decodeType(Server.caveMesh.getTile(x, y + 1)))) {
      return false;
    }
    return true;
  }
  
  public static final void reProspect()
  {
    int numsChanged = 0;
    int numsUntouched = 0;
    for (int x = 0; x < (1 << Constants.meshSize) * (1 << Constants.meshSize); x++)
    {
      int xx = x & (1 << Constants.meshSize) - 1;
      int yy = x >> Constants.meshSize;
      
      int old = Server.caveMesh.getTile(xx, yy);
      if (Tiles.isOreCave(Tiles.decodeType(old))) {
        if ((xx > 5) && (yy > 5) && (xx < worldSizeX - 3) && (yy < worldSizeX - 3)) {
          if (surroundedByWalls(xx, yy))
          {
            byte newType = prospect(xx, yy, true);
            Server.caveMesh.setTile(xx, yy, Tiles.encode(Tiles.decodeHeight(old), newType, Tiles.decodeData(old)));
            numsChanged++;
          }
          else
          {
            numsUntouched++;
          }
        }
      }
    }
    logger.log(Level.INFO, "Reprospect finished. Changed=" + numsChanged + ", untouched=" + numsUntouched);
    try
    {
      Server.caveMesh.saveAllDirtyRows();
    }
    catch (IOException iox)
    {
      logger.log(Level.WARNING, iox.getMessage(), iox);
    }
  }
  
  public static final Item createRandomGem()
  {
    return createRandomGem(100.0F);
  }
  
  public static final Item createRandomGem(float maxql)
  {
    try
    {
      int rand = Server.rand.nextInt(300);
      int templateId = 349;
      float ql = Server.rand.nextFloat() * maxql;
      if (rand < 50)
      {
        templateId = 349;
      }
      else if (rand < 100)
      {
        templateId = 446;
      }
      else if (rand < 140)
      {
        templateId = 376;
        if (ql >= 99.0F) {
          templateId = 377;
        }
      }
      else if (rand < 180)
      {
        templateId = 374;
        if (ql >= 99.0F) {
          templateId = 375;
        }
      }
      else if (rand < 220)
      {
        templateId = 382;
        if (ql >= 99.0F) {
          templateId = 383;
        }
      }
      else if (rand < 260)
      {
        templateId = 378;
        if (ql >= 99.0F) {
          templateId = 379;
        }
      }
      else if (rand < 300)
      {
        templateId = 380;
        if (ql >= 99.0F) {
          templateId = 381;
        }
      }
      return ItemFactory.createItem(templateId, Server.rand.nextFloat() * ql, null);
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
  
  static final Item createGem(int minedTilex, int minedTiley, Creature performer, double power, boolean surfaced, @Nullable Action act)
  {
    return createGem(minedTilex, minedTiley, minedTilex, minedTiley, performer, power, surfaced, act);
  }
  
  static final Item createGem(int tilex, int tiley, int createtilex, int createtiley, Creature performer, double power, boolean surfaced, @Nullable Action act)
  {
    byte rarity = act != null ? act.getRarity() : 0;
    try
    {
      rockRandom.setSeed((tilex + tiley * Zones.worldTileSizeY) * 102533L);
      if (rockRandom.nextInt(100) == 0) {
        if (Server.rand.nextInt(10) == 0)
        {
          if ((tilex < 0) && (tiley < 0))
          {
            Item gem = ItemFactory.createItem(349, (float)power, null);
            gem.setLastOwnerId(performer.getWurmId());
            return gem;
          }
          Item salt = ItemFactory.createItem(349, (float)power, rarity, null);
          salt.setLastOwnerId(performer.getWurmId());
          salt.putItemInfrontof(performer, 0.0F);
          
          performer.getCommunicator().sendNormalServerMessage("You mine some salt.");
        }
      }
      rockRandom.setSeed((tilex + tiley * Zones.worldTileSizeY) * SOURCE_PRIME);
      if (rockRandom.nextInt(sourceFactor) == 0)
      {
        boolean isVein = Tiles.isOreCave(Tiles.decodeType(Server.caveMesh.getTile(tilex, tiley)));
        if ((Server.rand.nextInt(10) == 0) && (!isVein))
        {
          if ((tilex < 0) && (tiley < 0))
          {
            Item gem = ItemFactory.createItem(765, (float)power, null);
            gem.setLastOwnerId(performer.getWurmId());
            return gem;
          }
          Item crystal = ItemFactory.createItem(765, (float)power, rarity, null);
          crystal.setLastOwnerId(performer.getWurmId());
          crystal.putItemInfrontof(performer, 0.0F);
          
          performer.getCommunicator().sendNormalServerMessage("You mine some pink crystals.");
        }
      }
      rockRandom.setSeed((tilex + tiley * Zones.worldTileSizeY) * 6883L);
      if (rockRandom.nextInt(200) == 0) {
        if (Server.rand.nextInt(40) == 0)
        {
          if ((tilex < 0) && (tiley < 0))
          {
            Item gem = ItemFactory.createItem(446, (float)power, null);
            
            gem.setLastOwnerId(performer.getWurmId());
            return gem;
          }
          Item flint = ItemFactory.createItem(446, (float)power, rarity, null);
          flint.setLastOwnerId(performer.getWurmId());
          flint.putItemInfrontof(performer, 0.0F);
          
          performer.getCommunicator().sendNormalServerMessage("You find flint!");
        }
      }
      if (Server.rand.nextInt(1000) == 0)
      {
        int rand = Server.rand.nextInt(5);
        if (rand == 0)
        {
          int templateId = 376;
          float ql = Math.min(MAX_QL, Server.rand.nextFloat() * 100.0F);
          if (ql >= 99.0F) {
            templateId = 377;
          }
          if ((tilex < 0) && (tiley < 0))
          {
            Item gem = ItemFactory.createItem(templateId, (float)power, null);
            
            gem.setLastOwnerId(performer.getWurmId());
            return gem;
          }
          Item gem = ItemFactory.createItem(templateId, (float)power, rarity, null);
          gem.setLastOwnerId(performer.getWurmId());
          gem.putItemInfrontof(performer, 0.0F);
          if (ql >= 99.0F) {
            performer.achievement(298);
          }
          if (gem.getQualityLevel() > 90.0F) {
            performer.achievement(299);
          }
          if (rarity > 2) {
            performer.achievement(334);
          }
          performer.getCommunicator().sendNormalServerMessage("You find " + gem.getNameWithGenus() + "!");
        }
        else if (rand == 1)
        {
          int templateId = 374;
          float ql = Math.min(MAX_QL, Server.rand.nextFloat() * 100.0F);
          if (ql >= 99.0F) {
            templateId = 375;
          }
          if ((tilex < 0) && (tiley < 0))
          {
            Item gem = ItemFactory.createItem(templateId, (float)power, null);
            
            gem.setLastOwnerId(performer.getWurmId());
            return gem;
          }
          Item gem = ItemFactory.createItem(templateId, (float)power, rarity, null);
          gem.setLastOwnerId(performer.getWurmId());
          gem.putItemInfrontof(performer, 0.0F);
          if (ql >= 99.0F) {
            performer.achievement(298);
          }
          if (gem.getQualityLevel() > 90.0F) {
            performer.achievement(299);
          }
          if (rarity > 2) {
            performer.achievement(334);
          }
          performer.getCommunicator().sendNormalServerMessage("You find " + gem.getNameWithGenus() + "!");
        }
        else if (rand == 2)
        {
          int templateId = 382;
          float ql = Math.min(MAX_QL, Server.rand.nextFloat() * 100.0F);
          if (ql >= 99.0F) {
            templateId = 383;
          }
          if ((tilex < 0) && (tiley < 0))
          {
            Item gem = ItemFactory.createItem(templateId, (float)power, null);
            
            gem.setLastOwnerId(performer.getWurmId());
            return gem;
          }
          Item gem = ItemFactory.createItem(templateId, (float)power, rarity, null);
          gem.setLastOwnerId(performer.getWurmId());
          gem.putItemInfrontof(performer, 0.0F);
          if (ql >= 99.0F) {
            performer.achievement(298);
          }
          if (gem.getQualityLevel() > 90.0F) {
            performer.achievement(299);
          }
          if (rarity > 2) {
            performer.achievement(334);
          }
          performer.getCommunicator().sendNormalServerMessage("You find " + gem.getNameWithGenus() + "!");
        }
        else if (rand == 3)
        {
          int templateId = 378;
          float ql = Math.min(MAX_QL, Server.rand.nextFloat() * 100.0F);
          if (ql >= 99.0F) {
            templateId = 379;
          }
          if ((tilex < 0) && (tiley < 0))
          {
            Item gem = ItemFactory.createItem(templateId, (float)power, null);
            
            gem.setLastOwnerId(performer.getWurmId());
            return gem;
          }
          Item gem = ItemFactory.createItem(templateId, (float)power, rarity, null);
          gem.setLastOwnerId(performer.getWurmId());
          gem.putItemInfrontof(performer, 0.0F);
          if (ql >= 99.0F) {
            performer.achievement(298);
          }
          if (gem.getQualityLevel() > 90.0F) {
            performer.achievement(299);
          }
          if (rarity > 2) {
            performer.achievement(334);
          }
          performer.getCommunicator().sendNormalServerMessage("You find " + gem.getNameWithGenus() + "!");
        }
        else
        {
          int templateId = 380;
          float ql = Math.min(MAX_QL, Server.rand.nextFloat() * 100.0F);
          if (ql >= 99.0F) {
            templateId = 381;
          }
          if ((tilex < 0) && (tiley < 0))
          {
            Item gem = ItemFactory.createItem(templateId, (float)power, null);
            
            gem.setLastOwnerId(performer.getWurmId());
            return gem;
          }
          Item gem = ItemFactory.createItem(templateId, (float)power, rarity, null);
          gem.setLastOwnerId(performer.getWurmId());
          gem.putItemInfrontof(performer, 0.0F);
          if (ql >= 99.0F) {
            performer.achievement(298);
          }
          if (gem.getQualityLevel() > 90.0F) {
            performer.achievement(299);
          }
          if (rarity > 2) {
            performer.achievement(334);
          }
          performer.getCommunicator().sendNormalServerMessage("You find " + gem.getNameWithGenus() + "!");
        }
      }
    }
    catch (FailedException fe)
    {
      logger.log(Level.WARNING, performer.getName() + ": " + fe.getMessage(), fe);
    }
    catch (NoSuchTemplateException nst)
    {
      logger.log(Level.WARNING, performer.getName() + ": no template", nst);
    }
    catch (Exception ex)
    {
      logger.log(Level.WARNING, "Factory failed to produce item", ex);
    }
    return null;
  }
  
  public static boolean cannotMineSlope(Creature performer, Skill mining, int digTilex, int digTiley)
  {
    int diff = Terraforming.getMaxSurfaceDifference(Server.surfaceMesh.getTile(digTilex, digTiley), digTilex, digTiley);
    
    int maxSlope = (int)(mining.getKnowledge(0.0D) * (Servers.localServer.PVPSERVER ? 1 : 3));
    if ((Math.signum(diff) == 1.0F) && (diff > maxSlope))
    {
      performer.getCommunicator().sendNormalServerMessage("You are too unskilled to mine here.", (byte)3);
      return true;
    }
    if ((Math.signum(diff) == -1.0F) && (-1 - diff > maxSlope))
    {
      performer.getCommunicator().sendNormalServerMessage("You are too unskilled to mine here.", (byte)3);
      return true;
    }
    return false;
  }
  
  public static final boolean mine(Action act, Creature performer, Item source, int tilex, int tiley, short action, float counter, int digTilex, int digTiley)
  {
    boolean done = true;
    int tile = Server.surfaceMesh.getTile(digTilex, digTiley);
    if ((digTilex < 1) || (digTilex > (1 << Constants.meshSize) - 1) || (digTiley < 1) || (digTiley > (1 << Constants.meshSize) - 1))
    {
      performer.getCommunicator().sendNormalServerMessage("The water is too deep to mine.", (byte)3);
      return true;
    }
    if (Zones.isTileProtected(digTilex, digTiley))
    {
      performer.getCommunicator().sendNormalServerMessage("This tile is protected by the gods. You can not mine here.", (byte)3);
      
      return true;
    }
    short h = Tiles.decodeHeight(tile);
    if (h > -25)
    {
      done = false;
      Skills skills = performer.getSkills();
      Skill mining = null;
      Skill tool = null;
      boolean insta = (performer.getPower() > 3) && (source.isWand());
      try
      {
        mining = skills.getSkill(1008);
      }
      catch (Exception ex)
      {
        mining = skills.learn(1008, 1.0F);
      }
      try
      {
        tool = skills.getSkill(source.getPrimarySkill());
      }
      catch (Exception ex)
      {
        try
        {
          tool = skills.learn(source.getPrimarySkill(), 1.0F);
        }
        catch (NoSuchSkillException nse)
        {
          logger.log(Level.WARNING, performer.getName() + " trying to mine with an item with no primary skill: " + source
            .getName());
        }
      }
      for (int x = -1; x <= 0; x++) {
        for (int y = -1; y <= 0; y++)
        {
          byte decType = Tiles.decodeType(Server.surfaceMesh.getTile(digTilex + x, digTiley + y));
          if ((decType != Tiles.Tile.TILE_ROCK.id) && (decType != Tiles.Tile.TILE_CLIFF.id))
          {
            performer.getCommunicator().sendNormalServerMessage("The surrounding area needs to be rock before you mine.", (byte)3);
            
            return true;
          }
        }
      }
      int y;
      VolaTile vt;
      for (int x = 0; x >= -1; x--) {
        for (y = 0; y >= -1; y--)
        {
          vt = Zones.getTileOrNull(digTilex + x, digTiley + y, true);
          if ((vt != null) && (vt.getStructure() != null))
          {
            if (vt.getStructure().isTypeHouse())
            {
              if ((x == 0) && (y == 0)) {
                performer.getCommunicator().sendNormalServerMessage("You cannot mine in a building.", (byte)3);
              } else {
                performer.getCommunicator().sendNormalServerMessage("You cannot mine next to a building.", (byte)3);
              }
              return true;
            }
            for (BridgePart bp : vt.getBridgeParts())
            {
              if (bp.getType().isSupportType())
              {
                performer.getCommunicator().sendNormalServerMessage("The bridge support nearby prevents mining.");
                return true;
              }
              if (((x == -1) && (bp.hasEastExit())) || ((x == 0) && 
                (bp.hasWestExit())) || ((y == -1) && 
                (bp.hasSouthExit())) || ((y == 0) && 
                (bp.hasNorthExit())))
              {
                performer.getCommunicator().sendNormalServerMessage("The end of the bridge nearby prevents mining.");
                return true;
              }
            }
          }
        }
      }
      VolaTile vt = Zones.getTileOrNull(digTilex, digTiley, true);
      if ((vt != null) && (vt.getFencesForLevel(0).length > 0))
      {
        performer.getCommunicator().sendNormalServerMessage("You cannot mine next to a fence.", (byte)3);
        
        return true;
      }
      vt = Zones.getTileOrNull(digTilex, digTiley - 1, true);
      if ((vt != null) && (vt.getFencesForLevel(0).length > 0))
      {
        y = vt.getFencesForLevel(0);vt = y.length;
        for (VolaTile localVolaTile1 = 0; localVolaTile1 < vt; localVolaTile1++)
        {
          Fence f = y[localVolaTile1];
          if (!f.isHorizontal())
          {
            performer.getCommunicator().sendNormalServerMessage("You cannot mine next to a fence.", (byte)3);
            
            return true;
          }
        }
      }
      vt = Zones.getTileOrNull(digTilex - 1, digTiley, true);
      if ((vt != null) && (vt.getFencesForLevel(0).length > 0))
      {
        y = vt.getFencesForLevel(0);vt = y.length;
        for (VolaTile localVolaTile2 = 0; localVolaTile2 < vt; localVolaTile2++)
        {
          Fence f = y[localVolaTile2];
          if (f.isHorizontal())
          {
            performer.getCommunicator().sendNormalServerMessage("You cannot mine next to a fence.", (byte)3);
            
            return true;
          }
        }
      }
      int time = 0;
      VolaTile dropTile = Zones.getTileOrNull((int)performer.getPosX() >> 2, (int)performer.getPosY() >> 2, true);
      if (dropTile != null) {
        if (dropTile.getNumberOfItems(performer.getFloorLevel()) > 99)
        {
          performer.getCommunicator().sendNormalServerMessage("There is no space to mine here. Clear the area first.", (byte)3);
          
          return true;
        }
      }
      if (counter == 1.0F)
      {
        if (cannotMineSlope(performer, mining, digTilex, digTiley)) {
          return true;
        }
        time = Actions.getStandardActionTime(performer, mining, source, 0.0D);
        try
        {
          performer.getCurrentAction().setTimeLeft(time);
        }
        catch (NoSuchActionException nsa)
        {
          logger.log(Level.INFO, "This action does not exist?", nsa);
        }
        Server.getInstance().broadCastAction(performer.getName() + " starts mining.", performer, 5);
        performer.getCommunicator().sendNormalServerMessage("You start to mine.");
        performer.sendActionControl(Actions.actionEntrys[''].getVerbString(), true, time);
        
        source.setDamage(source.getDamage() + 0.0015F * source.getDamageModifier());
        performer.getStatus().modifyStamina(-1000.0F);
      }
      else
      {
        try
        {
          time = performer.getCurrentAction().getTimeLeft();
        }
        catch (NoSuchActionException nsa)
        {
          logger.log(Level.INFO, "This action does not exist?", nsa);
        }
        if ((counter * 10.0F <= time) && (!insta))
        {
          if ((act.currentSecond() % 5 == 0) || ((act.currentSecond() == 3) && (time < 50)))
          {
            String sstring = "sound.work.mining1";
            int x = Server.rand.nextInt(3);
            if (x == 0) {
              sstring = "sound.work.mining2";
            } else if (x == 1) {
              sstring = "sound.work.mining3";
            }
            SoundPlayer.playSound(sstring, digTilex, digTiley, performer.isOnSurface(), 0.0F);
            source.setDamage(source.getDamage() + 0.0015F * source.getDamageModifier());
            performer.getStatus().modifyStamina(-7000.0F);
          }
        }
        else
        {
          if (act.getRarity() != 0) {
            performer.playPersonalSound("sound.fx.drumroll");
          }
          if (cannotMineSlope(performer, mining, digTilex, digTiley)) {
            return true;
          }
          double bonus = 0.0D;
          double power = 0.0D;
          done = true;
          int itemTemplateCreated = 146;
          float diff = 1.0F;
          
          int caveTile = Server.caveMesh.getTile(digTilex, digTiley);
          short caveFloor = Tiles.decodeHeight(caveTile);
          int caveCeilingHeight = caveFloor + (short)(Tiles.decodeData(caveTile) & 0xFF);
          
          MeshIO mesh = Server.surfaceMesh;
          if (h - 1 <= caveCeilingHeight)
          {
            performer.getCommunicator().sendNormalServerMessage("The rock sounds hollow. You need to tunnel to proceed.", (byte)3);
            
            return true;
          }
          double imbueEnhancement = 1.0D + 0.23047D * source.getSkillSpellImprovement(1008) / 100.0D;
          
          int lNewTile = mesh.getTile(digTilex - 1, digTiley);
          short maxDiff = (short)(int)Math.max(10.0D, mining.getKnowledge(0.0D) * 3.0D * imbueEnhancement);
          if (Terraforming.checkMineSurfaceTile(lNewTile, performer, h, maxDiff)) {
            return true;
          }
          lNewTile = mesh.getTile(digTilex + 1, digTiley);
          if (Terraforming.checkMineSurfaceTile(lNewTile, performer, h, maxDiff)) {
            return true;
          }
          lNewTile = mesh.getTile(digTilex, digTiley - 1);
          if (Terraforming.checkMineSurfaceTile(lNewTile, performer, h, maxDiff)) {
            return true;
          }
          lNewTile = mesh.getTile(digTilex, digTiley + 1);
          if (Terraforming.checkMineSurfaceTile(lNewTile, performer, h, maxDiff)) {
            return true;
          }
          if (Terraforming.isAltarBlocking(performer, tilex, tiley))
          {
            performer.getCommunicator().sendSafeServerMessage("You cannot build here, since this is holy ground.", (byte)2);
            
            return true;
          }
          if ((performer.getTutorialLevel() == 10) && (!performer.skippedTutorial())) {
            performer.missionFinished(true, true);
          }
          float tickCounter = counter;
          if (tool != null) {
            bonus = tool.skillCheck(1.0D, source, 0.0D, false, tickCounter) / 5.0D;
          }
          power = Math.max(1.0D, mining
            .skillCheck(1.0D, source, bonus, false, tickCounter));
          float chance = Math.max(0.2F, (float)mining.getKnowledge(0.0D) / 200.0F);
          if (Server.rand.nextFloat() < chance) {
            try
            {
              if (mining.getKnowledge(0.0D) * imbueEnhancement < power) {
                power = mining.getKnowledge(0.0D) * imbueEnhancement;
              }
              rockRandom.setSeed((digTilex + digTiley * Zones.worldTileSizeY) * 789221L);
              int m = 100;
              int max = Math.min(100, 
                (int)(20.0D + rockRandom.nextInt(80) * imbueEnhancement));
              power = Math.min(power, max);
              if (source.isCrude()) {
                power = 1.0D;
              }
              float modifier = 1.0F;
              if (source.getSpellEffects() != null) {
                modifier *= source.getSpellEffects().getRuneEffect(RuneUtilities.ModifierEffect.ENCH_RESGATHERED);
              }
              float orePower = GeneralUtilities.calcOreRareQuality(power * modifier, act.getRarity(), source.getRarity());
              
              Item newItem = ItemFactory.createItem(146, orePower, performer
              
                .getPosX(), performer.getPosY(), Server.rand.nextFloat() * 360.0F, performer
                .isOnSurface(), act.getRarity(), -10L, null);
              newItem.setLastOwnerId(performer.getWurmId());
              newItem.setDataXY(tilex, tiley);
              performer.getCommunicator().sendNormalServerMessage("You mine some " + newItem.getName() + ".");
              Server.getInstance().broadCastAction(performer
                .getName() + " mines some " + newItem.getName() + ".", performer, 5);
              
              TileEvent.log(digTilex, digTiley, 0, performer.getWurmId(), action);
              
              short newHeight = (short)(h - 1);
              mesh.setTile(digTilex, digTiley, 
                Tiles.encode(newHeight, Tiles.Tile.TILE_ROCK.id, Tiles.decodeData(tile)));
              Server.rockMesh.setTile(digTilex, digTiley, Tiles.encode(newHeight, (short)0));
              for (int xx = 0; xx >= -1; xx--) {
                for (int yy = 0; yy >= -1; yy--)
                {
                  performer.getMovementScheme().touchFreeMoveCounter();
                  Players.getInstance().sendChangedTile(digTilex + xx, digTiley + yy, performer
                    .isOnSurface(), true);
                  try
                  {
                    Zone toCheckForChange = Zones.getZone(digTilex + xx, digTiley + yy, performer
                      .isOnSurface());
                    toCheckForChange.changeTile(digTilex + xx, digTiley + yy);
                  }
                  catch (NoSuchZoneException nsz)
                  {
                    logger.log(Level.INFO, "no such zone?: " + tilex + ", " + tiley, nsz);
                  }
                }
              }
            }
            catch (Exception ex)
            {
              logger.log(Level.WARNING, "Factory failed to produce item", ex);
            }
          } else {
            performer.getCommunicator().sendNormalServerMessage("You chip away at the rock.");
          }
        }
      }
    }
    else
    {
      performer.getCommunicator().sendNormalServerMessage("The water is too deep to mine.", (byte)3);
    }
    return done;
  }
  
  public static final byte prospect(int x, int y, boolean reprospecting)
  {
    oreRand = Server.rand.nextInt(reprospecting ? 75 : 1000);
    if (oreRand < 74)
    {
      if (reprospecting)
      {
        if (minezones[(x / 32)][(y / 32)] != Tiles.Tile.TILE_CAVE_WALL.id)
        {
          if (Server.rand.nextInt(5) == 0) {
            return getOreId(oreRand);
          }
          return minezones[(x / 32)][(y / 32)];
        }
        return getOreId(oreRand);
      }
      if (Server.rand.nextInt(5) == 0) {
        return getOreId(oreRand);
      }
      byte type = minezones[(x / 32)][(y / 32)];
      
      return type;
    }
    return Tiles.Tile.TILE_CAVE_WALL.id;
  }
  
  static boolean affectsHighway(int tilex, int tiley)
  {
    if (MethodsHighways.onHighway(tilex, tiley - 1, true)) {
      return true;
    }
    if (MethodsHighways.onHighway(tilex + 1, tiley - 1, true)) {
      return true;
    }
    if (MethodsHighways.onHighway(tilex + 1, tiley, true)) {
      return true;
    }
    if (MethodsHighways.onHighway(tilex + 1, tiley + 1, true)) {
      return true;
    }
    if (MethodsHighways.onHighway(tilex, tiley + 1, true)) {
      return true;
    }
    if (MethodsHighways.onHighway(tilex - 1, tiley + 1, true)) {
      return true;
    }
    if (MethodsHighways.onHighway(tilex - 1, tiley, true)) {
      return true;
    }
    if (MethodsHighways.onHighway(tilex - 1, tiley - 1, true)) {
      return true;
    }
    return false;
  }
  
  private static byte getOreId(int num)
  {
    if (num < 2) {
      return Tiles.Tile.TILE_CAVE_WALL_ORE_GOLD.id;
    }
    if (num < 6) {
      return Tiles.Tile.TILE_CAVE_WALL_ORE_SILVER.id;
    }
    if (num < 10) {
      return Tiles.Tile.TILE_CAVE_WALL_ORE_COPPER.id;
    }
    if (num < 14) {
      return Tiles.Tile.TILE_CAVE_WALL_ORE_ZINC.id;
    }
    if (num < 18) {
      return Tiles.Tile.TILE_CAVE_WALL_ORE_LEAD.id;
    }
    if (num < 22) {
      return Tiles.Tile.TILE_CAVE_WALL_ORE_TIN.id;
    }
    if (num < 72) {
      return Tiles.Tile.TILE_CAVE_WALL_ORE_IRON.id;
    }
    if (num < 73) {
      return Tiles.Tile.TILE_CAVE_WALL_MARBLE.id;
    }
    if (num < 74) {
      return Tiles.Tile.TILE_CAVE_WALL_SLATE.id;
    }
    return Tiles.Tile.TILE_CAVE_WALL.id;
  }
  
  static final int getItemTemplateForTile(byte type)
  {
    if (type == Tiles.Tile.TILE_CAVE_WALL_ORE_COPPER.id) {
      return 43;
    }
    if (type == Tiles.Tile.TILE_CAVE_WALL_ORE_GOLD.id) {
      return 39;
    }
    if (type == Tiles.Tile.TILE_CAVE_WALL_ORE_IRON.id) {
      return 38;
    }
    if (type == Tiles.Tile.TILE_CAVE_WALL_ORE_LEAD.id) {
      return 41;
    }
    if (type == Tiles.Tile.TILE_CAVE_WALL_ORE_SILVER.id) {
      return 40;
    }
    if (type == Tiles.Tile.TILE_CAVE_WALL_ORE_TIN.id) {
      return 207;
    }
    if (type == Tiles.Tile.TILE_CAVE_WALL_ORE_ZINC.id) {
      return 42;
    }
    if (type == Tiles.Tile.TILE_CAVE_WALL_ORE_ADAMANTINE.id) {
      return 693;
    }
    if (type == Tiles.Tile.TILE_CAVE_WALL_ORE_GLIMMERSTEEL.id) {
      return 697;
    }
    if (type == Tiles.Tile.TILE_CAVE_WALL_MARBLE.id) {
      return 785;
    }
    if (type == Tiles.Tile.TILE_CAVE_WALL_SLATE.id) {
      return 770;
    }
    if (type == Tiles.Tile.TILE_CAVE_WALL_ROCKSALT.id) {
      return 1238;
    }
    if (type == Tiles.Tile.TILE_CAVE_WALL_SANDSTONE.id) {
      return 1116;
    }
    return 146;
  }
  
  static final int getDifficultyForTile(byte type)
  {
    if ((type == Tiles.Tile.TILE_CAVE_WALL_ORE_COPPER.id) || (type == Tiles.Tile.TILE_CAVE_WALL_SLATE.id)) {
      return 20;
    }
    if ((type == Tiles.Tile.TILE_CAVE_WALL_ORE_GOLD.id) || (type == Tiles.Tile.TILE_CAVE_WALL_REINFORCED.id) || (type == Tiles.Tile.TILE_CAVE_WALL_MARBLE.id)) {
      return 40;
    }
    if (type == Tiles.Tile.TILE_CAVE_WALL_ORE_IRON.id) {
      return 3;
    }
    if (type == Tiles.Tile.TILE_CAVE_WALL_ORE_LEAD.id) {
      return 20;
    }
    if (type == Tiles.Tile.TILE_CAVE_WALL_ORE_SILVER.id) {
      return 35;
    }
    if (type == Tiles.Tile.TILE_CAVE_WALL_ORE_TIN.id) {
      return 10;
    }
    if (type == Tiles.Tile.TILE_CAVE_WALL_ORE_ADAMANTINE.id) {
      return 60;
    }
    if (type == Tiles.Tile.TILE_CAVE_WALL_ORE_GLIMMERSTEEL.id) {
      return 55;
    }
    if (type == Tiles.Tile.TILE_CAVE_WALL_ROCKSALT.id) {
      return 30;
    }
    if (type == Tiles.Tile.TILE_CAVE_WALL_SANDSTONE.id) {
      return 45;
    }
    return 2;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\behaviours\TileRockBehaviour.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */