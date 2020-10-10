package javax.mail;

public class NoSuchProviderException
  extends MessagingException
{
  private static final long serialVersionUID = 8058319293154708827L;
  
  public NoSuchProviderException() {}
  
  public NoSuchProviderException(String message)
  {
    super(message);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\javax\mail\NoSuchProviderException.class
 * Java compiler version: 4 (48.0)
 * JD-Core Version:       0.7.1
 */