package org.flywaydb.core.internal.command;

import java.sql.Connection;
import java.sql.SQLException;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.callback.FlywayCallback;
import org.flywaydb.core.internal.dbsupport.DbSupport;
import org.flywaydb.core.internal.dbsupport.Schema;
import org.flywaydb.core.internal.metadatatable.MetaDataTable;
import org.flywaydb.core.internal.util.StopWatch;
import org.flywaydb.core.internal.util.TimeFormat;
import org.flywaydb.core.internal.util.jdbc.TransactionCallback;
import org.flywaydb.core.internal.util.jdbc.TransactionTemplate;
import org.flywaydb.core.internal.util.logging.Log;
import org.flywaydb.core.internal.util.logging.LogFactory;

public class DbClean
{
  private static final Log LOG = LogFactory.getLog(DbClean.class);
  private final Connection connection;
  private final MetaDataTable metaDataTable;
  private final Schema[] schemas;
  private final FlywayCallback[] callbacks;
  private boolean cleanDisabled;
  private final DbSupport dbSupport;
  
  public DbClean(Connection connection, DbSupport dbSupport, MetaDataTable metaDataTable, Schema[] schemas, FlywayCallback[] callbacks, boolean cleanDisabled)
  {
    this.connection = connection;
    this.dbSupport = dbSupport;
    this.metaDataTable = metaDataTable;
    this.schemas = schemas;
    this.callbacks = callbacks;
    this.cleanDisabled = cleanDisabled;
  }
  
  public void clean()
    throws FlywayException
  {
    if (this.cleanDisabled) {
      throw new FlywayException("Unable to execute clean as it has been disabled with the \"flyway.cleanDisabled\" property.");
    }
    try
    {
      FlywayCallback[] arrayOfFlywayCallback = this.callbacks;int i = arrayOfFlywayCallback.length;
      for (FlywayCallback localFlywayCallback1 = 0; localFlywayCallback1 < i; localFlywayCallback1++)
      {
        callback = arrayOfFlywayCallback[localFlywayCallback1];
        new TransactionTemplate(this.connection).execute(new TransactionCallback()
        {
          public Object doInTransaction()
            throws SQLException
          {
            DbClean.this.dbSupport.changeCurrentSchemaTo(DbClean.this.schemas[0]);
            callback.beforeClean(DbClean.this.connection);
            return null;
          }
        });
      }
      this.dbSupport.changeCurrentSchemaTo(this.schemas[0]);
      boolean dropSchemas = false;
      try
      {
        dropSchemas = this.metaDataTable.hasSchemasMarker();
      }
      catch (Exception e)
      {
        LOG.error("Error while checking whether the schemas should be dropped", (Exception)e);
      }
      e = this.schemas;localFlywayCallback1 = e.length;
      for (final FlywayCallback callback = 0; callback < localFlywayCallback1; callback++)
      {
        Schema schema = e[callback];
        if (!schema.exists()) {
          LOG.warn("Unable to clean unknown schema: " + schema);
        } else if (dropSchemas) {
          dropSchema(schema);
        } else {
          cleanSchema(schema);
        }
      }
      e = this.callbacks;FlywayCallback localFlywayCallback2 = e.length;
      for (callback = 0; callback < localFlywayCallback2; callback++)
      {
        final FlywayCallback callback = e[callback];
        new TransactionTemplate(this.connection).execute(new TransactionCallback()
        {
          public Object doInTransaction()
            throws SQLException
          {
            DbClean.this.dbSupport.changeCurrentSchemaTo(DbClean.this.schemas[0]);
            callback.afterClean(DbClean.this.connection);
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
  
  private void dropSchema(final Schema schema)
  {
    LOG.debug("Dropping schema " + schema + " ...");
    StopWatch stopWatch = new StopWatch();
    stopWatch.start();
    new TransactionTemplate(this.connection).execute(new TransactionCallback()
    {
      public Void doInTransaction()
      {
        schema.drop();
        return null;
      }
    });
    stopWatch.stop();
    LOG.info(String.format("Successfully dropped schema %s (execution time %s)", new Object[] { schema, 
      TimeFormat.format(stopWatch.getTotalTimeMillis()) }));
  }
  
  private void cleanSchema(final Schema schema)
  {
    LOG.debug("Cleaning schema " + schema + " ...");
    StopWatch stopWatch = new StopWatch();
    stopWatch.start();
    new TransactionTemplate(this.connection).execute(new TransactionCallback()
    {
      public Void doInTransaction()
      {
        schema.clean();
        return null;
      }
    });
    stopWatch.stop();
    LOG.info(String.format("Successfully cleaned schema %s (execution time %s)", new Object[] { schema, 
      TimeFormat.format(stopWatch.getTotalTimeMillis()) }));
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\flywaydb\core\internal\command\DbClean.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */