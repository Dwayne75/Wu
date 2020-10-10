package com.wurmonline.server.database;

import javax.annotation.concurrent.Immutable;

@Immutable
public enum WurmDatabaseSchema
{
  CREATURES("WURMCREATURES", "creatures"),  DEITIES("WURMDEITIES", "deities"),  ECONOMY("WURMECONOMY", "economy"),  ITEMS("WURMITEMS", "items"),  LOGIN("WURMLOGIN", "login"),  LOGS("WURMLOGS", "logs"),  PLAYERS("WURMPLAYERS", "players"),  TEMPLATES("WURMTEMPLATES", "templates"),  ZONES("WURMZONES", "zones");
  
  private final String database;
  private final String migration;
  
  private WurmDatabaseSchema(String database, String migration)
  {
    this.database = database;
    this.migration = migration;
  }
  
  public String getDatabase()
  {
    return this.database;
  }
  
  public String getMigration()
  {
    return this.migration;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\database\WurmDatabaseSchema.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */