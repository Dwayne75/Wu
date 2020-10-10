package coffee.keenan.network.validators.address;

import coffee.keenan.network.config.IConfiguration;
import java.net.InetAddress;

public abstract interface IAddressValidator
{
  public abstract boolean validate(InetAddress paramInetAddress, IConfiguration paramIConfiguration);
  
  public abstract Exception getException();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\coffee\keenan\network\validators\address\IAddressValidator.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */