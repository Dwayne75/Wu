package org.flywaydb.core.internal.dbsupport.solid;

import java.sql.Connection;
import java.sql.SQLException;
import org.flywaydb.core.internal.dbsupport.DbSupport;
import org.flywaydb.core.internal.dbsupport.JdbcTemplate;
import org.flywaydb.core.internal.dbsupport.Schema;
import org.flywaydb.core.internal.dbsupport.SqlStatementBuilder;

public class SolidDbSupport
  extends DbSupport
{
  public SolidDbSupport(Connection connection)
  {
    super(new JdbcTemplate(connection, 0));
  }
  
  public Schema getSchema(String name)
  {
    return new SolidSchema(this.jdbcTemplate, this, name);
  }
  
  public SqlStatementBuilder createSqlStatementBuilder()
  {
    return new SolidSqlStatementBuilder();
  }
  
  public String getDbName()
  {
    return "solid";
  }
  
  protected String doGetCurrentSchemaName()
    throws SQLException
  {
    return this.jdbcTemplate.queryForString("SELECT CURRENT_SCHEMA()", new String[0]);
  }
  
  protected void doChangeCurrentSchemaTo(String schema)
    throws SQLException
  {
    this.jdbcTemplate.execute("SET SCHEMA " + schema, new Object[0]);
  }
  
  public String getCurrentUserFunction()
  {
    return "LOGIN_SCHEMA()";
  }
  
  public boolean supportsDdlTransactions()
  {
    return true;
  }
  
  public String getBooleanTrue()
  {
    return "1";
  }
  
  public String getBooleanFalse()
  {
    return "0";
  }
  
  protected String doQuote(String identifier)
  {
    return "\"" + identifier + "\"";
  }
  
  public boolean catalogIsSchema()
  {
    return false;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\flywaydb\core\internal\dbsupport\solid\SolidDbSupport.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */