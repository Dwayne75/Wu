package org.flywaydb.core.internal.dbsupport.saphana;

import java.sql.Connection;
import java.sql.SQLException;
import org.flywaydb.core.internal.dbsupport.DbSupport;
import org.flywaydb.core.internal.dbsupport.JdbcTemplate;
import org.flywaydb.core.internal.dbsupport.Schema;
import org.flywaydb.core.internal.dbsupport.SqlStatementBuilder;

public class SapHanaDbSupport
  extends DbSupport
{
  public SapHanaDbSupport(Connection connection)
  {
    super(new JdbcTemplate(connection, 12));
  }
  
  public SqlStatementBuilder createSqlStatementBuilder()
  {
    return new SapHanaSqlStatementBuilder();
  }
  
  public String getDbName()
  {
    return "saphana";
  }
  
  protected String doGetCurrentSchemaName()
    throws SQLException
  {
    return this.jdbcTemplate.queryForString("SELECT CURRENT_SCHEMA FROM DUMMY", new String[0]);
  }
  
  protected void doChangeCurrentSchemaTo(String schema)
    throws SQLException
  {
    this.jdbcTemplate.execute("SET SCHEMA " + schema, new Object[0]);
  }
  
  public String getCurrentUserFunction()
  {
    return "CURRENT_USER";
  }
  
  public boolean supportsDdlTransactions()
  {
    return false;
  }
  
  public String getBooleanTrue()
  {
    return "1";
  }
  
  public String getBooleanFalse()
  {
    return "0";
  }
  
  public String doQuote(String identifier)
  {
    return "\"" + identifier + "\"";
  }
  
  public Schema getSchema(String name)
  {
    return new SapHanaSchema(this.jdbcTemplate, this, name);
  }
  
  public boolean catalogIsSchema()
  {
    return false;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\flywaydb\core\internal\dbsupport\saphana\SapHanaDbSupport.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */