package org.flywaydb.core.api.configuration;

import java.util.Map;
import javax.sql.DataSource;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.callback.FlywayCallback;
import org.flywaydb.core.api.resolver.MigrationResolver;

public abstract interface FlywayConfiguration
{
  public abstract ClassLoader getClassLoader();
  
  public abstract DataSource getDataSource();
  
  public abstract MigrationVersion getBaselineVersion();
  
  public abstract String getBaselineDescription();
  
  public abstract MigrationResolver[] getResolvers();
  
  public abstract boolean isSkipDefaultResolvers();
  
  public abstract FlywayCallback[] getCallbacks();
  
  public abstract boolean isSkipDefaultCallbacks();
  
  public abstract String getSqlMigrationSuffix();
  
  public abstract String getRepeatableSqlMigrationPrefix();
  
  public abstract String getSqlMigrationSeparator();
  
  public abstract String getSqlMigrationPrefix();
  
  public abstract boolean isPlaceholderReplacement();
  
  public abstract String getPlaceholderSuffix();
  
  public abstract String getPlaceholderPrefix();
  
  public abstract Map<String, String> getPlaceholders();
  
  public abstract MigrationVersion getTarget();
  
  public abstract String getTable();
  
  public abstract String[] getSchemas();
  
  public abstract String getEncoding();
  
  public abstract String[] getLocations();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\flywaydb\core\api\configuration\FlywayConfiguration.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */