package org.fourthline.cling.model;

public class DiscoveryOptions
{
  protected boolean advertised;
  protected boolean byeByeBeforeFirstAlive;
  
  public DiscoveryOptions(boolean advertised)
  {
    this.advertised = advertised;
  }
  
  public DiscoveryOptions(boolean advertised, boolean byeByeBeforeFirstAlive)
  {
    this.advertised = advertised;
    this.byeByeBeforeFirstAlive = byeByeBeforeFirstAlive;
  }
  
  public boolean isAdvertised()
  {
    return this.advertised;
  }
  
  public boolean isByeByeBeforeFirstAlive()
  {
    return this.byeByeBeforeFirstAlive;
  }
  
  private static String simpleName = DiscoveryOptions.class.getSimpleName();
  
  public String toString()
  {
    return "(" + simpleName + ")" + " advertised: " + isAdvertised() + " byebyeBeforeFirstAlive: " + isByeByeBeforeFirstAlive();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\model\DiscoveryOptions.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */