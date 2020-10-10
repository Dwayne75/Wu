package com.mysql.jdbc;

import java.io.InputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.sql.NClob;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLXML;

public class JDBC4ResultSet
  extends ResultSetImpl
{
  public JDBC4ResultSet(long updateCount, long updateID, ConnectionImpl conn, StatementImpl creatorStmt)
  {
    super(updateCount, updateID, conn, creatorStmt);
  }
  
  public JDBC4ResultSet(String catalog, Field[] fields, RowData tuples, ConnectionImpl conn, StatementImpl creatorStmt)
    throws SQLException
  {
    super(catalog, fields, tuples, conn, creatorStmt);
  }
  
  public Reader getNCharacterStream(int columnIndex)
    throws SQLException
  {
    checkColumnBounds(columnIndex);
    
    String fieldEncoding = this.fields[(columnIndex - 1)].getCharacterSet();
    if ((fieldEncoding == null) || (!fieldEncoding.equals("UTF-8"))) {
      throw new SQLException("Can not call getNCharacterStream() when field's charset isn't UTF-8");
    }
    return getCharacterStream(columnIndex);
  }
  
  public Reader getNCharacterStream(String columnName)
    throws SQLException
  {
    return getNCharacterStream(findColumn(columnName));
  }
  
  public NClob getNClob(int columnIndex)
    throws SQLException
  {
    checkColumnBounds(columnIndex);
    
    String fieldEncoding = this.fields[(columnIndex - 1)].getCharacterSet();
    if ((fieldEncoding == null) || (!fieldEncoding.equals("UTF-8"))) {
      throw new SQLException("Can not call getNClob() when field's charset isn't UTF-8");
    }
    if (!this.isBinaryEncoded)
    {
      String asString = getStringForNClob(columnIndex);
      if (asString == null) {
        return null;
      }
      return new JDBC4NClob(asString, getExceptionInterceptor());
    }
    return getNativeNClob(columnIndex);
  }
  
  public NClob getNClob(String columnName)
    throws SQLException
  {
    return getNClob(findColumn(columnName));
  }
  
  protected NClob getNativeNClob(int columnIndex)
    throws SQLException
  {
    String stringVal = getStringForNClob(columnIndex);
    if (stringVal == null) {
      return null;
    }
    return getNClobFromString(stringVal, columnIndex);
  }
  
  private String getStringForNClob(int columnIndex)
    throws SQLException
  {
    String asString = null;
    
    String forcedEncoding = "UTF-8";
    try
    {
      byte[] asBytes = null;
      if (!this.isBinaryEncoded) {
        asBytes = getBytes(columnIndex);
      } else {
        asBytes = getNativeBytes(columnIndex, true);
      }
      if (asBytes != null) {
        asString = new String(asBytes, forcedEncoding);
      }
    }
    catch (UnsupportedEncodingException uee)
    {
      throw SQLError.createSQLException("Unsupported character encoding " + forcedEncoding, "S1009", getExceptionInterceptor());
    }
    return asString;
  }
  
  private final NClob getNClobFromString(String stringVal, int columnIndex)
    throws SQLException
  {
    return new JDBC4NClob(stringVal, getExceptionInterceptor());
  }
  
  public String getNString(int columnIndex)
    throws SQLException
  {
    checkColumnBounds(columnIndex);
    
    String fieldEncoding = this.fields[(columnIndex - 1)].getCharacterSet();
    if ((fieldEncoding == null) || (!fieldEncoding.equals("UTF-8"))) {
      throw new SQLException("Can not call getNString() when field's charset isn't UTF-8");
    }
    return getString(columnIndex);
  }
  
  public String getNString(String columnName)
    throws SQLException
  {
    return getNString(findColumn(columnName));
  }
  
  public void updateNCharacterStream(int columnIndex, Reader x, int length)
    throws SQLException
  {
    throw new NotUpdatable();
  }
  
  public void updateNCharacterStream(String columnName, Reader reader, int length)
    throws SQLException
  {
    updateNCharacterStream(findColumn(columnName), reader, length);
  }
  
  public void updateNClob(String columnName, NClob nClob)
    throws SQLException
  {
    updateNClob(findColumn(columnName), nClob);
  }
  
  public void updateRowId(int columnIndex, RowId x)
    throws SQLException
  {
    throw new NotUpdatable();
  }
  
  public void updateRowId(String columnName, RowId x)
    throws SQLException
  {
    updateRowId(findColumn(columnName), x);
  }
  
  public int getHoldability()
    throws SQLException
  {
    throw SQLError.notImplemented();
  }
  
  public RowId getRowId(int columnIndex)
    throws SQLException
  {
    throw SQLError.notImplemented();
  }
  
  public RowId getRowId(String columnLabel)
    throws SQLException
  {
    return getRowId(findColumn(columnLabel));
  }
  
  public SQLXML getSQLXML(int columnIndex)
    throws SQLException
  {
    checkColumnBounds(columnIndex);
    
    return new JDBC4MysqlSQLXML(this, columnIndex, getExceptionInterceptor());
  }
  
  public SQLXML getSQLXML(String columnLabel)
    throws SQLException
  {
    return getSQLXML(findColumn(columnLabel));
  }
  
  public synchronized boolean isClosed()
    throws SQLException
  {
    return this.isClosed;
  }
  
  public void updateAsciiStream(int columnIndex, InputStream x)
    throws SQLException
  {
    throw new NotUpdatable();
  }
  
  public void updateAsciiStream(String columnLabel, InputStream x)
    throws SQLException
  {
    updateAsciiStream(findColumn(columnLabel), x);
  }
  
  public void updateAsciiStream(int columnIndex, InputStream x, long length)
    throws SQLException
  {
    throw new NotUpdatable();
  }
  
  public void updateAsciiStream(String columnLabel, InputStream x, long length)
    throws SQLException
  {
    updateAsciiStream(findColumn(columnLabel), x, length);
  }
  
  public void updateBinaryStream(int columnIndex, InputStream x)
    throws SQLException
  {
    throw new NotUpdatable();
  }
  
  public void updateBinaryStream(String columnLabel, InputStream x)
    throws SQLException
  {
    updateBinaryStream(findColumn(columnLabel), x);
  }
  
  public void updateBinaryStream(int columnIndex, InputStream x, long length)
    throws SQLException
  {
    throw new NotUpdatable();
  }
  
  public void updateBinaryStream(String columnLabel, InputStream x, long length)
    throws SQLException
  {
    updateBinaryStream(findColumn(columnLabel), x, length);
  }
  
  public void updateBlob(int columnIndex, InputStream inputStream)
    throws SQLException
  {
    throw new NotUpdatable();
  }
  
  public void updateBlob(String columnLabel, InputStream inputStream)
    throws SQLException
  {
    updateBlob(findColumn(columnLabel), inputStream);
  }
  
  public void updateBlob(int columnIndex, InputStream inputStream, long length)
    throws SQLException
  {
    throw new NotUpdatable();
  }
  
  public void updateBlob(String columnLabel, InputStream inputStream, long length)
    throws SQLException
  {
    updateBlob(findColumn(columnLabel), inputStream, length);
  }
  
  public void updateCharacterStream(int columnIndex, Reader x)
    throws SQLException
  {
    throw new NotUpdatable();
  }
  
  public void updateCharacterStream(String columnLabel, Reader reader)
    throws SQLException
  {
    updateCharacterStream(findColumn(columnLabel), reader);
  }
  
  public void updateCharacterStream(int columnIndex, Reader x, long length)
    throws SQLException
  {
    throw new NotUpdatable();
  }
  
  public void updateCharacterStream(String columnLabel, Reader reader, long length)
    throws SQLException
  {
    updateCharacterStream(findColumn(columnLabel), reader, length);
  }
  
  public void updateClob(int columnIndex, Reader reader)
    throws SQLException
  {
    throw new NotUpdatable();
  }
  
  public void updateClob(String columnLabel, Reader reader)
    throws SQLException
  {
    updateClob(findColumn(columnLabel), reader);
  }
  
  public void updateClob(int columnIndex, Reader reader, long length)
    throws SQLException
  {
    throw new NotUpdatable();
  }
  
  public void updateClob(String columnLabel, Reader reader, long length)
    throws SQLException
  {
    updateClob(findColumn(columnLabel), reader, length);
  }
  
  public void updateNCharacterStream(int columnIndex, Reader x)
    throws SQLException
  {
    throw new NotUpdatable();
  }
  
  public void updateNCharacterStream(String columnLabel, Reader reader)
    throws SQLException
  {
    updateNCharacterStream(findColumn(columnLabel), reader);
  }
  
  public void updateNCharacterStream(int columnIndex, Reader x, long length)
    throws SQLException
  {
    throw new NotUpdatable();
  }
  
  public void updateNCharacterStream(String columnLabel, Reader reader, long length)
    throws SQLException
  {
    updateNCharacterStream(findColumn(columnLabel), reader, length);
  }
  
  public void updateNClob(int columnIndex, NClob nClob)
    throws SQLException
  {
    throw new NotUpdatable();
  }
  
  public void updateNClob(int columnIndex, Reader reader)
    throws SQLException
  {
    throw new NotUpdatable();
  }
  
  public void updateNClob(String columnLabel, Reader reader)
    throws SQLException
  {
    updateNClob(findColumn(columnLabel), reader);
  }
  
  public void updateNClob(int columnIndex, Reader reader, long length)
    throws SQLException
  {
    throw new NotUpdatable();
  }
  
  public void updateNClob(String columnLabel, Reader reader, long length)
    throws SQLException
  {
    updateNClob(findColumn(columnLabel), reader, length);
  }
  
  public void updateNString(int columnIndex, String nString)
    throws SQLException
  {
    throw new NotUpdatable();
  }
  
  public void updateNString(String columnLabel, String nString)
    throws SQLException
  {
    updateNString(findColumn(columnLabel), nString);
  }
  
  public void updateSQLXML(int columnIndex, SQLXML xmlObject)
    throws SQLException
  {
    throw new NotUpdatable();
  }
  
  public void updateSQLXML(String columnLabel, SQLXML xmlObject)
    throws SQLException
  {
    updateSQLXML(findColumn(columnLabel), xmlObject);
  }
  
  public boolean isWrapperFor(Class<?> iface)
    throws SQLException
  {
    checkClosed();
    
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
      throw SQLError.createSQLException("Unable to unwrap to " + iface.toString(), "S1009", getExceptionInterceptor());
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\mysql\jdbc\JDBC4ResultSet.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */