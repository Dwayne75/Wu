package com.mysql.jdbc.interceptors;

import com.mysql.jdbc.Connection;
import com.mysql.jdbc.ResultSetInternalMethods;
import com.mysql.jdbc.Statement;
import com.mysql.jdbc.StatementInterceptor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.SQLException;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ResultSetScannerInterceptor
  implements StatementInterceptor
{
  private Pattern regexP;
  
  public void init(Connection conn, Properties props)
    throws SQLException
  {
    String regexFromUser = props.getProperty("resultSetScannerRegex");
    if ((regexFromUser == null) || (regexFromUser.length() == 0)) {
      throw new SQLException("resultSetScannerRegex must be configured, and must be > 0 characters");
    }
    try
    {
      this.regexP = Pattern.compile(regexFromUser);
    }
    catch (Throwable t)
    {
      SQLException sqlEx = new SQLException("Can't use configured regex due to underlying exception.");
      sqlEx.initCause(t);
      
      throw sqlEx;
    }
  }
  
  public ResultSetInternalMethods postProcess(String sql, Statement interceptedStatement, ResultSetInternalMethods originalResultSet, Connection connection)
    throws SQLException
  {
    ResultSetInternalMethods finalResultSet = originalResultSet;
    
    (ResultSetInternalMethods)Proxy.newProxyInstance(originalResultSet.getClass().getClassLoader(), new Class[] { ResultSetInternalMethods.class }, new InvocationHandler()
    {
      private final ResultSetInternalMethods val$finalResultSet;
      
      public Object invoke(Object proxy, Method method, Object[] args)
        throws Throwable
      {
        Object invocationResult = method.invoke(this.val$finalResultSet, args);
        
        String methodName = method.getName();
        if (((invocationResult != null) && ((invocationResult instanceof String))) || ("getString".equals(methodName)) || ("getObject".equals(methodName)) || ("getObjectStoredProc".equals(methodName)))
        {
          Matcher matcher = ResultSetScannerInterceptor.this.regexP.matcher(invocationResult.toString());
          if (matcher.matches()) {
            throw new SQLException("value disallowed by filter");
          }
        }
        return invocationResult;
      }
    });
  }
  
  public ResultSetInternalMethods preProcess(String sql, Statement interceptedStatement, Connection connection)
    throws SQLException
  {
    return null;
  }
  
  public boolean executeTopLevelOnly()
  {
    return false;
  }
  
  public void destroy() {}
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\mysql\jdbc\interceptors\ResultSetScannerInterceptor.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */