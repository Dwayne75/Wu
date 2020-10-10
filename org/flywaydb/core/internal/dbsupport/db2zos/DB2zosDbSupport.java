package org.flywaydb.core.internal.dbsupport.db2zos;

import java.sql.Connection;
import java.sql.SQLException;
import org.flywaydb.core.internal.dbsupport.DbSupport;
import org.flywaydb.core.internal.dbsupport.JdbcTemplate;
import org.flywaydb.core.internal.dbsupport.Schema;
import org.flywaydb.core.internal.dbsupport.SqlStatementBuilder;

public class DB2zosDbSupport
  extends DbSupport
{
  public DB2zosDbSupport(Connection connection)
  {
    super(new JdbcTemplate(connection, 12));
  }
  
  public String getDbName()
  {
    return "db2zos";
  }
  
  public SqlStatementBuilder createSqlStatementBuilder()
  {
    return new DB2zosSqlStatementBuilder();
  }
  
  public String getScriptLocation()
  {
    return "com/googlecode/flyway/core/dbsupport/db2zos/";
  }
  
  protected String doGetCurrentSchemaName()
    throws SQLException
  {
    return this.jdbcTemplate.queryForString("select current_schema from sysibm.sysdummy1", new String[0]);
  }
  
  protected void doChangeCurrentSchemaTo(String schema)
    throws SQLException
  {
    this.jdbcTemplate.execute("SET SCHEMA " + schema, new Object[0]);
  }
  
  public String getCurrentUserFunction()
  {
    return "USER";
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
  
  public String doQuote(String identifier)
  {
    return "\"" + identifier + "\"";
  }
  
  public Schema getSchema(String name)
  {
    return new DB2zosSchema(this.jdbcTemplate, this, name);
  }
  
  public boolean catalogIsSchema()
  {
    return false;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\flywaydb\core\internal\dbsupport\db2zos\DB2zosDbSupport.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */