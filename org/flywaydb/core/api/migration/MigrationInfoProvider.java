package org.flywaydb.core.api.migration;

import org.flywaydb.core.api.MigrationVersion;

public abstract interface MigrationInfoProvider
{
  public abstract MigrationVersion getVersion();
  
  public abstract String getDescription();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\flywaydb\core\api\migration\MigrationInfoProvider.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */