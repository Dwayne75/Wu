package com.sun.javaws;

import com.sun.javaws.jnl.AssociationDesc;
import com.sun.javaws.jnl.LaunchDesc;
import java.io.IOException;
import java.net.URL;
import java.util.Date;

public abstract interface LocalApplicationProperties
{
  public abstract URL getLocation();
  
  public abstract String getVersionId();
  
  public abstract LaunchDesc getLaunchDescriptor();
  
  public abstract void setLastAccessed(Date paramDate);
  
  public abstract Date getLastAccessed();
  
  public abstract int getLaunchCount();
  
  public abstract void incrementLaunchCount();
  
  public abstract void setAskedForInstall(boolean paramBoolean);
  
  public abstract boolean getAskedForInstall();
  
  public abstract void setRebootNeeded(boolean paramBoolean);
  
  public abstract boolean isRebootNeeded();
  
  public abstract void setLocallyInstalled(boolean paramBoolean);
  
  public abstract boolean isLocallyInstalled();
  
  public abstract boolean isLocallyInstalledSystem();
  
  public abstract boolean forceUpdateCheck();
  
  public abstract void setForceUpdateCheck(boolean paramBoolean);
  
  public abstract boolean isApplicationDescriptor();
  
  public abstract boolean isExtensionDescriptor();
  
  public abstract AssociationDesc[] getAssociations();
  
  public abstract void addAssociation(AssociationDesc paramAssociationDesc);
  
  public abstract void setAssociations(AssociationDesc[] paramArrayOfAssociationDesc);
  
  public abstract String getNativeLibDirectory();
  
  public abstract String getInstallDirectory();
  
  public abstract void setNativeLibDirectory(String paramString);
  
  public abstract void setInstallDirectory(String paramString);
  
  public abstract String getRegisteredTitle();
  
  public abstract void setRegisteredTitle(String paramString);
  
  public abstract void put(String paramString1, String paramString2);
  
  public abstract String get(String paramString);
  
  public abstract int getInteger(String paramString);
  
  public abstract boolean getBoolean(String paramString);
  
  public abstract Date getDate(String paramString);
  
  public abstract void store()
    throws IOException;
  
  public abstract void refreshIfNecessary();
  
  public abstract void refresh();
  
  public abstract boolean isShortcutSupported();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\javaws\LocalApplicationProperties.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */