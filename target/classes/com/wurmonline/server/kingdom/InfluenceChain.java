package com.wurmonline.server.kingdom;

import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.villages.Villages;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Logger;

public class InfluenceChain
{
  protected static Logger logger = Logger.getLogger(InfluenceChain.class.getName());
  public static final int MAX_TOWER_CHAIN_DISTANCE = 120;
  protected static HashMap<Byte, InfluenceChain> influenceChains = new HashMap();
  protected ArrayList<Item> chainMarkers = new ArrayList();
  protected int chainedMarkers = 0;
  protected byte kingdom;
  
  public InfluenceChain(byte kingdom)
  {
    this.kingdom = kingdom;
    Village capital = Villages.getCapital(kingdom);
    if (capital != null)
    {
      try
      {
        this.chainMarkers.add(capital.getToken());
      }
      catch (NoSuchItemException e)
      {
        logger.warning(String.format("Influence Chain Error: No token found for village %s.", new Object[] { capital.getName() }));
      }
    }
    else
    {
      for (Village v : Villages.getVillages()) {
        if (v.kingdom == kingdom)
        {
          logger.info(String.format("Because kingdom %s has no capital, the village %s has been selected as it's influence chain start.", new Object[] {
            Kingdoms.getKingdom(kingdom).getName(), v.getName() }));
          capital = v;
          break;
        }
      }
      if (capital != null) {
        try
        {
          this.chainMarkers.add(capital.getToken());
        }
        catch (NoSuchItemException e)
        {
          logger.warning(String.format("Influence Chain Error: No token found for village %s.", new Object[] { capital.getName() }));
        }
      } else {
        logger.warning(String.format("Influence Chain Error: There is no compatible villages for kingdom %s to start an influence chain.", new Object[] {
          Kingdoms.getKingdom(kingdom).getName() }));
      }
    }
  }
  
  public ArrayList<Item> getChainMarkers()
  {
    return this.chainMarkers;
  }
  
  public void pulseChain(Item marker)
  {
    for (Item otherMarker : this.chainMarkers) {
      if (!otherMarker.isChained())
      {
        int distX = Math.abs(marker.getTileX() - otherMarker.getTileX());
        int distY = Math.abs(marker.getTileY() - otherMarker.getTileY());
        int maxDist = Math.max(distX, distY);
        if (maxDist <= 120)
        {
          otherMarker.setChained(true);
          this.chainedMarkers += 1;
          pulseChain(otherMarker);
        }
      }
    }
  }
  
  public void recalculateChain()
  {
    for (Iterator localIterator = this.chainMarkers.iterator(); localIterator.hasNext();)
    {
      marker = (Item)localIterator.next();
      
      marker.setChained(false);
    }
    Item marker;
    Item capitalToken = (Item)this.chainMarkers.get(0);
    capitalToken.setChained(true);
    this.chainedMarkers = 1;
    for (Village v : Villages.getVillages()) {
      if ((v.kingdom == this.kingdom) && (v.isPermanent)) {
        try
        {
          Item villageToken = v.getToken();
          villageToken.setChained(true);
          this.chainedMarkers += 1;
          pulseChain(villageToken);
        }
        catch (NoSuchItemException e)
        {
          logger.warning(String.format("Influence Chain Error: No token found for village %s.", new Object[] { v.getName() }));
        }
      }
    }
    pulseChain(capitalToken);
  }
  
  public static InfluenceChain getInfluenceChain(byte kingdom)
  {
    if (influenceChains.containsKey(Byte.valueOf(kingdom))) {
      return (InfluenceChain)influenceChains.get(Byte.valueOf(kingdom));
    }
    InfluenceChain newChain = new InfluenceChain(kingdom);
    influenceChains.put(Byte.valueOf(kingdom), newChain);
    return newChain;
  }
  
  public void addToken(Item token)
  {
    if (this.chainMarkers.contains(token)) {
      logger.info(String.format("Token at %d, %d already exists in the influence chain.", new Object[] {
        Integer.valueOf(token.getTileX()), Integer.valueOf(token.getTileY()) }));
    }
    this.chainMarkers.add(token);
    recalculateChain();
    logger.info(String.format("Added new village token to %s, which now has %d markers ad %d successfully linked.", new Object[] {
      Kingdoms.getKingdom(this.kingdom).getName(), Integer.valueOf(this.chainMarkers.size()), Integer.valueOf(this.chainedMarkers) }));
  }
  
  public static void addTokenToChain(byte kingdom, Item token)
  {
    InfluenceChain kingdomChain = getInfluenceChain(kingdom);
    kingdomChain.addToken(token);
  }
  
  public void addTower(Item tower)
  {
    if (this.chainMarkers.contains(tower))
    {
      logger.info(String.format("Tower at %d, %d already exists in the influence chain.", new Object[] {
        Integer.valueOf(tower.getTileX()), Integer.valueOf(tower.getTileY()) }));
      return;
    }
    this.chainMarkers.add(tower);
    recalculateChain();
    logger.info(String.format("Added new tower to %s, which now has %d markers and %d successfully linked.", new Object[] {
      Kingdoms.getKingdom(this.kingdom).getName(), Integer.valueOf(this.chainMarkers.size()), Integer.valueOf(this.chainedMarkers) }));
  }
  
  public static void addTowerToChain(byte kingdom, Item tower)
  {
    InfluenceChain kingdomChain = getInfluenceChain(kingdom);
    kingdomChain.addTower(tower);
  }
  
  public void removeTower(Item tower)
  {
    this.chainMarkers.remove(tower);
    recalculateChain();
    logger.info(String.format("Removed tower from %s, which now has %d markers and %d successfully linked.", new Object[] {
      Kingdoms.getKingdom(this.kingdom).getName(), Integer.valueOf(this.chainMarkers.size()), Integer.valueOf(this.chainedMarkers) }));
  }
  
  public static void removeTowerFromChain(byte kingdom, Item tower)
  {
    InfluenceChain kingdomChain = getInfluenceChain(kingdom);
    kingdomChain.removeTower(tower);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\kingdom\InfluenceChain.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */