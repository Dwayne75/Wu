package javax.mail;

import java.net.InetAddress;

public abstract class Authenticator
{
  private InetAddress requestingSite;
  private int requestingPort;
  private String requestingProtocol;
  private String requestingPrompt;
  private String requestingUserName;
  
  private void reset()
  {
    this.requestingSite = null;
    this.requestingPort = -1;
    this.requestingProtocol = null;
    this.requestingPrompt = null;
    this.requestingUserName = null;
  }
  
  final PasswordAuthentication requestPasswordAuthentication(InetAddress addr, int port, String protocol, String prompt, String defaultUserName)
  {
    reset();
    this.requestingSite = addr;
    this.requestingPort = port;
    this.requestingProtocol = protocol;
    this.requestingPrompt = prompt;
    this.requestingUserName = defaultUserName;
    return getPasswordAuthentication();
  }
  
  protected final InetAddress getRequestingSite()
  {
    return this.requestingSite;
  }
  
  protected final int getRequestingPort()
  {
    return this.requestingPort;
  }
  
  protected final String getRequestingProtocol()
  {
    return this.requestingProtocol;
  }
  
  protected final String getRequestingPrompt()
  {
    return this.requestingPrompt;
  }
  
  protected final String getDefaultUserName()
  {
    return this.requestingUserName;
  }
  
  protected PasswordAuthentication getPasswordAuthentication()
  {
    return null;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\javax\mail\Authenticator.class
 * Java compiler version: 4 (48.0)
 * JD-Core Version:       0.7.1
 */