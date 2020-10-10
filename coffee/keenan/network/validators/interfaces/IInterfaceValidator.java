package coffee.keenan.network.validators.interfaces;

import coffee.keenan.network.config.IConfiguration;
import java.net.NetworkInterface;

public abstract interface IInterfaceValidator
{
  public abstract boolean validate(NetworkInterface paramNetworkInterface, IConfiguration paramIConfiguration);
  
  public abstract Exception getException();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\coffee\keenan\network\validators\interfaces\IInterfaceValidator.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */