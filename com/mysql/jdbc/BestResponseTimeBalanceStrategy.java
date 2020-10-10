package com.mysql.jdbc;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class BestResponseTimeBalanceStrategy
  implements BalanceStrategy
{
  public void destroy() {}
  
  public void init(Connection conn, Properties props)
    throws SQLException
  {}
  
  public Connection pickConnection(LoadBalancingConnectionProxy proxy, List configuredHosts, Map liveConnections, long[] responseTimes, int numRetries)
    throws SQLException
  {
    Map blackList = proxy.getGlobalBlacklist();
    
    SQLException ex = null;
    
    int attempts = 0;
    Connection conn;
    for (;;)
    {
      if (attempts >= numRetries) {
        break label252;
      }
      long minResponseTime = Long.MAX_VALUE;
      
      int bestHostIndex = 0;
      if (blackList.size() == configuredHosts.size()) {
        blackList = proxy.getGlobalBlacklist();
      }
      for (int i = 0; i < responseTimes.length; i++)
      {
        long candidateResponseTime = responseTimes[i];
        if ((candidateResponseTime < minResponseTime) && (!blackList.containsKey(configuredHosts.get(i))))
        {
          if (candidateResponseTime == 0L)
          {
            bestHostIndex = i;
            
            break;
          }
          bestHostIndex = i;
          minResponseTime = candidateResponseTime;
        }
      }
      String bestHost = (String)configuredHosts.get(bestHostIndex);
      
      conn = (Connection)liveConnections.get(bestHost);
      if (conn == null) {
        try
        {
          conn = proxy.createConnectionForHost(bestHost);
        }
        catch (SQLException sqlEx)
        {
          ex = sqlEx;
          if (((sqlEx instanceof CommunicationsException)) || ("08S01".equals(sqlEx.getSQLState())))
          {
            proxy.addToGlobalBlacklist(bestHost);
            blackList.put(bestHost, null);
            if (blackList.size() == configuredHosts.size())
            {
              attempts++;
              try
              {
                Thread.sleep(250L);
              }
              catch (InterruptedException e) {}
              blackList = proxy.getGlobalBlacklist();
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
    label252:
    if (ex != null) {
      throw ex;
    }
    return null;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\mysql\jdbc\BestResponseTimeBalanceStrategy.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */