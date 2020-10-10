package org.apache.http;

public class HttpException
  extends Exception
{
  private static final long serialVersionUID = -5437299376222011036L;
  
  public HttpException() {}
  
  public HttpException(String message)
  {
    super(message);
  }
  
  public HttpException(String message, Throwable cause)
  {
    super(message);
    initCause(cause);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\apache\http\HttpException.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */