package org.fourthline.cling.binding.xml;

import org.w3c.dom.Node;

public abstract class Descriptor
{
  public static abstract interface Device
  {
    public static final String NAMESPACE_URI = "urn:schemas-upnp-org:device-1-0";
    public static final String DLNA_NAMESPACE_URI = "urn:schemas-dlna-org:device-1-0";
    public static final String DLNA_PREFIX = "dlna";
    public static final String SEC_NAMESPACE_URI = "http://www.sec.co.kr/dlna";
    public static final String SEC_PREFIX = "sec";
    
    public static enum ELEMENT
    {
      root,  specVersion,  major,  minor,  URLBase,  device,  UDN,  X_DLNADOC,  X_DLNACAP,  ProductCap,  X_ProductCap,  deviceType,  friendlyName,  manufacturer,  manufacturerURL,  modelDescription,  modelName,  modelNumber,  modelURL,  presentationURL,  UPC,  serialNumber,  iconList,  icon,  width,  height,  depth,  url,  mimetype,  serviceList,  service,  serviceType,  serviceId,  SCPDURL,  controlURL,  eventSubURL,  deviceList;
      
      private ELEMENT() {}
      
      public static ELEMENT valueOrNullOf(String s)
      {
        try
        {
          return valueOf(s);
        }
        catch (IllegalArgumentException ex) {}
        return null;
      }
      
      public boolean equals(Node node)
      {
        return toString().equals(node.getLocalName());
      }
    }
  }
  
  public static abstract interface Service
  {
    public static final String NAMESPACE_URI = "urn:schemas-upnp-org:service-1-0";
    
    public static enum ELEMENT
    {
      scpd,  specVersion,  major,  minor,  actionList,  action,  name,  argumentList,  argument,  direction,  relatedStateVariable,  retval,  serviceStateTable,  stateVariable,  dataType,  defaultValue,  allowedValueList,  allowedValue,  allowedValueRange,  minimum,  maximum,  step;
      
      private ELEMENT() {}
      
      public static ELEMENT valueOrNullOf(String s)
      {
        try
        {
          return valueOf(s);
        }
        catch (IllegalArgumentException ex) {}
        return null;
      }
      
      public boolean equals(Node node)
      {
        return toString().equals(node.getLocalName());
      }
    }
    
    public static enum ATTRIBUTE
    {
      sendEvents;
      
      private ATTRIBUTE() {}
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\binding\xml\Descriptor.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */