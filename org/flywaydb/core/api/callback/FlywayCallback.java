package org.flywaydb.core.api.callback;

import java.sql.Connection;
import org.flywaydb.core.api.MigrationInfo;

public abstract interface FlywayCallback
{
  public abstract void beforeClean(Connection paramConnection);
  
  public abstract void afterClean(Connection paramConnection);
  
  public abstract void beforeMigrate(Connection paramConnection);
  
  public abstract void afterMigrate(Connection paramConnection);
  
  public abstract void beforeEachMigrate(Connection paramConnection, MigrationInfo paramMigrationInfo);
  
  public abstract void afterEachMigrate(Connection paramConnection, MigrationInfo paramMigrationInfo);
  
  public abstract void beforeValidate(Connection paramConnection);
  
  public abstract void afterValidate(Connection paramConnection);
  
  public abstract void beforeBaseline(Connection paramConnection);
  
  public abstract void afterBaseline(Connection paramConnection);
  
  public abstract void beforeRepair(Connection paramConnection);
  
  public abstract void afterRepair(Connection paramConnection);
  
  public abstract void beforeInfo(Connection paramConnection);
  
  public abstract void afterInfo(Connection paramConnection);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\flywaydb\core\api\callback\FlywayCallback.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */