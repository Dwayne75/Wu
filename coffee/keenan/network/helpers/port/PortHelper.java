package coffee.keenan.network.helpers.port;

import coffee.keenan.network.config.DefaultConfiguration;
import coffee.keenan.network.config.IConfiguration;
import coffee.keenan.network.helpers.ErrorTracking;
import coffee.keenan.network.validators.port.IPortValidator;
import coffee.keenan.network.wrappers.upnp.UPNPService;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class PortHelper
  extends ErrorTracking
{
  private final IConfiguration configuration;
  
  public PortHelper()
  {
    this.configuration = new DefaultConfiguration();
  }
  
  public PortHelper(IConfiguration configuration)
  {
    this.configuration = configuration;
  }
  
  public static Port assignPort(@NotNull Port port)
  {
    return assignPort(port, new DefaultConfiguration());
  }
  
  public Port assignFavoredPort(@NotNull Port port)
  {
    Integer p = port.getFavoredPort();
    if ((p != null) && (validatePort(port.getAddress(), p.intValue(), port.getValidators()))) {
      port.setAssignedPort(p.intValue());
    }
    return port;
  }
  
  public static Port assignPort(@NotNull Port port, @NotNull IConfiguration configuration)
  {
    PortHelper portHelper = new PortHelper(configuration);
    portHelper.assignFavoredPort(port);
    if (port.getAssignedPort() != 0) {
      return port;
    }
    for (Iterator localIterator = port.getPorts().iterator(); localIterator.hasNext();)
    {
      int p = ((Integer)localIterator.next()).intValue();
      if (portHelper.validatePort(port.getAddress(), p, port.getValidators()))
      {
        port.setAssignedPort(p);
        break;
      }
    }
    if ((port.isToMap()) && (port.getAssignedPort() != 0)) {
      UPNPService.getInstance().openPort(port);
    }
    return port;
  }
  
  public boolean validatePort(InetAddress address, int port, IPortValidator... validators)
  {
    return validatePort(address, port, Arrays.asList(validators));
  }
  
  public boolean validatePort(InetAddress address, int port, Collection<IPortValidator> validators)
  {
    for (IPortValidator validator : validators) {
      if (!validator.validate(address, this.configuration, port))
      {
        addException("address: " + address.toString() + ", port: " + String.valueOf(port), validator.getException());
        return false;
      }
    }
    return true;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\coffee\keenan\network\helpers\port\PortHelper.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */