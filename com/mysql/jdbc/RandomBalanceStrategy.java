package com.mysql.jdbc;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class RandomBalanceStrategy
  implements BalanceStrategy
{
  public void destroy() {}
  
  public void init(Connection conn, Properties props)
    throws SQLException
  {}
  
  public Connection pickConnection(LoadBalancingConnectionProxy proxy, List configuredHosts, Map liveConnections, long[] responseTimes, int numRetries)
    throws SQLException
  {
    int numHosts = configuredHosts.size();
    
    SQLException ex = null;
    
    List whiteList = new ArrayList(numHosts);
    whiteList.addAll(configuredHosts);
    
    Map blackList = proxy.getGlobalBlacklist();
    
    whiteList.removeAll(blackList.keySet());
    
    Map whiteListMap = getArrayIndexMap(whiteList);
    
    int attempts = 0;
    Connection conn;
    for (;;)
    {
      if (attempts >= numRetries) {
        break label286;
      }
      int random = (int)Math.floor(Math.random() * whiteList.size());
      
      String hostPortSpec = (String)whiteList.get(random);
      
      conn = (Connection)liveConnections.get(hostPortSpec);
      if (conn == null) {
        try
        {
          conn = proxy.createConnectionForHost(hostPortSpec);
        }
        catch (SQLException sqlEx)
        {
          ex = sqlEx;
          if (((sqlEx instanceof CommunicationsException)) || ("08S01".equals(sqlEx.getSQLState())))
          {
            Integer whiteListIndex = (Integer)whiteListMap.get(hostPortSpec);
            if (whiteListIndex != null)
            {
              whiteList.remove(whiteListIndex.intValue());
              whiteListMap = getArrayIndexMap(whiteList);
            }
            proxy.addToGlobalBlacklist(hostPortSpec);
            if (whiteList.size() == 0)
            {
              attempts++;
              try
              {
                Thread.sleep(250L);
              }
              catch (InterruptedException e) {}
              whiteListMap = new HashMap(numHosts);
              whiteList.addAll(configuredHosts);
              blackList = proxy.getGlobalBlacklist();
              
              whiteList.removeAll(blackList.keySet());
              whiteListMap = getArrayIndexMap(whiteList);
            }
          }
          else
          {
            throw sqlEx;
          }
        }
      }
    }
    return conn;
    label286:
    if (ex != null) {
      throw ex;
    }
    return null;
  }
  
  private Map getArrayIndexMap(List l)
  {
    Map m = new HashMap(l.size());
    for (int i = 0; i < l.size(); i++) {
      m.put(l.get(i), new Integer(i));
    }
    return m;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\mysql\jdbc\RandomBalanceStrategy.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */