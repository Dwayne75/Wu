package org.flywaydb.core.internal.metadatatable;

import java.util.List;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.internal.dbsupport.Schema;

public abstract interface MetaDataTable
{
  public abstract void lock();
  
  public abstract void addAppliedMigration(AppliedMigration paramAppliedMigration);
  
  public abstract boolean hasAppliedMigrations();
  
  public abstract List<AppliedMigration> allAppliedMigrations();
  
  public abstract void addBaselineMarker(MigrationVersion paramMigrationVersion, String paramString);
  
  public abstract boolean hasBaselineMarker();
  
  public abstract AppliedMigration getBaselineMarker();
  
  public abstract void removeFailedMigrations();
  
  public abstract void addSchemasMarker(Schema[] paramArrayOfSchema);
  
  public abstract boolean hasSchemasMarker();
  
  public abstract void updateChecksum(MigrationVersion paramMigrationVersion, Integer paramInteger);
  
  public abstract boolean upgradeIfNecessary();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\flywaydb\core\internal\metadatatable\MetaDataTable.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */