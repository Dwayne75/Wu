package com.mysql.jdbc;

import com.mysql.jdbc.profiler.ProfilerEvent;
import com.mysql.jdbc.profiler.ProfilerEventHandler;
import com.mysql.jdbc.profiler.ProfilerEventHandlerFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Array;
import java.sql.Date;
import java.sql.Ref;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.TreeMap;

public class ResultSetImpl
  implements ResultSetInternalMethods
{
  private static final Constructor JDBC_4_RS_4_ARG_CTOR;
  private static final Constructor JDBC_4_RS_6_ARG_CTOR;
  private static final Constructor JDBC_4_UPD_RS_6_ARG_CTOR;
  protected static final double MIN_DIFF_PREC;
  protected static final double MAX_DIFF_PREC;
  protected static int resultCounter;
  
  protected static BigInteger convertLongToUlong(long longVal)
  {
    byte[] asBytes = new byte[8];
    asBytes[7] = ((byte)(int)(longVal & 0xFF));
    asBytes[6] = ((byte)(int)(longVal >>> 8));
    asBytes[5] = ((byte)(int)(longVal >>> 16));
    asBytes[4] = ((byte)(int)(longVal >>> 24));
    asBytes[3] = ((byte)(int)(longVal >>> 32));
    asBytes[2] = ((byte)(int)(longVal >>> 40));
    asBytes[1] = ((byte)(int)(longVal >>> 48));
    asBytes[0] = ((byte)(int)(longVal >>> 56));
    
    return new BigInteger(1, asBytes);
  }
  
  protected String catalog = null;
  protected Map columnLabelToIndex = null;
  protected Map columnToIndexCache = null;
  protected boolean[] columnUsed = null;
  protected ConnectionImpl connection;
  protected long connectionId = 0L;
  protected int currentRow = -1;
  TimeZone defaultTimeZone;
  protected boolean doingUpdates = false;
  protected ProfilerEventHandler eventSink = null;
  Calendar fastDateCal = null;
  protected int fetchDirection = 1000;
  protected int fetchSize = 0;
  protected Field[] fields;
  protected char firstCharOfQuery;
  protected Map fullColumnNameToIndex = null;
  protected Map columnNameToIndex = null;
  protected boolean hasBuiltIndexMapping = false;
  protected boolean isBinaryEncoded = false;
  protected boolean isClosed = false;
  protected ResultSetInternalMethods nextResultSet = null;
  protected boolean onInsertRow = false;
  protected StatementImpl owningStatement;
  protected Throwable pointOfOrigin;
  protected boolean profileSql = false;
  protected boolean reallyResult = false;
  protected int resultId;
  protected int resultSetConcurrency = 0;
  protected int resultSetType = 0;
  protected RowData rowData;
  protected String serverInfo = null;
  PreparedStatement statementUsedForFetchingRows;
  protected ResultSetRow thisRow = null;
  protected long updateCount;
  protected long updateId = -1L;
  private boolean useStrictFloatingPoint = false;
  protected boolean useUsageAdvisor = false;
  protected SQLWarning warningChain = null;
  protected boolean wasNullFlag = false;
  protected Statement wrapperStatement;
  protected boolean retainOwningStatement;
  protected Calendar gmtCalendar = null;
  protected boolean useFastDateParsing = false;
  private boolean padCharsWithSpace = false;
  private boolean jdbcCompliantTruncationForReads;
  private boolean useFastIntParsing = true;
  private boolean useColumnNamesInFindColumn;
  private ExceptionInterceptor exceptionInterceptor;
  protected static final char[] EMPTY_SPACE;
  
  static
  {
    if (Util.isJdbc4())
    {
      try
      {
        JDBC_4_RS_4_ARG_CTOR = Class.forName("com.mysql.jdbc.JDBC4ResultSet").getConstructor(new Class[] { Long.TYPE, Long.TYPE, ConnectionImpl.class, StatementImpl.class });
        
        JDBC_4_RS_6_ARG_CTOR = Class.forName("com.mysql.jdbc.JDBC4ResultSet").getConstructor(new Class[] { String.class, new Field[0].getClass(), RowData.class, ConnectionImpl.class, StatementImpl.class });
        
        JDBC_4_UPD_RS_6_ARG_CTOR = Class.forName("com.mysql.jdbc.JDBC4UpdatableResultSet").getConstructor(new Class[] { String.class, new Field[0].getClass(), RowData.class, ConnectionImpl.class, StatementImpl.class });
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
    }
    else
    {
      JDBC_4_RS_4_ARG_CTOR = null;
      JDBC_4_RS_6_ARG_CTOR = null;
      JDBC_4_UPD_RS_6_ARG_CTOR = null;
    }
    MIN_DIFF_PREC = Float.parseFloat(Float.toString(Float.MIN_VALUE)) - Double.parseDouble(Float.toString(Float.MIN_VALUE));
    
    MAX_DIFF_PREC = Float.parseFloat(Float.toString(Float.MAX_VALUE)) - Double.parseDouble(Float.toString(Float.MAX_VALUE));
    
    resultCounter = 1;
    
    EMPTY_SPACE = new char['ÿ'];
    for (int i = 0; i < EMPTY_SPACE.length; i++) {
      EMPTY_SPACE[i] = ' ';
    }
  }
  
  protected static ResultSetImpl getInstance(long updateCount, long updateID, ConnectionImpl conn, StatementImpl creatorStmt)
    throws SQLException
  {
    if (!Util.isJdbc4()) {
      return new ResultSetImpl(updateCount, updateID, conn, creatorStmt);
    }
    return (ResultSetImpl)Util.handleNewInstance(JDBC_4_RS_4_ARG_CTOR, new Object[] { Constants.longValueOf(updateCount), Constants.longValueOf(updateID), conn, creatorStmt }, conn.getExceptionInterceptor());
  }
  
  protected static ResultSetImpl getInstance(String catalog, Field[] fields, RowData tuples, ConnectionImpl conn, StatementImpl creatorStmt, boolean isUpdatable)
    throws SQLException
  {
    if (!Util.isJdbc4())
    {
      if (!isUpdatable) {
        return new ResultSetImpl(catalog, fields, tuples, conn, creatorStmt);
      }
      return new UpdatableResultSet(catalog, fields, tuples, conn, creatorStmt);
    }
    if (!isUpdatable) {
      return (ResultSetImpl)Util.handleNewInstance(JDBC_4_RS_6_ARG_CTOR, new Object[] { catalog, fields, tuples, conn, creatorStmt }, conn.getExceptionInterceptor());
    }
    return (ResultSetImpl)Util.handleNewInstance(JDBC_4_UPD_RS_6_ARG_CTOR, new Object[] { catalog, fields, tuples, conn, creatorStmt }, conn.getExceptionInterceptor());
  }
  
  public ResultSetImpl(long updateCount, long updateID, ConnectionImpl conn, StatementImpl creatorStmt)
  {
    this.updateCount = updateCount;
    this.updateId = updateID;
    this.reallyResult = false;
    this.fields = new Field[0];
    
    this.connection = conn;
    this.owningStatement = creatorStmt;
    
    this.exceptionInterceptor = this.connection.getExceptionInterceptor();
    
    this.retainOwningStatement = false;
    if (this.connection != null)
    {
      this.retainOwningStatement = this.connection.getRetainStatementAfterResultSetClose();
      
      this.connectionId = this.connection.getId();
      this.serverTimeZoneTz = this.connection.getServerTimezoneTZ();
      this.padCharsWithSpace = this.connection.getPadCharsWithSpace();
    }
    this.useLegacyDatetimeCode = this.connection.getUseLegacyDatetimeCode();
  }
  
  public ResultSetImpl(String catalog, Field[] fields, RowData tuples, ConnectionImpl conn, StatementImpl creatorStmt)
    throws SQLException
  {
    this.connection = conn;
    
    this.retainOwningStatement = false;
    if (this.connection != null)
    {
      this.useStrictFloatingPoint = this.connection.getStrictFloatingPoint();
      
      setDefaultTimeZone(this.connection.getDefaultTimeZone());
      this.connectionId = this.connection.getId();
      this.useFastDateParsing = this.connection.getUseFastDateParsing();
      this.profileSql = this.connection.getProfileSql();
      this.retainOwningStatement = this.connection.getRetainStatementAfterResultSetClose();
      
      this.jdbcCompliantTruncationForReads = this.connection.getJdbcCompliantTruncationForReads();
      this.useFastIntParsing = this.connection.getUseFastIntParsing();
      this.serverTimeZoneTz = this.connection.getServerTimezoneTZ();
      this.padCharsWithSpace = this.connection.getPadCharsWithSpace();
    }
    this.owningStatement = creatorStmt;
    
    this.catalog = catalog;
    
    this.fields = fields;
    this.rowData = tuples;
    this.updateCount = this.rowData.size();
    
    this.reallyResult = true;
    if (this.rowData.size() > 0)
    {
      if ((this.updateCount == 1L) && 
        (this.thisRow == null))
      {
        this.rowData.close();
        this.updateCount = -1L;
      }
    }
    else {
      this.thisRow = null;
    }
    this.rowData.setOwner(this);
    if (this.fields != null) {
      initializeWithMetadata();
    }
    this.useLegacyDatetimeCode = this.connection.getUseLegacyDatetimeCode();
    
    this.useColumnNamesInFindColumn = this.connection.getUseColumnNamesInFindColumn();
    
    setRowPositionValidity();
  }
  
  public void initializeWithMetadata()
    throws SQLException
  {
    this.rowData.setMetadata(this.fields);
    
    this.columnToIndexCache = new HashMap();
    if ((this.profileSql) || (this.connection.getUseUsageAdvisor()))
    {
      this.columnUsed = new boolean[this.fields.length];
      this.pointOfOrigin = new Throwable();
      this.resultId = (resultCounter++);
      this.useUsageAdvisor = this.connection.getUseUsageAdvisor();
      this.eventSink = ProfilerEventHandlerFactory.getInstance(this.connection);
    }
    if (this.connection.getGatherPerformanceMetrics())
    {
      this.connection.incrementNumberOfResultSetsCreated();
      
      Map tableNamesMap = new HashMap();
      for (int i = 0; i < this.fields.length; i++)
      {
        Field f = this.fields[i];
        
        String tableName = f.getOriginalTableName();
        if (tableName == null) {
          tableName = f.getTableName();
        }
        if (tableName != null)
        {
          if (this.connection.lowerCaseTableNames()) {
            tableName = tableName.toLowerCase();
          }
          tableNamesMap.put(tableName, null);
        }
      }
      this.connection.reportNumberOfTablesAccessed(tableNamesMap.size());
    }
  }
  
  private synchronized void createCalendarIfNeeded()
  {
    if (this.fastDateCal == null)
    {
      this.fastDateCal = new GregorianCalendar(Locale.US);
      this.fastDateCal.setTimeZone(getDefaultTimeZone());
    }
  }
  
  public boolean absolute(int row)
    throws SQLException
  {
    checkClosed();
    boolean b;
    boolean b;
    if (this.rowData.size() == 0)
    {
      b = false;
    }
    else
    {
      if (row == 0) {
        throw SQLError.createSQLException(Messages.getString("ResultSet.Cannot_absolute_position_to_row_0_110"), "S1009", getExceptionInterceptor());
      }
      if (this.onInsertRow) {
        this.onInsertRow = false;
      }
      if (this.doingUpdates) {
        this.doingUpdates = false;
      }
      if (this.thisRow != null) {
        this.thisRow.closeOpenStreams();
      }
      boolean b;
      if (row == 1)
      {
        b = first();
      }
      else
      {
        boolean b;
        if (row == -1)
        {
          b = last();
        }
        else
        {
          boolean b;
          if (row > this.rowData.size())
          {
            afterLast();
            b = false;
          }
          else
          {
            boolean b;
            if (row < 0)
            {
              int newRowPosition = this.rowData.size() + row + 1;
              boolean b;
              if (newRowPosition <= 0)
              {
                beforeFirst();
                b = false;
              }
              else
              {
                b = absolute(newRowPosition);
              }
            }
            else
            {
              row--;
              this.rowData.setCurrentRow(row);
              this.thisRow = this.rowData.getAt(row);
              b = true;
            }
          }
        }
      }
    }
    setRowPositionValidity();
    
    return b;
  }
  
  public void afterLast()
    throws SQLException
  {
    checkClosed();
    if (this.onInsertRow) {
      this.onInsertRow = false;
    }
    if (this.doingUpdates) {
      this.doingUpdates = false;
    }
    if (this.thisRow != null) {
      this.thisRow.closeOpenStreams();
    }
    if (this.rowData.size() != 0)
    {
      this.rowData.afterLast();
      this.thisRow = null;
    }
    setRowPositionValidity();
  }
  
  public void beforeFirst()
    throws SQLException
  {
    checkClosed();
    if (this.onInsertRow) {
      this.onInsertRow = false;
    }
    if (this.doingUpdates) {
      this.doingUpdates = false;
    }
    if (this.rowData.size() == 0) {
      return;
    }
    if (this.thisRow != null) {
      this.thisRow.closeOpenStreams();
    }
    this.rowData.beforeFirst();
    this.thisRow = null;
    
    setRowPositionValidity();
  }
  
  public void buildIndexMapping()
    throws SQLException
  {
    int numFields = this.fields.length;
    this.columnLabelToIndex = new TreeMap(String.CASE_INSENSITIVE_ORDER);
    this.fullColumnNameToIndex = new TreeMap(String.CASE_INSENSITIVE_ORDER);
    this.columnNameToIndex = new TreeMap(String.CASE_INSENSITIVE_ORDER);
    for (int i = numFields - 1; i >= 0; i--)
    {
      Integer index = Constants.integerValueOf(i);
      String columnName = this.fields[i].getOriginalName();
      String columnLabel = this.fields[i].getName();
      String fullColumnName = this.fields[i].getFullName();
      if (columnLabel != null) {
        this.columnLabelToIndex.put(columnLabel, index);
      }
      if (fullColumnName != null) {
        this.fullColumnNameToIndex.put(fullColumnName, index);
      }
      if (columnName != null) {
        this.columnNameToIndex.put(columnName, index);
      }
    }
    this.hasBuiltIndexMapping = true;
  }
  
  public void cancelRowUpdates()
    throws SQLException
  {
    throw new NotUpdatable();
  }
  
  protected final void checkClosed()
    throws SQLException
  {
    if (this.isClosed) {
      throw SQLError.createSQLException(Messages.getString("ResultSet.Operation_not_allowed_after_ResultSet_closed_144"), "S1000", getExceptionInterceptor());
    }
  }
  
  protected final void checkColumnBounds(int columnIndex)
    throws SQLException
  {
    if (columnIndex < 1) {
      throw SQLError.createSQLException(Messages.getString("ResultSet.Column_Index_out_of_range_low", new Object[] { Constants.integerValueOf(columnIndex), Constants.integerValueOf(this.fields.length) }), "S1009", getExceptionInterceptor());
    }
    if (columnIndex > this.fields.length) {
      throw SQLError.createSQLException(Messages.getString("ResultSet.Column_Index_out_of_range_high", new Object[] { Constants.integerValueOf(columnIndex), Constants.integerValueOf(this.fields.length) }), "S1009", getExceptionInterceptor());
    }
    if ((this.profileSql) || (this.useUsageAdvisor)) {
      this.columnUsed[(columnIndex - 1)] = true;
    }
  }
  
  protected void checkRowPos()
    throws SQLException
  {
    checkClosed();
    if (!this.onValidRow) {
      throw SQLError.createSQLException(this.invalidRowReason, "S1000", getExceptionInterceptor());
    }
  }
  
  private boolean onValidRow = false;
  private String invalidRowReason = null;
  protected boolean useLegacyDatetimeCode;
  private TimeZone serverTimeZoneTz;
  
  private void setRowPositionValidity()
    throws SQLException
  {
    if ((!this.rowData.isDynamic()) && (this.rowData.size() == 0))
    {
      this.invalidRowReason = Messages.getString("ResultSet.Illegal_operation_on_empty_result_set");
      
      this.onValidRow = false;
    }
    else if (this.rowData.isBeforeFirst())
    {
      this.invalidRowReason = Messages.getString("ResultSet.Before_start_of_result_set_146");
      
      this.onValidRow = false;
    }
    else if (this.rowData.isAfterLast())
    {
      this.invalidRowReason = Messages.getString("ResultSet.After_end_of_result_set_148");
      
      this.onValidRow = false;
    }
    else
    {
      this.onValidRow = true;
      this.invalidRowReason = null;
    }
  }
  
  public void clearNextResult()
  {
    this.nextResultSet = null;
  }
  
  public void clearWarnings()
    throws SQLException
  {
    this.warningChain = null;
  }
  
  public void close()
    throws SQLException
  {
    realClose(true);
  }
  
  private int convertToZeroWithEmptyCheck()
    throws SQLException
  {
    if (this.connection.getEmptyStringsConvertToZero()) {
      return 0;
    }
    throw SQLError.createSQLException("Can't convert empty string ('') to numeric", "22018", getExceptionInterceptor());
  }
  
  private String convertToZeroLiteralStringWithEmptyCheck()
    throws SQLException
  {
    if (this.connection.getEmptyStringsConvertToZero()) {
      return "0";
    }
    throw SQLError.createSQLException("Can't convert empty string ('') to numeric", "22018", getExceptionInterceptor());
  }
  
  public ResultSetInternalMethods copy()
    throws SQLException
  {
    ResultSetInternalMethods rs = getInstance(this.catalog, this.fields, this.rowData, this.connection, this.owningStatement, false);
    
    return rs;
  }
  
  public void redefineFieldsForDBMD(Field[] f)
  {
    this.fields = f;
    for (int i = 0; i < this.fields.length; i++)
    {
      this.fields[i].setUseOldNameMetadata(true);
      this.fields[i].setConnection(this.connection);
    }
  }
  
  public void populateCachedMetaData(CachedResultSetMetaData cachedMetaData)
    throws SQLException
  {
    cachedMetaData.fields = this.fields;
    cachedMetaData.columnNameToIndex = this.columnLabelToIndex;
    cachedMetaData.fullColumnNameToIndex = this.fullColumnNameToIndex;
    cachedMetaData.metadata = getMetaData();
  }
  
  public void initializeFromCachedMetaData(CachedResultSetMetaData cachedMetaData)
  {
    this.fields = cachedMetaData.fields;
    this.columnLabelToIndex = cachedMetaData.columnNameToIndex;
    this.fullColumnNameToIndex = cachedMetaData.fullColumnNameToIndex;
    this.hasBuiltIndexMapping = true;
  }
  
  public void deleteRow()
    throws SQLException
  {
    throw new NotUpdatable();
  }
  
  private String extractStringFromNativeColumn(int columnIndex, int mysqlType)
    throws SQLException
  {
    int columnIndexMinusOne = columnIndex - 1;
    
    this.wasNullFlag = false;
    if (this.thisRow.isNull(columnIndexMinusOne))
    {
      this.wasNullFlag = true;
      
      return null;
    }
    this.wasNullFlag = false;
    
    String encoding = this.fields[columnIndexMinusOne].getCharacterSet();
    
    return this.thisRow.getString(columnIndex - 1, encoding, this.connection);
  }
  
  protected synchronized Date fastDateCreate(Calendar cal, int year, int month, int day)
  {
    if (this.useLegacyDatetimeCode) {
      return TimeUtil.fastDateCreate(year, month, day, cal);
    }
    if (cal == null)
    {
      createCalendarIfNeeded();
      cal = this.fastDateCal;
    }
    boolean useGmtMillis = this.connection.getUseGmtMillisForDatetimes();
    
    return TimeUtil.fastDateCreate(useGmtMillis, useGmtMillis ? getGmtCalendar() : cal, cal, year, month, day);
  }
  
  protected synchronized Time fastTimeCreate(Calendar cal, int hour, int minute, int second)
    throws SQLException
  {
    if (!this.useLegacyDatetimeCode) {
      return TimeUtil.fastTimeCreate(hour, minute, second, cal, getExceptionInterceptor());
    }
    if (cal == null)
    {
      createCalendarIfNeeded();
      cal = this.fastDateCal;
    }
    return TimeUtil.fastTimeCreate(cal, hour, minute, second, getExceptionInterceptor());
  }
  
  protected synchronized Timestamp fastTimestampCreate(Calendar cal, int year, int month, int day, int hour, int minute, int seconds, int secondsPart)
  {
    if (!this.useLegacyDatetimeCode) {
      return TimeUtil.fastTimestampCreate(cal.getTimeZone(), year, month, day, hour, minute, seconds, secondsPart);
    }
    if (cal == null)
    {
      createCalendarIfNeeded();
      cal = this.fastDateCal;
    }
    boolean useGmtMillis = this.connection.getUseGmtMillisForDatetimes();
    
    return TimeUtil.fastTimestampCreate(useGmtMillis, useGmtMillis ? getGmtCalendar() : null, cal, year, month, day, hour, minute, seconds, secondsPart);
  }
  
  public synchronized int findColumn(String columnName)
    throws SQLException
  {
    checkClosed();
    if (!this.hasBuiltIndexMapping) {
      buildIndexMapping();
    }
    Integer index = (Integer)this.columnToIndexCache.get(columnName);
    if (index != null) {
      return index.intValue() + 1;
    }
    index = (Integer)this.columnLabelToIndex.get(columnName);
    if ((index == null) && (this.useColumnNamesInFindColumn)) {
      index = (Integer)this.columnNameToIndex.get(columnName);
    }
    if (index == null) {
      index = (Integer)this.fullColumnNameToIndex.get(columnName);
    }
    if (index != null)
    {
      this.columnToIndexCache.put(columnName, index);
      
      return index.intValue() + 1;
    }
    for (int i = 0; i < this.fields.length; i++)
    {
      if (this.fields[i].getName().equalsIgnoreCase(columnName)) {
        return i + 1;
      }
      if (this.fields[i].getFullName().equalsIgnoreCase(columnName)) {
        return i + 1;
      }
    }
    throw SQLError.createSQLException(Messages.getString("ResultSet.Column____112") + columnName + Messages.getString("ResultSet.___not_found._113"), "S0022", getExceptionInterceptor());
  }
  
  public boolean first()
    throws SQLException
  {
    checkClosed();
    
    boolean b = true;
    if (this.rowData.isEmpty())
    {
      b = false;
    }
    else
    {
      if (this.onInsertRow) {
        this.onInsertRow = false;
      }
      if (this.doingUpdates) {
        this.doingUpdates = false;
      }
      this.rowData.beforeFirst();
      this.thisRow = this.rowData.next();
    }
    setRowPositionValidity();
    
    return b;
  }
  
  public Array getArray(int i)
    throws SQLException
  {
    checkColumnBounds(i);
    
    throw SQLError.notImplemented();
  }
  
  public Array getArray(String colName)
    throws SQLException
  {
    return getArray(findColumn(colName));
  }
  
  public InputStream getAsciiStream(int columnIndex)
    throws SQLException
  {
    checkRowPos();
    if (!this.isBinaryEncoded) {
      return getBinaryStream(columnIndex);
    }
    return getNativeBinaryStream(columnIndex);
  }
  
  public InputStream getAsciiStream(String columnName)
    throws SQLException
  {
    return getAsciiStream(findColumn(columnName));
  }
  
  public BigDecimal getBigDecimal(int columnIndex)
    throws SQLException
  {
    if (!this.isBinaryEncoded)
    {
      String stringVal = getString(columnIndex);
      if (stringVal != null)
      {
        if (stringVal.length() == 0)
        {
          BigDecimal val = new BigDecimal(convertToZeroLiteralStringWithEmptyCheck());
          
          return val;
        }
        try
        {
          return new BigDecimal(stringVal);
        }
        catch (NumberFormatException ex)
        {
          throw SQLError.createSQLException(Messages.getString("ResultSet.Bad_format_for_BigDecimal", new Object[] { stringVal, Constants.integerValueOf(columnIndex) }), "S1009", getExceptionInterceptor());
        }
      }
      return null;
    }
    return getNativeBigDecimal(columnIndex);
  }
  
  /**
   * @deprecated
   */
  public BigDecimal getBigDecimal(int columnIndex, int scale)
    throws SQLException
  {
    if (!this.isBinaryEncoded)
    {
      String stringVal = getString(columnIndex);
      if (stringVal != null)
      {
        if (stringVal.length() == 0)
        {
          BigDecimal val = new BigDecimal(convertToZeroLiteralStringWithEmptyCheck());
          try
          {
            return val.setScale(scale);
          }
          catch (ArithmeticException ex)
          {
            try
            {
              return val.setScale(scale, 4);
            }
            catch (ArithmeticException arEx)
            {
              throw SQLError.createSQLException(Messages.getString("ResultSet.Bad_format_for_BigDecimal", new Object[] { stringVal, new Integer(columnIndex) }), "S1009", getExceptionInterceptor());
            }
          }
        }
        try
        {
          val = new BigDecimal(stringVal);
        }
        catch (NumberFormatException ex)
        {
          BigDecimal val;
          BigDecimal val;
          if (this.fields[(columnIndex - 1)].getMysqlType() == 16)
          {
            long valueAsLong = getNumericRepresentationOfSQLBitType(columnIndex);
            
            val = new BigDecimal(valueAsLong);
          }
          else
          {
            throw SQLError.createSQLException(Messages.getString("ResultSet.Bad_format_for_BigDecimal", new Object[] { Constants.integerValueOf(columnIndex), stringVal }), "S1009", getExceptionInterceptor());
          }
        }
        try
        {
          return val.setScale(scale);
        }
        catch (ArithmeticException ex)
        {
          try
          {
            BigDecimal val;
            return val.setScale(scale, 4);
          }
          catch (ArithmeticException arithEx)
          {
            throw SQLError.createSQLException(Messages.getString("ResultSet.Bad_format_for_BigDecimal", new Object[] { Constants.integerValueOf(columnIndex), stringVal }), "S1009", getExceptionInterceptor());
          }
        }
      }
      return null;
    }
    return getNativeBigDecimal(columnIndex, scale);
  }
  
  public BigDecimal getBigDecimal(String columnName)
    throws SQLException
  {
    return getBigDecimal(findColumn(columnName));
  }
  
  /**
   * @deprecated
   */
  public BigDecimal getBigDecimal(String columnName, int scale)
    throws SQLException
  {
    return getBigDecimal(findColumn(columnName), scale);
  }
  
  private final BigDecimal getBigDecimalFromString(String stringVal, int columnIndex, int scale)
    throws SQLException
  {
    if (stringVal != null)
    {
      if (stringVal.length() == 0)
      {
        BigDecimal bdVal = new BigDecimal(convertToZeroLiteralStringWithEmptyCheck());
        try
        {
          return bdVal.setScale(scale);
        }
        catch (ArithmeticException ex)
        {
          try
          {
            return bdVal.setScale(scale, 4);
          }
          catch (ArithmeticException arEx)
          {
            throw new SQLException(Messages.getString("ResultSet.Bad_format_for_BigDecimal", new Object[] { stringVal, Constants.integerValueOf(columnIndex) }), "S1009");
          }
        }
      }
      try
      {
        return new BigDecimal(stringVal).setScale(scale);
      }
      catch (ArithmeticException ex)
      {
        try
        {
          return new BigDecimal(stringVal).setScale(scale, 4);
        }
        catch (ArithmeticException arEx)
        {
          throw new SQLException(Messages.getString("ResultSet.Bad_format_for_BigDecimal", new Object[] { stringVal, Constants.integerValueOf(columnIndex) }), "S1009");
        }
      }
      catch (NumberFormatException ex)
      {
        if (this.fields[(columnIndex - 1)].getMysqlType() == 16)
        {
          long valueAsLong = getNumericRepresentationOfSQLBitType(columnIndex);
          try
          {
            return new BigDecimal(valueAsLong).setScale(scale);
          }
          catch (ArithmeticException arEx1)
          {
            try
            {
              return new BigDecimal(valueAsLong).setScale(scale, 4);
            }
            catch (ArithmeticException arEx2)
            {
              throw new SQLException(Messages.getString("ResultSet.Bad_format_for_BigDecimal", new Object[] { stringVal, Constants.integerValueOf(columnIndex) }), "S1009");
            }
          }
        }
        if ((this.fields[(columnIndex - 1)].getMysqlType() == 1) && (this.connection.getTinyInt1isBit()) && (this.fields[(columnIndex - 1)].getLength() == 1L)) {
          return new BigDecimal(stringVal.equalsIgnoreCase("true") ? 1.0D : 0.0D).setScale(scale);
        }
        throw new SQLException(Messages.getString("ResultSet.Bad_format_for_BigDecimal", new Object[] { stringVal, Constants.integerValueOf(columnIndex) }), "S1009");
      }
    }
    return null;
  }
  
  public InputStream getBinaryStream(int columnIndex)
    throws SQLException
  {
    checkRowPos();
    if (!this.isBinaryEncoded)
    {
      checkColumnBounds(columnIndex);
      
      int columnIndexMinusOne = columnIndex - 1;
      if (this.thisRow.isNull(columnIndexMinusOne))
      {
        this.wasNullFlag = true;
        
        return null;
      }
      this.wasNullFlag = false;
      
      return this.thisRow.getBinaryInputStream(columnIndexMinusOne);
    }
    return getNativeBinaryStream(columnIndex);
  }
  
  public InputStream getBinaryStream(String columnName)
    throws SQLException
  {
    return getBinaryStream(findColumn(columnName));
  }
  
  public java.sql.Blob getBlob(int columnIndex)
    throws SQLException
  {
    if (!this.isBinaryEncoded)
    {
      checkRowPos();
      
      checkColumnBounds(columnIndex);
      
      int columnIndexMinusOne = columnIndex - 1;
      if (this.thisRow.isNull(columnIndexMinusOne)) {
        this.wasNullFlag = true;
      } else {
        this.wasNullFlag = false;
      }
      if (this.wasNullFlag) {
        return null;
      }
      if (!this.connection.getEmulateLocators()) {
        return new Blob(this.thisRow.getColumnValue(columnIndexMinusOne), getExceptionInterceptor());
      }
      return new BlobFromLocator(this, columnIndex, getExceptionInterceptor());
    }
    return getNativeBlob(columnIndex);
  }
  
  public java.sql.Blob getBlob(String colName)
    throws SQLException
  {
    return getBlob(findColumn(colName));
  }
  
  public boolean getBoolean(int columnIndex)
    throws SQLException
  {
    checkColumnBounds(columnIndex);
    
    int columnIndexMinusOne = columnIndex - 1;
    
    Field field = this.fields[columnIndexMinusOne];
    if (field.getMysqlType() == 16) {
      return byteArrayToBoolean(columnIndexMinusOne);
    }
    this.wasNullFlag = false;
    
    int sqlType = field.getSQLType();
    switch (sqlType)
    {
    case 16: 
      if (field.getMysqlType() == -1)
      {
        String stringVal = getString(columnIndex);
        
        return getBooleanFromString(stringVal, columnIndex);
      }
      long boolVal = getLong(columnIndex, false);
      
      return (boolVal == -1L) || (boolVal > 0L);
    case -7: 
    case -6: 
    case -5: 
    case 2: 
    case 3: 
    case 4: 
    case 5: 
    case 6: 
    case 7: 
    case 8: 
      long boolVal = getLong(columnIndex, false);
      
      return (boolVal == -1L) || (boolVal > 0L);
    }
    if (this.connection.getPedantic()) {
      switch (sqlType)
      {
      case -4: 
      case -3: 
      case -2: 
      case 70: 
      case 91: 
      case 92: 
      case 93: 
      case 2000: 
      case 2002: 
      case 2003: 
      case 2004: 
      case 2005: 
      case 2006: 
        throw SQLError.createSQLException("Required type conversion not allowed", "22018", getExceptionInterceptor());
      }
    }
    if ((sqlType == -2) || (sqlType == -3) || (sqlType == -4) || (sqlType == 2004)) {
      return byteArrayToBoolean(columnIndexMinusOne);
    }
    if (this.useUsageAdvisor) {
      issueConversionViaParsingWarning("getBoolean()", columnIndex, this.thisRow.getColumnValue(columnIndexMinusOne), this.fields[columnIndex], new int[] { 16, 5, 1, 2, 3, 8, 4 });
    }
    String stringVal = getString(columnIndex);
    
    return getBooleanFromString(stringVal, columnIndex);
  }
  
  private boolean byteArrayToBoolean(int columnIndexMinusOne)
    throws SQLException
  {
    Object value = this.thisRow.getColumnValue(columnIndexMinusOne);
    if (value == null)
    {
      this.wasNullFlag = true;
      
      return false;
    }
    this.wasNullFlag = false;
    if (((byte[])value).length == 0) {
      return false;
    }
    byte boolVal = ((byte[])value)[0];
    if (boolVal == 49) {
      return true;
    }
    if (boolVal == 48) {
      return false;
    }
    return (boolVal == -1) || (boolVal > 0);
  }
  
  public boolean getBoolean(String columnName)
    throws SQLException
  {
    return getBoolean(findColumn(columnName));
  }
  
  private final boolean getBooleanFromString(String stringVal, int columnIndex)
    throws SQLException
  {
    if ((stringVal != null) && (stringVal.length() > 0))
    {
      int c = Character.toLowerCase(stringVal.charAt(0));
      
      return (c == 116) || (c == 121) || (c == 49) || (stringVal.equals("-1"));
    }
    return false;
  }
  
  public byte getByte(int columnIndex)
    throws SQLException
  {
    if (!this.isBinaryEncoded)
    {
      String stringVal = getString(columnIndex);
      if ((this.wasNullFlag) || (stringVal == null)) {
        return 0;
      }
      return getByteFromString(stringVal, columnIndex);
    }
    return getNativeByte(columnIndex);
  }
  
  public byte getByte(String columnName)
    throws SQLException
  {
    return getByte(findColumn(columnName));
  }
  
  private final byte getByteFromString(String stringVal, int columnIndex)
    throws SQLException
  {
    if ((stringVal != null) && (stringVal.length() == 0)) {
      return (byte)convertToZeroWithEmptyCheck();
    }
    if (stringVal == null) {
      return 0;
    }
    stringVal = stringVal.trim();
    try
    {
      int decimalIndex = stringVal.indexOf(".");
      if (decimalIndex != -1)
      {
        double valueAsDouble = Double.parseDouble(stringVal);
        if ((this.jdbcCompliantTruncationForReads) && (
          (valueAsDouble < -128.0D) || (valueAsDouble > 127.0D))) {
          throwRangeException(stringVal, columnIndex, -6);
        }
        return (byte)(int)valueAsDouble;
      }
      long valueAsLong = Long.parseLong(stringVal);
      if ((this.jdbcCompliantTruncationForReads) && (
        (valueAsLong < -128L) || (valueAsLong > 127L))) {
        throwRangeException(String.valueOf(valueAsLong), columnIndex, -6);
      }
      return (byte)(int)valueAsLong;
    }
    catch (NumberFormatException NFE)
    {
      throw SQLError.createSQLException(Messages.getString("ResultSet.Value____173") + stringVal + Messages.getString("ResultSet.___is_out_of_range_[-127,127]_174"), "S1009", getExceptionInterceptor());
    }
  }
  
  public byte[] getBytes(int columnIndex)
    throws SQLException
  {
    return getBytes(columnIndex, false);
  }
  
  protected byte[] getBytes(int columnIndex, boolean noConversion)
    throws SQLException
  {
    if (!this.isBinaryEncoded)
    {
      checkRowPos();
      
      checkColumnBounds(columnIndex);
      
      int columnIndexMinusOne = columnIndex - 1;
      if (this.thisRow.isNull(columnIndexMinusOne)) {
        this.wasNullFlag = true;
      } else {
        this.wasNullFlag = false;
      }
      if (this.wasNullFlag) {
        return null;
      }
      return this.thisRow.getColumnValue(columnIndexMinusOne);
    }
    return getNativeBytes(columnIndex, noConversion);
  }
  
  public byte[] getBytes(String columnName)
    throws SQLException
  {
    return getBytes(findColumn(columnName));
  }
  
  private final byte[] getBytesFromString(String stringVal, int columnIndex)
    throws SQLException
  {
    if (stringVal != null) {
      return StringUtils.getBytes(stringVal, this.connection.getEncoding(), this.connection.getServerCharacterEncoding(), this.connection.parserKnowsUnicode(), this.connection, getExceptionInterceptor());
    }
    return null;
  }
  
  protected Calendar getCalendarInstanceForSessionOrNew()
  {
    if (this.connection != null) {
      return this.connection.getCalendarInstanceForSessionOrNew();
    }
    return new GregorianCalendar();
  }
  
  public Reader getCharacterStream(int columnIndex)
    throws SQLException
  {
    if (!this.isBinaryEncoded)
    {
      checkColumnBounds(columnIndex);
      
      int columnIndexMinusOne = columnIndex - 1;
      if (this.thisRow.isNull(columnIndexMinusOne))
      {
        this.wasNullFlag = true;
        
        return null;
      }
      this.wasNullFlag = false;
      
      return this.thisRow.getReader(columnIndexMinusOne);
    }
    return getNativeCharacterStream(columnIndex);
  }
  
  public Reader getCharacterStream(String columnName)
    throws SQLException
  {
    return getCharacterStream(findColumn(columnName));
  }
  
  private final Reader getCharacterStreamFromString(String stringVal, int columnIndex)
    throws SQLException
  {
    if (stringVal != null) {
      return new StringReader(stringVal);
    }
    return null;
  }
  
  public java.sql.Clob getClob(int i)
    throws SQLException
  {
    if (!this.isBinaryEncoded)
    {
      String asString = getStringForClob(i);
      if (asString == null) {
        return null;
      }
      return new Clob(asString, getExceptionInterceptor());
    }
    return getNativeClob(i);
  }
  
  public java.sql.Clob getClob(String colName)
    throws SQLException
  {
    return getClob(findColumn(colName));
  }
  
  private final java.sql.Clob getClobFromString(String stringVal, int columnIndex)
    throws SQLException
  {
    return new Clob(stringVal, getExceptionInterceptor());
  }
  
  public int getConcurrency()
    throws SQLException
  {
    return 1007;
  }
  
  public String getCursorName()
    throws SQLException
  {
    throw SQLError.createSQLException(Messages.getString("ResultSet.Positioned_Update_not_supported"), "S1C00", getExceptionInterceptor());
  }
  
  public Date getDate(int columnIndex)
    throws SQLException
  {
    return getDate(columnIndex, null);
  }
  
  public Date getDate(int columnIndex, Calendar cal)
    throws SQLException
  {
    if (this.isBinaryEncoded) {
      return getNativeDate(columnIndex, cal);
    }
    if (!this.useFastDateParsing)
    {
      String stringVal = getStringInternal(columnIndex, false);
      if (stringVal == null) {
        return null;
      }
      return getDateFromString(stringVal, columnIndex, cal);
    }
    checkColumnBounds(columnIndex);
    
    int columnIndexMinusOne = columnIndex - 1;
    if (this.thisRow.isNull(columnIndexMinusOne))
    {
      this.wasNullFlag = true;
      
      return null;
    }
    this.wasNullFlag = false;
    
    return this.thisRow.getDateFast(columnIndexMinusOne, this.connection, this, cal);
  }
  
  public Date getDate(String columnName)
    throws SQLException
  {
    return getDate(findColumn(columnName));
  }
  
  public Date getDate(String columnName, Calendar cal)
    throws SQLException
  {
    return getDate(findColumn(columnName), cal);
  }
  
  private final Date getDateFromString(String stringVal, int columnIndex, Calendar targetCalendar)
    throws SQLException
  {
    int year = 0;
    int month = 0;
    int day = 0;
    try
    {
      this.wasNullFlag = false;
      if (stringVal == null)
      {
        this.wasNullFlag = true;
        
        return null;
      }
      stringVal = stringVal.trim();
      if ((stringVal.equals("0")) || (stringVal.equals("0000-00-00")) || (stringVal.equals("0000-00-00 00:00:00")) || (stringVal.equals("00000000000000")) || (stringVal.equals("0")))
      {
        if ("convertToNull".equals(this.connection.getZeroDateTimeBehavior()))
        {
          this.wasNullFlag = true;
          
          return null;
        }
        if ("exception".equals(this.connection.getZeroDateTimeBehavior())) {
          throw SQLError.createSQLException("Value '" + stringVal + "' can not be represented as java.sql.Date", "S1009", getExceptionInterceptor());
        }
        return fastDateCreate(targetCalendar, 1, 1, 1);
      }
      if (this.fields[(columnIndex - 1)].getMysqlType() == 7)
      {
        switch (stringVal.length())
        {
        case 19: 
        case 21: 
          year = Integer.parseInt(stringVal.substring(0, 4));
          month = Integer.parseInt(stringVal.substring(5, 7));
          day = Integer.parseInt(stringVal.substring(8, 10));
          
          return fastDateCreate(targetCalendar, year, month, day);
        case 8: 
        case 14: 
          year = Integer.parseInt(stringVal.substring(0, 4));
          month = Integer.parseInt(stringVal.substring(4, 6));
          day = Integer.parseInt(stringVal.substring(6, 8));
          
          return fastDateCreate(targetCalendar, year, month, day);
        case 6: 
        case 10: 
        case 12: 
          year = Integer.parseInt(stringVal.substring(0, 2));
          if (year <= 69) {
            year += 100;
          }
          month = Integer.parseInt(stringVal.substring(2, 4));
          day = Integer.parseInt(stringVal.substring(4, 6));
          
          return fastDateCreate(targetCalendar, year + 1900, month, day);
        case 4: 
          year = Integer.parseInt(stringVal.substring(0, 4));
          if (year <= 69) {
            year += 100;
          }
          month = Integer.parseInt(stringVal.substring(2, 4));
          
          return fastDateCreate(targetCalendar, year + 1900, month, 1);
        case 2: 
          year = Integer.parseInt(stringVal.substring(0, 2));
          if (year <= 69) {
            year += 100;
          }
          return fastDateCreate(targetCalendar, year + 1900, 1, 1);
        }
        throw SQLError.createSQLException(Messages.getString("ResultSet.Bad_format_for_Date", new Object[] { stringVal, Constants.integerValueOf(columnIndex) }), "S1009", getExceptionInterceptor());
      }
      if (this.fields[(columnIndex - 1)].getMysqlType() == 13)
      {
        if ((stringVal.length() == 2) || (stringVal.length() == 1))
        {
          year = Integer.parseInt(stringVal);
          if (year <= 69) {
            year += 100;
          }
          year += 1900;
        }
        else
        {
          year = Integer.parseInt(stringVal.substring(0, 4));
        }
        return fastDateCreate(targetCalendar, year, 1, 1);
      }
      if (this.fields[(columnIndex - 1)].getMysqlType() == 11) {
        return fastDateCreate(targetCalendar, 1970, 1, 1);
      }
      if (stringVal.length() < 10)
      {
        if (stringVal.length() == 8) {
          return fastDateCreate(targetCalendar, 1970, 1, 1);
        }
        throw SQLError.createSQLException(Messages.getString("ResultSet.Bad_format_for_Date", new Object[] { stringVal, Constants.integerValueOf(columnIndex) }), "S1009", getExceptionInterceptor());
      }
      if (stringVal.length() != 18)
      {
        year = Integer.parseInt(stringVal.substring(0, 4));
        month = Integer.parseInt(stringVal.substring(5, 7));
        day = Integer.parseInt(stringVal.substring(8, 10));
      }
      else
      {
        StringTokenizer st = new StringTokenizer(stringVal, "- ");
        
        year = Integer.parseInt(st.nextToken());
        month = Integer.parseInt(st.nextToken());
        day = Integer.parseInt(st.nextToken());
      }
      return fastDateCreate(targetCalendar, year, month, day);
    }
    catch (SQLException sqlEx)
    {
      throw sqlEx;
    }
    catch (Exception e)
    {
      SQLException sqlEx = SQLError.createSQLException(Messages.getString("ResultSet.Bad_format_for_Date", new Object[] { stringVal, Constants.integerValueOf(columnIndex) }), "S1009", getExceptionInterceptor());
      
      sqlEx.initCause(e);
      
      throw sqlEx;
    }
  }
  
  private TimeZone getDefaultTimeZone()
  {
    if ((!this.useLegacyDatetimeCode) && (this.connection != null)) {
      return this.serverTimeZoneTz;
    }
    return this.connection.getDefaultTimeZone();
  }
  
  public double getDouble(int columnIndex)
    throws SQLException
  {
    if (!this.isBinaryEncoded) {
      return getDoubleInternal(columnIndex);
    }
    return getNativeDouble(columnIndex);
  }
  
  public double getDouble(String columnName)
    throws SQLException
  {
    return getDouble(findColumn(columnName));
  }
  
  private final double getDoubleFromString(String stringVal, int columnIndex)
    throws SQLException
  {
    return getDoubleInternal(stringVal, columnIndex);
  }
  
  protected double getDoubleInternal(int colIndex)
    throws SQLException
  {
    return getDoubleInternal(getString(colIndex), colIndex);
  }
  
  protected double getDoubleInternal(String stringVal, int colIndex)
    throws SQLException
  {
    try
    {
      if (stringVal == null) {
        return 0.0D;
      }
      if (stringVal.length() == 0) {
        return convertToZeroWithEmptyCheck();
      }
      double d = Double.parseDouble(stringVal);
      if (this.useStrictFloatingPoint) {
        if (d == 2.147483648E9D) {
          d = 2.147483647E9D;
        } else if (d == 1.0000000036275E-15D) {
          d = 1.0E-15D;
        } else if (d == 9.999999869911E14D) {
          d = 9.99999999999999E14D;
        } else if (d == 1.4012984643248E-45D) {
          d = 1.4E-45D;
        } else if (d == 1.4013E-45D) {
          d = 1.4E-45D;
        } else if (d == 3.4028234663853E37D) {
          d = 3.4028235E37D;
        } else if (d == -2.14748E9D) {
          d = -2.147483648E9D;
        } else if (d != 3.40282E37D) {}
      }
      return 3.4028235E37D;
    }
    catch (NumberFormatException e)
    {
      if (this.fields[(colIndex - 1)].getMysqlType() == 16)
      {
        long valueAsLong = getNumericRepresentationOfSQLBitType(colIndex);
        
        return valueAsLong;
      }
      throw SQLError.createSQLException(Messages.getString("ResultSet.Bad_format_for_number", new Object[] { stringVal, Constants.integerValueOf(colIndex) }), "S1009", getExceptionInterceptor());
    }
  }
  
  public int getFetchDirection()
    throws SQLException
  {
    return this.fetchDirection;
  }
  
  public int getFetchSize()
    throws SQLException
  {
    return this.fetchSize;
  }
  
  public char getFirstCharOfQuery()
  {
    return this.firstCharOfQuery;
  }
  
  public float getFloat(int columnIndex)
    throws SQLException
  {
    if (!this.isBinaryEncoded)
    {
      String val = null;
      
      val = getString(columnIndex);
      
      return getFloatFromString(val, columnIndex);
    }
    return getNativeFloat(columnIndex);
  }
  
  public float getFloat(String columnName)
    throws SQLException
  {
    return getFloat(findColumn(columnName));
  }
  
  private final float getFloatFromString(String val, int columnIndex)
    throws SQLException
  {
    try
    {
      if (val != null)
      {
        if (val.length() == 0) {
          return convertToZeroWithEmptyCheck();
        }
        float f = Float.parseFloat(val);
        if ((this.jdbcCompliantTruncationForReads) && (
          (f == Float.MIN_VALUE) || (f == Float.MAX_VALUE)))
        {
          double valAsDouble = Double.parseDouble(val);
          if ((valAsDouble < 1.401298464324817E-45D - MIN_DIFF_PREC) || (valAsDouble > 3.4028234663852886E38D - MAX_DIFF_PREC)) {
            throwRangeException(String.valueOf(valAsDouble), columnIndex, 6);
          }
        }
        return f;
      }
      return 0.0F;
    }
    catch (NumberFormatException nfe)
    {
      try
      {
        Double valueAsDouble = new Double(val);
        float valueAsFloat = valueAsDouble.floatValue();
        if (this.jdbcCompliantTruncationForReads) {
          if (((this.jdbcCompliantTruncationForReads) && (valueAsFloat == Float.NEGATIVE_INFINITY)) || (valueAsFloat == Float.POSITIVE_INFINITY)) {
            throwRangeException(valueAsDouble.toString(), columnIndex, 6);
          }
        }
        return valueAsFloat;
      }
      catch (NumberFormatException newNfe)
      {
        throw SQLError.createSQLException(Messages.getString("ResultSet.Invalid_value_for_getFloat()_-____200") + val + Messages.getString("ResultSet.___in_column__201") + columnIndex, "S1009", getExceptionInterceptor());
      }
    }
  }
  
  public int getInt(int columnIndex)
    throws SQLException
  {
    checkRowPos();
    if (!this.isBinaryEncoded)
    {
      int columnIndexMinusOne = columnIndex - 1;
      if (this.useFastIntParsing)
      {
        checkColumnBounds(columnIndex);
        if (this.thisRow.isNull(columnIndexMinusOne)) {
          this.wasNullFlag = true;
        } else {
          this.wasNullFlag = false;
        }
        if (this.wasNullFlag) {
          return 0;
        }
        if (this.thisRow.length(columnIndexMinusOne) == 0L) {
          return convertToZeroWithEmptyCheck();
        }
        boolean needsFullParse = this.thisRow.isFloatingPointNumber(columnIndexMinusOne);
        if (!needsFullParse) {
          try
          {
            return getIntWithOverflowCheck(columnIndexMinusOne);
          }
          catch (NumberFormatException nfe)
          {
            try
            {
              return parseIntAsDouble(columnIndex, this.thisRow.getString(columnIndexMinusOne, this.fields[columnIndexMinusOne].getCharacterSet(), this.connection));
            }
            catch (NumberFormatException newNfe)
            {
              if (this.fields[columnIndexMinusOne].getMysqlType() == 16)
              {
                long valueAsLong = getNumericRepresentationOfSQLBitType(columnIndex);
                if ((this.connection.getJdbcCompliantTruncationForReads()) && ((valueAsLong < -2147483648L) || (valueAsLong > 2147483647L))) {
                  throwRangeException(String.valueOf(valueAsLong), columnIndex, 4);
                }
                return (int)valueAsLong;
              }
              throw SQLError.createSQLException(Messages.getString("ResultSet.Invalid_value_for_getInt()_-____74") + this.thisRow.getString(columnIndexMinusOne, this.fields[columnIndexMinusOne].getCharacterSet(), this.connection) + "'", "S1009", getExceptionInterceptor());
            }
          }
        }
      }
      String val = null;
      try
      {
        val = getString(columnIndex);
        if (val != null)
        {
          if (val.length() == 0) {
            return convertToZeroWithEmptyCheck();
          }
          if ((val.indexOf("e") == -1) && (val.indexOf("E") == -1) && (val.indexOf(".") == -1))
          {
            int intVal = Integer.parseInt(val);
            
            checkForIntegerTruncation(columnIndexMinusOne, null, intVal);
            
            return intVal;
          }
          int intVal = parseIntAsDouble(columnIndex, val);
          
          checkForIntegerTruncation(columnIndex, null, intVal);
          
          return intVal;
        }
        return 0;
      }
      catch (NumberFormatException nfe)
      {
        try
        {
          return parseIntAsDouble(columnIndex, val);
        }
        catch (NumberFormatException newNfe)
        {
          if (this.fields[columnIndexMinusOne].getMysqlType() == 16)
          {
            long valueAsLong = getNumericRepresentationOfSQLBitType(columnIndex);
            if ((this.jdbcCompliantTruncationForReads) && ((valueAsLong < -2147483648L) || (valueAsLong > 2147483647L))) {
              throwRangeException(String.valueOf(valueAsLong), columnIndex, 4);
            }
            return (int)valueAsLong;
          }
          throw SQLError.createSQLException(Messages.getString("ResultSet.Invalid_value_for_getInt()_-____74") + val + "'", "S1009", getExceptionInterceptor());
        }
      }
    }
    return getNativeInt(columnIndex);
  }
  
  public int getInt(String columnName)
    throws SQLException
  {
    return getInt(findColumn(columnName));
  }
  
  private final int getIntFromString(String val, int columnIndex)
    throws SQLException
  {
    try
    {
      if (val != null)
      {
        if (val.length() == 0) {
          return convertToZeroWithEmptyCheck();
        }
        if ((val.indexOf("e") == -1) && (val.indexOf("E") == -1) && (val.indexOf(".") == -1))
        {
          val = val.trim();
          
          int valueAsInt = Integer.parseInt(val);
          if ((this.jdbcCompliantTruncationForReads) && (
            (valueAsInt == Integer.MIN_VALUE) || (valueAsInt == Integer.MAX_VALUE)))
          {
            long valueAsLong = Long.parseLong(val);
            if ((valueAsLong < -2147483648L) || (valueAsLong > 2147483647L)) {
              throwRangeException(String.valueOf(valueAsLong), columnIndex, 4);
            }
          }
          return valueAsInt;
        }
        double valueAsDouble = Double.parseDouble(val);
        if ((this.jdbcCompliantTruncationForReads) && (
          (valueAsDouble < -2.147483648E9D) || (valueAsDouble > 2.147483647E9D))) {
          throwRangeException(String.valueOf(valueAsDouble), columnIndex, 4);
        }
        return (int)valueAsDouble;
      }
      return 0;
    }
    catch (NumberFormatException nfe)
    {
      try
      {
        double valueAsDouble = Double.parseDouble(val);
        if ((this.jdbcCompliantTruncationForReads) && (
          (valueAsDouble < -2.147483648E9D) || (valueAsDouble > 2.147483647E9D))) {
          throwRangeException(String.valueOf(valueAsDouble), columnIndex, 4);
        }
        return (int)valueAsDouble;
      }
      catch (NumberFormatException newNfe)
      {
        throw SQLError.createSQLException(Messages.getString("ResultSet.Invalid_value_for_getInt()_-____206") + val + Messages.getString("ResultSet.___in_column__207") + columnIndex, "S1009", getExceptionInterceptor());
      }
    }
  }
  
  public long getLong(int columnIndex)
    throws SQLException
  {
    return getLong(columnIndex, true);
  }
  
  private long getLong(int columnIndex, boolean overflowCheck)
    throws SQLException
  {
    if (!this.isBinaryEncoded)
    {
      checkRowPos();
      
      int columnIndexMinusOne = columnIndex - 1;
      if (this.useFastIntParsing)
      {
        checkColumnBounds(columnIndex);
        if (this.thisRow.isNull(columnIndexMinusOne)) {
          this.wasNullFlag = true;
        } else {
          this.wasNullFlag = false;
        }
        if (this.wasNullFlag) {
          return 0L;
        }
        if (this.thisRow.length(columnIndexMinusOne) == 0L) {
          return convertToZeroWithEmptyCheck();
        }
        boolean needsFullParse = this.thisRow.isFloatingPointNumber(columnIndexMinusOne);
        if (!needsFullParse) {
          try
          {
            return getLongWithOverflowCheck(columnIndexMinusOne, overflowCheck);
          }
          catch (NumberFormatException nfe)
          {
            try
            {
              return parseLongAsDouble(columnIndexMinusOne, this.thisRow.getString(columnIndexMinusOne, this.fields[columnIndexMinusOne].getCharacterSet(), this.connection));
            }
            catch (NumberFormatException newNfe)
            {
              if (this.fields[columnIndexMinusOne].getMysqlType() == 16) {
                return getNumericRepresentationOfSQLBitType(columnIndex);
              }
              throw SQLError.createSQLException(Messages.getString("ResultSet.Invalid_value_for_getLong()_-____79") + this.thisRow.getString(columnIndexMinusOne, this.fields[columnIndexMinusOne].getCharacterSet(), this.connection) + "'", "S1009", getExceptionInterceptor());
            }
          }
        }
      }
      String val = null;
      try
      {
        val = getString(columnIndex);
        if (val != null)
        {
          if (val.length() == 0) {
            return convertToZeroWithEmptyCheck();
          }
          if ((val.indexOf("e") == -1) && (val.indexOf("E") == -1)) {
            return parseLongWithOverflowCheck(columnIndexMinusOne, null, val, overflowCheck);
          }
          return parseLongAsDouble(columnIndexMinusOne, val);
        }
        return 0L;
      }
      catch (NumberFormatException nfe)
      {
        try
        {
          return parseLongAsDouble(columnIndexMinusOne, val);
        }
        catch (NumberFormatException newNfe)
        {
          throw SQLError.createSQLException(Messages.getString("ResultSet.Invalid_value_for_getLong()_-____79") + val + "'", "S1009", getExceptionInterceptor());
        }
      }
    }
    return getNativeLong(columnIndex, overflowCheck, true);
  }
  
  public long getLong(String columnName)
    throws SQLException
  {
    return getLong(findColumn(columnName));
  }
  
  private final long getLongFromString(String val, int columnIndexZeroBased)
    throws SQLException
  {
    try
    {
      if (val != null)
      {
        if (val.length() == 0) {
          return convertToZeroWithEmptyCheck();
        }
        if ((val.indexOf("e") == -1) && (val.indexOf("E") == -1)) {
          return parseLongWithOverflowCheck(columnIndexZeroBased, null, val, true);
        }
        return parseLongAsDouble(columnIndexZeroBased, val);
      }
      return 0L;
    }
    catch (NumberFormatException nfe)
    {
      try
      {
        return parseLongAsDouble(columnIndexZeroBased, val);
      }
      catch (NumberFormatException newNfe)
      {
        throw SQLError.createSQLException(Messages.getString("ResultSet.Invalid_value_for_getLong()_-____211") + val + Messages.getString("ResultSet.___in_column__212") + (columnIndexZeroBased + 1), "S1009", getExceptionInterceptor());
      }
    }
  }
  
  public java.sql.ResultSetMetaData getMetaData()
    throws SQLException
  {
    checkClosed();
    
    return new ResultSetMetaData(this.fields, this.connection.getUseOldAliasMetadataBehavior(), getExceptionInterceptor());
  }
  
  protected Array getNativeArray(int i)
    throws SQLException
  {
    throw SQLError.notImplemented();
  }
  
  protected InputStream getNativeAsciiStream(int columnIndex)
    throws SQLException
  {
    checkRowPos();
    
    return getNativeBinaryStream(columnIndex);
  }
  
  protected BigDecimal getNativeBigDecimal(int columnIndex)
    throws SQLException
  {
    checkColumnBounds(columnIndex);
    
    int scale = this.fields[(columnIndex - 1)].getDecimals();
    
    return getNativeBigDecimal(columnIndex, scale);
  }
  
  protected BigDecimal getNativeBigDecimal(int columnIndex, int scale)
    throws SQLException
  {
    checkColumnBounds(columnIndex);
    
    String stringVal = null;
    
    Field f = this.fields[(columnIndex - 1)];
    
    Object value = this.thisRow.getColumnValue(columnIndex - 1);
    if (value == null)
    {
      this.wasNullFlag = true;
      
      return null;
    }
    this.wasNullFlag = false;
    switch (f.getSQLType())
    {
    case 2: 
    case 3: 
      stringVal = StringUtils.toAsciiString((byte[])value);
      
      break;
    default: 
      stringVal = getNativeString(columnIndex);
    }
    return getBigDecimalFromString(stringVal, columnIndex, scale);
  }
  
  protected InputStream getNativeBinaryStream(int columnIndex)
    throws SQLException
  {
    checkRowPos();
    
    int columnIndexMinusOne = columnIndex - 1;
    if (this.thisRow.isNull(columnIndexMinusOne))
    {
      this.wasNullFlag = true;
      
      return null;
    }
    this.wasNullFlag = false;
    switch (this.fields[columnIndexMinusOne].getSQLType())
    {
    case -7: 
    case -4: 
    case -3: 
    case -2: 
    case 2004: 
      return this.thisRow.getBinaryInputStream(columnIndexMinusOne);
    }
    byte[] b = getNativeBytes(columnIndex, false);
    if (b != null) {
      return new ByteArrayInputStream(b);
    }
    return null;
  }
  
  protected java.sql.Blob getNativeBlob(int columnIndex)
    throws SQLException
  {
    checkRowPos();
    
    checkColumnBounds(columnIndex);
    
    Object value = this.thisRow.getColumnValue(columnIndex - 1);
    if (value == null) {
      this.wasNullFlag = true;
    } else {
      this.wasNullFlag = false;
    }
    if (this.wasNullFlag) {
      return null;
    }
    int mysqlType = this.fields[(columnIndex - 1)].getMysqlType();
    
    byte[] dataAsBytes = null;
    switch (mysqlType)
    {
    case 249: 
    case 250: 
    case 251: 
    case 252: 
      dataAsBytes = (byte[])value;
      break;
    default: 
      dataAsBytes = getNativeBytes(columnIndex, false);
    }
    if (!this.connection.getEmulateLocators()) {
      return new Blob(dataAsBytes, getExceptionInterceptor());
    }
    return new BlobFromLocator(this, columnIndex, getExceptionInterceptor());
  }
  
  public static boolean arraysEqual(byte[] left, byte[] right)
  {
    if (left == null) {
      return right == null;
    }
    if (right == null) {
      return false;
    }
    if (left.length != right.length) {
      return false;
    }
    for (int i = 0; i < left.length; i++) {
      if (left[i] != right[i]) {
        return false;
      }
    }
    return true;
  }
  
  protected byte getNativeByte(int columnIndex)
    throws SQLException
  {
    return getNativeByte(columnIndex, true);
  }
  
  protected byte getNativeByte(int columnIndex, boolean overflowCheck)
    throws SQLException
  {
    checkRowPos();
    
    checkColumnBounds(columnIndex);
    
    Object value = this.thisRow.getColumnValue(columnIndex - 1);
    if (value == null)
    {
      this.wasNullFlag = true;
      
      return 0;
    }
    if (value == null) {
      this.wasNullFlag = true;
    } else {
      this.wasNullFlag = false;
    }
    if (this.wasNullFlag) {
      return 0;
    }
    columnIndex--;
    
    Field field = this.fields[columnIndex];
    switch (field.getMysqlType())
    {
    case 16: 
      long valueAsLong = getNumericRepresentationOfSQLBitType(columnIndex + 1);
      if ((overflowCheck) && (this.jdbcCompliantTruncationForReads) && ((valueAsLong < -128L) || (valueAsLong > 127L))) {
        throwRangeException(String.valueOf(valueAsLong), columnIndex + 1, -6);
      }
      return (byte)(int)valueAsLong;
    case 1: 
      byte valueAsByte = ((byte[])value)[0];
      if (!field.isUnsigned()) {
        return valueAsByte;
      }
      short valueAsShort = valueAsByte >= 0 ? (short)valueAsByte : (short)(valueAsByte + 256);
      if ((overflowCheck) && (this.jdbcCompliantTruncationForReads) && 
        (valueAsShort > 127)) {
        throwRangeException(String.valueOf(valueAsShort), columnIndex + 1, -6);
      }
      return (byte)valueAsShort;
    case 2: 
    case 13: 
      short valueAsShort = getNativeShort(columnIndex + 1);
      if ((overflowCheck) && (this.jdbcCompliantTruncationForReads) && (
        (valueAsShort < -128) || (valueAsShort > 127))) {
        throwRangeException(String.valueOf(valueAsShort), columnIndex + 1, -6);
      }
      return (byte)valueAsShort;
    case 3: 
    case 9: 
      int valueAsInt = getNativeInt(columnIndex + 1, false);
      if ((overflowCheck) && (this.jdbcCompliantTruncationForReads) && (
        (valueAsInt < -128) || (valueAsInt > 127))) {
        throwRangeException(String.valueOf(valueAsInt), columnIndex + 1, -6);
      }
      return (byte)valueAsInt;
    case 4: 
      float valueAsFloat = getNativeFloat(columnIndex + 1);
      if ((overflowCheck) && (this.jdbcCompliantTruncationForReads) && (
        (valueAsFloat < -128.0F) || (valueAsFloat > 127.0F))) {
        throwRangeException(String.valueOf(valueAsFloat), columnIndex + 1, -6);
      }
      return (byte)(int)valueAsFloat;
    case 5: 
      double valueAsDouble = getNativeDouble(columnIndex + 1);
      if ((overflowCheck) && (this.jdbcCompliantTruncationForReads) && (
        (valueAsDouble < -128.0D) || (valueAsDouble > 127.0D))) {
        throwRangeException(String.valueOf(valueAsDouble), columnIndex + 1, -6);
      }
      return (byte)(int)valueAsDouble;
    case 8: 
      long valueAsLong = getNativeLong(columnIndex + 1, false, true);
      if ((overflowCheck) && (this.jdbcCompliantTruncationForReads) && (
        (valueAsLong < -128L) || (valueAsLong > 127L))) {
        throwRangeException(String.valueOf(valueAsLong), columnIndex + 1, -6);
      }
      return (byte)(int)valueAsLong;
    }
    if (this.useUsageAdvisor) {
      issueConversionViaParsingWarning("getByte()", columnIndex, this.thisRow.getColumnValue(columnIndex - 1), this.fields[columnIndex], new int[] { 5, 1, 2, 3, 8, 4 });
    }
    return getByteFromString(getNativeString(columnIndex + 1), columnIndex + 1);
  }
  
  protected byte[] getNativeBytes(int columnIndex, boolean noConversion)
    throws SQLException
  {
    checkRowPos();
    
    checkColumnBounds(columnIndex);
    
    Object value = this.thisRow.getColumnValue(columnIndex - 1);
    if (value == null) {
      this.wasNullFlag = true;
    } else {
      this.wasNullFlag = false;
    }
    if (this.wasNullFlag) {
      return null;
    }
    Field field = this.fields[(columnIndex - 1)];
    
    int mysqlType = field.getMysqlType();
    if (noConversion) {
      mysqlType = 252;
    }
    switch (mysqlType)
    {
    case 16: 
    case 249: 
    case 250: 
    case 251: 
    case 252: 
      return (byte[])value;
    case 15: 
    case 253: 
    case 254: 
      if ((value instanceof byte[])) {
        return (byte[])value;
      }
      break;
    }
    int sqlType = field.getSQLType();
    if ((sqlType == -3) || (sqlType == -2)) {
      return (byte[])value;
    }
    return getBytesFromString(getNativeString(columnIndex), columnIndex);
  }
  
  protected Reader getNativeCharacterStream(int columnIndex)
    throws SQLException
  {
    int columnIndexMinusOne = columnIndex - 1;
    switch (this.fields[columnIndexMinusOne].getSQLType())
    {
    case -1: 
    case 1: 
    case 12: 
    case 2005: 
      if (this.thisRow.isNull(columnIndexMinusOne))
      {
        this.wasNullFlag = true;
        
        return null;
      }
      this.wasNullFlag = false;
      
      return this.thisRow.getReader(columnIndexMinusOne);
    }
    String asString = null;
    
    asString = getStringForClob(columnIndex);
    if (asString == null) {
      return null;
    }
    return getCharacterStreamFromString(asString, columnIndex);
  }
  
  protected java.sql.Clob getNativeClob(int columnIndex)
    throws SQLException
  {
    String stringVal = getStringForClob(columnIndex);
    if (stringVal == null) {
      return null;
    }
    return getClobFromString(stringVal, columnIndex);
  }
  
  private String getNativeConvertToString(int columnIndex, Field field)
    throws SQLException
  {
    int sqlType = field.getSQLType();
    int mysqlType = field.getMysqlType();
    switch (sqlType)
    {
    case -7: 
      return String.valueOf(getNumericRepresentationOfSQLBitType(columnIndex));
    case 16: 
      boolean booleanVal = getBoolean(columnIndex);
      if (this.wasNullFlag) {
        return null;
      }
      return String.valueOf(booleanVal);
    case -6: 
      byte tinyintVal = getNativeByte(columnIndex, false);
      if (this.wasNullFlag) {
        return null;
      }
      if ((!field.isUnsigned()) || (tinyintVal >= 0)) {
        return String.valueOf(tinyintVal);
      }
      short unsignedTinyVal = (short)(tinyintVal & 0xFF);
      
      return String.valueOf(unsignedTinyVal);
    case 5: 
      int intVal = getNativeInt(columnIndex, false);
      if (this.wasNullFlag) {
        return null;
      }
      if ((!field.isUnsigned()) || (intVal >= 0)) {
        return String.valueOf(intVal);
      }
      intVal &= 0xFFFF;
      
      return String.valueOf(intVal);
    case 4: 
      int intVal = getNativeInt(columnIndex, false);
      if (this.wasNullFlag) {
        return null;
      }
      if ((!field.isUnsigned()) || (intVal >= 0) || (field.getMysqlType() == 9)) {
        return String.valueOf(intVal);
      }
      long longVal = intVal & 0xFFFFFFFF;
      
      return String.valueOf(longVal);
    case -5: 
      if (!field.isUnsigned())
      {
        long longVal = getNativeLong(columnIndex, false, true);
        if (this.wasNullFlag) {
          return null;
        }
        return String.valueOf(longVal);
      }
      long longVal = getNativeLong(columnIndex, false, false);
      if (this.wasNullFlag) {
        return null;
      }
      return String.valueOf(convertLongToUlong(longVal));
    case 7: 
      float floatVal = getNativeFloat(columnIndex);
      if (this.wasNullFlag) {
        return null;
      }
      return String.valueOf(floatVal);
    case 6: 
    case 8: 
      double doubleVal = getNativeDouble(columnIndex);
      if (this.wasNullFlag) {
        return null;
      }
      return String.valueOf(doubleVal);
    case 2: 
    case 3: 
      String stringVal = StringUtils.toAsciiString((byte[])this.thisRow.getColumnValue(columnIndex - 1));
      if (stringVal != null)
      {
        this.wasNullFlag = false;
        if (stringVal.length() == 0)
        {
          BigDecimal val = new BigDecimal(0.0D);
          
          return val.toString();
        }
        try
        {
          val = new BigDecimal(stringVal);
        }
        catch (NumberFormatException ex)
        {
          BigDecimal val;
          throw SQLError.createSQLException(Messages.getString("ResultSet.Bad_format_for_BigDecimal", new Object[] { stringVal, Constants.integerValueOf(columnIndex) }), "S1009", getExceptionInterceptor());
        }
        BigDecimal val;
        return val.toString();
      }
      this.wasNullFlag = true;
      
      return null;
    case -1: 
    case 1: 
    case 12: 
      return extractStringFromNativeColumn(columnIndex, mysqlType);
    case -4: 
    case -3: 
    case -2: 
      if (!field.isBlob()) {
        return extractStringFromNativeColumn(columnIndex, mysqlType);
      }
      if (!field.isBinary()) {
        return extractStringFromNativeColumn(columnIndex, mysqlType);
      }
      byte[] data = getBytes(columnIndex);
      Object obj = data;
      if ((data != null) && (data.length >= 2))
      {
        if ((data[0] == -84) && (data[1] == -19)) {
          try
          {
            ByteArrayInputStream bytesIn = new ByteArrayInputStream(data);
            
            ObjectInputStream objIn = new ObjectInputStream(bytesIn);
            
            obj = objIn.readObject();
            objIn.close();
            bytesIn.close();
          }
          catch (ClassNotFoundException cnfe)
          {
            throw SQLError.createSQLException(Messages.getString("ResultSet.Class_not_found___91") + cnfe.toString() + Messages.getString("ResultSet._while_reading_serialized_object_92"), getExceptionInterceptor());
          }
          catch (IOException ex)
          {
            obj = data;
          }
        }
        return obj.toString();
      }
      return extractStringFromNativeColumn(columnIndex, mysqlType);
    case 91: 
      if (mysqlType == 13)
      {
        short shortVal = getNativeShort(columnIndex);
        if (!this.connection.getYearIsDateType())
        {
          if (this.wasNullFlag) {
            return null;
          }
          return String.valueOf(shortVal);
        }
        if (field.getLength() == 2L)
        {
          if (shortVal <= 69) {
            shortVal = (short)(shortVal + 100);
          }
          shortVal = (short)(shortVal + 1900);
        }
        return fastDateCreate(null, shortVal, 1, 1).toString();
      }
      Date dt = getNativeDate(columnIndex);
      if (dt == null) {
        return null;
      }
      return String.valueOf(dt);
    case 92: 
      Time tm = getNativeTime(columnIndex, null, this.defaultTimeZone, false);
      if (tm == null) {
        return null;
      }
      return String.valueOf(tm);
    case 93: 
      Timestamp tstamp = getNativeTimestamp(columnIndex, null, this.defaultTimeZone, false);
      if (tstamp == null) {
        return null;
      }
      String result = String.valueOf(tstamp);
      if (!this.connection.getNoDatetimeStringSync()) {
        return result;
      }
      if (result.endsWith(".0")) {
        return result.substring(0, result.length() - 2);
      }
      break;
    }
    return extractStringFromNativeColumn(columnIndex, mysqlType);
  }
  
  protected Date getNativeDate(int columnIndex)
    throws SQLException
  {
    return getNativeDate(columnIndex, null);
  }
  
  protected Date getNativeDate(int columnIndex, Calendar cal)
    throws SQLException
  {
    checkRowPos();
    checkColumnBounds(columnIndex);
    
    int columnIndexMinusOne = columnIndex - 1;
    
    int mysqlType = this.fields[columnIndexMinusOne].getMysqlType();
    
    Date dateToReturn = null;
    if (mysqlType == 10)
    {
      dateToReturn = this.thisRow.getNativeDate(columnIndexMinusOne, this.connection, this, cal);
    }
    else
    {
      TimeZone tz = cal != null ? cal.getTimeZone() : getDefaultTimeZone();
      
      boolean rollForward = (tz != null) && (!tz.equals(getDefaultTimeZone()));
      
      dateToReturn = (Date)this.thisRow.getNativeDateTimeValue(columnIndexMinusOne, null, 91, mysqlType, tz, rollForward, this.connection, this);
    }
    if (dateToReturn == null)
    {
      this.wasNullFlag = true;
      
      return null;
    }
    this.wasNullFlag = false;
    
    return dateToReturn;
  }
  
  Date getNativeDateViaParseConversion(int columnIndex)
    throws SQLException
  {
    if (this.useUsageAdvisor) {
      issueConversionViaParsingWarning("getDate()", columnIndex, this.thisRow.getColumnValue(columnIndex - 1), this.fields[(columnIndex - 1)], new int[] { 10 });
    }
    String stringVal = getNativeString(columnIndex);
    
    return getDateFromString(stringVal, columnIndex, null);
  }
  
  protected double getNativeDouble(int columnIndex)
    throws SQLException
  {
    checkRowPos();
    checkColumnBounds(columnIndex);
    
    columnIndex--;
    if (this.thisRow.isNull(columnIndex))
    {
      this.wasNullFlag = true;
      
      return 0.0D;
    }
    this.wasNullFlag = false;
    
    Field f = this.fields[columnIndex];
    switch (f.getMysqlType())
    {
    case 5: 
      return this.thisRow.getNativeDouble(columnIndex);
    case 1: 
      if (!f.isUnsigned()) {
        return getNativeByte(columnIndex + 1);
      }
      return getNativeShort(columnIndex + 1);
    case 2: 
    case 13: 
      if (!f.isUnsigned()) {
        return getNativeShort(columnIndex + 1);
      }
      return getNativeInt(columnIndex + 1);
    case 3: 
    case 9: 
      if (!f.isUnsigned()) {
        return getNativeInt(columnIndex + 1);
      }
      return getNativeLong(columnIndex + 1);
    case 8: 
      long valueAsLong = getNativeLong(columnIndex + 1);
      if (!f.isUnsigned()) {
        return valueAsLong;
      }
      BigInteger asBigInt = convertLongToUlong(valueAsLong);
      
      return asBigInt.doubleValue();
    case 4: 
      return getNativeFloat(columnIndex + 1);
    case 16: 
      return getNumericRepresentationOfSQLBitType(columnIndex + 1);
    }
    String stringVal = getNativeString(columnIndex + 1);
    if (this.useUsageAdvisor) {
      issueConversionViaParsingWarning("getDouble()", columnIndex, stringVal, this.fields[columnIndex], new int[] { 5, 1, 2, 3, 8, 4 });
    }
    return getDoubleFromString(stringVal, columnIndex + 1);
  }
  
  protected float getNativeFloat(int columnIndex)
    throws SQLException
  {
    checkRowPos();
    checkColumnBounds(columnIndex);
    
    columnIndex--;
    if (this.thisRow.isNull(columnIndex))
    {
      this.wasNullFlag = true;
      
      return 0.0F;
    }
    this.wasNullFlag = false;
    
    Field f = this.fields[columnIndex];
    switch (f.getMysqlType())
    {
    case 16: 
      long valueAsLong = getNumericRepresentationOfSQLBitType(columnIndex + 1);
      
      return (float)valueAsLong;
    case 5: 
      Double valueAsDouble = new Double(getNativeDouble(columnIndex + 1));
      
      float valueAsFloat = valueAsDouble.floatValue();
      if (((this.jdbcCompliantTruncationForReads) && (valueAsFloat == Float.NEGATIVE_INFINITY)) || (valueAsFloat == Float.POSITIVE_INFINITY)) {
        throwRangeException(valueAsDouble.toString(), columnIndex + 1, 6);
      }
      return (float)getNativeDouble(columnIndex + 1);
    case 1: 
      if (!f.isUnsigned()) {
        return getNativeByte(columnIndex + 1);
      }
      return getNativeShort(columnIndex + 1);
    case 2: 
    case 13: 
      if (!f.isUnsigned()) {
        return getNativeShort(columnIndex + 1);
      }
      return getNativeInt(columnIndex + 1);
    case 3: 
    case 9: 
      if (!f.isUnsigned()) {
        return getNativeInt(columnIndex + 1);
      }
      return (float)getNativeLong(columnIndex + 1);
    case 8: 
      long valueAsLong = getNativeLong(columnIndex + 1);
      if (!f.isUnsigned()) {
        return (float)valueAsLong;
      }
      BigInteger asBigInt = convertLongToUlong(valueAsLong);
      
      return asBigInt.floatValue();
    case 4: 
      return this.thisRow.getNativeFloat(columnIndex);
    }
    String stringVal = getNativeString(columnIndex + 1);
    if (this.useUsageAdvisor) {
      issueConversionViaParsingWarning("getFloat()", columnIndex, stringVal, this.fields[columnIndex], new int[] { 5, 1, 2, 3, 8, 4 });
    }
    return getFloatFromString(stringVal, columnIndex + 1);
  }
  
  protected int getNativeInt(int columnIndex)
    throws SQLException
  {
    return getNativeInt(columnIndex, true);
  }
  
  protected int getNativeInt(int columnIndex, boolean overflowCheck)
    throws SQLException
  {
    checkRowPos();
    checkColumnBounds(columnIndex);
    
    columnIndex--;
    if (this.thisRow.isNull(columnIndex))
    {
      this.wasNullFlag = true;
      
      return 0;
    }
    this.wasNullFlag = false;
    
    Field f = this.fields[columnIndex];
    switch (f.getMysqlType())
    {
    case 16: 
      long valueAsLong = getNumericRepresentationOfSQLBitType(columnIndex + 1);
      if ((overflowCheck) && (this.jdbcCompliantTruncationForReads) && ((valueAsLong < -2147483648L) || (valueAsLong > 2147483647L))) {
        throwRangeException(String.valueOf(valueAsLong), columnIndex + 1, 4);
      }
      return (short)(int)valueAsLong;
    case 1: 
      byte tinyintVal = getNativeByte(columnIndex + 1, false);
      if ((!f.isUnsigned()) || (tinyintVal >= 0)) {
        return tinyintVal;
      }
      return tinyintVal + 256;
    case 2: 
    case 13: 
      short asShort = getNativeShort(columnIndex + 1, false);
      if ((!f.isUnsigned()) || (asShort >= 0)) {
        return asShort;
      }
      return asShort + 65536;
    case 3: 
    case 9: 
      int valueAsInt = this.thisRow.getNativeInt(columnIndex);
      if (!f.isUnsigned()) {
        return valueAsInt;
      }
      long valueAsLong = valueAsInt >= 0 ? valueAsInt : valueAsInt + 4294967296L;
      if ((overflowCheck) && (this.jdbcCompliantTruncationForReads) && (valueAsLong > 2147483647L)) {
        throwRangeException(String.valueOf(valueAsLong), columnIndex + 1, 4);
      }
      return (int)valueAsLong;
    case 8: 
      long valueAsLong = getNativeLong(columnIndex + 1, false, true);
      if ((overflowCheck) && (this.jdbcCompliantTruncationForReads) && (
        (valueAsLong < -2147483648L) || (valueAsLong > 2147483647L))) {
        throwRangeException(String.valueOf(valueAsLong), columnIndex + 1, 4);
      }
      return (int)valueAsLong;
    case 5: 
      double valueAsDouble = getNativeDouble(columnIndex + 1);
      if ((overflowCheck) && (this.jdbcCompliantTruncationForReads) && (
        (valueAsDouble < -2.147483648E9D) || (valueAsDouble > 2.147483647E9D))) {
        throwRangeException(String.valueOf(valueAsDouble), columnIndex + 1, 4);
      }
      return (int)valueAsDouble;
    case 4: 
      double valueAsDouble = getNativeFloat(columnIndex + 1);
      if ((overflowCheck) && (this.jdbcCompliantTruncationForReads) && (
        (valueAsDouble < -2.147483648E9D) || (valueAsDouble > 2.147483647E9D))) {
        throwRangeException(String.valueOf(valueAsDouble), columnIndex + 1, 4);
      }
      return (int)valueAsDouble;
    }
    String stringVal = getNativeString(columnIndex + 1);
    if (this.useUsageAdvisor) {
      issueConversionViaParsingWarning("getInt()", columnIndex, stringVal, this.fields[columnIndex], new int[] { 5, 1, 2, 3, 8, 4 });
    }
    return getIntFromString(stringVal, columnIndex + 1);
  }
  
  protected long getNativeLong(int columnIndex)
    throws SQLException
  {
    return getNativeLong(columnIndex, true, true);
  }
  
  protected long getNativeLong(int columnIndex, boolean overflowCheck, boolean expandUnsignedLong)
    throws SQLException
  {
    checkRowPos();
    checkColumnBounds(columnIndex);
    
    columnIndex--;
    if (this.thisRow.isNull(columnIndex))
    {
      this.wasNullFlag = true;
      
      return 0L;
    }
    this.wasNullFlag = false;
    
    Field f = this.fields[columnIndex];
    switch (f.getMysqlType())
    {
    case 16: 
      return getNumericRepresentationOfSQLBitType(columnIndex + 1);
    case 1: 
      if (!f.isUnsigned()) {
        return getNativeByte(columnIndex + 1);
      }
      return getNativeInt(columnIndex + 1);
    case 2: 
      if (!f.isUnsigned()) {
        return getNativeShort(columnIndex + 1);
      }
      return getNativeInt(columnIndex + 1, false);
    case 13: 
      return getNativeShort(columnIndex + 1);
    case 3: 
    case 9: 
      int asInt = getNativeInt(columnIndex + 1, false);
      if ((!f.isUnsigned()) || (asInt >= 0)) {
        return asInt;
      }
      return asInt + 4294967296L;
    case 8: 
      long valueAsLong = this.thisRow.getNativeLong(columnIndex);
      if ((!f.isUnsigned()) || (!expandUnsignedLong)) {
        return valueAsLong;
      }
      BigInteger asBigInt = convertLongToUlong(valueAsLong);
      if ((overflowCheck) && (this.jdbcCompliantTruncationForReads) && ((asBigInt.compareTo(new BigInteger(String.valueOf(Long.MAX_VALUE))) > 0) || (asBigInt.compareTo(new BigInteger(String.valueOf(Long.MIN_VALUE))) < 0))) {
        throwRangeException(asBigInt.toString(), columnIndex + 1, -5);
      }
      return getLongFromString(asBigInt.toString(), columnIndex);
    case 5: 
      double valueAsDouble = getNativeDouble(columnIndex + 1);
      if ((overflowCheck) && (this.jdbcCompliantTruncationForReads) && (
        (valueAsDouble < -9.223372036854776E18D) || (valueAsDouble > 9.223372036854776E18D))) {
        throwRangeException(String.valueOf(valueAsDouble), columnIndex + 1, -5);
      }
      return valueAsDouble;
    case 4: 
      double valueAsDouble = getNativeFloat(columnIndex + 1);
      if ((overflowCheck) && (this.jdbcCompliantTruncationForReads) && (
        (valueAsDouble < -9.223372036854776E18D) || (valueAsDouble > 9.223372036854776E18D))) {
        throwRangeException(String.valueOf(valueAsDouble), columnIndex + 1, -5);
      }
      return valueAsDouble;
    }
    String stringVal = getNativeString(columnIndex + 1);
    if (this.useUsageAdvisor) {
      issueConversionViaParsingWarning("getLong()", columnIndex, stringVal, this.fields[columnIndex], new int[] { 5, 1, 2, 3, 8, 4 });
    }
    return getLongFromString(stringVal, columnIndex + 1);
  }
  
  protected Ref getNativeRef(int i)
    throws SQLException
  {
    throw SQLError.notImplemented();
  }
  
  protected short getNativeShort(int columnIndex)
    throws SQLException
  {
    return getNativeShort(columnIndex, true);
  }
  
  protected short getNativeShort(int columnIndex, boolean overflowCheck)
    throws SQLException
  {
    checkRowPos();
    checkColumnBounds(columnIndex);
    
    columnIndex--;
    if (this.thisRow.isNull(columnIndex))
    {
      this.wasNullFlag = true;
      
      return 0;
    }
    this.wasNullFlag = false;
    
    Field f = this.fields[columnIndex];
    switch (f.getMysqlType())
    {
    case 1: 
      byte tinyintVal = getNativeByte(columnIndex + 1, false);
      if ((!f.isUnsigned()) || (tinyintVal >= 0)) {
        return (short)tinyintVal;
      }
      return (short)(tinyintVal + 256);
    case 2: 
    case 13: 
      short asShort = this.thisRow.getNativeShort(columnIndex);
      if (!f.isUnsigned()) {
        return asShort;
      }
      int valueAsInt = asShort & 0xFFFF;
      if ((overflowCheck) && (this.jdbcCompliantTruncationForReads) && (valueAsInt > 32767)) {
        throwRangeException(String.valueOf(valueAsInt), columnIndex + 1, 5);
      }
      return (short)valueAsInt;
    case 3: 
    case 9: 
      if (!f.isUnsigned())
      {
        int valueAsInt = getNativeInt(columnIndex + 1, false);
        if (((overflowCheck) && (this.jdbcCompliantTruncationForReads) && (valueAsInt > 32767)) || (valueAsInt < 32768)) {
          throwRangeException(String.valueOf(valueAsInt), columnIndex + 1, 5);
        }
        return (short)valueAsInt;
      }
      long valueAsLong = getNativeLong(columnIndex + 1, false, true);
      if ((overflowCheck) && (this.jdbcCompliantTruncationForReads) && (valueAsLong > 32767L)) {
        throwRangeException(String.valueOf(valueAsLong), columnIndex + 1, 5);
      }
      return (short)(int)valueAsLong;
    case 8: 
      long valueAsLong = getNativeLong(columnIndex + 1, false, false);
      if (!f.isUnsigned())
      {
        if ((overflowCheck) && (this.jdbcCompliantTruncationForReads) && (
          (valueAsLong < -32768L) || (valueAsLong > 32767L))) {
          throwRangeException(String.valueOf(valueAsLong), columnIndex + 1, 5);
        }
        return (short)(int)valueAsLong;
      }
      BigInteger asBigInt = convertLongToUlong(valueAsLong);
      if ((overflowCheck) && (this.jdbcCompliantTruncationForReads) && ((asBigInt.compareTo(new BigInteger(String.valueOf(32767))) > 0) || (asBigInt.compareTo(new BigInteger(String.valueOf(32768))) < 0))) {
        throwRangeException(asBigInt.toString(), columnIndex + 1, 5);
      }
      return (short)getIntFromString(asBigInt.toString(), columnIndex + 1);
    case 5: 
      double valueAsDouble = getNativeDouble(columnIndex + 1);
      if ((overflowCheck) && (this.jdbcCompliantTruncationForReads) && (
        (valueAsDouble < -32768.0D) || (valueAsDouble > 32767.0D))) {
        throwRangeException(String.valueOf(valueAsDouble), columnIndex + 1, 5);
      }
      return (short)(int)valueAsDouble;
    case 4: 
      float valueAsFloat = getNativeFloat(columnIndex + 1);
      if ((overflowCheck) && (this.jdbcCompliantTruncationForReads) && (
        (valueAsFloat < -32768.0F) || (valueAsFloat > 32767.0F))) {
        throwRangeException(String.valueOf(valueAsFloat), columnIndex + 1, 5);
      }
      return (short)(int)valueAsFloat;
    }
    String stringVal = getNativeString(columnIndex + 1);
    if (this.useUsageAdvisor) {
      issueConversionViaParsingWarning("getShort()", columnIndex, stringVal, this.fields[columnIndex], new int[] { 5, 1, 2, 3, 8, 4 });
    }
    return getShortFromString(stringVal, columnIndex + 1);
  }
  
  protected String getNativeString(int columnIndex)
    throws SQLException
  {
    checkRowPos();
    checkColumnBounds(columnIndex);
    if (this.fields == null) {
      throw SQLError.createSQLException(Messages.getString("ResultSet.Query_generated_no_fields_for_ResultSet_133"), "S1002", getExceptionInterceptor());
    }
    if (this.thisRow.isNull(columnIndex - 1))
    {
      this.wasNullFlag = true;
      
      return null;
    }
    this.wasNullFlag = false;
    
    String stringVal = null;
    
    Field field = this.fields[(columnIndex - 1)];
    
    stringVal = getNativeConvertToString(columnIndex, field);
    if ((field.isZeroFill()) && (stringVal != null))
    {
      int origLength = stringVal.length();
      
      StringBuffer zeroFillBuf = new StringBuffer(origLength);
      
      long numZeros = field.getLength() - origLength;
      for (long i = 0L; i < numZeros; i += 1L) {
        zeroFillBuf.append('0');
      }
      zeroFillBuf.append(stringVal);
      
      stringVal = zeroFillBuf.toString();
    }
    return stringVal;
  }
  
  private Time getNativeTime(int columnIndex, Calendar targetCalendar, TimeZone tz, boolean rollForward)
    throws SQLException
  {
    checkRowPos();
    checkColumnBounds(columnIndex);
    
    int columnIndexMinusOne = columnIndex - 1;
    
    int mysqlType = this.fields[columnIndexMinusOne].getMysqlType();
    
    Time timeVal = null;
    if (mysqlType == 11) {
      timeVal = this.thisRow.getNativeTime(columnIndexMinusOne, targetCalendar, tz, rollForward, this.connection, this);
    } else {
      timeVal = (Time)this.thisRow.getNativeDateTimeValue(columnIndexMinusOne, null, 92, mysqlType, tz, rollForward, this.connection, this);
    }
    if (timeVal == null)
    {
      this.wasNullFlag = true;
      
      return null;
    }
    this.wasNullFlag = false;
    
    return timeVal;
  }
  
  Time getNativeTimeViaParseConversion(int columnIndex, Calendar targetCalendar, TimeZone tz, boolean rollForward)
    throws SQLException
  {
    if (this.useUsageAdvisor) {
      issueConversionViaParsingWarning("getTime()", columnIndex, this.thisRow.getColumnValue(columnIndex - 1), this.fields[(columnIndex - 1)], new int[] { 11 });
    }
    String strTime = getNativeString(columnIndex);
    
    return getTimeFromString(strTime, targetCalendar, columnIndex, tz, rollForward);
  }
  
  private Timestamp getNativeTimestamp(int columnIndex, Calendar targetCalendar, TimeZone tz, boolean rollForward)
    throws SQLException
  {
    checkRowPos();
    checkColumnBounds(columnIndex);
    
    int columnIndexMinusOne = columnIndex - 1;
    
    Timestamp tsVal = null;
    
    int mysqlType = this.fields[columnIndexMinusOne].getMysqlType();
    switch (mysqlType)
    {
    case 7: 
    case 12: 
      tsVal = this.thisRow.getNativeTimestamp(columnIndexMinusOne, targetCalendar, tz, rollForward, this.connection, this);
      
      break;
    default: 
      tsVal = (Timestamp)this.thisRow.getNativeDateTimeValue(columnIndexMinusOne, null, 93, mysqlType, tz, rollForward, this.connection, this);
    }
    if (tsVal == null)
    {
      this.wasNullFlag = true;
      
      return null;
    }
    this.wasNullFlag = false;
    
    return tsVal;
  }
  
  Timestamp getNativeTimestampViaParseConversion(int columnIndex, Calendar targetCalendar, TimeZone tz, boolean rollForward)
    throws SQLException
  {
    if (this.useUsageAdvisor) {
      issueConversionViaParsingWarning("getTimestamp()", columnIndex, this.thisRow.getColumnValue(columnIndex - 1), this.fields[(columnIndex - 1)], new int[] { 7, 12 });
    }
    String strTimestamp = getNativeString(columnIndex);
    
    return getTimestampFromString(columnIndex, targetCalendar, strTimestamp, tz, rollForward);
  }
  
  protected InputStream getNativeUnicodeStream(int columnIndex)
    throws SQLException
  {
    checkRowPos();
    
    return getBinaryStream(columnIndex);
  }
  
  protected URL getNativeURL(int colIndex)
    throws SQLException
  {
    String val = getString(colIndex);
    if (val == null) {
      return null;
    }
    try
    {
      return new URL(val);
    }
    catch (MalformedURLException mfe)
    {
      throw SQLError.createSQLException(Messages.getString("ResultSet.Malformed_URL____141") + val + "'", "S1009", getExceptionInterceptor());
    }
  }
  
  public ResultSetInternalMethods getNextResultSet()
  {
    return this.nextResultSet;
  }
  
  public Object getObject(int columnIndex)
    throws SQLException
  {
    checkRowPos();
    checkColumnBounds(columnIndex);
    
    int columnIndexMinusOne = columnIndex - 1;
    if (this.thisRow.isNull(columnIndexMinusOne))
    {
      this.wasNullFlag = true;
      
      return null;
    }
    this.wasNullFlag = false;
    
    Field field = this.fields[columnIndexMinusOne];
    switch (field.getSQLType())
    {
    case -7: 
    case 16: 
      if ((field.getMysqlType() == 16) && (!field.isSingleBit())) {
        return getBytes(columnIndex);
      }
      return Boolean.valueOf(getBoolean(columnIndex));
    case -6: 
      if (!field.isUnsigned()) {
        return Constants.integerValueOf(getByte(columnIndex));
      }
      return Constants.integerValueOf(getInt(columnIndex));
    case 5: 
      return Constants.integerValueOf(getInt(columnIndex));
    case 4: 
      if ((!field.isUnsigned()) || (field.getMysqlType() == 9)) {
        return Constants.integerValueOf(getInt(columnIndex));
      }
      return Constants.longValueOf(getLong(columnIndex));
    case -5: 
      if (!field.isUnsigned()) {
        return Constants.longValueOf(getLong(columnIndex));
      }
      String stringVal = getString(columnIndex);
      if (stringVal == null) {
        return null;
      }
      try
      {
        return new BigInteger(stringVal);
      }
      catch (NumberFormatException nfe)
      {
        throw SQLError.createSQLException(Messages.getString("ResultSet.Bad_format_for_BigInteger", new Object[] { Constants.integerValueOf(columnIndex), stringVal }), "S1009", getExceptionInterceptor());
      }
    case 2: 
    case 3: 
      String stringVal = getString(columnIndex);
      if (stringVal != null)
      {
        if (stringVal.length() == 0)
        {
          BigDecimal val = new BigDecimal(0.0D);
          
          return val;
        }
        try
        {
          val = new BigDecimal(stringVal);
        }
        catch (NumberFormatException ex)
        {
          BigDecimal val;
          throw SQLError.createSQLException(Messages.getString("ResultSet.Bad_format_for_BigDecimal", new Object[] { stringVal, new Integer(columnIndex) }), "S1009", getExceptionInterceptor());
        }
        BigDecimal val;
        return val;
      }
      return null;
    case 7: 
      return new Float(getFloat(columnIndex));
    case 6: 
    case 8: 
      return new Double(getDouble(columnIndex));
    case 1: 
    case 12: 
      if (!field.isOpaqueBinary()) {
        return getString(columnIndex);
      }
      return getBytes(columnIndex);
    case -1: 
      if (!field.isOpaqueBinary()) {
        return getStringForClob(columnIndex);
      }
      return getBytes(columnIndex);
    case -4: 
    case -3: 
    case -2: 
      if (field.getMysqlType() == 255) {
        return getBytes(columnIndex);
      }
      if ((field.isBinary()) || (field.isBlob()))
      {
        byte[] data = getBytes(columnIndex);
        if (this.connection.getAutoDeserialize())
        {
          Object obj = data;
          if ((data != null) && (data.length >= 2)) {
            if ((data[0] == -84) && (data[1] == -19)) {
              try
              {
                ByteArrayInputStream bytesIn = new ByteArrayInputStream(data);
                
                ObjectInputStream objIn = new ObjectInputStream(bytesIn);
                
                obj = objIn.readObject();
                objIn.close();
                bytesIn.close();
              }
              catch (ClassNotFoundException cnfe)
              {
                throw SQLError.createSQLException(Messages.getString("ResultSet.Class_not_found___91") + cnfe.toString() + Messages.getString("ResultSet._while_reading_serialized_object_92"), getExceptionInterceptor());
              }
              catch (IOException ex)
              {
                obj = data;
              }
            } else {
              return getString(columnIndex);
            }
          }
          return obj;
        }
        return data;
      }
      return getBytes(columnIndex);
    case 91: 
      if ((field.getMysqlType() == 13) && (!this.connection.getYearIsDateType())) {
        return Constants.shortValueOf(getShort(columnIndex));
      }
      return getDate(columnIndex);
    case 92: 
      return getTime(columnIndex);
    case 93: 
      return getTimestamp(columnIndex);
    }
    return getString(columnIndex);
  }
  
  public Object getObject(int i, Map map)
    throws SQLException
  {
    return getObject(i);
  }
  
  public Object getObject(String columnName)
    throws SQLException
  {
    return getObject(findColumn(columnName));
  }
  
  public Object getObject(String colName, Map map)
    throws SQLException
  {
    return getObject(findColumn(colName), map);
  }
  
  public Object getObjectStoredProc(int columnIndex, int desiredSqlType)
    throws SQLException
  {
    checkRowPos();
    checkColumnBounds(columnIndex);
    
    Object value = this.thisRow.getColumnValue(columnIndex - 1);
    if (value == null)
    {
      this.wasNullFlag = true;
      
      return null;
    }
    this.wasNullFlag = false;
    
    Field field = this.fields[(columnIndex - 1)];
    switch (desiredSqlType)
    {
    case -7: 
    case 16: 
      return Boolean.valueOf(getBoolean(columnIndex));
    case -6: 
      return Constants.integerValueOf(getInt(columnIndex));
    case 5: 
      return Constants.integerValueOf(getInt(columnIndex));
    case 4: 
      if ((!field.isUnsigned()) || (field.getMysqlType() == 9)) {
        return Constants.integerValueOf(getInt(columnIndex));
      }
      return Constants.longValueOf(getLong(columnIndex));
    case -5: 
      if (field.isUnsigned()) {
        return getBigDecimal(columnIndex);
      }
      return Constants.longValueOf(getLong(columnIndex));
    case 2: 
    case 3: 
      String stringVal = getString(columnIndex);
      if (stringVal != null)
      {
        if (stringVal.length() == 0)
        {
          BigDecimal val = new BigDecimal(0.0D);
          
          return val;
        }
        try
        {
          val = new BigDecimal(stringVal);
        }
        catch (NumberFormatException ex)
        {
          BigDecimal val;
          throw SQLError.createSQLException(Messages.getString("ResultSet.Bad_format_for_BigDecimal", new Object[] { stringVal, new Integer(columnIndex) }), "S1009", getExceptionInterceptor());
        }
        BigDecimal val;
        return val;
      }
      return null;
    case 7: 
      return new Float(getFloat(columnIndex));
    case 6: 
      if (!this.connection.getRunningCTS13()) {
        return new Double(getFloat(columnIndex));
      }
      return new Float(getFloat(columnIndex));
    case 8: 
      return new Double(getDouble(columnIndex));
    case 1: 
    case 12: 
      return getString(columnIndex);
    case -1: 
      return getStringForClob(columnIndex);
    case -4: 
    case -3: 
    case -2: 
      return getBytes(columnIndex);
    case 91: 
      if ((field.getMysqlType() == 13) && (!this.connection.getYearIsDateType())) {
        return Constants.shortValueOf(getShort(columnIndex));
      }
      return getDate(columnIndex);
    case 92: 
      return getTime(columnIndex);
    case 93: 
      return getTimestamp(columnIndex);
    }
    return getString(columnIndex);
  }
  
  public Object getObjectStoredProc(int i, Map map, int desiredSqlType)
    throws SQLException
  {
    return getObjectStoredProc(i, desiredSqlType);
  }
  
  public Object getObjectStoredProc(String columnName, int desiredSqlType)
    throws SQLException
  {
    return getObjectStoredProc(findColumn(columnName), desiredSqlType);
  }
  
  public Object getObjectStoredProc(String colName, Map map, int desiredSqlType)
    throws SQLException
  {
    return getObjectStoredProc(findColumn(colName), map, desiredSqlType);
  }
  
  public Ref getRef(int i)
    throws SQLException
  {
    checkColumnBounds(i);
    throw SQLError.notImplemented();
  }
  
  public Ref getRef(String colName)
    throws SQLException
  {
    return getRef(findColumn(colName));
  }
  
  public int getRow()
    throws SQLException
  {
    checkClosed();
    
    int currentRowNumber = this.rowData.getCurrentRowNumber();
    int row = 0;
    if (!this.rowData.isDynamic())
    {
      if ((currentRowNumber < 0) || (this.rowData.isAfterLast()) || (this.rowData.isEmpty())) {
        row = 0;
      } else {
        row = currentRowNumber + 1;
      }
    }
    else {
      row = currentRowNumber + 1;
    }
    return row;
  }
  
  public String getServerInfo()
  {
    return this.serverInfo;
  }
  
  private long getNumericRepresentationOfSQLBitType(int columnIndex)
    throws SQLException
  {
    Object value = this.thisRow.getColumnValue(columnIndex - 1);
    if ((this.fields[(columnIndex - 1)].isSingleBit()) || (((byte[])value).length == 1)) {
      return ((byte[])value)[0];
    }
    byte[] asBytes = (byte[])value;
    
    int shift = 0;
    
    long[] steps = new long[asBytes.length];
    for (int i = asBytes.length - 1; i >= 0; i--)
    {
      steps[i] = ((asBytes[i] & 0xFF) << shift);
      shift += 8;
    }
    long valueAsLong = 0L;
    for (int i = 0; i < asBytes.length; i++) {
      valueAsLong |= steps[i];
    }
    return valueAsLong;
  }
  
  public short getShort(int columnIndex)
    throws SQLException
  {
    if (!this.isBinaryEncoded)
    {
      checkRowPos();
      if (this.useFastIntParsing)
      {
        checkColumnBounds(columnIndex);
        
        Object value = this.thisRow.getColumnValue(columnIndex - 1);
        if (value == null) {
          this.wasNullFlag = true;
        } else {
          this.wasNullFlag = false;
        }
        if (this.wasNullFlag) {
          return 0;
        }
        byte[] shortAsBytes = (byte[])value;
        if (shortAsBytes.length == 0) {
          return (short)convertToZeroWithEmptyCheck();
        }
        boolean needsFullParse = false;
        for (int i = 0; i < shortAsBytes.length; i++) {
          if (((char)shortAsBytes[i] == 'e') || ((char)shortAsBytes[i] == 'E'))
          {
            needsFullParse = true;
            
            break;
          }
        }
        if (!needsFullParse) {
          try
          {
            return parseShortWithOverflowCheck(columnIndex, shortAsBytes, null);
          }
          catch (NumberFormatException nfe)
          {
            try
            {
              return parseShortAsDouble(columnIndex, new String(shortAsBytes));
            }
            catch (NumberFormatException newNfe)
            {
              if (this.fields[(columnIndex - 1)].getMysqlType() == 16)
              {
                long valueAsLong = getNumericRepresentationOfSQLBitType(columnIndex);
                if ((this.jdbcCompliantTruncationForReads) && ((valueAsLong < -32768L) || (valueAsLong > 32767L))) {
                  throwRangeException(String.valueOf(valueAsLong), columnIndex, 5);
                }
                return (short)(int)valueAsLong;
              }
              throw SQLError.createSQLException(Messages.getString("ResultSet.Invalid_value_for_getShort()_-____96") + new String(shortAsBytes) + "'", "S1009", getExceptionInterceptor());
            }
          }
        }
      }
      String val = null;
      try
      {
        val = getString(columnIndex);
        if (val != null)
        {
          if (val.length() == 0) {
            return (short)convertToZeroWithEmptyCheck();
          }
          if ((val.indexOf("e") == -1) && (val.indexOf("E") == -1) && (val.indexOf(".") == -1)) {
            return parseShortWithOverflowCheck(columnIndex, null, val);
          }
          return parseShortAsDouble(columnIndex, val);
        }
        return 0;
      }
      catch (NumberFormatException nfe)
      {
        try
        {
          return parseShortAsDouble(columnIndex, val);
        }
        catch (NumberFormatException newNfe)
        {
          if (this.fields[(columnIndex - 1)].getMysqlType() == 16)
          {
            long valueAsLong = getNumericRepresentationOfSQLBitType(columnIndex);
            if ((this.jdbcCompliantTruncationForReads) && ((valueAsLong < -32768L) || (valueAsLong > 32767L))) {
              throwRangeException(String.valueOf(valueAsLong), columnIndex, 5);
            }
            return (short)(int)valueAsLong;
          }
          throw SQLError.createSQLException(Messages.getString("ResultSet.Invalid_value_for_getShort()_-____96") + val + "'", "S1009", getExceptionInterceptor());
        }
      }
    }
    return getNativeShort(columnIndex);
  }
  
  public short getShort(String columnName)
    throws SQLException
  {
    return getShort(findColumn(columnName));
  }
  
  private final short getShortFromString(String val, int columnIndex)
    throws SQLException
  {
    try
    {
      if (val != null)
      {
        if (val.length() == 0) {
          return (short)convertToZeroWithEmptyCheck();
        }
        if ((val.indexOf("e") == -1) && (val.indexOf("E") == -1) && (val.indexOf(".") == -1)) {
          return parseShortWithOverflowCheck(columnIndex, null, val);
        }
        return parseShortAsDouble(columnIndex, val);
      }
      return 0;
    }
    catch (NumberFormatException nfe)
    {
      try
      {
        return parseShortAsDouble(columnIndex, val);
      }
      catch (NumberFormatException newNfe)
      {
        throw SQLError.createSQLException(Messages.getString("ResultSet.Invalid_value_for_getShort()_-____217") + val + Messages.getString("ResultSet.___in_column__218") + columnIndex, "S1009", getExceptionInterceptor());
      }
    }
  }
  
  public Statement getStatement()
    throws SQLException
  {
    if ((this.isClosed) && (!this.retainOwningStatement)) {
      throw SQLError.createSQLException("Operation not allowed on closed ResultSet. Statements can be retained over result set closure by setting the connection property \"retainStatementAfterResultSetClose\" to \"true\".", "S1000", getExceptionInterceptor());
    }
    if (this.wrapperStatement != null) {
      return this.wrapperStatement;
    }
    return this.owningStatement;
  }
  
  public String getString(int columnIndex)
    throws SQLException
  {
    String stringVal = getStringInternal(columnIndex, true);
    if ((this.padCharsWithSpace) && (stringVal != null))
    {
      Field f = this.fields[(columnIndex - 1)];
      if (f.getMysqlType() == 254)
      {
        int fieldLength = (int)f.getLength() / f.getMaxBytesPerCharacter();
        
        int currentLength = stringVal.length();
        if (currentLength < fieldLength)
        {
          StringBuffer paddedBuf = new StringBuffer(fieldLength);
          paddedBuf.append(stringVal);
          
          int difference = fieldLength - currentLength;
          
          paddedBuf.append(EMPTY_SPACE, 0, difference);
          
          stringVal = paddedBuf.toString();
        }
      }
    }
    return stringVal;
  }
  
  public String getString(String columnName)
    throws SQLException
  {
    return getString(findColumn(columnName));
  }
  
  private String getStringForClob(int columnIndex)
    throws SQLException
  {
    String asString = null;
    
    String forcedEncoding = this.connection.getClobCharacterEncoding();
    if (forcedEncoding == null)
    {
      if (!this.isBinaryEncoded) {
        asString = getString(columnIndex);
      } else {
        asString = getNativeString(columnIndex);
      }
    }
    else {
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
    }
    return asString;
  }
  
  protected String getStringInternal(int columnIndex, boolean checkDateTypes)
    throws SQLException
  {
    if (!this.isBinaryEncoded)
    {
      checkRowPos();
      checkColumnBounds(columnIndex);
      if (this.fields == null) {
        throw SQLError.createSQLException(Messages.getString("ResultSet.Query_generated_no_fields_for_ResultSet_99"), "S1002", getExceptionInterceptor());
      }
      int internalColumnIndex = columnIndex - 1;
      if (this.thisRow.isNull(internalColumnIndex))
      {
        this.wasNullFlag = true;
        
        return null;
      }
      this.wasNullFlag = false;
      
      Field metadata = this.fields[internalColumnIndex];
      
      String stringVal = null;
      if (metadata.getMysqlType() == 16)
      {
        if (metadata.isSingleBit())
        {
          byte[] value = this.thisRow.getColumnValue(internalColumnIndex);
          if (value.length == 0) {
            return String.valueOf(convertToZeroWithEmptyCheck());
          }
          return String.valueOf(value[0]);
        }
        return String.valueOf(getNumericRepresentationOfSQLBitType(columnIndex));
      }
      String encoding = metadata.getCharacterSet();
      
      stringVal = this.thisRow.getString(internalColumnIndex, encoding, this.connection);
      if (metadata.getMysqlType() == 13)
      {
        if (!this.connection.getYearIsDateType()) {
          return stringVal;
        }
        Date dt = getDateFromString(stringVal, columnIndex, null);
        if (dt == null)
        {
          this.wasNullFlag = true;
          
          return null;
        }
        this.wasNullFlag = false;
        
        return dt.toString();
      }
      if ((checkDateTypes) && (!this.connection.getNoDatetimeStringSync())) {
        switch (metadata.getSQLType())
        {
        case 92: 
          Time tm = getTimeFromString(stringVal, null, columnIndex, getDefaultTimeZone(), false);
          if (tm == null)
          {
            this.wasNullFlag = true;
            
            return null;
          }
          this.wasNullFlag = false;
          
          return tm.toString();
        case 91: 
          Date dt = getDateFromString(stringVal, columnIndex, null);
          if (dt == null)
          {
            this.wasNullFlag = true;
            
            return null;
          }
          this.wasNullFlag = false;
          
          return dt.toString();
        case 93: 
          Timestamp ts = getTimestampFromString(columnIndex, null, stringVal, getDefaultTimeZone(), false);
          if (ts == null)
          {
            this.wasNullFlag = true;
            
            return null;
          }
          this.wasNullFlag = false;
          
          return ts.toString();
        }
      }
      return stringVal;
    }
    return getNativeString(columnIndex);
  }
  
  public Time getTime(int columnIndex)
    throws SQLException
  {
    return getTimeInternal(columnIndex, null, getDefaultTimeZone(), false);
  }
  
  public Time getTime(int columnIndex, Calendar cal)
    throws SQLException
  {
    return getTimeInternal(columnIndex, cal, cal.getTimeZone(), true);
  }
  
  public Time getTime(String columnName)
    throws SQLException
  {
    return getTime(findColumn(columnName));
  }
  
  public Time getTime(String columnName, Calendar cal)
    throws SQLException
  {
    return getTime(findColumn(columnName), cal);
  }
  
  private Time getTimeInternal(int columnIndex, Calendar targetCalendar, TimeZone tz, boolean rollForward)
    throws SQLException
  {
    checkRowPos();
    if (this.isBinaryEncoded) {
      return getNativeTime(columnIndex, targetCalendar, tz, rollForward);
    }
    if (!this.useFastDateParsing)
    {
      String timeAsString = getStringInternal(columnIndex, false);
      
      return getTimeFromString(timeAsString, targetCalendar, columnIndex, tz, rollForward);
    }
    checkColumnBounds(columnIndex);
    
    int columnIndexMinusOne = columnIndex - 1;
    if (this.thisRow.isNull(columnIndexMinusOne))
    {
      this.wasNullFlag = true;
      
      return null;
    }
    this.wasNullFlag = false;
    
    return this.thisRow.getTimeFast(columnIndexMinusOne, targetCalendar, tz, rollForward, this.connection, this);
  }
  
  public Timestamp getTimestamp(int columnIndex)
    throws SQLException
  {
    return getTimestampInternal(columnIndex, null, getDefaultTimeZone(), false);
  }
  
  public Timestamp getTimestamp(int columnIndex, Calendar cal)
    throws SQLException
  {
    return getTimestampInternal(columnIndex, cal, cal.getTimeZone(), true);
  }
  
  public Timestamp getTimestamp(String columnName)
    throws SQLException
  {
    return getTimestamp(findColumn(columnName));
  }
  
  public Timestamp getTimestamp(String columnName, Calendar cal)
    throws SQLException
  {
    return getTimestamp(findColumn(columnName), cal);
  }
  
  private Timestamp getTimestampInternal(int columnIndex, Calendar targetCalendar, TimeZone tz, boolean rollForward)
    throws SQLException
  {
    if (this.isBinaryEncoded) {
      return getNativeTimestamp(columnIndex, targetCalendar, tz, rollForward);
    }
    Timestamp tsVal = null;
    if (!this.useFastDateParsing)
    {
      String timestampValue = getStringInternal(columnIndex, false);
      
      tsVal = getTimestampFromString(columnIndex, targetCalendar, timestampValue, tz, rollForward);
    }
    else
    {
      checkClosed();
      checkRowPos();
      checkColumnBounds(columnIndex);
      
      tsVal = this.thisRow.getTimestampFast(columnIndex - 1, targetCalendar, tz, rollForward, this.connection, this);
    }
    if (tsVal == null) {
      this.wasNullFlag = true;
    } else {
      this.wasNullFlag = false;
    }
    return tsVal;
  }
  
  public int getType()
    throws SQLException
  {
    return this.resultSetType;
  }
  
  /**
   * @deprecated
   */
  public InputStream getUnicodeStream(int columnIndex)
    throws SQLException
  {
    if (!this.isBinaryEncoded)
    {
      checkRowPos();
      
      return getBinaryStream(columnIndex);
    }
    return getNativeBinaryStream(columnIndex);
  }
  
  /**
   * @deprecated
   */
  public InputStream getUnicodeStream(String columnName)
    throws SQLException
  {
    return getUnicodeStream(findColumn(columnName));
  }
  
  public long getUpdateCount()
  {
    return this.updateCount;
  }
  
  public long getUpdateID()
  {
    return this.updateId;
  }
  
  public URL getURL(int colIndex)
    throws SQLException
  {
    String val = getString(colIndex);
    if (val == null) {
      return null;
    }
    try
    {
      return new URL(val);
    }
    catch (MalformedURLException mfe)
    {
      throw SQLError.createSQLException(Messages.getString("ResultSet.Malformed_URL____104") + val + "'", "S1009", getExceptionInterceptor());
    }
  }
  
  public URL getURL(String colName)
    throws SQLException
  {
    String val = getString(colName);
    if (val == null) {
      return null;
    }
    try
    {
      return new URL(val);
    }
    catch (MalformedURLException mfe)
    {
      throw SQLError.createSQLException(Messages.getString("ResultSet.Malformed_URL____107") + val + "'", "S1009", getExceptionInterceptor());
    }
  }
  
  public SQLWarning getWarnings()
    throws SQLException
  {
    return this.warningChain;
  }
  
  public void insertRow()
    throws SQLException
  {
    throw new NotUpdatable();
  }
  
  public boolean isAfterLast()
    throws SQLException
  {
    checkClosed();
    
    boolean b = this.rowData.isAfterLast();
    
    return b;
  }
  
  public boolean isBeforeFirst()
    throws SQLException
  {
    checkClosed();
    
    return this.rowData.isBeforeFirst();
  }
  
  public boolean isFirst()
    throws SQLException
  {
    checkClosed();
    
    return this.rowData.isFirst();
  }
  
  public boolean isLast()
    throws SQLException
  {
    checkClosed();
    
    return this.rowData.isLast();
  }
  
  private void issueConversionViaParsingWarning(String methodName, int columnIndex, Object value, Field fieldInfo, int[] typesWithNoParseConversion)
    throws SQLException
  {
    StringBuffer originalQueryBuf = new StringBuffer();
    if ((this.owningStatement != null) && ((this.owningStatement instanceof PreparedStatement)))
    {
      originalQueryBuf.append(Messages.getString("ResultSet.CostlyConversionCreatedFromQuery"));
      originalQueryBuf.append(((PreparedStatement)this.owningStatement).originalSql);
      
      originalQueryBuf.append("\n\n");
    }
    else
    {
      originalQueryBuf.append(".");
    }
    StringBuffer convertibleTypesBuf = new StringBuffer();
    for (int i = 0; i < typesWithNoParseConversion.length; i++)
    {
      convertibleTypesBuf.append(MysqlDefs.typeToName(typesWithNoParseConversion[i]));
      convertibleTypesBuf.append("\n");
    }
    String message = Messages.getString("ResultSet.CostlyConversion", new Object[] { methodName, new Integer(columnIndex + 1), fieldInfo.getOriginalName(), fieldInfo.getOriginalTableName(), originalQueryBuf.toString(), value != null ? value.getClass().getName() : ResultSetMetaData.getClassNameForJavaType(fieldInfo.getSQLType(), fieldInfo.isUnsigned(), fieldInfo.getMysqlType(), (fieldInfo.isBinary()) || (fieldInfo.isBlob()) ? 1 : false, fieldInfo.isOpaqueBinary()), MysqlDefs.typeToName(fieldInfo.getMysqlType()), convertibleTypesBuf.toString() });
    
    this.eventSink.consumeEvent(new ProfilerEvent((byte)0, "", this.owningStatement == null ? "N/A" : this.owningStatement.currentCatalog, this.connectionId, this.owningStatement == null ? -1 : this.owningStatement.getId(), this.resultId, System.currentTimeMillis(), 0L, Constants.MILLIS_I18N, null, this.pointOfOrigin, message));
  }
  
  public boolean last()
    throws SQLException
  {
    checkClosed();
    
    boolean b = true;
    if (this.rowData.size() == 0)
    {
      b = false;
    }
    else
    {
      if (this.onInsertRow) {
        this.onInsertRow = false;
      }
      if (this.doingUpdates) {
        this.doingUpdates = false;
      }
      if (this.thisRow != null) {
        this.thisRow.closeOpenStreams();
      }
      this.rowData.beforeLast();
      this.thisRow = this.rowData.next();
    }
    setRowPositionValidity();
    
    return b;
  }
  
  public void moveToCurrentRow()
    throws SQLException
  {
    throw new NotUpdatable();
  }
  
  public void moveToInsertRow()
    throws SQLException
  {
    throw new NotUpdatable();
  }
  
  public boolean next()
    throws SQLException
  {
    checkClosed();
    if (this.onInsertRow) {
      this.onInsertRow = false;
    }
    if (this.doingUpdates) {
      this.doingUpdates = false;
    }
    if (!reallyResult()) {
      throw SQLError.createSQLException(Messages.getString("ResultSet.ResultSet_is_from_UPDATE._No_Data_115"), "S1000", getExceptionInterceptor());
    }
    if (this.thisRow != null) {
      this.thisRow.closeOpenStreams();
    }
    boolean b;
    boolean b;
    if (this.rowData.size() == 0)
    {
      b = false;
    }
    else
    {
      this.thisRow = this.rowData.next();
      boolean b;
      if (this.thisRow == null)
      {
        b = false;
      }
      else
      {
        clearWarnings();
        
        b = true;
      }
    }
    setRowPositionValidity();
    
    return b;
  }
  
  private int parseIntAsDouble(int columnIndex, String val)
    throws NumberFormatException, SQLException
  {
    if (val == null) {
      return 0;
    }
    double valueAsDouble = Double.parseDouble(val);
    if ((this.jdbcCompliantTruncationForReads) && (
      (valueAsDouble < -2.147483648E9D) || (valueAsDouble > 2.147483647E9D))) {
      throwRangeException(String.valueOf(valueAsDouble), columnIndex, 4);
    }
    return (int)valueAsDouble;
  }
  
  private int getIntWithOverflowCheck(int columnIndex)
    throws SQLException
  {
    int intValue = this.thisRow.getInt(columnIndex);
    
    checkForIntegerTruncation(columnIndex, null, intValue);
    
    return intValue;
  }
  
  private void checkForIntegerTruncation(int columnIndex, byte[] valueAsBytes, int intValue)
    throws SQLException
  {
    if ((this.jdbcCompliantTruncationForReads) && (
      (intValue == Integer.MIN_VALUE) || (intValue == Integer.MAX_VALUE)))
    {
      String valueAsString = null;
      if (valueAsBytes == null) {
        valueAsString = this.thisRow.getString(columnIndex, this.fields[columnIndex].getCharacterSet(), this.connection);
      }
      long valueAsLong = Long.parseLong(valueAsString == null ? new String(valueAsBytes) : valueAsString);
      if ((valueAsLong < -2147483648L) || (valueAsLong > 2147483647L)) {
        throwRangeException(valueAsString == null ? new String(valueAsBytes) : valueAsString, columnIndex + 1, 4);
      }
    }
  }
  
  private long parseLongAsDouble(int columnIndexZeroBased, String val)
    throws NumberFormatException, SQLException
  {
    if (val == null) {
      return 0L;
    }
    double valueAsDouble = Double.parseDouble(val);
    if ((this.jdbcCompliantTruncationForReads) && (
      (valueAsDouble < -9.223372036854776E18D) || (valueAsDouble > 9.223372036854776E18D))) {
      throwRangeException(val, columnIndexZeroBased + 1, -5);
    }
    return valueAsDouble;
  }
  
  private long getLongWithOverflowCheck(int columnIndexZeroBased, boolean doOverflowCheck)
    throws SQLException
  {
    long longValue = this.thisRow.getLong(columnIndexZeroBased);
    if (doOverflowCheck) {
      checkForLongTruncation(columnIndexZeroBased, null, longValue);
    }
    return longValue;
  }
  
  private long parseLongWithOverflowCheck(int columnIndexZeroBased, byte[] valueAsBytes, String valueAsString, boolean doCheck)
    throws NumberFormatException, SQLException
  {
    long longValue = 0L;
    if ((valueAsBytes == null) && (valueAsString == null)) {
      return 0L;
    }
    if (valueAsBytes != null)
    {
      longValue = StringUtils.getLong(valueAsBytes);
    }
    else
    {
      valueAsString = valueAsString.trim();
      
      longValue = Long.parseLong(valueAsString);
    }
    if ((doCheck) && (this.jdbcCompliantTruncationForReads)) {
      checkForLongTruncation(columnIndexZeroBased, valueAsBytes, longValue);
    }
    return longValue;
  }
  
  private void checkForLongTruncation(int columnIndexZeroBased, byte[] valueAsBytes, long longValue)
    throws SQLException
  {
    if ((longValue == Long.MIN_VALUE) || (longValue == Long.MAX_VALUE))
    {
      String valueAsString = null;
      if (valueAsBytes == null) {
        valueAsString = this.thisRow.getString(columnIndexZeroBased, this.fields[columnIndexZeroBased].getCharacterSet(), this.connection);
      }
      double valueAsDouble = Double.parseDouble(valueAsString == null ? new String(valueAsBytes) : valueAsString);
      if ((valueAsDouble < -9.223372036854776E18D) || (valueAsDouble > 9.223372036854776E18D)) {
        throwRangeException(valueAsString == null ? new String(valueAsBytes) : valueAsString, columnIndexZeroBased + 1, -5);
      }
    }
  }
  
  private short parseShortAsDouble(int columnIndex, String val)
    throws NumberFormatException, SQLException
  {
    if (val == null) {
      return 0;
    }
    double valueAsDouble = Double.parseDouble(val);
    if ((this.jdbcCompliantTruncationForReads) && (
      (valueAsDouble < -32768.0D) || (valueAsDouble > 32767.0D))) {
      throwRangeException(String.valueOf(valueAsDouble), columnIndex, 5);
    }
    return (short)(int)valueAsDouble;
  }
  
  private short parseShortWithOverflowCheck(int columnIndex, byte[] valueAsBytes, String valueAsString)
    throws NumberFormatException, SQLException
  {
    short shortValue = 0;
    if ((valueAsBytes == null) && (valueAsString == null)) {
      return 0;
    }
    if (valueAsBytes != null)
    {
      shortValue = StringUtils.getShort(valueAsBytes);
    }
    else
    {
      valueAsString = valueAsString.trim();
      
      shortValue = Short.parseShort(valueAsString);
    }
    if ((this.jdbcCompliantTruncationForReads) && (
      (shortValue == Short.MIN_VALUE) || (shortValue == Short.MAX_VALUE)))
    {
      long valueAsLong = Long.parseLong(valueAsString == null ? new String(valueAsBytes) : valueAsString);
      if ((valueAsLong < -32768L) || (valueAsLong > 32767L)) {
        throwRangeException(valueAsString == null ? new String(valueAsBytes) : valueAsString, columnIndex, 5);
      }
    }
    return shortValue;
  }
  
  public boolean prev()
    throws SQLException
  {
    checkClosed();
    
    int rowIndex = this.rowData.getCurrentRowNumber();
    if (this.thisRow != null) {
      this.thisRow.closeOpenStreams();
    }
    boolean b = true;
    if (rowIndex - 1 >= 0)
    {
      rowIndex--;
      this.rowData.setCurrentRow(rowIndex);
      this.thisRow = this.rowData.getAt(rowIndex);
      
      b = true;
    }
    else if (rowIndex - 1 == -1)
    {
      rowIndex--;
      this.rowData.setCurrentRow(rowIndex);
      this.thisRow = null;
      
      b = false;
    }
    else
    {
      b = false;
    }
    setRowPositionValidity();
    
    return b;
  }
  
  public boolean previous()
    throws SQLException
  {
    if (this.onInsertRow) {
      this.onInsertRow = false;
    }
    if (this.doingUpdates) {
      this.doingUpdates = false;
    }
    return prev();
  }
  
  public void realClose(boolean calledExplicitly)
    throws SQLException
  {
    if (this.isClosed) {
      return;
    }
    try
    {
      if (this.useUsageAdvisor)
      {
        if (!calledExplicitly) {
          this.eventSink.consumeEvent(new ProfilerEvent((byte)0, "", this.owningStatement == null ? "N/A" : this.owningStatement.currentCatalog, this.connectionId, this.owningStatement == null ? -1 : this.owningStatement.getId(), this.resultId, System.currentTimeMillis(), 0L, Constants.MILLIS_I18N, null, this.pointOfOrigin, Messages.getString("ResultSet.ResultSet_implicitly_closed_by_driver")));
        }
        if ((this.rowData instanceof RowDataStatic))
        {
          if (this.rowData.size() > this.connection.getResultSetSizeThreshold()) {
            this.eventSink.consumeEvent(new ProfilerEvent((byte)0, "", this.owningStatement == null ? Messages.getString("ResultSet.N/A_159") : this.owningStatement.currentCatalog, this.connectionId, this.owningStatement == null ? -1 : this.owningStatement.getId(), this.resultId, System.currentTimeMillis(), 0L, Constants.MILLIS_I18N, null, this.pointOfOrigin, Messages.getString("ResultSet.Too_Large_Result_Set", new Object[] { new Integer(this.rowData.size()), new Integer(this.connection.getResultSetSizeThreshold()) })));
          }
          if ((!isLast()) && (!isAfterLast()) && (this.rowData.size() != 0)) {
            this.eventSink.consumeEvent(new ProfilerEvent((byte)0, "", this.owningStatement == null ? Messages.getString("ResultSet.N/A_159") : this.owningStatement.currentCatalog, this.connectionId, this.owningStatement == null ? -1 : this.owningStatement.getId(), this.resultId, System.currentTimeMillis(), 0L, Constants.MILLIS_I18N, null, this.pointOfOrigin, Messages.getString("ResultSet.Possible_incomplete_traversal_of_result_set", new Object[] { new Integer(getRow()), new Integer(this.rowData.size()) })));
          }
        }
        if ((this.columnUsed.length > 0) && (!this.rowData.wasEmpty()))
        {
          StringBuffer buf = new StringBuffer(Messages.getString("ResultSet.The_following_columns_were_never_referenced"));
          
          boolean issueWarn = false;
          for (int i = 0; i < this.columnUsed.length; i++) {
            if (this.columnUsed[i] == 0)
            {
              if (!issueWarn) {
                issueWarn = true;
              } else {
                buf.append(", ");
              }
              buf.append(this.fields[i].getFullName());
            }
          }
          if (issueWarn) {
            this.eventSink.consumeEvent(new ProfilerEvent((byte)0, "", this.owningStatement == null ? "N/A" : this.owningStatement.currentCatalog, this.connectionId, this.owningStatement == null ? -1 : this.owningStatement.getId(), 0, System.currentTimeMillis(), 0L, Constants.MILLIS_I18N, null, this.pointOfOrigin, buf.toString()));
          }
        }
      }
    }
    finally
    {
      if ((this.owningStatement != null) && (calledExplicitly)) {
        this.owningStatement.removeOpenResultSet(this);
      }
      SQLException exceptionDuringClose = null;
      if (this.rowData != null) {
        try
        {
          this.rowData.close();
        }
        catch (SQLException sqlEx)
        {
          exceptionDuringClose = sqlEx;
        }
      }
      if (this.statementUsedForFetchingRows != null) {
        try
        {
          this.statementUsedForFetchingRows.realClose(true, false);
        }
        catch (SQLException sqlEx)
        {
          if (exceptionDuringClose != null) {
            exceptionDuringClose.setNextException(sqlEx);
          } else {
            exceptionDuringClose = sqlEx;
          }
        }
      }
      this.rowData = null;
      this.defaultTimeZone = null;
      this.fields = null;
      this.columnLabelToIndex = null;
      this.fullColumnNameToIndex = null;
      this.columnToIndexCache = null;
      this.eventSink = null;
      this.warningChain = null;
      if (!this.retainOwningStatement) {
        this.owningStatement = null;
      }
      this.catalog = null;
      this.serverInfo = null;
      this.thisRow = null;
      this.fastDateCal = null;
      this.connection = null;
      
      this.isClosed = true;
      if (exceptionDuringClose != null) {
        throw exceptionDuringClose;
      }
    }
  }
  
  public boolean reallyResult()
  {
    if (this.rowData != null) {
      return true;
    }
    return this.reallyResult;
  }
  
  public void refreshRow()
    throws SQLException
  {
    throw new NotUpdatable();
  }
  
  public boolean relative(int rows)
    throws SQLException
  {
    checkClosed();
    if (this.rowData.size() == 0)
    {
      setRowPositionValidity();
      
      return false;
    }
    if (this.thisRow != null) {
      this.thisRow.closeOpenStreams();
    }
    this.rowData.moveRowRelative(rows);
    this.thisRow = this.rowData.getAt(this.rowData.getCurrentRowNumber());
    
    setRowPositionValidity();
    
    return (!this.rowData.isAfterLast()) && (!this.rowData.isBeforeFirst());
  }
  
  public boolean rowDeleted()
    throws SQLException
  {
    throw SQLError.notImplemented();
  }
  
  public boolean rowInserted()
    throws SQLException
  {
    throw SQLError.notImplemented();
  }
  
  public boolean rowUpdated()
    throws SQLException
  {
    throw SQLError.notImplemented();
  }
  
  protected void setBinaryEncoded()
  {
    this.isBinaryEncoded = true;
  }
  
  private void setDefaultTimeZone(TimeZone defaultTimeZone)
  {
    this.defaultTimeZone = defaultTimeZone;
  }
  
  public void setFetchDirection(int direction)
    throws SQLException
  {
    if ((direction != 1000) && (direction != 1001) && (direction != 1002)) {
      throw SQLError.createSQLException(Messages.getString("ResultSet.Illegal_value_for_fetch_direction_64"), "S1009", getExceptionInterceptor());
    }
    this.fetchDirection = direction;
  }
  
  public void setFetchSize(int rows)
    throws SQLException
  {
    if (rows < 0) {
      throw SQLError.createSQLException(Messages.getString("ResultSet.Value_must_be_between_0_and_getMaxRows()_66"), "S1009", getExceptionInterceptor());
    }
    this.fetchSize = rows;
  }
  
  public void setFirstCharOfQuery(char c)
  {
    this.firstCharOfQuery = c;
  }
  
  protected void setNextResultSet(ResultSetInternalMethods nextResultSet)
  {
    this.nextResultSet = nextResultSet;
  }
  
  public void setOwningStatement(StatementImpl owningStatement)
  {
    this.owningStatement = owningStatement;
  }
  
  protected void setResultSetConcurrency(int concurrencyFlag)
  {
    this.resultSetConcurrency = concurrencyFlag;
  }
  
  protected void setResultSetType(int typeFlag)
  {
    this.resultSetType = typeFlag;
  }
  
  protected void setServerInfo(String info)
  {
    this.serverInfo = info;
  }
  
  public void setStatementUsedForFetchingRows(PreparedStatement stmt)
  {
    this.statementUsedForFetchingRows = stmt;
  }
  
  public void setWrapperStatement(Statement wrapperStatement)
  {
    this.wrapperStatement = wrapperStatement;
  }
  
  private void throwRangeException(String valueAsString, int columnIndex, int jdbcType)
    throws SQLException
  {
    String datatype = null;
    switch (jdbcType)
    {
    case -6: 
      datatype = "TINYINT";
      break;
    case 5: 
      datatype = "SMALLINT";
      break;
    case 4: 
      datatype = "INTEGER";
      break;
    case -5: 
      datatype = "BIGINT";
      break;
    case 7: 
      datatype = "REAL";
      break;
    case 6: 
      datatype = "FLOAT";
      break;
    case 8: 
      datatype = "DOUBLE";
      break;
    case 3: 
      datatype = "DECIMAL";
      break;
    case -4: 
    case -3: 
    case -2: 
    case -1: 
    case 0: 
    case 1: 
    case 2: 
    default: 
      datatype = " (JDBC type '" + jdbcType + "')";
    }
    throw SQLError.createSQLException("'" + valueAsString + "' in column '" + columnIndex + "' is outside valid range for the datatype " + datatype + ".", "22003", getExceptionInterceptor());
  }
  
  public String toString()
  {
    if (this.reallyResult) {
      return super.toString();
    }
    return "Result set representing update count of " + this.updateCount;
  }
  
  public void updateArray(int arg0, Array arg1)
    throws SQLException
  {
    throw SQLError.notImplemented();
  }
  
  public void updateArray(String arg0, Array arg1)
    throws SQLException
  {
    throw SQLError.notImplemented();
  }
  
  public void updateAsciiStream(int columnIndex, InputStream x, int length)
    throws SQLException
  {
    throw new NotUpdatable();
  }
  
  public void updateAsciiStream(String columnName, InputStream x, int length)
    throws SQLException
  {
    updateAsciiStream(findColumn(columnName), x, length);
  }
  
  public void updateBigDecimal(int columnIndex, BigDecimal x)
    throws SQLException
  {
    throw new NotUpdatable();
  }
  
  public void updateBigDecimal(String columnName, BigDecimal x)
    throws SQLException
  {
    updateBigDecimal(findColumn(columnName), x);
  }
  
  public void updateBinaryStream(int columnIndex, InputStream x, int length)
    throws SQLException
  {
    throw new NotUpdatable();
  }
  
  public void updateBinaryStream(String columnName, InputStream x, int length)
    throws SQLException
  {
    updateBinaryStream(findColumn(columnName), x, length);
  }
  
  public void updateBlob(int arg0, java.sql.Blob arg1)
    throws SQLException
  {
    throw new NotUpdatable();
  }
  
  public void updateBlob(String arg0, java.sql.Blob arg1)
    throws SQLException
  {
    throw new NotUpdatable();
  }
  
  public void updateBoolean(int columnIndex, boolean x)
    throws SQLException
  {
    throw new NotUpdatable();
  }
  
  public void updateBoolean(String columnName, boolean x)
    throws SQLException
  {
    updateBoolean(findColumn(columnName), x);
  }
  
  public void updateByte(int columnIndex, byte x)
    throws SQLException
  {
    throw new NotUpdatable();
  }
  
  public void updateByte(String columnName, byte x)
    throws SQLException
  {
    updateByte(findColumn(columnName), x);
  }
  
  public void updateBytes(int columnIndex, byte[] x)
    throws SQLException
  {
    throw new NotUpdatable();
  }
  
  public void updateBytes(String columnName, byte[] x)
    throws SQLException
  {
    updateBytes(findColumn(columnName), x);
  }
  
  public void updateCharacterStream(int columnIndex, Reader x, int length)
    throws SQLException
  {
    throw new NotUpdatable();
  }
  
  public void updateCharacterStream(String columnName, Reader reader, int length)
    throws SQLException
  {
    updateCharacterStream(findColumn(columnName), reader, length);
  }
  
  public void updateClob(int arg0, java.sql.Clob arg1)
    throws SQLException
  {
    throw SQLError.notImplemented();
  }
  
  public void updateClob(String columnName, java.sql.Clob clob)
    throws SQLException
  {
    updateClob(findColumn(columnName), clob);
  }
  
  public void updateDate(int columnIndex, Date x)
    throws SQLException
  {
    throw new NotUpdatable();
  }
  
  public void updateDate(String columnName, Date x)
    throws SQLException
  {
    updateDate(findColumn(columnName), x);
  }
  
  public void updateDouble(int columnIndex, double x)
    throws SQLException
  {
    throw new NotUpdatable();
  }
  
  public void updateDouble(String columnName, double x)
    throws SQLException
  {
    updateDouble(findColumn(columnName), x);
  }
  
  public void updateFloat(int columnIndex, float x)
    throws SQLException
  {
    throw new NotUpdatable();
  }
  
  public void updateFloat(String columnName, float x)
    throws SQLException
  {
    updateFloat(findColumn(columnName), x);
  }
  
  public void updateInt(int columnIndex, int x)
    throws SQLException
  {
    throw new NotUpdatable();
  }
  
  public void updateInt(String columnName, int x)
    throws SQLException
  {
    updateInt(findColumn(columnName), x);
  }
  
  public void updateLong(int columnIndex, long x)
    throws SQLException
  {
    throw new NotUpdatable();
  }
  
  public void updateLong(String columnName, long x)
    throws SQLException
  {
    updateLong(findColumn(columnName), x);
  }
  
  public void updateNull(int columnIndex)
    throws SQLException
  {
    throw new NotUpdatable();
  }
  
  public void updateNull(String columnName)
    throws SQLException
  {
    updateNull(findColumn(columnName));
  }
  
  public void updateObject(int columnIndex, Object x)
    throws SQLException
  {
    throw new NotUpdatable();
  }
  
  public void updateObject(int columnIndex, Object x, int scale)
    throws SQLException
  {
    throw new NotUpdatable();
  }
  
  public void updateObject(String columnName, Object x)
    throws SQLException
  {
    updateObject(findColumn(columnName), x);
  }
  
  public void updateObject(String columnName, Object x, int scale)
    throws SQLException
  {
    updateObject(findColumn(columnName), x);
  }
  
  public void updateRef(int arg0, Ref arg1)
    throws SQLException
  {
    throw SQLError.notImplemented();
  }
  
  public void updateRef(String arg0, Ref arg1)
    throws SQLException
  {
    throw SQLError.notImplemented();
  }
  
  public void updateRow()
    throws SQLException
  {
    throw new NotUpdatable();
  }
  
  public void updateShort(int columnIndex, short x)
    throws SQLException
  {
    throw new NotUpdatable();
  }
  
  public void updateShort(String columnName, short x)
    throws SQLException
  {
    updateShort(findColumn(columnName), x);
  }
  
  public void updateString(int columnIndex, String x)
    throws SQLException
  {
    throw new NotUpdatable();
  }
  
  public void updateString(String columnName, String x)
    throws SQLException
  {
    updateString(findColumn(columnName), x);
  }
  
  public void updateTime(int columnIndex, Time x)
    throws SQLException
  {
    throw new NotUpdatable();
  }
  
  public void updateTime(String columnName, Time x)
    throws SQLException
  {
    updateTime(findColumn(columnName), x);
  }
  
  public void updateTimestamp(int columnIndex, Timestamp x)
    throws SQLException
  {
    throw new NotUpdatable();
  }
  
  public void updateTimestamp(String columnName, Timestamp x)
    throws SQLException
  {
    updateTimestamp(findColumn(columnName), x);
  }
  
  public boolean wasNull()
    throws SQLException
  {
    return this.wasNullFlag;
  }
  
  protected Calendar getGmtCalendar()
  {
    if (this.gmtCalendar == null) {
      this.gmtCalendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
    }
    return this.gmtCalendar;
  }
  
  protected ExceptionInterceptor getExceptionInterceptor()
  {
    return this.exceptionInterceptor;
  }
  
  /* Error */
  private Time getTimeFromString(String timeAsString, Calendar targetCalendar, int columnIndex, TimeZone tz, boolean rollForward)
    throws SQLException
  {
    // Byte code:
    //   0: iconst_0
    //   1: istore 6
    //   3: iconst_0
    //   4: istore 7
    //   6: iconst_0
    //   7: istore 8
    //   9: aload_1
    //   10: ifnonnull +10 -> 20
    //   13: aload_0
    //   14: iconst_1
    //   15: putfield 54	com/mysql/jdbc/ResultSetImpl:wasNullFlag	Z
    //   18: aconst_null
    //   19: areturn
    //   20: aload_1
    //   21: invokevirtual 249	java/lang/String:trim	()Ljava/lang/String;
    //   24: astore_1
    //   25: aload_1
    //   26: ldc -101
    //   28: invokevirtual 244	java/lang/String:equals	(Ljava/lang/Object;)Z
    //   31: ifne +33 -> 64
    //   34: aload_1
    //   35: ldc_w 293
    //   38: invokevirtual 244	java/lang/String:equals	(Ljava/lang/Object;)Z
    //   41: ifne +23 -> 64
    //   44: aload_1
    //   45: ldc_w 294
    //   48: invokevirtual 244	java/lang/String:equals	(Ljava/lang/Object;)Z
    //   51: ifne +13 -> 64
    //   54: aload_1
    //   55: ldc_w 295
    //   58: invokevirtual 244	java/lang/String:equals	(Ljava/lang/Object;)Z
    //   61: ifeq +87 -> 148
    //   64: ldc_w 296
    //   67: aload_0
    //   68: getfield 64	com/mysql/jdbc/ResultSetImpl:connection	Lcom/mysql/jdbc/ConnectionImpl;
    //   71: invokevirtual 297	com/mysql/jdbc/ConnectionImpl:getZeroDateTimeBehavior	()Ljava/lang/String;
    //   74: invokevirtual 244	java/lang/String:equals	(Ljava/lang/Object;)Z
    //   77: ifeq +10 -> 87
    //   80: aload_0
    //   81: iconst_1
    //   82: putfield 54	com/mysql/jdbc/ResultSetImpl:wasNullFlag	Z
    //   85: aconst_null
    //   86: areturn
    //   87: ldc_w 298
    //   90: aload_0
    //   91: getfield 64	com/mysql/jdbc/ResultSetImpl:connection	Lcom/mysql/jdbc/ConnectionImpl;
    //   94: invokevirtual 297	com/mysql/jdbc/ConnectionImpl:getZeroDateTimeBehavior	()Ljava/lang/String;
    //   97: invokevirtual 244	java/lang/String:equals	(Ljava/lang/Object;)Z
    //   100: ifeq +39 -> 139
    //   103: new 182	java/lang/StringBuffer
    //   106: dup
    //   107: invokespecial 183	java/lang/StringBuffer:<init>	()V
    //   110: ldc_w 299
    //   113: invokevirtual 185	java/lang/StringBuffer:append	(Ljava/lang/String;)Ljava/lang/StringBuffer;
    //   116: aload_1
    //   117: invokevirtual 185	java/lang/StringBuffer:append	(Ljava/lang/String;)Ljava/lang/StringBuffer;
    //   120: ldc_w 552
    //   123: invokevirtual 185	java/lang/StringBuffer:append	(Ljava/lang/String;)Ljava/lang/StringBuffer;
    //   126: invokevirtual 187	java/lang/StringBuffer:toString	()Ljava/lang/String;
    //   129: ldc 118
    //   131: aload_0
    //   132: invokevirtual 119	com/mysql/jdbc/ResultSetImpl:getExceptionInterceptor	()Lcom/mysql/jdbc/ExceptionInterceptor;
    //   135: invokestatic 120	com/mysql/jdbc/SQLError:createSQLException	(Ljava/lang/String;Ljava/lang/String;Lcom/mysql/jdbc/ExceptionInterceptor;)Ljava/sql/SQLException;
    //   138: athrow
    //   139: aload_0
    //   140: aload_2
    //   141: iconst_0
    //   142: iconst_0
    //   143: iconst_0
    //   144: invokevirtual 553	com/mysql/jdbc/ResultSetImpl:fastTimeCreate	(Ljava/util/Calendar;III)Ljava/sql/Time;
    //   147: areturn
    //   148: aload_0
    //   149: iconst_0
    //   150: putfield 54	com/mysql/jdbc/ResultSetImpl:wasNullFlag	Z
    //   153: aload_0
    //   154: getfield 63	com/mysql/jdbc/ResultSetImpl:fields	[Lcom/mysql/jdbc/Field;
    //   157: iload_3
    //   158: iconst_1
    //   159: isub
    //   160: aaload
    //   161: astore 9
    //   163: aload 9
    //   165: invokevirtual 211	com/mysql/jdbc/Field:getMysqlType	()I
    //   168: bipush 7
    //   170: if_icmpne +344 -> 514
    //   173: aload_1
    //   174: invokevirtual 200	java/lang/String:length	()I
    //   177: istore 10
    //   179: iload 10
    //   181: tableswitch	default:+194->375, 10:+162->343, 11:+194->375, 12:+109->290, 13:+194->375, 14:+109->290, 15:+194->375, 16:+194->375, 17:+194->375, 18:+194->375, 19:+55->236
    //   236: aload_1
    //   237: iload 10
    //   239: bipush 8
    //   241: isub
    //   242: iload 10
    //   244: bipush 6
    //   246: isub
    //   247: invokevirtual 302	java/lang/String:substring	(II)Ljava/lang/String;
    //   250: invokestatic 303	java/lang/Integer:parseInt	(Ljava/lang/String;)I
    //   253: istore 6
    //   255: aload_1
    //   256: iload 10
    //   258: iconst_5
    //   259: isub
    //   260: iload 10
    //   262: iconst_3
    //   263: isub
    //   264: invokevirtual 302	java/lang/String:substring	(II)Ljava/lang/String;
    //   267: invokestatic 303	java/lang/Integer:parseInt	(Ljava/lang/String;)I
    //   270: istore 7
    //   272: aload_1
    //   273: iload 10
    //   275: iconst_2
    //   276: isub
    //   277: iload 10
    //   279: invokevirtual 302	java/lang/String:substring	(II)Ljava/lang/String;
    //   282: invokestatic 303	java/lang/Integer:parseInt	(Ljava/lang/String;)I
    //   285: istore 8
    //   287: goto +144 -> 431
    //   290: aload_1
    //   291: iload 10
    //   293: bipush 6
    //   295: isub
    //   296: iload 10
    //   298: iconst_4
    //   299: isub
    //   300: invokevirtual 302	java/lang/String:substring	(II)Ljava/lang/String;
    //   303: invokestatic 303	java/lang/Integer:parseInt	(Ljava/lang/String;)I
    //   306: istore 6
    //   308: aload_1
    //   309: iload 10
    //   311: iconst_4
    //   312: isub
    //   313: iload 10
    //   315: iconst_2
    //   316: isub
    //   317: invokevirtual 302	java/lang/String:substring	(II)Ljava/lang/String;
    //   320: invokestatic 303	java/lang/Integer:parseInt	(Ljava/lang/String;)I
    //   323: istore 7
    //   325: aload_1
    //   326: iload 10
    //   328: iconst_2
    //   329: isub
    //   330: iload 10
    //   332: invokevirtual 302	java/lang/String:substring	(II)Ljava/lang/String;
    //   335: invokestatic 303	java/lang/Integer:parseInt	(Ljava/lang/String;)I
    //   338: istore 8
    //   340: goto +91 -> 431
    //   343: aload_1
    //   344: bipush 6
    //   346: bipush 8
    //   348: invokevirtual 302	java/lang/String:substring	(II)Ljava/lang/String;
    //   351: invokestatic 303	java/lang/Integer:parseInt	(Ljava/lang/String;)I
    //   354: istore 6
    //   356: aload_1
    //   357: bipush 8
    //   359: bipush 10
    //   361: invokevirtual 302	java/lang/String:substring	(II)Ljava/lang/String;
    //   364: invokestatic 303	java/lang/Integer:parseInt	(Ljava/lang/String;)I
    //   367: istore 7
    //   369: iconst_0
    //   370: istore 8
    //   372: goto +59 -> 431
    //   375: new 182	java/lang/StringBuffer
    //   378: dup
    //   379: invokespecial 183	java/lang/StringBuffer:<init>	()V
    //   382: ldc_w 554
    //   385: invokestatic 117	com/mysql/jdbc/Messages:getString	(Ljava/lang/String;)Ljava/lang/String;
    //   388: invokevirtual 185	java/lang/StringBuffer:append	(Ljava/lang/String;)Ljava/lang/StringBuffer;
    //   391: iload_3
    //   392: invokevirtual 366	java/lang/StringBuffer:append	(I)Ljava/lang/StringBuffer;
    //   395: ldc_w 555
    //   398: invokevirtual 185	java/lang/StringBuffer:append	(Ljava/lang/String;)Ljava/lang/StringBuffer;
    //   401: aload_0
    //   402: getfield 63	com/mysql/jdbc/ResultSetImpl:fields	[Lcom/mysql/jdbc/Field;
    //   405: iload_3
    //   406: iconst_1
    //   407: isub
    //   408: aaload
    //   409: invokevirtual 556	java/lang/StringBuffer:append	(Ljava/lang/Object;)Ljava/lang/StringBuffer;
    //   412: ldc_w 557
    //   415: invokevirtual 185	java/lang/StringBuffer:append	(Ljava/lang/String;)Ljava/lang/StringBuffer;
    //   418: invokevirtual 187	java/lang/StringBuffer:toString	()Ljava/lang/String;
    //   421: ldc 118
    //   423: aload_0
    //   424: invokevirtual 119	com/mysql/jdbc/ResultSetImpl:getExceptionInterceptor	()Lcom/mysql/jdbc/ExceptionInterceptor;
    //   427: invokestatic 120	com/mysql/jdbc/SQLError:createSQLException	(Ljava/lang/String;Ljava/lang/String;Lcom/mysql/jdbc/ExceptionInterceptor;)Ljava/sql/SQLException;
    //   430: athrow
    //   431: new 558	java/sql/SQLWarning
    //   434: dup
    //   435: new 182	java/lang/StringBuffer
    //   438: dup
    //   439: invokespecial 183	java/lang/StringBuffer:<init>	()V
    //   442: ldc_w 559
    //   445: invokestatic 117	com/mysql/jdbc/Messages:getString	(Ljava/lang/String;)Ljava/lang/String;
    //   448: invokevirtual 185	java/lang/StringBuffer:append	(Ljava/lang/String;)Ljava/lang/StringBuffer;
    //   451: iload_3
    //   452: invokevirtual 366	java/lang/StringBuffer:append	(I)Ljava/lang/StringBuffer;
    //   455: ldc_w 555
    //   458: invokevirtual 185	java/lang/StringBuffer:append	(Ljava/lang/String;)Ljava/lang/StringBuffer;
    //   461: aload_0
    //   462: getfield 63	com/mysql/jdbc/ResultSetImpl:fields	[Lcom/mysql/jdbc/Field;
    //   465: iload_3
    //   466: iconst_1
    //   467: isub
    //   468: aaload
    //   469: invokevirtual 556	java/lang/StringBuffer:append	(Ljava/lang/Object;)Ljava/lang/StringBuffer;
    //   472: ldc_w 557
    //   475: invokevirtual 185	java/lang/StringBuffer:append	(Ljava/lang/String;)Ljava/lang/StringBuffer;
    //   478: invokevirtual 187	java/lang/StringBuffer:toString	()Ljava/lang/String;
    //   481: invokespecial 560	java/sql/SQLWarning:<init>	(Ljava/lang/String;)V
    //   484: astore 11
    //   486: aload_0
    //   487: getfield 53	com/mysql/jdbc/ResultSetImpl:warningChain	Ljava/sql/SQLWarning;
    //   490: ifnonnull +12 -> 502
    //   493: aload_0
    //   494: aload 11
    //   496: putfield 53	com/mysql/jdbc/ResultSetImpl:warningChain	Ljava/sql/SQLWarning;
    //   499: goto +12 -> 511
    //   502: aload_0
    //   503: getfield 53	com/mysql/jdbc/ResultSetImpl:warningChain	Ljava/sql/SQLWarning;
    //   506: aload 11
    //   508: invokevirtual 561	java/sql/SQLWarning:setNextWarning	(Ljava/sql/SQLWarning;)V
    //   511: goto +262 -> 773
    //   514: aload 9
    //   516: invokevirtual 211	com/mysql/jdbc/Field:getMysqlType	()I
    //   519: bipush 12
    //   521: if_icmpne +125 -> 646
    //   524: aload_1
    //   525: bipush 11
    //   527: bipush 13
    //   529: invokevirtual 302	java/lang/String:substring	(II)Ljava/lang/String;
    //   532: invokestatic 303	java/lang/Integer:parseInt	(Ljava/lang/String;)I
    //   535: istore 6
    //   537: aload_1
    //   538: bipush 14
    //   540: bipush 16
    //   542: invokevirtual 302	java/lang/String:substring	(II)Ljava/lang/String;
    //   545: invokestatic 303	java/lang/Integer:parseInt	(Ljava/lang/String;)I
    //   548: istore 7
    //   550: aload_1
    //   551: bipush 17
    //   553: bipush 19
    //   555: invokevirtual 302	java/lang/String:substring	(II)Ljava/lang/String;
    //   558: invokestatic 303	java/lang/Integer:parseInt	(Ljava/lang/String;)I
    //   561: istore 8
    //   563: new 558	java/sql/SQLWarning
    //   566: dup
    //   567: new 182	java/lang/StringBuffer
    //   570: dup
    //   571: invokespecial 183	java/lang/StringBuffer:<init>	()V
    //   574: ldc_w 562
    //   577: invokestatic 117	com/mysql/jdbc/Messages:getString	(Ljava/lang/String;)Ljava/lang/String;
    //   580: invokevirtual 185	java/lang/StringBuffer:append	(Ljava/lang/String;)Ljava/lang/StringBuffer;
    //   583: iload_3
    //   584: invokevirtual 366	java/lang/StringBuffer:append	(I)Ljava/lang/StringBuffer;
    //   587: ldc_w 555
    //   590: invokevirtual 185	java/lang/StringBuffer:append	(Ljava/lang/String;)Ljava/lang/StringBuffer;
    //   593: aload_0
    //   594: getfield 63	com/mysql/jdbc/ResultSetImpl:fields	[Lcom/mysql/jdbc/Field;
    //   597: iload_3
    //   598: iconst_1
    //   599: isub
    //   600: aaload
    //   601: invokevirtual 556	java/lang/StringBuffer:append	(Ljava/lang/Object;)Ljava/lang/StringBuffer;
    //   604: ldc_w 557
    //   607: invokevirtual 185	java/lang/StringBuffer:append	(Ljava/lang/String;)Ljava/lang/StringBuffer;
    //   610: invokevirtual 187	java/lang/StringBuffer:toString	()Ljava/lang/String;
    //   613: invokespecial 560	java/sql/SQLWarning:<init>	(Ljava/lang/String;)V
    //   616: astore 10
    //   618: aload_0
    //   619: getfield 53	com/mysql/jdbc/ResultSetImpl:warningChain	Ljava/sql/SQLWarning;
    //   622: ifnonnull +12 -> 634
    //   625: aload_0
    //   626: aload 10
    //   628: putfield 53	com/mysql/jdbc/ResultSetImpl:warningChain	Ljava/sql/SQLWarning;
    //   631: goto +12 -> 643
    //   634: aload_0
    //   635: getfield 53	com/mysql/jdbc/ResultSetImpl:warningChain	Ljava/sql/SQLWarning;
    //   638: aload 10
    //   640: invokevirtual 561	java/sql/SQLWarning:setNextWarning	(Ljava/sql/SQLWarning;)V
    //   643: goto +130 -> 773
    //   646: aload 9
    //   648: invokevirtual 211	com/mysql/jdbc/Field:getMysqlType	()I
    //   651: bipush 10
    //   653: if_icmpne +12 -> 665
    //   656: aload_0
    //   657: aload_2
    //   658: iconst_0
    //   659: iconst_0
    //   660: iconst_0
    //   661: invokevirtual 553	com/mysql/jdbc/ResultSetImpl:fastTimeCreate	(Ljava/util/Calendar;III)Ljava/sql/Time;
    //   664: areturn
    //   665: aload_1
    //   666: invokevirtual 200	java/lang/String:length	()I
    //   669: iconst_5
    //   670: if_icmpeq +58 -> 728
    //   673: aload_1
    //   674: invokevirtual 200	java/lang/String:length	()I
    //   677: bipush 8
    //   679: if_icmpeq +49 -> 728
    //   682: new 182	java/lang/StringBuffer
    //   685: dup
    //   686: invokespecial 183	java/lang/StringBuffer:<init>	()V
    //   689: ldc_w 563
    //   692: invokestatic 117	com/mysql/jdbc/Messages:getString	(Ljava/lang/String;)Ljava/lang/String;
    //   695: invokevirtual 185	java/lang/StringBuffer:append	(Ljava/lang/String;)Ljava/lang/StringBuffer;
    //   698: aload_1
    //   699: invokevirtual 185	java/lang/StringBuffer:append	(Ljava/lang/String;)Ljava/lang/StringBuffer;
    //   702: ldc_w 564
    //   705: invokestatic 117	com/mysql/jdbc/Messages:getString	(Ljava/lang/String;)Ljava/lang/String;
    //   708: invokevirtual 185	java/lang/StringBuffer:append	(Ljava/lang/String;)Ljava/lang/StringBuffer;
    //   711: iload_3
    //   712: invokevirtual 366	java/lang/StringBuffer:append	(I)Ljava/lang/StringBuffer;
    //   715: invokevirtual 187	java/lang/StringBuffer:toString	()Ljava/lang/String;
    //   718: ldc 118
    //   720: aload_0
    //   721: invokevirtual 119	com/mysql/jdbc/ResultSetImpl:getExceptionInterceptor	()Lcom/mysql/jdbc/ExceptionInterceptor;
    //   724: invokestatic 120	com/mysql/jdbc/SQLError:createSQLException	(Ljava/lang/String;Ljava/lang/String;Lcom/mysql/jdbc/ExceptionInterceptor;)Ljava/sql/SQLException;
    //   727: athrow
    //   728: aload_1
    //   729: iconst_0
    //   730: iconst_2
    //   731: invokevirtual 302	java/lang/String:substring	(II)Ljava/lang/String;
    //   734: invokestatic 303	java/lang/Integer:parseInt	(Ljava/lang/String;)I
    //   737: istore 6
    //   739: aload_1
    //   740: iconst_3
    //   741: iconst_5
    //   742: invokevirtual 302	java/lang/String:substring	(II)Ljava/lang/String;
    //   745: invokestatic 303	java/lang/Integer:parseInt	(Ljava/lang/String;)I
    //   748: istore 7
    //   750: aload_1
    //   751: invokevirtual 200	java/lang/String:length	()I
    //   754: iconst_5
    //   755: if_icmpne +7 -> 762
    //   758: iconst_0
    //   759: goto +12 -> 771
    //   762: aload_1
    //   763: bipush 6
    //   765: invokevirtual 565	java/lang/String:substring	(I)Ljava/lang/String;
    //   768: invokestatic 303	java/lang/Integer:parseInt	(Ljava/lang/String;)I
    //   771: istore 8
    //   773: aload_0
    //   774: invokevirtual 566	com/mysql/jdbc/ResultSetImpl:getCalendarInstanceForSessionOrNew	()Ljava/util/Calendar;
    //   777: astore 10
    //   779: aload 10
    //   781: dup
    //   782: astore 11
    //   784: monitorenter
    //   785: aload_0
    //   786: getfield 64	com/mysql/jdbc/ResultSetImpl:connection	Lcom/mysql/jdbc/ConnectionImpl;
    //   789: aload 10
    //   791: aload_2
    //   792: aload_0
    //   793: aload 10
    //   795: iload 6
    //   797: iload 7
    //   799: iload 8
    //   801: invokevirtual 553	com/mysql/jdbc/ResultSetImpl:fastTimeCreate	(Ljava/util/Calendar;III)Ljava/sql/Time;
    //   804: aload_0
    //   805: getfield 64	com/mysql/jdbc/ResultSetImpl:connection	Lcom/mysql/jdbc/ConnectionImpl;
    //   808: invokevirtual 70	com/mysql/jdbc/ConnectionImpl:getServerTimezoneTZ	()Ljava/util/TimeZone;
    //   811: aload 4
    //   813: iload 5
    //   815: invokestatic 567	com/mysql/jdbc/TimeUtil:changeTimezone	(Lcom/mysql/jdbc/ConnectionImpl;Ljava/util/Calendar;Ljava/util/Calendar;Ljava/sql/Time;Ljava/util/TimeZone;Ljava/util/TimeZone;Z)Ljava/sql/Time;
    //   818: aload 11
    //   820: monitorexit
    //   821: areturn
    //   822: astore 12
    //   824: aload 11
    //   826: monitorexit
    //   827: aload 12
    //   829: athrow
    //   830: astore 9
    //   832: aload 9
    //   834: invokevirtual 568	java/lang/Exception:toString	()Ljava/lang/String;
    //   837: ldc 118
    //   839: aload_0
    //   840: invokevirtual 119	com/mysql/jdbc/ResultSetImpl:getExceptionInterceptor	()Lcom/mysql/jdbc/ExceptionInterceptor;
    //   843: invokestatic 120	com/mysql/jdbc/SQLError:createSQLException	(Ljava/lang/String;Ljava/lang/String;Lcom/mysql/jdbc/ExceptionInterceptor;)Ljava/sql/SQLException;
    //   846: astore 10
    //   848: aload 10
    //   850: aload 9
    //   852: invokevirtual 310	java/sql/SQLException:initCause	(Ljava/lang/Throwable;)Ljava/lang/Throwable;
    //   855: pop
    //   856: aload 10
    //   858: athrow
    // Line number table:
    //   Java source line #5785	-> byte code offset #0
    //   Java source line #5786	-> byte code offset #3
    //   Java source line #5787	-> byte code offset #6
    //   Java source line #5791	-> byte code offset #9
    //   Java source line #5792	-> byte code offset #13
    //   Java source line #5794	-> byte code offset #18
    //   Java source line #5805	-> byte code offset #20
    //   Java source line #5807	-> byte code offset #25
    //   Java source line #5811	-> byte code offset #64
    //   Java source line #5813	-> byte code offset #80
    //   Java source line #5815	-> byte code offset #85
    //   Java source line #5816	-> byte code offset #87
    //   Java source line #5818	-> byte code offset #103
    //   Java source line #5825	-> byte code offset #139
    //   Java source line #5828	-> byte code offset #148
    //   Java source line #5830	-> byte code offset #153
    //   Java source line #5832	-> byte code offset #163
    //   Java source line #5834	-> byte code offset #173
    //   Java source line #5836	-> byte code offset #179
    //   Java source line #5839	-> byte code offset #236
    //   Java source line #5841	-> byte code offset #255
    //   Java source line #5843	-> byte code offset #272
    //   Java source line #5847	-> byte code offset #287
    //   Java source line #5850	-> byte code offset #290
    //   Java source line #5852	-> byte code offset #308
    //   Java source line #5854	-> byte code offset #325
    //   Java source line #5858	-> byte code offset #340
    //   Java source line #5861	-> byte code offset #343
    //   Java source line #5862	-> byte code offset #356
    //   Java source line #5863	-> byte code offset #369
    //   Java source line #5866	-> byte code offset #372
    //   Java source line #5869	-> byte code offset #375
    //   Java source line #5878	-> byte code offset #431
    //   Java source line #5885	-> byte code offset #486
    //   Java source line #5886	-> byte code offset #493
    //   Java source line #5888	-> byte code offset #502
    //   Java source line #5890	-> byte code offset #514
    //   Java source line #5891	-> byte code offset #524
    //   Java source line #5892	-> byte code offset #537
    //   Java source line #5893	-> byte code offset #550
    //   Java source line #5895	-> byte code offset #563
    //   Java source line #5902	-> byte code offset #618
    //   Java source line #5903	-> byte code offset #625
    //   Java source line #5905	-> byte code offset #634
    //   Java source line #5907	-> byte code offset #646
    //   Java source line #5908	-> byte code offset #656
    //   Java source line #5912	-> byte code offset #665
    //   Java source line #5914	-> byte code offset #682
    //   Java source line #5921	-> byte code offset #728
    //   Java source line #5922	-> byte code offset #739
    //   Java source line #5923	-> byte code offset #750
    //   Java source line #5927	-> byte code offset #773
    //   Java source line #5929	-> byte code offset #779
    //   Java source line #5930	-> byte code offset #785
    //   Java source line #5937	-> byte code offset #822
    //   Java source line #5938	-> byte code offset #830
    //   Java source line #5939	-> byte code offset #832
    //   Java source line #5941	-> byte code offset #848
    //   Java source line #5943	-> byte code offset #856
    // Local variable table:
    //   start	length	slot	name	signature
    //   0	859	0	this	ResultSetImpl
    //   0	859	1	timeAsString	String
    //   0	859	2	targetCalendar	Calendar
    //   0	859	3	columnIndex	int
    //   0	859	4	tz	TimeZone
    //   0	859	5	rollForward	boolean
    //   1	795	6	hr	int
    //   4	794	7	min	int
    //   7	793	8	sec	int
    //   161	486	9	timeColField	Field
    //   830	21	9	ex	Exception
    //   177	154	10	length	int
    //   616	23	10	precisionLost	SQLWarning
    //   777	17	10	sessionCalendar	Calendar
    //   846	11	10	sqlEx	SQLException
    //   484	23	11	precisionLost	SQLWarning
    //   782	43	11	Ljava/lang/Object;	Object
    //   822	6	12	localObject1	Object
    // Exception table:
    //   from	to	target	type
    //   785	821	822	finally
    //   822	827	822	finally
    //   9	19	830	java/lang/Exception
    //   20	86	830	java/lang/Exception
    //   87	147	830	java/lang/Exception
    //   148	664	830	java/lang/Exception
    //   665	821	830	java/lang/Exception
    //   822	830	830	java/lang/Exception
  }
  
  /* Error */
  private Timestamp getTimestampFromString(int columnIndex, Calendar targetCalendar, String timestampValue, TimeZone tz, boolean rollForward)
    throws SQLException
  {
    // Byte code:
    //   0: aload_0
    //   1: iconst_0
    //   2: putfield 54	com/mysql/jdbc/ResultSetImpl:wasNullFlag	Z
    //   5: aload_3
    //   6: ifnonnull +10 -> 16
    //   9: aload_0
    //   10: iconst_1
    //   11: putfield 54	com/mysql/jdbc/ResultSetImpl:wasNullFlag	Z
    //   14: aconst_null
    //   15: areturn
    //   16: aload_3
    //   17: invokevirtual 249	java/lang/String:trim	()Ljava/lang/String;
    //   20: astore_3
    //   21: aload_3
    //   22: invokevirtual 200	java/lang/String:length	()I
    //   25: istore 6
    //   27: aload_0
    //   28: getfield 64	com/mysql/jdbc/ResultSetImpl:connection	Lcom/mysql/jdbc/ConnectionImpl;
    //   31: invokevirtual 572	com/mysql/jdbc/ConnectionImpl:getUseJDBCCompliantTimezoneShift	()Z
    //   34: ifeq +13 -> 47
    //   37: aload_0
    //   38: getfield 64	com/mysql/jdbc/ResultSetImpl:connection	Lcom/mysql/jdbc/ConnectionImpl;
    //   41: invokevirtual 573	com/mysql/jdbc/ConnectionImpl:getUtcCalendar	()Ljava/util/Calendar;
    //   44: goto +7 -> 51
    //   47: aload_0
    //   48: invokevirtual 566	com/mysql/jdbc/ResultSetImpl:getCalendarInstanceForSessionOrNew	()Ljava/util/Calendar;
    //   51: astore 7
    //   53: aload 7
    //   55: dup
    //   56: astore 8
    //   58: monitorenter
    //   59: iload 6
    //   61: ifle +146 -> 207
    //   64: aload_3
    //   65: iconst_0
    //   66: invokevirtual 241	java/lang/String:charAt	(I)C
    //   69: bipush 48
    //   71: if_icmpne +136 -> 207
    //   74: aload_3
    //   75: ldc_w 293
    //   78: invokevirtual 244	java/lang/String:equals	(Ljava/lang/Object;)Z
    //   81: ifne +32 -> 113
    //   84: aload_3
    //   85: ldc_w 294
    //   88: invokevirtual 244	java/lang/String:equals	(Ljava/lang/Object;)Z
    //   91: ifne +22 -> 113
    //   94: aload_3
    //   95: ldc_w 295
    //   98: invokevirtual 244	java/lang/String:equals	(Ljava/lang/Object;)Z
    //   101: ifne +12 -> 113
    //   104: aload_3
    //   105: ldc -101
    //   107: invokevirtual 244	java/lang/String:equals	(Ljava/lang/Object;)Z
    //   110: ifeq +97 -> 207
    //   113: ldc_w 296
    //   116: aload_0
    //   117: getfield 64	com/mysql/jdbc/ResultSetImpl:connection	Lcom/mysql/jdbc/ConnectionImpl;
    //   120: invokevirtual 297	com/mysql/jdbc/ConnectionImpl:getZeroDateTimeBehavior	()Ljava/lang/String;
    //   123: invokevirtual 244	java/lang/String:equals	(Ljava/lang/Object;)Z
    //   126: ifeq +13 -> 139
    //   129: aload_0
    //   130: iconst_1
    //   131: putfield 54	com/mysql/jdbc/ResultSetImpl:wasNullFlag	Z
    //   134: aconst_null
    //   135: aload 8
    //   137: monitorexit
    //   138: areturn
    //   139: ldc_w 298
    //   142: aload_0
    //   143: getfield 64	com/mysql/jdbc/ResultSetImpl:connection	Lcom/mysql/jdbc/ConnectionImpl;
    //   146: invokevirtual 297	com/mysql/jdbc/ConnectionImpl:getZeroDateTimeBehavior	()Ljava/lang/String;
    //   149: invokevirtual 244	java/lang/String:equals	(Ljava/lang/Object;)Z
    //   152: ifeq +39 -> 191
    //   155: new 182	java/lang/StringBuffer
    //   158: dup
    //   159: invokespecial 183	java/lang/StringBuffer:<init>	()V
    //   162: ldc_w 299
    //   165: invokevirtual 185	java/lang/StringBuffer:append	(Ljava/lang/String;)Ljava/lang/StringBuffer;
    //   168: aload_3
    //   169: invokevirtual 185	java/lang/StringBuffer:append	(Ljava/lang/String;)Ljava/lang/StringBuffer;
    //   172: ldc_w 574
    //   175: invokevirtual 185	java/lang/StringBuffer:append	(Ljava/lang/String;)Ljava/lang/StringBuffer;
    //   178: invokevirtual 187	java/lang/StringBuffer:toString	()Ljava/lang/String;
    //   181: ldc 118
    //   183: aload_0
    //   184: invokevirtual 119	com/mysql/jdbc/ResultSetImpl:getExceptionInterceptor	()Lcom/mysql/jdbc/ExceptionInterceptor;
    //   187: invokestatic 120	com/mysql/jdbc/SQLError:createSQLException	(Ljava/lang/String;Ljava/lang/String;Lcom/mysql/jdbc/ExceptionInterceptor;)Ljava/sql/SQLException;
    //   190: athrow
    //   191: aload_0
    //   192: aconst_null
    //   193: iconst_1
    //   194: iconst_1
    //   195: iconst_1
    //   196: iconst_0
    //   197: iconst_0
    //   198: iconst_0
    //   199: iconst_0
    //   200: invokevirtual 575	com/mysql/jdbc/ResultSetImpl:fastTimestampCreate	(Ljava/util/Calendar;IIIIIII)Ljava/sql/Timestamp;
    //   203: aload 8
    //   205: monitorexit
    //   206: areturn
    //   207: aload_0
    //   208: getfield 63	com/mysql/jdbc/ResultSetImpl:fields	[Lcom/mysql/jdbc/Field;
    //   211: iload_1
    //   212: iconst_1
    //   213: isub
    //   214: aaload
    //   215: invokevirtual 211	com/mysql/jdbc/Field:getMysqlType	()I
    //   218: bipush 13
    //   220: if_icmpne +80 -> 300
    //   223: aload_0
    //   224: getfield 74	com/mysql/jdbc/ResultSetImpl:useLegacyDatetimeCode	Z
    //   227: ifne +27 -> 254
    //   230: aload 4
    //   232: aload_3
    //   233: iconst_0
    //   234: iconst_4
    //   235: invokevirtual 302	java/lang/String:substring	(II)Ljava/lang/String;
    //   238: invokestatic 303	java/lang/Integer:parseInt	(Ljava/lang/String;)I
    //   241: iconst_1
    //   242: iconst_1
    //   243: iconst_0
    //   244: iconst_0
    //   245: iconst_0
    //   246: iconst_0
    //   247: invokestatic 175	com/mysql/jdbc/TimeUtil:fastTimestampCreate	(Ljava/util/TimeZone;IIIIIII)Ljava/sql/Timestamp;
    //   250: aload 8
    //   252: monitorexit
    //   253: areturn
    //   254: aload_0
    //   255: getfield 64	com/mysql/jdbc/ResultSetImpl:connection	Lcom/mysql/jdbc/ConnectionImpl;
    //   258: aload 7
    //   260: aload_2
    //   261: aload_0
    //   262: aload 7
    //   264: aload_3
    //   265: iconst_0
    //   266: iconst_4
    //   267: invokevirtual 302	java/lang/String:substring	(II)Ljava/lang/String;
    //   270: invokestatic 303	java/lang/Integer:parseInt	(Ljava/lang/String;)I
    //   273: iconst_1
    //   274: iconst_1
    //   275: iconst_0
    //   276: iconst_0
    //   277: iconst_0
    //   278: iconst_0
    //   279: invokevirtual 575	com/mysql/jdbc/ResultSetImpl:fastTimestampCreate	(Ljava/util/Calendar;IIIIIII)Ljava/sql/Timestamp;
    //   282: aload_0
    //   283: getfield 64	com/mysql/jdbc/ResultSetImpl:connection	Lcom/mysql/jdbc/ConnectionImpl;
    //   286: invokevirtual 70	com/mysql/jdbc/ConnectionImpl:getServerTimezoneTZ	()Ljava/util/TimeZone;
    //   289: aload 4
    //   291: iload 5
    //   293: invokestatic 576	com/mysql/jdbc/TimeUtil:changeTimezone	(Lcom/mysql/jdbc/ConnectionImpl;Ljava/util/Calendar;Ljava/util/Calendar;Ljava/sql/Timestamp;Ljava/util/TimeZone;Ljava/util/TimeZone;Z)Ljava/sql/Timestamp;
    //   296: aload 8
    //   298: monitorexit
    //   299: areturn
    //   300: aload_3
    //   301: ldc -6
    //   303: invokevirtual 446	java/lang/String:endsWith	(Ljava/lang/String;)Z
    //   306: ifeq +15 -> 321
    //   309: aload_3
    //   310: iconst_0
    //   311: aload_3
    //   312: invokevirtual 200	java/lang/String:length	()I
    //   315: iconst_1
    //   316: isub
    //   317: invokevirtual 302	java/lang/String:substring	(II)Ljava/lang/String;
    //   320: astore_3
    //   321: iconst_0
    //   322: istore 9
    //   324: iconst_0
    //   325: istore 10
    //   327: iconst_0
    //   328: istore 11
    //   330: iconst_0
    //   331: istore 12
    //   333: iconst_0
    //   334: istore 13
    //   336: iconst_0
    //   337: istore 14
    //   339: iconst_0
    //   340: istore 15
    //   342: iload 6
    //   344: tableswitch	default:+883->1227, 2:+841->1185, 3:+883->1227, 4:+791->1135, 5:+883->1227, 6:+732->1076, 7:+883->1227, 8:+622->966, 9:+883->1227, 10:+465->809, 11:+883->1227, 12:+367->711, 13:+883->1227, 14:+289->633, 15:+883->1227, 16:+883->1227, 17:+883->1227, 18:+883->1227, 19:+116->460, 20:+116->460, 21:+116->460, 22:+116->460, 23:+116->460, 24:+116->460, 25:+116->460, 26:+116->460
    //   460: aload_3
    //   461: iconst_0
    //   462: iconst_4
    //   463: invokevirtual 302	java/lang/String:substring	(II)Ljava/lang/String;
    //   466: invokestatic 303	java/lang/Integer:parseInt	(Ljava/lang/String;)I
    //   469: istore 9
    //   471: aload_3
    //   472: iconst_5
    //   473: bipush 7
    //   475: invokevirtual 302	java/lang/String:substring	(II)Ljava/lang/String;
    //   478: invokestatic 303	java/lang/Integer:parseInt	(Ljava/lang/String;)I
    //   481: istore 10
    //   483: aload_3
    //   484: bipush 8
    //   486: bipush 10
    //   488: invokevirtual 302	java/lang/String:substring	(II)Ljava/lang/String;
    //   491: invokestatic 303	java/lang/Integer:parseInt	(Ljava/lang/String;)I
    //   494: istore 11
    //   496: aload_3
    //   497: bipush 11
    //   499: bipush 13
    //   501: invokevirtual 302	java/lang/String:substring	(II)Ljava/lang/String;
    //   504: invokestatic 303	java/lang/Integer:parseInt	(Ljava/lang/String;)I
    //   507: istore 12
    //   509: aload_3
    //   510: bipush 14
    //   512: bipush 16
    //   514: invokevirtual 302	java/lang/String:substring	(II)Ljava/lang/String;
    //   517: invokestatic 303	java/lang/Integer:parseInt	(Ljava/lang/String;)I
    //   520: istore 13
    //   522: aload_3
    //   523: bipush 17
    //   525: bipush 19
    //   527: invokevirtual 302	java/lang/String:substring	(II)Ljava/lang/String;
    //   530: invokestatic 303	java/lang/Integer:parseInt	(Ljava/lang/String;)I
    //   533: istore 14
    //   535: iconst_0
    //   536: istore 15
    //   538: iload 6
    //   540: bipush 19
    //   542: if_icmple +730 -> 1272
    //   545: aload_3
    //   546: bipush 46
    //   548: invokevirtual 577	java/lang/String:lastIndexOf	(I)I
    //   551: istore 16
    //   553: iload 16
    //   555: iconst_m1
    //   556: if_icmpeq +74 -> 630
    //   559: iload 16
    //   561: iconst_2
    //   562: iadd
    //   563: iload 6
    //   565: if_icmpgt +57 -> 622
    //   568: aload_3
    //   569: iload 16
    //   571: iconst_1
    //   572: iadd
    //   573: invokevirtual 565	java/lang/String:substring	(I)Ljava/lang/String;
    //   576: invokestatic 303	java/lang/Integer:parseInt	(Ljava/lang/String;)I
    //   579: istore 15
    //   581: iload 6
    //   583: iload 16
    //   585: iconst_1
    //   586: iadd
    //   587: isub
    //   588: istore 17
    //   590: iload 17
    //   592: bipush 9
    //   594: if_icmpge +25 -> 619
    //   597: ldc2_w 578
    //   600: bipush 9
    //   602: iload 17
    //   604: isub
    //   605: i2d
    //   606: invokestatic 580	java/lang/Math:pow	(DD)D
    //   609: d2i
    //   610: istore 18
    //   612: iload 15
    //   614: iload 18
    //   616: imul
    //   617: istore 15
    //   619: goto +11 -> 630
    //   622: new 581	java/lang/IllegalArgumentException
    //   625: dup
    //   626: invokespecial 582	java/lang/IllegalArgumentException:<init>	()V
    //   629: athrow
    //   630: goto +642 -> 1272
    //   633: aload_3
    //   634: iconst_0
    //   635: iconst_4
    //   636: invokevirtual 302	java/lang/String:substring	(II)Ljava/lang/String;
    //   639: invokestatic 303	java/lang/Integer:parseInt	(Ljava/lang/String;)I
    //   642: istore 9
    //   644: aload_3
    //   645: iconst_4
    //   646: bipush 6
    //   648: invokevirtual 302	java/lang/String:substring	(II)Ljava/lang/String;
    //   651: invokestatic 303	java/lang/Integer:parseInt	(Ljava/lang/String;)I
    //   654: istore 10
    //   656: aload_3
    //   657: bipush 6
    //   659: bipush 8
    //   661: invokevirtual 302	java/lang/String:substring	(II)Ljava/lang/String;
    //   664: invokestatic 303	java/lang/Integer:parseInt	(Ljava/lang/String;)I
    //   667: istore 11
    //   669: aload_3
    //   670: bipush 8
    //   672: bipush 10
    //   674: invokevirtual 302	java/lang/String:substring	(II)Ljava/lang/String;
    //   677: invokestatic 303	java/lang/Integer:parseInt	(Ljava/lang/String;)I
    //   680: istore 12
    //   682: aload_3
    //   683: bipush 10
    //   685: bipush 12
    //   687: invokevirtual 302	java/lang/String:substring	(II)Ljava/lang/String;
    //   690: invokestatic 303	java/lang/Integer:parseInt	(Ljava/lang/String;)I
    //   693: istore 13
    //   695: aload_3
    //   696: bipush 12
    //   698: bipush 14
    //   700: invokevirtual 302	java/lang/String:substring	(II)Ljava/lang/String;
    //   703: invokestatic 303	java/lang/Integer:parseInt	(Ljava/lang/String;)I
    //   706: istore 14
    //   708: goto +564 -> 1272
    //   711: aload_3
    //   712: iconst_0
    //   713: iconst_2
    //   714: invokevirtual 302	java/lang/String:substring	(II)Ljava/lang/String;
    //   717: invokestatic 303	java/lang/Integer:parseInt	(Ljava/lang/String;)I
    //   720: istore 9
    //   722: iload 9
    //   724: bipush 69
    //   726: if_icmpgt +10 -> 736
    //   729: iload 9
    //   731: bipush 100
    //   733: iadd
    //   734: istore 9
    //   736: iload 9
    //   738: sipush 1900
    //   741: iadd
    //   742: istore 9
    //   744: aload_3
    //   745: iconst_2
    //   746: iconst_4
    //   747: invokevirtual 302	java/lang/String:substring	(II)Ljava/lang/String;
    //   750: invokestatic 303	java/lang/Integer:parseInt	(Ljava/lang/String;)I
    //   753: istore 10
    //   755: aload_3
    //   756: iconst_4
    //   757: bipush 6
    //   759: invokevirtual 302	java/lang/String:substring	(II)Ljava/lang/String;
    //   762: invokestatic 303	java/lang/Integer:parseInt	(Ljava/lang/String;)I
    //   765: istore 11
    //   767: aload_3
    //   768: bipush 6
    //   770: bipush 8
    //   772: invokevirtual 302	java/lang/String:substring	(II)Ljava/lang/String;
    //   775: invokestatic 303	java/lang/Integer:parseInt	(Ljava/lang/String;)I
    //   778: istore 12
    //   780: aload_3
    //   781: bipush 8
    //   783: bipush 10
    //   785: invokevirtual 302	java/lang/String:substring	(II)Ljava/lang/String;
    //   788: invokestatic 303	java/lang/Integer:parseInt	(Ljava/lang/String;)I
    //   791: istore 13
    //   793: aload_3
    //   794: bipush 10
    //   796: bipush 12
    //   798: invokevirtual 302	java/lang/String:substring	(II)Ljava/lang/String;
    //   801: invokestatic 303	java/lang/Integer:parseInt	(Ljava/lang/String;)I
    //   804: istore 14
    //   806: goto +466 -> 1272
    //   809: aload_0
    //   810: getfield 63	com/mysql/jdbc/ResultSetImpl:fields	[Lcom/mysql/jdbc/Field;
    //   813: iload_1
    //   814: iconst_1
    //   815: isub
    //   816: aaload
    //   817: invokevirtual 211	com/mysql/jdbc/Field:getMysqlType	()I
    //   820: bipush 10
    //   822: if_icmpeq +14 -> 836
    //   825: aload_3
    //   826: ldc_w 583
    //   829: invokevirtual 251	java/lang/String:indexOf	(Ljava/lang/String;)I
    //   832: iconst_m1
    //   833: if_icmpeq +48 -> 881
    //   836: aload_3
    //   837: iconst_0
    //   838: iconst_4
    //   839: invokevirtual 302	java/lang/String:substring	(II)Ljava/lang/String;
    //   842: invokestatic 303	java/lang/Integer:parseInt	(Ljava/lang/String;)I
    //   845: istore 9
    //   847: aload_3
    //   848: iconst_5
    //   849: bipush 7
    //   851: invokevirtual 302	java/lang/String:substring	(II)Ljava/lang/String;
    //   854: invokestatic 303	java/lang/Integer:parseInt	(Ljava/lang/String;)I
    //   857: istore 10
    //   859: aload_3
    //   860: bipush 8
    //   862: bipush 10
    //   864: invokevirtual 302	java/lang/String:substring	(II)Ljava/lang/String;
    //   867: invokestatic 303	java/lang/Integer:parseInt	(Ljava/lang/String;)I
    //   870: istore 11
    //   872: iconst_0
    //   873: istore 12
    //   875: iconst_0
    //   876: istore 13
    //   878: goto +394 -> 1272
    //   881: aload_3
    //   882: iconst_0
    //   883: iconst_2
    //   884: invokevirtual 302	java/lang/String:substring	(II)Ljava/lang/String;
    //   887: invokestatic 303	java/lang/Integer:parseInt	(Ljava/lang/String;)I
    //   890: istore 9
    //   892: iload 9
    //   894: bipush 69
    //   896: if_icmpgt +10 -> 906
    //   899: iload 9
    //   901: bipush 100
    //   903: iadd
    //   904: istore 9
    //   906: aload_3
    //   907: iconst_2
    //   908: iconst_4
    //   909: invokevirtual 302	java/lang/String:substring	(II)Ljava/lang/String;
    //   912: invokestatic 303	java/lang/Integer:parseInt	(Ljava/lang/String;)I
    //   915: istore 10
    //   917: aload_3
    //   918: iconst_4
    //   919: bipush 6
    //   921: invokevirtual 302	java/lang/String:substring	(II)Ljava/lang/String;
    //   924: invokestatic 303	java/lang/Integer:parseInt	(Ljava/lang/String;)I
    //   927: istore 11
    //   929: aload_3
    //   930: bipush 6
    //   932: bipush 8
    //   934: invokevirtual 302	java/lang/String:substring	(II)Ljava/lang/String;
    //   937: invokestatic 303	java/lang/Integer:parseInt	(Ljava/lang/String;)I
    //   940: istore 12
    //   942: aload_3
    //   943: bipush 8
    //   945: bipush 10
    //   947: invokevirtual 302	java/lang/String:substring	(II)Ljava/lang/String;
    //   950: invokestatic 303	java/lang/Integer:parseInt	(Ljava/lang/String;)I
    //   953: istore 13
    //   955: iload 9
    //   957: sipush 1900
    //   960: iadd
    //   961: istore 9
    //   963: goto +309 -> 1272
    //   966: aload_3
    //   967: ldc_w 584
    //   970: invokevirtual 251	java/lang/String:indexOf	(Ljava/lang/String;)I
    //   973: iconst_m1
    //   974: if_icmpeq +52 -> 1026
    //   977: aload_3
    //   978: iconst_0
    //   979: iconst_2
    //   980: invokevirtual 302	java/lang/String:substring	(II)Ljava/lang/String;
    //   983: invokestatic 303	java/lang/Integer:parseInt	(Ljava/lang/String;)I
    //   986: istore 12
    //   988: aload_3
    //   989: iconst_3
    //   990: iconst_5
    //   991: invokevirtual 302	java/lang/String:substring	(II)Ljava/lang/String;
    //   994: invokestatic 303	java/lang/Integer:parseInt	(Ljava/lang/String;)I
    //   997: istore 13
    //   999: aload_3
    //   1000: bipush 6
    //   1002: bipush 8
    //   1004: invokevirtual 302	java/lang/String:substring	(II)Ljava/lang/String;
    //   1007: invokestatic 303	java/lang/Integer:parseInt	(Ljava/lang/String;)I
    //   1010: istore 14
    //   1012: sipush 1970
    //   1015: istore 9
    //   1017: iconst_1
    //   1018: istore 10
    //   1020: iconst_1
    //   1021: istore 11
    //   1023: goto +249 -> 1272
    //   1026: aload_3
    //   1027: iconst_0
    //   1028: iconst_4
    //   1029: invokevirtual 302	java/lang/String:substring	(II)Ljava/lang/String;
    //   1032: invokestatic 303	java/lang/Integer:parseInt	(Ljava/lang/String;)I
    //   1035: istore 9
    //   1037: aload_3
    //   1038: iconst_4
    //   1039: bipush 6
    //   1041: invokevirtual 302	java/lang/String:substring	(II)Ljava/lang/String;
    //   1044: invokestatic 303	java/lang/Integer:parseInt	(Ljava/lang/String;)I
    //   1047: istore 10
    //   1049: aload_3
    //   1050: bipush 6
    //   1052: bipush 8
    //   1054: invokevirtual 302	java/lang/String:substring	(II)Ljava/lang/String;
    //   1057: invokestatic 303	java/lang/Integer:parseInt	(Ljava/lang/String;)I
    //   1060: istore 11
    //   1062: iload 9
    //   1064: sipush 1900
    //   1067: isub
    //   1068: istore 9
    //   1070: iinc 10 -1
    //   1073: goto +199 -> 1272
    //   1076: aload_3
    //   1077: iconst_0
    //   1078: iconst_2
    //   1079: invokevirtual 302	java/lang/String:substring	(II)Ljava/lang/String;
    //   1082: invokestatic 303	java/lang/Integer:parseInt	(Ljava/lang/String;)I
    //   1085: istore 9
    //   1087: iload 9
    //   1089: bipush 69
    //   1091: if_icmpgt +10 -> 1101
    //   1094: iload 9
    //   1096: bipush 100
    //   1098: iadd
    //   1099: istore 9
    //   1101: iload 9
    //   1103: sipush 1900
    //   1106: iadd
    //   1107: istore 9
    //   1109: aload_3
    //   1110: iconst_2
    //   1111: iconst_4
    //   1112: invokevirtual 302	java/lang/String:substring	(II)Ljava/lang/String;
    //   1115: invokestatic 303	java/lang/Integer:parseInt	(Ljava/lang/String;)I
    //   1118: istore 10
    //   1120: aload_3
    //   1121: iconst_4
    //   1122: bipush 6
    //   1124: invokevirtual 302	java/lang/String:substring	(II)Ljava/lang/String;
    //   1127: invokestatic 303	java/lang/Integer:parseInt	(Ljava/lang/String;)I
    //   1130: istore 11
    //   1132: goto +140 -> 1272
    //   1135: aload_3
    //   1136: iconst_0
    //   1137: iconst_2
    //   1138: invokevirtual 302	java/lang/String:substring	(II)Ljava/lang/String;
    //   1141: invokestatic 303	java/lang/Integer:parseInt	(Ljava/lang/String;)I
    //   1144: istore 9
    //   1146: iload 9
    //   1148: bipush 69
    //   1150: if_icmpgt +10 -> 1160
    //   1153: iload 9
    //   1155: bipush 100
    //   1157: iadd
    //   1158: istore 9
    //   1160: iload 9
    //   1162: sipush 1900
    //   1165: iadd
    //   1166: istore 9
    //   1168: aload_3
    //   1169: iconst_2
    //   1170: iconst_4
    //   1171: invokevirtual 302	java/lang/String:substring	(II)Ljava/lang/String;
    //   1174: invokestatic 303	java/lang/Integer:parseInt	(Ljava/lang/String;)I
    //   1177: istore 10
    //   1179: iconst_1
    //   1180: istore 11
    //   1182: goto +90 -> 1272
    //   1185: aload_3
    //   1186: iconst_0
    //   1187: iconst_2
    //   1188: invokevirtual 302	java/lang/String:substring	(II)Ljava/lang/String;
    //   1191: invokestatic 303	java/lang/Integer:parseInt	(Ljava/lang/String;)I
    //   1194: istore 9
    //   1196: iload 9
    //   1198: bipush 69
    //   1200: if_icmpgt +10 -> 1210
    //   1203: iload 9
    //   1205: bipush 100
    //   1207: iadd
    //   1208: istore 9
    //   1210: iload 9
    //   1212: sipush 1900
    //   1215: iadd
    //   1216: istore 9
    //   1218: iconst_1
    //   1219: istore 10
    //   1221: iconst_1
    //   1222: istore 11
    //   1224: goto +48 -> 1272
    //   1227: new 217	java/sql/SQLException
    //   1230: dup
    //   1231: new 182	java/lang/StringBuffer
    //   1234: dup
    //   1235: invokespecial 183	java/lang/StringBuffer:<init>	()V
    //   1238: ldc_w 585
    //   1241: invokevirtual 185	java/lang/StringBuffer:append	(Ljava/lang/String;)Ljava/lang/StringBuffer;
    //   1244: aload_3
    //   1245: invokevirtual 185	java/lang/StringBuffer:append	(Ljava/lang/String;)Ljava/lang/StringBuffer;
    //   1248: ldc_w 586
    //   1251: invokevirtual 185	java/lang/StringBuffer:append	(Ljava/lang/String;)Ljava/lang/StringBuffer;
    //   1254: iload_1
    //   1255: invokevirtual 366	java/lang/StringBuffer:append	(I)Ljava/lang/StringBuffer;
    //   1258: ldc -6
    //   1260: invokevirtual 185	java/lang/StringBuffer:append	(Ljava/lang/String;)Ljava/lang/StringBuffer;
    //   1263: invokevirtual 187	java/lang/StringBuffer:toString	()Ljava/lang/String;
    //   1266: ldc 118
    //   1268: invokespecial 218	java/sql/SQLException:<init>	(Ljava/lang/String;Ljava/lang/String;)V
    //   1271: athrow
    //   1272: aload_0
    //   1273: getfield 74	com/mysql/jdbc/ResultSetImpl:useLegacyDatetimeCode	Z
    //   1276: ifne +26 -> 1302
    //   1279: aload 4
    //   1281: iload 9
    //   1283: iload 10
    //   1285: iload 11
    //   1287: iload 12
    //   1289: iload 13
    //   1291: iload 14
    //   1293: iload 15
    //   1295: invokestatic 175	com/mysql/jdbc/TimeUtil:fastTimestampCreate	(Ljava/util/TimeZone;IIIIIII)Ljava/sql/Timestamp;
    //   1298: aload 8
    //   1300: monitorexit
    //   1301: areturn
    //   1302: aload_0
    //   1303: getfield 64	com/mysql/jdbc/ResultSetImpl:connection	Lcom/mysql/jdbc/ConnectionImpl;
    //   1306: aload 7
    //   1308: aload_2
    //   1309: aload_0
    //   1310: aload 7
    //   1312: iload 9
    //   1314: iload 10
    //   1316: iload 11
    //   1318: iload 12
    //   1320: iload 13
    //   1322: iload 14
    //   1324: iload 15
    //   1326: invokevirtual 575	com/mysql/jdbc/ResultSetImpl:fastTimestampCreate	(Ljava/util/Calendar;IIIIIII)Ljava/sql/Timestamp;
    //   1329: aload_0
    //   1330: getfield 64	com/mysql/jdbc/ResultSetImpl:connection	Lcom/mysql/jdbc/ConnectionImpl;
    //   1333: invokevirtual 70	com/mysql/jdbc/ConnectionImpl:getServerTimezoneTZ	()Ljava/util/TimeZone;
    //   1336: aload 4
    //   1338: iload 5
    //   1340: invokestatic 576	com/mysql/jdbc/TimeUtil:changeTimezone	(Lcom/mysql/jdbc/ConnectionImpl;Ljava/util/Calendar;Ljava/util/Calendar;Ljava/sql/Timestamp;Ljava/util/TimeZone;Ljava/util/TimeZone;Z)Ljava/sql/Timestamp;
    //   1343: aload 8
    //   1345: monitorexit
    //   1346: areturn
    //   1347: astore 19
    //   1349: aload 8
    //   1351: monitorexit
    //   1352: aload 19
    //   1354: athrow
    //   1355: astore 6
    //   1357: new 182	java/lang/StringBuffer
    //   1360: dup
    //   1361: invokespecial 183	java/lang/StringBuffer:<init>	()V
    //   1364: ldc_w 587
    //   1367: invokevirtual 185	java/lang/StringBuffer:append	(Ljava/lang/String;)Ljava/lang/StringBuffer;
    //   1370: aload_3
    //   1371: invokevirtual 185	java/lang/StringBuffer:append	(Ljava/lang/String;)Ljava/lang/StringBuffer;
    //   1374: ldc_w 588
    //   1377: invokevirtual 185	java/lang/StringBuffer:append	(Ljava/lang/String;)Ljava/lang/StringBuffer;
    //   1380: iload_1
    //   1381: invokevirtual 366	java/lang/StringBuffer:append	(I)Ljava/lang/StringBuffer;
    //   1384: ldc_w 589
    //   1387: invokevirtual 185	java/lang/StringBuffer:append	(Ljava/lang/String;)Ljava/lang/StringBuffer;
    //   1390: invokevirtual 187	java/lang/StringBuffer:toString	()Ljava/lang/String;
    //   1393: ldc 118
    //   1395: aload_0
    //   1396: invokevirtual 119	com/mysql/jdbc/ResultSetImpl:getExceptionInterceptor	()Lcom/mysql/jdbc/ExceptionInterceptor;
    //   1399: invokestatic 120	com/mysql/jdbc/SQLError:createSQLException	(Ljava/lang/String;Ljava/lang/String;Lcom/mysql/jdbc/ExceptionInterceptor;)Ljava/sql/SQLException;
    //   1402: astore 7
    //   1404: aload 7
    //   1406: aload 6
    //   1408: invokevirtual 310	java/sql/SQLException:initCause	(Ljava/lang/Throwable;)Ljava/lang/Throwable;
    //   1411: pop
    //   1412: aload 7
    //   1414: athrow
    // Line number table:
    //   Java source line #6073	-> byte code offset #0
    //   Java source line #6075	-> byte code offset #5
    //   Java source line #6076	-> byte code offset #9
    //   Java source line #6078	-> byte code offset #14
    //   Java source line #6089	-> byte code offset #16
    //   Java source line #6091	-> byte code offset #21
    //   Java source line #6093	-> byte code offset #27
    //   Java source line #6097	-> byte code offset #53
    //   Java source line #6098	-> byte code offset #59
    //   Java source line #6105	-> byte code offset #113
    //   Java source line #6107	-> byte code offset #129
    //   Java source line #6109	-> byte code offset #134
    //   Java source line #6110	-> byte code offset #139
    //   Java source line #6112	-> byte code offset #155
    //   Java source line #6119	-> byte code offset #191
    //   Java source line #6121	-> byte code offset #207
    //   Java source line #6123	-> byte code offset #223
    //   Java source line #6124	-> byte code offset #230
    //   Java source line #6128	-> byte code offset #254
    //   Java source line #6138	-> byte code offset #300
    //   Java source line #6139	-> byte code offset #309
    //   Java source line #6145	-> byte code offset #321
    //   Java source line #6146	-> byte code offset #324
    //   Java source line #6147	-> byte code offset #327
    //   Java source line #6148	-> byte code offset #330
    //   Java source line #6149	-> byte code offset #333
    //   Java source line #6150	-> byte code offset #336
    //   Java source line #6151	-> byte code offset #339
    //   Java source line #6153	-> byte code offset #342
    //   Java source line #6162	-> byte code offset #460
    //   Java source line #6163	-> byte code offset #471
    //   Java source line #6165	-> byte code offset #483
    //   Java source line #6166	-> byte code offset #496
    //   Java source line #6168	-> byte code offset #509
    //   Java source line #6170	-> byte code offset #522
    //   Java source line #6173	-> byte code offset #535
    //   Java source line #6175	-> byte code offset #538
    //   Java source line #6176	-> byte code offset #545
    //   Java source line #6178	-> byte code offset #553
    //   Java source line #6179	-> byte code offset #559
    //   Java source line #6180	-> byte code offset #568
    //   Java source line #6183	-> byte code offset #581
    //   Java source line #6185	-> byte code offset #590
    //   Java source line #6186	-> byte code offset #597
    //   Java source line #6187	-> byte code offset #612
    //   Java source line #6190	-> byte code offset #622
    //   Java source line #6204	-> byte code offset #633
    //   Java source line #6205	-> byte code offset #644
    //   Java source line #6207	-> byte code offset #656
    //   Java source line #6208	-> byte code offset #669
    //   Java source line #6210	-> byte code offset #682
    //   Java source line #6212	-> byte code offset #695
    //   Java source line #6215	-> byte code offset #708
    //   Java source line #6219	-> byte code offset #711
    //   Java source line #6221	-> byte code offset #722
    //   Java source line #6222	-> byte code offset #729
    //   Java source line #6225	-> byte code offset #736
    //   Java source line #6227	-> byte code offset #744
    //   Java source line #6229	-> byte code offset #755
    //   Java source line #6230	-> byte code offset #767
    //   Java source line #6231	-> byte code offset #780
    //   Java source line #6233	-> byte code offset #793
    //   Java source line #6236	-> byte code offset #806
    //   Java source line #6240	-> byte code offset #809
    //   Java source line #6242	-> byte code offset #836
    //   Java source line #6243	-> byte code offset #847
    //   Java source line #6245	-> byte code offset #859
    //   Java source line #6246	-> byte code offset #872
    //   Java source line #6247	-> byte code offset #875
    //   Java source line #6249	-> byte code offset #881
    //   Java source line #6251	-> byte code offset #892
    //   Java source line #6252	-> byte code offset #899
    //   Java source line #6255	-> byte code offset #906
    //   Java source line #6257	-> byte code offset #917
    //   Java source line #6258	-> byte code offset #929
    //   Java source line #6259	-> byte code offset #942
    //   Java source line #6262	-> byte code offset #955
    //   Java source line #6265	-> byte code offset #963
    //   Java source line #6269	-> byte code offset #966
    //   Java source line #6270	-> byte code offset #977
    //   Java source line #6272	-> byte code offset #988
    //   Java source line #6274	-> byte code offset #999
    //   Java source line #6276	-> byte code offset #1012
    //   Java source line #6277	-> byte code offset #1017
    //   Java source line #6278	-> byte code offset #1020
    //   Java source line #6279	-> byte code offset #1023
    //   Java source line #6282	-> byte code offset #1026
    //   Java source line #6283	-> byte code offset #1037
    //   Java source line #6285	-> byte code offset #1049
    //   Java source line #6287	-> byte code offset #1062
    //   Java source line #6288	-> byte code offset #1070
    //   Java source line #6290	-> byte code offset #1073
    //   Java source line #6294	-> byte code offset #1076
    //   Java source line #6296	-> byte code offset #1087
    //   Java source line #6297	-> byte code offset #1094
    //   Java source line #6300	-> byte code offset #1101
    //   Java source line #6302	-> byte code offset #1109
    //   Java source line #6304	-> byte code offset #1120
    //   Java source line #6306	-> byte code offset #1132
    //   Java source line #6310	-> byte code offset #1135
    //   Java source line #6312	-> byte code offset #1146
    //   Java source line #6313	-> byte code offset #1153
    //   Java source line #6316	-> byte code offset #1160
    //   Java source line #6318	-> byte code offset #1168
    //   Java source line #6321	-> byte code offset #1179
    //   Java source line #6323	-> byte code offset #1182
    //   Java source line #6327	-> byte code offset #1185
    //   Java source line #6329	-> byte code offset #1196
    //   Java source line #6330	-> byte code offset #1203
    //   Java source line #6333	-> byte code offset #1210
    //   Java source line #6334	-> byte code offset #1218
    //   Java source line #6335	-> byte code offset #1221
    //   Java source line #6337	-> byte code offset #1224
    //   Java source line #6341	-> byte code offset #1227
    //   Java source line #6347	-> byte code offset #1272
    //   Java source line #6348	-> byte code offset #1279
    //   Java source line #6352	-> byte code offset #1302
    //   Java source line #6359	-> byte code offset #1347
    //   Java source line #6360	-> byte code offset #1355
    //   Java source line #6361	-> byte code offset #1357
    //   Java source line #6364	-> byte code offset #1404
    //   Java source line #6366	-> byte code offset #1412
    // Local variable table:
    //   start	length	slot	name	signature
    //   0	1415	0	this	ResultSetImpl
    //   0	1415	1	columnIndex	int
    //   0	1415	2	targetCalendar	Calendar
    //   0	1415	3	timestampValue	String
    //   0	1415	4	tz	TimeZone
    //   0	1415	5	rollForward	boolean
    //   25	557	6	length	int
    //   1355	52	6	e	Exception
    //   51	1260	7	sessionCalendar	Calendar
    //   1402	11	7	sqlEx	SQLException
    //   322	991	9	year	int
    //   325	990	10	month	int
    //   328	989	11	day	int
    //   331	988	12	hour	int
    //   334	987	13	minutes	int
    //   337	986	14	seconds	int
    //   340	985	15	nanos	int
    //   551	33	16	decimalIndex	int
    //   588	15	17	numDigits	int
    //   610	5	18	factor	int
    //   1347	6	19	localObject1	Object
    // Exception table:
    //   from	to	target	type
    //   59	138	1347	finally
    //   139	206	1347	finally
    //   207	253	1347	finally
    //   254	299	1347	finally
    //   300	1301	1347	finally
    //   1302	1346	1347	finally
    //   1347	1352	1347	finally
    //   0	15	1355	java/lang/Exception
    //   16	138	1355	java/lang/Exception
    //   139	206	1355	java/lang/Exception
    //   207	253	1355	java/lang/Exception
    //   254	299	1355	java/lang/Exception
    //   300	1301	1355	java/lang/Exception
    //   1302	1346	1355	java/lang/Exception
    //   1347	1355	1355	java/lang/Exception
  }
  
  /* Error */
  private Timestamp getTimestampFromBytes(int columnIndex, Calendar targetCalendar, byte[] timestampAsBytes, TimeZone tz, boolean rollForward)
    throws SQLException
  {
    // Byte code:
    //   0: aload_0
    //   1: iload_1
    //   2: invokevirtual 191	com/mysql/jdbc/ResultSetImpl:checkColumnBounds	(I)V
    //   5: aload_0
    //   6: iconst_0
    //   7: putfield 54	com/mysql/jdbc/ResultSetImpl:wasNullFlag	Z
    //   10: aload_3
    //   11: ifnonnull +10 -> 21
    //   14: aload_0
    //   15: iconst_1
    //   16: putfield 54	com/mysql/jdbc/ResultSetImpl:wasNullFlag	Z
    //   19: aconst_null
    //   20: areturn
    //   21: aload_3
    //   22: arraylength
    //   23: istore 6
    //   25: aload_0
    //   26: getfield 64	com/mysql/jdbc/ResultSetImpl:connection	Lcom/mysql/jdbc/ConnectionImpl;
    //   29: invokevirtual 572	com/mysql/jdbc/ConnectionImpl:getUseJDBCCompliantTimezoneShift	()Z
    //   32: ifeq +13 -> 45
    //   35: aload_0
    //   36: getfield 64	com/mysql/jdbc/ResultSetImpl:connection	Lcom/mysql/jdbc/ConnectionImpl;
    //   39: invokevirtual 573	com/mysql/jdbc/ConnectionImpl:getUtcCalendar	()Ljava/util/Calendar;
    //   42: goto +7 -> 49
    //   45: aload_0
    //   46: invokevirtual 566	com/mysql/jdbc/ResultSetImpl:getCalendarInstanceForSessionOrNew	()Ljava/util/Calendar;
    //   49: astore 7
    //   51: aload 7
    //   53: dup
    //   54: astore 8
    //   56: monitorenter
    //   57: iconst_1
    //   58: istore 9
    //   60: aload_3
    //   61: bipush 58
    //   63: invokestatic 590	com/mysql/jdbc/StringUtils:indexOf	([BC)I
    //   66: iconst_m1
    //   67: if_icmpeq +7 -> 74
    //   70: iconst_1
    //   71: goto +4 -> 75
    //   74: iconst_0
    //   75: istore 10
    //   77: iconst_0
    //   78: istore 11
    //   80: iload 11
    //   82: iload 6
    //   84: if_icmpge +87 -> 171
    //   87: aload_3
    //   88: iload 11
    //   90: baload
    //   91: istore 12
    //   93: iload 12
    //   95: bipush 32
    //   97: if_icmpeq +17 -> 114
    //   100: iload 12
    //   102: bipush 45
    //   104: if_icmpeq +10 -> 114
    //   107: iload 12
    //   109: bipush 47
    //   111: if_icmpne +6 -> 117
    //   114: iconst_0
    //   115: istore 10
    //   117: iload 12
    //   119: bipush 48
    //   121: if_icmpeq +44 -> 165
    //   124: iload 12
    //   126: bipush 32
    //   128: if_icmpeq +37 -> 165
    //   131: iload 12
    //   133: bipush 58
    //   135: if_icmpeq +30 -> 165
    //   138: iload 12
    //   140: bipush 45
    //   142: if_icmpeq +23 -> 165
    //   145: iload 12
    //   147: bipush 47
    //   149: if_icmpeq +16 -> 165
    //   152: iload 12
    //   154: bipush 46
    //   156: if_icmpeq +9 -> 165
    //   159: iconst_0
    //   160: istore 9
    //   162: goto +9 -> 171
    //   165: iinc 11 1
    //   168: goto -88 -> 80
    //   171: iload 10
    //   173: ifne +125 -> 298
    //   176: iload 9
    //   178: ifeq +120 -> 298
    //   181: ldc_w 296
    //   184: aload_0
    //   185: getfield 64	com/mysql/jdbc/ResultSetImpl:connection	Lcom/mysql/jdbc/ConnectionImpl;
    //   188: invokevirtual 297	com/mysql/jdbc/ConnectionImpl:getZeroDateTimeBehavior	()Ljava/lang/String;
    //   191: invokevirtual 244	java/lang/String:equals	(Ljava/lang/Object;)Z
    //   194: ifeq +13 -> 207
    //   197: aload_0
    //   198: iconst_1
    //   199: putfield 54	com/mysql/jdbc/ResultSetImpl:wasNullFlag	Z
    //   202: aconst_null
    //   203: aload 8
    //   205: monitorexit
    //   206: areturn
    //   207: ldc_w 298
    //   210: aload_0
    //   211: getfield 64	com/mysql/jdbc/ResultSetImpl:connection	Lcom/mysql/jdbc/ConnectionImpl;
    //   214: invokevirtual 297	com/mysql/jdbc/ConnectionImpl:getZeroDateTimeBehavior	()Ljava/lang/String;
    //   217: invokevirtual 244	java/lang/String:equals	(Ljava/lang/Object;)Z
    //   220: ifeq +39 -> 259
    //   223: new 182	java/lang/StringBuffer
    //   226: dup
    //   227: invokespecial 183	java/lang/StringBuffer:<init>	()V
    //   230: ldc_w 299
    //   233: invokevirtual 185	java/lang/StringBuffer:append	(Ljava/lang/String;)Ljava/lang/StringBuffer;
    //   236: aload_3
    //   237: invokevirtual 556	java/lang/StringBuffer:append	(Ljava/lang/Object;)Ljava/lang/StringBuffer;
    //   240: ldc_w 574
    //   243: invokevirtual 185	java/lang/StringBuffer:append	(Ljava/lang/String;)Ljava/lang/StringBuffer;
    //   246: invokevirtual 187	java/lang/StringBuffer:toString	()Ljava/lang/String;
    //   249: ldc 118
    //   251: aload_0
    //   252: invokevirtual 119	com/mysql/jdbc/ResultSetImpl:getExceptionInterceptor	()Lcom/mysql/jdbc/ExceptionInterceptor;
    //   255: invokestatic 120	com/mysql/jdbc/SQLError:createSQLException	(Ljava/lang/String;Ljava/lang/String;Lcom/mysql/jdbc/ExceptionInterceptor;)Ljava/sql/SQLException;
    //   258: athrow
    //   259: aload_0
    //   260: getfield 74	com/mysql/jdbc/ResultSetImpl:useLegacyDatetimeCode	Z
    //   263: ifne +19 -> 282
    //   266: aload 4
    //   268: iconst_1
    //   269: iconst_1
    //   270: iconst_1
    //   271: iconst_0
    //   272: iconst_0
    //   273: iconst_0
    //   274: iconst_0
    //   275: invokestatic 175	com/mysql/jdbc/TimeUtil:fastTimestampCreate	(Ljava/util/TimeZone;IIIIIII)Ljava/sql/Timestamp;
    //   278: aload 8
    //   280: monitorexit
    //   281: areturn
    //   282: aload_0
    //   283: aconst_null
    //   284: iconst_1
    //   285: iconst_1
    //   286: iconst_1
    //   287: iconst_0
    //   288: iconst_0
    //   289: iconst_0
    //   290: iconst_0
    //   291: invokevirtual 575	com/mysql/jdbc/ResultSetImpl:fastTimestampCreate	(Ljava/util/Calendar;IIIIIII)Ljava/sql/Timestamp;
    //   294: aload 8
    //   296: monitorexit
    //   297: areturn
    //   298: aload_0
    //   299: getfield 63	com/mysql/jdbc/ResultSetImpl:fields	[Lcom/mysql/jdbc/Field;
    //   302: iload_1
    //   303: iconst_1
    //   304: isub
    //   305: aaload
    //   306: invokevirtual 211	com/mysql/jdbc/Field:getMysqlType	()I
    //   309: bipush 13
    //   311: if_icmpne +74 -> 385
    //   314: aload_0
    //   315: getfield 74	com/mysql/jdbc/ResultSetImpl:useLegacyDatetimeCode	Z
    //   318: ifne +24 -> 342
    //   321: aload 4
    //   323: aload_3
    //   324: iconst_0
    //   325: iconst_4
    //   326: invokestatic 591	com/mysql/jdbc/StringUtils:getInt	([BII)I
    //   329: iconst_1
    //   330: iconst_1
    //   331: iconst_0
    //   332: iconst_0
    //   333: iconst_0
    //   334: iconst_0
    //   335: invokestatic 175	com/mysql/jdbc/TimeUtil:fastTimestampCreate	(Ljava/util/TimeZone;IIIIIII)Ljava/sql/Timestamp;
    //   338: aload 8
    //   340: monitorexit
    //   341: areturn
    //   342: aload_0
    //   343: getfield 64	com/mysql/jdbc/ResultSetImpl:connection	Lcom/mysql/jdbc/ConnectionImpl;
    //   346: aload 7
    //   348: aload_2
    //   349: aload_0
    //   350: aload 7
    //   352: aload_3
    //   353: iconst_0
    //   354: iconst_4
    //   355: invokestatic 591	com/mysql/jdbc/StringUtils:getInt	([BII)I
    //   358: iconst_1
    //   359: iconst_1
    //   360: iconst_0
    //   361: iconst_0
    //   362: iconst_0
    //   363: iconst_0
    //   364: invokevirtual 575	com/mysql/jdbc/ResultSetImpl:fastTimestampCreate	(Ljava/util/Calendar;IIIIIII)Ljava/sql/Timestamp;
    //   367: aload_0
    //   368: getfield 64	com/mysql/jdbc/ResultSetImpl:connection	Lcom/mysql/jdbc/ConnectionImpl;
    //   371: invokevirtual 70	com/mysql/jdbc/ConnectionImpl:getServerTimezoneTZ	()Ljava/util/TimeZone;
    //   374: aload 4
    //   376: iload 5
    //   378: invokestatic 576	com/mysql/jdbc/TimeUtil:changeTimezone	(Lcom/mysql/jdbc/ConnectionImpl;Ljava/util/Calendar;Ljava/util/Calendar;Ljava/sql/Timestamp;Ljava/util/TimeZone;Ljava/util/TimeZone;Z)Ljava/sql/Timestamp;
    //   381: aload 8
    //   383: monitorexit
    //   384: areturn
    //   385: aload_3
    //   386: iload 6
    //   388: iconst_1
    //   389: isub
    //   390: baload
    //   391: bipush 46
    //   393: if_icmpne +6 -> 399
    //   396: iinc 6 -1
    //   399: iconst_0
    //   400: istore 11
    //   402: iconst_0
    //   403: istore 12
    //   405: iconst_0
    //   406: istore 13
    //   408: iconst_0
    //   409: istore 14
    //   411: iconst_0
    //   412: istore 15
    //   414: iconst_0
    //   415: istore 16
    //   417: iconst_0
    //   418: istore 17
    //   420: iload 6
    //   422: tableswitch	default:+726->1148, 2:+687->1109, 3:+726->1148, 4:+643->1065, 5:+726->1148, 6:+593->1015, 7:+726->1148, 8:+502->924, 9:+726->1148, 10:+370->792, 11:+726->1148, 12:+290->712, 13:+726->1148, 14:+230->652, 15:+726->1148, 16:+726->1148, 17:+726->1148, 18:+726->1148, 19:+114->536, 20:+114->536, 21:+114->536, 22:+114->536, 23:+114->536, 24:+114->536, 25:+114->536, 26:+114->536
    //   536: aload_3
    //   537: iconst_0
    //   538: iconst_4
    //   539: invokestatic 591	com/mysql/jdbc/StringUtils:getInt	([BII)I
    //   542: istore 11
    //   544: aload_3
    //   545: iconst_5
    //   546: bipush 7
    //   548: invokestatic 591	com/mysql/jdbc/StringUtils:getInt	([BII)I
    //   551: istore 12
    //   553: aload_3
    //   554: bipush 8
    //   556: bipush 10
    //   558: invokestatic 591	com/mysql/jdbc/StringUtils:getInt	([BII)I
    //   561: istore 13
    //   563: aload_3
    //   564: bipush 11
    //   566: bipush 13
    //   568: invokestatic 591	com/mysql/jdbc/StringUtils:getInt	([BII)I
    //   571: istore 14
    //   573: aload_3
    //   574: bipush 14
    //   576: bipush 16
    //   578: invokestatic 591	com/mysql/jdbc/StringUtils:getInt	([BII)I
    //   581: istore 15
    //   583: aload_3
    //   584: bipush 17
    //   586: bipush 19
    //   588: invokestatic 591	com/mysql/jdbc/StringUtils:getInt	([BII)I
    //   591: istore 16
    //   593: iconst_0
    //   594: istore 17
    //   596: iload 6
    //   598: bipush 19
    //   600: if_icmple +600 -> 1200
    //   603: aload_3
    //   604: bipush 46
    //   606: invokestatic 592	com/mysql/jdbc/StringUtils:lastIndexOf	([BC)I
    //   609: istore 18
    //   611: iload 18
    //   613: iconst_m1
    //   614: if_icmpeq +35 -> 649
    //   617: iload 18
    //   619: iconst_2
    //   620: iadd
    //   621: iload 6
    //   623: if_icmpgt +18 -> 641
    //   626: aload_3
    //   627: iload 18
    //   629: iconst_1
    //   630: iadd
    //   631: iload 6
    //   633: invokestatic 591	com/mysql/jdbc/StringUtils:getInt	([BII)I
    //   636: istore 17
    //   638: goto +11 -> 649
    //   641: new 581	java/lang/IllegalArgumentException
    //   644: dup
    //   645: invokespecial 582	java/lang/IllegalArgumentException:<init>	()V
    //   648: athrow
    //   649: goto +551 -> 1200
    //   652: aload_3
    //   653: iconst_0
    //   654: iconst_4
    //   655: invokestatic 591	com/mysql/jdbc/StringUtils:getInt	([BII)I
    //   658: istore 11
    //   660: aload_3
    //   661: iconst_4
    //   662: bipush 6
    //   664: invokestatic 591	com/mysql/jdbc/StringUtils:getInt	([BII)I
    //   667: istore 12
    //   669: aload_3
    //   670: bipush 6
    //   672: bipush 8
    //   674: invokestatic 591	com/mysql/jdbc/StringUtils:getInt	([BII)I
    //   677: istore 13
    //   679: aload_3
    //   680: bipush 8
    //   682: bipush 10
    //   684: invokestatic 591	com/mysql/jdbc/StringUtils:getInt	([BII)I
    //   687: istore 14
    //   689: aload_3
    //   690: bipush 10
    //   692: bipush 12
    //   694: invokestatic 591	com/mysql/jdbc/StringUtils:getInt	([BII)I
    //   697: istore 15
    //   699: aload_3
    //   700: bipush 12
    //   702: bipush 14
    //   704: invokestatic 591	com/mysql/jdbc/StringUtils:getInt	([BII)I
    //   707: istore 16
    //   709: goto +491 -> 1200
    //   712: aload_3
    //   713: iconst_0
    //   714: iconst_2
    //   715: invokestatic 591	com/mysql/jdbc/StringUtils:getInt	([BII)I
    //   718: istore 11
    //   720: iload 11
    //   722: bipush 69
    //   724: if_icmpgt +10 -> 734
    //   727: iload 11
    //   729: bipush 100
    //   731: iadd
    //   732: istore 11
    //   734: iload 11
    //   736: sipush 1900
    //   739: iadd
    //   740: istore 11
    //   742: aload_3
    //   743: iconst_2
    //   744: iconst_4
    //   745: invokestatic 591	com/mysql/jdbc/StringUtils:getInt	([BII)I
    //   748: istore 12
    //   750: aload_3
    //   751: iconst_4
    //   752: bipush 6
    //   754: invokestatic 591	com/mysql/jdbc/StringUtils:getInt	([BII)I
    //   757: istore 13
    //   759: aload_3
    //   760: bipush 6
    //   762: bipush 8
    //   764: invokestatic 591	com/mysql/jdbc/StringUtils:getInt	([BII)I
    //   767: istore 14
    //   769: aload_3
    //   770: bipush 8
    //   772: bipush 10
    //   774: invokestatic 591	com/mysql/jdbc/StringUtils:getInt	([BII)I
    //   777: istore 15
    //   779: aload_3
    //   780: bipush 10
    //   782: bipush 12
    //   784: invokestatic 591	com/mysql/jdbc/StringUtils:getInt	([BII)I
    //   787: istore 16
    //   789: goto +411 -> 1200
    //   792: aload_0
    //   793: getfield 63	com/mysql/jdbc/ResultSetImpl:fields	[Lcom/mysql/jdbc/Field;
    //   796: iload_1
    //   797: iconst_1
    //   798: isub
    //   799: aaload
    //   800: invokevirtual 211	com/mysql/jdbc/Field:getMysqlType	()I
    //   803: bipush 10
    //   805: if_icmpeq +13 -> 818
    //   808: aload_3
    //   809: bipush 45
    //   811: invokestatic 590	com/mysql/jdbc/StringUtils:indexOf	([BC)I
    //   814: iconst_m1
    //   815: if_icmpeq +39 -> 854
    //   818: aload_3
    //   819: iconst_0
    //   820: iconst_4
    //   821: invokestatic 591	com/mysql/jdbc/StringUtils:getInt	([BII)I
    //   824: istore 11
    //   826: aload_3
    //   827: iconst_5
    //   828: bipush 7
    //   830: invokestatic 591	com/mysql/jdbc/StringUtils:getInt	([BII)I
    //   833: istore 12
    //   835: aload_3
    //   836: bipush 8
    //   838: bipush 10
    //   840: invokestatic 591	com/mysql/jdbc/StringUtils:getInt	([BII)I
    //   843: istore 13
    //   845: iconst_0
    //   846: istore 14
    //   848: iconst_0
    //   849: istore 15
    //   851: goto +349 -> 1200
    //   854: aload_3
    //   855: iconst_0
    //   856: iconst_2
    //   857: invokestatic 591	com/mysql/jdbc/StringUtils:getInt	([BII)I
    //   860: istore 11
    //   862: iload 11
    //   864: bipush 69
    //   866: if_icmpgt +10 -> 876
    //   869: iload 11
    //   871: bipush 100
    //   873: iadd
    //   874: istore 11
    //   876: aload_3
    //   877: iconst_2
    //   878: iconst_4
    //   879: invokestatic 591	com/mysql/jdbc/StringUtils:getInt	([BII)I
    //   882: istore 12
    //   884: aload_3
    //   885: iconst_4
    //   886: bipush 6
    //   888: invokestatic 591	com/mysql/jdbc/StringUtils:getInt	([BII)I
    //   891: istore 13
    //   893: aload_3
    //   894: bipush 6
    //   896: bipush 8
    //   898: invokestatic 591	com/mysql/jdbc/StringUtils:getInt	([BII)I
    //   901: istore 14
    //   903: aload_3
    //   904: bipush 8
    //   906: bipush 10
    //   908: invokestatic 591	com/mysql/jdbc/StringUtils:getInt	([BII)I
    //   911: istore 15
    //   913: iload 11
    //   915: sipush 1900
    //   918: iadd
    //   919: istore 11
    //   921: goto +279 -> 1200
    //   924: aload_3
    //   925: bipush 58
    //   927: invokestatic 590	com/mysql/jdbc/StringUtils:indexOf	([BC)I
    //   930: iconst_m1
    //   931: if_icmpeq +43 -> 974
    //   934: aload_3
    //   935: iconst_0
    //   936: iconst_2
    //   937: invokestatic 591	com/mysql/jdbc/StringUtils:getInt	([BII)I
    //   940: istore 14
    //   942: aload_3
    //   943: iconst_3
    //   944: iconst_5
    //   945: invokestatic 591	com/mysql/jdbc/StringUtils:getInt	([BII)I
    //   948: istore 15
    //   950: aload_3
    //   951: bipush 6
    //   953: bipush 8
    //   955: invokestatic 591	com/mysql/jdbc/StringUtils:getInt	([BII)I
    //   958: istore 16
    //   960: sipush 1970
    //   963: istore 11
    //   965: iconst_1
    //   966: istore 12
    //   968: iconst_1
    //   969: istore 13
    //   971: goto +229 -> 1200
    //   974: aload_3
    //   975: iconst_0
    //   976: iconst_4
    //   977: invokestatic 591	com/mysql/jdbc/StringUtils:getInt	([BII)I
    //   980: istore 11
    //   982: aload_3
    //   983: iconst_4
    //   984: bipush 6
    //   986: invokestatic 591	com/mysql/jdbc/StringUtils:getInt	([BII)I
    //   989: istore 12
    //   991: aload_3
    //   992: bipush 6
    //   994: bipush 8
    //   996: invokestatic 591	com/mysql/jdbc/StringUtils:getInt	([BII)I
    //   999: istore 13
    //   1001: iload 11
    //   1003: sipush 1900
    //   1006: isub
    //   1007: istore 11
    //   1009: iinc 12 -1
    //   1012: goto +188 -> 1200
    //   1015: aload_3
    //   1016: iconst_0
    //   1017: iconst_2
    //   1018: invokestatic 591	com/mysql/jdbc/StringUtils:getInt	([BII)I
    //   1021: istore 11
    //   1023: iload 11
    //   1025: bipush 69
    //   1027: if_icmpgt +10 -> 1037
    //   1030: iload 11
    //   1032: bipush 100
    //   1034: iadd
    //   1035: istore 11
    //   1037: iload 11
    //   1039: sipush 1900
    //   1042: iadd
    //   1043: istore 11
    //   1045: aload_3
    //   1046: iconst_2
    //   1047: iconst_4
    //   1048: invokestatic 591	com/mysql/jdbc/StringUtils:getInt	([BII)I
    //   1051: istore 12
    //   1053: aload_3
    //   1054: iconst_4
    //   1055: bipush 6
    //   1057: invokestatic 591	com/mysql/jdbc/StringUtils:getInt	([BII)I
    //   1060: istore 13
    //   1062: goto +138 -> 1200
    //   1065: aload_3
    //   1066: iconst_0
    //   1067: iconst_2
    //   1068: invokestatic 591	com/mysql/jdbc/StringUtils:getInt	([BII)I
    //   1071: istore 11
    //   1073: iload 11
    //   1075: bipush 69
    //   1077: if_icmpgt +10 -> 1087
    //   1080: iload 11
    //   1082: bipush 100
    //   1084: iadd
    //   1085: istore 11
    //   1087: iload 11
    //   1089: sipush 1900
    //   1092: iadd
    //   1093: istore 11
    //   1095: aload_3
    //   1096: iconst_2
    //   1097: iconst_4
    //   1098: invokestatic 591	com/mysql/jdbc/StringUtils:getInt	([BII)I
    //   1101: istore 12
    //   1103: iconst_1
    //   1104: istore 13
    //   1106: goto +94 -> 1200
    //   1109: aload_3
    //   1110: iconst_0
    //   1111: iconst_2
    //   1112: invokestatic 591	com/mysql/jdbc/StringUtils:getInt	([BII)I
    //   1115: istore 11
    //   1117: iload 11
    //   1119: bipush 69
    //   1121: if_icmpgt +10 -> 1131
    //   1124: iload 11
    //   1126: bipush 100
    //   1128: iadd
    //   1129: istore 11
    //   1131: iload 11
    //   1133: sipush 1900
    //   1136: iadd
    //   1137: istore 11
    //   1139: iconst_1
    //   1140: istore 12
    //   1142: iconst_1
    //   1143: istore 13
    //   1145: goto +55 -> 1200
    //   1148: new 217	java/sql/SQLException
    //   1151: dup
    //   1152: new 182	java/lang/StringBuffer
    //   1155: dup
    //   1156: invokespecial 183	java/lang/StringBuffer:<init>	()V
    //   1159: ldc_w 585
    //   1162: invokevirtual 185	java/lang/StringBuffer:append	(Ljava/lang/String;)Ljava/lang/StringBuffer;
    //   1165: new 532	java/lang/String
    //   1168: dup
    //   1169: aload_3
    //   1170: invokespecial 533	java/lang/String:<init>	([B)V
    //   1173: invokevirtual 185	java/lang/StringBuffer:append	(Ljava/lang/String;)Ljava/lang/StringBuffer;
    //   1176: ldc_w 586
    //   1179: invokevirtual 185	java/lang/StringBuffer:append	(Ljava/lang/String;)Ljava/lang/StringBuffer;
    //   1182: iload_1
    //   1183: invokevirtual 366	java/lang/StringBuffer:append	(I)Ljava/lang/StringBuffer;
    //   1186: ldc -6
    //   1188: invokevirtual 185	java/lang/StringBuffer:append	(Ljava/lang/String;)Ljava/lang/StringBuffer;
    //   1191: invokevirtual 187	java/lang/StringBuffer:toString	()Ljava/lang/String;
    //   1194: ldc 118
    //   1196: invokespecial 218	java/sql/SQLException:<init>	(Ljava/lang/String;Ljava/lang/String;)V
    //   1199: athrow
    //   1200: aload_0
    //   1201: getfield 74	com/mysql/jdbc/ResultSetImpl:useLegacyDatetimeCode	Z
    //   1204: ifne +26 -> 1230
    //   1207: aload 4
    //   1209: iload 11
    //   1211: iload 12
    //   1213: iload 13
    //   1215: iload 14
    //   1217: iload 15
    //   1219: iload 16
    //   1221: iload 17
    //   1223: invokestatic 175	com/mysql/jdbc/TimeUtil:fastTimestampCreate	(Ljava/util/TimeZone;IIIIIII)Ljava/sql/Timestamp;
    //   1226: aload 8
    //   1228: monitorexit
    //   1229: areturn
    //   1230: aload_0
    //   1231: getfield 64	com/mysql/jdbc/ResultSetImpl:connection	Lcom/mysql/jdbc/ConnectionImpl;
    //   1234: aload 7
    //   1236: aload_2
    //   1237: aload_0
    //   1238: aload 7
    //   1240: iload 11
    //   1242: iload 12
    //   1244: iload 13
    //   1246: iload 14
    //   1248: iload 15
    //   1250: iload 16
    //   1252: iload 17
    //   1254: invokevirtual 575	com/mysql/jdbc/ResultSetImpl:fastTimestampCreate	(Ljava/util/Calendar;IIIIIII)Ljava/sql/Timestamp;
    //   1257: aload_0
    //   1258: getfield 64	com/mysql/jdbc/ResultSetImpl:connection	Lcom/mysql/jdbc/ConnectionImpl;
    //   1261: invokevirtual 70	com/mysql/jdbc/ConnectionImpl:getServerTimezoneTZ	()Ljava/util/TimeZone;
    //   1264: aload 4
    //   1266: iload 5
    //   1268: invokestatic 576	com/mysql/jdbc/TimeUtil:changeTimezone	(Lcom/mysql/jdbc/ConnectionImpl;Ljava/util/Calendar;Ljava/util/Calendar;Ljava/sql/Timestamp;Ljava/util/TimeZone;Ljava/util/TimeZone;Z)Ljava/sql/Timestamp;
    //   1271: aload 8
    //   1273: monitorexit
    //   1274: areturn
    //   1275: astore 19
    //   1277: aload 8
    //   1279: monitorexit
    //   1280: aload 19
    //   1282: athrow
    //   1283: astore 6
    //   1285: new 182	java/lang/StringBuffer
    //   1288: dup
    //   1289: invokespecial 183	java/lang/StringBuffer:<init>	()V
    //   1292: ldc_w 587
    //   1295: invokevirtual 185	java/lang/StringBuffer:append	(Ljava/lang/String;)Ljava/lang/StringBuffer;
    //   1298: new 532	java/lang/String
    //   1301: dup
    //   1302: aload_3
    //   1303: invokespecial 533	java/lang/String:<init>	([B)V
    //   1306: invokevirtual 185	java/lang/StringBuffer:append	(Ljava/lang/String;)Ljava/lang/StringBuffer;
    //   1309: ldc_w 588
    //   1312: invokevirtual 185	java/lang/StringBuffer:append	(Ljava/lang/String;)Ljava/lang/StringBuffer;
    //   1315: iload_1
    //   1316: invokevirtual 366	java/lang/StringBuffer:append	(I)Ljava/lang/StringBuffer;
    //   1319: ldc_w 589
    //   1322: invokevirtual 185	java/lang/StringBuffer:append	(Ljava/lang/String;)Ljava/lang/StringBuffer;
    //   1325: invokevirtual 187	java/lang/StringBuffer:toString	()Ljava/lang/String;
    //   1328: ldc 118
    //   1330: aload_0
    //   1331: invokevirtual 119	com/mysql/jdbc/ResultSetImpl:getExceptionInterceptor	()Lcom/mysql/jdbc/ExceptionInterceptor;
    //   1334: invokestatic 120	com/mysql/jdbc/SQLError:createSQLException	(Ljava/lang/String;Ljava/lang/String;Lcom/mysql/jdbc/ExceptionInterceptor;)Ljava/sql/SQLException;
    //   1337: astore 7
    //   1339: aload 7
    //   1341: aload 6
    //   1343: invokevirtual 310	java/sql/SQLException:initCause	(Ljava/lang/Throwable;)Ljava/lang/Throwable;
    //   1346: pop
    //   1347: aload 7
    //   1349: athrow
    // Line number table:
    //   Java source line #6375	-> byte code offset #0
    //   Java source line #6378	-> byte code offset #5
    //   Java source line #6380	-> byte code offset #10
    //   Java source line #6381	-> byte code offset #14
    //   Java source line #6383	-> byte code offset #19
    //   Java source line #6386	-> byte code offset #21
    //   Java source line #6388	-> byte code offset #25
    //   Java source line #6392	-> byte code offset #51
    //   Java source line #6393	-> byte code offset #57
    //   Java source line #6395	-> byte code offset #60
    //   Java source line #6397	-> byte code offset #77
    //   Java source line #6398	-> byte code offset #87
    //   Java source line #6400	-> byte code offset #93
    //   Java source line #6401	-> byte code offset #114
    //   Java source line #6404	-> byte code offset #117
    //   Java source line #6406	-> byte code offset #159
    //   Java source line #6408	-> byte code offset #162
    //   Java source line #6397	-> byte code offset #165
    //   Java source line #6412	-> byte code offset #171
    //   Java source line #6414	-> byte code offset #181
    //   Java source line #6416	-> byte code offset #197
    //   Java source line #6418	-> byte code offset #202
    //   Java source line #6419	-> byte code offset #207
    //   Java source line #6421	-> byte code offset #223
    //   Java source line #6428	-> byte code offset #259
    //   Java source line #6429	-> byte code offset #266
    //   Java source line #6433	-> byte code offset #282
    //   Java source line #6434	-> byte code offset #298
    //   Java source line #6436	-> byte code offset #314
    //   Java source line #6437	-> byte code offset #321
    //   Java source line #6441	-> byte code offset #342
    //   Java source line #6449	-> byte code offset #385
    //   Java source line #6450	-> byte code offset #396
    //   Java source line #6455	-> byte code offset #399
    //   Java source line #6456	-> byte code offset #402
    //   Java source line #6457	-> byte code offset #405
    //   Java source line #6458	-> byte code offset #408
    //   Java source line #6459	-> byte code offset #411
    //   Java source line #6460	-> byte code offset #414
    //   Java source line #6461	-> byte code offset #417
    //   Java source line #6463	-> byte code offset #420
    //   Java source line #6472	-> byte code offset #536
    //   Java source line #6473	-> byte code offset #544
    //   Java source line #6474	-> byte code offset #553
    //   Java source line #6475	-> byte code offset #563
    //   Java source line #6476	-> byte code offset #573
    //   Java source line #6477	-> byte code offset #583
    //   Java source line #6479	-> byte code offset #593
    //   Java source line #6481	-> byte code offset #596
    //   Java source line #6482	-> byte code offset #603
    //   Java source line #6484	-> byte code offset #611
    //   Java source line #6485	-> byte code offset #617
    //   Java source line #6486	-> byte code offset #626
    //   Java source line #6488	-> byte code offset #641
    //   Java source line #6502	-> byte code offset #652
    //   Java source line #6503	-> byte code offset #660
    //   Java source line #6504	-> byte code offset #669
    //   Java source line #6505	-> byte code offset #679
    //   Java source line #6506	-> byte code offset #689
    //   Java source line #6507	-> byte code offset #699
    //   Java source line #6509	-> byte code offset #709
    //   Java source line #6513	-> byte code offset #712
    //   Java source line #6515	-> byte code offset #720
    //   Java source line #6516	-> byte code offset #727
    //   Java source line #6519	-> byte code offset #734
    //   Java source line #6521	-> byte code offset #742
    //   Java source line #6522	-> byte code offset #750
    //   Java source line #6523	-> byte code offset #759
    //   Java source line #6524	-> byte code offset #769
    //   Java source line #6525	-> byte code offset #779
    //   Java source line #6527	-> byte code offset #789
    //   Java source line #6531	-> byte code offset #792
    //   Java source line #6533	-> byte code offset #818
    //   Java source line #6534	-> byte code offset #826
    //   Java source line #6535	-> byte code offset #835
    //   Java source line #6536	-> byte code offset #845
    //   Java source line #6537	-> byte code offset #848
    //   Java source line #6539	-> byte code offset #854
    //   Java source line #6541	-> byte code offset #862
    //   Java source line #6542	-> byte code offset #869
    //   Java source line #6545	-> byte code offset #876
    //   Java source line #6546	-> byte code offset #884
    //   Java source line #6547	-> byte code offset #893
    //   Java source line #6548	-> byte code offset #903
    //   Java source line #6550	-> byte code offset #913
    //   Java source line #6553	-> byte code offset #921
    //   Java source line #6557	-> byte code offset #924
    //   Java source line #6558	-> byte code offset #934
    //   Java source line #6559	-> byte code offset #942
    //   Java source line #6560	-> byte code offset #950
    //   Java source line #6562	-> byte code offset #960
    //   Java source line #6563	-> byte code offset #965
    //   Java source line #6564	-> byte code offset #968
    //   Java source line #6566	-> byte code offset #971
    //   Java source line #6569	-> byte code offset #974
    //   Java source line #6570	-> byte code offset #982
    //   Java source line #6571	-> byte code offset #991
    //   Java source line #6573	-> byte code offset #1001
    //   Java source line #6574	-> byte code offset #1009
    //   Java source line #6576	-> byte code offset #1012
    //   Java source line #6580	-> byte code offset #1015
    //   Java source line #6582	-> byte code offset #1023
    //   Java source line #6583	-> byte code offset #1030
    //   Java source line #6586	-> byte code offset #1037
    //   Java source line #6588	-> byte code offset #1045
    //   Java source line #6589	-> byte code offset #1053
    //   Java source line #6591	-> byte code offset #1062
    //   Java source line #6595	-> byte code offset #1065
    //   Java source line #6597	-> byte code offset #1073
    //   Java source line #6598	-> byte code offset #1080
    //   Java source line #6601	-> byte code offset #1087
    //   Java source line #6603	-> byte code offset #1095
    //   Java source line #6604	-> byte code offset #1103
    //   Java source line #6606	-> byte code offset #1106
    //   Java source line #6610	-> byte code offset #1109
    //   Java source line #6612	-> byte code offset #1117
    //   Java source line #6613	-> byte code offset #1124
    //   Java source line #6616	-> byte code offset #1131
    //   Java source line #6617	-> byte code offset #1139
    //   Java source line #6618	-> byte code offset #1142
    //   Java source line #6620	-> byte code offset #1145
    //   Java source line #6624	-> byte code offset #1148
    //   Java source line #6630	-> byte code offset #1200
    //   Java source line #6631	-> byte code offset #1207
    //   Java source line #6635	-> byte code offset #1230
    //   Java source line #6642	-> byte code offset #1275
    //   Java source line #6643	-> byte code offset #1283
    //   Java source line #6644	-> byte code offset #1285
    //   Java source line #6647	-> byte code offset #1339
    //   Java source line #6649	-> byte code offset #1347
    // Local variable table:
    //   start	length	slot	name	signature
    //   0	1350	0	this	ResultSetImpl
    //   0	1350	1	columnIndex	int
    //   0	1350	2	targetCalendar	Calendar
    //   0	1350	3	timestampAsBytes	byte[]
    //   0	1350	4	tz	TimeZone
    //   0	1350	5	rollForward	boolean
    //   23	609	6	length	int
    //   1283	59	6	e	Exception
    //   49	1190	7	sessionCalendar	Calendar
    //   1337	11	7	sqlEx	SQLException
    //   58	119	9	allZeroTimestamp	boolean
    //   75	97	10	onlyTimePresent	boolean
    //   78	88	11	i	int
    //   400	841	11	year	int
    //   91	62	12	b	byte
    //   403	840	12	month	int
    //   406	839	13	day	int
    //   409	838	14	hour	int
    //   412	837	15	minutes	int
    //   415	836	16	seconds	int
    //   418	835	17	nanos	int
    //   609	19	18	decimalIndex	int
    //   1275	6	19	localObject1	Object
    // Exception table:
    //   from	to	target	type
    //   57	206	1275	finally
    //   207	281	1275	finally
    //   282	297	1275	finally
    //   298	341	1275	finally
    //   342	384	1275	finally
    //   385	1229	1275	finally
    //   1230	1274	1275	finally
    //   1275	1280	1275	finally
    //   5	20	1283	java/lang/Exception
    //   21	206	1283	java/lang/Exception
    //   207	281	1283	java/lang/Exception
    //   282	297	1283	java/lang/Exception
    //   298	341	1283	java/lang/Exception
    //   342	384	1283	java/lang/Exception
    //   385	1229	1283	java/lang/Exception
    //   1230	1274	1283	java/lang/Exception
    //   1275	1283	1283	java/lang/Exception
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\mysql\jdbc\ResultSetImpl.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */