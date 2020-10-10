package com.mysql.jdbc;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Blob;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BlobFromLocator
  implements Blob
{
  private List primaryKeyColumns = null;
  private List primaryKeyValues = null;
  private ResultSetImpl creatorResultSet;
  private String blobColumnName = null;
  private String tableName = null;
  private int numColsInResultSet = 0;
  private int numPrimaryKeys = 0;
  private String quotedId;
  private ExceptionInterceptor exceptionInterceptor;
  
  BlobFromLocator(ResultSetImpl creatorResultSetToSet, int blobColumnIndex, ExceptionInterceptor exceptionInterceptor)
    throws SQLException
  {
    this.exceptionInterceptor = exceptionInterceptor;
    this.creatorResultSet = creatorResultSetToSet;
    
    this.numColsInResultSet = this.creatorResultSet.fields.length;
    this.quotedId = this.creatorResultSet.connection.getMetaData().getIdentifierQuoteString();
    if (this.numColsInResultSet > 1)
    {
      this.primaryKeyColumns = new ArrayList();
      this.primaryKeyValues = new ArrayList();
      for (int i = 0; i < this.numColsInResultSet; i++) {
        if (this.creatorResultSet.fields[i].isPrimaryKey())
        {
          StringBuffer keyName = new StringBuffer();
          keyName.append(this.quotedId);
          
          String originalColumnName = this.creatorResultSet.fields[i].getOriginalName();
          if ((originalColumnName != null) && (originalColumnName.length() > 0)) {
            keyName.append(originalColumnName);
          } else {
            keyName.append(this.creatorResultSet.fields[i].getName());
          }
          keyName.append(this.quotedId);
          
          this.primaryKeyColumns.add(keyName.toString());
          this.primaryKeyValues.add(this.creatorResultSet.getString(i + 1));
        }
      }
    }
    else
    {
      notEnoughInformationInQuery();
    }
    this.numPrimaryKeys = this.primaryKeyColumns.size();
    if (this.numPrimaryKeys == 0) {
      notEnoughInformationInQuery();
    }
    if (this.creatorResultSet.fields[0].getOriginalTableName() != null)
    {
      StringBuffer tableNameBuffer = new StringBuffer();
      
      String databaseName = this.creatorResultSet.fields[0].getDatabaseName();
      if ((databaseName != null) && (databaseName.length() > 0))
      {
        tableNameBuffer.append(this.quotedId);
        tableNameBuffer.append(databaseName);
        tableNameBuffer.append(this.quotedId);
        tableNameBuffer.append('.');
      }
      tableNameBuffer.append(this.quotedId);
      tableNameBuffer.append(this.creatorResultSet.fields[0].getOriginalTableName());
      
      tableNameBuffer.append(this.quotedId);
      
      this.tableName = tableNameBuffer.toString();
    }
    else
    {
      StringBuffer tableNameBuffer = new StringBuffer();
      
      tableNameBuffer.append(this.quotedId);
      tableNameBuffer.append(this.creatorResultSet.fields[0].getTableName());
      
      tableNameBuffer.append(this.quotedId);
      
      this.tableName = tableNameBuffer.toString();
    }
    this.blobColumnName = (this.quotedId + this.creatorResultSet.getString(blobColumnIndex) + this.quotedId);
  }
  
  private void notEnoughInformationInQuery()
    throws SQLException
  {
    throw SQLError.createSQLException("Emulated BLOB locators must come from a ResultSet with only one table selected, and all primary keys selected", "S1000", this.exceptionInterceptor);
  }
  
  public OutputStream setBinaryStream(long indexToWriteAt)
    throws SQLException
  {
    throw SQLError.notImplemented();
  }
  
  public InputStream getBinaryStream()
    throws SQLException
  {
    return new BufferedInputStream(new LocatorInputStream(), this.creatorResultSet.connection.getLocatorFetchBufferSize());
  }
  
  public int setBytes(long writeAt, byte[] bytes, int offset, int length)
    throws SQLException
  {
    PreparedStatement pStmt = null;
    if (offset + length > bytes.length) {
      length = bytes.length - offset;
    }
    byte[] bytesToWrite = new byte[length];
    System.arraycopy(bytes, offset, bytesToWrite, 0, length);
    
    StringBuffer query = new StringBuffer("UPDATE ");
    query.append(this.tableName);
    query.append(" SET ");
    query.append(this.blobColumnName);
    query.append(" = INSERT(");
    query.append(this.blobColumnName);
    query.append(", ");
    query.append(writeAt);
    query.append(", ");
    query.append(length);
    query.append(", ?) WHERE ");
    
    query.append((String)this.primaryKeyColumns.get(0));
    query.append(" = ?");
    for (int i = 1; i < this.numPrimaryKeys; i++)
    {
      query.append(" AND ");
      query.append((String)this.primaryKeyColumns.get(i));
      query.append(" = ?");
    }
    try
    {
      pStmt = this.creatorResultSet.connection.prepareStatement(query.toString());
      
      pStmt.setBytes(1, bytesToWrite);
      for (int i = 0; i < this.numPrimaryKeys; i++) {
        pStmt.setString(i + 2, (String)this.primaryKeyValues.get(i));
      }
      int rowsUpdated = pStmt.executeUpdate();
      if (rowsUpdated != 1) {
        throw SQLError.createSQLException("BLOB data not found! Did primary keys change?", "S1000", this.exceptionInterceptor);
      }
    }
    finally
    {
      if (pStmt != null)
      {
        try
        {
          pStmt.close();
        }
        catch (SQLException sqlEx) {}
        pStmt = null;
      }
    }
    return (int)length();
  }
  
  public int setBytes(long writeAt, byte[] bytes)
    throws SQLException
  {
    return setBytes(writeAt, bytes, 0, bytes.length);
  }
  
  public byte[] getBytes(long pos, int length)
    throws SQLException
  {
    PreparedStatement pStmt = null;
    try
    {
      pStmt = createGetBytesStatement();
      
      return getBytesInternal(pStmt, pos, length);
    }
    finally
    {
      if (pStmt != null)
      {
        try
        {
          pStmt.close();
        }
        catch (SQLException sqlEx) {}
        pStmt = null;
      }
    }
  }
  
  public long length()
    throws SQLException
  {
    ResultSet blobRs = null;
    PreparedStatement pStmt = null;
    
    StringBuffer query = new StringBuffer("SELECT LENGTH(");
    query.append(this.blobColumnName);
    query.append(") FROM ");
    query.append(this.tableName);
    query.append(" WHERE ");
    
    query.append((String)this.primaryKeyColumns.get(0));
    query.append(" = ?");
    for (int i = 1; i < this.numPrimaryKeys; i++)
    {
      query.append(" AND ");
      query.append((String)this.primaryKeyColumns.get(i));
      query.append(" = ?");
    }
    try
    {
      pStmt = this.creatorResultSet.connection.prepareStatement(query.toString());
      for (int i = 0; i < this.numPrimaryKeys; i++) {
        pStmt.setString(i + 1, (String)this.primaryKeyValues.get(i));
      }
      blobRs = pStmt.executeQuery();
      if (blobRs.next()) {
        return blobRs.getLong(1);
      }
      throw SQLError.createSQLException("BLOB data not found! Did primary keys change?", "S1000", this.exceptionInterceptor);
    }
    finally
    {
      if (blobRs != null)
      {
        try
        {
          blobRs.close();
        }
        catch (SQLException sqlEx) {}
        blobRs = null;
      }
      if (pStmt != null)
      {
        try
        {
          pStmt.close();
        }
        catch (SQLException sqlEx) {}
        pStmt = null;
      }
    }
  }
  
  public long position(Blob pattern, long start)
    throws SQLException
  {
    return position(pattern.getBytes(0L, (int)pattern.length()), start);
  }
  
  public long position(byte[] pattern, long start)
    throws SQLException
  {
    ResultSet blobRs = null;
    PreparedStatement pStmt = null;
    
    StringBuffer query = new StringBuffer("SELECT LOCATE(");
    query.append("?, ");
    query.append(this.blobColumnName);
    query.append(", ");
    query.append(start);
    query.append(") FROM ");
    query.append(this.tableName);
    query.append(" WHERE ");
    
    query.append((String)this.primaryKeyColumns.get(0));
    query.append(" = ?");
    for (int i = 1; i < this.numPrimaryKeys; i++)
    {
      query.append(" AND ");
      query.append((String)this.primaryKeyColumns.get(i));
      query.append(" = ?");
    }
    try
    {
      pStmt = this.creatorResultSet.connection.prepareStatement(query.toString());
      
      pStmt.setBytes(1, pattern);
      for (int i = 0; i < this.numPrimaryKeys; i++) {
        pStmt.setString(i + 2, (String)this.primaryKeyValues.get(i));
      }
      blobRs = pStmt.executeQuery();
      if (blobRs.next()) {
        return blobRs.getLong(1);
      }
      throw SQLError.createSQLException("BLOB data not found! Did primary keys change?", "S1000", this.exceptionInterceptor);
    }
    finally
    {
      if (blobRs != null)
      {
        try
        {
          blobRs.close();
        }
        catch (SQLException sqlEx) {}
        blobRs = null;
      }
      if (pStmt != null)
      {
        try
        {
          pStmt.close();
        }
        catch (SQLException sqlEx) {}
        pStmt = null;
      }
    }
  }
  
  public void truncate(long length)
    throws SQLException
  {
    PreparedStatement pStmt = null;
    
    StringBuffer query = new StringBuffer("UPDATE ");
    query.append(this.tableName);
    query.append(" SET ");
    query.append(this.blobColumnName);
    query.append(" = LEFT(");
    query.append(this.blobColumnName);
    query.append(", ");
    query.append(length);
    query.append(") WHERE ");
    
    query.append((String)this.primaryKeyColumns.get(0));
    query.append(" = ?");
    for (int i = 1; i < this.numPrimaryKeys; i++)
    {
      query.append(" AND ");
      query.append((String)this.primaryKeyColumns.get(i));
      query.append(" = ?");
    }
    try
    {
      pStmt = this.creatorResultSet.connection.prepareStatement(query.toString());
      for (int i = 0; i < this.numPrimaryKeys; i++) {
        pStmt.setString(i + 1, (String)this.primaryKeyValues.get(i));
      }
      int rowsUpdated = pStmt.executeUpdate();
      if (rowsUpdated != 1) {
        throw SQLError.createSQLException("BLOB data not found! Did primary keys change?", "S1000", this.exceptionInterceptor);
      }
    }
    finally
    {
      if (pStmt != null)
      {
        try
        {
          pStmt.close();
        }
        catch (SQLException sqlEx) {}
        pStmt = null;
      }
    }
  }
  
  PreparedStatement createGetBytesStatement()
    throws SQLException
  {
    StringBuffer query = new StringBuffer("SELECT SUBSTRING(");
    
    query.append(this.blobColumnName);
    query.append(", ");
    query.append("?");
    query.append(", ");
    query.append("?");
    query.append(") FROM ");
    query.append(this.tableName);
    query.append(" WHERE ");
    
    query.append((String)this.primaryKeyColumns.get(0));
    query.append(" = ?");
    for (int i = 1; i < this.numPrimaryKeys; i++)
    {
      query.append(" AND ");
      query.append((String)this.primaryKeyColumns.get(i));
      query.append(" = ?");
    }
    return this.creatorResultSet.connection.prepareStatement(query.toString());
  }
  
  byte[] getBytesInternal(PreparedStatement pStmt, long pos, int length)
    throws SQLException
  {
    ResultSet blobRs = null;
    try
    {
      pStmt.setLong(1, pos);
      pStmt.setInt(2, length);
      for (int i = 0; i < this.numPrimaryKeys; i++) {
        pStmt.setString(i + 3, (String)this.primaryKeyValues.get(i));
      }
      blobRs = pStmt.executeQuery();
      if (blobRs.next()) {
        return ((ResultSetImpl)blobRs).getBytes(1, true);
      }
      throw SQLError.createSQLException("BLOB data not found! Did primary keys change?", "S1000", this.exceptionInterceptor);
    }
    finally
    {
      if (blobRs != null)
      {
        try
        {
          blobRs.close();
        }
        catch (SQLException sqlEx) {}
        blobRs = null;
      }
    }
  }
  
  class LocatorInputStream
    extends InputStream
  {
    long currentPositionInBlob = 0L;
    long length = 0L;
    PreparedStatement pStmt = null;
    
    LocatorInputStream()
      throws SQLException
    {
      this.length = BlobFromLocator.this.length();
      this.pStmt = BlobFromLocator.this.createGetBytesStatement();
    }
    
    LocatorInputStream(long pos, long len)
      throws SQLException
    {
      this.length = (pos + len);
      this.currentPositionInBlob = pos;
      long blobLength = BlobFromLocator.this.length();
      if (pos + len > blobLength) {
        throw SQLError.createSQLException(Messages.getString("Blob.invalidStreamLength", new Object[] { new Long(blobLength), new Long(pos), new Long(len) }), "S1009", BlobFromLocator.this.exceptionInterceptor);
      }
      if (pos < 1L) {
        throw SQLError.createSQLException(Messages.getString("Blob.invalidStreamPos"), "S1009", BlobFromLocator.this.exceptionInterceptor);
      }
      if (pos > blobLength) {
        throw SQLError.createSQLException(Messages.getString("Blob.invalidStreamPos"), "S1009", BlobFromLocator.this.exceptionInterceptor);
      }
    }
    
    public int read()
      throws IOException
    {
      if (this.currentPositionInBlob + 1L > this.length) {
        return -1;
      }
      try
      {
        byte[] asBytes = BlobFromLocator.this.getBytesInternal(this.pStmt, this.currentPositionInBlob++ + 1L, 1);
        if (asBytes == null) {
          return -1;
        }
        return asBytes[0];
      }
      catch (SQLException sqlEx)
      {
        throw new IOException(sqlEx.toString());
      }
    }
    
    public int read(byte[] b, int off, int len)
      throws IOException
    {
      if (this.currentPositionInBlob + 1L > this.length) {
        return -1;
      }
      try
      {
        byte[] asBytes = BlobFromLocator.this.getBytesInternal(this.pStmt, this.currentPositionInBlob + 1L, len);
        if (asBytes == null) {
          return -1;
        }
        System.arraycopy(asBytes, 0, b, off, asBytes.length);
        
        this.currentPositionInBlob += asBytes.length;
        
        return asBytes.length;
      }
      catch (SQLException sqlEx)
      {
        throw new IOException(sqlEx.toString());
      }
    }
    
    public int read(byte[] b)
      throws IOException
    {
      if (this.currentPositionInBlob + 1L > this.length) {
        return -1;
      }
      try
      {
        byte[] asBytes = BlobFromLocator.this.getBytesInternal(this.pStmt, this.currentPositionInBlob + 1L, b.length);
        if (asBytes == null) {
          return -1;
        }
        System.arraycopy(asBytes, 0, b, 0, asBytes.length);
        
        this.currentPositionInBlob += asBytes.length;
        
        return asBytes.length;
      }
      catch (SQLException sqlEx)
      {
        throw new IOException(sqlEx.toString());
      }
    }
    
    public void close()
      throws IOException
    {
      if (this.pStmt != null) {
        try
        {
          this.pStmt.close();
        }
        catch (SQLException sqlEx)
        {
          throw new IOException(sqlEx.toString());
        }
      }
      super.close();
    }
  }
  
  public void free()
    throws SQLException
  {
    this.creatorResultSet = null;
    this.primaryKeyColumns = null;
    this.primaryKeyValues = null;
  }
  
  public InputStream getBinaryStream(long pos, long length)
    throws SQLException
  {
    return new LocatorInputStream(pos, length);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\mysql\jdbc\BlobFromLocator.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */