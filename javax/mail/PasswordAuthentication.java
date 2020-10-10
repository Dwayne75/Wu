package javax.mail;

public final class PasswordAuthentication
{
  private final String userName;
  private final String password;
  
  public PasswordAuthentication(String userName, String password)
  {
    this.userName = userName;
    this.password = password;
  }
  
  public String getUserName()
  {
    return this.userName;
  }
  
  public String getPassword()
  {
    return this.password;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\javax\mail\PasswordAuthentication.class
 * Java compiler version: 4 (48.0)
 * JD-Core Version:       0.7.1
 */