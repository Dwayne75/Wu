package com.mysql.jdbc;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class LoadBalancingConnectionProxy
  implements InvocationHandler, PingTarget
{
  private static Method getLocalTimeMethod;
  public static final String BLACKLIST_TIMEOUT_PROPERTY_KEY = "loadBalanceBlacklistTimeout";
  private Connection currentConn;
  private List hostList;
  private Map liveConnections;
  private Map connectionsToHostsMap;
  private long[] responseTimes;
  private Map hostsToListIndexMap;
  
  static
  {
    try
    {
      getLocalTimeMethod = System.class.getMethod("nanoTime", new Class[0]);
    }
    catch (SecurityException e) {}catch (NoSuchMethodException e) {}
  }
  
  protected class ConnectionErrorFiringInvocationHandler
    implements InvocationHandler
  {
    Object invokeOn = null;
    
    public ConnectionErrorFiringInvocationHandler(Object toInvokeOn)
    {
      this.invokeOn = toInvokeOn;
    }
    
    public Object invoke(Object proxy, Method method, Object[] args)
      throws Throwable
    {
      Object result = null;
      try
      {
        result = method.invoke(this.invokeOn, args);
        if (result != null) {
          result = LoadBalancingConnectionProxy.this.proxyIfInterfaceIsJdbc(result, result.getClass());
        }
      }
      catch (InvocationTargetException e)
      {
        LoadBalancingConnectionProxy.this.dealWithInvocationException(e);
      }
      return result;
    }
  }
  
  private boolean inTransaction = false;
  private long transactionStartTime = 0L;
  private Properties localProps;
  private boolean isClosed = false;
  private BalanceStrategy balancer;
  private int retriesAllDown;
  private static Map globalBlacklist = new HashMap();
  private int globalBlacklistTimeout = 0;
  
  LoadBalancingConnectionProxy(List hosts, Properties props)
    throws SQLException
  {
    this.hostList = hosts;
    
    int numHosts = this.hostList.size();
    
    this.liveConnections = new HashMap(numHosts);
    this.connectionsToHostsMap = new HashMap(numHosts);
    this.responseTimes = new long[numHosts];
    this.hostsToListIndexMap = new HashMap(numHosts);
    for (int i = 0; i < numHosts; i++) {
      this.hostsToListIndexMap.put(this.hostList.get(i), new Integer(i));
    }
    this.localProps = ((Properties)props.clone());
    this.localProps.remove("HOST");
    this.localProps.remove("PORT");
    this.localProps.setProperty("useLocalSessionState", "true");
    
    String strategy = this.localProps.getProperty("loadBalanceStrategy", "random");
    
    String retriesAllDownAsString = this.localProps.getProperty("retriesAllDown", "120");
    try
    {
      this.retriesAllDown = Integer.parseInt(retriesAllDownAsString);
    }
    catch (NumberFormatException nfe)
    {
      throw SQLError.createSQLException(Messages.getString("LoadBalancingConnectionProxy.badValueForRetriesAllDown", new Object[] { retriesAllDownAsString }), "S1009", null);
    }
    String blacklistTimeoutAsString = this.localProps.getProperty("loadBalanceBlacklistTimeout", "0");
    try
    {
      this.globalBlacklistTimeout = Integer.parseInt(blacklistTimeoutAsString);
    }
    catch (NumberFormatException nfe)
    {
      throw SQLError.createSQLException(Messages.getString("LoadBalancingConnectionProxy.badValueForLoadBalanceBlacklistTimeout", new Object[] { retriesAllDownAsString }), "S1009", null);
    }
    if ("random".equals(strategy)) {
      this.balancer = ((BalanceStrategy)Util.loadExtensions(null, props, "com.mysql.jdbc.RandomBalanceStrategy", "InvalidLoadBalanceStrategy", null).get(0));
    } else if ("bestResponseTime".equals(strategy)) {
      this.balancer = ((BalanceStrategy)Util.loadExtensions(null, props, "com.mysql.jdbc.BestResponseTimeBalanceStrategy", "InvalidLoadBalanceStrategy", null).get(0));
    } else {
      this.balancer = ((BalanceStrategy)Util.loadExtensions(null, props, strategy, "InvalidLoadBalanceStrategy", null).get(0));
    }
    this.balancer.init(null, props);
    
    pickNewConnection();
  }
  
  public synchronized Connection createConnectionForHost(String hostPortSpec)
    throws SQLException
  {
    Properties connProps = (Properties)this.localProps.clone();
    
    String[] hostPortPair = NonRegisteringDriver.parseHostPortPair(hostPortSpec);
    if (hostPortPair[1] == null) {
      hostPortPair[1] = "3306";
    }
    connProps.setProperty("HOST", hostPortSpec);
    
    connProps.setProperty("PORT", hostPortPair[1]);
    
    Connection conn = ConnectionImpl.getInstance(hostPortSpec, Integer.parseInt(hostPortPair[1]), connProps, connProps.getProperty("DBNAME"), "jdbc:mysql://" + hostPortPair[0] + ":" + hostPortPair[1] + "/");
    
    this.liveConnections.put(hostPortSpec, conn);
    this.connectionsToHostsMap.put(conn, hostPortSpec);
    
    return conn;
  }
  
  void dealWithInvocationException(InvocationTargetException e)
    throws SQLException, Throwable, InvocationTargetException
  {
    Throwable t = e.getTargetException();
    if (t != null)
    {
      if ((t instanceof SQLException))
      {
        String sqlState = ((SQLException)t).getSQLState();
        if ((sqlState != null) && 
          (sqlState.startsWith("08"))) {
          invalidateCurrentConnection();
        }
      }
      throw t;
    }
    throw e;
  }
  
  synchronized void invalidateCurrentConnection()
    throws SQLException
  {
    try
    {
      if (!this.currentConn.isClosed()) {
        this.currentConn.close();
      }
    }
    finally
    {
      if (isGlobalBlacklistEnabled()) {
        addToGlobalBlacklist((String)this.connectionsToHostsMap.get(this.currentConn));
      }
      this.liveConnections.remove(this.connectionsToHostsMap.get(this.currentConn));
      
      int hostIndex = ((Integer)this.hostsToListIndexMap.get(this.connectionsToHostsMap.get(this.currentConn))).intValue();
      synchronized (this.responseTimes)
      {
        this.responseTimes[hostIndex] = 0L;
      }
      this.connectionsToHostsMap.remove(this.currentConn);
    }
  }
  
  public Object invoke(Object proxy, Method method, Object[] args)
    throws Throwable
  {
    String methodName = method.getName();
    if (("equals".equals(methodName)) && (args.length == 1))
    {
      if ((args[0] instanceof Proxy)) {
        return Boolean.valueOf(((Proxy)args[0]).equals(this));
      }
      return Boolean.valueOf(equals(args[0]));
    }
    if ("close".equals(methodName))
    {
      synchronized (this)
      {
        Iterator allConnections = this.liveConnections.values().iterator();
        while (allConnections.hasNext()) {
          ((Connection)allConnections.next()).close();
        }
        if (!this.isClosed) {
          this.balancer.destroy();
        }
        this.liveConnections.clear();
        this.connectionsToHostsMap.clear();
      }
      return null;
    }
    if ("isClosed".equals(methodName)) {
      return Boolean.valueOf(this.isClosed);
    }
    if (this.isClosed) {
      throw SQLError.createSQLException("No operations allowed after connection closed.", "08003", null);
    }
    if (!this.inTransaction)
    {
      this.inTransaction = true;
      this.transactionStartTime = getLocalTimeBestResolution();
    }
    Object result = null;
    try
    {
      result = method.invoke(this.currentConn, args);
      if (result != null)
      {
        if ((result instanceof Statement)) {
          ((Statement)result).setPingTarget(this);
        }
        result = proxyIfInterfaceIsJdbc(result, result.getClass());
      }
    }
    catch (InvocationTargetException e)
    {
      dealWithInvocationException(e);
    }
    finally
    {
      if (("commit".equals(methodName)) || ("rollback".equals(methodName)))
      {
        this.inTransaction = false;
        
        String host = (String)this.connectionsToHostsMap.get(this.currentConn);
        if (host != null)
        {
          int hostIndex = ((Integer)this.hostsToListIndexMap.get(host)).intValue();
          synchronized (this.responseTimes)
          {
            this.responseTimes[hostIndex] = (getLocalTimeBestResolution() - this.transactionStartTime);
          }
        }
        pickNewConnection();
      }
    }
    return result;
  }
  
  private synchronized void pickNewConnection()
    throws SQLException
  {
    if (this.currentConn == null)
    {
      this.currentConn = this.balancer.pickConnection(this, Collections.unmodifiableList(this.hostList), Collections.unmodifiableMap(this.liveConnections), (long[])this.responseTimes.clone(), this.retriesAllDown);
      
      return;
    }
    Connection newConn = this.balancer.pickConnection(this, Collections.unmodifiableList(this.hostList), Collections.unmodifiableMap(this.liveConnections), (long[])this.responseTimes.clone(), this.retriesAllDown);
    
    newConn.setTransactionIsolation(this.currentConn.getTransactionIsolation());
    
    newConn.setAutoCommit(this.currentConn.getAutoCommit());
    this.currentConn = newConn;
  }
  
  Object proxyIfInterfaceIsJdbc(Object toProxy, Class clazz)
  {
    Class[] interfaces = clazz.getInterfaces();
    
    int i = 0;
    if (i < interfaces.length)
    {
      String packageName = interfaces[i].getPackage().getName();
      if (("java.sql".equals(packageName)) || ("javax.sql".equals(packageName))) {
        return Proxy.newProxyInstance(toProxy.getClass().getClassLoader(), interfaces, new ConnectionErrorFiringInvocationHandler(toProxy));
      }
      return proxyIfInterfaceIsJdbc(toProxy, interfaces[i]);
    }
    return toProxy;
  }
  
  private static long getLocalTimeBestResolution()
  {
    if (getLocalTimeMethod != null) {
      try
      {
        return ((Long)getLocalTimeMethod.invoke(null, null)).longValue();
      }
      catch (IllegalArgumentException e) {}catch (IllegalAccessException e) {}catch (InvocationTargetException e) {}
    }
    return System.currentTimeMillis();
  }
  
  public synchronized void doPing()
    throws SQLException
  {
    if (isGlobalBlacklistEnabled())
    {
      SQLException se = null;
      boolean foundHost = false;
      Iterator i;
      synchronized (this)
      {
        for (i = this.hostList.iterator(); i.hasNext();)
        {
          String host = (String)i.next();
          Connection conn = (Connection)this.liveConnections.get(host);
          if (conn != null) {
            try
            {
              conn.ping();
              foundHost = true;
            }
            catch (SQLException e)
            {
              se = e;
              addToGlobalBlacklist(host);
            }
          }
        }
      }
      if (!foundHost) {
        throw se;
      }
    }
    else
    {
      Iterator allConns = this.liveConnections.values().iterator();
      while (allConns.hasNext()) {
        ((Connection)allConns.next()).ping();
      }
    }
  }
  
  public void addToGlobalBlacklist(String host)
  {
    if (isGlobalBlacklistEnabled()) {
      synchronized (globalBlacklist)
      {
        globalBlacklist.put(host, new Long(System.currentTimeMillis() + this.globalBlacklistTimeout));
      }
    }
  }
  
  public boolean isGlobalBlacklistEnabled()
  {
    return this.globalBlacklistTimeout > 0;
  }
  
  public Map getGlobalBlacklist()
  {
    if (!isGlobalBlacklistEnabled()) {
      return new HashMap(1);
    }
    Map blacklistClone = new HashMap(globalBlacklist.size());
    synchronized (globalBlacklist)
    {
      blacklistClone.putAll(globalBlacklist);
    }
    Set keys = blacklistClone.keySet();
    
    keys.retainAll(this.hostList);
    if (keys.size() == this.hostList.size()) {
      return new HashMap(1);
    }
    for (Iterator i = keys.iterator(); i.hasNext();)
    {
      String host = (String)i.next();
      
      Long timeout = (Long)globalBlacklist.get(host);
      if ((timeout != null) && (timeout.longValue() < System.currentTimeMillis()))
      {
        synchronized (globalBlacklist)
        {
          globalBlacklist.remove(host);
        }
        i.remove();
      }
    }
    return blacklistClone;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\mysql\jdbc\LoadBalancingConnectionProxy.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */