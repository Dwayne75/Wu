package org.fourthline.cling.android;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import org.fourthline.cling.UpnpService;
import org.fourthline.cling.UpnpServiceConfiguration;
import org.fourthline.cling.UpnpServiceImpl;
import org.fourthline.cling.controlpoint.ControlPoint;
import org.fourthline.cling.protocol.ProtocolFactory;
import org.fourthline.cling.registry.Registry;
import org.fourthline.cling.registry.RegistryListener;
import org.fourthline.cling.transport.Router;

public class AndroidUpnpServiceImpl
  extends Service
{
  protected UpnpService upnpService;
  protected Binder binder;
  
  public AndroidUpnpServiceImpl()
  {
    this.binder = new Binder();
  }
  
  public void onCreate()
  {
    super.onCreate();
    
    this.upnpService = new UpnpServiceImpl(createConfiguration(), new RegistryListener[0])
    {
      protected Router createRouter(ProtocolFactory protocolFactory, Registry registry)
      {
        return AndroidUpnpServiceImpl.this.createRouter(
          getConfiguration(), protocolFactory, AndroidUpnpServiceImpl.this);
      }
      
      public synchronized void shutdown()
      {
        ((AndroidRouter)getRouter()).unregisterBroadcastReceiver();
        
        super.shutdown(true);
      }
    };
  }
  
  protected UpnpServiceConfiguration createConfiguration()
  {
    return new AndroidUpnpServiceConfiguration();
  }
  
  protected AndroidRouter createRouter(UpnpServiceConfiguration configuration, ProtocolFactory protocolFactory, Context context)
  {
    return new AndroidRouter(configuration, protocolFactory, context);
  }
  
  public IBinder onBind(Intent intent)
  {
    return this.binder;
  }
  
  public void onDestroy()
  {
    this.upnpService.shutdown();
    super.onDestroy();
  }
  
  protected class Binder
    extends Binder
    implements AndroidUpnpService
  {
    protected Binder() {}
    
    public UpnpService get()
    {
      return AndroidUpnpServiceImpl.this.upnpService;
    }
    
    public UpnpServiceConfiguration getConfiguration()
    {
      return AndroidUpnpServiceImpl.this.upnpService.getConfiguration();
    }
    
    public Registry getRegistry()
    {
      return AndroidUpnpServiceImpl.this.upnpService.getRegistry();
    }
    
    public ControlPoint getControlPoint()
    {
      return AndroidUpnpServiceImpl.this.upnpService.getControlPoint();
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\android\AndroidUpnpServiceImpl.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */