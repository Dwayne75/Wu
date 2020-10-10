package com.mysql.jdbc;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public abstract interface BalanceStrategy
  extends Extension
{
  public abstract Connection pickConnection(LoadBalancingConnectionProxy paramLoadBalancingConnectionProxy, List paramList, Map paramMap, long[] paramArrayOfLong, int paramInt)
    throws SQLException;
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\mysql\jdbc\BalanceStrategy.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */