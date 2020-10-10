package com.wurmonline.server.database.migrations;

public abstract interface MigrationStrategy
{
  public abstract MigrationResult migrate();
  
  public abstract boolean hasPendingMigrations();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\database\migrations\MigrationStrategy.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */