package coffee.keenan.network.helpers.interfaces;

import coffee.keenan.network.config.DefaultConfiguration;
import coffee.keenan.network.config.IConfiguration;
import coffee.keenan.network.helpers.ErrorTracking;
import coffee.keenan.network.validators.interfaces.IInterfaceValidator;
import coffee.keenan.network.validators.interfaces.NotLoopbackValidator;
import coffee.keenan.network.validators.interfaces.UpValidator;
import java.net.NetworkInterface;
import org.jetbrains.annotations.NotNull;

public class InterfaceHelper
  extends ErrorTracking
{
  private final IConfiguration configuration;
  private IInterfaceValidator[] interfaceValidators = { new NotLoopbackValidator(), new UpValidator() };
  
  public InterfaceHelper()
  {
    this.configuration = new DefaultConfiguration();
  }
  
  public InterfaceHelper(IConfiguration configuration)
  {
    this.configuration = configuration;
  }
  
  public IInterfaceValidator[] getInterfaceValidators()
  {
    return this.interfaceValidators;
  }
  
  public void setInterfaceValidators(@NotNull IInterfaceValidator... validators)
  {
    this.interfaceValidators = validators;
  }
  
  public boolean validateInterface(NetworkInterface networkInterface)
  {
    if (networkInterface == null)
    {
      addException("null interface", new Exception("given interface was null"));
      return false;
    }
    for (IInterfaceValidator validator : getInterfaceValidators()) {
      if (!validator.validate(networkInterface, this.configuration))
      {
        addException("interface: " + networkInterface.getDisplayName() + "(" + networkInterface.getName() + ")", validator.getException());
        return false;
      }
    }
    return true;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\coffee\keenan\network\helpers\interfaces\InterfaceHelper.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */