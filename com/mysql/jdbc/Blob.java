package com.mysql.jdbc;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;

public class Blob
  implements java.sql.Blob, OutputStreamWatcher
{
  private byte[] binaryData = null;
  private boolean isClosed = false;
  private ExceptionInterceptor exceptionInterceptor;
  
  Blob(ExceptionInterceptor exceptionInterceptor)
  {
    setBinaryData(Constants.EMPTY_BYTE_ARRAY);
    this.exceptionInterceptor = exceptionInterceptor;
  }
  
  Blob(byte[] data, ExceptionInterceptor exceptionInterceptor)
  {
    setBinaryData(data);
    this.exceptionInterceptor = exceptionInterceptor;
  }
  
  Blob(byte[] data, ResultSetInternalMethods creatorResultSetToSet, int columnIndexToSet)
  {
    setBinaryData(data);
  }
  
  private synchronized byte[] getBinaryData()
  {
    return this.binaryData;
  }
  
  public synchronized InputStream getBinaryStream()
    throws SQLException
  {
    checkClosed();
    
    return new ByteArrayInputStream(getBinaryData());
  }
  
  public synchronized byte[] getBytes(long pos, int length)
    throws SQLException
  {
    checkClosed();
    if (pos < 1L) {
      throw SQLError.createSQLException(Messages.getString("Blob.2"), "S1009", this.exceptionInterceptor);
    }
    pos -= 1L;
    if (pos > this.binaryData.length) {
      throw SQLError.createSQLException("\"pos\" argument can not be larger than the BLOB's length.", "S1009", this.exceptionInterceptor);
    }
    if (pos + length > this.binaryData.length) {
      throw SQLError.createSQLException("\"pos\" + \"length\" arguments can not be larger than the BLOB's length.", "S1009", this.exceptionInterceptor);
    }
    byte[] newData = new byte[length];
    System.arraycopy(getBinaryData(), (int)pos, newData, 0, length);
    
    return newData;
  }
  
  public synchronized long length()
    throws SQLException
  {
    checkClosed();
    
    return getBinaryData().length;
  }
  
  public synchronized long position(byte[] pattern, long start)
    throws SQLException
  {
    throw SQLError.createSQLException("Not implemented", this.exceptionInterceptor);
  }
  
  public synchronized long position(java.sql.Blob pattern, long start)
    throws SQLException
  {
    checkClosed();
    
    return position(pattern.getBytes(0L, (int)pattern.length()), start);
  }
  
  private synchronized void setBinaryData(byte[] newBinaryData)
  {
    this.binaryData = newBinaryData;
  }
  
  public synchronized OutputStream setBinaryStream(long indexToWriteAt)
    throws SQLException
  {
    checkClosed();
    if (indexToWriteAt < 1L) {
      throw SQLError.createSQLException(Messages.getString("Blob.0"), "S1009", this.exceptionInterceptor);
    }
    WatchableOutputStream bytesOut = new WatchableOutputStream();
    bytesOut.setWatcher(this);
    if (indexToWriteAt > 0L) {
      bytesOut.write(this.binaryData, 0, (int)(indexToWriteAt - 1L));
    }
    return bytesOut;
  }
  
  public synchronized int setBytes(long writeAt, byte[] bytes)
    throws SQLException
  {
    checkClosed();
    
    return setBytes(writeAt, bytes, 0, bytes.length);
  }
  
  public synchronized int setBytes(long writeAt, byte[] bytes, int offset, int length)
    throws SQLException
  {
    checkClosed();
    
    OutputStream bytesOut = setBinaryStream(writeAt);
    try
    {
      bytesOut.write(bytes, offset, length);
      SQLException sqlEx;
      return length;
    }
    catch (IOException ioEx)
    {
      sqlEx = SQLError.createSQLException(Messages.getString("Blob.1"), "S1000", this.exceptionInterceptor);
      
      sqlEx.initCause(ioEx);
      
      throw sqlEx;
    }
    finally
    {
      try
      {
        bytesOut.close();
      }
      catch (IOException doNothing) {}
    }
  }
  
  public synchronized void streamClosed(byte[] byteData)
  {
    this.binaryData = byteData;
  }
  
  public synchronized void streamClosed(WatchableOutputStream out)
  {
    int streamSize = out.size();
    if (streamSize < this.binaryData.length) {
      out.write(this.binaryData, streamSize, this.binaryData.length - streamSize);
    }
    this.binaryData = out.toByteArray();
  }
  
  public synchronized void truncate(long len)
    throws SQLException
  {
    checkClosed();
    if (len < 0L) {
      throw SQLError.createSQLException("\"len\" argument can not be < 1.", "S1009", this.exceptionInterceptor);
    }
    if (len > this.binaryData.length) {
      throw SQLError.createSQLException("\"len\" argument can not be larger than the BLOB's length.", "S1009", this.exceptionInterceptor);
    }
    byte[] newData = new byte[(int)len];
    System.arraycopy(getBinaryData(), 0, newData, 0, (int)len);
    this.binaryData = newData;
  }
  
  public synchronized void free()
    throws SQLException
  {
    this.binaryData = null;
    this.isClosed = true;
  }
  
  public synchronized InputStream getBinaryStream(long pos, long length)
    throws SQLException
  {
    checkClosed();
    if (pos < 1L) {
      throw SQLError.createSQLException("\"pos\" argument can not be < 1.", "S1009", this.exceptionInterceptor);
    }
    pos -= 1L;
    if (pos > this.binaryData.length) {
      throw SQLError.createSQLException("\"pos\" argument can not be larger than the BLOB's length.", "S1009", this.exceptionInterceptor);
    }
    if (pos + length > this.binaryData.length) {
      throw SQLError.createSQLException("\"pos\" + \"length\" arguments can not be larger than the BLOB's length.", "S1009", this.exceptionInterceptor);
    }
    return new ByteArrayInputStream(getBinaryData(), (int)pos, (int)length);
  }
  
  private synchronized void checkClosed()
    throws SQLException
  {
    if (this.isClosed) {
      throw SQLError.createSQLException("Invalid operation on closed BLOB", "S1009", this.exceptionInterceptor);
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\mysql\jdbc\Blob.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */