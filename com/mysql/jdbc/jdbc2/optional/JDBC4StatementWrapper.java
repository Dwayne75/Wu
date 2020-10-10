package com.mysql.jdbc.jdbc2.optional;

import com.mysql.jdbc.SQLError;
import java.lang.reflect.Proxy;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

public class JDBC4StatementWrapper
  extends StatementWrapper
{
  public JDBC4StatementWrapper(ConnectionWrapper c, MysqlPooledConnection conn, Statement toWrap)
  {
    super(c, conn, toWrap);
  }
  
  public void close()
    throws SQLException
  {
    try
    {
      super.close();
    }
    finally
    {
      this.unwrappedInterfaces = null;
    }
  }
  
  public boolean isClosed()
    throws SQLException
  {
    try
    {
      if (this.wrappedStmt != null) {
        return this.wrappedStmt.isClosed();
      }
      throw SQLError.createSQLException("Statement already closed", "S1009", this.exceptionInterceptor);
    }
    catch (SQLException sqlEx)
    {
      checkAndFireConnectionError(sqlEx);
    }
    return false;
  }
  
  public void setPoolable(boolean poolable)
    throws SQLException
  {
    try
    {
      if (this.wrappedStmt != null) {
        this.wrappedStmt.setPoolable(poolable);
      } else {
        throw SQLError.createSQLException("Statement already closed", "S1009", this.exceptionInterceptor);
      }
    }
    catch (SQLException sqlEx)
    {
      checkAndFireConnectionError(sqlEx);
    }
  }
  
  public boolean isPoolable()
    throws SQLException
  {
    try
    {
      if (this.wrappedStmt != null) {
        return this.wrappedStmt.isPoolable();
      }
      throw SQLError.createSQLException("Statement already closed", "S1009", this.exceptionInterceptor);
    }
    catch (SQLException sqlEx)
    {
      checkAndFireConnectionError(sqlEx);
    }
    return false;
  }
  
  public boolean isWrapperFor(Class<?> iface)
    throws SQLException
  {
    boolean isInstance = iface.isInstance(this);
    if (isInstance) {
      return true;
    }
    String interfaceClassName = iface.getName();
    
    return (interfaceClassName.equals("com.mysql.jdbc.Statement")) || (interfaceClassName.equals("java.sql.Statement")) || (interfaceClassName.equals("java.sql.Wrapper"));
  }
  
  public synchronized <T> T unwrap(Class<T> iface)
    throws SQLException
  {
    try
    {
      if (("java.sql.Statement".equals(iface.getName())) || ("java.sql.Wrapper.class".equals(iface.getName()))) {
        return (T)iface.cast(this);
      }
      if (this.unwrappedInterfaces == null) {
        this.unwrappedInterfaces = new HashMap();
      }
      Object cachedUnwrapped = this.unwrappedInterfaces.get(iface);
      if (cachedUnwrapped == null)
      {
        cachedUnwrapped = Proxy.newProxyInstance(this.wrappedStmt.getClass().getClassLoader(), new Class[] { iface }, new WrapperBase.ConnectionErrorFiringInvocationHandler(this, this.wrappedStmt));
        
        this.unwrappedInterfaces.put(iface, cachedUnwrapped);
      }
      return (T)iface.cast(cachedUnwrapped);
    }
    catch (ClassCastException cce)
    {
      throw SQLError.createSQLException("Unable to unwrap to " + iface.toString(), "S1009", this.exceptionInterceptor);
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\mysql\jdbc\jdbc2\optional\JDBC4StatementWrapper.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */