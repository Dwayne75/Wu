package oauth.signpost.exception;

public abstract class OAuthException
  extends Exception
{
  public OAuthException(String message)
  {
    super(message);
  }
  
  public OAuthException(Throwable cause)
  {
    super(cause);
  }
  
  public OAuthException(String message, Throwable cause)
  {
    super(message, cause);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\oauth\signpost\exception\OAuthException.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */