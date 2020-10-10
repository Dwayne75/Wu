package com.wurmonline.server.economy;

import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class SupplyDemand
{
  final int id;
  int itemsBought;
  int itemsSold;
  private static final Logger logger = Logger.getLogger(SupplyDemand.class.getName());
  long lastPolled;
  
  SupplyDemand(int aId, int aItemsBought, int aItemsSold)
  {
    this.id = aId;
    this.itemsBought = aItemsBought;
    this.itemsSold = aItemsSold;
    if (!supplyDemandExists()) {
      createSupplyDemand(aItemsBought, aItemsSold);
    } else {
      logger.log(Level.INFO, "Creating supply demand for already existing id: " + aId);
    }
    Economy.addSupplyDemand(this);
  }
  
  SupplyDemand(int aId, int aItemsBought, int aItemsSold, long aLastPolled)
  {
    this.id = aId;
    this.itemsBought = aItemsBought;
    this.itemsSold = aItemsSold;
    this.lastPolled = aLastPolled;
    Economy.addSupplyDemand(this);
  }
  
  public final float getDemandMod(int extraSold)
  {
    return Math.max(1000.0F, this.itemsSold) / Math.max(1000.0F, this.itemsBought + extraSold);
  }
  
  public final int getItemsBoughtByTraders()
  {
    return this.itemsBought;
  }
  
  public final int getItemsSoldByTraders()
  {
    return this.itemsSold;
  }
  
  final void addItemBoughtByTrader()
  {
    updateItemsBoughtByTraders(this.itemsBought + 1);
  }
  
  final void addItemSoldByTrader()
  {
    updateItemsSoldByTraders(this.itemsSold + 1);
  }
  
  public final int getId()
  {
    return this.id;
  }
  
  public final int getPool()
  {
    return this.itemsBought - this.itemsSold;
  }
  
  public final long getLastPolled()
  {
    return this.lastPolled;
  }
  
  abstract void updateItemsBoughtByTraders(int paramInt);
  
  abstract void updateItemsSoldByTraders(int paramInt);
  
  abstract void createSupplyDemand(int paramInt1, int paramInt2);
  
  abstract boolean supplyDemandExists();
  
  abstract void reset(long paramLong);
  
  public final String toString()
  {
    return 
      "SupplyDemand [TemplateID: " + this.id + ", Items bought:" + this.itemsBought + ", Sold:" + this.itemsSold + ", Pool: " + getPool() + ", Time last polled: " + this.lastPolled + ']';
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\economy\SupplyDemand.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */