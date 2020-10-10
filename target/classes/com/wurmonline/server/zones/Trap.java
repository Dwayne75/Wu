package com.wurmonline.server.zones;

import com.wurmonline.mesh.Tiles;
import com.wurmonline.mesh.Tiles.Tile;
import com.wurmonline.server.DbConnector;
import com.wurmonline.server.Items;
import com.wurmonline.server.Server;
import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.behaviours.ActionEntry;
import com.wurmonline.server.behaviours.Actions;
import com.wurmonline.server.behaviours.Terraforming;
import com.wurmonline.server.bodys.Body;
import com.wurmonline.server.combat.ArmourTemplate;
import com.wurmonline.server.combat.CombatEngine;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.CreatureStatus;
import com.wurmonline.server.creatures.NoArmourException;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.NoSpaceException;
import com.wurmonline.server.players.Cultist;
import com.wurmonline.server.skills.NoSuchSkillException;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.skills.Skills;
import com.wurmonline.server.sounds.SoundPlayer;
import com.wurmonline.server.structures.Structure;
import com.wurmonline.server.utils.DbUtilities;
import com.wurmonline.server.villages.Village;
import com.wurmonline.shared.constants.SoundNames;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class Trap
  implements TimeConstants, SoundNames
{
  private static final Logger logger = Logger.getLogger(Trap.class.getName());
  private static final String INSERT_TRAP = "INSERT INTO TRAPS(TYPE,QL,KINGDOM,VILLAGE,ID,FDAMQL,ROTDAMQL,SPEEDBON) VALUES (?,?,?,?,?,?,?,?)";
  private static final String UPDATE_TRAPS = "UPDATE TRAPS SET QL=QL-1";
  private static final String DELETE_TRAP = "DELETE FROM TRAPS WHERE ID=?";
  private static final String DELETE_DECAYED_TRAPS = "DELETE FROM TRAPS WHERE QL<=0";
  private static final String LOAD_ALL_TRAPS = "SELECT * FROM TRAPS";
  private static final Map<Integer, Trap> traps = new HashMap();
  private static final Map<Integer, Trap> quickTraps = new HashMap();
  private static long lastPolled = System.currentTimeMillis();
  private static long lastPolledQuick = System.currentTimeMillis();
  static final byte TYPE_STICKS = 0;
  static final byte TYPE_POLE = 1;
  static final byte TYPE_CORROSION = 2;
  static final byte TYPE_AXE = 3;
  static final byte TYPE_KNIFE = 4;
  static final byte TYPE_NET = 5;
  static final byte TYPE_SCYTHE = 6;
  static final byte TYPE_MAN = 7;
  static final byte TYPE_BOW = 8;
  static final byte TYPE_ROPE = 9;
  public static final byte TYPE_FORECAST = 10;
  private static final byte[] emptyPos = new byte[0];
  private static final byte[] feet = { 11, 12, 16, 15 };
  private final byte type;
  private byte ql;
  private final byte kingdom;
  private final byte fdamql;
  private final byte rotdamql;
  private byte speedbon;
  private final int village;
  private final int id;
  
  public Trap(byte _type, byte _ql, byte _kingdom, int _village, int _id, byte _rotdamql, byte _fdamql, byte _speedbon)
  {
    this.type = _type;
    this.ql = _ql;
    this.kingdom = _kingdom;
    this.village = _village;
    this.id = _id;
    this.rotdamql = _rotdamql;
    this.fdamql = _fdamql;
    this.speedbon = _speedbon;
  }
  
  private final boolean setQl(byte newQl)
  {
    this.ql = newQl;
    return this.ql <= 0;
  }
  
  public static void checkQuickUpdate()
    throws IOException
  {
    if (System.currentTimeMillis() - lastPolledQuick > 1000L)
    {
      lastPolledQuick = System.currentTimeMillis();
      Integer[] ints = (Integer[])quickTraps.keySet().toArray(new Integer[quickTraps.size()]);
      for (int x = 0; x < ints.length; x++)
      {
        Trap t = (Trap)quickTraps.get(ints[x]);
        if (t.setQl((byte)(t.getQualityLevel() - 1)))
        {
          quickTraps.remove(ints[x]);
          traps.remove(ints[x]);
        }
      }
    }
  }
  
  public static void checkUpdate()
    throws IOException
  {
    if (System.currentTimeMillis() - lastPolled > 21600000L)
    {
      lastPolled = System.currentTimeMillis();
      Connection dbcon = null;
      PreparedStatement ps = null;
      try
      {
        dbcon = DbConnector.getZonesDbCon();
        ps = dbcon.prepareStatement("UPDATE TRAPS SET QL=QL-1");
        ps.executeUpdate();
      }
      catch (SQLException sqx)
      {
        throw new IOException(sqx);
      }
      finally
      {
        DbUtilities.closeDatabaseObjects(ps, null);
        DbConnector.returnConnection(dbcon);
      }
      Integer[] ints = (Integer[])traps.keySet().toArray(new Integer[traps.size()]);
      for (int x = 0; x < ints.length; x++)
      {
        Trap t = (Trap)traps.get(ints[x]);
        if (t.setQl((byte)(t.getQualityLevel() - 1))) {
          traps.remove(ints[x]);
        }
      }
      try
      {
        dbcon = DbConnector.getZonesDbCon();
        ps = dbcon.prepareStatement("DELETE FROM TRAPS WHERE QL<=0");
        ps.executeUpdate();
      }
      catch (SQLException sqx)
      {
        throw new IOException(sqx);
      }
      finally
      {
        DbUtilities.closeDatabaseObjects(ps, null);
        DbConnector.returnConnection(dbcon);
      }
    }
  }
  
  public static int createId(int tilex, int tiley, int layer)
  {
    return (tilex << 17) - (tiley << 4) + (layer & 0xFF);
  }
  
  public static Trap getTrap(int tilex, int tiley, int layer)
  {
    return (Trap)traps.get(Integer.valueOf(createId(tilex, tiley, layer)));
  }
  
  public void create()
    throws IOException
  {
    if (isQuick())
    {
      quickTraps.put(Integer.valueOf(this.id), this);
      traps.put(Integer.valueOf(this.id), this);
    }
    else
    {
      Connection dbcon = null;
      PreparedStatement ps = null;
      try
      {
        dbcon = DbConnector.getZonesDbCon();
        ps = dbcon.prepareStatement("INSERT INTO TRAPS(TYPE,QL,KINGDOM,VILLAGE,ID,FDAMQL,ROTDAMQL,SPEEDBON) VALUES (?,?,?,?,?,?,?,?)");
        ps.setByte(1, this.type);
        ps.setByte(2, this.ql);
        ps.setByte(3, this.kingdom);
        ps.setInt(4, this.village);
        ps.setInt(5, this.id);
        ps.setByte(6, this.fdamql);
        ps.setByte(7, this.rotdamql);
        ps.setByte(8, this.speedbon);
        ps.executeUpdate();
        traps.put(Integer.valueOf(this.id), this);
      }
      catch (SQLException sqx)
      {
        throw new IOException(sqx);
      }
      finally
      {
        DbUtilities.closeDatabaseObjects(ps, null);
        DbConnector.returnConnection(dbcon);
      }
    }
  }
  
  public void delete()
    throws IOException
  {
    quickTraps.remove(Integer.valueOf(this.id));
    traps.remove(Integer.valueOf(this.id));
    Connection dbcon = null;
    PreparedStatement ps = null;
    try
    {
      dbcon = DbConnector.getZonesDbCon();
      ps = dbcon.prepareStatement("DELETE FROM TRAPS WHERE ID=?");
      ps.setInt(1, this.id);
      ps.executeUpdate();
    }
    catch (SQLException sqx)
    {
      logger.log(Level.WARNING, "Problem deleting Trap id " + this.id, sqx);
      throw new IOException(sqx);
    }
    finally
    {
      DbUtilities.closeDatabaseObjects(ps, null);
      DbConnector.returnConnection(dbcon);
    }
  }
  
  public boolean isQuick()
  {
    return this.type == 10;
  }
  
  public static void loadAllTraps()
    throws IOException
  {
    Connection dbcon = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    try
    {
      dbcon = DbConnector.getZonesDbCon();
      ps = dbcon.prepareStatement("SELECT * FROM TRAPS");
      rs = ps.executeQuery();
      while (rs.next())
      {
        int id = rs.getInt("ID");
        byte type = rs.getByte("TYPE");
        byte kingdom = rs.getByte("KINGDOM");
        byte ql = rs.getByte("QL");
        int village = rs.getInt("VILLAGE");
        byte fdamql = rs.getByte("FDAMQL");
        byte rotdamql = rs.getByte("ROTDAMQL");
        byte speedbon = rs.getByte("SPEEDBON");
        Trap trap = new Trap(type, ql, kingdom, village, id, fdamql, rotdamql, speedbon);
        traps.put(Integer.valueOf(id), trap);
        if (trap.isQuick()) {
          quickTraps.put(Integer.valueOf(id), trap);
        }
      }
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
  }
  
  public static byte getTypeForTemplate(int template)
  {
    if (template == 619) {
      return 9;
    }
    if (template == 610) {
      return 0;
    }
    if (template == 611) {
      return 1;
    }
    if (template == 612) {
      return 2;
    }
    if (template == 613) {
      return 3;
    }
    if (template == 614) {
      return 4;
    }
    if (template == 615) {
      return 5;
    }
    if (template == 616) {
      return 6;
    }
    if (template == 617) {
      return 7;
    }
    if (template == 618) {
      return 8;
    }
    logger.log(Level.INFO, "Unknown trap type for templateid " + template);
    return 0;
  }
  
  public boolean mayTrapRemainOnTile(byte tiletype)
  {
    if (this.type == 9) {
      return isRopeTile(tiletype);
    }
    if (this.type == 0) {
      return isSticksTile(tiletype);
    }
    if (this.type == 1) {
      return isPoleTile(tiletype);
    }
    if (this.type == 2) {
      return isCorrosionTile(tiletype);
    }
    if (this.type == 3) {
      return isPoleTile(tiletype);
    }
    if (this.type == 4) {
      return isSticksTile(tiletype);
    }
    if (this.type == 5) {
      return isTreeTile(tiletype);
    }
    if (this.type == 6) {
      return isRockTile(tiletype);
    }
    if (this.type == 7) {
      return isSticksTile(tiletype);
    }
    if (this.type == 8) {
      return (isPoleTile(tiletype)) || (isRockTile(tiletype));
    }
    if (this.type == 10) {
      return true;
    }
    return false;
  }
  
  public static boolean mayTrapTemplateOnTile(int template, byte tiletype)
  {
    if (template == 619) {
      return isRopeTile(tiletype);
    }
    if (template == 610) {
      return isSticksTile(tiletype);
    }
    if (template == 611) {
      return isPoleTile(tiletype);
    }
    if (template == 612) {
      return isCorrosionTile(tiletype);
    }
    if (template == 613) {
      return isPoleTile(tiletype);
    }
    if (template == 614) {
      return isSticksTile(tiletype);
    }
    if (template == 615) {
      return isTreeTile(tiletype);
    }
    if (template == 616) {
      return isRockTile(tiletype);
    }
    if (template == 617) {
      return isSticksTile(tiletype);
    }
    if (template == 618) {
      return (isPoleTile(tiletype)) || (isRockTile(tiletype));
    }
    return false;
  }
  
  static boolean isRopeTile(byte type)
  {
    return (Tiles.getTile(type).isTree()) || (type == Tiles.Tile.TILE_GRASS.id) || (type == Tiles.Tile.TILE_MYCELIUM.id) || (type == Tiles.Tile.TILE_CLAY.id) || (type == Tiles.Tile.TILE_MARSH.id) || (type == Tiles.Tile.TILE_PEAT.id) || (type == Tiles.Tile.TILE_TAR.id) || (type == Tiles.Tile.TILE_MOSS.id) || (type == Tiles.Tile.TILE_STEPPE.id) || (type == Tiles.Tile.TILE_ENCHANTED_GRASS.id) || (type == Tiles.Tile.TILE_SAND.id) || (type == Tiles.Tile.TILE_DIRT.id);
  }
  
  static boolean isSticksTile(byte type)
  {
    return (type == Tiles.Tile.TILE_GRASS.id) || (type == Tiles.Tile.TILE_CLAY.id) || (type == Tiles.Tile.TILE_MARSH.id) || (type == Tiles.Tile.TILE_PEAT.id) || (type == Tiles.Tile.TILE_TAR.id) || (type == Tiles.Tile.TILE_MOSS.id) || (type == Tiles.Tile.TILE_MYCELIUM.id) || (type == Tiles.Tile.TILE_ENCHANTED_GRASS.id) || (type == Tiles.Tile.TILE_STEPPE.id) || (type == Tiles.Tile.TILE_FIELD.id) || (type == Tiles.Tile.TILE_FIELD2.id) || (type == Tiles.Tile.TILE_SAND.id) || (type == Tiles.Tile.TILE_DIRT.id);
  }
  
  static boolean isPoleTile(byte type)
  {
    return (Tiles.getTile(type).isTree()) || (type == Tiles.Tile.TILE_MARSH.id) || (type == Tiles.Tile.TILE_TAR.id);
  }
  
  static boolean isTreeTile(byte type)
  {
    return Tiles.getTile(type).isTree();
  }
  
  static boolean isCorrosionTile(byte type)
  {
    return Tiles.getTile(type).isTree();
  }
  
  static boolean isRockTile(byte type)
  {
    return (type == Tiles.Tile.TILE_CAVE.id) || (type == Tiles.Tile.TILE_CAVE_EXIT.id) || (type == Tiles.Tile.TILE_ROCK.id);
  }
  
  public static boolean mayPlantCorrosion(int tilex, int tiley, int layer)
  {
    if (layer < 0) {
      return true;
    }
    VolaTile t = Zones.getTileOrNull(tilex, tiley, layer >= 0);
    if (t != null) {
      if ((t.getStructure() != null) && (t.getStructure().isFinished())) {
        return true;
      }
    }
    return false;
  }
  
  public static boolean disarm(Creature performer, Item disarmItem, int tilex, int tiley, int layer, float counter, Action act)
  {
    boolean toReturn = true;
    if (disarmItem.isDisarmTrap())
    {
      double power = 0.0D;
      int time = 2000;
      
      toReturn = false;
      if (counter == 1.0F)
      {
        Skill trapping = null;
        try
        {
          trapping = performer.getSkills().getSkill(10084);
        }
        catch (NoSuchSkillException nss)
        {
          trapping = performer.getSkills().learn(10084, 1.0F);
        }
        time = Actions.getStandardActionTime(performer, trapping, disarmItem, 0.0D);
        act.setTimeLeft(time);
        performer.getCommunicator().sendNormalServerMessage("You try to trigger any traps in the area.");
        Server.getInstance().broadCastAction(performer.getName() + " starts to trigger traps in the area.", performer, 5);
        
        performer.sendActionControl(Actions.actionEntrys['ŷ'].getVerbString(), true, time);
      }
      else
      {
        time = act.getTimeLeft();
      }
      if (counter * 10.0F > time)
      {
        Skill trapping = null;
        try
        {
          trapping = performer.getSkills().getSkill(10084);
        }
        catch (NoSuchSkillException nss)
        {
          trapping = performer.getSkills().learn(10084, 1.0F);
        }
        power = trapping.skillCheck(10.0D, disarmItem.getCurrentQualityLevel(), true, counter);
        toReturn = true;
        if (power > 0.0D)
        {
          Trap t = getTrap(tilex, tiley, performer.getLayer());
          if (t != null)
          {
            try
            {
              t.delete();
            }
            catch (IOException iox)
            {
              performer.getCommunicator().sendNormalServerMessage("You detect a " + t
                .getName() + " but nothing happens. It may still be armed.");
              return true;
            }
            power = trapping.skillCheck(10.0D, disarmItem.getCurrentQualityLevel(), false, counter);
            SoundPlayer.playSound(t.getSound(), tilex, tiley, performer.isOnSurface(), 0.0F);
            
            String tosend = "You trigger a  " + t.getName() + "!";
            performer.getStatus().modifyStamina(1000.0F);
            performer.getCommunicator().sendNormalServerMessage(tosend);
            Server.getInstance().broadCastAction(performer.getName() + " trigger a " + t.getName() + ".", performer, 5);
            
            Items.destroyItem(disarmItem.getWurmId());
          }
          else
          {
            performer.getCommunicator().sendNormalServerMessage("Nothing happens.");
          }
        }
        else
        {
          performer.getCommunicator().sendNormalServerMessage("Nothing happens.");
        }
      }
    }
    else
    {
      performer.getCommunicator().sendNormalServerMessage("You can not disarm traps safely with " + disarmItem
        .getName() + ".");
    }
    return toReturn;
  }
  
  private String getSound()
  {
    return getSoundForTrapType(this.type);
  }
  
  static String getSoundForTrapType(byte type)
  {
    switch (type)
    {
    case 0: 
      return "sound.trap.chak";
    case 1: 
      return "sound.trap.thuk";
    case 2: 
      return "sound.trap.splash";
    case 3: 
      return "sound.trap.thuk";
    case 4: 
      return "sound.trap.wham";
    case 5: 
      return "sound.trap.swish";
    case 6: 
      return "sound.trap.scith";
    case 7: 
      return "sound.trap.chak";
    case 9: 
      return "sound.trap.swish";
    case 8: 
      return "sound.trap.thuk";
    }
    return "sound.trap.thuk";
  }
  
  static byte getDamageForTrapType(byte type)
  {
    switch (type)
    {
    case 0: 
      return 2;
    case 1: 
      return 2;
    case 2: 
      return 10;
    case 3: 
      return 1;
    case 4: 
      return 2;
    case 5: 
      return -1;
    case 6: 
      return 1;
    case 7: 
      return 1;
    case 9: 
      return -1;
    case 8: 
      return 2;
    case 10: 
      return 10;
    }
    return 2;
  }
  
  public String getName()
  {
    return getNameForTrapType(this.type);
  }
  
  public byte getKingdom()
  {
    return this.kingdom;
  }
  
  public int getVillage()
  {
    return this.village;
  }
  
  public byte getFireDamage()
  {
    return this.fdamql;
  }
  
  public byte getRotDamage()
  {
    return this.rotdamql;
  }
  
  public byte getSpeedBon()
  {
    return this.speedbon;
  }
  
  public byte getQualityLevel()
  {
    return this.ql;
  }
  
  public void doEffect(Creature performer, int tilex, int tiley, int layer)
  {
    SoundPlayer.playSound(getSound(), tilex, tiley, layer >= 0, 0.0F);
    try
    {
      delete();
    }
    catch (IOException iox)
    {
      performer.getCommunicator().sendNormalServerMessage("A " + 
        getName() + " triggers but nothing happens. It may still be armed.");
      return;
    }
    if (this.speedbon == 0)
    {
      this.speedbon = 2;
    }
    else if ((performer.getCultist() != null) && (performer.getCultist().ignoresTraps()))
    {
      performer.getCommunicator().sendSafeServerMessage("A " + getName() + " triggers but you easily avoid it!");
    }
    else if (performer.getBodyControlSkill().skillCheck(Server.rand.nextInt(100 + this.speedbon / 2), -this.ql, false, 10.0F) > 0.0D)
    {
      performer.getCommunicator().sendSafeServerMessage("A " + getName() + " triggers but you manage to avoid it!");
    }
    else
    {
      byte damtype = getDamageForTrapType(this.type);
      if (damtype == -1)
      {
        performer.getCommunicator().sendAlertServerMessage("A " + 
          getName() + " entangles you! Breaking free tires you.");
        performer.getStatus().modifyStamina2(-this.ql / 100.0F);
      }
      else
      {
        performer.getCommunicator().sendAlertServerMessage("A " + getName() + " triggers and hits you with full force!");
        
        byte trapType = 54;
        int nums;
        byte[] poses;
        float percentSpell;
        int baseDam;
        switch (this.type)
        {
        case 0: 
          int nums = Server.rand.nextInt(this.ql / 10) + 1;
          byte[] poses = emptyPos;
          float percentSpell = 0.2F;
          int baseDam = 150;
          trapType = 54;
          break;
        case 1: 
          int nums = 1;
          byte[] poses = emptyPos;
          float percentSpell = 1.0F;
          int baseDam = 250;
          trapType = 55;
          break;
        case 2: 
          int nums = Server.rand.nextInt(this.ql / 10) + 1;
          byte[] poses = emptyPos;
          float percentSpell = 0.2F;
          int baseDam = 150;
          trapType = 56;
          break;
        case 3: 
          int nums = 1;
          byte[] poses = emptyPos;
          float percentSpell = 1.0F;
          int baseDam = 300;
          trapType = 57;
          break;
        case 4: 
          int nums = Server.rand.nextInt(this.ql / 5) + 1;
          byte[] poses = emptyPos;
          float percentSpell = 0.3F;
          int baseDam = 200;
          trapType = 58;
          break;
        case 6: 
          int nums = 1;
          byte[] poses = emptyPos;
          float percentSpell = 1.0F;
          int baseDam = 350;
          trapType = 60;
          break;
        case 7: 
          int nums = 1;
          byte[] poses = feet;
          float percentSpell = 1.0F;
          int baseDam = 300;
          trapType = 61;
          break;
        case 8: 
          int nums = 1;
          byte[] poses = emptyPos;
          float percentSpell = 1.0F;
          int baseDam = 300;
          trapType = 62;
          break;
        case 10: 
          int nums = 1;
          byte[] poses = emptyPos;
          float percentSpell = 1.0F;
          int baseDam = 300;
          trapType = 71;
          break;
        case 5: 
        case 9: 
        default: 
          nums = 0;
          poses = emptyPos;
          percentSpell = 0.0F;
          baseDam = 300;
        }
        VolaTile t = performer.getCurrentTile();
        if (t != null) {
          t.sendAddQuickTileEffect(trapType, layer * 30);
        }
        if (performer.isUnique()) {
          baseDam = (int)(baseDam * 0.3F);
        }
        addDamage(performer, baseDam, nums, damtype, poses, percentSpell);
      }
    }
  }
  
  private void addDamage(Creature creature, int baseDam, int nums, byte damtype, byte[] positions, float percentSpellDamage)
  {
    for (int x = 0; x < nums; x++) {
      try
      {
        byte pos = creature.getBody().getRandomWoundPos();
        if (positions.length > 0) {
          pos = positions[Server.rand.nextInt(positions.length)];
        }
        float armourMod = 1.0F;
        try
        {
          byte bodyPosition = ArmourTemplate.getArmourPosition(pos);
          Item armour = creature.getArmour(bodyPosition);
          armourMod = ArmourTemplate.calculateDR(armour, damtype);
          if (creature.isPlayer()) {
            armour.setDamage(armour.getDamage() + 
              Math.max(0.05F, 
              
              Math.min(1.0F, baseDam * this.ql * ArmourTemplate.getArmourDamageModFor(armour, damtype) / 1200000.0F * armour
              .getDamageModifier())));
          }
        }
        catch (NoArmourException localNoArmourException) {}catch (NoSpaceException nsp)
        {
          logger.log(Level.WARNING, creature.getName() + " no armour space on loc " + pos);
        }
        if (creature.getBonusForSpellEffect((byte)22) > 0.0F) {
          if (armourMod >= 1.0F) {
            armourMod = 0.2F + (1.0F - creature.getBonusForSpellEffect((byte)22) / 100.0F) * 0.6F;
          } else {
            armourMod = Math.min(armourMod, 0.2F + 
              (1.0F - creature.getBonusForSpellEffect((byte)22) / 100.0F) * 0.6F);
          }
        }
        if (!creature.isDead()) {
          CombatEngine.addWound(null, creature, damtype, pos, this.ql * baseDam, armourMod, "hits", null, 0.0F, 0.0F, false, false, false, false);
        } else {
          return;
        }
        if (Server.rand.nextFloat() < percentSpellDamage)
        {
          if ((!creature.isDead()) && (this.rotdamql > 0)) {
            creature.addWoundOfType(null, (byte)6, pos, false, 1.0F, true, this.rotdamql * baseDam / 10.0F, this.ql, 0.0F, false, false);
          } else {
            return;
          }
          if ((!creature.isDead()) && (this.fdamql > 0)) {
            creature.addWoundOfType(null, (byte)4, pos, false, 1.0F, true, this.fdamql * baseDam / 10.0F, 0.0F, 0.0F, false, false);
          } else {
            return;
          }
        }
      }
      catch (Exception ex)
      {
        logger.log(Level.WARNING, ex.getMessage(), ex);
        return;
      }
    }
  }
  
  static String getNameForTrapType(byte type)
  {
    switch (type)
    {
    case 0: 
      return "stick trap";
    case 1: 
      return "pole trap";
    case 2: 
      return "corrosion trap";
    case 3: 
      return "axe trap";
    case 4: 
      return "knife trap";
    case 5: 
      return "net trap";
    case 6: 
      return "scythe trap";
    case 7: 
      return "man trap";
    case 9: 
      return "rope trap";
    case 8: 
      return "bow trap";
    }
    return "trap";
  }
  
  public static String getQualityLevelString(byte qualityLevel)
  {
    String qlString;
    String qlString;
    if (qualityLevel < 20)
    {
      qlString = "low";
    }
    else
    {
      String qlString;
      if (qualityLevel > 80)
      {
        qlString = "deadly";
      }
      else
      {
        String qlString;
        if (qualityLevel > 50) {
          qlString = "high";
        } else {
          qlString = "average";
        }
      }
    }
    return qlString;
  }
  
  public static boolean trap(Creature performer, Item trap, int tile, int tilex, int tiley, int layer, float counter, Action act)
  {
    boolean toReturn = true;
    boolean ok = false;
    if (trap.isTrap()) {
      if (mayTrapTemplateOnTile(trap.getTemplateId(), Tiles.decodeType(tile)))
      {
        if (getTrap(tilex, tiley, performer.getLayer()) == null) {
          ok = true;
        }
      }
      else if (trap.getTemplateId() == 612) {
        if (mayPlantCorrosion(tilex, tiley, performer.getLayer())) {
          if (getTrap(tilex, tiley, performer.getLayer()) == null) {
            ok = true;
          }
        }
      }
    }
    if (ok)
    {
      double power = 0.0D;
      int time = 2000;
      
      toReturn = false;
      if (counter == 1.0F)
      {
        Skill trapping = null;
        try
        {
          trapping = performer.getSkills().getSkill(10084);
        }
        catch (NoSuchSkillException nss)
        {
          trapping = performer.getSkills().learn(10084, 1.0F);
        }
        if (Terraforming.isCornerUnderWater(tilex, tiley, performer.isOnSurface()))
        {
          performer.getCommunicator().sendNormalServerMessage("The ground is too moist here, the trap would not work.");
          
          return true;
        }
        time = Actions.getSlowActionTime(performer, trapping, trap, 0.0D);
        act.setTimeLeft(time);
        performer.getCommunicator().sendNormalServerMessage("You start setting the " + trap.getName() + ".");
        Server.getInstance().broadCastAction(performer.getName() + " starts to set " + trap.getNameWithGenus() + ".", performer, 5);
        
        performer.sendActionControl(Actions.actionEntrys['Ŷ'].getVerbString(), true, time);
      }
      else
      {
        time = act.getTimeLeft();
      }
      if (counter * 10.0F > time)
      {
        Skill trapping = null;
        try
        {
          trapping = performer.getSkills().getSkill(10084);
        }
        catch (NoSuchSkillException nss)
        {
          trapping = performer.getSkills().learn(10084, 1.0F);
        }
        power = trapping.skillCheck(trap.getCurrentQualityLevel() / 5.0F, trap.getCurrentQualityLevel(), false, counter);
        toReturn = true;
        if (power > 0.0D)
        {
          SoundPlayer.playSound("sound.trap.set", tilex, tiley, performer.isOnSurface(), 0.0F);
          
          byte type = getTypeForTemplate(trap.getTemplateId());
          
          int villid = 0;
          if (performer.getCitizenVillage() != null) {
            villid = performer.getCitizenVillage().id;
          }
          try
          {
            Trap t = new Trap(type, (byte)(int)trap.getCurrentQualityLevel(), performer.getKingdomId(), villid, createId(tilex, tiley, layer), (byte)(int)trap.getSpellDamageBonus(), (byte)(int)trap.getSpellRotModifier(), (byte)(int)trap.getSpellSpeedBonus());
            t.create();
          }
          catch (IOException iox)
          {
            performer.getCommunicator().sendNormalServerMessage("Something goes awry! You sense bad omens and can not set traps right now.");
            
            return true;
          }
          String tosend = "You carefully set the " + trap.getName() + ".";
          performer.getStatus().modifyStamina(-1000.0F);
          performer.getCommunicator().sendNormalServerMessage(tosend);
          Server.getInstance().broadCastAction(performer.getName() + " sets " + trap.getNameWithGenus() + ".", performer, 5);
        }
        else
        {
          performer.getCommunicator().sendNormalServerMessage("Sadly, you fail to set the trap correctly. The trap triggers and is destroyed.");
        }
        Items.destroyItem(trap.getWurmId());
      }
    }
    else
    {
      performer.getCommunicator().sendNormalServerMessage("You can not trap that place.");
    }
    return toReturn;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\zones\Trap.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */