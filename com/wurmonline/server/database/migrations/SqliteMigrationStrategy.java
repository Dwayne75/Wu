package com.wurmonline.server.database.migrations;

import com.wurmonline.server.database.SqliteConnectionFactory;
import com.wurmonline.server.database.WurmDatabaseSchema;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;
import org.flywaydb.core.api.MigrationVersion;

public class SqliteMigrationStrategy
  implements MigrationStrategy
{
  private static final Logger logger = Logger.getLogger(SqliteMigrationStrategy.class.getName());
  private final List<SqliteMigrator> migrators;
  
  public SqliteMigrationStrategy(List<SqliteConnectionFactory> connectionFactories, Path migrationsDir)
  {
    List<SqliteMigrator> migrators = new ArrayList();
    for (SqliteConnectionFactory connectionFactory : connectionFactories)
    {
      Path migrationsDirForSchema = migrationsDir.resolve(connectionFactory.getSchema().getMigration());
      migrators.add(new SqliteMigrator(connectionFactory, migrationsDirForSchema));
    }
    this.migrators = migrators;
  }
  
  private void logErrors(Map<WurmDatabaseSchema, MigrationResult.MigrationSuccess> successfulMigrations)
  {
    if (successfulMigrations.size() == 0)
    {
      logger.warning("Cannot perform migrations, error encounted. No migrations performed successfully.");
    }
    else
    {
      logger.warning("Cannot continue migrations, error encountered. Migration in a partial state.");
      logger.warning("The following migrations were performed successfully:");
      for (Map.Entry<WurmDatabaseSchema, MigrationResult.MigrationSuccess> entry : successfulMigrations.entrySet())
      {
        MigrationResult.MigrationSuccess result = (MigrationResult.MigrationSuccess)entry.getValue();
        String before = result.getVersionBefore().getVersion();
        if (before == null) {
          before = "baseline";
        }
        logger.warning(((WurmDatabaseSchema)entry.getKey()).name() + " : performed " + result
          .getNumMigrations() + " migrations from " + before + " to " + result
          .getVersionAfter());
      }
    }
  }
  
  public boolean hasPendingMigrations()
  {
    for (SqliteMigrator migrator : this.migrators) {
      if (migrator.hasPendingMigrations()) {
        return true;
      }
    }
    return false;
  }
  
  public MigrationResult migrate()
  {
    MigrationVersion earliestVersion = MigrationVersion.LATEST;
    MigrationVersion latestVersion = MigrationVersion.EMPTY;
    int numMigrations = 0;
    LinkedHashMap<WurmDatabaseSchema, MigrationResult.MigrationSuccess> schemasMigrated = new LinkedHashMap();
    for (SqliteMigrator migrator : this.migrators)
    {
      MigrationResult result = migrator.migrate();
      if (result.isError())
      {
        logErrors(schemasMigrated);
        return result;
      }
      MigrationResult.MigrationSuccess success = result.asSuccess();
      schemasMigrated.put(migrator.getSchema(), success);
      if (latestVersion.compareTo(success.getVersionAfter()) < 0) {
        latestVersion = success.getVersionAfter();
      }
      MigrationVersion before = success.getVersionBefore();
      if (before == null) {
        earliestVersion = MigrationVersion.EMPTY;
      } else if (before.compareTo(earliestVersion) < 0) {
        earliestVersion = success.getVersionBefore();
      }
      numMigrations += success.getNumMigrations();
    }
    if (latestVersion == null)
    {
      String errorMessage = "Error encountered after performing migrations: could not determine latest version";
      logger.warning(errorMessage);
      return MigrationResult.newError(errorMessage);
    }
    return MigrationResult.newSuccess(earliestVersion, latestVersion, numMigrations);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\database\migrations\SqliteMigrationStrategy.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */