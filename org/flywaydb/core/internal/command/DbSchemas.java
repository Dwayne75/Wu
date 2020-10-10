package org.flywaydb.core.internal.command;

import java.sql.Connection;
import org.flywaydb.core.internal.dbsupport.Schema;
import org.flywaydb.core.internal.metadatatable.MetaDataTable;
import org.flywaydb.core.internal.util.jdbc.TransactionCallback;
import org.flywaydb.core.internal.util.jdbc.TransactionTemplate;
import org.flywaydb.core.internal.util.logging.Log;
import org.flywaydb.core.internal.util.logging.LogFactory;

public class DbSchemas
{
  private static final Log LOG = LogFactory.getLog(DbSchemas.class);
  private final Connection connection;
  private final Schema[] schemas;
  private final MetaDataTable metaDataTable;
  
  public DbSchemas(Connection connection, Schema[] schemas, MetaDataTable metaDataTable)
  {
    this.connection = connection;
    this.schemas = schemas;
    this.metaDataTable = metaDataTable;
  }
  
  public void create()
  {
    new TransactionTemplate(this.connection).execute(new TransactionCallback()
    {
      public Void doInTransaction()
      {
        for (Schema schema : DbSchemas.this.schemas) {
          if (schema.exists())
          {
            DbSchemas.LOG.debug("Schema " + schema + " already exists. Skipping schema creation.");
            return null;
          }
        }
        for (Schema schema : DbSchemas.this.schemas)
        {
          DbSchemas.LOG.info("Creating schema " + schema + " ...");
          schema.create();
        }
        DbSchemas.this.metaDataTable.addSchemasMarker(DbSchemas.this.schemas);
        
        return null;
      }
    });
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\flywaydb\core\internal\command\DbSchemas.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */