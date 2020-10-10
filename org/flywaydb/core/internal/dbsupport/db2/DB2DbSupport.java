package org.flywaydb.core.internal.dbsupport.db2;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.internal.dbsupport.DbSupport;
import org.flywaydb.core.internal.dbsupport.JdbcTemplate;
import org.flywaydb.core.internal.dbsupport.Schema;
import org.flywaydb.core.internal.dbsupport.SqlStatementBuilder;

public class DB2DbSupport
  extends DbSupport
{
  private final int majorVersion;
  
  public DB2DbSupport(Connection connection)
  {
    super(new JdbcTemplate(connection, 12));
    try
    {
      this.majorVersion = connection.getMetaData().getDatabaseMajorVersion();
    }
    catch (SQLException e)
    {
      throw new FlywayException("Unable to determine DB2 major version", e);
    }
  }
  
  public SqlStatementBuilder createSqlStatementBuilder()
  {
    return new DB2SqlStatementBuilder();
  }
  
  public String getDbName()
  {
    return "db2";
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
    return "CURRENT_USER";
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
    return new DB2Schema(this.jdbcTemplate, this, name);
  }
  
  public boolean catalogIsSchema()
  {
    return false;
  }
  
  public int getDb2MajorVersion()
  {
    return this.majorVersion;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\flywaydb\core\internal\dbsupport\db2\DB2DbSupport.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */