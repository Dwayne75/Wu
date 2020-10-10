package org.fourthline.cling.binding.xml;

import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;
import org.fourthline.cling.binding.staging.MutableDevice;
import org.fourthline.cling.binding.staging.MutableIcon;
import org.fourthline.cling.binding.staging.MutableService;
import org.fourthline.cling.binding.staging.MutableUDAVersion;
import org.fourthline.cling.model.ValidationException;
import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.types.DLNACaps;
import org.fourthline.cling.model.types.DLNADoc;
import org.fourthline.cling.model.types.InvalidValueException;
import org.fourthline.cling.model.types.ServiceId;
import org.fourthline.cling.model.types.ServiceType;
import org.fourthline.cling.model.types.UDN;
import org.seamless.util.MimeType;
import org.seamless.xml.SAXParser;
import org.seamless.xml.SAXParser.Handler;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class UDA10DeviceDescriptorBinderSAXImpl
  extends UDA10DeviceDescriptorBinderImpl
{
  private static Logger log = Logger.getLogger(DeviceDescriptorBinder.class.getName());
  
  public <D extends Device> D describe(D undescribedDevice, String descriptorXml)
    throws DescriptorBindingException, ValidationException
  {
    if ((descriptorXml == null) || (descriptorXml.length() == 0)) {
      throw new DescriptorBindingException("Null or empty descriptor");
    }
    try
    {
      log.fine("Populating device from XML descriptor: " + undescribedDevice);
      
      SAXParser parser = new SAXParser();
      
      MutableDevice descriptor = new MutableDevice();
      new RootHandler(descriptor, parser);
      
      parser.parse(new InputSource(new StringReader(descriptorXml
      
        .trim())));
      
      return descriptor.build(undescribedDevice);
    }
    catch (ValidationException ex)
    {
      throw ex;
    }
    catch (Exception ex)
    {
      throw new DescriptorBindingException("Could not parse device descriptor: " + ex.toString(), ex);
    }
  }
  
  protected static class RootHandler
    extends UDA10DeviceDescriptorBinderSAXImpl.DeviceDescriptorHandler<MutableDevice>
  {
    public RootHandler(MutableDevice instance, SAXParser parser)
    {
      super(parser);
    }
    
    public void startElement(Descriptor.Device.ELEMENT element, Attributes attributes)
      throws SAXException
    {
      if (element.equals(UDA10DeviceDescriptorBinderSAXImpl.SpecVersionHandler.EL))
      {
        MutableUDAVersion udaVersion = new MutableUDAVersion();
        ((MutableDevice)getInstance()).udaVersion = udaVersion;
        new UDA10DeviceDescriptorBinderSAXImpl.SpecVersionHandler(udaVersion, this);
      }
      if (element.equals(UDA10DeviceDescriptorBinderSAXImpl.DeviceHandler.EL)) {
        new UDA10DeviceDescriptorBinderSAXImpl.DeviceHandler((MutableDevice)getInstance(), this);
      }
    }
    
    public void endElement(Descriptor.Device.ELEMENT element)
      throws SAXException
    {
      switch (UDA10DeviceDescriptorBinderSAXImpl.1.$SwitchMap$org$fourthline$cling$binding$xml$Descriptor$Device$ELEMENT[element.ordinal()])
      {
      case 1: 
        try
        {
          String urlString = getCharacters();
          if ((urlString != null) && (urlString.length() > 0)) {
            ((MutableDevice)getInstance()).baseURL = new URL(urlString);
          }
        }
        catch (Exception ex)
        {
          throw new SAXException("Invalid URLBase: " + ex.toString());
        }
      }
    }
  }
  
  protected static class SpecVersionHandler
    extends UDA10DeviceDescriptorBinderSAXImpl.DeviceDescriptorHandler<MutableUDAVersion>
  {
    public static final Descriptor.Device.ELEMENT EL = Descriptor.Device.ELEMENT.specVersion;
    
    public SpecVersionHandler(MutableUDAVersion instance, UDA10DeviceDescriptorBinderSAXImpl.DeviceDescriptorHandler parent)
    {
      super(parent);
    }
    
    public void endElement(Descriptor.Device.ELEMENT element)
      throws SAXException
    {
      switch (UDA10DeviceDescriptorBinderSAXImpl.1.$SwitchMap$org$fourthline$cling$binding$xml$Descriptor$Device$ELEMENT[element.ordinal()])
      {
      case 2: 
        String majorVersion = getCharacters().trim();
        if (!majorVersion.equals("1"))
        {
          UDA10DeviceDescriptorBinderSAXImpl.log.warning("Unsupported UDA major version, ignoring: " + majorVersion);
          majorVersion = "1";
        }
        ((MutableUDAVersion)getInstance()).major = Integer.valueOf(majorVersion).intValue();
        break;
      case 3: 
        String minorVersion = getCharacters().trim();
        if (!minorVersion.equals("0"))
        {
          UDA10DeviceDescriptorBinderSAXImpl.log.warning("Unsupported UDA minor version, ignoring: " + minorVersion);
          minorVersion = "0";
        }
        ((MutableUDAVersion)getInstance()).minor = Integer.valueOf(minorVersion).intValue();
      }
    }
    
    public boolean isLastElement(Descriptor.Device.ELEMENT element)
    {
      return element.equals(EL);
    }
  }
  
  protected static class DeviceHandler
    extends UDA10DeviceDescriptorBinderSAXImpl.DeviceDescriptorHandler<MutableDevice>
  {
    public static final Descriptor.Device.ELEMENT EL = Descriptor.Device.ELEMENT.device;
    
    public DeviceHandler(MutableDevice instance, UDA10DeviceDescriptorBinderSAXImpl.DeviceDescriptorHandler parent)
    {
      super(parent);
    }
    
    public void startElement(Descriptor.Device.ELEMENT element, Attributes attributes)
      throws SAXException
    {
      if (element.equals(UDA10DeviceDescriptorBinderSAXImpl.IconListHandler.EL))
      {
        List<MutableIcon> icons = new ArrayList();
        ((MutableDevice)getInstance()).icons = icons;
        new UDA10DeviceDescriptorBinderSAXImpl.IconListHandler(icons, this);
      }
      if (element.equals(UDA10DeviceDescriptorBinderSAXImpl.ServiceListHandler.EL))
      {
        List<MutableService> services = new ArrayList();
        ((MutableDevice)getInstance()).services = services;
        new UDA10DeviceDescriptorBinderSAXImpl.ServiceListHandler(services, this);
      }
      if (element.equals(UDA10DeviceDescriptorBinderSAXImpl.DeviceListHandler.EL))
      {
        List<MutableDevice> devices = new ArrayList();
        ((MutableDevice)getInstance()).embeddedDevices = devices;
        new UDA10DeviceDescriptorBinderSAXImpl.DeviceListHandler(devices, this);
      }
    }
    
    public void endElement(Descriptor.Device.ELEMENT element)
      throws SAXException
    {
      switch (UDA10DeviceDescriptorBinderSAXImpl.1.$SwitchMap$org$fourthline$cling$binding$xml$Descriptor$Device$ELEMENT[element.ordinal()])
      {
      case 4: 
        ((MutableDevice)getInstance()).deviceType = getCharacters();
        break;
      case 5: 
        ((MutableDevice)getInstance()).friendlyName = getCharacters();
        break;
      case 6: 
        ((MutableDevice)getInstance()).manufacturer = getCharacters();
        break;
      case 7: 
        ((MutableDevice)getInstance()).manufacturerURI = UDA10DeviceDescriptorBinderImpl.parseURI(getCharacters());
        break;
      case 8: 
        ((MutableDevice)getInstance()).modelDescription = getCharacters();
        break;
      case 9: 
        ((MutableDevice)getInstance()).modelName = getCharacters();
        break;
      case 10: 
        ((MutableDevice)getInstance()).modelNumber = getCharacters();
        break;
      case 11: 
        ((MutableDevice)getInstance()).modelURI = UDA10DeviceDescriptorBinderImpl.parseURI(getCharacters());
        break;
      case 12: 
        ((MutableDevice)getInstance()).presentationURI = UDA10DeviceDescriptorBinderImpl.parseURI(getCharacters());
        break;
      case 13: 
        ((MutableDevice)getInstance()).upc = getCharacters();
        break;
      case 14: 
        ((MutableDevice)getInstance()).serialNumber = getCharacters();
        break;
      case 15: 
        ((MutableDevice)getInstance()).udn = UDN.valueOf(getCharacters());
        break;
      case 16: 
        String txt = getCharacters();
        try
        {
          ((MutableDevice)getInstance()).dlnaDocs.add(DLNADoc.valueOf(txt));
        }
        catch (InvalidValueException ex)
        {
          UDA10DeviceDescriptorBinderSAXImpl.log.info("Invalid X_DLNADOC value, ignoring value: " + txt);
        }
      case 17: 
        ((MutableDevice)getInstance()).dlnaCaps = DLNACaps.valueOf(getCharacters());
      }
    }
    
    public boolean isLastElement(Descriptor.Device.ELEMENT element)
    {
      return element.equals(EL);
    }
  }
  
  protected static class IconListHandler
    extends UDA10DeviceDescriptorBinderSAXImpl.DeviceDescriptorHandler<List<MutableIcon>>
  {
    public static final Descriptor.Device.ELEMENT EL = Descriptor.Device.ELEMENT.iconList;
    
    public IconListHandler(List<MutableIcon> instance, UDA10DeviceDescriptorBinderSAXImpl.DeviceDescriptorHandler parent)
    {
      super(parent);
    }
    
    public void startElement(Descriptor.Device.ELEMENT element, Attributes attributes)
      throws SAXException
    {
      if (element.equals(UDA10DeviceDescriptorBinderSAXImpl.IconHandler.EL))
      {
        MutableIcon icon = new MutableIcon();
        ((List)getInstance()).add(icon);
        new UDA10DeviceDescriptorBinderSAXImpl.IconHandler(icon, this);
      }
    }
    
    public boolean isLastElement(Descriptor.Device.ELEMENT element)
    {
      return element.equals(EL);
    }
  }
  
  protected static class IconHandler
    extends UDA10DeviceDescriptorBinderSAXImpl.DeviceDescriptorHandler<MutableIcon>
  {
    public static final Descriptor.Device.ELEMENT EL = Descriptor.Device.ELEMENT.icon;
    
    public IconHandler(MutableIcon instance, UDA10DeviceDescriptorBinderSAXImpl.DeviceDescriptorHandler parent)
    {
      super(parent);
    }
    
    public void endElement(Descriptor.Device.ELEMENT element)
      throws SAXException
    {
      switch (UDA10DeviceDescriptorBinderSAXImpl.1.$SwitchMap$org$fourthline$cling$binding$xml$Descriptor$Device$ELEMENT[element.ordinal()])
      {
      case 18: 
        ((MutableIcon)getInstance()).width = Integer.valueOf(getCharacters()).intValue();
        break;
      case 19: 
        ((MutableIcon)getInstance()).height = Integer.valueOf(getCharacters()).intValue();
        break;
      case 20: 
        try
        {
          ((MutableIcon)getInstance()).depth = Integer.valueOf(getCharacters()).intValue();
        }
        catch (NumberFormatException ex)
        {
          UDA10DeviceDescriptorBinderSAXImpl.log.warning("Invalid icon depth '" + getCharacters() + "', using 16 as default: " + ex);
          ((MutableIcon)getInstance()).depth = 16;
        }
      case 21: 
        ((MutableIcon)getInstance()).uri = UDA10DeviceDescriptorBinderImpl.parseURI(getCharacters());
        break;
      case 22: 
        try
        {
          ((MutableIcon)getInstance()).mimeType = getCharacters();
          MimeType.valueOf(((MutableIcon)getInstance()).mimeType);
        }
        catch (IllegalArgumentException ex)
        {
          UDA10DeviceDescriptorBinderSAXImpl.log.warning("Ignoring invalid icon mime type: " + ((MutableIcon)getInstance()).mimeType);
          ((MutableIcon)getInstance()).mimeType = "";
        }
      }
    }
    
    public boolean isLastElement(Descriptor.Device.ELEMENT element)
    {
      return element.equals(EL);
    }
  }
  
  protected static class ServiceListHandler
    extends UDA10DeviceDescriptorBinderSAXImpl.DeviceDescriptorHandler<List<MutableService>>
  {
    public static final Descriptor.Device.ELEMENT EL = Descriptor.Device.ELEMENT.serviceList;
    
    public ServiceListHandler(List<MutableService> instance, UDA10DeviceDescriptorBinderSAXImpl.DeviceDescriptorHandler parent)
    {
      super(parent);
    }
    
    public void startElement(Descriptor.Device.ELEMENT element, Attributes attributes)
      throws SAXException
    {
      if (element.equals(UDA10DeviceDescriptorBinderSAXImpl.ServiceHandler.EL))
      {
        MutableService service = new MutableService();
        ((List)getInstance()).add(service);
        new UDA10DeviceDescriptorBinderSAXImpl.ServiceHandler(service, this);
      }
    }
    
    public boolean isLastElement(Descriptor.Device.ELEMENT element)
    {
      boolean last = element.equals(EL);
      if (last)
      {
        Iterator<MutableService> it = ((List)getInstance()).iterator();
        while (it.hasNext())
        {
          MutableService service = (MutableService)it.next();
          if ((service.serviceType == null) || (service.serviceId == null)) {
            it.remove();
          }
        }
      }
      return last;
    }
  }
  
  protected static class ServiceHandler
    extends UDA10DeviceDescriptorBinderSAXImpl.DeviceDescriptorHandler<MutableService>
  {
    public static final Descriptor.Device.ELEMENT EL = Descriptor.Device.ELEMENT.service;
    
    public ServiceHandler(MutableService instance, UDA10DeviceDescriptorBinderSAXImpl.DeviceDescriptorHandler parent)
    {
      super(parent);
    }
    
    public void endElement(Descriptor.Device.ELEMENT element)
      throws SAXException
    {
      try
      {
        switch (UDA10DeviceDescriptorBinderSAXImpl.1.$SwitchMap$org$fourthline$cling$binding$xml$Descriptor$Device$ELEMENT[element.ordinal()])
        {
        case 23: 
          ((MutableService)getInstance()).serviceType = ServiceType.valueOf(getCharacters());
          break;
        case 24: 
          ((MutableService)getInstance()).serviceId = ServiceId.valueOf(getCharacters());
          break;
        case 25: 
          ((MutableService)getInstance()).descriptorURI = UDA10DeviceDescriptorBinderImpl.parseURI(getCharacters());
          break;
        case 26: 
          ((MutableService)getInstance()).controlURI = UDA10DeviceDescriptorBinderImpl.parseURI(getCharacters());
          break;
        case 27: 
          ((MutableService)getInstance()).eventSubscriptionURI = UDA10DeviceDescriptorBinderImpl.parseURI(getCharacters());
        }
      }
      catch (InvalidValueException ex)
      {
        UDA10DeviceDescriptorBinderSAXImpl.log.warning("UPnP specification violation, skipping invalid service declaration. " + ex
          .getMessage());
      }
    }
    
    public boolean isLastElement(Descriptor.Device.ELEMENT element)
    {
      return element.equals(EL);
    }
  }
  
  protected static class DeviceListHandler
    extends UDA10DeviceDescriptorBinderSAXImpl.DeviceDescriptorHandler<List<MutableDevice>>
  {
    public static final Descriptor.Device.ELEMENT EL = Descriptor.Device.ELEMENT.deviceList;
    
    public DeviceListHandler(List<MutableDevice> instance, UDA10DeviceDescriptorBinderSAXImpl.DeviceDescriptorHandler parent)
    {
      super(parent);
    }
    
    public void startElement(Descriptor.Device.ELEMENT element, Attributes attributes)
      throws SAXException
    {
      if (element.equals(UDA10DeviceDescriptorBinderSAXImpl.DeviceHandler.EL))
      {
        MutableDevice device = new MutableDevice();
        ((List)getInstance()).add(device);
        new UDA10DeviceDescriptorBinderSAXImpl.DeviceHandler(device, this);
      }
    }
    
    public boolean isLastElement(Descriptor.Device.ELEMENT element)
    {
      return element.equals(EL);
    }
  }
  
  protected static class DeviceDescriptorHandler<I>
    extends SAXParser.Handler<I>
  {
    public DeviceDescriptorHandler(I instance)
    {
      super();
    }
    
    public DeviceDescriptorHandler(I instance, SAXParser parser)
    {
      super(parser);
    }
    
    public DeviceDescriptorHandler(I instance, DeviceDescriptorHandler parent)
    {
      super(parent);
    }
    
    public DeviceDescriptorHandler(I instance, SAXParser parser, DeviceDescriptorHandler parent)
    {
      super(parser, parent);
    }
    
    public void startElement(String uri, String localName, String qName, Attributes attributes)
      throws SAXException
    {
      super.startElement(uri, localName, qName, attributes);
      Descriptor.Device.ELEMENT el = Descriptor.Device.ELEMENT.valueOrNullOf(localName);
      if (el == null) {
        return;
      }
      startElement(el, attributes);
    }
    
    public void endElement(String uri, String localName, String qName)
      throws SAXException
    {
      super.endElement(uri, localName, qName);
      Descriptor.Device.ELEMENT el = Descriptor.Device.ELEMENT.valueOrNullOf(localName);
      if (el == null) {
        return;
      }
      endElement(el);
    }
    
    protected boolean isLastElement(String uri, String localName, String qName)
    {
      Descriptor.Device.ELEMENT el = Descriptor.Device.ELEMENT.valueOrNullOf(localName);
      return (el != null) && (isLastElement(el));
    }
    
    public void startElement(Descriptor.Device.ELEMENT element, Attributes attributes)
      throws SAXException
    {}
    
    public void endElement(Descriptor.Device.ELEMENT element)
      throws SAXException
    {}
    
    public boolean isLastElement(Descriptor.Device.ELEMENT element)
    {
      return false;
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\binding\xml\UDA10DeviceDescriptorBinderSAXImpl.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */