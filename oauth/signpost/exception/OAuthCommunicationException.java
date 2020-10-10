package oauth.signpost.exception;

public class OAuthCommunicationException
  extends OAuthException
{
  private String responseBody;
  
  public OAuthCommunicationException(Exception cause)
  {
    super("Communication with the service provider failed: " + cause.getLocalizedMessage(), cause);
  }
  
  public OAuthCommunicationException(String message, String responseBody)
  {
    super(message);
    this.responseBody = responseBody;
  }
  
  public String getResponseBody()
  {
    return this.responseBody;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\oauth\signpost\exception\OAuthCommunicationException.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */