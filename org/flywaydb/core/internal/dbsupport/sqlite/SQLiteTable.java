package org.flywaydb.core.internal.dbsupport.sqlite;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import org.flywaydb.core.internal.dbsupport.DbSupport;
import org.flywaydb.core.internal.dbsupport.JdbcTemplate;
import org.flywaydb.core.internal.dbsupport.Schema;
import org.flywaydb.core.internal.dbsupport.Table;
import org.flywaydb.core.internal.util.logging.Log;
import org.flywaydb.core.internal.util.logging.LogFactory;

public class SQLiteTable
  extends Table
{
  private static final Log LOG = LogFactory.getLog(SQLiteTable.class);
  private static final Collection<String> SYSTEM_TABLES = Collections.singleton("sqlite_sequence");
  private final boolean undroppable;
  
  public SQLiteTable(JdbcTemplate jdbcTemplate, DbSupport dbSupport, Schema schema, String name)
  {
    super(jdbcTemplate, dbSupport, schema, name);
    this.undroppable = SYSTEM_TABLES.contains(name);
  }
  
  protected void doDrop()
    throws SQLException
  {
    if (this.undroppable) {
      LOG.debug("SQLite system table " + this + " cannot be dropped. Ignoring.");
    } else {
      this.jdbcTemplate.execute("DROP TABLE " + this.dbSupport.quote(new String[] { this.schema.getName(), this.name }), new Object[0]);
    }
  }
  
  protected boolean doExists()
    throws SQLException
  {
    return this.jdbcTemplate.queryForInt("SELECT count(tbl_name) FROM " + this.dbSupport
      .quote(new String[] {this.schema.getName() }) + ".sqlite_master WHERE type='table' AND tbl_name='" + this.name + "'", new String[0]) > 0;
  }
  
  protected void doLock()
    throws SQLException
  {
    LOG.debug("Unable to lock " + this + " as SQLite does not support locking. No concurrent migration supported.");
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\flywaydb\core\internal\dbsupport\sqlite\SQLiteTable.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */