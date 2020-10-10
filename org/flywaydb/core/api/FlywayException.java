package org.flywaydb.core.api;

public class FlywayException
  extends RuntimeException
{
  public FlywayException(String message, Throwable cause)
  {
    super(message, cause);
  }
  
  public FlywayException(Throwable cause)
  {
    super(cause);
  }
  
  public FlywayException(String message)
  {
    super(message);
  }
  
  public FlywayException() {}
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\flywaydb\core\api\FlywayException.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */