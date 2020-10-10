package com.mysql.jdbc;

import com.mysql.jdbc.profiler.ProfilerEvent;
import com.mysql.jdbc.profiler.ProfilerEventHandler;
import com.mysql.jdbc.profiler.ProfilerEventHandlerFactory;
import java.sql.SQLException;
import java.sql.Statement;

public class RowDataDynamic
  implements RowData
{
  private int columnCount;
  private Field[] metadata;
  
  class OperationNotSupportedException
    extends SQLException
  {
    OperationNotSupportedException()
    {
      super("S1009");
    }
  }
  
  private int index = -1;
  private MysqlIO io;
  private boolean isAfterEnd = false;
  private boolean noMoreRows = false;
  private boolean isBinaryEncoded = false;
  private ResultSetRow nextRow;
  private ResultSetImpl owner;
  private boolean streamerClosed = false;
  private boolean wasEmpty = false;
  private boolean useBufferRowExplicit;
  private boolean moreResultsExisted;
  private ExceptionInterceptor exceptionInterceptor;
  
  public RowDataDynamic(MysqlIO io, int colCount, Field[] fields, boolean isBinaryEncoded)
    throws SQLException
  {
    this.io = io;
    this.columnCount = colCount;
    this.isBinaryEncoded = isBinaryEncoded;
    this.metadata = fields;
    this.exceptionInterceptor = this.io.getExceptionInterceptor();
    this.useBufferRowExplicit = MysqlIO.useBufferRowExplicit(this.metadata);
  }
  
  public void addRow(ResultSetRow row)
    throws SQLException
  {
    notSupported();
  }
  
  public void afterLast()
    throws SQLException
  {
    notSupported();
  }
  
  public void beforeFirst()
    throws SQLException
  {
    notSupported();
  }
  
  public void beforeLast()
    throws SQLException
  {
    notSupported();
  }
  
  public void close()
    throws SQLException
  {
    Object mutex = this;
    
    ConnectionImpl conn = null;
    if (this.owner != null)
    {
      conn = this.owner.connection;
      if (conn != null) {
        mutex = conn.getMutex();
      }
    }
    boolean hadMore = false;
    int howMuchMore = 0;
    synchronized (mutex)
    {
      while (next() != null)
      {
        hadMore = true;
        howMuchMore++;
        if (howMuchMore % 100 == 0) {
          Thread.yield();
        }
      }
      if (conn != null)
      {
        if ((!conn.getClobberStreamingResults()) && (conn.getNetTimeoutForStreamingResults() > 0))
        {
          String oldValue = conn.getServerVariable("net_write_timeout");
          if ((oldValue == null) || (oldValue.length() == 0)) {
            oldValue = "60";
          }
          this.io.clearInputStream();
          
          Statement stmt = null;
          try
          {
            stmt = conn.createStatement();
            ((StatementImpl)stmt).executeSimpleNonQuery(conn, "SET net_write_timeout=" + oldValue);
          }
          finally
          {
            if (stmt != null) {
              stmt.close();
            }
          }
        }
        if ((conn.getUseUsageAdvisor()) && 
          (hadMore))
        {
          ProfilerEventHandler eventSink = ProfilerEventHandlerFactory.getInstance(conn);
          
          eventSink.consumeEvent(new ProfilerEvent((byte)0, "", this.owner.owningStatement == null ? "N/A" : this.owner.owningStatement.currentCatalog, this.owner.connectionId, this.owner.owningStatement == null ? -1 : this.owner.owningStatement.getId(), -1, System.currentTimeMillis(), 0L, Constants.MILLIS_I18N, null, null, Messages.getString("RowDataDynamic.2") + howMuchMore + Messages.getString("RowDataDynamic.3") + Messages.getString("RowDataDynamic.4") + Messages.getString("RowDataDynamic.5") + Messages.getString("RowDataDynamic.6") + this.owner.pointOfOrigin));
        }
      }
    }
    this.metadata = null;
    this.owner = null;
  }
  
  public ResultSetRow getAt(int ind)
    throws SQLException
  {
    notSupported();
    
    return null;
  }
  
  public int getCurrentRowNumber()
    throws SQLException
  {
    notSupported();
    
    return -1;
  }
  
  public ResultSetInternalMethods getOwner()
  {
    return this.owner;
  }
  
  public boolean hasNext()
    throws SQLException
  {
    boolean hasNext = this.nextRow != null;
    if ((!hasNext) && (!this.streamerClosed))
    {
      this.io.closeStreamer(this);
      this.streamerClosed = true;
    }
    return hasNext;
  }
  
  public boolean isAfterLast()
    throws SQLException
  {
    return this.isAfterEnd;
  }
  
  public boolean isBeforeFirst()
    throws SQLException
  {
    return this.index < 0;
  }
  
  public boolean isDynamic()
  {
    return true;
  }
  
  public boolean isEmpty()
    throws SQLException
  {
    notSupported();
    
    return false;
  }
  
  public boolean isFirst()
    throws SQLException
  {
    notSupported();
    
    return false;
  }
  
  public boolean isLast()
    throws SQLException
  {
    notSupported();
    
    return false;
  }
  
  public void moveRowRelative(int rows)
    throws SQLException
  {
    notSupported();
  }
  
  public ResultSetRow next()
    throws SQLException
  {
    nextRecord();
    if ((this.nextRow == null) && (!this.streamerClosed) && (!this.moreResultsExisted))
    {
      this.io.closeStreamer(this);
      this.streamerClosed = true;
    }
    if ((this.nextRow != null) && 
      (this.index != Integer.MAX_VALUE)) {
      this.index += 1;
    }
    return this.nextRow;
  }
  
  private void nextRecord()
    throws SQLException
  {
    try
    {
      if (!this.noMoreRows)
      {
        this.nextRow = this.io.nextRow(this.metadata, this.columnCount, this.isBinaryEncoded, 1007, true, this.useBufferRowExplicit, true, null);
        if (this.nextRow == null)
        {
          this.noMoreRows = true;
          this.isAfterEnd = true;
          this.moreResultsExisted = this.io.tackOnMoreStreamingResults(this.owner);
          if (this.index == -1) {
            this.wasEmpty = true;
          }
        }
      }
      else
      {
        this.isAfterEnd = true;
      }
    }
    catch (SQLException sqlEx)
    {
      if ((sqlEx instanceof StreamingNotifiable)) {
        ((StreamingNotifiable)sqlEx).setWasStreamingResults();
      }
      throw sqlEx;
    }
    catch (Exception ex)
    {
      String exceptionType = ex.getClass().getName();
      String exceptionMessage = ex.getMessage();
      
      exceptionMessage = exceptionMessage + Messages.getString("RowDataDynamic.7");
      exceptionMessage = exceptionMessage + Util.stackTraceToString(ex);
      
      SQLException sqlEx = SQLError.createSQLException(Messages.getString("RowDataDynamic.8") + exceptionType + Messages.getString("RowDataDynamic.9") + exceptionMessage, "S1000", this.exceptionInterceptor);
      
      sqlEx.initCause(ex);
      
      throw sqlEx;
    }
  }
  
  private void notSupported()
    throws SQLException
  {
    throw new OperationNotSupportedException();
  }
  
  public void removeRow(int ind)
    throws SQLException
  {
    notSupported();
  }
  
  public void setCurrentRow(int rowNumber)
    throws SQLException
  {
    notSupported();
  }
  
  public void setOwner(ResultSetImpl rs)
  {
    this.owner = rs;
  }
  
  public int size()
  {
    return -1;
  }
  
  public boolean wasEmpty()
  {
    return this.wasEmpty;
  }
  
  public void setMetadata(Field[] metadata)
  {
    this.metadata = metadata;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\mysql\jdbc\RowDataDynamic.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */