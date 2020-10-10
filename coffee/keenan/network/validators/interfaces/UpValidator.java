package coffee.keenan.network.validators.interfaces;

import coffee.keenan.network.config.IConfiguration;
import java.net.NetworkInterface;

public class UpValidator
  implements IInterfaceValidator
{
  private Exception exception;
  
  public boolean validate(NetworkInterface networkInterface, IConfiguration configuration)
  {
    try
    {
      return networkInterface.isUp();
    }
    catch (Exception e)
    {
      this.exception = e;
    }
    return false;
  }
  
  public Exception getException()
  {
    return this.exception == null ? new Exception("interface is not up") : this.exception;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\coffee\keenan\network\validators\interfaces\UpValidator.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */