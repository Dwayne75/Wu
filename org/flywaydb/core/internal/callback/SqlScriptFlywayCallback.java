package org.flywaydb.core.internal.callback;

import java.sql.Connection;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.callback.FlywayCallback;
import org.flywaydb.core.internal.dbsupport.DbSupport;
import org.flywaydb.core.internal.dbsupport.JdbcTemplate;
import org.flywaydb.core.internal.dbsupport.SqlScript;
import org.flywaydb.core.internal.util.Location;
import org.flywaydb.core.internal.util.Locations;
import org.flywaydb.core.internal.util.PlaceholderReplacer;
import org.flywaydb.core.internal.util.logging.Log;
import org.flywaydb.core.internal.util.logging.LogFactory;
import org.flywaydb.core.internal.util.scanner.Resource;
import org.flywaydb.core.internal.util.scanner.Scanner;

public class SqlScriptFlywayCallback
  implements FlywayCallback
{
  private static final Log LOG = LogFactory.getLog(SqlScriptFlywayCallback.class);
  private static final String BEFORE_CLEAN = "beforeClean";
  private static final String AFTER_CLEAN = "afterClean";
  private static final String BEFORE_MIGRATE = "beforeMigrate";
  private static final String AFTER_MIGRATE = "afterMigrate";
  private static final String BEFORE_EACH_MIGRATE = "beforeEachMigrate";
  private static final String AFTER_EACH_MIGRATE = "afterEachMigrate";
  private static final String BEFORE_VALIDATE = "beforeValidate";
  private static final String AFTER_VALIDATE = "afterValidate";
  private static final String BEFORE_BASELINE = "beforeBaseline";
  private static final String AFTER_BASELINE = "afterBaseline";
  private static final String BEFORE_REPAIR = "beforeRepair";
  private static final String AFTER_REPAIR = "afterRepair";
  private static final String BEFORE_INFO = "beforeInfo";
  private static final String AFTER_INFO = "afterInfo";
  public static final List<String> ALL_CALLBACKS = Arrays.asList(new String[] { "beforeClean", "afterClean", "beforeMigrate", "beforeEachMigrate", "afterEachMigrate", "afterMigrate", "beforeValidate", "afterValidate", "beforeBaseline", "afterBaseline", "beforeRepair", "afterRepair", "beforeInfo", "afterInfo" });
  private final Map<String, SqlScript> scripts = new HashMap();
  
  public SqlScriptFlywayCallback(DbSupport dbSupport, Scanner scanner, Locations locations, PlaceholderReplacer placeholderReplacer, String encoding, String sqlMigrationSuffix)
  {
    for (String callback : ALL_CALLBACKS) {
      this.scripts.put(callback, null);
    }
    LOG.debug("Scanning for SQL callbacks ...");
    for (Location location : locations.getLocations())
    {
      Resource[] resources;
      try
      {
        resources = scanner.scanForResources(location, "", sqlMigrationSuffix);
      }
      catch (FlywayException e) {}
      continue;
      Resource[] resources;
      for (Resource resource : resources)
      {
        String key = resource.getFilename().replace(sqlMigrationSuffix, "");
        if (this.scripts.keySet().contains(key))
        {
          SqlScript existing = (SqlScript)this.scripts.get(key);
          if (existing != null) {
            throw new FlywayException("Found more than 1 SQL callback script for " + key + "!\n" + "Offenders:\n" + "-> " + existing.getResource().getLocationOnDisk() + "\n" + "-> " + resource.getLocationOnDisk());
          }
          this.scripts.put(key, new SqlScript(dbSupport, resource, placeholderReplacer, encoding));
        }
      }
    }
  }
  
  public void beforeClean(Connection connection)
  {
    execute("beforeClean", connection);
  }
  
  public void afterClean(Connection connection)
  {
    execute("afterClean", connection);
  }
  
  public void beforeMigrate(Connection connection)
  {
    execute("beforeMigrate", connection);
  }
  
  public void afterMigrate(Connection connection)
  {
    execute("afterMigrate", connection);
  }
  
  public void beforeEachMigrate(Connection connection, MigrationInfo info)
  {
    execute("beforeEachMigrate", connection);
  }
  
  public void afterEachMigrate(Connection connection, MigrationInfo info)
  {
    execute("afterEachMigrate", connection);
  }
  
  public void beforeValidate(Connection connection)
  {
    execute("beforeValidate", connection);
  }
  
  public void afterValidate(Connection connection)
  {
    execute("afterValidate", connection);
  }
  
  public void beforeBaseline(Connection connection)
  {
    execute("beforeBaseline", connection);
  }
  
  public void afterBaseline(Connection connection)
  {
    execute("afterBaseline", connection);
  }
  
  public void beforeRepair(Connection connection)
  {
    execute("beforeRepair", connection);
  }
  
  public void afterRepair(Connection connection)
  {
    execute("afterRepair", connection);
  }
  
  public void beforeInfo(Connection connection)
  {
    execute("beforeInfo", connection);
  }
  
  public void afterInfo(Connection connection)
  {
    execute("afterInfo", connection);
  }
  
  private void execute(String key, Connection connection)
  {
    SqlScript sqlScript = (SqlScript)this.scripts.get(key);
    if (sqlScript != null)
    {
      LOG.info("Executing SQL callback: " + key);
      sqlScript.execute(new JdbcTemplate(connection, 0));
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\flywaydb\core\internal\callback\SqlScriptFlywayCallback.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */