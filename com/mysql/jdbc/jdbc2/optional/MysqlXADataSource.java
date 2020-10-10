package com.mysql.jdbc.jdbc2.optional;

import com.mysql.jdbc.ConnectionImpl;
import java.sql.SQLException;
import javax.sql.XAConnection;
import javax.sql.XADataSource;

public class MysqlXADataSource
  extends MysqlDataSource
  implements XADataSource
{
  public XAConnection getXAConnection()
    throws SQLException
  {
    java.sql.Connection conn = getConnection();
    
    return wrapConnection(conn);
  }
  
  public XAConnection getXAConnection(String u, String p)
    throws SQLException
  {
    java.sql.Connection conn = getConnection(u, p);
    
    return wrapConnection(conn);
  }
  
  private XAConnection wrapConnection(java.sql.Connection conn)
    throws SQLException
  {
    if ((getPinGlobalTxToPhysicalConnection()) || (((com.mysql.jdbc.Connection)conn).getPinGlobalTxToPhysicalConnection())) {
      return SuspendableXAConnection.getInstance((ConnectionImpl)conn);
    }
    return MysqlXAConnection.getInstance((ConnectionImpl)conn, getLogXaCommands());
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\mysql\jdbc\jdbc2\optional\MysqlXADataSource.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */