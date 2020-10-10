package com.mysql.jdbc;

import com.mysql.jdbc.log.Log;
import com.mysql.jdbc.log.LogFactory;
import com.mysql.jdbc.log.NullLogger;
import com.mysql.jdbc.profiler.ProfilerEvent;
import com.mysql.jdbc.profiler.ProfilerEventHandler;
import com.mysql.jdbc.profiler.ProfilerEventHandlerFactory;
import com.mysql.jdbc.util.LRUCache;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.UnsupportedCharsetException;
import java.sql.Blob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Savepoint;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.Stack;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TreeMap;

public class ConnectionImpl
  extends ConnectionPropertiesImpl
  implements Connection
{
  private static final String JDBC_LOCAL_CHARACTER_SET_RESULTS = "jdbc.local.character_set_results";
  
  class ExceptionInterceptorChain
    implements ExceptionInterceptor
  {
    List interceptors;
    
    ExceptionInterceptorChain(String interceptorClasses)
      throws SQLException
    {
      this.interceptors = Util.loadExtensions(ConnectionImpl.this, ConnectionImpl.this.props, interceptorClasses, "Connection.BadExceptionInterceptor", this);
    }
    
    public SQLException interceptException(SQLException sqlEx)
    {
      if (this.interceptors != null)
      {
        Iterator iter = this.interceptors.iterator();
        while (iter.hasNext()) {
          sqlEx = ((ExceptionInterceptor)iter.next()).interceptException(sqlEx);
        }
      }
      return sqlEx;
    }
    
    public void destroy()
    {
      if (this.interceptors != null)
      {
        Iterator iter = this.interceptors.iterator();
        while (iter.hasNext()) {
          ((ExceptionInterceptor)iter.next()).destroy();
        }
      }
    }
    
    public void init(Connection conn, Properties props)
      throws SQLException
    {
      if (this.interceptors != null)
      {
        Iterator iter = this.interceptors.iterator();
        while (iter.hasNext()) {
          ((ExceptionInterceptor)iter.next()).init(conn, props);
        }
      }
    }
  }
  
  class CompoundCacheKey
  {
    String componentOne;
    String componentTwo;
    int hashCode;
    
    CompoundCacheKey(String partOne, String partTwo)
    {
      this.componentOne = partOne;
      this.componentTwo = partTwo;
      
      this.hashCode = ((this.componentOne != null ? this.componentOne : "") + this.componentTwo).hashCode();
    }
    
    public boolean equals(Object obj)
    {
      if ((obj instanceof CompoundCacheKey))
      {
        CompoundCacheKey another = (CompoundCacheKey)obj;
        
        boolean firstPartEqual = false;
        if (this.componentOne == null) {
          firstPartEqual = another.componentOne == null;
        } else {
          firstPartEqual = this.componentOne.equals(another.componentOne);
        }
        return (firstPartEqual) && (this.componentTwo.equals(another.componentTwo));
      }
      return false;
    }
    
    public int hashCode()
    {
      return this.hashCode;
    }
  }
  
  private static final Object CHARSET_CONVERTER_NOT_AVAILABLE_MARKER = new Object();
  public static Map charsetMap;
  protected static final String DEFAULT_LOGGER_CLASS = "com.mysql.jdbc.log.StandardLogger";
  private static final int HISTOGRAM_BUCKETS = 20;
  private static final String LOGGER_INSTANCE_NAME = "MySQL";
  private static Map mapTransIsolationNameToValue = null;
  private static final Log NULL_LOGGER = new NullLogger("MySQL");
  private static Map roundRobinStatsMap;
  private static final Map serverCollationByUrl = new HashMap();
  private static final Map serverConfigByUrl = new HashMap();
  private long queryTimeCount;
  private double queryTimeSum;
  private double queryTimeSumSquares;
  private double queryTimeMean;
  private static Timer cancelTimer;
  private List connectionLifecycleInterceptors;
  private static final Constructor JDBC_4_CONNECTION_CTOR;
  private static final int DEFAULT_RESULT_SET_TYPE = 1003;
  private static final int DEFAULT_RESULT_SET_CONCURRENCY = 1007;
  
  static
  {
    mapTransIsolationNameToValue = new HashMap(8);
    mapTransIsolationNameToValue.put("READ-UNCOMMITED", Constants.integerValueOf(1));
    
    mapTransIsolationNameToValue.put("READ-UNCOMMITTED", Constants.integerValueOf(1));
    
    mapTransIsolationNameToValue.put("READ-COMMITTED", Constants.integerValueOf(2));
    
    mapTransIsolationNameToValue.put("REPEATABLE-READ", Constants.integerValueOf(4));
    
    mapTransIsolationNameToValue.put("SERIALIZABLE", Constants.integerValueOf(8));
    
    boolean createdNamedTimer = false;
    try
    {
      Constructor ctr = class$java$util$Timer.getConstructor(new Class[] { String.class, Boolean.TYPE });
      
      cancelTimer = (Timer)ctr.newInstance(new Object[] { "MySQL Statement Cancellation Timer", Boolean.TRUE });
      createdNamedTimer = true;
    }
    catch (Throwable t)
    {
      createdNamedTimer = false;
    }
    if (!createdNamedTimer) {
      cancelTimer = new Timer(true);
    }
    if (Util.isJdbc4()) {
      try
      {
        JDBC_4_CONNECTION_CTOR = Class.forName("com.mysql.jdbc.JDBC4Connection").getConstructor(new Class[] { String.class, Integer.TYPE, Properties.class, String.class, String.class });
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
      JDBC_4_CONNECTION_CTOR = null;
    }
  }
  
  protected static SQLException appendMessageToException(SQLException sqlEx, String messageToAppend, ExceptionInterceptor interceptor)
  {
    String origMessage = sqlEx.getMessage();
    String sqlState = sqlEx.getSQLState();
    int vendorErrorCode = sqlEx.getErrorCode();
    
    StringBuffer messageBuf = new StringBuffer(origMessage.length() + messageToAppend.length());
    
    messageBuf.append(origMessage);
    messageBuf.append(messageToAppend);
    
    SQLException sqlExceptionWithNewMessage = SQLError.createSQLException(messageBuf.toString(), sqlState, vendorErrorCode, interceptor);
    try
    {
      Method getStackTraceMethod = null;
      Method setStackTraceMethod = null;
      Object theStackTraceAsObject = null;
      
      Class stackTraceElementClass = Class.forName("java.lang.StackTraceElement");
      
      Class stackTraceElementArrayClass = Array.newInstance(stackTraceElementClass, new int[] { 0 }).getClass();
      
      getStackTraceMethod = Throwable.class.getMethod("getStackTrace", new Class[0]);
      
      setStackTraceMethod = class$java$lang$Throwable.getMethod("setStackTrace", new Class[] { stackTraceElementArrayClass });
      if ((getStackTraceMethod != null) && (setStackTraceMethod != null))
      {
        theStackTraceAsObject = getStackTraceMethod.invoke(sqlEx, new Object[0]);
        
        setStackTraceMethod.invoke(sqlExceptionWithNewMessage, new Object[] { theStackTraceAsObject });
      }
    }
    catch (NoClassDefFoundError noClassDefFound) {}catch (NoSuchMethodException noSuchMethodEx) {}catch (Throwable catchAll) {}
    return sqlExceptionWithNewMessage;
  }
  
  protected static Timer getCancelTimer()
  {
    return cancelTimer;
  }
  
  protected static Connection getInstance(String hostToConnectTo, int portToConnectTo, Properties info, String databaseToConnectTo, String url)
    throws SQLException
  {
    if (!Util.isJdbc4()) {
      return new ConnectionImpl(hostToConnectTo, portToConnectTo, info, databaseToConnectTo, url);
    }
    return (Connection)Util.handleNewInstance(JDBC_4_CONNECTION_CTOR, new Object[] { hostToConnectTo, Constants.integerValueOf(portToConnectTo), info, databaseToConnectTo, url }, null);
  }
  
  private static synchronized int getNextRoundRobinHostIndex(String url, List hostList)
  {
    int indexRange = hostList.size();
    
    int index = (int)(Math.random() * indexRange);
    
    return index;
  }
  
  private static boolean nullSafeCompare(String s1, String s2)
  {
    if ((s1 == null) && (s2 == null)) {
      return true;
    }
    if ((s1 == null) && (s2 != null)) {
      return false;
    }
    return s1.equals(s2);
  }
  
  private boolean autoCommit = true;
  private Map cachedPreparedStatementParams;
  private String characterSetMetadata = null;
  private String characterSetResultsOnServer = null;
  private Map charsetConverterMap = new HashMap(CharsetMapping.getNumberOfCharsetsConfigured());
  private Map charsetToNumBytesMap;
  private long connectionCreationTimeMillis = 0L;
  private long connectionId;
  private String database = null;
  private java.sql.DatabaseMetaData dbmd = null;
  private TimeZone defaultTimeZone;
  private ProfilerEventHandler eventSink;
  private boolean executingFailoverReconnect = false;
  private boolean failedOver = false;
  private Throwable forceClosedReason;
  private Throwable forcedClosedLocation;
  private boolean hasIsolationLevels = false;
  private boolean hasQuotedIdentifiers = false;
  private String host = null;
  private List hostList = null;
  private int hostListSize = 0;
  private String[] indexToCharsetMapping = CharsetMapping.INDEX_TO_CHARSET;
  private MysqlIO io = null;
  private boolean isClientTzUTC = false;
  private boolean isClosed = true;
  private boolean isInGlobalTx = false;
  private boolean isRunningOnJDK13 = false;
  private int isolationLevel = 2;
  private boolean isServerTzUTC = false;
  private long lastQueryFinishedTime = 0L;
  private Log log = NULL_LOGGER;
  private long longestQueryTimeMs = 0L;
  private boolean lowerCaseTableNames = false;
  private long masterFailTimeMillis = 0L;
  private long maximumNumberTablesAccessed = 0L;
  private boolean maxRowsChanged = false;
  private long metricsLastReportedMs;
  private long minimumNumberTablesAccessed = Long.MAX_VALUE;
  private final Object mutex = new Object();
  private String myURL = null;
  private boolean needsPing = false;
  private int netBufferLength = 16384;
  private boolean noBackslashEscapes = false;
  private long numberOfPreparedExecutes = 0L;
  private long numberOfPrepares = 0L;
  private long numberOfQueriesIssued = 0L;
  private long numberOfResultSetsCreated = 0L;
  private long[] numTablesMetricsHistBreakpoints;
  private int[] numTablesMetricsHistCounts;
  private long[] oldHistBreakpoints = null;
  private int[] oldHistCounts = null;
  private Map openStatements;
  private LRUCache parsedCallableStatementCache;
  private boolean parserKnowsUnicode = false;
  private String password = null;
  private long[] perfMetricsHistBreakpoints;
  private int[] perfMetricsHistCounts;
  private Throwable pointOfOrigin;
  private int port = 3306;
  private boolean preferSlaveDuringFailover = false;
  protected Properties props = null;
  private long queriesIssuedFailedOver = 0L;
  private boolean readInfoMsg = false;
  private boolean readOnly = false;
  protected LRUCache resultSetMetadataCache;
  private TimeZone serverTimezoneTZ = null;
  private Map serverVariables = null;
  private long shortestQueryTimeMs = Long.MAX_VALUE;
  private Map statementsUsingMaxRows;
  private double totalQueryTimeMs = 0.0D;
  private boolean transactionsSupported = false;
  private Map typeMap;
  private boolean useAnsiQuotes = false;
  private String user = null;
  private boolean useServerPreparedStmts = false;
  private LRUCache serverSideStatementCheckCache;
  private LRUCache serverSideStatementCache;
  private Calendar sessionCalendar;
  private Calendar utcCalendar;
  private String origHostToConnectTo;
  private int origPortToConnectTo;
  private String origDatabaseToConnectTo;
  private String errorMessageEncoding = "Cp1252";
  private boolean usePlatformCharsetConverters;
  private boolean hasTriedMasterFlag = false;
  private String statementComment = null;
  private boolean storesLowerCaseTableName;
  private List statementInterceptors;
  private boolean requiresEscapingEncoder;
  
  protected ConnectionImpl(String hostToConnectTo, int portToConnectTo, Properties info, String databaseToConnectTo, String url)
    throws SQLException
  {
    this.charsetToNumBytesMap = new HashMap();
    
    this.connectionCreationTimeMillis = System.currentTimeMillis();
    this.pointOfOrigin = new Throwable();
    
    this.origHostToConnectTo = hostToConnectTo;
    this.origPortToConnectTo = portToConnectTo;
    this.origDatabaseToConnectTo = databaseToConnectTo;
    try
    {
      class$java$sql$Blob.getMethod("truncate", new Class[] { Long.TYPE });
      
      this.isRunningOnJDK13 = false;
    }
    catch (NoSuchMethodException nsme)
    {
      this.isRunningOnJDK13 = true;
    }
    this.sessionCalendar = new GregorianCalendar();
    this.utcCalendar = new GregorianCalendar();
    this.utcCalendar.setTimeZone(TimeZone.getTimeZone("GMT"));
    
    this.log = LogFactory.getLogger(getLogger(), "MySQL", getExceptionInterceptor());
    
    this.defaultTimeZone = Util.getDefaultTimeZone();
    if ("GMT".equalsIgnoreCase(this.defaultTimeZone.getID())) {
      this.isClientTzUTC = true;
    } else {
      this.isClientTzUTC = false;
    }
    this.openStatements = new HashMap();
    this.serverVariables = new HashMap();
    this.hostList = new ArrayList();
    
    int numHosts = Integer.parseInt(info.getProperty("NUM_HOSTS"));
    if (hostToConnectTo == null)
    {
      this.host = "localhost";
      this.hostList.add(this.host + ":" + portToConnectTo);
    }
    else if (numHosts > 1)
    {
      for (int i = 0; i < numHosts; i++)
      {
        int index = i + 1;
        
        this.hostList.add(info.getProperty(new StringBuffer().append("HOST.").append(index).toString()) + ":" + info.getProperty(new StringBuffer().append("PORT.").append(index).toString()));
      }
    }
    else
    {
      this.host = hostToConnectTo;
      if (hostToConnectTo.indexOf(":") == -1) {
        this.hostList.add(this.host + ":" + portToConnectTo);
      } else {
        this.hostList.add(this.host);
      }
    }
    this.hostListSize = this.hostList.size();
    this.port = portToConnectTo;
    if (databaseToConnectTo == null) {
      databaseToConnectTo = "";
    }
    this.database = databaseToConnectTo;
    this.myURL = url;
    this.user = info.getProperty("user");
    this.password = info.getProperty("password");
    if ((this.user == null) || (this.user.equals(""))) {
      this.user = "";
    }
    if (this.password == null) {
      this.password = "";
    }
    this.props = info;
    initializeDriverProperties(info);
    try
    {
      this.dbmd = getMetaData(false, false);
      createNewIO(false);
      initializeStatementInterceptors();
      this.io.setStatementInterceptors(this.statementInterceptors);
    }
    catch (SQLException ex)
    {
      cleanup(ex);
      
      throw ex;
    }
    catch (Exception ex)
    {
      cleanup(ex);
      
      StringBuffer mesg = new StringBuffer(128);
      if (!getParanoid())
      {
        mesg.append("Cannot connect to MySQL server on ");
        mesg.append(this.host);
        mesg.append(":");
        mesg.append(this.port);
        mesg.append(".\n\n");
        mesg.append("Make sure that there is a MySQL server ");
        mesg.append("running on the machine/port you are trying ");
        mesg.append("to connect to and that the machine this software is running on ");
        
        mesg.append("is able to connect to this host/port (i.e. not firewalled). ");
        
        mesg.append("Also make sure that the server has not been started with the --skip-networking ");
        
        mesg.append("flag.\n\n");
      }
      else
      {
        mesg.append("Unable to connect to database.");
      }
      SQLException sqlEx = SQLError.createSQLException(mesg.toString(), "08S01", getExceptionInterceptor());
      
      sqlEx.initCause(ex);
      
      throw sqlEx;
    }
  }
  
  protected void initializeStatementInterceptors()
    throws SQLException
  {
    this.statementInterceptors = Util.loadExtensions(this, this.props, getStatementInterceptors(), "MysqlIo.BadStatementInterceptor", getExceptionInterceptor());
  }
  
  protected List getStatementInterceptorsInstances()
  {
    return this.statementInterceptors;
  }
  
  private void addToHistogram(int[] histogramCounts, long[] histogramBreakpoints, long value, int numberOfTimes, long currentLowerBound, long currentUpperBound)
  {
    if (histogramCounts == null) {
      createInitialHistogram(histogramBreakpoints, currentLowerBound, currentUpperBound);
    } else {
      for (int i = 0; i < 20; i++) {
        if (histogramBreakpoints[i] >= value)
        {
          histogramCounts[i] += numberOfTimes;
          
          break;
        }
      }
    }
  }
  
  private void addToPerformanceHistogram(long value, int numberOfTimes)
  {
    checkAndCreatePerformanceHistogram();
    
    addToHistogram(this.perfMetricsHistCounts, this.perfMetricsHistBreakpoints, value, numberOfTimes, this.shortestQueryTimeMs == Long.MAX_VALUE ? 0L : this.shortestQueryTimeMs, this.longestQueryTimeMs);
  }
  
  private void addToTablesAccessedHistogram(long value, int numberOfTimes)
  {
    checkAndCreateTablesAccessedHistogram();
    
    addToHistogram(this.numTablesMetricsHistCounts, this.numTablesMetricsHistBreakpoints, value, numberOfTimes, this.minimumNumberTablesAccessed == Long.MAX_VALUE ? 0L : this.minimumNumberTablesAccessed, this.maximumNumberTablesAccessed);
  }
  
  private void buildCollationMapping()
    throws SQLException
  {
    if (versionMeetsMinimum(4, 1, 0))
    {
      TreeMap sortedCollationMap = null;
      if (getCacheServerConfiguration()) {
        synchronized (serverConfigByUrl)
        {
          sortedCollationMap = (TreeMap)serverCollationByUrl.get(getURL());
        }
      }
      java.sql.Statement stmt = null;
      ResultSet results = null;
      try
      {
        if (sortedCollationMap == null)
        {
          sortedCollationMap = new TreeMap();
          
          stmt = getMetadataSafeStatement();
          
          results = stmt.executeQuery("SHOW COLLATION");
          while (results.next())
          {
            String charsetName = results.getString(2);
            Integer charsetIndex = Constants.integerValueOf(results.getInt(3));
            
            sortedCollationMap.put(charsetIndex, charsetName);
          }
          if (getCacheServerConfiguration()) {
            synchronized (serverConfigByUrl)
            {
              serverCollationByUrl.put(getURL(), sortedCollationMap);
            }
          }
        }
        int highestIndex = ((Integer)sortedCollationMap.lastKey()).intValue();
        if (CharsetMapping.INDEX_TO_CHARSET.length > highestIndex) {
          highestIndex = CharsetMapping.INDEX_TO_CHARSET.length;
        }
        this.indexToCharsetMapping = new String[highestIndex + 1];
        for (int i = 0; i < CharsetMapping.INDEX_TO_CHARSET.length; i++) {
          this.indexToCharsetMapping[i] = CharsetMapping.INDEX_TO_CHARSET[i];
        }
        Iterator indexIter = sortedCollationMap.entrySet().iterator();
        while (indexIter.hasNext())
        {
          Map.Entry indexEntry = (Map.Entry)indexIter.next();
          
          String mysqlCharsetName = (String)indexEntry.getValue();
          
          this.indexToCharsetMapping[((Integer)indexEntry.getKey()).intValue()] = CharsetMapping.getJavaEncodingForMysqlEncoding(mysqlCharsetName, this);
        }
      }
      catch (SQLException e)
      {
        throw e;
      }
      finally
      {
        if (results != null) {
          try
          {
            results.close();
          }
          catch (SQLException sqlE) {}
        }
        if (stmt != null) {
          try
          {
            stmt.close();
          }
          catch (SQLException sqlE) {}
        }
      }
    }
    else
    {
      this.indexToCharsetMapping = CharsetMapping.INDEX_TO_CHARSET;
    }
  }
  
  private boolean canHandleAsServerPreparedStatement(String sql)
    throws SQLException
  {
    if ((sql == null) || (sql.length() == 0)) {
      return true;
    }
    if (!this.useServerPreparedStmts) {
      return false;
    }
    if (getCachePreparedStatements()) {
      synchronized (this.serverSideStatementCheckCache)
      {
        Boolean flag = (Boolean)this.serverSideStatementCheckCache.get(sql);
        if (flag != null) {
          return flag.booleanValue();
        }
        boolean canHandle = canHandleAsServerPreparedStatementNoCache(sql);
        if (sql.length() < getPreparedStatementCacheSqlLimit()) {
          this.serverSideStatementCheckCache.put(sql, canHandle ? Boolean.TRUE : Boolean.FALSE);
        }
        return canHandle;
      }
    }
    return canHandleAsServerPreparedStatementNoCache(sql);
  }
  
  private boolean canHandleAsServerPreparedStatementNoCache(String sql)
    throws SQLException
  {
    if (StringUtils.startsWithIgnoreCaseAndNonAlphaNumeric(sql, "CALL")) {
      return false;
    }
    boolean canHandleAsStatement = true;
    if ((!versionMeetsMinimum(5, 0, 7)) && ((StringUtils.startsWithIgnoreCaseAndNonAlphaNumeric(sql, "SELECT")) || (StringUtils.startsWithIgnoreCaseAndNonAlphaNumeric(sql, "DELETE")) || (StringUtils.startsWithIgnoreCaseAndNonAlphaNumeric(sql, "INSERT")) || (StringUtils.startsWithIgnoreCaseAndNonAlphaNumeric(sql, "UPDATE")) || (StringUtils.startsWithIgnoreCaseAndNonAlphaNumeric(sql, "REPLACE"))))
    {
      int currentPos = 0;
      int statementLength = sql.length();
      int lastPosToLook = statementLength - 7;
      boolean allowBackslashEscapes = !this.noBackslashEscapes;
      char quoteChar = this.useAnsiQuotes ? '"' : '\'';
      boolean foundLimitWithPlaceholder = false;
      while (currentPos < lastPosToLook)
      {
        int limitStart = StringUtils.indexOfIgnoreCaseRespectQuotes(currentPos, sql, "LIMIT ", quoteChar, allowBackslashEscapes);
        if (limitStart == -1) {
          break;
        }
        currentPos = limitStart + 7;
        while (currentPos < statementLength)
        {
          char c = sql.charAt(currentPos);
          if ((!Character.isDigit(c)) && (!Character.isWhitespace(c)) && (c != ',') && (c != '?')) {
            break;
          }
          if (c == '?')
          {
            foundLimitWithPlaceholder = true;
            break;
          }
          currentPos++;
        }
      }
      canHandleAsStatement = !foundLimitWithPlaceholder;
    }
    else if (StringUtils.startsWithIgnoreCaseAndWs(sql, "CREATE TABLE"))
    {
      canHandleAsStatement = false;
    }
    else if (StringUtils.startsWithIgnoreCaseAndWs(sql, "DO"))
    {
      canHandleAsStatement = false;
    }
    else if (StringUtils.startsWithIgnoreCaseAndWs(sql, "SET"))
    {
      canHandleAsStatement = false;
    }
    return canHandleAsStatement;
  }
  
  public void changeUser(String userName, String newPassword)
    throws SQLException
  {
    if ((userName == null) || (userName.equals(""))) {
      userName = "";
    }
    if (newPassword == null) {
      newPassword = "";
    }
    this.io.changeUser(userName, newPassword, this.database);
    this.user = userName;
    this.password = newPassword;
    if (versionMeetsMinimum(4, 1, 0)) {
      configureClientCharacterSet(true);
    }
    setupServerForTruncationChecks();
  }
  
  private boolean characterSetNamesMatches(String mysqlEncodingName)
  {
    return (mysqlEncodingName != null) && (mysqlEncodingName.equalsIgnoreCase((String)this.serverVariables.get("character_set_client"))) && (mysqlEncodingName.equalsIgnoreCase((String)this.serverVariables.get("character_set_connection")));
  }
  
  private void checkAndCreatePerformanceHistogram()
  {
    if (this.perfMetricsHistCounts == null) {
      this.perfMetricsHistCounts = new int[20];
    }
    if (this.perfMetricsHistBreakpoints == null) {
      this.perfMetricsHistBreakpoints = new long[20];
    }
  }
  
  private void checkAndCreateTablesAccessedHistogram()
  {
    if (this.numTablesMetricsHistCounts == null) {
      this.numTablesMetricsHistCounts = new int[20];
    }
    if (this.numTablesMetricsHistBreakpoints == null) {
      this.numTablesMetricsHistBreakpoints = new long[20];
    }
  }
  
  protected void checkClosed()
    throws SQLException
  {
    if (this.isClosed) {
      throwConnectionClosedException();
    }
  }
  
  private void throwConnectionClosedException()
    throws SQLException
  {
    StringBuffer messageBuf = new StringBuffer("No operations allowed after connection closed.");
    if ((this.forcedClosedLocation != null) || (this.forceClosedReason != null)) {
      messageBuf.append("Connection was implicitly closed by the driver.");
    }
    SQLException ex = SQLError.createSQLException(messageBuf.toString(), "08003", getExceptionInterceptor());
    if (this.forceClosedReason != null) {
      ex.initCause(this.forceClosedReason);
    }
    throw ex;
  }
  
  private void checkServerEncoding()
    throws SQLException
  {
    if ((getUseUnicode()) && (getEncoding() != null)) {
      return;
    }
    String serverEncoding = (String)this.serverVariables.get("character_set");
    if (serverEncoding == null) {
      serverEncoding = (String)this.serverVariables.get("character_set_server");
    }
    String mappedServerEncoding = null;
    if (serverEncoding != null) {
      mappedServerEncoding = CharsetMapping.getJavaEncodingForMysqlEncoding(serverEncoding.toUpperCase(Locale.ENGLISH), this);
    }
    if ((!getUseUnicode()) && (mappedServerEncoding != null))
    {
      SingleByteCharsetConverter converter = getCharsetConverter(mappedServerEncoding);
      if (converter != null)
      {
        setUseUnicode(true);
        setEncoding(mappedServerEncoding);
        
        return;
      }
    }
    if (serverEncoding != null)
    {
      if (mappedServerEncoding == null) {
        if (Character.isLowerCase(serverEncoding.charAt(0)))
        {
          char[] ach = serverEncoding.toCharArray();
          ach[0] = Character.toUpperCase(serverEncoding.charAt(0));
          setEncoding(new String(ach));
        }
      }
      if (mappedServerEncoding == null) {
        throw SQLError.createSQLException("Unknown character encoding on server '" + serverEncoding + "', use 'characterEncoding=' property " + " to provide correct mapping", "01S00", getExceptionInterceptor());
      }
      try
      {
        "abc".getBytes(mappedServerEncoding);
        setEncoding(mappedServerEncoding);
        setUseUnicode(true);
      }
      catch (UnsupportedEncodingException UE)
      {
        throw SQLError.createSQLException("The driver can not map the character encoding '" + getEncoding() + "' that your server is using " + "to a character encoding your JVM understands. You " + "can specify this mapping manually by adding \"useUnicode=true\" " + "as well as \"characterEncoding=[an_encoding_your_jvm_understands]\" " + "to your JDBC URL.", "0S100", getExceptionInterceptor());
      }
    }
  }
  
  private void checkTransactionIsolationLevel()
    throws SQLException
  {
    String txIsolationName = null;
    if (versionMeetsMinimum(4, 0, 3)) {
      txIsolationName = "tx_isolation";
    } else {
      txIsolationName = "transaction_isolation";
    }
    String s = (String)this.serverVariables.get(txIsolationName);
    if (s != null)
    {
      Integer intTI = (Integer)mapTransIsolationNameToValue.get(s);
      if (intTI != null) {
        this.isolationLevel = intTI.intValue();
      }
    }
  }
  
  protected void abortInternal()
    throws SQLException
  {
    if (this.io != null)
    {
      try
      {
        this.io.forceClose();
      }
      catch (Throwable t) {}
      this.io = null;
    }
    this.isClosed = true;
  }
  
  private void cleanup(Throwable whyCleanedUp)
  {
    try
    {
      if ((this.io != null) && (!isClosed())) {
        realClose(false, false, false, whyCleanedUp);
      } else if (this.io != null) {
        this.io.forceClose();
      }
    }
    catch (SQLException sqlEx) {}
    this.isClosed = true;
  }
  
  public void clearHasTriedMaster()
  {
    this.hasTriedMasterFlag = false;
  }
  
  public java.sql.PreparedStatement clientPrepareStatement(String sql)
    throws SQLException
  {
    return clientPrepareStatement(sql, 1003, 1007);
  }
  
  public java.sql.PreparedStatement clientPrepareStatement(String sql, int autoGenKeyIndex)
    throws SQLException
  {
    java.sql.PreparedStatement pStmt = clientPrepareStatement(sql);
    
    ((PreparedStatement)pStmt).setRetrieveGeneratedKeys(autoGenKeyIndex == 1);
    
    return pStmt;
  }
  
  public java.sql.PreparedStatement clientPrepareStatement(String sql, int resultSetType, int resultSetConcurrency)
    throws SQLException
  {
    return clientPrepareStatement(sql, resultSetType, resultSetConcurrency, true);
  }
  
  protected java.sql.PreparedStatement clientPrepareStatement(String sql, int resultSetType, int resultSetConcurrency, boolean processEscapeCodesIfNeeded)
    throws SQLException
  {
    checkClosed();
    
    String nativeSql = (processEscapeCodesIfNeeded) && (getProcessEscapeCodesForPrepStmts()) ? nativeSQL(sql) : sql;
    
    PreparedStatement pStmt = null;
    if (getCachePreparedStatements()) {
      synchronized (this.cachedPreparedStatementParams)
      {
        PreparedStatement.ParseInfo pStmtInfo = (PreparedStatement.ParseInfo)this.cachedPreparedStatementParams.get(nativeSql);
        if (pStmtInfo == null)
        {
          pStmt = PreparedStatement.getInstance(this, nativeSql, this.database);
          
          PreparedStatement.ParseInfo parseInfo = pStmt.getParseInfo();
          if (parseInfo.statementLength < getPreparedStatementCacheSqlLimit())
          {
            if (this.cachedPreparedStatementParams.size() >= getPreparedStatementCacheSize())
            {
              Iterator oldestIter = this.cachedPreparedStatementParams.keySet().iterator();
              
              long lruTime = Long.MAX_VALUE;
              String oldestSql = null;
              while (oldestIter.hasNext())
              {
                String sqlKey = (String)oldestIter.next();
                PreparedStatement.ParseInfo lruInfo = (PreparedStatement.ParseInfo)this.cachedPreparedStatementParams.get(sqlKey);
                if (lruInfo.lastUsed < lruTime)
                {
                  lruTime = lruInfo.lastUsed;
                  oldestSql = sqlKey;
                }
              }
              if (oldestSql != null) {
                this.cachedPreparedStatementParams.remove(oldestSql);
              }
            }
            this.cachedPreparedStatementParams.put(nativeSql, pStmt.getParseInfo());
          }
        }
        else
        {
          pStmtInfo.lastUsed = System.currentTimeMillis();
          pStmt = new PreparedStatement(this, nativeSql, this.database, pStmtInfo);
        }
      }
    }
    pStmt = PreparedStatement.getInstance(this, nativeSql, this.database);
    
    pStmt.setResultSetType(resultSetType);
    pStmt.setResultSetConcurrency(resultSetConcurrency);
    
    return pStmt;
  }
  
  public java.sql.PreparedStatement clientPrepareStatement(String sql, int[] autoGenKeyIndexes)
    throws SQLException
  {
    PreparedStatement pStmt = (PreparedStatement)clientPrepareStatement(sql);
    
    pStmt.setRetrieveGeneratedKeys((autoGenKeyIndexes != null) && (autoGenKeyIndexes.length > 0));
    
    return pStmt;
  }
  
  public java.sql.PreparedStatement clientPrepareStatement(String sql, String[] autoGenKeyColNames)
    throws SQLException
  {
    PreparedStatement pStmt = (PreparedStatement)clientPrepareStatement(sql);
    
    pStmt.setRetrieveGeneratedKeys((autoGenKeyColNames != null) && (autoGenKeyColNames.length > 0));
    
    return pStmt;
  }
  
  public java.sql.PreparedStatement clientPrepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability)
    throws SQLException
  {
    return clientPrepareStatement(sql, resultSetType, resultSetConcurrency, true);
  }
  
  public synchronized void close()
    throws SQLException
  {
    if (this.connectionLifecycleInterceptors != null) {
      new IterateBlock(this.connectionLifecycleInterceptors.iterator())
      {
        void forEach(Object each)
          throws SQLException
        {
          ((ConnectionLifecycleInterceptor)each).close();
        }
      }.doForAll();
    }
    realClose(true, true, false, null);
  }
  
  private void closeAllOpenStatements()
    throws SQLException
  {
    SQLException postponedException = null;
    if (this.openStatements != null)
    {
      List currentlyOpenStatements = new ArrayList();
      
      Iterator iter = this.openStatements.keySet().iterator();
      while (iter.hasNext()) {
        currentlyOpenStatements.add(iter.next());
      }
      int numStmts = currentlyOpenStatements.size();
      for (int i = 0; i < numStmts; i++)
      {
        StatementImpl stmt = (StatementImpl)currentlyOpenStatements.get(i);
        try
        {
          stmt.realClose(false, true);
        }
        catch (SQLException sqlEx)
        {
          postponedException = sqlEx;
        }
      }
      if (postponedException != null) {
        throw postponedException;
      }
    }
  }
  
  private void closeStatement(java.sql.Statement stmt)
  {
    if (stmt != null)
    {
      try
      {
        stmt.close();
      }
      catch (SQLException sqlEx) {}
      stmt = null;
    }
  }
  
  public void commit()
    throws SQLException
  {
    synchronized (getMutex())
    {
      checkClosed();
      try
      {
        if (this.connectionLifecycleInterceptors != null)
        {
          IterateBlock iter = new IterateBlock(this.connectionLifecycleInterceptors.iterator())
          {
            void forEach(Object each)
              throws SQLException
            {
              if (!((ConnectionLifecycleInterceptor)each).commit()) {
                this.stopIterating = true;
              }
            }
          };
          iter.doForAll();
          if (!iter.fullIteration())
          {
            this.needsPing = getReconnectAtTxEnd(); return;
          }
        }
        if ((this.autoCommit) && (!getRelaxAutoCommit())) {
          throw SQLError.createSQLException("Can't call commit when autocommit=true", getExceptionInterceptor());
        }
        if (this.transactionsSupported)
        {
          if ((getUseLocalTransactionState()) && (versionMeetsMinimum(5, 0, 0)) && 
            (!this.io.inTransactionOnServer()))
          {
            this.needsPing = getReconnectAtTxEnd(); return;
          }
          execSQL(null, "commit", -1, null, 1003, 1007, false, this.database, null, false);
        }
      }
      catch (SQLException sqlException)
      {
        if ("08S01".equals(sqlException.getSQLState())) {
          throw SQLError.createSQLException("Communications link failure during commit(). Transaction resolution unknown.", "08007", getExceptionInterceptor());
        }
        throw sqlException;
      }
      finally
      {
        this.needsPing = getReconnectAtTxEnd();
      }
      return;
    }
  }
  
  private void configureCharsetProperties()
    throws SQLException
  {
    if (getEncoding() != null) {
      try
      {
        String testString = "abc";
        testString.getBytes(getEncoding());
      }
      catch (UnsupportedEncodingException UE)
      {
        String oldEncoding = getEncoding();
        
        setEncoding(CharsetMapping.getJavaEncodingForMysqlEncoding(oldEncoding, this));
        if (getEncoding() == null) {
          throw SQLError.createSQLException("Java does not support the MySQL character encoding  encoding '" + oldEncoding + "'.", "01S00", getExceptionInterceptor());
        }
        try
        {
          String testString = "abc";
          testString.getBytes(getEncoding());
        }
        catch (UnsupportedEncodingException encodingEx)
        {
          throw SQLError.createSQLException("Unsupported character encoding '" + getEncoding() + "'.", "01S00", getExceptionInterceptor());
        }
      }
    }
  }
  
  private boolean configureClientCharacterSet(boolean dontCheckServerMatch)
    throws SQLException
  {
    String realJavaEncoding = getEncoding();
    boolean characterSetAlreadyConfigured = false;
    try
    {
      if (versionMeetsMinimum(4, 1, 0))
      {
        characterSetAlreadyConfigured = true;
        
        setUseUnicode(true);
        
        configureCharsetProperties();
        realJavaEncoding = getEncoding();
        try
        {
          if ((this.props != null) && (this.props.getProperty("com.mysql.jdbc.faultInjection.serverCharsetIndex") != null)) {
            this.io.serverCharsetIndex = Integer.parseInt(this.props.getProperty("com.mysql.jdbc.faultInjection.serverCharsetIndex"));
          }
          serverEncodingToSet = CharsetMapping.INDEX_TO_CHARSET[this.io.serverCharsetIndex];
          if ((serverEncodingToSet == null) || (serverEncodingToSet.length() == 0)) {
            if (realJavaEncoding != null) {
              setEncoding(realJavaEncoding);
            } else {
              throw SQLError.createSQLException("Unknown initial character set index '" + this.io.serverCharsetIndex + "' received from server. Initial client character set can be forced via the 'characterEncoding' property.", "S1000", getExceptionInterceptor());
            }
          }
          if ((versionMeetsMinimum(4, 1, 0)) && ("ISO8859_1".equalsIgnoreCase(serverEncodingToSet))) {
            serverEncodingToSet = "Cp1252";
          }
        }
        catch (ArrayIndexOutOfBoundsException outOfBoundsEx)
        {
          String serverEncodingToSet;
          if (realJavaEncoding != null) {
            setEncoding(realJavaEncoding);
          } else {
            throw SQLError.createSQLException("Unknown initial character set index '" + this.io.serverCharsetIndex + "' received from server. Initial client character set can be forced via the 'characterEncoding' property.", "S1000", getExceptionInterceptor());
          }
        }
        if (getEncoding() == null) {
          setEncoding("ISO8859_1");
        }
        if (getUseUnicode()) {
          if (realJavaEncoding != null)
          {
            if ((realJavaEncoding.equalsIgnoreCase("UTF-8")) || (realJavaEncoding.equalsIgnoreCase("UTF8")))
            {
              if ((!getUseOldUTF8Behavior()) && (
                (dontCheckServerMatch) || (!characterSetNamesMatches("utf8")))) {
                execSQL(null, "SET NAMES utf8", -1, null, 1003, 1007, false, this.database, null, false);
              }
              setEncoding(realJavaEncoding);
            }
            else
            {
              String mysqlEncodingName = CharsetMapping.getMysqlEncodingForJavaEncoding(realJavaEncoding.toUpperCase(Locale.ENGLISH), this);
              if (mysqlEncodingName != null) {
                if ((dontCheckServerMatch) || (!characterSetNamesMatches(mysqlEncodingName))) {
                  execSQL(null, "SET NAMES " + mysqlEncodingName, -1, null, 1003, 1007, false, this.database, null, false);
                }
              }
              setEncoding(realJavaEncoding);
            }
          }
          else if (getEncoding() != null)
          {
            String mysqlEncodingName = CharsetMapping.getMysqlEncodingForJavaEncoding(getEncoding().toUpperCase(Locale.ENGLISH), this);
            if ((dontCheckServerMatch) || (!characterSetNamesMatches(mysqlEncodingName))) {
              execSQL(null, "SET NAMES " + mysqlEncodingName, -1, null, 1003, 1007, false, this.database, null, false);
            }
            realJavaEncoding = getEncoding();
          }
        }
        String onServer = null;
        boolean isNullOnServer = false;
        if (this.serverVariables != null)
        {
          onServer = (String)this.serverVariables.get("character_set_results");
          
          isNullOnServer = (onServer == null) || ("NULL".equalsIgnoreCase(onServer)) || (onServer.length() == 0);
        }
        if (getCharacterSetResults() == null)
        {
          if (!isNullOnServer)
          {
            execSQL(null, "SET character_set_results = NULL", -1, null, 1003, 1007, false, this.database, null, false);
            if (!this.usingCachedConfig) {
              this.serverVariables.put("jdbc.local.character_set_results", null);
            }
          }
          else if (!this.usingCachedConfig)
          {
            this.serverVariables.put("jdbc.local.character_set_results", onServer);
          }
        }
        else
        {
          String charsetResults = getCharacterSetResults();
          String mysqlEncodingName = null;
          if (("UTF-8".equalsIgnoreCase(charsetResults)) || ("UTF8".equalsIgnoreCase(charsetResults))) {
            mysqlEncodingName = "utf8";
          } else {
            mysqlEncodingName = CharsetMapping.getMysqlEncodingForJavaEncoding(charsetResults.toUpperCase(Locale.ENGLISH), this);
          }
          if (!mysqlEncodingName.equalsIgnoreCase((String)this.serverVariables.get("character_set_results")))
          {
            StringBuffer setBuf = new StringBuffer("SET character_set_results = ".length() + mysqlEncodingName.length());
            
            setBuf.append("SET character_set_results = ").append(mysqlEncodingName);
            
            execSQL(null, setBuf.toString(), -1, null, 1003, 1007, false, this.database, null, false);
            if (!this.usingCachedConfig) {
              this.serverVariables.put("jdbc.local.character_set_results", mysqlEncodingName);
            }
          }
          else if (!this.usingCachedConfig)
          {
            this.serverVariables.put("jdbc.local.character_set_results", onServer);
          }
        }
        if (getConnectionCollation() != null)
        {
          StringBuffer setBuf = new StringBuffer("SET collation_connection = ".length() + getConnectionCollation().length());
          
          setBuf.append("SET collation_connection = ").append(getConnectionCollation());
          
          execSQL(null, setBuf.toString(), -1, null, 1003, 1007, false, this.database, null, false);
        }
      }
      else
      {
        realJavaEncoding = getEncoding();
      }
    }
    finally
    {
      setEncoding(realJavaEncoding);
    }
    try
    {
      CharsetEncoder enc = Charset.forName(getEncoding()).newEncoder();
      CharBuffer cbuf = CharBuffer.allocate(1);
      ByteBuffer bbuf = ByteBuffer.allocate(1);
      
      cbuf.put("¥");
      cbuf.position(0);
      enc.encode(cbuf, bbuf, true);
      if (bbuf.get(0) == 92)
      {
        this.requiresEscapingEncoder = true;
      }
      else
      {
        cbuf.clear();
        bbuf.clear();
        
        cbuf.put("₩");
        cbuf.position(0);
        enc.encode(cbuf, bbuf, true);
        if (bbuf.get(0) == 92) {
          this.requiresEscapingEncoder = true;
        }
      }
    }
    catch (UnsupportedCharsetException ucex)
    {
      try
      {
        byte[] bbuf = new String("¥").getBytes(getEncoding());
        if (bbuf[0] == 92)
        {
          this.requiresEscapingEncoder = true;
        }
        else
        {
          bbuf = new String("₩").getBytes(getEncoding());
          if (bbuf[0] == 92) {
            this.requiresEscapingEncoder = true;
          }
        }
      }
      catch (UnsupportedEncodingException ueex)
      {
        throw SQLError.createSQLException("Unable to use encoding: " + getEncoding(), "S1000", ueex, getExceptionInterceptor());
      }
    }
    return characterSetAlreadyConfigured;
  }
  
  private void configureTimezone()
    throws SQLException
  {
    String configuredTimeZoneOnServer = (String)this.serverVariables.get("timezone");
    if (configuredTimeZoneOnServer == null)
    {
      configuredTimeZoneOnServer = (String)this.serverVariables.get("time_zone");
      if ("SYSTEM".equalsIgnoreCase(configuredTimeZoneOnServer)) {
        configuredTimeZoneOnServer = (String)this.serverVariables.get("system_time_zone");
      }
    }
    String canoncicalTimezone = getServerTimezone();
    if (((getUseTimezone()) || (!getUseLegacyDatetimeCode())) && (configuredTimeZoneOnServer != null))
    {
      if ((canoncicalTimezone == null) || (StringUtils.isEmptyOrWhitespaceOnly(canoncicalTimezone))) {
        try
        {
          canoncicalTimezone = TimeUtil.getCanoncialTimezone(configuredTimeZoneOnServer, getExceptionInterceptor());
          if (canoncicalTimezone == null) {
            throw SQLError.createSQLException("Can't map timezone '" + configuredTimeZoneOnServer + "' to " + " canonical timezone.", "S1009", getExceptionInterceptor());
          }
        }
        catch (IllegalArgumentException iae)
        {
          throw SQLError.createSQLException(iae.getMessage(), "S1000", getExceptionInterceptor());
        }
      }
    }
    else {
      canoncicalTimezone = getServerTimezone();
    }
    if ((canoncicalTimezone != null) && (canoncicalTimezone.length() > 0))
    {
      this.serverTimezoneTZ = TimeZone.getTimeZone(canoncicalTimezone);
      if ((!canoncicalTimezone.equalsIgnoreCase("GMT")) && (this.serverTimezoneTZ.getID().equals("GMT"))) {
        throw SQLError.createSQLException("No timezone mapping entry for '" + canoncicalTimezone + "'", "S1009", getExceptionInterceptor());
      }
      if ("GMT".equalsIgnoreCase(this.serverTimezoneTZ.getID())) {
        this.isServerTzUTC = true;
      } else {
        this.isServerTzUTC = false;
      }
    }
  }
  
  private void createInitialHistogram(long[] breakpoints, long lowerBound, long upperBound)
  {
    double bucketSize = (upperBound - lowerBound) / 20.0D * 1.25D;
    if (bucketSize < 1.0D) {
      bucketSize = 1.0D;
    }
    for (int i = 0; i < 20; i++)
    {
      breakpoints[i] = lowerBound;
      lowerBound = (lowerBound + bucketSize);
    }
  }
  
  protected void createNewIO(boolean isForReconnect)
    throws SQLException
  {
    synchronized (this.mutex)
    {
      Properties mergedProps = exposeAsProperties(this.props);
      
      long queriesIssuedFailedOverCopy = this.queriesIssuedFailedOver;
      this.queriesIssuedFailedOver = 0L;
      try
      {
        if ((!getHighAvailability()) && (!this.failedOver))
        {
          boolean connectionGood = false;
          Exception connectionNotEstablishedBecause = null;
          
          int hostIndex = 0;
          if (getRoundRobinLoadBalance()) {}
          for (hostIndex = getNextRoundRobinHostIndex(getURL(), this.hostList); hostIndex < this.hostListSize; hostIndex++)
          {
            if (hostIndex == 0) {
              this.hasTriedMasterFlag = true;
            }
            try
            {
              String newHostPortPair = (String)this.hostList.get(hostIndex);
              
              int newPort = 3306;
              
              String[] hostPortPair = NonRegisteringDriver.parseHostPortPair(newHostPortPair);
              
              String newHost = hostPortPair[0];
              if ((newHost == null) || (StringUtils.isEmptyOrWhitespaceOnly(newHost))) {
                newHost = "localhost";
              }
              if (hostPortPair[1] != null) {
                try
                {
                  newPort = Integer.parseInt(hostPortPair[1]);
                }
                catch (NumberFormatException nfe)
                {
                  throw SQLError.createSQLException("Illegal connection port value '" + hostPortPair[1] + "'", "01S00", getExceptionInterceptor());
                }
              }
              this.io = new MysqlIO(newHost, newPort, mergedProps, getSocketFactoryClassName(), this, getSocketTimeout(), this.largeRowSizeThreshold.getValueAsInt());
              
              this.io.doHandshake(this.user, this.password, this.database);
              
              this.connectionId = this.io.getThreadId();
              this.isClosed = false;
              
              boolean oldAutoCommit = getAutoCommit();
              int oldIsolationLevel = this.isolationLevel;
              boolean oldReadOnly = isReadOnly();
              String oldCatalog = getCatalog();
              
              initializePropsFromServer();
              if (isForReconnect)
              {
                setAutoCommit(oldAutoCommit);
                if (this.hasIsolationLevels) {
                  setTransactionIsolation(oldIsolationLevel);
                }
                setCatalog(oldCatalog);
              }
              if (hostIndex != 0)
              {
                setFailedOverState();
                queriesIssuedFailedOverCopy = 0L;
              }
              else
              {
                this.failedOver = false;
                queriesIssuedFailedOverCopy = 0L;
                if (this.hostListSize > 1) {
                  setReadOnlyInternal(false);
                } else {
                  setReadOnlyInternal(oldReadOnly);
                }
              }
              connectionGood = true;
            }
            catch (Exception EEE)
            {
              if (this.io != null) {
                this.io.forceClose();
              }
              connectionNotEstablishedBecause = EEE;
              
              connectionGood = false;
              if ((EEE instanceof SQLException))
              {
                SQLException sqlEx = (SQLException)EEE;
                
                String sqlState = sqlEx.getSQLState();
                if ((sqlState == null) || (!sqlState.equals("08S01"))) {
                  throw sqlEx;
                }
              }
              if (getRoundRobinLoadBalance()) {
                hostIndex = getNextRoundRobinHostIndex(getURL(), this.hostList) - 1;
              } else if (this.hostListSize - 1 == hostIndex) {
                throw SQLError.createCommunicationsException(this, this.io != null ? this.io.getLastPacketSentTimeMs() : 0L, this.io != null ? this.io.getLastPacketReceivedTimeMs() : 0L, EEE, getExceptionInterceptor());
              }
            }
          }
          if (!connectionGood)
          {
            SQLException chainedEx = SQLError.createSQLException(Messages.getString("Connection.UnableToConnect"), "08001", getExceptionInterceptor());
            
            chainedEx.initCause(connectionNotEstablishedBecause);
            
            throw chainedEx;
          }
        }
        else
        {
          double timeout = getInitialTimeout();
          boolean connectionGood = false;
          
          Exception connectionException = null;
          
          int hostIndex = 0;
          if (getRoundRobinLoadBalance()) {}
          for (hostIndex = getNextRoundRobinHostIndex(getURL(), this.hostList); (hostIndex < this.hostListSize) && (!connectionGood); hostIndex++)
          {
            if (hostIndex == 0) {
              this.hasTriedMasterFlag = true;
            }
            if ((this.preferSlaveDuringFailover) && (hostIndex == 0)) {
              hostIndex++;
            }
            for (int attemptCount = 0; (attemptCount < getMaxReconnects()) && (!connectionGood); attemptCount++)
            {
              try
              {
                if (this.io != null) {
                  this.io.forceClose();
                }
                String newHostPortPair = (String)this.hostList.get(hostIndex);
                
                int newPort = 3306;
                
                String[] hostPortPair = NonRegisteringDriver.parseHostPortPair(newHostPortPair);
                
                String newHost = hostPortPair[0];
                if ((newHost == null) || (StringUtils.isEmptyOrWhitespaceOnly(newHost))) {
                  newHost = "localhost";
                }
                if (hostPortPair[1] != null) {
                  try
                  {
                    newPort = Integer.parseInt(hostPortPair[1]);
                  }
                  catch (NumberFormatException nfe)
                  {
                    throw SQLError.createSQLException("Illegal connection port value '" + hostPortPair[1] + "'", "01S00", getExceptionInterceptor());
                  }
                }
                this.io = new MysqlIO(newHost, newPort, mergedProps, getSocketFactoryClassName(), this, getSocketTimeout(), this.largeRowSizeThreshold.getValueAsInt());
                
                this.io.doHandshake(this.user, this.password, this.database);
                
                pingInternal(false, 0);
                this.connectionId = this.io.getThreadId();
                this.isClosed = false;
                
                boolean oldAutoCommit = getAutoCommit();
                int oldIsolationLevel = this.isolationLevel;
                boolean oldReadOnly = isReadOnly();
                String oldCatalog = getCatalog();
                
                initializePropsFromServer();
                if (isForReconnect)
                {
                  setAutoCommit(oldAutoCommit);
                  if (this.hasIsolationLevels) {
                    setTransactionIsolation(oldIsolationLevel);
                  }
                  setCatalog(oldCatalog);
                }
                connectionGood = true;
                if (hostIndex != 0)
                {
                  setFailedOverState();
                  queriesIssuedFailedOverCopy = 0L;
                }
                else
                {
                  this.failedOver = false;
                  queriesIssuedFailedOverCopy = 0L;
                  if (this.hostListSize > 1) {
                    setReadOnlyInternal(false);
                  } else {
                    setReadOnlyInternal(oldReadOnly);
                  }
                }
              }
              catch (Exception EEE)
              {
                connectionException = EEE;
                connectionGood = false;
                if (getRoundRobinLoadBalance()) {
                  hostIndex = getNextRoundRobinHostIndex(getURL(), this.hostList) - 1;
                }
                if (!connectionGood) {
                  break label1003;
                }
              }
              break;
              label1003:
              if (attemptCount > 0) {
                try
                {
                  Thread.sleep(timeout * 1000L);
                }
                catch (InterruptedException IE) {}
              }
            }
          }
          if (!connectionGood)
          {
            SQLException chainedEx = SQLError.createSQLException(Messages.getString("Connection.UnableToConnectWithRetries", new Object[] { new Integer(getMaxReconnects()) }), "08001", getExceptionInterceptor());
            
            chainedEx.initCause(connectionException);
            
            throw chainedEx;
          }
        }
        if ((getParanoid()) && (!getHighAvailability()) && (this.hostListSize <= 1))
        {
          this.password = null;
          this.user = null;
        }
        if (isForReconnect)
        {
          Iterator statementIter = this.openStatements.values().iterator();
          
          Stack serverPreparedStatements = null;
          while (statementIter.hasNext())
          {
            Object statementObj = statementIter.next();
            if ((statementObj instanceof ServerPreparedStatement))
            {
              if (serverPreparedStatements == null) {
                serverPreparedStatements = new Stack();
              }
              serverPreparedStatements.add(statementObj);
            }
          }
          if (serverPreparedStatements != null) {
            while (!serverPreparedStatements.isEmpty()) {
              ((ServerPreparedStatement)serverPreparedStatements.pop()).rePrepare();
            }
          }
        }
      }
      finally
      {
        this.queriesIssuedFailedOver = queriesIssuedFailedOverCopy;
      }
    }
  }
  
  private void createPreparedStatementCaches()
  {
    int cacheSize = getPreparedStatementCacheSize();
    
    this.cachedPreparedStatementParams = new HashMap(cacheSize);
    if (getUseServerPreparedStmts())
    {
      this.serverSideStatementCheckCache = new LRUCache(cacheSize);
      
      this.serverSideStatementCache = new LRUCache(cacheSize)
      {
        protected boolean removeEldestEntry(Map.Entry eldest)
        {
          if (this.maxElements <= 1) {
            return false;
          }
          boolean removeIt = super.removeEldestEntry(eldest);
          if (removeIt)
          {
            ServerPreparedStatement ps = (ServerPreparedStatement)eldest.getValue();
            
            ps.isCached = false;
            ps.setClosed(false);
            try
            {
              ps.close();
            }
            catch (SQLException sqlEx) {}
          }
          return removeIt;
        }
      };
    }
  }
  
  public java.sql.Statement createStatement()
    throws SQLException
  {
    return createStatement(1003, 1007);
  }
  
  public java.sql.Statement createStatement(int resultSetType, int resultSetConcurrency)
    throws SQLException
  {
    checkClosed();
    
    StatementImpl stmt = new StatementImpl(this, this.database);
    stmt.setResultSetType(resultSetType);
    stmt.setResultSetConcurrency(resultSetConcurrency);
    
    return stmt;
  }
  
  public java.sql.Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability)
    throws SQLException
  {
    if ((getPedantic()) && 
      (resultSetHoldability != 1)) {
      throw SQLError.createSQLException("HOLD_CUSRORS_OVER_COMMIT is only supported holdability level", "S1009", getExceptionInterceptor());
    }
    return createStatement(resultSetType, resultSetConcurrency);
  }
  
  protected void dumpTestcaseQuery(String query)
  {
    System.err.println(query);
  }
  
  protected Connection duplicate()
    throws SQLException
  {
    return new ConnectionImpl(this.origHostToConnectTo, this.origPortToConnectTo, this.props, this.origDatabaseToConnectTo, this.myURL);
  }
  
  ResultSetInternalMethods execSQL(StatementImpl callingStatement, String sql, int maxRows, Buffer packet, int resultSetType, int resultSetConcurrency, boolean streamResults, String catalog, Field[] cachedMetadata)
    throws SQLException
  {
    return execSQL(callingStatement, sql, maxRows, packet, resultSetType, resultSetConcurrency, streamResults, catalog, cachedMetadata, false);
  }
  
  ResultSetInternalMethods execSQL(StatementImpl callingStatement, String sql, int maxRows, Buffer packet, int resultSetType, int resultSetConcurrency, boolean streamResults, String catalog, Field[] cachedMetadata, boolean isBatch)
    throws SQLException
  {
    synchronized (this.mutex)
    {
      long queryStartTime = 0L;
      
      int endOfQueryPacketPosition = 0;
      if (packet != null) {
        endOfQueryPacketPosition = packet.getPosition();
      }
      if (getGatherPerformanceMetrics()) {
        queryStartTime = System.currentTimeMillis();
      }
      this.lastQueryFinishedTime = 0L;
      if ((this.failedOver) && (this.autoCommit) && (!isBatch) && 
        (shouldFallBack()) && (!this.executingFailoverReconnect)) {
        try
        {
          this.executingFailoverReconnect = true;
          
          createNewIO(true);
          
          String connectedHost = this.io.getHost();
          if ((connectedHost != null) && (this.hostList.get(0).equals(connectedHost)))
          {
            this.failedOver = false;
            this.queriesIssuedFailedOver = 0L;
            setReadOnlyInternal(false);
          }
        }
        finally
        {
          this.executingFailoverReconnect = false;
        }
      }
      if (((getHighAvailability()) || (this.failedOver)) && ((this.autoCommit) || (getAutoReconnectForPools())) && (this.needsPing) && (!isBatch)) {
        try
        {
          pingInternal(false, 0);
          
          this.needsPing = false;
        }
        catch (Exception Ex)
        {
          createNewIO(true);
        }
      }
      try
      {
        if (packet == null)
        {
          encoding = null;
          if (getUseUnicode()) {
            encoding = getEncoding();
          }
          ResultSetInternalMethods localResultSetInternalMethods = this.io.sqlQueryDirect(callingStatement, sql, encoding, null, maxRows, resultSetType, resultSetConcurrency, streamResults, catalog, cachedMetadata);jsr 241;return localResultSetInternalMethods;
        }
        String encoding = this.io.sqlQueryDirect(callingStatement, null, null, packet, maxRows, resultSetType, resultSetConcurrency, streamResults, catalog, cachedMetadata);jsr 207;return encoding;
      }
      catch (SQLException sqlE)
      {
        if (getDumpQueriesOnException())
        {
          String extractedSql = extractSqlFromPacket(sql, packet, endOfQueryPacketPosition);
          
          StringBuffer messageBuf = new StringBuffer(extractedSql.length() + 32);
          
          messageBuf.append("\n\nQuery being executed when exception was thrown:\n\n");
          
          messageBuf.append(extractedSql);
          
          sqlE = appendMessageToException(sqlE, messageBuf.toString(), getExceptionInterceptor());
        }
        if ((getHighAvailability()) || (this.failedOver))
        {
          this.needsPing = true;
        }
        else
        {
          String sqlState = sqlE.getSQLState();
          if ((sqlState != null) && (sqlState.equals("08S01"))) {
            cleanup(sqlE);
          }
        }
        throw sqlE;
      }
      catch (Exception ex)
      {
        if ((getHighAvailability()) || (this.failedOver)) {
          this.needsPing = true;
        } else if ((ex instanceof IOException)) {
          cleanup(ex);
        }
        SQLException sqlEx = SQLError.createSQLException(Messages.getString("Connection.UnexpectedException"), "S1000", getExceptionInterceptor());
        
        sqlEx.initCause(ex);
        
        throw sqlEx;
      }
      finally
      {
        jsr 6;
      }
      localObject3 = returnAddress;
      if (getMaintainTimeStats()) {
        this.lastQueryFinishedTime = System.currentTimeMillis();
      }
      if (this.failedOver) {
        this.queriesIssuedFailedOver += 1L;
      }
      if (getGatherPerformanceMetrics())
      {
        long queryTime = System.currentTimeMillis() - queryStartTime;
        
        registerQueryExecutionTime(queryTime);
      }
      ret;
    }
  }
  
  protected String extractSqlFromPacket(String possibleSqlQuery, Buffer queryPacket, int endOfQueryPacketPosition)
    throws SQLException
  {
    String extractedSql = null;
    if (possibleSqlQuery != null) {
      if (possibleSqlQuery.length() > getMaxQuerySizeToLog())
      {
        StringBuffer truncatedQueryBuf = new StringBuffer(possibleSqlQuery.substring(0, getMaxQuerySizeToLog()));
        
        truncatedQueryBuf.append(Messages.getString("MysqlIO.25"));
        extractedSql = truncatedQueryBuf.toString();
      }
      else
      {
        extractedSql = possibleSqlQuery;
      }
    }
    if (extractedSql == null)
    {
      int extractPosition = endOfQueryPacketPosition;
      
      boolean truncated = false;
      if (endOfQueryPacketPosition > getMaxQuerySizeToLog())
      {
        extractPosition = getMaxQuerySizeToLog();
        truncated = true;
      }
      extractedSql = new String(queryPacket.getByteBuffer(), 5, extractPosition - 5);
      if (truncated) {
        extractedSql = extractedSql + Messages.getString("MysqlIO.25");
      }
    }
    return extractedSql;
  }
  
  protected void finalize()
    throws Throwable
  {
    cleanup(null);
    
    super.finalize();
  }
  
  protected StringBuffer generateConnectionCommentBlock(StringBuffer buf)
  {
    buf.append("/* conn id ");
    buf.append(getId());
    buf.append(" clock: ");
    buf.append(System.currentTimeMillis());
    buf.append(" */ ");
    
    return buf;
  }
  
  public boolean getAutoCommit()
    throws SQLException
  {
    return this.autoCommit;
  }
  
  protected Calendar getCalendarInstanceForSessionOrNew()
  {
    if (getDynamicCalendars()) {
      return Calendar.getInstance();
    }
    return getSessionLockedCalendar();
  }
  
  public String getCatalog()
    throws SQLException
  {
    return this.database;
  }
  
  protected String getCharacterSetMetadata()
  {
    return this.characterSetMetadata;
  }
  
  SingleByteCharsetConverter getCharsetConverter(String javaEncodingName)
    throws SQLException
  {
    if (javaEncodingName == null) {
      return null;
    }
    if (this.usePlatformCharsetConverters) {
      return null;
    }
    SingleByteCharsetConverter converter = null;
    synchronized (this.charsetConverterMap)
    {
      Object asObject = this.charsetConverterMap.get(javaEncodingName);
      if (asObject == CHARSET_CONVERTER_NOT_AVAILABLE_MARKER) {
        return null;
      }
      converter = (SingleByteCharsetConverter)asObject;
      if (converter == null) {
        try
        {
          converter = SingleByteCharsetConverter.getInstance(javaEncodingName, this);
          if (converter == null) {
            this.charsetConverterMap.put(javaEncodingName, CHARSET_CONVERTER_NOT_AVAILABLE_MARKER);
          } else {
            this.charsetConverterMap.put(javaEncodingName, converter);
          }
        }
        catch (UnsupportedEncodingException unsupEncEx)
        {
          this.charsetConverterMap.put(javaEncodingName, CHARSET_CONVERTER_NOT_AVAILABLE_MARKER);
          
          converter = null;
        }
      }
    }
    return converter;
  }
  
  protected String getCharsetNameForIndex(int charsetIndex)
    throws SQLException
  {
    String charsetName = null;
    if (getUseOldUTF8Behavior()) {
      return getEncoding();
    }
    if (charsetIndex != -1)
    {
      try
      {
        charsetName = this.indexToCharsetMapping[charsetIndex];
        if (("sjis".equalsIgnoreCase(charsetName)) || ("MS932".equalsIgnoreCase(charsetName))) {
          if (CharsetMapping.isAliasForSjis(getEncoding())) {
            charsetName = getEncoding();
          }
        }
      }
      catch (ArrayIndexOutOfBoundsException outOfBoundsEx)
      {
        throw SQLError.createSQLException("Unknown character set index for field '" + charsetIndex + "' received from server.", "S1000", getExceptionInterceptor());
      }
      if (charsetName == null) {
        charsetName = getEncoding();
      }
    }
    else
    {
      charsetName = getEncoding();
    }
    return charsetName;
  }
  
  protected TimeZone getDefaultTimeZone()
  {
    return this.defaultTimeZone;
  }
  
  protected String getErrorMessageEncoding()
  {
    return this.errorMessageEncoding;
  }
  
  public int getHoldability()
    throws SQLException
  {
    return 2;
  }
  
  long getId()
  {
    return this.connectionId;
  }
  
  public long getIdleFor()
  {
    if (this.lastQueryFinishedTime == 0L) {
      return 0L;
    }
    long now = System.currentTimeMillis();
    long idleTime = now - this.lastQueryFinishedTime;
    
    return idleTime;
  }
  
  protected MysqlIO getIO()
    throws SQLException
  {
    if ((this.io == null) || (this.isClosed)) {
      throw SQLError.createSQLException("Operation not allowed on closed connection", "08003", getExceptionInterceptor());
    }
    return this.io;
  }
  
  public Log getLog()
    throws SQLException
  {
    return this.log;
  }
  
  protected int getMaxBytesPerChar(String javaCharsetName)
    throws SQLException
  {
    String charset = CharsetMapping.getMysqlEncodingForJavaEncoding(javaCharsetName, this);
    if (versionMeetsMinimum(4, 1, 0))
    {
      Map mapToCheck = null;
      if (!getUseDynamicCharsetInfo())
      {
        mapToCheck = CharsetMapping.STATIC_CHARSET_TO_NUM_BYTES_MAP;
      }
      else
      {
        mapToCheck = this.charsetToNumBytesMap;
        synchronized (this.charsetToNumBytesMap)
        {
          if (this.charsetToNumBytesMap.isEmpty())
          {
            java.sql.Statement stmt = null;
            ResultSet rs = null;
            try
            {
              stmt = getMetadataSafeStatement();
              
              rs = stmt.executeQuery("SHOW CHARACTER SET");
              while (rs.next()) {
                this.charsetToNumBytesMap.put(rs.getString("Charset"), Constants.integerValueOf(rs.getInt("Maxlen")));
              }
              rs.close();
              rs = null;
              
              stmt.close();
              
              stmt = null;
            }
            finally
            {
              if (rs != null)
              {
                rs.close();
                rs = null;
              }
              if (stmt != null)
              {
                stmt.close();
                stmt = null;
              }
            }
          }
        }
      }
      Integer mbPerChar = (Integer)mapToCheck.get(charset);
      if (mbPerChar != null) {
        return mbPerChar.intValue();
      }
      return 1;
    }
    return 1;
  }
  
  public java.sql.DatabaseMetaData getMetaData()
    throws SQLException
  {
    return getMetaData(true, true);
  }
  
  private java.sql.DatabaseMetaData getMetaData(boolean checkClosed, boolean checkForInfoSchema)
    throws SQLException
  {
    if (checkClosed) {
      checkClosed();
    }
    return DatabaseMetaData.getInstance(this, this.database, checkForInfoSchema);
  }
  
  protected java.sql.Statement getMetadataSafeStatement()
    throws SQLException
  {
    java.sql.Statement stmt = createStatement();
    if (stmt.getMaxRows() != 0) {
      stmt.setMaxRows(0);
    }
    stmt.setEscapeProcessing(false);
    if (stmt.getFetchSize() != 0) {
      stmt.setFetchSize(0);
    }
    return stmt;
  }
  
  Object getMutex()
    throws SQLException
  {
    if (this.io == null) {
      throwConnectionClosedException();
    }
    reportMetricsIfNeeded();
    
    return this.mutex;
  }
  
  int getNetBufferLength()
  {
    return this.netBufferLength;
  }
  
  public String getServerCharacterEncoding()
  {
    if (this.io.versionMeetsMinimum(4, 1, 0)) {
      return (String)this.serverVariables.get("character_set_server");
    }
    return (String)this.serverVariables.get("character_set");
  }
  
  int getServerMajorVersion()
  {
    return this.io.getServerMajorVersion();
  }
  
  int getServerMinorVersion()
  {
    return this.io.getServerMinorVersion();
  }
  
  int getServerSubMinorVersion()
  {
    return this.io.getServerSubMinorVersion();
  }
  
  public TimeZone getServerTimezoneTZ()
  {
    return this.serverTimezoneTZ;
  }
  
  String getServerVariable(String variableName)
  {
    if (this.serverVariables != null) {
      return (String)this.serverVariables.get(variableName);
    }
    return null;
  }
  
  String getServerVersion()
  {
    return this.io.getServerVersion();
  }
  
  protected Calendar getSessionLockedCalendar()
  {
    return this.sessionCalendar;
  }
  
  public int getTransactionIsolation()
    throws SQLException
  {
    if ((this.hasIsolationLevels) && (!getUseLocalSessionState()))
    {
      java.sql.Statement stmt = null;
      ResultSet rs = null;
      try
      {
        stmt = getMetadataSafeStatement();
        
        String query = null;
        
        int offset = 0;
        if (versionMeetsMinimum(4, 0, 3))
        {
          query = "SELECT @@session.tx_isolation";
          offset = 1;
        }
        else
        {
          query = "SHOW VARIABLES LIKE 'transaction_isolation'";
          offset = 2;
        }
        rs = stmt.executeQuery(query);
        if (rs.next())
        {
          String s = rs.getString(offset);
          if (s != null)
          {
            Integer intTI = (Integer)mapTransIsolationNameToValue.get(s);
            if (intTI != null) {
              return intTI.intValue();
            }
          }
          throw SQLError.createSQLException("Could not map transaction isolation '" + s + " to a valid JDBC level.", "S1000", getExceptionInterceptor());
        }
        throw SQLError.createSQLException("Could not retrieve transaction isolation level from server", "S1000", getExceptionInterceptor());
      }
      finally
      {
        if (rs != null)
        {
          try
          {
            rs.close();
          }
          catch (Exception ex) {}
          rs = null;
        }
        if (stmt != null)
        {
          try
          {
            stmt.close();
          }
          catch (Exception ex) {}
          stmt = null;
        }
      }
    }
    return this.isolationLevel;
  }
  
  public synchronized Map getTypeMap()
    throws SQLException
  {
    if (this.typeMap == null) {
      this.typeMap = new HashMap();
    }
    return this.typeMap;
  }
  
  String getURL()
  {
    return this.myURL;
  }
  
  String getUser()
  {
    return this.user;
  }
  
  protected Calendar getUtcCalendar()
  {
    return this.utcCalendar;
  }
  
  public SQLWarning getWarnings()
    throws SQLException
  {
    return null;
  }
  
  public boolean hasSameProperties(Connection c)
  {
    return this.props.equals(((ConnectionImpl)c).props);
  }
  
  public boolean hasTriedMaster()
  {
    return this.hasTriedMasterFlag;
  }
  
  protected void incrementNumberOfPreparedExecutes()
  {
    if (getGatherPerformanceMetrics())
    {
      this.numberOfPreparedExecutes += 1L;
      
      this.numberOfQueriesIssued += 1L;
    }
  }
  
  protected void incrementNumberOfPrepares()
  {
    if (getGatherPerformanceMetrics()) {
      this.numberOfPrepares += 1L;
    }
  }
  
  protected void incrementNumberOfResultSetsCreated()
  {
    if (getGatherPerformanceMetrics()) {
      this.numberOfResultSetsCreated += 1L;
    }
  }
  
  private void initializeDriverProperties(Properties info)
    throws SQLException
  {
    initializeProperties(info);
    
    String exceptionInterceptorClasses = getExceptionInterceptors();
    if ((exceptionInterceptorClasses != null) && (!"".equals(exceptionInterceptorClasses)))
    {
      this.exceptionInterceptor = new ExceptionInterceptorChain(exceptionInterceptorClasses);
      this.exceptionInterceptor.init(this, info);
    }
    this.usePlatformCharsetConverters = getUseJvmCharsetConverters();
    
    this.log = LogFactory.getLogger(getLogger(), "MySQL", getExceptionInterceptor());
    if ((getProfileSql()) || (getUseUsageAdvisor())) {
      this.eventSink = ProfilerEventHandlerFactory.getInstance(this);
    }
    if (getCachePreparedStatements()) {
      createPreparedStatementCaches();
    }
    if ((getNoDatetimeStringSync()) && (getUseTimezone())) {
      throw SQLError.createSQLException("Can't enable noDatetimeSync and useTimezone configuration properties at the same time", "01S00", getExceptionInterceptor());
    }
    if (getCacheCallableStatements()) {
      this.parsedCallableStatementCache = new LRUCache(getCallableStatementCacheSize());
    }
    if (getAllowMultiQueries()) {
      setCacheResultSetMetadata(false);
    }
    if (getCacheResultSetMetadata()) {
      this.resultSetMetadataCache = new LRUCache(getMetadataCacheSize());
    }
  }
  
  private void initializePropsFromServer()
    throws SQLException
  {
    String connectionInterceptorClasses = getConnectionLifecycleInterceptors();
    
    this.connectionLifecycleInterceptors = null;
    if (connectionInterceptorClasses != null)
    {
      this.connectionLifecycleInterceptors = Util.loadExtensions(this, this.props, connectionInterceptorClasses, "Connection.badLifecycleInterceptor", getExceptionInterceptor());
      
      Iterator iter = this.connectionLifecycleInterceptors.iterator();
      
      new IterateBlock(iter)
      {
        void forEach(Object each)
          throws SQLException
        {
          ((ConnectionLifecycleInterceptor)each).init(ConnectionImpl.this, ConnectionImpl.this.props);
        }
      }.doForAll();
    }
    setSessionVariables();
    if (!versionMeetsMinimum(4, 1, 0)) {
      setTransformedBitIsBoolean(false);
    }
    this.parserKnowsUnicode = versionMeetsMinimum(4, 1, 0);
    if ((getUseServerPreparedStmts()) && (versionMeetsMinimum(4, 1, 0)))
    {
      this.useServerPreparedStmts = true;
      if ((versionMeetsMinimum(5, 0, 0)) && (!versionMeetsMinimum(5, 0, 3))) {
        this.useServerPreparedStmts = false;
      }
    }
    this.serverVariables.clear();
    if (versionMeetsMinimum(3, 21, 22))
    {
      loadServerVariables();
      if (versionMeetsMinimum(5, 0, 2)) {
        this.autoIncrementIncrement = getServerVariableAsInt("auto_increment_increment", 1);
      } else {
        this.autoIncrementIncrement = 1;
      }
      buildCollationMapping();
      
      LicenseConfiguration.checkLicenseType(this.serverVariables);
      
      String lowerCaseTables = (String)this.serverVariables.get("lower_case_table_names");
      
      this.lowerCaseTableNames = (("on".equalsIgnoreCase(lowerCaseTables)) || ("1".equalsIgnoreCase(lowerCaseTables)) || ("2".equalsIgnoreCase(lowerCaseTables)));
      
      this.storesLowerCaseTableName = (("1".equalsIgnoreCase(lowerCaseTables)) || ("on".equalsIgnoreCase(lowerCaseTables)));
      
      configureTimezone();
      if (this.serverVariables.containsKey("max_allowed_packet"))
      {
        int serverMaxAllowedPacket = getServerVariableAsInt("max_allowed_packet", -1);
        if ((serverMaxAllowedPacket != -1) && ((serverMaxAllowedPacket < getMaxAllowedPacket()) || (getMaxAllowedPacket() <= 0))) {
          setMaxAllowedPacket(serverMaxAllowedPacket);
        } else if ((serverMaxAllowedPacket == -1) && (getMaxAllowedPacket() == -1)) {
          setMaxAllowedPacket(65535);
        }
        int preferredBlobSendChunkSize = getBlobSendChunkSize();
        
        int allowedBlobSendChunkSize = Math.min(preferredBlobSendChunkSize, getMaxAllowedPacket()) - 8192 - 11;
        
        setBlobSendChunkSize(String.valueOf(allowedBlobSendChunkSize));
      }
      if (this.serverVariables.containsKey("net_buffer_length")) {
        this.netBufferLength = getServerVariableAsInt("net_buffer_length", 16384);
      }
      checkTransactionIsolationLevel();
      if (!versionMeetsMinimum(4, 1, 0)) {
        checkServerEncoding();
      }
      this.io.checkForCharsetMismatch();
      if (this.serverVariables.containsKey("sql_mode"))
      {
        int sqlMode = 0;
        
        String sqlModeAsString = (String)this.serverVariables.get("sql_mode");
        try
        {
          sqlMode = Integer.parseInt(sqlModeAsString);
        }
        catch (NumberFormatException nfe)
        {
          sqlMode = 0;
          if (sqlModeAsString != null)
          {
            if (sqlModeAsString.indexOf("ANSI_QUOTES") != -1) {
              sqlMode |= 0x4;
            }
            if (sqlModeAsString.indexOf("NO_BACKSLASH_ESCAPES") != -1) {
              this.noBackslashEscapes = true;
            }
          }
        }
        if ((sqlMode & 0x4) > 0) {
          this.useAnsiQuotes = true;
        } else {
          this.useAnsiQuotes = false;
        }
      }
    }
    this.errorMessageEncoding = CharsetMapping.getCharacterEncodingForErrorMessages(this);
    
    boolean overrideDefaultAutocommit = isAutoCommitNonDefaultOnServer();
    
    configureClientCharacterSet(false);
    if (versionMeetsMinimum(3, 23, 15))
    {
      this.transactionsSupported = true;
      if (!overrideDefaultAutocommit) {
        setAutoCommit(true);
      }
    }
    else
    {
      this.transactionsSupported = false;
    }
    if (versionMeetsMinimum(3, 23, 36)) {
      this.hasIsolationLevels = true;
    } else {
      this.hasIsolationLevels = false;
    }
    this.hasQuotedIdentifiers = versionMeetsMinimum(3, 23, 6);
    
    this.io.resetMaxBuf();
    if (this.io.versionMeetsMinimum(4, 1, 0))
    {
      String characterSetResultsOnServerMysql = (String)this.serverVariables.get("jdbc.local.character_set_results");
      if ((characterSetResultsOnServerMysql == null) || (StringUtils.startsWithIgnoreCaseAndWs(characterSetResultsOnServerMysql, "NULL")) || (characterSetResultsOnServerMysql.length() == 0))
      {
        String defaultMetadataCharsetMysql = (String)this.serverVariables.get("character_set_system");
        
        String defaultMetadataCharset = null;
        if (defaultMetadataCharsetMysql != null) {
          defaultMetadataCharset = CharsetMapping.getJavaEncodingForMysqlEncoding(defaultMetadataCharsetMysql, this);
        } else {
          defaultMetadataCharset = "UTF-8";
        }
        this.characterSetMetadata = defaultMetadataCharset;
      }
      else
      {
        this.characterSetResultsOnServer = CharsetMapping.getJavaEncodingForMysqlEncoding(characterSetResultsOnServerMysql, this);
        
        this.characterSetMetadata = this.characterSetResultsOnServer;
      }
    }
    else
    {
      this.characterSetMetadata = getEncoding();
    }
    if ((versionMeetsMinimum(4, 1, 0)) && (!versionMeetsMinimum(4, 1, 10)) && (getAllowMultiQueries())) {
      if (isQueryCacheEnabled()) {
        setAllowMultiQueries(false);
      }
    }
    if ((versionMeetsMinimum(5, 0, 0)) && ((getUseLocalTransactionState()) || (getElideSetAutoCommits())) && (isQueryCacheEnabled()) && (!versionMeetsMinimum(6, 0, 10)))
    {
      setUseLocalTransactionState(false);
      setElideSetAutoCommits(false);
    }
    setupServerForTruncationChecks();
  }
  
  private boolean isQueryCacheEnabled()
  {
    return ("ON".equalsIgnoreCase((String)this.serverVariables.get("query_cache_type"))) && (!"0".equalsIgnoreCase((String)this.serverVariables.get("query_cache_size")));
  }
  
  private int getServerVariableAsInt(String variableName, int fallbackValue)
    throws SQLException
  {
    try
    {
      return Integer.parseInt((String)this.serverVariables.get(variableName));
    }
    catch (NumberFormatException nfe)
    {
      getLog().logWarn(Messages.getString("Connection.BadValueInServerVariables", new Object[] { variableName, this.serverVariables.get(variableName), new Integer(fallbackValue) }));
    }
    return fallbackValue;
  }
  
  private boolean isAutoCommitNonDefaultOnServer()
    throws SQLException
  {
    boolean overrideDefaultAutocommit = false;
    
    String initConnectValue = (String)this.serverVariables.get("init_connect");
    if ((versionMeetsMinimum(4, 1, 2)) && (initConnectValue != null) && (initConnectValue.length() > 0)) {
      if (!getElideSetAutoCommits())
      {
        ResultSet rs = null;
        java.sql.Statement stmt = null;
        try
        {
          stmt = getMetadataSafeStatement();
          
          rs = stmt.executeQuery("SELECT @@session.autocommit");
          if (rs.next())
          {
            this.autoCommit = rs.getBoolean(1);
            if (this.autoCommit != true) {
              overrideDefaultAutocommit = true;
            }
          }
        }
        finally
        {
          if (rs != null) {
            try
            {
              rs.close();
            }
            catch (SQLException sqlEx) {}
          }
          if (stmt != null) {
            try
            {
              stmt.close();
            }
            catch (SQLException sqlEx) {}
          }
        }
      }
      else if (getIO().isSetNeededForAutoCommitMode(true))
      {
        this.autoCommit = false;
        overrideDefaultAutocommit = true;
      }
    }
    return overrideDefaultAutocommit;
  }
  
  protected boolean isClientTzUTC()
  {
    return this.isClientTzUTC;
  }
  
  public boolean isClosed()
  {
    return this.isClosed;
  }
  
  protected boolean isCursorFetchEnabled()
    throws SQLException
  {
    return (versionMeetsMinimum(5, 0, 2)) && (getUseCursorFetch());
  }
  
  public boolean isInGlobalTx()
  {
    return this.isInGlobalTx;
  }
  
  public synchronized boolean isMasterConnection()
  {
    return !this.failedOver;
  }
  
  public boolean isNoBackslashEscapesSet()
  {
    return this.noBackslashEscapes;
  }
  
  boolean isReadInfoMsgEnabled()
  {
    return this.readInfoMsg;
  }
  
  public boolean isReadOnly()
    throws SQLException
  {
    return this.readOnly;
  }
  
  protected boolean isRunningOnJDK13()
  {
    return this.isRunningOnJDK13;
  }
  
  public synchronized boolean isSameResource(Connection otherConnection)
  {
    if (otherConnection == null) {
      return false;
    }
    boolean directCompare = true;
    
    String otherHost = ((ConnectionImpl)otherConnection).origHostToConnectTo;
    String otherOrigDatabase = ((ConnectionImpl)otherConnection).origDatabaseToConnectTo;
    String otherCurrentCatalog = ((ConnectionImpl)otherConnection).database;
    if (!nullSafeCompare(otherHost, this.origHostToConnectTo)) {
      directCompare = false;
    } else if ((otherHost != null) && (otherHost.indexOf(',') == -1) && (otherHost.indexOf(':') == -1)) {
      directCompare = ((ConnectionImpl)otherConnection).origPortToConnectTo == this.origPortToConnectTo;
    }
    if (directCompare) {
      if (!nullSafeCompare(otherOrigDatabase, this.origDatabaseToConnectTo))
      {
        directCompare = false;
        directCompare = false;
      }
      else if (!nullSafeCompare(otherCurrentCatalog, this.database))
      {
        directCompare = false;
      }
    }
    if (directCompare) {
      return true;
    }
    String otherResourceId = ((ConnectionImpl)otherConnection).getResourceId();
    String myResourceId = getResourceId();
    if ((otherResourceId != null) || (myResourceId != null))
    {
      directCompare = nullSafeCompare(otherResourceId, myResourceId);
      if (directCompare) {
        return true;
      }
    }
    return false;
  }
  
  protected boolean isServerTzUTC()
  {
    return this.isServerTzUTC;
  }
  
  private boolean usingCachedConfig = false;
  
  private void loadServerVariables()
    throws SQLException
  {
    if (getCacheServerConfiguration()) {
      synchronized (serverConfigByUrl)
      {
        Map cachedVariableMap = (Map)serverConfigByUrl.get(getURL());
        if (cachedVariableMap != null)
        {
          this.serverVariables = cachedVariableMap;
          this.usingCachedConfig = true;
          
          return;
        }
      }
    }
    java.sql.Statement stmt = null;
    ResultSet results = null;
    try
    {
      stmt = getMetadataSafeStatement();
      
      String version = this.dbmd.getDriverVersion();
      if ((version != null) && (version.indexOf('*') != -1))
      {
        StringBuffer buf = new StringBuffer(version.length() + 10);
        for (int i = 0; i < version.length(); i++)
        {
          char c = version.charAt(i);
          if (c == '*') {
            buf.append("[star]");
          } else {
            buf.append(c);
          }
        }
        version = buf.toString();
      }
      String versionComment = "/* " + version + " */";
      
      String query = versionComment + "SHOW VARIABLES";
      if (versionMeetsMinimum(5, 0, 3)) {
        query = versionComment + "SHOW VARIABLES WHERE Variable_name ='language'" + " OR Variable_name = 'net_write_timeout'" + " OR Variable_name = 'interactive_timeout'" + " OR Variable_name = 'wait_timeout'" + " OR Variable_name = 'character_set_client'" + " OR Variable_name = 'character_set_connection'" + " OR Variable_name = 'character_set'" + " OR Variable_name = 'character_set_server'" + " OR Variable_name = 'tx_isolation'" + " OR Variable_name = 'transaction_isolation'" + " OR Variable_name = 'character_set_results'" + " OR Variable_name = 'timezone'" + " OR Variable_name = 'time_zone'" + " OR Variable_name = 'system_time_zone'" + " OR Variable_name = 'lower_case_table_names'" + " OR Variable_name = 'max_allowed_packet'" + " OR Variable_name = 'net_buffer_length'" + " OR Variable_name = 'sql_mode'" + " OR Variable_name = 'query_cache_type'" + " OR Variable_name = 'query_cache_size'" + " OR Variable_name = 'init_connect'";
      }
      results = stmt.executeQuery(query);
      while (results.next()) {
        this.serverVariables.put(results.getString(1), results.getString(2));
      }
      if (versionMeetsMinimum(5, 0, 2))
      {
        results = stmt.executeQuery(versionComment + "SELECT @@session.auto_increment_increment");
        if (results.next()) {
          this.serverVariables.put("auto_increment_increment", results.getString(1));
        }
      }
      if (getCacheServerConfiguration()) {
        synchronized (serverConfigByUrl)
        {
          serverConfigByUrl.put(getURL(), this.serverVariables);
        }
      }
    }
    catch (SQLException e)
    {
      throw e;
    }
    finally
    {
      if (results != null) {
        try
        {
          results.close();
        }
        catch (SQLException sqlE) {}
      }
      if (stmt != null) {
        try
        {
          stmt.close();
        }
        catch (SQLException sqlE) {}
      }
    }
  }
  
  private int autoIncrementIncrement = 0;
  private ExceptionInterceptor exceptionInterceptor;
  
  public int getAutoIncrementIncrement()
  {
    return this.autoIncrementIncrement;
  }
  
  public boolean lowerCaseTableNames()
  {
    return this.lowerCaseTableNames;
  }
  
  void maxRowsChanged(Statement stmt)
  {
    synchronized (this.mutex)
    {
      if (this.statementsUsingMaxRows == null) {
        this.statementsUsingMaxRows = new HashMap();
      }
      this.statementsUsingMaxRows.put(stmt, stmt);
      
      this.maxRowsChanged = true;
    }
  }
  
  public String nativeSQL(String sql)
    throws SQLException
  {
    if (sql == null) {
      return null;
    }
    Object escapedSqlResult = EscapeProcessor.escapeSQL(sql, serverSupportsConvertFn(), this);
    if ((escapedSqlResult instanceof String)) {
      return (String)escapedSqlResult;
    }
    return ((EscapeProcessorResult)escapedSqlResult).escapedSql;
  }
  
  private CallableStatement parseCallableStatement(String sql)
    throws SQLException
  {
    Object escapedSqlResult = EscapeProcessor.escapeSQL(sql, serverSupportsConvertFn(), this);
    
    boolean isFunctionCall = false;
    String parsedSql = null;
    if ((escapedSqlResult instanceof EscapeProcessorResult))
    {
      parsedSql = ((EscapeProcessorResult)escapedSqlResult).escapedSql;
      isFunctionCall = ((EscapeProcessorResult)escapedSqlResult).callingStoredFunction;
    }
    else
    {
      parsedSql = (String)escapedSqlResult;
      isFunctionCall = false;
    }
    return CallableStatement.getInstance(this, parsedSql, this.database, isFunctionCall);
  }
  
  public boolean parserKnowsUnicode()
  {
    return this.parserKnowsUnicode;
  }
  
  public void ping()
    throws SQLException
  {
    pingInternal(true, 0);
  }
  
  protected void pingInternal(boolean checkForClosedConnection, int timeoutMillis)
    throws SQLException
  {
    if (checkForClosedConnection) {
      checkClosed();
    }
    long pingMillisLifetime = getSelfDestructOnPingSecondsLifetime();
    int pingMaxOperations = getSelfDestructOnPingMaxOperations();
    if (((pingMillisLifetime > 0L) && (System.currentTimeMillis() - this.connectionCreationTimeMillis > pingMillisLifetime)) || ((pingMaxOperations > 0) && (pingMaxOperations <= this.io.getCommandCount())))
    {
      close();
      
      throw SQLError.createSQLException(Messages.getString("Connection.exceededConnectionLifetime"), "08S01", getExceptionInterceptor());
    }
    this.io.sendCommand(14, null, null, false, null, timeoutMillis);
  }
  
  public java.sql.CallableStatement prepareCall(String sql)
    throws SQLException
  {
    return prepareCall(sql, 1003, 1007);
  }
  
  public java.sql.CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency)
    throws SQLException
  {
    if (versionMeetsMinimum(5, 0, 0))
    {
      CallableStatement cStmt = null;
      if (!getCacheCallableStatements()) {
        cStmt = parseCallableStatement(sql);
      } else {
        synchronized (this.parsedCallableStatementCache)
        {
          CompoundCacheKey key = new CompoundCacheKey(getCatalog(), sql);
          
          CallableStatement.CallableStatementParamInfo cachedParamInfo = (CallableStatement.CallableStatementParamInfo)this.parsedCallableStatementCache.get(key);
          if (cachedParamInfo != null)
          {
            cStmt = CallableStatement.getInstance(this, cachedParamInfo);
          }
          else
          {
            cStmt = parseCallableStatement(sql);
            
            cachedParamInfo = cStmt.paramInfo;
            
            this.parsedCallableStatementCache.put(key, cachedParamInfo);
          }
        }
      }
      cStmt.setResultSetType(resultSetType);
      cStmt.setResultSetConcurrency(resultSetConcurrency);
      
      return cStmt;
    }
    throw SQLError.createSQLException("Callable statements not supported.", "S1C00", getExceptionInterceptor());
  }
  
  public java.sql.CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability)
    throws SQLException
  {
    if ((getPedantic()) && 
      (resultSetHoldability != 1)) {
      throw SQLError.createSQLException("HOLD_CUSRORS_OVER_COMMIT is only supported holdability level", "S1009", getExceptionInterceptor());
    }
    CallableStatement cStmt = (CallableStatement)prepareCall(sql, resultSetType, resultSetConcurrency);
    
    return cStmt;
  }
  
  public java.sql.PreparedStatement prepareStatement(String sql)
    throws SQLException
  {
    return prepareStatement(sql, 1003, 1007);
  }
  
  public java.sql.PreparedStatement prepareStatement(String sql, int autoGenKeyIndex)
    throws SQLException
  {
    java.sql.PreparedStatement pStmt = prepareStatement(sql);
    
    ((PreparedStatement)pStmt).setRetrieveGeneratedKeys(autoGenKeyIndex == 1);
    
    return pStmt;
  }
  
  public java.sql.PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency)
    throws SQLException
  {
    checkClosed();
    
    PreparedStatement pStmt = null;
    
    boolean canServerPrepare = true;
    
    String nativeSql = getProcessEscapeCodesForPrepStmts() ? nativeSQL(sql) : sql;
    if ((this.useServerPreparedStmts) && (getEmulateUnsupportedPstmts())) {
      canServerPrepare = canHandleAsServerPreparedStatement(nativeSql);
    }
    if ((this.useServerPreparedStmts) && (canServerPrepare))
    {
      if (getCachePreparedStatements()) {
        synchronized (this.serverSideStatementCache)
        {
          pStmt = (ServerPreparedStatement)this.serverSideStatementCache.remove(sql);
          if (pStmt != null)
          {
            ((ServerPreparedStatement)pStmt).setClosed(false);
            pStmt.clearParameters();
          }
          if (pStmt == null) {
            try
            {
              pStmt = ServerPreparedStatement.getInstance(this, nativeSql, this.database, resultSetType, resultSetConcurrency);
              if (sql.length() < getPreparedStatementCacheSqlLimit()) {
                ((ServerPreparedStatement)pStmt).isCached = true;
              }
              pStmt.setResultSetType(resultSetType);
              pStmt.setResultSetConcurrency(resultSetConcurrency);
            }
            catch (SQLException sqlEx)
            {
              if (getEmulateUnsupportedPstmts())
              {
                pStmt = (PreparedStatement)clientPrepareStatement(nativeSql, resultSetType, resultSetConcurrency, false);
                if (sql.length() < getPreparedStatementCacheSqlLimit()) {
                  this.serverSideStatementCheckCache.put(sql, Boolean.FALSE);
                }
              }
              else
              {
                throw sqlEx;
              }
            }
          }
        }
      }
      try
      {
        pStmt = ServerPreparedStatement.getInstance(this, nativeSql, this.database, resultSetType, resultSetConcurrency);
        
        pStmt.setResultSetType(resultSetType);
        pStmt.setResultSetConcurrency(resultSetConcurrency);
      }
      catch (SQLException sqlEx)
      {
        if (getEmulateUnsupportedPstmts()) {
          pStmt = (PreparedStatement)clientPrepareStatement(nativeSql, resultSetType, resultSetConcurrency, false);
        } else {
          throw sqlEx;
        }
      }
    }
    else
    {
      pStmt = (PreparedStatement)clientPrepareStatement(nativeSql, resultSetType, resultSetConcurrency, false);
    }
    return pStmt;
  }
  
  public java.sql.PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability)
    throws SQLException
  {
    if ((getPedantic()) && 
      (resultSetHoldability != 1)) {
      throw SQLError.createSQLException("HOLD_CUSRORS_OVER_COMMIT is only supported holdability level", "S1009", getExceptionInterceptor());
    }
    return prepareStatement(sql, resultSetType, resultSetConcurrency);
  }
  
  public java.sql.PreparedStatement prepareStatement(String sql, int[] autoGenKeyIndexes)
    throws SQLException
  {
    java.sql.PreparedStatement pStmt = prepareStatement(sql);
    
    ((PreparedStatement)pStmt).setRetrieveGeneratedKeys((autoGenKeyIndexes != null) && (autoGenKeyIndexes.length > 0));
    
    return pStmt;
  }
  
  public java.sql.PreparedStatement prepareStatement(String sql, String[] autoGenKeyColNames)
    throws SQLException
  {
    java.sql.PreparedStatement pStmt = prepareStatement(sql);
    
    ((PreparedStatement)pStmt).setRetrieveGeneratedKeys((autoGenKeyColNames != null) && (autoGenKeyColNames.length > 0));
    
    return pStmt;
  }
  
  protected void realClose(boolean calledExplicitly, boolean issueRollback, boolean skipLocalTeardown, Throwable reason)
    throws SQLException
  {
    SQLException sqlEx = null;
    if (isClosed()) {
      return;
    }
    this.forceClosedReason = reason;
    try
    {
      if (!skipLocalTeardown)
      {
        if ((!getAutoCommit()) && (issueRollback)) {
          try
          {
            rollback();
          }
          catch (SQLException ex)
          {
            sqlEx = ex;
          }
        }
        reportMetrics();
        if (getUseUsageAdvisor())
        {
          if (!calledExplicitly)
          {
            String message = "Connection implicitly closed by Driver. You should call Connection.close() from your code to free resources more efficiently and avoid resource leaks.";
            
            this.eventSink.consumeEvent(new ProfilerEvent((byte)0, "", getCatalog(), getId(), -1, -1, System.currentTimeMillis(), 0L, Constants.MILLIS_I18N, null, this.pointOfOrigin, message));
          }
          long connectionLifeTime = System.currentTimeMillis() - this.connectionCreationTimeMillis;
          if (connectionLifeTime < 500L)
          {
            String message = "Connection lifetime of < .5 seconds. You might be un-necessarily creating short-lived connections and should investigate connection pooling to be more efficient.";
            
            this.eventSink.consumeEvent(new ProfilerEvent((byte)0, "", getCatalog(), getId(), -1, -1, System.currentTimeMillis(), 0L, Constants.MILLIS_I18N, null, this.pointOfOrigin, message));
          }
        }
        try
        {
          closeAllOpenStatements();
        }
        catch (SQLException ex)
        {
          sqlEx = ex;
        }
        if (this.io != null) {
          try
          {
            this.io.quit();
          }
          catch (Exception e) {}
        }
      }
      else
      {
        this.io.forceClose();
      }
      if (this.statementInterceptors != null) {
        for (int i = 0; i < this.statementInterceptors.size(); i++) {
          ((StatementInterceptor)this.statementInterceptors.get(i)).destroy();
        }
      }
      if (this.exceptionInterceptor != null) {
        this.exceptionInterceptor.destroy();
      }
    }
    finally
    {
      this.openStatements = null;
      this.io = null;
      this.statementInterceptors = null;
      this.exceptionInterceptor = null;
      ProfilerEventHandlerFactory.removeInstance(this);
      this.isClosed = true;
    }
    if (sqlEx != null) {
      throw sqlEx;
    }
  }
  
  protected void recachePreparedStatement(ServerPreparedStatement pstmt)
    throws SQLException
  {
    if (pstmt.isPoolable()) {
      synchronized (this.serverSideStatementCache)
      {
        this.serverSideStatementCache.put(pstmt.originalSql, pstmt);
      }
    }
  }
  
  protected void registerQueryExecutionTime(long queryTimeMs)
  {
    if (queryTimeMs > this.longestQueryTimeMs)
    {
      this.longestQueryTimeMs = queryTimeMs;
      
      repartitionPerformanceHistogram();
    }
    addToPerformanceHistogram(queryTimeMs, 1);
    if (queryTimeMs < this.shortestQueryTimeMs) {
      this.shortestQueryTimeMs = (queryTimeMs == 0L ? 1L : queryTimeMs);
    }
    this.numberOfQueriesIssued += 1L;
    
    this.totalQueryTimeMs += queryTimeMs;
  }
  
  void registerStatement(Statement stmt)
  {
    synchronized (this.openStatements)
    {
      this.openStatements.put(stmt, stmt);
    }
  }
  
  private void repartitionHistogram(int[] histCounts, long[] histBreakpoints, long currentLowerBound, long currentUpperBound)
  {
    if (this.oldHistCounts == null)
    {
      this.oldHistCounts = new int[histCounts.length];
      this.oldHistBreakpoints = new long[histBreakpoints.length];
    }
    System.arraycopy(histCounts, 0, this.oldHistCounts, 0, histCounts.length);
    
    System.arraycopy(histBreakpoints, 0, this.oldHistBreakpoints, 0, histBreakpoints.length);
    
    createInitialHistogram(histBreakpoints, currentLowerBound, currentUpperBound);
    for (int i = 0; i < 20; i++) {
      addToHistogram(histCounts, histBreakpoints, this.oldHistBreakpoints[i], this.oldHistCounts[i], currentLowerBound, currentUpperBound);
    }
  }
  
  private void repartitionPerformanceHistogram()
  {
    checkAndCreatePerformanceHistogram();
    
    repartitionHistogram(this.perfMetricsHistCounts, this.perfMetricsHistBreakpoints, this.shortestQueryTimeMs == Long.MAX_VALUE ? 0L : this.shortestQueryTimeMs, this.longestQueryTimeMs);
  }
  
  private void repartitionTablesAccessedHistogram()
  {
    checkAndCreateTablesAccessedHistogram();
    
    repartitionHistogram(this.numTablesMetricsHistCounts, this.numTablesMetricsHistBreakpoints, this.minimumNumberTablesAccessed == Long.MAX_VALUE ? 0L : this.minimumNumberTablesAccessed, this.maximumNumberTablesAccessed);
  }
  
  private void reportMetrics()
  {
    if (getGatherPerformanceMetrics())
    {
      StringBuffer logMessage = new StringBuffer(256);
      
      logMessage.append("** Performance Metrics Report **\n");
      logMessage.append("\nLongest reported query: " + this.longestQueryTimeMs + " ms");
      
      logMessage.append("\nShortest reported query: " + this.shortestQueryTimeMs + " ms");
      
      logMessage.append("\nAverage query execution time: " + this.totalQueryTimeMs / this.numberOfQueriesIssued + " ms");
      
      logMessage.append("\nNumber of statements executed: " + this.numberOfQueriesIssued);
      
      logMessage.append("\nNumber of result sets created: " + this.numberOfResultSetsCreated);
      
      logMessage.append("\nNumber of statements prepared: " + this.numberOfPrepares);
      
      logMessage.append("\nNumber of prepared statement executions: " + this.numberOfPreparedExecutes);
      if (this.perfMetricsHistBreakpoints != null)
      {
        logMessage.append("\n\n\tTiming Histogram:\n");
        int maxNumPoints = 20;
        int highestCount = Integer.MIN_VALUE;
        for (int i = 0; i < 20; i++) {
          if (this.perfMetricsHistCounts[i] > highestCount) {
            highestCount = this.perfMetricsHistCounts[i];
          }
        }
        if (highestCount == 0) {
          highestCount = 1;
        }
        for (int i = 0; i < 19; i++)
        {
          if (i == 0) {
            logMessage.append("\n\tless than " + this.perfMetricsHistBreakpoints[(i + 1)] + " ms: \t" + this.perfMetricsHistCounts[i]);
          } else {
            logMessage.append("\n\tbetween " + this.perfMetricsHistBreakpoints[i] + " and " + this.perfMetricsHistBreakpoints[(i + 1)] + " ms: \t" + this.perfMetricsHistCounts[i]);
          }
          logMessage.append("\t");
          
          int numPointsToGraph = (int)(maxNumPoints * (this.perfMetricsHistCounts[i] / highestCount));
          for (int j = 0; j < numPointsToGraph; j++) {
            logMessage.append("*");
          }
          if (this.longestQueryTimeMs < this.perfMetricsHistCounts[(i + 1)]) {
            break;
          }
        }
        if (this.perfMetricsHistBreakpoints[18] < this.longestQueryTimeMs)
        {
          logMessage.append("\n\tbetween ");
          logMessage.append(this.perfMetricsHistBreakpoints[18]);
          
          logMessage.append(" and ");
          logMessage.append(this.perfMetricsHistBreakpoints[19]);
          
          logMessage.append(" ms: \t");
          logMessage.append(this.perfMetricsHistCounts[19]);
        }
      }
      if (this.numTablesMetricsHistBreakpoints != null)
      {
        logMessage.append("\n\n\tTable Join Histogram:\n");
        int maxNumPoints = 20;
        int highestCount = Integer.MIN_VALUE;
        for (int i = 0; i < 20; i++) {
          if (this.numTablesMetricsHistCounts[i] > highestCount) {
            highestCount = this.numTablesMetricsHistCounts[i];
          }
        }
        if (highestCount == 0) {
          highestCount = 1;
        }
        for (int i = 0; i < 19; i++)
        {
          if (i == 0) {
            logMessage.append("\n\t" + this.numTablesMetricsHistBreakpoints[(i + 1)] + " tables or less: \t\t" + this.numTablesMetricsHistCounts[i]);
          } else {
            logMessage.append("\n\tbetween " + this.numTablesMetricsHistBreakpoints[i] + " and " + this.numTablesMetricsHistBreakpoints[(i + 1)] + " tables: \t" + this.numTablesMetricsHistCounts[i]);
          }
          logMessage.append("\t");
          
          int numPointsToGraph = (int)(maxNumPoints * (this.numTablesMetricsHistCounts[i] / highestCount));
          for (int j = 0; j < numPointsToGraph; j++) {
            logMessage.append("*");
          }
          if (this.maximumNumberTablesAccessed < this.numTablesMetricsHistBreakpoints[(i + 1)]) {
            break;
          }
        }
        if (this.numTablesMetricsHistBreakpoints[18] < this.maximumNumberTablesAccessed)
        {
          logMessage.append("\n\tbetween ");
          logMessage.append(this.numTablesMetricsHistBreakpoints[18]);
          
          logMessage.append(" and ");
          logMessage.append(this.numTablesMetricsHistBreakpoints[19]);
          
          logMessage.append(" tables: ");
          logMessage.append(this.numTablesMetricsHistCounts[19]);
        }
      }
      this.log.logInfo(logMessage);
      
      this.metricsLastReportedMs = System.currentTimeMillis();
    }
  }
  
  private void reportMetricsIfNeeded()
  {
    if ((getGatherPerformanceMetrics()) && 
      (System.currentTimeMillis() - this.metricsLastReportedMs > getReportMetricsIntervalMillis())) {
      reportMetrics();
    }
  }
  
  protected void reportNumberOfTablesAccessed(int numTablesAccessed)
  {
    if (numTablesAccessed < this.minimumNumberTablesAccessed) {
      this.minimumNumberTablesAccessed = numTablesAccessed;
    }
    if (numTablesAccessed > this.maximumNumberTablesAccessed)
    {
      this.maximumNumberTablesAccessed = numTablesAccessed;
      
      repartitionTablesAccessedHistogram();
    }
    addToTablesAccessedHistogram(numTablesAccessed, 1);
  }
  
  public void resetServerState()
    throws SQLException
  {
    if ((!getParanoid()) && (this.io != null) && (versionMeetsMinimum(4, 0, 6))) {
      changeUser(this.user, this.password);
    }
  }
  
  public void rollback()
    throws SQLException
  {
    synchronized (getMutex())
    {
      checkClosed();
      try
      {
        if (this.connectionLifecycleInterceptors != null)
        {
          IterateBlock iter = new IterateBlock(this.connectionLifecycleInterceptors.iterator())
          {
            void forEach(Object each)
              throws SQLException
            {
              if (!((ConnectionLifecycleInterceptor)each).rollback()) {
                this.stopIterating = true;
              }
            }
          };
          iter.doForAll();
          if (!iter.fullIteration())
          {
            this.needsPing = getReconnectAtTxEnd(); return;
          }
        }
        if ((this.autoCommit) && (!getRelaxAutoCommit())) {
          throw SQLError.createSQLException("Can't call rollback when autocommit=true", "08003", getExceptionInterceptor());
        }
        if (this.transactionsSupported) {
          try
          {
            rollbackNoChecks();
          }
          catch (SQLException sqlEx)
          {
            if ((getIgnoreNonTxTables()) && (sqlEx.getErrorCode() != 1196)) {
              throw sqlEx;
            }
          }
        }
      }
      catch (SQLException sqlException)
      {
        if ("08S01".equals(sqlException.getSQLState())) {
          throw SQLError.createSQLException("Communications link failure during rollback(). Transaction resolution unknown.", "08007", getExceptionInterceptor());
        }
        throw sqlException;
      }
      finally
      {
        this.needsPing = getReconnectAtTxEnd();
      }
    }
  }
  
  public void rollback(Savepoint savepoint)
    throws SQLException
  {
    if ((versionMeetsMinimum(4, 0, 14)) || (versionMeetsMinimum(4, 1, 1))) {
      synchronized (getMutex())
      {
        checkClosed();
        try
        {
          if (this.connectionLifecycleInterceptors != null)
          {
            IterateBlock iter = new IterateBlock(this.connectionLifecycleInterceptors.iterator())
            {
              private final Savepoint val$savepoint;
              
              void forEach(Object each)
                throws SQLException
              {
                if (!((ConnectionLifecycleInterceptor)each).rollback(this.val$savepoint)) {
                  this.stopIterating = true;
                }
              }
            };
            iter.doForAll();
            if (!iter.fullIteration())
            {
              this.needsPing = getReconnectAtTxEnd(); return;
            }
          }
          StringBuffer rollbackQuery = new StringBuffer("ROLLBACK TO SAVEPOINT ");
          
          rollbackQuery.append('`');
          rollbackQuery.append(savepoint.getSavepointName());
          rollbackQuery.append('`');
          
          java.sql.Statement stmt = null;
          try
          {
            stmt = getMetadataSafeStatement();
            
            stmt.executeUpdate(rollbackQuery.toString());
          }
          catch (SQLException sqlEx)
          {
            int errno = sqlEx.getErrorCode();
            if (errno == 1181)
            {
              String msg = sqlEx.getMessage();
              if (msg != null)
              {
                int indexOfError153 = msg.indexOf("153");
                if (indexOfError153 != -1) {
                  throw SQLError.createSQLException("Savepoint '" + savepoint.getSavepointName() + "' does not exist", "S1009", errno, getExceptionInterceptor());
                }
              }
            }
            if ((getIgnoreNonTxTables()) && (sqlEx.getErrorCode() != 1196)) {
              throw sqlEx;
            }
            if ("08S01".equals(sqlEx.getSQLState())) {
              throw SQLError.createSQLException("Communications link failure during rollback(). Transaction resolution unknown.", "08007", getExceptionInterceptor());
            }
            throw sqlEx;
          }
          finally
          {
            closeStatement(stmt);
          }
        }
        finally
        {
          this.needsPing = getReconnectAtTxEnd();
        }
      }
    }
    throw SQLError.notImplemented();
  }
  
  private void rollbackNoChecks()
    throws SQLException
  {
    if ((getUseLocalTransactionState()) && (versionMeetsMinimum(5, 0, 0)) && 
      (!this.io.inTransactionOnServer())) {
      return;
    }
    execSQL(null, "rollback", -1, null, 1003, 1007, false, this.database, null, false);
  }
  
  public java.sql.PreparedStatement serverPrepareStatement(String sql)
    throws SQLException
  {
    String nativeSql = getProcessEscapeCodesForPrepStmts() ? nativeSQL(sql) : sql;
    
    return ServerPreparedStatement.getInstance(this, nativeSql, getCatalog(), 1003, 1007);
  }
  
  public java.sql.PreparedStatement serverPrepareStatement(String sql, int autoGenKeyIndex)
    throws SQLException
  {
    String nativeSql = getProcessEscapeCodesForPrepStmts() ? nativeSQL(sql) : sql;
    
    PreparedStatement pStmt = ServerPreparedStatement.getInstance(this, nativeSql, getCatalog(), 1003, 1007);
    
    pStmt.setRetrieveGeneratedKeys(autoGenKeyIndex == 1);
    
    return pStmt;
  }
  
  public java.sql.PreparedStatement serverPrepareStatement(String sql, int resultSetType, int resultSetConcurrency)
    throws SQLException
  {
    String nativeSql = getProcessEscapeCodesForPrepStmts() ? nativeSQL(sql) : sql;
    
    return ServerPreparedStatement.getInstance(this, nativeSql, getCatalog(), resultSetType, resultSetConcurrency);
  }
  
  public java.sql.PreparedStatement serverPrepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability)
    throws SQLException
  {
    if ((getPedantic()) && 
      (resultSetHoldability != 1)) {
      throw SQLError.createSQLException("HOLD_CUSRORS_OVER_COMMIT is only supported holdability level", "S1009", getExceptionInterceptor());
    }
    return serverPrepareStatement(sql, resultSetType, resultSetConcurrency);
  }
  
  public java.sql.PreparedStatement serverPrepareStatement(String sql, int[] autoGenKeyIndexes)
    throws SQLException
  {
    PreparedStatement pStmt = (PreparedStatement)serverPrepareStatement(sql);
    
    pStmt.setRetrieveGeneratedKeys((autoGenKeyIndexes != null) && (autoGenKeyIndexes.length > 0));
    
    return pStmt;
  }
  
  public java.sql.PreparedStatement serverPrepareStatement(String sql, String[] autoGenKeyColNames)
    throws SQLException
  {
    PreparedStatement pStmt = (PreparedStatement)serverPrepareStatement(sql);
    
    pStmt.setRetrieveGeneratedKeys((autoGenKeyColNames != null) && (autoGenKeyColNames.length > 0));
    
    return pStmt;
  }
  
  protected boolean serverSupportsConvertFn()
    throws SQLException
  {
    return versionMeetsMinimum(4, 0, 2);
  }
  
  public void setAutoCommit(boolean autoCommitFlag)
    throws SQLException
  {
    synchronized (getMutex())
    {
      checkClosed();
      if (this.connectionLifecycleInterceptors != null)
      {
        IterateBlock iter = new IterateBlock(this.connectionLifecycleInterceptors.iterator())
        {
          private final boolean val$autoCommitFlag;
          
          void forEach(Object each)
            throws SQLException
          {
            if (!((ConnectionLifecycleInterceptor)each).setAutoCommit(this.val$autoCommitFlag)) {
              this.stopIterating = true;
            }
          }
        };
        iter.doForAll();
        if (!iter.fullIteration()) {
          return;
        }
      }
      if (getAutoReconnectForPools()) {
        setHighAvailability(true);
      }
      try
      {
        if (this.transactionsSupported)
        {
          boolean needsSetOnServer = true;
          if ((getUseLocalSessionState()) && (this.autoCommit == autoCommitFlag)) {
            needsSetOnServer = false;
          } else if (!getHighAvailability()) {
            needsSetOnServer = getIO().isSetNeededForAutoCommitMode(autoCommitFlag);
          }
          this.autoCommit = autoCommitFlag;
          if (needsSetOnServer) {
            execSQL(null, autoCommitFlag ? "SET autocommit=1" : "SET autocommit=0", -1, null, 1003, 1007, false, this.database, null, false);
          }
        }
        else
        {
          if ((!autoCommitFlag) && (!getRelaxAutoCommit())) {
            throw SQLError.createSQLException("MySQL Versions Older than 3.23.15 do not support transactions", "08003", getExceptionInterceptor());
          }
          this.autoCommit = autoCommitFlag;
        }
      }
      finally
      {
        if (getAutoReconnectForPools()) {
          setHighAvailability(false);
        }
      }
      return;
    }
  }
  
  public void setCatalog(String catalog)
    throws SQLException
  {
    synchronized (getMutex())
    {
      checkClosed();
      if (catalog == null) {
        throw SQLError.createSQLException("Catalog can not be null", "S1009", getExceptionInterceptor());
      }
      if (this.connectionLifecycleInterceptors != null)
      {
        IterateBlock iter = new IterateBlock(this.connectionLifecycleInterceptors.iterator())
        {
          private final String val$catalog;
          
          void forEach(Object each)
            throws SQLException
          {
            if (!((ConnectionLifecycleInterceptor)each).setCatalog(this.val$catalog)) {
              this.stopIterating = true;
            }
          }
        };
        iter.doForAll();
        if (!iter.fullIteration()) {
          return;
        }
      }
      if (getUseLocalSessionState()) {
        if (this.lowerCaseTableNames)
        {
          if (!this.database.equalsIgnoreCase(catalog)) {}
        }
        else if (this.database.equals(catalog)) {
          return;
        }
      }
      String quotedId = this.dbmd.getIdentifierQuoteString();
      if ((quotedId == null) || (quotedId.equals(" "))) {
        quotedId = "";
      }
      StringBuffer query = new StringBuffer("USE ");
      query.append(quotedId);
      query.append(catalog);
      query.append(quotedId);
      
      execSQL(null, query.toString(), -1, null, 1003, 1007, false, this.database, null, false);
      
      this.database = catalog;
    }
  }
  
  public synchronized void setFailedOver(boolean flag)
  {
    if ((flag) && (getRoundRobinLoadBalance())) {
      return;
    }
    this.failedOver = flag;
  }
  
  private void setFailedOverState()
    throws SQLException
  {
    if (getRoundRobinLoadBalance()) {
      return;
    }
    if (getFailOverReadOnly()) {
      setReadOnlyInternal(true);
    }
    this.queriesIssuedFailedOver = 0L;
    this.failedOver = true;
    this.masterFailTimeMillis = System.currentTimeMillis();
  }
  
  public void setInGlobalTx(boolean flag)
  {
    this.isInGlobalTx = flag;
  }
  
  public void setPreferSlaveDuringFailover(boolean flag)
  {
    this.preferSlaveDuringFailover = flag;
  }
  
  void setReadInfoMsgEnabled(boolean flag)
  {
    this.readInfoMsg = flag;
  }
  
  public void setReadOnly(boolean readOnlyFlag)
    throws SQLException
  {
    checkClosed();
    if ((this.failedOver) && (getFailOverReadOnly()) && (!readOnlyFlag)) {
      return;
    }
    setReadOnlyInternal(readOnlyFlag);
  }
  
  protected void setReadOnlyInternal(boolean readOnlyFlag)
    throws SQLException
  {
    this.readOnly = readOnlyFlag;
  }
  
  public Savepoint setSavepoint()
    throws SQLException
  {
    MysqlSavepoint savepoint = new MysqlSavepoint(getExceptionInterceptor());
    
    setSavepoint(savepoint);
    
    return savepoint;
  }
  
  private void setSavepoint(MysqlSavepoint savepoint)
    throws SQLException
  {
    if ((versionMeetsMinimum(4, 0, 14)) || (versionMeetsMinimum(4, 1, 1))) {
      synchronized (getMutex())
      {
        checkClosed();
        
        StringBuffer savePointQuery = new StringBuffer("SAVEPOINT ");
        savePointQuery.append('`');
        savePointQuery.append(savepoint.getSavepointName());
        savePointQuery.append('`');
        
        java.sql.Statement stmt = null;
        try
        {
          stmt = getMetadataSafeStatement();
          
          stmt.executeUpdate(savePointQuery.toString());
        }
        finally
        {
          closeStatement(stmt);
        }
      }
    }
    throw SQLError.notImplemented();
  }
  
  public synchronized Savepoint setSavepoint(String name)
    throws SQLException
  {
    MysqlSavepoint savepoint = new MysqlSavepoint(name, getExceptionInterceptor());
    
    setSavepoint(savepoint);
    
    return savepoint;
  }
  
  private void setSessionVariables()
    throws SQLException
  {
    if ((versionMeetsMinimum(4, 0, 0)) && (getSessionVariables() != null))
    {
      List variablesToSet = StringUtils.split(getSessionVariables(), ",", "\"'", "\"'", false);
      
      int numVariablesToSet = variablesToSet.size();
      
      java.sql.Statement stmt = null;
      try
      {
        stmt = getMetadataSafeStatement();
        for (int i = 0; i < numVariablesToSet; i++)
        {
          String variableValuePair = (String)variablesToSet.get(i);
          if (variableValuePair.startsWith("@")) {
            stmt.executeUpdate("SET " + variableValuePair);
          } else {
            stmt.executeUpdate("SET SESSION " + variableValuePair);
          }
        }
      }
      finally
      {
        if (stmt != null) {
          stmt.close();
        }
      }
    }
  }
  
  public synchronized void setTransactionIsolation(int level)
    throws SQLException
  {
    checkClosed();
    if (this.hasIsolationLevels)
    {
      String sql = null;
      
      boolean shouldSendSet = false;
      if (getAlwaysSendSetIsolation()) {
        shouldSendSet = true;
      } else if (level != this.isolationLevel) {
        shouldSendSet = true;
      }
      if (getUseLocalSessionState()) {
        shouldSendSet = this.isolationLevel != level;
      }
      if (shouldSendSet)
      {
        switch (level)
        {
        case 0: 
          throw SQLError.createSQLException("Transaction isolation level NONE not supported by MySQL", getExceptionInterceptor());
        case 2: 
          sql = "SET SESSION TRANSACTION ISOLATION LEVEL READ COMMITTED";
          
          break;
        case 1: 
          sql = "SET SESSION TRANSACTION ISOLATION LEVEL READ UNCOMMITTED";
          
          break;
        case 4: 
          sql = "SET SESSION TRANSACTION ISOLATION LEVEL REPEATABLE READ";
          
          break;
        case 8: 
          sql = "SET SESSION TRANSACTION ISOLATION LEVEL SERIALIZABLE";
          
          break;
        case 3: 
        case 5: 
        case 6: 
        case 7: 
        default: 
          throw SQLError.createSQLException("Unsupported transaction isolation level '" + level + "'", "S1C00", getExceptionInterceptor());
        }
        execSQL(null, sql, -1, null, 1003, 1007, false, this.database, null, false);
        
        this.isolationLevel = level;
      }
    }
    else
    {
      throw SQLError.createSQLException("Transaction Isolation Levels are not supported on MySQL versions older than 3.23.36.", "S1C00", getExceptionInterceptor());
    }
  }
  
  public synchronized void setTypeMap(Map map)
    throws SQLException
  {
    this.typeMap = map;
  }
  
  private void setupServerForTruncationChecks()
    throws SQLException
  {
    if ((getJdbcCompliantTruncation()) && 
      (versionMeetsMinimum(5, 0, 2)))
    {
      String currentSqlMode = (String)this.serverVariables.get("sql_mode");
      
      boolean strictTransTablesIsSet = StringUtils.indexOfIgnoreCase(currentSqlMode, "STRICT_TRANS_TABLES") != -1;
      if ((currentSqlMode == null) || (currentSqlMode.length() == 0) || (!strictTransTablesIsSet))
      {
        StringBuffer commandBuf = new StringBuffer("SET sql_mode='");
        if ((currentSqlMode != null) && (currentSqlMode.length() > 0))
        {
          commandBuf.append(currentSqlMode);
          commandBuf.append(",");
        }
        commandBuf.append("STRICT_TRANS_TABLES'");
        
        execSQL(null, commandBuf.toString(), -1, null, 1003, 1007, false, this.database, null, false);
        
        setJdbcCompliantTruncation(false);
      }
      else if (strictTransTablesIsSet)
      {
        setJdbcCompliantTruncation(false);
      }
    }
  }
  
  private boolean shouldFallBack()
  {
    long secondsSinceFailedOver = (System.currentTimeMillis() - this.masterFailTimeMillis) / 1000L;
    
    boolean tryFallback = (secondsSinceFailedOver >= getSecondsBeforeRetryMaster()) || (this.queriesIssuedFailedOver >= getQueriesBeforeRetryMaster());
    
    return tryFallback;
  }
  
  public void shutdownServer()
    throws SQLException
  {
    try
    {
      this.io.sendCommand(8, null, null, false, null, 0);
    }
    catch (Exception ex)
    {
      SQLException sqlEx = SQLError.createSQLException(Messages.getString("Connection.UnhandledExceptionDuringShutdown"), "S1000", getExceptionInterceptor());
      
      sqlEx.initCause(ex);
      
      throw sqlEx;
    }
  }
  
  public boolean supportsIsolationLevel()
  {
    return this.hasIsolationLevels;
  }
  
  public boolean supportsQuotedIdentifiers()
  {
    return this.hasQuotedIdentifiers;
  }
  
  public boolean supportsTransactions()
  {
    return this.transactionsSupported;
  }
  
  void unregisterStatement(Statement stmt)
  {
    if (this.openStatements != null) {
      synchronized (this.openStatements)
      {
        this.openStatements.remove(stmt);
      }
    }
  }
  
  void unsetMaxRows(Statement stmt)
    throws SQLException
  {
    synchronized (this.mutex)
    {
      if (this.statementsUsingMaxRows != null)
      {
        Object found = this.statementsUsingMaxRows.remove(stmt);
        if ((found != null) && (this.statementsUsingMaxRows.size() == 0))
        {
          execSQL(null, "SET OPTION SQL_SELECT_LIMIT=DEFAULT", -1, null, 1003, 1007, false, this.database, null, false);
          
          this.maxRowsChanged = false;
        }
      }
    }
  }
  
  boolean useAnsiQuotedIdentifiers()
  {
    return this.useAnsiQuotes;
  }
  
  public boolean versionMeetsMinimum(int major, int minor, int subminor)
    throws SQLException
  {
    checkClosed();
    
    return this.io.versionMeetsMinimum(major, minor, subminor);
  }
  
  protected void initializeResultsMetadataFromCache(String sql, CachedResultSetMetaData cachedMetaData, ResultSetInternalMethods resultSet)
    throws SQLException
  {
    if (cachedMetaData == null)
    {
      cachedMetaData = new CachedResultSetMetaData();
      
      resultSet.buildIndexMapping();
      resultSet.initializeWithMetadata();
      if ((resultSet instanceof UpdatableResultSet)) {
        ((UpdatableResultSet)resultSet).checkUpdatability();
      }
      resultSet.populateCachedMetaData(cachedMetaData);
      
      this.resultSetMetadataCache.put(sql, cachedMetaData);
    }
    else
    {
      resultSet.initializeFromCachedMetaData(cachedMetaData);
      resultSet.initializeWithMetadata();
      if ((resultSet instanceof UpdatableResultSet)) {
        ((UpdatableResultSet)resultSet).checkUpdatability();
      }
    }
  }
  
  public String getStatementComment()
  {
    return this.statementComment;
  }
  
  public void setStatementComment(String comment)
  {
    this.statementComment = comment;
  }
  
  public synchronized void reportQueryTime(long millisOrNanos)
  {
    this.queryTimeCount += 1L;
    this.queryTimeSum += millisOrNanos;
    this.queryTimeSumSquares += millisOrNanos * millisOrNanos;
    this.queryTimeMean = ((this.queryTimeMean * (this.queryTimeCount - 1L) + millisOrNanos) / this.queryTimeCount);
  }
  
  public synchronized boolean isAbonormallyLongQuery(long millisOrNanos)
  {
    if (this.queryTimeCount < 15L) {
      return false;
    }
    double stddev = Math.sqrt((this.queryTimeSumSquares - this.queryTimeSum * this.queryTimeSum / this.queryTimeCount) / (this.queryTimeCount - 1L));
    
    return millisOrNanos > this.queryTimeMean + 5.0D * stddev;
  }
  
  public void initializeExtension(Extension ex)
    throws SQLException
  {
    ex.init(this, this.props);
  }
  
  protected void transactionBegun()
    throws SQLException
  {
    if (this.connectionLifecycleInterceptors != null)
    {
      IterateBlock iter = new IterateBlock(this.connectionLifecycleInterceptors.iterator())
      {
        void forEach(Object each)
          throws SQLException
        {
          ((ConnectionLifecycleInterceptor)each).transactionBegun();
        }
      };
      iter.doForAll();
    }
  }
  
  protected void transactionCompleted()
    throws SQLException
  {
    if (this.connectionLifecycleInterceptors != null)
    {
      IterateBlock iter = new IterateBlock(this.connectionLifecycleInterceptors.iterator())
      {
        void forEach(Object each)
          throws SQLException
        {
          ((ConnectionLifecycleInterceptor)each).transactionCompleted();
        }
      };
      iter.doForAll();
    }
  }
  
  public boolean storesLowerCaseTableName()
  {
    return this.storesLowerCaseTableName;
  }
  
  public ExceptionInterceptor getExceptionInterceptor()
  {
    return this.exceptionInterceptor;
  }
  
  public boolean getRequiresEscapingEncoder()
  {
    return this.requiresEscapingEncoder;
  }
  
  protected ConnectionImpl() {}
  
  public void clearWarnings()
    throws SQLException
  {}
  
  /* Error */
  public int getActiveStatementCount()
  {
    // Byte code:
    //   0: aload_0
    //   1: getfield 139	com/mysql/jdbc/ConnectionImpl:openStatements	Ljava/util/Map;
    //   4: ifnull +27 -> 31
    //   7: aload_0
    //   8: getfield 139	com/mysql/jdbc/ConnectionImpl:openStatements	Ljava/util/Map;
    //   11: dup
    //   12: astore_1
    //   13: monitorenter
    //   14: aload_0
    //   15: getfield 139	com/mysql/jdbc/ConnectionImpl:openStatements	Ljava/util/Map;
    //   18: invokeinterface 303 1 0
    //   23: aload_1
    //   24: monitorexit
    //   25: ireturn
    //   26: astore_2
    //   27: aload_1
    //   28: monitorexit
    //   29: aload_2
    //   30: athrow
    //   31: iconst_0
    //   32: ireturn
    // Line number table:
    //   Java source line #2779	-> byte code offset #0
    //   Java source line #2780	-> byte code offset #7
    //   Java source line #2781	-> byte code offset #14
    //   Java source line #2782	-> byte code offset #26
    //   Java source line #2785	-> byte code offset #31
    // Local variable table:
    //   start	length	slot	name	signature
    //   0	33	0	this	ConnectionImpl
    //   12	16	1	Ljava/lang/Object;	Object
    //   26	4	2	localObject1	Object
    // Exception table:
    //   from	to	target	type
    //   14	25	26	finally
    //   26	29	26	finally
  }
  
  public void releaseSavepoint(Savepoint arg0)
    throws SQLException
  {}
  
  public void setHoldability(int arg0)
    throws SQLException
  {}
  
  /* Error */
  boolean useMaxRows()
  {
    // Byte code:
    //   0: aload_0
    //   1: getfield 79	com/mysql/jdbc/ConnectionImpl:mutex	Ljava/lang/Object;
    //   4: dup
    //   5: astore_1
    //   6: monitorenter
    //   7: aload_0
    //   8: getfield 74	com/mysql/jdbc/ConnectionImpl:maxRowsChanged	Z
    //   11: aload_1
    //   12: monitorexit
    //   13: ireturn
    //   14: astore_2
    //   15: aload_1
    //   16: monitorexit
    //   17: aload_2
    //   18: athrow
    // Line number table:
    //   Java source line #5536	-> byte code offset #0
    //   Java source line #5537	-> byte code offset #7
    //   Java source line #5538	-> byte code offset #14
    // Local variable table:
    //   start	length	slot	name	signature
    //   0	19	0	this	ConnectionImpl
    //   5	11	1	Ljava/lang/Object;	Object
    //   14	4	2	localObject1	Object
    // Exception table:
    //   from	to	target	type
    //   7	13	14	finally
    //   14	17	14	finally
  }
  
  /* Error */
  protected CachedResultSetMetaData getCachedMetaData(String sql)
  {
    // Byte code:
    //   0: aload_0
    //   1: getfield 552	com/mysql/jdbc/ConnectionImpl:resultSetMetadataCache	Lcom/mysql/jdbc/util/LRUCache;
    //   4: ifnull +29 -> 33
    //   7: aload_0
    //   8: getfield 552	com/mysql/jdbc/ConnectionImpl:resultSetMetadataCache	Lcom/mysql/jdbc/util/LRUCache;
    //   11: dup
    //   12: astore_2
    //   13: monitorenter
    //   14: aload_0
    //   15: getfield 552	com/mysql/jdbc/ConnectionImpl:resultSetMetadataCache	Lcom/mysql/jdbc/util/LRUCache;
    //   18: aload_1
    //   19: invokevirtual 221	com/mysql/jdbc/util/LRUCache:get	(Ljava/lang/Object;)Ljava/lang/Object;
    //   22: checkcast 791	com/mysql/jdbc/CachedResultSetMetaData
    //   25: aload_2
    //   26: monitorexit
    //   27: areturn
    //   28: astore_3
    //   29: aload_2
    //   30: monitorexit
    //   31: aload_3
    //   32: athrow
    //   33: aconst_null
    //   34: areturn
    // Line number table:
    //   Java source line #5563	-> byte code offset #0
    //   Java source line #5564	-> byte code offset #7
    //   Java source line #5565	-> byte code offset #14
    //   Java source line #5567	-> byte code offset #28
    //   Java source line #5570	-> byte code offset #33
    // Local variable table:
    //   start	length	slot	name	signature
    //   0	35	0	this	ConnectionImpl
    //   0	35	1	sql	String
    //   12	18	2	Ljava/lang/Object;	Object
    //   28	4	3	localObject1	Object
    // Exception table:
    //   from	to	target	type
    //   14	27	28	finally
    //   28	31	28	finally
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\mysql\jdbc\ConnectionImpl.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */