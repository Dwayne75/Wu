package javax.mail;

public class AuthenticationFailedException
  extends MessagingException
{
  private static final long serialVersionUID = 492080754054436511L;
  
  public AuthenticationFailedException() {}
  
  public AuthenticationFailedException(String message)
  {
    super(message);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\javax\mail\AuthenticationFailedException.class
 * Java compiler version: 4 (48.0)
 * JD-Core Version:       0.7.1
 */