package com.mysql.jdbc.jdbc2.optional;

import com.mysql.jdbc.ConnectionImpl;
import com.mysql.jdbc.Constants;
import com.mysql.jdbc.Util;
import com.mysql.jdbc.log.Log;
import java.lang.reflect.Constructor;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.sql.XAConnection;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

public class MysqlXAConnection
  extends MysqlPooledConnection
  implements XAConnection, XAResource
{
  private ConnectionImpl underlyingConnection;
  private static final Map MYSQL_ERROR_CODES_TO_XA_ERROR_CODES;
  private Log log;
  protected boolean logXaCommands;
  private static final Constructor JDBC_4_XA_CONNECTION_WRAPPER_CTOR;
  
  static
  {
    HashMap temp = new HashMap();
    
    temp.put(Constants.integerValueOf(1397), Constants.integerValueOf(-4));
    temp.put(Constants.integerValueOf(1398), Constants.integerValueOf(-5));
    temp.put(Constants.integerValueOf(1399), Constants.integerValueOf(-7));
    temp.put(Constants.integerValueOf(1400), Constants.integerValueOf(-9));
    temp.put(Constants.integerValueOf(1401), Constants.integerValueOf(-3));
    temp.put(Constants.integerValueOf(1402), Constants.integerValueOf(100));
    
    MYSQL_ERROR_CODES_TO_XA_ERROR_CODES = Collections.unmodifiableMap(temp);
    if (Util.isJdbc4()) {
      try
      {
        JDBC_4_XA_CONNECTION_WRAPPER_CTOR = Class.forName("com.mysql.jdbc.jdbc2.optional.JDBC4MysqlXAConnection").getConstructor(new Class[] { ConnectionImpl.class, Boolean.TYPE });
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
      JDBC_4_XA_CONNECTION_WRAPPER_CTOR = null;
    }
  }
  
  protected static MysqlXAConnection getInstance(ConnectionImpl mysqlConnection, boolean logXaCommands)
    throws SQLException
  {
    if (!Util.isJdbc4()) {
      return new MysqlXAConnection(mysqlConnection, logXaCommands);
    }
    return (MysqlXAConnection)Util.handleNewInstance(JDBC_4_XA_CONNECTION_WRAPPER_CTOR, new Object[] { mysqlConnection, Boolean.valueOf(logXaCommands) }, mysqlConnection.getExceptionInterceptor());
  }
  
  public MysqlXAConnection(ConnectionImpl connection, boolean logXaCommands)
    throws SQLException
  {
    super(connection);
    this.underlyingConnection = connection;
    this.log = connection.getLog();
    this.logXaCommands = logXaCommands;
  }
  
  public XAResource getXAResource()
    throws SQLException
  {
    return this;
  }
  
  public int getTransactionTimeout()
    throws XAException
  {
    return 0;
  }
  
  public boolean setTransactionTimeout(int arg0)
    throws XAException
  {
    return false;
  }
  
  public boolean isSameRM(XAResource xares)
    throws XAException
  {
    if ((xares instanceof MysqlXAConnection)) {
      return this.underlyingConnection.isSameResource(((MysqlXAConnection)xares).underlyingConnection);
    }
    return false;
  }
  
  public Xid[] recover(int flag)
    throws XAException
  {
    return recover(this.underlyingConnection, flag);
  }
  
  protected static Xid[] recover(Connection c, int flag)
    throws XAException
  {
    boolean startRscan = (flag & 0x1000000) > 0;
    boolean endRscan = (flag & 0x800000) > 0;
    if ((!startRscan) && (!endRscan) && (flag != 0)) {
      throw new MysqlXAException(-5, "Invalid flag, must use TMNOFLAGS, or any combination of TMSTARTRSCAN and TMENDRSCAN", null);
    }
    if (!startRscan) {
      return new Xid[0];
    }
    ResultSet rs = null;
    Statement stmt = null;
    
    List recoveredXidList = new ArrayList();
    try
    {
      stmt = c.createStatement();
      
      rs = stmt.executeQuery("XA RECOVER");
      while (rs.next())
      {
        int formatId = rs.getInt(1);
        int gtridLength = rs.getInt(2);
        int bqualLength = rs.getInt(3);
        byte[] gtridAndBqual = rs.getBytes(4);
        
        byte[] gtrid = new byte[gtridLength];
        byte[] bqual = new byte[bqualLength];
        if (gtridAndBqual.length != gtridLength + bqualLength) {
          throw new MysqlXAException(105, "Error while recovering XIDs from RM. GTRID and BQUAL are wrong sizes", null);
        }
        System.arraycopy(gtridAndBqual, 0, gtrid, 0, gtridLength);
        
        System.arraycopy(gtridAndBqual, gtridLength, bqual, 0, bqualLength);
        
        recoveredXidList.add(new MysqlXid(gtrid, bqual, formatId));
      }
    }
    catch (SQLException sqlEx)
    {
      throw mapXAExceptionFromSQLException(sqlEx);
    }
    finally
    {
      if (rs != null) {
        try
        {
          rs.close();
        }
        catch (SQLException sqlEx)
        {
          throw mapXAExceptionFromSQLException(sqlEx);
        }
      }
      if (stmt != null) {
        try
        {
          stmt.close();
        }
        catch (SQLException sqlEx)
        {
          throw mapXAExceptionFromSQLException(sqlEx);
        }
      }
    }
    int numXids = recoveredXidList.size();
    
    Xid[] asXids = new Xid[numXids];
    Object[] asObjects = recoveredXidList.toArray();
    for (int i = 0; i < numXids; i++) {
      asXids[i] = ((Xid)asObjects[i]);
    }
    return asXids;
  }
  
  public int prepare(Xid xid)
    throws XAException
  {
    StringBuffer commandBuf = new StringBuffer();
    commandBuf.append("XA PREPARE ");
    commandBuf.append(xidToString(xid));
    
    dispatchCommand(commandBuf.toString());
    
    return 0;
  }
  
  public void rollback(Xid xid)
    throws XAException
  {
    StringBuffer commandBuf = new StringBuffer();
    commandBuf.append("XA ROLLBACK ");
    commandBuf.append(xidToString(xid));
    try
    {
      dispatchCommand(commandBuf.toString());
    }
    finally
    {
      this.underlyingConnection.setInGlobalTx(false);
    }
  }
  
  public void end(Xid xid, int flags)
    throws XAException
  {
    StringBuffer commandBuf = new StringBuffer();
    commandBuf.append("XA END ");
    commandBuf.append(xidToString(xid));
    switch (flags)
    {
    case 67108864: 
      break;
    case 33554432: 
      commandBuf.append(" SUSPEND");
      break;
    case 536870912: 
      break;
    default: 
      throw new XAException(-5);
    }
    dispatchCommand(commandBuf.toString());
  }
  
  public void start(Xid xid, int flags)
    throws XAException
  {
    StringBuffer commandBuf = new StringBuffer();
    commandBuf.append("XA START ");
    commandBuf.append(xidToString(xid));
    switch (flags)
    {
    case 2097152: 
      commandBuf.append(" JOIN");
      break;
    case 134217728: 
      commandBuf.append(" RESUME");
      break;
    case 0: 
      break;
    default: 
      throw new XAException(-5);
    }
    dispatchCommand(commandBuf.toString());
    
    this.underlyingConnection.setInGlobalTx(true);
  }
  
  public void commit(Xid xid, boolean onePhase)
    throws XAException
  {
    StringBuffer commandBuf = new StringBuffer();
    commandBuf.append("XA COMMIT ");
    commandBuf.append(xidToString(xid));
    if (onePhase) {
      commandBuf.append(" ONE PHASE");
    }
    try
    {
      dispatchCommand(commandBuf.toString());
    }
    finally
    {
      this.underlyingConnection.setInGlobalTx(false);
    }
  }
  
  private ResultSet dispatchCommand(String command)
    throws XAException
  {
    Statement stmt = null;
    try
    {
      if (this.logXaCommands) {
        this.log.logDebug("Executing XA statement: " + command);
      }
      stmt = this.underlyingConnection.createStatement();
      
      stmt.execute(command);
      
      ResultSet rs = stmt.getResultSet();
      
      return rs;
    }
    catch (SQLException sqlEx)
    {
      throw mapXAExceptionFromSQLException(sqlEx);
    }
    finally
    {
      if (stmt != null) {
        try
        {
          stmt.close();
        }
        catch (SQLException sqlEx) {}
      }
    }
  }
  
  protected static XAException mapXAExceptionFromSQLException(SQLException sqlEx)
  {
    Integer xaCode = (Integer)MYSQL_ERROR_CODES_TO_XA_ERROR_CODES.get(Constants.integerValueOf(sqlEx.getErrorCode()));
    if (xaCode != null) {
      return new MysqlXAException(xaCode.intValue(), sqlEx.getMessage(), null);
    }
    return new MysqlXAException(sqlEx.getMessage(), null);
  }
  
  private static String xidToString(Xid xid)
  {
    byte[] gtrid = xid.getGlobalTransactionId();
    
    byte[] btrid = xid.getBranchQualifier();
    
    int lengthAsString = 6;
    if (gtrid != null) {
      lengthAsString += 2 * gtrid.length;
    }
    if (btrid != null) {
      lengthAsString += 2 * btrid.length;
    }
    String formatIdInHex = Integer.toHexString(xid.getFormatId());
    
    lengthAsString += formatIdInHex.length();
    lengthAsString += 3;
    
    StringBuffer asString = new StringBuffer(lengthAsString);
    
    asString.append("0x");
    if (gtrid != null) {
      for (int i = 0; i < gtrid.length; i++)
      {
        String asHex = Integer.toHexString(gtrid[i] & 0xFF);
        if (asHex.length() == 1) {
          asString.append("0");
        }
        asString.append(asHex);
      }
    }
    asString.append(",");
    if (btrid != null)
    {
      asString.append("0x");
      for (int i = 0; i < btrid.length; i++)
      {
        String asHex = Integer.toHexString(btrid[i] & 0xFF);
        if (asHex.length() == 1) {
          asString.append("0");
        }
        asString.append(asHex);
      }
    }
    asString.append(",0x");
    asString.append(formatIdInHex);
    
    return asString.toString();
  }
  
  public synchronized Connection getConnection()
    throws SQLException
  {
    Connection connToWrap = getConnection(false, true);
    
    return connToWrap;
  }
  
  public void forget(Xid xid)
    throws XAException
  {}
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\mysql\jdbc\jdbc2\optional\MysqlXAConnection.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */