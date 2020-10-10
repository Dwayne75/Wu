package org.flywaydb.core.internal.dbsupport.hsql;

import java.sql.Connection;
import java.sql.SQLException;
import org.flywaydb.core.internal.dbsupport.DbSupport;
import org.flywaydb.core.internal.dbsupport.JdbcTemplate;
import org.flywaydb.core.internal.dbsupport.Schema;
import org.flywaydb.core.internal.dbsupport.SqlStatementBuilder;

public class HsqlDbSupport
  extends DbSupport
{
  public HsqlDbSupport(Connection connection)
  {
    super(new JdbcTemplate(connection, 12));
  }
  
  public String getDbName()
  {
    return "hsql";
  }
  
  public String getCurrentUserFunction()
  {
    return "USER()";
  }
  
  /* Error */
  protected String doGetCurrentSchemaName()
    throws SQLException
  {
    // Byte code:
    //   0: aconst_null
    //   1: astore_1
    //   2: aconst_null
    //   3: astore_2
    //   4: aload_0
    //   5: getfield 7	org/flywaydb/core/internal/dbsupport/hsql/HsqlDbSupport:jdbcTemplate	Lorg/flywaydb/core/internal/dbsupport/JdbcTemplate;
    //   8: invokevirtual 8	org/flywaydb/core/internal/dbsupport/JdbcTemplate:getMetaData	()Ljava/sql/DatabaseMetaData;
    //   11: invokeinterface 9 1 0
    //   16: astore_1
    //   17: aload_1
    //   18: invokeinterface 10 1 0
    //   23: ifeq +26 -> 49
    //   26: aload_1
    //   27: ldc 11
    //   29: invokeinterface 12 2 0
    //   34: ifeq -17 -> 17
    //   37: aload_1
    //   38: ldc 13
    //   40: invokeinterface 14 2 0
    //   45: astore_2
    //   46: goto +3 -> 49
    //   49: aload_1
    //   50: invokestatic 15	org/flywaydb/core/internal/util/jdbc/JdbcUtils:closeResultSet	(Ljava/sql/ResultSet;)V
    //   53: goto +10 -> 63
    //   56: astore_3
    //   57: aload_1
    //   58: invokestatic 15	org/flywaydb/core/internal/util/jdbc/JdbcUtils:closeResultSet	(Ljava/sql/ResultSet;)V
    //   61: aload_3
    //   62: athrow
    //   63: aload_2
    //   64: areturn
    // Line number table:
    //   Java source line #52	-> byte code offset #0
    //   Java source line #53	-> byte code offset #2
    //   Java source line #56	-> byte code offset #4
    //   Java source line #57	-> byte code offset #17
    //   Java source line #58	-> byte code offset #26
    //   Java source line #59	-> byte code offset #37
    //   Java source line #60	-> byte code offset #46
    //   Java source line #64	-> byte code offset #49
    //   Java source line #65	-> byte code offset #53
    //   Java source line #64	-> byte code offset #56
    //   Java source line #67	-> byte code offset #63
    // Local variable table:
    //   start	length	slot	name	signature
    //   0	65	0	this	HsqlDbSupport
    //   1	57	1	resultSet	java.sql.ResultSet
    //   3	61	2	schema	String
    //   56	6	3	localObject	Object
    // Exception table:
    //   from	to	target	type
    //   4	49	56	finally
  }
  
  protected void doChangeCurrentSchemaTo(String schema)
    throws SQLException
  {
    this.jdbcTemplate.execute("SET SCHEMA " + schema, new Object[0]);
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
    return new HsqlSqlStatementBuilder();
  }
  
  public String doQuote(String identifier)
  {
    return "\"" + identifier + "\"";
  }
  
  public Schema getSchema(String name)
  {
    return new HsqlSchema(this.jdbcTemplate, this, name);
  }
  
  public boolean catalogIsSchema()
  {
    return false;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\flywaydb\core\internal\dbsupport\hsql\HsqlDbSupport.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */