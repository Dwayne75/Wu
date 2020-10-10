package com.wurmonline.server.kingdom;

import com.wurmonline.server.Features.Feature;
import com.wurmonline.server.Items;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Players;
import com.wurmonline.server.Server;
import com.wurmonline.server.ServerEntry;
import com.wurmonline.server.Servers;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.villages.Villages;
import com.wurmonline.server.zones.Zones;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentHashMap.KeySetView;
import java.util.logging.Logger;

public final class Kingdoms
  implements MiscConstants
{
  public static final String KINGDOM_NAME_JENN = "Jenn-Kellon";
  public static final String KINGDOM_CHAT_JENN = "Jenn-Kellon";
  public static final String KINGDOM_NAME_MOLREHAN = "Mol Rehan";
  public static final String KINGDOM_CHAT_MOLREHAN = "Mol Rehan";
  public static final String KINGDOM_NAME_LIBILA = "Horde of the Summoned";
  public static final String KINGDOM_CHAT_HOTS = "HOTS";
  public static final String KINGDOM_NAME_FREEDOM = "Freedom Isles";
  public static final String KINGDOM_CHAT_FREEDOM = "Freedom";
  public static final String KINGDOM_NAME_NONE = "no known kingdom";
  public static final String KINGDOM_SUFFIX_JENN = "jenn.";
  public static final String KINGDOM_SUFFIX_MOLREHAN = "molr.";
  public static final String KINGDOM_SUFFIX_HOTS = "hots.";
  public static final String KINGDOM_SUFFIX_FREEDOM = "free.";
  public static final String KINGDOM_SUFFIX_NONE = "";
  public static int activePremiumJenn = 0;
  public static int activePremiumMolr = 0;
  public static int activePremiumHots = 0;
  public static final int TOWER_INFLUENCE = 60;
  public static final int CHALLENGE_ITEM_INFLUENCE = 20;
  public static final int minKingdomDist = Servers.localServer.id == 3 ? 100 : Servers.localServer.isChallengeServer() ? 60 : 150;
  public static final int maxTowerDistance = 100;
  private static final Map<Byte, Kingdom> kingdoms = new HashMap();
  private static Logger logger = Logger.getLogger(Kingdoms.class.getName());
  private static final ConcurrentHashMap<Item, GuardTower> towers = new ConcurrentHashMap();
  public static final int minOwnTowerDistance = 50;
  public static final int minArcheryTowerDistance = 20;
  
  public static final void createBasicKingdoms()
  {
    addKingdom(new Kingdom((byte)0, (byte)0, "no known kingdom", "abofk7ba", "none", "", "Unknown", "Unknown", false));
    
    addKingdom(new Kingdom((byte)1, (byte)1, "Jenn-Kellon", "abosdsd", "Jenn-Kellon", "jenn.", "Noble", "Protectors", true));
    
    addKingdom(new Kingdom((byte)2, (byte)2, "Mol Rehan", "ajajkjh3d", "Mol Rehan", "molr.", "Fire", "Gold", true));
    
    addKingdom(new Kingdom((byte)3, (byte)3, "Horde of the Summoned", "11dfkjutyd", "HOTS", "hots.", "Hate", "Vengeance", true));
    
    addKingdom(new Kingdom((byte)4, (byte)4, "Freedom Isles", "asiuytsr", "Freedom", "free.", "Peaceful", "Friendly", true));
  }
  
  protected static final int numKingdoms()
  {
    return kingdoms.size();
  }
  
  static int getActivePremiumJenn()
  {
    return activePremiumJenn;
  }
  
  static void setActivePremiumJenn(int aActivePremiumJenn)
  {
    activePremiumJenn = aActivePremiumJenn;
  }
  
  static int getActivePremiumMolr()
  {
    return activePremiumMolr;
  }
  
  static void setActivePremiumMolr(int aActivePremiumMolr)
  {
    activePremiumMolr = aActivePremiumMolr;
  }
  
  static int getActivePremiumHots()
  {
    return activePremiumHots;
  }
  
  static void setActivePremiumHots(int aActivePremiumHots)
  {
    activePremiumHots = aActivePremiumHots;
  }
  
  public static final String getNameFor(byte kingdom)
  {
    Kingdom k = getKingdomOrNull(kingdom);
    if (k != null) {
      return k.getName();
    }
    return "no known kingdom";
  }
  
  public static final boolean isKingdomChat(String chatTitle)
  {
    for (Kingdom k : kingdoms.values()) {
      if (k.getChatName().equals(chatTitle)) {
        return true;
      }
    }
    return false;
  }
  
  public static final boolean isGlobalKingdomChat(String chatTitle)
  {
    for (Kingdom k : kingdoms.values()) {
      if (("GL-" + k.getChatName()).equals(chatTitle)) {
        return true;
      }
    }
    return false;
  }
  
  public static final boolean mayCreateKingdom()
  {
    return kingdoms.size() < 255;
  }
  
  public static final Kingdom getKingdomWithName(String kname)
  {
    for (Kingdom k : kingdoms.values()) {
      if (k.getName().equalsIgnoreCase(kname)) {
        return k;
      }
    }
    return null;
  }
  
  public static final Kingdom getKingdomWithChatTitle(String chatTitle)
  {
    for (Kingdom k : kingdoms.values()) {
      if (k.getChatName().equals(chatTitle)) {
        return k;
      }
    }
    return null;
  }
  
  public static final Kingdom getKingdomWithSuffix(String suffix)
  {
    for (Kingdom k : kingdoms.values()) {
      if (k.getSuffix().equals(suffix)) {
        return k;
      }
    }
    return null;
  }
  
  public static final void loadKingdom(Kingdom kingdom)
  {
    Kingdom oldk = (Kingdom)kingdoms.get(Byte.valueOf(kingdom.kingdomId));
    if (oldk != null) {
      kingdom.setAlliances(oldk.getAllianceMap());
    }
    kingdoms.put(Byte.valueOf(kingdom.kingdomId), kingdom);
  }
  
  public static final boolean addKingdom(Kingdom kingdom)
  {
    boolean isNew = false;
    boolean exists = false;
    Kingdom oldk = (Kingdom)kingdoms.get(Byte.valueOf(kingdom.kingdomId));
    if (oldk != null)
    {
      exists = true;
      kingdom.setAlliances(oldk.getAllianceMap());
      kingdom.setExistsHere(oldk.existsHere());
      kingdom.activePremiums = oldk.activePremiums;
      if ((oldk.acceptsTransfers() != kingdom.acceptsTransfers()) || 
        (!oldk.getFirstMotto().equals(kingdom.getFirstMotto())) || 
        (!oldk.getSecondMotto().equals(kingdom.getSecondMotto())) || 
        (!oldk.getPassword().equals(kingdom.getPassword()))) {
        kingdom.update();
      }
    }
    else
    {
      isNew = true;
    }
    kingdoms.put(Byte.valueOf(kingdom.kingdomId), kingdom);
    if (isNew) {
      Players.getInstance().sendKingdomToPlayers(kingdom);
    }
    kingdom.setShouldBeDeleted(false);
    if (!exists) {
      kingdom.saveToDisk();
    }
    return isNew;
  }
  
  public static void markAllKingdomsForDeletion()
  {
    Kingdom[] allKingdoms = getAllKingdoms();
    for (Kingdom k : allKingdoms) {
      k.setShouldBeDeleted(true);
    }
  }
  
  public static void trimKingdoms()
  {
    Kingdom[] allKingdoms = getAllKingdoms();
    for (Kingdom k : allKingdoms) {
      if (k.isShouldBeDeleted())
      {
        k.delete();
        removeKingdom(k.getId());
      }
    }
  }
  
  public static final void removeKingdom(byte id)
  {
    King.purgeKing(id);
    kingdoms.remove(Byte.valueOf(id));
  }
  
  public static final Kingdom getKingdomOrNull(byte id)
  {
    return (Kingdom)kingdoms.get(Byte.valueOf(id));
  }
  
  public static final Kingdom getKingdom(byte id)
  {
    Kingdom toret = (Kingdom)kingdoms.get(Byte.valueOf(id));
    if (toret == null) {
      return (Kingdom)kingdoms.get(Byte.valueOf((byte)0));
    }
    return toret;
  }
  
  public static final byte getKingdomTemplateFor(byte id)
  {
    Kingdom toret = (Kingdom)kingdoms.get(Byte.valueOf(id));
    if (toret == null) {
      return 0;
    }
    return toret.getTemplate();
  }
  
  public static final Kingdom[] getAllKingdoms()
  {
    return (Kingdom[])kingdoms.values().toArray(new Kingdom[kingdoms.values().size()]);
  }
  
  public static ConcurrentHashMap<Item, GuardTower> getTowers()
  {
    return towers;
  }
  
  public static final byte getNextAvailableKingdomId()
  {
    for (byte b = Byte.MIN_VALUE; b < Byte.MAX_VALUE; b = (byte)(b + 1)) {
      if ((b < 0) || (b > 4)) {
        if (kingdoms.get(Byte.valueOf(b)) == null) {
          return b;
        }
      }
    }
    return 0;
  }
  
  public static final String getSuffixFor(byte kingdom)
  {
    Kingdom k = getKingdomOrNull(kingdom);
    if (k != null) {
      return k.getSuffix();
    }
    return "";
  }
  
  public static final String getChatNameFor(byte kingdom)
  {
    Kingdom k = getKingdomOrNull(kingdom);
    if (k != null) {
      return k.getChatName();
    }
    return "no known kingdom";
  }
  
  public static final void addTower(Item tower)
  {
    if (!towers.keySet().contains(tower))
    {
      towers.put(tower, new GuardTower(tower));
      addTowerKingdom(tower);
    }
  }
  
  public static final void reAddKingdomInfluences(int startx, int starty, int endx, int endy)
  {
    for (Village v : Villages.getVillagesWithin(startx, starty, endx, endy)) {
      v.setKingdomInfluence();
    }
    Zones.addWarDomains();
    for (Object i = towers.keySet().iterator(); ((Iterator)i).hasNext();)
    {
      Item it = (Item)((Iterator)i).next();
      if ((it.getTileX() >= startx) && (it.getTileX() <= endx) && (it.getTileY() >= starty) && (it.getTileY() < endy)) {
        addTowerKingdom(it);
      }
    }
  }
  
  public static void addTowerKingdom(Item tower)
  {
    if ((tower.getKingdom() != 0) && (tower.getTemplateId() != 996))
    {
      Kingdom k = getKingdom(tower.getKingdom());
      if (k.getId() != 0)
      {
        for (int x = tower.getTileX() - 60; x < tower.getTileX() + 60; x++) {
          for (int y = tower.getTileY() - 60; y < tower.getTileY() + 60; y++) {
            if (Zones.getKingdom(x, y) == 0) {
              Zones.setKingdom(x, y, tower.getKingdom());
            }
          }
        }
        if (Features.Feature.TOWER_CHAINING.isEnabled()) {
          InfluenceChain.addTowerToChain(k.getId(), tower);
        }
      }
    }
  }
  
  public static final void removeInfluenceForTower(Item item)
  {
    int extraCheckedTiles = 1;
    for (int x = item.getTileX() - 60 - 1; x < item.getTileX() + 60 + 1; x++) {
      for (int y = item.getTileY() - 60 - 1; y < item.getTileY() + 60 + 1; y++) {
        if (Zones.getKingdom(x, y) == item.getKingdom()) {
          if (Villages.getVillageWithPerimeterAt(x, y, true) == null) {
            Zones.setKingdom(x, y, (byte)0);
          }
        }
      }
    }
    if (Features.Feature.TOWER_CHAINING.isEnabled()) {
      InfluenceChain.removeTowerFromChain(item.getKingdom(), item);
    }
  }
  
  public static final void addWarTargetKingdom(Item target)
  {
    if (target.getKingdom() != 0)
    {
      Kingdom k = getKingdom(target.getKingdom());
      if (k.getId() != 0)
      {
        int sx = Zones.safeTileX(target.getTileX() - 60);
        int ex = Zones.safeTileX(target.getTileX() + 60);
        int sy = Zones.safeTileY(target.getTileY() - 60);
        int ey = Zones.safeTileY(target.getTileY() + 60);
        for (int x = sx; x <= ex; x++) {
          for (int y = sy; y <= ey; y++) {
            if (Villages.getVillageWithPerimeterAt(x, y, true) == null) {
              Zones.setKingdom(x, y, target.getKingdom());
            }
          }
        }
      }
    }
  }
  
  public static final void destroyTower(Item item)
  {
    destroyTower(item, false);
  }
  
  public static final void destroyTower(Item item, boolean destroyItem)
  {
    if ((towers == null) || (towers.size() == 0))
    {
      GuardTower t = new GuardTower(item);
      t.destroy();
      Items.destroyItem(item.getWurmId());
    }
    else
    {
      GuardTower t = (GuardTower)towers.get(item);
      if (t != null) {
        t.destroy();
      }
      towers.remove(item);
      if (destroyItem) {
        Items.destroyItem(item.getWurmId());
      }
      removeInfluenceForTower(item);
      Zones.removeGuardTower(item);
      reAddKingdomInfluences(item.getTileX() - 200, item.getTileY() - 200, item.getTileX() + 200, item
        .getTileY() + 200);
    }
  }
  
  public static final GuardTower getTower(Item tower)
  {
    return (GuardTower)towers.get(tower);
  }
  
  public static final GuardTower getClosestTower(int tilex, int tiley, boolean surfaced)
  {
    GuardTower closest = null;
    int minDist = 2000;
    for (Iterator<GuardTower> it = towers.values().iterator(); it.hasNext();)
    {
      GuardTower tower = (GuardTower)it.next();
      if (tower.getTower().isOnSurface() == surfaced)
      {
        int distx = Math.abs(tower.getTower().getTileX() - tilex);
        int disty = Math.abs(tower.getTower().getTileY() - tiley);
        if ((distx < 50) && (disty < 50) && ((distx <= minDist) || (disty <= minDist)))
        {
          minDist = Math.min(distx, disty);
          closest = tower;
        }
      }
    }
    return closest;
  }
  
  public static final GuardTower getClosestEnemyTower(int tilex, int tiley, boolean surfaced, Creature searcher)
  {
    GuardTower closest = null;
    int minDist;
    Iterator<GuardTower> it;
    if (searcher.getKingdomId() != 0)
    {
      minDist = 2000;
      for (it = towers.values().iterator(); it.hasNext();)
      {
        GuardTower tower = (GuardTower)it.next();
        if (tower.getTower().isOnSurface() == surfaced)
        {
          int distx = Math.abs(tower.getTower().getTileX() - tilex);
          int disty = Math.abs(tower.getTower().getTileY() - tiley);
          if ((distx <= minDist) || (disty <= minDist)) {
            if (!searcher.isFriendlyKingdom(tower.getKingdom()))
            {
              minDist = Math.min(distx, disty);
              closest = tower;
            }
          }
        }
      }
    }
    return closest;
  }
  
  public static final Item getClosestWarTarget(int tilex, int tiley, Creature searcher)
  {
    Item closest = null;
    if (searcher.getKingdomId() != 0)
    {
      int minDist = 200;
      for (Item target : Items.getWarTargets())
      {
        int distx = Math.abs(target.getTileX() - tilex);
        int disty = Math.abs(target.getTileY() - tiley);
        if ((distx <= minDist) || (disty <= minDist)) {
          if (searcher.isFriendlyKingdom(target.getKingdom()))
          {
            minDist = Math.min(distx, disty);
            closest = target;
          }
        }
      }
    }
    return closest;
  }
  
  public static final GuardTower getTower(Creature guard)
  {
    for (Iterator<GuardTower> it = towers.values().iterator(); it.hasNext();)
    {
      GuardTower tower = (GuardTower)it.next();
      if (tower.isMyTower(guard)) {
        return tower;
      }
    }
    return null;
  }
  
  public static final GuardTower getRandomTowerForKingdom(byte kingdom)
  {
    LinkedList<GuardTower> tows = new LinkedList();
    for (Iterator<GuardTower> it = towers.values().iterator(); it.hasNext();)
    {
      GuardTower tower = (GuardTower)it.next();
      if (tower.getKingdom() == kingdom) {
        tows.add(tower);
      }
    }
    if (tows.size() > 0) {
      return (GuardTower)tows.get(Server.rand.nextInt(tows.size()));
    }
    return null;
  }
  
  public static final boolean isTowerTooNear(int tilex, int tiley, boolean surfaced, boolean archery)
  {
    Object it;
    if (archery) {
      for (Item gt : Items.getAllItems()) {
        if ((gt.isProtectionTower()) && (gt.isOnSurface() == surfaced)) {
          if ((Math.abs(((int)gt.getPosX() >> 2) - tilex) < 20) && 
            (Math.abs(((int)gt.getPosY() >> 2) - tiley) < 20)) {
            return true;
          }
        }
      }
    } else {
      for (it = towers.keySet().iterator(); ((Iterator)it).hasNext();)
      {
        Item gt = (Item)((Iterator)it).next();
        if (gt.isOnSurface() == surfaced) {
          if ((Math.abs(((int)gt.getPosX() >> 2) - tilex) < 50) && 
            (Math.abs(((int)gt.getPosY() >> 2) - tiley) < 50)) {
            return true;
          }
        }
      }
    }
    return false;
  }
  
  public static final void convertTowersWithin(int startx, int starty, int endx, int endy, byte newKingdom)
  {
    for (Iterator<Item> i = towers.keySet().iterator(); i.hasNext();)
    {
      Item it = (Item)i.next();
      if ((it.getTileX() >= startx) && (it.getTileX() <= endx) && (it.getTileY() >= starty) && (it.getTileY() < endy))
      {
        removeInfluenceForTower(it);
        it.setAuxData(newKingdom);
        addTowerKingdom(it);
        Kingdom k = getKingdom(newKingdom);
        boolean changed = false;
        if (k != null)
        {
          String aName = k.getName() + " guard tower";
          
          it.setName(aName);
          int templateId = 384;
          if (k.getTemplate() == 2) {
            templateId = 528;
          } else if (k.getTemplate() == 3) {
            templateId = 430;
          }
          if (k.getTemplate() == 4) {
            templateId = 638;
          }
          if (it.getTemplateId() != templateId)
          {
            it.setTemplateId(templateId);
            changed = true;
          }
        }
        if (!changed) {
          it.updateIfGroundItem();
        }
        ((GuardTower)towers.get(it)).destroyGuards();
      }
    }
  }
  
  public static final void poll()
  {
    for (Iterator<GuardTower> it = towers.values().iterator(); it.hasNext();) {
      ((GuardTower)it.next()).poll();
    }
    King[] kings = King.getKings();
    if (kings != null) {
      for (King king : kings) {
        try
        {
          Player player = Players.getInstance().getPlayer(king.kingid);
          if (player.getKingdomId() != king.kingdom) {
            king.abdicate(player.isOnSurface(), false);
          }
        }
        catch (NoSuchPlayerException localNoSuchPlayerException) {}
      }
    }
  }
  
  public static int getNumberOfGuardTowers()
  {
    int numberOfTowers;
    int numberOfTowers;
    if (towers != null) {
      numberOfTowers = towers.size();
    } else {
      numberOfTowers = 0;
    }
    return numberOfTowers;
  }
  
  public static void checkIfDisbandKingdom() {}
  
  public static final void destroyTowersWithKingdom(byte deletedKingdom)
  {
    if (towers != null) {
      for (GuardTower tower : towers.values()) {
        if (tower.getKingdom() == deletedKingdom) {
          destroyTower(tower.getTower(), true);
        }
      }
    }
  }
  
  public static final boolean isCustomKingdom(byte kingdomId)
  {
    return (kingdomId < 0) || (kingdomId > 4);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\kingdom\Kingdoms.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */