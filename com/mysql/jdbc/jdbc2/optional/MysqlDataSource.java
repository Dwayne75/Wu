package com.mysql.jdbc.jdbc2.optional;

import com.mysql.jdbc.ConnectionPropertiesImpl;
import com.mysql.jdbc.NonRegisteringDriver;
import java.io.PrintWriter;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.Referenceable;
import javax.naming.StringRefAddr;
import javax.sql.DataSource;

public class MysqlDataSource
  extends ConnectionPropertiesImpl
  implements DataSource, Referenceable, Serializable
{
  protected static NonRegisteringDriver mysqlDriver = null;
  
  static
  {
    try
    {
      mysqlDriver = new NonRegisteringDriver();
    }
    catch (Exception E)
    {
      throw new RuntimeException("Can not load Driver class com.mysql.jdbc.Driver");
    }
  }
  
  protected PrintWriter logWriter = null;
  protected String databaseName = null;
  protected String encoding = null;
  protected String hostName = null;
  protected String password = null;
  protected String profileSql = "false";
  protected String url = null;
  protected String user = null;
  protected boolean explicitUrl = false;
  protected int port = 3306;
  
  public Connection getConnection()
    throws SQLException
  {
    return getConnection(this.user, this.password);
  }
  
  public Connection getConnection(String userID, String pass)
    throws SQLException
  {
    Properties props = new Properties();
    if (userID != null) {
      props.setProperty("user", userID);
    }
    if (pass != null) {
      props.setProperty("password", pass);
    }
    exposeAsProperties(props);
    
    return getConnection(props);
  }
  
  public void setDatabaseName(String dbName)
  {
    this.databaseName = dbName;
  }
  
  public String getDatabaseName()
  {
    return this.databaseName != null ? this.databaseName : "";
  }
  
  public void setLogWriter(PrintWriter output)
    throws SQLException
  {
    this.logWriter = output;
  }
  
  public PrintWriter getLogWriter()
  {
    return this.logWriter;
  }
  
  public int getLoginTimeout()
  {
    return 0;
  }
  
  public void setPassword(String pass)
  {
    this.password = pass;
  }
  
  public void setPort(int p)
  {
    this.port = p;
  }
  
  public int getPort()
  {
    return this.port;
  }
  
  public void setPortNumber(int p)
  {
    setPort(p);
  }
  
  public int getPortNumber()
  {
    return getPort();
  }
  
  public void setPropertiesViaRef(Reference ref)
    throws SQLException
  {
    super.initializeFromRef(ref);
  }
  
  public Reference getReference()
    throws NamingException
  {
    String factoryName = "com.mysql.jdbc.jdbc2.optional.MysqlDataSourceFactory";
    Reference ref = new Reference(getClass().getName(), factoryName, null);
    ref.add(new StringRefAddr("user", getUser()));
    
    ref.add(new StringRefAddr("password", this.password));
    
    ref.add(new StringRefAddr("serverName", getServerName()));
    ref.add(new StringRefAddr("port", "" + getPort()));
    ref.add(new StringRefAddr("databaseName", getDatabaseName()));
    ref.add(new StringRefAddr("url", getUrl()));
    ref.add(new StringRefAddr("explicitUrl", String.valueOf(this.explicitUrl)));
    try
    {
      storeToRef(ref);
    }
    catch (SQLException sqlEx)
    {
      throw new NamingException(sqlEx.getMessage());
    }
    return ref;
  }
  
  public void setServerName(String serverName)
  {
    this.hostName = serverName;
  }
  
  public String getServerName()
  {
    return this.hostName != null ? this.hostName : "";
  }
  
  public void setURL(String url)
  {
    setUrl(url);
  }
  
  public String getURL()
  {
    return getUrl();
  }
  
  public void setUrl(String url)
  {
    this.url = url;
    this.explicitUrl = true;
  }
  
  public String getUrl()
  {
    if (!this.explicitUrl)
    {
      String builtUrl = "jdbc:mysql://";
      builtUrl = builtUrl + getServerName() + ":" + getPort() + "/" + getDatabaseName();
      
      return builtUrl;
    }
    return this.url;
  }
  
  public void setUser(String userID)
  {
    this.user = userID;
  }
  
  public String getUser()
  {
    return this.user;
  }
  
  protected Connection getConnection(Properties props)
    throws SQLException
  {
    String jdbcUrlToUse = null;
    if (!this.explicitUrl)
    {
      StringBuffer jdbcUrl = new StringBuffer("jdbc:mysql://");
      if (this.hostName != null) {
        jdbcUrl.append(this.hostName);
      }
      jdbcUrl.append(":");
      jdbcUrl.append(this.port);
      jdbcUrl.append("/");
      if (this.databaseName != null) {
        jdbcUrl.append(this.databaseName);
      }
      jdbcUrlToUse = jdbcUrl.toString();
    }
    else
    {
      jdbcUrlToUse = this.url;
    }
    Properties urlProps = mysqlDriver.parseURL(jdbcUrlToUse, null);
    urlProps.remove("DBNAME");
    urlProps.remove("HOST");
    urlProps.remove("PORT");
    
    Iterator keys = urlProps.keySet().iterator();
    while (keys.hasNext())
    {
      String key = (String)keys.next();
      
      props.setProperty(key, urlProps.getProperty(key));
    }
    return mysqlDriver.connect(jdbcUrlToUse, props);
  }
  
  public void setLoginTimeout(int seconds)
    throws SQLException
  {}
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\mysql\jdbc\jdbc2\optional\MysqlDataSource.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */