package com.mysql.jdbc;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Proxy;
import java.net.URLDecoder;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

public class NonRegisteringDriver
  implements Driver
{
  private static final String REPLICATION_URL_PREFIX = "jdbc:mysql:replication://";
  private static final String URL_PREFIX = "jdbc:mysql://";
  private static final String MXJ_URL_PREFIX = "jdbc:mysql:mxj://";
  private static final String LOADBALANCE_URL_PREFIX = "jdbc:mysql:loadbalance://";
  public static final String DBNAME_PROPERTY_KEY = "DBNAME";
  public static final boolean DEBUG = false;
  public static final int HOST_NAME_INDEX = 0;
  public static final String HOST_PROPERTY_KEY = "HOST";
  public static final String NUM_HOSTS_PROPERTY_KEY = "NUM_HOSTS";
  public static final String PASSWORD_PROPERTY_KEY = "password";
  public static final int PORT_NUMBER_INDEX = 1;
  public static final String PORT_PROPERTY_KEY = "PORT";
  public static final String PROPERTIES_TRANSFORM_KEY = "propertiesTransform";
  public static final boolean TRACE = false;
  public static final String USE_CONFIG_PROPERTY_KEY = "useConfigs";
  public static final String USER_PROPERTY_KEY = "user";
  
  static int getMajorVersionInternal()
  {
    return safeIntParse("5");
  }
  
  static int getMinorVersionInternal()
  {
    return safeIntParse("1");
  }
  
  protected static String[] parseHostPortPair(String hostPortPair)
    throws SQLException
  {
    int portIndex = hostPortPair.indexOf(":");
    
    String[] splitValues = new String[2];
    
    String hostname = null;
    if (portIndex != -1)
    {
      if (portIndex + 1 < hostPortPair.length())
      {
        String portAsString = hostPortPair.substring(portIndex + 1);
        hostname = hostPortPair.substring(0, portIndex);
        
        splitValues[0] = hostname;
        
        splitValues[1] = portAsString;
      }
      else
      {
        throw SQLError.createSQLException(Messages.getString("NonRegisteringDriver.37"), "01S00", null);
      }
    }
    else
    {
      splitValues[0] = hostPortPair;
      splitValues[1] = null;
    }
    return splitValues;
  }
  
  private static int safeIntParse(String intAsString)
  {
    try
    {
      return Integer.parseInt(intAsString);
    }
    catch (NumberFormatException nfe) {}
    return 0;
  }
  
  public NonRegisteringDriver()
    throws SQLException
  {}
  
  public boolean acceptsURL(String url)
    throws SQLException
  {
    return parseURL(url, null) != null;
  }
  
  public Connection connect(String url, Properties info)
    throws SQLException
  {
    if (url != null)
    {
      if (StringUtils.startsWithIgnoreCase(url, "jdbc:mysql:loadbalance://")) {
        return connectLoadBalanced(url, info);
      }
      if (StringUtils.startsWithIgnoreCase(url, "jdbc:mysql:replication://")) {
        return connectReplicationConnection(url, info);
      }
    }
    Properties props = null;
    if ((props = parseURL(url, info)) == null) {
      return null;
    }
    try
    {
      return ConnectionImpl.getInstance(host(props), port(props), props, database(props), url);
    }
    catch (SQLException sqlEx)
    {
      throw sqlEx;
    }
    catch (Exception ex)
    {
      SQLException sqlEx = SQLError.createSQLException(Messages.getString("NonRegisteringDriver.17") + ex.toString() + Messages.getString("NonRegisteringDriver.18"), "08001", null);
      
      sqlEx.initCause(ex);
      
      throw sqlEx;
    }
  }
  
  private Connection connectLoadBalanced(String url, Properties info)
    throws SQLException
  {
    Properties parsedProps = parseURL(url, info);
    
    parsedProps.remove("roundRobinLoadBalance");
    if (parsedProps == null) {
      return null;
    }
    int numHosts = Integer.parseInt(parsedProps.getProperty("NUM_HOSTS"));
    
    List hostList = new ArrayList();
    for (int i = 0; i < numHosts; i++)
    {
      int index = i + 1;
      
      hostList.add(parsedProps.getProperty(new StringBuffer().append("HOST.").append(index).toString()) + ":" + parsedProps.getProperty(new StringBuffer().append("PORT.").append(index).toString()));
    }
    LoadBalancingConnectionProxy proxyBal = new LoadBalancingConnectionProxy(hostList, parsedProps);
    
    return (Connection)Proxy.newProxyInstance(getClass().getClassLoader(), new Class[] { Connection.class }, proxyBal);
  }
  
  protected Connection connectReplicationConnection(String url, Properties info)
    throws SQLException
  {
    Properties parsedProps = parseURL(url, info);
    if (parsedProps == null) {
      return null;
    }
    Properties masterProps = (Properties)parsedProps.clone();
    Properties slavesProps = (Properties)parsedProps.clone();
    
    slavesProps.setProperty("com.mysql.jdbc.ReplicationConnection.isSlave", "true");
    
    int numHosts = Integer.parseInt(parsedProps.getProperty("NUM_HOSTS"));
    if (numHosts < 2) {
      throw SQLError.createSQLException("Must specify at least one slave host to connect to for master/slave replication load-balancing functionality", "01S00", null);
    }
    for (int i = 1; i < numHosts; i++)
    {
      int index = i + 1;
      
      masterProps.remove("HOST." + index);
      masterProps.remove("PORT." + index);
      
      slavesProps.setProperty("HOST." + i, parsedProps.getProperty("HOST." + index));
      slavesProps.setProperty("PORT." + i, parsedProps.getProperty("PORT." + index));
    }
    masterProps.setProperty("NUM_HOSTS", "1");
    slavesProps.remove("HOST." + numHosts);
    slavesProps.remove("PORT." + numHosts);
    slavesProps.setProperty("NUM_HOSTS", String.valueOf(numHosts - 1));
    slavesProps.setProperty("HOST", slavesProps.getProperty("HOST.1"));
    slavesProps.setProperty("PORT", slavesProps.getProperty("PORT.1"));
    
    return new ReplicationConnection(masterProps, slavesProps);
  }
  
  public String database(Properties props)
  {
    return props.getProperty("DBNAME");
  }
  
  public int getMajorVersion()
  {
    return getMajorVersionInternal();
  }
  
  public int getMinorVersion()
  {
    return getMinorVersionInternal();
  }
  
  public DriverPropertyInfo[] getPropertyInfo(String url, Properties info)
    throws SQLException
  {
    if (info == null) {
      info = new Properties();
    }
    if ((url != null) && (url.startsWith("jdbc:mysql://"))) {
      info = parseURL(url, info);
    }
    DriverPropertyInfo hostProp = new DriverPropertyInfo("HOST", info.getProperty("HOST"));
    
    hostProp.required = true;
    hostProp.description = Messages.getString("NonRegisteringDriver.3");
    
    DriverPropertyInfo portProp = new DriverPropertyInfo("PORT", info.getProperty("PORT", "3306"));
    
    portProp.required = false;
    portProp.description = Messages.getString("NonRegisteringDriver.7");
    
    DriverPropertyInfo dbProp = new DriverPropertyInfo("DBNAME", info.getProperty("DBNAME"));
    
    dbProp.required = false;
    dbProp.description = "Database name";
    
    DriverPropertyInfo userProp = new DriverPropertyInfo("user", info.getProperty("user"));
    
    userProp.required = true;
    userProp.description = Messages.getString("NonRegisteringDriver.13");
    
    DriverPropertyInfo passwordProp = new DriverPropertyInfo("password", info.getProperty("password"));
    
    passwordProp.required = true;
    passwordProp.description = Messages.getString("NonRegisteringDriver.16");
    
    DriverPropertyInfo[] dpi = ConnectionPropertiesImpl.exposeAsDriverPropertyInfo(info, 5);
    
    dpi[0] = hostProp;
    dpi[1] = portProp;
    dpi[2] = dbProp;
    dpi[3] = userProp;
    dpi[4] = passwordProp;
    
    return dpi;
  }
  
  public String host(Properties props)
  {
    return props.getProperty("HOST", "localhost");
  }
  
  public boolean jdbcCompliant()
  {
    return false;
  }
  
  public Properties parseURL(String url, Properties defaults)
    throws SQLException
  {
    Properties urlProps = defaults != null ? new Properties(defaults) : new Properties();
    if (url == null) {
      return null;
    }
    if ((!StringUtils.startsWithIgnoreCase(url, "jdbc:mysql://")) && (!StringUtils.startsWithIgnoreCase(url, "jdbc:mysql:mxj://")) && (!StringUtils.startsWithIgnoreCase(url, "jdbc:mysql:loadbalance://")) && (!StringUtils.startsWithIgnoreCase(url, "jdbc:mysql:replication://"))) {
      return null;
    }
    int beginningOfSlashes = url.indexOf("//");
    if (StringUtils.startsWithIgnoreCase(url, "jdbc:mysql:mxj://")) {
      urlProps.setProperty("socketFactory", "com.mysql.management.driverlaunched.ServerLauncherSocketFactory");
    }
    int index = url.indexOf("?");
    if (index != -1)
    {
      String paramString = url.substring(index + 1, url.length());
      url = url.substring(0, index);
      
      StringTokenizer queryParams = new StringTokenizer(paramString, "&");
      while (queryParams.hasMoreTokens())
      {
        String parameterValuePair = queryParams.nextToken();
        
        int indexOfEquals = StringUtils.indexOfIgnoreCase(0, parameterValuePair, "=");
        
        String parameter = null;
        String value = null;
        if (indexOfEquals != -1)
        {
          parameter = parameterValuePair.substring(0, indexOfEquals);
          if (indexOfEquals + 1 < parameterValuePair.length()) {
            value = parameterValuePair.substring(indexOfEquals + 1);
          }
        }
        if ((value != null) && (value.length() > 0) && (parameter != null) && (parameter.length() > 0)) {
          try
          {
            urlProps.put(parameter, URLDecoder.decode(value, "UTF-8"));
          }
          catch (UnsupportedEncodingException badEncoding)
          {
            urlProps.put(parameter, URLDecoder.decode(value));
          }
          catch (NoSuchMethodError nsme)
          {
            urlProps.put(parameter, URLDecoder.decode(value));
          }
        }
      }
    }
    url = url.substring(beginningOfSlashes + 2);
    
    String hostStuff = null;
    
    int slashIndex = url.indexOf("/");
    if (slashIndex != -1)
    {
      hostStuff = url.substring(0, slashIndex);
      if (slashIndex + 1 < url.length()) {
        urlProps.put("DBNAME", url.substring(slashIndex + 1, url.length()));
      }
    }
    else
    {
      hostStuff = url;
    }
    int numHosts = 0;
    if ((hostStuff != null) && (hostStuff.trim().length() > 0))
    {
      StringTokenizer st = new StringTokenizer(hostStuff, ",");
      while (st.hasMoreTokens())
      {
        numHosts++;
        
        String[] hostPortPair = parseHostPortPair(st.nextToken());
        if ((hostPortPair[0] != null) && (hostPortPair[0].trim().length() > 0)) {
          urlProps.setProperty("HOST." + numHosts, hostPortPair[0]);
        } else {
          urlProps.setProperty("HOST." + numHosts, "localhost");
        }
        if (hostPortPair[1] != null) {
          urlProps.setProperty("PORT." + numHosts, hostPortPair[1]);
        } else {
          urlProps.setProperty("PORT." + numHosts, "3306");
        }
      }
    }
    else
    {
      numHosts = 1;
      urlProps.setProperty("HOST.1", "localhost");
      urlProps.setProperty("PORT.1", "3306");
    }
    urlProps.setProperty("NUM_HOSTS", String.valueOf(numHosts));
    urlProps.setProperty("HOST", urlProps.getProperty("HOST.1"));
    urlProps.setProperty("PORT", urlProps.getProperty("PORT.1"));
    
    String propertiesTransformClassName = urlProps.getProperty("propertiesTransform");
    if (propertiesTransformClassName != null) {
      try
      {
        ConnectionPropertiesTransform propTransformer = (ConnectionPropertiesTransform)Class.forName(propertiesTransformClassName).newInstance();
        
        urlProps = propTransformer.transformProperties(urlProps);
      }
      catch (InstantiationException e)
      {
        throw SQLError.createSQLException("Unable to create properties transform instance '" + propertiesTransformClassName + "' due to underlying exception: " + e.toString(), "01S00", null);
      }
      catch (IllegalAccessException e)
      {
        throw SQLError.createSQLException("Unable to create properties transform instance '" + propertiesTransformClassName + "' due to underlying exception: " + e.toString(), "01S00", null);
      }
      catch (ClassNotFoundException e)
      {
        throw SQLError.createSQLException("Unable to create properties transform instance '" + propertiesTransformClassName + "' due to underlying exception: " + e.toString(), "01S00", null);
      }
    }
    if ((Util.isColdFusion()) && (urlProps.getProperty("autoConfigureForColdFusion", "true").equalsIgnoreCase("true")))
    {
      String configs = urlProps.getProperty("useConfigs");
      
      StringBuffer newConfigs = new StringBuffer();
      if (configs != null)
      {
        newConfigs.append(configs);
        newConfigs.append(",");
      }
      newConfigs.append("coldFusion");
      
      urlProps.setProperty("useConfigs", newConfigs.toString());
    }
    String configNames = null;
    if (defaults != null) {
      configNames = defaults.getProperty("useConfigs");
    }
    if (configNames == null) {
      configNames = urlProps.getProperty("useConfigs");
    }
    if (configNames != null)
    {
      List splitNames = StringUtils.split(configNames, ",", true);
      
      Properties configProps = new Properties();
      
      Iterator namesIter = splitNames.iterator();
      while (namesIter.hasNext())
      {
        String configName = (String)namesIter.next();
        try
        {
          InputStream configAsStream = getClass().getResourceAsStream("configs/" + configName + ".properties");
          if (configAsStream == null) {
            throw SQLError.createSQLException("Can't find configuration template named '" + configName + "'", "01S00", null);
          }
          configProps.load(configAsStream);
        }
        catch (IOException ioEx)
        {
          SQLException sqlEx = SQLError.createSQLException("Unable to load configuration template '" + configName + "' due to underlying IOException: " + ioEx, "01S00", null);
          
          sqlEx.initCause(ioEx);
          
          throw sqlEx;
        }
      }
      Iterator propsIter = urlProps.keySet().iterator();
      while (propsIter.hasNext())
      {
        String key = propsIter.next().toString();
        String property = urlProps.getProperty(key);
        configProps.setProperty(key, property);
      }
      urlProps = configProps;
    }
    if (defaults != null)
    {
      Iterator propsIter = defaults.keySet().iterator();
      while (propsIter.hasNext())
      {
        String key = propsIter.next().toString();
        if (!key.equals("NUM_HOSTS"))
        {
          String property = defaults.getProperty(key);
          urlProps.setProperty(key, property);
        }
      }
    }
    return urlProps;
  }
  
  public int port(Properties props)
  {
    return Integer.parseInt(props.getProperty("PORT", "3306"));
  }
  
  public String property(String name, Properties props)
  {
    return props.getProperty(name);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\mysql\jdbc\NonRegisteringDriver.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */