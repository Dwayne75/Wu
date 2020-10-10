package com.mysql.jdbc;

import com.mysql.jdbc.exceptions.MySQLStatementCancelledException;
import com.mysql.jdbc.exceptions.MySQLTimeoutException;
import com.mysql.jdbc.profiler.ProfilerEvent;
import com.mysql.jdbc.profiler.ProfilerEventHandler;
import com.mysql.jdbc.profiler.ProfilerEventHandlerFactory;
import java.io.InputStream;
import java.math.BigInteger;
import java.sql.BatchUpdateException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

public class StatementImpl
  implements Statement
{
  protected static final String PING_MARKER = "/* ping */";
  
  class CancelTask
    extends TimerTask
  {
    long connectionId = 0L;
    SQLException caughtWhileCancelling = null;
    StatementImpl toCancel;
    
    CancelTask(StatementImpl cancellee)
      throws SQLException
    {
      this.connectionId = StatementImpl.this.connection.getIO().getThreadId();
      this.toCancel = cancellee;
    }
    
    public void run()
    {
      Thread cancelThread = new StatementImpl.1(this);
      
      cancelThread.start();
    }
  }
  
  protected Object cancelTimeoutMutex = new Object();
  protected static int statementCounter = 1;
  public static final byte USES_VARIABLES_FALSE = 0;
  public static final byte USES_VARIABLES_TRUE = 1;
  public static final byte USES_VARIABLES_UNKNOWN = -1;
  protected boolean wasCancelled = false;
  protected boolean wasCancelledByTimeout = false;
  protected List batchedArgs;
  protected SingleByteCharsetConverter charConverter = null;
  protected String charEncoding = null;
  protected ConnectionImpl connection = null;
  protected long connectionId = 0L;
  protected String currentCatalog = null;
  protected boolean doEscapeProcessing = true;
  protected ProfilerEventHandler eventSink = null;
  private int fetchSize = 0;
  protected boolean isClosed = false;
  protected long lastInsertId = -1L;
  protected int maxFieldSize = MysqlIO.getMaxBuf();
  protected int maxRows = -1;
  protected boolean maxRowsChanged = false;
  protected Set openResults = new HashSet();
  protected boolean pedantic = false;
  protected Throwable pointOfOrigin;
  protected boolean profileSQL = false;
  protected ResultSetInternalMethods results = null;
  protected int resultSetConcurrency = 0;
  protected int resultSetType = 0;
  protected int statementId;
  protected int timeoutInMillis = 0;
  protected long updateCount = -1L;
  protected boolean useUsageAdvisor = false;
  protected SQLWarning warningChain = null;
  protected boolean holdResultsOpenOverClose = false;
  protected ArrayList batchedGeneratedKeys = null;
  protected boolean retrieveGeneratedKeys = false;
  protected boolean continueBatchOnError = false;
  protected PingTarget pingTarget = null;
  protected boolean useLegacyDatetimeCode;
  private ExceptionInterceptor exceptionInterceptor;
  protected boolean lastQueryIsOnDupKeyUpdate = false;
  
  public StatementImpl(ConnectionImpl c, String catalog)
    throws SQLException
  {
    if ((c == null) || (c.isClosed())) {
      throw SQLError.createSQLException(Messages.getString("Statement.0"), "08003", null);
    }
    this.connection = c;
    this.connectionId = this.connection.getId();
    this.exceptionInterceptor = c.getExceptionInterceptor();
    
    this.currentCatalog = catalog;
    this.pedantic = this.connection.getPedantic();
    this.continueBatchOnError = this.connection.getContinueBatchOnError();
    this.useLegacyDatetimeCode = this.connection.getUseLegacyDatetimeCode();
    if (!this.connection.getDontTrackOpenResources()) {
      this.connection.registerStatement(this);
    }
    if (this.connection != null)
    {
      this.maxFieldSize = this.connection.getMaxAllowedPacket();
      
      int defaultFetchSize = this.connection.getDefaultFetchSize();
      if (defaultFetchSize != 0) {
        setFetchSize(defaultFetchSize);
      }
    }
    if (this.connection.getUseUnicode())
    {
      this.charEncoding = this.connection.getEncoding();
      
      this.charConverter = this.connection.getCharsetConverter(this.charEncoding);
    }
    boolean profiling = (this.connection.getProfileSql()) || (this.connection.getUseUsageAdvisor()) || (this.connection.getLogSlowQueries());
    if ((this.connection.getAutoGenerateTestcaseScript()) || (profiling)) {
      this.statementId = (statementCounter++);
    }
    if (profiling)
    {
      this.pointOfOrigin = new Throwable();
      this.profileSQL = this.connection.getProfileSql();
      this.useUsageAdvisor = this.connection.getUseUsageAdvisor();
      this.eventSink = ProfilerEventHandlerFactory.getInstance(this.connection);
    }
    int maxRowsConn = this.connection.getMaxRows();
    if (maxRowsConn != -1) {
      setMaxRows(maxRowsConn);
    }
    this.holdResultsOpenOverClose = this.connection.getHoldResultsOpenOverStatementClose();
  }
  
  public synchronized void addBatch(String sql)
    throws SQLException
  {
    if (this.batchedArgs == null) {
      this.batchedArgs = new ArrayList();
    }
    if (sql != null) {
      this.batchedArgs.add(sql);
    }
  }
  
  public void cancel()
    throws SQLException
  {
    if ((!this.isClosed) && (this.connection != null) && (this.connection.versionMeetsMinimum(5, 0, 0)))
    {
      Connection cancelConn = null;
      java.sql.Statement cancelStmt = null;
      try
      {
        cancelConn = this.connection.duplicate();
        cancelStmt = cancelConn.createStatement();
        cancelStmt.execute("KILL QUERY " + this.connection.getIO().getThreadId());
        
        this.wasCancelled = true;
      }
      finally
      {
        if (cancelStmt != null) {
          cancelStmt.close();
        }
        if (cancelConn != null) {
          cancelConn.close();
        }
      }
    }
  }
  
  protected void checkClosed()
    throws SQLException
  {
    if (this.isClosed) {
      throw SQLError.createSQLException(Messages.getString("Statement.49"), "08003", getExceptionInterceptor());
    }
  }
  
  protected void checkForDml(String sql, char firstStatementChar)
    throws SQLException
  {
    if ((firstStatementChar == 'I') || (firstStatementChar == 'U') || (firstStatementChar == 'D') || (firstStatementChar == 'A') || (firstStatementChar == 'C'))
    {
      String noCommentSql = StringUtils.stripComments(sql, "'\"", "'\"", true, false, true, true);
      if ((StringUtils.startsWithIgnoreCaseAndWs(noCommentSql, "INSERT")) || (StringUtils.startsWithIgnoreCaseAndWs(noCommentSql, "UPDATE")) || (StringUtils.startsWithIgnoreCaseAndWs(noCommentSql, "DELETE")) || (StringUtils.startsWithIgnoreCaseAndWs(noCommentSql, "DROP")) || (StringUtils.startsWithIgnoreCaseAndWs(noCommentSql, "CREATE")) || (StringUtils.startsWithIgnoreCaseAndWs(noCommentSql, "ALTER"))) {
        throw SQLError.createSQLException(Messages.getString("Statement.57"), "S1009", getExceptionInterceptor());
      }
    }
  }
  
  protected void checkNullOrEmptyQuery(String sql)
    throws SQLException
  {
    if (sql == null) {
      throw SQLError.createSQLException(Messages.getString("Statement.59"), "S1009", getExceptionInterceptor());
    }
    if (sql.length() == 0) {
      throw SQLError.createSQLException(Messages.getString("Statement.61"), "S1009", getExceptionInterceptor());
    }
  }
  
  public synchronized void clearBatch()
    throws SQLException
  {
    if (this.batchedArgs != null) {
      this.batchedArgs.clear();
    }
  }
  
  public void clearWarnings()
    throws SQLException
  {
    this.warningChain = null;
  }
  
  public synchronized void close()
    throws SQLException
  {
    realClose(true, true);
  }
  
  protected synchronized void closeAllOpenResults()
  {
    if (this.openResults != null)
    {
      for (Iterator iter = this.openResults.iterator(); iter.hasNext();)
      {
        ResultSetInternalMethods element = (ResultSetInternalMethods)iter.next();
        try
        {
          element.realClose(false);
        }
        catch (SQLException sqlEx)
        {
          AssertionFailedException.shouldNotHappen(sqlEx);
        }
      }
      this.openResults.clear();
    }
  }
  
  public synchronized void removeOpenResultSet(ResultSet rs)
  {
    if (this.openResults != null) {
      this.openResults.remove(rs);
    }
  }
  
  public synchronized int getOpenResultSetCount()
  {
    if (this.openResults != null) {
      return this.openResults.size();
    }
    return 0;
  }
  
  private ResultSetInternalMethods createResultSetUsingServerFetch(String sql)
    throws SQLException
  {
    java.sql.PreparedStatement pStmt = this.connection.prepareStatement(sql, this.resultSetType, this.resultSetConcurrency);
    
    pStmt.setFetchSize(this.fetchSize);
    if (this.maxRows > -1) {
      pStmt.setMaxRows(this.maxRows);
    }
    pStmt.execute();
    
    ResultSetInternalMethods rs = ((StatementImpl)pStmt).getResultSetInternal();
    
    rs.setStatementUsedForFetchingRows((PreparedStatement)pStmt);
    
    this.results = rs;
    
    return rs;
  }
  
  protected boolean createStreamingResultSet()
  {
    return (this.resultSetType == 1003) && (this.resultSetConcurrency == 1007) && (this.fetchSize == Integer.MIN_VALUE);
  }
  
  private int originalResultSetType = 0;
  private int originalFetchSize = 0;
  
  public void enableStreamingResults()
    throws SQLException
  {
    this.originalResultSetType = this.resultSetType;
    this.originalFetchSize = this.fetchSize;
    
    setFetchSize(Integer.MIN_VALUE);
    setResultSetType(1003);
  }
  
  public void disableStreamingResults()
    throws SQLException
  {
    if ((this.fetchSize == Integer.MIN_VALUE) && (this.resultSetType == 1003))
    {
      setFetchSize(this.originalFetchSize);
      setResultSetType(this.originalResultSetType);
    }
  }
  
  public boolean execute(String sql)
    throws SQLException
  {
    return execute(sql, false);
  }
  
  private boolean execute(String sql, boolean returnGeneratedKeys)
    throws SQLException
  {
    checkClosed();
    
    ConnectionImpl locallyScopedConn = this.connection;
    synchronized (locallyScopedConn.getMutex())
    {
      this.retrieveGeneratedKeys = returnGeneratedKeys;
      this.lastQueryIsOnDupKeyUpdate = false;
      if (returnGeneratedKeys) {
        this.lastQueryIsOnDupKeyUpdate = containsOnDuplicateKeyInString(sql);
      }
      resetCancelledState();
      
      checkNullOrEmptyQuery(sql);
      
      checkClosed();
      
      char firstNonWsChar = StringUtils.firstAlphaCharUc(sql, findStartOfStatement(sql));
      
      boolean isSelect = true;
      if (firstNonWsChar != 'S')
      {
        isSelect = false;
        if (locallyScopedConn.isReadOnly()) {
          throw SQLError.createSQLException(Messages.getString("Statement.27") + Messages.getString("Statement.28"), "S1009", getExceptionInterceptor());
        }
      }
      boolean doStreaming = createStreamingResultSet();
      if ((doStreaming) && (this.connection.getNetTimeoutForStreamingResults() > 0)) {
        executeSimpleNonQuery(locallyScopedConn, "SET net_write_timeout=" + this.connection.getNetTimeoutForStreamingResults());
      }
      if (this.doEscapeProcessing)
      {
        Object escapedSqlResult = EscapeProcessor.escapeSQL(sql, locallyScopedConn.serverSupportsConvertFn(), locallyScopedConn);
        if ((escapedSqlResult instanceof String)) {
          sql = (String)escapedSqlResult;
        } else {
          sql = ((EscapeProcessorResult)escapedSqlResult).escapedSql;
        }
      }
      if ((this.results != null) && 
        (!locallyScopedConn.getHoldResultsOpenOverStatementClose())) {
        this.results.realClose(false);
      }
      if ((sql.charAt(0) == '/') && 
        (sql.startsWith("/* ping */")))
      {
        doPingInstead();
        
        return true;
      }
      CachedResultSetMetaData cachedMetaData = null;
      
      ResultSetInternalMethods rs = null;
      
      this.batchedGeneratedKeys = null;
      if (useServerFetch())
      {
        rs = createResultSetUsingServerFetch(sql);
      }
      else
      {
        CancelTask timeoutTask = null;
        
        String oldCatalog = null;
        try
        {
          if ((locallyScopedConn.getEnableQueryTimeouts()) && (this.timeoutInMillis != 0) && (locallyScopedConn.versionMeetsMinimum(5, 0, 0)))
          {
            timeoutTask = new CancelTask(this);
            ConnectionImpl.getCancelTimer().schedule(timeoutTask, this.timeoutInMillis);
          }
          if (!locallyScopedConn.getCatalog().equals(this.currentCatalog))
          {
            oldCatalog = locallyScopedConn.getCatalog();
            locallyScopedConn.setCatalog(this.currentCatalog);
          }
          Field[] cachedFields = null;
          if (locallyScopedConn.getCacheResultSetMetadata())
          {
            cachedMetaData = locallyScopedConn.getCachedMetaData(sql);
            if (cachedMetaData != null) {
              cachedFields = cachedMetaData.fields;
            }
          }
          if (locallyScopedConn.useMaxRows())
          {
            int rowLimit = -1;
            if (isSelect)
            {
              if (StringUtils.indexOfIgnoreCase(sql, "LIMIT") != -1) {
                rowLimit = this.maxRows;
              } else if (this.maxRows <= 0) {
                executeSimpleNonQuery(locallyScopedConn, "SET OPTION SQL_SELECT_LIMIT=DEFAULT");
              } else {
                executeSimpleNonQuery(locallyScopedConn, "SET OPTION SQL_SELECT_LIMIT=" + this.maxRows);
              }
            }
            else {
              executeSimpleNonQuery(locallyScopedConn, "SET OPTION SQL_SELECT_LIMIT=DEFAULT");
            }
            rs = locallyScopedConn.execSQL(this, sql, rowLimit, null, this.resultSetType, this.resultSetConcurrency, doStreaming, this.currentCatalog, cachedFields);
          }
          else
          {
            rs = locallyScopedConn.execSQL(this, sql, -1, null, this.resultSetType, this.resultSetConcurrency, doStreaming, this.currentCatalog, cachedFields);
          }
          if (timeoutTask != null)
          {
            if (timeoutTask.caughtWhileCancelling != null) {
              throw timeoutTask.caughtWhileCancelling;
            }
            timeoutTask.cancel();
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
        }
        finally
        {
          if (timeoutTask != null) {
            timeoutTask.cancel();
          }
          if (oldCatalog != null) {
            locallyScopedConn.setCatalog(oldCatalog);
          }
        }
      }
      if (rs != null)
      {
        this.lastInsertId = rs.getUpdateID();
        
        this.results = rs;
        
        rs.setFirstCharOfQuery(firstNonWsChar);
        if (rs.reallyResult()) {
          if (cachedMetaData != null) {
            locallyScopedConn.initializeResultsMetadataFromCache(sql, cachedMetaData, this.results);
          } else if (this.connection.getCacheResultSetMetadata()) {
            locallyScopedConn.initializeResultsMetadataFromCache(sql, null, this.results);
          }
        }
      }
      return (rs != null) && (rs.reallyResult());
    }
  }
  
  protected synchronized void resetCancelledState()
  {
    if (this.cancelTimeoutMutex == null) {
      return;
    }
    synchronized (this.cancelTimeoutMutex)
    {
      this.wasCancelled = false;
      this.wasCancelledByTimeout = false;
    }
  }
  
  public boolean execute(String sql, int returnGeneratedKeys)
    throws SQLException
  {
    if (returnGeneratedKeys == 1)
    {
      checkClosed();
      
      ConnectionImpl locallyScopedConn = this.connection;
      synchronized (locallyScopedConn.getMutex())
      {
        boolean readInfoMsgState = this.connection.isReadInfoMsgEnabled();
        
        locallyScopedConn.setReadInfoMsgEnabled(true);
        try
        {
          boolean bool1 = execute(sql, true);
          
          locallyScopedConn.setReadInfoMsgEnabled(readInfoMsgState);return bool1;
        }
        finally
        {
          locallyScopedConn.setReadInfoMsgEnabled(readInfoMsgState);
        }
      }
    }
    return execute(sql);
  }
  
  public boolean execute(String sql, int[] generatedKeyIndices)
    throws SQLException
  {
    if ((generatedKeyIndices != null) && (generatedKeyIndices.length > 0))
    {
      checkClosed();
      
      ConnectionImpl locallyScopedConn = this.connection;
      synchronized (locallyScopedConn.getMutex())
      {
        this.retrieveGeneratedKeys = true;
        
        boolean readInfoMsgState = locallyScopedConn.isReadInfoMsgEnabled();
        
        locallyScopedConn.setReadInfoMsgEnabled(true);
        try
        {
          boolean bool1 = execute(sql, true);
          
          locallyScopedConn.setReadInfoMsgEnabled(readInfoMsgState);return bool1;
        }
        finally
        {
          locallyScopedConn.setReadInfoMsgEnabled(readInfoMsgState);
        }
      }
    }
    return execute(sql);
  }
  
  public boolean execute(String sql, String[] generatedKeyNames)
    throws SQLException
  {
    if ((generatedKeyNames != null) && (generatedKeyNames.length > 0))
    {
      checkClosed();
      
      ConnectionImpl locallyScopedConn = this.connection;
      synchronized (locallyScopedConn.getMutex())
      {
        this.retrieveGeneratedKeys = true;
        
        boolean readInfoMsgState = this.connection.isReadInfoMsgEnabled();
        
        locallyScopedConn.setReadInfoMsgEnabled(true);
        try
        {
          boolean bool1 = execute(sql, true);
          
          locallyScopedConn.setReadInfoMsgEnabled(readInfoMsgState);return bool1;
        }
        finally
        {
          locallyScopedConn.setReadInfoMsgEnabled(readInfoMsgState);
        }
      }
    }
    return execute(sql);
  }
  
  public synchronized int[] executeBatch()
    throws SQLException
  {
    checkClosed();
    
    ConnectionImpl locallyScopedConn = this.connection;
    if (locallyScopedConn.isReadOnly()) {
      throw SQLError.createSQLException(Messages.getString("Statement.34") + Messages.getString("Statement.35"), "S1009", getExceptionInterceptor());
    }
    if ((this.results != null) && 
      (!locallyScopedConn.getHoldResultsOpenOverStatementClose())) {
      this.results.realClose(false);
    }
    synchronized (locallyScopedConn.getMutex())
    {
      if ((this.batchedArgs == null) || (this.batchedArgs.size() == 0)) {
        return new int[0];
      }
      int individualStatementTimeout = this.timeoutInMillis;
      this.timeoutInMillis = 0;
      
      CancelTask timeoutTask = null;
      try
      {
        resetCancelledState();
        
        this.retrieveGeneratedKeys = true;
        
        int[] updateCounts = null;
        if (this.batchedArgs != null)
        {
          nbrCommands = this.batchedArgs.size();
          
          this.batchedGeneratedKeys = new ArrayList(this.batchedArgs.size());
          
          boolean multiQueriesEnabled = locallyScopedConn.getAllowMultiQueries();
          if ((locallyScopedConn.versionMeetsMinimum(4, 1, 1)) && ((multiQueriesEnabled) || ((locallyScopedConn.getRewriteBatchedStatements()) && (nbrCommands > 4))))
          {
            int[] arrayOfInt1 = executeBatchUsingMultiQueries(multiQueriesEnabled, nbrCommands, individualStatementTimeout);jsr 366;return arrayOfInt1;
          }
          if ((locallyScopedConn.getEnableQueryTimeouts()) && (individualStatementTimeout != 0) && (locallyScopedConn.versionMeetsMinimum(5, 0, 0)))
          {
            timeoutTask = new CancelTask(this);
            ConnectionImpl.getCancelTimer().schedule(timeoutTask, individualStatementTimeout);
          }
          updateCounts = new int[nbrCommands];
          for (int i = 0; i < nbrCommands; i++) {
            updateCounts[i] = -3;
          }
          SQLException sqlEx = null;
          
          int commandIndex = 0;
          for (commandIndex = 0; commandIndex < nbrCommands; commandIndex++) {
            try
            {
              String sql = (String)this.batchedArgs.get(commandIndex);
              updateCounts[commandIndex] = executeUpdate(sql, true, true);
              
              getBatchedGeneratedKeys(containsOnDuplicateKeyInString(sql) ? 1 : 0);
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
                if (hasDeadlockOrTimeoutRolledBackTx(ex)) {
                  for (int i = 0; i < newUpdateCounts.length; i++) {
                    newUpdateCounts[i] = -3;
                  }
                } else {
                  System.arraycopy(updateCounts, 0, newUpdateCounts, 0, commandIndex);
                }
                throw new BatchUpdateException(ex.getMessage(), ex.getSQLState(), ex.getErrorCode(), newUpdateCounts);
              }
            }
          }
          if (sqlEx != null) {
            throw new BatchUpdateException(sqlEx.getMessage(), sqlEx.getSQLState(), sqlEx.getErrorCode(), updateCounts);
          }
        }
        if (timeoutTask != null)
        {
          if (timeoutTask.caughtWhileCancelling != null) {
            throw timeoutTask.caughtWhileCancelling;
          }
          timeoutTask.cancel();
          timeoutTask = null;
        }
        int nbrCommands = updateCounts != null ? updateCounts : new int[0];jsr 16;return nbrCommands;
      }
      finally
      {
        jsr 6;
      }
      localObject2 = returnAddress;
      if (timeoutTask != null) {
        timeoutTask.cancel();
      }
      resetCancelledState();
      
      this.timeoutInMillis = individualStatementTimeout;
      
      clearBatch();ret;
    }
  }
  
  protected final boolean hasDeadlockOrTimeoutRolledBackTx(SQLException ex)
  {
    int vendorCode = ex.getErrorCode();
    switch (vendorCode)
    {
    case 1206: 
    case 1213: 
      return true;
    case 1205: 
      try
      {
        return !this.connection.versionMeetsMinimum(5, 0, 13);
      }
      catch (SQLException sqlEx)
      {
        return false;
      }
    }
    return false;
  }
  
  private int[] executeBatchUsingMultiQueries(boolean multiQueriesEnabled, int nbrCommands, int individualStatementTimeout)
    throws SQLException
  {
    ConnectionImpl locallyScopedConn = this.connection;
    if (!multiQueriesEnabled) {
      locallyScopedConn.getIO().enableMultiQueries();
    }
    java.sql.Statement batchStmt = null;
    
    CancelTask timeoutTask = null;
    try
    {
      int[] updateCounts = new int[nbrCommands];
      for (int i = 0; i < nbrCommands; i++) {
        updateCounts[i] = -3;
      }
      int commandIndex = 0;
      
      StringBuffer queryBuf = new StringBuffer();
      
      batchStmt = locallyScopedConn.createStatement();
      if ((locallyScopedConn.getEnableQueryTimeouts()) && (individualStatementTimeout != 0) && (locallyScopedConn.versionMeetsMinimum(5, 0, 0)))
      {
        timeoutTask = new CancelTask((StatementImpl)batchStmt);
        ConnectionImpl.getCancelTimer().schedule(timeoutTask, individualStatementTimeout);
      }
      int counter = 0;
      
      int numberOfBytesPerChar = 1;
      
      String connectionEncoding = locallyScopedConn.getEncoding();
      if (StringUtils.startsWithIgnoreCase(connectionEncoding, "utf")) {
        numberOfBytesPerChar = 3;
      } else if (CharsetMapping.isMultibyteCharset(connectionEncoding)) {
        numberOfBytesPerChar = 2;
      }
      int escapeAdjust = 1;
      if (this.doEscapeProcessing) {
        escapeAdjust = 2;
      }
      SQLException sqlEx = null;
      
      int argumentSetsInBatchSoFar = 0;
      for (commandIndex = 0; commandIndex < nbrCommands; commandIndex++)
      {
        String nextQuery = (String)this.batchedArgs.get(commandIndex);
        if (((queryBuf.length() + nextQuery.length()) * numberOfBytesPerChar + 1 + 4) * escapeAdjust + 32 > this.connection.getMaxAllowedPacket())
        {
          try
          {
            batchStmt.execute(queryBuf.toString(), 1);
          }
          catch (SQLException ex)
          {
            sqlEx = handleExceptionForBatch(commandIndex, argumentSetsInBatchSoFar, updateCounts, ex);
          }
          counter = processMultiCountsAndKeys((StatementImpl)batchStmt, counter, updateCounts);
          
          queryBuf = new StringBuffer();
          argumentSetsInBatchSoFar = 0;
        }
        queryBuf.append(nextQuery);
        queryBuf.append(";");
        argumentSetsInBatchSoFar++;
      }
      if (queryBuf.length() > 0)
      {
        try
        {
          batchStmt.execute(queryBuf.toString(), 1);
        }
        catch (SQLException ex)
        {
          sqlEx = handleExceptionForBatch(commandIndex - 1, argumentSetsInBatchSoFar, updateCounts, ex);
        }
        counter = processMultiCountsAndKeys((StatementImpl)batchStmt, counter, updateCounts);
      }
      if (timeoutTask != null)
      {
        if (timeoutTask.caughtWhileCancelling != null) {
          throw timeoutTask.caughtWhileCancelling;
        }
        timeoutTask.cancel();
        timeoutTask = null;
      }
      if (sqlEx != null) {
        throw new BatchUpdateException(sqlEx.getMessage(), sqlEx.getSQLState(), sqlEx.getErrorCode(), updateCounts);
      }
      return updateCounts != null ? updateCounts : new int[0];
    }
    finally
    {
      if (timeoutTask != null) {
        timeoutTask.cancel();
      }
      resetCancelledState();
      try
      {
        if (batchStmt != null) {
          batchStmt.close();
        }
      }
      finally
      {
        if (!multiQueriesEnabled) {
          locallyScopedConn.getIO().disableMultiQueries();
        }
      }
    }
  }
  
  protected int processMultiCountsAndKeys(StatementImpl batchedStatement, int updateCountCounter, int[] updateCounts)
    throws SQLException
  {
    updateCounts[(updateCountCounter++)] = batchedStatement.getUpdateCount();
    
    boolean doGenKeys = this.batchedGeneratedKeys != null;
    
    byte[][] row = (byte[][])null;
    if (doGenKeys)
    {
      long generatedKey = batchedStatement.getLastInsertID();
      
      row = new byte[1][];
      row[0] = Long.toString(generatedKey).getBytes();
      this.batchedGeneratedKeys.add(new ByteArrayRow(row, getExceptionInterceptor()));
    }
    while ((batchedStatement.getMoreResults()) || (batchedStatement.getUpdateCount() != -1))
    {
      updateCounts[(updateCountCounter++)] = batchedStatement.getUpdateCount();
      if (doGenKeys)
      {
        long generatedKey = batchedStatement.getLastInsertID();
        
        row = new byte[1][];
        row[0] = Long.toString(generatedKey).getBytes();
        this.batchedGeneratedKeys.add(new ByteArrayRow(row, getExceptionInterceptor()));
      }
    }
    return updateCountCounter;
  }
  
  protected SQLException handleExceptionForBatch(int endOfBatchIndex, int numValuesPerBatch, int[] updateCounts, SQLException ex)
    throws BatchUpdateException
  {
    for (int j = endOfBatchIndex; j > endOfBatchIndex - numValuesPerBatch; j--) {
      updateCounts[j] = -3;
    }
    SQLException sqlEx;
    if ((this.continueBatchOnError) && (!(ex instanceof MySQLTimeoutException)) && (!(ex instanceof MySQLStatementCancelledException)) && (!hasDeadlockOrTimeoutRolledBackTx(ex)))
    {
      sqlEx = ex;
    }
    else
    {
      int[] newUpdateCounts = new int[endOfBatchIndex];
      System.arraycopy(updateCounts, 0, newUpdateCounts, 0, endOfBatchIndex);
      
      throw new BatchUpdateException(ex.getMessage(), ex.getSQLState(), ex.getErrorCode(), newUpdateCounts);
    }
    SQLException sqlEx;
    return sqlEx;
  }
  
  public ResultSet executeQuery(String sql)
    throws SQLException
  {
    checkClosed();
    
    ConnectionImpl locallyScopedConn = this.connection;
    synchronized (locallyScopedConn.getMutex())
    {
      this.retrieveGeneratedKeys = false;
      
      resetCancelledState();
      
      checkNullOrEmptyQuery(sql);
      
      boolean doStreaming = createStreamingResultSet();
      if ((doStreaming) && (this.connection.getNetTimeoutForStreamingResults() > 0)) {
        executeSimpleNonQuery(locallyScopedConn, "SET net_write_timeout=" + this.connection.getNetTimeoutForStreamingResults());
      }
      if (this.doEscapeProcessing)
      {
        Object escapedSqlResult = EscapeProcessor.escapeSQL(sql, locallyScopedConn.serverSupportsConvertFn(), this.connection);
        if ((escapedSqlResult instanceof String)) {
          sql = (String)escapedSqlResult;
        } else {
          sql = ((EscapeProcessorResult)escapedSqlResult).escapedSql;
        }
      }
      char firstStatementChar = StringUtils.firstNonWsCharUc(sql, findStartOfStatement(sql));
      if ((sql.charAt(0) == '/') && 
        (sql.startsWith("/* ping */")))
      {
        doPingInstead();
        
        return this.results;
      }
      checkForDml(sql, firstStatementChar);
      if ((this.results != null) && 
        (!locallyScopedConn.getHoldResultsOpenOverStatementClose())) {
        this.results.realClose(false);
      }
      CachedResultSetMetaData cachedMetaData = null;
      if (useServerFetch())
      {
        this.results = createResultSetUsingServerFetch(sql);
        
        return this.results;
      }
      CancelTask timeoutTask = null;
      
      String oldCatalog = null;
      try
      {
        if ((locallyScopedConn.getEnableQueryTimeouts()) && (this.timeoutInMillis != 0) && (locallyScopedConn.versionMeetsMinimum(5, 0, 0)))
        {
          timeoutTask = new CancelTask(this);
          ConnectionImpl.getCancelTimer().schedule(timeoutTask, this.timeoutInMillis);
        }
        if (!locallyScopedConn.getCatalog().equals(this.currentCatalog))
        {
          oldCatalog = locallyScopedConn.getCatalog();
          locallyScopedConn.setCatalog(this.currentCatalog);
        }
        Field[] cachedFields = null;
        if (locallyScopedConn.getCacheResultSetMetadata())
        {
          cachedMetaData = locallyScopedConn.getCachedMetaData(sql);
          if (cachedMetaData != null) {
            cachedFields = cachedMetaData.fields;
          }
        }
        if (locallyScopedConn.useMaxRows())
        {
          if (StringUtils.indexOfIgnoreCase(sql, "LIMIT") != -1)
          {
            this.results = locallyScopedConn.execSQL(this, sql, this.maxRows, null, this.resultSetType, this.resultSetConcurrency, doStreaming, this.currentCatalog, cachedFields);
          }
          else
          {
            if (this.maxRows <= 0) {
              executeSimpleNonQuery(locallyScopedConn, "SET OPTION SQL_SELECT_LIMIT=DEFAULT");
            } else {
              executeSimpleNonQuery(locallyScopedConn, "SET OPTION SQL_SELECT_LIMIT=" + this.maxRows);
            }
            this.results = locallyScopedConn.execSQL(this, sql, -1, null, this.resultSetType, this.resultSetConcurrency, doStreaming, this.currentCatalog, cachedFields);
            if (oldCatalog != null) {
              locallyScopedConn.setCatalog(oldCatalog);
            }
          }
        }
        else {
          this.results = locallyScopedConn.execSQL(this, sql, -1, null, this.resultSetType, this.resultSetConcurrency, doStreaming, this.currentCatalog, cachedFields);
        }
        if (timeoutTask != null)
        {
          if (timeoutTask.caughtWhileCancelling != null) {
            throw timeoutTask.caughtWhileCancelling;
          }
          timeoutTask.cancel();
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
      }
      finally
      {
        if (timeoutTask != null) {
          timeoutTask.cancel();
        }
        if (oldCatalog != null) {
          locallyScopedConn.setCatalog(oldCatalog);
        }
      }
      this.lastInsertId = this.results.getUpdateID();
      if (cachedMetaData != null) {
        locallyScopedConn.initializeResultsMetadataFromCache(sql, cachedMetaData, this.results);
      } else if (this.connection.getCacheResultSetMetadata()) {
        locallyScopedConn.initializeResultsMetadataFromCache(sql, null, this.results);
      }
      return this.results;
    }
  }
  
  protected void doPingInstead()
    throws SQLException
  {
    if (this.pingTarget != null) {
      this.pingTarget.doPing();
    } else {
      this.connection.ping();
    }
    ResultSetInternalMethods fakeSelectOneResultSet = generatePingResultSet();
    this.results = fakeSelectOneResultSet;
  }
  
  protected ResultSetInternalMethods generatePingResultSet()
    throws SQLException
  {
    Field[] fields = { new Field(null, "1", -5, 1) };
    ArrayList rows = new ArrayList();
    byte[] colVal = { 49 };
    
    rows.add(new ByteArrayRow(new byte[][] { colVal }, getExceptionInterceptor()));
    
    return (ResultSetInternalMethods)DatabaseMetaData.buildResultSet(fields, rows, this.connection);
  }
  
  protected void executeSimpleNonQuery(ConnectionImpl c, String nonQuery)
    throws SQLException
  {
    c.execSQL(this, nonQuery, -1, null, 1003, 1007, false, this.currentCatalog, null, false).close();
  }
  
  public int executeUpdate(String sql)
    throws SQLException
  {
    return executeUpdate(sql, false, false);
  }
  
  protected int executeUpdate(String sql, boolean isBatch, boolean returnGeneratedKeys)
    throws SQLException
  {
    checkClosed();
    
    ConnectionImpl locallyScopedConn = this.connection;
    
    char firstStatementChar = StringUtils.firstAlphaCharUc(sql, findStartOfStatement(sql));
    
    ResultSetInternalMethods rs = null;
    synchronized (locallyScopedConn.getMutex())
    {
      this.retrieveGeneratedKeys = returnGeneratedKeys;
      
      resetCancelledState();
      
      checkNullOrEmptyQuery(sql);
      if (this.doEscapeProcessing)
      {
        Object escapedSqlResult = EscapeProcessor.escapeSQL(sql, this.connection.serverSupportsConvertFn(), this.connection);
        if ((escapedSqlResult instanceof String)) {
          sql = (String)escapedSqlResult;
        } else {
          sql = ((EscapeProcessorResult)escapedSqlResult).escapedSql;
        }
      }
      if (locallyScopedConn.isReadOnly()) {
        throw SQLError.createSQLException(Messages.getString("Statement.42") + Messages.getString("Statement.43"), "S1009", getExceptionInterceptor());
      }
      if (StringUtils.startsWithIgnoreCaseAndWs(sql, "select")) {
        throw SQLError.createSQLException(Messages.getString("Statement.46"), "01S03", getExceptionInterceptor());
      }
      if ((this.results != null) && 
        (!locallyScopedConn.getHoldResultsOpenOverStatementClose())) {
        this.results.realClose(false);
      }
      CancelTask timeoutTask = null;
      
      String oldCatalog = null;
      try
      {
        if ((locallyScopedConn.getEnableQueryTimeouts()) && (this.timeoutInMillis != 0) && (locallyScopedConn.versionMeetsMinimum(5, 0, 0)))
        {
          timeoutTask = new CancelTask(this);
          ConnectionImpl.getCancelTimer().schedule(timeoutTask, this.timeoutInMillis);
        }
        if (!locallyScopedConn.getCatalog().equals(this.currentCatalog))
        {
          oldCatalog = locallyScopedConn.getCatalog();
          locallyScopedConn.setCatalog(this.currentCatalog);
        }
        if (locallyScopedConn.useMaxRows()) {
          executeSimpleNonQuery(locallyScopedConn, "SET OPTION SQL_SELECT_LIMIT=DEFAULT");
        }
        rs = locallyScopedConn.execSQL(this, sql, -1, null, 1003, 1007, false, this.currentCatalog, null, isBatch);
        if (timeoutTask != null)
        {
          if (timeoutTask.caughtWhileCancelling != null) {
            throw timeoutTask.caughtWhileCancelling;
          }
          timeoutTask.cancel();
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
      }
      finally
      {
        if (timeoutTask != null) {
          timeoutTask.cancel();
        }
        if (oldCatalog != null) {
          locallyScopedConn.setCatalog(oldCatalog);
        }
      }
    }
    this.results = rs;
    
    rs.setFirstCharOfQuery(firstStatementChar);
    
    this.updateCount = rs.getUpdateCount();
    
    int truncatedUpdateCount = 0;
    if (this.updateCount > 2147483647L) {
      truncatedUpdateCount = Integer.MAX_VALUE;
    } else {
      truncatedUpdateCount = (int)this.updateCount;
    }
    this.lastInsertId = rs.getUpdateID();
    
    return truncatedUpdateCount;
  }
  
  public int executeUpdate(String sql, int returnGeneratedKeys)
    throws SQLException
  {
    if (returnGeneratedKeys == 1)
    {
      checkClosed();
      
      ConnectionImpl locallyScopedConn = this.connection;
      synchronized (locallyScopedConn.getMutex())
      {
        boolean readInfoMsgState = locallyScopedConn.isReadInfoMsgEnabled();
        
        locallyScopedConn.setReadInfoMsgEnabled(true);
        try
        {
          int i = executeUpdate(sql, false, true);
          
          locallyScopedConn.setReadInfoMsgEnabled(readInfoMsgState);return i;
        }
        finally
        {
          locallyScopedConn.setReadInfoMsgEnabled(readInfoMsgState);
        }
      }
    }
    return executeUpdate(sql);
  }
  
  public int executeUpdate(String sql, int[] generatedKeyIndices)
    throws SQLException
  {
    if ((generatedKeyIndices != null) && (generatedKeyIndices.length > 0))
    {
      checkClosed();
      
      ConnectionImpl locallyScopedConn = this.connection;
      synchronized (locallyScopedConn.getMutex())
      {
        boolean readInfoMsgState = locallyScopedConn.isReadInfoMsgEnabled();
        
        locallyScopedConn.setReadInfoMsgEnabled(true);
        try
        {
          int i = executeUpdate(sql, false, true);
          
          locallyScopedConn.setReadInfoMsgEnabled(readInfoMsgState);return i;
        }
        finally
        {
          locallyScopedConn.setReadInfoMsgEnabled(readInfoMsgState);
        }
      }
    }
    return executeUpdate(sql);
  }
  
  public int executeUpdate(String sql, String[] generatedKeyNames)
    throws SQLException
  {
    if ((generatedKeyNames != null) && (generatedKeyNames.length > 0))
    {
      checkClosed();
      
      ConnectionImpl locallyScopedConn = this.connection;
      synchronized (locallyScopedConn.getMutex())
      {
        boolean readInfoMsgState = this.connection.isReadInfoMsgEnabled();
        
        locallyScopedConn.setReadInfoMsgEnabled(true);
        try
        {
          int i = executeUpdate(sql, false, true);
          
          locallyScopedConn.setReadInfoMsgEnabled(readInfoMsgState);return i;
        }
        finally
        {
          locallyScopedConn.setReadInfoMsgEnabled(readInfoMsgState);
        }
      }
    }
    return executeUpdate(sql);
  }
  
  protected Calendar getCalendarInstanceForSessionOrNew()
  {
    if (this.connection != null) {
      return this.connection.getCalendarInstanceForSessionOrNew();
    }
    return new GregorianCalendar();
  }
  
  public java.sql.Connection getConnection()
    throws SQLException
  {
    return this.connection;
  }
  
  public int getFetchDirection()
    throws SQLException
  {
    return 1000;
  }
  
  public int getFetchSize()
    throws SQLException
  {
    return this.fetchSize;
  }
  
  public synchronized ResultSet getGeneratedKeys()
    throws SQLException
  {
    if (!this.retrieveGeneratedKeys) {
      throw SQLError.createSQLException(Messages.getString("Statement.GeneratedKeysNotRequested"), "S1009", getExceptionInterceptor());
    }
    if (this.batchedGeneratedKeys == null)
    {
      if (this.lastQueryIsOnDupKeyUpdate) {
        return getGeneratedKeysInternal(1);
      }
      return getGeneratedKeysInternal();
    }
    Field[] fields = new Field[1];
    fields[0] = new Field("", "GENERATED_KEY", -5, 17);
    fields[0].setConnection(this.connection);
    
    return ResultSetImpl.getInstance(this.currentCatalog, fields, new RowDataStatic(this.batchedGeneratedKeys), this.connection, this, false);
  }
  
  protected ResultSet getGeneratedKeysInternal()
    throws SQLException
  {
    int numKeys = getUpdateCount();
    return getGeneratedKeysInternal(numKeys);
  }
  
  protected synchronized ResultSet getGeneratedKeysInternal(int numKeys)
    throws SQLException
  {
    Field[] fields = new Field[1];
    fields[0] = new Field("", "GENERATED_KEY", -5, 17);
    fields[0].setConnection(this.connection);
    fields[0].setUseOldNameMetadata(true);
    
    ArrayList rowSet = new ArrayList();
    
    long beginAt = getLastInsertID();
    if (beginAt < 0L) {
      fields[0].setUnsigned();
    }
    if (this.results != null)
    {
      String serverInfo = this.results.getServerInfo();
      if ((numKeys > 0) && (this.results.getFirstCharOfQuery() == 'R') && (serverInfo != null) && (serverInfo.length() > 0)) {
        numKeys = getRecordCountFromInfo(serverInfo);
      }
      if ((beginAt != 0L) && (numKeys > 0)) {
        for (int i = 0; i < numKeys; i++)
        {
          byte[][] row = new byte[1][];
          if (beginAt > 0L)
          {
            row[0] = Long.toString(beginAt).getBytes();
          }
          else
          {
            byte[] asBytes = new byte[8];
            asBytes[7] = ((byte)(int)(beginAt & 0xFF));
            asBytes[6] = ((byte)(int)(beginAt >>> 8));
            asBytes[5] = ((byte)(int)(beginAt >>> 16));
            asBytes[4] = ((byte)(int)(beginAt >>> 24));
            asBytes[3] = ((byte)(int)(beginAt >>> 32));
            asBytes[2] = ((byte)(int)(beginAt >>> 40));
            asBytes[1] = ((byte)(int)(beginAt >>> 48));
            asBytes[0] = ((byte)(int)(beginAt >>> 56));
            
            BigInteger val = new BigInteger(1, asBytes);
            
            row[0] = val.toString().getBytes();
          }
          rowSet.add(new ByteArrayRow(row, getExceptionInterceptor()));
          beginAt += this.connection.getAutoIncrementIncrement();
        }
      }
    }
    ResultSetImpl gkRs = ResultSetImpl.getInstance(this.currentCatalog, fields, new RowDataStatic(rowSet), this.connection, this, false);
    
    this.openResults.add(gkRs);
    
    return gkRs;
  }
  
  protected int getId()
  {
    return this.statementId;
  }
  
  public long getLastInsertID()
  {
    return this.lastInsertId;
  }
  
  public long getLongUpdateCount()
  {
    if (this.results == null) {
      return -1L;
    }
    if (this.results.reallyResult()) {
      return -1L;
    }
    return this.updateCount;
  }
  
  public int getMaxFieldSize()
    throws SQLException
  {
    return this.maxFieldSize;
  }
  
  public int getMaxRows()
    throws SQLException
  {
    if (this.maxRows <= 0) {
      return 0;
    }
    return this.maxRows;
  }
  
  public boolean getMoreResults()
    throws SQLException
  {
    return getMoreResults(1);
  }
  
  public synchronized boolean getMoreResults(int current)
    throws SQLException
  {
    if (this.results == null) {
      return false;
    }
    boolean streamingMode = createStreamingResultSet();
    while ((streamingMode) && 
      (this.results.reallyResult()) && 
      (this.results.next())) {}
    ResultSetInternalMethods nextResultSet = this.results.getNextResultSet();
    switch (current)
    {
    case 1: 
      if (this.results != null)
      {
        if (!streamingMode) {
          this.results.close();
        }
        this.results.clearNextResult();
      }
      break;
    case 3: 
      if (this.results != null)
      {
        if (!streamingMode) {
          this.results.close();
        }
        this.results.clearNextResult();
      }
      closeAllOpenResults();
      
      break;
    case 2: 
      if (!this.connection.getDontTrackOpenResources()) {
        this.openResults.add(this.results);
      }
      this.results.clearNextResult();
      
      break;
    default: 
      throw SQLError.createSQLException(Messages.getString("Statement.19"), "S1009", getExceptionInterceptor());
    }
    this.results = nextResultSet;
    if (this.results == null)
    {
      this.updateCount = -1L;
      this.lastInsertId = -1L;
    }
    else if (this.results.reallyResult())
    {
      this.updateCount = -1L;
      this.lastInsertId = -1L;
    }
    else
    {
      this.updateCount = this.results.getUpdateCount();
      this.lastInsertId = this.results.getUpdateID();
    }
    return (this.results != null) && (this.results.reallyResult());
  }
  
  public int getQueryTimeout()
    throws SQLException
  {
    return this.timeoutInMillis / 1000;
  }
  
  private int getRecordCountFromInfo(String serverInfo)
  {
    StringBuffer recordsBuf = new StringBuffer();
    int recordsCount = 0;
    int duplicatesCount = 0;
    
    char c = '\000';
    
    int length = serverInfo.length();
    for (int i = 0; i < length; i++)
    {
      c = serverInfo.charAt(i);
      if (Character.isDigit(c)) {
        break;
      }
    }
    recordsBuf.append(c);
    i++;
    for (; i < length; i++)
    {
      c = serverInfo.charAt(i);
      if (!Character.isDigit(c)) {
        break;
      }
      recordsBuf.append(c);
    }
    recordsCount = Integer.parseInt(recordsBuf.toString());
    
    StringBuffer duplicatesBuf = new StringBuffer();
    for (; i < length; i++)
    {
      c = serverInfo.charAt(i);
      if (Character.isDigit(c)) {
        break;
      }
    }
    duplicatesBuf.append(c);
    i++;
    for (; i < length; i++)
    {
      c = serverInfo.charAt(i);
      if (!Character.isDigit(c)) {
        break;
      }
      duplicatesBuf.append(c);
    }
    duplicatesCount = Integer.parseInt(duplicatesBuf.toString());
    
    return recordsCount - duplicatesCount;
  }
  
  public ResultSet getResultSet()
    throws SQLException
  {
    return (this.results != null) && (this.results.reallyResult()) ? this.results : null;
  }
  
  public int getResultSetConcurrency()
    throws SQLException
  {
    return this.resultSetConcurrency;
  }
  
  public int getResultSetHoldability()
    throws SQLException
  {
    return 1;
  }
  
  protected ResultSetInternalMethods getResultSetInternal()
  {
    return this.results;
  }
  
  public int getResultSetType()
    throws SQLException
  {
    return this.resultSetType;
  }
  
  public int getUpdateCount()
    throws SQLException
  {
    if (this.results == null) {
      return -1;
    }
    if (this.results.reallyResult()) {
      return -1;
    }
    int truncatedUpdateCount = 0;
    if (this.results.getUpdateCount() > 2147483647L) {
      truncatedUpdateCount = Integer.MAX_VALUE;
    } else {
      truncatedUpdateCount = (int)this.results.getUpdateCount();
    }
    return truncatedUpdateCount;
  }
  
  public SQLWarning getWarnings()
    throws SQLException
  {
    checkClosed();
    if ((this.connection != null) && (!this.connection.isClosed()) && (this.connection.versionMeetsMinimum(4, 1, 0)))
    {
      SQLWarning pendingWarningsFromServer = SQLError.convertShowWarningsToSQLWarnings(this.connection);
      if (this.warningChain != null) {
        this.warningChain.setNextWarning(pendingWarningsFromServer);
      } else {
        this.warningChain = pendingWarningsFromServer;
      }
      return this.warningChain;
    }
    return this.warningChain;
  }
  
  protected synchronized void realClose(boolean calledExplicitly, boolean closeOpenResults)
    throws SQLException
  {
    if (this.isClosed) {
      return;
    }
    if ((this.useUsageAdvisor) && 
      (!calledExplicitly))
    {
      String message = Messages.getString("Statement.63") + Messages.getString("Statement.64");
      
      this.eventSink.consumeEvent(new ProfilerEvent((byte)0, "", this.currentCatalog, this.connectionId, getId(), -1, System.currentTimeMillis(), 0L, Constants.MILLIS_I18N, null, this.pointOfOrigin, message));
    }
    if (closeOpenResults) {
      closeOpenResults = !this.holdResultsOpenOverClose;
    }
    if (closeOpenResults)
    {
      if (this.results != null) {
        try
        {
          this.results.close();
        }
        catch (Exception ex) {}
      }
      closeAllOpenResults();
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
    this.isClosed = true;
    
    this.results = null;
    this.connection = null;
    this.warningChain = null;
    this.openResults = null;
    this.batchedGeneratedKeys = null;
    this.localInfileInputStream = null;
    this.pingTarget = null;
  }
  
  public void setCursorName(String name)
    throws SQLException
  {}
  
  public void setEscapeProcessing(boolean enable)
    throws SQLException
  {
    this.doEscapeProcessing = enable;
  }
  
  public void setFetchDirection(int direction)
    throws SQLException
  {
    switch (direction)
    {
    case 1000: 
    case 1001: 
    case 1002: 
      break;
    default: 
      throw SQLError.createSQLException(Messages.getString("Statement.5"), "S1009", getExceptionInterceptor());
    }
  }
  
  public void setFetchSize(int rows)
    throws SQLException
  {
    if (((rows < 0) && (rows != Integer.MIN_VALUE)) || ((this.maxRows != 0) && (this.maxRows != -1) && (rows > getMaxRows()))) {
      throw SQLError.createSQLException(Messages.getString("Statement.7"), "S1009", getExceptionInterceptor());
    }
    this.fetchSize = rows;
  }
  
  protected void setHoldResultsOpenOverClose(boolean holdResultsOpenOverClose)
  {
    this.holdResultsOpenOverClose = holdResultsOpenOverClose;
  }
  
  public void setMaxFieldSize(int max)
    throws SQLException
  {
    if (max < 0) {
      throw SQLError.createSQLException(Messages.getString("Statement.11"), "S1009", getExceptionInterceptor());
    }
    int maxBuf = this.connection != null ? this.connection.getMaxAllowedPacket() : MysqlIO.getMaxBuf();
    if (max > maxBuf) {
      throw SQLError.createSQLException(Messages.getString("Statement.13", new Object[] { Constants.longValueOf(maxBuf) }), "S1009", getExceptionInterceptor());
    }
    this.maxFieldSize = max;
  }
  
  public void setMaxRows(int max)
    throws SQLException
  {
    if ((max > 50000000) || (max < 0)) {
      throw SQLError.createSQLException(Messages.getString("Statement.15") + max + " > " + 50000000 + ".", "S1009", getExceptionInterceptor());
    }
    if (max == 0) {
      max = -1;
    }
    this.maxRows = max;
    this.maxRowsChanged = true;
    if (this.maxRows == -1)
    {
      this.connection.unsetMaxRows(this);
      this.maxRowsChanged = false;
    }
    else
    {
      this.connection.maxRowsChanged(this);
    }
  }
  
  public void setQueryTimeout(int seconds)
    throws SQLException
  {
    if (seconds < 0) {
      throw SQLError.createSQLException(Messages.getString("Statement.21"), "S1009", getExceptionInterceptor());
    }
    this.timeoutInMillis = (seconds * 1000);
  }
  
  void setResultSetConcurrency(int concurrencyFlag)
  {
    this.resultSetConcurrency = concurrencyFlag;
  }
  
  void setResultSetType(int typeFlag)
  {
    this.resultSetType = typeFlag;
  }
  
  protected void getBatchedGeneratedKeys(java.sql.Statement batchedStatement)
    throws SQLException
  {
    if (this.retrieveGeneratedKeys)
    {
      ResultSet rs = null;
      try
      {
        rs = batchedStatement.getGeneratedKeys();
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
  
  protected void getBatchedGeneratedKeys(int maxKeys)
    throws SQLException
  {
    if (this.retrieveGeneratedKeys)
    {
      ResultSet rs = null;
      try
      {
        if (maxKeys == 0) {
          rs = getGeneratedKeysInternal();
        } else {
          rs = getGeneratedKeysInternal(maxKeys);
        }
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
  
  private boolean useServerFetch()
    throws SQLException
  {
    return (this.connection.isCursorFetchEnabled()) && (this.fetchSize > 0) && (this.resultSetConcurrency == 1007) && (this.resultSetType == 1003);
  }
  
  public synchronized boolean isClosed()
    throws SQLException
  {
    return this.isClosed;
  }
  
  private boolean isPoolable = true;
  private InputStream localInfileInputStream;
  
  public boolean isPoolable()
    throws SQLException
  {
    return this.isPoolable;
  }
  
  public void setPoolable(boolean poolable)
    throws SQLException
  {
    this.isPoolable = poolable;
  }
  
  public boolean isWrapperFor(Class iface)
    throws SQLException
  {
    checkClosed();
    
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
      throw SQLError.createSQLException("Unable to unwrap to " + iface.toString(), "S1009", getExceptionInterceptor());
    }
  }
  
  protected int findStartOfStatement(String sql)
  {
    int statementStartPos = 0;
    if (StringUtils.startsWithIgnoreCaseAndWs(sql, "/*"))
    {
      statementStartPos = sql.indexOf("*/");
      if (statementStartPos == -1) {
        statementStartPos = 0;
      } else {
        statementStartPos += 2;
      }
    }
    else if ((StringUtils.startsWithIgnoreCaseAndWs(sql, "--")) || (StringUtils.startsWithIgnoreCaseAndWs(sql, "#")))
    {
      statementStartPos = sql.indexOf('\n');
      if (statementStartPos == -1)
      {
        statementStartPos = sql.indexOf('\r');
        if (statementStartPos == -1) {
          statementStartPos = 0;
        }
      }
    }
    return statementStartPos;
  }
  
  public synchronized InputStream getLocalInfileInputStream()
  {
    return this.localInfileInputStream;
  }
  
  public synchronized void setLocalInfileInputStream(InputStream stream)
  {
    this.localInfileInputStream = stream;
  }
  
  public synchronized void setPingTarget(PingTarget pingTarget)
  {
    this.pingTarget = pingTarget;
  }
  
  public ExceptionInterceptor getExceptionInterceptor()
  {
    return this.exceptionInterceptor;
  }
  
  protected boolean containsOnDuplicateKeyInString(String sql)
  {
    return getOnDuplicateKeyLocation(sql) != -1;
  }
  
  protected int getOnDuplicateKeyLocation(String sql)
  {
    return StringUtils.indexOfIgnoreCaseRespectMarker(0, sql, " ON DUPLICATE KEY UPDATE ", "\"'`", "\"'`", !this.connection.isNoBackslashEscapesSet());
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\mysql\jdbc\StatementImpl.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */