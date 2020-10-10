package winterwell.jtwitter;

import java.net.HttpURLConnection;
import oauth.signpost.AbstractOAuthConsumer;
import oauth.signpost.basic.HttpURLConnectionRequestAdapter;
import oauth.signpost.http.HttpRequest;

class SimpleOAuthConsumer
  extends AbstractOAuthConsumer
{
  private static final long serialVersionUID = 1L;
  
  public SimpleOAuthConsumer(String consumerKey, String consumerSecret)
  {
    super(consumerKey, consumerSecret);
  }
  
  protected HttpRequest wrap(Object request)
  {
    if ((request instanceof HttpRequest)) {
      return (HttpRequest)request;
    }
    return new HttpURLConnectionRequestAdapter(
      (HttpURLConnection)request);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\winterwell\jtwitter\SimpleOAuthConsumer.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */