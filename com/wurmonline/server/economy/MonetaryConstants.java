package com.wurmonline.server.economy;

public abstract interface MonetaryConstants
{
  public static final int COIN_IRON = 1;
  public static final int COIN_COPPER = 100;
  public static final int COIN_SILVER = 10000;
  public static final int COIN_GOLD = 1000000;
  public static final int MAX_DISCARDMONEY_HOUR = 500;
  
  public static enum TransactionReason
  {
    Banked,  Charged,  Destroyed,  Notrade,  PersonalShop,  Sacrificed,  TraderShop;
    
    private TransactionReason() {}
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\economy\MonetaryConstants.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */