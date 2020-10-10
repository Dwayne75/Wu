package org.flywaydb.core.api;

public abstract interface MigrationInfoService
{
  public abstract MigrationInfo[] all();
  
  public abstract MigrationInfo current();
  
  public abstract MigrationInfo[] pending();
  
  public abstract MigrationInfo[] applied();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\flywaydb\core\api\MigrationInfoService.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */