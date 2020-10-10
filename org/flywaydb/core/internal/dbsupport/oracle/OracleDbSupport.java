package org.flywaydb.core.internal.dbsupport.oracle;

import java.sql.Connection;
import java.sql.SQLException;
import org.flywaydb.core.internal.dbsupport.DbSupport;
import org.flywaydb.core.internal.dbsupport.JdbcTemplate;
import org.flywaydb.core.internal.dbsupport.Schema;
import org.flywaydb.core.internal.dbsupport.SqlStatementBuilder;

public class OracleDbSupport
  extends DbSupport
{
  public OracleDbSupport(Connection connection)
  {
    super(new JdbcTemplate(connection, 12));
  }
  
  public String getDbName()
  {
    return "oracle";
  }
  
  public String getCurrentUserFunction()
  {
    return "USER";
  }
  
  protected String doGetCurrentSchemaName()
    throws SQLException
  {
    return this.jdbcTemplate.queryForString("SELECT USER FROM dual", new String[0]);
  }
  
  protected void doChangeCurrentSchemaTo(String schema)
    throws SQLException
  {
    this.jdbcTemplate.execute("ALTER SESSION SET CURRENT_SCHEMA=" + schema, new Object[0]);
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
  
  public SqlStatementBuilder createSqlStatementBuilder()
  {
    return new OracleSqlStatementBuilder();
  }
  
  public String doQuote(String identifier)
  {
    return "\"" + identifier + "\"";
  }
  
  public Schema getSchema(String name)
  {
    return new OracleSchema(this.jdbcTemplate, this, name);
  }
  
  public boolean catalogIsSchema()
  {
    return false;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\flywaydb\core\internal\dbsupport\oracle\OracleDbSupport.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */