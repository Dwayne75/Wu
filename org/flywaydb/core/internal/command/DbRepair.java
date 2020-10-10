package org.flywaydb.core.internal.command;

import java.sql.Connection;
import java.sql.SQLException;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.callback.FlywayCallback;
import org.flywaydb.core.api.resolver.MigrationResolver;
import org.flywaydb.core.api.resolver.ResolvedMigration;
import org.flywaydb.core.internal.dbsupport.DbSupport;
import org.flywaydb.core.internal.dbsupport.Schema;
import org.flywaydb.core.internal.info.MigrationInfoImpl;
import org.flywaydb.core.internal.info.MigrationInfoServiceImpl;
import org.flywaydb.core.internal.metadatatable.AppliedMigration;
import org.flywaydb.core.internal.metadatatable.MetaDataTable;
import org.flywaydb.core.internal.util.ObjectUtils;
import org.flywaydb.core.internal.util.StopWatch;
import org.flywaydb.core.internal.util.TimeFormat;
import org.flywaydb.core.internal.util.jdbc.TransactionCallback;
import org.flywaydb.core.internal.util.jdbc.TransactionTemplate;
import org.flywaydb.core.internal.util.logging.Log;
import org.flywaydb.core.internal.util.logging.LogFactory;

public class DbRepair
{
  private static final Log LOG = LogFactory.getLog(DbRepair.class);
  private final Connection connection;
  private final MigrationInfoServiceImpl migrationInfoService;
  private final Schema schema;
  private final MetaDataTable metaDataTable;
  private final FlywayCallback[] callbacks;
  private final DbSupport dbSupport;
  
  public DbRepair(DbSupport dbSupport, Connection connection, Schema schema, MigrationResolver migrationResolver, MetaDataTable metaDataTable, FlywayCallback[] callbacks)
  {
    this.dbSupport = dbSupport;
    this.connection = connection;
    this.schema = schema;
    this.migrationInfoService = new MigrationInfoServiceImpl(migrationResolver, metaDataTable, MigrationVersion.LATEST, true, true, true);
    this.metaDataTable = metaDataTable;
    this.callbacks = callbacks;
  }
  
  public void repair()
  {
    try
    {
      FlywayCallback[] arrayOfFlywayCallback1 = this.callbacks;int i = arrayOfFlywayCallback1.length;
      for (FlywayCallback localFlywayCallback1 = 0; localFlywayCallback1 < i; localFlywayCallback1++)
      {
        callback = arrayOfFlywayCallback1[localFlywayCallback1];
        new TransactionTemplate(this.connection).execute(new TransactionCallback()
        {
          public Object doInTransaction()
            throws SQLException
          {
            DbRepair.this.dbSupport.changeCurrentSchemaTo(DbRepair.this.schema);
            callback.beforeRepair(DbRepair.this.connection);
            return null;
          }
        });
      }
      StopWatch stopWatch = new StopWatch();
      stopWatch.start();
      
      new TransactionTemplate(this.connection).execute(new TransactionCallback()
      {
        public Void doInTransaction()
        {
          DbRepair.this.dbSupport.changeCurrentSchemaTo(DbRepair.this.schema);
          DbRepair.this.metaDataTable.removeFailedMigrations();
          DbRepair.this.repairChecksums();
          return null;
        }
      });
      stopWatch.stop();
      
      LOG.info("Successfully repaired metadata table " + this.metaDataTable + " (execution time " + 
        TimeFormat.format(stopWatch.getTotalTimeMillis()) + ").");
      if (!this.dbSupport.supportsDdlTransactions()) {
        LOG.info("Manual cleanup of the remaining effects the failed migration may still be required.");
      }
      FlywayCallback[] arrayOfFlywayCallback2 = this.callbacks;localFlywayCallback1 = arrayOfFlywayCallback2.length;
      for (final FlywayCallback callback = 0; callback < localFlywayCallback1; callback++)
      {
        final FlywayCallback callback = arrayOfFlywayCallback2[callback];
        new TransactionTemplate(this.connection).execute(new TransactionCallback()
        {
          public Object doInTransaction()
            throws SQLException
          {
            DbRepair.this.dbSupport.changeCurrentSchemaTo(DbRepair.this.schema);
            callback.afterRepair(DbRepair.this.connection);
            return null;
          }
        });
      }
    }
    finally
    {
      this.dbSupport.restoreCurrentSchema();
    }
  }
  
  public void repairChecksums()
  {
    this.migrationInfoService.refresh();
    for (MigrationInfo migrationInfo : this.migrationInfoService.all())
    {
      MigrationInfoImpl migrationInfoImpl = (MigrationInfoImpl)migrationInfo;
      
      ResolvedMigration resolved = migrationInfoImpl.getResolvedMigration();
      AppliedMigration applied = migrationInfoImpl.getAppliedMigration();
      if ((resolved != null) && (applied != null) && 
        (!ObjectUtils.nullSafeEquals(resolved.getChecksum(), applied.getChecksum())) && 
        (resolved.getVersion() != null)) {
        this.metaDataTable.updateChecksum(migrationInfoImpl.getVersion(), resolved.getChecksum());
      }
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\flywaydb\core\internal\command\DbRepair.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */