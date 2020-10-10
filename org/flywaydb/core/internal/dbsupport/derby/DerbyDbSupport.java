package org.flywaydb.core.internal.dbsupport.derby;

import java.sql.Connection;
import java.sql.SQLException;
import org.flywaydb.core.internal.dbsupport.DbSupport;
import org.flywaydb.core.internal.dbsupport.JdbcTemplate;
import org.flywaydb.core.internal.dbsupport.Schema;
import org.flywaydb.core.internal.dbsupport.SqlStatementBuilder;

public class DerbyDbSupport
  extends DbSupport
{
  public DerbyDbSupport(Connection connection)
  {
    super(new JdbcTemplate(connection, 12));
  }
  
  public String getDbName()
  {
    return "derby";
  }
  
  public String getCurrentUserFunction()
  {
    return "CURRENT_USER";
  }
  
  protected String doGetCurrentSchemaName()
    throws SQLException
  {
    return this.jdbcTemplate.queryForString("SELECT CURRENT SCHEMA FROM SYSIBM.SYSDUMMY1", new String[0]);
  }
  
  protected void doChangeCurrentSchemaTo(String schema)
    throws SQLException
  {
    this.jdbcTemplate.execute("SET SCHEMA " + schema, new Object[0]);
  }
  
  public boolean supportsDdlTransactions()
  {
    return true;
  }
  
  public String getBooleanTrue()
  {
    return "true";
  }
  
  public String getBooleanFalse()
  {
    return "false";
  }
  
  public SqlStatementBuilder createSqlStatementBuilder()
  {
    return new DerbySqlStatementBuilder();
  }
  
  public String doQuote(String identifier)
  {
    return "\"" + identifier + "\"";
  }
  
  public Schema getSchema(String name)
  {
    return new DerbySchema(this.jdbcTemplate, this, name);
  }
  
  public boolean catalogIsSchema()
  {
    return false;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\flywaydb\core\internal\dbsupport\derby\DerbyDbSupport.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */