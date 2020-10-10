package com.mysql.jdbc.jdbc2.optional;

import com.mysql.jdbc.ExceptionInterceptor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.SQLException;
import java.util.Map;

abstract class WrapperBase
{
  protected MysqlPooledConnection pooledConnection;
  
  protected void checkAndFireConnectionError(SQLException sqlEx)
    throws SQLException
  {
    if ((this.pooledConnection != null) && 
      ("08S01".equals(sqlEx.getSQLState()))) {
      this.pooledConnection.callConnectionEventListeners(1, sqlEx);
    }
    throw sqlEx;
  }
  
  protected Map unwrappedInterfaces = null;
  protected ExceptionInterceptor exceptionInterceptor;
  
  protected WrapperBase(MysqlPooledConnection pooledConnection)
  {
    this.pooledConnection = pooledConnection;
    this.exceptionInterceptor = this.pooledConnection.getExceptionInterceptor();
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
          result = proxyIfInterfaceIsJdbc(result, result.getClass());
        }
      }
      catch (InvocationTargetException e)
      {
        if ((e.getTargetException() instanceof SQLException)) {
          WrapperBase.this.checkAndFireConnectionError((SQLException)e.getTargetException());
        } else {
          throw e;
        }
      }
      return result;
    }
    
    private Object proxyIfInterfaceIsJdbc(Object toProxy, Class clazz)
    {
      Class[] interfaces = clazz.getInterfaces();
      
      int i = 0;
      if (i < interfaces.length)
      {
        String packageName = interfaces[i].getPackage().getName();
        if (("java.sql".equals(packageName)) || ("javax.sql".equals(packageName))) {
          return Proxy.newProxyInstance(toProxy.getClass().getClassLoader(), interfaces, new ConnectionErrorFiringInvocationHandler(WrapperBase.this, toProxy));
        }
        return proxyIfInterfaceIsJdbc(toProxy, interfaces[i]);
      }
      return toProxy;
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\mysql\jdbc\jdbc2\optional\WrapperBase.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */