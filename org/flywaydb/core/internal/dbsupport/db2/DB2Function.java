package org.flywaydb.core.internal.dbsupport.db2;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import org.flywaydb.core.internal.dbsupport.DbSupport;
import org.flywaydb.core.internal.dbsupport.Function;
import org.flywaydb.core.internal.dbsupport.JdbcTemplate;
import org.flywaydb.core.internal.dbsupport.Schema;
import org.flywaydb.core.internal.util.StringUtils;

public class DB2Function
  extends Function
{
  private static final Collection<String> typesWithLength = Arrays.asList(new String[] { "character", "char", "varchar", "graphic", "vargraphic", "decimal", "float", "varbinary" });
  
  public DB2Function(JdbcTemplate jdbcTemplate, DbSupport dbSupport, Schema schema, String name, String... args)
  {
    super(jdbcTemplate, dbSupport, schema, name, args);
  }
  
  protected void doDrop()
    throws SQLException
  {
    try
    {
      this.jdbcTemplate.execute("DROP FUNCTION " + this.dbSupport
        .quote(new String[] {this.schema.getName(), this.name }) + "(" + 
        argsToCommaSeparatedString(this.args) + ")", new Object[0]);
    }
    catch (SQLException e)
    {
      this.jdbcTemplate.execute("DROP FUNCTION " + this.dbSupport.quote(new String[] { this.schema.getName(), this.name }), new Object[0]);
    }
  }
  
  private String argsToCommaSeparatedString(String[] args)
  {
    StringBuilder buf = new StringBuilder();
    for (String arg : args)
    {
      if (buf.length() > 0) {
        buf.append(",");
      }
      buf.append(arg);
      if (typesWithLength.contains(arg.toLowerCase())) {
        buf.append("()");
      }
    }
    return buf.toString();
  }
  
  public String toString()
  {
    return super.toString() + "(" + StringUtils.arrayToCommaDelimitedString(this.args) + ")";
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\flywaydb\core\internal\dbsupport\db2\DB2Function.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */