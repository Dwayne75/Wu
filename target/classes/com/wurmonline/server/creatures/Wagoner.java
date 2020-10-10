package com.wurmonline.server.creatures;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.Items;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.Servers;
import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.WurmCalendar;
import com.wurmonline.server.highways.Node;
import com.wurmonline.server.highways.PathToCalculate;
import com.wurmonline.server.highways.Route;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.utils.DbUtilities;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;

public class Wagoner
  implements MiscConstants, TimeConstants
{
  private static final Logger logger = Logger.getLogger(Wagoner.class.getName());
  public static final byte STATE_IDLE = 0;
  public static final byte STATE_GOING_TO_SLEEP = 1;
  public static final byte STATE_SLEEPING = 2;
  public static final byte STATE_WAKING_UP = 3;
  public static final byte STATE_GETTING_READY = 4;
  public static final byte STATE_DRIVING_TO_COLLECTION_POINT = 5;
  public static final byte STATE_LOADING = 6;
  public static final byte STATE_DELIVERING = 7;
  public static final byte STATE_UNLOADING = 8;
  public static final byte STATE_GOING_HOME = 9;
  public static final byte STATE_PARKING = 10;
  public static final byte STUCK_COLLECTING = 11;
  public static final byte STUCK_DELIVERING = 12;
  public static final byte STUCK_GOING_HOME = 13;
  public static final byte TEST_WAITING = 14;
  public static final byte TEST_DRIVING = 15;
  public static final int MAX_NOT_MOVING = 30;
  public static final byte SPEECH_CHATTYNESS_SELDOM = 0;
  public static final byte SPEECH_CHATTYNESS_SILENT = 1;
  public static final byte SPEECH_CHATTYNESS_NOISY = 2;
  public static final byte SPEECH_CHATTYNESS_RANDOM = 3;
  public static final byte SPEECH_CONTEXT_TEST = 1;
  public static final byte SPEECH_CONTEXT_ERROR = 2;
  public static final byte SPEECH_CONTEXT_FOOD = 4;
  public static final byte SPEECH_CONTEXT_WORK = 8;
  public static final byte SPEECH_CONTEXT_SLEEP = 16;
  public static final byte SPEECH_CONTEXT_RANDOM = 32;
  public static final byte SPEECH_STYLE_NORMAL = 0;
  public static final byte SPEECH_STYLE_WHITTY = 1;
  public static final byte SPEECH_STYLE_GRUMPY = 2;
  public static final byte SPEECH_STYLE_CHEERY = 3;
  private static final String CREATE_WAGONER = "INSERT INTO WAGONER (WURMID,STATE,OWNERID,CONTRACT_ID,HOME_WAYSTONE_ID,HOME_VILLAGE_ID,WAGON_ID,RESTING_PLACE_ID,CHAIR_ID,TENT_ID,BED_ID,DELIVERY_ID,WAGON_POSX,WAGON_POSY,WAGON_ON_SURFACE,CAMP_ROT,LAST_WAYSTONE_ID,GOAL_WAYSTONE_ID) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
  private static final String UPDATE_WAGONER_STATE = "UPDATE WAGONER SET STATE=? WHERE WURMID=?";
  private static final String UPDATE_WAGONER_DELIVERY = "UPDATE WAGONER SET DELIVERY_ID=?, STATE=? WHERE WURMID=?";
  private static final String UPDATE_WAGONER_LAST_ID = "UPDATE WAGONER SET LAST_WAYSTONE_ID=? WHERE WURMID=?";
  private static final String UPDATE_WAGONER_GOAL_ID = "UPDATE WAGONER SET GOAL_WAYSTONE_ID=? WHERE WURMID=?";
  private static final String UPDATE_WAGONER_CHAT_OPTIONS = "UPDATE WAGONER SET CHAT=? WHERE WURMID=?";
  private static final String DELETE_WAGONER = "DELETE FROM WAGONER WHERE WURMID=?";
  private static final String GET_ALL_WAGONERS = "SELECT * FROM WAGONER";
  private static final Map<Long, Wagoner> wagoners = new ConcurrentHashMap();
  private long wagonerId;
  private byte state;
  private long ownerId;
  private long contractId;
  private long homeWaystoneId;
  private int homeVillageId;
  private long wagonId;
  private long restingPlaceId;
  private long chairId;
  private long tentId;
  private long bedId;
  private long deliveryId;
  private float wagonPosX;
  private float wagonPosY;
  private boolean wagonOnSurface;
  private float campRot;
  private long lastWaystoneId = -10L;
  private long goalWaystoneId = -10L;
  private List<Route> path = null;
  private LinkedList<Item> currentCatseyes = null;
  private boolean updateCatseyes = false;
  private LinkedList<Item> catseyesCollecting = null;
  private LinkedList<Item> catseyesDelivering = null;
  private LinkedList<Item> catseyesReturning = null;
  private Item wagon = null;
  private Creature creature = null;
  private boolean forceStateChange = false;
  private byte forcedNewState = 0;
  private final Random rnd;
  private long lazy = 0L;
  private long lastCheck = 0L;
  private byte subState = 0;
  private int notMoving = 0;
  private int lastDir = -1;
  private int tilex = 0;
  private int tiley = 0;
  private int tileCount = 0;
  private byte speechChattyness = 0;
  private boolean speechContextFood = false;
  private boolean speechContextWork = false;
  private boolean speechContextSleep = false;
  private boolean speechContextRandom = false;
  private byte speechStyle = 0;
  private int randomness = 1;
  private long chatDelay = 0L;
  private long lastChat = 0L;
  
  public Wagoner(long wagonerId, byte state, long ownerId, long contractId, long homeWaystoneId, int homeVillageId, long wagonId, long restingPlaceId, long chairId, long tentId, long bedId, long deliveryId, float wagonPosX, float wagonPosY, boolean wagonOnSurface, float campRot, long lastWaystoneId, long goalWaystoneId, byte speachChatty, boolean speachFood, boolean speachWork, boolean speachSleep, boolean speachRandom, byte speachType)
  {
    this.wagonerId = wagonerId;
    this.state = state;
    this.ownerId = ownerId;
    this.contractId = contractId;
    this.homeWaystoneId = homeWaystoneId;
    this.homeVillageId = homeVillageId;
    this.wagonId = wagonId;
    this.restingPlaceId = restingPlaceId;
    this.chairId = chairId;
    this.tentId = tentId;
    this.bedId = bedId;
    this.deliveryId = deliveryId;
    
    this.wagonPosX = wagonPosX;
    this.wagonPosY = wagonPosY;
    this.wagonOnSurface = wagonOnSurface;
    this.campRot = campRot;
    
    this.lastWaystoneId = lastWaystoneId;
    this.goalWaystoneId = goalWaystoneId;
    
    this.rnd = new Random(wagonerId);
    this.speechChattyness = speachChatty;
    this.speechContextFood = speachFood;
    this.speechContextWork = speachWork;
    this.speechContextSleep = speachSleep;
    this.speechContextRandom = speachRandom;
    this.speechStyle = speachType;
    this.randomness = (this.rnd.nextInt(5) + 5);
    try
    {
      this.wagon = Items.getItem(this.wagonId);
      this.wagon.setWagonerWagon(true);
      this.wagon.setDamage(0.0F);
      addWagoner(this);
    }
    catch (NoSuchItemException e)
    {
      logger.log(Level.WARNING, "Wagoner wagon (" + this.wagonId + ") missing! " + e.getMessage(), e);
    }
    if (this.deliveryId != -10L) {
      grabDeliveryCatseyes(this.deliveryId);
    }
    this.lazy = (60000L + 3000L * this.rnd.nextInt(60));
    setChatGap();
  }
  
  public long getWurmId()
  {
    return this.wagonerId;
  }
  
  public byte getState()
  {
    return this.state;
  }
  
  public String getStateName()
  {
    return getStateName(this.state);
  }
  
  public boolean isIdle()
  {
    switch (this.state)
    {
    case 0: 
    case 1: 
    case 2: 
    case 3: 
      return true;
    }
    return false;
  }
  
  public long getOwnerId()
  {
    return this.ownerId;
  }
  
  public void setOwnerId(long newOwnerId)
  {
    this.ownerId = newOwnerId;
  }
  
  public long getContractId()
  {
    return this.contractId;
  }
  
  public long getHomeWaystoneId()
  {
    return this.homeWaystoneId;
  }
  
  public int getVillageId()
  {
    return this.homeVillageId;
  }
  
  public long getWagonId()
  {
    return this.wagonId;
  }
  
  public Item getWagon()
  {
    return this.wagon;
  }
  
  public long getRestingPlaceId()
  {
    return this.restingPlaceId;
  }
  
  public long getChairId()
  {
    return this.chairId;
  }
  
  public long getTentId()
  {
    return this.tentId;
  }
  
  public long getBedId()
  {
    return this.bedId;
  }
  
  public long getDeliveryId()
  {
    return this.deliveryId;
  }
  
  public float getWagonPosX()
  {
    return this.wagonPosX;
  }
  
  public float getWagonPosY()
  {
    return this.wagonPosY;
  }
  
  public boolean getWagonOnSurface()
  {
    return this.wagonOnSurface;
  }
  
  public float getCampRot()
  {
    return this.campRot;
  }
  
  public long getLastWaystoneId()
  {
    return this.lastWaystoneId;
  }
  
  public void setLastWaystoneId(long newLastWaystoneId)
  {
    this.lastWaystoneId = newLastWaystoneId;
    dbUpdateLastWaystoneId(this.wagonerId, this.lastWaystoneId);
  }
  
  public long getGoalWaystoneId()
  {
    return this.goalWaystoneId;
  }
  
  public void setGoalWaystoneId(long newGoalWaystoneId)
  {
    this.goalWaystoneId = newGoalWaystoneId;
    dbUpdateGoalWaystoneId(this.wagonerId, this.goalWaystoneId);
  }
  
  public void calculateRoute(long newLastWaystoneId, long newGoalWaystoneId)
  {
    setGoalWaystoneId(newGoalWaystoneId);
    calculateRoute(newLastWaystoneId);
  }
  
  public void calculateRoute(long newLastWaystoneId)
  {
    setLastWaystoneId(newLastWaystoneId);
    calculateRoute();
  }
  
  public void calculateRoute()
  {
    if (this.lastWaystoneId == this.goalWaystoneId)
    {
      this.path = null;
    }
    else
    {
      this.path = PathToCalculate.getRoute(this.lastWaystoneId, this.goalWaystoneId);
      this.updateCatseyes = hasPath();
    }
  }
  
  public LinkedList<Item> getCurrentCatseyes()
  {
    return this.currentCatseyes;
  }
  
  public boolean maybeUpdateCatseyes()
  {
    if (this.path == null)
    {
      this.updateCatseyes = false;
      return false;
    }
    if (this.updateCatseyes)
    {
      LinkedList<Item> catseyes = new LinkedList();
      while (!this.path.isEmpty())
      {
        Route actualRoute = (Route)this.path.remove(0);
        catseyes.addAll(actualRoute.getCatseyesListCopy());
        
        catseyes.add(actualRoute.getEndNode().getWaystone());
      }
      this.currentCatseyes = catseyes;
      this.updateCatseyes = false;
      return true;
    }
    return false;
  }
  
  public boolean updateCatseyes(Item marker)
  {
    this.path = PathToCalculate.getRoute(this.lastWaystoneId, this.goalWaystoneId);
    this.currentCatseyes = new LinkedList();
    while (!this.path.isEmpty())
    {
      Route actualRoute = (Route)this.path.remove(0);
      this.currentCatseyes.addAll(actualRoute.getCatseyesListCopy());
      
      this.currentCatseyes.add(actualRoute.getEndNode().getWaystone());
    }
    if (this.currentCatseyes.contains(marker)) {
      while (this.currentCatseyes.getFirst() != marker) {
        this.currentCatseyes.removeFirst();
      }
    }
    return false;
    this.updateCatseyes = false;
    return true;
  }
  
  @Nullable
  public List<Route> getPath()
  {
    return this.path;
  }
  
  public boolean hasPath()
  {
    return (this.path != null) && (!this.path.isEmpty());
  }
  
  public void remove()
  {
    removeWagoner(this);
  }
  
  public Creature getCreature()
  {
    return this.creature;
  }
  
  public void setCreature(Creature creature)
  {
    this.creature = creature;
  }
  
  public String getName()
  {
    if (this.creature == null) {
      return "Unknown";
    }
    return this.creature.getName();
  }
  
  public void updateState(byte newState)
  {
    if (this.state != newState)
    {
      this.state = newState;
      dbUpdateWagonerState(this.wagonerId, this.state);
      this.forceStateChange = false;
    }
    if (newState == 6) {
      this.catseyesCollecting = new LinkedList();
    }
    if (newState == 8) {
      this.catseyesDelivering = new LinkedList();
    }
    if ((isIdle()) && (this.homeVillageId == -1))
    {
      removeWagonerCamp();
      
      Delivery.rejectWaitingForAccept(this.wagonerId);
      Delivery.clrWagonerQueue(this.wagonerId);
      remove();
    }
  }
  
  public boolean updateDeliveryId(long newDeliveryId)
  {
    if (newDeliveryId != -10L) {
      if (!grabDeliveryCatseyes(newDeliveryId))
      {
        this.catseyesCollecting = null;
        this.catseyesDelivering = null;
        this.catseyesReturning = null;
        
        return false;
      }
    }
    this.deliveryId = newDeliveryId;
    if (newDeliveryId == -10L)
    {
      this.state = 0;
      this.catseyesCollecting = null;
      this.catseyesDelivering = null;
      this.catseyesReturning = null;
    }
    else
    {
      this.state = 4;
    }
    dbUpdateWagonerDelivery(this.wagonerId, this.deliveryId, this.state);
    this.forceStateChange = false;
    return true;
  }
  
  public boolean grabDeliveryCatseyes(long deliveryId)
  {
    Delivery delivery = Delivery.getDelivery(deliveryId);
    if (delivery == null) {
      return false;
    }
    this.catseyesCollecting = new LinkedList();
    this.catseyesDelivering = new LinkedList();
    this.catseyesReturning = new LinkedList();
    switch (this.state)
    {
    case 0: 
    case 4: 
    case 5: 
    case 11: 
      List<Route> pathCollecting = PathToCalculate.getRoute(this.homeWaystoneId, delivery.getCollectionWaystoneId());
      if ((pathCollecting == null) || (pathCollecting.isEmpty())) {
        return false;
      }
      while (!pathCollecting.isEmpty())
      {
        Route actualRoute = (Route)pathCollecting.remove(0);
        this.catseyesCollecting.addAll(actualRoute.getCatseyesListCopy());
        
        this.catseyesCollecting.add(actualRoute.getEndNode().getWaystone());
      }
    case 6: 
    case 7: 
    case 12: 
      List<Route> pathDelivering = PathToCalculate.getRoute(delivery.getCollectionWaystoneId(), delivery.getDeliveryWaystoneId());
      if ((pathDelivering == null) || (pathDelivering.isEmpty())) {
        return false;
      }
      while (!pathDelivering.isEmpty())
      {
        Route actualRoute = (Route)pathDelivering.remove(0);
        this.catseyesDelivering.addAll(actualRoute.getCatseyesListCopy());
        
        this.catseyesDelivering.add(actualRoute.getEndNode().getWaystone());
      }
    case 8: 
    case 9: 
    case 13: 
      List<Route> pathReturning = PathToCalculate.getRoute(delivery.getDeliveryWaystoneId(), this.homeWaystoneId);
      if ((pathReturning == null) || (pathReturning.isEmpty())) {
        return false;
      }
      while (!pathReturning.isEmpty())
      {
        Route actualRoute = (Route)pathReturning.remove(0);
        this.catseyesReturning.addAll(actualRoute.getCatseyesListCopy());
        
        this.catseyesReturning.add(actualRoute.getEndNode().getWaystone());
      }
      return true;
    }
    return false;
  }
  
  public boolean markerOnDeliveryRoute(Item marker)
  {
    if (this.deliveryId == -10L) {
      return false;
    }
    if ((this.catseyesReturning == null) || (this.catseyesReturning.isEmpty())) {
      if (!grabDeliveryCatseyes(this.deliveryId)) {
        return false;
      }
    }
    return (this.catseyesCollecting.contains(marker)) || 
      (this.catseyesDelivering.contains(marker)) || 
      (this.catseyesReturning.contains(marker));
  }
  
  public void forceStateChange(byte newState)
  {
    this.forcedNewState = newState;
    this.forceStateChange = true;
  }
  
  public boolean isForcedState()
  {
    return this.forceStateChange;
  }
  
  public byte getForcedState()
  {
    return this.forcedNewState;
  }
  
  public int getQueueLength()
  {
    return Delivery.getQueueLength(this.wagonerId);
  }
  
  public void clrTileCount()
  {
    this.tileCount = 0;
  }
  
  public void setTile(int tilex, int tiley)
  {
    this.tilex = tilex;
    this.tiley = tiley;
  }
  
  public boolean moved(int tilex, int tiley)
  {
    if ((tilex != this.tilex) || (tiley != this.tiley))
    {
      setTile(tilex, tiley);
      this.tileCount += 1;
      return true;
    }
    return false;
  }
  
  public void clrVillage()
  {
    logger.log(Level.WARNING, getName() + " (" + this.wagonerId + ") removed from village id: " + this.homeVillageId + ".", new Exception());
    if (this.homeVillageId != -1)
    {
      this.homeVillageId = -1;
      Delivery.rejectWaitingForAccept(this.wagonerId);
      if (isIdle())
      {
        removeWagonerCamp();
        Delivery.clrWagonerQueue(this.wagonerId);
        remove();
      }
    }
  }
  
  private void removeWagonerCamp()
  {
    try
    {
      Creatures.getInstance().getCreature(this.wagonerId).destroy();
      this.creature = null;
    }
    catch (NoSuchCreatureException localNoSuchCreatureException) {}
    Items.destroyItem(this.wagonId, true);
    Items.destroyItem(this.chairId, true);
    Items.destroyItem(this.bedId, true);
    Items.destroyItem(this.tentId, true);
    Items.destroyItem(this.restingPlaceId, true);
    try
    {
      Item homeWaystone = Items.getItem(this.homeWaystoneId);
      homeWaystone.setData(-1L);
    }
    catch (NoSuchItemException e)
    {
      logger.log(Level.WARNING, "Home Waystone is missing " + e.getMessage(), e);
    }
    try
    {
      Item contract = Items.getItem(this.contractId);
      contract.setData(-1L);
      
      contract.setDescription("");
    }
    catch (NoSuchItemException e)
    {
      logger.log(Level.WARNING, "Wagoner Contract is missing " + e.getMessage(), e);
    }
  }
  
  public void say(Wagoner.Speech speech)
  {
    say(speech, null);
  }
  
  public void say(Wagoner.Speech speech, @Nullable Delivery delivery)
  {
    this.lastChat = WurmCalendar.getCurrentTime();
    boolean inContext = true;
    switch (speech.getContext())
    {
    case 4: 
      inContext = this.speechContextFood;
      break;
    case 8: 
      inContext = this.speechContextWork;
      break;
    case 16: 
      inContext = this.speechContextSleep;
      break;
    case 1: 
      inContext = Servers.isThisATestServer();
      break;
    case 2: 
      inContext = true;
      break;
    default: 
      inContext = false;
    }
    if ((inContext) && (sayIt())) {
      switch (speech.getParams())
      {
      case 0: 
        this.creature.say(speech.getMsg(this.speechStyle), speech.isEmote(this.speechStyle));
        break;
      case 1: 
        String sub1 = delivery.getCrates() + " crates";
        String msg = String.format(speech.getMsg(this.speechStyle), new Object[] { sub1 });
        this.creature.say(msg, speech.isEmote(this.speechStyle));
        break;
      case 2: 
        String sub1 = delivery.getCrates() + " crates";
        String sub2 = delivery.getReceiverName();
        String msg = String.format(speech.getMsg(this.speechStyle), new Object[] { sub1, sub2 });
        this.creature.say(msg, speech.isEmote(this.speechStyle));
        break;
      }
    }
  }
  
  public void sayRandom()
  {
    long now = WurmCalendar.getCurrentTime();
    if (this.lastChat + this.chatDelay > now) {
      return;
    }
    if ((this.speechContextRandom) && (this.tileCount % 10 == 5) && (sayIt()) && (this.rnd.nextInt(4) == 0))
    {
      this.lastChat = now;
      
      long time = now + 300L;
      int mins = (int)(time % 3600L / 60L);
      if (mins <= 10)
      {
        int hour = (int)(time % 86400L / 3600L);
        for (Wagoner.TimedSpeech ts : Wagoner.TimedSpeech.values()) {
          if (ts.getHour() == hour)
          {
            this.creature.say(ts.getMsg(this.speechStyle, this.creature.isOnSurface()), ts.isEmote(this.speechStyle, this.creature.isOnSurface()));
            return;
          }
        }
      }
      Wagoner.RandomSpeech rs = Wagoner.RandomSpeech.values()[this.rnd.nextInt(Wagoner.RandomSpeech.values().length)];
      this.creature.say(rs.getMsg(this.speechStyle, this.creature.isOnSurface()), rs.isEmote(this.speechStyle, this.creature.isOnSurface()));
    }
  }
  
  public void sayStuck()
  {
    long now = WurmCalendar.getCurrentTime();
    if (this.lastChat + this.chatDelay > now) {
      return;
    }
    this.lastChat = now;
    Wagoner.RandomSpeech rs = Wagoner.RandomSpeech.values()[this.rnd.nextInt(Wagoner.RandomSpeech.values().length)];
    this.creature.say(rs.getMsg(this.speechStyle, this.creature.isOnSurface()), rs.isEmote(this.speechStyle, this.creature.isOnSurface()));
  }
  
  private boolean sayIt()
  {
    switch (this.speechChattyness)
    {
    case 0: 
      return this.rnd.nextInt(3) == 0;
    case 1: 
      return false;
    case 2: 
      return true;
    case 3: 
      return this.rnd.nextInt(this.randomness) < 4;
    }
    return true;
  }
  
  public long getLazy()
  {
    return this.lazy;
  }
  
  public long getLastCheck()
  {
    return this.lastCheck;
  }
  
  public void setLastCheck(long now)
  {
    this.lastCheck = now;
  }
  
  public byte getSubState()
  {
    return this.subState;
  }
  
  public void incSubState()
  {
    this.subState = ((byte)(this.subState + 1));
  }
  
  public void clrSubState()
  {
    this.subState = 0;
  }
  
  public void setSubState(byte newSubState)
  {
    this.subState = newSubState;
  }
  
  public int getNotMoving()
  {
    return this.notMoving;
  }
  
  public void incNotMoving()
  {
    this.notMoving += 1;
  }
  
  public void clrNotMoving()
  {
    this.notMoving = 0;
  }
  
  public int getLastDir()
  {
    return this.lastDir;
  }
  
  public void setLastDir(int newDir)
  {
    this.lastDir = newDir;
  }
  
  public byte getSpeechChattyness()
  {
    return this.speechChattyness;
  }
  
  public boolean isSpeachContextFood()
  {
    return this.speechContextFood;
  }
  
  public boolean isSpeachContextWork()
  {
    return this.speechContextWork;
  }
  
  public boolean isSpeachContextSleep()
  {
    return this.speechContextSleep;
  }
  
  public boolean isSpeachContextRandom()
  {
    return this.speechContextRandom;
  }
  
  public byte getSpeechStyle()
  {
    return this.speechStyle;
  }
  
  public void setSpeechOptions(byte newChattyness, boolean newContextFood, boolean newContextWork, boolean newContextSleep, boolean newContextRandom, byte newChatType)
  {
    this.speechChattyness = newChattyness;
    this.speechContextFood = newContextFood;
    this.speechContextWork = newContextWork;
    this.speechContextSleep = newContextSleep;
    this.speechContextRandom = newContextRandom;
    this.speechStyle = newChatType;
    
    setChatGap();
    
    byte newSpeachOptions = (byte)(newChattyness + (newContextFood ? 0 : 4) + (newContextWork ? 0 : 8) + (newContextSleep ? 0 : 16) + (newContextRandom ? 32 : 0) + (newChatType << 6));
    
    dbUpdateWagonerChatOptions(this.wagonerId, newSpeachOptions);
  }
  
  private void setChatGap()
  {
    switch (this.speechChattyness)
    {
    case 0: 
      this.chatDelay = 300L;
      break;
    case 1: 
      this.chatDelay = 3600L;
      break;
    case 2: 
      this.chatDelay = 60L;
      break;
    case 3: 
      this.chatDelay = (60L + this.rnd.nextInt(300));
    }
  }
  
  public static String getStateName(byte state)
  {
    switch (state)
    {
    case 0: 
      return "Idle";
    case 1: 
    case 2: 
    case 3: 
      return "Sleeping";
    case 4: 
      return "Getting ready";
    case 5: 
    case 11: 
      return "Driving to collection point";
    case 6: 
      return "Loading";
    case 7: 
    case 12: 
      return "Delivering";
    case 8: 
      return "Unloading";
    case 9: 
    case 13: 
      return "Going home";
    case 10: 
      return "Parking";
    case 14: 
      return "waiting";
    case 15: 
      return "driving";
    }
    return "";
  }
  
  public static final Wagoner getWagoner(long wurmId)
  {
    return (Wagoner)wagoners.get(Long.valueOf(wurmId));
  }
  
  public static final Map<Long, Wagoner> getWagoners()
  {
    return wagoners;
  }
  
  public static final Wagoner[] getAllWagoners()
  {
    return (Wagoner[])wagoners.values().toArray(new Wagoner[wagoners.size()]);
  }
  
  private static final void removeWagoner(Wagoner wagoner)
  {
    wagoners.remove(Long.valueOf(wagoner.getWurmId()));
    dbRemoveWagoner(wagoner.getWurmId());
  }
  
  private static final void addWagoner(Wagoner wagoner)
  {
    wagoners.put(Long.valueOf(wagoner.getWurmId()), wagoner);
  }
  
  public static final void addWagoner(long wurmId, long ownerId, long contractId, long homeWaystoneId, int homeVillageId, Item wagon, long restingPlaceId, long chairId, long tentId, long bedId, byte speachChatty, boolean speachFood, boolean speachWork, boolean speachSleep, boolean speachRandom, byte speachType)
  {
    byte state = 0;
    long deliveryId = -10L;
    long lastWaystoneId = -10L;
    long goalWaystoneId = -10L;
    
    Wagoner wagoner = new Wagoner(wurmId, (byte)0, ownerId, contractId, homeWaystoneId, homeVillageId, wagon.getWurmId(), restingPlaceId, chairId, tentId, bedId, -10L, wagon.getPosX(), wagon.getPosY(), wagon.isOnSurface(), wagon.getRotation(), -10L, -10L, speachChatty, speachFood, speachWork, speachSleep, speachRandom, speachType);
    
    dbCreateWagoner(wagoner);
  }
  
  public static final String getWagonerNameFrom(long waystoneId)
  {
    for (Wagoner wagoner : wagoners.values()) {
      if (wagoner.getHomeWaystoneId() == waystoneId) {
        return wagoner.getName() + " camp";
      }
    }
    return "";
  }
  
  public static boolean isOnActiveDeliveryRoute(Item marker)
  {
    for (Wagoner wagoner : wagoners.values()) {
      if (wagoner.markerOnDeliveryRoute(marker)) {
        return true;
      }
    }
    return false;
  }
  
  public static final void dbLoadAllWagoners()
  {
    logger.log(Level.INFO, "Loading all wagoners.");
    long start = System.nanoTime();
    Connection dbcon = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    try
    {
      dbcon = DbConnector.getCreatureDbCon();
      ps = dbcon.prepareStatement("SELECT * FROM WAGONER");
      rs = ps.executeQuery();
      while (rs.next())
      {
        long wurmId = rs.getLong("WURMID");
        byte state = rs.getByte("STATE");
        long ownerId = rs.getLong("OWNERID");
        long contractId = rs.getLong("CONTRACT_ID");
        long homeWaystoneId = rs.getLong("HOME_WAYSTONE_ID");
        int homeVillageId = rs.getInt("HOME_VILLAGE_ID");
        long wagonId = rs.getLong("WAGON_ID");
        long restingPlaceId = rs.getLong("RESTING_PLACE_ID");
        long chairId = rs.getLong("CHAIR_ID");
        long tentId = rs.getLong("TENT_ID");
        long bedId = rs.getLong("BED_ID");
        long deliveryId = rs.getLong("DELIVERY_ID");
        float wagonPosX = rs.getFloat("WAGON_POSX");
        float wagonPosY = rs.getFloat("WAGON_POSY");
        boolean wagonOnSurface = rs.getBoolean("WAGON_ON_SURFACE");
        float campRot = rs.getFloat("CAMP_ROT");
        long lastWaystoneId = rs.getLong("LAST_WAYSTONE_ID");
        long goalWaystoneId = rs.getLong("GOAL_WAYSTONE_ID");
        byte chat = rs.getByte("CHAT");
        byte speachChatty = (byte)(chat & 0x3);
        boolean speachFood = (chat & 0x4) != 4;
        boolean speachWork = (chat & 0x8) != 8;
        boolean speachSleep = (chat & 0x10) != 16;
        boolean speachRandom = (chat & 0x20) == 32;
        byte speachType = (byte)(chat >> 6 & 0x3);
        new Wagoner(wurmId, state, ownerId, contractId, homeWaystoneId, homeVillageId, wagonId, restingPlaceId, chairId, tentId, bedId, deliveryId, wagonPosX, wagonPosY, wagonOnSurface, campRot, lastWaystoneId, goalWaystoneId, speachChatty, speachFood, speachWork, speachSleep, speachRandom, speachType);
      }
    }
    catch (SQLException sqex)
    {
      long end;
      logger.log(Level.WARNING, "Failed to load all wagoners: " + sqex.getMessage(), sqex);
    }
    finally
    {
      long end;
      DbUtilities.closeDatabaseObjects(ps, rs);
      DbConnector.returnConnection(dbcon);
      long end = System.nanoTime();
      logger.log(Level.INFO, "Loaded " + wagoners.size() + " wagoners. That took " + (float)(end - start) / 1000000.0F + " ms.");
    }
  }
  
  private static void dbCreateWagoner(Wagoner wag)
  {
    Connection dbcon = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    try
    {
      dbcon = DbConnector.getCreatureDbCon();
      ps = dbcon.prepareStatement("INSERT INTO WAGONER (WURMID,STATE,OWNERID,CONTRACT_ID,HOME_WAYSTONE_ID,HOME_VILLAGE_ID,WAGON_ID,RESTING_PLACE_ID,CHAIR_ID,TENT_ID,BED_ID,DELIVERY_ID,WAGON_POSX,WAGON_POSY,WAGON_ON_SURFACE,CAMP_ROT,LAST_WAYSTONE_ID,GOAL_WAYSTONE_ID) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
      ps.setLong(1, wag.getWurmId());
      ps.setByte(2, wag.getState());
      ps.setLong(3, wag.getOwnerId());
      ps.setLong(4, wag.getContractId());
      ps.setLong(5, wag.getHomeWaystoneId());
      ps.setInt(6, wag.getVillageId());
      ps.setLong(7, wag.getWagonId());
      ps.setLong(8, wag.getRestingPlaceId());
      ps.setLong(9, wag.getChairId());
      ps.setLong(10, wag.getTentId());
      ps.setLong(11, wag.getBedId());
      ps.setLong(12, wag.getDeliveryId());
      ps.setFloat(13, wag.getWagonPosX());
      ps.setFloat(14, wag.getWagonPosY());
      ps.setBoolean(15, wag.getWagonOnSurface());
      ps.setFloat(16, wag.getCampRot());
      ps.setLong(17, wag.getLastWaystoneId());
      ps.setLong(18, wag.getGoalWaystoneId());
      ps.executeUpdate();
    }
    catch (SQLException ex)
    {
      logger.log(Level.WARNING, "Failed to create wagoner " + wag.getWurmId() + " in wagoner table.", ex);
    }
    finally
    {
      DbUtilities.closeDatabaseObjects(ps, rs);
      DbConnector.returnConnection(dbcon);
    }
  }
  
  private static void dbRemoveWagoner(long wagonerId)
  {
    Connection dbcon = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    try
    {
      dbcon = DbConnector.getCreatureDbCon();
      ps = dbcon.prepareStatement("DELETE FROM WAGONER WHERE WURMID=?");
      ps.setLong(1, wagonerId);
      ps.executeUpdate();
    }
    catch (SQLException ex)
    {
      logger.log(Level.WARNING, "Failed to remove wagoner " + wagonerId + " from wagoner table.", ex);
    }
    finally
    {
      DbUtilities.closeDatabaseObjects(ps, rs);
      DbConnector.returnConnection(dbcon);
    }
  }
  
  private static void dbUpdateWagonerState(long wagonerId, byte state)
  {
    Connection dbcon = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    try
    {
      dbcon = DbConnector.getCreatureDbCon();
      ps = dbcon.prepareStatement("UPDATE WAGONER SET STATE=? WHERE WURMID=?");
      ps.setByte(1, state);
      ps.setLong(2, wagonerId);
      ps.executeUpdate();
    }
    catch (SQLException ex)
    {
      logger.log(Level.WARNING, "Failed to update wagoner " + wagonerId + " to state " + state + " in wagoner table.", ex);
    }
    finally
    {
      DbUtilities.closeDatabaseObjects(ps, rs);
      DbConnector.returnConnection(dbcon);
    }
  }
  
  private static void dbUpdateWagonerDelivery(long wagonerId, long deliveryId, byte state)
  {
    Connection dbcon = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    try
    {
      dbcon = DbConnector.getCreatureDbCon();
      ps = dbcon.prepareStatement("UPDATE WAGONER SET DELIVERY_ID=?, STATE=? WHERE WURMID=?");
      ps.setLong(1, deliveryId);
      ps.setByte(2, state);
      ps.setLong(3, wagonerId);
      ps.executeUpdate();
    }
    catch (SQLException ex)
    {
      logger.log(Level.WARNING, "Failed to update wagoner " + wagonerId + " delivery " + deliveryId + " and state " + state + " in wagoner table.", ex);
    }
    finally
    {
      DbUtilities.closeDatabaseObjects(ps, rs);
      DbConnector.returnConnection(dbcon);
    }
  }
  
  private static void dbUpdateLastWaystoneId(long wagonerId, long lastWaystoneId)
  {
    Connection dbcon = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    try
    {
      dbcon = DbConnector.getCreatureDbCon();
      ps = dbcon.prepareStatement("UPDATE WAGONER SET LAST_WAYSTONE_ID=? WHERE WURMID=?");
      ps.setLong(1, lastWaystoneId);
      ps.setLong(2, wagonerId);
      ps.executeUpdate();
    }
    catch (SQLException ex)
    {
      logger.log(Level.WARNING, "Failed to update wagoner " + wagonerId + " last waystone to " + lastWaystoneId + ".", ex);
    }
    finally
    {
      DbUtilities.closeDatabaseObjects(ps, rs);
      DbConnector.returnConnection(dbcon);
    }
  }
  
  private static void dbUpdateGoalWaystoneId(long wagonerId, long goalWaystoneId)
  {
    Connection dbcon = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    try
    {
      dbcon = DbConnector.getCreatureDbCon();
      ps = dbcon.prepareStatement("UPDATE WAGONER SET GOAL_WAYSTONE_ID=? WHERE WURMID=?");
      ps.setLong(1, goalWaystoneId);
      ps.setLong(2, wagonerId);
      ps.executeUpdate();
    }
    catch (SQLException ex)
    {
      logger.log(Level.WARNING, "Failed to update wagoner " + wagonerId + " goal waystone to " + goalWaystoneId + ".", ex);
    }
    finally
    {
      DbUtilities.closeDatabaseObjects(ps, rs);
      DbConnector.returnConnection(dbcon);
    }
  }
  
  private static void dbUpdateWagonerChatOptions(long wagonerId, byte chatOptions)
  {
    Connection dbcon = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    try
    {
      dbcon = DbConnector.getCreatureDbCon();
      ps = dbcon.prepareStatement("UPDATE WAGONER SET CHAT=? WHERE WURMID=?");
      ps.setByte(1, chatOptions);
      ps.setLong(2, wagonerId);
      ps.executeUpdate();
    }
    catch (SQLException ex)
    {
      logger.log(Level.WARNING, "Failed to update wagoner " + wagonerId + " chat options to " + chatOptions + ".", ex);
    }
    finally
    {
      DbUtilities.closeDatabaseObjects(ps, rs);
      DbConnector.returnConnection(dbcon);
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\creatures\Wagoner.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */