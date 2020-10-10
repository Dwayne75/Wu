package org.flywaydb.core.internal.dbsupport.sybase.ase;

import java.sql.SQLException;
import org.flywaydb.core.internal.dbsupport.DbSupport;
import org.flywaydb.core.internal.dbsupport.JdbcTemplate;
import org.flywaydb.core.internal.dbsupport.Schema;
import org.flywaydb.core.internal.dbsupport.Table;

public class SybaseASETable
  extends Table
{
  public SybaseASETable(JdbcTemplate jdbcTemplate, DbSupport dbSupport, Schema schema, String name)
  {
    super(jdbcTemplate, dbSupport, schema, name);
  }
  
  protected boolean doExists()
    throws SQLException
  {
    return exists(null, getSchema(), getName(), new String[0]);
  }
  
  protected void doLock()
    throws SQLException
  {
    this.jdbcTemplate.execute("LOCK TABLE " + this + " IN EXCLUSIVE MODE WAIT 10", new Object[0]);
  }
  
  protected void doDrop()
    throws SQLException
  {
    this.jdbcTemplate.execute("DROP TABLE " + getName(), new Object[0]);
  }
  
  public String toString()
  {
    return this.name;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\flywaydb\core\internal\dbsupport\sybase\ase\SybaseASETable.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */