package com.wurmonline.server.creatures;

import com.wurmonline.mesh.MeshIO;
import com.wurmonline.mesh.Tiles;
import com.wurmonline.server.Constants;
import com.wurmonline.server.DbConnector;
import com.wurmonline.server.Items;
import com.wurmonline.server.LoginHandler;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Players;
import com.wurmonline.server.Server;
import com.wurmonline.server.ServerEntry;
import com.wurmonline.server.Servers;
import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.WurmCalendar;
import com.wurmonline.server.WurmId;
import com.wurmonline.server.behaviours.Seat;
import com.wurmonline.server.behaviours.Vehicle;
import com.wurmonline.server.behaviours.Vehicles;
import com.wurmonline.server.bodys.Body;
import com.wurmonline.server.bodys.BodyFactory;
import com.wurmonline.server.deities.Deities;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.players.PlayerInfo;
import com.wurmonline.server.players.PlayerInfoFactory;
import com.wurmonline.server.skills.Skills;
import com.wurmonline.server.structures.NoSuchStructureException;
import com.wurmonline.server.structures.Structure;
import com.wurmonline.server.structures.Structures;
import com.wurmonline.server.tutorial.MissionTargets;
import com.wurmonline.server.utils.DbUtilities;
import com.wurmonline.server.villages.NoSuchRoleException;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.villages.Villages;
import com.wurmonline.server.zones.Den;
import com.wurmonline.server.zones.Dens;
import com.wurmonline.server.zones.NoSuchZoneException;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zone;
import com.wurmonline.server.zones.Zones;
import com.wurmonline.shared.exceptions.WurmServerException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.Timer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class Creatures
  implements MiscConstants, CreatureTemplateIds, TimeConstants
{
  private final Map<Long, Creature> creatures;
  private final Map<Long, Creature> offlineCreatures;
  private final ConcurrentHashMap<Long, Creature> avatars;
  private final Map<Long, Long> protectedCreatures = new ConcurrentHashMap();
  private final Map<String, Creature> npcs = new ConcurrentHashMap();
  private final Map<Integer, Integer> creaturesByType;
  private static Creatures instance = null;
  private static Logger logger = Logger.getLogger(Creatures.class.getName());
  private static final String getAllCreatures = "SELECT * FROM CREATURES";
  private static final String COUNT_CREATURES = "SELECT COUNT(*) FROM CREATURES";
  private static final String DELETE_CREATURE = "DELETE FROM CREATURES WHERE WURMID=?";
  private static final String DELETE_CREATUREBODY = "DELETE FROM BODYPARTS WHERE OWNERID=?";
  private static final String DELETE_CREATURESKILLS = "DELETE FROM SKILLS WHERE OWNER=?";
  private static final String DELETE_CREATUREITEMS = "DELETE FROM ITEMS WHERE OWNERID=?";
  private static final String DELETE_CREATURE_SPLIT = "DELETE FROM CREATURES_BASE WHERE WURMID=?";
  private static final String DELETE_CREATURE_POS_SPLIT = "DELETE FROM CREATURES_POS WHERE WURMID=?";
  private static final String DELETE_PROT_CREATURE = "DELETE FROM PROTECTED WHERE WURMID=?";
  private static final String INSERT_PROT_CREATURE = "INSERT INTO PROTECTED (WURMID,PLAYERID) VALUES(?,?)";
  private static final String LOAD_PROT_CREATURES = "SELECT * FROM PROTECTED";
  private static final boolean fixColourTraits = false;
  private final Map<Long, Brand> brandedCreatures = new ConcurrentHashMap();
  private final Map<Long, Long> ledCreatures = new ConcurrentHashMap();
  private static Map<Long, Creature> rideCreatures;
  private final Timer creaturePollThread;
  private final Creatures.PollTimerTask pollTask;
  private int numberOfNice = 0;
  private int numberOfAgg = 0;
  private int numberOfTyped = 0;
  private int kingdomCreatures = 0;
  private static int destroyedCaveCrets = 0;
  private static boolean loading = false;
  private static int nums = 0;
  private static int seaMonsters = 0;
  private static int seaHunters = 0;
  private int currentCreature = 0;
  private Creature[] crets;
  public int numberOfZonesX = 64;
  private long totalTime = 0L;
  private long startTime = 0L;
  private boolean logCreaturePolls = false;
  
  public static Creatures getInstance()
  {
    if (instance == null) {
      instance = new Creatures();
    }
    return instance;
  }
  
  public int getNumberOfCreatures()
  {
    return this.creatures.size();
  }
  
  public int getNumberOfCreaturesWithTemplate(int templateChecked)
  {
    int toReturn = 0;
    for (Creature cret : this.creatures.values()) {
      if (cret.getTemplate().getTemplateId() == templateChecked) {
        toReturn++;
      }
    }
    return toReturn;
  }
  
  public final void setLastLed(long creatureLed, long leader)
  {
    this.ledCreatures.put(Long.valueOf(creatureLed), Long.valueOf(leader));
  }
  
  public final boolean wasLastLed(long potentialLeader, long creatureLed)
  {
    Long lastLeader = (Long)this.ledCreatures.get(Long.valueOf(creatureLed));
    if (lastLeader != null) {
      return lastLeader.longValue() == potentialLeader;
    }
    return false;
  }
  
  public final void addBrand(Brand brand)
  {
    this.brandedCreatures.put(Long.valueOf(brand.getCreatureId()), brand);
  }
  
  public final void setBrand(long creatureId, long brandid)
  {
    if (brandid <= 0L)
    {
      this.brandedCreatures.remove(Long.valueOf(creatureId));
    }
    else
    {
      Brand brand = (Brand)this.brandedCreatures.get(Long.valueOf(creatureId));
      if (brand == null) {
        brand = new Brand(creatureId, System.currentTimeMillis(), brandid, false);
      } else {
        brand.setBrandId(brandid);
      }
      this.brandedCreatures.put(Long.valueOf(creatureId), brand);
    }
  }
  
  public final Brand getBrand(long creatureId)
  {
    Brand brand = (Brand)this.brandedCreatures.get(Long.valueOf(creatureId));
    return brand;
  }
  
  public final boolean isBrandedBy(long creatureId, long brandId)
  {
    Brand brand = (Brand)this.brandedCreatures.get(Long.valueOf(creatureId));
    if (brand != null) {
      return brand.getBrandId() == brandId;
    }
    return false;
  }
  
  public final Creature[] getBranded(long villageId)
  {
    Map<Long, Brand> removeMap = new ConcurrentHashMap();
    
    Set<Creature> brandedSet = new HashSet();
    for (Brand b : this.brandedCreatures.values()) {
      if (b.getBrandId() == villageId) {
        try
        {
          brandedSet.add(getCreature(b.getCreatureId()));
        }
        catch (NoSuchCreatureException e)
        {
          Long cid = new Long(b.getCreatureId());
          if (isCreatureOffline(cid))
          {
            Creature creature = (Creature)this.offlineCreatures.get(cid);
            brandedSet.add(creature);
          }
          else
          {
            removeMap.put(Long.valueOf(b.getCreatureId()), b);
          }
        }
      }
    }
    return (Creature[])brandedSet.toArray(new Creature[brandedSet.size()]);
  }
  
  public void removeBrandingFor(int villageId)
  {
    for (Brand b : this.brandedCreatures.values()) {
      if (b.getBrandId() == villageId) {
        b.deleteBrand();
      }
    }
  }
  
  public int getNumberOfNice()
  {
    return this.numberOfNice;
  }
  
  public int getNumberOfAgg()
  {
    return this.numberOfAgg;
  }
  
  public int getNumberOfTyped()
  {
    return this.numberOfTyped;
  }
  
  public int getNumberOfKingdomCreatures()
  {
    return this.kingdomCreatures;
  }
  
  public int getNumberOfSeaMonsters()
  {
    return seaMonsters;
  }
  
  public int getNumberOfSeaHunters()
  {
    return seaHunters;
  }
  
  private Creatures()
  {
    int numberOfCreaturesInDatabase = Math.max(getNumberOfCreaturesInDatabase(), 100);
    this.creatures = new ConcurrentHashMap(numberOfCreaturesInDatabase);
    this.avatars = new ConcurrentHashMap();
    this.creaturesByType = new ConcurrentHashMap(numberOfCreaturesInDatabase);
    this.offlineCreatures = new ConcurrentHashMap();
    this.creaturePollThread = new Timer();
    this.pollTask = new Creatures.PollTimerTask(this, null);
  }
  
  public final void startPollTask() {}
  
  public final void shutDownPolltask()
  {
    this.pollTask.shutDown();
  }
  
  public void sendOfflineCreatures(Communicator c, boolean showOwner)
  {
    for (Creature cret : this.offlineCreatures.values())
    {
      String dominatorName = " dominator=" + cret.dominator;
      if (showOwner) {
        try
        {
          PlayerInfo p = PlayerInfoFactory.getPlayerInfoWithWurmId(cret.dominator);
          if (p != null) {
            dominatorName = " dominator=" + p.getName();
          }
        }
        catch (Exception localException) {}
      } else {
        dominatorName = "";
      }
      c.sendNormalServerMessage(cret.getName() + " at " + cret.getPosX() / 4.0F + ", " + cret.getPosY() / 4.0F + " loyalty " + cret
        .getLoyalty() + dominatorName);
    }
  }
  
  public void setCreatureDead(Creature dead)
  {
    long deadid = dead.getWurmId();
    for (Creature creature : this.creatures.values())
    {
      if (creature.opponent == dead) {
        creature.setOpponent(null);
      }
      if (creature.target == deadid) {
        creature.setTarget(-10L, true);
      }
      creature.removeTarget(deadid);
    }
    Vehicles.removeDragger(dead);
  }
  
  public void combatRound()
  {
    for (Creature lCreature : this.creatures.values()) {
      lCreature.getCombatHandler().clearRound();
    }
  }
  
  private int getNumberOfCreaturesInDatabase()
  {
    Statement stmt = null;
    ResultSet rs = null;
    int numberOfCreatures = 0;
    try
    {
      Connection dbcon = DbConnector.getCreatureDbCon();
      stmt = dbcon.createStatement();
      rs = stmt.executeQuery("SELECT COUNT(*) FROM CREATURES");
      if (rs.next()) {
        numberOfCreatures = rs.getInt(1);
      }
    }
    catch (SQLException e)
    {
      logger.log(Level.WARNING, "Failed to count creatures:" + e.getMessage(), e);
    }
    finally
    {
      DbUtilities.closeDatabaseObjects(stmt, rs);
    }
    return numberOfCreatures;
  }
  
  private final void loadMoreStuff(Creature toReturn)
  {
    try
    {
      toReturn.getBody().createBodyParts();
      Items.loadAllItemsForNonPlayer(toReturn, toReturn.getStatus().getInventoryId());
      Village v = Villages.getVillageForCreature(toReturn);
      if ((v == null) && 
        (toReturn.isNpcTrader())) {
        if (toReturn.getName().startsWith("Trader"))
        {
          v = Villages.getVillage(toReturn.getTileX(), toReturn.getTileY(), true);
          if (v != null) {
            try
            {
              logger.log(Level.INFO, "Adding " + toReturn
                .getName() + " as citizen to " + v.getName());
              v.addCitizen(toReturn, v.getRoleForStatus((byte)3));
            }
            catch (IOException iox)
            {
              logger.log(Level.INFO, iox.getMessage());
            }
            catch (NoSuchRoleException nsx)
            {
              logger.log(Level.INFO, nsx.getMessage());
            }
          }
        }
      }
      toReturn.setCitizenVillage(v);
      
      toReturn.postLoad();
      if ((toReturn.getTemplate().getTemplateId() == 46) || 
        (toReturn.getTemplate().getTemplateId() == 47))
      {
        Zones.setHasLoadedChristmas(true);
        if (!WurmCalendar.isChristmas()) {
          permanentlyDelete(toReturn);
        } else if (toReturn.getTemplate().getTemplateId() == 46)
        {
          if ((!Servers.localServer.HOMESERVER) && (toReturn.getKingdomId() == 2)) {
            Zones.santaMolRehan = toReturn;
          } else if ((Servers.localServer.HOMESERVER) && (toReturn.getKingdomId() == 4)) {
            Zones.santas.put(Long.valueOf(toReturn.getWurmId()), toReturn);
          } else {
            Zones.santa = toReturn;
          }
        }
        else {
          Zones.evilsanta = toReturn;
        }
      }
    }
    catch (Exception ex)
    {
      logger.log(Level.WARNING, ex.getMessage(), ex);
    }
  }
  
  private static final void initializeCreature(String templateName, ResultSet rs, Creature statusHolder)
  {
    long id = statusHolder.getWurmId();
    
    statusHolder.getStatus().setPosition(CreaturePos.getPosition(id));
    try
    {
      statusHolder.getStatus().setStatusExists(true);
      statusHolder.getStatus().template = CreatureTemplateFactory.getInstance().getTemplate(templateName);
      statusHolder.template = statusHolder.getStatus().template;
      statusHolder.getStatus().bodyId = rs.getLong("BODYID");
      statusHolder.getStatus().body = BodyFactory.getBody(statusHolder, statusHolder.getStatus().template.getBodyType(), 
        statusHolder.getStatus().template.getCentimetersHigh(), 
        statusHolder.getStatus().template.getCentimetersLong(), 
        statusHolder.getStatus().template.getCentimetersWide());
      statusHolder.getStatus().body.setCentimetersLong(rs.getShort("CENTIMETERSLONG"));
      statusHolder.getStatus().body.setCentimetersHigh(rs.getShort("CENTIMETERSHIGH"));
      statusHolder.getStatus().body.setCentimetersWide(rs.getShort("CENTIMETERSWIDE"));
      statusHolder.getStatus().sex = rs.getByte("SEX");
      statusHolder.getStatus().modtype = rs.getByte("TYPE");
      String name = rs.getString("NAME");
      statusHolder.setName(name);
      statusHolder.getStatus().inventoryId = rs.getLong("INVENTORYID");
      statusHolder.getStatus().stamina = (rs.getShort("STAMINA") & 0xFFFF);
      statusHolder.getStatus().hunger = (rs.getShort("HUNGER") & 0xFFFF);
      statusHolder.getStatus().thirst = (rs.getShort("THIRST") & 0xFFFF);
      statusHolder.getStatus().buildingId = rs.getLong("BUILDINGID");
      statusHolder.getStatus().kingdom = rs.getByte("KINGDOM");
      statusHolder.getStatus().dead = rs.getBoolean("DEAD");
      statusHolder.getStatus().stealth = rs.getBoolean("STEALTH");
      statusHolder.getStatus().age = rs.getInt("AGE");
      statusHolder.getStatus().fat = rs.getByte("FAT");
      statusHolder.getStatus().lastPolledAge = rs.getLong("LASTPOLLEDAGE");
      statusHolder.dominator = rs.getLong("DOMINATOR");
      statusHolder.getStatus().reborn = rs.getBoolean("REBORN");
      statusHolder.getStatus().loyalty = rs.getFloat("LOYALTY");
      statusHolder.getStatus().lastPolledLoyalty = rs.getLong("LASTPOLLEDLOYALTY");
      statusHolder.getStatus().detectInvisCounter = rs.getShort("DETECTIONSECS");
      statusHolder.getStatus().traits = rs.getLong("TRAITS");
      if (statusHolder.getStatus().traits != 0L) {
        statusHolder.getStatus().setTraitBits(statusHolder.getStatus().traits);
      }
      statusHolder.getStatus().mother = rs.getLong("MOTHER");
      statusHolder.getStatus().father = rs.getLong("FATHER");
      statusHolder.getStatus().nutrition = rs.getFloat("NUTRITION");
      statusHolder.getStatus().disease = rs.getByte("DISEASE");
      if (statusHolder.getStatus().buildingId != -10L) {
        try
        {
          Structure struct = Structures.getStructure(statusHolder.getStatus().buildingId);
          if (!struct.isFinalFinished()) {
            statusHolder.setStructure(struct);
          } else {
            statusHolder.getStatus().buildingId = -10L;
          }
        }
        catch (NoSuchStructureException nss)
        {
          statusHolder.getStatus().buildingId = -10L;
          logger.log(Level.INFO, "Could not find structure for " + statusHolder.getName());
          statusHolder.setStructure(null);
        }
      }
      statusHolder.getStatus().lastGroomed = rs.getLong("LASTGROOMED");
      statusHolder.getStatus().offline = rs.getBoolean("OFFLINE");
      statusHolder.getStatus().stayOnline = rs.getBoolean("STAYONLINE");
      String petName = rs.getString("PETNAME");
      statusHolder.setPetName(petName);
      
      statusHolder.calculateSize();
      statusHolder.vehicle = rs.getLong("VEHICLE");
      statusHolder.seatType = rs.getByte("SEAT_TYPE");
      if (statusHolder.vehicle > 0L) {
        rideCreatures.put(Long.valueOf(id), statusHolder);
      }
    }
    catch (Exception ex)
    {
      logger.log(Level.WARNING, "Failed to load creature " + id + " " + ex.getMessage(), ex);
    }
  }
  
  public int loadAllCreatures()
    throws NoSuchCreatureException
  {
    Brand.loadAllBrands();
    loading = true;
    Offspring.loadAllOffspring();
    loadAllProtectedCreatures();
    
    long lNow2 = System.nanoTime();
    logger.info("Loading all skills for creatures");
    try
    {
      Skills.loadAllCreatureSkills();
    }
    catch (Exception ex)
    {
      logger.log(Level.INFO, "Failed Loading creature skills.", ex);
      System.exit(0);
    }
    logger.log(Level.INFO, "Loaded creature skills. That took " + (float)(System.nanoTime() - lNow2) / 1000000.0F);
    logger.info("Loading Creatures");
    long lNow = System.nanoTime();
    long cpS = 0L;
    long cpOne = 0L;
    long cpTwo = 0L;
    long cpThree = 0L;
    long cpFour = 0L;
    Creature toReturn = null;
    Connection dbcon = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    Set<Creature> toRemove = new HashSet();
    rideCreatures = new ConcurrentHashMap();
    try
    {
      dbcon = DbConnector.getCreatureDbCon();
      ps = dbcon.prepareStatement("SELECT * FROM CREATURES");
      rs = ps.executeQuery();
      while (rs.next())
      {
        cpS = System.nanoTime();
        try
        {
          String templateName = rs.getString("TEMPLATENAME");
          if ((templateName.equalsIgnoreCase("human")) || (templateName.equalsIgnoreCase("npc human"))) {
            toReturn = new Npc(rs.getLong("WURMID"));
          } else {
            toReturn = new Creature(rs.getLong("WURMID"));
          }
          initializeCreature(templateName, rs, toReturn);
          toReturn.loadTemplate();
          if (toReturn.isFish())
          {
            logger.info("Fish removed " + toReturn.getName());
            permanentlyDelete(toReturn);
          }
          else if (((!toReturn.isUnique()) && ((toReturn.isOffline()) || (toReturn.isDominated())) && 
            (!toReturn.isStayonline())) || ((!Constants.loadNpcs) && (toReturn.isNpc())))
          {
            addOfflineCreature(toReturn);
            addCreature(toReturn, true);
            toRemove.add(toReturn);
          }
          else if (!addCreature(toReturn, false))
          {
            permanentlyDelete(toReturn);
          }
          cpOne += System.nanoTime() - cpS;
          cpS = System.nanoTime();
        }
        catch (Exception ex)
        {
          logger.log(Level.WARNING, "Failed to load creature: " + toReturn + "due to " + ex.getMessage(), ex);
        }
      }
      for (Creature rider : rideCreatures.values())
      {
        vehicleId = rider.vehicle;
        byte seatType = rider.seatType;
        rider.vehicle = -10L;
        rider.seatType = -1;
        try
        {
          Vehicle vehic = null;
          Item vehicle = null;
          Creature creature = null;
          if (WurmId.getType(vehicleId) == 1)
          {
            creature = Server.getInstance().getCreature(vehicleId);
            vehic = Vehicles.getVehicle(creature);
          }
          else
          {
            vehicle = Items.getItem(vehicleId);
            vehic = Vehicles.getVehicle(vehicle);
          }
          if (vehic != null) {
            if ((seatType == -1) || (seatType == 2))
            {
              if (vehic.addDragger(rider))
              {
                rider.setHitched(vehic, true);
                Seat driverseat = vehic.getPilotSeat();
                if (driverseat != null)
                {
                  float _r = (-vehicle.getRotation() + 180.0F) * 3.1415927F / 180.0F;
                  float _s = (float)Math.sin(_r);
                  float _c = (float)Math.cos(_r);
                  float xo = _s * -driverseat.offx - _c * -driverseat.offy;
                  float yo = _c * -driverseat.offx + _s * -driverseat.offy;
                  float nPosX = rider.getStatus().getPositionX() - xo;
                  float nPosY = rider.getStatus().getPositionY() - yo;
                  float nPosZ = rider.getStatus().getPositionZ() - driverseat.offz;
                  
                  rider.getStatus().setPositionX(nPosX);
                  rider.getStatus().setPositionY(nPosY);
                  rider.getStatus().setRotation(-vehicle.getRotation() + 180.0F);
                  rider.getMovementScheme().setPosition(rider.getStatus().getPositionX(), rider
                    .getStatus().getPositionY(), nPosZ, rider
                    .getStatus().getRotation(), rider.getLayer());
                }
              }
            }
            else if ((seatType == 0) || (seatType == 1)) {
              for (int x = 0; x < vehic.seats.length; x++) {
                if ((vehic.seats[x].getType() == seatType) && (
                  (!vehic.seats[x].isOccupied()) || (vehic.seats[x].occupant == rider.getWurmId())))
                {
                  vehic.seats[x].occupy(vehic, rider);
                  if (seatType == 0)
                  {
                    vehic.pilotId = rider.getWurmId();
                    rider.setVehicleCommander(true);
                  }
                  MountAction m = new MountAction(creature, vehicle, vehic, x, seatType == 0, vehic.seats[x].offz);
                  rider.setMountAction(m);
                  rider.setVehicle(vehicleId, true, seatType);
                  break;
                }
              }
            }
          }
        }
        catch (NoSuchItemException|NoSuchPlayerException|NoSuchCreatureException nsi)
        {
          logger.log(Level.INFO, "Item " + vehicleId + " missing for hitched " + rider.getWurmId() + " " + rider.getName());
        }
      }
      rideCreatures = null;
      long lNow1 = System.nanoTime();
      logger.info("Loading all items for creatures");
      
      Items.loadAllCreatureItems();
      logger.log(Level.INFO, "Loaded creature items. That took " + (float)(System.nanoTime() - lNow1) / 1000000.0F + " ms for " + 
      
        Items.getNumItems() + " items and " + Items.getNumCoins() + " coins.");
      for (Creature creature : this.creatures.values())
      {
        Skills.fillCreatureTempSkills(creature);
        loadMoreStuff(creature);
      }
      for (Creature creature : toRemove)
      {
        loadMoreStuff(creature);
        removeCreature(creature);
        
        creature.getStatus().offline = true;
      }
    }
    catch (SQLException sqx)
    {
      long vehicleId;
      logger.log(Level.WARNING, "Failed to load creatures:" + sqx.getMessage(), sqx);
      throw new NoSuchCreatureException(sqx);
    }
    finally
    {
      DbUtilities.closeDatabaseObjects(ps, rs);
      DbConnector.returnConnection(dbcon);
      logger.log(Level.INFO, "Loaded " + getNumberOfCreatures() + " creatures. Destroyed " + destroyedCaveCrets + ". That took " + 
        (float)(System.nanoTime() - lNow) / 1000000.0F + " ms. CheckPoints cp1=" + (float)cpOne / 1000000.0F + ", cp2=" + 0.0F + ", cp3=" + 0.0F + ", cp4=" + 0.0F);
      
      logger.log(Level.INFO, "Loaded items for creature. CheckPoints cp1=" + 
        (float)Items.getCpOne() / 1000000.0F + ", cp2=" + 
        (float)Items.getCpTwo() / 1000000.0F + ", cp3=" + 
        (float)Items.getCpThree() / 1000000.0F + ", cp4=" + 
        
        (float)Items.getCpFour() / 1000000.0F);
    }
    loading = false;
    Items.clearCreatureLoadMap();
    Skills.clearCreatureLoadMap();
    Offspring.resetOffspringCounters();
    return getNumberOfCreatures();
  }
  
  public boolean creatureWithTemplateExists(int templateId)
  {
    for (Creature lCreature : this.creatures.values()) {
      if (lCreature.template.getTemplateId() == templateId) {
        return true;
      }
    }
    return false;
  }
  
  public Creature getUniqueCreatureWithTemplate(int templateId)
  {
    List<Creature> foundCreatures = new ArrayList();
    for (Creature lCreature : this.creatures.values()) {
      if (lCreature.template.getTemplateId() == templateId) {
        foundCreatures.add(lCreature);
      }
    }
    if (foundCreatures.size() == 0) {
      return null;
    }
    if (foundCreatures.size() == 1) {
      return (Creature)foundCreatures.get(0);
    }
    throw new UnsupportedOperationException("Multiple creatures found");
  }
  
  public Creature getCreature(long id)
    throws NoSuchCreatureException
  {
    Creature toReturn = null;
    Long cid = new Long(id);
    if (this.creatures.containsKey(cid)) {
      toReturn = (Creature)this.creatures.get(cid);
    } else {
      throw new NoSuchCreatureException("No such creature for id: " + id);
    }
    if (toReturn == null) {
      throw new NoSuchCreatureException("No creature with id " + id);
    }
    return toReturn;
  }
  
  public Creature getCreatureOrNull(long id)
  {
    try
    {
      return getCreature(id);
    }
    catch (NoSuchCreatureException n) {}
    return null;
  }
  
  private void removeTarget(long id)
  {
    for (Creature cret : this.creatures.values()) {
      if (cret.target == id) {
        cret.setTarget(-10L, true);
      }
    }
    for (??? = this.offlineCreatures.values().iterator(); ???.hasNext();)
    {
      cret = (Creature)???.next();
      if (cret.target == id) {
        cret.setTarget(-10L, true);
      }
    }
    Creature cret;
    Player[] players = Players.getInstance().getPlayers();
    for (Player lPlayer : players) {
      if (lPlayer.target == id) {
        lPlayer.setTarget(-10L, true);
      }
    }
  }
  
  public void setCreatureOffline(Creature creature)
  {
    try
    {
      Creature[] watchers = creature.getInventory().getWatchers();
      for (Creature lWatcher : watchers) {
        creature.getInventory().removeWatcher(lWatcher, true);
      }
    }
    catch (NoSuchCreatureException localNoSuchCreatureException) {}catch (Exception nsc)
    {
      logger.log(Level.WARNING, creature.getName() + " " + nsc.getMessage(), nsc);
    }
    try
    {
      Creature[] watchers = creature.getBody().getBodyItem().getWatchers();
      for (Creature lWatcher : watchers) {
        creature.getBody().getBodyItem().removeWatcher(lWatcher, true);
      }
    }
    catch (NoSuchCreatureException localNoSuchCreatureException1) {}catch (Exception nsc)
    {
      logger.log(Level.WARNING, creature.getName() + " " + nsc.getMessage(), nsc);
    }
    creature.clearOrders();
    creature.setLeader(null);
    creature.destroyVisionArea();
    removeTarget(creature.getWurmId());
    removeCreature(creature);
    addOfflineCreature(creature);
    creature.setPathing(false, true);
    creature.setOffline(true);
    try
    {
      creature.getStatus().savePosition(creature.getWurmId(), false, -10, true);
    }
    catch (IOException iox)
    {
      logger.log(Level.WARNING, iox.getMessage(), iox);
    }
  }
  
  private final void saveCreatureProtected(long creatureId, long protector)
  {
    Connection dbcon = null;
    PreparedStatement ps = null;
    try
    {
      dbcon = DbConnector.getCreatureDbCon();
      ps = dbcon.prepareStatement("INSERT INTO PROTECTED (WURMID,PLAYERID) VALUES(?,?)");
      ps.setLong(1, creatureId);
      ps.setLong(2, protector);
      ps.executeUpdate();
    }
    catch (SQLException sqex)
    {
      logger.log(Level.WARNING, "Failed to insert creature protected " + creatureId, sqex);
    }
    finally
    {
      DbUtilities.closeDatabaseObjects(ps, null);
      DbConnector.returnConnection(dbcon);
    }
  }
  
  private final void deleteCreatureProtected(long creatureId)
  {
    Connection dbcon = null;
    PreparedStatement ps = null;
    try
    {
      dbcon = DbConnector.getCreatureDbCon();
      ps = dbcon.prepareStatement("DELETE FROM PROTECTED WHERE WURMID=?");
      ps.setLong(1, creatureId);
      ps.executeUpdate();
    }
    catch (SQLException sqex)
    {
      logger.log(Level.WARNING, "Failed to delete creature protected " + creatureId, sqex);
    }
    finally
    {
      DbUtilities.closeDatabaseObjects(ps, null);
      DbConnector.returnConnection(dbcon);
    }
  }
  
  private final void loadAllProtectedCreatures()
  {
    Connection dbcon = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    try
    {
      dbcon = DbConnector.getCreatureDbCon();
      ps = dbcon.prepareStatement("SELECT * FROM PROTECTED");
      rs = ps.executeQuery();
      while (rs.next()) {
        this.protectedCreatures.put(Long.valueOf(rs.getLong("WURMID")), Long.valueOf(rs.getLong("PLAYERID")));
      }
    }
    catch (SQLException sqex)
    {
      logger.log(Level.WARNING, "Failed to load creatures protected.", sqex);
    }
    finally
    {
      DbUtilities.closeDatabaseObjects(ps, rs);
      DbConnector.returnConnection(dbcon);
    }
  }
  
  public final int getNumberOfCreaturesProtectedBy(long protector)
  {
    int numsToReturn = 0;
    for (Long l : this.protectedCreatures.values()) {
      if (l.longValue() == protector) {
        numsToReturn++;
      }
    }
    return numsToReturn;
  }
  
  public final int setNoCreaturesProtectedBy(long protector)
  {
    int numsToReturn = 0;
    LinkedList<Long> toRemove = new LinkedList();
    for (Map.Entry<Long, Long> l : this.protectedCreatures.entrySet()) {
      if (((Long)l.getValue()).longValue() == protector) {
        toRemove.add(l.getKey());
      }
    }
    for (Long l : toRemove)
    {
      numsToReturn++;
      deleteCreatureProtected(l.longValue());
      this.protectedCreatures.remove(l);
    }
    return numsToReturn;
  }
  
  public final void setCreatureProtected(Creature creature, long protector, boolean setProtected)
  {
    if (setProtected)
    {
      if (!this.protectedCreatures.containsKey(Long.valueOf(creature.getWurmId()))) {
        saveCreatureProtected(creature.getWurmId(), protector);
      }
      this.protectedCreatures.put(Long.valueOf(creature.getWurmId()), Long.valueOf(protector));
    }
    else if (this.protectedCreatures.containsKey(Long.valueOf(creature.getWurmId())))
    {
      deleteCreatureProtected(creature.getWurmId());
      this.protectedCreatures.remove(Long.valueOf(creature.getWurmId()));
    }
  }
  
  public final long getCreatureProtectorFor(long wurmId)
  {
    if (this.protectedCreatures.containsKey(Long.valueOf(wurmId))) {
      return ((Long)this.protectedCreatures.get(Long.valueOf(wurmId))).longValue();
    }
    return -10L;
  }
  
  public final Creature[] getProtectedCreaturesFor(long playerId)
  {
    Set<Creature> protectedSet = new HashSet();
    for (Map.Entry<Long, Long> entry : this.protectedCreatures.entrySet()) {
      if (((Long)entry.getValue()).longValue() == playerId) {
        try
        {
          protectedSet.add(getCreature(((Long)entry.getKey()).longValue()));
        }
        catch (NoSuchCreatureException e)
        {
          logger.log(Level.WARNING, e.getMessage(), e);
        }
      }
    }
    return (Creature[])protectedSet.toArray(new Creature[protectedSet.size()]);
  }
  
  public final boolean isCreatureProtected(long wurmId)
  {
    return this.protectedCreatures.containsKey(Long.valueOf(wurmId));
  }
  
  public final long getCreatureProctector(Creature creature)
  {
    Long whom = (Long)this.protectedCreatures.get(Long.valueOf(creature.getWurmId()));
    if (whom != null) {
      return whom.longValue();
    }
    return -10L;
  }
  
  public void pollOfflineCreatures()
  {
    Set<Creature> toReturn = new HashSet();
    Iterator<Creature> creatureIterator = this.offlineCreatures.values().iterator();
    while (creatureIterator.hasNext())
    {
      Creature offline = (Creature)creatureIterator.next();
      if (offline.pollAge())
      {
        if (logger.isLoggable(Level.FINER)) {
          logger.finer(offline.getWurmId() + ", " + offline.getName() + " is dead.");
        }
        creatureIterator.remove();
      }
      else
      {
        offline.pollLoyalty();
        if ((offline.dominator == -10L) && ((!offline.isNpc()) || (Constants.loadNpcs == true))) {
          toReturn.add(offline);
        }
      }
    }
    for (Creature c : toReturn) {
      try
      {
        logger.log(Level.INFO, "Returning " + c.getName() + " from being offline due to no loyalty.");
        loadOfflineCreature(c.getWurmId());
      }
      catch (NoSuchCreatureException nsc)
      {
        logger.log(Level.WARNING, nsc.getMessage());
      }
    }
  }
  
  public Creature loadOfflineCreature(long creatureId)
    throws NoSuchCreatureException
  {
    Long cid = new Long(creatureId);
    if (isCreatureOffline(cid))
    {
      Creature creature = (Creature)this.offlineCreatures.remove(cid);
      creature.setOffline(false);
      creature.setLeader(null);
      creature.setCitizenVillage(Villages.getVillageForCreature(creature));
      creature.getStatus().visible = true;
      try
      {
        creature.createVisionArea();
      }
      catch (Exception ex)
      {
        logger.log(Level.WARNING, "Problem creating VisionArea for creature with id " + creatureId + "due to " + ex
          .getMessage(), ex);
      }
      addCreature(creature, false);
      
      return creature;
    }
    throw new NoSuchCreatureException("No such creature with id " + creatureId);
  }
  
  public boolean isCreatureOffline(Long aCreatureId)
  {
    return this.offlineCreatures.containsKey(aCreatureId);
  }
  
  public final long getPetId(long dominatorId)
  {
    for (Creature c : this.offlineCreatures.values()) {
      if (c.dominator == dominatorId) {
        return c.getWurmId();
      }
    }
    for (Creature c : this.creatures.values()) {
      if (c.dominator == dominatorId) {
        return c.getWurmId();
      }
    }
    return -10L;
  }
  
  public void returnCreaturesForPlayer(long playerId)
  {
    Set<Creature> toLoad = new HashSet();
    for (Creature c : this.offlineCreatures.values()) {
      if (c.dominator == playerId)
      {
        toLoad.add(c);
        c.setLoyalty(0.0F);
        c.setDominator(-10L);
      }
    }
    for (Creature c : toLoad) {
      try
      {
        logger.log(Level.INFO, "Returning " + c.getName() + " from being offline due to no loyalty.");
        loadOfflineCreature(c.getWurmId());
      }
      catch (NoSuchCreatureException nsc)
      {
        logger.log(Level.WARNING, nsc.getMessage());
      }
    }
  }
  
  public final Creature getNpc(String name)
  {
    return (Creature)this.npcs.get(LoginHandler.raiseFirstLetter(name));
  }
  
  public final Npc[] getNpcs()
  {
    return (Npc[])this.npcs.values().toArray(new Npc[this.npcs.size()]);
  }
  
  public void removeCreature(Creature creature)
  {
    if (creature.isNpc()) {
      this.npcs.remove(creature.getName());
    }
    this.creatures.remove(new Long(creature.getWurmId()));
    this.avatars.remove(new Long(creature.getWurmId()));
    removeCreatureByType(creature.getTemplate().getTemplateId());
  }
  
  public boolean addCreature(Creature creature, boolean offline)
  {
    return addCreature(creature, offline, true);
  }
  
  void sendToWorld(Creature creature)
  {
    try
    {
      Zones.getZone(creature.getTileX(), creature.getTileY(), creature.isOnSurface()).addCreature(creature.getWurmId());
    }
    catch (NoSuchCreatureException nex)
    {
      logger.log(Level.WARNING, "Failed to add creature ID: " + creature.getWurmId() + " due to " + nex.getMessage(), nex);
    }
    catch (NoSuchZoneException sex)
    {
      logger.log(Level.WARNING, "Failed to add creature ID: " + creature.getWurmId() + " due to " + sex.getMessage(), sex);
    }
    catch (NoSuchPlayerException nsp)
    {
      logger.log(Level.WARNING, "Failed to add creature ID: " + creature.getWurmId() + " due to " + nsp.getMessage(), nsp);
    }
  }
  
  final void addCreatureByType(int creatureType)
  {
    Integer val = (Integer)this.creaturesByType.get(Integer.valueOf(creatureType));
    if (val == null) {
      this.creaturesByType.put(Integer.valueOf(creatureType), Integer.valueOf(1));
    } else {
      this.creaturesByType.put(Integer.valueOf(creatureType), Integer.valueOf(val.intValue() + 1));
    }
  }
  
  final void removeCreatureByType(int creatureType)
  {
    Integer val = (Integer)this.creaturesByType.get(Integer.valueOf(creatureType));
    if ((val == null) || (val.intValue() == 0)) {
      this.creaturesByType.put(Integer.valueOf(creatureType), Integer.valueOf(0));
    } else {
      this.creaturesByType.put(Integer.valueOf(creatureType), Integer.valueOf(val.intValue() - 1));
    }
  }
  
  public final int getCreatureByType(int creatureType)
  {
    Integer val = (Integer)this.creaturesByType.get(Integer.valueOf(creatureType));
    if ((val == null) || (val.intValue() == 0)) {
      return 0;
    }
    return val.intValue();
  }
  
  public final Map<Integer, Integer> getCreatureTypeList()
  {
    return this.creaturesByType;
  }
  
  public final int getOpenSpawnSlotsForCreatureType(int creatureType)
  {
    int currentCount = getCreatureByType(creatureType);
    try
    {
      CreatureTemplate ctemplate = CreatureTemplateFactory.getInstance().getTemplate(creatureType);
      
      int maxByPercent = (int)(Servers.localServer.maxCreatures * ctemplate.getMaxPercentOfCreatures());
      int slotsOpenForPercent = Math.max(maxByPercent - currentCount, 0);
      if (ctemplate.usesMaxPopulation())
      {
        int maxPop = ctemplate.getMaxPopulationOfCreatures();
        int slotsByPopulation = Math.max(maxPop - currentCount, 0);
        if (maxPop <= maxByPercent) {
          return slotsByPopulation;
        }
        return slotsOpenForPercent;
      }
      return slotsOpenForPercent;
    }
    catch (NoSuchCreatureTemplateException e)
    {
      logger.log(Level.WARNING, "Unable to find creature template with id: " + creatureType + ".", e);
    }
    return 0;
  }
  
  boolean addCreature(Creature creature, boolean offline, boolean sendToWorld)
  {
    this.creatures.put(new Long(creature.getWurmId()), creature);
    if (creature.isNpc()) {
      this.npcs.put(LoginHandler.raiseFirstLetter(creature.getName()), creature);
    }
    if (creature.isAvatar()) {
      this.avatars.put(new Long(creature.getWurmId()), creature);
    }
    addCreatureByType(creature.getTemplate().getTemplateId());
    if (!creature.isDead()) {
      try
      {
        if (!creature.isOnSurface()) {
          if (Tiles.isSolidCave(Tiles.decodeType(Server.caveMesh.getTile(creature.getTileX(), creature.getTileY()))))
          {
            creature.setLayer(0, false);
            logger.log(Level.INFO, "Changed layer to surface for ID: " + creature
              .getWurmId() + " - " + creature.getName() + '.');
          }
        }
        if (!offline)
        {
          if (!creature.isFloating()) {
            if ((creature.isMonster()) || (creature.isAggHuman())) {
              this.numberOfAgg += 1;
            } else {
              this.numberOfNice += 1;
            }
          }
          if (creature.getStatus().modtype > 0) {
            this.numberOfTyped += 1;
          }
          if ((creature.isAggWhitie()) || (creature.isDefendKingdom())) {
            this.kingdomCreatures += 1;
          }
          if ((creature.isFloating()) && (!creature.isSpiritGuard())) {
            if (creature.getTemplate().getTemplateId() == 70) {
              seaMonsters += 1;
            } else {
              seaHunters += 1;
            }
          }
          if (sendToWorld)
          {
            int numsOnTile = Zones.getZone(creature.getTileX(), creature.getTileY(), creature.isOnSurface()).addCreature(creature.getWurmId());
            if ((loading) && (numsOnTile > 2) && (!creature.isHorse())) {
              if ((!creature.isOnSurface()) && (!creature.isDominated()) && (!creature.isUnique()) && 
                (!creature.isSalesman()) && (!creature.isWagoner()) && 
                (!creature.hasTrait(63)) && (!creature.isHitched()) && (creature.getBody().getAllItems().length == 0) && 
                (!creature.isBranded()) && (!creature.isCaredFor()))
              {
                Zones.getZone(creature.getTileX(), creature.getTileY(), creature.isOnSurface()).deleteCreature(creature, true);
                logger.log(Level.INFO, "Destroying " + creature.getName() + ", " + creature.getWurmId() + " at cave " + creature
                  .getTileX() + ", " + creature
                  .getTileY() + " - overcrowded.");
                destroyedCaveCrets += 1;
                return false;
              }
            }
          }
        }
      }
      catch (NoSuchCreatureException nex)
      {
        logger.log(Level.WARNING, "Failed to add creature ID: " + creature.getWurmId() + " due to " + nex.getMessage(), nex);
        
        this.creatures.remove(new Long(creature.getWurmId()));
        this.avatars.remove(new Long(creature.getWurmId()));
        removeCreatureByType(creature.getTemplate().getTemplateId());
        return false;
      }
      catch (NoSuchZoneException sex)
      {
        logger.log(Level.WARNING, "Failed to add creature ID: " + creature.getWurmId() + " due to " + sex.getMessage(), sex);
        
        this.creatures.remove(new Long(creature.getWurmId()));
        this.avatars.remove(new Long(creature.getWurmId()));
        removeCreatureByType(creature.getTemplate().getTemplateId());
        return false;
      }
      catch (NoSuchPlayerException nsp)
      {
        logger.log(Level.WARNING, "Failed to add creature ID: " + creature.getWurmId() + " due to " + nsp.getMessage(), nsp);
        
        this.creatures.remove(new Long(creature.getWurmId()));
        this.avatars.remove(new Long(creature.getWurmId()));
        removeCreatureByType(creature.getTemplate().getTemplateId());
        return false;
      }
    }
    return true;
  }
  
  public static final boolean isLoading()
  {
    return loading;
  }
  
  private void addOfflineCreature(Creature creature)
  {
    this.offlineCreatures.put(new Long(creature.getWurmId()), creature);
    if ((!creature.isDead()) && (!creature.isOnSurface())) {
      if (Tiles.isSolidCave(Tiles.decodeType(Server.caveMesh.getTile(creature.getTileX(), creature.getTileY()))))
      {
        creature.setLayer(0, false);
        logger.log(Level.INFO, "Changed layer to surface for ID: " + creature.getWurmId() + " - " + creature.getName() + '.');
      }
    }
  }
  
  public Creature[] getCreatures()
  {
    Creature[] toReturn = new Creature[this.creatures.size()];
    return (Creature[])this.creatures.values().toArray(toReturn);
  }
  
  public Creature[] getAvatars()
  {
    Creature[] toReturn = new Creature[this.avatars.size()];
    return (Creature[])this.avatars.values().toArray(toReturn);
  }
  
  public void saveCreatures()
  {
    Creature[] creatarr = getCreatures();
    Exception error = null;
    int numsSaved = 0;
    for (Creature creature : creatarr) {
      try
      {
        if (creature.getStatus().save()) {
          numsSaved++;
        }
      }
      catch (IOException iox)
      {
        error = iox;
      }
    }
    logger.log(Level.INFO, "Saved " + numsSaved + " creature statuses.");
    if (error != null) {
      logger.log(Level.INFO, "An error occurred while saving creatures:" + error.getMessage(), error);
    }
  }
  
  void permanentlyDelete(Creature creature)
  {
    removeCreature(creature);
    if (!creature.isFloating()) {
      if ((creature.isMonster()) || (creature.isAggHuman())) {
        this.numberOfAgg -= 1;
      } else {
        this.numberOfNice -= 1;
      }
    }
    if (creature.getStatus().modtype > 0) {
      this.numberOfTyped -= 1;
    }
    if ((creature.isAggWhitie()) || (creature.isDefendKingdom())) {
      this.kingdomCreatures -= 1;
    }
    if (creature.isFloating()) {
      if (creature.getTemplate().getTemplateId() == 70) {
        seaMonsters -= 1;
      } else {
        seaHunters -= 1;
      }
    }
    Brand brand = getBrand(creature.getWurmId());
    if (brand != null)
    {
      brand.deleteBrand();
      setBrand(creature.getWurmId(), 0L);
    }
    setCreatureProtected(creature, -10L, false);
    CreaturePos.delete(creature.getWurmId());
    
    MissionTargets.destroyMissionTarget(creature.getWurmId(), true);
    Connection dbcon = null;
    PreparedStatement ps = null;
    Connection dbcon2 = null;
    PreparedStatement ps2 = null;
    try
    {
      dbcon = DbConnector.getCreatureDbCon();
      if (Constants.useSplitCreaturesTable)
      {
        ps = dbcon.prepareStatement("DELETE FROM CREATURES_BASE WHERE WURMID=?");
        ps.setLong(1, creature.getWurmId());
        ps.executeUpdate();
        DbUtilities.closeDatabaseObjects(ps, null);
        
        ps = dbcon.prepareStatement("DELETE FROM CREATURES_POS WHERE WURMID=?");
        ps.setLong(1, creature.getWurmId());
        ps.executeUpdate();
        DbUtilities.closeDatabaseObjects(ps, null);
        
        ps = dbcon.prepareStatement("DELETE FROM SKILLS WHERE OWNER=?");
        ps.setLong(1, creature.getWurmId());
        ps.executeUpdate();
        DbUtilities.closeDatabaseObjects(ps, null);
      }
      else
      {
        ps = dbcon.prepareStatement("DELETE FROM CREATURES WHERE WURMID=?");
        ps.setLong(1, creature.getWurmId());
        ps.executeUpdate();
        DbUtilities.closeDatabaseObjects(ps, null);
        
        ps = dbcon.prepareStatement("DELETE FROM SKILLS WHERE OWNER=?");
        ps.setLong(1, creature.getWurmId());
        ps.executeUpdate();
        DbUtilities.closeDatabaseObjects(ps, null);
      }
      dbcon2 = DbConnector.getItemDbCon();
      
      ps2 = dbcon2.prepareStatement("DELETE FROM BODYPARTS WHERE OWNERID=?");
      ps2.setLong(1, creature.getWurmId());
      ps2.executeUpdate();
      DbUtilities.closeDatabaseObjects(ps2, null);
      
      ps2 = dbcon2.prepareStatement("DELETE FROM ITEMS WHERE OWNERID=?");
      ps2.setLong(1, creature.getWurmId());
      ps2.executeUpdate();
      DbUtilities.closeDatabaseObjects(ps2, null);
    }
    catch (SQLException sqex)
    {
      logger.log(Level.WARNING, "Failed to delete creature " + creature, sqex);
    }
    finally
    {
      DbUtilities.closeDatabaseObjects(ps, null);
      DbUtilities.closeDatabaseObjects(ps2, null);
      DbConnector.returnConnection(dbcon);
      DbConnector.returnConnection(dbcon2);
    }
    if (creature.isUnique()) {
      if (creature.getTemplate() != null)
      {
        Den d = Dens.getDen(Integer.valueOf(creature.getTemplate().getTemplateId()).intValue());
        if (d != null) {
          if (!getInstance().creatureWithTemplateExists(creature.getTemplate().getTemplateId())) {
            Dens.deleteDen(creature.getTemplate().getTemplateId());
          }
        }
      }
    }
  }
  
  int resetGuardSkills()
  {
    int count = 0;
    for (Creature cret : this.creatures.values()) {
      if (cret.isSpiritGuard()) {
        try
        {
          cret.skills.delete();
          cret.skills.clone(cret.getSkills().getSkills());
          cret.skills.save();
          count++;
        }
        catch (Exception ex)
        {
          logger.log(Level.WARNING, cret.getWurmId() + ":" + ex.getMessage(), ex);
        }
      }
    }
    logger.log(Level.INFO, "Reset " + count + " guards skills.");
    return count;
  }
  
  final Creature[] getCreaturesWithName(String name)
  {
    name = name.toLowerCase();
    Set<Creature> toReturn = new HashSet();
    for (Creature cret : this.creatures.values()) {
      if ((cret.getName().toLowerCase().indexOf(name) < 0 ? 1 : 0) == 0) {
        toReturn.add(cret);
      }
    }
    return (Creature[])toReturn.toArray(new Creature[toReturn.size()]);
  }
  
  public Creature[] getHorsesWithName(String aName)
  {
    String name = aName.toLowerCase();
    Set<Creature> toReturn = new HashSet();
    for (Creature cret : this.creatures.values()) {
      if (cret.getTemplate().isHorse) {
        if ((cret.getName().toLowerCase().indexOf(name) < 0 ? 1 : 0) == 0) {
          toReturn.add(cret);
        }
      }
    }
    return (Creature[])toReturn.toArray(new Creature[toReturn.size()]);
  }
  
  public static boolean shouldDestroy(Creature c)
  {
    int tid = c.getTemplate().getTemplateId();
    if (nums < 7000) {
      if ((tid == 15) || (tid == 54) || (tid == 25) || (tid == 44) || (tid == 52) || (tid == 55) || (tid == 10) || (tid == 42) || (tid == 12) || (tid == 45) || (tid == 48) || (tid == 59) || (tid == 13) || (tid == 21))
      {
        nums += 1;
        return true;
      }
    }
    return false;
  }
  
  public static void destroySwimmers()
  {
    Creature[] crets = getInstance().getCreatures();
    for (Creature lCret : crets) {
      if (shouldDestroy(lCret)) {
        lCret.destroy();
      }
    }
  }
  
  public static void createLightAvengers()
  {
    int numsa = 0;
    while (numsa < 20)
    {
      int x = Zones.safeTileX(Server.rand.nextInt(Zones.worldTileSizeX));
      int y = Zones.safeTileY(Server.rand.nextInt(Zones.worldTileSizeY));
      int t = Server.surfaceMesh.getTile(x, y);
      if (Tiles.decodeHeight(t) > 0)
      {
        byte deity = 1;
        if (Tiles.decodeHeightAsFloat(t) > 100.0F) {
          deity = 2;
        }
        try
        {
          CreatureTemplate ctemplate = CreatureTemplateFactory.getInstance().getTemplate(68);
          
          Creature cret = Creature.doNew(68, (x << 2) + 2.0F, (y << 2) + 2.0F, Server.rand
            .nextInt(360), 0, "", ctemplate.getSex(), (byte)0);
          
          cret.setDeity(Deities.getDeity(deity));
          
          numsa++;
        }
        catch (Exception ex)
        {
          logger.log(Level.WARNING, ex.getMessage(), ex);
        }
      }
    }
  }
  
  public final void togglePollTaskLog()
  {
    setLog(!isLog());
  }
  
  public void pollAllCreatures(int num)
  {
    if ((num == 1) || (this.crets == null))
    {
      if (this.crets != null)
      {
        if ((isLog()) && (this.totalTime > 0L)) {
          logger.log(Level.INFO, "Creatures polled " + this.crets.length + " Took " + this.totalTime);
        }
        this.totalTime = 0L;
      }
      this.currentCreature = 0;
      this.crets = getCreatures();
      if (this.crets != null) {
        for (Player creature : Players.getInstance().getPlayers()) {
          try
          {
            VolaTile t = creature.getCurrentTile();
            if (creature.poll())
            {
              if (t != null) {
                t.deleteCreature(creature);
              }
            }
            else if ((creature.isDoLavaDamage()) && 
              (creature.doLavaDamage())) {
              if (t != null) {
                t.deleteCreature(creature);
              }
            }
            if (creature.isDoAreaDamage()) {
              if ((t != null) && 
                (t.doAreaDamage(creature))) {
                if (t != null) {
                  t.deleteCreature(creature);
                }
              }
            }
          }
          catch (Exception ex)
          {
            logger.log(Level.INFO, ex.getMessage(), ex);
            ex.printStackTrace();
          }
        }
      }
    }
    this.startTime = System.currentTimeMillis();
    long start = System.currentTimeMillis();
    int rest = 0;
    if (num == this.numberOfZonesX) {
      rest = this.crets.length % this.numberOfZonesX;
    }
    for (int x = this.currentCreature; x < rest + this.crets.length / this.numberOfZonesX * num; x++)
    {
      this.currentCreature += 1;
      try
      {
        VolaTile t = this.crets[x].getCurrentTile();
        if (this.crets[x].poll())
        {
          if (t != null) {
            t.deleteCreature(this.crets[x]);
          }
        }
        else if ((this.crets[x].isDoLavaDamage()) && 
          (this.crets[x].doLavaDamage())) {
          if (t != null) {
            t.deleteCreature(this.crets[x]);
          }
        }
        if (this.crets[x].isDoAreaDamage()) {
          if ((t != null) && 
            (t.doAreaDamage(this.crets[x]))) {
            if (t != null) {
              t.deleteCreature(this.crets[x]);
            }
          }
        }
      }
      catch (Exception ex)
      {
        logger.log(Level.INFO, ex.getMessage(), ex);
        ex.printStackTrace();
      }
    }
    this.totalTime += System.currentTimeMillis() - start;
  }
  
  public boolean isLog()
  {
    return this.logCreaturePolls;
  }
  
  public void setLog(boolean log)
  {
    this.logCreaturePolls = log;
  }
  
  public static final Creature[] getManagedAnimalsFor(Player player, int villageId, boolean includeAll)
  {
    Set<Creature> animals = new HashSet();
    if ((villageId >= 0) && (includeAll)) {
      for (Creature animal : getInstance().getBranded(villageId)) {
        animals.add(animal);
      }
    }
    for (??? = getInstance().creatures.values().iterator(); ((Iterator)???).hasNext();)
    {
      Creature animal = (Creature)((Iterator)???).next();
      
      long whom = getInstance().getCreatureProctector(animal);
      if (whom == player.getWurmId()) {
        animals.add(animal);
      } else if ((animal.canManage(player)) && (!animal.isWagoner())) {
        animals.add(animal);
      }
    }
    if (player.getPet() != null) {
      animals.add(player.getPet());
    }
    return (Creature[])animals.toArray(new Creature[animals.size()]);
  }
  
  public static final Creature[] getManagedWagonersFor(Player player, int villageId)
  {
    Set<Creature> animals = new HashSet();
    if (!Servers.isThisAPvpServer()) {
      for (Map.Entry<Long, Wagoner> entry : Wagoner.getWagoners().entrySet()) {
        if (((Wagoner)entry.getValue()).getVillageId() == villageId)
        {
          Creature wagoner = ((Wagoner)entry.getValue()).getCreature();
          if (wagoner != null) {
            animals.add(wagoner);
          }
        }
        else
        {
          Creature wagoner = ((Wagoner)entry.getValue()).getCreature();
          if ((wagoner != null) && (wagoner.canManage(player))) {
            animals.add(wagoner);
          }
        }
      }
    }
    return (Creature[])animals.toArray(new Creature[animals.size()]);
  }
  
  public static final Set<Creature> getMayUseWagonersFor(Creature performer)
  {
    Set<Creature> wagoners = new HashSet();
    if (!Servers.isThisAPvpServer()) {
      for (Map.Entry<Long, Wagoner> entry : Wagoner.getWagoners().entrySet())
      {
        Wagoner wagoner = (Wagoner)entry.getValue();
        Creature creature = wagoner.getCreature();
        if ((wagoner.getVillageId() != -1) && (creature != null) && (creature.mayUse(performer))) {
          wagoners.add(creature);
        }
      }
    }
    return wagoners;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\creatures\Creatures.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */