package org.flywaydb.core.api;

import java.util.Date;

public abstract interface MigrationInfo
  extends Comparable<MigrationInfo>
{
  public abstract MigrationType getType();
  
  public abstract Integer getChecksum();
  
  public abstract MigrationVersion getVersion();
  
  public abstract String getDescription();
  
  public abstract String getScript();
  
  public abstract MigrationState getState();
  
  public abstract Date getInstalledOn();
  
  public abstract String getInstalledBy();
  
  public abstract Integer getInstalledRank();
  
  public abstract Integer getExecutionTime();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\flywaydb\core\api\MigrationInfo.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */