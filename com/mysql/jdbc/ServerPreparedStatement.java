package com.mysql.jdbc;

import com.mysql.jdbc.exceptions.MySQLStatementCancelledException;
import com.mysql.jdbc.exceptions.MySQLTimeoutException;
import com.mysql.jdbc.profiler.ProfilerEvent;
import com.mysql.jdbc.profiler.ProfilerEventHandler;
import com.mysql.jdbc.profiler.ProfilerEventHandlerFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.BatchUpdateException;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.ParameterMetaData;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;
import java.util.Timer;

public class ServerPreparedStatement
  extends PreparedStatement
{
  private static final Constructor JDBC_4_SPS_CTOR;
  protected static final int BLOB_STREAM_READ_BUF_SIZE = 8192;
  private static final byte MAX_DATE_REP_LENGTH = 5;
  private static final byte MAX_DATETIME_REP_LENGTH = 12;
  private static final byte MAX_TIME_REP_LENGTH = 13;
  
  static
  {
    if (Util.isJdbc4()) {
      try
      {
        JDBC_4_SPS_CTOR = Class.forName("com.mysql.jdbc.JDBC4ServerPreparedStatement").getConstructor(new Class[] { ConnectionImpl.class, String.class, String.class, Integer.TYPE, Integer.TYPE });
      }
      catch (SecurityException e)
      {
        throw new RuntimeException(e);
      }
      catch (NoSuchMethodException e)
      {
        throw new RuntimeException(e);
      }
      catch (ClassNotFoundException e)
      {
        throw new RuntimeException(e);
      }
    } else {
      JDBC_4_SPS_CTOR = null;
    }
  }
  
  static class BatchedBindValues
  {
    ServerPreparedStatement.BindValue[] batchedParameterValues;
    
    BatchedBindValues(ServerPreparedStatement.BindValue[] paramVals)
    {
      int numParams = paramVals.length;
      
      this.batchedParameterValues = new ServerPreparedStatement.BindValue[numParams];
      for (int i = 0; i < numParams; i++) {
        this.batchedParameterValues[i] = new ServerPreparedStatement.BindValue(paramVals[i]);
      }
    }
  }
  
  public static class BindValue
  {
    long boundBeforeExecutionNum = 0L;
    public long bindLength;
    int bufferType;
    byte byteBinding;
    double doubleBinding;
    float floatBinding;
    int intBinding;
    public boolean isLongData;
    public boolean isNull;
    boolean isSet = false;
    long longBinding;
    short shortBinding;
    public Object value;
    
    BindValue() {}
    
    BindValue(BindValue copyMe)
    {
      this.value = copyMe.value;
      this.isSet = copyMe.isSet;
      this.isLongData = copyMe.isLongData;
      this.isNull = copyMe.isNull;
      this.bufferType = copyMe.bufferType;
      this.bindLength = copyMe.bindLength;
      this.byteBinding = copyMe.byteBinding;
      this.shortBinding = copyMe.shortBinding;
      this.intBinding = copyMe.intBinding;
      this.longBinding = copyMe.longBinding;
      this.floatBinding = copyMe.floatBinding;
      this.doubleBinding = copyMe.doubleBinding;
    }
    
    void reset()
    {
      this.isSet = false;
      this.value = null;
      this.isLongData = false;
      
      this.byteBinding = 0;
      this.shortBinding = 0;
      this.intBinding = 0;
      this.longBinding = 0L;
      this.floatBinding = 0.0F;
      this.doubleBinding = 0.0D;
    }
    
    public String toString()
    {
      return toString(false);
    }
    
    public String toString(boolean quoteIfNeeded)
    {
      if (this.isLongData) {
        return "' STREAM DATA '";
      }
      switch (this.bufferType)
      {
      case 1: 
        return String.valueOf(this.byteBinding);
      case 2: 
        return String.valueOf(this.shortBinding);
      case 3: 
        return String.valueOf(this.intBinding);
      case 8: 
        return String.valueOf(this.longBinding);
      case 4: 
        return String.valueOf(this.floatBinding);
      case 5: 
        return String.valueOf(this.doubleBinding);
      case 7: 
      case 10: 
      case 11: 
      case 12: 
      case 15: 
      case 253: 
      case 254: 
        if (quoteIfNeeded) {
          return "'" + String.valueOf(this.value) + "'";
        }
        return String.valueOf(this.value);
      }
      if ((this.value instanceof byte[])) {
        return "byte data";
      }
      if (quoteIfNeeded) {
        return "'" + String.valueOf(this.value) + "'";
      }
      return String.valueOf(this.value);
    }
    
    long getBoundLength()
    {
      if (this.isNull) {
        return 0L;
      }
      if (this.isLongData) {
        return this.bindLength;
      }
      switch (this.bufferType)
      {
      case 1: 
        return 1L;
      case 2: 
        return 2L;
      case 3: 
        return 4L;
      case 8: 
        return 8L;
      case 4: 
        return 4L;
      case 5: 
        return 8L;
      case 11: 
        return 9L;
      case 10: 
        return 7L;
      case 7: 
      case 12: 
        return 11L;
      case 0: 
      case 15: 
      case 246: 
      case 253: 
      case 254: 
        if ((this.value instanceof byte[])) {
          return ((byte[])this.value).length;
        }
        return ((String)this.value).length();
      }
      return 0L;
    }
  }
  
  private boolean hasOnDuplicateKeyUpdate = false;
  
  private void storeTime(Buffer intoBuf, Time tm)
    throws SQLException
  {
    intoBuf.ensureCapacity(9);
    intoBuf.writeByte((byte)8);
    intoBuf.writeByte((byte)0);
    intoBuf.writeLong(0L);
    
    Calendar sessionCalendar = getCalendarInstanceForSessionOrNew();
    synchronized (sessionCalendar)
    {
      java.util.Date oldTime = sessionCalendar.getTime();
      try
      {
        sessionCalendar.setTime(tm);
        intoBuf.writeByte((byte)sessionCalendar.get(11));
        intoBuf.writeByte((byte)sessionCalendar.get(12));
        intoBuf.writeByte((byte)sessionCalendar.get(13));
      }
      finally
      {
        sessionCalendar.setTime(oldTime);
      }
    }
  }
  
  private boolean detectedLongParameterSwitch = false;
  private int fieldCount;
  private boolean invalid = false;
  private SQLException invalidationException;
  private boolean isSelectQuery;
  private Buffer outByteBuffer;
  private BindValue[] parameterBindings;
  private Field[] parameterFields;
  private Field[] resultFields;
  private boolean sendTypesToServer = false;
  private long serverStatementId;
  private int stringTypeCode = 254;
  private boolean serverNeedsResetBeforeEachExecution;
  
  protected static ServerPreparedStatement getInstance(ConnectionImpl conn, String sql, String catalog, int resultSetType, int resultSetConcurrency)
    throws SQLException
  {
    if (!Util.isJdbc4()) {
      return new ServerPreparedStatement(conn, sql, catalog, resultSetType, resultSetConcurrency);
    }
    try
    {
      return (ServerPreparedStatement)JDBC_4_SPS_CTOR.newInstance(new Object[] { conn, sql, catalog, Constants.integerValueOf(resultSetType), Constants.integerValueOf(resultSetConcurrency) });
    }
    catch (IllegalArgumentException e)
    {
      throw new SQLException(e.toString(), "S1000");
    }
    catch (InstantiationException e)
    {
      throw new SQLException(e.toString(), "S1000");
    }
    catch (IllegalAccessException e)
    {
      throw new SQLException(e.toString(), "S1000");
    }
    catch (InvocationTargetException e)
    {
      Throwable target = e.getTargetException();
      if ((target instanceof SQLException)) {
        throw ((SQLException)target);
      }
      throw new SQLException(target.toString(), "S1000");
    }
  }
  
  protected ServerPreparedStatement(ConnectionImpl conn, String sql, String catalog, int resultSetType, int resultSetConcurrency)
    throws SQLException
  {
    super(conn, catalog);
    
    checkNullOrEmptyQuery(sql);
    
    this.hasOnDuplicateKeyUpdate = containsOnDuplicateKeyInString(sql);
    
    int startOfStatement = findStartOfStatement(sql);
    
    this.firstCharOfStmt = StringUtils.firstAlphaCharUc(sql, startOfStatement);
    
    this.isSelectQuery = ('S' == this.firstCharOfStmt);
    if (this.connection.versionMeetsMinimum(5, 0, 0)) {
      this.serverNeedsResetBeforeEachExecution = (!this.connection.versionMeetsMinimum(5, 0, 3));
    } else {
      this.serverNeedsResetBeforeEachExecution = (!this.connection.versionMeetsMinimum(4, 1, 10));
    }
    this.useAutoSlowLog = this.connection.getAutoSlowLog();
    this.useTrueBoolean = this.connection.versionMeetsMinimum(3, 21, 23);
    this.hasLimitClause = (StringUtils.indexOfIgnoreCase(sql, "LIMIT") != -1);
    
    String statementComment = this.connection.getStatementComment();
    
    this.originalSql = ("/* " + statementComment + " */ " + sql);
    if (this.connection.versionMeetsMinimum(4, 1, 2)) {
      this.stringTypeCode = 253;
    } else {
      this.stringTypeCode = 254;
    }
    try
    {
      serverPrepare(sql);
    }
    catch (SQLException sqlEx)
    {
      realClose(false, true);
      
      throw sqlEx;
    }
    catch (Exception ex)
    {
      realClose(false, true);
      
      SQLException sqlEx = SQLError.createSQLException(ex.toString(), "S1000", getExceptionInterceptor());
      
      sqlEx.initCause(ex);
      
      throw sqlEx;
    }
    setResultSetType(resultSetType);
    setResultSetConcurrency(resultSetConcurrency);
    
    this.parameterTypes = new int[this.parameterCount];
  }
  
  public synchronized void addBatch()
    throws SQLException
  {
    checkClosed();
    if (this.batchedArgs == null) {
      this.batchedArgs = new ArrayList();
    }
    this.batchedArgs.add(new BatchedBindValues(this.parameterBindings));
  }
  
  protected String asSql(boolean quoteStreamsAndUnknowns)
    throws SQLException
  {
    if (this.isClosed) {
      return "statement has been closed, no further internal information available";
    }
    PreparedStatement pStmtForSub = null;
    try
    {
      pStmtForSub = PreparedStatement.getInstance(this.connection, this.originalSql, this.currentCatalog);
      
      int numParameters = pStmtForSub.parameterCount;
      int ourNumParameters = this.parameterCount;
      for (int i = 0; (i < numParameters) && (i < ourNumParameters); i++) {
        if (this.parameterBindings[i] != null) {
          if (this.parameterBindings[i].isNull)
          {
            pStmtForSub.setNull(i + 1, 0);
          }
          else
          {
            BindValue bindValue = this.parameterBindings[i];
            switch (bindValue.bufferType)
            {
            case 1: 
              pStmtForSub.setByte(i + 1, bindValue.byteBinding);
              break;
            case 2: 
              pStmtForSub.setShort(i + 1, bindValue.shortBinding);
              break;
            case 3: 
              pStmtForSub.setInt(i + 1, bindValue.intBinding);
              break;
            case 8: 
              pStmtForSub.setLong(i + 1, bindValue.longBinding);
              break;
            case 4: 
              pStmtForSub.setFloat(i + 1, bindValue.floatBinding);
              break;
            case 5: 
              pStmtForSub.setDouble(i + 1, bindValue.doubleBinding);
              
              break;
            case 6: 
            case 7: 
            default: 
              pStmtForSub.setObject(i + 1, this.parameterBindings[i].value);
            }
          }
        }
      }
      return pStmtForSub.asSql(quoteStreamsAndUnknowns);
    }
    finally
    {
      if (pStmtForSub != null) {
        try
        {
          pStmtForSub.close();
        }
        catch (SQLException sqlEx) {}
      }
    }
  }
  
  protected void checkClosed()
    throws SQLException
  {
    if (this.invalid) {
      throw this.invalidationException;
    }
    super.checkClosed();
  }
  
  public void clearParameters()
    throws SQLException
  {
    checkClosed();
    clearParametersInternal(true);
  }
  
  private void clearParametersInternal(boolean clearServerParameters)
    throws SQLException
  {
    boolean hadLongData = false;
    if (this.parameterBindings != null) {
      for (int i = 0; i < this.parameterCount; i++)
      {
        if ((this.parameterBindings[i] != null) && (this.parameterBindings[i].isLongData)) {
          hadLongData = true;
        }
        this.parameterBindings[i].reset();
      }
    }
    if ((clearServerParameters) && (hadLongData))
    {
      serverResetStatement();
      
      this.detectedLongParameterSwitch = false;
    }
  }
  
  protected boolean isCached = false;
  private boolean useAutoSlowLog;
  private Calendar serverTzCalendar;
  private Calendar defaultTzCalendar;
  
  protected void setClosed(boolean flag)
  {
    this.isClosed = flag;
  }
  
  public synchronized void close()
    throws SQLException
  {
    if ((this.isCached) && (!this.isClosed))
    {
      clearParameters();
      
      this.isClosed = true;
      
      this.connection.recachePreparedStatement(this);
      return;
    }
    realClose(true, true);
  }
  
  private void dumpCloseForTestcase()
  {
    StringBuffer buf = new StringBuffer();
    this.connection.generateConnectionCommentBlock(buf);
    buf.append("DEALLOCATE PREPARE debug_stmt_");
    buf.append(this.statementId);
    buf.append(";\n");
    
    this.connection.dumpTestcaseQuery(buf.toString());
  }
  
  private void dumpExecuteForTestcase()
    throws SQLException
  {
    StringBuffer buf = new StringBuffer();
    for (int i = 0; i < this.parameterCount; i++)
    {
      this.connection.generateConnectionCommentBlock(buf);
      
      buf.append("SET @debug_stmt_param");
      buf.append(this.statementId);
      buf.append("_");
      buf.append(i);
      buf.append("=");
      if (this.parameterBindings[i].isNull) {
        buf.append("NULL");
      } else {
        buf.append(this.parameterBindings[i].toString(true));
      }
      buf.append(";\n");
    }
    this.connection.generateConnectionCommentBlock(buf);
    
    buf.append("EXECUTE debug_stmt_");
    buf.append(this.statementId);
    if (this.parameterCount > 0)
    {
      buf.append(" USING ");
      for (int i = 0; i < this.parameterCount; i++)
      {
        if (i > 0) {
          buf.append(", ");
        }
        buf.append("@debug_stmt_param");
        buf.append(this.statementId);
        buf.append("_");
        buf.append(i);
      }
    }
    buf.append(";\n");
    
    this.connection.dumpTestcaseQuery(buf.toString());
  }
  
  private void dumpPrepareForTestcase()
    throws SQLException
  {
    StringBuffer buf = new StringBuffer(this.originalSql.length() + 64);
    
    this.connection.generateConnectionCommentBlock(buf);
    
    buf.append("PREPARE debug_stmt_");
    buf.append(this.statementId);
    buf.append(" FROM \"");
    buf.append(this.originalSql);
    buf.append("\";\n");
    
    this.connection.dumpTestcaseQuery(buf.toString());
  }
  
  protected int[] executeBatchSerially(int batchTimeout)
    throws SQLException
  {
    ConnectionImpl locallyScopedConn = this.connection;
    if (locallyScopedConn == null) {
      checkClosed();
    }
    if (locallyScopedConn.isReadOnly()) {
      throw SQLError.createSQLException(Messages.getString("ServerPreparedStatement.2") + Messages.getString("ServerPreparedStatement.3"), "S1009", getExceptionInterceptor());
    }
    checkClosed();
    BindValue[] oldBindValues;
    synchronized (locallyScopedConn.getMutex())
    {
      clearWarnings();
      
      oldBindValues = this.parameterBindings;
    }
    try
    {
      int[] updateCounts = null;
      if (this.batchedArgs != null)
      {
        nbrCommands = this.batchedArgs.size();
        updateCounts = new int[nbrCommands];
        if (this.retrieveGeneratedKeys) {
          this.batchedGeneratedKeys = new ArrayList(nbrCommands);
        }
        for (int i = 0; i < nbrCommands; i++) {
          updateCounts[i] = -3;
        }
        SQLException sqlEx = null;
        
        int commandIndex = 0;
        
        BindValue[] previousBindValuesForBatch = null;
        
        StatementImpl.CancelTask timeoutTask = null;
        try
        {
          if ((locallyScopedConn.getEnableQueryTimeouts()) && (batchTimeout != 0) && (locallyScopedConn.versionMeetsMinimum(5, 0, 0)))
          {
            timeoutTask = new StatementImpl.CancelTask(this, this);
            ConnectionImpl.getCancelTimer().schedule(timeoutTask, batchTimeout);
          }
          for (commandIndex = 0; commandIndex < nbrCommands; commandIndex++)
          {
            Object arg = this.batchedArgs.get(commandIndex);
            if ((arg instanceof String))
            {
              updateCounts[commandIndex] = executeUpdate((String)arg);
            }
            else
            {
              this.parameterBindings = ((BatchedBindValues)arg).batchedParameterValues;
              try
              {
                if (previousBindValuesForBatch != null) {
                  for (int j = 0; j < this.parameterBindings.length; j++) {
                    if (this.parameterBindings[j].bufferType != previousBindValuesForBatch[j].bufferType)
                    {
                      this.sendTypesToServer = true;
                      
                      break;
                    }
                  }
                }
                try
                {
                  updateCounts[commandIndex] = executeUpdate(false, true);
                }
                finally
                {
                  previousBindValuesForBatch = this.parameterBindings;
                }
                if (this.retrieveGeneratedKeys)
                {
                  ResultSet rs = null;
                  try
                  {
                    rs = getGeneratedKeysInternal();
                    while (rs.next()) {
                      this.batchedGeneratedKeys.add(new ByteArrayRow(new byte[][] { rs.getBytes(1) }, getExceptionInterceptor()));
                    }
                  }
                  finally
                  {
                    if (rs != null) {
                      rs.close();
                    }
                  }
                }
              }
              catch (SQLException ex)
              {
                updateCounts[commandIndex] = -3;
                if ((this.continueBatchOnError) && (!(ex instanceof MySQLTimeoutException)) && (!(ex instanceof MySQLStatementCancelledException)) && (!hasDeadlockOrTimeoutRolledBackTx(ex)))
                {
                  sqlEx = ex;
                }
                else
                {
                  int[] newUpdateCounts = new int[commandIndex];
                  System.arraycopy(updateCounts, 0, newUpdateCounts, 0, commandIndex);
                  
                  throw new BatchUpdateException(ex.getMessage(), ex.getSQLState(), ex.getErrorCode(), newUpdateCounts);
                }
              }
            }
          }
        }
        finally
        {
          if (timeoutTask != null) {
            timeoutTask.cancel();
          }
          resetCancelledState();
        }
        if (sqlEx != null) {
          throw new BatchUpdateException(sqlEx.getMessage(), sqlEx.getSQLState(), sqlEx.getErrorCode(), updateCounts);
        }
      }
      int nbrCommands = updateCounts != null ? updateCounts : new int[0];jsr 16;return nbrCommands;
    }
    finally
    {
      jsr 6;
    }
    localObject6 = returnAddress;this.parameterBindings = oldBindValues;
    this.sendTypesToServer = true;
    
    clearBatch();ret;
    
    localObject7 = finally;throw ((Throwable)localObject7);
  }
  
  protected ResultSetInternalMethods executeInternal(int maxRowsToRetrieve, Buffer sendPacket, boolean createStreamingResultSet, boolean queryIsSelectOnly, Field[] metadataFromCache, boolean isBatch)
    throws SQLException
  {
    this.numberOfExecutions += 1;
    try
    {
      return serverExecute(maxRowsToRetrieve, createStreamingResultSet, metadataFromCache);
    }
    catch (SQLException sqlEx)
    {
      if (this.connection.getEnablePacketDebug()) {
        this.connection.getIO().dumpPacketRingBuffer();
      }
      if (this.connection.getDumpQueriesOnException())
      {
        String extractedSql = toString();
        StringBuffer messageBuf = new StringBuffer(extractedSql.length() + 32);
        
        messageBuf.append("\n\nQuery being executed when exception was thrown:\n\n");
        
        messageBuf.append(extractedSql);
        
        sqlEx = ConnectionImpl.appendMessageToException(sqlEx, messageBuf.toString(), getExceptionInterceptor());
      }
      throw sqlEx;
    }
    catch (Exception ex)
    {
      if (this.connection.getEnablePacketDebug()) {
        this.connection.getIO().dumpPacketRingBuffer();
      }
      SQLException sqlEx = SQLError.createSQLException(ex.toString(), "S1000", getExceptionInterceptor());
      if (this.connection.getDumpQueriesOnException())
      {
        String extractedSql = toString();
        StringBuffer messageBuf = new StringBuffer(extractedSql.length() + 32);
        
        messageBuf.append("\n\nQuery being executed when exception was thrown:\n\n");
        
        messageBuf.append(extractedSql);
        
        sqlEx = ConnectionImpl.appendMessageToException(sqlEx, messageBuf.toString(), getExceptionInterceptor());
      }
      sqlEx.initCause(ex);
      
      throw sqlEx;
    }
  }
  
  protected Buffer fillSendPacket()
    throws SQLException
  {
    return null;
  }
  
  protected Buffer fillSendPacket(byte[][] batchedParameterStrings, InputStream[] batchedParameterStreams, boolean[] batchedIsStream, int[] batchedStreamLengths)
    throws SQLException
  {
    return null;
  }
  
  protected BindValue getBinding(int parameterIndex, boolean forLongData)
    throws SQLException
  {
    checkClosed();
    if (this.parameterBindings.length == 0) {
      throw SQLError.createSQLException(Messages.getString("ServerPreparedStatement.8"), "S1009", getExceptionInterceptor());
    }
    parameterIndex--;
    if ((parameterIndex < 0) || (parameterIndex >= this.parameterBindings.length)) {
      throw SQLError.createSQLException(Messages.getString("ServerPreparedStatement.9") + (parameterIndex + 1) + Messages.getString("ServerPreparedStatement.10") + this.parameterBindings.length, "S1009", getExceptionInterceptor());
    }
    if (this.parameterBindings[parameterIndex] == null) {
      this.parameterBindings[parameterIndex] = new BindValue();
    } else if ((this.parameterBindings[parameterIndex].isLongData) && (!forLongData)) {
      this.detectedLongParameterSwitch = true;
    }
    this.parameterBindings[parameterIndex].isSet = true;
    this.parameterBindings[parameterIndex].boundBeforeExecutionNum = this.numberOfExecutions;
    
    return this.parameterBindings[parameterIndex];
  }
  
  byte[] getBytes(int parameterIndex)
    throws SQLException
  {
    BindValue bindValue = getBinding(parameterIndex, false);
    if (bindValue.isNull) {
      return null;
    }
    if (bindValue.isLongData) {
      throw SQLError.notImplemented();
    }
    if (this.outByteBuffer == null) {
      this.outByteBuffer = new Buffer(this.connection.getNetBufferLength());
    }
    this.outByteBuffer.clear();
    
    int originalPosition = this.outByteBuffer.getPosition();
    
    storeBinding(this.outByteBuffer, bindValue, this.connection.getIO());
    
    int newPosition = this.outByteBuffer.getPosition();
    
    int length = newPosition - originalPosition;
    
    byte[] valueAsBytes = new byte[length];
    
    System.arraycopy(this.outByteBuffer.getByteBuffer(), originalPosition, valueAsBytes, 0, length);
    
    return valueAsBytes;
  }
  
  public java.sql.ResultSetMetaData getMetaData()
    throws SQLException
  {
    checkClosed();
    if (this.resultFields == null) {
      return null;
    }
    return new ResultSetMetaData(this.resultFields, this.connection.getUseOldAliasMetadataBehavior(), getExceptionInterceptor());
  }
  
  public ParameterMetaData getParameterMetaData()
    throws SQLException
  {
    checkClosed();
    if (this.parameterMetaData == null) {
      this.parameterMetaData = new MysqlParameterMetadata(this.parameterFields, this.parameterCount, getExceptionInterceptor());
    }
    return this.parameterMetaData;
  }
  
  boolean isNull(int paramIndex)
  {
    throw new IllegalArgumentException(Messages.getString("ServerPreparedStatement.7"));
  }
  
  protected void realClose(boolean calledExplicitly, boolean closeOpenResults)
    throws SQLException
  {
    if (this.isClosed) {
      return;
    }
    if (this.connection != null)
    {
      if (this.connection.getAutoGenerateTestcaseScript()) {
        dumpCloseForTestcase();
      }
      SQLException exceptionDuringClose = null;
      if ((calledExplicitly) && (!this.connection.isClosed())) {
        synchronized (this.connection.getMutex())
        {
          try
          {
            MysqlIO mysql = this.connection.getIO();
            
            Buffer packet = mysql.getSharedSendPacket();
            
            packet.writeByte((byte)25);
            packet.writeLong(this.serverStatementId);
            
            mysql.sendCommand(25, null, packet, true, null, 0);
          }
          catch (SQLException sqlEx)
          {
            exceptionDuringClose = sqlEx;
          }
        }
      }
      super.realClose(calledExplicitly, closeOpenResults);
      
      clearParametersInternal(false);
      this.parameterBindings = null;
      
      this.parameterFields = null;
      this.resultFields = null;
      if (exceptionDuringClose != null) {
        throw exceptionDuringClose;
      }
    }
  }
  
  protected void rePrepare()
    throws SQLException
  {
    this.invalidationException = null;
    try
    {
      serverPrepare(this.originalSql);
    }
    catch (SQLException sqlEx)
    {
      this.invalidationException = sqlEx;
    }
    catch (Exception ex)
    {
      this.invalidationException = SQLError.createSQLException(ex.toString(), "S1000", getExceptionInterceptor());
      
      this.invalidationException.initCause(ex);
    }
    if (this.invalidationException != null)
    {
      this.invalid = true;
      
      this.parameterBindings = null;
      
      this.parameterFields = null;
      this.resultFields = null;
      if (this.results != null) {
        try
        {
          this.results.close();
        }
        catch (Exception ex) {}
      }
      if (this.connection != null)
      {
        if (this.maxRowsChanged) {
          this.connection.unsetMaxRows(this);
        }
        if (!this.connection.getDontTrackOpenResources()) {
          this.connection.unregisterStatement(this);
        }
      }
    }
  }
  
  private ResultSetInternalMethods serverExecute(int maxRowsToRetrieve, boolean createStreamingResultSet, Field[] metadataFromCache)
    throws SQLException
  {
    synchronized (this.connection.getMutex())
    {
      if (this.detectedLongParameterSwitch)
      {
        boolean firstFound = false;
        long boundTimeToCheck = 0L;
        for (int i = 0; i < this.parameterCount - 1; i++) {
          if (this.parameterBindings[i].isLongData)
          {
            if ((firstFound) && (boundTimeToCheck != this.parameterBindings[i].boundBeforeExecutionNum)) {
              throw SQLError.createSQLException(Messages.getString("ServerPreparedStatement.11") + Messages.getString("ServerPreparedStatement.12"), "S1C00", getExceptionInterceptor());
            }
            firstFound = true;
            boundTimeToCheck = this.parameterBindings[i].boundBeforeExecutionNum;
          }
        }
        serverResetStatement();
      }
      for (int i = 0; i < this.parameterCount; i++) {
        if (!this.parameterBindings[i].isSet) {
          throw SQLError.createSQLException(Messages.getString("ServerPreparedStatement.13") + (i + 1) + Messages.getString("ServerPreparedStatement.14"), "S1009", getExceptionInterceptor());
        }
      }
      for (int i = 0; i < this.parameterCount; i++) {
        if (this.parameterBindings[i].isLongData) {
          serverLongData(i, this.parameterBindings[i]);
        }
      }
      if (this.connection.getAutoGenerateTestcaseScript()) {
        dumpExecuteForTestcase();
      }
      MysqlIO mysql = this.connection.getIO();
      
      Buffer packet = mysql.getSharedSendPacket();
      
      packet.clear();
      packet.writeByte((byte)23);
      packet.writeLong(this.serverStatementId);
      
      boolean usingCursor = false;
      if (this.connection.versionMeetsMinimum(4, 1, 2))
      {
        if ((this.resultFields != null) && (this.connection.isCursorFetchEnabled()) && (getResultSetType() == 1003) && (getResultSetConcurrency() == 1007) && (getFetchSize() > 0))
        {
          packet.writeByte((byte)1);
          usingCursor = true;
        }
        else
        {
          packet.writeByte((byte)0);
        }
        packet.writeLong(1L);
      }
      int nullCount = (this.parameterCount + 7) / 8;
      
      int nullBitsPosition = packet.getPosition();
      for (int i = 0; i < nullCount; i++) {
        packet.writeByte((byte)0);
      }
      byte[] nullBitsBuffer = new byte[nullCount];
      
      packet.writeByte((byte)(this.sendTypesToServer ? 1 : 0));
      if (this.sendTypesToServer) {
        for (int i = 0; i < this.parameterCount; i++) {
          packet.writeInt(this.parameterBindings[i].bufferType);
        }
      }
      for (int i = 0; i < this.parameterCount; i++) {
        if (!this.parameterBindings[i].isLongData) {
          if (!this.parameterBindings[i].isNull)
          {
            storeBinding(packet, this.parameterBindings[i], mysql);
          }
          else
          {
            int tmp553_552 = (i / 8); byte[] tmp553_546 = nullBitsBuffer;tmp553_546[tmp553_552] = ((byte)(tmp553_546[tmp553_552] | 1 << (i & 0x7)));
          }
        }
      }
      int endPosition = packet.getPosition();
      packet.setPosition(nullBitsPosition);
      packet.writeBytesNoNull(nullBitsBuffer);
      packet.setPosition(endPosition);
      
      long begin = 0L;
      
      boolean logSlowQueries = this.connection.getLogSlowQueries();
      boolean gatherPerformanceMetrics = this.connection.getGatherPerformanceMetrics();
      if ((this.profileSQL) || (logSlowQueries) || (gatherPerformanceMetrics)) {
        begin = mysql.getCurrentTimeNanosOrMillis();
      }
      resetCancelledState();
      
      StatementImpl.CancelTask timeoutTask = null;
      try
      {
        if ((this.connection.getEnableQueryTimeouts()) && (this.timeoutInMillis != 0) && (this.connection.versionMeetsMinimum(5, 0, 0)))
        {
          timeoutTask = new StatementImpl.CancelTask(this, this);
          ConnectionImpl.getCancelTimer().schedule(timeoutTask, this.timeoutInMillis);
        }
        Buffer resultPacket = mysql.sendCommand(23, null, packet, false, null, 0);
        
        long queryEndTime = 0L;
        if ((logSlowQueries) || (gatherPerformanceMetrics) || (this.profileSQL)) {
          queryEndTime = mysql.getCurrentTimeNanosOrMillis();
        }
        if (timeoutTask != null)
        {
          timeoutTask.cancel();
          if (timeoutTask.caughtWhileCancelling != null) {
            throw timeoutTask.caughtWhileCancelling;
          }
          timeoutTask = null;
        }
        synchronized (this.cancelTimeoutMutex)
        {
          if (this.wasCancelled)
          {
            SQLException cause = null;
            if (this.wasCancelledByTimeout) {
              cause = new MySQLTimeoutException();
            } else {
              cause = new MySQLStatementCancelledException();
            }
            resetCancelledState();
            
            throw cause;
          }
        }
        boolean queryWasSlow = false;
        if ((logSlowQueries) || (gatherPerformanceMetrics))
        {
          long elapsedTime = queryEndTime - begin;
          if (logSlowQueries) {
            if (this.useAutoSlowLog)
            {
              queryWasSlow = elapsedTime > this.connection.getSlowQueryThresholdMillis();
            }
            else
            {
              queryWasSlow = this.connection.isAbonormallyLongQuery(elapsedTime);
              
              this.connection.reportQueryTime(elapsedTime);
            }
          }
          if (queryWasSlow)
          {
            StringBuffer mesgBuf = new StringBuffer(48 + this.originalSql.length());
            
            mesgBuf.append(Messages.getString("ServerPreparedStatement.15"));
            
            mesgBuf.append(mysql.getSlowQueryThreshold());
            mesgBuf.append(Messages.getString("ServerPreparedStatement.15a"));
            
            mesgBuf.append(elapsedTime);
            mesgBuf.append(Messages.getString("ServerPreparedStatement.16"));
            
            mesgBuf.append("as prepared: ");
            mesgBuf.append(this.originalSql);
            mesgBuf.append("\n\n with parameters bound:\n\n");
            mesgBuf.append(asSql(true));
            
            this.eventSink.consumeEvent(new ProfilerEvent((byte)6, "", this.currentCatalog, this.connection.getId(), getId(), 0, System.currentTimeMillis(), elapsedTime, mysql.getQueryTimingUnits(), null, new Throwable(), mesgBuf.toString()));
          }
          if (gatherPerformanceMetrics) {
            this.connection.registerQueryExecutionTime(elapsedTime);
          }
        }
        this.connection.incrementNumberOfPreparedExecutes();
        if (this.profileSQL)
        {
          this.eventSink = ProfilerEventHandlerFactory.getInstance(this.connection);
          
          this.eventSink.consumeEvent(new ProfilerEvent((byte)4, "", this.currentCatalog, this.connectionId, this.statementId, -1, System.currentTimeMillis(), (int)(mysql.getCurrentTimeNanosOrMillis() - begin), mysql.getQueryTimingUnits(), null, new Throwable(), truncateQueryToLog(asSql(true))));
        }
        ResultSetInternalMethods rs = mysql.readAllResults(this, maxRowsToRetrieve, this.resultSetType, this.resultSetConcurrency, createStreamingResultSet, this.currentCatalog, resultPacket, true, this.fieldCount, metadataFromCache);
        if (this.profileSQL)
        {
          long fetchEndTime = mysql.getCurrentTimeNanosOrMillis();
          
          this.eventSink.consumeEvent(new ProfilerEvent((byte)5, "", this.currentCatalog, this.connection.getId(), getId(), 0, System.currentTimeMillis(), fetchEndTime - queryEndTime, mysql.getQueryTimingUnits(), null, new Throwable(), null));
        }
        if ((queryWasSlow) && (this.connection.getExplainSlowQueries()))
        {
          queryAsString = asSql(true);
          
          mysql.explainSlowQuery(((String)queryAsString).getBytes(), (String)queryAsString);
        }
        if ((!createStreamingResultSet) && (this.serverNeedsResetBeforeEachExecution)) {
          serverResetStatement();
        }
        this.sendTypesToServer = false;
        this.results = rs;
        if (mysql.hadWarnings()) {
          mysql.scanForAndThrowDataTruncation();
        }
        Object queryAsString = rs;
        if (timeoutTask != null) {
          timeoutTask.cancel();
        }
        return (ResultSetInternalMethods)queryAsString;
      }
      finally
      {
        if (timeoutTask != null) {
          timeoutTask.cancel();
        }
      }
    }
  }
  
  private void serverLongData(int parameterIndex, BindValue longData)
    throws SQLException
  {
    synchronized (this.connection.getMutex())
    {
      MysqlIO mysql = this.connection.getIO();
      
      Buffer packet = mysql.getSharedSendPacket();
      
      Object value = longData.value;
      if ((value instanceof byte[]))
      {
        packet.clear();
        packet.writeByte((byte)24);
        packet.writeLong(this.serverStatementId);
        packet.writeInt(parameterIndex);
        
        packet.writeBytesNoNull((byte[])longData.value);
        
        mysql.sendCommand(24, null, packet, true, null, 0);
      }
      else if ((value instanceof InputStream))
      {
        storeStream(mysql, parameterIndex, packet, (InputStream)value);
      }
      else if ((value instanceof Blob))
      {
        storeStream(mysql, parameterIndex, packet, ((Blob)value).getBinaryStream());
      }
      else if ((value instanceof Reader))
      {
        storeReader(mysql, parameterIndex, packet, (Reader)value);
      }
      else
      {
        throw SQLError.createSQLException(Messages.getString("ServerPreparedStatement.18") + value.getClass().getName() + "'", "S1009", getExceptionInterceptor());
      }
    }
  }
  
  private void serverPrepare(String sql)
    throws SQLException
  {
    synchronized (this.connection.getMutex())
    {
      MysqlIO mysql = this.connection.getIO();
      if (this.connection.getAutoGenerateTestcaseScript()) {
        dumpPrepareForTestcase();
      }
      try
      {
        long begin = 0L;
        if (StringUtils.startsWithIgnoreCaseAndWs(sql, "LOAD DATA")) {
          this.isLoadDataQuery = true;
        } else {
          this.isLoadDataQuery = false;
        }
        if (this.connection.getProfileSql()) {
          begin = System.currentTimeMillis();
        }
        String characterEncoding = null;
        String connectionEncoding = this.connection.getEncoding();
        if ((!this.isLoadDataQuery) && (this.connection.getUseUnicode()) && (connectionEncoding != null)) {
          characterEncoding = connectionEncoding;
        }
        Buffer prepareResultPacket = mysql.sendCommand(22, sql, null, false, characterEncoding, 0);
        if (this.connection.versionMeetsMinimum(4, 1, 1)) {
          prepareResultPacket.setPosition(1);
        } else {
          prepareResultPacket.setPosition(0);
        }
        this.serverStatementId = prepareResultPacket.readLong();
        this.fieldCount = prepareResultPacket.readInt();
        this.parameterCount = prepareResultPacket.readInt();
        this.parameterBindings = new BindValue[this.parameterCount];
        for (int i = 0; i < this.parameterCount; i++) {
          this.parameterBindings[i] = new BindValue();
        }
        this.connection.incrementNumberOfPrepares();
        if (this.profileSQL) {
          this.eventSink.consumeEvent(new ProfilerEvent((byte)2, "", this.currentCatalog, this.connectionId, this.statementId, -1, System.currentTimeMillis(), mysql.getCurrentTimeNanosOrMillis() - begin, mysql.getQueryTimingUnits(), null, new Throwable(), truncateQueryToLog(sql)));
        }
        if ((this.parameterCount > 0) && 
          (this.connection.versionMeetsMinimum(4, 1, 2)) && (!mysql.isVersion(5, 0, 0)))
        {
          this.parameterFields = new Field[this.parameterCount];
          
          Buffer metaDataPacket = mysql.readPacket();
          
          int i = 0;
          while ((!metaDataPacket.isLastDataPacket()) && (i < this.parameterCount))
          {
            this.parameterFields[(i++)] = mysql.unpackField(metaDataPacket, false);
            
            metaDataPacket = mysql.readPacket();
          }
        }
        if (this.fieldCount > 0)
        {
          this.resultFields = new Field[this.fieldCount];
          
          Buffer fieldPacket = mysql.readPacket();
          
          int i = 0;
          while ((!fieldPacket.isLastDataPacket()) && (i < this.fieldCount))
          {
            this.resultFields[(i++)] = mysql.unpackField(fieldPacket, false);
            
            fieldPacket = mysql.readPacket();
          }
        }
      }
      catch (SQLException sqlEx)
      {
        if (this.connection.getDumpQueriesOnException())
        {
          StringBuffer messageBuf = new StringBuffer(this.originalSql.length() + 32);
          
          messageBuf.append("\n\nQuery being prepared when exception was thrown:\n\n");
          
          messageBuf.append(this.originalSql);
          
          sqlEx = ConnectionImpl.appendMessageToException(sqlEx, messageBuf.toString(), getExceptionInterceptor());
        }
        throw sqlEx;
      }
      finally
      {
        this.connection.getIO().clearInputStream();
      }
    }
  }
  
  private String truncateQueryToLog(String sql)
  {
    String query = null;
    if (sql.length() > this.connection.getMaxQuerySizeToLog())
    {
      StringBuffer queryBuf = new StringBuffer(this.connection.getMaxQuerySizeToLog() + 12);
      
      queryBuf.append(sql.substring(0, this.connection.getMaxQuerySizeToLog()));
      queryBuf.append(Messages.getString("MysqlIO.25"));
      
      query = queryBuf.toString();
    }
    else
    {
      query = sql;
    }
    return query;
  }
  
  private void serverResetStatement()
    throws SQLException
  {
    synchronized (this.connection.getMutex())
    {
      MysqlIO mysql = this.connection.getIO();
      
      Buffer packet = mysql.getSharedSendPacket();
      
      packet.clear();
      packet.writeByte((byte)26);
      packet.writeLong(this.serverStatementId);
      try
      {
        mysql.sendCommand(26, null, packet, !this.connection.versionMeetsMinimum(4, 1, 2), null, 0);
      }
      catch (SQLException sqlEx)
      {
        throw sqlEx;
      }
      catch (Exception ex)
      {
        SQLException sqlEx = SQLError.createSQLException(ex.toString(), "S1000", getExceptionInterceptor());
        
        sqlEx.initCause(ex);
        
        throw sqlEx;
      }
      finally
      {
        mysql.clearInputStream();
      }
    }
  }
  
  public void setArray(int i, Array x)
    throws SQLException
  {
    throw SQLError.notImplemented();
  }
  
  public void setAsciiStream(int parameterIndex, InputStream x, int length)
    throws SQLException
  {
    checkClosed();
    if (x == null)
    {
      setNull(parameterIndex, -2);
    }
    else
    {
      BindValue binding = getBinding(parameterIndex, true);
      setType(binding, 252);
      
      binding.value = x;
      binding.isNull = false;
      binding.isLongData = true;
      if (this.connection.getUseStreamLengthsInPrepStmts()) {
        binding.bindLength = length;
      } else {
        binding.bindLength = -1L;
      }
    }
  }
  
  public void setBigDecimal(int parameterIndex, BigDecimal x)
    throws SQLException
  {
    checkClosed();
    if (x == null)
    {
      setNull(parameterIndex, 3);
    }
    else
    {
      BindValue binding = getBinding(parameterIndex, false);
      if (this.connection.versionMeetsMinimum(5, 0, 3)) {
        setType(binding, 246);
      } else {
        setType(binding, this.stringTypeCode);
      }
      binding.value = StringUtils.fixDecimalExponent(StringUtils.consistentToString(x));
      
      binding.isNull = false;
      binding.isLongData = false;
    }
  }
  
  public void setBinaryStream(int parameterIndex, InputStream x, int length)
    throws SQLException
  {
    checkClosed();
    if (x == null)
    {
      setNull(parameterIndex, -2);
    }
    else
    {
      BindValue binding = getBinding(parameterIndex, true);
      setType(binding, 252);
      
      binding.value = x;
      binding.isNull = false;
      binding.isLongData = true;
      if (this.connection.getUseStreamLengthsInPrepStmts()) {
        binding.bindLength = length;
      } else {
        binding.bindLength = -1L;
      }
    }
  }
  
  public void setBlob(int parameterIndex, Blob x)
    throws SQLException
  {
    checkClosed();
    if (x == null)
    {
      setNull(parameterIndex, -2);
    }
    else
    {
      BindValue binding = getBinding(parameterIndex, true);
      setType(binding, 252);
      
      binding.value = x;
      binding.isNull = false;
      binding.isLongData = true;
      if (this.connection.getUseStreamLengthsInPrepStmts()) {
        binding.bindLength = x.length();
      } else {
        binding.bindLength = -1L;
      }
    }
  }
  
  public void setBoolean(int parameterIndex, boolean x)
    throws SQLException
  {
    setByte(parameterIndex, (byte)(x ? 1 : 0));
  }
  
  public void setByte(int parameterIndex, byte x)
    throws SQLException
  {
    checkClosed();
    
    BindValue binding = getBinding(parameterIndex, false);
    setType(binding, 1);
    
    binding.value = null;
    binding.byteBinding = x;
    binding.isNull = false;
    binding.isLongData = false;
  }
  
  public void setBytes(int parameterIndex, byte[] x)
    throws SQLException
  {
    checkClosed();
    if (x == null)
    {
      setNull(parameterIndex, -2);
    }
    else
    {
      BindValue binding = getBinding(parameterIndex, false);
      setType(binding, 253);
      
      binding.value = x;
      binding.isNull = false;
      binding.isLongData = false;
    }
  }
  
  public void setCharacterStream(int parameterIndex, Reader reader, int length)
    throws SQLException
  {
    checkClosed();
    if (reader == null)
    {
      setNull(parameterIndex, -2);
    }
    else
    {
      BindValue binding = getBinding(parameterIndex, true);
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
  
  public void setClob(int parameterIndex, Clob x)
    throws SQLException
  {
    checkClosed();
    if (x == null)
    {
      setNull(parameterIndex, -2);
    }
    else
    {
      BindValue binding = getBinding(parameterIndex, true);
      setType(binding, 252);
      
      binding.value = x.getCharacterStream();
      binding.isNull = false;
      binding.isLongData = true;
      if (this.connection.getUseStreamLengthsInPrepStmts()) {
        binding.bindLength = x.length();
      } else {
        binding.bindLength = -1L;
      }
    }
  }
  
  public void setDate(int parameterIndex, java.sql.Date x)
    throws SQLException
  {
    setDate(parameterIndex, x, null);
  }
  
  public void setDate(int parameterIndex, java.sql.Date x, Calendar cal)
    throws SQLException
  {
    if (x == null)
    {
      setNull(parameterIndex, 91);
    }
    else
    {
      BindValue binding = getBinding(parameterIndex, false);
      setType(binding, 10);
      
      binding.value = x;
      binding.isNull = false;
      binding.isLongData = false;
    }
  }
  
  public void setDouble(int parameterIndex, double x)
    throws SQLException
  {
    checkClosed();
    if ((!this.connection.getAllowNanAndInf()) && ((x == Double.POSITIVE_INFINITY) || (x == Double.NEGATIVE_INFINITY) || (Double.isNaN(x)))) {
      throw SQLError.createSQLException("'" + x + "' is not a valid numeric or approximate numeric value", "S1009", getExceptionInterceptor());
    }
    BindValue binding = getBinding(parameterIndex, false);
    setType(binding, 5);
    
    binding.value = null;
    binding.doubleBinding = x;
    binding.isNull = false;
    binding.isLongData = false;
  }
  
  public void setFloat(int parameterIndex, float x)
    throws SQLException
  {
    checkClosed();
    
    BindValue binding = getBinding(parameterIndex, false);
    setType(binding, 4);
    
    binding.value = null;
    binding.floatBinding = x;
    binding.isNull = false;
    binding.isLongData = false;
  }
  
  public void setInt(int parameterIndex, int x)
    throws SQLException
  {
    checkClosed();
    
    BindValue binding = getBinding(parameterIndex, false);
    setType(binding, 3);
    
    binding.value = null;
    binding.intBinding = x;
    binding.isNull = false;
    binding.isLongData = false;
  }
  
  public void setLong(int parameterIndex, long x)
    throws SQLException
  {
    checkClosed();
    
    BindValue binding = getBinding(parameterIndex, false);
    setType(binding, 8);
    
    binding.value = null;
    binding.longBinding = x;
    binding.isNull = false;
    binding.isLongData = false;
  }
  
  public void setNull(int parameterIndex, int sqlType)
    throws SQLException
  {
    checkClosed();
    
    BindValue binding = getBinding(parameterIndex, false);
    if (binding.bufferType == 0) {
      setType(binding, 6);
    }
    binding.value = null;
    binding.isNull = true;
    binding.isLongData = false;
  }
  
  public void setNull(int parameterIndex, int sqlType, String typeName)
    throws SQLException
  {
    checkClosed();
    
    BindValue binding = getBinding(parameterIndex, false);
    if (binding.bufferType == 0) {
      setType(binding, 6);
    }
    binding.value = null;
    binding.isNull = true;
    binding.isLongData = false;
  }
  
  public void setRef(int i, Ref x)
    throws SQLException
  {
    throw SQLError.notImplemented();
  }
  
  public void setShort(int parameterIndex, short x)
    throws SQLException
  {
    checkClosed();
    
    BindValue binding = getBinding(parameterIndex, false);
    setType(binding, 2);
    
    binding.value = null;
    binding.shortBinding = x;
    binding.isNull = false;
    binding.isLongData = false;
  }
  
  public void setString(int parameterIndex, String x)
    throws SQLException
  {
    checkClosed();
    if (x == null)
    {
      setNull(parameterIndex, 1);
    }
    else
    {
      BindValue binding = getBinding(parameterIndex, false);
      
      setType(binding, this.stringTypeCode);
      
      binding.value = x;
      binding.isNull = false;
      binding.isLongData = false;
    }
  }
  
  public void setTime(int parameterIndex, Time x)
    throws SQLException
  {
    setTimeInternal(parameterIndex, x, null, this.connection.getDefaultTimeZone(), false);
  }
  
  public void setTime(int parameterIndex, Time x, Calendar cal)
    throws SQLException
  {
    setTimeInternal(parameterIndex, x, cal, cal.getTimeZone(), true);
  }
  
  public void setTimeInternal(int parameterIndex, Time x, Calendar targetCalendar, TimeZone tz, boolean rollForward)
    throws SQLException
  {
    if (x == null)
    {
      setNull(parameterIndex, 92);
    }
    else
    {
      BindValue binding = getBinding(parameterIndex, false);
      setType(binding, 11);
      if (!this.useLegacyDatetimeCode)
      {
        binding.value = x;
      }
      else
      {
        Calendar sessionCalendar = getCalendarInstanceForSessionOrNew();
        synchronized (sessionCalendar)
        {
          binding.value = TimeUtil.changeTimezone(this.connection, sessionCalendar, targetCalendar, x, tz, this.connection.getServerTimezoneTZ(), rollForward);
        }
      }
      binding.isNull = false;
      binding.isLongData = false;
    }
  }
  
  public void setTimestamp(int parameterIndex, Timestamp x)
    throws SQLException
  {
    setTimestampInternal(parameterIndex, x, null, this.connection.getDefaultTimeZone(), false);
  }
  
  public void setTimestamp(int parameterIndex, Timestamp x, Calendar cal)
    throws SQLException
  {
    setTimestampInternal(parameterIndex, x, cal, cal.getTimeZone(), true);
  }
  
  protected void setTimestampInternal(int parameterIndex, Timestamp x, Calendar targetCalendar, TimeZone tz, boolean rollForward)
    throws SQLException
  {
    if (x == null)
    {
      setNull(parameterIndex, 93);
    }
    else
    {
      BindValue binding = getBinding(parameterIndex, false);
      setType(binding, 12);
      if (!this.useLegacyDatetimeCode)
      {
        binding.value = x;
      }
      else
      {
        Calendar sessionCalendar = this.connection.getUseJDBCCompliantTimezoneShift() ? this.connection.getUtcCalendar() : getCalendarInstanceForSessionOrNew();
        synchronized (sessionCalendar)
        {
          binding.value = TimeUtil.changeTimezone(this.connection, sessionCalendar, targetCalendar, x, tz, this.connection.getServerTimezoneTZ(), rollForward);
        }
        binding.isNull = false;
        binding.isLongData = false;
      }
    }
  }
  
  protected void setType(BindValue oldValue, int bufferType)
  {
    if (oldValue.bufferType != bufferType) {
      this.sendTypesToServer = true;
    }
    oldValue.bufferType = bufferType;
  }
  
  /**
   * @deprecated
   */
  public void setUnicodeStream(int parameterIndex, InputStream x, int length)
    throws SQLException
  {
    checkClosed();
    
    throw SQLError.notImplemented();
  }
  
  public void setURL(int parameterIndex, URL x)
    throws SQLException
  {
    checkClosed();
    
    setString(parameterIndex, x.toString());
  }
  
  private void storeBinding(Buffer packet, BindValue bindValue, MysqlIO mysql)
    throws SQLException
  {
    try
    {
      Object value = bindValue.value;
      switch (bindValue.bufferType)
      {
      case 1: 
        packet.writeByte(bindValue.byteBinding);
        return;
      case 2: 
        packet.ensureCapacity(2);
        packet.writeInt(bindValue.shortBinding);
        return;
      case 3: 
        packet.ensureCapacity(4);
        packet.writeLong(bindValue.intBinding);
        return;
      case 8: 
        packet.ensureCapacity(8);
        packet.writeLongLong(bindValue.longBinding);
        return;
      case 4: 
        packet.ensureCapacity(4);
        packet.writeFloat(bindValue.floatBinding);
        return;
      case 5: 
        packet.ensureCapacity(8);
        packet.writeDouble(bindValue.doubleBinding);
        return;
      case 11: 
        storeTime(packet, (Time)value);
        return;
      case 7: 
      case 10: 
      case 12: 
        storeDateTime(packet, (java.util.Date)value, mysql, bindValue.bufferType);
        return;
      case 0: 
      case 15: 
      case 246: 
      case 253: 
      case 254: 
        if ((value instanceof byte[])) {
          packet.writeLenBytes((byte[])value);
        } else if (!this.isLoadDataQuery) {
          packet.writeLenString((String)value, this.charEncoding, this.connection.getServerCharacterEncoding(), this.charConverter, this.connection.parserKnowsUnicode(), this.connection);
        } else {
          packet.writeLenBytes(((String)value).getBytes());
        }
        return;
      }
    }
    catch (UnsupportedEncodingException uEE)
    {
      throw SQLError.createSQLException(Messages.getString("ServerPreparedStatement.22") + this.connection.getEncoding() + "'", "S1000", getExceptionInterceptor());
    }
  }
  
  private void storeDateTime412AndOlder(Buffer intoBuf, java.util.Date dt, int bufferType)
    throws SQLException
  {
    Calendar sessionCalendar = null;
    if (!this.useLegacyDatetimeCode)
    {
      if (bufferType == 10) {
        sessionCalendar = getDefaultTzCalendar();
      } else {
        sessionCalendar = getServerTzCalendar();
      }
    }
    else {
      sessionCalendar = ((dt instanceof Timestamp)) && (this.connection.getUseJDBCCompliantTimezoneShift()) ? this.connection.getUtcCalendar() : getCalendarInstanceForSessionOrNew();
    }
    synchronized (sessionCalendar)
    {
      java.util.Date oldTime = sessionCalendar.getTime();
      try
      {
        intoBuf.ensureCapacity(8);
        intoBuf.writeByte((byte)7);
        
        sessionCalendar.setTime(dt);
        
        int year = sessionCalendar.get(1);
        int month = sessionCalendar.get(2) + 1;
        int date = sessionCalendar.get(5);
        
        intoBuf.writeInt(year);
        intoBuf.writeByte((byte)month);
        intoBuf.writeByte((byte)date);
        if ((dt instanceof java.sql.Date))
        {
          intoBuf.writeByte((byte)0);
          intoBuf.writeByte((byte)0);
          intoBuf.writeByte((byte)0);
        }
        else
        {
          intoBuf.writeByte((byte)sessionCalendar.get(11));
          
          intoBuf.writeByte((byte)sessionCalendar.get(12));
          
          intoBuf.writeByte((byte)sessionCalendar.get(13));
        }
      }
      finally
      {
        sessionCalendar.setTime(oldTime);
      }
    }
  }
  
  private void storeDateTime(Buffer intoBuf, java.util.Date dt, MysqlIO mysql, int bufferType)
    throws SQLException
  {
    if (this.connection.versionMeetsMinimum(4, 1, 3)) {
      storeDateTime413AndNewer(intoBuf, dt, bufferType);
    } else {
      storeDateTime412AndOlder(intoBuf, dt, bufferType);
    }
  }
  
  private void storeDateTime413AndNewer(Buffer intoBuf, java.util.Date dt, int bufferType)
    throws SQLException
  {
    Calendar sessionCalendar = null;
    if (!this.useLegacyDatetimeCode)
    {
      if (bufferType == 10) {
        sessionCalendar = getDefaultTzCalendar();
      } else {
        sessionCalendar = getServerTzCalendar();
      }
    }
    else {
      sessionCalendar = ((dt instanceof Timestamp)) && (this.connection.getUseJDBCCompliantTimezoneShift()) ? this.connection.getUtcCalendar() : getCalendarInstanceForSessionOrNew();
    }
    synchronized (sessionCalendar)
    {
      java.util.Date oldTime = sessionCalendar.getTime();
      try
      {
        sessionCalendar.setTime(dt);
        if ((dt instanceof java.sql.Date))
        {
          sessionCalendar.set(11, 0);
          sessionCalendar.set(12, 0);
          sessionCalendar.set(13, 0);
        }
        byte length = 7;
        if ((dt instanceof Timestamp)) {
          length = 11;
        }
        intoBuf.ensureCapacity(length);
        
        intoBuf.writeByte(length);
        
        int year = sessionCalendar.get(1);
        int month = sessionCalendar.get(2) + 1;
        int date = sessionCalendar.get(5);
        
        intoBuf.writeInt(year);
        intoBuf.writeByte((byte)month);
        intoBuf.writeByte((byte)date);
        if ((dt instanceof java.sql.Date))
        {
          intoBuf.writeByte((byte)0);
          intoBuf.writeByte((byte)0);
          intoBuf.writeByte((byte)0);
        }
        else
        {
          intoBuf.writeByte((byte)sessionCalendar.get(11));
          
          intoBuf.writeByte((byte)sessionCalendar.get(12));
          
          intoBuf.writeByte((byte)sessionCalendar.get(13));
        }
        if (length == 11) {
          intoBuf.writeLong(((Timestamp)dt).getNanos() / 1000);
        }
      }
      finally
      {
        sessionCalendar.setTime(oldTime);
      }
    }
  }
  
  private Calendar getServerTzCalendar()
  {
    synchronized (this)
    {
      if (this.serverTzCalendar == null) {
        this.serverTzCalendar = new GregorianCalendar(this.connection.getServerTimezoneTZ());
      }
      return this.serverTzCalendar;
    }
  }
  
  private Calendar getDefaultTzCalendar()
  {
    synchronized (this)
    {
      if (this.defaultTzCalendar == null) {
        this.defaultTzCalendar = new GregorianCalendar(TimeZone.getDefault());
      }
      return this.defaultTzCalendar;
    }
  }
  
  private void storeReader(MysqlIO mysql, int parameterIndex, Buffer packet, Reader inStream)
    throws SQLException
  {
    String forcedEncoding = this.connection.getClobCharacterEncoding();
    
    String clobEncoding = forcedEncoding == null ? this.connection.getEncoding() : forcedEncoding;
    
    int maxBytesChar = 2;
    if (clobEncoding != null) {
      if (!clobEncoding.equals("UTF-16"))
      {
        maxBytesChar = this.connection.getMaxBytesPerChar(clobEncoding);
        if (maxBytesChar == 1) {
          maxBytesChar = 2;
        }
      }
      else
      {
        maxBytesChar = 4;
      }
    }
    char[] buf = new char[8192 / maxBytesChar];
    
    int numRead = 0;
    
    int bytesInPacket = 0;
    int totalBytesRead = 0;
    int bytesReadAtLastSend = 0;
    int packetIsFullAt = this.connection.getBlobSendChunkSize();
    try
    {
      packet.clear();
      packet.writeByte((byte)24);
      packet.writeLong(this.serverStatementId);
      packet.writeInt(parameterIndex);
      
      boolean readAny = false;
      while ((numRead = inStream.read(buf)) != -1)
      {
        readAny = true;
        
        byte[] valueAsBytes = StringUtils.getBytes(buf, null, clobEncoding, this.connection.getServerCharacterEncoding(), 0, numRead, this.connection.parserKnowsUnicode(), getExceptionInterceptor());
        
        packet.writeBytesNoNull(valueAsBytes, 0, valueAsBytes.length);
        
        bytesInPacket += valueAsBytes.length;
        totalBytesRead += valueAsBytes.length;
        if (bytesInPacket >= packetIsFullAt)
        {
          bytesReadAtLastSend = totalBytesRead;
          
          mysql.sendCommand(24, null, packet, true, null, 0);
          
          bytesInPacket = 0;
          packet.clear();
          packet.writeByte((byte)24);
          packet.writeLong(this.serverStatementId);
          packet.writeInt(parameterIndex);
        }
      }
      if (totalBytesRead != bytesReadAtLastSend) {
        mysql.sendCommand(24, null, packet, true, null, 0);
      }
      if (!readAny) {
        mysql.sendCommand(24, null, packet, true, null, 0);
      }
    }
    catch (IOException ioEx)
    {
      SQLException sqlEx = SQLError.createSQLException(Messages.getString("ServerPreparedStatement.24") + ioEx.toString(), "S1000", getExceptionInterceptor());
      
      sqlEx.initCause(ioEx);
      
      throw sqlEx;
    }
    finally
    {
      if ((this.connection.getAutoClosePStmtStreams()) && 
        (inStream != null)) {
        try
        {
          inStream.close();
        }
        catch (IOException ioEx) {}
      }
    }
  }
  
  private void storeStream(MysqlIO mysql, int parameterIndex, Buffer packet, InputStream inStream)
    throws SQLException
  {
    byte[] buf = new byte[' '];
    
    int numRead = 0;
    try
    {
      int bytesInPacket = 0;
      int totalBytesRead = 0;
      int bytesReadAtLastSend = 0;
      int packetIsFullAt = this.connection.getBlobSendChunkSize();
      
      packet.clear();
      packet.writeByte((byte)24);
      packet.writeLong(this.serverStatementId);
      packet.writeInt(parameterIndex);
      
      boolean readAny = false;
      while ((numRead = inStream.read(buf)) != -1)
      {
        readAny = true;
        
        packet.writeBytesNoNull(buf, 0, numRead);
        bytesInPacket += numRead;
        totalBytesRead += numRead;
        if (bytesInPacket >= packetIsFullAt)
        {
          bytesReadAtLastSend = totalBytesRead;
          
          mysql.sendCommand(24, null, packet, true, null, 0);
          
          bytesInPacket = 0;
          packet.clear();
          packet.writeByte((byte)24);
          packet.writeLong(this.serverStatementId);
          packet.writeInt(parameterIndex);
        }
      }
      if (totalBytesRead != bytesReadAtLastSend) {
        mysql.sendCommand(24, null, packet, true, null, 0);
      }
      if (!readAny) {
        mysql.sendCommand(24, null, packet, true, null, 0);
      }
    }
    catch (IOException ioEx)
    {
      SQLException sqlEx = SQLError.createSQLException(Messages.getString("ServerPreparedStatement.25") + ioEx.toString(), "S1000", getExceptionInterceptor());
      
      sqlEx.initCause(ioEx);
      
      throw sqlEx;
    }
    finally
    {
      if ((this.connection.getAutoClosePStmtStreams()) && 
        (inStream != null)) {
        try
        {
          inStream.close();
        }
        catch (IOException ioEx) {}
      }
    }
  }
  
  public String toString()
  {
    StringBuffer toStringBuf = new StringBuffer();
    
    toStringBuf.append("com.mysql.jdbc.ServerPreparedStatement[");
    toStringBuf.append(this.serverStatementId);
    toStringBuf.append("] - ");
    try
    {
      toStringBuf.append(asSql());
    }
    catch (SQLException sqlEx)
    {
      toStringBuf.append(Messages.getString("ServerPreparedStatement.6"));
      toStringBuf.append(sqlEx);
    }
    return toStringBuf.toString();
  }
  
  protected long getServerStatementId()
  {
    return this.serverStatementId;
  }
  
  private boolean hasCheckedRewrite = false;
  private boolean canRewrite = false;
  
  public synchronized boolean canRewriteAsMultiValueInsertAtSqlLevel()
    throws SQLException
  {
    if (!this.hasCheckedRewrite)
    {
      this.hasCheckedRewrite = true;
      this.canRewrite = canRewrite(this.originalSql, isOnDuplicateKeyUpdate(), getLocationOfOnDuplicateKeyUpdate(), 0);
      
      this.parseInfo = new PreparedStatement.ParseInfo(this, this.originalSql, this.connection, this.connection.getMetaData(), this.charEncoding, this.charConverter);
    }
    return this.canRewrite;
  }
  
  public synchronized boolean canRewriteAsMultivalueInsertStatement()
    throws SQLException
  {
    if (!canRewriteAsMultiValueInsertAtSqlLevel()) {
      return false;
    }
    BindValue[] currentBindValues = null;
    BindValue[] previousBindValues = null;
    
    int nbrCommands = this.batchedArgs.size();
    for (int commandIndex = 0; commandIndex < nbrCommands; commandIndex++)
    {
      Object arg = this.batchedArgs.get(commandIndex);
      if (!(arg instanceof String))
      {
        currentBindValues = ((BatchedBindValues)arg).batchedParameterValues;
        if (previousBindValues != null) {
          for (int j = 0; j < this.parameterBindings.length; j++) {
            if (currentBindValues[j].bufferType != previousBindValues[j].bufferType) {
              return false;
            }
          }
        }
      }
    }
    return true;
  }
  
  private int locationOfOnDuplicateKeyUpdate = -2;
  
  protected synchronized int getLocationOfOnDuplicateKeyUpdate()
  {
    if (this.locationOfOnDuplicateKeyUpdate == -2) {
      this.locationOfOnDuplicateKeyUpdate = getOnDuplicateKeyLocation(this.originalSql);
    }
    return this.locationOfOnDuplicateKeyUpdate;
  }
  
  protected synchronized boolean isOnDuplicateKeyUpdate()
  {
    return getLocationOfOnDuplicateKeyUpdate() != -1;
  }
  
  protected long[] computeMaxParameterSetSizeAndBatchSize(int numBatchedArgs)
  {
    long sizeOfEntireBatch = 10L;
    long maxSizeOfParameterSet = 0L;
    for (int i = 0; i < numBatchedArgs; i++)
    {
      BindValue[] paramArg = ((BatchedBindValues)this.batchedArgs.get(i)).batchedParameterValues;
      
      long sizeOfParameterSet = 0L;
      
      sizeOfParameterSet += (this.parameterCount + 7) / 8;
      
      sizeOfParameterSet += this.parameterCount * 2;
      for (int j = 0; j < this.parameterBindings.length; j++) {
        if (!paramArg[j].isNull)
        {
          long size = paramArg[j].getBoundLength();
          if (paramArg[j].isLongData)
          {
            if (size != -1L) {
              sizeOfParameterSet += size;
            }
          }
          else {
            sizeOfParameterSet += size;
          }
        }
      }
      sizeOfEntireBatch += sizeOfParameterSet;
      if (sizeOfParameterSet > maxSizeOfParameterSet) {
        maxSizeOfParameterSet = sizeOfParameterSet;
      }
    }
    return new long[] { maxSizeOfParameterSet, sizeOfEntireBatch };
  }
  
  protected int setOneBatchedParameterSet(java.sql.PreparedStatement batchedStatement, int batchedParamIndex, Object paramSet)
    throws SQLException
  {
    BindValue[] paramArg = ((BatchedBindValues)paramSet).batchedParameterValues;
    for (int j = 0; j < paramArg.length; j++) {
      if (paramArg[j].isNull)
      {
        batchedStatement.setNull(batchedParamIndex++, 0);
      }
      else if (paramArg[j].isLongData)
      {
        Object value = paramArg[j].value;
        if ((value instanceof InputStream)) {
          batchedStatement.setBinaryStream(batchedParamIndex++, (InputStream)value, (int)paramArg[j].bindLength);
        } else {
          batchedStatement.setCharacterStream(batchedParamIndex++, (Reader)value, (int)paramArg[j].bindLength);
        }
      }
      else
      {
        switch (paramArg[j].bufferType)
        {
        case 1: 
          batchedStatement.setByte(batchedParamIndex++, paramArg[j].byteBinding);
          
          break;
        case 2: 
          batchedStatement.setShort(batchedParamIndex++, paramArg[j].shortBinding);
          
          break;
        case 3: 
          batchedStatement.setInt(batchedParamIndex++, paramArg[j].intBinding);
          
          break;
        case 8: 
          batchedStatement.setLong(batchedParamIndex++, paramArg[j].longBinding);
          
          break;
        case 4: 
          batchedStatement.setFloat(batchedParamIndex++, paramArg[j].floatBinding);
          
          break;
        case 5: 
          batchedStatement.setDouble(batchedParamIndex++, paramArg[j].doubleBinding);
          
          break;
        case 11: 
          batchedStatement.setTime(batchedParamIndex++, (Time)paramArg[j].value);
          
          break;
        case 10: 
          batchedStatement.setDate(batchedParamIndex++, (java.sql.Date)paramArg[j].value);
          
          break;
        case 7: 
        case 12: 
          batchedStatement.setTimestamp(batchedParamIndex++, (Timestamp)paramArg[j].value);
          
          break;
        case 0: 
        case 15: 
        case 246: 
        case 253: 
        case 254: 
          Object value = paramArg[j].value;
          if ((value instanceof byte[])) {
            batchedStatement.setBytes(batchedParamIndex, (byte[])value);
          } else {
            batchedStatement.setString(batchedParamIndex, (String)value);
          }
          if ((batchedStatement instanceof ServerPreparedStatement))
          {
            BindValue asBound = ((ServerPreparedStatement)batchedStatement).getBinding(batchedParamIndex, false);
            
            asBound.bufferType = paramArg[j].bufferType;
          }
          batchedParamIndex++;
          
          break;
        default: 
          throw new IllegalArgumentException("Unknown type when re-binding parameter into batched statement for parameter index " + batchedParamIndex);
        }
      }
    }
    return batchedParamIndex;
  }
  
  protected boolean containsOnDuplicateKeyUpdateInSQL()
  {
    return this.hasOnDuplicateKeyUpdate;
  }
  
  protected PreparedStatement prepareBatchedInsertSQL(ConnectionImpl localConn, int numBatches)
    throws SQLException
  {
    try
    {
      PreparedStatement pstmt = new ServerPreparedStatement(localConn, this.parseInfo.getSqlForBatch(numBatches), this.currentCatalog, this.resultSetConcurrency, this.resultSetType);
      pstmt.setRetrieveGeneratedKeys(this.retrieveGeneratedKeys);
      
      return pstmt;
    }
    catch (UnsupportedEncodingException e)
    {
      SQLException sqlEx = SQLError.createSQLException("Unable to prepare batch statement", "S1000", getExceptionInterceptor());
      sqlEx.initCause(e);
      
      throw sqlEx;
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\mysql\jdbc\ServerPreparedStatement.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */