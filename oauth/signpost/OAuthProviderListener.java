package oauth.signpost;

import oauth.signpost.http.HttpRequest;
import oauth.signpost.http.HttpResponse;

public abstract interface OAuthProviderListener
{
  public abstract void prepareRequest(HttpRequest paramHttpRequest)
    throws Exception;
  
  public abstract void prepareSubmission(HttpRequest paramHttpRequest)
    throws Exception;
  
  public abstract boolean onResponseReceived(HttpRequest paramHttpRequest, HttpResponse paramHttpResponse)
    throws Exception;
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\oauth\signpost\OAuthProviderListener.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */