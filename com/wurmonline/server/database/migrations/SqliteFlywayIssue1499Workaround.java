package com.wurmonline.server.database.migrations;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;
import javax.sql.DataSource;
import org.sqlite.SQLiteDataSource;

public class SqliteFlywayIssue1499Workaround
  implements DataSource
{
  private final SQLiteDataSource dataSource;
  private Connection connection;
  
  public SqliteFlywayIssue1499Workaround(SQLiteDataSource dataSource)
  {
    this.dataSource = dataSource;
  }
  
  public Connection getConnection()
    throws SQLException
  {
    if ((this.connection == null) || (this.connection.isClosed())) {
      this.connection = this.dataSource.getConnection();
    }
    return this.connection;
  }
  
  public Connection getConnection(String username, String password)
    throws SQLException
  {
    return getConnection();
  }
  
  public <T> T unwrap(Class<T> iface)
    throws SQLException
  {
    return (T)this.dataSource.unwrap(iface);
  }
  
  public boolean isWrapperFor(Class<?> iface)
    throws SQLException
  {
    return SQLiteDataSource.class.equals(iface);
  }
  
  public PrintWriter getLogWriter()
    throws SQLException
  {
    return this.dataSource.getLogWriter();
  }
  
  public void setLogWriter(PrintWriter out)
    throws SQLException
  {
    this.dataSource.setLogWriter(out);
  }
  
  public void setLoginTimeout(int seconds)
    throws SQLException
  {
    this.dataSource.setLoginTimeout(seconds);
  }
  
  public int getLoginTimeout()
    throws SQLException
  {
    return this.dataSource.getLoginTimeout();
  }
  
  public Logger getParentLogger()
    throws SQLFeatureNotSupportedException
  {
    return this.dataSource.getParentLogger();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\database\migrations\SqliteFlywayIssue1499Workaround.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */