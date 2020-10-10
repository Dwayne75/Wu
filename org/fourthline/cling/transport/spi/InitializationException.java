package org.fourthline.cling.transport.spi;

public class InitializationException
  extends RuntimeException
{
  public InitializationException(String s)
  {
    super(s);
  }
  
  public InitializationException(String s, Throwable throwable)
  {
    super(s, throwable);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\transport\spi\InitializationException.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */