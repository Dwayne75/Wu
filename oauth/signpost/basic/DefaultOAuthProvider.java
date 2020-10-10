package oauth.signpost.basic;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import oauth.signpost.AbstractOAuthProvider;
import oauth.signpost.http.HttpRequest;
import oauth.signpost.http.HttpResponse;

public class DefaultOAuthProvider
  extends AbstractOAuthProvider
{
  private static final long serialVersionUID = 1L;
  
  public DefaultOAuthProvider(String requestTokenEndpointUrl, String accessTokenEndpointUrl, String authorizationWebsiteUrl)
  {
    super(requestTokenEndpointUrl, accessTokenEndpointUrl, authorizationWebsiteUrl);
  }
  
  protected HttpRequest createRequest(String endpointUrl)
    throws MalformedURLException, IOException
  {
    HttpURLConnection connection = (HttpURLConnection)new URL(endpointUrl).openConnection();
    connection.setRequestMethod("POST");
    connection.setAllowUserInteraction(false);
    connection.setRequestProperty("Content-Length", "0");
    return new HttpURLConnectionRequestAdapter(connection);
  }
  
  protected HttpResponse sendRequest(HttpRequest request)
    throws IOException
  {
    HttpURLConnection connection = (HttpURLConnection)request.unwrap();
    connection.connect();
    return new HttpURLConnectionResponseAdapter(connection);
  }
  
  protected void closeConnection(HttpRequest request, HttpResponse response)
  {
    HttpURLConnection connection = (HttpURLConnection)request.unwrap();
    if (connection != null) {
      connection.disconnect();
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\oauth\signpost\basic\DefaultOAuthProvider.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */