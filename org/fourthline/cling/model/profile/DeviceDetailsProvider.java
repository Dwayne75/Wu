package org.fourthline.cling.model.profile;

import org.fourthline.cling.model.meta.DeviceDetails;

public abstract interface DeviceDetailsProvider
{
  public abstract DeviceDetails provide(RemoteClientInfo paramRemoteClientInfo);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\model\profile\DeviceDetailsProvider.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */