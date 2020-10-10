package coffee.keenan.network.config;

public class DefaultConfiguration
  implements IConfiguration
{
  public int getTimeout()
  {
    return 3000;
  }
  
  public String getTestUrl()
  {
    return "www.google.com";
  }
  
  public int getTestPort()
  {
    return 80;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\coffee\keenan\network\config\DefaultConfiguration.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */