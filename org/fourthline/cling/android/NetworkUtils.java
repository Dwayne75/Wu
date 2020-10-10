package org.fourthline.cling.android;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import java.util.logging.Logger;
import org.fourthline.cling.model.ModelUtil;

public class NetworkUtils
{
  private static final Logger log = Logger.getLogger(NetworkUtils.class.getName());
  
  public static NetworkInfo getConnectedNetworkInfo(Context context)
  {
    ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService("connectivity");
    
    NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
    if ((networkInfo != null) && (networkInfo.isAvailable()) && (networkInfo.isConnected())) {
      return networkInfo;
    }
    networkInfo = connectivityManager.getNetworkInfo(1);
    if ((networkInfo != null) && (networkInfo.isAvailable()) && (networkInfo.isConnected())) {
      return networkInfo;
    }
    networkInfo = connectivityManager.getNetworkInfo(0);
    if ((networkInfo != null) && (networkInfo.isAvailable()) && (networkInfo.isConnected())) {
      return networkInfo;
    }
    networkInfo = connectivityManager.getNetworkInfo(6);
    if ((networkInfo != null) && (networkInfo.isAvailable()) && (networkInfo.isConnected())) {
      return networkInfo;
    }
    networkInfo = connectivityManager.getNetworkInfo(9);
    if ((networkInfo != null) && (networkInfo.isAvailable()) && (networkInfo.isConnected())) {
      return networkInfo;
    }
    log.info("Could not find any connected network...");
    
    return null;
  }
  
  public static boolean isEthernet(NetworkInfo networkInfo)
  {
    return isNetworkType(networkInfo, 9);
  }
  
  public static boolean isWifi(NetworkInfo networkInfo)
  {
    return (isNetworkType(networkInfo, 1)) || (ModelUtil.ANDROID_EMULATOR);
  }
  
  public static boolean isMobile(NetworkInfo networkInfo)
  {
    return (isNetworkType(networkInfo, 0)) || (isNetworkType(networkInfo, 6));
  }
  
  public static boolean isNetworkType(NetworkInfo networkInfo, int type)
  {
    return (networkInfo != null) && (networkInfo.getType() == type);
  }
  
  public static boolean isSSDPAwareNetwork(NetworkInfo networkInfo)
  {
    return (isWifi(networkInfo)) || (isEthernet(networkInfo));
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\android\NetworkUtils.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */