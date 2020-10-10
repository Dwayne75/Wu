package org.flywaydb.core.internal.command;

import java.sql.Connection;
import java.sql.SQLException;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.MigrationState;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.callback.FlywayCallback;
import org.flywaydb.core.api.resolver.MigrationExecutor;
import org.flywaydb.core.api.resolver.MigrationResolver;
import org.flywaydb.core.api.resolver.ResolvedMigration;
import org.flywaydb.core.internal.dbsupport.DbSupport;
import org.flywaydb.core.internal.dbsupport.DbSupportFactory;
import org.flywaydb.core.internal.dbsupport.Schema;
import org.flywaydb.core.internal.info.MigrationInfoImpl;
import org.flywaydb.core.internal.info.MigrationInfoServiceImpl;
import org.flywaydb.core.internal.metadatatable.AppliedMigration;
import org.flywaydb.core.internal.metadatatable.MetaDataTable;
import org.flywaydb.core.internal.util.StopWatch;
import org.flywaydb.core.internal.util.TimeFormat;
import org.flywaydb.core.internal.util.jdbc.TransactionCallback;
import org.flywaydb.core.internal.util.jdbc.TransactionTemplate;
import org.flywaydb.core.internal.util.logging.Log;
import org.flywaydb.core.internal.util.logging.LogFactory;

public class DbMigrate
{
  private static final Log LOG = LogFactory.getLog(DbMigrate.class);
  private final MigrationVersion target;
  private final DbSupport dbSupport;
  private final MetaDataTable metaDataTable;
  private final Schema schema;
  private final MigrationResolver migrationResolver;
  private final Connection connectionMetaDataTable;
  private final Connection connectionUserObjects;
  private final boolean ignoreFutureMigrations;
  private final boolean ignoreFailedFutureMigration;
  private final boolean outOfOrder;
  private final FlywayCallback[] callbacks;
  private final DbSupport dbSupportUserObjects;
  
  public DbMigrate(Connection connectionMetaDataTable, Connection connectionUserObjects, DbSupport dbSupport, MetaDataTable metaDataTable, Schema schema, MigrationResolver migrationResolver, MigrationVersion target, boolean ignoreFutureMigrations, boolean ignoreFailedFutureMigration, boolean outOfOrder, FlywayCallback[] callbacks)
  {
    this.connectionMetaDataTable = connectionMetaDataTable;
    this.connectionUserObjects = connectionUserObjects;
    this.dbSupport = dbSupport;
    this.metaDataTable = metaDataTable;
    this.schema = schema;
    this.migrationResolver = migrationResolver;
    this.target = target;
    this.ignoreFutureMigrations = ignoreFutureMigrations;
    this.ignoreFailedFutureMigration = ignoreFailedFutureMigration;
    this.outOfOrder = outOfOrder;
    this.callbacks = callbacks;
    
    this.dbSupportUserObjects = DbSupportFactory.createDbSupport(connectionUserObjects, false);
  }
  
  public int migrate()
    throws FlywayException
  {
    try
    {
      for (final FlywayCallback callback : this.callbacks) {
        new TransactionTemplate(this.connectionUserObjects).execute(new TransactionCallback()
        {
          public Object doInTransaction()
            throws SQLException
          {
            DbMigrate.this.dbSupportUserObjects.changeCurrentSchemaTo(DbMigrate.this.schema);
            callback.beforeMigrate(DbMigrate.this.connectionUserObjects);
            return null;
          }
        });
      }
      StopWatch stopWatch = new StopWatch();
      stopWatch.start();
      
      int migrationSuccessCount = 0;
      final boolean firstRun;
      for (;;)
      {
        firstRun = migrationSuccessCount == 0;
        done = ((Boolean)new TransactionTemplate(this.connectionMetaDataTable, false).execute(new TransactionCallback()
        {
          public Boolean doInTransaction()
          {
            DbMigrate.this.metaDataTable.lock();
            
            MigrationInfoServiceImpl infoService = new MigrationInfoServiceImpl(DbMigrate.this.migrationResolver, DbMigrate.this.metaDataTable, DbMigrate.this.target, DbMigrate.this.outOfOrder, true, true);
            infoService.refresh();
            
            MigrationVersion currentSchemaVersion = MigrationVersion.EMPTY;
            if (infoService.current() != null) {
              currentSchemaVersion = infoService.current().getVersion();
            }
            if (firstRun)
            {
              DbMigrate.LOG.info("Current version of schema " + DbMigrate.this.schema + ": " + currentSchemaVersion);
              if (DbMigrate.this.outOfOrder) {
                DbMigrate.LOG.warn("outOfOrder mode is active. Migration of schema " + DbMigrate.this.schema + " may not be reproducible.");
              }
            }
            MigrationInfo[] future = infoService.future();
            if (future.length > 0)
            {
              MigrationInfo[] resolved = infoService.resolved();
              if (resolved.length == 0)
              {
                DbMigrate.LOG.warn("Schema " + DbMigrate.this.schema + " has version " + currentSchemaVersion + ", but no migration could be resolved in the configured locations !");
              }
              else
              {
                int offset = resolved.length - 1;
                while (resolved[offset].getVersion() == null) {
                  offset--;
                }
                DbMigrate.LOG.warn("Schema " + DbMigrate.this.schema + " has a version (" + currentSchemaVersion + ") that is newer than the latest available migration (" + resolved[offset]
                
                  .getVersion() + ") !");
              }
            }
            MigrationInfo[] failed = infoService.failed();
            if (failed.length > 0) {
              if ((failed.length == 1) && 
                (failed[0].getState() == MigrationState.FUTURE_FAILED) && (
                (DbMigrate.this.ignoreFutureMigrations) || (DbMigrate.this.ignoreFailedFutureMigration))) {
                DbMigrate.LOG.warn("Schema " + DbMigrate.this.schema + " contains a failed future migration to version " + failed[0].getVersion() + " !");
              } else {
                throw new FlywayException("Schema " + DbMigrate.this.schema + " contains a failed migration to version " + failed[0].getVersion() + " !");
              }
            }
            MigrationInfoImpl[] pendingMigrations = infoService.pending();
            if (pendingMigrations.length == 0) {
              return Boolean.valueOf(true);
            }
            boolean isOutOfOrder = (pendingMigrations[0].getVersion() != null) && (pendingMigrations[0].getVersion().compareTo(currentSchemaVersion) < 0);
            return DbMigrate.this.applyMigration(pendingMigrations[0], isOutOfOrder);
          }
        })).booleanValue();
        if (done) {
          break;
        }
        migrationSuccessCount++;
      }
      stopWatch.stop();
      
      logSummary(migrationSuccessCount, stopWatch.getTotalTimeMillis());
      
      FlywayCallback[] arrayOfFlywayCallback2 = this.callbacks;boolean done = arrayOfFlywayCallback2.length;
      for (boolean bool1 = false; bool1 < done; bool1++)
      {
        final FlywayCallback callback = arrayOfFlywayCallback2[bool1];
        new TransactionTemplate(this.connectionUserObjects).execute(new TransactionCallback()
        {
          public Object doInTransaction()
            throws SQLException
          {
            DbMigrate.this.dbSupportUserObjects.changeCurrentSchemaTo(DbMigrate.this.schema);
            callback.afterMigrate(DbMigrate.this.connectionUserObjects);
            return null;
          }
        });
      }
      return migrationSuccessCount;
    }
    finally
    {
      this.dbSupportUserObjects.restoreCurrentSchema();
    }
  }
  
  private void logSummary(int migrationSuccessCount, long executionTime)
  {
    if (migrationSuccessCount == 0)
    {
      LOG.info("Schema " + this.schema + " is up to date. No migration necessary.");
      return;
    }
    if (migrationSuccessCount == 1) {
      LOG.info("Successfully applied 1 migration to schema " + this.schema + " (execution time " + TimeFormat.format(executionTime) + ").");
    } else {
      LOG.info("Successfully applied " + migrationSuccessCount + " migrations to schema " + this.schema + " (execution time " + TimeFormat.format(executionTime) + ").");
    }
  }
  
  private Boolean applyMigration(final MigrationInfoImpl migration, boolean isOutOfOrder)
  {
    MigrationVersion version = migration.getVersion();
    String migrationText;
    final String migrationText;
    if (version != null) {
      migrationText = "schema " + this.schema + " to version " + version + " - " + migration.getDescription() + (isOutOfOrder ? " (out of order)" : "");
    } else {
      migrationText = "schema " + this.schema + " with repeatable migration " + migration.getDescription();
    }
    LOG.info("Migrating " + migrationText);
    
    StopWatch stopWatch = new StopWatch();
    stopWatch.start();
    try
    {
      final MigrationExecutor migrationExecutor = migration.getResolvedMigration().getExecutor();
      if (migrationExecutor.executeInTransaction()) {
        new TransactionTemplate(this.connectionUserObjects).execute(new TransactionCallback()
        {
          public Object doInTransaction()
            throws SQLException
          {
            DbMigrate.this.doMigrate(migration, migrationExecutor, migrationText);
            return null;
          }
        });
      } else {
        try
        {
          doMigrate(migration, migrationExecutor, migrationText);
        }
        catch (SQLException e)
        {
          throw new FlywayException("Unable to apply migration", e);
        }
      }
    }
    catch (FlywayException e)
    {
      String failedMsg = "Migration of " + migrationText + " failed!";
      if (this.dbSupport.supportsDdlTransactions())
      {
        LOG.error(failedMsg + " Changes successfully rolled back.");
      }
      else
      {
        LOG.error(failedMsg + " Please restore backups and roll back database and code!");
        
        stopWatch.stop();
        int executionTime = (int)stopWatch.getTotalTimeMillis();
        
        AppliedMigration appliedMigration = new AppliedMigration(version, migration.getDescription(), migration.getType(), migration.getScript(), migration.getResolvedMigration().getChecksum(), executionTime, false);
        this.metaDataTable.addAppliedMigration(appliedMigration);
      }
      throw e;
    }
    stopWatch.stop();
    int executionTime = (int)stopWatch.getTotalTimeMillis();
    
    AppliedMigration appliedMigration = new AppliedMigration(version, migration.getDescription(), migration.getType(), migration.getScript(), migration.getResolvedMigration().getChecksum(), executionTime, true);
    this.metaDataTable.addAppliedMigration(appliedMigration);
    
    return Boolean.valueOf(false);
  }
  
  private void doMigrate(MigrationInfoImpl migration, MigrationExecutor migrationExecutor, String migrationText)
    throws SQLException
  {
    this.dbSupportUserObjects.changeCurrentSchemaTo(this.schema);
    for (FlywayCallback callback : this.callbacks) {
      callback.beforeEachMigrate(this.connectionUserObjects, migration);
    }
    migrationExecutor.execute(this.connectionUserObjects);
    LOG.debug("Successfully completed migration of " + migrationText);
    for (FlywayCallback callback : this.callbacks) {
      callback.afterEachMigrate(this.connectionUserObjects, migration);
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\flywaydb\core\internal\command\DbMigrate.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */