package oauth.signpost.http;

import java.io.IOException;
import java.io.InputStream;

public abstract interface HttpResponse
{
  public abstract int getStatusCode()
    throws IOException;
  
  public abstract String getReasonPhrase()
    throws Exception;
  
  public abstract InputStream getContent()
    throws IOException;
  
  public abstract Object unwrap();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\oauth\signpost\http\HttpResponse.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */