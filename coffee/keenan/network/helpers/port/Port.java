package coffee.keenan.network.helpers.port;

import coffee.keenan.network.helpers.ErrorTracking;
import coffee.keenan.network.validators.port.IPortValidator;
import coffee.keenan.network.validators.port.TCPValidator;
import coffee.keenan.network.validators.port.UDPValidator;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.fourthline.cling.support.model.PortMapping;
import org.fourthline.cling.support.model.PortMapping.Protocol;

public class Port
  extends ErrorTracking
{
  private final List<Integer> ports = new ArrayList();
  private Integer favortedPort;
  private final Protocol protocols;
  private final InetAddress address;
  private final Set<IPortValidator> validators = new HashSet();
  private String description = "";
  private boolean toMap;
  private int assignedPort;
  private boolean isMapped;
  
  public Port(InetAddress address, Protocol protocol)
  {
    this.address = address;
    this.protocols = protocol;
    switch (getProtocol())
    {
    case TCP: 
      this.validators.add(new TCPValidator());
      break;
    case UDP: 
      this.validators.add(new UDPValidator());
      break;
    case Both: 
      this.validators.add(new TCPValidator());
      this.validators.add(new UDPValidator());
    }
  }
  
  public Port setFavoredPort(int port)
  {
    this.favortedPort = Integer.valueOf(port);
    return this;
  }
  
  public Integer getFavoredPort()
  {
    return this.favortedPort;
  }
  
  public Port addPort(int port)
  {
    this.ports.add(Integer.valueOf(port));
    return this;
  }
  
  public Port addPortRange(int start, int end)
  {
    for (int i = start; i <= end; i++) {
      this.ports.add(Integer.valueOf(i));
    }
    return this;
  }
  
  public Port addPorts(Integer... ports)
  {
    Collections.addAll(this.ports, ports);
    return this;
  }
  
  public Port addPorts(List<Integer> ports)
  {
    this.ports.addAll(ports);
    return this;
  }
  
  public Port toMap()
  {
    return toMap(true);
  }
  
  public Port toMap(boolean toMap)
  {
    this.toMap = toMap;
    return this;
  }
  
  public List<Integer> getPorts()
  {
    return this.ports;
  }
  
  public Protocol getProtocol()
  {
    return this.protocols;
  }
  
  public String getDescription()
  {
    return this.description;
  }
  
  public Port setDescription(String description)
  {
    this.description = description;
    return this;
  }
  
  public boolean isToMap()
  {
    return this.toMap;
  }
  
  public boolean isMapped()
  {
    return this.isMapped;
  }
  
  public Port setMapped(boolean value)
  {
    this.isMapped = value;
    return this;
  }
  
  public int getAssignedPort()
  {
    return this.assignedPort;
  }
  
  public Port setAssignedPort(int assignedPort)
  {
    this.assignedPort = assignedPort;
    return this;
  }
  
  public InetAddress getAddress()
  {
    return this.address;
  }
  
  public PortMapping[] getMappings()
  {
    if (!isToMap()) {
      return new PortMapping[0];
    }
    List<PortMapping> mappings = new ArrayList();
    switch (getProtocol())
    {
    case TCP: 
      mappings.add(new PortMapping(getAssignedPort(), this.address.getHostAddress(), PortMapping.Protocol.TCP, getDescription()));
      break;
    case UDP: 
      mappings.add(new PortMapping(getAssignedPort(), this.address.getHostAddress(), PortMapping.Protocol.UDP, getDescription()));
      break;
    case Both: 
      mappings.add(new PortMapping(getAssignedPort(), this.address.getHostAddress(), PortMapping.Protocol.UDP, getDescription()));
      mappings.add(new PortMapping(getAssignedPort(), this.address.getHostAddress(), PortMapping.Protocol.TCP, getDescription()));
    }
    return (PortMapping[])mappings.toArray(new PortMapping[0]);
  }
  
  public Collection<IPortValidator> getValidators()
  {
    return this.validators;
  }
  
  public Port addValidators(IPortValidator... validators)
  {
    this.validators.addAll(Arrays.asList(validators));
    return this;
  }
  
  public String toString()
  {
    return getDescription() + " (" + getAddress().getHostAddress() + ":" + getAssignedPort() + ")";
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\coffee\keenan\network\helpers\port\Port.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */