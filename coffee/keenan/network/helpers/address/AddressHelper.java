package coffee.keenan.network.helpers.address;

import coffee.keenan.network.config.DefaultConfiguration;
import coffee.keenan.network.config.IConfiguration;
import coffee.keenan.network.helpers.ErrorTracking;
import coffee.keenan.network.helpers.interfaces.InterfaceHelper;
import coffee.keenan.network.validators.address.IAddressValidator;
import coffee.keenan.network.validators.address.IP4Validator;
import coffee.keenan.network.validators.address.InternetValidator;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AddressHelper
  extends ErrorTracking
{
  private final IConfiguration configuration;
  private Set<IAddressValidator> addressValidators = new HashSet(
    (Collection)Stream.of(new IAddressValidator[] { new IP4Validator(), new InternetValidator() }).collect(Collectors.toList()));
  
  public AddressHelper()
  {
    this.configuration = new DefaultConfiguration();
  }
  
  public AddressHelper(IConfiguration configuration)
  {
    this.configuration = configuration;
  }
  
  @Nullable
  public static InetAddress getFirstValidAddress()
  {
    return getFirstValidAddress(new DefaultConfiguration());
  }
  
  @Nullable
  public static InetAddress getFirstValidAddress(IConfiguration configuration)
  {
    InterfaceHelper interfaceHelper = new InterfaceHelper(configuration);
    AddressHelper addressHelper = new AddressHelper(configuration);
    List<NetworkInterface> interfaces = null;
    try
    {
      interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
    }
    catch (SocketException e)
    {
      e.printStackTrace();
    }
    for (NetworkInterface networkInterface : (List)Objects.requireNonNull(interfaces)) {
      if (interfaceHelper.validateInterface(networkInterface)) {
        for (InetAddress inetAddress : Collections.list(networkInterface.getInetAddresses())) {
          if (addressHelper.validateAddress(inetAddress)) {
            return inetAddress;
          }
        }
      }
    }
    return null;
  }
  
  public void addAddressValidators(@NotNull IAddressValidator... validators)
  {
    this.addressValidators.addAll(Arrays.asList(validators));
  }
  
  public Collection<IAddressValidator> getAddressValidators()
  {
    return this.addressValidators;
  }
  
  public void setAddressValidators(@NotNull IAddressValidator... validators)
  {
    this.addressValidators = new HashSet(Arrays.asList(validators));
  }
  
  public boolean validateAddress(InetAddress address)
  {
    for (IAddressValidator validator : getAddressValidators()) {
      if (!validator.validate(address, this.configuration))
      {
        addException("address: " + address.toString(), validator.getException());
        return false;
      }
    }
    return true;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\coffee\keenan\network\helpers\address\AddressHelper.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */