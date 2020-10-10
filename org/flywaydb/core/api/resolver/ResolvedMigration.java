package org.flywaydb.core.api.resolver;

import org.flywaydb.core.api.MigrationType;
import org.flywaydb.core.api.MigrationVersion;

public abstract interface ResolvedMigration
{
  public abstract MigrationVersion getVersion();
  
  public abstract String getDescription();
  
  public abstract String getScript();
  
  public abstract Integer getChecksum();
  
  public abstract MigrationType getType();
  
  public abstract String getPhysicalLocation();
  
  public abstract MigrationExecutor getExecutor();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\flywaydb\core\api\resolver\ResolvedMigration.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */