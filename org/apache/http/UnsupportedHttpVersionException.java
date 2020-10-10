package org.apache.http;

public class UnsupportedHttpVersionException
  extends ProtocolException
{
  private static final long serialVersionUID = -1348448090193107031L;
  
  public UnsupportedHttpVersionException() {}
  
  public UnsupportedHttpVersionException(String message)
  {
    super(message);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\apache\http\UnsupportedHttpVersionException.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */