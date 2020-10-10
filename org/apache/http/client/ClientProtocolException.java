package org.apache.http.client;

import java.io.IOException;
import org.apache.http.annotation.Immutable;

@Immutable
public class ClientProtocolException
  extends IOException
{
  private static final long serialVersionUID = -5596590843227115865L;
  
  public ClientProtocolException() {}
  
  public ClientProtocolException(String s)
  {
    super(s);
  }
  
  public ClientProtocolException(Throwable cause)
  {
    initCause(cause);
  }
  
  public ClientProtocolException(String message, Throwable cause)
  {
    super(message);
    initCause(cause);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\apache\http\client\ClientProtocolException.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */