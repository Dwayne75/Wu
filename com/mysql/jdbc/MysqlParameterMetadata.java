package com.mysql.jdbc;

import java.sql.ParameterMetaData;
import java.sql.SQLException;

public class MysqlParameterMetadata
  implements ParameterMetaData
{
  boolean returnSimpleMetadata = false;
  ResultSetMetaData metadata = null;
  int parameterCount = 0;
  private ExceptionInterceptor exceptionInterceptor;
  
  MysqlParameterMetadata(Field[] fieldInfo, int parameterCount, ExceptionInterceptor exceptionInterceptor)
  {
    this.metadata = new ResultSetMetaData(fieldInfo, false, exceptionInterceptor);
    
    this.parameterCount = parameterCount;
    this.exceptionInterceptor = exceptionInterceptor;
  }
  
  MysqlParameterMetadata(int count)
  {
    this.parameterCount = count;
    this.returnSimpleMetadata = true;
  }
  
  public int getParameterCount()
    throws SQLException
  {
    return this.parameterCount;
  }
  
  public int isNullable(int arg0)
    throws SQLException
  {
    checkAvailable();
    
    return this.metadata.isNullable(arg0);
  }
  
  private void checkAvailable()
    throws SQLException
  {
    if ((this.metadata == null) || (this.metadata.fields == null)) {
      throw SQLError.createSQLException("Parameter metadata not available for the given statement", "S1C00", this.exceptionInterceptor);
    }
  }
  
  public boolean isSigned(int arg0)
    throws SQLException
  {
    if (this.returnSimpleMetadata)
    {
      checkBounds(arg0);
      
      return false;
    }
    checkAvailable();
    
    return this.metadata.isSigned(arg0);
  }
  
  public int getPrecision(int arg0)
    throws SQLException
  {
    if (this.returnSimpleMetadata)
    {
      checkBounds(arg0);
      
      return 0;
    }
    checkAvailable();
    
    return this.metadata.getPrecision(arg0);
  }
  
  public int getScale(int arg0)
    throws SQLException
  {
    if (this.returnSimpleMetadata)
    {
      checkBounds(arg0);
      
      return 0;
    }
    checkAvailable();
    
    return this.metadata.getScale(arg0);
  }
  
  public int getParameterType(int arg0)
    throws SQLException
  {
    if (this.returnSimpleMetadata)
    {
      checkBounds(arg0);
      
      return 12;
    }
    checkAvailable();
    
    return this.metadata.getColumnType(arg0);
  }
  
  public String getParameterTypeName(int arg0)
    throws SQLException
  {
    if (this.returnSimpleMetadata)
    {
      checkBounds(arg0);
      
      return "VARCHAR";
    }
    checkAvailable();
    
    return this.metadata.getColumnTypeName(arg0);
  }
  
  public String getParameterClassName(int arg0)
    throws SQLException
  {
    if (this.returnSimpleMetadata)
    {
      checkBounds(arg0);
      
      return "java.lang.String";
    }
    checkAvailable();
    
    return this.metadata.getColumnClassName(arg0);
  }
  
  public int getParameterMode(int arg0)
    throws SQLException
  {
    return 1;
  }
  
  private void checkBounds(int paramNumber)
    throws SQLException
  {
    if (paramNumber < 1) {
      throw SQLError.createSQLException("Parameter index of '" + paramNumber + "' is invalid.", "S1009", this.exceptionInterceptor);
    }
    if (paramNumber > this.parameterCount) {
      throw SQLError.createSQLException("Parameter index of '" + paramNumber + "' is greater than number of parameters, which is '" + this.parameterCount + "'.", "S1009", this.exceptionInterceptor);
    }
  }
  
  public boolean isWrapperFor(Class iface)
    throws SQLException
  {
    return iface.isInstance(this);
  }
  
  public Object unwrap(Class iface)
    throws SQLException
  {
    try
    {
      return Util.cast(iface, this);
    }
    catch (ClassCastException cce)
    {
      throw SQLError.createSQLException("Unable to unwrap to " + iface.toString(), "S1009", this.exceptionInterceptor);
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\mysql\jdbc\MysqlParameterMetadata.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */