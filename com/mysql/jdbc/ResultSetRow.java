package com.mysql.jdbc;

import java.io.InputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.StringTokenizer;
import java.util.TimeZone;

public abstract class ResultSetRow
{
  protected ExceptionInterceptor exceptionInterceptor;
  protected Field[] metadata;
  
  protected ResultSetRow(ExceptionInterceptor exceptionInterceptor)
  {
    this.exceptionInterceptor = exceptionInterceptor;
  }
  
  public abstract void closeOpenStreams();
  
  public abstract InputStream getBinaryInputStream(int paramInt)
    throws SQLException;
  
  public abstract byte[] getColumnValue(int paramInt)
    throws SQLException;
  
  protected final Date getDateFast(int columnIndex, byte[] dateAsBytes, int offset, int length, ConnectionImpl conn, ResultSetImpl rs, Calendar targetCalendar)
    throws SQLException
  {
    int year = 0;
    int month = 0;
    int day = 0;
    try
    {
      if (dateAsBytes == null) {
        return null;
      }
      boolean allZeroDate = true;
      
      boolean onlyTimePresent = false;
      for (int i = 0; i < length; i++) {
        if (dateAsBytes[(offset + i)] == 58)
        {
          onlyTimePresent = true;
          break;
        }
      }
      for (int i = 0; i < length; i++)
      {
        byte b = dateAsBytes[(offset + i)];
        if ((b == 32) || (b == 45) || (b == 47)) {
          onlyTimePresent = false;
        }
        if ((b != 48) && (b != 32) && (b != 58) && (b != 45) && (b != 47) && (b != 46))
        {
          allZeroDate = false;
          
          break;
        }
      }
      if ((!onlyTimePresent) && (allZeroDate))
      {
        if ("convertToNull".equals(conn.getZeroDateTimeBehavior())) {
          return null;
        }
        if ("exception".equals(conn.getZeroDateTimeBehavior())) {
          throw SQLError.createSQLException("Value '" + new String(dateAsBytes) + "' can not be represented as java.sql.Date", "S1009", this.exceptionInterceptor);
        }
        return rs.fastDateCreate(targetCalendar, 1, 1, 1);
      }
      if (this.metadata[columnIndex].getMysqlType() == 7)
      {
        switch (length)
        {
        case 19: 
        case 21: 
        case 29: 
          year = StringUtils.getInt(dateAsBytes, offset + 0, offset + 4);
          
          month = StringUtils.getInt(dateAsBytes, offset + 5, offset + 7);
          
          day = StringUtils.getInt(dateAsBytes, offset + 8, offset + 10);
          
          return rs.fastDateCreate(targetCalendar, year, month, day);
        case 8: 
        case 14: 
          year = StringUtils.getInt(dateAsBytes, offset + 0, offset + 4);
          
          month = StringUtils.getInt(dateAsBytes, offset + 4, offset + 6);
          
          day = StringUtils.getInt(dateAsBytes, offset + 6, offset + 8);
          
          return rs.fastDateCreate(targetCalendar, year, month, day);
        case 6: 
        case 10: 
        case 12: 
          year = StringUtils.getInt(dateAsBytes, offset + 0, offset + 2);
          if (year <= 69) {
            year += 100;
          }
          month = StringUtils.getInt(dateAsBytes, offset + 2, offset + 4);
          
          day = StringUtils.getInt(dateAsBytes, offset + 4, offset + 6);
          
          return rs.fastDateCreate(targetCalendar, year + 1900, month, day);
        case 4: 
          year = StringUtils.getInt(dateAsBytes, offset + 0, offset + 4);
          if (year <= 69) {
            year += 100;
          }
          month = StringUtils.getInt(dateAsBytes, offset + 2, offset + 4);
          
          return rs.fastDateCreate(targetCalendar, year + 1900, month, 1);
        case 2: 
          year = StringUtils.getInt(dateAsBytes, offset + 0, offset + 2);
          if (year <= 69) {
            year += 100;
          }
          return rs.fastDateCreate(targetCalendar, year + 1900, 1, 1);
        }
        throw SQLError.createSQLException(Messages.getString("ResultSet.Bad_format_for_Date", new Object[] { new String(dateAsBytes), Constants.integerValueOf(columnIndex + 1) }), "S1009", this.exceptionInterceptor);
      }
      if (this.metadata[columnIndex].getMysqlType() == 13)
      {
        if ((length == 2) || (length == 1))
        {
          year = StringUtils.getInt(dateAsBytes, offset, offset + length);
          if (year <= 69) {
            year += 100;
          }
          year += 1900;
        }
        else
        {
          year = StringUtils.getInt(dateAsBytes, offset + 0, offset + 4);
        }
        return rs.fastDateCreate(targetCalendar, year, 1, 1);
      }
      if (this.metadata[columnIndex].getMysqlType() == 11) {
        return rs.fastDateCreate(targetCalendar, 1970, 1, 1);
      }
      if (length < 10)
      {
        if (length == 8) {
          return rs.fastDateCreate(targetCalendar, 1970, 1, 1);
        }
        throw SQLError.createSQLException(Messages.getString("ResultSet.Bad_format_for_Date", new Object[] { new String(dateAsBytes), Constants.integerValueOf(columnIndex + 1) }), "S1009", this.exceptionInterceptor);
      }
      if (length != 18)
      {
        year = StringUtils.getInt(dateAsBytes, offset + 0, offset + 4);
        
        month = StringUtils.getInt(dateAsBytes, offset + 5, offset + 7);
        
        day = StringUtils.getInt(dateAsBytes, offset + 8, offset + 10);
      }
      else
      {
        StringTokenizer st = new StringTokenizer(new String(dateAsBytes, offset, length, "ISO8859_1"), "- ");
        
        year = Integer.parseInt(st.nextToken());
        month = Integer.parseInt(st.nextToken());
        day = Integer.parseInt(st.nextToken());
      }
      return rs.fastDateCreate(targetCalendar, year, month, day);
    }
    catch (SQLException sqlEx)
    {
      throw sqlEx;
    }
    catch (Exception e)
    {
      SQLException sqlEx = SQLError.createSQLException(Messages.getString("ResultSet.Bad_format_for_Date", new Object[] { new String(dateAsBytes), Constants.integerValueOf(columnIndex + 1) }), "S1009", this.exceptionInterceptor);
      
      sqlEx.initCause(e);
      
      throw sqlEx;
    }
  }
  
  public abstract Date getDateFast(int paramInt, ConnectionImpl paramConnectionImpl, ResultSetImpl paramResultSetImpl, Calendar paramCalendar)
    throws SQLException;
  
  public abstract int getInt(int paramInt)
    throws SQLException;
  
  public abstract long getLong(int paramInt)
    throws SQLException;
  
  protected Date getNativeDate(int columnIndex, byte[] bits, int offset, int length, ConnectionImpl conn, ResultSetImpl rs, Calendar cal)
    throws SQLException
  {
    int year = 0;
    int month = 0;
    int day = 0;
    if (length != 0)
    {
      year = bits[(offset + 0)] & 0xFF | (bits[(offset + 1)] & 0xFF) << 8;
      
      month = bits[(offset + 2)];
      day = bits[(offset + 3)];
    }
    if ((year == 0) && (month == 0) && (day == 0))
    {
      if ("convertToNull".equals(conn.getZeroDateTimeBehavior())) {
        return null;
      }
      if ("exception".equals(conn.getZeroDateTimeBehavior())) {
        throw SQLError.createSQLException("Value '0000-00-00' can not be represented as java.sql.Date", "S1009", this.exceptionInterceptor);
      }
      year = 1;
      month = 1;
      day = 1;
    }
    if (!rs.useLegacyDatetimeCode) {
      return TimeUtil.fastDateCreate(year, month, day, cal);
    }
    return rs.fastDateCreate(cal == null ? rs.getCalendarInstanceForSessionOrNew() : cal, year, month, day);
  }
  
  public abstract Date getNativeDate(int paramInt, ConnectionImpl paramConnectionImpl, ResultSetImpl paramResultSetImpl, Calendar paramCalendar)
    throws SQLException;
  
  protected Object getNativeDateTimeValue(int columnIndex, byte[] bits, int offset, int length, Calendar targetCalendar, int jdbcType, int mysqlType, TimeZone tz, boolean rollForward, ConnectionImpl conn, ResultSetImpl rs)
    throws SQLException
  {
    int year = 0;
    int month = 0;
    int day = 0;
    
    int hour = 0;
    int minute = 0;
    int seconds = 0;
    
    int nanos = 0;
    if (bits == null) {
      return null;
    }
    Calendar sessionCalendar = conn.getUseJDBCCompliantTimezoneShift() ? conn.getUtcCalendar() : rs.getCalendarInstanceForSessionOrNew();
    
    boolean populatedFromDateTimeValue = false;
    switch (mysqlType)
    {
    case 7: 
    case 12: 
      populatedFromDateTimeValue = true;
      if (length != 0)
      {
        year = bits[(offset + 0)] & 0xFF | (bits[(offset + 1)] & 0xFF) << 8;
        
        month = bits[(offset + 2)];
        day = bits[(offset + 3)];
        if (length > 4)
        {
          hour = bits[(offset + 4)];
          minute = bits[(offset + 5)];
          seconds = bits[(offset + 6)];
        }
        if (length > 7) {
          nanos = (bits[(offset + 7)] & 0xFF | (bits[(offset + 8)] & 0xFF) << 8 | (bits[(offset + 9)] & 0xFF) << 16 | (bits[(offset + 10)] & 0xFF) << 24) * 1000;
        }
      }
      break;
    case 10: 
      populatedFromDateTimeValue = true;
      if (bits.length != 0)
      {
        year = bits[(offset + 0)] & 0xFF | (bits[(offset + 1)] & 0xFF) << 8;
        
        month = bits[(offset + 2)];
        day = bits[(offset + 3)];
      }
      break;
    case 11: 
      populatedFromDateTimeValue = true;
      if (bits.length != 0)
      {
        hour = bits[(offset + 5)];
        minute = bits[(offset + 6)];
        seconds = bits[(offset + 7)];
      }
      year = 1970;
      month = 1;
      day = 1;
      
      break;
    case 8: 
    case 9: 
    default: 
      populatedFromDateTimeValue = false;
    }
    switch (jdbcType)
    {
    case 92: 
      if (populatedFromDateTimeValue)
      {
        if (!rs.useLegacyDatetimeCode) {
          return TimeUtil.fastTimeCreate(hour, minute, seconds, targetCalendar, this.exceptionInterceptor);
        }
        Time time = TimeUtil.fastTimeCreate(rs.getCalendarInstanceForSessionOrNew(), hour, minute, seconds, this.exceptionInterceptor);
        
        Time adjustedTime = TimeUtil.changeTimezone(conn, sessionCalendar, targetCalendar, time, conn.getServerTimezoneTZ(), tz, rollForward);
        
        return adjustedTime;
      }
      return rs.getNativeTimeViaParseConversion(columnIndex + 1, targetCalendar, tz, rollForward);
    case 91: 
      if (populatedFromDateTimeValue)
      {
        if ((year == 0) && (month == 0) && (day == 0))
        {
          if ("convertToNull".equals(conn.getZeroDateTimeBehavior())) {
            return null;
          }
          if ("exception".equals(conn.getZeroDateTimeBehavior())) {
            throw new SQLException("Value '0000-00-00' can not be represented as java.sql.Date", "S1009");
          }
          year = 1;
          month = 1;
          day = 1;
        }
        if (!rs.useLegacyDatetimeCode) {
          return TimeUtil.fastDateCreate(year, month, day, targetCalendar);
        }
        return rs.fastDateCreate(rs.getCalendarInstanceForSessionOrNew(), year, month, day);
      }
      return rs.getNativeDateViaParseConversion(columnIndex + 1);
    case 93: 
      if (populatedFromDateTimeValue)
      {
        if ((year == 0) && (month == 0) && (day == 0))
        {
          if ("convertToNull".equals(conn.getZeroDateTimeBehavior())) {
            return null;
          }
          if ("exception".equals(conn.getZeroDateTimeBehavior())) {
            throw new SQLException("Value '0000-00-00' can not be represented as java.sql.Timestamp", "S1009");
          }
          year = 1;
          month = 1;
          day = 1;
        }
        if (!rs.useLegacyDatetimeCode) {
          return TimeUtil.fastTimestampCreate(tz, year, month, day, hour, minute, seconds, nanos);
        }
        Timestamp ts = rs.fastTimestampCreate(rs.getCalendarInstanceForSessionOrNew(), year, month, day, hour, minute, seconds, nanos);
        
        Timestamp adjustedTs = TimeUtil.changeTimezone(conn, sessionCalendar, targetCalendar, ts, conn.getServerTimezoneTZ(), tz, rollForward);
        
        return adjustedTs;
      }
      return rs.getNativeTimestampViaParseConversion(columnIndex + 1, targetCalendar, tz, rollForward);
    }
    throw new SQLException("Internal error - conversion method doesn't support this type", "S1000");
  }
  
  public abstract Object getNativeDateTimeValue(int paramInt1, Calendar paramCalendar, int paramInt2, int paramInt3, TimeZone paramTimeZone, boolean paramBoolean, ConnectionImpl paramConnectionImpl, ResultSetImpl paramResultSetImpl)
    throws SQLException;
  
  protected double getNativeDouble(byte[] bits, int offset)
  {
    long valueAsLong = bits[(offset + 0)] & 0xFF | (bits[(offset + 1)] & 0xFF) << 8 | (bits[(offset + 2)] & 0xFF) << 16 | (bits[(offset + 3)] & 0xFF) << 24 | (bits[(offset + 4)] & 0xFF) << 32 | (bits[(offset + 5)] & 0xFF) << 40 | (bits[(offset + 6)] & 0xFF) << 48 | (bits[(offset + 7)] & 0xFF) << 56;
    
    return Double.longBitsToDouble(valueAsLong);
  }
  
  public abstract double getNativeDouble(int paramInt)
    throws SQLException;
  
  protected float getNativeFloat(byte[] bits, int offset)
  {
    int asInt = bits[(offset + 0)] & 0xFF | (bits[(offset + 1)] & 0xFF) << 8 | (bits[(offset + 2)] & 0xFF) << 16 | (bits[(offset + 3)] & 0xFF) << 24;
    
    return Float.intBitsToFloat(asInt);
  }
  
  public abstract float getNativeFloat(int paramInt)
    throws SQLException;
  
  protected int getNativeInt(byte[] bits, int offset)
  {
    int valueAsInt = bits[(offset + 0)] & 0xFF | (bits[(offset + 1)] & 0xFF) << 8 | (bits[(offset + 2)] & 0xFF) << 16 | (bits[(offset + 3)] & 0xFF) << 24;
    
    return valueAsInt;
  }
  
  public abstract int getNativeInt(int paramInt)
    throws SQLException;
  
  protected long getNativeLong(byte[] bits, int offset)
  {
    long valueAsLong = bits[(offset + 0)] & 0xFF | (bits[(offset + 1)] & 0xFF) << 8 | (bits[(offset + 2)] & 0xFF) << 16 | (bits[(offset + 3)] & 0xFF) << 24 | (bits[(offset + 4)] & 0xFF) << 32 | (bits[(offset + 5)] & 0xFF) << 40 | (bits[(offset + 6)] & 0xFF) << 48 | (bits[(offset + 7)] & 0xFF) << 56;
    
    return valueAsLong;
  }
  
  public abstract long getNativeLong(int paramInt)
    throws SQLException;
  
  protected short getNativeShort(byte[] bits, int offset)
  {
    short asShort = (short)(bits[(offset + 0)] & 0xFF | (bits[(offset + 1)] & 0xFF) << 8);
    
    return asShort;
  }
  
  public abstract short getNativeShort(int paramInt)
    throws SQLException;
  
  protected Time getNativeTime(int columnIndex, byte[] bits, int offset, int length, Calendar targetCalendar, TimeZone tz, boolean rollForward, ConnectionImpl conn, ResultSetImpl rs)
    throws SQLException
  {
    int hour = 0;
    int minute = 0;
    int seconds = 0;
    if (length != 0)
    {
      hour = bits[(offset + 5)];
      minute = bits[(offset + 6)];
      seconds = bits[(offset + 7)];
    }
    if (!rs.useLegacyDatetimeCode) {
      return TimeUtil.fastTimeCreate(hour, minute, seconds, targetCalendar, this.exceptionInterceptor);
    }
    Calendar sessionCalendar = rs.getCalendarInstanceForSessionOrNew();
    synchronized (sessionCalendar)
    {
      Time time = TimeUtil.fastTimeCreate(sessionCalendar, hour, minute, seconds, this.exceptionInterceptor);
      
      Time adjustedTime = TimeUtil.changeTimezone(conn, sessionCalendar, targetCalendar, time, conn.getServerTimezoneTZ(), tz, rollForward);
      
      return adjustedTime;
    }
  }
  
  public abstract Time getNativeTime(int paramInt, Calendar paramCalendar, TimeZone paramTimeZone, boolean paramBoolean, ConnectionImpl paramConnectionImpl, ResultSetImpl paramResultSetImpl)
    throws SQLException;
  
  protected Timestamp getNativeTimestamp(byte[] bits, int offset, int length, Calendar targetCalendar, TimeZone tz, boolean rollForward, ConnectionImpl conn, ResultSetImpl rs)
    throws SQLException
  {
    int year = 0;
    int month = 0;
    int day = 0;
    
    int hour = 0;
    int minute = 0;
    int seconds = 0;
    
    int nanos = 0;
    if (length != 0)
    {
      year = bits[(offset + 0)] & 0xFF | (bits[(offset + 1)] & 0xFF) << 8;
      month = bits[(offset + 2)];
      day = bits[(offset + 3)];
      if (length > 4)
      {
        hour = bits[(offset + 4)];
        minute = bits[(offset + 5)];
        seconds = bits[(offset + 6)];
      }
      if (length > 7) {
        nanos = (bits[(offset + 7)] & 0xFF | (bits[(offset + 8)] & 0xFF) << 8 | (bits[(offset + 9)] & 0xFF) << 16 | (bits[(offset + 10)] & 0xFF) << 24) * 1000;
      }
    }
    if ((year == 0) && (month == 0) && (day == 0))
    {
      if ("convertToNull".equals(conn.getZeroDateTimeBehavior())) {
        return null;
      }
      if ("exception".equals(conn.getZeroDateTimeBehavior())) {
        throw SQLError.createSQLException("Value '0000-00-00' can not be represented as java.sql.Timestamp", "S1009", this.exceptionInterceptor);
      }
      year = 1;
      month = 1;
      day = 1;
    }
    if (!rs.useLegacyDatetimeCode) {
      return TimeUtil.fastTimestampCreate(tz, year, month, day, hour, minute, seconds, nanos);
    }
    Calendar sessionCalendar = conn.getUseJDBCCompliantTimezoneShift() ? conn.getUtcCalendar() : rs.getCalendarInstanceForSessionOrNew();
    synchronized (sessionCalendar)
    {
      Timestamp ts = rs.fastTimestampCreate(sessionCalendar, year, month, day, hour, minute, seconds, nanos);
      
      Timestamp adjustedTs = TimeUtil.changeTimezone(conn, sessionCalendar, targetCalendar, ts, conn.getServerTimezoneTZ(), tz, rollForward);
      
      return adjustedTs;
    }
  }
  
  public abstract Timestamp getNativeTimestamp(int paramInt, Calendar paramCalendar, TimeZone paramTimeZone, boolean paramBoolean, ConnectionImpl paramConnectionImpl, ResultSetImpl paramResultSetImpl)
    throws SQLException;
  
  public abstract Reader getReader(int paramInt)
    throws SQLException;
  
  public abstract String getString(int paramInt, String paramString, ConnectionImpl paramConnectionImpl)
    throws SQLException;
  
  protected String getString(String encoding, ConnectionImpl conn, byte[] value, int offset, int length)
    throws SQLException
  {
    String stringVal = null;
    if ((conn != null) && (conn.getUseUnicode())) {
      try
      {
        if (encoding == null)
        {
          stringVal = new String(value);
        }
        else
        {
          SingleByteCharsetConverter converter = conn.getCharsetConverter(encoding);
          if (converter != null) {
            stringVal = converter.toString(value, offset, length);
          } else {
            stringVal = new String(value, offset, length, encoding);
          }
        }
      }
      catch (UnsupportedEncodingException E)
      {
        throw SQLError.createSQLException(Messages.getString("ResultSet.Unsupported_character_encoding____101") + encoding + "'.", "0S100", this.exceptionInterceptor);
      }
    } else {
      stringVal = StringUtils.toAsciiString(value, offset, length);
    }
    return stringVal;
  }
  
  /* Error */
  protected Time getTimeFast(int columnIndex, byte[] timeAsBytes, int offset, int length, Calendar targetCalendar, TimeZone tz, boolean rollForward, ConnectionImpl conn, ResultSetImpl rs)
    throws SQLException
  {
    // Byte code:
    //   0: iconst_0
    //   1: istore 10
    //   3: iconst_0
    //   4: istore 11
    //   6: iconst_0
    //   7: istore 12
    //   9: aload_2
    //   10: ifnonnull +5 -> 15
    //   13: aconst_null
    //   14: areturn
    //   15: iconst_1
    //   16: istore 13
    //   18: iconst_0
    //   19: istore 14
    //   21: iconst_0
    //   22: istore 15
    //   24: iload 15
    //   26: iload 4
    //   28: if_icmpge +26 -> 54
    //   31: aload_2
    //   32: iload_3
    //   33: iload 15
    //   35: iadd
    //   36: baload
    //   37: bipush 58
    //   39: if_icmpne +9 -> 48
    //   42: iconst_1
    //   43: istore 14
    //   45: goto +9 -> 54
    //   48: iinc 15 1
    //   51: goto -27 -> 24
    //   54: iconst_0
    //   55: istore 15
    //   57: iload 15
    //   59: iload 4
    //   61: if_icmpge +89 -> 150
    //   64: aload_2
    //   65: iload_3
    //   66: iload 15
    //   68: iadd
    //   69: baload
    //   70: istore 16
    //   72: iload 16
    //   74: bipush 32
    //   76: if_icmpeq +17 -> 93
    //   79: iload 16
    //   81: bipush 45
    //   83: if_icmpeq +10 -> 93
    //   86: iload 16
    //   88: bipush 47
    //   90: if_icmpne +6 -> 96
    //   93: iconst_0
    //   94: istore 14
    //   96: iload 16
    //   98: bipush 48
    //   100: if_icmpeq +44 -> 144
    //   103: iload 16
    //   105: bipush 32
    //   107: if_icmpeq +37 -> 144
    //   110: iload 16
    //   112: bipush 58
    //   114: if_icmpeq +30 -> 144
    //   117: iload 16
    //   119: bipush 45
    //   121: if_icmpeq +23 -> 144
    //   124: iload 16
    //   126: bipush 47
    //   128: if_icmpeq +16 -> 144
    //   131: iload 16
    //   133: bipush 46
    //   135: if_icmpeq +9 -> 144
    //   138: iconst_0
    //   139: istore 13
    //   141: goto +9 -> 150
    //   144: iinc 15 1
    //   147: goto -90 -> 57
    //   150: iload 14
    //   152: ifne +88 -> 240
    //   155: iload 13
    //   157: ifeq +83 -> 240
    //   160: ldc 3
    //   162: aload 8
    //   164: invokevirtual 4	com/mysql/jdbc/ConnectionImpl:getZeroDateTimeBehavior	()Ljava/lang/String;
    //   167: invokevirtual 5	java/lang/String:equals	(Ljava/lang/Object;)Z
    //   170: ifeq +5 -> 175
    //   173: aconst_null
    //   174: areturn
    //   175: ldc 6
    //   177: aload 8
    //   179: invokevirtual 4	com/mysql/jdbc/ConnectionImpl:getZeroDateTimeBehavior	()Ljava/lang/String;
    //   182: invokevirtual 5	java/lang/String:equals	(Ljava/lang/Object;)Z
    //   185: ifeq +44 -> 229
    //   188: new 7	java/lang/StringBuffer
    //   191: dup
    //   192: invokespecial 8	java/lang/StringBuffer:<init>	()V
    //   195: ldc 9
    //   197: invokevirtual 10	java/lang/StringBuffer:append	(Ljava/lang/String;)Ljava/lang/StringBuffer;
    //   200: new 11	java/lang/String
    //   203: dup
    //   204: aload_2
    //   205: invokespecial 12	java/lang/String:<init>	([B)V
    //   208: invokevirtual 10	java/lang/StringBuffer:append	(Ljava/lang/String;)Ljava/lang/StringBuffer;
    //   211: ldc 66
    //   213: invokevirtual 10	java/lang/StringBuffer:append	(Ljava/lang/String;)Ljava/lang/StringBuffer;
    //   216: invokevirtual 14	java/lang/StringBuffer:toString	()Ljava/lang/String;
    //   219: ldc 15
    //   221: aload_0
    //   222: getfield 2	com/mysql/jdbc/ResultSetRow:exceptionInterceptor	Lcom/mysql/jdbc/ExceptionInterceptor;
    //   225: invokestatic 16	com/mysql/jdbc/SQLError:createSQLException	(Ljava/lang/String;Ljava/lang/String;Lcom/mysql/jdbc/ExceptionInterceptor;)Ljava/sql/SQLException;
    //   228: athrow
    //   229: aload 9
    //   231: aload 5
    //   233: iconst_0
    //   234: iconst_0
    //   235: iconst_0
    //   236: invokevirtual 67	com/mysql/jdbc/ResultSetImpl:fastTimeCreate	(Ljava/util/Calendar;III)Ljava/sql/Time;
    //   239: areturn
    //   240: aload_0
    //   241: getfield 18	com/mysql/jdbc/ResultSetRow:metadata	[Lcom/mysql/jdbc/Field;
    //   244: iload_1
    //   245: aaload
    //   246: astore 15
    //   248: aload 15
    //   250: invokevirtual 19	com/mysql/jdbc/Field:getMysqlType	()I
    //   253: bipush 7
    //   255: if_icmpne +306 -> 561
    //   258: iload 4
    //   260: tableswitch	default:+203->463, 10:+169->429, 11:+203->463, 12:+113->373, 13:+203->463, 14:+113->373, 15:+203->463, 16:+203->463, 17:+203->463, 18:+203->463, 19:+56->316
    //   316: aload_2
    //   317: iload_3
    //   318: iload 4
    //   320: iadd
    //   321: bipush 8
    //   323: isub
    //   324: iload_3
    //   325: iload 4
    //   327: iadd
    //   328: bipush 6
    //   330: isub
    //   331: invokestatic 20	com/mysql/jdbc/StringUtils:getInt	([BII)I
    //   334: istore 10
    //   336: aload_2
    //   337: iload_3
    //   338: iload 4
    //   340: iadd
    //   341: iconst_5
    //   342: isub
    //   343: iload_3
    //   344: iload 4
    //   346: iadd
    //   347: iconst_3
    //   348: isub
    //   349: invokestatic 20	com/mysql/jdbc/StringUtils:getInt	([BII)I
    //   352: istore 11
    //   354: aload_2
    //   355: iload_3
    //   356: iload 4
    //   358: iadd
    //   359: iconst_2
    //   360: isub
    //   361: iload_3
    //   362: iload 4
    //   364: iadd
    //   365: invokestatic 20	com/mysql/jdbc/StringUtils:getInt	([BII)I
    //   368: istore 12
    //   370: goto +142 -> 512
    //   373: aload_2
    //   374: iload_3
    //   375: iload 4
    //   377: iadd
    //   378: bipush 6
    //   380: isub
    //   381: iload_3
    //   382: iload 4
    //   384: iadd
    //   385: iconst_4
    //   386: isub
    //   387: invokestatic 20	com/mysql/jdbc/StringUtils:getInt	([BII)I
    //   390: istore 10
    //   392: aload_2
    //   393: iload_3
    //   394: iload 4
    //   396: iadd
    //   397: iconst_4
    //   398: isub
    //   399: iload_3
    //   400: iload 4
    //   402: iadd
    //   403: iconst_2
    //   404: isub
    //   405: invokestatic 20	com/mysql/jdbc/StringUtils:getInt	([BII)I
    //   408: istore 11
    //   410: aload_2
    //   411: iload_3
    //   412: iload 4
    //   414: iadd
    //   415: iconst_2
    //   416: isub
    //   417: iload_3
    //   418: iload 4
    //   420: iadd
    //   421: invokestatic 20	com/mysql/jdbc/StringUtils:getInt	([BII)I
    //   424: istore 12
    //   426: goto +86 -> 512
    //   429: aload_2
    //   430: iload_3
    //   431: bipush 6
    //   433: iadd
    //   434: iload_3
    //   435: bipush 8
    //   437: iadd
    //   438: invokestatic 20	com/mysql/jdbc/StringUtils:getInt	([BII)I
    //   441: istore 10
    //   443: aload_2
    //   444: iload_3
    //   445: bipush 8
    //   447: iadd
    //   448: iload_3
    //   449: bipush 10
    //   451: iadd
    //   452: invokestatic 20	com/mysql/jdbc/StringUtils:getInt	([BII)I
    //   455: istore 11
    //   457: iconst_0
    //   458: istore 12
    //   460: goto +52 -> 512
    //   463: new 7	java/lang/StringBuffer
    //   466: dup
    //   467: invokespecial 8	java/lang/StringBuffer:<init>	()V
    //   470: ldc 68
    //   472: invokestatic 62	com/mysql/jdbc/Messages:getString	(Ljava/lang/String;)Ljava/lang/String;
    //   475: invokevirtual 10	java/lang/StringBuffer:append	(Ljava/lang/String;)Ljava/lang/StringBuffer;
    //   478: iload_1
    //   479: iconst_1
    //   480: iadd
    //   481: invokevirtual 69	java/lang/StringBuffer:append	(I)Ljava/lang/StringBuffer;
    //   484: ldc 70
    //   486: invokevirtual 10	java/lang/StringBuffer:append	(Ljava/lang/String;)Ljava/lang/StringBuffer;
    //   489: aload 15
    //   491: invokevirtual 71	java/lang/StringBuffer:append	(Ljava/lang/Object;)Ljava/lang/StringBuffer;
    //   494: ldc 72
    //   496: invokevirtual 10	java/lang/StringBuffer:append	(Ljava/lang/String;)Ljava/lang/StringBuffer;
    //   499: invokevirtual 14	java/lang/StringBuffer:toString	()Ljava/lang/String;
    //   502: ldc 15
    //   504: aload_0
    //   505: getfield 2	com/mysql/jdbc/ResultSetRow:exceptionInterceptor	Lcom/mysql/jdbc/ExceptionInterceptor;
    //   508: invokestatic 16	com/mysql/jdbc/SQLError:createSQLException	(Ljava/lang/String;Ljava/lang/String;Lcom/mysql/jdbc/ExceptionInterceptor;)Ljava/sql/SQLException;
    //   511: athrow
    //   512: new 73	java/sql/SQLWarning
    //   515: dup
    //   516: new 7	java/lang/StringBuffer
    //   519: dup
    //   520: invokespecial 8	java/lang/StringBuffer:<init>	()V
    //   523: ldc 74
    //   525: invokestatic 62	com/mysql/jdbc/Messages:getString	(Ljava/lang/String;)Ljava/lang/String;
    //   528: invokevirtual 10	java/lang/StringBuffer:append	(Ljava/lang/String;)Ljava/lang/StringBuffer;
    //   531: iload_1
    //   532: invokevirtual 69	java/lang/StringBuffer:append	(I)Ljava/lang/StringBuffer;
    //   535: ldc 70
    //   537: invokevirtual 10	java/lang/StringBuffer:append	(Ljava/lang/String;)Ljava/lang/StringBuffer;
    //   540: aload 15
    //   542: invokevirtual 71	java/lang/StringBuffer:append	(Ljava/lang/Object;)Ljava/lang/StringBuffer;
    //   545: ldc 72
    //   547: invokevirtual 10	java/lang/StringBuffer:append	(Ljava/lang/String;)Ljava/lang/StringBuffer;
    //   550: invokevirtual 14	java/lang/StringBuffer:toString	()Ljava/lang/String;
    //   553: invokespecial 75	java/sql/SQLWarning:<init>	(Ljava/lang/String;)V
    //   556: astore 16
    //   558: goto +240 -> 798
    //   561: aload 15
    //   563: invokevirtual 19	com/mysql/jdbc/Field:getMysqlType	()I
    //   566: bipush 12
    //   568: if_icmpne +96 -> 664
    //   571: aload_2
    //   572: iload_3
    //   573: bipush 11
    //   575: iadd
    //   576: iload_3
    //   577: bipush 13
    //   579: iadd
    //   580: invokestatic 20	com/mysql/jdbc/StringUtils:getInt	([BII)I
    //   583: istore 10
    //   585: aload_2
    //   586: iload_3
    //   587: bipush 14
    //   589: iadd
    //   590: iload_3
    //   591: bipush 16
    //   593: iadd
    //   594: invokestatic 20	com/mysql/jdbc/StringUtils:getInt	([BII)I
    //   597: istore 11
    //   599: aload_2
    //   600: iload_3
    //   601: bipush 17
    //   603: iadd
    //   604: iload_3
    //   605: bipush 19
    //   607: iadd
    //   608: invokestatic 20	com/mysql/jdbc/StringUtils:getInt	([BII)I
    //   611: istore 12
    //   613: new 73	java/sql/SQLWarning
    //   616: dup
    //   617: new 7	java/lang/StringBuffer
    //   620: dup
    //   621: invokespecial 8	java/lang/StringBuffer:<init>	()V
    //   624: ldc 76
    //   626: invokestatic 62	com/mysql/jdbc/Messages:getString	(Ljava/lang/String;)Ljava/lang/String;
    //   629: invokevirtual 10	java/lang/StringBuffer:append	(Ljava/lang/String;)Ljava/lang/StringBuffer;
    //   632: iload_1
    //   633: iconst_1
    //   634: iadd
    //   635: invokevirtual 69	java/lang/StringBuffer:append	(I)Ljava/lang/StringBuffer;
    //   638: ldc 70
    //   640: invokevirtual 10	java/lang/StringBuffer:append	(Ljava/lang/String;)Ljava/lang/StringBuffer;
    //   643: aload 15
    //   645: invokevirtual 71	java/lang/StringBuffer:append	(Ljava/lang/Object;)Ljava/lang/StringBuffer;
    //   648: ldc 72
    //   650: invokevirtual 10	java/lang/StringBuffer:append	(Ljava/lang/String;)Ljava/lang/StringBuffer;
    //   653: invokevirtual 14	java/lang/StringBuffer:toString	()Ljava/lang/String;
    //   656: invokespecial 75	java/sql/SQLWarning:<init>	(Ljava/lang/String;)V
    //   659: astore 16
    //   661: goto +137 -> 798
    //   664: aload 15
    //   666: invokevirtual 19	com/mysql/jdbc/Field:getMysqlType	()I
    //   669: bipush 10
    //   671: if_icmpne +13 -> 684
    //   674: aload 9
    //   676: aconst_null
    //   677: iconst_0
    //   678: iconst_0
    //   679: iconst_0
    //   680: invokevirtual 67	com/mysql/jdbc/ResultSetImpl:fastTimeCreate	(Ljava/util/Calendar;III)Ljava/sql/Time;
    //   683: areturn
    //   684: iload 4
    //   686: iconst_5
    //   687: if_icmpeq +63 -> 750
    //   690: iload 4
    //   692: bipush 8
    //   694: if_icmpeq +56 -> 750
    //   697: new 7	java/lang/StringBuffer
    //   700: dup
    //   701: invokespecial 8	java/lang/StringBuffer:<init>	()V
    //   704: ldc 77
    //   706: invokestatic 62	com/mysql/jdbc/Messages:getString	(Ljava/lang/String;)Ljava/lang/String;
    //   709: invokevirtual 10	java/lang/StringBuffer:append	(Ljava/lang/String;)Ljava/lang/StringBuffer;
    //   712: new 11	java/lang/String
    //   715: dup
    //   716: aload_2
    //   717: invokespecial 12	java/lang/String:<init>	([B)V
    //   720: invokevirtual 10	java/lang/StringBuffer:append	(Ljava/lang/String;)Ljava/lang/StringBuffer;
    //   723: ldc 78
    //   725: invokestatic 62	com/mysql/jdbc/Messages:getString	(Ljava/lang/String;)Ljava/lang/String;
    //   728: invokevirtual 10	java/lang/StringBuffer:append	(Ljava/lang/String;)Ljava/lang/StringBuffer;
    //   731: iload_1
    //   732: iconst_1
    //   733: iadd
    //   734: invokevirtual 69	java/lang/StringBuffer:append	(I)Ljava/lang/StringBuffer;
    //   737: invokevirtual 14	java/lang/StringBuffer:toString	()Ljava/lang/String;
    //   740: ldc 15
    //   742: aload_0
    //   743: getfield 2	com/mysql/jdbc/ResultSetRow:exceptionInterceptor	Lcom/mysql/jdbc/ExceptionInterceptor;
    //   746: invokestatic 16	com/mysql/jdbc/SQLError:createSQLException	(Ljava/lang/String;Ljava/lang/String;Lcom/mysql/jdbc/ExceptionInterceptor;)Ljava/sql/SQLException;
    //   749: athrow
    //   750: aload_2
    //   751: iload_3
    //   752: iconst_0
    //   753: iadd
    //   754: iload_3
    //   755: iconst_2
    //   756: iadd
    //   757: invokestatic 20	com/mysql/jdbc/StringUtils:getInt	([BII)I
    //   760: istore 10
    //   762: aload_2
    //   763: iload_3
    //   764: iconst_3
    //   765: iadd
    //   766: iload_3
    //   767: iconst_5
    //   768: iadd
    //   769: invokestatic 20	com/mysql/jdbc/StringUtils:getInt	([BII)I
    //   772: istore 11
    //   774: iload 4
    //   776: iconst_5
    //   777: if_icmpne +7 -> 784
    //   780: iconst_0
    //   781: goto +15 -> 796
    //   784: aload_2
    //   785: iload_3
    //   786: bipush 6
    //   788: iadd
    //   789: iload_3
    //   790: bipush 8
    //   792: iadd
    //   793: invokestatic 20	com/mysql/jdbc/StringUtils:getInt	([BII)I
    //   796: istore 12
    //   798: aload 9
    //   800: invokevirtual 38	com/mysql/jdbc/ResultSetImpl:getCalendarInstanceForSessionOrNew	()Ljava/util/Calendar;
    //   803: astore 16
    //   805: aload 9
    //   807: getfield 36	com/mysql/jdbc/ResultSetImpl:useLegacyDatetimeCode	Z
    //   810: ifne +17 -> 827
    //   813: aload 9
    //   815: aload 5
    //   817: iload 10
    //   819: iload 11
    //   821: iload 12
    //   823: invokevirtual 67	com/mysql/jdbc/ResultSetImpl:fastTimeCreate	(Ljava/util/Calendar;III)Ljava/sql/Time;
    //   826: areturn
    //   827: aload 16
    //   829: dup
    //   830: astore 17
    //   832: monitorenter
    //   833: aload 8
    //   835: aload 16
    //   837: aload 5
    //   839: aload 9
    //   841: aload 16
    //   843: iload 10
    //   845: iload 11
    //   847: iload 12
    //   849: invokevirtual 67	com/mysql/jdbc/ResultSetImpl:fastTimeCreate	(Ljava/util/Calendar;III)Ljava/sql/Time;
    //   852: aload 8
    //   854: invokevirtual 43	com/mysql/jdbc/ConnectionImpl:getServerTimezoneTZ	()Ljava/util/TimeZone;
    //   857: aload 6
    //   859: iload 7
    //   861: invokestatic 44	com/mysql/jdbc/TimeUtil:changeTimezone	(Lcom/mysql/jdbc/ConnectionImpl;Ljava/util/Calendar;Ljava/util/Calendar;Ljava/sql/Time;Ljava/util/TimeZone;Ljava/util/TimeZone;Z)Ljava/sql/Time;
    //   864: aload 17
    //   866: monitorexit
    //   867: areturn
    //   868: astore 18
    //   870: aload 17
    //   872: monitorexit
    //   873: aload 18
    //   875: athrow
    //   876: astore 13
    //   878: aload 13
    //   880: invokevirtual 79	java/lang/Exception:toString	()Ljava/lang/String;
    //   883: ldc 15
    //   885: aload_0
    //   886: getfield 2	com/mysql/jdbc/ResultSetRow:exceptionInterceptor	Lcom/mysql/jdbc/ExceptionInterceptor;
    //   889: invokestatic 16	com/mysql/jdbc/SQLError:createSQLException	(Ljava/lang/String;Ljava/lang/String;Lcom/mysql/jdbc/ExceptionInterceptor;)Ljava/sql/SQLException;
    //   892: astore 14
    //   894: aload 14
    //   896: aload 13
    //   898: invokevirtual 34	java/sql/SQLException:initCause	(Ljava/lang/Throwable;)Ljava/lang/Throwable;
    //   901: pop
    //   902: aload 14
    //   904: athrow
    // Line number table:
    //   Java source line #818	-> byte code offset #0
    //   Java source line #819	-> byte code offset #3
    //   Java source line #820	-> byte code offset #6
    //   Java source line #824	-> byte code offset #9
    //   Java source line #825	-> byte code offset #13
    //   Java source line #828	-> byte code offset #15
    //   Java source line #829	-> byte code offset #18
    //   Java source line #831	-> byte code offset #21
    //   Java source line #832	-> byte code offset #31
    //   Java source line #833	-> byte code offset #42
    //   Java source line #834	-> byte code offset #45
    //   Java source line #831	-> byte code offset #48
    //   Java source line #838	-> byte code offset #54
    //   Java source line #839	-> byte code offset #64
    //   Java source line #841	-> byte code offset #72
    //   Java source line #842	-> byte code offset #93
    //   Java source line #845	-> byte code offset #96
    //   Java source line #847	-> byte code offset #138
    //   Java source line #849	-> byte code offset #141
    //   Java source line #838	-> byte code offset #144
    //   Java source line #853	-> byte code offset #150
    //   Java source line #854	-> byte code offset #160
    //   Java source line #856	-> byte code offset #173
    //   Java source line #857	-> byte code offset #175
    //   Java source line #859	-> byte code offset #188
    //   Java source line #867	-> byte code offset #229
    //   Java source line #870	-> byte code offset #240
    //   Java source line #872	-> byte code offset #248
    //   Java source line #874	-> byte code offset #258
    //   Java source line #877	-> byte code offset #316
    //   Java source line #879	-> byte code offset #336
    //   Java source line #881	-> byte code offset #354
    //   Java source line #885	-> byte code offset #370
    //   Java source line #888	-> byte code offset #373
    //   Java source line #890	-> byte code offset #392
    //   Java source line #892	-> byte code offset #410
    //   Java source line #896	-> byte code offset #426
    //   Java source line #899	-> byte code offset #429
    //   Java source line #901	-> byte code offset #443
    //   Java source line #903	-> byte code offset #457
    //   Java source line #906	-> byte code offset #460
    //   Java source line #909	-> byte code offset #463
    //   Java source line #919	-> byte code offset #512
    //   Java source line #928	-> byte code offset #561
    //   Java source line #929	-> byte code offset #571
    //   Java source line #930	-> byte code offset #585
    //   Java source line #931	-> byte code offset #599
    //   Java source line #933	-> byte code offset #613
    //   Java source line #943	-> byte code offset #664
    //   Java source line #944	-> byte code offset #674
    //   Java source line #949	-> byte code offset #684
    //   Java source line #950	-> byte code offset #697
    //   Java source line #958	-> byte code offset #750
    //   Java source line #959	-> byte code offset #762
    //   Java source line #960	-> byte code offset #774
    //   Java source line #964	-> byte code offset #798
    //   Java source line #966	-> byte code offset #805
    //   Java source line #967	-> byte code offset #813
    //   Java source line #970	-> byte code offset #827
    //   Java source line #971	-> byte code offset #833
    //   Java source line #975	-> byte code offset #868
    //   Java source line #976	-> byte code offset #876
    //   Java source line #977	-> byte code offset #878
    //   Java source line #979	-> byte code offset #894
    //   Java source line #981	-> byte code offset #902
    // Local variable table:
    //   start	length	slot	name	signature
    //   0	905	0	this	ResultSetRow
    //   0	905	1	columnIndex	int
    //   0	905	2	timeAsBytes	byte[]
    //   0	905	3	offset	int
    //   0	905	4	length	int
    //   0	905	5	targetCalendar	Calendar
    //   0	905	6	tz	TimeZone
    //   0	905	7	rollForward	boolean
    //   0	905	8	conn	ConnectionImpl
    //   0	905	9	rs	ResultSetImpl
    //   1	843	10	hr	int
    //   4	842	11	min	int
    //   7	841	12	sec	int
    //   16	140	13	allZeroTime	boolean
    //   876	21	13	ex	Exception
    //   19	132	14	onlyTimePresent	boolean
    //   892	11	14	sqlEx	SQLException
    //   22	27	15	i	int
    //   55	90	15	i	int
    //   246	419	15	timeColField	Field
    //   70	62	16	b	byte
    //   556	3	16	precisionLost	java.sql.SQLWarning
    //   659	3	16	precisionLost	java.sql.SQLWarning
    //   803	39	16	sessionCalendar	Calendar
    //   830	41	17	Ljava/lang/Object;	Object
    //   868	6	18	localObject1	Object
    // Exception table:
    //   from	to	target	type
    //   833	867	868	finally
    //   868	873	868	finally
    //   9	14	876	java/lang/Exception
    //   15	174	876	java/lang/Exception
    //   175	239	876	java/lang/Exception
    //   240	683	876	java/lang/Exception
    //   684	826	876	java/lang/Exception
    //   827	867	876	java/lang/Exception
    //   868	876	876	java/lang/Exception
  }
  
  public abstract Time getTimeFast(int paramInt, Calendar paramCalendar, TimeZone paramTimeZone, boolean paramBoolean, ConnectionImpl paramConnectionImpl, ResultSetImpl paramResultSetImpl)
    throws SQLException;
  
  /* Error */
  protected Timestamp getTimestampFast(int columnIndex, byte[] timestampAsBytes, int offset, int length, Calendar targetCalendar, TimeZone tz, boolean rollForward, ConnectionImpl conn, ResultSetImpl rs)
    throws SQLException
  {
    // Byte code:
    //   0: aload 8
    //   2: invokevirtual 39	com/mysql/jdbc/ConnectionImpl:getUseJDBCCompliantTimezoneShift	()Z
    //   5: ifeq +11 -> 16
    //   8: aload 8
    //   10: invokevirtual 40	com/mysql/jdbc/ConnectionImpl:getUtcCalendar	()Ljava/util/Calendar;
    //   13: goto +8 -> 21
    //   16: aload 9
    //   18: invokevirtual 38	com/mysql/jdbc/ResultSetImpl:getCalendarInstanceForSessionOrNew	()Ljava/util/Calendar;
    //   21: astore 10
    //   23: aload 10
    //   25: dup
    //   26: astore 11
    //   28: monitorenter
    //   29: iconst_1
    //   30: istore 12
    //   32: iconst_0
    //   33: istore 13
    //   35: iconst_0
    //   36: istore 14
    //   38: iload 14
    //   40: iload 4
    //   42: if_icmpge +26 -> 68
    //   45: aload_2
    //   46: iload_3
    //   47: iload 14
    //   49: iadd
    //   50: baload
    //   51: bipush 58
    //   53: if_icmpne +9 -> 62
    //   56: iconst_1
    //   57: istore 13
    //   59: goto +9 -> 68
    //   62: iinc 14 1
    //   65: goto -27 -> 38
    //   68: iconst_0
    //   69: istore 14
    //   71: iload 14
    //   73: iload 4
    //   75: if_icmpge +89 -> 164
    //   78: aload_2
    //   79: iload_3
    //   80: iload 14
    //   82: iadd
    //   83: baload
    //   84: istore 15
    //   86: iload 15
    //   88: bipush 32
    //   90: if_icmpeq +17 -> 107
    //   93: iload 15
    //   95: bipush 45
    //   97: if_icmpeq +10 -> 107
    //   100: iload 15
    //   102: bipush 47
    //   104: if_icmpne +6 -> 110
    //   107: iconst_0
    //   108: istore 13
    //   110: iload 15
    //   112: bipush 48
    //   114: if_icmpeq +44 -> 158
    //   117: iload 15
    //   119: bipush 32
    //   121: if_icmpeq +37 -> 158
    //   124: iload 15
    //   126: bipush 58
    //   128: if_icmpeq +30 -> 158
    //   131: iload 15
    //   133: bipush 45
    //   135: if_icmpeq +23 -> 158
    //   138: iload 15
    //   140: bipush 47
    //   142: if_icmpeq +16 -> 158
    //   145: iload 15
    //   147: bipush 46
    //   149: if_icmpeq +9 -> 158
    //   152: iconst_0
    //   153: istore 12
    //   155: goto +9 -> 164
    //   158: iinc 14 1
    //   161: goto -90 -> 71
    //   164: iload 13
    //   166: ifne +114 -> 280
    //   169: iload 12
    //   171: ifeq +109 -> 280
    //   174: ldc 3
    //   176: aload 8
    //   178: invokevirtual 4	com/mysql/jdbc/ConnectionImpl:getZeroDateTimeBehavior	()Ljava/lang/String;
    //   181: invokevirtual 5	java/lang/String:equals	(Ljava/lang/Object;)Z
    //   184: ifeq +8 -> 192
    //   187: aconst_null
    //   188: aload 11
    //   190: monitorexit
    //   191: areturn
    //   192: ldc 6
    //   194: aload 8
    //   196: invokevirtual 4	com/mysql/jdbc/ConnectionImpl:getZeroDateTimeBehavior	()Ljava/lang/String;
    //   199: invokevirtual 5	java/lang/String:equals	(Ljava/lang/Object;)Z
    //   202: ifeq +37 -> 239
    //   205: new 7	java/lang/StringBuffer
    //   208: dup
    //   209: invokespecial 8	java/lang/StringBuffer:<init>	()V
    //   212: ldc 9
    //   214: invokevirtual 10	java/lang/StringBuffer:append	(Ljava/lang/String;)Ljava/lang/StringBuffer;
    //   217: aload_2
    //   218: invokevirtual 71	java/lang/StringBuffer:append	(Ljava/lang/Object;)Ljava/lang/StringBuffer;
    //   221: ldc 80
    //   223: invokevirtual 10	java/lang/StringBuffer:append	(Ljava/lang/String;)Ljava/lang/StringBuffer;
    //   226: invokevirtual 14	java/lang/StringBuffer:toString	()Ljava/lang/String;
    //   229: ldc 15
    //   231: aload_0
    //   232: getfield 2	com/mysql/jdbc/ResultSetRow:exceptionInterceptor	Lcom/mysql/jdbc/ExceptionInterceptor;
    //   235: invokestatic 16	com/mysql/jdbc/SQLError:createSQLException	(Ljava/lang/String;Ljava/lang/String;Lcom/mysql/jdbc/ExceptionInterceptor;)Ljava/sql/SQLException;
    //   238: athrow
    //   239: aload 9
    //   241: getfield 36	com/mysql/jdbc/ResultSetImpl:useLegacyDatetimeCode	Z
    //   244: ifne +19 -> 263
    //   247: aload 6
    //   249: iconst_1
    //   250: iconst_1
    //   251: iconst_1
    //   252: iconst_0
    //   253: iconst_0
    //   254: iconst_0
    //   255: iconst_0
    //   256: invokestatic 49	com/mysql/jdbc/TimeUtil:fastTimestampCreate	(Ljava/util/TimeZone;IIIIIII)Ljava/sql/Timestamp;
    //   259: aload 11
    //   261: monitorexit
    //   262: areturn
    //   263: aload 9
    //   265: aconst_null
    //   266: iconst_1
    //   267: iconst_1
    //   268: iconst_1
    //   269: iconst_0
    //   270: iconst_0
    //   271: iconst_0
    //   272: iconst_0
    //   273: invokevirtual 50	com/mysql/jdbc/ResultSetImpl:fastTimestampCreate	(Ljava/util/Calendar;IIIIIII)Ljava/sql/Timestamp;
    //   276: aload 11
    //   278: monitorexit
    //   279: areturn
    //   280: aload_0
    //   281: getfield 18	com/mysql/jdbc/ResultSetRow:metadata	[Lcom/mysql/jdbc/Field;
    //   284: iload_1
    //   285: aaload
    //   286: invokevirtual 19	com/mysql/jdbc/Field:getMysqlType	()I
    //   289: bipush 13
    //   291: if_icmpne +73 -> 364
    //   294: aload 9
    //   296: getfield 36	com/mysql/jdbc/ResultSetImpl:useLegacyDatetimeCode	Z
    //   299: ifne +24 -> 323
    //   302: aload 6
    //   304: aload_2
    //   305: iload_3
    //   306: iconst_4
    //   307: invokestatic 20	com/mysql/jdbc/StringUtils:getInt	([BII)I
    //   310: iconst_1
    //   311: iconst_1
    //   312: iconst_0
    //   313: iconst_0
    //   314: iconst_0
    //   315: iconst_0
    //   316: invokestatic 49	com/mysql/jdbc/TimeUtil:fastTimestampCreate	(Ljava/util/TimeZone;IIIIIII)Ljava/sql/Timestamp;
    //   319: aload 11
    //   321: monitorexit
    //   322: areturn
    //   323: aload 8
    //   325: aload 10
    //   327: aload 5
    //   329: aload 9
    //   331: aload 10
    //   333: aload_2
    //   334: iload_3
    //   335: iconst_4
    //   336: invokestatic 20	com/mysql/jdbc/StringUtils:getInt	([BII)I
    //   339: iconst_1
    //   340: iconst_1
    //   341: iconst_0
    //   342: iconst_0
    //   343: iconst_0
    //   344: iconst_0
    //   345: invokevirtual 50	com/mysql/jdbc/ResultSetImpl:fastTimestampCreate	(Ljava/util/Calendar;IIIIIII)Ljava/sql/Timestamp;
    //   348: aload 8
    //   350: invokevirtual 43	com/mysql/jdbc/ConnectionImpl:getServerTimezoneTZ	()Ljava/util/TimeZone;
    //   353: aload 6
    //   355: iload 7
    //   357: invokestatic 51	com/mysql/jdbc/TimeUtil:changeTimezone	(Lcom/mysql/jdbc/ConnectionImpl;Ljava/util/Calendar;Ljava/util/Calendar;Ljava/sql/Timestamp;Ljava/util/TimeZone;Ljava/util/TimeZone;Z)Ljava/sql/Timestamp;
    //   360: aload 11
    //   362: monitorexit
    //   363: areturn
    //   364: aload_2
    //   365: iload_3
    //   366: iload 4
    //   368: iadd
    //   369: iconst_1
    //   370: isub
    //   371: baload
    //   372: bipush 46
    //   374: if_icmpne +6 -> 380
    //   377: iinc 4 -1
    //   380: iconst_0
    //   381: istore 14
    //   383: iconst_0
    //   384: istore 15
    //   386: iconst_0
    //   387: istore 16
    //   389: iconst_0
    //   390: istore 17
    //   392: iconst_0
    //   393: istore 18
    //   395: iconst_0
    //   396: istore 19
    //   398: iconst_0
    //   399: istore 20
    //   401: iload 4
    //   403: tableswitch	default:+1009->1412, 2:+966->1369, 3:+1009->1412, 4:+922->1325, 5:+1009->1412, 6:+860->1263, 7:+1009->1412, 8:+714->1117, 9:+1009->1412, 10:+521->924, 11:+1009->1412, 12:+417->820, 13:+1009->1412, 14:+333->736, 15:+1009->1412, 16:+1009->1412, 17:+1009->1412, 18:+1009->1412, 19:+125->528, 20:+125->528, 21:+125->528, 22:+125->528, 23:+125->528, 24:+125->528, 25:+125->528, 26:+125->528, 27:+1009->1412, 28:+1009->1412, 29:+125->528
    //   528: aload_2
    //   529: iload_3
    //   530: iconst_0
    //   531: iadd
    //   532: iload_3
    //   533: iconst_4
    //   534: iadd
    //   535: invokestatic 20	com/mysql/jdbc/StringUtils:getInt	([BII)I
    //   538: istore 14
    //   540: aload_2
    //   541: iload_3
    //   542: iconst_5
    //   543: iadd
    //   544: iload_3
    //   545: bipush 7
    //   547: iadd
    //   548: invokestatic 20	com/mysql/jdbc/StringUtils:getInt	([BII)I
    //   551: istore 15
    //   553: aload_2
    //   554: iload_3
    //   555: bipush 8
    //   557: iadd
    //   558: iload_3
    //   559: bipush 10
    //   561: iadd
    //   562: invokestatic 20	com/mysql/jdbc/StringUtils:getInt	([BII)I
    //   565: istore 16
    //   567: aload_2
    //   568: iload_3
    //   569: bipush 11
    //   571: iadd
    //   572: iload_3
    //   573: bipush 13
    //   575: iadd
    //   576: invokestatic 20	com/mysql/jdbc/StringUtils:getInt	([BII)I
    //   579: istore 17
    //   581: aload_2
    //   582: iload_3
    //   583: bipush 14
    //   585: iadd
    //   586: iload_3
    //   587: bipush 16
    //   589: iadd
    //   590: invokestatic 20	com/mysql/jdbc/StringUtils:getInt	([BII)I
    //   593: istore 18
    //   595: aload_2
    //   596: iload_3
    //   597: bipush 17
    //   599: iadd
    //   600: iload_3
    //   601: bipush 19
    //   603: iadd
    //   604: invokestatic 20	com/mysql/jdbc/StringUtils:getInt	([BII)I
    //   607: istore 19
    //   609: iconst_0
    //   610: istore 20
    //   612: iload 4
    //   614: bipush 19
    //   616: if_icmple +848 -> 1464
    //   619: iconst_m1
    //   620: istore 21
    //   622: iconst_0
    //   623: istore 22
    //   625: iload 22
    //   627: iload 4
    //   629: if_icmpge +24 -> 653
    //   632: aload_2
    //   633: iload_3
    //   634: iload 22
    //   636: iadd
    //   637: baload
    //   638: bipush 46
    //   640: if_icmpne +7 -> 647
    //   643: iload 22
    //   645: istore 21
    //   647: iinc 22 1
    //   650: goto -25 -> 625
    //   653: iload 21
    //   655: iconst_m1
    //   656: if_icmpeq +77 -> 733
    //   659: iload 21
    //   661: iconst_2
    //   662: iadd
    //   663: iload 4
    //   665: if_icmpgt +60 -> 725
    //   668: aload_2
    //   669: iload 21
    //   671: iconst_1
    //   672: iadd
    //   673: iload_3
    //   674: iload 4
    //   676: iadd
    //   677: invokestatic 20	com/mysql/jdbc/StringUtils:getInt	([BII)I
    //   680: istore 20
    //   682: iload_3
    //   683: iload 4
    //   685: iadd
    //   686: iload 21
    //   688: iconst_1
    //   689: iadd
    //   690: isub
    //   691: istore 22
    //   693: iload 22
    //   695: bipush 9
    //   697: if_icmpge +25 -> 722
    //   700: ldc2_w 81
    //   703: bipush 9
    //   705: iload 22
    //   707: isub
    //   708: i2d
    //   709: invokestatic 83	java/lang/Math:pow	(DD)D
    //   712: d2i
    //   713: istore 23
    //   715: iload 20
    //   717: iload 23
    //   719: imul
    //   720: istore 20
    //   722: goto +11 -> 733
    //   725: new 84	java/lang/IllegalArgumentException
    //   728: dup
    //   729: invokespecial 85	java/lang/IllegalArgumentException:<init>	()V
    //   732: athrow
    //   733: goto +731 -> 1464
    //   736: aload_2
    //   737: iload_3
    //   738: iconst_0
    //   739: iadd
    //   740: iload_3
    //   741: iconst_4
    //   742: iadd
    //   743: invokestatic 20	com/mysql/jdbc/StringUtils:getInt	([BII)I
    //   746: istore 14
    //   748: aload_2
    //   749: iload_3
    //   750: iconst_4
    //   751: iadd
    //   752: iload_3
    //   753: bipush 6
    //   755: iadd
    //   756: invokestatic 20	com/mysql/jdbc/StringUtils:getInt	([BII)I
    //   759: istore 15
    //   761: aload_2
    //   762: iload_3
    //   763: bipush 6
    //   765: iadd
    //   766: iload_3
    //   767: bipush 8
    //   769: iadd
    //   770: invokestatic 20	com/mysql/jdbc/StringUtils:getInt	([BII)I
    //   773: istore 16
    //   775: aload_2
    //   776: iload_3
    //   777: bipush 8
    //   779: iadd
    //   780: iload_3
    //   781: bipush 10
    //   783: iadd
    //   784: invokestatic 20	com/mysql/jdbc/StringUtils:getInt	([BII)I
    //   787: istore 17
    //   789: aload_2
    //   790: iload_3
    //   791: bipush 10
    //   793: iadd
    //   794: iload_3
    //   795: bipush 12
    //   797: iadd
    //   798: invokestatic 20	com/mysql/jdbc/StringUtils:getInt	([BII)I
    //   801: istore 18
    //   803: aload_2
    //   804: iload_3
    //   805: bipush 12
    //   807: iadd
    //   808: iload_3
    //   809: bipush 14
    //   811: iadd
    //   812: invokestatic 20	com/mysql/jdbc/StringUtils:getInt	([BII)I
    //   815: istore 19
    //   817: goto +647 -> 1464
    //   820: aload_2
    //   821: iload_3
    //   822: iconst_0
    //   823: iadd
    //   824: iload_3
    //   825: iconst_2
    //   826: iadd
    //   827: invokestatic 20	com/mysql/jdbc/StringUtils:getInt	([BII)I
    //   830: istore 14
    //   832: iload 14
    //   834: bipush 69
    //   836: if_icmpgt +10 -> 846
    //   839: iload 14
    //   841: bipush 100
    //   843: iadd
    //   844: istore 14
    //   846: iload 14
    //   848: sipush 1900
    //   851: iadd
    //   852: istore 14
    //   854: aload_2
    //   855: iload_3
    //   856: iconst_2
    //   857: iadd
    //   858: iload_3
    //   859: iconst_4
    //   860: iadd
    //   861: invokestatic 20	com/mysql/jdbc/StringUtils:getInt	([BII)I
    //   864: istore 15
    //   866: aload_2
    //   867: iload_3
    //   868: iconst_4
    //   869: iadd
    //   870: iload_3
    //   871: bipush 6
    //   873: iadd
    //   874: invokestatic 20	com/mysql/jdbc/StringUtils:getInt	([BII)I
    //   877: istore 16
    //   879: aload_2
    //   880: iload_3
    //   881: bipush 6
    //   883: iadd
    //   884: iload_3
    //   885: bipush 8
    //   887: iadd
    //   888: invokestatic 20	com/mysql/jdbc/StringUtils:getInt	([BII)I
    //   891: istore 17
    //   893: aload_2
    //   894: iload_3
    //   895: bipush 8
    //   897: iadd
    //   898: iload_3
    //   899: bipush 10
    //   901: iadd
    //   902: invokestatic 20	com/mysql/jdbc/StringUtils:getInt	([BII)I
    //   905: istore 18
    //   907: aload_2
    //   908: iload_3
    //   909: bipush 10
    //   911: iadd
    //   912: iload_3
    //   913: bipush 12
    //   915: iadd
    //   916: invokestatic 20	com/mysql/jdbc/StringUtils:getInt	([BII)I
    //   919: istore 19
    //   921: goto +543 -> 1464
    //   924: iconst_0
    //   925: istore 21
    //   927: iconst_0
    //   928: istore 22
    //   930: iload 22
    //   932: iload 4
    //   934: if_icmpge +26 -> 960
    //   937: aload_2
    //   938: iload_3
    //   939: iload 22
    //   941: iadd
    //   942: baload
    //   943: bipush 45
    //   945: if_icmpne +9 -> 954
    //   948: iconst_1
    //   949: istore 21
    //   951: goto +9 -> 960
    //   954: iinc 22 1
    //   957: goto -27 -> 930
    //   960: aload_0
    //   961: getfield 18	com/mysql/jdbc/ResultSetRow:metadata	[Lcom/mysql/jdbc/Field;
    //   964: iload_1
    //   965: aaload
    //   966: invokevirtual 19	com/mysql/jdbc/Field:getMysqlType	()I
    //   969: bipush 10
    //   971: if_icmpeq +8 -> 979
    //   974: iload 21
    //   976: ifeq +51 -> 1027
    //   979: aload_2
    //   980: iload_3
    //   981: iconst_0
    //   982: iadd
    //   983: iload_3
    //   984: iconst_4
    //   985: iadd
    //   986: invokestatic 20	com/mysql/jdbc/StringUtils:getInt	([BII)I
    //   989: istore 14
    //   991: aload_2
    //   992: iload_3
    //   993: iconst_5
    //   994: iadd
    //   995: iload_3
    //   996: bipush 7
    //   998: iadd
    //   999: invokestatic 20	com/mysql/jdbc/StringUtils:getInt	([BII)I
    //   1002: istore 15
    //   1004: aload_2
    //   1005: iload_3
    //   1006: bipush 8
    //   1008: iadd
    //   1009: iload_3
    //   1010: bipush 10
    //   1012: iadd
    //   1013: invokestatic 20	com/mysql/jdbc/StringUtils:getInt	([BII)I
    //   1016: istore 16
    //   1018: iconst_0
    //   1019: istore 17
    //   1021: iconst_0
    //   1022: istore 18
    //   1024: goto +440 -> 1464
    //   1027: aload_2
    //   1028: iload_3
    //   1029: iconst_0
    //   1030: iadd
    //   1031: iload_3
    //   1032: iconst_2
    //   1033: iadd
    //   1034: invokestatic 20	com/mysql/jdbc/StringUtils:getInt	([BII)I
    //   1037: istore 14
    //   1039: iload 14
    //   1041: bipush 69
    //   1043: if_icmpgt +10 -> 1053
    //   1046: iload 14
    //   1048: bipush 100
    //   1050: iadd
    //   1051: istore 14
    //   1053: aload_2
    //   1054: iload_3
    //   1055: iconst_2
    //   1056: iadd
    //   1057: iload_3
    //   1058: iconst_4
    //   1059: iadd
    //   1060: invokestatic 20	com/mysql/jdbc/StringUtils:getInt	([BII)I
    //   1063: istore 15
    //   1065: aload_2
    //   1066: iload_3
    //   1067: iconst_4
    //   1068: iadd
    //   1069: iload_3
    //   1070: bipush 6
    //   1072: iadd
    //   1073: invokestatic 20	com/mysql/jdbc/StringUtils:getInt	([BII)I
    //   1076: istore 16
    //   1078: aload_2
    //   1079: iload_3
    //   1080: bipush 6
    //   1082: iadd
    //   1083: iload_3
    //   1084: bipush 8
    //   1086: iadd
    //   1087: invokestatic 20	com/mysql/jdbc/StringUtils:getInt	([BII)I
    //   1090: istore 17
    //   1092: aload_2
    //   1093: iload_3
    //   1094: bipush 8
    //   1096: iadd
    //   1097: iload_3
    //   1098: bipush 10
    //   1100: iadd
    //   1101: invokestatic 20	com/mysql/jdbc/StringUtils:getInt	([BII)I
    //   1104: istore 18
    //   1106: iload 14
    //   1108: sipush 1900
    //   1111: iadd
    //   1112: istore 14
    //   1114: goto +350 -> 1464
    //   1117: iconst_0
    //   1118: istore 21
    //   1120: iconst_0
    //   1121: istore 22
    //   1123: iload 22
    //   1125: iload 4
    //   1127: if_icmpge +26 -> 1153
    //   1130: aload_2
    //   1131: iload_3
    //   1132: iload 22
    //   1134: iadd
    //   1135: baload
    //   1136: bipush 58
    //   1138: if_icmpne +9 -> 1147
    //   1141: iconst_1
    //   1142: istore 21
    //   1144: goto +9 -> 1153
    //   1147: iinc 22 1
    //   1150: goto -27 -> 1123
    //   1153: iload 21
    //   1155: ifeq +55 -> 1210
    //   1158: aload_2
    //   1159: iload_3
    //   1160: iconst_0
    //   1161: iadd
    //   1162: iload_3
    //   1163: iconst_2
    //   1164: iadd
    //   1165: invokestatic 20	com/mysql/jdbc/StringUtils:getInt	([BII)I
    //   1168: istore 17
    //   1170: aload_2
    //   1171: iload_3
    //   1172: iconst_3
    //   1173: iadd
    //   1174: iload_3
    //   1175: iconst_5
    //   1176: iadd
    //   1177: invokestatic 20	com/mysql/jdbc/StringUtils:getInt	([BII)I
    //   1180: istore 18
    //   1182: aload_2
    //   1183: iload_3
    //   1184: bipush 6
    //   1186: iadd
    //   1187: iload_3
    //   1188: bipush 8
    //   1190: iadd
    //   1191: invokestatic 20	com/mysql/jdbc/StringUtils:getInt	([BII)I
    //   1194: istore 19
    //   1196: sipush 1970
    //   1199: istore 14
    //   1201: iconst_1
    //   1202: istore 15
    //   1204: iconst_1
    //   1205: istore 16
    //   1207: goto +257 -> 1464
    //   1210: aload_2
    //   1211: iload_3
    //   1212: iconst_0
    //   1213: iadd
    //   1214: iload_3
    //   1215: iconst_4
    //   1216: iadd
    //   1217: invokestatic 20	com/mysql/jdbc/StringUtils:getInt	([BII)I
    //   1220: istore 14
    //   1222: aload_2
    //   1223: iload_3
    //   1224: iconst_4
    //   1225: iadd
    //   1226: iload_3
    //   1227: bipush 6
    //   1229: iadd
    //   1230: invokestatic 20	com/mysql/jdbc/StringUtils:getInt	([BII)I
    //   1233: istore 15
    //   1235: aload_2
    //   1236: iload_3
    //   1237: bipush 6
    //   1239: iadd
    //   1240: iload_3
    //   1241: bipush 8
    //   1243: iadd
    //   1244: invokestatic 20	com/mysql/jdbc/StringUtils:getInt	([BII)I
    //   1247: istore 16
    //   1249: iload 14
    //   1251: sipush 1900
    //   1254: isub
    //   1255: istore 14
    //   1257: iinc 15 -1
    //   1260: goto +204 -> 1464
    //   1263: aload_2
    //   1264: iload_3
    //   1265: iconst_0
    //   1266: iadd
    //   1267: iload_3
    //   1268: iconst_2
    //   1269: iadd
    //   1270: invokestatic 20	com/mysql/jdbc/StringUtils:getInt	([BII)I
    //   1273: istore 14
    //   1275: iload 14
    //   1277: bipush 69
    //   1279: if_icmpgt +10 -> 1289
    //   1282: iload 14
    //   1284: bipush 100
    //   1286: iadd
    //   1287: istore 14
    //   1289: iload 14
    //   1291: sipush 1900
    //   1294: iadd
    //   1295: istore 14
    //   1297: aload_2
    //   1298: iload_3
    //   1299: iconst_2
    //   1300: iadd
    //   1301: iload_3
    //   1302: iconst_4
    //   1303: iadd
    //   1304: invokestatic 20	com/mysql/jdbc/StringUtils:getInt	([BII)I
    //   1307: istore 15
    //   1309: aload_2
    //   1310: iload_3
    //   1311: iconst_4
    //   1312: iadd
    //   1313: iload_3
    //   1314: bipush 6
    //   1316: iadd
    //   1317: invokestatic 20	com/mysql/jdbc/StringUtils:getInt	([BII)I
    //   1320: istore 16
    //   1322: goto +142 -> 1464
    //   1325: aload_2
    //   1326: iload_3
    //   1327: iconst_0
    //   1328: iadd
    //   1329: iload_3
    //   1330: iconst_2
    //   1331: iadd
    //   1332: invokestatic 20	com/mysql/jdbc/StringUtils:getInt	([BII)I
    //   1335: istore 14
    //   1337: iload 14
    //   1339: bipush 69
    //   1341: if_icmpgt +10 -> 1351
    //   1344: iload 14
    //   1346: bipush 100
    //   1348: iadd
    //   1349: istore 14
    //   1351: aload_2
    //   1352: iload_3
    //   1353: iconst_2
    //   1354: iadd
    //   1355: iload_3
    //   1356: iconst_4
    //   1357: iadd
    //   1358: invokestatic 20	com/mysql/jdbc/StringUtils:getInt	([BII)I
    //   1361: istore 15
    //   1363: iconst_1
    //   1364: istore 16
    //   1366: goto +98 -> 1464
    //   1369: aload_2
    //   1370: iload_3
    //   1371: iconst_0
    //   1372: iadd
    //   1373: iload_3
    //   1374: iconst_2
    //   1375: iadd
    //   1376: invokestatic 20	com/mysql/jdbc/StringUtils:getInt	([BII)I
    //   1379: istore 14
    //   1381: iload 14
    //   1383: bipush 69
    //   1385: if_icmpgt +10 -> 1395
    //   1388: iload 14
    //   1390: bipush 100
    //   1392: iadd
    //   1393: istore 14
    //   1395: iload 14
    //   1397: sipush 1900
    //   1400: iadd
    //   1401: istore 14
    //   1403: iconst_1
    //   1404: istore 15
    //   1406: iconst_1
    //   1407: istore 16
    //   1409: goto +55 -> 1464
    //   1412: new 32	java/sql/SQLException
    //   1415: dup
    //   1416: new 7	java/lang/StringBuffer
    //   1419: dup
    //   1420: invokespecial 8	java/lang/StringBuffer:<init>	()V
    //   1423: ldc 86
    //   1425: invokevirtual 10	java/lang/StringBuffer:append	(Ljava/lang/String;)Ljava/lang/StringBuffer;
    //   1428: new 11	java/lang/String
    //   1431: dup
    //   1432: aload_2
    //   1433: invokespecial 12	java/lang/String:<init>	([B)V
    //   1436: invokevirtual 10	java/lang/StringBuffer:append	(Ljava/lang/String;)Ljava/lang/StringBuffer;
    //   1439: ldc 87
    //   1441: invokevirtual 10	java/lang/StringBuffer:append	(Ljava/lang/String;)Ljava/lang/StringBuffer;
    //   1444: iload_1
    //   1445: iconst_1
    //   1446: iadd
    //   1447: invokevirtual 69	java/lang/StringBuffer:append	(I)Ljava/lang/StringBuffer;
    //   1450: ldc 88
    //   1452: invokevirtual 10	java/lang/StringBuffer:append	(Ljava/lang/String;)Ljava/lang/StringBuffer;
    //   1455: invokevirtual 14	java/lang/StringBuffer:toString	()Ljava/lang/String;
    //   1458: ldc 15
    //   1460: invokespecial 46	java/sql/SQLException:<init>	(Ljava/lang/String;Ljava/lang/String;)V
    //   1463: athrow
    //   1464: aload 9
    //   1466: getfield 36	com/mysql/jdbc/ResultSetImpl:useLegacyDatetimeCode	Z
    //   1469: ifne +26 -> 1495
    //   1472: aload 6
    //   1474: iload 14
    //   1476: iload 15
    //   1478: iload 16
    //   1480: iload 17
    //   1482: iload 18
    //   1484: iload 19
    //   1486: iload 20
    //   1488: invokestatic 49	com/mysql/jdbc/TimeUtil:fastTimestampCreate	(Ljava/util/TimeZone;IIIIIII)Ljava/sql/Timestamp;
    //   1491: aload 11
    //   1493: monitorexit
    //   1494: areturn
    //   1495: aload 8
    //   1497: aload 10
    //   1499: aload 5
    //   1501: aload 9
    //   1503: aload 10
    //   1505: iload 14
    //   1507: iload 15
    //   1509: iload 16
    //   1511: iload 17
    //   1513: iload 18
    //   1515: iload 19
    //   1517: iload 20
    //   1519: invokevirtual 50	com/mysql/jdbc/ResultSetImpl:fastTimestampCreate	(Ljava/util/Calendar;IIIIIII)Ljava/sql/Timestamp;
    //   1522: aload 8
    //   1524: invokevirtual 43	com/mysql/jdbc/ConnectionImpl:getServerTimezoneTZ	()Ljava/util/TimeZone;
    //   1527: aload 6
    //   1529: iload 7
    //   1531: invokestatic 51	com/mysql/jdbc/TimeUtil:changeTimezone	(Lcom/mysql/jdbc/ConnectionImpl;Ljava/util/Calendar;Ljava/util/Calendar;Ljava/sql/Timestamp;Ljava/util/TimeZone;Ljava/util/TimeZone;Z)Ljava/sql/Timestamp;
    //   1534: aload 11
    //   1536: monitorexit
    //   1537: areturn
    //   1538: astore 24
    //   1540: aload 11
    //   1542: monitorexit
    //   1543: aload 24
    //   1545: athrow
    //   1546: astore 10
    //   1548: new 7	java/lang/StringBuffer
    //   1551: dup
    //   1552: invokespecial 8	java/lang/StringBuffer:<init>	()V
    //   1555: ldc 89
    //   1557: invokevirtual 10	java/lang/StringBuffer:append	(Ljava/lang/String;)Ljava/lang/StringBuffer;
    //   1560: aload_0
    //   1561: iload_1
    //   1562: ldc 26
    //   1564: aload 8
    //   1566: invokevirtual 90	com/mysql/jdbc/ResultSetRow:getString	(ILjava/lang/String;Lcom/mysql/jdbc/ConnectionImpl;)Ljava/lang/String;
    //   1569: invokevirtual 10	java/lang/StringBuffer:append	(Ljava/lang/String;)Ljava/lang/StringBuffer;
    //   1572: ldc 91
    //   1574: invokevirtual 10	java/lang/StringBuffer:append	(Ljava/lang/String;)Ljava/lang/StringBuffer;
    //   1577: iload_1
    //   1578: iconst_1
    //   1579: iadd
    //   1580: invokevirtual 69	java/lang/StringBuffer:append	(I)Ljava/lang/StringBuffer;
    //   1583: ldc 92
    //   1585: invokevirtual 10	java/lang/StringBuffer:append	(Ljava/lang/String;)Ljava/lang/StringBuffer;
    //   1588: invokevirtual 14	java/lang/StringBuffer:toString	()Ljava/lang/String;
    //   1591: ldc 15
    //   1593: aload_0
    //   1594: getfield 2	com/mysql/jdbc/ResultSetRow:exceptionInterceptor	Lcom/mysql/jdbc/ExceptionInterceptor;
    //   1597: invokestatic 16	com/mysql/jdbc/SQLError:createSQLException	(Ljava/lang/String;Ljava/lang/String;Lcom/mysql/jdbc/ExceptionInterceptor;)Ljava/sql/SQLException;
    //   1600: astore 11
    //   1602: aload 11
    //   1604: aload 10
    //   1606: invokevirtual 34	java/sql/SQLException:initCause	(Ljava/lang/Throwable;)Ljava/lang/Throwable;
    //   1609: pop
    //   1610: aload 11
    //   1612: athrow
    // Line number table:
    //   Java source line #995	-> byte code offset #0
    //   Java source line #999	-> byte code offset #23
    //   Java source line #1000	-> byte code offset #29
    //   Java source line #1002	-> byte code offset #32
    //   Java source line #1004	-> byte code offset #35
    //   Java source line #1005	-> byte code offset #45
    //   Java source line #1006	-> byte code offset #56
    //   Java source line #1007	-> byte code offset #59
    //   Java source line #1004	-> byte code offset #62
    //   Java source line #1011	-> byte code offset #68
    //   Java source line #1012	-> byte code offset #78
    //   Java source line #1014	-> byte code offset #86
    //   Java source line #1015	-> byte code offset #107
    //   Java source line #1018	-> byte code offset #110
    //   Java source line #1020	-> byte code offset #152
    //   Java source line #1022	-> byte code offset #155
    //   Java source line #1011	-> byte code offset #158
    //   Java source line #1026	-> byte code offset #164
    //   Java source line #1028	-> byte code offset #174
    //   Java source line #1031	-> byte code offset #187
    //   Java source line #1032	-> byte code offset #192
    //   Java source line #1034	-> byte code offset #205
    //   Java source line #1042	-> byte code offset #239
    //   Java source line #1043	-> byte code offset #247
    //   Java source line #1047	-> byte code offset #263
    //   Java source line #1049	-> byte code offset #280
    //   Java source line #1051	-> byte code offset #294
    //   Java source line #1052	-> byte code offset #302
    //   Java source line #1057	-> byte code offset #323
    //   Java source line #1064	-> byte code offset #364
    //   Java source line #1065	-> byte code offset #377
    //   Java source line #1070	-> byte code offset #380
    //   Java source line #1071	-> byte code offset #383
    //   Java source line #1072	-> byte code offset #386
    //   Java source line #1073	-> byte code offset #389
    //   Java source line #1074	-> byte code offset #392
    //   Java source line #1075	-> byte code offset #395
    //   Java source line #1076	-> byte code offset #398
    //   Java source line #1078	-> byte code offset #401
    //   Java source line #1088	-> byte code offset #528
    //   Java source line #1090	-> byte code offset #540
    //   Java source line #1092	-> byte code offset #553
    //   Java source line #1094	-> byte code offset #567
    //   Java source line #1096	-> byte code offset #581
    //   Java source line #1098	-> byte code offset #595
    //   Java source line #1101	-> byte code offset #609
    //   Java source line #1103	-> byte code offset #612
    //   Java source line #1104	-> byte code offset #619
    //   Java source line #1106	-> byte code offset #622
    //   Java source line #1107	-> byte code offset #632
    //   Java source line #1108	-> byte code offset #643
    //   Java source line #1106	-> byte code offset #647
    //   Java source line #1112	-> byte code offset #653
    //   Java source line #1113	-> byte code offset #659
    //   Java source line #1114	-> byte code offset #668
    //   Java source line #1118	-> byte code offset #682
    //   Java source line #1120	-> byte code offset #693
    //   Java source line #1121	-> byte code offset #700
    //   Java source line #1122	-> byte code offset #715
    //   Java source line #1125	-> byte code offset #725
    //   Java source line #1139	-> byte code offset #736
    //   Java source line #1141	-> byte code offset #748
    //   Java source line #1143	-> byte code offset #761
    //   Java source line #1145	-> byte code offset #775
    //   Java source line #1147	-> byte code offset #789
    //   Java source line #1149	-> byte code offset #803
    //   Java source line #1152	-> byte code offset #817
    //   Java source line #1156	-> byte code offset #820
    //   Java source line #1159	-> byte code offset #832
    //   Java source line #1160	-> byte code offset #839
    //   Java source line #1163	-> byte code offset #846
    //   Java source line #1165	-> byte code offset #854
    //   Java source line #1167	-> byte code offset #866
    //   Java source line #1169	-> byte code offset #879
    //   Java source line #1171	-> byte code offset #893
    //   Java source line #1173	-> byte code offset #907
    //   Java source line #1176	-> byte code offset #921
    //   Java source line #1180	-> byte code offset #924
    //   Java source line #1182	-> byte code offset #927
    //   Java source line #1183	-> byte code offset #937
    //   Java source line #1184	-> byte code offset #948
    //   Java source line #1185	-> byte code offset #951
    //   Java source line #1182	-> byte code offset #954
    //   Java source line #1189	-> byte code offset #960
    //   Java source line #1191	-> byte code offset #979
    //   Java source line #1193	-> byte code offset #991
    //   Java source line #1195	-> byte code offset #1004
    //   Java source line #1197	-> byte code offset #1018
    //   Java source line #1198	-> byte code offset #1021
    //   Java source line #1200	-> byte code offset #1027
    //   Java source line #1203	-> byte code offset #1039
    //   Java source line #1204	-> byte code offset #1046
    //   Java source line #1207	-> byte code offset #1053
    //   Java source line #1209	-> byte code offset #1065
    //   Java source line #1211	-> byte code offset #1078
    //   Java source line #1213	-> byte code offset #1092
    //   Java source line #1216	-> byte code offset #1106
    //   Java source line #1219	-> byte code offset #1114
    //   Java source line #1223	-> byte code offset #1117
    //   Java source line #1225	-> byte code offset #1120
    //   Java source line #1226	-> byte code offset #1130
    //   Java source line #1227	-> byte code offset #1141
    //   Java source line #1228	-> byte code offset #1144
    //   Java source line #1225	-> byte code offset #1147
    //   Java source line #1232	-> byte code offset #1153
    //   Java source line #1233	-> byte code offset #1158
    //   Java source line #1235	-> byte code offset #1170
    //   Java source line #1237	-> byte code offset #1182
    //   Java source line #1240	-> byte code offset #1196
    //   Java source line #1241	-> byte code offset #1201
    //   Java source line #1242	-> byte code offset #1204
    //   Java source line #1244	-> byte code offset #1207
    //   Java source line #1247	-> byte code offset #1210
    //   Java source line #1249	-> byte code offset #1222
    //   Java source line #1251	-> byte code offset #1235
    //   Java source line #1254	-> byte code offset #1249
    //   Java source line #1255	-> byte code offset #1257
    //   Java source line #1257	-> byte code offset #1260
    //   Java source line #1261	-> byte code offset #1263
    //   Java source line #1264	-> byte code offset #1275
    //   Java source line #1265	-> byte code offset #1282
    //   Java source line #1268	-> byte code offset #1289
    //   Java source line #1270	-> byte code offset #1297
    //   Java source line #1272	-> byte code offset #1309
    //   Java source line #1275	-> byte code offset #1322
    //   Java source line #1279	-> byte code offset #1325
    //   Java source line #1282	-> byte code offset #1337
    //   Java source line #1283	-> byte code offset #1344
    //   Java source line #1286	-> byte code offset #1351
    //   Java source line #1289	-> byte code offset #1363
    //   Java source line #1291	-> byte code offset #1366
    //   Java source line #1295	-> byte code offset #1369
    //   Java source line #1298	-> byte code offset #1381
    //   Java source line #1299	-> byte code offset #1388
    //   Java source line #1302	-> byte code offset #1395
    //   Java source line #1303	-> byte code offset #1403
    //   Java source line #1304	-> byte code offset #1406
    //   Java source line #1306	-> byte code offset #1409
    //   Java source line #1310	-> byte code offset #1412
    //   Java source line #1318	-> byte code offset #1464
    //   Java source line #1319	-> byte code offset #1472
    //   Java source line #1325	-> byte code offset #1495
    //   Java source line #1334	-> byte code offset #1538
    //   Java source line #1335	-> byte code offset #1546
    //   Java source line #1336	-> byte code offset #1548
    //   Java source line #1340	-> byte code offset #1602
    //   Java source line #1342	-> byte code offset #1610
    // Local variable table:
    //   start	length	slot	name	signature
    //   0	1613	0	this	ResultSetRow
    //   0	1613	1	columnIndex	int
    //   0	1613	2	timestampAsBytes	byte[]
    //   0	1613	3	offset	int
    //   0	1613	4	length	int
    //   0	1613	5	targetCalendar	Calendar
    //   0	1613	6	tz	TimeZone
    //   0	1613	7	rollForward	boolean
    //   0	1613	8	conn	ConnectionImpl
    //   0	1613	9	rs	ResultSetImpl
    //   21	1483	10	sessionCalendar	Calendar
    //   1546	59	10	e	Exception
    //   1600	11	11	sqlEx	SQLException
    //   30	140	12	allZeroTimestamp	boolean
    //   33	132	13	onlyTimePresent	boolean
    //   36	27	14	i	int
    //   69	90	14	i	int
    //   381	1125	14	year	int
    //   84	62	15	b	byte
    //   384	1124	15	month	int
    //   387	1123	16	day	int
    //   390	1122	17	hour	int
    //   393	1121	18	minutes	int
    //   396	1120	19	seconds	int
    //   399	1119	20	nanos	int
    //   620	67	21	decimalIndex	int
    //   925	50	21	hasDash	boolean
    //   1118	36	21	hasColon	boolean
    //   623	25	22	i	int
    //   691	15	22	numDigits	int
    //   928	27	22	i	int
    //   1121	27	22	i	int
    //   713	5	23	factor	int
    //   1538	6	24	localObject1	Object
    // Exception table:
    //   from	to	target	type
    //   29	191	1538	finally
    //   192	262	1538	finally
    //   263	279	1538	finally
    //   280	322	1538	finally
    //   323	363	1538	finally
    //   364	1494	1538	finally
    //   1495	1537	1538	finally
    //   1538	1543	1538	finally
    //   0	191	1546	java/lang/Exception
    //   192	262	1546	java/lang/Exception
    //   263	279	1546	java/lang/Exception
    //   280	322	1546	java/lang/Exception
    //   323	363	1546	java/lang/Exception
    //   364	1494	1546	java/lang/Exception
    //   1495	1537	1546	java/lang/Exception
    //   1538	1546	1546	java/lang/Exception
  }
  
  public abstract Timestamp getTimestampFast(int paramInt, Calendar paramCalendar, TimeZone paramTimeZone, boolean paramBoolean, ConnectionImpl paramConnectionImpl, ResultSetImpl paramResultSetImpl)
    throws SQLException;
  
  public abstract boolean isFloatingPointNumber(int paramInt)
    throws SQLException;
  
  public abstract boolean isNull(int paramInt)
    throws SQLException;
  
  public abstract long length(int paramInt)
    throws SQLException;
  
  public abstract void setColumnValue(int paramInt, byte[] paramArrayOfByte)
    throws SQLException;
  
  public ResultSetRow setMetadata(Field[] f)
    throws SQLException
  {
    this.metadata = f;
    
    return this;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\mysql\jdbc\ResultSetRow.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */