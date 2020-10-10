package org.flywaydb.core.internal.command;

import java.sql.Connection;
import java.sql.SQLException;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.callback.FlywayCallback;
import org.flywaydb.core.internal.dbsupport.DbSupport;
import org.flywaydb.core.internal.dbsupport.Schema;
import org.flywaydb.core.internal.metadatatable.AppliedMigration;
import org.flywaydb.core.internal.metadatatable.MetaDataTable;
import org.flywaydb.core.internal.util.jdbc.TransactionCallback;
import org.flywaydb.core.internal.util.jdbc.TransactionTemplate;
import org.flywaydb.core.internal.util.logging.Log;
import org.flywaydb.core.internal.util.logging.LogFactory;

public class DbBaseline
{
  private static final Log LOG = LogFactory.getLog(DbBaseline.class);
  private final Connection connection;
  private final MetaDataTable metaDataTable;
  private final MigrationVersion baselineVersion;
  private final String baselineDescription;
  private final FlywayCallback[] callbacks;
  private final DbSupport dbSupport;
  private final Schema schema;
  
  public DbBaseline(Connection connection, DbSupport dbSupport, MetaDataTable metaDataTable, Schema schema, MigrationVersion baselineVersion, String baselineDescription, FlywayCallback[] callbacks)
  {
    this.connection = connection;
    this.dbSupport = dbSupport;
    this.metaDataTable = metaDataTable;
    this.schema = schema;
    this.baselineVersion = baselineVersion;
    this.baselineDescription = baselineDescription;
    this.callbacks = callbacks;
  }
  
  public void baseline()
  {
    try
    {
      for (final FlywayCallback callback : this.callbacks) {
        new TransactionTemplate(this.connection).execute(new TransactionCallback()
        {
          public Object doInTransaction()
            throws SQLException
          {
            DbBaseline.this.dbSupport.changeCurrentSchemaTo(DbBaseline.this.schema);
            callback.beforeBaseline(DbBaseline.this.connection);
            return null;
          }
        });
      }
      new TransactionTemplate(this.connection).execute(new TransactionCallback()
      {
        public Void doInTransaction()
        {
          DbBaseline.this.dbSupport.changeCurrentSchemaTo(DbBaseline.this.schema);
          if (DbBaseline.this.metaDataTable.hasAppliedMigrations()) {
            throw new FlywayException("Unable to baseline metadata table " + DbBaseline.this.metaDataTable + " as it already contains migrations");
          }
          if (DbBaseline.this.metaDataTable.hasBaselineMarker())
          {
            AppliedMigration baselineMarker = DbBaseline.this.metaDataTable.getBaselineMarker();
            if ((DbBaseline.this.baselineVersion.equals(baselineMarker.getVersion())) && 
              (DbBaseline.this.baselineDescription.equals(baselineMarker.getDescription())))
            {
              DbBaseline.LOG.info("Metadata table " + DbBaseline.this.metaDataTable + " already initialized with (" + 
                DbBaseline.this.baselineVersion + "," + DbBaseline.this.baselineDescription + "). Skipping.");
              return null;
            }
            throw new FlywayException("Unable to baseline metadata table " + DbBaseline.this.metaDataTable + " with (" + DbBaseline.this.baselineVersion + "," + DbBaseline.this.baselineDescription + ") as it has already been initialized with (" + baselineMarker.getVersion() + "," + baselineMarker.getDescription() + ")");
          }
          if ((DbBaseline.this.metaDataTable.hasSchemasMarker()) && (DbBaseline.this.baselineVersion.equals(MigrationVersion.fromVersion("0")))) {
            throw new FlywayException("Unable to baseline metadata table " + DbBaseline.this.metaDataTable + " with version 0 as this version was used for schema creation");
          }
          DbBaseline.this.metaDataTable.addBaselineMarker(DbBaseline.this.baselineVersion, DbBaseline.this.baselineDescription);
          
          return null;
        }
      });
      LOG.info("Successfully baselined schema with version: " + this.baselineVersion);
      for (final FlywayCallback callback : this.callbacks) {
        new TransactionTemplate(this.connection).execute(new TransactionCallback()
        {
          public Object doInTransaction()
            throws SQLException
          {
            DbBaseline.this.dbSupport.changeCurrentSchemaTo(DbBaseline.this.schema);
            callback.afterBaseline(DbBaseline.this.connection);
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
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\flywaydb\core\internal\command\DbBaseline.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */