package com.mysql.jdbc;

import java.io.Reader;
import java.sql.NClob;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLXML;

public class JDBC4ServerPreparedStatement
  extends ServerPreparedStatement
{
  public JDBC4ServerPreparedStatement(ConnectionImpl conn, String sql, String catalog, int resultSetType, int resultSetConcurrency)
    throws SQLException
  {
    super(conn, sql, catalog, resultSetType, resultSetConcurrency);
  }
  
  public void setNCharacterStream(int parameterIndex, Reader reader, long length)
    throws SQLException
  {
    if ((!this.charEncoding.equalsIgnoreCase("UTF-8")) && (!this.charEncoding.equalsIgnoreCase("utf8"))) {
      throw SQLError.createSQLException("Can not call setNCharacterStream() when connection character set isn't UTF-8", getExceptionInterceptor());
    }
    checkClosed();
    if (reader == null)
    {
      setNull(parameterIndex, -2);
    }
    else
    {
      ServerPreparedStatement.BindValue binding = getBinding(parameterIndex, true);
      setType(binding, 252);
      
      binding.value = reader;
      binding.isNull = false;
      binding.isLongData = true;
      if (this.connection.getUseStreamLengthsInPrepStmts()) {
        binding.bindLength = length;
      } else {
        binding.bindLength = -1L;
      }
    }
  }
  
  public void setNClob(int parameterIndex, NClob x)
    throws SQLException
  {
    setNClob(parameterIndex, x.getCharacterStream(), this.connection.getUseStreamLengthsInPrepStmts() ? x.length() : -1L);
  }
  
  public void setNClob(int parameterIndex, Reader reader, long length)
    throws SQLException
  {
    if ((!this.charEncoding.equalsIgnoreCase("UTF-8")) && (!this.charEncoding.equalsIgnoreCase("utf8"))) {
      throw SQLError.createSQLException("Can not call setNClob() when connection character set isn't UTF-8", getExceptionInterceptor());
    }
    checkClosed();
    if (reader == null)
    {
      setNull(parameterIndex, 2011);
    }
    else
    {
      ServerPreparedStatement.BindValue binding = getBinding(parameterIndex, true);
      setType(binding, 252);
      
      binding.value = reader;
      binding.isNull = false;
      binding.isLongData = true;
      if (this.connection.getUseStreamLengthsInPrepStmts()) {
        binding.bindLength = length;
      } else {
        binding.bindLength = -1L;
      }
    }
  }
  
  public void setNString(int parameterIndex, String x)
    throws SQLException
  {
    if ((this.charEncoding.equalsIgnoreCase("UTF-8")) || (this.charEncoding.equalsIgnoreCase("utf8"))) {
      setString(parameterIndex, x);
    } else {
      throw SQLError.createSQLException("Can not call setNString() when connection character set isn't UTF-8", getExceptionInterceptor());
    }
  }
  
  public void setRowId(int parameterIndex, RowId x)
    throws SQLException
  {
    JDBC4PreparedStatementHelper.setRowId(this, parameterIndex, x);
  }
  
  public void setSQLXML(int parameterIndex, SQLXML xmlObject)
    throws SQLException
  {
    JDBC4PreparedStatementHelper.setSQLXML(this, parameterIndex, xmlObject);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\mysql\jdbc\JDBC4ServerPreparedStatement.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */