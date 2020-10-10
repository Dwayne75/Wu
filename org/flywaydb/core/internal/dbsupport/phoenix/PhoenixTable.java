package org.flywaydb.core.internal.dbsupport.phoenix;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.flywaydb.core.internal.dbsupport.DbSupport;
import org.flywaydb.core.internal.dbsupport.JdbcTemplate;
import org.flywaydb.core.internal.dbsupport.Schema;
import org.flywaydb.core.internal.dbsupport.Table;
import org.flywaydb.core.internal.util.logging.Log;
import org.flywaydb.core.internal.util.logging.LogFactory;

public class PhoenixTable
  extends Table
{
  private static final Log LOG = LogFactory.getLog(PhoenixTable.class);
  
  public PhoenixTable(JdbcTemplate jdbcTemplate, DbSupport dbSupport, Schema schema, String name)
  {
    super(jdbcTemplate, dbSupport, schema, name);
  }
  
  protected void doDrop()
    throws SQLException
  {
    this.jdbcTemplate.execute("DROP TABLE " + this.dbSupport.quote(new String[] { this.schema.getName(), this.name }), new Object[0]);
  }
  
  protected boolean doExists()
    throws SQLException
  {
    ResultSet rs = this.jdbcTemplate.getMetaData().getTables(null, this.schema.getName(), this.name, new String[] { "TABLE" });
    if (rs.next())
    {
      String tableName = rs.getString("TABLE_NAME");
      if (tableName != null) {
        return tableName.equals(this.name);
      }
    }
    return false;
  }
  
  protected void doLock()
    throws SQLException
  {
    LOG.debug("Unable to lock " + this + " as Phoenix does not support locking. No concurrent migration supported.");
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\flywaydb\core\internal\dbsupport\phoenix\PhoenixTable.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */