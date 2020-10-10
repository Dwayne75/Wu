package org.flywaydb.core.internal.dbsupport.db2zos;

import java.sql.SQLException;
import org.flywaydb.core.internal.dbsupport.DbSupport;
import org.flywaydb.core.internal.dbsupport.Function;
import org.flywaydb.core.internal.dbsupport.JdbcTemplate;
import org.flywaydb.core.internal.dbsupport.Schema;
import org.flywaydb.core.internal.util.StringUtils;

public class DB2zosFunction
  extends Function
{
  public DB2zosFunction(JdbcTemplate jdbcTemplate, DbSupport dbSupport, Schema schema, String name, String... args)
  {
    super(jdbcTemplate, dbSupport, schema, name, args);
  }
  
  protected void doDrop()
    throws SQLException
  {
    this.jdbcTemplate.execute("DROP FUNCTION " + this.dbSupport
      .quote(new String[] {this.schema.getName(), this.name }) + "(" + 
      StringUtils.arrayToCommaDelimitedString(this.args) + ")", new Object[0]);
  }
  
  public String toString()
  {
    return super.toString() + "(" + StringUtils.arrayToCommaDelimitedString(this.args) + ")";
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\flywaydb\core\internal\dbsupport\db2zos\DB2zosFunction.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */