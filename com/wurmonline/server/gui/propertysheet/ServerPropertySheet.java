package com.wurmonline.server.gui.propertysheet;

import coffee.keenan.network.helpers.address.AddressHelper;
import com.wurmonline.server.Constants;
import com.wurmonline.server.ServerEntry;
import com.wurmonline.server.ServerProperties;
import com.wurmonline.server.Servers;
import com.wurmonline.server.steam.SteamHandler;
import java.io.PrintStream;
import java.net.InetAddress;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import org.controlsfx.control.PropertySheet;
import org.controlsfx.control.PropertySheet.Item;
import org.controlsfx.control.PropertySheet.Mode;
import org.controlsfx.property.editor.DefaultPropertyEditorFactory;
import org.controlsfx.property.editor.PropertyEditor;

public final class ServerPropertySheet
  extends VBox
{
  private static final Logger logger = Logger.getLogger(ServerPropertySheet.class.getName());
  private ServerEntry current;
  private PropertySheet propertySheet;
  private final ObservableList<PropertySheet.Item> list;
  
  private static enum PropertyType
  {
    SERVERID,  EXTSERVERIP,  EXTSERVERPORT,  INTIP,  INTPORT,  MAXPLAYERS,  SKILLGAINRATE,  ACTIONTIMER,  RMIPORT,  RMI_REG_PORT,  PVPSERVER,  HOMESERVER,  SPAWNPOINTJENNX,  SPAWNPOINTJENNY,  SPAWNPOINTMOLX,  SPAWNPOINTMOLY,  SPAWNPOINTLIBX,  SPAWNPOINTLIBY,  KINGDOM,  INTRASERVERPASSWORD,  TESTSERVER,  NAME,  LOGINSERVER,  ISPAYMENT,  TWITTERCONSUMERKEY,  TWITTERCONSUMERSECRET,  TWITTERAPPTOKEN,  TWITTERAPPSECRET,  MAINTAINING,  HOTADELAY,  MAXCREATURES,  PERCENTAGG,  RANDOMSPAWNS,  SKBASIC,  SKFIGHT,  SKMIND,  SKOVERALL,  SKBC,  EPIC,  CRMOD,  STEAMPW,  UPKEEP,  MAXDEED,  FREEDEEDS,  TRADERMAX,  TRADERINIT,  TUNNELING,  BREEDING,  FIELDGROWTH,  KINGSMONEY,  MOTD,  NPCS,  STEAMQUERYPORT,  TREEGROWTH,  ADMIN_PWD,  ENDGAMEITEMS,  SPY_PREVENTION,  NEWBIE_FRIENDLY,  ENABLE_PNP_PORT_FORWARD;
    
    private PropertyType() {}
  }
  
  private final String categoryServerSettings = "1: Server Settings";
  private final String categoryAdvanceSettings = "2: Advance Server Settings";
  private final String categoryTweaks = "3: Gameplay Tweaks";
  private final String categoryTwitter = "4: Twitter Settings";
  private final String categoryMaintenance = "5: Maintenance";
  private final String categoryOtherServerSettings = "1: Server Settings";
  private Set<PropertyType> changedProperties = new HashSet();
  
  class CustomPropertyItem
    implements PropertySheet.Item
  {
    private ServerPropertySheet.PropertyType type;
    private String category;
    private String name;
    private String description;
    private boolean editable = true;
    private Object value;
    private Object minValue;
    private Object maxValue;
    
    CustomPropertyItem(ServerPropertySheet.PropertyType aType, String aCategory, String aName, String aDescription, boolean aEditable, Object aValue)
    {
      this.type = aType;
      this.category = aCategory;
      this.name = aName;
      this.description = aDescription;
      this.editable = aEditable;
      this.value = aValue;
    }
    
    CustomPropertyItem(ServerPropertySheet.PropertyType aType, String aCategory, String aName, String aDescription, boolean aEditable, Object aValue, Object aMinValue)
    {
      this(aType, aCategory, aName, aDescription, aEditable, aValue);
      this.minValue = aMinValue;
    }
    
    CustomPropertyItem(ServerPropertySheet.PropertyType aType, String aCategory, String aName, String aDescription, boolean aEditable, Object aValue, Object aMinValue, Object aMaxValue)
    {
      this(aType, aCategory, aName, aDescription, aEditable, aValue, aMinValue);
      this.maxValue = aMaxValue;
    }
    
    public ServerPropertySheet.PropertyType getPropertyType()
    {
      return this.type;
    }
    
    public Class<?> getType()
    {
      return this.value.getClass();
    }
    
    public String getCategory()
    {
      return this.category;
    }
    
    public String getName()
    {
      return this.name;
    }
    
    public String getDescription()
    {
      return this.description;
    }
    
    public Optional<Class<? extends PropertyEditor<?>>> getPropertyEditorClass()
    {
      return super.getPropertyEditorClass();
    }
    
    public boolean isEditable()
    {
      return this.editable;
    }
    
    public Object getValue()
    {
      return this.value;
    }
    
    public Object getMinValue()
    {
      return this.minValue;
    }
    
    public Object getMaxValue()
    {
      return this.maxValue;
    }
    
    public void setValue(Object aValue)
    {
      Object newValue = aValue;
      if (!this.value.equals(newValue)) {
        ServerPropertySheet.this.changedProperties.add(this.type);
      }
      this.value = newValue;
    }
    
    public Optional<ObservableValue<? extends Object>> getObservableValue()
    {
      return Optional.of(new SimpleObjectProperty(this.value));
    }
  }
  
  boolean saveNewGui = false;
  boolean saveSpawns = false;
  boolean saveTwitter = false;
  boolean changedId = false;
  int oldId = 0;
  private static final String PASSWORD_CHARS = "abcdefgijkmnopqrstwxyzABCDEFGHJKLMNPQRSTWXYZ23456789";
  
  public final String save()
  {
    String toReturn = "";
    boolean saveAtAll = false;
    for (CustomPropertyItem item : (CustomPropertyItem[])this.list.toArray(new CustomPropertyItem[this.list.size()])) {
      if ((this.changedProperties.contains(item.getPropertyType())) || (this.current.isCreating))
      {
        saveAtAll = true;
        try
        {
          switch (item.getPropertyType())
          {
          case SERVERID: 
            if (this.current.isLocal)
            {
              this.changedId = true;
              this.oldId = this.current.id;
            }
            this.current.id = ((Integer)item.getValue()).intValue();
            this.saveNewGui = true;
            break;
          case ENABLE_PNP_PORT_FORWARD: 
            boolean upnpSetting = ((Boolean)item.getValue()).booleanValue();
            if (upnpSetting != ServerProperties.getBoolean("ENABLE_PNP_PORT_FORWARD", Constants.enablePnpPortForward))
            {
              ServerProperties.setValue("ENABLE_PNP_PORT_FORWARD", Boolean.toString(upnpSetting));
              ServerProperties.checkProperties();
            }
            break;
          case EXTSERVERIP: 
            this.current.EXTERNALIP = item.getValue().toString();
            this.saveNewGui = true;
            break;
          case EXTSERVERPORT: 
            this.current.EXTERNALPORT = item.getValue().toString();
            this.saveNewGui = true;
            break;
          case INTIP: 
            this.current.INTRASERVERADDRESS = item.getValue().toString();
            this.saveNewGui = true;
            break;
          case INTPORT: 
            this.current.INTRASERVERPORT = item.getValue().toString();
            this.saveNewGui = true;
            break;
          case MAXPLAYERS: 
            this.current.pLimit = ((Integer)item.getValue()).intValue();
            this.saveNewGui = true;
            break;
          case SKILLGAINRATE: 
            this.current.setSkillGainRate(((Float)item.getValue()).floatValue());
            this.saveNewGui = true;
            break;
          case ACTIONTIMER: 
            this.current.setActionTimer(((Float)item.getValue()).floatValue());
            this.saveNewGui = true;
            break;
          case RMIPORT: 
            this.current.RMI_PORT = ((Integer)item.getValue()).intValue();
            this.saveNewGui = true;
            break;
          case RMI_REG_PORT: 
            this.current.REGISTRATION_PORT = ((Integer)item.getValue()).intValue();
            this.saveNewGui = true;
            break;
          case PVPSERVER: 
            this.current.PVPSERVER = ((Boolean)item.getValue()).booleanValue();
            this.saveNewGui = true;
            break;
          case EPIC: 
            this.current.EPIC = ((Boolean)item.getValue()).booleanValue();
            this.saveNewGui = true;
            break;
          case HOMESERVER: 
            this.current.HOMESERVER = ((Boolean)item.getValue()).booleanValue();
            this.saveNewGui = true;
            break;
          case SPAWNPOINTJENNX: 
            this.current.SPAWNPOINTJENNX = ((Integer)item.getValue()).intValue();
            this.saveSpawns = true;
            break;
          case SPAWNPOINTJENNY: 
            this.current.SPAWNPOINTJENNY = ((Integer)item.getValue()).intValue();
            this.saveSpawns = true;
            break;
          case SPAWNPOINTMOLX: 
            this.current.SPAWNPOINTMOLX = ((Integer)item.getValue()).intValue();
            this.saveSpawns = true;
            break;
          case SPAWNPOINTMOLY: 
            this.current.SPAWNPOINTMOLY = ((Integer)item.getValue()).intValue();
            this.saveSpawns = true;
            break;
          case SPAWNPOINTLIBX: 
            this.current.SPAWNPOINTLIBX = ((Integer)item.getValue()).intValue();
            this.saveSpawns = true;
            break;
          case SPAWNPOINTLIBY: 
            this.current.SPAWNPOINTLIBY = ((Integer)item.getValue()).intValue();
            this.saveSpawns = true;
            break;
          case KINGDOM: 
            this.current.KINGDOM = ((Byte)item.getValue()).byteValue();
            this.saveNewGui = true;
            break;
          case INTRASERVERPASSWORD: 
            this.current.INTRASERVERPASSWORD = item.getValue().toString();
            this.saveNewGui = true;
            break;
          case TESTSERVER: 
            this.current.testServer = ((Boolean)item.getValue()).booleanValue();
            this.saveNewGui = true;
            break;
          case NPCS: 
            boolean newSetting = ((Boolean)item.getValue()).booleanValue();
            if (newSetting != ServerProperties.getBoolean("NPCS", Constants.loadNpcs))
            {
              ServerProperties.setValue("NPCS", Boolean.toString(newSetting));
              ServerProperties.checkProperties();
            }
            break;
          case ENDGAMEITEMS: 
            boolean loadEGI = ((Boolean)item.getValue()).booleanValue();
            if (loadEGI != ServerProperties.getBoolean("ENDGAMEITEMS", Constants.loadEndGameItems))
            {
              ServerProperties.setValue("ENDGAMEITEMS", Boolean.toString(loadEGI));
              ServerProperties.checkProperties();
            }
            break;
          case SPY_PREVENTION: 
            boolean spy = ((Boolean)item.getValue()).booleanValue();
            if (spy != ServerProperties.getBoolean("SPYPREVENTION", Constants.enableSpyPrevention))
            {
              ServerProperties.setValue("SPYPREVENTION", Boolean.toString(spy));
              ServerProperties.checkProperties();
            }
            break;
          case NEWBIE_FRIENDLY: 
            boolean newbie = ((Boolean)item.getValue()).booleanValue();
            if (newbie != ServerProperties.getBoolean("NEWBIEFRIENDLY", Constants.isNewbieFriendly))
            {
              ServerProperties.setValue("NEWBIEFRIENDLY", Boolean.toString(newbie));
              ServerProperties.checkProperties();
            }
            break;
          case STEAMQUERYPORT: 
            short sqp = ((Short)item.getValue()).shortValue();
            if (sqp != ServerProperties.getShort("STEAMQUERYPORT", sqp))
            {
              ServerProperties.setValue("STEAMQUERYPORT", Short.toString(sqp));
              ServerProperties.checkProperties();
            }
            break;
          case ADMIN_PWD: 
            String pwd = (String)item.getValue();
            if (!pwd.equals(ServerProperties.getString("ADMINPASSWORD", pwd)))
            {
              ServerProperties.setValue("ADMINPASSWORD", pwd);
              ServerProperties.checkProperties();
            }
            break;
          case NAME: 
            this.current.name = item.getValue().toString();
            this.saveNewGui = true;
            break;
          case LOGINSERVER: 
            this.current.LOGINSERVER = ((Boolean)item.getValue()).booleanValue();
            this.saveNewGui = true;
            break;
          case ISPAYMENT: 
            this.current.ISPAYMENT = false;
            this.saveNewGui = true;
            break;
          case TWITTERCONSUMERKEY: 
            this.current.setConsumerKeyToUse(item.getValue().toString());
            this.saveTwitter = true;
            break;
          case TWITTERCONSUMERSECRET: 
            this.current.setConsumerSecret(item.getValue().toString());
            this.saveTwitter = true;
            break;
          case TWITTERAPPTOKEN: 
            this.current.setApplicationToken(item.getValue().toString());
            this.saveTwitter = true;
            break;
          case TWITTERAPPSECRET: 
            this.current.setApplicationSecret(item.getValue().toString());
            this.saveTwitter = true;
            break;
          case MAINTAINING: 
            this.current.maintaining = ((Boolean)item.getValue()).booleanValue();
            break;
          case MAXCREATURES: 
            this.current.maxCreatures = ((Integer)item.getValue()).intValue();
            this.saveNewGui = true;
            break;
          case PERCENTAGG: 
            this.current.percentAggCreatures = ((Float)item.getValue()).floatValue();
            this.saveNewGui = true;
            break;
          case HOTADELAY: 
            this.current.setHotaDelay(((Integer)item.getValue()).intValue());
            this.saveNewGui = true;
            break;
          case RANDOMSPAWNS: 
            this.current.randomSpawns = ((Boolean)item.getValue()).booleanValue();
            this.saveNewGui = true;
            break;
          case SKBASIC: 
            this.current.setSkillbasicval(((Float)item.getValue()).floatValue());
            this.saveNewGui = true;
            break;
          case SKFIGHT: 
            this.current.setSkillfightval(((Float)item.getValue()).floatValue());
            this.saveNewGui = true;
            break;
          case SKMIND: 
            this.current.setSkillmindval(((Float)item.getValue()).floatValue());
            this.saveNewGui = true;
            break;
          case SKOVERALL: 
            this.current.setSkilloverallval(((Float)item.getValue()).floatValue());
            this.saveNewGui = true;
            break;
          case SKBC: 
            this.current.setSkillbcval(((Float)item.getValue()).floatValue());
            this.saveNewGui = true;
            break;
          case CRMOD: 
            this.current.setCombatRatingModifier(((Float)item.getValue()).floatValue());
            this.saveNewGui = true;
            break;
          case STEAMPW: 
            this.current.setSteamServerPassword(item.getValue().toString());
            this.saveNewGui = true;
            break;
          case UPKEEP: 
            this.current.setUpkeep(((Boolean)item.getValue()).booleanValue());
            this.saveNewGui = true;
            break;
          case MAXDEED: 
            this.current.setMaxDeedSize(((Integer)item.getValue()).intValue());
            this.saveNewGui = true;
            break;
          case FREEDEEDS: 
            this.current.setFreeDeeds(((Boolean)item.getValue()).booleanValue());
            this.saveNewGui = true;
            break;
          case TRADERMAX: 
            this.current.setTraderMaxIrons(((Integer)item.getValue()).intValue() * 10000);
            this.saveNewGui = true;
            break;
          case TRADERINIT: 
            this.current.setInitialTraderIrons(((Integer)item.getValue()).intValue() * 10000);
            this.saveNewGui = true;
            break;
          case TUNNELING: 
            this.current.setTunnelingHits(((Integer)item.getValue()).intValue());
            this.saveNewGui = true;
            break;
          case BREEDING: 
            this.current.setBreedingTimer(((Long)item.getValue()).longValue());
            this.saveNewGui = true;
            break;
          case FIELDGROWTH: 
            this.current.setFieldGrowthTime((((Float)item.getValue()).floatValue() * 3600.0F * 1000.0F));
            this.saveNewGui = true;
            break;
          case TREEGROWTH: 
            this.current.treeGrowth = ((Integer)item.getValue()).intValue();
            this.saveNewGui = true;
            break;
          case KINGSMONEY: 
            this.current.setKingsmoneyAtRestart(((Integer)item.getValue()).intValue() * 10000);
            this.saveNewGui = true;
            break;
          case MOTD: 
            this.current.setMotd(item.getValue().toString());
            this.saveNewGui = true;
          }
        }
        catch (Exception ex)
        {
          saveAtAll = false;
          toReturn = toReturn + "Invalid value " + item.getCategory() + ": " + item.getValue() + ". ";
          logger.log(Level.INFO, "Error " + ex.getMessage(), ex);
        }
      }
    }
    if (toReturn.length() == 0) {
      if (saveAtAll)
      {
        if (this.current.isCreating)
        {
          toReturn = "New server saved";
          Servers.registerServer(this.current.id, this.current.getName(), this.current.HOMESERVER, this.current.SPAWNPOINTJENNX, this.current.SPAWNPOINTJENNY, this.current.SPAWNPOINTLIBX, this.current.SPAWNPOINTLIBY, this.current.SPAWNPOINTMOLX, this.current.SPAWNPOINTMOLY, this.current.INTRASERVERADDRESS, this.current.INTRASERVERPORT, this.current.INTRASERVERPASSWORD, this.current.EXTERNALIP, this.current.EXTERNALPORT, this.current.LOGINSERVER, this.current.KINGDOM, this.current.ISPAYMENT, this.current
          
            .getConsumerKey(), this.current.getConsumerSecret(), this.current
            .getApplicationToken(), this.current.getApplicationSecret(), false, this.current.testServer, this.current.randomSpawns);
        }
        else
        {
          toReturn = "Properties saved";
        }
        if (this.saveNewGui)
        {
          logger.log(Level.INFO, "Saved using new method.");
          this.saveNewGui = false;
          if (this.changedId)
          {
            this.current.saveNewGui(this.oldId);
            this.current.movePlayersFromId(this.oldId);
            this.changedId = false;
            Servers.moveServerId(this.current, this.oldId);
            this.oldId = 0;
          }
          else
          {
            this.current.saveNewGui(this.current.id);
          }
        }
        if ((this.saveTwitter) && (!this.current.isCreating))
        {
          if (this.current.saveTwitter()) {
            logger.log(Level.INFO, "Saved twitter settings. The server will attempt to tweet.");
          } else {
            logger.log(Level.INFO, "Saved twitter settings. The server will not tweet.");
          }
          this.saveTwitter = false;
        }
        if (this.saveSpawns)
        {
          this.current.updateSpawns();
          logger.log(Level.INFO, "Saved new spawn points.");
          this.saveSpawns = false;
        }
        saveAtAll = false;
        this.changedProperties.clear();
        this.current.isCreating = false;
      }
    }
    return toReturn;
  }
  
  class ServerPropertySheetChangeListener<CustomPropertyItem>
    implements ListChangeListener
  {
    ServerPropertySheetChangeListener() {}
    
    public void onChanged(ListChangeListener.Change c)
    {
      System.out.println("Change occurred: " + c);
    }
  }
  
  public static final String generateRandomPassword()
  {
    Random rand = new Random();
    
    int length = rand.nextInt(3) + 6;
    char[] password = new char[length];
    for (int x = 0; x < length; x++)
    {
      int randDecimalAsciiVal = rand.nextInt("abcdefgijkmnopqrstwxyzABCDEFGHJKLMNPQRSTWXYZ23456789".length());
      password[x] = "abcdefgijkmnopqrstwxyzABCDEFGHJKLMNPQRSTWXYZ23456789".charAt(randDecimalAsciiVal);
    }
    return String.valueOf(password);
  }
  
  public static final short getNewServerId()
  {
    Random random = new Random();
    short newRand = 0;
    HashSet<Integer> usedNumbers = new HashSet();
    for (ServerEntry entry : Servers.getAllServers()) {
      usedNumbers.add(Integer.valueOf(entry.id));
    }
    int tries = 0;
    int max = 30000;
    usedNumbers.add(Integer.valueOf(0));
    while ((usedNumbers.contains(Integer.valueOf(newRand))) && (tries < max))
    {
      newRand = (short)random.nextInt(32767);
      if (!usedNumbers.contains(Integer.valueOf(newRand))) {
        break;
      }
      tries++;
    }
    return newRand;
  }
  
  public ServerPropertySheet(ServerEntry entry)
  {
    this.current = entry;
    this.list = FXCollections.observableArrayList();
    if (entry == null) {
      return;
    }
    if (entry.isLocal) {
      initializeLocalServer(entry);
    } else {
      initializeNonLocalServer(entry);
    }
  }
  
  private void initializeLocalServer(ServerEntry entry)
  {
    this.saveNewGui = false;
    this.saveSpawns = false;
    this.saveTwitter = false;
    this.list.add(new CustomPropertyItem(PropertyType.NAME, "1: Server Settings", "Server Name", "Name", true, entry.name));
    if (entry.id == 0)
    {
      this.changedId = true;
      this.oldId = 0;
      entry.id = getNewServerId();
      this.list.add(new CustomPropertyItem(PropertyType.SERVERID, "2: Advance Server Settings", "Server ID", "The unique ID in the cluster", true, 
        Integer.valueOf(entry.id)));
      this.changedProperties.add(PropertyType.SERVERID);
      this.saveNewGui = true;
    }
    else
    {
      this.list.add(new CustomPropertyItem(PropertyType.SERVERID, "2: Advance Server Settings", "Server ID", "The unique ID in the cluster", true, 
        Integer.valueOf(entry.id)));
    }
    this.list.add(new CustomPropertyItem(PropertyType.ENABLE_PNP_PORT_FORWARD, "1: Server Settings", "Auto port-forward", "Uses PNP to set up port-forwarding on your router", true, Boolean.valueOf(ServerProperties.getBoolean("ENABLE_PNP_PORT_FORWARD", Constants.enablePnpPortForward))));
    if ((entry.EXTERNALIP == null) || (entry.EXTERNALIP.equals(""))) {
      if (entry.isLocal) {
        try
        {
          entry.EXTERNALIP = InetAddress.getLocalHost().getHostAddress();
          this.changedProperties.add(PropertyType.EXTSERVERIP);
          this.saveNewGui = true;
        }
        catch (Exception ex)
        {
          logger.log(Level.INFO, ex.getMessage());
        }
      }
    }
    this.list.add(new CustomPropertyItem(PropertyType.EXTSERVERIP, "1: Server Settings", "Server External IP Address", "IP Address", true, entry.EXTERNALIP));
    
    this.list.add(new CustomPropertyItem(PropertyType.EXTSERVERPORT, "1: Server Settings", "Server External IP Port", "IP Port number", true, entry.EXTERNALPORT));
    if (((entry.INTRASERVERADDRESS == null) || (entry.INTRASERVERADDRESS.equals(""))) && (entry.isLocal)) {
      try
      {
        entry.INTRASERVERADDRESS = InetAddress.getLoopbackAddress().getHostAddress();
        this.changedProperties.add(PropertyType.INTIP);
        this.saveNewGui = true;
      }
      catch (Exception ex)
      {
        logger.log(Level.INFO, ex.getMessage());
      }
    }
    this.list.add(new CustomPropertyItem(PropertyType.INTIP, "2: Advance Server Settings", "Server Internal IP Address", "IP Address", true, entry.INTRASERVERADDRESS));
    
    this.list.add(new CustomPropertyItem(PropertyType.INTPORT, "2: Advance Server Settings", "Server Internal IP Port", "IP Port number", true, entry.INTRASERVERPORT));
    
    this.list.add(new CustomPropertyItem(PropertyType.RMI_REG_PORT, "2: Advance Server Settings", "RMI Registration Port", "IP Port number", true, 
      Integer.valueOf(entry.REGISTRATION_PORT)));
    this.list.add(new CustomPropertyItem(PropertyType.RMIPORT, "2: Advance Server Settings", "RMI Port", "IP Port number", true, 
      Integer.valueOf(entry.RMI_PORT)));
    this.list.add(new CustomPropertyItem(PropertyType.STEAMQUERYPORT, "2: Advance Server Settings", "Steam Query Port", "A port Steam uses for queries about connections. Standard is 27016", true, 
      Short.valueOf(ServerProperties.getShort("STEAMQUERYPORT", SteamHandler.steamQueryPort))));
    if (((entry.INTRASERVERPASSWORD == null) || (entry.INTRASERVERPASSWORD.equals(""))) && (entry.isLocal))
    {
      entry.INTRASERVERPASSWORD = generateRandomPassword();
      this.changedProperties.add(PropertyType.INTRASERVERPASSWORD);
      this.saveNewGui = true;
    }
    this.list.add(new CustomPropertyItem(PropertyType.INTRASERVERPASSWORD, "2: Advance Server Settings", "Intra server password", "Server Cross-Communication password. Used for connecting servers to eachother.", true, entry.INTRASERVERPASSWORD));
    
    this.list.add(new CustomPropertyItem(PropertyType.STEAMPW, "1: Server Settings", "Server password", "Server Password. If set, players need to provide this in order to connect to your server", true, entry
      .getSteamServerPassword()));
    this.list.add(new CustomPropertyItem(PropertyType.MAXPLAYERS, "1: Server Settings", "Maximum number of players", "Maximum number of players on this server", true, 
      Integer.valueOf(entry.pLimit)));
    this.list.add(new CustomPropertyItem(PropertyType.PVPSERVER, "1: Server Settings", "Allow PvP", "Allowing Player Versus Player combat and theft", true, 
      Boolean.valueOf(entry.PVPSERVER)));
    this.list.add(new CustomPropertyItem(PropertyType.EPIC, "1: Server Settings", "Epic settings", "Faster skillgain, missions affect Valrei", true, 
      Boolean.valueOf(entry.EPIC)));
    this.list.add(new CustomPropertyItem(PropertyType.HOMESERVER, "1: Server Settings", "Home Server", "A Home server can only have settlements from one kingdom", true, 
    
      Boolean.valueOf(entry.HOMESERVER)));
    this.list.add(new CustomPropertyItem(PropertyType.KINGDOM, "1: Server Settings", "Home Server Kingdom", "Which kingdom the Home server should have, 0 = No kingdom, 1 = Jenn-Kellon, 2 = Mol Rehan, 3 = Horde of the Summoned, 4 = Freedom Isles", true, 
      Byte.valueOf(entry.KINGDOM)));
    this.list.add(new CustomPropertyItem(PropertyType.LOGINSERVER, "2: Advance Server Settings", "Login Server", "The login server is the central cluster node responsible for bank accounts and cross communication", true, 
      Boolean.valueOf(entry.LOGINSERVER)));
    
    this.list.add(new CustomPropertyItem(PropertyType.TESTSERVER, "2: Advance Server Settings", "Test Server", "Some special settings and debug options", true, 
      Boolean.valueOf(entry.testServer)));
    this.list.add(new CustomPropertyItem(PropertyType.NPCS, "2: Advance Server Settings", "Npcs", "Whether npcs should be loaded", true, 
      Boolean.valueOf(ServerProperties.getBoolean("NPCS", Constants.loadNpcs))));
    this.list.add(new CustomPropertyItem(PropertyType.ENDGAMEITEMS, "2: Advance Server Settings", "End Game Items", "Whether artifacts and huge altars should be loaded", true, 
      Boolean.valueOf(ServerProperties.getBoolean("ENDGAMEITEMS", Constants.loadEndGameItems))));
    this.list.add(new CustomPropertyItem(PropertyType.SPY_PREVENTION, "2: Advance Server Settings", "PVP Spy Prevention", "Prevents multiple IPs from different kingdoms from logging in at the same time.", true, 
      Boolean.valueOf(ServerProperties.getBoolean("SPYPREVENTION", Constants.enableSpyPrevention))));
    this.list.add(new CustomPropertyItem(PropertyType.NEWBIE_FRIENDLY, "2: Advance Server Settings", "Newbie Friendly", "Prevents harder creatures from spawning.", true, 
      Boolean.valueOf(ServerProperties.getBoolean("NEWBIEFRIENDLY", Constants.isNewbieFriendly))));
    
    this.list.add(new CustomPropertyItem(PropertyType.RANDOMSPAWNS, "2: Advance Server Settings", "Random spawn points", "Enable random spawn points for new players", true, 
      Boolean.valueOf(entry.randomSpawns)));
    this.list.add(new CustomPropertyItem(PropertyType.SPAWNPOINTJENNX, "2: Advance Server Settings", "Spawnpoint x", "Where players generally spawn, tile x", true, 
      Integer.valueOf(entry.SPAWNPOINTJENNX)));
    this.list.add(new CustomPropertyItem(PropertyType.SPAWNPOINTJENNY, "2: Advance Server Settings", "Spawnpoint y", "Where players generally spawn, tile y", true, 
      Integer.valueOf(entry.SPAWNPOINTJENNY)));
    this.list.add(new CustomPropertyItem(PropertyType.SPAWNPOINTMOLX, "2: Advance Server Settings", "Kingdom 2 Spawnpoint x", "Where kingdom 2 players spawn, tile x", true, 
      Integer.valueOf(entry.SPAWNPOINTMOLX)));
    this.list.add(new CustomPropertyItem(PropertyType.SPAWNPOINTMOLY, "2: Advance Server Settings", "Kingdom 2 Spawnpoint y", "Where kingdom 2 players spawn, tile y", true, 
      Integer.valueOf(entry.SPAWNPOINTMOLY)));
    this.list.add(new CustomPropertyItem(PropertyType.SPAWNPOINTLIBX, "2: Advance Server Settings", "Kingdom 3 Spawnpoint x", "Where kingdom 3 players spawn, tile x", true, 
      Integer.valueOf(entry.SPAWNPOINTLIBX)));
    this.list.add(new CustomPropertyItem(PropertyType.SPAWNPOINTLIBY, "2: Advance Server Settings", "Kingdom 3 Spawnpoint y", "Where kingdom 3 players spawn, tile y", true, 
      Integer.valueOf(entry.SPAWNPOINTLIBY)));
    this.list.add(new CustomPropertyItem(PropertyType.MOTD, "1: Server Settings", "Message of the day", "A message to display upon login", true, entry
      .getMotd()));
    
    this.list.add(new CustomPropertyItem(PropertyType.TWITTERCONSUMERKEY, "4: Twitter Settings", "Consumer key", "Consumer key", true, entry
      .getConsumerKey()));
    this.list.add(new CustomPropertyItem(PropertyType.TWITTERCONSUMERSECRET, "4: Twitter Settings", "Consumer secret", "Consumer secret", true, entry
      .getConsumerSecret()));
    this.list.add(new CustomPropertyItem(PropertyType.TWITTERAPPTOKEN, "4: Twitter Settings", "Application token", "Application token", true, entry
      .getApplicationToken()));
    this.list.add(new CustomPropertyItem(PropertyType.TWITTERAPPSECRET, "4: Twitter Settings", "Application secret", "Application secret", true, entry
      .getApplicationSecret()));
    
    this.list.add(new CustomPropertyItem(PropertyType.SKILLGAINRATE, "3: Gameplay Tweaks", "Skill gain rate multiplier", "Multiplies the server skill gain rate. Higher means faster skill gain. It's not exact depending on a number of factors.", true, 
    
      Float.valueOf(this.current.getSkillGainRate()), Float.valueOf(0.01F)));
    this.list.add(new CustomPropertyItem(PropertyType.SKBASIC, "3: Gameplay Tweaks", "Characteristics start value", "Start value of Characteristics such as body strength, stamina and soul depth", true, 
      Float.valueOf(this.current
      .getSkillbasicval()), Float.valueOf(1.0F), Float.valueOf(100.0F)));
    this.list.add(new CustomPropertyItem(PropertyType.SKMIND, "3: Gameplay Tweaks", "Mind Logic skill start value", "Start value of Mind Logic Characteristic (used for controlling vehicles)", true, 
      Float.valueOf(this.current
      .getSkillmindval()), Float.valueOf(1.0F), Float.valueOf(100.0F)));
    this.list.add(new CustomPropertyItem(PropertyType.SKBC, "3: Gameplay Tweaks", "Body Control skill start value", "Start value of Mind Logic Characteristic (used for controlling mounts)", true, 
      Float.valueOf(this.current
      .getSkillbcval()), Float.valueOf(1.0F), Float.valueOf(100.0F)));
    this.list.add(new CustomPropertyItem(PropertyType.SKFIGHT, "3: Gameplay Tweaks", "Fight skill start value", "Affects start value of the overall fighting skill", true, 
      Float.valueOf(this.current.getSkillfightval()), Float.valueOf(1.0F), Float.valueOf(100.0F)));
    this.list.add(new CustomPropertyItem(PropertyType.SKOVERALL, "3: Gameplay Tweaks", "Overall skill start value", "Start value of all other skills", true, 
      Float.valueOf(this.current.getSkilloverallval()), Float.valueOf(1.0F), Float.valueOf(100.0F)));
    this.list.add(new CustomPropertyItem(PropertyType.CRMOD, "3: Gameplay Tweaks", "Player combat rating modifier", "Modifies player combat power versus creatures", true, 
      Float.valueOf(this.current.getCombatRatingModifier())));
    this.list.add(new CustomPropertyItem(PropertyType.ACTIONTIMER, "3: Gameplay Tweaks", "Action speed multiplier", "Divides the max standard time an action takes. Higher makes actions faster.", true, 
      Float.valueOf(this.current.getActionTimer()), Float.valueOf(0.01F)));
    this.list.add(new CustomPropertyItem(PropertyType.HOTADELAY, "3: Gameplay Tweaks", "Hota Delay", "The time in minutes between Hunt Of The Ancients rounds", true, 
      Integer.valueOf(this.current.getHotaDelay())));
    this.list.add(new CustomPropertyItem(PropertyType.MAXCREATURES, "3: Gameplay Tweaks", "Max Creatures", "Max creatures", true, 
      Integer.valueOf(this.current.maxCreatures)));
    this.list.add(new CustomPropertyItem(PropertyType.PERCENTAGG, "3: Gameplay Tweaks", "Aggressive Creatures, %", "Approximate max number of aggressive creatures in per cent of all creatures", true, 
    
      Float.valueOf(this.current.percentAggCreatures), Float.valueOf(0.0F)));
    this.list.add(new CustomPropertyItem(PropertyType.UPKEEP, "3: Gameplay Tweaks", "Settlement upkeep enabled", "If settlements require upkeep money", true, 
      Boolean.valueOf(this.current.isUpkeep())));
    
    this.list.add(new CustomPropertyItem(PropertyType.FREEDEEDS, "3: Gameplay Tweaks", "No deeding costs", "If deeding is free and costs no money", true, 
      Boolean.valueOf(this.current.isFreeDeeds())));
    
    this.list.add(new CustomPropertyItem(PropertyType.TRADERMAX, "3: Gameplay Tweaks", "Trader max money in silver", "The max amount of money a trader will receive from the pool", true, 
      Integer.valueOf(this.current.getTraderMaxIrons() / 10000)));
    
    this.list.add(new CustomPropertyItem(PropertyType.TRADERINIT, "3: Gameplay Tweaks", "Trader initial money in silver", "The initial amount of money a trader will receive from the pool", true, 
      Integer.valueOf(this.current.getInitialTraderIrons() / 10000)));
    this.list.add(new CustomPropertyItem(PropertyType.TUNNELING, "3: Gameplay Tweaks", "Minimum mining hits required", "The minimum number of times you need to mine before a wall disappears", true, 
      Integer.valueOf(this.current.getTunnelingHits())));
    this.list.add(new CustomPropertyItem(PropertyType.BREEDING, "3: Gameplay Tweaks", "Breeding time modifier", "A modifier which makes breeding faster the higher it is.", true, 
      Long.valueOf(this.current.getBreedingTimer())));
    this.list.add(new CustomPropertyItem(PropertyType.FIELDGROWTH, "3: Gameplay Tweaks", "Field growth timer, hour", "The number hours between field growth checks", true, 
      Float.valueOf((float)this.current.getFieldGrowthTime() / 1000.0F / 3600.0F), Float.valueOf(0.01F)));
    this.list.add(new CustomPropertyItem(PropertyType.TREEGROWTH, "3: Gameplay Tweaks", "Tree spread odds", "Odds of new trees or mushrooms appearing, as determined by a 1 in <Tree Growth> chance. When set to 0, tree growth is prevented.", true, 
      Integer.valueOf(this.current.treeGrowth), Integer.valueOf(0)));
    
    this.list.add(new CustomPropertyItem(PropertyType.KINGSMONEY, "3: Gameplay Tweaks", "Money pool in silver", "This is the amount of money that will be in the money pool after server restart.", true, 
      Integer.valueOf(this.current.getKingsmoneyAtRestart() / 10000)));
    
    this.list.add(new CustomPropertyItem(PropertyType.MAINTAINING, "5: Maintenance", "Maintenance", "Start in maintenance mode", true, 
      Boolean.valueOf(false)));
    
    this.list.add(new CustomPropertyItem(PropertyType.ADMIN_PWD, "1: Server Settings", "Admin Password", "Password used to unlock the ability to change game tweaks from within the game.", true, 
      ServerProperties.getString("ADMINPASSWORD", "")));
    if (this.saveNewGui)
    {
      this.saveNewGui = false;
      if (entry.isCreating)
      {
        save();
      }
      else if (this.changedId)
      {
        this.current.saveNewGui(this.oldId);
        this.current.movePlayersFromId(this.oldId);
        this.changedId = false;
        Servers.moveServerId(this.current, this.oldId);
        this.oldId = 0;
      }
      else
      {
        entry.saveNewGui(entry.id);
      }
      this.changedProperties.clear();
      System.out.println("Saved new server");
    }
    final SimpleObjectProperty<Callback<PropertySheet.Item, PropertyEditor<?>>> propertyEditorFactory = new SimpleObjectProperty(this, "propertyEditor", new DefaultPropertyEditorFactory());
    this.propertySheet = new PropertySheet(this.list);
    this.propertySheet.setPropertyEditorFactory(new Callback()
    {
      public PropertyEditor<?> call(PropertySheet.Item param)
      {
        if ((param instanceof ServerPropertySheet.CustomPropertyItem))
        {
          ServerPropertySheet.CustomPropertyItem pi = (ServerPropertySheet.CustomPropertyItem)param;
          if ((pi.type == ServerPropertySheet.PropertyType.ACTIONTIMER) || 
            (pi.getValue().getClass() == Float.class)) {
            return new FormattedFloatEditor(param);
          }
        }
        if (propertyEditorFactory.get() == null) {
          throw new NullPointerException("Null!");
        }
        return (PropertyEditor)((Callback)propertyEditorFactory.get()).call(param);
      }
    });
    this.propertySheet.setMode(PropertySheet.Mode.CATEGORY);
    VBox.setVgrow(this.propertySheet, Priority.ALWAYS);
    getChildren().add(this.propertySheet);
  }
  
  private void initializeNonLocalServer(ServerEntry entry)
  {
    this.saveNewGui = false;
    this.saveSpawns = false;
    this.saveTwitter = false;
    this.list.add(new CustomPropertyItem(PropertyType.NAME, "1: Server Settings", "Server Name", "Name", true, entry.name));
    if (entry.id == 0)
    {
      this.changedId = true;
      this.oldId = 0;
      entry.id = getNewServerId();
      this.list.add(new CustomPropertyItem(PropertyType.SERVERID, "1: Server Settings", "Server ID", "The unique ID in the cluster", true, 
        Integer.valueOf(entry.id)));
      this.changedProperties.add(PropertyType.SERVERID);
      this.saveNewGui = true;
    }
    else
    {
      this.list.add(new CustomPropertyItem(PropertyType.SERVERID, "1: Server Settings", "Server ID", "The unique ID in the cluster", true, 
        Integer.valueOf(entry.id)));
    }
    this.list.add(new CustomPropertyItem(PropertyType.ENABLE_PNP_PORT_FORWARD, "1: Server Settings", "Auto port-forward", "Uses PNP to set up port-forwarding on your router", true, Boolean.valueOf(ServerProperties.getBoolean("ENABLE_PNP_PORT_FORWARD", Constants.enablePnpPortForward))));
    if ((entry.EXTERNALIP == null) || (entry.EXTERNALIP.equals(""))) {
      if (entry.isLocal)
      {
        try
        {
          entry.EXTERNALIP = InetAddress.getLocalHost().getHostAddress();
          this.changedProperties.add(PropertyType.EXTSERVERIP);
          this.saveNewGui = true;
        }
        catch (Exception ex)
        {
          logger.log(Level.INFO, ex.getMessage());
        }
      }
      else
      {
        entry.EXTERNALIP = ((InetAddress)Objects.requireNonNull(AddressHelper.getFirstValidAddress())).getHostAddress();
        this.changedProperties.add(PropertyType.EXTSERVERIP);
        this.saveNewGui = true;
      }
    }
    this.list.add(new CustomPropertyItem(PropertyType.EXTSERVERIP, "1: Server Settings", "Server External IP Address", "IP Address", true, entry.EXTERNALIP));
    
    this.list.add(new CustomPropertyItem(PropertyType.EXTSERVERPORT, "1: Server Settings", "Server External IP Port", "IP Port number", true, entry.EXTERNALPORT));
    if (((entry.INTRASERVERADDRESS == null) || (entry.INTRASERVERADDRESS.equals(""))) && (entry.isLocal)) {
      try
      {
        entry.INTRASERVERADDRESS = InetAddress.getLocalHost().getHostAddress();
        this.changedProperties.add(PropertyType.INTIP);
        this.saveNewGui = true;
      }
      catch (Exception ex)
      {
        logger.log(Level.INFO, ex.getMessage());
      }
    }
    this.list.add(new CustomPropertyItem(PropertyType.INTIP, "1: Server Settings", "Server Internal IP Address", "IP Address", true, entry.INTRASERVERADDRESS));
    
    this.list.add(new CustomPropertyItem(PropertyType.INTPORT, "1: Server Settings", "Server Internal IP Port", "IP Port number", true, entry.INTRASERVERPORT));
    
    this.list.add(new CustomPropertyItem(PropertyType.RMI_REG_PORT, "1: Server Settings", "RMI Registration Port", "IP Port number", true, 
      Integer.valueOf(entry.REGISTRATION_PORT)));
    this.list.add(new CustomPropertyItem(PropertyType.RMIPORT, "1: Server Settings", "RMI Port", "IP Port number", true, 
      Integer.valueOf(entry.RMI_PORT)));
    if (((entry.INTRASERVERPASSWORD == null) || (entry.INTRASERVERPASSWORD.equals(""))) && (entry.isLocal))
    {
      entry.INTRASERVERPASSWORD = generateRandomPassword();
      this.changedProperties.add(PropertyType.INTRASERVERPASSWORD);
      this.saveNewGui = true;
    }
    this.list.add(new CustomPropertyItem(PropertyType.INTRASERVERPASSWORD, "1: Server Settings", "Intra server password", "Server Cross-Communication password. Used for connecting servers to eachother.", true, entry.INTRASERVERPASSWORD));
    
    this.list.add(new CustomPropertyItem(PropertyType.LOGINSERVER, "1: Server Settings", "Login Server", "The login server is the central cluster node responsible for bank accounts and cross communication", true, 
      Boolean.valueOf(entry.LOGINSERVER)));
    this.list.add(new CustomPropertyItem(PropertyType.PVPSERVER, "1: Server Settings", "Allows PvP", "Allowing Player Versus Player combat and theft", true, 
      Boolean.valueOf(entry.PVPSERVER)));
    this.list.add(new CustomPropertyItem(PropertyType.HOMESERVER, "1: Server Settings", "Home Server", "A Home server can only have settlements from one kingdom", true, 
    
      Boolean.valueOf(entry.HOMESERVER)));
    this.list.add(new CustomPropertyItem(PropertyType.KINGDOM, "1: Server Settings", "Home Server Kingdom", "Which kingdom the Home server should have, 0 = No kingdom, 1 = Jenn-Kellon, 2 = Mol Rehan, 3 = Horde of the Summoned, 4 = Freedom Isles", true, 
      Byte.valueOf(entry.KINGDOM)));
    if (this.saveNewGui)
    {
      this.saveNewGui = false;
      if (entry.isCreating)
      {
        save();
      }
      else if (this.changedId)
      {
        this.current.saveNewGui(this.oldId);
        this.current.movePlayersFromId(this.oldId);
        this.changedId = false;
        Servers.moveServerId(this.current, this.oldId);
        this.oldId = 0;
      }
      else
      {
        entry.saveNewGui(entry.id);
      }
      this.changedProperties.clear();
      System.out.println("Saved new server");
    }
    this.propertySheet = new PropertySheet(this.list);
    this.propertySheet.setMode(PropertySheet.Mode.CATEGORY);
    VBox.setVgrow(this.propertySheet, Priority.ALWAYS);
    getChildren().add(this.propertySheet);
  }
  
  public void setReadOnly()
  {
    if (this.propertySheet != null)
    {
      this.propertySheet.setMode(PropertySheet.Mode.NAME);
      this.propertySheet.setDisable(true);
    }
  }
  
  public boolean haveChanges()
  {
    if (this.changedProperties.isEmpty()) {
      return false;
    }
    return true;
  }
  
  public void AskIfSave()
  {
    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
    alert.setTitle("Unsaved changes!");
    alert.setHeaderText("There are unsaved changes in the local server tab");
    alert.setContentText("Do you want to save the changes?");
    
    Optional<ButtonType> result = alert.showAndWait();
    if (result.get() == ButtonType.OK) {
      save();
    }
  }
  
  public final ServerEntry getCurrentServerEntry()
  {
    return this.current;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\gui\propertysheet\ServerPropertySheet.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */