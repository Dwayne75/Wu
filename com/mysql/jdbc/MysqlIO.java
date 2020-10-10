package com.mysql.jdbc;

import com.mysql.jdbc.exceptions.MySQLStatementCancelledException;
import com.mysql.jdbc.exceptions.MySQLTimeoutException;
import com.mysql.jdbc.log.Log;
import com.mysql.jdbc.profiler.ProfilerEvent;
import com.mysql.jdbc.profiler.ProfilerEventHandler;
import com.mysql.jdbc.profiler.ProfilerEventHandlerFactory;
import com.mysql.jdbc.util.ReadAheadInputStream;
import com.mysql.jdbc.util.ResultSetUtil;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.lang.ref.SoftReference;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.SocketException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.zip.Deflater;

class MysqlIO
{
  private static final int UTF8_CHARSET_INDEX = 33;
  private static final String CODE_PAGE_1252 = "Cp1252";
  protected static final int NULL_LENGTH = -1;
  protected static final int COMP_HEADER_LENGTH = 3;
  protected static final int MIN_COMPRESS_LEN = 50;
  protected static final int HEADER_LENGTH = 4;
  protected static final int AUTH_411_OVERHEAD = 33;
  private static int maxBufferSize = 65535;
  private static final int CLIENT_COMPRESS = 32;
  protected static final int CLIENT_CONNECT_WITH_DB = 8;
  private static final int CLIENT_FOUND_ROWS = 2;
  private static final int CLIENT_LOCAL_FILES = 128;
  private static final int CLIENT_LONG_FLAG = 4;
  private static final int CLIENT_LONG_PASSWORD = 1;
  private static final int CLIENT_PROTOCOL_41 = 512;
  private static final int CLIENT_INTERACTIVE = 1024;
  protected static final int CLIENT_SSL = 2048;
  private static final int CLIENT_TRANSACTIONS = 8192;
  protected static final int CLIENT_RESERVED = 16384;
  protected static final int CLIENT_SECURE_CONNECTION = 32768;
  private static final int CLIENT_MULTI_QUERIES = 65536;
  private static final int CLIENT_MULTI_RESULTS = 131072;
  private static final int SERVER_STATUS_IN_TRANS = 1;
  private static final int SERVER_STATUS_AUTOCOMMIT = 2;
  static final int SERVER_MORE_RESULTS_EXISTS = 8;
  private static final int SERVER_QUERY_NO_GOOD_INDEX_USED = 16;
  private static final int SERVER_QUERY_NO_INDEX_USED = 32;
  private static final int SERVER_QUERY_WAS_SLOW = 2048;
  private static final int SERVER_STATUS_CURSOR_EXISTS = 64;
  private static final String FALSE_SCRAMBLE = "xxxxxxxx";
  protected static final int MAX_QUERY_SIZE_TO_LOG = 1024;
  protected static final int MAX_QUERY_SIZE_TO_EXPLAIN = 1048576;
  protected static final int INITIAL_PACKET_SIZE = 1024;
  private static String jvmPlatformCharset = null;
  protected static final String ZERO_DATE_VALUE_MARKER = "0000-00-00";
  protected static final String ZERO_DATETIME_VALUE_MARKER = "0000-00-00 00:00:00";
  private static final int MAX_PACKET_DUMP_LENGTH = 1024;
  
  static
  {
    OutputStreamWriter outWriter = null;
    try
    {
      outWriter = new OutputStreamWriter(new ByteArrayOutputStream());
      jvmPlatformCharset = outWriter.getEncoding();
    }
    finally
    {
      try
      {
        if (outWriter != null) {
          outWriter.close();
        }
      }
      catch (IOException ioEx) {}
    }
  }
  
  private boolean packetSequenceReset = false;
  protected int serverCharsetIndex;
  private Buffer reusablePacket = null;
  private Buffer sendPacket = null;
  private Buffer sharedSendPacket = null;
  protected BufferedOutputStream mysqlOutput = null;
  protected ConnectionImpl connection;
  private Deflater deflater = null;
  protected InputStream mysqlInput = null;
  private LinkedList packetDebugRingBuffer = null;
  private RowData streamingData = null;
  protected Socket mysqlConnection = null;
  private SocketFactory socketFactory = null;
  private SoftReference loadFileBufRef;
  private SoftReference splitBufRef;
  protected String host = null;
  protected String seed;
  private String serverVersion = null;
  private String socketFactoryClassName = null;
  private byte[] packetHeaderBuf = new byte[4];
  private boolean colDecimalNeedsBump = false;
  private boolean hadWarnings = false;
  private boolean has41NewNewProt = false;
  private boolean hasLongColumnInfo = false;
  private boolean isInteractiveClient = false;
  private boolean logSlowQueries = false;
  private boolean platformDbCharsetMatches = true;
  private boolean profileSql = false;
  private boolean queryBadIndexUsed = false;
  private boolean queryNoIndexUsed = false;
  private boolean serverQueryWasSlow = false;
  private boolean use41Extensions = false;
  private boolean useCompression = false;
  private boolean useNewLargePackets = false;
  private boolean useNewUpdateCounts = false;
  private byte packetSequence = 0;
  private byte readPacketSequence = -1;
  private boolean checkPacketSequence = false;
  private byte protocolVersion = 0;
  private int maxAllowedPacket = 1048576;
  protected int maxThreeBytes = 16581375;
  protected int port = 3306;
  protected int serverCapabilities;
  private int serverMajorVersion = 0;
  private int serverMinorVersion = 0;
  private int oldServerStatus = 0;
  private int serverStatus = 0;
  private int serverSubMinorVersion = 0;
  private int warningCount = 0;
  protected long clientParam = 0L;
  protected long lastPacketSentTimeMs = 0L;
  protected long lastPacketReceivedTimeMs = 0L;
  private boolean traceProtocol = false;
  private boolean enablePacketDebug = false;
  private Calendar sessionCalendar;
  private boolean useConnectWithDb;
  private boolean needToGrabQueryFromPacket;
  private boolean autoGenerateTestcaseScript;
  private long threadId;
  private boolean useNanosForElapsedTime;
  private long slowQueryThreshold;
  private String queryTimingUnits;
  private boolean useDirectRowUnpack = true;
  private int useBufferRowSizeThreshold;
  private int commandCount = 0;
  private List statementInterceptors;
  private ExceptionInterceptor exceptionInterceptor;
  
  public MysqlIO(String host, int port, Properties props, String socketFactoryClassName, ConnectionImpl conn, int socketTimeout, int useBufferRowSizeThreshold)
    throws IOException, SQLException
  {
    this.connection = conn;
    if (this.connection.getEnablePacketDebug()) {
      this.packetDebugRingBuffer = new LinkedList();
    }
    this.traceProtocol = this.connection.getTraceProtocol();
    
    this.useAutoSlowLog = this.connection.getAutoSlowLog();
    
    this.useBufferRowSizeThreshold = useBufferRowSizeThreshold;
    this.useDirectRowUnpack = this.connection.getUseDirectRowUnpack();
    
    this.logSlowQueries = this.connection.getLogSlowQueries();
    
    this.reusablePacket = new Buffer(1024);
    this.sendPacket = new Buffer(1024);
    
    this.port = port;
    this.host = host;
    
    this.socketFactoryClassName = socketFactoryClassName;
    this.socketFactory = createSocketFactory();
    this.exceptionInterceptor = this.connection.getExceptionInterceptor();
    try
    {
      this.mysqlConnection = this.socketFactory.connect(this.host, this.port, props);
      if (socketTimeout != 0) {
        try
        {
          this.mysqlConnection.setSoTimeout(socketTimeout);
        }
        catch (Exception ex) {}
      }
      this.mysqlConnection = this.socketFactory.beforeHandshake();
      if (this.connection.getUseReadAheadInput()) {
        this.mysqlInput = new ReadAheadInputStream(this.mysqlConnection.getInputStream(), 16384, this.connection.getTraceProtocol(), this.connection.getLog());
      } else if (this.connection.useUnbufferedInput()) {
        this.mysqlInput = this.mysqlConnection.getInputStream();
      } else {
        this.mysqlInput = new BufferedInputStream(this.mysqlConnection.getInputStream(), 16384);
      }
      this.mysqlOutput = new BufferedOutputStream(this.mysqlConnection.getOutputStream(), 16384);
      
      this.isInteractiveClient = this.connection.getInteractiveClient();
      this.profileSql = this.connection.getProfileSql();
      this.sessionCalendar = Calendar.getInstance();
      this.autoGenerateTestcaseScript = this.connection.getAutoGenerateTestcaseScript();
      
      this.needToGrabQueryFromPacket = ((this.profileSql) || (this.logSlowQueries) || (this.autoGenerateTestcaseScript));
      if ((this.connection.getUseNanosForElapsedTime()) && (Util.nanoTimeAvailable()))
      {
        this.useNanosForElapsedTime = true;
        
        this.queryTimingUnits = Messages.getString("Nanoseconds");
      }
      else
      {
        this.queryTimingUnits = Messages.getString("Milliseconds");
      }
      if (this.connection.getLogSlowQueries()) {
        calculateSlowQueryThreshold();
      }
    }
    catch (IOException ioEx)
    {
      throw SQLError.createCommunicationsException(this.connection, 0L, 0L, ioEx, getExceptionInterceptor());
    }
  }
  
  public boolean hasLongColumnInfo()
  {
    return this.hasLongColumnInfo;
  }
  
  protected boolean isDataAvailable()
    throws SQLException
  {
    try
    {
      return this.mysqlInput.available() > 0;
    }
    catch (IOException ioEx)
    {
      throw SQLError.createCommunicationsException(this.connection, this.lastPacketSentTimeMs, this.lastPacketReceivedTimeMs, ioEx, getExceptionInterceptor());
    }
  }
  
  protected long getLastPacketSentTimeMs()
  {
    return this.lastPacketSentTimeMs;
  }
  
  protected long getLastPacketReceivedTimeMs()
  {
    return this.lastPacketReceivedTimeMs;
  }
  
  protected ResultSetImpl getResultSet(StatementImpl callingStatement, long columnCount, int maxRows, int resultSetType, int resultSetConcurrency, boolean streamResults, String catalog, boolean isBinaryEncoded, Field[] metadataFromCache)
    throws SQLException
  {
    Field[] fields = null;
    if (metadataFromCache == null)
    {
      fields = new Field[(int)columnCount];
      for (int i = 0; i < columnCount; i++)
      {
        Buffer fieldPacket = null;
        
        fieldPacket = readPacket();
        fields[i] = unpackField(fieldPacket, false);
      }
    }
    else
    {
      for (int i = 0; i < columnCount; i++) {
        skipPacket();
      }
    }
    Buffer packet = reuseAndReadPacket(this.reusablePacket);
    
    readServerStatusForResultSets(packet);
    if ((this.connection.versionMeetsMinimum(5, 0, 2)) && (this.connection.getUseCursorFetch()) && (isBinaryEncoded) && (callingStatement != null) && (callingStatement.getFetchSize() != 0) && (callingStatement.getResultSetType() == 1003))
    {
      ServerPreparedStatement prepStmt = (ServerPreparedStatement)callingStatement;
      
      boolean usingCursor = true;
      if (this.connection.versionMeetsMinimum(5, 0, 5)) {
        usingCursor = (this.serverStatus & 0x40) != 0;
      }
      if (usingCursor)
      {
        RowData rows = new RowDataCursor(this, prepStmt, fields);
        
        ResultSetImpl rs = buildResultSetWithRows(callingStatement, catalog, fields, rows, resultSetType, resultSetConcurrency, isBinaryEncoded);
        if (usingCursor) {
          rs.setFetchSize(callingStatement.getFetchSize());
        }
        return rs;
      }
    }
    RowData rowData = null;
    if (!streamResults)
    {
      rowData = readSingleRowSet(columnCount, maxRows, resultSetConcurrency, isBinaryEncoded, metadataFromCache == null ? fields : metadataFromCache);
    }
    else
    {
      rowData = new RowDataDynamic(this, (int)columnCount, metadataFromCache == null ? fields : metadataFromCache, isBinaryEncoded);
      
      this.streamingData = rowData;
    }
    ResultSetImpl rs = buildResultSetWithRows(callingStatement, catalog, metadataFromCache == null ? fields : metadataFromCache, rowData, resultSetType, resultSetConcurrency, isBinaryEncoded);
    
    return rs;
  }
  
  protected final void forceClose()
  {
    try
    {
      if (this.mysqlInput != null) {
        this.mysqlInput.close();
      }
    }
    catch (IOException ioEx)
    {
      this.mysqlInput = null;
    }
    try
    {
      if (this.mysqlOutput != null) {
        this.mysqlOutput.close();
      }
    }
    catch (IOException ioEx)
    {
      this.mysqlOutput = null;
    }
    try
    {
      if (this.mysqlConnection != null) {
        this.mysqlConnection.close();
      }
    }
    catch (IOException ioEx)
    {
      this.mysqlConnection = null;
    }
  }
  
  protected final Field unpackField(Buffer packet, boolean extractDefaultValues)
    throws SQLException
  {
    if (this.use41Extensions)
    {
      if (this.has41NewNewProt)
      {
        int catalogNameStart = packet.getPosition() + 1;
        int catalogNameLength = packet.fastSkipLenString();
        catalogNameStart = adjustStartForFieldLength(catalogNameStart, catalogNameLength);
      }
      int databaseNameStart = packet.getPosition() + 1;
      int databaseNameLength = packet.fastSkipLenString();
      databaseNameStart = adjustStartForFieldLength(databaseNameStart, databaseNameLength);
      
      int tableNameStart = packet.getPosition() + 1;
      int tableNameLength = packet.fastSkipLenString();
      tableNameStart = adjustStartForFieldLength(tableNameStart, tableNameLength);
      
      int originalTableNameStart = packet.getPosition() + 1;
      int originalTableNameLength = packet.fastSkipLenString();
      originalTableNameStart = adjustStartForFieldLength(originalTableNameStart, originalTableNameLength);
      
      int nameStart = packet.getPosition() + 1;
      int nameLength = packet.fastSkipLenString();
      
      nameStart = adjustStartForFieldLength(nameStart, nameLength);
      
      int originalColumnNameStart = packet.getPosition() + 1;
      int originalColumnNameLength = packet.fastSkipLenString();
      originalColumnNameStart = adjustStartForFieldLength(originalColumnNameStart, originalColumnNameLength);
      
      packet.readByte();
      
      short charSetNumber = (short)packet.readInt();
      
      long colLength = 0L;
      if (this.has41NewNewProt) {
        colLength = packet.readLong();
      } else {
        colLength = packet.readLongInt();
      }
      int colType = packet.readByte() & 0xFF;
      
      short colFlag = 0;
      if (this.hasLongColumnInfo) {
        colFlag = (short)packet.readInt();
      } else {
        colFlag = (short)(packet.readByte() & 0xFF);
      }
      int colDecimals = packet.readByte() & 0xFF;
      
      int defaultValueStart = -1;
      int defaultValueLength = -1;
      if (extractDefaultValues)
      {
        defaultValueStart = packet.getPosition() + 1;
        defaultValueLength = packet.fastSkipLenString();
      }
      Field field = new Field(this.connection, packet.getByteBuffer(), databaseNameStart, databaseNameLength, tableNameStart, tableNameLength, originalTableNameStart, originalTableNameLength, nameStart, nameLength, originalColumnNameStart, originalColumnNameLength, colLength, colType, colFlag, colDecimals, defaultValueStart, defaultValueLength, charSetNumber);
      
      return field;
    }
    int tableNameStart = packet.getPosition() + 1;
    int tableNameLength = packet.fastSkipLenString();
    tableNameStart = adjustStartForFieldLength(tableNameStart, tableNameLength);
    
    int nameStart = packet.getPosition() + 1;
    int nameLength = packet.fastSkipLenString();
    nameStart = adjustStartForFieldLength(nameStart, nameLength);
    
    int colLength = packet.readnBytes();
    int colType = packet.readnBytes();
    packet.readByte();
    
    short colFlag = 0;
    if (this.hasLongColumnInfo) {
      colFlag = (short)packet.readInt();
    } else {
      colFlag = (short)(packet.readByte() & 0xFF);
    }
    int colDecimals = packet.readByte() & 0xFF;
    if (this.colDecimalNeedsBump) {
      colDecimals++;
    }
    Field field = new Field(this.connection, packet.getByteBuffer(), nameStart, nameLength, tableNameStart, tableNameLength, colLength, colType, colFlag, colDecimals);
    
    return field;
  }
  
  private int adjustStartForFieldLength(int nameStart, int nameLength)
  {
    if (nameLength < 251) {
      return nameStart;
    }
    if ((nameLength >= 251) && (nameLength < 65536)) {
      return nameStart + 2;
    }
    if ((nameLength >= 65536) && (nameLength < 16777216)) {
      return nameStart + 3;
    }
    return nameStart + 8;
  }
  
  protected boolean isSetNeededForAutoCommitMode(boolean autoCommitFlag)
  {
    if ((this.use41Extensions) && (this.connection.getElideSetAutoCommits()))
    {
      boolean autoCommitModeOnServer = (this.serverStatus & 0x2) != 0;
      if ((!autoCommitFlag) && (versionMeetsMinimum(5, 0, 0)))
      {
        boolean inTransactionOnServer = (this.serverStatus & 0x1) != 0;
        
        return !inTransactionOnServer;
      }
      return autoCommitModeOnServer != autoCommitFlag;
    }
    return true;
  }
  
  protected boolean inTransactionOnServer()
  {
    return (this.serverStatus & 0x1) != 0;
  }
  
  protected void changeUser(String userName, String password, String database)
    throws SQLException
  {
    this.packetSequence = -1;
    
    int passwordLength = 16;
    int userLength = userName != null ? userName.length() : 0;
    int databaseLength = database != null ? database.length() : 0;
    
    int packLength = (userLength + passwordLength + databaseLength) * 2 + 7 + 4 + 33;
    if ((this.serverCapabilities & 0x8000) != 0)
    {
      Buffer changeUserPacket = new Buffer(packLength + 1);
      changeUserPacket.writeByte((byte)17);
      if (versionMeetsMinimum(4, 1, 1)) {
        secureAuth411(changeUserPacket, packLength, userName, password, database, false);
      } else {
        secureAuth(changeUserPacket, packLength, userName, password, database, false);
      }
    }
    else
    {
      Buffer packet = new Buffer(packLength);
      packet.writeByte((byte)17);
      
      packet.writeString(userName);
      if (this.protocolVersion > 9) {
        packet.writeString(Util.newCrypt(password, this.seed));
      } else {
        packet.writeString(Util.oldCrypt(password, this.seed));
      }
      boolean localUseConnectWithDb = (this.useConnectWithDb) && (database != null) && (database.length() > 0);
      if (localUseConnectWithDb) {
        packet.writeString(database);
      }
      send(packet, packet.getPosition());
      checkErrorPacket();
      if (!localUseConnectWithDb) {
        changeDatabaseTo(database);
      }
    }
  }
  
  protected Buffer checkErrorPacket()
    throws SQLException
  {
    return checkErrorPacket(-1);
  }
  
  protected void checkForCharsetMismatch()
  {
    if ((this.connection.getUseUnicode()) && (this.connection.getEncoding() != null))
    {
      String encodingToCheck = jvmPlatformCharset;
      if (encodingToCheck == null) {
        encodingToCheck = System.getProperty("file.encoding");
      }
      if (encodingToCheck == null) {
        this.platformDbCharsetMatches = false;
      } else {
        this.platformDbCharsetMatches = encodingToCheck.equals(this.connection.getEncoding());
      }
    }
  }
  
  protected void clearInputStream()
    throws SQLException
  {
    try
    {
      int len = this.mysqlInput.available();
      while (len > 0)
      {
        this.mysqlInput.skip(len);
        len = this.mysqlInput.available();
      }
    }
    catch (IOException ioEx)
    {
      throw SQLError.createCommunicationsException(this.connection, this.lastPacketSentTimeMs, this.lastPacketReceivedTimeMs, ioEx, getExceptionInterceptor());
    }
  }
  
  protected void resetReadPacketSequence()
  {
    this.readPacketSequence = 0;
  }
  
  protected void dumpPacketRingBuffer()
    throws SQLException
  {
    if ((this.packetDebugRingBuffer != null) && (this.connection.getEnablePacketDebug()))
    {
      StringBuffer dumpBuffer = new StringBuffer();
      
      dumpBuffer.append("Last " + this.packetDebugRingBuffer.size() + " packets received from server, from oldest->newest:\n");
      
      dumpBuffer.append("\n");
      
      Iterator ringBufIter = this.packetDebugRingBuffer.iterator();
      while (ringBufIter.hasNext())
      {
        dumpBuffer.append((StringBuffer)ringBufIter.next());
        dumpBuffer.append("\n");
      }
      this.connection.getLog().logTrace(dumpBuffer.toString());
    }
  }
  
  protected void explainSlowQuery(byte[] querySQL, String truncatedQuery)
    throws SQLException
  {
    if (StringUtils.startsWithIgnoreCaseAndWs(truncatedQuery, "SELECT"))
    {
      PreparedStatement stmt = null;
      ResultSet rs = null;
      try
      {
        stmt = (PreparedStatement)this.connection.clientPrepareStatement("EXPLAIN ?");
        stmt.setBytesNoEscapeNoQuotes(1, querySQL);
        rs = stmt.executeQuery();
        
        StringBuffer explainResults = new StringBuffer(Messages.getString("MysqlIO.8") + truncatedQuery + Messages.getString("MysqlIO.9"));
        
        ResultSetUtil.appendResultSetSlashGStyle(explainResults, rs);
        
        this.connection.getLog().logWarn(explainResults.toString());
      }
      catch (SQLException sqlEx) {}finally
      {
        if (rs != null) {
          rs.close();
        }
        if (stmt != null) {
          stmt.close();
        }
      }
    }
  }
  
  static int getMaxBuf()
  {
    return maxBufferSize;
  }
  
  final int getServerMajorVersion()
  {
    return this.serverMajorVersion;
  }
  
  final int getServerMinorVersion()
  {
    return this.serverMinorVersion;
  }
  
  final int getServerSubMinorVersion()
  {
    return this.serverSubMinorVersion;
  }
  
  String getServerVersion()
  {
    return this.serverVersion;
  }
  
  void doHandshake(String user, String password, String database)
    throws SQLException
  {
    this.checkPacketSequence = false;
    this.readPacketSequence = 0;
    
    Buffer buf = readPacket();
    
    this.protocolVersion = buf.readByte();
    if (this.protocolVersion == -1)
    {
      try
      {
        this.mysqlConnection.close();
      }
      catch (Exception e) {}
      int errno = 2000;
      
      errno = buf.readInt();
      
      String serverErrorMessage = buf.readString("ASCII", getExceptionInterceptor());
      
      StringBuffer errorBuf = new StringBuffer(Messages.getString("MysqlIO.10"));
      
      errorBuf.append(serverErrorMessage);
      errorBuf.append("\"");
      
      String xOpen = SQLError.mysqlToSqlState(errno, this.connection.getUseSqlStateCodes());
      
      throw SQLError.createSQLException(SQLError.get(xOpen) + ", " + errorBuf.toString(), xOpen, errno, getExceptionInterceptor());
    }
    this.serverVersion = buf.readString("ASCII", getExceptionInterceptor());
    
    int point = this.serverVersion.indexOf('.');
    if (point != -1)
    {
      try
      {
        int n = Integer.parseInt(this.serverVersion.substring(0, point));
        this.serverMajorVersion = n;
      }
      catch (NumberFormatException NFE1) {}
      String remaining = this.serverVersion.substring(point + 1, this.serverVersion.length());
      
      point = remaining.indexOf('.');
      if (point != -1)
      {
        try
        {
          int n = Integer.parseInt(remaining.substring(0, point));
          this.serverMinorVersion = n;
        }
        catch (NumberFormatException nfe) {}
        remaining = remaining.substring(point + 1, remaining.length());
        
        int pos = 0;
        while ((pos < remaining.length()) && 
          (remaining.charAt(pos) >= '0') && (remaining.charAt(pos) <= '9')) {
          pos++;
        }
        try
        {
          int n = Integer.parseInt(remaining.substring(0, pos));
          this.serverSubMinorVersion = n;
        }
        catch (NumberFormatException nfe) {}
      }
    }
    if (versionMeetsMinimum(4, 0, 8))
    {
      this.maxThreeBytes = 16777215;
      this.useNewLargePackets = true;
    }
    else
    {
      this.maxThreeBytes = 16581375;
      this.useNewLargePackets = false;
    }
    this.colDecimalNeedsBump = versionMeetsMinimum(3, 23, 0);
    this.colDecimalNeedsBump = (!versionMeetsMinimum(3, 23, 15));
    this.useNewUpdateCounts = versionMeetsMinimum(3, 22, 5);
    
    this.threadId = buf.readLong();
    this.seed = buf.readString("ASCII", getExceptionInterceptor());
    
    this.serverCapabilities = 0;
    if (buf.getPosition() < buf.getBufLength()) {
      this.serverCapabilities = buf.readInt();
    }
    if (versionMeetsMinimum(4, 1, 1))
    {
      int position = buf.getPosition();
      
      this.serverCharsetIndex = (buf.readByte() & 0xFF);
      this.serverStatus = buf.readInt();
      checkTransactionState(0);
      buf.setPosition(position + 16);
      
      String seedPart2 = buf.readString("ASCII", getExceptionInterceptor());
      StringBuffer newSeed = new StringBuffer(20);
      newSeed.append(this.seed);
      newSeed.append(seedPart2);
      this.seed = newSeed.toString();
    }
    if (((this.serverCapabilities & 0x20) != 0) && (this.connection.getUseCompression())) {
      this.clientParam |= 0x20;
    }
    this.useConnectWithDb = ((database != null) && (database.length() > 0) && (!this.connection.getCreateDatabaseIfNotExist()));
    if (this.useConnectWithDb) {
      this.clientParam |= 0x8;
    }
    if (((this.serverCapabilities & 0x800) == 0) && (this.connection.getUseSSL()))
    {
      if (this.connection.getRequireSSL())
      {
        this.connection.close();
        forceClose();
        throw SQLError.createSQLException(Messages.getString("MysqlIO.15"), "08001", getExceptionInterceptor());
      }
      this.connection.setUseSSL(false);
    }
    if ((this.serverCapabilities & 0x4) != 0)
    {
      this.clientParam |= 0x4;
      this.hasLongColumnInfo = true;
    }
    if (!this.connection.getUseAffectedRows()) {
      this.clientParam |= 0x2;
    }
    if (this.connection.getAllowLoadLocalInfile()) {
      this.clientParam |= 0x80;
    }
    if (this.isInteractiveClient) {
      this.clientParam |= 0x400;
    }
    if (this.protocolVersion > 9) {
      this.clientParam |= 1L;
    } else {
      this.clientParam &= 0xFFFFFFFFFFFFFFFE;
    }
    if (versionMeetsMinimum(4, 1, 0))
    {
      if (versionMeetsMinimum(4, 1, 1))
      {
        this.clientParam |= 0x200;
        this.has41NewNewProt = true;
        
        this.clientParam |= 0x2000;
        
        this.clientParam |= 0x20000;
        if (this.connection.getAllowMultiQueries()) {
          this.clientParam |= 0x10000;
        }
      }
      else
      {
        this.clientParam |= 0x4000;
        this.has41NewNewProt = false;
      }
      this.use41Extensions = true;
    }
    int passwordLength = 16;
    int userLength = user != null ? user.length() : 0;
    int databaseLength = database != null ? database.length() : 0;
    
    int packLength = (userLength + passwordLength + databaseLength) * 2 + 7 + 4 + 33;
    
    Buffer packet = null;
    if (!this.connection.getUseSSL())
    {
      if ((this.serverCapabilities & 0x8000) != 0)
      {
        this.clientParam |= 0x8000;
        if (versionMeetsMinimum(4, 1, 1)) {
          secureAuth411(null, packLength, user, password, database, true);
        } else {
          secureAuth(null, packLength, user, password, database, true);
        }
      }
      else
      {
        packet = new Buffer(packLength);
        if ((this.clientParam & 0x4000) != 0L)
        {
          if (versionMeetsMinimum(4, 1, 1))
          {
            packet.writeLong(this.clientParam);
            packet.writeLong(this.maxThreeBytes);
            
            packet.writeByte((byte)8);
            
            packet.writeBytesNoNull(new byte[23]);
          }
          else
          {
            packet.writeLong(this.clientParam);
            packet.writeLong(this.maxThreeBytes);
          }
        }
        else
        {
          packet.writeInt((int)this.clientParam);
          packet.writeLongInt(this.maxThreeBytes);
        }
        packet.writeString(user, "Cp1252", this.connection);
        if (this.protocolVersion > 9) {
          packet.writeString(Util.newCrypt(password, this.seed), "Cp1252", this.connection);
        } else {
          packet.writeString(Util.oldCrypt(password, this.seed), "Cp1252", this.connection);
        }
        if (this.useConnectWithDb) {
          packet.writeString(database, "Cp1252", this.connection);
        }
        send(packet, packet.getPosition());
      }
    }
    else {
      negotiateSSLConnection(user, password, database, packLength);
    }
    if (!versionMeetsMinimum(4, 1, 1)) {
      checkErrorPacket();
    }
    if (((this.serverCapabilities & 0x20) != 0) && (this.connection.getUseCompression()))
    {
      this.deflater = new Deflater();
      this.useCompression = true;
      this.mysqlInput = new CompressedInputStream(this.connection, this.mysqlInput);
    }
    if (!this.useConnectWithDb) {
      changeDatabaseTo(database);
    }
    try
    {
      this.mysqlConnection = this.socketFactory.afterHandshake();
    }
    catch (IOException ioEx)
    {
      throw SQLError.createCommunicationsException(this.connection, this.lastPacketSentTimeMs, this.lastPacketReceivedTimeMs, ioEx, getExceptionInterceptor());
    }
  }
  
  private void changeDatabaseTo(String database)
    throws SQLException
  {
    if ((database == null) || (database.length() == 0)) {
      return;
    }
    try
    {
      sendCommand(2, database, null, false, null, 0);
    }
    catch (Exception ex)
    {
      if (this.connection.getCreateDatabaseIfNotExist())
      {
        sendCommand(3, "CREATE DATABASE IF NOT EXISTS " + database, null, false, null, 0);
        
        sendCommand(2, database, null, false, null, 0);
      }
      else
      {
        throw SQLError.createCommunicationsException(this.connection, this.lastPacketSentTimeMs, this.lastPacketReceivedTimeMs, ex, getExceptionInterceptor());
      }
    }
  }
  
  final ResultSetRow nextRow(Field[] fields, int columnCount, boolean isBinaryEncoded, int resultSetConcurrency, boolean useBufferRowIfPossible, boolean useBufferRowExplicit, boolean canReuseRowPacketForBufferRow, Buffer existingRowPacket)
    throws SQLException
  {
    if ((this.useDirectRowUnpack) && (existingRowPacket == null) && (!isBinaryEncoded) && (!useBufferRowIfPossible) && (!useBufferRowExplicit)) {
      return nextRowFast(fields, columnCount, isBinaryEncoded, resultSetConcurrency, useBufferRowIfPossible, useBufferRowExplicit, canReuseRowPacketForBufferRow);
    }
    Buffer rowPacket = null;
    if (existingRowPacket == null)
    {
      rowPacket = checkErrorPacket();
      if ((!useBufferRowExplicit) && (useBufferRowIfPossible) && 
        (rowPacket.getBufLength() > this.useBufferRowSizeThreshold)) {
        useBufferRowExplicit = true;
      }
    }
    else
    {
      rowPacket = existingRowPacket;
      checkErrorPacket(existingRowPacket);
    }
    if (!isBinaryEncoded)
    {
      rowPacket.setPosition(rowPacket.getPosition() - 1);
      if (!rowPacket.isLastDataPacket())
      {
        if ((resultSetConcurrency == 1008) || ((!useBufferRowIfPossible) && (!useBufferRowExplicit)))
        {
          byte[][] rowData = new byte[columnCount][];
          for (int i = 0; i < columnCount; i++) {
            rowData[i] = rowPacket.readLenByteArray(0);
          }
          return new ByteArrayRow(rowData, getExceptionInterceptor());
        }
        if (!canReuseRowPacketForBufferRow) {
          this.reusablePacket = new Buffer(rowPacket.getBufLength());
        }
        return new BufferRow(rowPacket, fields, false, getExceptionInterceptor());
      }
      readServerStatusForResultSets(rowPacket);
      
      return null;
    }
    if (!rowPacket.isLastDataPacket())
    {
      if ((resultSetConcurrency == 1008) || ((!useBufferRowIfPossible) && (!useBufferRowExplicit))) {
        return unpackBinaryResultSetRow(fields, rowPacket, resultSetConcurrency);
      }
      if (!canReuseRowPacketForBufferRow) {
        this.reusablePacket = new Buffer(rowPacket.getBufLength());
      }
      return new BufferRow(rowPacket, fields, true, getExceptionInterceptor());
    }
    rowPacket.setPosition(rowPacket.getPosition() - 1);
    readServerStatusForResultSets(rowPacket);
    
    return null;
  }
  
  final ResultSetRow nextRowFast(Field[] fields, int columnCount, boolean isBinaryEncoded, int resultSetConcurrency, boolean useBufferRowIfPossible, boolean useBufferRowExplicit, boolean canReuseRowPacket)
    throws SQLException
  {
    try
    {
      int lengthRead = readFully(this.mysqlInput, this.packetHeaderBuf, 0, 4);
      if (lengthRead < 4)
      {
        forceClose();
        throw new RuntimeException(Messages.getString("MysqlIO.43"));
      }
      int packetLength = (this.packetHeaderBuf[0] & 0xFF) + ((this.packetHeaderBuf[1] & 0xFF) << 8) + ((this.packetHeaderBuf[2] & 0xFF) << 16);
      if (packetLength == this.maxThreeBytes)
      {
        reuseAndReadPacket(this.reusablePacket, packetLength);
        
        return nextRow(fields, columnCount, isBinaryEncoded, resultSetConcurrency, useBufferRowIfPossible, useBufferRowExplicit, canReuseRowPacket, this.reusablePacket);
      }
      if (packetLength > this.useBufferRowSizeThreshold)
      {
        reuseAndReadPacket(this.reusablePacket, packetLength);
        
        return nextRow(fields, columnCount, isBinaryEncoded, resultSetConcurrency, true, true, false, this.reusablePacket);
      }
      int remaining = packetLength;
      
      boolean firstTime = true;
      
      byte[][] rowData = (byte[][])null;
      for (int i = 0; i < columnCount; i++)
      {
        int sw = this.mysqlInput.read() & 0xFF;
        remaining--;
        if (firstTime)
        {
          if (sw == 255)
          {
            Buffer errorPacket = new Buffer(packetLength + 4);
            errorPacket.setPosition(0);
            errorPacket.writeByte(this.packetHeaderBuf[0]);
            errorPacket.writeByte(this.packetHeaderBuf[1]);
            errorPacket.writeByte(this.packetHeaderBuf[2]);
            errorPacket.writeByte((byte)1);
            errorPacket.writeByte((byte)sw);
            readFully(this.mysqlInput, errorPacket.getByteBuffer(), 5, packetLength - 1);
            errorPacket.setPosition(4);
            checkErrorPacket(errorPacket);
          }
          if ((sw == 254) && (packetLength < 9))
          {
            if (this.use41Extensions)
            {
              this.warningCount = (this.mysqlInput.read() & 0xFF | (this.mysqlInput.read() & 0xFF) << 8);
              
              remaining -= 2;
              if (this.warningCount > 0) {
                this.hadWarnings = true;
              }
              this.oldServerStatus = this.serverStatus;
              
              this.serverStatus = (this.mysqlInput.read() & 0xFF | (this.mysqlInput.read() & 0xFF) << 8);
              
              checkTransactionState(this.oldServerStatus);
              
              remaining -= 2;
              if (remaining > 0) {
                skipFully(this.mysqlInput, remaining);
              }
            }
            return null;
          }
          rowData = new byte[columnCount][];
          
          firstTime = false;
        }
        int len = 0;
        switch (sw)
        {
        case 251: 
          len = -1;
          break;
        case 252: 
          len = this.mysqlInput.read() & 0xFF | (this.mysqlInput.read() & 0xFF) << 8;
          
          remaining -= 2;
          break;
        case 253: 
          len = this.mysqlInput.read() & 0xFF | (this.mysqlInput.read() & 0xFF) << 8 | (this.mysqlInput.read() & 0xFF) << 16;
          
          remaining -= 3;
          break;
        case 254: 
          len = (int)(this.mysqlInput.read() & 0xFF | (this.mysqlInput.read() & 0xFF) << 8 | (this.mysqlInput.read() & 0xFF) << 16 | (this.mysqlInput.read() & 0xFF) << 24 | (this.mysqlInput.read() & 0xFF) << 32 | (this.mysqlInput.read() & 0xFF) << 40 | (this.mysqlInput.read() & 0xFF) << 48 | (this.mysqlInput.read() & 0xFF) << 56);
          
          remaining -= 8;
          break;
        default: 
          len = sw;
        }
        if (len == -1)
        {
          rowData[i] = null;
        }
        else if (len == 0)
        {
          rowData[i] = Constants.EMPTY_BYTE_ARRAY;
        }
        else
        {
          rowData[i] = new byte[len];
          
          int bytesRead = readFully(this.mysqlInput, rowData[i], 0, len);
          if (bytesRead != len) {
            throw SQLError.createCommunicationsException(this.connection, this.lastPacketSentTimeMs, this.lastPacketReceivedTimeMs, new IOException(Messages.getString("MysqlIO.43")), getExceptionInterceptor());
          }
          remaining -= bytesRead;
        }
      }
      if (remaining > 0) {
        skipFully(this.mysqlInput, remaining);
      }
      return new ByteArrayRow(rowData, getExceptionInterceptor());
    }
    catch (IOException ioEx)
    {
      throw SQLError.createCommunicationsException(this.connection, this.lastPacketSentTimeMs, this.lastPacketReceivedTimeMs, ioEx, getExceptionInterceptor());
    }
  }
  
  final void quit()
    throws SQLException
  {
    Buffer packet = new Buffer(6);
    this.packetSequence = -1;
    packet.writeByte((byte)1);
    send(packet, packet.getPosition());
    forceClose();
  }
  
  Buffer getSharedSendPacket()
  {
    if (this.sharedSendPacket == null) {
      this.sharedSendPacket = new Buffer(1024);
    }
    return this.sharedSendPacket;
  }
  
  void closeStreamer(RowData streamer)
    throws SQLException
  {
    if (this.streamingData == null) {
      throw SQLError.createSQLException(Messages.getString("MysqlIO.17") + streamer + Messages.getString("MysqlIO.18"), getExceptionInterceptor());
    }
    if (streamer != this.streamingData) {
      throw SQLError.createSQLException(Messages.getString("MysqlIO.19") + streamer + Messages.getString("MysqlIO.20") + Messages.getString("MysqlIO.21") + Messages.getString("MysqlIO.22"), getExceptionInterceptor());
    }
    this.streamingData = null;
  }
  
  boolean tackOnMoreStreamingResults(ResultSetImpl addingTo)
    throws SQLException
  {
    if ((this.serverStatus & 0x8) != 0)
    {
      boolean moreRowSetsExist = true;
      ResultSetImpl currentResultSet = addingTo;
      boolean firstTime = true;
      while ((moreRowSetsExist) && (
        (firstTime) || (!currentResultSet.reallyResult())))
      {
        firstTime = false;
        
        Buffer fieldPacket = checkErrorPacket();
        fieldPacket.setPosition(0);
        
        java.sql.Statement owningStatement = addingTo.getStatement();
        
        int maxRows = owningStatement.getMaxRows();
        
        ResultSetImpl newResultSet = readResultsForQueryOrUpdate((StatementImpl)owningStatement, maxRows, owningStatement.getResultSetType(), owningStatement.getResultSetConcurrency(), true, owningStatement.getConnection().getCatalog(), fieldPacket, addingTo.isBinaryEncoded, -1L, null);
        
        currentResultSet.setNextResultSet(newResultSet);
        
        currentResultSet = newResultSet;
        
        moreRowSetsExist = (this.serverStatus & 0x8) != 0;
        if ((!currentResultSet.reallyResult()) && (!moreRowSetsExist)) {
          return false;
        }
      }
      return true;
    }
    return false;
  }
  
  ResultSetImpl readAllResults(StatementImpl callingStatement, int maxRows, int resultSetType, int resultSetConcurrency, boolean streamResults, String catalog, Buffer resultPacket, boolean isBinaryEncoded, long preSentColumnCount, Field[] metadataFromCache)
    throws SQLException
  {
    resultPacket.setPosition(resultPacket.getPosition() - 1);
    
    ResultSetImpl topLevelResultSet = readResultsForQueryOrUpdate(callingStatement, maxRows, resultSetType, resultSetConcurrency, streamResults, catalog, resultPacket, isBinaryEncoded, preSentColumnCount, metadataFromCache);
    
    ResultSetImpl currentResultSet = topLevelResultSet;
    
    boolean checkForMoreResults = (this.clientParam & 0x20000) != 0L;
    
    boolean serverHasMoreResults = (this.serverStatus & 0x8) != 0;
    if ((serverHasMoreResults) && (streamResults))
    {
      if (topLevelResultSet.getUpdateCount() != -1L) {
        tackOnMoreStreamingResults(topLevelResultSet);
      }
      reclaimLargeReusablePacket();
      
      return topLevelResultSet;
    }
    boolean moreRowSetsExist = checkForMoreResults & serverHasMoreResults;
    while (moreRowSetsExist)
    {
      Buffer fieldPacket = checkErrorPacket();
      fieldPacket.setPosition(0);
      
      ResultSetImpl newResultSet = readResultsForQueryOrUpdate(callingStatement, maxRows, resultSetType, resultSetConcurrency, streamResults, catalog, fieldPacket, isBinaryEncoded, preSentColumnCount, metadataFromCache);
      
      currentResultSet.setNextResultSet(newResultSet);
      
      currentResultSet = newResultSet;
      
      moreRowSetsExist = (this.serverStatus & 0x8) != 0;
    }
    if (!streamResults) {
      clearInputStream();
    }
    reclaimLargeReusablePacket();
    
    return topLevelResultSet;
  }
  
  void resetMaxBuf()
  {
    this.maxAllowedPacket = this.connection.getMaxAllowedPacket();
  }
  
  final Buffer sendCommand(int command, String extraData, Buffer queryPacket, boolean skipCheck, String extraDataCharEncoding, int timeoutMillis)
    throws SQLException
  {
    this.commandCount += 1;
    
    this.enablePacketDebug = this.connection.getEnablePacketDebug();
    this.readPacketSequence = 0;
    
    int oldTimeout = 0;
    if (timeoutMillis != 0) {
      try
      {
        oldTimeout = this.mysqlConnection.getSoTimeout();
        this.mysqlConnection.setSoTimeout(timeoutMillis);
      }
      catch (SocketException e)
      {
        throw SQLError.createCommunicationsException(this.connection, this.lastPacketSentTimeMs, this.lastPacketReceivedTimeMs, e, getExceptionInterceptor());
      }
    }
    try
    {
      checkForOutstandingStreamingData();
      
      this.oldServerStatus = this.serverStatus;
      this.serverStatus = 0;
      this.hadWarnings = false;
      this.warningCount = 0;
      
      this.queryNoIndexUsed = false;
      this.queryBadIndexUsed = false;
      this.serverQueryWasSlow = false;
      if (this.useCompression)
      {
        int bytesLeft = this.mysqlInput.available();
        if (bytesLeft > 0) {
          this.mysqlInput.skip(bytesLeft);
        }
      }
      long id;
      try
      {
        clearInputStream();
        if (queryPacket == null)
        {
          int packLength = 8 + (extraData != null ? extraData.length() : 0) + 2;
          if (this.sendPacket == null) {
            this.sendPacket = new Buffer(packLength);
          }
          this.packetSequence = -1;
          this.readPacketSequence = 0;
          this.checkPacketSequence = true;
          this.sendPacket.clear();
          
          this.sendPacket.writeByte((byte)command);
          if ((command == 2) || (command == 5) || (command == 6) || (command == 3) || (command == 22))
          {
            if (extraDataCharEncoding == null) {
              this.sendPacket.writeStringNoNull(extraData);
            } else {
              this.sendPacket.writeStringNoNull(extraData, extraDataCharEncoding, this.connection.getServerCharacterEncoding(), this.connection.parserKnowsUnicode(), this.connection);
            }
          }
          else if (command == 12)
          {
            id = Long.parseLong(extraData);
            this.sendPacket.writeLong(id);
          }
          send(this.sendPacket, this.sendPacket.getPosition());
        }
        else
        {
          this.packetSequence = -1;
          send(queryPacket, queryPacket.getPosition());
        }
      }
      catch (SQLException sqlEx)
      {
        throw sqlEx;
      }
      catch (Exception ex)
      {
        throw SQLError.createCommunicationsException(this.connection, this.lastPacketSentTimeMs, this.lastPacketReceivedTimeMs, ex, getExceptionInterceptor());
      }
      Buffer returnPacket = null;
      if (!skipCheck)
      {
        if ((command == 23) || (command == 26))
        {
          this.readPacketSequence = 0;
          this.packetSequenceReset = true;
        }
        returnPacket = checkErrorPacket(command);
      }
      return returnPacket;
    }
    catch (IOException ioEx)
    {
      throw SQLError.createCommunicationsException(this.connection, this.lastPacketSentTimeMs, this.lastPacketReceivedTimeMs, ioEx, getExceptionInterceptor());
    }
    finally
    {
      if (timeoutMillis != 0) {
        try
        {
          this.mysqlConnection.setSoTimeout(oldTimeout);
        }
        catch (SocketException e)
        {
          throw SQLError.createCommunicationsException(this.connection, this.lastPacketSentTimeMs, this.lastPacketReceivedTimeMs, e, getExceptionInterceptor());
        }
      }
    }
  }
  
  private int statementExecutionDepth = 0;
  private boolean useAutoSlowLog;
  
  final ResultSetInternalMethods sqlQueryDirect(StatementImpl callingStatement, String query, String characterEncoding, Buffer queryPacket, int maxRows, int resultSetType, int resultSetConcurrency, boolean streamResults, String catalog, Field[] cachedMetadata)
    throws Exception
  {
    this.statementExecutionDepth += 1;
    try
    {
      if (this.statementInterceptors != null)
      {
        ResultSetInternalMethods interceptedResults = invokeStatementInterceptorsPre(query, callingStatement);
        if (interceptedResults != null) {
          return interceptedResults;
        }
      }
      long queryStartTime = 0L;
      long queryEndTime = 0L;
      if (query != null)
      {
        int packLength = 5 + query.length() * 2 + 2;
        
        String statementComment = this.connection.getStatementComment();
        
        byte[] commentAsBytes = null;
        if (statementComment != null)
        {
          commentAsBytes = StringUtils.getBytes(statementComment, null, characterEncoding, this.connection.getServerCharacterEncoding(), this.connection.parserKnowsUnicode(), getExceptionInterceptor());
          
          packLength += commentAsBytes.length;
          packLength += 6;
        }
        if (this.sendPacket == null) {
          this.sendPacket = new Buffer(packLength);
        } else {
          this.sendPacket.clear();
        }
        this.sendPacket.writeByte((byte)3);
        if (commentAsBytes != null)
        {
          this.sendPacket.writeBytesNoNull(Constants.SLASH_STAR_SPACE_AS_BYTES);
          this.sendPacket.writeBytesNoNull(commentAsBytes);
          this.sendPacket.writeBytesNoNull(Constants.SPACE_STAR_SLASH_SPACE_AS_BYTES);
        }
        if (characterEncoding != null)
        {
          if (this.platformDbCharsetMatches) {
            this.sendPacket.writeStringNoNull(query, characterEncoding, this.connection.getServerCharacterEncoding(), this.connection.parserKnowsUnicode(), this.connection);
          } else if (StringUtils.startsWithIgnoreCaseAndWs(query, "LOAD DATA")) {
            this.sendPacket.writeBytesNoNull(query.getBytes());
          } else {
            this.sendPacket.writeStringNoNull(query, characterEncoding, this.connection.getServerCharacterEncoding(), this.connection.parserKnowsUnicode(), this.connection);
          }
        }
        else {
          this.sendPacket.writeStringNoNull(query);
        }
        queryPacket = this.sendPacket;
      }
      byte[] queryBuf = null;
      int oldPacketPosition = 0;
      if (this.needToGrabQueryFromPacket)
      {
        queryBuf = queryPacket.getByteBuffer();
        
        oldPacketPosition = queryPacket.getPosition();
        
        queryStartTime = getCurrentTimeNanosOrMillis();
      }
      if (this.autoGenerateTestcaseScript)
      {
        String testcaseQuery = null;
        if (query != null) {
          testcaseQuery = query;
        } else {
          testcaseQuery = new String(queryBuf, 5, oldPacketPosition - 5);
        }
        StringBuffer debugBuf = new StringBuffer(testcaseQuery.length() + 32);
        this.connection.generateConnectionCommentBlock(debugBuf);
        debugBuf.append(testcaseQuery);
        debugBuf.append(';');
        this.connection.dumpTestcaseQuery(debugBuf.toString());
      }
      Buffer resultPacket = sendCommand(3, null, queryPacket, false, null, 0);
      
      long fetchBeginTime = 0L;
      long fetchEndTime = 0L;
      
      String profileQueryToLog = null;
      
      boolean queryWasSlow = false;
      if ((this.profileSql) || (this.logSlowQueries))
      {
        queryEndTime = System.currentTimeMillis();
        
        boolean shouldExtractQuery = false;
        if (this.profileSql)
        {
          shouldExtractQuery = true;
        }
        else if (this.logSlowQueries)
        {
          long queryTime = queryEndTime - queryStartTime;
          
          boolean logSlow = false;
          if (this.useAutoSlowLog)
          {
            logSlow = queryTime > this.connection.getSlowQueryThresholdMillis();
          }
          else
          {
            logSlow = this.connection.isAbonormallyLongQuery(queryTime);
            
            this.connection.reportQueryTime(queryTime);
          }
          if (logSlow)
          {
            shouldExtractQuery = true;
            queryWasSlow = true;
          }
        }
        if (shouldExtractQuery)
        {
          boolean truncated = false;
          
          int extractPosition = oldPacketPosition;
          if (oldPacketPosition > this.connection.getMaxQuerySizeToLog())
          {
            extractPosition = this.connection.getMaxQuerySizeToLog() + 5;
            truncated = true;
          }
          profileQueryToLog = new String(queryBuf, 5, extractPosition - 5);
          if (truncated) {
            profileQueryToLog = profileQueryToLog + Messages.getString("MysqlIO.25");
          }
        }
        fetchBeginTime = queryEndTime;
      }
      ResultSetInternalMethods rs = readAllResults(callingStatement, maxRows, resultSetType, resultSetConcurrency, streamResults, catalog, resultPacket, false, -1L, cachedMetadata);
      if ((queryWasSlow) && (!this.serverQueryWasSlow))
      {
        StringBuffer mesgBuf = new StringBuffer(48 + profileQueryToLog.length());
        
        mesgBuf.append(Messages.getString("MysqlIO.SlowQuery", new Object[] { new Long(this.slowQueryThreshold), this.queryTimingUnits, new Long(queryEndTime - queryStartTime) }));
        
        mesgBuf.append(profileQueryToLog);
        
        ProfilerEventHandler eventSink = ProfilerEventHandlerFactory.getInstance(this.connection);
        
        eventSink.consumeEvent(new ProfilerEvent((byte)6, "", catalog, this.connection.getId(), callingStatement != null ? callingStatement.getId() : 999, ((ResultSetImpl)rs).resultId, System.currentTimeMillis(), (int)(queryEndTime - queryStartTime), this.queryTimingUnits, null, new Throwable(), mesgBuf.toString()));
        if (this.connection.getExplainSlowQueries()) {
          if (oldPacketPosition < 1048576) {
            explainSlowQuery(queryPacket.getBytes(5, oldPacketPosition - 5), profileQueryToLog);
          } else {
            this.connection.getLog().logWarn(Messages.getString("MysqlIO.28") + 1048576 + Messages.getString("MysqlIO.29"));
          }
        }
      }
      if (this.logSlowQueries)
      {
        ProfilerEventHandler eventSink = ProfilerEventHandlerFactory.getInstance(this.connection);
        if (this.queryBadIndexUsed) {
          eventSink.consumeEvent(new ProfilerEvent((byte)6, "", catalog, this.connection.getId(), callingStatement != null ? callingStatement.getId() : 999, ((ResultSetImpl)rs).resultId, System.currentTimeMillis(), queryEndTime - queryStartTime, this.queryTimingUnits, null, new Throwable(), Messages.getString("MysqlIO.33") + profileQueryToLog));
        }
        if (this.queryNoIndexUsed) {
          eventSink.consumeEvent(new ProfilerEvent((byte)6, "", catalog, this.connection.getId(), callingStatement != null ? callingStatement.getId() : 999, ((ResultSetImpl)rs).resultId, System.currentTimeMillis(), queryEndTime - queryStartTime, this.queryTimingUnits, null, new Throwable(), Messages.getString("MysqlIO.35") + profileQueryToLog));
        }
        if (this.serverQueryWasSlow) {
          eventSink.consumeEvent(new ProfilerEvent((byte)6, "", catalog, this.connection.getId(), callingStatement != null ? callingStatement.getId() : 999, ((ResultSetImpl)rs).resultId, System.currentTimeMillis(), queryEndTime - queryStartTime, this.queryTimingUnits, null, new Throwable(), Messages.getString("MysqlIO.ServerSlowQuery") + profileQueryToLog));
        }
      }
      if (this.profileSql)
      {
        fetchEndTime = getCurrentTimeNanosOrMillis();
        
        ProfilerEventHandler eventSink = ProfilerEventHandlerFactory.getInstance(this.connection);
        
        eventSink.consumeEvent(new ProfilerEvent((byte)3, "", catalog, this.connection.getId(), callingStatement != null ? callingStatement.getId() : 999, ((ResultSetImpl)rs).resultId, System.currentTimeMillis(), queryEndTime - queryStartTime, this.queryTimingUnits, null, new Throwable(), profileQueryToLog));
        
        eventSink.consumeEvent(new ProfilerEvent((byte)5, "", catalog, this.connection.getId(), callingStatement != null ? callingStatement.getId() : 999, ((ResultSetImpl)rs).resultId, System.currentTimeMillis(), fetchEndTime - fetchBeginTime, this.queryTimingUnits, null, new Throwable(), null));
      }
      if (this.hadWarnings) {
        scanForAndThrowDataTruncation();
      }
      ResultSetInternalMethods interceptedResults;
      if (this.statementInterceptors != null)
      {
        interceptedResults = invokeStatementInterceptorsPost(query, callingStatement, rs);
        if (interceptedResults != null) {
          rs = interceptedResults;
        }
      }
      return rs;
    }
    catch (SQLException sqlEx)
    {
      if (callingStatement != null) {
        synchronized (callingStatement.cancelTimeoutMutex)
        {
          if (callingStatement.wasCancelled)
          {
            SQLException cause = null;
            if (callingStatement.wasCancelledByTimeout) {
              cause = new MySQLTimeoutException();
            } else {
              cause = new MySQLStatementCancelledException();
            }
            callingStatement.resetCancelledState();
            
            throw cause;
          }
        }
      }
      throw sqlEx;
    }
    finally
    {
      this.statementExecutionDepth -= 1;
    }
  }
  
  private ResultSetInternalMethods invokeStatementInterceptorsPre(String sql, Statement interceptedStatement)
    throws SQLException
  {
    ResultSetInternalMethods previousResultSet = null;
    
    Iterator interceptors = this.statementInterceptors.iterator();
    while (interceptors.hasNext())
    {
      StatementInterceptor interceptor = (StatementInterceptor)interceptors.next();
      
      boolean executeTopLevelOnly = interceptor.executeTopLevelOnly();
      boolean shouldExecute = ((executeTopLevelOnly) && (this.statementExecutionDepth == 1)) || (!executeTopLevelOnly);
      if (shouldExecute)
      {
        String sqlToInterceptor = sql;
        if ((interceptedStatement instanceof PreparedStatement)) {
          sqlToInterceptor = ((PreparedStatement)interceptedStatement).asSql();
        }
        ResultSetInternalMethods interceptedResultSet = interceptor.preProcess(sqlToInterceptor, interceptedStatement, this.connection);
        if (interceptedResultSet != null) {
          previousResultSet = interceptedResultSet;
        }
      }
    }
    return previousResultSet;
  }
  
  private ResultSetInternalMethods invokeStatementInterceptorsPost(String sql, Statement interceptedStatement, ResultSetInternalMethods originalResultSet)
    throws SQLException
  {
    Iterator interceptors = this.statementInterceptors.iterator();
    while (interceptors.hasNext())
    {
      StatementInterceptor interceptor = (StatementInterceptor)interceptors.next();
      
      boolean executeTopLevelOnly = interceptor.executeTopLevelOnly();
      boolean shouldExecute = ((executeTopLevelOnly) && (this.statementExecutionDepth == 1)) || (!executeTopLevelOnly);
      if (shouldExecute)
      {
        String sqlToInterceptor = sql;
        if ((interceptedStatement instanceof PreparedStatement)) {
          sqlToInterceptor = ((PreparedStatement)interceptedStatement).asSql();
        }
        ResultSetInternalMethods interceptedResultSet = interceptor.postProcess(sqlToInterceptor, interceptedStatement, originalResultSet, this.connection);
        if (interceptedResultSet != null) {
          originalResultSet = interceptedResultSet;
        }
      }
    }
    return originalResultSet;
  }
  
  private void calculateSlowQueryThreshold()
  {
    this.slowQueryThreshold = this.connection.getSlowQueryThresholdMillis();
    if (this.connection.getUseNanosForElapsedTime())
    {
      long nanosThreshold = this.connection.getSlowQueryThresholdNanos();
      if (nanosThreshold != 0L) {
        this.slowQueryThreshold = nanosThreshold;
      } else {
        this.slowQueryThreshold *= 1000000L;
      }
    }
  }
  
  protected long getCurrentTimeNanosOrMillis()
  {
    if (this.useNanosForElapsedTime) {
      return Util.getCurrentTimeNanosOrMillis();
    }
    return System.currentTimeMillis();
  }
  
  String getHost()
  {
    return this.host;
  }
  
  boolean isVersion(int major, int minor, int subminor)
  {
    return (major == getServerMajorVersion()) && (minor == getServerMinorVersion()) && (subminor == getServerSubMinorVersion());
  }
  
  boolean versionMeetsMinimum(int major, int minor, int subminor)
  {
    if (getServerMajorVersion() >= major)
    {
      if (getServerMajorVersion() == major)
      {
        if (getServerMinorVersion() >= minor)
        {
          if (getServerMinorVersion() == minor) {
            return getServerSubMinorVersion() >= subminor;
          }
          return true;
        }
        return false;
      }
      return true;
    }
    return false;
  }
  
  private static final String getPacketDumpToLog(Buffer packetToDump, int packetLength)
  {
    if (packetLength < 1024) {
      return packetToDump.dump(packetLength);
    }
    StringBuffer packetDumpBuf = new StringBuffer(4096);
    packetDumpBuf.append(packetToDump.dump(1024));
    packetDumpBuf.append(Messages.getString("MysqlIO.36"));
    packetDumpBuf.append(1024);
    packetDumpBuf.append(Messages.getString("MysqlIO.37"));
    
    return packetDumpBuf.toString();
  }
  
  private final int readFully(InputStream in, byte[] b, int off, int len)
    throws IOException
  {
    if (len < 0) {
      throw new IndexOutOfBoundsException();
    }
    int n = 0;
    while (n < len)
    {
      int count = in.read(b, off + n, len - n);
      if (count < 0) {
        throw new EOFException(Messages.getString("MysqlIO.EOF", new Object[] { new Integer(len), new Integer(n) }));
      }
      n += count;
    }
    return n;
  }
  
  private final long skipFully(InputStream in, long len)
    throws IOException
  {
    if (len < 0L) {
      throw new IOException("Negative skip length not allowed");
    }
    long n = 0L;
    while (n < len)
    {
      long count = in.skip(len - n);
      if (count < 0L) {
        throw new EOFException(Messages.getString("MysqlIO.EOF", new Object[] { new Long(len), new Long(n) }));
      }
      n += count;
    }
    return n;
  }
  
  protected final ResultSetImpl readResultsForQueryOrUpdate(StatementImpl callingStatement, int maxRows, int resultSetType, int resultSetConcurrency, boolean streamResults, String catalog, Buffer resultPacket, boolean isBinaryEncoded, long preSentColumnCount, Field[] metadataFromCache)
    throws SQLException
  {
    long columnCount = resultPacket.readFieldLength();
    if (columnCount == 0L) {
      return buildResultSetWithUpdates(callingStatement, resultPacket);
    }
    if (columnCount == -1L)
    {
      String charEncoding = null;
      if (this.connection.getUseUnicode()) {
        charEncoding = this.connection.getEncoding();
      }
      String fileName = null;
      if (this.platformDbCharsetMatches) {
        fileName = charEncoding != null ? resultPacket.readString(charEncoding, getExceptionInterceptor()) : resultPacket.readString();
      } else {
        fileName = resultPacket.readString();
      }
      return sendFileToServer(callingStatement, fileName);
    }
    ResultSetImpl results = getResultSet(callingStatement, columnCount, maxRows, resultSetType, resultSetConcurrency, streamResults, catalog, isBinaryEncoded, metadataFromCache);
    
    return results;
  }
  
  private int alignPacketSize(int a, int l)
  {
    return a + l - 1 & (l - 1 ^ 0xFFFFFFFF);
  }
  
  private ResultSetImpl buildResultSetWithRows(StatementImpl callingStatement, String catalog, Field[] fields, RowData rows, int resultSetType, int resultSetConcurrency, boolean isBinaryEncoded)
    throws SQLException
  {
    ResultSetImpl rs = null;
    switch (resultSetConcurrency)
    {
    case 1007: 
      rs = ResultSetImpl.getInstance(catalog, fields, rows, this.connection, callingStatement, false);
      if (isBinaryEncoded) {
        rs.setBinaryEncoded();
      }
      break;
    case 1008: 
      rs = ResultSetImpl.getInstance(catalog, fields, rows, this.connection, callingStatement, true);
      
      break;
    default: 
      return ResultSetImpl.getInstance(catalog, fields, rows, this.connection, callingStatement, false);
    }
    rs.setResultSetType(resultSetType);
    rs.setResultSetConcurrency(resultSetConcurrency);
    
    return rs;
  }
  
  private ResultSetImpl buildResultSetWithUpdates(StatementImpl callingStatement, Buffer resultPacket)
    throws SQLException
  {
    long updateCount = -1L;
    long updateID = -1L;
    String info = null;
    try
    {
      if (this.useNewUpdateCounts)
      {
        updateCount = resultPacket.newReadLength();
        updateID = resultPacket.newReadLength();
      }
      else
      {
        updateCount = resultPacket.readLength();
        updateID = resultPacket.readLength();
      }
      if (this.use41Extensions)
      {
        this.serverStatus = resultPacket.readInt();
        
        checkTransactionState(this.oldServerStatus);
        
        this.warningCount = resultPacket.readInt();
        if (this.warningCount > 0) {
          this.hadWarnings = true;
        }
        resultPacket.readByte();
        
        setServerSlowQueryFlags();
      }
      if (this.connection.isReadInfoMsgEnabled()) {
        info = resultPacket.readString(this.connection.getErrorMessageEncoding(), getExceptionInterceptor());
      }
    }
    catch (Exception ex)
    {
      SQLException sqlEx = SQLError.createSQLException(SQLError.get("S1000"), "S1000", -1, getExceptionInterceptor());
      
      sqlEx.initCause(ex);
      
      throw sqlEx;
    }
    ResultSetInternalMethods updateRs = ResultSetImpl.getInstance(updateCount, updateID, this.connection, callingStatement);
    if (info != null) {
      ((ResultSetImpl)updateRs).setServerInfo(info);
    }
    return (ResultSetImpl)updateRs;
  }
  
  private void setServerSlowQueryFlags()
  {
    if (this.profileSql)
    {
      this.queryNoIndexUsed = ((this.serverStatus & 0x10) != 0);
      
      this.queryBadIndexUsed = ((this.serverStatus & 0x20) != 0);
      
      this.serverQueryWasSlow = ((this.serverStatus & 0x800) != 0);
    }
  }
  
  private void checkForOutstandingStreamingData()
    throws SQLException
  {
    if (this.streamingData != null)
    {
      boolean shouldClobber = this.connection.getClobberStreamingResults();
      if (!shouldClobber) {
        throw SQLError.createSQLException(Messages.getString("MysqlIO.39") + this.streamingData + Messages.getString("MysqlIO.40") + Messages.getString("MysqlIO.41") + Messages.getString("MysqlIO.42"), getExceptionInterceptor());
      }
      this.streamingData.getOwner().realClose(false);
      
      clearInputStream();
    }
  }
  
  private Buffer compressPacket(Buffer packet, int offset, int packetLen, int headerLength)
    throws SQLException
  {
    packet.writeLongInt(packetLen - headerLength);
    packet.writeByte((byte)0);
    
    int lengthToWrite = 0;
    int compressedLength = 0;
    byte[] bytesToCompress = packet.getByteBuffer();
    byte[] compressedBytes = null;
    int offsetWrite = 0;
    if (packetLen < 50)
    {
      lengthToWrite = packetLen;
      compressedBytes = packet.getByteBuffer();
      compressedLength = 0;
      offsetWrite = offset;
    }
    else
    {
      compressedBytes = new byte[bytesToCompress.length * 2];
      
      this.deflater.reset();
      this.deflater.setInput(bytesToCompress, offset, packetLen);
      this.deflater.finish();
      
      int compLen = this.deflater.deflate(compressedBytes);
      if (compLen > packetLen)
      {
        lengthToWrite = packetLen;
        compressedBytes = packet.getByteBuffer();
        compressedLength = 0;
        offsetWrite = offset;
      }
      else
      {
        lengthToWrite = compLen;
        headerLength += 3;
        compressedLength = packetLen;
      }
    }
    Buffer compressedPacket = new Buffer(packetLen + headerLength);
    
    compressedPacket.setPosition(0);
    compressedPacket.writeLongInt(lengthToWrite);
    compressedPacket.writeByte(this.packetSequence);
    compressedPacket.writeLongInt(compressedLength);
    compressedPacket.writeBytesNoNull(compressedBytes, offsetWrite, lengthToWrite);
    
    return compressedPacket;
  }
  
  private final void readServerStatusForResultSets(Buffer rowPacket)
    throws SQLException
  {
    if (this.use41Extensions)
    {
      rowPacket.readByte();
      
      this.warningCount = rowPacket.readInt();
      if (this.warningCount > 0) {
        this.hadWarnings = true;
      }
      this.oldServerStatus = this.serverStatus;
      this.serverStatus = rowPacket.readInt();
      checkTransactionState(this.oldServerStatus);
      
      setServerSlowQueryFlags();
    }
  }
  
  private SocketFactory createSocketFactory()
    throws SQLException
  {
    try
    {
      if (this.socketFactoryClassName == null) {
        throw SQLError.createSQLException(Messages.getString("MysqlIO.75"), "08001", getExceptionInterceptor());
      }
      return (SocketFactory)Class.forName(this.socketFactoryClassName).newInstance();
    }
    catch (Exception ex)
    {
      SQLException sqlEx = SQLError.createSQLException(Messages.getString("MysqlIO.76") + this.socketFactoryClassName + Messages.getString("MysqlIO.77"), "08001", getExceptionInterceptor());
      
      sqlEx.initCause(ex);
      
      throw sqlEx;
    }
  }
  
  private void enqueuePacketForDebugging(boolean isPacketBeingSent, boolean isPacketReused, int sendLength, byte[] header, Buffer packet)
    throws SQLException
  {
    if (this.packetDebugRingBuffer.size() + 1 > this.connection.getPacketDebugBufferSize()) {
      this.packetDebugRingBuffer.removeFirst();
    }
    StringBuffer packetDump = null;
    if (!isPacketBeingSent)
    {
      int bytesToDump = Math.min(1024, packet.getBufLength());
      
      Buffer packetToDump = new Buffer(4 + bytesToDump);
      
      packetToDump.setPosition(0);
      packetToDump.writeBytesNoNull(header);
      packetToDump.writeBytesNoNull(packet.getBytes(0, bytesToDump));
      
      String packetPayload = packetToDump.dump(bytesToDump);
      
      packetDump = new StringBuffer(96 + packetPayload.length());
      
      packetDump.append("Server ");
      if (isPacketReused) {
        packetDump.append("(re-used)");
      } else {
        packetDump.append("(new)");
      }
      packetDump.append(" ");
      packetDump.append(packet.toSuperString());
      packetDump.append(" --------------------> Client\n");
      packetDump.append("\nPacket payload:\n\n");
      packetDump.append(packetPayload);
      if (bytesToDump == 1024) {
        packetDump.append("\nNote: Packet of " + packet.getBufLength() + " bytes truncated to " + 1024 + " bytes.\n");
      }
    }
    else
    {
      int bytesToDump = Math.min(1024, sendLength);
      
      String packetPayload = packet.dump(bytesToDump);
      
      packetDump = new StringBuffer(68 + packetPayload.length());
      
      packetDump.append("Client ");
      packetDump.append(packet.toSuperString());
      packetDump.append("--------------------> Server\n");
      packetDump.append("\nPacket payload:\n\n");
      packetDump.append(packetPayload);
      if (bytesToDump == 1024) {
        packetDump.append("\nNote: Packet of " + sendLength + " bytes truncated to " + 1024 + " bytes.\n");
      }
    }
    this.packetDebugRingBuffer.addLast(packetDump);
  }
  
  private RowData readSingleRowSet(long columnCount, int maxRows, int resultSetConcurrency, boolean isBinaryEncoded, Field[] fields)
    throws SQLException
  {
    ArrayList rows = new ArrayList();
    
    boolean useBufferRowExplicit = useBufferRowExplicit(fields);
    
    ResultSetRow row = nextRow(fields, (int)columnCount, isBinaryEncoded, resultSetConcurrency, false, useBufferRowExplicit, false, null);
    
    int rowCount = 0;
    if (row != null)
    {
      rows.add(row);
      rowCount = 1;
    }
    while (row != null)
    {
      row = nextRow(fields, (int)columnCount, isBinaryEncoded, resultSetConcurrency, false, useBufferRowExplicit, false, null);
      if ((row != null) && (
        (maxRows == -1) || (rowCount < maxRows)))
      {
        rows.add(row);
        rowCount++;
      }
    }
    RowData rowData = new RowDataStatic(rows);
    
    return rowData;
  }
  
  public static boolean useBufferRowExplicit(Field[] fields)
  {
    if (fields == null) {
      return false;
    }
    for (int i = 0; i < fields.length; i++) {
      switch (fields[i].getSQLType())
      {
      case -4: 
      case -1: 
      case 2004: 
      case 2005: 
        return true;
      }
    }
    return false;
  }
  
  private void reclaimLargeReusablePacket()
  {
    if ((this.reusablePacket != null) && (this.reusablePacket.getCapacity() > 1048576)) {
      this.reusablePacket = new Buffer(1024);
    }
  }
  
  private final Buffer reuseAndReadPacket(Buffer reuse)
    throws SQLException
  {
    return reuseAndReadPacket(reuse, -1);
  }
  
  private int readRemainingMultiPackets(Buffer reuse, byte multiPacketSeq, int packetEndPoint)
    throws IOException, SQLException
  {
    int lengthRead = readFully(this.mysqlInput, this.packetHeaderBuf, 0, 4);
    if (lengthRead < 4)
    {
      forceClose();
      throw new IOException(Messages.getString("MysqlIO.47"));
    }
    int packetLength = (this.packetHeaderBuf[0] & 0xFF) + ((this.packetHeaderBuf[1] & 0xFF) << 8) + ((this.packetHeaderBuf[2] & 0xFF) << 16);
    
    Buffer multiPacket = new Buffer(packetLength);
    boolean firstMultiPkt = true;
    for (;;)
    {
      if (!firstMultiPkt)
      {
        lengthRead = readFully(this.mysqlInput, this.packetHeaderBuf, 0, 4);
        if (lengthRead < 4)
        {
          forceClose();
          throw new IOException(Messages.getString("MysqlIO.48"));
        }
        packetLength = (this.packetHeaderBuf[0] & 0xFF) + ((this.packetHeaderBuf[1] & 0xFF) << 8) + ((this.packetHeaderBuf[2] & 0xFF) << 16);
      }
      else
      {
        firstMultiPkt = false;
      }
      if ((!this.useNewLargePackets) && (packetLength == 1))
      {
        clearInputStream();
        
        break;
      }
      if (packetLength < this.maxThreeBytes)
      {
        byte newPacketSeq = this.packetHeaderBuf[3];
        if (newPacketSeq != multiPacketSeq + 1) {
          throw new IOException(Messages.getString("MysqlIO.49"));
        }
        multiPacketSeq = newPacketSeq;
        
        multiPacket.setPosition(0);
        
        multiPacket.setBufLength(packetLength);
        
        byte[] byteBuf = multiPacket.getByteBuffer();
        int lengthToWrite = packetLength;
        
        int bytesRead = readFully(this.mysqlInput, byteBuf, 0, packetLength);
        if (bytesRead != lengthToWrite) {
          throw SQLError.createCommunicationsException(this.connection, this.lastPacketSentTimeMs, this.lastPacketReceivedTimeMs, SQLError.createSQLException(Messages.getString("MysqlIO.50") + lengthToWrite + Messages.getString("MysqlIO.51") + bytesRead + ".", getExceptionInterceptor()), getExceptionInterceptor());
        }
        reuse.writeBytesNoNull(byteBuf, 0, lengthToWrite);
        
        packetEndPoint += lengthToWrite;
        
        break;
      }
      byte newPacketSeq = this.packetHeaderBuf[3];
      if (newPacketSeq != multiPacketSeq + 1) {
        throw new IOException(Messages.getString("MysqlIO.53"));
      }
      multiPacketSeq = newPacketSeq;
      
      multiPacket.setPosition(0);
      
      multiPacket.setBufLength(packetLength);
      
      byte[] byteBuf = multiPacket.getByteBuffer();
      int lengthToWrite = packetLength;
      
      int bytesRead = readFully(this.mysqlInput, byteBuf, 0, packetLength);
      if (bytesRead != lengthToWrite) {
        throw SQLError.createCommunicationsException(this.connection, this.lastPacketSentTimeMs, this.lastPacketReceivedTimeMs, SQLError.createSQLException(Messages.getString("MysqlIO.54") + lengthToWrite + Messages.getString("MysqlIO.55") + bytesRead + ".", getExceptionInterceptor()), getExceptionInterceptor());
      }
      reuse.writeBytesNoNull(byteBuf, 0, lengthToWrite);
      
      packetEndPoint += lengthToWrite;
    }
    reuse.setPosition(0);
    reuse.setWasMultiPacket(true);
    return packetLength;
  }
  
  private void checkPacketSequencing(byte multiPacketSeq)
    throws SQLException
  {
    if ((multiPacketSeq == Byte.MIN_VALUE) && (this.readPacketSequence != Byte.MAX_VALUE)) {
      throw SQLError.createCommunicationsException(this.connection, this.lastPacketSentTimeMs, this.lastPacketReceivedTimeMs, new IOException("Packets out of order, expected packet # -128, but received packet # " + multiPacketSeq), getExceptionInterceptor());
    }
    if ((this.readPacketSequence == -1) && (multiPacketSeq != 0)) {
      throw SQLError.createCommunicationsException(this.connection, this.lastPacketSentTimeMs, this.lastPacketReceivedTimeMs, new IOException("Packets out of order, expected packet # -1, but received packet # " + multiPacketSeq), getExceptionInterceptor());
    }
    if ((multiPacketSeq != Byte.MIN_VALUE) && (this.readPacketSequence != -1) && (multiPacketSeq != this.readPacketSequence + 1)) {
      throw SQLError.createCommunicationsException(this.connection, this.lastPacketSentTimeMs, this.lastPacketReceivedTimeMs, new IOException("Packets out of order, expected packet # " + (this.readPacketSequence + 1) + ", but received packet # " + multiPacketSeq), getExceptionInterceptor());
    }
  }
  
  void enableMultiQueries()
    throws SQLException
  {
    Buffer buf = getSharedSendPacket();
    
    buf.clear();
    buf.writeByte((byte)27);
    buf.writeInt(0);
    sendCommand(27, null, buf, false, null, 0);
  }
  
  void disableMultiQueries()
    throws SQLException
  {
    Buffer buf = getSharedSendPacket();
    
    buf.clear();
    buf.writeByte((byte)27);
    buf.writeInt(1);
    sendCommand(27, null, buf, false, null, 0);
  }
  
  private final void send(Buffer packet, int packetLen)
    throws SQLException
  {
    try
    {
      if ((this.maxAllowedPacket > 0) && (packetLen > this.maxAllowedPacket)) {
        throw new PacketTooBigException(packetLen, this.maxAllowedPacket);
      }
      if ((this.serverMajorVersion >= 4) && (packetLen >= this.maxThreeBytes))
      {
        sendSplitPackets(packet);
      }
      else
      {
        this.packetSequence = ((byte)(this.packetSequence + 1));
        
        Buffer packetToSend = packet;
        
        packetToSend.setPosition(0);
        if (this.useCompression)
        {
          int originalPacketLen = packetLen;
          
          packetToSend = compressPacket(packet, 0, packetLen, 4);
          
          packetLen = packetToSend.getPosition();
          if (this.traceProtocol)
          {
            StringBuffer traceMessageBuf = new StringBuffer();
            
            traceMessageBuf.append(Messages.getString("MysqlIO.57"));
            traceMessageBuf.append(getPacketDumpToLog(packetToSend, packetLen));
            
            traceMessageBuf.append(Messages.getString("MysqlIO.58"));
            traceMessageBuf.append(getPacketDumpToLog(packet, originalPacketLen));
            
            this.connection.getLog().logTrace(traceMessageBuf.toString());
          }
        }
        else
        {
          packetToSend.writeLongInt(packetLen - 4);
          packetToSend.writeByte(this.packetSequence);
          if (this.traceProtocol)
          {
            StringBuffer traceMessageBuf = new StringBuffer();
            
            traceMessageBuf.append(Messages.getString("MysqlIO.59"));
            traceMessageBuf.append(packetToSend.dump(packetLen));
            
            this.connection.getLog().logTrace(traceMessageBuf.toString());
          }
        }
        this.mysqlOutput.write(packetToSend.getByteBuffer(), 0, packetLen);
        
        this.mysqlOutput.flush();
      }
      if (this.enablePacketDebug) {
        enqueuePacketForDebugging(true, false, packetLen + 5, this.packetHeaderBuf, packet);
      }
      if (packet == this.sharedSendPacket) {
        reclaimLargeSharedSendPacket();
      }
      if (this.connection.getMaintainTimeStats()) {
        this.lastPacketSentTimeMs = System.currentTimeMillis();
      }
    }
    catch (IOException ioEx)
    {
      throw SQLError.createCommunicationsException(this.connection, this.lastPacketSentTimeMs, this.lastPacketReceivedTimeMs, ioEx, getExceptionInterceptor());
    }
  }
  
  private final ResultSetImpl sendFileToServer(StatementImpl callingStatement, String fileName)
    throws SQLException
  {
    Buffer filePacket = this.loadFileBufRef == null ? null : (Buffer)this.loadFileBufRef.get();
    
    int bigPacketLength = Math.min(this.connection.getMaxAllowedPacket() - 12, alignPacketSize(this.connection.getMaxAllowedPacket() - 16, 4096) - 12);
    
    int oneMeg = 1048576;
    
    int smallerPacketSizeAligned = Math.min(oneMeg - 12, alignPacketSize(oneMeg - 16, 4096) - 12);
    
    int packetLength = Math.min(smallerPacketSizeAligned, bigPacketLength);
    if (filePacket == null) {
      try
      {
        filePacket = new Buffer(packetLength + 4);
        this.loadFileBufRef = new SoftReference(filePacket);
      }
      catch (OutOfMemoryError oom)
      {
        throw SQLError.createSQLException("Could not allocate packet of " + packetLength + " bytes required for LOAD DATA LOCAL INFILE operation." + " Try increasing max heap allocation for JVM or decreasing server variable " + "'max_allowed_packet'", "S1001", getExceptionInterceptor());
      }
    }
    filePacket.clear();
    send(filePacket, 0);
    
    byte[] fileBuf = new byte[packetLength];
    
    BufferedInputStream fileIn = null;
    try
    {
      if (!this.connection.getAllowLoadLocalInfile()) {
        throw SQLError.createSQLException(Messages.getString("MysqlIO.LoadDataLocalNotAllowed"), "S1000", getExceptionInterceptor());
      }
      InputStream hookedStream = null;
      if (callingStatement != null) {
        hookedStream = callingStatement.getLocalInfileInputStream();
      }
      if (hookedStream != null) {
        fileIn = new BufferedInputStream(hookedStream);
      } else if (!this.connection.getAllowUrlInLocalInfile()) {
        fileIn = new BufferedInputStream(new FileInputStream(fileName));
      } else if (fileName.indexOf(':') != -1) {
        try
        {
          URL urlFromFileName = new URL(fileName);
          fileIn = new BufferedInputStream(urlFromFileName.openStream());
        }
        catch (MalformedURLException badUrlEx)
        {
          fileIn = new BufferedInputStream(new FileInputStream(fileName));
        }
      } else {
        fileIn = new BufferedInputStream(new FileInputStream(fileName));
      }
      int bytesRead = 0;
      while ((bytesRead = fileIn.read(fileBuf)) != -1)
      {
        filePacket.clear();
        filePacket.writeBytesNoNull(fileBuf, 0, bytesRead);
        send(filePacket, filePacket.getPosition());
      }
    }
    catch (IOException ioEx)
    {
      StringBuffer messageBuf = new StringBuffer(Messages.getString("MysqlIO.60"));
      if (!this.connection.getParanoid())
      {
        messageBuf.append("'");
        if (fileName != null) {
          messageBuf.append(fileName);
        }
        messageBuf.append("'");
      }
      messageBuf.append(Messages.getString("MysqlIO.63"));
      if (!this.connection.getParanoid())
      {
        messageBuf.append(Messages.getString("MysqlIO.64"));
        messageBuf.append(Util.stackTraceToString(ioEx));
      }
      throw SQLError.createSQLException(messageBuf.toString(), "S1009", getExceptionInterceptor());
    }
    finally
    {
      if (fileIn != null)
      {
        try
        {
          fileIn.close();
        }
        catch (Exception ex)
        {
          SQLException sqlEx = SQLError.createSQLException(Messages.getString("MysqlIO.65"), "S1000", getExceptionInterceptor());
          
          sqlEx.initCause(ex);
          
          throw sqlEx;
        }
        fileIn = null;
      }
      else
      {
        filePacket.clear();
        send(filePacket, filePacket.getPosition());
        checkErrorPacket();
      }
    }
    filePacket.clear();
    send(filePacket, filePacket.getPosition());
    
    Buffer resultPacket = checkErrorPacket();
    
    return buildResultSetWithUpdates(callingStatement, resultPacket);
  }
  
  private Buffer checkErrorPacket(int command)
    throws SQLException
  {
    int statusCode = 0;
    Buffer resultPacket = null;
    this.serverStatus = 0;
    try
    {
      resultPacket = reuseAndReadPacket(this.reusablePacket);
    }
    catch (SQLException sqlEx)
    {
      throw sqlEx;
    }
    catch (Exception fallThru)
    {
      throw SQLError.createCommunicationsException(this.connection, this.lastPacketSentTimeMs, this.lastPacketReceivedTimeMs, fallThru, getExceptionInterceptor());
    }
    checkErrorPacket(resultPacket);
    
    return resultPacket;
  }
  
  private void checkErrorPacket(Buffer resultPacket)
    throws SQLException
  {
    int statusCode = resultPacket.readByte();
    if (statusCode == -1)
    {
      int errno = 2000;
      if (this.protocolVersion > 9)
      {
        errno = resultPacket.readInt();
        
        String xOpen = null;
        
        String serverErrorMessage = resultPacket.readString(this.connection.getErrorMessageEncoding(), getExceptionInterceptor());
        if (serverErrorMessage.charAt(0) == '#')
        {
          if (serverErrorMessage.length() > 6)
          {
            xOpen = serverErrorMessage.substring(1, 6);
            serverErrorMessage = serverErrorMessage.substring(6);
            if (xOpen.equals("HY000")) {
              xOpen = SQLError.mysqlToSqlState(errno, this.connection.getUseSqlStateCodes());
            }
          }
          else
          {
            xOpen = SQLError.mysqlToSqlState(errno, this.connection.getUseSqlStateCodes());
          }
        }
        else {
          xOpen = SQLError.mysqlToSqlState(errno, this.connection.getUseSqlStateCodes());
        }
        clearInputStream();
        
        StringBuffer errorBuf = new StringBuffer();
        
        String xOpenErrorMessage = SQLError.get(xOpen);
        if ((!this.connection.getUseOnlyServerErrorMessages()) && 
          (xOpenErrorMessage != null))
        {
          errorBuf.append(xOpenErrorMessage);
          errorBuf.append(Messages.getString("MysqlIO.68"));
        }
        errorBuf.append(serverErrorMessage);
        if ((!this.connection.getUseOnlyServerErrorMessages()) && 
          (xOpenErrorMessage != null)) {
          errorBuf.append("\"");
        }
        appendInnodbStatusInformation(xOpen, errorBuf);
        if ((xOpen != null) && (xOpen.startsWith("22"))) {
          throw new MysqlDataTruncation(errorBuf.toString(), 0, true, false, 0, 0, errno);
        }
        throw SQLError.createSQLException(errorBuf.toString(), xOpen, errno, getExceptionInterceptor());
      }
      String serverErrorMessage = resultPacket.readString(this.connection.getErrorMessageEncoding(), getExceptionInterceptor());
      
      clearInputStream();
      if (serverErrorMessage.indexOf(Messages.getString("MysqlIO.70")) != -1) {
        throw SQLError.createSQLException(SQLError.get("S0022") + ", " + serverErrorMessage, "S0022", -1, getExceptionInterceptor());
      }
      StringBuffer errorBuf = new StringBuffer(Messages.getString("MysqlIO.72"));
      
      errorBuf.append(serverErrorMessage);
      errorBuf.append("\"");
      
      throw SQLError.createSQLException(SQLError.get("S1000") + ", " + errorBuf.toString(), "S1000", -1, getExceptionInterceptor());
    }
  }
  
  private void appendInnodbStatusInformation(String xOpen, StringBuffer errorBuf)
    throws SQLException
  {
    if ((this.connection.getIncludeInnodbStatusInDeadlockExceptions()) && (xOpen != null) && ((xOpen.startsWith("40")) || (xOpen.startsWith("41"))) && (this.streamingData == null))
    {
      ResultSet rs = null;
      try
      {
        rs = sqlQueryDirect(null, "SHOW ENGINE INNODB STATUS", this.connection.getEncoding(), null, -1, 1003, 1007, false, this.connection.getCatalog(), null);
        if (rs.next())
        {
          errorBuf.append("\n\n");
          errorBuf.append(rs.getString("Status"));
        }
        else
        {
          errorBuf.append("\n\n");
          errorBuf.append(Messages.getString("MysqlIO.NoInnoDBStatusFound"));
        }
      }
      catch (Exception ex)
      {
        errorBuf.append("\n\n");
        errorBuf.append(Messages.getString("MysqlIO.InnoDBStatusFailed"));
        
        errorBuf.append("\n\n");
        errorBuf.append(Util.stackTraceToString(ex));
      }
      finally
      {
        if (rs != null) {
          rs.close();
        }
      }
    }
  }
  
  private final void sendSplitPackets(Buffer packet)
    throws SQLException
  {
    try
    {
      Buffer headerPacket = this.splitBufRef == null ? null : (Buffer)this.splitBufRef.get();
      if (headerPacket == null)
      {
        headerPacket = new Buffer(this.maxThreeBytes + 4);
        
        this.splitBufRef = new SoftReference(headerPacket);
      }
      int len = packet.getPosition();
      int splitSize = this.maxThreeBytes;
      int originalPacketPos = 4;
      byte[] origPacketBytes = packet.getByteBuffer();
      byte[] headerPacketBytes = headerPacket.getByteBuffer();
      while (len >= this.maxThreeBytes)
      {
        this.packetSequence = ((byte)(this.packetSequence + 1));
        
        headerPacket.setPosition(0);
        headerPacket.writeLongInt(splitSize);
        
        headerPacket.writeByte(this.packetSequence);
        System.arraycopy(origPacketBytes, originalPacketPos, headerPacketBytes, 4, splitSize);
        
        int packetLen = splitSize + 4;
        if (!this.useCompression)
        {
          this.mysqlOutput.write(headerPacketBytes, 0, splitSize + 4);
          
          this.mysqlOutput.flush();
        }
        else
        {
          headerPacket.setPosition(0);
          Buffer packetToSend = compressPacket(headerPacket, 4, splitSize, 4);
          
          packetLen = packetToSend.getPosition();
          
          this.mysqlOutput.write(packetToSend.getByteBuffer(), 0, packetLen);
          
          this.mysqlOutput.flush();
        }
        originalPacketPos += splitSize;
        len -= splitSize;
      }
      headerPacket.clear();
      headerPacket.setPosition(0);
      headerPacket.writeLongInt(len - 4);
      this.packetSequence = ((byte)(this.packetSequence + 1));
      headerPacket.writeByte(this.packetSequence);
      if (len != 0) {
        System.arraycopy(origPacketBytes, originalPacketPos, headerPacketBytes, 4, len - 4);
      }
      int packetLen = len - 4;
      if (!this.useCompression)
      {
        this.mysqlOutput.write(headerPacket.getByteBuffer(), 0, len);
        this.mysqlOutput.flush();
      }
      else
      {
        headerPacket.setPosition(0);
        Buffer packetToSend = compressPacket(headerPacket, 4, packetLen, 4);
        
        packetLen = packetToSend.getPosition();
        
        this.mysqlOutput.write(packetToSend.getByteBuffer(), 0, packetLen);
        
        this.mysqlOutput.flush();
      }
    }
    catch (IOException ioEx)
    {
      throw SQLError.createCommunicationsException(this.connection, this.lastPacketSentTimeMs, this.lastPacketReceivedTimeMs, ioEx, getExceptionInterceptor());
    }
  }
  
  private void reclaimLargeSharedSendPacket()
  {
    if ((this.sharedSendPacket != null) && (this.sharedSendPacket.getCapacity() > 1048576)) {
      this.sharedSendPacket = new Buffer(1024);
    }
  }
  
  boolean hadWarnings()
  {
    return this.hadWarnings;
  }
  
  void scanForAndThrowDataTruncation()
    throws SQLException
  {
    if ((this.streamingData == null) && (versionMeetsMinimum(4, 1, 0)) && (this.connection.getJdbcCompliantTruncation()) && (this.warningCount > 0)) {
      SQLError.convertShowWarningsToSQLWarnings(this.connection, this.warningCount, true);
    }
  }
  
  private void secureAuth(Buffer packet, int packLength, String user, String password, String database, boolean writeClientParams)
    throws SQLException
  {
    if (packet == null) {
      packet = new Buffer(packLength);
    }
    if (writeClientParams) {
      if (this.use41Extensions)
      {
        if (versionMeetsMinimum(4, 1, 1))
        {
          packet.writeLong(this.clientParam);
          packet.writeLong(this.maxThreeBytes);
          
          packet.writeByte((byte)8);
          
          packet.writeBytesNoNull(new byte[23]);
        }
        else
        {
          packet.writeLong(this.clientParam);
          packet.writeLong(this.maxThreeBytes);
        }
      }
      else
      {
        packet.writeInt((int)this.clientParam);
        packet.writeLongInt(this.maxThreeBytes);
      }
    }
    packet.writeString(user, "Cp1252", this.connection);
    if (password.length() != 0) {
      packet.writeString("xxxxxxxx", "Cp1252", this.connection);
    } else {
      packet.writeString("", "Cp1252", this.connection);
    }
    if (this.useConnectWithDb) {
      packet.writeString(database, "Cp1252", this.connection);
    }
    send(packet, packet.getPosition());
    if (password.length() > 0)
    {
      Buffer b = readPacket();
      
      b.setPosition(0);
      
      byte[] replyAsBytes = b.getByteBuffer();
      if ((replyAsBytes.length == 25) && (replyAsBytes[0] != 0)) {
        if (replyAsBytes[0] != 42) {
          try
          {
            byte[] buff = Security.passwordHashStage1(password);
            
            byte[] passwordHash = new byte[buff.length];
            System.arraycopy(buff, 0, passwordHash, 0, buff.length);
            
            passwordHash = Security.passwordHashStage2(passwordHash, replyAsBytes);
            
            byte[] packetDataAfterSalt = new byte[replyAsBytes.length - 5];
            
            System.arraycopy(replyAsBytes, 4, packetDataAfterSalt, 0, replyAsBytes.length - 5);
            
            byte[] mysqlScrambleBuff = new byte[20];
            
            Security.passwordCrypt(packetDataAfterSalt, mysqlScrambleBuff, passwordHash, 20);
            
            Security.passwordCrypt(mysqlScrambleBuff, buff, buff, 20);
            
            Buffer packet2 = new Buffer(25);
            packet2.writeBytesNoNull(buff);
            
            this.packetSequence = ((byte)(this.packetSequence + 1));
            
            send(packet2, 24);
          }
          catch (NoSuchAlgorithmException nse)
          {
            throw SQLError.createSQLException(Messages.getString("MysqlIO.91") + Messages.getString("MysqlIO.92"), "S1000", getExceptionInterceptor());
          }
        } else {
          try
          {
            byte[] passwordHash = Security.createKeyFromOldPassword(password);
            
            byte[] netReadPos4 = new byte[replyAsBytes.length - 5];
            
            System.arraycopy(replyAsBytes, 4, netReadPos4, 0, replyAsBytes.length - 5);
            
            byte[] mysqlScrambleBuff = new byte[20];
            
            Security.passwordCrypt(netReadPos4, mysqlScrambleBuff, passwordHash, 20);
            
            String scrambledPassword = Util.scramble(new String(mysqlScrambleBuff), password);
            
            Buffer packet2 = new Buffer(packLength);
            packet2.writeString(scrambledPassword, "Cp1252", this.connection);
            this.packetSequence = ((byte)(this.packetSequence + 1));
            
            send(packet2, 24);
          }
          catch (NoSuchAlgorithmException nse)
          {
            throw SQLError.createSQLException(Messages.getString("MysqlIO.93") + Messages.getString("MysqlIO.94"), "S1000", getExceptionInterceptor());
          }
        }
      }
    }
  }
  
  void secureAuth411(Buffer packet, int packLength, String user, String password, String database, boolean writeClientParams)
    throws SQLException
  {
    if (packet == null) {
      packet = new Buffer(packLength);
    }
    if (writeClientParams) {
      if (this.use41Extensions)
      {
        if (versionMeetsMinimum(4, 1, 1))
        {
          packet.writeLong(this.clientParam);
          packet.writeLong(this.maxThreeBytes);
          
          packet.writeByte((byte)33);
          
          packet.writeBytesNoNull(new byte[23]);
        }
        else
        {
          packet.writeLong(this.clientParam);
          packet.writeLong(this.maxThreeBytes);
        }
      }
      else
      {
        packet.writeInt((int)this.clientParam);
        packet.writeLongInt(this.maxThreeBytes);
      }
    }
    packet.writeString(user, "utf-8", this.connection);
    if (password.length() != 0)
    {
      packet.writeByte((byte)20);
      try
      {
        packet.writeBytesNoNull(Security.scramble411(password, this.seed, this.connection));
      }
      catch (NoSuchAlgorithmException nse)
      {
        throw SQLError.createSQLException(Messages.getString("MysqlIO.95") + Messages.getString("MysqlIO.96"), "S1000", getExceptionInterceptor());
      }
      catch (UnsupportedEncodingException e)
      {
        throw SQLError.createSQLException(Messages.getString("MysqlIO.95") + Messages.getString("MysqlIO.96"), "S1000", getExceptionInterceptor());
      }
    }
    else
    {
      packet.writeByte((byte)0);
    }
    if (this.useConnectWithDb) {
      packet.writeString(database, "utf-8", this.connection);
    }
    send(packet, packet.getPosition());
    
    byte savePacketSequence = this.packetSequence++;
    
    Buffer reply = checkErrorPacket();
    if (reply.isLastDataPacket())
    {
      savePacketSequence = (byte)(savePacketSequence + 1);this.packetSequence = savePacketSequence;
      packet.clear();
      
      String seed323 = this.seed.substring(0, 8);
      packet.writeString(Util.newCrypt(password, seed323));
      send(packet, packet.getPosition());
      
      checkErrorPacket();
    }
  }
  
  private final ResultSetRow unpackBinaryResultSetRow(Field[] fields, Buffer binaryData, int resultSetConcurrency)
    throws SQLException
  {
    int numFields = fields.length;
    
    byte[][] unpackedRowData = new byte[numFields][];
    
    int nullCount = (numFields + 9) / 8;
    
    byte[] nullBitMask = new byte[nullCount];
    for (int i = 0; i < nullCount; i++) {
      nullBitMask[i] = binaryData.readByte();
    }
    int nullMaskPos = 0;
    int bit = 4;
    for (int i = 0; i < numFields; i++)
    {
      if ((nullBitMask[nullMaskPos] & bit) != 0) {
        unpackedRowData[i] = null;
      } else if (resultSetConcurrency != 1008) {
        extractNativeEncodedColumn(binaryData, fields, i, unpackedRowData);
      } else {
        unpackNativeEncodedColumn(binaryData, fields, i, unpackedRowData);
      }
      if ((bit <<= 1 & 0xFF) == 0)
      {
        bit = 1;
        
        nullMaskPos++;
      }
    }
    return new ByteArrayRow(unpackedRowData, getExceptionInterceptor());
  }
  
  private final void extractNativeEncodedColumn(Buffer binaryData, Field[] fields, int columnIndex, byte[][] unpackedRowData)
    throws SQLException
  {
    Field curField = fields[columnIndex];
    switch (curField.getMysqlType())
    {
    case 6: 
      break;
    case 1: 
      unpackedRowData[columnIndex] = { binaryData.readByte() };
      break;
    case 2: 
    case 13: 
      unpackedRowData[columnIndex] = binaryData.getBytes(2);
      break;
    case 3: 
    case 9: 
      unpackedRowData[columnIndex] = binaryData.getBytes(4);
      break;
    case 8: 
      unpackedRowData[columnIndex] = binaryData.getBytes(8);
      break;
    case 4: 
      unpackedRowData[columnIndex] = binaryData.getBytes(4);
      break;
    case 5: 
      unpackedRowData[columnIndex] = binaryData.getBytes(8);
      break;
    case 11: 
      int length = (int)binaryData.readFieldLength();
      
      unpackedRowData[columnIndex] = binaryData.getBytes(length);
      
      break;
    case 10: 
      int length = (int)binaryData.readFieldLength();
      
      unpackedRowData[columnIndex] = binaryData.getBytes(length);
      
      break;
    case 7: 
    case 12: 
      int length = (int)binaryData.readFieldLength();
      
      unpackedRowData[columnIndex] = binaryData.getBytes(length);
      break;
    case 0: 
    case 15: 
    case 246: 
    case 249: 
    case 250: 
    case 251: 
    case 252: 
    case 253: 
    case 254: 
    case 255: 
      unpackedRowData[columnIndex] = binaryData.readLenByteArray(0);
      
      break;
    case 16: 
      unpackedRowData[columnIndex] = binaryData.readLenByteArray(0);
      
      break;
    default: 
      throw SQLError.createSQLException(Messages.getString("MysqlIO.97") + curField.getMysqlType() + Messages.getString("MysqlIO.98") + columnIndex + Messages.getString("MysqlIO.99") + fields.length + Messages.getString("MysqlIO.100"), "S1000", getExceptionInterceptor());
    }
  }
  
  private final void unpackNativeEncodedColumn(Buffer binaryData, Field[] fields, int columnIndex, byte[][] unpackedRowData)
    throws SQLException
  {
    Field curField = fields[columnIndex];
    switch (curField.getMysqlType())
    {
    case 6: 
      break;
    case 1: 
      byte tinyVal = binaryData.readByte();
      if (!curField.isUnsigned())
      {
        unpackedRowData[columnIndex] = String.valueOf(tinyVal).getBytes();
      }
      else
      {
        short unsignedTinyVal = (short)(tinyVal & 0xFF);
        
        unpackedRowData[columnIndex] = String.valueOf(unsignedTinyVal).getBytes();
      }
      break;
    case 2: 
    case 13: 
      short shortVal = (short)binaryData.readInt();
      if (!curField.isUnsigned())
      {
        unpackedRowData[columnIndex] = String.valueOf(shortVal).getBytes();
      }
      else
      {
        int unsignedShortVal = shortVal & 0xFFFF;
        
        unpackedRowData[columnIndex] = String.valueOf(unsignedShortVal).getBytes();
      }
      break;
    case 3: 
    case 9: 
      int intVal = (int)binaryData.readLong();
      if (!curField.isUnsigned())
      {
        unpackedRowData[columnIndex] = String.valueOf(intVal).getBytes();
      }
      else
      {
        long longVal = intVal & 0xFFFFFFFF;
        
        unpackedRowData[columnIndex] = String.valueOf(longVal).getBytes();
      }
      break;
    case 8: 
      long longVal = binaryData.readLongLong();
      if (!curField.isUnsigned())
      {
        unpackedRowData[columnIndex] = String.valueOf(longVal).getBytes();
      }
      else
      {
        BigInteger asBigInteger = ResultSetImpl.convertLongToUlong(longVal);
        
        unpackedRowData[columnIndex] = asBigInteger.toString().getBytes();
      }
      break;
    case 4: 
      float floatVal = Float.intBitsToFloat(binaryData.readIntAsLong());
      
      unpackedRowData[columnIndex] = String.valueOf(floatVal).getBytes();
      
      break;
    case 5: 
      double doubleVal = Double.longBitsToDouble(binaryData.readLongLong());
      
      unpackedRowData[columnIndex] = String.valueOf(doubleVal).getBytes();
      
      break;
    case 11: 
      int length = (int)binaryData.readFieldLength();
      
      int hour = 0;
      int minute = 0;
      int seconds = 0;
      if (length != 0)
      {
        binaryData.readByte();
        binaryData.readLong();
        hour = binaryData.readByte();
        minute = binaryData.readByte();
        seconds = binaryData.readByte();
        if (length > 8) {
          binaryData.readLong();
        }
      }
      byte[] timeAsBytes = new byte[8];
      
      timeAsBytes[0] = ((byte)Character.forDigit(hour / 10, 10));
      timeAsBytes[1] = ((byte)Character.forDigit(hour % 10, 10));
      
      timeAsBytes[2] = 58;
      
      timeAsBytes[3] = ((byte)Character.forDigit(minute / 10, 10));
      
      timeAsBytes[4] = ((byte)Character.forDigit(minute % 10, 10));
      
      timeAsBytes[5] = 58;
      
      timeAsBytes[6] = ((byte)Character.forDigit(seconds / 10, 10));
      
      timeAsBytes[7] = ((byte)Character.forDigit(seconds % 10, 10));
      
      unpackedRowData[columnIndex] = timeAsBytes;
      
      break;
    case 10: 
      int length = (int)binaryData.readFieldLength();
      
      int year = 0;
      int month = 0;
      int day = 0;
      
      int hour = 0;
      int minute = 0;
      int seconds = 0;
      if (length != 0)
      {
        year = binaryData.readInt();
        month = binaryData.readByte();
        day = binaryData.readByte();
      }
      if ((year == 0) && (month == 0) && (day == 0))
      {
        if ("convertToNull".equals(this.connection.getZeroDateTimeBehavior()))
        {
          unpackedRowData[columnIndex] = null;
        }
        else
        {
          if ("exception".equals(this.connection.getZeroDateTimeBehavior())) {
            throw SQLError.createSQLException("Value '0000-00-00' can not be represented as java.sql.Date", "S1009", getExceptionInterceptor());
          }
          year = 1;
          month = 1;
          day = 1;
        }
      }
      else
      {
        byte[] dateAsBytes = new byte[10];
        
        dateAsBytes[0] = ((byte)Character.forDigit(year / 1000, 10));
        
        int after1000 = year % 1000;
        
        dateAsBytes[1] = ((byte)Character.forDigit(after1000 / 100, 10));
        
        int after100 = after1000 % 100;
        
        dateAsBytes[2] = ((byte)Character.forDigit(after100 / 10, 10));
        
        dateAsBytes[3] = ((byte)Character.forDigit(after100 % 10, 10));
        
        dateAsBytes[4] = 45;
        
        dateAsBytes[5] = ((byte)Character.forDigit(month / 10, 10));
        
        dateAsBytes[6] = ((byte)Character.forDigit(month % 10, 10));
        
        dateAsBytes[7] = 45;
        
        dateAsBytes[8] = ((byte)Character.forDigit(day / 10, 10));
        dateAsBytes[9] = ((byte)Character.forDigit(day % 10, 10));
        
        unpackedRowData[columnIndex] = dateAsBytes;
      }
      break;
    case 7: 
    case 12: 
      int length = (int)binaryData.readFieldLength();
      
      int year = 0;
      int month = 0;
      int day = 0;
      
      int hour = 0;
      int minute = 0;
      int seconds = 0;
      
      int nanos = 0;
      if (length != 0)
      {
        year = binaryData.readInt();
        month = binaryData.readByte();
        day = binaryData.readByte();
        if (length > 4)
        {
          hour = binaryData.readByte();
          minute = binaryData.readByte();
          seconds = binaryData.readByte();
        }
      }
      if ((year == 0) && (month == 0) && (day == 0))
      {
        if ("convertToNull".equals(this.connection.getZeroDateTimeBehavior()))
        {
          unpackedRowData[columnIndex] = null;
        }
        else
        {
          if ("exception".equals(this.connection.getZeroDateTimeBehavior())) {
            throw SQLError.createSQLException("Value '0000-00-00' can not be represented as java.sql.Timestamp", "S1009", getExceptionInterceptor());
          }
          year = 1;
          month = 1;
          day = 1;
        }
      }
      else
      {
        int stringLength = 19;
        
        byte[] nanosAsBytes = Integer.toString(nanos).getBytes();
        
        stringLength += 1 + nanosAsBytes.length;
        
        byte[] datetimeAsBytes = new byte[stringLength];
        
        datetimeAsBytes[0] = ((byte)Character.forDigit(year / 1000, 10));
        
        int after1000 = year % 1000;
        
        datetimeAsBytes[1] = ((byte)Character.forDigit(after1000 / 100, 10));
        
        int after100 = after1000 % 100;
        
        datetimeAsBytes[2] = ((byte)Character.forDigit(after100 / 10, 10));
        
        datetimeAsBytes[3] = ((byte)Character.forDigit(after100 % 10, 10));
        
        datetimeAsBytes[4] = 45;
        
        datetimeAsBytes[5] = ((byte)Character.forDigit(month / 10, 10));
        
        datetimeAsBytes[6] = ((byte)Character.forDigit(month % 10, 10));
        
        datetimeAsBytes[7] = 45;
        
        datetimeAsBytes[8] = ((byte)Character.forDigit(day / 10, 10));
        
        datetimeAsBytes[9] = ((byte)Character.forDigit(day % 10, 10));
        
        datetimeAsBytes[10] = 32;
        
        datetimeAsBytes[11] = ((byte)Character.forDigit(hour / 10, 10));
        
        datetimeAsBytes[12] = ((byte)Character.forDigit(hour % 10, 10));
        
        datetimeAsBytes[13] = 58;
        
        datetimeAsBytes[14] = ((byte)Character.forDigit(minute / 10, 10));
        
        datetimeAsBytes[15] = ((byte)Character.forDigit(minute % 10, 10));
        
        datetimeAsBytes[16] = 58;
        
        datetimeAsBytes[17] = ((byte)Character.forDigit(seconds / 10, 10));
        
        datetimeAsBytes[18] = ((byte)Character.forDigit(seconds % 10, 10));
        
        datetimeAsBytes[19] = 46;
        
        int nanosOffset = 20;
        for (int j = 0; j < nanosAsBytes.length; j++) {
          datetimeAsBytes[(nanosOffset + j)] = nanosAsBytes[j];
        }
        unpackedRowData[columnIndex] = datetimeAsBytes;
      }
      break;
    case 0: 
    case 15: 
    case 16: 
    case 246: 
    case 249: 
    case 250: 
    case 251: 
    case 252: 
    case 253: 
    case 254: 
      unpackedRowData[columnIndex] = binaryData.readLenByteArray(0);
      
      break;
    default: 
      throw SQLError.createSQLException(Messages.getString("MysqlIO.97") + curField.getMysqlType() + Messages.getString("MysqlIO.98") + columnIndex + Messages.getString("MysqlIO.99") + fields.length + Messages.getString("MysqlIO.100"), "S1000", getExceptionInterceptor());
    }
  }
  
  private void negotiateSSLConnection(String user, String password, String database, int packLength)
    throws SQLException
  {
    if (!ExportControlled.enabled()) {
      throw new ConnectionFeatureNotAvailableException(this.connection, this.lastPacketSentTimeMs, null);
    }
    boolean doSecureAuth = false;
    if ((this.serverCapabilities & 0x8000) != 0)
    {
      this.clientParam |= 0x8000;
      doSecureAuth = true;
    }
    this.clientParam |= 0x800;
    
    Buffer packet = new Buffer(packLength);
    if (this.use41Extensions) {
      packet.writeLong(this.clientParam);
    } else {
      packet.writeInt((int)this.clientParam);
    }
    send(packet, packet.getPosition());
    
    ExportControlled.transformSocketToSSLSocket(this);
    
    packet.clear();
    if (doSecureAuth)
    {
      if (versionMeetsMinimum(4, 1, 1)) {
        secureAuth411(null, packLength, user, password, database, true);
      } else {
        secureAuth411(null, packLength, user, password, database, true);
      }
    }
    else
    {
      if (this.use41Extensions)
      {
        packet.writeLong(this.clientParam);
        packet.writeLong(this.maxThreeBytes);
      }
      else
      {
        packet.writeInt((int)this.clientParam);
        packet.writeLongInt(this.maxThreeBytes);
      }
      packet.writeString(user);
      if (this.protocolVersion > 9) {
        packet.writeString(Util.newCrypt(password, this.seed));
      } else {
        packet.writeString(Util.oldCrypt(password, this.seed));
      }
      if (((this.serverCapabilities & 0x8) != 0) && (database != null) && (database.length() > 0)) {
        packet.writeString(database);
      }
      send(packet, packet.getPosition());
    }
  }
  
  protected int getServerStatus()
  {
    return this.serverStatus;
  }
  
  protected List fetchRowsViaCursor(List fetchedRows, long statementId, Field[] columnTypes, int fetchSize, boolean useBufferRowExplicit)
    throws SQLException
  {
    if (fetchedRows == null) {
      fetchedRows = new ArrayList(fetchSize);
    } else {
      fetchedRows.clear();
    }
    this.sharedSendPacket.clear();
    
    this.sharedSendPacket.writeByte((byte)28);
    this.sharedSendPacket.writeLong(statementId);
    this.sharedSendPacket.writeLong(fetchSize);
    
    sendCommand(28, null, this.sharedSendPacket, true, null, 0);
    
    ResultSetRow row = null;
    while ((row = nextRow(columnTypes, columnTypes.length, true, 1007, false, useBufferRowExplicit, false, null)) != null) {
      fetchedRows.add(row);
    }
    return fetchedRows;
  }
  
  protected long getThreadId()
  {
    return this.threadId;
  }
  
  protected boolean useNanosForElapsedTime()
  {
    return this.useNanosForElapsedTime;
  }
  
  protected long getSlowQueryThreshold()
  {
    return this.slowQueryThreshold;
  }
  
  protected String getQueryTimingUnits()
  {
    return this.queryTimingUnits;
  }
  
  protected int getCommandCount()
  {
    return this.commandCount;
  }
  
  private void checkTransactionState(int oldStatus)
    throws SQLException
  {
    boolean previouslyInTrans = (oldStatus & 0x1) != 0;
    boolean currentlyInTrans = (this.serverStatus & 0x1) != 0;
    if ((previouslyInTrans) && (!currentlyInTrans)) {
      this.connection.transactionCompleted();
    } else if ((!previouslyInTrans) && (currentlyInTrans)) {
      this.connection.transactionBegun();
    }
  }
  
  protected void setStatementInterceptors(List statementInterceptors)
  {
    this.statementInterceptors = statementInterceptors;
  }
  
  protected ExceptionInterceptor getExceptionInterceptor()
  {
    return this.exceptionInterceptor;
  }
  
  /* Error */
  protected final void skipPacket()
    throws SQLException
  {
    // Byte code:
    //   0: aload_0
    //   1: aload_0
    //   2: getfield 8	com/mysql/jdbc/MysqlIO:mysqlInput	Ljava/io/InputStream;
    //   5: aload_0
    //   6: getfield 16	com/mysql/jdbc/MysqlIO:packetHeaderBuf	[B
    //   9: iconst_0
    //   10: iconst_4
    //   11: invokespecial 125	com/mysql/jdbc/MysqlIO:readFully	(Ljava/io/InputStream;[BII)I
    //   14: istore_1
    //   15: iload_1
    //   16: iconst_4
    //   17: if_icmpge +20 -> 37
    //   20: aload_0
    //   21: invokevirtual 126	com/mysql/jdbc/MysqlIO:forceClose	()V
    //   24: new 100	java/io/IOException
    //   27: dup
    //   28: ldc 127
    //   30: invokestatic 96	com/mysql/jdbc/Messages:getString	(Ljava/lang/String;)Ljava/lang/String;
    //   33: invokespecial 128	java/io/IOException:<init>	(Ljava/lang/String;)V
    //   36: athrow
    //   37: aload_0
    //   38: getfield 16	com/mysql/jdbc/MysqlIO:packetHeaderBuf	[B
    //   41: iconst_0
    //   42: baload
    //   43: sipush 255
    //   46: iand
    //   47: aload_0
    //   48: getfield 16	com/mysql/jdbc/MysqlIO:packetHeaderBuf	[B
    //   51: iconst_1
    //   52: baload
    //   53: sipush 255
    //   56: iand
    //   57: bipush 8
    //   59: ishl
    //   60: iadd
    //   61: aload_0
    //   62: getfield 16	com/mysql/jdbc/MysqlIO:packetHeaderBuf	[B
    //   65: iconst_2
    //   66: baload
    //   67: sipush 255
    //   70: iand
    //   71: bipush 16
    //   73: ishl
    //   74: iadd
    //   75: istore_2
    //   76: aload_0
    //   77: getfield 50	com/mysql/jdbc/MysqlIO:traceProtocol	Z
    //   80: ifeq +66 -> 146
    //   83: new 129	java/lang/StringBuffer
    //   86: dup
    //   87: invokespecial 130	java/lang/StringBuffer:<init>	()V
    //   90: astore_3
    //   91: aload_3
    //   92: ldc -125
    //   94: invokestatic 96	com/mysql/jdbc/Messages:getString	(Ljava/lang/String;)Ljava/lang/String;
    //   97: invokevirtual 132	java/lang/StringBuffer:append	(Ljava/lang/String;)Ljava/lang/StringBuffer;
    //   100: pop
    //   101: aload_3
    //   102: iload_2
    //   103: invokevirtual 133	java/lang/StringBuffer:append	(I)Ljava/lang/StringBuffer;
    //   106: pop
    //   107: aload_3
    //   108: ldc -122
    //   110: invokestatic 96	com/mysql/jdbc/Messages:getString	(Ljava/lang/String;)Ljava/lang/String;
    //   113: invokevirtual 132	java/lang/StringBuffer:append	(Ljava/lang/String;)Ljava/lang/StringBuffer;
    //   116: pop
    //   117: aload_3
    //   118: aload_0
    //   119: getfield 16	com/mysql/jdbc/MysqlIO:packetHeaderBuf	[B
    //   122: iconst_4
    //   123: invokestatic 135	com/mysql/jdbc/StringUtils:dumpAsHex	([BI)Ljava/lang/String;
    //   126: invokevirtual 132	java/lang/StringBuffer:append	(Ljava/lang/String;)Ljava/lang/StringBuffer;
    //   129: pop
    //   130: aload_0
    //   131: getfield 55	com/mysql/jdbc/MysqlIO:connection	Lcom/mysql/jdbc/ConnectionImpl;
    //   134: invokevirtual 77	com/mysql/jdbc/ConnectionImpl:getLog	()Lcom/mysql/jdbc/log/Log;
    //   137: aload_3
    //   138: invokevirtual 136	java/lang/StringBuffer:toString	()Ljava/lang/String;
    //   141: invokeinterface 137 2 0
    //   146: aload_0
    //   147: getfield 16	com/mysql/jdbc/MysqlIO:packetHeaderBuf	[B
    //   150: iconst_3
    //   151: baload
    //   152: istore_3
    //   153: aload_0
    //   154: getfield 2	com/mysql/jdbc/MysqlIO:packetSequenceReset	Z
    //   157: ifne +25 -> 182
    //   160: aload_0
    //   161: getfield 51	com/mysql/jdbc/MysqlIO:enablePacketDebug	Z
    //   164: ifeq +23 -> 187
    //   167: aload_0
    //   168: getfield 34	com/mysql/jdbc/MysqlIO:checkPacketSequence	Z
    //   171: ifeq +16 -> 187
    //   174: aload_0
    //   175: iload_3
    //   176: invokespecial 138	com/mysql/jdbc/MysqlIO:checkPacketSequencing	(B)V
    //   179: goto +8 -> 187
    //   182: aload_0
    //   183: iconst_0
    //   184: putfield 2	com/mysql/jdbc/MysqlIO:packetSequenceReset	Z
    //   187: aload_0
    //   188: iload_3
    //   189: putfield 33	com/mysql/jdbc/MysqlIO:readPacketSequence	B
    //   192: aload_0
    //   193: aload_0
    //   194: getfield 8	com/mysql/jdbc/MysqlIO:mysqlInput	Ljava/io/InputStream;
    //   197: iload_2
    //   198: i2l
    //   199: invokespecial 139	com/mysql/jdbc/MysqlIO:skipFully	(Ljava/io/InputStream;J)J
    //   202: pop2
    //   203: goto +43 -> 246
    //   206: astore_1
    //   207: aload_0
    //   208: getfield 55	com/mysql/jdbc/MysqlIO:connection	Lcom/mysql/jdbc/ConnectionImpl;
    //   211: aload_0
    //   212: getfield 48	com/mysql/jdbc/MysqlIO:lastPacketSentTimeMs	J
    //   215: aload_0
    //   216: getfield 49	com/mysql/jdbc/MysqlIO:lastPacketReceivedTimeMs	J
    //   219: aload_1
    //   220: aload_0
    //   221: invokevirtual 101	com/mysql/jdbc/MysqlIO:getExceptionInterceptor	()Lcom/mysql/jdbc/ExceptionInterceptor;
    //   224: invokestatic 102	com/mysql/jdbc/SQLError:createCommunicationsException	(Lcom/mysql/jdbc/ConnectionImpl;JJLjava/lang/Exception;Lcom/mysql/jdbc/ExceptionInterceptor;)Ljava/sql/SQLException;
    //   227: athrow
    //   228: astore_1
    //   229: aload_0
    //   230: getfield 55	com/mysql/jdbc/MysqlIO:connection	Lcom/mysql/jdbc/ConnectionImpl;
    //   233: iconst_0
    //   234: iconst_0
    //   235: iconst_1
    //   236: aload_1
    //   237: invokevirtual 141	com/mysql/jdbc/ConnectionImpl:realClose	(ZZZLjava/lang/Throwable;)V
    //   240: aload_1
    //   241: athrow
    //   242: astore 4
    //   244: aload_1
    //   245: athrow
    //   246: return
    // Line number table:
    //   Java source line #539	-> byte code offset #0
    //   Java source line #542	-> byte code offset #15
    //   Java source line #543	-> byte code offset #20
    //   Java source line #544	-> byte code offset #24
    //   Java source line #547	-> byte code offset #37
    //   Java source line #551	-> byte code offset #76
    //   Java source line #552	-> byte code offset #83
    //   Java source line #554	-> byte code offset #91
    //   Java source line #555	-> byte code offset #101
    //   Java source line #556	-> byte code offset #107
    //   Java source line #557	-> byte code offset #117
    //   Java source line #560	-> byte code offset #130
    //   Java source line #563	-> byte code offset #146
    //   Java source line #565	-> byte code offset #153
    //   Java source line #566	-> byte code offset #160
    //   Java source line #567	-> byte code offset #174
    //   Java source line #570	-> byte code offset #182
    //   Java source line #573	-> byte code offset #187
    //   Java source line #575	-> byte code offset #192
    //   Java source line #585	-> byte code offset #203
    //   Java source line #576	-> byte code offset #206
    //   Java source line #577	-> byte code offset #207
    //   Java source line #579	-> byte code offset #228
    //   Java source line #581	-> byte code offset #229
    //   Java source line #583	-> byte code offset #240
    //   Java source line #586	-> byte code offset #246
    // Local variable table:
    //   start	length	slot	name	signature
    //   0	247	0	this	MysqlIO
    //   14	2	1	lengthRead	int
    //   206	14	1	ioEx	IOException
    //   228	17	1	oom	OutOfMemoryError
    //   75	123	2	packetLength	int
    //   90	48	3	traceMessageBuf	StringBuffer
    //   152	37	3	multiPacketSeq	byte
    //   242	1	4	localObject	Object
    // Exception table:
    //   from	to	target	type
    //   0	203	206	java/io/IOException
    //   0	203	228	java/lang/OutOfMemoryError
    //   229	240	242	finally
    //   242	244	242	finally
  }
  
  /* Error */
  protected final Buffer readPacket()
    throws SQLException
  {
    // Byte code:
    //   0: aload_0
    //   1: aload_0
    //   2: getfield 8	com/mysql/jdbc/MysqlIO:mysqlInput	Ljava/io/InputStream;
    //   5: aload_0
    //   6: getfield 16	com/mysql/jdbc/MysqlIO:packetHeaderBuf	[B
    //   9: iconst_0
    //   10: iconst_4
    //   11: invokespecial 125	com/mysql/jdbc/MysqlIO:readFully	(Ljava/io/InputStream;[BII)I
    //   14: istore_1
    //   15: iload_1
    //   16: iconst_4
    //   17: if_icmpge +20 -> 37
    //   20: aload_0
    //   21: invokevirtual 126	com/mysql/jdbc/MysqlIO:forceClose	()V
    //   24: new 100	java/io/IOException
    //   27: dup
    //   28: ldc 127
    //   30: invokestatic 96	com/mysql/jdbc/Messages:getString	(Ljava/lang/String;)Ljava/lang/String;
    //   33: invokespecial 128	java/io/IOException:<init>	(Ljava/lang/String;)V
    //   36: athrow
    //   37: aload_0
    //   38: getfield 16	com/mysql/jdbc/MysqlIO:packetHeaderBuf	[B
    //   41: iconst_0
    //   42: baload
    //   43: sipush 255
    //   46: iand
    //   47: aload_0
    //   48: getfield 16	com/mysql/jdbc/MysqlIO:packetHeaderBuf	[B
    //   51: iconst_1
    //   52: baload
    //   53: sipush 255
    //   56: iand
    //   57: bipush 8
    //   59: ishl
    //   60: iadd
    //   61: aload_0
    //   62: getfield 16	com/mysql/jdbc/MysqlIO:packetHeaderBuf	[B
    //   65: iconst_2
    //   66: baload
    //   67: sipush 255
    //   70: iand
    //   71: bipush 16
    //   73: ishl
    //   74: iadd
    //   75: istore_2
    //   76: iload_2
    //   77: aload_0
    //   78: getfield 37	com/mysql/jdbc/MysqlIO:maxAllowedPacket	I
    //   81: if_icmple +18 -> 99
    //   84: new 142	com/mysql/jdbc/PacketTooBigException
    //   87: dup
    //   88: iload_2
    //   89: i2l
    //   90: aload_0
    //   91: getfield 37	com/mysql/jdbc/MysqlIO:maxAllowedPacket	I
    //   94: i2l
    //   95: invokespecial 143	com/mysql/jdbc/PacketTooBigException:<init>	(JJ)V
    //   98: athrow
    //   99: aload_0
    //   100: getfield 50	com/mysql/jdbc/MysqlIO:traceProtocol	Z
    //   103: ifeq +66 -> 169
    //   106: new 129	java/lang/StringBuffer
    //   109: dup
    //   110: invokespecial 130	java/lang/StringBuffer:<init>	()V
    //   113: astore_3
    //   114: aload_3
    //   115: ldc -125
    //   117: invokestatic 96	com/mysql/jdbc/Messages:getString	(Ljava/lang/String;)Ljava/lang/String;
    //   120: invokevirtual 132	java/lang/StringBuffer:append	(Ljava/lang/String;)Ljava/lang/StringBuffer;
    //   123: pop
    //   124: aload_3
    //   125: iload_2
    //   126: invokevirtual 133	java/lang/StringBuffer:append	(I)Ljava/lang/StringBuffer;
    //   129: pop
    //   130: aload_3
    //   131: ldc -122
    //   133: invokestatic 96	com/mysql/jdbc/Messages:getString	(Ljava/lang/String;)Ljava/lang/String;
    //   136: invokevirtual 132	java/lang/StringBuffer:append	(Ljava/lang/String;)Ljava/lang/StringBuffer;
    //   139: pop
    //   140: aload_3
    //   141: aload_0
    //   142: getfield 16	com/mysql/jdbc/MysqlIO:packetHeaderBuf	[B
    //   145: iconst_4
    //   146: invokestatic 135	com/mysql/jdbc/StringUtils:dumpAsHex	([BI)Ljava/lang/String;
    //   149: invokevirtual 132	java/lang/StringBuffer:append	(Ljava/lang/String;)Ljava/lang/StringBuffer;
    //   152: pop
    //   153: aload_0
    //   154: getfield 55	com/mysql/jdbc/MysqlIO:connection	Lcom/mysql/jdbc/ConnectionImpl;
    //   157: invokevirtual 77	com/mysql/jdbc/ConnectionImpl:getLog	()Lcom/mysql/jdbc/log/Log;
    //   160: aload_3
    //   161: invokevirtual 136	java/lang/StringBuffer:toString	()Ljava/lang/String;
    //   164: invokeinterface 137 2 0
    //   169: aload_0
    //   170: getfield 16	com/mysql/jdbc/MysqlIO:packetHeaderBuf	[B
    //   173: iconst_3
    //   174: baload
    //   175: istore_3
    //   176: aload_0
    //   177: getfield 2	com/mysql/jdbc/MysqlIO:packetSequenceReset	Z
    //   180: ifne +25 -> 205
    //   183: aload_0
    //   184: getfield 51	com/mysql/jdbc/MysqlIO:enablePacketDebug	Z
    //   187: ifeq +23 -> 210
    //   190: aload_0
    //   191: getfield 34	com/mysql/jdbc/MysqlIO:checkPacketSequence	Z
    //   194: ifeq +16 -> 210
    //   197: aload_0
    //   198: iload_3
    //   199: invokespecial 138	com/mysql/jdbc/MysqlIO:checkPacketSequencing	(B)V
    //   202: goto +8 -> 210
    //   205: aload_0
    //   206: iconst_0
    //   207: putfield 2	com/mysql/jdbc/MysqlIO:packetSequenceReset	Z
    //   210: aload_0
    //   211: iload_3
    //   212: putfield 33	com/mysql/jdbc/MysqlIO:readPacketSequence	B
    //   215: iload_2
    //   216: iconst_1
    //   217: iadd
    //   218: newarray <illegal type>
    //   220: astore 4
    //   222: aload_0
    //   223: aload_0
    //   224: getfield 8	com/mysql/jdbc/MysqlIO:mysqlInput	Ljava/io/InputStream;
    //   227: aload 4
    //   229: iconst_0
    //   230: iload_2
    //   231: invokespecial 125	com/mysql/jdbc/MysqlIO:readFully	(Ljava/io/InputStream;[BII)I
    //   234: istore 5
    //   236: iload 5
    //   238: iload_2
    //   239: if_icmpeq +40 -> 279
    //   242: new 100	java/io/IOException
    //   245: dup
    //   246: new 129	java/lang/StringBuffer
    //   249: dup
    //   250: invokespecial 130	java/lang/StringBuffer:<init>	()V
    //   253: ldc -112
    //   255: invokevirtual 132	java/lang/StringBuffer:append	(Ljava/lang/String;)Ljava/lang/StringBuffer;
    //   258: iload_2
    //   259: invokevirtual 133	java/lang/StringBuffer:append	(I)Ljava/lang/StringBuffer;
    //   262: ldc -111
    //   264: invokevirtual 132	java/lang/StringBuffer:append	(Ljava/lang/String;)Ljava/lang/StringBuffer;
    //   267: iload 5
    //   269: invokevirtual 133	java/lang/StringBuffer:append	(I)Ljava/lang/StringBuffer;
    //   272: invokevirtual 136	java/lang/StringBuffer:toString	()Ljava/lang/String;
    //   275: invokespecial 128	java/io/IOException:<init>	(Ljava/lang/String;)V
    //   278: athrow
    //   279: aload 4
    //   281: iload_2
    //   282: iconst_0
    //   283: bastore
    //   284: new 65	com/mysql/jdbc/Buffer
    //   287: dup
    //   288: aload 4
    //   290: invokespecial 146	com/mysql/jdbc/Buffer:<init>	([B)V
    //   293: astore 6
    //   295: aload 6
    //   297: iload_2
    //   298: iconst_1
    //   299: iadd
    //   300: invokevirtual 147	com/mysql/jdbc/Buffer:setBufLength	(I)V
    //   303: aload_0
    //   304: getfield 50	com/mysql/jdbc/MysqlIO:traceProtocol	Z
    //   307: ifeq +52 -> 359
    //   310: new 129	java/lang/StringBuffer
    //   313: dup
    //   314: invokespecial 130	java/lang/StringBuffer:<init>	()V
    //   317: astore 7
    //   319: aload 7
    //   321: ldc -108
    //   323: invokestatic 96	com/mysql/jdbc/Messages:getString	(Ljava/lang/String;)Ljava/lang/String;
    //   326: invokevirtual 132	java/lang/StringBuffer:append	(Ljava/lang/String;)Ljava/lang/StringBuffer;
    //   329: pop
    //   330: aload 7
    //   332: aload 6
    //   334: iload_2
    //   335: invokestatic 149	com/mysql/jdbc/MysqlIO:getPacketDumpToLog	(Lcom/mysql/jdbc/Buffer;I)Ljava/lang/String;
    //   338: invokevirtual 132	java/lang/StringBuffer:append	(Ljava/lang/String;)Ljava/lang/StringBuffer;
    //   341: pop
    //   342: aload_0
    //   343: getfield 55	com/mysql/jdbc/MysqlIO:connection	Lcom/mysql/jdbc/ConnectionImpl;
    //   346: invokevirtual 77	com/mysql/jdbc/ConnectionImpl:getLog	()Lcom/mysql/jdbc/log/Log;
    //   349: aload 7
    //   351: invokevirtual 136	java/lang/StringBuffer:toString	()Ljava/lang/String;
    //   354: invokeinterface 137 2 0
    //   359: aload_0
    //   360: getfield 51	com/mysql/jdbc/MysqlIO:enablePacketDebug	Z
    //   363: ifeq +16 -> 379
    //   366: aload_0
    //   367: iconst_0
    //   368: iconst_0
    //   369: iconst_0
    //   370: aload_0
    //   371: getfield 16	com/mysql/jdbc/MysqlIO:packetHeaderBuf	[B
    //   374: aload 6
    //   376: invokespecial 150	com/mysql/jdbc/MysqlIO:enqueuePacketForDebugging	(ZZI[BLcom/mysql/jdbc/Buffer;)V
    //   379: aload_0
    //   380: getfield 55	com/mysql/jdbc/MysqlIO:connection	Lcom/mysql/jdbc/ConnectionImpl;
    //   383: invokevirtual 151	com/mysql/jdbc/ConnectionImpl:getMaintainTimeStats	()Z
    //   386: ifeq +10 -> 396
    //   389: aload_0
    //   390: invokestatic 152	java/lang/System:currentTimeMillis	()J
    //   393: putfield 49	com/mysql/jdbc/MysqlIO:lastPacketReceivedTimeMs	J
    //   396: aload 6
    //   398: areturn
    //   399: astore_1
    //   400: aload_0
    //   401: getfield 55	com/mysql/jdbc/MysqlIO:connection	Lcom/mysql/jdbc/ConnectionImpl;
    //   404: aload_0
    //   405: getfield 48	com/mysql/jdbc/MysqlIO:lastPacketSentTimeMs	J
    //   408: aload_0
    //   409: getfield 49	com/mysql/jdbc/MysqlIO:lastPacketReceivedTimeMs	J
    //   412: aload_1
    //   413: aload_0
    //   414: invokevirtual 101	com/mysql/jdbc/MysqlIO:getExceptionInterceptor	()Lcom/mysql/jdbc/ExceptionInterceptor;
    //   417: invokestatic 102	com/mysql/jdbc/SQLError:createCommunicationsException	(Lcom/mysql/jdbc/ConnectionImpl;JJLjava/lang/Exception;Lcom/mysql/jdbc/ExceptionInterceptor;)Ljava/sql/SQLException;
    //   420: athrow
    //   421: astore_1
    //   422: aload_0
    //   423: getfield 55	com/mysql/jdbc/MysqlIO:connection	Lcom/mysql/jdbc/ConnectionImpl;
    //   426: iconst_0
    //   427: iconst_0
    //   428: iconst_1
    //   429: aload_1
    //   430: invokevirtual 141	com/mysql/jdbc/ConnectionImpl:realClose	(ZZZLjava/lang/Throwable;)V
    //   433: aload_1
    //   434: athrow
    //   435: astore 8
    //   437: aload_1
    //   438: athrow
    // Line number table:
    //   Java source line #599	-> byte code offset #0
    //   Java source line #602	-> byte code offset #15
    //   Java source line #603	-> byte code offset #20
    //   Java source line #604	-> byte code offset #24
    //   Java source line #607	-> byte code offset #37
    //   Java source line #611	-> byte code offset #76
    //   Java source line #612	-> byte code offset #84
    //   Java source line #615	-> byte code offset #99
    //   Java source line #616	-> byte code offset #106
    //   Java source line #618	-> byte code offset #114
    //   Java source line #619	-> byte code offset #124
    //   Java source line #620	-> byte code offset #130
    //   Java source line #621	-> byte code offset #140
    //   Java source line #624	-> byte code offset #153
    //   Java source line #627	-> byte code offset #169
    //   Java source line #629	-> byte code offset #176
    //   Java source line #630	-> byte code offset #183
    //   Java source line #631	-> byte code offset #197
    //   Java source line #634	-> byte code offset #205
    //   Java source line #637	-> byte code offset #210
    //   Java source line #640	-> byte code offset #215
    //   Java source line #641	-> byte code offset #222
    //   Java source line #644	-> byte code offset #236
    //   Java source line #645	-> byte code offset #242
    //   Java source line #649	-> byte code offset #279
    //   Java source line #651	-> byte code offset #284
    //   Java source line #652	-> byte code offset #295
    //   Java source line #654	-> byte code offset #303
    //   Java source line #655	-> byte code offset #310
    //   Java source line #657	-> byte code offset #319
    //   Java source line #658	-> byte code offset #330
    //   Java source line #661	-> byte code offset #342
    //   Java source line #664	-> byte code offset #359
    //   Java source line #665	-> byte code offset #366
    //   Java source line #669	-> byte code offset #379
    //   Java source line #670	-> byte code offset #389
    //   Java source line #673	-> byte code offset #396
    //   Java source line #674	-> byte code offset #399
    //   Java source line #675	-> byte code offset #400
    //   Java source line #677	-> byte code offset #421
    //   Java source line #679	-> byte code offset #422
    //   Java source line #681	-> byte code offset #433
    // Local variable table:
    //   start	length	slot	name	signature
    //   0	439	0	this	MysqlIO
    //   14	2	1	lengthRead	int
    //   399	14	1	ioEx	IOException
    //   421	17	1	oom	OutOfMemoryError
    //   75	260	2	packetLength	int
    //   113	48	3	traceMessageBuf	StringBuffer
    //   175	37	3	multiPacketSeq	byte
    //   220	69	4	buffer	byte[]
    //   234	34	5	numBytesRead	int
    //   293	104	6	packet	Buffer
    //   317	33	7	traceMessageBuf	StringBuffer
    //   435	1	8	localObject	Object
    // Exception table:
    //   from	to	target	type
    //   0	398	399	java/io/IOException
    //   0	398	421	java/lang/OutOfMemoryError
    //   422	433	435	finally
    //   435	437	435	finally
  }
  
  /* Error */
  private final Buffer reuseAndReadPacket(Buffer reuse, int existingPacketLength)
    throws SQLException
  {
    // Byte code:
    //   0: aload_1
    //   1: iconst_0
    //   2: invokevirtual 480	com/mysql/jdbc/Buffer:setWasMultiPacket	(Z)V
    //   5: iconst_0
    //   6: istore_3
    //   7: iload_2
    //   8: iconst_m1
    //   9: if_icmpne +85 -> 94
    //   12: aload_0
    //   13: aload_0
    //   14: getfield 8	com/mysql/jdbc/MysqlIO:mysqlInput	Ljava/io/InputStream;
    //   17: aload_0
    //   18: getfield 16	com/mysql/jdbc/MysqlIO:packetHeaderBuf	[B
    //   21: iconst_0
    //   22: iconst_4
    //   23: invokespecial 125	com/mysql/jdbc/MysqlIO:readFully	(Ljava/io/InputStream;[BII)I
    //   26: istore 4
    //   28: iload 4
    //   30: iconst_4
    //   31: if_icmpge +21 -> 52
    //   34: aload_0
    //   35: invokevirtual 126	com/mysql/jdbc/MysqlIO:forceClose	()V
    //   38: new 100	java/io/IOException
    //   41: dup
    //   42: ldc_w 298
    //   45: invokestatic 96	com/mysql/jdbc/Messages:getString	(Ljava/lang/String;)Ljava/lang/String;
    //   48: invokespecial 128	java/io/IOException:<init>	(Ljava/lang/String;)V
    //   51: athrow
    //   52: aload_0
    //   53: getfield 16	com/mysql/jdbc/MysqlIO:packetHeaderBuf	[B
    //   56: iconst_0
    //   57: baload
    //   58: sipush 255
    //   61: iand
    //   62: aload_0
    //   63: getfield 16	com/mysql/jdbc/MysqlIO:packetHeaderBuf	[B
    //   66: iconst_1
    //   67: baload
    //   68: sipush 255
    //   71: iand
    //   72: bipush 8
    //   74: ishl
    //   75: iadd
    //   76: aload_0
    //   77: getfield 16	com/mysql/jdbc/MysqlIO:packetHeaderBuf	[B
    //   80: iconst_2
    //   81: baload
    //   82: sipush 255
    //   85: iand
    //   86: bipush 16
    //   88: ishl
    //   89: iadd
    //   90: istore_3
    //   91: goto +5 -> 96
    //   94: iload_2
    //   95: istore_3
    //   96: aload_0
    //   97: getfield 50	com/mysql/jdbc/MysqlIO:traceProtocol	Z
    //   100: ifeq +74 -> 174
    //   103: new 129	java/lang/StringBuffer
    //   106: dup
    //   107: invokespecial 130	java/lang/StringBuffer:<init>	()V
    //   110: astore 4
    //   112: aload 4
    //   114: ldc_w 481
    //   117: invokestatic 96	com/mysql/jdbc/Messages:getString	(Ljava/lang/String;)Ljava/lang/String;
    //   120: invokevirtual 132	java/lang/StringBuffer:append	(Ljava/lang/String;)Ljava/lang/StringBuffer;
    //   123: pop
    //   124: aload 4
    //   126: iload_3
    //   127: invokevirtual 133	java/lang/StringBuffer:append	(I)Ljava/lang/StringBuffer;
    //   130: pop
    //   131: aload 4
    //   133: ldc_w 482
    //   136: invokestatic 96	com/mysql/jdbc/Messages:getString	(Ljava/lang/String;)Ljava/lang/String;
    //   139: invokevirtual 132	java/lang/StringBuffer:append	(Ljava/lang/String;)Ljava/lang/StringBuffer;
    //   142: pop
    //   143: aload 4
    //   145: aload_0
    //   146: getfield 16	com/mysql/jdbc/MysqlIO:packetHeaderBuf	[B
    //   149: iconst_4
    //   150: invokestatic 135	com/mysql/jdbc/StringUtils:dumpAsHex	([BI)Ljava/lang/String;
    //   153: invokevirtual 132	java/lang/StringBuffer:append	(Ljava/lang/String;)Ljava/lang/StringBuffer;
    //   156: pop
    //   157: aload_0
    //   158: getfield 55	com/mysql/jdbc/MysqlIO:connection	Lcom/mysql/jdbc/ConnectionImpl;
    //   161: invokevirtual 77	com/mysql/jdbc/ConnectionImpl:getLog	()Lcom/mysql/jdbc/log/Log;
    //   164: aload 4
    //   166: invokevirtual 136	java/lang/StringBuffer:toString	()Ljava/lang/String;
    //   169: invokeinterface 137 2 0
    //   174: aload_0
    //   175: getfield 16	com/mysql/jdbc/MysqlIO:packetHeaderBuf	[B
    //   178: iconst_3
    //   179: baload
    //   180: istore 4
    //   182: aload_0
    //   183: getfield 2	com/mysql/jdbc/MysqlIO:packetSequenceReset	Z
    //   186: ifne +26 -> 212
    //   189: aload_0
    //   190: getfield 51	com/mysql/jdbc/MysqlIO:enablePacketDebug	Z
    //   193: ifeq +24 -> 217
    //   196: aload_0
    //   197: getfield 34	com/mysql/jdbc/MysqlIO:checkPacketSequence	Z
    //   200: ifeq +17 -> 217
    //   203: aload_0
    //   204: iload 4
    //   206: invokespecial 138	com/mysql/jdbc/MysqlIO:checkPacketSequencing	(B)V
    //   209: goto +8 -> 217
    //   212: aload_0
    //   213: iconst_0
    //   214: putfield 2	com/mysql/jdbc/MysqlIO:packetSequenceReset	Z
    //   217: aload_0
    //   218: iload 4
    //   220: putfield 33	com/mysql/jdbc/MysqlIO:readPacketSequence	B
    //   223: aload_1
    //   224: iconst_0
    //   225: invokevirtual 233	com/mysql/jdbc/Buffer:setPosition	(I)V
    //   228: aload_1
    //   229: invokevirtual 160	com/mysql/jdbc/Buffer:getByteBuffer	()[B
    //   232: arraylength
    //   233: iload_3
    //   234: if_icmpgt +12 -> 246
    //   237: aload_1
    //   238: iload_3
    //   239: iconst_1
    //   240: iadd
    //   241: newarray <illegal type>
    //   243: invokevirtual 483	com/mysql/jdbc/Buffer:setByteBuffer	([B)V
    //   246: aload_1
    //   247: iload_3
    //   248: invokevirtual 147	com/mysql/jdbc/Buffer:setBufLength	(I)V
    //   251: aload_0
    //   252: aload_0
    //   253: getfield 8	com/mysql/jdbc/MysqlIO:mysqlInput	Ljava/io/InputStream;
    //   256: aload_1
    //   257: invokevirtual 160	com/mysql/jdbc/Buffer:getByteBuffer	()[B
    //   260: iconst_0
    //   261: iload_3
    //   262: invokespecial 125	com/mysql/jdbc/MysqlIO:readFully	(Ljava/io/InputStream;[BII)I
    //   265: istore 5
    //   267: iload 5
    //   269: iload_3
    //   270: if_icmpeq +40 -> 310
    //   273: new 100	java/io/IOException
    //   276: dup
    //   277: new 129	java/lang/StringBuffer
    //   280: dup
    //   281: invokespecial 130	java/lang/StringBuffer:<init>	()V
    //   284: ldc -112
    //   286: invokevirtual 132	java/lang/StringBuffer:append	(Ljava/lang/String;)Ljava/lang/StringBuffer;
    //   289: iload_3
    //   290: invokevirtual 133	java/lang/StringBuffer:append	(I)Ljava/lang/StringBuffer;
    //   293: ldc -111
    //   295: invokevirtual 132	java/lang/StringBuffer:append	(Ljava/lang/String;)Ljava/lang/StringBuffer;
    //   298: iload 5
    //   300: invokevirtual 133	java/lang/StringBuffer:append	(I)Ljava/lang/StringBuffer;
    //   303: invokevirtual 136	java/lang/StringBuffer:toString	()Ljava/lang/String;
    //   306: invokespecial 128	java/io/IOException:<init>	(Ljava/lang/String;)V
    //   309: athrow
    //   310: aload_0
    //   311: getfield 50	com/mysql/jdbc/MysqlIO:traceProtocol	Z
    //   314: ifeq +52 -> 366
    //   317: new 129	java/lang/StringBuffer
    //   320: dup
    //   321: invokespecial 130	java/lang/StringBuffer:<init>	()V
    //   324: astore 6
    //   326: aload 6
    //   328: ldc_w 484
    //   331: invokestatic 96	com/mysql/jdbc/Messages:getString	(Ljava/lang/String;)Ljava/lang/String;
    //   334: invokevirtual 132	java/lang/StringBuffer:append	(Ljava/lang/String;)Ljava/lang/StringBuffer;
    //   337: pop
    //   338: aload 6
    //   340: aload_1
    //   341: iload_3
    //   342: invokestatic 149	com/mysql/jdbc/MysqlIO:getPacketDumpToLog	(Lcom/mysql/jdbc/Buffer;I)Ljava/lang/String;
    //   345: invokevirtual 132	java/lang/StringBuffer:append	(Ljava/lang/String;)Ljava/lang/StringBuffer;
    //   348: pop
    //   349: aload_0
    //   350: getfield 55	com/mysql/jdbc/MysqlIO:connection	Lcom/mysql/jdbc/ConnectionImpl;
    //   353: invokevirtual 77	com/mysql/jdbc/ConnectionImpl:getLog	()Lcom/mysql/jdbc/log/Log;
    //   356: aload 6
    //   358: invokevirtual 136	java/lang/StringBuffer:toString	()Ljava/lang/String;
    //   361: invokeinterface 137 2 0
    //   366: aload_0
    //   367: getfield 51	com/mysql/jdbc/MysqlIO:enablePacketDebug	Z
    //   370: ifeq +15 -> 385
    //   373: aload_0
    //   374: iconst_0
    //   375: iconst_1
    //   376: iconst_0
    //   377: aload_0
    //   378: getfield 16	com/mysql/jdbc/MysqlIO:packetHeaderBuf	[B
    //   381: aload_1
    //   382: invokespecial 150	com/mysql/jdbc/MysqlIO:enqueuePacketForDebugging	(ZZI[BLcom/mysql/jdbc/Buffer;)V
    //   385: iconst_0
    //   386: istore 6
    //   388: iload_3
    //   389: aload_0
    //   390: getfield 39	com/mysql/jdbc/MysqlIO:maxThreeBytes	I
    //   393: if_icmpne +27 -> 420
    //   396: aload_1
    //   397: aload_0
    //   398: getfield 39	com/mysql/jdbc/MysqlIO:maxThreeBytes	I
    //   401: invokevirtual 233	com/mysql/jdbc/Buffer:setPosition	(I)V
    //   404: iload_3
    //   405: istore 7
    //   407: iconst_1
    //   408: istore 6
    //   410: aload_0
    //   411: aload_1
    //   412: iload 4
    //   414: iload 7
    //   416: invokespecial 485	com/mysql/jdbc/MysqlIO:readRemainingMultiPackets	(Lcom/mysql/jdbc/Buffer;BI)I
    //   419: istore_3
    //   420: iload 6
    //   422: ifne +10 -> 432
    //   425: aload_1
    //   426: invokevirtual 160	com/mysql/jdbc/Buffer:getByteBuffer	()[B
    //   429: iload_3
    //   430: iconst_0
    //   431: bastore
    //   432: aload_0
    //   433: getfield 55	com/mysql/jdbc/MysqlIO:connection	Lcom/mysql/jdbc/ConnectionImpl;
    //   436: invokevirtual 151	com/mysql/jdbc/ConnectionImpl:getMaintainTimeStats	()Z
    //   439: ifeq +10 -> 449
    //   442: aload_0
    //   443: invokestatic 152	java/lang/System:currentTimeMillis	()J
    //   446: putfield 49	com/mysql/jdbc/MysqlIO:lastPacketReceivedTimeMs	J
    //   449: aload_1
    //   450: areturn
    //   451: astore_3
    //   452: aload_0
    //   453: getfield 55	com/mysql/jdbc/MysqlIO:connection	Lcom/mysql/jdbc/ConnectionImpl;
    //   456: aload_0
    //   457: getfield 48	com/mysql/jdbc/MysqlIO:lastPacketSentTimeMs	J
    //   460: aload_0
    //   461: getfield 49	com/mysql/jdbc/MysqlIO:lastPacketReceivedTimeMs	J
    //   464: aload_3
    //   465: aload_0
    //   466: invokevirtual 101	com/mysql/jdbc/MysqlIO:getExceptionInterceptor	()Lcom/mysql/jdbc/ExceptionInterceptor;
    //   469: invokestatic 102	com/mysql/jdbc/SQLError:createCommunicationsException	(Lcom/mysql/jdbc/ConnectionImpl;JJLjava/lang/Exception;Lcom/mysql/jdbc/ExceptionInterceptor;)Ljava/sql/SQLException;
    //   472: athrow
    //   473: astore_3
    //   474: aload_0
    //   475: invokevirtual 329	com/mysql/jdbc/MysqlIO:clearInputStream	()V
    //   478: jsr +14 -> 492
    //   481: goto +30 -> 511
    //   484: astore 8
    //   486: jsr +6 -> 492
    //   489: aload 8
    //   491: athrow
    //   492: astore 9
    //   494: aload_0
    //   495: getfield 55	com/mysql/jdbc/MysqlIO:connection	Lcom/mysql/jdbc/ConnectionImpl;
    //   498: iconst_0
    //   499: iconst_0
    //   500: iconst_1
    //   501: aload_3
    //   502: invokevirtual 141	com/mysql/jdbc/ConnectionImpl:realClose	(ZZZLjava/lang/Throwable;)V
    //   505: aload_3
    //   506: athrow
    //   507: astore 10
    //   509: aload_3
    //   510: athrow
    //   511: goto +0 -> 511
    // Line number table:
    //   Java source line #2945	-> byte code offset #0
    //   Java source line #2946	-> byte code offset #5
    //   Java source line #2948	-> byte code offset #7
    //   Java source line #2949	-> byte code offset #12
    //   Java source line #2952	-> byte code offset #28
    //   Java source line #2953	-> byte code offset #34
    //   Java source line #2954	-> byte code offset #38
    //   Java source line #2957	-> byte code offset #52
    //   Java source line #2961	-> byte code offset #94
    //   Java source line #2964	-> byte code offset #96
    //   Java source line #2965	-> byte code offset #103
    //   Java source line #2967	-> byte code offset #112
    //   Java source line #2968	-> byte code offset #124
    //   Java source line #2969	-> byte code offset #131
    //   Java source line #2970	-> byte code offset #143
    //   Java source line #2973	-> byte code offset #157
    //   Java source line #2976	-> byte code offset #174
    //   Java source line #2978	-> byte code offset #182
    //   Java source line #2979	-> byte code offset #189
    //   Java source line #2980	-> byte code offset #203
    //   Java source line #2983	-> byte code offset #212
    //   Java source line #2986	-> byte code offset #217
    //   Java source line #2989	-> byte code offset #223
    //   Java source line #2997	-> byte code offset #228
    //   Java source line #2998	-> byte code offset #237
    //   Java source line #3002	-> byte code offset #246
    //   Java source line #3005	-> byte code offset #251
    //   Java source line #3008	-> byte code offset #267
    //   Java source line #3009	-> byte code offset #273
    //   Java source line #3013	-> byte code offset #310
    //   Java source line #3014	-> byte code offset #317
    //   Java source line #3016	-> byte code offset #326
    //   Java source line #3017	-> byte code offset #338
    //   Java source line #3020	-> byte code offset #349
    //   Java source line #3023	-> byte code offset #366
    //   Java source line #3024	-> byte code offset #373
    //   Java source line #3028	-> byte code offset #385
    //   Java source line #3030	-> byte code offset #388
    //   Java source line #3031	-> byte code offset #396
    //   Java source line #3033	-> byte code offset #404
    //   Java source line #3036	-> byte code offset #407
    //   Java source line #3038	-> byte code offset #410
    //   Java source line #3042	-> byte code offset #420
    //   Java source line #3043	-> byte code offset #425
    //   Java source line #3046	-> byte code offset #432
    //   Java source line #3047	-> byte code offset #442
    //   Java source line #3050	-> byte code offset #449
    //   Java source line #3051	-> byte code offset #451
    //   Java source line #3052	-> byte code offset #452
    //   Java source line #3054	-> byte code offset #473
    //   Java source line #3057	-> byte code offset #474
    //   Java source line #3058	-> byte code offset #478
    //   Java source line #3064	-> byte code offset #481
    //   Java source line #3059	-> byte code offset #484
    //   Java source line #3060	-> byte code offset #494
    //   Java source line #3062	-> byte code offset #505
    //   Java source line #3067	-> byte code offset #511
    // Local variable table:
    //   start	length	slot	name	signature
    //   0	514	0	this	MysqlIO
    //   0	514	1	reuse	Buffer
    //   0	514	2	existingPacketLength	int
    //   6	424	3	packetLength	int
    //   451	14	3	ioEx	IOException
    //   473	37	3	oom	OutOfMemoryError
    //   26	3	4	lengthRead	int
    //   110	55	4	traceMessageBuf	StringBuffer
    //   180	233	4	multiPacketSeq	byte
    //   265	34	5	numBytesRead	int
    //   324	33	6	traceMessageBuf	StringBuffer
    //   386	35	6	isMultiPacket	boolean
    //   405	10	7	packetEndPoint	int
    //   484	6	8	localObject1	Object
    //   492	1	9	localObject2	Object
    //   507	1	10	localObject3	Object
    // Exception table:
    //   from	to	target	type
    //   0	450	451	java/io/IOException
    //   0	450	473	java/lang/OutOfMemoryError
    //   474	481	484	finally
    //   484	489	484	finally
    //   494	505	507	finally
    //   507	509	507	finally
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\mysql\jdbc\MysqlIO.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */