package coffee.keenan.network.validators.address;

import coffee.keenan.network.config.IConfiguration;
import java.net.Inet4Address;
import java.net.InetAddress;

public class IP4Validator
  implements IAddressValidator
{
  public boolean validate(InetAddress address, IConfiguration configuration)
  {
    return address instanceof Inet4Address;
  }
  
  public Exception getException()
  {
    return new Exception("address is not an instance of Inet4Address");
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\coffee\keenan\network\validators\address\IP4Validator.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */