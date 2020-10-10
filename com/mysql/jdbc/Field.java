package com.mysql.jdbc;

import java.io.UnsupportedEncodingException;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.regex.PatternSyntaxException;

public class Field
{
  private static final int AUTO_INCREMENT_FLAG = 512;
  private static final int NO_CHARSET_INFO = -1;
  private byte[] buffer;
  private int charsetIndex = 0;
  private String charsetName = null;
  private int colDecimals;
  private short colFlag;
  private String collationName = null;
  private ConnectionImpl connection = null;
  private String databaseName = null;
  private int databaseNameLength = -1;
  private int databaseNameStart = -1;
  private int defaultValueLength = -1;
  private int defaultValueStart = -1;
  private String fullName = null;
  private String fullOriginalName = null;
  private boolean isImplicitTempTable = false;
  private long length;
  private int mysqlType = -1;
  private String name;
  private int nameLength;
  private int nameStart;
  private String originalColumnName = null;
  private int originalColumnNameLength = -1;
  private int originalColumnNameStart = -1;
  private String originalTableName = null;
  private int originalTableNameLength = -1;
  private int originalTableNameStart = -1;
  private int precisionAdjustFactor = 0;
  private int sqlType = -1;
  private String tableName;
  private int tableNameLength;
  private int tableNameStart;
  private boolean useOldNameMetadata = false;
  private boolean isSingleBit;
  private int maxBytesPerChar;
  
  Field(ConnectionImpl conn, byte[] buffer, int databaseNameStart, int databaseNameLength, int tableNameStart, int tableNameLength, int originalTableNameStart, int originalTableNameLength, int nameStart, int nameLength, int originalColumnNameStart, int originalColumnNameLength, long length, int mysqlType, short colFlag, int colDecimals, int defaultValueStart, int defaultValueLength, int charsetIndex)
    throws SQLException
  {
    this.connection = conn;
    this.buffer = buffer;
    this.nameStart = nameStart;
    this.nameLength = nameLength;
    this.tableNameStart = tableNameStart;
    this.tableNameLength = tableNameLength;
    this.length = length;
    this.colFlag = colFlag;
    this.colDecimals = colDecimals;
    this.mysqlType = mysqlType;
    
    this.databaseNameStart = databaseNameStart;
    this.databaseNameLength = databaseNameLength;
    
    this.originalTableNameStart = originalTableNameStart;
    this.originalTableNameLength = originalTableNameLength;
    
    this.originalColumnNameStart = originalColumnNameStart;
    this.originalColumnNameLength = originalColumnNameLength;
    
    this.defaultValueStart = defaultValueStart;
    this.defaultValueLength = defaultValueLength;
    
    this.charsetIndex = charsetIndex;
    
    this.sqlType = MysqlDefs.mysqlToJavaType(this.mysqlType);
    
    checkForImplicitTemporaryTable();
    
    boolean isFromFunction = this.originalTableNameLength == 0;
    if (this.mysqlType == 252) {
      if (((this.connection != null) && (this.connection.getBlobsAreStrings())) || ((this.connection.getFunctionsNeverReturnBlobs()) && (isFromFunction)))
      {
        this.sqlType = 12;
        this.mysqlType = 15;
      }
      else if ((this.charsetIndex == 63) || (!this.connection.versionMeetsMinimum(4, 1, 0)))
      {
        if ((this.connection.getUseBlobToStoreUTF8OutsideBMP()) && (shouldSetupForUtf8StringInBlob()))
        {
          setupForUtf8StringInBlob();
        }
        else
        {
          setBlobTypeBasedOnLength();
          this.sqlType = MysqlDefs.mysqlToJavaType(this.mysqlType);
        }
      }
      else
      {
        this.mysqlType = 253;
        this.sqlType = -1;
      }
    }
    if ((this.sqlType == -6) && (this.length == 1L) && (this.connection.getTinyInt1isBit())) {
      if (conn.getTinyInt1isBit()) {
        if (conn.getTransformedBitIsBoolean()) {
          this.sqlType = 16;
        } else {
          this.sqlType = -7;
        }
      }
    }
    if ((!isNativeNumericType()) && (!isNativeDateTimeType()))
    {
      this.charsetName = this.connection.getCharsetNameForIndex(this.charsetIndex);
      
      boolean isBinary = isBinary();
      if ((this.connection.versionMeetsMinimum(4, 1, 0)) && (this.mysqlType == 253) && (isBinary) && (this.charsetIndex == 63)) {
        if ((this.connection != null) && (this.connection.getFunctionsNeverReturnBlobs()) && (isFromFunction))
        {
          this.sqlType = 12;
          this.mysqlType = 15;
        }
        else if (isOpaqueBinary())
        {
          this.sqlType = -3;
        }
      }
      if ((this.connection.versionMeetsMinimum(4, 1, 0)) && (this.mysqlType == 254) && (isBinary) && (this.charsetIndex == 63)) {
        if ((isOpaqueBinary()) && (!this.connection.getBlobsAreStrings())) {
          this.sqlType = -2;
        }
      }
      if (this.mysqlType == 16)
      {
        this.isSingleBit = (this.length == 0L);
        if ((this.connection != null) && ((this.connection.versionMeetsMinimum(5, 0, 21)) || (this.connection.versionMeetsMinimum(5, 1, 10))) && (this.length == 1L)) {
          this.isSingleBit = true;
        }
        if (this.isSingleBit)
        {
          this.sqlType = -7;
        }
        else
        {
          this.sqlType = -3;
          this.colFlag = ((short)(this.colFlag | 0x80));
          this.colFlag = ((short)(this.colFlag | 0x10));
          isBinary = true;
        }
      }
      if ((this.sqlType == -4) && (!isBinary)) {
        this.sqlType = -1;
      } else if ((this.sqlType == -3) && (!isBinary)) {
        this.sqlType = 12;
      }
    }
    else
    {
      this.charsetName = "US-ASCII";
    }
    if (!isUnsigned()) {
      switch (this.mysqlType)
      {
      case 0: 
      case 246: 
        this.precisionAdjustFactor = -1;
        
        break;
      case 4: 
      case 5: 
        this.precisionAdjustFactor = 1;
      }
    } else {
      switch (this.mysqlType)
      {
      case 4: 
      case 5: 
        this.precisionAdjustFactor = 1;
      }
    }
  }
  
  private boolean shouldSetupForUtf8StringInBlob()
    throws SQLException
  {
    String includePattern = this.connection.getUtf8OutsideBmpIncludedColumnNamePattern();
    
    String excludePattern = this.connection.getUtf8OutsideBmpExcludedColumnNamePattern();
    if ((excludePattern != null) && (!StringUtils.isEmptyOrWhitespaceOnly(excludePattern))) {
      try
      {
        if (getOriginalName().matches(excludePattern))
        {
          if ((includePattern != null) && (!StringUtils.isEmptyOrWhitespaceOnly(includePattern))) {
            try
            {
              if (getOriginalName().matches(includePattern)) {
                return true;
              }
            }
            catch (PatternSyntaxException pse)
            {
              SQLException sqlEx = SQLError.createSQLException("Illegal regex specified for \"utf8OutsideBmpIncludedColumnNamePattern\"", "S1009", this.connection.getExceptionInterceptor());
              if (!this.connection.getParanoid()) {
                sqlEx.initCause(pse);
              }
              throw sqlEx;
            }
          }
          return false;
        }
      }
      catch (PatternSyntaxException pse)
      {
        SQLException sqlEx = SQLError.createSQLException("Illegal regex specified for \"utf8OutsideBmpExcludedColumnNamePattern\"", "S1009", this.connection.getExceptionInterceptor());
        if (!this.connection.getParanoid()) {
          sqlEx.initCause(pse);
        }
        throw sqlEx;
      }
    }
    return true;
  }
  
  private void setupForUtf8StringInBlob()
  {
    if ((this.length == 255L) || (this.length == 65535L))
    {
      this.mysqlType = 15;
      this.sqlType = 12;
    }
    else
    {
      this.mysqlType = 253;
      this.sqlType = -1;
    }
    this.charsetIndex = 33;
  }
  
  Field(ConnectionImpl conn, byte[] buffer, int nameStart, int nameLength, int tableNameStart, int tableNameLength, int length, int mysqlType, short colFlag, int colDecimals)
    throws SQLException
  {
    this(conn, buffer, -1, -1, tableNameStart, tableNameLength, -1, -1, nameStart, nameLength, -1, -1, length, mysqlType, colFlag, colDecimals, -1, -1, -1);
  }
  
  Field(String tableName, String columnName, int jdbcType, int length)
  {
    this.tableName = tableName;
    this.name = columnName;
    this.length = length;
    this.sqlType = jdbcType;
    this.colFlag = 0;
    this.colDecimals = 0;
  }
  
  Field(String tableName, String columnName, int charsetIndex, int jdbcType, int length)
  {
    this.tableName = tableName;
    this.name = columnName;
    this.length = length;
    this.sqlType = jdbcType;
    this.colFlag = 0;
    this.colDecimals = 0;
    this.charsetIndex = charsetIndex;
    switch (this.sqlType)
    {
    case -3: 
    case -2: 
      this.colFlag = ((short)(this.colFlag | 0x80));
      this.colFlag = ((short)(this.colFlag | 0x10));
    }
  }
  
  private void checkForImplicitTemporaryTable()
  {
    this.isImplicitTempTable = ((this.tableNameLength > 5) && (this.buffer[this.tableNameStart] == 35) && (this.buffer[(this.tableNameStart + 1)] == 115) && (this.buffer[(this.tableNameStart + 2)] == 113) && (this.buffer[(this.tableNameStart + 3)] == 108) && (this.buffer[(this.tableNameStart + 4)] == 95));
  }
  
  public String getCharacterSet()
    throws SQLException
  {
    return this.charsetName;
  }
  
  public void setCharacterSet(String javaEncodingName)
    throws SQLException
  {
    this.charsetName = javaEncodingName;
    this.charsetIndex = CharsetMapping.getCharsetIndexForMysqlEncodingName(javaEncodingName);
  }
  
  public synchronized String getCollation()
    throws SQLException
  {
    if ((this.collationName == null) && 
      (this.connection != null) && 
      (this.connection.versionMeetsMinimum(4, 1, 0))) {
      if (this.connection.getUseDynamicCharsetInfo())
      {
        DatabaseMetaData dbmd = this.connection.getMetaData();
        
        String quotedIdStr = dbmd.getIdentifierQuoteString();
        if (" ".equals(quotedIdStr)) {
          quotedIdStr = "";
        }
        String csCatalogName = getDatabaseName();
        String csTableName = getOriginalTableName();
        String csColumnName = getOriginalName();
        if ((csCatalogName != null) && (csCatalogName.length() != 0) && (csTableName != null) && (csTableName.length() != 0) && (csColumnName != null) && (csColumnName.length() != 0))
        {
          StringBuffer queryBuf = new StringBuffer(csCatalogName.length() + csTableName.length() + 28);
          
          queryBuf.append("SHOW FULL COLUMNS FROM ");
          queryBuf.append(quotedIdStr);
          queryBuf.append(csCatalogName);
          queryBuf.append(quotedIdStr);
          queryBuf.append(".");
          queryBuf.append(quotedIdStr);
          queryBuf.append(csTableName);
          queryBuf.append(quotedIdStr);
          
          Statement collationStmt = null;
          ResultSet collationRs = null;
          try
          {
            collationStmt = this.connection.createStatement();
            
            collationRs = collationStmt.executeQuery(queryBuf.toString());
            while (collationRs.next()) {
              if (csColumnName.equals(collationRs.getString("Field"))) {
                this.collationName = collationRs.getString("Collation");
              }
            }
          }
          finally
          {
            if (collationRs != null)
            {
              collationRs.close();
              collationRs = null;
            }
            if (collationStmt != null)
            {
              collationStmt.close();
              collationStmt = null;
            }
          }
        }
      }
      else
      {
        this.collationName = CharsetMapping.INDEX_TO_COLLATION[this.charsetIndex];
      }
    }
    return this.collationName;
  }
  
  public String getColumnLabel()
    throws SQLException
  {
    return getName();
  }
  
  public String getDatabaseName()
    throws SQLException
  {
    if ((this.databaseName == null) && (this.databaseNameStart != -1) && (this.databaseNameLength != -1)) {
      this.databaseName = getStringFromBytes(this.databaseNameStart, this.databaseNameLength);
    }
    return this.databaseName;
  }
  
  int getDecimals()
  {
    return this.colDecimals;
  }
  
  public String getFullName()
    throws SQLException
  {
    if (this.fullName == null)
    {
      StringBuffer fullNameBuf = new StringBuffer(getTableName().length() + 1 + getName().length());
      
      fullNameBuf.append(this.tableName);
      
      fullNameBuf.append('.');
      fullNameBuf.append(this.name);
      this.fullName = fullNameBuf.toString();
      fullNameBuf = null;
    }
    return this.fullName;
  }
  
  public String getFullOriginalName()
    throws SQLException
  {
    getOriginalName();
    if (this.originalColumnName == null) {
      return null;
    }
    if (this.fullName == null)
    {
      StringBuffer fullOriginalNameBuf = new StringBuffer(getOriginalTableName().length() + 1 + getOriginalName().length());
      
      fullOriginalNameBuf.append(this.originalTableName);
      
      fullOriginalNameBuf.append('.');
      fullOriginalNameBuf.append(this.originalColumnName);
      this.fullOriginalName = fullOriginalNameBuf.toString();
      fullOriginalNameBuf = null;
    }
    return this.fullOriginalName;
  }
  
  public long getLength()
  {
    return this.length;
  }
  
  public synchronized int getMaxBytesPerCharacter()
    throws SQLException
  {
    if (this.maxBytesPerChar == 0) {
      this.maxBytesPerChar = this.connection.getMaxBytesPerChar(getCharacterSet());
    }
    return this.maxBytesPerChar;
  }
  
  public int getMysqlType()
  {
    return this.mysqlType;
  }
  
  public String getName()
    throws SQLException
  {
    if (this.name == null) {
      this.name = getStringFromBytes(this.nameStart, this.nameLength);
    }
    return this.name;
  }
  
  public String getNameNoAliases()
    throws SQLException
  {
    if (this.useOldNameMetadata) {
      return getName();
    }
    if ((this.connection != null) && (this.connection.versionMeetsMinimum(4, 1, 0))) {
      return getOriginalName();
    }
    return getName();
  }
  
  public String getOriginalName()
    throws SQLException
  {
    if ((this.originalColumnName == null) && (this.originalColumnNameStart != -1) && (this.originalColumnNameLength != -1)) {
      this.originalColumnName = getStringFromBytes(this.originalColumnNameStart, this.originalColumnNameLength);
    }
    return this.originalColumnName;
  }
  
  public String getOriginalTableName()
    throws SQLException
  {
    if ((this.originalTableName == null) && (this.originalTableNameStart != -1) && (this.originalTableNameLength != -1)) {
      this.originalTableName = getStringFromBytes(this.originalTableNameStart, this.originalTableNameLength);
    }
    return this.originalTableName;
  }
  
  public int getPrecisionAdjustFactor()
  {
    return this.precisionAdjustFactor;
  }
  
  public int getSQLType()
  {
    return this.sqlType;
  }
  
  private String getStringFromBytes(int stringStart, int stringLength)
    throws SQLException
  {
    if ((stringStart == -1) || (stringLength == -1)) {
      return null;
    }
    String stringVal = null;
    if (this.connection != null)
    {
      if (this.connection.getUseUnicode())
      {
        String encoding = this.connection.getCharacterSetMetadata();
        if (encoding == null) {
          encoding = this.connection.getEncoding();
        }
        if (encoding != null)
        {
          SingleByteCharsetConverter converter = null;
          if (this.connection != null) {
            converter = this.connection.getCharsetConverter(encoding);
          }
          if (converter != null)
          {
            stringVal = converter.toString(this.buffer, stringStart, stringLength);
          }
          else
          {
            byte[] stringBytes = new byte[stringLength];
            
            int endIndex = stringStart + stringLength;
            int pos = 0;
            for (int i = stringStart; i < endIndex; i++) {
              stringBytes[(pos++)] = this.buffer[i];
            }
            try
            {
              stringVal = new String(stringBytes, encoding);
            }
            catch (UnsupportedEncodingException ue)
            {
              throw new RuntimeException(Messages.getString("Field.12") + encoding + Messages.getString("Field.13"));
            }
          }
        }
        else
        {
          stringVal = StringUtils.toAsciiString(this.buffer, stringStart, stringLength);
        }
      }
      else
      {
        stringVal = StringUtils.toAsciiString(this.buffer, stringStart, stringLength);
      }
    }
    else {
      stringVal = StringUtils.toAsciiString(this.buffer, stringStart, stringLength);
    }
    return stringVal;
  }
  
  public String getTable()
    throws SQLException
  {
    return getTableName();
  }
  
  public String getTableName()
    throws SQLException
  {
    if (this.tableName == null) {
      this.tableName = getStringFromBytes(this.tableNameStart, this.tableNameLength);
    }
    return this.tableName;
  }
  
  public String getTableNameNoAliases()
    throws SQLException
  {
    if (this.connection.versionMeetsMinimum(4, 1, 0)) {
      return getOriginalTableName();
    }
    return getTableName();
  }
  
  public boolean isAutoIncrement()
  {
    return (this.colFlag & 0x200) > 0;
  }
  
  public boolean isBinary()
  {
    return (this.colFlag & 0x80) > 0;
  }
  
  public boolean isBlob()
  {
    return (this.colFlag & 0x10) > 0;
  }
  
  private boolean isImplicitTemporaryTable()
  {
    return this.isImplicitTempTable;
  }
  
  public boolean isMultipleKey()
  {
    return (this.colFlag & 0x8) > 0;
  }
  
  boolean isNotNull()
  {
    return (this.colFlag & 0x1) > 0;
  }
  
  boolean isOpaqueBinary()
    throws SQLException
  {
    if ((this.charsetIndex == 63) && (isBinary()) && ((getMysqlType() == 254) || (getMysqlType() == 253)))
    {
      if ((this.originalTableNameLength == 0) && (this.connection != null) && (!this.connection.versionMeetsMinimum(5, 0, 25))) {
        return false;
      }
      return !isImplicitTemporaryTable();
    }
    return (this.connection.versionMeetsMinimum(4, 1, 0)) && ("binary".equalsIgnoreCase(getCharacterSet()));
  }
  
  public boolean isPrimaryKey()
  {
    return (this.colFlag & 0x2) > 0;
  }
  
  boolean isReadOnly()
    throws SQLException
  {
    if (this.connection.versionMeetsMinimum(4, 1, 0))
    {
      String orgColumnName = getOriginalName();
      String orgTableName = getOriginalTableName();
      
      return (orgColumnName == null) || (orgColumnName.length() <= 0) || (orgTableName == null) || (orgTableName.length() <= 0);
    }
    return false;
  }
  
  public boolean isUniqueKey()
  {
    return (this.colFlag & 0x4) > 0;
  }
  
  public boolean isUnsigned()
  {
    return (this.colFlag & 0x20) > 0;
  }
  
  public void setUnsigned()
  {
    this.colFlag = ((short)(this.colFlag | 0x20));
  }
  
  public boolean isZeroFill()
  {
    return (this.colFlag & 0x40) > 0;
  }
  
  private void setBlobTypeBasedOnLength()
  {
    if (this.length == 255L) {
      this.mysqlType = 249;
    } else if (this.length == 65535L) {
      this.mysqlType = 252;
    } else if (this.length == 16777215L) {
      this.mysqlType = 250;
    } else if (this.length == 4294967295L) {
      this.mysqlType = 251;
    }
  }
  
  private boolean isNativeNumericType()
  {
    return ((this.mysqlType >= 1) && (this.mysqlType <= 5)) || (this.mysqlType == 8) || (this.mysqlType == 13);
  }
  
  private boolean isNativeDateTimeType()
  {
    return (this.mysqlType == 10) || (this.mysqlType == 14) || (this.mysqlType == 12) || (this.mysqlType == 11) || (this.mysqlType == 7);
  }
  
  public void setConnection(ConnectionImpl conn)
  {
    this.connection = conn;
    if ((this.charsetName == null) || (this.charsetIndex == 0)) {
      this.charsetName = this.connection.getEncoding();
    }
  }
  
  void setMysqlType(int type)
  {
    this.mysqlType = type;
    this.sqlType = MysqlDefs.mysqlToJavaType(this.mysqlType);
  }
  
  protected void setUseOldNameMetadata(boolean useOldNameMetadata)
  {
    this.useOldNameMetadata = useOldNameMetadata;
  }
  
  public String toString()
  {
    try
    {
      StringBuffer asString = new StringBuffer();
      asString.append(super.toString());
      asString.append("[");
      asString.append("catalog=");
      asString.append(getDatabaseName());
      asString.append(",tableName=");
      asString.append(getTableName());
      asString.append(",originalTableName=");
      asString.append(getOriginalTableName());
      asString.append(",columnName=");
      asString.append(getName());
      asString.append(",originalColumnName=");
      asString.append(getOriginalName());
      asString.append(",mysqlType=");
      asString.append(getMysqlType());
      asString.append("(");
      asString.append(MysqlDefs.typeToName(getMysqlType()));
      asString.append(")");
      asString.append(",flags=");
      if (isAutoIncrement()) {
        asString.append(" AUTO_INCREMENT");
      }
      if (isPrimaryKey()) {
        asString.append(" PRIMARY_KEY");
      }
      if (isUniqueKey()) {
        asString.append(" UNIQUE_KEY");
      }
      if (isBinary()) {
        asString.append(" BINARY");
      }
      if (isBlob()) {
        asString.append(" BLOB");
      }
      if (isMultipleKey()) {
        asString.append(" MULTI_KEY");
      }
      if (isUnsigned()) {
        asString.append(" UNSIGNED");
      }
      if (isZeroFill()) {
        asString.append(" ZEROFILL");
      }
      asString.append(", charsetIndex=");
      asString.append(this.charsetIndex);
      asString.append(", charsetName=");
      asString.append(this.charsetName);
      
      asString.append("]");
      
      return asString.toString();
    }
    catch (Throwable t) {}
    return super.toString();
  }
  
  protected boolean isSingleBit()
  {
    return this.isSingleBit;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\mysql\jdbc\Field.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */