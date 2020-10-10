package com.mysql.jdbc;

import java.io.InputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.ParameterMetaData;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class CallableStatement
  extends PreparedStatement
  implements java.sql.CallableStatement
{
  protected static final Constructor JDBC_4_CSTMT_2_ARGS_CTOR;
  protected static final Constructor JDBC_4_CSTMT_4_ARGS_CTOR;
  private static final int NOT_OUTPUT_PARAMETER_INDICATOR = Integer.MIN_VALUE;
  private static final String PARAMETER_NAMESPACE_PREFIX = "@com_mysql_jdbc_outparam_";
  
  static
  {
    if (Util.isJdbc4())
    {
      try
      {
        JDBC_4_CSTMT_2_ARGS_CTOR = Class.forName("com.mysql.jdbc.JDBC4CallableStatement").getConstructor(new Class[] { ConnectionImpl.class, CallableStatementParamInfo.class });
        
        JDBC_4_CSTMT_4_ARGS_CTOR = Class.forName("com.mysql.jdbc.JDBC4CallableStatement").getConstructor(new Class[] { ConnectionImpl.class, String.class, String.class, Boolean.TYPE });
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
      JDBC_4_CSTMT_4_ARGS_CTOR = null;
      JDBC_4_CSTMT_2_ARGS_CTOR = null;
    }
  }
  
  protected class CallableStatementParam
  {
    int desiredJdbcType;
    int index;
    int inOutModifier;
    boolean isIn;
    boolean isOut;
    int jdbcType;
    short nullability;
    String paramName;
    int precision;
    int scale;
    String typeName;
    
    CallableStatementParam(String name, int idx, boolean in, boolean out, int jdbcType, String typeName, int precision, int scale, short nullability, int inOutModifier)
    {
      this.paramName = name;
      this.isIn = in;
      this.isOut = out;
      this.index = idx;
      
      this.jdbcType = jdbcType;
      this.typeName = typeName;
      this.precision = precision;
      this.scale = scale;
      this.nullability = nullability;
      this.inOutModifier = inOutModifier;
    }
    
    protected Object clone()
      throws CloneNotSupportedException
    {
      return super.clone();
    }
  }
  
  protected class CallableStatementParamInfo
  {
    String catalogInUse;
    boolean isFunctionCall;
    String nativeSql;
    int numParameters;
    List parameterList;
    Map parameterMap;
    boolean isReadOnlySafeProcedure = false;
    boolean isReadOnlySafeChecked = false;
    
    CallableStatementParamInfo(CallableStatementParamInfo fullParamInfo)
    {
      this.nativeSql = CallableStatement.this.originalSql;
      this.catalogInUse = CallableStatement.this.currentCatalog;
      this.isFunctionCall = fullParamInfo.isFunctionCall;
      int[] localParameterMap = CallableStatement.this.placeholderToParameterIndexMap;
      int parameterMapLength = localParameterMap.length;
      
      this.isReadOnlySafeProcedure = fullParamInfo.isReadOnlySafeProcedure;
      this.isReadOnlySafeChecked = fullParamInfo.isReadOnlySafeChecked;
      this.parameterList = new ArrayList(fullParamInfo.numParameters);
      this.parameterMap = new HashMap(fullParamInfo.numParameters);
      if (this.isFunctionCall) {
        this.parameterList.add(fullParamInfo.parameterList.get(0));
      }
      int offset = this.isFunctionCall ? 1 : 0;
      for (int i = 0; i < parameterMapLength; i++) {
        if (localParameterMap[i] != 0)
        {
          CallableStatement.CallableStatementParam param = (CallableStatement.CallableStatementParam)fullParamInfo.parameterList.get(localParameterMap[i] + offset);
          
          this.parameterList.add(param);
          this.parameterMap.put(param.paramName, param);
        }
      }
      this.numParameters = this.parameterList.size();
    }
    
    CallableStatementParamInfo(ResultSet paramTypesRs)
      throws SQLException
    {
      boolean hadRows = paramTypesRs.last();
      
      this.nativeSql = CallableStatement.this.originalSql;
      this.catalogInUse = CallableStatement.this.currentCatalog;
      this.isFunctionCall = CallableStatement.this.callingStoredFunction;
      if (hadRows)
      {
        this.numParameters = paramTypesRs.getRow();
        
        this.parameterList = new ArrayList(this.numParameters);
        this.parameterMap = new HashMap(this.numParameters);
        
        paramTypesRs.beforeFirst();
        
        addParametersFromDBMD(paramTypesRs);
      }
      else
      {
        this.numParameters = 0;
      }
      if (this.isFunctionCall) {
        this.numParameters += 1;
      }
    }
    
    private void addParametersFromDBMD(ResultSet paramTypesRs)
      throws SQLException
    {
      int i = 0;
      while (paramTypesRs.next())
      {
        String paramName = paramTypesRs.getString(4);
        int inOutModifier = paramTypesRs.getInt(5);
        
        boolean isOutParameter = false;
        boolean isInParameter = false;
        if ((i == 0) && (this.isFunctionCall))
        {
          isOutParameter = true;
          isInParameter = false;
        }
        else if (inOutModifier == 2)
        {
          isOutParameter = true;
          isInParameter = true;
        }
        else if (inOutModifier == 1)
        {
          isOutParameter = false;
          isInParameter = true;
        }
        else if (inOutModifier == 4)
        {
          isOutParameter = true;
          isInParameter = false;
        }
        int jdbcType = paramTypesRs.getInt(6);
        String typeName = paramTypesRs.getString(7);
        int precision = paramTypesRs.getInt(8);
        int scale = paramTypesRs.getInt(10);
        short nullability = paramTypesRs.getShort(12);
        
        CallableStatement.CallableStatementParam paramInfoToAdd = new CallableStatement.CallableStatementParam(CallableStatement.this, paramName, i++, isInParameter, isOutParameter, jdbcType, typeName, precision, scale, nullability, inOutModifier);
        
        this.parameterList.add(paramInfoToAdd);
        this.parameterMap.put(paramName, paramInfoToAdd);
      }
    }
    
    protected void checkBounds(int paramIndex)
      throws SQLException
    {
      int localParamIndex = paramIndex - 1;
      if ((paramIndex < 0) || (localParamIndex >= this.numParameters)) {
        throw SQLError.createSQLException(Messages.getString("CallableStatement.11") + paramIndex + Messages.getString("CallableStatement.12") + this.numParameters + Messages.getString("CallableStatement.13"), "S1009", CallableStatement.this.getExceptionInterceptor());
      }
    }
    
    protected Object clone()
      throws CloneNotSupportedException
    {
      return super.clone();
    }
    
    CallableStatement.CallableStatementParam getParameter(int index)
    {
      return (CallableStatement.CallableStatementParam)this.parameterList.get(index);
    }
    
    CallableStatement.CallableStatementParam getParameter(String name)
    {
      return (CallableStatement.CallableStatementParam)this.parameterMap.get(name);
    }
    
    public String getParameterClassName(int arg0)
      throws SQLException
    {
      String mysqlTypeName = getParameterTypeName(arg0);
      
      boolean isBinaryOrBlob = (StringUtils.indexOfIgnoreCase(mysqlTypeName, "BLOB") != -1) || (StringUtils.indexOfIgnoreCase(mysqlTypeName, "BINARY") != -1);
      
      boolean isUnsigned = StringUtils.indexOfIgnoreCase(mysqlTypeName, "UNSIGNED") != -1;
      
      int mysqlTypeIfKnown = 0;
      if (StringUtils.startsWithIgnoreCase(mysqlTypeName, "MEDIUMINT")) {
        mysqlTypeIfKnown = 9;
      }
      return ResultSetMetaData.getClassNameForJavaType(getParameterType(arg0), isUnsigned, mysqlTypeIfKnown, isBinaryOrBlob, false);
    }
    
    public int getParameterCount()
      throws SQLException
    {
      if (this.parameterList == null) {
        return 0;
      }
      return this.parameterList.size();
    }
    
    public int getParameterMode(int arg0)
      throws SQLException
    {
      checkBounds(arg0);
      
      return getParameter(arg0 - 1).inOutModifier;
    }
    
    public int getParameterType(int arg0)
      throws SQLException
    {
      checkBounds(arg0);
      
      return getParameter(arg0 - 1).jdbcType;
    }
    
    public String getParameterTypeName(int arg0)
      throws SQLException
    {
      checkBounds(arg0);
      
      return getParameter(arg0 - 1).typeName;
    }
    
    public int getPrecision(int arg0)
      throws SQLException
    {
      checkBounds(arg0);
      
      return getParameter(arg0 - 1).precision;
    }
    
    public int getScale(int arg0)
      throws SQLException
    {
      checkBounds(arg0);
      
      return getParameter(arg0 - 1).scale;
    }
    
    public int isNullable(int arg0)
      throws SQLException
    {
      checkBounds(arg0);
      
      return getParameter(arg0 - 1).nullability;
    }
    
    public boolean isSigned(int arg0)
      throws SQLException
    {
      checkBounds(arg0);
      
      return false;
    }
    
    Iterator iterator()
    {
      return this.parameterList.iterator();
    }
    
    int numberOfParameters()
    {
      return this.numParameters;
    }
  }
  
  protected class CallableStatementParamInfoJDBC3
    extends CallableStatement.CallableStatementParamInfo
    implements ParameterMetaData
  {
    CallableStatementParamInfoJDBC3(ResultSet paramTypesRs)
      throws SQLException
    {
      super(paramTypesRs);
    }
    
    public CallableStatementParamInfoJDBC3(CallableStatement.CallableStatementParamInfo paramInfo)
    {
      super(paramInfo);
    }
    
    public boolean isWrapperFor(Class iface)
      throws SQLException
    {
      CallableStatement.this.checkClosed();
      
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
        throw SQLError.createSQLException("Unable to unwrap to " + iface.toString(), "S1009", CallableStatement.this.getExceptionInterceptor());
      }
    }
  }
  
  private static String mangleParameterName(String origParameterName)
  {
    if (origParameterName == null) {
      return null;
    }
    int offset = 0;
    if ((origParameterName.length() > 0) && (origParameterName.charAt(0) == '@')) {
      offset = 1;
    }
    StringBuffer paramNameBuf = new StringBuffer("@com_mysql_jdbc_outparam_".length() + origParameterName.length());
    
    paramNameBuf.append("@com_mysql_jdbc_outparam_");
    paramNameBuf.append(origParameterName.substring(offset));
    
    return paramNameBuf.toString();
  }
  
  private boolean callingStoredFunction = false;
  private ResultSetInternalMethods functionReturnValueResults;
  private boolean hasOutputParams = false;
  private ResultSetInternalMethods outputParameterResults;
  protected boolean outputParamWasNull = false;
  private int[] parameterIndexToRsIndex;
  protected CallableStatementParamInfo paramInfo;
  private CallableStatementParam returnValueParam;
  private int[] placeholderToParameterIndexMap;
  
  public CallableStatement(ConnectionImpl conn, CallableStatementParamInfo paramInfo)
    throws SQLException
  {
    super(conn, paramInfo.nativeSql, paramInfo.catalogInUse);
    
    this.paramInfo = paramInfo;
    this.callingStoredFunction = this.paramInfo.isFunctionCall;
    if (this.callingStoredFunction) {
      this.parameterCount += 1;
    }
    this.retrieveGeneratedKeys = true;
  }
  
  protected static CallableStatement getInstance(ConnectionImpl conn, String sql, String catalog, boolean isFunctionCall)
    throws SQLException
  {
    if (!Util.isJdbc4()) {
      return new CallableStatement(conn, sql, catalog, isFunctionCall);
    }
    return (CallableStatement)Util.handleNewInstance(JDBC_4_CSTMT_4_ARGS_CTOR, new Object[] { conn, sql, catalog, Boolean.valueOf(isFunctionCall) }, conn.getExceptionInterceptor());
  }
  
  protected static CallableStatement getInstance(ConnectionImpl conn, CallableStatementParamInfo paramInfo)
    throws SQLException
  {
    if (!Util.isJdbc4()) {
      return new CallableStatement(conn, paramInfo);
    }
    return (CallableStatement)Util.handleNewInstance(JDBC_4_CSTMT_2_ARGS_CTOR, new Object[] { conn, paramInfo }, conn.getExceptionInterceptor());
  }
  
  private void generateParameterMap()
    throws SQLException
  {
    if (this.paramInfo == null) {
      return;
    }
    int parameterCountFromMetaData = this.paramInfo.getParameterCount();
    if (this.callingStoredFunction) {
      parameterCountFromMetaData--;
    }
    if ((this.paramInfo != null) && (this.parameterCount != parameterCountFromMetaData))
    {
      this.placeholderToParameterIndexMap = new int[this.parameterCount];
      
      int startPos = this.callingStoredFunction ? StringUtils.indexOfIgnoreCase(this.originalSql, "SELECT") : StringUtils.indexOfIgnoreCase(this.originalSql, "CALL");
      if (startPos != -1)
      {
        int parenOpenPos = this.originalSql.indexOf('(', startPos + 4);
        if (parenOpenPos != -1)
        {
          int parenClosePos = StringUtils.indexOfIgnoreCaseRespectQuotes(parenOpenPos, this.originalSql, ")", '\'', true);
          if (parenClosePos != -1)
          {
            List parsedParameters = StringUtils.split(this.originalSql.substring(parenOpenPos + 1, parenClosePos), ",", "'\"", "'\"", true);
            
            int numParsedParameters = parsedParameters.size();
            if (numParsedParameters != this.parameterCount) {}
            int placeholderCount = 0;
            for (int i = 0; i < numParsedParameters; i++) {
              if (((String)parsedParameters.get(i)).equals("?")) {
                this.placeholderToParameterIndexMap[(placeholderCount++)] = i;
              }
            }
          }
        }
      }
    }
  }
  
  public CallableStatement(ConnectionImpl conn, String sql, String catalog, boolean isFunctionCall)
    throws SQLException
  {
    super(conn, sql, catalog);
    
    this.callingStoredFunction = isFunctionCall;
    if (!this.callingStoredFunction)
    {
      if (!StringUtils.startsWithIgnoreCaseAndWs(sql, "CALL")) {
        fakeParameterTypes(false);
      } else {
        determineParameterTypes();
      }
      generateParameterMap();
    }
    else
    {
      determineParameterTypes();
      generateParameterMap();
      
      this.parameterCount += 1;
    }
    this.retrieveGeneratedKeys = true;
  }
  
  public void addBatch()
    throws SQLException
  {
    setOutParams();
    
    super.addBatch();
  }
  
  private CallableStatementParam checkIsOutputParam(int paramIndex)
    throws SQLException
  {
    if (this.callingStoredFunction)
    {
      if (paramIndex == 1)
      {
        if (this.returnValueParam == null) {
          this.returnValueParam = new CallableStatementParam("", 0, false, true, 12, "VARCHAR", 0, 0, (short)2, 5);
        }
        return this.returnValueParam;
      }
      paramIndex--;
    }
    checkParameterIndexBounds(paramIndex);
    
    int localParamIndex = paramIndex - 1;
    if (this.placeholderToParameterIndexMap != null) {
      localParamIndex = this.placeholderToParameterIndexMap[localParamIndex];
    }
    CallableStatementParam paramDescriptor = this.paramInfo.getParameter(localParamIndex);
    if (this.connection.getNoAccessToProcedureBodies())
    {
      paramDescriptor.isOut = true;
      paramDescriptor.isIn = true;
      paramDescriptor.inOutModifier = 2;
    }
    else if (!paramDescriptor.isOut)
    {
      throw SQLError.createSQLException(Messages.getString("CallableStatement.9") + paramIndex + Messages.getString("CallableStatement.10"), "S1009", getExceptionInterceptor());
    }
    this.hasOutputParams = true;
    
    return paramDescriptor;
  }
  
  private void checkParameterIndexBounds(int paramIndex)
    throws SQLException
  {
    this.paramInfo.checkBounds(paramIndex);
  }
  
  private void checkStreamability()
    throws SQLException
  {
    if ((this.hasOutputParams) && (createStreamingResultSet())) {
      throw SQLError.createSQLException(Messages.getString("CallableStatement.14"), "S1C00", getExceptionInterceptor());
    }
  }
  
  public synchronized void clearParameters()
    throws SQLException
  {
    super.clearParameters();
    try
    {
      if (this.outputParameterResults != null) {
        this.outputParameterResults.close();
      }
    }
    finally
    {
      this.outputParameterResults = null;
    }
  }
  
  private void fakeParameterTypes(boolean isReallyProcedure)
    throws SQLException
  {
    Field[] fields = new Field[13];
    
    fields[0] = new Field("", "PROCEDURE_CAT", 1, 0);
    fields[1] = new Field("", "PROCEDURE_SCHEM", 1, 0);
    fields[2] = new Field("", "PROCEDURE_NAME", 1, 0);
    fields[3] = new Field("", "COLUMN_NAME", 1, 0);
    fields[4] = new Field("", "COLUMN_TYPE", 1, 0);
    fields[5] = new Field("", "DATA_TYPE", 5, 0);
    fields[6] = new Field("", "TYPE_NAME", 1, 0);
    fields[7] = new Field("", "PRECISION", 4, 0);
    fields[8] = new Field("", "LENGTH", 4, 0);
    fields[9] = new Field("", "SCALE", 5, 0);
    fields[10] = new Field("", "RADIX", 5, 0);
    fields[11] = new Field("", "NULLABLE", 5, 0);
    fields[12] = new Field("", "REMARKS", 1, 0);
    
    String procName = isReallyProcedure ? extractProcedureName() : null;
    
    byte[] procNameAsBytes = null;
    try
    {
      procNameAsBytes = procName == null ? null : procName.getBytes("UTF-8");
    }
    catch (UnsupportedEncodingException ueEx)
    {
      procNameAsBytes = StringUtils.s2b(procName, this.connection);
    }
    ArrayList resultRows = new ArrayList();
    for (int i = 0; i < this.parameterCount; i++)
    {
      byte[][] row = new byte[13][];
      row[0] = null;
      row[1] = null;
      row[2] = procNameAsBytes;
      row[3] = StringUtils.s2b(String.valueOf(i), this.connection);
      
      row[4] = StringUtils.s2b(String.valueOf(1), this.connection);
      
      row[5] = StringUtils.s2b(String.valueOf(12), this.connection);
      
      row[6] = StringUtils.s2b("VARCHAR", this.connection);
      row[7] = StringUtils.s2b(Integer.toString(65535), this.connection);
      row[8] = StringUtils.s2b(Integer.toString(65535), this.connection);
      row[9] = StringUtils.s2b(Integer.toString(0), this.connection);
      row[10] = StringUtils.s2b(Integer.toString(10), this.connection);
      
      row[11] = StringUtils.s2b(Integer.toString(2), this.connection);
      
      row[12] = null;
      
      resultRows.add(new ByteArrayRow(row, getExceptionInterceptor()));
    }
    ResultSet paramTypesRs = DatabaseMetaData.buildResultSet(fields, resultRows, this.connection);
    
    convertGetProcedureColumnsToInternalDescriptors(paramTypesRs);
  }
  
  private void determineParameterTypes()
    throws SQLException
  {
    if (this.connection.getNoAccessToProcedureBodies())
    {
      fakeParameterTypes(true);
      
      return;
    }
    ResultSet paramTypesRs = null;
    try
    {
      String procName = extractProcedureName();
      
      java.sql.DatabaseMetaData dbmd = this.connection.getMetaData();
      
      boolean useCatalog = false;
      if (procName.indexOf(".") == -1) {
        useCatalog = true;
      }
      paramTypesRs = dbmd.getProcedureColumns((this.connection.versionMeetsMinimum(5, 0, 2)) && (useCatalog) ? this.currentCatalog : null, null, procName, "%");
      
      convertGetProcedureColumnsToInternalDescriptors(paramTypesRs);
    }
    finally
    {
      SQLException sqlExRethrow = null;
      if (paramTypesRs != null)
      {
        try
        {
          paramTypesRs.close();
        }
        catch (SQLException sqlEx)
        {
          sqlExRethrow = sqlEx;
        }
        paramTypesRs = null;
      }
      if (sqlExRethrow != null) {
        throw sqlExRethrow;
      }
    }
  }
  
  private void convertGetProcedureColumnsToInternalDescriptors(ResultSet paramTypesRs)
    throws SQLException
  {
    if (!this.connection.isRunningOnJDK13()) {
      this.paramInfo = new CallableStatementParamInfoJDBC3(paramTypesRs);
    } else {
      this.paramInfo = new CallableStatementParamInfo(paramTypesRs);
    }
  }
  
  public boolean execute()
    throws SQLException
  {
    boolean returnVal = false;
    
    checkClosed();
    
    checkStreamability();
    synchronized (this.connection.getMutex())
    {
      setInOutParamsOnServer();
      setOutParams();
      
      returnVal = super.execute();
      if (this.callingStoredFunction)
      {
        this.functionReturnValueResults = this.results;
        this.functionReturnValueResults.next();
        this.results = null;
      }
      retrieveOutParams();
    }
    if (!this.callingStoredFunction) {
      return returnVal;
    }
    return false;
  }
  
  public ResultSet executeQuery()
    throws SQLException
  {
    checkClosed();
    
    checkStreamability();
    
    ResultSet execResults = null;
    synchronized (this.connection.getMutex())
    {
      setInOutParamsOnServer();
      setOutParams();
      
      execResults = super.executeQuery();
      
      retrieveOutParams();
    }
    return execResults;
  }
  
  public int executeUpdate()
    throws SQLException
  {
    int returnVal = -1;
    
    checkClosed();
    
    checkStreamability();
    if (this.callingStoredFunction)
    {
      execute();
      
      return -1;
    }
    synchronized (this.connection.getMutex())
    {
      setInOutParamsOnServer();
      setOutParams();
      
      returnVal = super.executeUpdate();
      
      retrieveOutParams();
    }
    return returnVal;
  }
  
  private String extractProcedureName()
    throws SQLException
  {
    String sanitizedSql = StringUtils.stripComments(this.originalSql, "`\"'", "`\"'", true, false, true, true);
    
    int endCallIndex = StringUtils.indexOfIgnoreCase(sanitizedSql, "CALL ");
    
    int offset = 5;
    if (endCallIndex == -1)
    {
      endCallIndex = StringUtils.indexOfIgnoreCase(sanitizedSql, "SELECT ");
      
      offset = 7;
    }
    if (endCallIndex != -1)
    {
      StringBuffer nameBuf = new StringBuffer();
      
      String trimmedStatement = sanitizedSql.substring(endCallIndex + offset).trim();
      
      int statementLength = trimmedStatement.length();
      for (int i = 0; i < statementLength; i++)
      {
        char c = trimmedStatement.charAt(i);
        if ((Character.isWhitespace(c)) || (c == '(') || (c == '?')) {
          break;
        }
        nameBuf.append(c);
      }
      return nameBuf.toString();
    }
    throw SQLError.createSQLException(Messages.getString("CallableStatement.1"), "S1000", getExceptionInterceptor());
  }
  
  protected String fixParameterName(String paramNameIn)
    throws SQLException
  {
    if ((paramNameIn == null) || (paramNameIn.length() == 0)) {
      throw SQLError.createSQLException(Messages.getString("CallableStatement.0") + paramNameIn == null ? Messages.getString("CallableStatement.15") : Messages.getString("CallableStatement.16"), "S1009", getExceptionInterceptor());
    }
    if (this.connection.getNoAccessToProcedureBodies()) {
      throw SQLError.createSQLException("No access to parameters by name when connection has been configured not to access procedure bodies", "S1009", getExceptionInterceptor());
    }
    return mangleParameterName(paramNameIn);
  }
  
  public synchronized Array getArray(int i)
    throws SQLException
  {
    ResultSetInternalMethods rs = getOutputParameters(i);
    
    Array retValue = rs.getArray(mapOutputParameterIndexToRsIndex(i));
    
    this.outputParamWasNull = rs.wasNull();
    
    return retValue;
  }
  
  public synchronized Array getArray(String parameterName)
    throws SQLException
  {
    ResultSetInternalMethods rs = getOutputParameters(0);
    
    Array retValue = rs.getArray(fixParameterName(parameterName));
    
    this.outputParamWasNull = rs.wasNull();
    
    return retValue;
  }
  
  public synchronized BigDecimal getBigDecimal(int parameterIndex)
    throws SQLException
  {
    ResultSetInternalMethods rs = getOutputParameters(parameterIndex);
    
    BigDecimal retValue = rs.getBigDecimal(mapOutputParameterIndexToRsIndex(parameterIndex));
    
    this.outputParamWasNull = rs.wasNull();
    
    return retValue;
  }
  
  /**
   * @deprecated
   */
  public synchronized BigDecimal getBigDecimal(int parameterIndex, int scale)
    throws SQLException
  {
    ResultSetInternalMethods rs = getOutputParameters(parameterIndex);
    
    BigDecimal retValue = rs.getBigDecimal(mapOutputParameterIndexToRsIndex(parameterIndex), scale);
    
    this.outputParamWasNull = rs.wasNull();
    
    return retValue;
  }
  
  public synchronized BigDecimal getBigDecimal(String parameterName)
    throws SQLException
  {
    ResultSetInternalMethods rs = getOutputParameters(0);
    
    BigDecimal retValue = rs.getBigDecimal(fixParameterName(parameterName));
    
    this.outputParamWasNull = rs.wasNull();
    
    return retValue;
  }
  
  public synchronized Blob getBlob(int parameterIndex)
    throws SQLException
  {
    ResultSetInternalMethods rs = getOutputParameters(parameterIndex);
    
    Blob retValue = rs.getBlob(mapOutputParameterIndexToRsIndex(parameterIndex));
    
    this.outputParamWasNull = rs.wasNull();
    
    return retValue;
  }
  
  public synchronized Blob getBlob(String parameterName)
    throws SQLException
  {
    ResultSetInternalMethods rs = getOutputParameters(0);
    
    Blob retValue = rs.getBlob(fixParameterName(parameterName));
    
    this.outputParamWasNull = rs.wasNull();
    
    return retValue;
  }
  
  public synchronized boolean getBoolean(int parameterIndex)
    throws SQLException
  {
    ResultSetInternalMethods rs = getOutputParameters(parameterIndex);
    
    boolean retValue = rs.getBoolean(mapOutputParameterIndexToRsIndex(parameterIndex));
    
    this.outputParamWasNull = rs.wasNull();
    
    return retValue;
  }
  
  public synchronized boolean getBoolean(String parameterName)
    throws SQLException
  {
    ResultSetInternalMethods rs = getOutputParameters(0);
    
    boolean retValue = rs.getBoolean(fixParameterName(parameterName));
    
    this.outputParamWasNull = rs.wasNull();
    
    return retValue;
  }
  
  public synchronized byte getByte(int parameterIndex)
    throws SQLException
  {
    ResultSetInternalMethods rs = getOutputParameters(parameterIndex);
    
    byte retValue = rs.getByte(mapOutputParameterIndexToRsIndex(parameterIndex));
    
    this.outputParamWasNull = rs.wasNull();
    
    return retValue;
  }
  
  public synchronized byte getByte(String parameterName)
    throws SQLException
  {
    ResultSetInternalMethods rs = getOutputParameters(0);
    
    byte retValue = rs.getByte(fixParameterName(parameterName));
    
    this.outputParamWasNull = rs.wasNull();
    
    return retValue;
  }
  
  public synchronized byte[] getBytes(int parameterIndex)
    throws SQLException
  {
    ResultSetInternalMethods rs = getOutputParameters(parameterIndex);
    
    byte[] retValue = rs.getBytes(mapOutputParameterIndexToRsIndex(parameterIndex));
    
    this.outputParamWasNull = rs.wasNull();
    
    return retValue;
  }
  
  public synchronized byte[] getBytes(String parameterName)
    throws SQLException
  {
    ResultSetInternalMethods rs = getOutputParameters(0);
    
    byte[] retValue = rs.getBytes(fixParameterName(parameterName));
    
    this.outputParamWasNull = rs.wasNull();
    
    return retValue;
  }
  
  public synchronized Clob getClob(int parameterIndex)
    throws SQLException
  {
    ResultSetInternalMethods rs = getOutputParameters(parameterIndex);
    
    Clob retValue = rs.getClob(mapOutputParameterIndexToRsIndex(parameterIndex));
    
    this.outputParamWasNull = rs.wasNull();
    
    return retValue;
  }
  
  public synchronized Clob getClob(String parameterName)
    throws SQLException
  {
    ResultSetInternalMethods rs = getOutputParameters(0);
    
    Clob retValue = rs.getClob(fixParameterName(parameterName));
    
    this.outputParamWasNull = rs.wasNull();
    
    return retValue;
  }
  
  public synchronized Date getDate(int parameterIndex)
    throws SQLException
  {
    ResultSetInternalMethods rs = getOutputParameters(parameterIndex);
    
    Date retValue = rs.getDate(mapOutputParameterIndexToRsIndex(parameterIndex));
    
    this.outputParamWasNull = rs.wasNull();
    
    return retValue;
  }
  
  public synchronized Date getDate(int parameterIndex, Calendar cal)
    throws SQLException
  {
    ResultSetInternalMethods rs = getOutputParameters(parameterIndex);
    
    Date retValue = rs.getDate(mapOutputParameterIndexToRsIndex(parameterIndex), cal);
    
    this.outputParamWasNull = rs.wasNull();
    
    return retValue;
  }
  
  public synchronized Date getDate(String parameterName)
    throws SQLException
  {
    ResultSetInternalMethods rs = getOutputParameters(0);
    
    Date retValue = rs.getDate(fixParameterName(parameterName));
    
    this.outputParamWasNull = rs.wasNull();
    
    return retValue;
  }
  
  public synchronized Date getDate(String parameterName, Calendar cal)
    throws SQLException
  {
    ResultSetInternalMethods rs = getOutputParameters(0);
    
    Date retValue = rs.getDate(fixParameterName(parameterName), cal);
    
    this.outputParamWasNull = rs.wasNull();
    
    return retValue;
  }
  
  public synchronized double getDouble(int parameterIndex)
    throws SQLException
  {
    ResultSetInternalMethods rs = getOutputParameters(parameterIndex);
    
    double retValue = rs.getDouble(mapOutputParameterIndexToRsIndex(parameterIndex));
    
    this.outputParamWasNull = rs.wasNull();
    
    return retValue;
  }
  
  public synchronized double getDouble(String parameterName)
    throws SQLException
  {
    ResultSetInternalMethods rs = getOutputParameters(0);
    
    double retValue = rs.getDouble(fixParameterName(parameterName));
    
    this.outputParamWasNull = rs.wasNull();
    
    return retValue;
  }
  
  public synchronized float getFloat(int parameterIndex)
    throws SQLException
  {
    ResultSetInternalMethods rs = getOutputParameters(parameterIndex);
    
    float retValue = rs.getFloat(mapOutputParameterIndexToRsIndex(parameterIndex));
    
    this.outputParamWasNull = rs.wasNull();
    
    return retValue;
  }
  
  public synchronized float getFloat(String parameterName)
    throws SQLException
  {
    ResultSetInternalMethods rs = getOutputParameters(0);
    
    float retValue = rs.getFloat(fixParameterName(parameterName));
    
    this.outputParamWasNull = rs.wasNull();
    
    return retValue;
  }
  
  public synchronized int getInt(int parameterIndex)
    throws SQLException
  {
    ResultSetInternalMethods rs = getOutputParameters(parameterIndex);
    
    int retValue = rs.getInt(mapOutputParameterIndexToRsIndex(parameterIndex));
    
    this.outputParamWasNull = rs.wasNull();
    
    return retValue;
  }
  
  public synchronized int getInt(String parameterName)
    throws SQLException
  {
    ResultSetInternalMethods rs = getOutputParameters(0);
    
    int retValue = rs.getInt(fixParameterName(parameterName));
    
    this.outputParamWasNull = rs.wasNull();
    
    return retValue;
  }
  
  public synchronized long getLong(int parameterIndex)
    throws SQLException
  {
    ResultSetInternalMethods rs = getOutputParameters(parameterIndex);
    
    long retValue = rs.getLong(mapOutputParameterIndexToRsIndex(parameterIndex));
    
    this.outputParamWasNull = rs.wasNull();
    
    return retValue;
  }
  
  public synchronized long getLong(String parameterName)
    throws SQLException
  {
    ResultSetInternalMethods rs = getOutputParameters(0);
    
    long retValue = rs.getLong(fixParameterName(parameterName));
    
    this.outputParamWasNull = rs.wasNull();
    
    return retValue;
  }
  
  protected int getNamedParamIndex(String paramName, boolean forOut)
    throws SQLException
  {
    if (this.connection.getNoAccessToProcedureBodies()) {
      throw SQLError.createSQLException("No access to parameters by name when connection has been configured not to access procedure bodies", "S1009", getExceptionInterceptor());
    }
    if ((paramName == null) || (paramName.length() == 0)) {
      throw SQLError.createSQLException(Messages.getString("CallableStatement.2"), "S1009", getExceptionInterceptor());
    }
    if (this.paramInfo == null) {
      throw SQLError.createSQLException(Messages.getString("CallableStatement.3") + paramName + Messages.getString("CallableStatement.4"), "S1009", getExceptionInterceptor());
    }
    CallableStatementParam namedParamInfo = this.paramInfo.getParameter(paramName);
    if ((forOut) && (!namedParamInfo.isOut)) {
      throw SQLError.createSQLException(Messages.getString("CallableStatement.5") + paramName + Messages.getString("CallableStatement.6"), "S1009", getExceptionInterceptor());
    }
    if (this.placeholderToParameterIndexMap == null) {
      return namedParamInfo.index + 1;
    }
    for (int i = 0; i < this.placeholderToParameterIndexMap.length; i++) {
      if (this.placeholderToParameterIndexMap[i] == namedParamInfo.index) {
        return i + 1;
      }
    }
    throw SQLError.createSQLException("Can't find local placeholder mapping for parameter named \"" + paramName + "\".", "S1009", getExceptionInterceptor());
  }
  
  public synchronized Object getObject(int parameterIndex)
    throws SQLException
  {
    CallableStatementParam paramDescriptor = checkIsOutputParam(parameterIndex);
    
    ResultSetInternalMethods rs = getOutputParameters(parameterIndex);
    
    Object retVal = rs.getObjectStoredProc(mapOutputParameterIndexToRsIndex(parameterIndex), paramDescriptor.desiredJdbcType);
    
    this.outputParamWasNull = rs.wasNull();
    
    return retVal;
  }
  
  public synchronized Object getObject(int parameterIndex, Map map)
    throws SQLException
  {
    ResultSetInternalMethods rs = getOutputParameters(parameterIndex);
    
    Object retVal = rs.getObject(mapOutputParameterIndexToRsIndex(parameterIndex), map);
    
    this.outputParamWasNull = rs.wasNull();
    
    return retVal;
  }
  
  public synchronized Object getObject(String parameterName)
    throws SQLException
  {
    ResultSetInternalMethods rs = getOutputParameters(0);
    
    Object retValue = rs.getObject(fixParameterName(parameterName));
    
    this.outputParamWasNull = rs.wasNull();
    
    return retValue;
  }
  
  public synchronized Object getObject(String parameterName, Map map)
    throws SQLException
  {
    ResultSetInternalMethods rs = getOutputParameters(0);
    
    Object retValue = rs.getObject(fixParameterName(parameterName), map);
    
    this.outputParamWasNull = rs.wasNull();
    
    return retValue;
  }
  
  protected ResultSetInternalMethods getOutputParameters(int paramIndex)
    throws SQLException
  {
    this.outputParamWasNull = false;
    if ((paramIndex == 1) && (this.callingStoredFunction) && (this.returnValueParam != null)) {
      return this.functionReturnValueResults;
    }
    if (this.outputParameterResults == null)
    {
      if (this.paramInfo.numberOfParameters() == 0) {
        throw SQLError.createSQLException(Messages.getString("CallableStatement.7"), "S1009", getExceptionInterceptor());
      }
      throw SQLError.createSQLException(Messages.getString("CallableStatement.8"), "S1000", getExceptionInterceptor());
    }
    return this.outputParameterResults;
  }
  
  public synchronized ParameterMetaData getParameterMetaData()
    throws SQLException
  {
    if (this.placeholderToParameterIndexMap == null) {
      return (CallableStatementParamInfoJDBC3)this.paramInfo;
    }
    return new CallableStatementParamInfoJDBC3(this.paramInfo);
  }
  
  public synchronized Ref getRef(int parameterIndex)
    throws SQLException
  {
    ResultSetInternalMethods rs = getOutputParameters(parameterIndex);
    
    Ref retValue = rs.getRef(mapOutputParameterIndexToRsIndex(parameterIndex));
    
    this.outputParamWasNull = rs.wasNull();
    
    return retValue;
  }
  
  public synchronized Ref getRef(String parameterName)
    throws SQLException
  {
    ResultSetInternalMethods rs = getOutputParameters(0);
    
    Ref retValue = rs.getRef(fixParameterName(parameterName));
    
    this.outputParamWasNull = rs.wasNull();
    
    return retValue;
  }
  
  public synchronized short getShort(int parameterIndex)
    throws SQLException
  {
    ResultSetInternalMethods rs = getOutputParameters(parameterIndex);
    
    short retValue = rs.getShort(mapOutputParameterIndexToRsIndex(parameterIndex));
    
    this.outputParamWasNull = rs.wasNull();
    
    return retValue;
  }
  
  public synchronized short getShort(String parameterName)
    throws SQLException
  {
    ResultSetInternalMethods rs = getOutputParameters(0);
    
    short retValue = rs.getShort(fixParameterName(parameterName));
    
    this.outputParamWasNull = rs.wasNull();
    
    return retValue;
  }
  
  public synchronized String getString(int parameterIndex)
    throws SQLException
  {
    ResultSetInternalMethods rs = getOutputParameters(parameterIndex);
    
    String retValue = rs.getString(mapOutputParameterIndexToRsIndex(parameterIndex));
    
    this.outputParamWasNull = rs.wasNull();
    
    return retValue;
  }
  
  public synchronized String getString(String parameterName)
    throws SQLException
  {
    ResultSetInternalMethods rs = getOutputParameters(0);
    
    String retValue = rs.getString(fixParameterName(parameterName));
    
    this.outputParamWasNull = rs.wasNull();
    
    return retValue;
  }
  
  public synchronized Time getTime(int parameterIndex)
    throws SQLException
  {
    ResultSetInternalMethods rs = getOutputParameters(parameterIndex);
    
    Time retValue = rs.getTime(mapOutputParameterIndexToRsIndex(parameterIndex));
    
    this.outputParamWasNull = rs.wasNull();
    
    return retValue;
  }
  
  public synchronized Time getTime(int parameterIndex, Calendar cal)
    throws SQLException
  {
    ResultSetInternalMethods rs = getOutputParameters(parameterIndex);
    
    Time retValue = rs.getTime(mapOutputParameterIndexToRsIndex(parameterIndex), cal);
    
    this.outputParamWasNull = rs.wasNull();
    
    return retValue;
  }
  
  public synchronized Time getTime(String parameterName)
    throws SQLException
  {
    ResultSetInternalMethods rs = getOutputParameters(0);
    
    Time retValue = rs.getTime(fixParameterName(parameterName));
    
    this.outputParamWasNull = rs.wasNull();
    
    return retValue;
  }
  
  public synchronized Time getTime(String parameterName, Calendar cal)
    throws SQLException
  {
    ResultSetInternalMethods rs = getOutputParameters(0);
    
    Time retValue = rs.getTime(fixParameterName(parameterName), cal);
    
    this.outputParamWasNull = rs.wasNull();
    
    return retValue;
  }
  
  public synchronized Timestamp getTimestamp(int parameterIndex)
    throws SQLException
  {
    ResultSetInternalMethods rs = getOutputParameters(parameterIndex);
    
    Timestamp retValue = rs.getTimestamp(mapOutputParameterIndexToRsIndex(parameterIndex));
    
    this.outputParamWasNull = rs.wasNull();
    
    return retValue;
  }
  
  public synchronized Timestamp getTimestamp(int parameterIndex, Calendar cal)
    throws SQLException
  {
    ResultSetInternalMethods rs = getOutputParameters(parameterIndex);
    
    Timestamp retValue = rs.getTimestamp(mapOutputParameterIndexToRsIndex(parameterIndex), cal);
    
    this.outputParamWasNull = rs.wasNull();
    
    return retValue;
  }
  
  public synchronized Timestamp getTimestamp(String parameterName)
    throws SQLException
  {
    ResultSetInternalMethods rs = getOutputParameters(0);
    
    Timestamp retValue = rs.getTimestamp(fixParameterName(parameterName));
    
    this.outputParamWasNull = rs.wasNull();
    
    return retValue;
  }
  
  public synchronized Timestamp getTimestamp(String parameterName, Calendar cal)
    throws SQLException
  {
    ResultSetInternalMethods rs = getOutputParameters(0);
    
    Timestamp retValue = rs.getTimestamp(fixParameterName(parameterName), cal);
    
    this.outputParamWasNull = rs.wasNull();
    
    return retValue;
  }
  
  public synchronized URL getURL(int parameterIndex)
    throws SQLException
  {
    ResultSetInternalMethods rs = getOutputParameters(parameterIndex);
    
    URL retValue = rs.getURL(mapOutputParameterIndexToRsIndex(parameterIndex));
    
    this.outputParamWasNull = rs.wasNull();
    
    return retValue;
  }
  
  public synchronized URL getURL(String parameterName)
    throws SQLException
  {
    ResultSetInternalMethods rs = getOutputParameters(0);
    
    URL retValue = rs.getURL(fixParameterName(parameterName));
    
    this.outputParamWasNull = rs.wasNull();
    
    return retValue;
  }
  
  protected int mapOutputParameterIndexToRsIndex(int paramIndex)
    throws SQLException
  {
    if ((this.returnValueParam != null) && (paramIndex == 1)) {
      return 1;
    }
    checkParameterIndexBounds(paramIndex);
    
    int localParamIndex = paramIndex - 1;
    if (this.placeholderToParameterIndexMap != null) {
      localParamIndex = this.placeholderToParameterIndexMap[localParamIndex];
    }
    int rsIndex = this.parameterIndexToRsIndex[localParamIndex];
    if (rsIndex == Integer.MIN_VALUE) {
      throw SQLError.createSQLException(Messages.getString("CallableStatement.21") + paramIndex + Messages.getString("CallableStatement.22"), "S1009", getExceptionInterceptor());
    }
    return rsIndex + 1;
  }
  
  public void registerOutParameter(int parameterIndex, int sqlType)
    throws SQLException
  {
    CallableStatementParam paramDescriptor = checkIsOutputParam(parameterIndex);
    paramDescriptor.desiredJdbcType = sqlType;
  }
  
  public void registerOutParameter(int parameterIndex, int sqlType, int scale)
    throws SQLException
  {
    registerOutParameter(parameterIndex, sqlType);
  }
  
  public void registerOutParameter(int parameterIndex, int sqlType, String typeName)
    throws SQLException
  {
    checkIsOutputParam(parameterIndex);
  }
  
  public synchronized void registerOutParameter(String parameterName, int sqlType)
    throws SQLException
  {
    registerOutParameter(getNamedParamIndex(parameterName, true), sqlType);
  }
  
  public void registerOutParameter(String parameterName, int sqlType, int scale)
    throws SQLException
  {
    registerOutParameter(getNamedParamIndex(parameterName, true), sqlType);
  }
  
  public void registerOutParameter(String parameterName, int sqlType, String typeName)
    throws SQLException
  {
    registerOutParameter(getNamedParamIndex(parameterName, true), sqlType, typeName);
  }
  
  private void retrieveOutParams()
    throws SQLException
  {
    int numParameters = this.paramInfo.numberOfParameters();
    
    this.parameterIndexToRsIndex = new int[numParameters];
    for (int i = 0; i < numParameters; i++) {
      this.parameterIndexToRsIndex[i] = Integer.MIN_VALUE;
    }
    int localParamIndex = 0;
    if (numParameters > 0)
    {
      StringBuffer outParameterQuery = new StringBuffer("SELECT ");
      
      boolean firstParam = true;
      boolean hadOutputParams = false;
      
      Iterator paramIter = this.paramInfo.iterator();
      while (paramIter.hasNext())
      {
        CallableStatementParam retrParamInfo = (CallableStatementParam)paramIter.next();
        if (retrParamInfo.isOut)
        {
          hadOutputParams = true;
          
          this.parameterIndexToRsIndex[retrParamInfo.index] = (localParamIndex++);
          
          String outParameterName = mangleParameterName(retrParamInfo.paramName);
          if (!firstParam) {
            outParameterQuery.append(",");
          } else {
            firstParam = false;
          }
          if (!outParameterName.startsWith("@")) {
            outParameterQuery.append('@');
          }
          outParameterQuery.append(outParameterName);
        }
      }
      if (hadOutputParams)
      {
        Statement outParameterStmt = null;
        ResultSet outParamRs = null;
        try
        {
          outParameterStmt = this.connection.createStatement();
          outParamRs = outParameterStmt.executeQuery(outParameterQuery.toString());
          
          this.outputParameterResults = ((ResultSetInternalMethods)outParamRs).copy();
          if (!this.outputParameterResults.next())
          {
            this.outputParameterResults.close();
            this.outputParameterResults = null;
          }
        }
        finally
        {
          if (outParameterStmt != null) {
            outParameterStmt.close();
          }
        }
      }
      else
      {
        this.outputParameterResults = null;
      }
    }
    else
    {
      this.outputParameterResults = null;
    }
  }
  
  public void setAsciiStream(String parameterName, InputStream x, int length)
    throws SQLException
  {
    setAsciiStream(getNamedParamIndex(parameterName, false), x, length);
  }
  
  public void setBigDecimal(String parameterName, BigDecimal x)
    throws SQLException
  {
    setBigDecimal(getNamedParamIndex(parameterName, false), x);
  }
  
  public void setBinaryStream(String parameterName, InputStream x, int length)
    throws SQLException
  {
    setBinaryStream(getNamedParamIndex(parameterName, false), x, length);
  }
  
  public void setBoolean(String parameterName, boolean x)
    throws SQLException
  {
    setBoolean(getNamedParamIndex(parameterName, false), x);
  }
  
  public void setByte(String parameterName, byte x)
    throws SQLException
  {
    setByte(getNamedParamIndex(parameterName, false), x);
  }
  
  public void setBytes(String parameterName, byte[] x)
    throws SQLException
  {
    setBytes(getNamedParamIndex(parameterName, false), x);
  }
  
  public void setCharacterStream(String parameterName, Reader reader, int length)
    throws SQLException
  {
    setCharacterStream(getNamedParamIndex(parameterName, false), reader, length);
  }
  
  public void setDate(String parameterName, Date x)
    throws SQLException
  {
    setDate(getNamedParamIndex(parameterName, false), x);
  }
  
  public void setDate(String parameterName, Date x, Calendar cal)
    throws SQLException
  {
    setDate(getNamedParamIndex(parameterName, false), x, cal);
  }
  
  public void setDouble(String parameterName, double x)
    throws SQLException
  {
    setDouble(getNamedParamIndex(parameterName, false), x);
  }
  
  public void setFloat(String parameterName, float x)
    throws SQLException
  {
    setFloat(getNamedParamIndex(parameterName, false), x);
  }
  
  private void setInOutParamsOnServer()
    throws SQLException
  {
    if (this.paramInfo.numParameters > 0)
    {
      int parameterIndex = 0;
      
      Iterator paramIter = this.paramInfo.iterator();
      while (paramIter.hasNext())
      {
        CallableStatementParam inParamInfo = (CallableStatementParam)paramIter.next();
        if ((inParamInfo.isOut) && (inParamInfo.isIn))
        {
          String inOutParameterName = mangleParameterName(inParamInfo.paramName);
          StringBuffer queryBuf = new StringBuffer(4 + inOutParameterName.length() + 1 + 1);
          
          queryBuf.append("SET ");
          queryBuf.append(inOutParameterName);
          queryBuf.append("=?");
          
          PreparedStatement setPstmt = null;
          try
          {
            setPstmt = (PreparedStatement)this.connection.clientPrepareStatement(queryBuf.toString());
            
            byte[] parameterAsBytes = getBytesRepresentation(inParamInfo.index);
            if (parameterAsBytes != null)
            {
              if ((parameterAsBytes.length > 8) && (parameterAsBytes[0] == 95) && (parameterAsBytes[1] == 98) && (parameterAsBytes[2] == 105) && (parameterAsBytes[3] == 110) && (parameterAsBytes[4] == 97) && (parameterAsBytes[5] == 114) && (parameterAsBytes[6] == 121) && (parameterAsBytes[7] == 39))
              {
                setPstmt.setBytesNoEscapeNoQuotes(1, parameterAsBytes);
              }
              else
              {
                int sqlType = inParamInfo.desiredJdbcType;
                switch (sqlType)
                {
                case -7: 
                case -4: 
                case -3: 
                case -2: 
                case 2000: 
                case 2004: 
                  setPstmt.setBytes(1, parameterAsBytes);
                  break;
                default: 
                  setPstmt.setBytesNoEscape(1, parameterAsBytes);
                }
              }
            }
            else {
              setPstmt.setNull(1, 0);
            }
            setPstmt.executeUpdate();
          }
          finally
          {
            if (setPstmt != null) {
              setPstmt.close();
            }
          }
        }
        parameterIndex++;
      }
    }
  }
  
  public void setInt(String parameterName, int x)
    throws SQLException
  {
    setInt(getNamedParamIndex(parameterName, false), x);
  }
  
  public void setLong(String parameterName, long x)
    throws SQLException
  {
    setLong(getNamedParamIndex(parameterName, false), x);
  }
  
  public void setNull(String parameterName, int sqlType)
    throws SQLException
  {
    setNull(getNamedParamIndex(parameterName, false), sqlType);
  }
  
  public void setNull(String parameterName, int sqlType, String typeName)
    throws SQLException
  {
    setNull(getNamedParamIndex(parameterName, false), sqlType, typeName);
  }
  
  public void setObject(String parameterName, Object x)
    throws SQLException
  {
    setObject(getNamedParamIndex(parameterName, false), x);
  }
  
  public void setObject(String parameterName, Object x, int targetSqlType)
    throws SQLException
  {
    setObject(getNamedParamIndex(parameterName, false), x, targetSqlType);
  }
  
  private void setOutParams()
    throws SQLException
  {
    if (this.paramInfo.numParameters > 0)
    {
      Iterator paramIter = this.paramInfo.iterator();
      while (paramIter.hasNext())
      {
        CallableStatementParam outParamInfo = (CallableStatementParam)paramIter.next();
        if ((!this.callingStoredFunction) && (outParamInfo.isOut))
        {
          String outParameterName = mangleParameterName(outParamInfo.paramName);
          int outParamIndex;
          int outParamIndex;
          if (this.placeholderToParameterIndexMap == null) {
            outParamIndex = outParamInfo.index + 1;
          } else {
            outParamIndex = this.placeholderToParameterIndexMap[(outParamInfo.index - 1)];
          }
          setBytesNoEscapeNoQuotes(outParamIndex, StringUtils.getBytes(outParameterName, this.charConverter, this.charEncoding, this.connection.getServerCharacterEncoding(), this.connection.parserKnowsUnicode(), getExceptionInterceptor()));
        }
      }
    }
  }
  
  public void setShort(String parameterName, short x)
    throws SQLException
  {
    setShort(getNamedParamIndex(parameterName, false), x);
  }
  
  public void setString(String parameterName, String x)
    throws SQLException
  {
    setString(getNamedParamIndex(parameterName, false), x);
  }
  
  public void setTime(String parameterName, Time x)
    throws SQLException
  {
    setTime(getNamedParamIndex(parameterName, false), x);
  }
  
  public void setTime(String parameterName, Time x, Calendar cal)
    throws SQLException
  {
    setTime(getNamedParamIndex(parameterName, false), x, cal);
  }
  
  public void setTimestamp(String parameterName, Timestamp x)
    throws SQLException
  {
    setTimestamp(getNamedParamIndex(parameterName, false), x);
  }
  
  public void setTimestamp(String parameterName, Timestamp x, Calendar cal)
    throws SQLException
  {
    setTimestamp(getNamedParamIndex(parameterName, false), x, cal);
  }
  
  public void setURL(String parameterName, URL val)
    throws SQLException
  {
    setURL(getNamedParamIndex(parameterName, false), val);
  }
  
  public synchronized boolean wasNull()
    throws SQLException
  {
    return this.outputParamWasNull;
  }
  
  public int[] executeBatch()
    throws SQLException
  {
    if (this.hasOutputParams) {
      throw SQLError.createSQLException("Can't call executeBatch() on CallableStatement with OUTPUT parameters", "S1009", getExceptionInterceptor());
    }
    return super.executeBatch();
  }
  
  protected int getParameterIndexOffset()
  {
    if (this.callingStoredFunction) {
      return -1;
    }
    return super.getParameterIndexOffset();
  }
  
  public void setAsciiStream(String parameterName, InputStream x)
    throws SQLException
  {
    setAsciiStream(getNamedParamIndex(parameterName, false), x);
  }
  
  public void setAsciiStream(String parameterName, InputStream x, long length)
    throws SQLException
  {
    setAsciiStream(getNamedParamIndex(parameterName, false), x, length);
  }
  
  public void setBinaryStream(String parameterName, InputStream x)
    throws SQLException
  {
    setBinaryStream(getNamedParamIndex(parameterName, false), x);
  }
  
  public void setBinaryStream(String parameterName, InputStream x, long length)
    throws SQLException
  {
    setBinaryStream(getNamedParamIndex(parameterName, false), x, length);
  }
  
  public void setBlob(String parameterName, Blob x)
    throws SQLException
  {
    setBlob(getNamedParamIndex(parameterName, false), x);
  }
  
  public void setBlob(String parameterName, InputStream inputStream)
    throws SQLException
  {
    setBlob(getNamedParamIndex(parameterName, false), inputStream);
  }
  
  public void setBlob(String parameterName, InputStream inputStream, long length)
    throws SQLException
  {
    setBlob(getNamedParamIndex(parameterName, false), inputStream, length);
  }
  
  public void setCharacterStream(String parameterName, Reader reader)
    throws SQLException
  {
    setCharacterStream(getNamedParamIndex(parameterName, false), reader);
  }
  
  public void setCharacterStream(String parameterName, Reader reader, long length)
    throws SQLException
  {
    setCharacterStream(getNamedParamIndex(parameterName, false), reader, length);
  }
  
  public void setClob(String parameterName, Clob x)
    throws SQLException
  {
    setClob(getNamedParamIndex(parameterName, false), x);
  }
  
  public void setClob(String parameterName, Reader reader)
    throws SQLException
  {
    setClob(getNamedParamIndex(parameterName, false), reader);
  }
  
  public void setClob(String parameterName, Reader reader, long length)
    throws SQLException
  {
    setClob(getNamedParamIndex(parameterName, false), reader, length);
  }
  
  public void setNCharacterStream(String parameterName, Reader value)
    throws SQLException
  {
    setNCharacterStream(getNamedParamIndex(parameterName, false), value);
  }
  
  public void setNCharacterStream(String parameterName, Reader value, long length)
    throws SQLException
  {
    setNCharacterStream(getNamedParamIndex(parameterName, false), value, length);
  }
  
  private boolean checkReadOnlyProcedure()
    throws SQLException
  {
    if (this.connection.getNoAccessToProcedureBodies()) {
      return false;
    }
    synchronized (this.paramInfo)
    {
      if (this.paramInfo.isReadOnlySafeChecked) {
        return this.paramInfo.isReadOnlySafeProcedure;
      }
      ResultSet rs = null;
      PreparedStatement ps = null;
      try
      {
        String procName = extractProcedureName();
        
        String catalog = this.currentCatalog;
        if (procName.indexOf(".") != -1)
        {
          catalog = procName.substring(0, procName.indexOf("."));
          procName = procName.substring(procName.indexOf(".") + 1);
          procName = new String(StringUtils.stripEnclosure(procName.getBytes(), "`", "`"));
        }
        ps = ((DatabaseMetaData)this.connection.getMetaData()).prepareMetaDataSafeStatement("SELECT SQL_DATA_ACCESS FROM  information_schema.routines  WHERE routine_schema = ?  AND routine_name = ?");
        
        ps.setString(1, catalog);
        ps.setString(2, procName);
        rs = ps.executeQuery();
        if (rs.next())
        {
          String sqlDataAccess = rs.getString(1);
          if (("READS SQL DATA".equalsIgnoreCase(sqlDataAccess)) || ("NO SQL".equalsIgnoreCase(sqlDataAccess)))
          {
            synchronized (this.paramInfo)
            {
              this.paramInfo.isReadOnlySafeChecked = true;
              this.paramInfo.isReadOnlySafeProcedure = true;
            }
            ??? = 1;jsr 30;return ???;
          }
        }
      }
      catch (SQLException e) {}finally
      {
        jsr 6;
      }
      localObject3 = returnAddress;
      if (rs != null) {
        rs.close();
      }
      if (ps != null) {
        ps.close();
      }
      ret;
      
      this.paramInfo.isReadOnlySafeChecked = false;
      this.paramInfo.isReadOnlySafeProcedure = false;
    }
    return false;
  }
  
  protected boolean checkReadOnlySafeStatement()
    throws SQLException
  {
    return (super.checkReadOnlySafeStatement()) || (checkReadOnlyProcedure());
  }
  
  public void setObject(String parameterName, Object x, int targetSqlType, int scale)
    throws SQLException
  {}
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\mysql\jdbc\CallableStatement.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */