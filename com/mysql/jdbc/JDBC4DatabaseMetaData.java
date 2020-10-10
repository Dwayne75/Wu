package com.mysql.jdbc;

import java.sql.ResultSet;
import java.sql.RowIdLifetime;
import java.sql.SQLException;
import java.util.ArrayList;

public class JDBC4DatabaseMetaData
  extends DatabaseMetaData
{
  public JDBC4DatabaseMetaData(ConnectionImpl connToSet, String databaseToSet)
  {
    super(connToSet, databaseToSet);
  }
  
  public RowIdLifetime getRowIdLifetime()
    throws SQLException
  {
    return RowIdLifetime.ROWID_UNSUPPORTED;
  }
  
  public boolean isWrapperFor(Class<?> iface)
    throws SQLException
  {
    return iface.isInstance(this);
  }
  
  public <T> T unwrap(Class<T> iface)
    throws SQLException
  {
    try
    {
      return (T)iface.cast(this);
    }
    catch (ClassCastException cce)
    {
      throw SQLError.createSQLException("Unable to unwrap to " + iface.toString(), "S1009", this.conn.getExceptionInterceptor());
    }
  }
  
  public ResultSet getClientInfoProperties()
    throws SQLException
  {
    Field[] fields = new Field[4];
    fields[0] = new Field("", "NAME", 12, 255);
    fields[1] = new Field("", "MAX_LEN", 4, 10);
    fields[2] = new Field("", "DEFAULT_VALUE", 12, 255);
    fields[3] = new Field("", "DESCRIPTION", 12, 255);
    
    ArrayList tuples = new ArrayList();
    
    return buildResultSet(fields, tuples, this.conn);
  }
  
  public boolean autoCommitFailureClosesAllResultSets()
    throws SQLException
  {
    return false;
  }
  
  public ResultSet getFunctions(String catalog, String schemaPattern, String functionNamePattern)
    throws SQLException
  {
    Field[] fields = new Field[6];
    
    fields[0] = new Field("", "FUNCTION_CAT", 1, 255);
    fields[1] = new Field("", "FUNCTION_SCHEM", 1, 255);
    fields[2] = new Field("", "FUNCTION_NAME", 1, 255);
    fields[3] = new Field("", "REMARKS", 1, 255);
    fields[4] = new Field("", "FUNCTION_TYPE", 5, 6);
    fields[5] = new Field("", "SPECIFIC_NAME", 1, 255);
    
    return getProceduresAndOrFunctions(fields, catalog, schemaPattern, functionNamePattern, false, true);
  }
  
  protected int getJDBC4FunctionNoTableConstant()
  {
    return 1;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\mysql\jdbc\JDBC4DatabaseMetaData.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */