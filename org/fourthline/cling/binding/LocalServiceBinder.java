package org.fourthline.cling.binding;

import org.fourthline.cling.model.meta.LocalService;
import org.fourthline.cling.model.types.ServiceId;
import org.fourthline.cling.model.types.ServiceType;

public abstract interface LocalServiceBinder
{
  public abstract LocalService read(Class<?> paramClass)
    throws LocalServiceBindingException;
  
  public abstract LocalService read(Class<?> paramClass, ServiceId paramServiceId, ServiceType paramServiceType, boolean paramBoolean, Class[] paramArrayOfClass)
    throws LocalServiceBindingException;
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\binding\LocalServiceBinder.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */