package org.flywaydb.core.internal.dbsupport.redshift;

import java.sql.SQLException;
import org.flywaydb.core.internal.dbsupport.DbSupport;
import org.flywaydb.core.internal.dbsupport.JdbcTemplate;
import org.flywaydb.core.internal.dbsupport.Schema;
import org.flywaydb.core.internal.dbsupport.Table;

public class RedshiftTable
  extends Table
{
  public RedshiftTable(JdbcTemplate jdbcTemplate, DbSupport dbSupport, Schema schema, String name)
  {
    super(jdbcTemplate, dbSupport, schema, name);
  }
  
  protected void doDrop()
    throws SQLException
  {
    this.jdbcTemplate.execute("DROP TABLE " + this.dbSupport.quote(new String[] { this.schema.getName(), this.name }) + " CASCADE", new Object[0]);
  }
  
  protected boolean doExists()
    throws SQLException
  {
    return exists(null, this.schema, this.name, new String[0]);
  }
  
  protected void doLock()
    throws SQLException
  {
    this.jdbcTemplate.execute("DELETE " + this + " WHERE 1=2", new Object[0]);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\flywaydb\core\internal\dbsupport\redshift\RedshiftTable.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */