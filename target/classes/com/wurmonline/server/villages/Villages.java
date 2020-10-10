package com.wurmonline.server.villages;

import com.wurmonline.math.TilePos;
import com.wurmonline.mesh.MeshIO;
import com.wurmonline.mesh.Tiles;
import com.wurmonline.server.Constants;
import com.wurmonline.server.DbConnector;
import com.wurmonline.server.FailedException;
import com.wurmonline.server.Features.Feature;
import com.wurmonline.server.HistoryManager;
import com.wurmonline.server.Items;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Server;
import com.wurmonline.server.ServerEntry;
import com.wurmonline.server.Servers;
import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.WurmId;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.CreatureTemplate;
import com.wurmonline.server.creatures.CreatureTemplateFactory;
import com.wurmonline.server.creatures.Creatures;
import com.wurmonline.server.creatures.NoSuchCreatureException;
import com.wurmonline.server.creatures.NoSuchCreatureTemplateException;
import com.wurmonline.server.creatures.Offspring;
import com.wurmonline.server.economy.Change;
import com.wurmonline.server.economy.MonetaryConstants;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemFactory;
import com.wurmonline.server.items.NoSuchTemplateException;
import com.wurmonline.server.kingdom.InfluenceChain;
import com.wurmonline.server.kingdom.Kingdom;
import com.wurmonline.server.kingdom.Kingdoms;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.utils.DbUtilities;
import com.wurmonline.server.zones.Den;
import com.wurmonline.server.zones.Dens;
import com.wurmonline.server.zones.FocusZone;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zones;
import com.wurmonline.shared.util.StringUtilities;
import java.awt.Rectangle;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;

public final class Villages
  implements VillageStatus, MiscConstants, MonetaryConstants, TimeConstants
{
  private static final ConcurrentHashMap<Integer, Village> villages = new ConcurrentHashMap();
  private static final ConcurrentHashMap<Long, DeadVillage> deadVillages = new ConcurrentHashMap();
  private static Logger logger = Logger.getLogger(Villages.class.getName());
  private static final String LOAD_VILLAGES = "SELECT * FROM VILLAGES WHERE DISBANDED=0";
  private static final String LOAD_DEAD_VILLAGES = "SELECT * FROM VILLAGES WHERE DISBANDED=1";
  private static final String CREATE_DEAD_VILLAGE = "INSERT INTO VILLAGES (NAME,FOUNDER,MAYOR,CREATIONDATE,STARTX,ENDX,STARTY,ENDY,DEEDID,LASTLOGIN,KINGDOM,DISBAND,DISBANDED,DEVISE) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
  private static final String LOAD_WARS = "SELECT * FROM VILLAGEWARS";
  private static final String LOAD_WAR_DECLARATIONS = "SELECT * FROM VILLAGEWARDECLARATIONS";
  @GuardedBy("ALLIANCES_RW_LOCK")
  private static final Set<Alliance> alliances = new HashSet();
  private static final ReentrantReadWriteLock ALLIANCES_RW_LOCK = new ReentrantReadWriteLock();
  @GuardedBy("WARS_RW_LOCK")
  private static final Set<Object> wars = new HashSet();
  private static final ReentrantReadWriteLock WARS_RW_LOCK = new ReentrantReadWriteLock();
  public static long TILE_UPKEEP = 20L;
  public static String TILE_UPKEEP_STRING = new Change(TILE_UPKEEP).getChangeString();
  public static long TILE_COST = 100L;
  public static String TILE_COST_STRING = new Change(TILE_COST).getChangeString();
  public static long GUARD_COST = (Servers.localServer.isChallengeOrEpicServer() ? 3 : 2) * '✐';
  public static String GUARD_COST_STRING = new Change(GUARD_COST).getChangeString();
  public static long GUARD_UPKEEP = (Servers.localServer.isChallengeOrEpicServer() ? 3 : 1) * '✐';
  public static String GUARD_UPKEEP_STRING = new Change(GUARD_UPKEEP).getChangeString();
  public static long PERIMETER_COST = 50L;
  public static String PERIMETER_COST_STRING = new Change(PERIMETER_COST).getChangeString();
  public static long PERIMETER_UPKEEP = 5L;
  public static String PERIMETER_UPKEEP_STRING = new Change(PERIMETER_UPKEEP).getChangeString();
  public static long MINIMUM_UPKEEP = 10000L;
  public static String MINIMUM_UPKEEP_STRING = new Change(MINIMUM_UPKEEP).getChangeString();
  private static long lastPolledVillageFaith = System.currentTimeMillis();
  
  public static Village getVillage(int id)
    throws NoSuchVillageException
  {
    Village toReturn = (Village)villages.get(Integer.valueOf(id));
    if (toReturn == null) {
      throw new NoSuchVillageException("No village with id " + id);
    }
    return toReturn;
  }
  
  public static Village getVillage(String name)
    throws NoSuchVillageException
  {
    for (Village v : villages.values()) {
      if (v.getName().equalsIgnoreCase(name)) {
        return v;
      }
    }
    throw new NoSuchVillageException("No village with name " + name);
  }
  
  public static Village getVillage(@Nonnull TilePos tilePos, boolean surfaced)
  {
    return getVillage(tilePos.x, tilePos.y, surfaced);
  }
  
  public static Village getVillage(int tilex, int tiley, boolean surfaced)
  {
    for (Village village : villages.values()) {
      if (village.covers(tilex, tiley)) {
        return village;
      }
    }
    return null;
  }
  
  public static Village getVillagePlus(int tilex, int tiley, boolean surfaced, int extra)
  {
    for (Village village : villages.values()) {
      if (village.coversPlus(tilex, tiley, extra)) {
        return village;
      }
    }
    return null;
  }
  
  public static final boolean isNameOk(String villageName, int ignoreVillageId)
  {
    for (Village village : villages.values()) {
      if ((village.id != ignoreVillageId) && (village.getName().equals(villageName))) {
        return false;
      }
    }
    return true;
  }
  
  public static final boolean isNameOk(String villageName)
  {
    return isNameOk(villageName, -1);
  }
  
  public static Village createVillage(int startx, int endx, int starty, int endy, int tokenx, int tokeny, String villageName, Creature founder, long deedid, boolean surfaced, boolean democracy, String devise, boolean permanent, byte spawnKingdom, int initialPerimeter)
    throws NoSuchItemException, IOException, NoSuchCreatureException, NoSuchPlayerException, NoSuchRoleException, FailedException
  {
    if (!isNameOk(villageName)) {
      throw new FailedException("The name " + villageName + " already exists. Please select another.");
    }
    Village toReturn = null;
    
    Item deed = Items.getItem(deedid);
    if (deed.getTemplateId() == 862)
    {
      deed.setDamage(0.0F);
      deed.setTemplateId(663);
      
      deed.setData1(100);
    }
    toReturn = new DbVillage(startx, endx, starty, endy, villageName, founder, deedid, surfaced, democracy, devise, permanent, spawnKingdom, initialPerimeter);
    
    toReturn.addCitizen(founder, toReturn.getRoleForStatus((byte)2));
    
    toReturn.initialize();
    try
    {
      Item token = createVillageToken(toReturn, tokenx, tokeny);
      toReturn.setTokenId(token.getWurmId());
    }
    catch (NoSuchTemplateException nst)
    {
      logger.log(Level.WARNING, nst.getMessage(), nst);
    }
    catch (FailedException fe)
    {
      logger.log(Level.WARNING, fe.getMessage(), fe);
    }
    deed.setData2(toReturn.getId());
    villages.put(Integer.valueOf(toReturn.getId()), toReturn);
    toReturn.createInitialUpkeepPlan();
    toReturn.addHistory(founder.getName(), "founded");
    HistoryManager.addHistory(founder.getName(), "founded " + villageName, false);
    founder.achievement(170);
    if (Features.Feature.TOWER_CHAINING.isEnabled())
    {
      InfluenceChain chain = InfluenceChain.getInfluenceChain(toReturn.kingdom);
      InfluenceChain.addTokenToChain(toReturn.kingdom, toReturn.getToken());
    }
    return toReturn;
  }
  
  static void removeVillage(int id)
  {
    Village v = (Village)villages.remove(Integer.valueOf(id));
    if (v != null)
    {
      DeadVillage dv = new DeadVillage(v.getDeedId(), v.getStartX(), v.getStartY(), v.getEndX(), v.getEndY(), v.getName(), v.getFounderName(), v.getMayor() != null ? v.getMayor().getName() : "Unknown", v.getCreationDate(), System.currentTimeMillis(), System.currentTimeMillis(), v.kingdom);
      deadVillages.put(Long.valueOf(v.getDeedId()), dv);
    }
  }
  
  public static boolean mayCreateTokenOnTile(boolean surfaced, int tilex, int tiley)
  {
    VolaTile tile = Zones.getTileOrNull(tilex, tiley, surfaced);
    if (tile == null) {
      return true;
    }
    if (tile.getStructure() == null) {
      return true;
    }
    return false;
  }
  
  static Item createTokenOnTile(Village village, int tilex, int tiley)
    throws NoSuchTemplateException, FailedException
  {
    VolaTile tile = Zones.getTileOrNull(tilex, tiley, village.isOnSurface());
    if (tile == null)
    {
      Item token = ItemFactory.createItem(236, 99.0F, (tilex << 2) + 2, (tiley << 2) + 2, 180.0F, village
        .isOnSurface(), (byte)0, -10L, null);
      token.setData2(village.getId());
      return token;
    }
    if (tile.getStructure() == null)
    {
      Item token = ItemFactory.createItem(236, 99.0F, (tilex << 2) + 2, (tiley << 2) + 2, 180.0F, village
        .isOnSurface(), (byte)0, -10L, null);
      token.setData2(village.getId());
      return token;
    }
    return null;
  }
  
  static Item createVillageToken(Village village, int tokenx, int tokeny)
    throws NoSuchTemplateException, FailedException
  {
    int size = village.endx - village.startx;
    Item token = createTokenOnTile(village, tokenx, tokeny);
    if (token != null) {
      return token;
    }
    for (int x = -1; x <= 1; x++) {
      for (int y = -1; y <= 1; y++)
      {
        token = createTokenOnTile(village, tokenx + x, tokeny + y);
        if (token != null) {
          return token;
        }
      }
    }
    for (int x = -size / 2; x <= size / 2; x++) {
      for (int y = -size / 2; y <= size / 2; y++)
      {
        token = createTokenOnTile(village, tokenx + x, tokeny + y);
        if (token != null) {
          return token;
        }
      }
    }
    throw new FailedException("Failed to locate a good spot for the token item.");
  }
  
  public static final String isFocusZoneBlocking(int sizeW, int sizeE, int sizeN, int sizeS, int tokenx, int tokeny, int desiredPerimeter, boolean surfaced)
  {
    int startpx = Zones.safeTileX(tokenx - sizeW - 5 - desiredPerimeter);
    int startpy = Zones.safeTileY(tokeny - sizeN - 5 - desiredPerimeter);
    int endpy = Zones.safeTileX(tokeny + sizeS + 1 + 5 + desiredPerimeter);
    int endpx = Zones.safeTileY(tokenx + sizeE + 1 + 5 + desiredPerimeter);
    Rectangle bounds = new Rectangle(startpx, startpy, endpx - startpx, endpy - startpy);
    StringBuilder toReturn = new StringBuilder();
    FocusZone[] fzs = FocusZone.getAllZones();
    for (FocusZone focusz : fzs) {
      if ((focusz.isNonPvP()) || (focusz.isPvP()))
      {
        Rectangle focusRect = new Rectangle(focusz.getStartX(), focusz.getStartY(), focusz.getEndX() - focusz.getStartX(), focusz.getEndY() - focusz.getStartY());
        if (focusRect.intersects(bounds)) {
          toReturn.append(focusz.getName() + " is within the planned area. ");
        }
      }
    }
    if (toReturn.toString().length() > 0) {
      toReturn.append("Settling there is no longer allowed.");
    }
    return toReturn.toString();
  }
  
  public static final Set<Village> getVillagesWithin(int startX, int startY, int endX, int endY)
  {
    Rectangle bounds = new Rectangle(startX, startY, endX - startX, endY - startY);
    Rectangle perimRect = bounds;
    Set<Village> toReturn = new HashSet();
    for (Village village : villages.values())
    {
      perimRect = new Rectangle(village.startx, village.starty, village.getDiameterX(), village.getDiameterY());
      if (perimRect.intersects(bounds)) {
        toReturn.add(village);
      }
    }
    return toReturn;
  }
  
  public static Map<Village, String> canFoundVillage(int sizeW, int sizeE, int sizeN, int sizeS, int tokenx, int tokeny, int desiredPerimeter, boolean surfaced, @Nullable Village original, Creature founder)
  {
    int startpx = Zones.safeTileX(tokenx - sizeW - 5 - desiredPerimeter);
    int startpy = Zones.safeTileY(tokeny - sizeN - 5 - desiredPerimeter);
    int endpy = Zones.safeTileX(tokeny + sizeS + 1 + 5 + desiredPerimeter);
    int endpx = Zones.safeTileY(tokenx + sizeE + 1 + 5 + desiredPerimeter);
    Rectangle bounds = new Rectangle(startpx, startpy, endpx - startpx, endpy - startpy);
    Rectangle perimRect = bounds;
    
    Map<Village, String> decliners = new Hashtable();
    boolean allianceOnly = (Servers.localServer.PVPSERVER) && (!Servers.localServer.isChallengeOrEpicServer());
    
    Rectangle allianceBounds = allianceOnly ? new Rectangle(Zones.safeTileX(startpx - 100), Zones.safeTileY(startpy - 100), endpx - startpx + 200, endpy - startpy + 200) : bounds;
    
    boolean accept = false;
    boolean prohibited = false;
    for (Village village : villages.values()) {
      if (village != original)
      {
        int mindist = 5 + village.getPerimeterSize();
        
        perimRect = new Rectangle(village.startx - mindist, village.starty - mindist, village.getDiameterX() + mindist * 2, village.getDiameterY() + mindist * 2);
        if (perimRect.intersects(bounds))
        {
          prohibited = true;
          decliners.put(village, "has perimeter within the planned settlement or its perimeter.");
        }
        else if ((allianceOnly) && (original == null))
        {
          if (perimRect.intersects(allianceBounds)) {
            if (founder != null) {
              if ((founder.getCitizenVillage() != null) && ((founder.getCitizenVillage() == village) || 
                (village.isAlly(founder)))) {
                accept = true;
              } else if ((founder.getCitizenVillage() == null) || (founder.getCitizenVillage() != village) || 
                (!village.isAlly(founder))) {
                decliners.put(village, "requires " + founder.getName() + " to be a citizen or ally.");
              }
            }
          }
        }
      }
    }
    if ((prohibited == true) || (!accept)) {
      return decliners;
    }
    return new Hashtable();
  }
  
  public static Village getVillageWithPerimeterAt(int tilex, int tiley, boolean surfaced)
  {
    for (Village village : villages.values())
    {
      int mindist = 5 + village.getPerimeterSize();
      Rectangle perimRect = new Rectangle(village.startx - mindist, village.starty - mindist, village.endx - village.startx + (1 + mindist * 2), village.endy - village.starty + (1 + mindist * 2));
      if (perimRect.contains(tilex, tiley)) {
        return village;
      }
    }
    return null;
  }
  
  public static Village doesNotAllowAction(Creature performer, int action, int tilex, int tiley, boolean surfaced)
  {
    if (!Servers.localServer.HOMESERVER) {
      return null;
    }
    if (performer.getKingdomId() != Servers.localServer.KINGDOM) {
      return null;
    }
    if (performer.getPower() > 1) {
      return null;
    }
    if (performer.getKingdomTemplateId() == 3) {
      return null;
    }
    VolaTile t = Zones.getTileOrNull(tilex, tiley, surfaced);
    if (t != null) {
      if (t.getVillage() != null) {
        return null;
      }
    }
    Village v = getVillageWithPerimeterAt(tilex, tiley, surfaced);
    if ((v != null) && (!v.isCitizen(performer)) && (!v.isAlly(performer))) {
      return v;
    }
    return null;
  }
  
  public static final Village doesNotAllowBuildAction(Creature performer, int action, int tilex, int tiley, boolean surfaced)
  {
    if (performer.getPower() > 1) {
      return null;
    }
    VolaTile t = Zones.getTileOrNull(tilex, tiley, surfaced);
    if (t != null)
    {
      Village village = t.getVillage();
      if (village != null)
      {
        VillageRole role = village.getRoleFor(performer);
        if (role != null)
        {
          if (role.mayBuild()) {
            return null;
          }
          return village;
        }
      }
    }
    Village v = getVillageWithPerimeterAt(tilex, tiley, surfaced);
    if ((v != null) && (!v.isCitizen(performer)) && (!v.isAlly(performer))) {
      return v;
    }
    return null;
  }
  
  public static Item isAltarOnDeed(int sizeW, int sizeE, int sizeN, int sizeS, int tokenx, int tokeny, boolean surfaced)
  {
    int startx = Math.max(0, tokenx - sizeW);
    int starty = Math.max(0, tokeny - sizeN);
    int endy = Math.min((1 << Constants.meshSize) - 1, tokeny + sizeS);
    int endx = Math.min((1 << Constants.meshSize) - 1, tokenx + sizeE);
    for (int x = startx; x <= endx; x++) {
      for (int y = starty; y <= endy; y++)
      {
        VolaTile t = Zones.getTileOrNull(x, y, surfaced);
        if (t != null)
        {
          Item[] items = t.getItems();
          for (int i = 0; i < items.length; i++) {
            if (!items[i].isUnfinished()) {
              if ((items[i].isNonDeedable()) || ((items[i].isRoyal()) && (items[i].isNoTake())) || (
                (items[i].isEpicTargetItem()) && (Servers.localServer.PVPSERVER))) {
                return items[i];
              }
            }
          }
        }
      }
    }
    return null;
  }
  
  public static Object isAggOnDeed(@Nullable Village currVill, Creature responder, int sizeW, int sizeE, int sizeN, int sizeS, int tokenx, int tokeny, boolean surfaced)
  {
    int startx = Math.max(0, tokenx - sizeW);
    int starty = Math.max(0, tokeny - sizeN);
    int endy = Zones.safeTileY(tokeny + sizeS);
    int endx = Zones.safeTileX(tokenx + sizeE);
    for (int x = startx; x <= endx; x++) {
      for (int y = starty; y <= endy; y++)
      {
        Den den = Dens.getDen(x, y);
        if (den != null) {
          try
          {
            CreatureTemplate template = CreatureTemplateFactory.getInstance().getTemplate(den.getTemplateId());
            if (responder.getPower() >= 2) {
              responder.getCommunicator().sendSafeServerMessage(template
                .getName() + " Den found at " + x + "," + y + ".");
            }
            if ((!template.isUnique()) || (Creatures.getInstance().creatureWithTemplateExists(den.getTemplateId()))) {
              return den;
            }
          }
          catch (NoSuchCreatureTemplateException nst)
          {
            logger.log(Level.WARNING, den.getTemplateId() + ":" + nst.getMessage(), nst);
            if (responder.getPower() >= 2) {
              responder.getCommunicator().sendSafeServerMessage("Den with unknown template ID: " + den.getTemplateId() + " found at " + x + ", " + y + ".");
            } else {
              responder.getCommunicator().sendSafeServerMessage("An invalid creature den was found. Please use /support to ask a GM for help to deal with this issue.");
            }
            return den;
          }
        }
        VolaTile t = Zones.getTileOrNull(x, y, surfaced);
        if (t != null) {
          if ((currVill == null) || (t.getVillage() != currVill))
          {
            Creature[] crets = t.getCreatures();
            for (int i = 0; i < crets.length; i++) {
              if (((crets[i].getAttitude(responder) == 2) && (
                (crets[i].getBaseCombatRating() > 5.0F) || (crets[i].isPlayer()))) || (crets[i].isUnique()))
              {
                if (responder.getPower() >= 2) {
                  responder.getCommunicator().sendSafeServerMessage(crets[i]
                    .getName() + " agro Creature found at " + x + "," + y + ".");
                }
                return crets[i];
              }
            }
          }
        }
      }
    }
    return null;
  }
  
  public static boolean canExpandVillage(int size, Item token)
    throws NoSuchVillageException
  {
    Village vill = getVillage(token.getData2());
    int tilex = vill.getStartX();
    int tiley = vill.getStartY();
    boolean surfaced = vill.isOnSurface();
    int startx = Math.max(0, tilex - size);
    int starty = Math.max(0, tiley - size);
    int endx = Math.min((1 << Constants.meshSize) - 1, tilex + size);
    
    int endy = Math.min((1 << Constants.meshSize) - 1, tiley + size);
    for (int x = startx; x <= endx; x += 5) {
      for (int y = starty; y <= endy; y += 5)
      {
        Village check = Zones.getVillage(x, y, surfaced);
        if ((check != null) && (!check.equals(vill))) {
          return false;
        }
      }
    }
    return true;
  }
  
  public static void generateDeadVillage(Player performer, boolean sendFeedback)
    throws IOException
  {
    int centerX = -1;
    int centerY = -1;
    boolean gotLocation = false;
    while (!gotLocation)
    {
      int testX = Server.rand.nextInt((int)(Zones.worldTileSizeX * 0.8F)) + (int)(Zones.worldTileSizeX * 0.1F);
      int testY = Server.rand.nextInt((int)(Zones.worldTileSizeY * 0.8F)) + (int)(Zones.worldTileSizeY * 0.1F);
      if (Tiles.decodeHeight(Server.surfaceMesh.getTile(testX, testY)) > 0)
      {
        centerX = testX;
        centerY = testY;
        
        gotLocation = true;
      }
    }
    int sizeX = Server.rand.nextInt(30) * (Server.rand.nextInt(4) == 0 ? 3 : 1) + 5;
    int sizeY = Math.max(sizeX / 4, Math.min(sizeX * 4, Server.rand.nextInt(30) * (Server.rand.nextInt(4) == 0 ? 3 : 1) + 5));
    sizeY = Math.max(5, sizeY);
    
    int startx = centerX - sizeX;
    int starty = centerY - sizeY;
    int endx = centerX + sizeX;
    int endy = centerY + sizeY;
    String name = StringUtilities.raiseFirstLetterOnly(generateGenericVillageName());
    String founderName = StringUtilities.raiseFirstLetterOnly(Server.rand.nextBoolean() ? Offspring.getRandomFemaleName() : Offspring.getRandomMaleName());
    String mayorName = StringUtilities.raiseFirstLetterOnly(Server.rand.nextBoolean() ? 
      Offspring.getRandomFemaleName() : Server.rand.nextBoolean() ? founderName : Offspring.getRandomMaleName());
    long creationDate = System.currentTimeMillis() - 2419200000L * Server.rand.nextInt(60);
    long deedid = WurmId.getNextItemId();
    long disbandDate = Math.min((float)(System.currentTimeMillis() - 2419200000L), 
      Math.max((float)(creationDate + 2419200000L), (float)creationDate + (float)(System.currentTimeMillis() - creationDate) * Server.rand.nextFloat()));
    long lastLogin = Math.max(creationDate + 2419200000L, disbandDate - 2419200000L * Server.rand.nextInt(6));
    byte kingdom = Servers.localServer.HOMESERVER ? Servers.localServer.KINGDOM : (byte)(Server.rand.nextInt(4) + 1);
    
    Connection dbcon = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    try
    {
      dbcon = DbConnector.getZonesDbCon();
      ps = dbcon.prepareStatement("INSERT INTO VILLAGES (NAME,FOUNDER,MAYOR,CREATIONDATE,STARTX,ENDX,STARTY,ENDY,DEEDID,LASTLOGIN,KINGDOM,DISBAND,DISBANDED,DEVISE) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)", 2);
      ps.setString(1, name);
      ps.setString(2, founderName);
      ps.setString(3, mayorName);
      ps.setLong(4, creationDate);
      ps.setInt(5, startx);
      ps.setInt(6, endx);
      ps.setInt(7, starty);
      ps.setInt(8, endy);
      ps.setLong(9, deedid);
      ps.setLong(10, lastLogin);
      ps.setByte(11, kingdom);
      ps.setLong(12, disbandDate);
      ps.setBoolean(13, true);
      ps.setString(14, "A settlement like no other.");
      ps.executeUpdate();
    }
    catch (SQLException sqx)
    {
      throw new IOException(sqx);
    }
    finally
    {
      DbUtilities.closeDatabaseObjects(ps, rs);
      DbConnector.returnConnection(dbcon);
    }
    DeadVillage dv = new DeadVillage(deedid, startx, starty, endx, endy, name, founderName, mayorName, creationDate, disbandDate, lastLogin, kingdom);
    
    deadVillages.put(Long.valueOf(deedid), dv);
    
    performer.sendToLoggers("Generated a dead village at " + centerX + "," + centerY + ".");
    if (sendFeedback) {
      performer.getCommunicator().sendNormalServerMessage("Dead Village \"" + name + "\" created at " + centerX + "," + centerY + ".");
    }
  }
  
  private static String generateGenericVillageName()
  {
    ArrayList<String> genericEndings = new ArrayList();
    addAllStrings(genericEndings, new String[] { " Village", " Isle", " Island", " Mountain", " Plains", " Estate", " Beach", " Homestead", " Valley", " Forest", " Farm", " Castle" });
    
    ArrayList<String> genericSuffix = new ArrayList();
    addAllStrings(genericSuffix, new String[] { "ford", "borough", "ington", "ton", "stead", "chester", "dale", "ham", "ing", "mouth", "port" });
    
    String toReturn = "";
    switch (Server.rand.nextInt(3))
    {
    case 0: 
      toReturn = toReturn + Offspring.getRandomMaleName();
      break;
    case 1: 
      toReturn = toReturn + Offspring.getRandomFemaleName();
      break;
    case 2: 
      toReturn = toReturn + Offspring.getRandomGenericName();
    }
    if (Server.rand.nextInt(3) == 0)
    {
      toReturn = toReturn + (String)genericSuffix.get(Server.rand.nextInt(genericSuffix.size()));
      if (Server.rand.nextBoolean()) {
        toReturn = toReturn + (String)genericEndings.get(Server.rand.nextInt(genericEndings.size()));
      }
    }
    else
    {
      toReturn = toReturn + (String)genericEndings.get(Server.rand.nextInt(genericEndings.size()));
    }
    return toReturn;
  }
  
  private static void addAllStrings(ArrayList<String> toAddTo, String... names)
  {
    for (String s : names) {
      toAddTo.add(s);
    }
  }
  
  public static void loadDeadVillages()
    throws IOException
  {
    logger.info("Loading dead villages.");
    
    long start = System.nanoTime();
    Connection dbcon = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    try
    {
      dbcon = DbConnector.getZonesDbCon();
      ps = dbcon.prepareStatement("SELECT * FROM VILLAGES WHERE DISBANDED=1");
      rs = ps.executeQuery();
      while (rs.next())
      {
        int startx = rs.getInt("STARTX");
        int starty = rs.getInt("STARTY");
        int endx = rs.getInt("ENDX");
        int endy = rs.getInt("ENDY");
        String name = rs.getString("NAME");
        String founderName = rs.getString("FOUNDER");
        String mayorName = rs.getString("MAYOR");
        long creationDate = rs.getLong("CREATIONDATE");
        long deedid = rs.getLong("DEEDID");
        long disband = rs.getLong("DISBAND");
        long lastLogin = rs.getLong("LASTLOGIN");
        byte kingdom = rs.getByte("KINGDOM");
        
        DeadVillage dv = new DeadVillage(deedid, startx, starty, endx, endy, name, founderName, mayorName, creationDate, disband, lastLogin, kingdom);
        
        deadVillages.put(Long.valueOf(deedid), dv);
      }
    }
    catch (SQLException sqx)
    {
      long end;
      throw new IOException(sqx);
    }
    finally
    {
      DbUtilities.closeDatabaseObjects(ps, rs);
      DbConnector.returnConnection(dbcon);
      long end = System.nanoTime();
      logger.info("Loaded " + deadVillages.size() + " dead villages from the database took " + (float)(end - start) / 1000000.0F + " ms");
    }
  }
  
  public static final void loadVillages()
    throws IOException
  {
    logger.info("Loading villages.");
    
    long start = System.nanoTime();
    Connection dbcon = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    try
    {
      dbcon = DbConnector.getZonesDbCon();
      ps = dbcon.prepareStatement("SELECT * FROM VILLAGES WHERE DISBANDED=0");
      rs = ps.executeQuery();
      while (rs.next())
      {
        id = rs.getInt("ID");
        int startx = rs.getInt("STARTX");
        int starty = rs.getInt("STARTY");
        int endx = rs.getInt("ENDX");
        int endy = rs.getInt("ENDY");
        String name = rs.getString("NAME");
        String founderName = rs.getString("FOUNDER");
        String mayorName = rs.getString("MAYOR");
        long creationDate = rs.getLong("CREATIONDATE");
        long deedid = rs.getLong("DEEDID");
        boolean surfaced = rs.getBoolean("SURFACED");
        String devise = rs.getString("DEVISE");
        boolean democracy = rs.getBoolean("DEMOCRACY");
        boolean homestead = rs.getBoolean("HOMESTEAD");
        long tokenid = rs.getLong("TOKEN");
        long disband = rs.getLong("DISBAND");
        long disbander = rs.getLong("DISBANDER");
        long lastLogin = rs.getLong("LASTLOGIN");
        byte kingdom = rs.getByte("KINGDOM");
        long upkeep = rs.getLong("UPKEEP");
        byte settings = rs.getByte("MAYPICKUP");
        boolean acceptsHomesteads = rs.getBoolean("ACCEPTSHOMESTEADS");
        int maxcitizens = rs.getInt("MAXCITIZENS");
        boolean perma = rs.getBoolean("PERMANENT");
        byte spawnKingdom = rs.getByte("SPAWNKINGDOM");
        boolean merchants = rs.getBoolean("MERCHANTS");
        int perimeterTiles = rs.getInt("PERIMETER");
        boolean aggros = rs.getBoolean("AGGROS");
        
        String consumerKeyToUse = rs.getString("TWITKEY");
        String consumerSecretToUse = rs.getString("TWITSECRET");
        String applicationToken = rs.getString("TWITAPP");
        String applicationSecret = rs.getString("TWITAPPSECRET");
        boolean twitChat = rs.getBoolean("TWITCHAT");
        boolean twitEnabled = rs.getBoolean("TWITENABLE");
        float faithWar = rs.getFloat("FAITHWAR");
        float faithHeal = rs.getFloat("FAITHHEAL");
        float faithCreate = rs.getFloat("FAITHCREATE");
        byte spawnSituation = rs.getByte("SPAWNSITUATION");
        int allianceNumber = rs.getInt("ALLIANCENUMBER");
        short wins = rs.getShort("HOTAWINS");
        long lastChangedName = rs.getLong("NAMECHANGED");
        int villageRep = rs.getInt("VILLAGEREP");
        
        String motd = rs.getString("MOTD");
        
        Village toAdd = new DbVillage(id, startx, endx, starty, endy, name, founderName, mayorName, deedid, surfaced, democracy, devise, creationDate, homestead, tokenid, disband, disbander, lastLogin, kingdom, upkeep, settings, acceptsHomesteads, merchants, maxcitizens, perma, spawnKingdom, perimeterTiles, aggros, consumerKeyToUse, consumerSecretToUse, applicationToken, applicationSecret, twitChat, twitEnabled, faithWar, faithHeal, faithCreate, spawnSituation, allianceNumber, wins, lastChangedName, motd);
        
        toAdd.villageReputation = villageRep;
        villages.put(Integer.valueOf(id), toAdd);
        Kingdoms.getKingdom(kingdom).setExistsHere(true);
        toAdd.loadRoles();
        
        toAdd.loadVillageMapAnnotations();
        toAdd.loadVillageRecruitees();
        
        toAdd.plan = new DbGuardPlan(id);
        if (logger.isLoggable(Level.FINE)) {
          logger.fine("Loaded Village ID: " + id + ": " + toAdd);
        }
      }
      for (Village toAdd : villages.values())
      {
        toAdd.initialize();
        toAdd.addGates();
        toAdd.addMineDoors();
        
        toAdd.loadReputations();
        toAdd.plan.fixGuards();
        toAdd.checkForEnemies();
        toAdd.loadHistory();
      }
    }
    catch (SQLException sqx)
    {
      int id;
      long end;
      throw new IOException(sqx);
    }
    finally
    {
      DbUtilities.closeDatabaseObjects(ps, rs);
      DbConnector.returnConnection(dbcon);
      long end = System.nanoTime();
      logger.info("Loaded " + villages.size() + " villages from the database took " + (float)(end - start) / 1000000.0F + " ms");
    }
  }
  
  public static final void loadCitizens()
  {
    logger.info("Loading villages citizens.");
    for (Village toAdd : villages.values()) {
      toAdd.loadCitizens();
    }
  }
  
  public static final void loadGuards()
  {
    logger.info("Loading villages guards.");
    for (Village toAdd : villages.values()) {
      toAdd.loadGuards();
    }
  }
  
  static final void createWar(Village villone, Village villtwo)
  {
    VillageWar newWar = new DbVillageWar(villone, villtwo);
    newWar.save();
    villone.startWar(newWar, true);
    villtwo.startWar(newWar, false);
    HistoryManager.addHistory("", villone.getName() + " and " + villtwo.getName() + " goes to war.");
  }
  
  public static final void declareWar(Village villone, Village villtwo)
  {
    WarDeclaration newWar = new WarDeclaration(villone, villtwo);
    
    villone.addWarDeclaration(newWar);
    villtwo.addWarDeclaration(newWar);
  }
  
  public static final void declarePeace(Creature performer, Creature accepter, Village villone, Village villtwo)
  {
    villone.declarePeace(performer, accepter, villtwo, true);
    villtwo.declarePeace(performer, accepter, villone, false);
    
    VillageWar[] wararr = getWars();
    for (int x = 0; x < wararr.length; x++) {
      if (((wararr[x].getVillone() == villone) && (wararr[x].getVilltwo() == villtwo)) || (
        (wararr[x].getVilltwo() == villone) && (wararr[x].getVillone() == villtwo))) {
        removeAndDeleteVillageWar(wararr[x]);
      }
    }
    HistoryManager.addHistory("", villone.getName() + " and " + villtwo.getName() + " make peace.");
  }
  
  /* Error */
  private static boolean removeAndDeleteVillageWar(VillageWar aVillageWar)
  {
    // Byte code:
    //   0: iconst_0
    //   1: istore_1
    //   2: aload_0
    //   3: ifnull +50 -> 53
    //   6: getstatic 374	com/wurmonline/server/villages/Villages:WARS_RW_LOCK	Ljava/util/concurrent/locks/ReentrantReadWriteLock;
    //   9: invokevirtual 375	java/util/concurrent/locks/ReentrantReadWriteLock:writeLock	()Ljava/util/concurrent/locks/ReentrantReadWriteLock$WriteLock;
    //   12: invokevirtual 376	java/util/concurrent/locks/ReentrantReadWriteLock$WriteLock:lock	()V
    //   15: getstatic 377	com/wurmonline/server/villages/Villages:wars	Ljava/util/Set;
    //   18: aload_0
    //   19: invokeinterface 378 2 0
    //   24: istore_1
    //   25: aload_0
    //   26: invokevirtual 379	com/wurmonline/server/villages/VillageWar:delete	()V
    //   29: getstatic 374	com/wurmonline/server/villages/Villages:WARS_RW_LOCK	Ljava/util/concurrent/locks/ReentrantReadWriteLock;
    //   32: invokevirtual 375	java/util/concurrent/locks/ReentrantReadWriteLock:writeLock	()Ljava/util/concurrent/locks/ReentrantReadWriteLock$WriteLock;
    //   35: invokevirtual 380	java/util/concurrent/locks/ReentrantReadWriteLock$WriteLock:unlock	()V
    //   38: goto +15 -> 53
    //   41: astore_2
    //   42: getstatic 374	com/wurmonline/server/villages/Villages:WARS_RW_LOCK	Ljava/util/concurrent/locks/ReentrantReadWriteLock;
    //   45: invokevirtual 375	java/util/concurrent/locks/ReentrantReadWriteLock:writeLock	()Ljava/util/concurrent/locks/ReentrantReadWriteLock$WriteLock;
    //   48: invokevirtual 380	java/util/concurrent/locks/ReentrantReadWriteLock$WriteLock:unlock	()V
    //   51: aload_2
    //   52: athrow
    //   53: iload_1
    //   54: ireturn
    // Line number table:
    //   Java source line #1105	-> byte code offset #0
    //   Java source line #1106	-> byte code offset #2
    //   Java source line #1108	-> byte code offset #6
    //   Java source line #1111	-> byte code offset #15
    //   Java source line #1112	-> byte code offset #25
    //   Java source line #1116	-> byte code offset #29
    //   Java source line #1117	-> byte code offset #38
    //   Java source line #1116	-> byte code offset #41
    //   Java source line #1117	-> byte code offset #51
    //   Java source line #1119	-> byte code offset #53
    // Local variable table:
    //   start	length	slot	name	signature
    //   0	55	0	aVillageWar	VillageWar
    //   1	53	1	lVillageWarExisted	boolean
    //   41	11	2	localObject	Object
    // Exception table:
    //   from	to	target	type
    //   15	29	41	finally
  }
  
  public static final VillageWar[] getWars()
  {
    WARS_RW_LOCK.readLock().lock();
    try
    {
      return (VillageWar[])wars.toArray(new VillageWar[wars.size()]);
    }
    finally
    {
      WARS_RW_LOCK.readLock().unlock();
    }
  }
  
  public static final Alliance[] getAlliances()
  {
    ALLIANCES_RW_LOCK.readLock().lock();
    try
    {
      return (Alliance[])alliances.toArray(new Alliance[alliances.size()]);
    }
    finally
    {
      ALLIANCES_RW_LOCK.readLock().unlock();
    }
  }
  
  public static final void loadWars()
    throws IOException
  {
    logger.log(Level.INFO, "Loading all wars.");
    
    long start = System.nanoTime();
    Connection dbcon = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    WARS_RW_LOCK.writeLock().lock();
    try
    {
      dbcon = DbConnector.getZonesDbCon();
      ps = dbcon.prepareStatement("SELECT * FROM VILLAGEWARS");
      rs = ps.executeQuery();
      int aid = -10;
      while (rs.next()) {
        try
        {
          aid = rs.getInt("ID");
          Village villone = getVillage(rs.getInt("VILLONE"));
          Village villtwo = getVillage(rs.getInt("VILLTWO"));
          VillageWar war = new DbVillageWar(villone, villtwo);
          villone.addWar(war);
          villtwo.addWar(war);
          wars.add(war);
          if (logger.isLoggable(Level.FINE)) {
            logger.fine("Loaded War ID: " + aid + ": " + war);
          }
        }
        catch (NoSuchVillageException nsv)
        {
          logger.log(Level.WARNING, "Failed to load war with id " + aid + "!");
        }
      }
    }
    catch (SQLException sqx)
    {
      long end;
      throw new IOException(sqx);
    }
    finally
    {
      WARS_RW_LOCK.writeLock().unlock();
      DbUtilities.closeDatabaseObjects(ps, rs);
      DbConnector.returnConnection(dbcon);
      long end = System.nanoTime();
      logger.info("Loaded " + wars.size() + " wars from the database took " + (float)(end - start) / 1000000.0F + " ms");
    }
  }
  
  public static final void loadWarDeclarations()
    throws IOException
  {
    Connection dbcon = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    WARS_RW_LOCK.writeLock().lock();
    try
    {
      dbcon = DbConnector.getZonesDbCon();
      ps = dbcon.prepareStatement("SELECT * FROM VILLAGEWARDECLARATIONS");
      rs = ps.executeQuery();
      int aid = -10;
      while (rs.next()) {
        try
        {
          aid = rs.getInt("ID");
          Village villone = getVillage(rs.getInt("VILLONE"));
          Village villtwo = getVillage(rs.getInt("VILLTWO"));
          long time = rs.getLong("DECLARETIME");
          WarDeclaration war = new WarDeclaration(villone, villtwo, time);
          villone.addWarDeclaration(war);
          villtwo.addWarDeclaration(war);
          wars.add(war);
        }
        catch (NoSuchVillageException nsv)
        {
          logger.log(Level.WARNING, "Failed to load war with id " + aid + "!");
        }
      }
    }
    catch (SQLException sqx)
    {
      throw new IOException(sqx);
    }
    finally
    {
      WARS_RW_LOCK.writeLock().unlock();
      DbUtilities.closeDatabaseObjects(ps, rs);
      DbConnector.returnConnection(dbcon);
    }
  }
  
  public static Village getVillageForCreature(Creature creature)
  {
    if (creature == null) {
      return null;
    }
    for (Village village : villages.values()) {
      if (village.isCitizen(creature)) {
        return village;
      }
    }
    return null;
  }
  
  public static Village getVillageForCreature(long wid)
  {
    if (wid == -10L) {
      return null;
    }
    for (Village village : villages.values()) {
      if (village.isCitizen(wid)) {
        return village;
      }
    }
    return null;
  }
  
  public static long getVillageMoney()
  {
    long toReturn = 0L;
    for (Village village : villages.values()) {
      if (village.plan != null) {
        toReturn += village.plan.moneyLeft;
      }
    }
    return toReturn;
  }
  
  public static final int getSizeForDeed(int templateId)
  {
    if ((templateId == 237) || (templateId == 234)) {
      return 5;
    }
    if ((templateId == 211) || (templateId == 253)) {
      return 10;
    }
    if (templateId == 238) {
      return 15;
    }
    if ((templateId == 239) || (templateId == 254)) {
      return 20;
    }
    if (templateId == 242) {
      return 50;
    }
    if (templateId == 244) {
      return 100;
    }
    if (templateId == 245) {
      return 200;
    }
    return 5;
  }
  
  public static final Village[] getVillages()
  {
    Village[] toReturn = new Village[0];
    if (villages != null) {
      toReturn = (Village[])villages.values().toArray(new Village[villages.size()]);
    }
    return toReturn;
  }
  
  public static int getNumberOfVillages()
  {
    return villages.size();
  }
  
  public static final void poll()
  {
    long now = System.currentTimeMillis();
    Village[] aVillages = getVillages();
    boolean lowerFaith = System.currentTimeMillis() - lastPolledVillageFaith > 86400000L;
    for (int x = 0; x < aVillages.length; x++) {
      aVillages[x].poll(now, lowerFaith);
    }
    if (lowerFaith) {
      lastPolledVillageFaith = System.currentTimeMillis();
    }
  }
  
  public static final Village getCapital(byte kingdom)
  {
    Village[] vills = getVillages();
    for (int x = 0; x < vills.length; x++) {
      if ((vills[x].kingdom == kingdom) && (vills[x].isCapital())) {
        return vills[x];
      }
    }
    return null;
  }
  
  public static final Village getFirstVillageForKingdom(byte kingdom)
  {
    Village[] vills = getVillages();
    for (int x = 0; x < vills.length; x++) {
      if (vills[x].kingdom == kingdom) {
        return vills[x];
      }
    }
    return null;
  }
  
  public static final Village getFirstPermanentVillageForKingdom(byte kingdom)
  {
    Village[] vills = getVillages();
    for (int x = 0; x < vills.length; x++) {
      if ((vills[x].kingdom == kingdom) && (vills[x].isPermanent)) {
        return vills[x];
      }
    }
    return null;
  }
  
  public static final Village[] getPermanentVillagesForKingdom(byte kingdom)
  {
    ConcurrentHashMap<Integer, Village> permVills = new ConcurrentHashMap();
    for (Village village : villages.values()) {
      if ((village.isPermanent) && (village.kingdom == kingdom)) {
        permVills.put(Integer.valueOf(village.getId()), village);
      }
    }
    return (Village[])permVills.values().toArray(new Village[permVills.size()]);
  }
  
  public static final boolean wasLastVillage(Village village)
  {
    Village[] vills = getVillages();
    for (int x = 0; x < vills.length; x++) {
      if ((village.getId() != vills[x].getId()) && (vills[x].kingdom == village.kingdom)) {
        return false;
      }
    }
    return true;
  }
  
  public static final void convertTowers()
  {
    Village[] vills = getVillages();
    for (int x = 0; x < vills.length; x++) {
      vills[x].convertTowersWithinDistance(150);
    }
    for (int x = 0; x < vills.length; x++) {
      vills[x].convertTowersWithinPerimeter();
    }
  }
  
  public static final Village[] getPermanentVillages(byte kingdomChecked)
  {
    Set<Village> toReturn = new HashSet();
    
    Kingdom kingd = Kingdoms.getKingdom(kingdomChecked);
    if (kingd != null) {
      for (Village v : villages.values()) {
        if (v.kingdom == kingdomChecked) {
          if ((v.isPermanent) || ((v.isCapital()) && (kingd.isCustomKingdom()))) {
            toReturn.add(v);
          }
        }
      }
    }
    return (Village[])toReturn.toArray(new Village[toReturn.size()]);
  }
  
  public static final Village[] getKosVillagesFor(long playerId)
  {
    Set<Village> toReturn = new HashSet();
    for (Village v : villages.values())
    {
      Reputation rep = v.getReputationObject(playerId);
      if (rep != null) {
        toReturn.add(v);
      }
    }
    return (Village[])toReturn.toArray(new Village[toReturn.size()]);
  }
  
  @Nullable
  public static final Village getVillageFor(Item waystone)
  {
    for (Village village : villages.values()) {
      if (village.coversPlus(waystone.getTileX(), waystone.getTileY(), 2)) {
        return village;
      }
    }
    return null;
  }
  
  public static final ArrayList<DeadVillage> getDeadVillagesFor(int tilex, int tiley)
  {
    return getDeadVillagesNear(tilex, tiley, 0);
  }
  
  public static final ArrayList<DeadVillage> getDeadVillagesNear(int tilex, int tiley, int range)
  {
    ArrayList<DeadVillage> toReturn = new ArrayList();
    for (DeadVillage dv : deadVillages.values()) {
      if ((dv.getStartX() - range <= tilex) && (dv.getEndX() + range >= tilex) && 
        (dv.getStartY() - range <= tiley) && (dv.getEndY() + range >= tiley)) {
        toReturn.add(dv);
      }
    }
    return toReturn;
  }
  
  public static final DeadVillage getDeadVillage(long deadVillageId)
  {
    return (DeadVillage)deadVillages.get(Long.valueOf(deadVillageId));
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\villages\Villages.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */