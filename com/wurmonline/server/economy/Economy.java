package com.wurmonline.server.economy;

import com.wurmonline.server.FailedException;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.Server;
import com.wurmonline.server.ServerEntry;
import com.wurmonline.server.Servers;
import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.Creatures;
import com.wurmonline.server.creatures.NoSuchCreatureException;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemFactory;
import com.wurmonline.server.items.NoSuchTemplateException;
import com.wurmonline.server.zones.VolaTile;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.concurrent.GuardedBy;

public abstract class Economy
  implements MonetaryConstants, MiscConstants, TimeConstants
{
  static long goldCoins;
  static long lastPolledTraders;
  static long copperCoins;
  static long silverCoins;
  static long ironCoins;
  private static final LinkedList<Item> goldOnes = new LinkedList();
  private static final LinkedList<Item> goldFives = new LinkedList();
  private static final LinkedList<Item> goldTwentys = new LinkedList();
  private static final LinkedList<Item> silverOnes = new LinkedList();
  private static final LinkedList<Item> silverFives = new LinkedList();
  private static final LinkedList<Item> silverTwentys = new LinkedList();
  private static final LinkedList<Item> copperOnes = new LinkedList();
  private static final LinkedList<Item> copperFives = new LinkedList();
  private static final LinkedList<Item> copperTwentys = new LinkedList();
  private static final LinkedList<Item> ironOnes = new LinkedList();
  private static final LinkedList<Item> ironFives = new LinkedList();
  private static final LinkedList<Item> ironTwentys = new LinkedList();
  private static final Logger logger = Logger.getLogger(Economy.class.getName());
  private static final Logger moneylogger = Logger.getLogger("Money");
  final int id;
  private static final Map<Integer, SupplyDemand> supplyDemand = new HashMap();
  @GuardedBy("SHOPS_RW_LOCK")
  private static final Map<Long, Shop> shops = new HashMap();
  private static final ReentrantReadWriteLock SHOPS_RW_LOCK = new ReentrantReadWriteLock();
  private static Economy economy;
  private static final int minTraderDistance = 64;
  
  public static Economy getEconomy()
  {
    if (economy == null)
    {
      long start = System.nanoTime();
      try
      {
        economy = new DbEconomy(Servers.localServer.id);
      }
      catch (IOException iox)
      {
        logger.log(Level.WARNING, "Failed to create economy: " + iox.getMessage(), iox);
        Server.getInstance().shutDown();
      }
      logger.log(Level.INFO, "Loading economy took " + (float)(System.nanoTime() - start) / 1000000.0F + " ms.");
    }
    return economy;
  }
  
  Economy(int aEconomy)
    throws IOException
  {
    goldCoins = 0L;
    copperCoins = 0L;
    silverCoins = 0L;
    ironCoins = 0L;
    this.id = aEconomy;
    initialize();
  }
  
  public long getGold()
  {
    return goldCoins;
  }
  
  public long getSilver()
  {
    return silverCoins;
  }
  
  public long getCopper()
  {
    return copperCoins;
  }
  
  public long getIron()
  {
    return ironCoins;
  }
  
  public static final int getValueFor(int coinType)
  {
    switch (coinType)
    {
    case 50: 
      return 100;
    case 54: 
      return 500;
    case 58: 
      return 2000;
    case 51: 
      return 1;
    case 55: 
      return 5;
    case 59: 
      return 20;
    case 52: 
      return 10000;
    case 56: 
      return 50000;
    case 60: 
      return 200000;
    case 53: 
      return 1000000;
    case 57: 
      return 5000000;
    case 61: 
      return 20000000;
    }
    return 0;
  }
  
  LinkedList<Item> getListForCointype(int type)
  {
    switch (type)
    {
    case 50: 
      return copperOnes;
    case 54: 
      return copperFives;
    case 58: 
      return copperTwentys;
    case 51: 
      return ironOnes;
    case 55: 
      return ironFives;
    case 59: 
      return ironTwentys;
    case 52: 
      return silverOnes;
    case 56: 
      return silverFives;
    case 60: 
      return silverTwentys;
    case 53: 
      return goldOnes;
    case 57: 
      return goldFives;
    case 61: 
      return goldTwentys;
    }
    logger.log(Level.WARNING, "Found no list for type " + type);
    return new LinkedList();
  }
  
  public void returnCoin(Item coin, String message)
  {
    returnCoin(coin, message, false);
  }
  
  public void returnCoin(Item coin, String message, boolean dontLog)
  {
    if (!dontLog) {
      transaction(coin.getWurmId(), coin.getOwnerId(), this.id, message, coin.getValue());
    }
    coin.setTradeWindow(null);
    coin.setOwner(-10L, false);
    coin.setLastOwnerId(-10L);
    coin.setZoneId(-10, true);
    coin.setParentId(-10L, true);
    coin.setRarity((byte)0);
    coin.setBanked(true);
    
    int templateid = coin.getTemplateId();
    List<Item> toAdd = getListForCointype(templateid);
    toAdd.add(coin);
  }
  
  public Item[] getCoinsFor(long value)
  {
    if (value > 0L) {
      try
      {
        if (value >= 1000000L) {
          return getGoldTwentyCoinsFor(value, new HashSet());
        }
        if (value >= 10000L) {
          return getSilverTwentyCoinsFor(value, new HashSet());
        }
        if (value >= 100L) {
          return getCopperTwentyCoinsFor(value, new HashSet());
        }
        return getIronTwentyCoinsFor(value, new HashSet());
      }
      catch (FailedException fe)
      {
        logger.log(Level.WARNING, "Failed to create coins: " + fe.getMessage(), fe);
      }
      catch (NoSuchTemplateException nst)
      {
        logger.log(Level.WARNING, "Failed to create coins: " + nst.getMessage(), nst);
      }
    }
    return new Item[0];
  }
  
  private Item[] getGoldTwentyCoinsFor(long value, Set<Item> items)
    throws FailedException, NoSuchTemplateException
  {
    if (value > 0L)
    {
      long num = value / 20000000L;
      if (items == null) {
        items = new HashSet();
      }
      for (long x = 0L; x < num; x += 1L)
      {
        Item coin = null;
        if (goldTwentys.size() > 0)
        {
          coin = (Item)goldTwentys.removeFirst();
        }
        else
        {
          coin = ItemFactory.createItem(61, Server.rand.nextFloat() * 100.0F, null);
          goldCoins += 20L;
          updateCreatedGold(goldCoins);
          logger.log(Level.INFO, "CREATING COIN GOLD20 " + coin.getWurmId(), new Exception());
        }
        items.add(coin);
        coin.setBanked(false);
        value -= 20000000L;
      }
    }
    return getGoldFiveCoinsFor(value, items);
  }
  
  public Shop[] getShops()
  {
    SHOPS_RW_LOCK.readLock().lock();
    try
    {
      return (Shop[])shops.values().toArray(new Shop[shops.size()]);
    }
    finally
    {
      SHOPS_RW_LOCK.readLock().unlock();
    }
  }
  
  public static Shop[] getTraders()
  {
    Map<Long, Shop> traders = new HashMap();
    SHOPS_RW_LOCK.readLock().lock();
    try
    {
      for (Object localObject1 = shops.values().iterator(); ((Iterator)localObject1).hasNext();)
      {
        Shop s = (Shop)((Iterator)localObject1).next();
        if ((!s.isPersonal()) && (s.getWurmId() != 0L)) {
          traders.put(Long.valueOf(s.getWurmId()), s);
        }
      }
      return (Shop[])traders.values().toArray(new Shop[traders.size()]);
    }
    finally
    {
      SHOPS_RW_LOCK.readLock().unlock();
    }
  }
  
  public long getShopMoney()
  {
    long toRet = 0L;
    Shop[] lShops = getShops();
    for (Shop lLShop : lShops) {
      if (lLShop.getMoney() > 0L) {
        toRet += lLShop.getMoney();
      }
    }
    return toRet;
  }
  
  public void pollTraderEarnings()
  {
    if (System.currentTimeMillis() - lastPolledTraders > 2419200000L)
    {
      resetEarnings();
      updateLastPolled();
      logger.log(Level.INFO, "Economy reset earnings.");
    }
  }
  
  private Item[] getGoldFiveCoinsFor(long value, Set<Item> items)
    throws FailedException, NoSuchTemplateException
  {
    if (value > 0L)
    {
      long num = value / 5000000L;
      if (items == null) {
        items = new HashSet();
      }
      for (long x = 0L; x < num; x += 1L)
      {
        Item coin = null;
        if (goldFives.size() > 0)
        {
          coin = (Item)goldFives.removeFirst();
        }
        else
        {
          coin = ItemFactory.createItem(57, Server.rand.nextFloat() * 100.0F, null);
          goldCoins += 5L;
          updateCreatedGold(goldCoins);
          logger.log(Level.INFO, "CREATING COIN GOLD5 " + coin.getWurmId(), new Exception());
        }
        items.add(coin);
        coin.setBanked(false);
        value -= 5000000L;
      }
    }
    return getGoldOneCoinsFor(value, items);
  }
  
  private Item[] getGoldOneCoinsFor(long value, Set<Item> items)
    throws FailedException, NoSuchTemplateException
  {
    if (value > 0L)
    {
      long num = value / 1000000L;
      if (items == null) {
        items = new HashSet();
      }
      for (long x = 0L; x < num; x += 1L)
      {
        Item coin = null;
        if (goldOnes.size() > 0)
        {
          coin = (Item)goldOnes.removeFirst();
        }
        else
        {
          coin = ItemFactory.createItem(53, Server.rand.nextFloat() * 100.0F, null);
          goldCoins += 1L;
          updateCreatedGold(goldCoins);
          
          logger.log(Level.INFO, "CREATING COIN GOLD1 " + coin.getWurmId(), new Exception());
        }
        items.add(coin);
        coin.setBanked(false);
        value -= 1000000L;
      }
    }
    return getSilverTwentyCoinsFor(value, items);
  }
  
  private Item[] getSilverTwentyCoinsFor(long value, Set<Item> items)
    throws FailedException, NoSuchTemplateException
  {
    if (value > 0L)
    {
      long num = value / 200000L;
      if (items == null) {
        items = new HashSet();
      }
      for (long x = 0L; x < num; x += 1L)
      {
        Item coin = null;
        if (silverTwentys.size() > 0)
        {
          coin = (Item)silverTwentys.removeFirst();
        }
        else
        {
          coin = ItemFactory.createItem(60, Server.rand.nextFloat() * 100.0F, null);
          silverCoins += 20L;
          updateCreatedSilver(silverCoins);
        }
        items.add(coin);
        coin.setBanked(false);
        value -= 200000L;
      }
    }
    return getSilverFiveCoinsFor(value, items);
  }
  
  private Item[] getSilverFiveCoinsFor(long value, Set<Item> items)
    throws FailedException, NoSuchTemplateException
  {
    if (value > 0L)
    {
      long num = value / 50000L;
      if (items == null) {
        items = new HashSet();
      }
      for (long x = 0L; x < num; x += 1L)
      {
        Item coin = null;
        if (silverFives.size() > 0)
        {
          coin = (Item)silverFives.removeFirst();
        }
        else
        {
          coin = ItemFactory.createItem(56, Server.rand.nextFloat() * 100.0F, null);
          silverCoins += 5L;
          updateCreatedSilver(silverCoins);
        }
        items.add(coin);
        coin.setBanked(false);
        value -= 50000L;
      }
    }
    return getSilverOneCoinsFor(value, items);
  }
  
  private Item[] getSilverOneCoinsFor(long value, Set<Item> items)
    throws FailedException, NoSuchTemplateException
  {
    if (value > 0L)
    {
      long num = value / 10000L;
      if (items == null) {
        items = new HashSet();
      }
      for (long x = 0L; x < num; x += 1L)
      {
        Item coin = null;
        if (silverOnes.size() > 0)
        {
          coin = (Item)silverOnes.removeFirst();
        }
        else
        {
          coin = ItemFactory.createItem(52, Server.rand.nextFloat() * 100.0F, null);
          silverCoins += 1L;
          updateCreatedSilver(silverCoins);
        }
        items.add(coin);
        coin.setBanked(false);
        value -= 10000L;
      }
    }
    return getCopperTwentyCoinsFor(value, items);
  }
  
  private Item[] getCopperTwentyCoinsFor(long value, Set<Item> items)
    throws FailedException, NoSuchTemplateException
  {
    if (value > 0L)
    {
      long num = value / 2000L;
      if (items == null) {
        items = new HashSet();
      }
      for (long x = 0L; x < num; x += 1L)
      {
        Item coin = null;
        if (copperTwentys.size() > 0)
        {
          coin = (Item)copperTwentys.removeFirst();
        }
        else
        {
          coin = ItemFactory.createItem(58, Server.rand.nextFloat() * 100.0F, null);
          copperCoins += 20L;
          updateCreatedCopper(copperCoins);
        }
        items.add(coin);
        coin.setBanked(false);
        value -= 2000L;
      }
    }
    return getCopperFiveCoinsFor(value, items);
  }
  
  private Item[] getCopperFiveCoinsFor(long value, Set<Item> items)
    throws FailedException, NoSuchTemplateException
  {
    if (value > 0L)
    {
      long num = value / 500L;
      if (items == null) {
        items = new HashSet();
      }
      for (long x = 0L; x < num; x += 1L)
      {
        Item coin = null;
        if (copperFives.size() > 0)
        {
          coin = (Item)copperFives.removeFirst();
        }
        else
        {
          coin = ItemFactory.createItem(54, Server.rand.nextFloat() * 100.0F, null);
          copperCoins += 5L;
          updateCreatedCopper(copperCoins);
        }
        items.add(coin);
        coin.setBanked(false);
        value -= 500L;
      }
    }
    return getCopperOneCoinsFor(value, items);
  }
  
  private Item[] getCopperOneCoinsFor(long value, Set<Item> items)
    throws FailedException, NoSuchTemplateException
  {
    if (value > 0L)
    {
      long num = value / 100L;
      if (items == null) {
        items = new HashSet();
      }
      for (long x = 0L; x < num; x += 1L)
      {
        Item coin = null;
        if (copperOnes.size() > 0)
        {
          coin = (Item)copperOnes.removeFirst();
        }
        else
        {
          coin = ItemFactory.createItem(50, Server.rand.nextFloat() * 100.0F, null);
          copperCoins += 1L;
          updateCreatedCopper(copperCoins);
        }
        items.add(coin);
        coin.setBanked(false);
        value -= 100L;
      }
    }
    return getIronTwentyCoinsFor(value, items);
  }
  
  private Item[] getIronTwentyCoinsFor(long value, Set<Item> items)
    throws FailedException, NoSuchTemplateException
  {
    if (value > 0L)
    {
      long num = value / 20L;
      if (items == null) {
        items = new HashSet();
      }
      for (long x = 0L; x < num; x += 1L)
      {
        Item coin = null;
        if (ironTwentys.size() > 0)
        {
          coin = (Item)ironTwentys.removeFirst();
        }
        else
        {
          coin = ItemFactory.createItem(59, Server.rand.nextFloat() * 100.0F, null);
          ironCoins += 20L;
          updateCreatedIron(ironCoins);
        }
        items.add(coin);
        coin.setBanked(false);
        value -= 20L;
      }
    }
    return getIronFiveCoinsFor(value, items);
  }
  
  private Item[] getIronFiveCoinsFor(long value, Set<Item> items)
    throws FailedException, NoSuchTemplateException
  {
    if (value > 0L)
    {
      long num = value / 5L;
      if (items == null) {
        items = new HashSet();
      }
      for (long x = 0L; x < num; x += 1L)
      {
        Item coin = null;
        if (ironFives.size() > 0)
        {
          coin = (Item)ironFives.removeFirst();
        }
        else
        {
          coin = ItemFactory.createItem(55, Server.rand.nextFloat() * 100.0F, null);
          ironCoins += 5L;
          updateCreatedIron(ironCoins);
        }
        items.add(coin);
        coin.setBanked(false);
        value -= 5L;
      }
    }
    return getIronOneCoinsFor(value, items);
  }
  
  private Item[] getIronOneCoinsFor(long value, Set<Item> items)
    throws FailedException, NoSuchTemplateException
  {
    if (value > 0L)
    {
      long num = value;
      if (items == null) {
        items = new HashSet();
      }
      for (int x = 0; x < num; x++)
      {
        Item coin = null;
        if (ironOnes.size() > 0)
        {
          coin = (Item)ironOnes.removeFirst();
        }
        else
        {
          coin = ItemFactory.createItem(51, Server.rand.nextFloat() * 100.0F, null);
          ironCoins += 1L;
          updateCreatedIron(ironCoins);
        }
        items.add(coin);
        coin.setBanked(false);
        value -= 1L;
      }
    }
    return (Item[])items.toArray(new Item[items.size()]);
  }
  
  SupplyDemand getSupplyDemand(int itemTemplateId)
  {
    SupplyDemand sd = (SupplyDemand)supplyDemand.get(Integer.valueOf(itemTemplateId));
    if (sd == null) {
      sd = createSupplyDemand(itemTemplateId);
    }
    return sd;
  }
  
  public int getPool(int itemTemplateId)
  {
    SupplyDemand sd = getSupplyDemand(itemTemplateId);
    return sd.getPool();
  }
  
  public Shop getShop(Creature creature)
  {
    return getShop(creature, false);
  }
  
  public Shop getShop(Creature creature, boolean destroying)
  {
    Shop tm = null;
    if (creature.isNpcTrader())
    {
      SHOPS_RW_LOCK.readLock().lock();
      try
      {
        tm = (Shop)shops.get(new Long(creature.getWurmId()));
      }
      finally
      {
        SHOPS_RW_LOCK.readLock().unlock();
      }
      if ((!destroying) && (tm == null)) {
        tm = createShop(creature.getWurmId());
      }
    }
    return tm;
  }
  
  public Shop[] getShopsForOwner(long owner)
  {
    Set<Shop> sh = new HashSet();
    SHOPS_RW_LOCK.readLock().lock();
    try
    {
      for (Shop shop : shops.values()) {
        if (shop.getOwnerId() == owner) {
          sh.add(shop);
        }
      }
    }
    finally
    {
      SHOPS_RW_LOCK.readLock().unlock();
    }
    return (Shop[])sh.toArray(new Shop[sh.size()]);
  }
  
  /* Error */
  public Shop getKingsShop()
  {
    // Byte code:
    //   0: getstatic 109	com/wurmonline/server/economy/Economy:SHOPS_RW_LOCK	Ljava/util/concurrent/locks/ReentrantReadWriteLock;
    //   3: invokevirtual 110	java/util/concurrent/locks/ReentrantReadWriteLock:readLock	()Ljava/util/concurrent/locks/ReentrantReadWriteLock$ReadLock;
    //   6: invokevirtual 111	java/util/concurrent/locks/ReentrantReadWriteLock$ReadLock:lock	()V
    //   9: getstatic 112	com/wurmonline/server/economy/Economy:shops	Ljava/util/Map;
    //   12: lconst_0
    //   13: invokestatic 126	java/lang/Long:valueOf	(J)Ljava/lang/Long;
    //   16: invokeinterface 167 2 0
    //   21: checkcast 115	com/wurmonline/server/economy/Shop
    //   24: astore_1
    //   25: getstatic 109	com/wurmonline/server/economy/Economy:SHOPS_RW_LOCK	Ljava/util/concurrent/locks/ReentrantReadWriteLock;
    //   28: invokevirtual 110	java/util/concurrent/locks/ReentrantReadWriteLock:readLock	()Ljava/util/concurrent/locks/ReentrantReadWriteLock$ReadLock;
    //   31: invokevirtual 118	java/util/concurrent/locks/ReentrantReadWriteLock$ReadLock:unlock	()V
    //   34: goto +15 -> 49
    //   37: astore_2
    //   38: getstatic 109	com/wurmonline/server/economy/Economy:SHOPS_RW_LOCK	Ljava/util/concurrent/locks/ReentrantReadWriteLock;
    //   41: invokevirtual 110	java/util/concurrent/locks/ReentrantReadWriteLock:readLock	()Ljava/util/concurrent/locks/ReentrantReadWriteLock$ReadLock;
    //   44: invokevirtual 118	java/util/concurrent/locks/ReentrantReadWriteLock$ReadLock:unlock	()V
    //   47: aload_2
    //   48: athrow
    //   49: aload_1
    //   50: ifnonnull +9 -> 59
    //   53: aload_0
    //   54: lconst_0
    //   55: invokevirtual 177	com/wurmonline/server/economy/Economy:createShop	(J)Lcom/wurmonline/server/economy/Shop;
    //   58: astore_1
    //   59: aload_1
    //   60: areturn
    // Line number table:
    //   Java source line #731	-> byte code offset #0
    //   Java source line #734	-> byte code offset #9
    //   Java source line #738	-> byte code offset #25
    //   Java source line #739	-> byte code offset #34
    //   Java source line #738	-> byte code offset #37
    //   Java source line #739	-> byte code offset #47
    //   Java source line #740	-> byte code offset #49
    //   Java source line #741	-> byte code offset #53
    //   Java source line #742	-> byte code offset #59
    // Local variable table:
    //   start	length	slot	name	signature
    //   0	61	0	this	Economy
    //   24	2	1	tm	Shop
    //   49	11	1	tm	Shop
    //   37	11	2	localObject	Object
    // Exception table:
    //   from	to	target	type
    //   9	25	37	finally
  }
  
  /* Error */
  static void addShop(Shop tm)
  {
    // Byte code:
    //   0: getstatic 109	com/wurmonline/server/economy/Economy:SHOPS_RW_LOCK	Ljava/util/concurrent/locks/ReentrantReadWriteLock;
    //   3: invokevirtual 179	java/util/concurrent/locks/ReentrantReadWriteLock:writeLock	()Ljava/util/concurrent/locks/ReentrantReadWriteLock$WriteLock;
    //   6: invokevirtual 180	java/util/concurrent/locks/ReentrantReadWriteLock$WriteLock:lock	()V
    //   9: getstatic 112	com/wurmonline/server/economy/Economy:shops	Ljava/util/Map;
    //   12: new 174	java/lang/Long
    //   15: dup
    //   16: aload_0
    //   17: invokevirtual 125	com/wurmonline/server/economy/Shop:getWurmId	()J
    //   20: invokespecial 176	java/lang/Long:<init>	(J)V
    //   23: aload_0
    //   24: invokeinterface 127 3 0
    //   29: pop
    //   30: getstatic 109	com/wurmonline/server/economy/Economy:SHOPS_RW_LOCK	Ljava/util/concurrent/locks/ReentrantReadWriteLock;
    //   33: invokevirtual 179	java/util/concurrent/locks/ReentrantReadWriteLock:writeLock	()Ljava/util/concurrent/locks/ReentrantReadWriteLock$WriteLock;
    //   36: invokevirtual 181	java/util/concurrent/locks/ReentrantReadWriteLock$WriteLock:unlock	()V
    //   39: goto +15 -> 54
    //   42: astore_1
    //   43: getstatic 109	com/wurmonline/server/economy/Economy:SHOPS_RW_LOCK	Ljava/util/concurrent/locks/ReentrantReadWriteLock;
    //   46: invokevirtual 179	java/util/concurrent/locks/ReentrantReadWriteLock:writeLock	()Ljava/util/concurrent/locks/ReentrantReadWriteLock$WriteLock;
    //   49: invokevirtual 181	java/util/concurrent/locks/ReentrantReadWriteLock$WriteLock:unlock	()V
    //   52: aload_1
    //   53: athrow
    //   54: return
    // Line number table:
    //   Java source line #747	-> byte code offset #0
    //   Java source line #750	-> byte code offset #9
    //   Java source line #754	-> byte code offset #30
    //   Java source line #755	-> byte code offset #39
    //   Java source line #754	-> byte code offset #42
    //   Java source line #755	-> byte code offset #52
    //   Java source line #756	-> byte code offset #54
    // Local variable table:
    //   start	length	slot	name	signature
    //   0	55	0	tm	Shop
    //   42	11	1	localObject	Object
    // Exception table:
    //   from	to	target	type
    //   9	30	42	finally
  }
  
  /* Error */
  public static void deleteShop(long wurmid)
  {
    // Byte code:
    //   0: getstatic 109	com/wurmonline/server/economy/Economy:SHOPS_RW_LOCK	Ljava/util/concurrent/locks/ReentrantReadWriteLock;
    //   3: invokevirtual 179	java/util/concurrent/locks/ReentrantReadWriteLock:writeLock	()Ljava/util/concurrent/locks/ReentrantReadWriteLock$WriteLock;
    //   6: invokevirtual 180	java/util/concurrent/locks/ReentrantReadWriteLock$WriteLock:lock	()V
    //   9: getstatic 112	com/wurmonline/server/economy/Economy:shops	Ljava/util/Map;
    //   12: new 174	java/lang/Long
    //   15: dup
    //   16: lload_0
    //   17: invokespecial 176	java/lang/Long:<init>	(J)V
    //   20: invokeinterface 167 2 0
    //   25: checkcast 115	com/wurmonline/server/economy/Shop
    //   28: astore_2
    //   29: aload_2
    //   30: ifnull +7 -> 37
    //   33: aload_2
    //   34: invokevirtual 182	com/wurmonline/server/economy/Shop:delete	()V
    //   37: getstatic 112	com/wurmonline/server/economy/Economy:shops	Ljava/util/Map;
    //   40: new 174	java/lang/Long
    //   43: dup
    //   44: lload_0
    //   45: invokespecial 176	java/lang/Long:<init>	(J)V
    //   48: invokeinterface 183 2 0
    //   53: pop
    //   54: getstatic 109	com/wurmonline/server/economy/Economy:SHOPS_RW_LOCK	Ljava/util/concurrent/locks/ReentrantReadWriteLock;
    //   57: invokevirtual 179	java/util/concurrent/locks/ReentrantReadWriteLock:writeLock	()Ljava/util/concurrent/locks/ReentrantReadWriteLock$WriteLock;
    //   60: invokevirtual 181	java/util/concurrent/locks/ReentrantReadWriteLock$WriteLock:unlock	()V
    //   63: goto +15 -> 78
    //   66: astore_3
    //   67: getstatic 109	com/wurmonline/server/economy/Economy:SHOPS_RW_LOCK	Ljava/util/concurrent/locks/ReentrantReadWriteLock;
    //   70: invokevirtual 179	java/util/concurrent/locks/ReentrantReadWriteLock:writeLock	()Ljava/util/concurrent/locks/ReentrantReadWriteLock$WriteLock;
    //   73: invokevirtual 181	java/util/concurrent/locks/ReentrantReadWriteLock$WriteLock:unlock	()V
    //   76: aload_3
    //   77: athrow
    //   78: return
    // Line number table:
    //   Java source line #760	-> byte code offset #0
    //   Java source line #763	-> byte code offset #9
    //   Java source line #764	-> byte code offset #29
    //   Java source line #765	-> byte code offset #33
    //   Java source line #766	-> byte code offset #37
    //   Java source line #770	-> byte code offset #54
    //   Java source line #771	-> byte code offset #63
    //   Java source line #770	-> byte code offset #66
    //   Java source line #771	-> byte code offset #76
    //   Java source line #772	-> byte code offset #78
    // Local variable table:
    //   start	length	slot	name	signature
    //   0	79	0	wurmid	long
    //   28	6	2	shop	Shop
    //   66	11	3	localObject	Object
    // Exception table:
    //   from	to	target	type
    //   9	54	66	finally
  }
  
  static void addSupplyDemand(SupplyDemand sd)
  {
    supplyDemand.put(Integer.valueOf(sd.getId()), sd);
  }
  
  public void addItemSoldByTraders(int templateId)
  {
    getSupplyDemand(templateId).addItemSoldByTrader();
  }
  
  public abstract void addItemSoldByTraders(String paramString1, long paramLong, String paramString2, String paramString3, int paramInt);
  
  public void addItemBoughtByTraders(int templateId)
  {
    getSupplyDemand(templateId).addItemBoughtByTrader();
  }
  
  public Change getChangeFor(long value)
  {
    return new Change(value);
  }
  
  public Creature getRandomTrader()
  {
    Creature toReturn = null;
    SHOPS_RW_LOCK.readLock().lock();
    try
    {
      size = shops.size();
      for (Shop shop : shops.values()) {
        if ((!shop.isPersonal()) && (shop.getWurmId() > 0L)) {
          if (Server.rand.nextInt(Math.max(2, size / 2)) == 0) {
            try
            {
              toReturn = Creatures.getInstance().getCreature(shop.getWurmId());
              return toReturn;
            }
            catch (NoSuchCreatureException nsc)
            {
              logger.log(Level.WARNING, "Weird, shop with id " + shop.getWurmId() + " has no creature.");
            }
          }
        }
      }
    }
    finally
    {
      int size;
      SHOPS_RW_LOCK.readLock().unlock();
    }
    return toReturn;
  }
  
  public Creature getTraderForZone(int x, int y, boolean surfaced)
  {
    int sx = 0;
    int sy = 0;
    int ex = 64;
    int ey = 64;
    Creature toReturn = null;
    SHOPS_RW_LOCK.readLock().lock();
    try
    {
      for (Shop shop : shops.values()) {
        if ((!shop.isPersonal()) && (shop.getWurmId() > 0L))
        {
          VolaTile tile = shop.getPos();
          if (tile != null)
          {
            sx = tile.getTileX() - 64;
            sy = tile.getTileY() - 64;
            ex = tile.getTileX() + 64;
            ey = tile.getTileY() + 64;
            if ((x < ex) && (x > sx) && (y < ey) && (y > sy) && (tile.isOnSurface() == surfaced)) {
              try
              {
                toReturn = Creatures.getInstance().getCreature(shop.getWurmId());
                return toReturn;
              }
              catch (NoSuchCreatureException nsc)
              {
                logger.log(Level.WARNING, "Weird, shop with id " + shop.getWurmId() + " has no creature.");
              }
            }
          }
        }
      }
    }
    finally
    {
      SHOPS_RW_LOCK.readLock().unlock();
    }
    return null;
  }
  
  public abstract void updateCreatedIron(long paramLong);
  
  public abstract void updateCreatedSilver(long paramLong);
  
  public abstract void updateCreatedCopper(long paramLong);
  
  public abstract void updateCreatedGold(long paramLong);
  
  abstract void loadSupplyDemand();
  
  abstract void loadShopMoney();
  
  abstract void initialize()
    throws IOException;
  
  abstract SupplyDemand createSupplyDemand(int paramInt);
  
  public abstract Shop createShop(long paramLong);
  
  public abstract Shop createShop(long paramLong1, long paramLong2);
  
  public abstract void transaction(long paramLong1, long paramLong2, long paramLong3, String paramString, long paramLong4);
  
  public abstract void updateLastPolled();
  
  public final void resetEarnings()
  {
    for (Shop s : shops.values()) {
      s.resetEarnings();
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\economy\Economy.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */