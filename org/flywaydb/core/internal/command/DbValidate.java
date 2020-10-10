package org.flywaydb.core.internal.command;

import java.sql.Connection;
import java.sql.SQLException;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.callback.FlywayCallback;
import org.flywaydb.core.api.resolver.MigrationResolver;
import org.flywaydb.core.internal.dbsupport.DbSupport;
import org.flywaydb.core.internal.dbsupport.Schema;
import org.flywaydb.core.internal.info.MigrationInfoServiceImpl;
import org.flywaydb.core.internal.metadatatable.MetaDataTable;
import org.flywaydb.core.internal.util.Pair;
import org.flywaydb.core.internal.util.StopWatch;
import org.flywaydb.core.internal.util.TimeFormat;
import org.flywaydb.core.internal.util.jdbc.TransactionCallback;
import org.flywaydb.core.internal.util.jdbc.TransactionTemplate;
import org.flywaydb.core.internal.util.logging.Log;
import org.flywaydb.core.internal.util.logging.LogFactory;

public class DbValidate
{
  private static final Log LOG = LogFactory.getLog(DbValidate.class);
  private final MigrationVersion target;
  private final MetaDataTable metaDataTable;
  private final Schema schema;
  private final MigrationResolver migrationResolver;
  private final Connection connection;
  private final boolean outOfOrder;
  private final boolean pending;
  private final boolean future;
  private final FlywayCallback[] callbacks;
  private final DbSupport dbSupport;
  
  public DbValidate(Connection connection, DbSupport dbSupport, MetaDataTable metaDataTable, Schema schema, MigrationResolver migrationResolver, MigrationVersion target, boolean outOfOrder, boolean pending, boolean future, FlywayCallback[] callbacks)
  {
    this.connection = connection;
    this.dbSupport = dbSupport;
    this.metaDataTable = metaDataTable;
    this.schema = schema;
    this.migrationResolver = migrationResolver;
    this.target = target;
    this.outOfOrder = outOfOrder;
    this.pending = pending;
    this.future = future;
    this.callbacks = callbacks;
  }
  
  public String validate()
  {
    try
    {
      for (final FlywayCallback callback : this.callbacks) {
        new TransactionTemplate(this.connection).execute(new TransactionCallback()
        {
          public Object doInTransaction()
            throws SQLException
          {
            DbValidate.this.dbSupport.changeCurrentSchemaTo(DbValidate.this.schema);
            callback.beforeValidate(DbValidate.this.connection);
            return null;
          }
        });
      }
      LOG.debug("Validating migrations ...");
      StopWatch stopWatch = new StopWatch();
      stopWatch.start();
      
      Object result = (Pair)new TransactionTemplate(this.connection).execute(new TransactionCallback()
      {
        public Pair<Integer, String> doInTransaction()
        {
          DbValidate.this.dbSupport.changeCurrentSchemaTo(DbValidate.this.schema);
          
          MigrationInfoServiceImpl migrationInfoService = new MigrationInfoServiceImpl(DbValidate.this.migrationResolver, DbValidate.this.metaDataTable, DbValidate.this.target, DbValidate.this.outOfOrder, DbValidate.this.pending, DbValidate.this.future);
          
          migrationInfoService.refresh();
          
          int count = migrationInfoService.all().length;
          String validationError = migrationInfoService.validate();
          return Pair.of(Integer.valueOf(count), validationError);
        }
      });
      stopWatch.stop();
      
      String error = (String)((Pair)result).getRight();
      int count;
      if (error == null)
      {
        count = ((Integer)((Pair)result).getLeft()).intValue();
        if (count == 1) {
          LOG.info(String.format("Successfully validated 1 migration (execution time %s)", new Object[] {
            TimeFormat.format(stopWatch.getTotalTimeMillis()) }));
        } else {
          LOG.info(String.format("Successfully validated %d migrations (execution time %s)", new Object[] {
            Integer.valueOf(count), TimeFormat.format(stopWatch.getTotalTimeMillis()) }));
        }
      }
      for (final FlywayCallback callback : this.callbacks) {
        new TransactionTemplate(this.connection).execute(new TransactionCallback()
        {
          public Object doInTransaction()
            throws SQLException
          {
            DbValidate.this.dbSupport.changeCurrentSchemaTo(DbValidate.this.schema);
            callback.afterValidate(DbValidate.this.connection);
            return null;
          }
        });
      }
      return error;
    }
    finally
    {
      this.dbSupport.restoreCurrentSchema();
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\flywaydb\core\internal\command\DbValidate.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */