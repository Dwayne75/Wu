package com.wurmonline.server.zones;

import com.wurmonline.server.Features.Feature;
import com.wurmonline.server.Items;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.structures.Structure;
import com.wurmonline.server.villages.Village;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public final class VolaTileItems
{
  private static final Logger logger = Logger.getLogger(VolaTileItems.class.getName());
  private int[] itemsPerLevel = new int[1];
  private int[] decoItemsPerLevel = new int[1];
  private final int PILECOUNT = 3;
  private boolean hasFire = false;
  private final int maxFloorLevel = 20;
  private ConcurrentHashMap<Integer, Item> pileItems = new ConcurrentHashMap();
  private ConcurrentHashMap<Integer, Item> onePerTileItems = new ConcurrentHashMap();
  private ConcurrentHashMap<Integer, Set<Item>> fourPerTileItems = new ConcurrentHashMap();
  private Set<Item> allItems = new HashSet();
  private Set<Item> alwaysPoll;
  
  private void incrementItemsOnLevelByOne(int level)
  {
    if (this.itemsPerLevel.length > level) {
      this.itemsPerLevel[level] += 1;
    }
  }
  
  private void incrementDecoItemsOnLevelByOne(int level)
  {
    if (this.decoItemsPerLevel.length > level) {
      this.decoItemsPerLevel[level] += 1;
    }
  }
  
  private void decrementItemsOnLevelByOne(int level)
  {
    if (this.itemsPerLevel.length > level) {
      if (this.itemsPerLevel[level] > 0) {
        this.itemsPerLevel[level] -= 1;
      }
    }
  }
  
  private void decrementDecoItemsOnLevelByOne(int level)
  {
    if (this.decoItemsPerLevel.length > level) {
      if (this.decoItemsPerLevel[level] > 0) {
        this.decoItemsPerLevel[level] -= 1;
      }
    }
  }
  
  public final boolean addItem(Item item, boolean starting)
  {
    if (!this.allItems.contains(item))
    {
      if (item.isFire())
      {
        if (this.hasFire) {
          if (item.getTemplateId() == 37)
          {
            Item c = getCampfire(false);
            if (c != null)
            {
              short maxTemp = 30000;
              c.setTemperature((short)Math.min(30000, c.getTemperature() + item.getWeightGrams()));
              Items.destroyItem(item.getWurmId());
              return false;
            }
          }
        }
        this.hasFire = true;
      }
      if (item.isOnePerTile()) {
        this.onePerTileItems.put(Integer.valueOf(item.getFloorLevel()), item);
      }
      if (item.isFourPerTile())
      {
        Set<Item> itemSet = (Set)this.fourPerTileItems.get(Integer.valueOf(item.getFloorLevel()));
        if (itemSet == null) {
          itemSet = new HashSet();
        }
        int count = getFourPerTileCount(item.getFloorLevel());
        if ((item.isPlanted()) && (count >= 4))
        {
          logger.info("Unplanted " + item.getName() + " (" + item.getWurmId() + ") as tile " + item.getTileX() + "," + item
            .getTileY() + " already has " + count + " items");
          item.setIsPlanted(false);
        }
        itemSet.add(item);
        this.fourPerTileItems.put(Integer.valueOf(item.getFloorLevel()), itemSet);
      }
      int fl = trimFloorLevel(item.getFloorLevel());
      if (fl + 1 > this.itemsPerLevel.length) {
        this.itemsPerLevel = createNewLevelArrayAt(fl + 1);
      }
      if (item.isDecoration())
      {
        if (fl + 1 > this.decoItemsPerLevel.length) {
          this.decoItemsPerLevel = createNewDecoLevelArrayAt(fl + 1);
        }
        incrementDecoItemsOnLevelByOne(fl);
      }
      this.allItems.add(item);
      incrementItemsOnLevelByOne(fl);
      if (item.isTent()) {
        Items.addTent(item);
      }
      if ((item.isRoadMarker()) && (item.isPlanted()) && (Features.Feature.HIGHWAYS.isEnabled())) {
        Items.addMarker(item);
      }
      if ((item.getTemplateId() == 677) && (item.isPlanted())) {
        Items.addGmSign(item);
      }
      if ((item.getTemplateId() == 1309) && (item.isPlanted()) && (Features.Feature.HIGHWAYS.isEnabled())) {
        Items.addWagonerContainer(item);
      }
      if (item.isAlwaysPoll()) {
        addAlwaysPollItem(item);
      }
      if (item.isSpawnPoint()) {
        Items.addSpawn(item);
      }
      return true;
    }
    return false;
  }
  
  private final int[] createNewLevelArrayAt(int floorLevels)
  {
    int[] newArr = new int[floorLevels];
    for (int level = 0; level < floorLevels; level++) {
      if (level < this.itemsPerLevel.length) {
        newArr[level] = this.itemsPerLevel[level];
      }
    }
    return newArr;
  }
  
  private final int[] createNewDecoLevelArrayAt(int floorLevels)
  {
    int[] newArr = new int[floorLevels];
    for (int level = 0; level < floorLevels; level++) {
      if (level < this.decoItemsPerLevel.length) {
        newArr[level] = this.decoItemsPerLevel[level];
      }
    }
    return newArr;
  }
  
  public final boolean contains(Item item)
  {
    return this.allItems.contains(item);
  }
  
  public final boolean removeItem(Item item)
  {
    if (item.isAlwaysPoll()) {
      removeAlwaysPollItem(item);
    }
    int fl = trimFloorLevel(item.getFloorLevel());
    decrementItemsOnLevelByOne(fl);
    if (item.isDecoration()) {
      decrementDecoItemsOnLevelByOne(fl);
    }
    this.allItems.remove(item);
    if (item.isFire()) {
      this.hasFire = stillHasFire();
    }
    if (item.isOnePerTile()) {
      this.onePerTileItems.remove(Integer.valueOf(item.getFloorLevel()));
    }
    if (item.isTent()) {
      Items.removeTent(item);
    }
    if (item.isSpawnPoint()) {
      Items.removeSpawn(item);
    }
    return isEmpty();
  }
  
  final void destroy(VolaTile toSendTo)
  {
    for (Item i : this.allItems) {
      toSendTo.sendRemoveItem(i);
    }
    for (Item pile : this.pileItems.values()) {
      toSendTo.sendRemoveItem(pile);
    }
  }
  
  void moveToNewFloorLevel(Item item, int oldFloorLevel)
  {
    if (item.getFloorLevel() != oldFloorLevel)
    {
      int fl = trimFloorLevel(item.getFloorLevel());
      int old = trimFloorLevel(oldFloorLevel);
      if (fl + 1 > this.itemsPerLevel.length) {
        this.itemsPerLevel = createNewLevelArrayAt(fl + 1);
      }
      incrementItemsOnLevelByOne(fl);
      decrementItemsOnLevelByOne(old);
      if (item.isDecoration())
      {
        if (fl + 1 > this.decoItemsPerLevel.length) {
          this.decoItemsPerLevel = createNewDecoLevelArrayAt(fl + 1);
        }
        incrementDecoItemsOnLevelByOne(fl);
        decrementDecoItemsOnLevelByOne(old);
      }
    }
  }
  
  boolean movePileItemToNewFloorLevel(Item item, int oldFloorLevel)
  {
    if (item.getFloorLevel() != oldFloorLevel)
    {
      Item oldPile = (Item)this.pileItems.get(Integer.valueOf(oldFloorLevel));
      if (oldPile == item) {
        this.pileItems.remove(Integer.valueOf(oldFloorLevel));
      }
      Item newPile = (Item)this.pileItems.get(Integer.valueOf(item.getFloorLevel()));
      if (newPile == null) {
        this.pileItems.put(Integer.valueOf(item.getFloorLevel()), item);
      } else if (newPile != item) {
        return true;
      }
    }
    return false;
  }
  
  final Item[] getPileItems()
  {
    return (Item[])this.pileItems.values().toArray(new Item[this.pileItems.size()]);
  }
  
  private boolean stillHasFire()
  {
    if (getCampfire(true) != null) {
      return true;
    }
    return false;
  }
  
  protected final boolean hasOnePerTileItem(int floorLevel)
  {
    return this.onePerTileItems.get(Integer.valueOf(floorLevel)) != null;
  }
  
  protected final Item getOnePerTileItem(int floorLevel)
  {
    return (Item)this.onePerTileItems.get(Integer.valueOf(floorLevel));
  }
  
  protected final int getFourPerTileCount(int floorLevel)
  {
    Set<Item> itemSet = (Set)this.fourPerTileItems.get(Integer.valueOf(floorLevel));
    if (itemSet == null) {
      return 0;
    }
    int count = 0;
    for (Item item : itemSet)
    {
      if (item.isPlanted()) {
        count++;
      }
      if (item.getTemplateId() == 1311) {
        count++;
      }
    }
    return count;
  }
  
  public final boolean hasFire()
  {
    return this.hasFire;
  }
  
  public final Item[] getAllItemsAsArray()
  {
    return (Item[])this.allItems.toArray(new Item[this.allItems.size()]);
  }
  
  public final Set<Item> getAllItemsAsSet()
  {
    return this.allItems;
  }
  
  private final int trimFloorLevel(int floorLevel)
  {
    return Math.min(20, Math.max(0, floorLevel));
  }
  
  public final int getNumberOfItems(int floorLevel)
  {
    if (this.itemsPerLevel == null) {
      return 0;
    }
    if (this.itemsPerLevel.length - 1 < trimFloorLevel(floorLevel)) {
      return 0;
    }
    return this.itemsPerLevel[trimFloorLevel(floorLevel)];
  }
  
  public final int getNumberOfDecorations(int floorLevel)
  {
    if (this.decoItemsPerLevel == null) {
      return 0;
    }
    if (this.decoItemsPerLevel.length - 1 < trimFloorLevel(floorLevel)) {
      return 0;
    }
    return this.decoItemsPerLevel[trimFloorLevel(floorLevel)];
  }
  
  void poll(boolean pollItems, int seed, boolean lava, Structure structure, boolean surfaced, Village village, long now)
  {
    if (pollItems)
    {
      Item[] lTempItems = getAllItemsAsArray();
      for (int x = 0; x < lTempItems.length; x++) {
        if ((lava) && (!lTempItems[x].isIndestructible()) && (!lTempItems[x].isHugeAltar())) {
          lTempItems[x].setDamage(lTempItems[x].getDamage() + 1.0F);
        } else {
          lTempItems[x].poll(((structure != null) && (structure.isFinished())) || (!surfaced), village != null, 1L);
        }
      }
    }
    else if (this.alwaysPoll != null)
    {
      Item[] lTempItems = (Item[])this.alwaysPoll.toArray(new Item[this.alwaysPoll.size()]);
      for (int x = 0; x < lTempItems.length; x++) {
        if ((!lTempItems[x].poll(((structure != null) && (structure.isFinished())) || (!surfaced), village != null, seed)) && (lava)) {
          lTempItems[x].setDamage(lTempItems[x].getDamage() + 0.1F);
        }
      }
    }
  }
  
  private void addAlwaysPollItem(Item item)
  {
    if (this.alwaysPoll == null) {
      this.alwaysPoll = new HashSet();
    }
    this.alwaysPoll.add(item);
  }
  
  private void removeAlwaysPollItem(Item item)
  {
    if (this.alwaysPoll != null)
    {
      this.alwaysPoll.remove(item);
      if (this.alwaysPoll.isEmpty()) {
        this.alwaysPoll = null;
      }
    }
  }
  
  final void removePileItem(int floorLevel)
  {
    this.pileItems.remove(Integer.valueOf(floorLevel));
  }
  
  final boolean checkIfCreatePileItem(int floorLevel)
  {
    if (this.itemsPerLevel.length - 1 < trimFloorLevel(floorLevel)) {
      return false;
    }
    int decoItems = 0;
    if (this.decoItemsPerLevel.length > trimFloorLevel(floorLevel)) {
      decoItems = this.decoItemsPerLevel[trimFloorLevel(floorLevel)];
    }
    if (this.itemsPerLevel[trimFloorLevel(floorLevel)] <= decoItems + 3 - 1) {
      return false;
    }
    if (this.itemsPerLevel[trimFloorLevel(floorLevel)] < 3) {
      return false;
    }
    return true;
  }
  
  final boolean checkIfRemovePileItem(int floorLevel)
  {
    if (this.itemsPerLevel.length - 1 < trimFloorLevel(floorLevel)) {
      return false;
    }
    int decoItems = 0;
    if (this.decoItemsPerLevel.length > trimFloorLevel(floorLevel)) {
      decoItems = this.decoItemsPerLevel[trimFloorLevel(floorLevel)];
    }
    if (this.itemsPerLevel[trimFloorLevel(floorLevel)] < decoItems + 3) {
      return true;
    }
    return false;
  }
  
  final Item getPileItem(int floorLevel)
  {
    return (Item)this.pileItems.get(Integer.valueOf(floorLevel));
  }
  
  final void addPileItem(Item pile)
  {
    this.pileItems.put(Integer.valueOf(pile.getFloorLevel()), pile);
  }
  
  final Item getCampfire(boolean requiresBurning)
  {
    for (Item i : this.allItems) {
      if ((i.getTemplateId() == 37) && ((!requiresBurning) || (i.getTemperature() >= 1000))) {
        return i;
      }
    }
    return null;
  }
  
  public boolean isEmpty()
  {
    return (this.allItems == null) || (this.allItems.size() == 0);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\zones\VolaTileItems.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */