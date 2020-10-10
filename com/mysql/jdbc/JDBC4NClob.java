package com.mysql.jdbc;

import java.sql.NClob;

public class JDBC4NClob
  extends Clob
  implements NClob
{
  JDBC4NClob(ExceptionInterceptor exceptionInterceptor)
  {
    super(exceptionInterceptor);
  }
  
  JDBC4NClob(String charDataInit, ExceptionInterceptor exceptionInterceptor)
  {
    super(charDataInit, exceptionInterceptor);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\mysql\jdbc\JDBC4NClob.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */