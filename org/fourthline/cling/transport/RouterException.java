package org.fourthline.cling.transport;

public class RouterException
  extends Exception
{
  public RouterException() {}
  
  public RouterException(String s)
  {
    super(s);
  }
  
  public RouterException(String s, Throwable throwable)
  {
    super(s, throwable);
  }
  
  public RouterException(Throwable throwable)
  {
    super(throwable);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\transport\RouterException.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */