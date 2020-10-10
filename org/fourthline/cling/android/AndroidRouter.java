package org.fourthline.cling.android;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.MulticastLock;
import android.net.wifi.WifiManager.WifiLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.fourthline.cling.UpnpServiceConfiguration;
import org.fourthline.cling.model.ModelUtil;
import org.fourthline.cling.protocol.ProtocolFactory;
import org.fourthline.cling.transport.Router;
import org.fourthline.cling.transport.RouterException;
import org.fourthline.cling.transport.RouterImpl;
import org.fourthline.cling.transport.spi.InitializationException;
import org.seamless.util.Exceptions;

public class AndroidRouter
  extends RouterImpl
{
  private static final Logger log = Logger.getLogger(Router.class.getName());
  private final Context context;
  private final WifiManager wifiManager;
  protected WifiManager.MulticastLock multicastLock;
  protected WifiManager.WifiLock wifiLock;
  protected NetworkInfo networkInfo;
  protected BroadcastReceiver broadcastReceiver;
  
  public AndroidRouter(UpnpServiceConfiguration configuration, ProtocolFactory protocolFactory, Context context)
    throws InitializationException
  {
    super(configuration, protocolFactory);
    
    this.context = context;
    this.wifiManager = ((WifiManager)context.getSystemService("wifi"));
    this.networkInfo = NetworkUtils.getConnectedNetworkInfo(context);
    if (!ModelUtil.ANDROID_EMULATOR)
    {
      this.broadcastReceiver = createConnectivityBroadcastReceiver();
      context.registerReceiver(this.broadcastReceiver, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
    }
  }
  
  protected BroadcastReceiver createConnectivityBroadcastReceiver()
  {
    return new ConnectivityBroadcastReceiver();
  }
  
  protected int getLockTimeoutMillis()
  {
    return 15000;
  }
  
  public void shutdown()
    throws RouterException
  {
    super.shutdown();
    unregisterBroadcastReceiver();
  }
  
  public boolean enable()
    throws RouterException
  {
    lock(this.writeLock);
    try
    {
      boolean enabled;
      if ((enabled = super.enable())) {
        if (isWifi())
        {
          setWiFiMulticastLock(true);
          setWifiLock(true);
        }
      }
      return enabled;
    }
    finally
    {
      unlock(this.writeLock);
    }
  }
  
  public boolean disable()
    throws RouterException
  {
    lock(this.writeLock);
    try
    {
      if (isWifi())
      {
        setWiFiMulticastLock(false);
        setWifiLock(false);
      }
      return super.disable();
    }
    finally
    {
      unlock(this.writeLock);
    }
  }
  
  public NetworkInfo getNetworkInfo()
  {
    return this.networkInfo;
  }
  
  public boolean isMobile()
  {
    return NetworkUtils.isMobile(this.networkInfo);
  }
  
  public boolean isWifi()
  {
    return NetworkUtils.isWifi(this.networkInfo);
  }
  
  public boolean isEthernet()
  {
    return NetworkUtils.isEthernet(this.networkInfo);
  }
  
  public boolean enableWiFi()
  {
    log.info("Enabling WiFi...");
    try
    {
      return this.wifiManager.setWifiEnabled(true);
    }
    catch (Throwable t)
    {
      log.log(Level.WARNING, "SetWifiEnabled failed", t);
    }
    return false;
  }
  
  public void unregisterBroadcastReceiver()
  {
    if (this.broadcastReceiver != null)
    {
      this.context.unregisterReceiver(this.broadcastReceiver);
      this.broadcastReceiver = null;
    }
  }
  
  protected void setWiFiMulticastLock(boolean enable)
  {
    if (this.multicastLock == null) {
      this.multicastLock = this.wifiManager.createMulticastLock(getClass().getSimpleName());
    }
    if (enable)
    {
      if (this.multicastLock.isHeld())
      {
        log.warning("WiFi multicast lock already acquired");
      }
      else
      {
        log.info("WiFi multicast lock acquired");
        this.multicastLock.acquire();
      }
    }
    else if (this.multicastLock.isHeld())
    {
      log.info("WiFi multicast lock released");
      this.multicastLock.release();
    }
    else
    {
      log.warning("WiFi multicast lock already released");
    }
  }
  
  protected void setWifiLock(boolean enable)
  {
    if (this.wifiLock == null) {
      this.wifiLock = this.wifiManager.createWifiLock(3, getClass().getSimpleName());
    }
    if (enable)
    {
      if (this.wifiLock.isHeld())
      {
        log.warning("WiFi lock already acquired");
      }
      else
      {
        log.info("WiFi lock acquired");
        this.wifiLock.acquire();
      }
    }
    else if (this.wifiLock.isHeld())
    {
      log.info("WiFi lock released");
      this.wifiLock.release();
    }
    else
    {
      log.warning("WiFi lock already released");
    }
  }
  
  protected void onNetworkTypeChange(NetworkInfo oldNetwork, NetworkInfo newNetwork)
    throws RouterException
  {
    log.info(String.format("Network type changed %s => %s", new Object[] { oldNetwork == null ? "" : oldNetwork
      .getTypeName(), newNetwork == null ? "NONE" : newNetwork
      .getTypeName() }));
    if (disable()) {
      log.info(String.format("Disabled router on network type change (old network: %s)", new Object[] { oldNetwork == null ? "NONE" : oldNetwork
      
        .getTypeName() }));
    }
    this.networkInfo = newNetwork;
    if (enable()) {
      log.info(String.format("Enabled router on network type change (new network: %s)", new Object[] { newNetwork == null ? "NONE" : newNetwork
      
        .getTypeName() }));
    }
  }
  
  protected void handleRouterExceptionOnNetworkTypeChange(RouterException ex)
  {
    Throwable cause = Exceptions.unwrap(ex);
    if ((cause instanceof InterruptedException)) {
      log.log(Level.INFO, "Router was interrupted: " + ex, cause);
    } else {
      log.log(Level.WARNING, "Router error on network change: " + ex, ex);
    }
  }
  
  class ConnectivityBroadcastReceiver
    extends BroadcastReceiver
  {
    ConnectivityBroadcastReceiver() {}
    
    public void onReceive(Context context, Intent intent)
    {
      if (!intent.getAction().equals("android.net.conn.CONNECTIVITY_CHANGE")) {
        return;
      }
      displayIntentInfo(intent);
      
      NetworkInfo newNetworkInfo = NetworkUtils.getConnectedNetworkInfo(context);
      if ((AndroidRouter.this.networkInfo != null) && (newNetworkInfo == null)) {
        for (int i = 1; i <= 3; i++)
        {
          try
          {
            Thread.sleep(1000L);
          }
          catch (InterruptedException e)
          {
            return;
          }
          AndroidRouter.log.warning(String.format("%s => NONE network transition, waiting for new network... retry #%d", new Object[] {AndroidRouter.this.networkInfo
          
            .getTypeName(), Integer.valueOf(i) }));
          
          newNetworkInfo = NetworkUtils.getConnectedNetworkInfo(context);
          if (newNetworkInfo != null) {
            break;
          }
        }
      }
      if (isSameNetworkType(AndroidRouter.this.networkInfo, newNetworkInfo)) {
        AndroidRouter.log.info("No actual network change... ignoring event!");
      } else {
        try
        {
          AndroidRouter.this.onNetworkTypeChange(AndroidRouter.this.networkInfo, newNetworkInfo);
        }
        catch (RouterException ex)
        {
          AndroidRouter.this.handleRouterExceptionOnNetworkTypeChange(ex);
        }
      }
    }
    
    protected boolean isSameNetworkType(NetworkInfo network1, NetworkInfo network2)
    {
      if ((network1 == null) && (network2 == null)) {
        return true;
      }
      if ((network1 == null) || (network2 == null)) {
        return false;
      }
      return network1.getType() == network2.getType();
    }
    
    protected void displayIntentInfo(Intent intent)
    {
      boolean noConnectivity = intent.getBooleanExtra("noConnectivity", false);
      String reason = intent.getStringExtra("reason");
      boolean isFailover = intent.getBooleanExtra("isFailover", false);
      
      NetworkInfo currentNetworkInfo = (NetworkInfo)intent.getParcelableExtra("networkInfo");
      NetworkInfo otherNetworkInfo = (NetworkInfo)intent.getParcelableExtra("otherNetwork");
      
      AndroidRouter.log.info("Connectivity change detected...");
      AndroidRouter.log.info("EXTRA_NO_CONNECTIVITY: " + noConnectivity);
      AndroidRouter.log.info("EXTRA_REASON: " + reason);
      AndroidRouter.log.info("EXTRA_IS_FAILOVER: " + isFailover);
      AndroidRouter.log.info("EXTRA_NETWORK_INFO: " + (currentNetworkInfo == null ? "none" : currentNetworkInfo));
      AndroidRouter.log.info("EXTRA_OTHER_NETWORK_INFO: " + (otherNetworkInfo == null ? "none" : otherNetworkInfo));
      AndroidRouter.log.info("EXTRA_EXTRA_INFO: " + intent.getStringExtra("extraInfo"));
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\android\AndroidRouter.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */