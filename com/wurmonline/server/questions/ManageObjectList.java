package com.wurmonline.server.questions;

import com.wurmonline.server.Items;
import com.wurmonline.server.LoginHandler;
import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.Servers;
import com.wurmonline.server.WurmId;
import com.wurmonline.server.behaviours.MethodsCreatures;
import com.wurmonline.server.behaviours.Vehicle;
import com.wurmonline.server.behaviours.Vehicles;
import com.wurmonline.server.creatures.AnimalSettings.Animal0Permissions;
import com.wurmonline.server.creatures.AnimalSettings.Animal1Permissions;
import com.wurmonline.server.creatures.AnimalSettings.Animal2Permissions;
import com.wurmonline.server.creatures.AnimalSettings.WagonerPermissions;
import com.wurmonline.server.creatures.Brand;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.CreatureStatus;
import com.wurmonline.server.creatures.Creatures;
import com.wurmonline.server.creatures.DbCreatureStatus;
import com.wurmonline.server.creatures.Delivery;
import com.wurmonline.server.creatures.MineDoorPermission;
import com.wurmonline.server.creatures.MineDoorSettings.MinedoorPermissions;
import com.wurmonline.server.creatures.NoSuchCreatureException;
import com.wurmonline.server.creatures.VisionArea;
import com.wurmonline.server.creatures.Wagoner;
import com.wurmonline.server.endgames.EndGameItems;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemSettings.BedPermissions;
import com.wurmonline.server.items.ItemSettings.CorpsePermissions;
import com.wurmonline.server.items.ItemSettings.CreatureTransporterPermissions;
import com.wurmonline.server.items.ItemSettings.ItemPermissions;
import com.wurmonline.server.items.ItemSettings.MessageBoardPermissions;
import com.wurmonline.server.items.ItemSettings.ShipTransporterPermissions;
import com.wurmonline.server.items.ItemSettings.SmallCartPermissions;
import com.wurmonline.server.items.ItemSettings.VehiclePermissions;
import com.wurmonline.server.items.ItemSettings.WagonPermissions;
import com.wurmonline.server.players.Permissions.IPermission;
import com.wurmonline.server.players.PermissionsPlayerList.ISettings;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.players.PlayerInfoFactory;
import com.wurmonline.server.structures.Door;
import com.wurmonline.server.structures.DoorSettings.DoorPermissions;
import com.wurmonline.server.structures.DoorSettings.GatePermissions;
import com.wurmonline.server.structures.FenceGate;
import com.wurmonline.server.structures.NoSuchStructureException;
import com.wurmonline.server.structures.Structure;
import com.wurmonline.server.structures.StructureSettings.StructurePermissions;
import com.wurmonline.server.structures.Structures;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.villages.VillageRole;
import com.wurmonline.server.zones.Zones;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ManageObjectList
  extends Question
{
  public static enum Type
  {
    ANIMAL0("Animal", AnimalSettings.Animal0Permissions.values()),  ANIMAL1("Animal", AnimalSettings.Animal1Permissions.values()),  ANIMAL2("Animal", AnimalSettings.Animal2Permissions.values()),  WAGONER("Wagoner", AnimalSettings.WagonerPermissions.values()),  DELIVERY("Wagoner", AnimalSettings.WagonerPermissions.values()),  BUILDING("Building", StructureSettings.StructurePermissions.values()),  LARGE_CART("Large Cart", ItemSettings.VehiclePermissions.values()),  DOOR("Door", DoorSettings.DoorPermissions.values()),  GATE("Gate", DoorSettings.GatePermissions.values()),  MINEDOOR("Minedoor", MineDoorSettings.MinedoorPermissions.values()),  SHIP("Ship", ItemSettings.VehiclePermissions.values()),  WAGON("Wagon", ItemSettings.WagonPermissions.values()),  SHIP_CARRIER("Ship Carrier", ItemSettings.ShipTransporterPermissions.values()),  CREATURE_CARRIER("Creature Carrier", ItemSettings.CreatureTransporterPermissions.values()),  SMALL_CART("Small Cart", ItemSettings.SmallCartPermissions.values()),  ITEM("Item", ItemSettings.ItemPermissions.values()),  BED("Bed", ItemSettings.BedPermissions.values()),  MESSAGE_BOARD("Village Message Board", ItemSettings.MessageBoardPermissions.values()),  CORPSE("Corpse", ItemSettings.CorpsePermissions.values()),  SEARCH("Search", null),  REPLY("Reply", null);
    
    private final String title;
    private final Permissions.IPermission[] enumValues;
    
    private Type(String aTitle, Permissions.IPermission[] values)
    {
      this.title = aTitle;
      this.enumValues = values;
    }
    
    public String getTitle()
    {
      return this.title;
    }
    
    public Permissions.IPermission[] getEnumValues()
    {
      return this.enumValues;
    }
  }
  
  private static final Logger logger = Logger.getLogger(ManageObjectList.class.getName());
  private static final String red = "color=\"255,127,127\"";
  private static final String green = "color=\"127,255,127\"";
  private static final String orange = "color=\"255,177,40\"";
  private final Player player;
  private final Type objectType;
  private final boolean fromList;
  private final int sortBy;
  private String searchName;
  private final boolean includeAll;
  private PermissionsPlayerList.ISettings[] objects = null;
  private boolean showingQueue = false;
  private boolean inQueue = true;
  private boolean waitAccept = false;
  private boolean inProgress = false;
  private boolean delivered = false;
  private boolean rejected = false;
  private boolean cancelled = false;
  
  public ManageObjectList(Creature aResponder, Type aType)
  {
    this(aResponder, aType, -10L, false, 1, "", true);
  }
  
  public ManageObjectList(Creature aResponder, Type aType, long parent, boolean wasFromList, int aSortBy, String searchFor, boolean aIncludeAll)
  {
    super(aResponder, getTitle(aResponder, aType, parent, wasFromList, searchFor), 
      getQuestion(aResponder, aType, parent, wasFromList, searchFor), 121, parent);
    this.player = ((Player)getResponder());
    this.fromList = wasFromList;
    this.objectType = aType;
    this.sortBy = aSortBy;
    this.searchName = searchFor;
    this.includeAll = aIncludeAll;
  }
  
  public ManageObjectList(Creature aResponder, Type aType, long parent, boolean wasFromList, int aSortBy, String searchFor, boolean aIncludeAll, boolean inqueue, boolean waitaccept, boolean inprogress, boolean delivered, boolean rejected, boolean cancelled)
  {
    super(aResponder, getTitle(aResponder, aType, parent, wasFromList, searchFor), 
      getQuestion(aResponder, aType, parent, wasFromList, searchFor), 121, parent);
    this.player = ((Player)getResponder());
    this.fromList = wasFromList;
    this.objectType = aType;
    this.sortBy = aSortBy;
    this.searchName = searchFor;
    this.includeAll = aIncludeAll;
    this.showingQueue = (aType == Type.WAGONER);
    this.inQueue = inqueue;
    this.waitAccept = waitaccept;
    this.inProgress = inprogress;
    this.delivered = delivered;
    this.rejected = rejected;
    this.cancelled = cancelled;
  }
  
  private static String getTitle(Creature aResponder, Type aType, long parent, boolean wasFromList, String lookingFor)
  {
    if (aType == Type.DOOR) {
      try
      {
        Structure structure = Structures.getStructure(parent);
        return structure.getName() + "'s List of doors";
      }
      catch (NoSuchStructureException e)
      {
        return aResponder.getName() + "'s List of " + aType.getTitle() + "s";
      }
    }
    if (aType == Type.SEARCH) {
      return "Player Search";
    }
    if (aType == Type.REPLY) {
      return "Search Result for " + lookingFor;
    }
    if ((aType == Type.SMALL_CART) || (aType == Type.LARGE_CART) || (aType == Type.WAGON) || (aType == Type.SHIP_CARRIER) || (aType == Type.CREATURE_CARRIER)) {
      return aResponder.getName() + "'s List of Small Carts, Large Carts, Wagons and Carriers";
    }
    if ((aType == Type.WAGONER) && (wasFromList))
    {
      Wagoner wagoner = Wagoner.getWagoner(parent);
      return "Wagoners " + wagoner.getName() + "'s Queue";
    }
    return aResponder.getName() + "'s List of " + aType.getTitle() + "s";
  }
  
  private static String getQuestion(Creature aResponder, Type aType, long parent, boolean wasFromList, String lookingFor)
  {
    if (aType == Type.DOOR) {
      try
      {
        Structure structure = Structures.getStructure(parent);
        return "Manage List of Doors for " + structure.getName();
      }
      catch (NoSuchStructureException e)
      {
        return "Manage Your List of " + aType.getTitle() + "s";
      }
    }
    if (aType == Type.SEARCH) {
      return "Player Search";
    }
    if (aType == Type.REPLY) {
      return "Search Result for " + lookingFor;
    }
    if ((aType == Type.SMALL_CART) || (aType == Type.LARGE_CART) || (aType == Type.WAGON) || (aType == Type.SHIP_CARRIER) || (aType == Type.CREATURE_CARRIER)) {
      return "Manage Your List of Small Carts, Large Carts, Wagons and Carriers";
    }
    if ((aType == Type.WAGONER) && (wasFromList))
    {
      Wagoner wagoner = Wagoner.getWagoner(parent);
      return "Viewing " + wagoner.getName() + "'s Queue";
    }
    return "Manage Your List of " + aType.getTitle() + "s";
  }
  
  public void answer(Properties aAnswer)
  {
    setAnswer(aAnswer);
    
    boolean managePermissions = getBooleanProp("permissions");
    boolean manageDoors = getBooleanProp("doors");
    boolean back = getBooleanProp("back");
    boolean close = getBooleanProp("close");
    boolean search = getBooleanProp("search");
    boolean remall = getBooleanProp("remall");
    boolean findAnimal = getBooleanProp("find");
    boolean inc = getBooleanProp("inc");
    boolean exc = getBooleanProp("exc");
    boolean queue = getBooleanProp("queue");
    boolean viewDelivery = getBooleanProp("delivery");
    if (close) {
      return;
    }
    if (inc)
    {
      ManageObjectList mol = new ManageObjectList(this.player, this.objectType, this.target, this.fromList, this.sortBy, this.searchName, true);
      mol.sendQuestion();
      return;
    }
    if (exc)
    {
      ManageObjectList mol = new ManageObjectList(this.player, this.objectType, this.target, this.fromList, this.sortBy, this.searchName, false);
      mol.sendQuestion();
      return;
    }
    if (back)
    {
      if (this.objectType == Type.DOOR)
      {
        ManageObjectList mol = new ManageObjectList(this.player, Type.BUILDING, this.target, false, 1, "", this.includeAll);
        mol.sendQuestion();
        return;
      }
      if (this.objectType == Type.WAGONER)
      {
        ManageObjectList mol = new ManageObjectList(this.player, Type.WAGONER, this.target, false, 1, "", this.includeAll);
        mol.sendQuestion();
        return;
      }
      if (this.objectType == Type.DELIVERY)
      {
        ManageObjectList mol = new ManageObjectList(this.player, Type.WAGONER, this.target, false, 1, "", this.includeAll);
        mol.sendQuestion2();
        return;
      }
      if (this.objectType == Type.REPLY)
      {
        ManageObjectList mol = new ManageObjectList(this.player, Type.SEARCH);
        mol.sendQuestion(); return;
      }
    }
    String who;
    String lookingFor;
    long lookId;
    if (search)
    {
      who = aAnswer.getProperty("who");
      lookingFor = LoginHandler.raiseFirstLetter(who);
      lookId = PlayerInfoFactory.getWurmId(lookingFor);
      
      ManageObjectList mol = new ManageObjectList(this.player, Type.REPLY, lookId, true, 1, lookingFor, this.includeAll);
      mol.sendQuestion();
      return;
    }
    if (remall)
    {
      who = this.objects;lookingFor = who.length;
      for (lookId = 0; lookId < lookingFor; lookId++)
      {
        PermissionsPlayerList.ISettings is = who[lookId];
        if (!is.isActualOwner(this.target))
        {
          is.removeGuest(this.target);
          this.player.getCommunicator().sendNormalServerMessage("You removed " + this.searchName + " from " + is
            .getTypeName() + " (" + is.getObjectName() + ")");
        }
      }
    }
    String sel;
    if ((managePermissions) || (manageDoors) || (findAnimal) || (queue) || (viewDelivery))
    {
      sel = aAnswer.getProperty("sel");
      long selId = Long.parseLong(sel);
      if (selId == -10L)
      {
        this.player.getCommunicator().sendNormalServerMessage("You decide to do nothing.");
        return;
      }
      if (managePermissions)
      {
        int ct = WurmId.getType(selId);
        ManagePermissions mp;
        if (ct == 1)
        {
          try
          {
            Creature creature = Creatures.getInstance().getCreature(selId);
            if (creature.isWagoner())
            {
              ManagePermissions mp = new ManagePermissions(this.player, Type.WAGONER, creature, true, this.target, false, this.objectType, "");
              
              mp.sendQuestion();
            }
            else
            {
              Vehicle vehicle = Vehicles.getVehicle(creature);
              if (vehicle == null)
              {
                ManagePermissions mp = new ManagePermissions(this.player, Type.ANIMAL0, creature, true, this.target, false, this.objectType, "");
                
                mp.sendQuestion();
              }
              else if (vehicle.isUnmountable())
              {
                ManagePermissions mp = new ManagePermissions(this.player, Type.ANIMAL0, creature, true, this.target, false, this.objectType, "");
                
                mp.sendQuestion();
              }
              else if (vehicle.getMaxPassengers() == 0)
              {
                ManagePermissions mp = new ManagePermissions(this.player, Type.ANIMAL1, creature, true, this.target, false, this.objectType, "");
                
                mp.sendQuestion();
              }
              else
              {
                mp = new ManagePermissions(this.player, Type.ANIMAL2, creature, true, this.target, false, this.objectType, "");
                
                mp.sendQuestion();
              }
            }
          }
          catch (NoSuchCreatureException nsce)
          {
            this.player.getCommunicator().sendNormalServerMessage("Cannot find animal, it was here a minute ago!");
            logger.log(Level.WARNING, "Cannot find animal, it was here a minute ago! Id:" + selId, nsce);
          }
        }
        else
        {
          ManagePermissions mp;
          if (ct == 4)
          {
            try
            {
              PermissionsPlayerList.ISettings theItem = Structures.getStructure(selId);
              mp = new ManagePermissions(this.player, Type.BUILDING, theItem, true, this.target, false, this.objectType, "");
              
              mp.sendQuestion();
            }
            catch (NoSuchStructureException nsse)
            {
              this.player.getCommunicator().sendNormalServerMessage("Cannot find structure, it was here a minute ago!");
              logger.log(Level.WARNING, "Cannot find structure, it was here a minute ago! Id:" + selId, nsse);
            }
          }
          else
          {
            if (ct == 5) {
              try
              {
                Structure structure = Structures.getStructure(this.target);
                mp = structure.getAllDoors();mp = mp.length;
                for (ManagePermissions localManagePermissions1 = 0; localManagePermissions1 < mp; localManagePermissions1++)
                {
                  Door door = mp[localManagePermissions1];
                  if (door.getWurmId() == selId)
                  {
                    ManagePermissions mp = new ManagePermissions(this.player, Type.DOOR, door, true, this.target, this.fromList, this.objectType, "");
                    
                    mp.sendQuestion();
                    return;
                  }
                }
                this.player.getCommunicator().sendNormalServerMessage("Cannot find door, it was here a minute ago!");
                logger.log(Level.WARNING, "Cannot find door, it was here a minute ago! Id:" + selId);
              }
              catch (NoSuchStructureException nsse)
              {
                this.player.getCommunicator().sendNormalServerMessage("Cannot find structure, it was here a minute ago!");
                logger.log(Level.WARNING, "Cannot find structure, it was here a minute ago! Id:" + selId, nsse);
              }
            }
            if (ct == 7)
            {
              FenceGate gate = FenceGate.getFenceGate(selId);
              if (gate != null)
              {
                ManagePermissions mp = new ManagePermissions(this.player, Type.GATE, gate, true, this.target, this.fromList, this.objectType, "");
                
                mp.sendQuestion();
              }
              else
              {
                this.player.getCommunicator().sendNormalServerMessage("Cannot find gate, it was here a minute ago!");
              }
            }
            else if (ct == 2)
            {
              try
              {
                Item item = Items.getItem(selId);
                
                Type itemType = Type.SHIP;
                if (item.getTemplateId() == 186) {
                  itemType = Type.SMALL_CART;
                } else if (item.getTemplateId() == 539) {
                  itemType = Type.LARGE_CART;
                } else if (item.getTemplateId() == 850) {
                  itemType = Type.WAGON;
                } else if (item.getTemplateId() == 853) {
                  itemType = Type.SHIP_CARRIER;
                } else if (item.getTemplateId() == 1410) {
                  itemType = Type.CREATURE_CARRIER;
                }
                ManagePermissions mp = new ManagePermissions(this.player, itemType, item, true, this.target, this.fromList, this.objectType, "");
                
                mp.sendQuestion();
              }
              catch (NoSuchItemException e)
              {
                this.player.getCommunicator().sendNormalServerMessage("Cannot find vehicle, it was here a minute ago!");
              }
            }
            else if (ct == 3)
            {
              MineDoorPermission mineDoor = MineDoorPermission.getPermission(selId);
              if (mineDoor != null)
              {
                ManagePermissions mp = new ManagePermissions(this.player, Type.MINEDOOR, mineDoor, true, this.target, this.fromList, this.objectType, "");
                
                mp.sendQuestion();
              }
              else
              {
                this.player.getCommunicator().sendNormalServerMessage("Cannot find minedoor, it was here a minute ago!");
              }
            }
            else
            {
              this.player.getCommunicator().sendNormalServerMessage("Unknown object!");
            }
          }
        }
        return;
      }
      if (manageDoors)
      {
        ManageObjectList mol = new ManageObjectList(this.player, Type.DOOR, selId, true, 1, "", this.includeAll);
        mol.sendQuestion();
      }
      else if ((findAnimal) && (!Servers.isThisAPvpServer()))
      {
        try
        {
          Creature creature = Creatures.getInstance().getCreature(selId);
          int centerx = creature.getTileX();
          int centery = creature.getTileY();
          int dx = Math.abs(centerx - this.player.getTileX());
          int dy = Math.abs(centery - this.player.getTileY());
          int mindist = (int)Math.sqrt(dx * dx + dy * dy);
          int dir = MethodsCreatures.getDir(this.player, centerx, centery);
          if (DbCreatureStatus.getIsLoaded(creature.getWurmId()) == 0)
          {
            String direction = MethodsCreatures.getLocationStringFor(this.player.getStatus().getRotation(), dir, "you");
            
            String toReturn = EndGameItems.getDistanceString(mindist, creature.getName(), direction, false);
            this.player.getCommunicator().sendNormalServerMessage(toReturn);
          }
          else
          {
            this.player.getCommunicator().sendNormalServerMessage("This creature is loaded in a cage, or on another server.");
          }
        }
        catch (NoSuchCreatureException nsce)
        {
          this.player.getCommunicator().sendNormalServerMessage("Cannot find animal, it was here a minute ago!");
          logger.log(Level.WARNING, "Cannot find animal, it was here a minute ago! Id:" + selId, nsce);
        }
      }
      else
      {
        if ((queue) && (this.objectType == Type.WAGONER))
        {
          this.inQueue = getBooleanProp("inqueue");
          this.waitAccept = getBooleanProp("waitaccept");
          this.inProgress = getBooleanProp("inprogress");
          this.delivered = getBooleanProp("delivered");
          this.rejected = getBooleanProp("rejected");
          this.cancelled = getBooleanProp("cancelled");
          
          ManageObjectList mol = new ManageObjectList(this.player, Type.WAGONER, selId, true, 1, "", this.includeAll, this.inQueue, this.waitAccept, this.inProgress, this.delivered, this.rejected, this.cancelled);
          
          mol.sendQuestion2();
          return;
        }
        this.inQueue = getBooleanProp("inqueue");
        this.waitAccept = getBooleanProp("waitaccept");
        this.inProgress = getBooleanProp("inprogress");
        this.delivered = getBooleanProp("delivered");
        this.rejected = getBooleanProp("rejected");
        this.cancelled = getBooleanProp("cancelled");
        
        ManageObjectList mol = new ManageObjectList(this.player, Type.DELIVERY, selId, true, 1, "", this.includeAll, this.inQueue, this.waitAccept, this.inProgress, this.delivered, this.rejected, this.cancelled);
        
        mol.sendQuestion3();
        return;
      }
    }
    for (String key : getAnswer().stringPropertyNames()) {
      if (key.startsWith("sort"))
      {
        String sid = key.substring(4);
        int newSort = Integer.parseInt(sid);
        if (this.showingQueue)
        {
          ManageObjectList mol = new ManageObjectList(this.player, this.objectType, this.target, this.fromList, newSort, this.searchName, this.includeAll, this.inQueue, this.waitAccept, this.inProgress, this.delivered, this.rejected, this.cancelled);
          
          mol.sendQuestion2();
          return;
        }
        if (this.objectType == Type.DELIVERY)
        {
          ManageObjectList mol = new ManageObjectList(this.player, this.objectType, this.target, this.fromList, newSort, this.searchName, this.includeAll, this.inQueue, this.waitAccept, this.inProgress, this.delivered, this.rejected, this.cancelled);
          
          mol.sendQuestion3();
          return;
        }
        ManageObjectList mol = new ManageObjectList(this.player, this.objectType, this.target, this.fromList, newSort, this.searchName, this.includeAll);
        mol.sendQuestion();
        return;
      }
    }
    if (this.objectType == Type.BUILDING) {
      for (String key : getAnswer().stringPropertyNames()) {
        if (key.startsWith("demolish"))
        {
          String sid = key.substring(8);
          long id = Long.parseLong(sid);
          try
          {
            Structure structure = Structures.getStructure(id);
            if (structure.isOnSurface()) {
              Zones.flash(structure.getCenterX(), structure.getCenterY(), false);
            }
            structure.totallyDestroy();
          }
          catch (NoSuchStructureException nsse)
          {
            this.player.getCommunicator().sendNormalServerMessage("Cannot find structure, it was here a minute ago!");
            logger.log(Level.WARNING, "Cannot find structure, it was here a minute ago! Id:" + id, nsse);
          }
        }
      }
    } else if (this.objectType == Type.WAGONER) {
      for (String key : getAnswer().stringPropertyNames()) {
        if (key.startsWith("dismiss"))
        {
          String sid = key.substring(7);
          long id = Long.parseLong(sid);
          try
          {
            Creature creature = Creatures.getInstance().getCreature(id);
            Wagoner wagoner = creature.getWagoner();
            if (wagoner.getVillageId() == -1)
            {
              this.player.getCommunicator().sendNormalServerMessage("Wagoner is already dismissing!");
            }
            else
            {
              WagonerDismissQuestion wdq = new WagonerDismissQuestion(getResponder(), wagoner);
              wdq.sendQuestion();
            }
          }
          catch (NoSuchCreatureException nsse)
          {
            this.player.getCommunicator().sendNormalServerMessage("Cannot find wagoner, it was here a minute ago!");
            logger.log(Level.WARNING, "Cannot find wagoner, it was here a minute ago! Id:" + id, nsse);
          }
        }
      }
    } else if ((this.objectType == Type.ANIMAL0) || (this.objectType == Type.ANIMAL1) || (this.objectType == Type.ANIMAL2)) {
      for (String key : getAnswer().stringPropertyNames())
      {
        if (key.startsWith("uncarefor"))
        {
          String sid = key.substring(9);
          long id = Long.parseLong(sid);
          try
          {
            int tc = Creatures.getInstance().getNumberOfCreaturesProtectedBy(this.player.getWurmId());
            int max = this.player.getNumberOfPossibleCreatureTakenCareOf();
            
            Creature animal = Creatures.getInstance().getCreature(id);
            if (animal.getCareTakerId() == this.player.getWurmId())
            {
              Creatures.getInstance().setCreatureProtected(animal, -10L, false);
              this.player.getCommunicator().sendNormalServerMessage("You let " + animal
                .getName() + " go in order to care for other creatures. You may care for " + (max - tc + 1) + " more creatures.");
            }
            else
            {
              this.player.getCommunicator().sendNormalServerMessage("You are not caring for this animal!");
            }
          }
          catch (NoSuchCreatureException nsce)
          {
            logger.log(Level.WARNING, nsce.getMessage(), nsce);
          }
        }
        if (key.startsWith("unbrand"))
        {
          String sid = key.substring(7);
          long id = Long.parseLong(sid);
          try
          {
            Creature animal = Creatures.getInstance().getCreature(id);
            Brand brand = Creatures.getInstance().getBrand(animal.getWurmId());
            if (brand != null)
            {
              if (animal.getBrandVillage() == this.player.getCitizenVillage())
              {
                if (this.player.getCitizenVillage().isActionAllowed((short)484, this.player))
                {
                  brand.deleteBrand();
                  if (animal.getVisionArea() != null) {
                    animal.getVisionArea().broadCastUpdateSelectBar(animal.getWurmId());
                  }
                }
                else
                {
                  this.player.getCommunicator().sendNormalServerMessage("You need to have deed permission to remove a brand.");
                }
              }
              else {
                this.player.getCommunicator().sendNormalServerMessage("You need to be in same village as the brand on the animal.");
              }
            }
            else {
              this.player.getCommunicator().sendNormalServerMessage("That animal is not branded.");
            }
          }
          catch (NoSuchCreatureException nsce)
          {
            logger.log(Level.WARNING, nsce.getMessage(), nsce);
          }
        }
        if (key.startsWith("untame"))
        {
          String sid = key.substring(6);
          long id = Long.parseLong(sid);
          try
          {
            Creature animal = Creatures.getInstance().getCreature(id);
            if (animal.getDominator() == this.player)
            {
              if (DbCreatureStatus.getIsLoaded(animal.getWurmId()) == 1)
              {
                this.player.getCommunicator().sendNormalServerMessage("This animal is caged, remove it first.", (byte)3);
                
                return;
              }
              Creature pet = this.player.getPet();
              if (animal.cantRideUntame())
              {
                assert (pet != null);
                Vehicle cret = Vehicles.getVehicleForId(pet.getWurmId());
                if (cret != null) {
                  cret.kickAll();
                }
              }
              animal.setDominator(-10L);
              this.player.setPet(-10L);
              this.player.getCommunicator().sendNormalServerMessage("You no longer have this animal tamed!");
            }
            else
            {
              this.player.getCommunicator().sendNormalServerMessage("You do not have this animal tamed!");
            }
          }
          catch (NoSuchCreatureException nsce)
          {
            logger.log(Level.WARNING, nsce.getMessage(), nsce);
          }
        }
      }
    }
  }
  
  public void sendQuestion()
  {
    int width = 300;
    StringBuilder buf = new StringBuilder();
    String closeBtn = "harray{" + (((this.objectType == Type.DOOR) || (this.objectType == Type.REPLY)) && (this.fromList) ? "button{text=\"Back\";id=\"back\"};" : "") + "label{text=\" \"};button{text=\"Close\";id=\"close\"};label{text=\" \"}};";
    
    buf.append("border{border{size=\"20,20\";null;null;label{type='bold';text=\"" + this.question + "\"};" + closeBtn + "null;}null;scroll{vertical=\"true\";horizontal=\"false\";varray{rescale=\"true\";passthrough{id=\"id\";text=\"" + 
    
      getId() + "\"}");
    String extraButton = "";
    if (this.objectType == Type.SEARCH)
    {
      buf.append("text{text=\"Allow searching for all objects that the player has permissions and you can manage.\"}");
      buf.append("harray{label{text=\"Look For Player \"}input{id=\"who\"}}");
      buf.append("text{text=\"\"}");
      
      buf.append("harray{button{text=\"Search\";id=\"search\";default=\"true\"}};");
      
      buf.append("}};null;null;}");
      getResponder().getCommunicator().sendBml(300, 160, true, true, buf.toString(), 200, 200, 200, this.title);
      return;
    }
    if (this.objectType == Type.REPLY)
    {
      if (this.target == -10L)
      {
        buf.append("label{text=\"No such Player\"}");
        buf.append("}};null;null;}");
        getResponder().getCommunicator().sendBml(300, 150, true, true, buf.toString(), 200, 200, 200, this.title);
        return;
      }
      Village vill = getResponder().getCitizenVillage();
      int vid = (vill != null) && (vill.getRoleFor(getResponder()).mayManageAllowedObjects()) ? vill.getId() : -1;
      
      Set<PermissionsPlayerList.ISettings> result = new HashSet();
      this.objects = Creatures.getManagedAnimalsFor(this.player, vid, true);
      for (PermissionsPlayerList.ISettings is : this.objects) {
        if (is.isGuest(this.target)) {
          result.add(is);
        }
      }
      this.objects = Structures.getManagedBuildingsFor(this.player, vid, true);
      for (PermissionsPlayerList.ISettings is : this.objects) {
        if (is.isGuest(this.target)) {
          result.add(is);
        }
      }
      this.objects = FenceGate.getManagedGatesFor(this.player, vid, true);
      for (PermissionsPlayerList.ISettings is : this.objects) {
        if (is.isGuest(this.target)) {
          result.add(is);
        }
      }
      this.objects = Items.getManagedCartsFor(this.player, true);
      for (PermissionsPlayerList.ISettings is : this.objects) {
        if (is.isGuest(this.target)) {
          result.add(is);
        }
      }
      this.objects = MineDoorPermission.getManagedMineDoorsFor(this.player, vid, true);
      for (PermissionsPlayerList.ISettings is : this.objects) {
        if (is.isGuest(this.target)) {
          result.add(is);
        }
      }
      this.objects = Items.getManagedShipsFor(this.player, true);
      for (is : this.objects) {
        if (is.isGuest(this.target)) {
          result.add(is);
        }
      }
      this.objects = ((PermissionsPlayerList.ISettings[])result.toArray(new PermissionsPlayerList.ISettings[result.size()]));
      
      buf.append("text{text=\"List of objects that player '" + this.searchName + "' has permissions for that you may manage.\"}");
      int absSortBy = Math.abs(this.sortBy);
      final int upDown = Integer.signum(this.sortBy);
      
      buf.append("table{rows=\"1\";cols=\"5\";label{text=\"\"};" + 
      
        colHeader("Name", 1, this.sortBy) + 
        colHeader("Type", 2, this.sortBy) + 
        colHeader("Owner?", 3, this.sortBy) + "label{type=\"bold\";text=\"\"};");
      switch (absSortBy)
      {
      case 1: 
        Arrays.sort(this.objects, new Comparator()
        {
          public int compare(PermissionsPlayerList.ISettings param1, PermissionsPlayerList.ISettings param2)
          {
            return param1.getObjectName().compareTo(param2.getObjectName()) * upDown;
          }
        });
        break;
      case 2: 
        Arrays.sort(this.objects, new Comparator()
        {
          public int compare(PermissionsPlayerList.ISettings param1, PermissionsPlayerList.ISettings param2)
          {
            return param1.getTypeName().compareTo(param2.getTypeName()) * upDown;
          }
        });
        break;
      case 3: 
        Arrays.sort(this.objects, new Comparator()
        {
          public int compare(PermissionsPlayerList.ISettings param1, PermissionsPlayerList.ISettings param2)
          {
            if (param1.isActualOwner(ManageObjectList.this.target) == param2.isActualOwner(ManageObjectList.this.target)) {
              return param1.getObjectName().compareTo(param2.getObjectName()) * upDown;
            }
            if (param1.isActualOwner(ManageObjectList.this.target)) {
              return -1 * upDown;
            }
            return 1 * upDown;
          }
        });
      }
      PermissionsPlayerList.ISettings[] arrayOfISettings2 = this.objects;PermissionsPlayerList.ISettings is = arrayOfISettings2.length;
      for (PermissionsPlayerList.ISettings localISettings1 = 0; localISettings1 < is; localISettings1++)
      {
        PermissionsPlayerList.ISettings object = arrayOfISettings2[localISettings1];
        
        buf.append("radio{group=\"sel\";id=\"" + object.getWurmId() + "\";text=\"\"}label{text=\"" + object
          .getObjectName() + "\"};label{text=\"" + object
          .getTypeName() + "\"};label{" + 
          showBoolean(object.isActualOwner(this.target)) + "};label{text=\"\"}");
      }
      buf.append("}");
      if (result.size() > 0) {
        extraButton = ";label{text=\"  \"};button{text=\"Remove all Permissions\";id=\"remall\"}";
      }
    }
    else if ((this.objectType == Type.ANIMAL0) || (this.objectType == Type.ANIMAL1) || (this.objectType == Type.ANIMAL2))
    {
      buf.append(makeAnimalList());
      if (!Servers.isThisAPvpServer()) {
        extraButton = ";label{text=\"  \"};button{text=\"Give direction to\";id=\"find\"}";
      }
      width = 550;
    }
    else if (this.objectType == Type.BUILDING)
    {
      buf.append(makeBuildingList());
      extraButton = ";label{text=\"  \"};button{text=\"Manage All Doors\";id=\"doors\"}";
      width = 500;
    }
    else if ((this.objectType == Type.LARGE_CART) || (this.objectType == Type.SMALL_CART) || (this.objectType == Type.WAGON) || (this.objectType == Type.SHIP_CARRIER) || (this.objectType == Type.CREATURE_CARRIER))
    {
      buf.append(makeLandVehicleList());
      width = 500;
    }
    else if (this.objectType == Type.DOOR)
    {
      buf.append(makeDoorList());
      width = 500;
    }
    else if (this.objectType == Type.GATE)
    {
      buf.append(makeGateList());
      width = 600;
    }
    else if (this.objectType == Type.MINEDOOR)
    {
      buf.append(makeMineDoorList());
      width = 400;
    }
    else if (this.objectType == Type.SHIP)
    {
      buf.append(makeShipList());
      width = 500;
    }
    else if (this.objectType == Type.WAGONER)
    {
      buf.append(makeWagonerList());
      width = 600;
    }
    buf.append("radio{group=\"sel\";id=\"-10\";selected=\"true\";text=\"None\"}");
    buf.append("text{text=\"\"}");
    
    buf.append("harray{button{text=\"Manage Permissions\";id=\"permissions\"}" + extraButton + "};");
    if (this.objectType == Type.WAGONER)
    {
      buf.append("text{text=\"\"}");
      buf.append("harray{button{text=\"View Deliveries\";id=\"queue\"};label{text=\" filter by \"};checkbox{id=\"inqueue\";text=\"In queue  \"" + (this.inQueue ? ";selected=\"true\"" : "") + "};checkbox{id=\"waitaccept\";text=\"Waiting for accept  \"" + (this.waitAccept ? ";selected=\"true\"" : "") + "};checkbox{id=\"inprogress\";text=\"In Progress  \"" + (this.inProgress ? ";selected=\"true\"" : "") + "};checkbox{id=\"delivered\";text=\"Delivered  \"" + (this.delivered ? ";selected=\"true\"" : "") + "};checkbox{id=\"rejected\";text=\"Rejected  \"" + (this.rejected ? ";selected=\"true\"" : "") + "};checkbox{id=\"cancelled\";text=\"Cancelled  \"" + (this.cancelled ? ";selected=\"true\"" : "") + "};};");
    }
    buf.append("}};null;null;}");
    getResponder().getCommunicator().sendBml(width, 400, true, true, buf.toString(), 200, 200, 200, this.title);
  }
  
  private void sendQuestion2()
  {
    StringBuilder buf = new StringBuilder();
    String closeBtn = "harray{button{text=\"Back\";id=\"back\"};label{text=\" \"};button{text=\"Close\";id=\"close\"};label{text=\" \"}};";
    
    buf.append("border{border{size=\"20,20\";null;null;label{type='bold';text=\"" + this.question + "\"};" + "harray{button{text=\"Back\";id=\"back\"};label{text=\" \"};button{text=\"Close\";id=\"close\"};label{text=\" \"}};" + "null;}null;scroll{vertical=\"true\";horizontal=\"false\";varray{rescale=\"true\";passthrough{id=\"id\";text=\"" + 
    
      getId() + "\"}");
    
    int absSortBy = Math.abs(this.sortBy);
    final int upDown = Integer.signum(this.sortBy);
    
    Delivery[] deliveries = Delivery.getDeliveriesFor(this.target, this.inQueue, this.waitAccept, this.inProgress, this.rejected, this.delivered);
    
    buf.append("table{rows=\"1\";cols=\"6\";label{text=\"\"};" + 
    
      colHeader("id", 1, this.sortBy) + 
      colHeader("Sender", 2, this.sortBy) + 
      colHeader("Receiver", 3, this.sortBy) + 
      colHeader("State", 4, this.sortBy) + "label{text=\"\"};");
    switch (absSortBy)
    {
    case 1: 
      Arrays.sort(deliveries, new Comparator()
      {
        public int compare(Delivery param1, Delivery param2)
        {
          long value1 = param1.getDeliveryId();
          long value2 = param2.getDeliveryId();
          if (value1 == value2) {
            return 0;
          }
          if (value1 < value2) {
            return -1 * upDown;
          }
          return 1 * upDown;
        }
      });
      break;
    case 2: 
      Arrays.sort(deliveries, new Comparator()
      {
        public int compare(Delivery param1, Delivery param2)
        {
          return param1.getSenderName().compareTo(param2.getSenderName()) * upDown;
        }
      });
      break;
    case 3: 
      Arrays.sort(deliveries, new Comparator()
      {
        public int compare(Delivery param1, Delivery param2)
        {
          return param1.getReceiverName().compareTo(param2.getReceiverName()) * upDown;
        }
      });
      break;
    case 4: 
      Arrays.sort(deliveries, new Comparator()
      {
        public int compare(Delivery param1, Delivery param2)
        {
          return param1.getStateName().compareTo(param2.getStateName()) * upDown;
        }
      });
    }
    for (Delivery delivery : deliveries) {
      buf.append("radio{group=\"sel\";id=\"" + delivery.getDeliveryId() + "\";text=\"\"}label{text=\"" + delivery
        .getDeliveryId() + "\"};label{text=\"" + delivery
        .getSenderName() + "\"};label{text=\"" + delivery
        .getReceiverName() + "\"};label{text=\"" + delivery
        .getStateName() + "\"};label{text=\"  \"};");
    }
    buf.append("}");
    buf.append("radio{group=\"sel\";id=\"-10\";selected=\"true\";text=\"None\"}");
    buf.append("text{text=\"\"}");
    buf.append("harray{button{text=\"View Delivery\";id=\"delivery\"};};");
    
    buf.append("}};null;null;}");
    getResponder().getCommunicator().sendBml(400, 400, true, true, buf.toString(), 200, 200, 200, this.title);
  }
  
  private void sendQuestion3()
  {
    StringBuilder buf = new StringBuilder();
    String closeBtn = "harray{button{text=\"Back\";id=\"back\"};label{text=\" \"};button{text=\"Close\";id=\"close\"};label{text=\" \"}};";
    
    buf.append("border{border{size=\"20,20\";null;null;label{type='bold';text=\"" + this.question + "\"};" + "harray{button{text=\"Back\";id=\"back\"};label{text=\" \"};button{text=\"Close\";id=\"close\"};label{text=\" \"}};" + "null;}null;scroll{vertical=\"true\";horizontal=\"false\";varray{rescale=\"true\";passthrough{id=\"id\";text=\"" + 
    
      getId() + "\"}");
    
    Delivery delivery = Delivery.getDelivery(this.target);
    buf.append("table{rows=\"1\";cols=\"4\";");
    
    buf.append("label{text=\"\"};label{text=\"Id\"};label{text=\"\"};label{text=\"" + delivery
    
      .getDeliveryId() + "\"};");
    
    buf.append("label{text=\"\"};label{text=\"Sender\"};label{text=\"\"};label{text=\"" + delivery
    
      .getSenderName() + "\"};");
    
    buf.append("label{text=\"\"};label{text=\"Receiver\"};label{text=\"\"};label{text=\"" + delivery
    
      .getReceiverName() + "\"};");
    
    buf.append("label{text=\"\"};label{text=\"State\"};label{text=\"\"};label{text=\"" + delivery
    
      .getStateName() + "\"};");
    
    buf.append("label{text=\"\"};label{text=\"Delivery setup\"};label{text=\"@\"};label{text=\"" + delivery
    
      .getStringWaitingForAccept() + "\"};");
    
    String reason = "Accepted";
    switch (delivery.getState())
    {
    case 6: 
    case 11: 
      reason = "Timed Out";
      break;
    case 5: 
    case 8: 
      reason = "Rejected";
      break;
    case 9: 
    case 10: 
      reason = "Cancelled";
    }
    buf.append("label{text=\"\"};label{text=\"" + reason + "\"};label{text=\"@\"};label{text=\"" + delivery
    
      .getStringAcceptedOrRejected() + "\"};");
    
    buf.append("label{text=\"\"};label{text=\"Delivery started\"};label{text=\"@\"};label{text=\"" + delivery
    
      .getStringDeliveryStarted() + "\"};");
    
    buf.append("label{text=\"\"};label{text=\"Crates picked up\"};label{text=\"@\"};label{text=\"" + delivery
    
      .getStringPickedUp() + "\"};");
    
    buf.append("label{text=\"\"};label{text=\"Crates delivered\"};label{text=\"@\"};label{text=\"" + delivery
    
      .getStringDelivered() + "\"};");
    
    buf.append("}");
    
    buf.append("}};null;null;}");
    getResponder().getCommunicator().sendBml(400, 400, true, true, buf.toString(), 200, 200, 200, this.title);
  }
  
  private String makeAnimalList()
  {
    StringBuilder buf = new StringBuilder();
    
    Village vill = getResponder().getCitizenVillage();
    final int vid = (vill != null) && (vill.getRoleFor(getResponder()).mayManageAllowedObjects()) ? vill.getId() : -1;
    
    buf.append("text{text=\"As well as the list containing any animals that you care for, and any tamed animals you have. It also includes any animals that are branded to your village that have 'Settlement may manage' Permission set to your village so long as you have the 'Manage Allowed Objects' settlement permission.\"}");
    
    buf.append("text{text=\"\"}");
    
    Creature[] animals = Creatures.getManagedAnimalsFor(this.player, vid, this.includeAll);
    int absSortBy = Math.abs(this.sortBy);
    final int upDown = Integer.signum(this.sortBy);
    
    buf.append("table{rows=\"1\";cols=\"8\";label{text=\"\"};" + 
    
      colHeader("Name", 1, this.sortBy) + 
      colHeader("Animal Type", 2, this.sortBy) + 
      colHeader("On Deed?", 3, this.sortBy) + 
      colHeader("Hitched?", 4, this.sortBy) + 
      colHeader("Cared For?", 5, this.sortBy) + 
      colHeader("Branded?", 6, this.sortBy) + 
      colHeader("Tamed?", 7, this.sortBy));
    switch (absSortBy)
    {
    case 1: 
      Arrays.sort(animals, new Comparator()
      {
        public int compare(Creature param1, Creature param2)
        {
          return param1.getName().compareTo(param2.getName()) * upDown;
        }
      });
      break;
    case 2: 
      Arrays.sort(animals, new Comparator()
      {
        public int compare(Creature param1, Creature param2)
        {
          return param1.getTypeName().compareTo(param2.getTypeName()) * upDown;
        }
      });
      break;
    case 3: 
      Arrays.sort(animals, new Comparator()
      {
        public int compare(Creature param1, Creature param2)
        {
          if (param1.isOnDeed() == param2.isOnDeed()) {
            return param1.getName().compareTo(param2.getName()) * upDown;
          }
          if (param1.isOnDeed()) {
            return 1 * upDown;
          }
          return -1 * upDown;
        }
      });
      break;
    case 4: 
      Arrays.sort(animals, new Comparator()
      {
        public int compare(Creature param1, Creature param2)
        {
          if (param1.isHitched() == param2.isHitched()) {
            return param1.getName().compareTo(param2.getName()) * upDown;
          }
          if (param1.isHitched()) {
            return 1 * upDown;
          }
          return -1 * upDown;
        }
      });
      break;
    case 5: 
      Arrays.sort(animals, new Comparator()
      {
        public int compare(Creature param1, Creature param2)
        {
          if (param1.isCaredFor(ManageObjectList.this.player) == param2.isCaredFor(ManageObjectList.this.player)) {
            return param1.getName().compareTo(param2.getName()) * upDown;
          }
          if (param1.isCaredFor(ManageObjectList.this.player)) {
            return 1 * upDown;
          }
          return -1 * upDown;
        }
      });
      break;
    case 6: 
      Arrays.sort(animals, new Comparator()
      {
        public int compare(Creature param1, Creature param2)
        {
          if (param1.isBrandedBy(vid) == param2.isBrandedBy(vid)) {
            return param1.getName().compareTo(param2.getName()) * upDown;
          }
          if (param1.isBrandedBy(vid)) {
            return 1 * upDown;
          }
          return -1 * upDown;
        }
      });
      break;
    case 7: 
      Arrays.sort(animals, new Comparator()
      {
        public int compare(Creature param1, Creature param2)
        {
          if (param1.isDominated() == param2.isDominated()) {
            return param1.getName().compareTo(param2.getName()) * upDown;
          }
          if (param1.isDominated()) {
            return 1 * upDown;
          }
          return -1 * upDown;
        }
      });
    }
    for (Creature animal : animals)
    {
      buf.append((animal.canHavePermissions() ? "radio{group=\"sel\";id=\"" + animal.getWurmId() + "\";text=\"\"}" : "label{text=\"\"};") + "label{text=\"" + animal
        .getName() + "\"};label{text=\"" + animal
        .getTypeName() + "\"};" + (animal
        .isBranded() ? "label{" + showBoolean(animal.isOnDeed()) + "};" : "label{text=\"not branded\"};") + "label{" + 
        showBoolean(animal.isHitched()) + "};");
      if (animal.isCaredFor(this.player)) {
        buf.append(unCareForButton(animal));
      } else {
        buf.append("label{" + showBoolean(animal.getCareTakerId() != -10L) + "};");
      }
      if ((animal.isBranded()) && (animal.getBrandVillage() == this.player.getCitizenVillage()) && 
        (this.player.getCitizenVillage().isActionAllowed((short)484, this.player))) {
        buf.append(unBrandButton(animal));
      } else {
        buf.append("label{" + showBoolean(animal.isBranded()) + "};");
      }
      if ((animal.isDominated()) && (animal.getDominator() == this.player)) {
        buf.append(unTameButton(animal));
      } else {
        buf.append("label{" + showBoolean(animal.isDominated()) + "};");
      }
    }
    buf.append("}");
    return buf.toString();
  }
  
  private String unCareForButton(Creature animal)
  {
    StringBuilder buf = new StringBuilder();
    buf.append("harray{button{text=\"Un-Care For\";id=\"uncarefor" + animal.getWurmId() + "\";confirm=\"You are about to un care for " + animal
      .getName() + ".\";question=\"Do you really want to do that?\"}label{text=\" \"}}");
    
    return buf.toString();
  }
  
  private String unBrandButton(Creature animal)
  {
    StringBuilder buf = new StringBuilder();
    buf.append("harray{button{text=\"Un-Brand\";id=\"unbrand" + animal.getWurmId() + "\";confirm=\"You are about to remove the brand from " + animal
      .getName() + ".\";question=\"Do you really want to do that?\"}label{text=\" \"}}");
    
    return buf.toString();
  }
  
  private String unTameButton(Creature animal)
  {
    StringBuilder buf = new StringBuilder();
    buf.append("harray{button{text=\"Un-Tame\";id=\"untame" + animal.getWurmId() + "\";confirm=\"You are about to un tame " + animal
      .getName() + ".\";question=\"Do you really want to do that?\"}label{text=\" \"}}");
    
    return buf.toString();
  }
  
  private String makeBuildingList()
  {
    StringBuilder buf = new StringBuilder();
    buf.append("text{text=\"List includes any buildings that you are the owner of plus any buildings in your settlment that have 'Settlement may manage' Permission set to your village so long as you have the 'Manage Allowed Objects' settlement permission.\"}");
    
    buf.append("text{text=\"\"}");
    
    Village vill = getResponder().getCitizenVillage();
    int vid = (vill != null) && (vill.getRoleFor(getResponder()).mayManageAllowedObjects()) ? vill.getId() : -1;
    
    Structure[] structures = Structures.getManagedBuildingsFor(this.player, vid, this.includeAll);
    int absSortBy = Math.abs(this.sortBy);
    final int upDown = Integer.signum(this.sortBy);
    
    buf.append("table{rows=\"1\";cols=\"7\";label{text=\"\"};" + 
    
      colHeader("Name", 1, this.sortBy) + 
      colHeader("Owner?", 2, this.sortBy) + 
      colHeader("Doors have locks?", 3, this.sortBy) + 
      colHeader("On Deed?", 4, this.sortBy) + 
      colHeader("Deed Managed?", 5, this.sortBy) + "label{type=\"bold\";text=\"\"};");
    switch (absSortBy)
    {
    case 1: 
      Arrays.sort(structures, new Comparator()
      {
        public int compare(Structure param1, Structure param2)
        {
          return param1.getObjectName().compareTo(param2.getObjectName()) * upDown;
        }
      });
      break;
    case 2: 
      Arrays.sort(structures, new Comparator()
      {
        public int compare(Structure param1, Structure param2)
        {
          if (param1.isActualOwner(ManageObjectList.this.player.getWurmId()) == param2.isActualOwner(ManageObjectList.this.player.getWurmId())) {
            return param1.getObjectName().compareTo(param2.getObjectName()) * upDown;
          }
          if (param1.isActualOwner(ManageObjectList.this.player.getWurmId())) {
            return -1 * upDown;
          }
          return 1 * upDown;
        }
      });
      break;
    case 3: 
      Arrays.sort(structures, new Comparator()
      {
        public int compare(Structure param1, Structure param2)
        {
          int value1 = param1.isLockable() ? 1 : param1.getAllDoors().length == 0 ? 0 : 2;
          int value2 = param2.isLockable() ? 1 : param2.getAllDoors().length == 0 ? 0 : 2;
          if (value1 == value2) {
            return param1.getObjectName().compareTo(param2.getObjectName()) * upDown;
          }
          if (value1 < value2) {
            return 1 * upDown;
          }
          return -1 * upDown;
        }
      });
      break;
    case 4: 
      Arrays.sort(structures, new Comparator()
      {
        public int compare(Structure param1, Structure param2)
        {
          int value1 = param1.getVillage() != null ? param1.getVillage().getId() : 0;
          int value2 = param2.getVillage() != null ? param2.getVillage().getId() : 0;
          if (value1 == value2) {
            return param1.getObjectName().compareTo(param2.getObjectName()) * upDown;
          }
          if (value1 < value2) {
            return 1 * upDown;
          }
          return -1 * upDown;
        }
      });
      break;
    case 5: 
      Arrays.sort(structures, new Comparator()
      {
        public int compare(Structure param1, Structure param2)
        {
          if (param1.isManaged() == param2.isManaged()) {
            return param1.getObjectName().compareTo(param2.getObjectName()) * upDown;
          }
          if (param1.isManaged()) {
            return -1 * upDown;
          }
          return 1 * upDown;
        }
      });
    }
    for (Structure structure : structures)
    {
      buf.append((structure.canHavePermissions() ? "radio{group=\"sel\";id=\"" + structure.getWurmId() + "\";text=\"\"}" : "label{text=\"\"};") + "label{text=\"" + structure
        .getObjectName() + "\"};label{" + 
        showBoolean(structure.isActualOwner(this.player.getWurmId())) + "};");
      if (structure.getAllDoors().length == 0) {
        buf.append("label{color=\"255,177,40\"text=\"No lockable doors.\"};");
      } else if (structure.isLockable()) {
        buf.append("label{color=\"127,255,127\"text=\"true\"};");
      } else {
        buf.append("label{color=\"255,127,127\"text=\"Not all doors have locks.\"};");
      }
      buf.append("label{" + showBoolean(structure.getVillage() != null) + "};");
      buf.append("label{" + showBoolean(structure.isManaged()) + "};");
      if (structure.isOwner(this.player.getWurmId())) {
        buf.append("harray{label{text=\" \"};button{text=\"Demolish\";id=\"demolish" + structure
          .getWurmId() + "\";confirm=\"You are about to blow up the building '" + structure
          .getObjectName() + "'.\";question=\"Do you really want to do that?\"}label{text=\" \"}}");
      } else {
        buf.append("label{text=\" \"}");
      }
    }
    buf.append("}");
    return buf.toString();
  }
  
  private String makeDoorList()
  {
    StringBuilder buf = new StringBuilder();
    try
    {
      Structure structure = Structures.getStructure(this.target);
      
      buf.append("text{text=\"List includes all doors in this building if you are the owner, or any doors in this building that have the 'Building may manage' Permission so long as you have the 'Manage Permissions' building permission.\"}");
      
      buf.append("text{text=\"Note: Owner of the Door is the Owner of the bulding.\"}");
      buf.append("text{text=\"\"}");
      buf.append("text{type=\"bold\";text=\"List of Doors that you may manage in this building.\"}");
      if (this.includeAll) {
        buf.append(extraButton("Exclude Doors without locks", "exc"));
      } else {
        buf.append(extraButton("Include Doors without locks", "inc"));
      }
      Door[] doors = structure.getAllDoors(this.includeAll);
      int absSortBy = Math.abs(this.sortBy);
      final int upDown = Integer.signum(this.sortBy);
      
      buf.append("table{rows=\"1\";cols=\"7\";label{text=\"\"};" + 
      
        colHeader("Name", 1, this.sortBy) + 
        colHeader("Door Type", 2, this.sortBy) + 
        colHeader("Level", 3, this.sortBy) + 
        colHeader("Has Lock?", 4, this.sortBy) + 
        colHeader("Locked?", 5, this.sortBy) + 
        colHeader("Building Managed?", 6, this.sortBy));
      
      Arrays.sort(doors, new Comparator()
      {
        public int compare(Door param1, Door param2)
        {
          if (param1.getFloorLevel() == param2.getFloorLevel())
          {
            int comp = param1.getTypeName().compareTo(param2.getTypeName());
            if (comp == 0) {
              return param1.getObjectName().compareTo(param2.getObjectName()) * upDown;
            }
            return comp * upDown;
          }
          if (param1.getFloorLevel() < param2.getFloorLevel()) {
            return 1 * upDown;
          }
          return -1 * upDown;
        }
      });
      switch (absSortBy)
      {
      case 1: 
        Arrays.sort(doors, new Comparator()
        {
          public int compare(Door param1, Door param2)
          {
            return param1.getObjectName().compareTo(param2.getObjectName()) * upDown;
          }
        });
        break;
      case 2: 
        Arrays.sort(doors, new Comparator()
        {
          public int compare(Door param1, Door param2)
          {
            return param1.getTypeName().compareTo(param2.getTypeName()) * upDown;
          }
        });
        break;
      case 3: 
        Arrays.sort(doors, new Comparator()
        {
          public int compare(Door param1, Door param2)
          {
            if (param1.getFloorLevel() == param2.getFloorLevel()) {
              return 0;
            }
            if (param1.getFloorLevel() < param2.getFloorLevel()) {
              return 1 * upDown;
            }
            return -1 * upDown;
          }
        });
        break;
      case 4: 
        Arrays.sort(doors, new Comparator()
        {
          public int compare(Door param1, Door param2)
          {
            if (param1.hasLock() == param2.hasLock()) {
              return 0;
            }
            if (param1.hasLock()) {
              return -1 * upDown;
            }
            return 1 * upDown;
          }
        });
        break;
      case 5: 
        Arrays.sort(doors, new Comparator()
        {
          public int compare(Door param1, Door param2)
          {
            if (param1.isLocked() == param2.isLocked()) {
              return 0;
            }
            if (param1.isLocked()) {
              return -1 * upDown;
            }
            return 1 * upDown;
          }
        });
        break;
      case 6: 
        Arrays.sort(doors, new Comparator()
        {
          public int compare(Door param1, Door param2)
          {
            if (param1.isManaged() == param2.isManaged()) {
              return 0;
            }
            if (param1.isManaged()) {
              return -1 * upDown;
            }
            return 1 * upDown;
          }
        });
      }
      for (Door door : doors) {
        buf.append((door.canHavePermissions() ? "radio{group=\"sel\";id=\"" + door.getWurmId() + "\";text=\"\"}" : "label{text=\"\"}") + "label{text=\"" + door
          .getObjectName() + "\"};label{text=\"" + door
          .getTypeName() + "\"};label{text=\"" + door
          .getFloorLevel() + "\"};label{" + 
          showBoolean(door.hasLock()) + "};label{" + 
          showBoolean(door.isLocked()) + "};label{" + 
          showBoolean(door.isManaged()) + "};");
      }
      buf.append("}");
    }
    catch (NoSuchStructureException nsse)
    {
      logger.log(Level.WARNING, "Cannot find structure, it was here a minute ago! Id:" + this.target, nsse);
      buf.append("text{text=\"Cannot find structure, it was here a minute ago!\"}");
    }
    return buf.toString();
  }
  
  private String makeGateList()
  {
    StringBuilder buf = new StringBuilder();
    buf.append("text{text=\"As well as the list containing any gates that you are the owner of their lock it also includes any gate that have 'Settlement may manage' Permission set to your village so long as you have the 'Manage Allowed Objects' settlement permission.\"}");
    
    Village vill = getResponder().getCitizenVillage();
    if ((vill != null) && (vill.isMayor(this.player))) {
      buf.append("text{text=\"As you are a mayor, the list will have all gates on your deed.\"}");
    }
    buf.append("text{text=\"\"}");
    if (this.includeAll) {
      buf.append(extraButton("Exclude Gates without locks", "exc"));
    } else {
      buf.append(extraButton("Include Gates without locks", "inc"));
    }
    int vid = (vill != null) && (vill.getRoleFor(getResponder()).mayManageAllowedObjects()) ? vill.getId() : -1;
    
    FenceGate[] gates = FenceGate.getManagedGatesFor(this.player, vid, this.includeAll);
    int absSortBy = Math.abs(this.sortBy);
    final int upDown = Integer.signum(this.sortBy);
    
    buf.append("table{rows=\"1\";cols=\"9\";label{text=\"\"};" + 
    
      colHeader("Name", 1, this.sortBy) + 
      colHeader("Gate Type", 2, this.sortBy) + 
      colHeader("Level", 3, this.sortBy) + 
      colHeader("Has Lock?", 4, this.sortBy) + 
      colHeader("Locked?", 5, this.sortBy) + 
      colHeader("Owner?", 6, this.sortBy) + 
      colHeader("On Deed?", 7, this.sortBy) + 
      colHeader("Deed Managed?", 8, this.sortBy));
    
    Arrays.sort(gates, new Comparator()
    {
      public int compare(FenceGate param1, FenceGate param2)
      {
        if (param1.getFloorLevel() == param2.getFloorLevel())
        {
          int comp = param1.getTypeName().compareTo(param2.getTypeName());
          if (comp == 0) {
            return param1.getObjectName().compareTo(param2.getObjectName()) * upDown;
          }
          return comp * upDown;
        }
        if (param1.getFloorLevel() < param2.getFloorLevel()) {
          return 1 * upDown;
        }
        return -1 * upDown;
      }
    });
    switch (absSortBy)
    {
    case 1: 
      Arrays.sort(gates, new Comparator()
      {
        public int compare(FenceGate param1, FenceGate param2)
        {
          return param1.getObjectName().compareTo(param2.getObjectName()) * upDown;
        }
      });
      break;
    case 2: 
      Arrays.sort(gates, new Comparator()
      {
        public int compare(FenceGate param1, FenceGate param2)
        {
          return param1.getTypeName().compareTo(param2.getTypeName()) * upDown;
        }
      });
      break;
    case 3: 
      Arrays.sort(gates, new Comparator()
      {
        public int compare(FenceGate param1, FenceGate param2)
        {
          if (param1.getFloorLevel() == param2.getFloorLevel()) {
            return 0;
          }
          if (param1.getFloorLevel() < param2.getFloorLevel()) {
            return 1 * upDown;
          }
          return -1 * upDown;
        }
      });
      break;
    case 4: 
      Arrays.sort(gates, new Comparator()
      {
        public int compare(FenceGate param1, FenceGate param2)
        {
          if (param1.hasLock() == param2.hasLock()) {
            return 0;
          }
          if (param1.hasLock()) {
            return -1 * upDown;
          }
          return 1 * upDown;
        }
      });
      break;
    case 5: 
      Arrays.sort(gates, new Comparator()
      {
        public int compare(FenceGate param1, FenceGate param2)
        {
          if (param1.isLocked() == param2.isLocked()) {
            return 0;
          }
          if (param1.isLocked()) {
            return -1 * upDown;
          }
          return 1 * upDown;
        }
      });
      break;
    case 6: 
      Arrays.sort(gates, new Comparator()
      {
        public int compare(FenceGate param1, FenceGate param2)
        {
          if (param1.isActualOwner(ManageObjectList.this.player.getWurmId()) == param2.isActualOwner(ManageObjectList.this.player.getWurmId())) {
            return param1.getObjectName().compareTo(param2.getObjectName()) * upDown;
          }
          if (param1.isActualOwner(ManageObjectList.this.player.getWurmId())) {
            return -1 * upDown;
          }
          return 1 * upDown;
        }
      });
      break;
    case 7: 
      Arrays.sort(gates, new Comparator()
      {
        public int compare(FenceGate param1, FenceGate param2)
        {
          int value1 = param1.getVillage() != null ? param1.getVillage().getId() : 0;
          int value2 = param2.getVillage() != null ? param2.getVillage().getId() : 0;
          if (value1 == value2) {
            return 0;
          }
          if (value1 < value2) {
            return -1 * upDown;
          }
          return 1 * upDown;
        }
      });
      break;
    case 8: 
      Arrays.sort(gates, new Comparator()
      {
        public int compare(FenceGate param1, FenceGate param2)
        {
          if (param1.isManaged() == param2.isManaged()) {
            return 0;
          }
          if (param1.isManaged()) {
            return -1 * upDown;
          }
          return 1 * upDown;
        }
      });
    }
    for (FenceGate gate : gates) {
      buf.append((gate.canHavePermissions() ? "radio{group=\"sel\";id=\"" + gate.getWurmId() + "\";text=\"\"}" : "label{text=\"\"}") + "label{text=\"" + gate
        .getObjectName() + "\"};label{text=\"" + gate
        .getTypeName() + "\"};label{text=\"" + gate
        .getFloorLevel() + "\"};label{" + 
        showBoolean(gate.hasLock()) + "};label{" + 
        showBoolean(gate.isLocked()) + "};label{" + 
        showBoolean(gate.isActualOwner(this.player.getWurmId())) + "};label{" + 
        showBoolean(gate.getVillage() != null) + "};label{" + 
        showBoolean(gate.isManaged()) + "};");
    }
    buf.append("}");
    return buf.toString();
  }
  
  private String makeLandVehicleList()
  {
    StringBuilder buf = new StringBuilder();
    buf.append("text{text=\"List contains the Small Carts, Large Carts, Wagons and Carriers that you can manage.\"}");
    buf.append("text{text=\"\"}");
    buf.append("text{type=\"bold\";text=\"List of Small Carts, Large Carts, Wagons and Carriers that you may manage.\"}");
    if (this.includeAll) {
      buf.append(extraButton("Exclude Vehicles without locks", "exc"));
    } else {
      buf.append(extraButton("Include Vehicles without locks", "inc"));
    }
    Item[] items = Items.getManagedCartsFor(this.player, this.includeAll);
    int absSortBy = Math.abs(this.sortBy);
    final int upDown = Integer.signum(this.sortBy);
    
    buf.append("table{rows=\"1\";cols=\"6\";label{text=\"\"};" + 
    
      colHeader("Name", 1, this.sortBy) + 
      colHeader("Type", 2, this.sortBy) + 
      colHeader("Owner?", 3, this.sortBy) + 
      colHeader("Locked?", 4, this.sortBy) + "label{type=\"bold\";text=\"\"};");
    switch (absSortBy)
    {
    case 1: 
      Arrays.sort(items, new Comparator()
      {
        public int compare(Item param1, Item param2)
        {
          return param1.getObjectName().compareTo(param2.getObjectName()) * upDown;
        }
      });
      break;
    case 2: 
      Arrays.sort(items, new Comparator()
      {
        public int compare(Item param1, Item param2)
        {
          return param1.getTypeName().compareTo(param2.getTypeName()) * upDown;
        }
      });
      break;
    case 3: 
      Arrays.sort(items, new Comparator()
      {
        public int compare(Item param1, Item param2)
        {
          if (param1.isActualOwner(ManageObjectList.this.player.getWurmId()) == param2.isActualOwner(ManageObjectList.this.player.getWurmId())) {
            return param1.getObjectName().compareTo(param2.getObjectName()) * upDown;
          }
          if (param1.isActualOwner(ManageObjectList.this.player.getWurmId())) {
            return -1 * upDown;
          }
          return 1 * upDown;
        }
      });
      break;
    case 4: 
      Arrays.sort(items, new Comparator()
      {
        public int compare(Item param1, Item param2)
        {
          if (param1.isLocked() == param2.isLocked()) {
            return param1.getObjectName().compareTo(param2.getObjectName()) * upDown;
          }
          if (param1.isLocked()) {
            return -1 * upDown;
          }
          return 1 * upDown;
        }
      });
    }
    for (Item item : items) {
      buf.append((item.canHavePermissions() ? "radio{group=\"sel\";id=\"" + item.getWurmId() + "\";text=\"\"}" : "label{text=\"\"}") + "label{text=\"" + item
        .getObjectName() + "\"};" + 
        addRariryColour(item, item.getTypeName()) + "label{" + 
        showBoolean(item.isActualOwner(this.player.getWurmId())) + "};label{" + 
        showBoolean(item.isLocked()) + "};label{text=\"\"};");
    }
    buf.append("}");
    return buf.toString();
  }
  
  private String makeMineDoorList()
  {
    Village vill = getResponder().getCitizenVillage();
    StringBuilder buf = new StringBuilder();
    buf.append("text{text=\"As well as the list containing any mine doors that you are the owner of it also includes any mine doors that have 'Settlement may manage' Permission set to your village so long as you have the 'Manage Allowed Objects' settlement permission.\"}");
    if ((vill != null) && (vill.isMayor(this.player))) {
      buf.append("text{text=\"As you are a mayor, the list will have all minedoors on your deed.\"}");
    }
    buf.append("text{text=\"\"}");
    
    int vid = (vill != null) && (vill.getRoleFor(getResponder()).mayManageAllowedObjects()) ? vill.getId() : -1;
    
    MineDoorPermission[] mineDoors = MineDoorPermission.getManagedMineDoorsFor(this.player, vid, this.includeAll);
    int absSortBy = Math.abs(this.sortBy);
    final int upDown = Integer.signum(this.sortBy);
    
    buf.append("table{rows=\"1\";cols=\"7\";label{text=\"\"};" + 
    
      colHeader("Name", 1, this.sortBy) + 
      colHeader("Door Type", 2, this.sortBy) + 
      colHeader("Owner?", 3, this.sortBy) + 
      colHeader("On Deed?", 4, this.sortBy) + 
      colHeader("Deed Managed?", 5, this.sortBy) + "label{type=\"bold\";text=\"\"};");
    switch (absSortBy)
    {
    case 1: 
      Arrays.sort(mineDoors, new Comparator()
      {
        public int compare(MineDoorPermission param1, MineDoorPermission param2)
        {
          return param1.getObjectName().compareTo(param2.getObjectName()) * upDown;
        }
      });
      break;
    case 2: 
      Arrays.sort(mineDoors, new Comparator()
      {
        public int compare(MineDoorPermission param1, MineDoorPermission param2)
        {
          return param1.getObjectName().compareTo(param2.getObjectName()) * upDown;
        }
      });
      break;
    case 3: 
      Arrays.sort(mineDoors, new Comparator()
      {
        public int compare(MineDoorPermission param1, MineDoorPermission param2)
        {
          if (param1.isActualOwner(ManageObjectList.this.player.getWurmId()) == param2.isActualOwner(ManageObjectList.this.player.getWurmId())) {
            return param1.getObjectName().compareTo(param2.getObjectName()) * upDown;
          }
          if (param1.isActualOwner(ManageObjectList.this.player.getWurmId())) {
            return -1 * upDown;
          }
          return 1 * upDown;
        }
      });
      break;
    case 4: 
      Arrays.sort(mineDoors, new Comparator()
      {
        public int compare(MineDoorPermission param1, MineDoorPermission param2)
        {
          int value1 = param1.getVillage() != null ? param1.getVillage().getId() : 0;
          int value2 = param2.getVillage() != null ? param2.getVillage().getId() : 0;
          if (value1 == value2) {
            return 0;
          }
          if (value1 < value2) {
            return -1 * upDown;
          }
          return 1 * upDown;
        }
      });
      break;
    case 5: 
      Arrays.sort(mineDoors, new Comparator()
      {
        public int compare(MineDoorPermission param1, MineDoorPermission param2)
        {
          if (param1.isManaged() == param2.isManaged()) {
            return param1.getObjectName().compareTo(param2.getObjectName()) * upDown;
          }
          if (param1.isManaged()) {
            return -1 * upDown;
          }
          return 1 * upDown;
        }
      });
    }
    for (MineDoorPermission mineDoor : mineDoors) {
      buf.append((mineDoor.canHavePermissions() ? "radio{group=\"sel\";id=\"" + mineDoor.getWurmId() + "\";text=\"\"}" : "label{text=\"\"}") + "label{text=\"" + mineDoor
        .getObjectName() + "\"};label{text=\"" + mineDoor
        .getTypeName() + "\"};label{" + 
        showBoolean(mineDoor.isActualOwner(this.player.getWurmId())) + "};label{" + 
        showBoolean(mineDoor.getVillage() != null) + "};label{" + 
        showBoolean(mineDoor.isManaged()) + "};label{text=\" \"}");
    }
    buf.append("}");
    return buf.toString();
  }
  
  private String makeShipList()
  {
    StringBuilder buf = new StringBuilder();
    buf.append("text{text=\"List contains the Ships that you can manage.\"}");
    buf.append("text{text=\"\"}");
    buf.append("text{type=\"bold\";text=\"Will have List of Ships that you may manage.\"}");
    if (this.includeAll) {
      buf.append(extraButton("Exclude ships without locks", "exc"));
    } else {
      buf.append(extraButton("Include ships without locks", "inc"));
    }
    Item[] items = Items.getManagedShipsFor(this.player, this.includeAll);
    int absSortBy = Math.abs(this.sortBy);
    final int upDown = Integer.signum(this.sortBy);
    
    buf.append("table{rows=\"1\";cols=\"6\";label{text=\"\"};" + 
    
      colHeader("Name", 1, this.sortBy) + 
      colHeader("Type", 2, this.sortBy) + 
      colHeader("Owner?", 3, this.sortBy) + 
      colHeader("Locked?", 4, this.sortBy) + "label{type=\"bold\";text=\"\"};");
    switch (absSortBy)
    {
    case 1: 
      Arrays.sort(items, new Comparator()
      {
        public int compare(Item param1, Item param2)
        {
          return param1.getObjectName().compareTo(param2.getObjectName()) * upDown;
        }
      });
      break;
    case 2: 
      Arrays.sort(items, new Comparator()
      {
        public int compare(Item param1, Item param2)
        {
          return param1.getTypeName().compareTo(param2.getTypeName()) * upDown;
        }
      });
      break;
    case 3: 
      Arrays.sort(items, new Comparator()
      {
        public int compare(Item param1, Item param2)
        {
          if (param1.isActualOwner(ManageObjectList.this.player.getWurmId()) == param2.isActualOwner(ManageObjectList.this.player.getWurmId())) {
            return param1.getObjectName().compareTo(param2.getObjectName()) * upDown;
          }
          if (param1.isActualOwner(ManageObjectList.this.player.getWurmId())) {
            return -1 * upDown;
          }
          return 1 * upDown;
        }
      });
      break;
    case 4: 
      Arrays.sort(items, new Comparator()
      {
        public int compare(Item param1, Item param2)
        {
          if (param1.isLocked() == param2.isLocked()) {
            return param1.getObjectName().compareTo(param2.getObjectName()) * upDown;
          }
          if (param1.isLocked()) {
            return -1 * upDown;
          }
          return 1 * upDown;
        }
      });
    }
    for (Item item : items) {
      buf.append((item.canHavePermissions() ? "radio{group=\"sel\";id=\"" + item.getWurmId() + "\";text=\"\"}" : "label{text=\"\"}") + "label{text=\"" + item
        .getObjectName() + "\"};" + 
        addRariryColour(item, item.getTypeName()) + "label{" + 
        showBoolean(item.isActualOwner(this.player.getWurmId())) + "};label{" + 
        showBoolean(item.isLocked()) + "};label{text=\"\"};");
    }
    buf.append("}");
    return buf.toString();
  }
  
  private String makeWagonerList()
  {
    StringBuilder buf = new StringBuilder();
    
    int vid = -1;
    
    Creature[] animals = Creatures.getManagedWagonersFor(this.player, -1);
    int absSortBy = Math.abs(this.sortBy);
    final int upDown = Integer.signum(this.sortBy);
    
    buf.append("table{rows=\"1\";cols=\"6\";label{text=\"\"};" + 
    
      colHeader("Name", 1, this.sortBy) + 
      colHeader("State", 2, this.sortBy) + 
      colHeader("Queue", 3, this.sortBy) + "label{text=\"\"};label{text=\"\"};");
    switch (absSortBy)
    {
    case 1: 
      Arrays.sort(animals, new Comparator()
      {
        public int compare(Creature param1, Creature param2)
        {
          return param1.getName().compareTo(param2.getName()) * upDown;
        }
      });
      break;
    case 2: 
      Arrays.sort(animals, new Comparator()
      {
        public int compare(Creature param1, Creature param2)
        {
          return param1.getWagoner().getStateName().compareTo(param2.getWagoner().getStateName()) * upDown;
        }
      });
      break;
    case 3: 
      Arrays.sort(animals, new Comparator()
      {
        public int compare(Creature param1, Creature param2)
        {
          int value1 = param1.getWagoner().getQueueLength();
          int value2 = param2.getWagoner().getQueueLength();
          if (value1 == value2) {
            return 0;
          }
          if (value1 < value2) {
            return -1 * upDown;
          }
          return 1 * upDown;
        }
      });
    }
    for (Creature animal : animals)
    {
      Wagoner wagoner = animal.getWagoner();
      int queueLength = Delivery.getQueueLength(wagoner.getWurmId());
      buf.append(
        (animal.canHavePermissions() ? "radio{group=\"sel\";id=\"" + animal
        .getWurmId() + "\";text=\"\"}" : "label{text=\"\"};") + "label{text=\"" + animal
        
        .getName() + "\"};label{text=\"" + wagoner
        .getStateName() + "\"};" + (queueLength == 0 ? "label{text=\"empty\"};" : new StringBuilder().append("label{text=\"").append(queueLength).append("\"};").toString()) + "label{text=\"  \"};");
      if (animal.mayManage(getResponder()))
      {
        if (wagoner.getVillageId() == -1) {
          buf.append("label{\"Dismissing.\"};");
        } else {
          buf.append(dismissButton(animal));
        }
      }
      else {
        buf.append("label{\"\"};");
      }
    }
    buf.append("}");
    return buf.toString();
  }
  
  private String dismissButton(Creature animal)
  {
    StringBuilder buf = new StringBuilder();
    buf.append("harray{button{text=\"Dismiss\";id=\"dismiss" + animal.getWurmId() + "\";}label{text=\" \"}}");
    
    return buf.toString();
  }
  
  private String extraButton(String txt, String id)
  {
    StringBuilder buf = new StringBuilder();
    buf.append("harray{label{text=\"Filter list:\"};button{text=\"" + txt + "\";id=\"" + id + "\"}};");
    
    return buf.toString();
  }
  
  public static String addRariryColour(Item item, String name)
  {
    StringBuilder buf = new StringBuilder();
    if (item.getRarity() == 1) {
      buf.append("label{color=\"66,153,225\";text=\"rare " + name + "\"};");
    } else if (item.getRarity() == 2) {
      buf.append("label{color=\"0,255,255\";text=\"supreme " + name + "\"};");
    } else if (item.getRarity() == 3) {
      buf.append("label{color=\"255,0,255\";text=\"fantastic " + name + "\"};");
    } else {
      buf.append("label{text=\"" + name + "\"};");
    }
    return buf.toString();
  }
  
  private String showBoolean(boolean flag)
  {
    StringBuilder buf = new StringBuilder();
    if (flag) {
      buf.append("color=\"127,255,127\"");
    } else {
      buf.append("color=\"255,127,127\"");
    }
    buf.append("text=\"" + flag + "\"");
    return buf.toString();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\questions\ManageObjectList.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */