package org.fourthline.cling.binding.xml;

import org.fourthline.cling.model.Namespace;
import org.fourthline.cling.model.ValidationException;
import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.profile.RemoteClientInfo;
import org.w3c.dom.Document;

public abstract interface DeviceDescriptorBinder
{
  public abstract <T extends Device> T describe(T paramT, String paramString)
    throws DescriptorBindingException, ValidationException;
  
  public abstract <T extends Device> T describe(T paramT, Document paramDocument)
    throws DescriptorBindingException, ValidationException;
  
  public abstract String generate(Device paramDevice, RemoteClientInfo paramRemoteClientInfo, Namespace paramNamespace)
    throws DescriptorBindingException;
  
  public abstract Document buildDOM(Device paramDevice, RemoteClientInfo paramRemoteClientInfo, Namespace paramNamespace)
    throws DescriptorBindingException;
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\binding\xml\DeviceDescriptorBinder.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */